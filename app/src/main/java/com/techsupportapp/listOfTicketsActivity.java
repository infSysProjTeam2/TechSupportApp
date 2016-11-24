package com.techsupportapp;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.fragments.BottomSheetFragment;
import com.techsupportapp.fragments.ListOfTicketsFragments;
import com.techsupportapp.services.MessagingService;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;

import java.util.ArrayList;

public class ListOfTicketsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DatabaseReference databaseUserReference;
    private DatabaseReference databaseTicketReference;

    private ArrayList<Ticket> listOfAvailableTickets = new ArrayList<>();
    private ArrayList<Ticket> listOfMyClosedTickets = new ArrayList<>();
    private ArrayList<Ticket> listOfSolvedTickets = new ArrayList<>();

    private ArrayList<User> usersList = new ArrayList<>();

    private ListOfTicketsFragments.SectionsPagerAdapter mSectionsPagerAdapter;

    private ImageView currUserImage;

    //region Listeners

    ValueEventListener userListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Globals.logInfoAPK(ListOfTicketsActivity.this, "Скачивание данных зарегистрированных пользователей - НАЧАТО");
            usersList = Globals.Downloads.Users.getVerifiedUserList(dataSnapshot);
            Globals.logInfoAPK(ListOfTicketsActivity.this, "Скачивание данных зарегистрированных пользователей - ОКОНЧЕНО");
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    ValueEventListener ticketListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Globals.logInfoAPK(ListOfTicketsActivity.this, "Скачивание заявок - НАЧАТО");
            listOfAvailableTickets = Globals.Downloads.Tickets.getSpecificTickets(dataSnapshot, DatabaseVariables.ExceptFolder.Tickets.DATABASE_UNMARKED_TICKET_TABLE);
            listOfSolvedTickets = Globals.Downloads.Tickets.getSpecificTickets(dataSnapshot, DatabaseVariables.ExceptFolder.Tickets.DATABASE_SOLVED_TICKET_TABLE);
            listOfMyClosedTickets.clear();

            for (Ticket ticket : listOfSolvedTickets)
                if (ticket.getAdminId().equals(Globals.currentUser.getLogin()))
                    listOfMyClosedTickets.add(ticket);

            mSectionsPagerAdapter.updateFirstFragment(listOfAvailableTickets, usersList, ListOfTicketsActivity.this, FirebaseDatabase.getInstance().getReference());
            mSectionsPagerAdapter.updateSecondFragment(listOfMyClosedTickets, usersList, ListOfTicketsActivity.this);
            mSectionsPagerAdapter.updateThirdFragment(listOfSolvedTickets, usersList, ListOfTicketsActivity.this);

            Globals.logInfoAPK(ListOfTicketsActivity.this, "Скачивание заявок - ОКОНЧЕНО");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_tickets);

        initializeComponents();
        setEvents();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            finish();
    }

    private void initializeComponents() {
        databaseUserReference = FirebaseDatabase.getInstance().getReference(DatabaseVariables.FullPath.Users.DATABASE_ALL_USER_TABLE);
        databaseTicketReference = FirebaseDatabase.getInstance().getReference(DatabaseVariables.FullPath.Tickets.DATABASE_ALL_TICKET_TABLE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Список заявок");
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

        currUserImage.setImageBitmap(Globals.ImageMethods.getClip(Globals.ImageMethods.createUserImage(Globals.currentUser.getUserName(), ListOfTicketsActivity.this)));

        mSectionsPagerAdapter = new ListOfTicketsFragments.SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        userName.setText(Globals.currentUser.getUserName());
        Menu nav_menu = navigationView.getMenu();

        int role = Globals.currentUser.getRole();

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.acceptedTickets) {
            finish();
        } else if (id == R.id.userActions) {
            Intent intent = new Intent(ListOfTicketsActivity.this, UserActionsActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.charts) {
            Intent intent = new Intent(ListOfTicketsActivity.this, ChartsActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.settings) {
            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
        } else if (id == R.id.about) {
            Globals.showAbout(ListOfTicketsActivity.this);
        } else if (id == R.id.logOut) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.exit) {
            new MaterialDialog.Builder(this)
                    .title("Закрыть приложение")
                    .content("Вы действительно хотите закрыть приложение?")
                    .positiveText(android.R.string.yes)
                    .negativeText(android.R.string.no)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (PreferenceManager.getDefaultSharedPreferences(ListOfTicketsActivity.this).getBoolean("allowNotifications", false)) {
                                MessagingService.stopMessagingService(getApplicationContext());
                                Log.e("СЛУЖБА", "ОСТАНОВЛЕНА");
                            }
                            ListOfTicketsActivity.this.finishAffinity();
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
    protected void onResume() {
        databaseTicketReference.addValueEventListener(ticketListener);
        databaseUserReference.addValueEventListener(userListener);
        Globals.logInfoAPK(ListOfTicketsActivity.this, "onResume - ВЫПОЛНЕН");
        super.onResume();
    }

    @Override
    protected void onPause() {
        databaseTicketReference.removeEventListener(ticketListener);
        databaseUserReference.removeEventListener(userListener);
        Globals.logInfoAPK(ListOfTicketsActivity.this, "onPause - ВЫПОЛНЕН");
        super.onPause();
    }

    @Override
    protected void onStop() {
        databaseTicketReference.removeEventListener(ticketListener);
        databaseUserReference.removeEventListener(userListener);
        Globals.logInfoAPK(ListOfTicketsActivity.this, "onStop - ВЫПОЛНЕН");
        super.onStop();
    }
}
