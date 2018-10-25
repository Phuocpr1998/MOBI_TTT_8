package com.hcmus.dreamers.foodmap.model;

import java.util.Date;

public class Comment {
    private Date dateTime;
    private String comment;
    private User user;

    public Comment() {
    }

    public Comment(Date dateTime, String comment, User user) {
        this.dateTime = dateTime;
        this.comment = comment;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
}
