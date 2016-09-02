package com.techsupportapp;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class SignInActivity extends AppCompatActivity {

    //region Composite Controls

    private Button closeAppBut;
    private Button signInBut;

    private EditText loginET;
    private EditText passwordET;

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
                readPasswordDatabase(); // Временная реализация
                //TODO Написать логику входа в приложения через логин/пароль
            }
        });
    }

    private void loadPasswordDatabase()
    {
        //TODO Скачивание базы данных с паролями с сервера.
    }

    private void readPasswordDatabase()
    {
        File file = new File("/sdcard/Android/data/com.techsuppotapp/passwordDatabase.db");
        File directory = new File("/sdcard/Android/data/com.techsuppotapp/");

        if (!file.exists())
        {
            if (!directory.exists())
                directory.mkdirs();
            //TODO используем метод loadPasswordDatabase
        }

        DatabaseOpenHelper helper = new DatabaseOpenHelper(this, file.getPath(), 1); //Временное решение до реализации скачивания базы с сервера
        SQLiteDatabase base = helper.getReadableDatabase();
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
}
