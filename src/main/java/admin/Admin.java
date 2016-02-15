package admin;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import connection.Server;
import proxy.AdminCommand;
import config.Configuration;
import connection.AdminConnection;
import connection.AdminState;
import statistics.Statistics;
import transformations.Leetifier;
import transformations.Rotation;

public class Admin {

    private Map<String, Server> userToServerMap;
    private String pass;
    private Selector adminSelector;
    private static final String UNKNOWN_COMMAND = "-ERR Unknown command\r\n";
    private AtomicBoolean finished;

    /**
     * An Admin to parse and execute the commands from our protocol
     */
    public Admin(Map<String, Server> userToServerMap, Selector adminSelector, AtomicBoolean finished) {
        this.userToServerMap = userToServerMap;
        this.adminSelector = adminSelector;
        this.finished = finished;
        this.pass = Configuration.getInstance().getAdminPassword();
    }

    /**
     * Parses the corresponding line and executes the command
     */
    public void parse(String line, AdminConnection connection) throws IOException, InterruptedException {

        String command[] = line.split(" ");
        AdminCommand adminCommand;

        try {
            adminCommand = AdminCommand.valueOf(command[0].trim().toUpperCase());
        } catch(IllegalArgumentException e) {
            adminCommand = AdminCommand.UNKNOWN;
        }
        switch(adminCommand) {

            case AUTH:
                if (connection.getState() == AdminState.AUTHORIZATION_ADMIN && command.length == 2 && command[1].equals(pass)) {
                    connection.setState(AdminState.TRANSACTION);
                    connection.setLastCommand(AdminCommand.AUTH);
                    writeToChannel(connection.getChannel(), "+OK Welcome master\r\n", connection.getBuffer().getWriteBuffer(), adminSelector);
                } else {
                    writeToChannel(connection.getChannel(), UNKNOWN_COMMAND, connection.getBuffer().getWriteBuffer(), adminSelector);
                }
                break;
            case LISTUSERS:
                if (connection.getState() == AdminState.TRANSACTION && command.length == 1) {
                    writeToChannel(connection.getChannel(), "+OK " + userToServerMap.keySet().size() + " users", connection.getBuffer().getWriteBuffer(), adminSelector);
                    for (String name : userToServerMap.keySet()) {
                        writeToChannel(connection.getChannel(), name + "\r\n", connection.getBuffer().getWriteBuffer(), adminSelector);
                        connection.setLastCommand(AdminCommand.LISTUSERS);
                    }
                    writeToChannel(connection.getChannel(), ".\r\n", connection.getBuffer().getWriteBuffer(), adminSelector);
                } else {
                    writeToChannel(connection.getChannel(), UNKNOWN_COMMAND, connection.getBuffer().getWriteBuffer(), adminSelector);
                }
                break;
            case SETSERVER:
                if (connection.getState() == AdminState.TRANSACTION && command.length == 4 && command[3].matches("^-?[0-9]+(\\.[0-9]+)?$")) {
                    userToServerMap.put(command[1], new Server(command[2], Integer.parseInt(command[3])));
                    writeToChannel(connection.getChannel(), "+OK Server " + command[2] + ":" + command[3] + " set for " + command[1] + "\r\n", connection.getBuffer().getWriteBuffer(), adminSelector);
                } else {
                    writeToChannel(connection.getChannel(), UNKNOWN_COMMAND, connection.getBuffer().getWriteBuffer(), adminSelector);
                }
                break;
            case RETRSTATS:
                if (connection.getState() == AdminState.TRANSACTION && command.length == 1) {
                    writeToChannel(connection.getChannel(), Statistics.getInstance().getCondensedStatistics(), connection.getBuffer().getWriteBuffer(), adminSelector);
                } else {
                    writeToChannel(connection.getChannel(), UNKNOWN_COMMAND, connection.getBuffer().getWriteBuffer(), adminSelector);
                }
                break;
            case TOGGLELEET:
                if (connection.getState() == AdminState.TRANSACTION && command.length == 1) {
                    boolean v;
                    do {
                        v = Leetifier.enabled.get();
                    } while(!Leetifier.enabled.compareAndSet(v, !v));
                    if (v) {
                        writeToChannel(connection.getChannel(), "+OK Leetifyer disabled\r\n", connection.getBuffer().getWriteBuffer(), adminSelector);
                    } else {
                        writeToChannel(connection.getChannel(), "+OK Leetifyer enabled\r\n", connection.getBuffer().getWriteBuffer(), adminSelector);

                    }
                } else {
                    writeToChannel(connection.getChannel(), UNKNOWN_COMMAND, connection.getBuffer().getWriteBuffer(), adminSelector);
                }
                break;
            case TOGGLEROTATION:
                if (connection.getState() == AdminState.TRANSACTION && command.length == 1) {
                    boolean v;
                    do {
                        v = Rotation.enabled.get();
                    } while(!Rotation.enabled.compareAndSet(v, !v));
                    if (v) {
                        writeToChannel(connection.getChannel(), "+OK Image rotation disabled\r\n", connection.getBuffer().getWriteBuffer(), adminSelector);
                    } else {
                        writeToChannel(connection.getChannel(), "+OK Image rotation enabled\r\n", connection.getBuffer().getWriteBuffer(), adminSelector);

                    }
                } else {
                    writeToChannel(connection.getChannel(), UNKNOWN_COMMAND, connection.getBuffer().getWriteBuffer(), adminSelector);
                }
                break;
            case FINALIZE:
                if (connection.getState() == AdminState.TRANSACTION && command.length == 1) {
                    finished.set(true);
                } else {
                    writeToChannel(connection.getChannel(), UNKNOWN_COMMAND, connection.getBuffer().getWriteBuffer(), adminSelector);
                }
                break;
            case QUIT:
                writeToChannel(connection.getChannel(), "+OK\r\n", connection.getBuffer().getWriteBuffer(), adminSelector);
                connection.close();
                break;
            case UNKNOWN:
                writeToChannel(connection.getChannel(), UNKNOWN_COMMAND, connection.getBuffer().getWriteBuffer(), adminSelector);
                break;
            default:
        }
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
}
