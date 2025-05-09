package dsd.dsdvansales.parsers;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.text.Html;
import android.text.TextUtils;

import com.bugsee.library.Bugsee;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.DownloadEngine;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.*;
import dsd.dsdvansales.services.DownloadJobService;

public class ParseXMLFile {

	public static int fillDataToDatabase(Context mContext, boolean bInit) {

		//Read the txt file
		String strHtmlRule = Utils.readFile(String.format(Locale.ENGLISH, "%s%s/html.txt", Global.DOWNLOAD_DIR, Global.gTripNumber));
		if(!TextUtils.isEmpty(strHtmlRule))
		{
			Gson gson = new Gson();
            Utils.htmlInstance = gson.fromJson(strHtmlRule, HTMLInstance.class);
			for(HtmlObj obj : Utils.htmlInstance.parsing)
			{
				Utils.encodedHtmlMap.put(obj.key, obj.value);
			}
            for(HtmlObj obj : Utils.htmlInstance.getting)
            {
                Utils.decodedHtmlMap.put(obj.key, obj.value);
            }
		}

		int bRes = 1;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			File xmlFile;
			FileInputStream fileInputStream;
			Document document;
			Element element;
			NodeList nodeList;
			ByteArrayInputStream sBufInputSream;
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();

			//Checking RouteCtl.
			if( bInit )
			{
				xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/ROUTECTL.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
				if(xmlFile.exists())
				{
					fileInputStream = new FileInputStream(xmlFile);
					byte[] buf = new byte[(int)xmlFile.length()];
					fileInputStream.read(buf);
					fileInputStream.close();
					document = Utils.parseXML(builder, buf);

					element = document.getDocumentElement();
					nodeList = element.getElementsByTagName("RouteCtl");
					if( nodeList.getLength() == 0 )
					{
						dbAdapter.close();
						return 2;
					}
				}
				else
				{
					dbAdapter.close();
					return 2;
				}
			}


			dbAdapter.beginTransacion();
			if( bInit )
			{
				dbAdapter.deleteCollectPaymentStatus();
				dbAdapter.deleteAllUOrderHInfo();
				dbAdapter.deleteAllUServicesInfo();
				dbAdapter.deleteAllUCollectionsDInfo();
				dbAdapter.deleteAllUInvenDInfo();
				dbAdapter.deleteAllUAssetsInfo();
				dbAdapter.deleteAllOrderHistoryInfos();
				dbAdapter.deleteDayNo();
				dbAdapter.deleteWhiteCustomerList();

				dbAdapter.deleteAllEquipment();
				dbAdapter.deleteAllEquipAss();
				dbAdapter.deleteEquipCompleteStatus();
				dbAdapter.deleteAllProdDetlInfo();
				dbAdapter.deleteAllLoadHeadInfo();
				dbAdapter.deleteAllLoadDetlInfo();
				dbAdapter.deleteAllMessageInfo();
				dbAdapter.deleteAllProdStruInfo();
				dbAdapter.deleteAllDescTypeInfo();
				dbAdapter.deleteAllProdLocnInfo();
				dbAdapter.deleteAllSurveyInfo();
				dbAdapter.deleteAllSurvDistInfo();
				dbAdapter.deleteAllSurvHeadInfo();
				dbAdapter.deleteAllSurvQuesInfo();
				dbAdapter.deleteAllSurvQoptInfo();
				dbAdapter.deleteAllCustomerInfo();
				dbAdapter.deleteAllCustContactInfo();
				dbAdapter.deleteAllPresoldOrHInfo();
				dbAdapter.deleteAllPresoldOrDInfo();
				dbAdapter.deleteAllPricingInfo();
				dbAdapter.deleteAllPromNovoInfo();
				dbAdapter.deleteAllPriceGrpInfo();
				dbAdapter.deleteAllPromAssInfo();
				dbAdapter.deleteAllPromHeadInfo();
				dbAdapter.deleteAllProMultiInfo();
				dbAdapter.deleteAllTaxCodesInfo();
				dbAdapter.deleteAllTaxRatesInfo();
				dbAdapter.deleteAllCompany();
				dbAdapter.deleteAllDocTextInfo();
				dbAdapter.deleteTripUser();
				dbAdapter.deleteAllARHeaderInfo();
				dbAdapter.deleteAuthHeaderInfo();
				dbAdapter.deleteAuthDetlInfo();
				dbAdapter.deleteStatusInfo();
				dbAdapter.deleteAllCustomerNote();
				dbAdapter.deleteAllUCustomerNote();
				dbAdapter.deleteAllUAttachment();
				dbAdapter.deleteAllTransactionHistoryInfo();
				dbAdapter.deleteCompletedSurvey();
				dbAdapter.deleteAllCustAddInfo();
				dbAdapter.deleteAllUVisitInfo();

				dbAdapter.deleteAllCustConBalInfo();
				dbAdapter.deleteAllLangInfo();
				dbAdapter.deleteAllPrint();
				dbAdapter.deleteAllProdInfos();
				/*UploadDbAdapter uploadDbAdapter = new UploadDbAdapter(mContext);
				uploadDbAdapter.open();
				uploadDbAdapter.deleteAllUploadFiles();
				uploadDbAdapter.close();*/

				/*PrepareUploadDbAdapter prepareUploadDbAdapter = new PrepareUploadDbAdapter(mContext);
				prepareUploadDbAdapter.open();
				prepareUploadDbAdapter.deleteAllUploadFiles();
				prepareUploadDbAdapter.close();
				 */

				dbAdapter.deleteAllLoginInfo();

				//For Lot Processing
				dbAdapter.deleteAllLoadBlot();
				dbAdapter.deleteAllULoadBlot();
				dbAdapter.deleteAllBatchLot();

				dbAdapter.deleteUBillingDetailsInfo();
				dbAdapter.deleteCustAdditionalInfo();
				dbAdapter.deleteContactDetailsInfo();

				Utils.setStringSetting(mContext, Global.SALE_TARGET_MTH, "");
				Utils.setStringSetting(mContext, Global.SALE_TARGET_DAY, "");
				Utils.setStringSetting(mContext, Global.SALE_ACTUAL_MTD, "");
				Utils.setStringSetting(mContext, Global.SALE_DAYS_REMAINING, "");
				Utils.setStringSetting(mContext, Global.SALE_PERIODDESC, "");
				Utils.setFloatSetting(mContext, Global.SALE_TODAY, 0.0f);
				Utils.setFloatSetting(mContext, Global.RETURNS_TODAY, 0.0f);
				Utils.setFloatSetting(mContext, Global.COLLECTIONS_TODAY, 0.0f);
				Utils.setIntSetting(mContext, Global.NEW_CUSTOMERS_TODAY, 0);

				Utils.setIntSetting(mContext, Global.TOTAL_SAMPLE_COUNT, 0);
				Utils.setIntSetting(mContext, Global.TOTAL_SALES_COUNT, 0);
				Utils.setIntSetting(mContext, Global.TOTAL_RETURNS_COUNT, 0);

				Utils.setIntSetting(mContext, Global.DAY_NO, Utils.getTodayWeekNo());

				Utils.setIntSetting(mContext, Global.PREF_POST_VISIT, 0);
				Utils.setStringSetting(mContext, Global.PREF_RETURNS, "");

				Utils.setStringSetting(mContext, Global.PREF_DISABLE_OFFLOADS, "");
				if(Global.gLoadTruckCustomerList != null)
				{
					Global.gLoadTruckCustomerList.clear();
					Global.gLoadTruckCustomerList = null;
				}
			}

			// Get ROUTECTL.XML
			//Utils.registerLoadTime("ROUTECTL Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/ROUTECTL.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {
				for (int i = 0; i < 5; i++) {
					Utils.setBooleanSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_END_TRIP_CHECKED, i), false);
				}
				if( bInit )
				{
					Utils.deleteDirectory(Global.PDF_LOCAL_DIR);
					Utils.deleteDirectory(Global.QRCODE_LOCAL_DIR);
					dbAdapter.deleteAllUInvenHInfo(0, true);
					dbAdapter.deleteAllUOrderHInfo();

					Utils.setStringSetting(mContext, Global.PREF_ROUTE_NUMBER, "");
					Utils.setStringSetting(mContext, Global.PREF_DRIVER_NUMBER, "");
					Utils.setStringSetting(mContext, Global.PREF_LOGIN_USER_NAME, "");
					Utils.setStringSetting(mContext, Global.PREF_ROUTE_PAYMENTADJUST, "");
					Utils.setStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND, "");
					Utils.setStringSetting(mContext, Global.PREF_USER_DESCRIPTION, "");
					Utils.setStringSetting(mContext, Global.PREF_LOGIN_PIN_NUMBER, "");
					Utils.setStringSetting(mContext, Global.PREF_REFRESH_PIN_NUMBER, "");
					Utils.setStringSetting(mContext, Global.PREF_COMMUNICATION_PIN_CODE, "");
					Utils.setStringSetting(mContext, Global.PREF_EXIT_PIN_CODE, "");
					Utils.setIntSetting(mContext, Global.PREF_GPS_POLL, 0);
					Utils.setStringSetting(mContext, Global.PREF_TRIP_NUMBER, "");
					Utils.setStringSetting(mContext, Global.PREF_TRIP_DATE, "");
					Utils.setIntSetting(mContext, Global.PREF_LOAD_CUSTOMER, 0);
					Utils.setIntSetting(mContext, Global.PREF_LOAD_CONTAINERS, 0);
					Utils.setStringSetting(mContext, Global.PREF_DEF_LOCN_NO, "");
					Utils.setStringSetting(mContext, Global.PREF_VEHICLE_NUMBER, "");
					Utils.setLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO, 0);
					Utils.setLongSetting(mContext, Global.PREF_LOADED_UINVEN_TRXN_NO, 0);
					Utils.setBooleanSetting(mContext, Global.PREF_LOAD_INVENTORY_COMPLETED, false);
					Utils.setIntSetting(mContext, Global.PREF_DELFUTDTE, 0);
					Utils.setStringSetting(mContext, Global.PREF_CURRENCY_SYMBOL, "$");
					Utils.setIntSetting(mContext, Global.PREF_EWAY_CARD_PROC, 0);
					Utils.setIntSetting(mContext, Global.PREF_ADJUST_ALLOW, 0);
					Utils.setIntSetting(mContext, Global.PREF_TEMP_CUST_CHAIN, 0);
					Utils.setIntSetting(mContext, Global.PREF_TEMP_CUST_PREFIX, 0);
					Utils.setIntSetting(mContext, Global.PREF_PRIME_CONTACT, 0);
					Utils.setIntSetting(mContext, Global.PREF_CUST_ADD_NEW, 0);
					Utils.setStringSetting(mContext, Global.PREF_EWAY_API, "");
					Utils.setStringSetting(mContext, Global.PREF_EWAY_SYSTEM, "");
					Utils.setStringSetting(mContext, Global.PREF_UPLOAD_INV_FMT, "");
					Utils.setStringSetting(mContext, Global.PREF_EWAY_API_KEY, "");
					Utils.setStringSetting(mContext, Global.PREF_EWAY_PASSWORD, "");
					Utils.setIntSetting(mContext, Global.PREF_PRINT_TYPE, 0);
					Utils.setStringSetting(mContext, Global.PREF_PRINTER_SIZE, "");
					Utils.setStringSetting(mContext, Global.PREF_PRINTER_NAME, "");
					Utils.setStringSetting(mContext, Global.PREF_HONEYWELLLINE_PRINTER_TEMPLATE_SUFFIX, "");
					Utils.setBooleanSetting(mContext, Global.PREF_LOAD_QTY_ENTRY, false);
					Utils.setIntSetting(mContext, Global.PREF_INVENCKECK, 0);
					Utils.setIntSetting(mContext, Global.PREF_INVENQTYOFF, 0);
					Utils.setIntSetting(mContext, Global.PREF_LOADMULTIALL, 0);
					Utils.setBooleanSetting(mContext, Global.PREF_PASSED_LOAD_SECTION, false);
					Utils.setIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_COLLECTION_SEQUENCE_NO, Global.gTripNumber), 0);
					Utils.setStringSetting(mContext, Global.PREF_INVOICE_NUM_FORMAT, "RRRYJJJSSS");
					Utils.setIntSetting(mContext, Global.PREF_SALENOTEMAN, 0);
					Utils.setIntSetting(mContext, Global.PREF_LOADREQUEST, 0);
					Utils.setIntSetting(mContext, Global.PREF_LOADSTKTAKE, 0);
					Utils.setIntSetting(mContext, Global.PREF_SALEQTYENTRY, 0);
					Utils.setIntSetting(mContext, Global.PREF_LOADDEFER, 0);
					Utils.setIntSetting(mContext, Global.PREF_LOADPERDDEF, 0);
					Utils.setStringSetting(mContext, Global.PREF_INVOICENUM, "");
					Utils.setStringSetting(mContext, Global.PREF_COMPANY_NAME, "");
					Utils.setIntSetting(mContext, Global.PREF_EQUIPCHECK, 0);
					Utils.setStringSetting(mContext, Global.PREF_EQUIPRMKEY, "");
					Utils.setStringSetting(mContext, Global.PREF_EQUIPREPREQ, "");
					Utils.setStringSetting(mContext, Global.PREF_PRINT_FONT_FAMILY, "");
					Utils.setStringSetting(mContext, Global.PREF_PRINT_FONT_FILENAME, "");
					Utils.setStringSetting(mContext, Global.PREF_PRINT_FONT_ENCODING, "");
					Utils.setBooleanSetting(mContext, Global.PREF_PRINT_FONT_EMBEDED, false);
					Utils.setStringSetting(mContext, Global.PREF_ALLOW_VOID, "");
					Utils.setStringSetting(mContext, Global.PREF_TREE_DESC, "");
					Utils.setStringSetting(mContext, Global.PREF_PRINT_PICK_LIST, "");
					Utils.setStringSetting(mContext, Global.PREF_CUSTINFO_MSG, "");
					Utils.setStringSetting(mContext, Global.PREF_COMMSMETHOD, "");

                    //For Routific Api
                    Utils.setStringSetting(mContext, Global.PREF_ROUTIFICAPI, "");
                    Utils.setIntSetting(mContext, Global.PREF_VISITDURATION, 0);
                    Utils.setDoubleSetting(mContext, Global.PREF_LOCON_NO_GPS_LATITUDE, 0);
                    Utils.setDoubleSetting(mContext, Global.PREF_LOCON_NO_GPS_LONGITUDE, 0);

                    Utils.setStringSetting(mContext, Global.PREF_DELNOTEMAN, "");

                    //For Mint Api Key
					Utils.setStringSetting(mContext, Global.PREF_MINTAPI, "");
					Utils.setStringSetting(mContext, Global.PREF_MINTSECRET, "");
					Utils.setStringSetting(mContext, Global.PREF_MINTUSER, "");
					Utils.setStringSetting(mContext, Global.PREF_MINTMODE, "");

					Utils.setStringSetting(mContext, Global.PREF_MINTCNPUSER, "");
					Utils.setStringSetting(mContext, Global.PREF_MINTCNPPWD, "");

					Utils.setStringSetting(mContext, Global.PREF_MINTCN_BASE_URL, "");


					//Load Temp
					Utils.setFloatSetting(mContext, Global.PREF_LOAD_TEMPMAX, 0.0f);
					Utils.setFloatSetting(mContext, Global.PREF_LOAD_TEMPMIN, 0.0f);

					Utils.setStringSetting(mContext, Global.PREF_INVENTORYUOM, "");

					Utils.setStringSetting(mContext, Global.PREF_KK_VALUE, "");
					Utils.setStringSetting(mContext, Global.PREF_USEORDNOASINV, "");

					Utils.setBooleanSetting(mContext, Global.PREF_CREATED_UNSERVICED_REASON_REPORT, false);
					Utils.setIntSetting(mContext, Global.PREF_SHOWSALESPERF, 0);

					//Ignore R Load Flag
					Utils.setStringSetting(mContext, Global.PREF_IGNORERLOAD, "");
					Utils.setLongSetting(mContext, Global.PREF_LAST_DOC_NO, 0);

					Utils.setStringSetting(mContext, Global.PREF_INVCALC, "");

					Utils.setBooleanSetting(mContext, Global.PREF_VISIT_SELECT_CUSTOMER, false);

					Utils.setIntSetting(mContext, Global.PREF_SHOW_INV, 0);
					Utils.setIntSetting(mContext, Global.PREF_DETAIL_ENTRY, 0);

					Utils.setStringSetting(mContext, Global.PREF_ATTACHMENT_LOGIN_PASSWORD, "");
					Utils.setStringSetting(mContext, Global.PREF_ATTACHMENT_LOGIN_PIN_NUMBER, "");
					Utils.setStringSetting(mContext, Global.PREF_ATTACHMENT_LOGIN_USERNAME, "");
					Utils.setStringSetting(mContext, Global.PREF_QTY_ENTRY_METHOD, "");

					Utils.setIntSetting(mContext, Global.PREF_SCALING, 0);
					Utils.setIntSetting(mContext, Global.PREF_DECIMAL, 2);

					Utils.setIntSetting(mContext, Global.PREF_SUPERVISOR, 0);
					Utils.setStringSetting(mContext, Global.PREF_SUPERVISOR_USER_PASSWORD, "");
					Utils.setStringSetting(mContext, Global.PREF_LOADADJUSTMENT_APPROVAL_KEY, "");
					Utils.setStringSetting(mContext, Global.PREF_LOADREQUEST_APPROVAL_KEY, "");
					Utils.setStringSetting(mContext, Global.PREF_SECURITY9_KEY, "");

					Utils.setIntSetting(mContext, Global.PREF_GET_GPS, 0);

					Utils.setStringSetting(mContext, Global.PREF_HANDLING_FEE_TAX_CODE, "");

					Utils.setStringSetting(mContext, Global.PREF_MINTHPPUSER, "");
					Utils.setStringSetting(mContext, Global.PREF_MINTHPPSECRET, "");
					Utils.setStringSetting(mContext, Global.PREF_MINTHPPURL, "");
					Utils.setStringSetting(mContext, Global.PREF_MINTHPPCURR, "");

					Utils.setStringSetting(mContext, Global.PREF_BARCODE_PREFIX, "");
					Utils.setStringSetting(mContext, Global.PREF_BARCODE_SUFFIX, "");

					Utils.setStringSetting(mContext, Global.PREF_DATEFMT, "");
					Utils.setStringSetting(mContext, Global.PREF_TEMPFMT, "");

					Utils.setStringSetting(mContext, Global.PREF_STRIPE_PUB_KEY, "");
					Utils.setStringSetting(mContext, Global.PREF_STRIPE_SEC_KEY, "");
					Utils.setStringSetting(mContext, Global.PREF_CURRENCY, "");

					Utils.setBooleanSetting(mContext, Global.PREF_DEBUG, false);

					Utils.setStringSetting(mContext, Global.PREF_TREE_ORDER, "");
					Utils.setStringSetting(mContext, Global.PREF_USER_NAME, "");

					Utils.setStringSetting(mContext, Global.PREF_DOCUMENTS, "");
					Utils.setStringSetting(mContext, Global.PREF_CON_EMPTY, "");

					Utils.setStringSetting(mContext, Global.PREF_CUSTTYPE, "");

					Utils.setStringSetting(mContext, Global.PREF_APIUSER, "");
					Utils.setStringSetting(mContext, Global.PREF_APIPASSWORD, "");
					Utils.setStringSetting(mContext, Global.PREF_APICOMPANY, "");
					Utils.setStringSetting(mContext, Global.PREF_GETORDERS, "");

					Utils.setBooleanSetting(mContext, Global.PREF_WINDCAVE_UAT, false);
					Utils.setStringSetting(mContext, Global.PREF_WINDCAVE_API, "");
					Utils.setStringSetting(mContext, Global.PREF_WINDCAVE_PASSWORD, "");
					Utils.setStringSetting(mContext, Global.PREF_WINDCAVE_CURR, "");

					Utils.setStringSetting(mContext, Global.PREF_WINDCAVE_HITUSERNAME, "");
					Utils.setStringSetting(mContext, Global.PREF_WINDCAVE_HITKEY, "");
					Utils.setStringSetting(mContext, Global.PREF_WINDCAVE_HITDEVICE_ID, "");
					Utils.setStringSetting(mContext, Global.PREF_WINDCAVE_HITSTATION_ID, "");
					Utils.setStringSetting(mContext, Global.PREF_WINDCAVE_HITPOSNAME, "");
					Utils.setStringSetting(mContext, Global.PREF_WINDCAVE_HITPOSVERSION, "");
					Utils.setStringSetting(mContext, Global.PREF_WINDCAVE_HITVENDORID, "");
					Utils.setBooleanSetting(mContext, Global.PREF_HOTSPOT, false);
					Utils.setStringSetting(mContext, Global.PREF_LOAD_BATCH, "");
					Utils.setStringSetting(mContext, Global.PREF_API_ROUTE_SCH, "");
					Utils.setStringSetting(mContext, Global.PREF_HIDE_SUMMARY_PAYMENT, "");
					Utils.setStringSetting(mContext, Global.PREF_USE_ORDERS_AS_LOAD, "");
					Utils.setStringSetting(mContext, Global.PREF_LOAD_REQUEST_UOM, "");
					Utils.setStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD, "");
					Utils.setStringSetting(mContext, Global.PREF_ENABLE_BREAKS, "");
					Utils.setStringSetting(mContext, Global.PREF_CRASH_REPORT, "");
					Utils.setStringSetting(mContext, Global.PREF_TRIP_USER, "");
					Utils.setStringSetting(mContext, Global.PREF_ENDTRIP_HIDE, "");
					Utils.setStringSetting(mContext, Global.PREF_EXCLUDES_TRIPS, "");
					Utils.setBooleanSetting(mContext, Global.PREF_GET_SALES_DELAY, false);
					Utils.setBooleanSetting(mContext, Global.PREF_ASKED_CHANGE_DRIVER_NAME, false);
					Utils.setStringSetting(mContext, Global.PREF_ORDER_API_END, "");
					Utils.setStringSetting(mContext, Global.PREF_ORDER_API_START, "");

					Utils.setStringSetting(mContext, Global.PREF_FRGOODPRM, "");
					Utils.setStringSetting(mContext, Global.PREF_FRGOODPLN, "");
					Utils.setStringSetting(mContext, Global.PREF_FRGOODASS, "");

					Utils.setStringSetting(mContext, Global.PREF_SEARCH_KEYWORD, "");
					Utils.setStringSetting(mContext, Global.PREF_SUMMARY_DETAIL, "");

					Utils.setIntSetting(mContext, Global.PREF_SEARCH_CRITIA_IDX, 0);
					Utils.setBooleanSetting(mContext, Global.PREF_SEARCH_FILTER, false);

					Utils.setIntSetting(mContext, Global.PREF_DOC_ARCHIVE_HISTORY_DAYS, 90);

					Utils.setStringSetting(mContext, Global.PREF_TRIP_ID, "");
					Utils.setStringSetting(mContext, Global.PREF_LOT_ASSIGNMENT, "");

					Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, false);

					Utils.setStringSetting(mContext, Global.PREF_DisableFTP, "");

					Utils.setStringSetting(mContext, Global.PREF_DisableInterTruck, "");
					Utils.setStringSetting(mContext, Global.PREF_UNLESHED, "");
					Utils.setStringSetting(mContext, Global.PREF_TEMPREQ, "");
					Utils.setStringSetting(mContext, Global.PREF_UI_DETL_SORT, "");
					Utils.setLongSetting(mContext, Global.PREF_BREAK_DOWN_START_TIME, 0);
					Utils.setStringSetting(mContext, Global.PREF_DRIVER_BREAK_DOWN_TYPE_INFO, "");

					Utils.setStringSetting(mContext, Global.PREF_PRINT_ZERO_QTY_PRESOLD_ITEM, "");
					Utils.setStringSetting(mContext, Global.PREF_TEMPLATE_CHAIN, "");
					Utils.setStringSetting(mContext, Global.PREF_TEMPLATE_CUST, "");
					Utils.setStringSetting(mContext, Global.PREF_ALLOW_REPEAT_SALES, "");
					Utils.setStringSetting(mContext, Global.PREF_STKTKEGRP, "");
					Utils.setStringSetting(mContext, Global.PREF_STKROLGRP, "");
					Utils.setStringSetting(mContext, Global.PREF_HONEYWELL_PRINTER, "");
					Utils.setStringSetting(mContext, Global.PREF_TEMP_CUST_MASK, "");
					Utils.setStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE, "");
					Utils.setStringSetting(mContext, Global.PREF_LOAD_SRT_DEF, "");
					Utils.setStringSetting(mContext, Global.PREF_ROUTEFIC_LOG, "");

					Utils.setLongSetting(mContext, Global.PREF_LOGIN_START_TIME, 0);
					Utils.setStringSetting(mContext, Global.PREF_SHOW_SOMA, "");

					Utils.setStringSetting(mContext, Global.PREF_LOAD_REQUEST_EOT, "");
					Utils.setStringSetting(mContext, Global.PREF_LOAD_REQUEST_DATA, "");
					Utils.setStringSetting(mContext, Global.PREF_DISALLOW_ACCOUNT_PAY, "");
					Utils.setStringSetting(mContext, Global.PREF_DISABLE_INV_REPRINT, "");
					Utils.setStringSetting(mContext, Global.PREF_TENDER_FULL_AMOUNT, "");
					Utils.setStringSetting(mContext, Global.PREF_DEFAULT_TODAY_SCREEN, "");
					Utils.setStringSetting(mContext, Global.PREF_DISABLE_NEG_INV_AMT, "");
					Utils.setStringSetting(mContext, Global.PREF_DISABLE_LOAD_ADJUST, "");
					Utils.setStringSetting(mContext, Global.PREF_DISABLE_FREE, "");
					Utils.setStringSetting(mContext, Global.PREF_LOAD_SHEET_INCLUDE_RLOAD, "");
					Utils.setStringSetting(mContext, Global.PREF_PRINT_ERROR_LOG, "");
					Utils.setStringSetting(mContext, Global.PREF_DISABLE_END_TRIP_ERROR_LOG, "");
					Utils.setStringSetting(mContext, Global.PREF_DISABLE_UPLOAD_LOG, "");
					Utils.setStringSetting(mContext, Global.PREF_DISABLE_LOGOUT, "");
					Utils.setStringSetting(mContext, Global.PREF_ROUTECTL_TRIP_DATE, "");
					Utils.setStringSetting(mContext, Global.PREF_DISABLE_AFTER_EOT, "");
					Utils.setStringSetting(mContext, Global.PREF_EOT_PIN, "");
					Utils.setStringSetting(mContext, Global.PREF_ROUTE_NUMBER_HAS_SUFFIX, "");
					Utils.setStringSetting(mContext, Global.PREF_CREDIT_IN_EOD_COLLECTION, "");
					Utils.setStringSetting(mContext, Global.PREF_SEPARATE_PERCENT_OFF_PROMO, "");
					Utils.setStringSetting(mContext, Global.PREF_NOT_AT_STORE_RADIUS, "");
					Utils.setStringSetting(mContext, Global.PREF_NOT_AT_STORE, "");
					Utils.setStringSetting(mContext, Global.PREF_PROD_SEARCH_IMAGE_MODE, "");
					Utils.setStringSetting(mContext, Global.PREF_TREE_AS_IMAGE, "");
					Utils.setStringSetting(mContext, Global.PREF_HIDE_CUST_MAINTENANCE, "");
					Utils.setStringSetting(mContext, Global.PREF_DISABLE_GPS_PIN, "");
					Utils.setStringSetting(mContext, Global.PREF_MFA_SECRET, "");
					Utils.setStringSetting(mContext, Global.PREF_CHECK_RETURN_QTY, "");
					Utils.setStringSetting(mContext, Global.PREF_MAX_OPEN_DAYS_UNAUTH, "");

					if(Global.bugSeeRecording)
					{
						Bugsee.stop();
					}
					Global.bugSeeRecording = false;

					Utils.resetScalingSufix();
					Global.MULTIPLE_VALUE = 100000;
				}

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("RouteCtl");
				if (nodeList.getLength() > 0) {
					Node item = nodeList.item(0);
					NamedNodeMap Attrs = item.getAttributes();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("RouteNumber") == true) {
							int nRouteNumber = Utils.getIntFromString(strNodeValue);
							String strRouteNumber = String.format(Locale.ENGLISH, "%06d", nRouteNumber);
							Utils.setStringSetting(mContext, Global.PREF_ROUTE_NUMBER, strRouteNumber);
							Utils.setStringSetting(mContext, Global.PREF_DRIVER_NUMBER, strNodeValue);
						} else if (strNodeName.equals("UserName") == true) {
							Utils.setStringSetting(mContext, Global.PREF_LOGIN_USER_NAME, strNodeValue);
							Utils.setStringSetting(mContext, Global.PREF_USER_NAME, strNodeValue);
						} else if (strNodeName.equals("CENTSROUND") == true) {
							Utils.setStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND, strNodeValue);
						} else if (strNodeName.equals("PAYMENTADJUST") == true) {
							Utils.setStringSetting(mContext, Global.PREF_ROUTE_PAYMENTADJUST, strNodeValue);
						} else if (strNodeName.equals("Description") == true) {
							Utils.setStringSetting(mContext, Global.PREF_USER_DESCRIPTION, strNodeValue);
							Utils.setStringSetting(mContext, Global.PREF_USER_LOGIN_DESCRIPTION, strNodeValue);
						} else if (strNodeName.equals("Security1") == true) {
							Utils.setStringSetting(mContext, Global.PREF_LOGIN_PIN_NUMBER, strNodeValue);
						} else if (strNodeName.equals("Security3") == true) {
							Utils.setStringSetting(mContext, Global.PREF_REFRESH_PIN_NUMBER, strNodeValue);
						} else if (strNodeName.equals("Security4") == true) {
							Utils.setStringSetting(mContext, Global.PREF_COMMUNICATION_PIN_CODE, strNodeValue);
						} else if (strNodeName.equals("Security5") == true) {
							Utils.setStringSetting(mContext, Global.PREF_EXIT_PIN_CODE, strNodeValue);
						} else if (strNodeName.equals("Security6") == true) {
							Utils.setStringSetting(mContext, Global.PREF_SUPERVISOR_USER_PASSWORD, strNodeValue);
						} else if (strNodeName.equals("Security7") == true) {
							Utils.setStringSetting(mContext, Global.PREF_LOADADJUSTMENT_APPROVAL_KEY, strNodeValue);
						} else if (strNodeName.equals("Security8") == true) {
							Utils.setStringSetting(mContext, Global.PREF_LOADREQUEST_APPROVAL_KEY, strNodeValue);
						} else if (strNodeName.equals("Security9") == true) {
							Utils.setStringSetting(mContext, Global.PREF_SECURITY9_KEY, strNodeValue);
						} else if (strNodeName.equals("GPSPOLL") == true) {
							int nGPSPoll = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_GPS_POLL, nGPSPoll);
						} else if (strNodeName.equals("Trip") == true) {
							Utils.setStringSetting(mContext, Global.PREF_TRIP_NUMBER, strNodeValue);
						} else if (strNodeName.equals("TripDate") == true) {
							Utils.setStringSetting(mContext, Global.PREF_TRIP_DATE, strNodeValue);
						} else if (strNodeName.equals("LOADCUST") == true) {
							int nLoadCust = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_LOAD_CUSTOMER, nLoadCust);
						} else if (strNodeName.equals("LOADCONT") == true) {
							int nLoadCont = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_LOAD_CONTAINERS, nLoadCont);
						} else if (strNodeName.equals("DefLocnNo") == true) {
							Utils.setStringSetting(mContext, Global.PREF_DEF_LOCN_NO, strNodeValue);
						} else if (strNodeName.equals("VehicleNumber") == true) {
							Utils.setStringSetting(mContext, Global.PREF_VEHICLE_NUMBER, strNodeValue);
						} else if (strNodeName.equals("DELFUTDTE") == true) {
							int nDeliverDay = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_DELFUTDTE, nDeliverDay);
						} else if (strNodeName.equals("CurrencySymbol") == true) {
							if (strNodeValue.length() > 0) {
								Utils.setStringSetting(mContext, Global.PREF_CURRENCY_SYMBOL, strNodeValue);
							}
						} else if (strNodeName.equals("CARDPROC") == true) {
							int nCardProc = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_EWAY_CARD_PROC, nCardProc);
						} else if (strNodeName.equals("ADJUSTALLOW") == true) {
							int nAdjustAllow = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_ADJUST_ALLOW, nAdjustAllow);
						} else if (strNodeName.equals("TEMPCUSTCHAIN") == true) {
							int nTempCustChain = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_TEMP_CUST_CHAIN, nTempCustChain);
						} else if (strNodeName.equals("TEMPCUSTPREFIX") == true) {
							int nTempCustPrefix = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_TEMP_CUST_PREFIX, nTempCustPrefix);
						} /*else if (strNodeName.equals("TEMPLATECHAIN") == true) {
							int nTempChain = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_TEMP_CHAIN, nTempChain);
						} else if (strNodeName.equals("TEMPLATECUSTOMER") == true) {
							int nTempCustomer = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_TEMP_CUSTOMER, nTempCustomer);
						} */else if (strNodeName.equals("PRIMECONTACT") == true) {
							int nPrimeContact = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_PRIME_CONTACT, nPrimeContact);
						} else if (strNodeName.equals("CUSTADDNEW") == true) {
							int nCustAddNew = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_CUST_ADD_NEW, nCustAddNew);
						} else if (strNodeName.equals("EWAY_API") == true){
							Utils.setStringSetting(mContext, Global.PREF_EWAY_API, strNodeValue);
						} else if (strNodeName.equals("EWAY_SYSTEM") == true){
							Utils.setStringSetting(mContext, Global.PREF_EWAY_SYSTEM, strNodeValue);
						} else if (strNodeName.equals("RAPIDAPIKEY") == true){
							Utils.setStringSetting(mContext, Global.PREF_EWAY_API_KEY, strNodeValue);
						} else if (strNodeName.equals("RAPIDAPIPWD") == true){
							Utils.setStringSetting(mContext, Global.PREF_EWAY_PASSWORD, strNodeValue);
						} else if (strNodeName.equals("UPLOADINVFMT") == true){
							Utils.setStringSetting(mContext, Global.PREF_UPLOAD_INV_FMT, strNodeValue);
						} else if (strNodeName.equals("PRINTTYPE") == true){
							int nPrintType = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_PRINT_TYPE, nPrintType);
						} else if(strNodeName.equals("LOADQTYENTRY") == true){
							int nLoadQtyEntry = Utils.getIntFromString(strNodeValue);
							Utils.setBooleanSetting(mContext, Global.PREF_LOAD_QTY_ENTRY, nLoadQtyEntry == 1);
						} else if (strNodeName.equals("INVENCHECK") == true){
							int nPrintType = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_INVENCKECK, nPrintType);
						} else if (strNodeName.equals("INVENQTYOFF") == true){
							int nInvenQtyOff = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_INVENQTYOFF, nInvenQtyOff);
						} else if ( strNodeName.equals("INVOICENUM") == true){
							if( TextUtils.isEmpty(strNodeValue.trim()) || strNodeValue.equals("0") )
							{
								continue;
							}
							if(strNodeValue.contains("U"))
							{
								Utils.setStringSetting(mContext, Global.PREF_INVOICENUM, strNodeValue);
							}
							else
							{
								Utils.setStringSetting(mContext, Global.PREF_INVOICE_NUM_FORMAT, strNodeValue);
							}
						} else if ( strNodeName.equals("LOADMULTIALL") == true){
							int nLoadMultiAll = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_LOADMULTIALL, nLoadMultiAll);
						} else if( strNodeName.equals("SALENOTEMAN") == true){
							int nSaleNoteMan = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_SALENOTEMAN, nSaleNoteMan);
						} else if( strNodeName.equals("LOADREQUEST") == true){
							int nValue = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_LOADREQUEST, nValue);
						} else if( strNodeName.equals("LOADSTKTAKE") == true){
							int nValue = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_LOADSTKTAKE, nValue);
						} else if( strNodeName.equals("SALEQTYENTRY") == true){
							int nValue = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_SALEQTYENTRY, nValue);
						} else if ( strNodeName.equals("LOADDEFER") == true){
							int nValue = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_LOADDEFER, nValue);
						} else if ( strNodeName.equals("LOADPERDEF") == true){
							int nValue = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_LOADPERDDEF, nValue);
						} else if ( strNodeName.equals("COMPANY") == true){
							Utils.setStringSetting(mContext, Global.PREF_COMPANY_NAME, strNodeValue);
						} else if ( strNodeName.equals("EQUIPCHECK") == true){
							int nValue = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_EQUIPCHECK, nValue);
						} else if ( strNodeName.equals("EQUIPRMKEY") == true){
							Utils.setStringSetting(mContext, Global.PREF_EQUIPRMKEY, strNodeValue.trim());
						} else if ( strNodeName.equals("EQUIPREPREQ") == true){
							Utils.setStringSetting(mContext, Global.PREF_EQUIPREPREQ, strNodeValue.trim());
						} else if( strNodeName.equals("PRINT_FONT_FAMILY") == true){
							Utils.setStringSetting(mContext, Global.PREF_PRINT_FONT_FAMILY, strNodeValue);
						} else if( strNodeName.equals("PRINT_FONT_ENCODING") == true){
							Utils.setStringSetting(mContext, Global.PREF_PRINT_FONT_ENCODING, strNodeValue);
						} else if( strNodeName.equals("PRINT_FONT_FILENAME") == true){
							Utils.setStringSetting(mContext, Global.PREF_PRINT_FONT_FILENAME, strNodeValue);
						} else if( strNodeName.equals("PRINT_FONT_EMBEDED") == true){
							if(strNodeValue.equals("1"))
							{
								Utils.setBooleanSetting(mContext, Global.PREF_PRINT_FONT_EMBEDED, true);
							}
							else
							{
								Utils.setBooleanSetting(mContext, Global.PREF_PRINT_FONT_EMBEDED, false);
							}
						} else if(strNodeName.toLowerCase().equals("allowvoid")) {
							Utils.setStringSetting(mContext, Global.PREF_ALLOW_VOID, strNodeValue);
						} else if(strNodeName.toLowerCase().equals("treedesc")) {
							Utils.setStringSetting(mContext, Global.PREF_TREE_DESC, strNodeValue);
						} else if(strNodeName.toLowerCase().equals("printpicklist")) {
							Utils.setStringSetting(mContext, Global.PREF_PRINT_PICK_LIST, strNodeValue);
						} else if ( strNodeName.equals("CUSTINFOMSG") == true){
							Utils.setStringSetting(mContext, Global.PREF_CUSTINFO_MSG, strNodeValue);
						} else if ( strNodeName.equals("ROUTIFICAPI") == true){
                            Utils.setStringSetting(mContext, Global.PREF_ROUTIFICAPI, strNodeValue);
                        } else if ( strNodeName.equals("VISITDURATION") == true){
                            int nValue = Utils.getIntFromString(strNodeValue);
                            Utils.setIntSetting(mContext, Global.PREF_VISITDURATION, nValue);
                        } else if( strNodeName.equals("LocnNoGPS") == true){
                            String[] values = strNodeValue.split(",");
                            if(values.length == 2)
                            {
                                double latitude = Utils.getDoubleFromString(values[0].trim());
                                double longitude = Utils.getDoubleFromString(values[1].trim());
                                Utils.setDoubleSetting(mContext, Global.PREF_LOCON_NO_GPS_LATITUDE, latitude);
                                Utils.setDoubleSetting(mContext, Global.PREF_LOCON_NO_GPS_LONGITUDE, longitude);
                            }
                        } else if ( strNodeName.equals("DELNOTEMAN") == true){
                            Utils.setStringSetting(mContext, Global.PREF_DELNOTEMAN, strNodeValue);
                        } else if ( strNodeName.equals("MINTAPI") == true){
                            Utils.setStringSetting(mContext, Global.PREF_MINTAPI, strNodeValue);
                        } else if ( strNodeName.equals("MINTSECRET") == true){
                            Utils.setStringSetting(mContext, Global.PREF_MINTSECRET, strNodeValue);
                        } else if ( strNodeName.equals("MINTUSER") == true){
							Utils.setStringSetting(mContext, Global.PREF_MINTUSER, strNodeValue);
						} else if ( strNodeName.equals("MINTMODE") == true){
							Utils.setStringSetting(mContext, Global.PREF_MINTMODE, strNodeValue);
						} else if ( strNodeName.equals("MINTCNPUSER") == true){
							Utils.setStringSetting(mContext, Global.PREF_MINTCNPUSER, strNodeValue);
						} else if ( strNodeName.equals("MINTCNPPWD") == true){
							Utils.setStringSetting(mContext, Global.PREF_MINTCNPPWD, strNodeValue);
						} else if ( strNodeName.equals("MINTCNPBASEURL") == true){
							Utils.setStringSetting(mContext, Global.PREF_MINTCN_BASE_URL, strNodeValue);
						} else if( strNodeName.toLowerCase().equals("inventoryuom") == true) {
							Utils.setStringSetting(mContext, Global.PREF_INVENTORYUOM, strNodeValue);
						} else if ( strNodeName.equals("LOADTEMPMAX") == true){
							float fValue = Utils.getFloatFromString(strNodeValue);
							Utils.setFloatSetting(mContext, Global.PREF_LOAD_TEMPMAX, fValue);
						} else if ( strNodeName.equals("LOADTEMPMIN") == true){
							float fValue = Utils.getFloatFromString(strNodeValue);
							Utils.setFloatSetting(mContext, Global.PREF_LOAD_TEMPMIN, fValue);
						} else if ( strNodeName.equals("LOADTEMPREQ") == true){
							int nValue = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_LOAD_TEMPREQ, nValue);
						} else if( strNodeName.equals("USEORDNOASINV") ){
							Utils.setStringSetting(mContext, Global.PREF_USEORDNOASINV, strNodeValue);
						} else if( strNodeName.equals("SHOWSALESPERF") ){
							int nValue = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_SHOWSALESPERF, nValue);
						} else if( strNodeName.equals("IGNORERLOAD") ){
							Utils.setStringSetting(mContext, Global.PREF_IGNORERLOAD, strNodeValue);
						} else if( strNodeName.equals("LASTDOCNO") ){
							long value = Utils.parseLongFromString(strNodeValue) + 1;
							Utils.setLongSetting(mContext, Global.PREF_LAST_DOC_NO, value);
						} else if( strNodeName.equals("INVCALC") ) {
							Utils.setStringSetting(mContext, Global.PREF_INVCALC, strNodeValue);
						} else if( strNodeName.equals("ShowInv") ) {
							int nValue = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_SHOW_INV, nValue);
						} else if( strNodeName.equals("DetailEntry") ) {
							int nValue = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_DETAIL_ENTRY, nValue);
						} else if( strNodeName.equals("APIUSER") ) {
							Utils.setStringSetting(mContext, Global.PREF_ATTACHMENT_LOGIN_USERNAME, strNodeValue);
							Utils.setStringSetting(mContext, Global.PREF_APIUSER, strNodeValue);
						} else if( strNodeName.equals("APICOMPANY") ) {
							Utils.setStringSetting(mContext, Global.PREF_ATTACHMENT_LOGIN_PIN_NUMBER, strNodeValue);
							Utils.setStringSetting(mContext, Global.PREF_APICOMPANY, strNodeValue);
						} else if( strNodeName.equals("APIPASSWORD") ) {
							Utils.setStringSetting(mContext, Global.PREF_ATTACHMENT_LOGIN_PASSWORD, strNodeValue);
							Utils.setStringSetting(mContext, Global.PREF_APIPASSWORD, strNodeValue);
						} else if( strNodeName.equals("QtyEntryMethod") ) {
							Utils.setStringSetting(mContext, Global.PREF_QTY_ENTRY_METHOD, strNodeValue);
						} else if( strNodeName.equals("Scaling")){
							Utils.setIntSetting(mContext, Global.PREF_SCALING, Utils.getIntFromString(strNodeValue));
							Global.MULTIPLE_VALUE = 100000 / (int)(Math.pow(10, Utils.getIntSetting(mContext, Global.PREF_SCALING)));
						} else if(strNodeName.equals("Decimal")){
							Utils.setIntSetting(mContext, Global.PREF_DECIMAL, Utils.getIntFromString(strNodeValue));
						} else if(strNodeName.equals("SUPERVISOR")){
							Utils.setIntSetting(mContext, Global.PREF_SUPERVISOR, Utils.getIntFromString(strNodeValue));
						} else if(strNodeName.equals("GetGPS")){
							Utils.setIntSetting(mContext, Global.PREF_GET_GPS, Utils.getIntFromString(strNodeValue));
						} else if( strNodeName.equals("HandlingFeeTaxCode")) {
							Utils.setStringSetting(mContext, Global.PREF_HANDLING_FEE_TAX_CODE, strNodeValue);
						} else if( strNodeName.equals("MINTHPPUSER")) {
							Utils.setStringSetting(mContext, Global.PREF_MINTHPPUSER, strNodeValue);
						} else if( strNodeName.equals("MINTHPPSECRET")) {
							Utils.setStringSetting(mContext, Global.PREF_MINTHPPSECRET, strNodeValue);
						} else if( strNodeName.equals("MINTHPPURL")) {
							Utils.setStringSetting(mContext, Global.PREF_MINTHPPURL, strNodeValue);
						} else if( strNodeName.equals("MINTHPPCURR")) {
							Utils.setStringSetting(mContext, Global.PREF_MINTHPPCURR, strNodeValue);
						} else if( strNodeName.equals("BarcodePrefix")) {
							Utils.setStringSetting(mContext, Global.PREF_BARCODE_PREFIX, strNodeValue);
						} else if( strNodeName.equals("BarcodeSuffix")) {
							Utils.setStringSetting(mContext, Global.PREF_BARCODE_SUFFIX, strNodeValue);
						} else if( strNodeName.equalsIgnoreCase("datefmt")) {
							Utils.setStringSetting(mContext, Global.PREF_DATEFMT, strNodeValue.toLowerCase());
						} else if( strNodeName.equalsIgnoreCase("tempfmt")) {
							Utils.setStringSetting(mContext, Global.PREF_TEMPFMT, strNodeValue);
						} else if( strNodeName.equals("StripePubKey")) {
							Utils.setStringSetting(mContext, Global.PREF_STRIPE_PUB_KEY, strNodeValue);
						} else if( strNodeName.equals("StripeSecKey")) {
							Utils.setStringSetting(mContext, Global.PREF_STRIPE_SEC_KEY, strNodeValue);
						} else if( strNodeName.equals("Currency")) {
							Utils.setStringSetting(mContext, Global.PREF_CURRENCY, strNodeValue);
						} else if( strNodeName.equals("Debug")) {
							Utils.setBooleanSetting(mContext, Global.PREF_DEBUG, strNodeValue.equals("1"));
						} else if( strNodeName.equals("TreeOrder")) {
							Utils.setStringSetting(mContext, Global.PREF_TREE_ORDER, strNodeValue);
						} else if( strNodeName.equals("PostVisit")){
							int nValue = Utils.getIntFromString(strNodeValue);
							Utils.setIntSetting(mContext, Global.PREF_POST_VISIT, nValue);
						} else if( strNodeName.toLowerCase().equals("documents")) {
							Utils.setStringSetting(mContext, Global.PREF_DOCUMENTS, strNodeValue);
						} else if( strNodeName.equals("ConEmpty")) {
							Utils.setStringSetting(mContext, Global.PREF_CON_EMPTY, strNodeValue);
						} else if( strNodeName.equals("RETURNS")) {
							Utils.setStringSetting(mContext, Global.PREF_RETURNS, strNodeValue);
						} else if( strNodeName.equals("CUSTYPE")) {
							Utils.setStringSetting(mContext, Global.PREF_CUSTTYPE, strNodeValue);
						} else if( strNodeName.equals("CommsMethod")) {
							Utils.setStringSetting(mContext, Global.PREF_COMMSMETHOD, strNodeValue.trim());
						} else if( strNodeName.equals("GETORDERS")) {
							Utils.setStringSetting(mContext, Global.PREF_GETORDERS, strNodeValue.trim());
						} else if( strNodeName.equals("WINDCAVEUAT")) {
							if(strNodeValue.trim().toLowerCase().equals("1") || strNodeValue.trim().toLowerCase().equals("true"))
							{
								Utils.setBooleanSetting(mContext, Global.PREF_WINDCAVE_UAT, true);
							}
							else
							{
								Utils.setBooleanSetting(mContext, Global.PREF_WINDCAVE_UAT, false);
							}
						} else if( strNodeName.equals("WINDCAVEAPIKEY")) {
							Utils.setStringSetting(mContext, Global.PREF_WINDCAVE_API, strNodeValue.trim());
						} else if( strNodeName.equals("WINDCAVEPASSWORD")) {
							Utils.setStringSetting(mContext, Global.PREF_WINDCAVE_PASSWORD, strNodeValue.trim());
						} else if( strNodeName.equals("WINDCAVECURR")) {
							Utils.setStringSetting(mContext, Global.PREF_WINDCAVE_CURR, strNodeValue.trim().toUpperCase());
						} else if( strNodeName.equals("WINDCAVE_HIT_USERNAME")) {
							Utils.setStringSetting(mContext, Global.PREF_WINDCAVE_HITUSERNAME, strNodeValue);
						} else if( strNodeName.equals("WINDCAVE_HIT_KEY")) {
							Utils.setStringSetting(mContext, Global.PREF_WINDCAVE_HITKEY, strNodeValue);
						} else if( strNodeName.equals("WINDCAVE_HIT_STATIONID")) {
							Utils.setStringSetting(mContext, Global.PREF_WINDCAVE_HITSTATION_ID, strNodeValue);
						} else if( strNodeName.equals("HOTSPOT")) {
							if(strNodeValue.trim().equals("1") || strNodeValue.trim().toLowerCase().equals("true") || strNodeValue.trim().toLowerCase().equals("yes"))
							{
								Utils.setBooleanSetting(mContext, Global.PREF_HOTSPOT, true);
							}
							else
							{
								Utils.setBooleanSetting(mContext, Global.PREF_HOTSPOT, false);
							}
						} else if(strNodeName.equals("LoadBatch")){
							Utils.setStringSetting(mContext, Global.PREF_LOAD_BATCH, strNodeValue.trim());
						} else if(strNodeName.equals("APIRouteSch")){
							Utils.setStringSetting(mContext, Global.PREF_API_ROUTE_SCH, strNodeValue.trim());
						} else if(strNodeName.equals("HideSummaryPayment")) {
							Utils.setStringSetting(mContext, Global.PREF_HIDE_SUMMARY_PAYMENT, strNodeValue.trim());
						} else if(strNodeName.equals("UseOrdersAsLoad")) {
							Utils.setStringSetting(mContext, Global.PREF_USE_ORDERS_AS_LOAD, strNodeValue.trim());
						} else if(strNodeName.equalsIgnoreCase("LoadRequestUoM")) {
							Utils.setStringSetting(mContext, Global.PREF_LOAD_REQUEST_UOM, strNodeValue.trim());
						} else if(strNodeName.equalsIgnoreCase("AutoScanAdd")) {
							Utils.setStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD, strNodeValue.trim());
						} else if(strNodeName.equalsIgnoreCase("EnableBreaks")) {
							Utils.setStringSetting(mContext, Global.PREF_ENABLE_BREAKS, strNodeValue.trim());
						} else if(strNodeName.equalsIgnoreCase("DisableOffload")) {
							Utils.setStringSetting(mContext, Global.PREF_DISABLE_OFFLOADS, strNodeValue.trim());
						} else if(strNodeName.equalsIgnoreCase("CrashReport")) {
							Utils.setStringSetting(mContext, Global.PREF_CRASH_REPORT, strNodeValue.trim());
						} else if(strNodeName.equalsIgnoreCase("SalesDelay")) {
							if(strNodeValue.equals("1") || strNodeValue.equals("yes"))
							{
								Utils.setBooleanSetting(mContext, Global.PREF_GET_SALES_DELAY, true);
							}
							else
							{
								Utils.setBooleanSetting(mContext, Global.PREF_GET_SALES_DELAY, false);
							}
						} else if( strNodeName.equals("TRIPUSER") ) {
							Utils.setStringSetting(mContext, Global.PREF_TRIP_USER, strNodeValue);
						} else if( strNodeName.equals("EXCLUDETRIPS") ) {
							Utils.setStringSetting(mContext, Global.PREF_EXCLUDES_TRIPS, strNodeValue);
						} else if( strNodeName.equals("EndTripHide") ) {
							Utils.setStringSetting(mContext, Global.PREF_ENDTRIP_HIDE, strNodeValue);
						} else if( strNodeName.equals("OrderAPIStart") ) {
							Utils.setStringSetting(mContext, Global.PREF_ORDER_API_START, strNodeValue);
						} else if( strNodeName.equals("OrderAPIEnd") ) {
							Utils.setStringSetting(mContext, Global.PREF_ORDER_API_END, strNodeValue);
						} else if( strNodeName.equals("FRGOODPRM") ) {
							Utils.setStringSetting(mContext, Global.PREF_FRGOODPRM, strNodeValue);
						} else if( strNodeName.equals("FRGOODPLN") ) {
							Utils.setStringSetting(mContext, Global.PREF_FRGOODPLN, strNodeValue);
						} else if( strNodeName.equals("FRGOODASS") ) {
							Utils.setStringSetting(mContext, Global.PREF_FRGOODASS, strNodeValue);
						} else if( strNodeName.equals("SummaryDetail") ) {
							Utils.setStringSetting(mContext, Global.PREF_SUMMARY_DETAIL, strNodeValue);
						} else if( strNodeName.equals("DocArchiveHistoryDays") ) {
							Utils.setIntSetting(mContext, Global.PREF_DOC_ARCHIVE_HISTORY_DAYS, Utils.parseIntFromString(strNodeValue));
						} else if( strNodeName.equals("TripId") ) {
							Utils.setStringSetting(mContext, Global.PREF_TRIP_ID, strNodeValue);
						} else if( strNodeName.equals("LotAssignment") ) {
							Utils.setStringSetting(mContext, Global.PREF_LOT_ASSIGNMENT, strNodeValue.trim());
						} else if( strNodeName.equals("DisableFTP") ) {
							Utils.setStringSetting(mContext, Global.PREF_DisableFTP, strNodeValue.trim());
						} else if( strNodeName.equals("DisableInterTruck") ) {
							Utils.setStringSetting(mContext, Global.PREF_DisableInterTruck, strNodeValue.trim());
						} else if( strNodeName.equals("Unleashed") ) {
							Utils.setStringSetting(mContext, Global.PREF_UNLESHED, strNodeValue.trim());
						} else if( strNodeName.equals("TEMPREQ") ) {
							Utils.setStringSetting(mContext, Global.PREF_TEMPREQ, strNodeValue.trim());
						} else if(strNodeName.equals("UIDETLSORT")) {
							Utils.setStringSetting(mContext, Global.PREF_UI_DETL_SORT, strNodeValue.trim());
						} else if(strNodeName.equals("PrintZeroQtyPresoldItem")) {
							Utils.setStringSetting(mContext, Global.PREF_PRINT_ZERO_QTY_PRESOLD_ITEM, strNodeValue.trim());
						} else if(strNodeName.equals("TEMPLATECHAIN")) {
							Utils.setStringSetting(mContext, Global.PREF_TEMPLATE_CHAIN, strNodeValue.trim());
						} else if(strNodeName.equals("TEMPLATECUSTOMER")) {
							Utils.setStringSetting(mContext, Global.PREF_TEMPLATE_CUST, strNodeValue.trim());
						} else if(strNodeName.equals("AllowRepeatSales")) {
							Utils.setStringSetting(mContext, Global.PREF_ALLOW_REPEAT_SALES, strNodeValue.trim());
						} else if(strNodeName.equals("STKTKEGRP")) {
							Utils.setStringSetting(mContext, Global.PREF_STKTKEGRP, strNodeValue.trim());
						} else if(strNodeName.equals("STKROLGRP")) {
							Utils.setStringSetting(mContext, Global.PREF_STKROLGRP, strNodeValue.trim());
						} else if(strNodeName.equals("HoneywellRPType")) {
							Utils.setStringSetting(mContext, Global.PREF_HONEYWELL_PRINTER, strNodeValue.trim());
						} else if(strNodeName.equals("TEMPCUSTMASK")) {
							Utils.setStringSetting(mContext, Global.PREF_TEMP_CUST_MASK, strNodeValue.trim());
						} else if(strNodeName.equals("ItemCodeDisplaySource")) {
							Utils.setStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE, strNodeValue.trim());
						} else if(strNodeName.equals("LOADSRTDFT")) {
							Utils.setStringSetting(mContext, Global.PREF_LOAD_SRT_DEF, strNodeValue.trim());
						} else if(strNodeName.equals("LogRoutefic")) {
							Utils.setStringSetting(mContext, Global.PREF_ROUTEFIC_LOG, strNodeValue.trim());
						} else if(strNodeName.equals("ShowSoma")) {
							Utils.setStringSetting(mContext, Global.PREF_SHOW_SOMA, strNodeValue.trim());
						} else if(strNodeName.equals("LoadRequestEOT")) {
							Utils.setStringSetting(mContext, Global.PREF_LOAD_REQUEST_EOT, strNodeValue.trim());
						} else if(strNodeName.equals("DisallowAcctPay")) {
							Utils.setStringSetting(mContext, Global.PREF_DISALLOW_ACCOUNT_PAY, strNodeValue.trim());
						} else if(strNodeName.equals("DisableInvReprint")) {
							Utils.setStringSetting(mContext, Global.PREF_DISABLE_INV_REPRINT, strNodeValue.trim());
						} else if(strNodeName.equals("TenderFullAmount")) {
							Utils.setStringSetting(mContext, Global.PREF_TENDER_FULL_AMOUNT, strNodeValue.trim());
						} else if(strNodeName.equals("DefaultTodayScreen")) {
							Utils.setStringSetting(mContext, Global.PREF_DEFAULT_TODAY_SCREEN, strNodeValue.trim());
						} else if(strNodeName.equals("DisableNegInvAmt")) {
							Utils.setStringSetting(mContext, Global.PREF_DISABLE_NEG_INV_AMT, strNodeValue.trim());
						} else if(strNodeName.equals("DisableLoadAdjust")) {
							Utils.setStringSetting(mContext, Global.PREF_DISABLE_LOAD_ADJUST, strNodeValue.trim());
						} else if(strNodeName.equals("DisableFree")) {
							Utils.setStringSetting(mContext, Global.PREF_DISABLE_FREE, strNodeValue.trim());
						} else if(strNodeName.equals("LoadSheetIncludeRLoad")) {
							Utils.setStringSetting(mContext, Global.PREF_LOAD_SHEET_INCLUDE_RLOAD, strNodeValue.trim());
						} else if(strNodeName.equals("PrintErrorLog")) {
							Utils.setStringSetting(mContext, Global.PREF_PRINT_ERROR_LOG, strNodeValue.trim());
						} else if(strNodeName.equals("EndTripErrorLog")) {
							Utils.setStringSetting(mContext, Global.PREF_DISABLE_END_TRIP_ERROR_LOG, strNodeValue.trim());
						} else if(strNodeName.equals("UploadLog")) {
							Utils.setStringSetting(mContext, Global.PREF_DISABLE_UPLOAD_LOG, strNodeValue.trim());
						} else if(strNodeName.equals("DisableLogout")) {
							Utils.setStringSetting(mContext, Global.PREF_DISABLE_LOGOUT, strNodeValue.trim());
						} else if(strNodeName.equals("RoutectlTripDate")) {
							Utils.setStringSetting(mContext, Global.PREF_ROUTECTL_TRIP_DATE, strNodeValue.trim());
						} else if(strNodeName.equals("DisableAfterEOT")) {
							Utils.setStringSetting(mContext, Global.PREF_DISABLE_AFTER_EOT, strNodeValue.trim());
						} else if(strNodeName.equals("EOTPIN")) {
							Utils.setStringSetting(mContext, Global.PREF_EOT_PIN, strNodeValue.trim());
						} else if(strNodeName.equals("HasSuffix")) {
							Utils.setStringSetting(mContext, Global.PREF_ROUTE_NUMBER_HAS_SUFFIX, strNodeValue.trim());
						} else if(strNodeName.equals("CreditInEODCollection")) {
							Utils.setStringSetting(mContext, Global.PREF_CREDIT_IN_EOD_COLLECTION, strNodeValue.trim());
						} else if(strNodeName.equals("SeparatePercentOffPromo")) {
							Utils.setStringSetting(mContext, Global.PREF_SEPARATE_PERCENT_OFF_PROMO, strNodeValue.trim());
						} else if(strNodeName.equals("NotAtStoreRadius")) {
							Utils.setStringSetting(mContext, Global.PREF_NOT_AT_STORE_RADIUS, strNodeValue.trim());
						} else if(strNodeName.equals("NotAtStore")) {
							Utils.setStringSetting(mContext, Global.PREF_NOT_AT_STORE, strNodeValue.trim());
						} else if(strNodeName.equals("ProdSearchImageMode")) {
							Utils.setStringSetting(mContext, Global.PREF_PROD_SEARCH_IMAGE_MODE, strNodeValue.trim());
						} else if(strNodeName.equals("TreeAsImage")) {
							Utils.setStringSetting(mContext, Global.PREF_TREE_AS_IMAGE, strNodeValue.trim());
						} else if(strNodeName.equals("HideCustMaint")) {
							Utils.setStringSetting(mContext, Global.PREF_HIDE_CUST_MAINTENANCE, strNodeValue.trim());
						} else if(strNodeName.equals("DisableGPSPin")) {
							Utils.setStringSetting(mContext, Global.PREF_DISABLE_GPS_PIN, strNodeValue.trim());
						} else if(strNodeName.equals("MFASecret")) {
							Utils.setStringSetting(mContext, Global.PREF_MFA_SECRET, strNodeValue.trim());
						} else if(strNodeName.equals("CheckReturnQty")) {
							Utils.setStringSetting(mContext, Global.PREF_CHECK_RETURN_QTY, strNodeValue.trim());
						} else if(strNodeName.equals("MaxOpenDaysUnauth")) {
							Utils.setStringSetting(mContext, Global.PREF_MAX_OPEN_DAYS_UNAUTH, strNodeValue.trim());
						}
					}
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("ROUTECTL End", System.currentTimeMillis());

			// Get SALETARG.XML
			//Utils.registerLoadTime("SALETARG Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/SALETARG.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				try
				{
					fileInputStream = new FileInputStream(xmlFile);
					byte[] buf = new byte[(int)xmlFile.length()];
					fileInputStream.read(buf);
					fileInputStream.close();
					document = Utils.parseXML(builder, buf);
					element = document.getDocumentElement();
					nodeList = element.getElementsByTagName("SaleTarget");
					if (nodeList.getLength() > 0) {
						Node item = nodeList.item(0);
						NamedNodeMap Attrs = item.getAttributes();
						for (int j = 0; j < Attrs.getLength(); j++) {
							Node attr = Attrs.item(j);
							String strNodeName = attr.getNodeName();
							String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
							if (strNodeName.equals("TargetMth") == true) {
								Utils.setStringSetting(mContext, Global.SALE_TARGET_MTH, strNodeValue);
							} else if (strNodeName.equals("TargetDay") == true) {
								Utils.setStringSetting(mContext, Global.SALE_TARGET_DAY, strNodeValue);
							} else if (strNodeName.equals("ActualMTD") == true) {
								Utils.setStringSetting(mContext, Global.SALE_ACTUAL_MTD, strNodeValue);
							} else if (strNodeName.equals("SaleDaysRemaining") == true) {
								Utils.setStringSetting(mContext, Global.SALE_DAYS_REMAINING, strNodeValue);
							} else if (strNodeName.equals("SalePeriodDesc") == true) {
								Utils.setStringSetting(mContext, Global.SALE_PERIODDESC, strNodeValue);
							}
						}
					}
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			//Utils.registerLoadTime("SALETARG End", System.currentTimeMillis());

			// Get SALETARG.XML
			//Utils.registerLoadTime("SALETARG Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/TARGET.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				try
				{
					fileInputStream = new FileInputStream(xmlFile);
					byte[] buf = new byte[(int)xmlFile.length()];
					fileInputStream.read(buf);
					fileInputStream.close();
					document = Utils.parseXML(builder, buf);
					element = document.getDocumentElement();
					nodeList = element.getElementsByTagName("TARGET");
					if (nodeList.getLength() > 0) {
						Node item = nodeList.item(0);
						NamedNodeMap Attrs = item.getAttributes();
						for (int j = 0; j < Attrs.getLength(); j++) {
							Node attr = Attrs.item(j);
							String strNodeName = attr.getNodeName();
							String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
							if (strNodeName.equals("SalesMTD") == true) {
								double value;
								if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
								{
									value = Utils.getDividedDoubleValue(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
								}
								else
								{
									value = 0;
								}
								Utils.setDoubleSetting(mContext, Global.PREF_SALES_MTD, value);
							} else if (strNodeName.equals("MonthTarget") == true) {
								double value;
								if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
								{
									value = Utils.getDividedDoubleValue(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
								}
								else
								{
									value = 0;
								}
								Utils.setDoubleSetting(mContext, Global.PREF_MONTH_TARGET, value);
							} else if (strNodeName.equals("SalesWTD") == true) {
								double value;
								if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
								{
									value = Utils.getDividedDoubleValue(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
								}
								else
								{
									value = 0;
								}
								Utils.setDoubleSetting(mContext, Global.PREF_SALES_WTD, value);
							} else if (strNodeName.equals("SalesLWTD") == true) {
								double value;
								if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
								{
									value = Utils.getDividedDoubleValue(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
								}
								else
								{
									value = 0;
								}
								Utils.setDoubleSetting(mContext, Global.PREF_SALES_LWTD, value);
							}
						}
					}
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			//Utils.registerLoadTime("SALETARG End", System.currentTimeMillis());

			try
			{
				// Get CUSTNOTE.XML
				//Utils.registerLoadTime("CUSTNOTE Start", System.currentTimeMillis());
				xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/CUSTNOTE.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
				if (xmlFile.exists() == true) {

					fileInputStream = new FileInputStream(xmlFile);
					byte[] buf = new byte[(int)xmlFile.length()];
					fileInputStream.read(buf);
					fileInputStream.close();
					document = Utils.parseXML(builder, buf);
					element = document.getDocumentElement();
					nodeList = element.getElementsByTagName("CustNote");
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node item = nodeList.item(i);
						NamedNodeMap Attrs = item.getAttributes();
						CustomerNoteInfo info = new CustomerNoteInfo();
						for (int j = 0; j < Attrs.getLength(); j++) {
							Node attr = Attrs.item(j);
							String strNodeName = attr.getNodeName();
							String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
							if (strNodeName.equals("ChainNo") == true) {
								info.nChainNo = Utils.getIntFromString(strNodeValue);
							} else if (strNodeName.equals("CustNo") == true) {
								info.nCustNo = Utils.getIntFromString(strNodeValue);
							} else if (strNodeName.equals("NoteType") == true) {
								info.strNoteType = strNodeValue;
							} else if (strNodeName.equals("NoteDate") == true) {
								info.strNoteDate = strNodeValue;
							} else if (strNodeName.equals("CreatedTime") == true) {
								info.strNoteTime = strNodeValue;
							} else if (strNodeName.equals("CreatedBy") == true) {
								info.strCreatedBy = strNodeValue;
							} else if (strNodeName.equals("Note") == true) {
								info.strNote = strNodeValue;
							} else if (strNodeName.equals("NoteId") == true) {
								info.strNoteId = strNodeValue;
							} else if (strNodeName.equals("Attachment") == true) {
								info.nAttachment = Utils.getIntFromString(strNodeValue);
							} else if(strNodeName.equals("CreatedTimeAndNoteDate") == true) {
								info.strCreatedTimeAndNoteDate = strNodeValue;
							}
						}
						dbAdapter.insertCustomerNoteInfo(info);
					}
				}
				//Utils.registerLoadTime("CUSTNOTE End", System.currentTimeMillis());
			}catch (Exception e) {
				e.printStackTrace();
			}

			try
			{
				// Get EQUIPMNT.XML
				//Utils.registerLoadTime("EQUIPMNT Start", System.currentTimeMillis());
				xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/EQUIPMNT.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
				if (xmlFile.exists() == true) {

					fileInputStream = new FileInputStream(xmlFile);
					byte[] buf = new byte[(int)xmlFile.length()];
					fileInputStream.read(buf);
					fileInputStream.close();
					document = Utils.parseXML(builder, buf);
					element = document.getDocumentElement();
					nodeList = element.getElementsByTagName("Equipmnt");
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node item = nodeList.item(i);
						NamedNodeMap Attrs = item.getAttributes();
						EquipmentInfo info = new EquipmentInfo();
						for (int j = 0; j < Attrs.getLength(); j++) {
							Node attr = Attrs.item(j);
							String strNodeName = attr.getNodeName();
							String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
							if (strNodeName.equals("EquipmentNo") == true) {
								info.strEquipmentNo = strNodeValue;
							} else if (strNodeName.equals("SerialNo") == true) {
								info.strSerialNo = strNodeValue;
							} else if (strNodeName.equals("Make") == true) {
								info.strMake = strNodeValue;
							} else if (strNodeName.equals("Model") == true) {
								info.strModel = strNodeValue;
							} else if (strNodeName.equals("Description") == true) {
								info.strDescription = strNodeValue;
							} else if (strNodeName.equals("AssetType") == true) {
								info.strAssetType = strNodeValue;
							} else if (strNodeName.equals("StatusCode") == true) {
								info.strStatusCode = strNodeValue;
							} else if (strNodeName.equals("EquipmentType") == true) {
								info.strEquipmentType = strNodeValue;
							} else if (strNodeName.equals("Application") == true) {
								info.strApplication = strNodeValue;
							} else if (strNodeName.equals("DollarCapacity") == true) {
								info.strDollarCapacity = strNodeValue;
							} else if (strNodeName.equals("LastRepair") == true) {
								info.strLastRepair = strNodeValue;
							} else if (strNodeName.equals("TotalSales") == true) {
								info.nTotalSales = Utils.getIntFromString(strNodeValue);
							} else if (strNodeName.equals("TotalRent") == true) {
								info.nTotalRent = Utils.getIntFromString(strNodeValue);
							} else if (strNodeName.equals("AltEquipment") == true) {
								info.strAltEquipment = strNodeValue;
							}
						}
						dbAdapter.insertEquipment(info);
					}
				}
				//Utils.registerLoadTime("EQUIPMNT End", System.currentTimeMillis());
			}catch (Exception e)
			{
				e.printStackTrace();
			}

			try
			{
				// Get EQUIPASS.XML
				//Utils.registerLoadTime("EQUIPASS Start", System.currentTimeMillis());
				xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/EQUIPASS.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
				if (xmlFile.exists() == true) {
					fileInputStream = new FileInputStream(xmlFile);
					byte[] buf = new byte[(int)xmlFile.length()];
					fileInputStream.read(buf);
					fileInputStream.close();
					document = Utils.parseXML(builder, buf);
					element = document.getDocumentElement();
					nodeList = element.getElementsByTagName("EquipAss");
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node item = nodeList.item(i);
						NamedNodeMap Attrs = item.getAttributes();
						EquipAssInfo info = new EquipAssInfo();
						for (int j = 0; j < Attrs.getLength(); j++) {
							Node attr = Attrs.item(j);
							String strNodeName = attr.getNodeName();
							String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
							if (strNodeName.equals("ChainNo") == true) {
								info.nChainNo = Utils.getIntFromString(strNodeValue);
							} else if (strNodeName.equals("CustNo") == true) {
								info.nCustNo = Utils.getIntFromString(strNodeValue);
							} else if (strNodeName.equals("EquipmentNo") == true) {
								info.strEquipmentNo = strNodeValue;
							} else if (strNodeName.equals("Installed") == true) {
								info.strInstalled = strNodeValue;
							} else if (strNodeName.equals("AgreementNo") == true) {
								info.strAgreementNo = strNodeValue;
							} else if (strNodeName.equals("AgreementDate") == true) {
								info.strAgreementDate = strNodeValue;
							} else if (strNodeName.equals("BillType") == true) {
								info.strBillType = strNodeValue;
							} else if (strNodeName.equals("BillFreq") == true) {
								info.strBillFreq = strNodeValue;
							} else if (strNodeName.equals("PayRentAmt") == true) {
								info.nPayRentAmt = Utils.getIntFromString(strNodeValue);
							} else if (strNodeName.equals("VerifyFlag") == true) {
								info.strVerifyFlag = strNodeValue;
							} else if (strNodeName.equals("Verified") == true) {
								info.strVerifiedDate = strNodeValue;
								//info.strVerified = "";
							} else if (strNodeName.equals("Features") == true) {
								info.strFeatures = strNodeValue;
							}
						}
						dbAdapter.insertEquipAss(info);
					}
				}
				//Utils.registerLoadTime("EQUIPASS End", System.currentTimeMillis());
			}catch (Exception e)
			{
				e.printStackTrace();
			}

			DownloadEngine.initEngine();
			DownloadEngine.getEngine().init();

			// Get PRODDETL.XML
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/PRODDETL.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			//Utils.registerLoadTime("PRODDETL Start", System.currentTimeMillis());
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("ProdDetl");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					ProdDetlInfo info = new ProdDetlInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("ItemNo") == true) {
							info.strItemNo = strNodeValue;
						} else if (strNodeName.equals("Description") == true) {
							info.strDescription = strNodeValue;
						} else if (strNodeName.equals("ShortDesc") == true) {
							info.strShortDesc = strNodeValue;
						} else if (strNodeName.equals("ItemUPC") == true) {
							info.strItemUPC = strNodeValue;
						} else if (strNodeName.equals("ItemUPC20") == true) {
							info.strItemUPC20 = strNodeValue;
						} else if (strNodeName.equals("Weight") == true) {
							info.nWeight = Utils.getFloatFromString(strNodeValue);
						} else if (strNodeName.equals("ItemType") == true) {
							info.strItemType = strNodeValue;
						} else if (strNodeName.equals("ProdGrp") == true || strNodeName.equals("ProdGroup") == true) {
							info.strProdGrp = strNodeValue.trim();
						} else if (strNodeName.equals("ProdLine") == true) {
							info.strProdLine = strNodeValue;
						} else if (strNodeName.equals("PackSize") == true) {
							info.strPackSize = strNodeValue;
						} else if (strNodeName.equals("ShelfLife") == true) {
							info.nShelfLife = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("Brand") == true) {
							info.strBrand = strNodeValue;
						} else if (strNodeName.equals("SubBrand") == true) {
							info.strSubBrand = strNodeValue;
						} else if (strNodeName.equals("Type") == true) {
							info.strType = strNodeValue;
						} else if (strNodeName.equals("ActiveDate") == true) {
							info.strActiveDate = strNodeValue;
						} else if (strNodeName.equals("DumpsAllowed") == true) {
							info.strDumpsAllowed = strNodeValue;
						} else if (strNodeName.equals("SalesAllowed") == true) {
							info.strSalesAllowed = strNodeValue;
						} else if (strNodeName.equals("ItemInactiveSwitch") == true) {
							info.strItemInactiveSwitch = strNodeValue;
						} else if (strNodeName.equals("GroupFilterKey") == true) {
							info.strGroupFilterKey = strNodeValue;
						} else if (strNodeName.equals("ServiceFee") == true) {
							info.strServiceFee = strNodeValue;
						} else if (strNodeName.equals("MaxQty") == true){
							info.nMaxQty = Utils.getFloatFromString(strNodeValue);
						} else if (strNodeName.equals("ConsumerUnit") == true) {
							info.nConsumerUnit = Utils.parseIntFromString(strNodeValue);
						} else if(strNodeName.equals("MarketGroup") || strNodeName.equals("MarketGrp")) {
							info.strMarketGrp = strNodeValue.trim();
						} else if(strNodeName.equals("PromoGroup") || strNodeName.equals("PromoGrp")) {
							info.strPromoGrp = strNodeValue.trim();
						} else if (strNodeName.equals("ImageURL") == true) {
							info.strImageUrl = strNodeValue.trim();
						} else if (strNodeName.equals("LotControl") == true) {
							info.strLotControl = strNodeValue.trim();
						}
					}
					if(!TextUtils.isEmpty(info.strImageUrl))
					{
						DownloadEngine.getEngine().addDownloadData(info.strItemNo, info.strImageUrl);
					}
					dbAdapter.insertProdDetlInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("PRODDETL End", System.currentTimeMillis());

			// Get LOADHEAD.XML
			//Utils.registerLoadTime("LOADHEAD Start", System.currentTimeMillis());
			Map<String, Boolean> availableDetailMap = new HashMap<>();
			Map<String, Boolean> rRecordMap = new HashMap<>();
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/LOADHEAD.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("LoadHead");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					LoadHeadInfo info = new LoadHeadInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("DetailFile") == true) {
							info.strDetailFile = strNodeValue;
						} else if (strNodeName.equals("LoadDate") == true) {
							info.strLoadDate = strNodeValue;
						} else if (strNodeName.equals("DayNo") == true) {
							info.nDayNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("DeliveryDate") == true) {
							info.strDeliveryDate = strNodeValue;
						} else if (strNodeName.equals("PeriodNo") == true) {
							info.nPeriodNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("RouteNumber") == true) {
							info.strRouteNumber = strNodeValue;
						} else if (strNodeName.equals("Description") == true) {
							info.strDescription = strNodeValue;
						} else if (strNodeName.equals("Type") == true) {
							info.strType = strNodeValue;
						} else if (strNodeName.equals("TripNumber") == true) {
							info.strTripNumber = strNodeValue;
						} else if(strNodeName.equals("Bay")) {
							info.strBay = strNodeValue.trim();
						} else if(strNodeName.equals("User1")){
							info.strUser1 = strNodeValue.trim();
						} else if(strNodeName.equals("User2")){
							info.strUser2 = strNodeValue.trim();
						}
					}
					if( !dbAdapter.isExistLoadHeadInfo(info.strDetailFile, info.strLoadDate, info.strDeliveryDate, info.nPeriodNo, info.strRouteNumber, info.strDescription, info.strType) )
					{
						availableDetailMap.put(info.strDetailFile, true);
						if(Utils.getStringSetting(mContext, Global.PREF_IGNORERLOAD).equals("Y"))
						{
							if(info.strType.equals("R"))
							{
								rRecordMap.put(info.strDetailFile, true);
							}
							else
							{
								dbAdapter.insertLoadHeadInfo(info);
							}
						}
						else
						{
							dbAdapter.insertLoadHeadInfo(info);
						}
					}
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("LOADHEAD End", System.currentTimeMillis());

			// Get LOADDETL.XML
			//Utils.registerLoadTime("LOADDETL Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/LOADDETL.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("LoadDetl");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					LoadDetlInfo info = new LoadDetlInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("DetailFile") == true) {
							info.strDetailFile = strNodeValue;
						} else if (strNodeName.equals("ItemNo") == true) {
							info.strItemNo = strNodeValue;
						} else if (strNodeName.equals("Qty") == true) {
							if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
							{
								info.nQty = Utils.getDividedValue(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
							}
							else
							{
								info.nQty = 0;
							}
						} else if (strNodeName.equals("Weight") == true){
							if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
							{
								info.dQty = Utils.getRoundedValue(Utils.getDividedDoubleValue(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING))));
							}
							else
							{
								info.dQty = 0;
							}
						} else if (strNodeName.equals("PickLocationCode") == true) {
							info.strPickLocationCode = strNodeValue;
						}
					}
					Boolean bInsertable = availableDetailMap.get(info.strDetailFile);
					if(  bInsertable!= null && bInsertable == true)
					{
						if(Utils.getStringSetting(mContext, Global.PREF_IGNORERLOAD).equals("Y"))
						{
							if(rRecordMap.get(info.strDetailFile) == null)
							{
								dbAdapter.insertLoadDetlInfo(info);
							}
						}
						else
						{
							dbAdapter.insertLoadDetlInfo(info);
						}
					}
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("LOADDETL End", System.currentTimeMillis());

			//Get LOADBLOT.XML file
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/LOADBLOT.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("LoadBlot");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					LoadBlotInfo info = new LoadBlotInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("DetailFile") == true) {
							info.strDetailFile = strNodeValue;
						} else if (strNodeName.equals("ItemNo") == true) {
							info.strItemNo = strNodeValue;
						} else if (strNodeName.equals("LotNumber") == true) {
							info.strLotNumber = strNodeValue;
						} else if (strNodeName.equals("LotQty") == true) {
							if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
							{
								info.nLotQty = Utils.getDividedValue(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
							}
							else
							{
								info.nLotQty = 0;
							}
						} else if (strNodeName.equals("LotWeight") == true) {
							if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
							{
								info.fLotWeight = Utils.getDividedValue(Utils.getIntFromString(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING))));
							}
							else
							{
								info.fLotWeight = 0;
							}
						} else if (strNodeName.equals("BatchDate") == true) {
							info.strBatchDate = strNodeValue;
						}
					}
					dbAdapter.insertLoadBlot(info);
				}
				fileInputStream.close();
			}

			// Get MESSDIST.XML
			//Utils.registerLoadTime("MESSDIST Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/MESSDIST.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("MessDist");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					MessageInfo info = new MessageInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("MessageID") == true) {
							info.nMessageID = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("CustNo") == true) {
							info.nCustNo = Utils.getIntFromString(strNodeValue);
						}
					}
					dbAdapter.insertMessageInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("MESSDIST End", System.currentTimeMillis());

			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/PRODINFO.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("ProdInfo");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					ProdInfo info = new ProdInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("InfoDetail") == true) {
							info.strInfoDetail = strNodeValue;
						} else if (strNodeName.equals("InfoType") == true) {
							info.strInfoType = strNodeValue;
						} else if (strNodeName.equals("ItemNo") == true) {
							info.strItemNo = strNodeValue;
						}
					}
					dbAdapter.insertProdInfo(info);
				}
				fileInputStream.close();
			}

			// Get MESSHEAD.XML
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/MESSHEAD.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			//Utils.registerLoadTime("MESSHEAD Start", System.currentTimeMillis());
			if (xmlFile.exists() == true) {
				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("MessHead");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					MessageInfo info = new MessageInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("MessageID") == true) {
							info.nMessageID = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("Subject") == true) {
							info.strSubject = strNodeValue;
						} else if (strNodeName.equals("MessageText") == true) {
							info.strMessageText = strNodeValue;
						}
					}
					dbAdapter.updateMessageInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("MESSHEAD End", System.currentTimeMillis());

			// Get PRODSTRU.XML
			//Utils.registerLoadTime("PRODSTRU Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/PRODSTRU.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("ProdStru");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					ProdStruInfo info = new ProdStruInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("EntryID") == true) {
							info.strEntryID = strNodeValue.trim();
						} else if (strNodeName.equals("Description") == true) {
							info.strDescription = strNodeValue;
						} else if (strNodeName.equals("ParentID") == true) {
							info.strParentID = strNodeValue.trim();
						} else if (strNodeName.equals("ObjectType") == true) {
							info.strObjectType = strNodeValue;
						} else if (strNodeName.equals("Reference") == true) {
							info.strReference = strNodeValue;
						} else if (strNodeName.equals("GroupNo") == true) {
							info.nGroupNo = Utils.getIntFromString(strNodeValue);
						}
					}
					dbAdapter.insertProdStruInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("PRODSTRU End", System.currentTimeMillis());

			// Get DESCTYPE.XML
			//Utils.registerLoadTime("DESCTYPE Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/DESCTYPE.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("DescType");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					DescTypeInfo info = new DescTypeInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("DescriptionEntryKey") == true) {
							info.strDescriptionEntryKey = strNodeValue;
						} else if (strNodeName.equals("SystemDescriptionTypeYN") == true) {
							info.nSystemDescriptionTypeYN = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("DescriptionTypeID") == true) {
							info.strDescriptionTypeID = strNodeValue;
						} else if (strNodeName.equals("NumericCodeYN") == true) {
							info.nNumericCodeYN = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("NumericKey") == true) {
							info.nNumericKey = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("AlphaKey") == true) {
							info.strAlphaKey = strNodeValue;
						} else if (strNodeName.equals("Description") == true) {
							info.strDescription = strNodeValue;
						} else if (strNodeName.equals("AlternateSeq") == true) {
							info.nAlternateSeq = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("Value1") == true) {
							info.strValue1 = strNodeValue;
						} else if (strNodeName.equals("Value2") == true) {
							info.strValue2 = strNodeValue;
						} else if (strNodeName.equals("Value3") == true) {
							info.strValue3 = strNodeValue;
						} else if (strNodeName.equals("Value4") == true) {
							info.strValue4 = strNodeValue;
						} else if (strNodeName.equals("Value5") == true) {
							info.strValue5 = strNodeValue;
						} else if (strNodeName.equals("Value6") == true) {
							info.strValue6 = strNodeValue;
						} else if (strNodeName.equals("Ownership") == true) {
							info.strOwnership = strNodeValue;
						}
					}
					dbAdapter.insertDescTypeInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("DESCTYPE End", System.currentTimeMillis());

			// Get PRODLOCN.XML
			//Utils.registerLoadTime("PRODLOCN Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/PRODLOCN.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("ProdLocn");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					ProdLocnInfo info = new ProdLocnInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("LocnNo") == true) {
							info.nLocnNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("ItemNo") == true) {
							info.strItemNo = strNodeValue;
						} else if (strNodeName.equals("SCWType") == true) {
							info.nSCWType = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("BasePrice") == true) {
							if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
							{
								info.nBasePrice = Utils.parseLongFromString(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
							}
							else
							{
								info.nBasePrice = 0;
							}
						} else if (strNodeName.equals("DriverPrice") == true) {
							if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
							{
								info.nDriverPrice = Utils.parseLongFromString(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
							}
							else
							{
								info.nDriverPrice = 0;
							}
						} else if (strNodeName.equals("CaseFactor") == true) {
							info.nCaseFactor = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("RetailPrice") == true) {
							if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
							{
								info.nRetailPrice = Utils.getIntFromString(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
							}
							else
							{
								info.nRetailPrice = 0;
							}
						} else if (strNodeName.equals("ReturnPrice") == true) {
							if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
							{
								info.nReturnPrice = Utils.getIntFromString(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
							}
							else
							{
								info.nReturnPrice = 0;
							}
						} else if (strNodeName.equals("TaxFlag") == true) {
							info.strTaxFlag = strNodeValue;
						} else if (strNodeName.equals("ItemTaxCode") == true) {
							info.strItemTaxCode = strNodeValue;
						} else if (strNodeName.equals("FullCase") == true) {
							info.strFullCase = strNodeValue;
						} else if (strNodeName.equals("PalletFactor") == true) {
							info.nPalletFactor = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("StackFactor") == true) {
							info.nStackFactor = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("CaseUOM") == true) {
							info.strCaseUOM = strNodeValue;
						} else if (strNodeName.equals("UnitUOM") == true) {
							info.strUnitUOM = strNodeValue;
						} else if (strNodeName.equals("WeightType") == true) {
							info.strWeightType = strNodeValue;
						} else if (strNodeName.equals("CustDiscountValid") == true) {
							info.nCustDiscountValid = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("DiscInLieuItem") == true) {
							info.strDiscInLieuItem = strNodeValue;
						} else if (strNodeName.equals("OrderSeq") == true) {
							info.nOrderSeq = Utils.getIntFromString(strNodeValue);
						}
						
					}
					dbAdapter.insertProdLocnInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("PRODLOCN End", System.currentTimeMillis());

			// Get SURVDIST.XML
			//Utils.registerLoadTime("SURVDIST Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/SURVDIST.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {
				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("SurvDist");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					SurveyInfo info = new SurveyInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("ChainNo") == true) {
							info.nChainNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("CustNo") == true) {
							info.nCustNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("SurveyID") == true) {
							info.strSurveyID = strNodeValue.trim();
						} else if (strNodeName.equals("SurveyHeaderId") == true) {
							info.strSurveyID = strNodeValue.trim();
						}
					}
					dbAdapter.insertSurveyInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("SURVDIST End", System.currentTimeMillis());

			// Get SURVHEAD.XML
			//Utils.registerLoadTime("SURVHEAD Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/SURVHEAD.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("SurvHead");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					SurveyInfo info = new SurveyInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("SurveyID") == true) {
							info.strSurveyID = strNodeValue.trim();
							info.bFromSurveyID = true;
						}
						else if (strNodeName.equals("SurveyHeaderId") == true) {
							info.strSurveyID = strNodeValue.trim();
							info.bFromSurveyID = false;
						}
						else if (strNodeName.equals("SurveyTtl") == true) {
							info.strSurveyTitle = strNodeValue;
						} else if (strNodeName.equals("CreateDate") == true) {
							info.strCreateDate = strNodeValue;
						} else if (strNodeName.equals("CompletionDate") == true) {
							info.strCompletionDate = strNodeValue;
						} else if (strNodeName.equals("SurveyResponse") == true) {
							info.strSurveyResponse = strNodeValue;
						} else if (strNodeName.equals("Mandatory") == true) {
							info.strMandatory = strNodeValue;
						} else if (strNodeName.equals("SurveyType") == true) {
							info.nSurveyType = Utils.getIntFromString(strNodeValue);
						}
					}
					dbAdapter.updateSurveyInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("SURVHEAD End", System.currentTimeMillis());

			//Because of DSDVS - Van Sales (7.8.4) task, We will configure new format SurDist & SurvHead
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/SURVDIST.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {
				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("SurvDist");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					SurvDistInfo info = new SurvDistInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("ChainNo") == true) {
							info.nChainNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("CustNo") == true) {
							info.nCustNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("SurveyID") == true) {
							info.strSurveyHeaderId = strNodeValue;
						} else if (strNodeName.equals("SurveyHeaderId") == true) {
							info.strSurveyHeaderId = strNodeValue;
						}
					}
					dbAdapter.insertSurveyDistInfo(info);
				}
				fileInputStream.close();
			}
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/SURVHEAD.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("SurvHead");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					SurvHeadInfo info = new SurvHeadInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).trim();
						if (strNodeName.equals("SurveyID") == true) {
							info.strSurveyHeaderId = strNodeValue.trim();
							info.bFromSurveyID = true;
						}
						else if (strNodeName.equals("SurveyHeaderId") == true) {
							info.strSurveyHeaderId = strNodeValue.trim();
							info.bFromSurveyID = false;
						}
						else if (strNodeName.equals("SurveyTtl") == true) {
							info.strSurveyTtl = strNodeValue;
						} else if (strNodeName.equals("CreateDate") == true) {
							info.strCreateDate = strNodeValue;
						} else if (strNodeName.equals("CompletionDate") == true) {
							info.strCompletionDate = strNodeValue;
						} else if (strNodeName.equals("SurveyResponse") == true) {
							info.strSurveyResponse = strNodeValue;
						} else if (strNodeName.equals("Mandatory") == true) {
							info.strMandatory = strNodeValue;
						} else if (strNodeName.equals("SurveyType") == true) {
							info.nSurveyType = Utils.getIntFromString(strNodeValue);
						}
					}
					dbAdapter.insertSurveyHeadInfo(info);
				}
				fileInputStream.close();
			}
			//

			// Get SURVQUES.XML
			//Utils.registerLoadTime("SURVQUES Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/SURVQUES.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("SurvQues");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					SurvQuesInfo info = new SurvQuesInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("SurveyID") == true) {
							info.strSurveyID = strNodeValue.trim();
						}
						else if (strNodeName.equals("SurveyHeaderId") == true) {
							info.strSurveyID = strNodeValue.trim();
						}
						else if (strNodeName.equals("QuestionID") == true) {
							info.strQuestionID = strNodeValue;
						}
						else if (strNodeName.equals("LookupId") == true) {
							info.strQuestionID = strNodeValue;
						}
						else if (strNodeName.equals("QuestionText") == true) {
							info.strQuestionText = strNodeValue;
						} else if (strNodeName.equals("QuestionType") == true) {
							info.strQuestionType = strNodeValue;
						} else if (strNodeName.equals("LookupNo") == true) {
							info.nLookupNo = Utils.getIntFromString(strNodeValue);
						} else if(strNodeName.equals("Mandatory") == true) {
							info.strMandatory = strNodeValue;
						} else if(strNodeName.equals("MultiResp") == true) {
							info.strMultiResp = strNodeValue;
						} else if(strNodeName.equals("ImageCaptureReq") == true) {
							info.bImageCaptureReq = strNodeValue.trim().equals("Y");
						} else if(strNodeName.equals("QuestionNumberYes") == true) {
							info.nQuestionNumberYes = Utils.getIntFromString(strNodeValue);
						} else if(strNodeName.equals("QuestionNumberNo") == true) {
							info.nQuestionNumberNo = Utils.getIntFromString(strNodeValue);
						}
					}
					dbAdapter.insertSurvQuesInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("SURVQUES End", System.currentTimeMillis());

			// Get SURVQOPT.XML
			//Utils.registerLoadTime("SURVQOPT Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/SURVQOPT.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("SurvQOpt");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					SurvQoptInfo info = new SurvQoptInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("LookupNo") == true) {
							info.nLookupNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("QuestionVal") == true) {
							info.strQuestionVal = strNodeValue;
						} else if (strNodeName.equals("LookupId") == true) {
							info.strLookupId = strNodeValue;
						} else if (strNodeName.equals("BranchQuestion") == true) {
							info.nBranchQuestion = Utils.getIntFromString(strNodeValue);
						}
					}
					dbAdapter.insertSurvQoptInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("SURVQOPT End", System.currentTimeMillis());

			// Get ROUTESCH.XML
			//Utils.registerLoadTime("ROUTESCH Start", System.currentTimeMillis());
			Map<Integer, Boolean> dayNoMap = new HashMap<>();

			List<CustomerInfo> routeSchList = new ArrayList<>();
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/ROUTESCH.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {
				Global.gUInvenHinfo = null;

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("RouteSch");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					CustomerInfo info = new CustomerInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("RepNo") == true) {
							info.nRouteNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("DayNo") == true) {
							info.nDayNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("SeqNo") == true) {
							info.nSeqNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("ChainNo") == true) {
							info.nChainNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("CustNo") == true) {
							info.nCustNo = Utils.getIntFromString(strNodeValue);
							Utils.setBooleanSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_CUSTOMER_GET_GPS, info.nCustNo), false);
						} else if (strNodeName.equals("PeriodNo") == true) {
							info.nPeriodNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("DeliveryDate") == true) {
							info.strDeliveryDate = strNodeValue;
						} else if (strNodeName.equals("StartTime1") == true) {
							info.strStartTime1 = getFormattedTime(strNodeValue);
							info.strArrivalTime = info.strStartTime1;
						} else if (strNodeName.equals("EndTime1") == true) {
							info.strEndTime1 = getFormattedTime(strNodeValue);
						} else if (strNodeName.equals("StartTime2") == true) {
							info.strStartTime2 = getFormattedTime(strNodeValue);
						} else if (strNodeName.equals("EndTime2") == true) {
							info.strEndTime2 = getFormattedTime(strNodeValue);
						} else if (strNodeName.equals("DeliveryType") == true){
							info.strDeliveryType = strNodeValue;
						}
					}
					if( info.nDayNo != 0 && dayNoMap.get(info.nDayNo) == null)
					{
						dayNoMap.put(info.nDayNo, true);
					}
					routeSchList.add(info);
					dbAdapter.insertCustomerInfo(info);
				}
				fileInputStream.close();
			}

			//Insert DayNo Table
			for (Map.Entry<Integer, Boolean> entry : dayNoMap.entrySet()) {
				dbAdapter.insertDayNo(entry.getKey());
			}
			//Utils.registerLoadTime("ROUTESCH End", System.currentTimeMillis());

			// Get CUSTDETL.XML
			//Utils.registerLoadTime("CUSTDETL Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/CUSTDETL.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();

				nodeList = element.getElementsByTagName("Default");
				int i = 0;
				for (i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					CustomerInfo info = Global.gDefaultCustomerInfo;
					UCustomerFMInfo uInfo = Global.gDefaultUCustomerFMInfo;
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("CustNo") == true) {
							info.nCustNo = Utils.getIntFromString(strNodeValue);
							uInfo.nCustNo = Utils.getIntFromString(strNodeValue);
						}else if (strNodeName.equals("AltCustNo") == true) {
							info.strAltCustNo = strNodeValue;
						} else if (strNodeName.equals("Name") == true) {
							info.strName = strNodeValue;
							uInfo.strName = strNodeValue;
						} else if (strNodeName.equals("TaxID") == true) {
							info.strCompanyTaxID = strNodeValue;
							uInfo.strCompanyTaxID = strNodeValue;
						} else if (strNodeName.equals("City") == true) {
							info.strCity = strNodeValue;
							uInfo.strCity = strNodeValue;
						} else if (strNodeName.equals("Address1") == true) {
							info.strAddress1 = strNodeValue;
							uInfo.strAddress1 = strNodeValue;
						} else if (strNodeName.equals("Address2") == true) {
							info.strAddress2 = strNodeValue;
							uInfo.strAddress2 = strNodeValue;
						} else if (strNodeName.equals("Zip") == true) {
							info.strZip = strNodeValue;
						} else if (strNodeName.equals("Phone") == true) {
							info.strPhone = strNodeValue;
							uInfo.strPhone = strNodeValue;
						} else if (strNodeName.equals("Location") == true) {
							info.nLocation = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("RouteNo") == true) {
							info.nRouteNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("Priority") == true) {
							info.strPriority = strNodeValue;
						} else if (strNodeName.equals("ChainNo") == true) {
							info.nChainNo = Utils.getIntFromString(strNodeValue);
							uInfo.nChainNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("OEContact") == true) {
							info.strBuyerName = strNodeValue;
						} else if (strNodeName.equals("Phone") == true) {
							info.strBuyerPhone = strNodeValue;
						} else if (strNodeName.equals("ARContact") == true) {
							info.strAccountName = strNodeValue;
						} else if (strNodeName.equals("ARPhone") == true) {
							info.strAccountPhone = strNodeValue;
						} else if (strNodeName.equals("PromoPlan") == true) {
							info.nPromoPlan = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("TemplateGrp") == true) {
							info.nTemplateGrp = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("TempGrp") == true) {
							info.nTempGrp = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("FeatGrp") == true || strNodeName.equals("FeaturedGrp") == true) {
							info.nFeatGrp = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("PriceGrp") == true) {
							info.nPriceGrp = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("AltPriceGrp") == true) {
							info.nAltPriceGrp = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("TaxCode") == true) {
							info.strTaxCode = strNodeValue;
						} else if (strNodeName.equals("ShipToState") == true) {
							info.strShipToState = strNodeValue;
							uInfo.strShipToState = strNodeValue;
						} else if (strNodeName.equals("ShipToZip") == true) {
							info.strShipToZip = strNodeValue;
							uInfo.strShipToZip = strNodeValue;
						} else if (strNodeName.equals("InvoiceFmt") == true) {
							info.nInvoiceFmt = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("CustPriceMaintFlag") == true) {
							info.nCustPriceMaintFlag = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("ShowPrice") == true) {
  							info.nShowPrice = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("DelayedPrint") == true) {
							info.nDelayedPrint = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("PayType") == true) {
							info.strPayType = strNodeValue;
						} else if (strNodeName.equals("BillName") == true) {
							info.strBillName = strNodeValue;
						} else if (strNodeName.equals("BillAddr1") == true) {
							info.strBillAddr1 = strNodeValue;
						} else if (strNodeName.equals("BillAddr2") == true) {
							info.strBillAddr2 = strNodeValue;
						} else if (strNodeName.equals("BillCity") == true) {
							info.strBillCity = strNodeValue;
						} else if (strNodeName.equals("BillPeriod") == true) {
							info.strBillPeriod = strNodeValue;
						} else if (strNodeName.equals("BillToChainNo") == true) {
							info.nBillChainNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("BillToCustNo") == true) {
							info.nBillCustNo = Utils.getIntFromString(strNodeValue);
						}else if (strNodeName.equals("VendorNo") == true) {
							info.strVendorNo = strNodeValue;
						}else if (strNodeName.equals("State") == true) {
							info.strState = strNodeValue;
						} else if (strNodeName.equals("Signature") == true) {
							info.strSignature = strNodeValue;
						} else if (strNodeName.equals("Terms") == true){
							info.strTerms = strNodeValue;
						} else if (strNodeName.equals("SalEntryMode") == true){
							info.strSalEntryMode = strNodeValue;
						} else if (strNodeName.equals("RtnEntryMode") == true){
							info.strRtnEntryMode = strNodeValue;
						} else if (strNodeName.equals("DumpsAllowed") == true){
							info.strDumpsAllowed = strNodeValue;
						} else if (strNodeName.equals("ImageCapture") == true){
							info.nImageCapture = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("DEXVersion") == true){
							info.strDexVersion = strNodeValue;
						} else if(strNodeName.equals("AuthGrp") == true){
							info.strAuthGrp = strNodeValue;
						} else if(strNodeName.equals("DriverLatitude") == true){
							info.driverLatitude = Utils.getDoubleFromString(strNodeValue);
						} else if(strNodeName.equals("DriverLongitude") == true){
							info.driverLongitude = Utils.getDoubleFromString(strNodeValue);
						} else if(strNodeName.equals("DeliveryWindowFrom") == true) {
							info.strDeliveryWindowFrom = strNodeValue;
						} else if(strNodeName.equals("DeliveryWindowTo") == true) {
							info.strDeliveryWindowTo = strNodeValue;
						} else if(strNodeName.equals("CreditHold") == true){
							int nValue = Utils.getIntFromString(strNodeValue);
							info.nCreditHold = nValue;
						} else if(strNodeName.equals("DeliveryFreq") == true){
							int nValue = Utils.getIntFromString(strNodeValue);
							info.nDeliveryFreq = nValue;
						} else if(strNodeName.equals("StoreNo") == true) {
							info.strStoreNo = strNodeValue;
						} else if(strNodeName.equals("SalesDistrict") == true){
							info.strSalesDistrict = strNodeValue;
						} else if(strNodeName.equals("MinOrderVal")){
							info.fMiniumOrderValue = Utils.getFloatFromString(strNodeValue) / 100.0f;
						} else if(strNodeName.equals("MinOrderChg")){
							info.fHandlingFee = Utils.getFloatFromString(strNodeValue) / 100.0f;
						} else if(strNodeName.equals("ConSales")){
							info.nConSales = Utils.getIntFromString(strNodeValue);
						} else if(strNodeName.equals("Type")){
							info.strType = strNodeValue;
						} else if(strNodeName.equals("UserCode1")){
							info.strUser1 = strNodeValue;
						} else if(strNodeName.equals("InvoiceDueMethod")){
							info.strInvoiceDueMethod = strNodeValue.trim();
						} else if(strNodeName.equals("InvoiceDueDate")){
							info.strInvoiceDueDate = strNodeValue.trim();
						} else if(strNodeName.equals("PONumberRequired")){
							info.strPONumberRequired = strNodeValue.trim();
						} else if(strNodeName.equals("PONumberMask")){
							info.strPONumberMask = strNodeValue.trim();
						} else if(strNodeName.equals("LegalName")){
							info.strLegalName = strNodeValue.trim();
						} else if(strNodeName.equals("CreditLimit")){
							info.strCreditLimit = strNodeValue.trim();
						}
					}
				}
				nodeList = element.getElementsByTagName("CustDetl");
				int nRouteSchCutomerCount = 0;
				for (i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					CustomerInfo info = new CustomerInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("CustNo") == true) {
							info.nCustNo = Utils.getIntFromString(strNodeValue);
						}else if (strNodeName.equals("AltCustNo") == true) {
							info.strAltCustNo = strNodeValue;
						} else if (strNodeName.equals("Name") == true) {
							info.strName = strNodeValue;
						} else if (strNodeName.equals("TaxID") == true) {
							info.strCompanyTaxID = strNodeValue;
						} else if (strNodeName.equals("City") == true) {
							info.strCity = strNodeValue;
						} else if (strNodeName.equals("Address1") == true) {
							info.strAddress1 = strNodeValue;
						} else if (strNodeName.equals("Address2") == true) {
							info.strAddress2 = strNodeValue;
						} else if (strNodeName.equals("Zip") == true) {
							info.strZip = strNodeValue;
						} else if (strNodeName.equals("Phone") == true) {
							info.strPhone = strNodeValue;
						} else if (strNodeName.equals("Location") == true) {
							info.nLocation = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("RouteNo") == true) {
							info.nRouteNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("Priority") == true) {
							info.strPriority = strNodeValue;
						} else if (strNodeName.equals("ChainNo") == true) {
							info.nChainNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("OEContact") == true) {
							info.strBuyerName = strNodeValue;
						} else if (strNodeName.equals("Phone") == true) {
							info.strBuyerPhone = strNodeValue;
						} else if (strNodeName.equals("ARContact") == true) {
							info.strAccountName = strNodeValue;
						} else if (strNodeName.equals("ARPhone") == true) {
							info.strAccountPhone = strNodeValue;
						} else if (strNodeName.equals("PromoPlan") == true) {
							info.nPromoPlan = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("TemplateGrp") == true) {
							info.nTemplateGrp = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("TempGrp") == true) {
							info.nTempGrp = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("FeatGrp") == true || strNodeName.equals("FeaturedGrp") == true) {
							info.nFeatGrp = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("PriceGrp") == true) {
							info.nPriceGrp = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("AltPriceGrp") == true) {
							info.nAltPriceGrp = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("TaxCode") == true) {
							info.strTaxCode = strNodeValue;
						} else if (strNodeName.equals("ShipToState") == true) {
							info.strShipToState = strNodeValue;
						} else if (strNodeName.equals("ShipToZip") == true) {
							info.strShipToZip = strNodeValue;
						} else if (strNodeName.equals("InvoiceFmt") == true) {
							info.nInvoiceFmt = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("CustPriceMaintFlag") == true) {
							info.nCustPriceMaintFlag = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("ShowPrice") == true) {
							info.nShowPrice = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("DelayedPrint") == true) {
							info.nDelayedPrint = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("PayType") == true) {
							info.strPayType = strNodeValue;
						} else if (strNodeName.equals("BillName") == true) {
							info.strBillName = strNodeValue;
						} else if (strNodeName.equals("BillAddr1") == true) {
							info.strBillAddr1 = strNodeValue;
						} else if (strNodeName.equals("BillAddr2") == true) {
							info.strBillAddr2 = strNodeValue;
						} else if (strNodeName.equals("BillCity") == true) {
							info.strBillCity = strNodeValue;
						} else if (strNodeName.equals("BillPeriod") == true) {
							info.strBillPeriod = strNodeValue;
						} else if (strNodeName.equals("BillToChainNo") == true) {
							info.nBillChainNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("BillToCustNo") == true) {
							info.nBillCustNo = Utils.getIntFromString(strNodeValue);
						}else if (strNodeName.equals("VendorNo") == true) {
							info.strVendorNo = strNodeValue;
						} else if (strNodeName.equals("State") == true) {
							info.strState = strNodeValue;
						}  else if (strNodeName.equals("Signature") == true) {
							info.strSignature = strNodeValue;
						} else if (strNodeName.equals("Terms") == true){
							info.strTerms = strNodeValue;
						} else if (strNodeName.equals("SalEntryMode") == true){
							info.strSalEntryMode = strNodeValue;
						} else if (strNodeName.equals("RtnEntryMode") == true){
							info.strRtnEntryMode = strNodeValue;
						} else if (strNodeName.equals("DumpsAllowed") == true){
							info.strDumpsAllowed = strNodeValue;
						} else if (strNodeName.equals("ImageCapture") == true){
							if(strNodeValue.equals("true"))
							{
								info.nImageCapture = 1;
							}
							else if(strNodeValue.equals("false") || TextUtils.isEmpty(strNodeValue))
							{
								info.nImageCapture = 0;
							}
							else
							{
								info.nImageCapture = Utils.getIntFromString(strNodeValue);
							}
						} else if (strNodeName.equals("DEXVersion") == true){
							info.strDexVersion = strNodeValue;
						} else if(strNodeName.equals("AuthGrp") == true){
							info.strAuthGrp = strNodeValue;
						} else if(strNodeName.equals("DriverLatitude") == true){
							if(!TextUtils.isEmpty(strNodeValue))
							{
								info.driverLatitude = Utils.getDoubleFromString(strNodeValue);
							}
						} else if(strNodeName.equals("DriverLongitude") == true){
							if(!TextUtils.isEmpty(strNodeValue))
							{
								info.driverLongitude = Utils.getDoubleFromString(strNodeValue);
							}
						} else if(strNodeName.equals("DeliveryWindowFrom") == true) {
							info.strDeliveryWindowFrom = strNodeValue;
						} else if(strNodeName.equals("DeliveryWindowTo") == true) {
							info.strDeliveryWindowTo = strNodeValue;
						} else if(strNodeName.equals("CreditHold") == true){
							int nValue = Utils.getIntFromString(strNodeValue);
							info.nCreditHold = nValue;
						} else if(strNodeName.equals("DeliveryFreq") == true){
							int nValue = Utils.getIntFromString(strNodeValue);
							info.nDeliveryFreq = nValue;
						} else if(strNodeName.equals("StoreNo") == true) {
							info.strStoreNo = strNodeValue;
						} else if(strNodeName.equals("SalesDistrict") == true){
							info.strSalesDistrict = strNodeValue;
						} else if(strNodeName.equals("MinOrderVal")){
							info.fMiniumOrderValue = Utils.getFloatFromString(strNodeValue) / 100.0f;
						} else if(strNodeName.equals("MinOrderChg")){
							info.fHandlingFee = Utils.getFloatFromString(strNodeValue) / 100.0f;
						} else if(strNodeName.equals("ConSales")){
							info.nConSales = Utils.getIntFromString(strNodeValue);
						} else if(strNodeName.equals("Type")){
							info.strType = strNodeValue;
						} else if(strNodeName.equals("UserCode1")){
							info.strUser1 = strNodeValue;
						} else if(strNodeName.equals("InvoiceDueMethod")){
							info.strInvoiceDueMethod = strNodeValue.trim();
						} else if(strNodeName.equals("InvoiceDueDate")){
							info.strInvoiceDueDate = strNodeValue.trim();
						} else if(strNodeName.equals("PONumberRequired")){
							info.strPONumberRequired = strNodeValue.trim();
						}  else if(strNodeName.equals("PONumberMask")){
							info.strPONumberMask = strNodeValue.trim();
						} else if(strNodeName.equals("LegalName")){
							info.strLegalName = strNodeValue.trim();
						} else if(strNodeName.equals("CreditLimit")){
							info.strCreditLimit = strNodeValue.trim();
						}
					}
					info.bIsRouteSch = false;
					for (CustomerInfo routeSchInfo : routeSchList) {
						if (info.nCustNo == routeSchInfo.nCustNo && info.nChainNo == routeSchInfo.nChainNo) {
							info.bIsRouteSch = true;
							break;
						}
					}
					if (info.bIsRouteSch == true) {
						info.nOrgSeqNo = nRouteSchCutomerCount;
						dbAdapter.updateCustomerInfo(info);
						nRouteSchCutomerCount++;
					} else {
						dbAdapter.insertCustomerInfo(info);
					}
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("CUSTDETL End", System.currentTimeMillis());

			// Get CUSTGPS.XML
			//Utils.registerLoadTime("CUSTGPS Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/CUSTGPS.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("CustGPS");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					CustomerInfo info = new CustomerInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("ChainNo") == true) {
							info.nChainNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("CustNo") == true) {
							info.nCustNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("Latitude") == true) {
							info.latitude = Utils.getDoubleFromString(strNodeValue);
						} else if (strNodeName.equals("Longitude") == true) {
							info.longitude = Utils.getDoubleFromString(strNodeValue);
						}
					}
					dbAdapter.updateCustomerInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("CUSTGPS End", System.currentTimeMillis());

			// Get CUSTINFO.XML
			//Utils.registerLoadTime("CUSTINFO Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/CUSTINFO.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("CustInfo");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					CustomerAddInfo info = new CustomerAddInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("ChainNo") == true) {
							info.nChainNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("CustNo") == true) {
							info.nCustNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("InfoType") == true) {
							info.strInfoType = strNodeValue.trim();
						} else if (strNodeName.equals("Info") == true) {
							info.strInfo = strNodeValue.trim();
						}
					}
					dbAdapter.insertCustAddInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("CUSTINFO End", System.currentTimeMillis());

			// Get CUSCONTACT.XML
			//Utils.registerLoadTime("CUSCONTACT Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/CUSCONTACT.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				
				nodeList = element.getElementsByTagName("Default");
				int i = 0;
				for (i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					CustContactInfo info = Global.gDefaultContactInfo;
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("ChainNo") == true) {
							info.nChainNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("CustNo") == true) {
							info.nCustNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("ContactType") == true) {
							info.nContactType = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("ContactTypeDesc") == true) {
							info.strContactTypeDesc = strNodeValue;
						} else if (strNodeName.equals("ContactName") == true) {
							info.strContactName = strNodeValue;
						} else if (strNodeName.equals("ContactPhoneNumber") == true) {
							info.strContactPhoneNumber = strNodeValue;
						} else if (strNodeName.equals("ContactEmailAddress") == true) {
							info.strContactEmailAddress = strNodeValue;
						} else if (strNodeName.equals("ContactInfo") == true) {
							info.strContactInfo = strNodeValue;
						}
					}
				}
				
				nodeList = element.getElementsByTagName("CustContact");
				for (i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					CustContactInfo info = new CustContactInfo();
					for (int j = 0; j < Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("ChainNo") == true) {
							info.nChainNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("CustNo") == true) {
							info.nCustNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("ContactType") == true) {
							info.nContactType = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("ContactTypeDesc") == true) {
							info.strContactTypeDesc = strNodeValue;
						} else if (strNodeName.equals("ContactName") == true) {
							info.strContactName = strNodeValue;
						} else if (strNodeName.equals("ContactPhoneNumber") == true) {
							info.strContactPhoneNumber = strNodeValue;
						} else if (strNodeName.equals("ContactEmailAddress") == true) {
							info.strContactEmailAddress = strNodeValue;
						} else if (strNodeName.equals("ContactInfo") == true) {
							info.strContactInfo = strNodeValue;
						}
					}
					dbAdapter.insertCustContactInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("CUSCONTACT End", System.currentTimeMillis());

			// Get PRESOLDH.XML
			//Utils.registerLoadTime("PRESOLDH Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/PRESOLDH.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("PresoldOrh");
				for (int i=0; i<nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					PresoldOrHInfo info = new PresoldOrHInfo();
					for (int j=0; j<Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("ChainNo") == true) {
							info.nChainNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("CustNo") == true) {
							info.nCustNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("DayNo") == true) {
							info.nDayNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("DetailFile") == true) {
							info.strDetailFile = strNodeValue;
						} else if (strNodeName.equals("OrderNo") == true) {
								info.strOrderNo = strNodeValue;
						} else if (strNodeName.equals("Type") == true) {
							info.strType = strNodeValue;
						} else if (strNodeName.equals("PORef") == true) {
							info.strPORef = strNodeValue;
						} else if (strNodeName.equals("CredRef") == true) {
							info.strCredRef = strNodeValue;
						} else if (strNodeName.equals("Instrs") == true) {
							info.strInstrs = strNodeValue;
						} else if( strNodeName.equals("User1") == true ){
							info.strUser1 = strNodeValue;
						} else if (strNodeName.equals("User2") == true) {
							if( strNodeValue.length() == 0 )
								info.nQty = 0;
							else
								info.nQty = Utils.getIntFromString(strNodeValue);
							info.strUser2 = strNodeValue;
						} else if( strNodeName.equals("Modifiable") == true ){
							info.strModifiable = strNodeValue;
						} else if(strNodeName.equals("DeliveryDate")){
							info.strDeliveryDate = strNodeName;
						} else if( strNodeName.equals("PeriodNo")){
							info.nPeriodNo = Utils.getIntFromString(strNodeValue);
						} else if( strNodeName.equals("DeliverySequence")){
							info.nDeliverySequence = Utils.getIntFromString(strNodeValue);
						} else if( strNodeName.equals("OrderBarcode")){
							info.strOrderBarCode = strNodeValue;
						}
					}
					dbAdapter.insertPresoldOrHInfo(info);

					/*
					if(!dbAdapter.isExistCustomerInfo(info.nCustNo, info.nChainNo, info.nPeriodNo, info.nDeliverySequence))
					{
						CustomerInfo custInfo = dbAdapter.getCustomerInfo(info.nCustNo, info.nChainNo);
						if( custInfo == null )
						{
							custInfo = dbAdapter.getCustomerInfo(info.nCustNo);
						}
						if( custInfo == null )
						{
							custInfo = new CustomerInfo();
						}
						custInfo.nCustNo = info.nCustNo;
						custInfo.nChainNo = info.nChainNo;
						custInfo.nPeriodNo = info.nPeriodNo;
						custInfo.nSeqNo = info.nDeliverySequence;
						custInfo.nIdx = 0;
						dbAdapter.insertCustomerInfo(custInfo);
					}*/
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("PRESOLDH End", System.currentTimeMillis());

			// Get PRESOLDD.XML
			//Utils.registerLoadTime("PRESOLDD Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/PRESOLDD.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("PresoldOrd");
				for (int i=0; i<nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					PresoldOrDInfo info = new PresoldOrDInfo();
					for (int j=0; j<Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("DetailFile") == true) {
							info.strDetailFile = strNodeValue;
						} else if (strNodeName.equals("ItemNo") == true) {
							info.strItemNo = strNodeValue;
						} else if (strNodeName.equals("QtyOption") == true) {
							info.nQtyOption = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("OrderQty") == true) {
							if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
							{
								info.nOrderQty = Utils.getDividedValue(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
								info.dOrderQty = Utils.getDividedValue(Utils.getIntFromString(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING))));
							}
							else
							{
								info.nOrderQty = 0;
								info.dOrderQty = 0;
							}
						} else if (strNodeName.equals("PickLocationCode") == true) {
							info.strPickLocationCode = strNodeValue;
						} else if (strNodeName.equals("Price") == true) {
							if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
							{
								info.nPrice = Utils.getIntFromString(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
							}
							else
							{
								info.nPrice = 0;
							}
						} else if (strNodeName.equals("TrxnType") == true) {
							info.strTrxnType = strNodeValue;
						} else if (strNodeName.equals("ReasonCode") == true) {
							info.nReasonCode = Utils.getIntFromString(strNodeValue);
						}
					}
					dbAdapter.insertPresoldOrDInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("PRESOLDD End", System.currentTimeMillis());

			// Get PRICING.XML
			//Utils.registerLoadTime("PRICING Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/PRICING.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("Pricing");
				for (int i=0; i<nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					PricingInfo info = new PricingInfo();
					for (int j=0; j<Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("CustNo") == true) {
							info.nCustNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("ItemNo") == true) {
							info.strItemNo = strNodeValue;
						} else if (strNodeName.equals("DateStart") == true) {
							info.strDateStart = strNodeValue;
						} else if (strNodeName.equals("DateEnd") == true) {
							info.strDateEnd = strNodeValue;
						} else if (strNodeName.equals("Price") == true) {
							if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
							{
								info.nPrice = Utils.getIntFromString(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
							}
							else
							{
								info.nPrice = 0;
							}
						} else if (strNodeName.equals("PrcFilRecord") == true) {
							info.strPrcFilRecord = strNodeValue;
						} else if (strNodeName.equals("PriceType") == true) {
							info.nPriceType = Utils.getIntFromString(strNodeValue);
						}
					}
					dbAdapter.insertPricingInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("PRICING End", System.currentTimeMillis());

			// Get PROMNOVO.XML
			//Utils.registerLoadTime("PROMNOVO Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/PROMNOVO.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("PromNoVo");
				for (int i=0; i<nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					PromNovoInfo info = new PromNovoInfo();
					for (int j=0; j<Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("AssignNo") == true) {
							info.nAssignNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("ItemNo") == true) {
							info.strItemNo = strNodeValue;
						} else if (strNodeName.equals("FreeItemNo") == true) {
							info.strFreeItemNo = strNodeValue;
						}else if (strNodeName.equals("PromoDiscount") == true) {
							if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
							{
								info.nPromoDiscount = Utils.getIntFromString(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
							}
							else
							{
								info.nPromoDiscount = 0;
							}
						}
					}
					dbAdapter.insertPromNovoInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("PROMNOVO End", System.currentTimeMillis());

			// Get PRICEGRP.XML
			//Utils.registerLoadTime("PRICEGRP Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/PRICEGRP.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("PriceGrp");
				for (int i=0; i<nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					PriceGrpInfo info = new PriceGrpInfo();
					for (int j=0; j<Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("PriceGrp") == true) {
							info.nPriceGrp = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("ItemNo") == true) {
							info.strItemNo = strNodeValue;
						} else if (strNodeName.equals("DateStart") == true) {
							info.strDateStart = strNodeValue;
						} else if (strNodeName.equals("DateEnd") == true) {
							info.strDateEnd = strNodeValue;
						} else if (strNodeName.equals("Price") == true) {
							if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
							{
								info.nPrice = Utils.getIntFromString(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
							}
							else
							{
								info.nPrice = 0;
							}

						} else if (strNodeName.equals("PrcFilRecord") == true) {
							info.strPrcFilRecord = strNodeValue;
						} else if (strNodeName.equals("PriceType") == true) {
							info.nPriceType = Utils.getIntFromString(strNodeValue);
						}
					}
					dbAdapter.insertPriceGrpInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("PRICEGRP End", System.currentTimeMillis());

			// Get PROMASS.XML
			//Utils.registerLoadTime("PROMASS Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/PROMASS.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("PromAss");
				for (int i=0; i<nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					PromAssInfo info = new PromAssInfo();
					for (int j=0; j<Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("AssignNo") == true) {
							info.nAssignNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("PromoAppMethod") == true) {
							info.nPromoAppMethod = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("PromoType") == true) {
							info.nPromoType = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("PromoMethod") == true) {
							info.nPromoMethod = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("Description") == true) {
							info.strDescription = strNodeValue;
						}
					}
					dbAdapter.insertPromAssInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("PROMASS End", System.currentTimeMillis());

			// Get PROMHEAD.XML
			//Utils.registerLoadTime("PROMHEAD Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/PROMHEAD.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("PromHead");
				for (int i=0; i<nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					PromHeadInfo info = new PromHeadInfo();
					for (int j=0; j<Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("PlanNo") == true) {
							info.nPlanNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("AssignNo") == true) {
							info.nAssignNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("DateStart") == true) {
							info.strDateStart = strNodeValue;
						} else if (strNodeName.equals("DateEnd") == true) {
							info.strDateEnd = strNodeValue;
						} else if (strNodeName.equals("Description") == true) {
							info.strDescription = strNodeValue;
						}
					}
					dbAdapter.insertPromHeadInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("PROMHEAD End", System.currentTimeMillis());

			// Get PROMULTI.XML
			//Utils.registerLoadTime("PROMULTI Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/PROMULTI.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("ProMulti");
				for (int i=0; i<nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					ProMultiInfo info = new ProMultiInfo();
					for (int j=0; j<Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("ChainNo") == true) {
							info.nChainNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("CustNo") == true) {
							info.nCustNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("PlanNo") == true) {
							info.nPlanNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("DateStart") == true) {
							info.strDateStart = strNodeValue;
						} else if (strNodeName.equals("DateEnd") == true) {
							info.strDateEnd = strNodeValue;
						} else if (strNodeName.equals("SeqNo") == true) {
							info.nSeqNo = Utils.getIntFromString(strNodeValue);
						}
					}
					dbAdapter.insertProMultiInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("PROMULTI End", System.currentTimeMillis());

			// Get TAXCODES.XML
			//Utils.registerLoadTime("TAXCODES Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/TAXCODES.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("TaxCodes");
				for (int i=0; i<nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					TaxCodesInfo info = new TaxCodesInfo();
					for (int j=0; j<Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("CustTaxCode") == true) {
							info.strCustTaxCode = strNodeValue;
						} else if (strNodeName.equals("ItemTaxCode") == true) {
							info.strItemTaxCode = strNodeValue;
						} else if (strNodeName.equals("TaxRateCode") == true) {
							info.strTaxRateCode = strNodeValue;
						} else if (strNodeName.equals("CumulativeFlag") == true) {
							info.strCumulativeFlag = strNodeValue;
						} else if (strNodeName.equals("TaxSeq") == true) {
							info.nTaxSeq = Utils.getIntFromString(strNodeValue);
						}
					}
					dbAdapter.insertTaxCodesInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("TAXCODES End", System.currentTimeMillis());

			// Get TAXRATES.XML
			//Utils.registerLoadTime("TAXRATES Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/TAXRATES.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("TaxRates");
				for (int i=0; i<nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					TaxRatesInfo info = new TaxRatesInfo();
					for (int j=0; j<Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("TaxRateCode") == true) {
							info.strTaxRateCode = strNodeValue;
						} else if (strNodeName.equals("StartDate") == true) {
							info.strStartDate = strNodeValue;
						} else if (strNodeName.equals("EndDate") == true) {
							info.strEndDate = strNodeValue;
						} else if (strNodeName.equals("TaxRate") == true) {
							if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
							{
								info.nTaxRate = Utils.getIntFromString(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
							}
							else
							{
								info.nTaxRate = 0;
							}
						} else if (strNodeName.equals("RateDescription") == true) {
							info.strRateDescription = strNodeValue;
						} else if (strNodeName.equals("TaxMethod") == true) {
							info.nTaxMethod = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("TaxRateProductCode") == true) {
							info.strTaxRateNumber = strNodeValue;
						} else if (strNodeName.equals("TaxType") == true) {
							info.strTaxType = strNodeValue;
						}
					}
					dbAdapter.insertTaxRatesInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("TAXRATES End", System.currentTimeMillis());

			//Utils.registerLoadTime("CMPYCONTACT Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/CMPYCONTACT.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("CmpyContact");
				for (int i=0; i<nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					CmpyContactInfo info = new CmpyContactInfo();
					for (int j=0; j<Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("CompanyNumber") == true) {
							info.nCompanyNumber = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("CompanyName") == true) {
							info.strCompanyName = strNodeValue;
						} else if (strNodeName.equals("CompanyWeb") == true) {
							info.strCompanyWeb = strNodeValue;
						} else if (strNodeName.equals("CompanyEmail") == true) {
							info.strCompanyEmail = strNodeValue;
						} else if (strNodeName.equals("CompanyPhone1") == true) {
							info.strCompanyPhone1 = strNodeValue;
						} else if (strNodeName.equals("CompanyPhone2") == true) {
							info.strCompanyPhone2 = strNodeValue;
						} else if (strNodeName.equals("CompanyInfo") == true) {
							info.strCompanyInfo = strNodeValue;
						} 
					}
					dbAdapter.insertCmpyContactInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("CMPYCONTACT End", System.currentTimeMillis());
			

			// Get DOCTEXT.XML
			//Utils.registerLoadTime("DOCTEXT Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/DOCTEXT.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("DocText");
				for (int i=0; i<nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					DocTextInfo info = new DocTextInfo();
					for (int j=0; j<Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("TextType") == true) {
							info.nTextType = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("Line") == true) {
							info.nLine = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("DocText") == true) {
							info.strDocText = strNodeValue;
						} else if ( strNodeName.equals("ChainNo") == true) {
							info.nChainNo = Utils.getIntFromString(strNodeValue);
						} else if ( strNodeName.equals("CustNo") == true) {
							info.nCustNo = Utils.getIntFromString(strNodeValue);
						} else if ( strNodeName.equals("SubType") == true) {
							info.strSubType = strNodeValue;
						}
					}
					dbAdapter.insertDocTextInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("DOCTEXT End", System.currentTimeMillis());

			try
			{
				if( bInit )
				{
					// Get TRIPUSER.XML
					//Utils.registerLoadTime("TRIPUSER Start", System.currentTimeMillis());
					xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/TRIPUSER.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
					if (xmlFile.exists() == true) {
						fileInputStream = new FileInputStream(xmlFile);
						byte[] buf = new byte[(int)xmlFile.length()];
						fileInputStream.read(buf);
						fileInputStream.close();
						document = Utils.parseXML(builder, buf);
						element = document.getDocumentElement();
						nodeList = element.getElementsByTagName("TripUser");
						for (int i = 0; i < nodeList.getLength(); i++) {
							Node item = nodeList.item(i);
							NamedNodeMap Attrs = item.getAttributes();
							TripUser info = new TripUser();
							for (int j = 0; j < Attrs.getLength(); j++) {
								Node attr = Attrs.item(j);
								String strNodeName = attr.getNodeName();
								String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
								if (strNodeName.equals("RouteNumber") == true) {
									info.strRouteNumber = strNodeValue;
								} else if (strNodeName.equals("Description") == true) {
									info.strDescription = strNodeValue;
								} else if (strNodeName.equals("Security1") == true) {
									info.strSecurity = strNodeValue;
								} else if (strNodeName.equals("UserName") == true) {
									info.strUserName = strNodeValue;
								} else if (strNodeName.equals("Trip") == true) {
									info.strTrip = strNodeValue;
								}
							}
							dbAdapter.insertTripUser(info);
						}
					}
					//Utils.registerLoadTime("TRIPUSER End", System.currentTimeMillis());
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}

			//Get ARHEADER.XML
			//Utils.registerLoadTime("ARHEADER Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/ARHEADER.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("ARHeader");
				for (int i=0; i<nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					ARHeaderInfo info = new ARHeaderInfo();
					for (int j=0; j<Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("ChainNo") == true) {
							info.nChainNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("CustNo") == true) {
							info.nCustNo = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("InvDate") == true) {
							info.strInvDate = strNodeValue;
						} else if (strNodeName.equals("InvNo") == true) {
							info.strInvNo = strNodeValue;
						} else if (strNodeName.equals("ARTrxnType") == true) {
							info.strARTrxnType = strNodeValue;
						} else if (strNodeName.equals("TrxnAmount") == true) {
							info.nTrxnAmount = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("DiscAmount") == true) {
							info.nDiscAmount = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("DiscStartDate") == true) {
							info.strDiscStartDate = strNodeValue;
						} else if (strNodeName.equals("DiscEndDate") == true) {
							info.strDiscEndDate = strNodeValue;
						} else if (strNodeName.equals("User1") == true) {
							info.strUser1 = strNodeValue;
						} else if (strNodeName.equals("User2") == true) {
							info.strUser2 = strNodeValue;
						} else if (strNodeName.equals("Description") == true) {
							info.strDescription = strNodeValue;
						}
					}
					dbAdapter.insertARHeaderInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("ARHEADER End", System.currentTimeMillis());

			//Get AUTHHEAD.XML
			//Utils.registerLoadTime("AUTHHEAD Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/AUTHHEAD.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("AuthHead");
				for (int i=0; i<nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					AuthHeaderInfo info = new AuthHeaderInfo();
					for (int j=0; j<Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("AuthGrp") == true) {
							info.nAuthGrp = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("AuthType") == true) {
							info.strAuthType = strNodeValue;
						}
					}
					dbAdapter.insertAuthHeaderInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("AUTHHEAD End", System.currentTimeMillis());

			//Get AUTHDETL.XML
			//Utils.registerLoadTime("AUTHDETL Start", System.currentTimeMillis());
			xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/AUTHDETL.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
			if (xmlFile.exists() == true) {

				fileInputStream = new FileInputStream(xmlFile);
				byte[] buf = new byte[(int)xmlFile.length()];
				fileInputStream.read(buf);
				fileInputStream.close();
				document = Utils.parseXML(builder, buf);

				element = document.getDocumentElement();
				nodeList = element.getElementsByTagName("AuthDetl");
				for (int i=0; i<nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					NamedNodeMap Attrs = item.getAttributes();
					AuthDetInfo info = new AuthDetInfo();
					for (int j=0; j<Attrs.getLength(); j++) {
						Node attr = Attrs.item(j);
						String strNodeName = attr.getNodeName();
						String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
						if (strNodeName.equals("AuthGrp") == true) {
							info.nAuthGrp = Utils.getIntFromString(strNodeValue);
						} else if (strNodeName.equals("ItemNo") == true) {
							info.strItemNo = strNodeValue;
						} else if (strNodeName.equals("StartDate") == true) {
							info.strStartDate = strNodeValue;
						} else if (strNodeName.equals("EndDate") == true) {
							info.strEndDate = strNodeValue;
						}
					}
					dbAdapter.insertAuthDetlInfo(info);
				}
				fileInputStream.close();
			}
			//Utils.registerLoadTime("AUTHDETL End", System.currentTimeMillis());

			try
			{
				// Get CUSCONBAL.XML
				//Utils.registerLoadTime("CUSCONBAL Start", System.currentTimeMillis());
				xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/CUSCONBAL.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
				if (xmlFile.exists() == true) {
					fileInputStream = new FileInputStream(xmlFile);
					byte[] buf = new byte[(int)xmlFile.length()];
					fileInputStream.read(buf);
					fileInputStream.close();
					document = Utils.parseXML(builder, buf);
					element = document.getDocumentElement();
					nodeList = element.getElementsByTagName("CusConBal");
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node item = nodeList.item(i);
						NamedNodeMap Attrs = item.getAttributes();
						CustConBalInfo info = new CustConBalInfo();
						for (int j = 0; j < Attrs.getLength(); j++) {
							Node attr = Attrs.item(j);
							String strNodeName = attr.getNodeName();
							String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
							if (strNodeName.equals("ChainNo") == true) {
								info.nChainNo = Utils.getIntFromString(strNodeValue);
							} else if (strNodeName.equals("CustNo") == true) {
								info.nCustNo = Utils.getIntFromString(strNodeValue);
							} else if (strNodeName.equals("ItemNo") == true) {
								info.strItemNo = strNodeValue;
							} else if (strNodeName.equals("Qty") == true) {
								if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
								{
									info.nQty = Utils.getDividedValue(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
								}
								else
								{
									info.nQty = 0;
								}
							} else if (strNodeName.equals("Weight") == true){
								if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
								{
									info.fWeight = Utils.getRoundedValue(Utils.getDividedDoubleValue(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING))));
								}
								else
								{
									info.fWeight = 0;
								}
							}
						}
						dbAdapter.insertCustConBalInfo(info);
					}
				}
				//Utils.registerLoadTime("CUSCONBAL End", System.currentTimeMillis());
			}catch (Exception e)
			{
				e.printStackTrace();
			}

			//Get ORDHIST.XML
			try
			{
				//Utils.registerLoadTime("ORDHIST Start", System.currentTimeMillis());
				xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/ORDHIST.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
				if (xmlFile.exists() == true) {
					fileInputStream = new FileInputStream(xmlFile);
					byte[] buf = new byte[(int)xmlFile.length()];
					fileInputStream.read(buf);
					fileInputStream.close();
					document = Utils.parseXML(builder, buf);
					element = document.getDocumentElement();
					nodeList = element.getElementsByTagName("OrdHist");
					for (int i=0; i<nodeList.getLength(); i++) {
						Node item = nodeList.item(i);
						NamedNodeMap Attrs = item.getAttributes();
						int nCustNo = 0;
						String strItemNo = "";
						Map<Integer, OrderHistoryInfo> hisMap = new HashMap<>();
						for (int j=0; j<Attrs.getLength(); j++) {
							Node attr = Attrs.item(j);
							String strNodeName = attr.getNodeName();
							String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
							if (strNodeName.equals("CustNo") == true) {
								nCustNo = Utils.getIntFromString(strNodeValue);
							} else if (strNodeName.equals("ItemNo") == true) {
								strItemNo = strNodeValue;
							} else if (strNodeName.startsWith("Date") == true) {
								int nIdx = Utils.getIntFromString(strNodeName.substring("Date".length()));
								OrderHistoryInfo info = hisMap.get(nIdx);
								if( info == null )
								{
									info = new OrderHistoryInfo();
									hisMap.put(nIdx, info);
								}
								info.strOrderDate = strNodeValue;
							} else if (strNodeName.startsWith("SAQty") == true) {
								int nIdx = Utils.getIntFromString(strNodeName.substring("SAQty".length()));
								OrderHistoryInfo info = hisMap.get(nIdx);
								if( info == null )
								{
									info = new OrderHistoryInfo();
									hisMap.put(nIdx, info);
								}
								info.nSAQty = Utils.getIntFromString(strNodeValue);
							} else if (strNodeName.startsWith("DUQty") == true) {
								int nIdx = Utils.getIntFromString(strNodeName.substring("DUQty".length()));
								OrderHistoryInfo info = hisMap.get(nIdx);
								if( info == null )
								{
									info = new OrderHistoryInfo();
									hisMap.put(nIdx, info);
								}
								info.nDUQty = Utils.getIntFromString(strNodeValue);
							} else if (strNodeName.startsWith("BBQty") == true) {
								int nIdx = Utils.getIntFromString(strNodeName.substring("BBQty".length()));
								OrderHistoryInfo info = hisMap.get(nIdx);
								if( info == null )
								{
									info = new OrderHistoryInfo();
									hisMap.put(nIdx, info);
								}
								info.nBBQty = Utils.getIntFromString(strNodeValue);
							}
						}

						for (Map.Entry<Integer, OrderHistoryInfo> entry : hisMap.entrySet())
						{
							OrderHistoryInfo info = entry.getValue();
							info.strItemNo = strItemNo;
							info.nCustNo = nCustNo;
							dbAdapter.insertOrderHistoryInfo(info);
						}
					}
				}
				//Utils.registerLoadTime("ORDHIST End", System.currentTimeMillis());

			}catch ( Exception e)
			{
				e.printStackTrace();
			}

			try
			{
				// Get LITERALS.XML
				xmlFile = new File(String.format(Locale.ENGLISH, "%s%s/LITERALS.XML", Global.DOWNLOAD_DIR, Global.gTripNumber));
				if (xmlFile.exists() == true) {
					fileInputStream = new FileInputStream(xmlFile);
					byte[] buf = new byte[(int)xmlFile.length()];
					fileInputStream.read(buf);
					fileInputStream.close();
					document = Utils.parseXML(builder, buf);
					element = document.getDocumentElement();
					nodeList = element.getElementsByTagName("LITERAL");
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node item = nodeList.item(i);
						NamedNodeMap Attrs = item.getAttributes();
						LoadHeadInfo info = new LoadHeadInfo();
						String strKey = "";
						String strValue = "";
						String strLang = "";

						for (int j = 0; j < Attrs.getLength(); j++) {
							Node attr = Attrs.item(j);
							String strNodeName = attr.getNodeName();
							String strNodeValue = Utils.getHTMLValue(attr.getNodeValue()).toString().trim();
							if (strNodeName.trim().toLowerCase().equals("key") == true) {
								strKey = strNodeValue;
							} else if (strNodeName.trim().toLowerCase().equals("value") == true) {
								strValue = strNodeValue;
							} else if (strNodeName.trim().toLowerCase().equals("lang") == true) {
								strLang = strNodeValue;
							}
						}
						if(!TextUtils.isEmpty(strKey) && !TextUtils.isEmpty(strValue) && !TextUtils.isEmpty(strLang))
						{
							dbAdapter.insertLangInfo(strKey, strValue, strLang);
						}
					}
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			//Utils.registerLoadTime("Order Processing Start", System.currentTimeMillis());
			if(bInit)
			{
				List<CustomerInfo> allCustomerList = dbAdapter.getDeferredCustomerList(true);
				Map<String, List<CustomerInfo>> overMap = new HashMap<>();
				for(CustomerInfo info : allCustomerList)
				{
					String key = info.nChainNo + "&&" + info.nCustNo +
							"&&" + info.nPeriodNo + "&&" + info.nSeqNo;
					List<CustomerInfo> customerInfoList = overMap.get(key);
					if(customerInfoList == null)
					{
						customerInfoList = new ArrayList<>();
						overMap.put(key, customerInfoList);
					}
					customerInfoList.add(info);
				}

				Iterator it = overMap.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pair = (Map.Entry) it.next();
					List<CustomerInfo> infoList = (List<CustomerInfo>) pair.getValue();
					it.remove(); // avoids a ConcurrentModificationException

					List<PresoldOrHInfo> presoldOrHInfoList = dbAdapter.getPresoldOrHInfo(infoList.get(0).nChainNo, infoList.get(0).nCustNo, infoList.get(0).nPeriodNo, infoList.get(0).nSeqNo);
					if (presoldOrHInfoList == null || presoldOrHInfoList.size() == 0) {
						continue;
					} else if (presoldOrHInfoList.size() <= infoList.size()) {
						for (int i = 0; i < presoldOrHInfoList.size(); i++) {
							presoldOrHInfoList.get(i).nCustIdx = infoList.get(i).nIdx;
							dbAdapter.updatePresoldOrHCustomerIdx(presoldOrHInfoList.get(i));
						}

					} else if (presoldOrHInfoList.size() > infoList.size()) {
						for (int i = 0; i < infoList.size(); i++) {
							presoldOrHInfoList.get(i).nCustIdx = infoList.get(i).nIdx;
							dbAdapter.updatePresoldOrHCustomerIdx(presoldOrHInfoList.get(i));
						}
						for (int i = infoList.size(); i < presoldOrHInfoList.size(); i++) {
							presoldOrHInfoList.get(i).nCustIdx = infoList.get(infoList.size() - 1).nIdx;
							dbAdapter.updatePresoldOrHCustomerIdx(presoldOrHInfoList.get(i));
						}
					}
				}
			}

			//dbAdapter.endTransaction();

			//For Driver Customer
			//dbAdapter.beginTransacion();
			if(bInit)
			{
				int nCustNo = Utils.getIntFromString(Utils.getStringSetting(mContext, Global.PREF_ROUTE_NUMBER));
				List<PresoldOrHInfo> presoldOrHInfoList = dbAdapter.getPresoldOrHInfo(Global.DRIVER_CUSTOMER_CHAINNO, nCustNo);
				for(PresoldOrHInfo presoldOrHInfo : presoldOrHInfoList)
				{
					if(presoldOrHInfo != null && presoldOrHInfo.strType != null && (presoldOrHInfo.strType.equals("B")
							|| presoldOrHInfo.strType.equals("C")))
					{
						List<PresoldOrDInfo> infos = dbAdapter.getPresoldOrDList(presoldOrHInfo.strDetailFile);
						if( infos != null && infos.size() > 0 )
						{
							for(PresoldOrDInfo info : infos)
							{
								if(info.nQtyOption == 2)
								{
									info.nSuggestedQty = info.nOrderQty;
									if(Utils.getBooleanSetting(mContext, Global.PREF_LOAD_QTY_ENTRY))
									{
										info.nRequestedQty = info.nSuggestedQty;
									}
									dbAdapter.updatePresoldOrDInfo(info);
								}
							}
						}
					}
				}
			}

			//Set KK Value
			List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeListWithNumericKey("RouteNumber", Utils.getIntFromString(Utils.getStringSetting(mContext, Global.PREF_ROUTE_NUMBER)));
			if(descTypeInfoList.size() > 0)
			{
				Utils.setStringSetting(mContext, Global.PREF_KK_VALUE, descTypeInfoList.get(0).strValue1);
			}
			//Utils.registerLoadTime("Order Processing End", System.currentTimeMillis());
			dbAdapter.endTransaction();
			dbAdapter.close();
			String strCurDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			Utils.setStringSetting(mContext, Global.PREF_LOGIN_DATE, strCurDate);

			Global.gPartialLocationFilterList.clear();
			Utils.loadLang(mContext);

			String strBugseeValidation = Utils.getStringSetting(mContext, Global.PREF_BUGSEE_VALIDATION);
			if(Utils.getStringSetting(mContext, Global.PREF_CRASH_REPORT).trim().equals("1") || (!TextUtils.isEmpty(strBugseeValidation) && strBugseeValidation.compareTo(strCurDate) > 0))
			{
				Utils.fileCopy(String.format(Locale.ENGLISH, "%s%s/ROUTECTL.XML", Global.DOWNLOAD_DIR, Global.gTripNumber), Global.ROOT_ROUTECTL_PATH);
				/*if(!Global.bugSeeRunning)
				{
					new Handler(Looper.getMainLooper()).post(() -> {
						LaunchOptions options = new LaunchOptions();
						options.Video.setMaxRecordingTime(60);
						options.General.setShakeToTrigger(false);
						options.Video.setScreenshotEnabled(false);
						Bugsee.relaunch(options);
						Global.bugSeeRunning = true;
					});
				}*/
			}
			Utils.deleteDirectory(Global.DOWNLOAD_DIR);

			DownloadEngine.getEngine().flushToFile();
			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
			{
				JobScheduler jobScheduler =
						(JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
				assert jobScheduler != null;
				jobScheduler.schedule(new JobInfo.Builder(DownloadJobService.MAIN_SERVICE_JOB_ID,
						new ComponentName(mContext, DownloadJobService.class))
						.setOverrideDeadline(500)
						.setPersisted(true)
						.setRequiresCharging(false)
						.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
						.setRequiresDeviceIdle(false)
						.build());
			}
			else
			{
				DownloadEngine.getEngine().loadAllDataToCache();
				DownloadEngine.getEngine().run();
			}


			bRes = 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bRes;
	}

	private static String getFormattedTime(String strOrig) {
		int nVal = Utils.getIntFromString(strOrig);
		String strResult = String.format(Locale.ENGLISH, "%04d", nVal);
		return strResult;
	}
}
