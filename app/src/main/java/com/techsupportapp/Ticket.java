package com.techsupportapp;

public class Ticket {

    public String userId;
    public String adminId;
    public String topic;
    public String message;

    public Ticket() {

    }

    public Ticket(String userId, String topic, String message) {
        this.userId = userId;
        this.topic = topic;
        this.message = message;
        this.adminId = null;
    }

    public void addAdmin(String adminId){
        this.adminId = adminId;
    }
}
