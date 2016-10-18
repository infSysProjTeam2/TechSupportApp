package com.techsupportapp;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ChartsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private String mUserId;
    private String mNickname;

    private ImageView currUserImage;

    private TextView firstDateTV;
    private TextView lastDateTV;

    private DatabaseReference databaseReference;

    private ArrayList<Ticket> allTickets = new ArrayList<Ticket>();

    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        mUserId = getIntent().getExtras().getString("uuid");
        mNickname = getIntent().getExtras().getString("nickname");

        initializeComponents();
        setEvents();
    }

    private void initializeComponents(){
        initChart();

        firstDateTV = (TextView)findViewById(R.id.firstDateLabel);
        lastDateTV = (TextView)findViewById(R.id.lastDateLabel);
        databaseReference = FirebaseDatabase.getInstance().getReference();

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

        currUserImage.setImageBitmap(Globals.ImageMethods.getclip(Globals.ImageMethods.createUserImage(mNickname, ChartsActivity.this)));

        Menu nav_menu = navigationView.getMenu();
        userName.setText(mNickname);
        userType.setText("Начальник отдела");
        nav_menu.findItem(R.id.signUpUser).setVisible(false);
    }

    private void initChart(){
        pieChart = (PieChart)findViewById(R.id.chart);

        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);

        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        //pieChart.animateXY(1000, 1000, Easing.EasingOption.EaseOutCirc, Easing.EasingOption.EaseOutCirc);

        pieChart.setDescription("");

        pieChart.setDrawEntryLabels(false);

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setTextSize(15f);
        legend.setDrawInside(false);
        legend.setXEntrySpace(7f);
        legend.setYEntrySpace(0f);
        legend.setYOffset(0f);
    }

    private ArrayList<Integer> getColors(){
        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());
        return colors;
    }

    private boolean compareDates(TextView dateSource, Calendar newDate) {
        try {
            if (dateSource.getId() == R.id.firstDateLabel){
                Calendar anotherDate = Calendar.getInstance();
                anotherDate.setTime(new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).parse(lastDateTV.getText().toString()));
                return newDate.before(anotherDate);
            } else {
                Calendar anotherDate = Calendar.getInstance();
                anotherDate.setTime(new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).parse(firstDateTV.getText().toString()));
                return newDate.after(anotherDate);
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
        firstDate.setTime(firstDate.getTime() - 86400000);
        lastDate.setTime(lastDate.getTime() + 86400000);
        for (int i = 0; i < allTickets.size(); i++) {
            Date ticketDate = stringToDate(allTickets.get(i).getCreateDate());
            if (ticketDate.before(lastDate) && ticketDate.after(firstDate)) {
                if (allTickets.get(i).getTicketState() == Ticket.SOLVED)
                    solvedTicketsCount++;
                else if (allTickets.get(i).getAdminId() != "" && allTickets.get(i).getAdminId() != null)
                    markedTicketsCount++;
                allTicketsCount++;
            }
        }
        initChartData(allTicketsCount, markedTicketsCount, solvedTicketsCount);
    }

    private String dateToString(Date date) {
        return new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(date);
    }

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
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        entries.add(new PieEntry(allTicketsCount, "Создано"));
        entries.add(new PieEntry(markedTicketsCount, "Принято"));
        entries.add(new PieEntry(solvedTicketsCount, "Решено"));

        PieDataSet dataSet = new PieDataSet(entries, "Заявки");

        dataSet.setColors(getColors());

        PieData data = new PieData(dataSet);

        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(16f);

        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void showDatePicker(final TextView dateSource){
        final Calendar date = Calendar.getInstance();
        date.setTime(stringToDate(dateSource.getText().toString()));
        DatePickerDialog datePickerDialog = new DatePickerDialog(ChartsActivity.this, new DatePickerDialog.OnDateSetListener() {
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
        datePickerDialog.setCancelable(false);
        datePickerDialog.show();
    }

    private void setEvents(){
        currUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(ChartsActivity.this, EditUserProfileActivity.class);
                intent.putExtra("userId", mUserId);
                intent.putExtra("currUserId", Globals.currentUser.getLogin());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
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

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                firstDateTV.setText(dataSnapshot.child(DatabaseVariables.Indexes.DATABASE_FIRST_DATE_INDEX).getValue(String.class));
                lastDateTV.setText(dataSnapshot.child(DatabaseVariables.Indexes.DATABASE_LAST_DATE_INDEX).getValue(String.class));

                if (firstDateTV.getText() == "" || lastDateTV.getText() == ""){
                    firstDateTV.setText(dateToString(Calendar.getInstance().getTime()));
                    lastDateTV.setText(dateToString(Calendar.getInstance().getTime()));
                }

                long markedTicketsCount = dataSnapshot.child(DatabaseVariables.Tickets.DATABASE_MARKED_TICKET_TABLE).getChildrenCount();
                long solvedTicketsCount = dataSnapshot.child(DatabaseVariables.Tickets.DATABASE_SOLVED_TICKET_TABLE).getChildrenCount();
                long allTicketsCount = dataSnapshot.child(DatabaseVariables.Tickets.DATABASE_UNMARKED_TICKET_TABLE).getChildrenCount() + markedTicketsCount + solvedTicketsCount;

                String firstDate = firstDateTV.getText().toString();
                SpannableString contentFirst = new SpannableString(firstDate);
                contentFirst.setSpan(new UnderlineSpan(), 0, firstDate.length(), 0);
                firstDateTV.setText(contentFirst);

                String lastDate = lastDateTV.getText().toString();
                SpannableString contentLast = new SpannableString(lastDate);
                contentLast.setSpan(new UnderlineSpan(), 0, lastDate.length(), 0);
                lastDateTV.setText(contentLast);

                initChartData(allTicketsCount, markedTicketsCount, solvedTicketsCount);
                allTickets = Globals.Downloads.getAllTickets(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.listOfTickets) {
            Intent intent = new Intent(ChartsActivity.this, ListOfTicketsActivity.class);
            intent.putExtra("uuid", mUserId);
            intent.putExtra("nickname", mNickname);
            startActivity(intent);
        } else if (id == R.id.listOfChannels) {
            finish();
        } else if (id == R.id.signUpUser) {
            Intent intent = new Intent(ChartsActivity.this, UserActionsActivity.class);
            intent.putExtra("uuid", mUserId);
            intent.putExtra("nickname", mNickname);
            startActivity(intent);
        } else if (id == R.id.settings) {

        } else if (id == R.id.about) {
            Globals.showAbout(ChartsActivity.this);
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
}
