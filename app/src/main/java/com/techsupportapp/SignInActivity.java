package com.techsupportapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.services.MessagingService;
import com.techsupportapp.utility.Globals;

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
    private ValueEventListener valueEventListener;

    //endregion

    //region Composite Controls

    private Button closeAppBut;
    private Button signInBut;

    private EditText loginET;
    private EditText passwordET;

    private CheckBox rememberPasCB;

    private MaterialDialog loadingDialog;

    //endregion

    //region Override Methods

    @Override
    public void onBackPressed() {
        new MaterialDialog.Builder(this)
            .title("Закрыть приложение")
            .content("Вы действительно хотите закрыть приложение?")
            .positiveText(android.R.string.yes)
            .negativeText(android.R.string.no)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    savePassAndLogin();
                    SignInActivity.this.finishAffinity();
                }
            })
            .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.cancel();
                }
            })
            .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        dataConstruction();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sign_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sign_up) {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SignInActivity.this);

        loginET.setText(preferences.getString("Login",""));
        passwordET.setText(preferences.getString("Password",""));
        rememberPasCB.setChecked(preferences.getBoolean("cbState", false));
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(valueEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseReference.removeEventListener(valueEventListener);
    }
    //endregion

    /**
     * Проверка полей логина и пароля, проверка подтверждения заявки на получение аккаунта.
     * @return true - если поля заполнены и аккаунт не находится на рассмотрении на добавление.
     * false - если хотя бы одно поле не заплонено или аккаунт пользователя находится на
     * рассмотрении на добавление в систему.
     */
    private boolean checkFields() {
        int index = Collections.binarySearch(unverifiedLoginList, loginET.getText().toString(), new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });
        if (loginET.getText().toString().equals("")) {
            loginET.requestFocus();
            Toast.makeText(getApplicationContext(), "Введите логин", Toast.LENGTH_LONG).show();
            return false;
        } else if (passwordET.getText().toString().equals("")) {
            passwordET.requestFocus();
            Toast.makeText(getApplicationContext(), "Введите пароль", Toast.LENGTH_LONG).show();
            return false;
        } else if (index >= 0) {
            Toast.makeText(getApplicationContext(), "Ваша заявка в списке ожидания. " +
                    "Подождите, пока администратор не примет ее", Toast.LENGTH_LONG).show();
            return false;
        } else return true;
    }

    /**
     * Проверка правильности логина и соответствия пароля. При успешном сопоставлении выполняется
     * вход в систему.
     */
    private void checkVerificationData() {
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
            signIn(userList.get(i));
        else {
            passwordET.setText("");
            Toast.makeText(getApplicationContext(), "Логин и/или пароль введен неверно. Повторите попытку", Toast.LENGTH_LONG).show();
        }

        closeLoadingDialog();
    }

    /**
     * Закрытие окна загрузки.
     */
    private void closeLoadingDialog(){
        loadingDialog.dismiss();
    }

    /**
     * Назначение начальних данных и параметров программы.
     */
    private void dataConstruction(){
        initializeComponents();
        setEvents();
    }

    /**
     * Проверка наличия подключения к Интернету.
     * @return true - если подключение есть. false - если подключение отсутствует.
     */
    private boolean hasConnection() {
        ConnectivityManager cm = (ConnectivityManager)SignInActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Инициализация переменных и элементов макета.
     */
    private void initializeComponents() {
        closeAppBut = (Button)findViewById(R.id.closeAppButton);
        signInBut = (Button)findViewById(R.id.signInButton);

        loginET = (EditText)findViewById(R.id.loginET);
        passwordET = (EditText)findViewById(R.id.passwordET);

        rememberPasCB = (CheckBox)findViewById((R.id.checkBoxBold));

        databaseReference = FirebaseDatabase.getInstance().getReference();

        setTitle("Авторизация");
    }

    /**
     * Сохранение данных логина и пароля для повторного входа.
     */
    private void savePassAndLogin(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SignInActivity.this);
        SharedPreferences.Editor editor = preferences.edit();
        if (rememberPasCB.isChecked()) {
            String login = loginET.getText().toString();
            String password = passwordET.getText().toString();
            editor.putString("Login", login);
            editor.putString("Password", password);
            editor.putBoolean("cbState", true);
        }
        else
            editor.clear();
        editor.apply();
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
                    showLoadingDialog();
                    databaseReference.addValueEventListener(valueEventListener);
                }
                else Toast.makeText(getApplicationContext(), "Нет подключения к интернету", Toast.LENGTH_LONG).show();
            }
        });

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                unverifiedLoginList.clear();

                userList = Globals.Downloads.getVerifiedUserList(dataSnapshot);
                unverifiedLoginList = Globals.Downloads.getUnverifiedLogins(dataSnapshot);

                if (!checkFields())
                    return;
                checkVerificationData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Ошибка в работе базы данных. Обратитесь к администратору компании или разработчику", Toast.LENGTH_LONG).show();
            }
        };

        passwordET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (hasConnection()) {
                    if (!checkFields())
                        return false;
                    checkVerificationData();
                    return true;
                }
                else {
                    Toast.makeText(getApplicationContext(), "Нет подключения к интернету", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });
    }

    /**
     * Отображение окна загрузки.
     */
    private void showLoadingDialog(){
        loadingDialog = new MaterialDialog.Builder(this)
                .content("Загрузка...")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .show();
    }

    /**
     * Вход в систему под определенным пользователем.
     * @param user Данные пользователя для входа в систему.
     */
    private void signIn(User user){
        Toast.makeText(getApplicationContext(), "Вход выполнен", Toast.LENGTH_SHORT).show();

        savePassAndLogin();

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("allowNotifications", false))
            startService(new Intent(this, MessagingService.class));

        Globals.currentUser = user;
        startActivity(new Intent(SignInActivity.this, TicketsActivity.class));
    }
}
