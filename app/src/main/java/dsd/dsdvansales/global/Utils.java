package dsd.dsdvansales.global;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Color;
import android.graphics.Matrix;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import android.util.DisplayMetrics;
import android.util.Log;

import android.view.Display;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.eway.payment.sdk.android.RapidAPI;
import com.eway.payment.sdk.android.RapidConfigurationException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.BaseFont;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;

import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatMessage;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.TlsVersion;
import com.stripe.model.issuing.Cardholder;
import com.stripe.param.SourceSourceTransactionsParams;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.text.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.zip.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import dsd.dsdvansales.BuildConfig;
import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.api.ApiClient;
import dsd.dsdvansales.api.DirectionApi;
import dsd.dsdvansales.api.IPApiClient;
import dsd.dsdvansales.api.RouteApi;
import dsd.dsdvansales.api.UploadApiClient;
import dsd.dsdvansales.chat.ChatHelper;
import dsd.dsdvansales.chat.DialogsManager;
import dsd.dsdvansales.chat.QbChatDialogMessageListenerImp;
import dsd.dsdvansales.chat.QbDialogHolder;
import dsd.dsdvansales.db.BackupDbAdapter;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.db.DownloadDbAdapter;
import dsd.dsdvansales.db.EndTripDbAdapter;
import dsd.dsdvansales.db.GPSRecordDbAdapter;
import dsd.dsdvansales.db.GPSUploadDbAdapter;
import dsd.dsdvansales.db.LoginDbAdapter;
import dsd.dsdvansales.db.PrepareUploadDbAdapter;
import dsd.dsdvansales.db.UploadDbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.fragment.MakeDeliveryFragment;
import dsd.dsdvansales.listeners.GPSLocationListener;
import dsd.dsdvansales.listeners.NetworkLocationListener;
import dsd.dsdvansales.model.*;
import dsd.dsdvansales.parsers.ParseXMLFile;
import dsd.dsdvansales.services.MainService;
import dsd.dsdvansales.services.Tls12SocketFactory;
import dsd.dsdvansales.services.WakeupReceiver;
import dsd.dsdvansales.thread.MainThread;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.WIFI_SERVICE;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class Utils {

	public static String getExternalStorageRootPath() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
		{
			if (!Environment.isExternalStorageManager())
			{
				if(TextUtils.isEmpty(Global.APPLICATION_FOLDER_PATH))
				{
					return "/data/user/0/dsd.dsdvansales/files";
				}
				else{
					return Global.APPLICATION_FOLDER_PATH;
				}
			}
			else
			{
				return Environment.getExternalStorageDirectory().getPath();
			}
		}
		else {
			return Environment.getExternalStorageDirectory().getPath();
		}
	}
	
	public static void setupExternalEnvironmentVariables(){
		Global.ROOT_LOCATION					= getExternalStorageRootPath() + "/.DSDVanSales/";
		Global.ROOT_ROUTECTL_PATH				= getExternalStorageRootPath() + "/.DSDVanSales/ROUTECTL.xml";
		Global.ROOT_DATABASE_LOCATION			= getExternalStorageRootPath() + "/.DSDVanSales/mydata.db";
		Global.ROOT_UPLOAD_DATABASE_LOCATION			= getExternalStorageRootPath() + "/.DSDVanSales/upload.db";
		Global.ROOT_DOWNLOAD_DATABASE_LOCATION			= getExternalStorageRootPath() + "/.DSDVanSales/download.db";
		Global.ROOT_BACKUP_DATABASE_LOCATION			= getExternalStorageRootPath() + "/.DSDVanSalesBackup/backup.db";
		Global.ROOT_GPS_UPLOAD_DATABASE_LOCATION		= getExternalStorageRootPath() + "/.DSDVanSales/gps_upload_data.db";
		Global.ROOT_PREPARE_UPDATE_DATABASE_LOCATION	= getExternalStorageRootPath() + "/.DSDVanSales/prepare_upload_data.db";
		Global.ROOT_GPS_PREPARE_UPLOAD_DATABASE_LOCATION	= getExternalStorageRootPath() + "/.DSDVanSales/gps_prepare_upload_data.db";
		Global.ROOT_GPS_RECORD_DATABASE_LOCATION		= getExternalStorageRootPath() + "/.DSDVanSales/gps_record_data.db";
		Global.ROOT_TRIP_STATUS_DATABASE_LOCATION			= getExternalStorageRootPath() + "/.DSDVanSales/trip_status.db";
		Global.ROOT_LOGIN_LOG_DATABASE_LOCATION			= getExternalStorageRootPath() + "/.DSDVanSalesBackup/login_status.db";
		Global.ROUTEFIC_LOG_FILE_PATH			= getExternalStorageRootPath() + "/.DSDVanSales/routific_log.txt";

		Global.INSTALL_PATH					= getExternalStorageRootPath() + "/.DSDInstaller/";

		Global.ROOT_PRINT_TEMPLATE_LOCCATION	= Global.ROOT_LOCATION;
		Global.DOWNLOAD_DIR					= Global.ROOT_LOCATION + "download/";
		Global.PRODUCT_DOWNLOAD_DIR			= Global.ROOT_LOCATION + "product/";
		Global.ERROR_LOG_DIR                   = Global.ROOT_LOCATION + "error_log.txt";
		Global.UPLOAD_DIR						= getExternalStorageRootPath() + "/.DSDVanSalesBackup/";
		Global.PHOTO_DIR						= getExternalStorageRootPath() + "/.DSDVanSalesBackup/";
		Global.LOGO_DIR						= Global.ROOT_LOCATION + "logo/";
		Global.PDF_DIR							= Utils.getExternalStorageRootPath() + "/.DSDVanSalesBackup/";
		Global.PDF_LOCAL_DIR					= Global.ROOT_LOCATION + "pdf_local/";
		Global.CUSTOMER_PHOTO_DIR				= Global.ROOT_LOCATION + "customer_photos/";
		Global.ROOT_EQUIPMENT_CATALOG_LOCATION = Global.ROOT_LOCATION + "equipment";
		Global.DEFAULT_SIGNATURE_FILEPATH		= Global.ROOT_LOCATION + "signature.jpg";
	}

	public static void printDebug(Context mContext, String msg, Object... args) {
		String strDebug;
		try {
			strDebug = String.format(Locale.ENGLISH, msg, args);
		} catch (Exception e) {
			strDebug = msg;
		}
		if (Global._DEBUG_ == true) {
			Log.d(Global.TAG_NAME, strDebug);
		}
		if (mContext != null) {
			try {
				Toast.makeText(mContext, strDebug, Toast.LENGTH_LONG).show();
			}catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}
	}

	public static boolean isConnectingToInternet(Context mContext) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo[] info = connectivity.getAllNetworkInfo();
				if (info != null) {
					for (int i = 0; i < info.length; i++) {
						if (info[i].getState() == NetworkInfo.State.CONNECTED) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	public static ComponentName isServiceExisted(Context mContext, String className) {
		ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);

		if (serviceList.size() <= 0) {
			return null;
		}
		for (int i = 0; i < serviceList.size(); i++) {
			ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
			ComponentName serviceName = serviceInfo.service;
			if (serviceName.getClassName().equals(className) == true) {
				return serviceName;
			}
		}
		return null;
	}

	public static void removeAllPreferneces(Context context) {
		SharedPreferences pref = null;
		try {
			pref = context.getSharedPreferences(context.getPackageName(), Activity.MODE_WORLD_READABLE);
		} catch (Exception e) {
			pref = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);
		}
		try {
			pref.edit().clear().commit();
			pref.edit().clear().apply();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static boolean getBooleanSetting(Context mContext, String key) {
		SharedPreferences pref = null;
		try {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
		} catch (Exception e) {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
		}
		return pref.getBoolean(key, false);
	}

	public static void setBooleanSetting(Context mContext, String key, boolean val) {
		SharedPreferences pref = null;
		try {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
		} catch (Exception e) {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
		}
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(key, val);
		editor.commit();
	}

	public static int getIntSetting(Context mContext, String key) {
		SharedPreferences pref = null;
		try {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
		} catch (Exception e) {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
		}
		return pref.getInt(key, 0);
	}

	public static void setIntSetting(Context mContext, String key, int val) {
		SharedPreferences pref = null;
		try {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
		} catch (Exception e) {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
		}
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(key, val);
		editor.commit();
	}

	public static void setDoubleSetting(Context mContext, String key, double val) {
		SharedPreferences pref = null;
		try {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
		} catch (Exception e) {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
		}
		SharedPreferences.Editor editor = pref.edit();
		editor.putLong(key, Double.doubleToRawLongBits(val));
		editor.commit();
	}

	public static Double getDoubleSetting(Context mContext, String key) {
		SharedPreferences pref = null;
		try {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
		} catch (Exception e) {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
		}
		double nResult = Double.longBitsToDouble(pref.getLong(key, Global.DEFAULT_LONG));
		return Double.isNaN(nResult) == true ? 0 : nResult;
	}

	public static long getLongSetting(Context mContext, String key) {
		SharedPreferences pref = null;
		try {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
		} catch (Exception e) {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
		}
		return pref.getLong(key, 0);
	}

	public static void setLongSetting(Context mContext, String key, long val) {
		SharedPreferences pref = null;
		try {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
		} catch (Exception e) {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
		}
		SharedPreferences.Editor editor = pref.edit();
		editor.putLong(key, val);
		editor.commit();
	}

	public static String getStringSetting(Context mContext, String key) {
		SharedPreferences pref = null;
		try {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
		} catch (Exception e) {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
		}
		return pref.getString(key, "");
	}

	public static void setStringSetting(Context mContext, String key, String val) {
		SharedPreferences pref = null;
		try {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
		} catch (Exception e) {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
		}
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(key, val);
		editor.commit();

	}

	public static void setFloatSetting(Context mContext, String key, float val) {
		SharedPreferences pref = null;
		try {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
		} catch (Exception e) {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
		}
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(key, Float.floatToIntBits(val));
		editor.commit();
	}

	public static Float getFloatSetting(Context mContext, String key) {
		SharedPreferences pref = null;
		try {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
		} catch (Exception e) {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
		}
		float nResult = Float.intBitsToFloat(pref.getInt(key, Global.DEFAULT_LONG));
		return Float.isNaN(nResult) == true ? Global.DEFAULT_FLOAT : nResult;
	}

	public static void clearSettings(Context mContext)
	{
		int nSomaLeftMargin = Utils.getIntSetting(mContext, Global.PREF_SOMA_LEFT_MARGIN);
		int nSomaTopMargin = Utils.getIntSetting(mContext, Global.PREF_SOMA_TOP_MARGIN);

		SharedPreferences pref = null;
		try {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_WORLD_READABLE);
		} catch (Exception e) {
			pref = mContext.getSharedPreferences(mContext.getPackageName(), Activity.MODE_PRIVATE);
		}
		SharedPreferences.Editor editor = pref.edit();
		editor.clear();
		editor.commit();

		Utils.setIntSetting(mContext, Global.PREF_SOMA_LEFT_MARGIN, nSomaLeftMargin);
		Utils.setIntSetting(mContext, Global.PREF_SOMA_TOP_MARGIN, nSomaTopMargin);

	}

	public static int getIntFromString(String str)
	{
		if(TextUtils.isEmpty(str)) return 0;
		int result = 0;
		try
		{
			result = Integer.valueOf(str);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static int parseIntFromString(String strVal) {
		int nResult = 0;
		if (strVal.length() > 0) {
			String pattern = "-?\\d+";
			if (strVal.matches(pattern)) {
				try {
					nResult = Integer.parseInt(strVal);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return nResult;
	}

	public static long parseLongFromString(String strVal) {
		long nResult = 0;
		if (strVal.length() > 0) {
			String pattern = "-?\\d+";
			if (strVal.matches(pattern)) {
				try {
					nResult = Long.parseLong(strVal);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}


		return nResult;
	}



	public static float parseFloatFromString(String str) {
		float result = 0;
		if (str.length() > 0) {
			if (isValidNumberFormat(str)) {
				try {
					result = Float.parseFloat(str.replace(",", "."));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	public static boolean isValidNumberFormat(String str) {
		final String Digits = "(\\p{Digit}+)";
		final String HexDigits = "(\\p{XDigit}+)";
// an exponent is 'e' or 'E' followed by an optionally
// signed decimal integer.
		final String Exp = "[eE][+-]?" + Digits;
		final String fpRegex =
				("[\\x00-\\x20]*" + // Optional leading "whitespace"
						"[+-]?(" +         // Optional sign character
						"NaN|" +           // "NaN" string
						"Infinity|" +      // "Infinity" string

						// A decimal floating-point string representing a finite positive
						// number without a leading sign has at most five basic pieces:
						// Digits . Digits ExponentPart FloatTypeSuffix
						//
						// Since this method allows integer-only strings as input
						// in addition to strings of floating-point literals, the
						// two sub-patterns below are simplifications of the grammar
						// productions from the Java Language Specification, 2nd
						// edition, section 3.10.2.

						// Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
						"(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +

						// . Digits ExponentPart_opt FloatTypeSuffix_opt
						"(\\.(" + Digits + ")(" + Exp + ")?)|" +

						// Hexadecimal strings
						"((" +
						// 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
						"(0[xX]" + HexDigits + "(\\.)?)|" +

						// 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
						"(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

						")[pP][+-]?" + Digits + "))" +
						"[fFdD]?))" +
						"[\\x00-\\x20]*");// Optional trailing "whitespace"

		if (Pattern.matches(fpRegex, str)) {
			return true;
		} else {
			return false;
		}
	}

	public static double parseDoubleFromString(String str) {
		double result = 0;
		if (str.length() > 0) {

			if (isValidNumberFormat(str)) {
				try {
					result = Double.parseDouble(str);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public static double getDividedDoubleValue(String strVal) {
		double dValue = parseDoubleFromString(strVal);
		return dValue / Global.MULTIPLE_VALUE;
	}

	public static int getDividedValue(String strVal) {
		int nVal = parseIntFromString(strVal);
		return nVal / Global.MULTIPLE_VALUE;
	}

	public static float getDividedValue(int nValue) {
		return (float) (nValue) / (float) (Global.MULTIPLE_VALUE);
	}

	public static float getDividedValue(long nValue) {
		return (float) (nValue) / (float) (Global.MULTIPLE_VALUE);
	}

	public static String getMultipliedString(int nValue, Context context) {
		String strValue = String.format(Locale.ENGLISH, "%d", (long) (nValue * Global.MULTIPLE_VALUE));
		if (!strValue.equals("0")) {
			strValue += Utils.getScalingSuffix(context);
		}
		return strValue;
	}

	public static String getMultipliedString(float nValue, Context context) {
		String strValue = String.format(Locale.ENGLISH, "%.0f", nValue * Global.MULTIPLE_VALUE);

		if (!strValue.equals("0")) {
			strValue += Utils.getScalingSuffix(context);
		}
		return strValue;
	}

	public static String getMultipliedStringForTTX(float nValue, Context context) {
		int nScale = (int)Math.log10(Global.MULTIPLE_VALUE);

		String strValue = String.valueOf(nValue);
		if(strValue.contains("."))
		{
			int nDecimalPosition = strValue.indexOf(".");
			int nDecimalCnt = strValue.substring(nDecimalPosition + 1).length();
			if(nDecimalCnt > nScale)
			{
				strValue = strValue.substring(0, nDecimalPosition) + strValue.substring(nDecimalPosition + 1, nDecimalPosition + nScale + 1);
			}
			else
			{
				strValue = strValue.substring(0, nDecimalPosition) + strValue.substring(nDecimalPosition + 1);
				for(int i = nDecimalCnt; i < nScale; i++)
				{
					strValue += "0";
				}
			}
		}
		else
		{
			for(int i = 0; i < nScale; i++)
			{
				strValue += "0";
			}
		}
		//remove first 0 from the string.
		while(strValue.startsWith("0") && strValue.length() > 1)
		{
			strValue = strValue.substring(1);
		}
		if (!strValue.equals("0")) {
			strValue += Utils.getScalingSuffix(context);
		}
		return strValue;
	}

	public static String getMultipliedString(double nValue, Context context) {
		String strValue = String.format(Locale.ENGLISH, "%.0f", nValue * Global.MULTIPLE_VALUE);
		if (!strValue.equals("0")) {
			strValue += Utils.getScalingSuffix(context);
		}
		return strValue;
	}

	public static float myDivider(float nVal1, float nVal2) {
		float nResult = 0;
		if (nVal2 != 0) {
			nResult = nVal1 / nVal2;
		}
		return nResult;
	}

	public static SpannableString setKeyword(String strOrig, String strKeyword) {
		SpannableString spanString = new SpannableString(strOrig);
		if (strKeyword.length() > 0) {
			int nStart = strOrig.toLowerCase().indexOf(strKeyword.toLowerCase());
			if (nStart != -1) {
				spanString.setSpan(new ForegroundColorSpan(Color.RED), nStart, nStart + strKeyword.length(), 0);
			}
		}
		return spanString;
	}

	public static String getFileExtension(String strFilePath) {
		String strExtension = "";
		String[] strArrays = strFilePath.split("\\.");
		if (strArrays.length > 1) {
			strExtension = strArrays[strArrays.length - 1];
		}
		return strExtension;
	}

	public static boolean isFileExist(String filePath) {
		if(TextUtils.isEmpty(filePath))
		{
			return false;
		}

		File file = new File(filePath);
		boolean nRes = false;
		try {
			nRes = file.exists();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nRes;
	}

	public static boolean isXmlFileCorrect(String filePath){
		//try to parse xml file
		File xmlFile = new File(filePath);
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			FileInputStream fileInputStream = new FileInputStream(xmlFile);
			byte[] buf = new byte[(int)xmlFile.length()];
			fileInputStream.read(buf);
			fileInputStream.close();

			Document document = Utils.parseXML(builder, buf);
			if(document != null)
			{
				return true;
			}
			else
			{
				return false;
			}

		}catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static boolean createDirectory(String dirPath) {
		boolean bRes = false;
		File dir = new File(dirPath);
		if (dir.exists() == false) {
			try {
				bRes = dir.mkdirs();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			bRes = dir.canWrite();
		}
		return bRes;
	}

	public static void fileDelete(String filePath) {
		File file = new File(filePath);
		if (file.exists() == true) {
			file.delete();
		}
	}

	public static void deleteDirectory(String path) {
		File dir = new File(path);
		if (dir.exists() == true) {
			File[] files = dir.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory() == true) {
						deleteDirectory(files[i].getAbsolutePath());
					} else {
						files[i].delete();
					}
				}
			}
		}
		dir.delete();
	}

	@SuppressWarnings("resource")
	public static boolean fileCopy(String fromFilePath, String toFilePath) {
		File in = new File(fromFilePath);
		File out = new File(toFilePath);
		try {
			FileChannel inChannel = new FileInputStream(in).getChannel();
			FileChannel outChannel = new FileOutputStream(out).getChannel();

			inChannel.transferTo(0, inChannel.size(), outChannel);

			inChannel.close();
			outChannel.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static FTPClient connnectingwithFTP(String ip, int port, String userName, String pass) {
		boolean status;
		FTPClient ftpClient = null;
		try {
			ftpClient = new FTPClient();
			ftpClient.setConnectTimeout(5 * 1000);
			ftpClient.connect(InetAddress.getByName(ip), port);
			status = ftpClient.login(userName, pass);
			printDebug(null, "connnectingwithFTP - status = " + status);

			if (status == true) {
				if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
					ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
					ftpClient.enterLocalPassiveMode();
				}
				return ftpClient;
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e){
		    e.printStackTrace();
        }

		try {
			ftpClient.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean checkDirectoryExists(Context context, String filePath) {
		String strFTPServerIP = Utils.getStringSetting(context, Global.PREF_FTP_SERVER_IP);
		String strFTPUserName = Utils.getStringSetting(context, Global.PREF_FTP_USERNAME);
		String strFTPPassword = Utils.getStringSetting(context, Global.PREF_FTP_PASSWORD);
		FTPClient ftpClient = connnectingwithFTP(strFTPServerIP, Global.DEFAULT_FTP_PORT, strFTPUserName, strFTPPassword);
		File file = new File(filePath);
		String dirPath = file.getParent();
		try {
			ftpClient.changeWorkingDirectory(dirPath);
			ftpClient.disconnect();
			int returnCode = ftpClient.getReplyCode();
			if (returnCode == 550) {
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static long findFileOnFTPServer(FTPClient ftpClient, String strParentDir, String strFileName) {
		long nSize = 0;
		try {
			FTPFile[] mFileArray = ftpClient.listFiles(strParentDir);
			for (FTPFile ftpFile : mFileArray) {
				printDebug(null, ftpFile.getName());
				if (ftpFile.isDirectory() == false) {
					if (ftpFile.getName().equalsIgnoreCase(strFileName) == true) {
						nSize = ftpFile.getSize();
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nSize;
	}

	public static boolean downloadFileFromFTPServer(FTPClient ftpClient, String strServerPath, String strLocalPath) {
		boolean bRes = false;
		OutputStream outputStream = null;
		try {
			fileDelete(strLocalPath);
			File file = new File(strLocalPath);
			outputStream = new BufferedOutputStream(new FileOutputStream(file));
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			bRes = ftpClient.retrieveFile(strServerPath, outputStream);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bRes;
	}

	public static boolean uploadFileToFTPServer(Context context, String strLocalFilePath, String strServerFilePath) {
		if (strLocalFilePath.length() == 0 || strServerFilePath.length() == 0 || isFileExist(strLocalFilePath) == false) {
			return false;
		}
		FTPClient ftpClient = null;
		try {
			String strFTPServerIP = Utils.getStringSetting(context, Global.PREF_FTP_SERVER_IP);
			String strFTPUserName = Utils.getStringSetting(context, Global.PREF_FTP_USERNAME);
			String strFTPPassword = Utils.getStringSetting(context, Global.PREF_FTP_PASSWORD);
			ftpClient = connnectingwithFTP(strFTPServerIP, Global.DEFAULT_FTP_PORT, strFTPUserName, strFTPPassword);
			FileInputStream srcFileStream = new FileInputStream(new File(strLocalFilePath));
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			boolean status = ftpClient.storeFile(strServerFilePath, srcFileStream);
			if (status == true) {
				/*File file = new File(strLocalFilePath);
				if (strLocalFilePath.startsWith(Global.ROOT_LOCATION)) {
					file.renameTo(new File(strLocalFilePath + "_"));
				}
				if (strLocalFilePath.endsWith(".pdf")) {
					file = new File(strLocalFilePath.substring(0, strLocalFilePath.length() - 4) + "_.pdf");
					file.delete();
				}*/
			}
			printDebug(null, "uploadFile - status = " + status);
			srcFileStream.close();
			ftpClient.disconnect();
			ftpClient = null;
			return status;
		} catch (Exception e) {
			if (ftpClient != null) {
				try {
					ftpClient.disconnect();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}
		return false;
	}

	public static void removeFileFromLocalStorage(String strLocalFilePath) {
		if (strLocalFilePath.endsWith(".pdf")) {
			String strTmpFilePath = strLocalFilePath.substring(0, strLocalFilePath.length() - 4) + "_.pdf";
			fileDelete(strTmpFilePath);
		}
		fileDelete(strLocalFilePath);
	}

	public static boolean bDownloadPhotoError = false;
	public static boolean bDownloadEquipmentCatalogError = false;


	public static boolean downloadEquipmentCatalogFromHttpServer(Context context, FTPClient ftpClient)
	{
		String strFTPServerIP = Utils.getStringSetting(context, Global.PREF_FTP_SERVER_IP);
		String strFTPUserName = Utils.getStringSetting(context, Global.PREF_FTP_USERNAME);
		String strCompanyName = Utils.getStringSetting(context, Global.PREF_FTP_COMPANY_NAME);
		String strDirectory = String.format(Locale.ENGLISH, Global.FTP_EQUIP_CATALOG_DIR, strCompanyName);

		try
		{
			final CountDownLatch latch = new CountDownLatch(1);
			final List<String> fileNameList = new ArrayList<>();
			bDownloadEquipmentCatalogError = false;
			UploadApiClient.getMainApiClient().getDirectoryFiles(strFTPUserName, strFTPServerIP, strCompanyName, strDirectory).enqueue(new Callback<ListDirectoryResponse>() {
				@Override
				public void onResponse(Call<ListDirectoryResponse> call, Response<ListDirectoryResponse> response) {
					if(response.isSuccessful() && response.body().result == 1)
					{
						fileNameList.addAll(response.body().files);
					}
					else {
						bDownloadEquipmentCatalogError = true;
					}
					latch.countDown();
				}

				@Override
				public void onFailure(Call<ListDirectoryResponse> call, Throwable t) {
					bDownloadEquipmentCatalogError = true;
					latch.countDown();
				}
			});

			latch.await();
			if(bDownloadEquipmentCatalogError)
				return false;
			boolean isFTPPossible = isPossibleDownloadFileViaFTP(context);
			for(String fileName : fileNameList)
			{
				String[] strArrays = fileName.split("\\.");
				String strOnlyName = "";
				for (int i = 0; i < strArrays.length - 1; i++) {
					strOnlyName += strArrays[i] + ".";
				}
				String destFilePath = String.format(Locale.ENGLISH, "%s/%sjpg", Global.ROOT_EQUIPMENT_CATALOG_LOCATION, strOnlyName);
				if(!downloadFile(context, strDirectory, fileName, destFilePath))
				{
					if(isFTPPossible && ftpClient != null) {
						doDownloadFileViaFTP(context, strDirectory, fileName, destFilePath, ftpClient);
					}
				} else {
					markDownloadSuccess(context, strDirectory, fileName);
				}
			}
			return true;
		}catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public static boolean downloadEquipmentCatalogFromFTPServer(FTPClient ftpClient, String strCompanyName) throws IOException {
		boolean bRes = true;
		String strDirectory = String.format(Locale.ENGLISH, Global.FTP_EQUIP_CATALOG_DIR, strCompanyName);
		FTPFile[] mFileArray = ftpClient.listFiles(strDirectory);
		for (FTPFile ftpFile : mFileArray) {
			printDebug(null, ftpFile.getName());
			if (ftpFile.isDirectory() == false) {
				String strFileName = ftpFile.getName();
				String[] strArrays = strFileName.split("\\.");
				String strOnlyName = "";
				for (int i = 0; i < strArrays.length - 1; i++) {
					strOnlyName += strArrays[i] + ".";
				}
				String destFilePath = String.format(Locale.ENGLISH, "%s/%sjpg", Global.ROOT_EQUIPMENT_CATALOG_LOCATION, strOnlyName);
				long nRemoteSize = ftpFile.getSize();
				long nLocalSize = 0;
				if (isFileExist(destFilePath) == true) {
					nLocalSize = new File(destFilePath).length();
				}
				if (nRemoteSize > 0 && nLocalSize > 0 && nRemoteSize == nLocalSize) {
					continue;
				}
				String remoteFileName = String.format(Locale.ENGLISH, Global.FTP_EQUIP_CATALOG_DIR, strCompanyName, strFileName);
				OutputStream outputStream = null;
				try {
					File downloadFile = new File(destFilePath);
					outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
					ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
					ftpClient.retrieveFile(remoteFileName, outputStream);
				} catch (Exception ex) {
					ex.printStackTrace();
					bRes = false;
				} finally {
					if (outputStream != null) {
						try {
							outputStream.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return bRes;
	}

	public static boolean downloadCustomerPhotoFromHttpServer(Context context, FTPClient ftpClient)
	{
		String strFTPServerIP = Utils.getStringSetting(context, Global.PREF_FTP_SERVER_IP);
		String strFTPUserName = Utils.getStringSetting(context, Global.PREF_FTP_USERNAME);
		String strCompanyName = Utils.getStringSetting(context, Global.PREF_FTP_COMPANY_NAME);
		String strDirectory = String.format(Locale.ENGLISH, Global.FTP_CUSTOMER_PHOTO_DIR, strCompanyName);

		try
		{
			final CountDownLatch latch = new CountDownLatch(1);
			final List<String> fileNameList = new ArrayList<>();
			bDownloadPhotoError = false;
			UploadApiClient.getMainApiClient().getDirectoryFiles(strFTPUserName, strFTPServerIP, strCompanyName, strDirectory).enqueue(new Callback<ListDirectoryResponse>() {
				@Override
				public void onResponse(Call<ListDirectoryResponse> call, Response<ListDirectoryResponse> response) {
					if(response.isSuccessful() && response.body().result == 1)
					{
						fileNameList.addAll(response.body().files);
					}
					else {
						bDownloadPhotoError = true;
					}
					latch.countDown();
				}

				@Override
				public void onFailure(Call<ListDirectoryResponse> call, Throwable t) {
					bDownloadPhotoError = true;
					latch.countDown();
				}
			});

			latch.await();
			if(bDownloadPhotoError)
				return false;
			boolean isFTPPossible = isPossibleDownloadFileViaFTP(context);
			for(String fileName : fileNameList)
			{
				String[] strArrays = fileName.split("\\.");
				String strOnlyName = "";
				for (int i = 0; i < strArrays.length - 1; i++) {
					strOnlyName += strArrays[i] + ".";
				}
				String destFilePath = String.format(Locale.ENGLISH, "%s%sjpg", Global.CUSTOMER_PHOTO_DIR, strOnlyName);
				if(!downloadFile(context, strDirectory, fileName, destFilePath))
				{
					if(isFTPPossible && ftpClient != null) {
						doDownloadFileViaFTP(context, strDirectory, fileName, destFilePath, ftpClient);
					}
				} else {
					markDownloadSuccess(context, strDirectory, fileName);
				}
			}
			return true;
		}catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public static boolean downloadCustomerPhotoFromFTPServer(FTPClient ftpClient, String strCompanyName) throws IOException {
		boolean bRes = true;
		String strDirectory = String.format(Locale.ENGLISH, Global.FTP_CUSTOMER_PHOTO_DIR, strCompanyName);
		FTPFile[] mFileArray = ftpClient.listFiles(strDirectory);
		for (FTPFile ftpFile : mFileArray) {
			printDebug(null, ftpFile.getName());
			if (ftpFile.isDirectory() == false) {
				String strFileName = ftpFile.getName();
				String[] strArrays = strFileName.split("\\.");
				String strOnlyName = "";
				for (int i = 0; i < strArrays.length - 1; i++) {
					strOnlyName += strArrays[i] + ".";
				}
				String destFilePath = String.format(Locale.ENGLISH, "%s%sjpg", Global.CUSTOMER_PHOTO_DIR, strOnlyName);
				long nRemoteSize = ftpFile.getSize();
				long nLocalSize = 0;
				if (isFileExist(destFilePath) == true) {
					nLocalSize = new File(destFilePath).length();
				}
				if (nRemoteSize > 0 && nLocalSize > 0 && nRemoteSize == nLocalSize) {
					continue;
				}
				String remoteFileName = String.format(Locale.ENGLISH, Global.FTP_CUSTOMER_PHOTO_PATH, strCompanyName, strFileName);
				OutputStream outputStream = null;
				try {
					File downloadFile = new File(destFilePath);
					outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
					ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
					ftpClient.retrieveFile(remoteFileName, outputStream);
				} catch (Exception ex) {
					ex.printStackTrace();
					bRes = false;
				} finally {
					if (outputStream != null) {
						try {
							outputStream.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return bRes;
	}

	public static boolean unzipData() {
		boolean bRes = false;
		try {
			String strZipPath = String.format(Locale.ENGLISH, "%s%s.zip", Global.DOWNLOAD_DIR, Global.gTripNumber);
			String strExtractDir = String.format(Locale.ENGLISH, "%s/%s/", Global.DOWNLOAD_DIR, Global.gTripNumber);

			deleteDirectory(strExtractDir);
			createDirectory(strExtractDir);

			InputStream is = new FileInputStream(strZipPath);
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
			ZipEntry ze;
			String strFileName;
			byte[] buffer = new byte[1024];
			int count;
			while ((ze = zis.getNextEntry()) != null) {
				strFileName = ze.getName();
				if (ze.isDirectory()) {
					File fmd = new File(strExtractDir + strFileName);
					fmd.mkdirs();
					continue;
				}
				FileOutputStream fout = new FileOutputStream(strExtractDir + strFileName);
				while ((count = zis.read(buffer)) != -1) {
					fout.write(buffer, 0, count);
				}
				fout.close();
				zis.closeEntry();
			}
			zis.close();
			bRes = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bRes;
	}

	public static String getFullAddress(Context context, String strAddress){
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        double nMyLat = Utils.getDoubleSetting(context, Global.PREF_MY_LATITUDE);
        double nMyLng = Utils.getDoubleSetting(context, Global.PREF_MY_LONGITUDE);
        String country = "";
        if(nMyLat == 0 && nMyLng == 0)
        {
            country = context.getResources().getConfiguration().locale.getDisplayCountry();
        }
        else
        {
            try {
                address = coder.getFromLocation(nMyLat, nMyLng, 5);
                if(address == null)
                {
                    country = context.getResources().getConfiguration().locale.getDisplayCountry();
                }
                else
                {
                    country = address.get(0).getCountryName();
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        {
            country = "Australia";
        }

        try {
            address = coder.getFromLocationName(strAddress + ", " + country,5);
            if (address==null) {
                return null;
            }
            Address location = address.get(0);

            String strRealAddress = location.getAddressLine(0);
            int nAddressLine = location.getMaxAddressLineIndex();
            for(int i = 1; i < nAddressLine; i++)
            {
                strRealAddress += ", " + location.getAddressLine(i);
            }

            return strRealAddress;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static LatLng getLocationFromAddress(Context context, String strAddress){

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        double nMyLat = Utils.getDoubleSetting(context, Global.PREF_MY_LATITUDE);
        double nMyLng = Utils.getDoubleSetting(context, Global.PREF_MY_LONGITUDE);
        String country = "";
        if(nMyLat == 0 && nMyLng == 0)
        {
            country = context.getResources().getConfiguration().locale.getDisplayCountry();
        }
        else
        {
            try {
                address = coder.getFromLocation(nMyLat, nMyLng, 5);
                if(address == null)
                {
                    country = context.getResources().getConfiguration().locale.getDisplayCountry();
                }
                else
                {
                    country = address.get(0).getCountryName();
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        /*{
            country = "Australia";
        }*/

        try {
            address = coder.getFromLocationName(strAddress + ", " + country,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);

            p1 = new LatLng(location.getLatitude(), location.getLongitude());

            return p1;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

	public static boolean uploadData(final Context mContext, int nCustNo, UploadInfo fileInfo, final boolean bNeedDownload) {
		boolean bRes = false;

		MakeZipFileInfo zipFileInfo = (fileInfo != null) ? fileInfo.makeZipFileInfo : null;

		final String strFTPServerIP = Utils.getStringSetting(mContext, Global.PREF_FTP_SERVER_IP);
		final String strFTPCompanyName = Utils.getStringSetting(mContext, Global.PREF_FTP_COMPANY_NAME);
		final String strFTPUserName = Utils.getStringSetting(mContext, Global.PREF_FTP_USERNAME);
		final String strFTPPassword = Utils.getStringSetting(mContext, Global.PREF_FTP_PASSWORD);
		FTPClient ftpClient = null;
		List<UGPSInfo> uGPSList;
		List<UTransactionsInfo> uTransactionsList;
		List<UFileTransactionInfo> uFileTransactionInfoList;
		List<UCamerasInfo> uCamerasList;
		List<UMessagesInfo> uMessagesList;
		List<UOrderHInfo> uOrderHList;
		List<UInvenHInfo> uInvenHList;
		List<UARTotalInfo> uARTotalList;
		List<USurveysInfo> uSurveysList;
		List<UCustomerFMInfo> uCustomerFMList = null;
		List<UServicesInfo> uServicesList = new ArrayList<>();
		List<UCollectionsDInfo> uCollectionsDList = new ArrayList<>();
		List<UStockHInfo> uStockHInfos;
		Map<Long, Map<String, List<BatchLot>>> uStockBatchLotMap = new HashMap<>();

		List<UInvenBlotInfo> uInvenBlotInfoList = new ArrayList<>();

		List<UCustomerNoteInfo> uCustomerNoteInfoList = new ArrayList<>();
		List<UVisitInfo> uVisitInfoList = new ArrayList<>();
		List<UStockHInfo> uConsignmentInfoList = new ArrayList<>();
		try {
			/*if(!isConnectingToInternet(mContext))
			{
				return false;
			}*/

			DbAdapter dbAdapter = new DbAdapter(mContext);
			if (zipFileInfo != null) {
				dbAdapter.open();
				List<Long> loginInfos = dbAdapter.getLoginInfoList();
				for (Long value : loginInfos) {
					boolean bAdd = true;
					for (Long trxn : zipFileInfo.trxnList) {
						if (trxn.longValue() == value.longValue()) {
							bAdd = false;
							break;
						}
					}
					if (bAdd) {
						zipFileInfo.trxnList.add(value);
					}
				}
				dbAdapter.close();
			}

			dbAdapter.open();
			if (zipFileInfo != null) {
				uGPSList = dbAdapter.getUGPSList(zipFileInfo);
				uTransactionsList = dbAdapter.getUTransactionsList(zipFileInfo);
				uFileTransactionInfoList = dbAdapter.getUFileTransactionsList(nCustNo, zipFileInfo);
				uCamerasList = dbAdapter.getUCameraList(nCustNo, zipFileInfo);
				uMessagesList = dbAdapter.getUMessagesList(nCustNo, zipFileInfo);
				uOrderHList = dbAdapter.getUOrderHList(nCustNo, zipFileInfo);
				uSurveysList = dbAdapter.getUSurveysList(nCustNo, zipFileInfo);
				uStockHInfos = dbAdapter.getUStockHInfoList(zipFileInfo);
				uInvenHList = dbAdapter.getUInvenHList(nCustNo, zipFileInfo);
				uConsignmentInfoList = dbAdapter.getUConsignmentInfoList(zipFileInfo);
				uInvenBlotInfoList = dbAdapter.getULoadBlot(zipFileInfo);
				for(UStockHInfo uStockHInfo : uConsignmentInfoList)
				{
					uStockHInfo.uStockDInfoList = dbAdapter.getUStockDInfoList(uStockHInfo.nTrxnNo);
					if(uStockBatchLotMap.get(uStockHInfo.nTrxnNo) == null)
					{
						uStockBatchLotMap.put(uStockHInfo.nTrxnNo, dbAdapter.getBatchLotList(uStockHInfo.nTrxnNo));
					}
				}

				for(UStockHInfo uStockHInfo : uStockHInfos)
				{
					if(uStockBatchLotMap.get(uStockHInfo.nTrxnNo) == null)
					{
						uStockBatchLotMap.put(uStockHInfo.nTrxnNo, dbAdapter.getBatchLotList(uStockHInfo.nTrxnNo));
					}
				}

				for (Long trxnNo : zipFileInfo.trxnList) {
					List<UCustomerNoteInfo> noteInfoList = dbAdapter.getUCustomerInfoList(trxnNo);
					if (noteInfoList.size() > 0) {
						noteInfoList.get(0).uAttachmentInfoList = dbAdapter.getUAttachmentInfo(trxnNo);
						uCustomerNoteInfoList.addAll(noteInfoList);
					}
					uVisitInfoList.addAll(dbAdapter.getUVisitInfo(trxnNo));
				}
			}
			else {
				uGPSList = dbAdapter.getUGPSList(nCustNo);
				uTransactionsList = dbAdapter.getUTransactionsList(nCustNo);
				uFileTransactionInfoList = dbAdapter.getUFileTransactionsList(nCustNo);
				uCamerasList = dbAdapter.getUCameraList(nCustNo);
				uMessagesList = dbAdapter.getUMessagesList(nCustNo);
				uOrderHList = dbAdapter.getUOrderHList(nCustNo);
				uSurveysList = dbAdapter.getUSurveysList(nCustNo);
				uStockHInfos = dbAdapter.getUStockHInfoList();
				uInvenHList = dbAdapter.getUInvenHList(nCustNo);
				uInvenBlotInfoList = dbAdapter.getULoadBlot();
				uVisitInfoList.addAll(dbAdapter.getAllUVisitInfo());
				//Register Inventory for UStocks.
				for(UStockHInfo uStockHInfo : uStockHInfos)
				{
					UInvenHInfo uInvenHInfo = dbAdapter.getUInvenHInfo(uStockHInfo.nTrxnNo);
					if(uInvenHInfo != null)
					{
						uInvenHList.add(uInvenHInfo);
					}

					if(uStockBatchLotMap.get(uStockHInfo.nTrxnNo) == null)
					{
						uStockBatchLotMap.put(uStockHInfo.nTrxnNo, dbAdapter.getBatchLotList(uStockHInfo.nTrxnNo));
					}
				}
			}
			if (Global.gIsEndTrip == true) {
				uServicesList = dbAdapter.getUServicesList(true);
				for (UServicesInfo uServicesInfo : uServicesList) {
					//Make Transaction File
					UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
					uTransactionInfo.nTrxnNo = uServicesInfo.nTrxnNo;
					uTransactionInfo.strReference = uServicesInfo.strReason;
					uTransactionInfo.strDocType = "SERV";
					uTransactionInfo.strTrxnDate = uServicesInfo.strTrxnDate;
					uTransactionInfo.strTrxnTime = uServicesInfo.strTrxnTime;
					uTransactionInfo.nChainNo = uServicesInfo.nChainNo;
					uTransactionInfo.nCustNo = uServicesInfo.nCustNo;
					uTransactionsList.add(uTransactionInfo);
				}
				//uCollectionsDList = dbAdapter.getUCollectionsDList();
			} else if (zipFileInfo != null) {
				uServicesList = dbAdapter.getUServicesList(zipFileInfo);
			}

			List<UOrderStatusHInfo> uOrderStatusInfoList = new ArrayList<>();
			if (zipFileInfo != null) {
				for (long nTrxnNo : zipFileInfo.trxnList) {
					UOrderStatusHInfo uStatusInfo = dbAdapter.getUOrderStatusHInfo(nTrxnNo);
					if (uStatusInfo != null) {
						uOrderStatusInfoList.add(uStatusInfo);
					}
				}
			}

			for (int i = 0; i < uOrderHList.size(); i++) {
				uOrderHList.get(i).uOrderDList = dbAdapter.getUOrderDList(uOrderHList.get(i).nTrxnNo);
			}

			for (int i = 0; i < uInvenHList.size(); i++) {
				uInvenHList.get(i).uInvenDList = dbAdapter.getUInvenDList(uInvenHList.get(i).nTrxnNo);
			}

			if (nCustNo != 0 || Global.gIsEndTrip == true) {
				if (zipFileInfo != null) {
					uARTotalList = dbAdapter.getUARTotalList(nCustNo, zipFileInfo);
				} else {
					uARTotalList = dbAdapter.getUARTotalList(nCustNo);
				}

			} else
				uARTotalList = new ArrayList<>();

			for (int i = 0; i < uARTotalList.size(); i++) {
				//uARTotalList.get(i).paymentList = dbAdapter.getUARPaymentList(uARTotalList.get(i).nTrxnNo);
				uARTotalList.get(i).paymentList = dbAdapter.getUARPaymentList(uARTotalList.get(i).strDocNo);
			}

			for (int i = 0; i < uSurveysList.size(); i++) {
				uSurveysList.get(i).detailList = dbAdapter.getUSurveyDetailList(uSurveysList.get(i).nTrxnNo);
			}

			/*if (nCustNo != 0 || Global.gIsEndTrip == true)
				uCustomerFMList = dbAdapter.getUCustomerFMList(nCustNo);
			else
				uCustomerFMList = new ArrayList<>();*/
			if(Global.gIsEndTrip == true)
			{
				uCustomerFMList = dbAdapter.getUCustomerFMList(0);
			}
			else
			{
				uCustomerFMList = new ArrayList<>();
				for(Long trxnNo : zipFileInfo.trxnList)
				{
					uCustomerFMList.addAll(dbAdapter.getUCustomerFMList(nCustNo, trxnNo));
				}
			}

			//dbAdapter.getUCustomerFMList(nCustNo);


			Map<String, Integer> refMap = new HashMap<>();
			for (UStockHInfo info : uStockHInfos) {
				String key = info.strStockType + "__" + info.nTrxnNo;
				Integer nCount = refMap.get(key);
				if (nCount == null || nCount == 0) {
					nCount = 0;
					if (info.strStockType.equals("6")) {
						info.uStockDInfoList = dbAdapter.getUStockDInfoListForUnload(info.nTrxnNo);
					} else if (info.strStockType.equals("9")) {
						info.uStockDInfoList = dbAdapter.getUStockDInfoListForEOD(info.nTrxnNo);
					} else {
						info.uStockDInfoList = dbAdapter.getUStockDInfoList(info.nTrxnNo);
					}
				}
				nCount++;
				refMap.put(key, nCount);
			}

			List<UAssetInfo> uAssetInfoList = dbAdapter.getUAssetsInfo(nCustNo);
			List<URouteSchHeader> uRouteSchHeaderList = dbAdapter.getURouteSchHeader();
			for (URouteSchHeader headerInfo : uRouteSchHeaderList) {
				headerInfo.body = dbAdapter.getURouteSchBody(headerInfo.nTrxnNo);
			}

			Map<String, UBillingDetailsInfo> mapBillingDetails = new HashMap<>();
			Map<String, List<ContactDetail>> mapContactDetails = new HashMap<>();
			Map<String, List<ContactInfo>> mapCustomerDetails = new HashMap<>();
			if(uCustomerFMList != null && uCustomerFMList.size() > 0)
			{
				//Gather information for CustomerFM --- BilingDetails, contacts details, customer additional information
				for(UCustomerFMInfo info : uCustomerFMList)
				{
					String mapKey = info.nCustNo + "/" + info.nChainNo;
					UBillingDetailsInfo uBillingDetailsInfo = dbAdapter.getUBillingDetailsInfo(info.nCustNo, info.nChainNo);
					mapBillingDetails.put(mapKey, uBillingDetailsInfo);
					List<ContactDetail> contactDetails = dbAdapter.getContactDetailList(info.nCustNo, info.nChainNo);
					mapContactDetails.put(mapKey, contactDetails);
					List<ContactInfo> custAdditionalInfoList = dbAdapter.getCustAdditionalInfoList(info.nCustNo, info.nChainNo);
					mapCustomerDetails.put(mapKey,custAdditionalInfoList);
				}
			}

			dbAdapter.close();

			//Arrange TrxnTime

			String strTrxnTime = "";
			/*if(uGPSList != null)
			{
				for(UGPSInfo info : uGPSList)
				{
					if(TextUtils.isEmpty(strTrxnTime)) strTrxnTime = info.strTrxnTime;
					else info.strTrxnTime = strTrxnTime;
				}
			}

			if(uTransactionsList != null)
			{
				for(UTransactionsInfo info : uTransactionsList)
				{
					if(TextUtils.isEmpty(strTrxnTime)) strTrxnTime = info.strTrxnTime;
					else info.strTrxnTime = strTrxnTime;
				}
			}
			if(uFileTransactionInfoList != null)
			{
				for(UFileTransactionInfo info : uFileTransactionInfoList)
				{
					if(TextUtils.isEmpty(strTrxnTime)) strTrxnTime = info.strTrxnTime;
					else info.strTrxnTime = strTrxnTime;
				}
			}
			if(uOrderStatusInfoList != null)
			{
				for(UOrderStatusHInfo info : uOrderStatusInfoList)
				{
					if(TextUtils.isEmpty(strTrxnTime)) strTrxnTime = info.strTrxnTime;
					else info.strTrxnTime = strTrxnTime;
				}
			}
			if(uInvenHList != null)
			{
				for(UInvenHInfo info : uInvenHList)
				{
					if(TextUtils.isEmpty(strTrxnTime)) strTrxnTime = info.strTrxnTime;
					else info.strTrxnTime = strTrxnTime;
				}
			}
			if(uStockHInfos != null)
			{
				for(UStockHInfo info : uStockHInfos)
				{
					if(TextUtils.isEmpty(strTrxnTime)) strTrxnTime = info.strTrxnTime;
					else info.strTrxnTime = strTrxnTime;
				}
			}
			if(uCamerasList != null)
			{
				for(UCamerasInfo info : uCamerasList)
				{
					if(TextUtils.isEmpty(strTrxnTime)) strTrxnTime = info.strTrxnTime;
					else info.strTrxnTime = strTrxnTime;
				}
			}
			if(uMessagesList != null)
			{
				for(UMessagesInfo info : uMessagesList)
				{
					if(TextUtils.isEmpty(strTrxnTime)) strTrxnTime = info.strTrxnTime;
					else info.strTrxnTime = strTrxnTime;
				}
			}
			if(uSurveysList != null)
			{
				for(USurveysInfo info : uSurveysList)
				{
					if(TextUtils.isEmpty(strTrxnTime)) strTrxnTime = info.strTrxnTime;
					else info.strTrxnTime = strTrxnTime;
				}
			}
			if(uServicesList != null)
			{
				for(UServicesInfo info : uServicesList)
				{
					if(TextUtils.isEmpty(strTrxnTime)) strTrxnTime = info.strTrxnTime;
					else info.strTrxnTime = strTrxnTime;
				}
			}
			if(uRouteSchHeaderList != null)
			{
				for(URouteSchHeader info : uRouteSchHeaderList)
				{
					if(TextUtils.isEmpty(strTrxnTime)) strTrxnTime = info.strTrxnTime;
					else info.strTrxnTime = strTrxnTime;
				}
			}
			if(uCustomerFMList != null)
			{
				for(UCustomerFMInfo info : uCustomerFMList)
				{
					if(TextUtils.isEmpty(strTrxnTime)) strTrxnTime = info.strTrxnTime;
					else info.strTrxnTime = strTrxnTime;
				}
			}*/



			if (TextUtils.isEmpty(strTrxnTime)) {
				strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(new Date());
			}

			Date curDate = new Date();
			String strSuffix = new SimpleDateFormat("yyyyMMddHHmmss").format(curDate);

			String strDocNo = (Global.gUOrderHInfo != null) ? Global.gUOrderHInfo.strDocNo : "0";
			if (strDocNo.equals("0")) strDocNo = Global.gStrDocNo;

			String strUploadDir = Global.UPLOAD_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/";
			String strContents;

			if (Utils.createDirectory(strUploadDir) == false) {
				Utils.printDebug(null, "Can't create the upload directory on SD Card.");
				return false;
			}

			List<String> fileList = new ArrayList<>();
			// write to UGPS.UPL
			if (uGPSList.size() > 0) {
				String strGPSFileName = "GPS" + strSuffix + ".upl";
//				fileList.add(strGPSFileName);
				boolean bFirst = true;
				strContents = "<GPS>";
				for (UGPSInfo info : uGPSList) { 
					/*info.nTrxnNo = lTime;
					info.strTrxnDate = strTrxnDate;
					info.strTrxnTime = strTrxnTime;*/
					if (bFirst) {
						if (info.strTrxnDate.length() > 0 && info.strTrxnTime.length() > 0) {
							strGPSFileName = "GPS" + info.strTrxnDate + info.strTrxnTime + ".upl";
						}
						fileList.add(strGPSFileName);
						bFirst = false;
					}
					strContents += "<GPSLog>";
					strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strContents += "<ChainNo>" + info.nChainNo + "</ChainNo>";
					strContents += "<CustNo>" + info.nCustNo + "</CustNo>";
					strContents += "<StartDate>" + info.strTrxnDate + "</StartDate>";
					strContents += "<StartTime>" + info.strTrxnTime + "</StartTime>";
					strContents += "<EndDate>" + info.strTrxnDate + "</EndDate>";
					strContents += "<EndTime>" + info.strTrxnTime + "</EndTime>";
					if (info.nStartTrxnNo == 0) {
						strContents += "<StartTrxnNo>" + "" + "</StartTrxnNo>";
					} else {
						strContents += "<StartTrxnNo>" + info.nStartTrxnNo + "</StartTrxnNo>";
					}
					strContents += "<GPSData>" + info.strGPSData + "</GPSData>";
					strContents += "<DocNo>" + info.strDocNo + "</DocNo>";
					strContents += "<DocType>" + info.strDocType + "</DocType>";
					strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
					strContents += "<PrintedFlag>" + "0" + "</PrintedFlag>";
					strContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
					strContents += "<TrxnTime>" + info.strTrxnTime + "</TrxnTime>";
					strContents += "<Reference>" + "" + "</Reference>";
					strContents += "<TCOMStatus>" + "0" + "</TCOMStatus>";
					strContents += "<SaleDate>" + info.strTrxnDate + "</SaleDate>";
					strContents += "<Trip>" + Global.gTripNumber + "</Trip>";
					strContents += "</GPSLog>";
				}
				strContents += "</GPS>";
				writeStringAsFile(strContents, strUploadDir, strGPSFileName);
			}

			// write to Transactions.upl
			if (uTransactionsList.size() > 0) {
				String strTransactionsFileName = "Transactions" + strSuffix + ".upl";
//				fileList.add(strTransactionsFileName);
				boolean bFirst = true;
				strContents = "<Transactions>";
				for (UTransactionsInfo info : uTransactionsList) {
/*					info.nTrxnNo = lTime;
					info.strTrxnDate = strTrxnDate;
					info.strTrxnTime = strTrxnTime;*/
					if (bFirst) {
						if (info.strTrxnDate.length() > 0 && info.strTrxnTime.length() > 0) {
							strTransactionsFileName = "Transactions" + info.strTrxnDate + info.strTrxnTime + ".upl";
						}
						fileList.add(strTransactionsFileName);
						bFirst = false;
					}

					//This is for VS292
					String strDocType = info.strDocType;
					if(Utils.getStringSetting(mContext, Global.PREF_UNLESHED).equals("1") && info.strDocType.equals("INV"))
					{
						strDocType = "ORD";
					}

					strContents += "<Transaction>";
					strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strContents += "<DocNo>" + info.strDocNo + "</DocNo>";
					strContents += "<DocType>" + strDocType + "</DocType>";
					strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
					strContents += "<PrintedFlag>" + "0" + "</PrintedFlag>";
					strContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
					strContents += "<TrxnTime>" + info.strTrxnTime + "</TrxnTime>";
					strContents += "<Reference>" + info.strReference + "</Reference>";
					strContents += "<TCOMStatus>" + "0" + "</TCOMStatus>";
					strContents += "<ChainNo>" + info.nChainNo + "</ChainNo>";
					strContents += "<CustNo>" + info.nCustNo + "</CustNo>";
					strContents += "<SaleDate>" + info.strTrxnDate + "</SaleDate>";
					strContents += "<PhysAvail>" + "0" + "</PhysAvail>";
					strContents += "<VirtAvail>" + "0" + "</VirtAvail>";
					strContents += "<Trip>" + Global.gTripNumber + "</Trip>";
					strContents += "<TripId>" + info.strTripId + "</TripId>";
					strContents += "<DriverId>" + info.strDriverId + "</DriverId>";
					strContents += "<DriverNumber>" + info.strDriverNumber + "</DriverNumber>";
					strContents += "<DriverName>" + info.strDriverName + "</DriverName>";
					strContents += "</Transaction>";
				}
				strContents += "</Transactions>";
				writeStringAsFile(strContents, strUploadDir, strTransactionsFileName);
			}

			//Write to UInvenBlot.upl
			if(uInvenBlotInfoList.size() > 0){
				String strTransactionsFileName = "InvenBlot" + strSuffix + ".upl";
				boolean bFirst = true;
				strContents = "<Invenblots>";
				for (UInvenBlotInfo info : uInvenBlotInfoList) {
					if (bFirst) {
						if (info.strTrxnDate.length() > 0 && info.strTrxnTime.length() > 0) {
							strTransactionsFileName = "InvenBlot" + info.strTrxnDate + info.strTrxnTime + ".upl";
						}
						fileList.add(strTransactionsFileName);
						bFirst = false;
					}
					strContents += "<Invenblot>";
					strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strContents += "<DocNo>" + info.strDocNo + "</DocNo>";
					strContents += "<DocType>" + info.strDocType + "</DocType>";
					strContents += "<VoidFlag>" + info.nVoidFlag + "</VoidFlag>";
					strContents += "<PrintedFlag>" + info.nPrintedFlag + "</PrintedFlag>";

					strContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
					strContents += "<TrxnTime>" + info.strTrxnTime + "</TrxnTime>";

					strContents += "<Reference>" + info.strReference + "</Reference>";
					strContents += "<TCOMStatus>" + info.nTComStatus + "</TCOMStatus>";
					strContents += "<ChainNo>" + info.nChainNo + "</ChainNo>";
					strContents += "<CustNo>" + info.nCustNo + "</CustNo>";

					strContents += "<SaleDate>" + info.strSaleDate + "</SaleDate>";
					strContents += "<PhysAvail>" + "" + "</PhysAvail>";
					strContents += "<VirtAvail>" + "" + "</VirtAvail>";
					strContents += "<Trip>" + info.strTrip + "</Trip>";
					for (UInvenBlotDInfo dInfo : info.details) {
						strContents += "<Detail>";
						strContents += "<DefLocnNo>" + dInfo.nLocnNo + "</DefLocnNo>";
						strContents += "<TrxnNo>" + dInfo.nTrxnNo + "</TrxnNo>";
						strContents += "<Trip>" + dInfo.strTrip + "</Trip>";
						strContents += "<ItemNo>" + dInfo.strItemNo + "</ItemNo>";
						strContents += "<LotNumber>" + dInfo.strLotNumber + "</LotNumber>";
						strContents += "<BatchDate>" + dInfo.strBatchDate + "</BatchDate>";
						strContents += "<BeginStock>" + Utils.getMultipliedString(dInfo.nBeginStock, mContext) + "</BeginStock>";
						strContents += "<Loaded>" + Utils.getMultipliedString(dInfo.nLoaded, mContext) + "</Loaded>";
						strContents += "<LoadAdj>" + Utils.getMultipliedString(dInfo.nLoadAdj, mContext) + "</LoadAdj>";
						strContents += "<TransIn>" + Utils.getMultipliedString(dInfo.nTransIn, mContext) + "</TransIn>";
						strContents += "<TransOut>" + Utils.getMultipliedString(dInfo.nTransOut, mContext) + "</TransOut>";
						strContents += "<Spoils>" + Utils.getMultipliedString(dInfo.nSpoils, mContext) + "</Spoils>";
						strContents += "<Dumps>" + Utils.getMultipliedString(dInfo.nDumps, mContext) + "</Dumps>";
						strContents += "<Sales>" + Utils.getMultipliedString(dInfo.nSales, mContext) + "</Sales>";
						strContents += "<Free>" + Utils.getMultipliedString(dInfo.nFree, mContext) + "</Free>";
						strContents += "<BuyBacks>" + Utils.getMultipliedString(dInfo.nBuyBacks, mContext) + "</BuyBacks>";
						strContents += "<Unload>" + Utils.getMultipliedString(dInfo.nUnLoad, mContext) + "</Unload>";
						strContents += "<DumpsUnload>" + Utils.getMultipliedString(dInfo.nDumpsUnLoad, mContext) + "</DumpsUnload>";
						strContents += "<EndAdj>" + Utils.getMultipliedString(dInfo.nEndAdj, mContext) + "</EndAdj>";
						strContents += "<Stock>" + Utils.getMultipliedString(dInfo.nStock, mContext) + "</Stock>";
						strContents += "<Samples>" + Utils.getMultipliedString(dInfo.nSamples, mContext) + "</Samples>";
						strContents += "</Detail>";
					}

					strContents += "</Invenblot>";
				}
				strContents += "</Invenblots>";
				writeStringAsFile(strContents, strUploadDir, strTransactionsFileName);
			}

			// write to UOrderStatus.upl
			if (uOrderStatusInfoList.size() > 0) {
				String strTransactionsFileName = "OrderStatus" + strSuffix + ".upl";
				boolean bFirst = true;
				strContents = "<OrderStatusS>";
				for (UOrderStatusHInfo info : uOrderStatusInfoList) {
					if (bFirst) {
						if (info.strTrxnDate.length() > 0 && info.strTrxnTime.length() > 0) {
							strTransactionsFileName = "OrderStatus" + info.strTrxnDate + info.strTrxnTime + ".upl";
						}
						fileList.add(strTransactionsFileName);
						bFirst = false;
					}
					strContents += "<OrderStatus>";
					strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strContents += "<DocNo>" + info.strDocNo + "</DocNo>";
					strContents += "<DocType>" + info.strDocType + "</DocType>";
					strContents += "<VoidFlag>" + "" + "</VoidFlag>";
					strContents += "<PrintedFlag>" + "" + "</PrintedFlag>";
					strContents += "<Reference>" + "" + "</Reference>";
					strContents += "<TCOMStatus>" + "" + "</TCOMStatus>";
					strContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
					strContents += "<TrxnTime>" + info.strTrxnTime + "</TrxnTime>";
					strContents += "<RouteNumber>" + Utils.getStringSetting(mContext, Global.PREF_ROUTE_NUMBER) + "</RouteNumber>";
					strContents += "<TripNumber>" + Utils.getStringSetting(mContext, Global.PREF_TRIP_NUMBER) + "</TripNumber>";
					for (UOrderStatusDInfo dInfo : info.uOrderStatusDInfoList) {
						strContents += "<Detail>";
						strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
						strContents += "<ChainNo>" + dInfo.nChainNo + "</ChainNo>";
						strContents += "<CustNo>" + dInfo.nCustNo + "</CustNo>";
						strContents += "<DayNo>" + dInfo.nDayNo + "</DayNo>";
						/*if (TextUtils.isEmpty(dInfo.strDeliveryDate)) {
							strContents += "<DeliveryDate>" + info.strTrxnDate + "</DeliveryDate>";
						} else {
							strContents += "<DeliveryDate>" + dInfo.strDeliveryDate + "</DeliveryDate>";
						}*/
						strContents += "<DeliveryDate>" + info.strTrxnDate + "</DeliveryDate>";
						strContents += "<DeliverySequence>" + dInfo.nDeliverySequence + "</DeliverySequence>";
						strContents += "<OrderNo>" + dInfo.strOrderNo + "</OrderNo>";
						strContents += "<OrderBarcode>" + dInfo.strOrderBarcode + "</OrderBarcode>";
						strContents += "<PORef>" + dInfo.strPORef + "</PORef>";
						strContents += "<RouteNumber>" + Utils.getStringSetting(mContext, Global.PREF_ROUTE_NUMBER) + "</RouteNumber>";
						strContents += "<TripNumber>" + Utils.getStringSetting(mContext, Global.PREF_TRIP_NUMBER) + "</TripNumber>";
						if (Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 0) {
							if (dInfo.nStatus == 4 || dInfo.nStatus == 5 || dInfo.nStatus == 6) {
								strContents += "<Status>" + dInfo.nStatus + "</Status>";
							} else {
								strContents += "<Status>" + "1" + "</Status>";
							}
						} else {
							strContents += "<Status>" + dInfo.nStatus + "</Status>";
						}
						strContents += "<Reference>" + dInfo.strReference + "</Reference>";
						strContents += "</Detail>";
					}

					strContents += "</OrderStatus>";
				}
				strContents += "</OrderStatusS>";
				writeStringAsFile(strContents, strUploadDir, strTransactionsFileName);
			}

			// write to FileTransactions.upl
			if (uFileTransactionInfoList.size() > 0) {
				String strFileTransactionName = "Files" + strSuffix + ".upl";
//				fileList.add(strTransactionsFileName);
				boolean bFirst = true;
				strContents = "<Files>";
				for (UFileTransactionInfo info : uFileTransactionInfoList) {
					if (bFirst) {
						if (info.strTrxnDate.length() > 0 && info.strTrxnTime.length() > 0) {
							strFileTransactionName = "Files" + info.strTrxnDate + info.strTrxnTime + ".upl";
						}
						fileList.add(strFileTransactionName);
						bFirst = false;
					}

					strContents += "<File>";
					strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strContents += "<DocNo>" + "" + "</DocNo>";
					strContents += "<DocType>" + info.strDocType + "</DocType>";
					strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
					strContents += "<PrintedFlag>" + "0" + "</PrintedFlag>";
					strContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
					strContents += "<TrxnTime>" + info.strTrxnTime + "</TrxnTime>";
					strContents += "<Reference>" + "" + "</Reference>";
					strContents += "<TCOMStatus>" + "0" + "</TCOMStatus>";
					strContents += "<ChainNo>" + info.nChainNo + "</ChainNo>";
					strContents += "<CustNo>" + info.nCustNo + "</CustNo>";
					strContents += "<SaleDate>" + info.strTrxnDate + "</SaleDate>";
					strContents += "<PhysAvail>" + "0" + "</PhysAvail>";
					strContents += "<VirtAvail>" + "0" + "</VirtAvail>";
					strContents += "<Trip>" + info.nTripNo + "</Trip>";

					strContents += "<FileTrxnNo>" + info.nFileTrxnNo + "</FileTrxnNo>";
					strContents += "<FileDocNo>" + info.strFileDocNo + "</FileDocNo>";
					strContents += "<FileShortDescription>" + info.strFileShortDescription + "</FileShortDescription>";
					strContents += "<FileLongDescription>" + info.strFileLongDescription + "</FileLongDescription>";
					strContents += "<FileCreateDate>" + info.strFileCreateDate + "</FileCreateDate>";
					strContents += "<FileCreateTime>" + info.strFileCreateTime + "</FileCreateTime>";
					strContents += "<FileFileName>" + info.strFileFileName + "</FileFileName>";
					strContents += "</File>";
				}
				strContents += "</Files>";
				writeStringAsFile(strContents, strUploadDir, strFileTransactionName);
			}

			String strLocnNo = Utils.getStringSetting(mContext, Global.PREF_DEF_LOCN_NO);
			// write to Inven.upl
			if (uInvenHList.size() > 0) {
				// need to check if we need upload
				boolean bInventExist = false;
				for (UInvenHInfo info : uInvenHList) {
					if (info.uInvenDList != null && info.uInvenDList.size() > 0) {
						bInventExist = true;
						break;
					}
				}
				if (bInventExist || !Utils.getStringSetting(mContext, Global.PREF_DISABLE_LOAD_ADJUST).equals("1")) {
					String strInvenFileName = "Inven" + strSuffix + ".upl";
//				fileList.add(strInvenFileName);
					boolean bFirst = true;
					strContents = "<Invens>";
					for (UInvenHInfo info : uInvenHList) {
/*					info.nTrxnNo = lTime;
					info.strTrxnDate = strTrxnDate;
					info.strTrxnTime = strTrxnTime;*/
						if (bFirst) {
							if (info.strTrxnDate.length() > 0 && info.strTrxnTime.length() > 0) {
								strInvenFileName = "Inven" + info.strTrxnDate + info.strTrxnTime + ".upl";
							}
							fileList.add(strInvenFileName);
							bFirst = false;
						}
						strContents += "<Inven>";
						strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
						strContents += "<DocNo>" + strDocNo + "</DocNo>";
						strContents += "<DocType>" + info.strDocType + "</DocType>";
						strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
						strContents += "<PrintedFlag>" + "0" + "</PrintedFlag>";
						strContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
						strContents += "<TrxnTime>" + info.strTrxnTime + "</TrxnTime>";
						strContents += "<Reference>" + info.strReference + "</Reference>";
						strContents += "<TCOMStatus>" + "0" + "</TCOMStatus>";
						strContents += "<ChainNo>" + "0" + "</ChainNo>";
						strContents += "<CustNo>" + "0" + "</CustNo>";
						strContents += "<SaleDate>" + info.strTrxnDate + "</SaleDate>";
						strContents += "<PhysAvail>" + "0" + "</PhysAvail>";
						strContents += "<VirtAvail>" + "0" + "</VirtAvail>";
						strContents += "<Trip>" + Global.gTripNumber + "</Trip>";
						List<UInvenDInfo> newList = new ArrayList<>();
						newList.clear();
						for (UInvenDInfo detailInfo : info.uInvenDList) {
//						if(detailInfo.bCompleted == false)	continue;
							boolean bExist = false;
							UInvenDInfo existItem = null;
							for (UInvenDInfo newInfo : newList) {
								if (newInfo != null && detailInfo != null && detailInfo.strItemNo.equals(newInfo.strItemNo)) {
									bExist = true;
									existItem = newInfo;
									break;
								}
							}
							if (bExist && existItem != null) {
								existItem.nStock += detailInfo.nStock;
								existItem.nLoaded += detailInfo.nLoaded;
							} else
								newList.add(detailInfo);
						}
						info.uInvenDList = newList;
						for (UInvenDInfo detailInfo : info.uInvenDList) {
							int nSCWType = getProdLoconSCWType(mContext, detailInfo.strItemNo);
							if (nSCWType == Global.WEIGHT_ITEM) {
								strContents += "<Detail>";
								strContents += "<DefLocnNo>" + strLocnNo + "</DefLocnNo>";
								strContents += "<TrxnNo>" + detailInfo.nTrxnNo + "</TrxnNo>";
								strContents += "<Trip>" + Global.gTripNumber + "</Trip>";
								strContents += "<ItemNo>" + detailInfo.strItemNo + "</ItemNo>";

								strContents += "<BeginStock>" + ((detailInfo.dBeginQty != 0) ? "1" : "0") + "</BeginStock>";
								strContents += "<Loaded>" + ((detailInfo.dLoaded != 0) ? "1" : "0") + "</Loaded>";
								strContents += "<LoadAdj>" + ((detailInfo.dLoadAdj != 0) ? "1" : "0") + "</LoadAdj>";
								strContents += "<TransIn>" + ((detailInfo.dTransIn != 0) ? "1" : "0") + "</TransIn>";
								strContents += "<TransOut>" + ((detailInfo.dTransOut != 0) ? "1" : "0") + "</TransOut>";
								strContents += "<Spoils>" + ((detailInfo.dSpoils != 0) ? "1" : "0") + "</Spoils>";
								strContents += "<Dumps>" + ((detailInfo.dDumps != 0) ? "1" : "0") + "</Dumps>";
								strContents += "<Sales>" + ((detailInfo.dSales != 0) ? "1" : "0") + "</Sales>";
								strContents += "<Free>" + "0" + "</Free>";
								strContents += "<BuyBacks>" + ((detailInfo.dBuybacks != 0) ? "1" : "0") + "</BuyBacks>";
								strContents += "<Unload>" + ((detailInfo.dUnload != 0) ? "1" : "0") + "</Unload>";
								strContents += "<DumpsUnload>" + ((detailInfo.dDumpsUnload != 0) ? "1" : "0") + "</DumpsUnload>";
								if (info.strDocType.equals("STKE")) {
									strContents += "<EndAdj>" + ((detailInfo.dSales + detailInfo.dStock - detailInfo.dBeginQty - detailInfo.dLoaded - detailInfo.dLoadAdj != 0) ? "1" : "0") + "</EndAdj>";
								} else {
									strContents += "<EndAdj>" + Utils.getMultipliedString(detailInfo.dSales + detailInfo.dStock - detailInfo.dBeginQty - detailInfo.dLoaded - detailInfo.dLoadAdj, mContext) + "</EndAdj>";
								}
								strContents += "<Stock>" + ((detailInfo.dStock != 0) ? "1" : "0") + "</Stock>";
								strContents += "<Samples>" + "0" + "</Samples>";
								strContents += "</Detail>";

								strContents += "<Weight>";
								strContents += "<DefLocnNo>" + strLocnNo + "</DefLocnNo>";
								strContents += "<TrxnNo>" + detailInfo.nTrxnNo + "</TrxnNo>";
								strContents += "<Trip>" + Global.gTripNumber + "</Trip>";
								strContents += "<ItemNo>" + detailInfo.strItemNo + "</ItemNo>";

								strContents += "<BeginStockW>" + Utils.getMultipliedString(detailInfo.dBeginQty, mContext) + "</BeginStockW>";
								strContents += "<LoadedW>" + Utils.getMultipliedString(detailInfo.dLoaded, mContext) + "</LoadedW>";
								strContents += "<LoadAdjW>" + Utils.getMultipliedString(detailInfo.dLoadAdj, mContext) + "</LoadAdjW>";
								strContents += "<TransInW>" + Utils.getMultipliedString(detailInfo.dTransIn, mContext) + "</TransInW>";
								strContents += "<TransOutW>" + Utils.getMultipliedString(detailInfo.dTransOut, mContext) + "</TransOutW>";
								strContents += "<SpoilsW>" + Utils.getMultipliedString(detailInfo.dSpoils, mContext) + "</SpoilsW>";
								strContents += "<DumpsW>" + Utils.getMultipliedString(detailInfo.dDumps, mContext) + "</DumpsW>";
								strContents += "<SalesW>" + Utils.getMultipliedString(detailInfo.dSales, mContext) + "</SalesW>";
								strContents += "<FreeW>" + "0" + "</FreeW>";
								strContents += "<BuyBacksW>" + Utils.getMultipliedString(detailInfo.dBuybacks, mContext) + "</BuyBacksW>";
								strContents += "<UnloadW>" + Utils.getMultipliedString(detailInfo.dUnload, mContext) + "</UnloadW>";
								strContents += "<DumpsUnloadW>" + Utils.getMultipliedString(detailInfo.dDumpsUnload, mContext) + "</DumpsUnloadW>";
								if (info.strDocType.equals("STKE")) {
									strContents += "<EndAdjW>" + Utils.getMultipliedString(detailInfo.dSales + detailInfo.dStock - detailInfo.dBeginQty - detailInfo.dLoaded - detailInfo.dLoadAdj, mContext) + "</EndAdjW>";
								} else {
									strContents += "<EndAdjW>" + "0" + "</EndAdjW>";
								}
								strContents += "<StockW>" + Utils.getMultipliedString(detailInfo.dStock, mContext) + "</StockW>";
								strContents += "<SamplesW>" + "0" + "</SamplesW>";
								strContents += "</Weight>";

								if(detailInfo.loadBlotInfoList != null && detailInfo.loadBlotInfoList.size() > 0)
								{
									for(LoadBlotInfo loadBlotInfo : detailInfo.loadBlotInfoList)
									{
										strContents += "<BatchLot>";
										strContents += "<TrxnNo>" + detailInfo.nTrxnNo + "</TrxnNo>";
										strContents += "<DetailType>" + "" + "</DetailType>";
										strContents += "<ItemNo>" + detailInfo.strItemNo + "</ItemNo>";
										strContents += "<LocnNo>" + strLocnNo + "</LocnNo>";
										strContents += "<ReasonCode>" + detailInfo.nReasonCode + "</ReasonCode>";
										strContents += "<Sequence>" + detailInfo.nSequence + "</Sequence>";
										strContents += "<LotNumber>" + loadBlotInfo.strLotNumber + "</LotNumber>";
										strContents += "<BatchDate>" + loadBlotInfo.strBatchDate + "</BatchDate>";
										strContents += "<ActualQtyLot>" + Utils.getMultipliedString(1, mContext) + "</ActualQtyLot>";
										strContents += "<BeginQtyLot>" + "0" + "</BeginQtyLot>";
										strContents += "<SuggestQtyLot>" + Utils.getMultipliedString(1, mContext) + "</SuggestQtyLot>";
										strContents += "</BatchLot>";
									}
								}
							} else {
								strContents += "<Detail>";
								strContents += "<DefLocnNo>" + strLocnNo + "</DefLocnNo>";
								strContents += "<TrxnNo>" + detailInfo.nTrxnNo + "</TrxnNo>";
								strContents += "<Trip>" + Global.gTripNumber + "</Trip>";
								strContents += "<ItemNo>" + detailInfo.strItemNo + "</ItemNo>";

								strContents += "<BeginStock>" + Utils.getMultipliedString(detailInfo.nBeginQty, mContext) + "</BeginStock>";
								strContents += "<Loaded>" + Utils.getMultipliedString(detailInfo.nLoaded, mContext) + "</Loaded>";
								strContents += "<LoadAdj>" + Utils.getMultipliedString(detailInfo.nLoadAdj, mContext) + "</LoadAdj>";
								strContents += "<TransIn>" + Utils.getMultipliedString(detailInfo.nTransIn, mContext) + "</TransIn>";
								strContents += "<TransOut>" + Utils.getMultipliedString(detailInfo.nTransOut, mContext) + "</TransOut>";
								strContents += "<Spoils>" + Utils.getMultipliedString(detailInfo.nSpoils, mContext) + "</Spoils>";
								strContents += "<Dumps>" + Utils.getMultipliedString(detailInfo.nDumps, mContext) + "</Dumps>";
								strContents += "<Sales>" + Utils.getMultipliedString(detailInfo.nSales, mContext) + "</Sales>";
								strContents += "<Free>" + "0" + "</Free>";
								strContents += "<BuyBacks>" + Utils.getMultipliedString(detailInfo.nBuybacks, mContext) + "</BuyBacks>";
								strContents += "<Unload>" + Utils.getMultipliedString(detailInfo.nUnload, mContext) + "</Unload>";
								strContents += "<DumpsUnload>" + Utils.getMultipliedString(detailInfo.nDumpsUnload, mContext) + "</DumpsUnload>";
								if (info.strDocType.equals("STKE")) {
									strContents += "<EndAdj>" + Utils.getMultipliedString(detailInfo.nEndAdj, mContext) + "</EndAdj>";
								} else {
									strContents += "<EndAdj>" + Utils.getMultipliedString(detailInfo.nSales + detailInfo.nStock - detailInfo.nBeginQty - detailInfo.nLoaded - detailInfo.nLoadAdj, mContext) + "</EndAdj>";
								}
								strContents += "<Stock>" + Utils.getMultipliedString(detailInfo.nStock, mContext) + "</Stock>";
								strContents += "<Samples>" + "0" + "</Samples>";
								strContents += "</Detail>";

								if(detailInfo.loadBlotInfoList != null && detailInfo.loadBlotInfoList.size() > 0)
								{
									for(LoadBlotInfo loadBlotInfo : detailInfo.loadBlotInfoList)
									{
										strContents += "<BatchLot>";
										strContents += "<TrxnNo>" + detailInfo.nTrxnNo + "</TrxnNo>";
										strContents += "<DetailType>" + "" + "</DetailType>";
										strContents += "<ItemNo>" + detailInfo.strItemNo + "</ItemNo>";
										strContents += "<LocnNo>" + strLocnNo + "</LocnNo>";
										strContents += "<ReasonCode>" + detailInfo.nReasonCode + "</ReasonCode>";
										strContents += "<Sequence>" + detailInfo.nSequence + "</Sequence>";
										strContents += "<LotNumber>" + loadBlotInfo.strLotNumber + "</LotNumber>";
										strContents += "<BatchDate>" + loadBlotInfo.strBatchDate + "</BatchDate>";
										strContents += "<ActualQtyLot>" + Utils.getMultipliedString(1, mContext) + "</ActualQtyLot>";
										strContents += "<BeginQtyLot>" + "0" + "</BeginQtyLot>";
										strContents += "<SuggestQtyLot>" + Utils.getMultipliedString(1, mContext) + "</SuggestQtyLot>";
										strContents += "</BatchLot>";
									}
								}
							}
						}
						strContents += "</Inven>";
					}
					strContents += "</Invens>";
					writeStringAsFile(strContents, strUploadDir, strInvenFileName);
				}
			}

			// write to Stock.upl
			if (uStockHInfos.size() > 0) {
				String strInvenFileName = "Stocks" + strSuffix + ".upl";
				boolean bFirst = true;
				strContents = "<Stocks>";
				for (UStockHInfo info : uStockHInfos) {
					if (bFirst) {
						if (info.strTrxnDate.length() > 0 && info.strTrxnTime.length() > 0) {
							strInvenFileName = "Stocks" + info.strTrxnDate + info.strTrxnTime + ".upl";
						}
						fileList.add(strInvenFileName);
						bFirst = false;
					}
					Map<String, List<BatchLot>> batchLotListMap = uStockBatchLotMap.get(info.nTrxnNo);

					strContents += "<Stock>";
					strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strContents += "<DocNo>" + info.strDocNo + "</DocNo>";
					strContents += "<DocType>" + info.strDocType + "</DocType>";
					strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
					strContents += "<PrintedFlag>" + info.printedFlag + "</PrintedFlag>";
					strContents += "<Reference>" + info.strReference + "</Reference>";
					strContents += "<TcomStatus>" + "0" + "</TcomStatus>";
					strContents += "<Chainno>" + "0" + "</Chainno>";
					strContents += "<Custno>" + "0" + "</Custno>";
					strContents += "<SaleDate>" + info.strTrxnDate + "</SaleDate>";
					strContents += "<PhysAvail>" + "0" + "</PhysAvail>";
					strContents += "<VirtAvail>" + "0" + "</VirtAvail>";
					strContents += "<DetailKey>" + "0" + "</DetailKey>";
					strContents += "<TransferRoute>" + info.strTransferRoute + "</TransferRoute>";
					strContents += "<RequiredDate>" + info.strRequiredDate + "</RequiredDate>";
					strContents += "<Period>" + "0" + "</Period>";
					strContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
					strContents += "<TrxnTime>" + info.strTrxnTime + "</TrxnTime>";
					strContents += "<StockType>" + info.strStockType + "</StockType>";
					strContents += "<ToWarehouse>" + info.strToWarehouse + "</ToWarehouse>";
					strContents += "<DetailKeyExpanded>" + info.strDetailKeyExpanded + "</DetailKeyExpanded>";
					strContents += "<TripNumber>" + Global.gTripNumber + "</TripNumber>";

					for (UStockDInfo detailInfo : info.uStockDInfoList) {
						List<BatchLot> batchLotList = null;
						if(batchLotListMap != null && batchLotListMap.size() > 0)
						{
							 batchLotList = batchLotListMap.get(detailInfo.strItemNo);
						}

						strContents += "<Detail>";
						strContents += "<TrxnNo>" + detailInfo.nTrxnNo + "</TrxnNo>";
						strContents += "<DetailType>" + detailInfo.strDetailType + "</DetailType>";
						strContents += "<ItemNo>" + detailInfo.strItemNo + "</ItemNo>";
						strContents += "<LocnNo>" + strLocnNo + "</LocnNo>";
						int nReasonCode = detailInfo.nReasonCode;
						strContents += "<ReasonCode>" + nReasonCode + "</ReasonCode>";
						strContents += "<Sequence>" + detailInfo.nSequence + "</Sequence>";
						if (Utils.getProdLoconSCWType(mContext, detailInfo.strItemNo) == Global.WEIGHT_ITEM) {
							strContents += "<ActualQty>" + getMultipliedString(detailInfo.dActualQty, mContext) + "</ActualQty>";
							strContents += "<BeginQty>" + getMultipliedString(detailInfo.dBeginQty, mContext) + "</BeginQty>";
							strContents += "<SuggestQty>" + getMultipliedString(detailInfo.dSuggestQty, mContext) + "</SuggestQty>";
						} else {
							strContents += "<ActualQty>" + getMultipliedString(detailInfo.nActualQty, mContext) + "</ActualQty>";
							strContents += "<BeginQty>" + getMultipliedString(detailInfo.nBeginQty, mContext) + "</BeginQty>";
							strContents += "<SuggestQty>" + getMultipliedString(detailInfo.nSuggestQty, mContext) + "</SuggestQty>";
						}
						strContents += "</Detail>";

						if(batchLotList != null)
						{
							for(BatchLot batchLot : batchLotList)
							{
								strContents += "<BatchLot>";
								strContents += "<TrxnNo>" + batchLot.nTrxnNo + "</TrxnNo>";
								strContents += "<DetailType>" + batchLot.strDetailType + "</DetailType>";
								strContents += "<ItemNo>" + batchLot.strItemNo + "</ItemNo>";
								strContents += "<LocnNo>" + batchLot.nLocnNo + "</LocnNo>";
								strContents += "<ReasonCode>" + batchLot.nReasonCode + "</ReasonCode>";
								strContents += "<Sequence>" + batchLot.nSequence + "</Sequence>";
								strContents += "<LotNumber>" + batchLot.strLotNumber + "</LotNumber>";
								strContents += "<BatchDate>" + batchLot.strBatchDate + "</BatchDate>";
								strContents += "<ActualQtyLot>" + getMultipliedString(batchLot.nActualQty, mContext) + "</ActualQtyLot>";
								strContents += "<BeginQtyLot>" + getMultipliedString(batchLot.nBeginQty, mContext) + "</BeginQtyLot>";
								strContents += "<SuggestQtyLot>" + getMultipliedString(batchLot.nSuggestQty, mContext) + "</SuggestQtyLot>";
								strContents += "</BatchLot>";
							}
						}
					}
					strContents += "</Stock>";
				}
				strContents += "</Stocks>";
				writeStringAsFile(strContents, strUploadDir, strInvenFileName);
			}

			// write to Consignment.upl
			if (uConsignmentInfoList.size() > 0) {
				String strInvenFileName = "Consignments" + strSuffix + ".upl";
				boolean bFirst = true;
				strContents = "<Consignments>";
				for (UStockHInfo info : uConsignmentInfoList) {
					if (bFirst) {
						if (info.strTrxnDate.length() > 0 && info.strTrxnTime.length() > 0) {
							strInvenFileName = "Consignments" + info.strTrxnDate + info.strTrxnTime + ".upl";
						}
						fileList.add(strInvenFileName);
						bFirst = false;
					}

					strContents += "<Consignment>";
					strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strContents += "<DocNo>" + info.strDocNo + "</DocNo>";
					strContents += "<DocType>" + info.strDocType + "</DocType>";
					strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
					strContents += "<PrintedFlag>" + "0" + "</PrintedFlag>";
					strContents += "<Reference></Reference>";
					strContents += "<TcomStatus>" + "0" + "</TcomStatus>";
					strContents += "<Chainno>" + info.nChainNo + "</Chainno>";
					strContents += "<Custno>" + info.nCustNo + "</Custno>";
					strContents += "<SaleDate>" + info.strTrxnDate + "</SaleDate>";
					strContents += "<PhysAvail>" + "0" + "</PhysAvail>";
					strContents += "<VirtAvail>" + "0" + "</VirtAvail>";
					strContents += "<Period>" + "0" + "</Period>";
					strContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
					strContents += "<TrxnTime>" + info.strTrxnTime + "</TrxnTime>";
					strContents += "<ConsignmentType>" + info.strStockType + "</ConsignmentType>";

					for (UStockDInfo detailInfo : info.uStockDInfoList) {
						strContents += "<Detail>";
						strContents += "<TrxnNo>" + detailInfo.nTrxnNo + "</TrxnNo>";
						strContents += "<ItemNo>" + detailInfo.strItemNo + "</ItemNo>";
						strContents += "<ReasonCode>" + "0" + "</ReasonCode>";
						if (Utils.getProdLoconSCWType(mContext, detailInfo.strItemNo) == Global.WEIGHT_ITEM) {
							strContents += "<ActualQty>0</ActualQty>";
							strContents += "<BeginQty>0</BeginQty>";
							strContents += "<SuggestQty>0</SuggestQty>";
							strContents += "<ActualQtyW>" + getMultipliedString(detailInfo.dActualQty, mContext) + "</ActualQtyW>";
							strContents += "<BeginQtyW>" + getMultipliedString(detailInfo.dBeginQty, mContext) + "</BeginQtyW>";
							strContents += "<SuggestQtyW>0</SuggestQtyW>";
						} else {
							strContents += "<ActualQty>" + getMultipliedString(detailInfo.nActualQty, mContext) + "</ActualQty>";
							strContents += "<BeginQty>" + getMultipliedString(detailInfo.nBeginQty, mContext) + "</BeginQty>";
							strContents += "<SuggestQty>0</SuggestQty>";
							strContents += "<ActualQtyW>0</ActualQtyW>";
							strContents += "<BeginQtyW>0</BeginQtyW>";
							strContents += "<SuggestQtyW>0</SuggestQtyW>";
						}
						strContents += "</Detail>";
					}
					strContents += "</Consignment>";
				}
				strContents += "</Consignments>";
				writeStringAsFile(strContents, strUploadDir, strInvenFileName);
			}

			// write to Cameras.upl
			if (uCamerasList.size() > 0) {
				String strCamerasFileName = "Cameras" + strSuffix + ".upl";
//				fileList.add(strCamerasFileName);
				boolean bFirst = true;
				strContents = "<Cameras>";
				for (UCamerasInfo info : uCamerasList) {
/*					info.nTrxnNo = lTime;
					info.strTrxnDate = strTrxnDate;
					info.strTrxnTime = strTrxnTime;*/
					if (bFirst) {
						if (info.strTrxnDate.length() > 0 && info.strTrxnTime.length() > 0) {
							strCamerasFileName = "Cameras" + info.strTrxnDate + info.strTrxnTime + ".upl";
						}
						fileList.add(strCamerasFileName);
						bFirst = false;
					}

					strContents += "<Camera>";
					strContents += "<ChainNo>" + info.nChainNo + "</ChainNo>";
					strContents += "<CustNo>" + info.nCustNo + "</CustNo>";
					strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strContents += "<DocNo>" + strDocNo + "</DocNo>";
					strContents += "<DocType>" + info.strDocType + "</DocType>";
					strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
					strContents += "<PrintedFlag>" + "0" + "</PrintedFlag>";
					strContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
					strContents += "<TrxnTime>" + info.strTrxnTime + "</TrxnTime>";
					strContents += "<Reference>" + info.strReference + "</Reference>";
					strContents += "<TCOMStatus>" + "0" + "</TCOMStatus>";
					strContents += "</Camera>";
				}
				strContents += "</Cameras>";
				writeStringAsFile(strContents, strUploadDir, strCamerasFileName);
			}

			// write to Messages.upl
			if (uMessagesList.size() > 0) {
				String strMessagesFileName = "Messages" + strSuffix + ".upl";
//				fileList.add(strMessagesFileName);
				boolean bFirst = true;
				strContents = "<Messages>";
				for (UMessagesInfo info : uMessagesList) {
/*					info.nTrxnNo = lTime;
					info.strTrxnDate = strTrxnDate;
					info.strTrxnTime = strTrxnTime;*/
					if (bFirst) {
						if (info.strTrxnDate.length() > 0 && info.strTrxnTime.length() > 0) {
							strMessagesFileName = "Messages" + info.strTrxnDate + info.strTrxnTime + ".upl";
						}
						fileList.add(strMessagesFileName);
						bFirst = false;
					}

					strContents += "<Message>";
					strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strContents += "<ChainNo>" + info.nChainNo + "</ChainNo>";
					strContents += "<CustNo>" + info.nCustNo + "</CustNo>";
					strContents += "<MessageID>" + info.nMessageID + "</MessageID>";
					strContents += "<aTrxnNo>" + "0" + "</aTrxnNo>";
					strContents += "<aDocNo>" + "0" + "</aDocNo>";
					strContents += "<DocType>" + "MESS" + "</DocType>";
					strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
					strContents += "<PrintedFlag>" + "0" + "</PrintedFlag>";
					strContents += "<aTrxnDate>" + info.strTrxnDate + "</aTrxnDate>";
					strContents += "<aTrxnTime>" + info.strTrxnTime + "</aTrxnTime>";
					strContents += "<Reference>" + "" + "</Reference>";
					strContents += "<TCOMStatus>" + "0" + "</TCOMStatus>";
					strContents += "<SaleDate>" + info.strTrxnDate + "</SaleDate>";
					strContents += "</Message>";
				}
				strContents += "</Messages>";
				writeStringAsFile(strContents, strUploadDir, strMessagesFileName);
			}

			// write to Surveys.upl
			if (uSurveysList.size() > 0) {
				String strSurveysFileName = "Surveys" + strSuffix + ".upl";
//				fileList.add(strSurveysFileName);
				boolean bFirst = true;
				strContents = "<Surveys>";
				for (USurveysInfo info : uSurveysList) {
/*					info.nTrxnNo = lTime;
					info.strTrxnDate = strTrxnDate;
					info.strTrxnTime = strTrxnTime;*/
					if (bFirst) {
						if (info.strTrxnDate.length() > 0 && info.strTrxnTime.length() > 0) {
							strSurveysFileName = "Surveys" + info.strTrxnDate + info.strTrxnTime + ".upl";
						}
						fileList.add(strSurveysFileName);
						bFirst = false;
					}

					strContents += "<Survey>";
					strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strContents += "<ChainNo>" + info.nChainNo + "</ChainNo>";
					strContents += "<CustNo>" + info.nCustNo + "</CustNo>";
//					strContents += "<SurveyID>" + info.strSurveyID + "</SurveyID>";
					if(info.bFromSurveyID)
					{
						strContents += "<SurveyID>" + info.strSurveyID + "</SurveyID>";
						strContents += "<SurveyHeaderID>" + "</SurveyHeaderID>";
					}
					else {
						strContents += "<SurveyID>" + "</SurveyID>";
						strContents += "<SurveyHeaderID>" + info.strSurveyID + "</SurveyHeaderID>";
					}

					strContents += "<Completed>" + info.strCompleted + "</Completed>";
					strContents += "<aTrxnNo>" + "0" + "</aTrxnNo>";
					strContents += "<aDocNo>" + "0" + "</aDocNo>";
					strContents += "<DocType>" + "SURV" + "</DocType>";
					strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
					strContents += "<PrintedFlag>" + "0" + "</PrintedFlag>";
					strContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
					strContents += "<TrxnTime>" + info.strTrxnTime + "</TrxnTime>";
					strContents += "<Reference>" + info.strReference + "</Reference>";
					strContents += "<TCOMStatus>" + "0" + "</TCOMStatus>";
					strContents += "<SaleDate>" + info.strTrxnDate + "</SaleDate>";
					for (USurveyDetailInfo detailInfo : info.detailList) {
						strContents += "<Detail>";
						strContents += "<TrxnNo>" + detailInfo.nTrxnNo + "</TrxnNo>";
						strContents += "<QuestionID>" + detailInfo.strQuestionID + "</QuestionID>";
						strContents += "<ResultVal>" + detailInfo.strResultVal + "</ResultVal>";
						strContents += "<Visible>" + detailInfo.nVisible + "</Visible>";
						strContents += "<SeqNo>" + detailInfo.nSeqNo + "</SeqNo>";
						strContents += "</Detail>";
					}
					strContents += "</Survey>";
				}
				strContents += "</Surveys>";
				writeStringAsFile(strContents, strUploadDir, strSurveysFileName);
			}

			// write to Orders.upl
			if (uOrderHList.size() > 0) {
				String strOrdersFileName = "Orders" + strSuffix + ".upl";
//				fileList.add(strOrdersFileName);
				boolean bFirst = true;
				strContents = "<Orders>";
				for (UOrderHInfo info : uOrderHList) {
/*					info.nTrxnNo = lTime;
					info.strTrxnDate = strTrxnDate;
					info.strTrxnTime = strTrxnTime;*/
					if (bFirst) {
						if (info.strTrxnDate.length() > 0 && info.strTrxnTime.length() > 0) {
							strOrdersFileName = "Orders" + info.strTrxnDate + strTrxnTime + ".upl";
						}
						fileList.add(strOrdersFileName);
						bFirst = false;
					}

					strContents += "<Order>";

					strContents += "<DocNo>" + (info.strDocNo.equals("0") ? strDocNo : info.strDocNo) + "</DocNo>";
					Date delDate = Utils.getDateFromString(info.strDeliveryDate);
					String strDocType = info.strDocType;
					if(TextUtils.isEmpty(strDocType))
					{
						Date cDate = new Date(System.currentTimeMillis());
						if (cDate.compareTo(delDate) < 0)
							strDocType = "ORD";
						else
							strDocType += "INV";
					}

					if(Utils.getStringSetting(mContext, Global.PREF_UNLESHED).equals("1") && strDocType.equals("INV"))
					{
						strDocType = "ORD";
					}
					strContents += "<DocType>" + strDocType + "</DocType>";
					strContents += "<VoidFlag>" + info.nVoidFlag + "</VoidFlag>";
					//strContents += "<PrintedFlag>" + info.nPrintedFlag + "</PrintedFlag>";
					strContents += "<PrintedFlag>" + "1" + "</PrintedFlag>";
					strContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
					strContents += "<TrxnTime>" + info.strTrxnTime + "</TrxnTime>";
					strContents += "<Reference>" + info.strReference + "</Reference>";
					strContents += "<TCOMStatus>" + "0" + "</TCOMStatus>";
					strContents += "<SaleDate>" + info.strTrxnDate + "</SaleDate>";
					strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strContents += "<ChainNo>" + info.nChainNo + "</ChainNo>";
					strContents += "<CustNo>" + info.nCustNo + "</CustNo>";
					strContents += "<OrderNo>" + info.strOrderNumber + "</OrderNo>";
					strContents += "<DayNo>" + info.nDayNo + "</DayNo>";
					strContents += "<CompletedDate>" + info.strTrxnDate + "</CompletedDate>";
					strContents += "<CompletedTime>" + info.strTrxnTime + "</CompletedTime>";

					strContents += "<TotalAmount>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.nTotalAmount + info.nTotalFree), mContext) + "</TotalAmount>";
					strContents += "<TotalDumps>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.nTotalDumps), mContext) + "</TotalDumps>";
					strContents += "<TotalBuyBack>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.nTotalBuybacks), mContext) + "</TotalBuyBack>";
					strContents += "<TotalSales>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.nTotalAmount), mContext) + "</TotalSales>";

					strContents += "<DTotalAmount>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.dTotalAmount + info.dTotalFree), mContext) + "</DTotalAmount>";
					strContents += "<DTotalDumps>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.dTotalDumps), mContext) + "</DTotalDumps>";
					strContents += "<DTotalBuyBack>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.dTotalBuybacks), mContext) + "</DTotalBuyBack>";
					strContents += "<DTotalSales>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.dTotalAmount), mContext) + "</DTotalSales>";

					strContents += "<PreviousBal>" + "" + "</PreviousBal>";
					strContents += "<SaleAmount>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.nSaleAmount), mContext) + "</SaleAmount>";
					strContents += "<PromotionAmount>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.nPromotionAmount), mContext) + "</PromotionAmount>";
					strContents += "<TaxAmount>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.nTaxAmount + info.nHandlingFeeTaxAmount), mContext) + "</TaxAmount>";

//					boolean bCentsRound = (Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") && (Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2")));
					if (info.bCentsRound) {
						float fValue = info.nSaleAmount + info.nTaxAmount + info.nHandlingFee + info.nHandlingFeeTaxAmount;
						float fFlag = 1;
						if (fValue < 0) {
							fFlag = -1;
						}
						int nTotal = Math.round(Utils.getRoundedValue(fValue * fFlag) * Global.MULTIPLE_VALUE);
						int nOffSet = nTotal % 5000;
						int nInvoiceTotal = ((nTotal / 5000) + ((Math.abs(nOffSet) >= 2500) ? 1 : 0)) * 5000;
						nOffSet = nTotal % 1000;
						nTotal = ((nTotal / 1000) + ((Math.abs(nOffSet) >= 500) ? 1 : 0)) * 1000;
						nOffSet = nInvoiceTotal - nTotal;
						nInvoiceTotal *= fFlag;
						nOffSet *= fFlag;
						strContents += "<CentsRounding>" + (nOffSet != 0 ? nOffSet + Utils.getScalingSuffix(mContext) : 0) + "</CentsRounding>";
						strContents += "<InvoiceTotal>" + (nInvoiceTotal != 0 ? nInvoiceTotal + Utils.getScalingSuffix(mContext) : 0) + "</InvoiceTotal>";
					} else {
						strContents += "<CentsRounding>" + "0" + "</CentsRounding>";
						strContents += "<InvoiceTotal>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, (info.nSaleAmount + info.nTaxAmount + info.nHandlingFee + info.nHandlingFeeTaxAmount)), mContext) + "</InvoiceTotal>";
					}

					strContents += "<HandlingFee>" + Utils.getMultipliedString(info.nHandlingFee, mContext) + "</HandlingFee>";
					strContents += "<HandlingFeeTaxCode>" + info.strHandlingTaxCode + "</HandlingFeeTaxCode>";
					strContents += "<HandlingFeeTax>" + Utils.getMultipliedString(info.nHandlingFeeTaxAmount, mContext) + "</HandlingFeeTax>";

					if(Utils.getStringSetting(mContext, Global.PREF_INVCALC).equals("TTx"))
					{
						strContents += "<DiscSales>" + Utils.getMultipliedStringForTTX(Utils.getRoundedValueForTTx(info.nDiscSales), mContext) + "</DiscSales>";
						strContents += "<DiscDumps>" + Utils.getMultipliedStringForTTX(Utils.getRoundedValueForTTx(info.nDiscDumps), mContext) + "</DiscDumps>";
						strContents += "<DiscBuybacks>" + Utils.getMultipliedStringForTTX(Utils.getRoundedValueForTTx(info.nDiscBuybacks), mContext) + "</DiscBuybacks>";
					}
					else
					{
						strContents += "<DiscSales>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.nDiscSales), mContext) + "</DiscSales>";
						strContents += "<DiscDumps>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.nDiscDumps), mContext) + "</DiscDumps>";
						strContents += "<DiscBuybacks>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.nDiscBuybacks), mContext) + "</DiscBuybacks>";
					}

					strContents += "<NetDumps>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.nNetDumps), mContext) + "</NetDumps>";
					strContents += "<NetBuybacks>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.nNetBuybacks), mContext) + "</NetBuybacks>";
					strContents += "<NetSales>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.nNetSales), mContext) + "</NetSales>";
					strContents += "<TaxableAmount>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.nSaleAmount + info.nHandlingFee), mContext) + "</TaxableAmount>";
					strContents += "<ChangeFlag>" + "" + "</ChangeFlag>";
					strContents += "<DeliveryDate>" + info.strDeliveryDate + "</DeliveryDate>";
					strContents += "<Period>" + info.nPeriodNo + "</Period>";
					//strContents += "<DriverNumber>" + Utils.getStringSetting(mContext, Global.PREF_ROUTE_NUMBER) + "</DriverNumber>";
					strContents += "<DriverNumber>" + info.strDriverNumber + "</DriverNumber>";
					strContents += "<DetailFile>" + "" + "</DetailFile>";
					strContents += "<OffInvPromoAmt>" + "0" + "</OffInvPromoAmt>";
					strContents += "<User1>" + info.strUser1 + "</User1>";
					strContents += "<User2>" + info.strUser2 + "</User2>";
					if (info.nSaleAmount >= 0) {
						strContents += "<PORef>" + ConvertHTM(info.strPOReference) + "</PORef>";
						strContents += "<CredRef>" + "" + "</CredRef>";
					} else {
						strContents += "<PORef>" + "" + "</PORef>";
						strContents += "<CredRef>" + ConvertHTM(info.strPOReference) + "</CredRef>";
					}
					strContents += "<Instrs>" + ConvertHTM(info.strSpecialInstructions) + "</Instrs>";
					strContents += "<DSD>" + "0" + "</DSD>";
					strContents += "<ARBeforeTrxnNo>" + "" + "</ARBeforeTrxnNo>";
					strContents += "<Distr>" + info.strDistributor + "</Distr>";
					strContents += "<SplitNett>" + info.strNettSplit + "</SplitNett>";
					strContents += "<TotalSamples>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.nTotalSamples), mContext) + "</TotalSamples>";
					strContents += "<TotalFree>" + Utils.getMultipliedString(Utils.get5RoundedValue(mContext, info.nTotalFree), mContext) + "</TotalFree>";
					strContents += "<DistrName>" + "" + "</DistrName>";
					strContents += "<TotalExchangeDU>" + "0" + "</TotalExchangeDU>";
					strContents += "<DiscExchangeDU>" + "0" + "</DiscExchangeDU>";
					strContents += "<NetExchangeDU>" + "0" + "</NetExchangeDU>";
					strContents += "<DexStatus>" + "0" + "</DexStatus>";
					strContents += "<SourceType>" + info.strSourceType + "</SourceType>";
					strContents += "<PresoldChgd>" + info.strPresoldChgd + "</PresoldChgd>";
					strContents += "<Reversed>" + "" + "</Reversed>";
					strContents += "<DiscInLieuAmt>" + "0" + "</DiscInLieuAmt>";
					strContents += "<PALLTrxnNo>" + "0" + "</PALLTrxnNo>";
					strContents += "<TicketType>" + "" + "</TicketType>";
					strContents += "<OrderContact>" + ConvertHTM(info.strOrderName) + "</OrderContact>";
					for (UOrderDInfo uOrderDInfo : info.uOrderDList) {
						strContents += getUOrderDContent(mContext, uOrderDInfo);
						if (uOrderDInfo.nPromoType == Global.PROMO_TYPE_BUY_FREE_MULTI) {
							uOrderDInfo.nTrxnType = 12;
							uOrderDInfo.nQuantity = uOrderDInfo.nPromoDiscount == 0 ? 0 : (int) (Utils.myDivider(uOrderDInfo.nQuantity, uOrderDInfo.nPromoDiscount));
							uOrderDInfo.nBasePrice = 0;
							uOrderDInfo.nRetailPrice = 0;
							uOrderDInfo.nTaxAmount = 0;
							uOrderDInfo.nPrice = 0;
							for(UTaxInfo uTaxInfo : uOrderDInfo.uTaxInfoList)
							{
								uTaxInfo.nTrxnNo = uOrderDInfo.nTrxnNo;
								uTaxInfo.nTrxnType = uOrderDInfo.nTrxnType;
							}
							strContents += getUOrderDContent(mContext, uOrderDInfo);
						}
					}
					strContents += "</Order>";
				}
				strContents += "</Orders>";
				writeStringAsFile(strContents, strUploadDir, strOrdersFileName);
			}

			// write to ARTotal.UPL
			if (uARTotalList.size() > 0) {
				String strARTotalFileName = "AR" + strSuffix + ".upl";
//				fileList.add(strARTotalFileName);
				boolean bFirst = true;
				strContents = "<AR>";
				for (UARTotalInfo info : uARTotalList) {
/*					info.nTrxnNo = lTime;
					info.strTrxnDate = strTrxnDate;
					info.strTrxnTime = strTrxnTime;*/
					if (bFirst) {
						if (info.strTrxnDate.length() > 0 && strTrxnTime.length() > 0) {
							strARTotalFileName = "AR" + info.strTrxnDate + strTrxnTime + ".upl";
						}
						fileList.add(strARTotalFileName);
						bFirst = false;
					}
					strContents += "<ARHeader>";
					strContents += "<DocNo>" + info.strDocNo + "</DocNo>";
					strContents += "<DocType>" + info.strDocType + "</DocType>";
					strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
					strContents += "<PrintedFlag>" + "1" + "</PrintedFlag>";
					strContents += "<TCOMStatus>" + "0" + "</TCOMStatus>";
					strContents += "<Trip>" + Global.gTripNumber + "</Trip>";
					strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strContents += "<ChainNo>" + info.nChainNo + "</ChainNo>";
					strContents += "<CustNo>" + info.nCustNo + "</CustNo>";
					strContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
					strContents += "<TrxnTime>" + info.strTrxnTime + "</TrxnTime>";
					strContents += "<SaleDate>" + info.strTrxnDate + "</SaleDate>";
					strContents += "<Reference>" + info.strReference + "</Reference>";
					strContents += "<TotalDue>" + Utils.getMultipliedString(info.nTotalDue, mContext) + "</TotalDue>";
					strContents += "<TotalUnalloc>" + Utils.getMultipliedString(info.nTotalUnalloc, mContext) + "</TotalUnalloc>";
					strContents += "<TotalCsh>" + Utils.getMultipliedString(info.nTotalCash, mContext) + "</TotalCsh>";
					strContents += "<TotalChq>" + Utils.getMultipliedString(info.nTotalCheque, mContext) + "</TotalChq>";
					strContents += "<InvTrxnNo>" + info.nInvoiceTrxnNo + "</InvTrxnNo>";
					strContents += "<InvAmt>" + Utils.getMultipliedString(Utils.getRoundedValue(info.nInvoiceAmount), mContext) + "</InvAmt>";
					int nInvoiceAmt = Utils.parseIntFromString(Utils.getMultipliedString(info.nInvoiceAmount, mContext));

					if (info.paymentList.size() > 0) {
						for (UARPaymentInfo paymentInfo : info.paymentList) {
							strContents += "<Payment>";
							strContents += "<TrxnNo>" + paymentInfo.nTrxnNo + "</TrxnNo>";
							strContents += "<ChainNo>" + paymentInfo.nChainNo + "</ChainNo>";
							strContents += "<CustNo>" + paymentInfo.nCustNo + "</CustNo>";
							strContents += "<PaymentType>" + paymentInfo.nPayType + "</PaymentType>";
							strContents += "<TrxnDate>" + paymentInfo.strTrxnDate + "</TrxnDate>";
							strContents += "<TrxnType>" + "PMT" + "</TrxnType>";
							strContents += "<TrxnAmount>" + Utils.getMultipliedString(paymentInfo.nTrxnAmount, mContext) + "</TrxnAmount>";
							strContents += "<InvNo>" + paymentInfo.strDocNo + "</InvNo>";
							if (TextUtils.isEmpty(paymentInfo.strBankNo)) {
								strContents += "<BankNo>" + "0" + "</BankNo>";
							} else {
								strContents += "<BankNo>" + paymentInfo.strBankNo + "</BankNo>";
							}
							if (TextUtils.isEmpty(paymentInfo.strAccount)) {
								strContents += "<Account>" + "0" + "</Account>";
							} else {
								strContents += "<Account>" + paymentInfo.strAccount + "</Account>";
							}
							if (TextUtils.isEmpty(paymentInfo.strCheckNo)) {
								strContents += "<CheckNo>" + "0" + "</CheckNo>";
							} else {
								strContents += "<CheckNo>" + paymentInfo.strCheckNo + "</CheckNo>";
							}
							if (TextUtils.isEmpty(paymentInfo.strCheckDate)) {
								String strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(info.nInvoiceTrxnNo)));
								strContents += "<CheckDate>" + strTrxnDate + "</CheckDate>";
							} else {
								strContents += "<CheckDate>" + paymentInfo.strCheckDate + "</CheckDate>";
							}
							strContents += "<OrigTrxnNo>" + "0" + "</OrigTrxnNo>";
							strContents += "<SeqNo>" + "0" + "</SeqNo>";
							strContents += "<ReconTrxnNo>" + "0" + "</ReconTrxnNo>";
							strContents += "<ARNotes>" + paymentInfo.strARNotes + "</ARNotes>";
							strContents += "</Payment>";

							strContents += "<Allocation>";
							strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
							strContents += "<ChainNo>" + info.nChainNo + "</ChainNo>";
							strContents += "<CustNo>" + info.nCustNo + "</CustNo>";
							strContents += "<InvDate>" + paymentInfo.strInvoiceDate + "</InvDate>";
							strContents += "<InvNo>" + paymentInfo.strDocNo + "</InvNo>";
							if (nInvoiceAmt > 0) {
								strContents += "<TrxnType>" + "INV" + "</TrxnType>";
							} else {
								strContents += "<TrxnType>" + "CRN" + "</TrxnType>";
							}
							strContents += "<TrxnAmount>" + Utils.getMultipliedString(paymentInfo.nTrxnAmount, mContext) + "</TrxnAmount>";
							strContents += "<InvROAAmt>" + Utils.getMultipliedString(paymentInfo.nTrxnAmount, mContext) + "</InvROAAmt>";

							//
							if (!TextUtils.isEmpty(paymentInfo.strCheckNo)) {
								strContents += "<CheckNo>" + paymentInfo.strCheckNo + "</CheckNo>";
							} else {
								strContents += "<CheckNo>" + "0" + "</CheckNo>";
							}
							strContents += "<OrigTrxnNo>" + "0" + "</OrigTrxnNo>";
							if (TextUtils.isEmpty(paymentInfo.strBankNo)) {
								strContents += "<BankNo>" + "0" + "</BankNo>";
							} else {
								strContents += "<BankNo>" + paymentInfo.strBankNo + "</BankNo>";
							}
							if (TextUtils.isEmpty(paymentInfo.strAccount)) {
								strContents += "<Account>" + "0" + "</Account>";
							} else {
								strContents += "<Account>" + paymentInfo.strAccount + "</Account>";
							}
							strContents += "<User1>" + paymentInfo.strDescription + "</User1>";
							strContents += "<User2>" + " " + "</User2>";
							strContents += "</Allocation>";
						}
					}

					strContents += "</ARHeader>";
				}
				strContents += "</AR>";
				writeStringAsFile(strContents, strUploadDir, strARTotalFileName);
			}

			// write to Services.upl
			if (uServicesList.size() > 0) {

				String strServicesFileName = "Services" + strSuffix + ".upl";
//				fileList.add(strServicesFileName);
				boolean bFirst = true;
				strContents = "<Services>";
				for (UServicesInfo info : uServicesList) {
/*					info.nTrxnNo = lTime;
					info.strTrxnDate = strTrxnDate;
					info.strTrxnTime = strTrxnTime;*/
					if (bFirst) {
						if (info.strTrxnDate.length() > 0 && info.strTrxnTime.length() > 0) {
							strServicesFileName = "Services" + info.strTrxnDate + info.strTrxnTime + ".upl";
						}
						bFirst = false;
						fileList.add(strServicesFileName);
					}

					strContents += "<Service>";
					strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strContents += "<ChainNo>" + info.nChainNo + "</ChainNo>";
					strContents += "<CustNo>" + info.nCustNo + "</CustNo>";
					strContents += "<Period>" + info.nPeriod + "</Period>";
					strContents += "<Seq>" + info.nSeqNo + "</Seq>";
					strContents += "<Done>" + info.nDone + "</Done>";
					strContents += "<Reason>" + info.strReason + "</Reason>";
					strContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
					strContents += "<TrxnTime>" + info.strTrxnTime + "</TrxnTime>";
					strContents += "<aTrxnNo>" + "0" + "</aTrxnNo>";
					strContents += "<aDocNo>" + "0" + "</aDocNo>";
					strContents += "<DocType>" + "SERV" + "</DocType>";
					strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
					strContents += "<PrintedFlag>" + "0" + "</PrintedFlag>";
					strContents += "<aTrxnDate>" + info.strTrxnDate + "</aTrxnDate>";
					strContents += "<aTrxnTime>" + info.strTrxnTime + "</aTrxnTime>";
					strContents += "<Reference>" + "" + "</Reference>";
					strContents += "<TCOMStatus>" + "0" + "</TCOMStatus>";
					strContents += "<SaleDate>" + info.strTrxnDate + "</SaleDate>";
					strContents += "</Service>";
				}
				strContents += "</Services>";
				writeStringAsFile(strContents, strUploadDir, strServicesFileName);
			}

			// write to Collections.UPL
			if (uCollectionsDList.size() > 0) {
				String strCollectionsFileName = "Collections" + strSuffix + ".upl";
//				fileList.add(strCollectionsFileName);
				boolean bFirst = true;
				strContents = "<Collections>";
				for (UCollectionsDInfo info : uCollectionsDList) {
/*					info.nTrxnNo = lTime;
					info.strTrxnDate = strTrxnDate;
//					info.strTrxnTime = strTrxnTime;*/
					if (bFirst) {
						if (info.strTrxnDate.length() > 0) {
							strCollectionsFileName = "Collections" + info.strTrxnDate + ".upl";
						}
						bFirst = false;
						fileList.add(strCollectionsFileName);
					}

					strContents += "<Collection>";
					strContents += "<Trip>" + Global.gTripNumber + "</Trip>";
					strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strContents += "<ChainNo>" + info.nChainNo + "</ChainNo>";
					strContents += "<CustNo>" + info.nCustNo + "</CustNo>";
					strContents += "<SeqNo>" + info.nSeqNo + "</SeqNo>";
					strContents += "<PayType>" + info.nPayType + "</PayType>";
					strContents += "<BankNo>" + info.strBankNo + "</BankNo>";
					strContents += "<Account>" + info.strAccount + "</Account>";
					strContents += "<CheckNo>" + info.strCheckNo + "</CheckNo>";
					strContents += "<CheckDate>" + info.strCheckDate + "</CheckDate>";
					strContents += "<TrxnAmount>" + Utils.getMultipliedString(info.nTrxnAmount, mContext) + "</TrxnAmount>";
					strContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
					strContents += "</Collection>";
				}
				strContents += "</Collections>";
				writeStringAsFile(strContents, strUploadDir, strCollectionsFileName);
			}

			// write to UTripSchedule.UPL
			if (uRouteSchHeaderList.size() > 0) {
				String strRouteSchFileName = "TripSchedule" + strSuffix + ".upl";
//							fileList.add(strCollectionsFileName);
				boolean bFirst = true;
				strContents = "<TripSchedules>";
				for (int i = 0; i < uRouteSchHeaderList.size(); i++) {
					strContents += "<TripSchedule>";
					strContents += "<TrxnNo>" + uRouteSchHeaderList.get(i).nTrxnNo + "</TrxnNo>";
					strContents += "<DocNo>" + "0" + "</DocNo>";
					strContents += "<DocType>" + uRouteSchHeaderList.get(i).strDocType + "</DocType>";
					strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
					strContents += "<PrintedFlag>" + "0" + "</PrintedFlag>";
					strContents += "<TrxnDate>" + uRouteSchHeaderList.get(i).strTrxnDate + "</TrxnDate>";
					strContents += "<TrxnTime>" + uRouteSchHeaderList.get(i).strTrxnTime + "</TrxnTime>";
					strContents += "<Reference>" + "" + "</Reference>";
					strContents += "<TCOMStatus>" + "0" + "</TCOMStatus>";
					strContents += "<ChainNo>" + "0" + "</ChainNo>";
					strContents += "<CustNo>" + "0" + "</CustNo>";
					strContents += "<SaleDate>" + uRouteSchHeaderList.get(i).strTrxnDate + "</SaleDate>";
					strContents += "<PhysAvail>" + "0" + "</PhysAvail>";
					strContents += "<VirtAvail>" + "0" + "</VirtAvail>";
					strContents += "<Period>" + "0" + "</Period>";
					strContents += "<Trip>" + Global.gTripNumber + "</Trip>";
					if (bFirst) {
						if (uRouteSchHeaderList.get(i).strTrxnDate.length() > 0 && uRouteSchHeaderList.get(i).strTrxnTime.length() > 0) {
							strRouteSchFileName = "TripSchedule" + uRouteSchHeaderList.get(i).strTrxnDate + uRouteSchHeaderList.get(i).strTrxnTime + ".upl";
						}
						fileList.add(strRouteSchFileName);
						bFirst = false;
					}
					for (URouteSchBody body : uRouteSchHeaderList.get(i).body) {
						strContents += "<Detail>";
						strContents += "<TrxnNo>" + body.nTrxnNo + "</TrxnNo>";
						strContents += "<ChainNo>" + body.nChainNo + "</ChainNo>";
						strContents += "<CustNo>" + body.nCustNo + "</CustNo>";
						strContents += "<OrigSeqNo>" + (body.nOrgSeqNo + 1) + "</OrigSeqNo>";
						strContents += "<ChangeSeqNo>" + (body.nChangeSeqNo + 1) + "</ChangeSeqNo>";
						strContents += "<ETADate>" + body.strETADate + "</ETADate>";
						strContents += "<ETATime>" + body.strETATime + "</ETATime>";
						strContents += "</Detail>";
					}
					strContents += "</TripSchedule>";
				}
				strContents += "</TripSchedules>";
				writeStringAsFile(strContents, strUploadDir, strRouteSchFileName);
			}

			// write to CustomerFM.UPL
			if (uCustomerFMList.size() > 0) {
				String strCustomerContactFMName = "CustomerContactFM" + strSuffix + ".upl";
				String strCustomerInfoFMName = "CustomerInfoFM" + strSuffix + ".upl";
				String strCustomerFMFileName = "CustomerFM" + strSuffix + ".upl";

				boolean bFirst = true;
				strContents = "<CustomerFMs>";
				String strContactContents = "<CustomerContactFMs>";
				String strCustomerInfoContents = "<CustomerInfoFMs>";

				for (UCustomerFMInfo info : uCustomerFMList) {
					String mapKey = info.nCustNo + "/" + info.nChainNo;
					UBillingDetailsInfo uBillingDetailsInfo = mapBillingDetails.get(mapKey);
					List<ContactDetail> contactDetailList = mapContactDetails.get(mapKey);
					List<ContactInfo> custInfoList = mapCustomerDetails.get(mapKey);

					if (bFirst) {
						if (info.strTrxnDate.length() > 0 && info.strTrxnTime.length() > 0) {
							strCustomerFMFileName = "CustomerFM" + info.strTrxnDate + info.strTrxnTime + ".upl";
							strCustomerContactFMName = "CustomerContactFM" + info.strTrxnDate + info.strTrxnTime + ".upl";
							strCustomerInfoFMName = "CustomerInfoFM" + info.strTrxnDate + info.strTrxnTime + ".upl";
						}
						fileList.add(strCustomerFMFileName);
						fileList.add(strCustomerContactFMName);
						fileList.add(strCustomerInfoFMName);
						bFirst = false;
					}

					strContents += "<CustomerFM>";
					strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strContents += "<ChainNo>" + info.nChainNo + "</ChainNo>";
					strContents += "<CustNo>" + info.nCustNo + "</CustNo>";
					strContents += "<TemplateChainNo>" + info.nTemplateChainNo + "</TemplateChainNo>";
					strContents += "<TemplateCustNo>" + info.nTemplateCustNo + "</TemplateCustNo>";
					strContents += "<DocType>" + "CSFM" + "</DocType>";
					strContents += "<StartDate>" + info.strStartDate + "</StartDate>";
					strContents += "<StartTime>" + info.strStartTime + "</StartTime>";
					strContents += "<EndDate>" + info.strEndDate + "</EndDate>";
					strContents += "<EndTime>" + info.strEndTime + "</EndTime>";
					strContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
					strContents += "<TrxnTime>" + info.strTrxnTime + "</TrxnTime>";
					strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
					strContents += "<PrintedFlag>" + "0" + "</PrintedFlag>";
					strContents += "<Reference>" + "" + "</Reference>";
					strContents += "<TCOMStatus>" + "0" + "</TCOMStatus>";
					strContents += "<EditType>" + info.strEditType + "</EditType>";
					strContents += "<Name>" + ConvertHTM(info.strName) + "</Name>";
					strContents += "<Address1>" + ConvertHTM(info.strAddress1) + "</Address1>";
					strContents += "<Address2>" + ConvertHTM(info.strAddress2) + "</Address2>";
					strContents += "<City>" + ConvertHTM(info.strCity) + "</City>";
					strContents += "<ShipToState>" + ConvertHTM(info.strShipToState) + "</ShipToState>";
					strContents += "<ShipToZip>" + ConvertHTM(info.strShipToZip) + "</ShipToZip>";
					strContents += "<Phone>" + info.strPhone + "</Phone>";
					strContents += "<TaxID>" + info.strCompanyTaxID + "</TaxID>";
					strContents += "<DeliveryWindowFrom>" + info.strDeliveryWindowFrom + "</DeliveryWindowFrom>";
					strContents += "<DeliveryWindowTo>" + info.strDeliveryWindowTo + "</DeliveryWindowTo>";
					strContents += "<StripeInfo>" + info.strStripeInfo + "</StripeInfo>";
					strContents += "<Type>" + info.strType + "</Type>";
					strContents += "<UserCode1>" + info.strUser1 + "</UserCode1>";
					if(uBillingDetailsInfo != null)
					{
						strContents += "<BillToName>" + uBillingDetailsInfo.strBillingName + "</BillToName>";
						strContents += "<BillToAddress1>" + uBillingDetailsInfo.strAddress1 + "</BillToAddress1>";
						strContents += "<BillToAddress2>" + uBillingDetailsInfo.strAddress2 + "</BillToAddress2>";
						strContents += "<BillToCity>" + uBillingDetailsInfo.strCity + "</BillToCity>";
						strContents += "<BillToState>" + uBillingDetailsInfo.strState + "</BillToState>";
						strContents += "<BillToZip>" + uBillingDetailsInfo.strPostCode + "</BillToZip>";
						strContents += "<LegalName>" + uBillingDetailsInfo.strLegalName + "</LegalName>";
						strContents += "<CreditLimit>" + uBillingDetailsInfo.strCreditLimit + "</CreditLimit>";
					}
					else
					{
						strContents += "<BillToName>" + "</BillToName>";
						strContents += "<BillToAddress1>" + "</BillToAddress1>";
						strContents += "<BillToAddress2>" + "</BillToAddress2>";
						strContents += "<BillToCity>" + "</BillToCity>";
						strContents += "<BillToState>" + "</BillToState>";
						strContents += "<BillToZip>" + "</BillToZip>";
						strContents += "<LegalName>" + "</LegalName>";
						strContents += "<CreditLimit>" + "</CreditLimit>";
					}
//					String strTypes[] = info.strContactType.split(Global.CONTACT_SPLITER);
//					String strNames[] = info.strContactName.split(Global.CONTACT_SPLITER);
//					String strPhones[] = info.strContactPhoneNumber.split(Global.CONTACT_SPLITER);
//					String strEmails[] = info.strContactEmailAddress.split(Global.CONTACT_SPLITER);
//					for (int i = 0; i < 6; i++) {
//						strContents += "<ContactType" + (i + 1) + ">" + (i < strTypes.length ? ConvertHTM(strTypes[i]) : "") + "</ContactType" + (i + 1) + ">";
//						strContents += "<ContactName" + (i + 1) + ">" + (i < strNames.length ? ConvertHTM(strNames[i]) : "") + "</ContactName" + (i + 1) + ">";
//						strContents += "<ContactPhoneNumber" + (i + 1) + ">" + (i < strPhones.length ? ConvertHTM(strPhones[i]) : "") + "</ContactPhoneNumber" + (i + 1) + ">";
//						strContents += "<ContactEmailAddress" + (i + 1) + ">" + (i < strEmails.length ? ConvertHTM(strEmails[i]) : "") + "</ContactEmailAddress" + (i + 1) + ">";
//					}
					strContents += "</CustomerFM>";

					strContactContents += "<CustomerContactFM>";
					strContactContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strContactContents += "<DocType>" + "CCFM" + "</DocType>";
					strContactContents += "<ChainNo>" + info.nChainNo + "</ChainNo>";
					strContactContents += "<CustNo>" + info.nCustNo + "</CustNo>";
					strContactContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
					strContactContents += "<TrxnTime>" + info.strTrxnTime + "</TrxnTime>";
					strContactContents += "<SaleDate>" + info.strTrxnDate + "</SaleDate>";
					strContactContents += "<Reference>" + "" + "</Reference>";
					strContactContents += "<Trip>" + Global.gTripNumber + "</Trip>";
					strContactContents += "<ContactDetail>";
					for(ContactDetail contactDetail : contactDetailList)
					{
						strContactContents += "<EditType>" + info.strEditType + "</EditType>";
						strContactContents += "<ContactTypeID>" + contactDetail.strContactTypeID + "</ContactTypeID>";
						strContactContents += "<ContactType>" + contactDetail.nContactTypeNumericKey + "</ContactType>";
						strContactContents += "<ContactID>" + "" + "</ContactID>";
						strContactContents += "<ContactName>" + contactDetail.strContactName + "</ContactName>";
						strContactContents += "<ContactPhoneNumber>" + contactDetail.strContactPhone1 + "</ContactPhoneNumber>";
						strContactContents += "<ContactPhoneNumber2>" + contactDetail.strContactPhone2 + "</ContactPhoneNumber2>";
						strContactContents += "<ContactPhoneNumber3>" + contactDetail.strContactPhone3 + "</ContactPhoneNumber3>";
						strContactContents += "<ContactEmailAddress>" + contactDetail.strContactEmail1 + "</ContactEmailAddress>";
						strContactContents += "<ContactEmailAddress2>" + contactDetail.strContactEmail2 + "</ContactEmailAddress2>";
						strContactContents += "<ContactEmailAddress3>" + contactDetail.strContactEmail3 + "</ContactEmailAddress3>";

						int nSize = contactDetail.contactInfoList.size();
						for(int i = 0; i < 4; i++)
						{
							ContactInfo contactInfo = null;
							if(i < nSize)
							{
								contactInfo = contactDetail.contactInfoList.get(i);
								String tag = contactInfo.strContactInfoDescription.replace(" ", "");
								if(i == 0)
								{
									strContactContents += "<ContactInfo>" + contactInfo.strContactInfoValue +  "</ContactInfo>";
								}
								else
								{
									strContactContents += "<ContactInfo" + (i + 1) + ">" + contactInfo.strContactInfoValue +  "</ContactInfo" + (i + 1) + ">";
								}
							}
							else
							{
								if(i == 0)
								{
									strContactContents += "<ContactInfo>" + "</ContactInfo>";
								}
								else
								{
									strContactContents += "<ContactInfo" + (i + 1) + ">" + "</ContactInfo" + (i + 1) + ">";
								}
							}
						}
					}
					strContactContents += "</ContactDetail>";
					strContactContents += "</CustomerContactFM>";

					strCustomerInfoContents += "<CustomerInfoFM>";
					strCustomerInfoContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strCustomerInfoContents += "<DocType>" + "CIFM" + "</DocType>";
					strCustomerInfoContents += "<ChainNo>" + info.nChainNo + "</ChainNo>";
					strCustomerInfoContents += "<CustNo>" + info.nCustNo + "</CustNo>";
					strCustomerInfoContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
					strCustomerInfoContents += "<TrxnTime>" + info.strTrxnTime + "</TrxnTime>";
					strCustomerInfoContents += "<SaleDate>" + info.strTrxnDate + "</SaleDate>";
					strCustomerInfoContents += "<Reference>" + "" + "</Reference>";
					strCustomerInfoContents += "<Trip>" + Global.gTripNumber + "</Trip>";
					for(ContactInfo contactInfo : custInfoList)
					{
						strCustomerInfoContents += "<InfoDetail>";
						strCustomerInfoContents += "<EditType>" + info.strEditType + "</EditType>";
						strCustomerInfoContents += "<InfoTypeID>" + contactInfo.strContactInfoEntryKey + "</InfoTypeID>";
						strCustomerInfoContents += "<InfoType>" + contactInfo.nContactInfoNumericKey + "</InfoType>";
						strCustomerInfoContents += "<InfoDetail>" + contactInfo.strContactInfoValue + "</InfoDetail>";
						strCustomerInfoContents += "</InfoDetail>";
					}
					strCustomerInfoContents += "</CustomerInfoFM>";

				}
				strContents += "</CustomerFMs>";
				strContactContents += "</CustomerContactFMs>";
				strCustomerInfoContents += "</CustomerInfoFMs>";
				writeStringAsFile(strContents, strUploadDir, strCustomerFMFileName);
				writeStringAsFile(strContactContents, strUploadDir, strCustomerContactFMName);
				writeStringAsFile(strCustomerInfoContents, strUploadDir, strCustomerInfoFMName);
			}

			// write to Assets.upl
			if (uAssetInfoList.size() > 0) {
				String strTransactionsFileName = "Assets" + strSuffix + ".upl";
//				fileList.add(strTransactionsFileName);
				boolean bFirst = true;
				strContents = "<Assets>";
				for (UAssetInfo info : uAssetInfoList) {
					if (info.strEquipmentActType.equals("V") || info.strEquipmentActType.equals("R")
							|| info.strEquipmentActType.equals("N") || info.strEquipmentActType.equals("A")) {
						SimpleDateFormat sdf = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT);
						SimpleDateFormat sdf1 = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT);
						String trxnDate = sdf.format(new Date(info.nTrxnNo));
						String trxnTime = sdf1.format(new Date(info.nTrxnNo));
						if (bFirst) {

							if (trxnDate.length() > 0 && trxnTime.length() > 0) {
								strTransactionsFileName = "Assets" + trxnDate + trxnTime + ".upl";
							}
							fileList.add(strTransactionsFileName);
							bFirst = false;
						}

						strContents += "<Asset>";
						strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
						strContents += "<DocNo>" + info.strDocNo + "</DocNo>";
						strContents += "<DocType>" + "AST" + info.strEquipmentActType + "</DocType>";
						strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
						strContents += "<PrintedFlag>" + "0" + "</PrintedFlag>";
						strContents += "<TrxnDate>" + trxnDate + "</TrxnDate>";
						strContents += "<TrxnTime>" + trxnTime + "</TrxnTime>";
						strContents += "<Reference>" + "" + "</Reference>";
						strContents += "<TCOMStatus>" + "0" + "</TCOMStatus>";
						strContents += "<SaleDate>" + trxnDate + "</SaleDate>";
						strContents += "<ChainNo>" + info.nChainNo + "</ChainNo>";
						strContents += "<CustNo>" + info.nCustNo + "</CustNo>";
						strContents += "<EquipmentNo>" + info.strEquipmentNo + "</EquipmentNo>";
						strContents += "<SerialNo>" + info.strSerialNo + "</SerialNo>";
						strContents += "<EquipmentType>" + info.strEquipmentType + "</EquipmentType>";
						strContents += "<RequestDate>" + info.strRequestDate + "</RequestDate>";
						strContents += "<Reason>" + "" + "</Reason>";
						strContents += "<Features>" + "" + "</Features>";
						strContents += "<Response>" + info.strResponse + "</Response>";
						strContents += "<Make>" + info.strMake + "</Make>";
						strContents += "<Model>" + info.strModel + "</Model>";
						strContents += "<Application>" + info.strApplication + "</Application>";
						strContents += "<StatusCode>" + info.strStatusCode + "</StatusCode>";
						strContents += "<AssetType>" + info.strAssetType + "</AssetType>";
						strContents += "<RepairReason>" + info.strRepairReason + "</RepairReason>";
						strContents += "<RepairNotes>" + info.strRepairNotes + "</RepairNotes>";
						strContents += "<AltEquipment>" + info.strAltEquipment + "</AltEquipment>";
						strContents += "</Asset>";
					}

				}
				strContents += "</Assets>";
				writeStringAsFile(strContents, strUploadDir, strTransactionsFileName);
			}

			// write to CustNote.upl
			if (uCustomerNoteInfoList.size() > 0) {
				String strTransactionsFileName = "CustNote" + strSuffix + ".upl";
//				fileList.add(strTransactionsFileName);
				boolean bFirst = true;
				strContents = "<CustNotes>";
				for (UCustomerNoteInfo info : uCustomerNoteInfoList) {
					SimpleDateFormat sdf = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT);
					SimpleDateFormat sdf1 = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT);
					String trxnDate = sdf.format(new Date(info.nTrxnNo));
					String trxnTime = sdf1.format(new Date(info.nTrxnNo));
					if (bFirst) {

						if (trxnDate.length() > 0 && trxnTime.length() > 0) {
							strTransactionsFileName = "CustNote" + trxnDate + trxnTime + ".upl";
						}
						fileList.add(strTransactionsFileName);
						bFirst = false;
					}

					strContents += "<CustNote>";
					strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strContents += "<DocNo>" + info.strDocNo + "</DocNo>";
					strContents += "<DocType>" + info.strDocType + "</DocType>";
					strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
					strContents += "<PrintedFlag>" + "0" + "</PrintedFlag>";
					strContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
					strContents += "<TrxnTime>" + info.strTrxnTime + "</TrxnTime>";
					strContents += "<Reference>" + info.strReference + "</Reference>";
					strContents += "<TCOMStatus>" + "0" + "</TCOMStatus>";
					strContents += "<SaleDate>" + info.strSalesDate + "</SaleDate>";
					strContents += "<ChainNo>" + info.nChainNo + "</ChainNo>";
					strContents += "<CustNo>" + info.nCustNo + "</CustNo>";
					strContents += "<PhysAvail>" + "0" + "</PhysAvail>";
					strContents += "<VirtAvail>" + "0" + "</VirtAvail>";
					strContents += "<Period>" + "0" + "</Period>";
					strContents += "<Trip>" + Global.gTripNumber + "</Trip>";
					strContents += "<TransactionType>" + info.strTransactionType + "</TransactionType>";
					strContents += "<User>" + info.strUser + "</User>";
					strContents += "<NoteType>" + info.strNoteType + "</NoteType>";
					strContents += "<Message>" + info.strMessage + "</Message>";
					if (info.nAttachment > 0) {
						for (int i = 0; i < info.uAttachmentInfoList.size(); i++) {
							strContents += "<Attachment>";
							strContents += "<TrxnNo>" + info.uAttachmentInfoList.get(i).nTrxnNo + "</TrxnNo>";
							strContents += "<FileName>" + info.uAttachmentInfoList.get(i).strFileName + "</FileName>";
							strContents += "</Attachment>";

							String strLocalFilePath = info.uAttachmentInfoList.get(i).strFilePath;
							String strServerFilePath = String.format(Locale.ENGLISH, Global.FTP_UPLOAD_PATH, strFTPCompanyName, Global.gRouteNumber, info.uAttachmentInfoList.get(i).strFileName);
							Utils.registerUploadData(mContext, strLocalFilePath, strServerFilePath);
						}
					}
					strContents += "</CustNote>";
				}
				strContents += "</CustNotes>";
				writeStringAsFile(strContents, strUploadDir, strTransactionsFileName);
			}

			// write to Visit.upl
			if (uVisitInfoList.size() > 0) {
				String strTransactionsFileName = "Visit" + strSuffix + ".upl";
				boolean bFirst = true;
				strContents = "<Visits>";
				for (UVisitInfo info : uVisitInfoList) {
					if (bFirst) {
						if (info.strTrxnDate.length() > 0 && info.strTrxnTime.length() > 0) {
							strTransactionsFileName = "Visit" + info.strTrxnDate + info.strTrxnTime + ".upl";
						}
						fileList.add(strTransactionsFileName);
						bFirst = false;
					}

					strContents += "<Visit>";
					strContents += "<TrxnNo>" + info.nTrxnNo + "</TrxnNo>";
					strContents += "<DocType>" + info.strDocType + "</DocType>";
					strContents += "<ChainNo>" + info.nChainNo + "</ChainNo>";
					strContents += "<CustNo>" + info.nCustNo + "</CustNo>";
					strContents += "<TrxnDate>" + info.strTrxnDate + "</TrxnDate>";
					strContents += "<TrxnTime>" + info.strTrxnTime + "</TrxnTime>";
					strContents += "<VoidFlag>" + info.nVoidFlag + "</VoidFlag>";
					strContents += "<PrintedFlag>" + info.nPrintedFlag + "</PrintedFlag>";
					strContents += "<Reference>" + info.strReference + "</Reference>";
					strContents += "<TCOMStatus>" + info.strTCOMStatus + "</TCOMStatus>";
					strContents += "<SaleDate>" + info.strSaleDate + "</SaleDate>";
					strContents += "<VisitStart>" + info.strVisitStart + "</VisitStart>";
					strContents += "<VisitEnd>" + info.strVisitEnd + "</VisitEnd>";
					strContents += "<VisitReason>" + info.strVisitReason + "</VisitReason>";
					strContents += "<VisitReasonID>" + info.strVisitReasonID + "</VisitReasonID>";
					strContents += "<NextVisit>" + info.strNextVisit + "</NextVisit>";
					strContents += "<NextVisitStart>" + info.strNextVisitStart + "</NextVisitStart>";
					strContents += "<NextVisitEnd>" + info.strNextVisitEnd + "</NextVisitEnd>";
					strContents += "<VisitMessage>" + info.strVisitMessage + "</VisitMessage>";
					strContents += "<CustomerUpdated>" + info.nCustomerUpdated + "</CustomerUpdated>";
					strContents += "<RangeChecked>" + info.nRangeChecked + "</RangeChecked>";
					strContents += "<OrderCreated>" + info.nOrderCreated + "</OrderCreated>";
					strContents += "<SurveyCompleted>" + info.nSurveyCompleted + "</SurveyCompleted>";
					strContents += "<AssetChecked>" + info.nAssetChecked + "</AssetChecked>";
					strContents += "<AssetRequested>" + info.nAssetRequested + "</AssetRequested>";
					strContents += "<VisitFrequency>" + info.nVisitFrequency + "</VisitFrequency>";
					strContents += "<PreferredVisitDay>" + info.nPreferredVisitDay + "</PreferredVisitDay>";
					strContents += "<PlannedVisitTime>" + info.strPlannedVisitTime + "</PlannedVisitTime>";
					strContents += "<VisitType>" + info.nVisitType + "</VisitType>";
					strContents += "<VisitTypeID>" + info.strVisitTypeId + "</VisitTypeID>";
					strContents += "</Visit>";
				}
				strContents += "</Visits>";
				writeStringAsFile(strContents, strUploadDir, strTransactionsFileName);
			}

			//write to UVersion.upl
			if (Global.gIsEndTrip == true) {
				String strVersionFileName = "Version" + strSuffix + ".upl";
				fileList.add(strVersionFileName);
				String strAppVersion = "";
				PackageManager manager = mContext.getPackageManager();
				PackageInfo info;
				try {
					info = manager.getPackageInfo(mContext.getPackageName(), 0);
					strAppVersion = info.versionName;
				} catch (Exception e) {
					strAppVersion = "0.0.0";
					e.printStackTrace();
				}

				String strAndroidVersion = android.os.Build.VERSION.RELEASE;

				String strBuildNumber = String.valueOf(BuildConfig.VERSION_CODE);
				String strDeviceModel = Utils.getStringSetting(mContext, Global.DEVICE_MODEL);
				strContents = "<Versions>";
				strContents += "<Version><Assembly>DSDVersion</Assembly><Version>" + strAppVersion + "</Version></Version>";
				strContents += "<Version><Assembly>AndroidVersion</Assembly><Version>" + strAndroidVersion + "</Version></Version>";
				strContents += "<Version><Assembly>DeviceModelNumber</Assembly><Version>" + strDeviceModel + "</Version></Version>";
				strContents += "<Version><Assembly>BuildNumber</Assembly><Version>" + strBuildNumber + "</Version></Version>";
				strContents += "</Versions>";
				writeStringAsFile(strContents, strUploadDir, strVersionFileName);
			}

			if (fileList.size() > 0) {
//				strSuffix = fileList.get(0).strTrxnDate + fileList.get(0).strTrxnTime;
				strSuffix = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
				String strZipFileName = "upload" + strSuffix + ".zip";
				int nIdx = 1;
				while(Utils.isFileExist(strZipFileName))
				{
					strZipFileName = "upload" + strSuffix + "(" + nIdx + ").zip";
				}
				makeZipFile(mContext, fileList, strZipFileName);

				//String strLocalFilePath = String.format(Locale.ENGLISH, "%s%s/%s", Global.UPLOAD_DIR, Global.gRouteNumber, strZipFileName);
				String strLocalFilePath = Global.UPLOAD_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + strZipFileName;
				String strServerFilePath = String.format(Locale.ENGLISH, Global.FTP_UPLOAD_PATH, strFTPCompanyName, Global.gRouteNumber, strZipFileName);
				if (!TextUtils.isEmpty(Global.gRouteNumber)) {
					Utils.registerUploadData(mContext, strLocalFilePath, strServerFilePath);

					for (UCamerasInfo info : uCamerasList) {
						strLocalFilePath = Global.PHOTO_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + info.strReference;
						if (info.strDocType.equals("MAIT") == true) {
							strServerFilePath = String.format(Locale.ENGLISH, Global.FTP_CUSTOMER_PHOTO_PATH, strFTPCompanyName, info.strReference);
						} else {
							strServerFilePath = String.format(Locale.ENGLISH, Global.FTP_UPLOAD_PATH, strFTPCompanyName, Global.gRouteNumber, info.strReference);
						}
						Utils.registerUploadData(mContext, strLocalFilePath, strServerFilePath);
					}
					for (UTransactionsInfo info : uTransactionsList) {
						if (info.strDocType.equals("FARC") == true) {
							strLocalFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + info.strReference;
							strServerFilePath = String.format(Locale.ENGLISH, Global.FTP_UPLOAD_PATH, strFTPCompanyName, Global.gRouteNumber, info.strReference);
							Utils.registerUploadData(mContext, strLocalFilePath, strServerFilePath);
						}
					}
				}

				dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				if (zipFileInfo == null) {
					dbAdapter.deleteAllUploadData(nCustNo);
				} else {
					dbAdapter.deleteAllUploadData(nCustNo, zipFileInfo);
				}
				for (URouteSchHeader headerInfo : uRouteSchHeaderList) {
					dbAdapter.deleteURouteSchsHeader(headerInfo.nTrxnNo);
					dbAdapter.deleteURouteSchsBody(headerInfo.nTrxnNo);
				}
				for (UOrderHInfo info : uOrderHList) {
					info.bUploaded = true;
					dbAdapter.updateUOrderHInfo(info);
				}
				dbAdapter.close();
			}


//			Utils.deleteDirectory(Global.UPLOAD_DIR);
			if (fileInfo != null) {
				Utils.unregisterUploadFile(mContext, fileInfo);
			}

			//Download Sepc Files from the server
			if (bNeedDownload == false) {
				return true;
			}

			if (Utils.getIntSetting(mContext, Global.PREF_LOADMULTIALL) == 0) {
				return true;
			}

//			new Thread()
//			{
//				@Override
//				public void run() {
//					if(!bDownloading)
//					{
//						bDownloading = true;
//
//						if (!Utils.isConnectingToInternet(mContext)) {
//							bDownloading = false;
//							return;
//						}
//
//						FTPClient ftpClient = Utils.connnectingwithFTP(strFTPServerIP, Global.DEFAULT_FTP_PORT, strFTPUserName, strFTPPassword);
//						if (ftpClient == null) {
//							bDownloading = false;
//							return;
//						}
//
//						String strParentDir = String.format(Locale.ENGLISH, Global.FTP_SPECIAL_DIR, strFTPCompanyName);
//						String strFileName = String.format(Locale.ENGLISH, "%s.zip", Global.gTripNumber);
//						if (Utils.findFileOnFTPServer(ftpClient, strParentDir, strFileName) == 0) {
//							Utils.printDebug(null, "Can't find the matched trip data.");
//							bDownloading = false;
//							return;
//						}
//						Utils.deleteDirectory(Global.DOWNLOAD_DIR);
//						if (Utils.createDirectory(Global.DOWNLOAD_DIR) == false) {
//							Utils.printDebug(null, "Can't create the download directory on SD Card.");
//							bDownloading = false;
//							return;
//						}
//						String strServerPath = String.format(Locale.ENGLISH, Global.FTP_SPECIAL_PATH, strFTPCompanyName, Global.gTripNumber);
//						String strLocalPath = Global.DOWNLOAD_DIR + strFileName;
//						if (Utils.downloadFileFromFTPServer(ftpClient, strServerPath, strLocalPath) == false) {
//							Utils.printDebug(null, "Can't download the territory data.");
//							bDownloading = false;
//							return;
//						}
//						if (Utils.unzipData() == false) {
//							Utils.printDebug(null, "Can't extract the data.");
//							bDownloading = false;
//							return;
//						}
//						if (ParseXMLFile.fillDataToDatabase(mContext, false) == 1) {
//							Utils.printDebug(null, "Can't fill the data to database.");
//							bDownloading = false;
//							return;
//						}
//					}
//				}
//			}.start();


			bRes = true;
		} catch (Exception e) {
			if (fileInfo != null) {
				Utils.addEndTripExceptionStackToFileIfNeeded(mContext, Global.UPLOAD_ERROR_FILENAME, e, String.valueOf(fileInfo.idx));
			}
			else {
				Utils.addEndTripExceptionStackToFileIfNeeded(mContext, Global.UPLOAD_ERROR_FILENAME, e, "none");
			}

			e.printStackTrace();
		} finally {
			try {
				if (ftpClient != null) {
					ftpClient.disconnect();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bRes;
	}

	public static boolean bDownloading = false;

	public static MakeZipFileInfo __makeZipFileInfo = null;

	public static void AddTrxnNo(int nCustNo, int nChainNo, long nTrxnNo) {
		if (__makeZipFileInfo == null) {
			__makeZipFileInfo = new MakeZipFileInfo();
			__makeZipFileInfo.nCustNo = nCustNo;
			__makeZipFileInfo.nChainNo = nChainNo;
		}
		boolean bAdd = true;
		for (Long trxnNo : __makeZipFileInfo.trxnList) {
			if (trxnNo == nTrxnNo) {
				bAdd = false;
				break;
			}
		}
		if (bAdd) {
			__makeZipFileInfo.trxnList.add(nTrxnNo);
		}
	}

	public static long setGeneralUploadData(Context context, String strDocType, int nChainNo, int nCustNo, long nTrxnNo, String strReference) {
//		double nLat = Utils.getDoubleSetting(context, Global.PREF_MY_LATITUDE);
//		double nLng = Utils.getDoubleSetting(context, Global.PREF_MY_LONGITUDE);
		Date mCurDate = new Date();
		long m_lTrxnNo;
		if (nTrxnNo != -1)
			m_lTrxnNo = nTrxnNo;
		else
			m_lTrxnNo = System.currentTimeMillis();

		UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
		uTransactionInfo.nTrxnNo = m_lTrxnNo;
		uTransactionInfo.strDocType = strDocType;
		uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(context, mCurDate));
		uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
		uTransactionInfo.nChainNo = nChainNo;
		uTransactionInfo.strReference = strReference;
		uTransactionInfo.nCustNo = nCustNo;

/*		UGPSInfo uGPSInfo = new UGPSInfo();
		uGPSInfo.nTrxnNo = m_lTrxnNo;
		uGPSInfo.nChainNo = nChainNo;
		uGPSInfo.nCustNo = nCustNo;
		uGPSInfo.nStartTrxnNo = uTransactionInfo.nTrxnNo;
		if (nLat != Global.DEFAULT_FLOAT && nLng != Global.DEFAULT_FLOAT) {
			uGPSInfo.strGPSData = String.format(Locale.ENGLISH, "%f, %f", nLat, nLng);
		} else {
			uGPSInfo.strGPSData = "ERROR: NO GPS";
		}
		if(strDocType.equals("MAIT"))
			uGPSInfo.strDocType = "MAIT";
		else
			uGPSInfo.strDocType = "GPS";
		uGPSInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mCurDate);
		uGPSInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);*/

		uTransactionInfo.strTripId = Utils.getStringSetting(context, Global.PREF_TRIP_ID);
		uTransactionInfo.strDriverId = "";
		uTransactionInfo.strDriverName = Utils.getStringSetting(context, Global.PREF_USER_DESCRIPTION);
		uTransactionInfo.strDriverNumber = Utils.getStringSetting(context, Global.PREF_DRIVER_NUMBER);

		DbAdapter dbAdapter = new DbAdapter(context);
		dbAdapter.open();
		dbAdapter.insertUTransactionsInfo(uTransactionInfo);
//		dbAdapter.insertUGPSInfo(uGPSInfo);
		dbAdapter.close();

		AddTrxnNo(nCustNo, nChainNo, m_lTrxnNo);

		return m_lTrxnNo;
	}

	public static boolean downloadData(Context mContext) {
		boolean bRes = false;
		String strFTPServerIP = Utils.getStringSetting(mContext, Global.PREF_FTP_SERVER_IP);
		String strFTPCompanyName = Utils.getStringSetting(mContext, Global.PREF_FTP_COMPANY_NAME);
		String strFTPUserName = Utils.getStringSetting(mContext, Global.PREF_FTP_USERNAME);
		String strFTPPassword = Utils.getStringSetting(mContext, Global.PREF_FTP_PASSWORD);
		FTPClient ftpClient = null;
		boolean isFTPPossible = isPossibleDownloadFileViaFTP(mContext);
		try {
			if (isFTPPossible) {
				ftpClient = Utils.connnectingwithFTP(strFTPServerIP, Global.DEFAULT_FTP_PORT, strFTPUserName, strFTPPassword);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		// Download Customer Photo
		if (Utils.createDirectory(Global.CUSTOMER_PHOTO_DIR) == false) {
			Utils.printDebug(null, "Can't create the customer photo directory on SD Card.");
			return false;
		}

		if(!Utils.downloadCustomerPhotoFromHttpServer(mContext, ftpClient))
		{
			if(isFTPPossible)
			{
				try
				{
					if (Utils.downloadCustomerPhotoFromFTPServer(ftpClient, strFTPCompanyName) == false) {
						Utils.printDebug(null, "Can't download the product image.");
						if(ftpClient != null)
						{
							ftpClient.disconnect();
						}
					}
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				Utils.printDebug(null, "Can't download the product image.");
				return false;
			}
		}

		if (Utils.createDirectory(Global.ROOT_EQUIPMENT_CATALOG_LOCATION) == false) {
			Utils.printDebug(null, "Can't create the customer photo directory on SD Card.");
			return false;
		}

		if(!Utils.downloadEquipmentCatalogFromHttpServer(mContext, ftpClient))
		{
			if(isFTPPossible)
			{
				try
				{
					if (Utils.downloadEquipmentCatalogFromFTPServer(ftpClient, strFTPCompanyName) == false) {
						Utils.printDebug(null, "Can't download the equipment catalog image.");
						if(ftpClient != null)
						{
							ftpClient.disconnect();
						}
					}
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				Utils.printDebug(null, "Can't download the product image.");
				return false;
			}
		}

		// Download Company Logo
		/*String strParentDir = String.format(Locale.ENGLISH, Global.FTP_REPORTS_DIR, strFTPCompanyName);
		String strFileName = Global.COMPANY_LOGO_FILENAME;
		long nRemoteSize = Utils.findFileOnFTPServer(ftpClient, strParentDir, strFileName);
		if (nRemoteSize == 0) {
			Utils.printDebug(null, "Can't find the Company Logo file.");
			Global.gNeedToDownload = false;
			return false;
		}
		long nLocalSize = 0;
		if (Utils.isFileExist(Global.DEFAULT_COMPANY_LOGO_FILEPATH) == true) {
			nLocalSize = new File(Global.DEFAULT_COMPANY_LOGO_FILEPATH).length();
		}
		if (nLocalSize == 0 || nRemoteSize != nLocalSize) {
			String strServerPath = strParentDir + Global.COMPANY_LOGO_FILENAME;
			String strLocalPath = Global.DEFAULT_COMPANY_LOGO_FILEPATH;
			Utils.createDirectory(Global.LOGO_DIR);
			if (Utils.downloadFileFromFTPServer(ftpClient, strServerPath, strLocalPath) == false) {
				Utils.printDebug(null, "Can't download the Company Logo file.");
				return false;
			}
		}*/

		Global.gNeedToDownload = false;
		bRes = true;

		return bRes;
	}

	private static void writeStringAsFile(String strContents, String strDirectory, String strFileName) {
		try {
			FileWriter out = new FileWriter(new File(strDirectory + strFileName));
			out.write(strContents);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void makeZipFile(Context mContext, List<String> fileList, String strZipFileName) {
		try {
			BufferedInputStream origin;
			//String strZipFilePath = String.format(Locale.ENGLISH, "%s%s/%s", Global.UPLOAD_DIR, Global.gRouteNumber, strZipFileName);
			String strZipFilePath = Global.UPLOAD_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + strZipFileName;
			FileOutputStream dest = new FileOutputStream(strZipFilePath);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
			int BUFFER = 1024;
			byte data[] = new byte[BUFFER];
			for (int i = 0; i < fileList.size(); i++) {
				if (!fileList.get(i).endsWith(".upl")) continue;
				Utils.printDebug(null, "Adding: " + fileList.get(i));
				//FileInputStream fi = new FileInputStream(String.format(Locale.ENGLISH, "%s%s/%s", Global.UPLOAD_DIR, Global.gRouteNumber, fileList.get(i)));
				String strPath = Global.UPLOAD_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + fileList.get(i);

				FileInputStream fi = new FileInputStream(strPath);
				origin = new BufferedInputStream(fi, BUFFER);
				ZipEntry entry = new ZipEntry(fileList.get(i));
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();

				//File file = new File(String.format(Locale.ENGLISH, "%s%s/%s", Global.UPLOAD_DIR, Global.gRouteNumber, fileList.get(i)));
				File file = new File(strPath);
				file.delete();
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getUOrderDContent(Context context, UOrderDInfo uOrderDInfo) {
		String strContents = "";
		if (getProdLoconSCWType(context, uOrderDInfo.strItemNo) == Global.WEIGHT_ITEM) {
			strContents += "<Detail>";
			strContents += "<TrxnNo>" + uOrderDInfo.nTrxnNo + "</TrxnNo>";
			strContents += "<TrxnType>" + uOrderDInfo.nTrxnType + "</TrxnType>";
			strContents += "<LocnNo>" + uOrderDInfo.nLocnNo + "</LocnNo>";
			strContents += "<ItemNo>" + uOrderDInfo.strItemNo + "</ItemNo>";
			if (uOrderDInfo.nPriceOverride == 0) {
				strContents += "<PriceOverride>" + "0" + "</PriceOverride>";
			} else {
				strContents += "<PriceOverride>" + Utils.getMultipliedString(uOrderDInfo.nPrice, context) + "</PriceOverride>";
			}
			//strContents += "<CustomerRetailPrice>" + Utils.getMultipliedString(uOrderDInfo.nCustomerRetailPrice) + "</CustomerRetailPrice>";
			//strContents += "<Quantity>" + Utils.getMultipliedString(uOrderDInfo.dQuantity) + "</Quantity>";
			//strContents += "<Quantity>" + Utils.getMultipliedString(uOrderDInfo.dQuantity, context) + "</Quantity>";
			strContents += "<Quantity>" + 100000 + "</Quantity>"; //VS146
			strContents += "<Dumps>" + Utils.getMultipliedString(uOrderDInfo.dDumps, context) + "</Dumps>";
			strContents += "<Sales>" + Utils.getMultipliedString(uOrderDInfo.dSales, context) + "</Sales>";
			strContents += "<GrossPrice>" + Utils.getMultipliedString(uOrderDInfo.nOrgGrossPrice, context) + "</GrossPrice>";
			strContents += "<TotalAllowance>" + Utils.getMultipliedString(uOrderDInfo.nOrgGrossPrice - uOrderDInfo.nOrgPrice, context) + "</TotalAllowance>";
			strContents += "<RetailPrice>" + Utils.getMultipliedString(uOrderDInfo.nCustomerRetailPrice, context) + "</RetailPrice>";
			strContents += "<RetailOverride>" + "0" + "</RetailOverride>";

			int nReasonCode;
			if (uOrderDInfo.nSCWType != Global.INVENTORY_CONTAINERS) {
				nReasonCode = uOrderDInfo.nReasonCode;
			} else {
				nReasonCode = 0;
			}

			strContents += "<ReasonCode>" + nReasonCode + "</ReasonCode>";
			strContents += "<WeightedItem>" + "4" + "</WeightedItem>";
			strContents += "<TaxAmount>" + Utils.getMultipliedString(uOrderDInfo.nTaxAmount, context) + "</TaxAmount>";
			strContents += "<PresoldQuantity>" + Utils.getMultipliedString(uOrderDInfo.dPlanQty, context) + "</PresoldQuantity>";
			strContents += "<PrintOnOrder>" + "" + "</PrintOnOrder>";
			strContents += "<IsKitItem>" + "N" + "</IsKitItem>";
			if(Utils.getStringSetting(context, Global.PREF_INVCALC).equals("TTx"))
			{
				strContents += "<ExtendAmount>" + Utils.getMultipliedString(uOrderDInfo.nOrgGrossPrice * uOrderDInfo.dQuantity, context) + "</ExtendAmount>";
			}
			else
			{
				strContents += "<ExtendAmount>" + Utils.getMultipliedString(uOrderDInfo.nPrice * uOrderDInfo.dQuantity, context) + "</ExtendAmount>";
			}
			strContents += "<User1>" + "" + "</User1>";
			strContents += "<User2>" + "" + "</User2>";
			strContents += "<TotalWeight>" + Utils.getMultipliedString(uOrderDInfo.dQuantity, context) + "</TotalWeight>";
			String strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(context, new Date(uOrderDInfo.nTrxnNo)));
			strContents += "<ForecastStartDate>" + strTrxnDate + "</ForecastStartDate>";
			strContents += "<ForecastEndDate>" + strTrxnDate + "</ForecastEndDate>";
			strContents += "<OrigForecastQty>" + "0" + "</OrigForecastQty>";
			strContents += "<PriceDiscFlag>" + "0" + "</PriceDiscFlag>";
			strContents += "<RetailPriceFlag>" + "0" + "</RetailPriceFlag>";
			strContents += "<DiscRateEntered>" + "0" + "</DiscRateEntered>";
			strContents += "<User3>" + "" + "</User3>";
			strContents += "<CustPrice>" + Utils.getMultipliedString(uOrderDInfo.nPrice, context) + "</CustPrice>";
			strContents += "<OffInvPromoAmtDtl>" + "0" + "</OffInvPromoAmtDtl>";
			strContents += "<TotalAllowExtended>" + "0" + "</TotalAllowExtended>";
			strContents += "<QuantityCompl>" + "0" + "</QuantityCompl>";
			strContents += "<CaseFactor>" + uOrderDInfo.nCaseFactor + "</CaseFactor>";
			strContents += "<DiscInLieuAmtDtl>" + "0" + "</DiscInLieuAmtDtl>";
			//strContents += "<CustRetailPrice>" + Utils.getMultipliedString(uOrderDInfo.nCustomerRetailPrice) + "</CustRetailPrice>";
			strContents += "<CustRetailPrice>" + 0 + "</CustRetailPrice>";
			strContents += "<CustPriceExtended>" + Utils.getMultipliedString(uOrderDInfo.nPrice * uOrderDInfo.dQuantity, context) + "</CustPriceExtended>";
			strContents += "</Detail>";

			for (UPromotionInfo uPromotionInfo : uOrderDInfo.uPromotionInfoList) {
				strContents += "<Promotion>";
				strContents += "<TrxnNo>" + uPromotionInfo.nTrxnNo + "</TrxnNo>";
				strContents += "<ItemNo>" + uPromotionInfo.strItemNo + "</ItemNo>";
				strContents += "<PlanNo>" + uPromotionInfo.nPlanNo + "</PlanNo>";
				strContents += "<AssignNo>" + uPromotionInfo.nAssignNo + "</AssignNo>";
				strContents += "<TrxnType>" + uPromotionInfo.nTrxnType + "</TrxnType>";
//				if(uPromotionInfo.nAmount * uOrderDInfo.dQuantity < 0)
//				{
//					strContents += "<Amount>" + uPromotionInfo.nAmount * uOrderDInfo.dQuantity + "</Amount>";
//				}
//				else
//				{
//					strContents += "<Amount>" + uPromotionInfo.nAmount * uOrderDInfo.dQuantity + "</Amount>";
//				}
				strContents += "<Amount>" + uPromotionInfo.nAmount * uOrderDInfo.dQuantity + "</Amount>";

				strContents += "<PriceDiscFlag>" + uPromotionInfo.nPriceDiscFlag + "</PriceDiscFlag>";
				strContents += "<ReasonCode>" + uPromotionInfo.nReasonCode + "</ReasonCode>";
				strContents += "<PromoAppMethod>" + uPromotionInfo.nPromoAppMethod + "</PromoAppMethod>";
				strContents += "<PromoType>" + uPromotionInfo.nPromoType + "</PromoType>";
				strContents += "<PromoMethod>" + uPromotionInfo.nPromoMethod + "</PromoMethod>";
				strContents += "<DiscAmt>" + uPromotionInfo.nDiscAmt + "</DiscAmt>";
				strContents += "<TrxnAmt>" + uPromotionInfo.nTrxnAmt + "</TrxnAmt>";
				strContents += "<DateStart>" + uPromotionInfo.strDateStart + "</DateStart>";
				strContents += "</Promotion>";
			}

			for(UTaxInfo uTaxInfo : uOrderDInfo.uTaxInfoList)
			{
				strContents += "<Tax>";
				strContents += "<TrxnNo>" + uTaxInfo.nTrxnNo + "</TrxnNo>";
				strContents += "<TrxnType>" + uTaxInfo.nTrxnType + "</TrxnType>";
				strContents += "<LocnNo>" + uTaxInfo.nLocnNo + "</LocnNo>";
				strContents += "<ItemNo>" + uTaxInfo.strItemNo + "</ItemNo>";
				strContents += "<TaxRateCode>" + uTaxInfo.strTaxRateCode + "</TaxRateCode>";
				strContents += "<TaxAmount>" + Utils.getMultipliedString(uTaxInfo.nTaxAmount, context) + "</TaxAmount>";
				strContents += "<CumulativeFlag>" + uTaxInfo.strCumulativeFlag + "</CumulativeFlag>";
				strContents += "<TaxRate>" + uTaxInfo.nTaxRate + "</TaxRate>";
				strContents += "<ReasonCode>" + uTaxInfo.nReasonCode + "</ReasonCode>";
				strContents += "<TaxRateProductCode>" + uTaxInfo.strTaxRateNumber + "</TaxRateProductCode>";
				strContents += "</Tax>";
			}
			if(uOrderDInfo.uLoadBlotInfo != null && uOrderDInfo.uLoadBlotInfo.size() > 0)
			{
				for(LoadBlotInfo loadBlotInfo : uOrderDInfo.uLoadBlotInfo)
				{
					strContents += "<BatchLot>";
					strContents += "<TrxnNo>" + uOrderDInfo.nTrxnNo + "</TrxnNo>";
					strContents += "<DetailType>" + "" + "</DetailType>";
					strContents += "<ItemNo>" + uOrderDInfo.strItemNo + "</ItemNo>";
					strContents += "<LocnNo>" + uOrderDInfo.nLocnNo + "</LocnNo>";
					strContents += "<ReasonCode>" + uOrderDInfo.nReasonCode + "</ReasonCode>";
					strContents += "<Sequence></Sequence>";
					strContents += "<LotNumber>" + loadBlotInfo.strLotNumber + "</LotNumber>";
					strContents += "<BatchDate>" + loadBlotInfo.strBatchDate + "</BatchDate>";
					strContents += "<ActualQtyLot>" + Utils.getMultipliedString(1, context) + "</ActualQtyLot>";
					strContents += "<BeginQtyLot>" + "0" + "</BeginQtyLot>";
					strContents += "<SuggestQtyLot>" + Utils.getMultipliedString(1, context) + "</SuggestQtyLot>";
					strContents += "</BatchLot>";
				}
			}
		} else {
			strContents += "<Detail>";
			strContents += "<TrxnNo>" + uOrderDInfo.nTrxnNo + "</TrxnNo>";
			strContents += "<TrxnType>" + uOrderDInfo.nTrxnType + "</TrxnType>";
			strContents += "<LocnNo>" + uOrderDInfo.nLocnNo + "</LocnNo>";
			strContents += "<ItemNo>" + uOrderDInfo.strItemNo + "</ItemNo>";
			if (uOrderDInfo.nPriceOverride == 0) {
				strContents += "<PriceOverride>" + "0" + "</PriceOverride>";
			} else {
				strContents += "<PriceOverride>" + Utils.getMultipliedString(uOrderDInfo.nPrice, context) + "</PriceOverride>";
			}
//		strContents += "<CustomerRetailPrice>" + Utils.getMultipliedString(uOrderDInfo.nCustomerRetailPrice) + "</CustomerRetailPrice>";
			if(getProdLoconSCWType(context, uOrderDInfo.strItemNo) == 3)
			{
				strContents += "<Quantity>" + 100000 + "</Quantity>";
			}
			else
			{
				strContents += "<Quantity>" + Utils.getMultipliedString(uOrderDInfo.nQuantity, context) + "</Quantity>";
			}
			strContents += "<Dumps>" + Utils.getMultipliedString(uOrderDInfo.nDumps, context) + "</Dumps>";
			strContents += "<Sales>" + Utils.getMultipliedString(uOrderDInfo.nSales, context) + "</Sales>";
			strContents += "<GrossPrice>" + Utils.getMultipliedString(uOrderDInfo.nOrgGrossPrice, context) + "</GrossPrice>";
			strContents += "<TotalAllowance>" + Utils.getMultipliedString(uOrderDInfo.nOrgGrossPrice - uOrderDInfo.nOrgPrice, context) + "</TotalAllowance>";
			strContents += "<RetailPrice>" + Utils.getMultipliedString(uOrderDInfo.nCustomerRetailPrice, context) + "</RetailPrice>";
			strContents += "<RetailOverride>" + "0" + "</RetailOverride>";

			int nReasonCode;
			if (uOrderDInfo.nSCWType != Global.INVENTORY_CONTAINERS) {
				nReasonCode = uOrderDInfo.nReasonCode;
			} else {
				nReasonCode = 0;
			}

			strContents += "<ReasonCode>" + nReasonCode + "</ReasonCode>";
			strContents += "<WeightedItem>" + "0" + "</WeightedItem>";
			strContents += "<TaxAmount>" + Utils.getMultipliedString(uOrderDInfo.nTaxAmount, context) + "</TaxAmount>";
			strContents += "<PresoldQuantity>" + Utils.getMultipliedString(uOrderDInfo.nPlanQty, context) + "</PresoldQuantity>";
			strContents += "<PrintOnOrder>" + "" + "</PrintOnOrder>";
			strContents += "<IsKitItem>" + "N" + "</IsKitItem>";
			if(Utils.getStringSetting(context, Global.PREF_INVCALC).equals("TTx"))
			{
				strContents += "<ExtendAmount>" + Utils.getMultipliedString(getRoundedValueForTTx(uOrderDInfo.nOrgGrossPrice) * uOrderDInfo.nQuantity, context) + "</ExtendAmount>";
			}
			else
			{
				strContents += "<ExtendAmount>" + Utils.getMultipliedString(uOrderDInfo.nPrice * uOrderDInfo.nQuantity, context) + "</ExtendAmount>";
			}

			strContents += "<User1>" + "" + "</User1>";
			strContents += "<User2>" + "" + "</User2>";
			strContents += "<TotalWeight>" + "100000" + "</TotalWeight>";
			String strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(context, new Date(uOrderDInfo.nTrxnNo)));
			strContents += "<ForecastStartDate>" + strTrxnDate + "</ForecastStartDate>";
			strContents += "<ForecastEndDate>" + strTrxnDate + "</ForecastEndDate>";
			strContents += "<OrigForecastQty>" + "0" + "</OrigForecastQty>";
			strContents += "<PriceDiscFlag>" + "0" + "</PriceDiscFlag>";
			strContents += "<RetailPriceFlag>" + "0" + "</RetailPriceFlag>";
			strContents += "<DiscRateEntered>" + "0" + "</DiscRateEntered>";
			strContents += "<User3>" + "" + "</User3>";
			strContents += "<CustPrice>" + Utils.getMultipliedString(uOrderDInfo.nPrice, context) + "</CustPrice>";
			strContents += "<OffInvPromoAmtDtl>" + "0" + "</OffInvPromoAmtDtl>";
			strContents += "<TotalAllowExtended>" + "0" + "</TotalAllowExtended>";
			strContents += "<QuantityCompl>" + "0" + "</QuantityCompl>";
			strContents += "<CaseFactor>" + uOrderDInfo.nCaseFactor + "</CaseFactor>";
			strContents += "<DiscInLieuAmtDtl>" + "0" + "</DiscInLieuAmtDtl>";
			//strContents += "<CustRetailPrice>" + Utils.getMultipliedString(uOrderDInfo.nCustomerRetailPrice) + "</CustRetailPrice>";
			strContents += "<CustRetailPrice>" + "0" + "</CustRetailPrice>";
			if(Utils.getStringSetting(context, Global.PREF_INVCALC).equals("TTx"))
			{
				strContents += "<CustPriceExtended>" + Utils.getMultipliedString(Utils.getRoundedValueForTTx(uOrderDInfo.nPrice * uOrderDInfo.nQuantity), context) + "</CustPriceExtended>";
			}
			else
			{
				strContents += "<CustPriceExtended>" + Utils.getMultipliedString(uOrderDInfo.nPrice * uOrderDInfo.nQuantity, context) + "</CustPriceExtended>";
			}

			strContents += "</Detail>";

			for (UPromotionInfo uPromotionInfo : uOrderDInfo.uPromotionInfoList) {
				strContents += "<Promotion>";
				strContents += "<TrxnNo>" + uPromotionInfo.nTrxnNo + "</TrxnNo>";
				strContents += "<ItemNo>" + uPromotionInfo.strItemNo + "</ItemNo>";
				strContents += "<PlanNo>" + uPromotionInfo.nPlanNo + "</PlanNo>";
				strContents += "<AssignNo>" + uPromotionInfo.nAssignNo + "</AssignNo>";
				strContents += "<TrxnType>" + uPromotionInfo.nTrxnType + "</TrxnType>";
//				if(uPromotionInfo.nAmount * uOrderDInfo.nQuantity < 0)
//				{
//					strContents += "<Amount>" + uPromotionInfo.nAmount * uOrderDInfo.nQuantity + "</Amount>";
//				}
//				else
//				{
//					strContents += "<Amount>" + uPromotionInfo.nAmount * uOrderDInfo.nQuantity + "</Amount>";
//				}
				strContents += "<Amount>" + uPromotionInfo.nAmount * uOrderDInfo.nQuantity + "</Amount>";

				strContents += "<PriceDiscFlag>" + uPromotionInfo.nPriceDiscFlag + "</PriceDiscFlag>";
				strContents += "<ReasonCode>" + uPromotionInfo.nReasonCode + "</ReasonCode>";
				strContents += "<PromoAppMethod>" + uPromotionInfo.nPromoAppMethod + "</PromoAppMethod>";
				strContents += "<PromoType>" + uPromotionInfo.nPromoType + "</PromoType>";
				strContents += "<PromoMethod>" + uPromotionInfo.nPromoMethod + "</PromoMethod>";
				strContents += "<DiscAmt>" + uPromotionInfo.nDiscAmt + "</DiscAmt>";
				strContents += "<TrxnAmt>" + uPromotionInfo.nTrxnAmt + "</TrxnAmt>";
				strContents += "<DateStart>" + uPromotionInfo.strDateStart + "</DateStart>";
				strContents += "</Promotion>";
			}

			for(UTaxInfo uTaxInfo : uOrderDInfo.uTaxInfoList)
			{
				strContents += "<Tax>";
				strContents += "<TrxnNo>" + uTaxInfo.nTrxnNo + "</TrxnNo>";
				strContents += "<TrxnType>" + uTaxInfo.nTrxnType + "</TrxnType>";
				strContents += "<LocnNo>" + uTaxInfo.nLocnNo + "</LocnNo>";
				strContents += "<ItemNo>" + uTaxInfo.strItemNo + "</ItemNo>";
				strContents += "<TaxRateCode>" + uTaxInfo.strTaxRateCode + "</TaxRateCode>";
				strContents += "<TaxAmount>" + Utils.getMultipliedString(uTaxInfo.nTaxAmount, context) + "</TaxAmount>";
				strContents += "<CumulativeFlag>" + uTaxInfo.strCumulativeFlag + "</CumulativeFlag>";
				strContents += "<TaxRate>" + String.valueOf(uTaxInfo.nTaxRate) + "</TaxRate>";
				strContents += "<ReasonCode>" + String.valueOf(uTaxInfo.nReasonCode) + "</ReasonCode>";
				strContents += "<TaxRateProductCode>" + uTaxInfo.strTaxRateNumber + "</TaxRateProductCode>";
				strContents += "</Tax>";
			}

			if(uOrderDInfo.uLoadBlotInfo != null && uOrderDInfo.uLoadBlotInfo.size() > 0)
			{
				for(LoadBlotInfo loadBlotInfo : uOrderDInfo.uLoadBlotInfo)
				{
					strContents += "<BatchLot>";
					strContents += "<TrxnNo>" + uOrderDInfo.nTrxnNo + "</TrxnNo>";
					strContents += "<DetailType>" + "" + "</DetailType>";
					strContents += "<ItemNo>" + uOrderDInfo.strItemNo + "</ItemNo>";
					strContents += "<LocnNo>" + uOrderDInfo.nLocnNo + "</LocnNo>";
					strContents += "<ReasonCode>" + uOrderDInfo.nReasonCode + "</ReasonCode>";
					strContents += "<Sequence></Sequence>";
					strContents += "<LotNumber>" + loadBlotInfo.strLotNumber + "</LotNumber>";
					strContents += "<BatchDate>" + loadBlotInfo.strBatchDate + "</BatchDate>";
					strContents += "<ActualQtyLot>" + Utils.getMultipliedString(1, context) + "</ActualQtyLot>";
					strContents += "<BeginQtyLot>" + "0" + "</BeginQtyLot>";
					strContents += "<SuggestQtyLot>" + Utils.getMultipliedString(1, context) + "</SuggestQtyLot>";
					strContents += "</BatchLot>";
				}
			}
		}


		return strContents;
	}

	public synchronized static Bitmap getSafeDecodeBitmap(String strFilePath, int maxSize) {
		try {
			// Max image size
			int IMAGE_MAX_SIZE = maxSize;
			File file = new File(strFilePath);
			if (file.exists() == false) {
				return null;
			}

			BitmapFactory.Options bfo = new BitmapFactory.Options();
			bfo.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(strFilePath, bfo);
			if (IMAGE_MAX_SIZE > 0) {
				if (bfo.outHeight * bfo.outWidth >= IMAGE_MAX_SIZE * IMAGE_MAX_SIZE) {
					bfo.inSampleSize = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (float) Math.max(bfo.outHeight, bfo.outWidth)) / Math.log(0.5)));
				}
			}

			bfo.inJustDecodeBounds = false;
			bfo.inPurgeable = true;
			bfo.inDither = true;

			final Bitmap bitmap = BitmapFactory.decodeFile(strFilePath, bfo);
			int degree = GetExifOrientation(strFilePath);
			return GetRotatedBitmap(bitmap, degree);
		} catch (OutOfMemoryError ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public synchronized static int GetExifOrientation(String filepath) {
		int degree = 0;
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(filepath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (exif != null) {
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
			if (orientation != -1) {
				// We only recognize a subset of orientation tag values.
				switch (orientation) {
					case ExifInterface.ORIENTATION_ROTATE_90:
						degree = 90;
						break;

					case ExifInterface.ORIENTATION_ROTATE_180:
						degree = 180;
						break;

					case ExifInterface.ORIENTATION_ROTATE_270:
						degree = 270;
						break;
				}
			}
		}
		return degree;
	}

	public synchronized static Bitmap GetRotatedBitmap(Bitmap bitmap, int degrees) {
		if (degrees != 0 && bitmap != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
			try {
				Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
				if (bitmap != b2) {
					bitmap.recycle();
					bitmap = b2;
				}
			} catch (OutOfMemoryError ex) {
				ex.printStackTrace();
			}
		}
		return bitmap;
	}

	public static double getDistance(double sourceLat, double sourceLng, double targetLat, double targetLng) {
		if (sourceLat == 0 || sourceLng == 0 || targetLat == 0 || targetLng == 0) {
			return Double.MAX_VALUE;
		}
		double theta = targetLng - sourceLng;
		double dist = (Math.sin(deg2rad(targetLat)) * Math.sin(deg2rad(sourceLat)) + Math.cos(deg2rad(targetLat)) * Math.cos(deg2rad(sourceLat)) * Math.cos(deg2rad(theta)));
		dist = (Math.acos(dist));
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515f;

		dist = dist * 1.609344f;        // Kilometer
//		dist = dist * 0.8684;		// Mile

		return dist;
	}

	private static double deg2rad(double deg) {
		return ((deg * Math.PI / 180.0f));
	}

	private static double rad2deg(double rad) {
		return ((rad * 180.0 / Math.PI));
	}

	public static Date getDateFromString(String strDate) {
		Date value = new Date();
		if (strDate.length() == 0) {
			return value;
		}
		SimpleDateFormat origFormatter = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT);
		try {
			value = origFormatter.parse(strDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return value;
	}

	public static String getFormattedPrice(Context context, float nPrice) {
		String strResult = String.format(Locale.ENGLISH, "%s" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", "", Math.abs(nPrice));
		if (Global.gCurCustomerInfo != null && Global.gCurCustomerInfo.nInvoiceFmt > 0) {
			strResult = String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice));
		}
		return strResult;
	}

	/*public static String getFormattedPrice(Context context, float nPrice) {
		if (Math.abs(nPrice) < 0.01f) {
			nPrice = 0f;
		}
		String strResult = "";
		if (Global.gCurrencySymbol.equals("$") == true) {
			strResult = String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", (float) nPrice);
		} else {
			strResult = String.format(Locale.ENGLISH, "%d", Math.round(nPrice));
		}
		return Global.gCurrencySymbol + strResult;
	}*/

	public static float getFloatFromString(String str) {
		float result = 0;
		try {
			result = Float.valueOf(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static double getDoubleFromString(String str) {
		double result = 0;
		try {
			result = Double.valueOf(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String[] _specialCharacter = {"&#33;", "&#34;", "&#35;", "&#36;", "&#37;", "&#38;", "&#39;", "&#40;", "&#41;", "&#42;", "&#43;", "&#44;", "&#45;", "&#46;", "&#47;",
			"&#58;", "&#59;", "&#60;", "&#61;", "&#62;", "&#63;", "&#64;", "&#91;", "&#92;", "&#93;", "&#94;", "&#95;", "&#96;", "&#123;", "&#124;", "&#125;", "&#126;"};
	public static String[] _specialCharacter_ = {"!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-", ".", "/",
			":", ";", "<", "=", ">", "?", "@", "[", "\\", "]", "^", "_", "`", "{", "|", "}", "~"};
	public static Map<String, String> _htmlMap = new HashMap<>();

	public static void BuildHTMMap() {
		_htmlMap.clear();
		for (int i = 0; i < _specialCharacter_.length; i++) {
			_htmlMap.put(_specialCharacter_[i], _specialCharacter[i]);
		}
	}

	public static String ConvertHTM(String strText) {
		if (strText == null) return "";
		if (strText.length() == 0) return "";
		String strResult = "";
		for (int i = 0; i < strText.length(); i++) {
			String strContent = strText.substring(i, i + 1);
			String strValue = _htmlMap.get(strContent);
			if (strValue != null)
				strContent = strValue;
			strResult += strContent;
		}
		return strResult;
	}

	public static boolean hasAnyPrefix(String number, String... prefixes) {
		if (number == null) {
			return false;
		}
		for (String prefix : prefixes) {
			if (number.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	public static int getCardLength(String strCardNo1) {
		if (hasAnyPrefix(strCardNo1, Global.PREFIXES_AMERICAN_EXPRESS)) return 15;
		if (hasAnyPrefix(strCardNo1, Global.PREFIXES_DINERS_CLUB)) return 14;
		return 16;
	}

	public static void registerUploadData(Context context, String strLocalFilePath, String strServerFilePath) {
		File file = new File(strLocalFilePath);
		if (!file.exists() || !file.isFile())
			return;
		UploadDbAdapter uploadDbAdapter = new UploadDbAdapter(context);
		uploadDbAdapter.open();
		uploadDbAdapter.insertUploadFile(strLocalFilePath, strServerFilePath);
		uploadDbAdapter.close();

		BackupDbAdapter backupDbAdapter = new BackupDbAdapter(context);
		backupDbAdapter.open();
		backupDbAdapter.insertUploadFile(strLocalFilePath, strServerFilePath, Utils.getStringSetting(context, Global.PREF_LOGIN_DATE), Global.gRouteNumber);
		backupDbAdapter.close();
	}

	public static String getOrderNo(String strValue, String strFormat) {
		int nFormatLen = strFormat.length();
		try {
			strValue = strValue.substring(strValue.length() - nFormatLen, strValue.length());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return strValue;
	}

	private static QBChatDialogMessageListener allDialogsMessagesListener;
	private static SystemMessagesListener systemMessagesListener;
	public static QBSystemMessagesManager systemMessagesManager;
	private static QBIncomingMessagesManager incomingMessagesManager;
	public static DialogsManager dialogsManager;

	public static class SystemMessagesListener implements QBSystemMessageListener {
		@Override
		public void processMessage(final QBChatMessage qbChatMessage) {
			dialogsManager.onSystemMessageReceived(qbChatMessage);
		}

		@Override
		public void processError(QBChatException e, QBChatMessage qbChatMessage) {

		}
	}

	public static class AllDialogsMessageListener extends QbChatDialogMessageListenerImp {
		@Override
		public void processMessage(final String dialogId, final QBChatMessage qbChatMessage, Integer senderId) {
			if (!senderId.equals(ChatHelper.getCurrentUser().getId())) {
				dialogsManager.onGlobalMessageReceived(dialogId, qbChatMessage);
			}
		}
	}

	public static void registerChatService(Context context) {
		allDialogsMessagesListener = new AllDialogsMessageListener();
		systemMessagesListener = new SystemMessagesListener();

		dialogsManager = new DialogsManager();
		incomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
		systemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();

		if (incomingMessagesManager != null) {
			incomingMessagesManager.addDialogMessageListener(allDialogsMessagesListener != null
					? allDialogsMessagesListener : new AllDialogsMessageListener());
		}

		if (systemMessagesManager != null) {
			systemMessagesManager.addSystemMessageListener(systemMessagesListener != null
					? systemMessagesListener : new SystemMessagesListener());
		}

/*		Global.chatRoster = QBChatService.getInstance().getRoster(QBRoster.SubscriptionMode.mutual, Global.subscriptionListener);

		QBPresence presence = new QBPresence(QBPresence.Type.online, "Online", 1, QBPresence.Mode.available);
		try {
			Global.chatRoster.sendPresence(presence);
		} catch (SmackException.NotConnectedException e) {

		}

		List<String> tags = new ArrayList<>();
		tags.add(getStringSetting(context, Global.PREF_COMPANY_NAME));
		QBUsers.getUsersByTags(tags, null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
			@Override
			public void onSuccess(ArrayList<QBUser> result, Bundle params) {

				QBUser curUser = QBChatService.getInstance().getUser();
				for(QBUser user : result)
				{
					if( user.getId() == curUser.getId() ) continue;
					if( Global.chatRoster.getPresence(user.getId()) == null)
					{
						try {
							Global.chatRoster.confirmSubscription(user.getId());
						} catch (Exception e) {
							e.printStackTrace();
						}
						try
						{
							Global.chatRoster.subscribe(user.getId());
						}catch(Exception e)
						{
							e.printStackTrace();
						}
					}

				}
				QBPresence presence = new QBPresence(QBPresence.Type.online, "Online", 1, QBPresence.Mode.available);
				try {
					Global.chatRoster.sendPresence(presence);
				} catch (SmackException.NotConnectedException e) {

				}
			}

			@Override
			public void onError(QBResponseException e) {

			}
		});*/
	}

	public static void unregisterQbChatListeners() {
		/*QBPresence presence = new QBPresence(QBPresence.Type.offline, "Offline", 1, QBPresence.Mode.available);
		try {
			Global.chatRoster.sendPresence(presence);
		} catch (Exception e) {

		}*/
		ChatHelper.getInstance().destroy();
		QbDialogHolder.getInstance().clear();

		if (incomingMessagesManager != null) {
			incomingMessagesManager.removeDialogMessageListrener(allDialogsMessagesListener);
		}
		incomingMessagesManager = null;

		if (systemMessagesManager != null) {
			systemMessagesManager.removeSystemMessageListener(systemMessagesListener);
		}
		systemMessagesListener = null;
		Global.gCurQBUser = null;
	}

	public static boolean isStorageOK(Context context) {
		File file = new File(Global.ROOT_LOCATION);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file.exists();
	}

	public static String getTermsText() {
		String termsText = "";
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			org.w3c.dom.Document document = null;
			document = builder.parse(new File(Global.ROOT_LOCATION + "/" + Global.PRINT_TERMS_TEMPLATE_FILENAME));
			NodeList elements = document.getElementsByTagName("Table");
			Node termsNode = null;
			for (int i = 0; i < elements.getLength(); i++) {
				Node node = elements.item(i);
				try {
					Node termsProperty = node.getAttributes().getNamedItem("property");
					if (termsProperty != null) {
						termsNode = node;
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (termsNode != null) {
				NodeList termsNodeList = termsNode.getChildNodes();
				for (int i = 0; i < termsNodeList.getLength(); i++) {
					termsText += termsNodeList.item(i).getTextContent().trim() + "\n";
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return termsText;
	}

	public static long getFileSize(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) return 0;
		return file.length();
	}

	public static String getFileName(String strPath) {
		if (strPath == null) {
			return "";
		}
		String[] strToken = strPath.split("/");
		return strToken[strToken.length - 1];
	}

	public static String getFolderPath(String strPath) {
		File file = new File(strPath);
		return file.getParent();
	}

	public static void setEndTripPlan(boolean bFlag, Context context) {
		if (bFlag == false) {
			String strLoginUserName = Utils.getStringSetting(context, Global.PREF_LOGIN_USER_NAME);
			String strLoginPinNumber = Utils.getStringSetting(context, Global.PREF_LOGIN_PIN_NUMBER);
			String strLoginTripNumber = Utils.getStringSetting(context, Global.PREF_TRIP_NUMBER);
			EndTripDbAdapter dbAdapter = new EndTripDbAdapter(context);
			dbAdapter.open();
			dbAdapter.insertTripData(strLoginTripNumber, strLoginPinNumber, strLoginUserName);
			dbAdapter.close();
		} else {
			EndTripDbAdapter dbAdapter = new EndTripDbAdapter(context);
			dbAdapter.open();
			dbAdapter.updateTripData();
			dbAdapter.close();
		}
	}

	private static String _print_font_family = "Arial";
	private static String _print_encoding = FontFactory.defaultEncoding;
	private static boolean _print_embeded = FontFactory.defaultEmbedding;
	private static String _print_font_filename = "";

	public static String getPrintFontFamily() {
		return _print_font_family;
	}

	public static String getPrintEncoding() {
		return _print_encoding;
	}

	public static boolean getPrintEmbeded() {
		return _print_embeded;
	}

	public static String getPrintFontFilePath() {
		if (TextUtils.isEmpty(_print_font_filename)) {
			return "";
		} else {
			return Global.ROOT_PRINT_TEMPLATE_LOCCATION + _print_font_filename;
		}
	}

	public static void registerPrintFontFamily(String strFontFileName, String strFontName, String strFontEncoding, boolean bEmbeded) {
		_print_font_family = strFontName;
		_print_encoding = strFontEncoding;
		_print_embeded = bEmbeded;
		_print_font_filename = strFontFileName;
		new Thread() {
			@Override
			public void run() {
				getFont(_print_font_filename, _print_encoding, _print_embeded, 10, Font.NORMAL, BaseColor.BLACK);
			}
		}.start();
	}

	public static Font getFont(String strPath, String encoding, boolean embeded, float size, int type, BaseColor color) {
		if (!TextUtils.isEmpty(strPath)) {
			try {
				BaseFont bf = BaseFont.createFont(strPath, encoding, embeded);
				Font font = new Font(bf, size, type, color);
				return font;
			} catch (DocumentException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			return FontFactory.getFont(getPrintFontFamily(), size, type, color);
		}
		return FontFactory.getFont(getPrintFontFamily(), size, type, color);
	}

	private static Map<String, Integer> mProdLoconSCWTypeMap = new HashMap<>();

	public static int getProdLoconSCWType(Context context, String strItem) {
		Integer type = mProdLoconSCWTypeMap.get(strItem);
		if (type == null) {
			DbAdapter dbAdapter = new DbAdapter(context);
			dbAdapter.open();
			type = dbAdapter.getProdLocnSCWType(strItem);
			dbAdapter.close();
			mProdLoconSCWTypeMap.put(strItem, type);
		}
		if(type == 3) return Global.WEIGHT_ITEM;
		return type;
	}


	/*public static Bitmap getBitmapFromPDFFile(Context context, String pdfFilePath)
	{
		try
		{

			// Render the image to an RGB Bitmap
			MuPDFCore muPDFCore = new MuPDFCore(pdfFilePath);
			muPDFCore.countPages();
			//muPDFCore.gotoPage(0);
			PointF pointF = muPDFCore.getPageSize(0);

			Bitmap image = Bitmap.createBitmap((int)(pointF.x * 1.5f), (int)(pointF.y * 1.5f), Bitmap.Config.ARGB_8888);
            muPDFCore.drawPage(image, 0, (int)(pointF.x * 1.5f), (int)(pointF.y * 1.5f), 0, 0, (int)(pointF.x * 1.5f), (int)(pointF.y * 1.5f), new Cookie());
			return image;

		}catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static void convertPDFtoImageFile(Context context, String pdfFilePath, String imageFilePath)
	{
		try
		{
			// Render the image to an RGB Bitmap
			MuPDFCore muPDFCore = new MuPDFCore(pdfFilePath);
			muPDFCore.countPages();
			//muPDFCore.gotoPage(0);
			PointF pointF = muPDFCore.getPageSize(0);

			Bitmap image = Bitmap.createBitmap((int)(pointF.x * 1.5f), (int)(pointF.y * 1.5f), Bitmap.Config.ARGB_8888);
			//muPDFCore.drawSinglePage(0, image, (int)(pointF.x * 1.5f), (int)(pointF.y * 1.5f));
            muPDFCore.drawPage(image, 0, (int)(pointF.x * 1.5f), (int)(pointF.y * 1.5f), 0, 0, (int)(pointF.x * 1.5f), (int)(pointF.y * 1.5f), new Cookie());
			File renderFile = new File(imageFilePath);
			FileOutputStream fileOut = new FileOutputStream(renderFile);
			image.compress(Bitmap.CompressFormat.JPEG, 60, fileOut);
			fileOut.close();

		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}*/

	public interface ApiInterface {
		@GET("/Transaction/{AccessCode}")
		retrofit.Call<EWayTransactionResponse> getResponse(@Path("AccessCode") String accessCode);

		@POST("/Transaction/{TransactionID}/Refund")
		retrofit.Call<EWayRefundResponse> refund(@Path("TransactionID") String transactionID);
	}


	private static void enableTls12OnPreLollipop(OkHttpClient client) {
		try {
			if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 20) {
				SSLContext sc = SSLContext.getInstance("TLSv1.2");
				sc.init(null, null, null);
				client.setSslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));
				ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
						.tlsVersions(TlsVersion.TLS_1_2)
						.build();
				List<ConnectionSpec> specs = new ArrayList<>();
				specs.add(cs);
				specs.add(ConnectionSpec.COMPATIBLE_TLS);
				specs.add(ConnectionSpec.CLEARTEXT);
				client.setConnectionSpecs(specs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static OkHttpClient getEwayClient(final String apiKey, final String password) throws RapidConfigurationException {
		OkHttpClient client = new OkHttpClient();
		enableTls12OnPreLollipop(client);
		client.interceptors().add(new Interceptor() {

			public com.squareup.okhttp.Response intercept(Interceptor.Chain chain) throws IOException {
				String credential = Credentials.basic(apiKey, password);
				Request originalRequest = chain.request();
				Request.Builder requestBuilder = originalRequest.newBuilder().header("Authorization", credential).header("User-Agent", ";eWAY SDK Android 1.2").method(originalRequest.method(), originalRequest.body());
				Request request = requestBuilder.build();
				return chain.proceed(request);
			}
		});
		client.setConnectTimeout(3, TimeUnit.MINUTES);
		client.setReadTimeout(3, TimeUnit.MINUTES);
		return client;
	}

	public static ApiInterface getEWayApiClient(String apiKey, String password) throws RapidConfigurationException {
		String baseUrl = RapidAPI.changeUrl(RapidAPI.RapidEndpoint);
		Retrofit retrofit = (new Retrofit.Builder()).baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).client(getEwayClient(apiKey, password)).build();
		ApiInterface adapter = retrofit.create(ApiInterface.class);
		return adapter;
	}

	public static String last4Character(String strText) {
		if (strText == null) return "";
		if (strText.length() < 4) return strText;
		return strText.substring(strText.length() - 4, strText.length());
	}

	public static String getRoundedString(Context context, double value) {
		BigDecimal rounded = BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
		rounded = BigDecimal.valueOf(rounded.doubleValue()).setScale(5, RoundingMode.HALF_UP);

		int nDecimal = Utils.getIntSetting(context, Global.PREF_DECIMAL);
		rounded = BigDecimal.valueOf(rounded.doubleValue()).setScale(nDecimal + 1, RoundingMode.DOWN);
		rounded = BigDecimal.valueOf(rounded.doubleValue()).setScale(nDecimal, RoundingMode.HALF_UP);

		return rounded.toString();
	}

	public static float getRoundedValueForTTx(double value)
	{
		return BigDecimal.valueOf(value).setScale(5, RoundingMode.HALF_UP).floatValue();
	}

	public static float getRoundedValue(double value) {
		try {
			BigDecimal rounded = BigDecimal.valueOf(value).setScale(5, RoundingMode.HALF_UP);
			rounded = BigDecimal.valueOf(rounded.doubleValue()).setScale(4, RoundingMode.HALF_UP);

			rounded = BigDecimal.valueOf(rounded.doubleValue()).setScale(3, RoundingMode.DOWN);
			rounded = BigDecimal.valueOf(rounded.doubleValue()).setScale(2, RoundingMode.HALF_UP);

			return rounded.floatValue();
		}
		catch (Exception e) {
			return 0f;
		}
	}

	public static BigDecimal getRoundedDecValue(BigDecimal value) {
		BigDecimal rounded = value.setScale(5, RoundingMode.HALF_UP);
		rounded = BigDecimal.valueOf(rounded.doubleValue()).setScale(4, RoundingMode.HALF_UP);

		rounded = BigDecimal.valueOf(rounded.doubleValue()).setScale(3, RoundingMode.DOWN);
		rounded = BigDecimal.valueOf(rounded.doubleValue()).setScale(2, RoundingMode.HALF_UP);

		return rounded;
	}

	public static float get5RoundedValue(Context context, double value) {
		if (Utils.getStringSetting(context, Global.PREF_INVCALC).equalsIgnoreCase("ERMS")) {
			BigDecimal rounded = BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
			rounded = BigDecimal.valueOf(rounded.doubleValue()).setScale(5, RoundingMode.HALF_UP);
			return rounded.floatValue();
		}
		else if(Utils.getStringSetting(context, Global.PREF_INVCALC).equals("TTx")) {
			BigDecimal rounded = BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP);
			rounded = BigDecimal.valueOf(rounded.doubleValue()).setScale(2, RoundingMode.HALF_UP);
			return rounded.floatValue();
		}
		else {
			BigDecimal rounded = BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP);
			rounded = BigDecimal.valueOf(rounded.doubleValue()).setScale(2, RoundingMode.HALF_UP);
			return rounded.floatValue();
		}
	}

	public static String getWeekDateName(int nWeekDay) {
		if (nWeekDay == getTodayWeekNo()) return "Today";
		switch (nWeekDay) {
			case 0:
				return "Unknown";
			case 1:
				return "Monday";
			case 2:
				return "Tuesday";
			case 3:
				return "Wednesday";
			case 4:
				return "Thursday";
			case 5:
				return "Friday";
			case 6:
				return "Saturday";
			case 7:
				return "Sunday";
		}
		return "Unknown";
	}

	public static int getTodayWeekNo() {
		Calendar calendar = Calendar.getInstance();
		int today = calendar.get(Calendar.DAY_OF_WEEK);
		if (today == Calendar.SUNDAY) {
			today = 7;
		} else {
			today--;
		}
		return today;
	}

	public static String ip_addr = "";

	public static String getIPAddress(Context context) {
		if(!Utils.isConnectingToInternet(context))
		{
			return ip_addr;
		}
		if (TextUtils.isEmpty(ip_addr)) {
			final CountDownLatch latch = new CountDownLatch(1);
			IPApiClient.getApiClient().getIPAddr(new HashMap<String, String>()).enqueue(new retrofit.Callback<IPAddrInfo>() {
				@Override
				public void onResponse(retrofit.Response<IPAddrInfo> response, Retrofit retrofit) {
					try {
						ip_addr = response.body().ip;
					} catch (Exception e) {
						e.printStackTrace();
					}
					latch.countDown();
				}

				@Override
				public void onFailure(Throwable t) {
					latch.countDown();
				}
			});

			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (TextUtils.isEmpty(ip_addr)) {
			try {
				WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
				WifiInfo wifiinfo = wm.getConnectionInfo();
				byte[] myIPAddress = BigInteger.valueOf(wifiinfo.getIpAddress()).toByteArray();

				InetAddress myInetIP = null;
				try {
					myInetIP = InetAddress.getByAddress(myIPAddress);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				String ip = myInetIP.getHostAddress();
				ip_addr = ip;
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return ip_addr;
	}

	private static List<CustomerInfo> filterSameCustomer(List<CustomerInfo> customerInfoList) {
		Map<String, CustomerInfo> customerInfoMap = new HashMap<>();
		List<CustomerInfo> newList = new ArrayList<>();
		for (CustomerInfo customerInfo : customerInfoList) {
			String key = customerInfo.nChainNo + "___" + customerInfo.nCustNo;
			CustomerInfo info = customerInfoMap.get(key);
			if (info == null || info.bCompleted == false) {
				customerInfoMap.put(key, customerInfo);
				newList.add(customerInfo);
			}
		}
		return newList;
	}

	public static boolean isAllCustomerHasReasonCode(Context context) {
		DbAdapter dbAdapter = new DbAdapter(context);
		dbAdapter.open();
		List<DescTypeInfo> descTypeList = dbAdapter.getDescTypeList("NOSERVRSN");
		//List<CustomerInfo> customerList = dbAdapter.getCustomerList(true);
		List<CustomerInfo> customerList = filterSameCustomer(dbAdapter.getWhiteCustomerInfo(false, Utils.getIntSetting(context, Global.DAY_NO)));
		customerList.addAll(dbAdapter.getCustomerList(true, false, Utils.getIntSetting(context, Global.DAY_NO)));


		if (descTypeList.size() == 0) {
			dbAdapter.close();
			return true;
		}
		boolean bExist = true;
		for (CustomerInfo customerInfo : customerList) {
			if (customerInfo.bCompleted == true) continue;
			UServicesInfo info = dbAdapter.getUServiceInfo(customerInfo.nCustNo, customerInfo.nChainNo);
			if (info == null) {
				bExist = false;
				break;
			}
		}
		dbAdapter.close();
		return bExist;
	}

	public static void setAlarm(Context context) {
		Intent intent = new Intent("dsd.dsdvansales.alarm.action.trigger");
		intent.setClass(context, WakeupReceiver.class);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
		{
			boolean alarmRunning = (PendingIntent.getBroadcast(context, Global.WAKE_RESPONSE, intent,
					PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) != null);
			if (alarmRunning) {
				cancelDailyFluidAlaram(context);
			}
		}
		else {
			boolean alarmRunning = (PendingIntent.getBroadcast(context, Global.WAKE_RESPONSE, intent,
					PendingIntent.FLAG_NO_CREATE) != null);
			if (alarmRunning) {
				cancelDailyFluidAlaram(context);
			}
		}



		PendingIntent sender;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
		{
			sender = PendingIntent.getBroadcast(context, Global.WAKE_RESPONSE,
				intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		}
		else {
			sender = PendingIntent.getBroadcast(context, Global.WAKE_RESPONSE,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}


		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		int nGPSPoll = Utils.getIntSetting(context, Global.PREF_GPS_POLL);
		if (nGPSPoll == 0) {
			nGPSPoll = 1;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			// Wakes up the device in Doze Mode
			am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + nGPSPoll * 60 * 1000, sender);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			// Wakes up the device in Idle Mode
			am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + nGPSPoll * 60 * 1000, sender);
		} else {
			// Old APIs
			am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + nGPSPoll * 60 * 1000, sender);
		}
	}

	public static void cancelDailyFluidAlaram(Context context) {
		//Intent intent = new Intent(context, DailyAlarmReceiver.class);
		Intent intent = new Intent("dsd.dsdvansales.alarm.action.trigger");
		intent.setClass(context, WakeupReceiver.class);

		PendingIntent sender;
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
		{
			sender = PendingIntent.getBroadcast(context, Global.WAKE_RESPONSE,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		else {
			sender = PendingIntent.getBroadcast(context, Global.WAKE_RESPONSE,
					intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		}

		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.cancel(sender);
	}

	public static void uploadGPSPollData(final Context context, Location location, long lCurTime) {
		if (Global.gSignedOn) {
			double lat = location.getLatitude();
			double lng = location.getLongitude();

			Date curDate = new Date();
			UGPSInfo uGPSInfo = new UGPSInfo();
			uGPSInfo.nTrxnNo = lCurTime;
			uGPSInfo.nChainNo = 0;
			uGPSInfo.nCustNo = Global.GPS_CUST_NO;
			uGPSInfo.nStartTrxnNo = 0;
			if (lat != 0 && lng != 0) {
				uGPSInfo.strGPSData = String.format(Locale.ENGLISH, "%f, %f", lat, lng);
			} else {
				uGPSInfo.strGPSData = "ERROR: NO GPS";
			}
			uGPSInfo.strDocType = "GPS";
			uGPSInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(curDate);
			uGPSInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(curDate);

			UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
			uTransactionInfo.nTrxnNo = uGPSInfo.nTrxnNo;
			uTransactionInfo.strDocType = "GPS";
			uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(context, new Date(uGPSInfo.nTrxnNo)));
			uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(uGPSInfo.nTrxnNo);
			uTransactionInfo.nChainNo = 0;
			uTransactionInfo.nCustNo = Global.GPS_CUST_NO;

			//Upload Data to server
			//String strUploadDir = Global.UPLOAD_DIR + Global.gRouteNumber + "/";
			String strUploadDir = Global.UPLOAD_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(context, Global.PREF_LOGIN_DATE) + "/";

			if (Utils.createDirectory(strUploadDir) == false) {
				Utils.registerChaseLog("Can not Create Directory", lCurTime);
				return;
			}
			String strSuffix = uTransactionInfo.strTrxnDate + uTransactionInfo.strTrxnTime;
			String strGPSFileName = "GPS" + strSuffix + ".upl";

			String strContents = "<GPS>";
			strContents += "<GPSLog>";
			strContents += "<TrxnNo>" + uGPSInfo.nTrxnNo + "</TrxnNo>";
			strContents += "<ChainNo>" + uGPSInfo.nChainNo + "</ChainNo>";
			strContents += "<CustNo>" + (uGPSInfo.nCustNo != Global.GPS_CUST_NO ? uGPSInfo.nCustNo : 0) + "</CustNo>";
			strContents += "<StartDate>" + uGPSInfo.strTrxnDate + "</StartDate>";
			strContents += "<StartTime>" + uGPSInfo.strTrxnTime + "</StartTime>";
			strContents += "<EndDate>" + uGPSInfo.strTrxnDate + "</EndDate>";
			strContents += "<EndTime>" + uGPSInfo.strTrxnTime + "</EndTime>";
			if (uGPSInfo.nStartTrxnNo == 0) {
				strContents += "<StartTrxnNo>" + "" + "</StartTrxnNo>";
			} else {
				strContents += "<StartTrxnNo>" + uGPSInfo.nStartTrxnNo + "</StartTrxnNo>";
			}
			strContents += "<GPSData>" + uGPSInfo.strGPSData + "</GPSData>";
			strContents += "<DocNo>" + "0" + "</DocNo>";
			strContents += "<DocType>" + uGPSInfo.strDocType + "</DocType>";
			strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
			strContents += "<PrintedFlag>" + "0" + "</PrintedFlag>";
			strContents += "<TrxnDate>" + uGPSInfo.strTrxnDate + "</TrxnDate>";
			strContents += "<TrxnTime>" + uGPSInfo.strTrxnTime + "</TrxnTime>";
			strContents += "<Reference>" + "" + "</Reference>";
			strContents += "<TCOMStatus>" + "0" + "</TCOMStatus>";
			strContents += "<SaleDate>" + uGPSInfo.strTrxnDate + "</SaleDate>";
			strContents += "<Trip>" + Global.gTripNumber + "</Trip>";
			strContents += "</GPSLog>";
			strContents += "</GPS>";
			writeStringAsFile(strContents, strUploadDir, strGPSFileName);


			// write to Transactions.upl

			String strTransactionsFileName = "Transactions" + strSuffix + ".upl";
			strContents = "<Transactions>";

			strContents += "<Transaction>";
			strContents += "<TrxnNo>" + uTransactionInfo.nTrxnNo + "</TrxnNo>";
			strContents += "<DocNo>" + "0" + "</DocNo>";
			strContents += "<DocType>" + uTransactionInfo.strDocType + "</DocType>";
			strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
			strContents += "<PrintedFlag>" + "0" + "</PrintedFlag>";
			strContents += "<TrxnDate>" + uTransactionInfo.strTrxnDate + "</TrxnDate>";
			strContents += "<TrxnTime>" + uTransactionInfo.strTrxnTime + "</TrxnTime>";
			strContents += "<Reference>" + uTransactionInfo.strReference + "</Reference>";
			strContents += "<TCOMStatus>" + "0" + "</TCOMStatus>";
			strContents += "<ChainNo>" + uTransactionInfo.nChainNo + "</ChainNo>";
			strContents += "<CustNo>" + (uTransactionInfo.nCustNo != Global.GPS_CUST_NO ? uTransactionInfo.nCustNo : 0) + "</CustNo>";
			strContents += "<SaleDate>" + uTransactionInfo.strTrxnDate + "</SaleDate>";
			strContents += "<PhysAvail>" + "0" + "</PhysAvail>";
			strContents += "<VirtAvail>" + "0" + "</VirtAvail>";
			strContents += "<Trip>" + Global.gTripNumber + "</Trip>";
			strContents += "<TripId>" + uTransactionInfo.strTripId + "</TripId>";
			strContents += "<DriverId>" + uTransactionInfo.strDriverId + "</DriverId>";
			strContents += "<DriverNumber>" + uTransactionInfo.strDriverNumber + "</DriverNumber>";
			strContents += "<DriverName>" + uTransactionInfo.strDriverName + "</DriverName>";
			strContents += "</Transaction>";
			strContents += "</Transactions>";
			writeStringAsFile(strContents, strUploadDir, strTransactionsFileName);
			List<String> fileList = new ArrayList<>();
			fileList.add(strGPSFileName);
			fileList.add(strTransactionsFileName);
            String strZipFileName = "upload" + strSuffix + ".zip";
            int nIdx = 1;
            while(Utils.isFileExist(strZipFileName))
            {
                strZipFileName = "upload" + strSuffix + "(" + nIdx + ").zip";
            }
            makeZipFile(context, fileList, strZipFileName);

			final String strFTPServerIP = Utils.getStringSetting(context, Global.PREF_FTP_SERVER_IP);
			final String strFTPCompanyName = Utils.getStringSetting(context, Global.PREF_FTP_COMPANY_NAME);
			final String strFTPUserName = Utils.getStringSetting(context, Global.PREF_FTP_USERNAME);
			//final String strLocalFilePath = String.format(Locale.ENGLISH, "%s%s/%s", Global.UPLOAD_DIR, Global.gRouteNumber, strZipFileName);
			final String strLocalFilePath = Global.UPLOAD_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(context, Global.PREF_LOGIN_DATE) + "/" + strZipFileName;
			final String strServerFilePath = String.format(Locale.ENGLISH, Global.FTP_UPLOAD_PATH, strFTPCompanyName, Global.gRouteNumber, strZipFileName);


			final long fileSize = Utils.getFileSize(strLocalFilePath);
			new Thread() {
				@Override
				public void run() {
					BackupDbAdapter backupDbAdapter = new BackupDbAdapter(context);
					backupDbAdapter.open();
					backupDbAdapter.insertUploadFile(strLocalFilePath, strServerFilePath, Utils.getStringSetting(context, Global.PREF_LOGIN_DATE), Global.gRouteNumber);
					backupDbAdapter.close();

					if (fileSize == 0) {
						processGPSUploadDataFromDB(context);
						return;
					} else if (new UploadViaHTTP().uploadFileToFTPServerUsingHttp(strFTPCompanyName, strFTPServerIP, strFTPUserName, strLocalFilePath, strServerFilePath)) {
						processGPSUploadDataFromDB(context);
						return;
					} else if (!Utils.getStringSetting(context, Global.PREF_DisableFTP).equals("1") && Utils.uploadFileToFTPServer(context, strLocalFilePath, strServerFilePath)) {
						processGPSUploadDataFromDB(context);
						return;
					} else {
						try
						{
							//Register this to database.
							GPSUploadDbAdapter dbAdapter = new GPSUploadDbAdapter(context);
							dbAdapter.open();
							dbAdapter.insertUploadFile(strLocalFilePath, strServerFilePath);
							dbAdapter.close();
						}catch (Exception e)
						{
							e.printStackTrace();
						}
						return;
					}
				}
			}.start();
		} else {
			Utils.registerChaseLog("Not Logged", lCurTime);
		}
	}

	private static boolean _gUploadingGPSData = false;

	public static void processGPSUploadDataFromDB(Context context) {
		if (_gUploadingGPSData == true) return;
		_gUploadingGPSData = true;
		GPSUploadDbAdapter dbAdapter = new GPSUploadDbAdapter(context);
		dbAdapter.open();
		List<UploadFileInfo> uploadFileInfoList = dbAdapter.getUploadFileList();
		String strFTPServerIP = Utils.getStringSetting(context, Global.PREF_FTP_SERVER_IP);
		String strFTPCompanyName = Utils.getStringSetting(context, Global.PREF_FTP_COMPANY_NAME);
		String strFTPUserName = Utils.getStringSetting(context, Global.PREF_FTP_USERNAME);

		for (UploadFileInfo uploadFileInfo : uploadFileInfoList) {
			if (new UploadViaHTTP().uploadFileToFTPServerUsingHttp(strFTPCompanyName, strFTPServerIP, strFTPUserName, uploadFileInfo.localPath, uploadFileInfo.serverPath)) {
				dbAdapter.deleteUploadFile(uploadFileInfo.idx);
			}
			else if(!Utils.getStringSetting(context, Global.PREF_DisableFTP).equals("1") && Utils.uploadFileToFTPServer(context, uploadFileInfo.localPath, uploadFileInfo.serverPath)) {
				dbAdapter.deleteUploadFile(uploadFileInfo.idx);
			}
		}
		dbAdapter.close();
		_gUploadingGPSData = false;
	}

	// Trigger new location updates at interval
	static LocationRequest locationRequest = null;

	public static void startLocationUpdates(final Context context) {

		// Create the location request to start receiving updates
		if (locationRequest == null) {
			locationRequest = new LocationRequest();
			locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			locationRequest.setInterval(10 * 1000);
			locationRequest.setFastestInterval(2 * 1000);

			// Create LocationSettingsRequest object using location request
			LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
			builder.addLocationRequest(locationRequest);
			LocationSettingsRequest locationSettingsRequest = builder.build();

			// Check whether location settings are satisfied
			// https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
			SettingsClient settingsClient = LocationServices.getSettingsClient(context);
			settingsClient.checkLocationSettings(locationSettingsRequest);

			try {
				if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					// TODO: Consider calling
					//    ActivityCompat#requestPermissions
					// here to request the missing permissions, and then overriding
					//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
					//                                          int[] grantResults)
					// to handle the case where the user grants the permission. See the documentation
					// for ActivityCompat#requestPermissions for more details.
					return;
				}
				getFusedLocationProviderClient(context).requestLocationUpdates(locationRequest, new LocationCallback() {
							@Override
							public void onLocationResult(LocationResult locationResult) {
								// do work here
								onLocationChanged(locationResult.getLastLocation(), context);
							}
						},
						null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static void onLocationChanged(final Location location, final Context context) {
		// New location has now been determined
		if (location != null) {

			Utils.setDoubleSetting(context, Global.PREF_MY_LATITUDE, location.getLatitude());
			Utils.setDoubleSetting(context, Global.PREF_MY_LONGITUDE, location.getLongitude());
			long lCurTime = System.currentTimeMillis();
			location.setTime(lCurTime);
			Utils.registerGPSLog(location.getProvider(), location);
			if (location.getAccuracy() <= 300) //Do Process if accuracy is lower than 300 m
			{
				//Compare two points are equal or not
				if (Utils.lastRecordedLocation != null) {
					float distance = Utils.lastRecordedLocation.distanceTo(location);
					if (distance <= Utils.lastRecordedLocation.getAccuracy()
							|| distance <= location.getAccuracy()) {
						location.set(Utils.lastRecordedLocation);
						location.setTime(lCurTime);
					}
				}

				Utils.processGPSRecordedData(context, Utils.lastRecordedLocation, location);
				Utils.lastRecordedLocation = location;
				Utils.registerLastGPSLog(Utils.lastRecordedLocation.getProvider(), Utils.lastRecordedLocation);
			}
		}
	}

	public static void recordGPSUploadTime(Context context, long lTime) {
		if (Global.gSignedOn) {
			GPSRecordDbAdapter dbAdapter = new GPSRecordDbAdapter(context);
			dbAdapter.open();
			dbAdapter.insertTime(lTime);
			dbAdapter.close();
		}
	}

	public static void getRoutePath(Location lLocation, Location location) {
		final GPSPath path = new GPSPath();
		//If two points are same, there is no need to process it
		if (lLocation.getLongitude() != location.getLongitude()
				|| lLocation.getLatitude() != location.getLatitude()) {
			JsonObject params = new JsonObject();

			JsonObject origin = new JsonObject();
			JsonObject latLng1 = new JsonObject();
			latLng1.addProperty("latitude", lLocation.getLatitude());
			latLng1.addProperty("longitude", lLocation.getLongitude());

			JsonObject location1 = new JsonObject();
			location1.add("latLng", latLng1);
			origin.add("location", location1);
			params.add("origin", origin);

			JsonObject destination = new JsonObject();
			JsonObject latLng2 = new JsonObject();
			latLng2.addProperty("latitude", location.getLatitude());
			latLng2.addProperty("longitude", location.getLongitude());

			JsonObject location2 = new JsonObject();
			location2.add("latLng", latLng2);
			destination.add("location", location2);
			params.add("destination", destination);

			params.addProperty("travelMode", "DRIVE");

			//final CountDownLatch latch = new CountDownLatch(1);
			RouteApi.getApiClient().computeRoutes("application/json", "AIzaSyApquLOSxeBRnCuWR0guRL1iuMRHDBbbsU", "*", params).enqueue(new retrofit.Callback<JsonObject>() {
				@Override
				public void onResponse(retrofit.Response<JsonObject> response, Retrofit retrofit) {
					try {
						JsonObject pathResponse = response.body();
						if (pathResponse != null) {
							JsonArray routes = pathResponse.getAsJsonArray("routes");
							if (routes != null && routes.size() > 0) {
								JsonObject firstRoute = routes.get(0).getAsJsonObject();
								JsonArray legs = firstRoute.getAsJsonArray("legs");
								if (legs != null && legs.size() > 0) {
									JsonObject firstLeg = legs.get(0).getAsJsonObject();
									path.distance = firstLeg.get("distanceMeters").getAsLong();
									path.duration = firstLeg.get("duration").getAsString();

									JsonObject startLocation = firstLeg.get("startLocation").getAsJsonObject();
									JsonObject latLng = startLocation.get("latLng").getAsJsonObject();
									path.start_location = new GPSPath.GPSPosition();
									path.start_location.lat = latLng.get("latitude").getAsDouble();
									path.start_location.lng = latLng.get("longitude").getAsDouble();
									JsonObject endLocation = firstLeg.get("endLocation").getAsJsonObject();
									latLng = endLocation.get("latLng").getAsJsonObject();
									path.end_location = new GPSPath.GPSPosition();
									path.end_location.lat = latLng.get("latitude").getAsDouble();
									path.end_location.lng = latLng.get("longitude").getAsDouble();

									JsonArray steps = firstLeg.getAsJsonArray("steps");
									if (steps != null && steps.size() > 0) {
										for (int i = 0; i < steps.size(); i++) {
											GPSPath subPath = new GPSPath();
											JsonObject step = steps.get(i).getAsJsonObject();

											subPath.distance = step.get("distanceMeters").getAsLong();
											subPath.duration = step.get("staticDuration").getAsString();

											startLocation = step.get("startLocation").getAsJsonObject();
											latLng = startLocation.get("latLng").getAsJsonObject();
											subPath.start_location = new GPSPath.GPSPosition();
											subPath.start_location.lat = latLng.get("latitude").getAsDouble();
											subPath.start_location.lng = latLng.get("longitude").getAsDouble();
											endLocation = step.get("endLocation").getAsJsonObject();
											latLng = endLocation.get("latLng").getAsJsonObject();
											subPath.end_location = new GPSPath.GPSPosition();
											subPath.end_location.lat = latLng.get("latitude").getAsDouble();
											subPath.end_location.lng = latLng.get("longitude").getAsDouble();
											path.pathList.add(subPath);
										}
									}
									//latch.countDown();
								}
								else {
									//latch.countDown();
								}
							}
							else {
								//latch.countDown();
							}
						}
						else {
							//latch.countDown();
						}
					}
					catch (Exception ex) {
						ex.printStackTrace();
						//latch.countDown();
					}
				}

				@Override
				public void onFailure(Throwable t) {
					//latch.countDown();
				}
			});

//			try {
//				latch.await();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
	}

	public static void processGPSRecordedData(final Context context, final Location lLocation, final Location location) {
		if(!Utils.isConnectingToInternet(context))
		{
			return;
		}
		new Thread() {
			@Override
			public void run() {
				//if (lLocation != null && Utils.isServiceExisted(context, Global.SERVICE_CLASS_NAME) != null) {
				if (lLocation != null) {
					GPSRecordDbAdapter dbAdapter = new GPSRecordDbAdapter(context);
					dbAdapter.open();
					List<Long> timeList = dbAdapter.getRecordTimeList(lLocation.getTime(), location.getTime());
					dbAdapter.close();
					GPSLocation gpsLocation = convertToGPSLocation(location);
					if (timeList.size() > 0) {
						final GPSPath path = new GPSPath();
						//If two points are same, there is no need to process it
						if (lLocation.getLongitude() != location.getLongitude()
								|| lLocation.getLatitude() != location.getLatitude()) {

							//Get The Path Data from the server.
							JsonObject params = new JsonObject();
							JsonObject origin = new JsonObject();
							JsonObject latLng1 = new JsonObject();
							latLng1.addProperty("latitude", lLocation.getLatitude());
							latLng1.addProperty("longitude", lLocation.getLongitude());

							JsonObject location1 = new JsonObject();
							location1.add("latLng", latLng1);
							origin.add("location", location1);
							params.add("origin", origin);

							JsonObject destination = new JsonObject();
							JsonObject latLng2 = new JsonObject();
							latLng2.addProperty("latitude", location.getLatitude());
							latLng2.addProperty("longitude", location.getLongitude());

							JsonObject location2 = new JsonObject();
							location2.add("latLng", latLng2);
							destination.add("location", location2);
							params.add("destination", destination);

							params.addProperty("travelMode", "DRIVE");

							final CountDownLatch latch = new CountDownLatch(1);
							RouteApi.getApiClient().computeRoutes("application/json", "AIzaSyApquLOSxeBRnCuWR0guRL1iuMRHDBbbsU", "*", params).enqueue(new retrofit.Callback<JsonObject>() {
								@Override
								public void onResponse(retrofit.Response<JsonObject> response, Retrofit retrofit) {
									try {
										JsonObject pathResponse = response.body();
										if (pathResponse != null) {
											JsonArray routes = pathResponse.getAsJsonArray("routes");
											if (routes != null && routes.size() > 0) {
												JsonObject firstRoute = routes.get(0).getAsJsonObject();
												JsonArray legs = firstRoute.getAsJsonArray("legs");
												if (legs != null && legs.size() > 0) {
													JsonObject firstLeg = legs.get(0).getAsJsonObject();
													path.distance = firstLeg.get("distanceMeters").getAsLong();
													path.duration = firstLeg.get("duration").getAsString();

													JsonObject startLocation = firstLeg.get("startLocation").getAsJsonObject();
													JsonObject latLng = startLocation.get("latLng").getAsJsonObject();
													path.start_location = new GPSPath.GPSPosition();
													path.start_location.lat = latLng.get("latitude").getAsDouble();
													path.start_location.lng = latLng.get("longitude").getAsDouble();
													JsonObject endLocation = firstLeg.get("endLocation").getAsJsonObject();
													latLng = endLocation.get("latLng").getAsJsonObject();
													path.end_location = new GPSPath.GPSPosition();
													path.end_location.lat = latLng.get("latitude").getAsDouble();
													path.end_location.lng = latLng.get("longitude").getAsDouble();

													JsonArray steps = firstLeg.getAsJsonArray("steps");
													if (steps != null && steps.size() > 0) {
														for (int i = 0; i < steps.size(); i++) {
															GPSPath subPath = new GPSPath();
															JsonObject step = steps.get(i).getAsJsonObject();

															subPath.distance = step.get("distanceMeters").getAsLong();
															subPath.duration = step.get("staticDuration").getAsString();

															startLocation = step.get("startLocation").getAsJsonObject();
															latLng = startLocation.get("latLng").getAsJsonObject();
															subPath.start_location = new GPSPath.GPSPosition();
															subPath.start_location.lat = latLng.get("latitude").getAsDouble();
															subPath.start_location.lng = latLng.get("longitude").getAsDouble();
															endLocation = step.get("endLocation").getAsJsonObject();
															latLng = endLocation.get("latLng").getAsJsonObject();
															subPath.end_location = new GPSPath.GPSPosition();
															subPath.end_location.lat = latLng.get("latitude").getAsDouble();
															subPath.end_location.lng = latLng.get("longitude").getAsDouble();
															path.pathList.add(subPath);
														}
													}
													latch.countDown();
												}
												else {
													latch.countDown();
												}
											}
											else {
												latch.countDown();
											}
										}
										else {
											latch.countDown();
										}
									}
									catch (Exception ex) {
										ex.printStackTrace();
										latch.countDown();
									}
								}

								@Override
								public void onFailure(Throwable t) {
									latch.countDown();
								}
							});
							try {
								latch.await();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						GPSLocation lastRecordGPSLocation = convertToGPSLocation(lLocation);
						for (Long time : timeList) {
							GPSLocation curGPSLocation;
							if (path.distance == 0) {
								curGPSLocation = getCalcGPSLocation(lastRecordGPSLocation, gpsLocation, time);
							} else {
								curGPSLocation = getCalcGPSLocationOnPath(lastRecordGPSLocation, gpsLocation, path, time);
							}

							double lat = curGPSLocation.latitude;
							double lng = curGPSLocation.longitude;

							Date curDate = new Date(time);
							UGPSInfo uGPSInfo = new UGPSInfo();
							uGPSInfo.nTrxnNo = time;
							uGPSInfo.nChainNo = 0;
							uGPSInfo.nCustNo = Global.GPS_CUST_NO;
							uGPSInfo.nStartTrxnNo = 0;
							if (lat != 0 && lng != 0) {
								uGPSInfo.strGPSData = String.format(Locale.ENGLISH, "%f, %f", lat, lng);
							} else {
								uGPSInfo.strGPSData = "ERROR: NO GPS";
							}
							uGPSInfo.strDocType = "GPS";
							uGPSInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(curDate);
							uGPSInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(curDate);

							UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
							uTransactionInfo.nTrxnNo = uGPSInfo.nTrxnNo;
							uTransactionInfo.strDocType = "GPS";
							uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(context, new Date(uGPSInfo.nTrxnNo)));
							uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(uGPSInfo.nTrxnNo);
							uTransactionInfo.nChainNo = 0;
							uTransactionInfo.nCustNo = Global.GPS_CUST_NO;

							//Create Data
							//String strUploadDir = Global.UPLOAD_DIR + Global.gRouteNumber + "/";
							String strUploadDir = Global.UPLOAD_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(context, Global.PREF_LOGIN_DATE) + "/";
							if (Utils.createDirectory(strUploadDir) == false) {
								return;
							}
							String strSuffix = uTransactionInfo.strTrxnDate + uTransactionInfo.strTrxnTime;
							String strGPSFileName = "GPS" + strSuffix + ".upl";

							String strContents = "<GPS>";
							strContents += "<GPSLog>";
							strContents += "<TrxnNo>" + uGPSInfo.nTrxnNo + "</TrxnNo>";
							strContents += "<ChainNo>" + uGPSInfo.nChainNo + "</ChainNo>";
							strContents += "<CustNo>" + (uGPSInfo.nCustNo != Global.GPS_CUST_NO ? uGPSInfo.nCustNo : 0) + "</CustNo>";
							strContents += "<StartDate>" + uGPSInfo.strTrxnDate + "</StartDate>";
							strContents += "<StartTime>" + uGPSInfo.strTrxnTime + "</StartTime>";
							strContents += "<EndDate>" + uGPSInfo.strTrxnDate + "</EndDate>";
							strContents += "<EndTime>" + uGPSInfo.strTrxnTime + "</EndTime>";
							if (uGPSInfo.nStartTrxnNo == 0) {
								strContents += "<StartTrxnNo>" + "" + "</StartTrxnNo>";
							} else {
								strContents += "<StartTrxnNo>" + uGPSInfo.nStartTrxnNo + "</StartTrxnNo>";
							}
							strContents += "<GPSData>" + uGPSInfo.strGPSData + "</GPSData>";
							strContents += "<DocNo>" + "0" + "</DocNo>";
							strContents += "<DocType>" + uGPSInfo.strDocType + "</DocType>";
							strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
							strContents += "<PrintedFlag>" + "0" + "</PrintedFlag>";
							strContents += "<TrxnDate>" + uGPSInfo.strTrxnDate + "</TrxnDate>";
							strContents += "<TrxnTime>" + uGPSInfo.strTrxnTime + "</TrxnTime>";
							strContents += "<Reference>" + "" + "</Reference>";
							strContents += "<TCOMStatus>" + "0" + "</TCOMStatus>";
							strContents += "<SaleDate>" + uGPSInfo.strTrxnDate + "</SaleDate>";
							strContents += "<Trip>" + Global.gTripNumber + "</Trip>";
							strContents += "</GPSLog>";
							strContents += "</GPS>";
							writeStringAsFile(strContents, strUploadDir, strGPSFileName);

							// write to Transactions.upl

							String strTransactionsFileName = "Transactions" + strSuffix + ".upl";
							strContents = "<Transactions>";

							strContents += "<Transaction>";
							strContents += "<TrxnNo>" + uTransactionInfo.nTrxnNo + "</TrxnNo>";
							strContents += "<DocNo>" + "0" + "</DocNo>";
							strContents += "<DocType>" + uTransactionInfo.strDocType + "</DocType>";
							strContents += "<VoidFlag>" + "0" + "</VoidFlag>";
							strContents += "<PrintedFlag>" + "0" + "</PrintedFlag>";
							strContents += "<TrxnDate>" + uTransactionInfo.strTrxnDate + "</TrxnDate>";
							strContents += "<TrxnTime>" + uTransactionInfo.strTrxnTime + "</TrxnTime>";
							strContents += "<Reference>" + uTransactionInfo.strReference + "</Reference>";
							strContents += "<TCOMStatus>" + "0" + "</TCOMStatus>";
							strContents += "<ChainNo>" + uTransactionInfo.nChainNo + "</ChainNo>";
							strContents += "<CustNo>" + (uTransactionInfo.nCustNo != Global.GPS_CUST_NO ? uTransactionInfo.nCustNo : 0) + "</CustNo>";
							strContents += "<SaleDate>" + uTransactionInfo.strTrxnDate + "</SaleDate>";
							strContents += "<PhysAvail>" + "0" + "</PhysAvail>";
							strContents += "<VirtAvail>" + "0" + "</VirtAvail>";
							strContents += "<Trip>" + Global.gTripNumber + "</Trip>";
							strContents += "<TripId>" + uTransactionInfo.strTripId + "</TripId>";
							strContents += "<DriverId>" + uTransactionInfo.strDriverId + "</DriverId>";
							strContents += "<DriverNumber>" + uTransactionInfo.strDriverNumber + "</DriverNumber>";
							strContents += "<DriverName>" + uTransactionInfo.strDriverName + "</DriverName>";
							strContents += "</Transaction>";
							strContents += "</Transactions>";
							writeStringAsFile(strContents, strUploadDir, strTransactionsFileName);
							List<String> fileList = new ArrayList<>();
							fileList.add(strGPSFileName);
							fileList.add(strTransactionsFileName);

                            String strZipFileName = "upload" + strSuffix + ".zip";
                            int nIdx = 1;
                            while(Utils.isFileExist(strZipFileName))
                            {
                                strZipFileName = "upload" + strSuffix + "(" + nIdx + ").zip";
                            }
                            makeZipFile(context, fileList, strZipFileName);



							final String strFTPCompanyName = Utils.getStringSetting(context, Global.PREF_FTP_COMPANY_NAME);
							//final String strLocalFilePath = String.format(Locale.ENGLISH, "%s%s/%s", Global.UPLOAD_DIR, Global.gRouteNumber, strZipFileName);
							final String strLocalFilePath = Global.UPLOAD_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(context, Global.PREF_LOGIN_DATE) + "/" + strZipFileName;
							final String strServerFilePath = String.format(Locale.ENGLISH, Global.FTP_UPLOAD_PATH, strFTPCompanyName, Global.gRouteNumber, strZipFileName);

							GPSUploadDbAdapter gpsUploadDbAdapter = new GPSUploadDbAdapter(context);
							gpsUploadDbAdapter.open();
							gpsUploadDbAdapter.insertUploadFile(strLocalFilePath, strServerFilePath);
							gpsUploadDbAdapter.close();
						}
					}
					processGPSUploadDataFromDB(context);
				}
			}
		}.start();
	}

	public static GPSLocationListener gpsLocationListener = null;
	public static NetworkLocationListener networkLocationListener = null;
	public static Location lastRecordedLocation = null;

	public static void startLocationListeners(Context context) {
		try {
			LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				if (gpsLocationListener == null) {
					gpsLocationListener = new GPSLocationListener(context);
				} else {
					locationManager.removeUpdates(gpsLocationListener);
				}
				if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					// TODO: Consider calling
					//    ActivityCompat#requestPermissions
					// here to request the missing permissions, and then overriding
					//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
					//                                          int[] grantResults)
					// to handle the case where the user grants the permission. See the documentation
					// for ActivityCompat#requestPermissions for more details.
					return;
				}
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, gpsLocationListener);
			} else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				if (networkLocationListener == null) {
					networkLocationListener = new NetworkLocationListener(context);
				} else {
					locationManager.removeUpdates(networkLocationListener);
				}
				if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					// TODO: Consider calling
					//    ActivityCompat#requestPermissions
					// here to request the missing permissions, and then overriding
					//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
					//                                          int[] grantResults)
					// to handle the case where the user grants the permission. See the documentation
					// for ActivityCompat#requestPermissions for more details.
					return;
				}
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 1, networkLocationListener);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void registerGPSLog(String provider, Location location)
	{
		/*Utils.createDirectory(Environment.getExternalStorageDirectory() + "/.DSDDelivery/");
		File file = new File(Environment.getExternalStorageDirectory() + "/.DSDDelivery/gps_track.txt");
		try
		{
			Date date = new Date(location.getTime());
			String recordTime = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(date);
			String strData = String.format(Locale.ENGLISH, "%s %.6f, %.6f, %.6f, %s\n", provider, location.getLatitude(), location.getLongitude(), location.getAccuracy(), recordTime);
			FileOutputStream fos = new FileOutputStream(file, true);
			fos.write(strData.getBytes());
			fos.flush();
			fos.close();
		}catch (Exception e)
		{
			e.printStackTrace();
		}*/
	}

	public static void registerChaseLog(String provider, long time)
	{
		/*Utils.createDirectory(Environment.getExternalStorageDirectory() + "/.DSDDelivery/");
		File file = new File(Environment.getExternalStorageDirectory() + "/.DSDDelivery/Chase.txt");
		try
		{
			Date date = new Date(time);
			String recordTime = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(date);
			String strData = String.format(Locale.ENGLISH, "%s %s\n", provider, recordTime);
			FileOutputStream fos = new FileOutputStream(file, true);
			fos.write(strData.getBytes());
			fos.flush();
			fos.close();
		}catch (Exception e)
		{
			e.printStackTrace();
		}*/
	}

	public static void registerLastGPSLog(String provider, Location location)
	{
		/*Utils.createDirectory(Environment.getExternalStorageDirectory() + "/.DSDDelivery/");
		File file = new File(Environment.getExternalStorageDirectory() + "/.DSDDelivery/last_gps_track.txt");
		try
		{
			Date date = new Date(location.getTime());
			String recordTime = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(date);
			String strData = String.format(Locale.ENGLISH, "%s %.6f, %.6f, %.6f, %s\n", provider, location.getLatitude(), location.getLongitude(), location.getAccuracy(), recordTime);
			FileOutputStream fos = new FileOutputStream(file, true);
			fos.write(strData.getBytes());
			fos.flush();
			fos.close();
		}catch (Exception e)
		{
			e.printStackTrace();
		}*/
	}

	public static GPSLocation getCalcGPSLocation(GPSLocation sLoc, GPSLocation eLoc, long time)
	{
		GPSLocation location = new GPSLocation();
		location.time = time;
		location.latitude = sLoc.latitude + (eLoc.latitude - sLoc.latitude) / (eLoc.time - sLoc.time) * (time - sLoc.time);
		location.longitude = sLoc.longitude + (eLoc.longitude - sLoc.longitude) / (eLoc.time - sLoc.time) * (time - sLoc.time);
		return location;
	}

	public static GPSLocation getCalcGPSLocationOnPath(GPSLocation sLoc, GPSLocation eLoc, GPSPath path, long time)
	{
		long duration = eLoc.time - sLoc.time;
		double speed = path.distance * 1.0f / duration;
		long distance = (long)((time - sLoc.time) * speed);
		long sDistance = 0, eDistance = 0 ;
		GPSPath selPath = null;
		for(int i = 0; i < path.pathList.size(); i++)
		{
			eDistance = sDistance + path.pathList.get(i).distance;
			if(distance >= sDistance && distance <= eDistance)
			{
				selPath = path.pathList.get(i);
				break;
			}
			sDistance = eDistance;
		}

		if(selPath == null)
		{
			return getCalcGPSLocation(sLoc, eLoc, time);
		}

		long startTime = sLoc.time;

		sLoc = new GPSLocation();
		sLoc.time = startTime + ((long)(sDistance * 1.0f / speed));
		sLoc.longitude = selPath.start_location.lng;
		sLoc.latitude = selPath.start_location.lat;

		eLoc = new GPSLocation();
		eLoc.time = startTime + ((long)(eDistance * 1.0f / speed));
		eLoc.longitude = selPath.end_location.lng;
		eLoc.latitude = selPath.end_location.lat;

		return getCalcGPSLocation(sLoc, eLoc, time);
	}

	public static GPSLocation convertToGPSLocation(Location location)
	{
		GPSLocation gpsLocation = new GPSLocation();
		gpsLocation.time = location.getTime();
		gpsLocation.latitude = location.getLatitude();
		gpsLocation.longitude = location.getLongitude();
		return gpsLocation;
	}

	public static String getBaseURL(String strPinNumber)
	{
		if(TextUtils.isEmpty(strPinNumber)) return "";
		return String.format(Locale.ENGLISH, Global.API_BASE_PATH, strPinNumber.toLowerCase());
	}

	public static void buildBaseURLMap()
	{
		Global.URL_BASE_MAP.clear();
		int key = 1;
		for(char a = 'a'; a <= 'z'; a++, key++)
		{
			String strValue = "" + a;
			String strKey = String.format(Locale.ENGLISH, "%02d", key);
			Global.URL_BASE_MAP.put(strKey, strValue);
		}
	}

	public static void reportUpgradeStatus(final String strCompany, final String strUserName, final String strTripNo, final String newNumber, final String oldNumber, final boolean bUpgrade)
	{
		new Thread()
		{
			@Override
			public void run() {
				Date date = new Date();
				Map<String, String> params = new HashMap<>();
				params.put("COMPANY", strCompany);
				params.put("USER_NAME", strUserName);
				params.put("TRIP_NUMBER", strTripNo);
				params.put("NEW_VERSION", newNumber);
				params.put("OLD_VERSION", oldNumber);
				params.put("DATE", new SimpleDateFormat("yyyy-MM-dd").format(date));
				params.put("TIME", new SimpleDateFormat("kk:mm:ss").format(date));
				params.put("UPGRADED", bUpgrade ? "1" : "0");
				ApiClient.getApiClient().RegisterUpgradeInfo(params).enqueue(new Callback<BaseResult>() {
					@Override
					public void onResponse(Call<BaseResult> call, Response<BaseResult> response) {

					}

					@Override
					public void onFailure(Call<BaseResult> call, Throwable t) {

					}
				});
			}
		}.start();
	}

	public static void stopMainService(Context context)
	{
		context.stopService(new Intent(context, MainService.class));
	}

	public static void resetLoginCount()
	{
		createDirectory(Global.INSTALL_PATH);
		File file = new File(Global.INSTALL_PATH + "VSLS");
		try
		{
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(new Integer(0).toString().getBytes());
			fos.flush();
			fos.close();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static int getLoginCount()
	{
		createDirectory(Global.INSTALL_PATH);
		File file = new File(Global.INSTALL_PATH + "VSLS");
		int nLoginCount = 0;
		try
		{
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[(int)file.length()];
			fis.read(buffer);
			String strValue = new String(buffer);
			nLoginCount = Utils.parseIntFromString(strValue);
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		return nLoginCount;
	}

	public static void setLoginCount(int nCnt)
	{
		createDirectory(Global.INSTALL_PATH);
		File file = new File(Global.INSTALL_PATH + "VSLS");
		try
		{
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(new Integer(nCnt).toString().getBytes());
			fos.flush();
			fos.close();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void markUpgradeAsk(String strMark)
	{
		createDirectory(Global.INSTALL_PATH);
		File file = new File(Global.INSTALL_PATH + "DSDVanSalesUpgraded");
		try
		{
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(strMark.getBytes());
			fos.flush();
			fos.close();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static boolean isMarkedFromEndTrip()
	{
		File file = new File(Global.INSTALL_PATH + "DSDVanSalesUpgraded");
		byte[] buffer = new byte[(int)file.length()];
		try
		{
			FileInputStream fis = new FileInputStream(file);
			fis.read(buffer);
			fis.close();
			String strMark = new String(buffer);
			if(strMark.equals("EndTrip"))
			{
				return true;
			}
			else
			{
				return false;
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	public static boolean isMarkedUpgradeAsk()
	{
		return isFileExist(Global.INSTALL_PATH + "DSDVanSalesUpgraded");
	}

	public static void unmarkUpgradeAsk()
	{
		fileDelete(Global.INSTALL_PATH + "DSDVanSalesUpgraded");
	}

	public static void writeTripInfo(String strTripNumber)
	{
		createDirectory(Global.INSTALL_PATH);
		File file = new File(Global.INSTALL_PATH + "DSDVanSales.ini");
		try
		{
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(strTripNumber.getBytes());
			fos.flush();
			fos.close();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static String readTripInfo()
	{
		if(isFileExist(Global.INSTALL_PATH + "DSDVanSales.ini"))
		{
			try
			{
				File file = new File(Global.INSTALL_PATH + "DSDVanSales.ini");
				FileInputStream fis = new FileInputStream(file);
				byte[] dataArray = new byte[(int)file.length()];
				fis.read(dataArray);
				String strTripNumber = new String(dataArray);
				return strTripNumber;
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return "";
	}

	private static String _scaling_sufix = null;
	public static String getScalingSuffix(Context context)
	{
		if(_scaling_sufix == null) {
			int nScalingValue = getIntSetting(context, Global.PREF_SCALING);
			_scaling_sufix = "";
			for (int i = 0; i < nScalingValue; i++) {
				_scaling_sufix += "0";
			}
		}
		return _scaling_sufix;
	}

	public static void resetScalingSufix()
	{
		_scaling_sufix = null;
	}

	public static PackageInfo isInstalledInstallerApp(Context context){
		String uri = "dsd.assist";
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo info = pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			return info;
		} catch (PackageManager.NameNotFoundException e) {
		}

		return null;
	}

	public static void deleteBackupExpiredData(Context context)
	{
		//Delete files
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, -7);
		String strCurDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
		File file = new File(Global.UPLOAD_DIR);
		if(!file.exists()) return;
		File[] fileList = file.listFiles();
		for(File routeDir : fileList)
		{
			if(routeDir.isDirectory())
			{
				File[] dateDirList = routeDir.listFiles();
				for(File dateDir : dateDirList)
				{
					if(dateDir.isDirectory() && dateDir.getName().compareTo(strCurDate) < 0)
					{
						deleteDirectory(dateDir.getAbsolutePath());
					}
				}
			}
		}

		BackupDbAdapter backupDbAdapter = new BackupDbAdapter(context);
		backupDbAdapter.open();
		backupDbAdapter.deleteExpiredData();
		backupDbAdapter.close();
	}

	/*public static void writeAsJson(String strText, boolean bRequest)
	{
		createDirectory(Global.ROOT_LOCATION + "/json");
		File file = new File(Global.ROOT_LOCATION + "/json/" + System.currentTimeMillis() + "_" + (bRequest ? "request" : "response") + ".json");
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(strText.getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	public static float get2DecimalValue(float fValue)
	{
		try
		{
			String strValue = String.format(Locale.ENGLISH, "%.2f", fValue);
			float value = Float.parseFloat(strValue.replace(",", "."));
			if( value == 0.0f ) value = 0;
			return value;
		}catch (Exception e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	public static void registerUploadFileAsTemp(Context context, MakeZipFileInfo info)
	{
		try
		{
			PrepareUploadDbAdapter dbAdapter = new PrepareUploadDbAdapter(context);
			dbAdapter.open();
			long idx = dbAdapter.insertUploadFile(info);
			dbAdapter.close();
			Global.gUploadTempList.add(idx);
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void doProcessInTemp(Context context)
	{
		if(Global.gUploadTempList.size() > 0)
		{
			Map<String, MakeZipFileInfo> makeZipFileInfoMap = new HashMap<>();
			PrepareUploadDbAdapter dbAdapter = new PrepareUploadDbAdapter(context);
			dbAdapter.open();
			for(Long idx : Global.gUploadTempList)
			{
				UploadInfo uploadInfo = dbAdapter.getMakeZipFileInfo(idx);
				if(uploadInfo != null && uploadInfo.makeZipFileInfo != null)
				{
					MakeZipFileInfo makeZipFileInfo = uploadInfo.makeZipFileInfo;
					MakeZipFileInfo data = makeZipFileInfoMap.get(makeZipFileInfo.nCustNo + " && " + makeZipFileInfo.nChainNo);
					if(data == null)
					{
						makeZipFileInfoMap.put(makeZipFileInfo.nCustNo + " && " + makeZipFileInfo.nChainNo, makeZipFileInfo);
					}
					else
					{
						data.trxnList.addAll(makeZipFileInfo.trxnList);
					}
					dbAdapter.deleteUploadFileInfo(idx);
				}
			}
			for (Map.Entry<String, MakeZipFileInfo> entry : makeZipFileInfoMap.entrySet()) {
				MakeZipFileInfo zipFileInfo = entry.getValue();
				long idx = dbAdapter.insertUploadFile(zipFileInfo);
				UploadInfo uploadInfo = new UploadInfo();
				uploadInfo.idx = idx;
				uploadInfo.makeZipFileInfo = zipFileInfo;
				Global.gUploadCustNoList.add(uploadInfo);
			}

			dbAdapter.close();
		}
		Global.gUploadTempList.clear();
	}

	public static void registerUploadFile(Context context, MakeZipFileInfo info)
    {
        PrepareUploadDbAdapter dbAdapter = new PrepareUploadDbAdapter(context);
        dbAdapter.open();
        long idx = dbAdapter.insertUploadFile(info);
        dbAdapter.close();
        UploadInfo uploadInfo = new UploadInfo();
        uploadInfo.idx = idx;
        uploadInfo.makeZipFileInfo = info;
        Global.gUploadCustNoList.add(uploadInfo);
    }

    public static void unregisterUploadFile(Context context, UploadInfo info)
	{
		PrepareUploadDbAdapter dbAdapter = new PrepareUploadDbAdapter(context);
		dbAdapter.open();
		dbAdapter.deleteUploadFileInfo(info.idx);
		dbAdapter.close();

		Global.gUploadCustNoList.remove(info);
	}

	public static void leaveLog(Context context, String tag, UploadFileInfo fileInfo, String reason)
	{
		if (Utils.getStringSetting(context, Global.PREF_DISABLE_UPLOAD_LOG).equals("1")) {
			return;
		}

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/UploadLog.txt");
        boolean bExist = file.exists();
        try {
            FileOutputStream fOut = new FileOutputStream(file, true);
            if(!bExist)
            {
                String strTitle = "Tag\tUploaded Date\tLocal Path\tServer Path\tUpload Status\nFileSize\n";
                fOut.write(strTitle.getBytes());
            }
            if (fileInfo != null) {
                File localFile = new File(fileInfo.localPath);

                long fileSize = 0;
                try
                {
                    fileSize = localFile.length();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                String strOut = tag + "\t" + new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(new Date()) + "\t" + fileInfo.localPath + "\t" + fileInfo.serverPath + "\t" + reason + "\t" + fileSize + "\n";
                fOut.write(strOut.getBytes());
                fOut.close();
            }
            else {
                String strOut = tag + "\t" + new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(new Date()) + "\t" + "Not File" + "\t" + "Not File" + "\t" + reason + "\t" + " " + "\n";
                fOut.write(strOut.getBytes());
                fOut.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	public static int getCustomerColorWithPresoldH(List<PresoldOrHInfo> presoldOrHInfoList)
	{
		int nColor = 0;
		if(presoldOrHInfoList != null && presoldOrHInfoList.size() > 0)
		{
			for(PresoldOrHInfo presoldOrHInfo : presoldOrHInfoList)
			{
				int nCurColor = 0;
				if(presoldOrHInfo != null &&
						(presoldOrHInfo.strType.equals("P") || presoldOrHInfo.strType.equals("D") || presoldOrHInfo.strType.equals("F"))
						&& presoldOrHInfo.strOrderNo.length() > 0 && !presoldOrHInfo.strOrderNo.equals("0"))
				{
					nCurColor = 3;
				}
				else if(presoldOrHInfo != null && presoldOrHInfo.strType.equals("V"))
				{
					nCurColor = 2;
				}
				else if(presoldOrHInfo != null && presoldOrHInfo.strType.equals("N"))
				{
					nCurColor = 1;
				}
				if(nCurColor > nColor)
				{
					nColor = nCurColor;
				}
			}
		}
		return nColor;
	}

	public static void registerLoginStatus(Context context, int nStatus)
	{
		String strUserName = Utils.getStringSetting(context, Global.PREF_LOGIN_USER_NAME);
		String strTripNumber = Utils.getStringSetting(context, Global.PREF_TRIP_NUMBER);
		LoginDbAdapter dbAdapter = new LoginDbAdapter(context);
		dbAdapter.open();
		dbAdapter.registerLogin(strUserName, strTripNumber, nStatus);
		dbAdapter.close();
	}

	public static int getLoginStatusCount(Context context, String strUserName, String strTrip, int nStatus)
	{
		String strDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		LoginDbAdapter dbAdapter = new LoginDbAdapter(context);
		dbAdapter.open();
		int nCount = dbAdapter.getLoginCount(strDate, strUserName, strTrip, nStatus);
		dbAdapter.close();
		return nCount;
	}

	public static int getLoginStatusCount(Context context, String strTrip, int nStatus)
	{
		String strDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		LoginDbAdapter dbAdapter = new LoginDbAdapter(context);
		dbAdapter.open();
		int nCount = dbAdapter.getLoginCount(strDate, strTrip, nStatus);
		dbAdapter.close();
		return nCount;
	}

	public static String getDateFormat(Context context, String strDateFormat)
	{
		String strDateFmt = Utils.getStringSetting(context, Global.PREF_DATEFMT);
		if(strDateFormat.equals(Global.DATE_DD_MM_YY_FORMAT))
		{
			if(TextUtils.isEmpty(strDateFmt) || strDateFmt.equals("dmy"))
			{
				return strDateFormat;
			}
			else if(strDateFmt.equals("mdy"))
			{
				return "MM/dd/yy";
			}
			else if(strDateFmt.equals("ymd"))
			{
				return "yy/MM/dd";
			}
			else
			{
				return strDateFormat;
			}
		}
		else if(strDateFormat.equals(Global.DATE_DD_MM_YYYY_FORMAT))
		{
			if(TextUtils.isEmpty(strDateFmt) || strDateFmt.equals("dmy"))
			{
				return strDateFormat;
			}
			else if(strDateFmt.equals("mdy"))
			{
				return "MM/dd/yyyy";
			}
			else if(strDateFmt.equals("ymd"))
			{
				return "yyyy/MM/dd";
			}
			else
			{
				return strDateFormat;
			}
		}
		else if (strDateFormat.equals(Global.DATE_YYYYMMDD_FORMAT))
		{
			return strDateFormat;
		}
		else if (strDateFormat.equals("yy/MM/dd"))
		{
			return strDateFormat;
		}
		else
		{
			return strDateFmt;
		}
	}

	public static void writeAsFile(String strText, String strFileName)
    {
        File file = new File(strFileName);
        try
        {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(strText.getBytes());
            fos.close();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String getAppNameFromPackage(String strPackageName)
	{
		String strAppName = "Unknown";
		if(strPackageName.equals("com.dropbox.android"))
		{
			strAppName = "Dropbox";
		}
		else if(strPackageName.equals("com.box.android"))
		{
			strAppName = "BoxStorage";
		}
		else if(strPackageName.equals("com.microsoft.skydrive"))
		{
			strAppName = "OneDrive";
		}
		else if(strPackageName.equals("com.google.android.apps.docs"))
		{
			strAppName = "GoogleDrive";
		}
		return strAppName;
	}

	public static Drawable getAppDrawableIcon(Context context, String strPackageName)
	{
		Drawable drawable;
		String strDrawablePath = null;

		if(strPackageName.equals("com.dropbox.android"))
		{
			strDrawablePath = Global.ROOT_LOCATION + "Dropbox.png";
		}
		else if(strPackageName.equals("com.box.android"))
		{
			strDrawablePath = Global.ROOT_LOCATION + "Box.png";
		}
		else if(strPackageName.equals("com.microsoft.skydrive"))
		{
			strDrawablePath = Global.ROOT_LOCATION + "OneDrive.png";
		}
		else if(strPackageName.equals("com.google.android.apps.docs"))
		{
			strDrawablePath = Global.ROOT_LOCATION + "GoogleDrive.png";
		}

		if(strDrawablePath != null && isFileExist(strDrawablePath))
		{
			//Drawable drawableRed = ContextCompat.getDrawable(context, R.drawable.icon_terms);
			drawable = Drawable.createFromPath(strDrawablePath);
			//drawable.setBounds(0, 0, 50, 50);
		}
		else
		{
			drawable = context.getResources().getDrawable(R.drawable.icon_terms);
		}
		return drawable;
	}

	public static void registerLoadTime(String strIndex, long nTime)
	{
		String strItem = strIndex + "\t" + nTime + "\n";
		File file = new File(Global.ROOT_LOCATION + "loading_time.txt");
		try
		{
			FileOutputStream fos = new FileOutputStream(file, true);
			fos.write(strItem.getBytes());
			fos.close();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

//	public static Thread mDownloadThread = null;
//	public static void runDownloadThread(final String strFTPServerIP, final String strUserName, final String strPassword)
//	{
//		if(mDownloadThread == null)
//		{
//			mDownloadThread = new Thread()
//			{
//				@Override
//				public void run() {
//					UploadFileInfo info;
//					FTPClient ftpClient = null;
//					do
//					{
//						info = null;
//						try
//						{
//							DownloadDbAdapter dbAdapter = new DownloadDbAdapter();
//							dbAdapter.open();
//							info = dbAdapter.getDownloadFile();
//							dbAdapter.close();
//
//							if(ftpClient == null)
//							{
//								try {
//									ftpClient = Utils.connnectingwithFTP(strFTPServerIP, Global.DEFAULT_FTP_PORT, strUserName, strPassword);
//									if (ftpClient == null) {
//										mDownloadThread = null;
//										return;
//									}
//								}catch (Exception e)
//								{
//									e.printStackTrace();
//								}
//							}
//
//
//							if(info != null)
//							{
//								String strParentDir = new File(info.serverPath).getParent();
//								String strFileName = new File(info.serverPath).getName();
//
//								long nRemoteSize = Utils.findFileOnFTPServer(ftpClient, strParentDir, strFileName);
//								if (nRemoteSize != 0) {
//									long nLocalSize = 0;
//									if (Utils.isFileExist(info.localPath) == true) {
//										nLocalSize = new File(info.localPath).length();
//									}
//									if (nLocalSize == 0 || nRemoteSize != nLocalSize) {
//										if (Utils.downloadFileFromFTPServer(ftpClient, info.serverPath, info.localPath) == false) {
//
//										}
//									}
//								}
//
//								dbAdapter.open();
//								dbAdapter.deleteDownloadFile(info.idx);
//								dbAdapter.close();
//							}
//						}catch (Exception e)
//						{
//							e.printStackTrace();
//						}
//
//					}while(info != null);
//
//					if(ftpClient != null)
//					{
//						try {
//							ftpClient.disconnect();
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//					mDownloadThread = null;
//				}
//			};
//			mDownloadThread.start();
//		}
//	}

	public static void errorReport(String where, String strReportString) {
		if (TextUtils.isEmpty(strReportString) || TextUtils.isEmpty(where)) return;

		File file = new File(Global.ERROR_LOG_DIR);
		try {
			String errorString = new SimpleDateFormat("yyyy-MM-dd hh:mm::ss").format(new Date()) + "\t" + where + "\t" + strReportString + "\n";
			FileOutputStream fileOutputStream = new FileOutputStream(file, true);
			fileOutputStream.write(errorString.getBytes());
			fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getCalculatedInvoiceDueDate(Context context, CustomerInfo customerInfo, int nType)
	{
		if(customerInfo == null) return null;
		if(TextUtils.isEmpty(customerInfo.strInvoiceDueMethod) || TextUtils.isEmpty(customerInfo.strInvoiceDueDate)) return null;
		if(customerInfo.strInvoiceDueMethod.equals("0")) return null;

		SimpleDateFormat sdf = null;
		if(nType == 0)
		{
			sdf = new SimpleDateFormat(getDateFormat(context, Global.DATE_DD_MM_YY_FORMAT));
		}
		else if(nType == 1)
		{
			sdf = new SimpleDateFormat("dd MMM");
		}

		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		if(customerInfo.strInvoiceDueMethod.equals("1"))
		{
			calendar.add(Calendar.MONTH, 1);
			date = calendar.getTime();
			date.setDate(Utils.parseIntFromString(customerInfo.strInvoiceDueDate));
			return sdf.format(date);
		}
		else if(customerInfo.strInvoiceDueMethod.equals("2"))
		{
			//date.setDate(Utils.parseIntFromString(customerInfo.strInvoiceDueDate));
			//calendar.setTime(date);
			calendar.add(Calendar.DATE, Utils.parseIntFromString(customerInfo.strInvoiceDueDate));
			date = calendar.getTime();
			return sdf.format(date);
		}
		else if(customerInfo.strInvoiceDueMethod.equals("3"))
		{
			date.setDate(1);
			calendar.setTime(date);
			calendar.add(Calendar.MONTH, 1);
			calendar.add(Calendar.DATE, Utils.parseIntFromString(customerInfo.strInvoiceDueDate) - 1);
			date = calendar.getTime();
			return sdf.format(date);
		}
		else if(customerInfo.strInvoiceDueMethod.equals("4"))
		{
			date.setDate(Utils.parseIntFromString(customerInfo.strInvoiceDueDate));
			return sdf.format(date);
		}
		else
			return null;
	}

	public static MainThread mMainThread = null;
	public static void startMainJob(Context context)
	{
		if(mMainThread != null)
		{
			if(!mMainThread.isAlive() || mMainThread.isInterrupted())
			{
				mMainThread.isRunning = false;
				mMainThread = null;
			}
		}
		if(mMainThread == null)
		{
			mMainThread = new MainThread(context);
			mMainThread.setDaemon(true);
			mMainThread.start();

			/*mGPSPollThread = new GPSPollThread(this);
			mGPSPollThread.start();*/

			//Set Alarm to wakeup system
			Utils.setAlarm(context);
		}

	}

	public static void endMainJob()
	{
		if (mMainThread != null) {
			mMainThread.isRunning = false;
			mMainThread = null;
		}
	}

	public static void loadLang(Context context)
	{
		Gson gson = new Gson();
		Map<String, String> defaultLangSettings = new HashMap<>();
		String strDefaultLang = Utils.getStringSetting(context, Global.PREF_DEFAULT_LANG);
		if(!TextUtils.isEmpty(strDefaultLang))
		{
			defaultLangSettings = gson.fromJson(strDefaultLang, Map.class);
		}
		Map<String, Integer> langIndexMap = new HashMap<>();
		try {
			Field field = Str.class.getDeclaredField("LANGUAGES");
			String[] strLangs = (String[])field.get(null);
			for(int i = 0; i < strLangs.length; i++)
			{
				langIndexMap.put(strLangs[i], i);
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}


		//Set the Default Language first
		for (Map.Entry<String, String> entry : defaultLangSettings.entrySet()) {
			String strKey = entry.getKey();
			String[] strKeyItems = strKey.split("%%");
			String strKeyword = strKeyItems[0];
			String strLang = strKeyItems[1];
			if(langIndexMap.get(strLang) == null) continue;
			int nLangIdx = langIndexMap.get(strLang);
			Field field = null;
			try {
				field = Str.class.getDeclaredField(strKeyword);
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			if(field == null) continue;
			try {
				((String[])field.get(null))[nLangIdx] = entry.getValue();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		//Set the Customize Language
		DbAdapter dbAdapter = new DbAdapter(context);
		dbAdapter.open();
		Map<String, String> customizeLangSettings = dbAdapter.getLangInfoMap();
		dbAdapter.close();

		for (Map.Entry<String, String> entry : customizeLangSettings.entrySet()) {
			String strKey = entry.getKey();
			String[] strKeyItems = strKey.split("%%");
			String strKeyword = strKeyItems[0];
			String strLang = strKeyItems[1];
			if(langIndexMap.get(strLang) == null) continue;
			int nLangIdx = langIndexMap.get(strLang);
			Field field = null;
			try {
				field = Str.class.getDeclaredField(strKeyword);
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			if(field == null) continue;
			try {
				defaultLangSettings.put(strKey, ((String[])field.get(null))[nLangIdx]);
				((String[])field.get(null))[nLangIdx] = entry.getValue();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		Utils.setStringSetting(context, Global.PREF_DEFAULT_LANG, gson.toJson(defaultLangSettings));
	}

	public static void doVoidInvoice(Context context, UOrderHInfo uOrderHInfo)
	{
		//Make The new Reverse UOrderHInfo
		UOrderHInfo newOrderHInfo = new UOrderHInfo();
		DbAdapter dbAdapter = new DbAdapter(context);
		dbAdapter.open();
		uOrderHInfo.deliveryList = dbAdapter.getUOrderDListWithSCWType(uOrderHInfo.nTrxnNo);
		dbAdapter.close();
		newOrderHInfo.copyFrom(uOrderHInfo);
		newOrderHInfo.nIdx = 0;
		newOrderHInfo.nTrxnNo = System.currentTimeMillis();
		newOrderHInfo.strTrxnDate = TimeUtils.getDateTime(newOrderHInfo.nTrxnNo, "yyyyMMdd");
		newOrderHInfo.strTrxnTime = TimeUtils.getDateTime(newOrderHInfo.nTrxnNo, "kkmmss");
		newOrderHInfo.strDeliveryDate = uOrderHInfo.strDeliveryDate;

		//Sales -> Buybacks
		for(UOrderDInfo uOrderDInfo : uOrderHInfo.deliveryList)
		{
			if(uOrderDInfo.nTrxnType == Global.TRXN_DELIVER)
			{
				UOrderDInfo newUOrderDInfo = uOrderDInfo.duplicate();
				newUOrderDInfo.nIdx = 0;
				newUOrderDInfo.nTrxnType = Global.TRXN_BUYBACK;
				newUOrderDInfo.nDumps = uOrderDInfo.nSales;
				newUOrderDInfo.dDumps = uOrderDInfo.dSales;
				newUOrderDInfo.dSales = 0;
				newUOrderDInfo.nTrxnNo = 0;
				boolean bExist = false;
				for(UOrderDInfo uInfo : newOrderHInfo.pickupList)
				{
					if(uInfo.strItemNo.equals(newUOrderDInfo.strItemNo) && uInfo.nTrxnType == Global.TRXN_BUYBACK)
					{
						uInfo.nDumps += newUOrderDInfo.nDumps;
						uInfo.dDumps += newUOrderDInfo.dDumps;
						for(UPromotionInfo promotionInfo : newUOrderDInfo.uPromotionInfoList)
						{
							promotionInfo.nAmount *= -1;
							uInfo.uPromotionInfoList.add(promotionInfo);
						}
						bExist = true;
						break;
					}
				}
				if(!bExist)
				{
					for(UPromotionInfo promotionInfo : newUOrderDInfo.uPromotionInfoList)
					{
						promotionInfo.nAmount *= -1;
					}
					newOrderHInfo.pickupList.add(newUOrderDInfo);
				}
			}
			else if(uOrderDInfo.nTrxnType == Global.TRXN_SAMPLE || uOrderDInfo.nTrxnType == Global.TRXN_FREE)
			{
				UOrderDInfo newUOrderDInfo = uOrderDInfo.duplicate();
				newUOrderDInfo.nIdx = 0;
				newUOrderDInfo.nTrxnType = uOrderDInfo.nTrxnType;
				newUOrderDInfo.nSales *= -1;
				newUOrderDInfo.dSales *= -1;
				newUOrderDInfo.nDumps = 0;
				newUOrderDInfo.dDumps = 0;
				newUOrderDInfo.nTrxnNo = 0;
				boolean bExist = false;
				for(UOrderDInfo uInfo : newOrderHInfo.deliveryList)
				{
					if(uInfo.strItemNo.equals(newUOrderDInfo.strItemNo) && uInfo.nTrxnType == newUOrderDInfo.nTrxnType)
					{
						uInfo.nSales += newUOrderDInfo.nSales;
						uInfo.dSales += newUOrderDInfo.dSales;
						for(UPromotionInfo promotionInfo : newUOrderDInfo.uPromotionInfoList)
						{
							promotionInfo.nAmount *= -1;
							uInfo.uPromotionInfoList.add(promotionInfo);
						}
						bExist = true;
						break;
					}
				}
				if(!bExist)
				{
					for(UPromotionInfo promotionInfo : newUOrderDInfo.uPromotionInfoList)
					{
						promotionInfo.nAmount *= -1;
					}
					newOrderHInfo.deliveryList.add(newUOrderDInfo);
				}
			}
			else if(uOrderDInfo.nTrxnType == Global.TRXN_BUYBACK)
			{
				UOrderDInfo newUOrderDInfo = uOrderDInfo.duplicate();
				newUOrderDInfo.nIdx = 0;
				newUOrderDInfo.nTrxnType = Global.TRXN_DELIVER;
				newUOrderDInfo.nDumps = 0;
				newUOrderDInfo.dDumps = 0;
				newUOrderDInfo.nTrxnNo = 0;
				newUOrderDInfo.nSales = uOrderDInfo.nDumps;
				newUOrderDInfo.dSales = uOrderDInfo.dDumps;
				boolean bExist = false;
				for(UOrderDInfo uInfo : newOrderHInfo.deliveryList)
				{
					if(uInfo.strItemNo.equals(newUOrderDInfo.strItemNo))
					{
						uInfo.nSales += newUOrderDInfo.nSales;
						uInfo.dSales += newUOrderDInfo.dSales;
						for(UPromotionInfo promotionInfo : newUOrderDInfo.uPromotionInfoList)
						{
							promotionInfo.nAmount *= -1;
							uInfo.uPromotionInfoList.add(promotionInfo);
						}
						bExist = true;
						break;
					}
				}
				if(!bExist)
				{
					for(UPromotionInfo promotionInfo : newUOrderDInfo.uPromotionInfoList)
					{
						promotionInfo.nAmount *= -1;
					}
					newOrderHInfo.deliveryList.add(newUOrderDInfo);
				}

			}
			else if(uOrderDInfo.nTrxnType == Global.TRXN_PICKUP)
			{
				UOrderDInfo newUOrderDInfo = uOrderDInfo.duplicate();
				newUOrderDInfo.nIdx = 0;
				newUOrderDInfo.nTrxnNo = 0;
				newUOrderDInfo.nDumps *= -1;
				newUOrderDInfo.dDumps *= -1;
				boolean bExist = false;
				for(UOrderDInfo uInfo : newOrderHInfo.pickupList)
				{
					if(uInfo.strItemNo.equals(newUOrderDInfo.strItemNo) && uInfo.nTrxnType == Global.TRXN_PICKUP)
					{
						uInfo.nDumps += newUOrderDInfo.nDumps;
						uInfo.dDumps += newUOrderDInfo.dDumps;
						for(UPromotionInfo promotionInfo : newUOrderDInfo.uPromotionInfoList)
						{
							promotionInfo.nAmount *= -1;
							uInfo.uPromotionInfoList.add(promotionInfo);
						}
						bExist = true;
						break;
					}
				}
				if(!bExist)
				{
					for(UPromotionInfo promotionInfo : newUOrderDInfo.uPromotionInfoList)
					{
						promotionInfo.nAmount *= -1;
					}
					newOrderHInfo.pickupList.add(newUOrderDInfo);
				}
			}
		}
		Global.gUOrderReverseOriginalHInfo = uOrderHInfo;
		Global.gUOrderReverseHInfo = newOrderHInfo;
		Global.gReverseTransactionID = uOrderHInfo.nTrxnNo;
		Global.gUOrderHInfo = new UOrderHInfo();
		Global.gUOrderHInfo.strDeliveryDate = newOrderHInfo.strDeliveryDate;
		MakeDeliveryFragment fragment = new MakeDeliveryFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean(Global.EXTRA_IS_PICKUP, false);
		bundle.putBoolean(Global.EXTRA_IS_REVERSE, true);
		fragment.setArguments(bundle);
		((MainActivity)context).replaceFragment(fragment);
	}

	public static Map<String, Map<String, String>> gDetlMap = new HashMap<>();
	public static String getProdDetlProperty(String strItemNo, String strProperty)
	{
		if(gDetlMap.size() == 0) return null;
		Map<String, String> propMap = gDetlMap.get(strItemNo);
		if(propMap != null)
		{
			return propMap.get(strProperty);
		}
		else
		{
			return null;
		}
	}

	public static List sort(List list, List<String> strSorts)
	{
		if(list == null) return null;
		if(strSorts.size() == 0) return list;

		final String strSort = strSorts.get(0);

		Collections.sort(list, (o1, o2) -> {
			if(o1 instanceof Map && o2 instanceof Map)
			{
				String strValue1 = (String)((Map)o1).get(strSort);
				String strValue2 = (String)((Map)o2).get(strSort);
				if(strValue1 == null && strValue2 == null)
				{
					String strItemCode1 = (String)((Map)o1).get("item");
					String strItemCode2 = (String)((Map)o2).get("item");
					if(strItemCode1 == null && strItemCode2 == null)
					{
						strItemCode1 =  (String)((Map)o1).get("code");
						strItemCode2 =  (String)((Map)o2).get("code");
					}

					if(strItemCode1 == null && strItemCode2 == null)
					{
						return 0;
					}
					strValue1 = getProdDetlProperty(strItemCode1, strSort);
					strValue2 = getProdDetlProperty(strItemCode2, strSort);
				}

				if(!TextUtils.isEmpty(strValue1) && !TextUtils.isEmpty(strValue2))
				{
					return strValue1.compareTo(strValue2);
				}
				else if(TextUtils.isEmpty(strValue1) && TextUtils.isEmpty(strValue2))
				{
					return 0;
				}
				else if(TextUtils.isEmpty(strValue1))
				{
					return -1;
				}
				else
				{
					return 1;
				}
			}
			else
			{
				return 0;
			}
		});

		if(strSorts.size() > 1)
		{
			List<String> subSorts = strSorts.subList(1, strSorts.size());
			List new_array = new ArrayList();

			String strPrevValue = "";
			List tmp_array = new ArrayList();

			for(Object obj : list)
			{
				String strValue = (String)((Map)obj).get(strSort);
				if(strValue == null)
				{
					String strItemCode = (String)((Map)obj).get("item");
					if(strItemCode == null)
					{
						strItemCode = (String)((Map)obj).get("code");
					}
					if(strItemCode != null)
					{
						strValue = getProdDetlProperty(strItemCode, strSort);
					}
				}

				if(TextUtils.isEmpty(strValue)) continue;

				if(!strValue.equals(strPrevValue))
				{
					if(tmp_array.size() > 1)
					{
						sort(tmp_array, subSorts);
					}
					new_array.addAll(tmp_array);
					tmp_array.clear();
				}
				tmp_array.add(obj);
				strPrevValue = strValue;
			}

			if(tmp_array.size() > 1)
			{
				sort(tmp_array, subSorts);
			}
			new_array.addAll(tmp_array);
			tmp_array.clear();
			list.clear();
			list.addAll(new_array);
		}


		return list;
	}

	public static Map<String, String> getPropertyMap(ProdDetlInfo info)
	{
		Map<String, String> param = new HashMap<>();
		if(info == null)
		{
			param.put("group", "");
			param.put("market_group", "");
			param.put("promo_group", "");
			param.put("item_type", "");
			param.put("upc", "");
			param.put("desc", "");
			param.put("short_desc", "");
			param.put("brand", "");
			param.put("code", "");
		}
		else
		{
			param.put("group", info.strProdGrp != null ? info.strProdGrp : "");
			param.put("market_group", info.strMarketGrp != null ? info.strMarketGrp : "");
			param.put("promo_group", info.strPromoGrp != null  ? info.strProdGrp : "");
			param.put("item_type", info.strItemType != null  ? info.strItemType : "");
			param.put("upc", info.strItemUPC != null  ? info.strItemUPC : "");
			param.put("desc", info.strDescription != null  ? info.strDescription : "");
			param.put("short_desc", info.strShortDesc != null ? info.strShortDesc : "");
			param.put("brand", info.strBrand != null ? info.strBrand : "");
			param.put("code", info.strItemNo != null ? info.strItemNo : "");
		}

		return param;
	}

	public static String getCorrectTimeFormat(String strTime)
	{
		String values[] = strTime.split(":");
		if(values.length != 2) return "";
		if(values[0].trim().length() > 2) return "";
		if(values[1].trim().length() > 2) return "";
		if(values[0].trim().length() == 0) return "";
		if(values[1].trim().length() == 0) return "";
		if(values[0].trim().length() == 1) values[0] = "0" + values[0].trim();
		if(values[1].trim().length() == 1) values[1] = "0" + values[1].trim();
		return values[0].trim() + ":" + values[1].trim();
	}

	public static boolean possibleCheckNewOrders(Context context)
	{
		String strStartTime = getCorrectTimeFormat(getStringSetting(context, Global.PREF_ORDER_API_START));
		String strEndTime = getCorrectTimeFormat(getStringSetting(context, Global.PREF_ORDER_API_END));
		if(TextUtils.isEmpty(strStartTime) || TextUtils.isEmpty(strEndTime)) return true;


		SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
		String strCurrentTime = sdf.format(new Date());

		return (strCurrentTime.compareTo(strStartTime) > 0 && strCurrentTime.compareTo(strEndTime) < 0);
	}

	private static int TAG_NONE = 0;
	private static int TAG_SECTION = 1;
	private static int TAG_KEY = 2;
	private static int TAG_EQUAL = 3;
	private static int TAG_VALUE = 4;
	public static Document parseXML(DocumentBuilder builder, byte[] data)
	{
		Document document = null;
		String strData = new String(data);

		ByteArrayInputStream sBufInputSream = new ByteArrayInputStream(data);
		try {
			document = builder.parse(sBufInputSream);
			return document;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		String newData = "";
		int nCurType = TAG_NONE;
		for(int i = 0; i < strData.length(); i++)
		{
			switch(strData.charAt(i))
			{
				case '<':
				{
					if(nCurType == TAG_VALUE)
					{
						newData += "&lt;";
					}
					else if(nCurType == TAG_NONE)
					{
						nCurType = TAG_SECTION;
						newData += "<";
					}
					else
					{
						continue;
					}
				}
				break;

				case '>':
				{
					if(nCurType == TAG_SECTION)
					{
						nCurType = TAG_NONE;
						newData += ">";
					}
					else if(nCurType == TAG_NONE)
					{
						newData += "&lt;";
					}
					else if(nCurType == TAG_KEY)
					{
						newData += ">";
						nCurType = TAG_NONE;
					}
					else if(nCurType == TAG_EQUAL)
					{
						newData += "\"&lt;";
						nCurType = TAG_VALUE;
					}
					else if(nCurType == TAG_VALUE)
					{
						newData += "&lt;";
					}
				}
				break;

				case '/': {
					if (nCurType == TAG_NONE) {
						newData += "/";
					} else if (nCurType == TAG_SECTION) {
						newData += "/";
					} else if (nCurType == TAG_KEY) {
						newData += "/";
						nCurType = TAG_SECTION;
					} else if (nCurType == TAG_VALUE) {
						newData += "/";
					} else if (nCurType == TAG_EQUAL) {
						newData += "\"/";
						nCurType = TAG_VALUE;
					}
				}
					break;

				case '"':
				{
					if (nCurType == TAG_NONE) {
						newData += "\"";
					} else if (nCurType == TAG_SECTION) {
						continue;
					} else if (nCurType == TAG_KEY) {
						continue;
					} else if (nCurType == TAG_VALUE) {
						newData += "\" ";
						nCurType = TAG_KEY;
					} else if (nCurType == TAG_EQUAL) {
						newData += "\"";
						nCurType = TAG_VALUE;
					}
				}
					break;
				case '=':
				{
					if (nCurType == TAG_NONE) {
						newData += "=";
					} else if (nCurType == TAG_SECTION) {
						continue;
					} else if (nCurType == TAG_KEY) {
						newData += "=";
						nCurType = TAG_EQUAL;
					} else if (nCurType == TAG_VALUE) {
						newData += "=";
					} else if (nCurType == TAG_EQUAL) {
						continue;
					}
				}
					break;
				case ' ':
				{
					if (nCurType == TAG_NONE) {
						newData += " ";
					} else if (nCurType == TAG_SECTION) {
						newData += " ";
						nCurType = TAG_KEY;
					} else if (nCurType == TAG_KEY) {
						continue;
					} else if (nCurType == TAG_VALUE) {
						newData += " ";
					} else if (nCurType == TAG_EQUAL) {
						continue;
					}
				}
					break;
				case '&':
				{
					boolean bOk = false;
					for(int j = i + 2; j < i + 6 && j < strData.length(); j++)
					{
						if(strData.charAt(j) == ';')
						{
							bOk = true;
							break;
						}
					}
					if(!bOk)
					{
						newData += Utils.getHtmlCharacters(strData.charAt(i));
					}
					else
					{
						newData += strData.charAt(i);
					}
				}
				break;
				default: {
					if(nCurType == TAG_VALUE)
					{
						newData += Utils.getHtmlCharacters(strData.charAt(i));
					}
					else
					{
						newData += strData.charAt(i);
					}

				}
			}
		}

		sBufInputSream = new ByteArrayInputStream(newData.getBytes());
		try {
			document = builder.parse(sBufInputSream);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return document;
	}

	public static Map<String, String> encodedHtmlMap = new HashMap<>();
	public static Map<String, String> decodedHtmlMap = new HashMap<>();
	public static String getHtmlCharacters(char ch)
	{
		String strHtmlCharacter = encodedHtmlMap.get(String.valueOf(ch));
		if(strHtmlCharacter == null) return String.valueOf(ch);
		else return strHtmlCharacter;
	}

	public static String readFile(String filename)
	{
		File file = new File(filename);
		if(isFileExist(filename))
		{
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				byte[] buf = new byte[(int)file.length()];
				fis.read(buf);
				String strData = new String(buf);
				return strData;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static HTMLInstance htmlInstance = null;
	public static String getHTMLValue(String strValue)
	{
		if(htmlInstance != null)
		{
			for(HtmlObj htmlObj : htmlInstance.getting)
			{
				strValue = strValue.replace(htmlObj.key, htmlObj.value);
			}
		}
		return strValue.trim();
	}

	public static String getDateFromDocumentArchiveData(String strDateTime)
	{
		if(TextUtils.isEmpty(strDateTime)) return "";
		return strDateTime.split("T")[0];
	}

	public static String convertTimeFormat(String strDate, String strSourceFormat, String strTargetFormat)
	{
		if(TextUtils.isEmpty(strDate)) return "";
		SimpleDateFormat sdf_source = new SimpleDateFormat(strSourceFormat);
		SimpleDateFormat sdf_target = new SimpleDateFormat(strTargetFormat);
		String strTargetDate = "";
		try {
			Date date = sdf_source.parse(strDate);
			strTargetDate = sdf_target.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return strTargetDate;
	}


	public static void launchPdf(Context context, String strPath)
	{
        /*Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(strPath)), "application/pdf");
        context.startActivity(intent);*/
		File file = new File(strPath);
		try
		{
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
			{
				Uri photoURI = FileProvider.getUriForFile(context, "dsd.dsdvansales.fileprovider", file);

				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				intent.setDataAndType(photoURI, "application/pdf");
				context.startActivity(intent);
			}
			else
			{
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file), "application/pdf");
				context.startActivity(intent);
			}
		}catch (Exception e)
		{
			e.printStackTrace();
			Toast.makeText(context, Str.NoApplicationToShowPDF[Global.LANG], Toast.LENGTH_SHORT).show();
		}

	}

	public static void showPdf(MainActivity context, ArchiveInfo info)
	{
		if(info != null)
		{
			showPdf(context, info.docNo, convertTimeFormat(getDateFromDocumentArchiveData(info.date), "yyyy-MM-dd", "yyyyMMdd"));
		}
	}

	public static void emailPdf(MainActivity context, ArchiveInfo info)
	{
		String strDate = convertTimeFormat(getDateFromDocumentArchiveData(info.date), "yyyy-MM-dd", "yyyyMMdd");
		String strDocNo = info.docNo;

		//Check Write Storage Permission in here
		final String strDocRootPath = Global.DOWNLOAD_DIR + "previous/";
		Utils.createDirectory(strDocRootPath);
		final String strLocalFileName = strDocNo + "_" + strDate + ".pdf";
		if(Utils.isFileExist(strDocRootPath + strLocalFileName))
		{
			emailPdf(context, strDocRootPath + strLocalFileName);
			return;
		}
		String strBaseUrl = getBaseURL(Utils.getStringSetting(context, Global.PREF_APICOMPANY));
		if(!TextUtils.isEmpty(strBaseUrl))
		{
			final String strUrl = getBaseURL(Utils.getStringSetting(context, Global.PREF_APICOMPANY)) + "api/document-archive?docno=" + strDocNo + "&date=" + strDate;
			context.showProgressDialog("Loading PDF files...");
			new Thread()
			{
				@Override
				public void run() {
					//Download Files
					try {

						FileOutputStream f = new FileOutputStream(strDocRootPath + strLocalFileName);
						URL u = new URL(strUrl);
						HttpsURLConnection c = (HttpsURLConnection) u.openConnection();
						c.setRequestMethod("GET");
						//c.setDoOutput(true);
						c.connect();
						InputStream in = c.getInputStream();
						byte[] buffer = new byte[1024];
						int len1 = 0;
						while ((len1 = in.read(buffer)) > 0) {
							f.write(buffer, 0, len1);
						}
						f.close();
						new Handler(Looper.getMainLooper()).post(() -> {
							context.hideProgressDialog();
							emailPdf(context, strDocRootPath + strLocalFileName);
						});
					} catch (final Exception e) {
						new Handler(Looper.getMainLooper()).post(() -> {
							context.hideProgressDialog();
							fileDelete(strDocRootPath + strLocalFileName);
							final MessageDialog dlg = new MessageDialog(context);
							dlg.mStrMessage = Str.There_is_no_pdf_data[Global.LANG];
							dlg.mStrTitle = "";
							dlg.mStrMiddle = Str.Ok[Global.LANG];
							dlg.setCancelable(false);
							dlg.show();
						});
						e.printStackTrace();
					}
				}
			}.start();
		}

	}

	public static void emailPdf(MainActivity context, String strPath)
	{
		File file = new File(strPath);

		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Invoice");
		emailIntent.putExtra(Intent.EXTRA_TEXT, "Here is the invoice");
		emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
		{
			Uri photoURI = FileProvider.getUriForFile(context, "dsd.dsdvansales.fileprovider", file);
			emailIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
		}
		else
		{
			emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		}
		context.startActivity(emailIntent);
	}

	public static void printPdf(MainActivity context, String strPath)
	{
		context.doPrint(strPath);
	}

	public static void printPdf(MainActivity context, ArchiveInfo info)
	{
		String strDate = convertTimeFormat(getDateFromDocumentArchiveData(info.date), "yyyy-MM-dd", "yyyyMMdd");
		String strDocNo = info.docNo;

		//Check Write Storage Permission in here
		final String strDocRootPath = Global.DOWNLOAD_DIR + "previous/";
		Utils.createDirectory(strDocRootPath);
		final String strLocalFileName = strDocNo + "_" + strDate + ".pdf";
		if(Utils.isFileExist(strDocRootPath + strLocalFileName))
		{
			printPdf(context, strDocRootPath + strLocalFileName);
			return;
		}

		String strBaseUrl = getBaseURL(Utils.getStringSetting(context, Global.PREF_APICOMPANY));
		if(!TextUtils.isEmpty(strBaseUrl))
		{
			final String strUrl = strBaseUrl + "api/document-archive?docno=" + strDocNo + "&date=" + strDate;
			context.showProgressDialog("Loading PDF files...");
			new Thread()
			{
				@Override
				public void run() {
					//Download Files
					try {

						FileOutputStream f = new FileOutputStream(strDocRootPath + strLocalFileName);
						URL u = new URL(strUrl);
						HttpsURLConnection c = (HttpsURLConnection) u.openConnection();
						c.setRequestMethod("GET");
						//c.setDoOutput(true);
						c.connect();
						InputStream in = c.getInputStream();
						byte[] buffer = new byte[1024];
						int len1 = 0;
						while ((len1 = in.read(buffer)) > 0) {
							f.write(buffer, 0, len1);
						}
						f.close();
						new Handler(Looper.getMainLooper()).post(() -> {
							context.hideProgressDialog();
							printPdf(context, strDocRootPath + strLocalFileName);
						});
					} catch (final Exception e) {
						new Handler(Looper.getMainLooper()).post(() -> {
							context.hideProgressDialog();
							fileDelete(strDocRootPath + strLocalFileName);
							final MessageDialog dlg = new MessageDialog(context);
							dlg.mStrMessage = Str.There_is_no_pdf_data[Global.LANG];
							dlg.mStrTitle = "";
							dlg.mStrMiddle = Str.Ok[Global.LANG];
							dlg.setCancelable(false);
							dlg.show();
						});
						e.printStackTrace();
					}
				}
			}.start();
		}

	}

	public static void showPdf(final MainActivity context, String strDocNo, String strDate)
	{
		//Check Write Storage Permission in here
		final String strDocRootPath = Global.DOWNLOAD_DIR + "previous/";
		Utils.createDirectory(strDocRootPath);
		final String strLocalFileName = strDocNo + "_" + strDate + ".pdf";
		if(Utils.isFileExist(strDocRootPath + strLocalFileName))
		{
			launchPdf(context, strDocRootPath + strLocalFileName);
			return;
		}

		final String strBaseUrl = getBaseURL(Utils.getStringSetting(context, Global.PREF_APICOMPANY));
		if(!TextUtils.isEmpty(strBaseUrl))
		{
			final String strUrl = strBaseUrl + "api/document-archive?docno=" + strDocNo + "&date=" + strDate;
			context.showProgressDialog("Loading PDF files...");
			new Thread()
			{
				@Override
				public void run() {
					//Download Files
					try {

						FileOutputStream f = new FileOutputStream(strDocRootPath + strLocalFileName);
						URL u = new URL(strUrl);
						HttpsURLConnection c = (HttpsURLConnection) u.openConnection();
						c.setRequestMethod("GET");
						//c.setDoOutput(true);
						c.connect();
						InputStream in = c.getInputStream();
						byte[] buffer = new byte[1024];
						int len1 = 0;
						while ((len1 = in.read(buffer)) > 0) {
							f.write(buffer, 0, len1);
						}
						f.close();
						new Handler(Looper.getMainLooper()).post(() -> {
							context.hideProgressDialog();
							launchPdf(context, strDocRootPath + strLocalFileName);
						});
					} catch (final Exception e) {
						new Handler(Looper.getMainLooper()).post(() -> {
							context.hideProgressDialog();
							fileDelete(strDocRootPath + strLocalFileName);
							final MessageDialog dlg = new MessageDialog(context);
							dlg.mStrMessage = Str.There_is_no_pdf_data[Global.LANG];
							dlg.mStrTitle = "";
							dlg.mStrMiddle = Str.Ok[Global.LANG];
							dlg.setCancelable(false);
							dlg.show();
						});
						e.printStackTrace();
					}
				}
			}.start();
		}

	}

	public static UInvenBlotInfo getUInvenBlot(UInvenHInfo info, String strDocNo, String strTrip)
	{
		UInvenBlotInfo newInfo = new UInvenBlotInfo();
		newInfo.nTrxnNo = info.nTrxnNo;
		newInfo.strDocNo = strDocNo;
		newInfo.strDocType = info.strDocType;
		newInfo.nVoidFlag = 0;
		newInfo.nPrintedFlag = 0;
		newInfo.strTrxnDate = info.strTrxnDate;
		newInfo.strTrxnTime = info.strTrxnTime;
		newInfo.strReference = info.strReference;
		newInfo.nTComStatus = 0;
		newInfo.nChainNo = 0;
		newInfo.nCustNo = 0;
		newInfo.strSaleDate = info.strTrxnDate;
		newInfo.strTrip = strTrip;
		return newInfo;
	}

	public static List<UInvenBlotDInfo> getUInvenBlotList(UInvenDInfo info)
	{
		List<UInvenBlotDInfo> newList = new ArrayList<>();
		if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
		{
			for(LoadBlotInfo bInfo : info.loadBlotInfoList)
			{
				UInvenBlotDInfo newInfo =new UInvenBlotDInfo();
				newInfo.nTrxnNo = info.nTrxnNo;
				newInfo.strItemNo = info.strItemNo;
				newInfo.strBatchDate = bInfo.strBatchDate;
				newInfo.strLotNumber = bInfo.strLotNumber;
				newInfo.nBeginStock = bInfo.nLotQty;
				newInfo.nStock = bInfo.nLotQty;
				newList.add(newInfo);
			}
		}
		return newList;
	}

	public static void saveBitmapToFile(Bitmap bitmap, String filepath) {
		try {
			File file = new File(filepath);
			if (file.exists()) {
				file.delete();
				file = new File(filepath);
			}
			FileOutputStream os = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
			os.flush();
			os.close();
		} catch (Exception e) {

		}
	}

	public static boolean isPossibleDownloadFileViaFTP(Context context)
	{
		if(context == null)
		{
			return false;
		}

		String strDisableFTP = Utils.getStringSetting(context, Global.PREF_DisableFTP);

		if(strDisableFTP.equals("1"))
		{
			return false;
		}

		return true;
	}

	public static boolean isPossibleDownloadFileViaHTTP(Context context)
	{
		if(context == null)
		{
			return false;
		}

		String strFTPServerIP = Utils.getStringSetting(context, Global.PREF_FTP_SERVER_IP);
		String strFTPUserName = Utils.getStringSetting(context, Global.PREF_FTP_USERNAME);
		String strCompanyName = Utils.getStringSetting(context, Global.PREF_FTP_COMPANY_NAME);
//		String strDisableFTP = Utils.getStringSetting(context, Global.PREF_DisableFTP);
		if(TextUtils.isEmpty(strFTPServerIP) || TextUtils.isEmpty(strFTPUserName) || TextUtils.isEmpty(strCompanyName))
		{
			return false;
		}

//		if(TextUtils.isEmpty(strFTPServerIP) || !strDisableFTP.equals("1"))
//		{
//			return false;
//		}

		return true;
	}

	public static FileInfo downloadFileFromServer(Context context, String strFolderPath, String strFileName, String strTargetPath, FTPClient ftpClient)
	{
		if(strFolderPath.endsWith("/"))
		{
			strFolderPath = strFolderPath.substring(0, strFolderPath.length() - 1);
		}

		boolean isFTPPossible = isPossibleDownloadFileViaFTP(context);

		FileInfo fileInfo = Utils.checkFileStatusUsingHttp(context, strFolderPath, strFileName);
		if(fileInfo != null)
		{
			long nLocalSize = 0;
			if (Utils.isFileExist(strTargetPath) == true) {
				nLocalSize = new File(strTargetPath).length();
			}
			if (nLocalSize == 0 || fileInfo.filesize != nLocalSize){
				Utils.createDirectory(new File(strTargetPath).getParentFile().getAbsolutePath());
				if(!downloadFile(context, strFolderPath, strFileName, strTargetPath))
				{
					if(isFTPPossible)
					{
						return doDownloadFileViaFTP(context, strFolderPath, strFileName, strTargetPath, ftpClient);
					}
					else {
						return null;
					}
				}
				else {
					markDownloadSuccess(context, strFolderPath, strFileName);
					return fileInfo;
				}
			} else {
				return fileInfo;
			}
		}
		else if(isFTPPossible)
		{
			return doDownloadFileViaFTP(context, strFolderPath, strFileName, strTargetPath, ftpClient);
		}
		else
		{
			return null;
		}
	}

	public static FileInfo doDownloadFileViaFTP(Context context, String strFolderPath, String strFileName, String strTargetPath, FTPClient ftpClient)
	{
		if(ftpClient == null)
		{
			String strFTPServerIP = Utils.getStringSetting(context, Global.PREF_FTP_SERVER_IP);
			String strFTPUserName = Utils.getStringSetting(context, Global.PREF_FTP_USERNAME);
			String strFTPPassword = Utils.getStringSetting(context, Global.PREF_FTP_PASSWORD);
			ftpClient = Utils.connnectingwithFTP(strFTPServerIP, Global.DEFAULT_FTP_PORT, strFTPUserName, strFTPPassword);
		}

		if(ftpClient == null)
		{
			return null;
		}

		FileInfo fileInfo = new FileInfo();
		fileInfo.filesize = Utils.findFileOnFTPServer(ftpClient, strFolderPath, strFileName);

		if(fileInfo.filesize == 0)
		{
			return null;
		}

		long nLocalSize = 0;
		if (Utils.isFileExist(strTargetPath) == true) {
			nLocalSize = new File(strTargetPath).length();
		}
		if (nLocalSize == 0 || fileInfo.filesize != nLocalSize){
			createDirectory(new File(strTargetPath).getParentFile().getAbsolutePath());
			if (downloadFileFromFTPServer(ftpClient, strFolderPath + "/" + strFileName, strTargetPath) == false) {
				Utils.printDebug(null, "Can't download the file.");
				return null;
			}
			return fileInfo;
		} else {
			return fileInfo;
		}
	}

	public static String makeDownloadLink(Context context, String strFolderPath, String strFileName)
	{
		if(context == null || TextUtils.isEmpty(strFolderPath) || TextUtils.isEmpty(strFileName))
		{
			return null;
		}

		if(strFolderPath.endsWith("/"))
		{
			strFolderPath = strFolderPath.substring(0, strFolderPath.length() - 1);
		}

		String strFTPServerIP = Utils.getStringSetting(context, Global.PREF_FTP_SERVER_IP);
		String strFTPUserName = Utils.getStringSetting(context, Global.PREF_FTP_USERNAME);
		String strCompanyName = Utils.getStringSetting(context, Global.PREF_FTP_COMPANY_NAME);

		String strDownloadPath = UploadApiClient.API_ROOT + "/dsd/index.php/Backend/downloadFile?ftp_user_name=" + strFTPUserName + "&ftp_ip_address=" + strFTPServerIP + "&company_name=" + strCompanyName + "&target_folder=" + strFolderPath + "&target_file=" + strFileName;
		return strDownloadPath;
	}

	public static boolean downloadFile(Context context, String strFolderPath, String strFileName, String strTargetPath){
		try
		{
			if(strFolderPath.endsWith("/"))
			{
				strFolderPath = strFolderPath.substring(0, strFolderPath.length() - 1);
			}

			String downloadUrl = makeDownloadLink(context, strFolderPath, strFileName);

			URL url = new URL(downloadUrl);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();

			c.setRequestMethod("GET");
			c.setDoOutput(true);

			c.connect();

			File file = new File(strTargetPath);
			if(file.exists())
			{
				file.delete();
			}

			FileOutputStream f = new FileOutputStream(file);
			InputStream in = new BufferedInputStream(url.openStream(), 4096);
			byte[] buffer = new byte[1024 * 1000];
			int len;
			while ((len = in.read(buffer)) > 0) {
				f.write(buffer, 0, len);
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
				return false;
			}
		}catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static void markDownloadSuccess(Context context, String strFolderPath, String strFileName){
		if(context == null || TextUtils.isEmpty(strFolderPath) || TextUtils.isEmpty(strFileName))
		{
			return;
		}

		String strFTPServerIP = Utils.getStringSetting(context, Global.PREF_FTP_SERVER_IP);
		String strFTPUserName = Utils.getStringSetting(context, Global.PREF_FTP_USERNAME);
		String strCompanyName = Utils.getStringSetting(context, Global.PREF_FTP_COMPANY_NAME);

		if(strFolderPath.endsWith("/"))
		{
			strFolderPath = strFolderPath.substring(0, strFolderPath.length() - 1);
		}

		UploadApiClient.getMainApiClient().registerDownloadSuccess(strFTPUserName, strFTPServerIP, strCompanyName, strFolderPath, strFileName).enqueue(new Callback<GeneralResponse>() {
			@Override
			public void onResponse(Call<GeneralResponse> call, Response<GeneralResponse> response) {
			}

			@Override
			public void onFailure(Call<GeneralResponse> call, Throwable t) {
			}
		});
	}

	public static FileInfo checkFileStatusUsingHttp(Context context, String strFolderPath, String strFileName)
	{
		if(context == null || TextUtils.isEmpty(strFolderPath) || TextUtils.isEmpty(strFileName))
		{
			return null;
		}

		String strFTPServerIP = Utils.getStringSetting(context, Global.PREF_FTP_SERVER_IP);
		String strFTPUserName = Utils.getStringSetting(context, Global.PREF_FTP_USERNAME);
		String strCompanyName = Utils.getStringSetting(context, Global.PREF_FTP_COMPANY_NAME);

		if(strFolderPath.endsWith("/"))
		{
			strFolderPath = strFolderPath.substring(0, strFolderPath.length() - 1);
		}
		CountDownLatch latch = new CountDownLatch(1);
		final FileInfo fileInfo = new FileInfo();
		UploadApiClient.getMainApiClient().getFileInfo(strFTPUserName, strFTPServerIP, strCompanyName, strFolderPath, strFileName).enqueue(new Callback<FileInfoStatus>() {
			@Override
			public void onResponse(Call<FileInfoStatus> call, Response<FileInfoStatus> response) {
				if(response.isSuccessful() && response.body() != null && response.body().result == 1)
				{
					fileInfo.filesize = response.body().filesize;
					fileInfo.modified_time = response.body().modified_time;
				}
				latch.countDown();
			}

			@Override
			public void onFailure(Call<FileInfoStatus> call, Throwable t) {
				latch.countDown();
			}
		});
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if(fileInfo.filesize > 0)
		{
			return fileInfo;
		}
		else
		{
			return null;
		}
	}

	public static String decodeFile(String path, int maxSize) {
		String strMyImagePath = null;
		Bitmap scaledBitmap = null;

		try {
			// Part 1: Decode image
			scaledBitmap = getSafeDecodeBitmap(path, maxSize);

			// Store to tmp file
			String s = "tmp.jpg";
			File f = new File(Global.ROOT_LOCATION, s);

			strMyImagePath = f.getAbsolutePath();
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(f);
				scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {

				e.printStackTrace();
			} catch (Exception e) {

				e.printStackTrace();
			}

			scaledBitmap.recycle();
		} catch (Throwable e) {
		}

		if (strMyImagePath == null) {
			return path;
		}
		return strMyImagePath;
	}

	public static void decodeFile(String path, String newPath, int maxSize) {
		Bitmap scaledBitmap = null;

		try {
			// Part 1: Decode image
			scaledBitmap = getSafeDecodeBitmap(path, maxSize);

			// Store to tmp file
			File f = new File(newPath);
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(f);
				scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {

				e.printStackTrace();
			} catch (Exception e) {

				e.printStackTrace();
			}

			scaledBitmap.recycle();
		} catch (Throwable e) {
		}
	}

	private static Map<String, String> sortOptions = new HashMap<>();
	public static boolean isValidSortValue(String strSortValue)
	{
		if(strSortValue.equals("group") ||
				strSortValue.equals("market_group") ||
				strSortValue.equals("promo_group") ||
				strSortValue.equals("item_type") ||
				strSortValue.equals("upc") ||
				strSortValue.equals("desc") ||
				strSortValue.equals("short_desc") ||
				strSortValue.equals("brand") ||
				strSortValue.equals("code"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static String getSortValue(String strSortItem)
	{
		if(sortOptions.size() == 0)
		{
			sortOptions.put("ProdGroup", "group");
			sortOptions.put("MarketGroup", "market_group");
			sortOptions.put("PromoGroup", "promo_group");
			sortOptions.put("ItemType", "item_type");
			sortOptions.put("ItemUPC", "upc");
			sortOptions.put("Description", "desc");
			sortOptions.put("ShortDesc", "short_desc");
			sortOptions.put("Brand", "brand");
			sortOptions.put("Item code", "code");
		}
		return sortOptions.get(strSortItem);
	}

	@SuppressLint("NewApi")
	public static int getScreenWidth(Activity activity) {
		Point size = new Point();
		Display d = activity.getWindowManager().getDefaultDisplay();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			d.getSize(size);
		} else {
			size.x = d.getWidth();
			size.y = d.getHeight();
		}

		return size.x;
	}

	public static float convertPixelsToDp(float px, Context context){
		return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
	}

	public static float convertDpToPixel(float dp, Context context){
		return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
	}

	public static void doEnableSSLChecking() {

		TrustManagerFactory tmf = null;
		X509TrustManager defaultTM = null;
		try {
			tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init((KeyStore)null);
			defaultTM = (X509TrustManager) tmf.getTrustManagers()[0];

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> {
				return true;
			});
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, new TrustManager[]{defaultTM}, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public static String getHoneywellPrintString(String strData, int nMax, String format)
	{
		if(strData.length() > nMax)
		{
			return strData.substring(0, nMax);
		}
		else {
			if(format.equals("center"))
			{
				//Need to add the space at front and end. Total length has to be nMax
				StringBuilder strResult = new StringBuilder();
				int nHalf = (nMax - strData.length()) / 2;
				for(int i = 0; i < nHalf; i++)
				{
					strResult.append(" ");
				}
				strResult.append(strData);
				for(int i = nHalf; i < nMax - strData.length(); i++)
				{
					strResult.append(" ");
				}
				return strResult.toString();
			}
			else if(format.equals("right"))
			{
				//Need to add the space at first of the text. Total length has to be nMax.
				StringBuilder strResult = new StringBuilder();
				for(int i = strData.length(); i<nMax; i++)
				{
					strResult.append(" ");
				}
				strResult.append(strData);
				return strResult.toString();
			}
			else {
				//Need to add the space at last of the text. Total length has to be nMax.
				StringBuilder strResult = new StringBuilder();
				for(int i = strData.length(); i<nMax; i++)
				{
					strResult.append(" ");
				}
				strResult.insert(0, strData);
				return strResult.toString();
			}
		}
	}

	public static Bitmap getBitMapFromFile(String strPath)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		return BitmapFactory.decodeFile(strPath, options);
	}

	public static boolean isProbablyArabic(String s) {
		for (int i = 0; i < s.length(); ) {
			int c = s.codePointAt(i);
			if (c >= 0x0600 && c <= 0x06E0)
				return true;
			i += Character.charCount(c);
		}
		return false;
	}

	public static String convertArabicForPrint(String s) {
		String[] words = s.split(" ");
		String[] reversedWords = new String[words.length];
		String wholeResult = "";
		for (int i = words.length-1; i >= 0; i--) {
			String word = words[i];
			String resultWord = "";
			for (int k = 0; k < word.length(); k++) {
				char ch = word.charAt(k);
				resultWord = ch + resultWord;
			}
			reversedWords[words.length-i-1] = resultWord;
		}
		wholeResult = String.join(" ", reversedWords);
		return wholeResult;
	}

	public static String getFinalPrintTemplateFileName(Context context, String strOriginalFileName) {
		String strPrinterSize = Utils.getStringSetting(context, Global.PREF_PRINTER_SIZE);
		String finalTemplateFileName = getFinalPrintTemplateFileName(context, strOriginalFileName, strPrinterSize);
		return finalTemplateFileName;
	}
	public static String getFinalPrintTemplateFileName(Context context, String strOriginalFileName, String strPrinterSize) {

		DbAdapter dbAdapter = new DbAdapter(context);
		dbAdapter.open();
		int nPrinterTypeCnt = dbAdapter.getDescTypeList(Global.PRINTER_TYPE_DESC_TYPE_ID).size();
		dbAdapter.close();
		int printType = Utils.getIntSetting(context, Global.PREF_PRINT_TYPE);
		if (nPrinterTypeCnt <= 1 || printType == 0) {
			return strOriginalFileName;
		}

		String strHoneywelllinePrinterSuffix = Utils.getStringSetting(context, Global.PREF_HONEYWELLLINE_PRINTER_TEMPLATE_SUFFIX);

		if (strOriginalFileName.isEmpty()) {
			return "";
		}
		if (strPrinterSize.isEmpty()) {
			return strOriginalFileName;
		}

		String[] parts = strOriginalFileName.split("\\.");
		if (parts.length < 2) {
			return "";
		}
		int nPartCount = parts.length;
		if (!parts[nPartCount-1].equalsIgnoreCase("XML")) {
			return "";
		}
		if (!strPrinterSize.equals(strHoneywelllinePrinterSuffix)) {
			parts[nPartCount-2] = parts[nPartCount-2] + "." + strPrinterSize;
		}
		String finalTemplateFileName = String.join(".", parts);
		return finalTemplateFileName;
	}

	public static String getFinalPrintTemplateFileNameForHoneywell(String strOriginalFileName, String strHoneywellSuffix) {

		if (strOriginalFileName.isEmpty()) {
			return "";
		}

		String[] parts = strOriginalFileName.split("\\.");
		if (parts.length < 2) {
			return "";
		}

		int nPartCount = parts.length;
		if (!parts[nPartCount-1].equalsIgnoreCase("XML")) {
			return "";
		}
		parts[nPartCount-2] = parts[nPartCount-2] + "." + strHoneywellSuffix;

		String finalTemplateFileName = String.join(".", parts);
		return finalTemplateFileName;
	}

	public static String getFinalPrintTemplateFileNameForHoneywell(Context context, String strOriginalFileName) {

		if (strOriginalFileName.isEmpty()) {
			return "";
		}

		String[] parts = strOriginalFileName.split("\\.");
		if (parts.length < 2) {
			return "";
		}

		int nPartCount = parts.length;
		if (!parts[nPartCount-1].equalsIgnoreCase("XML")) {
			return "";
		}

		String strHoneywellSuffix = Utils.getStringSetting(context, Global.PREF_HONEYWELLLINE_PRINTER_TEMPLATE_SUFFIX);
		parts[nPartCount-2] = parts[nPartCount-2] + "." + strHoneywellSuffix;

		String finalTemplateFileName = String.join(".", parts);
		return finalTemplateFileName;
	}

	public static byte[] getTLVForValue(byte tagNum, String tagValue) {

		try {

			byte[] tagValueBytes = tagValue.getBytes("UTF-8");
			byte[] resultBytes = new byte[tagValueBytes.length+2];

			resultBytes[0] = tagNum;
			resultBytes[1] = (byte)tagValueBytes.length;

			for (int i = 0; i < tagValueBytes.length; i++) {
				resultBytes[2+i] = tagValueBytes[i];
			}

			return resultBytes;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Map<String, ProdInfo> mProdInfoMap;
	private static Map<String, ProdDetlInfo> mProdDetlItemMap;
	public static String getDisplayItemCode(Context context, String strItemNo) {
		if (mProdInfoMap == null || mProdDetlItemMap == null) {
			DbAdapter dbAdapter = new DbAdapter(context);
			dbAdapter.open();
			mProdDetlItemMap = new HashMap<>();
			mProdInfoMap = new HashMap<>();
			dbAdapter.getProdDetlList(mProdDetlItemMap, null);
			if(Utils.getStringSetting(context, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("PRODINFO"))
			{
				mProdInfoMap = dbAdapter.getProdInfoMap("ItemCode");
			}
			dbAdapter.close();
		}

		String strDisplayItemCode = "";
		if(Utils.getStringSetting(context, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("PRODINFO"))
		{
			ProdInfo prodInfo = mProdInfoMap.get(strItemNo);
			strDisplayItemCode = prodInfo != null ? prodInfo.strInfoDetail : "";
		}
		else if(Utils.getStringSetting(context, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("UPC"))
		{
			ProdDetlInfo prodDetlInfo = mProdDetlItemMap.get(strItemNo);
			strDisplayItemCode = prodDetlInfo != null ? prodDetlInfo.strItemUPC : "";
		}
		else if(Utils.getStringSetting(context, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("UPC20"))
		{
			ProdDetlInfo prodDetlInfo = mProdDetlItemMap.get(strItemNo);
			strDisplayItemCode = prodDetlInfo != null ? prodDetlInfo.strItemUPC20 : "";
		}
		else {
			strDisplayItemCode = strItemNo;
		}
		return strDisplayItemCode;
	}

	public static void addPrintExceptionStackToFileIfNeeded(Context context, String strFilename, Exception ex) {
		if (Utils.getStringSetting(context, Global.PREF_PRINT_ERROR_LOG).equals("1")) {
			String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + strFilename;
			File logFile = new File(filePath);

			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String timestamp = sdf.format(new Date());

				FileWriter writer = new FileWriter(logFile, true);
				writer.append("Timestamp: ").append(timestamp).append("\n");
				writer.append("Stack trace:\n");
				writer.append(android.util.Log.getStackTraceString(ex));
				writer.append("\n\n");
				writer.flush();
				writer.close();
			} catch (Exception ex1) {
				ex1.printStackTrace();
			}
		}
	}

	public static void addEndTripExceptionStackToFileIfNeeded(Context context, String strFilename, Exception ex, String extra) {
		if (Utils.getStringSetting(context, Global.PREF_DISABLE_END_TRIP_ERROR_LOG).equals("1")) {
			return;
		}

		String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + strFilename;
		File logFile = new File(filePath);

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String timestamp = sdf.format(new Date());

			FileWriter writer = new FileWriter(logFile, true);
			writer.append("Extra: ").append(extra).append("\n");
			writer.append("Timestamp: ").append(timestamp).append("\n");
			writer.append("Stack trace:\n");
			writer.append(android.util.Log.getStackTraceString(ex));
			writer.append("\n\n");
			writer.flush();
			writer.close();
		} catch (Exception ex1) {
			ex1.printStackTrace();
		}
	}

	public static String getRoutectlTripDate(Context context, String parseFormat, String convFormat, String strDate) {

		if (Utils.getStringSetting(context, Global.PREF_ROUTECTL_TRIP_DATE).equals("1")) {
			// convert trip date into proper format
			// get trip date
			SimpleDateFormat df = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT);
			SimpleDateFormat tdf = new SimpleDateFormat(Utils.getDateFormat(context, convFormat));
			String strDate1 = "";
			String strTripDate = Utils.getStringSetting(context, Global.PREF_TRIP_DATE);

			try {
				strDate1 = tdf.format(df.parse(strTripDate));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return strDate1;
		}
		else {
			SimpleDateFormat df = new SimpleDateFormat(parseFormat);
			SimpleDateFormat tdf = new SimpleDateFormat(Utils.getDateFormat(context, convFormat));
			String strDate1 = "";

			try {
				strDate1 = tdf.format(df.parse(strDate));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return strDate1;
		}
	}

	public static String getRoutectlTripDate(Context context, String convFormat, Date date) {

		if (Utils.getStringSetting(context, Global.PREF_ROUTECTL_TRIP_DATE).equals("1")) {
			// convert trip date into proper format
			// get trip date
			SimpleDateFormat df = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT);
			SimpleDateFormat tdf = new SimpleDateFormat(Utils.getDateFormat(context, convFormat));
			String strDate = "";
			String strTripDate = Utils.getStringSetting(context, Global.PREF_TRIP_DATE);

			try {
				strDate = tdf.format(df.parse(strTripDate));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return strDate;
		}
		else {
			SimpleDateFormat tdf = new SimpleDateFormat(Utils.getDateFormat(context, convFormat));
			String strDate = "";

			try {
				strDate = tdf.format(date);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return strDate;
		}
	}

	public static String getRoutectlTripDateTime(Context context, String convDateFormat, String convTimeFormat, Date date) {

		String strTime = "";
		try {
			strTime = new SimpleDateFormat(convTimeFormat).format(date);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		if (Utils.getStringSetting(context, Global.PREF_ROUTECTL_TRIP_DATE).equals("1")) {
			// convert trip date into proper format
			// get trip date
			SimpleDateFormat df = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT);
			SimpleDateFormat tdf = new SimpleDateFormat(Utils.getDateFormat(context, convDateFormat));
			String strDate = "";
			String strTripDate = Utils.getStringSetting(context, Global.PREF_TRIP_DATE);

			try {
				strDate = tdf.format(df.parse(strTripDate));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return strDate + strTime;
		}
		else {
			SimpleDateFormat tdf = new SimpleDateFormat(Utils.getDateFormat(context, convDateFormat));
			String strDate = "";

			try {
				strDate = tdf.format(date);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return strDate + strTime;
		}
	}

	public static Date getRoutectlTripDate(Context context, Date originDate) {

		if (Utils.getStringSetting(context, Global.PREF_ROUTECTL_TRIP_DATE).equals("1")) {
			// convert trip date into proper format
			// get trip date
			SimpleDateFormat df = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT);
			String strTripDate = Utils.getStringSetting(context, Global.PREF_TRIP_DATE);
			Date parsedDate = new Date();

			try {
				parsedDate = df.parse(strTripDate);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return parsedDate;
		}
		else {
			return originDate;
		}
	}

	public static Date getRoutectlTripDateTime(Context context, Date originDate) {

		if (Utils.getStringSetting(context, Global.PREF_ROUTECTL_TRIP_DATE).equals("1")) {
			// convert trip date into proper format
			// get trip date
			SimpleDateFormat df = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT);
			String strTripDate = Utils.getStringSetting(context, Global.PREF_TRIP_DATE);
			Date parsedDate = new Date();

			try {
				parsedDate = df.parse(strTripDate);

				if (parsedDate == null) {
					return new Date();
				}

				// Create a calendar instance for the source date
				Calendar sourceCal = Calendar.getInstance();
				sourceCal.setTime(originDate);

				// Get the hour, minute, and second from the source date
				int hour = sourceCal.get(Calendar.HOUR_OF_DAY);
				int minute = sourceCal.get(Calendar.MINUTE);
				int second = sourceCal.get(Calendar.SECOND);
				int milliSecond = sourceCal.get(Calendar.MILLISECOND);

				// Create a new calendar for the target date (Target Date)
				Calendar targetCal = Calendar.getInstance();
				targetCal.setTime(parsedDate); // Any date or current date

				// Set only the hour, minute, and second in the target date
				targetCal.set(Calendar.HOUR_OF_DAY, hour);
				targetCal.set(Calendar.MINUTE, minute);
				targetCal.set(Calendar.SECOND, second);
				targetCal.set(Calendar.MILLISECOND, milliSecond);

				// Get the new date with the copied time
				parsedDate = targetCal.getTime();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return parsedDate;
		}
		else {
			return originDate;
		}
	}

	public static boolean isContainerItem(Context context, String strItemNo) {
		int nSCWType = getSCWType(context, strItemNo);
		if (nSCWType == 999) {
			return false;
		}

		return nSCWType == 2;
	}

	public static int getSCWType(Context context, String strItemNo) {
		if (mProdDetlItemMap == null) {
			DbAdapter dbAdapter = new DbAdapter(context);
			dbAdapter.open();
			mProdDetlItemMap = new HashMap<>();
			dbAdapter.getProdDetlList(mProdDetlItemMap, null);
			dbAdapter.close();
		}

		ProdDetlInfo prodDetlInfo = mProdDetlItemMap.get(strItemNo);
		if (prodDetlInfo == null) {
			return 999;
		}

		return prodDetlInfo.prodLocnInfo.nSCWType;
	}

	private static Map<String, ProdDetlInfo> mProductDetailMap;
	public static ProdDetlInfo getProductDetail(Context context, String strItemNo) {
		if (mProductDetailMap == null) {
			DbAdapter dbAdapter = new DbAdapter(context);
			dbAdapter.open();
			mProductDetailMap = new HashMap<>();
			dbAdapter.getProductList(mProductDetailMap, null);
			dbAdapter.close();
		}

		ProdDetlInfo getProductDetail = mProductDetailMap.get(strItemNo);
		return getProductDetail;
	}

	public static float getProductDriverPrice(Context context, String strItemNo) {
		if(Global.prodLocnInfoMap.size() == 0)
		{
			DbAdapter dbAdapter = new DbAdapter(context);
			dbAdapter.open();
			dbAdapter.getProdLocnInfoList(Global.prodLocnInfoMap);
			dbAdapter.close();
		}

		ProdLocnInfo info = Global.prodLocnInfoMap.get(strItemNo);
		if(info == null)
		{
			return 0;
		}
		else
		{
			return Utils.getDividedValue(info.nDriverPrice);
		}
	}

	public static void generateLiterals(String version) {
		String csvFileName = "VanSales_Literals_" + version + ".csv";
		String csvFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + csvFileName;
		File file = new File(csvFilePath);
		if (file.exists()) {
			file.delete();
		}
		try {
			FileWriter writer = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(writer);

			// Get all fields of the Str class
			Field[] fields = Str.class.getDeclaredFields();
			int columnCount = Str.LANGUAGES.length;

			// Write headers using field names
			int fieldCount = fields.length;
			for (int k = 0; k < fieldCount; k++) {
				Field field = fields[k];
				if (k == fieldCount - 1) {
					System.out.println("" + k);
				}
				StringBuilder rowString = new StringBuilder();
				if (field.getType().isArray()) {
					String[] values = (String[]) field.get(null);
					int nValueCount = values.length;
					for (int i = 0; i <= columnCount; i++) {
						if (nValueCount < i) {
							String strIssue = "Issue: ";
							for (String value : values) {
								strIssue += value + ",";
							}
							System.out.println(strIssue);
							break;
						}
						if (i == 0) {
							rowString.append(field.getName()).append(",");
						} else {
							String strValue = values[i - 1];
							strValue = strValue.replace("\n", "%%%n%%%%");
							strValue = strValue.replace(",", "%%%%%n%%%%%");
							if (i == columnCount) {
								rowString.append(strValue);
							} else {
								rowString.append(strValue).append(",");
							}
						}
					}
				}
				rowString.append("\n");
				bufferedWriter.write(rowString.toString());  // Write the full content
				bufferedWriter.flush();  // Ensure all data is flushed to the file
			}
			System.out.println("CSV file created successfully: " + csvFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

