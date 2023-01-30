package bg.sofia.uni.fmi.mjt.server;

public enum ServerReply {

    REGISTER_COMMAND_SUCCESSFULLY_REPLY("The registration process is successful. Now " +
        "you can log in."),
    REGISTER_COMMAND_ALGORITHM_REPLY("The registration process is unsuccessful " +
        "as there's a problem in the hashing algorithm. Please, try again later."),
    REGISTER_COMMAND_ALREADY_EXIST_REPLY("The registration process is unsuccessful " +
        "as the email is already registered."),
    REGISTER_COMMAND_INVALID_EMAIL_REPLY("The registration process is " +
        "unsuccessful as the provided email is not valid. Please, try to enter it again."),
    IO_DATABASE_PROBLEM_REPLY("Database is temporarily unavailable. Please, try again later."),
    SERVER_EXCEPTION("Something went wrong. Please, try again."),
    LOGIN_COMMAND_SUCCESSFULLY_REPLY("The login process is successful. " +
        "Now you are logged in."),
    LOGIN_COMMAND_USER_NOT_EXIST_REPLY("The login process is unsuccessful as " +
        "there's not such a profile registered."),
    LOGIN_COMMAND_ALGORITHM_REPLY("The login process is unsuccessful as there is a " +
        "problem in the hashing algorithm."),
    LOGIN_COMMAND_USER_ALREADY_LOGGED("You cannot login as there is a user associated with the current session. " +
        "Please, log out first."),
    LOGIN_COMMAND("login"),
    DISCONNECT_COMMAND("disconnect"),
    SEARCH_COMMAND("search"),
    TOP_COMMAND("top"),
    CREATE_PLAYLIST("create-playlist"),
    ADD_SONG_TO("add-song-to"),
    SHOW_PLAYLIST("show-playlist"),
    PLAY_SONG("play"),
    STOP_COMMAND("stop");

    private final String reply;

    ServerReply(String reply) {
        this.reply = reply;
    }

    public String getReply() {

        return this.reply;
    }

}
