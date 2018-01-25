package de.fwpm.android.fefesblog.fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.SyncReceiver;


/**
 * Created by Daniel.Eschenbacher on 25.01.2018.
 */

public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "SETTINGFRAGMENT";

    private String automaticUpdatesKey;
    private String automaticNotification;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        // Register for changes
        automaticUpdatesKey = getString(R.string.pref_background_update_key);
        automaticNotification = getString(R.string.pref_notification_key);
        findPreference(automaticUpdatesKey).setOnPreferenceChangeListener(this);
        findPreference(automaticNotification).setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if(automaticUpdatesKey.equals(key)) {
            onAutomaticUpdatesToggle((Boolean) newValue);
        }else if(automaticNotification.equals(key)){
            onAutomaticNotificationToggle((Boolean)newValue);
        }
        else {
            throw new RuntimeException("Unknown preference");
        }

        return true;
    }

    private void onAutomaticNotificationToggle(Boolean isEnabled) {
        if (isEnabled){
            Log.d(TAG, "Enable Notifications");
            SyncReceiver.areNotificationsAllowed = true;
        }else{
            Log.d(TAG, "Enable Notifications");
            SyncReceiver.areNotificationsAllowed = false;

        }
    }

    private void onAutomaticUpdatesToggle(Boolean isEnabled) {
        if(isEnabled) {
            Log.d(TAG, "Enable Updates");
        }
        else {
            Log.d(TAG, "Disable Updates");
        }
    }
}