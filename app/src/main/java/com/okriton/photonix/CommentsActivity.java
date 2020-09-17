package com.okriton.photonix;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private RecyclerView commentListView;

    private CircleImageView cmntUserImage;
    private EditText cmntTextInput;
    private ImageView cmntPostBtn;

    private String post_id, current_user_id;
    private String saveCurrentDate, saveCurrentTime;

    private DatabaseReference userDataRef;
    private DatabaseReference commentRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        post_id = getIntent().getStringExtra("post_id");

        mAuth = FirebaseAuth.getInstance();
        userDataRef = FirebaseDatabase.getInstance().getReference().child("Users");
        commentRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(post_id).child("Comments");

        current_user_id = mAuth.getCurrentUser().getUid();

        mToolbar = findViewById(R.id.comments_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cmntUserImage = findViewById(R.id.comment_user_image);
        cmntTextInput = findViewById(R.id.comment_edit_text);
        cmntPostBtn = findViewById(R.id.comment_post_btn);

        Calendar calendarDate = Calendar.getInstance();
        java.text.SimpleDateFormat currentDate = new java.text.SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calendarDate.getTime());

        java.text.SimpleDateFormat currentTime = new java.text.SimpleDateFormat("hh:mm aa");
        saveCurrentTime = currentTime.format(calendarDate.getTime());

        commentListView = findViewById(R.id.comments_list_view);
        commentListView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        commentListView.setLayoutManager(linearLayoutManager);

        userDataRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @SuppressLint("CheckResult")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    String image = dataSnapshot.child("image").getValue().toString();
                    final String username = dataSnapshot.child("username").getValue().toString();

                    RequestOptions placeholderRequest = new RequestOptions();
                    placeholderRequest.placeholder(R.drawable.user_view);

                    Glide.with(getApplicationContext()).setDefaultRequestOptions(placeholderRequest).load(image).into(cmntUserImage);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        cmntPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userDataRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            final String username = dataSnapshot.child("username").getValue().toString();

                            addComment(username);

                            cmntTextInput.setText("");

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        startListening();

    }

    private void startListening() {

        FirebaseRecyclerOptions<Comments> options =
                new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(commentRef, Comments.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Comments, CommentsActivity.CommentsViewHolder>(options) {

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.single_comment_layout, viewGroup, false);
                return new CommentsViewHolder(view);

            }

            @Override
            protected void onBindViewHolder(@NonNull final CommentsViewHolder holder, int position, @NonNull Comments model) {

                String user_id = model.getUser_id();

                userDataRef.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            String image = dataSnapshot.child("image").getValue().toString();

                            holder.setCommentUserImageView(image);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                holder.setCommentTextView(model.getComment());
                holder.setUsernameTextView(model.getUsername());

                String commentTime = String.valueOf(model.getTimestamp());
                GetTimeAgo getTimeAgo = new GetTimeAgo();
                long lastTime = Long.parseLong(commentTime);
                String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                holder.setDateTextView(lastSeenTime);

            }
        };

        commentListView.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();

    }

    private void addComment(String username) {

        String commentText = cmntTextInput.getText().toString();

        if (!TextUtils.isEmpty(commentText)){

            String RandomKey = current_user_id + saveCurrentDate + "-" + saveCurrentTime;

            Map<String, Object> commentMap = new HashMap<>();
            commentMap.put("user_id", current_user_id);
            commentMap.put("comment", commentText);
            commentMap.put("date", saveCurrentDate);
            commentMap.put("time", saveCurrentTime);
            commentMap.put("timestamp", ServerValue.TIMESTAMP);
            commentMap.put("username", username);

            commentRef.child(RandomKey).updateChildren(commentMap);

        }

    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        private CircleImageView commentUserImageView;
        private TextView commentTextView, usernameTextView, dateTextView;

        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            commentUserImageView = mView.findViewById(R.id.comment_single_user_image);
            commentTextView = mView.findViewById(R.id.comment_single_comment);
            usernameTextView = mView.findViewById(R.id.comment_single_user_name);
            dateTextView = mView.findViewById(R.id.comment_single_date);

        }

        @SuppressLint("CheckResult")
        public void setCommentUserImageView (String imageFile){

            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.user_view);

            Glide.with(getApplicationContext()).setDefaultRequestOptions(placeholderRequest).load(imageFile).into(commentUserImageView);

        }

        public void setCommentTextView(String commentText){

            commentTextView.setText(commentText);

        }

        public void setUsernameTextView (String usernameText){

            usernameTextView.setText(usernameText);

        }

        public void setDateTextView(String dateText){

            dateTextView.setText(dateText);

        }
    }
}
