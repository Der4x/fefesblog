package de.fwpm.android.fefesblog.fragments;

import android.app.job.JobScheduler;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import de.fwpm.android.fefesblog.App;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.adapter.BookmarkRecyclerViewAdapter;
import de.fwpm.android.fefesblog.adapter.NewPostsRecyclerViewAdapter;

import static de.fwpm.android.fefesblog.MainActivity.setThemeChanged;
import static de.fwpm.android.fefesblog.backgroundsync.BackgroundTask.scheduleJob;
import static de.fwpm.android.fefesblog.fragments.SettingFragment.DarkThemeMode.*;


/**
 * Created by Daniel.Eschenbacher on 25.01.2018.
 */

public class SettingFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "SETTINGFRAGMENT";

    public static final String PREVIEW_SIZE = "preview_size";
    public static final String PREVIEW_SIZE_BACKUP = "preview_size_backup";
    public static final String UPDATE_INTERVALL = "update_intevall";
    public static final int UPDATE_ITNVERVALL_DEFAULT = 3600000;
    public static final String NOTIFICATION_ENABLED = "notification_enabled";
    public static final String NIGHTMODE_ENABLED = "nightmode_enabled";
    public static final String DARKTHEME_MODE = "darktheme_mode";
    public static final String AMOLED_NIGHTMODE_ENABLED = "amoled_nightmode_enabled";

    public static final boolean NOTIFICATION_DEFAULT = true;

    private String automaticUpdatesKey;
    private String automaticNotification;
    private String updateSeq;
    private String previewSize;
    private String previewMode;
    private String nightMode;
    private String amoledNightMode;
    private String sensitivityNightmode;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        addPreferencesFromResource(R.xml.preferences);

        // Register for changes
        automaticUpdatesKey = getString(R.string.pref_background_update_key);
        automaticNotification = getString(R.string.pref_notification_key);
        updateSeq = getString(R.string.pref_update_seq_key);
        previewSize = getString(R.string.pref_preview_size_key);
        nightMode = getString(R.string.pref_theme_key);
        previewMode = getString(R.string.pref_preview_key);
        amoledNightMode = getString(R.string.pref_amoled_theme_key);
        sensitivityNightmode = getString(R.string.pref_sensitivity_key);

        findPreference(automaticUpdatesKey).setOnPreferenceChangeListener(this);
        findPreference(automaticNotification).setOnPreferenceChangeListener(this);
        findPreference(previewSize).setOnPreferenceChangeListener(this);
        findPreference(updateSeq).setOnPreferenceChangeListener(this);
        findPreference(nightMode).setOnPreferenceChangeListener(this);
        findPreference(previewMode).setOnPreferenceChangeListener(this);
        findPreference(sensitivityNightmode).setOnPreferenceChangeListener(this);
        findPreference(amoledNightMode).setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        String key = preference.getKey();

        if (automaticUpdatesKey.equals(key)) {

            onAutomaticUpdatesToggle((Boolean) newValue);

        } else if (automaticNotification.equals(key)) {

            onAutomaticNotificationToggle((Boolean) newValue);

        } else if (previewSize.equals(key)) {

            setPreviewSize((String) newValue);

        } else if (updateSeq.equals(key)) {

            setUpdateSeq((String) newValue);

        } else if(nightMode.equals(key)) {

            onNightmodeChanged(Integer.parseInt(newValue.toString()));

        } else if(previewMode.equals(key)) {

            onPreviewModeToggle((Boolean) newValue);

        }
        else if(amoledNightMode.equals(key)) {

            onAmoledNightModeToggle((Boolean) newValue);

        } else if(sensitivityNightmode.equals(key)) {

            onSensitivityChanged((int) newValue);

        }
        else throw new RuntimeException("Unknown preference");


        return true;
    }

    private void onSensitivityChanged(int newValue) {

        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt(getString(R.string.pref_sensitivity_key), newValue).apply();

    }

    private void onAmoledNightModeToggle(Boolean isEnabled) {

        App.getInstance().setAmoledModeEnabled(isEnabled);
        if(App.getInstance().isNightModeEnabled()) {
            getActivity().recreate();
            setThemeChanged();
        }
        
    }

    private void onAutomaticNotificationToggle(Boolean isEnabled) {

        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(NOTIFICATION_ENABLED, isEnabled).apply();

    }

    private void onNightmodeChanged(int themeMode) {

        DarkThemeMode newMode = OFF;

        switch (themeMode) {
            case 0:
                newMode = OFF;
                break;
            case 1:
                newMode = ON;
                break;
            case 2:
                newMode = AMBIENT;
                break;
            case 3:
                newMode = SYSTEM;
                break;
        }

        App.getInstance().setDarkThemeMode(newMode);
        getActivity().recreate();
        setThemeChanged();

    }

    private void onAutomaticUpdatesToggle(Boolean isEnabled) {
        if (isEnabled) {
            Log.d(TAG, "Enable Updates");
            findPreference(automaticNotification).setEnabled(true);
            scheduleJob();

        } else {
            Log.d(TAG, "Disable Updates");
            findPreference(automaticNotification).setEnabled(false);
            ((SwitchPreference) findPreference(automaticNotification)).setChecked(false);
            ((JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(1234);
        }
    }

    public void setUpdateSeq(String newValue) {
        int uodateseq = Integer.parseInt(newValue);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt(UPDATE_INTERVALL, uodateseq).apply();
        ((JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(1234);
        scheduleJob();

    }

    public void setPreviewSize(String newValueString) {
        int newValue = Integer.parseInt(newValueString);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt(PREVIEW_SIZE, newValue).apply();
        NewPostsRecyclerViewAdapter.MAX_LINES = newValue;
        BookmarkRecyclerViewAdapter.MAX_LINES = newValue;
    }

    private void onPreviewModeToggle(Boolean showFullPost) {

        findPreference(previewSize).setEnabled(!showFullPost);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();

        int newValue;

        if(showFullPost) {

            newValue = Integer.MAX_VALUE;
            editor.putInt(PREVIEW_SIZE_BACKUP, prefs.getInt(PREVIEW_SIZE, 6)).apply();
            editor.putInt(PREVIEW_SIZE, newValue).commit();

        } else {

            newValue = prefs.getInt(PREVIEW_SIZE_BACKUP, 6);
            editor.putInt(PREVIEW_SIZE, newValue).commit();

        }

        NewPostsRecyclerViewAdapter.MAX_LINES = newValue;
        BookmarkRecyclerViewAdapter.MAX_LINES = newValue;

    }

    @Override
    public void onResume() {

        super.onResume();
        findPreference(automaticNotification).setEnabled(((SwitchPreference) findPreference(automaticUpdatesKey)).isChecked());
        findPreference(previewSize).setEnabled(!((SwitchPreference) findPreference(previewMode)).isChecked());

        findPreference(sensitivityNightmode).setEnabled(((ListPreference) findPreference(nightMode)).getValue().equals("2"));

    }

    public enum DarkThemeMode {
        OFF, ON, AMBIENT, SYSTEM
    }

}
