package de.fwpm.android.fefesblog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import de.fwpm.android.fefesblog.data.ALDataFetcher;

public class AlternativlosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alternativlos);

        new ALDataFetcher(this).execute();

    }
}
