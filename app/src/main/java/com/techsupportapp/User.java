package com.techsupportapp;

import java.io.Serializable;

public class User implements Serializable {

    public String login;
    public String password;
    public boolean isAdmin;

    public User() {

    }

    public User(String login, String password, boolean isAdmin) {
        this.login = login;
        this.password = password;
        this.isAdmin = isAdmin;
    }

}
