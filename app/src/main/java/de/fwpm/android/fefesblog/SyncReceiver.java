package de.fwpm.android.fefesblog;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import static android.content.Context.NOTIFICATION_SERVICE;
import static de.fwpm.android.fefesblog.NotificationHelper.NOTIFICATION_ID;
import static de.fwpm.android.fefesblog.NotificationHelper.createNotificationBuilder;
import static de.fwpm.android.fefesblog.NotificationHelper.makeNotificationChannel;

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
