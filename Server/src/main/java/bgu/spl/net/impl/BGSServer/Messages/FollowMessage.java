package bgu.spl.net.impl.BGSServer.Messages;

public class FollowMessage extends BGSMessage {
    private final byte follow;
    private final String username;

    public byte getFollow() {
        return this.follow;
    }

    public String getUsername() {
        return this.username;
    }

    public FollowMessage(byte follow, String username) {
        super(BGSMessage.Opcode.FOLLOW);
        this.follow = follow;
        this.username = username;
    }
    
    public static FollowMessage decode(String src) {
        return new FollowMessage((byte)src.charAt(0), src.substring(1, src.length()-1));
    }

    public String encode() {
        return BGSMessage.opcodeToString(this.opcode) + (char)this.follow + this.username + '\0';
    }
    
    public String toString() {
        return "Follow(" + this.hashCode() + "):" + this.follow + " " + this.username;
    }
} 
