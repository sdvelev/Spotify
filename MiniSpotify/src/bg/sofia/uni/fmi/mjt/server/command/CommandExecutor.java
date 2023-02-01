package bg.sofia.uni.fmi.mjt.server.command;

import bg.sofia.uni.fmi.mjt.server.Server;
import bg.sofia.uni.fmi.mjt.server.ServerReply;
import bg.sofia.uni.fmi.mjt.server.StreamingPlatform;
import bg.sofia.uni.fmi.mjt.server.exceptions.EmailAlreadyRegisteredException;
import bg.sofia.uni.fmi.mjt.server.exceptions.IODatabaseException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoSongPlayingException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoSuchPlaylistException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoSuchSongException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NotValidEmailFormatException;
import bg.sofia.uni.fmi.mjt.server.exceptions.PlaylistAlreadyExistException;
import bg.sofia.uni.fmi.mjt.server.exceptions.PlaylistNotEmptyException;
import bg.sofia.uni.fmi.mjt.server.exceptions.SongAlreadyInPlaylistException;
import bg.sofia.uni.fmi.mjt.server.exceptions.SongIsAlreadyPlayingException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserAlreadyLoggedException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotLoggedException;
import bg.sofia.uni.fmi.mjt.server.logger.SpotifyLogger;
import bg.sofia.uni.fmi.mjt.server.login.User;
import bg.sofia.uni.fmi.mjt.server.storage.Playlist;
import bg.sofia.uni.fmi.mjt.server.storage.Song;
import bg.sofia.uni.fmi.mjt.server.storage.SongEntity;

import java.nio.channels.SelectionKey;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;

import static bg.sofia.uni.fmi.mjt.server.login.Authentication.login;
import static bg.sofia.uni.fmi.mjt.server.login.Authentication.register;

public class CommandExecutor {

    private final static String REGISTER_COMMAND_NAME = "register";
    private final static String REGISTER_COMMAND_SUCCESSFULLY_REPLY = "The registration process is successful. Now " +
        "you can log in.";
    private final static String REGISTER_COMMAND_ALREADY_EXIST_REPLY = "The registration process is unsuccessful " +
        "as the email is already registered.";
    private final static String REGISTER_COMMAND_ALGORITHM_REPLY = "The registration process is unsuccessful " +
        "as there's a problem in the hashing algorithm. Please, try again later.";
    private final static String REGISTER_COMMAND_INVALID_EMAIL_REPLY = "The registration process is " +
        "unsuccessful as the provided email is not valid. Please, try to enter it again.";

    private final static String LOGIN_COMMAND_NAME = "login";
    private final static String LOGIN_COMMAND_SUCCESSFULLY_REPLY = "The login process is successful. " +
        "Now you are logged-in.";
    private final static String LOGIN_COMMAND_USER_NOT_EXIST_REPLY = "The login process is not successful as " +
        "there is not such a profile.";
    private final static String LOGIN_COMMAND_ALGORITHM_REPLY = "The login process is not successful as there is a " +
        "problem in the hashing algorithm.";

    private final static String SEARCH_COMMAND_NAME = "search";

    private final static String TOP_COMMAND_NAME = "top";
    private final static String TOP_COMMAND_INVALID_ARGUMENT_REPLY = "The provided input is in invalid format. " +
        "Please, enter whole non-negative number.";

    private final static String CREATE_PLAYLIST_NAME = "create-playlist";
    private final static String CREATE_PLAYLIST_SUCCESSFULLY_REPLY = "The playlist was created successfully.";
    private final static String CREATE_PLAYLIST_NOT_LOGGED_REPLY = "The playlist was not created successfully as " +
        "you are not logged-in. Please, try first to login.";


    private final static String ADD_SONG_TO_NAME = "add-song-to";
    private final static String ADD_SONG_TO_SUCCESSFULLY_REPLY = "You have successfully added the song to the playlist";
    private final static String ADD_SONG_TO_NO_SUCH_SONG_REPLY = "We could not find such a song in our platform.";
    private final static String ADD_SONG_TO_NO_SUCH_PLAYLIST_REPLY = "We could not find such a playlist " +
        "associated with that profile";
    private final static String ADD_SONG_TO_NOT_LOGGED_REPLY = "The song was not added to the playlist as " +
        "you are not logged-in. Please, try first to login.";

    private final static String SHOW_PLAYLIST_NAME = "show-playlist";
    private final static String SHOW_PLAYLIST_NOT_LOGGED_REPLY = "You are not logged-in. Please, try to log-in first.";
    private final static String SHOW_PLAYLIST_NO_SUCH_PLAYLIST_REPLY = "We could not find such a playlist " +
        "associated with that profile.";

    private final static String PLAY_SONG_NAME = "play";
    private final static String PLAY_SONG_NOT_LOGGED_REPLY = "You cannot play songs unless you are logged-in.";
    private final static String PLAY_SONG_NO_SUCH_SONG_REPLY = "There is not such a song in the platform.";
    private final static String PLAY_SONG_SUCCESSFULLY_REPLY = "The song is playing.";
    private final static String PLAY_SONG_IS_ALREADY_RUNNING_REPLY = "Song has already been started and is now " +
        "playing. You can stop it with the relevant command or wait for it to finish.";

    private final static String STOP_COMMAND_NAME = "stop";
    private final static String STOP_COMMAND_SUCCESSFULLY_REPLY = "The song was stopped successfully";
    private final static String STOP_COMMAND_NOT_LOGGED_REPLY = "You cannot stop a song as you are not logged-in.";
    private final static String STOP_COMMAND_NO_SONG_PLAYING = "There is not a song which is playing at the moment.";

    private final static String LOGOUT_COMMAND_NAME = "logout";

    private final static String DISCONNECT_COMMAND_NAME = "disconnect";

    private final static String DELETE_PLAYLIST_COMMAND_NAME = "delete-playlist";

    private final static String UNKNOWN_COMMAND_REPLY = "The inserted command is not correct or in the right " +
        "format. Please, try to enter it again.";


    private final static String EMAIL_MESSAGE_TO_LOG = "With email: ";
    private final static String PASSWORD_MESSAGE_TO_LOG = "With password: ";
    private final static String REPLY_FIELD_TO_LOG = "Reply from server: ";

    private final static String POSITIVE_NUMBER_REGEX = "^[0-9]+$";

    private StreamingPlatform streamingPlatform;

    public CommandExecutor(StreamingPlatform streamingPlatform) {
        this.streamingPlatform = streamingPlatform;
    }

    public String executeCommand(Command cmd, SelectionKey selectionKey) {

        return switch(cmd.command()) {

            case REGISTER_COMMAND_NAME -> this.processRegisterCommand(cmd.arguments());
            case LOGIN_COMMAND_NAME -> this.processLoginCommand(cmd.arguments(), selectionKey);
            case LOGOUT_COMMAND_NAME -> this.processLogoutCommand(selectionKey);
            case DISCONNECT_COMMAND_NAME -> this.processDisconnectCommand(selectionKey);
            case SEARCH_COMMAND_NAME -> this.processSearchCommand(cmd.arguments());
            case TOP_COMMAND_NAME -> this.processTopCommand(cmd.arguments());
            case CREATE_PLAYLIST_NAME -> this.processCreatePlaylistCommand(cmd.arguments(), selectionKey);
            case DELETE_PLAYLIST_COMMAND_NAME -> this.processDeletePlaylistCommand(cmd.arguments(), selectionKey);
            case ADD_SONG_TO_NAME -> this.processAddSongToCommand(cmd.arguments(), selectionKey);
            case SHOW_PLAYLIST_NAME -> this.processShowPlaylistCommand(cmd.arguments(), selectionKey);
            case PLAY_SONG_NAME -> this.processPlayCommand(cmd.arguments(), selectionKey);
            case STOP_COMMAND_NAME -> this.processStopCommand(selectionKey);
            default -> UNKNOWN_COMMAND_REPLY;
        };

    }

    private String processDisconnectCommand(SelectionKey selectionKey) {

        String result = processLogoutCommand(selectionKey);

        if (!result.equals(ServerReply.SERVER_EXCEPTION.getReply())) {

            return ServerReply.DISCONNECT_COMMAND_SUCCESSFULLY_REPLY.getReply();
        }

        return ServerReply.DISCONNECT_COMMAND_ERROR_REPLY.getReply();
    }

    private String processLogoutCommand(SelectionKey selectionKey) {

        try {

            this.streamingPlatform.logout(selectionKey);
        } catch (UserNotLoggedException e) {

            return getCorrectReply(Level.INFO, ServerReply.LOGOUT_COMMAND_USER_NOT_LOGGED_REPLY.getReply(), e);
        } catch (Exception e) {

            return getCorrectReply(Level.SEVERE, ServerReply.SERVER_EXCEPTION.getReply(), e);
        }

        return ServerReply.LOGOUT_COMMAND_SUCCESSFULLY_REPLY.getReply();
    }

    private String processStopCommand(SelectionKey selectionKey) {

        try {

            this.streamingPlatform.stopSong(selectionKey);
        } catch (UserNotLoggedException e) {

            return getCorrectReply(Level.INFO, ServerReply.STOP_COMMAND_NOT_LOGGED_REPLY.getReply(), e);
        } catch (NoSongPlayingException e) {

            return getCorrectReply(Level.INFO, this.streamingPlatform.getUser().getEmail(),
                ServerReply.STOP_COMMAND_NO_SONG_PLAYING.getReply(), e);
        } catch (Exception e) {

            return getCorrectReply(Level.SEVERE, this.streamingPlatform.getUser().getEmail(),
                ServerReply.STOP_COMMAND_ERROR_REPLY.getReply(), e);
        }

        return ServerReply.STOP_COMMAND_SUCCESSFULLY_REPLY.getReply();
    }

    private String processPlayCommand(List<String> arguments, SelectionKey selectionKey) {

        String songName = arguments.get(0);
        try {

            this.streamingPlatform.playSong(songName, selectionKey);
        } catch (UserNotLoggedException e) {

            return getCorrectReply(Level.INFO, ServerReply.PLAY_SONG_NOT_LOGGED_REPLY.getReply(), e);
        } catch (NoSuchSongException e) {

            return getCorrectReply(Level.INFO, this.streamingPlatform.getUser().getEmail(),
                ServerReply.PLAY_SONG_NO_SUCH_SONG_REPLY.getReply(), e);
        } catch (SongIsAlreadyPlayingException e) {

            return getCorrectReply(Level.INFO, this.streamingPlatform.getUser().getEmail(),
                ServerReply.PLAY_SONG_IS_ALREADY_RUNNING_REPLY.getReply(), e);
        } catch (IODatabaseException e) {

            return getCorrectReply(Level.SEVERE, this.streamingPlatform.getUser().getEmail(),
                ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        } catch (Exception e) {

            return getCorrectReply(Level.SEVERE, this.streamingPlatform.getUser().getEmail(),
                ServerReply.SERVER_EXCEPTION.getReply(), e);
        }

        return ServerReply.PLAY_SONG_SUCCESSFULLY_REPLY.getReply();
    }

    private String generateOutputShowPlaylistCommand(String playlistTitle, Playlist toReturn) {

        StringBuilder resultString = new StringBuilder();
        resultString.append(ServerReply.SHOW_PLAYLIST_SUCCESSFULLY_REPLY.getReply() +
            playlistTitle.toUpperCase() + System.lineSeparator());
        int counter = 1;
        for (Song currentSong : toReturn.getPlaylistSongs()) {

            String toAppend = counter + TITLE_LABEL + currentSong.getTitle() + ARTIST_LABEL +
                currentSong.getArtist() + System.lineSeparator();
            resultString.append(toAppend);
        }

        resultString.deleteCharAt(resultString.length() - 1);
        return resultString.toString();
    }

    private String processShowPlaylistCommand(List<String> arguments, SelectionKey selectionKey) {

        String playlistTitle = arguments.get(0);
        try {

            Playlist toReturn = this.streamingPlatform.showPlaylist(playlistTitle, selectionKey);
            return generateOutputShowPlaylistCommand(playlistTitle, toReturn);
        } catch (UserNotLoggedException e) {

            return getCorrectReply(Level.INFO, ServerReply.SHOW_PLAYLIST_NOT_LOGGED_REPLY.getReply(), e);
        } catch (NoSuchPlaylistException e) {

            return getCorrectReply(Level.INFO, this.streamingPlatform.getUser().getEmail(),
                ServerReply.SHOW_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply(), e);
        } catch (Exception e) {

            return getCorrectReply(Level.SEVERE, this.streamingPlatform.getUser().getEmail(),
                ServerReply.SERVER_EXCEPTION.getReply(), e);
        }
    }

    private String processAddSongToCommand(List<String> arguments, SelectionKey selectionKey) {

        String playlistTitle = arguments.get(0);
        String songTitle = arguments.get(1);

        try {

            this.streamingPlatform.addSongToPlaylist(playlistTitle, songTitle, selectionKey);
        } catch (UserNotLoggedException e) {

            return getCorrectReply(Level.INFO, ServerReply.ADD_SONG_TO_NOT_LOGGED_REPLY.getReply(), e);
        } catch (NoSuchSongException e) {

            return getCorrectReply(Level.INFO, this.streamingPlatform.getUser().getEmail(),
                ServerReply.ADD_SONG_TO_NO_SUCH_SONG_REPLY.getReply(), e);
        } catch (NoSuchPlaylistException e) {

            return getCorrectReply(Level.INFO, this.streamingPlatform.getUser().getEmail(),
                ServerReply.ADD_SONG_TO_NO_SUCH_PLAYLIST_REPLY.getReply(), e);
        } catch (SongAlreadyInPlaylistException e) {

            return getCorrectReply(Level.INFO, this.streamingPlatform.getUser().getEmail(),
                ServerReply.ADD_SONG_TO_SONG_ALREADY_EXIST_REPLY.getReply(), e);
        } catch (IODatabaseException e) {

            return getCorrectReply(Level.SEVERE, this.streamingPlatform.getUser().getEmail(),
                ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        } catch (Exception e) {

            return getCorrectReply(Level.SEVERE, this.streamingPlatform.getUser().getEmail(),
                ServerReply.SERVER_EXCEPTION.getReply(), e);
        }

        return ServerReply.ADD_SONG_TO_SUCCESSFULLY_REPLY.getReply();
    }

    private String processCreatePlaylistCommand(List<String> arguments, SelectionKey selectionKey) {

        String playlistTitle = arguments.get(0);
        try {

            this.streamingPlatform.createPlaylist(playlistTitle, selectionKey);
        } catch (UserNotLoggedException e) {

            return getCorrectReply(Level.INFO, ServerReply.CREATE_PLAYLIST_NOT_LOGGED_REPLY.getReply(), e);
        } catch (IODatabaseException e) {

            return getCorrectReply(Level.SEVERE, this.streamingPlatform.getUser().getEmail(),
                ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        } catch (PlaylistAlreadyExistException e) {

            return getCorrectReply(Level.INFO, this.streamingPlatform.getUser().getEmail(),
                ServerReply.CREATE_PLAYLIST_ALREADY_EXIST_REPLY.getReply(), e);
        } catch (Exception e) {

            return getCorrectReply(Level.SEVERE, this.streamingPlatform.getUser().getEmail(),
                ServerReply.SERVER_EXCEPTION.getReply(), e);
        }

        return ServerReply.CREATE_PLAYLIST_SUCCESSFULLY_REPLY.getReply();
    }

    private String processDeletePlaylistCommand(List<String> arguments, SelectionKey selectionKey) {

        String playlistTitle = arguments.get(0);
        try {

            this.streamingPlatform.deletePlaylist(playlistTitle, selectionKey);
        } catch (UserNotLoggedException e) {

            return getCorrectReply(Level.INFO, ServerReply.DELETE_PLAYLIST_NOT_LOGGED_REPLY.getReply(), e);
        } catch (IODatabaseException e) {

            return getCorrectReply(Level.SEVERE, this.streamingPlatform.getUser().getEmail(),
                ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        } catch (NoSuchPlaylistException e) {

            return getCorrectReply(Level.INFO, this.streamingPlatform.getUser().getEmail(),
                ServerReply.DELETE_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply(), e);
        } catch (PlaylistNotEmptyException e) {

            return getCorrectReply(Level.INFO, this.streamingPlatform.getUser().getEmail(),
                ServerReply.DELETE_PLAYLIST_NOT_EMPTY_PLAYLIST_REPLY.getReply(), e);
        } catch (Exception e) {

            return getCorrectReply(Level.SEVERE, this.streamingPlatform.getUser().getEmail(),
                ServerReply.SERVER_EXCEPTION.getReply(), e);
        }

        return ServerReply.DELETE_PLAYLIST_SUCCESSFULLY_REPLY.getReply();
    }

    private final static String TITLE_LABEL = " Title: ";
    private final static String ARTIST_LABEL = " Artist: ";
    private String processSearchCommand(List<String> arguments) {

        String wordToSearch = arguments.get(0);

        List<SongEntity> searchedSongs = this.streamingPlatform.searchSongs(wordToSearch);

        if (searchedSongs.isEmpty()) {
            return getCorrectReply(Level.INFO, ServerReply.SEARCH_COMMAND_NO_SONGS_REPLY.getReply());
        }

        StringBuilder toReturn = new StringBuilder();
        toReturn.append(ServerReply.SEARCH_COMMAND_SUCCESSFULLY_REPLY.getReply() + System.lineSeparator());
        for (SongEntity currentSongEntity : searchedSongs) {

            String currentResult = TITLE_LABEL + currentSongEntity.getSong().getTitle() +
                ARTIST_LABEL + currentSongEntity.getSong().getArtist() + System.lineSeparator();
            toReturn.append(currentResult);
        }

        return toReturn.toString();
    }

    private String processRegisterCommand(List<String> arguments) {

        String emailToRegister = arguments.get(0);
        String passwordToRegister = arguments.get(1);
        try {
            register(emailToRegister, passwordToRegister);
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

        if (this.streamingPlatform.getAlreadyLogged().contains(selectionKey)) {
            throw new UserAlreadyLoggedException(ServerReply.LOGIN_COMMAND_USER_ALREADY_LOGGED_REPLY.getReply());
        }
    }

    private String getCorrectReply(Level level, String message) {

        SpotifyLogger.log(level, REPLY_FIELD_TO_LOG + message);
        return message;
    }

    private String getCorrectReply(Level level, String message, Exception e) {

        SpotifyLogger.log(level, REPLY_FIELD_TO_LOG + message, e);
        return message;
    }

    private String getCorrectReply(Level level, String email, String message, Exception e) {

        SpotifyLogger.log(level, EMAIL_MESSAGE_TO_LOG + email + " " + REPLY_FIELD_TO_LOG +
            message, e);
        return message;
    }

    private String processLoginCommand(List<String> arguments, SelectionKey selectionKey) {

        String emailToLogin = arguments.get(0);
        String passwordToLogin = arguments.get(1);
        try {
            validateIsLogged(selectionKey);
            User toLog = login(emailToLogin, passwordToLogin);
            this.streamingPlatform.setUser(toLog);
            this.streamingPlatform.getAlreadyLogged().add(selectionKey);
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

    private final static String TIMES_SIGN = "# ";

    private String processTopCommand(List<String> arguments) {

        if (arguments.get(0).equals("0") || !arguments.get(0).matches(POSITIVE_NUMBER_REGEX)) {

            return getCorrectReply(Level.INFO, ServerReply.TOP_COMMAND_INVALID_ARGUMENT_REPLY.getReply());
        }

        List<SongEntity> result = this.streamingPlatform.getTopNMostListenedSongs(
            Integer.parseInt(arguments.get(0)));

        StringBuilder toReturn = new StringBuilder();
        toReturn.append(ServerReply.TOP_COMMAND_SUCCESSFULLY_REPLY.getReply() + System.lineSeparator());
        for (SongEntity currentSongEntity : result) {

            String toAppend = TIMES_SIGN + currentSongEntity.getListeningTimes() + TITLE_LABEL +
                currentSongEntity.getSong().getTitle() + ARTIST_LABEL + currentSongEntity.getSong().getArtist()
                + System.lineSeparator();
            toReturn.append(toAppend);
        }
        toReturn.deleteCharAt(toReturn.length() - 1);
        return toReturn.toString();
    }

    public static void main(String[] args) throws IODatabaseException {
        CommandExecutor ce = new CommandExecutor(new StreamingPlatform());

        Command cm = new Command("register", List.of("sampleEmail@abv.bg", "666777888"));
        //System.out.println(ce.executeCommand(cm), );
    }

}
