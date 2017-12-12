package com.lin.jiang.niceplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.lin.jiang.niceplayerlib.DefaultMediaController;
import com.lin.jiang.niceplayerlib.NiceVideoView;
import com.lin.jiang.niceplayerlib.base.MediaPlayerManager;

public class MainActivity extends AppCompatActivity {

    private NiceVideoView mNiceVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        mNiceVideoView = (NiceVideoView) findViewById(R.id.nice_video_view);
        mNiceVideoView.setUp("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4", null);

        DefaultMediaController defaultMediaController = new DefaultMediaController(this);
        defaultMediaController.setTitle("办公室小野开番外了，居然在办公室开澡堂！老板还点赞？");
        defaultMediaController.setImage("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-30-43.jpg");

        mNiceVideoView.setController(defaultMediaController);
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        if (MediaPlayerManager.getInstance().onBackPressd()) return;

        super.onBackPressed();
    }

    public void enterFullScreen(View view) {
        if (mNiceVideoView.isPlaying()
                || mNiceVideoView.isBufferingPlaying()
                || mNiceVideoView.isPaused()
                || mNiceVideoView.isBufferingPaused()) {
//            mNiceVideoView.enterFullScreen();
            mNiceVideoView.tinyWindowToFullScreen();
        }
    }

    public void enterTinyWindow(View view) {
        if (mNiceVideoView.isPlaying()
                || mNiceVideoView.isBufferingPlaying()
                || mNiceVideoView.isPaused()
                || mNiceVideoView.isBufferingPaused()) {
            mNiceVideoView.enterTinyWindow();
        }
    }

    public void showVideoList(View view) {
        startActivity(new Intent(this, RecyclerViewActivity.class));
    }
}
