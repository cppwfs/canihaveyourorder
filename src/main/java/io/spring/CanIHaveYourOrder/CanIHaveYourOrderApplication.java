package io.spring.CanIHaveYourOrder;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;

@SpringBootApplication
public class CanIHaveYourOrderApplication {


	private static final AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, true);
	private static final File wavFile = new File("recording.wav");

	public static void main(String[] args) {
		SpringApplication.run(CanIHaveYourOrderApplication.class, args);
	}

	@Bean
	ApplicationRunner applicationRunner(OpenAiAudioTranscriptionModel transcriptionModel,
										ChatModel chatModel,
										SpeechModel speechModel) {
		return args -> {
			try {
				recordAudio();
				System.out.println("Recording saved as " + wavFile.getAbsolutePath());
				String order = speechToText(transcriptionModel);
				String response = respond(order, chatModel);
				respondViaVoice(response,speechModel);
				//TODO: stuff happens
				//TODO: Send Event to order fullfillment using Pulsar-Binder
				//TODO: Order fish food and we have no fishfood
				//TODO: Send Event to Judger
				//TODO: App sends or updates VectorDB with fishfood
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	}

	String speechToText(OpenAiAudioTranscriptionModel transcriptionModel) {
		AudioTranscriptionResponse response = transcriptionModel.call(new AudioTranscriptionPrompt(new FileSystemResource("/Users/grenfro/Downloads/CanIHaveYourOrder/recording.wav")));
		String text = response.getResult().getOutput();
		System.out.println(text);
		return text;
	}

	String respond(String order, ChatModel chatModel){
		ChatClient chatClient = ChatClient.create(chatModel);
		ChatResponse chatResponse = chatClient.prompt()
				.user("From the order give, extract the items from the following order and give them a friendly acknowledgement confirming their order.  If you don't understand please let them know. : \"" + order + "\"")
				.call()
				.chatResponse();
		System.out.println(chatResponse.getResult().getOutput().getContent());
		return chatResponse.getResult().getOutput().getContent();
	}
	void respondViaVoice(String response, SpeechModel speechModel) {
		OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
				.withModel("tts-1")
				.withVoice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
				.withResponseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
				.withSpeed(1.0f)
				.build();

		SpeechPrompt speechPrompt = new SpeechPrompt(response);
		SpeechResponse speechResponse = speechModel.call(speechPrompt);
		byte[] audio = speechResponse.getResult().getOutput();
		storeMp3toFile(audio);
//		playWavFromByteArray(audio);
	}
	void storeMp3toFile(byte[] audioResponse) {
		FileSystemResource fileSystemResource = new FileSystemResource("response.mp3");
		try (OutputStream outputStream = fileSystemResource.getOutputStream()) {
			outputStream.write(audioResponse);
			System.out.println("Data has been written to " + fileSystemResource.getFile().getAbsolutePath());
		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
			e.printStackTrace();
		}	}
	public void playWavFromByteArray(byte[] wavData) {
		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(wavData);
			 AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(byteArrayInputStream)) {

			// Get the audio format and open a line to play it
			AudioFormat audioFormat = audioInputStream.getFormat();
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
			SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(info);

			// Open the line and start playback
			sourceLine.open(audioFormat);
			sourceLine.start();

			System.out.println("Playing WAV audio...");

			// Read the audio data and play it
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = audioInputStream.read(buffer)) != -1) {
				sourceLine.write(buffer, 0, bytesRead);
			}

			// Drain and close the line after playback is complete
			sourceLine.drain();
			sourceLine.close();
			System.out.println("Playback finished.");

		} catch (UnsupportedAudioFileException e) {
			System.err.println("Unsupported audio format: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error reading audio data: " + e.getMessage());
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			System.err.println("Audio line unavailable: " + e.getMessage());
			e.printStackTrace();
		}
	}


	void recordAudio() throws LineUnavailableException, IOException {
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
		TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);

		line.open(audioFormat);
		line.start();

		System.out.println("Recording... Press ENTER to stop.");
		Thread recordingThread = new Thread(() -> {
			try (AudioInputStream audioStream = new AudioInputStream(line)) {
				AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, wavFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		recordingThread.start();

		System.in.read(); // Wait for the user to press ENTER
		line.stop();
		line.close();
	}

}
