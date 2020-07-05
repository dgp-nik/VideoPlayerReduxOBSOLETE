package com.wkplayback.videoplayerredux;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContentResolver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ToggleButton;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

@SuppressLint("SourceLockedOrientationActivity")

public class MainActivity extends AppCompatActivity implements KeyEvent.Callback, SensorEventListener {
    // All of the important fullscreen flags, all in one place.
    private static final int FLAG_FULLSCREEN =
            View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

    private ArrayList<PlaybackItemContainer> playbackContainers = new ArrayList<PlaybackItemContainer>();
    private int activePlaybackContainer = 0;
    //this variable was an attempt to load a next playback container while the current one
    //is still visible. It helps with the behaviour that starts the next video playing upon
    //a key-down, and reveals the video on key-up, to help smooth rough frames.
    private int nextPlaybackContainer = 0;
    private int loadedSurfaces = 0;
    //This next switch helps avoid repeated key-down actions.
    private boolean keyDown = false;
    //the screenBlack variable helps the program remember if the screen is black. I think it's
    //currently possible to somehow reverse the
    private boolean screenBlack = false;
    //the touchswitch variable causes the touch listener to turn off once it detects a touch, and
    //turn back on when the finger is removed, to stop the input from repeating quickly.
    private boolean touchSwitch = false;
    //I think the brightness function should live in the main method since it's global.
    private int brightness;
    //the next two lines are mirrored in the container class, but I wanted the info in both classes.
    //maybe a 'get' function is more traditional here?
    private boolean loopSwitch = true;
    private boolean playPauseSwitch = false;
    //the next line is for the not-yet-working resume from position function. This probably should go
    //in the container class, or just be omitted in the first version.
    private int videoPosition = 0;

    //below declares variables for the proximity sensor.
    private SensorManager mSensorManager;
    private Sensor mProximity;
    private static final int SENSOR_SENSITIVITY = 1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // https://developer.android.com/guide/components/activities/activity-lifecycle
        // In the android activity lifecycle, the onCreate method is the first method called
        // when the activity is created. However, there are instances where the activity can be
        // stopped or otherwise unloaded without killing the app process.
        // In such circumstances, the user can return to the app and onCreate() will not be called.
        // So, even though the activity has been unloaded and effectively destroyed,
        // it won't be "created" again.
        // So, onCreate is not the best place for all parts of the init cycle.
        // onStart() or onResume() are alternatives.

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        String packageName = getPackageName();
        ToggleButton loopToggle = findViewById(R.id.loopToggle);
        ToggleButton playPauseToggle = findViewById(R.id.PlayPauseToggle);
        loopToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                loopSwitch = !loopSwitch;
            }
        });
        playPauseToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                playPauseSwitch = !playPauseSwitch;
            }
        });
        //the next three lines are for the proximity sensor.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert mSensorManager != null;
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        //this next line should go in onresume, and be unregistered in on pause possibly
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);


        if (!Settings.System.canWrite(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
        }
        //this next piece asks permission for the app to control the brightnesss.
        try {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

            brightness = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("Error", "Cannot access system brightness");
            e.printStackTrace();
        }
        int brightnessMode = 0;
        try {
            brightnessMode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (brightnessMode == android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /*
            String videoString = "android.resource://" + getPackageName() + "/" + R.raw.one;

            requestWindowFeature(getWindow().FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            VideoView v = findViewById(R.id.v);
            v.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                }
            });
            v.setVideoPath(videoString);
            v.start();
            v.setSystemUiVisibility(FLAG_FULLSCREEN);
        }
    */
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK && reqCode == 1 && data != null) {
            Uri uri = data.getData();
            String extension = "";

            ContentResolver contentResolver = this.getContentResolver();
            MimeTypeMap mimetypeMap = MimeTypeMap.getSingleton();
            extension = mimetypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

            switch (extension) {
                case "jpg":
                case "jpeg":
                case "png":
                case "webp":
                case "bmp":
                    playbackContainers.add(new ImagePlaybackContainer(uri, this));
                    break;
                case "mp4":
                case "mkv":
                case "avi":
                case "webm":
                case "mov":
                    playbackContainers.add(new VideoPlaybackContainer(uri, this));
                    break;
                default:
                    break;
            }

            LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.verticalLayout);
            LinearLayout newRow = new LinearLayout(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 150);
            newRow.setOrientation(LinearLayout.HORIZONTAL);

            ImageView icon = new ImageView(this);

            Glide.with(this).load(uri).centerCrop().into(icon);

            icon.setLayoutParams(layoutParams);
            icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
            newRow.addView(icon);

            TextView text2 = new TextView(this);
            text2.setText(uri.getPath());
            newRow.addView(text2);

            verticalLayout.addView(newRow);
        }
    }

    @Override
    //I've split cuing into two methods - onKeyUp and onKeyDown. Key down prepares the next
    //video and key up shows it.
    public boolean onKeyUp(int keycode, KeyEvent event) {
        switch (keycode) {
            case KeyEvent.KEYCODE_SPACE:
                keyDown = false;
                advancePlayback();
                return true;
            case KeyEvent.KEYCODE_ENTER:
                toggleBlack();
                return true;
            case KeyEvent.KEYCODE_DEL:
                resetVideo();
                return true;
            case KeyEvent.KEYCODE_MINUS:
                brightnessDown();
                return true;
            case KeyEvent.KEYCODE_EQUALS:
                brightnessUp();
                return true;
            case KeyEvent.KEYCODE_BACKSLASH:
                brightnessFull();
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_4:
            case KeyEvent.KEYCODE_5:
            case KeyEvent.KEYCODE_6:
            case KeyEvent.KEYCODE_7:
            case KeyEvent.KEYCODE_8:
            case KeyEvent.KEYCODE_9:
                goToPlayback(keycode - 8);
                return true;
            case KeyEvent.KEYCODE_0:
                goToPlayback(keycode + 2);
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        switch (keycode) {
            case KeyEvent.KEYCODE_SPACE:
                keyDown = true;
                advancePlaybackDown();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                return true;
            default:
                return false;
        }
    }
    //a few methods to adjust the brightness. I've arbitrarily picked 5 as the interval.
    public void brightnessFull() {
        brightness = 255;
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);  //brightness is an integer variable (0-255), but dont use 0
    }

    public void brightnessUp() {
        try {
            brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        brightness += 5;
        if (brightness > 255) {
            brightness = 255;
        }
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);  //brightness is an integer variable (0-255), but dont use 0
    }

    public void brightnessDown() {
        try {
            brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        brightness -= 5;
        if (brightness < 1) {
            brightness = 1;
        }
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);  //brightness is an integer variable (0-255), but dont use 0
    }
    //These next two methods were my attempt to smooth out the cuing. I'm not 100% sure which of these
    //lines is absolutely critical but it seems to work pretty minimally this way. Playback down
    //starts the next video, advancePlayback shows the video.
    public void advancePlaybackDown() {
        nextPlaybackContainer = activePlaybackContainer + 1;
        if (nextPlaybackContainer >= playbackContainers.size()) {
            nextPlaybackContainer = 0;
        }
        if (playbackContainers.isEmpty())
            return;
        playbackContainers.get(nextPlaybackContainer).play();
        playbackContainers.get(nextPlaybackContainer).setLoop(loopSwitch);

    }

    public void advancePlayback() {
        if (playbackContainers.isEmpty())
            return;

        playbackContainers.get(activePlaybackContainer).hide();
        playbackContainers.get(activePlaybackContainer).reset();
        playbackContainers.get(activePlaybackContainer).play();
        playbackContainers.get(activePlaybackContainer).pause();

        activePlaybackContainer += 1;
        if (activePlaybackContainer >= playbackContainers.size())
            activePlaybackContainer = 0;
        if (!screenBlack) {
            playbackContainers.get(activePlaybackContainer).show();
        }
    }

    public void goToPlayback(int i) {
        if (i < 0 || i >= playbackContainers.size() && i != activePlaybackContainer)
            return;
        playbackContainers.get(activePlaybackContainer).hide();
        playbackContainers.get(activePlaybackContainer).reset();
        playbackContainers.get(activePlaybackContainer).pause();

        activePlaybackContainer = i;
        playbackContainers.get(activePlaybackContainer).show();
        playbackContainers.get(activePlaybackContainer).play();
    }

    public void resetVideo() {
        if (playbackContainers.isEmpty())
            return;
        playbackContainers.get(activePlaybackContainer).reset();
    }

    public void toggleBlack() {
        if (playbackContainers.isEmpty())
            return;
        playbackContainers.get(activePlaybackContainer).black();
    }

    public void addVideo(View v) {
        String[] mimes = {"video/*", "image/*"};

        Intent intent = new Intent();
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimes);
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Image/Video"), 1);
    }

    public void startPlayback(View v) {
        if (playbackContainers.isEmpty())
            return;

        ViewGroup layout = (ViewGroup) findViewById(R.id.constraintLayout);
        layout.removeAllViews();
        //setContentView(R.layout.activity_main);


        //This next piece governs the behaviour of the screen touch. At the moment, if the touch
        //hits the top 20% of the screen, it acts as a reset. (it should go to the first video in the
        //playlist ideally). Note the X coordinate isn't currently used, but might as well put it in.
        //The touch and reset behaviour could be switched on and off with buttons in the playlist view?
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x;
                float y;
                int h = v.getMeasuredHeight();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!touchSwitch) {
                            x = event.getX();
                            y = event.getY();
                            if (y < h / 5) {
                                toggleBlack();
                                return true;
                            }
                            touchSwitch = true;
                            advancePlaybackDown();
                            return true;
                        }
                    case MotionEvent.ACTION_UP:
                        if (touchSwitch) {
                            advancePlayback();
                            touchSwitch = false;
                            return true;
                        }
                }
                return false;
            }
        });

        for (PlaybackItemContainer container : playbackContainers)
            container.init(layout);

        layout.setSystemUiVisibility(FLAG_FULLSCREEN);
    }

    public void containerReady() {
        for (PlaybackItemContainer container : playbackContainers) {
            if (!container.isReady)
                return;
        }
        PlaybackItemContainer container = playbackContainers.get(0);
        container.show();
        container.play();
        activePlaybackContainer = 0;
        //I've added an onCompletionListener here to implement the looping and play-pause/play-next behaviour.
        container.getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (playPauseSwitch && !loopSwitch) {
                    advancePlaybackDown();
                    advancePlayback();
                }
                if (loopSwitch) {
                    playbackContainers.get(activePlaybackContainer).pause();
                }
            }
        });

    }

    @Override
    //this is what makes the proximity sensor work. I think it's currently possible to break this function
    //by for example hitting 'next' while the sensor is engaged. Maybe we should mute all inputs while the
    //proximity sensor switch is on. The sensitivity setting apparently can depend on the phone, so in
    //the final app we should have an advanced setting to change the number. Here it's just fixed at a
    //number that works well on my Pixel 3a.
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
                //near
                toggleBlack();
                screenBlack = true;
            } else {
                //far
                toggleBlack();
                screenBlack = false;
            }
        }
    }

    @Override
    //this was a part of the code package that helped me implement the proximity sensor. I don't know
    //if it's necessary, but the sensor seems to work great right now!
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    //this method is intended to stop power button long presses by restoring focus when it's lost.
    //It's just pure copied code. However a long press currently makes the video lose its full screen
    //status, and I'm not totally sure how to fix that yet.
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }
    //the rest of the code is just the whole Android lifecycle. I tried to play with this but only got crashes.
    @Override

    protected void onRestart() {

        super.onRestart();


    }

    @Override

    protected void onResume() {

        super.onResume();//visible
    }

    @Override

    protected void onPause() {

        super.onPause();//invisible
    }

    @Override

    protected void onStop() {

        super.onStop();

    }
}



