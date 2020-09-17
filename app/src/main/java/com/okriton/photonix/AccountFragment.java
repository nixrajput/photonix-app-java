package com.okriton.photonix;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


public class AccountFragment extends Fragment {

    private LinearLayout allUsersLay, verifyEmailLay, addPhoneLay, aboutLay, logoutLay;
    private LinearLayout privacyPolicyLay, usesTermsLay;
    private CircleImageView accountImage;
    private TextView accountUsername;
    private TextView friendCount, requestCount, postCount;

    private LinearLayout postLayout, friendLayout, requestLayout;

    private FirebaseAuth mAuth;
    private DatabaseReference userDataRef;
    private DatabaseReference friendRequestRef;
    private DatabaseReference friendsRef;
    private DatabaseReference postRef;

    private String user_id;
    private int friend_count, request_count, post_count;

    private Activity activity;

    public static AccountFragment newInstance() {
        return new AccountFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_account, container, false);

        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        userDataRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userDataRef.keepSynced(true);

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        friendRequestRef.keepSynced(true);

        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        friendsRef.keepSynced(true);

        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        friendsRef.keepSynced(true);

        allUsersLay = mainView.findViewById(R.id.account_all_users_layout);
        verifyEmailLay = mainView.findViewById(R.id.complete_profile_layout);
        addPhoneLay = mainView.findViewById(R.id.account_reset_password_layout);
        aboutLay = mainView.findViewById(R.id.account_about_layout);
        logoutLay = mainView.findViewById(R.id.account_logout_layout);
        privacyPolicyLay = mainView.findViewById(R.id.account_privacy_policy_layout);
        usesTermsLay = mainView.findViewById(R.id.account_terms_layout);

        accountImage = mainView.findViewById(R.id.account_user_image);
        accountUsername = mainView.findViewById(R.id.account_user_name);

        friendCount = mainView.findViewById(R.id.accnt_frnd_no);
        postCount = mainView.findViewById(R.id.account_photo_no);
        requestCount = mainView.findViewById(R.id.accnt_request_no);

        postLayout = mainView.findViewById(R.id.photos_layout);
        friendLayout = mainView.findViewById(R.id.friends_layout);
        requestLayout = mainView.findViewById(R.id.requests_layout);

        if (mAuth.getCurrentUser() != null) {

            userDataRef.child(user_id).addValueEventListener(new ValueEventListener() {
                @SuppressLint("CheckResult")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()){

                        String name = dataSnapshot.child("name").getValue().toString();
                        String image = dataSnapshot.child("image").getValue().toString();

                        accountUsername.setText(name);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.user_view);

                        if (activity == null){

                            return;
                        }

                        Glide.with(getActivity()).setDefaultRequestOptions(placeholderRequest).load(image)
                                .into(accountImage);

                    } else {

                        Toast.makeText(getActivity(),
                                "User data doesn't exists",
                                Toast.LENGTH_LONG).show();

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            friendRequestRef.child(user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()){

                        request_count = (int) dataSnapshot.getChildrenCount();

                        requestCount.setText(String.valueOf(request_count));

                    } else {

                        requestCount.setText("0");

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            friendsRef.child(user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()){

                        friend_count = (int) dataSnapshot.getChildrenCount();

                        friendCount.setText(String.valueOf(friend_count));

                    } else {

                        friendCount.setText("0");

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            postRef.orderByChild("user_id").startAt(user_id).endAt(user_id + "\uf8ff").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()){

                        post_count = (int) dataSnapshot.getChildrenCount();

                        postCount.setText(String.valueOf(post_count));

                    } else {

                        postCount.setText("0");

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        accountImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent imageIntent = new Intent(getContext(), ProfileImageActivity.class);
                startActivity(imageIntent);

            }
        });

        logoutLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String current_user_id = mAuth.getCurrentUser().getUid();

                userDataRef.child(current_user_id).child("online").setValue(ServerValue.TIMESTAMP);

                mAuth.signOut();

                senToStart();

            }
        });

        allUsersLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent userIntent = new Intent(getContext(), AllUsersActivity.class);
                startActivity(userIntent);

            }
        });

        verifyEmailLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



            }
        });

        postLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent postIntent = new Intent(getContext(), PostsActivity.class);
                postIntent.putExtra("user_id", user_id);
                startActivity(postIntent);

            }
        });

        friendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent friendIntent = new Intent(getContext(), FriendsActivity.class);
                startActivity(friendIntent);

            }
        });

        requestLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent requestIntent = new Intent(getContext(), RequestsActivity.class);
                requestIntent.putExtra("user_id", user_id);
                startActivity(requestIntent);

            }
        });

        aboutLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent aboutIntent = new Intent(getContext(), AboutActivity.class);
                startActivity(aboutIntent);

            }
        });

        privacyPolicyLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

        // Inflate the layout for this fragment
        return mainView;
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

    private void senToStart() {

        Intent startIntent = new Intent(getContext(), StartActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startIntent);

    }
}
