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

public class UnverifiedUserRecyclerAdapter extends RecyclerView.Adapter<UnverifiedUserRecyclerAdapter.ViewHolder> {

    private Context context;
    private final ArrayList<User> values;


    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView userImage;
        private TextView userNameText;
        private TextView userDateText;

        private ViewHolder(View view) {
            super(view);
            userImage = (ImageView) view.findViewById(R.id.userImage);
            userNameText = (TextView) view.findViewById(R.id.userName);
            userDateText = (TextView) view.findViewById(R.id.userDate);
        }
    }


    public UnverifiedUserRecyclerAdapter(Context context, ArrayList<User> values) {
        this.context = context;
        this.values = values;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_unverified, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.userNameText.setText(values.get(position).getUserName());
        holder.userDateText.setText(values.get(position).getRegistrationDate());
        holder.userImage.setImageDrawable(Globals.ImageMethods.getRoundImage(context, values.get(position).getUserName()));
    }

    @Override
    public int getItemCount() {
        return values.size();
    }
}