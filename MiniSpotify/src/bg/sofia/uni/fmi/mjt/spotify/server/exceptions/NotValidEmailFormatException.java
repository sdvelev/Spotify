package bg.sofia.uni.fmi.mjt.spotify.server.exceptions;

public class NotValidEmailFormatException extends SpotifyException {

    public NotValidEmailFormatException(String message) {
        super(message);
    }

    public NotValidEmailFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
