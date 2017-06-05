package com.lin.jiang.niceplayerlib;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lin.jiang.niceplayerlib.base.AbstractMediaController;
import com.lin.jiang.niceplayerlib.base.CommonUtil;
import com.lin.jiang.niceplayerlib.base.IMediaPlayer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 可以使用DefaultMediaController，也可以自定义
 * <p>
 * Created by jianglin on 17-6-1.
 */

public class DefaultMediaController extends AbstractMediaController implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "NiceMediaPlayer";

    private Context mContext;
    private IMediaPlayer mMediaPlayer; // 实际使用的是NicePlayer

    private ImageView mImage;
    private ImageView mCenterStart;

    private LinearLayout mTop;
    private ImageView mBack;
    private TextView mTitle;

    private LinearLayout mBottom;
    private ImageView mRestartPause;
    private TextView mPosition;
    private TextView mDuration;
    private SeekBar mSeekBar;
    private ImageView mFullScreen;

    private LinearLayout mLoading;
    private TextView mLoadText;

    private LinearLayout mError;
    private TextView mRetry;

    private LinearLayout mCompleted;
    private TextView mReplay;
    private TextView mShare;

    private boolean topBottomVisible;
    private Timer mUpdateProgressTimer;
    private TimerTask mUpdateProgressTimerTask;
    private CountDownTimer mDismissTopBottomCountDownTimer;

    public DefaultMediaController(@NonNull Context context) {
        super(context);
        mContext = context;
        init();
        setOnClickListener();
    }

    private void init() {
        LayoutInflater.from(mContext).inflate(R.layout.nice_player_controller, this, true);
        mImage = (ImageView) findViewById(R.id.iv_image);
        mCenterStart = (ImageView) findViewById(R.id.iv_center_start);

        mTop = (LinearLayout) findViewById(R.id.ll_top);
        mBack = (ImageView) findViewById(R.id.iv_back);
        mTitle = (TextView) findViewById(R.id.tv_title);

        mBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        mRestartPause = (ImageView) findViewById(R.id.iv_restart_or_pause);
        mPosition = (TextView) findViewById(R.id.tv_position);
        mDuration = (TextView) findViewById(R.id.tv_duration);
        mSeekBar = (SeekBar) findViewById(R.id.sb_seek);
        mFullScreen = (ImageView) findViewById(R.id.iv_full_screen);

        mLoading = (LinearLayout) findViewById(R.id.ll_loading);
        mLoadText = (TextView) findViewById(R.id.tv_loading);

        mError = (LinearLayout) findViewById(R.id.ll_error);
        mRetry = (TextView) findViewById(R.id.tv_retry);

        mCompleted = (LinearLayout) findViewById(R.id.ll_completed);
        mReplay = (TextView) findViewById(R.id.tv_replay);
        mShare = (TextView) findViewById(R.id.tv_share);

        Log.d(TAG, "init: done");
    }

    private void setOnClickListener() {
        mCenterStart.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mRestartPause.setOnClickListener(this);
        mRetry.setOnClickListener(this);
        mReplay.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mFullScreen.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
        this.setOnClickListener(this);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
        Log.d(TAG, "setTitle: " + title);
    }

    public void setImage(String imgUrl) {
        Glide.with(mContext).load(imgUrl).placeholder(R.drawable.img_default).crossFade().into(mImage);
        Log.d(TAG, "setImage: " + imgUrl);
    }

    public void setImage(@DrawableRes int resId) {
        mImage.setImageResource(resId);
        Log.d(TAG, "setImage: " + resId);
    }

    public void setMediaPlayer(IMediaPlayer player) {
        mMediaPlayer = player;
        if (mMediaPlayer.isIdle()) {
            mBack.setVisibility(GONE);
            mTop.setVisibility(VISIBLE);
            mBottom.setVisibility(GONE);
        }
        Log.d(TAG, "setMediaPlayer: " + player);
    }

    /**
     * 修改MediaController状态，以此修改其UI
     *
     * @param playerState 播放器状态(全屏，正常，小窗口)
     * @param playState   播放状态
     */
    public void setControllerState(int playerState, int playState) {
        Log.d(TAG, "setControllerState: playerState=" + playerState + ", playState" + playState);
        switch (playerState) {
            case NiceMediaPlayer.PLAYER_NORMAL:
                mBack.setVisibility(View.GONE);
                mFullScreen.setVisibility(View.VISIBLE);
                mFullScreen.setImageResource(R.drawable.ic_player_enlarge);
                break;
            case NiceMediaPlayer.PLAYER_FULL_SCREEN:
                mBack.setVisibility(View.VISIBLE);
                mFullScreen.setVisibility(View.VISIBLE);
                mFullScreen.setImageResource(R.drawable.ic_player_shrink);
                break;
            case NiceMediaPlayer.PLAYER_TINY_WINDOW:
                mFullScreen.setVisibility(View.GONE);
                break;
        }
        switch (playState) {
            case NiceMediaPlayer.STATE_IDLE:
                break;
            case NiceMediaPlayer.STATE_PREPARING:
                // 只显示准备中动画，其他不显示
                mImage.setVisibility(View.GONE);
                mLoading.setVisibility(View.VISIBLE);
                mLoadText.setText("正在准备...");
                mError.setVisibility(View.GONE);
                mCompleted.setVisibility(View.GONE);
                mTop.setVisibility(View.GONE);
                mCenterStart.setVisibility(View.GONE);
                break;
            case NiceMediaPlayer.STATE_PREPARED:
                startUpdateProgressTimer();
                break;
            case NiceMediaPlayer.STATE_PLAYING:
                mLoading.setVisibility(View.GONE);
                mRestartPause.setImageResource(R.drawable.ic_player_pause);
                startDismissTopBottomTimer();
                break;
            case NiceMediaPlayer.STATE_PAUSED:
                mLoading.setVisibility(View.GONE);
                mRestartPause.setImageResource(R.drawable.ic_player_start);
                cancelDismissTopBottomTimer();
                break;
            case NiceMediaPlayer.STATE_BUFFERING_PLAYING:
                mLoading.setVisibility(View.VISIBLE);
                mRestartPause.setImageResource(R.drawable.ic_player_pause);
                mLoadText.setText("正在缓冲...");
                startDismissTopBottomTimer();
                break;
            case NiceMediaPlayer.STATE_BUFFERING_PAUSED:
                mLoading.setVisibility(View.VISIBLE);
                mRestartPause.setImageResource(R.drawable.ic_player_start);
                mLoadText.setText("正在缓冲...");
                cancelDismissTopBottomTimer();
            case NiceMediaPlayer.STATE_COMPLETED:
                cancelUpdateProgressTimer();
                setTopBottomVisible(false);
                mImage.setVisibility(View.VISIBLE);
                mCompleted.setVisibility(View.VISIBLE);
                if (mMediaPlayer.isFullScreen()) {
                    mMediaPlayer.exitFullScreen();
                }
                if (mMediaPlayer.isTinyWindow()) {
                    mMediaPlayer.exitTinyWindow();
                }
                break;
            case NiceMediaPlayer.STATE_ERROR:
                cancelUpdateProgressTimer();
                setTopBottomVisible(false);
                mTop.setVisibility(View.VISIBLE);
                mError.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void reset() {
        topBottomVisible = false;
        cancelUpdateProgressTimer();
        cancelDismissTopBottomTimer();
        mSeekBar.setProgress(0);
        mSeekBar.setSecondaryProgress(0);

        mCenterStart.setVisibility(View.VISIBLE);
        mImage.setVisibility(View.VISIBLE);

        mBottom.setVisibility(View.GONE);
        mFullScreen.setImageResource(R.drawable.ic_player_enlarge);

        mTop.setVisibility(View.VISIBLE);
        mBack.setVisibility(View.GONE);

        mLoading.setVisibility(View.GONE);
        mError.setVisibility(View.GONE);
        mCompleted.setVisibility(View.GONE);
    }

    private void startUpdateProgressTimer() {
        cancelUpdateProgressTimer();
        if (mUpdateProgressTimer == null) {
            mUpdateProgressTimer = new Timer();
        }
        if (mUpdateProgressTimerTask == null) {
            mUpdateProgressTimerTask = new TimerTask() {
                @Override
                public void run() {
                    DefaultMediaController.this.post(new Runnable() {
                        @Override
                        public void run() {
                            updateProgress();
                        }
                    });
                }
            };
        }
        mUpdateProgressTimer.schedule(mUpdateProgressTimerTask, 0, 300);
    }

    private void updateProgress() {
        int position = mMediaPlayer.getCurrentPosition();
        int duration = mMediaPlayer.getDuration();
        int bufferPercentage = mMediaPlayer.getBufferPercentage();
        mSeekBar.setSecondaryProgress(bufferPercentage);
        int progress = (int) (100f * position / duration);
        mSeekBar.setProgress(progress);
        mPosition.setText(CommonUtil.formatTime(position));
        mDuration.setText(CommonUtil.formatTime(duration));
    }

    private void cancelUpdateProgressTimer() {
        if (mUpdateProgressTimer != null) {
            mUpdateProgressTimer.cancel();
            mUpdateProgressTimer = null;
        }
        if (mUpdateProgressTimerTask != null) {
            mUpdateProgressTimerTask.cancel();
            mUpdateProgressTimerTask = null;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mCenterStart) {
            if (mMediaPlayer.isIdle())
                mMediaPlayer.start();
        } else if (v == mBack) {
            if (mMediaPlayer.isFullScreen())
                mMediaPlayer.exitFullScreen();
            else if (mMediaPlayer.isTinyWindow())
                mMediaPlayer.exitTinyWindow();
        } else if (v == mRestartPause) {
            if (mMediaPlayer.isPlaying() || mMediaPlayer.isBufferingPlaying())
                mMediaPlayer.pause();
            else if (mMediaPlayer.isPaused() || mMediaPlayer.isBufferingPaused())
                mMediaPlayer.restart();
        } else if (v == mFullScreen) {
            if (mMediaPlayer.isNormal())
                mMediaPlayer.enterFullScreen();
            else if (mMediaPlayer.isFullScreen())
                mMediaPlayer.exitFullScreen();
        } else if (v == mRetry) {
            mMediaPlayer.release();
            mMediaPlayer.start();
        } else if (v == mReplay) {
            mRetry.performClick();
        } else if (v == mShare) {
            Toast.makeText(mContext, "分享", Toast.LENGTH_SHORT).show();
        } else if (v == this) {
            if (mMediaPlayer.isPlaying()
                    || mMediaPlayer.isPaused()
                    || mMediaPlayer.isBufferingPlaying()
                    || mMediaPlayer.isBufferingPaused()) {
                setTopBottomVisible(!topBottomVisible);
            }
        }
    }

    private void setTopBottomVisible(boolean visible) {
        mTop.setVisibility(visible ? VISIBLE : GONE);
        mBottom.setVisibility(visible ? VISIBLE : GONE);
        topBottomVisible = visible;
        if (visible) {
            if (!mMediaPlayer.isPaused() && !mMediaPlayer.isBufferingPaused()) {
                startDismissTopBottomTimer();
            }
        } else {
            cancelDismissTopBottomTimer();
        }
    }

    private void startDismissTopBottomTimer() {
        cancelDismissTopBottomTimer();
        if (mDismissTopBottomCountDownTimer == null) {
            mDismissTopBottomCountDownTimer = new CountDownTimer(8000, 8000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    setTopBottomVisible(false);
                }
            };
        }
        mDismissTopBottomCountDownTimer.start();
    }


    private void cancelDismissTopBottomTimer() {
        if (mDismissTopBottomCountDownTimer != null) {
            mDismissTopBottomCountDownTimer.cancel();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        cancelDismissTopBottomTimer();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mMediaPlayer.isBufferingPaused() || mMediaPlayer.isPaused()) {
            mMediaPlayer.restart();
        }
        int position = (int) (mMediaPlayer.getDuration() * seekBar.getProgress() / 100f);
        mMediaPlayer.seekTo(position);
        startDismissTopBottomTimer();
    }
}
