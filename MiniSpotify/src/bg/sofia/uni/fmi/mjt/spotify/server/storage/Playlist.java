package bg.sofia.uni.fmi.mjt.spotify.server.storage;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Playlist {

    private String emailCreator;
    private String title;
    private final Set<Song> playlistSongs;

    public Playlist() {
        playlistSongs = new LinkedHashSet<>();
    }

    public Playlist(String emailCreator, String title) {
        this.emailCreator = emailCreator;
        this.title = title;
        this.playlistSongs = new LinkedHashSet<>();
    }

    public void addSong(Song toAdd) {
        playlistSongs.add(toAdd);
    }

    public void removeSong(Song toRemove) {
        playlistSongs.remove(toRemove);
    }

    public boolean containsSong(Song toCheck) {
        return playlistSongs.contains(toCheck);
    }

    public String getEmailCreator() {
        return emailCreator;
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