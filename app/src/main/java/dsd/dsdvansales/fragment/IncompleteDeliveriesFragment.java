package dsd.dsdvansales.fragment;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.*;
import java.text.*;
import java.util.*;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.PresoldOrDInfo;
import dsd.dsdvansales.model.PresoldOrHInfo;
import dsd.dsdvansales.model.ProdLocnInfo;
import dsd.dsdvansales.model.UServicesInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.model.UVisitInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class IncompleteDeliveriesFragment extends BaseFragment implements View.OnClickListener {

	private class IncompleteInfo {
		public CustomerInfo customerInfo = new CustomerInfo();
		public int nReasonIdx = -1;
		public int nCartons = 0;
		public int nUnits = 0;
	}

	private Font fontDarkGray7Normal;
	private Font fontBlack7Bold;
	private Font fontBlack10Bold;

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<IncompleteInfo> mIncompleteList = new ArrayList<>();
	private List<IncompleteInfo> mIncompletedList = new ArrayList<>();
	private List<DescTypeInfo> mDescTypeList = new ArrayList<>();

	private String mStrPdfFileName = "";
	private long mPdfTrxnNo = 0;

	private boolean bApplyAll = false;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_incomplete_deliveries, container, false);
		commonInit();
		loadFont();
		return mRootView;
	}

	private void loadFont()
	{
		fontDarkGray7Normal = Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 7, Font.NORMAL, BaseColor.DARK_GRAY);
		fontBlack7Bold = Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 7, Font.BOLD, BaseColor.BLACK);
		fontBlack10Bold = Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 10, Font.BOLD, BaseColor.BLACK);
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_INCOMPLETE_DELIVERIES;
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnReturn).setOnClickListener(this);
		mRootView.findViewById(R.id.btnProceed).setOnClickListener(this);

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mListView = (ListView) mRootView.findViewById(R.id.listView);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);

		loadData();
	}
	
	

	@Override
	public void loadData() {
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		mDescTypeList = dbAdapter.getDescTypeList("NOSERVRSN");
		//List<CustomerInfo> customerList = dbAdapter.getCustomerList(true);
		List<CustomerInfo> customerList = filterSameCustomer(dbAdapter.getWhiteCustomerInfo(false, Utils.getIntSetting(mContext, Global.DAY_NO)));
		customerList.addAll(dbAdapter.getCustomerList(true, false, Utils.getIntSetting(mContext, Global.DAY_NO)));

		if (customerList.size() > 0) {
			if (customerList.get(0).nOrderNo == -1) {
				Collections.sort(customerList, CustomerInfo.seqNoComparator);
				Collections.sort(customerList, CustomerInfo.periodNoComparator);
				Collections.sort(customerList, CustomerInfo.deliveryDateComparator);
				for (int i=0; i<customerList.size(); i++) {
					customerList.get(i).nOrderNo = i;
				}
			} else {
				Collections.sort(customerList, CustomerInfo.orderComparator);
			}
		}
		for (CustomerInfo customerInfo : customerList) {
			if (customerInfo.bCompleted == true) {
				UServicesInfo uServicesInfo = dbAdapter.getUServiceInfo(customerInfo.nCustNo, customerInfo.nChainNo);
				if(uServicesInfo != null)
				{
					IncompleteInfo incompleteInfo = new IncompleteInfo();
					incompleteInfo.customerInfo = customerInfo;
					incompleteInfo.nReasonIdx = -1;
					for(int i = 0; i < mDescTypeList.size(); i++)
					{
						if(mDescTypeList.get(i).strAlphaKey.equals(uServicesInfo.strReason))
						{
							incompleteInfo.nReasonIdx = i;
							break;
						}
					}
					if(incompleteInfo.nReasonIdx != -1)
					{
						mIncompletedList.add(incompleteInfo);
					}
				}
				continue;
			}
			IncompleteInfo incompleteInfo = new IncompleteInfo();
			incompleteInfo.customerInfo = customerInfo;
			incompleteInfo.nReasonIdx = -1;
			incompleteInfo.nCartons = 0;
			incompleteInfo.nUnits = 0;
			List<PresoldOrHInfo> presoldOrHInfos = dbAdapter.getPresoldOrHInfo(customerInfo.nChainNo, customerInfo.nCustNo, customerInfo.nPeriodNo);
			for(PresoldOrHInfo presoldOrHInfo : presoldOrHInfos)
			{
				if (presoldOrHInfo != null) {
					List<PresoldOrDInfo> presoldOrDList = dbAdapter.getPresoldOrDList(presoldOrHInfo.strDetailFile);
					for (PresoldOrDInfo presoldOrDInfo : presoldOrDList) {
						ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(presoldOrDInfo.strItemNo);
						if (prodLocnInfo != null && prodLocnInfo.strFullCase != null && prodLocnInfo.strFullCase.equalsIgnoreCase("Y") == true) {
							incompleteInfo.nCartons += presoldOrDInfo.nOrderQty;
						} else {
							incompleteInfo.nUnits += presoldOrDInfo.nOrderQty;
						}
					}
				}
			}
			mIncompleteList.add(incompleteInfo);
		}
		dbAdapter.close();

		refreshLanguage();
	}

	@Override
	public void refreshPage() {
		if (mIncompleteList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
		boolean bEnabled = true;
		for (IncompleteInfo info : mIncompleteList) {
			if (info.nReasonIdx == -1) {
				bEnabled = false;
				break;
			}
		}
		mRootView.findViewById(R.id.btnProceed).setEnabled(bEnabled);
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.IncompleteDeliveries[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.lblDescription)).setText(Str.IncompleteDeliveries_desc[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnProceed)).setText(Str.Proceed[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		refreshPage();
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	private void showReasonCodeList(final int nPosition) {
		final String strArray[] = new String[mDescTypeList.size()];
		for (int i=0; i<mDescTypeList.size(); i++) {
			strArray[i] = mDescTypeList.get(i).strDescription;
		}
		final int nOrigIdx = mIncompleteList.get(nPosition).nReasonIdx;

		try {
			new AlertDialog.Builder(mContext)
					.setTitle(Str.Select_Reason_code[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), nOrigIdx, (dialog, nSelected) -> mIncompleteList.get(nPosition).nReasonIdx = nSelected)
					.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
						if(!bApplyAll)
						{
							if(mIncompleteList.size() > 1)
							{
								try {
									final MessageDialog msgDialog = new MessageDialog(mContext);
									msgDialog.setCancelable(false);
									msgDialog.mStrMessage = Str.ReasonCodeApplyAll[Global.LANG];
									msgDialog.mStrLeft = Str.No[Global.LANG];
									msgDialog.mStrRight = Str.Yes[Global.LANG];
									msgDialog.setOnDismissListener(dialog1 -> {
										if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
											bApplyAll = true;
											refreshPage();
										} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
											bApplyAll = true;
											for(IncompleteInfo reason : mIncompleteList)
											{
												reason.nReasonIdx = mIncompleteList.get(nPosition).nReasonIdx;
											}
											refreshPage();
										}
									});
									msgDialog.show();
								}catch (Exception e)
								{
									e.printStackTrace();
								}
								return;
							}
						}
						bApplyAll = true;
						refreshPage();
					})
					.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> mIncompleteList.get(nPosition).nReasonIdx = nOrigIdx)
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private PdfPTable createHeader() {
		PdfPTable table = new PdfPTable(3);
		PdfPCell cell;
		Phrase phrase;
		Date curDate = new Date();
		try {
			table.setWidths(new int[]{1, 1, 1});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			if (Utils.isFileExist(Global.DEFAULT_COMPANY_LOGO_FILEPATH) == true) {
				Image image = Image.getInstance(Global.DEFAULT_COMPANY_LOGO_FILEPATH);
				image.scaleToFit(120, 60);
				cell = new PdfPCell(image);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			} else {
				phrase = new Phrase("No Logo File", fontBlack10Bold);
				cell = new PdfPCell(phrase);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setVerticalAlignment(Element.ALIGN_CENTER);
			}
			cell.setRowspan(7);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			String strDocument = "";
			if (mStrPdfFileName.length() > 4) {
				strDocument = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
			}
			phrase = new Phrase("Trip Number: " + Global.gTripNumber, fontDarkGray7Normal);
			table.addCell(phrase);
			phrase = new Phrase("Document #: " + strDocument, fontDarkGray7Normal);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setRowspan(7);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			phrase = new Phrase("Date: " + new SimpleDateFormat(Utils.getDateFormat(mContext, Global.DATE_DD_MM_YY_FORMAT)).format(curDate), fontDarkGray7Normal);
			table.addCell(phrase);
			phrase = new Phrase("Time: " + new SimpleDateFormat(Global.TIME_HH_MM_FORMAT).format(curDate), fontDarkGray7Normal);
			table.addCell(phrase);
			phrase = new Phrase("User Name: " + Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION), fontDarkGray7Normal);
			table.addCell(phrase);
			phrase = new Phrase("Route #: " + Utils.getStringSetting(mContext, Global.PREF_ROUTE_NUMBER), fontDarkGray7Normal);
			table.addCell(phrase);
			phrase = new Phrase("Vehicle #: " + Utils.getStringSetting(mContext, Global.PREF_VEHICLE_NUMBER), fontDarkGray7Normal);
			table.addCell(phrase);
			phrase = new Phrase("Transaction #: " + mPdfTrxnNo, fontDarkGray7Normal);
			table.addCell(phrase);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	private PdfPTable createBody() {
		PdfPTable table = new PdfPTable(4);
		PdfPCell cell;
		Phrase phrase;
		try {
			table.setWidths(new int[]{2, 3, 4, 2});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			phrase = new Phrase("Undelivered Customers Report", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(4);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPadding(5);
			table.addCell(cell);

			phrase = new Phrase("Customer Number", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Customer Name", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Ship To Address", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Undelivered Reason", fontBlack7Bold);
			table.addCell(phrase);

			List<IncompleteInfo> printList = new ArrayList<>();
			printList.addAll(mIncompletedList);
			printList.addAll(mIncompleteList);
			for (IncompleteInfo info : printList) {
				phrase = new Phrase(String.valueOf(info.customerInfo.nCustNo), fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(info.customerInfo.strName, fontDarkGray7Normal);
				table.addCell(phrase);
				String strAddress = info.customerInfo.strAddress1;
				if (info.customerInfo.strAddress2.length() > 0) {
					if (strAddress.length() > 0) {
						strAddress += ", ";
					}
					strAddress += info.customerInfo.strAddress2;
				}
				if (info.customerInfo.strCity.length() > 0) {
					if (strAddress.length() > 0) {
						strAddress += ", ";
					}
					strAddress += info.customerInfo.strCity;
				}
				phrase = new Phrase(strAddress, fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(mDescTypeList.get(info.nReasonIdx).strDescription, fontDarkGray7Normal);
				table.addCell(phrase);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	private void createPDF() {
		try {
			Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
			mPdfTrxnNo = System.currentTimeMillis();
			mStrPdfFileName = getPDFFileName();
			Document document = new Document();
			PdfWriter.getInstance(document, new FileOutputStream(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName));
			document.open();
			PdfPTable table = createHeader();
			document.add(table);
			table = createBody();
			table.setSpacingBefore(20);
			document.add(table);
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doComplete() {
		String strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
		long nTrxnNo = System.currentTimeMillis();
		UTransactionsInfo uTransactionsInfo = setUTransactionsLastInfo(nTrxnNo, "FARC", mStrPdfFileName);
		setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, 0, 0, mPdfTrxnNo, strDocNo, "NONSERVICED", "Undelivered Customers Report", uTransactionsInfo.strTrxnDate, uTransactionsInfo.strTrxnTime, mStrPdfFileName);

		Date curDate = new Date();
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		nTrxnNo = setGeneralUploadData("VIS", 0, 0, -1, "No Sales Reasons");
		for (IncompleteInfo info : mIncompleteList) {
			//nTrxnNo = setGeneralUploadData("SERV", info.customerInfo.nChainNo, info.customerInfo.nCustNo, nTrxnNo++, mDescTypeList.get(info.nReasonIdx).strAlphaKey);
			UServicesInfo uServicesInfo = new UServicesInfo();
			uServicesInfo.nTrxnNo = ++nTrxnNo;
			uServicesInfo.nChainNo = info.customerInfo.nChainNo;
			uServicesInfo.nCustNo = info.customerInfo.nCustNo;
			uServicesInfo.nPeriod = 0;
			uServicesInfo.nSeqNo = info.customerInfo.nSeqNo;
			uServicesInfo.nDone = 0;
			uServicesInfo.strReason = mDescTypeList.get(info.nReasonIdx).strAlphaKey;
			uServicesInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, curDate));
			uServicesInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(curDate);
			uServicesInfo.bFromEndTrip = true;
			dbAdapter.insertUServicesInfo(uServicesInfo);

			//Upload the Visit Information
			List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList("VisitType");

			UVisitInfo visitInfo = new UVisitInfo();
			visitInfo.nTrxnNo = nTrxnNo;
			visitInfo.strDocType = "VIS";
			visitInfo.nChainNo = info.customerInfo.nChainNo;
			visitInfo.nCustNo = info.customerInfo.nCustNo;
			visitInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, curDate));
			visitInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(curDate);
			visitInfo.nPrintedFlag = 0;
			visitInfo.strVisitReason = mDescTypeList.get(info.nReasonIdx).strAlphaKey;
			visitInfo.strVisitReasonID = mDescTypeList.get(info.nReasonIdx).strDescriptionTypeID;
			visitInfo.strSaleDate = visitInfo.strTrxnDate;
			visitInfo.strVisitStart = Global.strVisitStart;
			visitInfo.strVisitEnd = new SimpleDateFormat("kkmmss").format(new Date());
			visitInfo.strNextVisit = "";
			visitInfo.strVisitMessage = "";
			visitInfo.nOrderCreated = Global.bOrderCreated ? 1 : 0;
			visitInfo.nSurveyCompleted = Global.nSurveyCompleted;
			visitInfo.nAssetChecked = Global.nAssetChecked;
			visitInfo.nAssetRequested = Global.nAssetRequested;
			visitInfo.nVisitFrequency = 0;
//			visitInfo.strVisitTypeId = (descTypeInfoList != null && descTypeInfoList.size() > 0 ? descTypeInfoList.get(0).strDescriptionEntryKey : "0");
			dbAdapter.insertUVisitInfo(visitInfo);
		}

		dbAdapter.close();
		Utils.setBooleanSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_END_TRIP_CHECKED, 0), true);
		//Global.gUploadCustNoList.add(0);
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				mContext.onBackPressed();
			}
		});

	}

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnReturn:
				mContext.onBackPressed();
				break;
			case R.id.btnProceed:
				new Thread(){
					@Override
					public void run() {
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								try {
									showProgressDialog(Str.CreatingPdf[Global.LANG]);
								}catch (Exception e)
								{
									e.printStackTrace();
								}

							}
						});
						createPDF();
						doComplete();
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								hideProgressDialog();
							}
						});

					}
				}.start();
				break;
		}
		m_bClicked = false;
	}

	private class ListAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView txtName, txtCustNo, txtReason, txtCartons, txtUnits;
		}

		public int getCount() {
			return mIncompleteList.size();
		}

		public IncompleteInfo getItem(int position) {
			if(position < mIncompleteList.size())
			{
				return mIncompleteList.get(position);
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
			final IncompleteInfo info = getItem(position);
			if(info == null) return null;
			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_incomplete_deliveries, parent, false);
				holder = new ViewHolder();
				holder.txtName = (TextView) view.findViewById(R.id.txtName);
				holder.txtCustNo = (TextView) view.findViewById(R.id.txtCustNo);
				holder.txtReason = (TextView) view.findViewById(R.id.txtReason);
				holder.txtCartons = (TextView) view.findViewById(R.id.txtCartons);
				holder.txtUnits = (TextView) view.findViewById(R.id.txtUnits);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.txtName.setText(info.customerInfo.strName);
			holder.txtCustNo.setText(String.valueOf(info.customerInfo.nCustNo));
			if (info.nReasonIdx == -1) {
				holder.txtReason.setText(Str.NonSalesReason[Global.LANG]);
			} else {
				holder.txtReason.setText(mDescTypeList.get(info.nReasonIdx).strDescription);
			}
			holder.txtCartons.setText(String.format(Locale.ENGLISH, "%d %s", info.nCartons, Str.cartons[Global.LANG]));
			holder.txtUnits.setText(String.format(Locale.ENGLISH, "%d %s", info.nUnits, Str.units[Global.LANG]));

			holder.txtReason.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showReasonCodeList(position);
				}
			});

			return view;
		}
	}

	private List<CustomerInfo> filterSameCustomer(List<CustomerInfo> customerInfoList)
	{
		Map<String, CustomerInfo> customerInfoMap = new HashMap<>();
		List<CustomerInfo> newList = new ArrayList<>();
		for(CustomerInfo customerInfo : customerInfoList)
		{
			String key = customerInfo.nChainNo + "___" + customerInfo.nCustNo;
			CustomerInfo info = customerInfoMap.get(key);
			if( info == null || info.bCompleted == false)
			{
				customerInfoMap.put(key, customerInfo);
				newList.add(customerInfo);
			}
		}
		return newList;
	}
}