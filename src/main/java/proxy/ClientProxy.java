package proxy;

import config.Configuration;
import connection.Connection;
import connection.Server;
import connection.State;
import org.apache.log4j.Logger;
import statistics.Statistics;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.Map;

/**
 * A proxy for POP3 requests
 */
public class ClientProxy extends Pop3Proxy {

    private static transient Logger LOGGER = Logger.getLogger(ClientProxy.class);

    /**
     * Create the Proxy with a map from usernames to Servers, the Client Selector and the Server Selector
     * @param serverMap A map from usernames to Servers
     * @param clientSelector The Client Selector
     * @param serverSelector The Server Selector
     * @throws IOException
     */
    public ClientProxy(Map<String, Server> serverMap, Selector clientSelector, Selector serverSelector) throws IOException {
        this.serverMap = serverMap;
        this.clientSelector = clientSelector;
        this.serverSelector = serverSelector;
    }

    /**
     * Process POP3 requests and forward them to Servers
     * @param line The message to process
     * @param connection The Connection between Client and Server to which the message belongs to
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void proxy(String line, Connection connection) throws IOException, InterruptedException {

        String command[] = line.split(" ");
        Command com;

        try {
            com = Command.valueOf(command[0].trim().toUpperCase());
        } catch(IllegalArgumentException e) {
            com = Command.UNKNOWN;
        }
        Statistics.getInstance().addRequest();
        LOGGER.info("Received request -> " + line.trim());
        switch(com) {

            case USER:
                if (isValidAuthentication(command, connection)) {
                    LOGGER.info("Authentication Request: " + command[1].trim());
                    Server server = serverMap.get(command[1].trim());
                    if (server == null) {
                        server = Configuration.getInstance().getDefaultServer();
                    }
                    connectToServer(connection, server);
                    connection.setState(State.AUTHORIZATION_PASS);
                    connection.setLastCommand(Command.USER);
                    writeToServer(connection, line);

                } else {
                    writeToClient(connection, "-ERR\r\n");
                }
                break;

            case PASS:
                if (connection.getState() != State.AUTHORIZATION_PASS || connection.getLastCommand() != Command.USER || command.length != 2) {
                    unknownCommand(line, connection);
                    return;
                }
                LOGGER.info("Authentication Password: " + command[1].trim());
                writeToServer(connection, line);
                connection.setLastCommand(com);
                break;

            case LIST:
                if (connection.getState() != State.TRANSACTION || command.length > 3) {
                    unknownCommand(line, connection);
                    return;
                }
                LOGGER.info("Listing Emails");
                writeToServer(connection, line);
                connection.setLastCommand(com);
                break;

            case RETR:
                if (connection.getState() != State.TRANSACTION || command.length != 2) {
                    unknownCommand(line, connection);
                    return;
                }
                LOGGER.info("Retrieving Email: " + command[1].trim());
                command[1] = command[1].trim();
                writeToServer(connection, line);
                connection.setLastCommand(com);
                break;

            case DELE :
                if (connection.getState() != State.TRANSACTION || command.length != 2) {
                    unknownCommand(line, connection);
                    return;
                }
                try {
                    Integer.parseInt(command[1].trim());
                } catch(Exception e){
                    unknownCommand(line, connection);
                    return;
                }
                writeToServer(connection, "RETR " + command[1]);
                LOGGER.info("Deleting Email: " + command[1].trim());
                connection.setMailToDelete(command[1].trim());
                connection.setLastCommand(com);
                break;

            case APOP:
                if (connection.getState() != State.TRANSACTION || command.length != 1) {
                    unknownCommand(line, connection);
                    return;
                }
                writeToServer(connection, line);
                connection.setLastCommand(com);
                break;

            case TOP:
                if (connection.getState() != State.TRANSACTION || command.length != 3) {
                    unknownCommand(line, connection);
                    return;
                }
                writeToServer(connection, line);
                command[1] = command[1].trim();
                command[2] = command[2].trim();
                connection.setLastCommand(com);
                break;

            case UIDL:
                if (connection.getState() != State.TRANSACTION || command.length > 2) {
                    unknownCommand(line, connection);
                    return;
                }
                writeToServer(connection, line);
                if (command.length == 2) {
                    command[1] = command[1].trim();
                    connection.setLastCommand(com);
                } else {
                    connection.setLastCommand(Command.UIDL_MULTI);
                }
                break;

            case QUIT:
                if (command.length != 1 ) {
                    unknownCommand(line, connection);
                    return;
                }
                if (connection.getState() == State.AUTHORIZATION_USER) {
                    writeToClient(connection, OK);
                    connection.close();
                } else {
                    writeToServer(connection, line);
                    connection.setLastCommand(com);
                }
                break;

            case STAT:

            case NOOP:

            case RSET:
                writeToServer(connection, line);
                connection.setLastCommand(com);
                break;
            default:
                if (connection.getState() == State.AUTHORIZATION_USER) {
                    writeToClient(connection, ERR + " invalid command\r\n");
                } else {
                    unknownCommand(line, connection);
                }
        }
    }
}
