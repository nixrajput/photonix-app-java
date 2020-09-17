package com.okriton.photonix;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


public class NotificationFragment extends Fragment {

    private RecyclerView notificationListView;

    private FirebaseAuth mAuth;
    private DatabaseReference rootDataRef;
    private DatabaseReference postDataRef;
    private DatabaseReference likesRef;

    private String userName;

    private Activity activity;
    private Context context;

    private String current_user_id;

    public static NotificationFragment newInstance() {
        return new NotificationFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View mainView = inflater.inflate(R.layout.fragment_notification, container, false);

        notificationListView = mainView.findViewById(R.id.notifications_list);
        notificationListView.setHasFixedSize(true);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        rootDataRef = FirebaseDatabase.getInstance().getReference();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postDataRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(container.getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        notificationListView.setLayoutManager(linearLayoutManager);

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



    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {

        View mView;

        private ImageView userImageView;
        private TextView notifTextView, timeTextView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            userImageView = mView.findViewById(R.id.notif_user_image);
            notifTextView = mView.findViewById(R.id.notif_data_text);
            timeTextView = mView.findViewById(R.id.notif_time);

        }

        @SuppressLint("CheckResult")
        public void setUserImage(String userImage){

            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.user_view);

            if (activity == null){

                return;
            }

            Glide.with(getActivity())
                    .setDefaultRequestOptions(placeholderRequest)
                    .load(userImage)
                    .into(userImageView);

        }

        public void setNotifData(String notifData){

            notifTextView.setText(notifData);

        }

        public void setNotifTime(String notifTime){

            timeTextView.setText(notifTime);

        }
    }
}
