package com.techsupportapp.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.techsupportapp.R;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.Globals;

import java.util.ArrayList;

public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.ViewHolder> {

    private Context context;
    private String userId;
    private final ArrayList<User> values;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView userImage;
        public TextView userNameText;

        public ViewHolder(View view) {
            super(view);
            userImage = (ImageView) view.findViewById(R.id.userImage);
            userNameText = (TextView) view.findViewById(R.id.userName);
        }
    }


    public UserRecyclerAdapter(Context context, ArrayList<User> values) {
        this.context = context;
        this.values = values;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        userId = values.get(position).getLogin();

        holder.userNameText.setText(values.get(position).getUserName());
        holder.userImage.setImageBitmap(Globals.ImageMethods.createUserImage(values.get(position).getUserName(), context));
    }

    @Override
    public int getItemCount() {
        return values.size();
    }
}