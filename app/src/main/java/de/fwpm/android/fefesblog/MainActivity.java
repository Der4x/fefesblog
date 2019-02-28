package de.fwpm.android.fefesblog;


import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import de.fwpm.android.fefesblog.adapter.StartScreenPagerAdapter;
import de.fwpm.android.fefesblog.fragments.BookmarkFragment;
import de.fwpm.android.fefesblog.fragments.NewPostsFragment;

import static de.fwpm.android.fefesblog.backgroundsync.BackgroundTask.scheduleJob;
import static de.fwpm.android.fefesblog.fragments.SettingFragment.AUTO_NIGHTMODE_ENABLED;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private static final String TAG = "MAINACTIVITY";
    public static final String FIRST_START = "firststart";

    private ViewPager viewPager;
    private StartScreenPagerAdapter adapter;
    public static FloatingActionButton fab;
    public static boolean themeChanged;
    private static SensorManager sMgr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (App.getInstance().isNightModeEnabled()) setTheme(R.style.MainActivityThemeDark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initView();

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(FIRST_START, true)) {
            scheduleJob(this);
        }

        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(AUTO_NIGHTMODE_ENABLED, false)) {
            initSensorManager(this);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (themeChanged) {
            themeChanged = false;
            recreate();

//            if (adapter != null)
////                adapter.notifyDataSetChanged();

        }

        if(sMgr != null)
            registerLightSensor();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.settings_button:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;

        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        int sensitivityValue = PreferenceManager.getDefaultSharedPreferences(this).getInt(getString(R.string.pref_sensitivity_key), 10);

        if(sensorEvent.values[0] > sensitivityValue) {          //day

            if(App.getInstance().isNightModeEnabled()) {

                App.getInstance().setIsNightModeEnabled(false);
                recreate();
            }

        } else {                                                //night

            if(!App.getInstance().isNightModeEnabled()) {

                App.getInstance().setIsNightModeEnabled(true);
                recreate();
            }
        }

        sMgr.unregisterListener(this);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void registerLightSensor() {
        Sensor light;
        if (sMgr != null && sMgr.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            light = sMgr.getDefaultSensor(Sensor.TYPE_LIGHT);
            sMgr.registerListener(this, light,SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public static void initSensorManager(Context context) {
        sMgr = (SensorManager) context.getSystemService(SENSOR_SERVICE);
    }

    public static void destroySensorManager() {
        sMgr = null;
    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageButton settingsButton = (ImageButton) toolbar.findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(this);
        setSupportActionBar(toolbar);

    }

    private void initView() {

        viewPager = (ViewPager) findViewById(R.id.container);
        adapter = new StartScreenPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabSelected: " + tab.getText());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

                if (tab.getText().equals(getResources().getString(R.string.newposts))) {

                    ((NewPostsFragment) adapter.getItem(0)).jumpToPosition(0);

                } else if (tab.getText().equals(getResources().getString(R.string.bookmarks))) {

                    ((BookmarkFragment) adapter.getItem(1)).jumpToPosition(0);

                }
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab_search);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), SearchActivity.class));
            }
        });
    }

    public static void setThemeChanged() {
        themeChanged = true;
    }

}
