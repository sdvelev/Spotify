package bg.sofia.uni.fmi.mjt.server;

import bg.sofia.uni.fmi.mjt.server.exceptions.IODatabaseException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoSongPlayingException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoSuchPlaylistException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoSuchSongException;
import bg.sofia.uni.fmi.mjt.server.exceptions.PlaylistAlreadyExistException;
import bg.sofia.uni.fmi.mjt.server.exceptions.PlaylistNotEmptyException;
import bg.sofia.uni.fmi.mjt.server.exceptions.SongAlreadyInPlaylistException;
import bg.sofia.uni.fmi.mjt.server.exceptions.SongIsAlreadyPlayingException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotLoggedException;
import bg.sofia.uni.fmi.mjt.server.login.User;
import bg.sofia.uni.fmi.mjt.server.player.PlayPlaylist;
import bg.sofia.uni.fmi.mjt.server.player.PlaySong;
import bg.sofia.uni.fmi.mjt.server.storage.Playlist;
import bg.sofia.uni.fmi.mjt.server.storage.Song;
import bg.sofia.uni.fmi.mjt.server.storage.SongEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.channels.SelectionKey;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StreamingPlatformTest {

    private final static String SONGS_LIST = """
        [
          {
            "song": {
              "title": "The Crown - Main title",
              "artist": "Hans Zimmer",
              "duration": 87,
              "genre": "classical"
            },
            "listeningTimes": 11
          },
          {
            "song": {
              "title": "No Time To Die",
              "artist": "Billie Eilish",
              "duration": 239,
              "genre": "pop"
            },
            "listeningTimes": 22
          },
          {
            "song": {
              "title": "Vivaldi Variation",
              "artist": "Florian Christl",
              "duration": 114,
              "genre": "classical"
            },
            "listeningTimes": 30
          },
          {
            "song": {
              "title": "The Crown - Bittersweet Symphony",
              "artist": "Richard Ashcroft",
              "duration": 248,
              "genre": "modern"
            },
            "listeningTimes": 9
          }
        ]""";

    private final static String PLAYLISTS_LIST = """
        [
          {
            "emailCreator": "sdvelev@gmail.com",
            "title": "MyPlaylist",
            "playlistSongs": [
              {
                "title": "The Crown - Main title",
                "artist": "Hans Zimmer",
                "duration": 87,
                "genre": "classical"
              },
              {
                "title": "No Time To Die",
                "artist": "Billie Eilish",
                "duration": 239,
                "genre": "pop"
              }
            ]
          },
          {
            "emailCreator": "sdvelev@gmail.com",
            "title": "CrownMusic",
            "playlistSongs": [
              {
                "title": "The Crown - Main title",
                "artist": "Hans Zimmer",
                "duration": 87,
                "genre": "classical"
              },
              {
                "title": "The Crown - Bittersweet Symphony",
                "artist": "Richard Ashcroft",
                "duration": 248,
                "genre": "modern"
              }
            ]
          },
          {
            "emailCreator": "sdvelev@gmail.con",
            "title": "Playlist2",
            "playlistSongs": [
              {
                "title": "Vivaldi Variation",
                "artist": "Florian Christl",
                "duration": 114,
                "genre": "classical"
              },
              {
                "title": "The Crown - Bittersweet Symphony",
                "artist": "Richard Ashcroft",
                "duration": 248,
                "genre": "modern"
              }
            ]
          }
        ]""";

    private final static String EXPECTED_TEST_CREATE_PLAYLIST_SUCCESSFULLY = """
        [
          {
            "emailCreator": "sdvelev@gmail.com",
            "title": "MyPlaylist",
            "playlistSongs": [
              {
                "title": "The Crown - Main title",
                "artist": "Hans Zimmer",
                "duration": 87,
                "genre": "classical"
              },
              {
                "title": "No Time To Die",
                "artist": "Billie Eilish",
                "duration": 239,
                "genre": "pop"
              }
            ]
          },
          {
            "emailCreator": "sdvelev@gmail.com",
            "title": "CrownMusic",
            "playlistSongs": [
              {
                "title": "The Crown - Main title",
                "artist": "Hans Zimmer",
                "duration": 87,
                "genre": "classical"
              },
              {
                "title": "The Crown - Bittersweet Symphony",
                "artist": "Richard Ashcroft",
                "duration": 248,
                "genre": "modern"
              }
            ]
          },
          {
            "emailCreator": "sdvelev@gmail.com",
            "title": "TestPlaylist",
            "playlistSongs": []
          },
          {
            "emailCreator": "sdvelev@gmail.con",
            "title": "Playlist2",
            "playlistSongs": [
              {
                "title": "Vivaldi Variation",
                "artist": "Florian Christl",
                "duration": 114,
                "genre": "classical"
              },
              {
                "title": "The Crown - Bittersweet Symphony",
                "artist": "Richard Ashcroft",
                "duration": 248,
                "genre": "modern"
              }
            ]
          }
        ]""";

    private final static String EXPECTED_TEST_CREATE_PLAYLIST_SUCCESSFULLY_NOT_LOGGED =  """
        [
          {
            "emailCreator": "sdvelev@gmail.com",
            "title": "MyPlaylist",
            "playlistSongs": [
              {
                "title": "The Crown - Main title",
                "artist": "Hans Zimmer",
                "duration": 87,
                "genre": "classical"
              },
              {
                "title": "No Time To Die",
                "artist": "Billie Eilish",
                "duration": 239,
                "genre": "pop"
              }
            ]
          },
          {
            "emailCreator": "sdvelev@gmail.com",
            "title": "CrownMusic",
            "playlistSongs": [
              {
                "title": "The Crown - Main title",
                "artist": "Hans Zimmer",
                "duration": 87,
                "genre": "classical"
              },
              {
                "title": "The Crown - Bittersweet Symphony",
                "artist": "Richard Ashcroft",
                "duration": 248,
                "genre": "modern"
              }
            ]
          },
          {
            "emailCreator": "sdvelev@gmail.con",
            "title": "Playlist2",
            "playlistSongs": [
              {
                "title": "Vivaldi Variation",
                "artist": "Florian Christl",
                "duration": 114,
                "genre": "classical"
              },
              {
                "title": "The Crown - Bittersweet Symphony",
                "artist": "Richard Ashcroft",
                "duration": 248,
                "genre": "modern"
              }
            ]
          },
          {
            "emailCreator": "sdvelev@outlook.com",
            "title": "TestPlaylist",
            "playlistSongs": []
          }
        ]""";
    private StreamingPlatform streamingPlatform;

    @Mock
    private SelectionKey selectionKey;

    @Mock
    private Set<SelectionKey> alreadyLoggedMock = mock(Set.class);

    private Map<SelectionKey, PlaySong> alreadyRunningMock = mock(Map.class);

    @BeforeEach
    void setTests() throws IODatabaseException {

        var songsListIn = new StringReader(SONGS_LIST);
        var playlistsListIn = new StringReader(PLAYLISTS_LIST);
        var songsListOut = new StringWriter();
        var playlistsListOut = new StringWriter();

        this.streamingPlatform = new StreamingPlatform(playlistsListIn, playlistsListOut, songsListIn, songsListOut,
            alreadyLoggedMock, alreadyRunningMock);
    }

    @AfterEach
    void setTestsCleaning() throws IODatabaseException {

        try {

            this.streamingPlatform.getPlaylistsReader().close();
            this.streamingPlatform.getPlaylistsWriter().close();
            this.streamingPlatform.getSongsReader().close();
            this.streamingPlatform.getSongsWriter().close();
        } catch(IOException e) {

            throw new IODatabaseException("There is an exception in closing streams", e);
        }
    }

    @Test
    void testCreatePlaylistSuccessfully()
        throws UserNotLoggedException, PlaylistAlreadyExistException, IODatabaseException {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String playlistTitle = "TestPlaylist";
        this.streamingPlatform.createPlaylist(playlistTitle, selectionKey);

        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);

        String actual = this.streamingPlatform.getPlaylistsWriter().toString();
        assertEquals(EXPECTED_TEST_CREATE_PLAYLIST_SUCCESSFULLY, actual,
            "The actual result after creating playlist is not the same as the expected.");
    }

    @Test
    void testCreatePlaylistSuccessfullyNoPlaylistsYet()
        throws UserNotLoggedException, PlaylistAlreadyExistException, IODatabaseException {

        User user = new User("sdvelev@outlook.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String playlistTitle = "TestPlaylist";
        this.streamingPlatform.createPlaylist(playlistTitle, selectionKey);

        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);

        String actual = this.streamingPlatform.getPlaylistsWriter().toString();
        assertEquals(EXPECTED_TEST_CREATE_PLAYLIST_SUCCESSFULLY_NOT_LOGGED, actual,
            "The actual result after creating playlist is not the same as the expected.");
    }

    @Test
    void testCreatePlaylistNotLoggedException() {

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(false);

        String playlistTitle = "TestPlaylist";

        assertThrows(UserNotLoggedException.class ,() ->
                this.streamingPlatform.createPlaylist(playlistTitle, selectionKey),
            "UserNotLoggedException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testCreatePlaylistPlaylistAlreadyExistException() {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String playlistTitle = "CrownMusic";

        assertThrows(PlaylistAlreadyExistException.class, () ->
                this.streamingPlatform.createPlaylist(playlistTitle, selectionKey),
            "PlaylistAlreadyExistException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testDeletePlaylistSuccessfully() throws UserNotLoggedException, IODatabaseException,
        NoSuchPlaylistException, PlaylistNotEmptyException, PlaylistAlreadyExistException {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String playlistTitle = "TestPlaylist";
        this.streamingPlatform. createPlaylist(playlistTitle, selectionKey);

        String actual = this.streamingPlatform.getPlaylistsWriter().toString();
        assertEquals(EXPECTED_TEST_CREATE_PLAYLIST_SUCCESSFULLY, actual,
            "The actual result after creating playlist is not the same as the expected.");

        var newWriter = new StringWriter();
        this.streamingPlatform.setPlaylistsWriter(newWriter);

        this.streamingPlatform.deletePlaylist(playlistTitle, selectionKey);

        actual = this.streamingPlatform.getPlaylistsWriter().toString();

        try {
            newWriter.close();
        } catch (IOException e) {

            throw new IODatabaseException("There is a problem in closing streams.", e);
        }

        assertEquals(PLAYLISTS_LIST, actual,
            "The actual result after deleting playlist is not the same as the expected.");

        verify(this.alreadyLoggedMock, times(2)).contains(this.selectionKey);
    }

    @Test
    void testDeletePlaylistNotLoggedException() {

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(false);

        String playlistTitle = "CrownMusic";

        assertThrows(UserNotLoggedException.class ,() ->
                this.streamingPlatform.deletePlaylist(playlistTitle, selectionKey),
            "UserNotLoggedException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testDeletePlaylistPlaylistNotEmptyException() {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String playlistTitle = "CrownMusic";

        assertThrows(PlaylistNotEmptyException.class ,() ->
                this.streamingPlatform.deletePlaylist(playlistTitle, selectionKey),
            "PlaylistNotEmptyException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testDeletePlaylistNoSuchPlaylistException() {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String playlistTitle = "FilmMusic";

        assertThrows(NoSuchPlaylistException.class ,() ->
                this.streamingPlatform.deletePlaylist(playlistTitle, selectionKey),
            "NoSuchPlaylistException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testShowPlaylistSuccessfully() throws UserNotLoggedException, NoSuchPlaylistException {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String playlistTitle = "CrownMusic";
        Playlist returned = this.streamingPlatform.showPlaylist(playlistTitle, selectionKey);

        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);

        Playlist expected = new Playlist("sdvelev@gmail.com", "CrownMusic");

        Song firstSong = new Song("The Crown - Main title", "Hans Zimmer", 87, "classical");
        Song secondSong = new Song("The Crown - Bittersweet Symphony", "Richard Ashcroft", 248,
            "modern");

        expected.addSong(firstSong);
        expected.addSong(secondSong);

        assertEquals(expected.getEmailCreator(), returned.getEmailCreator(),
            "The email of the returned playlist is not the same as the expected.");
        assertEquals(expected.getTitle(), returned.getTitle(),
            "The title of the returned playlist is not the same as the expected.");
        assertIterableEquals(expected.getPlaylistSongs(), returned.getPlaylistSongs(),
            "The songs of the returned playlist are not the same as the expected.");
    }

    @Test
    void testShowPlaylistNotLoggedException() {

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(false);

        String playlistTitle = "CrownMusic";

        assertThrows(UserNotLoggedException.class ,() ->
                this.streamingPlatform.showPlaylist(playlistTitle, selectionKey),
            "UserNotLoggedException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testShowPlaylistNoSuchPlaylistException() {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String playlistTitle = "FilmMusic";

        assertThrows(NoSuchPlaylistException.class ,() ->
                this.streamingPlatform.showPlaylist(playlistTitle, selectionKey),
            "NoSuchPlaylistException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testShowPlaylistsSuccessfully() throws UserNotLoggedException {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);


        List<String> returned = this.streamingPlatform.showPlaylists(selectionKey);

        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);


        assertIterableEquals(List.of("MyPlaylist", "CrownMusic"), returned,
            "The list of returned playlist titles is not the same as the expected.");
    }

    @Test
    void testShowPlaylistsNotLoggedException() {

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(false);

        assertThrows(UserNotLoggedException.class ,() ->
                this.streamingPlatform.showPlaylists(selectionKey),
            "UserNotLoggedException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testAddSongToPlaylistSuccessfully()
        throws UserNotLoggedException, NoSuchSongException, IODatabaseException, NoSuchPlaylistException,
        SongAlreadyInPlaylistException {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String playlistTitle = "MyPlaylist";
        String songTitleToAdd = "Vivaldi Variation";
        this.streamingPlatform.addSongToPlaylist(playlistTitle, songTitleToAdd, selectionKey);

        Playlist expected = new Playlist("sdvelev@gmail.com", "MyPlaylist");

        Song firstSong = new Song("The Crown - Main title", "Hans Zimmer", 87, "classical");
        Song secondSong = new Song("No Time To Die", "Billie Eilish", 239,
            "pop");
        Song thirdSong = new Song("Vivaldi Variation", "Florian Christl", 114,
            "classical");

        expected.addSong(firstSong);
        expected.addSong(secondSong);
        expected.addSong(thirdSong);

        Playlist returned = this.streamingPlatform.showPlaylist(playlistTitle, selectionKey);

        verify(this.alreadyLoggedMock, times(2)).contains(this.selectionKey);

        assertIterableEquals(expected.getPlaylistSongs(), returned.getPlaylistSongs(),
            "The song has to be added to the playlist but it was not found to be there.");
    }

    @Test
    void testAddSongToPlaylistSuccessfullyCaseInSensitiveSongTitles()
        throws UserNotLoggedException, NoSuchSongException, IODatabaseException, NoSuchPlaylistException,
        SongAlreadyInPlaylistException {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String playlistTitle = "MyPlaylist";
        String songTitleToAdd = "ViVaLdI vArIaTiOn";
        this.streamingPlatform.addSongToPlaylist(playlistTitle, songTitleToAdd, selectionKey);

        Playlist expected = new Playlist("sdvelev@gmail.com", "MyPlaylist");

        Song firstSong = new Song("The Crown - Main title", "Hans Zimmer", 87, "classical");
        Song secondSong = new Song("No Time To Die", "Billie Eilish", 239,
            "pop");
        Song thirdSong = new Song("Vivaldi Variation", "Florian Christl", 114,
            "classical");

        expected.addSong(firstSong);
        expected.addSong(secondSong);
        expected.addSong(thirdSong);

        Playlist returned = this.streamingPlatform.showPlaylist(playlistTitle, selectionKey);

        verify(this.alreadyLoggedMock, times(2)).contains(this.selectionKey);

        assertIterableEquals(expected.getPlaylistSongs(), returned.getPlaylistSongs(),
            "The song has to be added to the playlist but it was not found to be there.");
    }

    @Test
    void testAddSongToNotLoggedException() {

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(false);

        String playListTitle = "MyPlaylist";
        String songTitleToAdd = "Vivaldi Variation";

        assertThrows(UserNotLoggedException.class ,() ->
                this.streamingPlatform.addSongToPlaylist(playListTitle, songTitleToAdd, selectionKey),
            "UserNotLoggedException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testAddSongToNoSuchSongException() {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String playListTitle = "MyPlaylist";
        String songTitleToAdd = "Vivaldi Variation Classic";

        assertThrows(NoSuchSongException.class ,() ->
                this.streamingPlatform.addSongToPlaylist(playListTitle, songTitleToAdd, selectionKey),
            "NoSuchSongException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testAddSongToNoSuchPlaylistExceptionCaseSensitivePlaylistTitles() {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String playListTitle = "myPlaylist";
        String songTitleToAdd = "Vivaldi Variation";

        assertThrows(NoSuchPlaylistException.class ,() ->
                this.streamingPlatform.addSongToPlaylist(playListTitle, songTitleToAdd, selectionKey),
            "NoSuchPlaylistException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testAddSongToSongAlreadyInPlaylistException() {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String playListTitle = "CrownMusic";
        String songTitleToAdd = "The Crown - Bittersweet Symphony";

        assertThrows(SongAlreadyInPlaylistException.class ,() ->
                this.streamingPlatform.addSongToPlaylist(playListTitle, songTitleToAdd, selectionKey),
            "SongAlreadyInPlaylistException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testRemoveSongFromPlaylistSuccessfully()
        throws UserNotLoggedException, NoSuchSongException, IODatabaseException, NoSuchPlaylistException {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String playlistTitle = "MyPlaylist";
        String songTitleToRemove = "The Crown - Main title";
        this.streamingPlatform.removeSongFromPlaylist(playlistTitle, songTitleToRemove, selectionKey);

        Playlist expected = new Playlist("sdvelev@gmail.com", "MyPlaylist");

        Song firstSong = new Song("No Time To Die", "Billie Eilish", 239,
            "pop");

        expected.addSong(firstSong);

        Playlist returned = this.streamingPlatform.showPlaylist(playlistTitle, selectionKey);

        verify(this.alreadyLoggedMock, times(2)).contains(this.selectionKey);

        assertIterableEquals(expected.getPlaylistSongs(), returned.getPlaylistSongs(),
            "The song has to be removed from the playlist but it was found to be still there.");
    }

    @Test
    void testRemoveSongFromPlaylistSuccessfullyCaseInsensitiveSongTitles()
        throws UserNotLoggedException, NoSuchSongException, IODatabaseException, NoSuchPlaylistException {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String playlistTitle = "MyPlaylist";
        String songTitleToRemove = "The CrOwN - Main TitlE";
        this.streamingPlatform.removeSongFromPlaylist(playlistTitle, songTitleToRemove, selectionKey);

        Playlist expected = new Playlist("sdvelev@gmail.com", "MyPlaylist");

        Song firstSong = new Song("No Time To Die", "Billie Eilish", 239,
            "pop");

        expected.addSong(firstSong);

        Playlist returned = this.streamingPlatform.showPlaylist(playlistTitle, selectionKey);

        verify(this.alreadyLoggedMock, times(2)).contains(this.selectionKey);

        assertIterableEquals(expected.getPlaylistSongs(), returned.getPlaylistSongs(),
            "The song has to be removed from the playlist but it was found to be still there.");
    }

    @Test
    void testRemoveSongFromNotLoggedException() {

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(false);

        String playListTitle = "MyPlaylist";
        String songTitleToAdd = "The Crown - Main title";

        assertThrows(UserNotLoggedException.class ,() ->
                this.streamingPlatform.removeSongFromPlaylist(playListTitle, songTitleToAdd, selectionKey),
            "UserNotLoggedException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testRemoveSongFromNoSuchSongException() {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String playListTitle = "MyPlaylist";
        String songTitleToAdd = "The Crown - Bittersweet Symphony";

        assertThrows(NoSuchSongException.class ,() ->
                this.streamingPlatform.removeSongFromPlaylist(playListTitle, songTitleToAdd, selectionKey),
            "NoSuchSongException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testRemoveSongFromNoSuchPlaylistException() {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String playListTitle = "Crown Music";
        String songTitleToAdd = "The Crown - Bittersweet Symphony";

        assertThrows(NoSuchPlaylistException.class ,() ->
                this.streamingPlatform.removeSongFromPlaylist(playListTitle, songTitleToAdd, selectionKey),
            "NoSuchPlaylistException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testGetTopNMostListenedSongsSuccessfully() {

        int nMostListenedNumber = 2;
        List<SongEntity> actual = this.streamingPlatform.getTopNMostListenedSongs(nMostListenedNumber);

        SongEntity firstPlaceSong = new SongEntity(new Song("Vivaldi Variation", "Florian Christl",
            114, "classical"), 30);
        SongEntity secondPlaceSong = new SongEntity(new Song("No Time To Die", "Billie Eilish",
            239, "pop"), 22);

        assertIterableEquals(List.of(firstPlaceSong, secondPlaceSong), actual,
            "The actual result after getting the two most listened songs is not the same as the expected.");
    }

    @Test
    void testGetTopNMostListenedSongsWithNEqualToZero() {

        int nMostListenedNumber = 0;
        List<SongEntity> actual = this.streamingPlatform.getTopNMostListenedSongs(nMostListenedNumber);

        assertTrue(actual.isEmpty(),
            "Empty list is expected but it is not.");
    }

    @Test
    void testGetTopNMostListenedSongsWithNNegative() {

        int nMostListenedNumber = -6;
        assertThrows(IllegalArgumentException.class, () -> this.streamingPlatform
                .getTopNMostListenedSongs(nMostListenedNumber),
            "IllegalArgumentException is expected but not thrown.");
    }


    @Test
    void testSearchSongsSuccessfullyOneWord() {

        String searchedWord = "crown";
        List<SongEntity> actual = this.streamingPlatform.searchSongs(searchedWord);

        SongEntity firstPlaceSong = new SongEntity(new Song("The Crown - Main title", "Hans Zimmer",
            87, "classical"), 11);
        SongEntity secondPlaceSong = new SongEntity(new Song("The Crown - Bittersweet Symphony",
            "Richard Ashcroft", 248, "modern"), 9);

        assertIterableEquals(List.of(firstPlaceSong, secondPlaceSong), actual,
            "The actual result after searching with one word is not the same as the expected.");
    }

    @Test
    void testSearchSongsSuccessfullyTwoWords() {

        String searchedWord = "crown zimmer";
        List<SongEntity> actual = this.streamingPlatform.searchSongs(searchedWord);

        SongEntity firstPlaceSong = new SongEntity(new Song("The Crown - Main title", "Hans Zimmer",
            87, "classical"), 11);

        assertIterableEquals(List.of(firstPlaceSong), actual,
            "The actual result after searching with two words is not the same as the expected.");
    }

    private int getListeningTimesBySongTitle(String songTitle) {

        for (SongEntity currentSongEntity : this.streamingPlatform.getSongs()) {

            if (currentSongEntity.getSong().getTitle().equalsIgnoreCase(songTitle)) {

                return currentSongEntity.getListeningTimes();
            }
        }

        return 0;
    }

    @Test
    void testPlaySongSuccessfully()
        throws UserNotLoggedException, NoSuchSongException, IODatabaseException, SongIsAlreadyPlayingException {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String songTitle = "The Crown - Bittersweet Symphony";

        int listeningTimesBefore = getListeningTimesBySongTitle(songTitle);

        this.streamingPlatform.playSong(songTitle, this.selectionKey);
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);

        int listeningTimesAfter = getListeningTimesBySongTitle(songTitle);

        //when(this.alreadyRunningMock.isEmpty()).thenReturn(false);

        assertFalse(this.streamingPlatform.getAlreadyRunning().isEmpty(),
            "Song is expected to run but it isn't playing.");
        assertTrue(listeningTimesAfter == listeningTimesBefore + 1,
            "The number of listening times is expected to increase with one when playing song but it isn't.");
    }

    @Test
    void testPlaySongUserNotLoggedException() {

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(false);

        String songTitle = "The Crown - Bittersweet Symphony";

        assertThrows(UserNotLoggedException.class ,() ->
                this.streamingPlatform.playSong(songTitle, selectionKey),
            "UserNotLoggedException is expected but not thrown.");

        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testPlaySongSongIsAlreadyPlayingException() {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String songTitle = "The Crown - Bittersweet Symphony";

        when(this.alreadyRunningMock.containsKey(this.selectionKey)).thenReturn(true);

        assertThrows(SongIsAlreadyPlayingException.class ,() ->
                this.streamingPlatform.playSong(songTitle, selectionKey),
            "SongIsAlreadyPlayingException is expected but not thrown.");

        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
        verify(this.alreadyRunningMock, times(1)).containsKey(this.selectionKey);
    }

    @Test
    void testPlaySongUserNoSuchSongException() {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String songTitle = "Bittersweet Symphony Classic";

        assertThrows(NoSuchSongException.class ,() ->
                this.streamingPlatform.playSong(songTitle, selectionKey),
            "NoSuchSongException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Mock
    private PlaySong playSongMock = mock(PlaySong.class);

    @Test
    void testStopSongSuccessfully() throws InterruptedException, UserNotLoggedException, NoSongPlayingException {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        when(this.alreadyRunningMock.containsKey(this.selectionKey)).thenReturn(true);

        when(this.alreadyRunningMock.get(selectionKey)).thenReturn(playSongMock);
        doNothing().when(playSongMock).terminateSong();
        doNothing().when(playSongMock).join();

        this.streamingPlatform.stopSong(selectionKey);

        verify(this.playSongMock, times(1)).terminateSong();
        verify(this.playSongMock, times(1)).join();
    }

    @Test
    void testStopSongNoSongPlayingException() {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        when(this.alreadyRunningMock.containsKey(this.selectionKey)).thenReturn(false);

        assertThrows(NoSongPlayingException.class, () -> this.streamingPlatform.stopSong(selectionKey),
            "NoSongPlayingException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
        verify(this.alreadyRunningMock, times(1)).containsKey(this.selectionKey);
    }

    @Test
    void testStopSongUserNotLoggedException() {

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(false);

        assertThrows(UserNotLoggedException.class, () -> this.streamingPlatform.stopSong(selectionKey),
            "UserNotLoggedException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testLogoutSuccessfully() throws InterruptedException, UserNotLoggedException, NoSongPlayingException {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        when(this.alreadyRunningMock.containsKey(this.selectionKey)).thenReturn(true);

        when(this.alreadyRunningMock.get(selectionKey)).thenReturn(playSongMock);
        doNothing().when(playSongMock).terminateSong();
        doNothing().when(playSongMock).join();

        this.streamingPlatform.logout(this.selectionKey);

        verify(this.playSongMock, times(1)).terminateSong();
        verify(this.playSongMock, times(1)).join();

        verify(this.alreadyLoggedMock, times(1)).remove(this.selectionKey);
    }

    @Test
    void testLogoutUserNotLoggedException() {

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(false);

        assertThrows(UserNotLoggedException.class, () -> this.streamingPlatform.logout(selectionKey),
            "UserNotLoggedException is expected but not thrown.");
        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testPlayPlaylistSuccessfully()
        throws UserNotLoggedException, SongIsAlreadyPlayingException, InterruptedException, NoSongPlayingException {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        when(this.alreadyRunningMock.containsKey(this.selectionKey)).thenReturn(false);

        String playlistTitle = "CrownMusic";

        String firstSongInPlaylistTitle = "The Crown - Main title";
        String secondsSongInPlaylistTitle = "The Crown - Bittersweet Symphony";

        int firstSongListeningTimesBefore = getListeningTimesBySongTitle(firstSongInPlaylistTitle);
        int secondSongListeningTimesBefore = getListeningTimesBySongTitle(secondsSongInPlaylistTitle);

        this.streamingPlatform.playPlaylist(playlistTitle, this.selectionKey);

        Thread.sleep(500);

        when(this.alreadyRunningMock.containsKey(this.selectionKey)).thenReturn(true);

        when(this.alreadyRunningMock.get(selectionKey)).thenReturn(playSongMock);
        doNothing().when(playSongMock).terminateSong();
        doNothing().when(playSongMock).join();

        this.streamingPlatform.stopSong(selectionKey);

        Thread.sleep(500);

        when(this.alreadyRunningMock.containsKey(this.selectionKey)).thenReturn(true);

        when(this.alreadyRunningMock.get(selectionKey)).thenReturn(playSongMock);
        doNothing().when(playSongMock).terminateSong();
        doNothing().when(playSongMock).join();

        this.streamingPlatform.stopSong(selectionKey);

        int firstSongListeningTimesAfter = getListeningTimesBySongTitle(firstSongInPlaylistTitle);
        int secondSongListeningTimesAfter = getListeningTimesBySongTitle(secondsSongInPlaylistTitle);


        assertTrue(firstSongListeningTimesAfter == firstSongListeningTimesBefore + 1 &&
                secondSongListeningTimesAfter == secondSongListeningTimesBefore + 1,
            "Number of listening times must increase when playing songs of playlist.");
    }

    @Test
    void testPlayPlaylistNotLoggedException() {

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(false);

        String playlistTitle = "MyPlaylist";
        assertThrows(UserNotLoggedException.class ,() ->
                this.streamingPlatform.playPlaylist(playlistTitle, selectionKey),
            "UserNotLoggedException is expected but not thrown.");

        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
    }

    @Test
    void testPlayPlaylistSongIsAlreadyPlayingException() {

        User user = new User("sdvelev@gmail.com", "123456");
        this.streamingPlatform.setUser(user);

        when(this.alreadyLoggedMock.contains(this.selectionKey)).thenReturn(true);

        String playListTitle = "MyPlaylist";

        when(this.alreadyRunningMock.containsKey(this.selectionKey)).thenReturn(true);

        assertThrows(SongIsAlreadyPlayingException.class ,() ->
                this.streamingPlatform.playPlaylist(playListTitle, selectionKey),
            "SongIsAlreadyPlayingException is expected but not thrown.");

        verify(this.alreadyLoggedMock, times(1)).contains(this.selectionKey);
        verify(this.alreadyRunningMock, times(1)).containsKey(this.selectionKey);
    }

}
