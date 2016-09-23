package com.techsupportapp.databaseClasses;

import com.techsupportapp.utility.TicketState;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Класс, агрегирующий данные заявки пользователя.
 * Основной {@link #Ticket(String, String, String, String) конструктор}.
 * @author Monarch
 */
public class Ticket {

    /**
     * Состояние заявки.
     */
    private TicketState ticketState;

    /**
     * Идентификатор заявки.
     */
    private String ticketId;

    /**
     * Идентификатор пользователя - создателя заявки.
     */
    private String userId;

    /**
     * Идентификатор администратора отвечающего за решение заявки.
     */
    private String adminId;

    /**
     * Тема заявки.
     */
    private String topic;

    /**
     * Сообщение - описание проблемы в заявке.
     */
    private String message;

    /**
     * Дата создания заявки.
     */
    private String date;

    /**
     * Конструктор по-умолчанию.
     */
    public Ticket() {

    }

    /**
     * Конструктор, использующийся для добавления новых заявок пользователей в систему.
     * @param ticketId Идентификатор заявки.
     * @param userId Идентификатор пользователя - создателя заявки.
     * @param topic Тема заявки.
     * @param message Сообщение - описание проблемы в заявке.
     */
    public Ticket(String ticketId, String userId, String topic, String message) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.topic = topic;
        this.message = message;
        this.adminId = null;
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        this.date = formatter.format(Calendar.getInstance().getTime());
        this.ticketState = TicketState.NotAccepted;
    }

    /**
     * Метод, задающий администратора, который будет решать проблему.
     * @param adminId
     */
    public void addAdmin(String adminId) {
        this.adminId = adminId;
        this.ticketState = TicketState.Accepted;
    }

    /**
     * @return Идентификатор заявки.
     */
    public String getTicketId() {
        return ticketId;
    }

    /**
     * @return Идентификатор пользователя - создателя заявки.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @return Идентификатор администратора отвечающего за решение заявки.
     */
    public String getAdminId() {
        return adminId;
    }

    /**
     * @return Тему заявки.
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @return Сообщение - описание проблемы в заявке.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return Дату создания заявки.
     */
    public String getDate() {
        return date;
    }

    /**
     * @return Состояние заявки.
     */
    public TicketState getTicketState() {
        return ticketState;
    }

}
