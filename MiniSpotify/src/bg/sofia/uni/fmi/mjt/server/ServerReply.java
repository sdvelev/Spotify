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
    LOGIN_COMMAND_USER_ALREADY_LOGGED_REPLY("You cannot log in as there is a user associated with the " +
        "current session. Please, log out first."),
    LOGOUT_COMMAND_USER_NOT_LOGGED_REPLY("You cannot log out as there's not a logged in user associated with " +
        "the current session. Please, log in first."),
    LOGOUT_COMMAND_SUCCESSFULLY_REPLY("The log out process is successful. Now you are logged out."),
    DISCONNECT_COMMAND_SUCCESSFULLY_REPLY("You've successfully disconnected from the server."),
    DISCONNECT_COMMAND_ERROR_REPLY("There was a problem in disconnecting from the server. Please, try again later."),
    SEARCH_COMMAND_SUCCESSFULLY_REPLY("We could find the following results:"),
    SEARCH_COMMAND_NO_SONGS_REPLY("We couldn't find any songs related to your search."),
    TOP_COMMAND_SUCCESSFULLY_REPLY("We could find the following results:"),
    TOP_COMMAND_INVALID_ARGUMENT_REPLY("We couldn't process the command as the provided argument is invalid. " +
        "Please, try with whole positive number."),
    CREATE_PLAYLIST_SUCCESSFULLY_REPLY("The playlist was created successfully."),
    CREATE_PLAYLIST_NOT_LOGGED_REPLY("The playlist was not created successfully as you are not logged in. " +
        "Please, try to log in first."),
    CREATE_PLAYLIST_ALREADY_EXIST_REPLY("The playlist was not created as there's already a playlist with the same " +
        "name associated with this profile. Please, try again with different title."),
    ADD_SONG_TO_NOT_LOGGED_REPLY("You cannot add song to playlists as you aren't logged in. " +
        "Please, try to log in first."),
    ADD_SONG_TO_NO_SUCH_SONG_REPLY("Song with such a title was not found in our platform. " +
        "Please, try again with different title."),
    ADD_SONG_TO_NO_SUCH_PLAYLIST_REPLY("Playlist with such a title was not found associated with your profile. " +
        "Please, try again with different title."),
    ADD_SONG_TO_SONG_ALREADY_EXIST_REPLY("The song is already present in that playlist."),
    ADD_SONG_TO_SUCCESSFULLY_REPLY("The song was added successfully to the desired playlist."),
    SHOW_PLAYLIST_SUCCESSFULLY_REPLY("Content of playlist "),
    SHOW_PLAYLIST_NOT_LOGGED_REPLY("You cannot view playlist as you aren't logged in. " +
        "Please, try to log in first."),
    SHOW_PLAYLIST_NO_SUCH_PLAYLIST_REPLY("There isn't such a playlist associated with this account."),
    PLAY_SONG_NOT_LOGGED_REPLY("You cannot play songs unless you are logged in."),
    PLAY_SONG_NO_SUCH_SONG_REPLY("There isn't such a song in the platform."),
    PLAY_SONG_SUCCESSFULLY_REPLY("The song is starting."),
    PLAY_SONG_IS_ALREADY_RUNNING_REPLY("Song has already been started and is now " +
        "playing. You can stop it with the relevant command or wait for it to finish."),
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
