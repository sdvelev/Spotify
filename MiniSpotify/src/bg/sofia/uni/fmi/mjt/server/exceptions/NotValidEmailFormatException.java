package bg.sofia.uni.fmi.mjt.server.exceptions;

public class NotValidEmailFormatException extends Exception {

    public NotValidEmailFormatException(String message) {
        super(message);
    }

    public NotValidEmailFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
