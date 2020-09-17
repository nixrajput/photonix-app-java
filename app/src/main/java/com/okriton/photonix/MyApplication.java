package com.okriton.photonix;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyApplication extends Application {

    public static final String MESSAGE_CHANNEL = "Your Messages";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        createNotificationChannel();

    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel messageChannel = new NotificationChannel(
                    MESSAGE_CHANNEL,
                    "Messages",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            messageChannel.setDescription("Notification for messages");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(messageChannel);

        }

    }



}
