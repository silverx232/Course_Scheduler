package wgu.c196.rachel.coursescheduler.util;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import wgu.c196.rachel.coursescheduler.R;

/**
 * Broadcast Receiver class that listens for notifications.
 *
 * <p> This class is used to receive notifications about the start or end of a course or
 * assessment. </p>
 */
public class AlertReceiver extends BroadcastReceiver {
    /**
     * Tag used for Intent to identify which AlertChannel channel to use
     */
    public static final String CHANNEL = "channel";
    /**
     * Tag used for Intent to pass a notification ID
     */
    public static final String NOTIFICATION_ID = "notification id";
    /**
     * Tag used for Intent to pass the notification's message
     */
    public static final String MESSAGE = "message";

    /**
     * Builds the notification using the information provided in an Intent.
     *
     * @param context The context where the receiver is running
     * @param intent The intent that contains the notification information
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String channel = "";
        int notificationId = 0;
        String message = "";

        // Find out which channel and id to use
        Bundle intentData = intent.getExtras();
        if (intentData != null) {
            channel = intentData.getString(CHANNEL);
            notificationId = intentData.getInt(NOTIFICATION_ID);
            message = intentData.getString(MESSAGE);
        }

        Notification notification = new NotificationCompat.Builder(context, channel)
                .setSmallIcon(R.drawable.ic_baseline_calendar_today_24)
                .setContentTitle("Course Scheduler Alert")
                .setContentText(message)
//                .setContentText(NotificationCompat.CATEGORY_REMINDER)
                .build();

        NotificationManagerCompat.from(context).notify(notificationId, notification);
    }
}
