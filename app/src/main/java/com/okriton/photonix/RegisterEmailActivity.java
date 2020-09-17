package com.okriton.photonix;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterEmailActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputLayout registerEmailText, registerPassText, confirmRegPassText;
    private Button registerBtn;

    private ProgressBar registerBar;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private String saveCurrentDate, saveCurrentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_email);

        mToolbar = findViewById(R.id.email_register_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create New Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference();

        registerEmailText = findViewById(R.id.register_email_text_layout);
        registerPassText = findViewById(R.id.register_pass_text_layout);
        confirmRegPassText = findViewById(R.id.register_confirm_pass_text_layout);

        registerBar = findViewById(R.id.register_progress_bar);

        registerBtn = findViewById(R.id.register_btn);

        // TEXT FIELD LISTENER START

        registerEmailText.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable em) {

                validateEmailText(em);

            }
        });

        registerEmailText.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b){

                    validateEmailText(((EditText) view).getText());

                }
            }
        });

        registerPassText.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void afterTextChanged(Editable ps) {

                validatePassText(ps);

            }
        });

        registerPassText.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                if (!b){

                    validatePassText(((EditText) view).getText());

                }

            }
        });

        confirmRegPassText.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable cps) {

                validateConfirmPassText(cps);

            }
        });

        confirmRegPassText.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                if (!b){

                    validateConfirmPassText(((EditText) view).getText());

                }

            }
        });

        // TEXT FIELD LISTENER END

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = registerEmailText.getEditText().getText().toString();
                String pass = registerPassText.getEditText().getText().toString();
                String confirmPass = confirmRegPassText.getEditText().getText().toString();

                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                    if (pass.equals(confirmPass)) {

                    if (pass.length() > 7) {

                            registerUser(email, pass);

                        } else {

                        registerPassText.setError("Minimum length of password should be 8");
                        confirmRegPassText.setError("Minimum length of password should be 8");

                        }

                    } else {

                        confirmRegPassText.setError("Password doesn't match");

                    }

                } else {

                    registerEmailText.setError("Email is Invalid");

                }

                }
        });
    }

    // INPUT VALIDATION START

    private void validateConfirmPassText(Editable cps) {

        if (!TextUtils.isEmpty(cps)){

            confirmRegPassText.setError(null);

        } else {

            confirmRegPassText.setError("Enter Password Again");

        }

    }

    private void validatePassText(Editable ps) {

        if (!TextUtils.isEmpty(ps)){

            registerPassText.setError(null);

        } else {

            registerPassText.setError("Password is Required");

        }

    }

    private void validateEmailText(Editable em) {

        if (TextUtils.isEmpty(em)){

            registerEmailText.setError("Email is Required");

        } else {

            registerEmailText.setError(null);

        }

    }

    // INPUT VALIDATION END

    private void registerUser(final String email, String pass) {

        registerBtn.setEnabled(false);
        registerBtn.setVisibility(View.INVISIBLE);
        registerBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){

                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            String user_id = currentUser.getUid();

                            String device_token = FirebaseInstanceId.getInstance().getToken();

                            Calendar calendarDate = Calendar.getInstance();
                            java.text.SimpleDateFormat currentDate = new java.text.SimpleDateFormat("dd-MMMM-yyyy");
                            saveCurrentDate = currentDate.format(calendarDate.getTime());

                            Calendar calendarTime = Calendar.getInstance();
                            java.text.SimpleDateFormat currentTime = new java.text.SimpleDateFormat("hh:mm aa");
                            saveCurrentTime = currentTime.format(calendarTime.getTime());

                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("email", email);
                            userMap.put("image", "default");
                            userMap.put("device_token", device_token);
                            userMap.put("reg_date", saveCurrentDate);
                            userMap.put("reg_time", saveCurrentTime);
                            userMap.put("user_id", user_id);

                            userRef.child("Users").child(user_id).setValue(userMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        sendVerifyEmail();

                                    } else {

                                        String error = task.getException().getMessage();
                                        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterEmailActivity.this);

                                        builder.setMessage(error);

                                        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                dialogInterface.dismiss();

                                            }
                                        });

                                        builder.show();

                                    }

                                    registerBtn.setEnabled(true);
                                    registerBtn.setVisibility(View.VISIBLE);
                                    registerBar.setVisibility(View.GONE);

                                }
                            });

                        } else {

                            String error = task.getException().getMessage();
                            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterEmailActivity.this);

                            builder.setMessage(error);

                            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    dialogInterface.dismiss();

                                }
                            });

                            builder.show();

                            registerBtn.setEnabled(true);
                            registerBtn.setVisibility(View.VISIBLE);
                            registerBar.setVisibility(View.GONE);

                        }
                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null){

            sendToMain();

        }

    }

    private void sendVerifyEmail(){

        FirebaseUser current_user = mAuth.getCurrentUser();

        if (current_user != null){

            current_user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()){

                        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterEmailActivity.this);

                        builder.setMessage("We've sent you a mail, please check and verify your account");

                        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                dialogInterface.dismiss();
                                sendToLogin();
                                mAuth.signOut();

                            }
                        });

                        builder.show();

                    } else {

                        String message = task.getException().getMessage();

                        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterEmailActivity.this);

                        builder.setMessage(message);

                        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                dialogInterface.dismiss();
                                mAuth.signOut();

                            }
                        });

                        builder.show();

                    }

                }
            });

        }

    }

    private void sendToMain() {

        Intent mainIntent = new Intent(RegisterEmailActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }

    private void sendToLogin() {

        Intent loginIntent = new Intent(RegisterEmailActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();

    }
}
