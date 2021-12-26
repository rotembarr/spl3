package bgu.spl.net.impl.BGSServer.Messages;

public class NotificationMessage extends BGSMessage{
    byte type;
    String postingUser;
    String content;
    
    public NotificationMessage(String src) {
        super(BGSMessage.Opcode.NOTIFICATION);
        this.type = (byte)src.charAt(0);
        String[] parts = src.split("\0");
        this.postingUser = parts[0];
        this.content = parts[1];
    }

    public String encode() {
        return null;
    }

    public String toString() {
        return "PostMessage(" + this.hashCode() + "): " + this.type + " " + this.postingUser + " " + this.content;
    }
} 
