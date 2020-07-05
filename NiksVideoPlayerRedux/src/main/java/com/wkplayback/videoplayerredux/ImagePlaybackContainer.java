package com.wkplayback.videoplayerredux;

import android.net.Uri;
import android.widget.VideoView;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.view.View;
import android.content.Context;
import android.media.MediaPlayer;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;

public class ImagePlaybackContainer extends PlaybackItemContainer
{
    public final PlaybackType type = PlaybackType.Image;
    private ImageView view;
    public ImagePlaybackContainer(Uri uri, Context context) {
        super(uri, context);

    }

    public void init(ViewGroup rootView)
    {
        view = new ImageView(context);
        view.setImageURI(uri);

        hide();
    }

    @Override
    public void setLoop(boolean loopSwitch) {

    }

    @Override
    public void setPlayNext(boolean playNextSwitch) {

    }

    @Override
    public MediaPlayer getMediaPlayer() {
        return null;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public void setPosition(int position) {

    }

    public void show()
    {
        if (view == null)
        {
            return;
        }
        view.setVisibility(View.VISIBLE);
    }
    public void hide()
    {
        if (view == null)
        {
            return;
        }
        view.setVisibility(View.INVISIBLE);
    }
    public void black()
    {}
    public void pause()
    {}
    public void play()
    {}
    public void reset()
    {}
}