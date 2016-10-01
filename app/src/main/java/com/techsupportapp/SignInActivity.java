package com.techsupportapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.GlobalsMethods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Класс для аутентификации пользователей.
 * @author Monarch
 */
public class SignInActivity extends AppCompatActivity {

    //region Fields

    private ArrayList<User> userList = new ArrayList<User>();
    private ArrayList<String> unverifiedLoginList = new ArrayList<String>();

    private DatabaseReference databaseReference;

    //endregion

    //region Composite Controls

    private Button closeAppBut;
    private Button signInBut;
    private Button signUpBut;

    private EditText loginET;
    private EditText passwordET;

    private CheckBox rememberPasCB;

    private ProgressDialog loadingDialog;

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        setTitle("Авторизация");

        initializeComponents();

        showLoadingDialog();

        setEvents();
    }

    private void showLoadingDialog(){
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Загрузка...");
        loadingDialog.setCancelable(false);
        loadingDialog.setInverseBackgroundForced(false);
        loadingDialog.show();
    }

    private void closeLoadingDialog(){
        loadingDialog.dismiss();
    }

    /**
     * Инициализация переменных и элементов макета.
     */
    private void initializeComponents() {
        closeAppBut = (Button)findViewById(R.id.closeAppButton);
        signInBut = (Button)findViewById(R.id.signInButton);
        signUpBut = (Button)findViewById(R.id.signUpButton);

        loginET = (EditText)findViewById(R.id.loginET);
        passwordET = (EditText)findViewById(R.id.passwordET);

        rememberPasCB = (CheckBox)findViewById((R.id.checkBoxBold));

        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Создание методов для событий.
     */
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
                    int index = Collections.binarySearch(unverifiedLoginList, loginET.getText().toString(), new Comparator<String>() {
                        @Override
                        public int compare(String lhs, String rhs) {
                            return lhs.compareTo(rhs);
                        }
                    });

                    if (loginET.getText().toString().equals("")) {
                        loginET.requestFocus();
                        Toast.makeText(getApplicationContext(), "Введите логин", Toast.LENGTH_LONG).show();
                    } else if (passwordET.getText().toString().equals("")) {
                        passwordET.requestFocus();
                        Toast.makeText(getApplicationContext(), "Введите пароль", Toast.LENGTH_LONG).show();
                    } else if (index >= 0) {
                        Toast.makeText(getApplicationContext(), "Ваша заявка в списке ожидания. " +
                                "Подождите, пока администратор не примет ее", Toast.LENGTH_LONG).show();
                    } else {
                        int i = 0;
                        while (!loginET.getText().toString().equals(userList.get(i).getLogin())
                                && ++i < userList.size()); //TODO binarySearch
                        if (i >= userList.size()) {
                            passwordET.setText("");
                            Toast.makeText(getApplicationContext(), "Логин и/или пароль введен неверно. " +
                                    "Повторите попытку", Toast.LENGTH_LONG).show();
                        }
                        else if (loginET.getText().toString().equals(userList.get(i).getLogin()) &&
                                passwordET.getText().toString().equals(userList.get(i).getPassword()))
                        {
                            Toast.makeText(getApplicationContext(), "Вход выполнен", Toast.LENGTH_LONG).show();

                            Intent intent;
                            String login;

                            login = loginET.getText().toString();

                            intent = new Intent(SignInActivity.this, TicketsOverviewActivity.class);

                            Bundle args = TicketsOverviewActivity.makeArgs(login, userList.get(i).getUserName(), userList.get(i).getRole());

                            intent.putExtras(args);
                            savePassAndLogin();
                            GlobalsMethods.currUserId = login;
                            GlobalsMethods.isCurrentAdmin = userList.get(i).getRole();
                            startActivity(intent);
                        }
                        else
                        {
                            passwordET.setText("");
                            Toast.makeText(getApplicationContext(), "Логин и/или пароль введен неверно. Повторите попытку", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                else Toast.makeText(getApplicationContext(), "Нет подключения к интернету", Toast.LENGTH_LONG).show();
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                unverifiedLoginList.clear();

                userList = GlobalsMethods.Downloads.getVerifiedUserList(dataSnapshot);
                unverifiedLoginList = GlobalsMethods.Downloads.getUnverifiedLogins(dataSnapshot);

                closeLoadingDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Ошибка в работе базы данных. Обратитесь к администратору компании или разработчику", Toast.LENGTH_LONG).show();
            }
        });

        signUpBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setPositiveButton("Закрыть приложение", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                savePassAndLogin();
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
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    private void savePassAndLogin(){
        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();
        if (rememberPasCB.isChecked()) {
            String login = loginET.getText().toString();
            String password = passwordET.getText().toString();
            editor.putString("Login", login);
            editor.putString("Password", password);
            editor.putBoolean("cbState", true);
        }
        else
            editor.clear();
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences settings = getPreferences(0);
        loginET.setText(settings.getString("Login",""));
        passwordET.setText(settings.getString("Password",""));
        rememberPasCB.setChecked(settings.getBoolean("cbState", false));
    }
}
