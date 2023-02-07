package bg.sofia.uni.fmi.mjt.spotify.server.exceptions;

public class PlaylistNotEmptyException extends SpotifyException {
    public PlaylistNotEmptyException(String message) {
        super(message);
    }

    public PlaylistNotEmptyException(String message, Throwable cause) {
        super(message, cause);
    }
}
