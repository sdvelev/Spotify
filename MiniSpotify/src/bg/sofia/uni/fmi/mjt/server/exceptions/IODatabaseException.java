package bg.sofia.uni.fmi.mjt.server.exceptions;

public class IODatabaseException extends Exception {
    public IODatabaseException(String message) {
        super(message);
    }

    public IODatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
