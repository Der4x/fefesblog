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
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(NIGHTMODE_ENABLED, isNightModeEnabled).apply();
    }

    public static App getInstance() {
        return instance;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


}
