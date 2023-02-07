package bg.sofia.uni.fmi.mjt.spotify.server.exceptions;

public class SongIsAlreadyPlayingException extends SpotifyException {
    public SongIsAlreadyPlayingException(String message) {
        super(message);
    }

    public SongIsAlreadyPlayingException(String message, Throwable cause) {
        super(message, cause);
    }
}
