package proxy;

import connection.Connection;
import connection.Server;
import connection.State;
import core.TransformationThread;
import mail.Mail;
import org.apache.log4j.Logger;
import statistics.Statistics;
import statistics.StatusCode;
import transformations.Leetifier;
import transformations.Rotation;
import transformations.Transformer;

import java.io.IOException;
import java.nio.channels.Selector;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A proxy for POP3 responses
 */
public class ServerProxy extends Pop3Proxy {

    /**
     * The class Logger
     */
    private static transient Logger LOGGER = Logger.getLogger(ServerProxy.class);

    /**
     * A Thread Pool to handle Transformations
     */
    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    /**
     * Create the Proxy with a map from usernames to Servers, the Client Selector and the Server Selector
     * @param serverMap A map from usernames to Servers
     * @param clientSelector The Client Selector
     * @param serverSelector The Server Selector
     * @throws IOException
     */
    public ServerProxy(Map<String, Server> serverMap, Selector clientSelector, Selector serverSelector) throws IOException {
        this.serverMap = serverMap;
        this.clientSelector = clientSelector;
        this.serverSelector = serverSelector;
    }

    /**
     * Process POP3 responses and forward them to Clients
     * @param line The message to process
     * @param connection The Connection between Client and Server to which the message belongs to
     * @throws IOException
     * @throws InterruptedException
     * @throws ParseException
     */
    @Override
    public void proxy(String line, Connection connection) throws IOException, InterruptedException, ParseException {
        if (line.startsWith(ERR)) {
            Statistics.getInstance().addResponse(StatusCode.ERR);
        } else if (!CONNECTED.equals(line)) {
            Statistics.getInstance().addResponse(StatusCode.OK);
        }

        LOGGER.debug("Received response -> " + line.trim());

        switch(connection.getLastCommand()) {

            case USER:
                if (line.startsWith(ERR)) {
                    LOGGER.info("Access failure: " + connection.getClient());
                    Statistics.getInstance().addAuth(StatusCode.ERR);
                }
                if(CONNECTED.equals(line)) {
                    // Skip initial connection message
                    break;
                }
                writeToClient(connection, line);
                break;

            case PASS:
                if (line.startsWith(OK)) {
                    connection.setState(State.TRANSACTION);
                    Statistics.getInstance().addAuth(StatusCode.OK);
                } else if (line.startsWith(ERR)) {
                    connection.setState(State.AUTHORIZATION_USER);
                    Statistics.getInstance().addAuth(StatusCode.ERR);
                }
                writeToClient(connection, line);
                break;

            case LIST:
                writeToClient(connection, line);
                break;

            case LIST_MULTI:
                if (line.equals(END)) {
                    connection.setLastCommand(Command.UNKNOWN);
                }
                writeToClient(connection, line);
                break;

            case RETR:
                connection.getMail().add(line);
                if(line.equals(END+"\r\n")){
                    List<Transformer> transformers = new LinkedList<>();
                    if (Leetifier.enabled.get()) {
                        transformers.add(new Leetifier());
                    }
                    if (Rotation.enabled.get()) {
                        transformers.add(new Rotation());
                    }
                    threadPool.execute(new TransformationThread(connection, clientSelector, transformers));
                }
                break;

            case DELE:
                connection.getMail().add(line);
                if (line.equals(END+"\r\n")) {
                    connection.getMail().parse();
                    LOGGER.info("Marking mail as deleted");
                    writeToServer(connection, "DELE " + connection.getMailToDelete() + "\r\n");
                    connection.setLastCommand(Command.UNKNOWN);
                    connection.setMail(new Mail());
                }
                break;

            case STAT:

            case NOOP:

            case RSET:

            case APOP:
                writeToClient(connection, line);
                break;

            case TOP:
                if (line.equals(END)) {
                    connection.setLastCommand(Command.UNKNOWN);
                }
                writeToClient(connection, line);
                break;

            case UIDL:
                writeToClient(connection, line);
                break;

            case UIDL_MULTI:
                if (line.equals(END)) {
                    connection.setLastCommand(Command.UNKNOWN);
                }
                writeToClient(connection, line);

                break;

            case QUIT:
                if (connection.getState() == State.TRANSACTION) {
                    connection.setState(State.UPDATE);
                }
                writeToClient(connection, line);
                connection.close();
                break;

            default:

            case UNKNOWN:
                writeToClient(connection, line);
        }
    }
}
