package bgu.spl.net.impl.BGSServer.Messages;

public class PMMessage extends BGSMessage{
    String username;
    String content;
    String sendingDateAndTime;
    
    public PMMessage(String src) {
        super(BGSMessage.Opcode.PM);
        String[] parts = src.split("\0");
        this.username = parts[0];
        this.content = parts[1];
        this.sendingDateAndTime = parts[2];
    }

    public String encode() {
        return null;
    }

    public String toString() {
        return "PMMessage(" + this.hashCode() + "): " + this.username + " " + this.content + " " + this.sendingDateAndTime;
    }
} 
