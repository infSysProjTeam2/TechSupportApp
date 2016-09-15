package com.techsupportapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.variables.DatabaseVariables;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Класс для аутентификации пользователей
 * @author Monarch
 */
public class SignInActivity extends AppCompatActivity {

    //region Constants

    /**
     *
     */
    private static final String appId = "E78031C1-13EA-4B71-A80A-53120BD37E3F";

    //endregion

    //region Fields

    private ArrayList<User> userList = new ArrayList<>();

    private DatabaseReference databaseReference;

    //endregion

    //region Composite Controls

    private Button closeAppBut;
    private Button signInBut;

    private EditText loginET;
    private EditText passwordET;

    private TextView noAccount;

    private CheckBox rememberPas;

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        initializeComponents();

        setEvents();
    }

    /**
     * Инициализация переменных и элементов макета
     */
    private void initializeComponents() {
        closeAppBut = (Button)findViewById(R.id.closeAppButton);
        signInBut = (Button)findViewById(R.id.signInButton);

        loginET = (EditText)findViewById(R.id.loginET);
        passwordET = (EditText)findViewById(R.id.passwordET);

        noAccount = (TextView)findViewById(R.id.noAccountText);

        rememberPas = (CheckBox)findViewById((R.id.checkBoxBold));

        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Создание методов для событий
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
                        if (i >= userList.size()) {
                            passwordET.setText("");
                            Toast.makeText(getApplicationContext(), "Логин и/или пароль введен неверно. Повторите попытку", Toast.LENGTH_LONG).show();
                        }
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

                            passwordET.setText("");

                            startActivityForResult(intent, 201);
                        }
                        else
                        {
                            passwordET.setText("");
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

        noAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, CreateUserActivity.class));
            }
        });
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
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    private String getId(String value) //TODO Вернуть
    {
        /*String result = "";
        for (int i = 0; i < value.length(); i++)
            result += (char)(value.charAt(i) + 1);
        return result;*/
        return value;
    }

}
