package bg.sofia.uni.fmi.mjt.server.exceptions;

public class NoSuchPlaylistException extends Exception {
    public NoSuchPlaylistException(String message) {
        super(message);
    }

    public NoSuchPlaylistException(String message, Throwable cause) {
        super(message, cause);
    }
}
