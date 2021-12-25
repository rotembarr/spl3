package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.BGSServer.Messages.BGSMessage;

public class BGSProtocol implements MessagingProtocol<BGSMessage> {
    
    private boolean shouldTerminate = false;

    @Override
    public BGSMessage process(BGSMessage msg) {
        // shouldTerminate = "bye".equals(msg);
        // System.out.println("[" + LocalDateTime.now() + "]: " + msg);
        return null; // TODO
    }    


    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
