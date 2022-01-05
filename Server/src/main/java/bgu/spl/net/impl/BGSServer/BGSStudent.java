package bgu.spl.net.impl.BGSServer;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.net.impl.BGSServer.Messages.NotificationMessage;
import bgu.spl.net.impl.BGSServer.Messages.PMMessage;
import bgu.spl.net.impl.BGSServer.Messages.PostMessage;

public class BGSStudent {
    private final String username;
    private final String password;
    private final String birthday;
    private AtomicInteger connectionId; // one thread will write but multiple reads.
    private List<BGSStudent> following; // one thread read and write.
    private Collection<BGSStudent> followers; // multiple therad write and one read.
    private Collection<BGSStudent> blockedStudents; // multiple therad write and one read.
    private Queue<NotificationMessage> backupNotifications; // multiple therad write and one read.

    // Statistics (can be accessed from multiple threads).
    private Collection<PostMessage> posts;
    private Collection<PMMessage> pms;

    // Constructor.
    public BGSStudent(String username, String password, String birthday) {
        this.username = username;
        this.password = password;
        this.birthday = birthday;
        this.connectionId  = new AtomicInteger(-1);
        this.following = new LinkedList<BGSStudent>();
        this.followers = new ConcurrentLinkedDeque<BGSStudent>();
        this.blockedStudents = new ConcurrentLinkedDeque<BGSStudent>();
        this.posts = new ConcurrentLinkedDeque<PostMessage>();
        this.pms = new ConcurrentLinkedDeque<PMMessage>();
        this.backupNotifications = new ConcurrentLinkedDeque<NotificationMessage>(); 
    }

    // Getters and Setters
    public String getUsername() {
        return this.username;
    }
    
    public String getPassword() {
        return this.password;
    }

    public String getBirthday() {
        return this.birthday;
    }
    
    public int getConnectionId() {
        return this.connectionId.get();
    }

    public void setConnectionId(int connectionId) {
        this.connectionId.set(connectionId);;
    }

    public int getNumOfPosts() {
        return this.posts.size();
    }

    public int getAge() {
        String[] bd = this.birthday.split("-");
        Period period = Period.between(LocalDate.now(), LocalDate.of(Integer.parseInt(bd[2]), Integer.parseInt(bd[1]), Integer.parseInt(bd[0])));
        return period.getYears();
    }

    public int getNumOfFollowers() {
        return this.followers.size();
    }

    public int getNumOfFollowing() {
        return this.following.size();
    }

    public Collection<BGSStudent> getFollowers() {
        return this.followers;
    }

    public boolean isFollowing(BGSStudent other) {
        return this.following.contains(other);
    }


    // Logic functions.
    public boolean follow(BGSStudent other) {
        
        // Dont add blockedStudents users.
        if (this.blockedStudents.contains(other)) {
            return false;
        } 

        // Add followers to other.
        if (!other.addFollower(this)) {
            return false;
        }

        // Only in success of the above.
        return this.following.add(other);
    }

    protected boolean addFollower(BGSStudent other) {
        
        // Dont add blockedStudents users.
        if (this.blockedStudents.contains(other)) {
            return false;
        } 

        return this.followers.add(other);
    }

    protected boolean reomoveFollower(BGSStudent other) {        
        return this.followers.remove(other);
    }

    public boolean unfollow(BGSStudent studentToUnfollow) {

        // Add followers to other.
        if (!studentToUnfollow.reomoveFollower(this)) {
            return false;
        }

        return this.following.remove(studentToUnfollow);
    }

    public void savePost(PostMessage msg) {
        this.posts.add(msg);
    }

    public void savePM(PMMessage msg) {
        this.pms.add(msg);
    }

    public void backupNotification(NotificationMessage msg) {
        this.backupNotifications.add(msg);
    }

    public NotificationMessage getBackupNotification() {
        return this.backupNotifications.poll();
    }

    public boolean isBlocking(BGSStudent other) {
        return this.blockedStudents.contains(other);
    }

    public void block(BGSStudent other) {
        // Attention: No need to delete backup msgs from 'other'

        // Add other to blockedStudent
        this.blockedStudents.add(other);
        
        // Unfollow each other.
        this.unfollow(other);
        other.unfollow(this);
    }
}
