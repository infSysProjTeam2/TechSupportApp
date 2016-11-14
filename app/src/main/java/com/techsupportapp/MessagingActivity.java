package com.techsupportapp;

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
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.adapters.ChatListAdapter;
import com.techsupportapp.databaseClasses.ChatMessage;
import com.techsupportapp.utility.Globals;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MessagingActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private ChatListAdapter mChatListAdapter;

    private String mUsername;
    private String mChatRoom;

    private EditText inputText;
    private ImageButton sendBtn;

    private MaterialDialog loadingDialog;

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
        databaseReference = FirebaseDatabase.getInstance().getReference("chat/" + mChatRoom);
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
        loadingDialog = new MaterialDialog.Builder(this)
                .content("Загрузка...")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .show();
    }

    @Override
    public void onStart() {
        super.onStart();
        final ListView listView = (ListView)findViewById(R.id.listChat);
        mChatListAdapter = new ChatListAdapter(databaseReference.limitToLast(150), this);//TODO 150 сообщений - норм?
        listView.setAdapter(mChatListAdapter);
        mChatListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(mChatListAdapter.getCount() - 1);
            }
        });

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (dataSnapshot.getKey().isEmpty());
                if (!connected)
                    loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Ошибка в работе базы данных. Обратитесь к администратору компании или разработчику", Toast.LENGTH_LONG).show();
            }
        };

        databaseReference.addValueEventListener(valueEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        databaseReference.removeEventListener(valueEventListener);
        mChatListAdapter.cleanup();
    }

    private void sendMessage() {
        EditText inputText = (EditText) findViewById(R.id.messageInput);
        String input = inputText.getText().toString();
        String messageTime;

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm, MMM dd", Locale.ENGLISH);
        messageTime = formatter.format(Calendar.getInstance().getTime());
        if (!input.equals("")) {
            ChatMessage chatMessage = new ChatMessage(input, Globals.currentUser.getUserName(), Globals.currentUser.getLogin(), messageTime, true);
            databaseReference.push().setValue(chatMessage);
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
