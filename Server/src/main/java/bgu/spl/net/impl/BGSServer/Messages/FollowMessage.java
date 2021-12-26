package bgu.spl.net.impl.BGSServer.Messages;

public class FollowMessage extends BGSMessage {
    char follow;
    String username;
    
    public FollowMessage(String src) {
        super(BGSMessage.Opcode.FOLLOW);
        this.follow = src.charAt(0);
        this.username = src.substring(2);
    }


    public String encode() {
        return null;
    }
    
    public String toString() {
        return this.follow + " " + this.username;
    }

    public String ack() {
        // 
        // 04 is FOLLOW Opcode.
        return "04" + this.username + '\0'; 
    }
} 
