package com.techsupportapp;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class AdminActivity extends AppCompatActivity {

    private Button listOfTicketsBtn;
    private Button listOfChannelsBtn;

    private String mAppId;
    private String mUserId;
    private String mNickname;
    private String mGcmRegToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mAppId = getIntent().getExtras().getString("appKey");
        mUserId = getIntent().getExtras().getString("uuid");
        mNickname = getIntent().getExtras().getString("nickname");
        mGcmRegToken = PreferenceManager.getDefaultSharedPreferences(AdminActivity.this).getString("SendBirdGCMToken", "");

        initializeComponents();

        setEvents();
    }

    private void initializeComponents() {
        listOfTicketsBtn = (Button) findViewById(R.id.listOfTicketsBtn);
        listOfChannelsBtn = (Button) findViewById(R.id.listOfChannelsBtn);
    }

    private void setEvents() {
        listOfTicketsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, ListOfTicketsActivity.class);
                startActivityForResult(intent, 203);
            }
        });

        listOfChannelsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, ListOfChannelsActivity.class);
                Bundle args = AdminActivity.makeSendBirdArgs(mAppId, mUserId, mNickname);
                intent.putExtras(args);
                startActivityForResult(intent, 204);
            }
        });
    }

    public static Bundle makeSendBirdArgs(String appKey, String uuid, String nickname) {
        Bundle args = new Bundle();
        args.putString("appKey", appKey);
        args.putString("uuid", uuid);
        args.putString("nickname", nickname);
        return args;
    }
}