package bg.sofia.uni.fmi.mjt.client;

import bg.sofia.uni.fmi.mjt.server.ServerReply;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {
    private static final int SERVER_PORT = 9999;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 512;
    private static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private final static String DISCONNECT_COMMAND_NAME = "disconnect";
    private final static String DISCONNECT_COMMAND_REPLY = "You have successfully disconnected.";


    public static void main(String[] args) {

        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            System.out.println("Connected to the server.");

            boolean toDisconnect = false;
            while (true) {

                System.out.print("Enter message: ");
                String message = scanner.nextLine();

                buffer.clear();
                buffer.put(message.getBytes());
                buffer.flip();
                socketChannel.write(buffer);

                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();

                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);
                String reply = new String(byteArray, "UTF-8");

                System.out.println(reply);

                if (reply.equals(ServerReply.DISCONNECT_COMMAND_SUCCESSFULLY_REPLY.getReply())) {
                    break;
                }
            }

        } catch (IOException e) {

            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }
}
