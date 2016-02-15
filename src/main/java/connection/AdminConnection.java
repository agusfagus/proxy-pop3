package connection;

import config.Configuration;
import org.apache.log4j.Logger;
import proxy.AdminCommand;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * A connection between the proxy and the admin
 */
public class AdminConnection {

    /**
     * The class Logger
     */
    private static transient Logger LOGGER = Logger.getLogger(AdminConnection.class);

    /**
     * The Admin Selector
     */
    private Selector selector;
    /**
     * The current State of the connection
     */
    private AdminState state;
    /**
     * The last Command that the admin sent
     */
    private AdminCommand lastCommand;
    /**
     * The read-write buffer to interact with the Admin
     */
    private DoubleBuffer buffer;
    /**
     * The SocketChannel to which the Admin is connected
     */
    private SocketChannel channel;

    /**
     * Create a connection with the Admin Selector and the SocketChannel to which the admin is connected
     * @param channel The SocketChannel to which the admin is connected
     * @param selector The Admin Selector
     * @throws IOException
     */
    public AdminConnection(SocketChannel channel, Selector selector) throws IOException{
        this.selector = selector;
        this.state = AdminState.AUTHORIZATION_ADMIN;
        this.buffer = new DoubleBuffer(Configuration.getInstance().getBufferSize());
        this.channel = channel;
    }

    /**
     * Get the SocketChannel to which the Admin is connected
     * @return The SocketChannel to which the Admin is connected
     */
    public SocketChannel getChannel(){
        return channel;
    }

    /**
     * Get the read-write buffer to interact with the Admin
     * @return The read-write buffer to interact with the Admin
     */
    public DoubleBuffer getBuffer() {
        return buffer;
    }

    /**
     * Get the current State of the connection
     * @return The current State of the connection
     */
    public AdminState getState() {
        return state;
    }

    /**
     * Set the current State of the connection
     * @param state The current State of the connection
     */
    public void setState(AdminState state) {
        this.state = state;
    }

    /**
     * Get the last Command that the admin sent
     * @return The last Command that the admin sent
     */
    public AdminCommand getLastCommand() {
        return lastCommand;
    }

    /**
     * Set the last Command that the admin sent
     * @param lastCommand The last Command that the admin sent
     */
    public void setLastCommand(AdminCommand lastCommand) {
        this.lastCommand = lastCommand;
    }

    /**
     * Close the SocketChannels corresponding to the Admin
     * Cancel the keys associated with the Admin Selector
     */
    public void close() {
        try {
            SelectionKey adminKey = channel.keyFor(selector);
            if (adminKey != null) {
                adminKey.cancel();
            }
            channel.close();
        } catch (IOException e) {
            LOGGER.error("Error closing Admin socket.");
        }
    }
}
