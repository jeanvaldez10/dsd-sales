package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.*;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
//import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.nhaarman.listviewanimations.itemmanipulation.*;
import com.nhaarman.listviewanimations.util.*;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import dsd.dsdvansales.R;
import dsd.dsdvansales.api.ConnectApiClient;
import dsd.dsdvansales.api.RoutificApiClient;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.KeyInformationDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.dialogs.NewOrdersFoundDialog;
import dsd.dsdvansales.dialogs.NonDeliveryDialog;
import dsd.dsdvansales.dialogs.OptimizationDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.RoutificLogs;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.CustomerOrderInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.DocTextInfo;
import dsd.dsdvansales.model.LocationInfo;
import dsd.dsdvansales.model.LoginInfo;
import dsd.dsdvansales.model.OrderLineInfo;
import dsd.dsdvansales.model.PresoldOrHInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.RoutificJob;
import dsd.dsdvansales.model.RoutificLocation;
import dsd.dsdvansales.model.RoutificResponse;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.URouteSchBody;
import dsd.dsdvansales.model.URouteSchHeader;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit2.Call;

public class SalesPlanFragment extends BaseFragment implements View.OnClickListener {

	private TextView mTxtNoData = null;
	private TextView mTxtDetailNoData = null;
	private DynamicListView mListView = null;
	private DynamicListView mDetailListView = null;
	private ListAdapter mListAdapter = null;
	private DetailListAdapter mDetailListAdapter = null;
	private List<CustomerInfo> mCustomerList = new ArrayList<>();
	retrofit.Response<RoutificJob> res = null;
	private String mCartonDescription = "";
	private boolean bRes = false;

	private ProgressDialog mProgressDlg = null;

	private String strETATime = "", strETADate = "";
	private int today = 0;

	private int[] dayNoArray;
	private String[] dayStringArray;

	private List<Integer> mDayNoList = new ArrayList<>();

	private int nSelDayNo = 0;
	int nSelDateIdx = 0;
	boolean bLoaded = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(!bLoaded)
		{
			mRootView = inflater.inflate(R.layout.fragment_sales_plan, container, false);
			commonInit();
			bLoaded = true;
		}
		return mRootView;
	}

	private void doGetLatestSalesOrder()
	{
		if(Utils.getStringSetting(mContext, Global.PREF_GETORDERS).equals("1"))
		{
			if(!Utils.possibleCheckNewOrders(mContext))
			{
				Toast.makeText(mContext, Str.No_New_Order_Found[Global.LANG], Toast.LENGTH_LONG).show();
				mContext.onBackPressed();
				return;
			}

			final String strBaseUrl = Utils.getBaseURL(Utils.getStringSetting(mContext, Global.PREF_APICOMPANY));
			if(!TextUtils.isEmpty(strBaseUrl) && Utils.isConnectingToInternet(mContext))
			{
				showProgressDialog(Str.CheckingForNewOrders[Global.LANG]);
				Map<String, String> params = new HashMap<>();
				params.put("username", Utils.getStringSetting(mContext, Global.PREF_APIUSER));
				params.put("password", Utils.getStringSetting(mContext, Global.PREF_APIPASSWORD));
				ConnectApiClient.getMainApiClient(strBaseUrl, null).login(params).enqueue(new retrofit2.Callback<LoginInfo>() {
					@Override
					public void onResponse(Call<LoginInfo> call, retrofit2.Response<LoginInfo> response) {

						if(response.isSuccessful() && response.body() != null)
						{
							final LoginInfo body = response.body();
							Map<String, String> headers = new HashMap<>();
							headers.put("X-Auth-Token", body.token);
							Map<String, String> params = new HashMap<>();
							params.put("deliveryDate", new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
							params.put("tripNumber", Global.gTripNumber);
							retrofit2.Callback<List<CustomerOrderInfo>> callback = new retrofit2.Callback<List<CustomerOrderInfo>>() {
								@Override
								public void onResponse(Call<List<CustomerOrderInfo>> call, final retrofit2.Response<List<CustomerOrderInfo>> response) {

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
												final List<CustomerInfo> newCustomerList = new ArrayList<>();
												Map<String, CustomerInfo> customerInfoMap = new HashMap<>();
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

												new Handler(Looper.getMainLooper()).post(new Runnable() {
													@Override
													public void run() {
														hideProgressDialog();
														if(customerOrderInfoList.size() > 0)
														{
															NewOrdersFoundDialog dialog = new NewOrdersFoundDialog(mContext, customerOrderInfoList, customerMap, new NewOrdersFoundDialog.Callback() {
																@Override
																public void onSelect(final List<CustomerOrderInfo> selOrderInfoList, final List<CustomerOrderInfo> unSelOrderInfoList) {
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
																				for(OrderLineInfo orderLineInfo : orderInfo.orderLines)
																				{
																					ProdDetlInfo productInfo = mProdDetlItemMap.get(orderLineInfo.itemNumber);
																					if(productInfo != null)
																					{
																						orderLineInfo.strShortDesc = productInfo.strShortDesc;
																						orderLineInfo.strLongDesc = productInfo.strDescription;
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

																			//Register the Ignored order.
																			for(CustomerOrderInfo orderInfo : unSelOrderInfoList)
																			{
																				dbAdapter.insertIgnoredLastestOrder(orderInfo);
																			}
																			dbAdapter.close();

																			new Handler(Looper.getMainLooper()).post(new Runnable() {
																				@Override
																				public void run() {
																					hideProgressDialog();
																					mContext.onBackPressed();
																				}
																			});
																		}
																	}.start();

																}
															});
															dialog.show();
														}
														else
														{
															mContext.onBackPressed();
														}

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

										mContext.onBackPressed();
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

									mContext.onBackPressed();
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

							mContext.onBackPressed();
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

						mContext.onBackPressed();
					}
				});
			}
			else
			{
				mContext.onBackPressed();
			}
		}
		else
		{
			mContext.onBackPressed();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_DELIVERY_PLAN;
		Global.gCurCustomerInfo = null;
	}

	@Override
	public void initView() {
		nSelDayNo = Utils.getIntSetting(mContext, Global.DAY_NO);

		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnMap).setOnClickListener(this);
		mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mTxtDetailNoData = (TextView) mRootView.findViewById(R.id.txtDetailNoData);
		mListView = (DynamicListView) mRootView.findViewById(R.id.listView);
		mDetailListView = (DynamicListView) mRootView.findViewById(R.id.detail_listview);
		mListAdapter = new ListAdapter();
		mDetailListAdapter = new DetailListAdapter();
		mDetailListView.setAdapter(mDetailListAdapter);
		mListView.setAdapter(mListAdapter);
		mListView.enableDragAndDrop();
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				gotoCustomerDetail(mCustomerList.get(position));
			}
		});
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				mListView.startDragging(position);
				return true;
			}
		});

		today = Utils.getTodayWeekNo();
		//mDetailListView.enableDragAndDrop();

		loadData();
	}

	private void showNonDeliveryDialog() {

		try {
			final NonDeliveryDialog dlg = new NonDeliveryDialog(mContext);
			dlg.mType = Global.REASON_TYPE_NON_SERVICE;
			dlg.setCancelable(false);
			dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {

						Global.gCurCustomerInfo.bCompleted = true;
						DbAdapter dbAdapter = new DbAdapter(mContext);
						dbAdapter.open();
						dbAdapter.updateCustomerInfo(Global.gCurCustomerInfo);
						dbAdapter.close();
						mCustomerList.remove(Global.gCurCustomerInfo);

						refreshPage();
					}
				}
			});
			dlg.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void addDayNo(int nDayNo)
	{
		for(int i = 0; i < mDayNoList.size(); i++)
		{
			if(mDayNoList.get(i) == nDayNo)
			{
				break;
			}
			if( mDayNoList.get(i) > nDayNo)
			{
				mDayNoList.add(i, nDayNo);
				break;
			}
		}
		if( mDayNoList.size() == 0)
		{
			mDayNoList.add(nDayNo);
		}

		if(mDayNoList.get(mDayNoList.size() - 1) < nDayNo)
		{
			mDayNoList.add(nDayNo);
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

	@Override
	public void loadData() {

		DbAdapter dbAdapter;
		dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		mDayNoList = dbAdapter.getDayNo();
		dbAdapter.close();

		//Add Selected Date & Today
		addDayNo(nSelDayNo);
		addDayNo(today);

		if( Global.gCurrentCustomerList != null && Global.gCurrentCustomerList.size() > 0 ) {
			mCustomerList = Global.gCurrentCustomerList;
			refreshPage();
			refreshLanguage();
		} else {
			mCustomerList.clear();
			dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList(Global.UNITOFMEASURE_DESC_TYPE_ID);
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

			//mCustomerList = dbAdapter.getCustomerList(true, false, nSelDayNo);
			mCustomerList = filterSameCustomer(dbAdapter.getWhiteCustomerInfo(false, nSelDayNo));

			mCustomerList.addAll(dbAdapter.getCustomerList(true, false, nSelDayNo));

			List<CustomerInfo> delList = new ArrayList<>();
			for (int i=0; i<mCustomerList.size(); i++) {
				mCustomerList.get(i).nMessageCnt = dbAdapter.getMessageList(mCustomerList.get(i).nCustNo).size();
				if( mCustomerList.get(i).bCompleted )
				{
					delList.add(mCustomerList.get(i));
				}
			}
			for(CustomerInfo info : delList)
			{
				mCustomerList.remove(info);
			}

			//Add PresoldH file Information
			for(CustomerInfo info : mCustomerList)
			{

				info.presoldOrHInfo = dbAdapter.getPresoldOrHInfo(info.nChainNo, info.nCustNo, info.nPeriodNo, info.nSeqNo);
				info.latestOrderInfo = dbAdapter.getCustomerLatestOrderList(info.nCustNo, info.nChainNo);
				String[] strCustInfos = Utils.getStringSetting(mContext, Global.PREF_CUSTINFO_MSG).split(",");
				info.strCustMsg= "";
				List<DocTextInfo> allDocTextInfoList = new ArrayList<>();

				for(int i = 0; i < strCustInfos.length; i++)
				{
					int nTextType = Utils.parseIntFromString(strCustInfos[i].trim());
					List<DocTextInfo> docTextInfoList = dbAdapter.getDocTextList(info.nCustNo, nTextType);
					allDocTextInfoList.addAll(docTextInfoList);
				}

				Collections.sort(allDocTextInfoList, (docTextInfo, t1) -> {
					if(docTextInfo.nTextType == t1.nTextType)
					{
						return docTextInfo.nLine - t1.nLine;
					}
					else
					{
						return docTextInfo.nTextType - t1.nTextType;
					}
				});

				for(DocTextInfo docTextInfo : allDocTextInfoList)
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
			dbAdapter.close();

			if (mCustomerList.size() > 0) {
				if (mCustomerList.get(0).nOrderNo == -1) {
					Collections.sort(mCustomerList, CustomerInfo.presold_seqNoComparator);
					Collections.sort(mCustomerList, CustomerInfo.presold_periodNoComparator);
					Collections.sort(mCustomerList, CustomerInfo.presold_deliveryDateComparator);
					Collections.sort(mCustomerList, CustomerInfo.deliveryDateComparator);
					Collections.sort(mCustomerList, CustomerInfo.seqNoComparator);
					Collections.sort(mCustomerList, CustomerInfo.periodNoComparator);
					Collections.sort(mCustomerList, new Comparator<CustomerInfo>() {
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
					for (int i=0; i<mCustomerList.size(); i++) {
						mCustomerList.get(i).nOrderNo = i;
					}
				} else {
					Collections.sort(mCustomerList, CustomerInfo.orderComparator);
				}
			}

			refreshPage();
			refreshLanguage();
		}
		Global.gCurrentCustomerList = null;
		
	}

	@Override
	public void refreshPage() {
		//Check Day Count
		if(mDayNoList.size() == 1)
		{
			mRootView.findViewById(R.id.layoutDayNo).setVisibility(View.INVISIBLE);
		}
		else
		{
			mRootView.findViewById(R.id.layoutDayNo).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.txtDay).setOnClickListener(this);
			((TextView)mRootView.findViewById(R.id.txtDay)).setText(Utils.getWeekDateName(nSelDayNo));
		}


		if (mCustomerList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mDetailListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mTxtDetailNoData.setVisibility(View.GONE);
			mListAdapter.notifyDataSetChanged();
			mDetailListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mDetailListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
			mTxtDetailNoData.setVisibility(View.VISIBLE);
		}
		boolean bShowKey = false;
		for(CustomerInfo customerInfo : mCustomerList)
		{
			if(!TextUtils.isEmpty(customerInfo.strDexVersion))
			{
				bShowKey = true;
				break;
			}
		}
		if( !bShowKey )
		{
			mRootView.findViewById(R.id.btnKey).setVisibility(View.INVISIBLE);
			mRootView.findViewById(R.id.btnKey).setOnClickListener(null);
		}
		else
		{
			mRootView.findViewById(R.id.btnKey).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.btnKey).setOnClickListener(this);
		}

		String apiToken = Utils.getStringSetting(mContext, Global.PREF_ROUTIFICAPI);
		if( TextUtils.isEmpty(apiToken) )
		{
			mRootView.findViewById(R.id.btnRoutific).setVisibility(View.INVISIBLE);
			mRootView.findViewById(R.id.btnRoutific).setOnClickListener(null);
		}
		else
		{
			mRootView.findViewById(R.id.btnRoutific).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.btnRoutific).setOnClickListener(this);
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.SalesPlan[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " # " + Global.gTripNumber);
		((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Completed[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		mTxtDetailNoData.setText(Str.No_data_has_been_found[Global.LANG]);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	private void gotoCustomerDetail(CustomerInfo info) {
		Global.gCurCustomerInfo = info;
		Fragment fragment = new CustomerDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(Global.EXTRA_CUSTOMER_INFO, info);
		fragment.setArguments(bundle);
		mContext.replaceFragment(fragment);
	}

	private void saveData()
	{
		if( mCustomerList.size() > 0 )
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();

			URouteSchHeader header = new URouteSchHeader();
			header.nTrxnNo = System.currentTimeMillis();
			SimpleDateFormat sdf= new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT);
			header.strTrxnDate = sdf.format(new Date(header.nTrxnNo));
			sdf = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT);
			header.strTrxnTime = sdf.format(new Date(header.nTrxnNo));
			dbAdapter.insertURouteSchHeader(header);
			for (int i=0; i<mCustomerList.size(); i++) {
				CustomerInfo info = mCustomerList.get(i);
				info.nOrderNo = i;
				dbAdapter.updateCustomerInfo(info);
				URouteSchBody body = new URouteSchBody();
				body.nTrxnNo = header.nTrxnNo;
				body.nChainNo = info.nChainNo;
				body.nCustNo = info.nCustNo;
				body.nOrgSeqNo = info.nOrgSeqNo;
				body.nChangeSeqNo = i;

				body.strETADate = strETADate;
				if(strETATime != null && !strETATime.equals(""))
				{
					sdf = new SimpleDateFormat("kk:mm");
					try {
						Date date = sdf.parse(info.strArrivalTime);
						SimpleDateFormat sdf_time = new SimpleDateFormat("kkmmss");
						body.strETATime = sdf_time.format(date);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				else
				{
					body.strETATime = "";
				}

				dbAdapter.insertURouteSchBody(body);
			}
			dbAdapter.close();
			AddTrxnNo(0, 0, header.nTrxnNo);
			//Global.gUploadCustNoList.add(makeZipFileInfo);
			Utils.registerUploadFile(mContext, makeZipFileInfo);

		}
	}
	
	private void doComplete() {
		Utils.setIntSetting(mContext, Global.DAY_NO, nSelDayNo);
		saveData();
		mContext.onBackPressed();
	}

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnBack:
				mContext.onBackPressed();
				break;
			case R.id.btnMap:
				Fragment fragment = new CustomerMapviewFragment();
				Bundle bundle = new Bundle();
				bundle.putBoolean(Global.EXTRA_IS_ALL_CUSTOMER, true);	//Modified by needman.	true->false
				Global.gCurrentCustomerList = mCustomerList;
				fragment.setArguments(bundle);
				mContext.replaceFragment(fragment);
				break;
			case R.id.btnCompleted:
				doComplete();
				break;
			case R.id.btnKey:
				try {
					KeyInformationDialog dialog = new KeyInformationDialog(mContext);
					dialog.setCustomerList(mCustomerList);
					dialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}

				break;
			case R.id.btnRoutific:
				try {
					OptimizationDialog optimizationDialog = new OptimizationDialog(mContext, new OptimizationDialog.Callback() {
					@Override
					public void onSelect(LocationInfo startLocation, LocationInfo endLocation) {
						doRoutificClicked(startLocation, endLocation);
					}
				});
				optimizationDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}

				break;
			case R.id.txtDay:
				doClickDay();
				break;
		}
		m_bClicked = false;
	}

	private void doClickDay()
	{
		final String strArray[] = new String[mDayNoList.size()];
		for(int i = 0; i < mDayNoList.size(); i++)
		{
			strArray[i] = Utils.getWeekDateName(mDayNoList.get(i));
			if( nSelDayNo == mDayNoList.get(i))
			{
				nSelDateIdx = i;
			}
		}
		final int origIdx = nSelDateIdx;

		try {
			new AlertDialog.Builder(mContext)
				.setTitle(Str.SelectDayToView[Global.LANG])
				.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), nSelDateIdx, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int nSelected)
					{
						nSelDateIdx = nSelected;
						((TextView) mRootView.findViewById(R.id.txtDay)).setText(strArray[nSelected]);
					}
				})
				.setPositiveButton(Str.Ok[Global.LANG], new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int nSelected)
					{
						nSelDayNo = mDayNoList.get(nSelDateIdx);
						loadData();
					}
				})
				.setNegativeButton(Str.Cancel[Global.LANG], new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int nSelected)
					{
						((TextView) mRootView.findViewById(R.id.txtDay)).setText(strArray[origIdx]);
					}
				})
				.create()
				.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void doRoutificClicked(final LocationInfo startLocation, final LocationInfo endLocation)
	{
		//Check the Location is set on or not.
		if(ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			Toast.makeText(mContext, Str.LocationPermissionDenied[Global.LANG], Toast.LENGTH_LONG).show();
			return;
		}

		showProgressDialog("Getting current location...");

		LocationServices.getFusedLocationProviderClient(mContext).getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
			@Override
			public void onComplete(@NonNull Task<Location> task) {
				if (task.isSuccessful() && task.getResult() != null) {
					hideProgressDialog();
					Location location = task.getResult();
					doProcessRoutfic(startLocation, endLocation, location);
				} else {
					hideProgressDialog();
					Toast.makeText(mContext, Str.FailedToGetCurrentLocation[Global.LANG], Toast.LENGTH_LONG).show();
				}
			}
		});

	}

	private void doProcessRoutfic(final LocationInfo startLocation, final LocationInfo endLocation, Location curLocation)
	{
		Utils.setDoubleSetting(mContext, Global.PREF_MY_LATITUDE, curLocation.getLatitude());
		Utils.setDoubleSetting(mContext, Global.PREF_MY_LONGITUDE, curLocation.getLongitude());
		showProgressDialog(Str.Optimizing[Global.LANG]);
		new Thread()
		{
			@Override
			public void run() {
				Map<String, String> headers = new HashMap<>();
				headers.put("Authorization", "bearer " + Utils.getStringSetting(mContext, Global.PREF_ROUTIFICAPI));

				final JSONObject request = new JSONObject();
				JSONObject fleet = new JSONObject();
				//Add Fleet
				try
				{
					JSONObject vehicle = new JSONObject();

					SimpleDateFormat sdf1 = new SimpleDateFormat("kk:mm");
					Calendar c = Calendar.getInstance();
					c.setTime(new Date());
					c.add(Calendar.MINUTE, 2);
					Date dt = c.getTime();

					vehicle.put("shift_start", sdf1.format(dt));

					JSONObject end_location = new JSONObject();
					end_location.put("id", endLocation.locationName);
					end_location.put("lat",  endLocation.latitude);
					end_location.put("lng", endLocation.longitude);
					vehicle.put("end_location", end_location);

					JSONObject start_location = new JSONObject();
					if(startLocation.locationName.equals(Str.CurrentLocation[Global.LANG]))
					{
						start_location.put("id", Global.gTripNumber);
					}
					else
					{
						start_location.put("id", startLocation.locationName);
					}
					start_location.put("lat", startLocation.latitude);
					start_location.put("lng", startLocation.longitude);

					vehicle.put("start_location", start_location);

					vehicle.put("strict_start", true);

					fleet.put("vehicle_1", vehicle);
					request.put("fleet", fleet);

					//Commented by asking from Andrew... at 11th of July, 2022
//					JSONObject options = new JSONObject();
//					options.put("traffic", "predicted");
//					request.put("options", options);

					dt = new Date();

					/*c = Calendar.getInstance();
					c.setTime(dt);
					c.add(Calendar.DATE, 1);
					dt = c.getTime();*/

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					String today = sdf.format(dt);
					request.put("date", today);

				}catch (Exception e)
				{
					e.printStackTrace();
				}
				JSONObject visits = new JSONObject();
				//Set Visit Parameters
				int orderNo = 0;
				for(CustomerInfo customer : mCustomerList)
				{
					boolean bAddress = false;
					String strAddress = "";
					if( customer.latitude == 0 && customer.longitude == 0
							&& customer.driverLatitude == 0 && customer.driverLongitude == 0 )
					{
						/*strAddress = customer.strAddress1;
						if(!TextUtils.isEmpty(customer.strAddress2))
						{
							if(!TextUtils.isEmpty(strAddress))
							{
								strAddress += ", " + customer.strAddress2;
							}
							else
							{
								strAddress = customer.strAddress2;
							}
						}
						strAddress = Utils.getFullAddress(mContext, strAddress);*/
						strAddress = customer.strAddress1 + ", " + customer.strCity + " " + customer.strShipToState + " " + customer.strShipToZip + ", Australia";
						if(strAddress != null)
						{
							bAddress = true;
						}
						else
						{
							continue;
						}
					}

					orderNo++;
					JSONObject order = new JSONObject();
					JSONObject location = new JSONObject();
					try
					{
						location.put("name", customer.nChainNo + "/" + customer.nCustNo);
						if(bAddress)
						{
							location.put("address", strAddress);
						}
						else
						{
							if(customer.driverLatitude == 0 || customer.driverLongitude == 0)
							{
								location.put("lat", customer.latitude);
								location.put("lng", customer.longitude);
							}
							else
							{
								location.put("lat", customer.driverLatitude);
								location.put("lng", customer.driverLongitude);
							}
						}


						order.put("location", location);
					}catch (Exception e)
					{
						e.printStackTrace();
					}
					try
					{
						JSONArray time_windows = new JSONArray();
						if((!customer.strStartTime1.equals("0000") && !TextUtils.isEmpty(customer.strStartTime1)) && (!customer.strEndTime1.equals("0000") && !TextUtils.isEmpty(customer.strEndTime1)))
						{
							JSONObject time = new JSONObject();
							if(!customer.strStartTime1.contains(":"))
							{
								time.put("start", customer.strStartTime1.substring(0, 2) + ":" + customer.strStartTime1.substring(2, 4));
							}
							else
							{
								time.put("start", customer.strStartTime1);
							}
							if(!customer.strEndTime1.contains(":"))
							{
								time.put("end", customer.strEndTime1.substring(0, 2) + ":" + customer.strEndTime1.substring(2, 4));
							}
							else
							{
								time.put("end", customer.strEndTime1);
							}
							time_windows.put(time);
						}
						if((!customer.strStartTime2.equals("0000") && !TextUtils.isEmpty(customer.strStartTime2)) && (!customer.strEndTime2.equals("0000") && !TextUtils.isEmpty(customer.strEndTime2)))
						{
							JSONObject time = new JSONObject();
							if(!customer.strStartTime2.contains(":"))
							{
								time.put("start", customer.strStartTime2.substring(0, 2) + ":" + customer.strStartTime2.substring(2, 4));
							}
							else
							{
								time.put("start", customer.strStartTime2);
							}
							if(!customer.strEndTime2.contains(":"))
							{
								time.put("end", customer.strEndTime2.substring(0, 2) + ":" + customer.strEndTime2.substring(2, 4));
							}
							else
							{
								time.put("end", customer.strEndTime2);
							}
							time_windows.put(time);
						}
						if( time_windows.length() != 0)
						{
							order.put("time_windows", time_windows);
						}
					}catch (Exception e)
					{
						e.printStackTrace();
					}

					try
					{
						order.put("duration", Utils.getIntSetting(mContext, Global.PREF_VISITDURATION));
					}catch (Exception e)
					{
						e.printStackTrace();
					}

					try {
						visits.put("order_" + orderNo, order);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				try
				{
					request.put("visits", visits);
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				res = null;

				try {
					if(Utils.isConnectingToInternet(mContext))
					{
						final CountDownLatch latch = new CountDownLatch(1);
						RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), request.toString().getBytes());
						//Utils.writeAsJson(request.toString(), true);
						if(Utils.getStringSetting(mContext, Global.PREF_ROUTEFIC_LOG).equals("1"))
						{
							RoutificLogs.getInstance().WriteLogs("\napi url : https://api.routific.com/v1/vrp-long\n" + "method : " + "POST\n" + "Authorization : " + "bearer " + Utils.getStringSetting(mContext, Global.PREF_ROUTIFICAPI) +
									"\nContent-Type : application/json\n\n" + "Body : \n" + request.toString());
						}
						RoutificApiClient.getApiClient().vrp( "bearer " + Utils.getStringSetting(mContext, Global.PREF_ROUTIFICAPI), requestBody).enqueue(new retrofit.Callback<RoutificJob>() {
							@Override
							public void onResponse(retrofit.Response<RoutificJob> response, Retrofit retrofit) {
								if(Utils.getStringSetting(mContext, Global.PREF_ROUTEFIC_LOG).equals("1"))
								{
									Gson gson = new Gson();
									RoutificLogs.getInstance().WriteLogs("\napi response:\n" + gson.toJson(response.body()));
								}
								res = response;
								latch.countDown();
							}

							@Override
							public void onFailure(Throwable t) {
								latch.countDown();

							}
						});
						latch.await();
					}

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if( res == null )
				{
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							hideProgressDialog();
							if(!mContext.isFinishing() && !mContext.isDestroyed())
							{
								try {
									Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
								}catch (Exception e1)
								{
									e1.printStackTrace();
								}
							}

						}
					});
					return;
				}

				if( res != null && !res.isSuccess() || res.body() == null)
				{
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							hideProgressDialog();
							if( res.isSuccess() )
							{
								if(!mContext.isFinishing() && !mContext.isDestroyed())
								{
									try {
										Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
									}catch (Exception e1)
									{
										e1.printStackTrace();
									}
								}

							}
							else
							{
								try {
									InputStream is = res.errorBody().byteStream();
									byte[] buffer = new byte[is.available()];
									is.read(buffer);
									String jsonResult = new String(buffer);
									JSONObject jsonObject = new JSONObject(jsonResult);
									String message = jsonObject.getString("error");
									if(!mContext.isFinishing() && !mContext.isDestroyed())
									{
										try {
											Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
										}catch (Exception e1)
										{
											e1.printStackTrace();
										}
									}

								} catch (Exception e) {
									e.printStackTrace();
									if(!mContext.isFinishing() && !mContext.isDestroyed())
									{
										try {
											Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
										}catch (Exception e1)
										{
											e1.printStackTrace();
										}
									}

								}
							}

						}
					});
					return;
				}
				try
				{
					if(res == null){
						return;
					}
					bRes = false;
					RoutificJob job = res.body();
					while(bRes == false)
					{
						final CountDownLatch latch = new CountDownLatch(1);
						if(Utils.getStringSetting(mContext, Global.PREF_ROUTEFIC_LOG).equals("1"))
						{
							RoutificLogs.getInstance().WriteLogs("\napi url : https://api.routific.com/jobs/" + job.job_id + "\nmethod : GET");
						}
						RoutificApiClient.getApiClient().getJobStatus(job.job_id).enqueue(new retrofit.Callback<RoutificResponse>() {
							@Override
							public void onResponse(final retrofit.Response<RoutificResponse> response, Retrofit retrofit) {
								if(Utils.getStringSetting(mContext, Global.PREF_ROUTEFIC_LOG).equals("1"))
								{
									Gson gson = new Gson();
									RoutificLogs.getInstance().WriteLogs("\napi response : \n" + gson.toJson(response.body()));
								}
								if( response.isSuccess() && response.body() != null)
								{
									if(!response.body().status.equals("finished") )
									{
										bRes = false;
									}
									else
									{
										bRes = true;
										SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss.SSS'Z'");
										try {
											Date date = sdf.parse(response.body().createdAt);
											SimpleDateFormat sdf_date = new SimpleDateFormat("yyyyMMdd");
											SimpleDateFormat sdf_time = new SimpleDateFormat("kkmmss");
											strETADate = sdf_date.format(date);
											strETATime = sdf_time.format(date);
										} catch (ParseException e) {
											e.printStackTrace();
										}

										new Handler(Looper.getMainLooper()).post(() -> {
											hideProgressDialog();
											RoutificResponse routificResponse = response.body();
											if( routificResponse.output == null )
											{
												if(!mContext.isFinishing() && !mContext.isDestroyed())
												{
													try {
														Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
													}catch (Exception e1)
													{
														e1.printStackTrace();
													}
												}

											}
											else
											{
												if( !TextUtils.isEmpty(routificResponse.output.status) && routificResponse.output.status.equals("success"))
												{
													if( routificResponse.output.solution != null )
													{
														if( routificResponse.output.solution.vehicle_1 != null && routificResponse.output.solution.vehicle_1.size() > 0 )
														{
															optimizeSequence(routificResponse.output.solution.vehicle_1);
														}
														else
														{
															if(!mContext.isFinishing() && !mContext.isDestroyed())
															{
																try {
																	Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
																}catch (Exception e1)
																{
																	e1.printStackTrace();
																}
															}

														}
													}
													else
													{
														if(!mContext.isFinishing() && !mContext.isDestroyed())
														{
															try {
																Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
															}catch (Exception e1)
															{
																e1.printStackTrace();
															}
														}

													}
												}
												else
												{
													if(!mContext.isFinishing() && !mContext.isDestroyed())
													{
														try {
															Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
														}catch (Exception e1)
														{
															e1.printStackTrace();
														}
													}

												}

											}
										});
									}
								}
								else
								{
									bRes = true;
									new Handler(Looper.getMainLooper()).post(new Runnable() {
										@Override
										public void run() {
											hideProgressDialog();
											if( response.isSuccess() )
											{
												if(!mContext.isFinishing() && !mContext.isDestroyed())
												{
													try {
														Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
													}catch (Exception e1)
													{
														e1.printStackTrace();
													}
												}

											}
											else
											{
												try {
													InputStream is = response.errorBody().byteStream();
													byte[] buffer = new byte[is.available()];
													is.read(buffer);
													String jsonResult = new String(buffer);
													JSONObject jsonObject = new JSONObject(jsonResult);
													String message = jsonObject.getString("error");
													if(!mContext.isFinishing() && !mContext.isDestroyed())
													{
														try {
															Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
														}catch (Exception e1)
														{
															e1.printStackTrace();
														}
													}

												} catch (Exception e) {
													e.printStackTrace();
													if(!mContext.isFinishing() && !mContext.isDestroyed())
													{
														try {
															Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
														}catch (Exception e1)
														{
															e1.printStackTrace();
														}
													}

												}
											}

										}
									});
								}
								latch.countDown();
							}

							@Override
							public void onFailure(Throwable t) {
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										hideProgressDialog();
										if(!mContext.isFinishing() && !mContext.isDestroyed())
										{
											try {
												Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
											}catch (Exception e1)
											{
												e1.printStackTrace();
											}
										}

									}
								});
								bRes = true;
								latch.countDown();
							}
						});
						latch.await();
					}

					/*RoutificApiClient.getApiClient().getJobStatusAsText(job.job_id).enqueue(new Callback<ResponseBody>() {
						@Override
						public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
							try {
								Utils.writeAsJson(response.body().string(), false);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onFailure(Throwable t) {

						}
					});*/

				}catch (Exception e)
				{
					e.printStackTrace();
				}


			}
		}.start();
	}

	private void optimizeSequence(List<RoutificLocation> locationList)
	{
		final Map<String, RoutificLocation> routificLocationMap = new HashMap<>();

		for(RoutificLocation location : locationList)
		{
			routificLocationMap.put(location.location_name, location);
		}
		for(CustomerInfo customerInfo : mCustomerList)
		{
			String key = customerInfo.nChainNo + "/" + customerInfo.nCustNo;
			RoutificLocation location = routificLocationMap.get(key);
			if( location != null )
			{
				customerInfo.strArrivalTime = location.arrival_time;
			}
			else if( customerInfo.latitude == 0 && customerInfo.longitude == 0
					&& customerInfo.driverLongitude == 0 && customerInfo.driverLatitude == 0)
			{
				customerInfo.strArrivalTime = "NO GPS";
			}
			else
			{
				customerInfo.strArrivalTime = "UNSCH";
			}
		}

		Collections.sort(mCustomerList, new Comparator<CustomerInfo>() {
			@Override
			public int compare(CustomerInfo t1, CustomerInfo t2) {
				int type1, type2 ;
				if(t1.strArrivalTime.equals("NO GPS") || t1.strArrivalTime.equals("UNSCH"))
				{
					type1 = 1;
				}
				else
				{
					type1 = 2;
				}

				if(t2.strArrivalTime.equals("NO GPS") || t2.strArrivalTime.equals("UNSCH"))
				{
					type2 = 1;
				}
				else
				{
					type2 = 2;
				}

				if(type1 == type2)
				{
					return t1.strArrivalTime.compareTo(t2.strArrivalTime);
				}
				else
				{
					return type1 - type2;
				}
			}
		});

		mDetailListAdapter.notifyDataSetChanged();
		mListAdapter.notifyDataSetChanged();
		long nTrxnNo = setGeneralUploadData("ROPT", 0, 0, -1);

		TransactionInfo transactionInfo = new TransactionInfo();
		transactionInfo.strType = "ROPT";
		transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertTransactionHistoryInfo(transactionInfo);
		dbAdapter.close();

		saveData();
		if(!mContext.isFinishing() && !mContext.isDestroyed())
		{
			try {
				Toast.makeText(mContext, Str.SuccessfullyOptimized[Global.LANG], Toast.LENGTH_LONG).show();
			}catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}


	}

	public void showProgressDialog(String message)
	{
		try {
			if( mProgressDlg == null )
			{
				mProgressDlg = new ProgressDialog(mContext);
			}
			mProgressDlg.show();
			mProgressDlg.setMessage(message);
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void hideProgressDialog()
	{
		try {
			if( mProgressDlg != null )
			{
				mProgressDlg.dismiss();
				mProgressDlg = null;
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}
	private class DetailListAdapter extends BaseAdapter implements Swappable {

		@Override
		public int getCount() {
			return mCustomerList.size();
		}

		@Override
		public Object getItem(int position) {
			if(position < mCustomerList.size())
			{
				return mCustomerList.get(position);
			}
			else
			{
				return null;
			}
		}

		@Override
		public long getItemId(int position) {
			return mCustomerList.get(position).nOrderNo;
		}

		@Override
		public View getView(final int position, View view, ViewGroup viewGroup) {
			if(view == null)
			{
				view = mContext.getLayoutInflater().inflate(R.layout.row_customer_detail_view, viewGroup, false);
			}

			CustomerInfo info = (CustomerInfo)getItem(position);
			if(info == null) return view;

			((TextView)view.findViewById(R.id.customer_name)).setText(info.strName);
			/*PresoldOrHInfo presoldOrHInfo = null;
			if( info.presoldOrHInfo != null && info.presoldOrHInfo.size() > 0)
			{
				presoldOrHInfo = info.presoldOrHInfo.get(0);
			}*/
			int nColor = 0;
			if(info.latestOrderInfo != null && info.latestOrderInfo.size() > 0)
			{
				nColor = 3;
			}
			else
			{
				nColor = Utils.getCustomerColorWithPresoldH(info.presoldOrHInfo);
			}

			if(info.bCompleted)
			{
				((TextView)view.findViewById(R.id.customer_name)).setTextColor(mContext.getResources().getColor(R.color.green_color));
			}
			else if(info.nCreditHold > 0)
			{
				((TextView)view.findViewById(R.id.customer_name)).setTextColor(mContext.getResources().getColor(R.color.red_color));
			}
			else if(nColor == 3)
			{
				((TextView)view.findViewById(R.id.customer_name)).setTextColor(mContext.getResources().getColor(R.color.blue_color));
			}
			else if(nColor == 2)
			{
				((TextView)view.findViewById(R.id.customer_name)).setTextColor(mContext.getResources().getColor(R.color.purple_text_color));
			}
			else if(nColor == 1)
			{
				((TextView)view.findViewById(R.id.customer_name)).setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
			}
			else if(info.strDeliveryType.equals("5"))
			{
				((TextView)view.findViewById(R.id.customer_name)).setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
			}
			else
			{
				((TextView)view.findViewById(R.id.customer_name)).setTextColor(mContext.getResources().getColor(R.color.black_color));
			}

			String strContent = "";
			if((!TextUtils.isEmpty(info.strStartTime1) && !TextUtils.isEmpty(info.strEndTime1) && !info.strStartTime1.equals("0000") && !info.strEndTime1.equals("0000"))
					|| (!TextUtils.isEmpty(info.strStartTime2) && !TextUtils.isEmpty(info.strEndTime2) && !info.strStartTime2.equals("0000") && !info.strEndTime2.equals("0000")))
			{
				strContent += "Delivery ";
				boolean bTime1Exist = false;
				if(!info.strStartTime1.equals("0000") && !info.strEndTime1.equals("0000"))
				{
					SimpleDateFormat sdf1 = new SimpleDateFormat("kk:mm");
					SimpleDateFormat sdf2 = new SimpleDateFormat("kkmm");
					SimpleDateFormat sdf_target = new SimpleDateFormat("h:mm a");

					String strStartTime = "";
					if(info.strStartTime1.contains(":"))
					{
						try
						{
							Date date = sdf1.parse(info.strStartTime1);
							strStartTime = sdf_target.format(date);
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						try
						{
							Date date = sdf2.parse(info.strStartTime1);
							strStartTime = sdf_target.format(date);
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}

					String strEndTime = "";
					if(info.strEndTime1.contains(":"))
					{
						try
						{
							Date date = sdf1.parse(info.strEndTime1);
							strEndTime = sdf_target.format(date);
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						try
						{
							Date date = sdf2.parse(info.strEndTime1);
							strEndTime = sdf_target.format(date);
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					if(!TextUtils.isEmpty(strStartTime) && !TextUtils.isEmpty(strEndTime))
					{
						strContent += strStartTime + " to " + strEndTime;
						bTime1Exist = true;
					}
				}
				if(!info.strStartTime2.equals("0000") || !info.strEndTime2.equals("0000"))
				{
					SimpleDateFormat sdf1 = new SimpleDateFormat("kk:mm");
					SimpleDateFormat sdf2 = new SimpleDateFormat("kkmm");
					SimpleDateFormat sdf_target = new SimpleDateFormat("h:mm a");

					String strStartTime = "";
					if(info.strStartTime2.contains(":"))
					{
						try
						{
							Date date = sdf1.parse(info.strStartTime2);
							strStartTime = sdf_target.format(date);
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						try
						{
							Date date = sdf2.parse(info.strStartTime2);
							strStartTime = sdf_target.format(date);
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}

					String strEndTime = "";
					if(info.strEndTime2.contains(":"))
					{
						try
						{
							Date date = sdf1.parse(info.strEndTime2);
							strEndTime = sdf_target.format(date);
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						try
						{
							Date date = sdf2.parse(info.strEndTime2);
							strEndTime = sdf_target.format(date);
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					if(!TextUtils.isEmpty(strStartTime) && !TextUtils.isEmpty(strEndTime))
					{
						if(bTime1Exist)
						{
							strContent += " or " +strStartTime + " to " + strEndTime;
						}
					}
				}
			}
			if(info.presoldOrHInfo != null && info.presoldOrHInfo.size() > 0)
			{
				for(int i = 0; i < info.presoldOrHInfo.size(); i++)
				{
					String strUser1 = info.presoldOrHInfo.get(i).strUser1;

					if(!TextUtils.isEmpty(strContent))
					{
						strContent += "\n";
					}
					strContent += "Order " + info.presoldOrHInfo.get(i).strOrderNo;
					if(strUser1.trim().length() > 0 && !strUser1.trim().equals("0"))
						if(strUser1.trim().length() > 0 && !strUser1.trim().equals("0"))
						{
							strContent += " for " + info.presoldOrHInfo.get(i).strUser1;
							if(!TextUtils.isEmpty(mCartonDescription))
							{
								int nValue = Utils.parseIntFromString(info.presoldOrHInfo.get(i).strUser1);
								if(nValue > 1)
								{
									strContent += " " + mCartonDescription + "s";
								}
								else
								{
									strContent += " " + mCartonDescription;
								}
							}
						}
					if(info.presoldOrHInfo.get(i).strInstrs.trim().length() > 0)
					{
						strContent += "\n" + info.presoldOrHInfo.get(i).strInstrs;
					}
				}
			}

			if(!TextUtils.isEmpty(info.strCustMsg))
			{
				strContent += "\n" + info.strCustMsg;
			}
			((TextView)view.findViewById(R.id.customer_detail)).setText(strContent);
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					gotoCustomerDetail(mCustomerList.get(position));
				}
			});
			view.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					Global.gCurCustomerInfo = mCustomerList.get(position);
					showNonDeliveryDialog();
					return true;
				}
			});
			return view;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public void swapItems(int positionOne, int positionTwo) {
			Collections.swap(mCustomerList, positionOne, positionTwo);
			mListAdapter.notifyDataSetChanged();
		}
	}

	private class ListAdapter extends BaseAdapter implements Swappable {

		private class ViewHolder {
			public TextView txtStartTime1, txtCustomerName;
			public ImageView imgMessage;
		}

		public int getCount() {
			return mCustomerList.size();
		}

		public CustomerInfo getItem(int position) {
			if(position >= mCustomerList.size()) return null;
			return mCustomerList.get(position);
		}

		public long getItemId(int position) {
			if(position >= mCustomerList.size()) return -1;
			return mCustomerList.get(position).nOrderNo;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ViewHolder holder;
			final CustomerInfo info = getItem(position);
			boolean bDiff = false;
			int nDefPeriodNo = Utils.getIntSetting(mContext, Global.PREF_LOADPERDDEF);
			if( info.presoldOrHInfo != null && info.presoldOrHInfo.size() > 0 )
			{
				for(PresoldOrHInfo presoldOrHInfo : info.presoldOrHInfo)
				{
					if( presoldOrHInfo.nPeriodNo != nDefPeriodNo)
					{
						bDiff = true;
						break;
					}
				}
			}
			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_delivery_plan, parent, false);
				holder = new ViewHolder();
				holder.txtStartTime1 = (TextView) view.findViewById(R.id.txtStartTime1);
				holder.txtCustomerName = (TextView) view.findViewById(R.id.txtCustomerName);
				holder.imgMessage = (ImageView) view.findViewById(R.id.imgMessage);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}
			if(bDiff)
			{
				holder.txtStartTime1.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
			}
			else
			{
				holder.txtStartTime1.setTextColor(mContext.getResources().getColor(R.color.black_color));
			}
			holder.txtStartTime1.setText(info.strArrivalTime);
			holder.txtCustomerName.setText(info.strName);
			if (info.nMessageCnt > 0) {
				holder.imgMessage.setVisibility(View.VISIBLE);
			} else {
				holder.imgMessage.setVisibility(View.GONE);
			}

			holder.imgMessage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Bundle bundle = new Bundle();
					bundle.putInt(Global.EXTRA_CUSTOMER_NO, info.nCustNo);
					bundle.putString(Global.EXTRA_CUSTOMER_NAME, info.strName);
					Fragment fragment = new MessageBoardFragment();
					fragment.setArguments(bundle);
					mContext.replaceFragment(fragment);
				}
			});

			int nColor = 0;
			if(info.latestOrderInfo != null && info.latestOrderInfo.size() > 0)
			{
				nColor = 3;
			}
			else
			{
				nColor = Utils.getCustomerColorWithPresoldH(info.presoldOrHInfo);
			}
			if(info.bCompleted)
			{
				holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.green_color));
			}
			else if(info.nCreditHold > 0)
			{
				holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.red_color));
			}
			else if(nColor == 3)
			{
				holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.blue_color));
			}
			else if(nColor == 2)
			{
				holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.purple_text_color));
			}
			else if(nColor == 1)
			{
				holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
			}
			else if(info.strDeliveryType.equals("5"))
			{
				holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
			}
			else
			{
				holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.black_color));
			}

			return view;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public void swapItems(int positionOne, int positionTwo) {
			Collections.swap(mCustomerList, positionOne, positionTwo);
			mDetailListAdapter.notifyDataSetChanged();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			strETATime = "";
			strETADate = sdf.format(new Date());
		}
	}
}