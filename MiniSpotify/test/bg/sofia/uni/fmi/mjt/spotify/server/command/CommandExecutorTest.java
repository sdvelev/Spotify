package bg.sofia.uni.fmi.mjt.spotify.server.command;

import bg.sofia.uni.fmi.mjt.spotify.server.ServerReply;
import bg.sofia.uni.fmi.mjt.spotify.server.StreamingPlatform;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.EmailAlreadyRegisteredException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.IODatabaseException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.NoSongPlayingException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.NoSongsInPlaylistException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.NoSuchPlaylistException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.NoSuchSongException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.NotValidEmailFormatException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.PlaylistAlreadyExistException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.PlaylistNotEmptyException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.SongAlreadyInPlaylistException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.SongIsAlreadyPlayingException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.SpotifyException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.UserNotLoggedException;
import bg.sofia.uni.fmi.mjt.spotify.server.logger.SpotifyLogger;
import bg.sofia.uni.fmi.mjt.spotify.server.login.AuthenticationService;
import bg.sofia.uni.fmi.mjt.spotify.server.login.User;
import bg.sofia.uni.fmi.mjt.spotify.server.storage.Playlist;
import bg.sofia.uni.fmi.mjt.spotify.server.storage.Song;
import bg.sofia.uni.fmi.mjt.spotify.server.storage.SongEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.channels.SelectionKey;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommandExecutorTest {

    @Mock
    private SelectionKey selectionKeyMock;

    @Mock
    private StreamingPlatform streamingPlatformMock;

    @Mock
    private AuthenticationService authenticationServiceMock;

    private CommandExecutor commandExecutor;

    @Mock
    private SpotifyLogger spotifyLoggerMock;

    @BeforeEach
    void setTests() {
        commandExecutor = new CommandExecutor(streamingPlatformMock, authenticationServiceMock, spotifyLoggerMock);
    }

    @Test
    void testExecuteCommandProcessLogoutSuccessfully() throws SpotifyException, InterruptedException {
        Command toProcess = new Command("logout", new ArrayList<>());

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.LOGOUT_COMMAND_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the logout command successfully is not " +
                "the same as the expected.");
        verify(streamingPlatformMock, times(1)).logout(selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessLogoutUserNotLoggedException() throws SpotifyException, InterruptedException {
        Command toProcess = new Command("logout", new ArrayList<>());

        doThrow(new UserNotLoggedException((ServerReply.LOGOUT_COMMAND_USER_NOT_LOGGED_REPLY.getReply())))
            .when(streamingPlatformMock).logout(selectionKeyMock);

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.LOGOUT_COMMAND_USER_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the logout command when user is not logged " +
                "is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).logout(selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessLogoutInterruptedException() throws SpotifyException, InterruptedException {
        Command toProcess = new Command("logout", new ArrayList<>());

        doThrow(new InterruptedException((ServerReply.SERVER_EXCEPTION.getReply())))
            .when(streamingPlatformMock).logout(selectionKeyMock);

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing the logout command when InterruptedException " +
                "is thrown is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).logout(selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessDisconnectSuccessfullyWithLogout() throws SpotifyException, InterruptedException {
        Command toProcess = new Command("disconnect", new ArrayList<>());

        when(streamingPlatformMock.getAlreadyLogged()).thenReturn(Set.of(selectionKeyMock));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.DISCONNECT_COMMAND_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the disconnect command successfully with " +
                "logout is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).logout(selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getAlreadyLogged();
    }

    @Test
    void testExecuteCommandProcessDisconnectSuccessfullyWithoutLogout() {
        Command toProcess = new Command("disconnect", new ArrayList<>());

        when(streamingPlatformMock.getAlreadyLogged()).thenReturn(new HashSet<>());

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.DISCONNECT_COMMAND_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the disconnect command successfully without " +
                "logout is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).getAlreadyLogged();
    }

    @Test
    void testExecuteCommandProcessDisconnectInterruptedException() throws SpotifyException, InterruptedException {
        Command toProcess = new Command("disconnect", new ArrayList<>());

        when(streamingPlatformMock.getAlreadyLogged()).thenReturn(Set.of(selectionKeyMock));

        doThrow(new InterruptedException((ServerReply.SERVER_EXCEPTION.getReply())))
            .when(streamingPlatformMock).logout(selectionKeyMock);

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.DISCONNECT_COMMAND_ERROR_REPLY.getReply(), result,
            "The received reply from the server after executing the disconnect command when InterruptedException " +
                "is thrown is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).logout(selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessSearchCommandSuccessfully() {
        Command toProcess = new Command("search", List.of("the crown"));

        SongEntity firstSearchedSong = new SongEntity(new Song("The Crown - Main title",
            "Hans Zimmer", 87, "classical"), 16);
        SongEntity secondSearchedSong = new SongEntity(new Song("The Crown - Bittersweet Symphony",
            "Richard Ashcroft", 248, "modern"), 12);

        List<SongEntity> toReturnList = new ArrayList<>();
        toReturnList.add(firstSearchedSong);
        toReturnList.add(secondSearchedSong);

        when(streamingPlatformMock.searchSongs("the crown")).thenReturn(toReturnList);

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        String expectedString = ServerReply.SEARCH_COMMAND_SUCCESSFULLY_REPLY.getReply() + System.lineSeparator() +
            " Title: The Crown - Main title Artist: Hans Zimmer Genre: classical Duration (in seconds): 87" +
            System.lineSeparator() + " Title: The Crown - Bittersweet Symphony Artist: Richard Ashcroft " +
            "Genre: modern Duration (in seconds): 248" + System.lineSeparator();

        assertEquals(expectedString, result,
            "The received reply from the server after executing the search command successfully with " +
                "two found songs is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).searchSongs("the crown");
    }

    @Test
    void testExecuteCommandProcessSearchCommandNoSongs() {
        Command toProcess = new Command("search", List.of("The Crown - Voices"));

        when(streamingPlatformMock.searchSongs("The Crown - Voices")).thenReturn(new ArrayList<>());

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.SEARCH_COMMAND_NO_SONGS_REPLY.getReply(), result,
            "The received reply from the server after executing the search command successfully with " +
                "no found songs is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).searchSongs("The Crown - Voices");
    }

    @Test
    void testExecuteCommandProcessTopCommandSuccessfully() {
        Command toProcess = new Command("top", List.of("2"));

        SongEntity firstSearchedSong = new SongEntity(new Song("The Crown - Main title",
            "Hans Zimmer", 87, "classical"), 16);
        SongEntity secondSearchedSong = new SongEntity(new Song("The Crown - Bittersweet Symphony",
            "Richard Ashcroft", 248, "modern"), 12);

        List<SongEntity> toReturnList = new ArrayList<>();
        toReturnList.add(firstSearchedSong);
        toReturnList.add(secondSearchedSong);

        when(streamingPlatformMock.getTopNMostListenedSongs(2)).thenReturn(toReturnList);

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        String expectedString = ServerReply.TOP_COMMAND_SUCCESSFULLY_REPLY.getReply() + System.lineSeparator() +
            "# 16 Title: The Crown - Main title Artist: Hans Zimmer Genre: classical Duration (in seconds): 87" +
            System.lineSeparator() + "# 12 Title: The Crown - Bittersweet Symphony Artist: Richard Ashcroft " +
            "Genre: modern Duration (in seconds): 248" + System.lineSeparator();

        assertEquals(expectedString, result,
            "The received reply from the server after executing the top command successfully with " +
                "n = 2 is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).getTopNMostListenedSongs(2);
    }

    @Test
    void testExecuteCommandProcessTopCommandWithNEqualToZero() {
        Command toProcess = new Command("top", List.of("0"));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.TOP_COMMAND_INVALID_ARGUMENT_REPLY.getReply(), result,
            "The received reply from the server after executing the top command successfully with " +
                "n = 0 is not the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessTopCommandWithNNegative() {
        Command toProcess = new Command("top", List.of("-6"));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.TOP_COMMAND_INVALID_ARGUMENT_REPLY.getReply(), result,
            "The received reply from the server after executing the top command successfully with " +
                "n = -6 is not the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessCreatePlaylistCommandSuccessfully() {
        Command toProcess = new Command("create-playlist", List.of("Favourites"));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.CREATE_PLAYLIST_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the create-playlist valid command " +
                "is not the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessCreatePlaylistCommandUserNotLoggedException() throws SpotifyException {
        Command toProcess = new Command("create-playlist", List.of("Favourites"));

        doThrow(new UserNotLoggedException((ServerReply.CREATE_PLAYLIST_NOT_LOGGED_REPLY.getReply())))
            .when(streamingPlatformMock).createPlaylist("Favourites" ,selectionKeyMock);

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.CREATE_PLAYLIST_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the create-playlist with user not logged " +
                "is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).createPlaylist("Favourites",
            selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessCreatePlaylistCommandIODatabaseException() throws SpotifyException {
        Command toProcess = new Command("create-playlist", List.of("Favourites"));

        doThrow(new IODatabaseException((ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply())))
            .when(streamingPlatformMock).createPlaylist("Favourites" ,selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), result,
            "The received reply from the server after executing the create-playlist with database exception " +
                "is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).createPlaylist("Favourites",
            selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessCreatePlaylistCommandNullPointerException() throws SpotifyException {
        Command toProcess = new Command("create-playlist", List.of("Favourites"));

        doThrow(new NullPointerException((ServerReply.SERVER_EXCEPTION.getReply())))
            .when(streamingPlatformMock).createPlaylist("Favourites" ,selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing the create-playlist with unexpected exception " +
                "thrown is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).createPlaylist("Favourites",
            selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessCreatePlaylistCommandPlaylistAlreadyExistException() throws SpotifyException {
        Command toProcess = new Command("create-playlist", List.of("Favourites"));

        doThrow(new PlaylistAlreadyExistException((ServerReply.CREATE_PLAYLIST_ALREADY_EXIST_REPLY.getReply())))
            .when(streamingPlatformMock).createPlaylist("Favourites" ,selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.CREATE_PLAYLIST_ALREADY_EXIST_REPLY.getReply(), result,
            "The received reply from the server after executing the create-playlist with playlist that " +
                "already exist is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).createPlaylist("Favourites",
            selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessDeletePlaylistCommandSuccessfully() {
        Command toProcess = new Command("delete-playlist", List.of("Favourites"));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.DELETE_PLAYLIST_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the delete-playlist valid command " +
                "is not the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessDeletePlaylistCommandUserNotLoggedException() throws SpotifyException {
        Command toProcess = new Command("delete-playlist", List.of("Favourites"));

        doThrow(new UserNotLoggedException((ServerReply.DELETE_PLAYLIST_NOT_LOGGED_REPLY.getReply())))
            .when(streamingPlatformMock).deletePlaylist("Favourites" ,selectionKeyMock);

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.DELETE_PLAYLIST_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the delete-playlist with user not logged " +
                "is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).deletePlaylist("Favourites",
            selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessDeletePlaylistCommandIODatabaseException() throws SpotifyException {
        Command toProcess = new Command("delete-playlist", List.of("Favourites"));

        doThrow(new IODatabaseException((ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply())))
            .when(streamingPlatformMock).deletePlaylist("Favourites" ,selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), result,
            "The received reply from the server after executing the delete-playlist with database exception " +
                "is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).deletePlaylist("Favourites",
            selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessDeletePlaylistCommandNoSuchPlaylistException() throws SpotifyException {
        Command toProcess = new Command("delete-playlist", List.of("Favourites"));

        doThrow(new NoSuchPlaylistException((ServerReply.DELETE_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply())))
            .when(streamingPlatformMock).deletePlaylist("Favourites" ,selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.DELETE_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply(), result,
            "The received reply from the server after executing the delete-playlist with playlist that " +
                "does not exist is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).deletePlaylist("Favourites",
            selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessDeletePlaylistCommandPlaylistNotEmptyExceptionException() throws SpotifyException {
        Command toProcess = new Command("delete-playlist", List.of("Favourites"));

        doThrow(new PlaylistNotEmptyException((ServerReply.DELETE_PLAYLIST_NOT_EMPTY_PLAYLIST_REPLY.getReply())))
            .when(streamingPlatformMock).deletePlaylist("Favourites" ,selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.DELETE_PLAYLIST_NOT_EMPTY_PLAYLIST_REPLY.getReply(), result,
            "The received reply from the server after executing the delete-playlist with playlist that " +
                "is not empty is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).deletePlaylist("Favourites",
            selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessDeletePlaylistCommandNullPointerException() throws SpotifyException {
        Command toProcess = new Command("delete-playlist", List.of("Favourites"));

        doThrow(new NullPointerException((ServerReply.SERVER_EXCEPTION.getReply())))
            .when(streamingPlatformMock).deletePlaylist("Favourites" ,selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing the delete-playlist with unexpected exception " +
                "thrown is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).deletePlaylist("Favourites",
            selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }


    @Test
    void testExecuteCommandProcessAddSongToCommandSuccessfully() {
        Command toProcess = new Command("add-song-to", List.of("Favourites", "No Time To Die"));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.ADD_SONG_TO_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the add-song-to valid command " +
                "is not the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessAddSongToCommandUserNotLoggedException() throws SpotifyException {
        Command toProcess = new Command("add-song-to", List.of("Favourites", "No Time To Die"));

        doThrow(new UserNotLoggedException((ServerReply.ADD_SONG_TO_NOT_LOGGED_REPLY.getReply())))
            .when(streamingPlatformMock).addSongToPlaylist("Favourites" , "No Time To Die",
                selectionKeyMock);

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.ADD_SONG_TO_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the add-song-to with user not logged " +
                "is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).addSongToPlaylist("Favourites",
            "No Time To Die", selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessAddSongToCommandIODatabaseException() throws SpotifyException {
        Command toProcess = new Command("add-song-to", List.of("Favourites", "No Time To Die"));

        doThrow(new IODatabaseException((ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply())))
            .when(streamingPlatformMock).addSongToPlaylist("Favourites", "No Time To Die",
                selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), result,
            "The received reply from the server after executing the add-song-to with database exception " +
                "is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).addSongToPlaylist("Favourites",
            "No Time To Die", selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessAddSongToCommandNoSuchSongException() throws SpotifyException {
        Command toProcess = new Command("add-song-to", List.of("Favourites", "No Time To Die"));

        doThrow(new NoSuchSongException((ServerReply.ADD_SONG_TO_NO_SUCH_SONG_REPLY.getReply())))
            .when(streamingPlatformMock).addSongToPlaylist("Favourites", "No Time To Die",
                selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.ADD_SONG_TO_NO_SUCH_SONG_REPLY.getReply(), result,
            "The received reply from the server after executing the add-song-to with song that " +
                "does not exist is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).addSongToPlaylist("Favourites",
            "No Time To Die", selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessAddSongToCommandNoSuchPlaylistException() throws SpotifyException {
        Command toProcess = new Command("add-song-to", List.of("Favourites", "No Time To Die"));

        doThrow(new NoSuchPlaylistException((ServerReply.ADD_SONG_TO_NO_SUCH_PLAYLIST_REPLY.getReply())))
            .when(streamingPlatformMock).addSongToPlaylist("Favourites", "No Time To Die",
                selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.ADD_SONG_TO_NO_SUCH_PLAYLIST_REPLY.getReply(), result,
            "The received reply from the server after executing the add-song-to with playlist that " +
                "does not exist is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).addSongToPlaylist("Favourites",
            "No Time To Die", selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessAddSongToCommandSongAlreadyInPlaylistException() throws SpotifyException {
        Command toProcess = new Command("add-song-to", List.of("Favourites", "No Time To Die"));

        doThrow(new SongAlreadyInPlaylistException((ServerReply.ADD_SONG_TO_SONG_ALREADY_EXIST_REPLY.getReply())))
            .when(streamingPlatformMock).addSongToPlaylist("Favourites", "No Time To Die",
                selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.ADD_SONG_TO_SONG_ALREADY_EXIST_REPLY.getReply(), result,
            "The received reply from the server after executing the add-song-to with song that " +
                "is already in the playlist is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).addSongToPlaylist("Favourites",
            "No Time To Die", selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessAddSongToCommandArrayIndexOutOfBoundsException() throws SpotifyException {
        Command toProcess = new Command("add-song-to", List.of("Favourites", "No Time To Die"));

        doThrow(new ArrayIndexOutOfBoundsException((ServerReply.SERVER_EXCEPTION.getReply())))
            .when(streamingPlatformMock).addSongToPlaylist("Favourites", "No Time To Die",
                selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing the add-song-to with unexpected " +
                "exception is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).addSongToPlaylist("Favourites",
            "No Time To Die", selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessRemoveSongFromCommandSuccessfully() {
        Command toProcess = new Command("remove-song-from", List.of("Favourites", "No Time To Die"));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.REMOVE_SONG_FROM_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the remove-song-from valid command " +
                "is not the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessRemoveSongFromCommandUserNotLoggedException() throws SpotifyException {
        Command toProcess = new Command("remove-song-from", List.of("Favourites", "No Time To Die"));

        doThrow(new UserNotLoggedException((ServerReply.REMOVE_SONG_FROM_NOT_LOGGED_REPLY.getReply())))
            .when(streamingPlatformMock).removeSongFromPlaylist("Favourites" , "No Time To Die",
                selectionKeyMock);

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.REMOVE_SONG_FROM_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the remove-song-from with user not logged " +
                "is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).removeSongFromPlaylist("Favourites",
            "No Time To Die", selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessRemoveSongFromCommandIODatabaseException() throws SpotifyException {
        Command toProcess = new Command("remove-song-from", List.of("Favourites", "No Time To Die"));

        doThrow(new IODatabaseException((ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply())))
            .when(streamingPlatformMock).removeSongFromPlaylist("Favourites", "No Time To Die",
                selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), result,
            "The received reply from the server after executing the remove-song-from with database exception " +
                "is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).removeSongFromPlaylist("Favourites",
            "No Time To Die", selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessRemoveSongFromCommandNoSuchSongException() throws SpotifyException {
        Command toProcess = new Command("remove-song-from", List.of("Favourites", "No Time To Die"));

        doThrow(new NoSuchSongException((ServerReply.REMOVE_SONG_FROM_NO_SUCH_SONG_REPLY.getReply())))
            .when(streamingPlatformMock).removeSongFromPlaylist("Favourites", "No Time To Die",
                selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.REMOVE_SONG_FROM_NO_SUCH_SONG_REPLY.getReply(), result,
            "The received reply from the server after executing the remove-song-from with song that " +
                "does not exist is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).removeSongFromPlaylist("Favourites",
            "No Time To Die", selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessRemoveSongFromCommandNoSuchPlaylistException() throws SpotifyException {
        Command toProcess = new Command("remove-song-from", List.of("Favourites", "No Time To Die"));

        doThrow(new NoSuchPlaylistException((ServerReply.REMOVE_SONG_FROM_NO_SUCH_PLAYLIST_REPLY.getReply())))
            .when(streamingPlatformMock).removeSongFromPlaylist("Favourites", "No Time To Die",
                selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.REMOVE_SONG_FROM_NO_SUCH_PLAYLIST_REPLY.getReply(), result,
            "The received reply from the server after executing the remove-song-from with playlist that " +
                "does not exist is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).removeSongFromPlaylist("Favourites",
            "No Time To Die", selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessRemoveSongFromCommandArrayIndexOutOfBoundsException() throws SpotifyException {
        Command toProcess = new Command("remove-song-from", List.of("Favourites", "No Time To Die"));

        doThrow(new ArrayIndexOutOfBoundsException((ServerReply.SERVER_EXCEPTION.getReply())))
            .when(streamingPlatformMock).removeSongFromPlaylist("Favourites", "No Time To Die",
                selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing the remove-song-from with unexpected " +
                "exception is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).removeSongFromPlaylist("Favourites",
            "No Time To Die", selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessShowPlaylistCommandSuccessfully() throws SpotifyException {
        Command toProcess = new Command("show-playlist", List.of("CrownMusic"));

        Song firstSongInPlaylist = new Song("The Crown - Main title","Hans Zimmer", 87, "classical");
        Song secondSongInPlaylist = new Song("The Crown - Bittersweet Symphony","Richard Ashcroft",248,
            "modern");

        Playlist toReturnPlaylist = new Playlist("sdvelev@gmail.com", "CrownMusic");
        toReturnPlaylist.addSong(firstSongInPlaylist);
        toReturnPlaylist.addSong(secondSongInPlaylist);

        when(streamingPlatformMock.showPlaylist("CrownMusic", selectionKeyMock))
            .thenReturn(toReturnPlaylist);

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        String expectedString = ServerReply.SHOW_PLAYLIST_SUCCESSFULLY_REPLY.getReply() + "CrownMusic" +
            System.lineSeparator() + "1 Title: The Crown - Main title Artist: Hans Zimmer Genre: classical " +
            "Duration (in seconds): 87" + System.lineSeparator() + "2 Title: The Crown - Bittersweet Symphony " +
            "Artist: Richard Ashcroft Genre: modern Duration (in seconds): 248" + System.lineSeparator();

        assertEquals(expectedString, result,
            "The received reply from the server after executing the show-playlist command successfully with " +
                "two found songs is not the same as the expected.");
        verify(streamingPlatformMock, times(1))
            .showPlaylist("CrownMusic", selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessShowPlaylistCommandNoSongs() throws SpotifyException {
        Command toProcess = new Command("show-playlist", List.of("CrownMusic"));

        Playlist toReturnPlaylist = new Playlist("sdvelev@gmail.com", "CrownMusic");

        when(streamingPlatformMock.showPlaylist("CrownMusic", selectionKeyMock))
            .thenReturn(toReturnPlaylist);

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.SHOW_PLAYLIST_NO_SONGS_REPLY.getReply(), result,
            "The received reply from the server after executing the show-playlist command successfully with " +
                "no found songs is not the same as the expected.");
        verify(streamingPlatformMock, times(1))
            .showPlaylist("CrownMusic", selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessShowPlaylistCommandUserNotLoggedException() throws SpotifyException {
        Command toProcess = new Command("show-playlist", List.of("CrownMusic"));

        when(streamingPlatformMock.showPlaylist("CrownMusic", selectionKeyMock))
            .thenThrow(new UserNotLoggedException(ServerReply.SHOW_PLAYLIST_NOT_LOGGED_REPLY.getReply()));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.SHOW_PLAYLIST_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the show-playlist command successfully with " +
                "not logged user is not the same as the expected.");
        verify(streamingPlatformMock, times(1))
            .showPlaylist("CrownMusic", selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessShowPlaylistCommandNoSuchPlaylistException() throws SpotifyException {
        Command toProcess = new Command("show-playlist", List.of("CrownMusic"));

        when(streamingPlatformMock.showPlaylist("CrownMusic", selectionKeyMock))
            .thenThrow(new NoSuchPlaylistException(ServerReply.SHOW_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply()));

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.SHOW_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply(), result,
            "The received reply from the server after executing the show-playlist command successfully with " +
                "no such playlist is not the same as the expected.");
        verify(streamingPlatformMock, times(1))
            .showPlaylist("CrownMusic", selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessShowPlaylistCommandNullPointerException() throws SpotifyException {
        Command toProcess = new Command("show-playlist", List.of("CrownMusic"));

        when(streamingPlatformMock.showPlaylist("CrownMusic", selectionKeyMock))
            .thenThrow(new NullPointerException(ServerReply.SERVER_EXCEPTION.getReply()));

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing the show-playlist command successfully with " +
                "unexpected exception is not the same as the expected.");
        verify(streamingPlatformMock, times(1))
            .showPlaylist("CrownMusic", selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessShowPlaylistsCommandSuccessfully() throws UserNotLoggedException {
        Command toProcess = new Command("show-playlists", new ArrayList<>());

        List<String> toReturnList = new ArrayList<>();
        toReturnList.add("MyFavourite");
        toReturnList.add("CrownMusic");

        when(streamingPlatformMock.showPlaylists(selectionKeyMock)).thenReturn(toReturnList);

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        String expectedString = ServerReply.SHOW_PLAYLISTS_SUCCESSFULLY_REPLY.getReply() + System.lineSeparator() +
            "1 Title: MyFavourite" + System.lineSeparator() +
            "2 Title: CrownMusic" + System.lineSeparator();

        assertEquals(expectedString, result,
            "The received reply from the server after executing the show-playlists command successfully with " +
                "two found playlists is not the same as the expected.");
        verify(streamingPlatformMock, times(1))
            .showPlaylists(selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessShowPlaylistsCommandNoPlaylists() throws UserNotLoggedException {
        Command toProcess = new Command("show-playlists", new ArrayList<>());

        when(streamingPlatformMock.showPlaylists(selectionKeyMock)).thenReturn(new ArrayList<>());

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.SHOW_PLAYLISTS_NO_PLAYLISTS_REPLY.getReply(), result,
            "The received reply from the server after executing the show-playlists command successfully with " +
                "no found playlists is not the same as the expected.");
        verify(streamingPlatformMock, times(1))
            .showPlaylists(selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessShowPlaylistsCommandUserNotLoggedException() throws UserNotLoggedException {
        Command toProcess = new Command("show-playlists", new ArrayList<>());

        when(streamingPlatformMock.showPlaylists(selectionKeyMock))
            .thenThrow(new UserNotLoggedException(ServerReply.SHOW_PLAYLISTS_NOT_LOGGED_REPLY.getReply()));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.SHOW_PLAYLISTS_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the show-playlists command with " +
                "not logged user is not the same as the expected.");
        verify(streamingPlatformMock, times(1))
            .showPlaylists(selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessShowPlaylistsCommandNullPointerException() throws UserNotLoggedException {
        Command toProcess = new Command("show-playlists", new ArrayList<>());

        when(streamingPlatformMock.showPlaylists(selectionKeyMock))
            .thenThrow(new NullPointerException(ServerReply.SERVER_EXCEPTION.getReply()));

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing the show-playlists command with " +
                "unexpected exception thrown is not the same as the expected.");
        verify(streamingPlatformMock, times(1))
            .showPlaylists(selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessPlayCommandSuccessfully() throws SpotifyException {
        Command toProcess = new Command("play", List.of("No Time To Die"));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.PLAY_SONG_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the play command successfully with " +
                "is not the same as the expected.");
        verify(streamingPlatformMock, times(1))
            .playSong("No Time To Die", selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessPlayCommandUserNotLoggedException() throws SpotifyException {
        Command toProcess = new Command("play", List.of("No Time To Die"));

        doThrow(new UserNotLoggedException(ServerReply.PLAY_SONG_NOT_LOGGED_REPLY.getReply()))
            .when(streamingPlatformMock).playSong("No Time To Die", selectionKeyMock);

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.PLAY_SONG_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the play command with " +
                "user not logged is not the same as the expected.");
        verify(streamingPlatformMock, times(1))
            .playSong("No Time To Die", selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessPlayCommandNoSuchSongException() throws SpotifyException {
        Command toProcess = new Command("play", List.of("No Time To Die"));

        doThrow(new NoSuchSongException(ServerReply.PLAY_SONG_NO_SUCH_SONG_REPLY.getReply()))
            .when(streamingPlatformMock).playSong("No Time To Die", selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.PLAY_SONG_NO_SUCH_SONG_REPLY.getReply(), result,
            "The received reply from the server after executing the play command with " +
                "song not found is not the same as the expected.");
        verify(streamingPlatformMock, times(1))
            .playSong("No Time To Die", selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessPlayCommandSongIsAlreadyPlayingException() throws SpotifyException {
        Command toProcess = new Command("play", List.of("No Time To Die"));

        doThrow(new SongIsAlreadyPlayingException(ServerReply.PLAY_SONG_IS_ALREADY_RUNNING_REPLY.getReply()))
            .when(streamingPlatformMock).playSong("No Time To Die", selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.PLAY_SONG_IS_ALREADY_RUNNING_REPLY.getReply(), result,
            "The received reply from the server after executing the play command with " +
                "song already playing is not the same as the expected.");
        verify(streamingPlatformMock, times(1))
            .playSong("No Time To Die", selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessPlayCommandIODatabaseException() throws SpotifyException {
        Command toProcess = new Command("play", List.of("No Time To Die"));

        doThrow(new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply()))
            .when(streamingPlatformMock).playSong("No Time To Die", selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), result,
            "The received reply from the server after executing the play command with " +
                "IO Database problem is not the same as the expected.");
        verify(streamingPlatformMock, times(1))
            .playSong("No Time To Die", selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessPlayCommandNullPointerException() throws SpotifyException {
        Command toProcess = new Command("play", List.of("No Time To Die"));

        doThrow(new NullPointerException(ServerReply.SERVER_EXCEPTION.getReply()))
            .when(streamingPlatformMock).playSong("No Time To Die", selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing the play command with " +
                "with unexpected exception thrown is not the same as the expected.");
        verify(streamingPlatformMock, times(1))
            .playSong("No Time To Die", selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessPlayPlaylistCommandSuccessfully() {
        Command toProcess = new Command("play-playlist", List.of("Favourites"));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.PLAY_PLAYLIST_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the play-playlist valid command " +
                "is not the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessPlayPlaylistCommandUserNotLoggedException() throws SpotifyException {
        Command toProcess = new Command("play-playlist", List.of("Favourites"));

        doThrow(new UserNotLoggedException((ServerReply.PLAY_PLAYLIST_NOT_LOGGED_REPLY.getReply())))
            .when(streamingPlatformMock).playPlaylist("Favourites" ,selectionKeyMock);

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.PLAY_PLAYLIST_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the play-playlist with user not logged " +
                "is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).playPlaylist("Favourites",
            selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessPlayPlaylistCommandSongIsAlreadyPlayingException() throws SpotifyException {
        Command toProcess = new Command("play-playlist", List.of("Favourites"));

        doThrow(new SongIsAlreadyPlayingException((ServerReply.PLAY_PLAYLIST_ALREADY_PLAYING.getReply())))
            .when(streamingPlatformMock).playPlaylist("Favourites" ,selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.PLAY_PLAYLIST_ALREADY_PLAYING.getReply(), result,
            "The received reply from the server after executing the play-playlist when song is already " +
                "running is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).playPlaylist("Favourites",
            selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessPlayPlaylistCommandNoSongsInPlaylistException() throws SpotifyException {
        Command toProcess = new Command("play-playlist", List.of("Favourites"));

        doThrow(new NoSongsInPlaylistException((ServerReply.PLAY_PLAYLIST_NO_SONGS_IN_PLAYLIST_REPLY.getReply())))
            .when(streamingPlatformMock).playPlaylist("Favourites" ,selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.PLAY_PLAYLIST_NO_SONGS_IN_PLAYLIST_REPLY.getReply(), result,
            "The received reply from the server after executing the play-playlist when no songs are found " +
                "is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).playPlaylist("Favourites",
            selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessPlayPlaylistCommandNoSuchPlaylistException() throws SpotifyException {
        Command toProcess = new Command("play-playlist", List.of("Favourites"));

        doThrow(new NoSuchPlaylistException((ServerReply.PLAY_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply())))
            .when(streamingPlatformMock).playPlaylist("Favourites" ,selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.PLAY_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply(), result,
            "The received reply from the server after executing the play-playlist when no such playlist is " +
                "found is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).playPlaylist("Favourites",
            selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessPlayPlaylistCommandNullPointerException() throws SpotifyException {
        Command toProcess = new Command("play-playlist", List.of("Favourites"));

        doThrow(new NullPointerException((ServerReply.SERVER_EXCEPTION.getReply())))
            .when(streamingPlatformMock).playPlaylist("Favourites" ,selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing the delete-playlist with unexpected " +
                "exception thrown is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).playPlaylist("Favourites",
            selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessStopCommandSuccessfully() throws SpotifyException, InterruptedException {
        Command toProcess = new Command("stop", new ArrayList<>());

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.STOP_COMMAND_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the stop command successfully is not " +
                "the same as the expected.");
        verify(streamingPlatformMock, times(1)).stopSong(selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessStopCommandUserNotLoggedException() throws SpotifyException, InterruptedException {
        Command toProcess = new Command("stop", new ArrayList<>());

        doThrow(new UserNotLoggedException((ServerReply.STOP_COMMAND_NOT_LOGGED_REPLY.getReply())))
            .when(streamingPlatformMock).stopSong(selectionKeyMock);

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.STOP_COMMAND_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the stop command when user is not logged " +
                "is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).stopSong(selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessStopCommandNoSongPlayingException() throws SpotifyException, InterruptedException {
        Command toProcess = new Command("stop", new ArrayList<>());

        doThrow(new NoSongPlayingException(ServerReply.STOP_COMMAND_NO_SONG_PLAYING.getReply()))
            .when(streamingPlatformMock).stopSong(selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.STOP_COMMAND_NO_SONG_PLAYING.getReply(), result,
            "The received reply from the server after executing the stop command when song is already playing " +
                "is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).stopSong(selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessStopCommandNullPointerException() throws SpotifyException , InterruptedException {
        Command toProcess = new Command("stop", new ArrayList<>());

        doThrow(new NullPointerException(ServerReply.STOP_COMMAND_ERROR_REPLY.getReply()))
            .when(streamingPlatformMock).stopSong(selectionKeyMock);

        when(streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.STOP_COMMAND_ERROR_REPLY.getReply(), result,
            "The received reply from the server after executing the stop command when unexpected exception is " +
                "thrown is not the same as the expected.");
        verify(streamingPlatformMock, times(1)).stopSong(selectionKeyMock);
        verify(streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessHelpCommandSuccessfully() {
        Command toProcess = new Command("help", new ArrayList<>());

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.HELP_COMMAND_REPLY.getReply(), result,
            "The received reply from the server after executing the help command successfully is not " +
                "the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessUnknownCommandSuccessfully() {
        Command toProcess = new Command("remove-playlist", new ArrayList<>());

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.UNKNOWN_COMMAND_REPLY.getReply(), result,
            "The received reply from the server after executing unknown command is not " +
                "the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessRegisterCommandSuccessfully() {
        Command toProcess = new Command("register", List.of("sdvelev@outlook.com", "123456"));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.REGISTER_COMMAND_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing register command successfully is not " +
                "the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessRegisterCommandNoSuchAlgorithmException() throws SpotifyException,
        NoSuchAlgorithmException {
        Command toProcess = new Command("register", List.of("sdvelev@outlook.com", "123456"));

        doThrow(new NoSuchAlgorithmException(ServerReply.REGISTER_COMMAND_ALGORITHM_REPLY.getReply()))
            .when(authenticationServiceMock).register("sdvelev@outlook.com", "123456");

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.REGISTER_COMMAND_ALGORITHM_REPLY.getReply(), result,
            "The received reply from the server after executing register command and algorithm is not " +
                "found is not the same as the expected.");
        verify(authenticationServiceMock, times(1))
            .register("sdvelev@outlook.com", "123456");
    }

    @Test
    void testExecuteCommandProcessRegisterCommandNotValidEmailFormatException() throws SpotifyException,
        NoSuchAlgorithmException {
        Command toProcess = new Command("register", List.of("sdvelev@outlook", "123456"));

        doThrow(new NotValidEmailFormatException(ServerReply.REGISTER_COMMAND_INVALID_EMAIL_REPLY.getReply()))
            .when(authenticationServiceMock).register("sdvelev@outlook", "123456");

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.REGISTER_COMMAND_INVALID_EMAIL_REPLY.getReply(), result,
            "The received reply from the server after executing register command and email is not " +
                "valid is not the same as the expected.");
        verify(authenticationServiceMock, times(1))
            .register("sdvelev@outlook", "123456");
    }

    @Test
    void testExecuteCommandProcessRegisterCommandEmailAlreadyRegisteredException() throws SpotifyException,
        NoSuchAlgorithmException {
        Command toProcess = new Command("register", List.of("sdvelev@outlook", "123456"));

        doThrow(new EmailAlreadyRegisteredException(ServerReply.REGISTER_COMMAND_ALREADY_EXIST_REPLY.getReply()))
            .when(authenticationServiceMock).register("sdvelev@outlook", "123456");

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.REGISTER_COMMAND_ALREADY_EXIST_REPLY.getReply(), result,
            "The received reply from the server after executing register command and email is already " +
                "registered is not the same as the expected.");
        verify(authenticationServiceMock, times(1))
            .register("sdvelev@outlook", "123456");
    }

    @Test
    void testExecuteCommandProcessRegisterCommandIODatabaseException() throws SpotifyException,
        NoSuchAlgorithmException {
        Command toProcess = new Command("register", List.of("sdvelev@outlook", "123456"));

        doThrow(new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply()))
            .when(authenticationServiceMock).register("sdvelev@outlook", "123456");

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), result,
            "The received reply from the server after executing register command and there is a problem with " +
                "database is not the same as the expected.");
        verify(authenticationServiceMock, times(1))
            .register("sdvelev@outlook", "123456");
    }

    @Test
    void testExecuteCommandProcessRegisterCommandIndexOutOfBoundsException() throws SpotifyException,
        NoSuchAlgorithmException {
        Command toProcess = new Command("register", List.of("sdvelev@outlook", "123456"));

        doThrow(new IndexOutOfBoundsException(ServerReply.SERVER_EXCEPTION.getReply()))
            .when(authenticationServiceMock).register("sdvelev@outlook", "123456");

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing register command and there is an unexpected " +
                "exception is not the same as the expected.");
        verify(authenticationServiceMock, times(1))
            .register("sdvelev@outlook", "123456");
    }

    @Test
    void testExecuteCommandProcessLoginCommandSuccessfully() {
        Command toProcess = new Command("login", List.of("sdvelev@outlook.com", "123456"));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.LOGIN_COMMAND_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing login command successfully is not " +
                "the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessLoginCommandNoSuchAlgorithmException() throws SpotifyException,
        NoSuchAlgorithmException {
        Command toProcess = new Command("login", List.of("sdvelev@outlook.com", "123456"));

        when(authenticationServiceMock.login("sdvelev@outlook.com", "123456"))
            .thenThrow(new NoSuchAlgorithmException(ServerReply.LOGIN_COMMAND_ALGORITHM_REPLY.getReply()));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.LOGIN_COMMAND_ALGORITHM_REPLY.getReply(), result,
            "The received reply from the server after executing login command and algorithm is not " +
                "found is not the same as the expected.");
        verify(authenticationServiceMock, times(1))
            .login("sdvelev@outlook.com", "123456");
    }

    @Test
    void testExecuteCommandProcessLoginCommandUserAlreadyLoggedException() {
        Command toProcess = new Command("login", List.of("sdvelev@outlook.com", "123456"));

        when(streamingPlatformMock.getAlreadyLogged()).thenReturn(Set.of(selectionKeyMock));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.LOGIN_COMMAND_USER_ALREADY_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing login command and user is already " +
                "logged is not the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessLoginCommandUserNotFoundException() throws SpotifyException,
        NoSuchAlgorithmException {
        Command toProcess = new Command("login", List.of("sdvelev@outlook", "123456"));

        when(authenticationServiceMock.login("sdvelev@outlook", "123456"))
            .thenThrow(new UserNotFoundException(ServerReply.LOGIN_COMMAND_USER_NOT_EXIST_REPLY.getReply()));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.LOGIN_COMMAND_USER_NOT_EXIST_REPLY.getReply(), result,
            "The received reply from the server after executing login command and user is not " +
                "found is not the same as the expected.");
        verify(authenticationServiceMock, times(1))
            .login("sdvelev@outlook", "123456");
    }

    @Test
    void testExecuteCommandProcessLoginCommandIODatabaseException() throws SpotifyException, NoSuchAlgorithmException {
        Command toProcess = new Command("login", List.of("sdvelev@outlook", "123456"));

        when(authenticationServiceMock.login("sdvelev@outlook", "123456"))
            .thenThrow(new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply()));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), result,
            "The received reply from the server after executing login command and there is a problem with " +
                "database is not the same as the expected.");
        verify(authenticationServiceMock, times(1))
            .login("sdvelev@outlook", "123456");
    }

    @Test
    void testExecuteCommandProcessLoginCommandIndexOutOfBoundsException() throws SpotifyException,
        NoSuchAlgorithmException {
        Command toProcess = new Command("login", List.of("sdvelev@outlook", "123456"));

        when(authenticationServiceMock.login("sdvelev@outlook", "123456"))
            .thenThrow(new IndexOutOfBoundsException(ServerReply.SERVER_EXCEPTION.getReply()));

        String result = commandExecutor.executeCommand(toProcess, selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing login command and there is an unexpected " +
                "exception is not the same as the expected.");
        verify(authenticationServiceMock, times(1))
            .login("sdvelev@outlook", "123456");
    }
}
