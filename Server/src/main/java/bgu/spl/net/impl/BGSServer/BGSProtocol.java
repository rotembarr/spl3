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
import bgu.spl.net.impl.BGSServer.Messages.NotificationMessage;
import bgu.spl.net.impl.BGSServer.Messages.BlockMessage;
import bgu.spl.net.impl.BGSServer.Messages.ErrorMessage;

public class BGSProtocol implements BidiMessagingProtocol<BGSMessage> {
    
    // BGU Variables.
    private Map<String, BGSStudent> usernamesToStudentMap = null;
    private BGSStudent student = null; // Student which the protocol represent.

    // Protocol Variables.
    private int connectionId = -1;
    private Connections<BGSMessage> connections = null;

    // Local Variable.
    private boolean shouldTerminate = false;

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
            this.handleLoginMessage((LoginMessage)msg);
        } else if (msg instanceof LogoutMessage) {
            this.handleLogout((LogoutMessage)msg);
        } else if (msg instanceof FollowMessage) {
            this.handleFollowMessage((FollowMessage)msg);
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
                BGSStudent newStudent = new BGSStudent(msg.getUsername(), msg.getPassword(), msg.getBirthday());
                this.usernamesToStudentMap.put(msg.getUsername(), newStudent);
                AckMessage ack = new AckMessage(BGSMessage.Opcode.REGISTER, "");
                this.connections.send(this.connectionId, ack);
            }
    }

    private void handleLoginMessage(LoginMessage msg) {
        
        // Get studnt (null if doesn't exists).
        // Attention: Assuming no other connection handler think it represent this student.
        BGSStudent mapedStudent = this.usernamesToStudentMap.get(msg.getUsername());

        // Student isn't loged in.
        if (mapedStudent == null) {
            ErrorMessage error = new ErrorMessage(BGSMessage.Opcode.LOGIN);
            this.connections.send(this.connectionId, error);
            return;
        }

        // This code has to be in synchronized in order not to logged in twice the same student.
        // TODO change to busy wait.
        synchronized (mapedStudent) {

            // Student already logged in.
            if (this.connections.isConnected(mapedStudent.getConnectionId())) { 
                ErrorMessage error = new ErrorMessage(BGSMessage.Opcode.LOGIN);
                this.connections.send(this.connectionId, error);
                return;
            }
            
            // Passwrords doesn't matches.
            if (!mapedStudent.getPassword().equals(msg.getPassword())) {
                ErrorMessage error = new ErrorMessage(BGSMessage.Opcode.LOGIN);
                this.connections.send(this.connectionId, error);
                return;
            }
    
            // Captcha is 0.
            if (msg.getCaptcha() == (byte)0) {
                ErrorMessage error = new ErrorMessage(BGSMessage.Opcode.LOGIN);
                this.connections.send(this.connectionId, error);
                return;
            }
    
            // If login aproval, set his connectionId and open him to conversation.
            // No need to synchronize this because this is the only thread anyone can change this student.
            mapedStudent.setConnectionId(this.connectionId);        
            this.student = mapedStudent;
        }
    }

    private void handleLogout(LogoutMessage msg) {

        // if no user register to this connection id send error.
        if (this.student == null) {
            ErrorMessage error = new ErrorMessage(BGSMessage.Opcode.LOGOUT);
            this.connections.send(this.connectionId, error);
        } else {

            // Make this student unconnected.
            // No need to synchronize this because this is the only thread anyone can change this student.
            this.connections.disconnect(this.connectionId);
            this.student.setConnectionId(-1);
            this.student = null;

            // After this ack sent, the client will close the connection, and this protocol will die.
            AckMessage ack = new AckMessage(BGSMessage.Opcode.LOGOUT, "");
            this.connections.send(this.connectionId, ack);
        }
    }

    private void handleFollowMessage(FollowMessage msg) {

        // if no user loged in for this connection id.
        if (this.student == null) {
            this.sendError(BGSMessage.Opcode.FOLLOW);
            return;
        } 

        // If the target student isn't exists.
        BGSStudent otherStudent = this.usernamesToStudentMap.get(msg.getUsername());
        if (otherStudent == null) {
            this.sendError(BGSMessage.Opcode.FOLLOW);
            return;
        }
        
        // 0 for follow
        if (msg.getFollow() == 0) {
            
            // Already following.
            if (this.student.isFollowing(otherStudent)) {
                this.sendError(BGSMessage.Opcode.FOLLOW);
                return;
            }

            // Follow.
            this.student.follow(otherStudent);

        // 1 for unfollow
        } else if (msg.getFollow() == 1) {
            
            // Not following.
            if (!this.student.isFollowing(otherStudent)) {
                this.sendError(BGSMessage.Opcode.FOLLOW);
                return;
            }

            // Unfollow.
            this.student.unfollow(otherStudent);
        } else {
            System.out.println("Error in follow message");
        }

        AckMessage ack = new AckMessage(BGSMessage.Opcode.FOLLOW, msg.getUsername() + '\0');
        this.connections.send(this.connectionId, ack);        
    }

    private void sendError(BGSMessage.Opcode opcode) {
        ErrorMessage error = new ErrorMessage(opcode);
        this.connections.send(this.connectionId, error);
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
