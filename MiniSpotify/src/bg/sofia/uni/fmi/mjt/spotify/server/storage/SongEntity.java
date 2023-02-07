package bg.sofia.uni.fmi.mjt.spotify.server.storage;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class SongEntity {

    private final Song song;
    private final AtomicInteger listeningTimes;
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
