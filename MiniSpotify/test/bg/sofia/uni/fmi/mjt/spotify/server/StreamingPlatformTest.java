package bg.sofia.uni.fmi.mjt.spotify.server;

import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.IODatabaseException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.NoSongPlayingException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.NoSongsInPlaylistException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.NoSuchPlaylistException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.NoSuchSongException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.PlaylistAlreadyExistException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.PlaylistNotEmptyException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.SongAlreadyInPlaylistException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.SongIsAlreadyPlayingException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.SpotifyException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.UserNotLoggedException;
import bg.sofia.uni.fmi.mjt.spotify.server.login.User;
import bg.sofia.uni.fmi.mjt.spotify.server.player.PlaySongThread;
import bg.sofia.uni.fmi.mjt.spotify.server.storage.Playlist;
import bg.sofia.uni.fmi.mjt.spotify.server.storage.Song;
import bg.sofia.uni.fmi.mjt.spotify.server.storage.SongEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
    private Set<SelectionKey> alreadyLoggedMock;

    @Mock
    private Map<SelectionKey, PlaySongThread> alreadyRunningMock;

    @Mock
    private PlaySongThread playSongThreadMock;

    @BeforeEach
    void setTests() throws IODatabaseException {
        var songsListIn = new StringReader(SONGS_LIST);
        var playlistsListIn = new StringReader(PLAYLISTS_LIST);
        var songsListOut = new StringWriter();
        var playlistsListOut = new StringWriter();

        streamingPlatform = new StreamingPlatform(playlistsListIn, playlistsListOut, songsListIn, songsListOut,
            alreadyLoggedMock, alreadyRunningMock);
    }

    @AfterEach
    void setTestsCleaning() throws IODatabaseException {
        try {
            streamingPlatform.getPlaylistsReader().close();
            streamingPlatform.getPlaylistsWriter().close();
            streamingPlatform.getSongsReader().close();
            streamingPlatform.getSongsWriter().close();
        } catch(IOException e) {

            throw new IODatabaseException("There is an exception in closing streams", e);
        }
    }

    @Test
    void testCreatePlaylistSuccessfully() throws SpotifyException {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playlistTitle = "TestPlaylist";
        streamingPlatform.createPlaylist(playlistTitle, selectionKey);

        verify(alreadyLoggedMock, times(1)).contains(selectionKey);

        String actual = streamingPlatform.getPlaylistsWriter().toString();
        assertEquals(EXPECTED_TEST_CREATE_PLAYLIST_SUCCESSFULLY, actual,
            "The actual result after creating playlist is not the same as the expected.");
    }

    @Test
    void testCreatePlaylistSuccessfullyNoPlaylistsYet() throws SpotifyException {
        User user = new User("sdvelev@outlook.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playlistTitle = "TestPlaylist";
        streamingPlatform.createPlaylist(playlistTitle, selectionKey);

        verify(alreadyLoggedMock, times(1)).contains(selectionKey);

        String actual = streamingPlatform.getPlaylistsWriter().toString();
        assertEquals(EXPECTED_TEST_CREATE_PLAYLIST_SUCCESSFULLY_NOT_LOGGED, actual,
            "The actual result after creating playlist is not the same as the expected.");
    }

    @Test
    void testCreatePlaylistNotLoggedException() {
        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(false);

        String playlistTitle = "TestPlaylist";

        assertThrows(UserNotLoggedException.class ,() ->
                streamingPlatform.createPlaylist(playlistTitle, selectionKey),
            "UserNotLoggedException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testCreatePlaylistPlaylistAlreadyExistException() {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playlistTitle = "CrownMusic";

        assertThrows(PlaylistAlreadyExistException.class, () ->
                streamingPlatform.createPlaylist(playlistTitle, selectionKey),
            "PlaylistAlreadyExistException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testDeletePlaylistSuccessfully() throws SpotifyException {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playlistTitle = "TestPlaylist";
        streamingPlatform. createPlaylist(playlistTitle, selectionKey);

        String actual = streamingPlatform.getPlaylistsWriter().toString();
        assertEquals(EXPECTED_TEST_CREATE_PLAYLIST_SUCCESSFULLY, actual,
            "The actual result after creating playlist is not the same as the expected.");

        var newWriter = new StringWriter();
        streamingPlatform.setPlaylistsWriter(newWriter);

        streamingPlatform.deletePlaylist(playlistTitle, selectionKey);

        actual = streamingPlatform.getPlaylistsWriter().toString();

        try {
            newWriter.close();
        } catch (IOException e) {

            throw new IODatabaseException("There is a problem in closing streams.", e);
        }

        assertEquals(PLAYLISTS_LIST, actual,
            "The actual result after deleting playlist is not the same as the expected.");

        verify(alreadyLoggedMock, times(2)).contains(selectionKey);
    }

    @Test
    void testDeletePlaylistNotLoggedException() {
        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(false);

        String playlistTitle = "CrownMusic";

        assertThrows(UserNotLoggedException.class ,() ->
                streamingPlatform.deletePlaylist(playlistTitle, selectionKey),
            "UserNotLoggedException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testDeletePlaylistPlaylistNotEmptyException() {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playlistTitle = "CrownMusic";

        assertThrows(PlaylistNotEmptyException.class ,() ->
                streamingPlatform.deletePlaylist(playlistTitle, selectionKey),
            "PlaylistNotEmptyException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testDeletePlaylistNoSuchPlaylistException() {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playlistTitle = "FilmMusic";

        assertThrows(NoSuchPlaylistException.class ,() ->
                streamingPlatform.deletePlaylist(playlistTitle, selectionKey),
            "NoSuchPlaylistException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testShowPlaylistSuccessfully() throws SpotifyException {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playlistTitle = "CrownMusic";
        Playlist returned = streamingPlatform.showPlaylist(playlistTitle, selectionKey);

        verify(alreadyLoggedMock, times(1)).contains(selectionKey);

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
        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(false);

        String playlistTitle = "CrownMusic";

        assertThrows(UserNotLoggedException.class ,() ->
                streamingPlatform.showPlaylist(playlistTitle, selectionKey),
            "UserNotLoggedException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testShowPlaylistNoSuchPlaylistException() {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playlistTitle = "FilmMusic";

        assertThrows(NoSuchPlaylistException.class ,() ->
                streamingPlatform.showPlaylist(playlistTitle, selectionKey),
            "NoSuchPlaylistException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testShowPlaylistsSuccessfully() throws UserNotLoggedException {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);


        List<String> returned = streamingPlatform.showPlaylists(selectionKey);

        verify(alreadyLoggedMock, times(1)).contains(selectionKey);


        assertIterableEquals(List.of("MyPlaylist", "CrownMusic"), returned,
            "The list of returned playlist titles is not the same as the expected.");
    }

    @Test
    void testShowPlaylistsNotLoggedException() {
        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(false);

        assertThrows(UserNotLoggedException.class ,() ->
                streamingPlatform.showPlaylists(selectionKey),
            "UserNotLoggedException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testAddSongToPlaylistSuccessfully() throws SpotifyException {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playlistTitle = "MyPlaylist";
        String songTitleToAdd = "Vivaldi Variation";
        streamingPlatform.addSongToPlaylist(playlistTitle, songTitleToAdd, selectionKey);

        Playlist expected = new Playlist("sdvelev@gmail.com", "MyPlaylist");

        Song firstSong = new Song("The Crown - Main title", "Hans Zimmer", 87, "classical");
        Song secondSong = new Song("No Time To Die", "Billie Eilish", 239,
            "pop");
        Song thirdSong = new Song("Vivaldi Variation", "Florian Christl", 114,
            "classical");

        expected.addSong(firstSong);
        expected.addSong(secondSong);
        expected.addSong(thirdSong);

        Playlist returned = streamingPlatform.showPlaylist(playlistTitle, selectionKey);

        verify(alreadyLoggedMock, times(2)).contains(selectionKey);

        assertIterableEquals(expected.getPlaylistSongs(), returned.getPlaylistSongs(),
            "The song has to be added to the playlist but it was not found to be there.");
    }

    @Test
    void testAddSongToPlaylistSuccessfullyCaseInSensitiveSongTitles() throws SpotifyException {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playlistTitle = "MyPlaylist";
        String songTitleToAdd = "ViVaLdI vArIaTiOn";
        streamingPlatform.addSongToPlaylist(playlistTitle, songTitleToAdd, selectionKey);

        Playlist expected = new Playlist("sdvelev@gmail.com", "MyPlaylist");

        Song firstSong = new Song("The Crown - Main title", "Hans Zimmer", 87, "classical");
        Song secondSong = new Song("No Time To Die", "Billie Eilish", 239,
            "pop");
        Song thirdSong = new Song("Vivaldi Variation", "Florian Christl", 114,
            "classical");

        expected.addSong(firstSong);
        expected.addSong(secondSong);
        expected.addSong(thirdSong);

        Playlist returned = streamingPlatform.showPlaylist(playlistTitle, selectionKey);

        verify(alreadyLoggedMock, times(2)).contains(selectionKey);

        assertIterableEquals(expected.getPlaylistSongs(), returned.getPlaylistSongs(),
            "The song has to be added to the playlist but it was not found to be there.");
    }

    @Test
    void testAddSongToNotLoggedException() {
        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(false);

        String playListTitle = "MyPlaylist";
        String songTitleToAdd = "Vivaldi Variation";

        assertThrows(UserNotLoggedException.class ,() ->
                streamingPlatform.addSongToPlaylist(playListTitle, songTitleToAdd, selectionKey),
            "UserNotLoggedException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testAddSongToNoSuchSongException() {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playListTitle = "MyPlaylist";
        String songTitleToAdd = "Vivaldi Variation Classic";

        assertThrows(NoSuchSongException.class ,() ->
                streamingPlatform.addSongToPlaylist(playListTitle, songTitleToAdd, selectionKey),
            "NoSuchSongException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testAddSongToNoSuchPlaylistExceptionCaseSensitivePlaylistTitles() {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playListTitle = "myPlaylist";
        String songTitleToAdd = "Vivaldi Variation";

        assertThrows(NoSuchPlaylistException.class ,() ->
                streamingPlatform.addSongToPlaylist(playListTitle, songTitleToAdd, selectionKey),
            "NoSuchPlaylistException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testAddSongToSongAlreadyInPlaylistException() {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playListTitle = "CrownMusic";
        String songTitleToAdd = "The Crown - Bittersweet Symphony";

        assertThrows(SongAlreadyInPlaylistException.class ,() ->
                streamingPlatform.addSongToPlaylist(playListTitle, songTitleToAdd, selectionKey),
            "SongAlreadyInPlaylistException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testRemoveSongFromPlaylistSuccessfully() throws SpotifyException {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playlistTitle = "MyPlaylist";
        String songTitleToRemove = "The Crown - Main title";
        streamingPlatform.removeSongFromPlaylist(playlistTitle, songTitleToRemove, selectionKey);

        Playlist expected = new Playlist("sdvelev@gmail.com", "MyPlaylist");

        Song firstSong = new Song("No Time To Die", "Billie Eilish", 239,
            "pop");

        expected.addSong(firstSong);

        Playlist returned = streamingPlatform.showPlaylist(playlistTitle, selectionKey);

        verify(alreadyLoggedMock, times(2)).contains(selectionKey);

        assertIterableEquals(expected.getPlaylistSongs(), returned.getPlaylistSongs(),
            "The song has to be removed from the playlist but it was found to be still there.");
    }

    @Test
    void testRemoveSongFromPlaylistSuccessfullyCaseInsensitiveSongTitles() throws SpotifyException {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playlistTitle = "MyPlaylist";
        String songTitleToRemove = "The CrOwN - Main TitLE";
        streamingPlatform.removeSongFromPlaylist(playlistTitle, songTitleToRemove, selectionKey);

        Playlist expected = new Playlist("sdvelev@gmail.com", "MyPlaylist");

        Song firstSong = new Song("No Time To Die", "Billie Eilish", 239,
            "pop");

        expected.addSong(firstSong);

        Playlist returned = streamingPlatform.showPlaylist(playlistTitle, selectionKey);

        verify(alreadyLoggedMock, times(2)).contains(selectionKey);

        assertIterableEquals(expected.getPlaylistSongs(), returned.getPlaylistSongs(),
            "The song has to be removed from the playlist but it was found to be still there.");
    }

    @Test
    void testRemoveSongFromNotLoggedException() {
        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(false);

        String playListTitle = "MyPlaylist";
        String songTitleToAdd = "The Crown - Main title";

        assertThrows(UserNotLoggedException.class ,() ->
                streamingPlatform.removeSongFromPlaylist(playListTitle, songTitleToAdd, selectionKey),
            "UserNotLoggedException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testRemoveSongFromNoSuchSongException() {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playListTitle = "MyPlaylist";
        String songTitleToAdd = "The Crown - Bittersweet Symphony";

        assertThrows(NoSuchSongException.class ,() ->
                streamingPlatform.removeSongFromPlaylist(playListTitle, songTitleToAdd, selectionKey),
            "NoSuchSongException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testRemoveSongFromNoSuchPlaylistException() {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playListTitle = "Crown Music";
        String songTitleToAdd = "The Crown - Bittersweet Symphony";

        assertThrows(NoSuchPlaylistException.class ,() ->
                streamingPlatform.removeSongFromPlaylist(playListTitle, songTitleToAdd, selectionKey),
            "NoSuchPlaylistException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testGetTopNMostListenedSongsSuccessfully() {
        int nMostListenedNumber = 2;
        List<SongEntity> actual = streamingPlatform.getTopNMostListenedSongs(nMostListenedNumber);

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
        List<SongEntity> actual = streamingPlatform.getTopNMostListenedSongs(nMostListenedNumber);

        assertTrue(actual.isEmpty(),
            "Empty list is expected but it is not.");
    }

    @Test
    void testGetTopNMostListenedSongsWithNNegative() {
        int nMostListenedNumber = -6;

        assertThrows(IllegalArgumentException.class, () -> streamingPlatform
                .getTopNMostListenedSongs(nMostListenedNumber),
            "IllegalArgumentException is expected but not thrown.");
    }


    @Test
    void testSearchSongsSuccessfullyOneWord() {
        String searchedWord = "crown";
        List<SongEntity> actual = streamingPlatform.searchSongs(searchedWord);

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
        List<SongEntity> actual = streamingPlatform.searchSongs(searchedWord);

        SongEntity firstPlaceSong = new SongEntity(new Song("The Crown - Main title", "Hans Zimmer",
            87, "classical"), 11);

        assertIterableEquals(List.of(firstPlaceSong), actual,
            "The actual result after searching with two words is not the same as the expected.");
    }

    private int getListeningTimesBySongTitle(String songTitle) {
        for (SongEntity currentSongEntity : streamingPlatform.getSongs()) {
            if (currentSongEntity.getSong().getTitle().equalsIgnoreCase(songTitle)) {
                return currentSongEntity.getListeningTimes();
            }
        }

        return 0;
    }

    @Test
    void testPlaySongSuccessfully() throws SpotifyException {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String songTitle = "The Crown - Main title";

        int listeningTimesBefore = getListeningTimesBySongTitle(songTitle);

        streamingPlatform.playSong(songTitle, selectionKey);
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);

        int listeningTimesAfter = getListeningTimesBySongTitle(songTitle);

        assertFalse(streamingPlatform.getAlreadyRunning().isEmpty(),
            "Song is expected to run but it isn't playing.");
        assertEquals(listeningTimesAfter, listeningTimesBefore + 1,
            "The number of listening times is expected to increase with one when playing song but it isn't.");
    }

    @Test
    void testPlaySongUserNotLoggedException() {
        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(false);

        String songTitle = "The Crown - Bittersweet Symphony";

        assertThrows(UserNotLoggedException.class ,() ->
                streamingPlatform.playSong(songTitle, selectionKey),
            "UserNotLoggedException is expected but not thrown.");

        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testPlaySongSongIsAlreadyPlayingException() {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String songTitle = "The Crown - Bittersweet Symphony";

        when(alreadyRunningMock.containsKey(selectionKey)).thenReturn(true);

        assertThrows(SongIsAlreadyPlayingException.class ,() ->
                streamingPlatform.playSong(songTitle, selectionKey),
            "SongIsAlreadyPlayingException is expected but not thrown.");

        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
        verify(alreadyRunningMock, times(1)).containsKey(selectionKey);
    }

    @Test
    void testPlaySongUserNoSuchSongException() {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String songTitle = "Bittersweet Symphony Classic";

        assertThrows(NoSuchSongException.class ,() ->
                streamingPlatform.playSong(songTitle, selectionKey),
            "NoSuchSongException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testStopSongSuccessfully() throws SpotifyException, InterruptedException {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        when(alreadyRunningMock.containsKey(selectionKey)).thenReturn(true);

        when(alreadyRunningMock.get(selectionKey)).thenReturn(playSongThreadMock);
        doNothing().when(playSongThreadMock).terminateSong();
        doNothing().when(playSongThreadMock).join();

        streamingPlatform.stopSong(selectionKey);

        verify(playSongThreadMock, times(1)).terminateSong();
        verify(playSongThreadMock, times(1)).join();
    }

    @Test
    void testStopSongNoSongPlayingException() {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        when(alreadyRunningMock.containsKey(selectionKey)).thenReturn(false);

        assertThrows(NoSongPlayingException.class, () -> streamingPlatform.stopSong(selectionKey),
            "NoSongPlayingException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
        verify(alreadyRunningMock, times(1)).containsKey(selectionKey);
    }

    @Test
    void testStopSongUserNotLoggedException() {
        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(false);

        assertThrows(UserNotLoggedException.class, () -> streamingPlatform.stopSong(selectionKey),
            "UserNotLoggedException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testLogoutSuccessfully() throws SpotifyException, InterruptedException {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        when(alreadyRunningMock.containsKey(selectionKey)).thenReturn(true);

        when(alreadyRunningMock.get(selectionKey)).thenReturn(playSongThreadMock);
        doNothing().when(playSongThreadMock).terminateSong();
        doNothing().when(playSongThreadMock).join();

        streamingPlatform.logout(selectionKey);

        verify(playSongThreadMock, times(1)).terminateSong();
        verify(playSongThreadMock, times(1)).join();

        verify(alreadyLoggedMock, times(1)).remove(selectionKey);
    }

    @Test
    void testLogoutUserNotLoggedException() {
        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(false);

        assertThrows(UserNotLoggedException.class, () -> streamingPlatform.logout(selectionKey),
            "UserNotLoggedException is expected but not thrown.");
        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testPlayPlaylistSuccessfully() throws SpotifyException, InterruptedException {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        when(alreadyRunningMock.containsKey(selectionKey)).thenReturn(false);

        String playlistTitle = "CrownMusic";

        String firstSongInPlaylistTitle = "The Crown - Main title";
        String secondsSongInPlaylistTitle = "The Crown - Bittersweet Symphony";

        int firstSongListeningTimesBefore = getListeningTimesBySongTitle(firstSongInPlaylistTitle);
        int secondSongListeningTimesBefore = getListeningTimesBySongTitle(secondsSongInPlaylistTitle);

        streamingPlatform.playPlaylist(playlistTitle, selectionKey);

        Thread.sleep(500);

        when(alreadyRunningMock.containsKey(selectionKey)).thenReturn(true);

        when(alreadyRunningMock.get(selectionKey)).thenReturn(playSongThreadMock);
        doNothing().when(playSongThreadMock).terminateSong();
        doNothing().when(playSongThreadMock).join();

        streamingPlatform.stopSong(selectionKey);

        Thread.sleep(500);

        when(alreadyRunningMock.containsKey(selectionKey)).thenReturn(true);

        when(alreadyRunningMock.get(selectionKey)).thenReturn(playSongThreadMock);
        doNothing().when(playSongThreadMock).terminateSong();
        doNothing().when(playSongThreadMock).join();

        streamingPlatform.stopSong(selectionKey);

        int firstSongListeningTimesAfter = getListeningTimesBySongTitle(firstSongInPlaylistTitle);
        int secondSongListeningTimesAfter = getListeningTimesBySongTitle(secondsSongInPlaylistTitle);


        assertTrue(firstSongListeningTimesAfter == firstSongListeningTimesBefore + 1 &&
                secondSongListeningTimesAfter == secondSongListeningTimesBefore + 1,
            "Number of listening times must increase when playing songs of playlist.");
    }

    @Test
    void testPlayPlaylistNotLoggedException() {
        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(false);

        String playlistTitle = "MyPlaylist";
        assertThrows(UserNotLoggedException.class ,() ->
                streamingPlatform.playPlaylist(playlistTitle, selectionKey),
            "UserNotLoggedException is expected but not thrown.");

        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testPlayPlaylistSongIsAlreadyPlayingException() {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playListTitle = "MyPlaylist";

        when(alreadyRunningMock.containsKey(selectionKey)).thenReturn(true);

        assertThrows(SongIsAlreadyPlayingException.class ,() ->
                streamingPlatform.playPlaylist(playListTitle, selectionKey),
            "SongIsAlreadyPlayingException is expected but not thrown.");

        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
        verify(alreadyRunningMock, times(1)).containsKey(selectionKey);
    }

    @Test
    void testPlayPlaylistNoSuchPlaylistException() {
        User user = new User("sm@sm.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        String playListTitle = "MyPlaylist";

        assertThrows(NoSuchPlaylistException.class ,() ->
                streamingPlatform.playPlaylist(playListTitle, selectionKey),
            "NoSuchPlaylistException is expected but not thrown.");

        verify(alreadyLoggedMock, times(1)).contains(selectionKey);
    }

    @Test
    void testPlayPlaylistNoSongsInPlaylistException() throws SpotifyException {
        User user = new User("sdvelev@gmail.com", "123456");
        streamingPlatform.setUser(user);

        when(alreadyLoggedMock.contains(selectionKey)).thenReturn(true);

        when(alreadyRunningMock.containsKey(selectionKey)).thenReturn(false);

        String playListTitle = "EmptyPlaylist";

        streamingPlatform.createPlaylist(playListTitle, selectionKey);

        assertThrows(NoSongsInPlaylistException.class ,() ->
                streamingPlatform.playPlaylist(playListTitle, selectionKey),
            "NoSongsInPlaylistException is expected but not thrown.");

        verify(alreadyLoggedMock, times(2)).contains(selectionKey);
        verify(alreadyRunningMock, times(1)).containsKey(selectionKey);
    }
}
