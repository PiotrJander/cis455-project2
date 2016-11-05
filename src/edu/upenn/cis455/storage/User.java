package edu.upenn.cis455.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
public class User {

    @PrimaryKey
    private String username;

    private String password;
    private String channels;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.channels = "[]";
    }

    private User() {
    } // For deserialization

    public String getPassword() {
        return password;
    }

    public List<String> getChannelsAsList() {
        try {
            ObjectMapper mapper;
            mapper = new ObjectMapper();
            return Arrays.asList(mapper.readValue(this.channels, String[].class));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getUsername() {
        return username;
    }

    public void addChannel(String channelName) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> channels = new ArrayList<>(Arrays.asList(mapper.readValue(this.channels, String[].class)));
            channels.add(channelName);
            this.channels = mapper.writeValueAsString(channels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeChannel(String channelName) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> channels = new ArrayList<>(Arrays.asList(mapper.readValue(this.channels, String[].class)));
            channels.remove(channelName);
            this.channels = mapper.writeValueAsString(channels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
