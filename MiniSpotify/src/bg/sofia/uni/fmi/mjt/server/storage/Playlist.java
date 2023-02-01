package bg.sofia.uni.fmi.mjt.server.storage;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Playlist {

    private String emailCreator;
    private String title;
    private Set<Song> playlistSongs;

    public Playlist() {
        this.playlistSongs = new HashSet<>();
    }
    public Playlist(String emailCreator, String title) {
        this.emailCreator = emailCreator;
        this.title = title;
        this.playlistSongs = new HashSet<>();
    }

    public void addSong(Song toAdd) {

        this.playlistSongs.add(toAdd);
    }

    public void removeSong(Song toRemove) {

        this.playlistSongs.remove(toRemove);
    }

    public boolean containsSong(Song toCheck) {

        return this.playlistSongs.contains(toCheck);
    }

    public String getEmailCreator() {

        return this.emailCreator;
    }

    public String getTitle() {
        return title;
    }

    public Set<Song> getPlaylistSongs() {
        return playlistSongs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist playlist = (Playlist) o;
        return Objects.equals(emailCreator, playlist.emailCreator) &&
            Objects.equals(title, playlist.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emailCreator, title);
    }
}
