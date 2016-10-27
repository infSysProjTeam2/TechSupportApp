package com.techsupportapp;

import android.app.ProgressDialog;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.techsupportapp.databaseClasses.ChatMessage;
import com.techsupportapp.adapters.ChatListAdapter;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MessagingActivity extends AppCompatActivity {

    private Firebase mFirebaseRef;
    private ValueEventListener mConnectedListener;
    private ChatListAdapter mChatListAdapter;

    private String mUsername;
    private String mChatRoom;

    private EditText inputText;
    private ImageButton sendBtn;

    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        mUsername = getIntent().getExtras().getString("userName");
        mChatRoom = getIntent().getExtras().getString("chatRoom");

        showLoadingDialog();
        initializeComponents();
        setEvents();
    }

    private void initializeComponents() {
        mFirebaseRef.setAndroidContext(MessagingActivity.this);
        mFirebaseRef = new Firebase(DatabaseVariables.FIREBASE_URL).child("chat").child(mChatRoom);
        inputText = (EditText) findViewById(R.id.messageInput);
        sendBtn = (ImageButton) findViewById(R.id.sendButton);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle("Чат с " + mUsername);
    }

    private void setEvents(){
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    sendMessage();
                }
                return true;
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void showLoadingDialog(){
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Загрузка...");
        loadingDialog.setCancelable(false);
        loadingDialog.setInverseBackgroundForced(false);
        loadingDialog.show();
    }

        @Override
    public void onStart() {
        super.onStart();
        final ListView listView = (ListView)findViewById(R.id.listChat);
        mChatListAdapter = new ChatListAdapter(mFirebaseRef.limit(150), this);//TODO 150 сообщений - норм?
        listView.setAdapter(mChatListAdapter);
        mChatListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(mChatListAdapter.getCount() - 1);
            }
        });

        mConnectedListener = mFirebaseRef.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if (connected) {
                    loadingDialog.dismiss();
                } else {
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mFirebaseRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
        mChatListAdapter.cleanup();
    }

    private void sendMessage() {
        EditText inputText = (EditText) findViewById(R.id.messageInput);
        String input = inputText.getText().toString();
        String messageTime;

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm, MMM dd", Locale.ENGLISH);
        messageTime = formatter.format(Calendar.getInstance().getTime());
        if (!input.equals("")) {
            ChatMessage chatMessage = new ChatMessage(input, Globals.currentUser.getUserName(), Globals.currentUser.getLogin(), messageTime);
            mFirebaseRef.push().setValue(chatMessage);
            inputText.setText("");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            this.finish();
        return super.onOptionsItemSelected(item);
    }
}
