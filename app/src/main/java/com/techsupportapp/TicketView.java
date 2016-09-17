package com.techsupportapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.techsupportapp.databaseClasses.Ticket;

public class TicketView extends RelativeLayout{ //TODO заготовка - ждать

    //region Composite Components

    private TextView authorText;
    private TextView dateText;
    private TextView topicText;
    private TextView descText;

    //endregion

    public TicketView(Context context) {
        super(context);
        initializeComponent();
    }

    public TicketView(Context context, Ticket source) {
        super(context);
        initializeComponent();
        setValues(source);
    }

    public TicketView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeComponent();
    }

    public TicketView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeComponent();
    }

    private void initializeComponent() {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.item_ticket, this);

        authorText = (TextView)findViewById(R.id.ticketAuthor);
        dateText = (TextView)findViewById(R.id.ticketDate);
        topicText = (TextView)findViewById(R.id.ticketTopic);
        descText = (TextView)findViewById(R.id.ticketDesc);
    }

    public void setValues(Ticket source){
        this.authorText.setText(source.userId);
        this.topicText.setText(source.topic);
        this.descText.setText(source.message);
        this.dateText.setText(source.date);
    }

}
