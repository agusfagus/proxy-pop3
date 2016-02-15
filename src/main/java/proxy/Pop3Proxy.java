package proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.Map;

import connection.Server;
import connection.Connection;
import connection.State;
import org.apache.log4j.Logger;
import statistics.Statistics;

/**
 * A proxy for POP3 requests and responses
 */
public abstract class Pop3Proxy {

    /**
     * The class Logger
     */
    private static transient Logger LOGGER = Logger.getLogger(Pop3Proxy.class);

    /**
     * String corresponding to standard POP3 messages
     */
    protected static final String OK="+OK", ERR="-ERR", END=".", CONNECTED="+OK POP3 ready\r\n";

    /**
     * A map from usernames to Servers
     */
	protected Map<String, Server> serverMap;

    /**
     * The Selector corresponding to the Client service
     */
    protected Selector clientSelector;

    /**
     * The Selector corresponding to the Server service
     */
    protected Selector serverSelector;

    /**
     * Process message and forward it to destination
     * @param line The message to process
     * @param connection The Connection between Client and Server to which the message belongs to
     * @throws IOException
     * @throws InterruptedException
     * @throws ParseException
     */
    public abstract void proxy(String line, Connection connection) throws IOException, InterruptedException, ParseException;

    /**
     * Process a message unknown to the proxy
     * @param line The unknown message
     * @param connection The The Connection between Client and Server to which the message belongs to
     * @throws IOException
     * @throws InterruptedException
     */
    protected void unknownCommand(String line, Connection connection) throws IOException, InterruptedException {
		LOGGER.info("Received unknown or invalid command: " + line);
        if (connection.getState() != State.AUTHORIZATION_USER && connection.getState() != State.AUTHORIZATION_PASS) {
            //Pass it to server without processing
            writeToServer(connection, line);
            connection.setLastCommand(Command.UNKNOWN);
        } else {
            writeToClient(connection, ERR + " incorrect state\r\n");
        }
	}

    /**
     * Verifies if a command is a valid authentication
     * @param command The POP3 command
     * @param connection The Connection between Client and Server to which the command belongs to
     * @return If the authorization is valid
     * @throws IOException
     * @throws InterruptedException
     */
    protected boolean isValidAuthentication(String command[], Connection connection) throws IOException, InterruptedException{
        boolean correctState = connection.getState().equals(State.AUTHORIZATION_USER);
        boolean correctParamNumber = command.length == 2;

        return (correctState && correctParamNumber);
    }

    /**
     * Write a message to the Client
     * @param connection The Connection between Client and Server to which the message belongs to
     * @param line The Message to write to the Client
     * @throws IOException
     * @throws InterruptedException
     */
    protected void writeToClient(Connection connection, String line) throws IOException, InterruptedException {
        writeToChannel(connection.getClient(), line, connection.getClientBuffer().getWriteBuffer(), clientSelector);
        Statistics.getInstance().processResponse(line.length());
    }

    /**
     * Write a message to the Server
     * @param connection The Connection between Client and Server to which the message belongs to
     * @param line The Message to write to the Client
     * @throws IOException
     * @throws InterruptedException
     */
    protected void writeToServer(Connection connection, String line) throws IOException, InterruptedException {
        writeToChannel(connection.getServer(), line, connection.getServerBuffer().getWriteBuffer(), serverSelector);
        Statistics.getInstance().processRequest(line.length());
    }

    /**
     * Write a Message to a Channel
     * @param channel The Channel to write to
     * @param line The Message to write to the Client
     * @param buffer The Buffer to write to
     * @param selector The Selector corresponding to the Channel to write to
     * @throws InterruptedException
     * @throws IOException
     */
    protected void writeToChannel(SocketChannel channel, String line, StringBuffer buffer, Selector selector) throws InterruptedException, IOException{
        SelectionKey key = channel.keyFor(selector);
        buffer.append(line);
        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    /**
     * Create a connection to a Server
     * @param connection The Connection between Client and Server to establish
     * @param server The Server to connect to
     * @throws IOException
     */
    protected void connectToServer(Connection connection, Server server) throws IOException{
        LOGGER.info("Server: "+ server.getName() +" port: "+ server.getPort());
        SocketChannel serverSocketChannel = SocketChannel.open(new InetSocketAddress(server.getName(), server.getPort()));
        serverSocketChannel.configureBlocking(false); // Must be nonblocking to register
        LOGGER.info("Creating connection -> " + serverSocketChannel.socket().getRemoteSocketAddress());
        connection.connectToServer(serverSocketChannel);
        serverSocketChannel.register(serverSelector, SelectionKey.OP_READ, connection);
        LOGGER.info("client:" + connection.getClient());
        LOGGER.info("host:" + serverSocketChannel);
    }

}
