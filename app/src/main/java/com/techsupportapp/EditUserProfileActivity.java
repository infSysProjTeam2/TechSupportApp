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

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    private ArrayList<User> usersList = new ArrayList<User>();

    private String mUserId;

    private int userPosition;

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

        mUserId = getIntent().getExtras().getString("userId");
        setContentView(R.layout.activity_edit_user_profile);

        initializeComponents();
        setEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseReference.addValueEventListener(valueEventListener);
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

    private void initializeComponents(){
        databaseReference = FirebaseDatabase.getInstance().getReference();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        userName = (EditText)findViewById(R.id.userName);
        workPlace = (EditText)findViewById(R.id.workPlace);

        changePasswordBtn = (Button)findViewById(R.id.changePasswordBtn);
        changeUserTypeBtn = (Button)findViewById(R.id.changeUserTypeBtn);

        saveBtn = (Button)findViewById(R.id.saveBtn);

        if (!mUserId.equals(Globals.currentUser.getLogin())) {
            userName.setEnabled(false);
            workPlace.setEnabled(false);
            changePasswordBtn.setEnabled(false);
        }
        changedRole = false;
    }

    private void setEvents(){
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usersList = Globals.Downloads.getVerifiedUserList(dataSnapshot);
                setData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

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
                                    if (usersList.get(userPosition).getRole() != User.SIMPLE_USER) {
                                        User chUser;
                                        try {
                                            chUser = new User(usersList.get(userPosition).getBranchId(), false, usersList.get(userPosition).getLogin(), usersList.get(userPosition).getPassword(),
                                                    User.SIMPLE_USER, usersList.get(userPosition).getLogin(), usersList.get(userPosition).getWorkPlace());
                                            databaseReference.child(DatabaseVariables.Users.DATABASE_VERIFIED_SIMPLE_USER_TABLE).child(chUser.getBranchId()).setValue(chUser);
                                            databaseReference.child(DatabaseVariables.Users.DATABASE_VERIFIED_ADMIN_TABLE).child(chUser.getBranchId()).removeValue();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        changedRole = true;
                                        Toast.makeText(getApplicationContext(), "Переведен в статус пользователя", Toast.LENGTH_LONG).show();
                                    } else
                                        Toast.makeText(getApplicationContext(), "Уже является пользователем", Toast.LENGTH_LONG).show();
                                } else if (usersList.get(userPosition).getRole() != User.ADMINISTRATOR) {
                                    User chUser;
                                    try {
                                        chUser = new User(usersList.get(userPosition).getBranchId(), false, usersList.get(userPosition).getLogin(), usersList.get(userPosition).getPassword(),
                                                User.ADMINISTRATOR, usersList.get(userPosition).getLogin(), usersList.get(userPosition).getWorkPlace());
                                        databaseReference.child(DatabaseVariables.Users.DATABASE_VERIFIED_ADMIN_TABLE).child(chUser.getBranchId()).setValue(chUser);
                                        databaseReference.child(DatabaseVariables.Users.DATABASE_VERIFIED_SIMPLE_USER_TABLE).child(chUser.getBranchId()).removeValue();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    changedRole = true;
                                    Toast.makeText(getApplicationContext(), "Переведен в статус администратора", Toast.LENGTH_LONG).show();
                                } else
                                    Toast.makeText(getApplicationContext(), "Уже является администратором", Toast.LENGTH_LONG).show();

                                if (changedRole && mUserId.equals(Globals.currentUser.getLogin())) {
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
                            User chUser = new User(usersList.get(userPosition).getBranchId(), false, usersList.get(userPosition).getLogin(), newPassword,
                                    usersList.get(userPosition).getRole(), usersList.get(userPosition).getLogin(), usersList.get(userPosition).getWorkPlace());
                            if (chUser.getRole() == User.ADMINISTRATOR)
                                databaseReference.child(DatabaseVariables.Users.DATABASE_VERIFIED_ADMIN_TABLE).child(chUser.getBranchId()).setValue(chUser);
                            else if (chUser.getRole() == User.SIMPLE_USER)
                                databaseReference.child(DatabaseVariables.Users.DATABASE_VERIFIED_SIMPLE_USER_TABLE).child(chUser.getBranchId()).setValue(chUser);
                            else if (chUser.getRole() == User.DEPARTMENT_CHIEF)
                                databaseReference.child(DatabaseVariables.Users.DATABASE_VERIFIED_CHIEF_TABLE).child(chUser.getBranchId()).setValue(chUser);
                            else if (chUser.getRole() == User.DEPARTMENT_MEMBER)
                                databaseReference.child(DatabaseVariables.Users.DATABASE_VERIFIED_WORKER_TABLE).child(chUser.getBranchId()).setValue(chUser);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                dialog = new MaterialDialog.Builder(EditUserProfileActivity.this)
                        .title("Смена пароля")
                        .customView(R.layout.prompts_change_password, true)
                        .positiveText(android.R.string.ok)
                        .negativeText(android.R.string.cancel)
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
                        } else if (newPasswordET.getText().toString().equals(""))
                            hintTV.setText("Поле нового пароля пусто");
                        else if (newPasswordRepeatET.getText().toString().equals(""))
                            hintTV.setText("Нужно повторить пароль");
                        else if (!currentPasswordET.getText().toString().equals(usersList.get(userPosition).getPassword()))
                            hintTV.setText("Введен неправильный пароль");
                        else if (!newPasswordET.getText().toString().equals(newPasswordRepeatET.getText().toString()))
                            hintTV.setText("Пароли должны совпадать");
                        else if (newPasswordET.getText().toString().length() < 5 || newPasswordRepeatET.getText().toString().length() < 5)
                            hintTV.setText("Пароль должен быть содержать не менее 5 символов");
                        else if (!Globals.isEnglishWord(newPasswordET.getText().toString()) || !Globals.isEnglishWord(newPasswordRepeatET.getText().toString()))
                            hintTV.setText("Пароли должны содержать только английские символы и цифры");
                        else {
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

    private void setData(){
        ArrayList<String> idList = new ArrayList<>();
        Collections.sort(usersList, new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                return lhs.getLogin().compareTo(rhs.getLogin());
            }
        });
        for (int i = 0; i < usersList.size(); i++)
            idList.add(usersList.get(i).getLogin());
        userPosition = Collections.binarySearch(idList, mUserId, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });

        if (Globals.currentUser.getRole() == User.SIMPLE_USER)
            changeUserTypeBtn.setEnabled(false);

        userName.setText(usersList.get(userPosition).getUserName());
        workPlace.setText(usersList.get(userPosition).getWorkPlace());

        setTitle("Профиль " + usersList.get(userPosition).getUserName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
