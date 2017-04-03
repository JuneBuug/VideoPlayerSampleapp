package com.example.junekim.videosampleapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioTrack;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.net.URI;

@EActivity(R.layout.activity_exoplayer)
public class ExoplayerActivity extends Activity {

    SimpleExoPlayer player;
    Context mContext = this;
    OrientationEventListener orientEventListener;
    Boolean is_locked=false;
    //
    final static String SAMPLE_VIDEO_URL = "https://s3-ap-northeast-1.amazonaws.com/imgs-bucket/kuan_about_nonsul2.mp4";
//    final static String SAMPLE_VIDEO_URL = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";

    Float current_playbackrate=1.0f;
    @ViewById
    SimpleExoPlayerView exo_view;

    @ViewById
    ScrollView video_info;

    @ViewById
    TextView lock,playback_rate_control;

    @ViewById
    ImageView plus,minus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Click
    void plus(){

        PlaybackParams params = new PlaybackParams();
        current_playbackrate+=0.1f;
        params.setSpeed(current_playbackrate);
        player.setPlaybackParams(params);
        String str = String.format("%.1f",current_playbackrate);
        playback_rate_control.setText(str+" 배속");

    }

    @TargetApi(Build.VERSION_CODES.M)
    @Click
    void minus(){

        PlaybackParams params = new PlaybackParams();
        current_playbackrate-=0.1f;
        params.setSpeed(current_playbackrate);
        String str = String.format("%.1f",current_playbackrate);
        playback_rate_control.setText(str+" 배속");
    }


    @Click
    void video_layout(){
        plus.setVisibility(View.VISIBLE);
        minus.setVisibility(View.VISIBLE);
        playback_rate_control.setVisibility(View.VISIBLE);
        lock.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fadeOutAndHideImage(plus);
                fadeOutAndHideImage(minus);
                fadeOutAndHideImage(playback_rate_control);
                fadeOutAndHideImage(lock);
            }
        },2000);

    }
    @Click
    void lock(){
        if(!is_locked){
           String orientation = getRotation(mContext);

            switch (orientation){
                case "portrait" :  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); break;
                case "landscape" :  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); break;
                case "reverse portrait" :  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT); break;
                case "reverse landscape" :  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE); break;

            }

            is_locked=true;
            lock.setText("방향 고정해제");
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            is_locked=false;
            lock.setText("방향 고정");
        }
    }


    public String getRotation(Context context){
        final int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return "portrait";
            case Surface.ROTATION_90:
                return "landscape";
            case Surface.ROTATION_180:
                return "reverse portrait";
            default:
                return "reverse landscape";
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @AfterViews
    protected  void afterViews(){


        orientEventListener = new OrientationEventListener(mContext, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {

                if(orientation==0||orientation==180){
                    video_info.setVisibility(View.VISIBLE);
                }else{
                    video_info.setVisibility(View.GONE);
                }

            }
        };


        // 1. Create a default TrackSelector
        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);

        // 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

     // 3. Create the player
        player =
                ExoPlayerFactory.newSimpleInstance(mContext, trackSelector, loadControl);

        exo_view.setPlayer(player);


        // Measures bandwidth during playback. Can be null if not required. 재생 중에 대역폭을 측정합니다. 필요하지 않은 경우 null 일 수 있습니다.
        DefaultBandwidthMeter bandwidthMeter2 = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded. 미디어 데이터가로드되는 DataSource 인스턴스를 생성합니다.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "yourApplicationName"), bandwidthMeter2);
        // Produces Extractor instances for parsing the media data. 미디어 데이터의 구문 분석을 위해 Extractor 인스턴스를 생성합니다.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        // This is the MediaSource representing the media to be played. 이것은 재생할 미디어를 나타내는 MediaSource입니다.

        Uri uri = Uri.parse(SAMPLE_VIDEO_URL);
        MediaSource videoSource = new ExtractorMediaSource(uri,
                dataSourceFactory, extractorsFactory, null, null);

        // Prepare the player with the source. 소스로 플레이어를 준비하십시오.
        player.prepare(videoSource);

        player.setPlayWhenReady(true);

        PlaybackParams params = new PlaybackParams();
        params.setSpeed(1.0f);
        player.setPlaybackParams(params);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                fadeOutAndHideImage(plus);
//                fadeOutAndHideImage(minus);
//                fadeOutAndHideImage(playback_rate_control);
//                fadeOutAndHideImage(lock);
//            }
//        },5000);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        player.release();
    }

    @Override
    protected void onStop(){
        super.onStop();
        player.release();
    }

    private void fadeOutAndHideImage(final View view)
    {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(500);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                view.setVisibility(View.GONE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        view.startAnimation(fadeOut);
    }

}
