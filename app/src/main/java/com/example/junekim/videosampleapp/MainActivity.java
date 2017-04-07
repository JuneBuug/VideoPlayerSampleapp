package com.example.junekim.videosampleapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.junekim.videosampleapp.Data.VideoListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {


    List<VideoListView> videos = new ArrayList<VideoListView>();
    ListViewAdapter mAdapter;
    Context mContext =this;
    @ViewById
    ListView list_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new ListViewAdapter();
    }

    @AfterViews
    protected void afterViews(){
        VideoListView a = new VideoListView();
        a.title="Exoplayer";
        a.content = "구글이 추천하는 동영상 재생 라이브러리.\n미디어 코댁 오디오 트랙 ,미디어DRM등을 지원한다고합니다. 연속 재생도 가능합니다.";


        VideoListView b= new VideoListView();
        b.title="Exoplayer_1분 미리보기";
        b.content = "1분 미리보기";


        videos.add(a);
        videos.add(b);

        mAdapter.setData(videos);
        list_view.setAdapter(mAdapter);

    }


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
                convertView = inflater.inflate(R.layout.view_video_list, null);

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



            if(video.title.equals("VideoView")){
                holder.whole_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        VideoViewActivity_.intent(mContext).start();
                    }
                });
            }


//            if(video.title.equals("기본 동영상 플레이어")){
//                holder.whole_view.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        String SAMPLE_VIDEO_URL = "https://s3-ap-northeast-1.amazonaws.com/imgs-bucket/kuan_about_nonsul2.mp4";
//                        Intent intent = new Intent(Intent.ACTION_VIEW);
//                        Uri uri = Uri.parse(SAMPLE_VIDEO_URL);
//                        intent.setDataAndType(uri,"video/mp4");
//                        startActivity(intent);
//
//                    }
//                });
//            }

            if(video.title.equals("Exoplayer")){
                holder.whole_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ExoplayerActivity_.intent(mContext)
                                .extra("one_min",false)
                                .start();


                    }
                });
            }

            if(video.title.contains("1분")){
                holder.whole_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ExoplayerActivity_.intent(mContext)
                                .extra("one_min",true)
                                .start();


                    }
                });
            }
            return convertView;
        }

        private void setViewHolder(View convertView, LikeViewHolder holder) {
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.content = (TextView) convertView.findViewById(R.id.content);
            holder.whole_view = (RelativeLayout) convertView.findViewById(R.id.whole_view);
        }
    }

    private class LikeViewHolder {
        TextView title,content;
        RelativeLayout whole_view;
    }

}
