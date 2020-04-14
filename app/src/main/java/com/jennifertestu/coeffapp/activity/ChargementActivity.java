package com.jennifertestu.coeffapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
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

        // Récupération de l'année active dans les préférences
        SharedPreferences prefs = getSharedPreferences("annee_active", MODE_PRIVATE);
        final int check = prefs.getInt("id", -1);//"No name defined" is the default value.

        // redirection sur l'acti principale
        // envoi de l'instruction après 3 sec
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // demarrer page

                if(check==-1) {
                    Intent intent = new Intent(getApplicationContext(), AideActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, TIMEOUT);

        TextView tv = findViewById(R.id.textApp);

        ObjectAnimator textColorAnim = ObjectAnimator.ofInt(tv, "textColor", Color.parseColor("#006F61"), Color.WHITE);
        textColorAnim.setDuration(3000);
        textColorAnim.setEvaluator(new ArgbEvaluator());
        textColorAnim.start();

    }


}
