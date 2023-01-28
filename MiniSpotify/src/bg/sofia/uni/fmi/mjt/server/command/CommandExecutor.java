package bg.sofia.uni.fmi.mjt.server.command;

import bg.sofia.uni.fmi.mjt.server.StreamingPlatform;
import bg.sofia.uni.fmi.mjt.server.exceptions.EmailAlreadyRegisteredException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NotValidEmailFormatException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.server.login.User;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import static bg.sofia.uni.fmi.mjt.server.login.Authentication.login;
import static bg.sofia.uni.fmi.mjt.server.login.Authentication.register;

public class CommandExecutor {

    private final static String REGISTER_COMMAND_NAME = "register";
    private final static String REGISTER_COMMAND_SUCCESSFULLY_REPLY = "The registration process is successful.";
    private final static String REGISTER_COMMAND_ALREADY_EXIST_REPLY = "The registration process is not successful as there is already a registration with such email.";
    private final static String REGISTER_COMMAND_ALGORITHM_REPLY = "The registration process is not successful as there is a problem in the hashing algorithm.";
    private final static String REGISTER_COMMAND_INVALID_EMAIL = "The registration process is not successful as the provided email is not valid.";

    private final static String LOGIN_COMMAND_NAME = "login";
    private final static String LOGIN_COMMAND_SUCCESSFULLY_REPLY = "The login process is successful. Now you are logged-in.";
    private final static String LOGIN_COMMAND_USER_NOT_EXIST_REPLY = "The login process is not successful as there is not such a profile.";
    private final static String LOGIN_COMMAND_ALGORITHM_REPLY = "The login process is not successful as there is a problem in the hashing algorithm.";
    private final static String DISCONNECT_COMMAND = "disconnect";
    private final static String SEARCH_COMMAND = "search";
    private final static String TOP_COMMAND = "top";
    private final static String CREATE_PLAYLIST = "create-playlist";
    private final static String ADD_SONG_TO = "add-song-to";
    private final static String SHOW_PLAYLIST = "show-playlist";
    private final static String PLAY_SONG = "play-song";
    private final static String STOP_COMMAND = "stop";

    private final static String UNKNOWN_COMMAND_REPLY = "The inserted command is not in the right format. Please, try again.";


    private StreamingPlatform streamingPlatform;

    public CommandExecutor(StreamingPlatform streamingPlatform) {
        this.streamingPlatform = streamingPlatform;
    }

    public String executeCommand(Command cmd) {

        return switch(cmd.command()) {

            case REGISTER_COMMAND_NAME -> this.processRegisterCommand(cmd.arguments());
            case LOGIN_COMMAND_NAME -> this.processLoginCommand(cmd.arguments());
            default -> UNKNOWN_COMMAND_REPLY;
        };

    }

    private String processRegisterCommand(List<String> arguments) {

        String emailToRegister = arguments.get(0);
        String passwordToRegister = arguments.get(1);

        try {
            register(emailToRegister, passwordToRegister);
        } catch(NoSuchAlgorithmException e) {

            return REGISTER_COMMAND_ALGORITHM_REPLY;
        } catch (NotValidEmailFormatException e) {

            return REGISTER_COMMAND_INVALID_EMAIL;
        } catch(EmailAlreadyRegisteredException e) {

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

            return LOGIN_COMMAND_USER_NOT_EXIST_REPLY;
        } catch (NoSuchAlgorithmException e) {

            return LOGIN_COMMAND_ALGORITHM_REPLY;
        }

        return LOGIN_COMMAND_SUCCESSFULLY_REPLY;
    }
}
