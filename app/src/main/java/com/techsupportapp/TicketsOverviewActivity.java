package com.techsupportapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
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
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;
import com.techsupportapp.utility.ItemClickSupport;

import java.util.ArrayList;

public class TicketsOverviewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView ticketsOverview;
    private LinearLayoutManager mLayoutManager;

    private String mUserId;
    private String mNickname;
    private int role;

    private DatabaseReference databaseRef;
    private ArrayList<Ticket> ticketsOverviewList = new ArrayList<Ticket>();
    private ArrayList<User> usersList = new ArrayList<User>();

    private TicketRecyclerAdapter adapter;

    private ImageView currUserImage;

    private View bottomSheetBehaviorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets_overview);

        mUserId = Globals.currentUser.getLogin();
        mNickname = Globals.currentUser.getUserName();
        role = Globals.currentUser.getRole();

        initializeComponents();
        setEvents();
    }

    private void initializeComponents(){
        ticketsOverview = (RecyclerView)findViewById(R.id.ticketsOverview);

        bottomSheetBehaviorView = findViewById(R.id.bottom_sheet);

        mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);

        databaseRef = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mNickname);
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

        currUserImage.setImageBitmap(Globals.ImageMethods.getclip(Globals.ImageMethods.createUserImage(mNickname, TicketsOverviewActivity.this)));

        Menu nav_menu = navigationView.getMenu();
        userName.setText(mNickname);
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
                usersList = Globals.Downloads.getVerifiedUserList(dataSnapshot);

                if (role != User.SIMPLE_USER) {
                    ticketsOverviewList = Globals.Downloads.getOverseerTicketList(dataSnapshot, mUserId);
                    adapter = new TicketRecyclerAdapter(getApplicationContext(), ticketsOverviewList, usersList, getSupportFragmentManager());
                    ticketsOverview.setLayoutManager(mLayoutManager);
                    ticketsOverview.setHasFixedSize(false);
                    ticketsOverview.setAdapter(adapter);
                } else {
                    ticketsOverviewList = Globals.Downloads.getUserSpecificTickets(dataSnapshot, DatabaseVariables.Tickets.DATABASE_MARKED_TICKET_TABLE, mUserId);
                    ticketsOverviewList.addAll(Globals.Downloads.getUserSpecificTickets(dataSnapshot, DatabaseVariables.Tickets.DATABASE_UNMARKED_TICKET_TABLE, mUserId));
                    adapter = new TicketRecyclerAdapter(getApplicationContext(), ticketsOverviewList, usersList, getSupportFragmentManager());
                    ticketsOverview.setLayoutManager(mLayoutManager);
                    ticketsOverview.setHasFixedSize(false);
                    ticketsOverview.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ItemClickSupport.addTo(ticketsOverview).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Intent intent = new Intent(TicketsOverviewActivity.this, MessagingActivity.class);
                        if (role != User.SIMPLE_USER) {
                            intent.putExtra("userName", ticketsOverviewList.get(position).getUserName());
                            intent.putExtra("chatRoom", ticketsOverviewList.get(position).getTicketId());
                        }
                        else {
                            if (ticketsOverviewList.get(position).getAdminId() == null || ticketsOverviewList.get(position).getAdminId().equals("")) {
                                Toast.makeText(getApplicationContext(), "Администратор еще не просматривал ваше сообщение, подождите", Toast.LENGTH_LONG).show();
                                return;
                            }
                            else {
                                intent.putExtra("userName", ticketsOverviewList.get(position).getAdminName());
                                intent.putExtra("chatRoom", ticketsOverviewList.get(position).getTicketId());
                            }
                        }
                        startActivity(intent);
                    }
                });

        ItemClickSupport.addTo(ticketsOverview).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener () {
                    @Override
                    public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                        if (role == User.DEPARTMENT_CHIEF) {
                            final Ticket selectedTicket = ticketsOverviewList.get(position);
                            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(TicketsOverviewActivity.this);

                            builder.setTitle("Отказ от заявки пользователя " + selectedTicket.getUserName());
                            builder.setMessage("Вы действительно хотите отказаться от данной заявки?");

                            builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    selectedTicket.removeAdmin();
                                    databaseRef.child(DatabaseVariables.Tickets.DATABASE_UNMARKED_TICKET_TABLE).child(selectedTicket.getTicketId()).setValue(selectedTicket);
                                    databaseRef.child(DatabaseVariables.Tickets.DATABASE_MARKED_TICKET_TABLE).child(selectedTicket.getTicketId()).removeValue();
                                }
                            });

                            builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            builder.setCancelable(false);
                            builder.show();
                        }
                        else if (role == User.SIMPLE_USER){
                            final Ticket selectedTicket = ticketsOverviewList.get(position);
                            if (selectedTicket.getAdminId() == null || selectedTicket.getAdminId().equals("")) {
                                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(TicketsOverviewActivity.this);

                                builder.setTitle("Отзыв заявки " + selectedTicket.getTicketId() + " от " + selectedTicket.getCreateDate());
                                builder.setMessage("Вы действительно хотите отозвать данную заявку?");

                                builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        databaseRef.child(DatabaseVariables.Tickets.DATABASE_UNMARKED_TICKET_TABLE).child(selectedTicket.getTicketId()).removeValue();
                                    }
                                });

                                builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.setCancelable(false);
                                builder.show();
                            }
                            else {
                                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(TicketsOverviewActivity.this);

                                builder.setTitle("Подтверждение решения проблемы по заявке " + selectedTicket.getTicketId() + " от " + selectedTicket.getCreateDate());
                                builder.setMessage("Ваша проблема действительно была решена?");

                                builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        databaseRef.child(DatabaseVariables.Tickets.DATABASE_SOLVED_TICKET_TABLE).child(selectedTicket.getTicketId()).setValue(selectedTicket);
                                        databaseRef.child(DatabaseVariables.Tickets.DATABASE_MARKED_TICKET_TABLE).child(selectedTicket.getTicketId()).removeValue();
                                    }
                                });

                                builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.setCancelable(false);
                                builder.show();
                            }
                        }
                        return true;
                    }
        });

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
        else
            super.onBackPressed();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.listOfTickets) {
            if (role != User.SIMPLE_USER) {
                Intent intent = new Intent(TicketsOverviewActivity.this, ListOfTicketsActivity.class);
                intent.putExtra("uuid", mUserId);
                intent.putExtra("nickname", mNickname);
                startActivity(intent);
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
        } else if (id == R.id.settings) {
            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
        } else if (id == R.id.about) {
            Globals.showAbout(TicketsOverviewActivity.this);
            return true;
        } else if (id == R.id.exit) {
            new MaterialDialog.Builder(this)
                    .title("Закрыть приложение")
                    .content("Вы действительно хотите закрыть приложение?")
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            exit();
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

    private void exit(){
        this.finishAffinity();
    }
}
