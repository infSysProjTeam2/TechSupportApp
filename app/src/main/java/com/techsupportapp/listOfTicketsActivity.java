package com.techsupportapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListOfTicketsActivity extends AppCompatActivity {

    private ListView viewOfTickets;
    private TextView problemBut;
    private DatabaseReference databaseRef;
    private ArrayList<Ticket> listOfTickets = new ArrayList<Ticket>();
    private ArrayAdapter<Ticket> adapter;
    private String adminId;

    private String mAppId;
    private String mUserId;
    private String mNickname;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_tickets);

        adminId = getIntent().getExtras().getString("adminId");
        mAppId = getIntent().getExtras().getString("appKey");
        mUserId = getIntent().getExtras().getString("uuid");
        mNickname = getIntent().getExtras().getString("nickname");
        isAdmin = getIntent().getExtras().getBoolean("isAdmin");

        initializeComponents();

        setEvents();
    }

    private void initializeComponents() {
        viewOfTickets = (ListView)findViewById(R.id.listOfTickets);
        problemBut = (TextView)findViewById(R.id.problemsBut);





        databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    private void setEvents() {
        problemBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listOfTickets.clear();
                for (DataSnapshot ticketRecord : dataSnapshot.child(DatabaseVariables.DATABASE_UNMARKED_TICKET_TABLE).getChildren()) {
                    Ticket ticket = ticketRecord.getValue(Ticket.class);
                    listOfTickets.add(ticket);
                }
                adapter = new ArrayAdapter<Ticket>(getApplicationContext(), android.R.layout.simple_list_item_1, listOfTickets);
                viewOfTickets.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        viewOfTickets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listOfTickets.get(position).addAdmin(adminId);
                databaseRef.child(DatabaseVariables.DATABASE_MARKED_TICKET_TABLE).child(listOfTickets.get(position).ticketId).setValue(listOfTickets.get(position));
                databaseRef.child(DatabaseVariables.DATABASE_UNMARKED_TICKET_TABLE).child(listOfTickets.get(position).ticketId).removeValue();

                Intent intent = new Intent(ListOfTicketsActivity.this, ChatActivity.class);
                Bundle args = ChatActivity.makeMessagingStartArgs(mAppId, adminId, mNickname, listOfTickets.get(position).userId);
                intent.putExtras(args);

                startActivityForResult(intent, 210);
            }
        });
    }

}
