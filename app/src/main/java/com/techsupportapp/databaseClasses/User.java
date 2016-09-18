package com.techsupportapp.databaseClasses;

import java.io.Serializable;

public class User implements Serializable {

    public String userId;
    public String login;
    public String password;
    public boolean isAdmin;

    public User() {

    }

    public User(String userId, String login, String password, boolean isAdmin) {
        this.userId = userId;
        this.login = login;
        this.password = password;
        this.isAdmin = isAdmin;
    }

}
