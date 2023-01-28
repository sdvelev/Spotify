package bg.sofia.uni.fmi.mjt.server.exceptions;

public class TopInvalidArgumentException extends Exception {
    public TopInvalidArgumentException(String message) {
        super(message);
    }

    public TopInvalidArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
