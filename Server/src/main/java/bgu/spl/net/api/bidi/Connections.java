package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.bidi.ConnectionHandler;

public interface Connections<T> {

    // TODO - ask if posible
    int connect(ConnectionHandler<T> handler);

    boolean send(int connectionId, T msg);

    void broadcast(T msg);

    void disconnect(int connectionId);
}
