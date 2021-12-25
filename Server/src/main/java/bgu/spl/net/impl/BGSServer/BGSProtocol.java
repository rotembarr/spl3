package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.BGSServer.Messages.BGSMessage;

public class BGSProtocol implements BidiMessagingProtocol<BGSMessage> {
    
    private boolean shouldTerminate = false;
    private int connectionId = -1;
    private Connections<BGSMessage> connections = null;

    @Override
    public void start(int connectionId, Connections<BGSMessage> connections) {
        this.shouldTerminate = false;
        this.connectionId = connectionId;
        this.connections = connections;
    }

    @Override
    public void process(BGSMessage msg) {
        // shouldTerminate = "bye".equals(msg);
        // System.out.println("[" + LocalDateTime.now() + "]: " + msg);
        // TODO
    }    


    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
