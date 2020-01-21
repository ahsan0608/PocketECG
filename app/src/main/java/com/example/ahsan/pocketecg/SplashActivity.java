package com.example.ahsan.pocketecg;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import gr.net.maroulis.library.EasySplashScreen;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EasySplashScreen config = new EasySplashScreen(SplashActivity.this)
                .withFullScreen()
                .withTargetActivity(MainActivity.class)
                .withSplashTimeOut(2000)
                .withBackgroundColor(Color.WHITE)
                .withLogo(R.drawable.ecg3)
                .withAfterLogoText("Pocket ECG");


        config.getAfterLogoTextView().setTextColor(Color.BLACK);


        View easySplashScreen = config.create();

        setContentView(easySplashScreen);
    }
}
