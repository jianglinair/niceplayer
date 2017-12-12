package com.lin.jiang.niceplayer.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.lin.jiang.niceplayer.R;
import com.lin.jiang.niceplayer.bean.Video;
import com.lin.jiang.niceplayerlib.NiceVideoView;
import com.lin.jiang.niceplayerlib.base.AbstractMediaController;

/**
 * Created by jianglin on 17-6-5.
 */

public class VideoViewHolder extends RecyclerView.ViewHolder {

    private NiceVideoView mNiceVideoView;
    private AbstractMediaController mController;

    public VideoViewHolder(View itemView) {
        super(itemView);
        mNiceVideoView = (NiceVideoView) itemView.findViewById(R.id.nvv_nice_video_view);
    }

    public void setController(AbstractMediaController controller) {
        mController = controller;
    }

    public void bindData(Video video) {
        mController.setTitle(video.getTitle());
        mController.setImage(video.getImgUrl());
        mNiceVideoView.setUp(video.getVideoUrl(), null);
        ViewGroup viewGroup = (ViewGroup) mController.getParent();
        if (viewGroup != null) {
            viewGroup.removeView(mController);
        }
        mNiceVideoView.setController(mController);
    }
}
