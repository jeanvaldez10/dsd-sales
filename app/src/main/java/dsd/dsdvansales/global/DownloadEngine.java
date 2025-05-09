package dsd.dsdvansales.global;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import dsd.dsdvansales.db.DownloadDbAdapter;
import dsd.dsdvansales.model.DownloadItem;

public class DownloadEngine {

    private static DownloadEngine engine;
    private DownloadDbAdapter dbAdapter;
    private List<DownloadItem> downloadItemList = new ArrayList<>();
    private Thread mThread;
    private boolean bRunning = false;
    public static void initEngine()
    {
        if(engine == null)
        {
            engine = new DownloadEngine();
        }
    }

    public static DownloadEngine getEngine()
    {
        return engine;
    }

    private DownloadEngine()
    {
        dbAdapter = new DownloadDbAdapter();
    }

    public void init()
    {
        if(dbAdapter != null)
        {
            dbAdapter.open();
            dbAdapter.deleteAllDownloadItem();
            dbAdapter.close();
            downloadItemList.clear();
            Utils.deleteDirectory(Global.PRODUCT_DOWNLOAD_DIR);
        }
    }

    public void loadAllDataToCache()
    {
        dbAdapter.open();
        downloadItemList.clear();
        downloadItemList.addAll(dbAdapter.getAllDownloadItem());
        dbAdapter.close();
    }

    public void addDownloadData(String strItemNo, String strUrl)
    {
        DownloadItem downloadItem = new DownloadItem();
        downloadItem.strItemNo = strItemNo;
        downloadItem.strUrl = strUrl;
        downloadItemList.add(downloadItem);
    }

    public void flushToFile()
    {
        dbAdapter.open();
        dbAdapter.beginTransacion();
        for(DownloadItem item : downloadItemList)
        {
            dbAdapter.addDownloadItem(item.strItemNo, item.strUrl);
        }
        dbAdapter.endTransaction();
        dbAdapter.close();
    }

    public void stop()
    {
        bRunning = false;
    }

    public void run()
    {
        bRunning = true;
        mThread = new Thread()
        {
            @Override
            public void run() {
                Utils.createDirectory(Global.PRODUCT_DOWNLOAD_DIR);
                while(true)
                {
                    List<DownloadItem> tmpList = new ArrayList<>();
                    tmpList.addAll(downloadItemList);
                    downloadItemList.clear();

                    for(DownloadItem item : tmpList)
                    {
                        try
                        {
                            if(!bRunning) break;
                            URL url = new URL(item.strUrl);
                            String strFilePath = Global.PRODUCT_DOWNLOAD_DIR + item.strItemNo + ".jpg";
                            HttpURLConnection c = (HttpURLConnection) url.openConnection();

                            int nProgress = 0;
                            c.setRequestMethod("GET");
                            c.setDoOutput(true);

                            c.connect();

                            int nFileSize = c.getContentLength();

                            File file = new File(strFilePath);
                            if(file.exists() && file.length() == nFileSize)
                            {
                                c.disconnect();
                                continue;
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
                                    oldProgress = progress;
                                }
                            }
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

                            //Delete from the db.
                            dbAdapter.open();
                            dbAdapter.deleteDownloadItem(item.strItemNo);
                            dbAdapter.close();

                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }

                    if(downloadItemList.size() == 0)
                        break;
                }

                bRunning = false;
                mThread = null;
            }
        };
        mThread.start();
    }

    public Thread getWorkingThread()
    {
        return mThread;
    }

}
