package bg.sofia.uni.fmi.mjt.server.player;

import bg.sofia.uni.fmi.mjt.server.ServerReply;
import bg.sofia.uni.fmi.mjt.server.StreamingPlatform;
import bg.sofia.uni.fmi.mjt.server.exceptions.IODatabaseException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoSuchSongException;
import bg.sofia.uni.fmi.mjt.server.exceptions.SongIsAlreadyPlayingException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotLoggedException;
import bg.sofia.uni.fmi.mjt.server.logger.SpotifyLogger;
import bg.sofia.uni.fmi.mjt.server.login.User;
import bg.sofia.uni.fmi.mjt.server.storage.Playlist;
import bg.sofia.uni.fmi.mjt.server.storage.Song;

import java.nio.channels.SelectionKey;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;

public class PlayPlaylist extends Thread {

    private final static int WAITING_TIME = 2000;
    private String playListTitle;
    private SelectionKey selectionKey;
    private StreamingPlatform streamingPlatform;

    public PlayPlaylist(String playListTitle, SelectionKey selectionKey,
                        StreamingPlatform streamingPlatform) {

        this.playListTitle = playListTitle;
        this.selectionKey = selectionKey;
        this.streamingPlatform = streamingPlatform;
    }

    @Override
    public void run()  {

        Set<Song> songsToPlay = new LinkedHashSet<>();
        for (Playlist currentPlaylist : streamingPlatform.getPlaylists().get(
            streamingPlatform.getUser().getEmail())) {

            if (currentPlaylist.getTitle().equals(playListTitle)) {

                songsToPlay = currentPlaylist.getPlaylistSongs();
                break;
            }
        }

        for (Song currentSong : songsToPlay.stream().toList()) {

            try {

                streamingPlatform.playSong(currentSong.getTitle(), selectionKey);
            } catch (UserNotLoggedException e) {

                SpotifyLogger.log(Level.INFO, ServerReply.PLAY_SONG_NOT_LOGGED_REPLY.getReply(), e);
            } catch (NoSuchSongException e) {

                SpotifyLogger.log(Level.INFO, streamingPlatform.getUser().getEmail() + " " +
                    ServerReply.PLAY_SONG_NO_SUCH_SONG_REPLY.getReply(), e);
            } catch (SongIsAlreadyPlayingException e) {

                SpotifyLogger.log(Level.INFO, streamingPlatform.getUser().getEmail() + " " +
                    ServerReply.PLAY_SONG_IS_ALREADY_RUNNING_REPLY.getReply(), e);
            } catch (IODatabaseException e) {

                SpotifyLogger.log(Level.INFO, streamingPlatform.getUser().getEmail() + " " +
                    ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
            } catch (Exception e) {

                SpotifyLogger.log(Level.INFO, streamingPlatform.getUser().getEmail() + " " +
                    ServerReply.SERVER_EXCEPTION.getReply(), e);
            }

            while (streamingPlatform.getAlreadyRunning().containsKey(selectionKey)) {

                try {

                    Thread.sleep(WAITING_TIME);
                } catch (InterruptedException e) {

                    SpotifyLogger.log(Level.INFO, streamingPlatform.getUser().getEmail() + " " +
                        ServerReply.SERVER_EXCEPTION.getReply(), e);
                }
            }
        }

    }
}
