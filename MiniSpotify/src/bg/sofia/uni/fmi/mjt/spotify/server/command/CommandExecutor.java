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
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.UserAlreadyLoggedException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.UserNotLoggedException;
import bg.sofia.uni.fmi.mjt.spotify.server.logger.SpotifyLogger;
import bg.sofia.uni.fmi.mjt.spotify.server.login.AuthenticationService;
import bg.sofia.uni.fmi.mjt.spotify.server.login.User;
import bg.sofia.uni.fmi.mjt.spotify.server.storage.Playlist;
import bg.sofia.uni.fmi.mjt.spotify.server.storage.Song;
import bg.sofia.uni.fmi.mjt.spotify.server.storage.SongEntity;

import java.nio.channels.SelectionKey;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class CommandExecutor {

    private final static String REGISTER_COMMAND_NAME = "register";
    private final static String LOGIN_COMMAND_NAME = "login";
    private final static String SEARCH_COMMAND_NAME = "search";
    private final static String TOP_COMMAND_NAME = "top";
    private final static String CREATE_PLAYLIST_COMMAND_NAME = "create-playlist";
    private final static String ADD_SONG_TO_COMMAND_NAME = "add-song-to";
    private final static String SHOW_PLAYLIST_COMMAND_NAME = "show-playlist";
    private final static String PLAY_SONG_COMMAND_NAME = "play";
    private final static String STOP_COMMAND_NAME = "stop";
    private final static String LOGOUT_COMMAND_NAME = "logout";
    private final static String DISCONNECT_COMMAND_NAME = "disconnect";
    private final static String DELETE_PLAYLIST_COMMAND_NAME = "delete-playlist";
    private final static String REMOVE_SONG_FROM_COMMAND_NAME = "remove-song-from";
    private final static String SHOW_PLAYLISTS_COMMAND_NAME = "show-playlists";
    private final static String PLAY_PLAYLIST_COMMAND_NAME = "play-playlist";
    private final static String HELP_COMMAND_NAME = "help";

    private final static String EMAIL_MESSAGE_TO_LOG = "With email: ";
    private final static String REPLY_FIELD_TO_LOG = "Reply from server: ";
    private final static String TITLE_LABEL = " Title: ";
    private final static String ARTIST_LABEL = " Artist: ";
    private final static String DURATION_LABEL = " Duration (in seconds): ";
    private final static String GENRE_LABEL = " Genre: ";
    private final static String ZERO_CHARACTER = "0";
    private final static String TIMES_SIGN = "# ";
    private final static String POSITIVE_NUMBER_REGEX = "^[0-9]+$";

    private final StreamingPlatform streamingPlatform;
    private final AuthenticationService authenticationService;
    private final SpotifyLogger spotifyLogger;

    public CommandExecutor(StreamingPlatform streamingPlatform, AuthenticationService authenticationService,
                           SpotifyLogger spotifyLogger) {
        this.streamingPlatform = streamingPlatform;
        this.authenticationService = authenticationService;
        this.spotifyLogger = spotifyLogger;
    }

    public String executeCommand(Command cmd, SelectionKey selectionKey) {
        Objects.requireNonNull(cmd, "The provided command cannot be null.");
        Objects.requireNonNull(selectionKey, "The provided selectionKey cannot be null.");

        return switch(cmd.command()) {
            case REGISTER_COMMAND_NAME -> processRegisterCommand(cmd.arguments());
            case LOGIN_COMMAND_NAME -> processLoginCommand(cmd.arguments(), selectionKey);
            case LOGOUT_COMMAND_NAME -> processLogoutCommand(selectionKey);
            case DISCONNECT_COMMAND_NAME -> processDisconnectCommand(selectionKey);
            case SEARCH_COMMAND_NAME -> processSearchCommand(cmd.arguments());
            case TOP_COMMAND_NAME -> processTopCommand(cmd.arguments());
            case CREATE_PLAYLIST_COMMAND_NAME -> processCreatePlaylistCommand(cmd.arguments(), selectionKey);
            case DELETE_PLAYLIST_COMMAND_NAME -> processDeletePlaylistCommand(cmd.arguments(), selectionKey);
            case ADD_SONG_TO_COMMAND_NAME -> processAddSongToCommand(cmd.arguments(), selectionKey);
            case REMOVE_SONG_FROM_COMMAND_NAME -> processRemoveSongFromCommand(cmd.arguments(), selectionKey);
            case SHOW_PLAYLIST_COMMAND_NAME -> processShowPlaylistCommand(cmd.arguments(), selectionKey);
            case SHOW_PLAYLISTS_COMMAND_NAME -> processShowPlaylistsCommand(selectionKey);
            case PLAY_SONG_COMMAND_NAME -> processPlayCommand(cmd.arguments(), selectionKey);
            case PLAY_PLAYLIST_COMMAND_NAME -> processPlayPlaylistCommand(cmd.arguments(), selectionKey);
            case STOP_COMMAND_NAME -> processStopCommand(selectionKey);
            case HELP_COMMAND_NAME -> processHelpCommand();
            default -> ServerReply.UNKNOWN_COMMAND_REPLY.getReply();
        };
    }

    private String processHelpCommand() {
        return ServerReply.HELP_COMMAND_REPLY.getReply();
    }

    private String processDisconnectCommand(SelectionKey selectionKey) {
        if (streamingPlatform.getAlreadyLogged().contains(selectionKey)) {
            String result = processLogoutCommand(selectionKey);
            if (!result.equals(ServerReply.SERVER_EXCEPTION.getReply())) {
                return ServerReply.DISCONNECT_COMMAND_SUCCESSFULLY_REPLY.getReply();
            }

            return ServerReply.DISCONNECT_COMMAND_ERROR_REPLY.getReply();
        }

        return ServerReply.DISCONNECT_COMMAND_SUCCESSFULLY_REPLY.getReply();
    }

    private String processLogoutCommand(SelectionKey selectionKey) {
        try {
            streamingPlatform.logout(selectionKey);
        } catch (UserNotLoggedException e) {

            return getCorrectReply(Level.INFO, ServerReply.LOGOUT_COMMAND_USER_NOT_LOGGED_REPLY.getReply(), e);
        } catch (Exception e) {

            return getCorrectReply(Level.SEVERE, ServerReply.SERVER_EXCEPTION.getReply(), e);
        }

        return ServerReply.LOGOUT_COMMAND_SUCCESSFULLY_REPLY.getReply();
    }

    private String processStopCommand(SelectionKey selectionKey) {

        try {
            streamingPlatform.stopSong(selectionKey);
        } catch (UserNotLoggedException e) {
            return getCorrectReply(Level.INFO, ServerReply.STOP_COMMAND_NOT_LOGGED_REPLY.getReply(), e);
        } catch (NoSongPlayingException e) {
            return getCorrectReply(Level.INFO, streamingPlatform.getUser().getEmail(),
                ServerReply.STOP_COMMAND_NO_SONG_PLAYING.getReply(), e);
        } catch (Exception e) {
            return getCorrectReply(Level.SEVERE, streamingPlatform.getUser().getEmail(),
                ServerReply.STOP_COMMAND_ERROR_REPLY.getReply(), e);
        }

        return ServerReply.STOP_COMMAND_SUCCESSFULLY_REPLY.getReply();
    }

    private String processPlayCommand(List<String> arguments, SelectionKey selectionKey) {
        Objects.requireNonNull(arguments, "The provided list of arguments cannot be null.");

        String songName = arguments.get(0);
        try {
            streamingPlatform.playSong(songName, selectionKey);
        } catch (UserNotLoggedException e) {
            return getCorrectReply(Level.INFO, ServerReply.PLAY_SONG_NOT_LOGGED_REPLY.getReply(), e);
        } catch (NoSuchSongException e) {
            return getCorrectReply(Level.INFO, streamingPlatform.getUser().getEmail(),
                ServerReply.PLAY_SONG_NO_SUCH_SONG_REPLY.getReply(), e);
        } catch (SongIsAlreadyPlayingException e) {
            return getCorrectReply(Level.INFO, streamingPlatform.getUser().getEmail(),
                ServerReply.PLAY_SONG_IS_ALREADY_RUNNING_REPLY.getReply(), e);
        } catch (IODatabaseException e) {
            return getCorrectReply(Level.SEVERE, streamingPlatform.getUser().getEmail(),
                ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        } catch (Exception e) {
            return getCorrectReply(Level.SEVERE, streamingPlatform.getUser().getEmail(),
                ServerReply.SERVER_EXCEPTION.getReply(), e);
        }

        return ServerReply.PLAY_SONG_SUCCESSFULLY_REPLY.getReply();
    }

    private String processPlayPlaylistCommand(List<String> arguments, SelectionKey selectionKey) {

        Objects.requireNonNull(arguments, "The provided list of arguments cannot be null.");

        String playlistName = arguments.get(0);

        try {
            streamingPlatform.playPlaylist(playlistName, selectionKey);
        } catch (UserNotLoggedException e) {
            return getCorrectReply(Level.INFO, ServerReply.PLAY_PLAYLIST_NOT_LOGGED_REPLY.getReply(), e);
        } catch (SongIsAlreadyPlayingException e) {
            return getCorrectReply(Level.INFO, streamingPlatform.getUser().getEmail(),
                ServerReply.PLAY_PLAYLIST_ALREADY_PLAYING.getReply(), e);
        } catch (NoSuchPlaylistException e) {
            return getCorrectReply(Level.INFO, streamingPlatform.getUser().getEmail(),
                ServerReply.PLAY_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply(), e);
        } catch (NoSongsInPlaylistException e) {
            return getCorrectReply(Level.INFO, streamingPlatform.getUser().getEmail(),
                ServerReply.PLAY_PLAYLIST_NO_SONGS_IN_PLAYLIST_REPLY.getReply(), e);
        } catch (Exception e) {
            return getCorrectReply(Level.SEVERE, streamingPlatform.getUser().getEmail(),
                ServerReply.SERVER_EXCEPTION.getReply(), e);
        }

        return ServerReply.PLAY_PLAYLIST_SUCCESSFULLY_REPLY.getReply();
    }

    private String generateOutputShowPlaylistCommand(Playlist toReturn) {
        Objects.requireNonNull(toReturn, "The provided playlist cannot be null.");

        if (toReturn.getPlaylistSongs().isEmpty()) {
            return ServerReply.SHOW_PLAYLIST_NO_SONGS_REPLY.getReply();
        }

        StringBuilder resultString = new StringBuilder();
        resultString.append(ServerReply.SHOW_PLAYLIST_SUCCESSFULLY_REPLY.getReply()).append(toReturn.getTitle())
            .append(System.lineSeparator());

        int counter = 1;
        for (Song currentSong : toReturn.getPlaylistSongs()) {

            resultString.append(counter).append(TITLE_LABEL).append(currentSong.getTitle()).append(ARTIST_LABEL)
                .append(currentSong.getArtist()).append(GENRE_LABEL).append(currentSong.getGenre())
                .append(DURATION_LABEL).append(currentSong.getDuration()).append(System.lineSeparator());

            ++counter;
        }

        return resultString.toString();
    }

    private String processShowPlaylistCommand(List<String> arguments, SelectionKey selectionKey) {
        Objects.requireNonNull(arguments, "The provided list of arguments cannot be null.");

        String playlistTitle = arguments.get(0);
        try {
            Playlist toReturn = streamingPlatform.showPlaylist(playlistTitle, selectionKey);
            return generateOutputShowPlaylistCommand(toReturn);
        } catch (UserNotLoggedException e) {
            return getCorrectReply(Level.INFO, ServerReply.SHOW_PLAYLIST_NOT_LOGGED_REPLY.getReply(), e);
        } catch (NoSuchPlaylistException e) {
            return getCorrectReply(Level.INFO, streamingPlatform.getUser().getEmail(),
                ServerReply.SHOW_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply(), e);
        } catch (Exception e) {
            return getCorrectReply(Level.SEVERE, streamingPlatform.getUser().getEmail(),
                ServerReply.SERVER_EXCEPTION.getReply(), e);
        }
    }

    private String generateOutputShowPlaylistsCommand(List<String> playlistTitles) {
        Objects.requireNonNull(playlistTitles, "The provided list of playlist titles cannot be null.");

        if (playlistTitles.isEmpty()) {
            return ServerReply.SHOW_PLAYLISTS_NO_PLAYLISTS_REPLY.getReply();
        }

        StringBuilder resultString = new StringBuilder();
        resultString.append(ServerReply.SHOW_PLAYLISTS_SUCCESSFULLY_REPLY.getReply()).append(System.lineSeparator());
        int counter = 1;
        for (String currentPlaylistTitle : playlistTitles) {
            resultString.append(counter).append(TITLE_LABEL).append(currentPlaylistTitle)
                .append(System.lineSeparator());

            ++counter;
        }

        return resultString.toString();
    }

    private String processShowPlaylistsCommand(SelectionKey selectionKey) {

        try {
            List<String> playlistTitles = streamingPlatform.showPlaylists(selectionKey);
            return generateOutputShowPlaylistsCommand(playlistTitles);
        } catch (UserNotLoggedException e) {
            return getCorrectReply(Level.INFO, ServerReply.SHOW_PLAYLISTS_NOT_LOGGED_REPLY.getReply(), e);
        } catch (Exception e) {
            return getCorrectReply(Level.SEVERE, streamingPlatform.getUser().getEmail(),
                ServerReply.SERVER_EXCEPTION.getReply(), e);
        }
    }

    private String processAddSongToCommand(List<String> arguments, SelectionKey selectionKey) {

        Objects.requireNonNull(arguments, "The provided list of arguments cannot be null.");

        String playlistTitle = arguments.get(0);
        String songTitle = arguments.get(1);

        try {
            streamingPlatform.addSongToPlaylist(playlistTitle, songTitle, selectionKey);
        } catch (UserNotLoggedException e) {
            return getCorrectReply(Level.INFO, ServerReply.ADD_SONG_TO_NOT_LOGGED_REPLY.getReply(), e);
        } catch (NoSuchSongException e) {
            return getCorrectReply(Level.INFO, streamingPlatform.getUser().getEmail(),
                ServerReply.ADD_SONG_TO_NO_SUCH_SONG_REPLY.getReply(), e);
        } catch (NoSuchPlaylistException e) {
            return getCorrectReply(Level.INFO, streamingPlatform.getUser().getEmail(),
                ServerReply.ADD_SONG_TO_NO_SUCH_PLAYLIST_REPLY.getReply(), e);
        } catch (SongAlreadyInPlaylistException e) {
            return getCorrectReply(Level.INFO, streamingPlatform.getUser().getEmail(),
                ServerReply.ADD_SONG_TO_SONG_ALREADY_EXIST_REPLY.getReply(), e);
        } catch (IODatabaseException e) {
            return getCorrectReply(Level.SEVERE, streamingPlatform.getUser().getEmail(),
                ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        } catch (Exception e) {
            return getCorrectReply(Level.SEVERE, streamingPlatform.getUser().getEmail(),
                ServerReply.SERVER_EXCEPTION.getReply(), e);
        }

        return ServerReply.ADD_SONG_TO_SUCCESSFULLY_REPLY.getReply();
    }

    private String processRemoveSongFromCommand(List<String> arguments, SelectionKey selectionKey) {

        Objects.requireNonNull(arguments, "The provided list of arguments cannot be null.");

        String playlistTitle = arguments.get(0);
        String songTitle = arguments.get(1);
        try {
            streamingPlatform.removeSongFromPlaylist(playlistTitle, songTitle, selectionKey);
        } catch (UserNotLoggedException e) {
            return getCorrectReply(Level.INFO, ServerReply.REMOVE_SONG_FROM_NOT_LOGGED_REPLY.getReply(), e);
        } catch (NoSuchSongException e) {
            return getCorrectReply(Level.INFO, streamingPlatform.getUser().getEmail(),
                ServerReply.REMOVE_SONG_FROM_NO_SUCH_SONG_REPLY.getReply(), e);
        } catch (NoSuchPlaylistException e) {
            return getCorrectReply(Level.INFO, streamingPlatform.getUser().getEmail(),
                ServerReply.REMOVE_SONG_FROM_NO_SUCH_PLAYLIST_REPLY.getReply(), e);
        } catch (IODatabaseException e) {
            return getCorrectReply(Level.SEVERE, streamingPlatform.getUser().getEmail(),
                ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        } catch (Exception e) {
            return getCorrectReply(Level.SEVERE, streamingPlatform.getUser().getEmail(),
                ServerReply.SERVER_EXCEPTION.getReply(), e);
        }

        return ServerReply.REMOVE_SONG_FROM_SUCCESSFULLY_REPLY.getReply();
    }

    private String processCreatePlaylistCommand(List<String> arguments, SelectionKey selectionKey) {

        Objects.requireNonNull(arguments, "The provided list of arguments cannot be null.");

        String playlistTitle = arguments.get(0);
        try {
            streamingPlatform.createPlaylist(playlistTitle, selectionKey);
        } catch (UserNotLoggedException e) {
            return getCorrectReply(Level.INFO, ServerReply.CREATE_PLAYLIST_NOT_LOGGED_REPLY.getReply(), e);
        } catch (IODatabaseException e) {
            return getCorrectReply(Level.SEVERE, streamingPlatform.getUser().getEmail(),
                ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        } catch (PlaylistAlreadyExistException e) {
            return getCorrectReply(Level.INFO, streamingPlatform.getUser().getEmail(),
                ServerReply.CREATE_PLAYLIST_ALREADY_EXIST_REPLY.getReply(), e);
        } catch (Exception e) {
            return getCorrectReply(Level.SEVERE, streamingPlatform.getUser().getEmail(),
                ServerReply.SERVER_EXCEPTION.getReply(), e);
        }

        return ServerReply.CREATE_PLAYLIST_SUCCESSFULLY_REPLY.getReply();
    }

    private String processDeletePlaylistCommand(List<String> arguments, SelectionKey selectionKey) {

        Objects.requireNonNull(arguments, "The provided list of arguments cannot be null.");

        String playlistTitle = arguments.get(0);
        try {
            streamingPlatform.deletePlaylist(playlistTitle, selectionKey);
        } catch (UserNotLoggedException e) {
            return getCorrectReply(Level.INFO, ServerReply.DELETE_PLAYLIST_NOT_LOGGED_REPLY.getReply(), e);
        } catch (IODatabaseException e) {
            return getCorrectReply(Level.SEVERE, streamingPlatform.getUser().getEmail(),
                ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        } catch (NoSuchPlaylistException e) {
            return getCorrectReply(Level.INFO, streamingPlatform.getUser().getEmail(),
                ServerReply.DELETE_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply(), e);
        } catch (PlaylistNotEmptyException e) {
            return getCorrectReply(Level.INFO, streamingPlatform.getUser().getEmail(),
                ServerReply.DELETE_PLAYLIST_NOT_EMPTY_PLAYLIST_REPLY.getReply(), e);
        } catch (Exception e) {
            return getCorrectReply(Level.SEVERE, streamingPlatform.getUser().getEmail(),
                ServerReply.SERVER_EXCEPTION.getReply(), e);
        }

        return ServerReply.DELETE_PLAYLIST_SUCCESSFULLY_REPLY.getReply();
    }

    private String processSearchCommand(List<String> arguments) {
        Objects.requireNonNull(arguments, "The provided list of arguments cannot be null.");

        String wordToSearch = arguments.get(0);
        List<SongEntity> searchedSongs = streamingPlatform.searchSongs(wordToSearch);

        if (searchedSongs.isEmpty()) {
            return ServerReply.SEARCH_COMMAND_NO_SONGS_REPLY.getReply();
        }

        StringBuilder toReturn = new StringBuilder();
        toReturn.append(ServerReply.SEARCH_COMMAND_SUCCESSFULLY_REPLY.getReply()).append(System.lineSeparator());
        for (SongEntity currentSongEntity : searchedSongs) {

            toReturn.append(TITLE_LABEL).append(currentSongEntity.getSong().getTitle()).append(ARTIST_LABEL)
                .append(currentSongEntity.getSong().getArtist()).append(GENRE_LABEL)
                .append(currentSongEntity.getSong().getGenre()).append(DURATION_LABEL)
                .append(currentSongEntity.getSong().getDuration()).append(System.lineSeparator());

        }

        return toReturn.toString();
    }

    private String processRegisterCommand(List<String> arguments) {
        Objects.requireNonNull(arguments, "The provided list of arguments cannot be null.");

        String emailToRegister = arguments.get(0);
        String passwordToRegister = arguments.get(1);
        try {
            authenticationService.register(emailToRegister, passwordToRegister);
        } catch (NoSuchAlgorithmException e) {
            return getCorrectReply(Level.SEVERE, emailToRegister,
                ServerReply.REGISTER_COMMAND_ALGORITHM_REPLY.getReply(), e);
        } catch (NotValidEmailFormatException e) {
            return getCorrectReply(Level.INFO, emailToRegister,
                ServerReply.REGISTER_COMMAND_INVALID_EMAIL_REPLY.getReply(), e);
        } catch (EmailAlreadyRegisteredException e) {
            return getCorrectReply(Level.INFO, emailToRegister,
                ServerReply.REGISTER_COMMAND_ALREADY_EXIST_REPLY.getReply(), e);
        } catch (IODatabaseException e) {
            return getCorrectReply(Level.SEVERE, emailToRegister,
                ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        } catch (Exception e) {
            return getCorrectReply(Level.SEVERE, emailToRegister,
                ServerReply.SERVER_EXCEPTION.getReply(), e);
        }

        return ServerReply.REGISTER_COMMAND_SUCCESSFULLY_REPLY.getReply();
    }

    private void validateIsLogged(SelectionKey selectionKey) throws UserAlreadyLoggedException {
        if (streamingPlatform.getAlreadyLogged().contains(selectionKey)) {
            throw new UserAlreadyLoggedException(ServerReply.LOGIN_COMMAND_USER_ALREADY_LOGGED_REPLY.getReply());
        }
    }

    private String getCorrectReply(Level level, String message, Exception e) {
        spotifyLogger.log(level, REPLY_FIELD_TO_LOG + message, e);
        return message;
    }

    private String getCorrectReply(Level level, String email, String message, Exception e) {
        spotifyLogger.log(level, EMAIL_MESSAGE_TO_LOG + email + " " + REPLY_FIELD_TO_LOG +
            message, e);
        return message;
    }

    private String processLoginCommand(List<String> arguments, SelectionKey selectionKey) {
        Objects.requireNonNull(arguments, "The provided list of arguments cannot be null.");

        String emailToLogin = arguments.get(0);
        String passwordToLogin = arguments.get(1);
        try {
            validateIsLogged(selectionKey);
            User toLog = authenticationService.login(emailToLogin, passwordToLogin);
            streamingPlatform.setUser(toLog);
            streamingPlatform.getAlreadyLogged().add(selectionKey);
        } catch (UserNotFoundException e) {
            return getCorrectReply(Level.INFO, emailToLogin,
                ServerReply.LOGIN_COMMAND_USER_NOT_EXIST_REPLY.getReply(), e);
        } catch (NoSuchAlgorithmException e) {
            return getCorrectReply(Level.SEVERE, emailToLogin,
                ServerReply.LOGIN_COMMAND_ALGORITHM_REPLY.getReply(), e);
        } catch (IODatabaseException e) {
            return getCorrectReply(Level.SEVERE, emailToLogin,
                ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        } catch (UserAlreadyLoggedException e) {
            return getCorrectReply(Level.SEVERE, emailToLogin,
                ServerReply.LOGIN_COMMAND_USER_ALREADY_LOGGED_REPLY.getReply(), e);
        } catch (Exception e) {
            return getCorrectReply(Level.SEVERE, emailToLogin,
                ServerReply.SERVER_EXCEPTION.getReply(), e);
        }

        return ServerReply.LOGIN_COMMAND_SUCCESSFULLY_REPLY.getReply();
    }

    private String processTopCommand(List<String> arguments) {
        Objects.requireNonNull(arguments, "The provided list of arguments cannot be null.");

        if (arguments.get(0).equals(ZERO_CHARACTER) || !arguments.get(0).matches(POSITIVE_NUMBER_REGEX)) {
            return getCorrectReply(Level.INFO, ServerReply.TOP_COMMAND_INVALID_ARGUMENT_REPLY.getReply(),
                new IllegalArgumentException(ServerReply.TOP_COMMAND_INVALID_ARGUMENT_REPLY.getReply()));
        }

        List<SongEntity> result = streamingPlatform.getTopNMostListenedSongs(
            Integer.parseInt(arguments.get(0)));

        StringBuilder toReturn = new StringBuilder();
        toReturn.append(ServerReply.TOP_COMMAND_SUCCESSFULLY_REPLY.getReply()).append(System.lineSeparator());
        for (SongEntity currentSongEntity : result) {
            String toAppend = TIMES_SIGN + currentSongEntity.getListeningTimes() + TITLE_LABEL +
                currentSongEntity.getSong().getTitle() + ARTIST_LABEL + currentSongEntity.getSong().getArtist() +
                GENRE_LABEL + currentSongEntity.getSong().getGenre() + DURATION_LABEL +
                currentSongEntity.getSong().getDuration() + System.lineSeparator();
            toReturn.append(toAppend);
        }

        return toReturn.toString();
    }
}
