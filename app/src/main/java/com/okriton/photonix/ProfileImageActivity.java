package com.okriton.photonix;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class ProfileImageActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private ImageView profileImageView;
    private Button selectImageBtn;

    private FirebaseAuth mAuth;
    private DatabaseReference userDataRef;
    private StorageReference mImageRef;

    private ProgressDialog upload_dialog;

    private Uri mainImageURI = null;

    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_image);

        mToolbar = findViewById(R.id.profile_image_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Profile Image");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        userDataRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mImageRef = FirebaseStorage.getInstance().getReference();

        profileImageView = findViewById(R.id.profile_image_user_image);
        selectImageBtn = findViewById(R.id.select_image_btn);

        userDataRef.child(user_id).addValueEventListener(new ValueEventListener() {
            @SuppressLint("CheckResult")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    String image = dataSnapshot.child("image").getValue().toString();

                    RequestOptions placeholderRequest = new RequestOptions();
                    placeholderRequest.placeholder(R.drawable.user_view);

                    Glide.with(getApplicationContext()).setDefaultRequestOptions(placeholderRequest).load(image)
                            .into(profileImageView);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .setMinCropWindowSize(512, 512)
                        .start(ProfileImageActivity.this);

            }
        });

        selectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mainImageURI != null){

                    saveImageData();

                } else {

                    Toast.makeText(getApplicationContext(), "Select an Image", Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private void saveImageData() {

        upload_dialog = new ProgressDialog(ProfileImageActivity.this);
        upload_dialog.setMessage("Updating Image...");
        upload_dialog.setCancelable(false);
        upload_dialog.show();

        StorageReference image_path = mImageRef.child("Profile_Images").child(user_id + ".jpg");

        image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()){

                    mImageRef.child("Profile_Images").child(user_id + ".jpg")
                            .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            String download_url = uri.toString();

                            userDataRef.child(user_id).child("image").setValue(download_url)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                Toast.makeText(ProfileImageActivity.this,
                                                        "Image Updated Successfully",
                                                        Toast.LENGTH_LONG).show();

                                                sendToMain();

                                            } else {

                                                String error = task.getException().getMessage();
                                                Toast.makeText(ProfileImageActivity.this,
                                                        "(Error) : " + error,
                                                        Toast.LENGTH_LONG).show();

                                            }

                                            upload_dialog.dismiss();

                                        }
                                    });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(ProfileImageActivity.this,
                                    "(Download URL Error) : " + e,
                                    Toast.LENGTH_LONG).show();

                            upload_dialog.dismiss();

                        }
                    });

                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(ProfileImageActivity.this, "(Image Error) : " + error,
                            Toast.LENGTH_LONG).show();

                    upload_dialog.dismiss();

                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){

                mainImageURI = result.getUri();

                profileImageView.setImageURI(mainImageURI);

                // selectImageBtn.setText("Update");

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }

    private void sendToMain() {

        Intent mainIntent = new Intent(ProfileImageActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();

    }
}
