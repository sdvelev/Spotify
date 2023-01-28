package bg.sofia.uni.fmi.mjt.server.logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SpotifyLogger {

    private static final Logger SPOTIFY_LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static SpotifyLogger spotifyLogger = new SpotifyLogger(SPOTIFY_LOGGER);
    private static FileHandler fileHandler;

    private Logger getLogger() {
        return SPOTIFY_LOGGER;
    }
    private SpotifyLogger(Logger spotifyLogger) {

        try {

            spotifyLogger.setUseParentHandlers(false);
            fileHandler = new FileHandler("data/spotifyLogger.log", true);
            spotifyLogger.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void log(Level level, String message, Exception exception) {

        spotifyLogger.getLogger().log(level, message, exception);
    }

   /* public static void main(String[] args) {
        SpotifyLogger spotifyLogger = new SpotifyLogger();

        spotifyLogger.getSpotifyLogger().info("This is second info log.");

    }*/

}
