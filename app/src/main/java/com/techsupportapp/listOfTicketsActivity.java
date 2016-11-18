package com.techsupportapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.adapters.BottomSheetFragment;
import com.techsupportapp.adapters.TicketRecyclerAdapter;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.services.MessagingService;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;
import com.techsupportapp.utility.ItemClickSupport;

import java.util.ArrayList;

public class ListOfTicketsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static Context context;

    private static RecyclerView viewOfAvailableTickets;
    private static RecyclerView viewOfMyClosedTickets;
    private static RecyclerView viewOfSolvedTickets;

    private static DatabaseReference databaseUserReference;
    private static DatabaseReference databaseTicketReference;

    private static ArrayList<Ticket> listOfAvailableTickets = new ArrayList<Ticket>();
    private static ArrayList<Ticket> listOfMyClosedTickets = new ArrayList<Ticket>();
    private static ArrayList<Ticket> listOfSolvedTickets = new ArrayList<Ticket>();
    private static TicketRecyclerAdapter adapter;
    private static TicketRecyclerAdapter adapter1;
    private static TicketRecyclerAdapter adapter2;

    private static ArrayList<User> usersList = new ArrayList<User>();
    private static FragmentManager fragmentManager;

    private ViewPager viewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ImageView currUserImage;

    private static String mUserId;
    private static String mNickname;

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
                if (ticket.getAdminId().equals(mUserId))
                    listOfMyClosedTickets.add(ticket);

            adapter = new TicketRecyclerAdapter(ListOfTicketsActivity.this, listOfAvailableTickets, usersList, getSupportFragmentManager());
            viewOfAvailableTickets.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            adapter1 = new TicketRecyclerAdapter(ListOfTicketsActivity.this, listOfMyClosedTickets, usersList, getSupportFragmentManager());
            viewOfMyClosedTickets.setAdapter(adapter1);
            adapter1.notifyDataSetChanged();

            adapter2 = new TicketRecyclerAdapter(ListOfTicketsActivity.this, listOfSolvedTickets, usersList, getSupportFragmentManager());
            viewOfSolvedTickets.setAdapter(adapter2);
            adapter2.notifyDataSetChanged();
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

        context = ListOfTicketsActivity.this;

        mUserId = getIntent().getExtras().getString("uuid");
        mNickname = getIntent().getExtras().getString("nickname");

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

        fragmentManager = getSupportFragmentManager();
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

        currUserImage.setImageBitmap(Globals.ImageMethods.getClip(Globals.ImageMethods.createUserImage(mNickname, ListOfTicketsActivity.this)));

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        userName.setText(mNickname);
        Menu nav_menu = navigationView.getMenu();

        int role = Globals.currentUser.getRole();

        if (role == User.ADMINISTRATOR) {
            userType.setText("Администратор");
            nav_menu.findItem(R.id.charts).setVisible(false);
        }
        else if (role == User.DEPARTMENT_CHIEF) {
            userType.setText("Начальник отдела");
            nav_menu.findItem(R.id.signUpUser).setVisible(false);
        }
        else if (role == User.DEPARTMENT_MEMBER){
            userType.setText("Работник отдела");
            nav_menu.findItem(R.id.signUpUser).setVisible(false);
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

        if (id == R.id.listOfChannels) {
            finish();
        } else if (id == R.id.signUpUser) {
            Intent intent = new Intent(ListOfTicketsActivity.this, UserActionsActivity.class);
            intent.putExtra("uuid", mUserId);
            intent.putExtra("nickname", mNickname);
            startActivity(intent);
            finish();
        } else if (id == R.id.charts) {
            Intent intent = new Intent(ListOfTicketsActivity.this, ChartsActivity.class);
            intent.putExtra("uuid", mUserId);
            intent.putExtra("nickname", mNickname);
            startActivity(intent);
            finish();
        } else if (id == R.id.settings) {
            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
        } else if (id == R.id.listOfTickets) {

        }else if (id == R.id.about) {
            Globals.showAbout(ListOfTicketsActivity.this);
            return true;
        } else if (id == R.id.exit) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);

            builder.setPositiveButton("Закрыть приложение", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (PreferenceManager.getDefaultSharedPreferences(ListOfTicketsActivity.this).getBoolean("allowNotifications", false)) {
                        MessagingService.stopMessagingService(getApplicationContext());
                        Log.e("СЛУЖБА", "ОСТАНОВЛЕНА");
                    }
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

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return FirstFragment.newInstance();
            else if (position == 1)
                return SecondFragment.newInstance();
            else
                return ThirdFragment.newInstance();
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Доступные";
                case 1:
                    return "Решенные мной";
                case 2:
                    return "Решенные";
            }
            return null;
        }
    }

    public static class FirstFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_recycler, container, false);
            viewOfAvailableTickets = (RecyclerView) v.findViewById(R.id.recycler);

            adapter = new TicketRecyclerAdapter(context, listOfAvailableTickets, usersList, fragmentManager);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

            viewOfAvailableTickets.setLayoutManager(mLayoutManager);
            viewOfAvailableTickets.setHasFixedSize(false);
            viewOfAvailableTickets.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            ItemClickSupport.addTo(viewOfAvailableTickets).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, final int position, View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    builder.setTitle("Принять заявку");
                    builder.setMessage("Вы действительно хотите принять заявку?");

                    builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            listOfAvailableTickets.get(position).addAdmin(mUserId, mNickname);

                            adapter = new TicketRecyclerAdapter(context, listOfAvailableTickets, usersList, fragmentManager);
                            viewOfAvailableTickets.setAdapter(adapter);
                            adapter.notifyDataSetChanged();

                            databaseTicketReference.child(DatabaseVariables.ExceptFolder.Tickets.DATABASE_MARKED_TICKET_TABLE).child(listOfAvailableTickets.get(position).getTicketId()).setValue(listOfAvailableTickets.get(position));
                            databaseTicketReference.child(DatabaseVariables.ExceptFolder.Tickets.DATABASE_UNMARKED_TICKET_TABLE).child(listOfAvailableTickets.get(position).getTicketId()).removeValue();
                        }
                    });

                    builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            });


            return v;
        }

        public static FirstFragment newInstance() {
            FirstFragment f = new FirstFragment();
            return f;
        }
    }

    public static class SecondFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_recycler, container, false);
            viewOfMyClosedTickets = (RecyclerView)v.findViewById(R.id.recycler);

            adapter1 = new TicketRecyclerAdapter(context, listOfMyClosedTickets, usersList, fragmentManager);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

            viewOfMyClosedTickets.setLayoutManager(mLayoutManager);
            viewOfMyClosedTickets.setHasFixedSize(false);
            viewOfMyClosedTickets.setAdapter(adapter1);
            adapter1.notifyDataSetChanged();

            return v;
        }

        public static SecondFragment newInstance() {
            SecondFragment f = new SecondFragment();
            return f;
        }
    }

    public static class ThirdFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_recycler, container, false);
            viewOfSolvedTickets = (RecyclerView)v.findViewById(R.id.recycler);

            adapter2 = new TicketRecyclerAdapter(context, listOfSolvedTickets, usersList, fragmentManager);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

            viewOfSolvedTickets.setLayoutManager(mLayoutManager);
            viewOfSolvedTickets.setHasFixedSize(false);
            viewOfSolvedTickets.setAdapter(adapter2);
            adapter2.notifyDataSetChanged();

            return v;
        }

        public static ThirdFragment newInstance() {
            ThirdFragment f = new ThirdFragment();
            return f;
        }
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
