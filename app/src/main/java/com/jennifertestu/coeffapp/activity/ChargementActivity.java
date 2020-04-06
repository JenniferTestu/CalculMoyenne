package com.jennifertestu.coeffapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.jennifertestu.coeffapp.R;

public class ChargementActivity extends AppCompatActivity {

    private final int TIMEOUT = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chargement);

        // redirection sur l'acti principale

        // envoi de l'instruction après 3 sec
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // demarrer page
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, TIMEOUT);

        TextView tv = findViewById(R.id.textApp);

        ObjectAnimator textColorAnim = ObjectAnimator.ofInt(tv, "textColor", Color.parseColor("#006F61"), Color.WHITE);
        textColorAnim.setDuration(3000);
        textColorAnim.setEvaluator(new ArgbEvaluator());
        textColorAnim.start();

    }


}
