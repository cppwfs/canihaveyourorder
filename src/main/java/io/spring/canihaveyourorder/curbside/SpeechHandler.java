package io.spring.canihaveyourorder.curbside;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.model.Model;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.core.io.FileSystemResource;

/**
 * Handles the recording and playback of speech from your laptop.
 */
@SuppressWarnings("unchecked")
public class SpeechHandler {
    private static final Logger log = LoggerFactory.getLogger(SpeechHandler.class);

    private static final AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, true);

    private final Model transcriptionModel;
    private final SpeechModel speechModel;

    public SpeechHandler(Model transcriptionModel, SpeechModel speechModel) {
        this.transcriptionModel = transcriptionModel;
        this.speechModel = speechModel;
    }

    /**
     * Produced the text for the wav file that it was provided.
     * @return String containing the text from the wav file provided.
     */
    public String recordOrder() throws Exception{
        String wavAbsolutePath = recordAudio();
        AudioTranscriptionResponse response =
                (AudioTranscriptionResponse) transcriptionModel.call(new AudioTranscriptionPrompt(new FileSystemResource(wavAbsolutePath)));
        return response.getResult().getOutput();
    }

    /**
     * Converts the provided text response into spoken audio using text-to-speech
     * and plays the audio.
     * This method combines text-to-speech processing and audio playback functionality.
     * @param response the text input to be converted into speech and played back
     */
    public void respondViaVoice(String response) {
        playResponse(textToSpeech(response));
    }

    /**
     * Provides a mp3 file containing spoken word for the text provided.
     * @param text the text to be transformed to speech
     * @return mp3 file containing the spoken word contained in the text
     */
    public byte[] textToSpeech(String text) {
        SpeechPrompt speechPrompt = new SpeechPrompt(text);
        SpeechResponse speechResponse = speechModel.call(speechPrompt);
        return speechResponse.getResult().getOutput();
    }

    /**
     * Accepts the audio stored in mp3 format as a byte array and plays the sound via your machine's speakers.
     * @param audioResponse mp3 content as a byte array.
     */
    public void playResponse(byte[] audioResponse)  {
        String mp3FilePath = storeMp3toFile(audioResponse);
        Media media = new Media(new File(mp3FilePath).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();

        log.info("Complete");
    }


    private String storeMp3toFile(byte[] audioResponse) {
        FileSystemResource fileSystemResource = new FileSystemResource("response.mp3");
        try (OutputStream outputStream = fileSystemResource.getOutputStream()) {
            outputStream.write(audioResponse);
            log.debug("Data has been written to {}", fileSystemResource.getFile().getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return fileSystemResource.getFile().getAbsolutePath();
    }
    /**
     * Records audio via your laptops microphone and stores it to a file specified by the file name parameter.
     * To start the user must press enter and press enter again to stop.
     * @return the absolute path where the file is stored.
     * @throws LineUnavailableException
     * @throws IOException
     */
    private  String recordAudio() throws LineUnavailableException, IOException {
        File wavFile = new File("recording.wav");
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);

        line.open(audioFormat);
        line.start();

        System.out.println("Recording... Press ENTER to stop.");
        Thread recordingThread = new Thread(() -> {
            try (AudioInputStream audioStream = new AudioInputStream(line)) {
                AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, wavFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        recordingThread.start();

        System.in.read(); // Wait for the user to press ENTER
        line.stop();
        line.close();
        String wavAbsolutePath = wavFile.getAbsolutePath();
        log.debug("Recording saved as {}", wavAbsolutePath);

        return wavAbsolutePath;
    }
}
