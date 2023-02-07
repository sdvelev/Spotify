package bg.sofia.uni.fmi.mjt.spotify.server.player;

import bg.sofia.uni.fmi.mjt.spotify.server.StreamingPlatform;
import bg.sofia.uni.fmi.mjt.spotify.server.logger.SpotifyLogger;
import bg.sofia.uni.fmi.mjt.spotify.server.player.PlaySongThread;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.channels.SelectionKey;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlaySongThreadTest {

    @Mock
    StreamingPlatform streamingPlatformMock;

    @Mock
    SelectionKey selectionKeyMock;

    @Mock
    SpotifyLogger spotifyLoggerMock;

    @Test
    void testRunSongThreadTerminateSong() {
        PlaySongThread playSongThread = new PlaySongThread("The Crown - Main title", selectionKeyMock,
            streamingPlatformMock, spotifyLoggerMock);

        Map<SelectionKey, PlaySongThread> toReturnAlreadyRunning = new LinkedHashMap<>();
        toReturnAlreadyRunning.put(selectionKeyMock, null);

        when(streamingPlatformMock.getAlreadyRunning()).thenReturn(toReturnAlreadyRunning);

        playSongThread.start();

        try {
            Thread.sleep(1000);

            playSongThread.terminateSong();

            playSongThread.join();

        } catch (InterruptedException e) {

            fail("There was a problem with thread sleep method.");
        }

        verify(streamingPlatformMock, times(1)).getAlreadyRunning();
    }
}
