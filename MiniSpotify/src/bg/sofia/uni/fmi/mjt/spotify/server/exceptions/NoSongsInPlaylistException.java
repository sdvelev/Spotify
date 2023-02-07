package bg.sofia.uni.fmi.mjt.spotify.server.exceptions;

public class NoSongsInPlaylistException extends SpotifyException {
    public NoSongsInPlaylistException(String message) {
        super(message);
    }

    public NoSongsInPlaylistException(String message, Throwable cause) {
        super(message, cause);
    }
}
