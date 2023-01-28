package bg.sofia.uni.fmi.mjt.server.exceptions;

public class EmailAlreadyRegisteredException extends Exception {
    public EmailAlreadyRegisteredException(String message) {
        super(message);
    }

    public EmailAlreadyRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }
}
