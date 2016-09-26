package com.techsupportapp.databaseClasses;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Класс, агрегирующий регистрационные данные подтвержденного пользователя.
 * Основной {@link #User(String, String, String, boolean) конструктор}.
 * @author Monarch
 */
public class User {

    //region Constants

    /**
     * Роль простого пользователя.
     */
    final static int SIMPLE_USER = 0;

    /**
     * Член отдела поддержки. Роль консультанта пользователей.
     */
    final static int DEPARTMENT_MEMBER = 1;

    /**
     * Администратор компании, ответственен за управление базой данных. Роль консультанта пользователей.
     */
    final static int ADMINISTRATOR = 2;

    /**
     * Начальник отдела поддержки. Роль консультанта пользователей.
     */
    final static int DEPARTMENT_CHIEF = 4;

    //endregion

    //region Fields

    /**
     * Идентификатор узла, объединяющего данные одного объекта класса в базе данных.
     */
    private String branchId;

    /**
     * Уникальный идентификатор пользователя.
     */
    private String userId;

    /**
     * Имя пользователя.
     */
    private String userName;

    /**
     * Рабочее место пользователя.
     */
    private String workPlace;

    /**
     * Флаг, показывающий, заблокирован ли пользователь.
     */
    private boolean isBlocked;

    /**
     * Логин пользователя.
     */
    private String login;

    /**
     * Пароль пользователя.
     */
    private String password;

    /**
     * Флаг, показывающий, наделен ли пользователь правами администратора.
     */
    private boolean isAdmin;

    /**
     * Дата регистрации пользователя.
     */
    private String registrationDate;

    //endregion

    //region Constructors

    /**
     * Конструктор по-умолчанию.
     * Используется для восстановления данных из базы данных.
     */
    public User() {

    }

    /**
     * Конструктор, использующийся для добавления новых подтвержденных пользователей в систему.
     * @param branchId Задает идентификатор узла, объединяющего данные одного объекта класса в базе данных.
     * @param login Задает логин пользователя.
     * @param password Задает пароль пользователя.
     * @param isAdmin Задает флаг, показывающий, наделен ли пользователь правами администратора.
     * @param userId
     * @param userName
     * @param workPlace
     * @param isBlocked
     */
    public User(String branchId, String login, String password, boolean isAdmin, String userId, String userName, String workPlace, boolean isBlocked) {
        this.branchId = branchId;
        this.login = login;
        this.password = password;
        this.isAdmin = isAdmin;

        this.userId = userId;
        this.userName = userName;
        this.workPlace = workPlace;
        this.isBlocked = isBlocked;

        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        this.registrationDate = formatter.format(Calendar.getInstance().getTime());
    }

    /**
     * Конструктор, использующийся для добавления новых подтвержденных пользователей в систему с заданием определенной даты регистрации.
     * @param branchId Задает идентификатор узла, объединяющего данные одного объекта класса в базе данных.
     * @param login Задает логин пользователя.
     * @param password Задает пароль пользователя.
     * @param isAdmin Задает флаг, показывающий, наделен ли пользователь правами администратора.
     * @param registrationDate Задает дату регистрации пользователя.
     */
    public User(String branchId, String login, String password, boolean isAdmin, String registrationDate) {
        this.branchId = branchId;
        this.login = login;
        this.password = password;
        this.isAdmin = isAdmin;
        this.registrationDate = registrationDate;
    }

    //endregion

    //region Getters

    /**
     * @return Идентификатор узла, объединяющего данные одного объекта класса в базе данных.
     */
    public String getBranchId() {
        return branchId;
    }

    /**
     * @return Логин пользователя.
     */
    public String getLogin() {
        return login;
    }

    /**
     * @return Пароль пользователя.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return Флаг, показывающий, наделен ли пользователь правами администратора.
     */
    public boolean getIsAdmin() {
        return isAdmin;
    }

    /**
     * @return Дату регистрации пользователя.
     */
    public String getRegistrationDate() {
        return registrationDate;
    }

    /**
     * @return Уникальный идентификатор пользователя.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @return Имя пользователя.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return Рабочее место пользователя.
     */
    public String getWorkPlace() {
        return workPlace;
    }

    /**
     * @return Флаг, показывающий, заблокирован ли пользователь.
     */
    public boolean getIsBlocked() {
        return isBlocked;
    }

    //endregion

}
