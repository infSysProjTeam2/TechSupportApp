package com.techsupportapp.utility;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techsupportapp.databaseClasses.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DatabaseStorage {
    public static int ACTION_CREATED = 0;
    public static int ACTION_ACCEPTED = 1;
    public static int ACTION_CLOSED = 2;
    public static int ACTION_SOLVED = 3;
    public static int ACTION_WITHDRAWN = 4;

    private static String result;
    private static boolean finished;

    public static void updateLogFile(Context context, String ticketId, final int action, final User currentUser){
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference("logs").child(ticketId + ".log");

        try {
            final File localFile = File.createTempFile(ticketId, "log");

            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.e("Error", "Successful download");
                    uploadFile(storageReference, localFile, action, currentUser);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("Error", "Error occurred while downloading");
                    uploadFile(storageReference, localFile, action, currentUser);
                }
            });
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void uploadFile(StorageReference storageReference, File file, int action, User currentUser){
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm, MMM dd yyyy", Locale.ENGLISH);
            String currentTime = formatter.format(Calendar.getInstance().getTime());
            String text = "";

            if (action == ACTION_CREATED)
                text = ": заявка создана пользователем ";
            else if (action == ACTION_ACCEPTED)
                text = ": заявка принята пользователем ";
            else if (action == ACTION_CLOSED)
                text = ": заявка закрыта пользователем ";
            else if (action == ACTION_SOLVED)
                text = ": заявка объявлена решенной пользователем ";
            else if (action == ACTION_WITHDRAWN)
                text = ": заявка отозвана пользователем ";

            String data = (currentTime + text + currentUser.getUserName());

            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
            out.println(data + "\r\n");
            out.close();

            storageReference.putFile(Uri.fromFile(file)).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("Error", "Error occurred while uploading");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.e("Error", "Successful upload");
                }
            });
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static String getLogText(String ticketId){
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference("logs").child(ticketId + ".log");
        finished = false;

        try {
            final File localFile = File.createTempFile(ticketId, "log");

            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.e("Error", "Successful download");

                    try {
                        FileInputStream fis = new FileInputStream(localFile);
                        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

                        result = "";
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            result += line;
                            result += "\n";
                        }
                        br.close();
                        finished = true;
                    } catch (FileNotFoundException e){
                        e.printStackTrace();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("Error", "Error occurred while downloading");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        do {
            if (finished)
                break;
        } while (!finished);
        return result;
    }
}
