package com.techsupportapp.databaseClasses;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.SimpleFormatter;

public class Ticket {

    public String ticketId;
    public String userId;
    public String adminId;
    public String topic;
    public String message;
    public String date;

    public Ticket() {

    }

    public Ticket(String ticketId, String userId, String topic, String message) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.topic = topic;
        this.message = message;
        this.adminId = null;
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        this.date = formatter.format(Calendar.getInstance().getTime());
    }

    public void addAdmin(String adminId) {
        this.adminId = adminId; }

    @Override
    public String toString() {
        return "ID: " + ticketId + " Тема: " + topic + " Сообщение: " + message;
    }
}
