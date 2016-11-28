package com.techsupportapp;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.fragments.BottomSheetFragment;
import com.techsupportapp.services.MessagingService;
import com.techsupportapp.utility.Globals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TicketTypePickerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private ExpandableListView ticketTypesList;
    private ImageView currUserImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_type_picker);

        initializeComponents();
        setEvents();
    }

    private void initializeComponents(){
        ticketTypesList = (ExpandableListView) findViewById(R.id.ticketTypesList);
        setupListView();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Выбор типа проблемы");
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

        userName.setText(Globals.currentUser.getUserName());
        userType.setText("Пользователь");

        Menu nav_menu = navigationView.getMenu();
        nav_menu.findItem(R.id.userActions).setVisible(false);
        nav_menu.findItem(R.id.charts).setVisible(false);
        nav_menu.findItem(R.id.acceptedTickets).setTitle("Список ваших заявок");
        nav_menu.findItem(R.id.listOfTickets).setTitle("Создать заявку");
    }

    private void setEvents(){
        ticketTypesList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
                int type = -1;
                switch (groupPosition){
                    case 0:
                        type = Ticket.TYPE_SOFTWARE;
                        break;
                    case 1:
                        type = Ticket.TYPE_HARDWARE;
                        break;
                    case 2:
                        type = Ticket.TYPE_EXTERNAL_DEVICES;
                        break;
                    case 3:
                        type = Ticket.TYPE_NETWORK;
                        break;
                }

                String ticketTopic = expandableListView.getExpandableListAdapter().getChild(groupPosition, childPosition).toString();

                Intent intent = new Intent(TicketTypePickerActivity.this, CreateTicketActivity.class);
                intent.putExtra("ticketTopic", ticketTopic.substring(9, ticketTopic.length() - 1));
                intent.putExtra("ticketType", type);
                startActivity(intent);
                return false;
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

    private void setupListView(){
        ArrayList<Map<String, String>> groupData;
        ArrayList<Map<String, String>> childDataItem;
        ArrayList<ArrayList<Map<String, String>>> childData;

        Map<String, String> map;

        String[] groups = getResources().getStringArray(R.array.ticket_types_array);

        final String[] softwareTroubles = getResources().getStringArray(R.array.software_troubles_array);
        final String[] hardwareTroubles = getResources().getStringArray(R.array.hardware_troubles_array);
        final String[] externalDevicesTroubles = getResources().getStringArray(R.array.external_devices_troubles_array);
        final String[] networkTroubles = getResources().getStringArray(R.array.network_troubles_array);

        ArrayList<String[]> troubles = new ArrayList<String[]>(){{
            add(softwareTroubles);
            add(hardwareTroubles);
            add(externalDevicesTroubles);
            add(networkTroubles);
        }};

        groupData = new ArrayList<>();
        for (String group : groups) {
            map = new HashMap<>();
            map.put("type", group);
            groupData.add(map);
        }

        String groupFrom[] = new String[] {"type"};
        int groupTo[] = new int[] {android.R.id.text1};

        childData = new ArrayList<>();

        for (String[] array : troubles) {
            childDataItem = new ArrayList<>();
            for (String trouble : array) {
                map = new HashMap<>();
                map.put("trouble", trouble);
                childDataItem.add(map);
            }
            childData.add(childDataItem);
        }

        String childFrom[] = new String[] {"trouble"};
        int childTo[] = new int[] {android.R.id.text1};

        SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                this,
                groupData,
                android.R.layout.simple_expandable_list_item_1,
                groupFrom,
                groupTo,
                childData,
                R.layout.item_elv_child,
                childFrom,
                childTo);

        ticketTypesList.setAdapter(adapter);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.acceptedTickets) {
            finish();
        } else if (id == R.id.settings) {
            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
        } else if (id == R.id.about) {
            Globals.showAbout(TicketTypePickerActivity.this);
        } else if (id == R.id.logOut) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.exit) {
            new MaterialDialog.Builder(this)
                    .title("Закрыть приложение")
                    .content("Вы действительно хотите закрыть приложение?")
                    .positiveText("Да")
                    .negativeText("Нет")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (PreferenceManager.getDefaultSharedPreferences(TicketTypePickerActivity.this).getBoolean("allowNotifications", false)) {
                                MessagingService.stopMessagingService(getApplicationContext());
                                Globals.logInfoAPK(TicketTypePickerActivity.this, "Служба - ОСТАНОВЛЕНА");
                            }
                            TicketTypePickerActivity.this.finishAffinity();
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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }
}
