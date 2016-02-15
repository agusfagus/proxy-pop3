package config;

import connection.Server;
import mail.Mail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton
 * Global configuration of the Proxy
 */
public class Configuration {

    /**
     * The Configuration instance
     */
    private static Configuration instance;

    /**
     * The default Server
     */
    private Server defaultServer;

    /**
     * The default port on which proxy listens to Client connections
     */
    private int defaultListenerClientPort;

    /**
     * The default port on which proxy listens to Server connections
     */
    private int defaultListenerServerPort;

    /**
     * The default port on which proxy listens to Admin connections
     */
    private int defaultListenerAdminPort;

    /**
     * The default buffer size
     */
    private int bufferSize;

    /**
     * The default Admin password
     */
    private String adminPassword;

    /**
     * Loads default configuration from properties file
     */
    private Configuration() {
        Properties properties = new Properties();
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("general.properties");
            properties.load(is);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        this.defaultServer = new Server(properties.getProperty("defaultServerName", "localhost"), Integer.valueOf(properties.getProperty("defaultPOP3ServerPort", "110")));
        this.defaultListenerClientPort = Integer.valueOf(properties.getProperty("defaultListenerClientPort", "4040"));
        this.defaultListenerServerPort = Integer.valueOf(properties.getProperty("defaultListenerServerPort", "4041"));
        this.defaultListenerAdminPort = Integer.valueOf(properties.getProperty("defaultListenerAdminPort", "4042"));
        this.bufferSize = Integer.valueOf(properties.getProperty("bufferSize", "1024")) * 1024;
        this.adminPassword = properties.getProperty("adminPassword", "protos");
        Mail.cantMails = Integer.valueOf(properties.getProperty("cantMails"));
    }

    /**
     * Get singleton
     * @return The Configuration instance
     */
    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    /**
     * Get the default Server
     * @return The default Server
     */
    public Server getDefaultServer() {
        return this.defaultServer;
    }

    /**
     * Get the default port on which the proxy listens to Client connections
     * @return The default port on which proxy listens to Client connections
     */
    public int getDefaultListenerClientPort() {
        return this.defaultListenerClientPort;
    }

    /**
     * Get the default port on which the proxy listens to Server connections
     * @return The default port on which proxy listens to Server connections
     */
    public int getDefaultListenerServerPort() {
        return this.defaultListenerServerPort;
    }

    /**
     * Get the default port on which the proxy listens to Admin connections
     * @return The default port on which proxy listens to Admin connections
     */
    public int getDefaultListenerAdminPort() {
        return this.defaultListenerAdminPort;
    }

    /**
     * Get the default buffer size
     * @return The default buffer size
     */
    public int getBufferSize(){
        return this.bufferSize;
    }

    /**
     * Get the default Admin password
     * @return The default Admin password
     */
    public String getAdminPassword(){
        return this.adminPassword;
    }
}

