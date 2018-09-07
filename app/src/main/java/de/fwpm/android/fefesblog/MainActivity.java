package de.fwpm.android.fefesblog;


import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import de.fwpm.android.fefesblog.adapter.StartScreenPagerAdapter;
import de.fwpm.android.fefesblog.fragments.FragmentLifecycle;

import static de.fwpm.android.fefesblog.backgroundsync.BackgroundTask.scheduleJob;
import static de.fwpm.android.fefesblog.fragments.BookmarkFragment.jump_To_Position;
import static de.fwpm.android.fefesblog.fragments.NewPostsFragment.jumpToPosition;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MAINACTIVITY";
    public static final String FIRST_START = "firststart";

    private ViewPager viewPager;
    private StartScreenPagerAdapter adapter;
    public static FloatingActionButton fab;
    public static boolean themeChanged;

    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (App.getInstance().isNightModeEnabled()) setTheme(R.style.MainActivityThemeDark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        initToolbar();

        initView();

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(FIRST_START, true)) {

            scheduleJob(this);

        }

    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageButton settingsButton = (ImageButton) toolbar.findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(this);
        setSupportActionBar(toolbar);

    }

    public static Context getMainContext() {
        return context;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (themeChanged) {
            themeChanged = false;
            recreate();
            if (adapter != null) {

                adapter.notifyDataSetChanged();

            }
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

    private void initView() {

        viewPager = (ViewPager) findViewById(R.id.container);
        adapter = new StartScreenPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(pageChangeListener);

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

                    jumpToPosition(0);

                } else if (tab.getText().equals(getResources().getString(R.string.bookmarks))) {

                    jump_To_Position(0);

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

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

        int currentPosition = 0;

        @Override
        public void onPageSelected(int newPosition) {

            FragmentLifecycle fragmentToShow = (FragmentLifecycle) adapter.getItem(newPosition);
            fragmentToShow.onResumeFragment();

            FragmentLifecycle fragmentToHide = (FragmentLifecycle) adapter.getItem(currentPosition);
            fragmentToHide.onPauseFragment();

            currentPosition = newPosition;
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void onPageScrollStateChanged(int arg0) {

        }

    };

    public static void setThemeChanged() {
        themeChanged = true;
    }


}
