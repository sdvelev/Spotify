package bg.sofia.uni.fmi.mjt.spotify.server;

import bg.sofia.uni.fmi.mjt.spotify.server.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.spotify.server.command.CommandExtractor;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.IODatabaseException;
import bg.sofia.uni.fmi.mjt.spotify.server.logger.SpotifyLogger;
import bg.sofia.uni.fmi.mjt.spotify.server.login.AuthenticationService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class Server {

    private static final int SERVER_PORT = 7600;
    private static final int BUFFER_SIZE = 2048;
    private static final String HOST = "localhost";

    private static final String ERROR_CLIENT_REQUEST = "Error occurred while processing your request. " +
        "Please, try again later or contact administrator";

    private static final String UNABLE_TO_START_SERVER = "A problem arise in starting the server";

    private static final String CLIENT_LABEL = "Client ";
    private static final String CLOSE_CONNECTION_LABEL = " has closed the connection.";
    private final CommandExecutor commandExecutor;
    private final int port;
    private boolean isServerWorking;

    private ByteBuffer buffer;
    private Selector selector;
    private final AtomicInteger numberOfConnection;
    private final SpotifyLogger spotifyLogger;

    public Server(int port, CommandExecutor commandExecutor, SpotifyLogger spotifyLogger) {
        this.port = port;
        this.commandExecutor = commandExecutor;
        this.numberOfConnection = new AtomicInteger(0);
        this.spotifyLogger = spotifyLogger;
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
            isServerWorking = true;

            while (isServerWorking) {
                try {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }

                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {

                        SelectionKey key = keyIterator.next();

                        if (key.isReadable()) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            try {
                                String clientInput = getClientInput(clientChannel);

                                if (clientInput == null) {
                                    key.channel().close();
                                    key.cancel();
                                    keyIterator.remove();
                                    continue;
                                }

                                String output = commandExecutor.executeCommand(CommandExtractor.newCommand(clientInput),
                                    key);
                                writeClientOutput(clientChannel, output);

                            } catch (SocketException e) {
                                key.channel().close();
                                key.cancel();
                                keyIterator.remove();
                                continue;
                            }

                        } else if (key.isAcceptable()) {
                            accept(selector, key);
                        }

                        keyIterator.remove();
                    }
                } catch (IOException e) {
                    spotifyLogger.log(Level.SEVERE, ERROR_CLIENT_REQUEST, e);
                    System.out.println(ERROR_CLIENT_REQUEST);
                }
            }
        } catch (IOException e) {
            spotifyLogger.log(Level.SEVERE, UNABLE_TO_START_SERVER, e);
            System.out.println(ERROR_CLIENT_REQUEST);
        }
    }

    public void stop() {
        isServerWorking = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(HOST, port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            System.out.println(CLIENT_LABEL + numberOfConnection.incrementAndGet() + CLOSE_CONNECTION_LABEL);
            SelectionKey key = clientChannel.keyFor(selector);
            clientChannel.close();
            if (key != null) {
                key.cancel();
            }
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();

        clientChannel.write(buffer);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    public static void main(String[] args) throws InterruptedException, IODatabaseException, IOException {
        SpotifyLogger spotifyLogger = new SpotifyLogger("SpotifyLogger.log");

        Server s = new Server(SERVER_PORT, new CommandExecutor(new StreamingPlatform(spotifyLogger),
            new AuthenticationService(), spotifyLogger),
            spotifyLogger);

        s.start();
    }
}