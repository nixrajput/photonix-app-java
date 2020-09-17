package com.okriton.photonix;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private RecyclerView chatListView;
    private FloatingActionButton sendMsgBtn;
    private EditText messageTextBox;

    private TextView mTitleView, mLastSeenView;
    private CircleImageView mProfileImage;

    private String current_user_id, receiver_user_id, receiver_user_name;
    private String saveCurrentDate, saveCurrentTime;

    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private DatabaseReference rootDataRef;

    private final List<Messages> chatList = new ArrayList<>();
    private ChatListAdapter adapter;

    private int current_page = 1;
    private static final int TOTAL_ITEMS_TO_LOAD = 10;

    private int itemPos = 0;

    private String mLastKey = "";
    private String mPrevKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        receiver_user_id = getIntent().getStringExtra("user_id");
        receiver_user_name = getIntent().getStringExtra("user_name");

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        rootDataRef = FirebaseDatabase.getInstance().getReference();

        // ACTION BAR START

        mToolbar = findViewById(R.id.chat_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        getSupportActionBar().setCustomView(action_bar_view);

        mTitleView = findViewById(R.id.custom_bar_title);
        mLastSeenView = findViewById(R.id.custom_bar_last_seen);
        mProfileImage = findViewById(R.id.custom_bar_image);

        mTitleView.setText(receiver_user_name);

        // ACTION BAR ENDS

        chatListView = findViewById(R.id.chat_message_list);
        sendMsgBtn = findViewById(R.id.send_message_btn);
        messageTextBox = findViewById(R.id.message_box);

        adapter = new ChatListAdapter(chatList);

        chatListView.setHasFixedSize(true);
        chatListView.setLayoutManager(new LinearLayoutManager(this));
        chatListView.setAdapter(adapter);

        Calendar calendarDate = Calendar.getInstance();
        java.text.SimpleDateFormat currentDate = new java.text.SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calendarDate.getTime());

        java.text.SimpleDateFormat currentTime = new java.text.SimpleDateFormat("hh:mm aa");
        saveCurrentTime = currentTime.format(calendarDate.getTime());

        rootDataRef.child("Chats").child(current_user_id).child(receiver_user_id).child("seen").setValue(true);

        loadMessages();

        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();

            }
        });

        userRef.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @SuppressLint({"CheckResult", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    String online = dataSnapshot.child("online").getValue().toString();
                    String image = dataSnapshot.child("image").getValue().toString();

                    RequestOptions placeholderRequest = new RequestOptions();
                    placeholderRequest.placeholder(R.drawable.user_view);

                    Glide.with(getApplicationContext()).setDefaultRequestOptions(placeholderRequest).load(image).into(mProfileImage);

                    if(online.equals("true")) {

                        mLastSeenView.setText("Online");

                    } else {

                        GetTimeAgo getTimeAgo = new GetTimeAgo();

                        long lastTime = Long.parseLong(online);

                        String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                        mLastSeenView.setText(lastSeenTime);

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMessages() {

        DatabaseReference messageRef = rootDataRef.child("Messages").child(current_user_id).child(receiver_user_id);

        Query messageQuery = messageRef.limitToLast(current_page * TOTAL_ITEMS_TO_LOAD);

        String device_token = FirebaseInstanceId.getInstance().getToken();

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if (itemPos == 1){

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;

                    mPrevKey = messageKey;

                }

                chatList.add(messages);
                adapter.notifyDataSetChanged();
                chatListView.scrollToPosition(chatList.size() - 1);

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

    }

    private void sendMessage() {

        String message = messageTextBox.getText().toString();

        if (!TextUtils.isEmpty(message)){

            String current_user_ref = "Messages/" + current_user_id + "/" + receiver_user_id;
            String receiver_user_ref = "Messages/" + receiver_user_id + "/" + current_user_id;

            DatabaseReference user_message_push = rootDataRef.child("Messages").child(current_user_id).child(receiver_user_id).push();
            String message_push_id = user_message_push.getKey();

            String device_token = FirebaseInstanceId.getInstance().getToken();

            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("device_token", device_token);
            messageMap.put("date", saveCurrentDate);
            messageMap.put("time", saveCurrentTime);
            messageMap.put("from", current_user_id);

            Map<String, Object> messageUserMap = new HashMap<>();
            messageUserMap.put(current_user_ref + "/" + message_push_id, messageMap);
            messageUserMap.put(receiver_user_ref + "/" + message_push_id, messageMap);

            rootDataRef.child("Chats").child(current_user_id).child(receiver_user_id).child("seen").setValue(true);
            rootDataRef.child("Chats").child(current_user_id).child(receiver_user_id).child("timestamp").setValue(ServerValue.TIMESTAMP);

            rootDataRef.child("Chats").child(receiver_user_id).child(current_user_id).child("seen").setValue(false);
            rootDataRef.child("Chats").child(receiver_user_id).child(current_user_id).child("timestamp").setValue(ServerValue.TIMESTAMP);

            messageTextBox.setText("");

            rootDataRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    if (databaseError != null){

                        Log.d("Chat Log", databaseError.getMessage());

                    }
                }
            });

        } else {

            sendMsgBtn.setEnabled(false);

        }

    }
}
