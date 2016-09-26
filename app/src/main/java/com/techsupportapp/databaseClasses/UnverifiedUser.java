package com.techsupportapp.databaseClasses;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Класс, агрегирующий регистрационные данные неподтвержденного пользователя.
 * Основной {@link #UnverifiedUser(String, String, String, boolean) конструктор}.
 * @author Monarch
 */
public class UnverifiedUser {

    //region Fields

    /**
     * Идентификатор узла, объединяющего данные одного объекта класса в базе данных.
     */
    private String branchId;

    /**
     * Логин неподтвержденного пользователя.
     */
    private String login;

    /**
     * Пароль неподтвержденного пользователя.
     */
    private String password;

    /**
     * Флаг, показывающий, будет ли наделен неподтвержденный пользователь правами администратора после подтверждения.
     */
    private boolean isAdmin;

    /**
     * Дата создания аккаунта для подтверждения.
     */
    private String registrationDate;

    //endregion

    //region Constructors

    /**
     * Конструктор по-умолчанию.
     * Используется для восстановления данных из базы данных.
     */
    public UnverifiedUser() {

    }

    /**
     * Конструктор, использующийся для добавления новых неподтвержденных пользователей в систему.
     * @param branchId Задает идентификатор узла, объединяющего данные одного объекта класса в базе данных.
     * @param login Задает логин неподтвержденного пользователя.
     * @param password Задает пароль неподтвержденного пользователя.
     * @param isAdmin Задает флаг, показывающий, будет ли наделен неподтвержденный пользователь правами администратора после подтверждения.
     */
    public UnverifiedUser(String branchId, String login, String password, boolean isAdmin) {
        this.branchId = branchId;
        this.login = login;
        this.password = password;
        this.isAdmin = isAdmin;
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        this.registrationDate = formatter.format(Calendar.getInstance().getTime());
    }

    //endregion

    //region Getters

    /**
     * Метод, переводящий данного пользователя в категорию подтвержденных.
     * @return Подвержденного пользователя.
     */
    public User verifyUser() {
        return new User(this.branchId, this.login, this.password, this.isAdmin, this.branchId,
                this.login, "Wayward Pines", false);
    }

    /**
     * @return Идентификатор узла, объединяющего данные одного объекта класса в базе данных.
     */
    public String getBranchId() {
        return branchId;
    }

    /**
     * @return Логин неподтвержденного пользователя.
     */
    public String getLogin() {
        return login;
    }

    /**
     * @return Пароль неподтвержденного пользователя.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return Флаг, показывающий, будет ли наделен неподтвержденный пользователь правами администратора после подтверждения.
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * @return Дату создания аккаунта для подтверждения.
     */
    public String getRegistrationDate() {
        return registrationDate;
    }

    //endregion

}
