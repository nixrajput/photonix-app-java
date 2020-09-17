package com.okriton.photonix;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity {

    private TextView tv1, tv2, tv3, tv4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        tv3 = (TextView) findViewById(R.id.tv3);
        tv4 = (TextView) findViewById(R.id.tv4);

        Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.mytrans);
        tv1.startAnimation(myAnim);
        tv2.startAnimation(myAnim);
        tv3.startAnimation(myAnim);
        tv4.startAnimation(myAnim);

        final Intent mainIntent = new Intent(this, MainActivity.class);

        Thread timer = new Thread(){
            public void run(){
                try {
                    sleep(5000);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }

                finally {
                    startActivity(mainIntent);
                    finish();
                }
            }
        };

        timer.start();
    }
}
