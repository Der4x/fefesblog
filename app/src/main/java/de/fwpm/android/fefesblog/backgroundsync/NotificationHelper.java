package de.fwpm.android.fefesblog.backgroundsync;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import de.fwpm.android.fefesblog.MainActivity;
import de.fwpm.android.fefesblog.R;

/**
 * Created by alex on 24.01.18.
 */

public class NotificationHelper {

    public static final String CHANNEL_ID = "fefes_blog";
    public static final int NOTIFICATION_ID = 5353;

    public static PendingIntent getResultPendingIntent(Context context) {

        Intent resultIntent = new Intent(context, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    public static NotificationCompat.Builder createNotificationBuilder(Context context, String[] newPosts, int updates) {

        String title = "Neuigkeiten von Fefes Blog";
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        String message;

        if(newPosts == null || newPosts.length == 0) {

            message = "Es gibt " + ((updates > 1) ? (updates + " neue Updates") : ("ein neues Update"));

        } else {

            message = newPosts[0];
            int more = 0;
            if(newPosts.length >= 5) more = newPosts.length - 5;

            if(more == 0) {

                for (int i = 0; i < newPosts.length; i++) {
                    inboxStyle.addLine(newPosts[i]);
                }
                if(updates > 0) inboxStyle.setSummaryText(updates + ((updates > 1) ? " Updates" : " Update"));

            } else {

                for (int i = 0; i < 5; i++) {
                    inboxStyle.addLine(newPosts[i]);
                }
                inboxStyle.setSummaryText("und " + more + " weitere");

            }

            inboxStyle.setBigContentTitle(title);

        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_rss_feed)
                .setContentTitle(title)
                .setContentText(message)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setContentIntent(getResultPendingIntent(context))
                .setAutoCancel(true);

        if(newPosts != null && newPosts.length == 1)
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(newPosts[0]));
        else if(newPosts != null && newPosts.length > 0)
            builder.setStyle(inboxStyle);

        return builder;

    }

    public static void makeNotificationChannel(NotificationManager mNotificationManager) {

        String id = CHANNEL_ID;
        CharSequence name = ("Fefes Blog");
        String description = ("Neuigkeiten von Fefes Blog");
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel mChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(id, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            mNotificationManager.createNotificationChannel(mChannel);
        }

    }

}
