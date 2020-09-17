package com.okriton.photonix;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private RecyclerView myPostListView;

    private DatabaseReference postDataRef;
    private FirebaseAuth mAuth;
    private DatabaseReference dataRef;
    private DatabaseReference likesRef;

    private String current_user_id;
    private String userName;
    private int countLikes;
    private boolean likeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        mToolbar = findViewById(R.id.post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Posts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        postDataRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        dataRef = FirebaseDatabase.getInstance().getReference();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        current_user_id = mAuth.getCurrentUser().getUid();

        myPostListView = findViewById(R.id.my_post_list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        myPostListView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        startListening();
    }

    private void startListening() {

        Query postQuery = postDataRef.orderByChild("user_id").startAt(current_user_id).endAt(current_user_id + "\uf8ff");

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(postQuery, Posts.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Posts, PostsActivity.MyPostViewHolder>(options){

            @NonNull
            @Override
            public MyPostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.post_list_item, viewGroup, false);
                return new PostsActivity.MyPostViewHolder(view);

            }

            @Override
            protected void onBindViewHolder(@NonNull final MyPostViewHolder holder, int position, @NonNull Posts model) {

                final String postKey = getRef(position).getKey();

                final String caption_data = model.getCaption();
                holder.setCaptionText(caption_data);

                String image_url = model.getImage_url();
                holder.setPostImage(image_url);

                final String dateString = model.getPost_date();
                String timeString = model.getPost_time();
                holder.setTimeText(dateString + " " + timeString);

                String user_id = model.getUser_id();

                holder.setPostLikeCount(postKey);

                dataRef.child("Users").child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {

                            userName = dataSnapshot.child("name").getValue().toString();
                            String userImage = dataSnapshot.child("image").getValue().toString();
                            String userUname = dataSnapshot.child("username").getValue().toString();

                            holder.setUserImage(userImage);
                            holder.setUserName(userName);
                            holder.setcaptionUname(userUname);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                holder.postLikeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        likeChecker = true;

                        final String current_time = DateFormat.getDateTimeInstance().format(new Date());

                        final Map<String, Object> likeMap = new HashMap<>();
                        likeMap.put("name", userName);
                        likeMap.put("timestamp", current_time);

                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (likeChecker) {

                                    if (dataSnapshot.child(postKey).hasChild(current_user_id)) {

                                        likesRef.child(postKey).child(current_user_id).removeValue();

                                        likeChecker = false;

                                    } else {

                                        likesRef.child(postKey).child(current_user_id).setValue(likeMap);

                                        likeChecker = false;

                                    }

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

                holder.postCmntBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent commentIntent = new Intent(PostsActivity.this, CommentsActivity.class);
                        startActivity(commentIntent);

                    }
                });

            }
        };

        myPostListView.setAdapter(adapter);
        adapter.startListening();

    }

    public class MyPostViewHolder extends RecyclerView.ViewHolder {

        View mView;

        private TextView captionView, timeView, userNameView, postCaptionUname;
        private ImageView postImageView;
        private CircleImageView profileImageView;

        private ImageView postLikeBtn, postCmntBtn;
        private TextView postLikeCount;

        private FirebaseAuth mAuth;
        private DatabaseReference likeRef;

        public MyPostViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            postLikeBtn = mView.findViewById(R.id.post_like_btn);
            postLikeCount = mView.findViewById(R.id.post_like_count);
            postCmntBtn = mView.findViewById(R.id.post_comment_btn);

            mAuth = FirebaseAuth.getInstance();
            likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            likeRef.keepSynced(true);

        }

        public void setCaptionText(String captionText){

            captionView = mView.findViewById(R.id.post_view_caption);
            captionView.setText(captionText);

        }

        public void setPostLikeCount(final String post_id){

            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(post_id).hasChild(mAuth.getCurrentUser().getUid())){

                        postLikeBtn.setImageResource(R.mipmap.ic_like_accent_btn);

                        countLikes = (int) dataSnapshot.child(post_id).getChildrenCount();

                        postLikeCount.setText((String.valueOf(countLikes) + (" Likes")));

                    } else {

                        postLikeBtn.setImageResource(R.mipmap.ic_like_btn);

                        countLikes = (int) dataSnapshot.child(post_id).getChildrenCount();

                        postLikeCount.setText((String.valueOf(countLikes) + (" Likes")));

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        public void setcaptionUname(String captionUname){

            postCaptionUname = mView.findViewById(R.id.post_view_user_uname);
            postCaptionUname.setText(captionUname);

        }

        @SuppressLint("CheckResult")
        public void setPostImage(String downloadUri) {

            postImageView = mView.findViewById(R.id.post_view_image);

            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.user_view);

            Glide.with(getApplicationContext()).setDefaultRequestOptions(placeholderRequest).load(downloadUri).into(postImageView);

        }

        public void setTimeText(String timeText){

            timeView = mView.findViewById(R.id.post_view_time);
            timeView.setText(timeText);

        }

        public void setUserName(String name){

            userNameView = mView.findViewById(R.id.post_list_user_name);
            userNameView.setText(name);

        }

        @SuppressLint("CheckResult")
        public void setUserImage(String image){

            profileImageView = mView.findViewById(R.id.post_list_user_image);

            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.user_view);

            Glide.with(getApplicationContext()).setDefaultRequestOptions(placeholderRequest).load(image).into(profileImageView);

        }
    }
}
