package com.techsupportapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.techsupportapp.R;
import com.techsupportapp.UserProfileActivity;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.User;
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
        final String titleText;
        final String userId;
        final String adminId;
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_ticket, parent, false);
            holder = new ViewHolder();
            holder.ticketImage = (ImageView) convertView.findViewById(R.id.ticketImage);
            holder.authorText = (TextView) convertView.findViewById(R.id.ticketAuthor);
            holder.dateText = (TextView) convertView.findViewById(R.id.ticketDate);
            holder.topicText = (TextView) convertView.findViewById(R.id.ticketTopic);
            holder.descText = (TextView) convertView.findViewById(R.id.ticketDesc);
            convertView.setTag(holder);
        } else{
            holder = (ViewHolder) convertView.getTag();
        }

        userId = values.get(position).getUserId();
        adminId = values.get(position).getAdminId();

        if (values.get(position).getAdminId() == null || values.get(position).getAdminId().equals("")) {
            if (GlobalsMethods.isCurrentAdmin == User.ADMINISTRATOR)
                holder.authorText.setText(values.get(position).getUserName());
            else
                holder.authorText.setText("Не установлено");

            titleText = holder.authorText.getText().toString();
        }
        else {
            if (GlobalsMethods.isCurrentAdmin == User.ADMINISTRATOR) {
                holder.authorText.setText(values.get(position).getUserName() + " ✔");
                titleText = values.get(position).getUserName();
            }
            else {
                holder.authorText.setText(values.get(position).getAdminName() + " ✔");
                titleText = values.get(position).getAdminName();
            }
        }

        holder.dateText.setText(values.get(position).getCreateDate());
        holder.topicText.setText(values.get(position).getTopic());
        holder.descText.setText(values.get(position).getMessage());
        holder.ticketImage.setImageBitmap(GlobalsMethods.ImageMethods.createUserImage(titleText, context));

        holder.ticketImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean fl = true;
                Intent intent = new Intent(context, UserProfileActivity.class);
                if (GlobalsMethods.isCurrentAdmin == User.ADMINISTRATOR) {
                    intent.putExtra("userId", userId);
                }
                else
                    if (titleText.equals("Не установлено"))
                        fl = false;
                    else
                        intent.putExtra("userId", adminId);

                intent.putExtra("currUserId", GlobalsMethods.currUserId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (fl)
                    context.startActivity(intent);
            }
        });
        return convertView;
    }

    static class ViewHolder {
        private TextView authorText;
        private TextView dateText;
        private TextView topicText;
        private TextView descText;
        private ImageView ticketImage;
    }
}
