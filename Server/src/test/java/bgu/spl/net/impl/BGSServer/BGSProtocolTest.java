package bgu.spl.net.impl.BGSServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Before;
import org.junit.Test;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.BGSServer.Messages.AckMessage;
import bgu.spl.net.impl.BGSServer.Messages.BGSMessage;
import bgu.spl.net.impl.BGSServer.Messages.ErrorMessage;
import bgu.spl.net.impl.BGSServer.Messages.LoginMessage;
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

    }


    // private class BlockingConnectionHandlerTest extends BlockingConnectionHandler<BGSMessage> {

    //     public BlockingConnectionHandlerTest(Connections<BGSMessage> connections, BidiMessagingProtocol<BGSMessage> protocol) {
    //         super(null, connections, null, protocol);
    //     }

    // }

    Map<String, BGSStudent> usernamesToStudentMap = null;
    ConnectionsTest connections = null;

    public BGSProtocolTest() {

    }

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
        RegisterMessage msg = new RegisterMessage("mor hayafa", "123 ", "29-12-1997");
        
        assertEquals(0, this.usernamesToStudentMap.size());

        // First register should success
        connections.expectSend(protocol.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol.process(msg);
        assertEquals(1, this.usernamesToStudentMap.size());
        
        // Second register should fail
        connections.expectSend(protocol.getConnectionId(), new ErrorMessage(Opcode.REGISTER));
        protocol.process(msg);
        assertEquals(1, this.usernamesToStudentMap.size());
    }

    @Test
    public void testLogin() {
        BGSProtocol protocol1 = this.createProtocol();
        BGSProtocol protocol2 = this.createProtocol();

        // RegisterMessage msg2 = new RegisterMessage("Rotem hahatih", "123 ", "29-12-1997");
        
        // Mor register should success
        RegisterMessage msg1 = new RegisterMessage("mor hayafa", "123 ", "29-12-1997");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol1.process(msg1);
        assertEquals(1, this.usernamesToStudentMap.size());

        // Mor login should fail for bad passwod
        LoginMessage msg2 = new LoginMessage("mor hayafa", "asas ", (byte)1);
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.LOGIN));
        protocol1.process(msg2);

        // Mor login should fail for bad captcha
        LoginMessage msg3 = new LoginMessage("mor hayafa", "123 ", (byte)0);
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.LOGIN));
        protocol1.process(msg3);

        // login should fail for bad username
        LoginMessage msg4 = new LoginMessage("hagar hayafa", "123 ", (byte)0);
        connections.expectSend(protocol1.getConnectionId(), new ErrorMessage(Opcode.LOGIN));
        protocol1.process(msg4);

        // Mor login should success
        LoginMessage msg5 = new LoginMessage("mor hayafa", "123 ", (byte)1);
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol1.process(msg5);

        // Mor login should failbecause she loged in
        LoginMessage msg6 = new LoginMessage("mor hayafa", "123 ", (byte)1);
        connections.expectSend(protocol2.getConnectionId(), new ErrorMessage(Opcode.LOGIN));
        protocol2.process(msg6);
    }

    @Test
    public void testFollow() {
        BGSProtocol protocol1 = this.createProtocol();
        BGSProtocol protocol2 = this.createProtocol();

        // Mor register should success
        RegisterMessage msg1 = new RegisterMessage("mor", "123 ", "29-12-1997");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol1.process(msg1);
        assertEquals(1, this.usernamesToStudentMap.size());

        // Rotem register should success
        RegisterMessage msg2 = new RegisterMessage("Rotem", "123 ", "29-12-1997");
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.REGISTER, ""));
        protocol1.process(msg2);
        assertEquals(1, this.usernamesToStudentMap.size());

        // Mor login should success
        LoginMessage msg3 = new LoginMessage("mor", "123 ", (byte)1);
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol1.process(msg3);

        // follow after non register should fail.

        // Follow after non-loged in should success.

        // Follow after already folowing should fail.

        // Rotem login should success
        LoginMessage msg4 = new LoginMessage("Rotem", "123 ", (byte)1);
        connections.expectSend(protocol1.getConnectionId(), new AckMessage(Opcode.LOGIN, ""));
        protocol1.process(msg4);

    }
}
