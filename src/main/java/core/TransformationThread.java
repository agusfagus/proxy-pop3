package core;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.List;

import mail.Mail;
import statistics.Statistics;
import transformations.Transformer;
import connection.Connection;

/**
 * A Thread to parse a Mail and apply Transformations
 */
public class TransformationThread extends Thread {

    /**
     * The List of transformers that should be applied
     */
    private List<Transformer> transformers;

    /**
     * The Selector corresponding to the Client Thread
     */
    private Selector clientSelector;

    /**
     * The Connection between Client and Server to which the Mail being transformed belongs
     */
    private Connection connection;

    /**
     * Create Thread with the Connection and Selector
     * @param connection The Connection between Client and Server to which the Mail being transformed belongs
     * @param clientSelector The Selector corresponding to the Client Thread
     */
    public TransformationThread(Connection connection, Selector clientSelector, List<Transformer> transformers) {
        this.connection = connection;
        this.clientSelector = clientSelector;
        this.transformers = transformers;
    }

    /**
     * Start transforming mail
     */
    @Override
    public void run() {
        try {
            connection.getMail().parse();
            for (Transformer transformer : transformers) {
                transformer.transform(connection.getMail());
            }
            writeMail(connection);
            connection.setMail(new Mail());
        } catch (IOException | InterruptedException ie) {

        }
    }

    /**
     * Write mail to client
     * @param connection The Connection between Client and Server to which the Mail being transformed belongs
     * @throws IOException
     * @throws InterruptedException
     */
    private void writeMail(Connection connection) throws IOException, InterruptedException {
        RandomAccessFile r = new RandomAccessFile("mails/mail" + connection.getMail().id() +".txt", "r");
        String s;
        while((s=r.readLine())!=null){
            writeToClient(connection, s + "\r\n");
        }
        r.close();
    }

    /**
     * Write a message to the Client
     * @param connection The Connection between Client and Server to which the Mail being transformed belongs
     * @param line The Message to write to the Client
     * @throws IOException
     * @throws InterruptedException
     */
    private void writeToClient(Connection connection, String line) throws IOException, InterruptedException {
        writeToChannel(connection.getClient(), line, connection.getClientBuffer().getWriteBuffer(), clientSelector);
        Statistics.getInstance().processResponse(line.length());
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
    private void writeToChannel(SocketChannel channel, String line, StringBuffer buffer, Selector selector) throws InterruptedException, IOException{
        SelectionKey key = channel.keyFor(selector);
        buffer.append(line);
        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }
}
