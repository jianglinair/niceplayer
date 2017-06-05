package com.lin.jiang.niceplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lin.jiang.niceplayer.R;
import com.lin.jiang.niceplayer.bean.Video;
import com.lin.jiang.niceplayerlib.DefaultMediaController;

import java.util.List;

/**
 * Created by jianglin on 17-6-5.
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoViewHolder> {
    private static final String TAG = "VideoAdapter";
    private Context mContext;
    private List<Video> mVideoList;

    public VideoAdapter(Context mContext, List<Video> mVideoList) {
        this.mContext = mContext;
        this.mVideoList = mVideoList;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: viewType=" + viewType);
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_video, parent, false);
        VideoViewHolder holder = new VideoViewHolder(itemView);
        DefaultMediaController controller = new DefaultMediaController(mContext);
        holder.setController(controller);
        return holder;
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        Video video = mVideoList.get(position);
        holder.bindData(video);
    }

    @Override
    public int getItemCount() {
        return mVideoList.size();
    }
}
