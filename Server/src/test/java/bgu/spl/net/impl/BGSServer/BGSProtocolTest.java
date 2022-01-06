package bgu.spl.net.impl.BGSServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Before;
import org.junit.Test;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.BGSServer.Messages.AckMessage;
import bgu.spl.net.impl.BGSServer.Messages.BGSMessage;
import bgu.spl.net.impl.BGSServer.Messages.BlockMessage;
import bgu.spl.net.impl.BGSServer.Messages.ErrorMessage;
import bgu.spl.net.impl.BGSServer.Messages.FollowMessage;
import bgu.spl.net.impl.BGSServer.Messages.LogStatMessage;
import bgu.spl.net.impl.BGSServer.Messages.LoginMessage;
import bgu.spl.net.impl.BGSServer.Messages.LogoutMessage;
import bgu.spl.net.impl.BGSServer.Messages.NotificationMessage;
import bgu.spl.net.impl.BGSServer.Messages.PMMessage;
import bgu.spl.net.impl.BGSServer.Messages.PostMessage;
import bgu.spl.net.impl.BGSServer.Messages.RegisterMessage;
import bgu.spl.net.impl.BGSServer.Messages.StatMessage;
import bgu.spl.net.impl.BGSServer.Messages.BGSMessage.Opcode;
import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.bidi.ConnectionHandler;

public class BGSProtocolTest {

    private class ConnectionsTest implements Connections<BGSMessage> {
        private Map<Integer, ConnectionHandler<BGSMessage>> idToHandlerMap = null;
        private Map<Integer, Queue<BGSMessage>> idToMsgListMap = null;
        private int connectionsCounter; 
    
        public ConnectionsTest() {
            this.idToHandlerMap = new HashMap<Integer, ConnectionHandler<BGSMessage>>();
            this.idToMsgListMap = new HashMap<Integer, Queue<BGSMessage>>();
            this.connectionsCounter = 0;
        }
    
        public void expectSend(int connectionId, BGSMessage msg) {
            this.idToMsgListMap.get(connectionId).add(msg);
        } 

        public boolean send(int connectionId, BGSMessage msg) {
            ConnectionHandler<BGSMessage> handler = idToHandlerMap.get(connectionId);
            
            if (handler == null) {
                return false;
            }
            
            Queue<BGSMessage> queue = this.idToMsgListMap.get(connectionId);
            BGSMessage other = queue.poll();
            assertTrue(msg.equals(other));
            return true;
        }
    
        public void broadcast(BGSMessage msg) {
        }
    
    
        public int connect(ConnectionHandler<BGSMessage> handler) {
            this.idToHandlerMap.put(this.connectionsCounter, handler);
            this.idToMsgListMap.put(this.connectionsCounter, new LinkedList<>());
            return this.connectionsCounter++;
        }
    
        public boolean isConnected(int connectionId) {
            return this.idToHandlerMap.containsKey(connectionId);
        }
    
    
        public void disconnect(int connectionId) {
            this.idToHandlerMap.remove(connectionId);
        }   
        
        public boolean checkAllClear() {
            Collection<Queue<BGSMessage>> queues = this.idToMsgListMap.values();
            for (Queue<BGSMessage> queue : queues) {
                if (queue.size() != 0) {
                    return false;
                }
            }

            return true;

        }

    }

    ////////////////// Test //////////////////
    Map<String, BGSStudent> usernamesToStudentMap = null;
    ConnectionsTest connections = null;

    @Before
    public void setUp() {
        this.usernamesToStudentMap = new ConcurrentHashMap<String, BGSStudent>();
        this.connections = new ConnectionsTest();
    }

    public BGSProtocol createProtocol() {
        BGSProtocol protocol = new BGSProtocol(this.usernamesToStudentMap);
        int id = this.connections.connect(new BlockingConnectionHandler<BGSMessage>(null, connections, null, protocol));
        protocol.start(id, this.connections);
        return protocol;
    }

    @Test
    public void testRegister() {
        BGSProtocol protocol = this.createProtocol();
        RegisterMessage msg = new RegisterMessage("Mor hayafa", "123 ", "29-12-1997");
        
        assertEquals(0, this.usernamesToStudentMap.size());

        // First register should success
        connections.expectSend(protocol.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol.process(msg);
        assertEquals(1, this.usernamesToStudentMap.size());
        
        // Second register should fail
        connections.expectSend(protocol.getConnectionId(), new ErrorMessage(Opcode.REGISTER));
        protocol.process(msg);
        assertEquals(1, this.usernamesToStudentMap.size());

        assertTrue("message left in connection", this.connections.checkAllClear());
    }

    @Test
    public void testLogin() {
        BGSProtocol protocol1 = this.createProtocol();
        BGSProtocol protocol2 = this.createProtocol();

        // RegisterMessage msg2 = new RegisterMessage("Rotem hahatih", "123 ", "29-12-1997");
        
        // Mor register should success
        RegisterMessage msg1 = new RegisterMessage("Mor hayafa", "123 ", "29-12-1997");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol1.process(msg1);
        assertEquals(1, this.usernamesToStudentMap.size());

        // Mor login should fail for bad passwod
        LoginMessage msg2 = new LoginMessage("Mor hayafa", "asas ", (byte)1);
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.LOGIN));
        protocol1.process(msg2);

        // Mor login should fail for bad captcha
        LoginMessage msg3 = new LoginMessage("Mor hayafa", "123 ", (byte)0);
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.LOGIN));
        protocol1.process(msg3);

        // login should fail for bad username
        LoginMessage msg4 = new LoginMessage("hagar hayafa", "123 ", (byte)0);
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.LOGIN));
        protocol1.process(msg4);

        // Mor login should success
        LoginMessage msg5 = new LoginMessage("Mor hayafa", "123 ", (byte)1);
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol1.process(msg5);

        // Mor login should failbecause she loged in
        LoginMessage msg6 = new LoginMessage("Mor hayafa", "123 ", (byte)1);
        connections.expectSend(protocol2.getConnectionId(), new ErrorMessage(Opcode.LOGIN));
        protocol2.process(msg6);

        assertTrue("message left in connection", this.connections.checkAllClear());
    }

    @Test
    public void testLogout() {
        BGSProtocol protocol1 = this.createProtocol();
        BGSProtocol protocol2 = this.createProtocol();
        
        LogoutMessage logoutMsg = new LogoutMessage();

        // logout should fail.
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.LOGOUT));
        protocol1.process(logoutMsg);

        // Mor register should success
        RegisterMessage msg1 = new RegisterMessage("Mor hayafa", "123 ", "29-12-1997");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol1.process(msg1);
        assertEquals(1, this.usernamesToStudentMap.size());

        // logout should fail.
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.LOGOUT));
        protocol1.process(logoutMsg);

        // Mor login should success
        LoginMessage msg2 = new LoginMessage("Mor hayafa", "123 ", (byte)1);
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol1.process(msg2);
        
        // Mor logout should success
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.LOGOUT, ""));
        protocol1.process(logoutMsg);
        
        // logout should fail.
        connections.expectSend(protocol2.getConnectionId(), new ErrorMessage(Opcode.LOGOUT));
        protocol2.process(logoutMsg);
        
        assertTrue("message left in connection", this.connections.checkAllClear());
    }

    @Test
    public void testFollow() {
        BGSProtocol protocol1 = this.createProtocol();
        BGSProtocol protocol2 = this.createProtocol();

        // Mor register should success
        RegisterMessage msg1 = new RegisterMessage("Mor", "123 ", "29-12-1997");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol1.process(msg1);
        assertEquals(1, this.usernamesToStudentMap.size());

        // Rotem register should success
        RegisterMessage msg2 = new RegisterMessage("Rotem", "123 ", "29-12-1997");
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol2.process(msg2);
        assertEquals(2, this.usernamesToStudentMap.size());

        // Mor login should success
        LoginMessage msg3 = new LoginMessage("Mor", "123 ", (byte)1);
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol1.process(msg3);

        // follow after non register should fail.
        FollowMessage msg4= new FollowMessage((byte) 0, "blabla");        
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.FOLLOW));
        protocol1.process(msg4);

        // Follow after non-loged in should success.
        FollowMessage msg5= new FollowMessage((byte) 0, "Rotem");        
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.FOLLOW, "Rotem" + '\0'));
        protocol1.process(msg5);

        // Follow after already folowing should fail.
        FollowMessage msg6= new FollowMessage((byte) 0, "Rotem");        
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.FOLLOW));
        protocol1.process(msg6);
       
        // Rotem login should success
        LoginMessage msg7 = new LoginMessage("Rotem", "123 ", (byte)1);
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol2.process(msg7);

        // Follow after loged in should success.
        FollowMessage msg8= new FollowMessage((byte) 0, "Mor");        
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.FOLLOW, "Mor" + '\0'));
        protocol2.process(msg8);

        assertTrue("message left in connection", this.connections.checkAllClear());
    }

    @Test
    public void testPost() {
        BGSProtocol protocol1 = this.createProtocol();
        BGSProtocol protocol2 = this.createProtocol();
        BGSProtocol protocol3 = this.createProtocol();
        BGSProtocol protocol4 = this.createProtocol();

        // Mor register should success
        RegisterMessage msg1 = new RegisterMessage("Mor", "123 ", "29-12-1997");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol1.process(msg1);
        assertEquals(1, this.usernamesToStudentMap.size());

        ////////// Register
        // Rotem register should success
        RegisterMessage msg2 = new RegisterMessage("Rotem", "123 ", "29-12-1997");
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol2.process(msg2);
        assertEquals(2, this.usernamesToStudentMap.size());

        // Shay register should success
        RegisterMessage msg3 = new RegisterMessage("Shay", "123 ", "29-12-1997");
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol2.process(msg3);
        assertEquals(3, this.usernamesToStudentMap.size());
        
        ////////// Login
        // Mor login should success
        LoginMessage msg4 = new LoginMessage("Mor", "123 ", (byte)1);
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol1.process(msg4);

        // Rotem login should success
        LoginMessage msg5 = new LoginMessage("Rotem", "123 ", (byte)1);
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol2.process(msg5);
        
        // Shay login should success
        LoginMessage msg6 = new LoginMessage("Shay", "123 ", (byte)1);
        connections.expectSend(protocol3.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol3.process(msg6);

        //////// Mor posts to nobody
        PostMessage post0 = new PostMessage("Post 0!!");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.POST, ""));
        protocol1.process(post0);

        //////// Rotem and Shay folow Mor.
        // Rotem's follow shold success
        FollowMessage msg7= new FollowMessage((byte) 0, "Mor");        
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.FOLLOW, "Mor" + '\0'));
        protocol2.process(msg7);
        
        // Shay's follow shold success
        FollowMessage msg8= new FollowMessage((byte) 0, "Mor");        
        connections.expectSend(protocol3.getConnectionId(), new AckMessage(Opcode.FOLLOW, "Mor" + '\0'));
        protocol3.process(msg8);

        ///////// Shay logout
        // Shay logout should success
        LogoutMessage msg9 = new LogoutMessage();
        connections.expectSend(protocol3.getConnectionId(), new AckMessage(Opcode.LOGOUT, ""));
        protocol3.process(msg9);
        
        //////// Mor posts
        // Mor post should success.
        PostMessage msg10 = new PostMessage("Post 1");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.POST, ""));
        connections.expectSend(protocol2.getConnectionId(), new NotificationMessage((byte)1, "Mor", "Post 1"));
        protocol1.process(msg10);
        
        // Login shay again and get message.
        LoginMessage msg11 = new LoginMessage("Shay", "123 ", (byte)1);
        connections.expectSend(protocol4.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        // after login, mor's post should be sent:
        connections.expectSend(protocol4.getConnectionId(), new NotificationMessage((byte)1, "Mor", "Post 1"));
        protocol4.process(msg11);
        
        /////// Rotem post to Shay using tag.
        PostMessage msg12 = new PostMessage("Post 1 @Shay");
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.POST, ""));
        connections.expectSend(protocol4.getConnectionId(), new NotificationMessage((byte)1, "Rotem", "Post 1 @Shay"));
        protocol2.process(msg12);

        assertTrue("message left in connection", this.connections.checkAllClear());
    }



    @Test
    public void testPM() {
        BGSProtocol protocol1 = this.createProtocol();
        BGSProtocol protocol2 = this.createProtocol();
        BGSProtocol protocol3 = this.createProtocol();
        BGSProtocol protocol4 = this.createProtocol();

        ////////// Register
        // Mor register should success
        RegisterMessage msg1 = new RegisterMessage("Mor", "123 ", "29-12-1997");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol1.process(msg1);
        assertEquals(1, this.usernamesToStudentMap.size());

        // Rotem register should success
        RegisterMessage msg2 = new RegisterMessage("Rotem", "123 ", "29-12-1997");
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol2.process(msg2);
        assertEquals(2, this.usernamesToStudentMap.size());

        // Shay register should success
        RegisterMessage msg3 = new RegisterMessage("Shay", "123 ", "29-12-1997");
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol2.process(msg3);
        assertEquals(3, this.usernamesToStudentMap.size());
        
        ////////// Login
        // Mor login should success
        LoginMessage msg4 = new LoginMessage("Mor", "123 ", (byte)1);
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol1.process(msg4);

        // Rotem login should success
        LoginMessage msg5 = new LoginMessage("Rotem", "123 ", (byte)1);
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol2.process(msg5);

        //////// Basic 
        // Shay PM while not connected should fail.
        PMMessage pm0 = new PMMessage("Rotem", "PM 0!!", "12-12-1212");
        connections.expectSend(protocol3.getConnectionId(), new ErrorMessage(Opcode.PM));
        protocol3.process(pm0);
        
        // Rotem PM to non-existing user should fail
        PMMessage pm1 = new PMMessage("blabla", "PM 1!!", "12-12-1212");
        connections.expectSend(protocol2.getConnectionId(), new ErrorMessage(Opcode.PM));
        protocol2.process(pm1);
        
        // Rotem PM not following mor - should fail
        PMMessage pm2 = new PMMessage("Mor", "PM 2!!", "12-12-1212");
        connections.expectSend(protocol2.getConnectionId(), new ErrorMessage(Opcode.PM));
        protocol2.process(pm2);

        //////// Login Shay
        // Shay login should success
        LoginMessage msg6 = new LoginMessage("Shay", "123 ", (byte)1);
        connections.expectSend(protocol3.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol3.process(msg6);

        //////// Rotem and Shay folow Mor.
        // Rotem's follow shold success
        FollowMessage msg7= new FollowMessage((byte) 0, "Mor");        
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.FOLLOW, "Mor" + '\0'));
        protocol2.process(msg7);
        
        // Shay's follow shold success
        FollowMessage msg8= new FollowMessage((byte) 0, "Mor");        
        connections.expectSend(protocol3.getConnectionId(), new AckMessage(Opcode.FOLLOW, "Mor" + '\0'));
        protocol3.process(msg8);

        //////// Rotem PMs to mor
        // PM should success.
        PMMessage pm3 = new PMMessage("Mor", "PM 3!!", "12-12-1212");
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.PM, ""));
        connections.expectSend(protocol1.getConnectionId(), new NotificationMessage((byte)0, "Rotem", "PM 3!! 12-12-1212"));
        protocol2.process(pm3);
        
        ///////// Mor logout
        // logout should success
        LogoutMessage msg9 = new LogoutMessage();
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.LOGOUT, ""));
        protocol1.process(msg9);
        

        //////// Rotem PMs to mor again, now with filter
        // PM should success.
        PMMessage pm4 = new PMMessage("Mor", "PM 4!! Kaki", "12-12-1212");
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.PM, ""));
        protocol2.process(pm4);

        //////// Mor login again and get 
        LoginMessage msg10 = new LoginMessage("Mor", "123 ", (byte)1);
        connections.expectSend(protocol4.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        connections.expectSend(protocol4.getConnectionId(), new NotificationMessage((byte)0, "Rotem", "PM 4!! <filtered> 12-12-1212"));
        protocol4.process(msg10);
        
        assertTrue("message left in connection", this.connections.checkAllClear());
    }

    @Test
    public void testBlock1() {
        BGSProtocol protocol1 = this.createProtocol();
        BGSProtocol protocol2 = this.createProtocol();
        BGSProtocol protocol3 = this.createProtocol();
        BGSProtocol protocol4 = this.createProtocol();

        ////////// Register
        // Mor register should success
        RegisterMessage msg1 = new RegisterMessage("Mor", "123 ", "29-12-1997");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol1.process(msg1);
        assertEquals(1, this.usernamesToStudentMap.size());

        // Rotem register should success
        RegisterMessage msg2 = new RegisterMessage("Rotem", "123 ", "29-12-1997");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol1.process(msg2);
        assertEquals(2, this.usernamesToStudentMap.size());

        // Shay register should success
        RegisterMessage msg3 = new RegisterMessage("Shay", "123 ", "29-12-1997");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol1.process(msg3);
        assertEquals(3, this.usernamesToStudentMap.size());

        // Shay register should success
        RegisterMessage msg4 = new RegisterMessage("Gaya", "123 ", "29-12-1997");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol1.process(msg4);
        assertEquals(4, this.usernamesToStudentMap.size());

        //////// Students.
        BGSStudent mor = this.usernamesToStudentMap.get("Mor");
        BGSStudent rotem = this.usernamesToStudentMap.get("Rotem");
        BGSStudent shay = this.usernamesToStudentMap.get("Shay");

        //////// Login
        // Mor login should success
        LoginMessage msg5 = new LoginMessage("Mor", "123 ", (byte)1);
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol1.process(msg5);

        // Rotem login should success
        LoginMessage msg6 = new LoginMessage("Rotem", "123 ", (byte)1);
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol2.process(msg6);
        
        // Shay login should success
        LoginMessage msg7 = new LoginMessage("Shay", "123 ", (byte)1);
        connections.expectSend(protocol3.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol3.process(msg7);

        // Gaya login should success
        LoginMessage msg8 = new LoginMessage("Gaya", "123 ", (byte)1);
        connections.expectSend(protocol4.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol4.process(msg8);

        //////// Follow
        // Mor follow Gaya should success
        FollowMessage msg9= new FollowMessage((byte) 0, "Gaya");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.FOLLOW, "Gaya" + '\0'));
        protocol1.process(msg9);

        // Mor follow Rotem should success
        FollowMessage msg10= new FollowMessage((byte) 0, "Rotem");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.FOLLOW, "Rotem" + '\0'));
        protocol1.process(msg10);
        assertTrue("mor not following rotem", mor.isFollowing(rotem));
        
        // Rotem follow Mor should success
        FollowMessage msg11= new FollowMessage((byte) 0, "Mor");
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.FOLLOW, "Mor" + '\0'));
        protocol2.process(msg11);
        assertTrue("rotem not following mor", rotem.isFollowing(mor));
        
        // Rotem follow Shay should success
        FollowMessage msg12= new FollowMessage((byte) 0, "Shay");
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.FOLLOW, "Shay" + '\0'));
        protocol2.process(msg12);
        assertTrue("rotem not following shay", rotem.isFollowing(shay));
        
        // Shay follow Gaya should success
        FollowMessage msg13= new FollowMessage((byte) 0, "Gaya");
        connections.expectSend(protocol3.getConnectionId(), new AckMessage(Opcode.FOLLOW, "Gaya" + '\0'));
        protocol3.process(msg13);

        // Gaya follow Mor should success
        FollowMessage msg14= new FollowMessage((byte) 0, "Mor");
        connections.expectSend(protocol3.getConnectionId(), new AckMessage(Opcode.FOLLOW, "Mor" + '\0'));
        protocol3.process(msg14);
        
        //////// Block
        // Mor block blabla should fail.
        BlockMessage msg15 = new BlockMessage("blabla");
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.BLOCK));
        protocol1.process(msg15);
        
        // Mor block Rotem should success
        BlockMessage msg16 = new BlockMessage("Rotem");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.BLOCK, ""));
        protocol1.process(msg16);
        assertFalse("rotem following mor", rotem.isFollowing(mor));
        assertFalse("mor following rotem", mor.isFollowing(rotem));
        assertFalse("rotem blocking mor", rotem.isBlocking(mor));
        assertTrue("mor not blocking rotem", mor.isBlocking(rotem));

        // Mor PM Rotem should Fail
        PMMessage msg17 = new PMMessage("Rotem", "PM 0!!", "12-12-1212");
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.PM));
        protocol1.process(msg17);
        
        // Rotem PM Mor should Fail
        PMMessage msg18 = new PMMessage("Mor", "PM 0!!", "12-12-1212");
        connections.expectSend(protocol2.getConnectionId(), new ErrorMessage(Opcode.PM));
        protocol2.process(msg18);

        // Rotem Log with Mor should return data wthout Mor
        
        // Mor block Shay should success
        BlockMessage msg19 = new BlockMessage("Shay");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.BLOCK, ""));
        protocol1.process(msg19);
        assertFalse("shay following mor", shay.isFollowing(mor));
        assertFalse("mor following shay", mor.isFollowing(shay));
        assertFalse("shay blocking mor", shay.isBlocking(mor));
        assertTrue("mor not blocking shay", mor.isBlocking(shay));
        
        // Shay follow Mor should Fail
        FollowMessage msg20 = new FollowMessage((byte) 0, "Mor");        
        connections.expectSend(protocol3.getConnectionId(), new ErrorMessage(Opcode.FOLLOW));
        protocol3.process(msg20);

        // Rotem follow Mor should Fail
        FollowMessage msg21 = new FollowMessage((byte) 0, "Mor"); 
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.FOLLOW));
        protocol1.process(msg21);

        // Shay LogStat shouldn't contain Mor.
        // Rotem LogStat shouldn't contain Mor.
        // Mor LogStat shouldn't contain Rotem and shay.
        // Mor Log shouldn't contain Rotem and shay.

        assertTrue("message left in connection", this.connections.checkAllClear());
    }

    
    @Test
    public void testLogStat() {
        BGSProtocol protocol1 = this.createProtocol();
        BGSProtocol protocol2 = this.createProtocol();
        BGSProtocol protocol3 = this.createProtocol();

        // LogStat by Unregister client - should return error.
        LogStatMessage msg1= new LogStatMessage();        
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.LOGSTAT));
        protocol1.process(msg1);

        // Mor register should success
        RegisterMessage msg2 = new RegisterMessage("Mor", "123 ", "29-12-1997");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol1.process(msg2);
        assertEquals(1, this.usernamesToStudentMap.size());
        
        //LogStat by unLogIn client- should return error.
        LogStatMessage msg3= new LogStatMessage();        
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.LOGSTAT));
        protocol1.process(msg3);

        // Mor login should success
        LoginMessage msg4 = new LoginMessage("Mor", "123 ", (byte)1);
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol1.process(msg4);
        
        // LogStat by Login client- should return ack message.
        LogStatMessage msg5= new LogStatMessage(); 
        BGSStudent mor= this.usernamesToStudentMap.get("Mor");
        String content = ""; 
        content += new String(BGSMessage.shortToBytes((short)mor.getAge()), StandardCharsets.UTF_8);
        content += new String(BGSMessage.shortToBytes((short)mor.getNumOfPosts()), StandardCharsets.UTF_8);
        content += new String(BGSMessage.shortToBytes((short)mor.getNumOfFollowers()), StandardCharsets.UTF_8);
        content += new String(BGSMessage.shortToBytes((short)mor.getNumOfFollowing()), StandardCharsets.UTF_8);   
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.LOGSTAT,content));
        protocol1.process(msg5);


        //////// LogStat for multipule users

        // Rotem register should success
        RegisterMessage msg6 = new RegisterMessage("Rotem", "123 ", "29-12-1997");
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol2.process(msg6);
        assertEquals(2, this.usernamesToStudentMap.size());
 
        // Shay register should success
        RegisterMessage msg7 = new RegisterMessage("Shay", "123 ", "29-12-1997");
        connections.expectSend(protocol3.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol3.process(msg7);
        assertEquals(3, this.usernamesToStudentMap.size());
        
        // Rotem login should success
        LoginMessage msg8 = new LoginMessage("Rotem", "123 ", (byte)1);
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol2.process(msg8);

        // Shay login should success
        LoginMessage msg9 = new LoginMessage("Shay", "123 ", (byte)1);
        connections.expectSend(protocol3.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol3.process(msg9);

        // Rotem follow after Mor and Mor follow after Rotem + Rotem is posting a post
        FollowMessage msg10= new FollowMessage((byte) 0, "Rotem");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.FOLLOW, "Rotem" + '\0'));
        protocol1.process(msg10);

        FollowMessage msg11= new FollowMessage((byte) 0, "Mor");
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.FOLLOW, "Mor" + '\0'));
        protocol2.process(msg11);

        PostMessage msg12 = new PostMessage("Post 1");
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.POST, ""));
        connections.expectSend(protocol1.getConnectionId(), new NotificationMessage((byte)1, "Rotem", "Post 1"));
        protocol2.process(msg12);

        // LogStat
        LogStatMessage msg13= new LogStatMessage(); 
        
        String content1 = ""; 
        Collection<BGSStudent> students = this.usernamesToStudentMap.values();
        for (Iterator<BGSStudent> iter =students.iterator(); iter.hasNext(); ) {
            BGSStudent currentStudent = iter.next();
            if (!mor.isBlocking(currentStudent) && !currentStudent.isBlocking(mor)) {
                content1 += new String(BGSMessage.shortToBytes((short)currentStudent.getAge()), StandardCharsets.UTF_8);
                content1 += new String(BGSMessage.shortToBytes((short)currentStudent.getNumOfPosts()), StandardCharsets.UTF_8);
                content1 += new String(BGSMessage.shortToBytes((short)currentStudent.getNumOfFollowers()), StandardCharsets.UTF_8);
                content1 += new String(BGSMessage.shortToBytes((short)currentStudent.getNumOfFollowing()), StandardCharsets.UTF_8);
            }
        }
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.LOGSTAT,content1));
        protocol1.process(msg13);

        // LogStat with blocked user
        BlockMessage msg14 = new BlockMessage("Shay");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.BLOCK, ""));
        protocol1.process(msg14);

        LogStatMessage msg15= new LogStatMessage();
        String content2 = ""; 
        Collection<BGSStudent> students1 = this.usernamesToStudentMap.values();
        for (Iterator<BGSStudent> iter1 =students1.iterator(); iter1.hasNext(); ) {
            BGSStudent currentStudent1 = iter1.next();
            if (!mor.isBlocking(currentStudent1) && !currentStudent1.isBlocking(mor)) {
                content2 += new String(BGSMessage.shortToBytes((short)currentStudent1.getAge()), StandardCharsets.UTF_8);
                content2 += new String(BGSMessage.shortToBytes((short)currentStudent1.getNumOfPosts()), StandardCharsets.UTF_8);
                content2 += new String(BGSMessage.shortToBytes((short)currentStudent1.getNumOfFollowers()), StandardCharsets.UTF_8);
                content2 += new String(BGSMessage.shortToBytes((short)currentStudent1.getNumOfFollowing()), StandardCharsets.UTF_8);
            }
        }
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.LOGSTAT,content2));
        protocol1.process(msg15);
        
        assertTrue("message left in connection", this.connections.checkAllClear());
    }

    @Test
    public void testStat() {
        BGSProtocol protocol1 = this.createProtocol();
        BGSProtocol protocol2 = this.createProtocol();
        BGSProtocol protocol3 = this.createProtocol();

        // Rotem register should success
        RegisterMessage msg1 = new RegisterMessage("Rotem", "123 ", "29-12-1997");
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol2.process(msg1);
        assertEquals(1, this.usernamesToStudentMap.size());

        // Shay register should success
        RegisterMessage msg2 = new RegisterMessage("Shay", "123 ", "29-12-1997");
        connections.expectSend(protocol3.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol3.process(msg2);
        assertEquals(2, this.usernamesToStudentMap.size());
    
        // Rotem login should success
        LoginMessage msg3 = new LoginMessage("Rotem", "123 ", (byte)1);
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol2.process(msg3);

        // Shay login should success
        LoginMessage msg4 = new LoginMessage("Shay", "123 ", (byte)1);
        connections.expectSend(protocol3.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol3.process(msg4);
        
        // Stat by Unregister client - should return error.
        List<String> usernames= new LinkedList<String>();
        usernames.add("Rotem");
        usernames.add("Shay");
        StatMessage msg5= new StatMessage(usernames);        
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.STAT));
        protocol1.process(msg5);


        // Stat by unLogIn client- should return error.
        // Mor register should success
        RegisterMessage msg6 = new RegisterMessage("Mor", "123 ", "29-12-1997");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol1.process(msg6);
        assertEquals(3, this.usernamesToStudentMap.size());

        StatMessage msg7= new StatMessage(usernames);        
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.STAT));
        protocol1.process(msg7);

        // LogStat by Login client- should return ack message.
        LoginMessage msg8 = new LoginMessage("Mor", "123 ", (byte)1);
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol1.process(msg8);

        //Rotem follow after Mor and Mor follow after Rotem + Rotem is posting a post
        FollowMessage msg9= new FollowMessage((byte) 0, "Rotem");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.FOLLOW, "Rotem" + '\0'));
        protocol1.process(msg9);

        FollowMessage msg10= new FollowMessage((byte) 0, "Mor");
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.FOLLOW, "Mor" + '\0'));
        protocol2.process(msg10);

        PostMessage msg11 = new PostMessage("Post 1");
        connections.expectSend(protocol2.getConnectionId(), new AckMessage(Opcode.POST, ""));
        connections.expectSend(protocol1.getConnectionId(), new NotificationMessage((byte)1, "Rotem", "Post 1"));
        protocol2.process(msg11);

        // Stat message should success.
        StatMessage msg12= new StatMessage(usernames); 
        String content = ""; 
        for (Iterator<String> iter = usernames.iterator(); iter.hasNext(); ) {
            BGSStudent currentStudent = this.usernamesToStudentMap.get(iter.next());
            content += new String(BGSMessage.shortToBytes((short)currentStudent.getAge()), StandardCharsets.UTF_8);
            content += new String(BGSMessage.shortToBytes((short)currentStudent.getNumOfPosts()), StandardCharsets.UTF_8);
            content += new String(BGSMessage.shortToBytes((short)currentStudent.getNumOfFollowers()), StandardCharsets.UTF_8);
            content += new String(BGSMessage.shortToBytes((short)currentStudent.getNumOfFollowing()), StandardCharsets.UTF_8);
        }
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.STAT,content));
        protocol1.process(msg12);


        //// LogStat with non register user-should return error
        usernames.add("Gaya");
        StatMessage msg13= new StatMessage(usernames);
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.STAT));
        protocol1.process(msg13);
        usernames.remove("Gaya");

        // Mor block Shay should success.
        BlockMessage msg14 = new BlockMessage("Shay");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.BLOCK, ""));
        protocol1.process(msg14);
        
        // Stat with blocking user should return Error
        StatMessage msg15= new StatMessage(usernames); 
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.STAT));
        protocol1.process(msg15);

        assertTrue("message left in connection", this.connections.checkAllClear());
    }

    @Test 
    public void testDate() {
        String date = "12-02-200";
        String[] arr = date.split("-");
        Period period = Period.between(LocalDate.now(), LocalDate.of(Integer.parseInt(arr[2]), Integer.parseInt(arr[1]), Integer.parseInt(arr[0])));
    }
}