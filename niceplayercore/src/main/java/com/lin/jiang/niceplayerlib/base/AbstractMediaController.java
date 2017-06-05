package com.lin.jiang.niceplayerlib.base;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by jianglin on 17-6-1.
 * <p>
 * MediaController API
 * <p>
 * 被实际的Controller实现，以此实现解耦，可以使用DefaultMediaController，也可以自定义
 */

public abstract class AbstractMediaController extends FrameLayout {

    public AbstractMediaController(@NonNull Context context) {
        super(context);
    }

    public AbstractMediaController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public abstract void setTitle(String title);

    public abstract void setImage(String imgUrl);

    public abstract void setImage(@DrawableRes int resId);

    public abstract void setMediaPlayer(IMediaPlayer player);

    public abstract void setControllerState(int playerState, int playState);

    public abstract void reset();
}
