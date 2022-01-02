package bgu.spl.net.impl.BGSServer.Messages;

import bgu.spl.net.impl.BGSServer.Filter;

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
    
    public void filter() {
        this.content = Filter.filter(this.content);
    }

    public PMMessage(String username, String content, String sendingDateAndTime) {
        super(BGSMessage.Opcode.PM);
        this.username = username;
        this.content = content;
        this.sendingDateAndTime = sendingDateAndTime;
    }
    
    public static PMMessage decode(String src) {
        String[] parts = src.split("\0"); 
        return new PMMessage(parts[0], parts[1], parts[2]);
    }

    public String encode() {
        return BGSMessage.opcodeToString(this.opcode) + this.username + '\0' + this.content + '\0' + this.sendingDateAndTime + '\0';
    }

    public String toString() {
        return "PMMessage(" + this.hashCode() + "): " + this.username + " " + this.content + " " + this.sendingDateAndTime;
    }
} 
