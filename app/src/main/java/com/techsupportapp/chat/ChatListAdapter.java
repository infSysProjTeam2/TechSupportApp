package com.techsupportapp.chat;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Query;
import com.techsupportapp.R;
import com.techsupportapp.UserProfileActivity;
import com.techsupportapp.utility.GlobalsMethods;

public class ChatListAdapter extends FirebaseListAdapter<Chat> {

    private String mUsername;
    private Context context;

    public ChatListAdapter(Query ref, Activity activity, String mUsername) {
        super(ref, Chat.class, activity);
        this.mUsername = mUsername;
        this.context = activity.getApplicationContext();
    }

    @Override
    protected void populateView(View view, final Chat chat) {
        final String author = chat.getAuthor();
        ViewHolder holder;

        if (view == null) {
            holder = new ViewHolder();
            holder.authorText = (TextView) view.findViewById(R.id.messageAuthor);
            holder.messageText = (TextView) view.findViewById(R.id.messageText);
            holder.messageTime = (TextView) view.findViewById(R.id.messageTime);
            holder.userImage = (ImageView) view.findViewById(R.id.userImage);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.authorText.setText(author + ": ");
        holder.messageText.setText(chat.getMessage());
        holder.messageTime.setText(chat.getMessageTime());
        holder.userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("userId", chat.getUserId());
                intent.putExtra("currUserId", GlobalsMethods.currUserId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        holder.userImage.setImageBitmap(GlobalsMethods.ImageMethods.getclip(GlobalsMethods.ImageMethods.createUserImage(author,context)));
    }

    static class ViewHolder {
        private TextView authorText;
        private TextView messageText;
        private TextView messageTime;
        private ImageView userImage;
    }
}
