package handler;

import config.Configuration;
import connection.BufferUtils;
import connection.Connection;
import org.apache.log4j.Logger;
import proxy.ServerProxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.ParseException;

/**
 * A Handler for messages from and to the Server
 */
public class ServerHandler extends ProxyHandler {
    /**
     * The class Logger
     */
    private static transient Logger LOGGER = Logger.getLogger(ServerHandler.class);

    /**
     * Create the Handler with the Selector corresponding to the Server Service, the Server Proxy
     * @param selector The Server Selector
     * @param proxy The Server Proxy
     */
    public ServerHandler(Selector selector, ServerProxy proxy) {
        this.selector = selector;
        this.proxy = proxy;
        this.bufferSize = Configuration.getInstance().getBufferSize();
    }

    /**
     * Handle accept requests ignoring them
     * @param key The key to handle
     */
    public void handleAccept(SelectionKey key) {

    }

    /**
     * Handle read requests reading information from server
     * @param key The key to handle
     * @throws IOException
     * @throws ParseException
     * @throws InterruptedException
     */
    public void handleRead(SelectionKey key) throws IOException, InterruptedException, ParseException {
        SocketChannel channel = (SocketChannel) key.channel();
        Connection connection = ((Connection) key.attachment());
        StringBuffer readBuffer = connection.getServerBuffer().getReadBuffer();

        ByteBuffer buf = ByteBuffer.allocate(bufferSize);
        long bytesRead = channel.read(buf);
        buf.flip();

        //If the other end disconnected close everything and finish
        if (bytesRead == -1) {
            LOGGER.info("Server disconnected: " + connection.getClient().socket().getRemoteSocketAddress());
            connection.close();
        } else if (bytesRead > 0) {
            String line = BufferUtils.bufferToString(buf);
            readBuffer.append(line);
            if (!line.endsWith("\r\n")) {
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
     * Handle write requests writing information to server
     * @param key The key to handle
     * @throws IOException
     */
    public void handleWrite(SelectionKey key) throws IOException {
        Connection connection = (Connection) key.attachment();
        SocketChannel channel = connection.getServer();
        StringBuffer writeBuffer = connection.getServerBuffer().getWriteBuffer();
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
