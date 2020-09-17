package com.okriton.photonix;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private CircleImageView profileImage;

    private EditText userDOBText;
    private TextInputLayout userNameText;
    private Button saveDataBtn;
    private RadioGroup genderRadio;
    private TextView errorText;

    private DatePickerDialog datePicker;

    private ProgressDialog setupDialog;

    private Uri mainImageURI = null;

    private String user_id;

    private StorageReference mImageRef;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mToolbar = findViewById(R.id.setup_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Setup Your Profile");

        mAuth = FirebaseAuth.getInstance();

        user_id = mAuth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference();
        mImageRef = FirebaseStorage.getInstance().getReference();

        profileImage = findViewById(R.id.setup_profile_image);
        userNameText = findViewById(R.id.setup_user_name);
        userDOBText = findViewById(R.id.setup_user_dob);
        genderRadio = findViewById(R.id.gender_radio_btn);
        errorText = findViewById(R.id.error_text);

        setupDialog = new ProgressDialog(this);
        setupDialog.setMessage("Saving User Data");
        setupDialog.setCancelable(false);

        saveDataBtn = findViewById(R.id.setup_save_btn);

        userDOBText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Calendar cal = Calendar.getInstance();
                int mYear = cal.get(Calendar.YEAR); // CURRENT YEAR
                int mMonth = cal.get(Calendar.MONTH); // CURRENT MONTH
                int mDay = cal.get(Calendar.DAY_OF_MONTH); // CURRENT DAY

                datePicker = new DatePickerDialog(SetupActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                                userDOBText.setText(day + "/" + (month + 1) + "/" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePicker.show();
                datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());

            }
        });

        saveDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // GET FIELD VALUE
                final String user_name = userNameText.getEditText().getText().toString();
                final String user_dob = userDOBText.getText().toString();
                RadioButton selected_gender = findViewById(genderRadio.getCheckedRadioButtonId());
                final String gender = selected_gender == null ? "":selected_gender.getText().toString();

                if (mainImageURI == null){

                    errorText.setVisibility(View.VISIBLE);
                    errorText.setText("Choose a Profile Image");

                }

                if (TextUtils.isEmpty(user_name)){

                    userNameText.setError("Enter Your Full Name");

                }

                if (TextUtils.isEmpty(gender)){

                    errorText.setVisibility(View.VISIBLE);
                    errorText.setText("Choose your Gender");

                }

                if (TextUtils.isEmpty(user_dob)){

                    errorText.setVisibility(View.VISIBLE);
                    errorText.setText("Select your Birthday");
                }

                if (mainImageURI != null && !TextUtils.isEmpty(user_dob) &&
                        !TextUtils.isEmpty(gender) && !TextUtils.isEmpty(user_name)){

                    setupDialog.show();
                    saveDataBtn.setEnabled(false);

                    saveUserData(user_name, user_dob, gender);

                }

            }
        });


        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(SetupActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED){

                        ActivityCompat.requestPermissions(SetupActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {

                        startImagePicker();

                    }

                } else {

                    startImagePicker();

                }

            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(SetupActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED){

                        ActivityCompat.requestPermissions(SetupActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {

                        startImagePicker();

                    }

                } else {

                    startImagePicker();

                }

            }
        });

    }

    private void saveUserData(final String user_name, final String user_dob, final String gender) {

        user_id = mAuth.getCurrentUser().getUid();

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

                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("name", user_name);
                            userMap.put("lowercase_name", user_name.toLowerCase());
                            userMap.put("image", download_url);
                            userMap.put("dob", user_dob);
                            userMap.put("gender", gender);

                            userRef.child("Users").child(user_id).updateChildren(userMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                Toast.makeText(SetupActivity.this,
                                                        "User Data Saved Successfully",
                                                        Toast.LENGTH_LONG).show();

                                                sendToUsername();

                                            } else {

                                                String error = task.getException().getMessage();
                                                Toast.makeText(SetupActivity.this,
                                                        "(Firebase Error) : " + error,
                                                        Toast.LENGTH_LONG).show();

                                            }

                                            saveDataBtn.setEnabled(true);
                                            setupDialog.dismiss();

                                        }
                                    });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(SetupActivity.this,
                                    "(Download URL Error) : " + e,
                                    Toast.LENGTH_LONG).show();

                            saveDataBtn.setEnabled(true);
                            setupDialog.dismiss();

                        }
                    });

                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "(Image Error) : " + error,
                            Toast.LENGTH_LONG).show();

                    saveDataBtn.setEnabled(true);
                    setupDialog.dismiss();

                }
            }
        });

    }

    private void sendToUsername() {

        Intent usernameIntent = new Intent(SetupActivity.this, UsernameActivity.class);
        usernameIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(usernameIntent);
        finish();

    }

    private void startImagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setMinCropWindowSize(512, 512)
                .start(SetupActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){

                mainImageURI = result.getUri();

                profileImage.setImageURI(mainImageURI);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }
}
