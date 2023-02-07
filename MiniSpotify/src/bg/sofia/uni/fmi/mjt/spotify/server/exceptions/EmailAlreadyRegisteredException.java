package bg.sofia.uni.fmi.mjt.spotify.server.exceptions;

public class EmailAlreadyRegisteredException extends SpotifyException {
    public EmailAlreadyRegisteredException(String message) {
        super(message);
    }

    public EmailAlreadyRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }
}
