package com.techsupportapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.GlobalsMethods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class UserProfileActivity extends AppCompatActivity{

    private DatabaseReference databaseRef;

    private ArrayList<User> usersList = new ArrayList<User>();

    private String mUserId;
    private String mCurrUserId;

    private TextView userName;
    private TextView userId;
    private TextView regDate;
    private TextView workPlace;
    private TextView accessLevel;

    private ImageView userImage;

    private Button changePasswordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mUserId = getIntent().getExtras().getString("userId");
        mCurrUserId = getIntent().getExtras().getString("currUserId");

        initializeComponents();
        setEvents();
    }

    private void initializeComponents(){
        databaseRef = FirebaseDatabase.getInstance().getReference();

        userName = (TextView)findViewById(R.id.userName);
        userId = (TextView)findViewById(R.id.userId);
        regDate = (TextView)findViewById(R.id.regDate);
        workPlace = (TextView)findViewById(R.id.workPlace);
        accessLevel = (TextView)findViewById(R.id.accessLevel);

        userImage = (ImageView)findViewById(R.id.userImage);

        changePasswordBtn = (Button)findViewById(R.id.changePasswordBtn);

        if (!mCurrUserId.equals(mUserId))
            changePasswordBtn.setVisibility(View.INVISIBLE);
    }

    private void setEvents(){
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot userRecord : dataSnapshot.child(DatabaseVariables.DATABASE_VERIFIED_USER_TABLE).getChildren()) {
                    User user = userRecord.getValue(User.class);
                    usersList.add(user);
                }
                setData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO todo
            }
        });
    }

    private void setData(){
        ArrayList<String> idList = new ArrayList<>();
        Collections.sort(usersList, new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                return lhs.login.compareTo(rhs.login);
            }
        });
        for (int i = 0; i < usersList.size(); i++)
            idList.add(usersList.get(i).login);
        int index = Collections.binarySearch(idList, mUserId, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });

        userName.setText(usersList.get(index).login);
        userId.setText(usersList.get(index).userId);
        //regDate.setText(""); TODO сделать
        //workPlace.setText(""); TODO сделать
        if (usersList.get(index).isAdmin)
            accessLevel.setText("Администратор");
        else
            accessLevel.setText("Пользователь");
        userImage.setImageBitmap(GlobalsMethods.ImageMethods.getclip(GlobalsMethods.ImageMethods.createUserImage(userName.getText().toString(), UserProfileActivity.this)));
        setTitle("Профиль " + userName.getText().toString());
    }
}


