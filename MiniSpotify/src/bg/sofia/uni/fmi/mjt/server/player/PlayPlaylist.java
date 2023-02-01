package bg.sofia.uni.fmi.mjt.server.player;

import bg.sofia.uni.fmi.mjt.server.ServerReply;
import bg.sofia.uni.fmi.mjt.server.StreamingPlatform;
import bg.sofia.uni.fmi.mjt.server.exceptions.IODatabaseException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoSuchSongException;
import bg.sofia.uni.fmi.mjt.server.exceptions.SongIsAlreadyPlayingException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotLoggedException;
import bg.sofia.uni.fmi.mjt.server.logger.SpotifyLogger;
import bg.sofia.uni.fmi.mjt.server.login.User;
import bg.sofia.uni.fmi.mjt.server.storage.Song;

import java.nio.channels.SelectionKey;
import java.util.Set;

public class PlayPlaylist extends Thread {

    private Set<Song> songsToPlay;
    private SelectionKey selectionKey;
    private StreamingPlatform streamingPlatform;

    public PlayPlaylist(Set<Song> songsToPlay, SelectionKey selectionKey,
                        StreamingPlatform streamingPlatform) {

        this.songsToPlay = songsToPlay;
        this.selectionKey = selectionKey;
        this.streamingPlatform = streamingPlatform;
    }



    @Override
    public void run()  {

        for (Song currentSong : this.songsToPlay) {

            try {

                this.streamingPlatform.playSong(currentSong.getTitle(), this.selectionKey);
            } catch (UserNotLoggedException e) {

                throw new RuntimeException(e);
            } catch (NoSuchSongException e) {

                throw new RuntimeException(e);
            } catch (SongIsAlreadyPlayingException e) {

                throw new RuntimeException(e);
            } catch (IODatabaseException e) {

                throw new RuntimeException(e);
            }

            while (this.streamingPlatform.getAlreadyRunning().containsKey(selectionKey)) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                    throw new RuntimeException(e);
                }
            }
        }

    }
}
