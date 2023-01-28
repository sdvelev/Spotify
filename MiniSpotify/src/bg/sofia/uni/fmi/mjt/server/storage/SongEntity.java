package bg.sofia.uni.fmi.mjt.server.storage;

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
}
