package com.techsupportapp.chat;

public class Chat {

    private String message;
    private String author;
    private String messageTime;
    private String userId;

    private Chat() {
    }

    public Chat(String message, String author, String userId, String messageTime) {
        this.message = message;
        this.author = author;
        this.userId = userId;
        this.messageTime = messageTime;
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

}
