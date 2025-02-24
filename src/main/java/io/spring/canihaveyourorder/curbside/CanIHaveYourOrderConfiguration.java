package io.spring.canihaveyourorder.curbside;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class CanIHaveYourOrderConfiguration {

    @Bean
    SpeechHandler speechHandler(OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel, SpeechModel speechModel) {
        return new SpeechHandler(openAiAudioTranscriptionModel, speechModel);
    }

    @Bean
    ChatService chatService(ChatModel chatModel, VectorStore vectorStore) {
        return new ChatService(chatModel, vectorStore);
    }

    @Bean
    Consumer<String> myConsunmer() {
        return o -> {
            System.out.println(o);
        };
    }
}
