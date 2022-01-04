package bgu.spl.net.impl.BGSServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import bgu.spl.net.impl.BGSServer.Messages.BGSMessage;
import bgu.spl.net.impl.BGSServer.Messages.FollowMessage;
import bgu.spl.net.impl.BGSServer.Messages.LoginMessage;
import bgu.spl.net.impl.BGSServer.Messages.LogoutMessage;
import bgu.spl.net.impl.BGSServer.Messages.PMMessage;
import bgu.spl.net.impl.BGSServer.Messages.PostMessage;
import bgu.spl.net.impl.BGSServer.Messages.BGSMessage.Opcode;

public class BGSEncDecTest {
    BGSEncoderDecoder encdec;

    public BGSEncDecTest() {
        this.encdec = new BGSEncoderDecoder();
    }
    
    @Test
    public void testLogin() {
        String src1 = BGSMessage.opcodeToString(Opcode.LOGIN) + "aa" + '\0' + "bb" + '\0' + (char)0 + ';';
        String src2 = BGSMessage.opcodeToString(Opcode.LOGIN) + "ss" + '\0' + "rr" + '\0' + (char)1 + ';';
        
        byte[] arr1 = src1.getBytes();
        for (int i = 0; i < arr1.length - 1; i++) {
            assertEquals(null, this.encdec.decodeNextByte(arr1[i]));
        }

        LoginMessage msg1 = (LoginMessage)this.encdec.decodeNextByte(arr1[arr1.length-1]);
        assertEquals("aa", msg1.getUsername());
        assertEquals("bb", msg1.getPassword());
        assertEquals(0, msg1.getCaptcha());

        byte[] arr2 = src2.getBytes();
        for (int i = 0; i < arr2.length - 1; i++) {
            assertEquals(null, this.encdec.decodeNextByte(arr2[i]));
        }
        LoginMessage msg2 = (LoginMessage)this.encdec.decodeNextByte(arr2[arr2.length-1]);
        assertEquals("ss", msg2.getUsername());
        assertEquals("rr", msg2.getPassword());
        assertEquals(1, msg2.getCaptcha());

    }

    @Test
    public void testLogout() {
        String src1 = BGSMessage.opcodeToString(Opcode.LOGOUT) + ';';
        byte[] arr1 = src1.getBytes();

        for (int i = 0; i < arr1.length - 1; i++) {
            assertEquals(null, this.encdec.decodeNextByte(arr1[i]));
        }
        LogoutMessage msg1 = (LogoutMessage)this.encdec.decodeNextByte(arr1[arr1.length-1]);
        assertNotEquals(null, msg1);
    }

    @Test
    public void testFollow() {
        String src = BGSMessage.opcodeToString(Opcode.FOLLOW) + (char)1 + "blabla" + '\0' + ';';
        
        byte[] arr = src.getBytes();
        for (int i = 0; i < arr.length - 1; i++) {
            assertEquals(null, this.encdec.decodeNextByte(arr[i]));
        }
        BGSMessage msg = this.encdec.decodeNextByte(arr[arr.length-1]);
        assertTrue("Bad type", msg instanceof FollowMessage);
        FollowMessage castmsg = (FollowMessage)msg;
        assertEquals(1, castmsg.getFollow());
        assertEquals("blabla", castmsg.getUsername());
    }

    @Test
    public void testPost() {
        String content = "@ asdas ad Kaki asfda asddas @s dvsd";
        String src = BGSMessage.opcodeToString(Opcode.POST) + content + '\0' + ';';
        
        byte[] arr = src.getBytes();
        for (int i = 0; i < arr.length - 1; i++) {
            assertEquals(null, this.encdec.decodeNextByte(arr[i]));
        }
        BGSMessage msg = this.encdec.decodeNextByte(arr[arr.length-1]);
        assertTrue("Bad type", msg instanceof PostMessage);
        PostMessage castmsg = (PostMessage)msg;
        assertEquals(content, castmsg.getContent());
    }


    @Test
    public void testPM() {
        String username = "asd sdfasd  dad";
        String dateandtime = "11-5-2222";
        String content = "@ asdas ad Kaki asfda asddas @s dvsd";
        String src = BGSMessage.opcodeToString(Opcode.PM) + username + '\0' + content + '\0' + dateandtime + '\0' + ';';
        
        byte[] arr = src.getBytes();
        for (int i = 0; i < arr.length - 1; i++) {
            assertEquals(null, this.encdec.decodeNextByte(arr[i]));
        }
        BGSMessage msg = this.encdec.decodeNextByte(arr[arr.length-1]);
        assertTrue("Bad type", msg instanceof PMMessage);
        PMMessage castmsg = (PMMessage)msg;
        assertEquals(username, castmsg.getUsername());
        assertEquals(content, castmsg.getContent());
        assertEquals(dateandtime, castmsg.getSendingDateAndTime());
    }

}


