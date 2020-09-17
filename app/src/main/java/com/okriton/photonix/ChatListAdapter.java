package com.okriton.photonix;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private List<Messages> chatList;

    private DatabaseReference userDataRef;

    Context context;

    private FirebaseAuth mAuth;

    private String current_user_id;

    public ChatListAdapter(List<Messages> chatList){

        this.chatList = chatList;

    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View mainView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.single_message_layout ,viewGroup, false);

        context = viewGroup.getContext().getApplicationContext();

        mAuth = FirebaseAuth.getInstance();

        current_user_id = mAuth.getCurrentUser().getUid();

        return new ChatViewHolder(mainView);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull final ChatViewHolder holder, int position) {

        final Messages chats = chatList.get(position);

        String message_from = chats.getFrom();
        String message_type = chats.getType();

        userDataRef = FirebaseDatabase.getInstance().getReference().child("Users");

        userDataRef.child(message_from).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    String image = dataSnapshot.child("image").getValue().toString();

                    holder.setUserImage(image);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.messageReceiveText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.messageReceiveDate.setVisibility(View.VISIBLE);
                holder.messageReceiveDate.setText(String.format("%s %s", chats.getDate(), chats.getTime()));

            }
        });

        holder.messageSendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.messageSendDate.setVisibility(View.VISIBLE);
                holder.messageSendDate.setText(String.format("%s %s", chats.getDate(), chats.getTime()));

            }
        });

        if (message_type.equals("text")){

            holder.messageReceiveText.setVisibility(View.INVISIBLE);
            holder.messageReceiveImage.setVisibility(View.INVISIBLE);

            holder.messageSendText.setVisibility(View.VISIBLE);

            if (message_from.equals(current_user_id)){

                holder.messageSendText.setText(chats.getMessage());


            } else {

                holder.messageSendText.setVisibility(View.INVISIBLE);

                holder.messageReceiveText.setVisibility(View.VISIBLE);
                holder.messageReceiveImage.setVisibility(View.VISIBLE);

                holder.messageReceiveText.setText(chats.getMessage());

            }

        }

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        View mView;

        private CircleImageView messageReceiveImage;
        private TextView messageReceiveText, messageSendText;
        private TextView messageSendDate, messageReceiveDate;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            messageReceiveImage = mView.findViewById(R.id.message_receiver_image);
            messageReceiveText = mView.findViewById(R.id.message_receive_text);
            messageSendText = mView.findViewById(R.id.message_send_text);
            messageReceiveDate = mView.findViewById(R.id.message_receive_date);
            messageSendDate = mView.findViewById(R.id.message_send_date);
        }

        @SuppressLint("CheckResult")
        public void setUserImage(String userImage){

            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.user_view);

            Glide.with(context).setDefaultRequestOptions(placeholderRequest).load(userImage).into(messageReceiveImage);

        }
    }
}
