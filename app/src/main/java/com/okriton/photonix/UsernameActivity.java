package com.okriton.photonix;

import android.app.ProgressDialog;
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UsernameActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private ProgressDialog usernameDialog;

    private TextInputLayout usernameTextLayout;
    private Button saveBtn;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);

        mToolbar = findViewById(R.id.username_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Username");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference();

        usernameTextLayout = findViewById(R.id.username_text_layout);
        saveBtn = findViewById(R.id.save_username_btn);

        usernameDialog = new ProgressDialog(UsernameActivity.this);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String uname = usernameTextLayout.getEditText().getText().toString();

                if (!TextUtils.isEmpty(uname)){

                    usernameDialog.setMessage("Checking Availability");
                    usernameDialog.setCancelable(false);
                    usernameDialog.show();

                    createUsername(uname);

                } else {

                    usernameTextLayout.setError("Enter an Username");

                }

            }
        });
    }

    private void createUsername(final String uname) {

        userRef.child("Users").orderByChild("username").equalTo(uname)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Log.i(Constants.TAG, "dataSnapshot value = " + dataSnapshot.getValue());

                if (dataSnapshot.exists()){

                    usernameTextLayout.setError("Username Not Available");

                    usernameDialog.dismiss();

                } else {

                    usernameDialog.setMessage("Saving Username");

                    usernameTextLayout.setError(null);
                    userRef.child("Users").child(current_user_id).child("username").setValue(uname)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        usernameDialog.dismiss();
                                        senToMain();

                                    }

                                }
                            });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void senToMain() {

        Intent mainIntent = new Intent(UsernameActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
