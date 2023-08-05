package com.example.vremenskaslika;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.DrawableImageViewTarget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private ImageView background;
    private ImageView radarImageView;


    private TextView utcTimeTextView;
    private TextView showStill;


    private Handler mainHandler;
    private boolean isStillImageShown = false;


    private final String radarImageUrl = "https://meteo.arso.gov.si/uploads/probase/www/observ/radar/si0-rm-anim.gif";
    private final String backgroundImage = "https://meteo.arso.gov.si/uploads/probase/www/observ/radar/si0-rm.gif";
    private final int gifLength = 5750;
    private final int updateAfter = 10;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        // Enable immersive fullscreen mode
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

        Configuration config = getResources().getConfiguration();

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            flags |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setSystemUiVisibility(flags);

        setContentView(R.layout.activity_main);

        utcTimeTextView = findViewById(R.id.utcTimeTextView);
        showStill = findViewById(R.id.showStill);

        radarImageView = findViewById(R.id.radarImageView);
        background = findViewById(R.id.background);


        mainHandler = new Handler(Looper.getMainLooper());


        showStill.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        radarImageView.setVisibility(View.GONE);
                        isStillImageShown = true;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        radarImageView.setVisibility(View.VISIBLE);
                        isStillImageShown = false;
                        break;
                }
                return true;
            }
        });

        // Update UTC time every second
        updateUTCTime();

        // Initial load of the image (load both so that it stays in ram)
        loadImageFromUrl(backgroundImage, background);
        loadImageFromUrl(radarImageUrl, radarImageView);


        // Schedule image refresh every 5 seconds
        refreshImage(radarImageUrl, radarImageView);
        try {Thread.sleep(gifLength / 2);}
        catch (InterruptedException e) {throw new RuntimeException(e);}
        refreshImage(backgroundImage, background);
    }


    @SuppressLint("SetTextI18n")
    private void updateUTCTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcTime = sdf.format(new Date());

        // Update the TextView with the new UTC time
        utcTimeTextView.setText("UTC čas:\n" + utcTime);

        // Schedule the update again after 1 second
        utcTimeTextView.postDelayed(this::updateUTCTime, 1000);
    }

    private void refreshImage(String imageLink, ImageView image) {
        mainHandler.postDelayed(() -> {
            if (!isStillImageShown) {
                loadImageFromUrl(imageLink, image);
            }
            refreshImage(imageLink, image); // Call the function again to refresh after x ms
        }, gifLength * updateAfter);
    }

    private void loadImageFromUrl(String imageUrl, ImageView image) {
        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable caching
                .skipMemoryCache(true) // Skip memory cache as well
                .into(new DrawableImageViewTarget(image));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null); // Remove callbacks to prevent leaks
    }
}