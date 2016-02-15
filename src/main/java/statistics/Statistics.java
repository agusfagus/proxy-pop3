package statistics;

/**
 * Singleton
 * Statistics gathering and processing
 */
public class Statistics {

	/**
	 * The Statistics instance
	 */
	private static Statistics instance;
	/**
	 * The Requests information
	 */
	private BaseInfo reqInfo;
	/**
	 * The Responses information
	 */
	private BaseInfo resInfo;
	/**
	 * The Status Codes information
	 */
	private ExtraInfo codesInfo;
	/**
	 * The Status Codes information
	 */
	private ExtraInfo authInfo;
	
	/**
	 * Creates the classes which hold the information required for the statistics
	 */
	private Statistics() {
		this.reqInfo = new BaseInfo();
		this.resInfo = new BaseInfo();
		this.codesInfo = new ExtraInfo();
		this.authInfo = new ExtraInfo();
	}
	
	/**
	 * Get Singleton
	 * @return the Statistics instance
	 */
	public synchronized static Statistics getInstance() {
		if (instance == null) {
            instance = new Statistics();
        }
        return instance;
	}
	
	/**
	 * Adds the call to the information gathered
	 */
	public synchronized void addRequest() {
		reqInfo.addCall();
	}
	
	/**
	 * Adds the call to the information gathered with its corresponding Status Code
	 */
	public synchronized void addResponse(StatusCode code) {
		resInfo.addCall();
		codesInfo.add(code);
	}
	
	/**
	 * Adds an established connection to the gathered data
	 */
	public synchronized void addConnection() {
		reqInfo.addConnection();
	}
	
	/**
	 * Process the request to the information gathered
	 * @param size size in bytes of the request
	 */
	public synchronized void processRequest(int size) {
		reqInfo.addBytes(size);
	}
	
	/**
	 * Process the response to the information gathered
	 * @param size size in bytes of the response
	 */
	public synchronized void processResponse(int size) {
		resInfo.addBytes(size);
	}
	
	/**
	 * Adds the authentication Status Code from the connection to the gathered data
	 */
	public synchronized void addAuth(StatusCode code) {
		authInfo.add(code);
	}
	
	/**
	 * Retrieves the information gathered thus far in a condensed way
	 * @return String containing the information gathered
	 */
	public synchronized String getCondensedStatistics() {
		StringBuffer str = new StringBuffer();
		int totalCount = reqInfo.getCount() + resInfo.getCount();
		int totalBytes = reqInfo.getBytesTransf() + resInfo.getBytesTransf();
		str.append("STATSSTART \r\n");
		str.append("Totals: \r\n" + totalCount + "\r\n" + totalBytes + "\r\n" + reqInfo.getConnections() + "\r\n");
		str.append("Requests: \r\n" + reqInfo.getCount() + "\r\n" + reqInfo.getBytesTransf() + "\r\n");
		str.append("Responses: \r\n" + resInfo.getCount() + "\r\n" + resInfo.getBytesTransf() + "\r\n");
		str.append("StatusCodes Histogram: \r\n" + codesInfo.getHistogram());
		str.append("Un/Authenticated connections Histogram: \r\n" + authInfo.getHistogram());
		str.append("STATSEND \r\n");
		
		return str.toString();
	}
	
}
