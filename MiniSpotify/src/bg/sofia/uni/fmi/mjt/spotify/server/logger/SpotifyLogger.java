package bg.sofia.uni.fmi.mjt.spotify.server.logger;

import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.LoggerNotCreatedSuccessfullyException;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SpotifyLogger {

    private static final Logger SPOTIFY_LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final String SPOTIFY_LOGGER_PATH = "data" + File.separator;
    private static final String SPOTIFY_LOGGER_NOT_CREATED_MESSAGE = "The SpotifyLogger object was not created " +
        "successfully.";

    public SpotifyLogger(String nameLogger) {
        try {
            SPOTIFY_LOGGER.setUseParentHandlers(false);
            FileHandler fileHandler = new FileHandler(SPOTIFY_LOGGER_PATH + nameLogger, true);

            SPOTIFY_LOGGER.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
            throw new LoggerNotCreatedSuccessfullyException(SPOTIFY_LOGGER_NOT_CREATED_MESSAGE, e);
        }
    }

    public void log(Level level, String message, Exception exception) {
        SPOTIFY_LOGGER.log(level, message, exception);
    }

    public Logger getLogger() {
        return SPOTIFY_LOGGER;
    }
}