package bgu.spl.net.impl.BGSServer.Messages;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class StatMessage extends BGSMessage{
    private List<String> usernames;

    public List<String> getUsernames() {
        return this.usernames;
    }

    public StatMessage(List<String> usernames) {
        super(BGSMessage.Opcode.STAT);
        this.usernames = usernames;
    }
    
    public static StatMessage decode(String src) {
        String[] names = src.substring(0, src.length()-1).split("|");
    
        List<String> usernames = new LinkedList<String>();
        for (int i = 0; i < names.length; i++) {
            usernames.add(names[i]);
        }

        return new StatMessage(usernames);
    }

    private String concatenateUsers() {
        String out = "";
        for (Iterator<String> iter = this.usernames.iterator(); iter.hasNext(); ) {
            out += iter.next() + "|";
        }
        return (out.substring(0, out.length()-1));
    }

    public String encode() {
        return BGSMessage.opcodeToString(this.opcode) + this.concatenateUsers();
    }
    
    public String toString() {
        return "StatMessage(" + this.hashCode() + "): " + this.concatenateUsers();
    }
} 
