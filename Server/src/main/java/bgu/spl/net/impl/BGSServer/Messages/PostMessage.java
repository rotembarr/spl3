package bgu.spl.net.impl.BGSServer.Messages;

public class PostMessage extends BGSMessage{
    private String content;

    public String getContent() {
        return this.content;
    }
    
    public PostMessage(String content) {
        super(BGSMessage.Opcode.POST);
        this.content = content;
    }

    public static PostMessage decode(String src) {
        return new PostMessage(src.substring(0, src.length()-1));
    }

    public String encode() {
        return BGSMessage.opcodeToString(this.opcode) + this.content + '\0';
    }
    
    public String toString() {
        return "PostMessage(" + this.hashCode() + "): " + this.content;
    }
} 
