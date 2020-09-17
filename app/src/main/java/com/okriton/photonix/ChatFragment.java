package com.okriton.photonix;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {

    private RecyclerView convListView;

    private DatabaseReference chatDataRef;
    private DatabaseReference messageDataRef;
    private DatabaseReference userDataRef;

    private FirebaseAuth mAuth;

    private String current_user_id;

    private Activity activity;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_chat, container, false);

        convListView = mainView.findViewById(R.id.chats_conv_list);

        mAuth = FirebaseAuth.getInstance();

        current_user_id = mAuth.getCurrentUser().getUid();

        chatDataRef = FirebaseDatabase.getInstance().getReference().child("Chats").child(current_user_id);

        messageDataRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(current_user_id);

        userDataRef = FirebaseDatabase.getInstance().getReference().child("Users");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        convListView.setHasFixedSize(true);
        convListView.setLayoutManager(linearLayoutManager);

        // Inflate the layout for this fragment
        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        startListening();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        activity = getActivity();

    }

    @Override
    public void onDetach() {
        super.onDetach();

        activity = null;

    }

    private void startListening() {

        Query conversationQuery = chatDataRef.orderByChild("timestamp");

        FirebaseRecyclerOptions<Chats> options =
                new FirebaseRecyclerOptions.Builder<Chats>()
                        .setQuery(conversationQuery, Chats.class)
                        .build();

        FirebaseRecyclerAdapter firebaseChatAdapter = new FirebaseRecyclerAdapter<Chats, ChatFragment.ConvViewHolder>(options) {

            @NonNull
            @Override
            public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.chat_frag_layout, viewGroup, false);
                return new ConvViewHolder(view);

            }

            @Override
            protected void onBindViewHolder(@NonNull final ConvViewHolder holder, int position, @NonNull final Chats model) {

                final String list_user_id = getRef(position).getKey();

                final Query lastMessageQuery = messageDataRef.child(list_user_id).limitToLast(1);

                messageDataRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(list_user_id)){

                            lastMessageQuery.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                    String data = dataSnapshot.child("message").getValue().toString();
                                    holder.setMessage(data, model.isSeen());

                                }

                                @Override
                                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                }

                                @Override
                                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            userDataRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    final String userName = dataSnapshot.child("name").getValue().toString();
                                    String userThumb = dataSnapshot.child("image").getValue().toString();

                                    holder.setName(userName);
                                    holder.setUserImage(userThumb);

                                    holder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_user_id);
                                            chatIntent.putExtra("user_name", userName);
                                            startActivity(chatIntent);

                                        }
                                    });

                                    holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                                        @Override
                                        public boolean onLongClick(View view) {

                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                            builder.setMessage("Are You Sure to Delete Chat?");

                                            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                    dialogInterface.dismiss();
                                                    messageDataRef.child(list_user_id).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){

                                                                        Toast.makeText(activity, "Chat Deleted", Toast.LENGTH_SHORT).show();

                                                                    } else {

                                                                        String msg = task.getException().getMessage();

                                                                        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();

                                                                    }

                                                                }
                                                            });

                                                }
                                            });

                                            builder.show();
                                            return true;
                                        }
                                    });

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

            }
        };

        convListView.setAdapter(firebaseChatAdapter);
        firebaseChatAdapter.startListening();
        firebaseChatAdapter.notifyDataSetChanged();

    }

    public class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;

        private ImageView seenImageView;

        TextView userStatusView;

        public ConvViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            seenImageView = mView.findViewById(R.id.seen_image_view);

            userStatusView = mView.findViewById(R.id.single_chat_message);

        }

        public void setMessage(String message, boolean isSeen){

            userStatusView.setText(message);

            if(!isSeen){

                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);

            } else {

                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);

            }

        }

        public void setName(String name){

            TextView userNameView = mView.findViewById(R.id.single_chat_username);
            userNameView.setText(name);

        }

        @SuppressLint("CheckResult")
        public void setUserImage(String userImage){

            CircleImageView userImageView = mView.findViewById(R.id.single_chat_user_image);

            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.user_view);

            if (activity == null){

                return;
            }

            Glide.with(getActivity()).setDefaultRequestOptions(placeholderRequest).load(userImage).into(userImageView);

        }

    }
}

