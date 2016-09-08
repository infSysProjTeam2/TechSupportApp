package com.techsupportapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class SignInActivity extends AppCompatActivity {

    //region Composite Controls

    private Button closeAppBut;
    private Button signInBut;

    private EditText loginET;
    private EditText passwordET;
    private CheckBox rememberPas;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        initializeComponents();

        setEvents();
    }

    private void initializeComponents() {
        closeAppBut = (Button)findViewById(R.id.closeAppButton);
        signInBut = (Button)findViewById(R.id.signInButton);

        loginET = (EditText)findViewById(R.id.loginET);
        passwordET = (EditText)findViewById(R.id.passwordET);
        rememberPas = (CheckBox)findViewById((R.id.checkBoxBold));
    }

    private void setEvents()
    {
        closeAppBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        signInBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginET.getText().toString().equals(""))
                {
                    loginET.requestFocus();
                    Toast.makeText(getApplicationContext(), "Введите логин", Toast.LENGTH_LONG);
                }
                else if (passwordET.getText().toString().equals(""))
                {
                    passwordET.requestFocus();
                    Toast.makeText(getApplicationContext(), "Введите пароль", Toast.LENGTH_LONG);
                }
                else {
                    savePassAndLogin();
                    readPasswordDatabase(); // Временная реализация
                    //TODO Написать логику входа в приложения через логин/пароль
                }
            }
        });
    }

    private void loadPasswordDatabase()
    {
        //TODO Скачивание базы данных с паролями с сервера.
    }
    private void savePassAndLogin(){
        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();
        if (rememberPas.isChecked()) {
            String login = loginET.getText().toString();
            String password = passwordET.getText().toString();
            editor.putString("Login", login);
            editor.putString("Password", password);
        }
        else editor.clear();
           editor.commit();
    }
    private void readPasswordDatabase()
    {
        File file = new File("/sdcard/Android/data/com.techsuppotapp/passwordDatabase.db");
        File directory = new File("/sdcard/Android/data/com.techsuppotapp/");

        if (!directory.exists())
            directory.mkdirs();

        /*DatabaseOpenHelper helper = new DatabaseOpenHelper(this, file.getPath(), 1); //Временное решение до реализации скачивания базы с сервера
        SQLiteDatabase base = helper.getReadableDatabase();
        Cursor resultList = base.query(false, "personality_table", new String[] {"login", "password"}, null, null, null, null, null, null);
        resultList.moveToFirst();
        if (resultList.getCount() == 0)
        {
            Toast.makeText(getApplicationContext(), "Введен неверный логин или пароль", Toast.LENGTH_LONG);
        }
        else if (resultList.getString(resultList.getColumnIndex("login")).equals(loginET.getText().toString()))
        {
            Toast.makeText(getApplicationContext(), "Вход выполнен успешно!!!", Toast.LENGTH_LONG);
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Ошибка базы данных. Обратитесь к администратору компании или разработчику", Toast.LENGTH_LONG);
        }*/

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setPositiveButton("Закрыть приложение", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SignInActivity.super.onBackPressed();
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setCancelable(false);
        builder.setMessage("Вы действительно хотите закрыть приложение?");
        builder.show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getPreferences(0);
        String login = settings.getString("Login","");
        String password = settings.getString("Password","");
        loginET.setText(login);
        passwordET.setText(password);
    }
    }

