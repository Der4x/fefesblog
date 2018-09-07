package de.fwpm.android.fefesblog;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static de.fwpm.android.fefesblog.fragments.SettingFragment.NIGHTMODE_ENABLED;

public class App extends Application {

    private boolean isNightModeEnabled = false;
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SharedPreferences mPrefs =  PreferenceManager.getDefaultSharedPreferences(this);
        this.isNightModeEnabled = mPrefs.getBoolean(NIGHTMODE_ENABLED, false);
    }

    public boolean isNightModeEnabled() {
        return isNightModeEnabled;
    }

    public void setIsNightModeEnabled(boolean isNightModeEnabled) {
        this.isNightModeEnabled = isNightModeEnabled;
    }

    public static App getInstance() {
        return instance;
    }

}
