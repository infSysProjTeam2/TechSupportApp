package com.techsupportapp;

public class Ticket {

    public String ticketId;
    public String userId;
    public String adminId;
    public String topic;
    public String message;

    public Ticket() {

    }

    public Ticket(String ticketId, String userId, String topic, String message) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.topic = topic;
        this.message = message;
        this.adminId = null;
    }

    public void addAdmin(String adminId){
        this.adminId = adminId; }

    @Override
    public String toString() {
        return "ID: " + ticketId + " Тема: " + topic + " Сообщение: " + message;
    }
}
