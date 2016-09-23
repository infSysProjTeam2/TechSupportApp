package com.techsupportapp.databaseClasses;

/**
 * Класс, агрегирующий регистрационные данные подтвержденного пользователя.
 * Основной {@link #User(String, String, String, boolean) конструктор}.
 * @author Monarch
 */
public class User {

    //region Fields

    /**
     * Идентификатор узла, объединяющего данные одного объекта класса в базе данных.
     */
    private String branchId;

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
     */
    public User(String branchId, String login, String password, boolean isAdmin) {
        this.branchId = branchId;
        this.login = login;
        this.password = password;
        this.isAdmin = isAdmin;
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

    //endregion

}
