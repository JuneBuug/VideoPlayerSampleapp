package com.example.junekim.videosampleapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.hardware.SensorManager;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.junekim.videosampleapp.Data.VideoListView;
import com.example.junekim.videosampleapp.View.CustomSimpleExoPlayerView;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_exoplayer)
public class ExoplayerActivity extends Activity {

    SimpleExoPlayer player;
    List<VideoListView> videos = new ArrayList<VideoListView>();
    Context mContext = this;
    OrientationEventListener orientEventListener;
    Boolean is_locked=false;
    ListViewAdapter mAdapter;
    MaterialDialog mDialog,mDialog2;



    String SAMPLE_VIDEO_URL = "";

    Float current_playbackrate=1.0f;


    @Extra("one_min")
    boolean isOne;

    @ViewById
    CustomSimpleExoPlayerView exo_view;

    @ViewById
    ScrollView lecture_info;

    @ViewById
    TextView lock,playback_rate_control,tab1_text,tab2_text,buy_video;

    @ViewById
    RelativeLayout video_layout;

    @ViewById
    LinearLayout playback_layout,tab1,tab2;

    @ViewById
    View tab1_line,tab2_line;

    @ViewById
    ListView other_videos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(mContext)) {

            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        }
                //4버전에서 wake_lock
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mAdapter = new ListViewAdapter();

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

    @Override
    protected void onResume(){
        super.onResume();
        player.setPlayWhenReady(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @AfterViews
    protected  void afterViews(){


        if(isOne){
            buy_video.setVisibility(View.VISIBLE);
        }else{
            buy_video.setVisibility(View.INVISIBLE);
        }

        VideoListView a = new VideoListView();
        a.title="콴쌤의 논술강의";
        a.content = "논술강의";


        VideoListView b= new VideoListView();
        b.title="탄핵 미리보기";
        b.content = "탄핵 법정 개념으로 뽀개기";

        VideoListView c= new VideoListView();
        c.title="논술, 나도 해도 되는걸까";
        c.content = "논술 전형 지원 가늠해보기";


        videos.add(a);
        videos.add(b);
        videos.add(c);

        mAdapter.setData(videos);
        other_videos.setAdapter(mAdapter);




        orientEventListener = new OrientationEventListener(mContext, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {

                if(orientation==0||orientation==180){
                    lecture_info.setVisibility(View.VISIBLE);
                    other_videos.setVisibility(View.GONE);
                }else{
                    lecture_info.setVisibility(View.GONE);
                    other_videos.setVisibility(View.GONE);
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
                Util.getUserAgent(this, "ExoVideoFactory"), bandwidthMeter2);
        // Produces Extractor instances for parsing the media data. 미디어 데이터의 구문 분석을 위해 Extractor 인스턴스를 생성합니다.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        // This is the Media
        //
        // Source representing the media to be played. 이것은 재생할 미디어를 나타내는 MediaSource입니다.

        Uri uri = Uri.parse(SAMPLE_VIDEO_URL);
        MediaSource videoSource = new ExtractorMediaSource(uri,
                dataSourceFactory, extractorsFactory, null, null);

        // Prepare the player with the source. 소스로 플레이어를 준비하십시오.
        player.prepare(videoSource);

        player.setPlayWhenReady(true);


        //오디오 포커스
        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        // Request audio focus for playback
        int result = am.requestAudioFocus(focusChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            try {

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
            playback_layout.setVisibility(View.VISIBLE);
            PlaybackParams params = new PlaybackParams();
            params.setSpeed(1.0f);
            player.setPlaybackParams(params);
        }else{
            playback_layout.setVisibility(View.GONE);
        }




        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                if(playbackState == ExoPlayer.STATE_ENDED &&isOne){
                    if(mDialog==null){
                        mDialog = new MaterialDialog.Builder(mContext)
                                .title("강의 구매")
                                .content("이 강의를 구매하고 더 보시겠습니까?")
                                .positiveText("구매하기")
                                .negativeText("취소")
                                .onPositive(new MaterialDialog.SingleButtonCallback(){
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        ExoplayerActivity_
                                                .intent(mContext)
                                                .extra("one_min",false)
                                                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                .start();
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback(){

                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        mDialog.dismiss();
                                    }
                                })
                                .show();
                    }else{
                        mDialog.show();
                    }
                }


            }

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity() {

            }
        });

    }


    @Click
    void buy_video(){
        if(mDialog2==null){
            mDialog2 = new MaterialDialog.Builder(mContext)
                    .title("강의 구매")
                    .content("이 강의를 구매하고 더 보시겠습니까?")
                    .positiveText("구매하기")
                    .negativeText("취소")
                    .onPositive(new MaterialDialog.SingleButtonCallback(){
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            ExoplayerActivity_
                                    .intent(mContext)
                                    .extra("one_min",false)
                                    .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    .start();
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback(){

                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mDialog2.dismiss();
                        }
                    })
                    .show();
        }else{
            mDialog2.show();
        }
    }

    @Click
    void tab1(){
        tab1_text.setTextColor(getResources().getColor(R.color.colorPrimary));
        tab2_text.setTextColor(getResources().getColor(R.color.colornone));
        tab1_line.setVisibility(View.VISIBLE);
        tab2_line.setVisibility(View.INVISIBLE);
        lecture_info.setVisibility(View.VISIBLE);
        other_videos.setVisibility(View.GONE);

    }


    @Click
    void tab2(){
        tab1_text.setTextColor(getResources().getColor(R.color.colornone));
        tab2_text.setTextColor(getResources().getColor(R.color.colorPrimary));
        tab1_line.setVisibility(View.INVISIBLE);
        tab2_line.setVisibility(View.VISIBLE);
        lecture_info.setVisibility(View.GONE);
        other_videos.setVisibility(View.VISIBLE);

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
    void fullscreen(){
        String orientation = getRotation(mContext);

        if(Settings.System.getInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION, 0) == 1){
            Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        }else{
            Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);

        }

        switch (orientation){
            case "portrait" :  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);break;
            case "landscape" :  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);break;
            case "reverse portrait" :  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);break;
            case "reverse landscape" :  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED); break;

        }

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


    /**
     * 오디오 포커스 리스너
     */
    private AudioManager.OnAudioFocusChangeListener focusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    AudioManager am =(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    switch (focusChange) {
                        case (AudioManager.AUDIOFOCUS_LOSS) :
                            player.setPlayWhenReady(false);
                            break;
                        case (AudioManager.AUDIOFOCUS_GAIN) :
                            player.setPlayWhenReady(true);
                            break;
                        default: break;
                    }
                }
            };




    /**
     * 이 선생님의 다른 영상 리스트 어댑터
     */
    private class ListViewAdapter extends BaseAdapter {
        private List<VideoListView> videos = new ArrayList<>();

        public void setData(List<VideoListView> videos) {
            if (videos == null) {
                return;
            }
            this.videos = videos;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return videos.size();
        }

        @Override
        public Object getItem(int position) {
            return videos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final LikeViewHolder holder;
            final VideoListView video = videos.get(position);

            if (convertView == null) {
                holder = new LikeViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.view_other_video_list, null);

                setViewHolder(convertView, holder);

                convertView.setTag(holder);
            } else {
                holder = (LikeViewHolder) convertView.getTag();
            }


            if(video.title!=null){
                holder.title.setText(video.title);
            }


            if(video.content!=null){
                holder.content.setText(video.content);
            }


            holder.video_cover.setImageDrawable(getResources().getDrawable(R.drawable.video_cover1));


            return convertView;
        }

        private void setViewHolder(View convertView, LikeViewHolder holder) {
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.content = (TextView) convertView.findViewById(R.id.content);
            holder.whole_view = (RelativeLayout) convertView.findViewById(R.id.whole_view);
            holder.video_cover = (ImageView) convertView.findViewById(R.id.video_cover);

        }
    }

    private class LikeViewHolder {
        TextView title,content;
        RelativeLayout whole_view;
        ImageView video_cover;
    }


}
