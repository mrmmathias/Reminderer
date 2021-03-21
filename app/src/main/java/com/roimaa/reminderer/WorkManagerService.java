package com.roimaa.reminderer;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.roimaa.reminderer.DB.Reminder;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class WorkManagerService extends Service {
    public static final String Id = "id";
    public static final String Action = "action";
    public static final int ADD = 1;
    public static final int REMOVE = 2;
    public static final int UPDATE = 3;
    public static final int DELETE = 4;

    private static final String TAG = WorkManagerService.class.getSimpleName();
    private static final int GEOFENCE_RADIUS = 100;
    private static final int GEOFENCE_EXPIRATION = 10 * 60 * 1000;
    private static final int GEOFENCE_DWELL_DELAY = 30 * 1000;

    private Looper mServiceLooper;
    private WorkManagerServiceHandler mServiceHandler;

    public WorkManagerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private final class WorkManagerServiceHandler extends Handler {
        public WorkManagerServiceHandler(Looper looper) {
            super(looper);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ADD:
                    Log.d(TAG, "ADD: " + msg.arg1);
                    Reminder added = DBHelper.getInstance(getApplicationContext()).getReminder(msg.arg1);
                    if (null != added) {
                        Log.d(TAG, "Lat:" + added.getLat());
                        if (-1 != added.getLat()) {
                            // Here we handle reminders with location set
                            GeofencingClient fencingClient = LocationServices.getGeofencingClient(getApplicationContext());

                            Geofence fence = new Geofence.Builder()
                                    .setRequestId(String.valueOf(added.getId()))
                                    .setCircularRegion(added.getLat(), added.getLon(), Float.valueOf(GEOFENCE_RADIUS))
                                    .setExpirationDuration(Long.valueOf(GEOFENCE_EXPIRATION))
                                    .setLoiteringDelay(GEOFENCE_DWELL_DELAY)
                                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                                    .build();

                            GeofencingRequest.Builder fenceReqBuilder = new GeofencingRequest.Builder();
                            fenceReqBuilder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_DWELL);
                            fenceReqBuilder.addGeofence(fence);

                            GeofencingRequest fenceReq = fenceReqBuilder.build();

                            fencingClient.addGeofences(fenceReq, getGeofencePendingIntent(added.getId()))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "GeoFence added succesfully.");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "GeoFence failed", e);
                                        }
                                    });
                        } else {
                            Data workerData = new Data.Builder()
                                    .putInt("id", added.getId())
                                    .build();

                            long timeout = added.getReminderTime().getTime() - new Date().getTime();

                            WorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                                    .addTag(String.valueOf(msg.arg1))
                                    .setInputData(workerData)
                                    .setInitialDelay(timeout, TimeUnit.MILLISECONDS)
                                    .build();

                            WorkManager.getInstance(getApplicationContext()).enqueue(uploadWorkRequest);
                        }
                    }
                    break;

                case REMOVE:
                    Log.d(TAG, "REMOVE: " + msg.arg1);
                    Reminder removed = DBHelper.getInstance(getApplicationContext()).getReminder(msg.arg1);
                    if (null != removed) {
                        WorkManager.getInstance(getApplicationContext())
                                .cancelAllWorkByTag(String.valueOf(msg.arg1));
                    }
                    break;

                case UPDATE:
                    Log.d(TAG, "UPDATE: " + msg.arg1);
                    Reminder updated = DBHelper.getInstance(getApplicationContext()).getReminder(msg.arg1);
                    if (null != updated) {
                        WorkManager.getInstance(getApplicationContext())
                                .cancelAllWorkByTag(String.valueOf(msg.arg1));

                        // UPDATE actually cancels the previous work and creates a new one.
                        Message updateMsg = mServiceHandler.obtainMessage();
                        updateMsg.copyFrom(msg);
                        updateMsg.what = ADD;
                        this.sendMessage(updateMsg);
                    }
                    break;

                case DELETE:
                    Log.d(TAG, "DELETE: " + msg.arg1);
                    Reminder deleted = DBHelper.getInstance(getApplicationContext()).getReminder(msg.arg1);
                    if (null != deleted) {
                        DBHelper.getInstance(getApplicationContext()).deleteReminder(msg.arg1);
                    }
                    break;

                default:
                    super.handleMessage(msg);
            }

            stopSelf(msg.arg2);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        HandlerThread thread = new HandlerThread("WorkManagerServiceHandlerThread",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new WorkManagerServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        int id = intent.getIntExtra(Id, -1);
        int action = intent.getIntExtra(Action, -1);

        Message msg = mServiceHandler.obtainMessage();
        msg.what = action;
        msg.arg1 = id;
        msg.arg2 = startId;
        mServiceHandler.sendMessage(msg);

        return START_STICKY;
    }

    private PendingIntent getGeofencePendingIntent(int Id) {
        Intent intent = new Intent(this, RemindererGeofenceReceiver.class);
        intent.putExtra("id", Id);
        return PendingIntent.getBroadcast(this, Id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}