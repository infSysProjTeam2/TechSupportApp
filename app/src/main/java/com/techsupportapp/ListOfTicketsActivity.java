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
import com.techsupportapp.fragments.BottomSheetFragment;
import com.techsupportapp.fragments.ListOfTicketsFragments;
import com.techsupportapp.services.MessagingService;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;

import java.util.ArrayList;

public class ListOfTicketsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DatabaseReference databaseTicketReference;

    private ArrayList<Ticket> listOfAvailableTickets = new ArrayList<>();
    private ArrayList<Ticket> listOfActiveTickets = new ArrayList<>();
    private ArrayList<Ticket> listOfClosedTickets = new ArrayList<>();

    private ListOfTicketsFragments.SectionsPagerAdapter mSectionsPagerAdapter;

    private ImageView currUserImage;

    //region Listeners

    ValueEventListener ticketListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Globals.logInfoAPK(ListOfTicketsActivity.this, "Скачивание заявок - НАЧАТО");
            listOfAvailableTickets = Globals.Downloads.Tickets.getSpecificTickets(dataSnapshot, DatabaseVariables.ExceptFolder.Tickets.DATABASE_UNMARKED_TICKET_TABLE);
            listOfClosedTickets = Globals.Downloads.Tickets.getSpecificTickets(dataSnapshot, DatabaseVariables.ExceptFolder.Tickets.DATABASE_SOLVED_TICKET_TABLE);
            listOfActiveTickets = Globals.Downloads.Tickets.getSpecificTickets(dataSnapshot, DatabaseVariables.ExceptFolder.Tickets.DATABASE_MARKED_TICKET_TABLE);

            mSectionsPagerAdapter.updateFirstFragment(listOfAvailableTickets, ListOfTicketsActivity.this, FirebaseDatabase.getInstance().getReference());
            mSectionsPagerAdapter.updateSecondFragment(listOfActiveTickets, ListOfTicketsActivity.this);
            mSectionsPagerAdapter.updateThirdFragment(listOfClosedTickets, ListOfTicketsActivity.this);

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
        else {
            new MaterialDialog.Builder(this)
                    .title("Закрыть приложение")
                    .content("Вы действительно хотите закрыть приложение?")
                    .positiveText("Да")
                    .negativeText("Нет")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
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
    }

    private void initializeComponents() {
        databaseTicketReference = FirebaseDatabase.getInstance().getReference(DatabaseVariables.FullPath.Tickets.DATABASE_ALL_TICKET_TABLE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Все заявки");
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

        currUserImage.setImageDrawable(Globals.ImageMethods.getRoundImage(Globals.currentUser.getUserName()));

        mSectionsPagerAdapter = new ListOfTicketsFragments.SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        userName.setText(Globals.currentUser.getUserName());
        Menu nav_menu = navigationView.getMenu();

        userType.setText("Диспетчер");
        nav_menu.findItem(R.id.charts).setVisible(false);
        nav_menu.findItem(R.id.acceptedTickets).setVisible(false);
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

        if (id == R.id.userActions) {
            Intent intent = new Intent(ListOfTicketsActivity.this, UserActionsActivity.class);
            startActivity(intent);
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
                    .positiveText("Да")
                    .negativeText("Нет")
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
        Globals.logInfoAPK(ListOfTicketsActivity.this, "onResume - ВЫПОЛНЕН");
        super.onResume();
    }

    @Override
    protected void onPause() {
        databaseTicketReference.removeEventListener(ticketListener);
        Globals.logInfoAPK(ListOfTicketsActivity.this, "onPause - ВЫПОЛНЕН");
        super.onPause();
    }

    @Override
    protected void onStop() {
        databaseTicketReference.removeEventListener(ticketListener);
        Globals.logInfoAPK(ListOfTicketsActivity.this, "onStop - ВЫПОЛНЕН");
        super.onStop();
    }
}
