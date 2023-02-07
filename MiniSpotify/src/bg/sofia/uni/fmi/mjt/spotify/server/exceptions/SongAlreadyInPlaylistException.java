package bg.sofia.uni.fmi.mjt.spotify.server.exceptions;

public class SongAlreadyInPlaylistException extends SpotifyException {
    public SongAlreadyInPlaylistException(String message) {
        super(message);
    }

    public SongAlreadyInPlaylistException(String message, Throwable cause) {
        super(message, cause);
    }
}
