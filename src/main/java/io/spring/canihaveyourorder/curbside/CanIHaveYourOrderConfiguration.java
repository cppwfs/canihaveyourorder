package io.spring.canihaveyourorder.curbside;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class CanIHaveYourOrderConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CanIHaveYourOrderConfiguration.class);


    @Bean
    SpeechHandler speechHandler(OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel, SpeechModel speechModel) {
        return new SpeechHandler(openAiAudioTranscriptionModel, speechModel);
    }

    @Bean
    ChatService chatService(ChatModel chatModel) {
        return new ChatService(chatModel);
    }

    @Bean
    Consumer<String> myConsumer() {
        return logger::info;
    }
}
