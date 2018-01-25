package de.fwpm.android.fefesblog;


import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.fwpm.android.fefesblog.adapter.NewPostsRecyclerViewAdapter;
import de.fwpm.android.fefesblog.adapter.StartScreenPagerAdapter;
import de.fwpm.android.fefesblog.fragments.BookmarkFragment;
import de.fwpm.android.fefesblog.fragments.FragmentLifecycle;
import de.fwpm.android.fefesblog.fragments.NewPostsFragment;

import static de.fwpm.android.fefesblog.NotificationHelper.NOTIFICATION_GROUP;
import static de.fwpm.android.fefesblog.NotificationHelper.NOTIFICATION_ID;
import static de.fwpm.android.fefesblog.NotificationHelper.createNotificationBuilder;
import static de.fwpm.android.fefesblog.NotificationHelper.makeNotificationChannel;
import static de.fwpm.android.fefesblog.fragments.NewPostsFragment.jumpToPosition;
import static de.fwpm.android.fefesblog.fragments.NewPostsFragment.update;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAINACTIVITY";
    private static final long SYNC_INTERVALL = 1000 * 60 * 60; //1 h
    public static final String FIRST_START = "firststart";

    private ViewPager viewPager;
    private StartScreenPagerAdapter adapter;
    private MenuItem setting_item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).getAllPendingJobs().size() == 0) {

            scheduleJob();

        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.container);
        adapter = new StartScreenPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(pageChangeListener);

        Log.d(TAG,"" + viewPager.getCurrentItem());

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

                if(tab.getText().equals(getResources().getString(R.string.newposts))) {

                    jumpToPosition(0);

                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_search);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), SearchActivity.class));
            }
        });

        update();


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
        public void onPageScrolled(int arg0, float arg1, int arg2) { }

        public void onPageScrollStateChanged(int arg0) {

        }

    };

    @Override
    public void onResume() {
        super.onResume();

        for(JobInfo jobInfo : ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).getAllPendingJobs()) {

            Log.d(TAG, "onResume: " + jobInfo.toString());

        }

    }

    private void scheduleJob() {

        final JobScheduler jobScheduler = (JobScheduler) getSystemService(
                Context.JOB_SCHEDULER_SERVICE);

        final ComponentName name = new ComponentName(this, SyncJobScheduler.class);
        final int result = jobScheduler.schedule(getJobInfo(1234, SYNC_INTERVALL, name));

        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.d("SYNC", "Scheduled job successfully!");
        }

    }

    private JobInfo getJobInfo(final int id, final long intervall, final ComponentName name) {
        final boolean isPersistent = true; // persist through boot
        final int networkType = JobInfo.NETWORK_TYPE_ANY;

        final JobInfo jobInfo;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            jobInfo = new JobInfo.Builder(id, name)
//                    .setMinimumLatency(1000 * 60 * 7)
                    .setPeriodic(intervall)
                    .setRequiredNetworkType(networkType)
                    .setPersisted(isPersistent)
                    .build();
        } else {
            jobInfo = new JobInfo.Builder(id, name)
                    .setPeriodic(intervall)
                    .setRequiredNetworkType(networkType)
                    .setPersisted(isPersistent)
                    .build();
        }

        return jobInfo;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        setting_item = menu.findItem(R.id.menu_settings);

        return true;

    }

}
