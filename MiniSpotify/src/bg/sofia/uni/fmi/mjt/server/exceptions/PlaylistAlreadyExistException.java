package bg.sofia.uni.fmi.mjt.server.exceptions;

public class PlaylistAlreadyExistException extends Exception {
    public PlaylistAlreadyExistException(String message) {
        super(message);
    }

    public PlaylistAlreadyExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
