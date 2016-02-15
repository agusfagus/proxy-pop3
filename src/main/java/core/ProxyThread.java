package core;

import handler.Handler;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.text.ParseException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A Thread to represent a service of the Proxy
 */
public class ProxyThread extends Thread {

    /**
     * The class Logger
     */
    private static transient Logger LOGGER = Logger.getLogger(ProxyThread.class);

    /**
     * The Selector corresponding to this service
     */
	private Selector selector;

    /**
     * The Handler corresponding to this service
     */
	private Handler handler;

    /**
     * The timeout for selecting a key
     */
	private static final int TIMEOUT = 10;

    /**
     * A variable indicating that the execution should halt
     */
    private AtomicBoolean finished;

    /**
     * Create Thread with the Selector, Handler and Port corresponding to this service
     * @param selector The Selector corresponding to this service
     * @param handler The Handler corresponding to this service
     * @param port The Port corresponding to this service
     */
	public ProxyThread(Selector selector, Handler handler, int port, AtomicBoolean finished) {
		this.selector = selector;
		this.handler = handler;
        this.finished = finished;
        try {
            initializeSocketChannel(port);
        } catch (IOException e) {
            LOGGER.error("Couldn't initialize SocketChannel");
            e.printStackTrace();
        }
	}

    /**
     * Initialize the SocketChannel corresponding to this service
     * @param port The Port corresponding to this service
     * @throws IOException
     */
	private void initializeSocketChannel(int port) throws IOException{
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.socket().bind(new InetSocketAddress(port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
	}

    /**
     * Handle the keys registered to the Selector
     * @param selector The Selector corresponding to this service
     * @param handler The Handler corresponding to this service
     * @throws IOException
     * @throws InterruptedException
     * @throws ParseException
     */
	private void handleKeys(Selector selector, Handler handler) throws IOException, InterruptedException, ParseException {

		// Get iterator on set of keys with I/O to process
		Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();

		while (keyIter.hasNext()) {
			SelectionKey key = keyIter.next(); // Key is bit mask

			// Server socket channel has pending connection requests
			if (key.isValid() && key.isAcceptable())
				handler.handleAccept(key);

			// Client socket channel has pending data
			if (key.isValid() && key.isReadable())
				handler.handleRead(key);
			
			// Client socket channel is available for writing
			if (key.isValid() && key.isWritable())
				handler.handleWrite(key);
			
			keyIter.remove(); // remove from set after handling
		}
	}

    /**
     * Start handling requests
     */
	@Override
    public void run() {
        try {
            while (!finished.get()) { 		// Run forever, processing available I/O operations
                // Wait for some channel to be ready (or timeout)
                if (selector.select(TIMEOUT) == 0) { // returns # of ready chans
                    continue;
                }
                handleKeys(selector, handler);
            }
        } catch (CancelledKeyException cke) {

        } catch (ClosedSelectorException cse) {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}