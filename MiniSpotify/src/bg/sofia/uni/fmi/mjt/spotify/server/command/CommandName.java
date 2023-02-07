package bg.sofia.uni.fmi.mjt.spotify.server.command;

public enum CommandName {

    REGISTER_COMMAND("register"),
    LOGIN_COMMAND("login"),
    LOGOUT_COMMAND("logout"),
    DISCONNECT_COMMAND("disconnect"),
    SEARCH_COMMAND("search"),
    TOP_COMMAND("top"),
    CREATE_PLAYLIST("create-playlist"),
    DELETE_PLAYLIST("delete-playlist"),
    ADD_SONG_TO("add-song-to"),
    REMOVE_SONG_FROM("remove-song-from"),
    SHOW_PLAYLIST("show-playlist"),
    SHOW_PLAYLISTS("show-playlists"),
    PLAY_SONG("play"),
    PLAY_PLAYLIST("play-playlist"),
    STOP_COMMAND("stop"),
    HELP_COMMAND("help");

    private final String commandName;

    CommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }
}
