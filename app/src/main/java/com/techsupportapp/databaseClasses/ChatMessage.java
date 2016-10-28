package com.techsupportapp.databaseClasses;

public class ChatMessage {

    private String message;
    private String author;
    private String messageTime;
    private String userId;
    private boolean unread;

    private ChatMessage() {
    }

    public ChatMessage(String message, String author, String userId, String messageTime, boolean unread) {
        this.message = message;
        this.author = author;
        this.userId = userId;
        this.messageTime = messageTime;
        this.unread = unread;
    }

    public String getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public String getUserId() { return userId; }

    public boolean isUnread(){
        return unread;
    }
}
