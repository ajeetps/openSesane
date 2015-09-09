package com.example.ajeetps.userApp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

/**
 * Created by sachinp on 09/09/15.
 */
public class CustomNotificationManager {

    // Sets an ID for the notification
    int mNotificationId = 1;

    public static final String CURRENT_TASK_FILE = "com.google.keepitup.keepitup.CURRENT_TASK_FILE";
    public static final String CURRENT_TASK_ID = "com.google.keepitup.keepitup.CURRENT_TASK_ID";
    public static final String CURRENT_TASK_LIST_ID = "com.google.keepitup.keepitup.CURRENT_TASK_LIST_ID";

    public void showNotification(Context context, String message) {
        // Gets an instance of the CustomNotificationManager service
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mNotificationId);

        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);  // TYPE_NOTIFICATION

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.cast_ic_notification_0)
                .setContentTitle("Please unlock this device to open the door")
                .setContentText("Please unlock this device to open the door ")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setColor(Color.RED)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setLights(Color.RED, 3000, 3000)
                .setSound(uri);


        // Builds the notification and issues it.
        notificationManager.notify(mNotificationId, notificationBuilder.build());
    }

    public void clearNotification(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mNotificationId);
    }
}