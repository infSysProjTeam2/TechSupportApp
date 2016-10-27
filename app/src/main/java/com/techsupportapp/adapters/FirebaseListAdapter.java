package com.techsupportapp.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.techsupportapp.R;
import com.techsupportapp.databaseClasses.ChatMessage;
import com.techsupportapp.utility.Globals;

import java.util.ArrayList;
import java.util.List;

public abstract class FirebaseListAdapter<T> extends BaseAdapter {

    private static final int TYPE_LEFT = 0;
    private static final int TYPE_RIGHT = 1;

    private Query mRef;
    private Class<T> mModelClass;
    private LayoutInflater mInflater;
    private List<T> mModels;
    private List<String> mKeys;
    private ChildEventListener mListener;
    private List<ChatMessage> mMessages;

    public FirebaseListAdapter(Query mRef, Class<T> mModelClass, Activity activity) {
        this.mRef = mRef;
        this.mModelClass = mModelClass;
        mInflater = activity.getLayoutInflater();
        mModels = new ArrayList<T>();
        mKeys = new ArrayList<String>();
        mMessages = new ArrayList<ChatMessage>();
        mListener = this.mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                T model = dataSnapshot.getValue(FirebaseListAdapter.this.mModelClass);
                String key = dataSnapshot.getKey();

                if (previousChildName == null) {
                    mModels.add(0, model);
                    mKeys.add(0, key);
                    mMessages.add(0, dataSnapshot.getValue(ChatMessage.class));
                } else {
                    int previousIndex = mKeys.indexOf(previousChildName);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == mModels.size()) {
                        mModels.add(model);
                        mKeys.add(key);
                        mMessages.add(dataSnapshot.getValue(ChatMessage.class));
                    } else {
                        mModels.add(nextIndex, model);
                        mKeys.add(nextIndex, key);
                        mMessages.add(nextIndex, dataSnapshot.getValue(ChatMessage.class));
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                T newModel = dataSnapshot.getValue(FirebaseListAdapter.this.mModelClass);
                int index = mKeys.indexOf(key);

                mModels.set(index, newModel);
                mMessages.add(index, dataSnapshot.getValue(ChatMessage.class));

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                String key = dataSnapshot.getKey();
                int index = mKeys.indexOf(key);

                mKeys.remove(index);
                mModels.remove(index);
                mMessages.remove(index);

                notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

                String key = dataSnapshot.getKey();
                T newModel = dataSnapshot.getValue(FirebaseListAdapter.this.mModelClass);
                int index = mKeys.indexOf(key);
                mModels.remove(index);
                mKeys.remove(index);
                mMessages.remove(index);
                if (previousChildName == null) {
                    mModels.add(0, newModel);
                    mKeys.add(0, key);
                    mMessages.add(0, dataSnapshot.getValue(ChatMessage.class));
                } else {
                    int previousIndex = mKeys.indexOf(previousChildName);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == mModels.size()) {
                        mModels.add(newModel);
                        mKeys.add(key);
                        mMessages.add(dataSnapshot.getValue(ChatMessage.class));
                    } else {
                        mModels.add(nextIndex, newModel);
                        mKeys.add(nextIndex, key);
                        mMessages.add(nextIndex, dataSnapshot.getValue(ChatMessage.class));
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("FirebaseListAdapter", "Listen was cancelled, no more updates will occur");
            }

        });
    }

    public void cleanup() {
        mRef.removeEventListener(mListener);
        mModels.clear();
        mKeys.clear();
        mMessages.clear();
    }

    @Override
    public int getCount() {
        return mModels.size();
    }

    @Override
    public Object getItem(int i) {
        return mModels.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        int type = getItemViewType(i);
        View v = view;

        switch (type) {
            case TYPE_LEFT:
                v = mInflater.inflate(R.layout.item_message_left, null);
                break;
            case TYPE_RIGHT:
                v = mInflater.inflate(R.layout.item_message_right, null);
                break;
            default:
        }

        T model = mModels.get(i);
        populateView(v, model);
        return v;
    }

    @Override
    public int getItemViewType(int position)
    {
        if (!(mMessages.get(position).getUserId().equals(Globals.currentUser.getLogin())))
            return TYPE_LEFT;
        else
            return TYPE_RIGHT;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    protected abstract void populateView(View v, T model);
}
