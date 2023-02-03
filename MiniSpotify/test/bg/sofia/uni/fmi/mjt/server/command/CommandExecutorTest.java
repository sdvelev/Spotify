package bg.sofia.uni.fmi.mjt.server.command;

import bg.sofia.uni.fmi.mjt.server.Server;
import bg.sofia.uni.fmi.mjt.server.ServerReply;
import bg.sofia.uni.fmi.mjt.server.StreamingPlatform;
import bg.sofia.uni.fmi.mjt.server.exceptions.IODatabaseException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoSongPlayingException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoSuchPlaylistException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoSuchSongException;
import bg.sofia.uni.fmi.mjt.server.exceptions.PlaylistAlreadyExistException;
import bg.sofia.uni.fmi.mjt.server.exceptions.PlaylistNotEmptyException;
import bg.sofia.uni.fmi.mjt.server.exceptions.SongAlreadyInPlaylistException;
import bg.sofia.uni.fmi.mjt.server.exceptions.SongIsAlreadyPlayingException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotLoggedException;
import bg.sofia.uni.fmi.mjt.server.logger.SpotifyLogger;
import bg.sofia.uni.fmi.mjt.server.login.User;
import bg.sofia.uni.fmi.mjt.server.storage.Playlist;
import bg.sofia.uni.fmi.mjt.server.storage.Song;
import bg.sofia.uni.fmi.mjt.server.storage.SongEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.internal.matchers.Null;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CommandExecutorTest {

    @Mock
    private SelectionKey selectionKeyMock = mock(SelectionKey.class);

    @Mock
    private StreamingPlatform streamingPlatformMock = mock(StreamingPlatform.class);

    private CommandExecutor commandExecutor;

    @Mock
    private SpotifyLogger spotifyLoggerMock = mock(SpotifyLogger.class);

    @BeforeEach
    void setTests() {

        this.commandExecutor = new CommandExecutor(streamingPlatformMock, spotifyLoggerMock);
    }

    @Test
    void testExecuteCommandProcessLogoutSuccessfully()
        throws UserNotLoggedException, NoSongPlayingException, InterruptedException {

        Command toProcess = new Command("logout", new ArrayList<>());

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.LOGOUT_COMMAND_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the logout command successfully is not " +
                "the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).logout(this.selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessLogoutUserNotLoggedException()
        throws UserNotLoggedException, NoSongPlayingException, InterruptedException {

        Command toProcess = new Command("logout", new ArrayList<>());

        doThrow(new UserNotLoggedException((ServerReply.LOGOUT_COMMAND_USER_NOT_LOGGED_REPLY.getReply())))
            .when(this.streamingPlatformMock).logout(this.selectionKeyMock);

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.LOGOUT_COMMAND_USER_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the logout command when user is not logged " +
                "is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).logout(this.selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessLogoutInterruptedException()
        throws UserNotLoggedException, NoSongPlayingException, InterruptedException {

        Command toProcess = new Command("logout", new ArrayList<>());

        doThrow(new InterruptedException((ServerReply.SERVER_EXCEPTION.getReply())))
            .when(this.streamingPlatformMock).logout(this.selectionKeyMock);

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing the logout command when InterruptedException " +
                "is thrown is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).logout(this.selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessDisconnectSuccessfullyWithLogout()
        throws UserNotLoggedException, NoSongPlayingException, InterruptedException {

        Command toProcess = new Command("disconnect", new ArrayList<>());


        when(this.streamingPlatformMock.getAlreadyLogged()).thenReturn(Set.of(this.selectionKeyMock));

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.DISCONNECT_COMMAND_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the disconnect command successfully with " +
                "logout is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).logout(this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getAlreadyLogged();
    }

    @Test
    void testExecuteCommandProcessDisconnectSuccessfullyWithoutLogout() {

        Command toProcess = new Command("disconnect", new ArrayList<>());

        when(this.streamingPlatformMock.getAlreadyLogged()).thenReturn(new HashSet<>());

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.DISCONNECT_COMMAND_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the disconnect command successfully without " +
                "logout is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).getAlreadyLogged();
    }

    @Test
    void testExecuteCommandProcessDisconnectInterruptedException()
        throws UserNotLoggedException, NoSongPlayingException, InterruptedException {

        Command toProcess = new Command("disconnect", new ArrayList<>());

        when(this.streamingPlatformMock.getAlreadyLogged()).thenReturn(Set.of(this.selectionKeyMock));

        doThrow(new InterruptedException((ServerReply.SERVER_EXCEPTION.getReply())))
            .when(this.streamingPlatformMock).logout(this.selectionKeyMock);

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.DISCONNECT_COMMAND_ERROR_REPLY.getReply(), result,
            "The received reply from the server after executing the disconnect command when InterruptedException " +
                "is thrown is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).logout(this.selectionKeyMock);
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

        when(this.streamingPlatformMock.searchSongs("the crown")).thenReturn(toReturnList);

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        String expectedString = ServerReply.SEARCH_COMMAND_SUCCESSFULLY_REPLY.getReply() + System.lineSeparator() +
            " Title: The Crown - Main title Artist: Hans Zimmer Genre: classical Duration (in seconds): 87" +
            System.lineSeparator() + " Title: The Crown - Bittersweet Symphony Artist: Richard Ashcroft " +
            "Genre: modern Duration (in seconds): 248" + System.lineSeparator();

        assertEquals(expectedString, result,
            "The received reply from the server after executing the search command successfully with " +
                "two found songs is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).searchSongs("the crown");
    }

    @Test
    void testExecuteCommandProcessSearchCommandNoSongs() {

        Command toProcess = new Command("search", List.of("Sineva"));

        when(this.streamingPlatformMock.searchSongs("Sineva")).thenReturn(new ArrayList<>());

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.SEARCH_COMMAND_NO_SONGS_REPLY.getReply(), result,
            "The received reply from the server after executing the search command successfully with " +
                "no found songs is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).searchSongs("Sineva");
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

        when(this.streamingPlatformMock.getTopNMostListenedSongs(2)).thenReturn(toReturnList);

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        String expectedString = ServerReply.TOP_COMMAND_SUCCESSFULLY_REPLY.getReply() + System.lineSeparator() +
            "# 16 Title: The Crown - Main title Artist: Hans Zimmer Genre: classical Duration (in seconds): 87" +
            System.lineSeparator() + "# 12 Title: The Crown - Bittersweet Symphony Artist: Richard Ashcroft " +
            "Genre: modern Duration (in seconds): 248" + System.lineSeparator();

        assertEquals(expectedString, result,
            "The received reply from the server after executing the top command successfully with " +
                "n = 2 is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).getTopNMostListenedSongs(2);
    }

    @Test
    void testExecuteCommandProcessTopCommandWithNEqualToZero() {

        Command toProcess = new Command("top", List.of("0"));

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.TOP_COMMAND_INVALID_ARGUMENT_REPLY.getReply(), result,
            "The received reply from the server after executing the top command successfully with " +
                "n = 0 is not the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessTopCommandWithNNegative() {

        Command toProcess = new Command("top", List.of("-6"));

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.TOP_COMMAND_INVALID_ARGUMENT_REPLY.getReply(), result,
            "The received reply from the server after executing the top command successfully with " +
                "n = -6 is not the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessCreatePlaylistCommandSuccessfully() {

        Command toProcess = new Command("create-playlist", List.of("Favourites"));

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.CREATE_PLAYLIST_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the create-playlist valid command " +
                "is not the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessCreatePlaylistCommandUserNotLoggedException()
        throws UserNotLoggedException, PlaylistAlreadyExistException, IODatabaseException {

        Command toProcess = new Command("create-playlist", List.of("Favourites"));

        doThrow(new UserNotLoggedException((ServerReply.CREATE_PLAYLIST_NOT_LOGGED_REPLY.getReply())))
            .when(this.streamingPlatformMock).createPlaylist("Favourites" ,this.selectionKeyMock);

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.CREATE_PLAYLIST_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the create-playlist with user not logged " +
                "is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).createPlaylist("Favourites",
            this.selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessCreatePlaylistCommandIODatabaseException()
        throws UserNotLoggedException, PlaylistAlreadyExistException, IODatabaseException {

        Command toProcess = new Command("create-playlist", List.of("Favourites"));

        doThrow(new IODatabaseException((ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply())))
            .when(this.streamingPlatformMock).createPlaylist("Favourites" ,this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), result,
            "The received reply from the server after executing the create-playlist with database exception " +
                "is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).createPlaylist("Favourites",
            this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessCreatePlaylistCommandNullPointerException()
        throws UserNotLoggedException, PlaylistAlreadyExistException, IODatabaseException {

        Command toProcess = new Command("create-playlist", List.of("Favourites"));

        doThrow(new NullPointerException((ServerReply.SERVER_EXCEPTION.getReply())))
            .when(this.streamingPlatformMock).createPlaylist("Favourites" ,this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing the create-playlist with unexpected exception " +
                "thrown is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).createPlaylist("Favourites",
            this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessCreatePlaylistCommandPlaylistAlreadyExistException()
        throws UserNotLoggedException, PlaylistAlreadyExistException, IODatabaseException {

        Command toProcess = new Command("create-playlist", List.of("Favourites"));

        doThrow(new PlaylistAlreadyExistException((ServerReply.CREATE_PLAYLIST_ALREADY_EXIST_REPLY.getReply())))
            .when(this.streamingPlatformMock).createPlaylist("Favourites" ,this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.CREATE_PLAYLIST_ALREADY_EXIST_REPLY.getReply(), result,
            "The received reply from the server after executing the create-playlist with playlist that " +
                "already exist is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).createPlaylist("Favourites",
            this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessDeletePlaylistCommandSuccessfully() {

        Command toProcess = new Command("delete-playlist", List.of("Favourites"));

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.DELETE_PLAYLIST_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the delete-playlist valid command " +
                "is not the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessDeletePlaylistCommandUserNotLoggedException()
        throws UserNotLoggedException, IODatabaseException, NoSuchPlaylistException,
        PlaylistNotEmptyException {

        Command toProcess = new Command("delete-playlist", List.of("Favourites"));

        doThrow(new UserNotLoggedException((ServerReply.DELETE_PLAYLIST_NOT_LOGGED_REPLY.getReply())))
            .when(this.streamingPlatformMock).deletePlaylist("Favourites" ,this.selectionKeyMock);

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.DELETE_PLAYLIST_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the delete-playlist with user not logged " +
                "is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).deletePlaylist("Favourites",
            this.selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessDeletePlaylistCommandIODatabaseException()
        throws UserNotLoggedException, IODatabaseException, NoSuchPlaylistException,
        PlaylistNotEmptyException {

        Command toProcess = new Command("delete-playlist", List.of("Favourites"));

        doThrow(new IODatabaseException((ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply())))
            .when(this.streamingPlatformMock).deletePlaylist("Favourites" ,this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), result,
            "The received reply from the server after executing the delete-playlist with database exception " +
                "is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).deletePlaylist("Favourites",
            this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessDeletePlaylistCommandNoSuchPlaylistException()
        throws UserNotLoggedException, IODatabaseException, NoSuchPlaylistException,
        PlaylistNotEmptyException {

        Command toProcess = new Command("delete-playlist", List.of("Favourites"));

        doThrow(new NoSuchPlaylistException((ServerReply.DELETE_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply())))
            .when(this.streamingPlatformMock).deletePlaylist("Favourites" ,this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.DELETE_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply(), result,
            "The received reply from the server after executing the delete-playlist with playlist that " +
                "does not exist is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).deletePlaylist("Favourites",
            this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessDeletePlaylistCommandPlaylistNotEmptyExceptionException()
        throws UserNotLoggedException, IODatabaseException, NoSuchPlaylistException,
        PlaylistNotEmptyException {

        Command toProcess = new Command("delete-playlist", List.of("Favourites"));

        doThrow(new PlaylistNotEmptyException((ServerReply.DELETE_PLAYLIST_NOT_EMPTY_PLAYLIST_REPLY.getReply())))
            .when(this.streamingPlatformMock).deletePlaylist("Favourites" ,this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.DELETE_PLAYLIST_NOT_EMPTY_PLAYLIST_REPLY.getReply(), result,
            "The received reply from the server after executing the delete-playlist with playlist that " +
                "is not empty is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).deletePlaylist("Favourites",
            this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessDeletePlaylistCommandNullPointerException()
        throws UserNotLoggedException, IODatabaseException, NoSuchPlaylistException,
        PlaylistNotEmptyException {

        Command toProcess = new Command("delete-playlist", List.of("Favourites"));

        doThrow(new NullPointerException((ServerReply.SERVER_EXCEPTION.getReply())))
            .when(this.streamingPlatformMock).deletePlaylist("Favourites" ,this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing the delete-playlist with unexpected exception " +
                "thrown is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).deletePlaylist("Favourites",
            this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }


    @Test
    void testExecuteCommandProcessAddSongToCommandSuccessfully() {

        Command toProcess = new Command("add-song-to", List.of("Favourites", "No Time To Die"));

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.ADD_SONG_TO_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the add-song-to valid command " +
                "is not the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessAddSongToCommandUserNotLoggedException()
        throws UserNotLoggedException, IODatabaseException, NoSuchSongException,
        NoSuchPlaylistException, SongAlreadyInPlaylistException {

        Command toProcess = new Command("add-song-to", List.of("Favourites", "No Time To Die"));

        doThrow(new UserNotLoggedException((ServerReply.ADD_SONG_TO_NOT_LOGGED_REPLY.getReply())))
            .when(this.streamingPlatformMock).addSongToPlaylist("Favourites" , "No Time To Die",
                this.selectionKeyMock);

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.ADD_SONG_TO_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the add-song-to with user not logged " +
                "is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).addSongToPlaylist("Favourites",
            "No Time To Die", this.selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessAddSongToCommandIODatabaseException()
        throws UserNotLoggedException, IODatabaseException, NoSuchSongException,
        NoSuchPlaylistException, SongAlreadyInPlaylistException {

        Command toProcess = new Command("add-song-to", List.of("Favourites", "No Time To Die"));

        doThrow(new IODatabaseException((ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply())))
            .when(this.streamingPlatformMock).addSongToPlaylist("Favourites", "No Time To Die",
                this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), result,
            "The received reply from the server after executing the add-song-to with database exception " +
                "is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).addSongToPlaylist("Favourites",
            "No Time To Die", this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessAddSongToCommandNoSuchSongException()
        throws UserNotLoggedException, IODatabaseException, NoSuchSongException, NoSuchPlaylistException,
        SongAlreadyInPlaylistException {

        Command toProcess = new Command("add-song-to", List.of("Favourites", "No Time To Die"));

        doThrow(new NoSuchSongException((ServerReply.ADD_SONG_TO_NO_SUCH_SONG_REPLY.getReply())))
            .when(this.streamingPlatformMock).addSongToPlaylist("Favourites", "No Time To Die",
                this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.ADD_SONG_TO_NO_SUCH_SONG_REPLY.getReply(), result,
            "The received reply from the server after executing the add-song-to with song that " +
                "does not exist is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).addSongToPlaylist("Favourites",
            "No Time To Die", this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessAddSongToCommandNoSuchPlaylistException()
        throws UserNotLoggedException, IODatabaseException, NoSuchSongException, NoSuchPlaylistException,
        SongAlreadyInPlaylistException {

        Command toProcess = new Command("add-song-to", List.of("Favourites", "No Time To Die"));

        doThrow(new NoSuchPlaylistException((ServerReply.ADD_SONG_TO_NO_SUCH_PLAYLIST_REPLY.getReply())))
            .when(this.streamingPlatformMock).addSongToPlaylist("Favourites", "No Time To Die",
                this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.ADD_SONG_TO_NO_SUCH_PLAYLIST_REPLY.getReply(), result,
            "The received reply from the server after executing the add-song-to with playlist that " +
                "does not exist is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).addSongToPlaylist("Favourites",
            "No Time To Die", this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessAddSongToCommandSongAlreadyInPlaylistException()
        throws UserNotLoggedException, IODatabaseException, NoSuchSongException, NoSuchPlaylistException,
        SongAlreadyInPlaylistException {

        Command toProcess = new Command("add-song-to", List.of("Favourites", "No Time To Die"));

        doThrow(new SongAlreadyInPlaylistException((ServerReply.ADD_SONG_TO_SONG_ALREADY_EXIST_REPLY.getReply())))
            .when(this.streamingPlatformMock).addSongToPlaylist("Favourites", "No Time To Die",
                this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.ADD_SONG_TO_SONG_ALREADY_EXIST_REPLY.getReply(), result,
            "The received reply from the server after executing the add-song-to with song that " +
                "is already in the playlist is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).addSongToPlaylist("Favourites",
            "No Time To Die", this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessAddSongToCommandArrayIndexOutOfBoundsException()
        throws UserNotLoggedException, IODatabaseException, NoSuchSongException, NoSuchPlaylistException,
        SongAlreadyInPlaylistException {

        Command toProcess = new Command("add-song-to", List.of("Favourites", "No Time To Die"));

        doThrow(new ArrayIndexOutOfBoundsException((ServerReply.SERVER_EXCEPTION.getReply())))
            .when(this.streamingPlatformMock).addSongToPlaylist("Favourites", "No Time To Die",
                this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing the add-song-to with unexpected " +
                "exception is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).addSongToPlaylist("Favourites",
            "No Time To Die", this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessRemoveSongFromCommandSuccessfully() {

        Command toProcess = new Command("remove-song-from", List.of("Favourites", "No Time To Die"));

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.REMOVE_SONG_FROM_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the remove-song-from valid command " +
                "is not the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessRemoveSongFromCommandUserNotLoggedException()
        throws UserNotLoggedException, IODatabaseException, NoSuchSongException, NoSuchPlaylistException {

        Command toProcess = new Command("remove-song-from", List.of("Favourites", "No Time To Die"));

        doThrow(new UserNotLoggedException((ServerReply.REMOVE_SONG_FROM_NOT_LOGGED_REPLY.getReply())))
            .when(this.streamingPlatformMock).removeSongFromPlaylist("Favourites" , "No Time To Die",
                this.selectionKeyMock);

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.REMOVE_SONG_FROM_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the remove-song-from with user not logged " +
                "is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).removeSongFromPlaylist("Favourites",
            "No Time To Die", this.selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessRemoveSongFromCommandIODatabaseException()
        throws UserNotLoggedException, IODatabaseException, NoSuchSongException, NoSuchPlaylistException {

        Command toProcess = new Command("remove-song-from", List.of("Favourites", "No Time To Die"));

        doThrow(new IODatabaseException((ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply())))
            .when(this.streamingPlatformMock).removeSongFromPlaylist("Favourites", "No Time To Die",
                this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), result,
            "The received reply from the server after executing the remove-song-from with database exception " +
                "is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).removeSongFromPlaylist("Favourites",
            "No Time To Die", this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessRemoveSongFromCommandNoSuchSongException()
        throws UserNotLoggedException, IODatabaseException, NoSuchSongException, NoSuchPlaylistException {

        Command toProcess = new Command("remove-song-from", List.of("Favourites", "No Time To Die"));

        doThrow(new NoSuchSongException((ServerReply.REMOVE_SONG_FROM_NO_SUCH_SONG_REPLY.getReply())))
            .when(this.streamingPlatformMock).removeSongFromPlaylist("Favourites", "No Time To Die",
                this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.REMOVE_SONG_FROM_NO_SUCH_SONG_REPLY.getReply(), result,
            "The received reply from the server after executing the remove-song-from with song that " +
                "does not exist is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).removeSongFromPlaylist("Favourites",
            "No Time To Die", this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessRemoveSongFromCommandNoSuchPlaylistException()
        throws UserNotLoggedException, IODatabaseException, NoSuchSongException, NoSuchPlaylistException {

        Command toProcess = new Command("remove-song-from", List.of("Favourites", "No Time To Die"));

        doThrow(new NoSuchPlaylistException((ServerReply.REMOVE_SONG_FROM_NO_SUCH_PLAYLIST_REPLY.getReply())))
            .when(this.streamingPlatformMock).removeSongFromPlaylist("Favourites", "No Time To Die",
                this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.REMOVE_SONG_FROM_NO_SUCH_PLAYLIST_REPLY.getReply(), result,
            "The received reply from the server after executing the remove-song-from with playlist that " +
                "does not exist is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).removeSongFromPlaylist("Favourites",
            "No Time To Die", this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessRemoveSongFromCommandArrayIndexOutOfBoundsException()
        throws UserNotLoggedException, IODatabaseException, NoSuchSongException, NoSuchPlaylistException {

        Command toProcess = new Command("remove-song-from", List.of("Favourites", "No Time To Die"));

        doThrow(new ArrayIndexOutOfBoundsException((ServerReply.SERVER_EXCEPTION.getReply())))
            .when(this.streamingPlatformMock).removeSongFromPlaylist("Favourites", "No Time To Die",
                this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing the remove-song-from with unexpected " +
                "exception is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).removeSongFromPlaylist("Favourites",
            "No Time To Die", this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessShowPlaylistCommandSuccessfully()
        throws UserNotLoggedException, NoSuchPlaylistException {

        //when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Command toProcess = new Command("show-playlist", List.of("CrownMusic"));

        Song firstSongInPlaylist = new Song("The Crown - Main title","Hans Zimmer", 87, "classical");
        Song secondSongInPlaylist = new Song("The Crown - Bittersweet Symphony","Richard Ashcroft",248,
            "modern");

        Playlist toReturnPlaylist = new Playlist("sdvelev@gmail.com", "CrownMusic");
        toReturnPlaylist.addSong(firstSongInPlaylist);
        toReturnPlaylist.addSong(secondSongInPlaylist);

        when(this.streamingPlatformMock.showPlaylist("CrownMusic", this.selectionKeyMock))
            .thenReturn(toReturnPlaylist);

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        String expectedString = ServerReply.SHOW_PLAYLIST_SUCCESSFULLY_REPLY.getReply() + "CrownMusic" +
            System.lineSeparator() + "1 Title: The Crown - Main title Artist: Hans Zimmer Genre: classical " +
            "Duration (in seconds): 87" + System.lineSeparator() + "2 Title: The Crown - Bittersweet Symphony " +
            "Artist: Richard Ashcroft Genre: modern Duration (in seconds): 248" + System.lineSeparator();

        assertEquals(expectedString, result,
            "The received reply from the server after executing the show-playlist command successfully with " +
                "two found songs is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1))
            .showPlaylist("CrownMusic", this.selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessShowPlaylistCommandNoSongs()
        throws UserNotLoggedException, NoSuchPlaylistException {

        //when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Command toProcess = new Command("show-playlist", List.of("CrownMusic"));

        Playlist toReturnPlaylist = new Playlist("sdvelev@gmail.com", "CrownMusic");

        when(this.streamingPlatformMock.showPlaylist("CrownMusic", this.selectionKeyMock))
            .thenReturn(toReturnPlaylist);

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.SHOW_PLAYLIST_NO_SONGS_REPLY.getReply(), result,
            "The received reply from the server after executing the show-playlist command successfully with " +
                "no found songs is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1))
            .showPlaylist("CrownMusic", this.selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessShowPlaylistCommandUserNotLoggedException()
        throws UserNotLoggedException, NoSuchPlaylistException {

        //when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Command toProcess = new Command("show-playlist", List.of("CrownMusic"));

        when(this.streamingPlatformMock.showPlaylist("CrownMusic", this.selectionKeyMock))
            .thenThrow(new UserNotLoggedException(ServerReply.SHOW_PLAYLIST_NOT_LOGGED_REPLY.getReply()));

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.SHOW_PLAYLIST_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the show-playlist command successfully with " +
                "not logged user is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1))
            .showPlaylist("CrownMusic", this.selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessShowPlaylistCommandNoSuchPlaylistException()
        throws UserNotLoggedException, NoSuchPlaylistException {

        //when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Command toProcess = new Command("show-playlist", List.of("CrownMusic"));

        when(this.streamingPlatformMock.showPlaylist("CrownMusic", this.selectionKeyMock))
            .thenThrow(new NoSuchPlaylistException(ServerReply.SHOW_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply()));

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.SHOW_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply(), result,
            "The received reply from the server after executing the show-playlist command successfully with " +
                "no such playlist is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1))
            .showPlaylist("CrownMusic", this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessShowPlaylistCommandNullPointerException()
        throws UserNotLoggedException, NoSuchPlaylistException {

        Command toProcess = new Command("show-playlist", List.of("CrownMusic"));

        when(this.streamingPlatformMock.showPlaylist("CrownMusic", this.selectionKeyMock))
            .thenThrow(new NullPointerException(ServerReply.SERVER_EXCEPTION.getReply()));

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing the show-playlist command successfully with " +
                "unexpected exception is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1))
            .showPlaylist("CrownMusic", this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessShowPlaylistsCommandSuccessfully()
        throws UserNotLoggedException {

        //when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Command toProcess = new Command("show-playlists", new ArrayList<>());

        List<String> toReturnList = new ArrayList<>();
        toReturnList.add("MyFavourite");
        toReturnList.add("CrownMusic");

        when(this.streamingPlatformMock.showPlaylists(this.selectionKeyMock)).thenReturn(toReturnList);

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        String expectedString = ServerReply.SHOW_PLAYLISTS_SUCCESSFULLY_REPLY.getReply() + System.lineSeparator() +
            "1 Title: MyFavourite" + System.lineSeparator() +
            "2 Title: CrownMusic" + System.lineSeparator();

        assertEquals(expectedString, result,
            "The received reply from the server after executing the show-playlists command successfully with " +
                "two found playlists is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1))
            .showPlaylists(this.selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessShowPlaylistsCommandNoPlaylists()
        throws UserNotLoggedException {

        //when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Command toProcess = new Command("show-playlists", new ArrayList<>());

        when(this.streamingPlatformMock.showPlaylists(this.selectionKeyMock)).thenReturn(new ArrayList<>());

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.SHOW_PLAYLISTS_NO_PLAYLISTS_REPLY.getReply(), result,
            "The received reply from the server after executing the show-playlists command successfully with " +
                "no found playlists is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1))
            .showPlaylists(this.selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessShowPlaylistsCommandUserNotLoggedException()
        throws UserNotLoggedException {

        //when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Command toProcess = new Command("show-playlists", new ArrayList<>());

        when(this.streamingPlatformMock.showPlaylists(this.selectionKeyMock))
            .thenThrow(new UserNotLoggedException(ServerReply.SHOW_PLAYLISTS_NOT_LOGGED_REPLY.getReply()));

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.SHOW_PLAYLISTS_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the show-playlists command with " +
                "not logged user is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1))
            .showPlaylists(this.selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessShowPlaylistsCommandNullPointerException()
        throws UserNotLoggedException {

        //when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Command toProcess = new Command("show-playlists", new ArrayList<>());

        when(this.streamingPlatformMock.showPlaylists(this.selectionKeyMock))
            .thenThrow(new NullPointerException(ServerReply.SERVER_EXCEPTION.getReply()));

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing the show-playlists command with " +
                "unexpected exception thrown is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1))
            .showPlaylists(this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessPlayCommandSuccessfully()
        throws UserNotLoggedException, NoSuchSongException, IODatabaseException, SongIsAlreadyPlayingException {

        //when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Command toProcess = new Command("play", List.of("No Time To Die"));

        //when(this.streamingPlatformMock.playSong("No Time To Die", this.selectionKeyMock)).thenReturn(new ArrayList<>());

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.PLAY_SONG_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the play command successfully with " +
                "is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1))
            .playSong("No Time To Die", this.selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessPlayCommandUserNotLoggedException()
        throws UserNotLoggedException, NoSuchSongException, IODatabaseException, SongIsAlreadyPlayingException {

        //when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Command toProcess = new Command("play", List.of("No Time To Die"));

        doThrow(new UserNotLoggedException(ServerReply.PLAY_SONG_NOT_LOGGED_REPLY.getReply()))
            .when(this.streamingPlatformMock).playSong("No Time To Die", this.selectionKeyMock);

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.PLAY_SONG_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the play command with " +
                "user not logged is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1))
            .playSong("No Time To Die", this.selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessPlayCommandNoSuchSongException()
        throws UserNotLoggedException, NoSuchSongException, IODatabaseException, SongIsAlreadyPlayingException {

        //when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Command toProcess = new Command("play", List.of("No Time To Die"));

        doThrow(new NoSuchSongException(ServerReply.PLAY_SONG_NO_SUCH_SONG_REPLY.getReply()))
            .when(this.streamingPlatformMock).playSong("No Time To Die", this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.PLAY_SONG_NO_SUCH_SONG_REPLY.getReply(), result,
            "The received reply from the server after executing the play command with " +
                "song not found is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1))
            .playSong("No Time To Die", this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessPlayCommandSongIsAlreadyPlayingException()
        throws UserNotLoggedException, NoSuchSongException, IODatabaseException, SongIsAlreadyPlayingException {

        //when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Command toProcess = new Command("play", List.of("No Time To Die"));

        doThrow(new SongIsAlreadyPlayingException(ServerReply.PLAY_SONG_IS_ALREADY_RUNNING_REPLY.getReply()))
            .when(this.streamingPlatformMock).playSong("No Time To Die", this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.PLAY_SONG_IS_ALREADY_RUNNING_REPLY.getReply(), result,
            "The received reply from the server after executing the play command with " +
                "song already playing is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1))
            .playSong("No Time To Die", this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessPlayCommandIODatabaseException()
        throws UserNotLoggedException, NoSuchSongException, IODatabaseException, SongIsAlreadyPlayingException {

        //when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Command toProcess = new Command("play", List.of("No Time To Die"));

        doThrow(new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply()))
            .when(this.streamingPlatformMock).playSong("No Time To Die", this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), result,
            "The received reply from the server after executing the play command with " +
                "IO Database problem is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1))
            .playSong("No Time To Die", this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessPlayCommandNullPointerException()
        throws UserNotLoggedException, NoSuchSongException, IODatabaseException, SongIsAlreadyPlayingException {

        //when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        Command toProcess = new Command("play", List.of("No Time To Die"));

        doThrow(new NullPointerException(ServerReply.SERVER_EXCEPTION.getReply()))
            .when(this.streamingPlatformMock).playSong("No Time To Die", this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing the play command with " +
                "with unexpected exception thrown is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1))
            .playSong("No Time To Die", this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessPlayPlaylistCommandSuccessfully() {

        Command toProcess = new Command("play-playlist", List.of("Favourites"));

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.PLAY_PLAYLIST_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the play-playlist valid command " +
                "is not the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessPlayPlaylistCommandUserNotLoggedException()
        throws UserNotLoggedException, SongIsAlreadyPlayingException {

        Command toProcess = new Command("play-playlist", List.of("Favourites"));

        doThrow(new UserNotLoggedException((ServerReply.PLAY_PLAYLIST_NOT_LOGGED_REPLY.getReply())))
            .when(this.streamingPlatformMock).playPlaylist("Favourites" ,this.selectionKeyMock);

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.PLAY_PLAYLIST_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the play-playlist with user not logged " +
                "is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).playPlaylist("Favourites",
            this.selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessPlayPlaylistCommandSongIsAlreadyPlayingException()
        throws UserNotLoggedException, SongIsAlreadyPlayingException {

        Command toProcess = new Command("play-playlist", List.of("Favourites"));

        doThrow(new SongIsAlreadyPlayingException((ServerReply.PLAY_PLAYLIST_ALREADY_PLAYING.getReply())))
            .when(this.streamingPlatformMock).playPlaylist("Favourites" ,this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.PLAY_PLAYLIST_ALREADY_PLAYING.getReply(), result,
            "The received reply from the server after executing the play-playlist when song is already " +
                "running is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).playPlaylist("Favourites",
            this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessPlayPlaylistCommandNullPointerException()
        throws UserNotLoggedException, SongIsAlreadyPlayingException {

        Command toProcess = new Command("play-playlist", List.of("Favourites"));

        doThrow(new NullPointerException((ServerReply.SERVER_EXCEPTION.getReply())))
            .when(this.streamingPlatformMock).playPlaylist("Favourites" ,this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.SERVER_EXCEPTION.getReply(), result,
            "The received reply from the server after executing the delete-playlist with unexpected " +
                "exception thrown is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).playPlaylist("Favourites",
            this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessStopCommandSuccessfully()
        throws UserNotLoggedException, NoSongPlayingException, InterruptedException {

        Command toProcess = new Command("stop", new ArrayList<>());

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.STOP_COMMAND_SUCCESSFULLY_REPLY.getReply(), result,
            "The received reply from the server after executing the stop command successfully is not " +
                "the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).stopSong(this.selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessStopCommandUserNotLoggedException()
        throws UserNotLoggedException, NoSongPlayingException, InterruptedException {

        Command toProcess = new Command("stop", new ArrayList<>());

        doThrow(new UserNotLoggedException((ServerReply.STOP_COMMAND_NOT_LOGGED_REPLY.getReply())))
            .when(this.streamingPlatformMock).stopSong(this.selectionKeyMock);

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.STOP_COMMAND_NOT_LOGGED_REPLY.getReply(), result,
            "The received reply from the server after executing the stop command when user is not logged " +
                "is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).stopSong(this.selectionKeyMock);
    }

    @Test
    void testExecuteCommandProcessStopCommandNoSongPlayingException()
        throws UserNotLoggedException, NoSongPlayingException, InterruptedException {

        Command toProcess = new Command("stop", new ArrayList<>());

        doThrow(new NoSongPlayingException(ServerReply.STOP_COMMAND_NO_SONG_PLAYING.getReply()))
            .when(this.streamingPlatformMock).stopSong(this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.STOP_COMMAND_NO_SONG_PLAYING.getReply(), result,
            "The received reply from the server after executing the stop command when song is already playing " +
                "is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).stopSong(this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessStopCommandNullPointerException()
        throws UserNotLoggedException, NoSongPlayingException, InterruptedException {

        Command toProcess = new Command("stop", new ArrayList<>());

        doThrow(new NullPointerException(ServerReply.STOP_COMMAND_ERROR_REPLY.getReply()))
            .when(this.streamingPlatformMock).stopSong(this.selectionKeyMock);

        when(this.streamingPlatformMock.getUser()).thenReturn(new User("sdvelev@gmail.com", "123456"));
        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.STOP_COMMAND_ERROR_REPLY.getReply(), result,
            "The received reply from the server after executing the stop command when unexpected exception is " +
                "thrown is not the same as the expected.");
        verify(this.streamingPlatformMock, times(1)).stopSong(this.selectionKeyMock);
        verify(this.streamingPlatformMock, times(1)).getUser();
    }

    @Test
    void testExecuteCommandProcessHelpCommandSuccessfully() {

        Command toProcess = new Command("help", new ArrayList<>());

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.HELP_COMMAND_REPLY.getReply(), result,
            "The received reply from the server after executing the help command successfully is not " +
                "the same as the expected.");
    }

    @Test
    void testExecuteCommandProcessUnknownCommandSuccessfully() {

        Command toProcess = new Command("remove-playlist", new ArrayList<>());

        String result = this.commandExecutor.executeCommand(toProcess, this.selectionKeyMock);

        assertEquals(ServerReply.UNKNOWN_COMMAND_REPLY.getReply(), result,
            "The received reply from the server after executing unknown command is not " +
                "the same as the expected.");
    }

}
