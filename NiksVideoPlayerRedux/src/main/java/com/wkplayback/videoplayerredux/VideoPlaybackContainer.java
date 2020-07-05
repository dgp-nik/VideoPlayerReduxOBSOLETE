package com.wkplayback.videoplayerredux;

import android.animation.TimeAnimator;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import android.widget.VideoView;
import android.view.TextureView;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.view.View;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaExtractor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.IOException;

public class VideoPlaybackContainer extends PlaybackItemContainer implements SurfaceHolder.Callback
{
    public final PlaybackType type = PlaybackType.Video;

    private SurfaceView     surfaceView;
    private MediaPlayer     mediaPlayer;
    private boolean loopSwitch;
    private boolean playNextSwitch;

    public VideoPlaybackContainer(Uri uri, Context context) {
        super(uri, context);
    }

    public void init(ViewGroup rootView) {
        surfaceView = new SurfaceView(context);
        surfaceView.getHolder().addCallback(this);
        mediaPlayer = MediaPlayer.create(context, uri);
        rootView.addView(surfaceView);
        mediaPlayer.start();
        mediaPlayer.pause();
        hide();
    }

    public void show()
    {
        if (surfaceView == null)
            return;
        surfaceView.setX(0);
    }
    public void hide()
    {
        if (surfaceView == null)
            return;

        DisplayMetrics dm = new DisplayMetrics();
        ((MainActivity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height= dm.heightPixels;
        surfaceView.setX(width);
    }
    public void black()
    {
        if (mediaPlayer == null)
            return;
        if (!isHidden())
        {
            hide();
            pause();
        }
        else
        {
            show();
            play();
        }
    }
    public void play()
    {
        if (mediaPlayer == null)
            return;
        mediaPlayer.start();
        mediaPlayer.setLooping(loopSwitch);
    }
    public void pause()
    {
        if (mediaPlayer == null)
            return;
        mediaPlayer.pause();
    }
    public void reset()
    {
        if (mediaPlayer == null)
            return;
        mediaPlayer.seekTo(0);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        mediaPlayer.setDisplay(holder);
        Log.d("VideoPlayback","SurfaceCreated");
        isReady = true;
        ((MainActivity)context).containerReady();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
    {
        Log.d("VideoPlayback","SurfaceChange");
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        //mediaPlayer.release();
        Log.d("VideoPlayback","SurfaceDestroy");
        mediaPlayer.setDisplay(null);
    }
    private boolean isHidden()
    {
        return surfaceView.getX() != 0;
    }

    @Override
    //simply turns looping on and off.
    public void setLoop(boolean loopSwitch) {
        mediaPlayer.setLooping(loopSwitch);
    }

    @Override
    //switch governing 'play next' behaviour
    public void setPlayNext(boolean playNextSwitch) {
        this.playNextSwitch = playNextSwitch;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public int getPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void setPosition(int position) {
        mediaPlayer.seekTo(position);
    }
}