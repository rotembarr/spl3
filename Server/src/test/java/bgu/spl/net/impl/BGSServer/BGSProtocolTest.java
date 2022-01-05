package bgu.spl.net.impl.BGSServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Before;
import org.junit.Test;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.BGSServer.Messages.AckMessage;
import bgu.spl.net.impl.BGSServer.Messages.BGSMessage;
import bgu.spl.net.impl.BGSServer.Messages.ErrorMessage;
import bgu.spl.net.impl.BGSServer.Messages.FollowMessage;
import bgu.spl.net.impl.BGSServer.Messages.LoginMessage;
import bgu.spl.net.impl.BGSServer.Messages.LogoutMessage;
import bgu.spl.net.impl.BGSServer.Messages.NotificationMessage;
import bgu.spl.net.impl.BGSServer.Messages.PMMessage;
import bgu.spl.net.impl.BGSServer.Messages.PostMessage;
import bgu.spl.net.impl.BGSServer.Messages.RegisterMessage;
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
}
