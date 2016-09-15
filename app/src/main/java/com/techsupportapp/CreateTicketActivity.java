package com.techsupportapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateTicketActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    //region Fields

    private DatabaseReference databaseReference;
    private SharedPreferences mSettings;

    private int ticketCount;
    private String mAppId;
    private String mUserId;
    private String mNickname;
    private boolean isAdmin;

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

        mAppId = getIntent().getExtras().getString("appKey");
        mUserId = getIntent().getExtras().getString("uuid");
        mNickname = getIntent().getExtras().getString("nickname");
        isAdmin = getIntent().getExtras().getBoolean("isAdmin");
        initializeComponents();

        setEvents();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.listOfChannels) {
            Intent intent = new Intent(CreateTicketActivity.this, ListOfChannelsActivity.class);
            intent.putExtra("appKey", mAppId);
            intent.putExtra("uuid", mUserId);
            intent.putExtra("nickname", mNickname);
            intent.putExtra("isAdmin", isAdmin);
            startActivity(intent);
        } else if (id == R.id.settings) {

        } else if (id == R.id.about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateTicketActivity.this);
            builder.setTitle("О программе");
            String str = String.format("Tech Support App V1.0");
            builder.setMessage(str);
            builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {

                }
            });
            builder.setCancelable(false);
            AlertDialog alert = builder.create();
            alert.show();
            return true;
        } else if (id == R.id.exit) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initializeComponents() {
        topicET = (EditText)findViewById(R.id.message_topic_text);
        messageET = (EditText)findViewById(R.id.message_text);

        createBut = (Button)findViewById(R.id.create_but);
        cancelBut = (Button)findViewById(R.id.cancel_but);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mSettings = getSharedPreferences(SharedPrefsVariables.APP_PREFERENCES, Context.MODE_PRIVATE);

        ticketCount = mSettings.getInt(SharedPrefsVariables.TICKETS_COUNT, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Создать заявку");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setEvents() {
        createBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (topicET.getText().toString().equals("") || messageET.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Заполните поля", Toast.LENGTH_LONG).show();
                } else {
                    Ticket newTicket = new Ticket("ticket" + ticketCount, mUserId, topicET.getText().toString(), messageET.getText().toString());
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
