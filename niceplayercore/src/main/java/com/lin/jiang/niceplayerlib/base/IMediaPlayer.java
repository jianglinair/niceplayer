package com.lin.jiang.niceplayerlib.base;

/**
 * Created by jianglin on 17-6-1.
 * <p>
 * MediaPlayer-like API
 */

public interface IMediaPlayer {

    void start();

    void restart();

    void pause();

    void seekTo(int pos);

    boolean isIdle();

    boolean isPreparing();

    boolean isPrepared();

    boolean isBufferingPlaying();

    boolean isBufferingPaused();

    boolean isPlaying();

    boolean isPaused();

    boolean isError();

    boolean isCompleted();

    boolean isFullScreen();

    boolean isTinyWindow();

    boolean isNormal();

    int getDuration();

    int getCurrentPosition();

    int getBufferPercentage();

    void enterFullScreen();

    boolean exitFullScreen();

    void enterTinyWindow();

    boolean exitTinyWindow();

    void release();
}
