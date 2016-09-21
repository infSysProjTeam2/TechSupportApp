package com.techsupportapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.techsupportapp.R;
import com.techsupportapp.UserProfileActivity;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.utility.GlobalsMethods;

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
        final String userId;
        final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_ticket, parent, false);
        ImageView ticketImage = (ImageView) rowView.findViewById(R.id.ticketImage);
        final TextView authorText = (TextView)rowView.findViewById(R.id.ticketAuthor);
        TextView dateText = (TextView)rowView.findViewById(R.id.ticketDate);
        TextView topicText = (TextView)rowView.findViewById(R.id.ticketTopic);
        TextView descText = (TextView)rowView.findViewById(R.id.ticketDesc);

        userId = values.get(position).userId;
        if (values.get(position).adminId == null || values.get(position).adminId.equals(""))
            authorText.setText(values.get(position).userId);
        else authorText.setText(values.get(position).userId + " ✔");

        dateText.setText(values.get(position).date);
        topicText.setText(values.get(position).topic);
        descText.setText(values.get(position).message);
        ticketImage.setImageBitmap(GlobalsMethods.ImageMethods.createUserImage(values.get(position).userId, context));

        ticketImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("currUserId", GlobalsMethods.currUserId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        return rowView;
    }
}
