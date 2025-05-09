package dsd.dsdvansales.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;

import androidx.annotation.RequiresApi;

import dsd.dsdvansales.global.DownloadEngine;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DownloadJobService extends JobService{
    public static final int MAIN_SERVICE_JOB_ID = 111;
    public static final String JOB_STATE_CHANGED = "jobStateChanged";
    public static boolean isJobRunning = false;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        DownloadEngine engine = DownloadEngine.getEngine();
        if(engine == null)
        {
            DownloadEngine.initEngine();
            engine = DownloadEngine.getEngine();
        }

        engine.loadAllDataToCache();
        engine.run();
        isJobRunning = true;
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        DownloadEngine engine = DownloadEngine.getEngine();
        if(engine != null)
        {
            engine.stop();
        }
        isJobRunning = false;
        jobFinished(jobParameters,false);
        return true;
    }


}
