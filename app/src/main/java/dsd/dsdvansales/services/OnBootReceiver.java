package dsd.dsdvansales.services;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Utils;

/**
 * Created by silve on 4/27/2017.
 */

public class OnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        startMainService(context);
    }

    public void startMainService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(Utils.mMainThread == null || !Utils.mMainThread.isAlive() || Utils.mMainThread.isInterrupted())
            {
                if(Utils.mMainThread != null) Utils.mMainThread.isRunning = false;
                JobScheduler jobScheduler =
                        (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                assert jobScheduler != null;
                jobScheduler.schedule(new JobInfo.Builder(MainJobService.MAIN_SERVICE_JOB_ID,
                        new ComponentName(context, MainJobService.class))
                        .setOverrideDeadline(500)
                        .setRequiresCharging(false)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                        .setPersisted(true)
                        .setRequiresDeviceIdle(false)
                        .build());
            }
        }
        else
        {
            if (Utils.isServiceExisted(context, Global.SERVICE_CLASS_NAME) != null) {
                Utils.printDebug(null, "MainService already exist.");
            } else {
                Utils.printDebug(null, "MainService created.");
                context.startService(new Intent(context, MainService.class));
            }
        }
    }
}
