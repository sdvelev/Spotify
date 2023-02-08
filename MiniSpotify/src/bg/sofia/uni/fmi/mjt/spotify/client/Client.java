package bg.sofia.uni.fmi.mjt.spotify.client;

import bg.sofia.uni.fmi.mjt.spotify.server.ServerReply;
import bg.sofia.uni.fmi.mjt.spotify.server.logger.SpotifyLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Level;

public class Client {
    private final static int SERVER_PORT = 6767;
    private final static String SERVER_HOST = "localhost";
    private final static int BUFFER_SIZE = 2048;
    private static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private final static String TO_SERVER_CONNECTED = "Connected to the server";
    private final static String CLIENT_PROMPT = "Enter command: ";
    private final static String NETWORK_COMMUNICATION_PROBLEM_MESSAGE =
        "Unable to connect to the server. Try again later or contact administrator";

    public static void main(String[] args) {

        try (SocketChannel socketChannel = SocketChannel.open(); Scanner scanner = new Scanner(System.in)) {
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            System.out.println(TO_SERVER_CONNECTED);

            while (true) {

                System.out.print(CLIENT_PROMPT);
                String message = scanner.nextLine();

                if (message.isEmpty() || message.isBlank()) {
                    continue;
                }

                buffer.clear();
                buffer.put(message.getBytes());
                buffer.flip();
                socketChannel.write(buffer);

                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();

                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);
                String reply = new String(byteArray, StandardCharsets.UTF_8);

                System.out.println(reply);

                if (reply.equals(ServerReply.DISCONNECT_COMMAND_SUCCESSFULLY_REPLY.getReply())) {
                    break;
                }
            }

        } catch (IOException e) {
            SpotifyLogger spotifyLogger = new SpotifyLogger("SpotifyLogger.log");
            spotifyLogger.log(Level.SEVERE, NETWORK_COMMUNICATION_PROBLEM_MESSAGE, e);
            System.out.println(NETWORK_COMMUNICATION_PROBLEM_MESSAGE);
        }
    }
}
