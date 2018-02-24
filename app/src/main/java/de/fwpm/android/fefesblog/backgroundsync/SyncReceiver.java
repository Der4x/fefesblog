package de.fwpm.android.fefesblog.backgroundsync;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static de.fwpm.android.fefesblog.backgroundsync.NotificationHelper.NOTIFICATION_ID;
import static de.fwpm.android.fefesblog.backgroundsync.NotificationHelper.createNotificationBuilder;
import static de.fwpm.android.fefesblog.backgroundsync.NotificationHelper.makeNotificationChannel;

/**
 * Created by alex on 22.01.18.
 */

public class SyncReceiver extends BroadcastReceiver {

    private static final String TAG = "SYNC";

    @Override
    public void onReceive(Context context, Intent intent) {

            int updates = intent.getIntExtra("Update", 0);
            String newPosts = intent.getStringExtra("NewPosts");

            String[] postsnippets = (!newPosts.equals("") ? newPosts.split("/;/") : new String[]{});

            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            makeNotificationChannel(mNotificationManager);

            mNotificationManager.notify(NOTIFICATION_ID, createNotificationBuilder(context, postsnippets, updates).build());

            Log.d(TAG, "onReceive: ");

    }

}
