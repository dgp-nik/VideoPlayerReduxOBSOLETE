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

public abstract class PlaybackItemContainer
{
    public enum PlaybackType
    {
        Base,
        Video,
        Image
    }

    public Uri uri;
    public final PlaybackType type = PlaybackType.Base;
    public Context context;
    public boolean isReady = false;

    public PlaybackItemContainer(Uri uri, Context context)
    {
        this.uri = uri;
        this.context = context;
    }

    public abstract void show();
    public abstract void hide();
    public abstract void play();
    public abstract void pause();
    public abstract void black();
    public abstract void reset();
    public abstract void init(ViewGroup rootView);
    //I added these next two 'switches' to help with the toggle button behaviour.
    //I don't know if you want to put together a universal "switch" for all attributes instead
    //or just use a boolean for each attribute that we know of.
    public abstract void setLoop(boolean loopSwitch);
    public abstract void setPlayNext(boolean playNextSwitch);
    public abstract MediaPlayer getMediaPlayer();
    //these next two lines were my attempt to get the player to restart from its prior position
    //after the app is paused or stopped. I didn't get very far however, I was going to keep
    //tinkering with this function after the app is unified since it's not a high priority.
    public abstract int getPosition();
    public abstract void setPosition(int position);
}


