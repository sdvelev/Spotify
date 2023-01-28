package bg.sofia.uni.fmi.mjt.server.storage;

public class Song {

    private String title;
    private String artist;
    private int duration;
    private String genre;

    public Song(String title, String artist, int duration, String genre) {

        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.genre = genre;
    }

    public String getTitle() {
        return title;
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

    @Override
    public String toString() {
        return "Song{" +
            "title='" + title + '\'' +
            ", artist='" + artist + '\'' +
            ", duration=" + duration +
            ", genre='" + genre + '\'' +
            '}';
    }
}
