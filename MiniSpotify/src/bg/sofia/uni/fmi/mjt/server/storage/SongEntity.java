package bg.sofia.uni.fmi.mjt.server.storage;

import java.util.Objects;

public class SongEntity {

    private Song song;
    private int listeningTimes;

    public SongEntity(Song song, int listeningTimes) {

        this.song = song;
        this.listeningTimes = listeningTimes;
    }

    public Song getSong() {
        return song;
    }

    public int getListeningTimes() {
        return listeningTimes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SongEntity that = (SongEntity) o;
        return Objects.equals(song, that.song);
    }

    @Override
    public int hashCode() {
        return Objects.hash(song);
    }
}
