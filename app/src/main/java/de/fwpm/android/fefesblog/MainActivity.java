package de.fwpm.android.fefesblog;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String BASIC_URL = "https://blog.fefe.de/";
    private static final String TAG = "MAINACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new DataFetcher(this).execute();

    }

    public void updateUi(ArrayList<BlogPost> allPosts) {

        Log.d(TAG, allPosts.get(0).getText());

    }
}
