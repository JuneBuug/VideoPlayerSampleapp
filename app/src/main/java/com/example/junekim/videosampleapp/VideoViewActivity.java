package com.example.junekim.videosampleapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;

@EActivity(R.layout.activity_videoview)
public class VideoViewActivity extends Activity {


    Context mContext = this;
    //
    final static String SAMPLE_VIDEO_URL = "https://s3-ap-northeast-1.amazonaws.com/imgs-bucket/kuan_about_nonsul2.mp4";
//    final static String SAMPLE_VIDEO_URL = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";

    Handler updateHandler = new Handler();

    @ViewById
    VideoView videoView;

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


    @AfterViews
    protected  void afterViews(){

        MediaController mc = new MediaController(this);
        videoView.setMediaController(mc);

        loadVideo(videoView);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void loadVideo(View view) {
        //Sample video URL : http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_2mb.mp4

        Toast.makeText(mContext, "비디오 로딩중", Toast.LENGTH_SHORT).show();
        videoView.setVideoURI(Uri.parse(SAMPLE_VIDEO_URL));
        videoView.requestFocus();

        // 토스트 다이얼로그를 이용하여 버퍼링중임을 알린다.
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {

                                        @Override
                                        public boolean onInfo(MediaPlayer mp, int what, int extra) {
                                            switch(what){
                                                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                                                    // Progress Diaglog 출력
                                                    Toast.makeText(mContext, "버퍼링 중 ", Toast.LENGTH_SHORT).show();
                                                    break;
                                                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                                                    // Progress Dialog 삭제
                                                    Toast.makeText(mContext, "버퍼링이 끝났습니다", Toast.LENGTH_SHORT).show();
                                                    videoView.start();
                                                    break;
                                            }
                                            return false;
                                        }
                                    }

        );

        // 플레이 준비가 되면, seekBar와 PlayTime을 세팅하고 플레이를 한다.
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
                long finalTime = videoView.getDuration();
//                TextView tvTotalTime = (TextView) findViewById(R.id.tvTotalTime);
//                tvTotalTime.setText(String.format("%d:%d",
//                        TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
//                        TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
//                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
//                );
//                seekBar.setMax((int) finalTime);
//                seekBar.setProgress(0);
//                updateHandler.postDelayed(updateVideoTime, 100);
                //Toast Box
                Toast.makeText(getApplicationContext(), "비디오를 재생합니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void playVideo(View view){
        videoView.requestFocus();
        videoView.start();

    }

    public void pauseVideo(View view){
        videoView.pause();
    }

//    // seekBar를 이동시키기 위한 쓰레드 객체
//    // 100ms 마다 viewView의 플레이 상태를 체크하여, seekBar를 업데이트 한다.
//    private Runnable updateVideoTime = new Runnable(){
//        public void run(){
//            long currentPosition = videoView.getCurrentPosition();
//            seekBar.setProgress((int) currentPosition);
//            updateHandler.postDelayed(this, 100);

//        }
//    };

}
