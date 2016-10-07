package com.techsupportapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.GlobalsMethods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class UserProfileActivity extends AppCompatActivity{

    private DatabaseReference databaseRef;

    private ArrayList<User> usersList = new ArrayList<User>();

    private String mUserId;
    private String mUserLogin;
    private String mCurrUserId;

    private TextView userName;
    private TextView userId;
    private TextView regDate;
    private TextView workPlace;
    private TextView accessLevel;

    private ImageView userImage;

    private Button editProfileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mUserId = getIntent().getExtras().getString("userId");
        mUserLogin = getIntent().getExtras().getString("userLogin");
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

        editProfileBtn = (Button)findViewById(R.id.changeDataBtn);

        LinearLayout ln = (LinearLayout)findViewById(R.id.changeDataLayout);

        if (!mCurrUserId.equals(mUserId) && GlobalsMethods.isCurrentAdmin == User.SIMPLE_USER)
            ln.setVisibility(View.GONE);
    }

    private void setEvents(){
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usersList = GlobalsMethods.Downloads.getVerifiedUserList(dataSnapshot);
                setData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(UserProfileActivity.this, EditUserProfileActivity.class);
                intent.putExtra("userId", mUserId);
                intent.putExtra("currUserId", mCurrUserId);
                startActivity(intent);
            }
        });
    }

    private void setData(){
        ArrayList<String> idList = new ArrayList<String>();
        Collections.sort(usersList, new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                return lhs.getLogin().compareTo(rhs.getLogin());
            }
        });
        for (int i = 0; i < usersList.size(); i++)
            idList.add(usersList.get(i).getLogin());
        int index = Collections.binarySearch(idList, mUserId, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });

        userName.setText(usersList.get(index).getUserName());
        userId.setText(usersList.get(index).getLogin());
        regDate.setText("Дата регистрации: " + usersList.get(index).getRegistrationDate());
        workPlace.setText("Рабочее место: " + usersList.get(index).getWorkPlace());
        if (usersList.get(index).getRole() == User.ADMINISTRATOR)
            accessLevel.setText("Права: администратор");
        else if (usersList.get(index).getRole() == User.SIMPLE_USER)
            accessLevel.setText("Права: пользователь");
        userImage.setImageBitmap(GlobalsMethods.ImageMethods.getclip(GlobalsMethods.ImageMethods.createUserImage(userName.getText().toString(), UserProfileActivity.this)));
        //setTitle("Профиль " + userName.getText().toString());
    }
}