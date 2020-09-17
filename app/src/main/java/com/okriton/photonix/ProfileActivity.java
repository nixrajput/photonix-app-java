package com.okriton.photonix;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private DatabaseReference friendRequestRef;
    private DatabaseReference friendsRef;
    private DatabaseReference notificationRef;

    private String receiver_user_id;
    private String sender_user_id;
    private String user_name;
    private String CURRENT_STATE;
    private String saveCurrentDate, saveCurrentTime;

    private CircleImageView profileImage;
    private TextView profileName, profileGender, profileDOB, profileRegDate;
    private Button sendActionBtn, cancelActionBtn;

    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mToolbar = findViewById(R.id.profile_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        receiver_user_id = getIntent().getStringExtra("user_id");
        user_name = getIntent().getStringExtra("user_name");

        getSupportActionBar().setTitle(user_name);

        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        friendRequestRef.keepSynced(true);

        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        friendsRef.keepSynced(true);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.keepSynced(true);

        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        notificationRef.keepSynced(true);

        profileImage = findViewById(R.id.profile_user_image);
        profileName = findViewById(R.id.profile_user_name);
        profileDOB = findViewById(R.id.profile_user_dob_date);
        profileRegDate = findViewById(R.id.profile_user_joined_date);
        profileGender = findViewById(R.id.profile_user_gender_value);
        sendActionBtn = findViewById(R.id.profile_send_btn);
        cancelActionBtn = findViewById(R.id.profile_cancel_btn);

        CURRENT_STATE = "not_friends";

        final Calendar calendarDate = Calendar.getInstance();
        java.text.SimpleDateFormat currentDate = new java.text.SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calendarDate.getTime());

        java.text.SimpleDateFormat currentTime = new java.text.SimpleDateFormat("hh:mm aa");
        saveCurrentTime = currentTime.format(calendarDate.getTime());

        // INITIALISATION END

        userRef.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @SuppressLint("CheckResult")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    name = dataSnapshot.child("name").getValue().toString();
                    final String image = dataSnapshot.child("image").getValue().toString();
                    String reg_date = dataSnapshot.child("reg_date").getValue().toString();
                    String gender = dataSnapshot.child("gender").getValue().toString();
                    String dob = dataSnapshot.child("dob").getValue().toString();

                    profileName.setText(name);
                    profileRegDate.setText(reg_date);
                    profileDOB.setText(dob);
                    profileGender.setText(gender);

                    RequestOptions placeholderRequest = new RequestOptions();
                    placeholderRequest.placeholder(R.drawable.user_view);

                    Glide.with(getApplicationContext()).setDefaultRequestOptions(placeholderRequest).load(image).into(profileImage);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent imageIntent = new Intent(ProfileActivity.this, ViewImageActivity.class);
                imageIntent.putExtra("user_id", receiver_user_id);
                startActivity(imageIntent);

            }
        });

        cancelActionBtn.setEnabled(false);

        // FRIEND REQUEST HANDLER

        friendRequestRef.child(sender_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(receiver_user_id)) {

                    String req_type = dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();

                    if (req_type.equals("sent")) {

                        CURRENT_STATE = "request_sent";
                        sendActionBtn.setText("Request Sent");
                        sendActionBtn.setEnabled(false);

                        cancelActionBtn.setVisibility(View.VISIBLE);
                        cancelActionBtn.setEnabled(true);

                    }

                    else if (req_type.equals("received")){

                        CURRENT_STATE = "request_received";
                        sendActionBtn.setText("Accept");

                        cancelActionBtn.setVisibility(View.VISIBLE);
                        cancelActionBtn.setEnabled(true);

                    }
                }

                else {

                    friendsRef.child(sender_user_id)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(receiver_user_id)){

                                        CURRENT_STATE = "friends";
                                        sendActionBtn.setText("Unfriend");

                                        cancelActionBtn.setVisibility(View.VISIBLE);
                                        cancelActionBtn.setEnabled(true);
                                        cancelActionBtn.setText("Send Message");

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // SEND BUTTON METHOD

        if (sender_user_id.equals(receiver_user_id)){

            sendActionBtn.setText("Edit Profile");
            sendActionBtn.setEnabled(true);

            cancelActionBtn.setText("More");
            cancelActionBtn.setEnabled(true);

            sendActionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent editIntent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                    editIntent.putExtra("user_id", sender_user_id);
                    startActivity(editIntent);

                }
            });

            cancelActionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {



                }
            });

        }

        if (!sender_user_id.equals(receiver_user_id)){

            cancelActionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    cancelActionBtn.setEnabled(false);

                    if (CURRENT_STATE.equals("request_received")){

                        declineFriendRequest();

                    }

                    if (CURRENT_STATE.equals("request_sent")){

                        cancelFriedRequest();

                    }

                    if (CURRENT_STATE.equals("friends")){

                        Intent chatIntent = new Intent(ProfileActivity.this, ChatActivity.class);
                        chatIntent.putExtra("user_id", receiver_user_id);
                        chatIntent.putExtra("user_name", name);
                        startActivity(chatIntent);

                    }


                }
            });

            sendActionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    sendActionBtn.setEnabled(false);

                    if (CURRENT_STATE.equals("not_friends")){

                        sendFriendRequestToUser();

                    }

                    if (CURRENT_STATE.equals("request_received")){

                        acceptFriendRequest();

                    }

                    if (CURRENT_STATE.equals("friends")){

                        unfriendTheFriend();

                    }

                }
            });

        }
    }

    // UNFRIEND MEHTOD

    private void unfriendTheFriend() {

        friendsRef.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            friendsRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                Toast.makeText(ProfileActivity.this, "Unfriend Successfully",
                                                        Toast.LENGTH_LONG).show();

                                                sendActionBtn.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendActionBtn.setText("Send Request");

                                                cancelActionBtn.setVisibility(View.VISIBLE);
                                                cancelActionBtn.setEnabled(false);
                                                cancelActionBtn.setText("Cancel Request");

                                            }

                                        }
                                    });

                        }

                    }
                });

    }

    // ACCEPTING FRIEND REQUEST METHOD

    private void acceptFriendRequest() {

        final Map<String, Object> friendsMap = new HashMap<>();
        friendsMap.put("date", saveCurrentDate);
        friendsMap.put("time", saveCurrentTime);

        friendsRef.child(sender_user_id).child(receiver_user_id).setValue(friendsMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        friendsRef.child(receiver_user_id).child(sender_user_id).setValue(friendsMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        friendRequestRef.child(sender_user_id).child(receiver_user_id).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()){

                                                            friendRequestRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if (task.isSuccessful()){

                                                                                Toast.makeText(ProfileActivity.this, "Friend Request Accepted",
                                                                                        Toast.LENGTH_LONG).show();

                                                                                sendActionBtn.setEnabled(true);
                                                                                CURRENT_STATE = "friends";
                                                                                sendActionBtn.setText("Unfriend");

                                                                                cancelActionBtn.setVisibility(View.VISIBLE);
                                                                                cancelActionBtn.setEnabled(true);
                                                                                cancelActionBtn.setText("Send Message");

                                                                            }

                                                                        }
                                                                    });

                                                        }

                                                    }
                                                });

                                    }
                                });

                    }
                });

    }

    // REMOVING FRIEND REQUEST METHOD

    private void cancelFriedRequest() {

        friendRequestRef.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            friendRequestRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                Toast.makeText(ProfileActivity.this, "Friend Request Cancelled",
                                                        Toast.LENGTH_LONG).show();

                                                sendActionBtn.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendActionBtn.setText("Send Request");

                                                cancelActionBtn.setVisibility(View.VISIBLE);
                                                cancelActionBtn.setEnabled(false);

                                            }

                                        }
                                    });

                        }

                    }
                });

    }

    // SENDING REQUEST METHOD

    private void sendFriendRequestToUser() {

        Map<String, Object> reqSentMap = new HashMap<>();
        reqSentMap.put("date", saveCurrentDate);
        reqSentMap.put("time", saveCurrentTime);
        reqSentMap.put("request_type", "sent");

        friendRequestRef.child(sender_user_id).child(receiver_user_id).setValue(reqSentMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            Map<String, Object> reqReceivedMap = new HashMap<>();
                            reqReceivedMap.put("date", saveCurrentDate);
                            reqReceivedMap.put("time", saveCurrentTime);
                            reqReceivedMap.put("request_type", "received");

                            friendRequestRef.child(receiver_user_id).child(sender_user_id)
                                    .setValue(reqReceivedMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        HashMap<String, String> notificationData = new HashMap<>();
                                        notificationData.put("from", sender_user_id);
                                        notificationData.put("type", "request");

                                        notificationRef.child(receiver_user_id).push().setValue(notificationData)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()){

                                                            Toast.makeText(ProfileActivity.this, "Friend Request Sent",
                                                                    Toast.LENGTH_LONG).show();

                                                            sendActionBtn.setEnabled(true);
                                                            CURRENT_STATE = "request_sent";
                                                            sendActionBtn.setText("Request Sent");

                                                            cancelActionBtn.setVisibility(View.VISIBLE);
                                                            cancelActionBtn.setEnabled(true);

                                                        }

                                                    }
                                                });

                                    }

                                }
                            });

                        }

                    }
                });

    }

    // DECLINE REQUEST METHOD

    private void declineFriendRequest() {

        friendRequestRef.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            friendRequestRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                Toast.makeText(ProfileActivity.this, "Friend Request Removed",
                                                        Toast.LENGTH_LONG).show();

                                                sendActionBtn.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendActionBtn.setText("Send Request");

                                                cancelActionBtn.setVisibility(View.VISIBLE);
                                                cancelActionBtn.setEnabled(false);

                                            }

                                        }
                                    });

                        }

                    }
                });

    }
}
