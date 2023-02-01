package bg.sofia.uni.fmi.mjt.server.exceptions;

public class PlaylistNotEmptyException extends Exception {
    public PlaylistNotEmptyException(String message) {
        super(message);
    }

    public PlaylistNotEmptyException(String message, Throwable cause) {
        super(message, cause);
    }
}
