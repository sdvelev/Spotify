package bg.sofia.uni.fmi.mjt.server.exceptions;

public class SongAlreadyInPlaylistException extends Exception {
    public SongAlreadyInPlaylistException(String message) {
        super(message);
    }

    public SongAlreadyInPlaylistException(String message, Throwable cause) {
        super(message, cause);
    }
}
