package com.roimaa.reminderer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.roimaa.reminderer.DB.Reminder;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RemindererGeofenceReceiver extends BroadcastReceiver {
    private final static String TAG = RemindererGeofenceReceiver.class.getSimpleName();
    private final static long FIVE_MINUTES = 5 * 60 * 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Broadcast received");

        if (null == intent) return;

        int id = intent.getIntExtra("id", -1);
        if (-1 == id) return;

        Reminder received = DBHelper.getInstance(context).getReminder(id);
        if (null == received) return;

        if (checkTime(received.getReminderTime())) {
            Data workerData = new Data.Builder()
                    .putInt("id", id)
                    .build();

            WorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                    .addTag(String.valueOf(id))
                    .setInputData(workerData)
                    .setInitialDelay(0, TimeUnit.MILLISECONDS)
                    .build();

            WorkManager.getInstance(context).enqueue(uploadWorkRequest);
        }
    }

    private boolean checkTime(Date when) {
        // This function checks if reminder time is about now
        if (null == when) return true;

        Date now = new Date();
        long difference = when.getTime() - now.getTime();
        return (difference > -FIVE_MINUTES && difference < FIVE_MINUTES);
    }
}