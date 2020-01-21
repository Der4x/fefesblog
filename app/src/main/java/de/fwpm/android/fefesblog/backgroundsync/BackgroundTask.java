package de.fwpm.android.fefesblog.backgroundsync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Random;
import java.util.UUID;

import de.fwpm.android.fefesblog.App;

import static de.fwpm.android.fefesblog.fragments.SettingFragment.UPDATE_INTERVALL;
import static de.fwpm.android.fefesblog.fragments.SettingFragment.UPDATE_ITNVERVALL_DEFAULT;

/**
 * Created by alex on 25.01.18.
 */

public class BackgroundTask {

    public static void scheduleJob() {

        final JobScheduler jobScheduler = (JobScheduler) App.getInstance().getSystemService(Context.JOB_SCHEDULER_SERVICE);

        final ComponentName name = new ComponentName(App.getInstance(), SyncJobScheduler.class);
        final int result = jobScheduler.schedule(getJobInfo(1234, PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getInt(UPDATE_INTERVALL, UPDATE_ITNVERVALL_DEFAULT), name));

        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.d("SYNC", "Scheduled job successfully!");
        }

    }

    private static JobInfo getJobInfo(final int id, final long intervall, final ComponentName name) {

        return new JobInfo.Builder(id, name)
                    .setPeriodic(intervall)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .build();

    }


}
