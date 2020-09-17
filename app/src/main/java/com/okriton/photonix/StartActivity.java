package com.okriton.photonix;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    private Button regEmailBtn, regPhoneBtn, loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        regEmailBtn = findViewById(R.id.start_reg_email_btn);
        regPhoneBtn = findViewById(R.id.start_reg_phone_btn);
        loginBtn = findViewById(R.id.start_login_btn);

        // EMAIL REGISTER BUTTON
        regEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent regEmailIntent = new Intent(StartActivity.this, RegisterEmailActivity.class);
                startActivity(regEmailIntent);

            }
        });

        // PHONE REGISTER BUTTON
        regPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent regPhoneIntent = new Intent(StartActivity.this, RegisterPhoneActivity.class);
                startActivity(regPhoneIntent);

            }
        });

        // LOGIN BUTTON
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent loginIntent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(loginIntent);

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null){

            sendToMain();

        }
    }

    private void sendToMain() {

        Intent mainIntent = new Intent(StartActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
