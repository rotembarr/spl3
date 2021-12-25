package bgu.spl.net.impl.BGSServer.Messages;

public class FollowMessage extends BaseMessage{
    char follow;
    String username;
    
    public FollowMessage(String src) {
        this.follow = src.charAt(0);
        this.username = src.substring(1);
    }

    public String toString() {
        return this.follow + " " + this.username;
    }

    public String ack() {
        return "04" + this.username + '\0';
    }
} 
