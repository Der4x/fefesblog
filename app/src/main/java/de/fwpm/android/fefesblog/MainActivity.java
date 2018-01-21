package de.fwpm.android.fefesblog;


import android.app.Fragment;
import android.nfc.Tag;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import de.fwpm.android.fefesblog.adapter.StartScreenPagerAdapter;
import de.fwpm.android.fefesblog.fragments.NewPostsFragment;

import static de.fwpm.android.fefesblog.fragments.NewPostsFragment.jumpToPosition;

public class MainActivity extends AppCompatActivity {

    private static final String BASIC_URL = "https://blog.fefe.de/";
    private static final String TAG = "MAINACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        final StartScreenPagerAdapter adapter = new StartScreenPagerAdapter(this, getSupportFragmentManager());
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

                if(tab.getText().equals(getResources().getString(R.string.newposts))) {

                    jumpToPosition(0);

                }
            }
        });

    }

}
