package com.techsupportapp;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.adapters.UnverifiedUserAdapter;
import com.techsupportapp.databaseClasses.UnverifiedUser;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.GlobalsMethods;

import java.util.ArrayList;

public class UserActionsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private ListView unverifiedUsersView;
    private ListView usersView;

    private String mAppId;
    private String mUserId;
    private String mNickname;

    private DatabaseReference databaseRef;
    private ArrayList<UnverifiedUser> unverifiedUsersList = new ArrayList<UnverifiedUser>();
    private UnverifiedUserAdapter adapter;

    private static Context cntxt;
    private TabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_actions);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        cntxt = getBaseContext();

        mAppId = getIntent().getExtras().getString("appKey");
        mUserId = getIntent().getExtras().getString("uuid");
        mNickname = getIntent().getExtras().getString("nickname");

        initTabHost();
        initializeComponents();
        setEvents();
    }

    private void initializeComponents(){
        unverifiedUsersView = (ListView)findViewById(R.id.listOfUnverifiedUsers);
        usersView = (ListView)findViewById(R.id.listOfUsers);

        databaseRef = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Пользователи");
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

        userImage.setImageBitmap(GlobalsMethods.ImageMethods.getclip(GlobalsMethods.ImageMethods.createUserImage(mNickname, UserActionsActivity.this)));

        userName.setText(mNickname);
        userType.setText("Администратор");

        Menu nav_menu = navigationView.getMenu();
        nav_menu.findItem(R.id.signUpUser).setVisible(false);
    }

    private void initTabHost(){
        tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        initTabSpec("tag1", R.id.tab1, "Авторизация");
        initTabSpec("tag2", R.id.tab2, "Все");

        tabHost.setCurrentTab(0);
    }

    private void initTabSpec(String tag, int viewId, String label) {
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(tag);
        tabSpec.setContent(viewId);
        tabSpec.setIndicator(label);
        tabHost.addTab(tabSpec);
    }

    private void setEvents() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                unverifiedUsersList.clear();
                for (DataSnapshot userRecord : dataSnapshot.child(DatabaseVariables.DATABASE_UNVERIFIED_USER_TABLE).getChildren()) {
                    UnverifiedUser user = userRecord.getValue(UnverifiedUser.class);
                    unverifiedUsersList.add(user);
                }
                adapter = new UnverifiedUserAdapter(getApplicationContext(), unverifiedUsersList);
                unverifiedUsersView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        unverifiedUsersView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                databaseRef.child(DatabaseVariables.DATABASE_VERIFIED_USER_TABLE).child(unverifiedUsersList.get(position).userId).setValue(unverifiedUsersList.get(position).verifyUser());
                databaseRef.child(DatabaseVariables.DATABASE_UNVERIFIED_USER_TABLE).child(unverifiedUsersList.get(position).userId).removeValue();
                Toast.makeText(getApplicationContext(), "Пользователь добавлен в базу данных", Toast.LENGTH_LONG).show();
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
            Intent intent = new Intent(UserActionsActivity.this, ListOfTicketsActivity.class);
            intent.putExtra("appKey", mAppId);
            intent.putExtra("uuid", mUserId);
            intent.putExtra("nickname", mNickname);
            startActivity(intent);
        } else if (id == R.id.settings) {

        } else if (id == R.id.about) {
            GlobalsMethods.showAbout(UserActionsActivity.this);
            return true;
        } else if (id == R.id.exit) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);

            builder.setPositiveButton("Закрыть приложение", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    exit();
                }
            });

            builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.setCancelable(false);
            builder.setMessage("Вы действительно хотите закрыть приложение?");
            builder.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void exit(){
        this.finishAffinity();
    }
}
