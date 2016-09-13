package com.techsupportapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateTicketActivity extends AppCompatActivity {

    //region Fields

    private DatabaseReference databaseReference;
    private SharedPreferences mSettings;

    private int ticketCount;
    private String userId;

    //endregion

    //region Composite Controls

    private EditText topicET;
    private EditText messageET;

    private Button createBut;
    private Button cancelBut;

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ticket);

        userId = getIntent().getExtras().getString("userId");

        initializeComponents();

        setEvents();
    }

    private void initializeComponents() {
        topicET = (EditText)findViewById(R.id.message_topic_text);
        messageET = (EditText)findViewById(R.id.message_text);

        createBut = (Button)findViewById(R.id.create_but);
        cancelBut = (Button)findViewById(R.id.cancel_but);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mSettings = getSharedPreferences(SharedPrefsVariables.APP_PREFERENCES, Context.MODE_PRIVATE);

        ticketCount = mSettings.getInt(SharedPrefsVariables.TICKETS_COUNT, 0);
    }

    private void setEvents() {
        createBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (topicET.getText().toString().equals("") || messageET.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Заполните поля", Toast.LENGTH_LONG).show();
                } else {
                    Ticket newTicket = new Ticket("ticket" + ticketCount, userId, topicET.getText().toString(), messageET.getText().toString());
                    databaseReference.child(DatabaseVariables.DATABASE_UNMARKED_TICKET_TABLE).child("ticket" + ticketCount++).setValue(newTicket);
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putInt(SharedPrefsVariables.TICKETS_COUNT, ticketCount);
                    editor.apply();
                }
                Toast.makeText(getApplicationContext(), "ТИКЕТ ТИПА ДОБАВЛЕН", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resizeTextComponents() {

    }
}
