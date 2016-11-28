package com.techsupportapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class EditUserProfileActivity extends AppCompatActivity{
    private DatabaseReference databaseUserReference;

    private User user;

    private boolean changedRole;

    private EditText userName;
    private EditText workPlace;
    private Button saveBtn;
    private Button changePasswordBtn;
    private Button changeUserTypeBtn;

    private String newPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = (User) getIntent().getExtras().getSerializable("user");
        setContentView(R.layout.activity_edit_user_profile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initializeComponents();
        setEvents();
    }

    private void initializeComponents(){
        databaseUserReference = FirebaseDatabase.getInstance().getReference(DatabaseVariables.FullPath.Users.DATABASE_ALL_USER_TABLE);

        userName = (EditText)findViewById(R.id.userName);
        workPlace = (EditText)findViewById(R.id.workPlace);

        changePasswordBtn = (Button)findViewById(R.id.changePasswordBtn);
        changeUserTypeBtn = (Button)findViewById(R.id.changeUserTypeBtn);

        saveBtn = (Button)findViewById(R.id.saveBtn);

        if (Globals.currentUser.getRole() == User.SIMPLE_USER) {
            changeUserTypeBtn.setVisibility(View.GONE);
        }

        userName.setText(user.getUserName());
        workPlace.setText(user.getWorkPlace());

        setTitle("Профиль");

        if (!user.getLogin().equals(Globals.currentUser.getLogin())) {
            userName.setEnabled(false);
            workPlace.setEnabled(false);
            changePasswordBtn.setVisibility(View.GONE);
        }
        changedRole = false;
    }

    private void setEvents(){
        changeUserTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(EditUserProfileActivity.this)
                        .title("Изменение прав")
                        .items(R.array.roles_array)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                //TODO сделать это
                                if (text.equals("Пользователь")) {
                                    if (user.getRole() != User.SIMPLE_USER) {
                                        User chUser;
                                        try {
                                            chUser = new User(user.getBranchId(), false, user.getLogin(), user.getPassword(), User.SIMPLE_USER, user.getLogin(), user.getWorkPlace());
                                            databaseUserReference.child(DatabaseVariables.ExceptFolder.Users.DATABASE_VERIFIED_SIMPLE_USER_TABLE).child(chUser.getBranchId()).setValue(chUser);
                                            databaseUserReference.child(DatabaseVariables.ExceptFolder.Users.DATABASE_VERIFIED_MANAGER_TABLE).child(chUser.getBranchId()).removeValue();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        changedRole = true;
                                        Toast.makeText(getApplicationContext(), "Переведен в статус пользователя", Toast.LENGTH_LONG).show();
                                    } else
                                        Toast.makeText(getApplicationContext(), "Уже является пользователем", Toast.LENGTH_LONG).show();
                                } else if (user.getRole() != User.MANAGER) {
                                    User chUser;
                                    try {
                                        chUser = new User(user.getBranchId(), false, user.getLogin(), user.getPassword(),
                                                User.MANAGER, user.getLogin(), user.getWorkPlace());
                                        databaseUserReference.child(DatabaseVariables.ExceptFolder.Users.DATABASE_VERIFIED_MANAGER_TABLE).child(chUser.getBranchId()).setValue(chUser);
                                        databaseUserReference.child(DatabaseVariables.ExceptFolder.Users.DATABASE_VERIFIED_SIMPLE_USER_TABLE).child(chUser.getBranchId()).removeValue();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    changedRole = true;
                                    Toast.makeText(getApplicationContext(), "Переведен в статус диспетчера", Toast.LENGTH_LONG).show();
                                } else
                                    Toast.makeText(getApplicationContext(), "Уже является диспетчером", Toast.LENGTH_LONG).show();

                                if (changedRole && user.getLogin().equals(Globals.currentUser.getLogin())) {
                                    Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
                                }
                            }
                        })
                        .show();
            }
        });

        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDialog dialog;
                final MaterialDialog.SingleButtonCallback callback;
                final EditText currentPasswordET;
                final EditText newPasswordET;
                final EditText newPasswordRepeatET;
                final TextView hintTV;
                final View positiveAction;

                callback = new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        try {
                            User chUser = new User(user.getBranchId(), false, user.getLogin(), newPassword, user.getRole(), user.getLogin(), user.getWorkPlace());
                            if (chUser.getRole() == User.MANAGER)
                                databaseUserReference.child(DatabaseVariables.ExceptFolder.Users.DATABASE_VERIFIED_MANAGER_TABLE).child(chUser.getBranchId()).setValue(chUser);
                            else if (chUser.getRole() == User.SIMPLE_USER)
                                databaseUserReference.child(DatabaseVariables.ExceptFolder.Users.DATABASE_VERIFIED_SIMPLE_USER_TABLE).child(chUser.getBranchId()).setValue(chUser);
                            else if (chUser.getRole() == User.DEPARTMENT_CHIEF)
                                databaseUserReference.child(DatabaseVariables.ExceptFolder.Users.DATABASE_VERIFIED_CHIEF_TABLE).child(chUser.getBranchId()).setValue(chUser);
                            else if (chUser.getRole() == User.DEPARTMENT_MEMBER)
                                databaseUserReference.child(DatabaseVariables.ExceptFolder.Users.DATABASE_VERIFIED_SPECIALIST_TABLE).child(chUser.getBranchId()).setValue(chUser);
                            else if (chUser.getRole() == User.MANAGER)
                                databaseUserReference.child(DatabaseVariables.ExceptFolder.Users.DATABASE_VERIFIED_MANAGER_TABLE).child(chUser.getBranchId()).setValue(chUser);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                dialog = new MaterialDialog.Builder(EditUserProfileActivity.this)
                        .title("Смена пароля")
                        .customView(R.layout.prompts_change_password, true)
                        .positiveText("Принять")
                        .negativeText("Отмена")
                        .onPositive(callback)
                        .cancelable(false)
                        .build();

                currentPasswordET = (EditText) dialog.findViewById(R.id.currentPasswordEt);
                newPasswordET = (EditText) dialog.findViewById(R.id.newPasswordEt);
                newPasswordRepeatET = (EditText) dialog.findViewById(R.id.newPasswordRepeatEt);
                hintTV = (TextView) dialog.findViewById(R.id.hint);
                positiveAction = dialog.getActionButton(DialogAction.POSITIVE);

                TextWatcher textWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (currentPasswordET.getText().toString().equals("")) {
                            hintTV.setText("Поле текущего пароля пусто");
                            positiveAction.setEnabled(false);
                        } else if (newPasswordET.getText().toString().equals("")) {
                            hintTV.setText("Поле нового пароля пусто");
                            positiveAction.setEnabled(false);
                        } else if (newPasswordRepeatET.getText().toString().equals("")) {
                            hintTV.setText("Нужно повторить пароль");
                            positiveAction.setEnabled(false);
                        } else if (!currentPasswordET.getText().toString().equals(user.getPassword())) {
                            hintTV.setText("Введен неправильный пароль");
                            positiveAction.setEnabled(false);
                        } else if (!newPasswordET.getText().toString().equals(newPasswordRepeatET.getText().toString())) {
                            hintTV.setText("Пароли должны совпадать");
                            positiveAction.setEnabled(false);
                        } else if (newPasswordET.getText().toString().length() < 5 || newPasswordRepeatET.getText().toString().length() < 5) {
                            hintTV.setText("Пароль должен быть содержать не менее 5 символов");
                            positiveAction.setEnabled(false);
                        } else if (!Globals.isEnglishWord(newPasswordET.getText().toString()) || !Globals.isEnglishWord(newPasswordRepeatET.getText().toString())) {
                            hintTV.setText("Пароли должны содержать только английские символы и цифры");
                            positiveAction.setEnabled(false);
                        } else {
                            newPassword = newPasswordET.getText().toString();
                            hintTV.setText("");
                            positiveAction.setEnabled(true);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                };

                currentPasswordET.addTextChangedListener(textWatcher);
                newPasswordET.addTextChangedListener(textWatcher);
                newPasswordRepeatET.addTextChangedListener(textWatcher);

                positiveAction.setEnabled(false);

                dialog.show();
            }
        });


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
