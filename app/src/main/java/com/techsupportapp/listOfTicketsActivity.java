package com.techsupportapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

public class ListOfTicketsActivity extends AppCompatActivity {

    private ListView listOfTickets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_tickets);

        initializeComponents();

        setEvents();
    }

    private void initializeComponents() {
        listOfTickets = (ListView)findViewById(R.id.listOfTickets);
    }

    private void setEvents() {

    }
}
