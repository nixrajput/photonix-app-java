package com.okriton.photonix;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewImageActivity extends AppCompatActivity {

    private ImageView profileImage;

    private DatabaseReference userDataRef;

    private String user_id;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        user_id = getIntent().getStringExtra("user_id");

        mToolbar = findViewById(R.id.view_image_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile Image");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userDataRef = FirebaseDatabase.getInstance().getReference().child("Users");

        profileImage = findViewById(R.id.view_profile_image);

        userDataRef.child(user_id).addValueEventListener(new ValueEventListener() {
            @SuppressLint("CheckResult")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    final String image = dataSnapshot.child("image").getValue().toString();

                    RequestOptions placeholderRequest = new RequestOptions();
                    placeholderRequest.placeholder(R.drawable.user_view);

                    Glide.with(getApplicationContext()).setDefaultRequestOptions(placeholderRequest).load(image).into(profileImage);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
