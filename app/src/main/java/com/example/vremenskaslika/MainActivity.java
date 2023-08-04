package com.example.vremenskaslika;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;

public class MainActivity extends AppCompatActivity {

    private ImageView weatherImageView;
    private Handler mainHandler;
    private boolean isStillImageShown = false;


    private final String radarImageUrl = "https://meteo.arso.gov.si/uploads/probase/www/observ/radar/si0-rm-anim.gif";
    private final String stillRadarImageUrl = "https://meteo.arso.gov.si/uploads/probase/www/observ/radar/si0-rm.gif";
    private final int gifLength = 5750;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Enable immersive fullscreen mode
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(flags);

        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main);

        weatherImageView = findViewById(R.id.weatherImageView);
        mainHandler = new Handler(Looper.getMainLooper());

        // Set touch listener for the image
        weatherImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Finger pressed, show the still image
                        loadImageFromUrl(stillRadarImageUrl);
                        isStillImageShown = true;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Finger released, restore the original image
                        loadImageFromUrl(radarImageUrl);
                        isStillImageShown = false;
                        break;
                }
                return true;
            }
        });

        // Initial load of the image (load both so that it stays in ram)
        loadImageFromUrl(radarImageUrl);

        // Schedule image refresh every 5 seconds
        refreshImage(radarImageUrl);
    }

    private void refreshImage(String imageLink) {
        mainHandler.postDelayed(() -> {
            if (!isStillImageShown) {
                loadImageFromUrl(imageLink);
            }
            refreshImage(imageLink); // Call the function again to refresh after x ms
        }, gifLength);
    }

    private void loadImageFromUrl(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .into(new DrawableImageViewTarget(weatherImageView));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null); // Remove callbacks to prevent leaks
    }
}