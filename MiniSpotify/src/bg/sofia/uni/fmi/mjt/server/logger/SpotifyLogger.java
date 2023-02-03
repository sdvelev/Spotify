package bg.sofia.uni.fmi.mjt.server.logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SpotifyLogger {

    private final static Logger SPOTIFY_LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final static String SPOTIFY_LOGGER_PATH = "data" + File.separator;
    //private SpotifyLogger spotifyLogger = new SpotifyLogger("spotifyLogger.log");
    private static FileHandler fileHandler;

   /* public Logger getLogger() {

        return SPOTIFY_LOGGER;
    }*/
    public SpotifyLogger(String nameLogger) {

        try {

            SPOTIFY_LOGGER.setUseParentHandlers(false);
            fileHandler = new FileHandler(SPOTIFY_LOGGER_PATH + nameLogger, true);
            SPOTIFY_LOGGER.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void log(Level level, String message, Exception exception) {

        SPOTIFY_LOGGER.log(level, message, exception);
    }

    public void log(Level level, String message) {

        SPOTIFY_LOGGER.log(level, message);
    }

   /* public static void main(String[] args) {
        SpotifyLogger spotifyLogger = new SpotifyLogger();

        spotifyLogger.getSpotifyLogger().info("This is second info log.");

    }*/

}
