package com.techsupportapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.security.MessageDigest;

public class SignInActivity extends AppCompatActivity {

    //region Composite Controls

    final String appId = "E78031C1-13EA-4B71-A80A-53120BD37E3F";
    String userId = generateDeviceUUID(SignInActivity.this);
    String userName;

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
                    //readPasswordDatabase(); // Временная реализация

                    Intent intent;
                    userName = loginET.getText().toString();

                    if (userName.equals("admin")) {
                        userId = "admin";
                        intent = new Intent(SignInActivity.this, AdminActivity.class);
                    }
                    else {
                        userId = "user";
                        intent = new Intent(SignInActivity.this, UserActivity.class);
                    }
                    Bundle args = AdminActivity.makeSendBirdArgs(appId, userId, userName);
                    intent.putExtras(args);

                    startActivityForResult(intent, 201);
                    //TODO Написать логику входа в приложения через логин/пароль
                }
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

    private String generateDeviceUUID(Context context) {
        String serial = Build.SERIAL;
        String androidID = Settings.Secure.ANDROID_ID;
        String deviceUUID = serial + androidID;

        /*
         * SHA-1
         */
        MessageDigest digest;
        byte[] result;
        try {
            digest = MessageDigest.getInstance("SHA-1");
            result = digest.digest(deviceUUID.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(String.format("%02X", b));
        }


        return sb.toString();
    }
}
