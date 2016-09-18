package com.techsupportapp;

import android.content.Context;
import android.content.Intent;
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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.GlobalsMethods;

import java.util.ArrayList;

public class VerifyUserActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private ListView usersView;

    private String mAppId;
    private String mUserId;
    private String mNickname;

    private DatabaseReference databaseRef;
    private ArrayList<User> usersList = new ArrayList<User>();
    private ArrayAdapter<User> adapter;

    private static Context cntxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_user);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        cntxt = getBaseContext();

        mAppId = getIntent().getExtras().getString("appKey");
        mUserId = getIntent().getExtras().getString("uuid");
        mNickname = getIntent().getExtras().getString("nickname");

        initializeComponents();
        setEvents();
    }

    private void initializeComponents(){
        usersView = (ListView)findViewById(R.id.listOfUsers);

        databaseRef = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Авторизация пользователей");
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

        userImage.setImageBitmap(GlobalsMethods.ImageMethods.getclip(GlobalsMethods.ImageMethods.createUserImage(mNickname, VerifyUserActivity.this)));

        userName.setText(mNickname);
        userType.setText("Администратор");

        Menu nav_menu = navigationView.getMenu();
        nav_menu.findItem(R.id.signUpUser).setVisible(false);
    }

    private void setEvents() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        usersView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
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
        } else if (id == R.id.listOfTickets) {
            Intent intent = new Intent(VerifyUserActivity.this, ListOfTicketsActivity.class);
            intent.putExtra("appKey", mAppId);
            intent.putExtra("uuid", mUserId);
            intent.putExtra("nickname", mNickname);
            startActivity(intent);
        } else if (id == R.id.settings) {

        } else if (id == R.id.about) {
            GlobalsMethods.showAbout(VerifyUserActivity.this);
            return true;
        } else if (id == R.id.exit) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
