package com.okriton.photonix;

public class Comments {

    public String user_id, username, comment, date, time;
    public long timestamp;

    public Comments(){

    }

    public Comments(String user_id, String username, String comment, String date, String time, long timestamp) {
        this.user_id = user_id;
        this.username = username;
        this.comment = comment;
        this.date = date;
        this.time = time;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
