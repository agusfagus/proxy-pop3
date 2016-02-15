package handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.Map;

import connection.Server;
import org.apache.log4j.Logger;
import proxy.ClientProxy;
import config.Configuration;
import connection.BufferUtils;
import connection.Connection;
import statistics.Statistics;

/**
 * A Handler for messages from and to the Client
 */
public class ClientHandler extends ProxyHandler {

    /**
     * The class Logger
     */
    private static transient Logger LOGGER = Logger.getLogger(ClientHandler.class);

    /**
     * A message representing a successful connection
     */
    private static final String CONNECTED = "+OK POP3 ready\r\n";

    /**
     * A map from usernames to Servers
     */
    Map<String, Server> userToServer;

    /**
     * Create the Handler with the Selector corresponding to the Client Service, the Client Proxy
     * and the map from usernames to Servers
     * @param selector The Client Selector
     * @param proxy The Client Proxy
     * @param userToServer The map from usernames to Servers
     */
    public ClientHandler(Selector selector, ClientProxy proxy, Map<String, Server> userToServer) {
        this.selector = selector;
        this.proxy = proxy;
        this.userToServer = userToServer;
        this.bufferSize = Configuration.getInstance().getBufferSize();
    }

    /**
     * Handle accept requests accepting connections from clients
     * @param key The key to handle
     * @throws IOException
     */
    public void handleAccept(SelectionKey key) throws IOException {
        //Accept connection and extract address from key.
        SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
        String address = clientChannel.socket().getRemoteSocketAddress().toString();
        address = address.substring(1, address.indexOf(':'));
        LOGGER.info(address);

        //Connect to proxy service
        clientChannel.configureBlocking(false);
        clientChannel.socket().setKeepAlive(true);
        LOGGER.info("Accepted connection -> " + clientChannel.socket().getRemoteSocketAddress());
        Connection connection = new Connection(clientChannel, selector);
        connection.getClientBuffer().getWriteBuffer().append(CONNECTED);
        Statistics.getInstance().addConnection();
        clientChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, connection);
    }

    /**
     * Handle read requests reading information from client
     * @param key The key to handle
     * @throws IOException
     * @throws ParseException
     * @throws InterruptedException
     */
    public void handleRead(SelectionKey key) throws IOException, ParseException, InterruptedException {
        // Client socket channel has pending data
        SocketChannel channel = (SocketChannel) key.channel();
        Connection connection = ((Connection) key.attachment());
        StringBuffer readBuffer = connection.getClientBuffer().getReadBuffer();

        ByteBuffer buf = ByteBuffer.allocate(bufferSize);
        long bytesRead = channel.read(buf);
        buf.flip();
        //If the other end disconnected close everything and finish
        if (bytesRead == -1) {
            LOGGER.info("Client disconnected:" + channel.socket().getRemoteSocketAddress());
            channel.close();
            connection.close();
        } else if (bytesRead > 0) {
            String line = BufferUtils.bufferToString(buf);
            readBuffer.append(line);
            if (!line.endsWith("\n")) {
                return;
            }
            line = readBuffer.toString();
            readBuffer.delete(0, readBuffer.length());
            for(String s: line.split("\r\n")){
                proxy.proxy(s.concat("\r\n"), connection);
            }
        }
    }

    /**
     * Handle write requests writing information to client
     * @param key The key to handle
     * @throws IOException
     */
    public void handleWrite(SelectionKey key) throws IOException {
        Connection connection = (Connection) key.attachment();
        SocketChannel channel = connection.getClient();
        StringBuffer writeBuffer = connection.getClientBuffer().getWriteBuffer();
        ByteBuffer buf = ByteBuffer.wrap(writeBuffer.toString().getBytes());
        int bytesWritten = channel.write(buf);
        // Buffer completely written?
        if (!buf.hasRemaining()) {
            // Nothing left, so no longer interested in writes
            key.interestOps(SelectionKey.OP_READ);
        }
        writeBuffer.delete(0, bytesWritten);
        // Make room for more data to be read in
        buf.compact();
        buf.clear();
    }

}
