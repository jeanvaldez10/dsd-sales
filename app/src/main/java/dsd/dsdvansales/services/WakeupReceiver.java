package dsd.dsdvansales.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import androidx.legacy.content.WakefulBroadcastReceiver;

import dsd.dsdvansales.global.Utils;


public class WakeupReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        @SuppressLint("InvalidWakeLockTag")
        PowerManager.WakeLock newWakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(1, "AlarmReceiver");
        newWakeLock.acquire();
        Utils.setAlarm(context);
        //Upload GPS Poll Data
        final long lCurTime = System.currentTimeMillis();

        if(Utils.lastRecordedLocation != null && lCurTime - Utils.lastRecordedLocation.getTime() <= 1 * 60 * 1000)
        {
            new Thread()
            {
                @Override
                public void run() {
                    Utils.registerChaseLog("1", lCurTime);
                    Utils.uploadGPSPollData(context, Utils.lastRecordedLocation, lCurTime);
                }
            }.start();

        }
        else if(Utils.lastRecordedLocation != null && lCurTime - Utils.lastRecordedLocation.getTime() > 1 * 60 * 1000)
        {
            new Thread()
            {
                @Override
                public void run() {
                    Utils.registerChaseLog("2", lCurTime);
                    Utils.recordGPSUploadTime(context, lCurTime);
                }
            }.start();

        }
        else
        {
            new Thread()
            {
                @Override
                public void run() {
                    Utils.registerChaseLog("0", lCurTime);
                }
            }.start();

        }

        newWakeLock.release();
    }
}
