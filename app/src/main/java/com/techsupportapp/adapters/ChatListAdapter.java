package com.techsupportapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Query;
import com.techsupportapp.R;
import com.techsupportapp.databaseClasses.ChatMessage;
import com.techsupportapp.utility.Globals;

public class ChatListAdapter extends FirebaseListAdapter<ChatMessage> {

    private Context context;

    public ChatListAdapter(Query ref, Activity activity) {
        super(ref, ChatMessage.class, activity);
        this.context = activity.getApplicationContext();
    }

    @Override
    protected void populateView(View view, final ChatMessage chat) {
        final String author = chat.getAuthor();

        TextView authorText = (TextView) view.findViewById(R.id.messageAuthor);
        TextView messageText = (TextView) view.findViewById(R.id.messageText);
        TextView messageTime = (TextView) view.findViewById(R.id.messageTime);
        TextView unread = (TextView) view.findViewById(R.id.unread);
        ImageView userImage = (ImageView) view.findViewById(R.id.userImage);

        authorText.setText(author + ": ");
        messageText.setText(chat.getMessage());
        messageTime.setText(chat.getMessageTime());
        if (!chat.isUnread())
            unread.setText("");
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO что-то сделать
            }
        });
        userImage.setImageBitmap(Globals.ImageMethods.getClip(Globals.ImageMethods.createUserImage(author,context)));
    }
}
