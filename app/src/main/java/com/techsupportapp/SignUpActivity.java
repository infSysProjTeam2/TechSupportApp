package com.techsupportapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;

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

    private EditText loginET;

    private EditText userNameET;
    private EditText workPlaceET;

    private EditText passwordET;
    private EditText repeatPasswordET;

    private RadioButton userRadBtn;
    private RadioButton workerRadBtn;
    private RadioButton adminRadBtn;
    private RadioButton chiefRadBtn;

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
     * Метод, проверяющий значение RadioButton для получения будущей роли пользователя.
     * @return Роль пользователя.
     */
    private int checkRole() {
        if (userRadBtn.isChecked())
            return User.SIMPLE_USER;
        else if (workerRadBtn.isChecked())
            return User.DEPARTMENT_MEMBER;
        else if (adminRadBtn.isChecked())
            return User.ADMINISTRATOR;
        else if (chiefRadBtn.isChecked())
            return User.DEPARTMENT_CHIEF;
        else return User.SIMPLE_USER;
    }

    /**
     * Инициализация переменных и элементов макета
     */
    private void initializeComponents() {

        loginET = (EditText)findViewById(R.id.loginET);
        userNameET = (EditText)findViewById(R.id.userNameET);
        workPlaceET = (EditText)findViewById(R.id.workPlaceET);
        passwordET = (EditText)findViewById(R.id.passwordET);
        repeatPasswordET = (EditText)findViewById(R.id.repeatPasswordET);

        userRadBtn = (RadioButton)findViewById(R.id.userRadBtn);
        workerRadBtn = (RadioButton)findViewById(R.id.workerRadBtn);
        adminRadBtn = (RadioButton)findViewById(R.id.adminRadBtn);
        chiefRadBtn = (RadioButton)findViewById(R.id.chiefRadBtn);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Создание методов для событий
     */
    private void setEvents() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                loginList = Globals.Downloads.Strings.getAllLogins(dataSnapshot);
                userCount = dataSnapshot.child(DatabaseVariables.Indexes.DATABASE_USER_INDEX_COUNTER).getValue(int.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        repeatPasswordET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    tryAddUser();
                return true;
            }
        });

    }

   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sign_up) {
            tryAddUser();
            return true;
        }
        else if (id == android.R.id.home)
            this.finish();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Проверка корректности заполнения полей заявки.
     * @return true - если поля заполнены корректно. false - если при заполнении появились ошибки.
     */
    private boolean isFieldsContentCorrect(){
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
        } else return true;
        return false;
    }

    /**
     * Метод, добавляющий пользователя при успешном прохождении соответствующих проверок.
     */
    private void tryAddUser(){
        if (isFieldsContentCorrect()) {
            try {
                databaseReference.child(DatabaseVariables.Users.DATABASE_UNVERIFIED_USER_TABLE).child("user_" + userCount)
                        .setValue(new User("user_" + userCount++, false, loginET.getText().toString(),
                                passwordET.getText().toString(), checkRole(), userNameET.getText().toString(),
                                workPlaceET.getText().toString()));
            } catch (Exception e) {
                Globals.showLongTimeToast(getApplicationContext(), "Ошибка при присвоении прав пользователю, обратитесь к разработчику");
            }

            databaseReference.child(DatabaseVariables.Indexes.DATABASE_USER_INDEX_COUNTER).setValue(userCount);
            Globals.showLongTimeToast(getApplicationContext(), "Ваша заявка отправлена на рассмотрение администратору");
            SignUpActivity.super.finish();
        }
    }
}
