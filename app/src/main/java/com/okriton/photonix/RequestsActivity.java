package com.okriton.photonix;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private RecyclerView requestList;

    String CURRENT_STATE;
    String current_user_id;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private DatabaseReference friendRequestRef;
    private DatabaseReference friendsRef;
    private DatabaseReference notificationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        mToolbar = findViewById(R.id.request_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Friend Requests");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CURRENT_STATE = "not_friends";

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        friendRequestRef.keepSynced(true);

        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        friendsRef.keepSynced(true);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.keepSynced(true);

        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        notificationRef.keepSynced(true);

        requestList = findViewById(R.id.request_lists);
        requestList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        requestList.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        startListening();
    }

    private void startListening() {

        Query reqQuery = friendRequestRef.child(current_user_id);

        FirebaseRecyclerOptions<Requests> options =
                new FirebaseRecyclerOptions.Builder<Requests>()
                        .setQuery(reqQuery, Requests.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Requests, RequestsActivity.RequestViewHolder>(options) {

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.single_user_item, viewGroup, false);
                return new RequestViewHolder(view);

            }

            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Requests model) {

                holder.setDate(model.getDate());

                final String list_user_id = getRef(position).getKey();

                userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumbImage = dataSnapshot.child("image").getValue().toString();

                        holder.setUserName(userName);
                        holder.setThumbImage(userThumbImage);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                });

                Calendar calendarDate = Calendar.getInstance();
                java.text.SimpleDateFormat currentDate = new java.text.SimpleDateFormat("dd-MMMM-yyyy");
                String saveCurrentDate = currentDate.format(calendarDate.getTime());

                java.text.SimpleDateFormat currentTime = new java.text.SimpleDateFormat("hh:mm aa");
                String saveCurrentTime = currentTime.format(calendarDate.getTime());

                final Map<String, Object> friendsMap = new HashMap<>();
                friendsMap.put("date", saveCurrentDate);
                friendsMap.put("time", saveCurrentTime);

                // FRIEND REQUEST HANDLER

                friendRequestRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(list_user_id)) {

                            String req_type = dataSnapshot.child(list_user_id).child("request_type").getValue().toString();

                            if (req_type.equals("sent")) {

                                CURRENT_STATE = "request_sent";
                                holder.sendReqBtn.setText("Cancel");

                            }

                            else if (req_type.equals("received")){

                                CURRENT_STATE = "request_received";
                                holder.sendReqBtn.setText("Accept");

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                // SEND BUTTON METHOD

                if (!current_user_id.equals(list_user_id)){

                    holder.sendReqBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            holder.sendReqBtn.setEnabled(false);

                            if (CURRENT_STATE.equals("not_friends")){

                                friendRequestRef.child(current_user_id).child(list_user_id).child("request_type").setValue("sent")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){

                                                    friendRequestRef.child(list_user_id).child(current_user_id).child("request_type")
                                                            .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if (task.isSuccessful()){

                                                                HashMap<String, String> notificationData = new HashMap<>();
                                                                notificationData.put("from", current_user_id);
                                                                notificationData.put("type", "request");

                                                                notificationRef.child(list_user_id).push().setValue(notificationData)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful()){

                                                                                    Toast.makeText(RequestsActivity.this, "Friend Request Sent",
                                                                                            Toast.LENGTH_LONG).show();

                                                                                    holder.sendReqBtn.setEnabled(true);
                                                                                    CURRENT_STATE = "request_sent";
                                                                                    holder.sendReqBtn.setText("Cancel");

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

                            if (CURRENT_STATE.equals("request_sent")){

                                friendRequestRef.child(current_user_id).child(list_user_id).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){

                                                    friendRequestRef.child(list_user_id).child(current_user_id
                                                    ).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){

                                                                        Toast.makeText(RequestsActivity.this, "Friend Request Cancelled",
                                                                                Toast.LENGTH_LONG).show();

                                                                        holder.sendReqBtn.setEnabled(true);
                                                                        CURRENT_STATE = "not_friends";
                                                                        holder.sendReqBtn.setText("Send");

                                                                    }

                                                                }
                                                            });

                                                }

                                            }
                                        });

                            }

                            if (CURRENT_STATE.equals("request_received")){

                                friendsRef.child(current_user_id).child(list_user_id).setValue(friendsMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                friendsRef.child(list_user_id).child(current_user_id).setValue(friendsMap)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                friendRequestRef.child(current_user_id).child(list_user_id).removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful()){

                                                                                    friendRequestRef.child(list_user_id).child(current_user_id).removeValue()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                                    if (task.isSuccessful()){

                                                                                                        Toast.makeText(RequestsActivity.this, "Friend Request Accepted",
                                                                                                                Toast.LENGTH_LONG).show();

                                                                                                        holder.sendReqBtn.setEnabled(true);
                                                                                                        CURRENT_STATE = "friends";
                                                                                                        holder.sendReqBtn.setText("Unfriend");

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

                        }
                    });

                } else {

                    holder.sendReqBtn.setVisibility(View.INVISIBLE);

                }

            }
        };

        requestList.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();

    }

    public class RequestViewHolder extends RecyclerView.ViewHolder {

        View mView;

        private Button sendReqBtn;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            sendReqBtn = mView.findViewById(R.id.send_req_btn);
        }

        @SuppressLint("SetTextI18n")
        public void setDate(String date) {

            TextView sinceFriends = mView.findViewById(R.id.single_reg_date);
            sinceFriends.setText("Requested at : \n" + date);

        }

        public void setUserName(String userName) {

            TextView userNameDisplay = mView.findViewById(R.id.single_list_user_name);
            userNameDisplay.setText(userName);

        }

        @SuppressLint("CheckResult")
        public void setThumbImage(final String image) {

            final CircleImageView userImageView = mView.findViewById(R.id.single_list_user_image);

            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.user_view);

            Glide.with(getApplicationContext()).setDefaultRequestOptions(placeholderRequest).load(image).into(userImageView);

        }
    }
}
