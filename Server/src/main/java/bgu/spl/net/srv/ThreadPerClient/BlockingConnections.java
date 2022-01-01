package bgu.spl.net.srv.ThreadPerClient;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.bidi.ConnectionHandler;

public class BlockingConnections<T> implements Connections<T>{

    private Map<Integer, ConnectionHandler<T>> idToHandlerMap = null;
    private int connectionsCounter; 

    public BlockingConnections() {
        this.idToHandlerMap = new HashMap<Integer, ConnectionHandler<T>>();
        this.connectionsCounter = 0;
    }

    public synchronized boolean send(int connectionId, T msg) {
        ConnectionHandler<T> handler = idToHandlerMap.get(connectionId);
        
        if (handler == null) {
            return false;
        }

        handler.send(msg);
        return true;
    }

    public synchronized void broadcast(T msg) {
        Collection<ConnectionHandler<T>> handlers = this.idToHandlerMap.values();
        for (Iterator<ConnectionHandler<T>> iter = handlers.iterator(); iter.hasNext(); ) {
            ConnectionHandler<T> handler = iter.next();
            handler.send(msg);
        }
    }


    public synchronized int connect(ConnectionHandler<T> handler) {
        if (this.idToHandlerMap.containsValue(handler)) {
            return -1;
        } else {
            this.idToHandlerMap.put(this.connectionsCounter, handler);
            return ++this.connectionsCounter;
        }
    }

    public synchronized boolean isConnected(int connectionId) {
        return this.idToHandlerMap.containsKey(connectionId);
    }


    public synchronized void disconnect(int connectionId) {
        this.idToHandlerMap.remove(connectionId);
    }    
}
