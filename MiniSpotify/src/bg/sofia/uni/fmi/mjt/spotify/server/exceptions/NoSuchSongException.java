package bg.sofia.uni.fmi.mjt.spotify.server.exceptions;

public class NoSuchSongException extends SpotifyException {
    public NoSuchSongException(String message) {
        super(message);
    }

    public NoSuchSongException(String message, Throwable cause) {
        super(message, cause);
    }
}
