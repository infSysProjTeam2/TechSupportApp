package com.techsupportapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.adapters.ChatRecyclerAdapter;
import com.techsupportapp.databaseClasses.ChatMessage;
import com.techsupportapp.utility.DatabaseStorage;
import com.techsupportapp.utility.Globals;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MessagingActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private ChatRecyclerAdapter chatRecyclerAdapter;

    RecyclerView recyclerView;

    private String mChatRoom;
    private String topic;

    private EditText inputText;
    private ImageButton sendBtn;

    private MaterialDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        mChatRoom = getIntent().getExtras().getString("chatRoom");
        topic = getIntent().getExtras().getString("topic");

        showLoadingDialog();
        initializeComponents();
        setEvents();
    }

    private void initializeComponents() {
        databaseReference = FirebaseDatabase.getInstance().getReference("chat").child(mChatRoom);
        inputText = (EditText) findViewById(R.id.messageInput);
        sendBtn = (ImageButton) findViewById(R.id.sendButton);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle(topic);
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
        recyclerView = (RecyclerView) findViewById(R.id.listChat);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(MessagingActivity.this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        chatRecyclerAdapter = new ChatRecyclerAdapter(databaseReference.limitToLast(150), MessagingActivity.this);//TODO 150 сообщений - норм?
        recyclerView.setAdapter(chatRecyclerAdapter);
        chatRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                recyclerView.scrollToPosition(chatRecyclerAdapter.getItemCount() - 1);
            }
        });


        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = !(dataSnapshot.getKey().isEmpty());
                if (connected) {
                    loadingDialog.dismiss();
                    databaseReference.removeEventListener(valueEventListener);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Ошибка в работе базы данных. Обратитесь к администратору компании или разработчику", Toast.LENGTH_LONG).show();
            }
        };
        chatRecyclerAdapter.notifyDataSetChanged();
        databaseReference.addValueEventListener(valueEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        databaseReference.removeEventListener(valueEventListener);
        chatRecyclerAdapter.cleanup();
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
