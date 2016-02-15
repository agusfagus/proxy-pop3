package mail;

/**
 * An Image belonging to a Mail
 */
public class Image {

    /**
     * The initial line of the Image
     */
	private int startLine;

    /**
     * The last line of the Image
     */
    private int endLine;

    /**
     * Get the initial line of the Image
     * @return The initial line of the Image
     */
	public int getStartLine() {
		return startLine;
	}

    /**
     * Get the last line of the Image
     * @return The last line of the Image
     */
	public int getEndLine() {
		return endLine;
	}

    /**
     * Set the initial line of the Image
     * @param startLine The initial line of the Image
     */
	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

    /**
     * Set the last line of the Image
     * @param endLine The last line of the Image
     */
	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}
}
