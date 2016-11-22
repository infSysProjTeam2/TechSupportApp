package com.techsupportapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.adapters.TicketRecyclerAdapter;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.fragments.BottomSheetFragment;
import com.techsupportapp.fragments.MyTicketsFragments;
import com.techsupportapp.utility.DatabaseStorage;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;
import com.techsupportapp.utility.ItemClickSupport;

import java.util.ArrayList;

public class TicketsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private int role;

    private DatabaseReference databaseUserReference;
    private DatabaseReference databaseTicketReference;

    private ArrayList<Ticket> ticketsList = new ArrayList<>();
    private ArrayList<User> usersList = new ArrayList<>();

    private ImageView currUserImage;

    private MyTicketsFragments.SectionsPagerAdapter sectionsPagerAdapter;

    private boolean isDownloaded;

    //region Listeners

    ValueEventListener ticketListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Globals.logInfoAPK(TicketsActivity.this, "Скачивание заявок - БЛОКИРОВАНО");
            while (!isDownloaded);
            Globals.logInfoAPK(TicketsActivity.this, "Скачивание заявок - НАЧАТО");
            if (role != User.SIMPLE_USER) {
                ticketsList = Globals.Downloads.Tickets.getOverseerTicketList(dataSnapshot, Globals.currentUser.getLogin());
                sectionsPagerAdapter.updateFirstFragment(TicketsActivity.this, ticketsList, usersList);

                ArrayList<Ticket> listOfSolvedTickets = Globals.Downloads.Tickets.getSpecificTickets(dataSnapshot, DatabaseVariables.ExceptFolder.Tickets.DATABASE_SOLVED_TICKET_TABLE);
                ArrayList<Ticket> listOfMyClosedTickets = new ArrayList<>();

                for (Ticket ticket : listOfSolvedTickets)
                    if (ticket.getAdminId().equals(Globals.currentUser.getLogin()))
                        listOfMyClosedTickets.add(ticket);
                sectionsPagerAdapter.updateSecondFragment(TicketsActivity.this, listOfMyClosedTickets, usersList);
            } else {
                ticketsList = Globals.Downloads.Tickets.getUserSpecificTickets(dataSnapshot, DatabaseVariables.ExceptFolder.Tickets.DATABASE_MARKED_TICKET_TABLE, Globals.currentUser.getLogin());
                ticketsList.addAll(Globals.Downloads.Tickets.getUserSpecificTickets(dataSnapshot, DatabaseVariables.ExceptFolder.Tickets.DATABASE_UNMARKED_TICKET_TABLE, Globals.currentUser.getLogin()));
                sectionsPagerAdapter.updateFirstFragment(TicketsActivity.this, ticketsList, usersList);

                ArrayList<Ticket> listOfSolvedTickets = Globals.Downloads.Tickets.getSpecificTickets(dataSnapshot, DatabaseVariables.ExceptFolder.Tickets.DATABASE_SOLVED_TICKET_TABLE);
                ArrayList<Ticket> listOfMyClosedTickets = new ArrayList<>();

                for (Ticket ticket : listOfSolvedTickets)
                    if (ticket.getUserId().equals(Globals.currentUser.getLogin()))
                        listOfMyClosedTickets.add(ticket);
                sectionsPagerAdapter.updateSecondFragment(TicketsActivity.this, listOfMyClosedTickets, usersList);
            }
            Globals.logInfoAPK(TicketsActivity.this, "Скачивание заявок - ЗАКОНЧЕНО");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    ValueEventListener userListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Globals.logInfoAPK(TicketsActivity.this, "Скачивание данных зарегистрированных пользователей - НАЧАТО");
            isDownloaded = false;
            usersList = Globals.Downloads.Users.getVerifiedUserList(dataSnapshot);
            isDownloaded = true;
            Globals.logInfoAPK(TicketsActivity.this, "Скачивание данных зарегистрированных пользователей - ЗАКОНЧЕНО");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets);
        role = Globals.currentUser.getRole();

        initializeComponents();
        setEvents();
    }

    private void initializeComponents(){
        databaseUserReference = FirebaseDatabase.getInstance().getReference(DatabaseVariables.FullPath.Users.DATABASE_ALL_USER_TABLE);
        databaseTicketReference = FirebaseDatabase.getInstance().getReference(DatabaseVariables.FullPath.Tickets.DATABASE_ALL_TICKET_TABLE);

        isDownloaded = false;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Мои заявки");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        currUserImage = (ImageView)navigationView.getHeaderView(0).findViewById(R.id.userImage);
        TextView userName = (TextView)navigationView.getHeaderView(0).findViewById(R.id.userName);
        TextView userType = (TextView)navigationView.getHeaderView(0).findViewById(R.id.userType);

        currUserImage.setImageBitmap(Globals.ImageMethods.getClip(Globals.ImageMethods.createUserImage(Globals.currentUser.getUserName(), TicketsActivity.this)));

        sectionsPagerAdapter = new MyTicketsFragments.SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Menu nav_menu = navigationView.getMenu();
        userName.setText(Globals.currentUser.getUserName());
        if (role == User.ADMINISTRATOR) {
            userType.setText("Администратор");
            nav_menu.findItem(R.id.charts).setVisible(false);
        }
        else if (role == User.DEPARTMENT_CHIEF) {
            userType.setText("Начальник отдела");
            nav_menu.findItem(R.id.userActions).setVisible(false);
        }
        else if (role == User.DEPARTMENT_MEMBER){
            userType.setText("Работник отдела");
            nav_menu.findItem(R.id.userActions).setVisible(false);
            nav_menu.findItem(R.id.charts).setVisible(false);
        }
        else {
            userType.setText("Пользователь");
            nav_menu.findItem(R.id.userActions).setVisible(false);
            nav_menu.findItem(R.id.charts).setVisible(false);
            nav_menu.findItem(R.id.listOfTickets).setTitle("Создать заявку");
        }
    }

    private void setEvents() {
        currUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialogFragment bottomSheetDialogFragment = BottomSheetFragment.newInstance(Globals.currentUser.getLogin(), Globals.currentUser.getLogin(), Globals.currentUser);
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else {
            new MaterialDialog.Builder(this)
                    .title("Закрыть приложение")
                    .content("Вы действительно хотите закрыть приложение?")
                    .positiveText(android.R.string.yes)
                    .negativeText(android.R.string.no)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            TicketsActivity.this.finishAffinity();
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.listOfTickets) {
            if (role != User.SIMPLE_USER) {
                Intent intent = new Intent(TicketsActivity.this, ListOfTicketsActivity.class);
                startActivity(intent);
            }
            else
            {
                Intent intent = new Intent(TicketsActivity.this, CreateTicketActivity.class);
                startActivity(intent);
            }
        } else if (id == R.id.userActions) {
            Intent intent = new Intent(TicketsActivity.this, UserActionsActivity.class);
            startActivity(intent);
        } else if (id == R.id.charts) {
            Intent intent = new Intent(TicketsActivity.this, ChartsActivity.class);
            startActivity(intent);
        } else if (id == R.id.settings) {
            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
        } else if (id == R.id.about) {
            Globals.showAbout(TicketsActivity.this);
        } else if (id == R.id.logOut) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.exit) {
            new MaterialDialog.Builder(this)
                    .title("Закрыть приложение")
                    .content("Вы действительно хотите закрыть приложение?")
                    .positiveText(android.R.string.yes)
                    .negativeText(android.R.string.no)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            TicketsActivity.this.finishAffinity();
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStop() {
        databaseUserReference.removeEventListener(userListener);
        databaseTicketReference.removeEventListener(ticketListener);
        super.onStop();
        Globals.logInfoAPK(TicketsActivity.this,  "onStop - ВЫПОЛНЕН");
    }

    @Override
    protected void onPause() {
        databaseUserReference.removeEventListener(userListener);
        databaseTicketReference.removeEventListener(ticketListener);
        isDownloaded = false;
        overridePendingTransition(R.anim.anim_slide_from_right, R.anim.anim_slide_to_left);
        super.onPause();
        Globals.logInfoAPK(TicketsActivity.this,  "onPause - ВЫПОЛНЕН");
    }

    @Override
    protected void onResume() {
        databaseUserReference.addValueEventListener(userListener);
        databaseTicketReference.addValueEventListener(ticketListener);
        super.onResume();
        Globals.logInfoAPK(TicketsActivity.this,  "onResume - ВЫПОЛНЕН");
    }
}
