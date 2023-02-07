package bg.sofia.uni.fmi.mjt.spotify.server.exceptions;

public class SpotifyException extends Exception {
    public SpotifyException(String message) {
        super(message);
    }

    public SpotifyException(String message, Throwable cause) {
        super(message, cause);
    }
}
