package bgu.spl.net.impl.BGSServer.Messages;

public class AckMessage extends BGSMessage{
    String messageOpcode;
    String optional;
    
    // TODO
    public AckMessage(String src) {
        super(BGSMessage.Opcode.ACK);
    }

    public String encode() {
        return null;
    }

} 
