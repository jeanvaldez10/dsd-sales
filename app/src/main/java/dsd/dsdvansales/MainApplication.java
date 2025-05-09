package dsd.dsdvansales;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.bugsee.library.Bugsee;
import com.bugsee.library.LaunchOptions;
import com.bugsee.library.attachment.CustomAttachment;
import com.bugsee.library.attachment.Report;
import com.bugsee.library.attachment.ReportAttachmentsProvider;
import com.bugsee.library.data.IssueSeverity;
import com.bugsee.library.data.IssueType;
import com.bugsee.library.serverapi.data.Stream;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.ServiceZone;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import dsd.dsdvansales.api.UploadApiClient;
import dsd.dsdvansales.chat.ChatConfigs;
import dsd.dsdvansales.chat.ConfigUtils;
import dsd.dsdvansales.chat.CoreConfigUtils;
import dsd.dsdvansales.chat.QbConfigs;
import dsd.dsdvansales.global.EncodingConverter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.HttpsTrustManager;
import dsd.dsdvansales.global.Utils;
import okhttp3.OkHttpClient;
import okhttp3.Cache;

/**
 * Created by silve on 7/5/2016.
 */
public class MainApplication extends MultiDexApplication {

    private static MainApplication instance;
    private static final String QB_CONFIG_DEFAULT_FILE_NAME = "qb_config.json";
    private QbConfigs qbConfigs;
    private static ChatConfigs chatConfigs;


    @Override
    public void onCreate() {
        super.onCreate();

        Global.APPLICATION_FOLDER_PATH = getFilesDir().getAbsolutePath();

        HttpsTrustManager.allowAllSSL();
        instance = this;
        String strServerIP = Utils.getStringSetting(this, Global.PREF_FTP_SERVER_IP);
        if(!TextUtils.isEmpty(strServerIP))
        {
            UploadApiClient.API_ROOT = "http://" + strServerIP;
        }

        //Utils.buildBaseURLMap();
        initQbConfigs();
        initCredentials();
        initSampleConfigs();
    }

    private void checkUpdates()
    {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);

// Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

// Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // This example applies an immediate update. To apply a flexible update
                    // instead, pass in AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Request the update.
            }
        });
    }

    private void initQbConfigs() {
        qbConfigs = CoreConfigUtils.getCoreConfigsOrNull(getQbConfigFileName());
    }

    public static synchronized MainApplication getInstance() {
        return instance;
    }

    public void initCredentials(){
        if (qbConfigs != null) {
            QBSettings.getInstance().init(getApplicationContext(), qbConfigs.getAppId(), qbConfigs.getAuthKey(), qbConfigs.getAuthSecret());
            QBSettings.getInstance().setAccountKey(qbConfigs.getAccountKey());

            if (!TextUtils.isEmpty(qbConfigs.getApiDomain()) && !TextUtils.isEmpty(qbConfigs.getChatDomain())) {
                QBSettings.getInstance().setEndpoints(qbConfigs.getApiDomain(), qbConfigs.getChatDomain(), ServiceZone.PRODUCTION);
                QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);
            }
        }
    }

    public QbConfigs getQbConfigs(){
        return qbConfigs;
    }

    protected String getQbConfigFileName(){
        return QB_CONFIG_DEFAULT_FILE_NAME;
    }

    private void initSampleConfigs() {
        try {
            chatConfigs = ConfigUtils.getSampleConfigs(Global.SAMPLE_CONFIG_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ChatConfigs getSampleConfigs() {
        return chatConfigs;
    }
}
