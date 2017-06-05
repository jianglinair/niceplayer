package com.lin.jiang.niceplayerlib.base;

import android.util.Log;

/**
 * 同一界面上有多个视频，或者视频放在ReclerView或者ListView的容器中，要保证同一时刻只有一个视频在播放，
 * 其他的都是初始状态，所以需要一个MediaPlayerManager来管理播放器，主要功能是保存当前已经开始了的播放器。
 * <p>
 * Created by jianglin on 17-6-1.
 */

public class MediaPlayerManager {

    private static final String TAG = "MediaPlayerManager";
    private static MediaPlayerManager sManager;
    private IMediaPlayer mMediaPlayer;

    private MediaPlayerManager() {
    }

    public static synchronized MediaPlayerManager getInstance() {
        if (sManager == null)
            sManager = new MediaPlayerManager();
        return sManager;
    }

    public void setMediaPlayer(IMediaPlayer player) {
        mMediaPlayer = player;
        Log.d(TAG, "setMediaPlayer: " + player);
    }

    public void releasePlayer() {
        Log.d(TAG, "releasePlayer: " + mMediaPlayer);
        if (mMediaPlayer != null)
            mMediaPlayer.release();
        mMediaPlayer = null;
    }

    /**
     * onBackPressed供Activity中用户按返回键时调用
     *
     * @return
     */
    public boolean onBackPressd() {
        Log.d(TAG, "onBackPressd: ");
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isFullScreen())
                return mMediaPlayer.exitFullScreen();
            if (mMediaPlayer.isTinyWindow())
                return mMediaPlayer.exitTinyWindow();
            mMediaPlayer.release();
            return false;
        }
        return false;
    }
}
