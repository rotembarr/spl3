package bgu.spl.net.impl.BGSServer.Messages;

public class BlockMessage extends BGSMessage {
    private final String username;

    public Object getUsername() {
        return this.username;
    }

    public BlockMessage(String username) {
        super(BGSMessage.Opcode.FOLLOW);
        this.username = username;
    }

    public static BlockMessage decode(String src) {
        return new BlockMessage(src.substring(0, src.length()-1));
    }

    public String encode() {
        return BGSMessage.opcodeToString(this.opcode) + this.username + '\0';
    }
    
    public String toString() {
        return "Blcok(" + this.hashCode() + "): " + this.username;
    }
} 
