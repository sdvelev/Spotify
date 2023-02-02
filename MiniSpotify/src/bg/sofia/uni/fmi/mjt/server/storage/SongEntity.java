package bg.sofia.uni.fmi.mjt.server.storage;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class SongEntity {

    private Song song;
    private AtomicInteger listeningTimes;

    public SongEntity() {

        song = new Song();
        listeningTimes = new AtomicInteger();
    }

    public SongEntity(Song song, int listeningTimes) {

        this.song = song;
        this.listeningTimes = new AtomicInteger(listeningTimes);
    }

    public Song getSong() {
        return song;
    }

    public int getListeningTimes() {
        return listeningTimes.intValue();
    }

    public void increaseListeningTimes() {

        listeningTimes.getAndIncrement();
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
