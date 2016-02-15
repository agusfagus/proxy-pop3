package core;

import admin.Admin;
import config.Configuration;
import connection.AdminConnection;
import connection.Connection;
import connection.Server;
import handler.*;
import org.apache.log4j.Logger;
import proxy.ClientProxy;
import proxy.ServerProxy;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    /**
     * The class Logger
     */
    private static transient Logger LOGGER = Logger.getLogger(Main.class);


    /**
     * A map from usernames to Servers
     */
	private static Map<String, Server> userToServerMap;

    /**
     * A variable indicating that the execution should halt
     */
    private static AtomicBoolean finished;

    /**
     * Set up and launch threads
     * @param args
     */
	public static void main(String[] args) {
		LOGGER.info("Starting proxy.");
		
		// Create selectors to multiplex listening sockets and connections
        try {
            Selector clientSelector = Selector.open();
            Selector serverSelector = Selector.open();
            Selector adminSelector = Selector.open();
		
            userToServerMap = new ConcurrentHashMap<>();
            finished = new AtomicBoolean(false);
            Admin admin = new Admin(userToServerMap, adminSelector, finished);
            ClientProxy clientProxy = new ClientProxy(userToServerMap, clientSelector, serverSelector);
            ServerProxy serverProxy = new ServerProxy(userToServerMap, clientSelector, serverSelector);

            // Create handlers that will implement the protocol
            ProxyHandler clientHandler = new ClientHandler(clientSelector, clientProxy, userToServerMap);
            ProxyHandler serverHandler = new ServerHandler(serverSelector, serverProxy);
            ProxyHandler adminHandler = new AdminHandler(adminSelector, admin);

            // Create threads that will solve the requests & responses
            ProxyThread clientThread = new ProxyThread(clientSelector, clientHandler, Configuration.getInstance().getDefaultListenerClientPort(), finished);
            ProxyThread serverThread = new ProxyThread(serverSelector, serverHandler, Configuration.getInstance().getDefaultListenerServerPort(), finished);
            ProxyThread adminThread = new ProxyThread(adminSelector, adminHandler, Configuration.getInstance().getDefaultListenerAdminPort(), finished);


            // Run threads previously created
            LOGGER.info("Starting client-side listeners.");
            clientThread.start();
            LOGGER.info("Starting server-side listeners.");
            serverThread.start();
            LOGGER.info("Starting Admin listeners.");
            adminThread.start();
            LOGGER.info("Proxy ready!");

            try {
                adminThread.join();
                clientThread.join();
                serverThread.join();
                for (SelectionKey key : clientSelector.keys()) {
                    Connection connection = ((Connection) key.attachment());
                    if (connection != null) {
                        key.cancel();
                    }
                }
                clientSelector.keys();
                for (SelectionKey key : serverSelector.keys()) {
                    Connection connection = ((Connection) key.attachment());
                    if (connection != null) {
                        key.cancel();
                    }
                }
                serverSelector.close();
                for (SelectionKey key : adminSelector.keys()) {
                    AdminConnection connection = ((AdminConnection) key.attachment());
                    if (connection != null) {
                        key.cancel();
                    }
                }
                adminSelector.close();
            } catch (InterruptedException e) {

            }


        } catch (IOException e) {
            LOGGER.error("Couldn't open selectors");
            e.printStackTrace();
        }
	}
}