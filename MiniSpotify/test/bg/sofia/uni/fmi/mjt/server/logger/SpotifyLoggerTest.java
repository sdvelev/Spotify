package bg.sofia.uni.fmi.mjt.server.logger;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SpotifyLoggerTest {

    private final static String SPOTIFY_LOGGER_NAME = "SpotifyLoggerTest.log";
    private final static String SPOTIFY_LOGGER_PATH = "data" + File.separator + SPOTIFY_LOGGER_NAME;

    @Test
    void testSpotifyLoggerSuccessfullyCreated() {

        SpotifyLogger spotifyLogger = new SpotifyLogger(SPOTIFY_LOGGER_NAME);

        File expectedFile = new File(SPOTIFY_LOGGER_PATH);
        assertTrue(expectedFile.exists(), "SpotifyLoggerTest.log must be created but actually it is not");

        spotifyLogger.log(Level.INFO, "This a test INFO log.", new IllegalArgumentException("Illegal argument"));
        assertTrue(expectedFile.length() > 0, "SpotifyLoggerTest.log must not be empty after a log");

        Arrays.stream(spotifyLogger.getLogger().getHandlers())
                .forEach(handler -> handler.close());

        if (!expectedFile.delete()) {

            fail("The SpotifyLoggerTest.log file must be deleted but it is not.");
        }
    }

}
