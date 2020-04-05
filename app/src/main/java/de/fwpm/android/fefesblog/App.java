package de.fwpm.android.fefesblog;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import de.fwpm.android.fefesblog.fragments.SettingFragment;

import static de.fwpm.android.fefesblog.MainActivity.destroySensorManager;
import static de.fwpm.android.fefesblog.MainActivity.initSensorManager;
import static de.fwpm.android.fefesblog.fragments.SettingFragment.*;
import static de.fwpm.android.fefesblog.fragments.SettingFragment.AMOLED_NIGHTMODE_ENABLED;
import static de.fwpm.android.fefesblog.fragments.SettingFragment.DarkThemeMode.*;
import static de.fwpm.android.fefesblog.fragments.SettingFragment.NIGHTMODE_ENABLED;

public class App extends Application {

    private boolean isNightModeEnabled = false;
    private boolean amoledModeEnabled = false;
    private DarkThemeMode darkThemeMode = OFF;
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        initAppTheme();

    }

    private void initAppTheme() {

        SharedPreferences mPrefs =  PreferenceManager.getDefaultSharedPreferences(this);
        this.darkThemeMode = DarkThemeMode.valueOf(mPrefs.getString(DARKTHEME_MODE, "OFF"));
        this.amoledModeEnabled = mPrefs.getBoolean(AMOLED_NIGHTMODE_ENABLED, false);

        editAppThemeMode();

    }

    private void editAppThemeMode() {

        switch (darkThemeMode) {

            case OFF:
                this.isNightModeEnabled = false;
                break;
            case ON:
                this.isNightModeEnabled = true;
                break;
            case AMBIENT:
                this.isNightModeEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(NIGHTMODE_ENABLED, false);
                initSensorManager(this);
                break;
            case SYSTEM:
                isNightModeEnabled = systemDarkModeActive();
                break;

        }

        if(darkThemeMode != AMBIENT) destroySensorManager();
    }

    public boolean isNightModeEnabled() {
        editAppThemeMode();
        return isNightModeEnabled;
    }

    public void setIsNightModeEnabled(boolean isNightModeEnabled) {
        this.isNightModeEnabled = isNightModeEnabled;
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(NIGHTMODE_ENABLED, isNightModeEnabled).apply();
    }

    public void setDarkThemeMode(DarkThemeMode mode) {
        darkThemeMode = mode;
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(DARKTHEME_MODE, mode.name()).apply();
        editAppThemeMode();
    }

    public boolean isAmoledModeEnabled() {
        return amoledModeEnabled;
    }

    public void setAmoledModeEnabled(boolean enabled) {
        this.amoledModeEnabled = enabled;
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(AMOLED_NIGHTMODE_ENABLED, enabled).apply();
    }

    public static App getInstance() {
        return instance;
    }

    public Boolean systemDarkModeActive() {

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;

    }

}
