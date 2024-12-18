package io.spring.CanIHaveYourOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.core.io.FileSystemResource;


/**
 * Provides speechToText and TextTo speech services as well as creation of chat responses.
 */
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final SpeechModel speechModel;
    private final ChatModel chatModel;

    ChatService(OpenAiAudioTranscriptionModel transcriptionModel, SpeechModel speechModel, ChatModel chatModel) {
        this.transcriptionModel = transcriptionModel;
        this.speechModel = speechModel;
        this.chatModel = chatModel;
    }

    String speechToText(String wavAbsolutePath) {
        AudioTranscriptionResponse response = transcriptionModel.call(new AudioTranscriptionPrompt(new FileSystemResource(wavAbsolutePath)));
        String text = response.getResult().getOutput();
        System.out.println(text);
        return text;
    }

    byte[] textToSpeech(String text) {
        SpeechPrompt speechPrompt = new SpeechPrompt(text);
        SpeechResponse speechResponse = speechModel.call(speechPrompt);
        return speechResponse.getResult().getOutput();
    }

    String promptToText(String prompt) {
        ChatClient chatClient = ChatClient.create(chatModel);
        ChatResponse chatResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .chatResponse();
        String result =  chatResponse.getResult().getOutput().getContent();
        log.info(result);
        return result;
    }
}
