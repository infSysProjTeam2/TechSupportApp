package com.techsupportapp.databaseClasses;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Класс, агрегирующий данные заявки пользователя.
 * Основной {@link #Ticket(String ticketId, String userId, String userName, String topic, String message) конструктор}.
 * @author Monarch
 */
public class Ticket implements Serializable {

    //region Constants

    /**
     * Новая, не рассмотренная заявка.
     */
    public final static int NOT_ACCEPTED = 0;

    /**
     * Заявка, находящаяся на рассмотрении у консультанта.
     */
    public final static int ACCEPTED = 1;

    /**
     * Заявка, переданная для других консультантов.
     */
    public final static int SUBMITTED = 2;

    /**
     * Заявка, решение которой было подтверждено пользователем - создателем заявки.
     */
    public final static int CONFIRMED_BY_USER = 4;

    /**
     * Решенная заявка.
     */
    public final static int SOLVED = 8;

    public final static int TYPE_SOFTWARE = 10;
    public final static int TYPE_HARDWARE = 11;
    public final static int TYPE_EXTERNAL_DEVICES = 12;
    public final static int TYPE_NETWORK = 13;

    //endregion

    //region Fields

    /**
     * Состояние заявки.
     */
    private int ticketState;

    /**
     * Идентификатор заявки.
     */
    private String ticketId;

    /**
     * Идентификатор пользователя - создателя заявки.
     */
    private String userId;

    /**
     * Идентификатор консультанта, отвечающего за решение заявки.
     */
    private String specialistId;

    /**
     * Имя консультанта, отвещающего за решение заявки.
     */
    private String specialistName;

    /**
     * Имя пользователя - создателя заявки.
     */
    private String userName;

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
    private String createDate;

    /**
     * Категория заявки.
     */
    private int type;

    //endregion

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
    public Ticket(String ticketId, int type, String userId, String userName, String topic, String message) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.userName = userName;
        this.topic = topic;
        this.message = message;
        this.type = type;
        this.specialistId = null;
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        this.createDate = formatter.format(Calendar.getInstance().getTime());
        this.ticketState = Ticket.NOT_ACCEPTED;
    }

    /**
     * Метод, задающий консультанта, который будет решать проблему.
     * @param adminId Идентификатор консультанта, отвечающего за решение заявки.
     * @param adminName Имя консультанта, отвещающего за решение заявки.
     */
    public void addSpecialist(String adminId, String adminName) {
        this.specialistId = adminId;
        this.specialistName = adminName;
        this.ticketState = Ticket.ACCEPTED;
    }

    /**
     * Метод, отзывающий консультанта и переводящий заявку в список нерассматриваемых.
     */
    public void removeSpecialist(){
        this.specialistId = null;
        this.specialistName = null;
        this.ticketState = Ticket.NOT_ACCEPTED;
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
    public String getSpecialistId() {
        return specialistId;
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
    public String getCreateDate() {
        return createDate;
    }

    /**
     * @return Состояние заявки.
     */
    public int getTicketState() {
        return ticketState;
    }

    /**
     * @return Имя пользователя - создателя заявки.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return Имя консультанта, отвещающего за решение заявки.
     */
    public String getSpecialistName() {
        return specialistName;
    }

    /**
     * @return Категория заявки
     */
    public int getType(){
        return type;
    }
}
