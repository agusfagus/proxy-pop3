package handler;

import proxy.Pop3Proxy;

import java.nio.channels.Selector;

/**
 * A Handler for a proxy specific service
 */
public abstract class ProxyHandler implements Handler {

    /**
     * The Selector corresponding to the given service
     */
    protected Selector selector;

    /**
     * The Proxy corresponding to the given service
     */
    protected Pop3Proxy proxy;

    /**
     * The buffer size
     */
    protected int bufferSize;

}
