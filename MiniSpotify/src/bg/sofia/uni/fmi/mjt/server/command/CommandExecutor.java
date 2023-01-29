package bg.sofia.uni.fmi.mjt.server.command;

import bg.sofia.uni.fmi.mjt.server.StreamingPlatform;
import bg.sofia.uni.fmi.mjt.server.exceptions.EmailAlreadyRegisteredException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoSuchPlaylistException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoSuchSongException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NotValidEmailFormatException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotLoggedException;
import bg.sofia.uni.fmi.mjt.server.logger.SpotifyLogger;
import bg.sofia.uni.fmi.mjt.server.login.User;
import bg.sofia.uni.fmi.mjt.server.storage.Playlist;
import bg.sofia.uni.fmi.mjt.server.storage.Song;
import bg.sofia.uni.fmi.mjt.server.storage.SongEntity;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;

import static bg.sofia.uni.fmi.mjt.server.login.Authentication.login;
import static bg.sofia.uni.fmi.mjt.server.login.Authentication.register;

public class CommandExecutor {

    private final static String REGISTER_COMMAND_NAME = "register";
    private final static String REGISTER_COMMAND_SUCCESSFULLY_REPLY = "The registration process is successful.";
    private final static String REGISTER_COMMAND_ALREADY_EXIST_REPLY = "The registration process is not successful " +
        "as there is already a registration with such email.";
    private final static String REGISTER_COMMAND_ALGORITHM_REPLY = "The registration process is not successful " +
        "as there is a problem in the hashing algorithm.";
    private final static String REGISTER_COMMAND_INVALID_EMAIL_REPLY = "The registration process is not " +
        "successful as the provided email is not valid.";

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

    private final static String PLAY_SONG = "play-song";
    private final static String STOP_COMMAND = "stop";

    private final static String UNKNOWN_COMMAND_REPLY = "The inserted command is not in the right format. " +
        "Please, try again.";


    private StreamingPlatform streamingPlatform;

    public CommandExecutor(StreamingPlatform streamingPlatform) {
        this.streamingPlatform = streamingPlatform;
    }

    public String executeCommand(Command cmd) {

        return switch(cmd.command()) {

            case REGISTER_COMMAND_NAME -> this.processRegisterCommand(cmd.arguments());
            case LOGIN_COMMAND_NAME -> this.processLoginCommand(cmd.arguments());
           // case DISCONNECT_COMMAND_NAME -> DISCONNECT_COMMAND_REPLY;
            case SEARCH_COMMAND_NAME -> this.processSearchCommand(cmd.arguments());
            case TOP_COMMAND_NAME -> this.processTopCommand(cmd.arguments());
            case CREATE_PLAYLIST_NAME -> this.processCreatePlaylistCommand(cmd.arguments());
            case ADD_SONG_TO_NAME -> this.processAddSongToCommand(cmd.arguments());
            case SHOW_PLAYLIST_NAME -> this.processShowPlaylistCommand(cmd.arguments());
            default -> UNKNOWN_COMMAND_REPLY;
        };

    }

    private String processShowPlaylistCommand(List<String> arguments) {

        String playlistTitle = arguments.get(0);

        try {

            Playlist toReturn = this.streamingPlatform.showPlaylist(playlistTitle);
            if (toReturn == null) {
                return "Something went wrong.";
            }

            StringBuilder resultString = new StringBuilder();
            int counter = 1;
            for (Song currentSong : toReturn.getPlaylistSongs()) {

                String toAppend = counter + ". Title: " + currentSong.getTitle() + " Artist: " +
                    currentSong.getArtist() + System.lineSeparator();
                resultString.append(toAppend);
            }
            return resultString.toString();

        } catch (UserNotLoggedException e) {
            SpotifyLogger.log(Level.SEVERE, SHOW_PLAYLIST_NOT_LOGGED_REPLY, e);
            return SHOW_PLAYLIST_NOT_LOGGED_REPLY;
        } catch (NoSuchPlaylistException e) {
            SpotifyLogger.log(Level.SEVERE, "User: " + this.streamingPlatform.getUser().getEmail() + " " +
                SHOW_PLAYLIST_NO_SUCH_PLAYLIST_REPLY, e);
            return SHOW_PLAYLIST_NO_SUCH_PLAYLIST_REPLY;
        }
    }

    private String processAddSongToCommand(List<String> arguments) {

        String playlistTitle = arguments.get(0);
        String songTitle = arguments.get(1);

        try {

            this.streamingPlatform.addSongToPlaylist(playlistTitle, songTitle);
        } catch (UserNotLoggedException e) {
            SpotifyLogger.log(Level.SEVERE, ADD_SONG_TO_NOT_LOGGED_REPLY, e);
            return ADD_SONG_TO_NOT_LOGGED_REPLY;
        } catch (NoSuchSongException e) {
            SpotifyLogger.log(Level.SEVERE, "User: " + this.streamingPlatform.getUser().getEmail() + " " +
                ADD_SONG_TO_NO_SUCH_SONG_REPLY, e);
            return ADD_SONG_TO_NO_SUCH_SONG_REPLY;
        } catch (NoSuchPlaylistException e) {
            SpotifyLogger.log(Level.SEVERE, "User: " + this.streamingPlatform.getUser().getEmail() + " " +
                ADD_SONG_TO_NO_SUCH_PLAYLIST_REPLY, e);
            return ADD_SONG_TO_NO_SUCH_PLAYLIST_REPLY;
        }

        return ADD_SONG_TO_SUCCESSFULLY_REPLY;
    }

    private String processCreatePlaylistCommand(List<String> arguments) {

        String title = arguments.get(0);

        try {

            this.streamingPlatform.createPlaylist(title);
        } catch (UserNotLoggedException e) {
            SpotifyLogger.log(Level.SEVERE, CREATE_PLAYLIST_NOT_LOGGED_REPLY, e);
            return CREATE_PLAYLIST_NOT_LOGGED_REPLY;
        }

        return CREATE_PLAYLIST_SUCCESSFULLY_REPLY;
    }
    private String processSearchCommand(List<String> arguments) {

        String toSearch = arguments.get(0);

        List<SongEntity> searchedSongs = this.streamingPlatform.searchSongs(toSearch);

        StringBuilder toReturn = new StringBuilder();

        for (SongEntity currentSongEntity : searchedSongs) {

            String currentResult = "Title: " + currentSongEntity.getSong().getTitle() + " Artist: " +
                currentSongEntity.getSong().getArtist() + System.lineSeparator();
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
            SpotifyLogger.log(Level.SEVERE, REGISTER_COMMAND_ALGORITHM_REPLY, e);
            return REGISTER_COMMAND_ALGORITHM_REPLY;
        } catch (NotValidEmailFormatException e) {
            SpotifyLogger.log(Level.SEVERE, REGISTER_COMMAND_INVALID_EMAIL_REPLY, e);
            return REGISTER_COMMAND_INVALID_EMAIL_REPLY;
        } catch (EmailAlreadyRegisteredException e) {
            SpotifyLogger.log(Level.SEVERE, REGISTER_COMMAND_ALREADY_EXIST_REPLY, e);
            return REGISTER_COMMAND_ALREADY_EXIST_REPLY;
        }

        return REGISTER_COMMAND_SUCCESSFULLY_REPLY;
    }

    private String processLoginCommand(List<String> arguments) {

        String emailToLogin = arguments.get(0);
        String passwordToLogin = arguments.get(1);

        try {
            User toLog = login(emailToLogin, passwordToLogin);
            this.streamingPlatform.setUser(toLog);
            this.streamingPlatform.setIsLogged(true);
        } catch (UserNotFoundException e) {
            SpotifyLogger.log(Level.SEVERE, "email: " + emailToLogin + " password: " + passwordToLogin + " " +
                LOGIN_COMMAND_USER_NOT_EXIST_REPLY, e);
            return LOGIN_COMMAND_USER_NOT_EXIST_REPLY;
        } catch (NoSuchAlgorithmException e) {
            SpotifyLogger.log(Level.SEVERE, LOGIN_COMMAND_ALGORITHM_REPLY, e);
            return LOGIN_COMMAND_ALGORITHM_REPLY;
        }

        return LOGIN_COMMAND_SUCCESSFULLY_REPLY;
    }

    private final static String POSITIVE_NUMBER_REGEX = "^[0-9]+$";

    private String processTopCommand(List<String> arguments) {

        if (arguments.get(0).equals("0") || !arguments.get(0).matches(POSITIVE_NUMBER_REGEX)) {

            SpotifyLogger.log(Level.SEVERE, TOP_COMMAND_INVALID_ARGUMENT_REPLY);
            return TOP_COMMAND_INVALID_ARGUMENT_REPLY;
        }

        List<SongEntity> result = this.streamingPlatform.getTopNMostListenedSongs(
            Integer.parseInt(arguments.get(0)));

        StringBuilder toReturn = new StringBuilder();

        for (SongEntity currentSongEntity : result) {

            String toAppend = "# " + currentSongEntity.getListeningTimes() + ". Title: " +
                currentSongEntity.getSong().getTitle() + ". Artist: " + currentSongEntity.getSong().getArtist()
                + System.lineSeparator();
            toReturn.append(toAppend);
        }
        return toReturn.toString();
    }

    public static void main(String[] args) {
        CommandExecutor ce = new CommandExecutor(new StreamingPlatform());

        Command cm = new Command("register", List.of("sampleEmail@abv.bg", "666777888"));
        System.out.println(ce.executeCommand(cm));
    }

}
