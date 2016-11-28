package com.techsupportapp.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
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
    private ArrayList<User> values;

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView userImage;
        private TextView userNameText;
        private TextView userPermission;

        private ViewHolder(View view) {
            super(view);
            userImage = (ImageView) view.findViewById(R.id.userImage);
            userNameText = (TextView) view.findViewById(R.id.userName);
            userPermission = (TextView) view.findViewById(R.id.userPermissions);
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
        try {
            holder.userNameText.setText(values.get(position).getUserName());
            holder.userPermission.setText(getStringRole(values.get(position)));
            holder.userImage.setImageDrawable(Globals.ImageMethods.getRoundImage(context, values.get(position).getUserName()));
        }
        catch (Exception e) {
            Globals.showLongTimeToast(context, e.getMessage() + "Обратитесь к разработчику");
        }
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    @NonNull
    private String getStringRole(User user) throws Exception {
        int role = user.getRole();
        if (role == User.SIMPLE_USER)
            return "Пользователь";
        else if (role == User.DEPARTMENT_MEMBER)
            return "Специалист";
        else if (role == User.DEPARTMENT_CHIEF)
            return "Начальник отдела";
        else if (role == User.MANAGER)
            return "Диспетчер";
        else throw new Exception("Передана нулевая ссылка или неверно указаны права пользователя.");
    }
}