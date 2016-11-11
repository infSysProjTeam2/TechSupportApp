package com.techsupportapp.adapters;

import android.content.Context;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.techsupportapp.R;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.fragments.BottomSheetFragment;
import com.techsupportapp.utility.Globals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TicketRecyclerAdapter extends RecyclerView.Adapter<TicketRecyclerAdapter.ViewHolder>{

    private Context context;
    private final ArrayList<Ticket> values;
    private final ArrayList<User> users;
    private FragmentManager fragmentManager;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView authorText;
        TextView dateText;
        TextView topicText;
        TextView descText;
        ImageView ticketImage;

        public ViewHolder(View view) {
            super(view);
            ticketImage = (ImageView) view.findViewById(R.id.ticketImage);
            authorText = (TextView) view.findViewById(R.id.ticketAuthor);
            dateText = (TextView) view.findViewById(R.id.ticketDate);
            topicText = (TextView) view.findViewById(R.id.ticketTopic);
            descText = (TextView) view.findViewById(R.id.ticketDesc);
        }
    }

    public TicketRecyclerAdapter(Context context, ArrayList<Ticket> values, ArrayList<User> users, FragmentManager fragmentManager) {
        this.context = context;
        this.values = values;
        this.users = users;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final String titleText;

        final String userId = values.get(position).getUserId();
        final String adminId = values.get(position).getAdminId();

        if (Globals.currentUser.getRole() != User.SIMPLE_USER){
            holder.authorText.setText(values.get(position).getUserName());
            titleText = values.get(position).getUserName();
        }
        else if (adminId == null || adminId.equals("")) {
            holder.authorText.setText("Не установлено");
            titleText = holder.authorText.getText().toString();
        } else {
            holder.authorText.setText(values.get(position).getAdminName());
            titleText = values.get(position).getAdminName();
        }

        holder.dateText.setText(values.get(position).getCreateDate());
        holder.topicText.setText(values.get(position).getTopic());
        holder.descText.setText(values.get(position).getMessage());
        holder.ticketImage.setImageBitmap(Globals.ImageMethods.createUserImage(titleText, context));

        holder.ticketImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!titleText.equals("Не установлено")) {
                    BottomSheetDialogFragment bottomSheetDialogFragment = BottomSheetFragment.newInstance(userId, adminId, getUser(userId, adminId));
                    bottomSheetDialogFragment.show(fragmentManager, bottomSheetDialogFragment.getTag());
                }
            }
        });
    }

    private User getUser(String userId, String adminId) {
        String find;
        find = userId;

        if (Globals.currentUser.getLogin().equals(userId))
            find = adminId;

        ArrayList<String> idList = new ArrayList<>();
        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                return lhs.getLogin().compareTo(rhs.getLogin());
            }
        });

        for (int i = 0; i < users.size(); i++)
            idList.add(users.get(i).getLogin());
        int index = Collections.binarySearch(idList, find, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });

        return users.get(index);
    }

    @Override
    public int getItemCount() {
        return values.size();
    }
}