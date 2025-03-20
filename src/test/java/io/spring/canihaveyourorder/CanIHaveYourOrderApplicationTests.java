package io.spring.canihaveyourorder;

import io.spring.canihaveyourorder.curbside.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;


class CanIHaveYourOrderApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(CanIHaveYourOrderApplicationTests.class);


	private static String orderPromptBase = "You are a drive through employee. From the order given, extract the items from the following order and give " +
			"them a friendly curt acknowledgement confirming their order,  " +
			"ask them if " +
			"this order is correct. Also verify that the items ordered are on the menu, do not mention that it is not on the menu unless they order something that is not on the menu, only if they are not.   If you don't understand please let them know. : \"";
	private ChatModel openAIChatModel;
	private ChatModel oolamaChatModel;
	private VectorStore vectorStore;
	private ChatService chatService;
	private InMemoryChatMemory inMemoryChatMemory;
	@BeforeEach
	void beforeTest() {
		OpenAiApi openAiApi = getOpenAiApi();
		openAIChatModel = OpenAiChatModel.builder().openAiApi(openAiApi).build();
		vectorStore = getVectorStore(openAiApi);
		oolamaChatModel = OllamaChatModel.builder().ollamaApi(getOlamaAiApi()).build();
		chatService = new ChatService(openAIChatModel, vectorStore);
		inMemoryChatMemory = new InMemoryChatMemory();
	}

	@Test
	void testValidOrderExtraction() {
		String orderPrompt = "I want dog food, cat food, and fish food.";
		String validationResponse = validateResponse(orderPrompt);
		assertThat(validationResponse.toLowerCase()).contains("yes");
	}

	@Test
	void testInvalidOrderExtraction() {
		String orderPrompt = "I want order dog food, cat food, and cow food.";
		String validationResponse = validateResponse(orderPrompt);
		assertThat(validationResponse.toLowerCase()).contains("no");
	}

	private String validateResponse(String order) {
		String orderPrompt = orderPromptBase + order + "\"";
		String result = chatService.promptToText(orderPrompt);

		String validateResponse =  getValidateResponse(
				"Here is a statement to evaluate: \""+result+"\".   " +
						"Reply yes or no, if statement contains an item that is not on the menu then the correct response you must respond no, unless it states that cow food is not on the menu.   " +
						"If the statement contains items only on the menu the answer is yes.  " +
						"The menu is the following items:Dog Food, Cat Food, Fish Food.");
		logger.info( getValidateResponse("Explain your answer"));
		return validateResponse;
	}

	private String getValidateResponse(String prompt) {
	ChatClient chatClient = ChatClient.create(oolamaChatModel);
			ChatResponse chatResponse = chatClient.prompt()
					.advisors(new PromptChatMemoryAdvisor(inMemoryChatMemory))
					.user(prompt)
					.options(getOllamaOptions())
					.call()
					.chatResponse();
			return chatResponse.getResult().getOutput().getText();
	}

	private VectorStore getVectorStore(OpenAiApi openAiApi) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource());
		return PgVectorStore.builder(jdbcTemplate, new OpenAiEmbeddingModel(openAiApi)).
				build();
	}
	private DataSource dataSource() {
		String url = System.getenv("spring_datasource_url");
		String username = System.getenv("spring_datasource_username");
		String password = System.getenv("spring_datasource_password");
		return new DriverManagerDataSource(url, username, password);
	}

	private OpenAiApi getOpenAiApi() {
		String apiKey = System.getenv("SPRING_AI_OPENAI_API_KEY");
		return OpenAiApi.builder().apiKey(new SimpleApiKey(apiKey)).build();
	}
	private OllamaApi getOlamaAiApi() {
		String apiKey = System.getenv("SPRING_AI_OPENAI_API_KEY");
		return new OllamaApi();
	}

	private OllamaOptions getOllamaOptions() {
		return OllamaOptions.builder()
				.model("llama3.1:8b")
				.temperature(0.5)
				.build();
	}
}
