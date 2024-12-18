package io.spring.CanIHaveYourOrder;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CanIHaveYourOrderConfiguration {

    @Bean
    SpeechHandler speechHandler() {
        return new SpeechHandler();
    }

    @Bean
    ChatService chatService(OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel, SpeechModel speechModel, ChatModel chatModel) {
        return new ChatService(openAiAudioTranscriptionModel, speechModel, chatModel);
    }
}
