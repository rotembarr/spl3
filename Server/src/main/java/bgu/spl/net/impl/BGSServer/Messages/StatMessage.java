package bgu.spl.net.impl.BGSServer.Messages;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class StatMessage extends BGSMessage{
    private List<String> usernames;
    
    public StatMessage(String src) {
        super(BGSMessage.Opcode.STAT);
        String[] names = src.substring(0, -1).split("|");

        this.usernames = new LinkedList<String>();
        for (int i = 0; i < names.length; i++) {
            this.usernames.add(names[i]);
        }
    }

    public String encode() {
        return null;
    }

    public String toString() {
        String out = "";
        for (Iterator<String> iter = this.usernames.iterator(); iter.hasNext(); ) {
            out += iter.next() + "|";
        }
        return "StatMessage(" + this.hashCode() + "): " + out;
    }
} 
