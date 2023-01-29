package bg.sofia.uni.fmi.mjt.server.exceptions;

public class NoSuchSongException extends Exception {
    public NoSuchSongException(String message) {
        super(message);
    }

    public NoSuchSongException(String message, Throwable cause) {
        super(message, cause);
    }
}
