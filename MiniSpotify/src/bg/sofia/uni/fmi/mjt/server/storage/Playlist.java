package bg.sofia.uni.fmi.mjt.server.storage;

import java.util.HashSet;
import java.util.Set;

public class Playlist {

    private String emailCreator;
    private String title;
    private Set<Song> playlistSongs;

    public Playlist(String emailCreator, String title) {
        this.emailCreator = emailCreator;
        this.title = title;
        this.playlistSongs = new HashSet<>();
    }

    public void addSongToPlaylist(Song toAdd) {

        this.playlistSongs.add(toAdd);
    }

    public String getEmailCreator() {

        return this.emailCreator;
    }


}
