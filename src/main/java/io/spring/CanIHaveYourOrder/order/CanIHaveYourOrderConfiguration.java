package io.spring.CanIHaveYourOrder.order;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CanIHaveYourOrderConfiguration {

    @Bean
    SpeechHandler speechHandler(OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel, SpeechModel speechModel) {
        return new SpeechHandler(openAiAudioTranscriptionModel, speechModel);
    }

    @Bean
    ChatService chatService(ChatModel chatModel) {
        return new ChatService(chatModel);
    }
}
