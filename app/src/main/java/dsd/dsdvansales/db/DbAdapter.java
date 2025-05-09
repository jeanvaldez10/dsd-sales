package dsd.dsdvansales.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.*;
import java.util.*;

import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.*;

public class DbAdapter {

	private static final int	DATABASE_VERSION			= 34;
	private static final String DATABASE_MASTER_TABLE		= "sqlite_master";
	private static final String DATABASE_DSD_DELIVERY		= "DSD_DELIVERY_INFO";

	// Tables
	private static final String TABLE_UGPS_INFO				= "UGPS_INFO";
	private static final String TABLE_UTRANSACTIONS_INFO	= "UTRANSACTIONS_INFO";
	private static final String TABLE_LOADDETL_INFO			= "LOADDETL_INFO";
	private static final String TABLE_LOADHEAD_INFO			= "LOADHEAD_INFO";
	private static final String TABLE_PRODDETL_INFO			= "PRODDETL_INFO";
	private static final String TABLE_UINVENH_INFO			= "UINVENH_INFO";
	private static final String TABLE_UINVEND_INFO			= "UINVEND_INFO";
	private static final String TABLE_MESSAGE_INFO			= "MESSAGE_INFO";
	private static final String TABLE_PRODSTRU_INFO			= "PRODSTRU_INFO";
	private static final String TABLE_DESCTYPE_INFO			= "DESCTYPE_INFO";
	private static final String TABLE_PRODLOCN_INFO			= "PRODLOCN_INFO";
	private static final String TABLE_UCAMERA_INFO			= "UCAMERA_INFO";
	private static final String TABLE_UMESSAGES_INFO		= "UMESSAGES_INFO";
	private static final String TABLE_SURVEY_INFO			= "SURVEY_INFO";
	private static final String TABLE_SURVEY_DIST_INFO		= "SURVEY_DIST_INFO";
	private static final String TABLE_SURVEY_HEAD_INFO		= "SURVEY_HEAD_INFO";
	private static final String TABLE_SURVQUES_INFO			= "SURVQUES_INFO";
	private static final String TABLE_SURVQOPT_INFO			= "SURVQOPT_INFO";
	private static final String TABLE_USURVEYS_INFO			= "USURVEYS_INFO";
	private static final String TABLE_USURVEY_DETAIL_INFO	= "USURVEY_DETAIL_INFO";
	private static final String TABLE_CUSTOMER_INFO			= "CUSTOMER_INFO";
	private static final String TABLE_CUSTCONTACT_INFO		= "CUSTCONTACT_INFO";
	private static final String TABLE_PRESOLDH_INFO			= "PRESOLDH_INFO";
	private static final String TABLE_PRESOLDD_INFO			= "PRESOLDD_INFO";
	private static final String TABLE_UORDERD_INFO			= "UORDERD_INFO";
	private static final String TABLE_UORDERH_INFO			= "UORDERH_INFO";
	private static final String TABLE_UTAX_INFO				= "UTAX_INFO";
	private static final String TABLE_TAXCODES_INFO			= "TAXCODES_INFO";
	private static final String TABLE_TAXRATES_INFO			= "TAXRATES_INFO";
	private static final String TABLE_PROMNOVO_INFO			= "PROMNOVO_INFO";
	private static final String TABLE_PRICING_INFO			= "PRICING_INFO";
	private static final String TABLE_PRICEGRP_INFO			= "PRICEGRP_INFO";
	private static final String TABLE_PROMASS_INFO			= "PROMASS_INFO";
	private static final String TABLE_PROMHEAD_INFO			= "PROMHEAD_INFO";
	private static final String TABLE_PROMULTI_INFO			= "PROMULTI_INFO";
	private static final String TABLE_UARTOTAL_INFO			= "UARTOTAL_INFO";
	private static final String TABLE_UARPAYMENT_INFO		= "UARPAYMENT_INFO";
	private static final String TABLE_USERVICES_INFO		= "USERVICES_INFO";
	private static final String TABLE_UCOLLECTIONSD_INFO	= "UCOLLECTIONSD_INFO";
	private static final String TABLE_DOCTEXT_INFO			= "DOCTEXT_INFO";
	private static final String TABLE_UCUSTOMERFM_INFO		= "UCUSTOMERFM_INFO";
	private static final String TABLE_CMPYCONTACT_INFO		= "CMPYCONTACT_INFO";
	private static final String TABLE_ARHEADER_INFO 		= "ARHEADER_INFO";
	private static final String TABLE_STATUS_INFO			= "STATUS_INFO";
	private static final String TABLE_USTOCKH_INFO			= "USTOCKH_INFO";
	private static final String TABLE_UCONSIGNMENT_INFO		= "UCONSIGNMENT_INFO";
	private static final String TABLE_USTOCKD_INFO			= "USTOCKD_INFO";
	private static final String TABLE_AUTHHEADER_INFO		= "AUTH_HEADER_INFO";
	private static final String TABLE_AUTHDETL_INFO			= "AUTH_DETL_INFO";
	private static final String TABLE_UFILETRANSACTION_INFO	= "UTRANSACTION_FILE_INFO";
	private static final String TABLE_UPLOAD_LIST			= "UPLOAD_LIST_INFO";
	private static final String	TABLE_LOGIN_INFO			= "LOGIN_INFO";
	private static final String	TABLE_UPROMOTION_INFO		= "UPROMOTION_INFO";
	private static final String TABLE_UORDERSTATUSH_INFO	= "UORDERSTATUSH_INFO";
	private static final String TABLE_UORDERSTATUSD_INFO	= "UORDERSTATUSD_INFO";
	private static final String TABLE_EQUIPMENT_INFO		= "EQUIPMENT_INFO";
	private static final String TABLE_EQUIP_ASS_INFO 		= "EQUIP_PASS_INFO";
	private static final String TABLE_EQUIP_COMPLETE_STATUS_INFO = "EQUIP_COMPLETE_STATUSINFO";
	private static final String TABLE_UASSETS_INFO			= "UASSETS_INFO";
	private static final String TABLE_UROUTESCHS_HEADER_INFO= "UROUTESCHS_HEADER_INFO";
	private static final String TABLE_UROUTESCHS_BODY_INFO	= "UROUTESCHS_BODY_INFO";
	private static final String TABLE_ORDER_HISTORY_INFO	= "ORDER_HISTORY_INFO";
	private static final String TABLE_TRIP_USER_INFO		= "TRIP_USER_INFO";
	private static final String TABLE_COLLECT_PAYMENT_STATUS = "COLLECT_PAYMENT_STATUS_INFO";
	private static final String TABLE_DAY_NO_INFO			= "DAY_NO_INFO";
    private static final String TABLE_WHITE_CUSTOMER_INFO   = "WHITE_CUSTOMER_INFO";
    private static final String TABLE_CUSTOMER_NOTE_INFO   	= "CUSTOMER_NOTE_INFO";
    private static final String TABLE_UCUSTOMER_NOTE_INFO  	= "UCUSTOMER_NOTE_INFO";
    private static final String TABLE_UATTACHMENT_INFO   	= "UATTACHMENT_INFO";
	private static final String TABLE_TRANSACTION_HISTORY_INFO	= "TRANSACTION_HISTORY_INFO";
	private static final String TABLE_SURVEY_COMPLETED		= "SURVEY_COMPLETED_INFO";
	private static final String TABLE_CUST_ADD_INFO			= "CUST_ADD_INFO";
	private static final String TABLE_UVISIT_INFO			= "UVISIT_INFO";
	private static final String TABLE_CUS_CON_BAL_INFO		= "CUS_CON_BAL_INFO";
	private static final String TABLE_LATEST_ORDER_INFO		= "LATEST_ORDER_INFO";
	private static final String TABLE_LANG_INFO				= "LANG_INFO";
	private static final String TABLE_PRINT_INFO			= "PRINT_INFO";
	private static final String TABLE_PROD_INFO				= "PROD_INFO";
	private static final String TABLE_LOAD_BLOT_INFO		= "LOAD_BLOT_INFO";
	private static final String TABLE_ULOADH_BLOT_INFO		= "LOAD_UBLOTH_INFO";
	private static final String TABLE_ULOADD_BLOT_INFO		= "LOAD_UBLOTD_INFO";
	private static final String TABLE_BATCH_LOT				= "BATCH_LOT_INFO";
	private static final String TABLE_UBILLING_DETAILS_INFO	= "UBILLING_DETAILS_INFO";
	private static final String TABLE_CONTACT_DETAIL_INFO	= "CONTACT_DETAIL_INFO";
	private static final String TABLE_CUSTOMER_ADDITIONAL_INFO	= "CUSTOMER_ADDTIONAL_INFO";


	// Key
	private static final String KEY_IDX						= "idx";
	private static final String KEY_CUST_NO					= "CustNo";
	private static final String KEY_ALT_CUST_NO				= "AltCustNo";
	private static final String KEY_CHAIN_NO				= "ChainNo";
	private static final String KEY_TRXN_NO					= "TrxnNo";
	private static final String KEY_START_TRXN_NO			= "StartTrxnNo";
	private static final String KEY_GPS_DATA				= "GPSData";
	private static final String KEY_TRXN_DATE				= "TrxnDate";
	private static final String KEY_TRXN_TIME				= "TrxnTime";
	private static final String KEY_DOC_TYPE				= "DocType";
	private static final String KEY_STOCK_TYPE				= "StockType";
	private static final String KEY_TRIP_NUMBER				= "TripNumber";
	private static final String KEY_DETAIL_FILE				= "DetailFile";
	private static final String KEY_ITEM_NO					= "ItemNo";
	private static final String KEY_FREE_ITEM_NO			= "FreeItemNo";
	private static final String KEY_ORDER_SEQ				= "OrderSeq";
	private static final String KEY_QTY						= "Qty";
	private static final String KEY_DQTY					= "DQty";
	private static final String KEY_PICK_LOCATION_CODE		= "PickLocationCode";
	private static final String KEY_SHOW_FLAG				= "ShowFlag";
	private static final String KEY_LOAD_DATE				= "LoadDate";
	private static final String KEY_DAY_NO					= "DayNo";
	private static final String KEY_DELIVERY_DATE			= "DeliveryDate";
	private static final String KEY_PERIOD_NO				= "PeriodNo";
	private static final String KEY_DELIVERY_SEQUENCE		= "DeliverySequence";
	private static final String KEY_ROUTE_NUMBER			= "RouteNumber";
	private static final String KEY_LOAD_TRIP_DATA_FLAG		= "LoadTripDataFlag";
	private static final String	KEY_LOAD_COMPLETED			= "LoadCompleted";
	private static final String KEY_DESCRIPTION				= "Description";
	private static final String KEY_FULL_DESCRIPTION		= "FullDescription";
	private static final String KEY_TYPE					= "Type";
	private static final String KEY_SHORT_DESC				= "ShortDesc";
	private static final String KEY_ITEM_UPC				= "ItemUPC";
	private static final String KEY_ITEM_UPC20				= "ItemUPC20";
	private static final String KEY_WEIGHT					= "Weight";
	private static final String KEY_ITEM_TYPE				= "ItemType";
	private static final String KEY_PROD_GRP				= "ProdGrp";
	private static final String KEY_MARKET_GRP				= "MarketGrp";
	private static final String KEY_PROMO_GRP				= "PromoGrp";
	private static final String KEY_PROD_LINE				= "ProdLine";
	private static final String KEY_PACK_SIZE				= "PackSize";
	private static final String KEY_SHELF_LIFE				= "ShelfLife";
	private static final String KEY_BRAND					= "Brand";
	private static final String KEY_SUB_BRAND				= "SubBrand";
	private static final String KEY_DUMPS_ALLOWED			= "DumpsAllowed";
	private static final String KEY_SALES_ALLOWED			= "SalesAllowed";
	private static final String KEY_ITEM_INACTIVE_SWITCH	= "ItemInactiveSwitch";
	private static final String KEY_ACTIVE_DATE				= "ActiveDate";
	private static final String KEY_GROUP_FILTER_KEY		= "GroupFilterKey";
	private static final String KEY_SERVICE_FEE				= "ServiceFee";
	private static final String KEY_MAX_QTY					= "MaxQty";
	private static final String KEY_LOADED					= "Loaded";
	private static final String KEY_STOCK					= "Stock";
	private static final String KEY_MESSAGE_ID				= "MessageID";
	private static final String KEY_SUBJECT					= "Subject";
	private static final String KEY_MESSAGE_TEXT			= "MessageText";
	private static final String KEY_IS_OLD_MESSAGE			= "IsOldMessage";
	private static final String KEY_ENTRY_ID				= "EntryID";
	private static final String KEY_PARENT_ID				= "ParentID";
	private static final String KEY_OBJECT_TYPE				= "ObjectType";
	private static final String KEY_GROUP_NO				= "GroupNo";
	private static final String KEY_REFERENCE				= "Reference";
	private static final String KEY_DESCRIPTION_ENTRY_KEY	= "DescriptionEntryKey";
	private static final String KEY_DESCRIPTION_TYPE_YN		= "SystemDescriptionTypeYN";
	private static final String KEY_DESCRIPTION_TYPE_ID		= "DescriptionTypeID";
	private static final String KEY_NUMERIC_CODE_YN			= "NumericCodeYN";
	private static final String KEY_NUMERIC_KEY				= "NumericKey";
	private static final String KEY_ALPHA_KEY				= "AlphaKey";
	private static final String KEY_ALTERNATE_SEQ			= "AlternateSeq";
	private static final String KEY_VALUE_1					= "Value1";
	private static final String KEY_VALUE_2					= "Value2";
	private static final String KEY_VALUE_3					= "Value3";
	private static final String KEY_VALUE_4					= "Value4";
	private static final String KEY_VALUE_5					= "Value5";
	private static final String KEY_VALUE_6					= "Value6";
	private static final String KEY_LOCN_NO					= "LocnNo";
	private static final String KEY_SCW_TYPE				= "SCWType";
	private static final String KEY_BASE_PRICE				= "BasePrice";
	private static final String KEY_DRIVER_PRICE			= "DriverPrice";
	private static final String KEY_CASE_FACTOR				= "CaseFactor";
	private static final String KEY_RETAIL_PRICE			= "RetailPrice";
	private static final String KEY_CUSTOMER_RETAIL_PRICE	= "CustomerRetailPrice";
	private static final String KEY_PRICE_OVERRIDE			= "PriceOverride";
	private static final String KEY_RETURN_PRICE			= "ReturnPrice";
	private static final String KEY_TAX_FLAG				= "TaxFlag";
	private static final String KEY_FULL_CASE				= "FullCase";
	private static final String KEY_PALLET_FACTOR			= "PalletFactor";
	private static final String KEY_STACK_FACTOR			= "StackFactor";
	private static final String KEY_CASE_UOM				= "CaseUOM";
	private static final String KEY_UNIT_UOM				= "UnitUOM";
	private static final String KEY_WEIGHT_TYPE				= "WeightType";
	private static final String KEY_ITEM_TAX_CODE			= "ItemTaxCode";
	private static final String KEY_CUST_DISCOUNT_VALID		= "CustDiscoufntValid";
	private static final String KEY_DISC_IN_LIEU_ITEM		= "DiscInLieuItem";
	private static final String KEY_LOOKUP_NO				= "LookupNo";
	private static final String KEY_LOOKUP_ID				= "LookupNoId";
	private static final String KEY_QUESTION_VAL			= "QuestionVal";
	private static final String KEY_BRANCH_QUESTION			= "BranchQuestion";
	private static final String KEY_SURVEY_ID				= "SurveyID";
	private static final String KEY_FROM_SURVEY_ID			= "FromSurveyID";
	private static final String KEY_SURVEY_TITLE			= "SurveyTitle";
	private static final String KEY_CREATE_DATE				= "CreateDate";
	private static final String KEY_COMPLETION_DATE			= "CompletionDate";
	private static final String KEY_SURVEY_RESPONSE			= "SurveyResponse";
	private static final String KEY_SURVEY_HEADER_ID		= "SurveyHeaderID";
	private static final String KEY_SURVEY_TYPE				= "SurveyType";
	private static final String KEY_QUESTION_ID				= "QuestionID";
	private static final String KEY_QUESTION_TEXT			= "QuestionText";
	private static final String KEY_QUESTION_TYPE			= "QuestionType";
	private static final String KEY_MULTI_RESP				= "MultiResp";
	private static final String KEY_IMAGE_CAPTURE_REQ		= "ImageCaptureReq";
	private static final String KEY_QUESTION_NUMBER_YES		= "QuestionNumberYes";
	private static final String KEY_QUESTION_NUMBER_NO		= "QuestionNumberNo";
	private static final String KEY_MANDATORY				= "Mandatory";
	private static final String KEY_STATUS					= "Status";
	private static final String KEY_COMPLETED				= "Completed";
	private static final String KEY_RESULT_VAL				= "ResultVal";
	private static final String KEY_VISIBLE					= "Visible";
	private static final String KEY_NAME					= "Name";
	private static final String KEY_COMPANY_TAX_ID			= "CompanyTaxID";
	private static final String KEY_CITY					= "City";
	private static final String KEY_ADDRESS1				= "Address1";
	private static final String KEY_ADDRESS2				= "Address2";
	private static final String KEY_PHONE					= "Phone";
	private static final String KEY_LOCATION				= "Location";
	private static final String KEY_PRIORITY				= "Priority";
	private static final String KEY_SEQ_NO					= "SeqNo";
	private static final String KEY_ZIP						= "Zip";
	private static final String KEY_IS_ROUTE_SCH			= "IsRouteSch";
	private static final String KEY_BUYER_NAME				= "BuyerName";
	private static final String KEY_BUYER_PHONE				= "BuyerPhone";
	private static final String KEY_ACCOUNT_NAME			= "AccountName";
	private static final String KEY_ACCOUNT_PHONE			= "AccountPhone";
	private static final String KEY_LATITUDE				= "Latitude";
	private static final String KEY_LONGITUDE				= "Longitude";
	private static final String KEY_PROMO_PLAN				= "PromoPlan";
	private static final String KEY_TEMPLATE_GRP			= "TemplateGrp";
	private static final String KEY_TEMP_GRP				= "TempGrp";
	private static final String KEY_FEAT_GRP				= "FeatGrp";
	private static final String KEY_PRICE_GRP				= "PriceGrp";
	private static final String KEY_ALT_PRICE_GRP			= "AltPriceGrp";
	private static final String KEY_TAX_CODE				= "TaxCode";
	private static final String KEY_START_TIME1				= "StartTime1";
	private static final String KEY_END_TIME1				= "EndTime1";
	private static final String KEY_START_TIME2				= "StartTime2";
	private static final String KEY_END_TIME2				= "EndTime2";
	private static final String KEY_CONTACT_TYPE			= "ContactType";
	private static final String KEY_CONTACT_TYPE_ID			= "ContactTypeId";
	private static final String KEY_CONTACT_TYPE_NUMERIC_KEY= "ContactTypeNumericKey";
	private static final String KEY_CONTACT_TYPE_DESC		= "ContactTypeDesc";
	private static final String KEY_CONTACT_NAME			= "ContactName";
	private static final String KEY_CONTACT_PHONE1			= "ContactPhone1";
	private static final String KEY_CONTACT_PHONE2			= "ContactPhone2";
	private static final String KEY_CONTACT_PHONE3			= "ContactPhone3";
	private static final String KEY_CONTACT_EMAIL1			= "ContactEmail1";
	private static final String KEY_CONTACT_EMAIL2			= "ContactEmail2";
	private static final String KEY_CONTACT_EMAIL3			= "ContactEmail3";
	private static final String KEY_CONTACT_PHONE_NUMBER	= "ContactPhoneNumber";
	private static final String KEY_CONTACT_EMAIL_ADDRESS	= "ContactEmailAddress";
	private static final String KEY_CONTACT_TRXNTIME		= "TrxnTime";
	private static final String KEY_CONTACT_TRXNDATE		= "TrxnDate";
	private static final String KEY_CONTACT_STARTTIME		= "StartTime";
	private static final String KEY_CONTACT_STARTDATE		= "StartDate";
	private static final String KEY_CONTACT_ENDTIME			= "EndTime";
	private static final String KEY_CONTACT_ENDDATE			= "EndDate";
	private static final String KEY_CONTACT_INFO			= "ContactInfo";
	private static final String KEY_CUST_INFO				= "CustInfo";
	private static final String KEY_ORDER_NO				= "OrderNo";
	private static final String KEY_QTY_OPTION				= "QtyOption";
	private static final String KEY_ORDER_QTY				= "OrderQty";
	private static final String KEY_SUGGESTED_QTY			= "SuggestedQty";
	private static final String KEY_REQUESTED_QTY			= "ReqeustedQty";
	private static final String KEY_POREF					= "PORef";
	private static final String KEY_CREDREF					= "CredRef";
	private static final String KEY_VISITED					= "Visited";
	private static final String KEY_SPECIAL_INSTRUCTIONS	= "SpecialInstructions";
	private static final String KEY_DISTRIBUTOR				= "Distributor";
	private static final String KEY_PO_REFERENCE			= "POReference";
	private static final String KEY_ORDER_NAME				= "OrderName";
	private static final String KEY_TOTAL_SAMPLES			= "TotalSamples";
	private static final String KEY_TOTAL_FREE				= "TotalFree";
	private static final String KEY_ORDER_TYPE				= "OrderType";
	private static final String KEY_TOTAL_DUMPS				= "TotalDumps";
	private static final String KEY_TOTAL_BUYBACKS			= "TotalBuybacks";
	private static final String KEY_DISC_SALES				= "DiscSales";
	private static final String KEY_DISC_DUMPS				= "DiscDumps";
	private static final String KEY_DISC_BUYBACKS			= "DiscBuyBacks";
	private static final String KEY_NET_SALES				= "NetSales";
	private static final String KEY_NET_DUMPS				= "NetDumps";
	private static final String KEY_NET_BUYBACKS			= "NetBuyBacks";
	private static final String KEY_SALE_AMOUNT				= "SaleAmount";
	private static final String KEY_TAX_AMOUNT				= "TaxAmount";
	private static final String KEY_PROMOTION_AMOUNT		= "PromotionAmount";
	private static final String KEY_TOTAL_AMOUNT			= "TotalAmount";
	private static final String KEY_TRXN_TYPE				= "TrxnType";
	private static final String KEY_QUANTITY				= "Quantity";
	private static final String KEY_REASON_CODE				= "ReasonCode";
	private static final String KEY_DISCOUNT_VAL			= "DiscountVal";
	private static final String KEY_PROMO_DISCOUNT			= "PromoDiscount";
	private static final String KEY_PROMO_TYPE				= "PromoType";
	private static final String KEY_PRICE					= "Price";
	private static final String KEY_ORG_PRICE				= "OrgPrice";
	private static final String KEY_GROSS_PRICE				= "GrossPrice";
	private static final String KEY_ORG_GROSS_PRICE			= "OrgGrossPrice";
	private static final String KEY_TAX_RATE_CODE			= "TaxRateCode";
	private static final String KEY_CUMULATIVE_FLAG			= "CumulativeFlag";
	private static final String KEY_TAX_CODE_SEQ			= "TaxCodeSeq";
	private static final String KEY_TAX_RATE				= "TaxRate";
	private static final String KEY_SIGNATURE_FILE_PATH		= "SignatureFilePath";
	private static final String KEY_START_DATE				= "StartDate";
	private static final String KEY_END_DATE				= "EndDate";
	private static final String KEY_RATE_DESCRIPTION		= "RateDescription";
	private static final String KEY_CUST_TAX_CODE			= "CustTaxCode";
	private static final String KEY_SALES					= "Sales";
	private static final String KEY_DUMPS					= "Dumps";
	private static final String KEY_BUYBACKS				= "Buybacks";
	private static final String KEY_ASSIGN_NO				= "AssignNo";
	private static final String KEY_DATE_START				= "DateStart";
	private static final String KEY_DATE_END				= "DateEnd";
	private static final String KEY_PRCFIL_RECORD			= "PrcFilRecord";
	private static final String KEY_PRICE_TYPE				= "PriceType";
	private static final String KEY_PROMO_APP_METHOD		= "PromoAppMethod";
	private static final String KEY_PROMO_METHOD			= "PromoMethod";
	private static final String KEY_PLAN_NO					= "PlanNo";
	private static final String KEY_PLAN_QTY				= "PlanQty";
	private static final String KEY_SHIP_TO_STATE			= "ShipToState";
	private static final String KEY_SHIP_TO_ZIP				= "ShipToZip";
	private static final String KEY_PRESOLD_CHGD			= "PresoldChgd";
	private static final String KEY_USER2					= "User2";
	private static final String KEY_USER1					= "User1";
	private static final String KEY_BAY						= "Bay";
	private static final String KEY_TOTAL_DUE				= "TotalDue";
	private static final String KEY_TOTAL_UNALLOC			= "TotalUnalloc";
	private static final String KEY_TOTAL_CASH				= "TotalCash";
	private static final String KEY_TOTAL_CHEQUE			= "TotalCheque";
	private static final String KEY_INVOICE_TRXN_NO			= "InvoiceTrxnNo";
	private static final String KEY_INVOICE_AMOUNT			= "InvoiceAmount";
	private static final String KEY_VISIT_DELIVERY			= "visit_delivery";
	private static final String KEY_VISIT_PICKUP			= "visit_pickup";
	private static final String KEY_TRXN_AMOUNT				= "TrxnAmount";
	private static final String KEY_INVOICE_NO				= "InvoiceNo";
	private static final String KEY_INVOICE_DATE			= "InvoiceDate";
	private static final String KEY_BANK_NO					= "BankNo";
	private static final String KEY_ACCOUNT					= "Account";
	private static final String KEY_CHECK_NO				= "CheckNo";
	private static final String KEY_CHECK_DATE				= "CheckDate";
	private static final String KEY_AR_NOTES				= "ARNotes";
	private static final String KEY_AR_PROCESS_STATUS		= "ARProcessStatus";
	private static final String KEY_PERIOD					= "Period";
	private static final String KEY_DONE					= "Done";
	private static final String KEY_REASON					= "Reason";
	private static final String KEY_TRANS_IN				= "TransIn";
	private static final String KEY_TRANS_OUT				= "TransOut";
	private static final String KEY_SPOILS					= "Spoils";
	private static final String KEY_UNLOAD					= "Unload";
	private static final String KEY_DUMPS_UNLOAD			= "DumpsUnload";
	private static final String KEY_END_ADJ					= "EndAdj";
	private static final String KEY_PAY_TYPE				= "PayType";
	private static final String KEY_TEXT_TYPE				= "TextType";
	private static final String KEY_LINE					= "Line";
	private static final String KEY_DOC_TEXT				= "DocText";
	private static final String KEY_SUB_TYPE				= "SubType";
	private static final String KEY_DOC_NO					= "DocNo";
	private static final String KEY_INVOICE_FMT				= "InvoiceFmt";
	private static final String KEY_CUST_PRICE_MAINT_FLAG	= "CustPriceMaintFlag";
	private static final String KEY_SHOW_PRICE				= "ShowPrice";
	private static final String	KEY_IMAGE_CAPTURE			= "ImageCapture";
	private static final String KEY_DELAYED_FLAG			= "DelayedFlag";
	private static final String KEY_EDIT_TYPE				= "EditType";
	private static final String KEY_BILL_NAME				= "BillName";
	private static final String KEY_BILL_ADDR1				= "BillAddr1";
	private static final String KEY_BILL_ADDR2				= "BillAddr2";
	private static final String KEY_BILL_CITY				= "BillCity";
	private static final String KEY_BILL_PERIOD				= "BillPeriod";
	private static final String KEY_BILL_CHAIN_NO			= "BillChainNo";
	private static final String KEY_BILL_CUST_NO			= "BillCustNo";
	private static final String KEY_VENDOR_NO				= "VendorNo";
	private static final String KEY_STATE					= "State";
	private static final String KEY_BILLING_NAME			= "BillingName";
	private static final String KEY_LEGAL_NAME				= "LegalName";
	private static final String KEY_CREDIT_LIMIT			= "CreditLimit";
	private static final String KEY_POST_CODE				= "PostCode";
	private static final String KEY_SIGNATURE				= "Signature";
	private static final String KEY_TERMS					= "Terms";
	private static final String KEY_SALENTRY_MODE			= "SalEntryMode";
	private static final String KEY_RTNENTRY_MODE			= "RtnEntryMode";
	private static final String KEY_DEFRRED_STATUS			= "DeferredStatus";
	private static final String KEY_COMPLETED_STATUS		= "CompletedStatus";
	private static final String KEY_LOAD_ADJ				= "LoadAdj";
	private static final String KEY_INSTRS					= "Instrs";
	private static final String KEY_VOID_FLAG				= "VoidFlag";
	private static final String KEY_PRINTED_FLAG			= "PrintedFlag";
	private static final String KEY_UPLOADED				= "Uploaded";
	private static final String KEY_CENTSROUND				= "CentsRound";
	private static final String KEY_PRINTED					= "Printed";
	private static final String KEY_MODIFIABLE				= "Modifiable";
	private static final String KEY_CUST_IDX				= "CustIdx";
	private static final String KEY_COMPANY_NUMBER			= "CompanyNumber";
	private static final String KEY_COMPANY_NAME			= "CompanyName";
	private static final String KEY_COMPANY_WEB				= "CompanyWeb";
	private static final String KEY_COMPANY_EMAIL			= "CompanyEmail";
	private static final String KEY_COMPANY_PHONE1			= "CompanyPhone1";
	private static final String KEY_COMPANY_PHONE2			= "CompanyPhone2";
	private static final String KEY_COMPANY_INFO			= "CompanyInfo";
	private static final String KEY_ARRIVAL_TIME			= "ArrivalTime";
	private static final String KEY_INV_DATE				= "InvDate";
	private static final String KEY_INV_NO					= "InvNo";
	private static final String KEY_AR_TRXN_TYPE			= "ArTrxnType";
	private static final String KEY_DISC_AMOUNT				= "DiscAmount";
	private static final String KEY_DISC_START_DATE			= "DiscStartDate";
	private static final String KEY_DISC_END_DATE			= "DiscEndDate";
	private static final String KEY_USER_1					= "User1";
	private static final String KEY_USER_2					= "User2";

	private static final String KEY_DRIVER_LATITUDE			= "DriverLatitude";
	private static final String KEY_DRIVER_LONGITUDE		= "DriverLongitude";

	private static final String KEY_DELIVERY_WINDOW_FROM	= "DeliveryWindowFrom";
	private static final String KEY_DELIVERY_WINDOW_TO		= "DeliveryWindowTo";

	private static final String KEY_CREDIT_HOLD				= "CreidtHold";
	private static final String KEY_DELIVERY_FREQ			= "DeliveryFreq";
	private static final String KEY_STORE_NO				= "StoreNo";
	private static final String KEY_DELIVERY_TYPE			= "DeliveryType";
	private static final String KEY_FROM_ADD				= "FromAdd";

	private static final String KEY_PAYMENT_STATUS			= "PaymentStatus";
	private static final String KEY_UPLOAD_STATUS			= "UploadStatus";
	private static final String KEY_PREPARE_STATUS			= "PrepareStatus";
	private static final String KEY_STATUS_CREATE_DATE		= "CreatedDate";
	private static final String KEY_STATUS_UPLOAD_DATE		= "UploadDate";
	private static final String KEY_STATUS_PAYMENT_DATE		= "PaymentDate";
	private static final String KEY_UPLOADED_STATUS			= "UploadedStatus";
	private static final String KEY_PREPARE_FILE_NAME		= "PrepareFileName";

	private static final String KEY_ACTUAL_QTY				= "ActualQty";
	private static final String KEY_BEGIN_QTY				= "BeginQty";
	private static final String KEY_SUGGEST_QTY				= "SuggestQty";
	private static final String KEY_DETAIL_TYPE				= "DetailType";

	private static final String KEY_AUTH_GRP				= "AuthGrp";
	private static final String KEY_AUTH_TYPE				= "AuthType";

	private static final String	KEY_FILE_TRXN_NO			= "FileTrxnNo";
	private static final String KEY_FILE_DOC_NO				= "FileDocNo";
	private static final String KEY_FILE_SHORT_DESCRIPTION 	= "FileShortDescription";
	private static final String KEY_FILE_LONG_DESCRIPTION	= "FileLongDescription";
	private static final String	KEY_FILE_CREATE_DATE		= "FileCreateDate";
	private static final String KEY_FILE_CREATE_TIME		= "FileCreateTime";
	private static final String KEY_FILE_FILE_NAME			= "FileFileName";
	private static final String KEY_DETAIL_KEY_EXPANDED 	= "DetailKeyExpanded";

	private static final String KEY_REQUIRED_DATE 			= "RequiredDate";

	private static final String KEY_LOCAL_PATH				= "LocalPath";
	private static final String KEY_SERVER_PATH				= "ServerPath";
	private static final String KEY_DEX_VERSION				= "DexVersion";
	private static final String KEY_ORDER_BARCODE			= "OrderBarCode";

	private static final String KEY_AMOUNT					= "Amount";
	private static final String KEY_PRICE_DISC_FLAG			= "PriceDiscFlag";

	private static final String	KEY_EQUIPMENT_NO			= "EquipmentNo";
	private static final String	KEY_SERIAL_NO				= "SerialNo";
	private static final String	KEY_MAKE					= "Make";
	private static final String	KEY_MODEL					= "Model";
	private static final String KEY_ASSET_TYPE				= "AssetType";
	private static final String	KEY_STATUS_CODE				= "StatusCode";
	private static final String	KEY_EQUIPMENT_TYPE			= "EquipmentType";
	private static final String	KEY_EQUIPMENT_ACT_TYPE		= "EquipmentActType";
	private static final String	KEY_OLD_EQUIPMENT_TYPE		= "EquipmentOldType";
	private static final String	KEY_OLD_EQUIPMENT_NO		= "EquipmentOldNo";
	private static final String	KEY_APPLICATION				= "Application";
	private static final String	KEY_DOLLAR_CAPACITY			= "DollarCapacity";
	private static final String KEY_LAST_REPAIR				= "LastRepair";
	private static final String KEY_TOTAL_SALES				= "TotalSales";
	private static final String KEY_TOTAL_RENT				= "TotalRent";
	private static final String KEY_ALT_EQUIPMENT			= "AltEquipment";
	private static final String KEY_RESPONSE				= "Response";

	private static final String	KEY_INSTALLED				= "Installed";
	private static final String	KEY_AGREEMENT_NO			= "AgreementNo";
	private static final String	KEY_AGREEMENT_DATE			= "AgreementDate";
	private static final String	KEY_OWNERSHIP				= "Ownership";
	private static final String	KEY_BILL_TYPE				= "BillType";
	private static final String	KEY_BILL_FREQ				= "BillFreq";
	private static final String	KEY_PAY_RENT_AMT			= "PayRentAmt";
	private static final String	KEY_VERIFY_FLAG				= "VerifyFlag";
	private static final String	KEY_VERIFIED				= "Verified";
	private static final String	KEY_VERIFIED_DATE			= "VerifiedDate";
	private static final String	KEY_REPAIRED_DATE			= "RepairedDate";
	private static final String	KEY_FEATURES				= "Features";
	private static final String	KEY_REPAIR_REASON			= "RepairReason";
	private static final String	KEY_EQUIPMENT_DOC			= "EquipmentDoc";
	private static final String	KEY_REPAIR_STATUS			= "RepairStatus";
	private static final String	KEY_REPAIR_TYPE				= "RepairType";
	private static final String	KEY_REPAIR_REQUESTED_PICKUP_DATE = "RepairRequestedPickupDate";
	private static final String	KEY_REPAIR_NOTES			= "RepairNotes";
	private static final String	KEY_REQUEST_DATE			= "RequestDate";
	private static final String	KEY_REPAIR_IMAGE			= "RepairImage";
	private static final String	KEY_NEW_PLACEMENT_DOC_PATH	= "NewPlacementDocPath";
	private static final String KEY_USER_CODE_1				= "UserCode1";
	private static final String KEY_ORG_SEQ_NO				= "OrgSeqNo";
	private static final String	KEY_CHANGE_SEQ_NO			= "ChangeSeqNo";

	private static final String	KEY_ORDER_DATE				= "OrderDate";
	private static final String KEY_SAQTY					= "SAQty";
	private static final String	KEY_BBQTY					= "BBQty";
	private static final String	KEY_DUQTY					= "DUQty";

	private static final String	KEY_BEGIN_PRESOLDD			= "BeginPresold";
	private static final String	KEY_BEGIN_PEDDLE			= "BeginPeddle";
	private static final String	KEY_RES_PRESOLD				= "ResPresold";
	private static final String	KEY_RES_PEDDLE				= "ResPeddle";

	private static final String KEY_DSTOCK					= "DStock";
	private static final String KEY_DBEGIN_QTY				= "DBeginQTY";
	private static final String KEY_DLOADED					= "DLoaded";
	private static final String KEY_DLOADADJ				= "DLoadedAdj";
	private static final String KEY_DADJ_QTY				= "DAdjQty";
	private static final String KEY_DSALES					= "DSales";
	private static final String KEY_DDUMPS					= "DDumps";
	private static final String KEY_DORDER_QTY				= "DOrderQty";
	private static final String KEY_DBEGIN_PRESOLDD			= "DBeginPresoldD";
	private static final String KEY_DBEGIN_PEDDLE			= "DBeginPeddle";
	private static final String KEY_DRES_PRESOLD			= "DResPresold";
	private static final String KEY_DRES_PEDDLE				= "DResPeddle";
	private static final String KEY_DACTUAL_QTY				= "DActualQty";
	private static final String KEY_DSUGGEST_QTY			= "DSuggestQty";
	private static final String KEY_DEND_ADJ				= "DEndAdj";
	private static final String KEY_DDUMPSUNLOAD			= "DDumpsUnload";
	private static final String KEY_DUNLOAD					= "DUnload";
	private static final String KEY_DBUYBACK				= "DBuyBack";
	private static final String KEY_DTRANS_IN				= "DTransIn";
	private static final String KEY_DTRANS_OUT				= "DTransOut";
	private static final String KEY_DSPOILS					= "DSpoils";
	private static final String KEY_DQUANTITY				= "DQuantity";
	private static final String KEY_DPLANQTY				= "DPlanQty";

	private static final String KEY_DTOTAL_AMOUNT			= "DTotalAmount";
	private static final String KEY_DTOTAL_DUMPS			= "DTotalDumps";
	private static final String KEY_DTOTAL_BUYBACKS			= "DTotalBuybacks";
	private static final String KEY_DTOTAL_FREE				= "DTotalFree";

	private static final String KEY_PRODLOCN_SCW_TYPE		= "ProdLocnSCWType";
	private static final String KEY_BATCH_NUMBER			= "BatchNumber";

	private static final String KEY_SECURITY				= "Security";
	private static final String KEY_USER_NAME				= "UserName";
	private static final String KEY_TRIP					= "Trip";

	private static final String KEY_ETADATE					= "ETADate";
	private static final String KEY_ETATIME					= "ETATime";

	private static final String KEY_PAY_METHOD				= "PayMethod";
	private static final String KEY_KIND					= "Kind";
	private static final String KEY_OTHER					= "Other";
	private static final String KEY_SOURCE_TYPE				= "SourceType";
	private static final String KEY_NETT_SPLIT				= "NettSplit";
	private static final String KEY_TRANSFER_ROUTE			= "TransferRoute";
	private static final String KEY_TO_WARE_HOUSE			= "ToWareHouse";
	private static final String KEY_SALES_DISTRICT			= "SalesDistrict";
	private static final String KEY_CON_SALES				= "ConSales";

	private static final String KEY_NOTE_TYPE				= "NoteType";
	private static final String KEY_NOTE_DATE				= "NoteDate";
	private static final String KEY_NOTE_TIME				= "NoteTime";
	private static final String KEY_NOTE_CREATED_BY			= "CreatedBy";
	private static final String KEY_NOTE					= "Note";
	private static final String KEY_NOTE_ID					= "NoteId";
	private static final String KEY_ATTACHMENT				= "Attachment";
	private static final String KEY_CREATED_TIME_AND_NOTE_DATE = "CreatedTimeAndNoteDate";
	private static final String KEY_SALES_DATE				= "SalesDate";
	private static final String KEY_USER					= "User";
	private static final String KEY_MESSAGE					= "Message";
	private static final String KEY_FILE_NAME				= "FileName";
	private static final String KEY_FILE_PATH				= "FilePath";
	private static final String KEY_END_TRIP				= "EndTrip";

	private static final String KEY_TAX_METHOD				= "TaxMethod";
	private static final String KEY_TAX_RATE_NUMBER			= "TaxRateNumber";
	private static final String KEY_TAX_TYPE				= "TaxType";
	private static final String KEY_CONSUMER_UNIT			= "ConsumerUnit";

	private static final String KEY_MINIMUM_ORDER_VALUE		= "MinimumOrderValue";
	private static final String KEY_HANDLING_FEE			= "HandlingFee";
	private static final String KEY_HANDLING_FEE_TAX_CODE	= "HandlingFeeTaxCode";
	private static final String KEY_HANDLING_FEE_TAX		= "HandlingFeeTax";
	private static final String KEY_DRIVER_NUMBER			= "DriverNumber";
	private static final String KEY_TRIP_ID					= "TripId";
	private static final String KEY_DRIVER_NAME				= "DriverName";
	private static final String KEY_DRIVER_ID				= "DriverID";

	private static final String KEY_INFO_TYPE				= "InfoType";
	private static final String KEY_INFO_DETAIL				= "InfoDetail";
	private static final String KEY_INFO					= "Info";

	private static final String KEY_STRIPE_INFO				= "StripeInfo";

	private static final String KEY_TCOMSTATUS				= "TComStatus";
	private static final String KEY_VISIT_START				= "VisitStart";
	private static final String KEY_VISIT_END				= "VisitEnd";
	private static final String KEY_VISIT_REASON			= "VisitReason";
	private static final String KEY_VISIT_REASON_ID			= "VisitReasonID";
	private static final String KEY_NEXT_VISIT				= "NextVisit";
	private static final String KEY_NEXT_VISIT_START		= "NextVisitStart";
	private static final String KEY_NEXT_VISIT_END			= "NextVisitEnd";
	private static final String KEY_VISIT_TYPE				= "VisitType";
	private static final String KEY_VISIT_TYPE_ID			= "VisitTypeID";
	private static final String KEY_VISIT_MESSAGE			= "VisitMessage";
	private static final String KEY_CUSTOMER_UPDATED		= "CustomerUpdated";
	private static final String KEY_RANGE_CHECKED			= "RangeChanged";
	private static final String KEY_ORDER_CREATED			= "OrderCreated";
	private static final String KEY_SURVEY_COMPLETED		= "SurveyCompleted";
	private static final String KEY_ASSET_CHECKED			= "AssetChecked";
	private static final String KEY_ASSET_REQUESTED			= "AssetRequested";
	private static final String KEY_VISIT_FREQUENCY			= "VisitFrequency";
	private static final String KEY_PREFERRED_VISIT_DAY		= "PreferredVisitDay";
	private static final String KEY_PLANNED_VISIT_TIME		= "PlannedVisitTime";
	private static final String KEY_ORDER_LINE				= "OrderLine";
	private static final String KEY_PURCHASE_ORDER_NUMBER	= "PurchaseOrderNo";
	private static final String KEY_DELIVERY_INSTRUCTIONS	= "DeliveryInstructions";
	private static final String KEY_INVOICE_DUE_DATE		= "InvoiceDueDate";
	private static final String KEY_INVOICE_DUE_METHOD		= "InvoiceDueMethod";
	private static final String KEY_PO_NUMBER_REQUIRED		= "PoNumberRequired";
	private static final String KEY_PO_NUMBER_MASK		= "PoNumberMask";

	private static final String KEY_VALUE   				= "Value";
	private static final String KEY_KEYWORD   				= "Keyword";
	private static final String KEY_LANG	   				= "Lang";

	private static final String KEY_VOID_TRXN_NO  			= "VoidTrxnNo";
	private static final String KEY_ORG_TRXN_NO  			= "OrgTrxnNo";
	private static final String KEY_IMAGE_URL  				= "ImageUrl";
	private static final String KEY_LOT_CONTROL  			= "LotControl";

	private static final String KEY_LOT_NUMBER				= "LotNumber";
	private static final String KEY_LOT_QTY					= "LotQty";
	private static final String KEY_LOT_WEIGHT				= "LotWeight";
	private static final String KEY_BATCH_DATE				= "BatchDate";
	private static final String KEY_BEGIN_STOCK				= "BeginStock";
	private static final String KEY_FREE					= "Free";
	private static final String KEY_SAMPLE					= "Sample";
	private static final String KEY_SEQUENCE				= "Sequence";
	private static final String KEY_LOAD_BLOT_DATA			= "LoadBlotData";
	private static final String KEY_TEMP_CHAIN_NO			= "TempChainNo";
	private static final String KEY_TEMP_CUST_NO			= "TempCustNo";
	private static final String KEY_DSD						= "DSD";


	private static final String CREATE_TABLE_COLLECT_PAYMENT_STATUS = "CREATE TABLE " 		+ TABLE_COLLECT_PAYMENT_STATUS + " (" +
															KEY_IDX				+ " integer primary key autoincrement, " +
															KEY_DOC_NO			+ " text, " +
															KEY_PAY_METHOD		+ " text, " +
															KEY_KIND			+ " text, " +
															KEY_OTHER			+ " text);";

	private static final String CREATE_TABLE_UPLOAD_LIST 	= "CREATE TABLE " 		+ TABLE_UPLOAD_LIST + " (" +
			KEY_IDX				+ " integer primary key autoincrement, " +
			KEY_LOCAL_PATH		+ " text, " +
			KEY_SERVER_PATH		+ " text);";

	private static final String CREATE_TABLE_UCUSTOMERFM	= "CREATE TABLE "		+ TABLE_UCUSTOMERFM_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_EDIT_TYPE		+ " text, " +
																KEY_NAME			+ " text, " +
																KEY_COMPANY_TAX_ID  + " text, " + 
																KEY_ADDRESS1		+ " text, " +
																KEY_ADDRESS2		+ " text, " +
																KEY_CITY			+ " text, " +
																KEY_SHIP_TO_STATE	+ " text, " +
																KEY_SHIP_TO_ZIP		+ " text, " +
																KEY_PHONE			+ " text, " +
																KEY_CONTACT_TYPE	+ " text, " +
																KEY_CONTACT_NAME	+ " text, " +
																KEY_CONTACT_PHONE_NUMBER	+ " text, " +
																KEY_CONTACT_EMAIL_ADDRESS	+ " text, " +
																KEY_CONTACT_STARTDATE	+ " text, " +
																KEY_CONTACT_STARTTIME	+ " text, " +
																KEY_CONTACT_ENDDATE	+ " text, " +
																KEY_CONTACT_ENDTIME	+ " text, " +
																KEY_CONTACT_TRXNDATE	+ " text, " +
																KEY_CONTACT_TRXNTIME	+ " text, " +
																KEY_DELIVERY_WINDOW_FROM+" text," +
																KEY_DELIVERY_WINDOW_TO	+" text," +
																KEY_STRIPE_INFO	+" text, " +
																KEY_TYPE	 + " text, " +
																KEY_USER1 	+ " text, " +
																KEY_TEMP_CHAIN_NO + " integer," +
																KEY_TEMP_CUST_NO + " integer);";


	private static final String CREATE_TABLE_DOCTEXT		= "CREATE TABLE "		+ TABLE_DOCTEXT_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TEXT_TYPE		+ " integer, " +
																KEY_LINE			+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_DOC_TEXT		+ " text, " +
																KEY_SUB_TYPE		+ " text);";

	private static final String CREATE_TABLE_UCOLLECTIONSD	= "CREATE TABLE "		+ TABLE_UCOLLECTIONSD_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_SEQ_NO			+ " integer, " +
																KEY_PAY_TYPE		+ " integer, " +
																KEY_NAME			+ " text, " +
																KEY_BANK_NO			+ " text, " +
																KEY_ACCOUNT			+ " text, " +
																KEY_CHECK_NO		+ " text, " +
																KEY_CHECK_DATE		+ " text, " +
																KEY_TRXN_AMOUNT		+ " float, " +
																KEY_EQUIPMENT_NO	+ " text, " +
																KEY_TRXN_DATE		+ " text, " +
																KEY_INVOICE_NO		+ " text);";

	private static final String CREATE_TABLE_USERVICES 		= "CREATE TABLE "		+ TABLE_USERVICES_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_PERIOD			+ " integer, " +
																KEY_SEQ_NO			+ " integer, " +
																KEY_DONE			+ " integer, " +
																KEY_REASON			+ " text, " +
																KEY_TRXN_DATE		+ " text, " +
																KEY_TRXN_TIME		+ " text, " +
																KEY_END_TRIP		+ " integer);";

	private static final String CREATE_TABLE_UARPAYMENT		= "CREATE TABLE "			+ TABLE_UARPAYMENT_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_TRXN_NO				+ " long, " +
																KEY_CHAIN_NO			+ " integer, " +
																KEY_CUST_NO				+ " integer, " +
																KEY_DESCRIPTION			+ " text, " +
																KEY_PAY_TYPE			+ " integer, " +
																KEY_TRXN_DATE			+ " text, " +
																KEY_TRXN_AMOUNT			+ " float, " +
																KEY_INVOICE_NO			+ " text, " +
																KEY_BANK_NO				+ " text, " +
																KEY_ACCOUNT				+ " text, " +
																KEY_CHECK_NO			+ " text, " +
																KEY_CHECK_DATE			+ " text, " +
																KEY_AR_NOTES			+ " text, " +
																KEY_AR_PROCESS_STATUS	+ " integer, " +
																KEY_INVOICE_DATE		+ " text, " +
																KEY_DOC_NO				+ " text);";

	private static final String CREATE_TABLE_UARTOTAL		= "CREATE TABLE "			+ TABLE_UARTOTAL_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_TRXN_NO				+ " long, " +
																KEY_CHAIN_NO			+ " integer, " +
																KEY_CUST_NO				+ " integer, " +
																KEY_TRXN_DATE			+ " text, " +
																KEY_TRXN_TIME			+ " text, " +
																KEY_TOTAL_DUE			+ " float, " +
																KEY_TOTAL_UNALLOC		+ " float, " +
																KEY_TOTAL_CASH			+ " float, " +
																KEY_TOTAL_CHEQUE		+ " float, " +
																KEY_INVOICE_TRXN_NO		+ " long, " +
																KEY_INVOICE_AMOUNT		+ " float, " +
																KEY_VISIT_DELIVERY		+ " integer, " + 
																KEY_VISIT_PICKUP		+ " integer, " +
																KEY_DOC_NO				+ " text, " +
																KEY_DOC_TYPE			+ " text, " +
																KEY_REFERENCE			+ " text);";

	private static final String CREATE_TABLE_PROMULTI		= "CREATE TABLE "			+ TABLE_PROMULTI_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_CHAIN_NO			+ " integer, " +
																KEY_CUST_NO				+ " integer, " +
																KEY_PLAN_NO				+ " integer, " +
																KEY_DATE_START			+ " text, " +
																KEY_DATE_END			+ " text, " +
																KEY_SEQ_NO				+ " integer);";

	private static final String CREATE_TABLE_PROMHEAD		= "CREATE TABLE "			+ TABLE_PROMHEAD_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_PLAN_NO				+ " integer, " +
																KEY_ASSIGN_NO			+ " integer, " +
																KEY_DATE_START			+ " text, " +
																KEY_DATE_END			+ " text, " +
																KEY_DESCRIPTION			+ " text);";

	private static final String CREATE_TABLE_PROMASS		= "CREATE TABLE "			+ TABLE_PROMASS_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_ASSIGN_NO			+ " integer, " +
																KEY_PROMO_APP_METHOD	+ " integer, " +
																KEY_PROMO_TYPE			+ " integer, " +
																KEY_PROMO_METHOD		+ " integer, " +
																KEY_DESCRIPTION			+ " text);";

	private static final String CREATE_TABLE_PRICEGRP		= "CREATE TABLE "			+ TABLE_PRICEGRP_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_PRICE_GRP			+ " integer, " +
																KEY_ITEM_NO				+ " text, " +
																KEY_DATE_START			+ " text, " +
																KEY_DATE_END			+ " text, " +
																KEY_PRICE				+ " integer, " +
																KEY_PRCFIL_RECORD		+ " text, " +
																KEY_PRICE_TYPE			+ " integer);";

	private static final String CREATE_TABLE_PRICING		= "CREATE TABLE "			+ TABLE_PRICING_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_CUST_NO				+ " integer, " +
																KEY_ITEM_NO				+ " text, " +
																KEY_DATE_START			+ " text, " +
																KEY_DATE_END			+ " text, " +
																KEY_PRICE				+ " integer, " +
																KEY_PRCFIL_RECORD		+ " text, " +
																KEY_PRICE_TYPE			+ " integer);";

	private static final String CREATE_TABLE_PROMNOVO		= "CREATE TABLE "			+ TABLE_PROMNOVO_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_ASSIGN_NO			+ " integer, " +
																KEY_ITEM_NO				+ " text, " +
																KEY_FREE_ITEM_NO		+ " text, " +
																KEY_PROMO_DISCOUNT		+ " integer);";

	private static final String CREATE_TABLE_TAXRATES		= "CREATE TABLE "			+ TABLE_TAXRATES_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_TAX_RATE_CODE		+ " text, " +
																KEY_START_DATE			+ " text, " +
																KEY_END_DATE			+ " text, " +
																KEY_TAX_RATE			+ " integer, " +
																KEY_RATE_DESCRIPTION	+ " text, " +
																KEY_TAX_METHOD			+ " integer, " +
																KEY_TAX_RATE_NUMBER		+ " text, " +
																KEY_TAX_TYPE			+ " text);";

	private static final String CREATE_TABLE_TAXCODES		= "CREATE TABLE "			+ TABLE_TAXCODES_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_CUST_TAX_CODE		+ " text, " +
																KEY_ITEM_TAX_CODE		+ " text, " +
																KEY_TAX_RATE_CODE		+ " text, " +
																KEY_CUMULATIVE_FLAG		+ " text, " +
																KEY_TAX_CODE_SEQ		+ " integer);";

	private static final String CREATE_TABLE_UTAX			= "CREATE TABLE "			+ TABLE_UTAX_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_TRXN_NO				+ " long, " +
																KEY_TRXN_TYPE			+ " integer, " +
																KEY_CUST_NO				+ " integer, " +
																KEY_LOCN_NO				+ " integer, " +
																KEY_ITEM_NO				+ " text, " +
																KEY_TAX_RATE_CODE		+ " text, " +
																KEY_TAX_AMOUNT			+ " float, " +
																KEY_CUMULATIVE_FLAG		+ " text, " +
																KEY_TAX_RATE			+ " integer, " +
																KEY_REASON_CODE			+ " integer, " +
																KEY_TAX_RATE_NUMBER		+ " text);";

	private static final String CREATE_TABLE_UORDERH		= "CREATE TABLE "			+ TABLE_UORDERH_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_TRXN_NO				+ " long, " +
																KEY_CHAIN_NO			+ " integer, " +
																KEY_CUST_NO				+ " integer, " +
																KEY_DOC_NO				+ " text, " +
																KEY_DOC_TYPE			+ " text, " +
																KEY_ORDER_NO			+ " text, " +
																KEY_DAY_NO				+ " integer, " +
																KEY_TRXN_DATE			+ " text, " +
																KEY_TRXN_TIME			+ " text, " +
																KEY_REFERENCE			+ " text, " +
																KEY_SPECIAL_INSTRUCTIONS+ " text, " +
																KEY_DISTRIBUTOR			+ " text, " +
																KEY_PO_REFERENCE		+ " text, " +
																KEY_DELIVERY_DATE		+ " text, " +
																KEY_ORDER_NAME			+ " text, " +
																KEY_PRESOLD_CHGD		+ " text, " +
																KEY_USER1				+ " text, " +
																KEY_USER2				+ " text, " +
																KEY_TOTAL_SAMPLES		+ " integer, " +
																KEY_TOTAL_FREE			+ " integer, " +
																KEY_ORDER_TYPE			+ " integer, " +
																KEY_TOTAL_DUMPS			+ " integer, " +
																KEY_TOTAL_BUYBACKS		+ " integer, " +
																KEY_DISC_SALES			+ " float, " +
																KEY_DISC_BUYBACKS		+ " float, " +
																KEY_DISC_DUMPS			+ " float, " +
																KEY_NET_SALES			+ " float, " +
																KEY_NET_DUMPS			+ " float, " +
																KEY_NET_BUYBACKS		+ " float, " +
																KEY_SALE_AMOUNT			+ " float, " +
																KEY_TAX_AMOUNT			+ " float, " +
																KEY_PROMOTION_AMOUNT	+ " float, " +
																KEY_SIGNATURE_FILE_PATH	+ " text, " +
																KEY_VOID_FLAG			+ " integer, " +
																KEY_PRINTED_FLAG		+ " integer, " +
																KEY_PRINTED				+ " integer, " +
																KEY_UPLOADED			+ " integer, " +
																KEY_TOTAL_AMOUNT		+ " integer, " +
																KEY_CENTSROUND			+ " integer, " +
																KEY_DTOTAL_AMOUNT		+ " double, " +
																KEY_DTOTAL_DUMPS		+ " double, " +
																KEY_DTOTAL_BUYBACKS		+ " double, " +
																KEY_DTOTAL_FREE			+ " double, " +
																KEY_PERIOD_NO			+ " integer, " +
																KEY_SOURCE_TYPE			+ " text, " +
																KEY_NETT_SPLIT			+ " text, " +
																KEY_HANDLING_FEE		+ " float, " +
																KEY_HANDLING_FEE_TAX_CODE + " text, " +
																KEY_HANDLING_FEE_TAX	+ " float, " +
																KEY_DRIVER_NUMBER		+ " text, " +
																KEY_VOID_TRXN_NO		+ " long, " +
																KEY_ORG_TRXN_NO			+ " long," +
																KEY_DSD					+ " text);";

	private static final String CREATE_TABLE_UORDERD		= "CREATE TABLE "			+ TABLE_UORDERD_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_TRXN_NO				+ " long, " +
																KEY_TRXN_TYPE			+ " integer, " +
																KEY_CUST_NO				+ " integer, " +
																KEY_LOCN_NO				+ " integer, " +
																KEY_ITEM_NO				+ " text, " +
																KEY_QUANTITY			+ " integer, " +
																KEY_RETAIL_PRICE		+ " integer, " +
																KEY_CASE_FACTOR			+ " integer, " +
																KEY_REASON_CODE			+ " integer, " +
																KEY_BASE_PRICE			+ " float, " +
																KEY_DISCOUNT_VAL		+ " float, " +
																KEY_TAX_AMOUNT			+ " float, " +
																KEY_PROMO_DISCOUNT		+ " float, " +
																KEY_PROMO_TYPE			+ " integer, " +
																KEY_DUMPS				+ " integer, " +
																KEY_SALES				+ " integer, " +
																KEY_DESCRIPTION			+ " text, " +
																KEY_SHORT_DESC			+ " text, " +
																KEY_CUSTOMER_RETAIL_PRICE + " float, " +
																KEY_PRICE_OVERRIDE 		+ " integer, " +
																KEY_PLAN_QTY			+ " integer, " +
																KEY_DQUANTITY			+ " double, " +
																KEY_DSALES				+ " double, " +
																KEY_DDUMPS				+ " double, " +
																KEY_DPLANQTY			+ " double, " +
																KEY_PRICE				+ " float, " +
																KEY_GROSS_PRICE			+ " float, " +
																KEY_ORG_GROSS_PRICE		+ " float, " +
																KEY_ORG_PRICE			+ " float, " +
																KEY_PROD_GRP			+ " text, " +
																KEY_LOAD_BLOT_DATA		+ " text);";

	private static final String CREATE_TABLE_PRESOLDD		= "CREATE TABLE "			+ TABLE_PRESOLDD_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_DETAIL_FILE			+ " text, " +
																KEY_ITEM_NO				+ " text, " +
																KEY_PICK_LOCATION_CODE	+ " text, " +
																KEY_QTY_OPTION			+ " integer, " +
																KEY_SUGGEST_QTY			+ " integer, " +
																KEY_REQUESTED_QTY 		+ " integer, " +
																KEY_ORDER_QTY			+ " integer, " +
																KEY_PRICE				+ " integer, " +
																KEY_TRXN_TYPE 			+ " text, " +
																KEY_REASON_CODE			+ " integer, " +
																KEY_DORDER_QTY 			+ " double)";

	private static final String CREATE_TABLE_PRESOLDH		= "CREATE TABLE "			+ TABLE_PRESOLDH_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_CHAIN_NO			+ " integer, " +
																KEY_CUST_NO				+ " integer, " +
																KEY_DAY_NO				+ " integer, " +
																KEY_DETAIL_FILE			+ " text, " +
																KEY_ORDER_NO			+ " text, " +
																KEY_TYPE				+ " text, " +
																KEY_POREF				+ " text, " +
																KEY_INSTRS				+ " text, " +
																KEY_CREDREF				+ " text, " + 
																KEY_QTY					+ " integer, " +
																KEY_MODIFIABLE			+ " text, " +
																KEY_DELIVERY_DATE		+ " text," +
																KEY_PERIOD_NO			+ " integer, " +
																KEY_DELIVERY_SEQUENCE 	+ " integer, " +
																KEY_VISITED				+ " integer, " +
																KEY_USER_1				+ " text, " +
																KEY_USER_2				+ " text, " +
 																KEY_ORDER_BARCODE		+ " text, " +
																KEY_CUST_IDX			+ " integer);";

	private static final String CREATE_TABLE_CUSTCONTACT	= "CREATE TABLE "				+ TABLE_CUSTCONTACT_INFO + " (" +
																KEY_IDX						+ " integer primary key autoincrement, " +
																KEY_CHAIN_NO				+ " integer, " +
																KEY_CUST_NO					+ " integer, " +
																KEY_CONTACT_TYPE			+ " integer, " +
																KEY_CONTACT_TYPE_DESC		+ " text, " +
																KEY_CONTACT_NAME			+ " text, " +
																KEY_CONTACT_PHONE_NUMBER	+ " text, " +
																KEY_CONTACT_INFO			+ " text, " +
																KEY_CONTACT_EMAIL_ADDRESS	+ " text);";

	private static final String CREATE_TABLE_CUSTOMER		= "CREATE TABLE "		+ TABLE_CUSTOMER_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_ALT_CUST_NO		+ " text, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_NAME			+ " text, " +
																KEY_COMPANY_TAX_ID	+ " text, " +
																KEY_CITY			+ " text, " +
																KEY_ADDRESS1		+ " text, " +
																KEY_ADDRESS2		+ " text, " +
																KEY_PHONE			+ " text, " +
																KEY_ZIP				+ " text, " +
																KEY_BUYER_NAME		+ " text, " +
																KEY_BUYER_PHONE		+ " text, " +
																KEY_ACCOUNT_NAME	+ " text, " +
																KEY_ACCOUNT_PHONE	+ " text, " +
																KEY_LOCATION		+ " integer, " +
																KEY_ROUTE_NUMBER	+ " integer, " +
																KEY_DAY_NO			+ " integer, " +
																KEY_SEQ_NO			+ " integer, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_IS_ROUTE_SCH	+ " integer, " +
																KEY_LATITUDE		+ " double, " +
																KEY_LONGITUDE		+ " double, " +
																KEY_COMPLETED		+ " integer, " +
																KEY_PROMO_PLAN		+ " integer, " +
																KEY_TEMPLATE_GRP	+ " integer, " +
																KEY_TEMP_GRP		+ " integer, " +
																KEY_FEAT_GRP		+ " integer, " +
																KEY_PRICE_GRP		+ " integer, " +
																KEY_ALT_PRICE_GRP	+ " integer, " +
																KEY_TAX_CODE		+ " text, " +
																KEY_START_TIME1		+ " text, " +
																KEY_END_TIME1		+ " text, " +
																KEY_START_TIME2		+ " text, " +
																KEY_END_TIME2		+ " text, " +
																KEY_DELIVERY_DATE	+ " text, " +
																KEY_SHIP_TO_STATE	+ " text, " +
																KEY_SHIP_TO_ZIP		+ " text, " +
																KEY_PERIOD_NO		+ " integer, " +
																KEY_ORDER_NO		+ " integer, " +
																KEY_INVOICE_FMT		+ " integer, " +
																KEY_CUST_PRICE_MAINT_FLAG	+ " integer, " +
																KEY_SHOW_PRICE		+ " integer, " +
																KEY_DELAYED_FLAG	+ " integer, " +
																KEY_PAY_TYPE		+ " text, " +
																KEY_BILL_NAME		+ " text, " +
																KEY_BILL_ADDR1		+ " text, " +
																KEY_BILL_ADDR2		+ " text, " +
																KEY_BILL_CITY		+ " text, " +
																KEY_BILL_PERIOD		+ " text, " +
																KEY_BILL_CHAIN_NO	+ " integer, " +
																KEY_BILL_CUST_NO	+ " integer, " +
																KEY_VENDOR_NO		+ " text," +
																KEY_STATE			+ " text, " +
																KEY_SIGNATURE		+ " text, " +
																KEY_PRIORITY		+ " text, " +
																KEY_SALENTRY_MODE   + " text, " +
																KEY_RTNENTRY_MODE   + " text, " +
																KEY_DUMPS_ALLOWED	+ " text, " +
																KEY_IMAGE_CAPTURE	+ " integer, " +
																KEY_COMPLETED_STATUS+ " integer, " +
																KEY_DEFRRED_STATUS	+ " integer, " +
																KEY_TERMS			+ " text, " +
																KEY_DEX_VERSION 	+ " text, " +
																KEY_ORG_SEQ_NO		+ " integer, " +
																KEY_ARRIVAL_TIME	+ " text, " +
																KEY_AUTH_GRP		+ " text, " +
																KEY_DRIVER_LATITUDE + " double, " +
																KEY_DRIVER_LONGITUDE+ " double, " +
																KEY_DELIVERY_WINDOW_FROM+" text, " +
																KEY_DELIVERY_WINDOW_TO+" text, " +
																KEY_CREDIT_HOLD		+ " integer, " +
																KEY_DELIVERY_FREQ	+ " integer, " +
																KEY_STORE_NO		+ " text, " +
																KEY_DELIVERY_TYPE + " text, " +
																KEY_FROM_ADD		+ " integer, " +
																KEY_MINIMUM_ORDER_VALUE	+ " float, " +
																KEY_HANDLING_FEE	+ " float, " +
																KEY_SALES_DISTRICT	+ " text, " +
																KEY_CON_SALES		+ " integer, " +
																KEY_TYPE			+ " text, " +
																KEY_USER1			+ " text, " +
																KEY_INVOICE_DUE_DATE	+ " text, " +
																KEY_INVOICE_DUE_METHOD	+ " text, " +
																KEY_PO_NUMBER_REQUIRED	+ " text, " +
																KEY_PO_NUMBER_MASK	+ " text, " +
																KEY_LEGAL_NAME + 	" text, " +
																KEY_CREDIT_LIMIT + 	" text);";

	private static final String CREATE_TABLE_USURVEYS 		= "CREATE TABLE "		+ TABLE_USURVEYS_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_SURVEY_ID		+ " text, " +
																KEY_FROM_SURVEY_ID	+ " integer, " +
																KEY_COMPLETED		+ " text, " +
																KEY_TRXN_DATE		+ " text, " +
																KEY_TRXN_TIME		+ " text, " +
																KEY_REFERENCE		+ " text);";

	private static final String CREATE_TABLE_USURVEY_DETAIL	= "CREATE TABLE "		+ TABLE_USURVEY_DETAIL_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_QUESTION_ID		+ " text, " +
																KEY_RESULT_VAL		+ " text, " +
																KEY_VISIBLE			+ " integer, " +
																KEY_SEQ_NO			+ " integer);";

	private static final String CREATE_TABLE_SURVQOPT 		= "CREATE TABLE "		+ TABLE_SURVQOPT_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_LOOKUP_NO		+ " integer, " +
																KEY_LOOKUP_ID		+ " text, " +
																KEY_QUESTION_VAL	+ " text, " +
																KEY_BRANCH_QUESTION	+ " integer);";

	private static final String CREATE_TABLE_SURVQUES 		= "CREATE TABLE "		+ TABLE_SURVQUES_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_SURVEY_ID		+ " text, " +
																KEY_QUESTION_ID		+ " text, " +
																KEY_QUESTION_TEXT	+ " text, " +
																KEY_QUESTION_TYPE	+ " text, " +
																KEY_MANDATORY		+ " text, " +
																KEY_LOOKUP_NO		+ " integer, " +
																KEY_MULTI_RESP		+ " text, " +
																KEY_IMAGE_CAPTURE_REQ	+ " integer, " +
																KEY_QUESTION_NUMBER_YES	+ " integer, " +
																KEY_QUESTION_NUMBER_NO	+ " integer);";

	private static final String CREATE_TABLE_SURVEY 		= "CREATE TABLE "		+ TABLE_SURVEY_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_SURVEY_ID		+ " text, " +
																KEY_FROM_SURVEY_ID 	+  " integer," +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_SURVEY_TITLE	+ " text, " +
																KEY_CREATE_DATE		+ " text, " +
																KEY_COMPLETION_DATE	+ " text, " +
																KEY_SURVEY_RESPONSE	+ " text, " +
																KEY_MANDATORY		+ " text, " +
																KEY_COMPLETED		+ " integer, " +
																KEY_STATUS			+ " integer, " +
																KEY_SURVEY_TYPE		+ " integer);";

	private static final String CREATE_TABLE_SURVEY_DIST	= "CREATE TABLE "		+ TABLE_SURVEY_DIST_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_SURVEY_HEADER_ID+ " text);";

	private static final String CREATE_TABLE_SURVEY_HEAD	= "CREATE TABLE "		+ TABLE_SURVEY_HEAD_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_SURVEY_HEADER_ID+ " text, " +
																KEY_FROM_SURVEY_ID 	+ " integer," +
																KEY_SURVEY_TITLE	+ " text, " +
																KEY_CREATE_DATE		+ " text, " +
																KEY_COMPLETION_DATE	+ " text, " +
																KEY_SURVEY_RESPONSE	+ " text, " +
																KEY_MANDATORY		+ " text, " +
																KEY_SURVEY_TYPE		+ " integer);";

	private static final String CREATE_TABLE_UMESSAGES 		= "CREATE TABLE "		+ TABLE_UMESSAGES_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_MESSAGE_ID		+ " integer, " +
																KEY_TRXN_DATE		+ " text, " +
																KEY_TRXN_TIME		+ " text);";

	private static final String CREATE_TABLE_UCAMERA		= "CREATE TABLE "		+ TABLE_UCAMERA_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_DOC_TYPE		+ " text, " +
																KEY_TRXN_DATE		+ " text, " +
																KEY_TRXN_TIME		+ " text, " +
																KEY_REFERENCE		+ " text);";

	private static final String CREATE_TABLE_PRODLOCN		= "CREATE TABLE "			+ TABLE_PRODLOCN_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_LOCN_NO				+ " integer, " +
																KEY_ITEM_NO				+ " text, " +
																KEY_SCW_TYPE			+ " integer, " +
																KEY_BASE_PRICE			+ " long, " +
																KEY_DRIVER_PRICE		+ " long, " +
																KEY_CASE_FACTOR			+ " integer, " +
																KEY_RETAIL_PRICE		+ " integer, " +
																KEY_RETURN_PRICE		+ " integer, " +
																KEY_TAX_FLAG			+ " text, " +
																KEY_FULL_CASE			+ " text, " +
																KEY_PALLET_FACTOR		+ " integer, " +
																KEY_STACK_FACTOR		+ " integer, " +
																KEY_CASE_UOM			+ " text, " +
																KEY_UNIT_UOM			+ " text, " +
																KEY_WEIGHT_TYPE			+ " text, " +
																KEY_ITEM_TAX_CODE		+ " text, " +
																KEY_CUST_DISCOUNT_VALID	+ " integer, " +
																KEY_ORDER_SEQ			+ " integer, " +
																KEY_DISC_IN_LIEU_ITEM	+ " text);";

	private static final String CREATE_TABLE_DESCTYPE 		= "CREATE TABLE "				+ TABLE_DESCTYPE_INFO + " (" +
																KEY_IDX						+ " integer primary key autoincrement, " +
																KEY_DESCRIPTION_ENTRY_KEY	+ " text, " +
																KEY_DESCRIPTION_TYPE_YN		+ " integer, " +
																KEY_DESCRIPTION_TYPE_ID		+ " text, " +
																KEY_NUMERIC_CODE_YN			+ " integer, " +
																KEY_NUMERIC_KEY				+ " integer, " +
																KEY_ALPHA_KEY				+ " text, " +
																KEY_DESCRIPTION				+ " text, " +
																KEY_ALTERNATE_SEQ			+ " integer," +
																KEY_OWNERSHIP				+ " text," +
																KEY_VALUE_1					+ " text, " +
																KEY_VALUE_2					+ " text, " +
																KEY_VALUE_3					+ " text, " +
																KEY_VALUE_4					+ " text, " +
																KEY_VALUE_5					+ " text, " +
																KEY_VALUE_6					+ " text);";

	private static final String CREATE_TABLE_PRODSTRU		= "CREATE TABLE "			+ TABLE_PRODSTRU_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_ENTRY_ID			+ " text, " +
																KEY_DESCRIPTION			+ " text, " +
																KEY_PARENT_ID			+ " text, " +
																KEY_OBJECT_TYPE			+ " text, " +
																KEY_REFERENCE			+ " text, " +
																KEY_GROUP_NO			+ " integer);";

	private static final String CREATE_TABLE_MESSAGE 		= "CREATE TABLE "		+ TABLE_MESSAGE_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_MESSAGE_ID		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_SUBJECT			+ " text, " +
																KEY_IS_OLD_MESSAGE	+ " integer, " +
																KEY_MESSAGE_TEXT	+ " text);";

	private static final String CREATE_TABLE_UINVEND		= "CREATE TABLE "		+ TABLE_UINVEND_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_ITEM_NO			+ " text, " +
																KEY_SALES			+ " integer, " +
																KEY_DUMPS			+ " integer, " +
																KEY_BUYBACKS		+ " integer, " +
																KEY_LOADED			+ " integer, " +
																KEY_LOAD_ADJ		+ " integer, " +
																KEY_STOCK			+ " integer, " +
																KEY_TRANS_IN		+ " integer, " +
																KEY_TRANS_OUT		+ " integer, " +
																KEY_SPOILS			+ " integer, " +
																KEY_UNLOAD			+ " integer, " +
																KEY_DUMPS_UNLOAD	+ " integer, " +
																KEY_END_ADJ			+ " integer, " +
																KEY_BEGIN_QTY		+ " integer, " +
																KEY_SHORT_DESC		+ " text, " +
																KEY_DESCRIPTION 	+ " text, " +
																KEY_PICK_LOCATION_CODE + " text, " +
																KEY_SHOW_FLAG		+ " integer, " +
																KEY_SCW_TYPE		+ " integer, " +
																KEY_BEGIN_PRESOLDD	+ " integer, " +
																KEY_BEGIN_PEDDLE	+ " integer, " +
																KEY_RES_PRESOLD		+ " integer, " +
																KEY_RES_PEDDLE		+ " integer, " +
																KEY_DSTOCK			+ " double, " +
																KEY_DBEGIN_QTY		+ " double, " +
																KEY_DLOADED			+ " double, " +
																KEY_DLOADADJ		+ " double, " +
																KEY_DADJ_QTY		+ " double, " +
																KEY_DSALES			+ " double, " +
																KEY_DDUMPS			+ " double, " +
																KEY_DBEGIN_PRESOLDD	+ " double, " +
																KEY_DBEGIN_PEDDLE	+ " double, " +
																KEY_DRES_PRESOLD	+ " double, " +
																KEY_DRES_PEDDLE		+ " double," +
																KEY_DEND_ADJ		+ " double," +
																KEY_DUNLOAD			+ " double, " +
																KEY_DDUMPSUNLOAD	+ " double, " +
																KEY_DBUYBACK		+ " double, " +
																KEY_DTRANS_IN		+ " double," +
																KEY_DTRANS_OUT		+ " double, " +
																KEY_DSPOILS			+ " double, " +
																KEY_PRODLOCN_SCW_TYPE + " integer, " +
																KEY_BATCH_NUMBER	+ " text, " +
																KEY_LOT_CONTROL		+" integer," +
																KEY_LOAD_BLOT_DATA	+ " text);";

	private static final String CREATE_TABLE_UINVENH		= "CREATE TABLE "		+ TABLE_UINVENH_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_TRXN_DATE		+ " text, " +
																KEY_TRXN_TIME		+ " text, " +
																KEY_DOC_TYPE		+ " text, " +
																KEY_REFERENCE		+ " text);";
	private static final String CREATE_TABLE_USTOCKH		= "CREATE TABLE "		+ TABLE_USTOCKH_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_TRXN_DATE		+ " text, " +
																KEY_TRXN_TIME		+ " text, " +
																KEY_REQUIRED_DATE	+ " text, " +
																KEY_DETAIL_KEY_EXPANDED + " text, " +
																KEY_PRINTED_FLAG	+ " integer, " +
																KEY_DOC_TYPE		+ " text, " +
																KEY_STOCK_TYPE		+ " text, " +
																KEY_DOC_NO			+ " text, " +
																KEY_REFERENCE		+ " text, " +
																KEY_TRANSFER_ROUTE	+ " text, " +
																KEY_TO_WARE_HOUSE	+ " text);";
	private static final String CREATE_TABLE_UCONSIGNMENT	= "CREATE TABLE "		+ TABLE_UCONSIGNMENT_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_TRXN_DATE		+ " text, " +
																KEY_TRXN_TIME		+ " text, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_REQUIRED_DATE	+ " text, " +
																KEY_DETAIL_KEY_EXPANDED + " text, " +
																KEY_PRINTED_FLAG	+ " integer, " +
																KEY_DOC_TYPE		+ " text, " +
																KEY_STOCK_TYPE		+ " text, " +
																KEY_DOC_NO			+ " text, " +
																KEY_REFERENCE		+ " text, " +
																KEY_TRANSFER_ROUTE	+ " text, " +
																KEY_TO_WARE_HOUSE	+ " text);";
	private static final String CREATE_TABLE_USTOCKD		= "CREATE TABLE "		+ TABLE_USTOCKD_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_ITEM_NO			+ " text, " +
																KEY_LOCN_NO			+ " integer, " +
																KEY_REASON_CODE		+ " integer, " +
																KEY_SEQ_NO			+ " integer, " +
																KEY_DETAIL_TYPE		+ " text, " +

																KEY_ACTUAL_QTY		+ " integer, " +
																KEY_BEGIN_QTY		+ " integer, " +
																KEY_SUGGEST_QTY		+ " integer," +

																KEY_DACTUAL_QTY		+ " double, " +
																KEY_DBEGIN_QTY		+ " double, " +
																KEY_DSUGGEST_QTY	+ " double);";

	private static final String CREATE_TABLE_PRODDETL		= "CREATE TABLE "			+ TABLE_PRODDETL_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_ITEM_NO				+ " text, " +
																KEY_DESCRIPTION			+ " text, " +
																KEY_SHORT_DESC			+ " text, " +
																KEY_ITEM_UPC			+ " text, " +
																KEY_ITEM_UPC20			+ " text, " +
																KEY_WEIGHT				+ " float, " +
																KEY_ITEM_TYPE			+ " text, " +
																KEY_PROD_GRP			+ " text, " +
																KEY_PROMO_GRP			+ " text, " +
																KEY_MARKET_GRP			+ " text, " +
																KEY_PROD_LINE			+ " text, " +
																KEY_PACK_SIZE			+ " text, " +
																KEY_SHELF_LIFE			+ " integer, " +
																KEY_BRAND				+ " text, " +
																KEY_SUB_BRAND			+ " text, " +
																KEY_TYPE				+ " text, " +
																KEY_ACTIVE_DATE			+ " text, " +
																KEY_DUMPS_ALLOWED		+ " text, " +
																KEY_SALES_ALLOWED		+ " text, " +
																KEY_ITEM_INACTIVE_SWITCH+ " text, " +
																KEY_GROUP_FILTER_KEY	+ " text, " +
																KEY_SERVICE_FEE			+ " text, " +
																KEY_MAX_QTY				+ " float, " +
																KEY_CONSUMER_UNIT		+ " integer," +
																KEY_IMAGE_URL			+ " text, " +
																KEY_LOT_CONTROL			+ " text);";

	private static final String CREATE_TABLE_PRODINFO			= "CREATE TABLE "			+ TABLE_PROD_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_ITEM_NO				+ " text, " +
																KEY_INFO_DETAIL			+ " text, " +
																KEY_INFO_TYPE			+ " text);";



	private static final String CREATE_TABLE_LOADHEAD		= "CREATE TABLE "			+ TABLE_LOADHEAD_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_DETAIL_FILE			+ " text, " +
																KEY_LOAD_DATE			+ " text, " +
																KEY_BAY					+ " text, " +
																KEY_USER1				+ " text, " +
																KEY_USER2				+ " text, " +
																KEY_DAY_NO				+ " integer, " +
																KEY_DELIVERY_DATE		+ " text, " +
																KEY_PERIOD_NO			+ " integer, " +
																KEY_ROUTE_NUMBER		+ " text, " +
																KEY_DESCRIPTION			+ " text, " +
																KEY_TYPE				+ " text, " +
																KEY_LOAD_TRIP_DATA_FLAG + " integer, " +
																KEY_LOAD_COMPLETED		+ " integer, " +
																KEY_TRIP_NUMBER			+ " text);";

	private static final String CREATE_TABLE_LOADDETL		= "CREATE TABLE "			+ TABLE_LOADDETL_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_DETAIL_FILE			+ " text, " +
																KEY_ITEM_NO				+ " text, " +
																KEY_QTY					+ " integer, " +
																KEY_DQTY				+ " double," +
																KEY_PICK_LOCATION_CODE	+ " text);";

	private static final String CREATE_TABLE_UTRANSACTIONS	= "CREATE TABLE "		+ TABLE_UTRANSACTIONS_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_DOC_TYPE		+ " text, " +
																KEY_TRXN_DATE		+ " text, " +
																KEY_TRXN_TIME		+ " text, " +
																KEY_REFERENCE		+ " text, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_TRIP_ID			+ " text, " +
																KEY_DRIVER_ID		+ " text, " +
																KEY_DRIVER_NUMBER	+ " text, " +
																KEY_DRIVER_NAME		+ " text);";

	private static final String CREATE_TABLE_UFILETRANSACTIONS	= "CREATE TABLE "		+ TABLE_UFILETRANSACTION_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_DOC_TYPE		+ " text, " +
																KEY_TRXN_DATE		+ " text, " +
																KEY_TRXN_TIME		+ " text, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_TRIP_NUMBER		+ " integer, " +
																KEY_FILE_TRXN_NO	+ " long, " +
																KEY_FILE_DOC_NO		+ " text, " +
																KEY_FILE_SHORT_DESCRIPTION + " text, " +
																KEY_FILE_LONG_DESCRIPTION + " text, " +
																KEY_FILE_CREATE_DATE + " text, " +
																KEY_FILE_CREATE_TIME + " text, " +
																KEY_FILE_FILE_NAME 	+ " text);";

	private static final String CREATE_TABLE_UGPS	 		= "CREATE TABLE "		+ TABLE_UGPS_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_START_TRXN_NO	+ " integer, " +
																KEY_GPS_DATA		+ " text, " +
																KEY_DOC_TYPE		+ " text, " +
																KEY_TRXN_DATE		+ " text, " +
																KEY_TRXN_TIME		+ " text, " +
																KEY_DOC_NO			+ " text);";

	private static final String CREATE_TABLE_CMPYCONTACT	= "CREATE TABLE "			+ TABLE_CMPYCONTACT_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_COMPANY_NUMBER		+ " integer, " +
																KEY_COMPANY_NAME		+ " text, " +
																KEY_COMPANY_WEB			+ " text, " +
																KEY_COMPANY_EMAIL		+ " text, " +
																KEY_COMPANY_PHONE1		+ " text, " +
																KEY_COMPANY_PHONE2		+ " text, " +
																KEY_COMPANY_INFO		+ " text);";
	
	private static final String CREATE_TABLE_ARHEADER	= "CREATE TABLE "			+ TABLE_ARHEADER_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_CHAIN_NO			+ " integer, " +
																KEY_CUST_NO				+ " integer, " +
																KEY_DESCRIPTION			+ " text, " +
																KEY_INV_DATE			+ " text, " +
																KEY_INV_NO				+ " integer, " +
																KEY_AR_TRXN_TYPE		+ " text, " +
																KEY_TRXN_AMOUNT			+ " integer, " +
																KEY_DISC_AMOUNT			+ " integer, " +
																KEY_DISC_START_DATE		+ " text, " +
																KEY_DISC_END_DATE		+ " text, " +
																KEY_AR_PROCESS_STATUS	+ " integer, " +
																KEY_USER_1				+ " text, " +
																KEY_USER_2		+ " text);";
	
	private static final String CREATE_TABLE_STATUS		= "CREATE TABLE "			+ TABLE_STATUS_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_CUST_NO				+ " integer, " +
																KEY_CHAIN_NO			+ " integer, " +
																KEY_PAYMENT_STATUS		+ " integer, " +
																KEY_UPLOAD_STATUS		+ " integer, " +
																KEY_PREPARE_STATUS		+ " integer, " +
																KEY_UPLOADED_STATUS		+ " integer, " +
																KEY_STATUS_CREATE_DATE	+ " text, " +
																KEY_STATUS_PAYMENT_DATE	+ " text, " +
																KEY_STATUS_UPLOAD_DATE	+ " text, " +
																KEY_PREPARE_FILE_NAME 	+ " text, " +
																KEY_INVOICE_NO 			+ " text);";

	private static final String CREATE_TABLE_AUTH_HEADER = "CREATE TABLE "			+ TABLE_AUTHHEADER_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_AUTH_GRP			+ " integer, " +
																KEY_AUTH_TYPE			+ " text);";

	private static final String CREATE_TABLE_AUTH_DETL = "CREATE TABLE "			+ TABLE_AUTHDETL_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_AUTH_GRP			+ " integer, " +
																KEY_ITEM_NO				+ " text, " +
																KEY_START_DATE			+ " text, " +
																KEY_END_DATE			+ " text);";

	private static final String CREATE_TABLE_LOGIN	 = "CREATE TABLE "			+ TABLE_LOGIN_INFO + " (" +
																KEY_IDX					+ " integer primary key autoincrement, " +
																KEY_TRXN_NO				+ " long);";

	private static final String CREATE_TABLE_UPROMOTION			= "CREATE TABLE " + TABLE_UPROMOTION_INFO + " (" +
																KEY_IDX 	+ " integer primary key autoincrement, " +
																KEY_TRXN_NO + " long, " +
																KEY_CHAIN_NO  + " integer, " +
																KEY_CUST_NO	+ " integer, " +
																KEY_SEQ_NO + " integer, " +
																KEY_ITEM_NO + " text, " +
																KEY_FREE_ITEM_NO + " text, " +
																KEY_PLAN_NO + " integer, " +
																KEY_ASSIGN_NO + " integer, " +
																KEY_TRXN_TYPE + " integer, " +
																KEY_AMOUNT	+ " integer, " +
																KEY_PRICE_DISC_FLAG + " integer, " +
																KEY_REASON_CODE + " integer, " +
																KEY_PROMO_APP_METHOD + " integer, " +
																KEY_PROMO_TYPE + " integer, " +
																KEY_DISC_AMOUNT + " integer, " +
																KEY_TRXN_AMOUNT + " integer, " +
																KEY_PROMO_METHOD + " integer, " +
																KEY_DATE_START + " text);";

	private static final String CREATE_TABLE_UORDERSTATUSH		= "CREATE TABLE " + TABLE_UORDERSTATUSH_INFO + " (" +
																KEY_IDX 	+ " integer primary key autoincrement, " +
																KEY_TRXN_NO + " long, " +
																KEY_DOC_NO + " text," +
																KEY_DOC_TYPE + " text," +
																KEY_TRXN_DATE + " text," +
																KEY_TRXN_TIME + " text);";

	private static final String CREATE_TABLE_UORDERSTATUSD		= "CREATE TABLE " + TABLE_UORDERSTATUSD_INFO + " (" +
																KEY_IDX 	+ " integer primary key autoincrement, " +
																KEY_TRXN_NO + " long, " +
																KEY_CHAIN_NO  + " integer, " +
																KEY_CUST_NO	+ " integer, " +
																KEY_DAY_NO	+ " integer," +
																KEY_DELIVERY_DATE	+ " text," +
																KEY_DELIVERY_SEQUENCE	+ " integer," +
																KEY_ORDER_NO	+ " text," +
																KEY_ORDER_BARCODE	+ " text," +
																KEY_POREF			+ " text," +
																KEY_STATUS + " integer, " +
																KEY_REFERENCE + " text);";
	private static final String CREATE_TABLE_EQUIP_PASS 	= "CREATE TABLE " 		+ TABLE_EQUIP_ASS_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_EQUIPMENT_NO	+ " text, " +
																KEY_INSTALLED		+ " text, " +
																KEY_AGREEMENT_NO	+ " text, " +
																KEY_AGREEMENT_DATE	+ " text, " +
																KEY_BILL_TYPE		+ " text, " +
																KEY_BILL_FREQ		+ " text, " +
																KEY_PAY_RENT_AMT	+ " integer, " +
																KEY_VERIFY_FLAG		+ " text, " +
																KEY_VERIFIED 		+ " text, " +
																KEY_REPAIR_REASON	+ " text," +
																KEY_REPAIR_NOTES	+ " text," +
																KEY_REPAIR_IMAGE	+ " text, " +
																KEY_REPAIR_STATUS	+ " text, " +
																KEY_REPAIR_TYPE		+ " text, " +
																KEY_REPAIR_REQUESTED_PICKUP_DATE		+ " text, " +
																KEY_STATUS			+ " text," +
																KEY_EQUIPMENT_DOC	+ " text," +
																KEY_OLD_EQUIPMENT_TYPE + " text," +
																KEY_OLD_EQUIPMENT_NO + " text," +
																KEY_FEATURES 		+ " text, " +
																KEY_VERIFIED_DATE	+ " text, " +
																KEY_REPAIRED_DATE	+ " text);";

	private static final String CREATE_TABLE_EQUIPMENT	 	= "CREATE TABLE " 		+ TABLE_EQUIPMENT_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_EQUIPMENT_NO	+ " text, " +
																KEY_SERIAL_NO		+ " text, " +
																KEY_MAKE			+ " text, " +
																KEY_MODEL			+ " text, " +
																KEY_DESCRIPTION		+ " text, " +
																KEY_ASSET_TYPE		+ " text, " +
																KEY_STATUS_CODE		+ " text, " +
																KEY_EQUIPMENT_TYPE	+ " text, " +
																KEY_EQUIPMENT_ACT_TYPE	+ " text, " +
																KEY_APPLICATION		+ " text, " +
																KEY_DOLLAR_CAPACITY + " text, " +
																KEY_LAST_REPAIR		+ " text, " +
																KEY_TOTAL_SALES		+ " integer, " +
																KEY_TOTAL_RENT		+ " integer, " +
																KEY_ALT_EQUIPMENT	+ " text, " +
																KEY_NEW_PLACEMENT_DOC_PATH + " text);";
	private static final String CREATE_TABLE_EQUIP_COMPLETE_STATUS = "CREATE TABLE " 		+ TABLE_EQUIP_COMPLETE_STATUS_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_COMPLETED		+ " integer);";
	private static final String CREATE_TABLE_UASSETS 		= "CREATE TABLE " + TABLE_UASSETS_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_EQUIPMENT_NO	+ " text, " +
																KEY_SERIAL_NO		+ " text, " +
																KEY_EQUIPMENT_TYPE	+ " text, " +
																KEY_EQUIPMENT_ACT_TYPE	+ " text, " +
																KEY_MAKE			+ " text, " +
																KEY_MODEL			+ " text, " +
																KEY_APPLICATION		+ " text, " +
																KEY_STATUS_CODE		+ " text, " +
																KEY_ASSET_TYPE		+ " text, " +
																KEY_REQUEST_DATE	+ " text, " +
																KEY_REPAIR_REASON	+ " text, " +
																KEY_EQUIPMENT_DOC	+ " text, " +
																KEY_REPAIR_STATUS	+ " text, " +
																KEY_REPAIR_TYPE		+ " text, " +
																KEY_REPAIR_REQUESTED_PICKUP_DATE		+ " text, " +
																KEY_DOC_NO			+ " text," +
																KEY_ALT_EQUIPMENT	+ " text," +
																KEY_RESPONSE		+ " text," +
																KEY_REPAIR_NOTES	+ " text);";

	private static final String CREATE_TABLE_UROUTESCH_HEADER	= "CREATE TABLE " + TABLE_UROUTESCHS_HEADER_INFO + " (" +
																KEY_IDX 	+ " integer primary key autoincrement, " +
																KEY_TRXN_NO + " long, " +
																KEY_DOC_TYPE  + " text, " +
																KEY_TRXN_DATE	+ " text, " +
																KEY_TRXN_TIME	+ " text);";
	private static final String CREATE_TABLE_UROUTESCH_BODY		= "CREATE TABLE " + TABLE_UROUTESCHS_BODY_INFO + " (" +
																KEY_IDX 			+ " integer primary key autoincrement, " +
																KEY_TRXN_NO 		+ " long, " +
																KEY_CHAIN_NO  		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_ORG_SEQ_NO		+ " integer," +
																KEY_CHANGE_SEQ_NO 	+ " integer, " +
																KEY_ETADATE			+ " text, " +
																KEY_ETATIME			+ " text);";
	private static final String CREATE_TABLE_ORDER_HISTORY		= "CREATE TABLE " 		+ TABLE_ORDER_HISTORY_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_ITEM_NO			+ " text, " +
																KEY_SAQTY			+ " integer, " +
																KEY_BBQTY			+ " integer, " +
																KEY_DUQTY			+ " integer, " +
																KEY_ORDER_DATE		+ " text);";

	private static final String CREATE_TABLE_TRIP_USER			= "CREATE TABLE "		+ TABLE_TRIP_USER_INFO + " (" +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_ROUTE_NUMBER			+ " long, " +
																KEY_DESCRIPTION		+ " integer, " +
																KEY_SECURITY			+ " integer, " +
																KEY_USER_NAME		+ " text, " +
																KEY_TRIP			+ " text);";

	private static final String CREATE_TABLE_DAY_NO				= "CREATE TABLE "	+ TABLE_DAY_NO_INFO + " ( " +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_DAY_NO			+ " integer);";

    private static final String CREATE_TABLE_WHITE_CUSTOMER		= "CREATE TABLE "	+ TABLE_WHITE_CUSTOMER_INFO + " ( " +
                                                                KEY_IDX				+ " integer primary key autoincrement, " +
                                                                KEY_CUST_NO         + " integer, " +
                                                                KEY_CHAIN_NO        + " integer, " +
                                                                KEY_DAY_NO			+ " integer);";

    private static final String CREATE_TABLE_CUSTOMER_NOTE		= "CREATE TABLE "	+ TABLE_CUSTOMER_NOTE_INFO + " ( " +
                                                                KEY_IDX				+ " integer primary key autoincrement, " +
                                                                KEY_CUST_NO         + " integer, " +
                                                                KEY_CHAIN_NO        + " integer, " +
                                                                KEY_NOTE_TYPE		+ " text, " +
																KEY_NOTE_DATE		+ " text, " +
																KEY_NOTE_TIME		+ " text, " +
																KEY_NOTE_CREATED_BY	+ " text, " +
																KEY_NOTE			+ " text, " +
																KEY_NOTE_ID			+ " text, " +
																KEY_ATTACHMENT		+ " integer," +
																KEY_CREATED_TIME_AND_NOTE_DATE + " text);";

	private static final String CREATE_TABLE_UCUSTOMER_NOTE		= "CREATE TABLE "	+ TABLE_UCUSTOMER_NOTE_INFO + " ( " +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_DOC_NO			+ " text, " +
																KEY_DOC_TYPE		+ " text, " +
																KEY_TRXN_DATE		+ " text, " +
																KEY_TRXN_TIME		+ " text, " +
																KEY_SALES_DATE		+ " text, " +
																KEY_CUST_NO         + " integer, " +
																KEY_CHAIN_NO        + " integer, " +
																KEY_NOTE_TYPE		+ " text, " +
																KEY_TRXN_TYPE		+ " text, " +
																KEY_USER			+ " text, " +
																KEY_MESSAGE			+ " text, " +
																KEY_ATTACHMENT		+ " integer, " +
																KEY_REFERENCE 		+ " text);";

	private static final String CREATE_TABLE_UATTACHMENT		= "CREATE TABLE "	+ TABLE_UATTACHMENT_INFO + " ( " +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_FILE_NAME		+ " text, " +
																KEY_FILE_PATH		+ " text);";

	private static final String CREATE_TABLE_TRANSACTION_HISTORY= "CREATE TABLE "	+ TABLE_TRANSACTION_HISTORY_INFO + " ( " +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_DOC_NO			+ " text, " +
																KEY_TRXN_TIME		+ " text, " +
																KEY_NAME			+ " text, " +
																KEY_TYPE			+ " text, " +
																KEY_VOID_FLAG		+ " text, " +
																KEY_TRXN_AMOUNT		+ " float);";

	private static final String CREATE_TABLE_SURVEY_COMPLETED	= "CREATE TABLE "	+ TABLE_SURVEY_COMPLETED + " ( " +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_SURVEY_ID		+ " text, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_CHAIN_NO		+ " integer);";

	private static final String CREATE_TABLE_CUST_ADD			= "CREATE TABLE "	+ TABLE_CUST_ADD_INFO + " ( " +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_INFO_TYPE		+ " text, " +
																KEY_INFO			+ " text, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_CHAIN_NO		+ " integer);";

	private static final String CREATE_TABLE_CUS_CON_BAL		= "CREATE TABLE "	+ TABLE_CUS_CON_BAL_INFO + " ( " +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_ITEM_NO			+ " text, " +
																KEY_QTY				+ " integer, " +
																KEY_WEIGHT			+ " float);";

	private static final String CREATE_TABLE_LATEST_ORDER_INFO	= "CREATE TABLE "	+ TABLE_LATEST_ORDER_INFO + " ( " +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_ORDER_TYPE		+ " text, " +
																KEY_ORDER_LINE		+ " text, " +
																KEY_ORDER_NO		+ " integer, " +
																KEY_STATUS			+ " integer, " +
																KEY_PURCHASE_ORDER_NUMBER	+ " text, " +
																KEY_DELIVERY_INSTRUCTIONS	+ " text);";

	private static final String CREATE_TABLE_UVISIT_INFO		= "CREATE TABLE "	+ TABLE_UVISIT_INFO	+ "( " +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_DOC_TYPE 		+ " text, " +
																KEY_CUST_NO 		+ " integer, " +
																KEY_CHAIN_NO 		+ " integer, " +
																KEY_TRXN_DATE 		+ " text, " +
																KEY_TRXN_TIME 		+ " text, " +
																KEY_VOID_FLAG 		+ " integer, " +
																KEY_PRINTED_FLAG 	+ " integer, " +
																KEY_REFERENCE 		+ " text, " +
																KEY_TCOMSTATUS 		+ " text, " +
																KEY_SALES_DATE 		+ " text, " +
																KEY_VISIT_START 	+ " text, " +
																KEY_VISIT_END 		+ " text, " +
																KEY_VISIT_REASON 	+ " text, " +
																KEY_VISIT_REASON_ID	+ " text, " +
																KEY_NEXT_VISIT 		+ " text, " +
																KEY_NEXT_VISIT_START+ " text, " +
																KEY_NEXT_VISIT_END 	+ " text, " +
																KEY_VISIT_MESSAGE 	+ " text, " +
																KEY_CUSTOMER_UPDATED	+ " integer, " +
																KEY_RANGE_CHECKED 	+ " integer, " +
																KEY_ORDER_CREATED 	+ " integer, " +
																KEY_SURVEY_COMPLETED	+ " integer, " +
																KEY_ASSET_CHECKED	+ " integer, " +
																KEY_ASSET_REQUESTED	+ " integer, " +
																KEY_VISIT_FREQUENCY	+ " integer, " +
																KEY_PREFERRED_VISIT_DAY	+ " integer, " +
																KEY_PLANNED_VISIT_TIME	+ " text, " +
																KEY_VISIT_TYPE + " integer, " +
																KEY_VISIT_TYPE_ID	+ " text); ";

	private static final String CREATE_TABLE_LANG				= "CREATE TABLE "	+ TABLE_LANG_INFO + " ( " +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_KEYWORD 		+ " text, " +
																KEY_LANG	 		+ " text, " +
																KEY_VALUE 			+ " text);";

	private static final String CREATE_TABLE_PRINT				= "CREATE TABLE "	+ TABLE_PRINT_INFO + " ( " +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_DOC_NO	 		+ " text, " +
																KEY_DOC_TYPE 		+ " text, " +
																KEY_FILE_PATH 		+ " text, " +
																KEY_NAME			+ " text," +
																KEY_INVOICE_AMOUNT	+ " float," +
																KEY_CUST_NO			+ " integer, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_TRXN_NO			+ " long);";

	private static final String CREATE_LOAD_BLOT				= "CREATE TABLE "	+ TABLE_LOAD_BLOT_INFO + " ( " +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_DETAIL_FILE		+ " text, " +
																KEY_ITEM_NO			+ " text, " +
																KEY_LOT_NUMBER 		+ " text, " +
																KEY_LOT_QTY			+ " integer," +
																KEY_LOT_WEIGHT		+ " float," +
																KEY_BATCH_DATE		+ " text);";

	private static final String CREATE_ULOADH_BLOT				= "CREATE TABLE "	+ TABLE_ULOADH_BLOT_INFO + " ( " +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_DOC_NO			+ " text, " +
																KEY_DOC_TYPE 		+ " text, " +
																KEY_VOID_FLAG		+ " integer," +
																KEY_PRINTED_FLAG	+ " integer," +
																KEY_TRXN_DATE		+ " text," +
																KEY_TRXN_TIME		+ " text," +
																KEY_REFERENCE		+ " text," +
																KEY_TCOMSTATUS		+ " text," +
																KEY_CHAIN_NO		+ " integer," +
																KEY_CUST_NO			+ " integer," +
																KEY_SALES_DATE		+ " text," +
																KEY_TRIP			+ " text);";

	private static final String CREATE_ULOADD_BLOT				= "CREATE TABLE "	+ TABLE_ULOADD_BLOT_INFO + " ( " +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_LOCN_NO			+ " integer, " +
																KEY_TRIP			+ " text," +
																KEY_ITEM_NO			+ " text," +
																KEY_LOT_NUMBER		+ " text," +
																KEY_BATCH_DATE		+ " text," +
																KEY_BEGIN_STOCK		+ " integer," +
																KEY_LOADED			+ " integer," +
																KEY_LOAD_ADJ		+ " integer," +
																KEY_TRANS_IN		+ " integer," +
																KEY_TRANS_OUT		+ " integer," +
																KEY_SPOILS			+ " integer," +
																KEY_DUMPS			+ " integer," +
																KEY_SALES			+ " integer," +
																KEY_FREE			+ " integer," +
																KEY_BUYBACKS		+ " integer," +
																KEY_UNLOAD			+ " integer," +
																KEY_DUMPS_UNLOAD	+ " integer," +
																KEY_END_ADJ			+ " integer," +
																KEY_STOCK			+ " integer," +
																KEY_SAMPLE			+ " integer);";

	private static final String CREATE_BATCH_LOT				= "CREATE TABLE "	+ TABLE_BATCH_LOT + " ( " +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_TRXN_NO			+ " long, " +
																KEY_DETAIL_TYPE		+ " text, " +
																KEY_ITEM_NO			+ " text, " +
																KEY_LOCN_NO			+ " integer," +
																KEY_REASON_CODE		+ " integer," +
																KEY_LOT_NUMBER		+ " text," +
																KEY_ACTUAL_QTY		+ " integer," +
																KEY_BEGIN_QTY		+ " integer," +
																KEY_SUGGEST_QTY		+ " integer," +
																KEY_SEQUENCE		+ " integer," +
																KEY_BATCH_DATE		+ " text);";

	private static final String CREATE_UBILLING_DETAILS_INFO	= "CREATE TABLE "	+ TABLE_UBILLING_DETAILS_INFO + " ( " +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_COMPANY_TAX_ID	+ " text, " +
																KEY_BILLING_NAME	+ " text," +
																KEY_ADDRESS1		+ " text," +
																KEY_ADDRESS2		+ " text," +
																KEY_CITY			+ " text," +
																KEY_STATE			+ " text," +
																KEY_POST_CODE		+ " text," +
																KEY_LEGAL_NAME		+ " text," +
																KEY_CREDIT_LIMIT	+ " text);";

	private static final String CREATE_CONTACT_DETAIL_INFO		= "CREATE TABLE "	+ TABLE_CONTACT_DETAIL_INFO + " ( " +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_CONTACT_TYPE	+ " text, " +
																KEY_CONTACT_TYPE_ID	+ " text, " +
																KEY_CONTACT_TYPE_NUMERIC_KEY + " integer, " +
																KEY_CONTACT_NAME	+ " text," +
																KEY_CONTACT_PHONE1	+ " text," +
																KEY_CONTACT_PHONE2	+ " text," +
																KEY_CONTACT_PHONE3	+ " text," +
																KEY_CONTACT_EMAIL1	+ " text," +
																KEY_CONTACT_EMAIL2	+ " text," +
																KEY_CONTACT_EMAIL3	+ " text," +
																KEY_CONTACT_INFO	+ " text);";

	private static final String CREATE_CUSTOMER_ADDITIONAL_INFO	= "CREATE TABLE "	+ TABLE_CUSTOMER_ADDITIONAL_INFO + " ( " +
																KEY_IDX				+ " integer primary key autoincrement, " +
																KEY_CHAIN_NO		+ " integer, " +
																KEY_CUST_NO			+ " integer, " +
																KEY_CUST_INFO		+ " text);";
	private static DatabaseHelper mDbHelper = null;
	private static SQLiteDatabase mDb = null;
	private Context mContext = null;

	private static class DatabaseHelper extends SQLiteOpenHelper {
		private SQLiteDatabase mMyDB = null;
		DatabaseHelper(Context mContext) {
			super(mContext, DATABASE_DSD_DELIVERY, null, DATABASE_VERSION);
			if(mMyDB == null)
			{
				mMyDB = SQLiteDatabase.openDatabase(Global.ROOT_DATABASE_LOCATION, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
				int currentDBVersion = mMyDB.getVersion();
				if (currentDBVersion < DATABASE_VERSION) {
					onUpgrade(mMyDB, currentDBVersion, DATABASE_VERSION);
				}
			}
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

		}


		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UGPS_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UTRANSACTIONS_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_LOADDETL_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_LOADHEAD_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODDETL_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_PROD_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UINVENH_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UINVEND_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_USTOCKH_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_USTOCKD_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODSTRU_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_DESCTYPE_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODLOCN_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UCAMERA_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UMESSAGES_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_SURVEY_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_SURVEY_DIST_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_SURVEY_HEAD_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_SURVQOPT_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_SURVQUES_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_USURVEYS_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_USURVEY_DETAIL_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMER_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTCONTACT_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_PRESOLDD_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_PRESOLDH_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UORDERH_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UORDERD_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UTAX_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_TAXRATES_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_TAXCODES_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_PRICING_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_PROMNOVO_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_PRICEGRP_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_PROMASS_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_PROMHEAD_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_PROMULTI_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UARTOTAL_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UARPAYMENT_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_USERVICES_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UCOLLECTIONSD_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCTEXT_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UCUSTOMERFM_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_CMPYCONTACT_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_ARHEADER_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_STATUS_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_AUTHHEADER_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_AUTHDETL_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UFILETRANSACTION_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UPROMOTION_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UORDERSTATUSH_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UORDERSTATUSD_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_EQUIP_ASS_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_EQUIPMENT_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_EQUIP_COMPLETE_STATUS_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UASSETS_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UROUTESCHS_HEADER_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UROUTESCHS_BODY_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_HISTORY_INFO);
			// We don't need to remove this to ensure normal login after upgrade
			// mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIP_USER_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_COLLECT_PAYMENT_STATUS);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_DAY_NO_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_WHITE_CUSTOMER_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMER_NOTE_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UCUSTOMER_NOTE_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UATTACHMENT_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION_HISTORY_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_SURVEY_COMPLETED);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_CUST_ADD_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UVISIT_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_CUS_CON_BAL_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UCONSIGNMENT_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_LATEST_ORDER_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_LANG_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_PRINT_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_LOAD_BLOT_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_ULOADD_BLOT_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_ULOADH_BLOT_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_BATCH_LOT);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UBILLING_DETAILS_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT_DETAIL_INFO);
			mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMER_ADDITIONAL_INFO);
			onCreate(db);
		}

		public void removeAllTables() {

		}
		
		public SQLiteDatabase getCustomDatabase()
		{
			return mMyDB;
		}
		
		@Override
		public void close()
		{
			super.close();
			mMyDB.close();
			mMyDB = null;
		}
	}

	public DbAdapter(Context mContext) {
		this.mContext = mContext;
	}

	public DbAdapter open() {
		while(mDb == null)
		{
			try
			{
				mDbHelper = new DatabaseHelper(mContext);
				mDb = mDbHelper.getCustomDatabase();
				Cursor cursor;

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_TRIP_USER_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_TRIP_USER);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_UGPS_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UGPS);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_UTRANSACTIONS_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UTRANSACTIONS);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_LOADDETL_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_LOADDETL);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_LOADHEAD_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_LOADHEAD);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_PRODDETL_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_PRODDETL);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_PROD_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_PRODINFO);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_UINVENH_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UINVENH);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_USTOCKH_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_USTOCKH);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_USTOCKD_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_USTOCKD);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_UINVEND_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UINVEND);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_MESSAGE_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_MESSAGE);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_PRODSTRU_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_PRODSTRU);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_DESCTYPE_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_DESCTYPE);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_PRODLOCN_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_PRODLOCN);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_UCAMERA_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UCAMERA);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_UMESSAGES_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UMESSAGES);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_SURVEY_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_SURVEY);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_SURVEY_DIST_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_SURVEY_DIST);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_SURVEY_HEAD_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_SURVEY_HEAD);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_SURVQUES_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_SURVQUES);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_SURVQOPT_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_SURVQOPT);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_USURVEYS_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_USURVEYS);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_USURVEY_DETAIL_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_USURVEY_DETAIL);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_CUSTOMER_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_CUSTOMER);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_CUSTCONTACT_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_CUSTCONTACT);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_PRESOLDD_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_PRESOLDD);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_PRESOLDH_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_PRESOLDH);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_UORDERH_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UORDERH);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_UORDERD_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UORDERD);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_UTAX_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UTAX);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_TAXRATES_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_TAXRATES);
				}
				cursor.close();

				String sql =	" SELECT	* " +
						" FROM		" + TABLE_TAXRATES_INFO +
						" WHERE		" + KEY_TAX_RATE_CODE + " = 'GST'";

				cursor = mDb.rawQuery(sql, null);
				int nCount = cursor.getCount();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_TAXCODES_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_TAXCODES);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_PRICING_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_PRICING);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_PROMNOVO_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_PROMNOVO);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_PRICEGRP_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_PRICEGRP);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_PROMASS_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_PROMASS);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_PROMHEAD_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_PROMHEAD);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_PROMULTI_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_PROMULTI);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_UARTOTAL_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UARTOTAL);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_UARPAYMENT_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UARPAYMENT);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_USERVICES_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_USERVICES);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_UCOLLECTIONSD_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UCOLLECTIONSD);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_DOCTEXT_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_DOCTEXT);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[]{"name"}, "name='" + TABLE_UCUSTOMERFM_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UCUSTOMERFM);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_CMPYCONTACT_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_CMPYCONTACT);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_ARHEADER_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_ARHEADER);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_STATUS_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_STATUS);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_AUTHHEADER_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_AUTH_HEADER);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_AUTHDETL_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_AUTH_DETL);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_UFILETRANSACTION_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UFILETRANSACTIONS);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_UPLOAD_LIST + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UPLOAD_LIST);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_LOGIN_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_LOGIN);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_UPROMOTION_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UPROMOTION);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_UORDERSTATUSH_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UORDERSTATUSH);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_UORDERSTATUSD_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UORDERSTATUSD);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_EQUIP_ASS_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_EQUIP_PASS);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_EQUIPMENT_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_EQUIPMENT);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_EQUIP_COMPLETE_STATUS_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_EQUIP_COMPLETE_STATUS);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_UASSETS_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UASSETS);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_UROUTESCHS_HEADER_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UROUTESCH_HEADER);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_UROUTESCHS_BODY_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UROUTESCH_BODY);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_ORDER_HISTORY_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_ORDER_HISTORY);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_COLLECT_PAYMENT_STATUS + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_COLLECT_PAYMENT_STATUS);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_DAY_NO_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_DAY_NO);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_WHITE_CUSTOMER_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_WHITE_CUSTOMER);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_CUSTOMER_NOTE_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_CUSTOMER_NOTE);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_UCUSTOMER_NOTE_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UCUSTOMER_NOTE);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_UATTACHMENT_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UATTACHMENT);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_TRANSACTION_HISTORY_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_TRANSACTION_HISTORY);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_SURVEY_COMPLETED + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_SURVEY_COMPLETED);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_CUST_ADD_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_CUST_ADD);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_UVISIT_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UVISIT_INFO);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_CUS_CON_BAL_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_CUS_CON_BAL);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_UCONSIGNMENT_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_UCONSIGNMENT);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_LATEST_ORDER_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_LATEST_ORDER_INFO);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_LANG_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_LANG);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_PRINT_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_TABLE_PRINT);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_LOAD_BLOT_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_LOAD_BLOT);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_ULOADH_BLOT_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_ULOADH_BLOT);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_ULOADD_BLOT_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_ULOADD_BLOT);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_BATCH_LOT + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_BATCH_LOT);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_UBILLING_DETAILS_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_UBILLING_DETAILS_INFO);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_CONTACT_DETAIL_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_CONTACT_DETAIL_INFO);
				}
				cursor.close();

				cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_CUSTOMER_ADDITIONAL_INFO + "'", null, null, null, null, null);
				if (cursor.getCount() == 0) {
					mDb.execSQL(CREATE_CUSTOMER_ADDITIONAL_INFO);
				}
				cursor.close();

				mDb.setVersion(DATABASE_VERSION);

			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		return this;
	}

	public void beginTransacion()
	{
		if( mDb != null )
		{
			mDb.beginTransaction();
		}
	}

	public void endTransaction()
	{
		if(mDb != null )
		{
			mDb.setTransactionSuccessful();
			mDb.endTransaction();
		}
	}


	public void close() {

		/*mDbHelper.close();
		mDb = null;
		mDbHelper = null;*/
	}

	public void closeAll()
	{
		if(mDbHelper != null)
		{
			mDbHelper.close();
			mDb = null;
			mDbHelper = null;
		}
	}


	// UGPSInfo
	public List<UGPSInfo> getUGPSList(int nCustNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_UGPS_INFO +
						" WHERE		" + KEY_CUST_NO + " = " + nCustNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UGPSInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UGPSInfo info = new UGPSInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nStartTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_START_TRXN_NO));
					info.strGPSData = cursor.getString(cursor.getColumnIndex(KEY_GPS_DATA));
					info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<UGPSInfo> getUGPSList(MakeZipFileInfo zipFileInfo) {
		List<UGPSInfo> list = new ArrayList<>();
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			String sql =	" SELECT	* " +
					" FROM		" + TABLE_UGPS_INFO +
					" WHERE		" + KEY_TRXN_NO + " = " + nTrxnNo;
			Cursor cursor = null;

			try {
				cursor = mDb.rawQuery(sql, null);
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						UGPSInfo info = new UGPSInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
						info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
						info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
						info.nStartTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_START_TRXN_NO));
						info.strGPSData = cursor.getString(cursor.getColumnIndex(KEY_GPS_DATA));
						info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
						info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
						info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
						info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
						cursor.moveToNext();
						list.add(info);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		return list;
	}


	public long insertUGPSInfo(UGPSInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_START_TRXN_NO, info.nStartTrxnNo);
		values.put(KEY_GPS_DATA, info.strGPSData);
		values.put(KEY_DOC_TYPE, info.strDocType);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		values.put(KEY_DOC_NO, info.strDocNo);
		return mDb.insert(TABLE_UGPS_INFO, null, values);
	}

	private boolean deleteAllUGPSInfo(int nCustNo) throws SQLException {
		return mDb.delete(TABLE_UGPS_INFO, KEY_CUST_NO + " = " + nCustNo, null) > 0;
	}

	private boolean deleteAllUGPSInfo(int nCustNo, MakeZipFileInfo zipFileInfo) throws SQLException {
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			mDb.delete(TABLE_UGPS_INFO, KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_TRXN_NO + " = " + nTrxnNo, null);
		}
		return true;
	}

	// UTransactionsInfo
	public List<UTransactionsInfo> getUTransactionsList(int nCustNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_UTRANSACTIONS_INFO +
						" WHERE		" + KEY_CUST_NO + " = " + nCustNo;
		Cursor cursor = null;
		List<UTransactionsInfo> list = new ArrayList<>();
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UTransactionsInfo info = new UTransactionsInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strDriverId = cursor.getString(cursor.getColumnIndex(KEY_DRIVER_ID));
					info.strTripId = cursor.getString(cursor.getColumnIndex(KEY_TRIP_ID));
					info.strDriverName = cursor.getString(cursor.getColumnIndex(KEY_DRIVER_NAME));
					info.strDriverNumber = cursor.getString(cursor.getColumnIndex(KEY_DRIVER_NUMBER));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	// UTransactionsInfo
	public List<UTransactionsInfo> getUTransactionsList(MakeZipFileInfo zipFileInfo) {
		List<UTransactionsInfo> list = new ArrayList<>();
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			String sql =	" SELECT	* " +
					" FROM		" + TABLE_UTRANSACTIONS_INFO +
					" WHERE		" + KEY_TRXN_NO + " = " + nTrxnNo;
			Cursor cursor = null;

			try {
				cursor = mDb.rawQuery(sql, null);
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						UTransactionsInfo info = new UTransactionsInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
						info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
						info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
						info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
						info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
						info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
						info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
						info.strDriverId = cursor.getString(cursor.getColumnIndex(KEY_DRIVER_ID));
						info.strTripId = cursor.getString(cursor.getColumnIndex(KEY_TRIP_ID));
						info.strDriverName = cursor.getString(cursor.getColumnIndex(KEY_DRIVER_NAME));
						info.strDriverNumber = cursor.getString(cursor.getColumnIndex(KEY_DRIVER_NUMBER));
						cursor.moveToNext();
						list.add(info);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		return list;
	}

	public long insertUTransactionsInfo(UTransactionsInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_DOC_TYPE, info.strDocType);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		values.put(KEY_REFERENCE, info.strReference);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_TRIP_ID, info.strTripId);
		values.put(KEY_DRIVER_ID, info.strDriverId);
		values.put(KEY_DRIVER_NAME, info.strDriverName);
		values.put(KEY_DRIVER_NUMBER, info.strDriverNumber);
		return mDb.insert(TABLE_UTRANSACTIONS_INFO, null, values);
	}

	private boolean deleteAllUTransactionsInfo(int nCustNo) throws SQLException {
		return mDb.delete(TABLE_UTRANSACTIONS_INFO, KEY_CUST_NO + " = " + nCustNo, null) > 0;
	}

	private boolean deleteAllUTransactionsInfo(int nCustNo, MakeZipFileInfo zipFileInfo) throws SQLException {

		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			mDb.delete(TABLE_UTRANSACTIONS_INFO, KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_TRXN_NO + " = " + nTrxnNo, null);
		}
		return true;
	}

	// UFileTransactionInfo
	public List<UFileTransactionInfo> getUFileTransactionsList(int nCustNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_UFILETRANSACTION_INFO +
						" WHERE		" + KEY_CUST_NO + " = " + nCustNo;
		Cursor cursor = null;
		List<UFileTransactionInfo> list = new ArrayList<>();
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UFileTransactionInfo info = new UFileTransactionInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nTripNo = cursor.getInt(cursor.getColumnIndex(KEY_TRIP_NUMBER));
					info.nFileTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_FILE_TRXN_NO));
					info.strFileDocNo = cursor.getString(cursor.getColumnIndex(KEY_FILE_DOC_NO));
					info.strFileShortDescription = cursor.getString(cursor.getColumnIndex(KEY_FILE_SHORT_DESCRIPTION));
					info.strFileLongDescription = cursor.getString(cursor.getColumnIndex(KEY_FILE_LONG_DESCRIPTION));
					info.strFileCreateDate = cursor.getString(cursor.getColumnIndex(KEY_FILE_CREATE_DATE));
					info.strFileCreateTime = cursor.getString(cursor.getColumnIndex(KEY_FILE_CREATE_TIME));
					info.strFileFileName = cursor.getString(cursor.getColumnIndex(KEY_FILE_FILE_NAME));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	// UFileTransactionInfo
	public List<UFileTransactionInfo> getUFileTransactionsList(int nCustNo, MakeZipFileInfo zipFileInfo) {
		List<UFileTransactionInfo> list = new ArrayList<>();
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			String sql =	" SELECT	* " +
					" FROM		" + TABLE_UFILETRANSACTION_INFO +
					" WHERE		" + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_TRXN_NO + " = " + nTrxnNo;
			Cursor cursor = mDb.rawQuery(sql, null);
			try {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						UFileTransactionInfo info = new UFileTransactionInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
						info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
						info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
						info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
						info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
						info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
						info.nTripNo = cursor.getInt(cursor.getColumnIndex(KEY_TRIP_NUMBER));
						info.nFileTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_FILE_TRXN_NO));
						info.strFileDocNo = cursor.getString(cursor.getColumnIndex(KEY_FILE_DOC_NO));
						info.strFileShortDescription = cursor.getString(cursor.getColumnIndex(KEY_FILE_SHORT_DESCRIPTION));
						info.strFileLongDescription = cursor.getString(cursor.getColumnIndex(KEY_FILE_LONG_DESCRIPTION));
						info.strFileCreateDate = cursor.getString(cursor.getColumnIndex(KEY_FILE_CREATE_DATE));
						info.strFileCreateTime = cursor.getString(cursor.getColumnIndex(KEY_FILE_CREATE_TIME));
						info.strFileFileName = cursor.getString(cursor.getColumnIndex(KEY_FILE_FILE_NAME));
						cursor.moveToNext();
						list.add(info);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		return list;
	}

	public long insertUFileTransactionsInfo(UFileTransactionInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_DOC_TYPE, info.strDocType);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_TRIP_NUMBER, info.nTripNo);
		values.put(KEY_FILE_TRXN_NO, info.nFileTrxnNo);
		values.put(KEY_FILE_DOC_NO, info.strFileDocNo);
		values.put(KEY_FILE_SHORT_DESCRIPTION, info.strFileShortDescription);
		values.put(KEY_FILE_LONG_DESCRIPTION, info.strFileLongDescription);
		values.put(KEY_FILE_CREATE_DATE, info.strFileCreateDate);
		values.put(KEY_FILE_CREATE_TIME, info.strFileCreateTime);
		values.put(KEY_FILE_FILE_NAME, info.strFileFileName);

		return mDb.insert(TABLE_UFILETRANSACTION_INFO, null, values);
	}

	private boolean deleteAllUFileTransactionsInfo(int nCustNo) throws SQLException {
		return mDb.delete(TABLE_UFILETRANSACTION_INFO, KEY_CUST_NO + " = " + nCustNo, null) > 0;
	}

	private boolean deleteAllUFileTransactionsInfo(int nCustNo, MakeZipFileInfo zipFileInfo) throws SQLException {
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			mDb.delete(TABLE_UFILETRANSACTION_INFO, KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_TRXN_NO + " = " + nTrxnNo, null);
		}
		return true;

	}

	public long insertUStockHInfoList(UStockHInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_DOC_TYPE, info.strDocType);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		values.put(KEY_DOC_NO, info.strDocNo);
		values.put(KEY_STOCK_TYPE, info.strStockType);
		values.put(KEY_PRINTED_FLAG, info.printedFlag);
		values.put(KEY_REQUIRED_DATE, info.strRequiredDate);
		values.put(KEY_DETAIL_KEY_EXPANDED, info.strDetailKeyExpanded);
		values.put(KEY_REFERENCE, info.strReference);
		values.put(KEY_TRANSFER_ROUTE, info.strTransferRoute);
		values.put(KEY_TO_WARE_HOUSE, info.strToWarehouse);
		return mDb.insert(TABLE_USTOCKH_INFO, null, values);
	}

	public long insertUConsignmentInfoList(UStockHInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_DOC_TYPE, info.strDocType);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		values.put(KEY_DOC_NO, info.strDocNo);
		values.put(KEY_STOCK_TYPE, info.strStockType);
		values.put(KEY_PRINTED_FLAG, info.printedFlag);
		values.put(KEY_REQUIRED_DATE, info.strRequiredDate);
		values.put(KEY_DETAIL_KEY_EXPANDED, info.strDetailKeyExpanded);
		values.put(KEY_REFERENCE, info.strReference);
		values.put(KEY_TRANSFER_ROUTE, info.strTransferRoute);
		values.put(KEY_TO_WARE_HOUSE, info.strToWarehouse);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		return mDb.insert(TABLE_UCONSIGNMENT_INFO, null, values);
	}

	public void updateUConsignmentInfo(UStockHInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_DOC_TYPE, info.strDocType);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		values.put(KEY_DOC_NO, info.strDocNo);
		values.put(KEY_STOCK_TYPE, info.strStockType);
		values.put(KEY_PRINTED_FLAG, info.printedFlag);
		values.put(KEY_REQUIRED_DATE, info.strRequiredDate);
		values.put(KEY_DETAIL_KEY_EXPANDED, info.strDetailKeyExpanded);
		values.put(KEY_REFERENCE, info.strReference);
		values.put(KEY_TRANSFER_ROUTE, info.strTransferRoute);
		values.put(KEY_TO_WARE_HOUSE, info.strToWarehouse);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		mDb.update(TABLE_UCONSIGNMENT_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}

	public List<UStockHInfo> getUConsignmentInfoList(MakeZipFileInfo zipFileInfo)
	{
		List<UStockHInfo> list = new ArrayList<>();
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			String sql = "SELECT * FROM " + TABLE_UCONSIGNMENT_INFO + " WHERE " + KEY_TRXN_NO + " = " + nTrxnNo;
			Cursor cursor = mDb.rawQuery(sql, null);

			try {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						UStockHInfo info = new UStockHInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
						info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
						info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
						info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
						info.strStockType = cursor.getString(cursor.getColumnIndex(KEY_STOCK_TYPE));
						info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
						info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
						info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
						info.printedFlag = cursor.getInt(cursor.getColumnIndex(KEY_PRINTED_FLAG));
						info.strRequiredDate = cursor.getString(cursor.getColumnIndex(KEY_REQUIRED_DATE));
						info.strDetailKeyExpanded = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_KEY_EXPANDED));
						info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
						info.strTransferRoute = cursor.getString(cursor.getColumnIndex(KEY_TRANSFER_ROUTE));
						info.strToWarehouse = cursor.getString(cursor.getColumnIndex(KEY_TO_WARE_HOUSE));
						cursor.moveToNext();
						list.add(info);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		return list;
	}

	private boolean deleteAllUStockHInfo()
	{
		return mDb.delete(TABLE_USTOCKH_INFO, null, null) > 0;
	}

	private boolean deleteAllUConsignmentInfo()
	{
		return mDb.delete(TABLE_UCONSIGNMENT_INFO, null, null) > 0;
	}

	private boolean deleteAllUStockHInfo(MakeZipFileInfo zipFileInfo)
	{
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			mDb.delete(TABLE_USTOCKH_INFO, KEY_TRXN_NO + " = " + nTrxnNo, null);
		}
		return true;
	}

	private boolean deleteAllUConsignmentInfo(MakeZipFileInfo zipFileInfo)
	{
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			mDb.delete(TABLE_UCONSIGNMENT_INFO, KEY_TRXN_NO + " = " + nTrxnNo, null);
		}
		return true;
	}

	public int updateUStockHInfo(UStockHInfo info){
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_STOCK_TYPE, info.strStockType);
		values.put(KEY_DOC_TYPE, info.strDocType);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		values.put(KEY_DOC_NO, info.strDocNo);
		values.put(KEY_PRINTED_FLAG, info.printedFlag);
		values.put(KEY_REQUIRED_DATE, info.strRequiredDate);
		values.put(KEY_DETAIL_KEY_EXPANDED, info.strDetailKeyExpanded);
		values.put(KEY_REFERENCE, info.strReference);
		values.put(KEY_TRANSFER_ROUTE, info.strTransferRoute);
		values.put(KEY_TO_WARE_HOUSE, info.strToWarehouse);
		return mDb.update(TABLE_USTOCKH_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}

	public List<UStockHInfo> getUStockHInfoList()
	{
		String sql = "SELECT * FROM " + TABLE_USTOCKH_INFO;

		Cursor cursor = mDb.rawQuery(sql, null);
		List<UStockHInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UStockHInfo info = new UStockHInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
					info.strStockType = cursor.getString(cursor.getColumnIndex(KEY_STOCK_TYPE));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
					info.printedFlag = cursor.getInt(cursor.getColumnIndex(KEY_PRINTED_FLAG));
					info.strRequiredDate = cursor.getString(cursor.getColumnIndex(KEY_REQUIRED_DATE));
					info.strDetailKeyExpanded = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_KEY_EXPANDED));
					info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
					info.strTransferRoute = cursor.getString(cursor.getColumnIndex(KEY_TRANSFER_ROUTE));
					info.strToWarehouse = cursor.getString(cursor.getColumnIndex(KEY_TO_WARE_HOUSE));

					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<UStockHInfo> getUStockHInfoList(MakeZipFileInfo zipFileInfo)
	{
		List<UStockHInfo> list = new ArrayList<>();
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			String sql = "SELECT * FROM " + TABLE_USTOCKH_INFO + " WHERE " + KEY_TRXN_NO + " = " + nTrxnNo;
			Cursor cursor = mDb.rawQuery(sql, null);

			try {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						UStockHInfo info = new UStockHInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
						info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
						info.strStockType = cursor.getString(cursor.getColumnIndex(KEY_STOCK_TYPE));
						info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
						info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
						info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
						info.printedFlag = cursor.getInt(cursor.getColumnIndex(KEY_PRINTED_FLAG));
						info.strRequiredDate = cursor.getString(cursor.getColumnIndex(KEY_REQUIRED_DATE));
						info.strDetailKeyExpanded = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_KEY_EXPANDED));
						info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
						info.strTransferRoute = cursor.getString(cursor.getColumnIndex(KEY_TRANSFER_ROUTE));
						info.strToWarehouse = cursor.getString(cursor.getColumnIndex(KEY_TO_WARE_HOUSE));
						cursor.moveToNext();
						list.add(info);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		return list;
	}

	private boolean deleteAllUStockDInfo(MakeZipFileInfo zipFileInfo)
	{
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			mDb.delete(TABLE_USTOCKD_INFO, KEY_TRXN_NO + " = " + nTrxnNo, null);
		}
		return true;
	}

	private boolean deleteAllUStockDInfo()
	{
		return mDb.delete(TABLE_USTOCKD_INFO, null, null) > 0;
	}

	public long insertUStockDInfoList(UStockDInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_LOCN_NO, info.nLocnNo);
		values.put(KEY_REASON_CODE, info.nReasonCode);
		values.put(KEY_ACTUAL_QTY, info.nActualQty);
		values.put(KEY_BEGIN_QTY, info.nBeginQty);
		values.put(KEY_DETAIL_TYPE, info.strDetailType);
		values.put(KEY_SUGGEST_QTY, info.nSuggestQty);
		values.put(KEY_DACTUAL_QTY, info.dActualQty);
		values.put(KEY_DBEGIN_QTY, info.dBeginQty);
		values.put(KEY_DSUGGEST_QTY, info.dSuggestQty);

		return mDb.insert(TABLE_USTOCKD_INFO, null, values);
	}

	public void updateUStockDInfo(UStockDInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_LOCN_NO, info.nLocnNo);
		values.put(KEY_REASON_CODE, info.nReasonCode);
		values.put(KEY_ACTUAL_QTY, info.nActualQty);
		values.put(KEY_BEGIN_QTY, info.nBeginQty);
		values.put(KEY_DETAIL_TYPE, info.strDetailType);
		values.put(KEY_SUGGEST_QTY, info.nSuggestQty);
		values.put(KEY_DACTUAL_QTY, info.dActualQty);
		values.put(KEY_DBEGIN_QTY, info.dBeginQty);
		values.put(KEY_DSUGGEST_QTY, info.dSuggestQty);

		mDb.update(TABLE_USTOCKD_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}
	public List<UStockDInfo> getUStockDInfoList(long nTrxnNo)
	{
		String sql = "SELECT * FROM " + TABLE_USTOCKD_INFO + " WHERE " + KEY_TRXN_NO + " = " + nTrxnNo;

		Cursor cursor = mDb.rawQuery(sql, null);
		List<UStockDInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UStockDInfo info = new UStockDInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));
					info.nReasonCode = cursor.getInt(cursor.getColumnIndex(KEY_REASON_CODE));
					info.nSequence = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDetailType = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_TYPE));

					info.nActualQty = cursor.getInt(cursor.getColumnIndex(KEY_ACTUAL_QTY));
					info.nBeginQty = cursor.getInt(cursor.getColumnIndex(KEY_BEGIN_QTY));
					info.nSuggestQty = cursor.getInt(cursor.getColumnIndex(KEY_SUGGEST_QTY));

					info.dActualQty = cursor.getDouble(cursor.getColumnIndex(KEY_DACTUAL_QTY));
					info.dBeginQty = cursor.getDouble(cursor.getColumnIndex(KEY_DBEGIN_QTY));
					info.dSuggestQty = cursor.getDouble(cursor.getColumnIndex(KEY_DSUGGEST_QTY));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<UStockDInfo> getUStockDInfoListForUnload(long nTrxnNo)
	{
		String sql = "SELECT * FROM " + TABLE_USTOCKD_INFO + " WHERE " + KEY_TRXN_NO + " = " + nTrxnNo + " AND (" + KEY_DETAIL_TYPE + " = 'D' OR " + KEY_DETAIL_TYPE + " = 'F' OR " + KEY_DETAIL_TYPE + " = 'S' )";

		Cursor cursor = mDb.rawQuery(sql, null);
		List<UStockDInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UStockDInfo info = new UStockDInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));
					info.nReasonCode = cursor.getInt(cursor.getColumnIndex(KEY_REASON_CODE));
					info.nSequence = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDetailType = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_TYPE));

					info.nActualQty = cursor.getInt(cursor.getColumnIndex(KEY_ACTUAL_QTY));
					info.nBeginQty = cursor.getInt(cursor.getColumnIndex(KEY_BEGIN_QTY));
					info.nSuggestQty = cursor.getInt(cursor.getColumnIndex(KEY_SUGGEST_QTY));

					info.dActualQty = cursor.getDouble(cursor.getColumnIndex(KEY_DACTUAL_QTY));
					info.dBeginQty = cursor.getDouble(cursor.getColumnIndex(KEY_DBEGIN_QTY));
					info.dSuggestQty = cursor.getDouble(cursor.getColumnIndex(KEY_DSUGGEST_QTY));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}
	public List<UStockDInfo> getUStockDInfoListForEOD(long nTrxnNo)
	{
		String sql = "SELECT * FROM " + TABLE_USTOCKD_INFO + " WHERE " + KEY_TRXN_NO + " = " + nTrxnNo + " AND " + KEY_DETAIL_TYPE + " = ''";

		Cursor cursor = mDb.rawQuery(sql, null);
		List<UStockDInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UStockDInfo info = new UStockDInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));
					info.nReasonCode = cursor.getInt(cursor.getColumnIndex(KEY_REASON_CODE));
					info.nSequence = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDetailType = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_TYPE));

					info.nActualQty = cursor.getInt(cursor.getColumnIndex(KEY_ACTUAL_QTY));
					info.nBeginQty = cursor.getInt(cursor.getColumnIndex(KEY_BEGIN_QTY));
					info.nSuggestQty = cursor.getInt(cursor.getColumnIndex(KEY_SUGGEST_QTY));

					info.dActualQty = cursor.getDouble(cursor.getColumnIndex(KEY_DACTUAL_QTY));
					info.dBeginQty = cursor.getDouble(cursor.getColumnIndex(KEY_DBEGIN_QTY));
					info.dSuggestQty = cursor.getDouble(cursor.getColumnIndex(KEY_DSUGGEST_QTY));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	// UInvenHInfo
	public List<UInvenHInfo> getUInvenHList(int nCustNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_UINVENH_INFO +
						" WHERE		" + KEY_CUST_NO + " = " + nCustNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UInvenHInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					if( Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO) == cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO)) ) continue;

					UInvenHInfo info = new UInvenHInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<UInvenHInfo> getUInvenHList(int nCustNo, MakeZipFileInfo zipFileInfo) {
		List<UInvenHInfo> list = new ArrayList<>();
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			String sql =	" SELECT	* " +
					" FROM		" + TABLE_UINVENH_INFO +
					" WHERE		" + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_TRXN_NO + " = " + nTrxnNo;
			Cursor cursor = mDb.rawQuery(sql, null);

			try {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						if( Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO) == cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO)) ) continue;

						UInvenHInfo info = new UInvenHInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
						info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
						info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
						info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
						info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
						info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
						cursor.moveToNext();
						list.add(info);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		return list;
	}

	public UInvenHInfo 	getUInvenHInfo(long nTrxnNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_UINVENH_INFO +
						" WHERE		" + KEY_TRXN_NO + " = " + nTrxnNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		UInvenHInfo info = null;
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info = new UInvenHInfo();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
				info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
				info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
				info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
				info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
				info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	public long insertUInvenHInfo(UInvenHInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		values.put(KEY_REFERENCE, info.strReference);
		values.put(KEY_DOC_TYPE, info.strDocType);
		return mDb.insert(TABLE_UINVENH_INFO, null, values);
	}

	public void deleteAllUInvenHInfo(int nCustNo, boolean bNeedDelete) throws SQLException {
		String sql =	" SELECT	" + KEY_TRXN_NO +
						" FROM		" + TABLE_UINVENH_INFO;
		if (nCustNo > 0) {
			sql += " WHERE " + KEY_CUST_NO + " = " + nCustNo;
		}
		Cursor cursor = mDb.rawQuery(sql, null);
		List<Long> list = new ArrayList<>();
		long nInvenTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					long nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					cursor.moveToNext();
					if (bNeedDelete == false && (nTrxnNo == nInvenTrxnNo)) {
						Utils.printDebug(null, "Do not delete base UINVEN.");
					} else {
						mDb.delete(TABLE_UINVENH_INFO, KEY_TRXN_NO + " = " + nTrxnNo, null);
						list.add(nTrxnNo);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		for (Long nTrxnNo : list) {
			deleteAllUInvenDInfo(nTrxnNo);
		}
	}

	public void deleteAllUInvenHInfo(int nCustNo, boolean bNeedDelete, MakeZipFileInfo zipFileInfo) throws SQLException {
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			String sql =	" SELECT	" + KEY_TRXN_NO +
					" FROM		" + TABLE_UINVENH_INFO;
			if (nCustNo > 0) {
				sql += " WHERE " + KEY_CUST_NO + " = " + nCustNo ;
				sql += " AND " + KEY_TRXN_NO + " = " + nTrxnNo;
			}
			else{
				sql += " WHERE " + KEY_TRXN_NO + " = " + nTrxnNo;
			}

			Cursor cursor = mDb.rawQuery(sql, null);
			List<Long> list = new ArrayList<>();
			long nInvenTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
			try {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						long nTrxnNo1 = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
						cursor.moveToNext();
						if (bNeedDelete == false && (nTrxnNo == nInvenTrxnNo)) {
							Utils.printDebug(null, "Do not delete base UINVEN.");
						} else {
							mDb.delete(TABLE_UINVENH_INFO, KEY_TRXN_NO + " = " + nTrxnNo, null);
							list.add(nTrxnNo1);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			for (Long nTrxnNo1 : list) {
				deleteAllUInvenDInfo(nTrxnNo1);
			}
		}

	}

	// UInvenDInfo
	public List<UInvenDInfo> getUInvenDList(long nTrxnNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_UINVEND_INFO +
						" WHERE		" + KEY_TRXN_NO + " = " + nTrxnNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		Gson gson = new Gson();
		Type userListType = new TypeToken<ArrayList<LoadBlotInfo>>(){}.getType();
		List<UInvenDInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UInvenDInfo info = new UInvenDInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.nLoaded = cursor.getInt(cursor.getColumnIndex(KEY_LOADED));
					info.nLoadAdj = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_ADJ));
					info.nStock = cursor.getInt(cursor.getColumnIndex(KEY_STOCK));
					info.nSales = cursor.getInt(cursor.getColumnIndex(KEY_SALES));
					info.nDumps = cursor.getInt(cursor.getColumnIndex(KEY_DUMPS));
					info.nBuybacks = cursor.getInt(cursor.getColumnIndex(KEY_BUYBACKS));
					info.nTransIn = cursor.getInt(cursor.getColumnIndex(KEY_TRANS_IN));
					info.nTransOut = cursor.getInt(cursor.getColumnIndex(KEY_TRANS_OUT));
					info.nSpoils = cursor.getInt(cursor.getColumnIndex(KEY_SPOILS));
					info.nUnload = cursor.getInt(cursor.getColumnIndex(KEY_UNLOAD));
					info.nDumpsUnload = cursor.getInt(cursor.getColumnIndex(KEY_DUMPS_UNLOAD));
					info.nEndAdj = cursor.getInt(cursor.getColumnIndex(KEY_END_ADJ));
					info.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
					info.nBeginQty = cursor.getInt(cursor.getColumnIndex(KEY_BEGIN_QTY));
					info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.bShow = cursor.getInt(cursor.getColumnIndex(KEY_SHOW_FLAG)) == 1;
					info.strPickLocationCode = cursor.getString(cursor.getColumnIndex(KEY_PICK_LOCATION_CODE));

					info.nBeginPresold = cursor.getInt(cursor.getColumnIndex(KEY_BEGIN_PRESOLDD));
					info.nBeginPeddle = cursor.getInt(cursor.getColumnIndex(KEY_BEGIN_PEDDLE));
					info.nResPresold = cursor.getInt(cursor.getColumnIndex(KEY_RES_PRESOLD));
					info.nResPeddle = cursor.getInt(cursor.getColumnIndex(KEY_RES_PEDDLE));

					info.dLoadAdj = cursor.getDouble(cursor.getColumnIndex(KEY_DLOADADJ));
					info.dStock = cursor.getDouble(cursor.getColumnIndex(KEY_DSTOCK));
					info.dBeginQty = cursor.getDouble(cursor.getColumnIndex(KEY_DBEGIN_QTY));
					info.dLoaded = cursor.getDouble(cursor.getColumnIndex(KEY_DLOADED));
					info.dAdjQty = cursor.getDouble(cursor.getColumnIndex(KEY_DADJ_QTY));
					info.dSales = cursor.getDouble(cursor.getColumnIndex(KEY_DSALES));
					info.dDumps = cursor.getDouble(cursor.getColumnIndex(KEY_DDUMPS));

					info.dBeginPresold = cursor.getDouble(cursor.getColumnIndex(KEY_DBEGIN_PRESOLDD));
					info.dBeginPeddle = cursor.getDouble(cursor.getColumnIndex(KEY_DBEGIN_PRESOLDD));
					info.dResPresold = cursor.getDouble(cursor.getColumnIndex(KEY_DRES_PRESOLD));
					info.dResPeddle = cursor.getDouble(cursor.getColumnIndex(KEY_DRES_PEDDLE));
					info.dEndAdj = cursor.getDouble(cursor.getColumnIndex(KEY_DEND_ADJ));

					info.dDumpsUnload = cursor.getDouble(cursor.getColumnIndex(KEY_DDUMPSUNLOAD));
					info.dUnload = cursor.getDouble(cursor.getColumnIndex(KEY_DUNLOAD));

					info.dBuybacks = cursor.getDouble(cursor.getColumnIndex(KEY_DBUYBACK));
					info.dTransIn = cursor.getDouble(cursor.getColumnIndex(KEY_DTRANS_IN));
					info.dTransOut = cursor.getDouble(cursor.getColumnIndex(KEY_DTRANS_OUT));
					info.dSpoils = cursor.getDouble(cursor.getColumnIndex(KEY_SPOILS));

					info.nProdLoconSCWType = cursor.getInt(cursor.getColumnIndex(KEY_PRODLOCN_SCW_TYPE));
					info.strBatchCode = cursor.getString(cursor.getColumnIndex(KEY_BATCH_NUMBER));
					info.bLotControl = cursor.getInt(cursor.getColumnIndex(KEY_LOT_CONTROL)) == 1;
					String strLoadBlot = cursor.getString(cursor.getColumnIndex(KEY_LOAD_BLOT_DATA));
					if(!TextUtils.isEmpty(strLoadBlot))
					{
						info.loadBlotInfoList = gson.fromJson(strLoadBlot, userListType);
					}
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<UInvenDInfo> getUInvenDList(String strItemNo) {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_UINVEND_INFO +
				" WHERE		" + KEY_ITEM_NO + " = '" + strItemNo + "'";
		Cursor cursor = mDb.rawQuery(sql, null);
		//Gson gson = new Gson();
		//Type userListType = new TypeToken<ArrayList<LoadBlotInfo>>(){}.getType();
		List<UInvenDInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UInvenDInfo info = new UInvenDInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.nLoaded = cursor.getInt(cursor.getColumnIndex(KEY_LOADED));
					info.nLoadAdj = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_ADJ));
					info.nStock = cursor.getInt(cursor.getColumnIndex(KEY_STOCK));
					info.nSales = cursor.getInt(cursor.getColumnIndex(KEY_SALES));
					info.nDumps = cursor.getInt(cursor.getColumnIndex(KEY_DUMPS));
					info.nBuybacks = cursor.getInt(cursor.getColumnIndex(KEY_BUYBACKS));
					info.nTransIn = cursor.getInt(cursor.getColumnIndex(KEY_TRANS_IN));
					info.nTransOut = cursor.getInt(cursor.getColumnIndex(KEY_TRANS_OUT));
					info.nSpoils = cursor.getInt(cursor.getColumnIndex(KEY_SPOILS));
					info.nUnload = cursor.getInt(cursor.getColumnIndex(KEY_UNLOAD));
					info.nDumpsUnload = cursor.getInt(cursor.getColumnIndex(KEY_DUMPS_UNLOAD));
					info.nEndAdj = cursor.getInt(cursor.getColumnIndex(KEY_END_ADJ));
					info.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
					info.nBeginQty = cursor.getInt(cursor.getColumnIndex(KEY_BEGIN_QTY));
					info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.bShow = cursor.getInt(cursor.getColumnIndex(KEY_SHOW_FLAG)) == 1;
					info.strPickLocationCode = cursor.getString(cursor.getColumnIndex(KEY_PICK_LOCATION_CODE));

					info.nBeginPresold = cursor.getInt(cursor.getColumnIndex(KEY_BEGIN_PRESOLDD));
					info.nBeginPeddle = cursor.getInt(cursor.getColumnIndex(KEY_BEGIN_PEDDLE));
					info.nResPresold = cursor.getInt(cursor.getColumnIndex(KEY_RES_PRESOLD));
					info.nResPeddle = cursor.getInt(cursor.getColumnIndex(KEY_RES_PEDDLE));

					info.dLoadAdj = cursor.getDouble(cursor.getColumnIndex(KEY_DLOADADJ));
					info.dStock = cursor.getDouble(cursor.getColumnIndex(KEY_DSTOCK));
					info.dBeginQty = cursor.getDouble(cursor.getColumnIndex(KEY_DBEGIN_QTY));
					info.dLoaded = cursor.getDouble(cursor.getColumnIndex(KEY_DLOADED));
					info.dAdjQty = cursor.getDouble(cursor.getColumnIndex(KEY_DADJ_QTY));
					info.dSales = cursor.getDouble(cursor.getColumnIndex(KEY_DSALES));
					info.dDumps = cursor.getDouble(cursor.getColumnIndex(KEY_DDUMPS));

					info.dBeginPresold = cursor.getDouble(cursor.getColumnIndex(KEY_DBEGIN_PRESOLDD));
					info.dBeginPeddle = cursor.getDouble(cursor.getColumnIndex(KEY_DBEGIN_PRESOLDD));
					info.dResPresold = cursor.getDouble(cursor.getColumnIndex(KEY_DRES_PRESOLD));
					info.dResPeddle = cursor.getDouble(cursor.getColumnIndex(KEY_DRES_PEDDLE));
					info.dEndAdj = cursor.getDouble(cursor.getColumnIndex(KEY_DEND_ADJ));

					info.dDumpsUnload = cursor.getDouble(cursor.getColumnIndex(KEY_DDUMPSUNLOAD));
					info.dUnload = cursor.getDouble(cursor.getColumnIndex(KEY_DUNLOAD));

					info.dBuybacks = cursor.getDouble(cursor.getColumnIndex(KEY_DBUYBACK));
					info.dTransIn = cursor.getDouble(cursor.getColumnIndex(KEY_DTRANS_IN));
					info.dTransOut = cursor.getDouble(cursor.getColumnIndex(KEY_DTRANS_OUT));
					info.dSpoils = cursor.getDouble(cursor.getColumnIndex(KEY_SPOILS));

					info.nProdLoconSCWType = cursor.getInt(cursor.getColumnIndex(KEY_PRODLOCN_SCW_TYPE));
					info.strBatchCode = cursor.getString(cursor.getColumnIndex(KEY_BATCH_NUMBER));
					info.bLotControl = cursor.getInt(cursor.getColumnIndex(KEY_LOT_CONTROL)) == 1;
//					String strLoadBlot = cursor.getString(cursor.getColumnIndex(KEY_LOAD_BLOT_DATA));
//					if(!TextUtils.isEmpty(strLoadBlot))
//					{
//						info.loadBlotInfoList = gson.fromJson(strLoadBlot, userListType);
//					}
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public long insertUInvenDInfo(UInvenDInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_LOADED, info.nLoaded);
		values.put(KEY_LOAD_ADJ, info.nLoadAdj);
		values.put(KEY_STOCK, info.nStock);
		values.put(KEY_SALES, info.nSales);
		values.put(KEY_DUMPS, info.nDumps);
		values.put(KEY_BUYBACKS, info.nBuybacks);
		values.put(KEY_TRANS_IN, info.nTransIn);
		values.put(KEY_TRANS_OUT, info.nTransOut);
		values.put(KEY_SPOILS, info.nSpoils);
		values.put(KEY_UNLOAD, info.nUnload);
		values.put(KEY_DUMPS_UNLOAD, info.nDumpsUnload);
		values.put(KEY_END_ADJ, info.nEndAdj);
		values.put(KEY_BEGIN_QTY, info.nBeginQty);
		values.put(KEY_SCW_TYPE, info.nSCWType);
		values.put(KEY_SHORT_DESC, info.strShortDesc);
		values.put(KEY_DESCRIPTION, info.strDescription);
		values.put(KEY_PICK_LOCATION_CODE, info.strPickLocationCode);
		values.put(KEY_SHOW_FLAG, info.bShow ? 1 : 0);

		values.put(KEY_BEGIN_PEDDLE, info.nBeginPeddle);
		values.put(KEY_BEGIN_PRESOLDD, info.nBeginPresold);
		values.put(KEY_RES_PEDDLE, info.nResPeddle);
		values.put(KEY_RES_PRESOLD, info.nResPresold);

		values.put(KEY_DSTOCK, info.dStock);
		values.put(KEY_DBEGIN_QTY, info.dBeginQty);
		values.put(KEY_DLOADED, info.dLoaded);
		values.put(KEY_DLOADADJ, info.dLoadAdj);
		values.put(KEY_DADJ_QTY, info.dAdjQty);
		values.put(KEY_DDUMPS, info.dDumps);
		values.put(KEY_DSALES, info.dSales);

		values.put(KEY_DBEGIN_PRESOLDD, info.dBeginPresold);
		values.put(KEY_DBEGIN_PEDDLE, info.dBeginPeddle);
		values.put(KEY_DRES_PRESOLD, info.dResPresold);
		values.put(KEY_DRES_PEDDLE, info.dResPeddle);
		values.put(KEY_DEND_ADJ, info.dEndAdj);

		values.put(KEY_DDUMPSUNLOAD, info.dDumpsUnload);
		values.put(KEY_DUNLOAD, info.dUnload);

		values.put(KEY_DBUYBACK, info.dBuybacks);
		values.put(KEY_DTRANS_IN, info.dTransIn);
		values.put(KEY_DTRANS_OUT, info.dTransOut);
		values.put(KEY_DSPOILS, info.dSpoils);

		values.put(KEY_PRODLOCN_SCW_TYPE, info.nProdLoconSCWType);
		values.put(KEY_BATCH_NUMBER, info.strBatchCode);

		values.put(KEY_LOT_CONTROL, info.bLotControl ? 1 : 0);
		Gson gson = new Gson();
		values.put(KEY_LOAD_BLOT_DATA, gson.toJson(info.loadBlotInfoList));

		return mDb.insert(TABLE_UINVEND_INFO, null, values);
	}

	public long updateUInvenDInfo(UInvenDInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_LOADED, info.nLoaded);
		values.put(KEY_LOAD_ADJ, info.nLoadAdj);
		values.put(KEY_STOCK, info.nStock);
		values.put(KEY_SALES, info.nSales);
		values.put(KEY_DUMPS, info.nDumps);
		values.put(KEY_BUYBACKS, info.nBuybacks);
		values.put(KEY_TRANS_IN, info.nTransIn);
		values.put(KEY_TRANS_OUT, info.nTransOut);
		values.put(KEY_SPOILS, info.nSpoils);
		values.put(KEY_UNLOAD, info.nUnload);
		values.put(KEY_DUMPS_UNLOAD, info.nDumpsUnload);
		values.put(KEY_END_ADJ, info.nEndAdj);
		values.put(KEY_BEGIN_QTY, info.nBeginQty);
		values.put(KEY_SCW_TYPE, info.nSCWType);
		values.put(KEY_SHORT_DESC, info.strShortDesc);
		values.put(KEY_DESCRIPTION, info.strDescription);
		values.put(KEY_PICK_LOCATION_CODE, info.strPickLocationCode);
		values.put(KEY_SHOW_FLAG, info.bShow ? 1: 0);

		values.put(KEY_BEGIN_PEDDLE, info.nBeginPeddle);
		values.put(KEY_BEGIN_PRESOLDD, info.nBeginPresold);
		values.put(KEY_RES_PEDDLE, info.nResPeddle);
		values.put(KEY_RES_PRESOLD, info.nResPresold);

		values.put(KEY_DSTOCK, info.dStock);
		values.put(KEY_DBEGIN_QTY, info.dBeginQty);
		values.put(KEY_DLOADED, info.dLoaded);
		values.put(KEY_DLOADADJ, info.dLoadAdj);
		values.put(KEY_DADJ_QTY, info.dAdjQty);
		values.put(KEY_DDUMPS, info.dDumps);
		values.put(KEY_DSALES, info.dSales);

		values.put(KEY_DBEGIN_PRESOLDD, info.dBeginPresold);
		values.put(KEY_DBEGIN_PEDDLE, info.dBeginPeddle);
		values.put(KEY_DRES_PRESOLD, info.dResPresold);
		values.put(KEY_DRES_PEDDLE, info.dResPeddle);
		values.put(KEY_DEND_ADJ, info.dEndAdj);

		values.put(KEY_DDUMPSUNLOAD, info.dDumpsUnload);
		values.put(KEY_DUNLOAD, info.dUnload);

		values.put(KEY_DBUYBACK, info.dBuybacks);
		values.put(KEY_DTRANS_IN, info.dTransIn);
		values.put(KEY_DTRANS_OUT, info.dTransOut);
		values.put(KEY_DSPOILS, info.dSpoils);

		values.put(KEY_PRODLOCN_SCW_TYPE, info.nProdLoconSCWType);
		values.put(KEY_BATCH_NUMBER, info.strBatchCode);

		values.put(KEY_LOT_CONTROL, info.bLotControl ? 1 : 0);
		Gson gson = new Gson();
		values.put(KEY_LOAD_BLOT_DATA, gson.toJson(info.loadBlotInfoList));

		return mDb.update(TABLE_UINVEND_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}

	public  boolean deleteAllUInvenDInfo()throws SQLException{
		return mDb.delete(TABLE_UINVEND_INFO, null, null) > 0;
	}

	private boolean deleteAllUInvenDInfo(long nTrxnNo) throws SQLException {
		return mDb.delete(TABLE_UINVEND_INFO, KEY_TRXN_NO + " = " + nTrxnNo, null) > 0;
	}

	//For Sequence Value
	public void getOrderSeqValue(Map<String, Integer> caseMap)
	{
		if( caseMap == null ) return;
		caseMap.clear();
		String sql =	" SELECT " + KEY_ORDER_SEQ + ", " + KEY_ITEM_NO +
				" FROM		" + TABLE_PRODLOCN_INFO;
		Cursor cursor = mDb.rawQuery(sql, null);
		try {
			if( cursor != null && cursor.getCount() > 0 ){
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					int nCaseValue = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_SEQ));
					String strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					caseMap.put(strItemNo, nCaseValue);
					cursor.moveToNext();
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	//For Case Value
	public void getCaseValue(Map<String, Integer> caseMap)
	{
		if( caseMap == null ) return;
		caseMap.clear();
		String sql =	" SELECT " + KEY_CASE_FACTOR + ", " + KEY_ITEM_NO +
				" FROM		" + TABLE_PRODLOCN_INFO;
		Cursor cursor = mDb.rawQuery(sql, null);
		try {
			if( cursor != null && cursor.getCount() > 0 ){
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					int nCaseValue = cursor.getInt(cursor.getColumnIndex(KEY_CASE_FACTOR));
					String strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					caseMap.put(strItemNo, nCaseValue);
					cursor.moveToNext();
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public int getProdLocnSCWType(String strItemNo)
	{
		String sql =	" SELECT	" + KEY_SCW_TYPE +
				" FROM		" + TABLE_PRODLOCN_INFO +
				" WHERE		" + KEY_ITEM_NO + " = '" + strItemNo + "' ";
		int nType = 0;
		Cursor cursor = null;
		try {
			cursor = mDb.rawQuery(sql, null);
			if(cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				nType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return nType;
	}

	// ProdLocnInfo
	public void getProdLocnInfoList(Map<String, ProdLocnInfo> prodLocnInfoMap)
	{
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_PRODLOCN_INFO;
		Cursor cursor = null;

		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i  = 0 ; i < cursor.getCount(); i++)
				{
					ProdLocnInfo info = new ProdLocnInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
					info.nBasePrice = cursor.getLong(cursor.getColumnIndex(KEY_BASE_PRICE));
					info.nDriverPrice = cursor.getLong(cursor.getColumnIndex(KEY_DRIVER_PRICE));
					info.nCaseFactor = cursor.getInt(cursor.getColumnIndex(KEY_CASE_FACTOR));
					info.nRetailPrice = cursor.getInt(cursor.getColumnIndex(KEY_RETAIL_PRICE));
					info.nReturnPrice = cursor.getInt(cursor.getColumnIndex(KEY_RETURN_PRICE));
					info.strTaxFlag = cursor.getString(cursor.getColumnIndex(KEY_TAX_FLAG));
					info.strItemTaxCode = cursor.getString(cursor.getColumnIndex(KEY_ITEM_TAX_CODE));
					info.strFullCase = cursor.getString(cursor.getColumnIndex(KEY_FULL_CASE));
					info.nPalletFactor = cursor.getInt(cursor.getColumnIndex(KEY_PALLET_FACTOR));
					info.nStackFactor = cursor.getInt(cursor.getColumnIndex(KEY_STACK_FACTOR));
					info.strCaseUOM = cursor.getString(cursor.getColumnIndex(KEY_CASE_UOM));
					info.strUnitUOM = cursor.getString(cursor.getColumnIndex(KEY_UNIT_UOM));
					info.strWeightType = cursor.getString(cursor.getColumnIndex(KEY_WEIGHT_TYPE));
					info.nCustDiscountValid = cursor.getInt(cursor.getColumnIndex(KEY_CUST_DISCOUNT_VALID));
					info.strDiscInLieuItem = cursor.getString(cursor.getColumnIndex(KEY_DISC_IN_LIEU_ITEM));
					info.nOrderSeq = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_SEQ));
					prodLocnInfoMap.put(info.strItemNo, info);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public ProdLocnInfo getProdLocnInfo(String strItemNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_PRODLOCN_INFO +
						" WHERE		" + KEY_ITEM_NO + " = '" + strItemNo + "' ";
		Cursor cursor = null;
		ProdLocnInfo info = null;
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info = new ProdLocnInfo();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));
				info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
				info.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
				info.nBasePrice = cursor.getLong(cursor.getColumnIndex(KEY_BASE_PRICE));
				info.nDriverPrice = cursor.getLong(cursor.getColumnIndex(KEY_DRIVER_PRICE));
				info.nCaseFactor = cursor.getInt(cursor.getColumnIndex(KEY_CASE_FACTOR));
				info.nRetailPrice = cursor.getInt(cursor.getColumnIndex(KEY_RETAIL_PRICE));
				info.nReturnPrice = cursor.getInt(cursor.getColumnIndex(KEY_RETURN_PRICE));
				info.strTaxFlag = cursor.getString(cursor.getColumnIndex(KEY_TAX_FLAG));
				info.strItemTaxCode = cursor.getString(cursor.getColumnIndex(KEY_ITEM_TAX_CODE));
				info.strFullCase = cursor.getString(cursor.getColumnIndex(KEY_FULL_CASE));
				info.nPalletFactor = cursor.getInt(cursor.getColumnIndex(KEY_PALLET_FACTOR));
				info.nStackFactor = cursor.getInt(cursor.getColumnIndex(KEY_STACK_FACTOR));
				info.strCaseUOM = cursor.getString(cursor.getColumnIndex(KEY_CASE_UOM));
				info.strUnitUOM = cursor.getString(cursor.getColumnIndex(KEY_UNIT_UOM));
				info.strWeightType = cursor.getString(cursor.getColumnIndex(KEY_WEIGHT_TYPE));
				info.nCustDiscountValid = cursor.getInt(cursor.getColumnIndex(KEY_CUST_DISCOUNT_VALID));
				info.strDiscInLieuItem = cursor.getString(cursor.getColumnIndex(KEY_DISC_IN_LIEU_ITEM));
				info.nOrderSeq = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_SEQ));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	public long insertProdLocnInfo(ProdLocnInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_LOCN_NO, info.nLocnNo);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_SCW_TYPE, info.nSCWType);
		values.put(KEY_BASE_PRICE, info.nBasePrice);
		values.put(KEY_DRIVER_PRICE, info.nDriverPrice);
		values.put(KEY_CASE_FACTOR, info.nCaseFactor);
		values.put(KEY_RETAIL_PRICE, info.nRetailPrice);
		values.put(KEY_RETURN_PRICE, info.nReturnPrice);
		values.put(KEY_TAX_FLAG, info.strTaxFlag);
		values.put(KEY_ITEM_TAX_CODE, info.strItemTaxCode);
		values.put(KEY_FULL_CASE, info.strFullCase);
		values.put(KEY_PALLET_FACTOR, info.nPalletFactor);
		values.put(KEY_STACK_FACTOR, info.nStackFactor);
		values.put(KEY_CASE_UOM, info.strCaseUOM);
		values.put(KEY_UNIT_UOM, info.strUnitUOM);
		values.put(KEY_WEIGHT_TYPE, info.strWeightType);
		values.put(KEY_CUST_DISCOUNT_VALID, info.nCustDiscountValid);
		values.put(KEY_DISC_IN_LIEU_ITEM, info.strDiscInLieuItem);
		values.put(KEY_ORDER_SEQ, info.nOrderSeq);

		return mDb.insert(TABLE_PRODLOCN_INFO, null, values);
	}

	public boolean deleteAllProdLocnInfo() throws SQLException {
		return mDb.delete(TABLE_PRODLOCN_INFO, null, null) > 0;
	}

	// ProdDetlInfo
	public List<ProdDetlInfo> getProdDetlContainerList() {
		String sql =	" SELECT	A.* " +
						" FROM		" + TABLE_PRODDETL_INFO + " A, " + TABLE_PRODLOCN_INFO + " B " +
						" WHERE		B." + KEY_SCW_TYPE + " = 2 AND A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<ProdDetlInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					ProdDetlInfo info = new ProdDetlInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
					info.strItemUPC = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC));
					info.strItemUPC20 = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC20));
					info.nWeight = cursor.getFloat(cursor.getColumnIndex(KEY_WEIGHT));
					info.strItemType = cursor.getString(cursor.getColumnIndex(KEY_ITEM_TYPE));
					info.strProdGrp = cursor.getString(cursor.getColumnIndex(KEY_PROD_GRP));
					info.strPromoGrp = cursor.getString(cursor.getColumnIndex(KEY_PROMO_GRP));
					info.strMarketGrp = cursor.getString(cursor.getColumnIndex(KEY_MARKET_GRP));
					info.strProdLine = cursor.getString(cursor.getColumnIndex(KEY_PROD_LINE));
					info.strPackSize = cursor.getString(cursor.getColumnIndex(KEY_PACK_SIZE));
					info.nShelfLife = cursor.getInt(cursor.getColumnIndex(KEY_SHELF_LIFE));
					info.strBrand = cursor.getString(cursor.getColumnIndex(KEY_BRAND));
					info.strSubBrand = cursor.getString(cursor.getColumnIndex(KEY_SUB_BRAND));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strActiveDate = cursor.getString(cursor.getColumnIndex(KEY_ACTIVE_DATE));
					info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
					info.strSalesAllowed = cursor.getString(cursor.getColumnIndex(KEY_SALES_ALLOWED));
					info.strItemInactiveSwitch = cursor.getString(cursor.getColumnIndex(KEY_ITEM_INACTIVE_SWITCH));
					info.strGroupFilterKey = cursor.getString(cursor.getColumnIndex(KEY_GROUP_FILTER_KEY));
					info.strServiceFee = cursor.getString(cursor.getColumnIndex(KEY_SERVICE_FEE));
					info.nMaxQty = cursor.getFloat(cursor.getColumnIndex(KEY_MAX_QTY));
					info.nConsumerUnit = cursor.getInt(cursor.getColumnIndex(KEY_CONSUMER_UNIT));
					info.strImageUrl = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URL));
					info.strLotControl = cursor.getString(cursor.getColumnIndex(KEY_LOT_CONTROL));

					cursor.moveToNext();

					// we need to skip the inactive info
					if (info.isInactive()) {
						continue;
					}

					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<ProdDetlInfo> getProdDetlContainerList(Map<String, ProdDetlInfo> map1, Map<String, ProdDetlInfo> map2) {
		String sql =	" SELECT	A.* " +
						" FROM		" + TABLE_PRODDETL_INFO + " A, " + TABLE_PRODLOCN_INFO + " B " +
						" WHERE		B." + KEY_SCW_TYPE + " = 2 AND A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<ProdDetlInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					ProdDetlInfo info = new ProdDetlInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
					info.strItemUPC = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC));
					info.strItemUPC20 = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC20));
					info.nWeight = cursor.getFloat(cursor.getColumnIndex(KEY_WEIGHT));
					info.strItemType = cursor.getString(cursor.getColumnIndex(KEY_ITEM_TYPE));
					info.strProdGrp = cursor.getString(cursor.getColumnIndex(KEY_PROD_GRP));
					info.strPromoGrp = cursor.getString(cursor.getColumnIndex(KEY_PROMO_GRP));
					info.strMarketGrp = cursor.getString(cursor.getColumnIndex(KEY_MARKET_GRP));
					info.strProdLine = cursor.getString(cursor.getColumnIndex(KEY_PROD_LINE));
					info.strPackSize = cursor.getString(cursor.getColumnIndex(KEY_PACK_SIZE));
					info.nShelfLife = cursor.getInt(cursor.getColumnIndex(KEY_SHELF_LIFE));
					info.strBrand = cursor.getString(cursor.getColumnIndex(KEY_BRAND));
					info.strSubBrand = cursor.getString(cursor.getColumnIndex(KEY_SUB_BRAND));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strActiveDate = cursor.getString(cursor.getColumnIndex(KEY_ACTIVE_DATE));
					info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
					info.strSalesAllowed = cursor.getString(cursor.getColumnIndex(KEY_SALES_ALLOWED));
					info.strItemInactiveSwitch = cursor.getString(cursor.getColumnIndex(KEY_ITEM_INACTIVE_SWITCH));
					info.strGroupFilterKey = cursor.getString(cursor.getColumnIndex(KEY_GROUP_FILTER_KEY));
					info.strServiceFee = cursor.getString(cursor.getColumnIndex(KEY_SERVICE_FEE));
					info.nMaxQty = cursor.getFloat(cursor.getColumnIndex(KEY_MAX_QTY));
					info.nConsumerUnit = cursor.getInt(cursor.getColumnIndex(KEY_CONSUMER_UNIT));
					info.strImageUrl = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URL));
					info.strLotControl = cursor.getString(cursor.getColumnIndex(KEY_LOT_CONTROL));

					cursor.moveToNext();

					// we need to skip the inactive info
					if (info.isInactive()) {
						continue;
					}

					if(map1 != null)
					{
						map1.put(info.strItemNo, info);
					}
					if(map2 != null)
					{
						if(!TextUtils.isEmpty(info.strItemNo))
						{
							map2.put(info.strItemNo, info);
						}
						if(!TextUtils.isEmpty(info.strItemUPC) && info.strItemUPC.length() > 2)
						{
							map2.put(info.strItemUPC, info);
						}
						if(!TextUtils.isEmpty(info.strItemUPC20) && info.strItemUPC20.length() > 2)
						{
							map2.put(info.strItemUPC20, info);
						}
					}

					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}


	public List<ProdDetlInfo> getProdDetlList(Map<String, ProdDetlInfo> itemMap, Map<String, ProdDetlInfo> upcMap) {
		String sql =	" SELECT	A.*, B." + KEY_UNIT_UOM + ", B." + KEY_CASE_UOM + ", B." + KEY_FULL_CASE + ", B." + KEY_SCW_TYPE + ", B." + KEY_ORDER_SEQ + ", B." + KEY_CASE_FACTOR +
				" FROM		" + TABLE_PRODDETL_INFO + " A, " + TABLE_PRODLOCN_INFO + " B " +
				" WHERE		A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO;
		Cursor cursor = null;
		List<ProdDetlInfo> list = new ArrayList<>();
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					ProdDetlInfo info = new ProdDetlInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
					info.strItemUPC = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC));
					info.strItemUPC20 = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC20));
					info.nWeight = cursor.getFloat(cursor.getColumnIndex(KEY_WEIGHT));
					info.strItemType = cursor.getString(cursor.getColumnIndex(KEY_ITEM_TYPE));
					info.strProdGrp = cursor.getString(cursor.getColumnIndex(KEY_PROD_GRP));
					info.strPromoGrp = cursor.getString(cursor.getColumnIndex(KEY_PROMO_GRP));
					info.strMarketGrp = cursor.getString(cursor.getColumnIndex(KEY_MARKET_GRP));
					info.strProdLine = cursor.getString(cursor.getColumnIndex(KEY_PROD_LINE));
					info.strPackSize = cursor.getString(cursor.getColumnIndex(KEY_PACK_SIZE));
					info.nShelfLife = cursor.getInt(cursor.getColumnIndex(KEY_SHELF_LIFE));
					info.strBrand = cursor.getString(cursor.getColumnIndex(KEY_BRAND));
					info.strSubBrand = cursor.getString(cursor.getColumnIndex(KEY_SUB_BRAND));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strActiveDate = cursor.getString(cursor.getColumnIndex(KEY_ACTIVE_DATE));
					info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
					info.strSalesAllowed = cursor.getString(cursor.getColumnIndex(KEY_SALES_ALLOWED));
					info.strItemInactiveSwitch = cursor.getString(cursor.getColumnIndex(KEY_ITEM_INACTIVE_SWITCH));
					info.strGroupFilterKey = cursor.getString(cursor.getColumnIndex(KEY_GROUP_FILTER_KEY));
					info.strServiceFee = cursor.getString(cursor.getColumnIndex(KEY_SERVICE_FEE));
					info.nMaxQty = cursor.getFloat(cursor.getColumnIndex(KEY_MAX_QTY));
					info.prodLocnInfo.strUnitUOM = cursor.getString(cursor.getColumnIndex(KEY_UNIT_UOM));
					info.prodLocnInfo.strCaseUOM = cursor.getString(cursor.getColumnIndex(KEY_CASE_UOM));
					info.prodLocnInfo.strFullCase = cursor.getString(cursor.getColumnIndex(KEY_FULL_CASE));
					info.prodLocnInfo.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
					info.prodLocnInfo.nCaseFactor = cursor.getInt(cursor.getColumnIndex(KEY_CASE_FACTOR));
					info.nConsumerUnit = cursor.getInt(cursor.getColumnIndex(KEY_CONSUMER_UNIT));
					info.strImageUrl = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URL));
					info.strLotControl = cursor.getString(cursor.getColumnIndex(KEY_LOT_CONTROL));
					cursor.moveToNext();

					// we need to skip the inactive info
					if (info.isInactive()) {
						continue;
					}

					list.add(info);
					if( itemMap != null )
					{
						if(!TextUtils.isEmpty(info.strItemNo))
						{
							itemMap.put(info.strItemNo, info);
						}
					}
					if( upcMap != null )
					{
						if(!TextUtils.isEmpty(info.strItemUPC) && info.strItemUPC.length() > 2)
						{
							upcMap.put(info.strItemUPC, info);
						}
						if(!TextUtils.isEmpty(info.strItemUPC20) && info.strItemUPC20.length() > 2)
						{
							upcMap.put(info.strItemUPC20, info);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<ProdDetlInfo> getProdDetlListWithInactiveItems(Map<String, ProdDetlInfo> itemMap, Map<String, ProdDetlInfo> upcMap) {
		String sql =	" SELECT	A.*, B." + KEY_UNIT_UOM + ", B." + KEY_CASE_UOM + ", B." + KEY_FULL_CASE + ", B." + KEY_SCW_TYPE + ", B." + KEY_ORDER_SEQ + ", B." + KEY_CASE_FACTOR +
				" FROM		" + TABLE_PRODDETL_INFO + " A, " + TABLE_PRODLOCN_INFO + " B " +
				" WHERE		A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO;
		Cursor cursor = null;
		List<ProdDetlInfo> list = new ArrayList<>();
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					ProdDetlInfo info = new ProdDetlInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
					info.strItemUPC = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC));
					info.strItemUPC20 = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC20));
					info.nWeight = cursor.getFloat(cursor.getColumnIndex(KEY_WEIGHT));
					info.strItemType = cursor.getString(cursor.getColumnIndex(KEY_ITEM_TYPE));
					info.strProdGrp = cursor.getString(cursor.getColumnIndex(KEY_PROD_GRP));
					info.strPromoGrp = cursor.getString(cursor.getColumnIndex(KEY_PROMO_GRP));
					info.strMarketGrp = cursor.getString(cursor.getColumnIndex(KEY_MARKET_GRP));
					info.strProdLine = cursor.getString(cursor.getColumnIndex(KEY_PROD_LINE));
					info.strPackSize = cursor.getString(cursor.getColumnIndex(KEY_PACK_SIZE));
					info.nShelfLife = cursor.getInt(cursor.getColumnIndex(KEY_SHELF_LIFE));
					info.strBrand = cursor.getString(cursor.getColumnIndex(KEY_BRAND));
					info.strSubBrand = cursor.getString(cursor.getColumnIndex(KEY_SUB_BRAND));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strActiveDate = cursor.getString(cursor.getColumnIndex(KEY_ACTIVE_DATE));
					info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
					info.strSalesAllowed = cursor.getString(cursor.getColumnIndex(KEY_SALES_ALLOWED));
					info.strItemInactiveSwitch = cursor.getString(cursor.getColumnIndex(KEY_ITEM_INACTIVE_SWITCH));
					info.strGroupFilterKey = cursor.getString(cursor.getColumnIndex(KEY_GROUP_FILTER_KEY));
					info.strServiceFee = cursor.getString(cursor.getColumnIndex(KEY_SERVICE_FEE));
					info.nMaxQty = cursor.getFloat(cursor.getColumnIndex(KEY_MAX_QTY));
					info.prodLocnInfo.strUnitUOM = cursor.getString(cursor.getColumnIndex(KEY_UNIT_UOM));
					info.prodLocnInfo.strCaseUOM = cursor.getString(cursor.getColumnIndex(KEY_CASE_UOM));
					info.prodLocnInfo.strFullCase = cursor.getString(cursor.getColumnIndex(KEY_FULL_CASE));
					info.prodLocnInfo.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
					info.prodLocnInfo.nCaseFactor = cursor.getInt(cursor.getColumnIndex(KEY_CASE_FACTOR));
					info.nConsumerUnit = cursor.getInt(cursor.getColumnIndex(KEY_CONSUMER_UNIT));
					info.strImageUrl = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URL));
					info.strLotControl = cursor.getString(cursor.getColumnIndex(KEY_LOT_CONTROL));
					cursor.moveToNext();

					list.add(info);
					if( itemMap != null )
					{
						if(!TextUtils.isEmpty(info.strItemNo))
						{
							itemMap.put(info.strItemNo, info);
						}
					}
					if( upcMap != null )
					{
						if(!TextUtils.isEmpty(info.strItemUPC) && info.strItemUPC.length() > 2)
						{
							upcMap.put(info.strItemUPC, info);
						}
						if(!TextUtils.isEmpty(info.strItemUPC20) && info.strItemUPC20.length() > 2)
						{
							upcMap.put(info.strItemUPC20, info);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public ProdDetlInfo getProdDetlInfo(String strItemNo) {
		String sql =	" SELECT	A.*, B." + KEY_UNIT_UOM + ", B." + KEY_CASE_UOM + ", B." + KEY_FULL_CASE + ", B." + KEY_SCW_TYPE + ", B." + KEY_CASE_FACTOR +
				" FROM		" + TABLE_PRODDETL_INFO + " A, " + TABLE_PRODLOCN_INFO + " B " +
				" WHERE		A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO + " AND A." + KEY_ITEM_NO + " = '" +  strItemNo + "'";
		Cursor cursor = null;
		ProdDetlInfo info = null;
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();

				info = new ProdDetlInfo();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
				info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
				info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
				info.strItemUPC = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC));
				info.strItemUPC20 = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC20));
				info.nWeight = cursor.getFloat(cursor.getColumnIndex(KEY_WEIGHT));
				info.strItemType = cursor.getString(cursor.getColumnIndex(KEY_ITEM_TYPE));
				info.strProdGrp = cursor.getString(cursor.getColumnIndex(KEY_PROD_GRP));
				info.strPromoGrp = cursor.getString(cursor.getColumnIndex(KEY_PROMO_GRP));
				info.strMarketGrp = cursor.getString(cursor.getColumnIndex(KEY_MARKET_GRP));
				info.strProdLine = cursor.getString(cursor.getColumnIndex(KEY_PROD_LINE));
				info.strPackSize = cursor.getString(cursor.getColumnIndex(KEY_PACK_SIZE));
				info.nShelfLife = cursor.getInt(cursor.getColumnIndex(KEY_SHELF_LIFE));
				info.strBrand = cursor.getString(cursor.getColumnIndex(KEY_BRAND));
				info.strSubBrand = cursor.getString(cursor.getColumnIndex(KEY_SUB_BRAND));
				info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
				info.strActiveDate = cursor.getString(cursor.getColumnIndex(KEY_ACTIVE_DATE));
				info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
				info.strSalesAllowed = cursor.getString(cursor.getColumnIndex(KEY_SALES_ALLOWED));
				info.strItemInactiveSwitch = cursor.getString(cursor.getColumnIndex(KEY_ITEM_INACTIVE_SWITCH));
				info.strGroupFilterKey = cursor.getString(cursor.getColumnIndex(KEY_GROUP_FILTER_KEY));
				info.strServiceFee = cursor.getString(cursor.getColumnIndex(KEY_SERVICE_FEE));
				info.nMaxQty = cursor.getFloat(cursor.getColumnIndex(KEY_MAX_QTY));
				info.nConsumerUnit = cursor.getInt(cursor.getColumnIndex(KEY_CONSUMER_UNIT));
				info.strImageUrl = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URL));
				info.strLotControl = cursor.getString(cursor.getColumnIndex(KEY_LOT_CONTROL));
				info.prodLocnInfo.strUnitUOM = cursor.getString(cursor.getColumnIndex(KEY_UNIT_UOM));
				info.prodLocnInfo.strCaseUOM = cursor.getString(cursor.getColumnIndex(KEY_CASE_UOM));
				info.prodLocnInfo.strFullCase = cursor.getString(cursor.getColumnIndex(KEY_FULL_CASE));
				info.prodLocnInfo.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
				info.prodLocnInfo.nCaseFactor = cursor.getInt(cursor.getColumnIndex(KEY_CASE_FACTOR));
				cursor.moveToNext();

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	public List<ProdDetlInfo> getProdDetlList() {
		String sql =	" SELECT	A.*, B." + KEY_UNIT_UOM + ", B." + KEY_CASE_UOM + ", B." + KEY_FULL_CASE + ", B." + KEY_SCW_TYPE + ", B." + KEY_CASE_FACTOR +
						" FROM		" + TABLE_PRODDETL_INFO + " A, " + TABLE_PRODLOCN_INFO + " B " +
						" WHERE		A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO;
		Cursor cursor = null;
		List<ProdDetlInfo> list = new ArrayList<>();
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					ProdDetlInfo info = new ProdDetlInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
					info.strItemUPC = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC));
					info.strItemUPC20 = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC20));
					info.nWeight = cursor.getFloat(cursor.getColumnIndex(KEY_WEIGHT));
					info.strItemType = cursor.getString(cursor.getColumnIndex(KEY_ITEM_TYPE));
					info.strProdGrp = cursor.getString(cursor.getColumnIndex(KEY_PROD_GRP));
					info.strPromoGrp = cursor.getString(cursor.getColumnIndex(KEY_PROMO_GRP));
					info.strMarketGrp = cursor.getString(cursor.getColumnIndex(KEY_MARKET_GRP));
					info.strProdLine = cursor.getString(cursor.getColumnIndex(KEY_PROD_LINE));
					info.strPackSize = cursor.getString(cursor.getColumnIndex(KEY_PACK_SIZE));
					info.nShelfLife = cursor.getInt(cursor.getColumnIndex(KEY_SHELF_LIFE));
					info.strBrand = cursor.getString(cursor.getColumnIndex(KEY_BRAND));
					info.strSubBrand = cursor.getString(cursor.getColumnIndex(KEY_SUB_BRAND));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strActiveDate = cursor.getString(cursor.getColumnIndex(KEY_ACTIVE_DATE));
					info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
					info.strSalesAllowed = cursor.getString(cursor.getColumnIndex(KEY_SALES_ALLOWED));
					info.strItemInactiveSwitch = cursor.getString(cursor.getColumnIndex(KEY_ITEM_INACTIVE_SWITCH));
					info.strGroupFilterKey = cursor.getString(cursor.getColumnIndex(KEY_GROUP_FILTER_KEY));
					info.strServiceFee = cursor.getString(cursor.getColumnIndex(KEY_SERVICE_FEE));
					info.nMaxQty = cursor.getFloat(cursor.getColumnIndex(KEY_MAX_QTY));
					info.strImageUrl = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URL));
					info.strLotControl = cursor.getString(cursor.getColumnIndex(KEY_LOT_CONTROL));
					info.nConsumerUnit = cursor.getInt(cursor.getColumnIndex(KEY_CONSUMER_UNIT));
					info.prodLocnInfo.strUnitUOM = cursor.getString(cursor.getColumnIndex(KEY_UNIT_UOM));
					info.prodLocnInfo.strCaseUOM = cursor.getString(cursor.getColumnIndex(KEY_CASE_UOM));
					info.prodLocnInfo.nCaseFactor = cursor.getInt(cursor.getColumnIndex(KEY_CASE_FACTOR));
					info.prodLocnInfo.strFullCase = cursor.getString(cursor.getColumnIndex(KEY_FULL_CASE));
					info.prodLocnInfo.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
					cursor.moveToNext();

					// we need to skip the inactive info
					if (info.isInactive()) {
						continue;
					}
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<ProdDetlInfo> getProdDetlList(Map<String, ProdDetlInfo> upc_map) {
		String sql =	" SELECT	A.*, B." + KEY_UNIT_UOM + ", B." + KEY_CASE_UOM + ", B." + KEY_FULL_CASE + ", B." + KEY_SCW_TYPE + ", B." + KEY_CASE_FACTOR +
						" FROM		" + TABLE_PRODDETL_INFO + " A, " + TABLE_PRODLOCN_INFO + " B " +
						" WHERE		A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO;
		Cursor cursor = null;
		List<ProdDetlInfo> list = new ArrayList<>();
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					ProdDetlInfo info = new ProdDetlInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
					info.strItemUPC = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC));
					info.strItemUPC20 = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC20));
					info.nWeight = cursor.getFloat(cursor.getColumnIndex(KEY_WEIGHT));
					info.strItemType = cursor.getString(cursor.getColumnIndex(KEY_ITEM_TYPE));
					info.strProdGrp = cursor.getString(cursor.getColumnIndex(KEY_PROD_GRP));
					info.strPromoGrp = cursor.getString(cursor.getColumnIndex(KEY_PROMO_GRP));
					info.strMarketGrp = cursor.getString(cursor.getColumnIndex(KEY_MARKET_GRP));
					info.strProdLine = cursor.getString(cursor.getColumnIndex(KEY_PROD_LINE));
					info.strPackSize = cursor.getString(cursor.getColumnIndex(KEY_PACK_SIZE));
					info.nShelfLife = cursor.getInt(cursor.getColumnIndex(KEY_SHELF_LIFE));
					info.strBrand = cursor.getString(cursor.getColumnIndex(KEY_BRAND));
					info.strSubBrand = cursor.getString(cursor.getColumnIndex(KEY_SUB_BRAND));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strActiveDate = cursor.getString(cursor.getColumnIndex(KEY_ACTIVE_DATE));
					info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
					info.strSalesAllowed = cursor.getString(cursor.getColumnIndex(KEY_SALES_ALLOWED));
					info.strItemInactiveSwitch = cursor.getString(cursor.getColumnIndex(KEY_ITEM_INACTIVE_SWITCH));
					info.strGroupFilterKey = cursor.getString(cursor.getColumnIndex(KEY_GROUP_FILTER_KEY));
					info.strServiceFee = cursor.getString(cursor.getColumnIndex(KEY_SERVICE_FEE));
					info.nMaxQty = cursor.getFloat(cursor.getColumnIndex(KEY_MAX_QTY));
					info.nConsumerUnit = cursor.getInt(cursor.getColumnIndex(KEY_CONSUMER_UNIT));
					info.strImageUrl = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URL));
					info.strLotControl = cursor.getString(cursor.getColumnIndex(KEY_LOT_CONTROL));
					info.prodLocnInfo.strUnitUOM = cursor.getString(cursor.getColumnIndex(KEY_UNIT_UOM));
					info.prodLocnInfo.strCaseUOM = cursor.getString(cursor.getColumnIndex(KEY_CASE_UOM));
					info.prodLocnInfo.nCaseFactor = cursor.getInt(cursor.getColumnIndex(KEY_CASE_FACTOR));
					info.prodLocnInfo.strFullCase = cursor.getString(cursor.getColumnIndex(KEY_FULL_CASE));
					info.prodLocnInfo.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));

					cursor.moveToNext();

					// we need to skip the inactive info
					if (info.isInactive()) {
						continue;
					}

					if(upc_map != null)
					{
						if(!TextUtils.isEmpty(info.strItemNo))
						{
							upc_map.put(info.strItemNo, info);
						}
						if(!TextUtils.isEmpty(info.strItemUPC) && info.strItemUPC.length() > 2)
						{
							upc_map.put(info.strItemUPC, info);
						}
						if(!TextUtils.isEmpty(info.strItemUPC20) && info.strItemUPC20.length() > 2)
						{
							upc_map.put(info.strItemUPC20, info);
						}
					}
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public long insertProdDetlInfo(ProdDetlInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_DESCRIPTION, info.strDescription);
		values.put(KEY_SHORT_DESC, info.strShortDesc);
		values.put(KEY_ITEM_UPC, info.strItemUPC);
		values.put(KEY_ITEM_UPC20, info.strItemUPC20);
		values.put(KEY_WEIGHT, info.nWeight);
		values.put(KEY_ITEM_TYPE, info.strItemType);
		values.put(KEY_PROD_GRP, info.strProdGrp);
		values.put(KEY_PROMO_GRP, info.strPromoGrp);
		values.put(KEY_MARKET_GRP, info.strMarketGrp);
		values.put(KEY_PROD_LINE, info.strProdLine);
		values.put(KEY_PACK_SIZE, info.strPackSize);
		values.put(KEY_SHELF_LIFE, info.nShelfLife);
		values.put(KEY_BRAND, info.strBrand);
		values.put(KEY_SUB_BRAND, info.strSubBrand);
		values.put(KEY_TYPE, info.strType);
		values.put(KEY_ACTIVE_DATE, info.strActiveDate);
		values.put(KEY_DUMPS_ALLOWED, info.strDumpsAllowed);
		values.put(KEY_SALES_ALLOWED, info.strSalesAllowed);
		values.put(KEY_ITEM_INACTIVE_SWITCH, info.strItemInactiveSwitch);
		values.put(KEY_GROUP_FILTER_KEY, info.strGroupFilterKey);
		values.put(KEY_SERVICE_FEE, info.strServiceFee);
		values.put(KEY_MAX_QTY, info.nMaxQty);
		values.put(KEY_CONSUMER_UNIT, info.nConsumerUnit);
		values.put(KEY_IMAGE_URL, info.strImageUrl);
		values.put(KEY_LOT_CONTROL, info.strLotControl);
		return mDb.insert(TABLE_PRODDETL_INFO, null, values);
	}

	public boolean deleteAllProdDetlInfo() throws SQLException {
		return mDb.delete(TABLE_PRODDETL_INFO, null, null) > 0;
	}


	public List<ProdDetlInfo> getProductList(Map<String, ProdDetlInfo> itemMap, Map<String, ProdDetlInfo> upcMap) {
		String sql =	" SELECT	A.*, B." + KEY_LOCN_NO + ", B." + KEY_BASE_PRICE + ", B." + KEY_DRIVER_PRICE + ", B." + KEY_CASE_FACTOR + ", B." + KEY_RETAIL_PRICE + ", B." + KEY_FULL_CASE + ", B." + KEY_SCW_TYPE +
				" FROM		" + TABLE_PRODDETL_INFO + " A, " + TABLE_PRODLOCN_INFO + " B " +
				" WHERE		A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO;
		Cursor cursor = null;
		List<ProdDetlInfo> list = new ArrayList<ProdDetlInfo>();
		try
		{
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0)
			{
				Map<String, List<UPromotionInfo>> uPromotionListMap = getUPromotionInfoList();
				Map<String, PricingInfo> pricingMap;
				Map<String, PriceGrpInfo> priceGrpMap;
				Map<String, PriceGrpInfo> altPriceGrpMap;
				if(Global.gCurCustomerInfo != null)
				{
					pricingMap = getPricingInfo(Global.gCurCustomerInfo.nCustNo);
					priceGrpMap = getPriceGrpInfo(Global.gCurCustomerInfo.nPriceGrp);
					altPriceGrpMap = getPriceGrpInfo(Global.gCurCustomerInfo.nAltPriceGrp);
				}
				else
				{
					pricingMap = getPricingInfo(0);
					priceGrpMap = getPriceGrpInfo(0);
					altPriceGrpMap = getPriceGrpInfo(0);
				}


				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++)
				{
					ProdDetlInfo info = new ProdDetlInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
					info.strItemUPC = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC));
					info.strItemUPC20 = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC20));
					info.nWeight = cursor.getFloat(cursor.getColumnIndex(KEY_WEIGHT));
					info.strItemType = cursor.getString(cursor.getColumnIndex(KEY_ITEM_TYPE));
					info.strProdGrp = cursor.getString(cursor.getColumnIndex(KEY_PROD_GRP));
					info.strPromoGrp = cursor.getString(cursor.getColumnIndex(KEY_PROMO_GRP));
					info.strMarketGrp = cursor.getString(cursor.getColumnIndex(KEY_MARKET_GRP));
					info.strProdLine = cursor.getString(cursor.getColumnIndex(KEY_PROD_LINE));
					info.strPackSize = cursor.getString(cursor.getColumnIndex(KEY_PACK_SIZE));
					info.nShelfLife = cursor.getInt(cursor.getColumnIndex(KEY_SHELF_LIFE));
					info.strBrand = cursor.getString(cursor.getColumnIndex(KEY_BRAND));
					info.strSubBrand = cursor.getString(cursor.getColumnIndex(KEY_SUB_BRAND));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strActiveDate = cursor.getString(cursor.getColumnIndex(KEY_ACTIVE_DATE));
					info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
					info.strSalesAllowed = cursor.getString(cursor.getColumnIndex(KEY_SALES_ALLOWED));
					info.strItemInactiveSwitch = cursor.getString(cursor.getColumnIndex(KEY_ITEM_INACTIVE_SWITCH));
					info.strGroupFilterKey = cursor.getString(cursor.getColumnIndex(KEY_GROUP_FILTER_KEY));
					info.strServiceFee = cursor.getString(cursor.getColumnIndex(KEY_SERVICE_FEE));
					info.nMaxQty = cursor.getFloat(cursor.getColumnIndex(KEY_MAX_QTY));
					info.nConsumerUnit = cursor.getInt(cursor.getColumnIndex(KEY_CONSUMER_UNIT));
					info.strImageUrl = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URL));
					info.strLotControl = cursor.getString(cursor.getColumnIndex(KEY_LOT_CONTROL));
					info.prodLocnInfo.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));
					info.prodLocnInfo.nBasePrice = cursor.getLong(cursor.getColumnIndex(KEY_BASE_PRICE));
					info.prodLocnInfo.nDriverPrice = cursor.getLong(cursor.getColumnIndex(KEY_DRIVER_PRICE));
					info.prodLocnInfo.nCaseFactor = cursor.getInt(cursor.getColumnIndex(KEY_CASE_FACTOR));
					info.prodLocnInfo.nRetailPrice = cursor.getInt(cursor.getColumnIndex(KEY_RETAIL_PRICE));
					info.prodLocnInfo.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
					info.prodLocnInfo.strFullCase = cursor.getString(cursor.getColumnIndex(KEY_FULL_CASE));
					
					if (info.isInactive()) {
						cursor.moveToNext();
						continue;
					}

					info.nPrice = 0;
					info.nDiscountVal = 0;

					String strItemNo = info.strItemNo;

					float nBasePrice = Utils.getDividedValue(info.prodLocnInfo.nBasePrice);
					float nPrice = 0;
					String strPrcFilRecord = "";
					PricingInfo pricingInfo = pricingMap.get(strItemNo);
					if (pricingInfo != null)
					{
						nPrice = Utils.getDividedValue(pricingInfo.nPrice);
						strPrcFilRecord = pricingInfo.strPrcFilRecord;
					}
					else
					{
						PriceGrpInfo priceGrpInfo = priceGrpMap.get(strItemNo);
						if (priceGrpInfo != null)
						{
							nPrice = Utils.getDividedValue(priceGrpInfo.nPrice);
							strPrcFilRecord = priceGrpInfo.strPrcFilRecord;
						}
						else
						{
							PriceGrpInfo altPriceGrpInfo = altPriceGrpMap.get(strItemNo);
							if(altPriceGrpInfo != null)
							{
								nPrice = Utils.getDividedValue(altPriceGrpInfo.nPrice);
								strPrcFilRecord = altPriceGrpInfo.strPrcFilRecord;
							}
						}
					}
					if (strPrcFilRecord.equals("") == true)
					{
						info.nPrice = nBasePrice;
					}
					else if (strPrcFilRecord.equals("N") == true)
					{
						info.nPrice = nPrice;
					}
					else if (strPrcFilRecord.equals("O") == true)
					{
						info.nPrice = nBasePrice - nPrice;
					}
					else if (strPrcFilRecord.equals("P") == true)
					{
						info.nPrice = nBasePrice / 100 * (100 - nPrice);
					}
					info.nGrossPrice = Utils.get5RoundedValue(mContext, info.nPrice);
					info.nOrgGrossPrice = info.nPrice;

					// Process Promotion
					List<UPromotionInfo> promNovoInfoList = uPromotionListMap.get(strItemNo);
					if( promNovoInfoList == null ) promNovoInfoList = new ArrayList<>();
					List<UPromotionInfo> allPromNovoInfoList = uPromotionListMap.get("*ALL");
					if( allPromNovoInfoList != null )
					{
						for(UPromotionInfo uPromotionInfo : allPromNovoInfoList)
						{
							if(/*Utils.getStringSetting(mContext, Global.PREF_FRGOODPRM).equals("1") &&*/
									uPromotionInfo.nPlanNo == Utils.getIntFromString(Utils.getStringSetting(mContext, Global.PREF_FRGOODPLN)) &&
									uPromotionInfo.nAssignNo == Utils.getIntFromString(Utils.getStringSetting(mContext, Global.PREF_FRGOODASS)))
							{
								continue;
							}
							promNovoInfoList.add(uPromotionInfo.duplicate());
						}
					}

					//Remove Same Promotion List
					List<UPromotionInfo> newPromNovoInfoList = new ArrayList<>();
					Map<String, UPromotionInfo> provMap = new HashMap<>();
					for(UPromotionInfo uPromotionInfo : promNovoInfoList)
					{
						String key = uPromotionInfo.nPromoType + "/" + uPromotionInfo.nDiscAmt;
						if(provMap.get(key) == null)
						{
							provMap.put(key, uPromotionInfo);
							newPromNovoInfoList.add(uPromotionInfo);
						}
					}
					promNovoInfoList = newPromNovoInfoList;

					Collections.sort(promNovoInfoList, new Comparator<UPromotionInfo>() {
						@Override
						public int compare(UPromotionInfo t1, UPromotionInfo t2) {
							if( t1.nSeqNo != t2.nSeqNo )
							{
								return t1.nSeqNo - t2.nSeqNo;
							}
							else
							{
								return t1.nPromoType - t2.nPromoType;
							}
						}
					});

					for(UPromotionInfo uPromotionInfo : promNovoInfoList)
					{
						float nTmpPrice = info.nPrice;
						if (uPromotionInfo.nPromoType == Global.PROMO_TYPE_CENTS_OFF)
						{
							float fDiscCount = Utils.getDividedValue(uPromotionInfo.nDiscAmt);
							info.nPrice = nTmpPrice - fDiscCount;
							uPromotionInfo.nAmount = uPromotionInfo.nDiscAmt;
						}
						else if (uPromotionInfo.nPromoType == Global.PROMO_TYPE_PERCENTAGE_OFF)
						{
							float fDiscCount = Utils.getDividedValue(uPromotionInfo.nDiscAmt);
							float fPro = (100 - fDiscCount);
							info.nPrice = Utils.get5RoundedValue(mContext, nTmpPrice / 100.0f * fPro);
							uPromotionInfo.nAmount = Math.round(((nTmpPrice - info.nPrice)* Global.MULTIPLE_VALUE));
						}
						else if (uPromotionInfo.nPromoType == Global.PROMO_TYPE_REPLACE_PRICE)
						{
							info.nPrice = Utils.getDividedValue(uPromotionInfo.nDiscAmt);
							uPromotionInfo.nAmount = Math.round((nTmpPrice - info.nPrice)* Global.MULTIPLE_VALUE);
						}
					}
					info.uPromotionInfoList = promNovoInfoList;
					info.nOrgPrice = info.nPrice;
					info.nPrice = Utils.get5RoundedValue(mContext, info.nPrice);
					info.nDiscountVal = nBasePrice - info.nPrice;

					cursor.moveToNext();
					if( itemMap != null )
					{
						if(!TextUtils.isEmpty(info.strItemNo))
						{
							itemMap.put(info.strItemNo, info);
						}
					}
					if( upcMap != null )
					{
						if(!TextUtils.isEmpty(info.strItemUPC) && info.strItemUPC.length() > 2)
						{
							upcMap.put(info.strItemUPC, info);
						}
						if(!TextUtils.isEmpty(info.strItemUPC20) && info.strItemUPC20.length() > 2)
						{
							upcMap.put(info.strItemUPC20, info);
						}
					}
					list.add(info);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return list;
	}

	public List<ProdDetlInfo> getProductListWithFreeGoods(String strItemNo)
	{
		String sql = " SELECT	A.*, B." + KEY_LOCN_NO + ", B." + KEY_BASE_PRICE + ", B." + KEY_DRIVER_PRICE + ", B." + KEY_CASE_FACTOR + ", B." + KEY_RETAIL_PRICE + ", B." + KEY_FULL_CASE + ", B." + KEY_SCW_TYPE +
				" FROM		" + TABLE_PRODDETL_INFO + " A, " + TABLE_PRODLOCN_INFO + " B " +
				" WHERE		A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO + " AND (A." + KEY_ITEM_NO + " = '" + strItemNo + "' OR A." + KEY_ITEM_UPC + " = '" + strItemNo + "' OR A." + KEY_ITEM_UPC20 + " = '" + strItemNo + "')";
		Cursor cursor = null;
		List<ProdDetlInfo> list = new ArrayList<ProdDetlInfo>();
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					ProdDetlInfo info = new ProdDetlInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
					info.strItemUPC = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC));
					info.strItemUPC20 = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC20));
					info.nWeight = cursor.getFloat(cursor.getColumnIndex(KEY_WEIGHT));
					info.strItemType = cursor.getString(cursor.getColumnIndex(KEY_ITEM_TYPE));
					info.strProdGrp = cursor.getString(cursor.getColumnIndex(KEY_PROD_GRP));
					info.strPromoGrp = cursor.getString(cursor.getColumnIndex(KEY_PROMO_GRP));
					info.strMarketGrp = cursor.getString(cursor.getColumnIndex(KEY_MARKET_GRP));
					info.strProdLine = cursor.getString(cursor.getColumnIndex(KEY_PROD_LINE));
					info.strPackSize = cursor.getString(cursor.getColumnIndex(KEY_PACK_SIZE));
					info.nShelfLife = cursor.getInt(cursor.getColumnIndex(KEY_SHELF_LIFE));
					info.strBrand = cursor.getString(cursor.getColumnIndex(KEY_BRAND));
					info.strSubBrand = cursor.getString(cursor.getColumnIndex(KEY_SUB_BRAND));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strActiveDate = cursor.getString(cursor.getColumnIndex(KEY_ACTIVE_DATE));
					info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
					info.strSalesAllowed = cursor.getString(cursor.getColumnIndex(KEY_SALES_ALLOWED));
					info.strItemInactiveSwitch = cursor.getString(cursor.getColumnIndex(KEY_ITEM_INACTIVE_SWITCH));
					info.strGroupFilterKey = cursor.getString(cursor.getColumnIndex(KEY_GROUP_FILTER_KEY));
					info.strServiceFee = cursor.getString(cursor.getColumnIndex(KEY_SERVICE_FEE));
					info.strImageUrl = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URL));
					info.strLotControl = cursor.getString(cursor.getColumnIndex(KEY_LOT_CONTROL));
					info.nMaxQty = cursor.getFloat(cursor.getColumnIndex(KEY_MAX_QTY));
					info.nConsumerUnit = cursor.getInt(cursor.getColumnIndex(KEY_CONSUMER_UNIT));
					info.prodLocnInfo.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));
					info.prodLocnInfo.nBasePrice = cursor.getLong(cursor.getColumnIndex(KEY_BASE_PRICE));
					info.prodLocnInfo.nDriverPrice = cursor.getLong(cursor.getColumnIndex(KEY_DRIVER_PRICE));
					info.prodLocnInfo.nCaseFactor = cursor.getInt(cursor.getColumnIndex(KEY_CASE_FACTOR));
					info.prodLocnInfo.nRetailPrice = cursor.getInt(cursor.getColumnIndex(KEY_RETAIL_PRICE));
					info.prodLocnInfo.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
					info.prodLocnInfo.strFullCase = cursor.getString(cursor.getColumnIndex(KEY_FULL_CASE));
					cursor.moveToNext();

					if (info.isInactive()) {
						continue;
					}

					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		List<UPromotionInfo> promSaleInfoList = getSalePromNovoList(strItemNo);
		if( promSaleInfoList == null ) promSaleInfoList = new ArrayList<>();

		//Remove Same Promotion List
		List<UPromotionInfo> newPromSaleInfoList = new ArrayList<>();
		Map<String, UPromotionInfo> provSaleMap = new HashMap<>();
		for(UPromotionInfo uPromotionInfo : promSaleInfoList)
		{
			String key = uPromotionInfo.nPromoType + "/" + uPromotionInfo.nDiscAmt;
			if(provSaleMap.get(key) == null)
			{
				provSaleMap.put(key, uPromotionInfo);
				newPromSaleInfoList.add(uPromotionInfo);
			}
		}
		promSaleInfoList = newPromSaleInfoList;

		Collections.sort(promSaleInfoList, (t1, t2) -> {
			if( t1.nSeqNo != t2.nSeqNo )
			{
				return t1.nSeqNo - t2.nSeqNo;
			}
			else
			{
				return t1.nPromoType - t2.nPromoType;
			}
		});

		List<UPromotionInfo> promNovoInfoList = getFreeGoodsPromNovoList();
		if( promNovoInfoList == null ) promNovoInfoList = new ArrayList<>();

		//Remove Same Promotion List
		List<UPromotionInfo> newPromNovoInfoList = new ArrayList<>();
		Map<String, UPromotionInfo> provMap = new HashMap<>();
		for(UPromotionInfo uPromotionInfo : promNovoInfoList)
		{
			String key = uPromotionInfo.nPromoType + "/" + uPromotionInfo.nDiscAmt;
			if(provMap.get(key) == null)
			{
				provMap.put(key, uPromotionInfo);
				newPromNovoInfoList.add(uPromotionInfo);
			}
		}
		promNovoInfoList = newPromNovoInfoList;

		Collections.sort(promNovoInfoList, (t1, t2) -> {
			if( t1.nSeqNo != t2.nSeqNo )
			{
				return t1.nSeqNo - t2.nSeqNo;
			}
			else
			{
				return t1.nPromoType - t2.nPromoType;
			}
		});

		Map<String, PricingInfo> pricingMap;
		Map<String, PriceGrpInfo> priceGrpMap;
		Map<String, PriceGrpInfo> altPriceGrpMap;
		if(Global.gCurCustomerInfo != null)
		{
			pricingMap = getPricingInfo(Global.gCurCustomerInfo.nCustNo);
			priceGrpMap = getPriceGrpInfo(Global.gCurCustomerInfo.nPriceGrp);
			altPriceGrpMap = getPriceGrpInfo(Global.gCurCustomerInfo.nAltPriceGrp);
		}
		else
		{
			pricingMap = getPricingInfo(0);
			priceGrpMap = getPriceGrpInfo(0);
			altPriceGrpMap = getPriceGrpInfo(0);
		}

		for(ProdDetlInfo info : list)
		{
			info.nPrice = 0;
			info.nDiscountVal = 0;

			float nBasePrice = Utils.getDividedValue(info.prodLocnInfo.nBasePrice);
			float nPrice = 0;
			String strPrcFilRecord = "";
			PricingInfo pricingInfo = pricingMap.get(strItemNo);
			if (pricingInfo != null)
			{
				nPrice = Utils.getDividedValue(pricingInfo.nPrice);
				strPrcFilRecord = pricingInfo.strPrcFilRecord;
			}
			else
			{
				PriceGrpInfo priceGrpInfo = priceGrpMap.get(strItemNo);
				if (priceGrpInfo != null)
				{
					nPrice = Utils.getDividedValue(priceGrpInfo.nPrice);
					strPrcFilRecord = priceGrpInfo.strPrcFilRecord;
				}
				else
				{
					PriceGrpInfo altPriceGrpInfo = altPriceGrpMap.get(strItemNo);
					if(altPriceGrpInfo != null)
					{
						nPrice = Utils.getDividedValue(altPriceGrpInfo.nPrice);
						strPrcFilRecord = altPriceGrpInfo.strPrcFilRecord;
					}
				}
			}
			if (strPrcFilRecord.equals("") == true)
			{
				info.nPrice = nBasePrice;
			}
			else if (strPrcFilRecord.equals("N") == true)
			{
				info.nPrice = nPrice;
			}
			else if (strPrcFilRecord.equals("O") == true)
			{
				info.nPrice = nBasePrice - nPrice;
			}
			else if (strPrcFilRecord.equals("P") == true)
			{
				info.nPrice = nBasePrice / 100 * (100 - nPrice);
			}
			info.nGrossPrice = Utils.get5RoundedValue(mContext, info.nPrice);

			//Apply Sale Promotion
			for(UPromotionInfo uPromotionInfo : promSaleInfoList)
			{
				float nTmpPrice = info.nPrice;
				if (uPromotionInfo.nPromoType == Global.PROMO_TYPE_CENTS_OFF)
				{
					float fDiscCount = Utils.getDividedValue(uPromotionInfo.nDiscAmt);
					info.nPrice = nTmpPrice - fDiscCount;
					uPromotionInfo.nAmount = uPromotionInfo.nDiscAmt;
				}
				else if (uPromotionInfo.nPromoType == Global.PROMO_TYPE_PERCENTAGE_OFF)
				{
					float fDiscCount = Utils.getDividedValue(uPromotionInfo.nDiscAmt);
					float fPro = (100 - fDiscCount);
					info.nPrice = Utils.get5RoundedValue(mContext, nTmpPrice / 100.0f * fPro);
					uPromotionInfo.nAmount = Math.round(((nTmpPrice - info.nPrice)* Global.MULTIPLE_VALUE));
				}
				else if (uPromotionInfo.nPromoType == Global.PROMO_TYPE_REPLACE_PRICE)
				{
					info.nPrice = Utils.getDividedValue(uPromotionInfo.nDiscAmt);
					uPromotionInfo.nAmount = Math.round((nTmpPrice - info.nPrice)* Global.MULTIPLE_VALUE);
				}
			}
			info.uPromotionInfoList.addAll(promSaleInfoList);
			info.nOrgGrossPrice = info.nPrice;

			//Apply Free Promotion
			for(UPromotionInfo uPromotionInfo : promNovoInfoList)
			{
				float nTmpPrice = info.nPrice;
				if (uPromotionInfo.nPromoType == Global.PROMO_TYPE_CENTS_OFF)
				{
					float fDiscCount = Utils.getDividedValue(uPromotionInfo.nDiscAmt);
					info.nPrice = nTmpPrice - fDiscCount;
					uPromotionInfo.nAmount = uPromotionInfo.nDiscAmt;
				}
				else if (uPromotionInfo.nPromoType == Global.PROMO_TYPE_PERCENTAGE_OFF)
				{
					float fDiscCount = Utils.getDividedValue(uPromotionInfo.nDiscAmt);
					float fPro = (100 - fDiscCount);
					info.nPrice = Utils.get5RoundedValue(mContext, nTmpPrice / 100.0f * fPro);
					uPromotionInfo.nAmount = Math.round(((nTmpPrice - info.nPrice)* Global.MULTIPLE_VALUE));
				}
				else if (uPromotionInfo.nPromoType == Global.PROMO_TYPE_REPLACE_PRICE)
				{
					info.nPrice = Utils.getDividedValue(uPromotionInfo.nDiscAmt);
					uPromotionInfo.nAmount = Math.round((nTmpPrice - info.nPrice)* Global.MULTIPLE_VALUE);
				}
			}
			info.uPromotionInfoList.addAll(promNovoInfoList);
			info.nOrgPrice = info.nPrice;
			info.nPrice = Utils.get5RoundedValue(mContext, info.nPrice);
//			float fGrossPrice = Math.max(info.nOrgGrossPrice, info.nGrossPrice);
//			info.nOrgGrossPrice = fGrossPrice;
			info.nDiscountVal = info.nOrgGrossPrice - info.nPrice;

		}
		return list;
	}

	public List<ProdDetlInfo> getProductList(String strItemNo) {
		String sql = " SELECT	A.*, B." + KEY_LOCN_NO + ", B." + KEY_BASE_PRICE + ", B." + KEY_DRIVER_PRICE + ", B." + KEY_CASE_FACTOR + ", B." + KEY_RETAIL_PRICE + ", B." + KEY_FULL_CASE + ", B." + KEY_SCW_TYPE +
				" FROM		" + TABLE_PRODDETL_INFO + " A, " + TABLE_PRODLOCN_INFO + " B " +
				" WHERE		A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO + " AND (A." + KEY_ITEM_NO + " = '" + strItemNo + "' OR A." + KEY_ITEM_UPC + " = '" + strItemNo + "' OR A." + KEY_ITEM_UPC20 + " = '" + strItemNo + "')";
		Cursor cursor = null;
		List<ProdDetlInfo> list = new ArrayList<ProdDetlInfo>();
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					ProdDetlInfo info = new ProdDetlInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
					info.strItemUPC = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC));
					info.strItemUPC20 = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC20));
					info.nWeight = cursor.getFloat(cursor.getColumnIndex(KEY_WEIGHT));
					info.strItemType = cursor.getString(cursor.getColumnIndex(KEY_ITEM_TYPE));
					info.strProdGrp = cursor.getString(cursor.getColumnIndex(KEY_PROD_GRP));
					info.strPromoGrp = cursor.getString(cursor.getColumnIndex(KEY_PROMO_GRP));
					info.strMarketGrp = cursor.getString(cursor.getColumnIndex(KEY_MARKET_GRP));
					info.strProdLine = cursor.getString(cursor.getColumnIndex(KEY_PROD_LINE));
					info.strPackSize = cursor.getString(cursor.getColumnIndex(KEY_PACK_SIZE));
					info.nShelfLife = cursor.getInt(cursor.getColumnIndex(KEY_SHELF_LIFE));
					info.strBrand = cursor.getString(cursor.getColumnIndex(KEY_BRAND));
					info.strSubBrand = cursor.getString(cursor.getColumnIndex(KEY_SUB_BRAND));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strActiveDate = cursor.getString(cursor.getColumnIndex(KEY_ACTIVE_DATE));
					info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
					info.strSalesAllowed = cursor.getString(cursor.getColumnIndex(KEY_SALES_ALLOWED));
					info.strItemInactiveSwitch = cursor.getString(cursor.getColumnIndex(KEY_ITEM_INACTIVE_SWITCH));
					info.strGroupFilterKey = cursor.getString(cursor.getColumnIndex(KEY_GROUP_FILTER_KEY));
					info.strServiceFee = cursor.getString(cursor.getColumnIndex(KEY_SERVICE_FEE));
					info.strImageUrl = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URL));
					info.strLotControl = cursor.getString(cursor.getColumnIndex(KEY_LOT_CONTROL));
					info.nMaxQty = cursor.getFloat(cursor.getColumnIndex(KEY_MAX_QTY));
					info.nConsumerUnit = cursor.getInt(cursor.getColumnIndex(KEY_CONSUMER_UNIT));
					info.prodLocnInfo.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));
					info.prodLocnInfo.nBasePrice = cursor.getLong(cursor.getColumnIndex(KEY_BASE_PRICE));
					info.prodLocnInfo.nDriverPrice = cursor.getLong(cursor.getColumnIndex(KEY_DRIVER_PRICE));
					info.prodLocnInfo.nCaseFactor = cursor.getInt(cursor.getColumnIndex(KEY_CASE_FACTOR));
					info.prodLocnInfo.nRetailPrice = cursor.getInt(cursor.getColumnIndex(KEY_RETAIL_PRICE));
					info.prodLocnInfo.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
					info.prodLocnInfo.strFullCase = cursor.getString(cursor.getColumnIndex(KEY_FULL_CASE));
					cursor.moveToNext();

					if (info.isInactive()) {
						continue;
					}

					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

	List<PromNovoInfo> promNovoList = getPromNovoList();
		for (int i=0; i<list.size(); i++)
		{
			list.get(i).nPrice = 0;
			list.get(i).nDiscountVal = 0;
			float nBasePrice = Utils.getDividedValue(list.get(i).prodLocnInfo.nBasePrice);
			float nPrice = 0;
			String strPrcFilRecord = "";
			PricingInfo pricingInfo;
			if(Global.gCurCustomerInfo != null)
			{
				pricingInfo = getPricingInfo(Global.gCurCustomerInfo.nCustNo, list.get(i).strItemNo);
			}
			else
			{
				pricingInfo = getPricingInfo(0, list.get(i).strItemNo);
			}

			if (pricingInfo != null)
			{
				nPrice = Utils.getDividedValue(pricingInfo.nPrice);
				strPrcFilRecord = pricingInfo.strPrcFilRecord;
			}
			else
			{
				PriceGrpInfo priceGrpInfo;
				if(Global.gCurCustomerInfo != null)
				{
					priceGrpInfo = getPriceGrpInfo(Global.gCurCustomerInfo.nPriceGrp, list.get(i).strItemNo);
				}
				else
				{
					priceGrpInfo = getPriceGrpInfo(0, list.get(i).strItemNo);
				}

				if (priceGrpInfo != null)
				{
					nPrice = Utils.getDividedValue(priceGrpInfo.nPrice);
					strPrcFilRecord = priceGrpInfo.strPrcFilRecord;
				}
				else
				{
					PriceGrpInfo altPriceGrpInfo;
					if(Global.gCurCustomerInfo != null)
					{
						altPriceGrpInfo = getPriceGrpInfo(Global.gCurCustomerInfo.nAltPriceGrp, list.get(i).strItemNo);
					}
					else
					{
						altPriceGrpInfo = getPriceGrpInfo(0, list.get(i).strItemNo);
					}

					if(altPriceGrpInfo != null)
					{
						nPrice = Utils.getDividedValue(altPriceGrpInfo.nPrice);
						strPrcFilRecord = altPriceGrpInfo.strPrcFilRecord;
					}
				}
			}
			if (strPrcFilRecord.equals("") == true)
			{
				list.get(i).nPrice = nBasePrice;
			}
			else if (strPrcFilRecord.equals("N") == true)
			{
				list.get(i).nPrice = nPrice;
			}
			else if (strPrcFilRecord.equals("O") == true)
			{
				list.get(i).nPrice = nBasePrice - nPrice;
			}
			else if (strPrcFilRecord.equals("P") == true)
			{
				list.get(i).nPrice = nBasePrice / 100 * (100 - nPrice);
			}
			list.get(i).nGrossPrice = Utils.get5RoundedValue(mContext, list.get(i).nPrice);
			list.get(i).nOrgGrossPrice = list.get(i).nPrice;

			float nPromoDiscount = 0;
			int nPromoType = 0;
			for (PromNovoInfo promNovoInfo : promNovoList)
			{
				if (promNovoInfo.strItemNo.equals(list.get(i).strItemNo) == true)
				{
					nPromoType = promNovoInfo.nPromoType;
					nPromoDiscount = Utils.getDividedValue(promNovoInfo.nPromoDiscount);
					break;
				}
			}
			if (nPromoDiscount != 0)
			{
				float nTmpPrice = list.get(i).nPrice;
				if (nPromoType == Global.PROMO_TYPE_CENTS_OFF)
				{
					list.get(i).nPrice = nTmpPrice - nPromoDiscount;
				}
				else if (nPromoType == Global.PROMO_TYPE_PERCENTAGE_OFF)
				{
					list.get(i).nPrice = Utils.get5RoundedValue(mContext, nTmpPrice / 100 * (100 - nPromoDiscount));
				}
				else if (nPromoType == Global.PROMO_TYPE_REPLACE_PRICE)
				{
					list.get(i).nPrice = nPromoDiscount;
				}
				list.get(i).nPromoDiscount = nPromoDiscount;
				list.get(i).nPromoType = nPromoType;
			}
			list.get(i).nOrgPrice = list.get(i).nPrice;
			list.get(i).nPrice = Utils.get5RoundedValue(mContext, list.get(i).nPrice);
			list.get(i).nDiscountVal = nBasePrice - list.get(i).nPrice;
		}

		return list;
	}


	public List<UOrderDInfo> getUOrderDList(int nChainNo, int nCustNo)
	{
		String sql = " SELECT	A.*, B." + KEY_LOCN_NO + ", B." + KEY_BASE_PRICE + ", B." + KEY_CASE_FACTOR + ", B." + KEY_RETAIL_PRICE + ", B." + KEY_FULL_CASE + ", B." + KEY_SCW_TYPE +
				", C." + KEY_PICK_LOCATION_CODE + ", C." + KEY_ORDER_QTY + ", D." + KEY_MODIFIABLE +
				" FROM		" + TABLE_PRODDETL_INFO + " A, " + TABLE_PRODLOCN_INFO + " B, " + TABLE_PRESOLDD_INFO + " C, " + TABLE_PRESOLDH_INFO + " D " +
				" WHERE		A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO + " AND C." + KEY_ITEM_NO + " = A." + KEY_ITEM_NO + " AND C." + KEY_DETAIL_FILE + " = D." + KEY_DETAIL_FILE + " AND " +
				" D." + KEY_CHAIN_NO + " = " + nChainNo + " AND D." + KEY_CUST_NO + " = " + nCustNo + " AND (D." + KEY_TYPE + " = 'P' OR D." + KEY_TYPE + " = 'D' OR D." + KEY_TYPE + " = 'B' OR D." + KEY_TYPE + " = 'C' OR D." + KEY_TYPE + " = '4') AND D." + KEY_VISITED + " = 0";
		Cursor cursor = null;
		List<UOrderDInfo> uOrderDInfoList = new ArrayList<>();
		try{
			cursor = mDb.rawQuery(sql, null);
			if(cursor != null && cursor.getCount() > 0){
				cursor.moveToFirst();
				for(int i = 0; i <cursor.getCount(); i++){
					UOrderDInfo uOrderDInfo = new UOrderDInfo();
					uOrderDInfo.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					uOrderDInfo.strProdGroup = cursor.getString(cursor.getColumnIndex(KEY_PROD_GRP));
					uOrderDInfo.nCustNo = nCustNo;
					uOrderDInfo.strPickLocationCode = cursor.getString(cursor.getColumnIndex(KEY_PICK_LOCATION_CODE));
					uOrderDInfo.nPlanQty = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_QTY));
					uOrderDInfo.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
					uOrderDInfo.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					uOrderDInfo.nBasePrice = Utils.getDividedValue(cursor.getLong(cursor.getColumnIndex(KEY_BASE_PRICE)));
					uOrderDInfo.strFullCase = cursor.getString(cursor.getColumnIndex(KEY_FULL_CASE));
					uOrderDInfo.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
					uOrderDInfo.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));
					String strModifiable = cursor.getString(cursor.getColumnIndex(KEY_MODIFIABLE));
					if( strModifiable.equals("N") )
					{
						uOrderDInfo.nSales = uOrderDInfo.nPlanQty;
					}
					uOrderDInfoList.add(uOrderDInfo);
					cursor.moveToNext();
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null){
				cursor.close();
			}
		}

		List<PromNovoInfo> promNovoList = getPromNovoList();
		for (int i=0; i<uOrderDInfoList.size(); i++)
		{
			uOrderDInfoList.get(i).nPrice = 0;
			uOrderDInfoList.get(i).nDiscountVal = 0;
			String strItemNo = uOrderDInfoList.get(i).strItemNo;
			float nBasePrice = uOrderDInfoList.get(i).nBasePrice;
			float nPrice = 0;
			String strPrcFilRecord = "";
			PricingInfo pricingInfo;
			if(Global.gCurCustomerInfo != null)
			{
				pricingInfo = getPricingInfo(Global.gCurCustomerInfo.nCustNo, strItemNo);
			}
			else
			{
				pricingInfo = getPricingInfo(0, strItemNo);
			}

			if (pricingInfo != null)
			{
				nPrice = Utils.getDividedValue(pricingInfo.nPrice);
				strPrcFilRecord = pricingInfo.strPrcFilRecord;
			}
			else
			{
				PriceGrpInfo priceGrpInfo;
				if(Global.gCurCustomerInfo != null)
				{
					priceGrpInfo = getPriceGrpInfo(Global.gCurCustomerInfo.nPriceGrp, strItemNo);
				}
				else
				{
					priceGrpInfo = getPriceGrpInfo(0, strItemNo);
				}
				if (priceGrpInfo != null)
				{
					nPrice = Utils.getDividedValue(priceGrpInfo.nPrice);
					strPrcFilRecord = priceGrpInfo.strPrcFilRecord;
				}
				else
				{
					PriceGrpInfo altPriceGrpInfo;
					if(Global.gCurCustomerInfo != null)
					{
						altPriceGrpInfo = getPriceGrpInfo(Global.gCurCustomerInfo.nAltPriceGrp, strItemNo);
					}
					else
					{
						altPriceGrpInfo = getPriceGrpInfo(0, strItemNo);
					}

					if(altPriceGrpInfo != null)
					{
						nPrice = Utils.getDividedValue(altPriceGrpInfo.nPrice);
						strPrcFilRecord = altPriceGrpInfo.strPrcFilRecord;
					}
				}
			}
			if (strPrcFilRecord.equals("") == true)
			{
				uOrderDInfoList.get(i).nPrice = nBasePrice;
			}
			else if (strPrcFilRecord.equals("N") == true)
			{
				uOrderDInfoList.get(i).nPrice = nPrice;
			}
			else if (strPrcFilRecord.equals("O") == true)
			{
				uOrderDInfoList.get(i).nPrice = nBasePrice - nPrice;
			}
			else if (strPrcFilRecord.equals("P") == true)
			{
				uOrderDInfoList.get(i).nPrice = nBasePrice / 100 * (100 - nPrice);
			}

			float nPromoDiscount = 0;
			int nPromoType = 0;
			for (PromNovoInfo promNovoInfo : promNovoList)
			{
				if (promNovoInfo.strItemNo.equals(strItemNo) == true)
				{
					nPromoType = promNovoInfo.nPromoType;
					nPromoDiscount = Utils.getDividedValue(promNovoInfo.nPromoDiscount);
					break;
				}
			}
			if (nPromoDiscount != 0)
			{
				float nTmpPrice = uOrderDInfoList.get(i).nPrice;
				if (nPromoType == Global.PROMO_TYPE_CENTS_OFF)
				{
					uOrderDInfoList.get(i).nPrice = nTmpPrice - nPromoDiscount;
				}
				else if (nPromoType == Global.PROMO_TYPE_PERCENTAGE_OFF)
				{
					uOrderDInfoList.get(i).nPrice = Utils.get5RoundedValue(mContext, nTmpPrice / 100 * (100 - nPromoDiscount));
				}
				else if (nPromoType == Global.PROMO_TYPE_REPLACE_PRICE)
				{
					uOrderDInfoList.get(i).nPrice = nPromoDiscount;
				}
				uOrderDInfoList.get(i).nPromoDiscount = nPromoDiscount;
				uOrderDInfoList.get(i).nPromoType = nPromoType;
			}
			uOrderDInfoList.get(i).nPrice = Utils.get5RoundedValue(mContext, uOrderDInfoList.get(i).nPrice);
			uOrderDInfoList.get(i).nDiscountVal = nBasePrice - uOrderDInfoList.get(i).nPrice;
		}

		return uOrderDInfoList;
	}

	public List<UOrderDInfo> getUOrderDListForSuggestedOrder(int nChainNo, int nCustNo)
	{
		String sql = " SELECT	A.*, B." + KEY_LOCN_NO + ", B." + KEY_BASE_PRICE + ", B." + KEY_CASE_FACTOR + ", B." + KEY_RETAIL_PRICE + ", B." + KEY_FULL_CASE + ", B." + KEY_SCW_TYPE +
				", C." + KEY_PICK_LOCATION_CODE + ", C." + KEY_ORDER_QTY + ", C." + KEY_SUGGEST_QTY + ", C." + KEY_PRICE + ", D." + KEY_TYPE + ", C." + KEY_QTY_OPTION + ", D." + KEY_MODIFIABLE +
				" FROM		" + TABLE_PRODDETL_INFO + " A, " + TABLE_PRODLOCN_INFO + " B, " + TABLE_PRESOLDD_INFO + " C, " + TABLE_PRESOLDH_INFO + " D " +
				" WHERE		A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO + " AND C." + KEY_ITEM_NO + " = A." + KEY_ITEM_NO + " AND C." + KEY_DETAIL_FILE + " = D." + KEY_DETAIL_FILE + " AND " +
				" D." + KEY_CHAIN_NO + " = " + nChainNo + " AND D." + KEY_CUST_NO + " = " + nCustNo + " AND (D." + KEY_TYPE + " = 'P' OR D." + KEY_TYPE + " = 'D' OR D." + KEY_TYPE + " = 'B' OR D." + KEY_TYPE + " = 'C' OR D." + KEY_TYPE + " = '4') AND D." + KEY_VISITED + " = 0";
		Cursor cursor = null;
		List<UOrderDInfo> uOrderDInfoList = new ArrayList<>();
		try{
			cursor = mDb.rawQuery(sql, null);
			if(cursor != null && cursor.getCount() > 0){
				cursor.moveToFirst();
				for(int i = 0; i <cursor.getCount(); i++){
					UOrderDInfo uOrderDInfo = new UOrderDInfo();
					uOrderDInfo.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					uOrderDInfo.strProdGroup = cursor.getString(cursor.getColumnIndex(KEY_PROD_GRP));
					uOrderDInfo.nCustNo = nCustNo;
					uOrderDInfo.strPickLocationCode = cursor.getString(cursor.getColumnIndex(KEY_PICK_LOCATION_CODE));
					uOrderDInfo.nPlanQty = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_QTY));
					uOrderDInfo.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
					uOrderDInfo.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					uOrderDInfo.nBasePrice = Utils.getDividedValue(cursor.getLong(cursor.getColumnIndex(KEY_PRICE)));
					if(uOrderDInfo.nBasePrice == 0.0f)
					{
						uOrderDInfo.nBasePrice = Utils.getDividedValue(cursor.getLong(cursor.getColumnIndex(KEY_BASE_PRICE)));
					}
					uOrderDInfo.strFullCase = cursor.getString(cursor.getColumnIndex(KEY_FULL_CASE));
					uOrderDInfo.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
					uOrderDInfo.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));;
					//String strModfiable = cursor.getString(cursor.getColumnIndex(KEY_MODIFIABLE));

					String type = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					int nQtyOption = cursor.getInt(cursor.getColumnIndex(KEY_QTY_OPTION));
					if( type.equals("B"))
					{
						if( nQtyOption != 2 ) continue;
						uOrderDInfo.nSales = cursor.getInt(cursor.getColumnIndex(KEY_SUGGEST_QTY));
					}
					else if( type.equals("C"))
					{
						uOrderDInfo.nSales = 0;
					}
					/*if( strModfiable.equals("N") )
					{
						uOrderDInfo.nSales = uOrderDInfo.nPlanQty;
					}*/
					uOrderDInfo.nTrxnType = 3;
					uOrderDInfoList.add(uOrderDInfo);
					cursor.moveToNext();
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null){
				cursor.close();
			}
		}

		return uOrderDInfoList;

	}

	public boolean isValidate(String strStartDate, String strEndDate, Date date)
	{
		String strCurDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(date);

		Date startDate = null;
		Date endDate = null;
		try
		{
			startDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).parse(strStartDate);
			endDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).parse(strEndDate);
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}

		if( date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0 && strCurDate.compareTo(strStartDate) >= 0 && strCurDate.compareTo(strEndDate) <= 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public List<UOrderDInfo> getCustomerTemplateOrderInfoList(int nAuthGrp, int nCustNo)
	{
		if( nAuthGrp == 0 ) return null;
		String sql = " SELECT	A.*, B." + KEY_LOCN_NO + ", B." + KEY_BASE_PRICE + ", B." + KEY_CASE_FACTOR + ", B." + KEY_RETAIL_PRICE + ", B." + KEY_FULL_CASE + ", B." + KEY_SCW_TYPE +
				", C." + KEY_START_DATE + ", C." + KEY_END_DATE +
				" FROM		" + TABLE_PRODDETL_INFO + " A, " + TABLE_PRODLOCN_INFO + " B, " + TABLE_AUTHDETL_INFO + " C, " + TABLE_AUTHHEADER_INFO + " D " +
				" WHERE		A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO + " AND C." + KEY_ITEM_NO + " = A." + KEY_ITEM_NO + " AND C." + KEY_AUTH_GRP + " = D." + KEY_AUTH_GRP + " AND " +
				" D." + KEY_AUTH_GRP + " = " + nAuthGrp + " AND D." + KEY_AUTH_TYPE + " = 'T'";
		Cursor cursor = null;
		List<UOrderDInfo> uOrderDInfoList = new ArrayList<>();
		Date curDate = new Date(System.currentTimeMillis());
		try{
			cursor = mDb.rawQuery(sql, null);
			if(cursor != null && cursor.getCount() > 0){
				cursor.moveToFirst();
				for(int i = 0; i <cursor.getCount(); i++){
					String startDate = cursor.getString(cursor.getColumnIndex(KEY_START_DATE));
					String endDate = cursor.getString(cursor.getColumnIndex(KEY_END_DATE));
					if( isValidate(startDate, endDate, curDate) )
					{
						UOrderDInfo uOrderDInfo = new UOrderDInfo();
						uOrderDInfo.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
						uOrderDInfo.strProdGroup = cursor.getString(cursor.getColumnIndex(KEY_PROD_GRP));
						uOrderDInfo.nCustNo = nCustNo;
						uOrderDInfo.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
						uOrderDInfo.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
						uOrderDInfo.nBasePrice = Utils.getDividedValue(cursor.getLong(cursor.getColumnIndex(KEY_BASE_PRICE)));
						uOrderDInfo.strFullCase = cursor.getString(cursor.getColumnIndex(KEY_FULL_CASE));
						uOrderDInfo.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
						uOrderDInfo.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));

						uOrderDInfoList.add(uOrderDInfo);
					}

					cursor.moveToNext();
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null){
				cursor.close();
			}
		}

		return uOrderDInfoList;

	}

	public String getProductListFromUPC(String strItemUPC) {
		String sql = " SELECT	A.*, B." + KEY_LOCN_NO + ", B." + KEY_BASE_PRICE + ", B." + KEY_CASE_FACTOR + ", B." + KEY_RETAIL_PRICE + ", B." + KEY_FULL_CASE + ", B." + KEY_SCW_TYPE +
				" FROM		" + TABLE_PRODDETL_INFO + " A, " + TABLE_PRODLOCN_INFO + " B " +
				" WHERE		A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO + " AND (A." + KEY_ITEM_UPC + " = '" + strItemUPC + "' OR A." + KEY_ITEM_UPC20 +" = '" + strItemUPC + "')";
		Cursor cursor = mDb.rawQuery(sql, null);
		String strData = null;
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					strData = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return strData;
	}

	public List<ProdDetlInfo> getProductList() {
		String sql =	" SELECT	A.*, B." + KEY_LOCN_NO + ", B." + KEY_BASE_PRICE + ", B." + KEY_DRIVER_PRICE + ", B." + KEY_CASE_FACTOR + ", B." + KEY_RETAIL_PRICE + ", B." + KEY_FULL_CASE + ", B." + KEY_SCW_TYPE +
						" FROM		" + TABLE_PRODDETL_INFO + " A, " + TABLE_PRODLOCN_INFO + " B " +
						" WHERE		A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<ProdDetlInfo> list = new ArrayList<ProdDetlInfo>();
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++)
				{
					ProdDetlInfo info = new ProdDetlInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
					info.strItemUPC = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC));
					info.strItemUPC20 = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC20));
					info.nWeight = cursor.getFloat(cursor.getColumnIndex(KEY_WEIGHT));
					info.strItemType = cursor.getString(cursor.getColumnIndex(KEY_ITEM_TYPE));
					info.strProdGrp = cursor.getString(cursor.getColumnIndex(KEY_PROD_GRP));
					info.strPromoGrp = cursor.getString(cursor.getColumnIndex(KEY_PROMO_GRP));
					info.strMarketGrp = cursor.getString(cursor.getColumnIndex(KEY_MARKET_GRP));
					info.strProdLine = cursor.getString(cursor.getColumnIndex(KEY_PROD_LINE));
					info.strPackSize = cursor.getString(cursor.getColumnIndex(KEY_PACK_SIZE));
					info.nShelfLife = cursor.getInt(cursor.getColumnIndex(KEY_SHELF_LIFE));
					info.strBrand = cursor.getString(cursor.getColumnIndex(KEY_BRAND));
					info.strSubBrand = cursor.getString(cursor.getColumnIndex(KEY_SUB_BRAND));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strActiveDate = cursor.getString(cursor.getColumnIndex(KEY_ACTIVE_DATE));
					info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
					info.strSalesAllowed = cursor.getString(cursor.getColumnIndex(KEY_SALES_ALLOWED));
					info.strItemInactiveSwitch = cursor.getString(cursor.getColumnIndex(KEY_ITEM_INACTIVE_SWITCH));
					info.strGroupFilterKey = cursor.getString(cursor.getColumnIndex(KEY_GROUP_FILTER_KEY));
					info.strServiceFee = cursor.getString(cursor.getColumnIndex(KEY_SERVICE_FEE));
					info.nMaxQty = cursor.getFloat(cursor.getColumnIndex(KEY_MAX_QTY));
					info.nConsumerUnit = cursor.getInt(cursor.getColumnIndex(KEY_CONSUMER_UNIT));
					info.strImageUrl = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URL));
					info.strLotControl = cursor.getString(cursor.getColumnIndex(KEY_LOT_CONTROL));
					info.prodLocnInfo.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));
					info.prodLocnInfo.nBasePrice = cursor.getLong(cursor.getColumnIndex(KEY_BASE_PRICE));
					info.prodLocnInfo.nDriverPrice = cursor.getLong(cursor.getColumnIndex(KEY_DRIVER_PRICE));
					info.prodLocnInfo.nCaseFactor = cursor.getInt(cursor.getColumnIndex(KEY_CASE_FACTOR));
					info.prodLocnInfo.nRetailPrice = cursor.getInt(cursor.getColumnIndex(KEY_RETAIL_PRICE));
					info.prodLocnInfo.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
					info.prodLocnInfo.strFullCase = cursor.getString(cursor.getColumnIndex(KEY_FULL_CASE));
					cursor.moveToNext();
					list.add(info);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}

		List<PromNovoInfo> promNovoList = getPromNovoList();
		for (int i=0; i<list.size(); i++)
		{
			list.get(i).nPrice = 0;
			list.get(i).nDiscountVal = 0;
			String strItemNo = list.get(i).strItemNo;
			float nBasePrice = Utils.getDividedValue(list.get(i).prodLocnInfo.nBasePrice);
			float nPrice = 0;
			String strPrcFilRecord = "";
			PricingInfo pricingInfo;
			if(Global.gCurCustomerInfo != null)
			{
				pricingInfo = getPricingInfo(Global.gCurCustomerInfo.nCustNo, strItemNo);
			}
			else
			{
				pricingInfo = getPricingInfo(0, strItemNo);
			}

			if (pricingInfo != null)
			{
				nPrice = Utils.getDividedValue(pricingInfo.nPrice);
				strPrcFilRecord = pricingInfo.strPrcFilRecord;
			}
			else
			{
				PriceGrpInfo priceGrpInfo;
				if(Global.gCurCustomerInfo != null)
				{
					priceGrpInfo = getPriceGrpInfo(Global.gCurCustomerInfo.nPriceGrp, strItemNo);
				}
				else
				{
					priceGrpInfo = getPriceGrpInfo(0, strItemNo);
				}

				if (priceGrpInfo != null)
				{
					nPrice = Utils.getDividedValue(priceGrpInfo.nPrice);
					strPrcFilRecord = priceGrpInfo.strPrcFilRecord;
				}
				else
				{
					PriceGrpInfo altPriceGrpInfo;
					if(Global.gCurCustomerInfo != null)
					{
						altPriceGrpInfo = getPriceGrpInfo(Global.gCurCustomerInfo.nAltPriceGrp, strItemNo);
					}
					else
					{
						altPriceGrpInfo = getPriceGrpInfo(0, strItemNo);
					}

					if(altPriceGrpInfo != null)
					{
						nPrice = Utils.getDividedValue(altPriceGrpInfo.nPrice);
						strPrcFilRecord = altPriceGrpInfo.strPrcFilRecord;
					}
				}
			}
			if (strPrcFilRecord.equals("") == true)
			{
				list.get(i).nPrice = nBasePrice;
			}
			else if (strPrcFilRecord.equals("N") == true)
			{
				list.get(i).nPrice = nPrice;
			}
			else if (strPrcFilRecord.equals("O") == true)
			{
				list.get(i).nPrice = nBasePrice - nPrice;
			}
			else if (strPrcFilRecord.equals("P") == true)
			{
				list.get(i).nPrice = nBasePrice / 100 * (100 - nPrice);
			}

			list.get(i).nGrossPrice = Utils.get5RoundedValue(mContext, list.get(i).nPrice);
			list.get(i).nOrgGrossPrice = list.get(i).nPrice;

			float nPromoDiscount = 0;
			int nPromoType = 0;
			for (PromNovoInfo promNovoInfo : promNovoList)
			{
				if (promNovoInfo.strItemNo.equals(strItemNo) == true)
				{
					nPromoType = promNovoInfo.nPromoType;
					nPromoDiscount = Utils.getDividedValue(promNovoInfo.nPromoDiscount);
					break;
				}
			}
			if (nPromoDiscount != 0)
			{
				float nTmpPrice = list.get(i).nPrice;
				if (nPromoType == Global.PROMO_TYPE_CENTS_OFF)
				{
					list.get(i).nPrice = nTmpPrice - nPromoDiscount;
				}
				else if (nPromoType == Global.PROMO_TYPE_PERCENTAGE_OFF)
				{
					list.get(i).nPrice = Utils.get5RoundedValue(mContext, nTmpPrice / 100 * (100 - nPromoDiscount));
				}
				else if (nPromoType == Global.PROMO_TYPE_REPLACE_PRICE)
				{
					list.get(i).nPrice = nPromoDiscount;
				}
				list.get(i).nPromoDiscount = nPromoDiscount;
				list.get(i).nPromoType = nPromoType;
			}
			list.get(i).nOrgPrice = list.get(i).nPrice;
			list.get(i).nPrice = Utils.get5RoundedValue(mContext, list.get(i).nPrice);
			list.get(i).nDiscountVal = nBasePrice - list.get(i).nPrice;
		}
		return list;
	}

	// LoadHeadInfo
	public long insertLoadHeadInfo(LoadHeadInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_DETAIL_FILE, info.strDetailFile);
		values.put(KEY_LOAD_DATE, info.strLoadDate);
		values.put(KEY_BAY, info.strBay);
		values.put(KEY_USER1, info.strUser1);
		values.put(KEY_USER2, info.strUser2);
		values.put(KEY_DAY_NO, info.nDayNo);
		values.put(KEY_DELIVERY_DATE, info.strDeliveryDate);
		values.put(KEY_PERIOD_NO, info.nPeriodNo);
		values.put(KEY_ROUTE_NUMBER, info.strRouteNumber);
		values.put(KEY_DESCRIPTION, info.strDescription);
		values.put(KEY_TYPE, info.strType);
		values.put(KEY_TRIP_NUMBER, info.strTripNumber);
		values.put(KEY_LOAD_TRIP_DATA_FLAG, info.bLoadTripDataFlag ? 1: 0);
		values.put(KEY_LOAD_COMPLETED, info.bLoadCompleted ? 1: 0);
		return mDb.insert(TABLE_LOADHEAD_INFO, null, values);
	}

	public void updateRLoadStatusAsLoadCompleted()
	{
		ContentValues values = new ContentValues();
		values.put(KEY_LOAD_COMPLETED, 1);
		mDb.update(TABLE_LOADHEAD_INFO, values, KEY_LOAD_TRIP_DATA_FLAG + " = 1 AND " + KEY_LOAD_COMPLETED + " = 0 AND " + KEY_TYPE + " = 'R'", null);
	}

	public void updateLLoadStatusAsLoadCompleted()
	{
		ContentValues values = new ContentValues();
		values.put(KEY_LOAD_COMPLETED, 1);
		mDb.update(TABLE_LOADHEAD_INFO, values, KEY_LOAD_TRIP_DATA_FLAG + " = 1 AND " + KEY_LOAD_COMPLETED + " = 0 AND (" + KEY_TYPE + " = 'L' OR " + KEY_TYPE + " = 'T')", null);
	}

	public void updateSignedLoadHeader()
	{
		ContentValues values = new ContentValues();
		values.put(KEY_LOAD_COMPLETED, 0);
		values.put(KEY_LOAD_TRIP_DATA_FLAG, 0);

		mDb.update(TABLE_LOADHEAD_INFO, values, "(" + KEY_TYPE + " = 'L' OR " + KEY_TYPE + " = 'T') AND " + KEY_LOAD_TRIP_DATA_FLAG + " = 1 AND " + KEY_LOAD_COMPLETED + " = 0",  null);
	}

	public int updateLoadHeadInfo(LoadHeadInfo info){
		ContentValues values = new ContentValues();
		values.put(KEY_DETAIL_FILE, info.strDetailFile);
		values.put(KEY_LOAD_DATE, info.strLoadDate);
		values.put(KEY_DAY_NO, info.nDayNo);
		values.put(KEY_BAY, info.strBay);
		values.put(KEY_USER1, info.strUser1);
		values.put(KEY_USER2, info.strUser2);
		values.put(KEY_DELIVERY_DATE, info.strDeliveryDate);
		values.put(KEY_PERIOD_NO, info.nPeriodNo);
		values.put(KEY_ROUTE_NUMBER, info.strRouteNumber);
		values.put(KEY_DESCRIPTION, info.strDescription);
		values.put(KEY_TYPE, info.strType);
		values.put(KEY_TRIP_NUMBER, info.strTripNumber);
		values.put(KEY_LOAD_TRIP_DATA_FLAG, info.bLoadTripDataFlag ? 1: 0);
		values.put(KEY_LOAD_COMPLETED, info.bLoadCompleted ? 1: 0);
		return mDb.update(TABLE_LOADHEAD_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}

	public boolean deleteAllLoadHeadInfo() throws SQLException {
		return mDb.delete(TABLE_LOADHEAD_INFO, null, null) > 0;
	}

	public boolean deleteAllUnCompletedLoadHeadInfo() throws SQLException {
		return mDb.delete(TABLE_LOADHEAD_INFO, KEY_LOAD_COMPLETED + " = 0", null) > 0;
	}


	public List<LoadHeadInfo> getSelectedLoadHeadInfo()
	{
		String sql = "SELECT * FROM " + TABLE_LOADHEAD_INFO + " WHERE " + KEY_LOAD_TRIP_DATA_FLAG + " = 1 AND " +
				KEY_LOAD_COMPLETED + " = 0" ;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<LoadHeadInfo> list = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					LoadHeadInfo info = new LoadHeadInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.strLoadDate = cursor.getString(cursor.getColumnIndex(KEY_LOAD_DATE));
					info.strBay = cursor.getString(cursor.getColumnIndex(KEY_BAY));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
					info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER2));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strRouteNumber = cursor.getString(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.bLoadTripDataFlag = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_TRIP_DATA_FLAG)) == 1;
					info.bLoadCompleted = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_COMPLETED)) == 1;
					cursor.moveToNext();
					list.add(info);
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally {
			if( cursor != null ){
				cursor.close();
			}
		}
		return list;
	}

	public List<LoadHeadInfo> getSelectedLoadHeadInfo(String strType)
	{
		String sql = "SELECT * FROM " + TABLE_LOADHEAD_INFO + " WHERE " + KEY_LOAD_TRIP_DATA_FLAG + " = 1 AND " +
				KEY_TYPE + " ='" + strType + "' AND " + KEY_LOAD_COMPLETED + " = 0" ;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<LoadHeadInfo> list = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					LoadHeadInfo info = new LoadHeadInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.strLoadDate = cursor.getString(cursor.getColumnIndex(KEY_LOAD_DATE));
					info.strBay = cursor.getString(cursor.getColumnIndex(KEY_BAY));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
					info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER2));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strRouteNumber = cursor.getString(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.bLoadTripDataFlag = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_TRIP_DATA_FLAG)) == 1;
					info.bLoadCompleted = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_COMPLETED)) == 1;
					cursor.moveToNext();
					list.add(info);
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally {
			if( cursor != null ){
				cursor.close();
			}
		}
		return list;
	}

	public List<LoadHeadInfo> getLoadProcessedHeadInfo()
		{
			String sql = "SELECT * FROM " + TABLE_LOADHEAD_INFO + " WHERE " + KEY_LOAD_TRIP_DATA_FLAG + " = 1 AND " + KEY_LOAD_COMPLETED + " = 0" ;
			Cursor cursor = mDb.rawQuery(sql, null);
			List<LoadHeadInfo> list = new ArrayList<>();
			try
			{
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						LoadHeadInfo info = new LoadHeadInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
						info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
						info.strLoadDate = cursor.getString(cursor.getColumnIndex(KEY_LOAD_DATE));
						info.strBay = cursor.getString(cursor.getColumnIndex(KEY_BAY));
						info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
						info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER2));
						info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
						info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
						info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
						info.strRouteNumber = cursor.getString(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
						info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
						info.bLoadTripDataFlag = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_TRIP_DATA_FLAG)) == 1;
						info.bLoadCompleted = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_COMPLETED)) == 1;
						cursor.moveToNext();
						list.add(info);
					}
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}finally {
				if( cursor != null ){
					cursor.close();
				}
			}
			return list;
	}
	public List<LoadHeadInfo> getLoadUnProcessedHeadInfo()
		{
			String sql = "SELECT * FROM " + TABLE_LOADHEAD_INFO + " WHERE " + KEY_LOAD_TRIP_DATA_FLAG + " = 0" ;
			Cursor cursor = mDb.rawQuery(sql, null);
			List<LoadHeadInfo> list = new ArrayList<>();
			try
			{
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						LoadHeadInfo info = new LoadHeadInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
						info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
						info.strLoadDate = cursor.getString(cursor.getColumnIndex(KEY_LOAD_DATE));
						info.strBay = cursor.getString(cursor.getColumnIndex(KEY_BAY));
						info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
						info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER2));
						info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
						info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
						info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
						info.strRouteNumber = cursor.getString(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
						info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
						info.bLoadTripDataFlag = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_TRIP_DATA_FLAG)) == 1;
						info.bLoadCompleted = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_COMPLETED)) == 1;
						cursor.moveToNext();
						list.add(info);
					}
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}finally {
				if( cursor != null ){
					cursor.close();
				}
			}
			return list;
	}

    public List<LoadHeadInfo> getLLoadUnProcessedHeadInfo()
    {
        String sql = "SELECT * FROM " + TABLE_LOADHEAD_INFO + " WHERE " + KEY_LOAD_TRIP_DATA_FLAG + " = 0 AND (" + KEY_TYPE + " = 'L' OR " + KEY_TYPE + " = 'T')" ;
        Cursor cursor = mDb.rawQuery(sql, null);
        List<LoadHeadInfo> list = new ArrayList<>();
        try
        {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    LoadHeadInfo info = new LoadHeadInfo();
                    info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
                    info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
                    info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
                    info.strLoadDate = cursor.getString(cursor.getColumnIndex(KEY_LOAD_DATE));
                    info.strBay = cursor.getString(cursor.getColumnIndex(KEY_BAY));
                    info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
                    info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER2));
                    info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
                    info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
                    info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
                    info.strRouteNumber = cursor.getString(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
                    info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
                    info.bLoadTripDataFlag = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_TRIP_DATA_FLAG)) == 1;
                    info.bLoadCompleted = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_COMPLETED)) == 1;
                    cursor.moveToNext();
                    list.add(info);
                }
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }finally {
            if( cursor != null ){
                cursor.close();
            }
        }
        return list;
    }

	public List<LoadHeadInfo> getLLoadHeadInfo(boolean bLoaded)
		{
			String sql = "";
			if(bLoaded)
			{
				sql = "SELECT * FROM " + TABLE_LOADHEAD_INFO + " WHERE " + KEY_LOAD_TRIP_DATA_FLAG + " = 1 AND " + KEY_LOAD_COMPLETED + " = 0 AND (" + KEY_TYPE + " = 'L' OR " + KEY_TYPE + " = 'T')" ;
			}
			else
			{
				sql = "SELECT * FROM " + TABLE_LOADHEAD_INFO + " WHERE " + KEY_LOAD_COMPLETED + " = 0 AND (" + KEY_TYPE + " = 'L' OR " + KEY_TYPE + " = 'T')" ;
			}

			Cursor cursor = mDb.rawQuery(sql, null);
			List<LoadHeadInfo> list = new ArrayList<>();
			try
			{
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						LoadHeadInfo info = new LoadHeadInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
						info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
						info.strLoadDate = cursor.getString(cursor.getColumnIndex(KEY_LOAD_DATE));
						info.strBay = cursor.getString(cursor.getColumnIndex(KEY_BAY));
						info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
						info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER2));
						info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
						info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
						info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
						info.strRouteNumber = cursor.getString(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
						info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
						info.bLoadTripDataFlag = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_TRIP_DATA_FLAG)) == 1;
						info.bLoadCompleted = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_COMPLETED)) == 1;
						cursor.moveToNext();
						list.add(info);
					}
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}finally {
				if( cursor != null ){
					cursor.close();
				}
			}
			return list;
	}

	public List<LoadHeadInfo> getLoadedHeadInfo()
		{
			String sql = "SELECT * FROM " + TABLE_LOADHEAD_INFO + " WHERE " + KEY_LOAD_TRIP_DATA_FLAG + " = 1" ;
			Cursor cursor = mDb.rawQuery(sql, null);
			List<LoadHeadInfo> list = new ArrayList<>();
			try
			{
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						LoadHeadInfo info = new LoadHeadInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
						info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
						info.strLoadDate = cursor.getString(cursor.getColumnIndex(KEY_LOAD_DATE));
						info.strBay = cursor.getString(cursor.getColumnIndex(KEY_BAY));
						info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
						info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER2));
						info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
						info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
						info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
						info.strRouteNumber = cursor.getString(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
						info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
						info.bLoadTripDataFlag = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_TRIP_DATA_FLAG)) == 1;
						info.bLoadCompleted = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_COMPLETED)) == 1;
						cursor.moveToNext();
						list.add(info);
					}
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}finally {
				if( cursor != null ){
					cursor.close();
				}
			}
			return list;
	}

	public List<LoadHeadInfo> getLoadHeadInfo(String loadDate)
	{
		String sql = "SELECT * FROM " + TABLE_LOADHEAD_INFO + " WHERE " + KEY_DELIVERY_DATE + " = '" + loadDate + "' AND " + KEY_LOAD_COMPLETED + " = 0 AND " + KEY_LOAD_TRIP_DATA_FLAG + " = 0";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<LoadHeadInfo> list = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					LoadHeadInfo info = new LoadHeadInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.strLoadDate = cursor.getString(cursor.getColumnIndex(KEY_LOAD_DATE));
					info.strBay = cursor.getString(cursor.getColumnIndex(KEY_BAY));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
					info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER2));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.strRouteNumber = cursor.getString(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strTripNumber = cursor.getString(cursor.getColumnIndex(KEY_TRIP_NUMBER));
					info.bLoadTripDataFlag = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_TRIP_DATA_FLAG)) == 1;
					info.bLoadCompleted = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_COMPLETED)) == 1;
					cursor.moveToNext();
					list.add(info);
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally {
			if( cursor != null ){
				cursor.close();
			}
		}
		return list;
	}

	public List<LoadHeadInfo> getRLoadHeadInfo(String loadDate)
	{
		String sql = "SELECT * FROM " + TABLE_LOADHEAD_INFO + " WHERE " + KEY_DELIVERY_DATE + " = '" + loadDate + "' AND " +
				KEY_LOAD_COMPLETED + " = 0 AND " + KEY_LOAD_TRIP_DATA_FLAG + " = 0 AND " + KEY_TYPE + " = 'R'";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<LoadHeadInfo> list = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					LoadHeadInfo info = new LoadHeadInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.strLoadDate = cursor.getString(cursor.getColumnIndex(KEY_LOAD_DATE));
					info.strBay = cursor.getString(cursor.getColumnIndex(KEY_BAY));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
					info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER2));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.strRouteNumber = cursor.getString(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strTripNumber = cursor.getString(cursor.getColumnIndex(KEY_TRIP_NUMBER));
					info.bLoadTripDataFlag = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_TRIP_DATA_FLAG)) == 1;
					info.bLoadCompleted = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_COMPLETED)) == 1;
					cursor.moveToNext();
					list.add(info);
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally {
			if( cursor != null ){
				cursor.close();
			}
		}
		return list;
	}

	public List<LoadHeadInfo> getRLoadHeadInfo()
	{
		String sql = "SELECT * FROM " + TABLE_LOADHEAD_INFO + " WHERE " + KEY_LOAD_COMPLETED + " = 0 AND " + KEY_LOAD_TRIP_DATA_FLAG + " = 0 AND " + KEY_TYPE + " = 'R'";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<LoadHeadInfo> list = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					LoadHeadInfo info = new LoadHeadInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.strLoadDate = cursor.getString(cursor.getColumnIndex(KEY_LOAD_DATE));
					info.strBay = cursor.getString(cursor.getColumnIndex(KEY_BAY));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
					info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER2));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.strRouteNumber = cursor.getString(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strTripNumber = cursor.getString(cursor.getColumnIndex(KEY_TRIP_NUMBER));
					info.bLoadTripDataFlag = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_TRIP_DATA_FLAG)) == 1;
					info.bLoadCompleted = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_COMPLETED)) == 1;
					cursor.moveToNext();
					list.add(info);
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally {
			if( cursor != null ){
				cursor.close();
			}
		}
		return list;
	}

	public boolean isExistLoadHeadInfo(String strDetailFile, String strLoadDate, String strDeliveryDate, int nPeriodNo,
									   String strRouteNumber, String strDescription, String strType)
	{
		String sql = "SELECT * FROM " + TABLE_LOADHEAD_INFO + " WHERE "
				+ KEY_DETAIL_FILE + " = '" + strDetailFile + "' AND " + KEY_LOAD_DATE + " = '" + strLoadDate + "' AND " +
				KEY_DELIVERY_DATE + " = '" + strDeliveryDate + "' AND " + KEY_PERIOD_NO + " = " + nPeriodNo + " AND " +
				KEY_ROUTE_NUMBER + " = '" + strRouteNumber + "' AND " + KEY_DESCRIPTION + " = '" + strDescription + "' AND " +
				KEY_TYPE + " = '" + strType + "'";

		Cursor cursor = mDb.rawQuery(sql, null);
		return ( cursor != null && cursor.getCount() > 0);
	}

	public LoadHeadInfo getLoadHeadInfoWithDetailFile(String strDetailFile){
		String sql = "SELECT * FROM " + TABLE_LOADHEAD_INFO + " WHERE " + KEY_DETAIL_FILE + " = '" + strDetailFile + "'";
		Cursor cursor = mDb.rawQuery(sql, null);
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				LoadHeadInfo info = new LoadHeadInfo();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
				info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
				info.strLoadDate = cursor.getString(cursor.getColumnIndex(KEY_LOAD_DATE));
				info.strBay = cursor.getString(cursor.getColumnIndex(KEY_BAY));
				info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
				info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER2));
				info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
				info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
				info.strRouteNumber = cursor.getString(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
				info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
				info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
				info.strTripNumber = cursor.getString(cursor.getColumnIndex(KEY_TRIP_NUMBER));
				info.bLoadTripDataFlag = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_TRIP_DATA_FLAG)) == 1;
				info.bLoadCompleted = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_COMPLETED)) == 1;

				return info;
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally {
			if( cursor != null ){
				cursor.close();
			}
		}
		return null;
	}

	public List<LoadHeadInfo> getLoadHeadInfo(){
		String sql = "SELECT * FROM " + TABLE_LOADHEAD_INFO + " WHERE " + KEY_LOAD_COMPLETED + " = 0 ";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<LoadHeadInfo> list = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					LoadHeadInfo info = new LoadHeadInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.strLoadDate = cursor.getString(cursor.getColumnIndex(KEY_LOAD_DATE));
					info.strBay = cursor.getString(cursor.getColumnIndex(KEY_BAY));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
					info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER2));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.strRouteNumber = cursor.getString(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strTripNumber = cursor.getString(cursor.getColumnIndex(KEY_TRIP_NUMBER));
					info.bLoadTripDataFlag = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_TRIP_DATA_FLAG)) == 1;
					info.bLoadCompleted = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_COMPLETED)) == 1;
					cursor.moveToNext();
					list.add(info);
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally {
			if( cursor != null ){
				cursor.close();
			}
		}
		return list;
	}

	public List<LoadDetlInfo> getRLoadDetl(String strDetailFile)
	{
		String sql =	"SELECT * FROM " + TABLE_LOADDETL_INFO + " WHERE " + KEY_DETAIL_FILE + "='" + strDetailFile + "'";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<LoadDetlInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					LoadDetlInfo info = new LoadDetlInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.nQty = cursor.getInt(cursor.getColumnIndex(KEY_QTY));
					info.dQty = cursor.getDouble(cursor.getColumnIndex(KEY_DQTY));
					info.strPickLocationCode = cursor.getString(cursor.getColumnIndex(KEY_PICK_LOCATION_CODE));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	// LoadDetlInfo
	public List<LoadDetlInfo> getLoadDetlContainerList() {
		String sql =	" SELECT	A.* " +
						" FROM		" + TABLE_LOADDETL_INFO + " A, " + TABLE_LOADHEAD_INFO + " B " +
						" WHERE		B." + KEY_TYPE + " = 'C' AND A." + KEY_DETAIL_FILE + " = B." + KEY_DETAIL_FILE + " AND B." + KEY_LOAD_COMPLETED + " = 0";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<LoadDetlInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					LoadDetlInfo info = new LoadDetlInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.nQty = cursor.getInt(cursor.getColumnIndex(KEY_QTY));
					info.dQty = cursor.getDouble(cursor.getColumnIndex(KEY_DQTY));
					info.strPickLocationCode = cursor.getString(cursor.getColumnIndex(KEY_PICK_LOCATION_CODE));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public LoadDetlInfo getLoadDetl(String strItemNo)
	{
		String sql =	" SELECT	A.* " +
				" FROM		" + TABLE_LOADDETL_INFO + " A, " + TABLE_LOADHEAD_INFO + " B " +
				" WHERE		A." + KEY_DETAIL_FILE + " = B." + KEY_DETAIL_FILE + " AND B." + KEY_LOAD_TRIP_DATA_FLAG + " = 1 AND " +
				" B." + KEY_TYPE + " = 'R' AND A." + KEY_ITEM_NO + " = '" + strItemNo + "' AND B." + KEY_LOAD_COMPLETED + " = 0";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<LoadDetlInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					LoadDetlInfo info = new LoadDetlInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.nQty = cursor.getInt(cursor.getColumnIndex(KEY_QTY));
					info.dQty = cursor.getDouble(cursor.getColumnIndex(KEY_DQTY));
					info.strPickLocationCode = cursor.getString(cursor.getColumnIndex(KEY_PICK_LOCATION_CODE));
					cursor.moveToNext();
					boolean bAdd = true;
					for(LoadDetlInfo oldInfo : list)
					{
						if( oldInfo.strItemNo.equals(info.strItemNo) &&
								info.strPickLocationCode.equals(oldInfo.strPickLocationCode))
						{
							oldInfo.nQty += info.nQty;
							bAdd = false;
							break;
						}
					}
					if( bAdd )
					{
						list.add(info);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		if( list.size() == 0 )
		{
			return null;
		}
		else
		{
			return list.get(0);
		}
	}


	public List<LoadDetlInfo> getLoadDetlList() {
		String sql =	" SELECT	A.* " +
				" FROM		" + TABLE_LOADDETL_INFO + " A, " + TABLE_LOADHEAD_INFO + " B " +
				" WHERE		A." + KEY_DETAIL_FILE + " = B." + KEY_DETAIL_FILE + " AND B." + KEY_LOAD_TRIP_DATA_FLAG + " = 1 AND" +
				" (B." + KEY_TYPE + " = 'L' OR B." + KEY_TYPE + " = 'T') AND B." + KEY_LOAD_COMPLETED + " = 0";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<LoadDetlInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					LoadDetlInfo info = new LoadDetlInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.nQty = cursor.getInt(cursor.getColumnIndex(KEY_QTY));
					info.dQty = cursor.getDouble(cursor.getColumnIndex(KEY_DQTY));
					info.strPickLocationCode = cursor.getString(cursor.getColumnIndex(KEY_PICK_LOCATION_CODE));
					cursor.moveToNext();
					boolean bAdd = true;
					for(LoadDetlInfo oldInfo : list)
					{
						if( oldInfo.strItemNo.equals(info.strItemNo) &&
								info.strPickLocationCode.equals(oldInfo.strPickLocationCode))
						{
							oldInfo.nQty += info.nQty;
							bAdd = false;
							break;
						}
					}
					if( bAdd )
					{
						list.add(info);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<LoadDetlInfo> getRLoadDetlList() {
		List<LoadDetlInfo> list = new ArrayList<>();
		{
			String sql = " SELECT	A.* " +
					" FROM		" + TABLE_LOADDETL_INFO + " A, " + TABLE_LOADHEAD_INFO + " B " +
					" WHERE		A." + KEY_DETAIL_FILE + " = B." + KEY_DETAIL_FILE + " AND B." + KEY_LOAD_TRIP_DATA_FLAG + " = 1 AND" +
					" B." + KEY_TYPE + " = 'R' AND B." + KEY_LOAD_COMPLETED + " = 0";
			Cursor cursor = mDb.rawQuery(sql, null);
			try {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						LoadDetlInfo info = new LoadDetlInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
						info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
						info.nQty = cursor.getInt(cursor.getColumnIndex(KEY_QTY));
						info.dQty = cursor.getDouble(cursor.getColumnIndex(KEY_DQTY));
						info.strPickLocationCode = cursor.getString(cursor.getColumnIndex(KEY_PICK_LOCATION_CODE));
						info.nBaseQty = info.nQty;
						info.dBaseQty = info.dQty;
						cursor.moveToNext();
						boolean bAdd = true;
						for (LoadDetlInfo oldInfo : list) {
							if (oldInfo.strItemNo.equals(info.strItemNo) &&
									info.strPickLocationCode.equals(oldInfo.strPickLocationCode)) {
								oldInfo.nQty += info.nQty;
								oldInfo.dQty += info.dQty;
								oldInfo.nBaseQty += info.nBaseQty;
								oldInfo.dBaseQty += info.dBaseQty;
								bAdd = false;
								break;
							}
						}
						if (bAdd) {
							list.add(info);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		return list;
	}

	public LoadDetlInfo getRLoadDetlList(String strItemNo) {
		List<LoadDetlInfo> list = new ArrayList<>();
		{
			String sql = " SELECT	A.* " +
					" FROM		" + TABLE_LOADDETL_INFO + " A, " + TABLE_LOADHEAD_INFO + " B " +
					" WHERE		A." + KEY_DETAIL_FILE + " = B." + KEY_DETAIL_FILE + " AND B." + KEY_LOAD_TRIP_DATA_FLAG + " = 1 AND" +
					" B." + KEY_TYPE + " = 'R' AND " + " A." + KEY_ITEM_NO + " = '" + strItemNo + "' AND B." + KEY_LOAD_COMPLETED + " = 1";
			Cursor cursor = mDb.rawQuery(sql, null);
			try {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						LoadDetlInfo info = new LoadDetlInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
						info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
						info.nQty = cursor.getInt(cursor.getColumnIndex(KEY_QTY));
						info.dQty = cursor.getDouble(cursor.getColumnIndex(KEY_DQTY));
						info.strPickLocationCode = cursor.getString(cursor.getColumnIndex(KEY_PICK_LOCATION_CODE));
						info.nBaseQty = info.nQty;
						cursor.moveToNext();
						boolean bAdd = true;
						for (LoadDetlInfo oldInfo : list) {
							if (oldInfo.strItemNo.equals(info.strItemNo) &&
									info.strPickLocationCode.equals(oldInfo.strPickLocationCode)) {
								oldInfo.nQty += info.nQty;
								oldInfo.nBaseQty += info.nBaseQty;
								bAdd = false;
								break;
							}
						}
						if (bAdd) {
							list.add(info);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		if( list.size() > 0 ) return list.get(0);
		else return null;
	}
	public long insertLoadDetlInfo(LoadDetlInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_DETAIL_FILE, info.strDetailFile);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_QTY, info.nQty);
		values.put(KEY_DQTY, info.dQty);
		values.put(KEY_PICK_LOCATION_CODE, info.strPickLocationCode);
		return mDb.insert(TABLE_LOADDETL_INFO, null, values);
	}

	public boolean deleteAllLoadDetlInfo() throws SQLException {
		return mDb.delete(TABLE_LOADDETL_INFO, null, null) > 0;
	}

	// MessageInfo
	public List<MessageInfo> getMessageList(int nCustNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_MESSAGE_INFO +
						" WHERE		" + KEY_CUST_NO + " = " + nCustNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<MessageInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					MessageInfo info = new MessageInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nMessageID = cursor.getInt(cursor.getColumnIndex(KEY_MESSAGE_ID));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strSubject = cursor.getString(cursor.getColumnIndex(KEY_SUBJECT));
					info.strMessageText = cursor.getString(cursor.getColumnIndex(KEY_MESSAGE_TEXT));
					info.bIsOldMessage = cursor.getInt(cursor.getColumnIndex(KEY_IS_OLD_MESSAGE)) == 1 ? true : false;
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public long insertMessageInfo(MessageInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_MESSAGE_ID, info.nMessageID);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_SUBJECT, info.strSubject);
		values.put(KEY_MESSAGE_TEXT, info.strMessageText);
		values.put(KEY_IS_OLD_MESSAGE, info.bIsOldMessage == true ? 1 : 0);
		return mDb.insert(TABLE_MESSAGE_INFO, null, values);
	}

	public long updateMessageInfo(MessageInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_SUBJECT, info.strSubject);
		values.put(KEY_MESSAGE_TEXT, info.strMessageText);
		values.put(KEY_IS_OLD_MESSAGE, info.bIsOldMessage == true ? 1 : 0);
		return mDb.update(TABLE_MESSAGE_INFO, values, KEY_MESSAGE_ID + " = " + info.nMessageID, null);
	}

	public boolean deleteMessageInfo(int nIdx) throws SQLException {
		return mDb.delete(TABLE_MESSAGE_INFO, KEY_IDX + " = " + nIdx, null) > 0;
	}

	public boolean deleteAllMessageInfo() throws SQLException {
		return mDb.delete(TABLE_MESSAGE_INFO, null, null) > 0;
	}

	// ProdStruInfo
	public List<ProdStruInfo> getProdStruList() {
		String sql =	" SELECT	*, (SELECT	IFNULL(" + KEY_SHORT_DESC + ", \"\") FROM	" + TABLE_PRODDETL_INFO + " WHERE	" + KEY_ITEM_NO + " = " + KEY_REFERENCE + " ) AS " + KEY_SHORT_DESC +
				 ", (SELECT	IFNULL(" + KEY_DESCRIPTION + ", \"\") FROM	" + TABLE_PRODDETL_INFO + " WHERE	" + KEY_ITEM_NO + " = " + KEY_REFERENCE + " ) AS " + KEY_FULL_DESCRIPTION +
				", (SELECT	IFNULL(" + KEY_ACTIVE_DATE + ", \"\") FROM	" + TABLE_PRODDETL_INFO + " WHERE	" + KEY_ITEM_NO + " = " + KEY_REFERENCE + " ) AS " + KEY_ACTIVE_DATE +
				", (SELECT	IFNULL(" + KEY_ITEM_INACTIVE_SWITCH + ", \"\") FROM	" + TABLE_PRODDETL_INFO + " WHERE	" + KEY_ITEM_NO + " = " + KEY_REFERENCE + " ) AS " + KEY_ITEM_INACTIVE_SWITCH +
						" FROM		" + TABLE_PRODSTRU_INFO;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<ProdStruInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					ProdStruInfo info = new ProdStruInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strEntryID = cursor.getString(cursor.getColumnIndex(KEY_ENTRY_ID));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strFullDescription = cursor.getString(cursor.getColumnIndex(KEY_FULL_DESCRIPTION));
					info.strParentID = cursor.getString(cursor.getColumnIndex(KEY_PARENT_ID));
					info.strObjectType = cursor.getString(cursor.getColumnIndex(KEY_OBJECT_TYPE));
					info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
					info.nGroupNo = cursor.getInt(cursor.getColumnIndex(KEY_GROUP_NO));
					info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));

					cursor.moveToNext();

					ProdDetlInfo detlInfo = new ProdDetlInfo();
					if (detlInfo.isInactive()) {
						continue;
					}

					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public long insertProdStruInfo(ProdStruInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_ENTRY_ID, info.strEntryID);
		values.put(KEY_DESCRIPTION, info.strDescription);
		values.put(KEY_PARENT_ID, info.strParentID);
		values.put(KEY_OBJECT_TYPE, info.strObjectType);
		values.put(KEY_REFERENCE, info.strReference);
		values.put(KEY_GROUP_NO, info.nGroupNo);
		return mDb.insert(TABLE_PRODSTRU_INFO, null, values);
	}

	public boolean deleteAllProdStruInfo() throws SQLException {
		return mDb.delete(TABLE_PRODSTRU_INFO, null, null) > 0;
	}

	// DescTypeInfo
	public List<DescTypeInfo> getDescTypeList(String strDescriptionTypeID) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_DESCTYPE_INFO;
		if (strDescriptionTypeID.length() > 0) {
			sql += " WHERE	" + KEY_DESCRIPTION_TYPE_ID + " = '" + strDescriptionTypeID + "' ";
		}
 		Cursor cursor = mDb.rawQuery(sql, null);
		List<DescTypeInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					DescTypeInfo info = new DescTypeInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDescriptionEntryKey = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION_ENTRY_KEY));
					info.nSystemDescriptionTypeYN = cursor.getInt(cursor.getColumnIndex(KEY_DESCRIPTION_TYPE_YN));
					info.strDescriptionTypeID = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION_TYPE_ID));
					info.nNumericCodeYN = cursor.getInt(cursor.getColumnIndex(KEY_NUMERIC_CODE_YN));
					info.nNumericKey = cursor.getInt(cursor.getColumnIndex(KEY_NUMERIC_KEY));
					info.strAlphaKey = cursor.getString(cursor.getColumnIndex(KEY_ALPHA_KEY));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.nAlternateSeq = cursor.getInt(cursor.getColumnIndex(KEY_ALTERNATE_SEQ));
					info.strOwnership = cursor.getString(cursor.getColumnIndex(KEY_OWNERSHIP));
					info.strValue1 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_1));
					info.strValue2 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_2));
					info.strValue3 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_3));
					info.strValue4 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_4));
					info.strValue5 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_5));
					info.strValue6 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_6));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<DescTypeInfo> getDescTypeListWithAlphaKey(String strDescriptionTypeID, String strAlphaKey) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_DESCTYPE_INFO;
		sql += " WHERE	" + KEY_DESCRIPTION_TYPE_ID + " = '" + strDescriptionTypeID + "' AND " + KEY_ALPHA_KEY + " = '" + strAlphaKey + "'";

 		Cursor cursor = mDb.rawQuery(sql, null);
		List<DescTypeInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					DescTypeInfo info = new DescTypeInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDescriptionEntryKey = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION_ENTRY_KEY));
					info.nSystemDescriptionTypeYN = cursor.getInt(cursor.getColumnIndex(KEY_DESCRIPTION_TYPE_YN));
					info.strDescriptionTypeID = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION_TYPE_ID));
					info.nNumericCodeYN = cursor.getInt(cursor.getColumnIndex(KEY_NUMERIC_CODE_YN));
					info.nNumericKey = cursor.getInt(cursor.getColumnIndex(KEY_NUMERIC_KEY));
					info.strAlphaKey = cursor.getString(cursor.getColumnIndex(KEY_ALPHA_KEY));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.nAlternateSeq = cursor.getInt(cursor.getColumnIndex(KEY_ALTERNATE_SEQ));
					info.strOwnership = cursor.getString(cursor.getColumnIndex(KEY_OWNERSHIP));
					info.strValue1 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_1));
					info.strValue2 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_2));
					info.strValue3 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_3));
					info.strValue4 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_4));
					info.strValue5 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_5));
					info.strValue6 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_6));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<DescTypeInfo> getDescTypeListWithNumericKey(String strDescriptionTypeID, int nNumericKey) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_DESCTYPE_INFO;
		sql += " WHERE	" + KEY_DESCRIPTION_TYPE_ID + " = '" + strDescriptionTypeID + "' AND " + KEY_NUMERIC_KEY + " = " + nNumericKey;

 		Cursor cursor = mDb.rawQuery(sql, null);
		List<DescTypeInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					DescTypeInfo info = new DescTypeInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDescriptionEntryKey = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION_ENTRY_KEY));
					info.nSystemDescriptionTypeYN = cursor.getInt(cursor.getColumnIndex(KEY_DESCRIPTION_TYPE_YN));
					info.strDescriptionTypeID = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION_TYPE_ID));
					info.nNumericCodeYN = cursor.getInt(cursor.getColumnIndex(KEY_NUMERIC_CODE_YN));
					info.nNumericKey = cursor.getInt(cursor.getColumnIndex(KEY_NUMERIC_KEY));
					info.strAlphaKey = cursor.getString(cursor.getColumnIndex(KEY_ALPHA_KEY));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.nAlternateSeq = cursor.getInt(cursor.getColumnIndex(KEY_ALTERNATE_SEQ));
					info.strOwnership = cursor.getString(cursor.getColumnIndex(KEY_OWNERSHIP));
					info.strValue1 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_1));
					info.strValue2 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_2));
					info.strValue3 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_3));
					info.strValue4 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_4));
					info.strValue5 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_5));
					info.strValue6 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_6));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	// DescTypeInfo
	public int getDescTypeListCount(String strDescriptionTypeID)
	{
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_DESCTYPE_INFO;
		if (strDescriptionTypeID.length() > 0) {
			sql += " WHERE	" + KEY_DESCRIPTION_TYPE_ID + " = '" + strDescriptionTypeID + "' ";
		}
		Cursor cursor = mDb.rawQuery(sql, null);
		int nResult = (cursor != null && cursor.getCount() > 0) ? cursor.getCount() : 0;
		try {
			cursor.close();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		return nResult;
	}

	public DescTypeInfo getDescTypeInfo(String strDescriptionTypeID, String strAlphaKey) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_DESCTYPE_INFO +
						" WHERE		" + KEY_DESCRIPTION_TYPE_ID + " = '" + strDescriptionTypeID + "' AND " +
										KEY_ALPHA_KEY + " = '" + strAlphaKey + "' ";
		Cursor cursor = mDb.rawQuery(sql, null);
		DescTypeInfo info = new DescTypeInfo();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.strDescriptionEntryKey = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION_ENTRY_KEY));
				info.nSystemDescriptionTypeYN = cursor.getInt(cursor.getColumnIndex(KEY_DESCRIPTION_TYPE_YN));
				info.strDescriptionTypeID = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION_TYPE_ID));
				info.nNumericCodeYN = cursor.getInt(cursor.getColumnIndex(KEY_NUMERIC_CODE_YN));
				info.nNumericKey = cursor.getInt(cursor.getColumnIndex(KEY_NUMERIC_KEY));
				info.strAlphaKey = cursor.getString(cursor.getColumnIndex(KEY_ALPHA_KEY));
				info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
				info.nAlternateSeq = cursor.getInt(cursor.getColumnIndex(KEY_ALTERNATE_SEQ));
				info.strOwnership = cursor.getString(cursor.getColumnIndex(KEY_OWNERSHIP));
				info.strValue1 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_1));
				info.strValue2 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_2));
				info.strValue3 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_3));
				info.strValue4 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_4));
				info.strValue5 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_5));
				info.strValue6 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_6));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	public List<DescTypeInfo> getDescTypeListWithDescription(String strDescriptionTypeID, String strDescription) {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_DESCTYPE_INFO;
		sql += " WHERE	" + KEY_DESCRIPTION_TYPE_ID + " = '" + strDescriptionTypeID + "' AND " + KEY_DESCRIPTION + " = '" + strDescription + "'";

		Cursor cursor = mDb.rawQuery(sql, null);
		List<DescTypeInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					DescTypeInfo info = new DescTypeInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDescriptionEntryKey = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION_ENTRY_KEY));
					info.nSystemDescriptionTypeYN = cursor.getInt(cursor.getColumnIndex(KEY_DESCRIPTION_TYPE_YN));
					info.strDescriptionTypeID = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION_TYPE_ID));
					info.nNumericCodeYN = cursor.getInt(cursor.getColumnIndex(KEY_NUMERIC_CODE_YN));
					info.nNumericKey = cursor.getInt(cursor.getColumnIndex(KEY_NUMERIC_KEY));
					info.strAlphaKey = cursor.getString(cursor.getColumnIndex(KEY_ALPHA_KEY));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.nAlternateSeq = cursor.getInt(cursor.getColumnIndex(KEY_ALTERNATE_SEQ));
					info.strOwnership = cursor.getString(cursor.getColumnIndex(KEY_OWNERSHIP));
					info.strValue1 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_1));
					info.strValue2 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_2));
					info.strValue3 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_3));
					info.strValue4 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_4));
					info.strValue5 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_5));
					info.strValue6 = cursor.getString(cursor.getColumnIndex(KEY_VALUE_6));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public long insertDescTypeInfo(DescTypeInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_DESCRIPTION_ENTRY_KEY, info.strDescriptionEntryKey);
		values.put(KEY_DESCRIPTION_TYPE_YN, info.nSystemDescriptionTypeYN);
		values.put(KEY_DESCRIPTION_TYPE_ID, info.strDescriptionTypeID);
		values.put(KEY_NUMERIC_CODE_YN, info.nNumericCodeYN);
		values.put(KEY_NUMERIC_KEY, info.nNumericKey);
		values.put(KEY_ALPHA_KEY, info.strAlphaKey);
		values.put(KEY_DESCRIPTION, info.strDescription);
		values.put(KEY_ALTERNATE_SEQ, info.nAlternateSeq);
		values.put(KEY_OWNERSHIP, info.strOwnership);
		values.put(KEY_VALUE_1, info.strValue1);
		values.put(KEY_VALUE_2, info.strValue2);
		values.put(KEY_VALUE_3, info.strValue3);
		values.put(KEY_VALUE_4, info.strValue4);
		values.put(KEY_VALUE_5, info.strValue5);
		values.put(KEY_VALUE_6, info.strValue6);
		return mDb.insert(TABLE_DESCTYPE_INFO, null, values);
	}

	public long updateDescTypeInfo(DescTypeInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_DESCRIPTION_ENTRY_KEY, info.strDescriptionEntryKey);
		values.put(KEY_DESCRIPTION_TYPE_YN, info.nSystemDescriptionTypeYN);
		values.put(KEY_DESCRIPTION_TYPE_ID, info.strDescriptionTypeID);
		values.put(KEY_NUMERIC_CODE_YN, info.nNumericCodeYN);
		values.put(KEY_NUMERIC_KEY, info.nNumericKey);
		values.put(KEY_ALPHA_KEY, info.strAlphaKey);
		values.put(KEY_DESCRIPTION, info.strDescription);
		values.put(KEY_ALTERNATE_SEQ, info.nAlternateSeq);
		values.put(KEY_OWNERSHIP, info.strOwnership);
		values.put(KEY_VALUE_1, info.strValue1);
		values.put(KEY_VALUE_2, info.strValue2);
		values.put(KEY_VALUE_3, info.strValue3);
		values.put(KEY_VALUE_4, info.strValue4);
		values.put(KEY_VALUE_5, info.strValue5);
		values.put(KEY_VALUE_6, info.strValue6);
		return mDb.update(TABLE_DESCTYPE_INFO, values, KEY_IDX + "=" + info.nIdx, null);
	}

	public boolean deleteAllDescTypeInfo() throws SQLException {
		return mDb.delete(TABLE_DESCTYPE_INFO, null, null) > 0;
	}

	// UCamerasInfo
	public List<UCamerasInfo> getUCameraList(int nCustNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_UCAMERA_INFO +
						" WHERE		" + KEY_CUST_NO + " = " + nCustNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UCamerasInfo> list = new ArrayList<UCamerasInfo>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UCamerasInfo info = new UCamerasInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<UCamerasInfo> getUCameraList(int nCustNo, MakeZipFileInfo zipFileInfo) {
		List<UCamerasInfo> list = new ArrayList<UCamerasInfo>();
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			String sql =	" SELECT	* " +
					" FROM		" + TABLE_UCAMERA_INFO +
					" WHERE		" + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_TRXN_NO + " = " + nTrxnNo;
			Cursor cursor = mDb.rawQuery(sql, null);

			try {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						UCamerasInfo info = new UCamerasInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
						info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
						info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
						info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
						info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
						info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
						info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
						cursor.moveToNext();
						list.add(info);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		return list;
	}

	public long insertUCameraInfo(UCamerasInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_DOC_TYPE, info.strDocType);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		values.put(KEY_REFERENCE, info.strReference);
		return mDb.insert(TABLE_UCAMERA_INFO, null, values);
	}

	private boolean deleteAllUCameraInfo(int nCustNo) throws SQLException {
		return mDb.delete(TABLE_UCAMERA_INFO, KEY_CUST_NO + " = " + nCustNo, null) > 0;
	}

	private boolean deleteAllUCameraInfo(int nCustNo, MakeZipFileInfo zipFileInfo) throws SQLException {
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			mDb.delete(TABLE_UCAMERA_INFO, KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_TRXN_NO + " = " + nTrxnNo, null);
		}
		return true;
	}

	// UMessagesInfo
	public List<UMessagesInfo> getUMessagesList(int nCustNo) {
		String sql = 	" SELECT	* " +
						" FROM		" + TABLE_UMESSAGES_INFO +
						" WHERE		" + KEY_CUST_NO + " = " + nCustNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UMessagesInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UMessagesInfo info = new UMessagesInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nMessageID = cursor.getInt(cursor.getColumnIndex(KEY_MESSAGE_ID));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<UMessagesInfo> getUMessagesList(int nCustNo, MakeZipFileInfo zipFileInfo) {
		List<UMessagesInfo> list = new ArrayList<>();
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			String sql = 	" SELECT	* " +
					" FROM		" + TABLE_UMESSAGES_INFO +
					" WHERE		" + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_TRXN_NO + " = " + nTrxnNo;
			Cursor cursor = mDb.rawQuery(sql, null);

			try {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						UMessagesInfo info = new UMessagesInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
						info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
						info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
						info.nMessageID = cursor.getInt(cursor.getColumnIndex(KEY_MESSAGE_ID));
						info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
						info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
						cursor.moveToNext();
						list.add(info);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		return list;
	}

	public long insertUMessagesInfo(UMessagesInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_MESSAGE_ID, info.nMessageID);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		return mDb.insert(TABLE_UMESSAGES_INFO, null, values);
	}

	private boolean deleteAllUMessagesInfo(int nCustNo) throws SQLException {
		return mDb.delete(TABLE_UMESSAGES_INFO, KEY_CUST_NO + " = " + nCustNo, null) > 0;
	}

	private boolean deleteAllUMessagesInfo(int nCustNo, MakeZipFileInfo zipFileInfo) throws SQLException {
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			mDb.delete(TABLE_UMESSAGES_INFO, KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_TRXN_NO + " = " + nTrxnNo, null);
		}
		return true;
	}

	// SurveyInfo
	public String getSurveyHeadTitle(String strSurveyID)
	{
		String sql = "SELECT " + KEY_SURVEY_TITLE + " FROM " + TABLE_SURVEY_INFO + " WHERE " + KEY_SURVEY_ID + " = '" + strSurveyID + "'";
		Cursor cursor = mDb.rawQuery(sql, null);
		String strTitle = "";
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				strTitle = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_TITLE));
			}

		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return strTitle;
	}

	public boolean getSurveyInfoLoaded(int nSurveyType)
	{
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_SURVEY_INFO +
				" WHERE		" + KEY_SURVEY_TYPE + " = " + nSurveyType + " AND " +
				KEY_CHAIN_NO + " = 0 AND " + KEY_CUST_NO + " = 0 ";
		boolean bReturn = false;
		Cursor cursor = mDb.rawQuery(sql, null);
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				bReturn = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED)) == 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return bReturn;
	}

	public SurveyInfo getSurveyInfo(int nSurveyType) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_SURVEY_INFO +
						" WHERE		" + KEY_SURVEY_TYPE + " = " + nSurveyType + " AND " +
										KEY_CHAIN_NO + " = 0 AND " + KEY_CUST_NO + " = 0 ";
		SurveyInfo info = new SurveyInfo();
		Cursor cursor = mDb.rawQuery(sql, null);
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.strSurveyID = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_ID));
				info.bFromSurveyID = cursor.getInt(cursor.getColumnIndex(KEY_FROM_SURVEY_ID)) == 1;
				info.bCompleted = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED)) == 1;
				info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
				info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
				info.strSurveyTitle = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_TITLE));
				info.strCreateDate = cursor.getString(cursor.getColumnIndex(KEY_CREATE_DATE));
				info.strCompletionDate = cursor.getString(cursor.getColumnIndex(KEY_COMPLETION_DATE));
				info.strSurveyResponse = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_RESPONSE));
				info.strMandatory = cursor.getString(cursor.getColumnIndex(KEY_MANDATORY));
				info.nSurveyType = cursor.getInt(cursor.getColumnIndex(KEY_SURVEY_TYPE));
				info.nStatus = cursor.getInt(cursor.getColumnIndex(KEY_STATUS));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	public SurveyInfo getSurveyInfo(int nSurveyType, boolean bAll) {
		String sql = "";

		if( bAll )
		{
			sql =	" SELECT	* " +
					" FROM		" + TABLE_SURVEY_INFO +
					" WHERE		" + KEY_SURVEY_TYPE + " = " + nSurveyType;
		}
		else
		{
			sql =	" SELECT	* " +
					" FROM		" + TABLE_SURVEY_INFO +
					" WHERE		" + KEY_SURVEY_TYPE + " = " + nSurveyType + " AND " +
					KEY_CHAIN_NO + " = 0 AND " + KEY_CUST_NO + " = 0 ";
		}

		SurveyInfo info = new SurveyInfo();
		Cursor cursor = mDb.rawQuery(sql, null);
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.strSurveyID = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_ID));
				info.bFromSurveyID = cursor.getInt(cursor.getColumnIndex(KEY_FROM_SURVEY_ID)) == 1;
				info.bCompleted = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED)) == 1;
				info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
				info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
				info.strSurveyTitle = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_TITLE));
				info.strCreateDate = cursor.getString(cursor.getColumnIndex(KEY_CREATE_DATE));
				info.strCompletionDate = cursor.getString(cursor.getColumnIndex(KEY_COMPLETION_DATE));
				info.strSurveyResponse = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_RESPONSE));
				info.strMandatory = cursor.getString(cursor.getColumnIndex(KEY_MANDATORY));
				info.nSurveyType = cursor.getInt(cursor.getColumnIndex(KEY_SURVEY_TYPE));
				info.nStatus = cursor.getInt(cursor.getColumnIndex(KEY_STATUS));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	public SurveyInfo getSurveyInfoByID(String strSurveyID) {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_SURVEY_INFO +
				" WHERE		" +	KEY_SURVEY_ID + " = '" + strSurveyID + "'";
		Cursor cursor = mDb.rawQuery(sql, null);
		SurveyInfo info = null;
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info = new SurveyInfo();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.strSurveyID = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_ID));
				info.bFromSurveyID = cursor.getInt(cursor.getColumnIndex(KEY_FROM_SURVEY_ID)) == 1;
				info.bCompleted = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED)) == 1;
				info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
				info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
				info.strSurveyTitle = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_TITLE));
				info.strCreateDate = cursor.getString(cursor.getColumnIndex(KEY_CREATE_DATE));
				info.strCompletionDate = cursor.getString(cursor.getColumnIndex(KEY_COMPLETION_DATE));
				info.strSurveyResponse = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_RESPONSE));
				info.strMandatory = cursor.getString(cursor.getColumnIndex(KEY_MANDATORY));
				info.nSurveyType = cursor.getInt(cursor.getColumnIndex(KEY_SURVEY_TYPE));
				info.nStatus = cursor.getInt(cursor.getColumnIndex(KEY_STATUS));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	public List<SurveyInfo> getSurveyList(int nSurveyType, int nCustNo, int nChainNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_SURVEY_INFO +
						" WHERE		" +	KEY_SURVEY_TYPE + " = " + nSurveyType + " AND " +
										KEY_CUST_NO + " = " + nCustNo+ " AND " + KEY_CHAIN_NO + " = " + nChainNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<SurveyInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++) {
					SurveyInfo info = new SurveyInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strSurveyID = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_ID));
					info.bFromSurveyID = cursor.getInt(cursor.getColumnIndex(KEY_FROM_SURVEY_ID)) == 1;
					info.bCompleted = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED)) == 1;
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strSurveyTitle = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_TITLE));
					info.strCreateDate = cursor.getString(cursor.getColumnIndex(KEY_CREATE_DATE));
					info.strCompletionDate = cursor.getString(cursor.getColumnIndex(KEY_COMPLETION_DATE));
					info.strSurveyResponse = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_RESPONSE));
					info.strMandatory = cursor.getString(cursor.getColumnIndex(KEY_MANDATORY));
					info.nSurveyType = cursor.getInt(cursor.getColumnIndex(KEY_SURVEY_TYPE));
					info.nStatus = cursor.getInt(cursor.getColumnIndex(KEY_STATUS));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public long insertSurveyInfo(SurveyInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_SURVEY_ID, info.strSurveyID);
		values.put(KEY_FROM_SURVEY_ID, info.bFromSurveyID ? 1 : 0);
		values.put(KEY_COMPLETED, info.bCompleted ? 1 : 0);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_SURVEY_TITLE, info.strSurveyTitle);
		values.put(KEY_CREATE_DATE, info.strCreateDate);
		values.put(KEY_COMPLETION_DATE, info.strCompletionDate);
		values.put(KEY_SURVEY_RESPONSE, info.strSurveyResponse);
		values.put(KEY_MANDATORY, info.strMandatory);
		values.put(KEY_SURVEY_TYPE, info.nSurveyType);
		values.put(KEY_STATUS, info.nStatus);
		return mDb.insert(TABLE_SURVEY_INFO, null, values);
	}

	public long updateSurveyInfo(SurveyInfo info) {
		ContentValues values = new ContentValues();
		if (!TextUtils.isEmpty(info.strSurveyID)) {
			values.put(KEY_SURVEY_ID, info.strSurveyID);
		}
		if (info.nChainNo > 0) {
			values.put(KEY_CHAIN_NO, info.nChainNo);
		}
		if (info.nCustNo > 0) {
			values.put(KEY_CUST_NO, info.nCustNo);
		}
		values.put(KEY_COMPLETED, info.bCompleted);
		if(!TextUtils.isEmpty(info.strSurveyTitle))
		{
			values.put(KEY_SURVEY_TITLE, info.strSurveyTitle);
		}
		if(!TextUtils.isEmpty(info.strCreateDate))
		{
			values.put(KEY_CREATE_DATE, info.strCreateDate);
		}
		if(!TextUtils.isEmpty(info.strCompletionDate))
		{
			values.put(KEY_COMPLETION_DATE, info.strCompletionDate);
		}
		if(!TextUtils.isEmpty(info.strSurveyResponse))
		{
			values.put(KEY_SURVEY_RESPONSE, info.strSurveyResponse);
		}
		if(!TextUtils.isEmpty(info.strMandatory))
		{
			values.put(KEY_MANDATORY, info.strMandatory);
		}
		values.put(KEY_SURVEY_TYPE, info.nSurveyType);
		values.put(KEY_STATUS, info.nStatus);
		values.put(KEY_FROM_SURVEY_ID, info.bFromSurveyID ? 1 : 0);
		if (info.nIdx != 0) {
			return mDb.update(TABLE_SURVEY_INFO, values, KEY_IDX + " = " + info.nIdx, null);
		}
		return mDb.update(TABLE_SURVEY_INFO, values, KEY_SURVEY_ID + " = '" + info.strSurveyID + "'", null);
	}

	public long updateSurveyInfoStatus(int nIdx, int nStatus) {
		ContentValues values = new ContentValues();
		values.put(KEY_STATUS, nStatus);
		return mDb.update(TABLE_SURVEY_INFO, values, KEY_IDX + " = " + nIdx, null);
	}

	public boolean deleteAllSurveyInfo() throws SQLException {
		return mDb.delete(TABLE_SURVEY_INFO, null, null) > 0;
	}

	// SurvQuesInfo
	public List<SurvQuesInfo> getSurvQuesList(String strSurveyID) {
		String sql = 	" SELECT	* " +
						" FROM		" + TABLE_SURVQUES_INFO +
						" WHERE		" + KEY_SURVEY_ID + " = '" + strSurveyID + "'";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<SurvQuesInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					SurvQuesInfo info = new SurvQuesInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strSurveyID = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_ID));
					info.strQuestionID = cursor.getString(cursor.getColumnIndex(KEY_QUESTION_ID));
					info.strQuestionText = cursor.getString(cursor.getColumnIndex(KEY_QUESTION_TEXT));
					info.strQuestionType = cursor.getString(cursor.getColumnIndex(KEY_QUESTION_TYPE));
					info.strMandatory = cursor.getString(cursor.getColumnIndex(KEY_MANDATORY));
					info.nLookupNo = cursor.getInt(cursor.getColumnIndex(KEY_LOOKUP_NO));
					info.strMultiResp = cursor.getString(cursor.getColumnIndex(KEY_MULTI_RESP));
					info.bImageCaptureReq = cursor.getInt(cursor.getColumnIndex(KEY_IMAGE_CAPTURE_REQ)) == 1;
					info.nQuestionNumberYes = cursor.getInt(cursor.getColumnIndex(KEY_QUESTION_NUMBER_YES));
					info.nQuestionNumberNo = cursor.getInt(cursor.getColumnIndex(KEY_QUESTION_NUMBER_NO));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public long insertSurvQuesInfo(SurvQuesInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_SURVEY_ID, info.strSurveyID);
		values.put(KEY_QUESTION_ID, info.strQuestionID);
		values.put(KEY_QUESTION_TEXT, info.strQuestionText);
		values.put(KEY_QUESTION_TYPE, info.strQuestionType);
		values.put(KEY_MANDATORY, info.strMandatory);
		values.put(KEY_MULTI_RESP, info.strMultiResp);
		values.put(KEY_LOOKUP_NO, info.nLookupNo);
		values.put(KEY_IMAGE_CAPTURE_REQ, info.bImageCaptureReq ? 1: 0);
		values.put(KEY_QUESTION_NUMBER_YES, info.nQuestionNumberYes);
		values.put(KEY_QUESTION_NUMBER_NO, info.nQuestionNumberNo);
		return mDb.insert(TABLE_SURVQUES_INFO, null, values);
	}

	public boolean deleteAllSurvQuesInfo() throws SQLException {
		return mDb.delete(TABLE_SURVQUES_INFO, null, null) > 0;
	}

	// SurvQoptInfo
	public List<SurvQoptInfo> getSurvQoptList(int nLookupNo) {
		String sql = 	" SELECT	* " +
						" FROM		" + TABLE_SURVQOPT_INFO +
						" WHERE		" + KEY_LOOKUP_NO + " = " + nLookupNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<SurvQoptInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					SurvQoptInfo info = new SurvQoptInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nLookupNo = cursor.getInt(cursor.getColumnIndex(KEY_LOOKUP_NO));
					info.strQuestionVal = cursor.getString(cursor.getColumnIndex(KEY_QUESTION_VAL));
					info.strLookupId = cursor.getString(cursor.getColumnIndex(KEY_LOOKUP_ID));
					info.nBranchQuestion = cursor.getInt(cursor.getColumnIndex(KEY_BRANCH_QUESTION));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	// SurvQoptInfo
	public List<SurvQoptInfo> getSurvQoptListWithLookupId(String strLookupId) {
		String sql = 	" SELECT	* " +
				" FROM		" + TABLE_SURVQOPT_INFO +
				" WHERE		" + KEY_LOOKUP_ID + " = '" + strLookupId + "'";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<SurvQoptInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					SurvQoptInfo info = new SurvQoptInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nLookupNo = cursor.getInt(cursor.getColumnIndex(KEY_LOOKUP_NO));
					info.strQuestionVal = cursor.getString(cursor.getColumnIndex(KEY_QUESTION_VAL));
					info.strLookupId = cursor.getString(cursor.getColumnIndex(KEY_LOOKUP_ID));
					info.nBranchQuestion = cursor.getInt(cursor.getColumnIndex(KEY_BRANCH_QUESTION));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public long insertSurvQoptInfo(SurvQoptInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_LOOKUP_NO, info.nLookupNo);
		values.put(KEY_QUESTION_VAL, info.strQuestionVal);
		values.put(KEY_BRANCH_QUESTION, info.nBranchQuestion);
		values.put(KEY_LOOKUP_ID, info.strLookupId);
		return mDb.insert(TABLE_SURVQOPT_INFO, null, values);
	}

	public boolean deleteAllSurvQoptInfo() throws SQLException {
		return mDb.delete(TABLE_SURVQOPT_INFO, null, null) > 0;
	}

	// USurveysInfo
	public List<USurveysInfo> getUSurveysList(int nCustNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_USURVEYS_INFO +
						" WHERE		" + KEY_CUST_NO + " = " + nCustNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<USurveysInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					USurveysInfo info = new USurveysInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strSurveyID = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_ID));
					info.bFromSurveyID = cursor.getInt(cursor.getColumnIndex(KEY_FROM_SURVEY_ID)) == 1;
					info.strCompleted = cursor.getString(cursor.getColumnIndex(KEY_COMPLETED));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<USurveysInfo> getUSurveysList(int nCustNo, MakeZipFileInfo zipFileInfo) {

		List<USurveysInfo> list = new ArrayList<>();
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			String sql =	" SELECT	* " +
					" FROM		" + TABLE_USURVEYS_INFO +
					" WHERE		" + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_TRXN_NO + " = " + nTrxnNo;
			Cursor cursor = mDb.rawQuery(sql, null);

			try {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						USurveysInfo info = new USurveysInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
						info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
						info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
						info.strSurveyID = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_ID));
						info.bFromSurveyID = cursor.getInt(cursor.getColumnIndex(KEY_FROM_SURVEY_ID)) == 1;
						info.strCompleted = cursor.getString(cursor.getColumnIndex(KEY_COMPLETED));
						info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
						info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
						info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
						cursor.moveToNext();
						list.add(info);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		return list;
	}

	public long insertUSurveysInfo(USurveysInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_SURVEY_ID, info.strSurveyID);
		values.put(KEY_FROM_SURVEY_ID, info.bFromSurveyID ? 1 : 0);
		values.put(KEY_COMPLETED, info.strCompleted);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		values.put(KEY_REFERENCE, info.strReference);
		return mDb.insert(TABLE_USURVEYS_INFO, null, values);
	}

	private boolean deleteAllUSurveysInfo(int nCustNo) throws SQLException {
		String sql =	" SELECT	" + KEY_TRXN_NO +
						" FROM		" + TABLE_USURVEYS_INFO +
						" WHERE		" + KEY_CUST_NO + " = " + nCustNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<Long> list = new ArrayList<Long>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					long nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					cursor.moveToNext();
					list.add(nTrxnNo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		for (Long nTrxnNo : list) {
			deleteAllUSurveyDetailInfo(nTrxnNo);
		}
		return mDb.delete(TABLE_USURVEYS_INFO, KEY_CUST_NO + " = " + nCustNo, null) > 0;
	}

	private boolean deleteAllUSurveysInfo(int nCustNo, MakeZipFileInfo zipFileInfo) throws SQLException {
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			String sql =	" SELECT	" + KEY_TRXN_NO +
					" FROM		" + TABLE_USURVEYS_INFO +
					" WHERE		" + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_TRXN_NO + " = " + nTrxnNo;
			Cursor cursor = mDb.rawQuery(sql, null);
			List<Long> list = new ArrayList<Long>();
			try {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						long nTrxnNo1 = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
						cursor.moveToNext();
						list.add(nTrxnNo1);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			for (Long nTrxnNo1 : list) {
				deleteAllUSurveyDetailInfo(nTrxnNo1);
			}
			mDb.delete(TABLE_USURVEYS_INFO, KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_TRXN_NO + " = " + nTrxnNo, null);
		}


		return true;
	}

	// USurveyDetailInfo
	public List<USurveyDetailInfo> getUSurveyDetailList(long nTrxnNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_USURVEY_DETAIL_INFO +
						" WHERE		" + KEY_TRXN_NO + " = " + nTrxnNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<USurveyDetailInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					USurveyDetailInfo info = new USurveyDetailInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strQuestionID = cursor.getString(cursor.getColumnIndex(KEY_QUESTION_ID));
					info.strResultVal = cursor.getString(cursor.getColumnIndex(KEY_RESULT_VAL));
					info.nVisible = cursor.getInt(cursor.getColumnIndex(KEY_VISIBLE));
					info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public long insertUSurveyDetailInfo(USurveyDetailInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_QUESTION_ID, info.strQuestionID);
		values.put(KEY_RESULT_VAL, info.strResultVal);
		values.put(KEY_VISIBLE, info.nVisible);
		values.put(KEY_SEQ_NO, info.nSeqNo);
		return mDb.insert(TABLE_USURVEY_DETAIL_INFO, null, values);
	}

	private boolean deleteAllUSurveyDetailInfo(long nTrxnNo) throws SQLException {
		return mDb.delete(TABLE_USURVEY_DETAIL_INFO, KEY_TRXN_NO + " = " + nTrxnNo, null) > 0;
	}

	
	// CustomerInfo
	public CustomerInfo getCustomerInfo(int nCustNo) {
		String sql =	" SELECT	* " +
						" FROM	" + TABLE_CUSTOMER_INFO +
						" WHERE	" + KEY_CUST_NO + " = " + nCustNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		CustomerInfo info = new CustomerInfo();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
				info.strAltCustNo = cursor.getString(cursor.getColumnIndex(KEY_ALT_CUST_NO));
				info.strName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
				info.strCompanyTaxID = cursor.getString(cursor.getColumnIndex(KEY_COMPANY_TAX_ID));
				info.strCity = cursor.getString(cursor.getColumnIndex(KEY_CITY));
				info.strAddress1 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS1));
				info.strAddress2 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS2));
				info.strPhone = cursor.getString(cursor.getColumnIndex(KEY_PHONE));
				info.strZip = cursor.getString(cursor.getColumnIndex(KEY_ZIP));
				info.strBuyerName = cursor.getString(cursor.getColumnIndex(KEY_BUYER_NAME));
				info.strBuyerPhone = cursor.getString(cursor.getColumnIndex(KEY_BUYER_PHONE));
				info.strAccountName = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_NAME));
				info.strAccountPhone = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_PHONE));
				info.nLocation = cursor.getInt(cursor.getColumnIndex(KEY_LOCATION));
				info.nRouteNo = cursor.getInt(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
				info.strPriority = cursor.getString(cursor.getColumnIndex(KEY_PRIORITY));
				info.nPromoPlan = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_PLAN));
				info.nTemplateGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMPLATE_GRP));
				info.nTempGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMP_GRP));
				info.nFeatGrp = cursor.getInt(cursor.getColumnIndex(KEY_FEAT_GRP));
				info.nPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_GRP));
				info.nAltPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_ALT_PRICE_GRP));
				info.strTaxCode = cursor.getString(cursor.getColumnIndex(KEY_TAX_CODE));
				info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
				info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
				info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
				info.bIsRouteSch = cursor.getInt(cursor.getColumnIndex(KEY_IS_ROUTE_SCH)) == 1 ? true : false;
				info.latitude = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE));
				info.longitude = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE));
				info.bCompleted = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED)) == 1 ? true : false;
				info.strStartTime1 = cursor.getString(cursor.getColumnIndex(KEY_START_TIME1));
				info.strEndTime1 = cursor.getString(cursor.getColumnIndex(KEY_END_TIME1));
				info.strStartTime2 = cursor.getString(cursor.getColumnIndex(KEY_START_TIME2));
				info.strEndTime2 = cursor.getString(cursor.getColumnIndex(KEY_END_TIME2));
				info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
				info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
				info.nOrderNo = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_NO));
				info.nInvoiceFmt = cursor.getInt(cursor.getColumnIndex(KEY_INVOICE_FMT));
				info.nCustPriceMaintFlag = cursor.getInt(cursor.getColumnIndex(KEY_CUST_PRICE_MAINT_FLAG));
				info.nShowPrice = cursor.getInt(cursor.getColumnIndex(KEY_SHOW_PRICE));
				info.nImageCapture = cursor.getInt(cursor.getColumnIndex(KEY_IMAGE_CAPTURE));
				info.nDelayedPrint = cursor.getInt(cursor.getColumnIndex(KEY_DELAYED_FLAG));
				info.strPayType = cursor.getString(cursor.getColumnIndex(KEY_PAY_TYPE));
				info.strBillName = cursor.getString(cursor.getColumnIndex(KEY_BILL_NAME));
				info.strBillAddr1 = cursor.getString(cursor.getColumnIndex(KEY_BILL_ADDR1));
				info.strBillAddr2 = cursor.getString(cursor.getColumnIndex(KEY_BILL_ADDR2));
				info.strBillCity = cursor.getString(cursor.getColumnIndex(KEY_BILL_CITY));
				info.strBillPeriod = cursor.getString(cursor.getColumnIndex(KEY_BILL_PERIOD));
				info.strVendorNo = cursor.getString(cursor.getColumnIndex(KEY_VENDOR_NO));
				info.nBillCustNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CUST_NO));
				info.nBillChainNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CHAIN_NO));
				info.strState = cursor.getString(cursor.getColumnIndex(KEY_STATE));
				info.strSignature = cursor.getString(cursor.getColumnIndex(KEY_SIGNATURE));
				info.strSalEntryMode = cursor.getString(cursor.getColumnIndex(KEY_SALENTRY_MODE));
				info.strRtnEntryMode = cursor.getString(cursor.getColumnIndex(KEY_RTNENTRY_MODE));
				info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
				info.strTerms = cursor.getString(cursor.getColumnIndex(KEY_TERMS));
				info.strDexVersion = cursor.getString(cursor.getColumnIndex(KEY_DEX_VERSION));
				info.nDeferredStatus = cursor.getInt(cursor.getColumnIndex(KEY_DEFRRED_STATUS));
				info.nCompleteStatus = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED_STATUS));
				info.nOrgSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_ORG_SEQ_NO));
				info.strArrivalTime = cursor.getString(cursor.getColumnIndex(KEY_ARRIVAL_TIME));
				info.strAuthGrp = cursor.getString(cursor.getColumnIndex(KEY_AUTH_GRP));
				info.driverLatitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LATITUDE));
				info.driverLongitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LONGITUDE));
				info.strDeliveryWindowTo = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_TO));
				info.strDeliveryWindowFrom = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_FROM));
				info.nCreditHold = cursor.getInt(cursor.getColumnIndex(KEY_CREDIT_HOLD));
				info.nDeliveryFreq = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_FREQ));
				info.strStoreNo = cursor.getString(cursor.getColumnIndex(KEY_STORE_NO));
				info.strDeliveryType = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_TYPE));
				info.bFromAddCustomer = cursor.getInt(cursor.getColumnIndex(KEY_FROM_ADD)) == 1;
				info.strSalesDistrict = cursor.getString(cursor.getColumnIndex(KEY_SALES_DISTRICT));
				info.fHandlingFee = cursor.getFloat(cursor.getColumnIndex(KEY_HANDLING_FEE));
				info.fMiniumOrderValue = cursor.getFloat(cursor.getColumnIndex(KEY_MINIMUM_ORDER_VALUE));
				info.nConSales = cursor.getInt(cursor.getColumnIndex(KEY_CON_SALES));
				info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
				info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
				info.strInvoiceDueDate = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_DATE));
				info.strInvoiceDueMethod = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_METHOD));
				info.strPONumberRequired = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_REQUIRED));
				info.strPONumberMask = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_MASK));
				info.strLegalName = cursor.getString(cursor.getColumnIndex(KEY_LEGAL_NAME));
				info.strCreditLimit = cursor.getString(cursor.getColumnIndex(KEY_CREDIT_LIMIT));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	public CustomerInfo getCustomerInfo(int nCustNo, int nChainNo) {
		String sql = " SELECT	* " + " FROM	" + TABLE_CUSTOMER_INFO + " WHERE	"
				+ KEY_CUST_NO + " = " + nCustNo + " AND "
				+ KEY_CHAIN_NO + " = " + nChainNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		CustomerInfo info = new CustomerInfo();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.nCustNo = cursor
						.getInt(cursor.getColumnIndex(KEY_CUST_NO));
				info.strAltCustNo = cursor
						.getString(cursor.getColumnIndex(KEY_ALT_CUST_NO));
				info.strName = cursor
						.getString(cursor.getColumnIndex(KEY_NAME));
				info.strCompanyTaxID = cursor.getString(cursor
						.getColumnIndex(KEY_COMPANY_TAX_ID));
				info.strCity = cursor
						.getString(cursor.getColumnIndex(KEY_CITY));
				info.strAddress1 = cursor.getString(cursor
						.getColumnIndex(KEY_ADDRESS1));
				info.strAddress2 = cursor.getString(cursor
						.getColumnIndex(KEY_ADDRESS2));
				info.strPhone = cursor.getString(cursor
						.getColumnIndex(KEY_PHONE));
				info.strZip = cursor.getString(cursor.getColumnIndex(KEY_ZIP));
				info.strBuyerName = cursor.getString(cursor
						.getColumnIndex(KEY_BUYER_NAME));
				info.strBuyerPhone = cursor.getString(cursor
						.getColumnIndex(KEY_BUYER_PHONE));
				info.strAccountName = cursor.getString(cursor
						.getColumnIndex(KEY_ACCOUNT_NAME));
				info.strAccountPhone = cursor.getString(cursor
						.getColumnIndex(KEY_ACCOUNT_PHONE));
				info.nLocation = cursor.getInt(cursor
						.getColumnIndex(KEY_LOCATION));
				info.nRouteNo = cursor.getInt(cursor
						.getColumnIndex(KEY_ROUTE_NUMBER));
				info.strPriority = cursor.getString(cursor
						.getColumnIndex(KEY_PRIORITY));
				info.nPromoPlan = cursor.getInt(cursor
						.getColumnIndex(KEY_PROMO_PLAN));
				info.nTemplateGrp = cursor.getInt(cursor
						.getColumnIndex(KEY_TEMPLATE_GRP));
				info.nTempGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMP_GRP));
				info.nFeatGrp = cursor.getInt(cursor.getColumnIndex(KEY_FEAT_GRP));
				info.nPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_GRP));
				info.nAltPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_ALT_PRICE_GRP));
				info.strTaxCode = cursor.getString(cursor
						.getColumnIndex(KEY_TAX_CODE));
				info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
				info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
				info.nChainNo = cursor.getInt(cursor
						.getColumnIndex(KEY_CHAIN_NO));
				info.bIsRouteSch = cursor.getInt(cursor
						.getColumnIndex(KEY_IS_ROUTE_SCH)) == 1 ? true : false;
				info.latitude = cursor.getDouble(cursor
						.getColumnIndex(KEY_LATITUDE));
				info.longitude = cursor.getDouble(cursor
						.getColumnIndex(KEY_LONGITUDE));
				info.bCompleted = cursor.getInt(cursor
						.getColumnIndex(KEY_COMPLETED)) == 1 ? true : false;
				info.strStartTime1 = cursor.getString(cursor
						.getColumnIndex(KEY_START_TIME1));
				info.strEndTime1 = cursor.getString(cursor
						.getColumnIndex(KEY_END_TIME1));
				info.strStartTime2 = cursor.getString(cursor
						.getColumnIndex(KEY_START_TIME2));
				info.strEndTime2 = cursor.getString(cursor
						.getColumnIndex(KEY_END_TIME2));
				info.strDeliveryDate = cursor.getString(cursor
						.getColumnIndex(KEY_DELIVERY_DATE));
				info.nPeriodNo = cursor.getInt(cursor
						.getColumnIndex(KEY_PERIOD_NO));
				info.nOrderNo = cursor.getInt(cursor
						.getColumnIndex(KEY_ORDER_NO));
				info.nInvoiceFmt = cursor.getInt(cursor
						.getColumnIndex(KEY_INVOICE_FMT));
				info.nCustPriceMaintFlag = cursor.getInt(cursor
						.getColumnIndex(KEY_CUST_PRICE_MAINT_FLAG));
				info.nShowPrice = cursor.getInt(cursor
						.getColumnIndex(KEY_SHOW_PRICE));
				info.nDelayedPrint = cursor.getInt(cursor
						.getColumnIndex(KEY_DELAYED_FLAG));
				info.strPayType = cursor.getString(cursor
						.getColumnIndex(KEY_PAY_TYPE));
				info.strBillName = cursor.getString(cursor
						.getColumnIndex(KEY_BILL_NAME));
				info.strBillAddr1 = cursor.getString(cursor
						.getColumnIndex(KEY_BILL_ADDR1));
				info.strBillAddr2 = cursor.getString(cursor
						.getColumnIndex(KEY_BILL_ADDR2));
				info.strBillCity = cursor.getString(cursor
						.getColumnIndex(KEY_BILL_CITY));
				info.strBillPeriod = cursor.getString(cursor.getColumnIndex(KEY_BILL_PERIOD));
				info.strVendorNo = cursor.getString(cursor.getColumnIndex(KEY_VENDOR_NO));
				info.nBillCustNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CUST_NO));
				info.nBillChainNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CHAIN_NO));
				info.strState = cursor.getString(cursor
						.getColumnIndex(KEY_STATE));
				info.strSignature = cursor.getString(cursor
						.getColumnIndex(KEY_SIGNATURE));
				info.strTerms = cursor.getString(cursor
						.getColumnIndex(KEY_TERMS));
				info.strSalEntryMode = cursor.getString(cursor
						.getColumnIndex(KEY_SALENTRY_MODE));
				info.strRtnEntryMode = cursor.getString(cursor
						.getColumnIndex(KEY_RTNENTRY_MODE));
				info.strDumpsAllowed = cursor.getString(cursor
						.getColumnIndex(KEY_DUMPS_ALLOWED));
				info.nImageCapture = cursor.getInt(cursor
						.getColumnIndex(KEY_IMAGE_CAPTURE));
				info.strDexVersion = cursor.getString(cursor.getColumnIndex(KEY_DEX_VERSION));
				info.nDeferredStatus = cursor.getInt(cursor.getColumnIndex(KEY_DEFRRED_STATUS));
				info.nCompleteStatus = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED_STATUS));
				info.nOrgSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_ORG_SEQ_NO));
				info.strArrivalTime = cursor.getString(cursor.getColumnIndex(KEY_ARRIVAL_TIME));
				info.strAuthGrp = cursor.getString(cursor.getColumnIndex(KEY_AUTH_GRP));
				info.driverLatitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LATITUDE));
				info.driverLongitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LONGITUDE));
				info.strDeliveryWindowTo = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_TO));
				info.strDeliveryWindowFrom = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_FROM));
				info.nCreditHold = cursor.getInt(cursor.getColumnIndex(KEY_CREDIT_HOLD));
				info.nDeliveryFreq = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_FREQ));
				info.strStoreNo = cursor.getString(cursor.getColumnIndex(KEY_STORE_NO));
				info.strDeliveryType = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_TYPE));
				info.bFromAddCustomer = cursor.getInt(cursor.getColumnIndex(KEY_FROM_ADD)) == 1;
				info.strSalesDistrict = cursor.getString(cursor.getColumnIndex(KEY_SALES_DISTRICT));
				info.fHandlingFee = cursor.getFloat(cursor.getColumnIndex(KEY_HANDLING_FEE));
				info.fMiniumOrderValue = cursor.getFloat(cursor.getColumnIndex(KEY_MINIMUM_ORDER_VALUE));
				info.nConSales = cursor.getInt(cursor.getColumnIndex(KEY_CON_SALES));
				info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
				info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
				info.strInvoiceDueDate = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_DATE));
				info.strInvoiceDueMethod = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_METHOD));
				info.strPONumberRequired = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_REQUIRED));
				info.strPONumberMask = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_MASK));
				info.strLegalName = cursor.getString(cursor.getColumnIndex(KEY_LEGAL_NAME));
				info.strCreditLimit = cursor.getString(cursor.getColumnIndex(KEY_CREDIT_LIMIT));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	public List<CustomerInfo> getCustomerList(boolean bIsRouteSch) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_CUSTOMER_INFO +
						" WHERE		" + KEY_IS_ROUTE_SCH + " = " + (bIsRouteSch == true ? 1 : 0);
		Cursor cursor = mDb.rawQuery(sql, null);
		List<CustomerInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					CustomerInfo info = new CustomerInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strAltCustNo = cursor.getString(cursor.getColumnIndex(KEY_ALT_CUST_NO));
					info.strName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
					info.strCompanyTaxID = cursor.getString(cursor.getColumnIndex(KEY_COMPANY_TAX_ID));
					info.strCity = cursor.getString(cursor.getColumnIndex(KEY_CITY));
					info.strAddress1 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS1));
					info.strAddress2 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS2));
					info.strPhone = cursor.getString(cursor.getColumnIndex(KEY_PHONE));
					info.strZip = cursor.getString(cursor.getColumnIndex(KEY_ZIP));
					info.strBuyerName = cursor.getString(cursor.getColumnIndex(KEY_BUYER_NAME));
					info.strBuyerPhone = cursor.getString(cursor.getColumnIndex(KEY_BUYER_PHONE));
					info.strAccountName = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_NAME));
					info.strAccountPhone = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_PHONE));
					info.nLocation = cursor.getInt(cursor.getColumnIndex(KEY_LOCATION));
					info.nRouteNo = cursor.getInt(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
					info.strPriority = cursor.getString(cursor.getColumnIndex(KEY_PRIORITY));
					info.nPromoPlan = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_PLAN));
					info.nTemplateGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMPLATE_GRP));
					info.nTempGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMP_GRP));
					info.nFeatGrp = cursor.getInt(cursor.getColumnIndex(KEY_FEAT_GRP));
					info.nPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_GRP));
					info.nAltPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_ALT_PRICE_GRP));
					info.strTaxCode = cursor.getString(cursor.getColumnIndex(KEY_TAX_CODE));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.bIsRouteSch = cursor.getInt(cursor.getColumnIndex(KEY_IS_ROUTE_SCH)) == 1 ? true : false;
					info.latitude = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE));
					info.longitude = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE));
					info.bCompleted = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED)) == 1 ? true : false;
					info.strStartTime1 = cursor.getString(cursor.getColumnIndex(KEY_START_TIME1));
					info.strEndTime1 = cursor.getString(cursor.getColumnIndex(KEY_END_TIME1));
					info.strStartTime2 = cursor.getString(cursor.getColumnIndex(KEY_START_TIME2));
					info.strEndTime2 = cursor.getString(cursor.getColumnIndex(KEY_END_TIME2));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.nOrderNo = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_NO));
					info.strShipToState = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_STATE));
					info.strShipToZip = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_ZIP));
					info.nInvoiceFmt = cursor.getInt(cursor.getColumnIndex(KEY_INVOICE_FMT));
					info.nCustPriceMaintFlag = cursor.getInt(cursor.getColumnIndex(KEY_CUST_PRICE_MAINT_FLAG));
					info.nShowPrice = cursor.getInt(cursor.getColumnIndex(KEY_SHOW_PRICE));
					info.nDelayedPrint = cursor.getInt(cursor.getColumnIndex(KEY_DELAYED_FLAG));
					info.strPayType = cursor.getString(cursor.getColumnIndex(KEY_PAY_TYPE));
					info.strBillName = cursor.getString(cursor.getColumnIndex(KEY_BILL_NAME));
					info.strBillAddr1 = cursor.getString(cursor.getColumnIndex(KEY_BILL_ADDR1));
					info.strBillAddr2 = cursor.getString(cursor.getColumnIndex(KEY_BILL_ADDR2));
					info.strBillCity = cursor.getString(cursor.getColumnIndex(KEY_BILL_CITY));
					info.strBillPeriod = cursor.getString(cursor.getColumnIndex(KEY_BILL_PERIOD));
					info.strVendorNo = cursor.getString(cursor.getColumnIndex(KEY_VENDOR_NO));
					info.nBillCustNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CUST_NO));
					info.nBillChainNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CHAIN_NO));
					info.strState = cursor.getString(cursor.getColumnIndex(KEY_STATE));
					info.strSignature = cursor.getString(cursor.getColumnIndex(KEY_SIGNATURE));
					info.strSalEntryMode = cursor.getString(cursor.getColumnIndex(KEY_SALENTRY_MODE));
					info.strRtnEntryMode = cursor.getString(cursor.getColumnIndex(KEY_RTNENTRY_MODE));
					info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
					info.strTerms = cursor.getString(cursor.getColumnIndex(KEY_TERMS));
					info.nImageCapture = cursor.getInt(cursor.getColumnIndex(KEY_IMAGE_CAPTURE));
					info.strDexVersion = cursor.getString(cursor.getColumnIndex(KEY_DEX_VERSION));
					info.nDeferredStatus = cursor.getInt(cursor.getColumnIndex(KEY_DEFRRED_STATUS));
					info.nCompleteStatus = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED_STATUS));
					info.nOrgSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_ORG_SEQ_NO));
					info.strArrivalTime = cursor.getString(cursor.getColumnIndex(KEY_ARRIVAL_TIME));
					info.strAuthGrp = cursor.getString(cursor.getColumnIndex(KEY_AUTH_GRP));
					info.driverLatitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LATITUDE));
					info.driverLongitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LONGITUDE));
					info.strDeliveryWindowTo = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_TO));
					info.strDeliveryWindowFrom = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_FROM));
					info.nCreditHold = cursor.getInt(cursor.getColumnIndex(KEY_CREDIT_HOLD));
					info.nDeliveryFreq = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_FREQ));
					info.strStoreNo = cursor.getString(cursor.getColumnIndex(KEY_STORE_NO));
					info.strDeliveryType = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_TYPE));
					info.bFromAddCustomer = cursor.getInt(cursor.getColumnIndex(KEY_FROM_ADD)) == 1;
					info.strSalesDistrict = cursor.getString(cursor.getColumnIndex(KEY_SALES_DISTRICT));
					info.fHandlingFee = cursor.getFloat(cursor.getColumnIndex(KEY_HANDLING_FEE));
					info.fMiniumOrderValue = cursor.getFloat(cursor.getColumnIndex(KEY_MINIMUM_ORDER_VALUE));
					info.nConSales = cursor.getInt(cursor.getColumnIndex(KEY_CON_SALES));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
					info.strInvoiceDueDate = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_DATE));
					info.strInvoiceDueMethod = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_METHOD));
					info.strPONumberRequired = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_REQUIRED));
					info.strPONumberMask = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_MASK));
					info.strLegalName = cursor.getString(cursor.getColumnIndex(KEY_LEGAL_NAME));
					info.strCreditLimit = cursor.getString(cursor.getColumnIndex(KEY_CREDIT_LIMIT));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<CustomerInfo> getCustomerInfoListWithChainNumber(int nChainNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_CUSTOMER_INFO +
						" WHERE		" + KEY_CHAIN_NO + " = " + nChainNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<CustomerInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					CustomerInfo info = new CustomerInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strAltCustNo = cursor.getString(cursor.getColumnIndex(KEY_ALT_CUST_NO));
					info.strName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
					info.strCompanyTaxID = cursor.getString(cursor.getColumnIndex(KEY_COMPANY_TAX_ID));
					info.strCity = cursor.getString(cursor.getColumnIndex(KEY_CITY));
					info.strAddress1 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS1));
					info.strAddress2 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS2));
					info.strPhone = cursor.getString(cursor.getColumnIndex(KEY_PHONE));
					info.strZip = cursor.getString(cursor.getColumnIndex(KEY_ZIP));
					info.strBuyerName = cursor.getString(cursor.getColumnIndex(KEY_BUYER_NAME));
					info.strBuyerPhone = cursor.getString(cursor.getColumnIndex(KEY_BUYER_PHONE));
					info.strAccountName = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_NAME));
					info.strAccountPhone = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_PHONE));
					info.nLocation = cursor.getInt(cursor.getColumnIndex(KEY_LOCATION));
					info.nRouteNo = cursor.getInt(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
					info.strPriority = cursor.getString(cursor.getColumnIndex(KEY_PRIORITY));
					info.nPromoPlan = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_PLAN));
					info.nTemplateGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMPLATE_GRP));
					info.nTempGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMP_GRP));
					info.nFeatGrp = cursor.getInt(cursor.getColumnIndex(KEY_FEAT_GRP));
					info.nPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_GRP));
					info.nAltPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_ALT_PRICE_GRP));
					info.strTaxCode = cursor.getString(cursor.getColumnIndex(KEY_TAX_CODE));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.bIsRouteSch = cursor.getInt(cursor.getColumnIndex(KEY_IS_ROUTE_SCH)) == 1 ? true : false;
					info.latitude = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE));
					info.longitude = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE));
					info.bCompleted = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED)) == 1 ? true : false;
					info.strStartTime1 = cursor.getString(cursor.getColumnIndex(KEY_START_TIME1));
					info.strEndTime1 = cursor.getString(cursor.getColumnIndex(KEY_END_TIME1));
					info.strStartTime2 = cursor.getString(cursor.getColumnIndex(KEY_START_TIME2));
					info.strEndTime2 = cursor.getString(cursor.getColumnIndex(KEY_END_TIME2));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.nOrderNo = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_NO));
					info.strShipToState = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_STATE));
					info.strShipToZip = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_ZIP));
					info.nInvoiceFmt = cursor.getInt(cursor.getColumnIndex(KEY_INVOICE_FMT));
					info.nCustPriceMaintFlag = cursor.getInt(cursor.getColumnIndex(KEY_CUST_PRICE_MAINT_FLAG));
					info.nShowPrice = cursor.getInt(cursor.getColumnIndex(KEY_SHOW_PRICE));
					info.nDelayedPrint = cursor.getInt(cursor.getColumnIndex(KEY_DELAYED_FLAG));
					info.strPayType = cursor.getString(cursor.getColumnIndex(KEY_PAY_TYPE));
					info.strBillName = cursor.getString(cursor.getColumnIndex(KEY_BILL_NAME));
					info.strBillAddr1 = cursor.getString(cursor.getColumnIndex(KEY_BILL_ADDR1));
					info.strBillAddr2 = cursor.getString(cursor.getColumnIndex(KEY_BILL_ADDR2));
					info.strBillCity = cursor.getString(cursor.getColumnIndex(KEY_BILL_CITY));
					info.strBillPeriod = cursor.getString(cursor.getColumnIndex(KEY_BILL_PERIOD));
					info.strVendorNo = cursor.getString(cursor.getColumnIndex(KEY_VENDOR_NO));
					info.nBillCustNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CUST_NO));
					info.nBillChainNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CHAIN_NO));
					info.strState = cursor.getString(cursor.getColumnIndex(KEY_STATE));
					info.strSignature = cursor.getString(cursor.getColumnIndex(KEY_SIGNATURE));
					info.strSalEntryMode = cursor.getString(cursor.getColumnIndex(KEY_SALENTRY_MODE));
					info.strRtnEntryMode = cursor.getString(cursor.getColumnIndex(KEY_RTNENTRY_MODE));
					info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
					info.strTerms = cursor.getString(cursor.getColumnIndex(KEY_TERMS));
					info.nImageCapture = cursor.getInt(cursor.getColumnIndex(KEY_IMAGE_CAPTURE));
					info.strDexVersion = cursor.getString(cursor.getColumnIndex(KEY_DEX_VERSION));
					info.nDeferredStatus = cursor.getInt(cursor.getColumnIndex(KEY_DEFRRED_STATUS));
					info.nCompleteStatus = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED_STATUS));
					info.nOrgSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_ORG_SEQ_NO));
					info.strArrivalTime = cursor.getString(cursor.getColumnIndex(KEY_ARRIVAL_TIME));
					info.strAuthGrp = cursor.getString(cursor.getColumnIndex(KEY_AUTH_GRP));
					info.driverLatitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LATITUDE));
					info.driverLongitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LONGITUDE));
					info.strDeliveryWindowTo = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_TO));
					info.strDeliveryWindowFrom = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_FROM));
					info.nCreditHold = cursor.getInt(cursor.getColumnIndex(KEY_CREDIT_HOLD));
					info.nDeliveryFreq = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_FREQ));
					info.strStoreNo = cursor.getString(cursor.getColumnIndex(KEY_STORE_NO));
					info.strDeliveryType = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_TYPE));
					info.bFromAddCustomer = cursor.getInt(cursor.getColumnIndex(KEY_FROM_ADD)) == 1;
					info.strSalesDistrict = cursor.getString(cursor.getColumnIndex(KEY_SALES_DISTRICT));
					info.fHandlingFee = cursor.getFloat(cursor.getColumnIndex(KEY_HANDLING_FEE));
					info.fMiniumOrderValue = cursor.getFloat(cursor.getColumnIndex(KEY_MINIMUM_ORDER_VALUE));
					info.nConSales = cursor.getInt(cursor.getColumnIndex(KEY_CON_SALES));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
					info.strInvoiceDueDate = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_DATE));
					info.strInvoiceDueMethod = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_METHOD));
					info.strPONumberRequired = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_REQUIRED));
					info.strPONumberMask = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_MASK));
					info.strLegalName = cursor.getString(cursor.getColumnIndex(KEY_LEGAL_NAME));
					info.strCreditLimit = cursor.getString(cursor.getColumnIndex(KEY_CREDIT_LIMIT));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<CustomerInfo> getUnPlanedCustomerList(int nDayNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_CUSTOMER_INFO +
						" WHERE		(" + KEY_IS_ROUTE_SCH + " = 0) OR ( " + KEY_IS_ROUTE_SCH + " = 1 AND " + KEY_DAY_NO + " != " + nDayNo + ")";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<CustomerInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					CustomerInfo info = new CustomerInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strAltCustNo = cursor.getString(cursor.getColumnIndex(KEY_ALT_CUST_NO));
					info.strName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
					info.strCompanyTaxID = cursor.getString(cursor.getColumnIndex(KEY_COMPANY_TAX_ID));
					info.strCity = cursor.getString(cursor.getColumnIndex(KEY_CITY));
					info.strAddress1 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS1));
					info.strAddress2 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS2));
					info.strPhone = cursor.getString(cursor.getColumnIndex(KEY_PHONE));
					info.strZip = cursor.getString(cursor.getColumnIndex(KEY_ZIP));
					info.strBuyerName = cursor.getString(cursor.getColumnIndex(KEY_BUYER_NAME));
					info.strBuyerPhone = cursor.getString(cursor.getColumnIndex(KEY_BUYER_PHONE));
					info.strAccountName = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_NAME));
					info.strAccountPhone = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_PHONE));
					info.nLocation = cursor.getInt(cursor.getColumnIndex(KEY_LOCATION));
					info.nRouteNo = cursor.getInt(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
					info.strPriority = cursor.getString(cursor.getColumnIndex(KEY_PRIORITY));
					info.nPromoPlan = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_PLAN));
					info.nTemplateGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMPLATE_GRP));
					info.nTempGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMP_GRP));
					info.nFeatGrp = cursor.getInt(cursor.getColumnIndex(KEY_FEAT_GRP));
					info.nPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_GRP));
					info.nAltPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_ALT_PRICE_GRP));
					info.strTaxCode = cursor.getString(cursor.getColumnIndex(KEY_TAX_CODE));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.bIsRouteSch = cursor.getInt(cursor.getColumnIndex(KEY_IS_ROUTE_SCH)) == 1 ? true : false;
					info.latitude = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE));
					info.longitude = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE));
					info.bCompleted = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED)) == 1 ? true : false;
					info.strStartTime1 = cursor.getString(cursor.getColumnIndex(KEY_START_TIME1));
					info.strEndTime1 = cursor.getString(cursor.getColumnIndex(KEY_END_TIME1));
					info.strStartTime2 = cursor.getString(cursor.getColumnIndex(KEY_START_TIME2));
					info.strEndTime2 = cursor.getString(cursor.getColumnIndex(KEY_END_TIME2));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.nOrderNo = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_NO));
					info.strShipToState = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_STATE));
					info.strShipToZip = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_ZIP));
					info.nInvoiceFmt = cursor.getInt(cursor.getColumnIndex(KEY_INVOICE_FMT));
					info.nCustPriceMaintFlag = cursor.getInt(cursor.getColumnIndex(KEY_CUST_PRICE_MAINT_FLAG));
					info.nShowPrice = cursor.getInt(cursor.getColumnIndex(KEY_SHOW_PRICE));
					info.nDelayedPrint = cursor.getInt(cursor.getColumnIndex(KEY_DELAYED_FLAG));
					info.strPayType = cursor.getString(cursor.getColumnIndex(KEY_PAY_TYPE));
					info.strBillName = cursor.getString(cursor.getColumnIndex(KEY_BILL_NAME));
					info.strBillAddr1 = cursor.getString(cursor.getColumnIndex(KEY_BILL_ADDR1));
					info.strBillAddr2 = cursor.getString(cursor.getColumnIndex(KEY_BILL_ADDR2));
					info.strBillCity = cursor.getString(cursor.getColumnIndex(KEY_BILL_CITY));
					info.strBillPeriod = cursor.getString(cursor.getColumnIndex(KEY_BILL_PERIOD));
					info.strVendorNo = cursor.getString(cursor.getColumnIndex(KEY_VENDOR_NO));
					info.nBillCustNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CUST_NO));
					info.nBillChainNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CHAIN_NO));
					info.strState = cursor.getString(cursor.getColumnIndex(KEY_STATE));
					info.strSignature = cursor.getString(cursor.getColumnIndex(KEY_SIGNATURE));
					info.strSalEntryMode = cursor.getString(cursor.getColumnIndex(KEY_SALENTRY_MODE));
					info.strRtnEntryMode = cursor.getString(cursor.getColumnIndex(KEY_RTNENTRY_MODE));
					info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
					info.strTerms = cursor.getString(cursor.getColumnIndex(KEY_TERMS));
					info.nImageCapture = cursor.getInt(cursor.getColumnIndex(KEY_IMAGE_CAPTURE));
					info.strDexVersion = cursor.getString(cursor.getColumnIndex(KEY_DEX_VERSION));
					info.nDeferredStatus = cursor.getInt(cursor.getColumnIndex(KEY_DEFRRED_STATUS));
					info.nCompleteStatus = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED_STATUS));
					info.nOrgSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_ORG_SEQ_NO));
					info.strArrivalTime = cursor.getString(cursor.getColumnIndex(KEY_ARRIVAL_TIME));
					info.strAuthGrp = cursor.getString(cursor.getColumnIndex(KEY_AUTH_GRP));
					info.driverLatitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LATITUDE));
					info.driverLongitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LONGITUDE));
					info.strDeliveryWindowTo = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_TO));
					info.strDeliveryWindowFrom = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_FROM));
					info.nCreditHold = cursor.getInt(cursor.getColumnIndex(KEY_CREDIT_HOLD));
					info.nDeliveryFreq = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_FREQ));
					info.strStoreNo = cursor.getString(cursor.getColumnIndex(KEY_STORE_NO));
					info.strDeliveryType = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_TYPE));
					info.bFromAddCustomer = cursor.getInt(cursor.getColumnIndex(KEY_FROM_ADD)) == 1;
					info.strSalesDistrict = cursor.getString(cursor.getColumnIndex(KEY_SALES_DISTRICT));
					info.fHandlingFee = cursor.getFloat(cursor.getColumnIndex(KEY_HANDLING_FEE));
					info.fMiniumOrderValue = cursor.getFloat(cursor.getColumnIndex(KEY_MINIMUM_ORDER_VALUE));
					info.nConSales = cursor.getInt(cursor.getColumnIndex(KEY_CON_SALES));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
					info.strInvoiceDueDate = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_DATE));
					info.strInvoiceDueMethod = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_METHOD));
					info.strPONumberRequired = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_REQUIRED));
					info.strPONumberMask = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_MASK));
					info.strLegalName = cursor.getString(cursor.getColumnIndex(KEY_LEGAL_NAME));
					info.strCreditLimit = cursor.getString(cursor.getColumnIndex(KEY_CREDIT_LIMIT));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<CustomerInfo> getCompletedCustomerList() {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_CUSTOMER_INFO +
				" WHERE		" + KEY_COMPLETED + " = 1";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<CustomerInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					CustomerInfo info = new CustomerInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strAltCustNo = cursor.getString(cursor.getColumnIndex(KEY_ALT_CUST_NO));
					info.strName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
					info.strCompanyTaxID = cursor.getString(cursor.getColumnIndex(KEY_COMPANY_TAX_ID));
					info.strCity = cursor.getString(cursor.getColumnIndex(KEY_CITY));
					info.strAddress1 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS1));
					info.strAddress2 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS2));
					info.strPhone = cursor.getString(cursor.getColumnIndex(KEY_PHONE));
					info.strZip = cursor.getString(cursor.getColumnIndex(KEY_ZIP));
					info.strBuyerName = cursor.getString(cursor.getColumnIndex(KEY_BUYER_NAME));
					info.strBuyerPhone = cursor.getString(cursor.getColumnIndex(KEY_BUYER_PHONE));
					info.strAccountName = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_NAME));
					info.strAccountPhone = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_PHONE));
					info.nLocation = cursor.getInt(cursor.getColumnIndex(KEY_LOCATION));
					info.nRouteNo = cursor.getInt(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
					info.strPriority = cursor.getString(cursor.getColumnIndex(KEY_PRIORITY));
					info.nPromoPlan = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_PLAN));
					info.nTemplateGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMPLATE_GRP));
					info.nTempGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMP_GRP));
					info.nFeatGrp = cursor.getInt(cursor.getColumnIndex(KEY_FEAT_GRP));
					info.nPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_GRP));
					info.nAltPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_ALT_PRICE_GRP));
					info.strTaxCode = cursor.getString(cursor.getColumnIndex(KEY_TAX_CODE));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.bIsRouteSch = cursor.getInt(cursor.getColumnIndex(KEY_IS_ROUTE_SCH)) == 1 ? true : false;
					info.latitude = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE));
					info.longitude = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE));
					info.bCompleted = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED)) == 1 ? true : false;
					info.strStartTime1 = cursor.getString(cursor.getColumnIndex(KEY_START_TIME1));
					info.strEndTime1 = cursor.getString(cursor.getColumnIndex(KEY_END_TIME1));
					info.strStartTime2 = cursor.getString(cursor.getColumnIndex(KEY_START_TIME2));
					info.strEndTime2 = cursor.getString(cursor.getColumnIndex(KEY_END_TIME2));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.nOrderNo = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_NO));
					info.strShipToState = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_STATE));
					info.strShipToZip = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_ZIP));
					info.nInvoiceFmt = cursor.getInt(cursor.getColumnIndex(KEY_INVOICE_FMT));
					info.nCustPriceMaintFlag = cursor.getInt(cursor.getColumnIndex(KEY_CUST_PRICE_MAINT_FLAG));
					info.nShowPrice = cursor.getInt(cursor.getColumnIndex(KEY_SHOW_PRICE));
					info.nDelayedPrint = cursor.getInt(cursor.getColumnIndex(KEY_DELAYED_FLAG));
					info.strPayType = cursor.getString(cursor.getColumnIndex(KEY_PAY_TYPE));
					info.strBillName = cursor.getString(cursor.getColumnIndex(KEY_BILL_NAME));
					info.strBillAddr1 = cursor.getString(cursor.getColumnIndex(KEY_BILL_ADDR1));
					info.strBillAddr2 = cursor.getString(cursor.getColumnIndex(KEY_BILL_ADDR2));
					info.strBillCity = cursor.getString(cursor.getColumnIndex(KEY_BILL_CITY));
					info.strBillPeriod = cursor.getString(cursor.getColumnIndex(KEY_BILL_PERIOD));
					info.strVendorNo = cursor.getString(cursor.getColumnIndex(KEY_VENDOR_NO));
					info.nBillCustNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CUST_NO));
					info.nBillChainNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CHAIN_NO));
					info.strState = cursor.getString(cursor.getColumnIndex(KEY_STATE));
					info.strSignature = cursor.getString(cursor.getColumnIndex(KEY_SIGNATURE));
					info.strSalEntryMode = cursor.getString(cursor.getColumnIndex(KEY_SALENTRY_MODE));
					info.strRtnEntryMode = cursor.getString(cursor.getColumnIndex(KEY_RTNENTRY_MODE));
					info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
					info.strTerms = cursor.getString(cursor.getColumnIndex(KEY_TERMS));
					info.nImageCapture = cursor.getInt(cursor.getColumnIndex(KEY_IMAGE_CAPTURE));
					info.strDexVersion = cursor.getString(cursor.getColumnIndex(KEY_DEX_VERSION));
					info.nDeferredStatus = cursor.getInt(cursor.getColumnIndex(KEY_DEFRRED_STATUS));
					info.nCompleteStatus = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED_STATUS));
					info.nOrgSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_ORG_SEQ_NO));
					info.strArrivalTime = cursor.getString(cursor.getColumnIndex(KEY_ARRIVAL_TIME));
					info.strAuthGrp = cursor.getString(cursor.getColumnIndex(KEY_AUTH_GRP));
					info.driverLatitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LATITUDE));
					info.driverLongitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LONGITUDE));
					info.strDeliveryWindowTo = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_TO));
					info.strDeliveryWindowFrom = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_FROM));
					info.nCreditHold = cursor.getInt(cursor.getColumnIndex(KEY_CREDIT_HOLD));
					info.nDeliveryFreq = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_FREQ));
					info.strStoreNo = cursor.getString(cursor.getColumnIndex(KEY_STORE_NO));
					info.strDeliveryType = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_TYPE));
					info.bFromAddCustomer = cursor.getInt(cursor.getColumnIndex(KEY_FROM_ADD)) == 1;
					info.strSalesDistrict = cursor.getString(cursor.getColumnIndex(KEY_SALES_DISTRICT));
					info.fHandlingFee = cursor.getFloat(cursor.getColumnIndex(KEY_HANDLING_FEE));
					info.fMiniumOrderValue = cursor.getFloat(cursor.getColumnIndex(KEY_MINIMUM_ORDER_VALUE));
					info.nConSales = cursor.getInt(cursor.getColumnIndex(KEY_CON_SALES));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
					info.strInvoiceDueDate = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_DATE));
					info.strInvoiceDueMethod = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_METHOD));
					info.strPONumberRequired = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_REQUIRED));
					info.strPONumberMask = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_MASK));
					info.strLegalName = cursor.getString(cursor.getColumnIndex(KEY_LEGAL_NAME));
					info.strCreditLimit = cursor.getString(cursor.getColumnIndex(KEY_CREDIT_LIMIT));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<CustomerInfo> getCustomerList(boolean bIsRouteSch, boolean bDeferredCustomer, int nDayNo) {
		String sql;
		if(bDeferredCustomer)
		{
			sql =	" SELECT	* " +
					" FROM		" + TABLE_CUSTOMER_INFO +
					" WHERE		" + KEY_DEFRRED_STATUS + " = 3 AND " + KEY_DAY_NO + " = " + nDayNo + " AND " + KEY_IS_ROUTE_SCH + " = " + (bIsRouteSch == true ? 1 : 0);
		}
		else
		{
			sql =	" SELECT	* " +
				" FROM		" + TABLE_CUSTOMER_INFO +
				" WHERE		" + KEY_DAY_NO + " = " + nDayNo + " AND " + KEY_IS_ROUTE_SCH + " = " + (bIsRouteSch == true ? 1 : 0);
		}

		Cursor cursor = mDb.rawQuery(sql, null);
		List<CustomerInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					CustomerInfo info = new CustomerInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strAltCustNo = cursor.getString(cursor.getColumnIndex(KEY_ALT_CUST_NO));
					info.strName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
					info.strCompanyTaxID = cursor.getString(cursor.getColumnIndex(KEY_COMPANY_TAX_ID));
					info.strCity = cursor.getString(cursor.getColumnIndex(KEY_CITY));
					info.strAddress1 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS1));
					info.strAddress2 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS2));
					info.strPhone = cursor.getString(cursor.getColumnIndex(KEY_PHONE));
					info.strZip = cursor.getString(cursor.getColumnIndex(KEY_ZIP));
					info.strBuyerName = cursor.getString(cursor.getColumnIndex(KEY_BUYER_NAME));
					info.strBuyerPhone = cursor.getString(cursor.getColumnIndex(KEY_BUYER_PHONE));
					info.strAccountName = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_NAME));
					info.strAccountPhone = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_PHONE));
					info.nLocation = cursor.getInt(cursor.getColumnIndex(KEY_LOCATION));
					info.nRouteNo = cursor.getInt(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
					info.strPriority = cursor.getString(cursor.getColumnIndex(KEY_PRIORITY));
					info.nPromoPlan = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_PLAN));
					info.nTemplateGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMPLATE_GRP));
					info.nTempGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMP_GRP));
					info.nFeatGrp = cursor.getInt(cursor.getColumnIndex(KEY_FEAT_GRP));
					info.nPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_GRP));
					info.nAltPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_ALT_PRICE_GRP));
					info.strTaxCode = cursor.getString(cursor.getColumnIndex(KEY_TAX_CODE));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.bIsRouteSch = cursor.getInt(cursor.getColumnIndex(KEY_IS_ROUTE_SCH)) == 1 ? true : false;
					info.latitude = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE));
					info.longitude = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE));
					info.bCompleted = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED)) == 1 ? true : false;
					info.strStartTime1 = cursor.getString(cursor.getColumnIndex(KEY_START_TIME1));
					info.strEndTime1 = cursor.getString(cursor.getColumnIndex(KEY_END_TIME1));
					info.strStartTime2 = cursor.getString(cursor.getColumnIndex(KEY_START_TIME2));
					info.strEndTime2 = cursor.getString(cursor.getColumnIndex(KEY_END_TIME2));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.nOrderNo = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_NO));
					info.strShipToState = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_STATE));
					info.strShipToZip = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_ZIP));
					info.nInvoiceFmt = cursor.getInt(cursor.getColumnIndex(KEY_INVOICE_FMT));
					info.nCustPriceMaintFlag = cursor.getInt(cursor.getColumnIndex(KEY_CUST_PRICE_MAINT_FLAG));
					info.nShowPrice = cursor.getInt(cursor.getColumnIndex(KEY_SHOW_PRICE));
					info.nDelayedPrint = cursor.getInt(cursor.getColumnIndex(KEY_DELAYED_FLAG));
					info.strPayType = cursor.getString(cursor.getColumnIndex(KEY_PAY_TYPE));
					info.strBillName = cursor.getString(cursor.getColumnIndex(KEY_BILL_NAME));
					info.strBillAddr1 = cursor.getString(cursor.getColumnIndex(KEY_BILL_ADDR1));
					info.strBillAddr2 = cursor.getString(cursor.getColumnIndex(KEY_BILL_ADDR2));
					info.strBillCity = cursor.getString(cursor.getColumnIndex(KEY_BILL_CITY));
					info.strBillPeriod = cursor.getString(cursor.getColumnIndex(KEY_BILL_PERIOD));
					info.strVendorNo = cursor.getString(cursor.getColumnIndex(KEY_VENDOR_NO));
					info.nBillCustNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CUST_NO));
					info.nBillChainNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CHAIN_NO));
					info.strState = cursor.getString(cursor.getColumnIndex(KEY_STATE));
					info.strSignature = cursor.getString(cursor.getColumnIndex(KEY_SIGNATURE));
					info.strSalEntryMode = cursor.getString(cursor.getColumnIndex(KEY_SALENTRY_MODE));
					info.strRtnEntryMode = cursor.getString(cursor.getColumnIndex(KEY_RTNENTRY_MODE));
					info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
					info.strTerms = cursor.getString(cursor.getColumnIndex(KEY_TERMS));
					info.nImageCapture = cursor.getInt(cursor.getColumnIndex(KEY_IMAGE_CAPTURE));
					info.strDexVersion = cursor.getString(cursor.getColumnIndex(KEY_DEX_VERSION));
					info.nDeferredStatus = cursor.getInt(cursor.getColumnIndex(KEY_DEFRRED_STATUS));
					info.nCompleteStatus = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED_STATUS));
					info.nOrgSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_ORG_SEQ_NO));
					info.strArrivalTime = cursor.getString(cursor.getColumnIndex(KEY_ARRIVAL_TIME));
					info.strAuthGrp = cursor.getString(cursor.getColumnIndex(KEY_AUTH_GRP));
					info.driverLatitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LATITUDE));
					info.driverLongitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LONGITUDE));
					info.strDeliveryWindowTo = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_TO));
					info.strDeliveryWindowFrom = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_FROM));
					info.nCreditHold = cursor.getInt(cursor.getColumnIndex(KEY_CREDIT_HOLD));
					info.nDeliveryFreq = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_FREQ));
					info.strStoreNo = cursor.getString(cursor.getColumnIndex(KEY_STORE_NO));
					info.strDeliveryType = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_TYPE));
					info.bFromAddCustomer = cursor.getInt(cursor.getColumnIndex(KEY_FROM_ADD)) == 1;
					info.strSalesDistrict = cursor.getString(cursor.getColumnIndex(KEY_SALES_DISTRICT));
					info.fHandlingFee = cursor.getFloat(cursor.getColumnIndex(KEY_HANDLING_FEE));
					info.fMiniumOrderValue = cursor.getFloat(cursor.getColumnIndex(KEY_MINIMUM_ORDER_VALUE));
					info.nConSales = cursor.getInt(cursor.getColumnIndex(KEY_CON_SALES));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
					info.strInvoiceDueDate = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_DATE));
					info.strInvoiceDueMethod = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_METHOD));
					info.strPONumberRequired = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_REQUIRED));
					info.strPONumberMask = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_MASK));
					info.strLegalName = cursor.getString(cursor.getColumnIndex(KEY_LEGAL_NAME));
					info.strCreditLimit = cursor.getString(cursor.getColumnIndex(KEY_CREDIT_LIMIT));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public int getDeferredCustomerCount(boolean bIsRouteSch)
	{
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_CUSTOMER_INFO +
				" WHERE		" + KEY_DEFRRED_STATUS + " = 3 AND " + KEY_IS_ROUTE_SCH + " = " + (bIsRouteSch == true ? 1 : 0);
		Cursor cursor = mDb.rawQuery(sql, null);
		int nCount = 0;
		if(cursor != null)
		{
			nCount = cursor.getCount();
			try
			{
				cursor.close();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return nCount;
	}

	public List<CustomerInfo> getDeferredCustomerList(boolean bIsRouteSch) {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_CUSTOMER_INFO +
				" WHERE		" + KEY_DEFRRED_STATUS + " = 3 AND " + KEY_IS_ROUTE_SCH + " = " + (bIsRouteSch == true ? 1 : 0);
		Cursor cursor = mDb.rawQuery(sql, null);
		List<CustomerInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					CustomerInfo info = new CustomerInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strAltCustNo = cursor.getString(cursor.getColumnIndex(KEY_ALT_CUST_NO));
					info.strName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
					info.strCompanyTaxID = cursor.getString(cursor.getColumnIndex(KEY_COMPANY_TAX_ID));
					info.strCity = cursor.getString(cursor.getColumnIndex(KEY_CITY));
					info.strAddress1 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS1));
					info.strAddress2 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS2));
					info.strPhone = cursor.getString(cursor.getColumnIndex(KEY_PHONE));
					info.strZip = cursor.getString(cursor.getColumnIndex(KEY_ZIP));
					info.strBuyerName = cursor.getString(cursor.getColumnIndex(KEY_BUYER_NAME));
					info.strBuyerPhone = cursor.getString(cursor.getColumnIndex(KEY_BUYER_PHONE));
					info.strAccountName = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_NAME));
					info.strAccountPhone = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_PHONE));
					info.nLocation = cursor.getInt(cursor.getColumnIndex(KEY_LOCATION));
					info.nRouteNo = cursor.getInt(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
					info.strPriority = cursor.getString(cursor.getColumnIndex(KEY_PRIORITY));
					info.nPromoPlan = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_PLAN));
					info.nTemplateGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMPLATE_GRP));
					info.nTempGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMP_GRP));
					info.nFeatGrp = cursor.getInt(cursor.getColumnIndex(KEY_FEAT_GRP));
					info.nPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_GRP));
					info.nAltPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_ALT_PRICE_GRP));
					info.strTaxCode = cursor.getString(cursor.getColumnIndex(KEY_TAX_CODE));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.bIsRouteSch = cursor.getInt(cursor.getColumnIndex(KEY_IS_ROUTE_SCH)) == 1 ? true : false;
					info.latitude = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE));
					info.longitude = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE));
					info.bCompleted = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED)) == 1 ? true : false;
					info.strStartTime1 = cursor.getString(cursor.getColumnIndex(KEY_START_TIME1));
					info.strEndTime1 = cursor.getString(cursor.getColumnIndex(KEY_END_TIME1));
					info.strStartTime2 = cursor.getString(cursor.getColumnIndex(KEY_START_TIME2));
					info.strEndTime2 = cursor.getString(cursor.getColumnIndex(KEY_END_TIME2));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.nOrderNo = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_NO));
					info.strShipToState = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_STATE));
					info.strShipToZip = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_ZIP));
					info.nInvoiceFmt = cursor.getInt(cursor.getColumnIndex(KEY_INVOICE_FMT));
					info.nCustPriceMaintFlag = cursor.getInt(cursor.getColumnIndex(KEY_CUST_PRICE_MAINT_FLAG));
					info.nShowPrice = cursor.getInt(cursor.getColumnIndex(KEY_SHOW_PRICE));
					info.nDelayedPrint = cursor.getInt(cursor.getColumnIndex(KEY_DELAYED_FLAG));
					info.strPayType = cursor.getString(cursor.getColumnIndex(KEY_PAY_TYPE));
					info.strBillName = cursor.getString(cursor.getColumnIndex(KEY_BILL_NAME));
					info.strBillAddr1 = cursor.getString(cursor.getColumnIndex(KEY_BILL_ADDR1));
					info.strBillAddr2 = cursor.getString(cursor.getColumnIndex(KEY_BILL_ADDR2));
					info.strBillCity = cursor.getString(cursor.getColumnIndex(KEY_BILL_CITY));
					info.strBillPeriod = cursor.getString(cursor.getColumnIndex(KEY_BILL_PERIOD));
					info.strVendorNo = cursor.getString(cursor.getColumnIndex(KEY_VENDOR_NO));
					info.nBillCustNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CUST_NO));
					info.nBillChainNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CHAIN_NO));
					info.strState = cursor.getString(cursor.getColumnIndex(KEY_STATE));
					info.strSignature = cursor.getString(cursor.getColumnIndex(KEY_SIGNATURE));
					info.strSalEntryMode = cursor.getString(cursor.getColumnIndex(KEY_SALENTRY_MODE));
					info.strRtnEntryMode = cursor.getString(cursor.getColumnIndex(KEY_RTNENTRY_MODE));
					info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
					info.strTerms = cursor.getString(cursor.getColumnIndex(KEY_TERMS));
					info.nImageCapture = cursor.getInt(cursor.getColumnIndex(KEY_IMAGE_CAPTURE));
					info.strDexVersion = cursor.getString(cursor.getColumnIndex(KEY_DEX_VERSION));
					info.nDeferredStatus = cursor.getInt(cursor.getColumnIndex(KEY_DEFRRED_STATUS));
					info.nCompleteStatus = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED_STATUS));
					info.nOrgSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_ORG_SEQ_NO));
					info.strArrivalTime = cursor.getString(cursor.getColumnIndex(KEY_ARRIVAL_TIME));
					info.strAuthGrp = cursor.getString(cursor.getColumnIndex(KEY_AUTH_GRP));
					info.driverLatitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LATITUDE));
					info.driverLongitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LONGITUDE));
					info.strDeliveryWindowTo = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_TO));
					info.strDeliveryWindowFrom = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_FROM));
					info.nCreditHold = cursor.getInt(cursor.getColumnIndex(KEY_CREDIT_HOLD));
					info.nDeliveryFreq = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_FREQ));
					info.strStoreNo = cursor.getString(cursor.getColumnIndex(KEY_STORE_NO));
					info.strDeliveryType = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_TYPE));
					info.bFromAddCustomer = cursor.getInt(cursor.getColumnIndex(KEY_FROM_ADD)) == 1;
					info.strSalesDistrict = cursor.getString(cursor.getColumnIndex(KEY_SALES_DISTRICT));
					info.fHandlingFee = cursor.getFloat(cursor.getColumnIndex(KEY_HANDLING_FEE));
					info.fMiniumOrderValue = cursor.getFloat(cursor.getColumnIndex(KEY_MINIMUM_ORDER_VALUE));
					info.nConSales = cursor.getInt(cursor.getColumnIndex(KEY_CON_SALES));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
					info.strInvoiceDueDate = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_DATE));
					info.strInvoiceDueMethod = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_METHOD));
					info.strPONumberRequired = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_REQUIRED));
					info.strPONumberMask = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_MASK));
					info.strLegalName = cursor.getString(cursor.getColumnIndex(KEY_LEGAL_NAME));
					info.strCreditLimit = cursor.getString(cursor.getColumnIndex(KEY_CREDIT_LIMIT));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<CustomerInfo> getLoadedCustomerList(boolean bIsRouteSch) {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_CUSTOMER_INFO +
				" WHERE		" + KEY_DEFRRED_STATUS + " = 1 AND " + KEY_IS_ROUTE_SCH + " = " + (bIsRouteSch == true ? 1 : 0);
		Cursor cursor = mDb.rawQuery(sql, null);
		List<CustomerInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					CustomerInfo info = new CustomerInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strAltCustNo = cursor.getString(cursor.getColumnIndex(KEY_ALT_CUST_NO));
					info.strName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
					info.strCompanyTaxID = cursor.getString(cursor.getColumnIndex(KEY_COMPANY_TAX_ID));
					info.strCity = cursor.getString(cursor.getColumnIndex(KEY_CITY));
					info.strAddress1 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS1));
					info.strAddress2 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS2));
					info.strPhone = cursor.getString(cursor.getColumnIndex(KEY_PHONE));
					info.strZip = cursor.getString(cursor.getColumnIndex(KEY_ZIP));
					info.strBuyerName = cursor.getString(cursor.getColumnIndex(KEY_BUYER_NAME));
					info.strBuyerPhone = cursor.getString(cursor.getColumnIndex(KEY_BUYER_PHONE));
					info.strAccountName = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_NAME));
					info.strAccountPhone = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_PHONE));
					info.nLocation = cursor.getInt(cursor.getColumnIndex(KEY_LOCATION));
					info.nRouteNo = cursor.getInt(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
					info.strPriority = cursor.getString(cursor.getColumnIndex(KEY_PRIORITY));
					info.nPromoPlan = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_PLAN));
					info.nTemplateGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMPLATE_GRP));
					info.nTempGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMP_GRP));
					info.nFeatGrp = cursor.getInt(cursor.getColumnIndex(KEY_FEAT_GRP));
					info.nPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_GRP));
					info.nAltPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_ALT_PRICE_GRP));
					info.strTaxCode = cursor.getString(cursor.getColumnIndex(KEY_TAX_CODE));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.bIsRouteSch = cursor.getInt(cursor.getColumnIndex(KEY_IS_ROUTE_SCH)) == 1 ? true : false;
					info.latitude = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE));
					info.longitude = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE));
					info.bCompleted = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED)) == 1 ? true : false;
					info.strStartTime1 = cursor.getString(cursor.getColumnIndex(KEY_START_TIME1));
					info.strEndTime1 = cursor.getString(cursor.getColumnIndex(KEY_END_TIME1));
					info.strStartTime2 = cursor.getString(cursor.getColumnIndex(KEY_START_TIME2));
					info.strEndTime2 = cursor.getString(cursor.getColumnIndex(KEY_END_TIME2));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.nOrderNo = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_NO));
					info.strShipToState = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_STATE));
					info.strShipToZip = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_ZIP));
					info.nInvoiceFmt = cursor.getInt(cursor.getColumnIndex(KEY_INVOICE_FMT));
					info.nCustPriceMaintFlag = cursor.getInt(cursor.getColumnIndex(KEY_CUST_PRICE_MAINT_FLAG));
					info.nShowPrice = cursor.getInt(cursor.getColumnIndex(KEY_SHOW_PRICE));
					info.nDelayedPrint = cursor.getInt(cursor.getColumnIndex(KEY_DELAYED_FLAG));
					info.strPayType = cursor.getString(cursor.getColumnIndex(KEY_PAY_TYPE));
					info.strBillName = cursor.getString(cursor.getColumnIndex(KEY_BILL_NAME));
					info.strBillAddr1 = cursor.getString(cursor.getColumnIndex(KEY_BILL_ADDR1));
					info.strBillAddr2 = cursor.getString(cursor.getColumnIndex(KEY_BILL_ADDR2));
					info.strBillCity = cursor.getString(cursor.getColumnIndex(KEY_BILL_CITY));
					info.strBillPeriod = cursor.getString(cursor.getColumnIndex(KEY_BILL_PERIOD));
					info.strVendorNo = cursor.getString(cursor.getColumnIndex(KEY_VENDOR_NO));
					info.nBillCustNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CUST_NO));
					info.nBillChainNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CHAIN_NO));
					info.strState = cursor.getString(cursor.getColumnIndex(KEY_STATE));
					info.strSignature = cursor.getString(cursor.getColumnIndex(KEY_SIGNATURE));
					info.strSalEntryMode = cursor.getString(cursor.getColumnIndex(KEY_SALENTRY_MODE));
					info.strRtnEntryMode = cursor.getString(cursor.getColumnIndex(KEY_RTNENTRY_MODE));
					info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
					info.strTerms = cursor.getString(cursor.getColumnIndex(KEY_TERMS));
					info.nImageCapture = cursor.getInt(cursor.getColumnIndex(KEY_IMAGE_CAPTURE));
					info.strDexVersion = cursor.getString(cursor.getColumnIndex(KEY_DEX_VERSION));
					info.nDeferredStatus = cursor.getInt(cursor.getColumnIndex(KEY_DEFRRED_STATUS));
					info.nCompleteStatus = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED_STATUS));
					info.nOrgSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_ORG_SEQ_NO));
					info.strArrivalTime = cursor.getString(cursor.getColumnIndex(KEY_ARRIVAL_TIME));
					info.strAuthGrp = cursor.getString(cursor.getColumnIndex(KEY_AUTH_GRP));
					info.driverLatitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LATITUDE));
					info.driverLongitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LONGITUDE));
					info.strDeliveryWindowTo = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_TO));
					info.strDeliveryWindowFrom = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_FROM));
					info.nCreditHold = cursor.getInt(cursor.getColumnIndex(KEY_CREDIT_HOLD));
					info.nDeliveryFreq = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_FREQ));
					info.strStoreNo = cursor.getString(cursor.getColumnIndex(KEY_STORE_NO));
					info.strDeliveryType = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_TYPE));
					info.bFromAddCustomer = cursor.getInt(cursor.getColumnIndex(KEY_FROM_ADD)) == 1;
					info.strSalesDistrict = cursor.getString(cursor.getColumnIndex(KEY_SALES_DISTRICT));
					info.fHandlingFee = cursor.getFloat(cursor.getColumnIndex(KEY_HANDLING_FEE));
					info.fMiniumOrderValue = cursor.getFloat(cursor.getColumnIndex(KEY_MINIMUM_ORDER_VALUE));
					info.nConSales = cursor.getInt(cursor.getColumnIndex(KEY_CON_SALES));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
					info.strInvoiceDueDate = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_DATE));
					info.strInvoiceDueMethod = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_METHOD));
					info.strPONumberRequired = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_REQUIRED));
					info.strPONumberMask = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_MASK));
					info.strLegalName = cursor.getString(cursor.getColumnIndex(KEY_LEGAL_NAME));
					info.strCreditLimit = cursor.getString(cursor.getColumnIndex(KEY_CREDIT_LIMIT));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public long insertCustomerInfo(CustomerInfo info) {

		ContentValues values = new ContentValues();
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_ALT_CUST_NO, info.strAltCustNo);
		values.put(KEY_NAME, info.strName);
		values.put(KEY_COMPANY_TAX_ID, info.strCompanyTaxID);
		values.put(KEY_CITY, info.strCity);
		values.put(KEY_ADDRESS1, info.strAddress1);
		values.put(KEY_ADDRESS2, info.strAddress2);
		values.put(KEY_PHONE, info.strPhone);
		values.put(KEY_BUYER_NAME, info.strBuyerName);
		values.put(KEY_BUYER_PHONE, info.strBuyerPhone);
		values.put(KEY_ACCOUNT_NAME, info.strAccountName);
		values.put(KEY_ACCOUNT_PHONE, info.strAccountPhone);
		values.put(KEY_ZIP, info.strZip);
		values.put(KEY_LOCATION, info.nLocation);
		values.put(KEY_ROUTE_NUMBER, info.nRouteNo);
		values.put(KEY_PRIORITY, info.strPriority);
		values.put(KEY_START_TIME1, info.strStartTime1);
		values.put(KEY_END_TIME1, info.strEndTime1);
		values.put(KEY_START_TIME2, info.strStartTime2);
		values.put(KEY_END_TIME2, info.strEndTime2);
		values.put(KEY_DELIVERY_DATE, info.strDeliveryDate);
		values.put(KEY_PROMO_PLAN, info.nPromoPlan);
		values.put(KEY_TEMPLATE_GRP, info.nTemplateGrp);
		values.put(KEY_TEMP_GRP, info.nTempGrp);
		values.put(KEY_FEAT_GRP, info.nFeatGrp);
		values.put(KEY_PRICE_GRP, info.nPriceGrp);
		values.put(KEY_ALT_PRICE_GRP, info.nAltPriceGrp);
		values.put(KEY_TAX_CODE, info.strTaxCode);
		values.put(KEY_DAY_NO, info.nDayNo);
		values.put(KEY_SEQ_NO, info.nSeqNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_LATITUDE, info.latitude);
		values.put(KEY_LONGITUDE, info.longitude);
		values.put(KEY_IS_ROUTE_SCH, info.bIsRouteSch == true ? 1 : 0);
		values.put(KEY_COMPLETED, info.bCompleted == true ? 1 : 0);
		values.put(KEY_PERIOD_NO, info.nPeriodNo);
		values.put(KEY_ORDER_NO, info.nOrderNo);
		values.put(KEY_SHIP_TO_STATE, info.strShipToState);
		values.put(KEY_SHIP_TO_ZIP, info.strShipToZip);
		values.put(KEY_INVOICE_FMT, info.nInvoiceFmt);
		values.put(KEY_CUST_PRICE_MAINT_FLAG, info.nCustPriceMaintFlag);
		values.put(KEY_SHOW_PRICE, info.nShowPrice);
		values.put(KEY_DELAYED_FLAG, info.nDelayedPrint);
		values.put(KEY_PAY_TYPE, info.strPayType);
		values.put(KEY_BILL_NAME, info.strBillName);
		values.put(KEY_BILL_ADDR1, info.strBillAddr1);
		values.put(KEY_BILL_ADDR2, info.strBillAddr2);
		values.put(KEY_BILL_CITY, info.strBillCity);
		values.put(KEY_BILL_CHAIN_NO, info.nBillChainNo);
		values.put(KEY_BILL_CUST_NO, info.nBillCustNo);
		values.put(KEY_BILL_PERIOD, info.strBillPeriod);
		values.put(KEY_VENDOR_NO, info.strVendorNo);
		values.put(KEY_STATE, info.strState);
		values.put(KEY_SIGNATURE, info.strSignature);
		values.put(KEY_TERMS, info.strTerms);
		values.put(KEY_SALENTRY_MODE, info.strSalEntryMode);
		values.put(KEY_RTNENTRY_MODE, info.strRtnEntryMode);
		values.put(KEY_DUMPS_ALLOWED, info.strDumpsAllowed);
		values.put(KEY_IMAGE_CAPTURE, info.nImageCapture);
		values.put(KEY_DEX_VERSION, info.strDexVersion);
		values.put(KEY_COMPLETED_STATUS, info.nCompleteStatus);
		values.put(KEY_DEFRRED_STATUS, info.nDeferredStatus);
		values.put(KEY_ORG_SEQ_NO, info.nOrgSeqNo);
		values.put(KEY_ARRIVAL_TIME, info.strArrivalTime);
		values.put(KEY_AUTH_GRP, info.strAuthGrp);
		values.put(KEY_DRIVER_LATITUDE, info.driverLatitude);
		values.put(KEY_DRIVER_LONGITUDE, info.driverLongitude);
		values.put(KEY_DELIVERY_WINDOW_FROM, info.strDeliveryWindowFrom);
		values.put(KEY_DELIVERY_WINDOW_TO, info.strDeliveryWindowTo);
		values.put(KEY_CREDIT_HOLD, info.nCreditHold);
		values.put(KEY_DELIVERY_FREQ, info.nDeliveryFreq);
		values.put(KEY_STORE_NO, info.strStoreNo);
		values.put(KEY_DELIVERY_TYPE, info.strDeliveryType);
		values.put(KEY_FROM_ADD, info.bFromAddCustomer ? 1 : 0);
		values.put(KEY_SALES_DISTRICT, info.strSalesDistrict);
		values.put(KEY_HANDLING_FEE, info.fHandlingFee);
		values.put(KEY_MINIMUM_ORDER_VALUE, info.fMiniumOrderValue);
		values.put(KEY_CON_SALES, info.nConSales);
		values.put(KEY_TYPE, info.strType);
		values.put(KEY_USER1, info.strUser1);
		values.put(KEY_INVOICE_DUE_DATE, info.strInvoiceDueDate);
		values.put(KEY_INVOICE_DUE_METHOD, info.strInvoiceDueMethod);
		values.put(KEY_PO_NUMBER_REQUIRED, info.strPONumberRequired);
		values.put(KEY_PO_NUMBER_MASK, info.strPONumberMask);
		values.put(KEY_LEGAL_NAME, info.strLegalName);
		values.put(KEY_CREDIT_LIMIT, info.strCreditLimit);
		return mDb.insert(TABLE_CUSTOMER_INFO, null, values);
	}

	public void resetSequNoForCustomer(CustomerInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_SEQ_NO, 0);
		values.put(KEY_PERIOD_NO, 0);
		values.put(KEY_DELIVERY_DATE, "");
		if (info.nIdx != 0) {
			mDb.update(TABLE_CUSTOMER_INFO, values, KEY_IDX + " = " + info.nIdx, null);
		}
		else
		{
			mDb.update(TABLE_CUSTOMER_INFO, values, KEY_CUST_NO + " = " + info.nCustNo + " AND " + KEY_CHAIN_NO + " = " + info.nChainNo, null);
		}
	}

	public long updateCustomerCompleted(CustomerInfo info){
		ContentValues values = new ContentValues();
		values.put(KEY_COMPLETED, info.bCompleted == true ? 1 : 0);
		if (info.nIdx != 0) {
			return mDb.update(TABLE_CUSTOMER_INFO, values, KEY_IDX + " = " + info.nIdx, null);
		}

		return mDb.update(TABLE_CUSTOMER_INFO, values, KEY_CUST_NO + " = " + info.nCustNo + " AND " + KEY_CHAIN_NO + " = " + info.nChainNo, null);
	}

	public long updateCustomerInfo(CustomerInfo info) {
		ContentValues values = new ContentValues();
		if (info.nCustNo != 0) {
			values.put(KEY_CUST_NO, info.nCustNo);
		}
		if( !info.strAltCustNo.equals(""))
		{
			values.put(KEY_ALT_CUST_NO, info.strAltCustNo);
		}
		if (info.strName.length() > 0) {
			values.put(KEY_NAME, info.strName);
		}
		if (info.strCompanyTaxID.length() > 0) {
			values.put(KEY_COMPANY_TAX_ID, info.strCompanyTaxID);
		}
		if (info.strCity.length() > 0) {
			values.put(KEY_CITY, info.strCity);
		}
		if (info.strAddress1.length() > 0) {
			values.put(KEY_ADDRESS1, info.strAddress1);
		}
		if (info.strAddress2.length() > 0) {
			values.put(KEY_ADDRESS2, info.strAddress2);
		}
		if (info.strPhone.length() > 0) {
			values.put(KEY_PHONE, info.strPhone);
		}
		if (info.strZip.length() > 0) {
			values.put(KEY_ZIP, info.strZip);
		}
		if (info.strBuyerName.length() > 0) {
			values.put(KEY_BUYER_NAME, info.strBuyerName);
		}
		if (info.strBuyerPhone.length() > 0) {
			values.put(KEY_BUYER_PHONE, info.strBuyerPhone);
		}
		if (info.strAccountName.length() > 0) {
			values.put(KEY_ACCOUNT_NAME, info.strAccountName);
		}
		if (info.strAccountPhone.length() > 0) {
			values.put(KEY_ACCOUNT_PHONE, info.strAccountPhone);
		}
		if (info.nLocation != 0) {
			values.put(KEY_LOCATION, info.nLocation);
		}
		if (info.nRouteNo != 0) {
			values.put(KEY_ROUTE_NUMBER, info.nRouteNo);
		}
		if (info.strPriority.length() > 0) {
			values.put(KEY_PRIORITY, info.strPriority);
		}
		if (info.strStartTime1.length() > 0) {
			values.put(KEY_START_TIME1, info.strStartTime1);
		}
		if (info.strEndTime1.length() > 0) {
			values.put(KEY_END_TIME1, info.strEndTime1);
		}
		if (info.strStartTime2.length() > 0) {
			values.put(KEY_START_TIME2, info.strStartTime2);
		}
		if (info.strEndTime2.length() > 0) {
			values.put(KEY_END_TIME2, info.strEndTime2);
		}
		if (info.nPromoPlan != 0) {
			values.put(KEY_PROMO_PLAN, info.nPromoPlan);
		}
		if (info.nTemplateGrp != 0) {
			values.put(KEY_TEMPLATE_GRP, info.nTemplateGrp);
		}
		if (info.nTempGrp != 0) {
			values.put(KEY_TEMP_GRP, info.nTempGrp);
		}
		if (info.nFeatGrp != 0) {
			values.put(KEY_FEAT_GRP, info.nFeatGrp);
		}
		if (info.nPriceGrp != 0) {
			values.put(KEY_PRICE_GRP, info.nPriceGrp);
		}
		if (info.nAltPriceGrp != 0) {
			values.put(KEY_ALT_PRICE_GRP, info.nAltPriceGrp);
		}
		if (info.strTaxCode.length() > 0) {
			values.put(KEY_TAX_CODE, info.strTaxCode);
		}
		if (info.strShipToState.length() > 0) {
			values.put(KEY_SHIP_TO_STATE, info.strShipToState);
		}
		if (info.strShipToZip.length() > 0) {
			values.put(KEY_SHIP_TO_ZIP, info.strShipToZip);
		}
		if (info.nDayNo != 0) {
			values.put(KEY_DAY_NO, info.nDayNo);
		}
		if (info.nSeqNo != 0) {
			values.put(KEY_SEQ_NO, info.nSeqNo);
		}
		if (info.nChainNo != 0) {
			values.put(KEY_CHAIN_NO, info.nChainNo);
		}
		if (info.latitude != 0) {
			values.put(KEY_LATITUDE, info.latitude);
		}
		if (info.longitude != 0) {
			values.put(KEY_LONGITUDE, info.longitude);
		}
		if (info.bIsRouteSch == true) {
			values.put(KEY_IS_ROUTE_SCH, info.bIsRouteSch == true ? 1 : 0);
		}
		if (info.bFromAddCustomer == true) {
			values.put(KEY_FROM_ADD, info.bFromAddCustomer ? 1 : 0);
		}
		if (info.bCompleted == true) {
			values.put(KEY_COMPLETED, info.bCompleted == true ? 1 : 0);
		}
		if (info.nInvoiceFmt != 0) {
			values.put(KEY_INVOICE_FMT, info.nInvoiceFmt);
		}
		if (info.nCustPriceMaintFlag != 0) {
			values.put(KEY_CUST_PRICE_MAINT_FLAG, info.nCustPriceMaintFlag);
		}
		if (info.nShowPrice != 0) {
			values.put(KEY_SHOW_PRICE, info.nShowPrice);
		}
		if (info.nDelayedPrint != 0) {
			values.put(KEY_DELAYED_FLAG, info.nDelayedPrint);
		}
		if (info.strPayType.length() != 0) {
			values.put(KEY_PAY_TYPE, info.strPayType);
		}
		if (info.strBillName.length() > 0) {
			values.put(KEY_BILL_NAME, info.strBillName);
		}
		if (info.strBillAddr1.length() > 0) {
			values.put(KEY_BILL_ADDR1, info.strBillAddr1);
		}
		if (info.strBillAddr2.length() > 0) {
			values.put(KEY_BILL_ADDR2, info.strBillAddr2);
		}
		if (info.strBillCity.length() > 0) {
			values.put(KEY_BILL_CITY, info.strBillCity);
		}
		if(info.nBillChainNo != 0) {
			values.put(KEY_BILL_CHAIN_NO, info.nBillChainNo);
		}
		if(info.nBillCustNo != 0) {
			values.put(KEY_BILL_CUST_NO, info.nBillCustNo);
		}
		if(info.strBillPeriod.length() > 0) {
			values.put(KEY_BILL_PERIOD, info.strBillPeriod);
		}
		if(info.strVendorNo.length() > 0) {
			values.put(KEY_VENDOR_NO, info.strVendorNo);
		}
		if (info.strState.length() > 0) {
			values.put(KEY_STATE, info.strState);
		}
		if (info.strSignature.length() > 0 ){
			values.put(KEY_SIGNATURE, info.strSignature);
		}
		if(info.strTerms.length() > 0 ){
			values.put(KEY_TERMS, info.strTerms);
		}
		if(info.strSalEntryMode.length() > 0 ){
			values.put(KEY_SALENTRY_MODE, info.strSalEntryMode);
		}
		if(info.strRtnEntryMode.length() > 0 ){
			values.put(KEY_RTNENTRY_MODE, info.strRtnEntryMode);
		}
		if(info.strDumpsAllowed.length() > 0 ){
			values.put(KEY_DUMPS_ALLOWED, info.strDumpsAllowed);
		}
		if(info.nOrderNo != -1) {
			values.put(KEY_ORDER_NO, info.nOrderNo);
		}
		if(info.nOrgSeqNo != 0 ){
			values.put(KEY_ORG_SEQ_NO, info.nOrgSeqNo);
		}

		if(info.nImageCapture != 0 ){
			values.put(KEY_IMAGE_CAPTURE, info.nImageCapture);
		}
		if(info.strDexVersion.length() > 0){
			values.put(KEY_DEX_VERSION, info.strDexVersion);
		}
		if(info.strArrivalTime.length() > 0 ) {
			values.put(KEY_ARRIVAL_TIME, info.strArrivalTime);
		}
		if(info.strAuthGrp.length() > 0 ) {
			values.put(KEY_AUTH_GRP, info.strAuthGrp);
		}
		if(info.driverLatitude != 0)
		{
			values.put(KEY_DRIVER_LATITUDE, info.driverLatitude);
		}
		if(info.driverLongitude != 0)
		{
			values.put(KEY_DRIVER_LONGITUDE, info.driverLongitude);
		}
		if(info.strDeliveryWindowFrom.length() > 0){
			values.put(KEY_DELIVERY_WINDOW_FROM, info.strDeliveryWindowFrom);
		}
		if(info.strDeliveryWindowTo.length() > 0){
			values.put(KEY_DELIVERY_WINDOW_TO, info.strDeliveryWindowTo);
		}
		if(info.nCreditHold > 0){
			values.put(KEY_CREDIT_HOLD, info.nCreditHold);
		}
		if(info.nDeliveryFreq > 0){
			values.put(KEY_DELIVERY_FREQ, info.nDeliveryFreq);
		}
		if(info.strStoreNo.length() > 0){
			values.put(KEY_STORE_NO, info.strStoreNo);
		}
		if(info.strDeliveryType.length() > 0){
			values.put(KEY_DELIVERY_TYPE, info.strDeliveryType);
		}
		if(info.nConSales != 0)
		{
			values.put(KEY_CON_SALES, info.nConSales);
		}
		values.put(KEY_COMPLETED_STATUS, info.nCompleteStatus);
		values.put(KEY_DEFRRED_STATUS, info.nDeferredStatus);
		if(info.strSalesDistrict.length() > 0)
		{
			values.put(KEY_SALES_DISTRICT, info.strSalesDistrict);
		}
		if(info.fHandlingFee != 0)
		{
			values.put(KEY_HANDLING_FEE, info.fHandlingFee);
		}
		if(info.fMiniumOrderValue != 0)
		{
			values.put(KEY_MINIMUM_ORDER_VALUE, info.fMiniumOrderValue);
		}
		if(info.strType.length() > 0)
		{
			values.put(KEY_TYPE, info.strType);
		}
		if(info.strUser1.length() > 0)
		{
			values.put(KEY_USER1, info.strUser1);
		}
		if(info.strInvoiceDueDate.length() > 0)
		{
			values.put(KEY_INVOICE_DUE_DATE, info.strInvoiceDueDate);
		}
		if(info.strInvoiceDueMethod.length() > 0)
		{
			values.put(KEY_INVOICE_DUE_METHOD, info.strInvoiceDueMethod);
		}
		if(info.strPONumberRequired.length() > 0)
		{
			values.put(KEY_PO_NUMBER_REQUIRED, info.strPONumberRequired);
		}
		if(info.strPONumberMask.length() > 0)
		{
			values.put(KEY_PO_NUMBER_MASK, info.strPONumberMask);
		}
		if(info.strLegalName.length() > 0)
		{
			values.put(KEY_LEGAL_NAME, info.strLegalName);
		}
		if(info.strCreditLimit.length() > 0)
		{
			values.put(KEY_CREDIT_LIMIT, info.strCreditLimit);
		}
		if (info.nIdx != 0) {
			return mDb.update(TABLE_CUSTOMER_INFO, values, KEY_IDX + " = " + info.nIdx, null);
		}

		return mDb.update(TABLE_CUSTOMER_INFO, values, KEY_CUST_NO + " = " + info.nCustNo + " AND " + KEY_CHAIN_NO + " = " + info.nChainNo, null);
	}

	public boolean deleteAllCustomerInfo() throws SQLException {
		return mDb.delete(TABLE_CUSTOMER_INFO, null, null) > 0;
	}

	// CustContactInfo
	public List<CustContactInfo> getCustContactList(int nChainNo, int nCustNo) {
		String sql = 	" SELECT	* " +
						" FROM		" + TABLE_CUSTCONTACT_INFO +
						" WHERE		" + KEY_CHAIN_NO + " = " + nChainNo + " AND " +
						"			" + KEY_CUST_NO + " = " + nCustNo +
						" ORDER BY	" + KEY_CONTACT_TYPE + " ASC ";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<CustContactInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					CustContactInfo info = new CustContactInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nContactType = cursor.getInt(cursor.getColumnIndex(KEY_CONTACT_TYPE));
					info.strContactTypeDesc = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_TYPE_DESC));
					info.strContactName = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_NAME));
					info.strContactPhoneNumber = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_PHONE_NUMBER));
					info.strContactEmailAddress = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_EMAIL_ADDRESS));
					info.strContactInfo = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_INFO));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public long insertCustContactInfo(CustContactInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_CONTACT_TYPE, info.nContactType);
		values.put(KEY_CONTACT_TYPE_DESC, info.strContactTypeDesc);
		values.put(KEY_CONTACT_NAME, info.strContactName);
		values.put(KEY_CONTACT_PHONE_NUMBER, info.strContactPhoneNumber);
		values.put(KEY_CONTACT_EMAIL_ADDRESS, info.strContactEmailAddress);
		values.put(KEY_CONTACT_INFO, info.strContactInfo);
		return mDb.insert(TABLE_CUSTCONTACT_INFO, null, values);
	}


	public long updateCustContactInfo(CustContactInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_CONTACT_TYPE, info.nContactType);
		values.put(KEY_CONTACT_TYPE_DESC, info.strContactTypeDesc);
		values.put(KEY_CONTACT_NAME, info.strContactName);
		values.put(KEY_CONTACT_PHONE_NUMBER, info.strContactPhoneNumber);
		values.put(KEY_CONTACT_EMAIL_ADDRESS, info.strContactEmailAddress);
		values.put(KEY_CONTACT_INFO, info.strContactInfo);
		return mDb.update(TABLE_CUSTCONTACT_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}

	public boolean deleteAllCustContactInfo() throws SQLException {
		return mDb.delete(TABLE_CUSTCONTACT_INFO, null, null) > 0;
	}

	public long updatePresoldOrHInfo(PresoldOrHInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_DAY_NO, info.nDayNo);
		values.put(KEY_DETAIL_FILE, info.strDetailFile);
		values.put(KEY_ORDER_NO, info.strOrderNo);
		values.put(KEY_TYPE, info.strType);
		values.put(KEY_POREF, info.strPORef);
		values.put(KEY_CREDREF, info.strCredRef);
		values.put(KEY_INSTRS, info.strInstrs);
		values.put(KEY_QTY, info.nQty);
		values.put(KEY_VISITED, info.nVisited);
		values.put(KEY_MODIFIABLE, info.strModifiable);
		values.put(KEY_DELIVERY_DATE, info.strDeliveryDate);
		values.put(KEY_PERIOD_NO, info.nPeriodNo);
		values.put(KEY_DELIVERY_SEQUENCE, info.nDeliverySequence);
		values.put(KEY_ORDER_BARCODE, info.strOrderBarCode);
		values.put(KEY_USER_1, info.strUser1);
		values.put(KEY_USER_2, info.strUser2);
		values.put(KEY_CUST_IDX, info.nCustIdx);
		return mDb.update(TABLE_PRESOLDH_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}
	// PresoldOrHInfo
	public List<PresoldOrHInfo> getPresoldOrHInfoList() {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_PRESOLDH_INFO + " WHERE " + KEY_TYPE + " != 'N' ";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<PresoldOrHInfo> infos = new ArrayList<>();
		PresoldOrHInfo info = null;
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					info = new PresoldOrHInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strOrderNo = cursor.getString(cursor.getColumnIndex(KEY_ORDER_NO));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strPORef = cursor.getString(cursor.getColumnIndex(KEY_POREF));
					info.strCredRef = cursor.getString(cursor.getColumnIndex(KEY_CREDREF));
					info.nVisited = cursor.getInt(cursor.getColumnIndex(KEY_VISITED));
					info.strInstrs = cursor.getString(cursor.getColumnIndex(KEY_INSTRS));
					info.nQty = cursor.getInt(cursor.getColumnIndex(KEY_QTY));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.nDeliverySequence = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_SEQUENCE));
					info.nDeliverySequence = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_SEQUENCE));
					info.strOrderBarCode = cursor.getString(cursor.getColumnIndex(KEY_ORDER_BARCODE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER_1));
					info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER_2));
					info.nCustIdx = cursor.getInt(cursor.getColumnIndex(KEY_CUST_IDX));
					info.strModifiable = cursor.getString(cursor.getColumnIndex(KEY_MODIFIABLE));
					cursor.moveToNext();
					infos.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	// PresoldOrHInfo
	public List<PresoldOrHInfo> getPresoldOrHInfo(int nChainNo, int nCustNo, int nPeriodNo, int nDeliverySequence, int nCustomerIdx) {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_PRESOLDH_INFO +
				" WHERE		" + KEY_CUST_IDX + " = " + nCustomerIdx + " AND " + KEY_CHAIN_NO + " = " + nChainNo + " AND " +
				KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_PERIOD_NO + " = " + nPeriodNo + " AND " +
				KEY_DELIVERY_SEQUENCE + " = " + nDeliverySequence + " AND (" +
				KEY_TYPE + " = 'P' OR " + KEY_TYPE + " = 'D' OR " + KEY_TYPE + "= 'N' OR " + KEY_TYPE + "= 'W' OR " +
				KEY_TYPE + " = '4' OR " + KEY_TYPE + " = 'V')";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<PresoldOrHInfo> infos = new ArrayList<>();
		PresoldOrHInfo info = null;
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					info = new PresoldOrHInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strOrderNo = cursor.getString(cursor.getColumnIndex(KEY_ORDER_NO));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strPORef = cursor.getString(cursor.getColumnIndex(KEY_POREF));
					info.strCredRef = cursor.getString(cursor.getColumnIndex(KEY_CREDREF));
					info.nVisited = cursor.getInt(cursor.getColumnIndex(KEY_VISITED));
					info.strInstrs = cursor.getString(cursor.getColumnIndex(KEY_INSTRS));
					info.nQty = cursor.getInt(cursor.getColumnIndex(KEY_QTY));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.nDeliverySequence = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_SEQUENCE));
					info.strOrderBarCode = cursor.getString(cursor.getColumnIndex(KEY_ORDER_BARCODE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER_1));
					info.nCustIdx = cursor.getInt(cursor.getColumnIndex(KEY_CUST_IDX));
					info.strModifiable = cursor.getString(cursor.getColumnIndex(KEY_MODIFIABLE));
					cursor.moveToNext();
					infos.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public PresoldOrHInfo getPresoldOrHInfoWithOrderNo(int nOrderNo) {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_PRESOLDH_INFO +
				" WHERE		" + KEY_ORDER_NO + " = '" + nOrderNo + "' AND (" +
				KEY_TYPE + " = 'P' OR " + KEY_TYPE + " = 'D' OR " + KEY_TYPE + "= 'N' OR " + KEY_TYPE + "= 'W' OR " +
				KEY_TYPE + " = '4' OR " + KEY_TYPE + " = 'V')";
		Cursor cursor = mDb.rawQuery(sql, null);
		PresoldOrHInfo info = null;
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info = new PresoldOrHInfo();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
				info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
				info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
				info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
				info.strOrderNo = cursor.getString(cursor.getColumnIndex(KEY_ORDER_NO));
				info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
				info.strPORef = cursor.getString(cursor.getColumnIndex(KEY_POREF));
				info.strCredRef = cursor.getString(cursor.getColumnIndex(KEY_CREDREF));
				info.nVisited = cursor.getInt(cursor.getColumnIndex(KEY_VISITED));
				info.strInstrs = cursor.getString(cursor.getColumnIndex(KEY_INSTRS));
				info.nQty = cursor.getInt(cursor.getColumnIndex(KEY_QTY));
				info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
				info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
				info.nDeliverySequence = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_SEQUENCE));
				info.strOrderBarCode = cursor.getString(cursor.getColumnIndex(KEY_ORDER_BARCODE));
				info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER_1));
				info.nCustIdx = cursor.getInt(cursor.getColumnIndex(KEY_CUST_IDX));
				info.strModifiable = cursor.getString(cursor.getColumnIndex(KEY_MODIFIABLE));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	public List<PresoldOrHInfo> getLoadRequestPresoldOrHInfoList(String routeNumber) {
		String sql =	" SELECT	* " + " FROM		" + TABLE_PRESOLDH_INFO + " WHERE		" + KEY_CHAIN_NO + " = 99999 AND " +
				KEY_CUST_NO + " = " + routeNumber + " AND " + KEY_TYPE + " = 'B'";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<PresoldOrHInfo> infos = new ArrayList<>();
		PresoldOrHInfo info = null;
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					info = new PresoldOrHInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strOrderNo = cursor.getString(cursor.getColumnIndex(KEY_ORDER_NO));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strPORef = cursor.getString(cursor.getColumnIndex(KEY_POREF));
					info.strCredRef = cursor.getString(cursor.getColumnIndex(KEY_CREDREF));
					info.nVisited = cursor.getInt(cursor.getColumnIndex(KEY_VISITED));
					info.strInstrs = cursor.getString(cursor.getColumnIndex(KEY_INSTRS));
					info.nQty = cursor.getInt(cursor.getColumnIndex(KEY_QTY));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.nDeliverySequence = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_SEQUENCE));
					info.strOrderBarCode = cursor.getString(cursor.getColumnIndex(KEY_ORDER_BARCODE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER_1));
					info.nCustIdx = cursor.getInt(cursor.getColumnIndex(KEY_CUST_IDX));
					info.strModifiable = cursor.getString(cursor.getColumnIndex(KEY_MODIFIABLE));
					cursor.moveToNext();
					infos.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public List<PresoldOrHInfo> getPresoldOrHInfo(int nChainNo, int nCustNo, int nPeriodNo, int nDeliverySequence, int nCustomerIdx, int nDayNo) {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_PRESOLDH_INFO +
				" WHERE		" + KEY_DAY_NO + " = " + nDayNo + " AND " + KEY_CUST_IDX + " = " + nCustomerIdx + " AND " + KEY_CHAIN_NO + " = " + nChainNo + " AND " +
				KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_PERIOD_NO + " = " + nPeriodNo + " AND " +
				KEY_DELIVERY_SEQUENCE + " = " + nDeliverySequence + " AND (" +
				KEY_TYPE + " = 'P' OR " + KEY_TYPE + " = 'D' OR " + KEY_TYPE + "= 'N' OR " + KEY_TYPE + "= 'W' OR " +
				KEY_TYPE + " = '4' OR " + KEY_TYPE + " = 'V')";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<PresoldOrHInfo> infos = new ArrayList<>();
		PresoldOrHInfo info = null;
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					info = new PresoldOrHInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strOrderNo = cursor.getString(cursor.getColumnIndex(KEY_ORDER_NO));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strPORef = cursor.getString(cursor.getColumnIndex(KEY_POREF));
					info.strCredRef = cursor.getString(cursor.getColumnIndex(KEY_CREDREF));
					info.nVisited = cursor.getInt(cursor.getColumnIndex(KEY_VISITED));
					info.strInstrs = cursor.getString(cursor.getColumnIndex(KEY_INSTRS));
					info.nQty = cursor.getInt(cursor.getColumnIndex(KEY_QTY));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.nDeliverySequence = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_SEQUENCE));
					info.strOrderBarCode = cursor.getString(cursor.getColumnIndex(KEY_ORDER_BARCODE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER_1));
					info.nCustIdx = cursor.getInt(cursor.getColumnIndex(KEY_CUST_IDX));
					info.strModifiable = cursor.getString(cursor.getColumnIndex(KEY_MODIFIABLE));
					cursor.moveToNext();
					infos.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public List<PresoldOrHInfo> getPresoldOrHInfo(int nChainNo, int nCustNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_PRESOLDH_INFO + 
						" WHERE		" + KEY_CHAIN_NO + " = " + nChainNo + " AND " +
										KEY_CUST_NO + " = " + nCustNo + " AND (" +
										KEY_TYPE + " = 'P' OR " + KEY_TYPE + " = 'D' OR " + KEY_TYPE + " = 'B' OR " + KEY_TYPE + "= 'C' OR " +
										KEY_TYPE + "= 'N' OR " + KEY_TYPE + "= 'W' OR " +
										KEY_TYPE + " = '4' OR " + KEY_TYPE + " = 'V')";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<PresoldOrHInfo> infos = new ArrayList<>();
		PresoldOrHInfo info = null;
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					info = new PresoldOrHInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strOrderNo = cursor.getString(cursor.getColumnIndex(KEY_ORDER_NO));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strPORef = cursor.getString(cursor.getColumnIndex(KEY_POREF));
					info.strCredRef = cursor.getString(cursor.getColumnIndex(KEY_CREDREF));
					info.nVisited = cursor.getInt(cursor.getColumnIndex(KEY_VISITED));
					info.strInstrs = cursor.getString(cursor.getColumnIndex(KEY_INSTRS));
					info.nQty = cursor.getInt(cursor.getColumnIndex(KEY_QTY));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.nDeliverySequence = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_SEQUENCE));
					info.nDeliverySequence = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_SEQUENCE));
					info.strOrderBarCode = cursor.getString(cursor.getColumnIndex(KEY_ORDER_BARCODE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER_1));
					info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER_2));
					info.nCustIdx = cursor.getInt(cursor.getColumnIndex(KEY_CUST_IDX));
					info.strModifiable = cursor.getString(cursor.getColumnIndex(KEY_MODIFIABLE));
					cursor.moveToNext();
					infos.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public List<PresoldOrHInfo> getPresoldOrHInfo(int nChainNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_PRESOLDH_INFO +
						" WHERE		" +	KEY_CHAIN_NO+ " = " + nChainNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<PresoldOrHInfo> infos = new ArrayList<>();
		PresoldOrHInfo info = null;
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					info = new PresoldOrHInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strOrderNo = cursor.getString(cursor.getColumnIndex(KEY_ORDER_NO));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strPORef = cursor.getString(cursor.getColumnIndex(KEY_POREF));
					info.strCredRef = cursor.getString(cursor.getColumnIndex(KEY_CREDREF));
					info.nVisited = cursor.getInt(cursor.getColumnIndex(KEY_VISITED));
					info.strInstrs = cursor.getString(cursor.getColumnIndex(KEY_INSTRS));
					info.nQty = cursor.getInt(cursor.getColumnIndex(KEY_QTY));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.nDeliverySequence = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_SEQUENCE));
					info.nDeliverySequence = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_SEQUENCE));
					info.strOrderBarCode = cursor.getString(cursor.getColumnIndex(KEY_ORDER_BARCODE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER_1));
					info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER_2));
					info.nCustIdx = cursor.getInt(cursor.getColumnIndex(KEY_CUST_IDX));
					info.strModifiable = cursor.getString(cursor.getColumnIndex(KEY_MODIFIABLE));
					cursor.moveToNext();
					infos.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	// PresoldOrHInfo
	public List<PresoldOrHInfo> getPresoldOrHInfo(int nChainNo, int nCustNo, int nPeriodNo, int nDeliverySequence) {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_PRESOLDH_INFO +
				" WHERE		" + KEY_CHAIN_NO + " = " + nChainNo + " AND " +
				KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_PERIOD_NO + " = " + nPeriodNo + " AND " +
				KEY_DELIVERY_SEQUENCE + " = " + nDeliverySequence + " AND (" +
				KEY_TYPE + " = 'P' OR " + KEY_TYPE + " = 'D' OR " + KEY_TYPE + " = '4' OR " + KEY_TYPE + " = 'N' OR " + KEY_TYPE + " = 'W' OR " +
				KEY_TYPE + " = 'V')";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<PresoldOrHInfo> infos = new ArrayList<>();
		PresoldOrHInfo info = null;
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					info = new PresoldOrHInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strOrderNo = cursor.getString(cursor.getColumnIndex(KEY_ORDER_NO));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strPORef = cursor.getString(cursor.getColumnIndex(KEY_POREF));
					info.strCredRef = cursor.getString(cursor.getColumnIndex(KEY_CREDREF));
					info.nVisited = cursor.getInt(cursor.getColumnIndex(KEY_VISITED));
					info.strInstrs = cursor.getString(cursor.getColumnIndex(KEY_INSTRS));
					info.nQty = cursor.getInt(cursor.getColumnIndex(KEY_QTY));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.nDeliverySequence = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_SEQUENCE));
					info.strOrderBarCode = cursor.getString(cursor.getColumnIndex(KEY_ORDER_BARCODE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER_1));
					info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER_2));
					info.nCustIdx = cursor.getInt(cursor.getColumnIndex(KEY_CUST_IDX));
					info.strModifiable = cursor.getString(cursor.getColumnIndex(KEY_MODIFIABLE));
					cursor.moveToNext();
					infos.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public List<PresoldOrHInfo> getPresoldOrHInfo(int nChainNo, int nCustNo, int nPeriodNo) {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_PRESOLDH_INFO +
				" WHERE		" + KEY_CHAIN_NO + " = " + nChainNo + " AND " +
				KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_PERIOD_NO + " = " + nPeriodNo + " AND (" +
				KEY_TYPE + " = 'P' OR " + KEY_TYPE + " = 'D' OR " + KEY_TYPE + " = 'B' OR " + KEY_TYPE + "= 'C' OR " + KEY_TYPE + " = 'N' OR " + KEY_TYPE + " = 'W' OR " +
				KEY_TYPE + " = '4' OR " + KEY_TYPE + " ='V')";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<PresoldOrHInfo> infos = new ArrayList<>();
		PresoldOrHInfo info = null;
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					info = new PresoldOrHInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strOrderNo = cursor.getString(cursor.getColumnIndex(KEY_ORDER_NO));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strPORef = cursor.getString(cursor.getColumnIndex(KEY_POREF));
					info.strCredRef = cursor.getString(cursor.getColumnIndex(KEY_CREDREF));
					info.nVisited = cursor.getInt(cursor.getColumnIndex(KEY_VISITED));
					info.strInstrs = cursor.getString(cursor.getColumnIndex(KEY_INSTRS));
					info.nQty = cursor.getInt(cursor.getColumnIndex(KEY_QTY));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.nDeliverySequence = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_SEQUENCE));
					info.strOrderBarCode = cursor.getString(cursor.getColumnIndex(KEY_ORDER_BARCODE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER_1));
					info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER_2));
					info.nCustIdx = cursor.getInt(cursor.getColumnIndex(KEY_CUST_IDX));
					info.strModifiable = cursor.getString(cursor.getColumnIndex(KEY_MODIFIABLE));
					cursor.moveToNext();
					infos.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public List<PresoldOrHInfo> getAllPresoldOrHInfoList() {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_PRESOLDH_INFO;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<PresoldOrHInfo> infos = new ArrayList<>();
		PresoldOrHInfo info = null;
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					info = new PresoldOrHInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strOrderNo = cursor.getString(cursor.getColumnIndex(KEY_ORDER_NO));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strPORef = cursor.getString(cursor.getColumnIndex(KEY_POREF));
					info.strCredRef = cursor.getString(cursor.getColumnIndex(KEY_CREDREF));
					info.nVisited = cursor.getInt(cursor.getColumnIndex(KEY_VISITED));
					info.strInstrs = cursor.getString(cursor.getColumnIndex(KEY_INSTRS));
					info.nQty = cursor.getInt(cursor.getColumnIndex(KEY_QTY));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.nDeliverySequence = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_SEQUENCE));
					info.strOrderBarCode = cursor.getString(cursor.getColumnIndex(KEY_ORDER_BARCODE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER_1));
					info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER_2));
					info.nCustIdx = cursor.getInt(cursor.getColumnIndex(KEY_CUST_IDX));
					info.strModifiable = cursor.getString(cursor.getColumnIndex(KEY_MODIFIABLE));
					cursor.moveToNext();
					infos.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public long insertPresoldOrHInfo(PresoldOrHInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_DAY_NO, info.nDayNo);
		values.put(KEY_DETAIL_FILE, info.strDetailFile);
		values.put(KEY_ORDER_NO, info.strOrderNo);
		values.put(KEY_TYPE, info.strType);
		values.put(KEY_POREF, info.strPORef);
		values.put(KEY_CREDREF, info.strCredRef);
		values.put(KEY_INSTRS, info.strInstrs);
		values.put(KEY_QTY, info.nQty);
		values.put(KEY_VISITED, info.nVisited);
		values.put(KEY_MODIFIABLE, info.strModifiable);
		values.put(KEY_DELIVERY_DATE, info.strDeliveryDate);
		values.put(KEY_PERIOD_NO, info.nPeriodNo);
		values.put(KEY_DELIVERY_SEQUENCE, info.nDeliverySequence);
		values.put(KEY_ORDER_BARCODE, info.strOrderBarCode);
		values.put(KEY_USER_1, info.strUser1);
		values.put(KEY_USER_2, info.strUser2);
		values.put(KEY_CUST_IDX, info.nCustIdx);

		return mDb.insert(TABLE_PRESOLDH_INFO, null, values);
	}

	public boolean deleteAllPresoldOrHInfo() throws SQLException {
		return mDb.delete(TABLE_PRESOLDH_INFO, null, null) > 0;
	}

	// PresoldOrDInfo
	public List<PresoldOrDInfo> getPresoldOrDListWithShortDesc(String strDetailFile) {
		Cursor cursor = null;
		List<PresoldOrDInfo> list = new ArrayList<>();
		try {
		String sql = " SELECT	A.*, B." + KEY_SHORT_DESC + ", B." + KEY_DESCRIPTION +
						" FROM		" + TABLE_PRESOLDD_INFO + " A, " + TABLE_PRODDETL_INFO + " B" +
						" WHERE		A." + KEY_DETAIL_FILE + " = '" + strDetailFile + "' AND A." + KEY_ITEM_NO + " = " +
						"B." + KEY_ITEM_NO;
		cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++) {
					PresoldOrDInfo info = new PresoldOrDInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.nQtyOption = cursor.getInt(cursor.getColumnIndex(KEY_QTY_OPTION));
					info.nOrderQty = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_QTY));
					info.dOrderQty = cursor.getDouble(cursor.getColumnIndex(KEY_DORDER_QTY));
					info.nSuggestedQty = cursor.getInt(cursor.getColumnIndex(KEY_SUGGEST_QTY));
					info.nRequestedQty = cursor.getInt(cursor.getColumnIndex(KEY_REQUESTED_QTY));
					info.strPickLocationCode = cursor.getString(cursor.getColumnIndex(KEY_PICK_LOCATION_CODE));
					info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
					info.strLongDesc = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.nPrice = cursor.getInt(cursor.getColumnIndex(KEY_PRICE));
					info.strTrxnType = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TYPE));
					info.nReasonCode = cursor.getInt(cursor.getColumnIndex(KEY_REASON_CODE));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}
		
	// PresoldOrDInfo
	public List<PresoldOrDInfo> getPresoldOrDList(String strDetailFile) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_PRESOLDD_INFO +
						" WHERE		" + KEY_DETAIL_FILE + " = '" + strDetailFile + "' ";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<PresoldOrDInfo> list = new ArrayList<>();
		List<PresoldOrDInfo> free_item_list = new ArrayList<>();

		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++) {
					PresoldOrDInfo info = new PresoldOrDInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.nQtyOption = cursor.getInt(cursor.getColumnIndex(KEY_QTY_OPTION));
					info.nSuggestedQty = cursor.getInt(cursor.getColumnIndex(KEY_SUGGEST_QTY));
					info.nRequestedQty = cursor.getInt(cursor.getColumnIndex(KEY_REQUESTED_QTY));
					info.nOrderQty = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_QTY));
					info.dOrderQty = cursor.getDouble(cursor.getColumnIndex(KEY_DORDER_QTY));
					info.nPrice = cursor.getInt(cursor.getColumnIndex(KEY_PRICE));
					info.strPickLocationCode = cursor.getString(cursor.getColumnIndex(KEY_PICK_LOCATION_CODE));
					info.strTrxnType = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TYPE));
					info.nReasonCode = cursor.getInt(cursor.getColumnIndex(KEY_REASON_CODE));
					cursor.moveToNext();

					if(!info.strTrxnType.equals("12") && !info.strTrxnType.equals("11"))
					{
						boolean bAdd = true;
						for(PresoldOrDInfo presoldOrDInfo : list)
						{
							if( presoldOrDInfo.strItemNo.equals(info.strItemNo) )
							{
								presoldOrDInfo.nSuggestedQty += info.nSuggestedQty;
								presoldOrDInfo.nRequestedQty += info.nRequestedQty;
								presoldOrDInfo.nOrderQty += info.nOrderQty;
								bAdd = false;
							}
						}
						if( bAdd )
						{
							list.add(info);
						}
					}
					else
					{
						boolean bAdd = true;
						for(PresoldOrDInfo presoldOrDInfo : free_item_list)
						{
							if( presoldOrDInfo.strItemNo.equals(info.strItemNo))
							{
								presoldOrDInfo.nSuggestedQty += info.nSuggestedQty;
								presoldOrDInfo.nRequestedQty += info.nRequestedQty;
								presoldOrDInfo.nOrderQty += info.nOrderQty;
								bAdd = false;
							}
						}
						if( bAdd )
						{
							free_item_list.add(info);
						}
					}

				}
			}

			for(PresoldOrDInfo freeInfo : free_item_list)
			{
				for(PresoldOrDInfo presoldOrDInfo : list)
				{
					if( presoldOrDInfo.strItemNo.equals(freeInfo.strItemNo) && presoldOrDInfo.strTrxnType.equals("4"))
					{
						presoldOrDInfo.nSuggestedQty += freeInfo.nSuggestedQty;
						presoldOrDInfo.nRequestedQty += freeInfo.nRequestedQty;
						presoldOrDInfo.nOrderQty += freeInfo.nOrderQty;
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<PresoldOrDInfo> getUnusedPresoldOrDInfo(String strItemNo) {
		String sql =	" SELECT	A.* " +
						" FROM		" + TABLE_PRESOLDD_INFO + " A, " + TABLE_PRESOLDH_INFO + " B " +
						" WHERE		A." + KEY_DETAIL_FILE + " = B." + KEY_DETAIL_FILE + " AND B." +
						KEY_VISITED + " = 0 AND A." + KEY_ITEM_NO + " = '" + strItemNo + "' AND B." + KEY_TYPE + " != 'B'";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<PresoldOrDInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++) {
					PresoldOrDInfo info = new PresoldOrDInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.nQtyOption = cursor.getInt(cursor.getColumnIndex(KEY_QTY_OPTION));
					info.nSuggestedQty = cursor.getInt(cursor.getColumnIndex(KEY_SUGGEST_QTY));
					info.nRequestedQty = cursor.getInt(cursor.getColumnIndex(KEY_REQUESTED_QTY));
					info.nOrderQty = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_QTY));
					info.dOrderQty = cursor.getDouble(cursor.getColumnIndex(KEY_DORDER_QTY));
					info.nPrice = cursor.getInt(cursor.getColumnIndex(KEY_PRICE));
					info.strPickLocationCode = cursor.getString(cursor.getColumnIndex(KEY_PICK_LOCATION_CODE));
					info.strTrxnType = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TYPE));
					info.nReasonCode = cursor.getInt(cursor.getColumnIndex(KEY_REASON_CODE));

					list.add(info);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<PresoldOrDInfo> getFreePresoldOrDList(String strDetailFile) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_PRESOLDD_INFO +
						" WHERE		" + KEY_DETAIL_FILE + " = '" + strDetailFile + "' AND (" + KEY_TRXN_TYPE + " = '12' OR " + KEY_TRXN_TYPE + " = '11')";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<PresoldOrDInfo> list = new ArrayList<>();

		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++) {
					PresoldOrDInfo info = new PresoldOrDInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.nQtyOption = cursor.getInt(cursor.getColumnIndex(KEY_QTY_OPTION));
					info.nSuggestedQty = cursor.getInt(cursor.getColumnIndex(KEY_SUGGEST_QTY));
					info.nRequestedQty = cursor.getInt(cursor.getColumnIndex(KEY_REQUESTED_QTY));
					info.nOrderQty = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_QTY));
					info.dOrderQty = cursor.getDouble(cursor.getColumnIndex(KEY_DORDER_QTY));
					info.nPrice = cursor.getInt(cursor.getColumnIndex(KEY_PRICE));
					info.strPickLocationCode = cursor.getString(cursor.getColumnIndex(KEY_PICK_LOCATION_CODE));
					info.strTrxnType = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TYPE));
					info.nReasonCode = cursor.getInt(cursor.getColumnIndex(KEY_REASON_CODE));
					cursor.moveToNext();
					list.add(info);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<PresoldOrDInfo> getPresoldOrDListForFreeItem(String strDetailFile) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_PRESOLDD_INFO +
						" WHERE		" + KEY_DETAIL_FILE + " = '" + strDetailFile + "' ";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<PresoldOrDInfo> list = new ArrayList<>();
		Map<String, PresoldOrDInfo> map = new HashMap<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++) {
					PresoldOrDInfo info = new PresoldOrDInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.nQtyOption = cursor.getInt(cursor.getColumnIndex(KEY_QTY_OPTION));
					info.nSuggestedQty = cursor.getInt(cursor.getColumnIndex(KEY_SUGGEST_QTY));
					info.nRequestedQty = cursor.getInt(cursor.getColumnIndex(KEY_REQUESTED_QTY));
					info.nOrderQty = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_QTY));
					info.dOrderQty = cursor.getDouble(cursor.getColumnIndex(KEY_DORDER_QTY));
					info.nPrice = cursor.getInt(cursor.getColumnIndex(KEY_PRICE));
					info.strPickLocationCode = cursor.getString(cursor.getColumnIndex(KEY_PICK_LOCATION_CODE));
					info.strTrxnType = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TYPE));
					info.nReasonCode = cursor.getInt(cursor.getColumnIndex(KEY_REASON_CODE));
					cursor.moveToNext();

					String mapKey = info.strItemNo + "+" + info.nQtyOption + "+" + info.strTrxnType + "+" + info.nReasonCode;
					PresoldOrDInfo presoldOrDInfo = map.get(mapKey);
					if( presoldOrDInfo != null )
					{
						presoldOrDInfo.nSuggestedQty += info.nSuggestedQty;
						presoldOrDInfo.nRequestedQty += info.nRequestedQty;
						presoldOrDInfo.nOrderQty += info.nOrderQty;
					}
					else
					{
						map.put(mapKey, presoldOrDInfo);
						list.add(info);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public long updatePresoldOrDInfo(PresoldOrDInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_DETAIL_FILE, info.strDetailFile);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_QTY_OPTION, info.nQtyOption);
		values.put(KEY_ORDER_QTY, info.nOrderQty);
		values.put(KEY_DORDER_QTY, info.dOrderQty);
		values.put(KEY_SUGGEST_QTY, info.nSuggestedQty);
		values.put(KEY_REQUESTED_QTY, info.nRequestedQty);
		values.put(KEY_PICK_LOCATION_CODE, info.strPickLocationCode);
		values.put(KEY_PRICE, info.nPrice);
		values.put(KEY_TRXN_TYPE, info.strTrxnType);
		values.put(KEY_REASON_CODE, info.nReasonCode);

		return mDb.update(TABLE_PRESOLDD_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}

	public long insertPresoldOrDInfo(PresoldOrDInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_DETAIL_FILE, info.strDetailFile);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_QTY_OPTION, info.nQtyOption);
		values.put(KEY_ORDER_QTY, info.nOrderQty);
		values.put(KEY_DORDER_QTY, info.dOrderQty);
		values.put(KEY_SUGGEST_QTY, info.nSuggestedQty);
		values.put(KEY_REQUESTED_QTY, info.nRequestedQty);
		values.put(KEY_PRICE, info.nPrice);
		values.put(KEY_PICK_LOCATION_CODE, info.strPickLocationCode);
		values.put(KEY_TRXN_TYPE, info.strTrxnType);
		values.put(KEY_REASON_CODE, info.nReasonCode);
		return mDb.insert(TABLE_PRESOLDD_INFO, null, values);
	}

	public void deletePresoldInfoWithChainNo(int nChainNo)
	{
		List<PresoldOrHInfo> presoldOrHInfoList = getPresoldOrHInfo(nChainNo);
		for(PresoldOrHInfo info : presoldOrHInfoList)
		{
			if(!info.strType.equals("B"))
			{
				deletePresoldOrDInfo(info.strDetailFile);
			}
		}
		mDb.delete(TABLE_PRESOLDH_INFO, KEY_CHAIN_NO + " = " + nChainNo + " AND " + KEY_TYPE + " != 'B'", null);
	}

	public boolean deleteAllPresoldOrDInfo() throws SQLException {
		return mDb.delete(TABLE_PRESOLDD_INFO, null, null) > 0;
	}

	public void deletePresoldOrDInfo(PresoldOrDInfo info)
	{
		mDb.delete(TABLE_PRESOLDD_INFO, KEY_IDX + " = " + info.nIdx, null);
	}

	public void deletePresoldOrDInfo(String strDetailFile)
	{
		mDb.delete(TABLE_PRESOLDD_INFO, KEY_DETAIL_FILE + " = '" + strDetailFile + "'", null);
	}
	// UOrderHInfo
	public List<UOrderHInfo> getUOrderHList(int nCustNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_UORDERH_INFO; 
		if (nCustNo == -1) {
//			sql += " WHERE	" + KEY_PRINTED + " = 0 AND " + KEY_VOID_FLAG + " = 0 ";
			//Modified By Andrew. For Print Menu.
			sql += " WHERE	" + KEY_VOID_FLAG + " = 0 ";
		} else if(nCustNo == 0) {
			//sql += " WHERE	" + KEY_VOID_FLAG + " = 0 ";
		}
		else {
			sql += " WHERE	" + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_UPLOADED + " = 0 ";
		}
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UOrderHInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++) {
					UOrderHInfo info = new UOrderHInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
					info.strOrderNumber = cursor.getString(cursor.getColumnIndex(KEY_ORDER_NO));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
					info.strSpecialInstructions = cursor.getString(cursor.getColumnIndex(KEY_SPECIAL_INSTRUCTIONS));
					info.strDistributor = cursor.getString(cursor.getColumnIndex(KEY_DISTRIBUTOR));
					info.strPOReference = cursor.getString(cursor.getColumnIndex(KEY_PO_REFERENCE));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.strOrderName = cursor.getString(cursor.getColumnIndex(KEY_ORDER_NAME));
					info.nTotalAmount = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_AMOUNT));
					info.nTotalSamples = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_SAMPLES));
					info.nTotalFree = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_FREE));
					info.nOrderType = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_TYPE));
					info.nTotalDumps = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_DUMPS));
					info.nTotalBuybacks = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_BUYBACKS));
					info.nDiscSales = cursor.getFloat(cursor.getColumnIndex(KEY_DISC_SALES));
					info.nDiscDumps = cursor.getFloat(cursor.getColumnIndex(KEY_DISC_DUMPS));
					info.nDiscBuybacks = cursor.getFloat(cursor.getColumnIndex(KEY_DISC_BUYBACKS));
					info.nNetSales = cursor.getFloat(cursor.getColumnIndex(KEY_NET_SALES));
					info.nNetDumps = cursor.getFloat(cursor.getColumnIndex(KEY_NET_DUMPS));
					info.nNetBuybacks = cursor.getFloat(cursor.getColumnIndex(KEY_NET_BUYBACKS));
					info.nSaleAmount = cursor.getFloat(cursor.getColumnIndex(KEY_SALE_AMOUNT));
					info.nTaxAmount = cursor.getFloat(cursor.getColumnIndex(KEY_TAX_AMOUNT));
					info.nPromotionAmount = cursor.getFloat(cursor.getColumnIndex(KEY_PROMOTION_AMOUNT));
					info.strSignatureFilePath = cursor.getString(cursor.getColumnIndex(KEY_SIGNATURE_FILE_PATH));
					info.strPresoldChgd = cursor.getString(cursor.getColumnIndex(KEY_PRESOLD_CHGD));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
					info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER2));
					info.nVoidFlag = cursor.getInt(cursor.getColumnIndex(KEY_VOID_FLAG));
					info.nPrintedFlag = cursor.getInt(cursor.getColumnIndex(KEY_PRINTED_FLAG));
					info.bPrinted = cursor.getInt(cursor.getColumnIndex(KEY_PRINTED)) == 0 ? false : true;
					info.bUploaded = cursor.getInt(cursor.getColumnIndex(KEY_UPLOADED)) == 0 ? false : true;
					info.bCentsRound = cursor.getInt(cursor.getColumnIndex(KEY_CENTSROUND)) == 0 ? false : true;
					info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
					info.dTotalAmount = cursor.getDouble(cursor.getColumnIndex(KEY_DTOTAL_AMOUNT));
					info.dTotalDumps = cursor.getDouble(cursor.getColumnIndex(KEY_DTOTAL_DUMPS));
					info.dTotalBuybacks = cursor.getDouble(cursor.getColumnIndex(KEY_DTOTAL_BUYBACKS));
					info.dTotalFree = cursor.getDouble(cursor.getColumnIndex(KEY_DTOTAL_FREE));
					info.strSourceType = cursor.getString(cursor.getColumnIndex(KEY_SOURCE_TYPE));
					info.strNettSplit = cursor.getString(cursor.getColumnIndex(KEY_NETT_SPLIT));

					info.nHandlingFee = cursor.getFloat(cursor.getColumnIndex(KEY_HANDLING_FEE));
					info.nHandlingFeeTaxAmount = cursor.getFloat(cursor.getColumnIndex(KEY_HANDLING_FEE_TAX));
					info.strHandlingTaxCode = cursor.getString(cursor.getColumnIndex(KEY_HANDLING_FEE_TAX_CODE));
					info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
					info.strDriverNumber = cursor.getString(cursor.getColumnIndex(KEY_DRIVER_NUMBER));
					info.strDSD = cursor.getString(cursor.getColumnIndex(KEY_DSD));

					info.nVoidTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_VOID_TRXN_NO));
					info.nOrgTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_ORG_TRXN_NO));

					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<UOrderHInfo> getUOrderHList(int nCustNo, MakeZipFileInfo zipFileInfo) {

		List<UOrderHInfo> list = new ArrayList<>();
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			String sql =	" SELECT	* " +
					" FROM		" + TABLE_UORDERH_INFO;
			sql += " WHERE	" + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_UPLOADED + " = 0 " + " AND " + KEY_TRXN_NO + " = " + nTrxnNo;
			Cursor cursor = mDb.rawQuery(sql, null);

			try {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i=0; i<cursor.getCount(); i++) {
						UOrderHInfo info = new UOrderHInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
						info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
						info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
						info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
						info.strOrderNumber = cursor.getString(cursor.getColumnIndex(KEY_ORDER_NO));
						info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
						info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
						info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
						info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
						info.strSpecialInstructions = cursor.getString(cursor.getColumnIndex(KEY_SPECIAL_INSTRUCTIONS));
						info.strDistributor = cursor.getString(cursor.getColumnIndex(KEY_DISTRIBUTOR));
						info.strPOReference = cursor.getString(cursor.getColumnIndex(KEY_PO_REFERENCE));
						info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
						info.strOrderName = cursor.getString(cursor.getColumnIndex(KEY_ORDER_NAME));
						info.nTotalAmount = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_AMOUNT));
						info.nTotalSamples = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_SAMPLES));
						info.nTotalFree = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_FREE));
						info.nOrderType = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_TYPE));
						info.nTotalDumps = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_DUMPS));
						info.nTotalBuybacks = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_BUYBACKS));
						info.nDiscSales = cursor.getFloat(cursor.getColumnIndex(KEY_DISC_SALES));
						info.nDiscDumps = cursor.getFloat(cursor.getColumnIndex(KEY_DISC_DUMPS));
						info.nDiscBuybacks = cursor.getFloat(cursor.getColumnIndex(KEY_DISC_BUYBACKS));
						info.nNetSales = cursor.getFloat(cursor.getColumnIndex(KEY_NET_SALES));
						info.nNetDumps = cursor.getFloat(cursor.getColumnIndex(KEY_NET_DUMPS));
						info.nNetBuybacks = cursor.getFloat(cursor.getColumnIndex(KEY_NET_BUYBACKS));
						info.nSaleAmount = cursor.getFloat(cursor.getColumnIndex(KEY_SALE_AMOUNT));
						info.nTaxAmount = cursor.getFloat(cursor.getColumnIndex(KEY_TAX_AMOUNT));
						info.nPromotionAmount = cursor.getFloat(cursor.getColumnIndex(KEY_PROMOTION_AMOUNT));
						info.strSignatureFilePath = cursor.getString(cursor.getColumnIndex(KEY_SIGNATURE_FILE_PATH));
						info.strPresoldChgd = cursor.getString(cursor.getColumnIndex(KEY_PRESOLD_CHGD));
						info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
						info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER2));
						info.nVoidFlag = cursor.getInt(cursor.getColumnIndex(KEY_VOID_FLAG));
						info.nPrintedFlag = cursor.getInt(cursor.getColumnIndex(KEY_PRINTED_FLAG));
						info.bPrinted = cursor.getInt(cursor.getColumnIndex(KEY_PRINTED)) == 0 ? false : true;
						info.bUploaded = cursor.getInt(cursor.getColumnIndex(KEY_UPLOADED)) == 0 ? false : true;
						info.bCentsRound = cursor.getInt(cursor.getColumnIndex(KEY_CENTSROUND)) == 0 ? false : true;
						info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
						info.dTotalAmount = cursor.getDouble(cursor.getColumnIndex(KEY_DTOTAL_AMOUNT));
						info.dTotalDumps = cursor.getDouble(cursor.getColumnIndex(KEY_DTOTAL_DUMPS));
						info.dTotalBuybacks = cursor.getDouble(cursor.getColumnIndex(KEY_DTOTAL_BUYBACKS));
						info.dTotalFree = cursor.getDouble(cursor.getColumnIndex(KEY_DTOTAL_FREE));
						info.strSourceType = cursor.getString(cursor.getColumnIndex(KEY_SOURCE_TYPE));
						info.strNettSplit = cursor.getString(cursor.getColumnIndex(KEY_NETT_SPLIT));

						info.nHandlingFee = cursor.getFloat(cursor.getColumnIndex(KEY_HANDLING_FEE));
						info.nHandlingFeeTaxAmount = cursor.getFloat(cursor.getColumnIndex(KEY_HANDLING_FEE_TAX));
						info.strHandlingTaxCode = cursor.getString(cursor.getColumnIndex(KEY_HANDLING_FEE_TAX_CODE));
						info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
						info.strDriverNumber = cursor.getString(cursor.getColumnIndex(KEY_DRIVER_NUMBER));
						info.strDSD = cursor.getString(cursor.getColumnIndex(KEY_DSD));

						info.nVoidTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_VOID_TRXN_NO));
						info.nOrgTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_ORG_TRXN_NO));
						cursor.moveToNext();
						list.add(info);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		return list;
	}

	public UOrderHInfo getUOrderHInfo(long nTrxnNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_UORDERH_INFO + 
						" WHERE		" + KEY_TRXN_NO + " = " + nTrxnNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		UOrderHInfo info = null;
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info = new UOrderHInfo();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
				info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
				info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
				info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
				info.strOrderNumber = cursor.getString(cursor.getColumnIndex(KEY_ORDER_NO));
				info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
				info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
				info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
				info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
				info.strSpecialInstructions = cursor.getString(cursor.getColumnIndex(KEY_SPECIAL_INSTRUCTIONS));
				info.strDistributor = cursor.getString(cursor.getColumnIndex(KEY_DISTRIBUTOR));
				info.strPOReference = cursor.getString(cursor.getColumnIndex(KEY_PO_REFERENCE));
				info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
				info.strOrderName = cursor.getString(cursor.getColumnIndex(KEY_ORDER_NAME));
				info.nTotalAmount = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_AMOUNT));
				info.nTotalSamples = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_SAMPLES));
				info.nTotalFree = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_FREE));
				info.nOrderType = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_TYPE));
				info.nTotalDumps = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_DUMPS));
				info.nTotalBuybacks = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_BUYBACKS));
				info.nDiscSales = cursor.getFloat(cursor.getColumnIndex(KEY_DISC_SALES));
				info.nDiscDumps = cursor.getFloat(cursor.getColumnIndex(KEY_DISC_DUMPS));
				info.nDiscBuybacks = cursor.getFloat(cursor.getColumnIndex(KEY_DISC_BUYBACKS));
				info.nNetSales = cursor.getFloat(cursor.getColumnIndex(KEY_NET_SALES));
				info.nNetDumps = cursor.getFloat(cursor.getColumnIndex(KEY_NET_DUMPS));
				info.nNetBuybacks = cursor.getFloat(cursor.getColumnIndex(KEY_NET_BUYBACKS));
				info.nSaleAmount = cursor.getFloat(cursor.getColumnIndex(KEY_SALE_AMOUNT));
				info.nTaxAmount = cursor.getFloat(cursor.getColumnIndex(KEY_TAX_AMOUNT));
				info.nPromotionAmount = cursor.getFloat(cursor.getColumnIndex(KEY_PROMOTION_AMOUNT));
				info.strSignatureFilePath = cursor.getString(cursor.getColumnIndex(KEY_SIGNATURE_FILE_PATH));
				info.strPresoldChgd = cursor.getString(cursor.getColumnIndex(KEY_PRESOLD_CHGD));
				info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
				info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER2));
				info.nVoidFlag = cursor.getInt(cursor.getColumnIndex(KEY_VOID_FLAG));
				info.nPrintedFlag = cursor.getInt(cursor.getColumnIndex(KEY_PRINTED_FLAG));
				info.bPrinted = cursor.getInt(cursor.getColumnIndex(KEY_PRINTED)) == 0 ? false : true;
				info.bUploaded = cursor.getInt(cursor.getColumnIndex(KEY_UPLOADED)) == 0 ? false : true;
				info.bCentsRound = cursor.getInt(cursor.getColumnIndex(KEY_CENTSROUND)) == 0 ? false : true;
				info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
				info.dTotalAmount = cursor.getDouble(cursor.getColumnIndex(KEY_DTOTAL_AMOUNT));
				info.dTotalDumps = cursor.getDouble(cursor.getColumnIndex(KEY_DTOTAL_DUMPS));
				info.dTotalBuybacks = cursor.getDouble(cursor.getColumnIndex(KEY_DTOTAL_BUYBACKS));
				info.dTotalFree = cursor.getDouble(cursor.getColumnIndex(KEY_DTOTAL_FREE));
				info.strSourceType = cursor.getString(cursor.getColumnIndex(KEY_SOURCE_TYPE));
				info.strNettSplit = cursor.getString(cursor.getColumnIndex(KEY_NETT_SPLIT));

				info.nHandlingFee = cursor.getFloat(cursor.getColumnIndex(KEY_HANDLING_FEE));
				info.nHandlingFeeTaxAmount = cursor.getFloat(cursor.getColumnIndex(KEY_HANDLING_FEE_TAX));
				info.strHandlingTaxCode = cursor.getString(cursor.getColumnIndex(KEY_HANDLING_FEE_TAX_CODE));
				info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
				info.strDriverNumber = cursor.getString(cursor.getColumnIndex(KEY_DRIVER_NUMBER));
				info.strDSD = cursor.getString(cursor.getColumnIndex(KEY_DSD));

				info.nVoidTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_VOID_TRXN_NO));
				info.nOrgTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_ORG_TRXN_NO));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	public long insertOrUpdateUOrderHInfo(UOrderHInfo info) {
		if (isExistUOrderHInfo(info.nIdx) == true) {
			updateUOrderHInfo(info);
			return info.nIdx;
		} else {
			long nIdx = insertUOrderHInfo(info);
			info.nIdx = (int)nIdx;
			return nIdx;
		}
	}

	private boolean isExistUOrderHInfo(int nIdx) {
		String sql =	" SELECT	" + KEY_IDX +
						" FROM		" + TABLE_UORDERH_INFO +
						" WHERE		" + KEY_IDX + " = " + nIdx;
		Cursor cursor = mDb.rawQuery(sql, null);
		boolean bRes = false;
		try {
			if (cursor != null && cursor.getCount() == 1) {
				bRes = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return bRes;
	}

	private long insertUOrderHInfo(UOrderHInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_DOC_NO, info.strDocNo);
		values.put(KEY_ORDER_NO, info.strOrderNumber);
		values.put(KEY_DAY_NO, info.nDayNo);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		values.put(KEY_REFERENCE, info.strReference);
		values.put(KEY_SPECIAL_INSTRUCTIONS, info.strSpecialInstructions);
		values.put(KEY_DISTRIBUTOR, info.strDistributor);
		values.put(KEY_PO_REFERENCE, info.strPOReference);
		values.put(KEY_DELIVERY_DATE, info.strDeliveryDate);
		values.put(KEY_ORDER_NAME, info.strOrderName);
		values.put(KEY_TOTAL_AMOUNT, info.nTotalAmount);
		values.put(KEY_TOTAL_SAMPLES, info.nTotalSamples);
		values.put(KEY_TOTAL_FREE, info.nTotalFree);
		values.put(KEY_ORDER_TYPE, info.nOrderType);
		values.put(KEY_TOTAL_DUMPS, info.nTotalDumps);
		values.put(KEY_TOTAL_BUYBACKS, info.nTotalBuybacks);
		values.put(KEY_DISC_SALES, info.nDiscSales);
		values.put(KEY_DISC_DUMPS, info.nDiscDumps);
		values.put(KEY_DISC_BUYBACKS, info.nDiscBuybacks);
		values.put(KEY_NET_SALES, info.nNetSales);
		values.put(KEY_NET_DUMPS, info.nNetDumps);
		values.put(KEY_NET_BUYBACKS, info.nNetBuybacks);
		values.put(KEY_SALE_AMOUNT, info.nSaleAmount);
		values.put(KEY_TAX_AMOUNT, info.nTaxAmount);
		values.put(KEY_PROMOTION_AMOUNT, info.nPromotionAmount);
		values.put(KEY_SIGNATURE_FILE_PATH, info.strSignatureFilePath);
		values.put(KEY_PRESOLD_CHGD, info.strPresoldChgd);
		values.put(KEY_USER1, info.strUser1);
		values.put(KEY_USER2, info.strUser2);
		values.put(KEY_VOID_FLAG, info.nVoidFlag);
		values.put(KEY_PRINTED_FLAG, info.nPrintedFlag);
		values.put(KEY_PRINTED, info.bPrinted == true ? 1 : 0);
		values.put(KEY_UPLOADED, info.bUploaded == true ? 1 : 0);
		values.put(KEY_CENTSROUND, info.bCentsRound == true ? 1 : 0);
		values.put(KEY_PERIOD_NO, info.nPeriodNo);
		values.put(KEY_DTOTAL_AMOUNT, info.dTotalAmount);
		values.put(KEY_DTOTAL_DUMPS, info.dTotalDumps);
		values.put(KEY_DTOTAL_BUYBACKS, info.dTotalBuybacks);
		values.put(KEY_DTOTAL_FREE, info.dTotalFree);
		values.put(KEY_SOURCE_TYPE, info.strSourceType);
		values.put(KEY_NETT_SPLIT, info.strNettSplit);
		values.put(KEY_HANDLING_FEE, info.nHandlingFee);
		values.put(KEY_HANDLING_FEE_TAX, info.nHandlingFeeTaxAmount);
		values.put(KEY_HANDLING_FEE_TAX_CODE, info.strHandlingTaxCode);
		values.put(KEY_DOC_TYPE, info.strDocType);
		values.put(KEY_DRIVER_NUMBER, info.strDriverNumber);
		values.put(KEY_VOID_TRXN_NO, info.nVoidTrxnNo);
		values.put(KEY_ORG_TRXN_NO, info.nOrgTrxnNo);
		values.put(KEY_DSD, info.strDSD);

		return mDb.insert(TABLE_UORDERH_INFO, null, values);
	}

	public long updateUOrderHInfo(UOrderHInfo info) {
		ContentValues values = new ContentValues();
		if (info.nTrxnNo != 0) {
			values.put(KEY_TRXN_NO, info.nTrxnNo);
		}
		if (info.nChainNo != 0) {
			values.put(KEY_CHAIN_NO, info.nChainNo);
		}
		if (info.nCustNo != 0) {
			values.put(KEY_CUST_NO, info.nCustNo);
		}
		if (info.strOrderNumber.length() > 0) {
			values.put(KEY_ORDER_NO, info.strOrderNumber);
		}
		if (info.strDocNo.length() > 0) {
			values.put(KEY_DOC_NO, info.strDocNo);
		}
		if (info.nDayNo != 0) {
			values.put(KEY_DAY_NO, info.nDayNo);
		}
		if (info.strTrxnDate.length() > 0) {
			values.put(KEY_TRXN_DATE, info.strTrxnDate);
		}
		if (info.strTrxnTime.length() > 0) {
			values.put(KEY_TRXN_TIME, info.strTrxnTime);
		}
		if (info.strReference.length() > 0) {
			values.put(KEY_REFERENCE, info.strReference);
		}
		if (info.strSpecialInstructions.length() > 0) {
			values.put(KEY_SPECIAL_INSTRUCTIONS, info.strSpecialInstructions);
		}
		if (info.strDistributor.length() > 0) {
			values.put(KEY_DISTRIBUTOR, info.strDistributor);
		}
		if (info.strPOReference.length() > 0) {
			values.put(KEY_PO_REFERENCE, info.strPOReference);
		}
		if (info.strDeliveryDate.length() > 0) {
			values.put(KEY_DELIVERY_DATE, info.strDeliveryDate);
		}
		if (info.strOrderName.length() > 0) {
			values.put(KEY_ORDER_NAME, info.strOrderName);
		}
		if (info.nTotalAmount != 0) {
			values.put(KEY_TOTAL_AMOUNT, info.nTotalAmount);
		}
		if (info.nPromotionAmount != 0) {
			values.put(KEY_PROMOTION_AMOUNT, info.nPromotionAmount);
		}
		if (info.nTotalSamples != 0) {
			values.put(KEY_TOTAL_SAMPLES, info.nTotalSamples);
		}
		if (info.nTotalFree != 0) {
			values.put(KEY_TOTAL_FREE, info.nTotalFree);
		}
		if (info.nOrderType != 0) {
			values.put(KEY_ORDER_TYPE, info.nOrderType);
		}
		if (info.nTotalDumps != 0) {
			values.put(KEY_TOTAL_DUMPS, info.nTotalDumps);
		}
		if (info.nTotalBuybacks != 0) {
			values.put(KEY_TOTAL_BUYBACKS, info.nTotalBuybacks);
		}
		if (info.nDiscSales != 0) {
			values.put(KEY_DISC_SALES, info.nDiscSales);
		}
		if (info.nDiscDumps != 0) {
			values.put(KEY_DISC_DUMPS, info.nDiscDumps);
		}
		if (info.nDiscBuybacks != 0) {
			values.put(KEY_DISC_BUYBACKS, info.nDiscBuybacks);
		}
		if (info.nNetSales != 0) {
			values.put(KEY_NET_SALES, info.nNetSales);
		}
		if (info.nNetDumps != 0) {
			values.put(KEY_NET_DUMPS, info.nNetDumps);
		}
		if (info.nNetBuybacks != 0) {
			values.put(KEY_NET_BUYBACKS, info.nNetBuybacks);
		}
		if (info.nSaleAmount != 0) {
			values.put(KEY_SALE_AMOUNT, info.nSaleAmount);
		}
		if (info.nTaxAmount != 0) {
			values.put(KEY_TAX_AMOUNT, info.nTaxAmount);
		}
		if (info.strSignatureFilePath.length() > 0) {
			values.put(KEY_SIGNATURE_FILE_PATH, info.strSignatureFilePath);
		}
		if (info.strPresoldChgd.length() > 0) {
			values.put(KEY_PRESOLD_CHGD, info.strPresoldChgd);
		}
		if (!TextUtils.isEmpty(info.strUser1)) {
			values.put(KEY_USER1, info.strUser1);
		}
		if (!TextUtils.isEmpty(info.strUser2)) {
			values.put(KEY_USER2, info.strUser2);
		}
		values.put(KEY_VOID_FLAG, info.nVoidFlag);
		values.put(KEY_PRINTED_FLAG, info.nPrintedFlag);
		values.put(KEY_PRINTED, info.bPrinted == true ? 1 : 0);
		values.put(KEY_UPLOADED, info.bUploaded == true ? 1 : 0);
		values.put(KEY_CENTSROUND, info.bCentsRound== true ? 1 : 0);
		if( info.nPeriodNo != 0 ) {
			values.put(KEY_PERIOD_NO, info.nPeriodNo);
		}
		if(info.dTotalAmount != 0)
		{
			values.put(KEY_DTOTAL_AMOUNT, info.dTotalAmount);
		}
		if(info.dTotalDumps != 0)
		{
			values.put(KEY_DTOTAL_DUMPS, info.dTotalDumps);
		}
		if(info.dTotalBuybacks != 0)
		{
			values.put(KEY_DTOTAL_BUYBACKS, info.dTotalBuybacks);
		}
		if(info.dTotalFree != 0)
		{
			values.put(KEY_DTOTAL_FREE, info.dTotalFree);
		}
		if(!info.strSourceType.equals("I"))
		{
			values.put(KEY_SOURCE_TYPE, info.strSourceType);
		}
		if(!info.strNettSplit.equals("N"))
		{
			values.put(KEY_NETT_SPLIT, info.strNettSplit);
		}
		if(info.nHandlingFee > 0)
		{
			values.put(KEY_HANDLING_FEE, info.nHandlingFee);
		}
		if(info.nHandlingFeeTaxAmount > 0)
		{
			values.put(KEY_HANDLING_FEE_TAX, info.nHandlingFeeTaxAmount);
		}
		if(!TextUtils.isEmpty(info.strHandlingTaxCode))
		{
			values.put(KEY_HANDLING_FEE_TAX_CODE, info.strHandlingTaxCode);
		}
		if(!TextUtils.isEmpty(info.strDocType))
		{
			values.put(KEY_DOC_TYPE, info.strDocType);
		}
		if(!TextUtils.isEmpty(info.strDriverNumber))
		{
			values.put(KEY_DRIVER_NUMBER, info.strDriverNumber);
		}

		if(info.nVoidTrxnNo != 0)
		{
			values.put(KEY_VOID_TRXN_NO, info.nVoidTrxnNo);
		}

		if(info.nOrgTrxnNo != 0)
		{
			values.put(KEY_ORG_TRXN_NO, info.nOrgTrxnNo);
		}

		if(!TextUtils.isEmpty(info.strDSD))
		{
			values.put(KEY_DSD, info.strDSD);
		}

		return mDb.update(TABLE_UORDERH_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}

	public boolean deleteUOrderHInfo(int nIdx) throws SQLException {
		String sql =	" SELECT	" + KEY_TRXN_NO +
						" FROM		" + TABLE_UORDERH_INFO +
						" WHERE		" + KEY_IDX + " = " + nIdx;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<Long> list = new ArrayList<Long>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					long nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					cursor.moveToNext();
					list.add(nTrxnNo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		for (long nTrxnNo : list) {
			deleteAllUOrderDInfo(nTrxnNo);
			deleteAllUTaxInfo(nTrxnNo);
		}
		return mDb.delete(TABLE_UORDERH_INFO, KEY_IDX + " = " + nIdx, null) > 0;
	}

	public boolean deleteAllUOrderHInfo() throws SQLException {
		String sql =	" SELECT	" + KEY_TRXN_NO +
						" FROM		" + TABLE_UORDERH_INFO;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<Long> list = new ArrayList<Long>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					long nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					cursor.moveToNext();
					list.add(nTrxnNo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		for (long nTrxnNo : list) {
			deleteAllUOrderDInfo(nTrxnNo);
			deleteAllUTaxInfo(nTrxnNo);
		}
		return mDb.delete(TABLE_UORDERH_INFO, null, null) > 0;
	}

	// UOrderDInfo
	public List<UOrderDInfo> getUOrderDList(long nTrxnNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_UORDERD_INFO +
						" WHERE		" + KEY_TRXN_NO + " = " + nTrxnNo +
						" ORDER BY	" + KEY_IDX + " ASC ";

		Cursor cursor = mDb.rawQuery(sql, null);
		List<UOrderDInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UOrderDInfo info = new UOrderDInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nTrxnType = cursor.getInt(cursor.getColumnIndex(KEY_TRXN_TYPE));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strProdGroup = cursor.getString(cursor.getColumnIndex(KEY_PROD_GRP));
					info.nQuantity = cursor.getInt(cursor.getColumnIndex(KEY_QUANTITY));
					info.nCaseFactor = cursor.getInt(cursor.getColumnIndex(KEY_CASE_FACTOR));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
					info.nPrice = cursor.getFloat(cursor.getColumnIndex(KEY_PRICE));
					info.nGrossPrice = cursor.getFloat(cursor.getColumnIndex(KEY_GROSS_PRICE));
					info.nOrgPrice = cursor.getFloat(cursor.getColumnIndex(KEY_ORG_PRICE));
					info.nOrgGrossPrice = cursor.getFloat(cursor.getColumnIndex(KEY_ORG_GROSS_PRICE));
					info.nRetailPrice = cursor.getInt(cursor.getColumnIndex(KEY_RETAIL_PRICE));
					info.nReasonCode = cursor.getInt(cursor.getColumnIndex(KEY_REASON_CODE));
					info.nBasePrice = cursor.getFloat(cursor.getColumnIndex(KEY_BASE_PRICE));
					info.nDiscountVal = cursor.getFloat(cursor.getColumnIndex(KEY_DISCOUNT_VAL));
					info.nTaxAmount = cursor.getFloat(cursor.getColumnIndex(KEY_TAX_AMOUNT));
					info.nPromoDiscount = cursor.getFloat(cursor.getColumnIndex(KEY_PROMO_DISCOUNT));
					info.nPromoType = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_TYPE));
					info.nPlanQty = cursor.getInt(cursor.getColumnIndex(KEY_PLAN_QTY));
					info.nDumps = cursor.getInt(cursor.getColumnIndex(KEY_DUMPS));
					info.nSales = cursor.getInt(cursor.getColumnIndex(KEY_SALES));
					info.dQuantity = cursor.getDouble(cursor.getColumnIndex(KEY_DQUANTITY));
					info.dSales = cursor.getDouble(cursor.getColumnIndex(KEY_DSALES));
					info.dDumps = cursor.getDouble(cursor.getColumnIndex(KEY_DDUMPS));
					info.dPlanQty = cursor.getDouble(cursor.getColumnIndex(KEY_DPLANQTY));
					info.uTaxInfoList = getUTaxInfoList(info.nTrxnNo, info.nCustNo, info.nLocnNo, info.strItemNo, info.nTrxnType);
					info.nCustomerRetailPrice = cursor.getFloat(cursor.getColumnIndex(KEY_CUSTOMER_RETAIL_PRICE));
					info.nPriceOverride = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_OVERRIDE));
					info.uPromotionInfoList = getUPromotionList(info.nTrxnNo, info.nCustNo, info.strItemNo, info.nTrxnType);
					String strLoadBlotData = cursor.getString(cursor.getColumnIndex(KEY_LOAD_BLOT_DATA));
					if(!TextUtils.isEmpty(strLoadBlotData))
					{
						Gson gson = new Gson();
						Type listType = new TypeToken<List<LoadBlotInfo>>(){}.getType();
						info.uLoadBlotInfo = gson.fromJson(strLoadBlotData, listType);
					}

					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<UOrderDInfo> getUOrderDListWithSCWType(long nTrxnNo) {
		String sql =	" SELECT	A.*, B." + KEY_SCW_TYPE +
				" FROM		" + TABLE_UORDERD_INFO + " A, " + TABLE_PRODLOCN_INFO + " B" +
				" WHERE		A." + KEY_TRXN_NO + " = " + nTrxnNo + " AND B." + KEY_ITEM_NO + " = A." + KEY_ITEM_NO +
				" ORDER BY	A." + KEY_IDX + " ASC ";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UOrderDInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UOrderDInfo info = new UOrderDInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nTrxnType = cursor.getInt(cursor.getColumnIndex(KEY_TRXN_TYPE));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strProdGroup = cursor.getString(cursor.getColumnIndex(KEY_PROD_GRP));
					info.nQuantity = cursor.getInt(cursor.getColumnIndex(KEY_QUANTITY));
					info.nCaseFactor = cursor.getInt(cursor.getColumnIndex(KEY_CASE_FACTOR));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
					info.nPrice = cursor.getFloat(cursor.getColumnIndex(KEY_PRICE));
					info.nGrossPrice = cursor.getFloat(cursor.getColumnIndex(KEY_GROSS_PRICE));
					info.nOrgPrice = cursor.getFloat(cursor.getColumnIndex(KEY_ORG_PRICE));
					info.nOrgGrossPrice = cursor.getFloat(cursor.getColumnIndex(KEY_ORG_GROSS_PRICE));
					info.nRetailPrice = cursor.getInt(cursor.getColumnIndex(KEY_RETAIL_PRICE));
					info.nReasonCode = cursor.getInt(cursor.getColumnIndex(KEY_REASON_CODE));
					info.nBasePrice = cursor.getFloat(cursor.getColumnIndex(KEY_BASE_PRICE));
					info.nDiscountVal = cursor.getFloat(cursor.getColumnIndex(KEY_DISCOUNT_VAL));
					info.nTaxAmount = cursor.getFloat(cursor.getColumnIndex(KEY_TAX_AMOUNT));
					info.nPromoDiscount = cursor.getFloat(cursor.getColumnIndex(KEY_PROMO_DISCOUNT));
					info.nPromoType = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_TYPE));
					info.nPlanQty = cursor.getInt(cursor.getColumnIndex(KEY_PLAN_QTY));
					info.nDumps = cursor.getInt(cursor.getColumnIndex(KEY_DUMPS));
					info.nSales = cursor.getInt(cursor.getColumnIndex(KEY_SALES));
					info.dQuantity = cursor.getDouble(cursor.getColumnIndex(KEY_DQUANTITY));
					info.dSales = cursor.getDouble(cursor.getColumnIndex(KEY_DSALES));
					info.dDumps = cursor.getDouble(cursor.getColumnIndex(KEY_DDUMPS));
					info.dPlanQty = cursor.getDouble(cursor.getColumnIndex(KEY_DPLANQTY));
					info.uTaxInfoList = getUTaxInfoList(info.nTrxnNo, info.nCustNo, info.nLocnNo, info.strItemNo, info.nTrxnType);
					info.nCustomerRetailPrice = cursor.getFloat(cursor.getColumnIndex(KEY_CUSTOMER_RETAIL_PRICE));
					info.nPriceOverride = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_OVERRIDE));
					info.uPromotionInfoList = getUPromotionList(info.nTrxnNo, info.nCustNo, info.strItemNo, info.nTrxnType);
					info.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
					String strLoadBlotData = cursor.getString(cursor.getColumnIndex(KEY_LOAD_BLOT_DATA));
					if(!TextUtils.isEmpty(strLoadBlotData))
					{
						Gson gson = new Gson();
						Type listType = new TypeToken<List<LoadBlotInfo>>(){}.getType();
						info.uLoadBlotInfo = gson.fromJson(strLoadBlotData, listType);
					}
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public void deleteUTaxInfo(long nTrxnNo, int nCustNo, int nLocnNo, String strItemNo, int nTrxnType)
	{
		mDb.delete(TABLE_UTAX_INFO, KEY_TRXN_NO + " = " + nTrxnNo +
				" AND " + KEY_TRXN_TYPE + " = " + nTrxnType + " AND " + KEY_CUST_NO + " = " + nCustNo +
				" AND " + KEY_ITEM_NO + " = '" + strItemNo + "' AND " + KEY_LOCN_NO + " = " + nLocnNo, null);
	}

	public long insertOrUpdateUOrderDInfo(UOrderDInfo info) {
		long nIdx;
		/*if (isExistUOrderDInfo(info.nIdx) == true) {
			nIdx = updateUOrderDInfo(info);
		} else {
			nIdx = insertUOrderDInfo(info);
		}*/
		nIdx = insertUOrderDInfo(info);
		info.nIdx = (int)nIdx;
		deleteUTaxInfo(info.nTrxnNo, info.nCustNo, info.nLocnNo, info.strItemNo, info.nTrxnType);
		for(UTaxInfo uTaxInfo : info.uTaxInfoList)
		{
			insertUTaxInfo(uTaxInfo);
		}
		deleteUPromotion(info.nTrxnNo, info.nCustNo, info.strItemNo, info.nTrxnType);
		for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
		{
			uPromotionInfo.nTrxnNo = info.nTrxnNo;
			insertUPromotionInfo(uPromotionInfo);
		}
		return nIdx;
	}

	private boolean isExistUOrderDInfo(int nIdx) {
		String sql =	" SELECT	" + KEY_IDX +
						" FROM		" + TABLE_UORDERD_INFO +
						" WHERE		" + KEY_IDX + " = " + nIdx;
		Cursor cursor = mDb.rawQuery(sql, null);
		boolean bRes = false;
		try {
			if (cursor != null && cursor.getCount() == 1) {
				bRes = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return bRes;
	}

	private long insertUOrderDInfo(UOrderDInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_TRXN_TYPE, info.nTrxnType);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_LOCN_NO, info.nLocnNo);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_QUANTITY, info.nQuantity);
		values.put(KEY_CASE_FACTOR, info.nCaseFactor);
		values.put(KEY_DESCRIPTION, info.strDescription);
		values.put(KEY_SHORT_DESC, info.strShortDesc);
		values.put(KEY_PRICE, info.nPrice);
		values.put(KEY_GROSS_PRICE, info.nGrossPrice);
		values.put(KEY_ORG_PRICE, info.nOrgPrice);
		values.put(KEY_ORG_GROSS_PRICE, info.nOrgGrossPrice);
		values.put(KEY_RETAIL_PRICE, info.nRetailPrice);
		values.put(KEY_REASON_CODE, info.nReasonCode);
		values.put(KEY_BASE_PRICE, info.nBasePrice);
		values.put(KEY_DISCOUNT_VAL, info.nDiscountVal);
		values.put(KEY_TAX_AMOUNT, info.nTaxAmount);
		values.put(KEY_PROMO_DISCOUNT, info.nPromoDiscount);
		values.put(KEY_PROMO_TYPE, info.nPromoType);
		values.put(KEY_DUMPS, info.nDumps);
		values.put(KEY_SALES, info.nSales);
		values.put(KEY_CUSTOMER_RETAIL_PRICE, info.nCustomerRetailPrice);
		values.put(KEY_PRICE_OVERRIDE, info.nPriceOverride);
		values.put(KEY_PLAN_QTY, info.nPlanQty);
		values.put(KEY_DQUANTITY, info.dQuantity);
		values.put(KEY_DSALES, info.dSales);
		values.put(KEY_DDUMPS, info.dDumps);
		values.put(KEY_DPLANQTY, info.dPlanQty);
		values.put(KEY_PROD_GRP, info.strProdGroup);
		Gson gson = new Gson();
		values.put(KEY_LOAD_BLOT_DATA, gson.toJson(info.uLoadBlotInfo));
		return mDb.insert(TABLE_UORDERD_INFO, null, values);
	}

	private long updateUOrderDInfo(UOrderDInfo info) {
		ContentValues values = new ContentValues();
		if (info.nTrxnNo != 0) {
			values.put(KEY_TRXN_NO, info.nTrxnNo);
		}
		if (info.nTrxnType != 0) {
			values.put(KEY_TRXN_TYPE, info.nTrxnType);
		}
		if (info.nCustNo != 0) {
			values.put(KEY_CUST_NO, info.nCustNo);
		}
		if (info.nLocnNo != 0) {
			values.put(KEY_LOCN_NO, info.nLocnNo);
		}
		if (TextUtils.isEmpty(info.strItemNo)) {
			values.put(KEY_ITEM_NO, info.strItemNo);
		}
		if (info.nQuantity != 0) {
			values.put(KEY_QUANTITY, info.nQuantity);
		}
		if (info.nCaseFactor != 0) {
			values.put(KEY_CASE_FACTOR, info.nCaseFactor);
		}
		if (!TextUtils.isEmpty(info.strDescription)) {
			values.put(KEY_DESCRIPTION, info.strDescription);
		}
		if (!TextUtils.isEmpty(info.strShortDesc)) {
			values.put(KEY_SHORT_DESC, info.strShortDesc);
		}
		if (info.nPrice != 0) {
			values.put(KEY_PRICE, info.nPrice);
		}
		if (info.nGrossPrice != 0) {
			values.put(KEY_GROSS_PRICE, info.nGrossPrice);
		}
		if (info.nOrgPrice != 0) {
			values.put(KEY_ORG_PRICE, info.nOrgPrice);
		}
		if (info.nOrgGrossPrice != 0) {
			values.put(KEY_ORG_GROSS_PRICE, info.nOrgGrossPrice);
		}
		if (info.nRetailPrice != 0) {
			values.put(KEY_RETAIL_PRICE, info.nRetailPrice);
		}
		if (info.nReasonCode != 0) {
			values.put(KEY_REASON_CODE, info.nReasonCode);
		}
		if (info.nBasePrice != 0) {
			values.put(KEY_BASE_PRICE, info.nBasePrice);
		}
		if (info.nDiscountVal != 0) {
			values.put(KEY_DISCOUNT_VAL, info.nDiscountVal);
		}
		if (info.nTaxAmount != 0) {
			values.put(KEY_TAX_AMOUNT, info.nTaxAmount);
		}
		if (info.nPromoDiscount != 0) {
			values.put(KEY_PROMO_DISCOUNT, info.nPromoDiscount);
		}
		if (info.nPromoType != 0) {
			values.put(KEY_PROMO_TYPE, info.nPromoType);
		}
		if (info.nDumps != 0) {
			values.put(KEY_DUMPS, info.nDumps);
		}
		if (info.nSales != 0) {
			values.put(KEY_SALES, info.nSales);
		}
		if (info.nPriceOverride != 0 )
		{
			values.put(KEY_PRICE_OVERRIDE, info.nPriceOverride);
		}
		if (info.nCustomerRetailPrice != 0 )
		{
			values.put(KEY_CUSTOMER_RETAIL_PRICE, info.nCustomerRetailPrice);
		}
		if (info.nPlanQty != 0 )
		{
			values.put(KEY_PLAN_QTY, info.nPlanQty);
		}
		if(info.dPlanQty != 0)
		{
			values.put(KEY_DQUANTITY, info.dQuantity);
		}
		if(info.dSales != 0)
		{
			values.put(KEY_DSALES, info.dSales);
		}
		if(info.dDumps != 0)
		{
			values.put(KEY_DDUMPS, info.dDumps);
		}
		if(info.dPlanQty != 0)
		{
			values.put(KEY_DPLANQTY, info.dPlanQty);
		}
		if(TextUtils.isEmpty(info.strProdGroup))
		{
			values.put(KEY_PROD_GRP, info.strProdGroup);
		}
		if(info.uLoadBlotInfo.size() > 0)
		{
			Gson gson = new Gson();
			values.put(KEY_LOAD_BLOT_DATA, gson.toJson(info.uLoadBlotInfo));
		}
		return mDb.update(TABLE_UORDERD_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}

	private boolean deleteAllUOrderDInfo(long nTrxnNo) throws SQLException {
		return mDb.delete(TABLE_UORDERD_INFO, KEY_TRXN_NO + " = " + nTrxnNo, null) > 0;
	}

	// UTaxInfo

	private List<UTaxInfo> getUTaxInfoList(long nTrxnNo, int nCustNo, int nLocnNo, String strItemNo, int nTrxnType) {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_UTAX_INFO +
				" WHERE		" + KEY_TRXN_NO + " = " + nTrxnNo + " AND " +
				KEY_CUST_NO + " = " + nCustNo + " AND " +
				KEY_LOCN_NO + " = " + nLocnNo + " AND " +
				KEY_ITEM_NO + " = '" + strItemNo + "' AND " +
				KEY_TRXN_TYPE + " = " + nTrxnType;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UTaxInfo> uTaxInfoList = new ArrayList<>();

		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					UTaxInfo info = new UTaxInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nTrxnType = cursor.getInt(cursor.getColumnIndex(KEY_TRXN_TYPE));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strTaxRateCode = cursor.getString(cursor.getColumnIndex(KEY_TAX_RATE_CODE));
					info.nTaxAmount = cursor.getFloat(cursor.getColumnIndex(KEY_TAX_AMOUNT));
					info.strCumulativeFlag = cursor.getString(cursor.getColumnIndex(KEY_CUMULATIVE_FLAG));
					info.nTaxRate = cursor.getInt(cursor.getColumnIndex(KEY_TAX_RATE));
					info.nReasonCode = cursor.getInt(cursor.getColumnIndex(KEY_REASON_CODE));
					info.strTaxRateNumber = cursor.getString(cursor.getColumnIndex(KEY_TAX_RATE_NUMBER));
					cursor.moveToNext();
					uTaxInfoList.add(info);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return uTaxInfoList;
	}

	public List<UTaxInfo> getUTaxList(int nCustNo) {
		String sql = 	" SELECT	* " +
						" FROM		" + TABLE_UTAX_INFO +
						" WHERE		" + KEY_CUST_NO + " = " + nCustNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UTaxInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UTaxInfo info = new UTaxInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nTrxnType = cursor.getInt(cursor.getColumnIndex(KEY_TRXN_TYPE));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strTaxRateCode = cursor.getString(cursor.getColumnIndex(KEY_TAX_RATE_CODE));
					info.nTaxAmount = cursor.getFloat(cursor.getColumnIndex(KEY_TAX_AMOUNT));
					info.strCumulativeFlag = cursor.getString(cursor.getColumnIndex(KEY_CUMULATIVE_FLAG));
					info.nTaxRate = cursor.getInt(cursor.getColumnIndex(KEY_TAX_RATE));
					info.nReasonCode = cursor.getInt(cursor.getColumnIndex(KEY_REASON_CODE));
					info.strTaxRateNumber = cursor.getString(cursor.getColumnIndex(KEY_TAX_RATE_NUMBER));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	private UTaxInfo getUTaxInfo(long nTrxnNo, int nCustNo, int nLocnNo, String strItemNo, int nTrxnType) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_UTAX_INFO +
						" WHERE		" + KEY_TRXN_NO + " = " + nTrxnNo + " AND " +
										KEY_CUST_NO + " = " + nCustNo + " AND " +
										KEY_LOCN_NO + " = " + nLocnNo + " AND " +
										KEY_TRXN_TYPE + " = " + nTrxnType + " AND " +
										KEY_ITEM_NO + " = '" + strItemNo + "' ";
		Cursor cursor = mDb.rawQuery(sql, null);
		UTaxInfo info = new UTaxInfo();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
				info.nTrxnType = cursor.getInt(cursor.getColumnIndex(KEY_TRXN_TYPE));
				info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
				info.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));
				info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
				info.strTaxRateCode = cursor.getString(cursor.getColumnIndex(KEY_TAX_RATE_CODE));
				info.nTaxAmount = cursor.getFloat(cursor.getColumnIndex(KEY_TAX_AMOUNT));
				info.strCumulativeFlag = cursor.getString(cursor.getColumnIndex(KEY_CUMULATIVE_FLAG));
				info.nTaxRate = cursor.getInt(cursor.getColumnIndex(KEY_TAX_RATE));
				info.nReasonCode = cursor.getInt(cursor.getColumnIndex(KEY_REASON_CODE));
				info.strTaxRateNumber = cursor.getString(cursor.getColumnIndex(KEY_TAX_RATE_NUMBER));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	private void insertOrUpdateUTaxInfo(UTaxInfo info) {
		if (isExistUTaxInfo(info.nIdx) == true) {
			updateUTaxInfo(info);
		} else {
			insertUTaxInfo(info);
		}
	}

	private boolean isExistUTaxInfo(int nIdx) {
		String sql =	" SELECT	" + KEY_IDX +
						" FROM		" + TABLE_UTAX_INFO +
						" WHERE		" + KEY_IDX + " = " + nIdx;
		Cursor cursor = mDb.rawQuery(sql, null);
		boolean bRes = false;
		try {
			if (cursor != null && cursor.getCount() == 1) {
				bRes = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return bRes;
	}

	public long insertUTaxInfo(UTaxInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_TRXN_TYPE, info.nTrxnType);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_LOCN_NO, info.nLocnNo);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_TAX_RATE_CODE, info.strTaxRateCode);
		values.put(KEY_TAX_AMOUNT, info.nTaxAmount);
		values.put(KEY_CUMULATIVE_FLAG, info.strCumulativeFlag);
		values.put(KEY_TAX_RATE, info.nTaxRate);
		values.put(KEY_REASON_CODE, info.nReasonCode);
		values.put(KEY_TAX_RATE_NUMBER, info.strTaxRateNumber);
		return mDb.insert(TABLE_UTAX_INFO, null, values);
	}

	private long updateUTaxInfo(UTaxInfo info) {
		ContentValues values = new ContentValues();
		if (info.nTrxnNo != 0) {
			values.put(KEY_TRXN_NO, info.nTrxnNo);
		}
		if (info.nTrxnType != 0) {
			values.put(KEY_TRXN_TYPE, info.nTrxnType);
		}
		if (info.nCustNo != 0) {
			values.put(KEY_CUST_NO, info.nCustNo);
		}
		if (info.nLocnNo != 0) {
			values.put(KEY_LOCN_NO, info.nLocnNo);
		}
		if (TextUtils.isEmpty(info.strItemNo)) {
			values.put(KEY_ITEM_NO, info.strItemNo);
		}
		if (TextUtils.isEmpty(info.strTaxRateCode)) {
			values.put(KEY_TAX_RATE_CODE, info.strTaxRateCode);
		}
		if (info.nTaxAmount != 0) {
			values.put(KEY_TAX_AMOUNT, info.nTaxAmount);
		}
		if (!TextUtils.isEmpty(info.strCumulativeFlag)) {
			values.put(KEY_CUMULATIVE_FLAG, info.strCumulativeFlag);
		}
		if (info.nTaxRate != 0) {
			values.put(KEY_TAX_RATE, info.nTaxRate);
		}
		if (info.nReasonCode != 0) {
			values.put(KEY_REASON_CODE, info.nReasonCode);
		}
		if(!TextUtils.isEmpty(info.strTaxRateNumber)) {
			values.put(KEY_TAX_RATE_NUMBER, info.strTaxRateNumber);
		}
		return mDb.update(TABLE_UTAX_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}

	private boolean deleteAllUTaxInfo(long nTrxnNo) throws SQLException {
		return mDb.delete(TABLE_UTAX_INFO, KEY_TRXN_NO + " = " + nTrxnNo, null) > 0;
	}

	// TaxCodesInfo
	private TaxCodesInfo getTaxCodesInfo(String strCustTaxCode, String strItemTaxCode) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_TAXCODES_INFO +
						" WHERE		" + KEY_CUST_TAX_CODE + " = '" + strCustTaxCode + "' AND " +
										KEY_ITEM_TAX_CODE + " = '" + strItemTaxCode + "' ";
		Cursor cursor = mDb.rawQuery(sql, null);
		TaxCodesInfo info = new TaxCodesInfo();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.strCustTaxCode = cursor.getString(cursor.getColumnIndex(KEY_CUST_TAX_CODE));
				info.strItemTaxCode = cursor.getString(cursor.getColumnIndex(KEY_ITEM_TAX_CODE));
				info.strTaxRateCode = cursor.getString(cursor.getColumnIndex(KEY_TAX_RATE_CODE));
				info.strCumulativeFlag = cursor.getString(cursor.getColumnIndex(KEY_CUMULATIVE_FLAG));
				info.nTaxSeq = cursor.getInt(cursor.getColumnIndex(KEY_TAX_CODE_SEQ));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	public long insertTaxCodesInfo(TaxCodesInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_CUST_TAX_CODE, info.strCustTaxCode);
		values.put(KEY_ITEM_TAX_CODE, info.strItemTaxCode);
		values.put(KEY_TAX_RATE_CODE, info.strTaxRateCode);
		values.put(KEY_CUMULATIVE_FLAG, info.strCumulativeFlag);
		values.put(KEY_TAX_CODE_SEQ, info.nTaxSeq);
		return mDb.insert(TABLE_TAXCODES_INFO, null, values);
	}

	public boolean deleteAllTaxCodesInfo() throws SQLException {
		return mDb.delete(TABLE_TAXCODES_INFO, null, null) > 0;
	}

	// TaxRatesInfo
	public List<TaxRatesInfo> getTaxRatesInfoList(String strCustTaxCode, String strItemNo) {
		ProdLocnInfo prodLocnInfo = getProdLocnInfo(strItemNo);
		if( prodLocnInfo == null )
		{
			return new ArrayList<>();
		}

		List<TaxCodesInfo> taxCodesInfoList = getTaxCodesInfoList(strCustTaxCode, prodLocnInfo.strItemTaxCode);
		String strToday = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());

		List<TaxRatesInfo> taxRatesInfoList = new ArrayList<>();
		for(TaxCodesInfo taxCodesInfo : taxCodesInfoList)
		{
			String sql =	" SELECT	* " +
					" FROM		" + TABLE_TAXRATES_INFO +
					" WHERE		" + KEY_TAX_RATE_CODE + " = '" + taxCodesInfo.strTaxRateCode + "' AND " +
					KEY_START_DATE + " <= '" + strToday + "' AND " +
					KEY_END_DATE + " >= '" + strToday + "' ";

			Cursor cursor = mDb.rawQuery(sql, null);

			try {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for(int i = 0; i < cursor.getCount(); i++)
					{
						TaxRatesInfo info = new TaxRatesInfo();
						info.strCumulativeFlag = taxCodesInfo.strCumulativeFlag;
						info.nTaxSeq = taxCodesInfo.nTaxSeq;
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.strTaxRateCode = cursor.getString(cursor.getColumnIndex(KEY_TAX_RATE_CODE));
						info.strStartDate = cursor.getString(cursor.getColumnIndex(KEY_START_DATE));
						info.strEndDate = cursor.getString(cursor.getColumnIndex(KEY_END_DATE));
						info.nTaxRate = cursor.getInt(cursor.getColumnIndex(KEY_TAX_RATE));
						info.strRateDescription = cursor.getString(cursor.getColumnIndex(KEY_RATE_DESCRIPTION));
						info.nTaxMethod = cursor.getInt(cursor.getColumnIndex(KEY_TAX_METHOD));
						info.strTaxRateNumber = cursor.getString(cursor.getColumnIndex(KEY_TAX_RATE_NUMBER));
						info.strTaxType = cursor.getString(cursor.getColumnIndex(KEY_TAX_TYPE));
						cursor.moveToNext();
						taxRatesInfoList.add(info);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		Collections.sort(taxRatesInfoList, new Comparator<TaxRatesInfo>() {
			@Override
			public int compare(TaxRatesInfo taxRatesInfo, TaxRatesInfo t1) {
				return taxRatesInfo.nTaxSeq - t1.nTaxSeq;
			}
		});
		return taxRatesInfoList;
	}

	// TaxRatesInfo
	public TaxRatesInfo getTaxRatesInfo(String strCustTaxCode, String strItemNo) {
		ProdLocnInfo prodLocnInfo = getProdLocnInfo(strItemNo);
		if( prodLocnInfo == null || prodLocnInfo.strItemTaxCode == null )
		{
			return new TaxRatesInfo();
		}
		TaxCodesInfo taxCodesInfo = getTaxCodesInfo(strCustTaxCode, prodLocnInfo.strItemTaxCode);
		String strToday = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_TAXRATES_INFO +
						" WHERE		" + KEY_TAX_RATE_CODE + " = '" + taxCodesInfo.strTaxRateCode + "' AND " +
										KEY_START_DATE + " <= '" + strToday + "' AND " +
										KEY_END_DATE + " >= '" + strToday + "' ";
		
		Cursor cursor = mDb.rawQuery(sql, null);
		TaxRatesInfo info = new TaxRatesInfo();
		info.strCumulativeFlag = taxCodesInfo.strCumulativeFlag;
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info.strCumulativeFlag = "";
				info.nTaxSeq = 0;
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.strTaxRateCode = cursor.getString(cursor.getColumnIndex(KEY_TAX_RATE_CODE));
				info.strStartDate = cursor.getString(cursor.getColumnIndex(KEY_START_DATE));
				info.strEndDate = cursor.getString(cursor.getColumnIndex(KEY_END_DATE));
				info.nTaxRate = cursor.getInt(cursor.getColumnIndex(KEY_TAX_RATE));
				info.strRateDescription = cursor.getString(cursor.getColumnIndex(KEY_RATE_DESCRIPTION));
				info.nTaxMethod = cursor.getInt(cursor.getColumnIndex(KEY_TAX_METHOD));
				info.strTaxRateNumber = cursor.getString(cursor.getColumnIndex(KEY_TAX_RATE_NUMBER));
				info.strTaxType = cursor.getString(cursor.getColumnIndex(KEY_TAX_TYPE));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	public TaxRatesInfo getTaxRatesInfo(String strTaxRateCode)
	{
		String strToday = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_TAXRATES_INFO +
				" WHERE		" + KEY_TAX_RATE_CODE + " = '" + strTaxRateCode + "' AND " +
				KEY_START_DATE + " <= '" + strToday + "' AND " +
				KEY_END_DATE + " >= '" + strToday + "' ";

		Cursor cursor = mDb.rawQuery(sql, null);
		TaxRatesInfo info = null;
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info = new TaxRatesInfo();
				info.strCumulativeFlag = "";
				info.nTaxSeq = 0;
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.strTaxRateCode = cursor.getString(cursor.getColumnIndex(KEY_TAX_RATE_CODE));
				info.strStartDate = cursor.getString(cursor.getColumnIndex(KEY_START_DATE));
				info.strEndDate = cursor.getString(cursor.getColumnIndex(KEY_END_DATE));
				info.nTaxRate = cursor.getInt(cursor.getColumnIndex(KEY_TAX_RATE));
				info.strRateDescription = cursor.getString(cursor.getColumnIndex(KEY_RATE_DESCRIPTION));
				info.nTaxMethod = cursor.getInt(cursor.getColumnIndex(KEY_TAX_METHOD));
				info.strTaxRateNumber = cursor.getString(cursor.getColumnIndex(KEY_TAX_RATE_NUMBER));
				info.strTaxType = cursor.getString(cursor.getColumnIndex(KEY_TAX_TYPE));
				cursor.moveToNext();

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	private List<TaxCodesInfo> getTaxCodesInfoList(String strCustTaxCode, String strItemTaxCode) {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_TAXCODES_INFO +
				" WHERE		" + KEY_CUST_TAX_CODE + " = '" + strCustTaxCode + "' AND " +
				KEY_ITEM_TAX_CODE + " = '" + strItemTaxCode + "' ";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<TaxCodesInfo> taxCodesInfoList = new ArrayList<>();

		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					TaxCodesInfo info = new TaxCodesInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strCustTaxCode = cursor.getString(cursor.getColumnIndex(KEY_CUST_TAX_CODE));
					info.strItemTaxCode = cursor.getString(cursor.getColumnIndex(KEY_ITEM_TAX_CODE));
					info.strTaxRateCode = cursor.getString(cursor.getColumnIndex(KEY_TAX_RATE_CODE));
					info.strCumulativeFlag = cursor.getString(cursor.getColumnIndex(KEY_CUMULATIVE_FLAG));
					info.nTaxSeq = cursor.getInt(cursor.getColumnIndex(KEY_TAX_CODE_SEQ));
					cursor.moveToNext();
					taxCodesInfoList.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return taxCodesInfoList;
	}

	public long insertTaxRatesInfo(TaxRatesInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TAX_RATE_CODE, info.strTaxRateCode);
		values.put(KEY_START_DATE, info.strStartDate);
		values.put(KEY_END_DATE, info.strEndDate);
		values.put(KEY_TAX_RATE, info.nTaxRate);
		values.put(KEY_RATE_DESCRIPTION, info.strRateDescription);

		values.put(KEY_TAX_METHOD, info.nTaxMethod);
		values.put(KEY_TAX_RATE_NUMBER, info.strTaxRateNumber);
		values.put(KEY_TAX_TYPE, info.strTaxType);

		mDb.insert(TABLE_TAXRATES_INFO, null, values);
		return 0;
	}

	public boolean deleteAllTaxRatesInfo() throws SQLException {
		return mDb.delete(TABLE_TAXRATES_INFO, null, null) > 0;
	}

	private Map<String, PromNovoInfo> getPromNovoList1()
	{
		List<PromHeadInfo> promHeadList = new ArrayList<PromHeadInfo>();
		if (Global.gCurCustomerInfo != null && Global.gCurCustomerInfo.nPromoPlan == 999999)
		{
			List<ProMultiInfo> proMultiList = getProMultiList(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
			for (ProMultiInfo multiInfo : proMultiList)
			{
				List<PromHeadInfo> list = getPromHeadList(multiInfo.nPlanNo);
				for (PromHeadInfo info : list)
				{
					promHeadList.add(info);
				}
			}
		}
		else if(Global.gCurCustomerInfo != null)
		{
			promHeadList = getPromHeadList(Global.gCurCustomerInfo.nPromoPlan);
		}
		else
		{
			promHeadList = getPromHeadList(0);
		}
		List<PromAssInfo> promAssList = new ArrayList<PromAssInfo>();
		for (PromHeadInfo promHeadInfo : promHeadList)
		{
			List<PromAssInfo> list = getPromAssList(promHeadInfo.nAssignNo);
			for (PromAssInfo info : list)
			{
				promAssList.add(info);
			}
		}

		Map<String, PromNovoInfo> map = new HashMap<>();
		for (PromAssInfo promAssInfo : promAssList)
		{
			String sql =	" SELECT	* " +
					" FROM		" + TABLE_PROMNOVO_INFO +
					" WHERE		" + KEY_ASSIGN_NO + " = " + promAssInfo.nAssignNo;
			Cursor cursor = mDb.rawQuery(sql, null);
			try
			{
				if (cursor != null && cursor.getCount() > 0)
				{
					cursor.moveToFirst();
					for (int i=0; i<cursor.getCount(); i++)
					{
						PromNovoInfo info = new PromNovoInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.nAssignNo = cursor.getInt(cursor.getColumnIndex(KEY_ASSIGN_NO));
						info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
						info.nPromoDiscount = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_DISCOUNT));
						info.nPromoType = promAssInfo.nPromoType;
						cursor.moveToNext();
						map.put(info.strItemNo, info);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (cursor != null)
				{
					cursor.close();
				}
			}
		}
		return map;
	}

	// PromNovoInfo
	private List<PromNovoInfo> getPromNovoList()
	{
		List<PromHeadInfo> promHeadList = new ArrayList<PromHeadInfo>();
		if (Global.gCurCustomerInfo != null && Global.gCurCustomerInfo.nPromoPlan == 999999)
		{
			List<ProMultiInfo> proMultiList = getProMultiList(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
			for (ProMultiInfo multiInfo : proMultiList)
			{
				List<PromHeadInfo> list = getPromHeadList(multiInfo.nPlanNo);
				for (PromHeadInfo info : list)
				{
					promHeadList.add(info);
				}
			}
		}
		else if(Global.gCurCustomerInfo != null)
		{
			promHeadList = getPromHeadList(Global.gCurCustomerInfo.nPromoPlan);
		}
		else
		{
			promHeadList = getPromHeadList(0);
		}
		List<PromAssInfo> promAssList = new ArrayList<PromAssInfo>();
		for (PromHeadInfo promHeadInfo : promHeadList)
		{
			List<PromAssInfo> list = getPromAssList(promHeadInfo.nAssignNo);
			for (PromAssInfo info : list)
			{
				promAssList.add(info);
			}
		}

		List<PromNovoInfo> list = new ArrayList<PromNovoInfo>();
		for (PromAssInfo promAssInfo : promAssList)
		{
			String sql =	" SELECT	* " +
							" FROM		" + TABLE_PROMNOVO_INFO + 
							" WHERE		" + KEY_ASSIGN_NO + " = " + promAssInfo.nAssignNo;
			Cursor cursor = mDb.rawQuery(sql, null);
			try
			{
				if (cursor != null && cursor.getCount() > 0)
				{
					cursor.moveToFirst();
					for (int i=0; i<cursor.getCount(); i++)
					{
						PromNovoInfo info = new PromNovoInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.nAssignNo = cursor.getInt(cursor.getColumnIndex(KEY_ASSIGN_NO));
						info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
						info.nPromoDiscount = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_DISCOUNT));
						info.nPromoType = promAssInfo.nPromoType;
						cursor.moveToNext();
						list.add(info);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (cursor != null)
				{
					cursor.close();
				}
			}
		}
		return list;
	}

	public List<PromAssInfo> getPromAssList(CustomerInfo customerInfo) {
		List<PromHeadInfo> promHeadList = new ArrayList<PromHeadInfo>();
		if (customerInfo != null && customerInfo.nPromoPlan == 999999)
		{
			List<ProMultiInfo> proMultiList = getProMultiList(customerInfo.nChainNo, customerInfo.nCustNo);
			for (ProMultiInfo multiInfo : proMultiList)
			{
				List<PromHeadInfo> list = getPromHeadList(multiInfo.nPlanNo);
				for (PromHeadInfo info : list)
				{
					promHeadList.add(info);
				}
			}
		}
		else if(customerInfo != null)
		{
			promHeadList = getPromHeadList(customerInfo.nPromoPlan);
		}
		else
		{
			promHeadList = getPromHeadList(0);
		}
		List<PromAssInfo> promAssList = new ArrayList<PromAssInfo>();
		for (PromHeadInfo promHeadInfo : promHeadList)
		{
			List<PromAssInfo> list = getPromAssList(promHeadInfo.nAssignNo);
			for (PromAssInfo info : list)
			{
				promAssList.add(info);
			}
		}
		return promAssList;
	}

	private List<UPromotionInfo> getSalePromNovoList(String strItemNo)
	{
		List<PromHeadInfo> promHeadList = new ArrayList<PromHeadInfo>();
		if (Global.gCurCustomerInfo != null && Global.gCurCustomerInfo.nPromoPlan == 999999)
		{
			List<ProMultiInfo> proMultiList = getProMultiList(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
			for (ProMultiInfo multiInfo : proMultiList)
			{
				List<PromHeadInfo> list = getPromHeadList(multiInfo.nPlanNo);
				for (PromHeadInfo info : list)
				{
					promHeadList.add(info);
				}
			}
		}
		else if(Global.gCurCustomerInfo != null)
		{
			promHeadList = getPromHeadList(Global.gCurCustomerInfo.nPromoPlan);
		}
		else
		{
			promHeadList = getPromHeadList(0);
		}
		List<PromAssInfo> promAssList = new ArrayList<PromAssInfo>();
		for (PromHeadInfo promHeadInfo : promHeadList)
		{
			List<PromAssInfo> list = getPromAssList(promHeadInfo.nAssignNo);
			for (PromAssInfo info : list)
			{
				promAssList.add(info);
			}
		}

		List<UPromotionInfo> list = new ArrayList<>();
		for (PromAssInfo promAssInfo : promAssList)
		{
			String sql =	" SELECT	* " +
					" FROM		" + TABLE_PROMNOVO_INFO +
					" WHERE		" + KEY_ASSIGN_NO + " = " + promAssInfo.nAssignNo + " AND (" + KEY_ITEM_NO + " = '" + strItemNo + "' OR " + KEY_ITEM_NO + " = '*ALL')";
			Cursor cursor = mDb.rawQuery(sql, null);
			try
			{
				if (cursor != null && cursor.getCount() > 0)
				{
					cursor.moveToFirst();
					for (int i=0; i<cursor.getCount(); i++)
					{
						UPromotionInfo info = new UPromotionInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.nAssignNo = cursor.getInt(cursor.getColumnIndex(KEY_ASSIGN_NO));
						info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
						if(Global.gCurCustomerInfo != null)
						{
							info.nCustNo = Global.gCurCustomerInfo.nCustNo;
							info.nChainNo = Global.gCurCustomerInfo.nChainNo;
						}
						else
						{
							info.nCustNo = 0;
							info.nChainNo = 0;
						}
						info.nPlanNo = promHeadList.get(0).nPlanNo;
						info.nPromoAppMethod = promAssInfo.nPromoAppMethod;
						info.nPromoMethod = promAssInfo.nPromoMethod;
						info.nPromoType = promAssInfo.nPromoType;
						info.nDiscAmt = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_DISCOUNT));
						info.strDateStart = promHeadList.get(0).strDateStart;
						info.nSeqNo = 0;
						list.add(info);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (cursor != null)
				{
					cursor.close();
				}
			}
		}
		return list;
	}

	private List<UPromotionInfo> getFreeGoodsPromNovoList()
	{
		List<PromHeadInfo> promHeadInfoList = getPromHeadList(Utils.getIntFromString(Utils.getStringSetting(mContext, Global.PREF_FRGOODPLN)));
		if(promHeadInfoList == null || promHeadInfoList.size() == 0) return null;
		List<PromAssInfo> promAssList = getPromAssList(Utils.getIntFromString(Utils.getStringSetting(mContext, Global.PREF_FRGOODASS)));
		if(promAssList == null || promAssList.size() == 0) return null;

		List<UPromotionInfo> list = new ArrayList<>();
		for (PromAssInfo promAssInfo : promAssList)
		{
			String sql =	" SELECT	* " +
							" FROM		" + TABLE_PROMNOVO_INFO +
							" WHERE		" + KEY_ASSIGN_NO + " = " + promAssInfo.nAssignNo;
			Cursor cursor = mDb.rawQuery(sql, null);
			try
			{
				if (cursor != null && cursor.getCount() > 0)
				{
					cursor.moveToFirst();
					for (int i=0; i<cursor.getCount(); i++)
					{
						UPromotionInfo info = new UPromotionInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.nAssignNo = cursor.getInt(cursor.getColumnIndex(KEY_ASSIGN_NO));
						info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
						if(Global.gCurCustomerInfo != null)
						{
							info.nCustNo = Global.gCurCustomerInfo.nCustNo;
							info.nChainNo = Global.gCurCustomerInfo.nChainNo;
						}
						else
						{
							info.nCustNo = 0;
							info.nChainNo = 0;
						}
						info.nPlanNo = promHeadInfoList.get(0).nPlanNo;
						info.nPromoAppMethod = promAssInfo.nPromoAppMethod;
						info.nPromoMethod = promAssInfo.nPromoMethod;
						info.nPromoType = promAssInfo.nPromoType;
						info.nDiscAmt = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_DISCOUNT));
						info.strDateStart = promHeadInfoList.get(0).strDateStart;
						info.nSeqNo = 0;
						list.add(info);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (cursor != null)
				{
					cursor.close();
				}
			}
		}
		return list;
	}

	public long insertPromNovoInfo(PromNovoInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_ASSIGN_NO, info.nAssignNo);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_FREE_ITEM_NO, info.strFreeItemNo);
		values.put(KEY_PROMO_DISCOUNT, info.nPromoDiscount);
		return mDb.insert(TABLE_PROMNOVO_INFO, null, values);
	}

	public boolean deleteAllPromNovoInfo() throws SQLException {
		return mDb.delete(TABLE_PROMNOVO_INFO, null, null) > 0;
	}

	// PricingInfo
	public Map<String, PricingInfo> getPricingInfo(int nCustNo)
	{
		String strToday = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_PRICING_INFO +
				" WHERE		" + KEY_CUST_NO + " = " + nCustNo + " AND " +
				KEY_DATE_START + " <= '" + strToday + "' AND " +
				KEY_DATE_END + " >= '" + strToday + "' ";
		Cursor cursor = mDb.rawQuery(sql, null);
		Map<String, PricingInfo> map = new HashMap<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					PricingInfo info = new PricingInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strDateStart = cursor.getString(cursor.getColumnIndex(KEY_DATE_START));
					info.strDateEnd = cursor.getString(cursor.getColumnIndex(KEY_DATE_END));
					info.nPrice = cursor.getInt(cursor.getColumnIndex(KEY_PRICE));
					info.strPrcFilRecord = cursor.getString(cursor.getColumnIndex(KEY_PRCFIL_RECORD));
					info.nPriceType = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_TYPE));
					map.put(info.strItemNo, info);
					cursor.moveToNext();
				}

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return map;
	}
	private PricingInfo getPricingInfo(int nCustNo, String strItemNo)
	{
		String strToday = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_PRICING_INFO + 
						" WHERE		" + KEY_CUST_NO + " = " + nCustNo + " AND " +
										KEY_ITEM_NO + " = '" + strItemNo + "' AND " +
										KEY_DATE_START + " <= '" + strToday + "' AND " +
										KEY_DATE_END + " >= '" + strToday + "' ";
		Cursor cursor = mDb.rawQuery(sql, null);
		PricingInfo info = null;
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				info = new PricingInfo();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
				info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
				info.strDateStart = cursor.getString(cursor.getColumnIndex(KEY_DATE_START));
				info.strDateEnd = cursor.getString(cursor.getColumnIndex(KEY_DATE_END));
				info.nPrice = cursor.getInt(cursor.getColumnIndex(KEY_PRICE));
				info.strPrcFilRecord = cursor.getString(cursor.getColumnIndex(KEY_PRCFIL_RECORD));
				info.nPriceType = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_TYPE));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return info;
	}

	public long insertPricingInfo(PricingInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_DATE_START, info.strDateStart);
		values.put(KEY_DATE_END, info.strDateEnd);
		values.put(KEY_PRICE, info.nPrice);
		values.put(KEY_PRCFIL_RECORD, info.strPrcFilRecord);
		values.put(KEY_PRICE_TYPE, info.nPriceType);
		return mDb.insert(TABLE_PRICING_INFO, null, values);
	}

	public boolean deleteAllPricingInfo() throws SQLException
	{
		return mDb.delete(TABLE_PRICING_INFO, null, null) > 0;
	}

	// PriceGrpInfo
	public Map<String, PriceGrpInfo> getPriceGrpInfo(int nPriceGrp)
	{
		String strToday = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_PRICEGRP_INFO +
				" WHERE		" + KEY_PRICE_GRP + " = " + nPriceGrp + " AND " +
				KEY_DATE_START + " <= '" + strToday + "' AND " +
				KEY_DATE_END + " >= '" + strToday + "' ";
		Cursor cursor = mDb.rawQuery(sql, null);
		Map<String, PriceGrpInfo> map = new HashMap<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					PriceGrpInfo info = new PriceGrpInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_GRP));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strDateStart = cursor.getString(cursor.getColumnIndex(KEY_DATE_START));
					info.strDateEnd = cursor.getString(cursor.getColumnIndex(KEY_DATE_END));
					info.nPrice = cursor.getInt(cursor.getColumnIndex(KEY_PRICE));
					info.strPrcFilRecord = cursor.getString(cursor.getColumnIndex(KEY_PRCFIL_RECORD));
					info.nPriceType = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_TYPE));
					map.put(info.strItemNo, info);
					cursor.moveToNext();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return map;
	}

	private PriceGrpInfo getPriceGrpInfo(int nPriceGrp, String strItemNo)
	{
		String strToday = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_PRICEGRP_INFO +
						" WHERE		" + KEY_PRICE_GRP + " = " + nPriceGrp + " AND " +
										KEY_ITEM_NO + " = '" + strItemNo + "' AND " +
										KEY_DATE_START + " <= '" + strToday + "' AND " +
										KEY_DATE_END + " >= '" + strToday + "' ";
		Cursor cursor = mDb.rawQuery(sql, null);
		PriceGrpInfo info = null;
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				info = new PriceGrpInfo();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.nPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_GRP));
				info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
				info.strDateStart = cursor.getString(cursor.getColumnIndex(KEY_DATE_START));
				info.strDateEnd = cursor.getString(cursor.getColumnIndex(KEY_DATE_END));
				info.nPrice = cursor.getInt(cursor.getColumnIndex(KEY_PRICE));
				info.strPrcFilRecord = cursor.getString(cursor.getColumnIndex(KEY_PRCFIL_RECORD));
				info.nPriceType = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_TYPE));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return info;
	}

	public long insertPriceGrpInfo(PriceGrpInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_PRICE_GRP, info.nPriceGrp);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_DATE_START, info.strDateStart);
		values.put(KEY_DATE_END, info.strDateEnd);
		values.put(KEY_PRICE, info.nPrice);
		values.put(KEY_PRCFIL_RECORD, info.strPrcFilRecord);
		values.put(KEY_PRICE_TYPE, info.nPriceType);
		return mDb.insert(TABLE_PRICEGRP_INFO, null, values);
	}

	public boolean deleteAllPriceGrpInfo() throws SQLException
	{
		return mDb.delete(TABLE_PRICEGRP_INFO, null, null) > 0;
	}

	// PromAssInfo
	public List<PromAssInfo> getPromAssList(int nAssignNo)
	{
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_PROMASS_INFO +
						" WHERE		" + KEY_ASSIGN_NO + " = " + nAssignNo + " AND " +
										KEY_PROMO_METHOD + " = 6 ";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<PromAssInfo> list = new ArrayList<PromAssInfo>();
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++)
				{
					PromAssInfo info = new PromAssInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nAssignNo = cursor.getInt(cursor.getColumnIndex(KEY_ASSIGN_NO));
					info.nPromoAppMethod = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_APP_METHOD));
					info.nPromoType = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_TYPE));
					info.nPromoMethod = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_METHOD));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					cursor.moveToNext();
					list.add(info);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return list;
	}

	public long insertPromAssInfo(PromAssInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_ASSIGN_NO, info.nAssignNo);
		values.put(KEY_PROMO_APP_METHOD, info.nPromoAppMethod);
		values.put(KEY_PROMO_TYPE, info.nPromoType);
		values.put(KEY_PROMO_METHOD, info.nPromoMethod);
		values.put(KEY_DESCRIPTION, info.strDescription);
		return mDb.insert(TABLE_PROMASS_INFO, null, values);
	}

	public boolean deleteAllPromAssInfo() throws SQLException
	{
		return mDb.delete(TABLE_PROMASS_INFO, null, null) > 0;
	}

	// PromHeadInfo
	public List<PromHeadInfo> getPromHeadList(int nPlanNo, List<Integer> dateList)
	{
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_PROMHEAD_INFO +
						" WHERE		" + KEY_PLAN_NO + " = " + nPlanNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<PromHeadInfo> list = new ArrayList<PromHeadInfo>();
		try
		{
			if (cursor != null && cursor.getCount() > 0 && dateList.size() > 0)
			{
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++)
				{
					PromHeadInfo info = new PromHeadInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nPlanNo = cursor.getInt(cursor.getColumnIndex(KEY_PLAN_NO));
					info.nAssignNo = cursor.getInt(cursor.getColumnIndex(KEY_ASSIGN_NO));
					info.strDateStart = cursor.getString(cursor.getColumnIndex(KEY_DATE_START));
					info.strDateEnd = cursor.getString(cursor.getColumnIndex(KEY_DATE_END));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
	
					int nDateStart = Utils.parseIntFromString(info.strDateStart);
					int nDateEnd = Utils.parseIntFromString(info.strDateEnd);
					if (nDateStart != 0 && nDateEnd != 0)
					{
						for (int nCurDate : dateList)
						{
							if (nDateStart <= nCurDate && nCurDate <= nDateEnd)
							{
								list.add(info);
								break;
							}
						}
					}
	
					cursor.moveToNext();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return list;
	}

	public List<PromHeadInfo> getPromHeadList(int nPlanNo)
	{
		String strToday = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
		//String strToday = Utils.getRoutectlTripDate(mContext, Global.DATE_YYYYMMDD_FORMAT, new Date());
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_PROMHEAD_INFO +
						" WHERE		" + KEY_PLAN_NO + " = " + nPlanNo + " AND " +
								KEY_DATE_START + " <= '" + strToday + "' AND " +
								KEY_DATE_END + " >= '" + strToday + "' ";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<PromHeadInfo> list = new ArrayList<PromHeadInfo>();
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++)
				{
					PromHeadInfo info = new PromHeadInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nPlanNo = cursor.getInt(cursor.getColumnIndex(KEY_PLAN_NO));
					info.nAssignNo = cursor.getInt(cursor.getColumnIndex(KEY_ASSIGN_NO));
					info.strDateStart = cursor.getString(cursor.getColumnIndex(KEY_DATE_START));
					info.strDateEnd = cursor.getString(cursor.getColumnIndex(KEY_DATE_END));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					cursor.moveToNext();
					list.add(info);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return list;
	}

	public long insertPromHeadInfo(PromHeadInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_PLAN_NO, info.nPlanNo);
		values.put(KEY_ASSIGN_NO, info.nAssignNo);
		values.put(KEY_DATE_START, info.strDateStart);
		values.put(KEY_DATE_END, info.strDateEnd);
		values.put(KEY_DESCRIPTION, info.strDescription);
		return mDb.insert(TABLE_PROMHEAD_INFO, null, values);
	}

	public boolean deleteAllPromHeadInfo() throws SQLException
	{
		return mDb.delete(TABLE_PROMHEAD_INFO, null, null) > 0;
	}

	// ProMultiInfo
	public List<ProMultiInfo> getProMultiList(int nChainNo, int nCustNo, List<Integer> dateList)
	{
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_PROMULTI_INFO + 
						" WHERE		" + KEY_CHAIN_NO + " = " + nChainNo + " AND " +
										KEY_CUST_NO + " = " + nCustNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<ProMultiInfo> list = new ArrayList<ProMultiInfo>();
		try
		{
			if (cursor != null && cursor.getCount() > 0 && dateList.size() > 0)
			{
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++)
				{
					ProMultiInfo info = new ProMultiInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nPlanNo = cursor.getInt(cursor.getColumnIndex(KEY_PLAN_NO));
					info.strDateStart = cursor.getString(cursor.getColumnIndex(KEY_DATE_START));
					info.strDateEnd = cursor.getString(cursor.getColumnIndex(KEY_DATE_END));
					info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
					cursor.moveToNext();

					int nDateStart = Utils.parseIntFromString(info.strDateStart);
					int nDateEnd = Utils.parseIntFromString(info.strDateEnd);
					if (nDateStart != 0 && nDateEnd != 0)
					{
						for (int nCurDate : dateList)
						{
							if (nDateStart <= nCurDate && nCurDate <= nDateEnd)
							{
								list.add(info);
								break;
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return list;
	}

	public List<ProMultiInfo> getProMultiList(int nChainNo, int nCustNo)
	{
		String strToday = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
		//String strToday = Utils.getRoutectlTripDate(mContext, Global.DATE_YYYYMMDD_FORMAT, new Date());
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_PROMULTI_INFO + 
						" WHERE		" + KEY_CHAIN_NO + " = " + nChainNo + " AND " +
										KEY_CUST_NO + " = " + nCustNo + " AND " +
										KEY_DATE_START + " <= '" + strToday + "' AND " +
										KEY_DATE_END + " >= '" + strToday + "' ";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<ProMultiInfo> list = new ArrayList<ProMultiInfo>();
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++)
				{
					ProMultiInfo info = new ProMultiInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nPlanNo = cursor.getInt(cursor.getColumnIndex(KEY_PLAN_NO));
					info.strDateStart = cursor.getString(cursor.getColumnIndex(KEY_DATE_START));
					info.strDateEnd = cursor.getString(cursor.getColumnIndex(KEY_DATE_END));
					info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
					cursor.moveToNext();
					list.add(info);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return list;
	}

	public long insertProMultiInfo(ProMultiInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_PLAN_NO, info.nPlanNo);
		values.put(KEY_DATE_START, info.strDateStart);
		values.put(KEY_DATE_END, info.strDateEnd);
		values.put(KEY_SEQ_NO, info.nSeqNo);
		return mDb.insert(TABLE_PROMULTI_INFO, null, values);
	}

	public boolean deleteAllProMultiInfo() throws SQLException
	{
		return mDb.delete(TABLE_PROMULTI_INFO, null, null) > 0;
	}

	// UARTotalInfo
	public List<UARTotalInfo> getUARTotalList(int nCustNo) {
		String sql = "";
		if( nCustNo == 0 )
		{
			sql =	" SELECT	* " +
					" FROM		" + TABLE_UARTOTAL_INFO;
		}
		else
		{
			sql =	" SELECT	* " +
					" FROM		" + TABLE_UARTOTAL_INFO +
					" WHERE		" + KEY_CUST_NO + " = " + nCustNo;
		}
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UARTotalInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UARTotalInfo info = new UARTotalInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.nTotalDue = cursor.getFloat(cursor.getColumnIndex(KEY_TOTAL_DUE));
					info.nTotalUnalloc = cursor.getFloat(cursor.getColumnIndex(KEY_TOTAL_UNALLOC));
					info.nTotalCash = cursor.getFloat(cursor.getColumnIndex(KEY_TOTAL_CASH));
					info.nTotalCheque = cursor.getFloat(cursor.getColumnIndex(KEY_TOTAL_CHEQUE));
					info.nInvoiceTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_INVOICE_TRXN_NO));
					info.nInvoiceAmount = cursor.getFloat(cursor.getColumnIndex(KEY_INVOICE_AMOUNT));
					info.bIsVisitedDelivery = cursor.getInt(cursor.getColumnIndex(KEY_VISIT_DELIVERY)) == 1 ? true : false;
					info.bIsVisitedPickup = cursor.getInt(cursor.getColumnIndex(KEY_VISIT_PICKUP)) == 1 ? true : false;
					info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
					info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
					info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<UARTotalInfo> getUARTotalList(int nCustNo, MakeZipFileInfo zipFileInfo) {

		List<UARTotalInfo> list = new ArrayList<>();
		HashMap<String, UARTotalInfo> existingMap = new HashMap<>();
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			String sql = "";
			if( nCustNo == 0 )
			{
				sql =	" SELECT	* " +
						" FROM		" + TABLE_UARTOTAL_INFO + " WHERE " + KEY_TRXN_NO + " = " + nTrxnNo;
			}
			else
			{
				sql =	" SELECT	* " +
						" FROM		" + TABLE_UARTOTAL_INFO +
						" WHERE		" + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_TRXN_NO + " = " + nTrxnNo;
			}
			Cursor cursor = mDb.rawQuery(sql, null);

			try {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						UARTotalInfo info = new UARTotalInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
						info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
						info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
						info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
						info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
						info.nTotalDue = cursor.getFloat(cursor.getColumnIndex(KEY_TOTAL_DUE));
						info.nTotalUnalloc = cursor.getFloat(cursor.getColumnIndex(KEY_TOTAL_UNALLOC));
						info.nTotalCash = cursor.getFloat(cursor.getColumnIndex(KEY_TOTAL_CASH));
						info.nTotalCheque = cursor.getFloat(cursor.getColumnIndex(KEY_TOTAL_CHEQUE));
						info.nInvoiceTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_INVOICE_TRXN_NO));
						info.nInvoiceAmount = cursor.getFloat(cursor.getColumnIndex(KEY_INVOICE_AMOUNT));
						info.bIsVisitedDelivery = cursor.getInt(cursor.getColumnIndex(KEY_VISIT_DELIVERY)) == 1 ? true : false;
						info.bIsVisitedPickup = cursor.getInt(cursor.getColumnIndex(KEY_VISIT_PICKUP)) == 1 ? true : false;
						info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
						info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
						info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
						cursor.moveToNext();

						String key = info.nTrxnNo + "-" + info.strTrxnTime;
						if (existingMap.get(key) == null) {
							list.add(info);
							existingMap.put(key, info);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}

		}
		return list;
	}

	public long insertUARTotalInfo(UARTotalInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		values.put(KEY_TOTAL_DUE, info.nTotalDue);
		values.put(KEY_TOTAL_UNALLOC, info.nTotalUnalloc);
		values.put(KEY_TOTAL_CASH, info.nTotalCash);
		values.put(KEY_TOTAL_CHEQUE, info.nTotalCheque);
		values.put(KEY_INVOICE_TRXN_NO, info.nInvoiceTrxnNo);
		values.put(KEY_INVOICE_AMOUNT, info.nInvoiceAmount);
		values.put(KEY_VISIT_DELIVERY, info.bIsVisitedDelivery ? 1 : 0);
		values.put(KEY_VISIT_PICKUP, info.bIsVisitedPickup ? 1 : 0);
		values.put(KEY_DOC_NO, info.strDocNo);
		values.put(KEY_DOC_TYPE, info.strDocType);
		values.put(KEY_REFERENCE, info.strReference);
		return mDb.insert(TABLE_UARTOTAL_INFO, null, values);
	}

	public long updateUARTotalInfo(UARTotalInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		values.put(KEY_TOTAL_DUE, info.nTotalDue);
		values.put(KEY_TOTAL_UNALLOC, info.nTotalUnalloc);
		values.put(KEY_TOTAL_CASH, info.nTotalCash);
		values.put(KEY_TOTAL_CHEQUE, info.nTotalCheque);
		values.put(KEY_INVOICE_TRXN_NO, info.nInvoiceTrxnNo);
		values.put(KEY_INVOICE_AMOUNT, info.nInvoiceAmount);
		values.put(KEY_VISIT_DELIVERY, info.bIsVisitedDelivery ? 1 : 0);
		values.put(KEY_VISIT_PICKUP, info.bIsVisitedPickup ? 1 : 0);
		values.put(KEY_DOC_NO, info.strDocNo);
		values.put(KEY_DOC_TYPE, info.strDocType);
		values.put(KEY_REFERENCE, info.strReference);
		return mDb.update(TABLE_UARTOTAL_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}

	private boolean deleteAllUARTotalInfo(int nCustNo) throws SQLException {
		return mDb.delete(TABLE_UARTOTAL_INFO, KEY_CUST_NO + " = " + nCustNo, null) > 0;
	}

	private boolean deleteAllUARTotalInfo() throws SQLException {
		return mDb.delete(TABLE_UARTOTAL_INFO, null, null) > 0;
	}

	// UARPaymentInfo
	public List<UARPaymentInfo> getUARPaymentList(long nTrxnNo) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_UARPAYMENT_INFO +
						" WHERE		" + KEY_TRXN_NO + " = " + nTrxnNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UARPaymentInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UARPaymentInfo info = new UARPaymentInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.nPayType = cursor.getInt(cursor.getColumnIndex(KEY_PAY_TYPE));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.nTrxnAmount = cursor.getFloat(cursor.getColumnIndex(KEY_TRXN_AMOUNT));
					info.strInvoiceNo = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_NO));
					info.strBankNo = cursor.getString(cursor.getColumnIndex(KEY_BANK_NO));
					info.strAccount = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT));
					info.strCheckNo = cursor.getString(cursor.getColumnIndex(KEY_CHECK_NO));
					info.strCheckDate = cursor.getString(cursor.getColumnIndex(KEY_CHECK_DATE));
					info.strARNotes = cursor.getString(cursor.getColumnIndex(KEY_AR_NOTES));
					info.nProcessStatus = cursor.getInt(cursor.getColumnIndex(KEY_AR_PROCESS_STATUS));
					info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
					info.strInvoiceDate = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DATE));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<UARPaymentInfo> getUARPaymentList(String strInvoiceNo) {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_UARPAYMENT_INFO +
				" WHERE		" + KEY_INVOICE_NO + " = '" + strInvoiceNo + "'";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UARPaymentInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UARPaymentInfo info = new UARPaymentInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.nPayType = cursor.getInt(cursor.getColumnIndex(KEY_PAY_TYPE));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.nTrxnAmount = cursor.getFloat(cursor.getColumnIndex(KEY_TRXN_AMOUNT));
					info.strInvoiceNo = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_NO));
					info.strBankNo = cursor.getString(cursor.getColumnIndex(KEY_BANK_NO));
					info.strAccount = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT));
					info.strCheckNo = cursor.getString(cursor.getColumnIndex(KEY_CHECK_NO));
					info.strCheckDate = cursor.getString(cursor.getColumnIndex(KEY_CHECK_DATE));
					info.strARNotes = cursor.getString(cursor.getColumnIndex(KEY_AR_NOTES));
					info.nProcessStatus = cursor.getInt(cursor.getColumnIndex(KEY_AR_PROCESS_STATUS));
					info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
					info.strInvoiceDate = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DATE));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	// UARPaymentInfo
	public List<UARPaymentInfo> getUARPaymentList(int nCustNo, int nChainNo) {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_UARPAYMENT_INFO +
				" WHERE		" + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_CHAIN_NO + " = " + nChainNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UARPaymentInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UARPaymentInfo info = new UARPaymentInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.nPayType = cursor.getInt(cursor.getColumnIndex(KEY_PAY_TYPE));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.nTrxnAmount = cursor.getFloat(cursor.getColumnIndex(KEY_TRXN_AMOUNT));
					info.strInvoiceNo = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_NO));
					info.strBankNo = cursor.getString(cursor.getColumnIndex(KEY_BANK_NO));
					info.strAccount = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT));
					info.strCheckNo = cursor.getString(cursor.getColumnIndex(KEY_CHECK_NO));
					info.strCheckDate = cursor.getString(cursor.getColumnIndex(KEY_CHECK_DATE));
					info.strARNotes = cursor.getString(cursor.getColumnIndex(KEY_AR_NOTES));
					info.nProcessStatus = cursor.getInt(cursor.getColumnIndex(KEY_AR_PROCESS_STATUS));
					info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
					info.strInvoiceDate = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DATE));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public int updateUARPaymentInfo(UARPaymentInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_DESCRIPTION, info.strDescription);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_AMOUNT, info.nTrxnAmount);
		values.put(KEY_INVOICE_NO, info.strInvoiceNo);
		values.put(KEY_BANK_NO, info.strBankNo);
		values.put(KEY_ACCOUNT, info.strAccount);
		values.put(KEY_CHECK_NO, info.strCheckNo);
		values.put(KEY_CHECK_DATE, info.strCheckDate);
		values.put(KEY_AR_NOTES, info.strARNotes);
		values.put(KEY_AR_PROCESS_STATUS, info.nProcessStatus);
		values.put(KEY_PAY_TYPE, info.nPayType);
		values.put(KEY_DOC_NO, info.strDocNo);
		values.put(KEY_INVOICE_DATE, info.strInvoiceDate);
		return mDb.update(TABLE_UARPAYMENT_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}

	public long insertUARPaymentInfo(UARPaymentInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_DESCRIPTION, info.strDescription);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_AMOUNT, info.nTrxnAmount);
		values.put(KEY_INVOICE_NO, info.strInvoiceNo);
		values.put(KEY_BANK_NO, info.strBankNo);
		values.put(KEY_ACCOUNT, info.strAccount);
		values.put(KEY_CHECK_NO, info.strCheckNo);
		values.put(KEY_CHECK_DATE, info.strCheckDate);
		values.put(KEY_AR_NOTES, info.strARNotes);
		values.put(KEY_AR_PROCESS_STATUS, info.nProcessStatus);
		values.put(KEY_PAY_TYPE, info.nPayType);
		values.put(KEY_DOC_NO, info.strDocNo);
		values.put(KEY_INVOICE_DATE, info.strInvoiceDate);
		return mDb.insert(TABLE_UARPAYMENT_INFO, null, values);
	}



	private boolean deleteAllUARPaymentInfo(int nCustNo) throws SQLException {
		return mDb.delete(TABLE_UARPAYMENT_INFO, KEY_CUST_NO + " = " + nCustNo, null) > 0;
	}

	private boolean deleteAllUARPaymentInfo() throws SQLException {
		return mDb.delete(TABLE_UARPAYMENT_INFO, null, null) > 0;
	}

	// UServicesInfo
	public UServicesInfo getUServiceInfo(int nCustNo, int nChainNo)
	{
		String sql =	"SELECT * FROM " + TABLE_USERVICES_INFO + " WHERE " + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_CHAIN_NO + " = " + nChainNo;
		Cursor cursor = mDb.rawQuery(sql, null);

		UServicesInfo info = null;
		try {
			if(cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				info = new UServicesInfo();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
				info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
				info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
				info.nPeriod = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD));
				info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
				info.nDone = cursor.getInt(cursor.getColumnIndex(KEY_DONE));
				info.strReason = cursor.getString(cursor.getColumnIndex(KEY_REASON));
				info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
				info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
				info.bFromEndTrip = cursor.getInt(cursor.getColumnIndex(KEY_END_TRIP)) == 1;
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(cursor != null){
				cursor.close();
			}
		}
		return info;
	}

	public List<UServicesInfo> getUServicesList(boolean bEndTrip) {

		String sql =	" SELECT	* " +
						" FROM		" + TABLE_USERVICES_INFO;
		if(bEndTrip)
		{
			sql += " WHERE " + KEY_END_TRIP + " = 1";
		}
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UServicesInfo> list = new ArrayList<UServicesInfo>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++) {
					UServicesInfo info = new UServicesInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nPeriod = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD));
					info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
					info.nDone = cursor.getInt(cursor.getColumnIndex(KEY_DONE));
					info.strReason = cursor.getString(cursor.getColumnIndex(KEY_REASON));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.bFromEndTrip = cursor.getInt(cursor.getColumnIndex(KEY_END_TRIP)) == 1;
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<UServicesInfo> getUServicesList(MakeZipFileInfo zipFileInfo) {
		List<UServicesInfo> list = new ArrayList<>();
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			String sql =	" SELECT	* " +
					" FROM		" + TABLE_USERVICES_INFO + " WHERE " + KEY_TRXN_NO + " = " + nTrxnNo;
			Cursor cursor = mDb.rawQuery(sql, null);
			try {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i=0; i<cursor.getCount(); i++) {
						UServicesInfo info = new UServicesInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
						info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
						info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
						info.nPeriod = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD));
						info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
						info.nDone = cursor.getInt(cursor.getColumnIndex(KEY_DONE));
						info.strReason = cursor.getString(cursor.getColumnIndex(KEY_REASON));
						info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
						info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
						info.bFromEndTrip = cursor.getInt(cursor.getColumnIndex(KEY_END_TRIP)) == 1;
						cursor.moveToNext();
						list.add(info);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		return list;
	}

	public long insertUServicesInfo(UServicesInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_PERIOD, info.nPeriod);
		values.put(KEY_SEQ_NO, info.nSeqNo);
		values.put(KEY_DONE, info.nDone);
		values.put(KEY_REASON, info.strReason);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		values.put(KEY_END_TRIP, info.bFromEndTrip ? 1 : 0);
		return mDb.insert(TABLE_USERVICES_INFO, null, values);
	}

	public boolean deleteAllUServicesInfo() throws SQLException {
		return mDb.delete(TABLE_USERVICES_INFO, null, null) > 0;
	}

	// UCollectionsDInfo
	public List<UCollectionsDInfo> getUCollectionsDList() {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_UCOLLECTIONSD_INFO;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UCollectionsDInfo> list = new ArrayList<UCollectionsDInfo>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++) {
					UCollectionsDInfo info = new UCollectionsDInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
					info.nPayType = cursor.getInt(cursor.getColumnIndex(KEY_PAY_TYPE));
					info.strCustomerName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
					info.strBankNo = cursor.getString(cursor.getColumnIndex(KEY_BANK_NO));
					info.strAccount = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT));
					info.strCheckNo = cursor.getString(cursor.getColumnIndex(KEY_CHECK_NO));
					info.strCheckDate = cursor.getString(cursor.getColumnIndex(KEY_CHECK_DATE));
					info.nTrxnAmount = cursor.getFloat(cursor.getColumnIndex(KEY_TRXN_AMOUNT));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strEquipmentNo = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_NO));
					info.strInvoiceNo = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_NO));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	// UCollectionsDInfo
	public List<UCollectionsDInfo> getUCollectionsDList(int nCustNo, int nChainNo) {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_UCOLLECTIONSD_INFO + " WHERE " + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_CHAIN_NO + " = " + nChainNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UCollectionsDInfo> list = new ArrayList<UCollectionsDInfo>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++) {
					UCollectionsDInfo info = new UCollectionsDInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
					info.nPayType = cursor.getInt(cursor.getColumnIndex(KEY_PAY_TYPE));
					info.strCustomerName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
					info.strBankNo = cursor.getString(cursor.getColumnIndex(KEY_BANK_NO));
					info.strAccount = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT));
					info.strCheckNo = cursor.getString(cursor.getColumnIndex(KEY_CHECK_NO));
					info.strCheckDate = cursor.getString(cursor.getColumnIndex(KEY_CHECK_DATE));
					info.nTrxnAmount = cursor.getFloat(cursor.getColumnIndex(KEY_TRXN_AMOUNT));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strEquipmentNo = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_NO));
					info.strInvoiceNo = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_NO));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public UCollectionsDInfo getUCollectionDInfo(int nCustNo, int nChainNo, String strEquipNo)
	{
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_UCOLLECTIONSD_INFO + " WHERE " + KEY_TRXN_NO + " = -1 AND " + KEY_CUST_NO + " = " + nCustNo + " AND " +
				KEY_CHAIN_NO + " = " + nChainNo + " AND " + KEY_EQUIPMENT_NO + " = '" + strEquipNo + "'";
		Cursor cursor = mDb.rawQuery(sql, null);
		UCollectionsDInfo info = null;
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			try{
				info = new UCollectionsDInfo();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
				info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
				info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
				info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
				info.nPayType = cursor.getInt(cursor.getColumnIndex(KEY_PAY_TYPE));
				info.strCustomerName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
				info.strBankNo = cursor.getString(cursor.getColumnIndex(KEY_BANK_NO));
				info.strAccount = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT));
				info.strCheckNo = cursor.getString(cursor.getColumnIndex(KEY_CHECK_NO));
				info.strCheckDate = cursor.getString(cursor.getColumnIndex(KEY_CHECK_DATE));
				info.nTrxnAmount = cursor.getFloat(cursor.getColumnIndex(KEY_TRXN_AMOUNT));
				info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
				info.strEquipmentNo = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_NO));
				info.strInvoiceNo = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_NO));
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		try {

			cursor.close();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		return info;
	}
	public long insertUCollectionsDInfo(UCollectionsDInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_SEQ_NO, info.nSeqNo);
		values.put(KEY_PAY_TYPE, info.nPayType);
		values.put(KEY_NAME, info.strCustomerName);
		values.put(KEY_BANK_NO, info.strBankNo);
		values.put(KEY_ACCOUNT, info.strAccount);
		values.put(KEY_CHECK_NO, info.strCheckNo);
		values.put(KEY_CHECK_DATE, info.strCheckDate);
		values.put(KEY_TRXN_AMOUNT, info.nTrxnAmount);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_EQUIPMENT_NO, info.strEquipmentNo);
		values.put(KEY_INVOICE_NO, info.strInvoiceNo);
		return mDb.insert(TABLE_UCOLLECTIONSD_INFO, null, values);
	}

	public long updateUCollectionsDInfo(UCollectionsDInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_SEQ_NO, info.nSeqNo);
		values.put(KEY_PAY_TYPE, info.nPayType);
		values.put(KEY_NAME, info.strCustomerName);
		values.put(KEY_BANK_NO, info.strBankNo);
		values.put(KEY_ACCOUNT, info.strAccount);
		values.put(KEY_CHECK_NO, info.strCheckNo);
		values.put(KEY_CHECK_DATE, info.strCheckDate);
		values.put(KEY_TRXN_AMOUNT, info.nTrxnAmount);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_EQUIPMENT_NO, info.strEquipmentNo);
		values.put(KEY_INVOICE_NO, info.strInvoiceNo);
		return mDb.update(TABLE_UCOLLECTIONSD_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}

	public boolean deleteAllUCollectionsDInfo() throws SQLException {
		return mDb.delete(TABLE_UCOLLECTIONSD_INFO, null, null) > 0;
	}

	// DocTextInfo
	public List<DocTextInfo> getDocTextListWithTypeLine(int nTextType, int nLine) {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_DOCTEXT_INFO + " WHERE " + KEY_CHAIN_NO + " = " + 0 + " AND " + KEY_CUST_NO + " = " + 0 + " AND " + KEY_TEXT_TYPE + " = " + nTextType + " AND " + KEY_LINE + " = " + nLine;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<DocTextInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					DocTextInfo info = new DocTextInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTextType = cursor.getInt(cursor.getColumnIndex(KEY_TEXT_TYPE));
					info.nLine = cursor.getInt(cursor.getColumnIndex(KEY_LINE));
					info.strDocText = cursor.getString(cursor.getColumnIndex(KEY_DOC_TEXT));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strSubType = cursor.getString(cursor.getColumnIndex(KEY_SUB_TYPE));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<DocTextInfo> getDocTextList(int nTextType) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_DOCTEXT_INFO + " WHERE " + KEY_CHAIN_NO + " = " + 0 + " AND " + KEY_CUST_NO + " = " + 0 + " AND " + KEY_TEXT_TYPE + " = " + nTextType;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<DocTextInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					DocTextInfo info = new DocTextInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTextType = cursor.getInt(cursor.getColumnIndex(KEY_TEXT_TYPE));
					info.nLine = cursor.getInt(cursor.getColumnIndex(KEY_LINE));
					info.strDocText = cursor.getString(cursor.getColumnIndex(KEY_DOC_TEXT));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strSubType = cursor.getString(cursor.getColumnIndex(KEY_SUB_TYPE));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<DocTextInfo> getDocTextList(int nCustNo, int nTextType) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_DOCTEXT_INFO + " WHERE " + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_TEXT_TYPE + " = " + nTextType;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<DocTextInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					DocTextInfo info = new DocTextInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTextType = cursor.getInt(cursor.getColumnIndex(KEY_TEXT_TYPE));
					info.nLine = cursor.getInt(cursor.getColumnIndex(KEY_LINE));
					info.strDocText = cursor.getString(cursor.getColumnIndex(KEY_DOC_TEXT));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strSubType = cursor.getString(cursor.getColumnIndex(KEY_SUB_TYPE));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<DocTextInfo> getDocTextList(int nCustNo, int nChainNo, int nTextType) {
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_DOCTEXT_INFO + " WHERE " + KEY_CHAIN_NO + " = " + nChainNo + " AND " + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_TEXT_TYPE + " = " + nTextType;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<DocTextInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					DocTextInfo info = new DocTextInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTextType = cursor.getInt(cursor.getColumnIndex(KEY_TEXT_TYPE));
					info.nLine = cursor.getInt(cursor.getColumnIndex(KEY_LINE));
					info.strDocText = cursor.getString(cursor.getColumnIndex(KEY_DOC_TEXT));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strSubType = cursor.getString(cursor.getColumnIndex(KEY_SUB_TYPE));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public long insertDocTextInfo(DocTextInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TEXT_TYPE, info.nTextType);
		values.put(KEY_LINE, info.nLine);
		values.put(KEY_DOC_TEXT, info.strDocText);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_SUB_TYPE, info.strSubType);
		return mDb.insert(TABLE_DOCTEXT_INFO, null, values);
	}

	public boolean deleteAllDocTextInfo() throws SQLException {
		return mDb.delete(TABLE_DOCTEXT_INFO, null, null) > 0;
	}

	// UCustomerFMInfo
	public List<UCustomerFMInfo> getUCustomerFMList(int nCustNo) {
		String sql = "";
		if( nCustNo == 0 )
		{
			sql =	" SELECT	* " +
					" FROM		" + TABLE_UCUSTOMERFM_INFO;
		}
		else
		{
			sql =	" SELECT	* " +
					" FROM		" + TABLE_UCUSTOMERFM_INFO +
					" WHERE " + KEY_CUST_NO + " = " + nCustNo;
		}
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UCustomerFMInfo> list = new ArrayList<UCustomerFMInfo>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++) {
					UCustomerFMInfo info = new UCustomerFMInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strEditType = cursor.getString(cursor.getColumnIndex(KEY_EDIT_TYPE));
					info.strName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
					info.strCompanyTaxID = cursor.getString(cursor.getColumnIndex(KEY_COMPANY_TAX_ID));
					info.strAddress1 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS1));
					info.strAddress2 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS2));
					info.strCity = cursor.getString(cursor.getColumnIndex(KEY_CITY));
					info.strShipToState = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_STATE));
					info.strShipToZip = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_ZIP));
					info.strPhone = cursor.getString(cursor.getColumnIndex(KEY_PHONE));
					info.strContactType = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_TYPE));
					info.strContactName = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_NAME));
					info.strContactPhoneNumber = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_PHONE_NUMBER));
					info.strContactEmailAddress = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_EMAIL_ADDRESS));
					info.strStartDate = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_STARTDATE));
					info.strStartTime = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_STARTTIME));
					info.strEndDate = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_ENDDATE));
					info.strEndTime = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_ENDTIME));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_TRXNDATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_TRXNTIME));
					info.strDeliveryWindowFrom = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_FROM));
					info.strDeliveryWindowTo = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_TO));
					info.strStripeInfo = cursor.getString(cursor.getColumnIndex(KEY_STRIPE_INFO));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
					info.nTemplateChainNo = cursor.getInt(cursor.getColumnIndex(KEY_TEMP_CHAIN_NO));
					info.nTemplateCustNo = cursor.getInt(cursor.getColumnIndex(KEY_TEMP_CUST_NO));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<UCustomerFMInfo> getUCustomerFMList(int nCustNo, long nTrxnNo) {
		String sql = "";
		sql =	" SELECT	* " +
				" FROM		" + TABLE_UCUSTOMERFM_INFO +
				" WHERE " + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_TRXN_NO + " = " + nTrxnNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UCustomerFMInfo> list = new ArrayList<UCustomerFMInfo>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++) {
					UCustomerFMInfo info = new UCustomerFMInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strEditType = cursor.getString(cursor.getColumnIndex(KEY_EDIT_TYPE));
					info.strName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
					info.strCompanyTaxID = cursor.getString(cursor.getColumnIndex(KEY_COMPANY_TAX_ID));
					info.strAddress1 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS1));
					info.strAddress2 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS2));
					info.strCity = cursor.getString(cursor.getColumnIndex(KEY_CITY));
					info.strShipToState = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_STATE));
					info.strShipToZip = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_ZIP));
					info.strPhone = cursor.getString(cursor.getColumnIndex(KEY_PHONE));
					info.strContactType = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_TYPE));
					info.strContactName = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_NAME));
					info.strContactPhoneNumber = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_PHONE_NUMBER));
					info.strContactEmailAddress = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_EMAIL_ADDRESS));
					info.strStartDate = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_STARTDATE));
					info.strStartTime = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_STARTTIME));
					info.strEndDate = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_ENDDATE));
					info.strEndTime = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_ENDTIME));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_TRXNDATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_TRXNTIME));
					info.strDeliveryWindowFrom = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_FROM));
					info.strDeliveryWindowTo = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_TO));
					info.strStripeInfo = cursor.getString(cursor.getColumnIndex(KEY_STRIPE_INFO));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
					info.nTemplateChainNo = cursor.getInt(cursor.getColumnIndex(KEY_TEMP_CHAIN_NO));
					info.nTemplateCustNo = cursor.getInt(cursor.getColumnIndex(KEY_TEMP_CUST_NO));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public long insertUCustomerFMInfo(UCustomerFMInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_EDIT_TYPE, info.strEditType);
		values.put(KEY_NAME, info.strName);
		values.put(KEY_COMPANY_TAX_ID, info.strCompanyTaxID);
		values.put(KEY_ADDRESS1, info.strAddress1);
		values.put(KEY_ADDRESS2, info.strAddress2);
		values.put(KEY_CITY, info.strCity);
		values.put(KEY_SHIP_TO_STATE, info.strShipToState);
		values.put(KEY_SHIP_TO_ZIP, info.strShipToZip);
		values.put(KEY_PHONE, info.strPhone);
		values.put(KEY_CONTACT_TYPE, info.strContactType);
		values.put(KEY_CONTACT_NAME, info.strContactName);
		values.put(KEY_CONTACT_PHONE_NUMBER, info.strContactPhoneNumber);
		values.put(KEY_CONTACT_EMAIL_ADDRESS, info.strContactEmailAddress);
		values.put(KEY_CONTACT_STARTDATE, info.strStartDate);
		values.put(KEY_CONTACT_STARTTIME, info.strStartTime);
		values.put(KEY_CONTACT_ENDDATE, info.strEndDate);
		values.put(KEY_CONTACT_ENDTIME, info.strEndTime);
		values.put(KEY_CONTACT_TRXNDATE, info.strTrxnDate);
		values.put(KEY_CONTACT_TRXNTIME, info.strTrxnTime);
		values.put(KEY_DELIVERY_WINDOW_FROM, info.strDeliveryWindowFrom);
		values.put(KEY_DELIVERY_WINDOW_TO, info.strDeliveryWindowTo);
		values.put(KEY_STRIPE_INFO, info.strStripeInfo);
		values.put(KEY_TYPE, info.strType);
		values.put(KEY_USER1, info.strUser1);
		values.put(KEY_TEMP_CHAIN_NO, info.nTemplateChainNo);
		values.put(KEY_TEMP_CUST_NO, info.nTemplateCustNo);


		return mDb.insert(TABLE_UCUSTOMERFM_INFO, null, values);
	}

	private boolean deleteAllUCustomerFMInfo() throws SQLException {
		return mDb.delete(TABLE_UCUSTOMERFM_INFO, null, null) > 0;
	}

	// delete All
	public void deleteAllUploadData(int nCustNo) {
		deleteAllUGPSInfo(nCustNo);
		deleteAllUMessagesInfo(nCustNo);
		deleteAllUTransactionsInfo(nCustNo);
		deleteAllUFileTransactionsInfo(nCustNo);
		deleteAllUCameraInfo(nCustNo);
		//deleteAllUARTotalInfo(nCustNo);
		//deleteAllUARPaymentInfo(nCustNo);
		deleteAllUSurveysInfo(nCustNo);
		deleteAllUInvenHInfo(nCustNo, false);
		//deleteAllUCustomerFMInfo();
		deleteAllUStockDInfo();
		deleteAllUStockHInfo();
		deleteAllUConsignmentInfo();
		deleteAllURouteSchsHeader();
		deleteAllURouteSchsBody();
		deleteAllUVisitInfo();
		if (Global.gIsEndTrip == true) {
			deleteAllULoadBlot();
			deleteAllUCustomerFMInfo();
			deleteAllUARTotalInfo();
			deleteAllUARPaymentInfo();
			deleteAllUOrderHInfo();
			deleteAllUServicesInfo();
			deleteAllUCollectionsDInfo();
			deleteStatusInfo();
			deleteAllUAssetsInfo();
			Global.gIsEndTrip = false;
		}
	}

	public void deleteAllUploadData(int nCustNo, MakeZipFileInfo zipFileInfo) {
		deleteAllUGPSInfo(nCustNo, zipFileInfo);
		deleteAllUMessagesInfo(nCustNo, zipFileInfo);
		deleteAllUTransactionsInfo(nCustNo, zipFileInfo);
		deleteAllUFileTransactionsInfo(nCustNo, zipFileInfo);
		deleteAllUCameraInfo(nCustNo, zipFileInfo);
		//deleteAllUARTotalInfo(nCustNo);
		//deleteAllUARPaymentInfo(nCustNo);
		deleteAllUSurveysInfo(nCustNo, zipFileInfo);
		deleteAllUInvenHInfo(nCustNo, false, zipFileInfo);
		//deleteAllUCustomerFMInfo();
		deleteAllUStockDInfo(zipFileInfo);
		deleteAllUStockHInfo(zipFileInfo);
		deleteAllUConsignmentInfo(zipFileInfo);
		deleteAllUVisitInfo(zipFileInfo);
		deleteULoadBlot(zipFileInfo);
		if (Global.gIsEndTrip == true) {
			deleteAllUCustomerFMInfo();
			deleteAllUARTotalInfo();
			deleteAllUARPaymentInfo();
			deleteAllUOrderHInfo();
			deleteAllUServicesInfo();
			deleteAllUCollectionsDInfo();
			deleteStatusInfo();
			Global.gIsEndTrip = false;
		}
	}

	// CmpyContactInfo
	public CmpyContactInfo getCmpyContactInfo()
	{
		CmpyContactInfo info = new CmpyContactInfo();
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_CMPYCONTACT_INFO + 
						" LIMIT		0, 1 ";
		Cursor cursor = mDb.rawQuery(sql, null);
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.nCompanyNumber = cursor.getInt(cursor.getColumnIndex(KEY_COMPANY_NUMBER));
				info.strCompanyName = cursor.getString(cursor.getColumnIndex(KEY_COMPANY_NAME));
				info.strCompanyWeb = cursor.getString(cursor.getColumnIndex(KEY_COMPANY_WEB));
				info.strCompanyEmail = cursor.getString(cursor.getColumnIndex(KEY_COMPANY_EMAIL));
				info.strCompanyPhone1 = cursor.getString(cursor.getColumnIndex(KEY_COMPANY_PHONE1));
				info.strCompanyPhone2 = cursor.getString(cursor.getColumnIndex(KEY_COMPANY_PHONE2));
				info.strCompanyInfo = cursor.getString(cursor.getColumnIndex(KEY_COMPANY_INFO));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return info;
	}
	
	public long insertCmpyContactInfo(CmpyContactInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_COMPANY_NUMBER, info.nCompanyNumber);
		values.put(KEY_COMPANY_NAME, info.strCompanyName);
		values.put(KEY_COMPANY_WEB, info.strCompanyWeb);
		values.put(KEY_COMPANY_EMAIL, info.strCompanyEmail);
		values.put(KEY_COMPANY_PHONE1, info.strCompanyPhone1);
		values.put(KEY_COMPANY_PHONE2, info.strCompanyPhone2);
		values.put(KEY_COMPANY_INFO, info.strCompanyInfo);
		return mDb.insert(TABLE_CMPYCONTACT_INFO, null, values);
	}
	
	public boolean deleteAllCompany()
	{
		return mDb.delete(TABLE_CMPYCONTACT_INFO, null, null) > 0;
	}
	
	// ARHeaderInfo
	public List<ARHeaderInfo> getARHeaderInfo(int nChainNo, int nCustNo)
	{

		String sql =	" SELECT	* " +
						" FROM		" + TABLE_ARHEADER_INFO + " WHERE "
						+ KEY_CHAIN_NO + " = " + nChainNo + " AND " + KEY_CUST_NO + " = " + nCustNo;
		List<ARHeaderInfo> infos = new ArrayList<>();
		Cursor cursor = mDb.rawQuery(sql, null);
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				for(int i = 0 ; i < cursor.getCount(); i++)
				{
					ARHeaderInfo info = new ARHeaderInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strInvDate = cursor.getString(cursor.getColumnIndex(KEY_INV_DATE));
					info.strInvNo = cursor.getString(cursor.getColumnIndex(KEY_INV_NO));
					info.strARTrxnType = cursor.getString(cursor.getColumnIndex(KEY_AR_TRXN_TYPE));
					info.nTrxnAmount = cursor.getInt(cursor.getColumnIndex(KEY_TRXN_AMOUNT));
					info.nDiscAmount = cursor.getInt(cursor.getColumnIndex(KEY_DISC_AMOUNT));
					info.strDiscStartDate = cursor.getString(cursor.getColumnIndex(KEY_DISC_START_DATE));
					info.strDiscEndDate = cursor.getString(cursor.getColumnIndex(KEY_DISC_END_DATE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER_1));
					info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER_2));
					info.nProcessStatus = cursor.getInt(cursor.getColumnIndex(KEY_AR_PROCESS_STATUS));
					infos.add(info);

					cursor.moveToNext();
				}

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return infos;
	}

	public List<ARHeaderInfo> getCompletedARHeaderInfo()
	{

		String sql =	" SELECT	* " +
				" FROM		" + TABLE_ARHEADER_INFO;
		List<ARHeaderInfo> infos = new ArrayList<>();
		Cursor cursor = mDb.rawQuery(sql, null);
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				for(int i = 0 ; i < cursor.getCount(); i++)
				{
					ARHeaderInfo info = new ARHeaderInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strInvDate = cursor.getString(cursor.getColumnIndex(KEY_INV_DATE));
					info.strInvNo = cursor.getString(cursor.getColumnIndex(KEY_INV_NO));
					info.strARTrxnType = cursor.getString(cursor.getColumnIndex(KEY_AR_TRXN_TYPE));
					info.nTrxnAmount = cursor.getInt(cursor.getColumnIndex(KEY_TRXN_AMOUNT));
					info.nDiscAmount = cursor.getInt(cursor.getColumnIndex(KEY_DISC_AMOUNT));
					info.strDiscStartDate = cursor.getString(cursor.getColumnIndex(KEY_DISC_START_DATE));
					info.strDiscEndDate = cursor.getString(cursor.getColumnIndex(KEY_DISC_END_DATE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER_1));
					info.strUser2 = cursor.getString(cursor.getColumnIndex(KEY_USER_2));
					info.nProcessStatus = cursor.getInt(cursor.getColumnIndex(KEY_AR_PROCESS_STATUS));
					infos.add(info);

					cursor.moveToNext();
				}

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return infos;
	}

	public long insertARHeaderInfo(ARHeaderInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_DESCRIPTION, info.strDescription);
		values.put(KEY_INV_DATE, info.strInvDate);
		values.put(KEY_INV_NO, info.strInvNo);
		values.put(KEY_AR_TRXN_TYPE, info.strARTrxnType);
		values.put(KEY_TRXN_AMOUNT, info.nTrxnAmount);
		values.put(KEY_DISC_AMOUNT, info.nDiscAmount);
		values.put(KEY_DISC_START_DATE, info.strDiscStartDate);
		values.put(KEY_DISC_END_DATE, info.strDiscEndDate);
		values.put(KEY_AR_PROCESS_STATUS, info.nProcessStatus);
		values.put(KEY_USER_1, info.strUser1);
		values.put(KEY_USER_2, info.strUser2);
		
		return mDb.insert(TABLE_ARHEADER_INFO, null, values);
	}

	public int updateARHeaderInfo(ARHeaderInfo info){
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_DESCRIPTION, info.strDescription);
		values.put(KEY_INV_DATE, info.strInvDate);
		values.put(KEY_INV_NO, info.strInvNo);
		values.put(KEY_AR_TRXN_TYPE, info.strARTrxnType);
		values.put(KEY_TRXN_AMOUNT, info.nTrxnAmount);
		values.put(KEY_DISC_AMOUNT, info.nDiscAmount);
		values.put(KEY_DISC_START_DATE, info.strDiscStartDate);
		values.put(KEY_DISC_END_DATE, info.strDiscEndDate);
		values.put(KEY_AR_PROCESS_STATUS, info.nProcessStatus);
		values.put(KEY_USER_1, info.strUser1);
		values.put(KEY_USER_2, info.strUser2);

		return mDb.update(TABLE_ARHEADER_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}

	public void deleteARHeaderInfo(ARHeaderInfo info){
		mDb.delete(TABLE_ARHEADER_INFO, KEY_IDX + " = " + info.nIdx, null);
	}
	
	public boolean deleteAllARHeaderInfo()
	{
		return mDb.delete(TABLE_ARHEADER_INFO, null, null) > 0;
	}

	public boolean deleteProcessedARHeader(int nCustNo, int nChainNo)
	{
		return mDb.delete(TABLE_ARHEADER_INFO, KEY_CHAIN_NO + " = " + nChainNo + " AND " + KEY_CUST_NO + " = " + nCustNo + " AND " +
		KEY_AR_PROCESS_STATUS + " = 3", null) > 0;
	}
	
	//Status Table
	public List<StatusInfo> getStatusInfo()
	{
		List<StatusInfo> infos = new ArrayList<StatusInfo>();
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_STATUS_INFO + " WHERE " + KEY_UPLOAD_STATUS + " = 0";
		Cursor cursor = mDb.rawQuery(sql, null);
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					StatusInfo info = new StatusInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nPaymentStatus = cursor.getInt(cursor.getColumnIndex(KEY_PAYMENT_STATUS));
					info.nUploadStatus = cursor.getInt(cursor.getColumnIndex(KEY_UPLOAD_STATUS));
					info.nUploadedStatus = cursor.getInt(cursor.getColumnIndex(KEY_UPLOADED_STATUS));
					info.nPrepareStatus = cursor.getInt(cursor.getColumnIndex(KEY_PREPARE_STATUS));
					info.strInvoiceNo = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_NO));
					info.strCreateDate = cursor.getString(cursor.getColumnIndex(KEY_STATUS_CREATE_DATE));
					info.strPaymentDate = cursor.getString(cursor.getColumnIndex(KEY_STATUS_PAYMENT_DATE));
					info.strUploadDate = cursor.getString(cursor.getColumnIndex(KEY_STATUS_UPLOAD_DATE));
					info.strPrepareFileName = cursor.getString(cursor.getColumnIndex(KEY_PREPARE_FILE_NAME));
					
					cursor.moveToNext();
					infos.add(info);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return infos;
	}
	
	public List<StatusInfo> getStatusInfoForUploadedStatus()
	{
		List<StatusInfo> infos = new ArrayList<StatusInfo>();
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_STATUS_INFO + " WHERE " + KEY_UPLOADED_STATUS + " = 0";
		Cursor cursor = mDb.rawQuery(sql, null);
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					StatusInfo info = new StatusInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nPaymentStatus = cursor.getInt(cursor.getColumnIndex(KEY_PAYMENT_STATUS));
					info.nUploadStatus = cursor.getInt(cursor.getColumnIndex(KEY_UPLOAD_STATUS));
					info.nUploadedStatus = cursor.getInt(cursor.getColumnIndex(KEY_UPLOADED_STATUS));
					info.nPrepareStatus = cursor.getInt(cursor.getColumnIndex(KEY_PREPARE_STATUS));
					info.strInvoiceNo = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_NO));
					info.strCreateDate = cursor.getString(cursor.getColumnIndex(KEY_STATUS_CREATE_DATE));
					info.strPaymentDate = cursor.getString(cursor.getColumnIndex(KEY_STATUS_PAYMENT_DATE));
					info.strUploadDate = cursor.getString(cursor.getColumnIndex(KEY_STATUS_UPLOAD_DATE));
					info.strPrepareFileName = cursor.getString(cursor.getColumnIndex(KEY_PREPARE_FILE_NAME));
					cursor.moveToNext();
					infos.add(info);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return infos;
	}
	
	public List<StatusInfo> getStatusInfoForPrepareStatus()
	{
		List<StatusInfo> infos = new ArrayList<StatusInfo>();
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_STATUS_INFO + " WHERE " + KEY_PREPARE_STATUS + " = 0";
		Cursor cursor = mDb.rawQuery(sql, null);
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					StatusInfo info = new StatusInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nPaymentStatus = cursor.getInt(cursor.getColumnIndex(KEY_PAYMENT_STATUS));
					info.nUploadStatus = cursor.getInt(cursor.getColumnIndex(KEY_UPLOAD_STATUS));
					info.nUploadedStatus = cursor.getInt(cursor.getColumnIndex(KEY_UPLOADED_STATUS));
					info.nPrepareStatus = cursor.getInt(cursor.getColumnIndex(KEY_PREPARE_STATUS));
					info.strInvoiceNo = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_NO));
					info.strCreateDate = cursor.getString(cursor.getColumnIndex(KEY_STATUS_CREATE_DATE));
					info.strPaymentDate = cursor.getString(cursor.getColumnIndex(KEY_STATUS_PAYMENT_DATE));
					info.strUploadDate = cursor.getString(cursor.getColumnIndex(KEY_STATUS_UPLOAD_DATE));
					info.strPrepareFileName = cursor.getString(cursor.getColumnIndex(KEY_PREPARE_FILE_NAME));
					cursor.moveToNext();
					infos.add(info);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return infos;
	}
	public StatusInfo getStatusInfo(int nCustNo)
	{
		StatusInfo info = null;
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_STATUS_INFO + " WHERE " + KEY_CUST_NO + " = " + nCustNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				info = new StatusInfo();
				cursor.moveToLast();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
				info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
				info.nPaymentStatus = cursor.getInt(cursor.getColumnIndex(KEY_PAYMENT_STATUS));
				info.nUploadStatus = cursor.getInt(cursor.getColumnIndex(KEY_UPLOAD_STATUS));
				info.nUploadedStatus = cursor.getInt(cursor.getColumnIndex(KEY_UPLOADED_STATUS));
				info.nPrepareStatus = cursor.getInt(cursor.getColumnIndex(KEY_PREPARE_STATUS));
				info.strInvoiceNo = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_NO));
				info.strCreateDate = cursor.getString(cursor.getColumnIndex(KEY_STATUS_CREATE_DATE));
				info.strPaymentDate = cursor.getString(cursor.getColumnIndex(KEY_STATUS_PAYMENT_DATE));
				info.strUploadDate = cursor.getString(cursor.getColumnIndex(KEY_STATUS_UPLOAD_DATE));
				info.strPrepareFileName = cursor.getString(cursor.getColumnIndex(KEY_PREPARE_FILE_NAME));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return info;
	}

	public long insertStatusInfo(StatusInfo info) {
		StatusInfo oldInfo = getStatusInfo(info.nCustNo);
		if( oldInfo != null )
		{
			info.nIdx = oldInfo.nIdx;
			return info.nIdx;
		}
		else
		{
			ContentValues values = new ContentValues();

			values.put(KEY_CUST_NO, info.nCustNo);
			values.put(KEY_CHAIN_NO, info.nChainNo);
			values.put(KEY_PAYMENT_STATUS, info.nPaymentStatus);
			values.put(KEY_UPLOAD_STATUS, info.nUploadStatus);
			values.put(KEY_PREPARE_STATUS, info.nPrepareStatus);
			values.put(KEY_INVOICE_NO, info.strInvoiceNo);
			values.put(KEY_STATUS_CREATE_DATE, info.strCreateDate);
			values.put(KEY_STATUS_PAYMENT_DATE, info.strPaymentDate);
			values.put(KEY_STATUS_UPLOAD_DATE, info.strUploadDate);
			values.put(KEY_UPLOADED_STATUS, info.nUploadedStatus);
			values.put(KEY_PREPARE_FILE_NAME, info.strPrepareFileName);
			return mDb.insert(TABLE_STATUS_INFO, null, values);
		}
	}

	public long updateStatusInfo(StatusInfo info) {
		ContentValues values = new ContentValues();
		
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_PAYMENT_STATUS, info.nPaymentStatus);
		values.put(KEY_UPLOAD_STATUS, info.nUploadStatus);
		values.put(KEY_PREPARE_STATUS, info.nPrepareStatus);
		values.put(KEY_INVOICE_NO, info.strInvoiceNo);
		values.put(KEY_STATUS_CREATE_DATE, info.strCreateDate);
		values.put(KEY_STATUS_PAYMENT_DATE, info.strPaymentDate);
		values.put(KEY_STATUS_UPLOAD_DATE, info.strUploadDate);
		values.put(KEY_UPLOADED_STATUS, info.nUploadedStatus);
		values.put(KEY_PREPARE_FILE_NAME, info.strPrepareFileName);
		return mDb.update(TABLE_STATUS_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}
	
	public boolean deleteStatusInfo()
	{
		return mDb.delete(TABLE_STATUS_INFO, null, null) > 0;
	}


	public List<AuthHeaderInfo> getAuthHeaderInfoList()
	{
		List<AuthHeaderInfo> authHeaderInfoList = new ArrayList<>();
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_AUTHHEADER_INFO + " WHERE " + KEY_AUTH_TYPE + " ='T'";
		Cursor cursor = mDb.rawQuery(sql, null);
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					AuthHeaderInfo info = new AuthHeaderInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nAuthGrp = cursor.getInt(cursor.getColumnIndex(KEY_AUTH_GRP));
					info.strAuthType = cursor.getString(cursor.getColumnIndex(KEY_AUTH_TYPE));
					cursor.moveToLast();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return authHeaderInfoList;
	}

	public AuthHeaderInfo getAuthHeaderInfo(int nAuthGrp)
	{
		AuthHeaderInfo info = null;
		String sql =	" SELECT	* " +
						" FROM		" + TABLE_AUTHHEADER_INFO + " WHERE " + KEY_AUTH_GRP + " = " + nAuthGrp;
		Cursor cursor = mDb.rawQuery(sql, null);
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				info = new AuthHeaderInfo();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.nAuthGrp = cursor.getInt(cursor.getColumnIndex(KEY_AUTH_GRP));
				info.strAuthType = cursor.getString(cursor.getColumnIndex(KEY_AUTH_TYPE));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return info;
	}

	public long insertAuthHeaderInfo(AuthHeaderInfo info) {

		ContentValues values = new ContentValues();

		values.put(KEY_AUTH_GRP, info.nAuthGrp);
		values.put(KEY_AUTH_TYPE, info.strAuthType);
		return mDb.insert(TABLE_AUTHHEADER_INFO, null, values);
	}

	public boolean deleteAuthHeaderInfo()
	{
		return mDb.delete(TABLE_AUTHHEADER_INFO, null, null) > 0;
	}

	public List<AuthDetInfo> getAuthDetailInfo(int nAuthGrp)
	{
		List<AuthDetInfo> authDetInfoList = new ArrayList<>();
		String sql =	" SELECT * FROM " + TABLE_AUTHDETL_INFO + " WHERE " + KEY_AUTH_GRP + " = " + nAuthGrp;
		Cursor cursor = mDb.rawQuery(sql, null);
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					AuthDetInfo info = new AuthDetInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nAuthGrp = cursor.getInt(cursor.getColumnIndex(KEY_AUTH_GRP));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strStartDate = cursor.getString(cursor.getColumnIndex(KEY_START_DATE));
					info.strEndDate = cursor.getString(cursor.getColumnIndex(KEY_END_DATE));
					authDetInfoList.add(info);
					cursor.moveToNext();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return authDetInfoList;
	}

	public List<AuthDetInfo> getAuthDetlInfo(int nAuthGrp)
	{
		List<AuthDetInfo> authDetInfoList = new ArrayList<>();
		String sql =	" SELECT	B.* " +
				" FROM		" + TABLE_AUTHHEADER_INFO + " A, " + TABLE_AUTHDETL_INFO + " B " +
				" WHERE A." + KEY_AUTH_TYPE + " ='T' AND A." + KEY_AUTH_GRP + " = B." + KEY_AUTH_GRP + " AND B." + KEY_AUTH_GRP + " = " + nAuthGrp;
		Cursor cursor = mDb.rawQuery(sql, null);
		try
		{
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					AuthDetInfo info = new AuthDetInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nAuthGrp = cursor.getInt(cursor.getColumnIndex(KEY_AUTH_GRP));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strStartDate = cursor.getString(cursor.getColumnIndex(KEY_START_DATE));
					info.strEndDate = cursor.getString(cursor.getColumnIndex(KEY_END_DATE));
					authDetInfoList.add(info);
					cursor.moveToNext();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return authDetInfoList;
	}
	public long insertAuthDetlInfo(AuthDetInfo info) {

		ContentValues values = new ContentValues();

		values.put(KEY_AUTH_GRP, info.nAuthGrp);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_START_DATE, info.strStartDate);
		values.put(KEY_END_DATE, info.strEndDate);

		return mDb.insert(TABLE_AUTHDETL_INFO, null, values);
	}

	public boolean deleteAuthDetlInfo()
	{
		return mDb.delete(TABLE_AUTHDETL_INFO, null, null) > 0;
	}

	public void insertUploadFile(String strLocalPath, String strServerPath)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_LOCAL_PATH, strLocalPath);
		values.put(KEY_SERVER_PATH, strServerPath);
		mDb.insert(TABLE_UPLOAD_LIST, null, values);
	}

	public UploadFileInfo getUploadFile()
	{
		String sql = "SELECT * FROM " + TABLE_UPLOAD_LIST + " LIMIT 1";
		Cursor cursor = mDb.rawQuery(sql, null);
		UploadFileInfo infos = null;
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				infos = new UploadFileInfo();
				infos.idx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				infos.localPath = cursor.getString(cursor.getColumnIndex(KEY_LOCAL_PATH));
				infos.serverPath = cursor.getString(cursor.getColumnIndex(KEY_SERVER_PATH));
			}

		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public List<UploadFileInfo> getUploadFileList()
	{
		String sql = "SELECT * FROM " + TABLE_UPLOAD_LIST;
		Cursor cursor = mDb.rawQuery(sql, null);
		UploadFileInfo info = null;
		List<UploadFileInfo> infos = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					info = new UploadFileInfo();
					info.idx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.localPath = cursor.getString(cursor.getColumnIndex(KEY_LOCAL_PATH));
					info.serverPath = cursor.getString(cursor.getColumnIndex(KEY_SERVER_PATH));
					infos.add(info);
					cursor.moveToNext();
				}

			}

		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public void deleteUploadFile(int nIdx)
	{
		mDb.delete(TABLE_UPLOAD_LIST, KEY_IDX + "=" + nIdx, null);
	}

	public void deleteAllUploadFiles()
	{
		mDb.delete(TABLE_UPLOAD_LIST, null, null);
	}


	public void insertLoginInfo(long nTrxnNo){
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, nTrxnNo);
		mDb.insert(TABLE_LOGIN_INFO, null, values);
	}

	public List<Long> getLoginInfoList(){
		String sql = "SELECT * FROM " + TABLE_LOGIN_INFO;
		Cursor cursor = mDb.rawQuery(sql, null);
		UploadFileInfo info = null;
		List<Long> infos = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					long trxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					infos.add(trxnNo);
					cursor.moveToNext();
				}
				deleteAllLoginInfo();
			}

		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public void deleteAllLoginInfo()
	{
		mDb.delete(TABLE_LOGIN_INFO, null, null);
	}

	public boolean isExistCustomerInfo(int nCustNo, int nChainNo, int nPeriodNo, int nDeliverySequence)
	{
		String sql =	" SELECT	* " +
				" FROM	" + TABLE_CUSTOMER_INFO +
				" WHERE	" + KEY_CUST_NO + " = " + nCustNo +
				" AND " + KEY_CHAIN_NO + " = " + nChainNo +
				" AND " + KEY_PERIOD_NO + " = " + nPeriodNo +
				" AND " + KEY_SEQ_NO + " = " + nDeliverySequence;
		Cursor cursor = mDb.rawQuery(sql, null);
		boolean bExist = false;
		try
		{
			if(cursor != null && cursor.getCount() > 0)
				bExist = true;
		}catch(Exception e)
		{

		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return bExist;
	}

	public long insertUPromotionInfo(UPromotionInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_FREE_ITEM_NO, info.strFreeItemNo);
		values.put(KEY_PLAN_NO, info.nPlanNo);
		values.put(KEY_ASSIGN_NO, info.nAssignNo);
		values.put(KEY_TRXN_TYPE, info.nTrxnType);
		values.put(KEY_AMOUNT, info.nAmount);
		values.put(KEY_PRICE_DISC_FLAG, info.nPriceDiscFlag);
		values.put(KEY_REASON_CODE, info.nReasonCode);
		values.put(KEY_PROMO_APP_METHOD, info.nPromoAppMethod);
		values.put(KEY_PROMO_TYPE, info.nPromoType);
		values.put(KEY_DISC_AMOUNT, info.nDiscAmt);
		values.put(KEY_TRXN_AMOUNT, info.nTrxnAmt);
		values.put(KEY_SEQ_NO, info.nSeqNo);
		values.put(KEY_PROMO_METHOD, info.nPromoMethod);
		values.put(KEY_DATE_START, info.strDateStart);

		return mDb.insert(TABLE_UPROMOTION_INFO, null, values);
	}

	public List<UPromotionInfo> getUPromotionList(long nTrxnNo, int nCustNo, String strItemNo, int nTrxnType)
	{
		String sql = "SELECT * FROM " + TABLE_UPROMOTION_INFO + " WHERE " + KEY_TRXN_NO + " = " + nTrxnNo +
				" AND " + KEY_TRXN_TYPE + " = " + nTrxnType + " AND " + KEY_CUST_NO + " = " + nCustNo +
				" AND " + KEY_ITEM_NO + " = '" + strItemNo + "'";

		Cursor cursor = mDb.rawQuery(sql, null);
		UPromotionInfo info = null;
		List<UPromotionInfo> infos = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					info = new UPromotionInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strFreeItemNo = cursor.getString(cursor.getColumnIndex(KEY_FREE_ITEM_NO));
					info.nPlanNo = cursor.getInt(cursor.getColumnIndex(KEY_PLAN_NO));
					info.nAssignNo = cursor.getInt(cursor.getColumnIndex(KEY_ASSIGN_NO));
					info.nTrxnType = cursor.getInt(cursor.getColumnIndex(KEY_TRXN_TYPE));
					info.nAmount = cursor.getInt(cursor.getColumnIndex(KEY_AMOUNT));
					info.nPriceDiscFlag = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_DISC_FLAG));
					info.nReasonCode = cursor.getInt(cursor.getColumnIndex(KEY_REASON_CODE));
					info.nPromoAppMethod = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_APP_METHOD));
					info.nPromoType = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_TYPE));
					info.nDiscAmt = cursor.getInt(cursor.getColumnIndex(KEY_DISC_AMOUNT));
					info.nTrxnAmt = cursor.getInt(cursor.getColumnIndex(KEY_TRXN_AMOUNT));
					info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
					info.nPromoMethod = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_METHOD));
					info.strDateStart = cursor.getString(cursor.getColumnIndex(KEY_DATE_START));

					infos.add(info);
					cursor.moveToNext();
				}

			}

		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public void deleteAllUPromotion()
	{
		mDb.delete(TABLE_UPROMOTION_INFO, null, null);
	}

	public void deleteUPromotion(long nTrxnNo)
	{
		mDb.delete(TABLE_UPROMOTION_INFO, KEY_TRXN_NO + " = " + nTrxnNo, null);
	}

	public void deleteUPromotion(long nTrxnNo, int nCustNo, String strItemNo, int nTrxnType)
	{
		mDb.delete(TABLE_UPROMOTION_INFO, KEY_TRXN_NO + " = " + nTrxnNo +
				" AND " + KEY_TRXN_TYPE + " = " + nTrxnType + " AND " + KEY_CUST_NO + " = " + nCustNo +
				" AND " + KEY_ITEM_NO + " = '" + strItemNo + "'", null);
	}

	private Map<String, List<UPromotionInfo>> getUPromotionInfoList()
	{
		Map<String, List<UPromotionInfo>> map = new HashMap<>();

		Map<Integer, Integer> seqNoMap = new HashMap<>();
		List<PromHeadInfo> promHeadList = new ArrayList<PromHeadInfo>();
		if (Global.gCurCustomerInfo != null && Global.gCurCustomerInfo.nPromoPlan == 999999)
		{
			List<ProMultiInfo> proMultiList = getProMultiList(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
			for (ProMultiInfo multiInfo : proMultiList)
			{
				List<PromHeadInfo> list = getPromHeadList(multiInfo.nPlanNo);
				promHeadList.addAll(list);
				seqNoMap.put(multiInfo.nPlanNo, multiInfo.nSeqNo);
			}
		}
		else if(Global.gCurCustomerInfo != null)
		{
			promHeadList = getPromHeadList(Global.gCurCustomerInfo.nPromoPlan);
		}
		else
		{
			promHeadList = getPromHeadList(0);
		}

		for (PromHeadInfo promHeadInfo : promHeadList)
		{
			List<PromAssInfo> promAssList = getPromAssList(promHeadInfo.nAssignNo);
			Integer seqNoObj = seqNoMap.get(promHeadInfo.nPlanNo);
			int nSeqNo = 0;
			if( seqNoObj != null )
			{
				nSeqNo = seqNoObj;
			}
			for (PromAssInfo promAssInfo : promAssList)
			{
				String sql =	" SELECT	* " +
						" FROM		" + TABLE_PROMNOVO_INFO +
						" WHERE		" + KEY_ASSIGN_NO + " = " + promAssInfo.nAssignNo;
				Cursor cursor = mDb.rawQuery(sql, null);
				try
				{
					if (cursor != null && cursor.getCount() > 0)
					{
						cursor.moveToFirst();
						for (int i=0; i<cursor.getCount(); i++)
						{
							UPromotionInfo info = new UPromotionInfo();
							info.nAssignNo = cursor.getInt(cursor.getColumnIndex(KEY_ASSIGN_NO));
							info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
							info.strFreeItemNo = cursor.getString(cursor.getColumnIndex(KEY_FREE_ITEM_NO));
							if(Global.gCurCustomerInfo != null)
							{
								info.nCustNo = Global.gCurCustomerInfo.nCustNo;
								info.nChainNo = Global.gCurCustomerInfo.nChainNo;
							}
							else
							{
								info.nCustNo = 0;
								info.nChainNo = 0;
							}

							info.nPlanNo = promHeadInfo.nPlanNo;
							info.nPromoAppMethod = promAssInfo.nPromoAppMethod;
							info.nPromoMethod = promAssInfo.nPromoMethod;
							info.nPromoType = promAssInfo.nPromoType;
							info.nDiscAmt = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_DISCOUNT));
							info.strDateStart = promHeadInfo.strDateStart;
							info.nSeqNo = nSeqNo;
							List<UPromotionInfo> infoList = map.get(info.strItemNo);
							if( infoList == null )
							{
								infoList = new ArrayList<>();
								map.put(info.strItemNo, infoList);
							}
							infoList.add(info);
							cursor.moveToNext();
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					if (cursor != null)
					{
						cursor.close();
					}
				}
			}
		}

		return map;
	}

	public void deleteAllUOrderStatusInfo(){
		mDb.delete(TABLE_UORDERSTATUSH_INFO, null, null);
		mDb.delete(TABLE_UORDERSTATUSD_INFO, null, null);
	}

	public void deleteUOrderStatusInfo(long nTrxnNo)
	{
		mDb.delete(TABLE_UORDERSTATUSH_INFO, KEY_TRXN_NO + "=" + nTrxnNo, null);
		mDb.delete(TABLE_UORDERSTATUSD_INFO, KEY_TRXN_NO + "=" + nTrxnNo, null);
	}

	public void updateOrInsertOrderStatusInfo(UOrderStatusHInfo info)
	{
		List<UOrderStatusDInfo> uOrderStatusDInfoList = getUOrderStatusDInfo(-1);
		Map<Integer, Integer> mapUpdate = new HashMap<>();
		for(UOrderStatusDInfo uOrderStatusDInfo : uOrderStatusDInfoList)
		{
			mapUpdate.put(uOrderStatusDInfo.nCustNo, uOrderStatusDInfo.nIdx);
		}

		ContentValues values = new ContentValues();
		for(UOrderStatusDInfo dInfo : info.uOrderStatusDInfoList)
		{
			values.clear();
			values.put(KEY_TRXN_NO, -1);
			values.put(KEY_CHAIN_NO, dInfo.nChainNo);
			values.put(KEY_CUST_NO, dInfo.nCustNo);
			values.put(KEY_DAY_NO, dInfo.nDayNo);
			values.put(KEY_DELIVERY_DATE, dInfo.strDeliveryDate);
			values.put(KEY_DELIVERY_SEQUENCE, dInfo.nDeliverySequence);
			values.put(KEY_ORDER_NO, dInfo.strOrderNo);
			values.put(KEY_ORDER_BARCODE, dInfo.strOrderBarcode);
			values.put(KEY_POREF, dInfo.strPORef);
			values.put(KEY_STATUS, dInfo.nStatus);
			values.put(KEY_REFERENCE, dInfo.strReference);
			Integer nIdx;
			nIdx = mapUpdate.get(dInfo.nCustNo);
			if( nIdx != null )
			{
				mDb.update(TABLE_UORDERSTATUSD_INFO, values, KEY_IDX + " = " + nIdx, null);
			}
			else
			{
				mDb.insert(TABLE_UORDERSTATUSD_INFO, null, values);
			}
		}
	}

	public void insertUOrderStatusInfo(UOrderStatusHInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_DOC_NO, info.strDocNo);
		values.put(KEY_DOC_TYPE, info.strDocType);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		mDb.insert(TABLE_UORDERSTATUSH_INFO, null, values);

		for(UOrderStatusDInfo dInfo : info.uOrderStatusDInfoList)
		{
			values.clear();
			values.put(KEY_TRXN_NO, info.nTrxnNo);
			values.put(KEY_CHAIN_NO, dInfo.nChainNo);
			values.put(KEY_CUST_NO, dInfo.nCustNo);
			values.put(KEY_DAY_NO, dInfo.nDayNo);
			values.put(KEY_DELIVERY_DATE, dInfo.strDeliveryDate);
			values.put(KEY_DELIVERY_SEQUENCE, dInfo.nDeliverySequence);
			values.put(KEY_ORDER_NO, dInfo.strOrderNo);
			values.put(KEY_ORDER_BARCODE, dInfo.strOrderBarcode);
			values.put(KEY_POREF, dInfo.strPORef);
			values.put(KEY_STATUS, dInfo.nStatus);
			values.put(KEY_REFERENCE, dInfo.strReference);
			mDb.insert(TABLE_UORDERSTATUSD_INFO, null, values);
		}

	}

	public UOrderStatusHInfo getUOrderStatusHInfo(long nTrxnNo)
	{
		String sql =	" SELECT	* " + " FROM		" + TABLE_UORDERSTATUSH_INFO + " WHERE " + KEY_TRXN_NO + " = " + nTrxnNo;
		Cursor cursor = mDb.rawQuery(sql, null);

		UOrderStatusHInfo info = null;
		try
		{
			if (cursor != null && cursor.getCount() == 1) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					info = new UOrderStatusHInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
					info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.uOrderStatusDInfoList = getUOrderStatusDInfo(nTrxnNo);
					cursor.moveToNext();
				}

			}

		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return info;
	}

	public List<UOrderStatusDInfo> getUOrderStatusDInfo(long nTrxnNo)
	{
		List<UOrderStatusDInfo> infoList = new ArrayList<>();
		String sql =	" SELECT	* " + " FROM		" + TABLE_UORDERSTATUSD_INFO + " WHERE " + KEY_TRXN_NO + " = " + nTrxnNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		UOrderStatusDInfo info;
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					info = new UOrderStatusDInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
					info.nDeliverySequence = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_SEQUENCE));
					info.strOrderNo = cursor.getString(cursor.getColumnIndex(KEY_ORDER_NO));
					info.strOrderBarcode = cursor.getString(cursor.getColumnIndex(KEY_ORDER_BARCODE));
					info.strPORef = cursor.getString(cursor.getColumnIndex(KEY_POREF));
					info.nStatus = cursor.getInt(cursor.getColumnIndex(KEY_STATUS));
					info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));

					infoList.add(info);
					cursor.moveToNext();
				}

			}

		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return infoList;
	}

	public long insertEquipment(EquipmentInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_EQUIPMENT_NO, info.strEquipmentNo);
		values.put(KEY_SERIAL_NO, info.strSerialNo);
		values.put(KEY_MAKE, info.strMake);
		values.put(KEY_MODEL, info.strModel);
		values.put(KEY_DESCRIPTION, info.strDescription);
		values.put(KEY_ASSET_TYPE, info.strAssetType);
		values.put(KEY_STATUS_CODE, info.strStatusCode);
		values.put(KEY_EQUIPMENT_TYPE, info.strEquipmentType);
		values.put(KEY_EQUIPMENT_ACT_TYPE, info.strEquipmentActType);
		values.put(KEY_APPLICATION, info.strApplication);
		values.put(KEY_DOLLAR_CAPACITY, info.strDollarCapacity);
		values.put(KEY_LAST_REPAIR, info.strLastRepair);
		values.put(KEY_TOTAL_RENT, info.nTotalRent);
		values.put(KEY_TOTAL_SALES, info.nTotalSales);
		values.put(KEY_ALT_EQUIPMENT, info.strAltEquipment);
		values.put(KEY_NEW_PLACEMENT_DOC_PATH, info.strNewPlacementDocPath);

		return mDb.insert(TABLE_EQUIPMENT_INFO, null, values);
	}

	public void updateEquipmentType(int nIdx, String equipmentType)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_EQUIPMENT_ACT_TYPE, equipmentType);
		mDb.update(TABLE_EQUIPMENT_INFO, values, KEY_IDX + " = " + nIdx, null);
	}

	public void updateAltEquipment(int nIdx, String strAltEquipment)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_ALT_EQUIPMENT, strAltEquipment);
		mDb.update(TABLE_EQUIPMENT_INFO, values, KEY_IDX + " = " + nIdx, null);
	}

	public int updateEquipment(EquipmentInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_EQUIPMENT_NO, info.strEquipmentNo);
		values.put(KEY_SERIAL_NO, info.strSerialNo);
		values.put(KEY_MAKE, info.strMake);
		values.put(KEY_MODEL, info.strModel);
		values.put(KEY_DESCRIPTION, info.strDescription);
		values.put(KEY_ASSET_TYPE, info.strAssetType);
		values.put(KEY_STATUS_CODE, info.strStatusCode);
		values.put(KEY_EQUIPMENT_TYPE, info.strEquipmentType);
		values.put(KEY_EQUIPMENT_ACT_TYPE, info.strEquipmentActType);
		values.put(KEY_APPLICATION, info.strApplication);
		values.put(KEY_DOLLAR_CAPACITY, info.strDollarCapacity);
		values.put(KEY_LAST_REPAIR, info.strLastRepair);
		values.put(KEY_TOTAL_RENT, info.nTotalRent);
		values.put(KEY_TOTAL_SALES, info.nTotalSales);
		values.put(KEY_ALT_EQUIPMENT, info.strAltEquipment);
		values.put(KEY_NEW_PLACEMENT_DOC_PATH, info.strNewPlacementDocPath);

		return mDb.update(TABLE_EQUIPMENT_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}

	public EquipmentInfo getEquipmentWithEquipNo(String strEquipNo)
	{
		String sql = "SELECT * FROM " + TABLE_EQUIPMENT_INFO + " WHERE " + KEY_EQUIPMENT_NO + " = '" + strEquipNo + "'";

		Cursor cursor = mDb.rawQuery(sql, null);
		EquipmentInfo info = null;
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info = new EquipmentInfo();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.strEquipmentNo = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_NO));
				info.strSerialNo = cursor.getString(cursor.getColumnIndex(KEY_SERIAL_NO));
				info.strMake = cursor.getString(cursor.getColumnIndex(KEY_MAKE));
				info.strModel = cursor.getString(cursor.getColumnIndex(KEY_MODEL));
				info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
				info.strAssetType = cursor.getString(cursor.getColumnIndex(KEY_ASSET_TYPE));
				info.strStatusCode = cursor.getString(cursor.getColumnIndex(KEY_STATUS_CODE));
				info.strEquipmentType = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_TYPE));
				info.strEquipmentActType = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_ACT_TYPE));
				info.strApplication = cursor.getString(cursor.getColumnIndex(KEY_APPLICATION));
				info.strDollarCapacity = cursor.getString(cursor.getColumnIndex(KEY_DOLLAR_CAPACITY));
				info.strLastRepair = cursor.getString(cursor.getColumnIndex(KEY_LAST_REPAIR));
				info.nTotalRent = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_RENT));
				info.nTotalSales = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_SALES));
				info.strAltEquipment = cursor.getString(cursor.getColumnIndex(KEY_ALT_EQUIPMENT));
				info.strNewPlacementDocPath = cursor.getString(cursor.getColumnIndex(KEY_NEW_PLACEMENT_DOC_PATH));
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	public EquipmentInfo getEquipmentWithSerialNo(String strSerialNo)
	{
		String sql = "SELECT * FROM " + TABLE_EQUIPMENT_INFO + " WHERE " + KEY_SERIAL_NO + " = '" + strSerialNo + "'";

		Cursor cursor = mDb.rawQuery(sql, null);
		EquipmentInfo info = null;
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info = new EquipmentInfo();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.strEquipmentNo = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_NO));
				info.strSerialNo = cursor.getString(cursor.getColumnIndex(KEY_SERIAL_NO));
				info.strMake = cursor.getString(cursor.getColumnIndex(KEY_MAKE));
				info.strModel = cursor.getString(cursor.getColumnIndex(KEY_MODEL));
				info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
				info.strAssetType = cursor.getString(cursor.getColumnIndex(KEY_ASSET_TYPE));
				info.strStatusCode = cursor.getString(cursor.getColumnIndex(KEY_STATUS_CODE));
				info.strEquipmentType = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_TYPE));
				info.strEquipmentActType = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_ACT_TYPE));
				info.strApplication = cursor.getString(cursor.getColumnIndex(KEY_APPLICATION));
				info.strDollarCapacity = cursor.getString(cursor.getColumnIndex(KEY_DOLLAR_CAPACITY));
				info.strLastRepair = cursor.getString(cursor.getColumnIndex(KEY_LAST_REPAIR));
				info.nTotalRent = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_RENT));
				info.nTotalSales = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_SALES));
				info.strAltEquipment = cursor.getString(cursor.getColumnIndex(KEY_ALT_EQUIPMENT));
				info.strNewPlacementDocPath = cursor.getString(cursor.getColumnIndex(KEY_NEW_PLACEMENT_DOC_PATH));
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	public EquipmentInfo getEquipmentWithAltEquipment(String strAltEquipment)
	{
		String sql = "SELECT * FROM " + TABLE_EQUIPMENT_INFO + " WHERE " + KEY_ALT_EQUIPMENT + " = '" + strAltEquipment + "'";

		Cursor cursor = mDb.rawQuery(sql, null);
		EquipmentInfo info = null;
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info = new EquipmentInfo();
				info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.strEquipmentNo = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_NO));
				info.strSerialNo = cursor.getString(cursor.getColumnIndex(KEY_SERIAL_NO));
				info.strMake = cursor.getString(cursor.getColumnIndex(KEY_MAKE));
				info.strModel = cursor.getString(cursor.getColumnIndex(KEY_MODEL));
				info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
				info.strAssetType = cursor.getString(cursor.getColumnIndex(KEY_ASSET_TYPE));
				info.strStatusCode = cursor.getString(cursor.getColumnIndex(KEY_STATUS_CODE));
				info.strEquipmentType = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_TYPE));
				info.strEquipmentActType = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_ACT_TYPE));
				info.strApplication = cursor.getString(cursor.getColumnIndex(KEY_APPLICATION));
				info.strDollarCapacity = cursor.getString(cursor.getColumnIndex(KEY_DOLLAR_CAPACITY));
				info.strLastRepair = cursor.getString(cursor.getColumnIndex(KEY_LAST_REPAIR));
				info.nTotalRent = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_RENT));
				info.nTotalSales = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_SALES));
				info.strAltEquipment = cursor.getString(cursor.getColumnIndex(KEY_ALT_EQUIPMENT));
				info.strNewPlacementDocPath = cursor.getString(cursor.getColumnIndex(KEY_NEW_PLACEMENT_DOC_PATH));
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	public List<EquipmentInfo> getAllEquipment()
	{
		String sql = "SELECT * FROM " + TABLE_EQUIPMENT_INFO;

		Cursor cursor = mDb.rawQuery(sql, null);
		EquipmentInfo info;
		List<EquipmentInfo> infos = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					info = new EquipmentInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strEquipmentNo = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_NO));
					info.strSerialNo = cursor.getString(cursor.getColumnIndex(KEY_SERIAL_NO));
					info.strMake = cursor.getString(cursor.getColumnIndex(KEY_MAKE));
					info.strModel = cursor.getString(cursor.getColumnIndex(KEY_MODEL));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strAssetType = cursor.getString(cursor.getColumnIndex(KEY_ASSET_TYPE));
					info.strStatusCode = cursor.getString(cursor.getColumnIndex(KEY_STATUS_CODE));
					info.strEquipmentType = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_TYPE));
					info.strEquipmentActType = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_ACT_TYPE));
					info.strApplication = cursor.getString(cursor.getColumnIndex(KEY_APPLICATION));
					info.strDollarCapacity = cursor.getString(cursor.getColumnIndex(KEY_DOLLAR_CAPACITY));
					info.strLastRepair = cursor.getString(cursor.getColumnIndex(KEY_LAST_REPAIR));
					info.nTotalRent = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_RENT));
					info.nTotalSales = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_SALES));
					info.strAltEquipment = cursor.getString(cursor.getColumnIndex(KEY_ALT_EQUIPMENT));
					info.strNewPlacementDocPath = cursor.getString(cursor.getColumnIndex(KEY_NEW_PLACEMENT_DOC_PATH));

					infos.add(info);
					cursor.moveToNext();
				}
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public void deleteAllEquipment()
	{
		mDb.delete(TABLE_EQUIPMENT_INFO, null, null);
	}

	public long insertEquipAss(EquipAssInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_EQUIPMENT_NO, info.strEquipmentNo);
		values.put(KEY_INSTALLED, info.strInstalled);
		values.put(KEY_AGREEMENT_NO, info.strAgreementNo);
		values.put(KEY_AGREEMENT_DATE, info.strAgreementDate);
		values.put(KEY_BILL_TYPE, info.strBillType);
		values.put(KEY_BILL_FREQ, info.strBillFreq);
		values.put(KEY_PAY_RENT_AMT, info.nPayRentAmt);
		values.put(KEY_VERIFY_FLAG, info.strVerifyFlag);
		values.put(KEY_VERIFIED, info.strVerified);
		values.put(KEY_FEATURES, info.strFeatures);
		values.put(KEY_REPAIR_NOTES, info.strRepairNotes);
		values.put(KEY_REPAIR_REASON, info.strRepairReason);
		values.put(KEY_EQUIPMENT_DOC, info.strEquipmentDoc);
		values.put(KEY_REPAIR_STATUS, info.strRepairStatus);
		values.put(KEY_REPAIR_TYPE, info.strRepairType);
		values.put(KEY_REPAIR_REQUESTED_PICKUP_DATE, info.strRepairRequestedPickUpDate);
		values.put(KEY_REPAIR_IMAGE, info.strRepairImage);
		values.put(KEY_STATUS, info.strStatus);
		values.put(KEY_OLD_EQUIPMENT_TYPE, info.strOldEquipmentType);
		values.put(KEY_OLD_EQUIPMENT_NO, info.strOldEquipmentNo);
		values.put(KEY_VERIFIED_DATE, info.strVerifiedDate);
		values.put(KEY_REPAIRED_DATE, info.strRepairedDate);

		return mDb.insert(TABLE_EQUIP_ASS_INFO, null, values);
	}

	public long updateEquipAss(EquipAssInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_EQUIPMENT_NO, info.strEquipmentNo);
		values.put(KEY_INSTALLED, info.strInstalled);
		values.put(KEY_AGREEMENT_NO, info.strAgreementNo);
		values.put(KEY_AGREEMENT_DATE, info.strAgreementDate);
		values.put(KEY_BILL_TYPE, info.strBillType);
		values.put(KEY_BILL_FREQ, info.strBillFreq);
		values.put(KEY_PAY_RENT_AMT, info.nPayRentAmt);
		values.put(KEY_VERIFY_FLAG, info.strVerifyFlag);
		values.put(KEY_VERIFIED, info.strVerified);
		values.put(KEY_FEATURES, info.strFeatures);
		values.put(KEY_REPAIR_NOTES, info.strRepairNotes);
		values.put(KEY_REPAIR_REASON, info.strRepairReason);
		values.put(KEY_EQUIPMENT_DOC, info.strEquipmentDoc);
		values.put(KEY_REPAIR_STATUS, info.strRepairStatus);
		values.put(KEY_REPAIR_TYPE, info.strRepairType);
		values.put(KEY_REPAIR_REQUESTED_PICKUP_DATE, info.strRepairRequestedPickUpDate);
		values.put(KEY_REPAIR_IMAGE, info.strRepairImage);
		values.put(KEY_STATUS, info.strStatus);
		values.put(KEY_OLD_EQUIPMENT_TYPE, info.strOldEquipmentType);
		values.put(KEY_OLD_EQUIPMENT_NO, info.strOldEquipmentNo);
		values.put(KEY_VERIFIED_DATE, info.strVerifiedDate);
		values.put(KEY_REPAIRED_DATE, info.strRepairedDate);

		return mDb.update(TABLE_EQUIP_ASS_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}

	public long updateEquipAss(EquipmentWithAssInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_EQUIPMENT_NO, info.strEquipmentNo);
		values.put(KEY_INSTALLED, info.strInstalled);
		values.put(KEY_AGREEMENT_NO, info.strAgreementNo);
		values.put(KEY_AGREEMENT_DATE, info.strAgreementDate);
		values.put(KEY_BILL_TYPE, info.strBillType);
		values.put(KEY_BILL_FREQ, info.strBillFreq);
		values.put(KEY_PAY_RENT_AMT, info.nPayRentAmt);
		values.put(KEY_VERIFY_FLAG, info.strVerifyFlag);
		values.put(KEY_VERIFIED, info.strVerified);
		values.put(KEY_FEATURES, info.strFeatures);
		values.put(KEY_REPAIR_NOTES, info.strRepairNotes);
		values.put(KEY_REPAIR_REASON, info.strRepairReason);
		values.put(KEY_EQUIPMENT_DOC, info.strEquipmentDoc);
		values.put(KEY_REPAIR_STATUS, info.strRepairStatus);
		values.put(KEY_REPAIR_TYPE, info.strRepairType);
		values.put(KEY_REPAIR_REQUESTED_PICKUP_DATE, info.strRepairRequestedPickUpDate);
		values.put(KEY_REPAIR_IMAGE, info.strRepairImage);
		values.put(KEY_STATUS, info.strStatus);
		values.put(KEY_OLD_EQUIPMENT_TYPE, info.strOldEquipmentType);
		values.put(KEY_OLD_EQUIPMENT_NO, info.strOldEquipmentNo);
		values.put(KEY_VERIFIED_DATE, info.strVerifiedDate);
		values.put(KEY_REPAIRED_DATE, info.strRepairedDate);

		return mDb.update(TABLE_EQUIP_ASS_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}

	public List<EquipAssInfo> getAllEquipAss()
	{
		String sql = "SELECT * FROM " + TABLE_EQUIP_ASS_INFO;

		Cursor cursor = mDb.rawQuery(sql, null);
		EquipAssInfo info;
		List<EquipAssInfo> infos = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					info = new EquipAssInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.strEquipmentNo = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_NO));
					info.strInstalled = cursor.getString(cursor.getColumnIndex(KEY_INSTALLED));
					info.strAgreementNo = cursor.getString(cursor.getColumnIndex(KEY_AGREEMENT_NO));
					info.strAgreementDate = cursor.getString(cursor.getColumnIndex(KEY_AGREEMENT_DATE));
					info.strBillType = cursor.getString(cursor.getColumnIndex(KEY_BILL_TYPE));
					info.strBillFreq = cursor.getString(cursor.getColumnIndex(KEY_BILL_FREQ));
					info.nPayRentAmt = cursor.getInt(cursor.getColumnIndex(KEY_PAY_RENT_AMT));
					info.strVerifyFlag = cursor.getString(cursor.getColumnIndex(KEY_VERIFY_FLAG));
					info.strVerified = cursor.getString(cursor.getColumnIndex(KEY_VERIFIED));
					info.strFeatures = cursor.getString(cursor.getColumnIndex(KEY_FEATURES));
					info.strRepairNotes = cursor.getString(cursor.getColumnIndex(KEY_REPAIR_NOTES));
					info.strRepairReason = cursor.getString(cursor.getColumnIndex(KEY_REPAIR_REASON));
					info.strEquipmentDoc = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_DOC));
					info.strRepairStatus = cursor.getString(cursor.getColumnIndex(KEY_REPAIR_STATUS));
					info.strRepairType = cursor.getString(cursor.getColumnIndex(KEY_REPAIR_TYPE));
					info.strRepairRequestedPickUpDate = cursor.getString(cursor.getColumnIndex(KEY_REPAIR_REQUESTED_PICKUP_DATE));
					info.strRepairImage = cursor.getString(cursor.getColumnIndex(KEY_REPAIR_IMAGE));
					info.strStatus = cursor.getString(cursor.getColumnIndex(KEY_STATUS));
					info.strOldEquipmentType = cursor.getString(cursor.getColumnIndex(KEY_OLD_EQUIPMENT_TYPE));
					info.strOldEquipmentNo = cursor.getString(cursor.getColumnIndex(KEY_OLD_EQUIPMENT_NO));
					info.strVerifiedDate = cursor.getString(cursor.getColumnIndex(KEY_VERIFIED_DATE));
					info.strRepairedDate = cursor.getString(cursor.getColumnIndex(KEY_REPAIRED_DATE));
					infos.add(info);
					cursor.moveToNext();
				}
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public void deleteAllEquipAss()
	{
		mDb.delete(TABLE_EQUIP_ASS_INFO, null, null);
	}

	public List<EquipmentWithAssInfo> getEquipmentWithCustomer(int nChainNo, int nCustNo)
	{
		String sql = "SELECT A.*, B." + KEY_SERIAL_NO + ", B." + KEY_MAKE + ", B." + KEY_MODEL + ", B." + KEY_DESCRIPTION + ", B." + KEY_ALT_EQUIPMENT +
				", B." + KEY_EQUIPMENT_TYPE + ", B." + KEY_APPLICATION + ", B." + KEY_STATUS_CODE + ", B." + KEY_ASSET_TYPE + ", B." + KEY_NEW_PLACEMENT_DOC_PATH + ", B." + KEY_EQUIPMENT_ACT_TYPE +
				" FROM " + TABLE_EQUIPMENT_INFO + " B, " + TABLE_EQUIP_ASS_INFO + " A WHERE " +
				"A." + KEY_EQUIPMENT_NO + " = B." + KEY_EQUIPMENT_NO + " AND A." + KEY_CHAIN_NO + " = " + nChainNo + " AND A." + KEY_CUST_NO + " = " + nCustNo;

		Cursor cursor = mDb.rawQuery(sql, null);
		EquipmentWithAssInfo info;
		List<EquipmentWithAssInfo> infos = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					info = new EquipmentWithAssInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.strEquipmentNo = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_NO));
					info.strInstalled = cursor.getString(cursor.getColumnIndex(KEY_INSTALLED));
					info.strAgreementNo = cursor.getString(cursor.getColumnIndex(KEY_AGREEMENT_NO));
					info.strAgreementDate = cursor.getString(cursor.getColumnIndex(KEY_AGREEMENT_DATE));
					info.strBillType = cursor.getString(cursor.getColumnIndex(KEY_BILL_TYPE));
					info.strBillFreq = cursor.getString(cursor.getColumnIndex(KEY_BILL_FREQ));
					info.nPayRentAmt = cursor.getInt(cursor.getColumnIndex(KEY_PAY_RENT_AMT));
					info.strVerifyFlag = cursor.getString(cursor.getColumnIndex(KEY_VERIFY_FLAG));
					info.strVerified = cursor.getString(cursor.getColumnIndex(KEY_VERIFIED));
					info.strFeatures = cursor.getString(cursor.getColumnIndex(KEY_FEATURES));
					info.strAltEquipment = cursor.getString(cursor.getColumnIndex(KEY_ALT_EQUIPMENT));
					info.strRepairNotes = cursor.getString(cursor.getColumnIndex(KEY_REPAIR_NOTES));
					info.strRepairReason = cursor.getString(cursor.getColumnIndex(KEY_REPAIR_REASON));
					info.strEquipmentDoc = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_DOC));
					info.strRepairStatus = cursor.getString(cursor.getColumnIndex(KEY_REPAIR_STATUS));
					info.strRepairType = cursor.getString(cursor.getColumnIndex(KEY_REPAIR_TYPE));
					info.strRepairRequestedPickUpDate = cursor.getString(cursor.getColumnIndex(KEY_REPAIR_REQUESTED_PICKUP_DATE));
					info.strRepairImage = cursor.getString(cursor.getColumnIndex(KEY_REPAIR_IMAGE));
					info.strNewPlacementDocPath = cursor.getString(cursor.getColumnIndex(KEY_NEW_PLACEMENT_DOC_PATH));
					info.strSerialNo = cursor.getString(cursor.getColumnIndex(KEY_SERIAL_NO));
					info.strMake = cursor.getString(cursor.getColumnIndex(KEY_MAKE));
					info.strModel = cursor.getString(cursor.getColumnIndex(KEY_MODEL));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strEquipmentType = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_TYPE));
					info.strEquipmentActType = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_ACT_TYPE));
					info.strApplication = cursor.getString(cursor.getColumnIndex(KEY_APPLICATION));
					info.strStatusCode = cursor.getString(cursor.getColumnIndex(KEY_STATUS_CODE));
					info.strAssetType = cursor.getString(cursor.getColumnIndex(KEY_ASSET_TYPE));
					info.strStatus = cursor.getString(cursor.getColumnIndex(KEY_STATUS));
					info.strOldEquipmentType = cursor.getString(cursor.getColumnIndex(KEY_OLD_EQUIPMENT_TYPE));
					info.strOldEquipmentNo = cursor.getString(cursor.getColumnIndex(KEY_OLD_EQUIPMENT_NO));
					info.strVerifiedDate = cursor.getString(cursor.getColumnIndex(KEY_VERIFIED_DATE));
					info.strRepairedDate = cursor.getString(cursor.getColumnIndex(KEY_REPAIRED_DATE));
					infos.add(info);
					cursor.moveToNext();
				}
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public boolean isExistEquip(int nChainNo, int nCustNo)
	{
		String sql = "SELECT " + KEY_IDX + " FROM " + TABLE_EQUIP_ASS_INFO + " WHERE " + KEY_CHAIN_NO + " = " + nChainNo + " AND " + KEY_CUST_NO + " = " + nCustNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		boolean bExist = false;
		try {
			bExist = cursor != null && cursor.getCount() > 0;

		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return bExist;
	}

	public void deleteEquipCompleteStatus()
	{
		mDb.delete(TABLE_EQUIP_COMPLETE_STATUS_INFO, null, null);
	}

	public void insertOrUpdateEquipCompleteStatus(int nChainNo, int nCustNo, boolean bCompleted)
	{
		String sql = "SELECT * FROM " + TABLE_EQUIP_COMPLETE_STATUS_INFO + " WHERE " + KEY_CHAIN_NO + " = " + nChainNo + " AND " + KEY_CUST_NO + " = " + nCustNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		boolean bExist = false;
		int nIdx = -1;
		try {
			bExist = cursor != null && cursor.getCount() > 0;
			if(bExist)
			{
				cursor.moveToFirst();
				nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, nChainNo);
		values.put(KEY_CUST_NO, nCustNo);
		values.put(KEY_COMPLETED, bCompleted ? 1: 0);
		if(bExist)
		{
			mDb.update(TABLE_EQUIP_COMPLETE_STATUS_INFO, values, KEY_IDX + " = " + nIdx, null);
		}
		else
		{
			mDb.insert(TABLE_EQUIP_COMPLETE_STATUS_INFO, null, values);
		}
	}

	public boolean isCompletedEquipStatus(int nChainNo, int nCustNo)
	{
		String sql = "SELECT * FROM " + TABLE_EQUIP_COMPLETE_STATUS_INFO + " WHERE " + KEY_CHAIN_NO + " = " + nChainNo + " AND " + KEY_CUST_NO + " = " + nCustNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		boolean bExist = false;
		int nCompleted = 0;
		try {
			bExist = cursor != null && cursor.getCount() > 0;
			if(bExist)
			{
				cursor.moveToFirst();
				nCompleted = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED));
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return nCompleted == 1;
	}

	public boolean isExistEquipModel()
	{
		String sql = "SELECT A.* FROM " + TABLE_EQUIPMENT_INFO + " A, " + TABLE_EQUIP_ASS_INFO +
				" B WHERE A." + KEY_EQUIPMENT_NO + " = B." + KEY_EQUIPMENT_NO + " AND B." + KEY_CHAIN_NO + " = 0 AND B." + KEY_CUST_NO + " = 0";

		Cursor cursor = mDb.rawQuery(sql, null);
		boolean bResult = cursor != null && cursor.getCount() > 0;
		try {
			if( cursor != null )
			{
				cursor.close();
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}

		return bResult;
	}

	public boolean isExistEquipmentMode(String strEquipmentNo)
	{
		String sql = "SELECT * FROM " + TABLE_EQUIPMENT_INFO + " WHERE " + KEY_EQUIPMENT_NO + " = '" + strEquipmentNo + "'";
		Cursor cursor = mDb.rawQuery(sql, null);
		boolean bResult = cursor != null && cursor.getCount() > 0;
		try {
			cursor.close();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		return bResult;
	}

	public List<EquipmentInfo> getEquipmentModel()
	{
		String sql = "SELECT A.* FROM " + TABLE_EQUIPMENT_INFO + " A, " + TABLE_EQUIP_ASS_INFO +
				" B WHERE A." + KEY_EQUIPMENT_NO + " = B." + KEY_EQUIPMENT_NO + " AND B." + KEY_CHAIN_NO + " = 0 AND B." + KEY_CUST_NO + " = 0";

		Cursor cursor = mDb.rawQuery(sql, null);
		EquipmentInfo info;
		List<EquipmentInfo> infos = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					info = new EquipmentInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strEquipmentNo = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_NO));
					info.strSerialNo = cursor.getString(cursor.getColumnIndex(KEY_SERIAL_NO));
					info.strMake = cursor.getString(cursor.getColumnIndex(KEY_MAKE));
					info.strModel = cursor.getString(cursor.getColumnIndex(KEY_MODEL));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strAssetType = cursor.getString(cursor.getColumnIndex(KEY_ASSET_TYPE));
					info.strStatusCode = cursor.getString(cursor.getColumnIndex(KEY_STATUS_CODE));
					info.strEquipmentType = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_TYPE));
					info.strEquipmentActType = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_ACT_TYPE));
					info.strApplication = cursor.getString(cursor.getColumnIndex(KEY_APPLICATION));
					info.strDollarCapacity = cursor.getString(cursor.getColumnIndex(KEY_DOLLAR_CAPACITY));
					info.strLastRepair = cursor.getString(cursor.getColumnIndex(KEY_LAST_REPAIR));
					info.nTotalRent = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_RENT));
					info.nTotalSales = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_SALES));
					info.strAltEquipment = cursor.getString(cursor.getColumnIndex(KEY_ALT_EQUIPMENT));
					info.strNewPlacementDocPath = cursor.getString(cursor.getColumnIndex(KEY_NEW_PLACEMENT_DOC_PATH));
					infos.add(info);
					cursor.moveToNext();
				}
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public void insertUAssetsInfo(UAssetInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_EQUIPMENT_NO, info.strEquipmentNo);
		values.put(KEY_SERIAL_NO, info.strSerialNo);
		values.put(KEY_EQUIPMENT_TYPE, info.strEquipmentType);
		values.put(KEY_EQUIPMENT_ACT_TYPE, info.strEquipmentActType);
		values.put(KEY_MAKE, info.strMake);
		values.put(KEY_MODEL, info.strModel);
		values.put(KEY_APPLICATION, info.strApplication);
		values.put(KEY_STATUS_CODE, info.strStatusCode);
		values.put(KEY_ASSET_TYPE, info.strAssetType);
		values.put(KEY_REQUEST_DATE, info.strRequestDate);
		values.put(KEY_REPAIR_REASON, info.strRepairReason);
		values.put(KEY_EQUIPMENT_DOC, info.strEquipmentDoc);
		values.put(KEY_REPAIR_STATUS, info.strRepairStatus);
		values.put(KEY_REPAIR_TYPE, info.strRepairType);
		values.put(KEY_REPAIR_REQUESTED_PICKUP_DATE, info.strRepairRequestedPickUpDate);
		values.put(KEY_REPAIR_NOTES, info.strRepairNotes);
		values.put(KEY_DOC_NO, info.strDocNo);
		values.put(KEY_ALT_EQUIPMENT, info.strAltEquipment);
		values.put(KEY_RESPONSE, info.strResponse);

		mDb.insert(TABLE_UASSETS_INFO, null, values);
	}

	public void deleteAllUAssetsInfo()
	{
		mDb.delete(TABLE_UASSETS_INFO, null, null);
	}

	public List<UAssetInfo> getUAssetsInfo(int nCustNo)
	{
		String sql = "SELECT * FROM " + TABLE_UASSETS_INFO + " WHERE " + KEY_CUST_NO + " = " + nCustNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		UAssetInfo info;
		List<UAssetInfo> infos = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					info = new UAssetInfo();
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strEquipmentNo = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_NO));
					info.strSerialNo = cursor.getString(cursor.getColumnIndex(KEY_SERIAL_NO));
					info.strMake = cursor.getString(cursor.getColumnIndex(KEY_MAKE));
					info.strModel = cursor.getString(cursor.getColumnIndex(KEY_MODEL));
					info.strAssetType = cursor.getString(cursor.getColumnIndex(KEY_ASSET_TYPE));
					info.strStatusCode = cursor.getString(cursor.getColumnIndex(KEY_STATUS_CODE));
					info.strEquipmentType = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_TYPE));
					info.strEquipmentActType = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_ACT_TYPE));
					info.strApplication = cursor.getString(cursor.getColumnIndex(KEY_APPLICATION));
					info.strRequestDate = cursor.getString(cursor.getColumnIndex(KEY_REQUEST_DATE));
					info.strRepairReason = cursor.getString(cursor.getColumnIndex(KEY_REPAIR_REASON));
					info.strEquipmentDoc = cursor.getString(cursor.getColumnIndex(KEY_EQUIPMENT_DOC));
					info.strRepairStatus = cursor.getString(cursor.getColumnIndex(KEY_REPAIR_STATUS));
					info.strRepairType = cursor.getString(cursor.getColumnIndex(KEY_REPAIR_TYPE));
					info.strRepairRequestedPickUpDate = cursor.getString(cursor.getColumnIndex(KEY_REPAIR_REQUESTED_PICKUP_DATE));
					info.strRepairNotes = cursor.getString(cursor.getColumnIndex(KEY_REPAIR_NOTES));
					info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
					info.strAltEquipment = cursor.getString(cursor.getColumnIndex(KEY_ALT_EQUIPMENT));
					info.strResponse = cursor.getString(cursor.getColumnIndex(KEY_RESPONSE));
					infos.add(info);
					cursor.moveToNext();
				}
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	//URouteSCH  Header
	public boolean deleteAllURouteSchsHeader() throws SQLException {
		return mDb.delete(TABLE_UROUTESCHS_HEADER_INFO, null, null) > 0;
	}

	public boolean deleteURouteSchsHeader(long nTrxnNo) throws SQLException {
		return mDb.delete(TABLE_UROUTESCHS_HEADER_INFO, KEY_TRXN_NO + " = " + nTrxnNo, null) > 0;
	}

	public long insertURouteSchHeader(URouteSchHeader info){
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_DOC_TYPE, info.strDocType);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		return mDb.insert(TABLE_UROUTESCHS_HEADER_INFO, null, values);
	}

	public List<URouteSchHeader> getURouteSchHeader()
	{
		String sql =	" SELECT	* " + " FROM		" + TABLE_UROUTESCHS_HEADER_INFO;
		Cursor cursor = mDb.rawQuery(sql, null);

		List<URouteSchHeader> infos = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					URouteSchHeader info = new URouteSchHeader();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					infos.add(info);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	//URouteSCH	Body
	public boolean deleteAllURouteSchsBody() throws SQLException {
		return mDb.delete(TABLE_UROUTESCHS_BODY_INFO, null, null) > 0;
	}

	public boolean deleteURouteSchsBody(long nTrxnNo) throws SQLException {
		return mDb.delete(TABLE_UROUTESCHS_BODY_INFO, KEY_TRXN_NO + " = " + nTrxnNo, null) > 0;
	}

	public long insertURouteSchBody(URouteSchBody info){
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_ORG_SEQ_NO, info.nOrgSeqNo);
		values.put(KEY_CHANGE_SEQ_NO, info.nChangeSeqNo);
		values.put(KEY_ETADATE, info.strETADate);
		values.put(KEY_ETATIME, info.strETATime);
		return mDb.insert(TABLE_UROUTESCHS_BODY_INFO, null, values);
	}

	public List<URouteSchBody> getURouteSchBody(long nTrxnNo)
	{
		String sql =	" SELECT	* " + " FROM		" + TABLE_UROUTESCHS_BODY_INFO + " WHERE " + KEY_TRXN_NO + " = " + nTrxnNo;
		Cursor cursor = mDb.rawQuery(sql, null);

		List<URouteSchBody> infos = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					URouteSchBody info = new URouteSchBody();
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nOrgSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_ORG_SEQ_NO));
					info.nChangeSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_CHANGE_SEQ_NO));
					info.strETADate = cursor.getString(cursor.getColumnIndex(KEY_ETADATE));
					info.strETATime = cursor.getString(cursor.getColumnIndex(KEY_ETATIME));
					infos.add(info);
					cursor.moveToNext();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public int getCustomerListCount(boolean bIsRouteSch) {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_CUSTOMER_INFO +
				" WHERE		" + KEY_IS_ROUTE_SCH + " = " + (bIsRouteSch == true ? 1 : 0);
		Cursor cursor = mDb.rawQuery(sql, null);
		if (cursor != null && cursor.getCount() > 0)
			return cursor.getCount();
		else
			return 0;
	}

	public long insertOrderHistoryInfo(OrderHistoryInfo info){
		ContentValues values = new ContentValues();
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_ORDER_DATE, info.strOrderDate);
		values.put(KEY_SAQTY, info.nSAQty);
		values.put(KEY_BBQTY, info.nBBQty);
		values.put(KEY_DUQTY, info.nDUQty);

		return mDb.insert(TABLE_ORDER_HISTORY_INFO, null, values);
	}

	public List<OrderHistoryInfo> getOrderHistoryList(String strItemNo)
	{
		String sql = "SELECT A.*, B." + KEY_SCW_TYPE +" FROM " + TABLE_ORDER_HISTORY_INFO + " A, " + TABLE_PRODLOCN_INFO + " B WHERE  A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO + " AND A." + KEY_ITEM_NO + " = '" + strItemNo + "'";

		Cursor cursor = mDb.rawQuery(sql, null);
		OrderHistoryInfo info = null;
		List<OrderHistoryInfo> infos = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					info = new OrderHistoryInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strOrderDate = cursor.getString(cursor.getColumnIndex(KEY_ORDER_DATE));
					info.nSAQty = cursor.getInt(cursor.getColumnIndex(KEY_SAQTY));
					info.nBBQty = cursor.getInt(cursor.getColumnIndex(KEY_BBQTY));
					info.nDUQty = cursor.getInt(cursor.getColumnIndex(KEY_DUQTY));
					info.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
					infos.add(info);
					cursor.moveToNext();
				}
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public int getAllCustomerCountInOrderHistory(String strType)
	{
		String sql = "SELECT A." + KEY_CUST_NO +" FROM " + TABLE_ORDER_HISTORY_INFO + " A, " + TABLE_CUSTOMER_INFO + " B WHERE A." + KEY_CUST_NO + " = B." + KEY_CUST_NO + " AND B." + KEY_TYPE + " = '" + strType + "' GROUP BY A." + KEY_CUST_NO;
		Cursor cursor = mDb.rawQuery(sql, null);
		int nCnt = 0;
		try {
			if(cursor != null && cursor.getCount() > 0){
				nCnt = cursor.getCount();
//				cursor.moveToFirst();
//				nCnt = cursor.getInt(cursor.getColumnIndex("count"));
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if(cursor != null)
			{
				cursor.close();
			}
		}
		return nCnt;
	}

	public int getCustomerCountInOrderHistory(String strType, String strItemNo)
	{
		String sql = "SELECT A." + KEY_CUST_NO +" FROM " + TABLE_ORDER_HISTORY_INFO + " A, " + TABLE_CUSTOMER_INFO + " B WHERE A." + KEY_ITEM_NO + " = '" + strItemNo + "' AND A." + KEY_CUST_NO + " = B." + KEY_CUST_NO + " AND B." + KEY_TYPE + " = '" + strType + "' GROUP BY A." + KEY_CUST_NO;
		Cursor cursor = mDb.rawQuery(sql, null);
		int nCnt = 0;
		try {
			if(cursor != null && cursor.getCount() > 0){
				nCnt = cursor.getCount();
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if(cursor != null)
			{
				cursor.close();
			}
		}
		return nCnt;
	}

	public Map<String, String> getAllOrderHistoryItemAsMap(int nCustNo)
	{
		String sql = "SELECT " + KEY_ITEM_NO + " FROM " + TABLE_ORDER_HISTORY_INFO + " WHERE " + KEY_CUST_NO + " = " + nCustNo + " GROUP BY " + KEY_ITEM_NO;

		Cursor cursor = mDb.rawQuery(sql, null);
		Map<String, String> itemNoList = new HashMap();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {

					String strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					itemNoList.put(strItemNo, strItemNo);
					cursor.moveToNext();
				}
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return itemNoList;
	}

	public List<String> getAllOrderHistoryItem()
	{
		String sql = "SELECT " + KEY_ITEM_NO + " FROM " + TABLE_ORDER_HISTORY_INFO + " GROUP BY " + KEY_ITEM_NO;

		Cursor cursor = mDb.rawQuery(sql, null);
		List<String> itemNoList = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {

					String strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					itemNoList.add(strItemNo);
					cursor.moveToNext();
				}
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return itemNoList;
	}

	public List<OrderHistoryInfo> getOrderHistoryList(int nCustNo, String strItemNo)
	{
		String sql = "SELECT A.*, B." + KEY_SCW_TYPE +" FROM " + TABLE_ORDER_HISTORY_INFO + " A, " + TABLE_PRODLOCN_INFO + " B WHERE A." + KEY_CUST_NO + " = " + nCustNo
				+ " AND A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO + " AND A." + KEY_ITEM_NO + " = '" + strItemNo + "'";

		Cursor cursor = mDb.rawQuery(sql, null);
		OrderHistoryInfo info = null;
		List<OrderHistoryInfo> infos = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					info = new OrderHistoryInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strOrderDate = cursor.getString(cursor.getColumnIndex(KEY_ORDER_DATE));
					info.nSAQty = cursor.getInt(cursor.getColumnIndex(KEY_SAQTY));
					info.nBBQty = cursor.getInt(cursor.getColumnIndex(KEY_BBQTY));
					info.nDUQty = cursor.getInt(cursor.getColumnIndex(KEY_DUQTY));
					info.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
					infos.add(info);
					cursor.moveToNext();
				}
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public List<String> getOrderHistoryItemNo(int nCustNo)
	{
		String sql = "SELECT * FROM " + TABLE_ORDER_HISTORY_INFO + " WHERE " + KEY_CUST_NO + " = " + nCustNo + " GROUP BY " + KEY_ITEM_NO;

		Cursor cursor = mDb.rawQuery(sql, null);
		OrderHistoryInfo info = null;
		List<String> itemList = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					String strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					itemList.add(strItemNo);
					cursor.moveToNext();
				}
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return itemList;
	}

	public List<OrderHistoryInfo> getOrderHistoryList(int nCustNo)
	{
		String sql = "SELECT A.*, B." + KEY_SCW_TYPE +" FROM " + TABLE_ORDER_HISTORY_INFO + " A, " + TABLE_PRODLOCN_INFO + " B WHERE A." + KEY_CUST_NO + " = " + nCustNo
				+ " AND A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO;

		Cursor cursor = mDb.rawQuery(sql, null);
		OrderHistoryInfo info = null;
		List<OrderHistoryInfo> infos = new ArrayList<>();
		try
		{
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					info = new OrderHistoryInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strOrderDate = cursor.getString(cursor.getColumnIndex(KEY_ORDER_DATE));
					info.nSAQty = cursor.getInt(cursor.getColumnIndex(KEY_SAQTY));
					info.nBBQty = cursor.getInt(cursor.getColumnIndex(KEY_BBQTY));
					info.nDUQty = cursor.getInt(cursor.getColumnIndex(KEY_DUQTY));
					info.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
					infos.add(info);
					cursor.moveToNext();
				}
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infos;
	}

	public void deleteAllOrderHistoryInfos()
	{
		mDb.delete(TABLE_ORDER_HISTORY_INFO, null, null);
	}

	public void getProdStruParentIdEntryMap(Map<String, String> map)
	{
		String sql = " SELECT " + KEY_PARENT_ID + ", " + KEY_ENTRY_ID + " FROM " + TABLE_PRODSTRU_INFO ;
		Cursor cursor = mDb.rawQuery(sql, null);
		map.clear();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					String strEntryID = cursor.getString(cursor.getColumnIndex(KEY_ENTRY_ID));
					String strParentID = cursor.getString(cursor.getColumnIndex(KEY_PARENT_ID));
					map.put(strEntryID, strParentID);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public void getProdStruObjectEntryIDMap(Map<String, String> map)
	{
		String sql =	" SELECT	" + KEY_ENTRY_ID + ", " + KEY_REFERENCE +" FROM " + TABLE_PRODSTRU_INFO + " WHERE " + KEY_OBJECT_TYPE + " = 'P'";
		Cursor cursor = mDb.rawQuery(sql, null);
		map.clear();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					String strItemNo = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
					String strEntryID = cursor.getString(cursor.getColumnIndex(KEY_ENTRY_ID));
					map.put(strItemNo, strEntryID);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public int getBeginPresoldCalc(String strItemNo)
	{
		String sql = "SELECT " + KEY_ORDER_QTY + " FROM " + TABLE_PRESOLDD_INFO + " WHERE " + KEY_ITEM_NO + " = '" + strItemNo + "' AND " + KEY_QTY_OPTION + " = 1";
		int nBeginPresold = 0;
		Cursor cursor = mDb.rawQuery(sql, null);
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					int nOrderQty = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_QTY));
					nBeginPresold += nOrderQty;
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return nBeginPresold;
	}


	public double getBeginPresoldCalcD(String strItemNo)
	{
		String sql = "SELECT " + KEY_DORDER_QTY + " FROM " + TABLE_PRESOLDD_INFO + " WHERE " + KEY_ITEM_NO + " = '" + strItemNo + "' AND " + KEY_QTY_OPTION + " = 1";
		double dBeginPresold = 0;
		Cursor cursor = mDb.rawQuery(sql, null);
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					double dOrderQty = cursor.getDouble(cursor.getColumnIndex(KEY_DORDER_QTY));
					dBeginPresold += dOrderQty;
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return dBeginPresold;
	}

	public List<TripUser> getTripUserList()
	{
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_TRIP_USER_INFO;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<TripUser> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					TripUser info = new TripUser();
					info.strRouteNumber = cursor.getString(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strSecurity = cursor.getString(cursor.getColumnIndex(KEY_SECURITY));
					info.strUserName = cursor.getString(cursor.getColumnIndex(KEY_USER_NAME));
					info.strTrip = cursor.getString(cursor.getColumnIndex(KEY_TRIP));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public boolean deleteTripUser(){
		return mDb.delete(TABLE_TRIP_USER_INFO, null, null) > 0;
	}

	public long insertTripUser(TripUser info){
		ContentValues values = new ContentValues();
		values.put(KEY_ROUTE_NUMBER, info.strRouteNumber);
		values.put(KEY_DESCRIPTION, info.strDescription);
		values.put(KEY_SECURITY, info.strSecurity);
		values.put(KEY_USER_NAME, info.strUserName);
		values.put(KEY_TRIP, info.strTrip);

		return mDb.insert(TABLE_TRIP_USER_INFO, null, values);
	}

	public void updatePresoldOrHCustomerIdx(PresoldOrHInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_CUST_IDX, info.nCustIdx);
		mDb.update(TABLE_PRESOLDH_INFO, values, KEY_IDX + "=" + info.nIdx, null);
	}

	public PaymentCollectInfo getPaymentCollectInfo(String strDocNo)
	{
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_COLLECT_PAYMENT_STATUS + " WHERE " + KEY_DOC_NO + " = '" + strDocNo + "'";
		Cursor cursor = mDb.rawQuery(sql, null);
		PaymentCollectInfo info = null;
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();

				info = new PaymentCollectInfo();
				info.id = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
				info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
				info.strPayMethod = cursor.getString(cursor.getColumnIndex(KEY_PAY_METHOD));
				info.strKind = cursor.getString(cursor.getColumnIndex(KEY_KIND));
				info.strOther = cursor.getString(cursor.getColumnIndex(KEY_OTHER));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	public void insertCollectPaymentStatus(PaymentCollectInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_DOC_NO, info.strDocNo);
		values.put(KEY_PAY_METHOD, info.strPayMethod);
		values.put(KEY_KIND, info.strKind);
		values.put(KEY_OTHER, info.strOther);

		mDb.insert(TABLE_COLLECT_PAYMENT_STATUS, null, values);
	}

	public void updateCollectPaymentStatus(PaymentCollectInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_PAY_METHOD, info.strPayMethod);
		values.put(KEY_KIND, info.strKind);
		values.put(KEY_OTHER, info.strOther);
		mDb.update(TABLE_COLLECT_PAYMENT_STATUS, values, KEY_DOC_NO + "='" + info.strDocNo + "'", null);
	}

	public void deleteCollectPaymentStatus()
	{
		mDb.delete(TABLE_COLLECT_PAYMENT_STATUS, null, null);
	}

	public void insertDayNo(int nDayNo)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_DAY_NO, nDayNo);
		mDb.insert(TABLE_DAY_NO_INFO, null, values);
	}

	public void deleteDayNo()
	{
		mDb.delete(TABLE_DAY_NO_INFO, null, null);
	}

	public List<Integer> getDayNo()
	{
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_DAY_NO_INFO + " ORDER BY " + KEY_DAY_NO + " ASC";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<Integer> dayNoList = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					int nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
					dayNoList.add(nDayNo);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return dayNoList;
	}

	public void deleteWhiteCustomerList()
    {
        mDb.delete(TABLE_WHITE_CUSTOMER_INFO, null, null);
    }

	public void registerWhiteCustomer(int nChainNo, int nCustNo, int nDayNo)
    {
        ContentValues values = new ContentValues();
        values.put(KEY_CHAIN_NO, nChainNo);
        values.put(KEY_CUST_NO, nCustNo);
        values.put(KEY_DAY_NO, nDayNo);

        mDb.insert(TABLE_WHITE_CUSTOMER_INFO, null, values);
    }

	public List<CustomerInfo> getWhiteCustomerInfo(boolean bDeferredCustomer, int nDayNo)
    {
		String sql;
    	if(bDeferredCustomer)
		{
			sql = "SELECT A.* FROM " + TABLE_CUSTOMER_INFO + " A, " + TABLE_WHITE_CUSTOMER_INFO + " B" +
					" WHERE A." + KEY_DEFRRED_STATUS + " = 3 AND A." + KEY_CUST_NO + " = B." + KEY_CUST_NO + " AND A." + KEY_CHAIN_NO + " = B." + KEY_CHAIN_NO + " AND "
					+ " B." + KEY_DAY_NO + " = " + nDayNo + " AND A." + KEY_IS_ROUTE_SCH + " = 1 ORDER BY B." + KEY_IDX + " ASC";
		}
    	else
		{
			sql = "SELECT A.* FROM " + TABLE_CUSTOMER_INFO + " A, " + TABLE_WHITE_CUSTOMER_INFO + " B" +
					" WHERE A." + KEY_CUST_NO + " = B." + KEY_CUST_NO + " AND A." + KEY_CHAIN_NO + " = B." + KEY_CHAIN_NO + " AND "
					+ " B." + KEY_DAY_NO + " = " + nDayNo + " AND A." + KEY_IS_ROUTE_SCH + " = 1 ORDER BY B." + KEY_IDX + " ASC";
		}

        Cursor cursor = mDb.rawQuery(sql, null);
        List<CustomerInfo> list = new ArrayList<>();
        try {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    CustomerInfo info = new CustomerInfo();
                    info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
                    info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
                    info.strAltCustNo = cursor.getString(cursor.getColumnIndex(KEY_ALT_CUST_NO));
                    info.strName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                    info.strCompanyTaxID = cursor.getString(cursor.getColumnIndex(KEY_COMPANY_TAX_ID));
                    info.strCity = cursor.getString(cursor.getColumnIndex(KEY_CITY));
                    info.strAddress1 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS1));
                    info.strAddress2 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS2));
                    info.strPhone = cursor.getString(cursor.getColumnIndex(KEY_PHONE));
                    info.strZip = cursor.getString(cursor.getColumnIndex(KEY_ZIP));
                    info.strBuyerName = cursor.getString(cursor.getColumnIndex(KEY_BUYER_NAME));
                    info.strBuyerPhone = cursor.getString(cursor.getColumnIndex(KEY_BUYER_PHONE));
                    info.strAccountName = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_NAME));
                    info.strAccountPhone = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_PHONE));
                    info.nLocation = cursor.getInt(cursor.getColumnIndex(KEY_LOCATION));
                    info.nRouteNo = cursor.getInt(cursor.getColumnIndex(KEY_ROUTE_NUMBER));
                    info.strPriority = cursor.getString(cursor.getColumnIndex(KEY_PRIORITY));
                    info.nPromoPlan = cursor.getInt(cursor.getColumnIndex(KEY_PROMO_PLAN));
                    info.nTemplateGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMPLATE_GRP));
                    info.nTempGrp = cursor.getInt(cursor.getColumnIndex(KEY_TEMP_GRP));
                    info.nFeatGrp = cursor.getInt(cursor.getColumnIndex(KEY_FEAT_GRP));
                    info.nPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_PRICE_GRP));
                    info.nAltPriceGrp = cursor.getInt(cursor.getColumnIndex(KEY_ALT_PRICE_GRP));
                    info.strTaxCode = cursor.getString(cursor.getColumnIndex(KEY_TAX_CODE));
                    info.nDayNo = cursor.getInt(cursor.getColumnIndex(KEY_DAY_NO));
                    info.nSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_SEQ_NO));
                    info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
                    info.bIsRouteSch = cursor.getInt(cursor.getColumnIndex(KEY_IS_ROUTE_SCH)) == 1 ? true : false;
                    info.latitude = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE));
                    info.longitude = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE));
                    info.bCompleted = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED)) == 1 ? true : false;
                    info.strStartTime1 = cursor.getString(cursor.getColumnIndex(KEY_START_TIME1));
                    info.strEndTime1 = cursor.getString(cursor.getColumnIndex(KEY_END_TIME1));
                    info.strStartTime2 = cursor.getString(cursor.getColumnIndex(KEY_START_TIME2));
                    info.strEndTime2 = cursor.getString(cursor.getColumnIndex(KEY_END_TIME2));
                    info.strDeliveryDate = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_DATE));
                    info.nPeriodNo = cursor.getInt(cursor.getColumnIndex(KEY_PERIOD_NO));
                    info.nOrderNo = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_NO));
                    info.strShipToState = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_STATE));
                    info.strShipToZip = cursor.getString(cursor.getColumnIndex(KEY_SHIP_TO_ZIP));
                    info.nInvoiceFmt = cursor.getInt(cursor.getColumnIndex(KEY_INVOICE_FMT));
                    info.nCustPriceMaintFlag = cursor.getInt(cursor.getColumnIndex(KEY_CUST_PRICE_MAINT_FLAG));
                    info.nShowPrice = cursor.getInt(cursor.getColumnIndex(KEY_SHOW_PRICE));
                    info.nDelayedPrint = cursor.getInt(cursor.getColumnIndex(KEY_DELAYED_FLAG));
                    info.strPayType = cursor.getString(cursor.getColumnIndex(KEY_PAY_TYPE));
                    info.strBillName = cursor.getString(cursor.getColumnIndex(KEY_BILL_NAME));
                    info.strBillAddr1 = cursor.getString(cursor.getColumnIndex(KEY_BILL_ADDR1));
                    info.strBillAddr2 = cursor.getString(cursor.getColumnIndex(KEY_BILL_ADDR2));
                    info.strBillCity = cursor.getString(cursor.getColumnIndex(KEY_BILL_CITY));
                    info.strBillPeriod = cursor.getString(cursor.getColumnIndex(KEY_BILL_PERIOD));
                    info.strVendorNo = cursor.getString(cursor.getColumnIndex(KEY_VENDOR_NO));
                    info.nBillCustNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CUST_NO));
                    info.nBillChainNo = cursor.getInt(cursor.getColumnIndex(KEY_BILL_CHAIN_NO));
                    info.strState = cursor.getString(cursor.getColumnIndex(KEY_STATE));
                    info.strSignature = cursor.getString(cursor.getColumnIndex(KEY_SIGNATURE));
                    info.strSalEntryMode = cursor.getString(cursor.getColumnIndex(KEY_SALENTRY_MODE));
                    info.strRtnEntryMode = cursor.getString(cursor.getColumnIndex(KEY_RTNENTRY_MODE));
                    info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
                    info.strTerms = cursor.getString(cursor.getColumnIndex(KEY_TERMS));
                    info.nImageCapture = cursor.getInt(cursor.getColumnIndex(KEY_IMAGE_CAPTURE));
                    info.strDexVersion = cursor.getString(cursor.getColumnIndex(KEY_DEX_VERSION));
                    info.nDeferredStatus = cursor.getInt(cursor.getColumnIndex(KEY_DEFRRED_STATUS));
                    info.nCompleteStatus = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED_STATUS));
                    info.nOrgSeqNo = cursor.getInt(cursor.getColumnIndex(KEY_ORG_SEQ_NO));
                    info.strArrivalTime = cursor.getString(cursor.getColumnIndex(KEY_ARRIVAL_TIME));
                    info.strAuthGrp = cursor.getString(cursor.getColumnIndex(KEY_AUTH_GRP));
                    info.driverLatitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LATITUDE));
                    info.driverLongitude = cursor.getDouble(cursor.getColumnIndex(KEY_DRIVER_LONGITUDE));
                    info.strDeliveryWindowTo = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_TO));
                    info.strDeliveryWindowFrom = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_WINDOW_FROM));
                    info.nCreditHold = cursor.getInt(cursor.getColumnIndex(KEY_CREDIT_HOLD));
                    info.nDeliveryFreq = cursor.getInt(cursor.getColumnIndex(KEY_DELIVERY_FREQ));
                    info.strStoreNo = cursor.getString(cursor.getColumnIndex(KEY_STORE_NO));
					info.strDeliveryType = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_TYPE));
					info.strSalesDistrict = cursor.getString(cursor.getColumnIndex(KEY_SALES_DISTRICT));
					info.fHandlingFee = cursor.getFloat(cursor.getColumnIndex(KEY_HANDLING_FEE));
					info.fMiniumOrderValue = cursor.getFloat(cursor.getColumnIndex(KEY_MINIMUM_ORDER_VALUE));
					info.nConSales = cursor.getInt(cursor.getColumnIndex(KEY_CON_SALES));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strUser1 = cursor.getString(cursor.getColumnIndex(KEY_USER1));
					info.strInvoiceDueDate = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_DATE));
					info.strInvoiceDueMethod = cursor.getString(cursor.getColumnIndex(KEY_INVOICE_DUE_METHOD));
					info.strPONumberRequired = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_REQUIRED));
					info.strPONumberMask = cursor.getString(cursor.getColumnIndex(KEY_PO_NUMBER_MASK));
					info.strLegalName = cursor.getString(cursor.getColumnIndex(KEY_LEGAL_NAME));
					info.strCreditLimit = cursor.getString(cursor.getColumnIndex(KEY_CREDIT_LIMIT));

					info.bFromAddCustomer = true;
                    cursor.moveToNext();
                    list.add(info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    public List<CustomerNoteInfo> getCustomerNoteInfoList(int nCustNo, int nChainNo)
	{
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_CUSTOMER_NOTE_INFO + " WHERE " + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_CHAIN_NO + " = " + nChainNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<CustomerNoteInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					CustomerNoteInfo info = new CustomerNoteInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.strNoteType = cursor.getString(cursor.getColumnIndex(KEY_NOTE_TYPE));
					info.strNoteDate = cursor.getString(cursor.getColumnIndex(KEY_NOTE_DATE));
					info.strNoteTime = cursor.getString(cursor.getColumnIndex(KEY_NOTE_TIME));
					info.strCreatedBy = cursor.getString(cursor.getColumnIndex(KEY_NOTE_CREATED_BY));
					info.strNote = cursor.getString(cursor.getColumnIndex(KEY_NOTE));
					info.strNoteId = cursor.getString(cursor.getColumnIndex(KEY_NOTE_ID));
					info.nAttachment = cursor.getInt(cursor.getColumnIndex(KEY_ATTACHMENT));
					info.strCreatedTimeAndNoteDate = cursor.getString(cursor.getColumnIndex(KEY_CREATED_TIME_AND_NOTE_DATE));

					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public void insertCustomerNoteInfo(CustomerNoteInfo info){
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_NOTE_TYPE, info.strNoteType);
		values.put(KEY_NOTE_DATE, info.strNoteDate);
		values.put(KEY_NOTE_TIME, info.strNoteTime);
		values.put(KEY_NOTE_CREATED_BY, info.strCreatedBy);
		values.put(KEY_NOTE, info.strNote);
		values.put(KEY_NOTE_ID, info.strNoteId);
		values.put(KEY_ATTACHMENT, info.nAttachment);
		values.put(KEY_CREATED_TIME_AND_NOTE_DATE, info.strCreatedTimeAndNoteDate);

		mDb.insert(TABLE_CUSTOMER_NOTE_INFO, null, values);
	}

	public void updateCustomerNoteInfo(CustomerNoteInfo info){
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_NOTE_TYPE, info.strNoteType);
		values.put(KEY_NOTE_DATE, info.strNoteDate);
		values.put(KEY_NOTE_TIME, info.strNoteTime);
		values.put(KEY_NOTE_CREATED_BY, info.strCreatedBy);
		values.put(KEY_NOTE, info.strNote);
		values.put(KEY_NOTE_ID, info.strNoteId);
		values.put(KEY_ATTACHMENT, info.nAttachment);
		values.put(KEY_CREATED_TIME_AND_NOTE_DATE, info.strCreatedTimeAndNoteDate);
		mDb.update(TABLE_CUSTOMER_NOTE_INFO, values, KEY_IDX + " = " + info.nIdx, null);
	}

	public void deleteCustomerNoteInfo(CustomerNoteInfo info){
		mDb.delete(TABLE_CUSTOMER_NOTE_INFO, KEY_IDX + " = " + info.nIdx , null);
	}

	public void deleteAllCustomerNote()
	{
		mDb.delete(TABLE_CUSTOMER_NOTE_INFO, null, null);
	}


	public List<UCustomerNoteInfo> getUCustomerInfoList(long nTrxnNo)
	{
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_UCUSTOMER_NOTE_INFO + " WHERE " + KEY_TRXN_NO + " = " + nTrxnNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UCustomerNoteInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UCustomerNoteInfo info = new UCustomerNoteInfo();
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
					info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strSalesDate = cursor.getString(cursor.getColumnIndex(KEY_SALES_DATE));
					info.strTransactionType = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TYPE));
					info.strUser = cursor.getString(cursor.getColumnIndex(KEY_USER));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.strNoteType = cursor.getString(cursor.getColumnIndex(KEY_NOTE_TYPE));
					info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
					info.strMessage = cursor.getString(cursor.getColumnIndex(KEY_MESSAGE));
					info.nAttachment = cursor.getInt(cursor.getColumnIndex(KEY_ATTACHMENT));

					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<UCustomerNoteInfo> getUCustomerInfoList(int nCustNo, int nChainNo)
	{
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_UCUSTOMER_NOTE_INFO + " WHERE " + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_CHAIN_NO + " = " + nChainNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UCustomerNoteInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UCustomerNoteInfo info = new UCustomerNoteInfo();
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
					info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strSalesDate = cursor.getString(cursor.getColumnIndex(KEY_SALES_DATE));
					info.strTransactionType = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TYPE));
					info.strUser = cursor.getString(cursor.getColumnIndex(KEY_USER));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.strNoteType = cursor.getString(cursor.getColumnIndex(KEY_NOTE_TYPE));
					info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
					info.strMessage = cursor.getString(cursor.getColumnIndex(KEY_MESSAGE));
					info.nAttachment = cursor.getInt(cursor.getColumnIndex(KEY_ATTACHMENT));
					if(info.nAttachment > 0)
					{
						info.uAttachmentInfoList = getUAttachmentInfo(info.nTrxnNo);
					}
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<UCustomerNoteInfo> getUCustomerInfoList(int nCustNo, int nChainNo, String strNoteType, String strNoteDate, String strNoteTime)
	{
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_UCUSTOMER_NOTE_INFO + " WHERE " + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_CHAIN_NO + " = " + nChainNo + " AND " + KEY_NOTE_TYPE + " = '" + strNoteType+ "' AND " + KEY_TRXN_DATE + " = '" + strNoteDate + "' AND " + KEY_TRXN_TIME + " = '" + strNoteTime + "'";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UCustomerNoteInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UCustomerNoteInfo info = new UCustomerNoteInfo();
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
					info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strSalesDate = cursor.getString(cursor.getColumnIndex(KEY_SALES_DATE));
					info.strTransactionType = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TYPE));
					info.strUser = cursor.getString(cursor.getColumnIndex(KEY_USER));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.strNoteType = cursor.getString(cursor.getColumnIndex(KEY_NOTE_TYPE));
					info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
					info.strMessage = cursor.getString(cursor.getColumnIndex(KEY_MESSAGE));
					info.nAttachment = cursor.getInt(cursor.getColumnIndex(KEY_ATTACHMENT));
					if(info.nAttachment > 0)
					{
						info.uAttachmentInfoList = getUAttachmentInfo(info.nTrxnNo);
					}
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public void insertUCustomerNoteInfo(UCustomerNoteInfo info){
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_DOC_NO, info.strDocNo);
		values.put(KEY_DOC_TYPE, info.strDocType);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_SALES_DATE, info.strSalesDate);
		values.put(KEY_TRXN_TYPE, info.strTransactionType);
		values.put(KEY_USER, info.strUser);
		values.put(KEY_MESSAGE, info.strMessage);
		values.put(KEY_NOTE_TYPE, info.strNoteType);
		values.put(KEY_REFERENCE, info.strReference);
		values.put(KEY_ATTACHMENT, info.nAttachment);

		mDb.insert(TABLE_UCUSTOMER_NOTE_INFO, null, values);
	}

	public void deleteAllUCustomerNote()
	{
		mDb.delete(TABLE_UCUSTOMER_NOTE_INFO, null, null);
	}

	public List<UAttachmentInfo> getUAttachmentInfo(long nTrxnNo)
	{
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_UATTACHMENT_INFO + " WHERE " + KEY_TRXN_NO + " = " + nTrxnNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UAttachmentInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					UAttachmentInfo info = new UAttachmentInfo();
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strFileName = cursor.getString(cursor.getColumnIndex(KEY_FILE_NAME));
					info.strFilePath = cursor.getString(cursor.getColumnIndex(KEY_FILE_PATH));

					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public void insertUAttachmentInfo(UAttachmentInfo info){
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_FILE_NAME, info.strFileName);
		values.put(KEY_FILE_PATH, info.strFilePath);

		mDb.insert(TABLE_UATTACHMENT_INFO, null, values);
	}

	public void deleteAllUAttachment()
	{
		mDb.delete(TABLE_UATTACHMENT_INFO, null, null);
	}


	//Transaction History Table

	public void insertTransactionHistoryInfo(TransactionInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_DOC_NO, info.strDocNo);
		values.put(KEY_TRXN_TIME, info.strTime);
		values.put(KEY_NAME, info.strCustomerName);
		values.put(KEY_TYPE, info.strType);
		values.put(KEY_VOID_FLAG, info.strVoid);
		values.put(KEY_TRXN_AMOUNT, info.value);

		mDb.insert(TABLE_TRANSACTION_HISTORY_INFO, null, values);
	}

	public void deleteAllTransactionHistoryInfo()
	{
		mDb.delete(TABLE_TRANSACTION_HISTORY_INFO, null, null);
	}

	public List<TransactionInfo> getAllTransactionHistory(){
		String sql =	" SELECT	* FROM		" + TABLE_TRANSACTION_HISTORY_INFO;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<TransactionInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					TransactionInfo info = new TransactionInfo();
					info.id = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
					info.strTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.strCustomerName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strVoid = cursor.getString(cursor.getColumnIndex(KEY_VOID_FLAG));
					info.value = cursor.getFloat(cursor.getColumnIndex(KEY_TRXN_AMOUNT));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public ProdDetlInfo getProdDetl(String strItemNo) {
		String sql =	" SELECT	A.*, B." + KEY_UNIT_UOM + ", B." + KEY_CASE_UOM + ", B." + KEY_FULL_CASE + ", B." + KEY_SCW_TYPE + ", B." + KEY_ORDER_SEQ + ", B." + KEY_CASE_FACTOR +
				" FROM		" + TABLE_PRODDETL_INFO + " A, " + TABLE_PRODLOCN_INFO + " B " +
				" WHERE		A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO + " AND B." + KEY_ITEM_NO + " = '" + strItemNo + "'";
		Cursor cursor = mDb.rawQuery(sql, null);
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				{
					ProdDetlInfo info = new ProdDetlInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
					info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
					info.strItemUPC = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC));
					info.strItemUPC20 = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC20));
					info.nWeight = cursor.getFloat(cursor.getColumnIndex(KEY_WEIGHT));
					info.strItemType = cursor.getString(cursor.getColumnIndex(KEY_ITEM_TYPE));
					info.strProdGrp = cursor.getString(cursor.getColumnIndex(KEY_PROD_GRP));
					info.strPromoGrp = cursor.getString(cursor.getColumnIndex(KEY_PROMO_GRP));
					info.strMarketGrp = cursor.getString(cursor.getColumnIndex(KEY_MARKET_GRP));
					info.strProdLine = cursor.getString(cursor.getColumnIndex(KEY_PROD_LINE));
					info.strPackSize = cursor.getString(cursor.getColumnIndex(KEY_PACK_SIZE));
					info.nShelfLife = cursor.getInt(cursor.getColumnIndex(KEY_SHELF_LIFE));
					info.strBrand = cursor.getString(cursor.getColumnIndex(KEY_BRAND));
					info.strSubBrand = cursor.getString(cursor.getColumnIndex(KEY_SUB_BRAND));
					info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
					info.strActiveDate = cursor.getString(cursor.getColumnIndex(KEY_ACTIVE_DATE));
					info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
					info.strSalesAllowed = cursor.getString(cursor.getColumnIndex(KEY_SALES_ALLOWED));
					info.strItemInactiveSwitch = cursor.getString(cursor.getColumnIndex(KEY_ITEM_INACTIVE_SWITCH));
					info.strGroupFilterKey = cursor.getString(cursor.getColumnIndex(KEY_GROUP_FILTER_KEY));
					info.strServiceFee = cursor.getString(cursor.getColumnIndex(KEY_SERVICE_FEE));
					info.strImageUrl = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URL));
					info.strLotControl = cursor.getString(cursor.getColumnIndex(KEY_LOT_CONTROL));
					info.prodLocnInfo.strUnitUOM = cursor.getString(cursor.getColumnIndex(KEY_UNIT_UOM));
					info.prodLocnInfo.strCaseUOM = cursor.getString(cursor.getColumnIndex(KEY_CASE_UOM));
					info.prodLocnInfo.strFullCase = cursor.getString(cursor.getColumnIndex(KEY_FULL_CASE));
					info.prodLocnInfo.nOrderSeq = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_SEQ));
					info.prodLocnInfo.nCaseFactor = cursor.getInt(cursor.getColumnIndex(KEY_CASE_FACTOR));
					info.prodLocnInfo.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
					info.nConsumerUnit = cursor.getInt(cursor.getColumnIndex(KEY_CONSUMER_UNIT));
					cursor.close();
					return info;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	public void registerSurveyInfoCompleted(String strSurveyID, int nCustNo, int nChainNo)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_SURVEY_ID, strSurveyID);
		values.put(KEY_CUST_NO, nCustNo);
		values.put(KEY_CHAIN_NO, nChainNo);

		mDb.insert(TABLE_SURVEY_COMPLETED, null, values);
	}

	public boolean isCompletedSurvey(String strSurveyID, int nCustNo, int nChainNo)
	{
		String sql = "SELECT * FROM " + TABLE_SURVEY_COMPLETED + " WHERE " + KEY_SURVEY_ID + " = '" + strSurveyID + "'" +
				" AND " + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_CHAIN_NO + " = " + nChainNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.close();
				cursor = null;
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return false;
	}

	public void deleteCompletedSurvey()
	{
		mDb.delete(TABLE_SURVEY_COMPLETED, null, null);
	}

	//Cust Additional Info Table
	public long insertCustAddInfo(CustomerAddInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_INFO_TYPE, info.strInfoType);
		values.put(KEY_INFO, info.strInfo);

		return mDb.insert(TABLE_CUST_ADD_INFO, null, values);
	}

	public List<CustomerAddInfo> getCustAddInfoList(int nCustNo, int nChainNo, String strInfoType) {
		String sql =	"SELECT * FROM " + TABLE_CUST_ADD_INFO + " WHERE " + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_CHAIN_NO + " = " + nChainNo + " AND " + KEY_INFO_TYPE + " = '" + strInfoType + "'";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<CustomerAddInfo> infoList = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					CustomerAddInfo info = new CustomerAddInfo();
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.strInfo = cursor.getString(cursor.getColumnIndex(KEY_INFO));
					info.strInfoType = cursor.getString(cursor.getColumnIndex(KEY_INFO_TYPE));
					infoList.add(info);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return infoList;
	}

	public void deleteAllCustAddInfo()
	{
		mDb.delete(TABLE_CUST_ADD_INFO, null, null);
	}

	//UVisitInfo
	public long insertUVisitInfo(UVisitInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_DOC_TYPE, info.strDocType);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		values.put(KEY_VOID_FLAG, info.nVoidFlag);
		values.put(KEY_PRINTED_FLAG, info.nPrintedFlag);
		values.put(KEY_REFERENCE, info.strReference);
		values.put(KEY_TCOMSTATUS, info.strTCOMStatus);
		values.put(KEY_SALES_DATE, info.strSaleDate);
		values.put(KEY_VISIT_START, info.strVisitStart);
		values.put(KEY_VISIT_END, info.strVisitEnd);
		values.put(KEY_VISIT_REASON, info.strVisitReason);
		values.put(KEY_VISIT_REASON_ID, info.strVisitReasonID);
		values.put(KEY_NEXT_VISIT, info.strNextVisit);
		values.put(KEY_NEXT_VISIT_START, info.strNextVisitStart);
		values.put(KEY_NEXT_VISIT_END, info.strNextVisitEnd);
		values.put(KEY_VISIT_MESSAGE, info.strVisitMessage);
		values.put(KEY_CUSTOMER_UPDATED, info.nCustomerUpdated);
		values.put(KEY_RANGE_CHECKED, info.nRangeChecked);
		values.put(KEY_ORDER_CREATED, info.nOrderCreated);
		values.put(KEY_SURVEY_COMPLETED, info.nSurveyCompleted);
		values.put(KEY_ASSET_CHECKED, info.nAssetChecked);
		values.put(KEY_ASSET_REQUESTED, info.nAssetRequested);
		values.put(KEY_VISIT_FREQUENCY, info.nVisitFrequency);
		values.put(KEY_PREFERRED_VISIT_DAY, info.nPreferredVisitDay);
		values.put(KEY_PLANNED_VISIT_TIME, info.strPlannedVisitTime);
		values.put(KEY_VISIT_TYPE, info.nVisitType);
		values.put(KEY_VISIT_TYPE_ID, info.strVisitTypeId);

		return mDb.insert(TABLE_UVISIT_INFO, null, values);
	}

	public List<UVisitInfo> getUVisitInfo(long nTrxnNo)
	{
		String sql =	"SELECT * FROM " + TABLE_UVISIT_INFO + " WHERE " + KEY_TRXN_NO + " = " + nTrxnNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UVisitInfo> uVisitInfoList = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					UVisitInfo info = new UVisitInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.nVoidFlag = cursor.getInt(cursor.getColumnIndex(KEY_VOID_FLAG));
					info.nPrintedFlag = cursor.getInt(cursor.getColumnIndex(KEY_PRINTED_FLAG));
					info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
					info.strTCOMStatus = cursor.getString(cursor.getColumnIndex(KEY_TCOMSTATUS));
					info.strSaleDate = cursor.getString(cursor.getColumnIndex(KEY_SALES_DATE));
					info.strVisitStart = cursor.getString(cursor.getColumnIndex(KEY_VISIT_START));
					info.strVisitEnd = cursor.getString(cursor.getColumnIndex(KEY_VISIT_END));
					info.strVisitReason = cursor.getString(cursor.getColumnIndex(KEY_VISIT_REASON));
					info.strVisitReasonID = cursor.getString(cursor.getColumnIndex(KEY_VISIT_REASON_ID));
					info.strNextVisit = cursor.getString(cursor.getColumnIndex(KEY_NEXT_VISIT));
					info.strNextVisitStart = cursor.getString(cursor.getColumnIndex(KEY_NEXT_VISIT_START));
					info.strNextVisitEnd = cursor.getString(cursor.getColumnIndex(KEY_NEXT_VISIT_END));
					info.strVisitMessage = cursor.getString(cursor.getColumnIndex(KEY_VISIT_MESSAGE));
					info.nCustomerUpdated = cursor.getInt(cursor.getColumnIndex(KEY_CUSTOMER_UPDATED));
					info.nRangeChecked = cursor.getInt(cursor.getColumnIndex(KEY_RANGE_CHECKED));
					info.nOrderCreated = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_CREATED));
					info.nSurveyCompleted = cursor.getInt(cursor.getColumnIndex(KEY_SURVEY_COMPLETED));
					info.nAssetChecked = cursor.getInt(cursor.getColumnIndex(KEY_ASSET_CHECKED));
					info.nAssetRequested = cursor.getInt(cursor.getColumnIndex(KEY_ASSET_REQUESTED));
					info.nVisitFrequency = cursor.getInt(cursor.getColumnIndex(KEY_VISIT_FREQUENCY));
					info.nPreferredVisitDay = cursor.getInt(cursor.getColumnIndex(KEY_PREFERRED_VISIT_DAY));
					info.strPlannedVisitTime = cursor.getString(cursor.getColumnIndex(KEY_PLANNED_VISIT_TIME));
					info.nVisitType = cursor.getInt(cursor.getColumnIndex(KEY_VISIT_TYPE));
					info.strVisitTypeId = cursor.getString(cursor.getColumnIndex(KEY_VISIT_TYPE_ID));
					uVisitInfoList.add(info);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return uVisitInfoList;
	}

	public List<UVisitInfo> getAllUVisitInfo()
	{
		String sql =	"SELECT * FROM " + TABLE_UVISIT_INFO;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<UVisitInfo> uVisitInfoList = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					UVisitInfo info = new UVisitInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.nVoidFlag = cursor.getInt(cursor.getColumnIndex(KEY_VOID_FLAG));
					info.nPrintedFlag = cursor.getInt(cursor.getColumnIndex(KEY_PRINTED_FLAG));
					info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
					info.strTCOMStatus = cursor.getString(cursor.getColumnIndex(KEY_TCOMSTATUS));
					info.strSaleDate = cursor.getString(cursor.getColumnIndex(KEY_SALES_DATE));
					info.strVisitStart = cursor.getString(cursor.getColumnIndex(KEY_VISIT_START));
					info.strVisitEnd = cursor.getString(cursor.getColumnIndex(KEY_VISIT_END));
					info.strVisitReason = cursor.getString(cursor.getColumnIndex(KEY_VISIT_REASON));
					info.strVisitReasonID = cursor.getString(cursor.getColumnIndex(KEY_VISIT_REASON_ID));
					info.strNextVisit = cursor.getString(cursor.getColumnIndex(KEY_NEXT_VISIT));
					info.strNextVisitStart = cursor.getString(cursor.getColumnIndex(KEY_NEXT_VISIT_START));
					info.strNextVisitEnd = cursor.getString(cursor.getColumnIndex(KEY_NEXT_VISIT_END));
					info.strVisitMessage = cursor.getString(cursor.getColumnIndex(KEY_VISIT_MESSAGE));
					info.nCustomerUpdated = cursor.getInt(cursor.getColumnIndex(KEY_CUSTOMER_UPDATED));
					info.nRangeChecked = cursor.getInt(cursor.getColumnIndex(KEY_RANGE_CHECKED));
					info.nOrderCreated = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_CREATED));
					info.nSurveyCompleted = cursor.getInt(cursor.getColumnIndex(KEY_SURVEY_COMPLETED));
					info.nAssetChecked = cursor.getInt(cursor.getColumnIndex(KEY_ASSET_CHECKED));
					info.nAssetRequested = cursor.getInt(cursor.getColumnIndex(KEY_ASSET_REQUESTED));
					info.nVisitFrequency = cursor.getInt(cursor.getColumnIndex(KEY_VISIT_FREQUENCY));
					info.nPreferredVisitDay = cursor.getInt(cursor.getColumnIndex(KEY_PREFERRED_VISIT_DAY));
					info.strPlannedVisitTime = cursor.getString(cursor.getColumnIndex(KEY_PLANNED_VISIT_TIME));
					info.nVisitType = cursor.getInt(cursor.getColumnIndex(KEY_VISIT_TYPE));
					info.strVisitTypeId = cursor.getString(cursor.getColumnIndex(KEY_VISIT_TYPE_ID));
					uVisitInfoList.add(info);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return uVisitInfoList;
	}

	public void deleteUVisitInfo(long nTrxnNo)
	{
		mDb.delete(TABLE_UVISIT_INFO, KEY_TRXN_NO + " = " + nTrxnNo, null);
	}

	public void deleteAllUVisitInfo()
	{
		mDb.delete(TABLE_UVISIT_INFO, null, null);
	}

	public boolean deleteAllUVisitInfo(MakeZipFileInfo zipFileInfo)
	{
		for(Long nTrxnNo : zipFileInfo.trxnList)
		{
			deleteUVisitInfo(nTrxnNo);
		}
		return true;
	}

	// CustConBal
	public List<CustConBalInfo> getCustConBalInfo(int nCustNo, int nChainNo) {
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_CUS_CON_BAL_INFO +
				" WHERE		" + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_CHAIN_NO + " = " + nChainNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<CustConBalInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					CustConBalInfo info = new CustConBalInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.nQty = cursor.getInt(cursor.getColumnIndex(KEY_QTY));
					info.fWeight = cursor.getFloat(cursor.getColumnIndex(KEY_WEIGHT));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public long insertCustConBalInfo(CustConBalInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_QTY, info.nQty);
		values.put(KEY_WEIGHT, info.fWeight);
		return mDb.insert(TABLE_CUS_CON_BAL_INFO, null, values);
	}

	public void updateCustConBalInfo(CustConBalInfo info) {
		ContentValues values = new ContentValues();
		if(info.nIdx != 0)
		{
			values.put(KEY_CHAIN_NO, info.nChainNo);
			values.put(KEY_CUST_NO, info.nCustNo);
			values.put(KEY_ITEM_NO, info.strItemNo);
			values.put(KEY_QTY, info.nQty);
			values.put(KEY_WEIGHT, info.fWeight);
			mDb.update(TABLE_CUS_CON_BAL_INFO, values, KEY_IDX + " = " + info.nIdx, null);
		}
		else
		{
			values.put(KEY_QTY, info.nQty);
			values.put(KEY_WEIGHT, info.fWeight);
			mDb.update(TABLE_CUS_CON_BAL_INFO, values, KEY_CHAIN_NO + " = " + info.nChainNo + " AND " + KEY_CUST_NO + " = " + info.nCustNo + " AND " + KEY_ITEM_NO + " = '" + info.strItemNo + "'", null);
		}
	}

	public void deleteAllCustConBalInfo()
	{
		mDb.delete(TABLE_CUS_CON_BAL_INFO, null, null);
	}


	public CustomerOrderInfo getLatestOrder(int nOrderNo)
	{
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_LATEST_ORDER_INFO +
				" WHERE		" + KEY_ORDER_NO + " = " + nOrderNo;
		CustomerOrderInfo info = null;
		Cursor cursor = mDb.rawQuery(sql, null);
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();

				info = new CustomerOrderInfo();
				info.chainNumber = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
				info.customerNumber = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
				info.orderType = cursor.getString(cursor.getColumnIndex(KEY_ORDER_TYPE));
				info.orderNumber = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_NO));
				info.purchaseOrderNumber = cursor.getString(cursor.getColumnIndex(KEY_PURCHASE_ORDER_NUMBER));
				info.deliveryInstructions = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_INSTRUCTIONS));
				Gson gson = new Gson();
				Type listType = new TypeToken<List<OrderLineInfo>>(){}.getType();
				info.orderLines = gson.fromJson(cursor.getString(cursor.getColumnIndex(KEY_ORDER_LINE)), listType);

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	public long insertLastestOrder(CustomerOrderInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.chainNumber);
		values.put(KEY_CUST_NO, info.customerNumber);
		values.put(KEY_ORDER_TYPE, info.orderType);
		values.put(KEY_ORDER_NO, info.orderNumber);
		values.put(KEY_PURCHASE_ORDER_NUMBER, info.purchaseOrderNumber);
		values.put(KEY_DELIVERY_INSTRUCTIONS, info.deliveryInstructions);
		values.put(KEY_STATUS, 0);
		Gson gson = new Gson();
		values.put(KEY_ORDER_LINE, gson.toJson(info.orderLines));
		return mDb.insert(TABLE_LATEST_ORDER_INFO, null, values);
	}

	public long insertIgnoredLastestOrder(CustomerOrderInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.chainNumber);
		values.put(KEY_CUST_NO, info.customerNumber);
		values.put(KEY_ORDER_TYPE, info.orderType);
		values.put(KEY_ORDER_NO, info.orderNumber);
		values.put(KEY_PURCHASE_ORDER_NUMBER, info.purchaseOrderNumber);
		values.put(KEY_DELIVERY_INSTRUCTIONS, info.deliveryInstructions);
		values.put(KEY_STATUS, 1);
		Gson gson = new Gson();
		values.put(KEY_ORDER_LINE, gson.toJson(info.orderLines));
		return mDb.insert(TABLE_LATEST_ORDER_INFO, null, values);
	}

	public void updateCustomerLatestOrderStatus(CustomerOrderInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_STATUS, 1);
		mDb.update(TABLE_LATEST_ORDER_INFO, values, KEY_ORDER_NO + " = " + info.orderNumber, null);
	}
	
	public void deleteLatestOrderList()
	{
		mDb.delete(TABLE_LATEST_ORDER_INFO, null, null);
	}

	public List<CustomerOrderInfo> getCustomerLatestOrderList(int nCustNo, int nChainNo)
	{
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_LATEST_ORDER_INFO +
				" WHERE		" + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_CHAIN_NO + " = " + nChainNo + " AND " + KEY_STATUS + " = 0";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<CustomerOrderInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					CustomerOrderInfo info = new CustomerOrderInfo();
					info.chainNumber = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.customerNumber = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.orderType = cursor.getString(cursor.getColumnIndex(KEY_ORDER_TYPE));
					info.orderNumber = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_NO));
					info.purchaseOrderNumber = cursor.getString(cursor.getColumnIndex(KEY_PURCHASE_ORDER_NUMBER));
					info.deliveryInstructions = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_INSTRUCTIONS));
					Gson gson = new Gson();
					Type listType = new TypeToken<List<OrderLineInfo>>(){}.getType();
					info.orderLines = gson.fromJson(cursor.getString(cursor.getColumnIndex(KEY_ORDER_LINE)), listType);
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<CustomerOrderInfo> getAllCustomerLatestOrderList()
	{
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_LATEST_ORDER_INFO +
				" WHERE		" + KEY_STATUS + " = 0";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<CustomerOrderInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					CustomerOrderInfo info = new CustomerOrderInfo();
					info.chainNumber = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.customerNumber = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.orderType = cursor.getString(cursor.getColumnIndex(KEY_ORDER_TYPE));
					info.orderNumber = cursor.getInt(cursor.getColumnIndex(KEY_ORDER_NO));
					info.purchaseOrderNumber = cursor.getString(cursor.getColumnIndex(KEY_PURCHASE_ORDER_NUMBER));
					info.deliveryInstructions = cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_INSTRUCTIONS));
					Gson gson = new Gson();
					Type listType = new TypeToken<List<OrderLineInfo>>(){}.getType();
					info.orderLines = gson.fromJson(cursor.getString(cursor.getColumnIndex(KEY_ORDER_LINE)), listType);
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	//Table For LangInfo
	public void insertLangInfo(String strKeyword, String strValue, String strLang)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_KEYWORD, strKeyword);
		values.put(KEY_VALUE, strValue);
		values.put(KEY_LANG, strLang);

		mDb.insert(TABLE_LANG_INFO, null, values);
	}

	public void deleteAllLangInfo()
	{
		mDb.delete(TABLE_LANG_INFO, null, null);
	}

	public Map<String, String> getLangInfoMap()
	{
		String sql = "SELECT * FROM " + TABLE_LANG_INFO;

		Cursor cursor = mDb.rawQuery(sql, null);
		Map<String, String> mapLangInfo = new HashMap<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					String strKeyword = cursor.getString(cursor.getColumnIndex(KEY_KEYWORD));
					String strLang = cursor.getString(cursor.getColumnIndex(KEY_LANG));
					String strValue = cursor.getString(cursor.getColumnIndex(KEY_VALUE));

					String strKey = strKeyword + "%%" + strLang;
					mapLangInfo.put(strKey, strValue);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return mapLangInfo;
	}

	public void insertPrint(PrintItem item)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, item.nTrxnNo);
		values.put(KEY_DOC_TYPE, item.strDocType);
		values.put(KEY_DOC_NO, item.strDocNo);
		values.put(KEY_CUST_NO, item.nCustNo);
		values.put(KEY_CHAIN_NO, item.nChainNo);
		values.put(KEY_FILE_PATH, item.strFilePath);
		values.put(KEY_NAME, item.strCustomerName);
		values.put(KEY_INVOICE_AMOUNT, item.nInvoiceAmount);

		mDb.insert(TABLE_PRINT_INFO, null, values);
	}
	
	public void deleteAllPrint()
	{
		mDb.delete(TABLE_PRINT_INFO, null, null);
	}
	
	public List<PrintItem> getPrintList()
	{
		String sql = "SELECT * FROM " + TABLE_PRINT_INFO;
		List<PrintItem> printItemList = new ArrayList<>();
		Cursor cursor = null;
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					PrintItem item = new PrintItem();
					item.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					item.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					item.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					item.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					item.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
					item.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
					item.strCustomerName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
					item.strFilePath = cursor.getString(cursor.getColumnIndex(KEY_FILE_PATH));
					item.nInvoiceAmount = cursor.getFloat(cursor.getColumnIndex(KEY_INVOICE_AMOUNT));
					printItemList.add(item);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return printItemList;
	}

	public void insertProdInfo(ProdInfo item)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_ITEM_NO, item.strItemNo);
		values.put(KEY_INFO_DETAIL, item.strInfoDetail);
		values.put(KEY_INFO_TYPE, item.strInfoType);

		mDb.insert(TABLE_PROD_INFO, null, values);
	}

	public void deleteAllProdInfos()
	{
		mDb.delete(TABLE_PROD_INFO, null, null);
	}

	public Map<String, ProdInfo> getProdInfoMap(String strInfoType)
	{
		String sql = "SELECT * FROM " + TABLE_PROD_INFO + " WHERE " + KEY_INFO_TYPE + " = '" + strInfoType + "'";
		Map<String, ProdInfo> prodInfoList = new HashMap<>();
		Cursor cursor = null;
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					ProdInfo item = new ProdInfo();
					item.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					item.strInfoDetail = cursor.getString(cursor.getColumnIndex(KEY_INFO_DETAIL));
					item.strInfoType = cursor.getString(cursor.getColumnIndex(KEY_INFO_TYPE));
					prodInfoList.put(item.strItemNo, item);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return prodInfoList;
	}

	public List<ProdInfo> getProdInfo(String strItemNo)
	{
		String sql = "SELECT * FROM " + TABLE_PROD_INFO + " WHERE " + KEY_ITEM_NO + " = '" + strItemNo + "'";
		List<ProdInfo> prodInfoList = new ArrayList<>();
		Cursor cursor = null;
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					ProdInfo item = new ProdInfo();

					item.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					item.strInfoDetail = cursor.getString(cursor.getColumnIndex(KEY_INFO_DETAIL));
					item.strInfoType = cursor.getString(cursor.getColumnIndex(KEY_INFO_TYPE));
					prodInfoList.add(item);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return prodInfoList;
	}

	//Table for LoadBlot
	public void insertLoadBlot(LoadBlotInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_DETAIL_FILE, info.strDetailFile);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_LOT_NUMBER, info.strLotNumber);
		values.put(KEY_LOT_QTY, info.nLotQty);
		values.put(KEY_LOT_WEIGHT, info.fLotWeight);
		values.put(KEY_BATCH_DATE, info.strBatchDate);

		mDb.insert(TABLE_LOAD_BLOT_INFO, null, values);
	}

	public void deleteAllLoadBlot()
	{
		mDb.delete(TABLE_LOAD_BLOT_INFO, null, null);
	}

	public List<LoadBlotInfo> getLoadBlot(String strDetailFile)
	{
		String sql = "SELECT * FROM " + TABLE_LOAD_BLOT_INFO + " WHERE " + KEY_DETAIL_FILE + " = '" + strDetailFile + "'";
		List<LoadBlotInfo> loadBlotInfoList = new ArrayList<>();
		Cursor cursor = null;
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					LoadBlotInfo info = new LoadBlotInfo();
					info.strDetailFile = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_FILE));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strLotNumber = cursor.getString(cursor.getColumnIndex(KEY_LOT_NUMBER));
					info.nLotQty = cursor.getInt(cursor.getColumnIndex(KEY_LOT_QTY));
					info.fLotWeight = cursor.getInt(cursor.getColumnIndex(KEY_LOT_WEIGHT));
					info.strBatchDate = cursor.getString(cursor.getColumnIndex(KEY_BATCH_DATE));

					loadBlotInfoList.add(info);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return loadBlotInfoList;
	}

	//ULOADBLOT
	public void insertULoadBlot(UInvenBlotInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_DOC_NO, info.strDocNo);
		values.put(KEY_DOC_TYPE, info.strDocType);
		values.put(KEY_VOID_FLAG, info.nVoidFlag);
		values.put(KEY_PRINTED_FLAG, info.nPrintedFlag);
		values.put(KEY_TRXN_DATE, info.strTrxnDate);
		values.put(KEY_TRXN_TIME, info.strTrxnTime);
		values.put(KEY_REFERENCE, info.strReference);
		values.put(KEY_TCOMSTATUS, info.nTComStatus);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_SALES_DATE, info.strSaleDate);
		values.put(KEY_TRIP, info.strTrip);

		mDb.insert(TABLE_ULOADH_BLOT_INFO, null, values);
	}

	public void insertULoadBlotList(List<UInvenBlotDInfo> infoList)
	{
		ContentValues values;
		for(UInvenBlotDInfo detailInfo : infoList)
		{
			values = new ContentValues();
			values.put(KEY_TRXN_NO, detailInfo.nTrxnNo);
			values.put(KEY_LOCN_NO, detailInfo.nLocnNo);
			values.put(KEY_TRIP, detailInfo.strTrip);
			values.put(KEY_ITEM_NO, detailInfo.strItemNo);
			values.put(KEY_LOT_NUMBER, detailInfo.strLotNumber);
			values.put(KEY_BATCH_DATE, detailInfo.strBatchDate);
			values.put(KEY_BEGIN_STOCK, detailInfo.nBeginStock);
			values.put(KEY_LOADED, detailInfo.nLoaded);
			values.put(KEY_LOAD_ADJ, detailInfo.nLoadAdj);
			values.put(KEY_TRANS_IN, detailInfo.nTransIn);
			values.put(KEY_TRANS_OUT, detailInfo.nTransOut);
			values.put(KEY_SPOILS, detailInfo.nSpoils);
			values.put(KEY_DUMPS, detailInfo.nDumps);
			values.put(KEY_SALES, detailInfo.nSales);
			values.put(KEY_FREE, detailInfo.nFree);
			values.put(KEY_BUYBACKS, detailInfo.nBuyBacks);
			values.put(KEY_UNLOAD, detailInfo.nUnLoad);
			values.put(KEY_DUMPS_UNLOAD, detailInfo.nDumpsUnLoad);
			values.put(KEY_END_ADJ, detailInfo.nEndAdj);
			values.put(KEY_STOCK, detailInfo.nStock);
			values.put(KEY_SAMPLE, detailInfo.nSamples);
			mDb.insert(TABLE_ULOADD_BLOT_INFO, null, values);
		}
	}

	public void deleteAllULoadBlot()
	{
		mDb.delete(TABLE_ULOADH_BLOT_INFO, null, null);
		mDb.delete(TABLE_ULOADD_BLOT_INFO, null, null);
	}

	public void deleteULoadBlot(MakeZipFileInfo info)
	{
		List<UInvenBlotInfo> uInvenBlotInfoList = new ArrayList<>();
		for(long nTrxnNo : info.trxnList)
		{
			deleteULoadBlot(nTrxnNo);
		}
	}

	public void deleteULoadBlot(long nTrxnNo)
	{
		mDb.delete(TABLE_ULOADH_BLOT_INFO, KEY_TRXN_NO + " = " + nTrxnNo, null);
		mDb.delete(TABLE_ULOADD_BLOT_INFO, KEY_TRXN_NO + " = " + nTrxnNo, null);
	}

	public List<UInvenBlotInfo> getULoadBlot(MakeZipFileInfo info)
	{
		List<UInvenBlotInfo> uInvenBlotInfoList = new ArrayList<>();
		for(long nTrxnNo : info.trxnList)
		{
			uInvenBlotInfoList.addAll(getULoadBlot(nTrxnNo));
		}
		return uInvenBlotInfoList;
	}

	public List<UInvenBlotInfo> getULoadBlot()
	{
		String sql = "SELECT * FROM " + TABLE_ULOADH_BLOT_INFO;
		List<UInvenBlotInfo> loadBlotInfoList = new ArrayList<>();
		Cursor cursor = null;
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					UInvenBlotInfo info = new UInvenBlotInfo();
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
					info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
					info.nVoidFlag = cursor.getInt(cursor.getColumnIndex(KEY_VOID_FLAG));
					info.nPrintedFlag = cursor.getInt(cursor.getColumnIndex(KEY_PRINTED_FLAG));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
					info.nTComStatus = cursor.getInt(cursor.getColumnIndex(KEY_TCOMSTATUS));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strSaleDate = cursor.getString(cursor.getColumnIndex(KEY_SALES_DATE));
					info.strTrip = cursor.getString(cursor.getColumnIndex(KEY_TRIP));

					//Get Details information.
					info.details = getULoadBlotDetail(info.nTrxnNo);
					loadBlotInfoList.add(info);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return loadBlotInfoList;
	}

	public List<UInvenBlotInfo> getULoadBlot(long nTrxnNo)
	{
		String sql = "SELECT * FROM " + TABLE_ULOADH_BLOT_INFO + " WHERE " + KEY_TRXN_NO + " = " + nTrxnNo;
		List<UInvenBlotInfo> loadBlotInfoList = new ArrayList<>();
		Cursor cursor = null;
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					UInvenBlotInfo info = new UInvenBlotInfo();
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strDocNo = cursor.getString(cursor.getColumnIndex(KEY_DOC_NO));
					info.strDocType = cursor.getString(cursor.getColumnIndex(KEY_DOC_TYPE));
					info.nVoidFlag = cursor.getInt(cursor.getColumnIndex(KEY_VOID_FLAG));
					info.nPrintedFlag = cursor.getInt(cursor.getColumnIndex(KEY_PRINTED_FLAG));
					info.strTrxnDate = cursor.getString(cursor.getColumnIndex(KEY_TRXN_DATE));
					info.strTrxnTime = cursor.getString(cursor.getColumnIndex(KEY_TRXN_TIME));
					info.strReference = cursor.getString(cursor.getColumnIndex(KEY_REFERENCE));
					info.nTComStatus = cursor.getInt(cursor.getColumnIndex(KEY_TCOMSTATUS));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strSaleDate = cursor.getString(cursor.getColumnIndex(KEY_SALES_DATE));
					info.strTrip = cursor.getString(cursor.getColumnIndex(KEY_TRIP));

					//Get Details information.
					info.details = getULoadBlotDetail(nTrxnNo);
					loadBlotInfoList.add(info);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return loadBlotInfoList;
	}

	public List<UInvenBlotDInfo> getULoadBlotDetail(long nTrxnNo)
	{
		String sql = "SELECT * FROM " + TABLE_ULOADD_BLOT_INFO + " WHERE " + KEY_TRXN_NO + " = " + nTrxnNo;
		List<UInvenBlotDInfo> loadBlotInfoList = new ArrayList<>();
		Cursor cursor = null;
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					UInvenBlotDInfo info = new UInvenBlotDInfo();
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strLotNumber = cursor.getString(cursor.getColumnIndex(KEY_LOT_NUMBER));
					info.strBatchDate = cursor.getString(cursor.getColumnIndex(KEY_BATCH_DATE));
					info.strTrip = cursor.getString(cursor.getColumnIndex(KEY_TRIP));
					info.nBeginStock = cursor.getInt(cursor.getColumnIndex(KEY_BEGIN_STOCK));
					info.nLoaded = cursor.getInt(cursor.getColumnIndex(KEY_LOADED));
					info.nLoadAdj = cursor.getInt(cursor.getColumnIndex(KEY_LOAD_ADJ));
					info.nTransIn = cursor.getInt(cursor.getColumnIndex(KEY_TRANS_IN));
					info.nTransOut = cursor.getInt(cursor.getColumnIndex(KEY_TRANS_OUT));
					info.nSpoils = cursor.getInt(cursor.getColumnIndex(KEY_SPOILS));
					info.nDumps = cursor.getInt(cursor.getColumnIndex(KEY_DUMPS));
					info.nSales = cursor.getInt(cursor.getColumnIndex(KEY_SALES));
					info.nFree = cursor.getInt(cursor.getColumnIndex(KEY_FREE));
					info.nBuyBacks = cursor.getInt(cursor.getColumnIndex(KEY_BUYBACKS));
					info.nUnLoad = cursor.getInt(cursor.getColumnIndex(KEY_UNLOAD));
					info.nDumpsUnLoad = cursor.getInt(cursor.getColumnIndex(KEY_DUMPS_UNLOAD));
					info.nEndAdj = cursor.getInt(cursor.getColumnIndex(KEY_END_ADJ));
					info.nStock = cursor.getInt(cursor.getColumnIndex(KEY_STOCK));
					info.nSamples = cursor.getInt(cursor.getColumnIndex(KEY_SAMPLE));

					loadBlotInfoList.add(info);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return loadBlotInfoList;
	}

	//BATCH LOT TABLE
	public void insertBatchLot(BatchLot info)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_TRXN_NO, info.nTrxnNo);
		values.put(KEY_ITEM_NO, info.strItemNo);
		values.put(KEY_LOT_NUMBER, info.strLotNumber);
		values.put(KEY_LOCN_NO, info.nLocnNo);
		values.put(KEY_SEQUENCE, info.nSequence);
		values.put(KEY_BATCH_DATE, info.strBatchDate);
		values.put(KEY_DETAIL_TYPE, info.strDetailType);
		values.put(KEY_ACTUAL_QTY, info.nActualQty);
		values.put(KEY_BEGIN_QTY, info.nBeginQty);
		values.put(KEY_SUGGEST_QTY, info.nSuggestQty);

		mDb.insert(TABLE_BATCH_LOT, null, values);
	}

	public void deleteAllBatchLot()
	{
		mDb.delete(TABLE_BATCH_LOT, null, null);
	}

	public Map<String, List<BatchLot>> getBatchLotList(long nTrxNo)
	{
		String sql = "SELECT * FROM " + TABLE_BATCH_LOT + " WHERE " + KEY_TRXN_NO + " = " + nTrxNo;

		Map<String, List<BatchLot>> batchLotMap = new HashMap<>();

		Cursor cursor = null;
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					BatchLot info = new BatchLot();
					info.nTrxnNo = cursor.getLong(cursor.getColumnIndex(KEY_TRXN_NO));
					info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
					info.strDetailType = cursor.getString(cursor.getColumnIndex(KEY_DETAIL_TYPE));
					info.strLotNumber = cursor.getString(cursor.getColumnIndex(KEY_LOT_NUMBER));
					info.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));
					info.nReasonCode = cursor.getInt(cursor.getColumnIndex(KEY_REASON_CODE));
					info.nSequence = cursor.getInt(cursor.getColumnIndex(KEY_SEQUENCE));
					info.strBatchDate = cursor.getString(cursor.getColumnIndex(KEY_BATCH_DATE));
					info.nActualQty = cursor.getInt(cursor.getColumnIndex(KEY_ACTUAL_QTY));
					info.nBeginQty = cursor.getInt(cursor.getColumnIndex(KEY_BEGIN_QTY));
					info.nSuggestQty = cursor.getInt(cursor.getColumnIndex(KEY_SUGGEST_QTY));

					List<BatchLot> batchLotList = batchLotMap.get(info.strItemNo);
					if(batchLotList == null)
					{
						batchLotList = new ArrayList<>();
						batchLotMap.put(info.strItemNo, batchLotList);
					}
					batchLotList.add(info);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return batchLotMap;
	}

	//UBilling Details
	public long insertUBillingDetailsInfo(UBillingDetailsInfo info) {
		if(info == null) return -1;
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_COMPANY_TAX_ID, info.strCompanyTaxID);
		values.put(KEY_BILLING_NAME, info.strBillingName);
		values.put(KEY_ADDRESS1, info.strAddress1);
		values.put(KEY_ADDRESS2, info.strAddress2);
		values.put(KEY_CITY, info.strCity);
		values.put(KEY_STATE, info.strState);
		values.put(KEY_POST_CODE, info.strPostCode);
		values.put(KEY_LEGAL_NAME, info.strLegalName);
		values.put(KEY_CREDIT_LIMIT, info.strCreditLimit);
		return mDb.insert(TABLE_UBILLING_DETAILS_INFO, null, values);
	}

	public long updateUBillingDetailsInfo(UBillingDetailsInfo info){
		if(info == null) return -1;
		ContentValues values = new ContentValues();
		values.put(KEY_COMPANY_TAX_ID, info.strCompanyTaxID);
		values.put(KEY_BILLING_NAME, info.strBillingName);
		values.put(KEY_ADDRESS1, info.strAddress1);
		values.put(KEY_ADDRESS2, info.strAddress2);
		values.put(KEY_CITY, info.strCity);
		values.put(KEY_STATE, info.strState);
		values.put(KEY_POST_CODE, info.strPostCode);
		values.put(KEY_LEGAL_NAME, info.strLegalName);
		values.put(KEY_CREDIT_LIMIT, info.strCreditLimit);
		return mDb.update(TABLE_UBILLING_DETAILS_INFO, values, KEY_CHAIN_NO + " = " + info.nChainNo + " AND " + KEY_CUST_NO + " = " + info.nCustNo, null);
	}

	public boolean deleteUBillingDetailsInfo() throws SQLException {
		return mDb.delete(TABLE_UBILLING_DETAILS_INFO, null, null) > 0;
	}

	public UBillingDetailsInfo getUBillingDetailsInfo(int nCustNo, int nChainNo)
	{
		String sql =	" SELECT	* " +
				" FROM		" + TABLE_UBILLING_DETAILS_INFO +
				" WHERE		" + KEY_CUST_NO + " = " + nCustNo +
				" AND "  + KEY_CHAIN_NO + " = " + nChainNo;

		UBillingDetailsInfo info = null;
		Cursor cursor = mDb.rawQuery(sql, null);
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();

				info = new UBillingDetailsInfo();
				info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
				info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
				info.strCompanyTaxID = cursor.getString(cursor.getColumnIndex(KEY_COMPANY_TAX_ID));
				info.strBillingName = cursor.getString(cursor.getColumnIndex(KEY_BILLING_NAME));
				info.strAddress1 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS1));
				info.strAddress2 = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS2));
				info.strCity = cursor.getString(cursor.getColumnIndex(KEY_CITY));
				info.strState = cursor.getString(cursor.getColumnIndex(KEY_STATE));
				info.strPostCode = cursor.getString(cursor.getColumnIndex(KEY_POST_CODE));
				info.strLegalName = cursor.getString(cursor.getColumnIndex(KEY_LEGAL_NAME));
				info.strCreditLimit = cursor.getString(cursor.getColumnIndex(KEY_CREDIT_LIMIT));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	// Contact Details Table
	public long insertContactDetails(ContactDetail info) {
		Gson gson = new Gson();
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_CONTACT_TYPE, info.strContactType);
		values.put(KEY_CONTACT_TYPE_ID, info.strContactTypeID);
		values.put(KEY_CONTACT_TYPE_NUMERIC_KEY, info.nContactTypeNumericKey);
		values.put(KEY_CONTACT_NAME, info.strContactName);
		values.put(KEY_CONTACT_PHONE1, info.strContactPhone1);
		values.put(KEY_CONTACT_PHONE2, info.strContactPhone2);
		values.put(KEY_CONTACT_PHONE3, info.strContactPhone3);
		values.put(KEY_CONTACT_EMAIL1, info.strContactEmail1);
		values.put(KEY_CONTACT_EMAIL2, info.strContactEmail2);
		values.put(KEY_CONTACT_EMAIL3, info.strContactEmail3);
		values.put(KEY_CONTACT_INFO, gson.toJson(info.contactInfoList));
		info.nIdx = mDb.insert(TABLE_CONTACT_DETAIL_INFO, null, values);
		return info.nIdx;
	}

	public void updateContactDetails(ContactDetail info){
		Gson gson = new Gson();
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);
		values.put(KEY_CONTACT_TYPE, info.strContactType);
		values.put(KEY_CONTACT_TYPE_ID, info.strContactTypeID);
		values.put(KEY_CONTACT_TYPE_NUMERIC_KEY, info.nContactTypeNumericKey);
		values.put(KEY_CONTACT_NAME, info.strContactName);
		values.put(KEY_CONTACT_PHONE1, info.strContactPhone1);
		values.put(KEY_CONTACT_PHONE2, info.strContactPhone2);
		values.put(KEY_CONTACT_PHONE3, info.strContactPhone3);
		values.put(KEY_CONTACT_EMAIL1, info.strContactEmail1);
		values.put(KEY_CONTACT_EMAIL2, info.strContactEmail2);
		values.put(KEY_CONTACT_EMAIL3, info.strContactEmail3);
		values.put(KEY_CONTACT_INFO, gson.toJson(info.contactInfoList));
		mDb.update(TABLE_CONTACT_DETAIL_INFO, values, KEY_IDX + " = " + info.nIdx, null);
		return;
	}

	public List<ContactDetail> getContactDetailList(int nCustNo, int nChainNo)
	{
		Gson gson = new Gson();
		String sql = "SELECT * FROM " + TABLE_CONTACT_DETAIL_INFO + " WHERE " + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_CHAIN_NO + " = " + nChainNo;
		List<ContactDetail> contactDetailList = new ArrayList<>();
		Cursor cursor = null;
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for(int i = 0; i < cursor.getCount(); i++)
				{
					ContactDetail info = new ContactDetail();
					info.nIdx = cursor.getLong(cursor.getColumnIndex(KEY_IDX));
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strContactType = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_TYPE));
					info.strContactTypeID = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_TYPE_ID));
					info.nContactTypeNumericKey = cursor.getInt(cursor.getColumnIndex(KEY_CONTACT_TYPE_NUMERIC_KEY));
					info.strContactName = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_NAME));
					info.strContactPhone1 = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_PHONE1));
					info.strContactPhone2 = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_PHONE2));
					info.strContactPhone3 = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_PHONE3));
					info.strContactEmail1 = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_EMAIL1));
					info.strContactEmail2 = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_EMAIL2));
					info.strContactEmail3 = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_EMAIL3));
					String strContactInfo = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_INFO));
					info.contactInfoList = gson.fromJson(strContactInfo, new TypeToken<List<ContactInfo>>(){}.getType());
					contactDetailList.add(info);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return contactDetailList;
	}

	public boolean deleteContactDetailsInfo() throws SQLException {
		return mDb.delete(TABLE_CONTACT_DETAIL_INFO, null, null) > 0;
	}

	public boolean deleteContactDetails(int nCustNo, int nChainNo) {
		return mDb.delete(TABLE_CONTACT_DETAIL_INFO, KEY_CHAIN_NO + " = " + nChainNo + " AND " + KEY_CUST_NO + " = " + nCustNo, null) > 0;
	}

	// Customer Addtional Information Table
	public void insertCustAdditionalInfo(List<ContactInfo> infoList, int nCustNo, int nChainNo) {
		Gson gson = new Gson();
		ContentValues values = new ContentValues();
		values.put(KEY_CHAIN_NO, nChainNo);
		values.put(KEY_CUST_NO, nCustNo);
		values.put(KEY_CUST_INFO, gson.toJson(infoList));
		mDb.insert(TABLE_CUSTOMER_ADDITIONAL_INFO, null, values);
		return;
	}

	public void updateCustAdditionalInfo(List<ContactInfo> infoList, int nCustNo, int nChainNo){
		Gson gson = new Gson();
		ContentValues values = new ContentValues();
		values.put(KEY_CUST_INFO, gson.toJson(infoList));
		mDb.update(TABLE_CUSTOMER_ADDITIONAL_INFO, values, KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_CHAIN_NO + " = " + nChainNo, null);
		return;
	}

	public List<ContactInfo> getCustAdditionalInfoList(int nCustNo, int nChainNo)
	{
		Gson gson = new Gson();
		String sql = "SELECT * FROM " + TABLE_CUSTOMER_ADDITIONAL_INFO + " WHERE " + KEY_CUST_NO + " = " + nCustNo + " AND " + KEY_CHAIN_NO + " = " + nChainNo;
		List<ContactInfo> infoList = new ArrayList<>();
		Cursor cursor = null;
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				infoList = gson.fromJson(cursor.getString(cursor.getColumnIndex(KEY_CUST_INFO)), new TypeToken<List<ContactInfo>>(){}.getType());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return infoList;
	}

	public boolean deleteCustAdditionalInfo() throws SQLException {
		return mDb.delete(TABLE_CUSTOMER_ADDITIONAL_INFO, null, null) > 0;
	}

	public List<ProdDetlInfo> getAuthDetailProduct(List<Integer> authGrp)
	{
		if( authGrp.size() == 0 ) return null;
		String strCondition = "";
		for(Integer nAuthGrp : authGrp)
		{
			if (!TextUtils.isEmpty(strCondition)) {
				strCondition += "OR B." + KEY_AUTH_GRP + " = " + nAuthGrp;
			} else {
				strCondition = "B." + KEY_AUTH_GRP + " = " + nAuthGrp;
			}
		}
		String sql = " SELECT	A.*, B." + KEY_START_DATE + ", B." + KEY_END_DATE +
				", C." + KEY_LOCN_NO + ", C." + KEY_BASE_PRICE + ", C." + KEY_DRIVER_PRICE + ", C." + KEY_CASE_FACTOR + ", C." + KEY_RETAIL_PRICE + ", C." + KEY_FULL_CASE + ", C." + KEY_SCW_TYPE +
				" FROM " + TABLE_PRODDETL_INFO + " A, " + TABLE_AUTHDETL_INFO + " B, " + TABLE_PRODLOCN_INFO + " C WHERE A." + KEY_ITEM_NO + " = B." + KEY_ITEM_NO + " AND (" + strCondition + ") AND A." + KEY_ITEM_NO + " = C." + KEY_ITEM_NO;

		Cursor cursor = null;
		List<ProdDetlInfo> uInfoList = new ArrayList<>();
		Date curDate = new Date(System.currentTimeMillis());
		try{
			cursor = mDb.rawQuery(sql, null);
			if(cursor != null && cursor.getCount() > 0){
				cursor.moveToFirst();
				for(int i = 0; i <cursor.getCount(); i++){
					String startDate = cursor.getString(cursor.getColumnIndex(KEY_START_DATE));
					String endDate = cursor.getString(cursor.getColumnIndex(KEY_END_DATE));
					if( isValidate(startDate, endDate, curDate) )
					{
						ProdDetlInfo info = new ProdDetlInfo();
						info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
						info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
						info.strDescription = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
						info.strShortDesc = cursor.getString(cursor.getColumnIndex(KEY_SHORT_DESC));
						info.strItemUPC = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC));
						info.strItemUPC20 = cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPC20));
						info.nWeight = cursor.getFloat(cursor.getColumnIndex(KEY_WEIGHT));
						info.strItemType = cursor.getString(cursor.getColumnIndex(KEY_ITEM_TYPE));
						info.strProdGrp = cursor.getString(cursor.getColumnIndex(KEY_PROD_GRP));
						info.strPromoGrp = cursor.getString(cursor.getColumnIndex(KEY_PROMO_GRP));
						info.strMarketGrp = cursor.getString(cursor.getColumnIndex(KEY_MARKET_GRP));
						info.strProdLine = cursor.getString(cursor.getColumnIndex(KEY_PROD_LINE));
						info.strPackSize = cursor.getString(cursor.getColumnIndex(KEY_PACK_SIZE));
						info.nShelfLife = cursor.getInt(cursor.getColumnIndex(KEY_SHELF_LIFE));
						info.strBrand = cursor.getString(cursor.getColumnIndex(KEY_BRAND));
						info.strSubBrand = cursor.getString(cursor.getColumnIndex(KEY_SUB_BRAND));
						info.strType = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
						info.strActiveDate = cursor.getString(cursor.getColumnIndex(KEY_ACTIVE_DATE));
						info.strDumpsAllowed = cursor.getString(cursor.getColumnIndex(KEY_DUMPS_ALLOWED));
						info.strSalesAllowed = cursor.getString(cursor.getColumnIndex(KEY_SALES_ALLOWED));
						info.strItemInactiveSwitch = cursor.getString(cursor.getColumnIndex(KEY_ITEM_INACTIVE_SWITCH));
						info.strGroupFilterKey = cursor.getString(cursor.getColumnIndex(KEY_GROUP_FILTER_KEY));
						info.strServiceFee = cursor.getString(cursor.getColumnIndex(KEY_SERVICE_FEE));
						info.nMaxQty = cursor.getFloat(cursor.getColumnIndex(KEY_MAX_QTY));
						info.nConsumerUnit = cursor.getInt(cursor.getColumnIndex(KEY_CONSUMER_UNIT));
						info.strImageUrl = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URL));
						info.strLotControl = cursor.getString(cursor.getColumnIndex(KEY_LOT_CONTROL));
						info.prodLocnInfo.nLocnNo = cursor.getInt(cursor.getColumnIndex(KEY_LOCN_NO));
						info.prodLocnInfo.nBasePrice = cursor.getLong(cursor.getColumnIndex(KEY_BASE_PRICE));
						info.prodLocnInfo.nDriverPrice = cursor.getLong(cursor.getColumnIndex(KEY_DRIVER_PRICE));
						info.prodLocnInfo.nCaseFactor = cursor.getInt(cursor.getColumnIndex(KEY_CASE_FACTOR));
						info.prodLocnInfo.nRetailPrice = cursor.getInt(cursor.getColumnIndex(KEY_RETAIL_PRICE));
						info.prodLocnInfo.nSCWType = cursor.getInt(cursor.getColumnIndex(KEY_SCW_TYPE));
						info.prodLocnInfo.strFullCase = cursor.getString(cursor.getColumnIndex(KEY_FULL_CASE));
						uInfoList.add(info);
					}

					cursor.moveToNext();
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally {
			if (cursor != null){
				cursor.close();
			}
		}

		return uInfoList;
	}

	//Survey Head
	public long insertSurveyHeadInfo(SurvHeadInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_SURVEY_HEADER_ID, info.strSurveyHeaderId);
		values.put(KEY_FROM_SURVEY_ID, info.bFromSurveyID ? 1 : 0);
		values.put(KEY_SURVEY_TITLE, info.strSurveyTtl);
		values.put(KEY_CREATE_DATE, info.strCreateDate);
		values.put(KEY_COMPLETION_DATE, info.strCompletionDate);
		values.put(KEY_SURVEY_RESPONSE, info.strSurveyResponse);
		values.put(KEY_MANDATORY, info.strMandatory);
		values.put(KEY_SURVEY_TYPE, info.nSurveyType);

		return mDb.insert(TABLE_SURVEY_HEAD_INFO, null, values);
	}

	public boolean deleteAllSurvHeadInfo() throws SQLException {
		return mDb.delete(TABLE_SURVEY_HEAD_INFO, null, null) > 0;
	}

	public long insertSurveyDistInfo(SurvDistInfo info) {
		ContentValues values = new ContentValues();
		values.put(KEY_SURVEY_HEADER_ID, info.strSurveyHeaderId);
		values.put(KEY_CHAIN_NO, info.nChainNo);
		values.put(KEY_CUST_NO, info.nCustNo);

		return mDb.insert(TABLE_SURVEY_DIST_INFO, null, values);
	}

	public boolean deleteAllSurvDistInfo() throws SQLException {
		return mDb.delete(TABLE_SURVEY_DIST_INFO, null, null) > 0;
	}

	public List<SurveyInfo> getSurveyListWithDistAndHead(int nSurveyType, int nCustNo, int nChainNo) {
		String sql =	" SELECT	A.*, B." + KEY_COMPLETION_DATE + ", B." + KEY_CREATE_DATE + ", B." + KEY_MANDATORY  + ", B." + KEY_SURVEY_TITLE + ", B." + KEY_SURVEY_TYPE + ", B." + KEY_SURVEY_RESPONSE + ", B." + KEY_FROM_SURVEY_ID +
				" FROM		" + TABLE_SURVEY_DIST_INFO + " A, " + TABLE_SURVEY_HEAD_INFO + " B" +
				" WHERE		A." + KEY_SURVEY_HEADER_ID + " = B." + KEY_SURVEY_HEADER_ID + " AND  B." +	KEY_SURVEY_TYPE + " = " + nSurveyType + " AND " +
				"A." + KEY_CUST_NO + " = " + nCustNo+ " AND " + "A." + KEY_CHAIN_NO + " = " + nChainNo;
		Cursor cursor = mDb.rawQuery(sql, null);
		List<SurveyInfo> list = new ArrayList<>();
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i=0; i<cursor.getCount(); i++) {
					SurveyInfo info = new SurveyInfo();
					info.nIdx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
					info.strSurveyID = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_HEADER_ID));
					info.bFromSurveyID = cursor.getInt(cursor.getColumnIndex(KEY_FROM_SURVEY_ID)) == 1;
					info.nChainNo = cursor.getInt(cursor.getColumnIndex(KEY_CHAIN_NO));
					info.nCustNo = cursor.getInt(cursor.getColumnIndex(KEY_CUST_NO));
					info.strSurveyTitle = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_TITLE));
					info.strCreateDate = cursor.getString(cursor.getColumnIndex(KEY_CREATE_DATE));
					info.strCompletionDate = cursor.getString(cursor.getColumnIndex(KEY_COMPLETION_DATE));
					info.strSurveyResponse = cursor.getString(cursor.getColumnIndex(KEY_SURVEY_RESPONSE));
					info.strMandatory = cursor.getString(cursor.getColumnIndex(KEY_MANDATORY));
					info.nSurveyType = cursor.getInt(cursor.getColumnIndex(KEY_SURVEY_TYPE));
					cursor.moveToNext();
					list.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}
}
