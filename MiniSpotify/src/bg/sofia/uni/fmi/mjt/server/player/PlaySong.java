package bg.sofia.uni.fmi.mjt.server.player;

import bg.sofia.uni.fmi.mjt.server.ServerReply;
import bg.sofia.uni.fmi.mjt.server.StreamingPlatform;
import bg.sofia.uni.fmi.mjt.server.logger.SpotifyLogger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.logging.Level;

public class PlaySong extends Thread {

    private String songName;
    private SelectionKey selectionKey;
    private boolean isRunning;
    private StreamingPlatform streamingPlatform;
    private final static String DATA_PATH = "data/";
    private final static String WAV_FORMAT = ".wav";


    public PlaySong(String songName, SelectionKey selectionKey, StreamingPlatform streamingPlatform) {

        this.isRunning = false;
        this.songName = songName;
        this.selectionKey = selectionKey;
        this.streamingPlatform = streamingPlatform;
    }

    @Override
    public void run() {

        this.isRunning = true;
        try {

            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(DATA_PATH + this.songName +
                WAV_FORMAT));
            // stream = AudioSystem.getAudioInputStream(new URL(
            //      "http://hostname/audiofile"));

            // AudioFormat audioFormat = AudioSystem
            //     .getAudioInputStream(new File("data/Hans Zimmer_The Crown - Main title.wav")).getFormat();

            AudioFormat format = stream.getFormat();
            if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format
                    .getSampleRate(), format.getSampleSizeInBits() /** 2*/, format
                    .getChannels(), format.getFrameSize() /** 2*/, format.getFrameRate(),
                    true); // big endian
                stream = AudioSystem.getAudioInputStream(format, stream);
            }

            SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, stream
                .getFormat(), ((int) stream.getFrameLength() * format.getFrameSize()));
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(stream.getFormat());
            line.start();

            int numRead = 0;
            byte[] buf = new byte[line.getBufferSize()];
            while ((numRead = stream.read(buf, 0, buf.length)) >= 0 && isRunning) {
                int offset = 0;
                while (offset < numRead) {
                    offset += line.write(buf, offset, numRead - offset);
                }
            }
            line.drain();
            line.stop();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {

            SpotifyLogger.log(Level.SEVERE, ServerReply.STOP_COMMAND_ERROR_REPLY.getReply(), e);
        }

        this.isRunning = false;
        removeFrom();
    }

    private void removeFrom() {

        this.streamingPlatform.getAlreadyRunning().remove(selectionKey);
    }

    public void terminateSong() {

        this.isRunning = false;
    }

}
