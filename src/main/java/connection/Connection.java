package connection;

import config.Configuration;
import mail.Mail;
import org.apache.log4j.Logger;
import proxy.Command;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * A connection between a user and a server
 */
public class Connection {

    /**
     * The class Logger
     */
    private static transient Logger LOGGER = Logger.getLogger(Connection.class);

    /**
     * The Client Selector
     */
    private Selector selector;

    /**
     * The current State of the connection
     */
    private State state;

    /**
     * The last Command that the client sent
     */
    private Command lastCommand;

    /**
     * The last Mail marked for deletion
     */
    private String mailToDelete;

    /**
     * The current Mail being transferred
     */
    private Mail mail;

    /**
     * The read-write buffer to interact with the Client
     */
    private DoubleBuffer clientBuffer;

    /**
     * The read-write buffer to interact with the Server
     */
    private DoubleBuffer serverBuffer;

    /**
     * The SocketChannel to which the Client is connected
     */
    private SocketChannel client;

    /**
     * The SocketChannel to which the Server is connected
     */
    private SocketChannel server;

    /**
     * Create a connection with the Client Selector and the SocketChannel to which the client is connected
     * @param client The SocketChannel to which the client is connected
     * @param selector The Client Selector
     * @throws IOException
     */
    public Connection(SocketChannel client, Selector selector) throws IOException{
        this.selector = selector;
        this.state = State.AUTHORIZATION_USER;
        this.lastCommand = Command.UNKNOWN;

        this.clientBuffer = new DoubleBuffer(Configuration.getInstance().getBufferSize());
        this.client = client;

        this.mail = new Mail();
    }

    /**
     * Connect to server creating a read-write buffer and storing the SocketChannel to which the server is connected
     * @param server The SocketChannel to which the server is connected
     */
    public void connectToServer(SocketChannel server){
        this.serverBuffer = new DoubleBuffer(Configuration.getInstance().getBufferSize());
        this.server = server;
    }

    /**
     * Get the current Mail being transferred
     * @return The current Mail being transferred
     */
    public Mail getMail() {
        return mail;
    }

    /**
     * Set the current Mail being transferred
     * @param mail The current Mail being transferred
     */
    public void setMail (Mail mail) {
        this.mail = mail;
    }

    /**
     * Get the SocketChannel to which the Client is connected
     * @return The SocketChannel to which the Client is connected
     */
    public SocketChannel getClient(){
        return client;
    }

    /**
     * Get the SocketChannel to which the Server is connected
     * @return The SocketChannel to which the Server is connected
     */
    public SocketChannel getServer(){
        return server;
    }

    /**
     * Get the read-write buffer to interact with the Client
     * @return The read-write buffer to interact with the Client
     */
    public DoubleBuffer getClientBuffer() {
        return clientBuffer;
    }

    /**
     * Get the read-write buffer to interact with the Server
     * @return The read-write buffer to interact with the Server
     */
    public DoubleBuffer getServerBuffer() {
        return serverBuffer;
    }

    /**
     * Get the current State of the connection
     * @return The current State of the connection
     */
    public State getState() {
        return state;
    }

    /**
     * Set the current State of the connection
     * @param state The current State of the connection
     */
    public void setState(State state) {
        this.state = state;
    }

    /**
     * Get the last Command that the client sent
     * @return The last Command that the client sent
     */
    public Command getLastCommand() {
        return lastCommand;
    }

    /**
     * Set the last Command that the client sent
     * @param lastCommand The last Command that the client sent
     */
    public void setLastCommand(Command lastCommand) {
        this.lastCommand = lastCommand;
    }

    /**
     * Get The last Mail marked for deletion
     * @return The last Mail marked for deletion
     */
    public String getMailToDelete() {
        return mailToDelete;
    }

    /**
     * Set the last Mail marked for deletion
     * @param mailToDelete The last Mail marked for deletion
     */
    public void setMailToDelete(String mailToDelete) {
        this.mailToDelete = mailToDelete;
    }

    /**
     * Close the SocketChannels corresponding to the Client and the Server
     * Cancel the keys associated with the Client Selector
     */
    public void close() {
        try {
            SelectionKey clientKey = client.keyFor(selector);
            if (clientKey != null) {
                clientKey.cancel();
            }
            client.close();
            if (server != null) {
                SelectionKey serverKey = server.keyFor(selector);
                if (serverKey != null) {
                    serverKey.cancel();
                }
                server.close();
            }
        } catch (IOException e) {
            LOGGER.error("Error closing socket");
        }
    }

}