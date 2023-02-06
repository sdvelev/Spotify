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

public class PlaySongThread extends Thread {

    private final static String DATA_PATH = "data" + File.separator + "music" + File.separator;
    private final static String WAV_FORMAT = ".wav";

    private final String songName;
    private final SelectionKey selectionKey;
    private boolean isRunning;
    private final StreamingPlatform streamingPlatform;
    private final SpotifyLogger spotifyLogger;

    public PlaySongThread(String songName, SelectionKey selectionKey, StreamingPlatform streamingPlatform,
                          SpotifyLogger spotifyLogger) {

        this.isRunning = false;
        this.songName = songName;
        this.selectionKey = selectionKey;
        this.streamingPlatform = streamingPlatform;
        this.spotifyLogger = spotifyLogger;
    }

    @Override
    public void run() {

        isRunning = true;
        try {

            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(DATA_PATH + songName +
                WAV_FORMAT));

            AudioFormat format = stream.getFormat();
            if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {

                format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(),
                    format.getSampleSizeInBits(), format.getChannels(), format.getFrameSize(), format.getFrameRate(),
                    true);

                stream = AudioSystem.getAudioInputStream(format, stream);
            }

            SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, stream.getFormat(),
                ((int) stream.getFrameLength() * format.getFrameSize()));

            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

            line.open(stream.getFormat());
            line.start();

            int numRead;
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

            spotifyLogger.log(Level.SEVERE, ServerReply.STOP_COMMAND_ERROR_REPLY.getReply(), e);
        }

        isRunning = false;
        removeFrom();
    }

    public void terminateSong() {

        isRunning = false;
    }

    private void removeFrom() {

        synchronized (streamingPlatform) {

            streamingPlatform.getAlreadyRunning().remove(selectionKey);
            streamingPlatform.notifyAll();
        }

    }

}
