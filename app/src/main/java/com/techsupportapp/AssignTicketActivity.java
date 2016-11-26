package com.techsupportapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.innodroid.expandablerecycler.ExpandableRecyclerAdapter;
import com.techsupportapp.adapters.SpecialistsExpandableRecyclerAdapter;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;

import java.util.ArrayList;

public class AssignTicketActivity extends AppCompatActivity {

    RecyclerView specialistsView;
    ArrayList<User> specialistsList;
    DatabaseReference databaseReference;
    Ticket currentTicket;

    ValueEventListener userListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Globals.logInfoAPK(AssignTicketActivity.this, "Скачивание данных пользователей - НАЧАТО");
            specialistsList = Globals.Downloads.Users.getSpecificVerifiedUserList(dataSnapshot, DatabaseVariables.FullPath.Users.DATABASE_VERIFIED_SPECIALIST_TABLE);
            ArrayList<SpecialistsExpandableRecyclerAdapter.TicketListItem> ticketListItems = new ArrayList<>();

            for (User user : specialistsList){
                ticketListItems.add(new SpecialistsExpandableRecyclerAdapter.TicketListItem(user));
                ArrayList<Ticket> tickets = Globals.Downloads.Tickets.getOverseerTicketList(dataSnapshot, user.getLogin(), false);
                for (Ticket ticket : tickets)
                    ticketListItems.add(new SpecialistsExpandableRecyclerAdapter.TicketListItem(ticket));
            }

            SpecialistsExpandableRecyclerAdapter adapter = new SpecialistsExpandableRecyclerAdapter(AssignTicketActivity.this, ticketListItems, currentTicket);
            adapter.setMode(ExpandableRecyclerAdapter.MODE_ACCORDION);

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(AssignTicketActivity.this, LinearLayoutManager.VERTICAL, false);
            specialistsView.setLayoutManager(mLayoutManager);
            specialistsView.setHasFixedSize(false);
            specialistsView.setAdapter(adapter);

            Globals.logInfoAPK(AssignTicketActivity.this, "Скачивание данных пользователей - ОКОНЧЕНО");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_ticket);
        setTitle("Специалисты");

        currentTicket = (Ticket) getIntent().getExtras().getSerializable("currentTicket");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initializeComponents();
        setEvents();
    }

    private void initializeComponents() {
        specialistsView = (RecyclerView) findViewById(R.id.specialistsList);

        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void setEvents(){

    };

    @Override
    protected void onResume() {
        super.onResume();
        databaseReference.addValueEventListener(userListener);
        Globals.logInfoAPK(AssignTicketActivity.this, "onResume - ВЫПОЛНЕН");
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(userListener);
        Globals.logInfoAPK(AssignTicketActivity.this, "onPause - ВЫПОЛНЕН");
    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseReference.removeEventListener(userListener);
        Globals.logInfoAPK(AssignTicketActivity.this, "onStop - ВЫПОЛНЕН");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
