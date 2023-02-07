package bg.sofia.uni.fmi.mjt.spotify.server.exceptions;

public class PlaylistAlreadyExistException extends SpotifyException {
    public PlaylistAlreadyExistException(String message) {
        super(message);
    }

    public PlaylistAlreadyExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
