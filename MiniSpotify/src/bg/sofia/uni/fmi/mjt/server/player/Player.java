package bg.sofia.uni.fmi.mjt.server.player;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Player {

    public static void main(String[] argv) throws Exception {
        AudioInputStream stream = AudioSystem.getAudioInputStream(new File(
            "data/Florian Christl_Vivaldi Variation.wav"));
       // stream = AudioSystem.getAudioInputStream(new URL(
        //      "http://hostname/audiofile"));

       // AudioFormat audioFormat = AudioSystem
       //     .getAudioInputStream(new File("data/Hans Zimmer_The Crown - Main title.wav")).getFormat();



        AudioFormat format = stream.getFormat();
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format
                .getSampleRate(), format.getSampleSizeInBits() * 2, format
                .getChannels(), format.getFrameSize() * 2, format.getFrameRate(),
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
        while ((numRead = stream.read(buf, 0, buf.length)) >= 0) {
            int offset = 0;
            while (offset < numRead) {
                offset += line.write(buf, offset, numRead - offset);
            }
        }
        line.drain();
        line.stop();
    }
}
