package de.fwpm.android.fefesblog;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import de.fwpm.android.fefesblog.adapter.StartScreenPagerAdapter;
import de.fwpm.android.fefesblog.fragments.BookmarkFragment;
import de.fwpm.android.fefesblog.fragments.NewPostsFragment;

import static de.fwpm.android.fefesblog.backgroundsync.BackgroundTask.scheduleJob;

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

        if (App.getInstance().isNightModeEnabled()) {
            if(App.getInstance().isAmoledModeEnabled())
                setTheme(R.style.MainActivityThemeAmoledDark);
            else
                setTheme(R.style.MainActivityThemeDark);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initView();

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(FIRST_START, true)) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(FIRST_START, false).apply();
            scheduleJob();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (themeChanged) {
            themeChanged = false;
            recreate();
            return;
        }

        if(sMgr != null) {
            registerLightSensor();
        }

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

        if(BuildConfig.DEBUG) Log.d("FATAL", "LIGHTSENSOR VALUE: " + sensorEvent.values[0]);

        boolean ambientDarkModeActive = sensorEvent.values[0] <= sensitivityValue;

        if(App.getInstance().isNightModeEnabled() != ambientDarkModeActive) {
            App.getInstance().setIsNightModeEnabled(ambientDarkModeActive);
            recreate();
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

        final RelativeLayout mainView = findViewById(R.id.main_content);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (OnApplyWindowInsetsListener) (v, insets) -> {

                int margin = insets.getSystemWindowInsetTop();

                if (margin > 0) {

                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mainView.getLayoutParams();
                    params.topMargin = margin;
                    mainView.setLayoutParams(params);

                }

                margin  = insets.getSystemWindowInsetBottom();

                if (margin > 0) {
                    Log.d(TAG, "onApplyWindowInsets: margin = " + margin);
                    CoordinatorLayout.LayoutParams fabParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                    fabParams.bottomMargin = margin + convertToDevicePixel(16);
                    fab.setLayoutParams(fabParams);
                }

                return insets.consumeSystemWindowInsets();

            });

        }

    }

    public static void setThemeChanged() {
        themeChanged = true;
    }

    public int convertToDevicePixel(int pixel) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixel, getResources().getDisplayMetrics());
    }

}
