package com.okriton.photonix;

import java.util.Date;

public class Posts {

    public String user_id, image_url, caption, username;
    public String post_date, post_time, post_id;
    public long post_count, timestamp;

    public Posts(){

    }

    public Posts(String user_id, String image_url, String caption, String username, String post_date, String post_time, String post_id, long post_count, long timestamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.caption = caption;
        this.username = username;
        this.post_date = post_date;
        this.post_time = post_time;
        this.post_id = post_id;
        this.post_count = post_count;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPost_date() {
        return post_date;
    }

    public void setPost_date(String post_date) {
        this.post_date = post_date;
    }

    public String getPost_time() {
        return post_time;
    }

    public void setPost_time(String post_time) {
        this.post_time = post_time;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public long getPost_count() {
        return post_count;
    }

    public void setPost_count(long post_count) {
        this.post_count = post_count;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
