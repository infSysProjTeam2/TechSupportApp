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
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.GlobalsMethods;

import java.util.ArrayList;

public class UserAdapter extends ArrayAdapter<User> {

    private final Context context;
    private final ArrayList<User> values;

    public UserAdapter(Context context, ArrayList<User> values) {
        super(context, R.layout.item_ticket, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_user, parent, false);
        ImageView userImage = (ImageView) rowView.findViewById(R.id.userImage);
        final TextView userNameText = (TextView)rowView.findViewById(R.id.userName);

        userNameText.setText(values.get(position).getLogin());
        userImage.setImageBitmap(GlobalsMethods.ImageMethods.createUserImage(values.get(position).getLogin(), context));

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("userId", userNameText.getText().toString());
                intent.putExtra("currUserId", GlobalsMethods.currUserId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        return rowView;
        //TODO сделать показ роли ListView
    }
}
