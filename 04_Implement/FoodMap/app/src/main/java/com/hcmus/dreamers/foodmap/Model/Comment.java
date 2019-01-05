package com.hcmus.dreamers.foodmap.Model;


import java.io.Serializable;
import java.util.Date;

public class Comment implements Serializable {
    private Date dateTime;
    private String comment;
    private String emailGuest;
    private String emailOwner;

    public Comment() {
        this.emailGuest = "";
        this.emailOwner = "";
        this.dateTime = new Date();
    }

    public Comment(Date dateTime, String comment, String emailGuest, String emailOwner) {
        this.dateTime = dateTime;
        this.comment = comment;
        this.emailGuest = emailGuest;
        this.emailOwner = emailOwner;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getEmailGuest() {
        return emailGuest;
    }

    public void setEmailGuest(String emailGuest) {
        this.emailGuest = emailGuest;
    }

    public String getEmailOwner() {
        return emailOwner;
    }

    public void setEmailOwner(String emailOwner) {
        this.emailOwner = emailOwner;
    }
}
