package com.roimaa.reminderer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.roimaa.reminderer.DB.Reminder;

public class ReminderWorker extends Worker {
    private static final String TAG = ReminderWorker.class.getSimpleName();
    private static final String NTF_CHANNEL = "RemindererNtfChannel";

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        int id = getInputData().getInt("id", -1);

        Log.d(TAG, "Working for " + id);

        Reminder added = DBHelper.getInstance(getApplicationContext()).getReminder(id);
        if (null != added) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

            NotificationChannel channel = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel = new NotificationChannel(NTF_CHANNEL, getApplicationContext().getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH);
                channel.enableLights(true);
                channel.enableVibration(true);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), NTF_CHANNEL)
                    .setContentTitle(getApplicationContext().getString(R.string.notification_title))
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentText(added.getMessage())
                    .setAutoCancel(true);

            Intent resultIntent = new Intent(getApplicationContext(), AddReminder.class);
            resultIntent.putExtra("reminderId", added.getId());
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            notification.setContentIntent(resultPendingIntent);

            Intent deleteIntent = new Intent(getApplicationContext(), WorkManagerService.class);
            deleteIntent.putExtra(WorkManagerService.Id, added.getId());
            deleteIntent.putExtra(WorkManagerService.Action, WorkManagerService.DELETE);
            PendingIntent deletePendingIntent = PendingIntent.getService(getApplicationContext(), 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notification.addAction(android.R.drawable.ic_delete, getApplicationContext().getString(R.string.delete), deletePendingIntent);

            notificationManager.notify(added.getId(), notification.build());

            return Result.success();
        }
        return Result.failure();
    }
}
