package com.techsupportapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.techsupportapp.EditUserProfileActivity;
import com.techsupportapp.R;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.Globals;
import com.techsupportapp.utility.LetterBitmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TicketRecyclerAdapter extends RecyclerView.Adapter<TicketRecyclerAdapter.ViewHolder>{

    private Context context;
    private static String userId;
    private static String adminId;
    private final ArrayList<Ticket> values;
    private final ArrayList<User> users;
    private View bottomSheetBehaviorView;
    private BottomSheetBehavior bottomSheetBehavior;

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

    public TicketRecyclerAdapter(Context context, ArrayList<Ticket> values, ArrayList<User> users, View bottomSheetBehaviorView) {
        this.context = context;
        this.values = values;
        this.users = users;
        this.bottomSheetBehaviorView = bottomSheetBehaviorView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final String titleText;

        userId = values.get(position).getUserId();
        adminId = values.get(position).getAdminId();

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


        if (!titleText.equals("Не установлено"))
            setData();

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetBehaviorView);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        bottomSheetBehaviorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.ticketImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!titleText.equals("Не установлено"))
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    private void setData() {
        String find;
        find = userId;

        if (Globals.currentUser.getLogin().equals(userId))
            find = adminId;

        ImageButton editUser = (ImageButton) bottomSheetBehaviorView.findViewById(R.id.editUserBtn);

        if (Globals.currentUser.getRole() == User.SIMPLE_USER && !Globals.currentUser.getLogin().equals(find))
            editUser.setVisibility(View.GONE);

        ArrayList<String> idList = new ArrayList<String>();
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

        TextView userName = (TextView) bottomSheetBehaviorView.findViewById(R.id.userName);
        final TextView userIdT = (TextView) bottomSheetBehaviorView.findViewById(R.id.userId);
        TextView regDate = (TextView) bottomSheetBehaviorView.findViewById(R.id.regDate);
        TextView workPlace = (TextView) bottomSheetBehaviorView.findViewById(R.id.workPlace);
        TextView accessLevel = (TextView) bottomSheetBehaviorView.findViewById(R.id.accessLevel);

        userName.setText(users.get(index).getUserName());
        userIdT.setText(users.get(index).getLogin());
        regDate.setText(users.get(index).getRegistrationDate());
        workPlace.setText(users.get(index).getWorkPlace());

        int role = users.get(index).getRole();

        if (role == User.SIMPLE_USER)
            accessLevel.setText("Пользователь");
        else if (role == User.DEPARTMENT_MEMBER)
            accessLevel.setText("Работник отдела");
        else if (role == User.ADMINISTRATOR)
            accessLevel.setText("Администратор");
        else if (role == User.DEPARTMENT_CHIEF)
            accessLevel.setText("Начальник отдела");

        editUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(context, EditUserProfileActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("currUserId", Globals.currentUser.getLogin());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return values.size();
    }
}