package com.techsupportapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SignInActivity extends AppCompatActivity {

    //region Constants

    private static final String appId = "E78031C1-13EA-4B71-A80A-53120BD37E3F";

    //endregion

    //region Fields

    private ArrayList<User> userList = new ArrayList<>();

    private DatabaseReference databaseReference;
    private boolean isConnectedToInternet;

    //endregion

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

        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void setEvents() {
        closeAppBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        signInBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasConnection()) {
                    if (loginET.getText().toString().equals("")) {
                        loginET.requestFocus();
                        Toast.makeText(getApplicationContext(), "Введите логин", Toast.LENGTH_LONG).show();
                    } else if (passwordET.getText().toString().equals("")) {
                        passwordET.requestFocus();
                        Toast.makeText(getApplicationContext(), "Введите пароль", Toast.LENGTH_LONG).show();
                    } else if (userList.size() == 0){
                        Toast.makeText(getApplicationContext(), "База данных пуста. " +
                                "Зарегистрируйте компанию у нас", Toast.LENGTH_LONG).show(); //Заготовка
                    } else {
                        int i = 0;
                        while (!loginET.getText().toString().equals(userList.get(i).login) && ++i < userList.size()); //TODO binarySearch
                        if (i >= userList.size())
                            Toast.makeText(getApplicationContext(), "Логин и/или пароль введен неверно. Повторите попытку", Toast.LENGTH_LONG).show();
                        else if (loginET.getText().toString().equals(userList.get(i).login) && passwordET.getText().toString().equals(userList.get(i).password))
                        {
                            Toast.makeText(getApplicationContext(), "Вход выполнен", Toast.LENGTH_LONG).show();

                            Intent intent;
                            String userName;

                            userName = loginET.getText().toString();

                            if (userList.get(i).isAdmin) {
                                intent = new Intent(SignInActivity.this, ListOfChannelsActivity.class);
                            }
                            else {
                                intent = new Intent(SignInActivity.this, ListOfChannelsActivity.class);
                            }

                            Bundle args = ListOfChannelsActivity.makeSendBirdArgs(appId, getId(userName), userName, userList.get(i).isAdmin);

                            intent.putExtras(args);

                            startActivityForResult(intent, 201);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Логин и/или пароль введен неверно. Повторите попытку", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Нет подключения к интернету", Toast.LENGTH_LONG).show();
                }
            }
        });


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot userRecord : dataSnapshot.child(DatabaseVariables.DATABASE_USER_TABLE).getChildren()) {
                    User user = userRecord.getValue(User.class);
                    userList.add(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Ошибка в работе базы данных. " +
                        "Обратитесь к администратору компании или разработчику", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addUser(String user_id, String login, String password) {//Метод для админов, потом перенести. TODO с учетом компании
        User newUser = new User(login, password, false);

        databaseReference.child(DatabaseVariables.DATABASE_USER_TABLE).child(user_id).setValue(newUser);
    }

    private void addUser(String user_id, String login, String password, boolean isAdmin) {//Метод для разработчиков, потом перенести. TODO в базах данных
        User newUser = new User(login, password, isAdmin);

        databaseReference.child(DatabaseVariables.DATABASE_USER_TABLE).child(user_id).setValue(newUser);
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

    private boolean hasConnection() {
        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sign_in_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_user_menu_but:
                addUser("User_" + userList.size(), "user" + userList.size(), "user" + userList.size());
                break;
            /*case R.id.add_admin_menu_but://TODO ВЕРНУТЬ после показа
                addUser("User_" + userList.size(), "admin" + userList.size(), "admin" + userList.size(), true);*/
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getId(String value) //TODO Вернуть
    {
        /*String result = "";
        for (int i = 0; i < value.length(); i++)
            result += (char)(value.charAt(i) + 1);
        return result;*/
        return value;
    }

    /*//TODO ВЕРНУТЬ после показа
     <item
        android:id="@+id/add_admin_menu_but"
        android:orderInCategory="200"
        android:title="Добавить администратора"
        app:showAsAction="never|withText"
        />
     */
}
