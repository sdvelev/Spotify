package bg.sofia.uni.fmi.mjt.server.exceptions;

public class LoggerNotCreatedSuccessfullyException extends RuntimeException {

    public LoggerNotCreatedSuccessfullyException(String message) {
        super(message);
    }

    public LoggerNotCreatedSuccessfullyException(String message, Throwable cause) {
        super(message, cause);
    }
}
