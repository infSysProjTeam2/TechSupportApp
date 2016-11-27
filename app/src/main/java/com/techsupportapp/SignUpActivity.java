package com.techsupportapp;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.weiwangcn.betterspinner.library.BetterSpinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Класс для запроса пользователей на создание своего аккаунта
 * @author Monarch
 */
public class SignUpActivity extends AppCompatActivity {

    //region Fields

    private DatabaseReference databaseUserReference;
    private DatabaseReference databaseIndexReference;
    private ArrayList<String> loginList = new ArrayList<>();
    private int userCount;

    //endregion

    //region Composite Controls

    private EditText loginET;

    private EditText userNameET;
    private EditText workPlaceET;

    private EditText passwordET;
    private EditText repeatPasswordET;

    private BetterSpinner spinner;

    private Button submitBtn;

    //endregion

    //region

    ValueEventListener databaseUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            loginList = Globals.Downloads.Strings.getAllLogins(dataSnapshot);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    ValueEventListener databaseIndexListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            userCount = dataSnapshot.getValue(int.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

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
        if (spinner.getText().toString().equals("Пользователь"))
            return User.SIMPLE_USER;
        else if (spinner.getText().toString().equals("Работник отдела"))
            return User.DEPARTMENT_MEMBER;
        else if (spinner.getText().toString().equals("Начальник отдела"))
            return User.DEPARTMENT_CHIEF;
        else if (spinner.getText().toString().equals("Диспетчер"))
            return  User.MANAGER;
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
        submitBtn = (Button) findViewById(R.id.submitBtn);

        databaseUserReference = FirebaseDatabase.getInstance().getReference(DatabaseVariables.FullPath.Users.DATABASE_ALL_USER_TABLE);
        databaseIndexReference = FirebaseDatabase.getInstance().getReference(DatabaseVariables.FullPath.Indexes.DATABASE_USER_INDEX_COUNTER);

        String[] roles_array = new String[] {"Пользователь", "Работник отдела", "Начальник отдела", "Диспетчер"};

        spinner = (BetterSpinner) findViewById(R.id.spinner);
        spinner.setAdapter(new ArrayAdapter<>(SignUpActivity.this, android.R.layout.simple_dropdown_item_1line, roles_array));
        spinner.setText(roles_array[0]);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Создание методов для событий
     */
    private void setEvents() {
        repeatPasswordET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    tryAddUser();
                return true;
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryAddUser();
            }
        });

        loginET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                TextInputLayout loginLayout = (TextInputLayout) findViewById(R.id.login_layout);
                loginLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        userNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                TextInputLayout userNameLayout = (TextInputLayout) findViewById(R.id.userName_layout);
                userNameLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        workPlaceET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                TextInputLayout workPlaceLayout = (TextInputLayout) findViewById(R.id.workPlace_layout);
                workPlaceLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        passwordET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                TextInputLayout passwordLayout = (TextInputLayout) findViewById(R.id.password_layout);
                passwordLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        repeatPasswordET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                TextInputLayout repeatPasswordLayout = (TextInputLayout) findViewById(R.id.repeat_password_layout);
                repeatPasswordLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
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
        if (loginET.getText().toString().isEmpty()) {
            loginET.requestFocus();
            TextInputLayout loginLayout = (TextInputLayout) findViewById(R.id.login_layout);
            loginLayout.setErrorEnabled(true);
            loginLayout.setError(getResources().getString(R.string.empty_field));
            Globals.showKeyboardOnEditText(SignUpActivity.this, loginET);
        }
        else if (userNameET.getText().toString().isEmpty()) {
            userNameET.requestFocus();
            TextInputLayout userNameLayout = (TextInputLayout) findViewById(R.id.userName_layout);
            userNameLayout.setErrorEnabled(true);
            userNameLayout.setError(getResources().getString(R.string.empty_field));
            Globals.showKeyboardOnEditText(SignUpActivity.this, userNameET);
        }
        else if (workPlaceET.getText().toString().isEmpty()) {
            workPlaceET.requestFocus();
            TextInputLayout workPlaceLayout = (TextInputLayout) findViewById(R.id.workPlace_layout);
            workPlaceLayout.setErrorEnabled(true);
            workPlaceLayout.setError(getResources().getString(R.string.empty_field));
            Globals.showKeyboardOnEditText(SignUpActivity.this, workPlaceET);
        }
        else if (passwordET.getText().toString().isEmpty()) {
            passwordET.requestFocus();
            TextInputLayout passwordLayout = (TextInputLayout) findViewById(R.id.password_layout);
            passwordLayout.setErrorEnabled(true);
            passwordLayout.setError(getResources().getString(R.string.empty_field));
            Globals.showKeyboardOnEditText(SignUpActivity.this, passwordET);
        }
        else if (!passwordET.getText().toString().equals(repeatPasswordET.getText().toString())) {
            repeatPasswordET.requestFocus();
            TextInputLayout passwordLayout = (TextInputLayout) findViewById(R.id.repeat_password_layout);
            passwordLayout.setErrorEnabled(true);
            passwordLayout.setError("Пароли должны совпадать");
            Globals.showKeyboardOnEditText(SignUpActivity.this, repeatPasswordET);
        }
        else if (index >= 0) {
            loginET.requestFocus();
            TextInputLayout loginLayout = (TextInputLayout) findViewById(R.id.login_layout);
            loginLayout.setErrorEnabled(true);
            loginLayout.setError("Этот логин уже занят");
            Globals.showKeyboardOnEditText(SignUpActivity.this, loginET);
        } else return true;
        return false;
        //TODO Сделать проверку парольей на длину и англ символы (уже есть в Globals)
    }

    /**
     * Метод, добавляющий пользователя при успешном прохождении соответствующих проверок.
     */
    private void tryAddUser(){
        if (isFieldsContentCorrect()) {
            try {
                databaseUserReference.child(DatabaseVariables.ExceptFolder.Users.DATABASE_UNVERIFIED_USER_TABLE).child("user_" + userCount)
                        .setValue(new User("user_" + userCount++, false, loginET.getText().toString(),
                                passwordET.getText().toString(), checkRole(), userNameET.getText().toString(),
                                workPlaceET.getText().toString()));
            } catch (Exception e) {
                Globals.showLongTimeToast(getApplicationContext(), "Ошибка при присвоении прав пользователю, обратитесь к разработчику");
            }

            databaseIndexReference.setValue(userCount);
            Globals.showLongTimeToast(getApplicationContext(), "Ваша заявка отправлена на рассмотрение дипетчеру");
            databaseUserReference.removeEventListener(databaseUserListener);
            databaseIndexReference.removeEventListener(databaseIndexListener);
            SignUpActivity.super.finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseUserReference.addValueEventListener(databaseUserListener);
        databaseIndexReference.addValueEventListener(databaseIndexListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseUserReference.removeEventListener(databaseUserListener);
        databaseIndexReference.removeEventListener(databaseIndexListener);
    }
}
