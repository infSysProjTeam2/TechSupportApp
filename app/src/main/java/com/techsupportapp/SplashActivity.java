package com.techsupportapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.services.MessagingService;
import com.techsupportapp.utility.Globals;

import java.io.IOException;
import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private ArrayList<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tryToConnect();
        finish();
    }

    private void tryToConnect(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getString("Login","").equals("") || preferences.getString("Password","").equals("")){
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else {
            if (hasConnection()) {
                valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        userList = Globals.Downloads.getVerifiedUserList(dataSnapshot);

                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);
                        for (User user : userList)
                            if (user.getLogin().equals(preferences.getString("Login", "")))
                                signIn(user);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "Ошибка в работе базы данных. Обратитесь к администратору компании или разработчику", Toast.LENGTH_LONG).show();
                        finish();
                    }
                };
                databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.addValueEventListener(valueEventListener);
            } else
                Toast.makeText(getApplicationContext(), "Нет подключения к интернету", Toast.LENGTH_LONG).show();
        }
    }

    private void signIn(User user){
        Toast.makeText(getApplicationContext(), "Вход выполнен", Toast.LENGTH_LONG).show();

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("allowNotifications", false))
            startService(new Intent(this, MessagingService.class));

        Globals.currentUser = user;
        databaseReference.removeEventListener(valueEventListener);
        startActivity(new Intent(SplashActivity.this, AcceptedTicketsActivity.class));
    }

    private boolean hasConnection() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }
}