package bgu.spl.net.impl.BGSServer;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
            this.handlePostMessage((PostMessage)msg);
        } else if (msg instanceof PMMessage) {
            this.handlePMMessage((PMMessage)msg);
        } else if (msg instanceof LogStatMessage) {
            this.handleLogStatMessage((LogStatMessage)msg);
        } else if (msg instanceof StatMessage) {
            this.handleStatMessage((StatMessage)msg);
        } else if (msg instanceof BlockMessage) {
            this.handleBlockMessage((BlockMessage)msg);
        } else {
            System.out.println("Unrecognized msg arrived to protocol");
        }
    }    

    public void handleRegisteMessage(RegisterMessage msg) {

        // Cannot register a register user.
        if (this.usernamesToStudentMap.containsKey(msg.getUsername())) {
            this.sendError(BGSMessage.Opcode.REGISTER);
        
        // Create student but don't connect him.
        } else {
            BGSStudent newStudent = new BGSStudent(msg.getUsername(), msg.getPassword(), msg.getBirthday());
            this.usernamesToStudentMap.put(msg.getUsername(), newStudent);
            this.sendAck(BGSMessage.Opcode.REGISTER, "");
        }
    }

    private void handleLoginMessage(LoginMessage msg) {
        
        // Get studnt (null if doesn't exists).
        // Attention: Assuming no other connection handler think it represent this student.
        BGSStudent mapedStudent = this.usernamesToStudentMap.get(msg.getUsername());

        // Student isn't loged in.
        if (mapedStudent == null) {
            this.sendError(BGSMessage.Opcode.LOGIN);
            return;
        }

        // This code has to be in synchronized in order not to logged in twice the same student.
        // TODO change to busy wait.
        synchronized (mapedStudent) {

            // Student already logged in.
            if (this.connections.isConnected(mapedStudent.getConnectionId())) { 
                this.sendError(BGSMessage.Opcode.LOGIN);
                return;
            }
            
            // Passwrords doesn't matches.
            if (!mapedStudent.getPassword().equals(msg.getPassword())) {
                this.sendError(BGSMessage.Opcode.LOGIN);
                return;
            }
    
            // Captcha is 0.
            if (msg.getCaptcha() == (byte)0) {
                this.sendError(BGSMessage.Opcode.LOGIN);
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
            this.sendError(BGSMessage.Opcode.LOGOUT);
        } else {

            // Make this student unconnected.
            // No need to synchronize this because this is the only thread anyone can change this student.
            this.connections.disconnect(this.connectionId);
            this.student.setConnectionId(-1);
            this.student = null;

            // After this ack sent, the client will close the connection, and this protocol will die.
            this.sendAck(BGSMessage.Opcode.LOGOUT, "");
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

        this.sendAck(BGSMessage.Opcode.FOLLOW, msg.getUsername() + '\0');
    }

    private void handlePostMessage(PostMessage msg) {

        // if no user loged in for this connection id.
        if (this.student == null) {
            this.sendError(BGSMessage.Opcode.POST);
            return;
        } 

        // Create notification msg to send for all the destenation users.
        NotificationMessage notiMsg = new NotificationMessage((byte)1, this.student.getUsername(), msg.getContent());

        // Send the post to all the follwers.
        Collection<BGSStudent> dstStudents = this.student.getFollowers();
        for (Iterator<BGSStudent> iter = dstStudents.iterator(); iter.hasNext(); ) {
            BGSStudent dst = iter.next();
            boolean success = this.connections.send(dst.getConnectionId(), notiMsg);
            if (!success) {
                dst.backupNotification(notiMsg);
            }
        }

        // Search directed users and send them also the post.
        String[] contentInWords = msg.getContent().split(" ");
        for (int i = 0; i < contentInWords.length; i++) {
            if (contentInWords[i].charAt(0) ==  '@') {
                BGSStudent directedStudent = this.usernamesToStudentMap.get(contentInWords[i].substring(1));
                if (directedStudent != null && !this.student.isBlocking(directedStudent) && !directedStudent.isBlocking(this.student)) {
                    boolean success = this.connections.send(directedStudent.getConnectionId(), notiMsg);
                    if (!success) {
                        directedStudent.backupNotification(notiMsg);
                    }
                }
            }
        }

        this.student.savePost(msg);
        this.sendAck(BGSMessage.Opcode.POST, "");
    }

    private void handlePMMessage(PMMessage msg) {
        
        // if no user loged in for this connection id.
        if (this.student == null) {
            this.sendError(BGSMessage.Opcode.PM);
            return;
        } 

        // If dst user doesn't register.
        BGSStudent dst = this.usernamesToStudentMap.get(msg.getUsername());
        if (dst == null) {
            this.sendError(BGSMessage.Opcode.PM);
            return;
        } 

        // TODO - this is my add
        if (dst.isBlocking(this.student) || this.student.isBlocking(dst)) {
            this.sendError(BGSMessage.Opcode.PM);
            return;
        } 

        // // if this.student isn't following dst user.
        // if (!this.student.isFollowing(dst)) {
        //     this.sendError(BGSMessage.Opcode.PM);
        //     return;
        // } TODO

        // Create notification msg to destenation user.
        msg.filter();
        NotificationMessage notiMsg = new NotificationMessage((byte)0, this.student.getUsername(), msg.getContent());

        // Send noti msg to dest, and save it if dest isn't connected.
        boolean success = this.connections.send(dst.getConnectionId(), notiMsg);
        if (!success) {
            dst.backupNotification(notiMsg);
        }

        this.student.savePM(msg);
        this.sendAck(BGSMessage.Opcode.PM, "");
    } 

    private void handleLogStatMessage(LogStatMessage msg) {

        // if no user loged in for this connection id.
        if (this.student == null) {
            this.sendError(BGSMessage.Opcode.LOGSTAT);
            return;
        } 

        // Create ack msg content.
        String content = ""; // TODO - handle block
        Collection<BGSStudent> students = this.usernamesToStudentMap.values();
        for (Iterator<BGSStudent> iter =students.iterator(); iter.hasNext(); ) {
            BGSStudent currentStudent = iter.next();
            content += new String(BGSMessage.shortToBytes((short)currentStudent.getAge()), StandardCharsets.UTF_8);
            content += new String(BGSMessage.shortToBytes((short)currentStudent.getNumOfPosts()), StandardCharsets.UTF_8);
            content += new String(BGSMessage.shortToBytes((short)currentStudent.getNumOfFollowers()), StandardCharsets.UTF_8);
            content += new String(BGSMessage.shortToBytes((short)currentStudent.getNumOfFollowing()), StandardCharsets.UTF_8);
        }

        this.sendAck(BGSMessage.Opcode.LOGSTAT, content);
    }

    private void handleStatMessage(StatMessage msg) {

        // if no user loged in for this connection id.
        if (this.student == null) {
            this.sendError(BGSMessage.Opcode.STAT);
            return;
        } 

        // if one of the usernames isn't register.
        List<String> usernames = msg.getUsernames();
        for (Iterator<String> iter = usernames.iterator(); iter.hasNext(); ) {
            String username = iter.next();
            if (!this.usernamesToStudentMap.containsKey(username)) {
                this.sendError(BGSMessage.Opcode.STAT);
                return;
            }
        }

        // Create ack msg content.
        String content = ""; // TODO - handle block
        for (Iterator<String> iter = usernames.iterator(); iter.hasNext(); ) {
            BGSStudent currentStudent = this.usernamesToStudentMap.get(iter.next());
            content += new String(BGSMessage.shortToBytes((short)currentStudent.getAge()), StandardCharsets.UTF_8);
            content += new String(BGSMessage.shortToBytes((short)currentStudent.getNumOfPosts()), StandardCharsets.UTF_8);
            content += new String(BGSMessage.shortToBytes((short)currentStudent.getNumOfFollowers()), StandardCharsets.UTF_8);
            content += new String(BGSMessage.shortToBytes((short)currentStudent.getNumOfFollowing()), StandardCharsets.UTF_8);
        }

        // Send msg back.
        this.sendAck(BGSMessage.Opcode.STAT, content);
    }


    private void handleBlockMessage(BlockMessage msg) {
        
        // if no user loged in for this connection id.
        if (this.student == null) {
            this.sendError(BGSMessage.Opcode.BLOCK);
            return;
        } 

        // if bad username to block.
        BGSStudent blockedStudent = this.usernamesToStudentMap.get(msg.getUsername());
        if (blockedStudent == null) {
            this.sendError(BGSMessage.Opcode.BLOCK);
            return;
        } 
        
        // Blocking.
        this.student.block(blockedStudent);
        
        // Send msg back.
        this.sendAck(BGSMessage.Opcode.BLOCK, "");
    }

    private void sendError(BGSMessage.Opcode opcode) {
        ErrorMessage error = new ErrorMessage(opcode);
        this.connections.send(this.connectionId, error);
    }

    private void sendAck(BGSMessage.Opcode opcode, String optional) {
        AckMessage ack = new AckMessage(opcode, optional);
        this.connections.send(this.connectionId, ack);        
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    // For test uses
    public int getConnectionId() {
        return this.connectionId;
    }
}
