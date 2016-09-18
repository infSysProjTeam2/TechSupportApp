package com.techsupportapp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.techsupportapp.R;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.UnverifiedUser;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.GlobalsMethods;

import java.util.ArrayList;

public class UnverifiedUserAdapter extends ArrayAdapter<UnverifiedUser> {

    private final Context context;
    private final ArrayList<UnverifiedUser> values;

    public UnverifiedUserAdapter(Context context, ArrayList<UnverifiedUser> values) {
        super(context, R.layout.item_ticket, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_user, parent, false);
        ImageView userImage = (ImageView) rowView.findViewById(R.id.userImage);
        TextView userNameText = (TextView)rowView.findViewById(R.id.userName);
        TextView userDateText = (TextView)rowView.findViewById(R.id.userDate);

        userNameText.setText(values.get(position).login);
        userDateText.setText(values.get(position).date);
        userImage.setImageBitmap(GlobalsMethods.ImageMethods.createUserImage(values.get(position).login, context));
        return rowView;
    }

}
