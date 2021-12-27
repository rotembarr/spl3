package bgu.spl.net.impl.BGSServer.Messages;

public class NotificationMessage extends BGSMessage{
    private byte type;
    private String postingUser;
    private String content;

    public byte getType() {
        return this.type;
    }

    public String getPostingUser() {
        return this.postingUser;
    }

    public String getContent() {
        return this.content;
    }
    
    public NotificationMessage(String src) {
        super(BGSMessage.Opcode.NOTIFICATION);
        this.type = (byte)src.charAt(0);
        String[] parts = src.split("\0");
        this.postingUser = parts[0];
        this.content = parts[1];
    }

    public String encode() {
        return BGSMessage.opcodeToString(this.opcode) + this.type + this.postingUser + '\0' + this.content + '\0';
    }

    public String toString() {
        return "PostMessage(" + this.hashCode() + "): " + this.type + " " + this.postingUser + " " + this.content;
    }
} 
