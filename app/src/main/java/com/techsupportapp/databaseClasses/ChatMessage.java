package com.techsupportapp.databaseClasses;

/**
 * Класс, агрегирующий пользовательские сообщения.
 * @author ahgpoug
 */
public class ChatMessage {

    /**
     * Текст сообщения.
     */
    private String message;

    /**
     * UserName пользователя, отправившего сообщение.
     */
    private String author;

    /**
     * Время отправки сообщения.
     */
    private String messageTime;

    /**
     * Login пользователя, отправившего сообщение.
     */
    private String userId;

    /**
     * Было ли прочитано сообщение.
     */
    private boolean unread;

    /**
     * Конструктор по-умолчанию.
     */
    private ChatMessage() {
    }

    /**
     * Создание нового экземпляра сообщения
     * @param message Текст сообщенияю
     * @param author UserName пользователя, отправившего сообщение.
     * @param userId Login пользователя, отправившего сообщение.
     * @param messageTime Время отправки сообщения.
     * @param unread Было ли прочитано сообщение.
     */
    public ChatMessage(String message, String author, String userId, String messageTime, boolean unread) {
        this.message = message;
        this.author = author;
        this.userId = userId;
        this.messageTime = messageTime;
        this.unread = unread;
    }


    /**
     * @return Текст сообщения.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return UserName автора сообщения.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @return Получение времени отправки сообщения.
     */
    public String getMessageTime() {
        return messageTime;
    }

    /**
     * @return Login автора сообщения.
     */
    public String getUserId() { return userId; }

    /**
     * Было ли прочитано сообщения
     * @return true - не прочитано. false - прочитано.
     */
    public boolean isUnread(){
        return unread;
    }
}
