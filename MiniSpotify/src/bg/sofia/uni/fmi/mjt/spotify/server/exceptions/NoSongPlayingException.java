package bg.sofia.uni.fmi.mjt.spotify.server.exceptions;

public class NoSongPlayingException extends SpotifyException {
    public NoSongPlayingException(String message) {
        super(message);
    }

    public NoSongPlayingException(String message, Throwable cause) {
        super(message, cause);
    }
}
