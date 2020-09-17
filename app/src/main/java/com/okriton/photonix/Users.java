package com.okriton.photonix;

public class Users {

    public String name, image, reg_date;

    public Users(){

    }

    public Users(String name, String image, String reg_date) {
        this.name = name;
        this.image = image;
        this.reg_date = reg_date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDate() {
        return reg_date;
    }

    public void setDate(String date) {
        this.reg_date = date;
    }
}
