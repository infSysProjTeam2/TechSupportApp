package com.techsupportapp.databaseClasses;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Класс, агрегирующий регистрационные данные подтвержденного пользователя.
 * Основной {@link #User(String branchId, String login, String password, int role, String userId, String workPlace, boolean isBlocked) конструктор}.
 * @author Monarch
 */
public class User {

    //region Constants

    /**
     * Роль простого пользователя.
     */
    public final static int SIMPLE_USER = 0;

    /**
     * Член отдела поддержки. Роль консультанта пользователей.
     */
    public final static int DEPARTMENT_MEMBER = 1;

    /**
     * Администратор компании, ответственен за управление базой данных. Роль консультанта пользователей.
     */
    public final static int ADMINISTRATOR = 2;

    /**
     * Начальник отдела поддержки. Роль консультанта пользователей.
     */
    public final static int DEPARTMENT_CHIEF = 4;

    //endregion

    //region Fields

    /**
     * Идентификатор узла, объединяющего данные одного объекта класса в базе данных.
     */
    private String branchId;

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
     * Дата регистрации пользователя.
     */
    private String registrationDate;

    /**
     * Роль данного аккаунта в системе.
     */
    private int role;

    /**
     * Имя пользователя.
     */
    private String userName;

    /**
     * Рабочее место пользователя.
     */
    private String workPlace;

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
     * @param role Задает флаг, показывающий, наделен ли пользователь правами администратора.
     * @param userName Имя пользователя.
     * @param workPlace Рабочее место пользователя.
     * @param isBlocked Флаг, показывающий, заблокирован ли пользователь.
     */
    public User(String branchId, boolean isBlocked, String login, String password, int role, String userName, String workPlace) throws Exception {
        this.branchId = branchId;
        this.login = login;
        this.password = password;
        if (role != 0 && role != 1 && role != 2 && role != 4)
            throw new IllegalArgumentException("Аргумент role имеет не дозволительное значение. Используте класс User");

        this.role = role;

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
     * @param role Задает флаг, показывающий, наделен ли пользователь правами администратора.
     * @param registrationDate Задает дату регистрации пользователя.
     */
    public User(String branchId, String login, String password, int role, String registrationDate) {
        this.branchId = branchId;
        this.login = login;
        this.password = password;
        this.role = role;
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
     * @return Флаг, показывающий, заблокирован ли пользователь.
     */
    public boolean getIsBlocked() {
        return isBlocked;
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
     * @return Дату регистрации пользователя.
     */
    public String getRegistrationDate() {
        return registrationDate;
    }

    /**
     * @return Флаг, показывающий, наделен ли пользователь правами администратора.
     */
    public int getRole() {
        return role;
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

    //endregion

}
