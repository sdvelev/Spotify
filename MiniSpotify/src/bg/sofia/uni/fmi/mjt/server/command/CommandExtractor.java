package bg.sofia.uni.fmi.mjt.server.command;

import java.util.ArrayList;
import java.util.List;

public class CommandExtractor {

    private final static String INTERVAL_REGEX = " ";
    private final static String UNKNOWN_COMMAND_STRING = "unknown";



    private static Command createCommandWithTwoArguments(List<String> arguments, String[] lineArray) {

        String commandName = lineArray[0];
        arguments.add(lineArray[1]);

        StringBuilder secondArgument = new StringBuilder();

        for (int i = 2; i < lineArray.length; i++) {

            secondArgument.append(lineArray[i]);
        }

        arguments.add(secondArgument.toString());

        return new Command(commandName.toLowerCase(), arguments);
    }

    private static Command createCommandWithOneArgument(List<String> arguments, String[] lineArray) {

        String commandName = lineArray[0];

        StringBuilder firstArgument = new StringBuilder();

        for (int i = 1; i < lineArray.length; i++) {

            firstArgument.append(lineArray[i]);
        }

        arguments.add(firstArgument.toString());

        return new Command(commandName.toLowerCase(), arguments);
    }

    private static Command createCommandWitNoArguments(List<String> arguments, String[] lineArray) {

        String commandName = lineArray[0];

        return new Command(commandName, arguments);
    }

    public static Command newCommand(String clientInput) {

        List<String> arguments = new ArrayList<>();

        String[] lineArray = clientInput.split(INTERVAL_REGEX);

        if (lineArray[0].equalsIgnoreCase(CommandName.REGISTER_COMMAND.getCommandName()) ||
        lineArray[0].equalsIgnoreCase(CommandName.LOGIN_COMMAND.getCommandName()) ||
        lineArray[0].equalsIgnoreCase(CommandName.ADD_SONG_TO.getCommandName())) {

            return createCommandWithTwoArguments(arguments, lineArray);
        }
        else if (lineArray[0].equalsIgnoreCase(CommandName.SEARCH_COMMAND.getCommandName()) ||
        lineArray[0].equalsIgnoreCase(CommandName.TOP_COMMAND.getCommandName()) ||
        lineArray[0].equalsIgnoreCase(CommandName.CREATE_PLAYLIST.getCommandName()) ||
        lineArray[0].equalsIgnoreCase(CommandName.SHOW_PLAYLIST.getCommandName()) ||
        lineArray[0].equalsIgnoreCase(CommandName.PLAY_SONG.getCommandName())) {

            return createCommandWithOneArgument(arguments, lineArray);
        }
        else if (lineArray[0].equalsIgnoreCase(CommandName.DISCONNECT_COMMAND.getCommandName()) ||
        lineArray[0].equalsIgnoreCase(CommandName.STOP_COMMAND.getCommandName())) {

            return createCommandWitNoArguments(arguments, lineArray);
        }

        return new Command(UNKNOWN_COMMAND_STRING, new ArrayList<>());
    }

}