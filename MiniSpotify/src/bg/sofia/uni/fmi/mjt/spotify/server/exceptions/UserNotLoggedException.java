package bg.sofia.uni.fmi.mjt.spotify.server.exceptions;

public class UserNotLoggedException extends SpotifyException {
    public UserNotLoggedException(String message) {
        super(message);
    }

    public UserNotLoggedException(String message, Throwable cause) {
        super(message, cause);
    }
}
