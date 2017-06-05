package com.lin.jiang.niceplayer.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.lin.jiang.niceplayer.R;
import com.lin.jiang.niceplayer.bean.Video;
import com.lin.jiang.niceplayerlib.NiceMediaPlayer;
import com.lin.jiang.niceplayerlib.base.AbstractMediaController;

/**
 * Created by jianglin on 17-6-5.
 */

public class VideoViewHolder extends RecyclerView.ViewHolder {

    private NiceMediaPlayer mNiceMediaPlayer;
    private AbstractMediaController mController;

    public VideoViewHolder(View itemView) {
        super(itemView);
        mNiceMediaPlayer = (NiceMediaPlayer) itemView.findViewById(R.id.nmp_media_player);
    }

    public void setController(AbstractMediaController controller) {
        mController = controller;
    }

    public void bindData(Video video) {
        mController.setTitle(video.getTitle());
        mController.setImage(video.getImgUrl());
        mNiceMediaPlayer.setUp(video.getVideoUrl(), null);
        ViewGroup viewGroup = (ViewGroup) mController.getParent();
        if (viewGroup != null) {
            viewGroup.removeView(mController);
        }
        mNiceMediaPlayer.setController(mController);
    }
}
