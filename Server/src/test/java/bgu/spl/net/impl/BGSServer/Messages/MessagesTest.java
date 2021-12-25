package bgu.spl.net.impl.BGSServer.Messages;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MessagesTest {
    
    @Test
    public void TestRegisterData() {
        String src = "aaa" + '\0' + "bbb" + '\0' + "ccc" + '\0';
        String[] parts = src.split("\0");
        System.out.println(parts.length);
        for (String part : parts) {
            System.out.println(part);
        }

        // String s = Short.((short)9);
        // System.out.println(s.length());
        // System.out.println(s);
    }
}
