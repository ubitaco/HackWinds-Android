package com.nucc.hackwinds.views;


import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.R;

import info.hoang8f.android.segmented.SegmentedGroup;

public class WaveWatchChartActivity extends AppCompatActivity implements SegmentedGroup.OnCheckedChangeListener {
    final private int WAVE_WATCH_HOUR_STEP = 3;
    final private int WAVE_WATCH_MAX_HOUR = 180;
    final private int WAVE_WATCH_MIN_HOUR = 0;
    final private int WAVE_WATCH_IMAGE_COUNT = (WAVE_WATCH_MAX_HOUR / WAVE_WATCH_HOUR_STEP) + 1;


    private enum WaveWatchChartType {
        WAVES,
        SWELL,
        PERIOD,
        WIND
    }

    private WaveWatchChartType mWaveWatchChartType;
    private AnimationDrawable mChartAnimation;
    private FutureCallback<Bitmap> mChartLoadCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave_watch_chart);

        // Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the segmented control view
        SegmentedGroup chartGroup = (SegmentedGroup) findViewById(R.id.wavewatch_segment_group);
        chartGroup.setTintColor(ContextCompat.getColor(getApplicationContext(), R.color.hackwinds_blue));
        chartGroup.setOnCheckedChangeListener(this);

        // Hide the play button and set the click listener
        ImageView chartPlayButton = (ImageView) findViewById(R.id.wavewatch_animate_play_button);
        chartPlayButton.setVisibility(View.GONE);
        chartPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Hide the play button
                view.setVisibility(View.GONE);

                // Start the animation
                mChartAnimation.start();
            }
        });

        // Set clicking the imageview to act as a pause button
        ImageView chartImage = (ImageView) findViewById(R.id.wave_watch_chart_image);
        chartImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mChartAnimation.isRunning()) {
                    // Only call to stop the animation if it is currently running
                    mChartAnimation.stop();

                    // Show the play button
                    ImageView playButton = (ImageView) findViewById(R.id.wavewatch_animate_play_button);
                    playButton.setVisibility(View.VISIBLE);
                }
            }
        });

        // Create the chart animation instance
        mChartAnimation = new AnimationDrawable();

        // Create chart loading callback
        mChartLoadCallback = new FutureCallback<Bitmap>() {
            @Override
            public void onCompleted(Exception e, Bitmap result) {
                BitmapDrawable chartFrame = new BitmapDrawable(getResources(), result);
                mChartAnimation.addFrame(chartFrame, 500);

                int nFrames = mChartAnimation.getNumberOfFrames();
                if (nFrames == 1) {
                    // Set the chart preview image as bitmap that was just received
                    ImageView chartImage = (ImageView) findViewById(R.id.wave_watch_chart_image);
                    chartImage.setImageDrawable(chartFrame);

                } else if (nFrames == WAVE_WATCH_IMAGE_COUNT) {
                    // Set the animation drawable as the imageview background
                    ImageView chartImage = (ImageView) findViewById(R.id.wave_watch_chart_image);
                    chartImage.setImageDrawable(mChartAnimation);

                    // Show the play button
                    ImageView playButton = (ImageView) findViewById(R.id.wavewatch_animate_play_button);
                    playButton.setVisibility(View.VISIBLE);
                }

                if (nFrames < WAVE_WATCH_IMAGE_COUNT) {
                    // Load the next image
                    getChartImageForIndex(mWaveWatchChartType, nFrames);
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Show the swell chart when the fragment is launched
        RadioButton wavesButton = (RadioButton) findViewById(R.id.wavewatch_waves_mode_segment_button);
        wavesButton.setChecked(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mChartAnimation.isRunning()) {
            // Stop the animation if it is still running
            mChartAnimation.stop();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int index) {
        // Start loading the new images.
        if (index == R.id.wavewatch_waves_mode_segment_button ) {
            mWaveWatchChartType = WaveWatchChartType.WAVES;
        } else if (index == R.id.wavewatch_swell_mode_segment_button) {
            mWaveWatchChartType = WaveWatchChartType.SWELL;
        } else if (index == R.id.wavewatch_period_mode_segment_button) {
            mWaveWatchChartType = WaveWatchChartType.PERIOD;
        } else if (index == R.id.wavewatch_wind_mode_segment) {
            mWaveWatchChartType = WaveWatchChartType.WIND;
        } else {
            return;
        }

        if (mChartAnimation.isRunning()) {
            mChartAnimation.stop();
        }

        // Remove the animation from the imageview
        findViewById(R.id.wave_watch_chart_image).setBackground(null);

        // Reset the chart animation object
        mChartAnimation = new AnimationDrawable();
        mChartAnimation.setOneShot(false);
        getChartImageForIndex(mWaveWatchChartType, 0);
    }

    public void getChartImageForIndex(WaveWatchChartType waveWatchChartType, int index) {
        final String BASE_URL = "http://polar.ncep.noaa.gov/waves/WEB/multi_1.latest_run/plots/US_eastcoast.%s.%s%03dh.png";
        final String PAST_HOUR_TIME_PREFIX = "h";
        final String FUTURE_HOUR_TIME_PREFIX = "f";

        // Format the url for the next image load
        final String chartTimePrefix;
        if (index == 0) {
            chartTimePrefix = PAST_HOUR_TIME_PREFIX;
        } else {
            chartTimePrefix = FUTURE_HOUR_TIME_PREFIX;
        }
        final String chartTypePrefix = getChartURLPrefix(waveWatchChartType);
        final String nextImageURL = String.format(BASE_URL, chartTypePrefix, chartTimePrefix, index * WAVE_WATCH_HOUR_STEP);

        // Load the next image
        Ion.with(this).load(nextImageURL).asBitmap().setCallback(mChartLoadCallback);
    }

    private String getChartURLPrefix(WaveWatchChartType waveWatchChartType) {
        switch (waveWatchChartType) {
            case WAVES:
                return "hs";
            case SWELL:
                return "hs_sw1";
            case PERIOD:
                return "tp_sw1";
            case WIND:
                return "u10";
            default:
                return "";
        }
    }

}
