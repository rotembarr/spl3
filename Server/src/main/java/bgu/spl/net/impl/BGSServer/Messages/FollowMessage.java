package bgu.spl.net.impl.BGSServer.Messages;

public class FollowMessage extends BGSMessage {
    private final char follow;
    private final String username;

    public char getFollow() {
        return this.follow;
    }

    public String getUsername() {
        return this.username;
    }

    public FollowMessage(String src) {
        super(BGSMessage.Opcode.FOLLOW);
        this.follow = src.charAt(0);
        this.username = src.substring(1, src.length()-1);
    }

    public String encode() {
        return BGSMessage.opcodeToString(this.opcode) + (byte)this.follow + this.username + '\0';
    }
    
    public String toString() {
        return "Follow(" + this.hashCode() + "):" + this.follow + " " + this.username;
    }
} 
