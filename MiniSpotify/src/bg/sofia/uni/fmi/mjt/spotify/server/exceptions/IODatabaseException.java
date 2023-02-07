package bg.sofia.uni.fmi.mjt.spotify.server.exceptions;

public class IODatabaseException extends SpotifyException {
    public IODatabaseException(String message) {
        super(message);
    }

    public IODatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
