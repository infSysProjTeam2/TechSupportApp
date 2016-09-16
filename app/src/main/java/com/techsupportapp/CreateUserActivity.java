package com.techsupportapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.variables.DatabaseVariables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Класс для запроса пользователей на создание своего аккаунта
 * @author Monarch
 */
public class CreateUserActivity extends AppCompatActivity {

    //region Fields

    DatabaseReference databaseReference;
    ArrayList<String> loginList = new ArrayList<String>();
    int userCount;

    //endregion

    //region Composite Controls

    private Button returnBut;
    private Button signUpBut;

    private EditText loginET;
    private EditText passwordET;
    private EditText repeatPasswordET;

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        initializeComponents();

        setEvents();
    }

    /**
     * Инициализация переменных и элементов макета
     */
    private void initializeComponents() {
        returnBut = (Button)findViewById(R.id.returnButton);
        signUpBut = (Button)findViewById(R.id.signUpButton);

        loginET = (EditText)findViewById(R.id.loginET);
        passwordET = (EditText)findViewById(R.id.passwordET);
        repeatPasswordET = (EditText)findViewById(R.id.repeatPasswordET);

        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Создание методов для событий
     */
    private void setEvents() {
        returnBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateUserActivity.super.finish();
            }
        });

        signUpBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = Collections.binarySearch(loginList, loginET.getText().toString(), new Comparator<String>() {
                    @Override
                    public int compare(String lhs, String rhs) {
                        return lhs.compareTo(rhs);
                    }
                });
                if (loginET.getText().toString().equals(""))
                    Toast.makeText(getApplicationContext(), "Поле логина пусто", Toast.LENGTH_LONG).show();
                else if (passwordET.getText().toString().equals(""))
                    Toast.makeText(getApplicationContext(), "Поле пароля пусто", Toast.LENGTH_LONG).show();
                else if (!passwordET.getText().toString().equals(repeatPasswordET.getText().toString()))
                    Toast.makeText(getApplicationContext(), "Пароли не совпадают", Toast.LENGTH_LONG).show();
                else if (index >= 0) {
                    loginET.setText("");
                    Toast.makeText(getApplicationContext(), "Такой логин уже существует, выберите другой", Toast.LENGTH_LONG).show();
                } else {
                    databaseReference.child(DatabaseVariables.DATABASE_USER_TABLE).child("user_" + userCount++)
                            .setValue(new User(loginET.getText().toString(), passwordET.getText().toString(), false));
                    databaseReference.child(DatabaseVariables.DATABASE_USER_INDEX_COUNTER).setValue(userCount);
                    CreateUserActivity.super.finish();
                }
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                loginList.clear();
                for (DataSnapshot userRecord : dataSnapshot.child(DatabaseVariables.DATABASE_USER_TABLE).getChildren())
                    loginList.add(userRecord.getValue(User.class).login);
                userCount = dataSnapshot.child(DatabaseVariables.DATABASE_USER_INDEX_COUNTER).getValue(int.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}