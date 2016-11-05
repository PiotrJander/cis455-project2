package edu.upenn.cis455.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class User {

    @PrimaryKey
    private String username;

    private String password;
    private String channels;

    User(String username, String password) {
        this.username = username;
        this.password = password;
        this.channels = "[]";
    }

    private User() {
    } // For deserialization

    public String getPassword() {
        return password;
    }

    public String getChannels() {
        return channels;
    }

    public String getUsername() {
        return username;
    }
}
