package bg.sofia.uni.fmi.mjt.server.logger;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SpotifyLoggerTest {

    private final static String SPOTIFY_LOGGER_NAME = "SpotifyLoggerTest.log";
    private final static String SPOTIFY_LOGGER_PATH = "data" + File.separator + SPOTIFY_LOGGER_NAME;

    @Test
    void testSpotifyLoggerSuccessfullyCreated() throws IOException {

        SpotifyLogger spotifyLogger = new SpotifyLogger(SPOTIFY_LOGGER_NAME);

        Path expectedFilePath = Paths.get(SPOTIFY_LOGGER_PATH);
        assertTrue(Files.exists(expectedFilePath), "SpotifyLoggerTest.log must be created but actually it is not");

        if (Files.isWritable(expectedFilePath)) {

            spotifyLogger.log(Level.INFO, "This a test INFO log.", new IllegalArgumentException("Illegal argument"));
            assertTrue(Files.size(expectedFilePath) > 0, "SpotifyLoggerTest.log must not be empty after a log");
        }

        Arrays.stream(spotifyLogger.getLogger().getHandlers())
                .forEach(Handler::close);

        if (!Files.deleteIfExists(expectedFilePath)) {

            fail("The SpotifyLoggerTest.log file must be deleted but it is not.");
        }
    }
}
