package de.fwpm.android.fefesblog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (App.getInstance().isNightModeEnabled()) setTheme(R.style.SettingsActivityThemeDark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Einstellungen");
    }

}
