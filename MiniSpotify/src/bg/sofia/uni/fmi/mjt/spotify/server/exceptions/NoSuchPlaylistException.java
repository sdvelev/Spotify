package bg.sofia.uni.fmi.mjt.spotify.server.exceptions;

public class NoSuchPlaylistException extends SpotifyException {
    public NoSuchPlaylistException(String message) {
        super(message);
    }

    public NoSuchPlaylistException(String message, Throwable cause) {
        super(message, cause);
    }
}
