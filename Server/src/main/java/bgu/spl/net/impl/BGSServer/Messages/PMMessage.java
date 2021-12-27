package bgu.spl.net.impl.BGSServer.Messages;

public class PMMessage extends BGSMessage{
    private String username;
    private String content;
    private String sendingDateAndTime;

    public String getUsername() {
        return this.username;
    }

    public String getContent() {
        return this.content;
    }

    public String getSendingDateAndTime() {
        return this.sendingDateAndTime;
    }
    
    public PMMessage(String src) {
        super(BGSMessage.Opcode.PM);
        String[] parts = src.split("\0");
        this.username = parts[0];
        this.content = parts[1];
        this.sendingDateAndTime = parts[2];
    }

    public String encode() {
        return BGSMessage.opcodeToString(this.opcode) + this.username + '\0' + this.content + '\0' + this.sendingDateAndTime + '\0';
    }

    public String toString() {
        return "PMMessage(" + this.hashCode() + "): " + this.username + " " + this.content + " " + this.sendingDateAndTime;
    }
} 
