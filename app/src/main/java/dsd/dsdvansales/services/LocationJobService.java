package dsd.dsdvansales.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Utils;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LocationJobService extends JobService{
    public static final int LOCATION_SERVICE_JOB_ID = 111;
    public static final String JOB_STATE_CHANGED = "jobStateChanged";
    public static boolean isJobRunning = false;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        startLocationUpdates();
        isJobRunning = true;
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        stopLocationUpdates();
        isJobRunning = false;
        jobFinished(jobParameters,false);
        return true;
    }

    private void startLocationUpdates()
    {
        if(isJobRunning) return;
        Utils.startLocationUpdates(getApplicationContext());

        Intent jobStartedMessage = new Intent(JOB_STATE_CHANGED);
        jobStartedMessage.putExtra("isStarted",true);
        Log.d("send broadcast"," as job started");
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(jobStartedMessage);
        createNotification();

    }

    private void stopLocationUpdates()
    {
        getSharedPreferences("track",MODE_PRIVATE).edit().putBoolean("isServiceStarted",false).apply();
        Intent jobStoppedMessage = new Intent(JOB_STATE_CHANGED);
        jobStoppedMessage.putExtra("isStarted",false);
        Log.d("broadcasted","job state change");
        removeNotification();
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(jobStoppedMessage);
    }

    private void createNotification()
    {
        Notification.Builder mBuilder = new Notification.Builder(
                getBaseContext());
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = mBuilder.setSmallIcon(R.drawable.icon_notification).setTicker("Tracking").setWhen(0)
                    .setAutoCancel(false)
                    .setCategory(Notification.EXTRA_BIG_TEXT)
                    .setContentTitle("Tracking")
                    .setContentText("Your trip in progress")
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setColor(getColor(R.color.top_banner_color))
                    .setStyle(new Notification.BigTextStyle()
                            .bigText("DSDVanSales is now tracking your location"))
                    .setChannelId("track_marty")
                    .setShowWhen(true)
                    .setOngoing(true)
                    .build();
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notification = mBuilder.setSmallIcon((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)? R.drawable.icon_notification:R.drawable.icon_notification).setTicker("Tracking").setWhen(0)
                        .setAutoCancel(false)
                        .setCategory(Notification.EXTRA_BIG_TEXT)
                        .setContentTitle("Tracking")
                        .setColor(getColor(R.color.top_banner_color))
                        .setContentText("DSDVanSales is now tracking your location")
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setStyle(new Notification.BigTextStyle()
                                .bigText("DSDVanSales is now tracking your location"))
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setShowWhen(true)
                        .setOngoing(true)
                        .build();
            }
            else
            {
                notification = mBuilder.setSmallIcon((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)? R.mipmap.ic_launcher:R.mipmap.ic_launcher).setTicker("Tracking").setWhen(0)
                        .setAutoCancel(false)
                        .setCategory(Notification.EXTRA_BIG_TEXT)
                        .setContentTitle("Tracking")
                        .setContentText("Track in progress")
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setStyle(new Notification.BigTextStyle()
                                .bigText("Track in progress"))
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setShowWhen(true)
                        .setOngoing(true)
                        .build();
            }
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("track_marty", "Track", NotificationManager.IMPORTANCE_HIGH);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(mChannel);
        }
        startForeground(1,notification); //for foreground service, don't use 0 as id. it will not work.
    }

    private void removeNotification()
    {
        stopForeground(true); // use this for foreground service
    }

}
