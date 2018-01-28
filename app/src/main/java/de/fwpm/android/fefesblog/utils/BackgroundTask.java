package de.fwpm.android.fefesblog.utils;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import de.fwpm.android.fefesblog.SyncJobScheduler;
import de.fwpm.android.fefesblog.fragments.SettingFragment;

import static de.fwpm.android.fefesblog.fragments.SettingFragment.UPDATE_INTERVALL;
import static de.fwpm.android.fefesblog.fragments.SettingFragment.UPDATE_ITNVERVALL_DEFAULT;

/**
 * Created by alex on 25.01.18.
 */

public class BackgroundTask {

    public static void scheduleJob(Context context) {

        final JobScheduler jobScheduler = (JobScheduler) context.getSystemService(
                Context.JOB_SCHEDULER_SERVICE);

        final ComponentName name = new ComponentName(context, SyncJobScheduler.class);
        final int result = jobScheduler.schedule(getJobInfo(1234, PreferenceManager.getDefaultSharedPreferences(context).getInt(UPDATE_INTERVALL, UPDATE_ITNVERVALL_DEFAULT), name));

        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.d("SYNC", "Scheduled job successfully!");
        }

    }

    private static JobInfo getJobInfo(final int id, final long intervall, final ComponentName name) {
        final boolean isPersistent = true; // persist through boot
        final int networkType = JobInfo.NETWORK_TYPE_ANY;

        final JobInfo jobInfo;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            jobInfo = new JobInfo.Builder(id, name)
                    .setPeriodic(intervall)
                    .setRequiredNetworkType(networkType)
                    .setPersisted(isPersistent)
                    .build();
        } else {
            jobInfo = new JobInfo.Builder(id, name)
                    .setPeriodic(intervall)
                    .setRequiredNetworkType(networkType)
                    .setPersisted(isPersistent)
                    .build();
        }

        return jobInfo;
    }


}
