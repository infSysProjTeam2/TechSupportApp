package com.techsupportapp.databaseClasses;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UnverifiedUser {

    public String userId;
    public String login;
    public String password;
    public boolean isAdmin;
    public String date;

    public UnverifiedUser() {

    }

    public UnverifiedUser(String userId, String login, String password, boolean isAdmin) {
        this.userId = userId;
        this.login = login;
        this.password = password;
        this.isAdmin = isAdmin;
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        this.date = formatter.format(Calendar.getInstance().getTime());
    }

    public User verifyUser() {
        return new User(this.userId, this.login, this.password, this.isAdmin);
    }

}
