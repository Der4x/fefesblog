package de.fwpm.android.fefesblog.fragments;

import android.app.job.JobScheduler;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;

import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.data.BackgroundDataFetcher;

import static de.fwpm.android.fefesblog.utils.BackgroundTask.scheduleJob;


/**
 * Created by Daniel.Eschenbacher on 25.01.2018.
 */

public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "SETTINGFRAGMENT";
    private static int updateSeqValue = 3600000;
    private static int previewSizeValue = 6;


    private String automaticUpdatesKey;
    private String automaticNotification;
    private String updateSeq;
    private String previewSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        // Register for changes
        automaticUpdatesKey = getString(R.string.pref_background_update_key);
        automaticNotification = getString(R.string.pref_notification_key);
        updateSeq = getString(R.string.pref_update_seq_key);
        previewSize = getString(R.string.pref_preview_size_key);

        findPreference(automaticUpdatesKey).setOnPreferenceChangeListener(this);
        findPreference(automaticNotification).setOnPreferenceChangeListener(this);
        findPreference(previewSize).setOnPreferenceChangeListener(this);

        findPreference(updateSeq).setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if(automaticUpdatesKey.equals(key)) {
            onAutomaticUpdatesToggle((Boolean) newValue);
        }else if(automaticNotification.equals(key)){
            onAutomaticNotificationToggle((Boolean)newValue);
        }else if(previewSize.equals(key)){
            setPreviewSize((String) newValue);

        }else if(updateSeq.equals(key)){
            setUpdateSeq((String) newValue);

        }
        else {
            throw new RuntimeException("Unknown preference");
        }

        return true;
    }


    private void onAutomaticNotificationToggle(Boolean isEnabled) {
        if (isEnabled){
            Log.d(TAG, "Enable Notifications");
            BackgroundDataFetcher.areNotificationsAllowed = true;
        }else{
            Log.d(TAG, "Enable Notifications");
            BackgroundDataFetcher.areNotificationsAllowed = false;

        }
    }

    private void onAutomaticUpdatesToggle(Boolean isEnabled) {
        if(isEnabled) {
            Log.d(TAG, "Enable Updates");
            findPreference(automaticNotification).setEnabled(true);
            scheduleJob(getActivity());

        }
        else {
            Log.d(TAG, "Disable Updates");
            findPreference(automaticNotification).setEnabled(false);
            ((SwitchPreference)findPreference(automaticNotification)).setChecked(false);
            ((JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(1234);
        }
    }


    public static int getPreviewSize(){
            return previewSizeValue;
    }

    public static int getUpdateSeq(){
        return updateSeqValue;
    }

    public static void setUpdateSeq(String newValue){
        updateSeqValue = Integer.parseInt(newValue);
    }
    public static void setPreviewSize(String newValue){
        previewSizeValue = Integer.parseInt(newValue);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!((SwitchPreference)findPreference(automaticUpdatesKey)).isChecked())
            findPreference(automaticNotification).setEnabled(false);
        else{
            findPreference(automaticNotification).setEnabled(true);

        }

         }
}
