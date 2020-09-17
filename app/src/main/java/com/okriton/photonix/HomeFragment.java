package com.okriton.photonix;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {

    private RecyclerView postListView;

    private FirebaseAuth mAuth;
    private DatabaseReference postDataRef;
    private DatabaseReference dataRef;
    private DatabaseReference likesRef;
    private DatabaseReference friendsRef;

    private int countLikes;
    private boolean likeChecker = false;
    private String userName;

    private Activity activity;
    private Context context;

    private String current_user_id;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View mainView = inflater.inflate(R.layout.fragment_home, container, false);

        postListView = mainView.findViewById(R.id.post_list_view);
        postListView.setHasFixedSize(true);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        postDataRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        dataRef = FirebaseDatabase.getInstance().getReference();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_user_id);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(container.getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        postListView.setLayoutManager(linearLayoutManager);

        // Inflate the layout for this fragment
        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null){

            startListening();

        }

    }

    private void startListening() {

        Query postQuery = postDataRef.orderByChild("post_count");

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(postQuery, Posts.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Posts, HomeFragment.PostViewHolder>(options){

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                context = viewGroup.getContext();

                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.post_list_item, viewGroup,false);

                return new PostViewHolder(view);

            }

            @Override
            protected void onBindViewHolder(@NonNull final PostViewHolder viewHolder, int position, @NonNull final Posts model) {

                final String post_id = getRef(position).getKey();
                final String user_id = model.getUser_id();

                final String caption_data = model.getCaption();
                viewHolder.setCaptionText(caption_data);

                String image_url = model.getImage_url();
                viewHolder.setPostImage(image_url);

                /*String postTime = String.valueOf(model.getTimestamp());
                GetTimeAgo getTimeAgo = new GetTimeAgo();
                long lastTime = Long.parseLong(postTime);
                String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, getContext());*/

                String postTime = model.getPost_time();
                String postDate = model.getPost_date();
                viewHolder.setTimeText(postTime + " " + postDate);

                viewHolder.setPostLikeCount(post_id);

                dataRef.child("Users").child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {

                            userName = dataSnapshot.child("name").getValue().toString();
                            String userImage = dataSnapshot.child("image").getValue().toString();
                            String userUname = dataSnapshot.child("username").getValue().toString();

                            viewHolder.setUserImage(userImage);
                            viewHolder.setUserName(userName);
                            viewHolder.setcaptionUname(userUname);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                // LIKE FEATURE

                viewHolder.postLikeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        likeChecker = true;

                        final String current_time = DateFormat.getDateTimeInstance().format(new Date());

                        final Map<String, Object> likeMap = new HashMap<>();
                        likeMap.put("name", userName);
                        likeMap.put("time", current_time);
                        likeMap.put("user_id", current_user_id);
                        likeMap.put("timestamp", ServerValue.TIMESTAMP);

                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (likeChecker) {

                                    if (dataSnapshot.child(post_id).hasChild(current_user_id)) {

                                        likesRef.child(post_id).child(current_user_id).removeValue();

                                        likeChecker = false;

                                    } else {

                                        likesRef.child(post_id).child(current_user_id).setValue(likeMap);

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

                viewHolder.profileImageView.setOnClickListener(new View.OnClickListener() {
                    @Nullable
                    @Override
                    public void onClick(View view) {

                        Intent profileintent = new Intent(context, ProfileActivity.class);
                        profileintent.putExtra("user_id", user_id);
                        profileintent.putExtra("user_name", userName);
                        context.startActivity(profileintent);

                    }
                });

                viewHolder.userNameView.setOnClickListener(new View.OnClickListener() {
                    @Nullable
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(context, ProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        profileIntent.putExtra("user_name", userName);
                        context.startActivity(profileIntent);

                    }
                });

                viewHolder.moreActionBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (current_user_id.equals(user_id)){

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);

                            String[] moreActions = {"Save Image to Gallery", "Report", "Share Post", "Delete Post", "Edit Post"};

                            builder.setItems(moreActions, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int item) {

                                    switch (item){

                                        case 0:

                                            saveImageToGallery(post_id);
                                            return;

                                        case 1:

                                            reportToAdmin(post_id);
                                            return;

                                        case 2:

                                            sharePost(post_id);
                                            return;

                                        case 3:
                                            deletePost(post_id);
                                            return;

                                        case 4:
                                            editPost(post_id);

                                    }

                                    dialogInterface.dismiss();

                                }
                            });

                            AlertDialog dialog = builder.create();
                            dialog.show();

                        } else {

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);

                            String[] moreActions = {"Save Image to Gallery", "Report", "Share Post"};

                            builder.setItems(moreActions, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int item) {

                                    switch (item){

                                        case 0:

                                            saveImageToGallery(post_id);
                                            return;

                                        case 1:

                                            reportToAdmin(post_id);
                                            return;

                                        case 2:

                                            sharePost(post_id);
                                            return;

                                    }

                                    dialogInterface.dismiss();

                                }
                            });

                            AlertDialog dialog = builder.create();
                            dialog.show();

                        }

                    }
                });

                viewHolder.postCmntBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent commentIntent = new Intent(context, CommentsActivity.class);
                        commentIntent.putExtra("post_id", post_id);
                        context.startActivity(commentIntent);

                    }
                });

            }
        };

        postListView.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();

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

    public class PostViewHolder extends RecyclerView.ViewHolder {

        View mView;

        private TextView captionView, timeView, userNameView, postCaptionUname;
        private ImageView postImageView, moreActionBtn;
        private CircleImageView profileImageView;

        private ImageView postLikeBtn, postCmntBtn;
        private TextView postLikeCount;

        private FirebaseAuth mAuth;
        private DatabaseReference likeRef;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            postLikeBtn = mView.findViewById(R.id.post_like_btn);
            postLikeCount = mView.findViewById(R.id.post_like_count);
            postCmntBtn = mView.findViewById(R.id.post_comment_btn);
            moreActionBtn = mView.findViewById(R.id.post_more_action_btn);

            profileImageView = mView.findViewById(R.id.post_list_user_image);
            userNameView = mView.findViewById(R.id.post_list_user_name);

            mAuth = FirebaseAuth.getInstance();
            likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            likeRef.keepSynced(true);

        }

        public void setPostLikeCount(final String post_id) {

            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(post_id).hasChild(mAuth.getCurrentUser().getUid())) {

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

        public void setCaptionText(String captionText) {

            captionView = mView.findViewById(R.id.post_view_caption);
            captionView.setText(captionText);

        }

        public void setcaptionUname(String captionUname) {

            postCaptionUname = mView.findViewById(R.id.post_view_user_uname);
            postCaptionUname.setText(captionUname);

        }

        @SuppressLint("CheckResult")
        public void setPostImage(String downloadUri) {

            postImageView = mView.findViewById(R.id.post_view_image);

            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.user_view);

            if (activity == null){

                return;
            }

            Glide.with(getActivity())
                    .setDefaultRequestOptions(placeholderRequest)
                    .load(downloadUri)
                    .into(postImageView);
        }

        public void setTimeText(String timeText) {

            timeView = mView.findViewById(R.id.post_view_time);
            timeView.setText(timeText);

        }

        public void setUserName(String name) {

            userNameView.setText(name);

        }

        @SuppressLint("CheckResult")
        public void setUserImage(String image) {

            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.user_view);

            if (activity == null){

                return;
            }

            Glide.with(getActivity())
                    .setDefaultRequestOptions(placeholderRequest)
                    .load(image)
                    .into(profileImageView);

        }

    }

    private void saveImageToGallery(String post_id) {



    }

    private void reportToAdmin(String post_id) {


    }

    private void sharePost(String post_id) {


    }

    private void editPost(String post_id) {

        Intent editPostIntent = new Intent(context, EditPostActivity.class);
        editPostIntent.putExtra("post_id", post_id);
        startActivity(editPostIntent);

    }

    private void deletePost(String post_id) {

        postDataRef.child(post_id).removeValue();

    }
}
