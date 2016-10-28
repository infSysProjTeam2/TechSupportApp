package com.techsupportapp.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.techsupportapp.R;
import com.techsupportapp.SignInActivity;

public class MessagingService extends Service {

    DatabaseReference databaseRef;
    ChildEventListener childEventListener;

    public MessagingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate()
    {
        databaseRef = FirebaseDatabase.getInstance().getReference();
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                showNotification();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                showNotification();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        databaseRef.addChildEventListener(childEventListener);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        databaseRef.removeEventListener(childEventListener);
    }

    private void showNotification(){
        Intent intent = new Intent(this, SignInActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification.Builder builder = new Notification.Builder(this)
                .setTicker("Новые сообщения")
                .setContentTitle("Новые сообщения")
                .setContentText("Новые сообщения")
                .setSmallIcon(R.mipmap.icon)
                .addAction(R.mipmap.ic_launcher, "Просмотр", pIntent);;

        Notification notification = new Notification.InboxStyle(builder)
                .addLine("Первое сообщение")
                .addLine("Второе сообщение")
                .addLine("Третье сообщение")
                .addLine("Четвертое сообщение")
                .setSummaryText("+2 more").build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(3, notification);
    }
}
