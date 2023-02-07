package bg.sofia.uni.fmi.mjt.spotify.server.exceptions;

public class UserNotFoundException extends SpotifyException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
