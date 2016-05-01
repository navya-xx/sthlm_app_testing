package com.example.ashutosh.mapping;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.LinearLayout;

public class FullscreenActivity extends Activity {
    //DB Class to perform DB related operations
    MyDbHandler controller = new MyDbHandler(this, null, null, 1);

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 1000;
    private static boolean splashLoaded = false;
    static boolean touch=false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // check that the user data is created in the database


        if (!splashLoaded) {

            setContentView(R.layout.activity_fullscreen);
            LinearLayout rLayout = (LinearLayout) findViewById(R.id.screen);
            splashLoaded = true;

            rLayout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    touch=true;
                    startActivity(new Intent(FullscreenActivity.this, List.class));
                }

            });

           transition();

        } else {
            Intent goToMainActivity = new Intent(FullscreenActivity.this, List.class);
            goToMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(goToMainActivity);
            finish();
        }
    }

    public void transition() {
        final int millisecDelayed = 6000;
        new CountDownTimer(millisecDelayed, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (touch == false)
                    startActivity(new Intent(FullscreenActivity.this, List.class));
            }
        }.start();
    }

}