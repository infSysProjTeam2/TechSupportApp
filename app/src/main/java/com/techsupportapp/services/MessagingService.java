package com.techsupportapp.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.R;
import com.techsupportapp.SignInActivity;
import com.techsupportapp.databaseClasses.ChatMessage;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.Globals;

import java.util.ArrayList;

public class MessagingService extends Service {
    //TODO сделать связку с количеством сообщений в каждом чате(возможно не требуется сервиса)
    DatabaseReference databaseRef;
    ChildEventListener childEventListener;
    private ArrayList<String> ticketIDList;
    private long messagesCount = 0;

    public MessagingService() {
    }

    public static void startMessagingService(Context context){
        Intent intent = new Intent(context, MessagingService.class);
        intent.addCategory("MessagingService");
        context.startService(intent);
    }

    public static void stopMessagingService(Context context){
        Intent intent = new Intent(context, MessagingService.class);
        intent.addCategory("MessagingService");
        context.stopService(intent);
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
                if (!dataSnapshot.getValue(ChatMessage.class).getUserId().equals(Globals.currentUser.getLogin()) && dataSnapshot.getValue(ChatMessage.class).isUnread()) {
                    messagesCount++;//TODO сделать нормальный счетчик
                    showNotification();
                }
                //TODO для администратора
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

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (Globals.currentUser.getRole() == User.SIMPLE_USER) {
                    ticketIDList = Globals.Downloads.Strings.getUserMarkedTicketIDs(dataSnapshot, Globals.currentUser.getLogin());
                    for (int i = 0; i < ticketIDList.size(); i++)
                        databaseRef.child("chat").child(ticketIDList.get(i)).addChildEventListener(childEventListener);
                }
                //TODO для администратора
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        if (Globals.currentUser.getRole() == User.SIMPLE_USER)
            for (int i = 0; i < ticketIDList.size(); i++)
                databaseRef.child("chat").child(ticketIDList.get(i)).removeEventListener(childEventListener);
    }

    private void showNotification(){
        Intent intent = new Intent(this, SignInActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification.Builder builder = new Notification.Builder(this)
                .setTicker("Новые сообщения")
                .setContentTitle("Новые сообщения")
                .setContentText("Количество новых сообщений " + messagesCount)
                .setSmallIcon(R.mipmap.icon)
                .addAction(R.mipmap.ic_launcher, "Просмотр", pIntent);;

        Notification notification = new Notification.InboxStyle(builder)
                .addLine("Количество новых сообщений " + messagesCount)
                .setSummaryText("+2 more").build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(3, notification);
    }
}
