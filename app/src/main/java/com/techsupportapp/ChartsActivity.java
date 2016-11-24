package com.techsupportapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
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
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.fragments.BottomSheetFragment;
import com.techsupportapp.services.MessagingService;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ChartsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private int oneDayMilliSec = 86400000;

    private ImageView currUserImage;

    private TextView firstDateTV;
    private TextView lastDateTV;

    private DatabaseReference databaseReference;
    private DatabaseReference firstDateReference;
    private DatabaseReference lastDateReference;


    private ArrayList<Ticket> allTickets = new ArrayList<Ticket>();

    private HorizontalBarChart horizontalBarChart;

    private boolean isDownloaded;

    //region Listeners

    ValueEventListener firstDateListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            //TODO пофиксить текст, переезжающий на другую строку
            Globals.logInfoAPK(ChartsActivity.this, "Обновление первой даты - НАЧАТО");
            firstDateTV.setText(dataSnapshot.getValue(String.class));

            if (firstDateTV.getText().equals(""))
                firstDateTV.setText(dateToString(Calendar.getInstance().getTime()));

            firstDateTV.getText().toString();
            Globals.logInfoAPK(ChartsActivity.this, "Обновление первой даты - ЗАВЕРШЕНО");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    ValueEventListener lastDateListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Globals.logInfoAPK(ChartsActivity.this, "Обновление последней даты - НАЧАТО");
            lastDateTV.setText(dataSnapshot.getValue(String.class));

            if (lastDateTV.getText().equals(""))
                lastDateTV.setText(dateToString(Calendar.getInstance().getTime()));

            lastDateTV.getText().toString();
            Globals.logInfoAPK(ChartsActivity.this, "Обновление последней даты - ЗАКОНЧЕНО");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    ValueEventListener ticketListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Globals.logInfoAPK(ChartsActivity.this, "Обновление графика - НАЧАТО");
            while (firstDateTV.getText().equals("") || lastDateTV.getText().equals(""))
                isDownloaded = true;
            if (isDownloaded) {
                Globals.logInfoAPK(ChartsActivity.this, "Прослушиватели - УДАЛЕНЫ");
                firstDateReference.removeEventListener(firstDateListener);
                lastDateReference.removeEventListener(lastDateListener);
                isDownloaded = false;
            }
            //TODO рассчет при обновлении для уже выбранной даты - при обновлении даты не обновляются, а тикеты обновляются
            long markedTicketsCount = dataSnapshot.child(DatabaseVariables.ExceptFolder.Tickets.DATABASE_MARKED_TICKET_TABLE).getChildrenCount();
            long solvedTicketsCount = dataSnapshot.child(DatabaseVariables.ExceptFolder.Tickets.DATABASE_SOLVED_TICKET_TABLE).getChildrenCount();
            long allTicketsCount = dataSnapshot.child(DatabaseVariables.ExceptFolder.Tickets.DATABASE_UNMARKED_TICKET_TABLE).getChildrenCount();

            initChartData(allTicketsCount, markedTicketsCount, solvedTicketsCount);
            allTickets = Globals.Downloads.Tickets.getAllTickets(dataSnapshot);
            Globals.logInfoAPK(ChartsActivity.this, "Обновление графика - ЗАКОНЧЕНО");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);
        initializeComponents();
        setEvents();
    }

    private void initializeComponents(){
        initChart();

        isDownloaded = false;

        firstDateTV = (TextView)findViewById(R.id.firstDateLabel);
        lastDateTV = (TextView)findViewById(R.id.lastDateLabel);
        firstDateReference = FirebaseDatabase.getInstance().getReference(DatabaseVariables.FullPath.Indexes.DATABASE_FIRST_DATE_INDEX);
        lastDateReference = FirebaseDatabase.getInstance().getReference(DatabaseVariables.FullPath.Indexes.DATABASE_LAST_DATE_INDEX);
        databaseReference = FirebaseDatabase.getInstance().getReference(DatabaseVariables.FullPath.Tickets.DATABASE_ALL_TICKET_TABLE);

        firstDateTV.setPaintFlags(firstDateTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        lastDateTV.setPaintFlags(lastDateTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Статистика");
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

        currUserImage.setImageBitmap(Globals.ImageMethods.getClip(Globals.ImageMethods.createUserImage(Globals.currentUser.getUserName(), ChartsActivity.this)));

        Menu nav_menu = navigationView.getMenu();
        userName.setText(Globals.currentUser.getUserName());
        userType.setText("Начальник отдела");
        nav_menu.findItem(R.id.userActions).setVisible(false);

        initChartData(0, 0, 0);
    }

    private void initChart(){
        horizontalBarChart = (HorizontalBarChart) findViewById(R.id.chart);

        horizontalBarChart.setDescription("");

        horizontalBarChart.setDrawBarShadow(false);
        horizontalBarChart.setDrawGridBackground(false);
        horizontalBarChart.setDrawValueAboveBar(true);
        horizontalBarChart.setScaleEnabled(false);

        horizontalBarChart.setVerticalScrollBarEnabled(false);
        horizontalBarChart.setHorizontalScrollBarEnabled(false);

        XAxis xl = horizontalBarChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawAxisLine(true);
        xl.setDrawLabels(false);
        xl.setTypeface(Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf"));
        xl.setDrawGridLines(false);
        xl.setGranularity(10f);

        YAxis yl = horizontalBarChart.getAxisLeft();
        yl.setDrawAxisLine(true);
        yl.setTypeface(Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf"));
        yl.setDrawGridLines(true);
        yl.setAxisMinValue(0f);

        YAxis yr = horizontalBarChart.getAxisRight();
        yr.setDrawAxisLine(true);
        yr.setTypeface(Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf"));
        yr.setDrawGridLines(false);
        yr.setAxisMinValue(0f);

        Legend l = horizontalBarChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setTextSize(18f);
        l.setFormSize(16f);
        l.setXEntrySpace(4f);
        l.setYEntrySpace(0f);
        l.setYOffset(25);

        horizontalBarChart.setFitBars(true);
        horizontalBarChart.animateXY(1500, 1500);
    }


    private boolean compareDates(TextView dateSource, Calendar newDate) {
        try {
            if (dateSource.getId() == R.id.firstDateLabel){
                Calendar anotherDate = Calendar.getInstance();
                anotherDate.setTime(new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).parse(lastDateTV.getText().toString()));
                return newDate.before(anotherDate);//TODO проверка заявок за один день
            } else {
                Calendar anotherDate = Calendar.getInstance();
                anotherDate.setTime(new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).parse(firstDateTV.getText().toString()));
                return newDate.after(anotherDate);//TODO проверка заявок за один день
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setChartValues(TextView dateSource){
        Date firstDate;
        Date lastDate;
        long allTicketsCount = 0;
        long markedTicketsCount = 0;
        long solvedTicketsCount = 0;
        if (dateSource.getId() == R.id.firstDateLabel){
            firstDate = stringToDate(dateSource.getText().toString());
            lastDate = stringToDate(lastDateTV.getText().toString());
        } else {
            firstDate = stringToDate(firstDateTV.getText().toString());
            lastDate = stringToDate(dateSource.getText().toString());
        }
        firstDate.setTime(firstDate.getTime() - oneDayMilliSec);
        lastDate.setTime(lastDate.getTime() + oneDayMilliSec);
        for (int i = 0; i < allTickets.size(); i++) {
            Date ticketDate = stringToDate(allTickets.get(i).getCreateDate());
            if (ticketDate.before(lastDate) && ticketDate.after(firstDate)) {
                if (allTickets.get(i).getTicketState() == Ticket.SOLVED)
                    solvedTicketsCount++;
                else if (allTickets.get(i).getTicketState() == Ticket.NOT_ACCEPTED)
                    allTicketsCount++;
                else if (allTickets.get(i).getTicketState() == Ticket.ACCEPTED)
                    markedTicketsCount++;
            }
        }
        initChartData(allTicketsCount, markedTicketsCount, solvedTicketsCount);
    }

    private String dateToString(Date date) {
        return new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(date);
    }

    @Nullable
    private Date stringToDate(String dateStr) {
        try {
            return new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).parse(dateStr);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initChartData(long allTicketsCount, long markedTicketsCount, long solvedTicketsCount){
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(2 * 20f, allTicketsCount));
        entries.add(new BarEntry(1 * 20f, markedTicketsCount));
        entries.add(new BarEntry(0, solvedTicketsCount));

        BarDataSet dataSet = new BarDataSet(entries, "Не решено, принято, решено");

        dataSet.setColors(ColorTemplate.PASTEL_COLORS);

        BarData data = new BarData(dataSet);
        data.setValueTextColor(Color.BLUE);
        data.setValueTextSize(16f);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return String.valueOf((int) value);
            }
        });
        data.setValueTypeface(Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf"));
        data.setBarWidth(20f);
        data.setHighlightEnabled(false);

        horizontalBarChart.setData(data);
        horizontalBarChart.invalidate();
    }

    private void showDatePicker(final TextView dateSource){
        final Calendar date = Calendar.getInstance();
        date.setTime(stringToDate(dateSource.getText().toString()));
        DatePickerDialog datePickerDialog = new DatePickerDialog(ChartsActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date.set(year, monthOfYear, dayOfMonth);
                if (compareDates(dateSource, date)) {
                    dateSource.setText(dateToString(date.getTime()));
                    setChartValues(dateSource);
                }
                else Globals.showLongTimeToast(getApplicationContext(), "Начальная дата должна предшествовать конечной");
            }
        }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setCancelable(true);
        datePickerDialog.show();
    }

    private void setEvents(){
        currUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialogFragment bottomSheetDialogFragment = BottomSheetFragment.newInstance(Globals.currentUser.getLogin(), Globals.currentUser.getLogin(), Globals.currentUser);
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });

        firstDateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(firstDateTV);
            }
        });

        lastDateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(lastDateTV);
            }
        });

        firstDateReference.addValueEventListener(firstDateListener);

        lastDateReference.addValueEventListener(lastDateListener);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.listOfTickets) {
            Intent intent = new Intent(ChartsActivity.this, ScheduleOfTicketsActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.acceptedTickets) {
            finish();
        } else if (id == R.id.userActions) {
            Intent intent = new Intent(ChartsActivity.this, UserActionsActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.settings) {
            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
        } else if (id == R.id.about) {
            Globals.showAbout(ChartsActivity.this);
            return true;
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
                            if (PreferenceManager.getDefaultSharedPreferences(ChartsActivity.this).getBoolean("allowNotifications", false)) {
                                MessagingService.stopMessagingService(getApplicationContext());
                                Globals.logInfoAPK(ChartsActivity.this, "Служба - ОСТАНОВЛЕНА");
                            }
                            ChartsActivity.this.finishAffinity();
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

    private void exit(){
        this.finishAffinity();
    }

    @Override
    protected void onResume() {
        databaseReference.addValueEventListener(ticketListener);
        Globals.logInfoAPK(ChartsActivity.this, "onResume - ВЫПОЛНЕН");
        super.onResume();
    }

    @Override
    protected void onPause() {
        databaseReference.removeEventListener(ticketListener);
        Globals.logInfoAPK(ChartsActivity.this, "onPause - ВЫПОЛНЕН");
        super.onPause();
    }

    @Override
    protected void onStop() {
        databaseReference.removeEventListener(ticketListener);
        Globals.logInfoAPK(ChartsActivity.this, "onStop - ВЫПОЛНЕН");
        super.onStop();
    }
}
