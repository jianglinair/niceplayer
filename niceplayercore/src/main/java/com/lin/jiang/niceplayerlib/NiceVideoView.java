package com.lin.jiang.niceplayerlib;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.lin.jiang.niceplayerlib.base.AbstractMediaController;
import com.lin.jiang.niceplayerlib.base.CommonUtil;
import com.lin.jiang.niceplayerlib.base.IMediaPlayer;
import com.lin.jiang.niceplayerlib.base.MediaPlayerManager;

import java.io.IOException;
import java.util.Map;

/**
 * Created by jianglin on 17-6-1.
 */

public class NiceVideoView extends FrameLayout implements IMediaPlayer, TextureView.SurfaceTextureListener {

    public static final int STATE_ERROR = -1;            // 播放错误
    public static final int STATE_IDLE = 0;              // 播放未开始
    public static final int STATE_PREPARING = 1;         // 播放准备中
    public static final int STATE_PREPARED = 2;          // 播放准备就绪
    public static final int STATE_PLAYING = 3;           // 正在播放
    public static final int STATE_PAUSED = 4;            // 暂停播放
    public static final int STATE_BUFFERING_PLAYING = 5; // 播放中缓冲
    public static final int STATE_BUFFERING_PAUSED = 6;  // 暂停中缓冲
    public static final int STATE_COMPLETED = 7;         // 播放完成

    public static final int PLAYER_NORMAL = 10;          // 普通播放器
    public static final int PLAYER_FULL_SCREEN = 11;     // 全屏播放器
    public static final int PLAYER_TINY_WINDOW = 12;     // 小窗口播放器
    private static final String TAG = "NiceVideoView";

    /**
     * 播放状态
     */
    private int mCurrentState = STATE_IDLE;
    /**
     * 播放器状态（全屏，正常，小窗口）
     */
    private int mPlayerState = PLAYER_NORMAL;
    private int mBufferPercent = 0;

    private Context mContext;
    private FrameLayout mContainer; // 包含TextureView和Controller
    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;
    private AbstractMediaController mController; // 实际使用的是NiceMediaController
    private String mUrl;
    private Map<String, String> mHeaders;
    private MediaPlayer mMediaPlayer;

    /*===== MediaPlayer's listeners =====
    *       OnPreparedListener
    *       OnVideoSizeChangedListener
    *       OnCompletionListener
    *       OnErrorListener
    *       OnInfoListener
    *       OnBufferingUpdateListener
    * ====================================*/
    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
            mCurrentState = STATE_PREPARED;
            mController.setControllerState(mPlayerState, mCurrentState);
            Log.d(TAG, "onPrepared: ");
        }
    };
    private MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            Log.d(TAG, "onVideoSizeChanged: width=" + width + ", height=" + height);
        }
    };
    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mCurrentState = STATE_COMPLETED;
            mController.setControllerState(mPlayerState, mCurrentState);
            MediaPlayerManager.getInstance().setMediaPlayer(null);
            Log.d(TAG, "onCompletion: ");
        }
    };
    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            mCurrentState = STATE_ERROR;
            mController.setControllerState(mPlayerState, mCurrentState);
            Log.d(TAG, "onError: ");
            return false;
        }
    };
    private MediaPlayer.OnInfoListener mOnInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                // 播放器渲染第一帧
                Log.d(TAG, "onInfo: MEDIA_INFO_VIDEO_RENDERING_START");
                mCurrentState = STATE_PLAYING;
                mController.setControllerState(mPlayerState, mCurrentState);
            } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                // MediaPlayer暂时不播放，以缓冲更多的数据
                Log.d(TAG, "onInfo: MEDIA_INFO_BUFFERING_START");
                if (mCurrentState == STATE_PAUSED || mCurrentState == STATE_BUFFERING_PAUSED) {
                    mCurrentState = STATE_BUFFERING_PAUSED;
                } else {
                    mCurrentState = STATE_BUFFERING_PLAYING;
                }
                mController.setControllerState(mPlayerState, mCurrentState);
            } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                // 填充缓冲区后，MediaPlayer恢复播放/暂停
                Log.d(TAG, "onInfo: MEDIA_INFO_BUFFERING_END");
                if (mCurrentState == STATE_BUFFERING_PLAYING) {
                    mCurrentState = STATE_PLAYING;
                    mController.setControllerState(mPlayerState, mCurrentState);
                }
                if (mCurrentState == STATE_BUFFERING_PAUSED) {
                    mCurrentState = STATE_PAUSED;
                    mController.setControllerState(mPlayerState, mCurrentState);
                }
            } else {
                Log.d(TAG, "onInfo: what=" + what);
            }
            return true;
        }
    };
    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mBufferPercent = percent;
            Log.d(TAG, "onBufferingUpdate: percent=" + percent);
        }
    };
    // ===== MediaPlayer's listeners end =====

    public NiceVideoView(@NonNull Context context) {
        this(context, null);
    }

    public NiceVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        mContainer = new FrameLayout(mContext);
        mContainer.setBackgroundColor(Color.BLACK);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mContainer, params);
    }

    /**
     * @param url     视频地址
     * @param headers
     */
    public void setUp(String url, Map<String, String> headers) {
        Log.d(TAG, "setUp: url=" + url + ", headers=" + headers);
        mUrl = url;
        mHeaders = headers;
    }

    /**
     * @param controller 自定义Controller
     */
    public void setController(AbstractMediaController controller) {
        mController = controller;
        mController.setMediaPlayer(this);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(mController, params);
    }

    /**
     * 初始化播放器
     */
    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);

            mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
            mMediaPlayer.setOnErrorListener(mOnErrorListener);
            mMediaPlayer.setOnInfoListener(mOnInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        }
    }

    private void initTextureView() {
        if (mTextureView == null) {
            mTextureView = new TextureView(mContext);
            mTextureView.setSurfaceTextureListener(this);
        }
    }

    /**
     * 向Container添加TextureView，放置在Controller的下层
     */
    private void addTextureView() {
        mContainer.removeView(mTextureView);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(mTextureView, 0, params);
    }

    @Override
    public void start() {
        Log.d(TAG, "start: ");
        MediaPlayerManager.getInstance().releasePlayer();
        MediaPlayerManager.getInstance().setMediaPlayer(this);
        if (mCurrentState == STATE_IDLE
                || mCurrentState == STATE_ERROR
                || mCurrentState == STATE_COMPLETED) {
            initMediaPlayer();
            initTextureView();
            addTextureView();
        }
    }

    @Override
    public void restart() {
        if (mCurrentState == STATE_PAUSED) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
            mController.setControllerState(mPlayerState, mCurrentState);
            Log.d(TAG, "restart: STATE_PLAYING");
        }
        if (mCurrentState == STATE_BUFFERING_PAUSED) {
            mMediaPlayer.start();
            mCurrentState = STATE_BUFFERING_PLAYING;
            mController.setControllerState(mPlayerState, mCurrentState);
            Log.d(TAG, "restart: STATE_BUFFERING_PLAYING");
        }
    }

    @Override
    public void pause() {
        if (mCurrentState == STATE_PLAYING) {
            mMediaPlayer.pause();
            mCurrentState = STATE_PAUSED;
            mController.setControllerState(mPlayerState, mCurrentState);
            Log.d(TAG, "pause: STATE_PAUSED");
        }
        if (mCurrentState == STATE_BUFFERING_PLAYING) {
            mMediaPlayer.pause();
            mCurrentState = STATE_BUFFERING_PAUSED;
            mController.setControllerState(mPlayerState, mCurrentState);
            Log.d(TAG, "pause: STATE_BUFFERING_PAUSED");
        }
    }

    @Override
    public void seekTo(int pos) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(pos);
            Log.d(TAG, "seekTo: pos=" + pos);
        }
    }

    @Override
    public boolean isIdle() {
        return mCurrentState == STATE_IDLE;
    }

    @Override
    public boolean isPreparing() {
        return mCurrentState == STATE_PREPARING;
    }

    @Override
    public boolean isPrepared() {
        return mCurrentState == STATE_PREPARED;
    }

    @Override
    public boolean isBufferingPlaying() {
        return mCurrentState == STATE_BUFFERING_PLAYING;
    }

    @Override
    public boolean isBufferingPaused() {
        return mCurrentState == STATE_BUFFERING_PAUSED;
    }

    @Override
    public boolean isPlaying() {
        return mCurrentState == STATE_PLAYING;
    }

    @Override
    public boolean isPaused() {
        return mCurrentState == STATE_PAUSED;
    }

    @Override
    public boolean isError() {
        return mCurrentState == STATE_ERROR;
    }

    @Override
    public boolean isCompleted() {
        return mCurrentState == STATE_COMPLETED;
    }

    @Override
    public boolean isFullScreen() {
        return mPlayerState == PLAYER_FULL_SCREEN;
    }

    @Override
    public boolean isTinyWindow() {
        return mPlayerState == PLAYER_TINY_WINDOW;
    }

    @Override
    public boolean isNormal() {
        return mPlayerState == PLAYER_NORMAL;
    }

    @Override
    public int getDuration() {
        return mMediaPlayer != null ? mMediaPlayer.getDuration() : 0;
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
    }

    @Override
    public int getBufferPercentage() {
        return mBufferPercent;
    }

    /**
     * 全屏，将mContainer(内部包含mTextureView和mController)从当前容器中移除，并添加到android.R.id.content中.
     * <p>
     * 每个Activity里面都有一个android.R.content，它是一个FrameLayout，里面包含了我们setContentView的所有
     * 控件。既然它是一个FrameLayout，我们就可以将它作为全屏和小窗口的目标视图。我们把从当前视图移除的mContainer
     * 重新添加到android.R.content中，并且设置成横屏。这个时候还需要注意android.R.content是不包括ActionBar
     * 和状态栏的，所以要将Activity设置成全屏模式，同时隐藏ActionBar。
     */
    @Override
    public void enterFullScreen() {
        Log.d(TAG, "enterFullScreen: ");
        if (mCurrentState == PLAYER_FULL_SCREEN) return;
        // 隐藏ActionBar、状态栏，并横屏
        CommonUtil.hideActionBar(mContext);
        CommonUtil.scanForActivity(mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 从NiceMediaPlayer这个FrameLayout中移除Container
        this.removeView(mContainer);
        // 将Container添加至contentView
        ViewGroup contentView = (ViewGroup) CommonUtil.scanForActivity(mContext).findViewById(android.R.id.content);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentView.addView(mContainer, params);

        mPlayerState = PLAYER_FULL_SCREEN;
        mController.setControllerState(mPlayerState, mCurrentState);
        Log.d(TAG, "enterFullScreen: PLAYER_FULL_SCREEN");
    }

    /**
     * 退出全屏，移除mTextureView和mController，并添加到非全屏的容器中。
     *
     * @return true退出全屏.
     */
    @Override
    public boolean exitFullScreen() {
        Log.d(TAG, "exitFullScreen: ");
        if (mPlayerState == PLAYER_FULL_SCREEN) {
            CommonUtil.showActionBar(mContext);
            CommonUtil.scanForActivity(mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            // 从contentView中移除Container
            ViewGroup contentView = (ViewGroup) CommonUtil.scanForActivity(mContext).findViewById(android.R.id.content);
            contentView.removeView(mContainer);
            // 将Container添加至NiceMediaPlayer这个FrameLayout
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            this.addView(mContainer, params);

            mPlayerState = PLAYER_NORMAL;
            mController.setControllerState(mPlayerState, mCurrentState);
            Log.d(TAG, "exitFullScreen: PLAYER_NORMAL");
            return true;
        }
        return false;
    }

    /**
     * 进入小窗口播放，小窗口播放的实现原理与全屏播放类似。
     */
    @Override
    public void enterTinyWindow() {
        Log.d(TAG, "enterTinyWindow: ");
        if (mPlayerState == PLAYER_TINY_WINDOW) return;
        this.removeView(mContainer);
        // 小窗口的宽度为屏幕宽度的60%，长宽比默认为16:9，右边距、下边距为8dp
        ViewGroup contentView = (ViewGroup) CommonUtil.scanForActivity(mContext).findViewById(android.R.id.content);
        LayoutParams params = new LayoutParams((int) (CommonUtil.getScreenWidth(mContext) * 0.6f),
                (int) (CommonUtil.getScreenWidth(mContext) * 0.6f * 9f / 16f));
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.rightMargin = CommonUtil.dp2px(mContext, 8f);
        params.bottomMargin = CommonUtil.dp2px(mContext, 8f);
        contentView.addView(mContainer, params);

        mPlayerState = PLAYER_TINY_WINDOW;
        mController.setControllerState(mPlayerState, mCurrentState);
        Log.d(TAG, "enterTinyWindow: PLAYER_TINY_WINDOW");
    }

    /**
     * 退出小窗口播放
     */
    @Override
    public boolean exitTinyWindow() {
        Log.d(TAG, "exitTinyWindow: ");
        if (mPlayerState == PLAYER_TINY_WINDOW) {
            ViewGroup contentView = (ViewGroup) CommonUtil.scanForActivity(mContext).findViewById(android.R.id.content);
            contentView.removeView(mContainer);
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            this.addView(mContainer, params);

            mPlayerState = PLAYER_NORMAL;
            mController.setControllerState(mPlayerState, mCurrentState);
            Log.d(TAG, "exitTinyWindow: PLAYER_NORMAL");
            return true;
        }
        return false;
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mContainer.removeView(mTextureView);
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        if (mController != null) {
            mController.reset();
        }
        mCurrentState = STATE_IDLE;
        mPlayerState = PLAYER_NORMAL;
    }

    /**
     * 当mContainer移除重新添加后，mContainer及其内部的mTextureView和mController都会重绘，
     * mTextureView重绘后，会重新new一个SurfaceTexture，并重新回调onSurfaceTextureAvailable方法，
     * 这样mTextureView的数据通道SurfaceTexture发生了变化，但是mMediaPlayer还是持有原先的mSurfaceTexture，
     * 所以在切换全屏之前要保存之前的mSurfaceTexture，当切换到全屏后重新调用onSurfaceTextureAvailable时，
     * 将之前的mSurfaceTexture重新设置给mTextureView，这样就保证了切换时视频播放的无缝衔接
     *
     * @param surface
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        // SurfaceTexture数据通道准备就绪
        Log.d(TAG, "onSurfaceTextureAvailable: ");
        if (mSurfaceTexture == null) {
            mSurfaceTexture = surface;
            openMediaPlayer();
        } else {
            mTextureView.setSurfaceTexture(mSurfaceTexture);
        }
    }

    private void openMediaPlayer() {
        try {
            mMediaPlayer.setDataSource(mContext.getApplicationContext(), Uri.parse(mUrl), mHeaders);
            mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            Log.d(TAG, "openMediaPlayer: STATE_PREPARING");
            mController.setControllerState(mPlayerState, mCurrentState);
        } catch (IOException e) {
            Log.d(TAG, "openMediaPlayer: IOException, " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureSizeChanged: width=" + width + ", height=" + height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "onSurfaceTextureDestroyed: " + surface);
        return mSurfaceTexture == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    public void tinyWindowToFullScreen() {
        if(mPlayerState == PLAYER_TINY_WINDOW) {
            exitTinyWindow();
            enterFullScreen();
        } else {
            enterFullScreen();
        }
    }
}
