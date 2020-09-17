package com.okriton.photonix;

public class Likes {

    public String name;
    public long timestamp;

    public Likes() {

    }

    public Likes(String name, long timestamp) {
        this.name = name;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
