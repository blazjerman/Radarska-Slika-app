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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.DrawableImageViewTarget;

public class MainActivity extends AppCompatActivity {

    private ImageView background;
    private ImageView radarImageView;
    private ImageView stillRadarImageView;


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
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

        Configuration config = getResources().getConfiguration();

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            flags |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setSystemUiVisibility(flags);

        setContentView(R.layout.activity_main);

        radarImageView = findViewById(R.id.radarImageView);
        stillRadarImageView = findViewById(R.id.stillRadarImageView);
        background = findViewById(R.id.background);

        mainHandler = new Handler(Looper.getMainLooper());

        // Set touch listener for the image
        radarImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Finger pressed, show the still image
                        loadImageFromUrl(stillRadarImageUrl, stillRadarImageView);
                        isStillImageShown = true;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Remove image
                        stillRadarImageView.setImageDrawable(null);
                        isStillImageShown = false;
                        break;
                }
                return true;
            }
        });

        // Initial load of the image (load both so that it stays in ram)
        loadImageFromUrl(radarImageUrl, radarImageView);
        loadImageFromUrl(stillRadarImageUrl, background);

        // Schedule image refresh every 5 seconds
        refreshImage(radarImageUrl, radarImageView);
        try {Thread.sleep(gifLength / 2);}
        catch (InterruptedException e) {throw new RuntimeException(e);}
        refreshImage(stillRadarImageUrl, background);
    }

    private void refreshImage(String imageLink, ImageView image) {
        mainHandler.postDelayed(() -> {
            if (!isStillImageShown) {
                loadImageFromUrl(imageLink, image);
            }
            refreshImage(imageLink, image); // Call the function again to refresh after x ms
        }, gifLength);
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