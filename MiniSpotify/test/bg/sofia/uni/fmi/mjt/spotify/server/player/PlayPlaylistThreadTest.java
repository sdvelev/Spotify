package bg.sofia.uni.fmi.mjt.spotify.server.player;

import bg.sofia.uni.fmi.mjt.spotify.server.ServerReply;
import bg.sofia.uni.fmi.mjt.spotify.server.StreamingPlatform;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.IODatabaseException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.NoSuchSongException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.SongIsAlreadyPlayingException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.SpotifyException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.UserNotLoggedException;
import bg.sofia.uni.fmi.mjt.spotify.server.logger.SpotifyLogger;
import bg.sofia.uni.fmi.mjt.spotify.server.login.User;
import bg.sofia.uni.fmi.mjt.spotify.server.storage.Playlist;
import bg.sofia.uni.fmi.mjt.spotify.server.storage.Song;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.channels.SelectionKey;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlayPlaylistThreadTest {


    @Mock
    private StreamingPlatform streamingPlatformMock;

    @Mock
    private SelectionKey selectionKeyMock;

    @Mock
    private SpotifyLogger spotifyLoggerMock;

    @Test
    void testRunPlayPlaylistThreadUserNotLoggedException() throws SpotifyException, InterruptedException {
        PlayPlaylistThread playPlaylistThread = new PlayPlaylistThread("CrownMusic", selectionKeyMock,
            streamingPlatformMock, spotifyLoggerMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Map<String, Set<Playlist>> toReturnMap = new LinkedHashMap<>();

        Playlist toReturnPlaylist = new Playlist("sdvelev@gmail.com", "CrownMusic");

        Song firstSong = new Song("The Crown - Main title", "Hans Zimmer", 87, "classical");
        Song secondSong = new Song("The Crown - Bittersweet Symphony", "Richard Ashcroft", 248,
            "modern");
        toReturnPlaylist.addSong(firstSong);
        toReturnPlaylist.addSong(secondSong);

        toReturnMap.put("sdvelev@gmail.com", Set.of(toReturnPlaylist));

        when(streamingPlatformMock.getPlaylists()).thenReturn(toReturnMap);

        Exception e = new UserNotLoggedException(ServerReply.PLAY_SONG_NOT_LOGGED_REPLY.getReply());

        doThrow(e)
            .when(streamingPlatformMock).playSong("The Crown - Main title", selectionKeyMock);

        doThrow(e)
            .when(streamingPlatformMock).playSong("The Crown - Bittersweet Symphony", selectionKeyMock);

        when(streamingPlatformMock.getAlreadyRunning()).thenReturn(new LinkedHashMap<>());

        playPlaylistThread.start();
        playPlaylistThread.join();

        verify(spotifyLoggerMock, times(2)).log(Level.INFO,
            ServerReply.PLAY_SONG_NOT_LOGGED_REPLY.getReply(), e);
    }

    @Test
    void testRunPlayPlaylistThreadNoSuchSongException() throws SpotifyException, InterruptedException {
        PlayPlaylistThread playPlaylistThread = new PlayPlaylistThread("CrownMusic", selectionKeyMock,
            streamingPlatformMock, spotifyLoggerMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Map<String, Set<Playlist>> toReturnMap = new LinkedHashMap<>();

        Playlist toReturnPlaylist = new Playlist("sdvelev@gmail.com", "CrownMusic");

        Song firstSong = new Song("The Crown - Main title", "Hans Zimmer", 87, "classical");
        Song secondSong = new Song("The Crown - Bittersweet Symphony", "Richard Ashcroft", 248,
            "modern");
        toReturnPlaylist.addSong(firstSong);
        toReturnPlaylist.addSong(secondSong);

        toReturnMap.put("sdvelev@gmail.com", Set.of(toReturnPlaylist));

        when(streamingPlatformMock.getPlaylists()).thenReturn(toReturnMap);

        Exception e = new NoSuchSongException(ServerReply.PLAY_SONG_NO_SUCH_SONG_REPLY.getReply());

        doThrow(e)
            .when(streamingPlatformMock).playSong("The Crown - Main title", selectionKeyMock);

        doThrow(e)
            .when(streamingPlatformMock).playSong("The Crown - Bittersweet Symphony", selectionKeyMock);

        when(streamingPlatformMock.getAlreadyRunning()).thenReturn(new LinkedHashMap<>());

        playPlaylistThread.start();
        playPlaylistThread.join();

        verify(spotifyLoggerMock, times(2)).log(Level.INFO,
            "sdvelev@gmail.com " + ServerReply.PLAY_SONG_NO_SUCH_SONG_REPLY.getReply(), e);
    }

    @Test
    void testRunPlayPlaylistThreadSongIsAlreadyPlayingException() throws SpotifyException, InterruptedException {
        PlayPlaylistThread playPlaylistThread = new PlayPlaylistThread("CrownMusic", selectionKeyMock,
            streamingPlatformMock, spotifyLoggerMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Map<String, Set<Playlist>> toReturnMap = new LinkedHashMap<>();

        Playlist toReturnPlaylist = new Playlist("sdvelev@gmail.com", "CrownMusic");

        Song firstSong = new Song("The Crown - Main title", "Hans Zimmer", 87, "classical");
        Song secondSong = new Song("The Crown - Bittersweet Symphony", "Richard Ashcroft", 248,
            "modern");
        toReturnPlaylist.addSong(firstSong);
        toReturnPlaylist.addSong(secondSong);

        toReturnMap.put("sdvelev@gmail.com", Set.of(toReturnPlaylist));

        when(streamingPlatformMock.getPlaylists()).thenReturn(toReturnMap);

        Exception e = new SongIsAlreadyPlayingException(ServerReply.PLAY_SONG_IS_ALREADY_RUNNING_REPLY.getReply());

        doThrow(e)
            .when(streamingPlatformMock).playSong("The Crown - Main title", selectionKeyMock);

        doThrow(e)
            .when(streamingPlatformMock).playSong("The Crown - Bittersweet Symphony", selectionKeyMock);

        when(streamingPlatformMock.getAlreadyRunning()).thenReturn(new LinkedHashMap<>());

        playPlaylistThread.start();
        playPlaylistThread.join();

        verify(spotifyLoggerMock, times(2)).log(Level.INFO,
            "sdvelev@gmail.com " + ServerReply.PLAY_SONG_IS_ALREADY_RUNNING_REPLY.getReply(), e);
    }

    @Test
    void testRunPlayPlaylistThreadIODatabaseException() throws SpotifyException, InterruptedException {
        PlayPlaylistThread playPlaylistThread = new PlayPlaylistThread("CrownMusic", selectionKeyMock,
            streamingPlatformMock, spotifyLoggerMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Map<String, Set<Playlist>> toReturnMap = new LinkedHashMap<>();

        Playlist toReturnPlaylist = new Playlist("sdvelev@gmail.com", "CrownMusic");

        Song firstSong = new Song("The Crown - Main title", "Hans Zimmer", 87, "classical");
        Song secondSong = new Song("The Crown - Bittersweet Symphony", "Richard Ashcroft", 248,
            "modern");
        toReturnPlaylist.addSong(firstSong);
        toReturnPlaylist.addSong(secondSong);

        toReturnMap.put("sdvelev@gmail.com", Set.of(toReturnPlaylist));

        when(streamingPlatformMock.getPlaylists()).thenReturn(toReturnMap);

        Exception e = new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply());

        doThrow(e)
            .when(streamingPlatformMock).playSong("The Crown - Main title", selectionKeyMock);

        doThrow(e)
            .when(streamingPlatformMock).playSong("The Crown - Bittersweet Symphony", selectionKeyMock);

        when(streamingPlatformMock.getAlreadyRunning()).thenReturn(new LinkedHashMap<>());

        playPlaylistThread.start();
        playPlaylistThread.join();

        verify(spotifyLoggerMock, times(2)).log(Level.INFO,
            "sdvelev@gmail.com " + ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
    }

    @Test
    void testRunPlayPlaylistThreadNullPointerException() throws SpotifyException, InterruptedException {
        PlayPlaylistThread playPlaylistThread = new PlayPlaylistThread("CrownMusic", selectionKeyMock,
            streamingPlatformMock, spotifyLoggerMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Map<String, Set<Playlist>> toReturnMap = new LinkedHashMap<>();

        Playlist toReturnPlaylist = new Playlist("sdvelev@gmail.com", "CrownMusic");

        Song firstSong = new Song("The Crown - Main title", "Hans Zimmer", 87, "classical");

        toReturnPlaylist.addSong(firstSong);

        toReturnMap.put("sdvelev@gmail.com", Set.of(toReturnPlaylist));

        when(streamingPlatformMock.getPlaylists()).thenReturn(toReturnMap);

        Exception e = new NullPointerException(ServerReply.SERVER_EXCEPTION.getReply());

        doThrow(e)
            .when(streamingPlatformMock).playSong("The Crown - Main title", selectionKeyMock);

        Map<SelectionKey, PlaySongThread> toReturnAlreadyRunning = new LinkedHashMap<>();
        toReturnAlreadyRunning.put(selectionKeyMock, null);

        doReturn(toReturnAlreadyRunning).doReturn(new LinkedHashMap<>()).when(streamingPlatformMock).getAlreadyRunning();

        playPlaylistThread.start();
        playPlaylistThread.join();

        verify(spotifyLoggerMock, times(1)).log(Level.INFO,
            "sdvelev@gmail.com " + ServerReply.SERVER_EXCEPTION.getReply(), e);
    }
}
