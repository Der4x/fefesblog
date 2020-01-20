package de.fwpm.android.fefesblog.fragments;

import android.app.job.JobScheduler;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ActionBarOverlayLayout;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Toast;

import java.util.Objects;

import de.fwpm.android.fefesblog.App;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.adapter.BookmarkRecyclerViewAdapter;
import de.fwpm.android.fefesblog.adapter.NewPostsRecyclerViewAdapter;

import static de.fwpm.android.fefesblog.MainActivity.destroySensorManager;
import static de.fwpm.android.fefesblog.MainActivity.initSensorManager;
import static de.fwpm.android.fefesblog.MainActivity.setThemeChanged;
import static de.fwpm.android.fefesblog.backgroundsync.BackgroundTask.scheduleJob;


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
    public static final String AUTO_NIGHTMODE_ENABLED = "auto_nightmode_enabled";


    public static final boolean NOTIFICATION_DEFAULT = true;

    private String automaticUpdatesKey;
    private String automaticNotification;
    private String updateSeq;
    private String previewSize;
    private String previewMode;
    private String nightMode;
    private String autoNightMode;
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
        autoNightMode = getString(R.string.pref_auto_theme_key);
        sensitivityNightmode = getString(R.string.pref_sensitivity_key);

        findPreference(automaticUpdatesKey).setOnPreferenceChangeListener(this);
        findPreference(automaticNotification).setOnPreferenceChangeListener(this);
        findPreference(previewSize).setOnPreferenceChangeListener(this);
        findPreference(updateSeq).setOnPreferenceChangeListener(this);
        findPreference(nightMode).setOnPreferenceChangeListener(this);
        findPreference(previewMode).setOnPreferenceChangeListener(this);
        findPreference(autoNightMode).setOnPreferenceChangeListener(this);
        findPreference(sensitivityNightmode).setOnPreferenceChangeListener(this);

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

            onNightmodeToggle((Boolean) newValue);

        } else if(previewMode.equals(key)) {

            onPreviewModeToggle((Boolean) newValue);

        } else if(autoNightMode.equals(key)) {

            onAutoNightModeToggle((Boolean) newValue);

        }
        else throw new RuntimeException("Unknown preference");


        return true;
    }

    private void onAutoNightModeToggle(Boolean isEnabled) {

        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(AUTO_NIGHTMODE_ENABLED, isEnabled).apply();
        findPreference(sensitivityNightmode).setEnabled(isEnabled);
        if(isEnabled)
            initSensorManager(getActivity());
        else
            destroySensorManager();

    }


    private void onAutomaticNotificationToggle(Boolean isEnabled) {

        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(NOTIFICATION_ENABLED, isEnabled).apply();

    }

    private void onNightmodeToggle(Boolean isEnabled) {

        App.getInstance().setIsNightModeEnabled(isEnabled);
        getActivity().recreate();
        setThemeChanged();

    }

    private void onAutomaticUpdatesToggle(Boolean isEnabled) {
        if (isEnabled) {
            Log.d(TAG, "Enable Updates");
            findPreference(automaticNotification).setEnabled(true);
            scheduleJob(getActivity());

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
        scheduleJob(getActivity());

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
        findPreference(sensitivityNightmode).setEnabled(((SwitchPreference) findPreference(autoNightMode)).isChecked());

        //Bug: no update on this switch-preference when changed setting programmatically in another activity
        ((SwitchPreference) findPreference(nightMode)).setChecked(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(NIGHTMODE_ENABLED, false));

    }

}
