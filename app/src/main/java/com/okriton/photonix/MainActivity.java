package com.okriton.photonix;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import static com.okriton.photonix.MyApplication.MESSAGE_CHANNEL;

public class MainActivity extends AppCompatActivity{

    private Toolbar mToolbar;

    private BottomNavigationView mBottomNav;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDataRef, friendRequestRef;

    private String current_user_id;

    private NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        userDataRef = FirebaseDatabase.getInstance().getReference().child("Users");

        notificationManager = NotificationManagerCompat.from(this);

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Photonix");
        // getSupportActionBar().setIcon(R.mipmap.ic_launcher_foreground);

        /*getSupportActionBar().setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.main_custom_bar, null);

        getSupportActionBar().setCustomView(action_bar_view);

        titleView = findViewById(R.id.main_custom_bar_title);
        titleView.setText("Photonix");*/

        if (firebaseAuth.getCurrentUser() != null){

            mBottomNav = findViewById(R.id.main_nav_bar);
            FloatingActionButton addPostBtn = findViewById(R.id.add_post_btn);

            current_user_id = firebaseAuth.getCurrentUser().getUid();

            friendRequestRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()){

                        Intent activityIntent = new Intent(MainActivity.this, RequestsActivity.class);

                        PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 0, activityIntent, 0);

                            Notification notification = new Notification.Builder(MainActivity.this, MESSAGE_CHANNEL)
                                    .setSmallIcon(R.drawable.ic_action_name)
                                    .setContentTitle("Friend Request")
                                    .setContentText("You have a new friend request")
                                    .setPriority(Notification.PRIORITY_DEFAULT)
                                    .setCategory(Notification.CATEGORY_MESSAGE)
                                    .setAutoCancel(true)
                                    .setBadgeIconType(Notification.BADGE_ICON_SMALL)
                                    .setContentIntent(contentIntent)
                                    .build();

                            notificationManager.notify(1, notification);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            setupBottomNavigation();

            if (savedInstanceState == null){

                loadHomeFragment();

            }

            addPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent newPostIntent = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(newPostIntent);

                }
            });

        }

    }

    private void setupBottomNavigation() {

        mBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {

                    case R.id.nav_home:
                        loadHomeFragment();
                        return true;

                    case R.id.nav_chat:
                        loadChatFragment();
                        return true;

                    case R.id.nav_notif:
                        loadNotificationFragment();
                        return true;

                    case R.id.nav_account:
                        loadAccountFragment();
                        return true;

                    default:
                        return false;

                }
            }
        });

    }

    private void loadAccountFragment() {

        AccountFragment fragment = AccountFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_frame, fragment);
        ft.commit();

    }

    private void loadNotificationFragment() {

        NotificationFragment fragment = NotificationFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_frame, fragment);
        ft.commit();

    }

    private void loadChatFragment() {

        ChatFragment fragment = ChatFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_frame, fragment);
        ft.commit();

    }

    private void loadHomeFragment() {

        HomeFragment fragment = HomeFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_frame, fragment);
        ft.commit();

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null){

            sendToStartPage();

        } else {

            userDataRef.child(current_user_id).child("online").setValue("true");
            userDataRef.child(current_user_id).child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);

            userDataRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (!dataSnapshot.hasChild("name")) {

                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        startActivity(setupIntent);
                        finish();

                    } else if (!dataSnapshot.hasChild("username")){

                        Intent usernameIntent = new Intent(MainActivity.this, UsernameActivity.class);
                        startActivity(usernameIntent);
                        finish();

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser != null) {

            userDataRef.child(current_user_id).child("online").setValue(ServerValue.TIMESTAMP);

        }

    }

    private void sendToStartPage() {

        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startIntent);
        finish();

    }
}
