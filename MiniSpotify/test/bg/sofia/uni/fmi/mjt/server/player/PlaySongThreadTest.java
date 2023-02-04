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
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.nio.channels.SelectionKey;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PlaySongThreadTest {

    @Mock
    StreamingPlatform streamingPlatformMock = mock(StreamingPlatform.class);

    @Mock
    SelectionKey selectionKeyMock = mock(SelectionKey.class);

    @Mock
    SpotifyLogger spotifyLoggerMock = mock(SpotifyLogger.class);

    @Test
    void testRunSongThreadTerminateSong() {

        PlaySong playSongThread = new PlaySong("The Crown - Main title", this.selectionKeyMock,
            this.streamingPlatformMock, this.spotifyLoggerMock);

        Map<SelectionKey, PlaySong> toReturnAlreadyRunning = new LinkedHashMap<>();
        toReturnAlreadyRunning.put(this.selectionKeyMock, null);

        when(this.streamingPlatformMock.getAlreadyRunning()).thenReturn(toReturnAlreadyRunning);

        playSongThread.start();

        try {
            Thread.sleep(1000);

            playSongThread.terminateSong();

            playSongThread.join();

        } catch (InterruptedException e) {

            fail("There was a problem with thread sleep method.");
        }

        verify(this.streamingPlatformMock, times(1)).getAlreadyRunning();
    }

}
