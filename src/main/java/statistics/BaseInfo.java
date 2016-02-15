package statistics;

/**
 * Gathers the base information regarding Requests and Responses for the
 * Statistics modules
 */
public class BaseInfo {
	
	/**
	 * Number of calls to gather data
	 */
	private int count = 0;
	/**
	 * Total number of connections established
	 */
	private int connections = 0;
	/**
	 * Total number of bytes transfered
	 */
	private int bytesTransf = 0;
	
	/**
	 * Adds a call
	 */
	public void addCall() {
		count++;
	}
	
	/**
	 * Adds a connection
	 */
	public void addConnection() {
		connections++;
	}
	
	/**
	 * Adds the bytes to the gathered data
	 * @param size number of bytes transfered
	 */
	public void addBytes(int size) {
		bytesTransf += size;
	}
	
	/**
	 * Gets the number requests or responses
	 * @return number of requests or responses
	 */
	public int getCount() {
		return count;
	}
	
	/**
	 * Gets the number of connections established
	 * @return number of connections established
	 */
	public int getConnections() {
		return connections;
	}
	
	/**
	 * Gets the number of bytes transfered in total thus far
	 * @return total number of bytes transfered
	 */
	public int getBytesTransf() {
		return bytesTransf;
	}
	

}
