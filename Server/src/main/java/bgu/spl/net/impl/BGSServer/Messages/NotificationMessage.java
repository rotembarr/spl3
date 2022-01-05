package bgu.spl.net.impl.BGSServer.Messages;

public class NotificationMessage extends BGSMessage{
    private final byte type;
    private final String postingUser;
    private final String content;

    public byte getType() {
        return this.type;
    }

    public String getPostingUser() {
        return this.postingUser;
    }

    public String getContent() {
        return this.content;
    }
    
    // public NotificationMessage(String src) {
    //     super(BGSMessage.Opcode.NOTIFICATION);
    //     this.type = (byte)src.charAt(0);
    //     String[] parts = src.split("\0");
    //     this.postingUser = parts[0];
    //     this.content = parts[1];
    // }

    public NotificationMessage(byte type, String postingUser, String content) {
        super(BGSMessage.Opcode.NOTIFICATION);
        this.type = type;
        this.postingUser = postingUser;
        this.content = content;
    }

    public boolean equals(Object other) {
        if (other instanceof NotificationMessage) {
            return (this.type == ((NotificationMessage)other).type && 
                this.postingUser.equals(((NotificationMessage)other).postingUser) && 
                this.content.equals(((NotificationMessage)other).content)
            );
        } else {
            return false;
        }
    }


    public String encode() {
        return BGSMessage.opcodeToString(this.opcode) + (char)this.type + this.postingUser + '\0' + this.content + '\0';
    }

    public String toString() {
        return "PostMessage(" + this.hashCode() + "): " + this.type + " " + this.postingUser + " " + this.content;
    }
} 
