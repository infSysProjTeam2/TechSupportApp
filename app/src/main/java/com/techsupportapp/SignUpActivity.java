package com.techsupportapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.databaseClasses.UnverifiedUser;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.GlobalsMethods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Класс для запроса пользователей на создание своего аккаунта
 * @author Monarch
 */
public class SignUpActivity extends AppCompatActivity {

    //region Fields

    private DatabaseReference databaseReference;
    private ArrayList<String> loginList = new ArrayList<String>();
    private int userCount;

    //endregion

    //region Composite Controls

    private Button returnBtn;
    private Button signUpBtn;

    private EditText loginET;

    private EditText userNameET;
    private EditText workPlaceET;

    private EditText passwordET;
    private EditText repeatPasswordET;

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        setTitle("Регистрация");

        initializeComponents();

        setEvents();
    }

    /**
     * Инициализация переменных и элементов макета
     */
    private void initializeComponents() {
        returnBtn = (Button)findViewById(R.id.returnButton);
        signUpBtn = (Button)findViewById(R.id.signUpButton);

        loginET = (EditText)findViewById(R.id.loginET);
        userNameET = (EditText)findViewById(R.id.userNameET);
        workPlaceET = (EditText)findViewById(R.id.workPlaceET);
        passwordET = (EditText)findViewById(R.id.passwordET);
        repeatPasswordET = (EditText)findViewById(R.id.repeatPasswordET);

        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Создание методов для событий
     */
    private void setEvents() {
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUpActivity.super.finish();
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = Collections.binarySearch(loginList, loginET.getText().toString(), new Comparator<String>() {
                    @Override
                    public int compare(String lhs, String rhs) {
                        return lhs.compareTo(rhs);
                    }
                });
                if (loginET.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Поле логина пусто", Toast.LENGTH_LONG).show();
                    loginET.requestFocus();
                }
                else if (userNameET.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Поле имени пользователя пусто", Toast.LENGTH_LONG).show();
                    userNameET.requestFocus();
                }
                else if (workPlaceET.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Поле рабочего места пользователя пусто", Toast.LENGTH_LONG).show();
                    workPlaceET.requestFocus();
                }
                else if (passwordET.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Поле пароля пусто", Toast.LENGTH_LONG).show();
                    passwordET.requestFocus();
                }
                else if (!passwordET.getText().toString().equals(repeatPasswordET.getText().toString())) {
                    passwordET.requestFocus();
                    Toast.makeText(getApplicationContext(), "Пароли не совпадают", Toast.LENGTH_LONG).show();
                    passwordET.setText("");
                    repeatPasswordET.setText("");
                }
                else if (index >= 0) {
                    loginET.setText("");
                    Toast.makeText(getApplicationContext(), "Такой логин уже существует, выберите другой", Toast.LENGTH_LONG).show();
                } else {
                     //TODO Определение прав нового пользователя.
                    databaseReference.child(DatabaseVariables.Users.DATABASE_UNVERIFIED_USER_TABLE).child("user_" + userCount)
                            .setValue(new UnverifiedUser("user_" + userCount++, loginET.getText().toString(),
                                    passwordET.getText().toString(), User.SIMPLE_USER, userNameET.getText().toString(),
                                    workPlaceET.getText().toString(), false));

                    databaseReference.child(DatabaseVariables.Indexes.DATABASE_USER_INDEX_COUNTER).setValue(userCount);
                    GlobalsMethods.showLongTimeToast(getApplicationContext(), "Ваша заявка отправлена на рассмотрение администратору");
                    SignUpActivity.super.finish();
                }
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                loginList = GlobalsMethods.Downloads.getAllLogins(dataSnapshot);
                userCount = dataSnapshot.child(DatabaseVariables.Indexes.DATABASE_USER_INDEX_COUNTER).getValue(int.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
