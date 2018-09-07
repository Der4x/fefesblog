package de.fwpm.android.fefesblog.fragments;

import android.app.job.JobScheduler;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

import de.fwpm.android.fefesblog.App;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.adapter.BookmarkRecyclerViewAdapter;
import de.fwpm.android.fefesblog.adapter.NewPostsRecyclerViewAdapter;

import static de.fwpm.android.fefesblog.MainActivity.setThemeChanged;
import static de.fwpm.android.fefesblog.backgroundsync.BackgroundTask.scheduleJob;


/**
 * Created by Daniel.Eschenbacher on 25.01.2018.
 */

public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "SETTINGFRAGMENT";

    public static final String PREVIEW_SIZE = "preview_size";
    public static final String UPDATE_INTERVALL = "update_intevall";
    public static final int UPDATE_ITNVERVALL_DEFAULT = 3600000;
    public static final String NOTIFICATION_ENABLED = "notification_enabled";
    public static final String NIGHTMODE_ENABLED = "notification_enabled";

    public static final boolean NOTIFICATION_DEFAULT = true;

    private String automaticUpdatesKey;
    private String automaticNotification;
    private String updateSeq;
    private String previewSize;
    private String nightMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        // Register for changes
        automaticUpdatesKey = getString(R.string.pref_background_update_key);
        automaticNotification = getString(R.string.pref_notification_key);
        updateSeq = getString(R.string.pref_update_seq_key);
        previewSize = getString(R.string.pref_preview_size_key);
        nightMode = getString(R.string.pref_theme_key);

        findPreference(automaticUpdatesKey).setOnPreferenceChangeListener(this);
        findPreference(automaticNotification).setOnPreferenceChangeListener(this);
        findPreference(previewSize).setOnPreferenceChangeListener(this);
        findPreference(updateSeq).setOnPreferenceChangeListener(this);
        findPreference(nightMode).setOnPreferenceChangeListener(this);

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
            onAutomaticNightmodeToggle((Boolean) newValue);
        } else {
            throw new RuntimeException("Unknown preference");
        }

        return true;
    }


    private void onAutomaticNotificationToggle(Boolean isEnabled) {

        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(NOTIFICATION_ENABLED, isEnabled).apply();

    }

    private void onAutomaticNightmodeToggle(Boolean isEnabled) {

        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(NIGHTMODE_ENABLED, isEnabled).apply();
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
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt(UPDATE_INTERVALL, uodateseq).commit();
        ((JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(1234);
        scheduleJob(getActivity());

    }

    public void setPreviewSize(String newValueString) {
        int newValue = Integer.parseInt(newValueString);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt(PREVIEW_SIZE, newValue).commit();
        NewPostsRecyclerViewAdapter.MAX_LINES = newValue;
        BookmarkRecyclerViewAdapter.MAX_LINES = newValue;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!((SwitchPreference) findPreference(automaticUpdatesKey)).isChecked())
            findPreference(automaticNotification).setEnabled(false);
        else {
            findPreference(automaticNotification).setEnabled(true);

        }
    }
}
