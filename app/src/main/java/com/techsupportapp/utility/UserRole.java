package com.techsupportapp.utility;

/**
 * Перечисление для задания ролей пользователей.
 * @author Monarch
 */
public enum UserRole {

    /**
     * Роль простого пользователя.
     */
    User(0),

    /**
     * Член отдела поддержки. Роль консультанта пользователей.
     */
    DepartMember(1),

    /**
     * Администратор компании, ответственен за управление базой данных. Роль консультанта пользователей.
     */
    Admin(2),

    /**
     * Начальник отдела поддержки. Роль консультанта пользователей.
     */
    DepartChief(4);

    /**
     * Условный показатель роли.
     */
    private int value;

    /**
     * Основной конструктор для создания ролей в системе.
     * @param value Условный показатель роли.
     */
    private UserRole(int value) {
        this.value = value;
    }

}
