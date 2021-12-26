package bgu.spl.net.srv.ThreadPerClient;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.bidi.ConnectionHandler;

public class BlockingConnections<T> implements Connections<T>{

    Map<Integer, ConnectionHandler<T>> idToHandlerMap = null;

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

    public synchronized boolean connect(int connectionId, ConnectionHandler<T> handler) {
        if (this.idToHandlerMap.containsKey(connectionId)) {
            return false;
        } else {
            this.idToHandlerMap.put(connectionId, handler);
            return true;
        }
    }

    public synchronized void disconnect(int connectionId) {
        this.idToHandlerMap.remove(connectionId);
    }
    
}
