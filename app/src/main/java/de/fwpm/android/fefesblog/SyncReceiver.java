package de.fwpm.android.fefesblog;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by alex on 22.01.18.
 */

public class SyncReceiver extends BroadcastReceiver {

    private static final String TAG = "SYNC";

    private Context mContext;
    int newPosts;
    int updates;

    @Override
    public void onReceive(Context context, Intent intent) {

        updates = intent.getIntExtra("Update", 0);
        newPosts = intent.getIntExtra("New", 0);

        Log.d(TAG, "onReceive: ");
        mContext = context;
        pushNotification();

    }

    private void pushNotification() {
        makeNotificationChannel();

        int time = (int) System.currentTimeMillis();
        Notification notification = null;


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(mContext)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Fefes Blog")
                    .setContentText("New Posts: " + newPosts + ", Updates: " + updates)
                    .setChannelId("my_channel_01")
                    .build();
        } else {
            notification = new Notification.Builder(mContext)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Fefes Blog")
                    .setContentText("New Posts: " + newPosts + ", Updates: " + updates)
                    .build();
        }

        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(NOTIFICATION_SERVICE);

        System.currentTimeMillis();
        notificationManager.notify(time, notification);
    }

    public void makeNotificationChannel() {

        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);

        String id = "my_channel_01";
        CharSequence name = ("Channel Name");
        String description = ("Channel Description");
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel mChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(id, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
        }

    }

}
