package com.techsupportapp;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class UserActivity extends AppCompatActivity {
    private Button createTicketBtn;
    private Button listOfChannelsBtn;

    private String mAppId;
    private String mUserId;
    private String mNickname;
    private String mGcmRegToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mAppId = getIntent().getExtras().getString("appKey");
        mUserId = getIntent().getExtras().getString("uuid");
        mNickname = getIntent().getExtras().getString("nickname");
        mGcmRegToken = PreferenceManager.getDefaultSharedPreferences(UserActivity.this).getString("SendBirdGCMToken", "");
        initializeComponents();

        setEvents();
    }

    public static Bundle makeSendBirdArgs(String appKey, String uuid, String nickname) {
        Bundle args = new Bundle();
        args.putString("appKey", appKey);
        args.putString("uuid", uuid);
        args.putString("nickname", nickname);
        return args;
    }

    private void initializeComponents() {
        createTicketBtn = (Button)findViewById(R.id.createTicket);
        listOfChannelsBtn = (Button)findViewById(R.id.listOfChannels);
    }

    private void setEvents()
    {
        createTicketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, CreateTicketActivity.class);
                Bundle args = UserActivity.makeSendBirdArgs(mAppId, mUserId, mNickname);
                intent.putExtras(args);

                startActivityForResult(intent, 204);
            }
        });

        listOfChannelsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, ListOfChannelsActivity.class);
                Bundle args = UserActivity.makeSendBirdArgs(mAppId, mUserId, mNickname);
                intent.putExtras(args);

                startActivityForResult(intent, 205);
            }
        });
    }
}
