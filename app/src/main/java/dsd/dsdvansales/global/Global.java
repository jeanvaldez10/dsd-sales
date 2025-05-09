package dsd.dsdvansales.global;

import android.os.Environment;

import androidx.fragment.app.Fragment;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dsd.dsdvansales.dialogs.CustomerNoteDialog;
import dsd.dsdvansales.fragment.BaseFragment;
import dsd.dsdvansales.model.*;

public class Global {

	public static 		String SAMPLE_CONFIG_FILE_NAME = "sample_config.json";
	public static 		boolean gUploadProcess 					= false;
	public static 		boolean	_DEBUG_							= false;
	public static		boolean	gLoggedIn						= false;
	public static		boolean gNeedToDownload					= false;
	public static		boolean gIsEndTrip						= false;
	public static		boolean gEnteredEndTrip					= false;

	public static		int		gFragIdx						= 0;
	public static		int		gDeliveryPickupCnt				= 0;

	public static		String	gTripNumber						= "";
	public static		String	gRouteNumber					= "";
	public static		String	gVisitStartedTime				= "";
	public static		String	gLoadStartedTime				= "";
	public static		String	gCurrencySymbol					= "$";

	//Chat variables
	public static 		QBUser	gCurQBUser						= null;
	public static final int		ORDER_ENTRY_NORMAL				= 0;
	public static final int		ORDER_ENTRY_COPY				= 1;
	public static final int		ORDER_ENTRY_TEMPLATE			= 2;
	public static final int		ORDER_ENTRY_SAMPLE				= 3;
	public static final int		ORDER_ENTRY_SUGGEST				= 4;
	public static final int		ORDER_ENTRY_REVIEW_NEW			= 5;
	public static final int		ORDER_ENTRY_REVIEW_SAMPLE		= 6;
	public static		int		gOrderEntryType					= ORDER_ENTRY_NORMAL;

	public static boolean g_bCurPaymentMethod = false;
	public static boolean g_bIsPickup = false;
	public static boolean g_bReverseMode = false;
	public static String g_strDocNo = "";
	public static String gPaymentReference = "";
	public static float dCurrentArAmount = 0f;
	public static List<ARHeaderInfo> gARHeaderInfoList = new ArrayList<>();

//	public static QBRoster chatRoster = null;

	public static EquipmentWithAssInfo gCurEquipmentWithAssInfo = null;
	public static List<EquipmentWithAssInfo> gCurEquipmentWithAssInfoList = null;
	public static EquipmentInfo gCurEquipmentInfo				 = null;
	public static CustomerInfo gDefaultCustomerInfo			= new CustomerInfo();
	public static UCustomerFMInfo gDefaultUCustomerFMInfo		= new UCustomerFMInfo();
	public static UInvenHInfo	gAdjustinfo						= new UInvenHInfo();
	public static List<UInvenDInfo>	gLoadReqeustInfo				= new ArrayList<UInvenDInfo>();
	public static List<UInvenDInfo>	gCurInventoryList				= new ArrayList<>();
	public static List<MakeZipFileInfo> gConsignmentAdjust			= new ArrayList<>();

	public static CustContactInfo	gDefaultContactInfo			= new CustContactInfo();
	public static ARHeaderInfo	gCurHeaderInfo					= null;
	public static UInvenHInfo	gUInvenHinfo					= null;
	public static UOrderHInfo	gUOrderHInfo					= null;
	public static UOrderHInfo	gUOrderReverseHInfo				= null;
	public static UOrderHInfo	gUOrderReverseOriginalHInfo		= null;

	public static long			gReverseTransactionID			= 0;
	public static String		gStrDocNo						= "0";
	public static CustomerInfo	gCurCustomerInfo				= new CustomerInfo();
	public static List<UOrderDInfo> gCurCustomerTemplateOrderInfoList = null;

	public static UARTotalInfo	gUARTotalInfo					= new UARTotalInfo();
	public static UARPaymentInfo	gUARPaymentChequeInfo		= new UARPaymentInfo();
	public static UARPaymentInfo	gUARPaymentCardInfo			= new UARPaymentInfo();
	public static UARPaymentInfo	gUARPaymentEFTInfo		= new UARPaymentInfo();

	public static List<UARTotalInfo> gUARTotalInfoList			= new ArrayList<>();
	public static List<UARPaymentInfo> gUARPaymentChequeInfoList = new ArrayList<>();
	public static List<UARPaymentInfo> gUARPaymentCardInfoList = new ArrayList<>();
	public static List<UARPaymentInfo> gUARPaymentEFTInfoList = new ArrayList<>();

	public static List<OrderHistoryInfo> gOrderHistoryResultInfoList = null;
	public static MakeZipFileInfo makeZipFileInfo = null;
	public static List<MakeZipFileInfo> makeZipFileInfoList		= new ArrayList<>();
	public static List<UploadInfo>	gUploadCustNoList				= new ArrayList<>();
	public static List<Long>	gUploadTempList				= new ArrayList<>();
	public static List<Boolean>	gProductTreeExpandedList		= null;
	public static List<String>	gPrevLocationFilterList			= new ArrayList<>();
	public static List<String>	gLocationFilterList				= new ArrayList<>();
	public static List<String>	gPartialPrevLocationFilterList			= new ArrayList<>();
	public static List<String>	gPartialLocationFilterList				= new ArrayList<>();

	public static List<CustomerInfo>	gCustomerList			= new ArrayList<>();
	public static List<CustomerInfo>	gLoadTruckCustomerList	= new ArrayList<>();
	public static List<CustomerInfo>	gCurrentCustomerList	= null;
	public static List<ProdDetlInfo>	gProdDetlList			= new ArrayList<>();
	public static List<ProdDetlInfo>	gProductList			= new ArrayList<>();
	public static List<CustConBalInfo> 	gCustConBalList			= new ArrayList<>();

	public static List<Long>			gCurCustomerDeliveryList = new ArrayList<>();
	public static List<Long>			gCurVoidCustomerDeliveryList = new ArrayList<>();
	public static List<Long>			gCurCustomerPickupList	 = new ArrayList<>();

	public static final	String	TAG_NAME						= "DSD Delivery";
	public static final String	SERVICE_CLASS_NAME				= "dsd.dsdvansales.services.MainService";

	public static String	ROOT_LOCATION					= Utils.getExternalStorageRootPath() + "/.DSDVanSales/";
	public static String	ROOT_ROUTECTL_PATH				= Utils.getExternalStorageRootPath() + "/.DSDVanSales/ROUTECTL.xml";
	public static String 	ROOT_DATABASE_LOCATION			= Utils.getExternalStorageRootPath() + "/.DSDVanSales/mydata.db";
	public static String 	ROOT_UPLOAD_DATABASE_LOCATION			= Utils.getExternalStorageRootPath() + "/.DSDVanSales/upload.db";
	public static String 	ROOT_DOWNLOAD_DATABASE_LOCATION			= Utils.getExternalStorageRootPath() + "/.DSDVanSales/download.db";
	public static String 	ROOT_BACKUP_DATABASE_LOCATION			= Utils.getExternalStorageRootPath() + "/.DSDVanSalesBackup/backup.db";
	public static String 	ROOT_GPS_UPLOAD_DATABASE_LOCATION		= Utils.getExternalStorageRootPath() + "/.DSDVanSales/gps_upload_data.db";
	public static String 	ROOT_PREPARE_UPDATE_DATABASE_LOCATION	= Utils.getExternalStorageRootPath() + "/.DSDVanSales/prepare_upload_data.db";
	public static String 	ROOT_GPS_PREPARE_UPLOAD_DATABASE_LOCATION	= Utils.getExternalStorageRootPath() + "/.DSDVanSales/gps_prepare_upload_data.db";
	public static String 	ROOT_GPS_RECORD_DATABASE_LOCATION		= Utils.getExternalStorageRootPath() + "/.DSDVanSales/gps_record_data.db";
	public static String 	ROOT_TRIP_STATUS_DATABASE_LOCATION			= Utils.getExternalStorageRootPath() + "/.DSDVanSales/trip_status.db";
	public static String 	ROOT_LOGIN_LOG_DATABASE_LOCATION			= Utils.getExternalStorageRootPath() + "/.DSDVanSalesBackup/login_status.db";
	public static String 	ROUTEFIC_LOG_FILE_PATH			= Utils.getExternalStorageRootPath() + "/.DSDVanSales/routific_log.txt";

	public static String 	INSTALL_PATH					= Utils.getExternalStorageRootPath() + "/.DSDInstaller/";

	public static String DOWNLOAD_INSTALLER_APK_PATH 						= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/DSDInstaller.apk";

	public static String	ROOT_PRINT_TEMPLATE_LOCCATION	= ROOT_LOCATION;
	public static String	DOWNLOAD_DIR					= ROOT_LOCATION + "download/";
	public static String	PRODUCT_DOWNLOAD_DIR			= ROOT_LOCATION + "product/";
	public static String  ERROR_LOG_DIR                   = ROOT_LOCATION + "error_log.txt";
	//public static final String	UPLOAD_DIR						= ROOT_LOCATION + "upload/";
	public static String	UPLOAD_DIR						= Utils.getExternalStorageRootPath() + "/.DSDVanSalesBackup/";
	public static String	PHOTO_DIR						= Utils.getExternalStorageRootPath() + "/.DSDVanSalesBackup/";
	public static String	LOGO_DIR						= ROOT_LOCATION + "logo/";
	public static String	PDF_DIR							= Utils.getExternalStorageRootPath() + "/.DSDVanSalesBackup/";
	public static String	PDF_LOCAL_DIR					= ROOT_LOCATION + "pdf_local/";
	public static String	QRCODE_LOCAL_DIR					= ROOT_LOCATION + "qrcode_local/";
	public static String	CUSTOMER_PHOTO_DIR				= ROOT_LOCATION + "customer_photos/";
	public static String	ROOT_EQUIPMENT_CATALOG_LOCATION = ROOT_LOCATION + "equipment";

	public static final String	COMPANY_LOGO_FILENAME			= "Cmpylogo.bmp";
	public static final	String 	COMPANY_SPLASH_FILENAME			= "Cmpylogin.bmp";

	public static String	HONEYWELL_PRINTER_SIZE_SUFFIX 				= "honeywell";
	public static final String	HONEYWELL_PRINTER_TYPE_VALUE1 				= "3";
	public static final String 	PRINT_TEMPLATE_INVOICE_FMT_FILENAME 		= "INVOICEFMT.xml";
	public static final String 	HONEYWELL_PRINT_TEMPLATE_INVOICE_FMT_FILENAME 		= "INVOICEFMT.honeywell.xml";
	public static final String 	PRINT_TEMPLATE_ORDERACKFMT_FILENAME 		= "ORDERACKFMT.xml";
	public static final String 	HONEYWELL_PRINT_TEMPLATE_ORDERACKFMT_FILENAME 		= "ORDERACKFMT.honeywell.xml";
	public static final String 	PRINT_TEMPLATE_DELIVERY_FMT_FILENAME		= "DELIVERYFMT.xml";
	public static final String 	HONEYWELL_PRINT_TEMPLATE_DELIVERY_FMT_FILENAME		= "DELIVERYFMT.honeywell.xml";
	public static final String	PRINT_LOAD_SHEET_TEMPLATE_FILENAME			= "LOADSHEETFMT.xml";
	public static final String	HONEYWELL_PRINT_LOAD_SHEET_TEMPLATE_FILENAME= "LOADSHEETFMT.honeywell.xml";
	public static final String	PRINT_TRUCK_INVENTORY_TEMPLATE_FILENAME		= "TRUCKINVENTORYFMT.xml";
	public static final String	HONEYWELL_PRINT_TRUCK_INVENTORY_TEMPLATE_FILENAME		= "TRUCKINVENTORYFMT.honeywell.xml";
	public static final String	PRINT_LOAD_REQUEST_TEMPLATE_FILENAME		= "LOADREQUESTFMT.xml";
	public static final String	HONEYWELL_PRINT_LOAD_REQUEST_TEMPLATE_FILENAME		= "LOADREQUESTFMT.honeywell.xml";
	public static final String 	PRINT_LOAD_ADJUSTMENT_TEMPLATE_FILENAME		= "LOADADJUSTMENTFMT.xml";
	public static final String 	HONEYWELL_PRINT_LOAD_ADJUSTMENT_TEMPLATE_FILENAME = "LOADADJUSTMENTFMT.honeywell.xml";
	public static final String	PRINT_STOCKTAKE_COUNT_TEMPLATE_FILENAME		= "STOCKTAKEFMT.xml";
	public static final String	HONEYWELL_PRINT_STOCKTAKE_COUNT_TEMPLATE_FILENAME		= "STOCKTAKEFMT.honeywell.xml";
	public static final String	PRINT_PICK_SLIP_TEMPLATE_FILENAME			= "PICKSLIPFMT.xml";
	public static final String	HONEYWELL_PRINT_PICK_SLIP_TEMPLATE_FILENAME	= "PICKSLIPFMT.honeywell.xml";

	public static final String 	PRINT_TERMS_TEMPLATE_FILENAME				= "TERMSFMT.xml";
	public static final String 	PRINT_SALES_PLAN_TEMPLATE_FILENAME			= "PRINTSALESFMT.xml";

	public static final String 	HONEYWELL_PRINT_SALES_PLAN_TEMPLATE_FILENAME = "PRINTSALESFMT.honeywell.xml";
	public static final String 	PRINT_OFFLOAD_TEMPLATE_FILENAME				= "OFFLOADFMT.xml";
	public static final String 	HONEYWELL_PRINT_OFFLOAD_TEMPLATE_FILENAME	= "OFFLOADFMT.honeywell.xml";
	public static final String 	PRINT_BANKING_TEMPLATE_FILENAME				= "BANKINGFMT.xml";
	public static final String 	HONEYWELL_PRINT_BANKING_TEMPLATE_FILENAME	= "BANKINGFMT.honeywell.xml";
	public static final String	PRINT_RECEIPT_FILENAME 						= "CASHRECEIPTFMT.xml";
	public static final String	HONEYWELL_PRINT_RECEIPT_FILENAME 			= "CASHRECEIPTFMT.honeywell.xml";
	public static final String	PRINT_COLLECTION_REPORT_FILENAME			= "COLLECTIONFMT.xml";
	public static final String	HONEYWELL_PRINT_COLLECTION_REPORT_FILENAME	= "COLLECTIONFMT.honeywell.xml";
	public static final String	PRINT_TRANSACTION_HISTORY_REPORT_FILENAME	= "ROUTEACTIVITYFMT.xml";
	public static final String	HONEYWELL_PRINT_TRANSACTION_HISTORY_REPORT_FILENAME	= "ROUTEACTIVITYFMT.honeywell.xml";
	public static final String 	PRINT_CONSIGNMENT_FILENAME 					= "CONSIGNMENT.xml";
	public static final String 	HONEYWELL_PRINT_CONSIGNMENT_FILENAME 		= "CONSIGNMENT.honeywell.xml";
	public static final String 	PRINT_CONSIGNMENT_DOCKET_FILENAME 			= "CONSIGNMENTDOCKET.xml";
	public static final String 	HONEYWELL_PRINT_CONSIGNMENT_DOCKET_FILENAME = "CONSIGNMENTDOCKET.honeywell.xml";
	public static final String	PRINT_CONSIGNMENT_INVENTORY_FILENAME		= "CONSIGNMENTINV.xml";
	public static final String	HONEYWELL_PRINT_CONSIGNMENT_INVENTORY_FILENAME		= "CONSIGNMENTINV.honeywell.xml";

	public static final String	PRINT_A4_INVOICE_FILENAME					= "A4INV.xml";
	public static final String	PRINT_A4_DOCKET_FILENAME					= "A4DOCKET.xml";

	public static final String	PRINT_A4_NEW_EQUIPMENT_DOC_FMT_FILENAME	= "NEWEQUIPDOCFMT.xml";
	public static final String	PRINT_A4_SURVEY_DOC_FMT_FILENAME	= "SURVEYRESPFMT.xml";

	public static String	DEFAULT_SIGNATURE_FILEPATH		= ROOT_LOCATION + "signature.jpg";
	public static final String	DEFAULT_COMPANY_LOGO_FILEPATH	= LOGO_DIR + COMPANY_LOGO_FILENAME;
	public static final String	DEFAULT_COMPANY_SPLASH_FILEPATH	= LOGO_DIR + COMPANY_SPLASH_FILENAME;
	public static final String	CONF_PATH						= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/DSDDel_Config.ini";

	public static final String	FTP_DOWNLOAD_DIR				= "/%s/REPS/000000/Download/";
	public static final String	FTP_DOWNLOAD_PATH				= "/%s/REPS/000000/Download/%s.zip";
	public static final String	FTP_SPECIAL_DIR					= "/%s/REPS/000000/Special/";
	public static final String	FTP_SPECIAL_PATH				= "/%s/REPS/000000/Special/%s.zip";
	public static final String	FTP_UPLOAD_DIR					= "/%s/REPS/%s/Upload/";
	public static final String	FTP_UPLOAD_PATH					= "/%s/REPS/%s/Upload/%s";
	public static final String	FTP_REPORTS_DIR					= "/%s/Reports/";
	public static final String	FTP_CUSTOMER_PHOTO_DIR			= "/%s/CustomerCatalog/";
	public static final String	FTP_CUSTOMER_PHOTO_PATH			= "/%s/CustomerCatalog/%s";
	public static final String	FTP_EQUIP_CATALOG_DIR			= "/%s/EquipmentCatalog/";

	public static final String PREF_PASS_CHECK_EXTERNAL_STORAGE = "PassCheckExternalStorage";
	public static final String	PREF_PRINT_FONT_FILENAME		= "PrintFontFileName";
	public static final String	PREF_PRINT_FONT_FAMILY			= "PrintFontFamily";
	public static final String	PREF_PRINT_FONT_ENCODING		= "PrintEncoding";
	public static final String	PREF_PRINT_FONT_EMBEDED			= "PrintEmbeded";

	public static final String	PREF_LOGIN_USER_NAME			= "LoginUserName";
	public static final String	PREF_USER_NAME					= "UserName";
	public static final String	PREF_USER_DESCRIPTION			= "UserDescription";
	public static final String	PREF_DRIVER_NUMBER				= "DriverNumber";
	public static final String	PREF_USER_LOGIN_DESCRIPTION		= "UserLoginDescription";
	public static final String	PREF_LOGIN_PIN_NUMBER			= "LoginPinNumber";
	public static final String	PREF_REFRESH_PIN_NUMBER			= "RefreshPinNumber";
	public static final String	PREF_EXIT_PIN_CODE				= "ExitPinCode";
	public static final String	PREF_ROUTE_NUMBER				= "RouteNumber";
	public static final String	PREF_ROUTE_CENTSROUND			= "CENTSROUND";
	public static final String	PREF_ROUTE_PAYMENTADJUST		= "PAYMENTADJUST";
	public static final String	PREF_TRIP_NUMBER				= "TripNumber";
	public static final String	PREF_TRIP_DATE					= "TripDate";
	public static final String	PREF_GPS_POLL					= "GPSPOLL";
	public static final String	PREF_COMMUNICATION_PIN_CODE		= "CommunicationPINCode";
	public static final String	PREF_FTP_SERVER_IP				= "FTP_ServerIP";
	public static final String	PREF_FTP_COMPANY_NAME			= "FTP_CompanyName";
	public static final String	PREF_FTP_USERNAME				= "FTP_UserName";
	public static final String	PREF_FTP_PASSWORD				= "FTP_Password";
	public static final String	PREF_MY_LONGITUDE				= "MyLongitude";
	public static final String	PREF_MY_LATITUDE				= "MyLatitude";
	public static final String	PREF_CUR_LANG					= "CurLang";
	public static final String	PREF_END_VISIT_PLAN				= "EndVisitPlan";
	public static final String	PREF_CONFIG_STATUS				= "ConfigStatus";
	public static final String	PREF_LOAD_CUSTOMER				= "LoadCustomer";
	public static final String	PREF_LOAD_CONTAINERS			= "LoadContainers";
	public static final String	PREF_DEF_LOCN_NO				= "DefLocnNo";
	public static final String	PREF_PDF_SEQUENCE_NO			= "PdfSequenceNo_%s";
	public static final String	PREF_VEHICLE_NUMBER				= "VehicleNumber";
	public static final String	PREF_CUSTOMER_GET_GPS			= "CustomerGetGPS_%d";
	public static final String	PREF_LOADED_UINVEN_TRXN_NO		= "LoadedUInvenTrxnNo";
	public static final String	PREF_UINVEN_TRXN_NO				= "UInvenTrxnNo";
	public static final String	PREF_LOAD_INVENTORY_COMPLETED	= "LoadInventoryCompleted";
	public static final String	PREF_DELFUTDTE					= "DELFUTDTE";
	public static final String	PREF_PRINTER_MAC_ADDRESS		= "PrinterMacAddress";
	public static final String	PREF_CURRENCY_SYMBOL			= "CurrencySymbol";
	public static final String	PREF_EWAY_CARD_PROC				= "EwayCardProc";
	public static final String	PREF_ADJUST_ALLOW				= "AdjustAllow";
	public static final String	PREF_END_TRIP_CHECKED			= "EndTrip_Checked_%d";
	public static final String	PREF_COLLECTION_SEQUENCE_NO		= "CollectionSequenceNo_%s";
	public static final String	PREF_TEMP_CUST_CHAIN			= "TempCustChain";
	public static final String	PREF_TEMP_CUST_PREFIX			= "TempCustPrefix";
//	public static final String	PREF_TEMP_CHAIN					= "TempChain";
//	public static final String	PREF_TEMP_CUSTOMER				= "TemplateCustomer";
	public static final String	PREF_PRIME_CONTACT				= "PrimeContact";
	public static final String	PREF_CUST_ADD_NEW				= "CustAddNew";
	public static final String	PREF_CUST_SEQ_NO				= "CustSeqNo";
	public static final String	PREF_TODAY						= "Today";
	public static final String	PREF_EWAY_API					= "EWayApi";
	public static final String	PREF_EWAY_SYSTEM				= "EWaySystem";
	public static final String	PREF_EWAY_API_KEY				= "EWayApiKey";
	public static final String	PREF_EWAY_PASSWORD				= "EWayPassword";
	public static final String 	PREF_LOADDEFER					= "LoadDefer";
	public static final String	PREF_LOADPERDDEF 				= "LoadPerDef";
	public static final String	PREF_COMPANY_ID 				= "CompnayId";
	public static final String	PREF_COMPNAY_PIN 				= "CompanyPin";
	public static final String	PREF_COMPANY_NAME				= "CompanyName";
	public static final String 	PREF_EQUIPCHECK					= "EquipCheck";
	public static final String 	PREF_EQUIPRMKEY					= "EquipRMKey";
	public static final String 	PREF_EQUIPREPREQ				= "EquipRepReq";
	public static final String	PREF_ROUTIFICAPI 				= "RoutificApi";
	public static final String	PREF_VISITDURATION 				= "VisitDuration";
	public static final String	PREF_LOCON_NO_GPS_LATITUDE 		= "LoconNoGPSLatitude";
	public static final String	PREF_LOCON_NO_GPS_LONGITUDE 	= "LoconNoGPSLongitude";
	public static final String	PREF_DELNOTEMAN 				= "DelNoteMan";
	public static final String	PREF_GET_GPS	 				= "GetGPS";

	public static final String 	PREF_LOAD_TEMPMAX				= "LoadTempMax";
	public static final String 	PREF_LOAD_TEMPMIN				= "LoadTempMin";
	public static final String 	PREF_LOAD_TEMPREQ				= "LoadTempReq";

	public static final String	PREF_MINTUSER					= "mIntUser";
	public static final String	PREF_MINTMODE					= "mIntMode";

	public static final String	PREF_MINTAPI 					= "MintAPI";
	public static final String	PREF_MINTSECRET					= "MintSecret";

	public static final String	PREF_MINTCNPUSER				= "MintCNUser";
	public static final String	PREF_MINTCNPPWD					= "MintCNPass";
	public static final String	PREF_MINTCN_BASE_URL			= "MintCNBaseUrl";

	public static final String 	PREF_UPLOAD_INV_FMT				= "UploadInvFmt";
	public static final String	PREF_PRINT_TYPE					= "PrintType";
	public static final String	PREF_HONEYWELLLINE_PRINTER_TEMPLATE_SUFFIX				= "HoneywelllinePrinterTemplateSuffix";
	public static final String	PREF_PRINTER_NAME				= "PrinterName";
	public static final String	PREF_PRINTER_SIZE				= "PrinterSize";
	public static final String	PREF_LOAD_QTY_ENTRY				= "LoadQtyEntry";
	public static final String	PREF_INVENCKECK					= "InventoryCheck";
	public static final String	PREF_INVENQTYOFF				= "InvenQtyOff";
	public static final String	PREF_LOADMULTIALL				= "LoadMultiAll";
	public static final String	PREF_INVOICE_NUM_FORMAT			= "InvoiceNumFormat";
	public static final String	PREF_PASSED_LOAD_SECTION		= "PassedLoadSection";
	public static final String 	PREF_SALENOTEMAN				= "SaleNoteMan";
	public static final String	PREF_LOADREQUEST				= "LoadRequest";
	public static final String	PREF_LOADSTKTAKE				= "LoadStockTake";
	public static final String	PREF_SALEQTYENTRY				= "SaleQtyEntry";
	public static final String	PREF_INVOICENUM					= "InvoiceNum";
	public static final String	PREF_ALLOW_VOID					= "AllowVoid";
	public static final String	PREF_TREE_DESC					= "TreeDesc";
	public static final String 	PREF_PRINT_PICK_LIST			= "PrintPickList";
	public static final String	PREF_CUSTINFO_MSG 				= "CustInfoMsg";
	public static final String	PREF_INVENTORYUOM  				= "InventoryUOM";
	public static final String	PREF_KK_VALUE  					= "KKValue";
	public static final String	PREF_USEORDNOASINV  			= "USEORDNOASINV";
	public static final String	PREF_CREATED_UNSERVICED_REASON_REPORT = "CREATED_UNSERVICED_REASON_REPORT";
	public static final String	PREF_SHOWSALESPERF 				= "ShowSalesPerf";
	public static final String	PREF_IGNORERLOAD 				= "IgnoreRLoad";
	public static final String	PREF_LAST_DOC_NO 				= "LastDocNo";
	public static final String	PREF_INVCALC 					= "InvcCalc";
	public static final String	PREF_PASSED_DOWNLOAD 			= "PassedDownload";
	public static final String	PREF_VISIT_SELECT_CUSTOMER 		= "VisitSelectCustomer";
	public static final String	PREF_SHOW_INV 					= "ShowInv";
	public static final String	PREF_DETAIL_ENTRY 				= "DailyEntry";
	public static final String	PREF_ATTACHMENT_LOGIN_PIN_NUMBER= "AttachmentLoginPinNumber";
	public static final String	PREF_ATTACHMENT_LOGIN_USERNAME	= "AttachmentLoginUserName";
	public static final String	PREF_ATTACHMENT_LOGIN_PASSWORD	= "AttachmentLoginPassword";
	public static final String	PREF_QTY_ENTRY_METHOD			= "QtyEntryMethod";
	public static final String	PREF_SCALING					= "Scaling";
	public static final String	PREF_DECIMAL					= "Decimal";

    public static final String	PREF_SUPERVISOR					= "SuperVisor";
    public static final String	PREF_SUPERVISOR_USER_PASSWORD	= "SuperVisorUserPassword";

	public static final String	PREF_LOADADJUSTMENT_APPROVAL_KEY	= "LoadAdjustmentApprovalKey";

	public static final String	PREF_LOADREQUEST_APPROVAL_KEY	= "LoadRequestApprovalKey";
	public static final String	PREF_SECURITY9_KEY	= "Security9Key";
	
    public static final String	PREF_LOGIN_AS_SUPER_VISOR		= "LoginAsSuperVisor";
	public static final String	PREF_HANDLING_FEE_TAX_CODE		= "HandlingFeeTaxCode";

	public static final String	PREF_MINTHPPUSER				= "MintHPPUser";
	public static final String	PREF_MINTHPPSECRET				= "MintHPPSecret";
	public static final String	PREF_MINTHPPURL					= "MintHPPURL";
	public static final String	PREF_MINTHPPCURR				= "MintHPPCURR";

	public static final String	PREF_BARCODE_PREFIX				= "BarcodePrefix";
	public static final String	PREF_BARCODE_SUFFIX				= "BarcodeSuffix";

	public static final String	PREF_DATEFMT					= "DateFmt";
	public static final String	PREF_TEMPFMT					= "TempFmt";

	public static final String	PREF_STRIPE_PUB_KEY				= "StripePubKey";
	public static final String	PREF_STRIPE_SEC_KEY				= "StripeSecKey";

	public static final String	PREF_CURRENCY					= "Currency";

	public static final String	PREF_DEBUG						= "Debug";
	public static final String	PREF_TREE_ORDER					= "TreeOrder";

	public static final String	PREF_POST_VISIT					= "PostVisit";

	public static final String	PREF_DOCUMENTS					= "Documents";

	public static final String	PREF_CON_EMPTY					= "ConEmpty";
	public static final String	PREF_RETURNS					= "Returns";

	public static final String	PREF_CUSTTYPE					= "CustType";

	public static final String	PREF_SALES_MTD					= "SalesMTD";
	public static final String	PREF_MONTH_TARGET				= "MonthTarget";
	public static final String	PREF_SALES_WTD					= "SalesWTD";
	public static final String	PREF_SALES_LWTD					= "SalesLWTD";

	public static final String	PREF_COMMSMETHOD				= "CommsMethod";

	public static final String	PREF_APIUSER					= "ApiUser";
	public static final String	PREF_APIPASSWORD				= "ApiPassword";
	public static final String	PREF_APICOMPANY					= "ApiCompany";
	public static final String	PREF_GETORDERS					= "GetOrders";

	public static final String	PREF_WINDCAVE_UAT		    	= "WindCaveUAT";
	public static final String	PREF_WINDCAVE_API				= "WindCaveAPIKey";
	public static final String	PREF_WINDCAVE_PASSWORD			= "WindCavePassword";
	public static final String	PREF_WINDCAVE_CURR				= "WindCaveCurr";

	public static final String	PREF_WINDCAVE_HITUSERNAME		= "WindCaveHitUsername";
	public static final String	PREF_WINDCAVE_HITKEY			= "WindCaveHitKey";
	public static final String	PREF_WINDCAVE_HITDEVICE_ID		= "WindCaveHitDeviceId";
	public static final String	PREF_WINDCAVE_HITSTATION_ID		= "WindCaveHitStationId";
	public static final String	PREF_WINDCAVE_HITPOSNAME		= "WindCaveHitPosName";
	public static final String	PREF_WINDCAVE_HITPOSVERSION		= "WindCaveHitPosVersion";
	public static final String	PREF_WINDCAVE_HITVENDORID		= "WindCaveHitVendorId";
	public static final String	PREF_HOTSPOT					= "Hotspot";
	public static final String	PREF_LOAD_BATCH					= "LoadBatch";
	public static final String	PREF_API_ROUTE_SCH				= "ApiRouteSCH";
	public static final String 	PREF_DEFAULT_LANG				= "DefaultLang";
	public static final String 	PREF_LOGIN_DATE					= "LoginDate";
	public static final String 	PREF_HIDE_SUMMARY_PAYMENT		= "HideSummaryPayment";
	public static final String 	PREF_USE_ORDERS_AS_LOAD			= "UseOrdersAsLoad";
	public static final String 	PREF_LOAD_REQUEST_UOM			= "LoadRequestUoM";
	public static final String 	PREF_AUTO_SCAN_ADD				= "AutoScanAdd";
	public static final String 	PREF_ENABLE_BREAKS				= "EnableBreaks";
	public static final String 	PREF_CRASH_REPORT				= "CrashReport";
	public static final String 	PREF_LAST_VISIT_BREAK			= "LastVisitBreak";

	public static final String 	PREF_DISABLE_OFFLOADS			= "DisableOffloads";

	public static final String 	PREF_GET_SALES_DELAY			= "GetSalesDelay";
	public static final String	PREF_TRIP_USER					= "TripUser";
	public static final String	PREF_EXCLUDES_TRIPS				= "ExcludeTrips";
	public static final String	PREF_ENDTRIP_HIDE				= "EndTripHide";
	public static final String	PREF_ASKED_CHANGE_DRIVER_NAME	= "AskedChangeDriverName";
	public static final String	PREF_ASKED_CHANGE_PRINTER_TYPE	= "AskedChangePrinterType";
	public static final String	PREF_BUGSEE_VALIDATION			= "BugseeValidation";
	public static final String	PREF_ORDER_API_START			= "OrderAPIStart";
	public static final String	PREF_ORDER_API_END				= "OrderAPIEnd";
	public static final String	PREF_FRGOODPRM					= "FRGoodPRM";
	public static final String	PREF_FRGOODPLN					= "FRGoodPLN";
	public static final String	PREF_FRGOODASS					= "FRGoodASS";

	public static final String	PREF_SEARCH_KEYWORD				= "SearchKeyword";
	public static final String	PREF_SEARCH_CRITIA_IDX			= "SearchCritiaIdx";
	public static final String	PREF_SEARCH_FILTER				= "SearchFilter";
	public static final String	PREF_SUMMARY_DETAIL				= "SummaryDetail";
	public static final String	PREF_DOC_ARCHIVE_HISTORY_DAYS	= "DocArchiveHistoryDays";
	public static final String	PREF_TRIP_ID					= "TripId";
	public static final String  PREF_LOT_ASSIGNMENT				= "LotAssignment";
	public static final String  PREF_FIRST_CHECK_LOAD			= "FirstCheckLoad";
	public static final String  PREF_DisableFTP					= "DisableFTP";
	public static final String  PREF_DisableInterTruck			= "DisableInterTruck";
	public static final String  PREF_UNLESHED					= "Unleshed";
	public static final String  PREF_TEMPREQ					= "TempReq";
	public static final String  PREF_UI_DETL_SORT				= "UIDetlSort";
	public static final String  PREF_BREAK_DOWN_START_TIME		= "BreakDownStartTime";
	public static final String  PREF_LOGIN_START_TIME		= "LoginStartTime";
	public static final String  PREF_DRIVER_BREAK_DOWN_TYPE_INFO= "DriverBreakDownTypeInfo";
	public static final String  PREF_PRINT_ZERO_QTY_PRESOLD_ITEM= "PrintZeroQTYPresoldDItem";

	public static final String	PREF_TOUR_PASSED				= "TourPassed";
	public static final String	PREF_TEMPLATE_CHAIN				= "TemplateChain";
	public static final String	PREF_TEMPLATE_CUST				= "TemplateCust";
	public static final String	PREF_ALLOW_REPEAT_SALES			= "AllowRepeatSales";
	public static final String	PREF_STKTKEGRP					= "StockTakeGroup";
	public static final String	PREF_STKROLGRP					= "StockRolGroup";
	public static final String	PREF_HONEYWELL_PRINTER			= "HoneywellPrinterType";
	public static final String	PREF_TEMP_CUST_MASK				= "TempCustMask";

	public static final String	PREF_ITEM_CODE_DISPLAY_SOURCE	= "ItemCodeDisplaySource";

	public static final String	PREF_HONEYWELL_PRINTER_MAC_ADDRESS	= "HoneywellPrinterMacAddress";
	public static final String	PREF_HONEYWELL_LINE_PRINTER_MAC_ADDRESS	= "HoneywellLinePrinterMacAddress";
	public static final String	PREF_BIXOLON_PRINTER_MAC_ADDRESS	= "BixolonPrinterMacAddress";
	public static final String	PREF_BIXOLON_PRINTER_FRIENDLY_NAME	= "BixolonPrinterFriendlyName";

	public static final String	PREF_LOAD_SRT_DEF = "LoadSortDefault";

	public static final String	PREF_ROUTEFIC_LOG = "RouteficLog";

	public static final String PREF_SOMA_LEFT_MARGIN = "SomaLeftMargin";
	public static final String PREF_SOMA_TOP_MARGIN = "SomaTopMargin";
	public static final String PREF_SHOW_SOMA = "ShowSoma";
	public static final String PREF_LOAD_REQUEST_EOT = "LoadRequestEOT";
	public static final String PREF_LOAD_REQUEST_DATA = "LoadRequestData";
	public static final String PREF_DISALLOW_ACCOUNT_PAY = "DisallowAcctPay";
	public static final String PREF_DISABLE_INV_REPRINT = "DisableInvReprint";
	public static final String PREF_TENDER_FULL_AMOUNT = "TenderFullAmount";
	public static final String PREF_DEFAULT_TODAY_SCREEN = "DefaultTodayScreen";
	public static final String PREF_DISABLE_NEG_INV_AMT = "DisableNegInvAmt";
	public static final String PREF_DISABLE_LOAD_ADJUST = "DisableLoadAdjust";
	public static final String PREF_DISABLE_FREE = "DisableFree";
	public static final String PREF_LOAD_SHEET_INCLUDE_RLOAD = "LoadSheetIncludeRLoad";
	public static final String PREF_PRINT_ERROR_LOG = "PrintErrorLog";
	public static final String PREF_DISABLE_END_TRIP_ERROR_LOG = "DisableEndTripErrorLog";
	public static final String PREF_DISABLE_UPLOAD_LOG = "DisableUploadLog";
	public static final String PREF_DISABLE_LOGOUT = "DisableLogout";
	public static final String PREF_ROUTECTL_TRIP_DATE = "RoutectlTripDate";
	public static final String PREF_DISABLE_AFTER_EOT = "DisableAfterEOT";
	public static final String PREF_EOT_PIN = "EOT_PIN";
	public static final String PREF_ROUTE_NUMBER_HAS_SUFFIX = "HasSuffix";
	public static final String PREF_CREDIT_IN_EOD_COLLECTION = "CreditInEODCollection";
	public static final String PREF_SEPARATE_PERCENT_OFF_PROMO = "SEPARATE_PERCENT_OFF_PROMO";
	public static final String PREF_NOT_AT_STORE_RADIUS = "NotAtStoreRadius";
	public static final String PREF_NOT_AT_STORE = "NotAtStore";
	public static final String PREF_PROD_SEARCH_IMAGE_MODE = "ProdSearchImageMode";
	public static final String PREF_TREE_AS_IMAGE = "TreeAsImage";
	public static final String PREF_HIDE_CUST_MAINTENANCE = "HideCustMaint";
	public static final String PREF_DISABLE_GPS_PIN = "DisableGPSPin";
	public static final String PREF_MFA_SECRET = "MFASecret";
	public static final String PREF_CHECK_RETURN_QTY = "CheckReturnQty";
	public static final String PREF_MAX_OPEN_DAYS_UNAUTH = "MaxOpenDaysUnauth";

	public static final String ZEBRA_PRINT_ERROR_FILENAME = "zebra_error_log.txt";
	public static final String HONEYWELL_PRINT_ERROR_FILENAME = "honeywell_error_log.txt";
	public static final String BIXOLON_PRINT_ERROR_FILENAME = "bixolon_error_log.txt";
	public static final String UPLOAD_ERROR_FILENAME = "upload_error_log.txt";

	public static final String	DAY_NO							= "DayNo";
	public static final String	SALE_TARGET_MTH					= "TargetMonth";
	public static final String	SALE_TARGET_DAY					= "TargetDay";
	public static final String 	SALE_ACTUAL_MTD					= "ActualMTD";
	public static final String 	SALE_DAYS_REMAINING				= "SaleDaysRemaining";
	public static final String 	SALE_PERIODDESC					= "SalePeriodDesc";

	public static final String 	SALE_TODAY						= "SalesToday";
	public static final String 	RETURNS_TODAY					= "ReturnsToday";
	public static final String 	COLLECTIONS_TODAY				= "CollectionsToday";
	public static final String	NEW_CUSTOMERS_TODAY				= "NewCustomersToday";

	public static final String	TOTAL_SAMPLE_COUNT				= "TotalSampleCount";
	public static final String	TOTAL_SALES_COUNT				= "TotalSalesCount";
	public static final String	TOTAL_RETURNS_COUNT				= "TotalReturnsCount";

	public static final String	EXTRA_TYPE						= "Type";
	public static final String	EXTRA_ITEM_NO					= "ItemNo";
	public static final String	EXTRA_ITEM_UPC					= "ItemUPC";
	public static final String	EXTRA_CUSTOMER_INFO				= "CustomerInfo";
	public static final String	EXTRA_PHOTO_PATH				= "PhotoPath";
	public static final String	EXTRA_IS_ALL_CUSTOMER			= "IsAllCustomer";
	public static final String	EXTRA_CUSTOMER_NO				= "CustomerNo";
	public static final String	EXTRA_IS_PICKUP					= "IsPickUp";
	public static final String	EXTRA_IS_REVERSE				= "IsReverse";
	public static final String	EXTRA_CUSTOMER_NAME				= "CustomerName";
	public static final String	EXTRA_SIGNATURE_PATH			= "SignaturePath";
	public static final String	EXTRA_SURVEY_ID					= "SurveyID";
	public static final String	EXTRA_TRANSFER_TYPE				= "TransferType";
	public static final String	EXTRA_IS_FROM_END_TRIP			= "IsFromEndTrip";
	public static final String	EXTRA_INVENTORY_TYPE			= "InventoryType";
	public static final String	EXTRA_TRANSFER_LATER			= "TransferLater";
	public static final String	EXTRA_TRANSFER_ROUTE			= "TransferRoute";
	public static final String	EXTRA_IS_EDIT_CUSTOMER			= "IsEditCustomer";
	public static final String	EXTRA_IS_EDIT_CUST_INFO			= "IsEditCustInfo";
	public static final String	EXTRA_IS_EDIT_CONTACT			= "IsEditContact";
	public static final String	EXTRA_IS_AUTO_COLLECT			= "IsAutoCollect";
	public static final String	EXTRA_CARD						= "ExtraCard";
	public static final String	EXTRA_IS_FROM_DELIVERY_SUMMARY	= "ExtraIsFromDeliverySummary";
	public static final String	EXTRA_SURVEY_IDX				= "SurveyIdx";
	public static final String	EXTRA_SURVEY_TYPE				= "SurveyType";
	public static final String	EXTRA_SURVEY_TITLE				= "SurveyTitle";
	public static final String	EXTRA_SURVEY_RESPONSE			= "SurveyResponse";
	public static final String	EXTRA_FROM_SURVEY_ID			= "FromSurveyID";
	public static final String	EXTRA_CONSIGNMENT				= "Consignment";
	public static final String	EXTRA_FILTER					= "Filter";
	public static final String	EXTRA_DESCTYPE_INFO				= "DescTypeInfo";
	public static final String	EXTRA_FROM						= "ExtraFrom";
	public static final String	EXTRA_BARCODESCAN_FROM			= "ExtraBarcodeScanFrom";

	public static final String	ACTION_REFRESH_LANGUAGE			= "ACTION_REFRESH_LANGUAGE";
	public static final String	ACTION_REFRESH_STOCK_VALUE		= "ACTION_REFRESH_STOCK_VALUE";
	public static final String	ACTION_CAPTURE_SIGNATURE_EXIT	= "ACTION_CAPTURE_SIGNATURE_EXIT";
	public static final String	ACTION_SELECT_PRODUCT			= "ACTION_SELECT_PRODUCT";
	public static final String	ACTION_FILTER_LOCATION			= "ACTION_FILTER_LOCATION";
	public static final String	ACTION_FILTER_BY				= "ACTION_FILTER_BY";
	public static final String	ACTION_TAKE_PHOTO				= "ACTION_TAKE_PHOTO";
	public static final String	ACTION_CAPTURE_SIGNATURE		= "ACTION_CAPTURE_SIGNATURE";
	public static final String	ACTION_REFRESH_CUSTOMER_LIST	= "ACTION_REFRESH_CUSTOMER_LIST";
	public static final String	ACTION_REFRESH_CUSTOMER_DETAIL	= "ACTION_REFRESH_CUSTOMER_DETAIL";
	public static final String	ACTION_MAP_REFRESH_CUSTOMER_LIST = "ACTION_MAP_REFRESH_CUSTOMER_LIST";
	public static final String	ACTION_ORDER_HISTORY			 = "ACTION_ORDER_HISTORY";
	public static final String	ACTION_BREAK_TIME_FIRE			 = "ACTION_BREAK_TIME_FIRE";
	public static final String 	ACTION_ADDITIONAL_DOWNLOAD_FINISHED		 = "ACTION_ADDITIONAL_DOWNLOAD_FINISHED";

	public static final int		DEFAULT_FTP_PORT				= 21;
	public static final float	DEFAULT_FLOAT					= -10000f;
	public static final int		DEFAULT_LONG					= -10000;
	public static int		MULTIPLE_VALUE					= 100000;

	public static final long	MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;
	public static final long	MIN_TIME_FOR_LOCATION_UPDATES	= 10 * 60 * 1000;
	public static final float	NOT_AT_STORE_RADIUS				= 0.5f;		// 200m = 0.2Km
	public static final String	CONTACT_SPLITER					= "<ContactSpliter>";

	public static final String	DATE_YYYYMMDD_FORMAT			= "yyyyMMdd";
	public static final String	DATE_DD_MM_YY_FORMAT			= "dd/MM/yy";
	public static final String	DATE_DD_MM_YYYY_FORMAT			= "dd/MM/yyyy";
	public static final String	DATE_YYYYMMDDHHMMSS_FORMAT		= "yyyyMMddHHmmss";

	public static final String	TIME_HHMMSS_FORMAT				= "HHmmss";
	public static final String	TIME_HH_MM_FORMAT				= "HH:mm";

	public static final String	SECTION_ACCOUNT					= "Account";
	public static final String	SECTION_FTP						= "FTP";
	public static final String	SECTION_INSTALL					= "Install";


	public static final String	KEY_USER_NAME					= "UserName";
	public static final String	KEY_PIN_NUMBER					= "PINNumber";
	public static final String	KEY_TRIP_NUMBER					= "TripNumber";
	public static final String	KEY_SERVER_IP					= "ServerIP";
	public static final String	KEY_COMPANY_NAME				= "CompanyName";
	public static final String	KEY_PASSWORD					= "Password";
	public static final String	KEY_COMPANY_ID					= "CompanyID";
	public static final String	KEY_COMPANY_PIN					= "CompanyPin";

	public static final String	KEY_FILTER_BY					= "FilterBy";

	public static final String	QUESTION_TYPE_RADIO				= "Y";
	public static final String	QUESTION_TYPE_CHOICE			= "L";
	public static final String	QUESTION_TYPE_MULTI_CHOICE		= "M";
	public static final String	QUESTION_TYPE_PHOTOGRAPH		= "I";
	public static final String	QUESTION_TYPE_ALPHANUMERIC		= "A";
	public static final String	QUESTION_TYPE_NUMERIC			= "N";
	public static final String	QUESTION_TYPE_PRICE				= "P";
	public static final String	QUESTION_TYPE_SIGNATURE			= "S";
	public static final String	QUESTION_TYPE_BARCODE			= "B";
	public static final String QUESTION_TYPE_DATE				= "D";

	public static final int		PROMO_TYPE_CENTS_OFF			= 1;
	public static final int		PROMO_TYPE_PERCENTAGE_OFF		= 2;
	public static final int		PROMO_TYPE_REPLACE_PRICE		= 3;
	public static final int		PROMO_TYPE_BUY_FREE				= 4;
	public static final int		PROMO_TYPE_BUY_FREE_MULTI		= 8;

	public static final int		REASON_TYPE_NON_DELIVERY		= 0;
	public static final int		REASON_TYPE_SHORT_DELIVERY		= 1;
	public static final int		REASON_TYPE_SHORT_PICKUP		= 2;
	public static final int		REASON_TYPE_NON_SERVICE			= 3;

	public static final int		CONF_NOT_LOADED					= 0;
	public static final int		CONF_NOT_EXIST					= 1;
	public static final int		CONF_GOT_LOADED					= 2;

	public static final int		DLG_RETURN_MIDDLE				= 0;
	public static final int		DLG_RETURN_LEFT					= 1;
	public static final int		DLG_RETURN_RIGHT				= 2;
	public static final int		DLG_RETURN_BOTTOM				= 3;

	public static final int		CONTACT_RETURN_TELEPHONE		= 1;
	public static final int		CONTACT_RETURN_SMS				= 2;
	public static final int		CONTACT_RETURN_EMAIL			= 3;

	public static final int		SELECT_PRODUCT_ITEMNO			= 0;
	public static final int		SELECT_PRODUCT_ITEMUPC			= 1;

	public static final int		ACTION_DELIVER_TO_STORE			= 1;
	public static final int		ACTION_VIEW_STORE_DETAILS		= 2;
	public static final int		ACTION_UNABLE_TO_DELIVER		= 3;
	public static final int		ACTION_NAVIGATE_ME_TO_STORE		= 4;

	public static final int		ACTION_PRINT					= 1;
	public static final int		ACTION_REPRINT					= 2;
	public static final int		ACTION_VOID						= 3;
	public static final int		ACTION_VIEW						= 4;
	public static final int		ACTION_VIEW_VOID				= 5;

	public static final int		INVENTORY_FRESH					= 0;
	public static final int		INVENTORY_RETURNS				= 1;
	public static final int		INVENTORY_SPOILS				= 2;
	public static final int		INVENTORY_CONTAINERS			= 3;
	public static final int		INVENTORY_FREE					= 4;
	public static final int		INVENTORY_TRUCK					= 1;

	public static final int		COLLECTION_CHEQUE				= 0;
	public static final int		COLLECTION_CASH					= 1;
	public static final int		COLLECTION_EOD_CHEQUE			= 2;
	public static final int		COLLECTION_EOD_CASH				= 3;
	public static final int		COLLECTION_CARD					= 7;
	public static final int		COLLECTION_EFT					= 9;

	public static final int		FRAG_LOGIN						= 0;
	public static final int		FRAG_PRE_TRIP_TASKS				= 1;
	public static final int		FRAG_MESSAGE_BOARD				= 2;
	public static final int		FRAG_DELIVERY_PLAN				= 3;
	public static final int		FRAG_PRE_TRIP_CHECK				= 4;
	public static final int		FRAG_CONFIGURATION				= 5;
	public static final int		FRAG_LOAD_TRUCK_BULK			= 6;
	public static final int		FRAG_PRODUCT_TREELIST			= 7;
	public static final int		FRAG_SEARCH_PRODUCT				= 8;
	public static final int		FRAG_FILTER_LOCATION			= 9;
	public static final int		FRAG_LOAD_CONTAINERS			= 10;
	public static final int		FRAG_LOAD_TRUCK_SIGN			= 11;
	public static final int		FRAG_CUSTOMER_DETAIL			= 12;
	public static final int		FRAG_CUSTOMER_PHOTOVIEW			= 13;
	public static final int		FRAG_CUSTOMER_MAPVIEW			= 14;
	public static final int		FRAG_SELECT_CUSTOMER			= 15;
	public static final int		FRAG_INSTORE_ACTIVITIES			= 16;
	public static final int		FRAG_MAKE_DELIVERY				= 17;
	public static final int		FRAG_DELIVERY_SUMMARY			= 18;
	public static final int		FRAG_PAYMENT_COLLECTION			= 19;
	public static final int		FRAG_CHEQUE_DETAIL				= 20;
	public static final int		FRAG_CARD_DETAIL				= 21;
	public static final int		FRAG_VEHICLE_INVENTORY			= 22;
	public static final int		FRAG_POS_CHECK					= 23;
	public static final int		FRAG_POS_CHECK_DETAIL			= 24;
	public static final int		FRAG_ADJUST_INVENTORY			= 25;
	public static final int		FRAG_ADJUST_SUMMARY				= 26;
	public static final int		FRAG_TRIP_SETTLEMENT			= 27;
	public static final int		FRAG_INCOMPLETE_DELIVERIES		= 28;
	public static final int 	FRAG_ENDING_INVENTORY 			= 29;
	public static final int		FRAG_COLLECTIONS_BALANCING		= 30;
	public static final int		FRAG_PRICE_DETAILS				= 31;
	public static final int		FRAG_SEARCH_CUSTOMER			= 32;
	public static final int		FRAG_NEW_CUSTOMER				= 33;
	public static final int		FRAG_PRINT_DOCUMENTS			= 34;
	public static final int		FRAG_LOADTRUCKBYCUSTOMER		= 35;
	public static final int		FRAG_PARTIAL_FILTER_LOCATION	= 36;
	public static final int		FRAG_LOAD_ADJUSTMENT			= 37;
	public static final int		FRAG_ORDER_ADJUSTMENT			= 38;
	public static final int		FRAG_RETURN_REASONS				= 39;
	public static final int		FRAG_DETAIL_COLLECTION			= 40;
	public static final int 	FRAG_SUGGESTED_ORDER 			= 41;
	public static final	int 	FRAG_CREATE_NEW_ORDER_FRAGMENT	= 42;
	public static final int		FRAG_LOAD_REQUEST				= 43;
	public static final int		FRAG_LOAD_REQUEST_SUMMARY		= 44;
	public static final int		FRAG_ENDING_INVENTORY_SUMMARY	= 45;
	public static final int		FRAG_ON_HANDCOUNT_FRAGMENT 		= 46;
	public static final int		FRAG_SALES_TRACKER				= 47;
	public static final int		FRAG_STOCKTAKE_COUNT_ENTRY		= 48;
	public static final int		FRAG_SPOILS_ENTRY_FRAGMENT		= 49;
	public static final int		FRAG_LOAD_TRUCK_CUSTOMER_ORDER_DETAIL		= 50;
	public static final int		FRAG_FREE_REASONS				= 51;
	public static final int		FRAG_SALES_PERFORMANCE			= 52;
	public static final int		FRAG_CUSTOMER_DIARY				= 53;
	public static final int		FRAG_NEW_CUSTOMER_DIARY			= 54;
	public static final int		FRAG_RESEND_DATA				= 55;
	public static final int		FRAG_CONSIGNMENT_COUNT_SUMMARY	= 56;
	public static final int		FRAG_CONSIGNMENT_COUNT			= 57;
	public static final int		FRAG_ADJUST_CONSIGNMENT_INVENTORY	= 58;
	public static final int		FRAG_UPDATE_CONSIGNMENT_INVENTORY	= 59;
	public static final int		FRAG_CONSIGNMENT_INVENTORY_SUMMARY	= 60;
	public static final int		FRAG_ADD_BILLING_FRAGMENT	= 61;
	public static final int		FRAG_ADD_CONTACT	= 62;
	public static final int		FRAG_CUSTOMER_INFO	= 63;
	public static final int		FRAG_CONTACT_DETAIL_LIST	= 64;
	public static final int		FRAG_ADD_CONTACT_INFO	= 65;
	public static final int		FRAG_FILTER_BY	= 66;
	public static final int		FRAG_PICKING	= 67;
	public static final int		FRAG_EQUIPMENT_LIST	= 68;
	public static final int		FRAG_ADD_EQUIPMENT_LIST	= 69;
	public static final int		FRAG_CARD_NOT_PRESENTED_COLLECTIONS	= 70;
	public static final int		FRAG_EFT_DETAIL	= 71;

	public static final int		TRXN_PICKUP						= 1;
	public static final int		TRXN_BUYBACK					= 2;
	public static final int		TRXN_DELIVER					= 4;
	public static final int		TRXN_CONTAINER_DUMP				= 6;
	public static final int		TRXN_CONTAINER_SALE				= 7;
	public static final int		TRXN_FREE						= 12;
	public static final int		TRXN_SAMPLE						= 11;

	public static final int 	WEIGHT_ITEM						= 4; // ProdLocn.nSCWType = 4

	public static final int		LANG_ENGLISH					= 0;
	public static final int		LANG_AMERICAN					= 1;
	public static final int		LANG_INDONESIAN					= 2;
	public static final int		LANG_THAI						= 3;
	public static final int		LANG_SPANISH					= 4;
	public static final int		LANG_ARABIC						= 5;

	public static 		int		LANG							= LANG_ENGLISH;

	public static final String	PRODUCT_FILTER_ARRAY[]			= {"ITEMTYPE", "PRODGROUP", "PRODLINE", "BRAND", "SUBBRAND"};
	public static final int		PRODUCT_FILTER_COUNT			= PRODUCT_FILTER_ARRAY.length;
	
	public static final String 	KEY_DETAILFILE					= "DetailFile";
	public static final String	KEY_PICKLOCN					= "PickLocn";
	public static final String	KEY_CUSTOMER_NAME				= "CustomerName";
	public static final String	KEY_CUSTOMER_TIME				= "CustomerTime";

	public static final String  BLE_SERVICE_UUID = "80323644-3537-4F0B-A53B-CF494ECEAAB3";
	public static final String  BLE_CHARACTERISTIC_UUID = "80323644-3537-4F0B-A53B-CF494ECEAAB3";
	
	public static final int		DISCOVER_BLUETOOTH_TIMEOUT		= 30;

	public static final int		DRIVER_CUSTOMER_CHAINNO			= 99999;

	public static float gCurPreviousBalance = 0.0f;
	public static float gCurTotalValue = 0.0f;
	public static float gCurTotalDueInvoice = 0.0f;
	public static float gCurRealPaymentAmount = 0.0f;

	public static final int SALE_PRINT = 1;
	public static final int LOAD_SHEET_PRINT = 2;
	public static final int TRUCK_INVENTORY_PRINT = 3;
	public static final int LOAD_REQUEST_PRINT = 4;
	public static final int LOAD_ADJUSTMENT_PRINT = 5;
	public static final int LOAD_STOCKTAKE_PRINT = 6;
	public static final int PICK_SLIP_PRINT = 7;
	public static final int SALE_DOCKET_PRINT = 8;
	public static final int SALES_PLAN_PRINT = 9;
	public static final int TERMS_PRINT = 10;
	public static final int OFFLOAD_PRINT = 11;
	public static final int CASH_PRINT = 12;
	public static final int COLLECTION_PRINT = 13;
	public static final int TRANSACTION_HISTORY_PRINT = 14;
	public static final int ORDER_PRINT = 15;
	public static final int CONSIGNMENT_COUNT_PRINT = 16;
	public static final int CONSIGNMENT_INVENTORY_PRINT = 17;
	public static final int CONSIGNMENT_DOCKET_PRINT = 18;
	public static final int BANKING_PRINT = 19;
	public static final int A4_INVOICE_PRINT = 20;
	public static final int A4_DOCKET_PRINT = 21;
	public static final int PRINT_EQUIPMENT_DOC = 22;
	public static final int PRINT_SURVEY_DOC = 23;

	public static UInvenHInfo gCurEndingInventory = null;
	public static List<UInvenDInfo> gCurEndingInventoryList = null;

	public static boolean gLoadedDefault = false;
	public static boolean bVisitAutoSelectCustomer = false;

	public static String gRTrxnDate = "";
	public static String gRTrxnTime = "";

	public static String DEVICE_MODEL = "";

	// Based on http://en.wikipedia.org/wiki/Bank_card_number#Issuer_identification_number_.28IIN.29
	public static final String[] PREFIXES_AMERICAN_EXPRESS = {"34", "37"};
	public static final String[] PREFIXES_DISCOVER = {"60", "62", "64", "65"};
	public static final String[] PREFIXES_JCB = {"35"};
	public static final String[] PREFIXES_DINERS_CLUB = {"300", "301", "302", "303", "304", "305", "309", "36", "38", "39"};
	public static final String[] PREFIXES_VISA = {"4"};
	public static final String[] PREFIXES_MASTERCARD = {"50", "51", "52", "53", "54", "55"};

	public static final int MAX_LENGTH_STANDARD = 16;
	public static final int MAX_LENGTH_AMERICAN_EXPRESS = 15;
	public static final int MAX_LENGTH_DINERS_CLUB = 14;

	public static String EQUIP_REPAIR_REASON_DESC_ID = "ReasonforRepair";
	public static String EQUIP_REPAIR_STATUS_DESC_ID = "RepairStatus";
	public static String EQUIP_REPAIR_TYPE_DESC_ID = "RepairType";
	public static String EQUIP_APPLICATION_DESC_ID = "EQAPP";
	public static String EQUIP_STATUS_DESC_ID = "EQSTATUS";
	public static String EQUIP_DESCRIPTION_ID = "EQDESC";
	public static String EQUIP_MAKE_ID = "EQMAKE";
	public static String EQUIP_ASSET_TYPE_DESC_ID = "AssetType";
	public static String EQUIP_MODEL_ID = "EQMODEL";
	public static String EQUIP_EQUIP_TYPE_ID = "EQEQMTTYPE";
	public static String CUSTOMER_TYPE_DESC_ID = "CUDEF1";

	public static String TERMS_TEXT = "";
	public static int CONTACT_DOC_TEXT_TYPE = 91;

	public static final String UNITOFMEASURE_DESC_TYPE_ID = "UnitOfMeasure";
	public static final String DRIVER_DESC_TYPE_ID = "DRIVERNAME";
	public static final String PRINTER_TYPE_DESC_TYPE_ID = "PRINTTYPE";
	public static final String DRIVER_BREAK_TYPE_ID = "BREAKTYPE";
	public static final String PACK_SIZE_DESC_TYPE_ID = "PackingSize";

	public static final String PRINT_DOCUMENT_ALHASA_TYPE_ID = "AL-HASA";
	public static final String AR_HEADER_PAID = "PAID";

	public static boolean gSignedOn = false;

	public static List<Fragment> gFragmentList = null;

	public static String PAYMENT_TYPE = "PaymentType";
	public static String PRICE_GROUP = "PriceGroup";
	public static String VISIT_FREQUENCY = "DeliveryFrequency";
	public static String TERMS = "TermsCode";

	public static boolean gbStopBackgroundUpload = false;
	public static boolean gbBackgroundUploading = false;

	public static String FLAG_MAKE_END_TRIP_DATA = "FlagMakeEndTripData";

	public static int WAKE_RESPONSE = 100020;
	public static		int		GPS_CUST_NO 					= -1;

	public static String SALES_DISTRICT = "SalesDistrict";

	public static final String  API_BASE_PATH = "https://dsdconnect%s.dsdassist.com/";
	public static final Map<String, String> URL_BASE_MAP = new HashMap<>();

	public static final String API_NEW_SERVER_ROOT = "https://installer.dsdassist.com/";
	public static final String API_OLD_SERVER_ROOT = "http://52.63.84.82:80/";
	public static String API_TOKEN = "";

	public static int MINT_IFRAME = 1023;

	public static String strVisitStart = "";
	public static int nSurveyCompleted = 0;
	public static int nAssetChecked = 0;
	public static int nAssetRequested = 0;
	public static boolean bOrderCreated = false;
	public static boolean gProcessedPayment = false;

	public static List<UStockDInfo> gStockDInfoList = new ArrayList<>();

	public static Hotspot hotspot;

	public static int WRITE_SETTINGS_PERMISSION = 10037;
	public static boolean gEndTripProcessing = false;
	public static BaseFragment startFragment = null;

	public static Map<String, ProdLocnInfo> prodLocnInfoMap = new HashMap<>();

	public static boolean bugSeeRecording = false;

	public static boolean bugSeeLaunch = false;

	public static long gStockDateTime = 0;
	public static long gAdjustDateTime = 0;

	public static String gShowImageUrl = null;
	public static CustomerNoteDialog gCustomerNoteDialog = null;
	public static int EDIT_PHOTO = 15002;

	public static String[] sortArray = {"ProdGroup", "MarketGroup", "PromoGroup", "ItemType", "ItemUPC", "Description", "ShortDesc", "Brand", "Item code"};

	public static UBillingDetailsInfo gUBillingDetailsInfo = null;
	public static CustomerInfo gCustomerTemplate = null;
	public static List<ContactDetail> gContactDetailList = null;
	public static ContactDetail gContactDetail = null;
	public static boolean bContactDetailCancelled = false;
	public static List<ContactInfo> gCustAdditionalInfoList = null;

	public static List<DescTypeInfo> gCurFilterBy = new ArrayList<>();

	public static Fragment gFragmentForCheckingModification = null;

	public static String strLoadPdfFileName = "";

	public static boolean bTransferLoad = false;

	public static List<Map<String, String>> gLoadAdjustmentReasonList = new ArrayList<>();
	public static List<String> gLoadAdjustmentPhotoList = new ArrayList<>();

	public static String APPLICATION_FOLDER_PATH = "";
	public static boolean g_bVisitTripSettlement = false;

	public static Map<String, Integer> pickedStatusMap = new HashMap<>();
}

