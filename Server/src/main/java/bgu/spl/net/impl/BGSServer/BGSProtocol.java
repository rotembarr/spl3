package bgu.spl.net.impl.BGSServer;

import java.util.Map;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.BGSServer.Messages.AckMessage;
import bgu.spl.net.impl.BGSServer.Messages.BGSMessage;
import bgu.spl.net.impl.BGSServer.Messages.RegisterMessage;
import bgu.spl.net.impl.BGSServer.Messages.LoginMessage;
import bgu.spl.net.impl.BGSServer.Messages.LogoutMessage;
import bgu.spl.net.impl.BGSServer.Messages.FollowMessage;
import bgu.spl.net.impl.BGSServer.Messages.PostMessage;
import bgu.spl.net.impl.BGSServer.Messages.PMMessage;
import bgu.spl.net.impl.BGSServer.Messages.LogStatMessage;
import bgu.spl.net.impl.BGSServer.Messages.StatMessage;
import bgu.spl.net.impl.BGSServer.Messages.BGSMessage.Opcode;
import bgu.spl.net.impl.BGSServer.Messages.NotificationMessage;
import bgu.spl.net.impl.BGSServer.Messages.BlockMessage;
import bgu.spl.net.impl.BGSServer.Messages.ErrorMessage;
import bgu.spl.net.impl.BGSServer.BGSStudent;

public class BGSProtocol implements BidiMessagingProtocol<BGSMessage> {
    
    private boolean shouldTerminate = false;
    private int connectionId = -1;
    private Connections<BGSMessage> connections = null;
    private Map<String, BGSStudent> usernamesToStudentMap = null;

    public BGSProtocol(Map<String, BGSStudent> usernamesToStudentMap) {
        this.usernamesToStudentMap = usernamesToStudentMap; // This map has to be syncronized!
    }

    @Override
    public void start(int connectionId, Connections<BGSMessage> connections) {
        this.shouldTerminate = false;
        this.connectionId = connectionId;
        this.connections = connections;
    }

    @Override
    public void process(BGSMessage msg) {

        // Basic checker.
        if (this.connectionId == -1) {
            System.out.println("Couldn't process messagewith connection-id = -1");
            return;
        }

        // Protocolllll.
        if (msg instanceof RegisterMessage) {
            this.handleRegisteMessage((RegisterMessage)msg);
        } else if (msg instanceof LoginMessage) {
            LoginMessage castMsg = (LoginMessage)msg;

        } else if (msg instanceof LogoutMessage) {
            LogoutMessage castMsg = (LogoutMessage)msg;

        } else if (msg instanceof FollowMessage) {
            FollowMessage castMsg = (FollowMessage)msg;

        } else if (msg instanceof PostMessage) {
            PostMessage castMsg = (PostMessage)msg;

        } else if (msg instanceof PMMessage) {
            PMMessage castMsg = (PMMessage)msg;

        } else if (msg instanceof LogStatMessage) {
            LogStatMessage castMsg = (LogStatMessage)msg;

        } else if (msg instanceof StatMessage) {
            StatMessage castMsg = (StatMessage)msg;

        } else if (msg instanceof NotificationMessage) {
            NotificationMessage castMsg = (NotificationMessage)msg;

        } else if (msg instanceof BlockMessage) {
            BlockMessage castMsg = (BlockMessage)msg;
        } else {
            System.out.println("Unrecognized msg arrived to protocol");
        }

        // shouldTerminate = "bye".equals(msg);
        // System.out.println("[" + LocalDateTime.now() + "]: " + msg);
        // TODO
    }    

    public void handleRegisteMessage(RegisterMessage msg) {

            // Cannot register a register user.
            if (this.usernamesToStudentMap.containsKey(msg.getUsername())) {
                ErrorMessage error = new ErrorMessage(BGSMessage.Opcode.REGISTER);
                this.connections.send(this.connectionId, msg);
            
            // Create student but don't connect him.
            } else {
                BGSStudent student = new BGSStudent(msg.getUsername(), msg.getPassword(), msg.getBirthday());
                this.usernamesToStudentMap.put(msg.getUsername(), student);
                AckMessage ack = new AckMessage(BGSMessage.Opcode.REGISTER, "");
            }
    }

    public void handleLoginMessage(LoginMessage msg) {
        
        // Get studnt (null if doesn't exists).
        // Attention: Assuming no other connection handler think it represent this student.
        BGSStudent student = this.usernamesToStudentMap.get(msg.getUsername());

        // Student isn't register.
        if (student == null) {
            ErrorMessage error = new ErrorMessage(BGSMessage.Opcode.LOGIN);
            this.connections.send(this.connectionId, msg);
            return;
        }
        
        // Passwrords doesn't matches.
        if (!student.getPassword().equals(msg.getPassword())) {
            ErrorMessage error = new ErrorMessage(BGSMessage.Opcode.LOGIN);
            this.connections.send(this.connectionId, msg);
            return;
        }
        
        // Student already logged in.
        if (student.isConnected()) {
            ErrorMessage error = new ErrorMessage(BGSMessage.Opcode.LOGIN);
            this.connections.send(this.connectionId, msg);
            return;
        }

        // Captcha is 0.
        if (msg.getCaptcha() == (byte)0) {
            ErrorMessage error = new ErrorMessage(BGSMessage.Opcode.LOGIN);
            this.connections.send(this.connectionId, msg);
            return;
        }

        // If login aproval, set his connectionId and open him to conversation.
        synchronized(student) { // TODO
            student.setConnectionId(this.connectionId);        
            student.setConnected(true);
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
