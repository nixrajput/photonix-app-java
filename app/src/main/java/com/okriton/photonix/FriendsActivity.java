package com.okriton.photonix;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView friendListView;

    private FirebaseAuth mAuth;
    private DatabaseReference friendsDataRef;
    private DatabaseReference userRef;
    private DatabaseReference friendsRef;
    private String current_user_id;
    private String CURRENT_STATE;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        friendsDataRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_user_id);
        friendsDataRef.keepSynced(true);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.keepSynced(true);

        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        friendsRef.keepSynced(true);

        mToolbar = findViewById(R.id.friends_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        friendListView = findViewById(R.id.friends_list_view);
        friendListView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null){
            
            startListening();
            
        }

    }

    private void startListening() {

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(friendsDataRef, Friends.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Friends, FriendsActivity.FriendsViewHolder>(options){

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.single_user_item, viewGroup, false);
                return new FriendsViewHolder(view);

            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {

                holder.setDate(model.getDate());

                final String list_user_id = getRef(position).getKey();

                userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumbImage = dataSnapshot.child("image").getValue().toString();

                        holder.setUserName(userName);
                        holder.setThumbImage(userThumbImage);

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};

                                final AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);

                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        //Click Event for each item.
                                        if(i == 0){

                                            Intent profileIntent = new Intent(FriendsActivity.this, ProfileActivity.class);
                                            profileIntent.putExtra("user_id", list_user_id);
                                            profileIntent.putExtra("user_name", userName);
                                            startActivity(profileIntent);

                                        }

                                        if(i == 1){

                                            Intent chatIntent = new Intent(FriendsActivity.this, ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_user_id);
                                            chatIntent.putExtra("user_name", userName);
                                            startActivity(chatIntent);

                                        }

                                    }
                                });

                                builder.show();

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                // FRIEND REQUEST HANDLER

                friendsRef.child(current_user_id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.hasChild(list_user_id)){

                                    CURRENT_STATE = "friends";
                                    holder.sendReqBtn.setText("Unfriend");

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

                            if (CURRENT_STATE.equals("friends")){

                                friendsRef.child(current_user_id).child(list_user_id).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){

                                                    friendsRef.child(list_user_id).child(current_user_id).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){

                                                                        Toast.makeText(FriendsActivity.this, "Unfriend Successfully",
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

                        }
                    });

                } else {

                    holder.sendReqBtn.setVisibility(View.INVISIBLE);

                }

            }
        };

        friendListView.setAdapter(adapter);
        adapter.startListening();
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        private Button sendReqBtn;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            sendReqBtn = mView.findViewById(R.id.send_req_btn);
        }

        @SuppressLint("SetTextI18n")
        public void setDate(String date) {

            TextView sinceFriends = mView.findViewById(R.id.single_reg_date);
            sinceFriends.setText("Friends Since : \n" + date);

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
