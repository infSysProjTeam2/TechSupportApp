package com.techsupportapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.techsupportapp.R;
import com.techsupportapp.databaseClasses.ChatMessage;
import com.techsupportapp.utility.Globals;

import java.util.ArrayList;
import java.util.List;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.ViewHolder> {

    private static final int TYPE_LEFT = 0;
    private static final int TYPE_RIGHT = 1;

    private Query mRef;
    private Context context;
    private List<ChatMessage> chatMessages;
    private List<String> keys;
    private ChildEventListener childEventListener;

    public ChatRecyclerAdapter(final Query mRef, final Context context) {
        this.mRef = mRef;
        this.context = context;
        chatMessages = new ArrayList<>();
        keys = new ArrayList<>();

        childEventListener = this.mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                String key = dataSnapshot.getKey();

                if ((chatMessage.isUnread() && !(chatMessage.getUserId().equals(Globals.currentUser.getLogin()))))
                    mRef.getRef().child(key).child("unread").setValue(false);

                if (previousChildName == null) {
                    chatMessages.add(0, chatMessage);
                    keys.add(0, key);
                } else {
                    int previousIndex = keys.indexOf(previousChildName);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == chatMessages.size()) {
                        chatMessages.add(chatMessage);
                        keys.add(key);
                    } else {
                        chatMessages.add(nextIndex, chatMessage);
                        keys.add(nextIndex, key);
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                ChatMessage newChatMessage = dataSnapshot.getValue(ChatMessage.class);
                int index = keys.indexOf(key);

                chatMessages.set(index, newChatMessage);

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                int index = keys.indexOf(key);

                keys.remove(index);
                chatMessages.remove(index);

                notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                String key = dataSnapshot.getKey();
                ChatMessage newChatMessage = dataSnapshot.getValue(ChatMessage.class);
                int index = keys.indexOf(key);
                chatMessages.remove(index);
                keys.remove(index);
                if (previousChildName == null) {
                    chatMessages.add(0, newChatMessage);
                    keys.add(0, key);
                } else {
                    int previousIndex = keys.indexOf(previousChildName);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == chatMessages.size()) {
                        chatMessages.add(newChatMessage);
                        keys.add(key);
                    } else {
                        chatMessages.add(nextIndex, newChatMessage);
                        keys.add(nextIndex, key);
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ChatRecyclerAdapter", "Listen was cancelled, no more updates will occur");
            }

        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView authorText;
        private TextView messageText;
        private TextView messageTime;
        private TextView unread;
        private ImageView userImage;

        private ViewHolder(View view) {
            super(view);
            authorText = (TextView) view.findViewById(R.id.messageAuthor);
            messageText = (TextView) view.findViewById(R.id.messageText);
            messageTime = (TextView) view.findViewById(R.id.messageTime);
            unread = (TextView) view.findViewById(R.id.unread);
            userImage = (ImageView) view.findViewById(R.id.userImage);
        }
    }

    public void cleanup() {
        mRef.removeEventListener(childEventListener);
        chatMessages.clear();
        keys.clear();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_LEFT)
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left, parent, false));
        else
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        try {
            holder.authorText.setText(chatMessage.getAuthor() + ": ");
            holder.messageText.setText(chatMessage.getMessage());
            holder.messageTime.setText(chatMessage.getMessageTime());

            if (chatMessage.isUnread())
                holder.unread.setText("Непрочит.");
            else
                holder.unread.setText("");

            holder.userImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO что-то сделать
                }
            });
            holder.userImage.setImageBitmap(Globals.ImageMethods.getClip(Globals.ImageMethods.createUserImage(chatMessage.getAuthor(), context)));
        }
        catch (Exception e) {
            Globals.showLongTimeToast(context, e.getMessage() + "Обратитесь к разработчику");
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemViewType(int position)
    {
        if (!(chatMessages.get(position).getUserId().equals(Globals.currentUser.getLogin())))
            return TYPE_LEFT;
        else
            return TYPE_RIGHT;
    }
}
