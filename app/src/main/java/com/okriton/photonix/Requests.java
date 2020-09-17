package com.okriton.photonix;

public class Requests {

    public String date, time;

    public Requests(){

    }

    public Requests(String user_id, String date, String time) {
        this.date = date;
        this.time = time;
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
}
