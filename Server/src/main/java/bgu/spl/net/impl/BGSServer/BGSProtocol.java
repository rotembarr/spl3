package bgu.spl.net.impl.BGSServer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
            return;
        }
        
        // Create student but don't connect him.
        BGSStudent newStudent = new BGSStudent(msg.getUsername(), msg.getPassword(), msg.getBirthday());
        this.usernamesToStudentMap.put(msg.getUsername(), newStudent);
        this.sendAck(BGSMessage.Opcode.REGISTER, "");
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
    
            // Captcha isn't 1.
            if (msg.getCaptcha() != (byte)1) {
                this.sendError(BGSMessage.Opcode.LOGIN);
                return;
            }
    
            // If login aproval, set his connectionId and open him to conversation.
            // No need to synchronize this because this is the only thread anyone can change this student.
            this.student = mapedStudent;
            mapedStudent.setConnectionId(this.connectionId);
            this.sendAck(BGSMessage.Opcode.LOGIN, "");

        } // synchronize
            
        // Send all the buffered notification.
        // No dengare of this.student logout beacuse he can logout only after this function finishes.
        NotificationMessage notiMsg = null;
        while ((notiMsg = this.student.getBackupNotification()) != null) {
            this.connections.send(this.connectionId, notiMsg);
        }

    }

    private void handleLogout(LogoutMessage msg) {
        // Note: no need to sync this function because we are loggin out.
        //       There is no denage of someone foolow or block this.student while executing this function.
        //       The denage here is we will logout but someone will send this CID a msg after we will send our ack for logout.    
        //       The solution for this problem is simple - 

        // if no user register to this connection id send error.
        if (this.student == null) {
            this.sendError(BGSMessage.Opcode.LOGOUT);
            return;
        }

        // Make this student unconnected.
        this.student.setConnectionId(-1);
        this.student = null;
        
        // After this ack sent, the client will close the connection, and this protocol will die.
        this.sendAck(BGSMessage.Opcode.LOGOUT, "");
        
        // No need to synchronize connections because this thread is the only thread that allowed to disconnet this CID.
        this.connections.disconnect(this.connectionId);
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
        
        // If follow after ourselves.
        if (otherStudent == this.student) {
            this.sendError(BGSMessage.Opcode.FOLLOW);
            return;
        }

        // Prepre locks.
        BGSStudent studentLock1 = null;
        BGSStudent studentLock2 = null;
        if (this.student.hashCode() >= otherStudent.hashCode()) {
            studentLock1 = this.student;
            studentLock2 = otherStudent;
        } else {
            studentLock1 = otherStudent;
            studentLock2 = this.student;
        }

        // Lock both students.
        synchronized (studentLock1) {
            synchronized (studentLock2) {

                // If one of us blocking the other
                if (this.student.isBlocking(otherStudent) || otherStudent.isBlocking(this.student)) {
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

                    // Follow !
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
                    System.out.println("Error in parsing follow message");
                    this.sendError(BGSMessage.Opcode.FOLLOW);
                    return;
                }         

                // Send ack.
                this.sendAck(BGSMessage.Opcode.FOLLOW, msg.getUsername() + '\0');

            } // synchronize
        } // synchronize

    }

    private void handlePostMessage(PostMessage msg) {

        // if no user loged in for this connection id.
        if (this.student == null) {
            this.sendError(BGSMessage.Opcode.POST);
            return;
        } 

        // Create notification msg to send for all the destenation users.
        NotificationMessage notiMsg = new NotificationMessage((byte)1, this.student.getUsername(), msg.getContent());

        // Lock the student in order no follow/unfollow or block to this user will happend during sending
        synchronized (this.student) {

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
    
                // Taged person
                if (contentInWords[i].charAt(0) ==  '@') {
                    BGSStudent directedStudent = this.usernamesToStudentMap.get(contentInWords[i].substring(1));
    
                    // if direct register and isn't blocking
                    if (directedStudent != null && !this.student.isBlocking(directedStudent) && !directedStudent.isBlocking(this.student)) {
    
                        // And if we are not already sent him this post.
                        if (!this.student.isFollower(directedStudent)) {
    
                            boolean success = this.connections.send(directedStudent.getConnectionId(), notiMsg);
                            if (!success) {
                                directedStudent.backupNotification(notiMsg);
                            }

                        }
                    }
                }
            }
        
            // Save Post and send ack.
            this.student.savePost(msg);
            this.sendAck(BGSMessage.Opcode.POST, "");

        } // synchronize
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

        // Synch this.student in order anyone else wont unfollow or block this.student
        synchronized (this.student) {

            // Can't send PM to blockes users
            if (dst.isBlocking(this.student) || this.student.isBlocking(dst)) {
                this.sendError(BGSMessage.Opcode.PM);
                return;
            } 
    
            // if this.student isn't following dst user.
            if (!this.student.isFollowing(dst)) {
                this.sendError(BGSMessage.Opcode.PM);
                return;
            } 
    
            // Create notification msg to destenation user.
            msg.filter();
            NotificationMessage notiMsg = new NotificationMessage((byte)0, this.student.getUsername(), msg.getContent() + " " + msg.getSendingDateAndTime());
    
            // Send noti msg to dest, and save it if dest isn't connected.
            boolean success = this.connections.send(dst.getConnectionId(), notiMsg);
            if (!success) {
                dst.backupNotification(notiMsg);
            }
    
            this.student.savePM(msg);
            this.sendAck(BGSMessage.Opcode.PM, "");
        } // synchronize
    } 

    private void handleLogStatMessage(LogStatMessage msg) {

        // if no user loged in for this connection id.
        if (this.student == null) {
            this.sendError(BGSMessage.Opcode.LOGSTAT);
            return;
        } 

        // Synch this.student in order anyone else wont unfollow or block this.student
        synchronized (this.student) {

            // Create ack msg content.
            String content = ""; 
            Collection<BGSStudent> students = this.usernamesToStudentMap.values();
            for (Iterator<BGSStudent> iter =students.iterator(); iter.hasNext(); ) {
                BGSStudent currentStudent = iter.next();

                // Create inforation for all user beside blocked users.
                if (!this.student.isBlocking(currentStudent) && !currentStudent.isBlocking(this.student)) {
                    content += new String(BGSMessage.shortToBytes((short)currentStudent.getAge()), StandardCharsets.UTF_8);
                    content += new String(BGSMessage.shortToBytes((short)currentStudent.getNumOfPosts()), StandardCharsets.UTF_8);
                    content += new String(BGSMessage.shortToBytes((short)currentStudent.getNumOfFollowers()), StandardCharsets.UTF_8);
                    content += new String(BGSMessage.shortToBytes((short)currentStudent.getNumOfFollowing()), StandardCharsets.UTF_8);
                }
            }

            // Send msg back.
            this.sendAck(BGSMessage.Opcode.LOGSTAT, content);
        } // synchronize
    }

    private void handleStatMessage(StatMessage msg) {

        // if no user loged in for this connection id.
        if (this.student == null) {
            this.sendError(BGSMessage.Opcode.STAT);
            return;
        } 

        // Synch this.student in order anyone else wont unfollow or block this.student
        synchronized (this.student) {

            // Create ack msg content.
            String content = ""; 
            List<String> usernames = msg.getUsernames();
            for (Iterator<String> iter = usernames.iterator(); iter.hasNext(); ) {
                String username = iter.next();

                // First check if threre is such a user.
                if (!this.usernamesToStudentMap.containsKey(username)) {
                    this.sendError(BGSMessage.Opcode.STAT);
                    return;
                }

                // Then check if there isn't blocking.
                BGSStudent currentStudent = this.usernamesToStudentMap.get(username);
                if (this.student.isBlocking(currentStudent) || currentStudent.isBlocking(this.student)) {
                    this.sendError(BGSMessage.Opcode.STAT);
                    return;
                } 

                // If all good, add this user to ack msg.
                content += new String(BGSMessage.shortToBytes((short)currentStudent.getAge()), StandardCharsets.UTF_8);
                content += new String(BGSMessage.shortToBytes((short)currentStudent.getNumOfPosts()), StandardCharsets.UTF_8);
                content += new String(BGSMessage.shortToBytes((short)currentStudent.getNumOfFollowers()), StandardCharsets.UTF_8);
                content += new String(BGSMessage.shortToBytes((short)currentStudent.getNumOfFollowing()), StandardCharsets.UTF_8);
            }

            // Send msg back.
            this.sendAck(BGSMessage.Opcode.STAT, content);
        } // synchronize
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
        
        // Prepre locks.
        BGSStudent studentLock1 = null;
        BGSStudent studentLock2 = null;
        if (this.student.hashCode() >= blockedStudent.hashCode()) {
            studentLock1 = this.student;
            studentLock2 = blockedStudent;
        } else {
            studentLock1 = blockedStudent;
            studentLock2 = this.student;
        }
        
        // Cant block ourselves.
        if (blockedStudent == this.student) {
            this.sendError(BGSMessage.Opcode.BLOCK);
            return;
        } 

        // Lock both students.
        synchronized (studentLock1) {
            synchronized (studentLock2) {

                // Blocking.
                this.student.block(blockedStudent);
                
                // Send msg back.
                this.sendAck(BGSMessage.Opcode.BLOCK, "");

            } // synchronize
        }
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
