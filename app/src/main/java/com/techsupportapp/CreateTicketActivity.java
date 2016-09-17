package com.techsupportapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.variables.DatabaseVariables;
import com.techsupportapp.variables.GlobalsMethods;

public class CreateTicketActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    //region Fields

    private DatabaseReference databaseReference;

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
        initializeComponents();

        setEvents();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.listOfChannels) {
            finish();
        } else if (id == R.id.settings) {

        } else if (id == R.id.about) {
            GlobalsMethods.showAbout(CreateTicketActivity.this);
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Создать заявку");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ImageView userImage = (ImageView)navigationView.getHeaderView(0).findViewById(R.id.userImage);
        TextView userName = (TextView)navigationView.getHeaderView(0).findViewById(R.id.userName);
        TextView userType = (TextView)navigationView.getHeaderView(0).findViewById(R.id.userType);

        int COVER_IMAGE_SIZE = 150;
        LetterBitmap letterBitmap = new LetterBitmap(CreateTicketActivity.this);
        Bitmap letterTile = letterBitmap.getLetterTile(mNickname.substring(0), mNickname.substring(1), COVER_IMAGE_SIZE, COVER_IMAGE_SIZE);
        userImage.setImageBitmap(ChatActivity.getclip(letterTile));

        userName.setText(mNickname);
        userType.setText("Пользователь");

        Menu nav_menu = navigationView.getMenu();
        nav_menu.findItem(R.id.signUpUser).setVisible(false);
        nav_menu.findItem(R.id.listOfTickets).setVisible(false);
        nav_menu.findItem(R.id.listOfChannels).setTitle("Список ваших заявок");
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
                    databaseReference.child(DatabaseVariables.DATABASE_TICKET_INDEX_COUNTER).setValue(ticketCount);
                }
                Toast.makeText(getApplicationContext(), "ТИКЕТ ТИПА ДОБАВЛЕН", Toast.LENGTH_LONG).show();
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ticketCount = dataSnapshot.child(DatabaseVariables.DATABASE_TICKET_INDEX_COUNTER).getValue(int.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void resizeTextComponents() {

    }
}
