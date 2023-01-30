package bg.sofia.uni.fmi.mjt.server.exceptions;

public class UserAlreadyLoggedException extends Exception {
    public UserAlreadyLoggedException(String message) {
        super(message);
    }

    public UserAlreadyLoggedException(String message, Throwable cause) {
        super(message, cause);
    }
}
