package com.techsupportapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tryToConnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseReference.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseReference.removeEventListener(valueEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(valueEventListener);
    }

    private void tryToConnect(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getString("Login","").equals("") || preferences.getString("Password","").equals("")){
            showSignInActivity();
        } else {
            if (hasConnection()) {
                valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<User> userList = Globals.Downloads.getVerifiedUserList(dataSnapshot);
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);
                        checkVerificationData(userList, preferences.getString("Login", ""), preferences.getString("Password", ""));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "Ошибка в работе базы данных. Обратитесь к администратору компании или разработчику", Toast.LENGTH_LONG).show();
                        SplashActivity.this.finish();
                    }
                };
                databaseReference = FirebaseDatabase.getInstance().getReference();
            } else {
                Toast.makeText(getApplicationContext(), "Нет подключения к интернету", Toast.LENGTH_LONG).show();
                showSignInActivity();
            }
        }
    }

    private void showSignInActivity(){
        startActivity(new Intent(SplashActivity.this, SignInActivity.class));
        SplashActivity.this.finish();
    }

    private void signIn(User user){
        Toast.makeText(getApplicationContext(), "Вход выполнен", Toast.LENGTH_LONG).show();

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("allowNotifications", false))
            startService(new Intent(this, MessagingService.class));

        Globals.currentUser = user;
        startActivity(new Intent(SplashActivity.this, AcceptedTicketsActivity.class));
        SplashActivity.this.finish();
    }

    private void checkVerificationData(ArrayList<User> userList, String login, String password) {
        int i = 0;
        while (!login.equals(userList.get(i).getLogin()) && ++i < userList.size());
        if (i >= userList.size()) {
            Toast.makeText(getApplicationContext(), "Логин и/или пароль введен неверно. Повторите попытку", Toast.LENGTH_LONG).show();
            showSignInActivity();
        }
        else if (login.equals(userList.get(i).getLogin()) && password.equals(userList.get(i).getPassword()))
            signIn(userList.get(i));
        else {
            Toast.makeText(getApplicationContext(), "Логин и/или пароль введен неверно. Повторите попытку", Toast.LENGTH_LONG).show();
            showSignInActivity();
        }
    }

    private boolean hasConnection() {
        ConnectivityManager cm = (ConnectivityManager)SplashActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork.isConnectedOrConnecting();
    }
}