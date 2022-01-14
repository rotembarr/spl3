package bgu.spl.net.srv;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.bidi.ConnectionHandler;

public class ConnectionsImpl<T> implements Connections<T>{

    private Map<Integer, ConnectionHandler<T>> idToHandlerMap = null;
    private int connectionsCounter; 

    public ConnectionsImpl() {
        this.idToHandlerMap = new ConcurrentHashMap<Integer, ConnectionHandler<T>>();
        this.connectionsCounter = 0;
    }

    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> handler = idToHandlerMap.get(connectionId);
        
        if (handler == null) {
            return false;
        }

        synchronized(handler) {

            // Check if disconnect doesnt happend.
            if (!this.idToHandlerMap.containsValue(handler)) {
                return false;
            }

            handler.send(msg);
            return true;
        }
    }

    public void broadcast(T msg) {
        Collection<ConnectionHandler<T>> handlers = this.idToHandlerMap.values();
        for (Iterator<ConnectionHandler<T>> iter = handlers.iterator(); iter.hasNext(); ) {
            ConnectionHandler<T> handler = iter.next();
            handler.send(msg);
        }
    }


    public int connect(ConnectionHandler<T> handler) {
        if (this.idToHandlerMap.containsValue(handler)) {
            return -1;
        } else {
            this.idToHandlerMap.put(this.connectionsCounter, handler);
            return this.connectionsCounter++;
        }
    }

    public boolean isConnected(int connectionId) {
        return this.idToHandlerMap.containsKey(connectionId);
    }


    public void disconnect(int connectionId) {
        this.idToHandlerMap.remove(connectionId);
    }    

    // public boolean sendAndDisconnect(int connectionId, T msg) {
    //     ConnectionHandler<T> handler = idToHandlerMap.get(connectionId);
        
    //     if (handler == null) {
    //         return false;
    //     }

    //     synchronized(handler) {

    //         // Check if disconnect doesnt happend.
    //         if (!this.idToHandlerMap.containsValue(handler)) {
    //             return false;
    //         }

    //         handler.send(msg);

    //         // This is the additional!!!
    //         this.disconnect(connectionId);

    //         return true;
    //     }
    // }
}