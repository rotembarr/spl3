package bgu.spl.net.impl.BGSServer.Messages;

public class PostMessage extends BGSMessage{
    String content;
    
    public PostMessage(String src) {
        super(BGSMessage.Opcode.POST);
        this.content = src.substring(0, -1);
    }

    public String encode() {
        return null;
    }
    
    public String toString() {
        return "PostMessage(" + this.hashCode() + "): " + this.content;
    }
} 
