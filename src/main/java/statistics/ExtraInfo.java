package statistics;

/**
 * Gathers extra information regarding Status Codes for the
 * Statistics modules
 */
public class ExtraInfo {
	
	/**
	 * Structure gathering the number of responses with each Status Code
	 */
	private int[] codesCount = new int[StatusCode.values().length];

	/**
	 * Adds the Status Code used to the gathered data
	 * @param code Status Code used
	 */
	public void add(StatusCode code) {
		codesCount[code.ordinal()]++;
	}

	/**
	 * Retrieves the information gathered thus far in the form of an Histogram
	 * @return String containing the information gathered
	 */
	public StringBuffer getHistogram() {
		int sum = 0; 
		for(int i : codesCount)
			sum += i;
		
		StringBuffer str = new StringBuffer();
		for(StatusCode code : StatusCode.values())
			str.append(code.name() + " " + (codesCount[code.ordinal()]/(double)sum) + "\r\n");
			
		return str;
	}
	
}
