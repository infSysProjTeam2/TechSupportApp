package com.techsupportapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ValueEventListener;
import com.sendbird.android.shadow.okhttp3.internal.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;;

public class SignInActivity extends AppCompatActivity {

    //region Constants

    private static final String DATABASE_USER_TABLE = "user_table";

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

        checkInternet();
    }

    private void checkInternet()
    {
        new Thread(new Runnable() {
            @Override
            public void run(){
                isConnectedToInternet = hasConnection();
                if (!isConnectedToInternet) {
                    Toast.makeText(getApplicationContext(), "Нет подключения к Интернету", Toast.LENGTH_LONG).show();;
                }
            }
        }).start();
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
                checkInternet();
                if (isConnectedToInternet) {
                    if (loginET.getText().toString().equals("")) {
                        loginET.requestFocus();
                        Toast.makeText(getApplicationContext(), "Введите логин", Toast.LENGTH_LONG).show();;
                    } else if (passwordET.getText().toString().equals("")) {
                        passwordET.requestFocus();
                        Toast.makeText(getApplicationContext(), "Введите пароль", Toast.LENGTH_LONG).show();;
                    } else if (userList.size() == 0){
                        Toast.makeText(getApplicationContext(), "База данных пуста. " +
                                "Зарегистрируйте компанию у нас", Toast.LENGTH_LONG).show();;
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
                                intent = new Intent(SignInActivity.this, AdminActivity.class);
                            }
                            else {
                                intent = new Intent(SignInActivity.this, UserActivity.class);
                            }
                            Bundle args = AdminActivity.makeSendBirdArgs(appId, userName, userName);
                            intent.putExtras(args);

                            startActivityForResult(intent, 201);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Логин и/или пароль введен неверно. Повторите попытку", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot userRecord : dataSnapshot.child(DATABASE_USER_TABLE).getChildren()) {
                    User user = userRecord.getValue(User.class);
                    userList.add(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Ошибка в работе базы данных. " +
                        "Обратитесь к администратору компании или разработчику", Toast.LENGTH_LONG).show();;
            }
        });
    }

    private void addUser(String user_id, String login, String password) {//Метод для админов, потом перенести. TODO с учетом компании
        User newUser = new User(login, password, false);

        databaseReference.child(DATABASE_USER_TABLE).child(user_id).setValue(newUser);
    }

    private void addUser(String user_id, String login, String password, boolean isAdmin) {//Метод для разработчиков, потом перенести. TODO в базах данных
        User newUser = new User(login, password, isAdmin);

        databaseReference.child(DATABASE_USER_TABLE).child(user_id).setValue(newUser);
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

    private boolean hasConnection()
    {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            try {
                URL url = new URL("http://www.google.com/");
                HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
                urlc.setRequestProperty("User-Agent", "test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1000);
                urlc.connect();
                if (urlc.getResponseCode() == 200) {
                    return true;
                }
                return false;

            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Ошибка проверки подключения к Интернету. " +
                        "Обратитесь к администратору компании или разработчику", Toast.LENGTH_LONG).show();;
                return false;
            }
        }

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

    /*//TODO ВЕРНУТЬ после показа
     <item
        android:id="@+id/add_admin_menu_but"
        android:orderInCategory="200"
        android:title="Добавить администратора"
        app:showAsAction="never|withText"
        />
     */
}
