package com.okriton.photonix;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class RegisterPhoneActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private EditText phoneed, codeed;
    private TextView timertext;
    private ImageView verifiedimg;

    private Boolean mVerified = false;
    private FloatingActionButton fabbutton;
    private String mVerificationId;

    private Timer timer;

    private DatabaseReference userRef;
    private FirebaseAuth mAuth;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private String saveCurrentDate, saveCurrentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone);

        mToolbar = findViewById(R.id.register_phone_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create New Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        phoneed = findViewById(R.id.numbered);
        codeed = findViewById(R.id.verificationed);
        fabbutton = findViewById(R.id.sendverifybt);
        timertext = findViewById(R.id.timertv);
        verifiedimg = findViewById(R.id.verifiedsign);
        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
                Log.d("TAG", "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("TAG", "onVerificationFailed", e);



                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.parentlayout), "Verification Failed !! Invalied verification Code", Snackbar.LENGTH_LONG);

                    snackbar.show();
                }
                else if (e instanceof FirebaseTooManyRequestsException) {
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.parentlayout), "Verification Failed !! Too many request. Try after some time. ", Snackbar.LENGTH_LONG);

                    snackbar.show();
                }
                else if (e instanceof FirebaseNetworkException) {
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.parentlayout), "Verification Failed !! Network Error. ", Snackbar.LENGTH_LONG);

                    snackbar.show();
                }
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("TAG", "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

        fabbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabbutton.getTag().equals(getResources().getString(R.string.tag_send))) {
                    if (!phoneed.getText().toString().trim().isEmpty() && phoneed.getText().toString().trim().length() >= 10) {
                        startPhoneNumberVerification(("+91" + phoneed.getText().toString().trim()));
                        mVerified = false;
                        starttimer();
                        codeed.setVisibility(View.VISIBLE);
                        fabbutton.setImageResource(R.drawable.ic_arrow_forward_white_24dp);
                        fabbutton.setTag(getResources().getString(R.string.tag_verify));
                    }
                    else {
                        phoneed.setError("Please enter valid mobile number");
                    }
                }

                if (fabbutton.getTag().equals(getResources().getString(R.string.tag_verify))) {
                    if (!codeed.getText().toString().trim().isEmpty() && !mVerified) {
                        Snackbar snackbar = Snackbar
                                .make(findViewById(R.id.parentlayout), "Please wait...", Snackbar.LENGTH_LONG);

                        snackbar.show();
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, codeed.getText().toString().trim());
                        signInWithPhoneAuthCredential(credential);
                    }
                    if (mVerified) {

                        saveUserData();

                    }

                }
            }
        });

        timertext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!phoneed.getText().toString().trim().isEmpty() && phoneed.getText().toString().trim().length() == 10) {
                    resendVerificationCode(("+91" + phoneed.getText().toString().trim()), mResendToken);
                    mVerified = false;
                    starttimer();
                    codeed.setVisibility(View.VISIBLE);
                    fabbutton.setImageResource(R.drawable.ic_arrow_forward_white_24dp);
                    fabbutton.setTag(getResources().getString(R.string.tag_verify));
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.parentlayout), "Resending verification code...", Snackbar.LENGTH_LONG);

                    snackbar.show();
                }
            }
        });

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            mVerified = true;
                            timer.cancel();
                            verifiedimg.setVisibility(View.VISIBLE);
                            timertext.setVisibility(View.INVISIBLE);
                            phoneed.setEnabled(false);
                            codeed.setVisibility(View.INVISIBLE);
                            Snackbar snackbar = Snackbar
                                    .make(findViewById(R.id.parentlayout), "Successfully Verified", Snackbar.LENGTH_LONG);

                            snackbar.show();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Snackbar snackbar = Snackbar
                                        .make(findViewById(R.id.parentlayout), "Invalid OTP ! Please enter correct OTP", Snackbar.LENGTH_LONG);

                                snackbar.show();
                            }
                        }
                    }
                });
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

    }

    public void starttimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {

            int second = 60;

            @Override
            public void run() {
                if (second <= 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timertext.setText("RESEND CODE");
                            timer.cancel();
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timertext.setText("00:" + second--);
                        }
                    });
                }

            }
        }, 0, 1000);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void saveUserData() {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String user_id = currentUser.getUid();
        String phone = currentUser.getPhoneNumber();

        String device_token = FirebaseInstanceId.getInstance().getToken();

        Calendar calendarDate = Calendar.getInstance();
        java.text.SimpleDateFormat currentDate = new java.text.SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calendarDate.getTime());

        java.text.SimpleDateFormat currentTime = new java.text.SimpleDateFormat("hh:mm aa");
        saveCurrentTime = currentTime.format(calendarDate.getTime());

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("phone", phone);
        userMap.put("device_token", device_token);
        userMap.put("reg_date", saveCurrentDate);
        userMap.put("reg_time", saveCurrentTime);
        userMap.put("user_id", user_id);

        userRef.child("Users").child(user_id).updateChildren(userMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            Toast.makeText(RegisterPhoneActivity.this,
                                    "Account Created Successfully", Toast.LENGTH_LONG).show();

                            sendToMain();

                        } else {

                            String error = task.getException().getMessage();
                            Toast.makeText(RegisterPhoneActivity.this, error,
                                    Toast.LENGTH_LONG).show();

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

    private void sendToMain() {

        Intent mainIntent = new Intent(RegisterPhoneActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }

}
