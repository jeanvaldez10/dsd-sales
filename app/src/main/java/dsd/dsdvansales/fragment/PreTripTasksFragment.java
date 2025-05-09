package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.content.pm.PackageInfo;
import android.graphics.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.*;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.itextpdf.text.pdf.BaseFont;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;

import org.apache.commons.net.ftp.FTPClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.api.ConnectApiClient;
import dsd.dsdvansales.api.TourApiClient;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.db.PrepareUploadDbAdapter;
import dsd.dsdvansales.dialogs.LoadDataCheckPopupDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.dialogs.NewOrdersFoundDialog;
import dsd.dsdvansales.dialogs.PresoldDialog;
import dsd.dsdvansales.dialogs.TourDialog;
import dsd.dsdvansales.global.BixolonPrinter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.HoneywellLinePrinter;
import dsd.dsdvansales.global.HoneywellPrinter;
import dsd.dsdvansales.global.PrintEngine;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.BatchLot;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.CustomerOrderInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.DocTextInfo;
import dsd.dsdvansales.model.LoadBlotInfo;
import dsd.dsdvansales.model.LoadDetl;
import dsd.dsdvansales.model.LoadDetlInfo;
import dsd.dsdvansales.model.LoadHead;
import dsd.dsdvansales.model.LoadHeadInfo;
import dsd.dsdvansales.model.LoadTripResponse;
import dsd.dsdvansales.model.LoginInfo;
import dsd.dsdvansales.model.MessageInfo;
import dsd.dsdvansales.model.OrderLineInfo;
import dsd.dsdvansales.model.PresoldOrHInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.SurveyInfo;
import dsd.dsdvansales.model.TourInfoResult;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UInvenBlotDInfo;
import dsd.dsdvansales.model.UInvenBlotInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;
import dsd.dsdvansales.model.UStockDInfo;
import dsd.dsdvansales.model.UStockHInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreTripTasksFragment extends BaseFragment implements View.OnClickListener {

	private class TaskMenuInfo {
		public int nIdx = 0;
		public String strName = "";
		public boolean bCompleted = false;
		public boolean bEnabled = false;
	}

	private static final int	MESSAGE_BOARD	= 0;
	private static final int	DELIVERY_PLAN	= 1;
	private static final int	PRINT_SALES_PLAN= 2;
	private static final int	INSPECTIONS		= 3;
	private static final int	LOAD_INVENTORY	= 4;
	private static final int	LOAD_CONTAINERS	= 5;
	private static final int	LOAD_EQUIPMENT	= 6;

	private static final int 	VEHICLE_INVENTORY = 0;
	private static final int 	RESEND_DATA = 1;

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<TaskMenuInfo> mTaskList = new ArrayList<>();

	private TaskMenuInfo surveyMenuInfo = null;

	private String mStrPdfFileName = "";
	private Connection printerConnection = null;
	private String mStrMacAddress = "";
	private String mStrFriendlyName = "";

	private String mCartonDescription = "";
	private boolean bShowTour = false;

	PrintEngine mHoneywellPrintEngine = null;
	String mHoneywellTemplatePath = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}


	private void doGetLatestSalesOrder()
	{
		if(Utils.getStringSetting(mContext, Global.PREF_GETORDERS).equals("1"))
		{
			if(!Utils.possibleCheckNewOrders(mContext))
			{
				Toast.makeText(mContext, Str.No_New_Order_Found[Global.LANG], Toast.LENGTH_LONG).show();
				doVisitSalesPlan();
				return;
			}

			final String strBaseUrl = Utils.getBaseURL(Utils.getStringSetting(mContext, Global.PREF_APICOMPANY));
			if(!TextUtils.isEmpty(strBaseUrl) && Utils.isConnectingToInternet(mContext))
			{
				showProgressDialog(Str.CheckingForNewOrders[Global.LANG]);

				Map<String, String> params = new HashMap<>();
				params.put("username", Utils.getStringSetting(mContext, Global.PREF_APIUSER));
				params.put("password", Utils.getStringSetting(mContext, Global.PREF_APIPASSWORD));
				ConnectApiClient.getMainApiClient(strBaseUrl, null).login(params).enqueue(new Callback<LoginInfo>() {
					@Override
					public void onResponse(Call<LoginInfo> call, Response<LoginInfo> response) {

						if(response.isSuccessful() && response.body() != null)
						{
							final LoginInfo body = response.body();
							Map<String, String> headers = new HashMap<>();
							headers.put("X-Auth-Token", body.token);
							Map<String, String> params = new HashMap<>();
							params.put("deliveryDate", new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
							params.put("tripNumber", Global.gTripNumber);
							Callback<List<CustomerOrderInfo>> callback = new Callback<List<CustomerOrderInfo>>() {
								@Override
								public void onResponse(Call<List<CustomerOrderInfo>> call, final Response<List<CustomerOrderInfo>> response) {

									if(response.isSuccessful() && response.body() != null && response.body().size() > 0)
									{
										new Thread()
										{
											@Override
											public void run() {
												List<CustomerOrderInfo> orderInfoList = response.body();
												DbAdapter dbAdapter = new DbAdapter(mContext);
												dbAdapter.open();
												Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();
												Map<String, ProdDetlInfo> mProdDetlUPCMap = new HashMap<>();
												dbAdapter.getProductList(mProdDetlItemMap, mProdDetlUPCMap);
												if(Global.gCustomerList == null || Global.gCustomerList.size() == 0)
												{
													Global.gCustomerList = filterSameCustomer(dbAdapter.getWhiteCustomerInfo(false, Utils.getIntSetting(mContext,  Global.DAY_NO)));

													Global.gCustomerList.addAll(dbAdapter.getCustomerList(true, false, Utils.getIntSetting(mContext, Global.DAY_NO)));
												}
												final List<CustomerOrderInfo> customerOrderInfoList = new ArrayList<>();
												final Map<String, CustomerInfo> customerMap = new HashMap<>();
												if(orderInfoList != null && orderInfoList.size() > 0)
												{
													for(CustomerOrderInfo orderInfo : orderInfoList)
													{
														if(dbAdapter.getLatestOrder(orderInfo.orderNumber) != null) continue;
														if(dbAdapter.getPresoldOrHInfoWithOrderNo(orderInfo.orderNumber) != null) continue;
														if(orderInfo.orderLines == null || orderInfo.orderLines.size() == 0) continue;
														//Check This Customer is available for today, and if not, we will add this customer.
														CustomerInfo customerInfo = dbAdapter.getCustomerInfo(orderInfo.customerNumber, orderInfo.chainNumber);
														if(customerInfo != null && customerInfo.nCustNo == orderInfo.customerNumber && customerInfo.nChainNo == orderInfo.chainNumber)
														{
															customerMap.put(customerInfo.nCustNo + "&&" + customerInfo.nChainNo, customerInfo);
															customerOrderInfoList.add(orderInfo);
														}
													}
												}

												dbAdapter.close();

												new Handler(Looper.getMainLooper()).post(() -> {
													hideProgressDialog();
													if(customerOrderInfoList.size() > 0)
													{
														//Show the Popup dialog to select users.
														NewOrdersFoundDialog dialog = new NewOrdersFoundDialog(mContext, customerOrderInfoList, customerMap, (selOrderInfoList, unSelOrderInfoList) -> {
															showProgressDialog(Str.AddingNewOrders[Global.LANG]);
															new Thread()
															{
																@Override
																public void run() {
																	final List<CustomerInfo> newCustomerList = new ArrayList<>();
																	Map<String, CustomerInfo> customerInfoMap = new HashMap<>();
																	dbAdapter.open();
																	for(CustomerOrderInfo orderInfo : selOrderInfoList)
																	{
																		CustomerInfo customerInfo = customerMap.get(orderInfo.customerNumber + "&&" + orderInfo.chainNumber);
																		if(customerInfoMap.get(orderInfo.customerNumber + "/" + orderInfo.chainNumber) == null)
																		{
																			newCustomerList.add(customerInfo);
																			customerInfoMap.put(orderInfo.customerNumber + "/" + orderInfo.chainNumber, customerInfo);
																		}

																		if(orderInfo.orderLines != null)
																		{
																			for(OrderLineInfo orderLineInfo : orderInfo.orderLines)
																			{
																				ProdDetlInfo productInfo = mProdDetlItemMap.get(orderLineInfo.itemNumber);
																				if(productInfo != null)
																				{
																					orderLineInfo.strShortDesc = productInfo.strShortDesc;
																					orderLineInfo.strLongDesc = productInfo.strDescription;
																				}
																			}
																		}
																		dbAdapter.insertLastestOrder(orderInfo);
																		if(!customerInfo.bIsRouteSch)
																		{
																			customerInfo.bIsRouteSch = true;
																			customerInfo.bFromAddCustomer = true;
																			customerInfo.nSeqNo = 0;
																			customerInfo.strStartTime1 = "0000";
																			customerInfo.strArrivalTime = customerInfo.strStartTime1;
																			customerInfo.nDayNo = Utils.getIntSetting(mContext, Global.DAY_NO);
																			if(Global.gCustomerList != null )
																			{
																				customerInfo.nOrderNo = 0;
																				for(CustomerInfo otherInfo: Global.gCustomerList)
																				{
																					otherInfo.nOrderNo++;
																				}
																			}
																			else
																			{
																				customerInfo.nOrderNo = 0;
																			}
																			Global.gCustomerList.add(0, customerInfo);

																			if(customerInfo.strDeliveryWindowFrom.length() > 0 && !customerInfo.strDeliveryWindowFrom.equals("0000"))
																			{
																				customerInfo.strStartTime1 = customerInfo.strDeliveryWindowFrom;
																				customerInfo.strArrivalTime = customerInfo.strStartTime1;
																			}
																			if(customerInfo.strDeliveryWindowTo.length() > 0 && !customerInfo.strDeliveryWindowTo.equals("0000"))
																			{
																				customerInfo.strEndTime1 = customerInfo.strDeliveryWindowTo;
																			}

																			int nCustomerCount = dbAdapter.getCustomerListCount(true);
																			customerInfo.nOrgSeqNo = nCustomerCount;
																			dbAdapter.updateCustomerInfo(customerInfo);
																		}
																		else
																		{
																			boolean bExist = false;
																			for(CustomerInfo info : Global.gCustomerList)
																			{
																				if(info.nCustNo == customerInfo.nCustNo && info.nChainNo == customerInfo.nChainNo)
																				{
																					bExist = true;
																				}
																			}
																			if(!bExist)
																			{
																				Global.gCustomerList.add(0, customerInfo);
																				customerInfo.nSeqNo = 0;
																				if(Global.gCustomerList != null )
																				{
																					customerInfo.nOrderNo = 0;
																					for(CustomerInfo otherInfo: Global.gCustomerList)
																					{
																						otherInfo.nOrderNo++;
																					}
																				}
																				else
																				{
																					customerInfo.nOrderNo = 0;
																				}
																				customerInfo.nPeriodNo = 0;
																				customerInfo.strDeliveryDate = "";
																				dbAdapter.resetSequNoForCustomer(customerInfo);
																				dbAdapter.registerWhiteCustomer(customerInfo.nChainNo, customerInfo.nCustNo, Utils.getIntSetting(mContext, Global.DAY_NO));
																				dbAdapter.updateCustomerInfo(customerInfo);
																			}
																		}

																		customerInfo.bCompleted = false;
																		dbAdapter.updateCustomerInfo(customerInfo);
																	}

																	//Register Ignored Order
																	for(CustomerOrderInfo orderInfo : unSelOrderInfoList)
																	{
																		dbAdapter.insertIgnoredLastestOrder(orderInfo);
																	}
																	dbAdapter.close();

																	new Handler(Looper.getMainLooper()).post(new Runnable() {
																		@Override
																		public void run() {
																			hideProgressDialog();
																			doVisitSalesPlan();
																		}
																	});
																}
															}.start();

														});
														dialog.show();
													}
													else
													{
														doVisitSalesPlan();
													}

												});
											}
										}.start();

									}
									else
									{
										hideProgressDialog();
										if(!mContext.isFinishing() && !mContext.isDestroyed())
										{
											try {
												Toast.makeText(mContext, Str.CouldNotConnect[Global.LANG], Toast.LENGTH_LONG).show();
											}catch (Exception e1)
											{
												e1.printStackTrace();
											}
										}

										doVisitSalesPlan();
									}

								}

								@Override
								public void onFailure(Call<List<CustomerOrderInfo>> call, Throwable t) {
									hideProgressDialog();
									if(!mContext.isFinishing() && !mContext.isDestroyed())
									{
										try {
											Toast.makeText(mContext, Str.CouldNotConnect[Global.LANG], Toast.LENGTH_LONG).show();
										}catch (Exception e1)
										{
											e1.printStackTrace();
										}
									}

									doVisitSalesPlan();
								}
							};
							if(Utils.getBooleanSetting(mContext, Global.PREF_GET_SALES_DELAY))
							{
								ConnectApiClient.getMainApiClient(strBaseUrl, headers).delay(params).enqueue(callback);
							}
							else
							{
								ConnectApiClient.getMainApiClient(strBaseUrl, headers).getSalesOrder(params).enqueue(callback);
							}
						}
						else {
							hideProgressDialog();
							if(!mContext.isFinishing() && !mContext.isDestroyed())
							{
								try {
									Toast.makeText(mContext, Str.CouldNotConnect[Global.LANG], Toast.LENGTH_LONG).show();
								}catch (Exception e1)
								{
									e1.printStackTrace();
								}
							}

							doVisitSalesPlan();
						}
					}

					@Override
					public void onFailure(Call<LoginInfo> call, Throwable t) {
						hideProgressDialog();
						if(!mContext.isFinishing() && !mContext.isDestroyed())
						{
							try {
								Toast.makeText(mContext, Str.CouldNotConnect[Global.LANG], Toast.LENGTH_LONG).show();
							}catch (Exception e1)
							{
								e1.printStackTrace();
							}
						}

						doVisitSalesPlan();
					}
				});
			}
			else
			{
				doVisitSalesPlan();
			}
		}
		else
		{
			doVisitSalesPlan();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mRootView = inflater.inflate(R.layout.fragment_pre_trip_tasks, container, false);
		mContext = (MainActivity) getActivity();
		if(Global.bVisitAutoSelectCustomer)
		{
			int nCustNo = 0;
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			List<MessageInfo> messageInfoList = dbAdapter.getMessageList(nCustNo);
			boolean bCompleted = dbAdapter.getSurveyInfoLoaded(99);
			dbAdapter.close();
			if( messageInfoList != null && messageInfoList.size() > 0 )
			{
				boolean bRead = true;
				for(MessageInfo info : messageInfoList )
				{
					if( info.bIsOldMessage == false )
					{
						bRead = false;
						break;
					}
				}
				if( bRead )
				{
					if( bCompleted )
					{
						gotoSelectCustomer();
						return mRootView;
					}
				}
			}
		}

		String strFontFamily = Utils.getStringSetting(mContext, Global.PREF_PRINT_FONT_FAMILY);
		String strFontEncoding = Utils.getStringSetting(mContext, Global.PREF_PRINT_FONT_ENCODING);
		String strFontFileName = Utils.getStringSetting(mContext, Global.PREF_PRINT_FONT_FILENAME);
		if(!TextUtils.isEmpty(strFontEncoding) && !TextUtils.isEmpty(strFontFileName))
		{
			Utils.registerPrintFontFamily(strFontFileName ,strFontFamily, strFontEncoding, Utils.getBooleanSetting(mContext, Global.PREF_PRINT_FONT_EMBEDED));
		}


		if(!bShowTour)
		{
			bShowTour = true;
			PackageInfo packageInfo = null;
			try {
				packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
				final String keyName = Global.PREF_TOUR_PASSED + "(" + packageInfo.versionName + ")";
				if(!Utils.getBooleanSetting(mContext, keyName))
				{
					showProgressDialog(Str.Loading[Global.LANG]);
					Map<String, String> params = new HashMap<>();
					params.put("app_name", "DSDVanSales");
					params.put("username", Utils.getStringSetting(mContext, Global.PREF_APICOMPANY));
					params.put("version", packageInfo.versionName);
					params.put("model", "Android");

				/*params.put("app_name", "DSDSalesForce");
				params.put("username", "general");
				params.put("version", "5.36");
				params.put("model", "iPad");*/

					TourApiClient.getApiClient().getTourInfo(params).enqueue(new Callback<TourInfoResult>() {
						@Override
						public void onResponse(Call<TourInfoResult> call, Response<TourInfoResult> response) {

							final TourInfoResult result = response.body();
							if(result != null && result.result == 1 && result.tour_details.size() > 0)
							{
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										hideProgressDialog();
										TourDialog dialog = new TourDialog(mContext, result.tour_details, new TourDialog.TourCallback() {
											@Override
											public void onSkip() {
												init();
											}

											@Override
											public void onClose() {
												init();
											}

											@Override
											public void onCheck(boolean b) {
												Utils.setBooleanSetting(mContext, keyName, b);
											}
										});
										dialog.setCancelable(false);
										dialog.show();
									}
								});
							}
							else
							{
								init();
							}
						}

						@Override
						public void onFailure(Call<TourInfoResult> call, Throwable t) {
//							hideProgressDialog();
							Toast.makeText(mContext, Str.network_connection_error[Global.LANG], Toast.LENGTH_LONG).show();
							init();
						}
					});
				}
				else
				{
					init();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else
		{
			init();
		}
		return mRootView;
	}

	public void init()
	{
		if(!Utils.getBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD)/* && Utils.getIntSetting(mContext, Global.PREF_LOADMULTIALL) > 0*/)
		{
			SpecialLoadProcessWithApi();
		}
		else {
			processLoad();
		}
	}

	public void processLoad() {
		showProgressDialog(Str.Loading[Global.LANG]);
		new Thread()
		{
			@Override
			public void run() {
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				List<LoadHeadInfo> loadHeadInfoList = dbAdapter.getLoadProcessedHeadInfo();
				List<LoadHeadInfo> unProcessedHeaderInfoList = dbAdapter.getLLoadUnProcessedHeadInfo();
				if( /*Utils.getBooleanSetting(mContext, Global.PREF_PASSED_LOAD_SECTION) == false &&*/
						unProcessedHeaderInfoList.size() > 0  &&
						Utils.getIntSetting(mContext, Global.PREF_LOADMULTIALL) == 0)
//				if( /*Utils.getBooleanSetting(mContext, Global.PREF_PASSED_LOAD_SECTION) == false &&
//						(loadHeadInfoList == null || loadHeadInfoList.size() == 0 ) &&*/
//						Utils.getIntSetting(mContext, Global.PREF_LOADMULTIALL) == 0)
				{
					boolean bLoaded = dbAdapter.getSelectedLoadHeadInfo().size() > 0;
					if( bLoaded || unProcessedHeaderInfoList.size() == 0 )
					{
						dbAdapter.close();
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								hideProgressDialog();
								commonInit();
							}
						});
						return;
					}
					else
					{
						Date curDate = new Date(System.currentTimeMillis());
						SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
						String strDate = format.format(curDate);
						List<LoadHeadInfo> datas = dbAdapter.getLoadHeadInfo(strDate);

						if( datas.size() > 0 )
						{
							for(LoadHeadInfo info : datas)
							{
								info.bLoadTripDataFlag = true;
								dbAdapter.updateLoadHeadInfo(info);
							}
							dbAdapter.close();
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									hideProgressDialog();
									init();
								}
							});
							return;
						}
						dbAdapter.close();
						if(mContext == null)
						{
							mContext = (MainActivity)getActivity();
						}
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								hideProgressDialog();
								try {
									LoadDataCheckPopupDialog dialog = new LoadDataCheckPopupDialog(mContext);
									if (Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 1 || Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 2) {
										dialog.setNextScreen(new LoadTruckByCustomerFragment());        // Load Truck by Customer
									} else {
										Global.bTransferLoad = false;
										dialog.setNextScreen(new LoadTruckBulkFragment());        // Load Truck BULK
									}
									dialog.setCancelable(false);
									dialog.show();
								}catch (Exception e)
								{
									e.printStackTrace();
								}
							}
						});
						return;

					}
				}
				else
				{
					//Load "R" only
					List<LoadHeadInfo> datas = dbAdapter.getRLoadHeadInfo();
					if( datas.size() > 0 )
					{
						Date date = new Date();
						long trxnNo = date.getTime();
						List<LoadDetlInfo> allLoadDetlInfoList = new ArrayList<>();

						Map<String, Double> rBeginQtyMap = new HashMap<>();
						for(LoadHeadInfo info : datas)
						{
							info.bLoadTripDataFlag = true;
							info.bLoadCompleted = true;
							dbAdapter.updateLoadHeadInfo(info);

							UStockHInfo stockHInfo = new UStockHInfo();
							stockHInfo.nTrxnNo = trxnNo++;
							int nDocNo = Utils.getIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_PDF_SEQUENCE_NO, Global.gTripNumber));

							String strRRR;
							if (Global.gRouteNumber.length() > 3) {
								strRRR = Global.gRouteNumber.substring(Global.gRouteNumber.length() - 3);
							} else {
								strRRR = Global.gRouteNumber;
							}
							String strY = new SimpleDateFormat("yyyy").format(new Date()).substring(3);
							Calendar cal = Calendar.getInstance();
							int nDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
							String strJJJ = String.format(Locale.ENGLISH, "%03d", nDayOfYear);
							String strSSS = String.format(Locale.ENGLISH, "%03d", nDocNo);
							stockHInfo.strDocNo = strRRR + strY + strJJJ + strSSS;

							stockHInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, date));
							stockHInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(date);

							stockHInfo.strReference = info.strType;

							if(info.strType.equals("R"))
							{
								stockHInfo.nTrxnNo = setGeneralUploadData("LOAD", 0, 0, stockHInfo.nTrxnNo, "Rollover", "");
								TransactionInfo transactionInfo = new TransactionInfo();
								transactionInfo.strType = "LOAD";
								transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(stockHInfo.nTrxnNo));
								dbAdapter.insertTransactionHistoryInfo(transactionInfo);

								List<LoadBlotInfo> loadBlotInfoList = dbAdapter.getLoadBlot(info.strDetailFile);
								Map<String, List<LoadBlotInfo>> loadBlotMap = new HashMap<>();
								for(LoadBlotInfo loadBlotInfo : loadBlotInfoList)
								{
									List<LoadBlotInfo> infoList = loadBlotMap.get(loadBlotInfo.strItemNo);
									if(infoList == null)
									{
										infoList = new ArrayList<>();
										loadBlotMap.put(loadBlotInfo.strItemNo, infoList);
									}
									infoList.add(loadBlotInfo);
								}

								List<LoadDetlInfo> loadDetlInfoList = dbAdapter.getRLoadDetl(info.strDetailFile);
								allLoadDetlInfoList.addAll(loadDetlInfoList);
								for(LoadDetlInfo loadDetlInfo : loadDetlInfoList)
								{
									UStockDInfo stockDInfo = new UStockDInfo();
									stockDInfo.nTrxnNo = stockHInfo.nTrxnNo;
									stockDInfo.strItemNo = loadDetlInfo.strItemNo;
									stockDInfo.nReasonCode = 0;
									if(Utils.getProdLoconSCWType(mContext, loadDetlInfo.strItemNo) == Global.WEIGHT_ITEM)
									{
										double nValue = 0;
										if(rBeginQtyMap.get(loadDetlInfo.strItemNo) != null)
										{
											try {
												nValue += rBeginQtyMap.get(loadDetlInfo.strItemNo);
											}catch (Exception e)
											{
												e.printStackTrace();
											}
										}
										stockDInfo.dActualQty = loadDetlInfo.dQty;
										stockDInfo.dBeginQty = nValue;
										stockDInfo.dSuggestQty = loadDetlInfo.dQty;
										nValue += loadDetlInfo.dQty;
										rBeginQtyMap.put(loadDetlInfo.strItemNo, nValue);
									}
									else
									{
										double nValue = 0;
										if(rBeginQtyMap.get(loadDetlInfo.strItemNo) != null)
										{
											try {
												nValue += rBeginQtyMap.get(loadDetlInfo.strItemNo);
											}catch (Exception e)
											{
												e.printStackTrace();
											}
										}
										stockDInfo.nActualQty = loadDetlInfo.nQty;
										stockDInfo.nBeginQty = (int)nValue;
										stockDInfo.nSuggestQty = loadDetlInfo.nQty;
										nValue += loadDetlInfo.nQty;
										rBeginQtyMap.put(loadDetlInfo.strItemNo, nValue);
									}

									if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
									{
										List<LoadBlotInfo> infoList = loadBlotMap.get(loadDetlInfo.strItemNo);
										if(infoList != null && infoList.size() > 0)
										{
											ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(loadDetlInfo.strItemNo);
											if(prodDetlInfo.strLotControl.equalsIgnoreCase("Y"))
											{
												for(LoadBlotInfo loadBlotInfo : infoList)
												{
													BatchLot batchLot = new BatchLot();
													batchLot.nTrxnNo = stockHInfo.nTrxnNo;
													batchLot.strItemNo = prodDetlInfo.strItemNo;
													batchLot.nLocnNo = stockDInfo.nLocnNo;
													batchLot.nReasonCode = stockDInfo.nReasonCode;
													batchLot.nSequence = stockDInfo.nSequence;
													batchLot.strLotNumber = loadBlotInfo.strLotNumber;
													batchLot.strBatchDate = loadBlotInfo.strBatchDate;
													batchLot.nActualQty = loadBlotInfo.nLotQty;
													batchLot.nSuggestQty = loadBlotInfo.nLotQty;
													dbAdapter.insertBatchLot(batchLot);
												}
											}
										}
									}

									dbAdapter.insertUStockDInfoList(stockDInfo);
								}
								stockHInfo.strDetailKeyExpanded = info.strDetailFile;
								dbAdapter.insertUStockHInfoList(stockHInfo);
							}
						}

				/*Global.gAdjustinfo = dbAdapter.getUInvenHInfo(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
				if (Global.gAdjustinfo == null) {
					Global.gAdjustinfo = new UInvenHInfo();
					Global.gAdjustinfo.nTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
				}

				List<UInvenDInfo> uInvenDList = dbAdapter.getUInvenDList(Global.gAdjustinfo.nTrxnNo);*/
						if(Utils.getBooleanSetting(mContext, Global.PREF_PASSED_LOAD_SECTION) == false && allLoadDetlInfoList.size() > 0)
						{
							long nTrxnNo = System.currentTimeMillis();
							Global.gRTrxnDate = Global.gAdjustinfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(nTrxnNo)));
							Global.gRTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(nTrxnNo);
							//Make UInventory with type = 'R'
							if(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO) == 0)
							{
								m_lTrxnNo = trxnNo;
								Global.gUInvenHinfo = new UInvenHInfo();
								Global.gUInvenHinfo.nTrxnNo = m_lTrxnNo;
								Global.gUInvenHinfo.strDocType = "LOAD";
								Global.gUInvenHinfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(m_lTrxnNo)));
								Global.gUInvenHinfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(new Date(m_lTrxnNo));
								dbAdapter.insertUInvenHInfo(Global.gUInvenHinfo);

								Utils.setLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO, m_lTrxnNo);
								setGeneralUploadData("LOAD", 0, 0, m_lTrxnNo + 1, "Rollover", "");
								//setGeneralUploadData("LOAD", 0, 0, m_lTrxnNo + 1);
								Global.gUInvenHinfo.nTrxnNo = m_lTrxnNo;
								dbAdapter.insertUInvenHInfo(Global.gUInvenHinfo);

								List<UInvenDInfo> uInvenDList = new ArrayList<>();
								List<String> strCodeList = new ArrayList<>();
								List<ProdDetlInfo> mProdDetlList = dbAdapter.getProdDetlList();
								Map<String, Map<String, List<LoadBlotInfo>>> blotMap = new HashMap<>();

								HashMap<String, Boolean> loadedMap = new HashMap<>();
								for (LoadDetlInfo loadDetlInfo : allLoadDetlInfoList) {
									String strItemNo = loadDetlInfo.strItemNo;
									if (Boolean.TRUE.equals(loadedMap.get(strItemNo))) {
										continue;
									}
									Double itemQty = rBeginQtyMap.get(strItemNo);
									if (itemQty == null) {
										continue;
									}

									UInvenDInfo uInvenDInfo = new UInvenDInfo();
									uInvenDInfo.strItemNo = strItemNo;
									uInvenDInfo.strPickLocationCode = loadDetlInfo.strPickLocationCode;
									if(Utils.getProdLoconSCWType(mContext, uInvenDInfo.strItemNo) == Global.WEIGHT_ITEM)
									{
										uInvenDInfo.dBeginQty = itemQty;
										uInvenDInfo.dLoaded = 0;
										uInvenDInfo.dStock = itemQty;
									}
									else
									{
										uInvenDInfo.nBeginQty = itemQty.intValue();;
										uInvenDInfo.nLoaded = 0;
										uInvenDInfo.nStock = itemQty.intValue();;
									}

									uInvenDInfo.nSequence = uInvenDInfo.nIdx;
									uInvenDInfo.nSCWType = Global.INVENTORY_TRUCK;
									//uInvenDInfo.bShow = uInvenDInfo.strPickLocationCode.trim().length() > 0;
									//Modified by Andrew 2016-9.11
									uInvenDInfo.bShow = true;
									for (ProdDetlInfo info : mProdDetlList) {
										if (info.strItemNo.equals(strItemNo) == true) {
											uInvenDInfo.strShortDesc = info.strShortDesc;
											uInvenDInfo.strDescription = info.strDescription;
											uInvenDInfo.strUOM = info.prodLocnInfo.strCaseUOM.length() > 0 ? info.prodLocnInfo.strCaseUOM : info.prodLocnInfo.strUnitUOM;
											uInvenDInfo.strFullCase = info.prodLocnInfo.strFullCase;
											uInvenDInfo.nWeight = info.nWeight;

											if(info.strLotControl.equalsIgnoreCase("Y") && Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
											{
												uInvenDInfo.bLotControl = true;
											}
											break;
										}
									}

									if(uInvenDInfo.bLotControl)
									{
										//Load the LoadBlot information from the db.
										Map<String, List<LoadBlotInfo>> blotDetailMap = blotMap.get(loadDetlInfo.strDetailFile);
										if(blotDetailMap == null)
										{
											blotDetailMap = new HashMap<>();
											blotMap.put(loadDetlInfo.strDetailFile, blotDetailMap);

											List<LoadBlotInfo> loadBlotInfoList = dbAdapter.getLoadBlot(loadDetlInfo.strDetailFile);

											for(LoadBlotInfo loadBlotInfo : loadBlotInfoList)
											{
												List<LoadBlotInfo> infoList = blotDetailMap.get(loadBlotInfo.strItemNo);
												if(infoList == null)
												{
													infoList = new ArrayList<>();
													blotDetailMap.put(loadBlotInfo.strItemNo, infoList);
												}
												infoList.add(loadBlotInfo);
											}
										}

										uInvenDInfo.loadBlotInfoList = blotDetailMap.get(uInvenDInfo.strItemNo);
									}

									uInvenDList.add(uInvenDInfo);
									if (uInvenDInfo.strPickLocationCode.length() > 0) {
										boolean bExist = false;
										for (String strCode : strCodeList) {
											if (strCode.equals(uInvenDInfo.strPickLocationCode) == true) {
												bExist = true;
												break;
											}
										}
										if (bExist == false) {
											strCodeList.add(uInvenDInfo.strPickLocationCode);
										}
									}
									loadedMap.put(strItemNo, true);
                                }

//								for (LoadDetlInfo loadDetlInfo : allLoadDetlInfoList) {
//									UInvenDInfo uInvenDInfo = new UInvenDInfo();
//									uInvenDInfo.strItemNo = loadDetlInfo.strItemNo;
//									uInvenDInfo.strPickLocationCode = loadDetlInfo.strPickLocationCode;
//									if(Utils.getProdLoconSCWType(mContext, uInvenDInfo.strItemNo) == Global.WEIGHT_ITEM)
//									{
//										uInvenDInfo.dBeginQty = loadDetlInfo.dQty;
//										uInvenDInfo.dLoaded = 0;
//										uInvenDInfo.dStock = loadDetlInfo.dQty;
//									}
//									else
//									{
//										uInvenDInfo.nBeginQty = loadDetlInfo.nQty;
//										uInvenDInfo.nLoaded = 0;
//										uInvenDInfo.nStock = loadDetlInfo.nQty;
//									}
//
//									uInvenDInfo.nSequence = uInvenDInfo.nIdx;
//									uInvenDInfo.nSCWType = Global.INVENTORY_TRUCK;
//									//uInvenDInfo.bShow = uInvenDInfo.strPickLocationCode.trim().length() > 0;
//									//Modified by Andrew 2016-9.11
//									uInvenDInfo.bShow = true;
//									for (ProdDetlInfo info : mProdDetlList) {
//										if (info.strItemNo.equals(loadDetlInfo.strItemNo) == true) {
//											uInvenDInfo.strShortDesc = info.strShortDesc;
//											uInvenDInfo.strDescription = info.strDescription;
//											uInvenDInfo.strUOM = info.prodLocnInfo.strCaseUOM.length() > 0 ? info.prodLocnInfo.strCaseUOM : info.prodLocnInfo.strUnitUOM;
//											uInvenDInfo.strFullCase = info.prodLocnInfo.strFullCase;
//											uInvenDInfo.nWeight = info.nWeight;
//
//											if(info.strLotControl.equalsIgnoreCase("Y") && Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
//											{
//												uInvenDInfo.bLotControl = true;
//											}
//											break;
//										}
//									}
//
//									if(uInvenDInfo.bLotControl)
//									{
//										//Load the LoadBlot information from the db.
//										Map<String, List<LoadBlotInfo>> blotDetailMap = blotMap.get(loadDetlInfo.strDetailFile);
//										if(blotDetailMap == null)
//										{
//											blotDetailMap = new HashMap<>();
//											blotMap.put(loadDetlInfo.strDetailFile, blotDetailMap);
//
//											List<LoadBlotInfo> loadBlotInfoList = dbAdapter.getLoadBlot(loadDetlInfo.strDetailFile);
//
//											for(LoadBlotInfo loadBlotInfo : loadBlotInfoList)
//											{
//												List<LoadBlotInfo> infoList = blotDetailMap.get(loadBlotInfo.strItemNo);
//												if(infoList == null)
//												{
//													infoList = new ArrayList<>();
//													blotDetailMap.put(loadBlotInfo.strItemNo, infoList);
//												}
//												infoList.add(loadBlotInfo);
//											}
//										}
//
//										uInvenDInfo.loadBlotInfoList = blotDetailMap.get(uInvenDInfo.strItemNo);
//									}
//
//									uInvenDList.add(uInvenDInfo);
//									if (uInvenDInfo.strPickLocationCode.length() > 0) {
//										boolean bExist = false;
//										for (String strCode : strCodeList) {
//											if (strCode.equals(uInvenDInfo.strPickLocationCode) == true) {
//												bExist = true;
//												break;
//											}
//										}
//										if (bExist == false) {
//											strCodeList.add(uInvenDInfo.strPickLocationCode);
//										}
//									}
//
//								}

								//Make 'R' Load
								UInvenBlotInfo uInvenBlotInfo = Utils.getUInvenBlot(Global.gUInvenHinfo, "", Global.gTripNumber);
								List<UInvenBlotDInfo> uInvenBlotDInfoList = new ArrayList<>();
								for(UInvenDInfo info : uInvenDList)
								{
									info.nTrxnNo = m_lTrxnNo - 1;
									dbAdapter.insertUInvenDInfo(info);

									info.nTrxnNo = m_lTrxnNo;
									dbAdapter.insertUInvenDInfo(info);
									uInvenBlotDInfoList.addAll(Utils.getUInvenBlotList(info));
								}

								if(uInvenBlotDInfoList.size() > 0)
								{
									dbAdapter.insertULoadBlot(uInvenBlotInfo);
									dbAdapter.insertULoadBlotList(uInvenBlotDInfoList);
								}
								//Set LoadCompleted Flag = 1
							}
						}
					}
					dbAdapter.close();

					if(makeZipFileInfo != null && makeZipFileInfo.trxnList.size() > 0)
					{
						//Global.gUploadCustNoList.add(makeZipFileInfo);
						Utils.registerUploadFile(mContext, makeZipFileInfo);
					}
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							hideProgressDialog();
							commonInit();
						}
					});
				}
			}
		}.start();

		new Thread()
		{
			@Override
			public void run() {
				Global.gUploadCustNoList.clear();
				PrepareUploadDbAdapter dbAdapter = new PrepareUploadDbAdapter(mContext);
				dbAdapter.open();
				Global.gUploadCustNoList.addAll(dbAdapter.getMakeZipFileInfoList());
				dbAdapter.close();
			}
		}.start();
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_PRE_TRIP_TASKS;
		if( Global.gCurCustomerInfo == null )
			Global.gCurCustomerInfo = new CustomerInfo();
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnExit).setOnClickListener(this);
		if(Utils.getBooleanSetting(mContext, Global.PREF_LOGIN_AS_SUPER_VISOR))
		{
			mRootView.findViewById(R.id.btnCompleted).setVisibility(View.GONE);
			mRootView.findViewById(R.id.btnCompleted).setOnClickListener(null);
		}
		else
		{
			mRootView.findViewById(R.id.btnCompleted).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);
		}

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mListView = (ListView) mRootView.findViewById(R.id.listView);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);

		((TextView) mRootView.findViewById(R.id.txtWelcome)).setText("Hi " + Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION));
		refreshLanguage();
	}

	@Override
	public void refreshDriverName() {
		((TextView) mRootView.findViewById(R.id.txtWelcome)).setText("Hi " + Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION));
	}

	@Override
	public void loadData() {
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<MessageInfo> messageList = dbAdapter.getMessageList(0);
		List<CustomerInfo> customerList = dbAdapter.getCustomerList(true);
		SurveyInfo surveyInfo = dbAdapter.getSurveyInfo(99);

		dbAdapter.close();

		mTaskList.clear();
		TaskMenuInfo info;

		if(Utils.getBooleanSetting(mContext, Global.PREF_LOGIN_AS_SUPER_VISOR))
		{
			info = new TaskMenuInfo();
			info.nIdx = VEHICLE_INVENTORY;
			info.strName = Str.VehicleInventory[Global.LANG];
			info.bEnabled = true;
			mTaskList.add(info);

			info = new TaskMenuInfo();
			info.nIdx = RESEND_DATA;
			info.strName = Str.ResendData[Global.LANG];
			info.bEnabled = true;
			mTaskList.add(info);
		}
		else
		{
			info = new TaskMenuInfo();
			info.nIdx = MESSAGE_BOARD;
			info.strName = Str.MessageBoard[Global.LANG];
			info.bEnabled = messageList.size() > 0;
			mTaskList.add(info);

			info = new TaskMenuInfo();
			info.nIdx = DELIVERY_PLAN;
			info.strName = Str.SalesPlan[Global.LANG];
			info.bEnabled = customerList.size() > 0;
			mTaskList.add(info);

			info = new TaskMenuInfo();
			info.nIdx = PRINT_SALES_PLAN;
			info.strName = Str.PrintSalesPlanForToday[Global.LANG];
			String strFinalPrintTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_SALES_PLAN_TEMPLATE_FILENAME);
			info.bEnabled = Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) != 0 && Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + strFinalPrintTemplateFileName);

			mTaskList.add(info);

			info = new TaskMenuInfo();
			info.nIdx = INSPECTIONS;
			info.strName = Str.Inspections[Global.LANG];
			info.bEnabled = surveyInfo.nSurveyType == 99;
			info.bCompleted = surveyInfo.bCompleted;
			surveyMenuInfo = info;
			mTaskList.add(info);

			boolean bLoadInventoryEnabled;
			if( Utils.getIntSetting(mContext, Global.PREF_LOADMULTIALL) == 1)
			{
				DbAdapter adapter = new DbAdapter(mContext);
				adapter.open();
				List<LoadHeadInfo> unProcessedList = adapter.getLoadUnProcessedHeadInfo();
				List<LoadHeadInfo> processedList = adapter.getLoadedHeadInfo();
				int nProcessCount = (processedList != null) ? processedList.size() : 0;
				int nUnProcessCount = (unProcessedList != null) ? unProcessedList.size() : 0;
				adapter.close();

				if( nProcessCount > 0 && nUnProcessCount > 0 )
				{
					dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					bLoadInventoryEnabled = dbAdapter.getLoadHeadInfo().size() > 0;
					dbAdapter.close();
				}
				else
				{
					bLoadInventoryEnabled = (Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO) == 0);
				}
			}
			else
			{
				bLoadInventoryEnabled = (Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO) == 0);
			}

			info = new TaskMenuInfo();
			info.nIdx = LOAD_INVENTORY;
			info.strName = Str.LoadInventory[Global.LANG];
			info.bEnabled = bLoadInventoryEnabled || !Utils.getBooleanSetting(mContext, Global.PREF_LOAD_INVENTORY_COMPLETED);
			mTaskList.add(info);

			boolean bLoadContainer = Utils.getIntSetting(mContext, Global.PREF_LOAD_CONTAINERS) == 1;
			info = new TaskMenuInfo();
			info.nIdx = LOAD_CONTAINERS;
			info.strName = Str.LoadContainers[Global.LANG];
			info.bEnabled = bLoadContainer && bLoadInventoryEnabled;
			mTaskList.add(info);
		}

		refreshPage();

		if(Utils.getStringSetting(mContext, Global.PREF_ENABLE_BREAKS).equals("1") && Utils.getLongSetting(mContext, Global.PREF_BREAK_DOWN_START_TIME) != 0)
		{
			new Handler(Looper.getMainLooper()).post(() -> mContext.BreakDownContinue());
		}
		else
		{
			//Check the INVOICEFMT.xml file and DELIVERYFMT.xml file
			checkPrintFormat();
		}

		/*info = new TaskMenuInfo();
		info.nIdx = LOAD_EQUIPMENT;
		info.strName = Str.LoadEquipment[Global.LANG];
		info.bEnabled = true;
		mTaskList.add(info);*/



	}
	public void refreshPageAndContinue() {
		refreshPage();

		if(Utils.getStringSetting(mContext, Global.PREF_ENABLE_BREAKS).equals("1") && Utils.getLongSetting(mContext, Global.PREF_BREAK_DOWN_START_TIME) != 0)
		{
			new Handler(Looper.getMainLooper()).post(() -> mContext.BreakDownContinue());
		}
		else
		{
			//Check the INVOICEFMT.xml file and DELIVERYFMT.xml file
			checkPrintFormat();
		}
	}

	public void checkPrintFormat()
	{
		new Thread()
		{
			@Override
			public void run() {
				String strDeliveryFmtFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_TEMPLATE_DELIVERY_FMT_FILENAME);
				String strInvoiceFmtFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_TEMPLATE_INVOICE_FMT_FILENAME);
				if(!Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + strDeliveryFmtFileName) ||
						!Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + strInvoiceFmtFileName) ||
						!Utils.isXmlFileCorrect(Global.ROOT_PRINT_TEMPLATE_LOCCATION + strDeliveryFmtFileName) ||
						!Utils.isXmlFileCorrect(Global.ROOT_PRINT_TEMPLATE_LOCCATION + strInvoiceFmtFileName))
				{
					new Handler(Looper.getMainLooper()).post(() -> {
						try
						{
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.PrintFormatDamagedOrMissing[Global.LANG];
							msgDialog.mStrLeft = Str.Return[Global.LANG];
							msgDialog.mStrRight = Str.DownloadingNow[Global.LANG];
							msgDialog.setOnDismissListener(dialog -> {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									if(!Utils.isConnectingToInternet(mContext))
									{
										Toast.makeText(mContext, Str.NoInternetConnection[Global.LANG], Toast.LENGTH_LONG ).show();
										checkPrintFormat();
									}
									else {
										showProgressDialog(Str.Loading[Global.LANG]);
										new Thread()
										{
											@Override
											public void run() {
												String mStrFTPCompanyName = Utils.getStringSetting(mContext, Global.PREF_FTP_COMPANY_NAME);
												String mStrFTPServerIP = Utils.getStringSetting(mContext, Global.PREF_FTP_SERVER_IP);
												String mStrFTPUserName = Utils.getStringSetting(mContext, Global.PREF_FTP_USERNAME);
												String mStrFTPPassword = Utils.getStringSetting(mContext, Global.PREF_FTP_PASSWORD);

												FTPClient ftpClient = null;
												if(!Utils.getStringSetting(mContext, Global.PREF_DisableFTP).equals("1"))
												{
													ftpClient = Utils.connnectingwithFTP(mStrFTPServerIP, Global.DEFAULT_FTP_PORT, mStrFTPUserName, mStrFTPPassword);
												}

												String strDeliveryFmtFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_TEMPLATE_DELIVERY_FMT_FILENAME);
												if(!Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + strDeliveryFmtFileName) ||
														!Utils.isXmlFileCorrect(Global.ROOT_PRINT_TEMPLATE_LOCCATION + strDeliveryFmtFileName))
												{
													String strParentDir = String.format(Locale.ENGLISH, Global.FTP_REPORTS_DIR, mStrFTPCompanyName);
													String strFileName = Global.PRINT_TEMPLATE_DELIVERY_FMT_FILENAME;
													String strTargetFile = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strFileName;
													Utils.downloadFileFromServer(mContext, strParentDir, strFileName, strTargetFile, ftpClient);
												}

												String strInvoiceFmtFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_TEMPLATE_INVOICE_FMT_FILENAME);
												if(!Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + strInvoiceFmtFileName) ||
														!Utils.isXmlFileCorrect(Global.ROOT_PRINT_TEMPLATE_LOCCATION + strInvoiceFmtFileName))
												{
													String strParentDir = String.format(Locale.ENGLISH, Global.FTP_REPORTS_DIR, mStrFTPCompanyName);
													String strFileName = Global.PRINT_TEMPLATE_INVOICE_FMT_FILENAME;
													String strTargetFile = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strFileName;
													Utils.downloadFileFromServer(mContext, strParentDir, strFileName, strTargetFile, ftpClient);
												}

												if(ftpClient != null)
												{
													try {
														ftpClient.disconnect();
													}catch (Exception e)
													{
														e.printStackTrace();
													}
												}

												new Handler(Looper.getMainLooper()).post(() -> hideProgressDialog());
											}
										}.start();
									}



								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					});

				}
			}
		}.start();
	}

	@Override
	public void refreshPage() {
		if (mTaskList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}

		if( Global.bVisitAutoSelectCustomer )
		{
			Global.bVisitAutoSelectCustomer = false;
			mRootView.findViewById(R.id.btnCompleted).performClick();
		}
	}

	@Override
	public void refreshLanguage() {
		if(Utils.getBooleanSetting(mContext, Global.PREF_LOGIN_AS_SUPER_VISOR))
		{
			((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.SuperVisor[Global.LANG].toUpperCase());
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.PreTripTasks[Global.LANG].toUpperCase());
		}
		((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " #" + Global.gTripNumber);
		((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Completed[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		loadData();
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
		if (strAction.equals(Global.ACTION_ADDITIONAL_DOWNLOAD_FINISHED)) {
			Toast.makeText(mContext, "Additional download finished", Toast.LENGTH_SHORT).show();
			loadData();
		}
	}

	private List<CustomerInfo> filterSameCustomer(List<CustomerInfo> customerInfoList)
	{
		Map<String, CustomerInfo> customerInfoMap = new HashMap<>();
		List<CustomerInfo> newList = new ArrayList<>();
		for(CustomerInfo customerInfo : customerInfoList)
		{
			String key = customerInfo.nChainNo + "___" + customerInfo.nCustNo;
			if(customerInfoMap.get(key) == null)
			{
				customerInfoMap.put(key, customerInfo);
				newList.add(customerInfo);
			}
		}
		return newList;
	}

	private void doPrint()
	{
		if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + Global.PRINT_SALES_PLAN_TEMPLATE_FILENAME))
		{
			new Thread()
			{
				@Override
				public void run() {
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							showProgressDialog(Str.CreatingPdf[Global.LANG]);
						}
					});

					PrintEngine printEngine = new PrintEngine();
					mHoneywellPrintEngine = printEngine;

					Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
					mStrPdfFileName = getPDFFileName();
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList(Global.UNITOFMEASURE_DESC_TYPE_ID);
					mCartonDescription = "";
					for(DescTypeInfo descTypeInfo : descTypeInfoList)
					{
						if( descTypeInfo.strAlphaKey.toUpperCase().equals("Z") )
						{
							if(TextUtils.isEmpty(mCartonDescription))
								mCartonDescription = descTypeInfo.strDescription;
							else
								mCartonDescription += "\n" + descTypeInfo.strDescription;
						}
					}
					//Global.gCustomerList = dbAdapter.getWhiteCustomerInfo(Utils.getIntSetting(mContext, Global.DAY_NO));
					//Global.gCustomerList.addAll(dbAdapter.getCustomerList(true, Utils.getIntSetting(mContext, Global.DAY_NO)));

					Global.gCustomerList = filterSameCustomer(dbAdapter.getWhiteCustomerInfo(false, Utils.getIntSetting(mContext, Global.DAY_NO)));
					Global.gCustomerList.addAll(dbAdapter.getCustomerList(true, false, Utils.getIntSetting(mContext, Global.DAY_NO)));

					List<CustomerInfo> delList = new ArrayList<>();
					for (int i=0; i<Global.gCustomerList.size(); i++) {
						Global.gCustomerList.get(i).nMessageCnt = dbAdapter.getMessageList(Global.gCustomerList.get(i).nCustNo).size();
						if( Global.gCustomerList.get(i).bCompleted )
						{
							delList.add(Global.gCustomerList.get(i));
						}
					}
					for(CustomerInfo info : delList)
					{
						Global.gCustomerList.remove(info);
					}

					//Add PresoldH file Information
					for(CustomerInfo info : Global.gCustomerList)
					{

						info.presoldOrHInfo = dbAdapter.getPresoldOrHInfo(info.nChainNo, info.nCustNo, info.nPeriodNo, info.nSeqNo);

						String[] custInfoMsgs = Utils.getStringSetting(mContext, Global.PREF_CUSTINFO_MSG).split(",");
						info.strCustMsg= "";
						for(int i = 0; i < custInfoMsgs.length; i++)
						{
							int nTextType = Utils.parseIntFromString(custInfoMsgs[i].trim());
							List<DocTextInfo> docTextInfoList = dbAdapter.getDocTextList(info.nCustNo, nTextType);
							for(DocTextInfo docTextInfo : docTextInfoList)
							{
								if(TextUtils.isEmpty(info.strCustMsg))
								{
									info.strCustMsg = docTextInfo.strDocText;
								}
								else
								{
									info.strCustMsg += "\n" + docTextInfo.strDocText;
								}
							}
						}

					}
					dbAdapter.close();

					if (Global.gCustomerList.size() > 0) {
						if (Global.gCustomerList.get(0).nOrderNo == -1) {
							Collections.sort(Global.gCustomerList, CustomerInfo.presold_seqNoComparator);
							Collections.sort(Global.gCustomerList, CustomerInfo.presold_periodNoComparator);
							Collections.sort(Global.gCustomerList, CustomerInfo.presold_deliveryDateComparator);
							Collections.sort(Global.gCustomerList, CustomerInfo.deliveryDateComparator);
							Collections.sort(Global.gCustomerList, CustomerInfo.seqNoComparator);
							Collections.sort(Global.gCustomerList, CustomerInfo.periodNoComparator);
							Collections.sort(Global.gCustomerList, new Comparator<CustomerInfo>() {
								@Override
								public int compare(CustomerInfo lhs, CustomerInfo rhs) {
									int nDefPeriodNo = Utils.getIntSetting(mContext, Global.PREF_LOADPERDDEF);
									int nFirstValue = 1;
									int nSecondValue = 1;
									if( lhs.presoldOrHInfo != null && lhs.presoldOrHInfo.size() > 0 )
									{
										boolean bDef = false;
										for(PresoldOrHInfo presoldOrHInfo : lhs.presoldOrHInfo)
										{
											if(presoldOrHInfo.nPeriodNo != nDefPeriodNo)
											{
												bDef = true;
												break;
											}
										}
										if( bDef == false ) nFirstValue = 0;
									}
									else
									{
										nFirstValue = 0;
									}

									if( rhs.presoldOrHInfo != null && rhs.presoldOrHInfo.size() > 0 )
									{
										boolean bDef = false;
										for(PresoldOrHInfo presoldOrHInfo : rhs.presoldOrHInfo)
										{
											if(presoldOrHInfo.nPeriodNo != nDefPeriodNo)
											{
												bDef = true;
												break;
											}
										}
										if( bDef == false ) nSecondValue = 0;
									}
									else
									{
										nSecondValue = 0;
									}

									return nFirstValue - nSecondValue;
								}
							});
							for (int i=0; i<Global.gCustomerList.size(); i++) {
								Global.gCustomerList.get(i).nOrderNo = i;
							}
						} else {
							Collections.sort(Global.gCustomerList, CustomerInfo.orderComparator);
						}
					}

					printEngine.PreparePrint(mContext, Global.gCustomerList, new Date(), mCartonDescription);

					String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_SALES_PLAN_TEMPLATE_FILENAME);
					mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
					printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, Global.SALES_PLAN_PRINT);
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							hideProgressDialog();
							outputPrinter();
						}
					});

				}
			}.start();

		}
		else
		{
			if(!mContext.isFinishing() && !mContext.isDestroyed())
			{
				try {
					Toast.makeText(mContext, Str.NoExistTemplateFile[Global.LANG], Toast.LENGTH_LONG).show();
				}catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}

		}
	}

	private void gotoTask(int nIdx) {
		if(Utils.getBooleanSetting(mContext, Global.PREF_LOGIN_AS_SUPER_VISOR))
		{
			switch (nIdx){
				case VEHICLE_INVENTORY:
				{
					Fragment fragment = new VehicleInventoryFragment();
					mContext.replaceFragment(fragment);
				}
				break;
				case RESEND_DATA:
				{
					Fragment fragment = new ResendDataFragment();
					mContext.replaceFragment(fragment);
				}
				break;
			}
		}
		else
		{
			switch (nIdx) {
				case MESSAGE_BOARD: {
					Bundle bundle = new Bundle();
					bundle.putInt(Global.EXTRA_CUSTOMER_NO, 0);
					bundle.putString(Global.EXTRA_CUSTOMER_NAME, "");
					Fragment fragment = new MessageBoardFragment();
					fragment.setArguments(bundle);
					mContext.replaceFragment(fragment);
					break;
				}
				case DELIVERY_PLAN:
					if(Utils.getStringSetting(mContext, Global.PREF_GETORDERS).equals("1"))
					{
						doGetLatestSalesOrder();
					}
					else
					{
						doVisitSalesPlan();
					}

					break;
				case PRINT_SALES_PLAN:
					doPrint();
					break;
				case INSPECTIONS: {
					Bundle bundle = new Bundle();
					bundle.putBoolean(Global.EXTRA_IS_FROM_END_TRIP, false);
					Fragment fragment = new PreTripCheckFragment();
					fragment.setArguments(bundle);
					mContext.replaceFragment(fragment);
					break;
				}
				case LOAD_INVENTORY:

					if(Utils.getIntSetting(mContext, Global.PREF_LOADMULTIALL) == 1)
					{
						try {
							if (Global.gLocationFilterList != null) {
								Global.gLocationFilterList.clear();
							}
							if (Global.gPrevLocationFilterList != null) {
								Global.gPrevLocationFilterList.clear();
							}
						}
						catch (Exception ex){
							ex.printStackTrace();
						}

						DbAdapter adapter = new DbAdapter(mContext);
						adapter.open();
						Date curDate = new Date(System.currentTimeMillis());
						SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
						String strDate = format.format(curDate);
						List<LoadHeadInfo> datas = adapter.getRLoadHeadInfo(strDate);

						if( datas.size() > 0 )
						{
							for(LoadHeadInfo info : datas)
							{
								info.bLoadTripDataFlag = true;
								adapter.updateLoadHeadInfo(info);
							}
							//adapter.close();

							long nTrxnNo = System.currentTimeMillis();
							Global.gRTrxnDate = Global.gAdjustinfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(nTrxnNo)));
							Global.gRTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(nTrxnNo);

						/*if (Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 1 || Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 2) {
							mContext.replaceFragment(new LoadTruckByCustomerFragment());        // Load Truck by Customer
						} else {sss
							mContext.replaceFragment(new LoadTruckBulkFragment());        // Load Truck BULK
						}
						break;*/
						}

						datas = adapter.getLoadUnProcessedHeadInfo();
						adapter.close();
						if(mContext == null)
						{
							mContext = (MainActivity)getActivity();
						}

						String strUserOrdersAsLoad = Utils.getStringSetting(mContext, Global.PREF_USE_ORDERS_AS_LOAD);
						try {
							LoadDataCheckPopupDialog dialog = new LoadDataCheckPopupDialog(mContext);
							dialog.bBackPreTrip = true;
							if(datas != null && datas.size() > 0 && (strUserOrdersAsLoad.equals("0") || strUserOrdersAsLoad.isEmpty()))
							{
								if (Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 1 || Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 2) {
									dialog.setNextScreen(new LoadTruckByCustomerFragment());        // Load Truck by Customer
								} else {
									Global.bTransferLoad = false;
									dialog.setNextScreen(new LoadTruckBulkFragment());        // Load Truck BULK
								}
								dialog.setCancelable(false);
								dialog.show();
							}
							else
							{
								if (Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 1 || Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 2) {
									mContext.replaceFragment(new LoadTruckByCustomerFragment());        // Load Truck by Customer
								} else {
									Global.bTransferLoad = false;
									mContext.replaceFragment(new LoadTruckBulkFragment());        // Load Truck BULK
								}
							}
						}catch (Exception e)
						{
							e.printStackTrace();
						}

					}
					else {
					/*DbAdapter adapter = new DbAdapter(mContext);
					adapter.open();
					adapter.updateRLoadStatusAsLoadCompleted();
					adapter.close();*/
						if (Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 1 || Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 2) {
							mContext.replaceFragment(new LoadTruckByCustomerFragment());        // Load Truck by Customer
						} else {
							Global.bTransferLoad = false;
							mContext.replaceFragment(new LoadTruckBulkFragment());        // Load Truck BULK
						}
					}
					break;
				case LOAD_CONTAINERS:
				/*if(Utils.getBooleanSetting(mContext, Global.PREF_PASSED_LOAD_SECTION) && Utils.getIntSetting(mContext, Global.PREF_LOADMULTIALL) == 1)
				{
					DbAdapter adapter = new DbAdapter(mContext);
					adapter.open();
					Date curDate = new Date(System.currentTimeMillis());
					SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
					String strDate = format.format(curDate);
					List<LoadHeadInfo> datas = adapter.getLoadHeadInfo(strDate);

					if( datas.size() > 0 )
					{
						for(LoadHeadInfo info : datas)
						{
							info.bLoadTripDataFlag = true;
							adapter.updateLoadHeadInfo(info);
						}
						adapter.close();
						mContext.replaceFragment(new LoadContainersFragment());
						break;
					}
					adapter.close();
					if(mContext == null)
					{
						mContext = (MainActivity)getActivity();
					}
					LoadDataCheckPopupDialog dialog = new LoadDataCheckPopupDialog(mContext);
					dialog.setNextScreen(new LoadContainersFragment());
					dialog.setCancelable(false);
					dialog.show();
				}
				else*/
				{
					/*DbAdapter adapter = new DbAdapter(mContext);
					adapter.open();
					adapter.updateRLoadStatusAsLoadCompleted();
					adapter.close();*/
					mContext.replaceFragment(new LoadContainersFragment());
				}
				break;
			/*case LOAD_EQUIPMENT:
				Utils.printDebug(mContext, "Load Equipment - coming soon...");
				break;*/
			}
		}
	}

	private void doVisitSalesPlan()
	{
		Global.gCurrentCustomerList = null;
		mContext.replaceFragment(new SalesPlanFragment());
	}

	public void doCompleted()
	{
		int nCustNo = 0;
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<MessageInfo> messageInfoList = dbAdapter.getMessageList(nCustNo);
		dbAdapter.close();
		if( messageInfoList != null && messageInfoList.size() > 0 )
		{
			boolean bRead = true;
			for(MessageInfo info : messageInfoList )
			{
				if( info.bIsOldMessage == false )
				{
					bRead = false;
					break;
				}
			}
			if( !bRead )
			{
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrTitle = Str.Alert[Global.LANG].toUpperCase();
					msgDialog.mStrMessage = Str.You_have_to_read_message_and_checked_before_complete[Global.LANG];
					msgDialog.mStrMiddle = Str.Ok[Global.LANG];
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				return;
			}
		}
		if( surveyMenuInfo != null )
		{
			if( !surveyMenuInfo.bCompleted && surveyMenuInfo.bEnabled )
			{
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrTitle = Str.Alert[Global.LANG].toUpperCase();
					msgDialog.mStrMessage = Str.You_have_to_complete_survey[Global.LANG];
					msgDialog.mStrMiddle = Str.Ok[Global.LANG];
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				return;
			}
		}


		if (Utils.getIntSetting(mContext, Global.PREF_LOADMULTIALL) == 0 && Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO) == 0) {
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrTitle = Str.Alert[Global.LANG].toUpperCase();
				msgDialog.mStrMessage = Str.InventoryNotLoaded_msg[Global.LANG];
				msgDialog.mStrMiddle = Str.Ok[Global.LANG];
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		} else {
			if (Utils.getBooleanSetting(mContext, Global.PREF_LOAD_INVENTORY_COMPLETED) == true) {
				gotoSelectCustomer();
			} else {
				//Check whether exits "L" type
				dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				List<LoadDetlInfo> loadDetlInfoList = dbAdapter.getLoadDetlList();
				dbAdapter.close();
				if( loadDetlInfoList != null && loadDetlInfoList.size() > 0 )
				{
					try
					{
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrTitle = Str.LoadNotComplete[Global.LANG].toUpperCase();
						msgDialog.mStrMessage = Str.LoadNotComplete_msg[Global.LANG];
						msgDialog.mStrLeft = Str.Return[Global.LANG];
						msgDialog.mStrRight = Str.Proceed[Global.LANG];
						msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									gotoSelectCustomer();
								}
							}
						});
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}


				}
				else
				{
					gotoSelectCustomer();
				}
			}
		}
	}

//	public void SpecialLoadProcessWithApi()
//	{
//		final String strBaseUrl = Utils.getBaseURL(Utils.getStringSetting(mContext, Global.PREF_APICOMPANY));
//
//		if(!TextUtils.isEmpty(strBaseUrl))
//		{
//			showProgressDialog(Str.checking_for_new_loads[Global.LANG]);
//			//Call Api to investigate
//
//			Map<String, String> params = new HashMap<>();
//			params.put("username", Utils.getStringSetting(mContext, Global.PREF_APIUSER));
//			params.put("password", Utils.getStringSetting(mContext, Global.PREF_APIPASSWORD));
//			try {
//				ConnectApiClient.getMainApiClient(strBaseUrl, null).login(params).enqueue(new retrofit2.Callback<LoginInfo>() {
//
//					@Override
//					public void onResponse(Call<LoginInfo> call, retrofit2.Response<LoginInfo> response) {
//						if(response.isSuccessful() && response.body() != null)
//						{
//							final LoginInfo body = response.body();
//							Map<String, String> headers = new HashMap<>();
//							headers.put("X-Auth-Token", body.token);
//
//							ConnectApiClient.getMainApiClient(strBaseUrl, headers).getLoad(Utils.getStringSetting(mContext, Global.PREF_TRIP_ID)).enqueue(new retrofit2.Callback<LoadTripResponse>() {
//								@Override
//								public void onResponse(Call<LoadTripResponse> call, retrofit2.Response<LoadTripResponse> response) {
//									if(response.isSuccessful() && response.body() != null)
//									{
//										//Load Data
//										new Thread()
//										{
//											@Override
//											public void run() {
//												List<LoadHead> loadHeadList = response.body().LoadHead;
//												boolean bFoundNewLoad = false;
//												if(loadHeadList.size() > 0)
//												{
//													DbAdapter dbAdapter = new DbAdapter(mContext);
//													dbAdapter.open();
//													for(LoadHead loadHead : loadHeadList)
//													{
//														LoadHeadInfo loadHeadInfo = dbAdapter.getLoadHeadInfoWithDetailFile(loadHead.DetailFile);
//														if(loadHeadInfo == null)
//														{
//															loadHeadInfo = new LoadHeadInfo();
//															loadHeadInfo.strLoadDate = loadHead.LoadDate;
//															loadHeadInfo.nDayNo = loadHead.DayNo;
//															loadHeadInfo.strType = loadHead.Type;
//															loadHeadInfo.strDescription = loadHead.Description;
//															loadHeadInfo.nPeriodNo = loadHead.PeriodNo;
//															loadHeadInfo.strDetailFile = loadHead.DetailFile;
//															loadHeadInfo.strRouteNumber = Global.gRouteNumber;
//															loadHeadInfo.strTripNumber = Global.gTripNumber;
//															dbAdapter.insertLoadHeadInfo(loadHeadInfo);
//
//															for(LoadDetl loadDetl : loadHead.LoadDetl)
//															{
//																LoadDetlInfo info = new LoadDetlInfo();
//																info.strDetailFile = loadDetl.DetailFile;
//																info.strItemNo = loadDetl.ItemNo;
//
//																String strNodeValue = String.valueOf(loadDetl.Qty);
//																if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
//																{
//																	info.nQty = Utils.getDividedValue(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
//																}
//																else
//																{
//																	info.nQty = 0;
//																}
//
//																strNodeValue = String.valueOf(loadDetl.Weight);
//																if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
//																{
//																	info.dQty = Utils.getRoundedValue(Utils.getDividedDoubleValue(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING))));
//																}
//																else
//																{
//																	info.dQty = 0;
//																}
//
//																bFoundNewLoad = true;
//																dbAdapter.insertLoadDetlInfo(info);
//															}
//														}
//													}
//
//													dbAdapter.close();
//												}
//												final boolean bFinalFoundNewLoad = bFoundNewLoad;
//												new Handler(Looper.getMainLooper()).post(new Runnable() {
//													@Override
//													public void run() {
//														Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
//														hideProgressDialog();
//														mContext.replaceFragment(new SelectCustomerFragment());
//
//														if(bFinalFoundNewLoad){
//															Toast.makeText(mContext, Str.new_load_found[Global.LANG], Toast.LENGTH_LONG).show();
//														}
//													}
//												});
//											}
//										}.start();
//									}
//									else
//									{
//										Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
//										hideProgressDialog();
//										mContext.replaceFragment(new SelectCustomerFragment());
//									}
//								}
//
//								@Override
//								public void onFailure(Call<LoadTripResponse> call, Throwable t) {
//									Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
//									hideProgressDialog();
//									mContext.replaceFragment(new SelectCustomerFragment());
//								}
//							});
//						}
//						else
//						{
//							Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
//							hideProgressDialog();
//							mContext.replaceFragment(new SelectCustomerFragment());
//						}
//					}
//
//					@Override
//					public void onFailure(Call<LoginInfo> call, Throwable t) {
//						Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
//						hideProgressDialog();
//						mContext.replaceFragment(new SelectCustomerFragment());
//					}
//				});
//			}catch (Exception e)
//			{
//				e.printStackTrace();
//				Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
//				hideProgressDialog();
//				mContext.replaceFragment(new SelectCustomerFragment());
//			}
//		}
//		else
//		{
//			mContext.replaceFragment(new SelectCustomerFragment());
//		}
//	}

	public void SpecialLoadProcessWithApi()
	{
		final String strBaseUrl = Utils.getBaseURL(Utils.getStringSetting(mContext, Global.PREF_APICOMPANY));

		if(!TextUtils.isEmpty(strBaseUrl))
		{
			showProgressDialog(Str.checking_for_new_loads[Global.LANG]);
			//Call Api to investigate

			Map<String, String> params = new HashMap<>();
			params.put("username", Utils.getStringSetting(mContext, Global.PREF_APIUSER));
			params.put("password", Utils.getStringSetting(mContext, Global.PREF_APIPASSWORD));
			try {
				ConnectApiClient.getMainApiClient(strBaseUrl, null).login(params).enqueue(new retrofit2.Callback<LoginInfo>() {

					@Override
					public void onResponse(Call<LoginInfo> call, retrofit2.Response<LoginInfo> response) {
						if(response.isSuccessful() && response.body() != null)
						{
							final LoginInfo body = response.body();
							Map<String, String> headers = new HashMap<>();
							headers.put("X-Auth-Token", body.token);

							ConnectApiClient.getMainApiClient(strBaseUrl, headers).getLoad(Utils.getStringSetting(mContext, Global.PREF_TRIP_ID)).enqueue(new retrofit2.Callback<LoadTripResponse>() {
								@Override
								public void onResponse(Call<LoadTripResponse> call, retrofit2.Response<LoadTripResponse> response) {
									if(response.isSuccessful() && response.body() != null)
									{
										//Load Data
										new Thread()
										{
											@Override
											public void run() {
												List<LoadHead> loadHeadList = response.body().LoadHead;
												boolean bFoundNewLoad = false;
												if(loadHeadList.size() > 0)
												{
													DbAdapter dbAdapter = new DbAdapter(mContext);
													dbAdapter.open();
													for(LoadHead loadHead : loadHeadList)
													{
														LoadHeadInfo loadHeadInfo = dbAdapter.getLoadHeadInfoWithDetailFile(loadHead.DetailFile);
														if(loadHeadInfo == null)
														{
															loadHeadInfo = new LoadHeadInfo();
															loadHeadInfo.strLoadDate = loadHead.LoadDate;
															loadHeadInfo.nDayNo = loadHead.DayNo;
															loadHeadInfo.strType = loadHead.Type;
															loadHeadInfo.strDescription = loadHead.Description;
															loadHeadInfo.nPeriodNo = loadHead.PeriodNo;
															loadHeadInfo.strDetailFile = loadHead.DetailFile;
															loadHeadInfo.strRouteNumber = Global.gRouteNumber;
															loadHeadInfo.strTripNumber = Global.gTripNumber;
															dbAdapter.insertLoadHeadInfo(loadHeadInfo);

															for(LoadDetl loadDetl : loadHead.LoadDetl)
															{
																LoadDetlInfo info = new LoadDetlInfo();
																info.strDetailFile = loadDetl.DetailFile;
																info.strItemNo = loadDetl.ItemNo;

																String strNodeValue = String.valueOf(loadDetl.Qty);
																if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
																{
																	info.nQty = Utils.getDividedValue(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
																}
																else
																{
																	info.nQty = 0;
																}

																strNodeValue = String.valueOf(loadDetl.Weight);
																if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
																{
																	info.dQty = Utils.getRoundedValue(Utils.getDividedDoubleValue(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING))));
																}
																else
																{
																	info.dQty = 0;
																}

																bFoundNewLoad = true;
																dbAdapter.insertLoadDetlInfo(info);
															}
														}
													}

													dbAdapter.close();
												}
												final boolean bFinalFoundNewLoad = bFoundNewLoad;

												new Handler(Looper.getMainLooper()).post(new Runnable() {
													@Override
													public void run() {
														Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
														hideProgressDialog();
														processLoad();

														if(bFinalFoundNewLoad){
															Toast.makeText(mContext, Str.new_load_found[Global.LANG], Toast.LENGTH_LONG).show();
														}
													}
												});
											}
										}.start();
									}
									else
									{
										Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
										hideProgressDialog();
										processLoad();
									}
								}

								@Override
								public void onFailure(Call<LoadTripResponse> call, Throwable t) {
									Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
									hideProgressDialog();
									processLoad();
								}
							});
						}
						else
						{
							Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
							hideProgressDialog();
							processLoad();
						}
					}

					@Override
					public void onFailure(Call<LoginInfo> call, Throwable t) {
						Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
						hideProgressDialog();
						processLoad();
					}
				});
			}catch (Exception e)
			{
				e.printStackTrace();
				Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
				hideProgressDialog();
				processLoad();
			}
		}
		else
		{
			processLoad();
		}
	}

	private void gotoSelectCustomer() {
		Global.gCustomerList.clear();

		//Remove PresoldD with ChainNumber = 99999 -- VS202
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.deletePresoldInfoWithChainNo(99999);
		dbAdapter.close();

		//Check Inventory Transaction ID and reset this.
		if (Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO) == 0)
		{
			//Reset This value. -- 12th of Feb, 2020 -- From Wang.
			Utils.setLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO, System.currentTimeMillis());
		}

		mContext.replaceFragment(new SelectCustomerFragment());
//		if(!Utils.getBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD) && Utils.getIntSetting(mContext, Global.PREF_LOADMULTIALL) > 0)
//		{
//			SpecialLoadProcessWithApi();
//		}
//		else {
//			mContext.replaceFragment(new SelectCustomerFragment());
//		}
	}

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnExit:
				mContext.onBackPressed();
				break;
			case R.id.btnCompleted:
				doCompleted();

				break;
		}
		m_bClicked = false;
	}

	private class ListAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView txtName;
			public ImageView imgIcon;
		}

		public int getCount() {
			return mTaskList.size();
		}

		public TaskMenuInfo getItem(int position) {
			if(position < mTaskList.size())
			{
				return mTaskList.get(position);
			}
			else
			{
				return null;
			}
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ViewHolder holder;
			final TaskMenuInfo info = getItem(position);
			if(info == null) return null;
			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_pre_trip_tasks, parent, false);
				holder = new ViewHolder();
				holder.txtName = (TextView) view.findViewById(R.id.txtName);
				holder.imgIcon = (ImageView) view.findViewById(R.id.imgIcon);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.txtName.setText(info.strName);

			if (info.bEnabled == false) {
				view.setBackgroundResource(R.color.white_color);
				holder.txtName.setTextColor(Color.GRAY);
			} else {
				view.setBackgroundResource(R.drawable.table_normal);
				holder.txtName.setTextColor(Color.BLACK);
			}

			if(Utils.getBooleanSetting(mContext, Global.PREF_LOGIN_AS_SUPER_VISOR))
			{
				if (info.nIdx == VEHICLE_INVENTORY)
				{
					holder.imgIcon.setVisibility(View.GONE);
				}
				else if(info.nIdx == RESEND_DATA)
				{
					holder.imgIcon.setVisibility(View.GONE);
				}
			}

			else
			{
				if (info.nIdx == MESSAGE_BOARD) {
					if (info.bEnabled == true) {
						holder.imgIcon.setBackgroundResource(R.drawable.icon_message);
					} else {
						holder.imgIcon.setVisibility(View.GONE);
					}
				} else if (info.nIdx == DELIVERY_PLAN) {
					if (info.bEnabled == true) {
						holder.imgIcon.setBackgroundResource(R.drawable.icon_map);
					} else {
						holder.imgIcon.setVisibility(View.GONE);
					}
				} else if (info.nIdx == INSPECTIONS){
					if( info.bCompleted == true )
					{
						holder.imgIcon.setBackgroundResource(R.drawable.icon_check);
						view.setBackgroundResource(R.color.white_color);
						holder.txtName.setTextColor(Color.GRAY);
					}
					else
					{
						holder.imgIcon.setVisibility(View.GONE);
					}
				}
				else {
					if (info.bCompleted == true){
						holder.imgIcon.setBackgroundResource(R.drawable.icon_check);
					}
					else
					{
						holder.imgIcon.setVisibility(View.GONE);
					}
				}
			}

			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (info.bEnabled == true && info.bCompleted == false) {
						gotoTask(info.nIdx);
					}
				}
			});

			return view;
		}
	}

	private void outputPrinter() {
		if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false) {
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.TurnOnBluetooth_alert[Global.LANG];
				msgDialog.mStrLeft = Str.Cancel[Global.LANG];
				msgDialog.mStrRight = Str.Retry[Global.LANG];
				msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							outputPrinter();
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

						}
					}
				});
				msgDialog.show();
			}catch (Exception e){
				e.printStackTrace();
			}
			return;
		}
		if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
		{
			mStrMacAddress = Utils.getStringSetting(mContext, Global.PREF_PRINTER_MAC_ADDRESS);
		}
		else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
		{
			mStrMacAddress = Utils.getStringSetting(mContext, Global.PREF_HONEYWELL_PRINTER_MAC_ADDRESS);
		}
		else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
		{
			mStrMacAddress = Utils.getStringSetting(mContext, Global.PREF_HONEYWELL_LINE_PRINTER_MAC_ADDRESS);
		}
		else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
		{
			mStrMacAddress = Utils.getStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_MAC_ADDRESS);
			mStrFriendlyName = Utils.getStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_FRIENDLY_NAME);
		}

		if (mStrMacAddress.length() == 0) {
			if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
			{
				discoveryPrinters();
			}
			else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
			{
				discoveryHoneywellPrinters();
			}
			else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
			{
				discoveryHoneywellPrinters();
			}
			else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
			{
				discoveryBixolonPrinters();
			}
		} else {
			if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
			{
				new PrintPdfTask().execute();
			}
			else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
			{
				doHoneywellPrintPdfTask();
			}
			else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
			{
				doHoneywellLinePrintPdfTask();
			}
			else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
			{
				doBixolonPrintPdfTask();
			}
		}
	}

	private void doHoneywellPrintPdfTask()
	{
		showProgressDialog(Str.Printing[Global.LANG]);
		HoneywellPrinter.doPrint(mContext, new HoneywellPrinter.PrintCallback() {
			@Override
			public void printFinished() {
				hideProgressDialog();
				try {
					Utils.setStringSetting(mContext, Global.PREF_HONEYWELL_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			@Override
			public void printError(String error) {
				hideProgressDialog();
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrTitle = "Printer Error";
					msgDialog.mStrMessage = error;
					msgDialog.mStrLeft = Str.No[Global.LANG];
					msgDialog.mStrRight = Str.Yes[Global.LANG];
					msgDialog.setOnDismissListener(dialog -> {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							doHoneywellPrintPdfTask();
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName);
	}

	private void doBixolonPrintPdfTask()
	{
		showProgressDialog(Str.Printing[Global.LANG]);
		BixolonPrinter.doPrint(mContext, new BixolonPrinter.PrintCallback() {
			@Override
			public void printFinished() {
				hideProgressDialog();
				try {
					Utils.setStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.setStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_FRIENDLY_NAME, mStrFriendlyName);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			@Override
			public void printError(String error) {
				hideProgressDialog();
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrTitle = "Printer Error";
					msgDialog.mStrMessage = error;
					msgDialog.mStrLeft = Str.No[Global.LANG];
					msgDialog.mStrRight = Str.Yes[Global.LANG];
					msgDialog.setOnDismissListener(dialog -> {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							doBixolonPrintPdfTask();
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, mStrFriendlyName, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName);
	}

	private void doHoneywellLinePrintPdfTask()
	{
		if(Utils.isFileExist(mHoneywellTemplatePath))
		{
			showProgressDialog(Str.Printing[Global.LANG]);
			HoneywellLinePrinter.doPrintDotMatrix(mContext, new HoneywellLinePrinter.PrintCallback() {
				@Override
				public void printFinished() {
					hideProgressDialog();
					try {
						Utils.setStringSetting(mContext, Global.PREF_PRINTER_MAC_ADDRESS, mStrMacAddress);
						Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void printError(String error) {
					hideProgressDialog();
					try {
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrTitle = "Printer Error";
						msgDialog.mStrMessage = error;
						msgDialog.mStrLeft = Str.No[Global.LANG];
						msgDialog.mStrRight = Str.Yes[Global.LANG];
						msgDialog.setOnDismissListener(dialog -> {
							if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
								doHoneywellLinePrintPdfTask();
							} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
							}
						});
						msgDialog.show();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, mStrMacAddress, mHoneywellTemplatePath, mHoneywellPrintEngine);
		}
	}

	private void discoveryHoneywellPrinters()
	{
		showProgressDialog(Str.DiscoveryingHoneywellPrinters[Global.LANG]);
		new Thread(() -> {
			Looper.prepare();
			try {
				final CountDownLatch latch = new CountDownLatch(1);
				final boolean[] result = {false};
				BluetoothDiscoverer.findPrinters(mContext, new DiscoveryHandler() {
					List<String> deviceList = new ArrayList<>();
					@Override
					public void foundPrinter(DiscoveredPrinter printer) {
						String strFriendlyName = "";
						if (printer instanceof DiscoveredPrinterBluetooth) {
							strFriendlyName = ((DiscoveredPrinterBluetooth) printer).friendlyName;
						}
						String strDevice = printer.address + "\n";
						if (strFriendlyName != null) {
							strDevice += strFriendlyName;
						} else {
							strDevice += "Unknown";
						}
						deviceList.add(strDevice);
					}

					@Override
					public void discoveryFinished() {
						hideProgressDialog();
						if (deviceList.size() > 0) {
							showDeviceList(deviceList);
							result[0] = true;
						}
						else
						{
							result[0] = false;
						}
						latch.countDown();
					}

					@Override
					public void discoveryError(String message) {
						Utils.printDebug(mContext, "Discovery error - " + message);
						result[0] = false;
						latch.countDown();
					}
				});

				latch.await();
				if(result[0] == false)
				{
					hideProgressDialog();
					mHander.sendEmptyMessage(0);

				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Looper.myLooper().quit();
			}
		}).start();
	}

	private void discoveryBixolonPrinters()
	{
		showProgressDialog(Str.DiscoveryingBixolonPrinters[Global.LANG]);
		new Thread(() -> {
			Looper.prepare();
			try {
				final CountDownLatch latch = new CountDownLatch(1);
				final boolean[] result = {false};
				BluetoothDiscoverer.findPrinters(mContext, new DiscoveryHandler() {
					List<String> deviceList = new ArrayList<>();
					@Override
					public void foundPrinter(DiscoveredPrinter printer) {
						String strFriendlyName = "";
						if (printer instanceof DiscoveredPrinterBluetooth) {
							strFriendlyName = ((DiscoveredPrinterBluetooth) printer).friendlyName;
						}
						String strDevice = printer.address + "\n";
						if (strFriendlyName != null) {
							strDevice += strFriendlyName;
						} else {
							strDevice += "Unknown";
						}
						deviceList.add(strDevice);
					}

					@Override
					public void discoveryFinished() {
						hideProgressDialog();
						if (deviceList.size() > 0) {
							showDeviceList(deviceList);
							result[0] = true;
						}
						else
						{
							result[0] = false;
						}
						latch.countDown();
					}

					@Override
					public void discoveryError(String message) {
						Utils.printDebug(mContext, "Discovery error - " + message);
						result[0] = false;
						latch.countDown();
					}
				});

				latch.await();
				if(result[0] == false)
				{
					hideProgressDialog();
					mHander.sendEmptyMessage(0);

				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Looper.myLooper().quit();
			}
		}).start();
	}
	private void discoveryPrinters() {
		showProgressDialog(Str.DiscoveryingPrinters[Global.LANG]);
		new Thread(new Runnable() {
			public void run() {
				Looper.prepare();
				try {
					final CountDownLatch latch = new CountDownLatch(1);
					final boolean[] result = {false};
					BluetoothDiscoverer.findPrinters(mContext, new DiscoveryHandler() {
						List<String> deviceList = new ArrayList<>();
						@Override
						public void foundPrinter(DiscoveredPrinter printer) {
							String strFriendlyName = "";
							if (printer instanceof DiscoveredPrinterBluetooth) {
								strFriendlyName = ((DiscoveredPrinterBluetooth) printer).friendlyName;
							}
							String strDevice = printer.address + "\n";
							if (strFriendlyName != null) {
								strDevice += strFriendlyName;
							} else {
								strDevice += "Unknown";
							}
							deviceList.add(strDevice);
						}

						@Override
						public void discoveryFinished() {
							hideProgressDialog();
							if (deviceList.size() > 0) {
								showDeviceList(deviceList);
								result[0] = true;
							}
							else
							{
								result[0] = false;
							}
							latch.countDown();
						}

						@Override
						public void discoveryError(String message) {
							Utils.printDebug(mContext, "Discovery error - " + message);
							result[0] = false;
							latch.countDown();
						}
					});

					//latch.await(Global.DISCOVER_BLUETOOTH_TIMEOUT, TimeUnit.SECONDS);
					latch.await();

					if(result[0] == false)
					{
						hideProgressDialog();
						mHander.sendEmptyMessage(0);

					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Looper.myLooper().quit();
				}
			}
		}).start();
	}

	public Handler mHander = new Handler(){
		public void handleMessage(Message msg){
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.TurnOnZebraPrinter[Global.LANG];
				msgDialog.mStrLeft = Str.Cancel[Global.LANG];
				msgDialog.mStrRight = Str.Retry[Global.LANG];
				msgDialog.setOnDismissListener(dialog -> {
					if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
						if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
						{
							discoveryPrinters();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
						{
							discoveryHoneywellPrinters();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
						{
							discoveryHoneywellPrinters();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
						{
							discoveryBixolonPrinters();
						}
					} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
					}
				});
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	};
	private void showDeviceList(List<String> deviceList) {
		if(deviceList.size() == 0)
		{
			if(!mContext.isFinishing() && !mContext.isDestroyed())
			{
				try {
					Toast.makeText(mContext, Str.NoDeviceDiscover[Global.LANG], Toast.LENGTH_LONG).show();
				}catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}

			return;
		}
		final String strDevices[] = new String[deviceList.size()];
		for (int i=0; i<strDevices.length; i++) {
			strDevices[i] = deviceList.get(i);
		}
		/*AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(Str.SelectPrinter[Global.LANG]);
		builder.setItems(strDevices, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int position) {
				String strArray[] = strDevices[position].split("\n");
				if (strArray.length > 0) {
					mStrMacAddress = strArray[0];
					new PrintPdfTask().execute();
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.setCancelable(false);
		alert.show();*/

		String strArray[] = strDevices[0].split("\n");
		if (strArray.length > 0) {
			mStrMacAddress = strArray[0];
		}
		if (strArray.length > 1) {
			mStrFriendlyName = strArray[1];
		}

		try {
			new AlertDialog.Builder(mContext)
					.setTitle(Str.SelectPrinter[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strDevices), 0, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							String strArray[] = strDevices[i].split("\n");
							if (strArray.length > 0) {
								mStrMacAddress = strArray[0];
							}
							if (strArray.length > 1) {
								mStrFriendlyName = strArray[1];
							}
						}
					})
					.setPositiveButton(Str.Choose[Global.LANG], (dialogInterface, which) -> {
						if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
						{
							new PrintPdfTask().execute();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
						{
							doHoneywellPrintPdfTask();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
						{
							doHoneywellLinePrintPdfTask();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
						{
							doBixolonPrintPdfTask();
						}
					})
					.setNegativeButton(Str.Research[Global.LANG], (dialogInterface, i) -> {
						if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
						{
							discoveryPrinters();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
						{
							discoveryHoneywellPrinters();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
						{
							discoveryHoneywellPrinters();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
						{
							discoveryBixolonPrinters();
						}
					})
					.setCancelable(false)
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private class PrintPdfTask extends AsyncTask<Void, String, String> {
		int nMessageType = 0;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			try {
				showProgressDialog(Str.Printing[Global.LANG]);
			}catch (Exception e)
			{
				e.printStackTrace();
			}

		}

		@Override
		protected String doInBackground(Void... params) {
			String strResult = "Unknown Error";
			printerConnection = new BluetoothConnection(mStrMacAddress);
			try {
				printerConnection.open();
				ZebraPrinter printer = null;
				if (printerConnection.isConnected()) {
					printer = ZebraPrinterFactory.getInstance(printerConnection);


					if (printer != null) {
						PrinterStatus printerStatus = printer.getCurrentStatus();
						if(printerStatus.isReadyToPrint)
						{
							printer.sendFileContents(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName);
							strResult = "";
						}
						else if(printerStatus.isPaused)
						{
							strResult = Str.PrinterPaused[Global.LANG];
							nMessageType = 1;
						}
						else if(printerStatus.isHeadOpen)
						{
							strResult = Str.PrinterHeadOpen[Global.LANG];
							nMessageType = 1;
						}
						else if(printerStatus.isPaperOut)
						{
							strResult = Str.PrinterPaperOut[Global.LANG];
							nMessageType = 1;
						}
						else if(printerStatus.isRibbonOut)
						{
							strResult = Str.PrinterRibbonOut[Global.LANG];
							nMessageType = 1;
						}
						else if(printerStatus.isHeadCold)
						{
							strResult = Str.PrinterHeadCold[Global.LANG];
							nMessageType = 1;
						}
						else if(printerStatus.isHeadTooHot)
						{
							strResult = Str.PrinterHeadTooHot[Global.LANG];
							nMessageType = 1;
						}
						else if(printerStatus.isReceiveBufferFull)
						{
							strResult = Str.PrinterReceiverBufferFull[Global.LANG];
							nMessageType = 1;
						}
						else
						{
							strResult = Str.PrinterError[Global.LANG];
							nMessageType = 2;
						}

						printerConnection.close();
					}
				}
			} catch (ConnectionException e) {
				Utils.addPrintExceptionStackToFileIfNeeded(mContext, Global.ZEBRA_PRINT_ERROR_FILENAME, e);
				e.printStackTrace();
				strResult = e.getMessage();
				nMessageType = 2;
			} catch (ZebraPrinterLanguageUnknownException e) {
				Utils.addPrintExceptionStackToFileIfNeeded(mContext, Global.ZEBRA_PRINT_ERROR_FILENAME, e);
				e.printStackTrace();
				strResult = e.getMessage();
				nMessageType = 2;
			} catch(Exception e){
				Utils.addPrintExceptionStackToFileIfNeeded(mContext, Global.ZEBRA_PRINT_ERROR_FILENAME, e);
				e.printStackTrace();
				strResult = e.getMessage();
				nMessageType = 2;
			}
			return strResult;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try
			{
				hideProgressDialog();
				if (result.length() == 0) {
					Utils.setStringSetting(mContext, Global.PREF_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
				} else {
					try {
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						if(nMessageType == 1)
						{
							msgDialog.mStrMessage = result;
							msgDialog.mStrLeft = Str.Cancel[Global.LANG];
							msgDialog.mStrRight = Str.Retry[Global.LANG];
						}
						else
						{
							msgDialog.mStrMessage = Str.Cannot_connect_printer[Global.LANG];
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
						}
						msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									if(nMessageType == 2)
									{
										discoveryPrinters();
									}
									else
									{
										new PrintPdfTask().execute();
									}
								} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

								}
							}
						});
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}

				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}

		}
	}
}