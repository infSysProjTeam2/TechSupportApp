package com.techsupportapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.techsupportapp.R;
import com.techsupportapp.UserProfileActivity;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.GlobalsMethods;

import java.util.ArrayList;

public class TicketRecyclerAdapter extends RecyclerView.Adapter<TicketRecyclerAdapter.ViewHolder>{

    private Context context;
    private final ArrayList<Ticket> values;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView authorText;
        public TextView dateText;
        public TextView topicText;
        public TextView descText;
        public ImageView ticketImage;

        public ViewHolder(View view) {
            super(view);
            ticketImage = (ImageView) view.findViewById(R.id.ticketImage);
            authorText = (TextView) view.findViewById(R.id.ticketAuthor);
            dateText = (TextView) view.findViewById(R.id.ticketDate);
            topicText = (TextView) view.findViewById(R.id.ticketTopic);
            descText = (TextView) view.findViewById(R.id.ticketDesc);
        }
    }

    public TicketRecyclerAdapter(Context context, ArrayList<Ticket> values) {
        this.context = context;
        this.values = values;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final String titleText;
        final String userId;
        final String adminId;

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
    }

    @Override
    public int getItemCount() {
        return values.size();
    }
}