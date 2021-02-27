package com.roimaa.reminderer;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ReminderWorker extends Worker {
    private static final String TAG = ReminderWorker.class.getSimpleName();

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        int id = getInputData().getInt("id", -1);
        // TODO: Show notification
        Log.d(TAG, "Working for " + id);
        return Result.success();
    }
}
