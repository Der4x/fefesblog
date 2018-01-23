package de.fwpm.android.fefesblog;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import de.fwpm.android.fefesblog.data.BackgroundDataFetcher;

/**
 * Created by alex on 22.01.18.
 */

public class SyncJobScheduler extends JobService {

    private static final String TAG = "SYNC";

    BackgroundDataFetcher dataFetcher;

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {

        dataFetcher = new BackgroundDataFetcher(this) {

            @Override
            protected void onPostExecute(Boolean success) {

                jobFinished(jobParameters, !success);
                Log.d(TAG, "onPostExecute: Job finished");

            }

        };
        dataFetcher.execute();
        Log.e(TAG, "StartingBackgroundSync");

        return true;


    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (dataFetcher != null) {
            dataFetcher.cancel(true);
        }
        Log.d(TAG, "onStopJob");
        return true;
    }

}
