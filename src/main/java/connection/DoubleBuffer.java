package connection;

/**
 * A read-write buffer
 */
public class DoubleBuffer {

    /**
     * The read Buffer
     */
	private StringBuffer readBuffer;

    /**
     * The write Buffer
     */
    private StringBuffer writeBuffer;

    /**
     * Create both buffers
     * @param n The Buffer size
     */
	public DoubleBuffer(int n){
		readBuffer = new StringBuffer(n);
		writeBuffer = new StringBuffer(n);
	}

    /**
     * Get the read Buffer
     * @return The read Buffer
     */
	public StringBuffer getReadBuffer() {
		return readBuffer;
	}

    /**
     * Get the write Buffer
     * @return The write Buffer
     */
	public StringBuffer getWriteBuffer() {
		return writeBuffer;
	}
	
	
}
