package bg.sofia.uni.fmi.mjt.spotify.server.storage;

import java.util.Objects;

public class Song {

    private final static String EMPTY_STRING = "";
    private String title;
    private String artist;
    private int duration;
    private String genre;

    public Song() {
        this(EMPTY_STRING, EMPTY_STRING, 0, EMPTY_STRING);
    }
    public Song(String title, String artist, int duration, String genre) {
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.genre = genre;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(title, song.title) && Objects.equals(artist, song.artist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, artist);
    }

    public String getArtist() {
        return artist;
    }

    public int getDuration() {
        return duration;
    }

    public String getGenre() {
        return genre;
    }

}
