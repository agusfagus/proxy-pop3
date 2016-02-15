package handler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.text.ParseException;

/**
 * A Handler for a given service
 */
public interface Handler {

    /**
     * Handle accept requests
     * @param key The key to handle
     * @throws IOException
     * @throws InterruptedException
     */
      void handleAccept(SelectionKey key) throws IOException, InterruptedException;

    /**
     * Handle read requests
     * @param key The key to handle
     * @throws IOException
     * @throws InterruptedException
     * @throws ParseException
     */
      void handleRead(SelectionKey key) throws IOException, InterruptedException, ParseException;

    /**
     * Handle write requests
     * @param key The key to handle
     * @throws IOException
     */
      void handleWrite(SelectionKey key) throws IOException;
}