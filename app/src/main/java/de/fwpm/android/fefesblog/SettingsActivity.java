package de.fwpm.android.fefesblog;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (App.getInstance().isNightModeEnabled()) {
            if(App.getInstance().isAmoledModeEnabled())
                setTheme(R.style.SettingsActivityThemeDarkAmoled);
            else
                setTheme(R.style.SettingsActivityThemeDark);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Einstellungen");

    }

}
