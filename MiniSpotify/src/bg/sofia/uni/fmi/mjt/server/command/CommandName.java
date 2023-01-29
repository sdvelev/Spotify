package bg.sofia.uni.fmi.mjt.server.command;

public enum CommandName {

    REGISTER_COMMAND("register"),
    LOGIN_COMMAND("login"),
    DISCONNECT_COMMAND("disconnect"),
    SEARCH_COMMAND("search"),
    TOP_COMMAND("top"),
    CREATE_PLAYLIST("create-playlist"),
    ADD_SONG_TO("add-song-to"),
    SHOW_PLAYLIST("show-playlist"),
    PLAY_SONG("play"),
    STOP_COMMAND("stop");

    private final String commandName;

    CommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandName() {

        return this.commandName;
    }
}
