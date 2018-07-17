package de.fwpm.android.fefesblog;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.fwpm.android.fefesblog.Episode;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.database.AppDatabase;
import de.fwpm.android.fefesblog.utils.MediaPlayerService;

public class EpisodeActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String INTENT_NR = "intent_nr";
    private static final String TAG = "EpisodeActivity";
    public static final String Broadcast_PLAY_NEW_AUDIO = "de.fwpm.android.fefesblog.PlayNewAudio";

    private Context mContext;

    private AppDatabase database;
    private Episode mEpisode;
    private int episode_nr;

    private static MediaPlayerService player;

    boolean serviceBound = false;
    static boolean audioStreaming;
    private static boolean streamIspaused;
    private static boolean seekbarUserInput;

    private static int mInterval = 1000;
    private static Handler mHandler;

    private TextView nr;
    private TextView topic;
    private static ImageButton play;

    // Music control bar
    private ConstraintLayout media_controller;

    private static ImageButton pause;
    private static ImageButton stop;

    private static TextView runningStreamTitle;
    private static TextView time;
    private static TextView duration;
    private static SeekBar seekbar;

    private static ProgressDialog mDialog;
    private static Context context;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();

            if(audioStreaming && !player.isPlaying()) streamIspaused = true;
            else audioStreaming = true;
            serviceBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this;
        database = AppDatabase.getInstance(this);
        mContext = this;
        mHandler = new Handler();

        episode_nr = getIntent().getIntExtra(INTENT_NR, -1);

        audioStreaming = isMyServiceRunning(MediaPlayerService.class);

        initMusicControlBar();

        if(audioStreaming) {
            bindMediaService();
            runningStreamTitle.setText(player.episode_info);
        }

        getEpisode();

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.pause:
                pauseAudio();
                break;
            case R.id.play:
                playAudio(mEpisode.getFile_ogg());
                break;
            case R.id.stop:
                stopAudio();
                break;

        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(serviceBound) {
            unbindService(serviceConnection);
            if(!player.isPlaying() && !streamIspaused) player.stopSelf();
            stopRepeatingTask();
        }

    }

    public void getEpisode() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                mEpisode = database.episodeDao().getEpisodeByNr(episode_nr);
                updateUI();

            }
        }).start();


    }

    private void initMusicControlBar() {

        media_controller = (ConstraintLayout) findViewById(R.id.media_controller);

        pause = (ImageButton) findViewById(R.id.pause);
        stop = (ImageButton) findViewById(R.id.stop);
        runningStreamTitle = (TextView) findViewById(R.id.stream_title);
        runningStreamTitle.setSelected(true);
        time = (TextView) findViewById(R.id.time);
        duration = (TextView) findViewById(R.id.duration);
        seekbar = (SeekBar) findViewById(R.id.seekBar);

        pause.setOnClickListener(this);
        stop.setOnClickListener(this);
    }

    private void bindMediaService() {
        bindService(new Intent(this, MediaPlayerService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }


    private void updateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initView();
            }
        });
    }

    private void initView() {

        nr = (TextView) findViewById(R.id.nr);
        topic = (TextView) findViewById(R.id.topic);
        play = (ImageButton) findViewById(R.id.play);
        play.setOnClickListener(this);

        nr.setText("Folge " + mEpisode.getNr());
        topic.setText(mEpisode.getTopic());

        if(audioStreaming) {

            media_controller.setVisibility(View.VISIBLE);
            setPauseButtonVisible(true);
            setPauseButtonIcon(streamIspaused);
            setAudioMetaData();

        }

    }

    private void playAudio(String media) {

        if(!audioStreaming) {
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            playerIntent.putExtra("media", media);
            playerIntent.putExtra("info", mEpisode.getTitle());
            startService(playerIntent);
            bindMediaService();
            streamIspaused = false;
            runningStreamTitle.setText(mEpisode.getTitle());

        } else {
            player.changeEpisode(mEpisode);
            runningStreamTitle.setText(player.episode_info);
        }

        startBufferingDialog();
        setPauseButtonVisible(true);
        setPauseButtonIcon(false);
        media_controller.setVisibility(View.VISIBLE);

    }

    private void pauseAudio() {

        if(player.isPlaying()) {

            player.pauseMedia();
            streamIspaused = true;
            setPauseButtonIcon(streamIspaused);

        } else {

            player.resumeMedia();
            streamIspaused = false;
            setPauseButtonIcon(streamIspaused);
            startRepeatingTask();

        }
    }

    private void stopAudio() {

        player.stopMedia();
        setPauseButtonVisible(false);
        streamIspaused = false;
        stopRepeatingTask();
        resetMusicBarData();
        media_controller.setVisibility(View.GONE);

    }

    private void resetMusicBarData() {
        time.setText("00:00");
        seekbar.setProgress(0);
    }


    private static void setPauseButtonIcon(boolean onPause) {

        pause.setImageResource(onPause ? R.drawable.ic_stat_play_arrow : R.drawable.ic_stat_pause);

    }

    private static void setPauseButtonVisible(boolean visible) {

        pause.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);

    }

    private static void startBufferingDialog() {

        mDialog = new ProgressDialog(context);
        mDialog.setMessage("Buffering...");
        mDialog.setCancelable(false);
        mDialog.show();

    }

    public static void stopDialog() {

        if(mDialog != null && mDialog.isShowing()) mDialog.dismiss();
        setAudioMetaData();

    }

    private static void setAudioMetaData() {

        int durationInMillis = player.getDuration();

        duration.setText(getFormattedTimeString(durationInMillis));
        seekbar.setMax(durationInMillis);
        
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(seekbarUserInput) time.setText(getFormattedTimeString(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekbarUserInput = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekbarUserInput = false;
                player.changeMediaPosition(seekBar.getProgress());
                streamIspaused = false;
                setPauseButtonIcon(streamIspaused);
                startRepeatingTask();
            }
        });

        startRepeatingTask();

    }

    private static void updateStatus() {

        int progressInMillis = player.getCurrentPosition();
        time.setText(getFormattedTimeString(progressInMillis));
        seekbar.setProgress(progressInMillis);

    }

    private static String getFormattedTimeString(int millis) {

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(millis);

        int seconds = cal.get(Calendar.SECOND);
        int minutes = cal.get(Calendar.MINUTE);
        int hours = cal.get(Calendar.HOUR);

        return hours
                + ":"
                + (minutes > 9 ? minutes : "0"+minutes)
                + ":"
                + (seconds > 9 ? seconds : "0"+seconds);

    }

    static Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                updateStatus(); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                if(!streamIspaused) mHandler.postDelayed(mStatusChecker, mInterval);
                else stopRepeatingTask();
            }
        }
    };

    static void startRepeatingTask() {
        mStatusChecker.run();
    }

    static void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
