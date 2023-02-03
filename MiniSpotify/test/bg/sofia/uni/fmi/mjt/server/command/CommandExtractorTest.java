package bg.sofia.uni.fmi.mjt.server.command;

import org.junit.jupiter.api.Test;

import static bg.sofia.uni.fmi.mjt.server.command.CommandExtractor.newCommand;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandExtractorTest {


    @Test
    void testNewCommandRegisterCommandSuccessfully() {

        String clientInput = "register sdvelev@outlook.com 000000";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("register"),
            "Actual command name is not the same as the expected.");
        assertEquals("sdvelev@outlook.com", returnedCommand.arguments().get(0),
            "Actual command email argument is not the same as the expected.");
        assertEquals("000000", returnedCommand.arguments().get(1),
            "Actual command password argument is not the same as the expected");
    }

    @Test
    void testNewCommandRegisterCommandOneArgument() {

        String clientInput = "register sdvelev@outlook.com";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandRegisterCommandThreeArguments() {

        String clientInput = "register sdvelev@outlook.com 000000 6";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandRegisterCommandNoArguments() {

        String clientInput = "register";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandLoginCommandSuccessfully() {

        String clientInput = "login sdvelev@gmail.com 123456";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("login"),
            "Actual command name is not the same as the expected.");
        assertEquals("sdvelev@gmail.com", returnedCommand.arguments().get(0),
            "Actual command email argument is not the same as the expected.");
        assertEquals("123456", returnedCommand.arguments().get(1),
            "Actual command password argument is not the same as the expected");
    }

    @Test
    void testNewCommandLoginCommandSuccessfullyIgnoreCase() {

        String clientInput = "LoGiN sdvelev@gmail.com 123456";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("login"),
            "Actual command name is not the same as the expected.");
        assertEquals("sdvelev@gmail.com", returnedCommand.arguments().get(0),
            "Actual command email argument is not the same as the expected.");
        assertEquals("123456", returnedCommand.arguments().get(1),
            "Actual command password argument is not the same as the expected");
    }

    @Test
    void testNewCommandLoginCommandOneArgument() {

        String clientInput = "login sdvelev@outlook.com";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandLoginCommandThreeArguments() {

        String clientInput = "login sdvelev@gmail.com 123456 6";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandLoginCommandNoArguments() {

        String clientInput = "login  ";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandAddSongToCommandSuccessfully() {

        String clientInput = "add-song-to MyPlaylist The Crown - Main title";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("add-song-to"),
            "Actual command name is not the same as the expected.");
        assertEquals("MyPlaylist", returnedCommand.arguments().get(0),
            "Actual command playlist argument is not the same as the expected.");
        assertEquals("The Crown - Main title", returnedCommand.arguments().get(1),
            "Actual command song argument is not the same as the expected");
    }

    @Test
    void testNewCommandAddSongToCommandOneArgument() {

        String clientInput = "add-song-to MyPlaylist";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandAddSongToCommandNoArguments() {

        String clientInput = "add-song-to";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandRemoveSongFromCommandSuccessfully() {

        String clientInput = "remove-song-from MyPlaylist The Crown - Main title";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("remove-song-from"),
            "Actual command name is not the same as the expected.");
        assertEquals("MyPlaylist", returnedCommand.arguments().get(0),
            "Actual command playlist argument is not the same as the expected.");
        assertEquals("The Crown - Main title", returnedCommand.arguments().get(1),
            "Actual command song argument is not the same as the expected");
    }

    @Test
    void testNewCommandRemoveSongFromCommandOneArgument() {

        String clientInput = "remove-song-from MyPlaylist";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandRemoveSongFromCommandNoArguments() {

        String clientInput = "remove-song-from";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandSearchCommandSuccessfullyTwoWords() {

        String clientInput = "search the crown";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("search"),
            "Actual command name is not the same as the expected.");
        assertEquals("the crown", returnedCommand.arguments().get(0),
            "Actual command keywords argument is not the same as the expected.");
    }

    @Test
    void testNewCommandSearchCommandSuccessfullyOneWord() {

        String clientInput = "search crown";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("search"),
            "Actual command name is not the same as the expected.");
        assertEquals("crown", returnedCommand.arguments().get(0),
            "Actual command keyword argument is not the same as the expected.");
    }

    @Test
    void testNewCommandSearchCommandNoArguments() {

        String clientInput = "search";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandPlayCommandSuccessfully() {

        String clientInput = "play Vivaldi Variation";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("play"),
            "Actual command name is not the same as the expected.");
        assertEquals("Vivaldi Variation", returnedCommand.arguments().get(0),
            "Actual command song title argument is not the same as the expected.");
    }

    @Test
    void testNewCommandPlayCommandNoArguments() {

        String clientInput = "play";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandTopCommandSuccessfully() {

        String clientInput = "top 6";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("top"),
            "Actual command name is not the same as the expected.");
        assertEquals("6", returnedCommand.arguments().get(0),
            "Actual command number argument is not the same as the expected.");
    }

    @Test
    void testNewCommandTopCommandTwoArguments() {

        String clientInput = "top 6 6";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandTopCommandNoArguments() {

        String clientInput = "top";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandCreatePlaylistCommandSuccessfully() {

        String clientInput = "create-playlist FilmMusic";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("create-playlist"),
            "Actual command name is not the same as the expected.");
        assertEquals("FilmMusic", returnedCommand.arguments().get(0),
            "Actual command playlist title argument is not the same as the expected.");
    }

    @Test
    void testNewCommandCreatePlaylistCommandTwoArguments() {

        String clientInput = "create-playlist Film Music";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandCreatePlaylistCommandNoArguments() {

        String clientInput = "create-playlist";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandDeletePlaylistCommandSuccessfully() {

        String clientInput = "delete-playlist FilmMusic";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("delete-playlist"),
            "Actual command name is not the same as the expected.");
        assertEquals("FilmMusic", returnedCommand.arguments().get(0),
            "Actual command playlist title argument is not the same as the expected.");
    }

    @Test
    void testNewCommandDeletePlaylistCommandTwoArguments() {

        String clientInput = "delete-playlist Film Music";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandDeletePlaylistCommandNoArguments() {

        String clientInput = "delete-playlist";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandShowPlaylistCommandSuccessfully() {

        String clientInput = "show-playlist FilmMusic";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("show-playlist"),
            "Actual command name is not the same as the expected.");
        assertEquals("FilmMusic", returnedCommand.arguments().get(0),
            "Actual command playlist title argument is not the same as the expected.");
    }

    @Test
    void testNewCommandShowPlaylistCommandTwoArguments() {

        String clientInput = "show-playlist Film Music";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandShowPlaylistCommandNoArguments() {

        String clientInput = "show-playlist";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandPlayPlaylistCommandSuccessfully() {

        String clientInput = "play-playlist FilmMusic";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("play-playlist"),
            "Actual command name is not the same as the expected.");
        assertEquals("FilmMusic", returnedCommand.arguments().get(0),
            "Actual command playlist title argument is not the same as the expected.");
    }

    @Test
    void testNewCommandPlayPlaylistCommandTwoArguments() {

        String clientInput = "play-playlist Film Music";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandPlayPlaylistCommandNoArguments() {

        String clientInput = "play-playlist";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandDisconnectCommandSuccessfully() {

        String clientInput = "disconnect";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("disconnect"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command arguments must be empty but actually they aren't.");
    }

    @Test
    void testNewCommandDisconnectCommandOneArgument() {

        String clientInput = "disconnect sdvelev@gmail.com";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandDisconnectCommandTwoArguments() {

        String clientInput = "disconnect sdvelev@gmail.com 123456";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandLogoutCommandSuccessfully() {

        String clientInput = "logout";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("logout"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command arguments must be empty but actually they aren't.");
    }

    @Test
    void testNewCommandLogoutCommandOneArgument() {

        String clientInput = "logout sdvelev@gmail.com";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandLogoutCommandTwoArguments() {

        String clientInput = "logout sdvelev@gmail.com 123456";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandStopCommandSuccessfully() {

        String clientInput = "stop";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("stop"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command arguments must be empty but actually they aren't.");
    }

    @Test
    void testNewCommandStopCommandOneArgument() {

        String clientInput = "stop No Time To Die";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandStopCommandTwoArguments() {

        String clientInput = "stop No Time To Die Billie Eilish";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandShowPlaylistsCommandSuccessfully() {

        String clientInput = "show-playlists";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("show-playlists"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command arguments must be empty but actually they aren't.");
    }

    @Test
    void testNewCommandShowPlaylistsCommandOneArgument() {

        String clientInput = "show-playlists sdvelev@gmail.com";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandShowPlaylistsCommandTwoArguments() {

        String clientInput = "show-playlists sdvelev@gmail.com 123456";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandHelpCommandSuccessfully() {

        String clientInput = "help";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("help"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command arguments must be empty but actually they aren't.");
    }

    @Test
    void testNewCommandHelpCommandOneArgument() {

        String clientInput = "help sdvelev@gmail.com";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandHelpCommandTwoArguments() {

        String clientInput = "help sdvelev@gmail.com 123456";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandNoCommand() {

        String clientInput = "";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name is not the same as the expected.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't");
    }

    @Test
    void testNewCommandNotExistingCommand() {

        String clientInput = "remove-playlist MyPlaylist";

        Command returnedCommand = newCommand(clientInput);

        assertTrue(returnedCommand.command().equalsIgnoreCase("unknown"),
            "Actual command name doesn't exist and unknown command is expected to be returned.");
        assertTrue(returnedCommand.arguments().isEmpty(),
            "Command argument must be empty as it is in wrong format but actually it isn't empty");
    }

}
