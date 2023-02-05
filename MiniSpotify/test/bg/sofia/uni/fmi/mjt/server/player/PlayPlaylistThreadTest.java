package bg.sofia.uni.fmi.mjt.server.player;

import bg.sofia.uni.fmi.mjt.server.ServerReply;
import bg.sofia.uni.fmi.mjt.server.StreamingPlatform;
import bg.sofia.uni.fmi.mjt.server.exceptions.IODatabaseException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoSuchSongException;
import bg.sofia.uni.fmi.mjt.server.exceptions.SongIsAlreadyPlayingException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotLoggedException;
import bg.sofia.uni.fmi.mjt.server.logger.SpotifyLogger;
import bg.sofia.uni.fmi.mjt.server.login.User;
import bg.sofia.uni.fmi.mjt.server.storage.Playlist;
import bg.sofia.uni.fmi.mjt.server.storage.Song;
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
    void testRunPlayPlaylistThreadUserNotLoggedException() throws UserNotLoggedException, NoSuchSongException,
        IODatabaseException, SongIsAlreadyPlayingException {

        PlayPlaylistThread playPlaylistThread = new PlayPlaylistThread("CrownMusic", this.selectionKeyMock,
            this.streamingPlatformMock, this.spotifyLoggerMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Map<String, Set<Playlist>> toReturnMap = new LinkedHashMap<>();

        Playlist toReturnPlaylist = new Playlist("sdvelev@gmail.com", "CrownMusic");

        Song firstSong = new Song("The Crown - Main title", "Hans Zimmer", 87, "classical");
        Song secondSong = new Song("The Crown - Bittersweet Symphony", "Richard Ashcroft", 248,
            "modern");
        toReturnPlaylist.addSong(firstSong);
        toReturnPlaylist.addSong(secondSong);

        toReturnMap.put("sdvelev@gmail.com", Set.of(toReturnPlaylist));

        when(this.streamingPlatformMock.getPlaylists()).thenReturn(toReturnMap);

        Exception e = new UserNotLoggedException(ServerReply.PLAY_SONG_NOT_LOGGED_REPLY.getReply());

        doThrow(e)
            .when(this.streamingPlatformMock).playSong("The Crown - Main title", this.selectionKeyMock);

        doThrow(e)
            .when(this.streamingPlatformMock).playSong("The Crown - Bittersweet Symphony", this.selectionKeyMock);

        when(this.streamingPlatformMock.getAlreadyRunning()).thenReturn(new LinkedHashMap<>());

        playPlaylistThread.run();

        verify(this.spotifyLoggerMock, times(2)).log(Level.INFO,
            ServerReply.PLAY_SONG_NOT_LOGGED_REPLY.getReply(), e);
    }

    @Test
    void testRunPlayPlaylistThreadNoSuchSongException() throws UserNotLoggedException, NoSuchSongException,
        IODatabaseException, SongIsAlreadyPlayingException {

        PlayPlaylistThread playPlaylistThread = new PlayPlaylistThread("CrownMusic", this.selectionKeyMock,
            this.streamingPlatformMock, this.spotifyLoggerMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Map<String, Set<Playlist>> toReturnMap = new LinkedHashMap<>();

        Playlist toReturnPlaylist = new Playlist("sdvelev@gmail.com", "CrownMusic");

        Song firstSong = new Song("The Crown - Main title", "Hans Zimmer", 87, "classical");
        Song secondSong = new Song("The Crown - Bittersweet Symphony", "Richard Ashcroft", 248,
            "modern");
        toReturnPlaylist.addSong(firstSong);
        toReturnPlaylist.addSong(secondSong);

        toReturnMap.put("sdvelev@gmail.com", Set.of(toReturnPlaylist));

        when(this.streamingPlatformMock.getPlaylists()).thenReturn(toReturnMap);

        Exception e = new NoSuchSongException(ServerReply.PLAY_SONG_NO_SUCH_SONG_REPLY.getReply());

        doThrow(e)
            .when(this.streamingPlatformMock).playSong("The Crown - Main title", this.selectionKeyMock);

        doThrow(e)
            .when(this.streamingPlatformMock).playSong("The Crown - Bittersweet Symphony", this.selectionKeyMock);

        when(this.streamingPlatformMock.getAlreadyRunning()).thenReturn(new LinkedHashMap<>());

        playPlaylistThread.run();

        verify(this.spotifyLoggerMock, times(2)).log(Level.INFO,
            "sdvelev@gmail.com " + ServerReply.PLAY_SONG_NO_SUCH_SONG_REPLY.getReply(), e);
    }

    @Test
    void testRunPlayPlaylistThreadSongIsAlreadyPlayingException() throws UserNotLoggedException, NoSuchSongException,
        IODatabaseException, SongIsAlreadyPlayingException {

        PlayPlaylistThread playPlaylistThread = new PlayPlaylistThread("CrownMusic", this.selectionKeyMock,
            this.streamingPlatformMock, this.spotifyLoggerMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Map<String, Set<Playlist>> toReturnMap = new LinkedHashMap<>();

        Playlist toReturnPlaylist = new Playlist("sdvelev@gmail.com", "CrownMusic");

        Song firstSong = new Song("The Crown - Main title", "Hans Zimmer", 87, "classical");
        Song secondSong = new Song("The Crown - Bittersweet Symphony", "Richard Ashcroft", 248,
            "modern");
        toReturnPlaylist.addSong(firstSong);
        toReturnPlaylist.addSong(secondSong);

        toReturnMap.put("sdvelev@gmail.com", Set.of(toReturnPlaylist));

        when(this.streamingPlatformMock.getPlaylists()).thenReturn(toReturnMap);

        Exception e = new SongIsAlreadyPlayingException(ServerReply.PLAY_SONG_IS_ALREADY_RUNNING_REPLY.getReply());

        doThrow(e)
            .when(this.streamingPlatformMock).playSong("The Crown - Main title", this.selectionKeyMock);

        doThrow(e)
            .when(this.streamingPlatformMock).playSong("The Crown - Bittersweet Symphony", this.selectionKeyMock);

        when(this.streamingPlatformMock.getAlreadyRunning()).thenReturn(new LinkedHashMap<>());

        playPlaylistThread.run();

        verify(this.spotifyLoggerMock, times(2)).log(Level.INFO,
            "sdvelev@gmail.com " + ServerReply.PLAY_SONG_IS_ALREADY_RUNNING_REPLY.getReply(), e);
    }

    @Test
    void testRunPlayPlaylistThreadIODatabaseException() throws UserNotLoggedException, NoSuchSongException,
        IODatabaseException, SongIsAlreadyPlayingException {

        PlayPlaylistThread playPlaylistThread = new PlayPlaylistThread("CrownMusic", this.selectionKeyMock,
            this.streamingPlatformMock, this.spotifyLoggerMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Map<String, Set<Playlist>> toReturnMap = new LinkedHashMap<>();

        Playlist toReturnPlaylist = new Playlist("sdvelev@gmail.com", "CrownMusic");

        Song firstSong = new Song("The Crown - Main title", "Hans Zimmer", 87, "classical");
        Song secondSong = new Song("The Crown - Bittersweet Symphony", "Richard Ashcroft", 248,
            "modern");
        toReturnPlaylist.addSong(firstSong);
        toReturnPlaylist.addSong(secondSong);

        toReturnMap.put("sdvelev@gmail.com", Set.of(toReturnPlaylist));

        when(this.streamingPlatformMock.getPlaylists()).thenReturn(toReturnMap);

        Exception e = new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply());

        doThrow(e)
            .when(this.streamingPlatformMock).playSong("The Crown - Main title", this.selectionKeyMock);

        doThrow(e)
            .when(this.streamingPlatformMock).playSong("The Crown - Bittersweet Symphony", this.selectionKeyMock);

        when(this.streamingPlatformMock.getAlreadyRunning()).thenReturn(new LinkedHashMap<>());

        playPlaylistThread.run();

        verify(this.spotifyLoggerMock, times(2)).log(Level.INFO,
            "sdvelev@gmail.com " + ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
    }

    @Test
    void testRunPlayPlaylistThreadNullPointerException() throws UserNotLoggedException, NoSuchSongException,
        IODatabaseException, SongIsAlreadyPlayingException {

        PlayPlaylistThread playPlaylistThread = new PlayPlaylistThread("CrownMusic", this.selectionKeyMock,
            this.streamingPlatformMock, this.spotifyLoggerMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Map<String, Set<Playlist>> toReturnMap = new LinkedHashMap<>();

        Playlist toReturnPlaylist = new Playlist("sdvelev@gmail.com", "CrownMusic");

        Song firstSong = new Song("The Crown - Main title", "Hans Zimmer", 87, "classical");

        toReturnPlaylist.addSong(firstSong);

        toReturnMap.put("sdvelev@gmail.com", Set.of(toReturnPlaylist));

        when(this.streamingPlatformMock.getPlaylists()).thenReturn(toReturnMap);

        Exception e = new NullPointerException(ServerReply.SERVER_EXCEPTION.getReply());

        doThrow(e)
            .when(this.streamingPlatformMock).playSong("The Crown - Main title", this.selectionKeyMock);

        Map<SelectionKey, PlaySongThread> toReturnAlreadyRunning = new LinkedHashMap<>();
        toReturnAlreadyRunning.put(this.selectionKeyMock, null);

        doReturn(toReturnAlreadyRunning).doReturn(new LinkedHashMap<>()).when(this.streamingPlatformMock).getAlreadyRunning();

        playPlaylistThread.run();

        verify(this.spotifyLoggerMock, times(1)).log(Level.INFO,
            "sdvelev@gmail.com " + ServerReply.SERVER_EXCEPTION.getReply(), e);
    }
}
