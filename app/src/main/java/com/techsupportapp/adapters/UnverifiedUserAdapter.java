package com.techsupportapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.techsupportapp.R;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.GlobalsMethods;

import java.util.ArrayList;

public class UnverifiedUserAdapter extends ArrayAdapter<User> {

    private final Context context;
    private final ArrayList<User> values;

    public UnverifiedUserAdapter(Context context, ArrayList<User> values) {
        super(context, R.layout.item_ticket, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_user_unverified, parent, false);
            holder = new ViewHolder();
            holder.userImage = (ImageView) convertView.findViewById(R.id.userImage);
            holder.userNameText = (TextView) convertView.findViewById(R.id.userName);
            holder.userDateText = (TextView) convertView.findViewById(R.id.userDate);
            convertView.setTag(holder);
        } else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.userNameText.setText(values.get(position).getUserName());
        holder.userDateText.setText(values.get(position).getRegistrationDate());
        holder.userImage.setImageBitmap(GlobalsMethods.ImageMethods.createUserImage(values.get(position).getLogin(), context));

        return convertView;
    }

    static class ViewHolder {
        private ImageView userImage;
        private TextView userNameText;
        private TextView userDateText;
    }
}
