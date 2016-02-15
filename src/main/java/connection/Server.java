package connection;

public class Server {

    /**
     * The server URL
     */
    private String url;

    /**
     * The server port
     */
    private int port;

    /**
     * Create the Server with the URL and port
     * @param url The server URL
     * @param port The server port
     */
    public Server (String url, int port) {
        this.url = url;
        this.port = port;
    }

    /**
     * Get the server URL
     * @return The server URL
     */
    public String getName() {
        return url;
    }

    /**
     * Set the Server URL
     * @param url The server URL
     */
    public void setName(String url) {
        this.url = url;
    }

    /**
     * Get the Server port
     * @return The Server port
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the Server port
     * @param port The Server port
     */
    public void setPort(int port) {
        this.port = port;
    }
}
