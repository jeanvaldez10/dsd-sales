package dsd.dsdvansales.global;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadInstance {

    public interface DownloadCallback{
        void startDownload(int nDownloadInstance);
        void downloading(int nDownloadInstance, int nOldProgress, int nNewProgress);
        void endDownload(int nDownloadInstance, String filePath);
    }

    DownloadCallback downloadCallback;
    public String strUrl = "";
    public String strFilePath = "";
    public int mDownloadInstance;

    public DownloadInstance(int nDownloadInstance, String url, DownloadCallback callback)
    {
        this.strUrl = url;
        this.downloadCallback = callback;
        this.mDownloadInstance = nDownloadInstance;
    }

    public void start()
    {
        Utils.createDirectory(Global.PHOTO_DIR);
        new Thread()
        {
            @Override
            public void run() {
                try
                {
                    downloadCallback.startDownload(mDownloadInstance);
                    URL url = new URL(strUrl);
                    strFilePath = Global.PHOTO_DIR + strUrl.substring(strUrl.lastIndexOf("/") + 1);
                    HttpURLConnection c = (HttpURLConnection) url.openConnection();

                    int nProgress = 0;
                    c.setRequestMethod("GET");
                    c.setDoOutput(true);

                    c.connect();

                    int nFileSize = c.getContentLength();

                    File file = new File(strFilePath);
                    if(file.exists() && file.length() == nFileSize)
                    {
                        downloadCallback.downloading(mDownloadInstance, 0, 100);
                        sleep(100);
                        downloadCallback.endDownload(mDownloadInstance, strFilePath);
                        return;
                    }
                    FileOutputStream f = new FileOutputStream(file);
                    InputStream in = new BufferedInputStream(url.openStream(), 4096);
                    byte[] buffer = new byte[1024 * 1000];
                    int len1;

                    int oldProgress = 0;

                    while ((len1 = in.read(buffer)) > 0) {
                        f.write(buffer, 0, len1);
                        nProgress += len1;
                        int progress = (int)(nProgress * 1.0f / nFileSize * 100f);
                        if(progress - oldProgress > 1)
                        {
                            downloadCallback.downloading(mDownloadInstance, oldProgress, nProgress);
                            oldProgress = progress;
                        }
                    }
                    downloadCallback.downloading(mDownloadInstance, oldProgress, 100);
                    try
                    {
                        f.flush();
                        f.close();
                        in.close();
                        c.disconnect();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    downloadCallback.endDownload(mDownloadInstance, strFilePath);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
