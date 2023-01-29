package bg.sofia.uni.fmi.mjt.server.exceptions;

public class NoSongPlayingException extends Exception {
    public NoSongPlayingException(String message) {
        super(message);
    }

    public NoSongPlayingException(String message, Throwable cause) {
        super(message, cause);
    }
}
