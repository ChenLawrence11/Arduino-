package com.example.arduinoalarm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class LauncherActivity extends AppCompatActivity {

    public static final int SPLASH_SCREEN = 2000;

    Animation bottomAnimation, topAnimation;
    TextView epilogueTV;
    ImageView portraitImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        init();

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                Intent intent = new Intent(LauncherActivity.this,MainActivity.class);
                startActivity(intent);
            }
        }, SPLASH_SCREEN);
    }

    public void init(){
        bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        topAnimation = AnimationUtils.loadAnimation(this,R.anim.top_animation);

        epilogueTV = findViewById(R.id.epilogue);
        portraitImg = findViewById(R.id.portraitImg);

        portraitImg.setAnimation(topAnimation);
        epilogueTV.setAnimation(bottomAnimation);
    }
}