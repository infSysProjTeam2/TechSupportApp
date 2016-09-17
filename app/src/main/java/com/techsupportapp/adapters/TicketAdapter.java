package com.techsupportapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.techsupportapp.R;
import com.techsupportapp.databaseClasses.Ticket;

import java.util.ArrayList;

public class TicketAdapter extends ArrayAdapter<Ticket> {

    private final Context context;
    private final ArrayList<Ticket> values;

    public TicketAdapter(Context context, ArrayList<Ticket> values) {
        super(context, R.layout.item_ticket, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_ticket, parent, false);
        TextView authorText = (TextView)rowView.findViewById(R.id.ticketAuthor);
        TextView dateText = (TextView)rowView.findViewById(R.id.ticketDate);
        TextView topicText = (TextView)rowView.findViewById(R.id.ticketTopic);
        TextView descText = (TextView)rowView.findViewById(R.id.ticketDesc);
        if (values.get(position).adminId == null || values.get(position).adminId.equals(""))
            authorText.setText(values.get(position).userId);
        else authorText.setText(values.get(position).userId + " ✔");
        dateText.setText(values.get(position).date);
        topicText.setText(values.get(position).topic);
        descText.setText(values.get(position).message);
        return rowView;
    }

}
