package com.techsupportapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;
import android.widget.ListView;

public class listOfTicketsActivity extends AppCompatActivity {

    private ListView listOfTickets;
    private TextView problemBut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_tickets);

        initializeComponents();

        setEvents();
    }

    private void initializeComponents() {
        listOfTickets = (ListView)findViewById(R.id.listOfTickets);
        problemBut = (TextView)findViewById(R.id.problemsBut);
    }

    private void setEvents() {
        problemBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
