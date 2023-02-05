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
    CREATE_PLAYLIST_NOT_LOGGED_REPLY("The playlist was not created successfully as you aren't logged in. " +
        "Please, try to log in first."),
    CREATE_PLAYLIST_ALREADY_EXIST_REPLY("The playlist was not created as there's already a playlist with the same " +
        "name associated with this profile. Please, try again with different title."),
    DELETE_PLAYLIST_SUCCESSFULLY_REPLY("The playlist was deleted successfully."),
    DELETE_PLAYLIST_NOT_LOGGED_REPLY("The playlist was not deleted successfully as you aren't logged in. " +
        "Please, try to log in first."),
    DELETE_PLAYLIST_NO_SUCH_PLAYLIST_REPLY("Playlist with such a title couldn't be deleted as it was not found. " +
        "Please, try again with different title."),
    DELETE_PLAYLIST_NOT_EMPTY_PLAYLIST_REPLY("The playlist was not deleted successfully as it contains songs. " +
        "Please, remove them from playlist first so as to delete it."),
    ADD_SONG_TO_NOT_LOGGED_REPLY("You cannot add song to playlists as you aren't logged in. " +
        "Please, try to log in first."),
    ADD_SONG_TO_NO_SUCH_SONG_REPLY("Song with such a title was not found in our platform. " +
        "Please, try again with different title."),
    ADD_SONG_TO_NO_SUCH_PLAYLIST_REPLY("Playlist with such a title was not found associated with your profile. " +
        "Please, try again with different title."),
    ADD_SONG_TO_SONG_ALREADY_EXIST_REPLY("The song is already present in that playlist."),
    ADD_SONG_TO_SUCCESSFULLY_REPLY("The song was added successfully to the desired playlist."),
    REMOVE_SONG_FROM_SUCCESSFULLY_REPLY("The song was removed from the playlist successfully"),
    REMOVE_SONG_FROM_NOT_LOGGED_REPLY("You cannot remove song from playlist as you aren't logged in. " +
        "Please, try to log in first."),
    REMOVE_SONG_FROM_NO_SUCH_PLAYLIST_REPLY("Playlist with such a name was not found in your profile. " +
        "Please, try again with different title."),
    REMOVE_SONG_FROM_NO_SUCH_SONG_REPLY("Song with such a title was not found in the provided playlist. " +
        "Please, try again with different title."),
    SHOW_PLAYLIST_SUCCESSFULLY_REPLY("Content of playlist "),
    SHOW_PLAYLIST_NOT_LOGGED_REPLY("You cannot view playlist as you aren't logged in. " +
        "Please, try to log in first."),
    SHOW_PLAYLIST_NO_SUCH_PLAYLIST_REPLY("There isn't such a playlist associated with this account."),
    SHOW_PLAYLIST_NO_SONGS_REPLY("The playlist is empty."),
    SHOW_PLAYLISTS_SUCCESSFULLY_REPLY("The playlists associated with this profile are:"),
    SHOW_PLAYLISTS_NOT_LOGGED_REPLY("You cannot view playlists as you aren't logged in. " +
        "Please, try to log in first."),
    SHOW_PLAYLISTS_NO_PLAYLISTS_REPLY("We couldn't find any playlists associated with your profile."),
    PLAY_SONG_NOT_LOGGED_REPLY("You cannot play songs unless you are logged in."),
    PLAY_SONG_NO_SUCH_SONG_REPLY("There isn't such a song in the platform."),
    PLAY_SONG_SUCCESSFULLY_REPLY("The song is starting."),
    PLAY_SONG_IS_ALREADY_RUNNING_REPLY("Song has already been started and is now " +
        "playing. You can stop it with the relevant command or wait for it to finish."),
    STOP_COMMAND_SUCCESSFULLY_REPLY("The song was stopped successfully"),
    STOP_COMMAND_NOT_LOGGED_REPLY("You cannot stop a song as you are not logged in."),
    STOP_COMMAND_NO_SONG_PLAYING("There isn't a song which is playing at the moment."),
    STOP_COMMAND_ERROR_REPLY("Something went wrong with streaming the song."),
    PLAY_PLAYLIST_NOT_LOGGED_REPLY("You cannot play playlist unless you log in first."),
    PLAY_PLAYLIST_SUCCESSFULLY_REPLY("The playlist is starting."),
    PLAY_PLAYLIST_NO_SUCH_PLAYLIST_REPLY("There isn't such a playlist to play associated with this account."),
    PLAY_PLAYLIST_NO_SONGS_IN_PLAYLIST_REPLY("There aren't any songs to play in the given playlist."),
    PLAY_PLAYLIST_ALREADY_PLAYING("Song is already playing at the moment. Please, stop it and try again."),
    HELP_COMMAND_REPLY("Commands (in lexicographic order):" + System.lineSeparator() +
        "add-song-to <name_of_the_playlist> <title_of_the_song> : Add <song> to <playlist>" + System.lineSeparator() +
        "create-playlist <name_of_the_playlist> : Create <playlist>. The title of the playlist must be one-word " +
        "and is case-sensitive" + System.lineSeparator() +
        "delete-playlist <name_of_the_playlist> : Delete <playlist>. The titles of the playlists are case-sensitive" +
        System.lineSeparator() +
        "disconnect : Disconnect from Spotify" + System.lineSeparator() +
        "help : List the current info" + System.lineSeparator() +
        "login <email> <password> : Log in Spotify" + System.lineSeparator() +
        "logout : Log out of Spotify" + System.lineSeparator() +
        "play <title_of_the_song> : Start playing the <song>" + System.lineSeparator() +
        "play-playlist <name_of_the_playlist> : Start playing all the songs of the <playlist> one after another. " +
        "The title of the playlists are case-sensitive. If you want to stop or skip a song, you can stop it with " +
        "the relevant command" + System.lineSeparator() +
        "register <email> <password> : Registration in Spotify" + System.lineSeparator() +
        "remove-song-from <name_of_the_playlist> <title_of_the_song> : Remove <song> from <playlist>. The titles of " +
        "the playlist are case-sensitive." + System.lineSeparator() +
        "search <words> : Retrieve all songs whose title or artist contain all the words" + System.lineSeparator() +
        "show-playlist <name_of_the_playlist> : List the content of the <playlist>. The titles of the  playlists are " +
        "case-sensitive." + System.lineSeparator() +
        "show-playlists : List the titles of all created playlists" + System.lineSeparator() +
        "stop : Stop playing the current song" + System.lineSeparator() +
        "top <number> : Retrieve list of the top <number> most listened songs"),
    UNKNOWN_COMMAND_REPLY("The inserted command is not correct or in the right " +
        "format. Please, try to enter it again or refer to the <help> command for more info."),
    LOGIN_COMMAND("login");

    private final String reply;

    ServerReply(String reply) {
        this.reply = reply;
    }

    public String getReply() {

        return reply;
    }
}
