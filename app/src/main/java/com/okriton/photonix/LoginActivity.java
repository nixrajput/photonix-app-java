package com.okriton.photonix;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private ProgressBar loginBar;

    private TextInputLayout loginEmailText, loginPassText;
    private Button loginBtn;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private String saveCurrentDate, saveCurrentTime;

    private Boolean emailChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mToolbar = findViewById(R.id.login_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Log In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference();

        loginEmailText = findViewById(R.id.login_email_text_layout);
        loginPassText = findViewById(R.id.login_pass_text_layout);
        loginBtn = findViewById(R.id.login_btn);

        loginBar = findViewById(R.id.login_progress_bar);

        Calendar calendarDate = Calendar.getInstance();
        java.text.SimpleDateFormat currentDate = new java.text.SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calendarDate.getTime());

        java.text.SimpleDateFormat currentTime = new java.text.SimpleDateFormat("hh:mm aa");
        saveCurrentTime = currentTime.format(calendarDate.getTime());

        // TEXT FIELD LISTENER START

        loginEmailText.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                validateEmailText(editable);

            }
        });

        loginEmailText.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                if (!b){

                    validateEmailText(((EditText) view).getText());

                }

            }
        });

        loginPassText.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                validatePassText(editable);

            }
        });

        loginPassText.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                if (!b){

                    validatePassText(((EditText) view).getText());

                }

            }
        });

        // TEXT FIELD LISTENER END

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String loginEmail = loginEmailText.getEditText().getText().toString();
                String loginPass = loginPassText.getEditText().getText().toString();

                if (Patterns.EMAIL_ADDRESS.matcher(loginEmail).matches()) {

                    if (loginPass.length() < 8){

                        loginPassText.setError("Minimum length of password should be 8");

                    } else {

                        loginUser(loginEmail, loginPass);

                    }

                } else {

                    loginEmailText.setError("Email is Invalid");

                }

            }
        });
    }

    private void loginUser(final String loginEmail, String loginPass) {

        loginBtn.setEnabled(false);
        loginBtn.setVisibility(View.INVISIBLE);
        loginBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(loginEmail, loginPass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            String current_user_id = mAuth.getCurrentUser().getUid();
                            String device_token = FirebaseInstanceId.getInstance().getToken();

                            Map<String, Object> loginMap = new HashMap<>();
                            loginMap.put("device_token", device_token);
                            loginMap.put("last_login", saveCurrentDate+ " " + saveCurrentTime);

                            userRef.child("Users").child(current_user_id).updateChildren(loginMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    verifyEmail();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    String error = e.getMessage();

                                }
                            });

                        } else {

                            String error = task.getException().getMessage();
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

                            builder.setMessage(error);

                            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    dialogInterface.dismiss();

                                }
                            });

                            builder.show();

                        }

                        loginBtn.setEnabled(true);
                        loginBtn.setVisibility(View.VISIBLE);
                        loginBar.setVisibility(View.GONE);

                    }
                });

    }


    private void validatePassText(Editable editable) {

        if (!TextUtils.isEmpty(editable)){

            loginPassText.setError(null);

        } else {

            loginPassText.setError("Password is Required");

        }

    }

    private void validateEmailText(Editable editable) {

        if (TextUtils.isEmpty(editable)){

            loginEmailText.setError("Email is Required");

        } else {

            loginEmailText.setError(null);

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null){

            sendToMain();

        }

    }

    private void verifyEmail(){

        final FirebaseUser current_user = mAuth.getCurrentUser();
        emailChecker = current_user.isEmailVerified();

        if (emailChecker){

            sendToMain();

        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

            builder.setMessage("Please verify your account first");

            builder.setPositiveButton("VERIFY", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    if (current_user != null){

                        current_user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()){

                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

                                    builder.setMessage("We've sent you a mail, please check and verify your account");

                                    builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            dialogInterface.dismiss();
                                            mAuth.signOut();

                                        }
                                    });

                                    builder.show();

                                } else {

                                    String message = task.getException().getMessage();

                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

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
            });

            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    mAuth.signOut();
                    dialogInterface.dismiss();

                }
            });

            builder.show();

        }

    }

    private void sendToMain() {

        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
