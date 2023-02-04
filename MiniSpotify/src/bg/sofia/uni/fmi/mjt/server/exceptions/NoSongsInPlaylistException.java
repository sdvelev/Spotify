package bg.sofia.uni.fmi.mjt.server.exceptions;

public class NoSongsInPlaylistException extends Exception {
    public NoSongsInPlaylistException(String message) {
        super(message);
    }

    public NoSongsInPlaylistException(String message, Throwable cause) {
        super(message, cause);
    }
}
