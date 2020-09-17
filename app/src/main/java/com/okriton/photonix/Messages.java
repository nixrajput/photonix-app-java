package com.okriton.photonix;

public class Messages {

    public String message, type, from;
    public String date, time, device_token;
    public boolean seen;

    public Messages(){

    }

    public Messages(String message, String type, String from, String date, String time, String device_token, boolean seen) {
        this.message = message;
        this.type = type;
        this.from = from;
        this.date = date;
        this.time = time;
        this.device_token = device_token;
        this.seen = seen;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
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

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
