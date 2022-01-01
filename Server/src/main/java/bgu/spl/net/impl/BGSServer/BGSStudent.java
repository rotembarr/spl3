package bgu.spl.net.impl.BGSServer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class BGSStudent {
    private final String username;
    private final String password;
    private final String birthday;
    private AtomicInteger connectionId; // one thread will write but multiple reads.
    private List<BGSStudent> following; // one thread read and write.
    private Collection<BGSStudent> followers; // multiple therad write and one read.
    
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

    public boolean follow(BGSStudent studentToFollow) {

        // Add followers to other.
        if (!studentToFollow.addFollower(this)) {
            return false;
        }

        // Only in success of the above.
        return this.following.add(studentToFollow);
    }

    protected boolean addFollower(BGSStudent student) {
        return this.followers.add(student);
    }

    protected boolean reomoveFollower(BGSStudent student) {
        return this.followers.remove(student);
    }

    public boolean unfollow(BGSStudent studentToUnfollow) {

        // Add followers to other.
        if (!studentToUnfollow.reomoveFollower(this)) {
            return false;
        }

        return this.following.remove(studentToUnfollow);
    }

    public boolean isFollowing(BGSStudent student) {
        return this.following.contains(student);
    }

    public BGSStudent(String username, String password, String birthday) {
        this.username = username;
        this.password = password;
        this.birthday = birthday;
        this.connectionId.set(-1);
        this.following = new LinkedList<BGSStudent>();
        this.followers = new ConcurrentLinkedDeque<BGSStudent>();
    }
    
}
