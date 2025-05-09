package dsd.dsdvansales.thread;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.zebra.sdk.util.internal.Sleeper;

import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.db.UploadDbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.UploadViaHTTP;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.MakeZipFileInfo;
import dsd.dsdvansales.model.UploadFileInfo;
import dsd.dsdvansales.model.UploadInfo;

public class MainThread extends Thread implements Runnable {

	private static final long THREAD_SLEEP_TIME = 1000 * 1;
	private Context mContext = null;
	public boolean isRunning = true;

	public MainThread(Context mContext) {
		this.mContext = mContext;
		isRunning = true;
	}

	public void run() {
		new Thread(){
			@Override
			public void run()
			{
				while(isRunning == true)
				{
					if( Utils.isConnectingToInternet(mContext) == true )
					{
						if(Utils.isStorageOK(mContext))
						{
							String strFTPServerIP = Utils.getStringSetting(mContext, Global.PREF_FTP_SERVER_IP);
							String strFTPUserName = Utils.getStringSetting(mContext, Global.PREF_FTP_USERNAME);
							String strCompanyName = Utils.getStringSetting(mContext, Global.PREF_FTP_COMPANY_NAME);
							try
							{
								UploadDbAdapter uploadDbAdapter = new UploadDbAdapter(mContext);
								uploadDbAdapter.open();
								List<UploadFileInfo> fileInfo = uploadDbAdapter.getUploadFileList();
								uploadDbAdapter.close();
								if(  fileInfo.size() > 0)
								{
									for(int i = 0; i < fileInfo.size() && !Global.gbStopBackgroundUpload; i++)
									{
										Global.gbBackgroundUploading = true;
										if( fileInfo.get(i).localPath.length() == 0 || fileInfo.get(i).serverPath.length() == 0 ||
												Utils.isFileExist(fileInfo.get(i).localPath) == false)
										{
											uploadDbAdapter.open();
											uploadDbAdapter.deleteUploadFile(fileInfo.get(i).idx);
											uploadDbAdapter.close();
											Utils.leaveLog(mContext, "MainThread", fileInfo.get(i), "Error File, File Not Exist");
										}
										else
										{
											long fileSize = Utils.getFileSize(fileInfo.get(i).localPath);
											if( fileSize == 0 )
											{
												uploadDbAdapter.open();
												uploadDbAdapter.deleteUploadFile(fileInfo.get(i).idx);
												uploadDbAdapter.close();
												Utils.leaveLog(mContext, "MainThread", fileInfo.get(i), "File Size is 0");
											}
											else if( new UploadViaHTTP().uploadFileToFTPServerUsingHttp(strCompanyName, strFTPServerIP, strFTPUserName, fileInfo.get(i).localPath, fileInfo.get(i).serverPath) )
											{
												uploadDbAdapter.open();
												uploadDbAdapter.deleteUploadFile(fileInfo.get(i).idx);
												uploadDbAdapter.close();
												Utils.leaveLog(mContext, "MainThread", fileInfo.get(i), "Uploaded Success using Http");
											}
											else if(!Utils.getStringSetting(mContext, Global.PREF_DisableFTP).equals("1") && Utils.uploadFileToFTPServer(mContext, fileInfo.get(i).localPath, fileInfo.get(i).serverPath) )
											{
												Utils.leaveLog(mContext, "MainThread", fileInfo.get(i), "Uploaded Failed using Http");
												uploadDbAdapter.open();
												uploadDbAdapter.deleteUploadFile(fileInfo.get(i).idx);
												uploadDbAdapter.close();
												Utils.leaveLog(mContext, "MainThread", fileInfo.get(i), "Uploaded Success using FTP");
											}
											else
											{
												Utils.leaveLog(mContext, "MainThread", fileInfo.get(i), "Uploaded Failed using FTP & Http");
											}
										}
										Global.gbBackgroundUploading = false;
									}
								}
								else
								{
									Thread.sleep(THREAD_SLEEP_TIME);
								}
							}catch(Exception e)
							{
								e.printStackTrace();
							}
						}
						else
						{
							try
							{
								Thread.sleep(THREAD_SLEEP_TIME);
							}catch(Exception e)
							{
								e.printStackTrace();
							}
						}
					}
					else
					{
						try
						{
							Thread.sleep(THREAD_SLEEP_TIME * 30);
						}catch(Exception e)
						{
							e.printStackTrace();
						}

					}
				}
			}
		}.start();

		new Thread()
		{
			@Override
			public void run() {
				while(isRunning)
				{
					try {
						if( Utils.isConnectingToInternet(mContext) == true && Global.gNeedToDownload == true )
						{
							Utils.downloadData(mContext);
						}
						Thread.sleep(THREAD_SLEEP_TIME);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();

		//Check the BreakTime
		new Thread(){
			@Override
			public void run() {
				while(isRunning){
					try {
						if(Utils.getStringSetting(mContext, Global.PREF_ENABLE_BREAKS).equals("1") && Utils.getLongSetting(mContext, Global.PREF_BREAK_DOWN_START_TIME) == 0 && Utils.getLongSetting(mContext, Global.PREF_LOGIN_START_TIME) != 0)
						{
							//Get the DescType data from the database.
							DbAdapter dbAdapter = new DbAdapter(mContext);
							dbAdapter.open();
							List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList(Global.DRIVER_BREAK_TYPE_ID);
							dbAdapter.close();

							//In this case, Value5 = Start Time, Value6 = Warning Time.
							for(DescTypeInfo info : descTypeInfoList)
							{
								if(!TextUtils.isEmpty(info.strValue5) || !TextUtils.isEmpty(info.strValue6))
								{
									continue;
								}

								long loginTime = Utils.getLongSetting(mContext, Global.PREF_LOGIN_START_TIME);
								long loginDuration = System.currentTimeMillis() - loginTime;
								int nSetDuration = 120;
								if(!TextUtils.isEmpty(info.strValue2.trim()))
								{
									try{
										nSetDuration = Utils.getIntFromString(info.strValue2.trim());
									}catch (Exception e)
									{
										e.printStackTrace();
									}
								}
								if(nSetDuration == 0) nSetDuration = 120;

								if(loginDuration >= nSetDuration * 60 * 1000)
								{
									Intent intent = new Intent(Global.ACTION_BREAK_TIME_FIRE);
									intent.putExtra(Global.EXTRA_DESCTYPE_INFO, info);
									mContext.sendBroadcast(intent);
									break;
								}
							}
						}

						Thread.sleep(60 * 1000); //Wait for 60 seconds
					}catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}.start();

		UploadInfo info = null;
		while(isRunning == true){
			try
			{
				if(Global.gUploadCustNoList.size() > 0 && !Global.gEndTripProcessing){
					Utils.leaveLog(mContext, "MainThread", null, "Trying Utils.uploadData for gUploadCustNoList");
					Global.gUploadProcess = true;
					info = Global.gUploadCustNoList.get(0);
					Utils.uploadData(mContext, info.makeZipFileInfo.nCustNo, info, true);
					Global.gUploadProcess = false;
					info = null;
				}
				Global.gUploadProcess = false;
				Thread.sleep(THREAD_SLEEP_TIME);
			}catch(Exception e)
			{
				e.printStackTrace();
				if(info != null)
				{
					// In this case, we have to leave log.
					Utils.errorReport("MainThread => 185", e.toString() + info.makeZipFileInfo.toJSON());
					Utils.unregisterUploadFile(mContext, info);
					Global.gUploadProcess = false;
				}
			}
		}


	}
}