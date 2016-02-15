package handler;

import admin.Admin;
import config.Configuration;
import connection.AdminConnection;
import connection.BufferUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.ParseException;

/**
 * A Handler for messages from and to the Client
 */
public class AdminHandler extends ProxyHandler {
    /**
     * The class Logger
     */
    private static transient Logger LOGGER = Logger.getLogger(AdminHandler.class);

	/**
     * A message representing a successful connection
     */
    private static final String CONNECTED = "+OK Authenticate thyself mortal!\r\n";
    
    /**
     * The Admin corresponding to the given service
     */
    private Admin admin;

    /**
     * Create the Handler with the Selector corresponding to the Admin Service and the Admin
     * @param selector The Admin Selector
     * @param admin The Admin
     */
    public AdminHandler(Selector selector, Admin admin) {
        this.admin = admin;
        this.selector = selector;
        this.bufferSize = Configuration.getInstance().getBufferSize();
    }

    /**
     * Handle accept requests accepting connections from the admin
     * @param key The key to handle
     * @throws IOException
     */
    public void handleAccept(SelectionKey key) throws IOException, InterruptedException {
        //Accept connection and extract address from key.
        SocketChannel adminChannel = ((ServerSocketChannel) key.channel()).accept();
        String address = adminChannel.socket().getRemoteSocketAddress().toString();
        address = address.substring(1, address.indexOf(':'));
        LOGGER.info("Admin attempting connection from : " + address);

        //Connect to admin service
        adminChannel.configureBlocking(false);
       LOGGER.info("Accepted Admin connection -> " + adminChannel.socket().getRemoteSocketAddress());
        AdminConnection adminConnection = new AdminConnection(adminChannel, selector);
        adminConnection.getBuffer().getWriteBuffer().append(CONNECTED);
        adminChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, adminConnection);
    }

    /**
     * Handle read requests reading information from admin
     * @param key The key to handle
     * @throws IOException
     * @throws ParseException
     * @throws InterruptedException
     */
    public void handleRead(SelectionKey key) throws IOException, InterruptedException {
        // Client socket channel has pending data
        SocketChannel channel = (SocketChannel) key.channel();
        AdminConnection connection = ((AdminConnection) key.attachment());
        StringBuffer readBuffer = connection.getBuffer().getReadBuffer();

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
            for(String s: line.split("\r\n")) {
                admin.parse(s, connection);
            }
        }
    }

    /**
     * Handle write requests writing information to admin
     * @param key The key to handle
     * @throws IOException
     */
    public void handleWrite(SelectionKey key) throws IOException {
        AdminConnection connection = (AdminConnection) key.attachment();
        SocketChannel channel = connection.getChannel();
        StringBuffer writeBuffer = connection.getBuffer().getWriteBuffer();
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
