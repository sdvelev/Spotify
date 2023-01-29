package bg.sofia.uni.fmi.mjt.server.exceptions;

public class SongIsAlreadyPlayingException extends Exception {
    public SongIsAlreadyPlayingException(String message) {
        super(message);
    }

    public SongIsAlreadyPlayingException(String message, Throwable cause) {
        super(message, cause);
    }
}
