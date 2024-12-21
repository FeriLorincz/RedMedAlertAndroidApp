package com.feri.redmedalertandroidapp.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.feri.redmedalertandroidapp.MainActivity;
import com.feri.redmedalertandroidapp.R;
import com.feri.redmedalertandroidapp.util.PermissionHelper;

public class NotificationService {

    private static final String TAG = "NotificationService";
    private static final String CHANNEL_ID_STATUS = "red_med_alert_status";
    private static final String CHANNEL_ID_DATA = "red_med_alert_data";
    private static final int NOTIFICATION_ID_STATUS = 1;
    private static final int NOTIFICATION_ID_DATA = 2;

    private final Context context;
    private final NotificationManagerCompat notificationManager;
    private final NotificationPreferences preferences;

    public NotificationService(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        this.preferences = new NotificationPreferences(context);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal pentru statusul permanent
            NotificationChannel statusChannel = new NotificationChannel(
                    CHANNEL_ID_STATUS,
                    "Status Monitorizare",
                    NotificationManager.IMPORTANCE_LOW
            );
            statusChannel.setDescription("Arată statusul curent al monitorizării");

            // Canal pentru notificări de date
            NotificationChannel dataChannel = new NotificationChannel(
                    CHANNEL_ID_DATA,
                    "Actualizări Date",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            dataChannel.setDescription("Notificări despre colectarea și transmiterea datelor");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(statusChannel);
                manager.createNotificationChannel(dataChannel);
            }
        }
    }

    public Notification createStatusNotification(boolean isCollecting, boolean isUploading) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        String status = getStatusText(isCollecting, isUploading);

        return new NotificationCompat.Builder(context, CHANNEL_ID_STATUS)
                .setContentTitle("RedMedAlert Activ")
                .setContentText(status)
                .setSmallIcon(R.drawable.ic_heart_notification)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build();
    }

    private String getStatusText(boolean isCollecting, boolean isUploading) {
        if (isCollecting && isUploading) {
            return "Monitorizare activă și transmitere date";
        } else if (isCollecting) {
            return "Monitorizare activă";
        } else if (isUploading) {
            return "Transmitere date în curs";
        }
        return "Serviciu activ";
    }

    public void showDataCollectionNotification() {
        if (!PermissionHelper.hasNotificationPermission(context)  || !preferences.showDataCollection())  {
            Log.w(TAG, "Nu avem permisiune pentru notificări sau notificările sunt dezactivate");
            return;
        }

        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_DATA)
                    .setSmallIcon(R.drawable.ic_heart_notification)
                    .setContentTitle("Colectare Date")
                    .setContentText("Se colectează date de la ceas")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            if (preferences.isVibrateEnabled()) {
                builder.setVibrate(new long[]{0, 100, 50, 100});
            }

            if (preferences.isSoundEnabled()) {
                builder.setDefaults(Notification.DEFAULT_SOUND);
            }

            notificationManager.notify(NOTIFICATION_ID_DATA, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Eroare la afișarea notificării: " + e.getMessage());
        }
    }

    public void showDataUploadNotification(boolean success) {
        if (!PermissionHelper.hasNotificationPermission(context) ||
                (!success && !preferences.showErrorNotifications()) ||
                (success && !preferences.showDataUpload())) {
            Log.w(TAG, "Nu avem permisiune sau notificările sunt dezactivate");
            return;
        }

        try {
            String title = success ? "Upload Reușit" : "Upload Eșuat";
            String message = success ?
                    "Datele au fost transmise cu succes" :
                    "Eroare la transmiterea datelor. Se va reîncerca.";

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_DATA)
                    .setSmallIcon(R.drawable.ic_heart_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            if (preferences.isVibrateEnabled()) {
                builder.setVibrate(new long[]{0, 100, 50, 100});
            }

            if (preferences.isSoundEnabled()) {
                builder.setDefaults(Notification.DEFAULT_SOUND);
            }

            notificationManager.notify(NOTIFICATION_ID_DATA, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Eroare la afișarea notificării: " + e.getMessage());
        }
    }
}