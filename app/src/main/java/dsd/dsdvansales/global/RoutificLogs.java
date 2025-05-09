package dsd.dsdvansales.global;

import static android.content.ContentValues.TAG;

import android.icu.text.SimpleDateFormat;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class RoutificLogs {
    private static RoutificLogs __instance = null;

    public static RoutificLogs getInstance()
    {
        if(__instance == null)
        {
            __instance = new RoutificLogs();
        }
        return __instance;
    }

    public void WriteLogs(String strLogs)
    {
        String strCurTime = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
           strCurTime = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(new Date());
        }

        String strData = strCurTime + " : " + strLogs + "\n\n";
        File logFile = new File(Global.ROUTEFIC_LOG_FILE_PATH);
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(strData);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            Log.e(TAG, "Error writing to log file", e);
        }
    }
}
