package com.techsupportapp;

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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.adapters.TicketAdapter;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.GlobalsMethods;

import java.util.ArrayList;

public class TicketsOverviewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ListView ticketsOverview;

    private String mUserId;
    private String mNickname;
    private int role;

    private DatabaseReference databaseRef;
    private ArrayList<Ticket> ticketsOverviewList = new ArrayList<Ticket>();
    private ArrayAdapter<Ticket> adapter;

    private ImageView currUserImage;

    public static Bundle makeArgs(String uuid, String nickname, int role) {
        Bundle args = new Bundle();
        args.putString("uuid", uuid);
        args.putString("nickname", nickname);
        args.putInt("role", role);
        return args;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets_overview);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mUserId = getIntent().getExtras().getString("uuid");
        mNickname = getIntent().getExtras().getString("nickname");
        role = getIntent().getExtras().getInt("role");

        initializeComponents();
        setEvents();
    }

    private void initializeComponents(){
        ticketsOverview = (ListView)findViewById(R.id.ticketsOverview);

        databaseRef = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mNickname);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        currUserImage = (ImageView)navigationView.getHeaderView(0).findViewById(R.id.userImage);
        TextView userName = (TextView)navigationView.getHeaderView(0).findViewById(R.id.userName);
        TextView userType = (TextView)navigationView.getHeaderView(0).findViewById(R.id.userType);

        currUserImage.setImageBitmap(GlobalsMethods.ImageMethods.getclip(GlobalsMethods.ImageMethods.createUserImage(mNickname, TicketsOverviewActivity.this)));

        Menu nav_menu = navigationView.getMenu();
        userName.setText(mNickname);
        if (role == User.ADMINISTRATOR) {
            //TODO ограничения/дозволения
            userType.setText("Администратор");
        }
        else if (role == User.DEPARTMENT_CHIEF) {
            //TODO ограничения/дозволения
        }
        else if (role == User.DEPARTMENT_MEMBER){
            //TODO ограничения/дозволения
        }
        else {
            userType.setText("Пользователь");
            nav_menu.findItem(R.id.signUpUser).setVisible(false);
            nav_menu.findItem(R.id.charts).setVisible(false);
            nav_menu.findItem(R.id.listOfChannels).setTitle("Список ваших заявок");
            nav_menu.findItem(R.id.listOfTickets).setTitle("Создать заявку");
        }
    }

    private void setEvents() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (role == User.ADMINISTRATOR) {
                    ticketsOverviewList = GlobalsMethods.Downloads.getAdminTicketList(dataSnapshot, mUserId);
                    adapter = new TicketAdapter(getApplicationContext(), ticketsOverviewList);
                    ticketsOverview.setAdapter(adapter);
                } else {
                    ticketsOverviewList = GlobalsMethods.Downloads.getUserSpecificTickets(dataSnapshot, DatabaseVariables.Tickets.DATABASE_MARKED_TICKET_TABLE, mUserId);
                    ticketsOverviewList.addAll(GlobalsMethods.Downloads.getUserSpecificTickets(dataSnapshot, DatabaseVariables.Tickets.DATABASE_UNMARKED_TICKET_TABLE, mUserId));
                    adapter = new TicketAdapter(getApplicationContext(), ticketsOverviewList);
                    ticketsOverview.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ticketsOverview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(TicketsOverviewActivity.this, MessagingActivity.class);
                if (role == User.ADMINISTRATOR) {
                    intent.putExtra("currUserName", mNickname);
                    intent.putExtra("userName", ticketsOverviewList.get(position).getUserName());
                    intent.putExtra("chatRoom", ticketsOverviewList.get(position).getTicketId());
                } else if (role == User.DEPARTMENT_CHIEF) {
                    //TODO Что открывается
                } else if (role == User.DEPARTMENT_MEMBER){
                    //TODO Что открывается
                }
                else {
                    if (ticketsOverviewList.get(position).getAdminId() == null || ticketsOverviewList.get(position).getAdminId().equals("")) {
                        Toast.makeText(getApplicationContext(), "Администратор еще не просматривал ваше сообщение, подождите", Toast.LENGTH_LONG).show();
                        return;
                    }
                    else {
                        intent.putExtra("currUserName", mNickname);
                        intent.putExtra("userName", ticketsOverviewList.get(position).getAdminName());
                        intent.putExtra("chatRoom", ticketsOverviewList.get(position).getTicketId());
                    }
                }
                startActivity(intent);
            }
        });

        ticketsOverview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (role == User.ADMINISTRATOR){
                    //TODO Что делается
                } else if (role == User.DEPARTMENT_CHIEF) {
                    //TODO Что делается
                } else if (role == User.DEPARTMENT_MEMBER){
                    //TODO Что делается
                }
                else {
                    //TODO вы точно хотите отозвать тикет
                    if (ticketsOverviewList.get(position).getAdminId() == null || ticketsOverviewList.get(position).getAdminId().equals(""))
                        databaseRef.child(DatabaseVariables.Tickets.DATABASE_UNMARKED_TICKET_TABLE).child(ticketsOverviewList.get(position).getTicketId()).removeValue();
                    else; //TODO проблема решена
                }
                return true;
            }
        });

        currUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TicketsOverviewActivity.this, UserProfileActivity.class);
                intent.putExtra("userId", mUserId);
                intent.putExtra("currUserId", mUserId);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.listOfTickets) {
            if (role == User.ADMINISTRATOR) {
                Intent intent = new Intent(TicketsOverviewActivity.this, ListOfTicketsActivity.class);
                intent.putExtra("uuid", mUserId);
                intent.putExtra("nickname", mNickname);
                startActivity(intent);
            } else if (role == User.DEPARTMENT_CHIEF) {
                //TODO Что открывается
            } else if (role == User.DEPARTMENT_MEMBER){
                //TODO Что открывается
            }
            else
            {
                Intent intent = new Intent(TicketsOverviewActivity.this, CreateTicketActivity.class);
                intent.putExtra("uuid", mUserId);
                intent.putExtra("nickname", mNickname);
                startActivity(intent);
            }
        } else if (id == R.id.signUpUser) {
            Intent intent = new Intent(TicketsOverviewActivity.this, UserActionsActivity.class);
            intent.putExtra("uuid", mUserId);
            intent.putExtra("nickname", mNickname);
            startActivity(intent);
        } else if (id == R.id.charts) {
            Intent intent = new Intent(TicketsOverviewActivity.this, ChartsActivity.class);
            intent.putExtra("uuid", mUserId);
            intent.putExtra("nickname", mNickname);
            startActivity(intent);
            finish();
        } else if (id == R.id.settings) {

        } else if (id == R.id.about) {
            GlobalsMethods.showAbout(TicketsOverviewActivity.this);
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
