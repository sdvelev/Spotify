package bg.sofia.uni.fmi.mjt.server.exceptions;

public class UserNotLoggedException extends Exception {
    public UserNotLoggedException(String message) {
        super(message);
    }

    public UserNotLoggedException(String message, Throwable cause) {
        super(message, cause);
    }
}
