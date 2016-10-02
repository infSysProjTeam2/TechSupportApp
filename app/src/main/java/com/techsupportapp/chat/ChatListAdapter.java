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

        TextView authorText = (TextView) view.findViewById(R.id.messageAuthor);
        TextView messageText = (TextView) view.findViewById(R.id.messageText);
        TextView messageTime = (TextView) view.findViewById(R.id.messageTime);
        ImageView userImage = (ImageView) view.findViewById(R.id.userImage);

        authorText.setText(author + ": ");
        messageText.setText(chat.getMessage());
        messageTime.setText(chat.getMessageTime());
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("userId", chat.getUserId());
                intent.putExtra("currUserId", GlobalsMethods.currUserId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        userImage.setImageBitmap(GlobalsMethods.ImageMethods.getclip(GlobalsMethods.ImageMethods.createUserImage(author,context)));
    }
}
