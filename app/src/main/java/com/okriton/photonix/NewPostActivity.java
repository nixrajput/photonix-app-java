package com.okriton.photonix;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NewPostActivity extends AppCompatActivity {

    private static final int MAX_LENGTH = 100;
    private ImageView postImage;
    private EditText postCaption;
    private Button postBtn;
    private TextView errorText;

    private Uri postImageUri = null;

    private ProgressDialog uploadDialog;

    private StorageReference mImageRef;
    private DatabaseReference postImageRef;
    private FirebaseAuth mAuth;

    private String current_user_id;
    private String saveCurrentDate, saveCurrentTime, postTimestamp;
    private long countPost = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        mImageRef = FirebaseStorage.getInstance().getReference();
        postImageRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        mAuth = FirebaseAuth.getInstance();

        current_user_id = mAuth.getCurrentUser().getUid();

        postImage = findViewById(R.id.post_view_image);
        postCaption = findViewById(R.id.post_caption);
        errorText = findViewById(R.id.error_text);

        postBtn = findViewById(R.id.post_btn);

        uploadDialog = new ProgressDialog(NewPostActivity.this);

        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .setMinCropResultSize(512, 512)
                        .setMinCropWindowSize(512, 512)
                        .start(NewPostActivity.this);

            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                postImage();

            }
        });

    }

    private void postImage() {

        final String caption = postCaption.getText().toString();

        if (postImageUri != null){

            uploadDialog.setMessage("Uploading Post");
            uploadDialog.setCancelable(false);
            uploadDialog.show();
            errorText.setVisibility(View.INVISIBLE);;
            postBtn.setEnabled(false);
            postImage.setEnabled(false);
            postCaption.setEnabled(false);

            final String randomName = random();

            postImageRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()){

                        countPost = dataSnapshot.getChildrenCount();

                    } else {

                        countPost = 0;

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            StorageReference file_path = mImageRef.child("Post_Images").child(randomName + ".jpg");

            file_path.putFile(postImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    mImageRef.child("Post_Images").child(randomName + ".jpg").getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String post_url = uri.toString();

                                    Calendar calendarDate = Calendar.getInstance();
                                    java.text.SimpleDateFormat currentDate = new java.text.SimpleDateFormat("dd-MMMM-yyyy");
                                    saveCurrentDate = currentDate.format(calendarDate.getTime());

                                    java.text.SimpleDateFormat currentTime = new java.text.SimpleDateFormat("hh:mm aa");
                                    saveCurrentTime = currentTime.format(calendarDate.getTime());

                                    java.text.SimpleDateFormat postTime = new java.text.SimpleDateFormat("HH:mm");
                                    postTimestamp = postTime.format(calendarDate.getTime());

                                    DatabaseReference post_push = postImageRef.push();

                                    String push_id = post_push.getKey() + "-" + saveCurrentDate + "-" + postTimestamp;

                                    Map<String, Object> postMap = new HashMap<>();
                                    postMap.put("image_url", post_url);
                                    postMap.put("caption", caption);
                                    postMap.put("user_id", current_user_id);
                                    postMap.put("post_date", saveCurrentDate);
                                    postMap.put("post_time", saveCurrentTime);
                                    postMap.put("timestamp", ServerValue.TIMESTAMP);
                                    postMap.put("post_id", push_id);
                                    postMap.put("post_count", countPost);

                                    postImageRef.child(push_id)
                                            .setValue(postMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()){

                                                        Toast.makeText(NewPostActivity.this,
                                                                "Post Added Successfully",
                                                                Toast.LENGTH_LONG).show();

                                                        sendToMain();

                                                    } else {

                                                        String error = task.getException().getMessage();
                                                        Toast.makeText(NewPostActivity.this,
                                                                "(Firestore Error) : " + error,
                                                                Toast.LENGTH_LONG).show();

                                                    }

                                                    postCaption.setEnabled(true);
                                                    postImage.setEnabled(true);
                                                    postBtn.setEnabled(true);
                                                    uploadDialog.dismiss();

                                                }
                                            });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(NewPostActivity.this,
                                    "(Download URL Error) : " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();

                            postCaption.setEnabled(true);
                            postImage.setEnabled(true);
                            postBtn.setEnabled(true);
                            uploadDialog.dismiss();

                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(NewPostActivity.this,
                            "(Error) : " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    postCaption.setEnabled(true);
                    postImage.setEnabled(true);
                    postBtn.setEnabled(true);
                    uploadDialog.dismiss();

                }
            });

        } else {

            if (postImageUri == null){

                errorText.setVisibility(View.VISIBLE);
                errorText.setText("Choose an Image");

            }

        }

    }

    private void sendToMain() {

        Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){

                postImageUri = result.getUri();

                postImage.setImageURI(postImageUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }

    public static String random(){
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
