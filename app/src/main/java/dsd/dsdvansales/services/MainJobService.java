package dsd.dsdvansales.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;

import androidx.annotation.RequiresApi;

import dsd.dsdvansales.global.Utils;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainJobService extends JobService{
    public static final int MAIN_SERVICE_JOB_ID = 110;
    public static final String JOB_STATE_CHANGED = "jobStateChanged";
    public static boolean isJobRunning = false;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Utils.startMainJob(getApplicationContext());
        isJobRunning = true;
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Utils.endMainJob();
        isJobRunning = false;
        jobFinished(jobParameters,false);
        return true;
    }


}
