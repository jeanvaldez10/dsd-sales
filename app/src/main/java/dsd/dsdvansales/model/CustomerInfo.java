package dsd.dsdvansales.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.*;

import dsd.dsdvansales.global.Global;

@SuppressWarnings("serial")
public class CustomerInfo implements Serializable {
	public int nIdx = 0;
	public int nCustNo = 0;
	public String strAltCustNo = "";
	public String strName = "";
	public String strCompanyTaxID = "";
	public String strCity = "";
	public String strAddress1 = "";
	public String strAddress2 = "";
	public String strZip = "";
	public String strPhone = "";
	public String strEmail = "";
	public int nLocation = 0;
	public int nRouteNo = 0;
	public String strPriority = "";
	public int nMessageCnt = 0;
	public int nDayNo = 0;
	public int nSeqNo = 0;
	public int nChainNo = 0;
	public boolean bIsRouteSch = false;
	public boolean bFromAddCustomer = false;
	public String strBuyerName = "";
	public String strBuyerPhone = "";
	public String strAccountName = "";
	public String strAccountPhone = "";
	public double latitude = 0;
	public double longitude = 0;
	public int nPromoPlan = 0;
	public int nTemplateGrp = 0;
	public int nTempGrp = 0;
	public int nFeatGrp = 0;
	public int nPriceGrp = 0;
	public int nAltPriceGrp = 0;
	public String strTaxCode = "";
	public String strStartTime1 = "";
	public String strEndTime1 = "";
	public String strStartTime2 = "";
	public String strEndTime2 = "";
	public String strDeliveryDate = "";
	public int nPeriodNo = 0;
	public int nOrderNo = -1;
	public String strShipToState = "";
	public String strShipToZip = "";
	public int nInvoiceFmt = 0;
	public int nCustPriceMaintFlag = 0;
	public int nDelayedPrint = 0;
	public String strPayType = "";
	public String strBillName = "";
	public String strBillAddr1 = "";
	public String strBillAddr2 = "";
	public String strBillCity = "";
	public String strBillPeriod = "";
	public String strVendorNo = "";
	public int nBillCustNo = 0;
	public int nBillChainNo = 0;
	public String strState = "";
	public String strSignature = "";
	public String strTerms = "";
	public String strSalEntryMode = "";
	public String strRtnEntryMode = "";
	public String strDumpsAllowed = "";
	public String strDexVersion = "";
	public int nShowPrice = 0;
	public int nImageCapture = 0;
	public int nOrgSeqNo = 0;
	public String strAuthGrp = "";
	public double driverLatitude = 0;
	public double driverLongitude = 0;
	public List<PresoldOrHInfo> presoldOrHInfo = new ArrayList<>();
	public List<CustomerOrderInfo> latestOrderInfo = new ArrayList<>();
	public boolean bCompleted = false;
	public int	nCompleteStatus = 0;
	public int 	nDeferredStatus = 3;
	public String strCustMsg = "";

	public String strArrivalTime = "";

	public String strDeliveryWindowFrom = "";
	public String strDeliveryWindowTo = "";
	public int nCreditHold = 0;

	public int nDeliveryFreq = 0;
	public String strStoreNo = "";
	public String strDeliveryType = "";

	public String strSalesDistrict = "";

	public float fMiniumOrderValue = 0;
	public float fHandlingFee = 0;

	public int nConSales = 0;

	public String strType = "";
	public String strUser1 = "";

	public String strInvoiceDueDate = "";
	public String strPONumberRequired = "";
	public String strInvoiceDueMethod = "";
	public String strPONumberMask = "";

	public String strLegalName = "";
	public String strCreditLimit = "";

	public final static Comparator<CustomerInfo> orderComparator = new Comparator<CustomerInfo>() {
		// Ascending
		@Override
		public int compare(CustomerInfo object1, CustomerInfo object2) {
			return object1.nOrderNo < object2.nOrderNo ? -1 : object1.nOrderNo > object2.nOrderNo ? 1:0;
		}
	};

	public final static Comparator<CustomerInfo> seqNoComparator = new Comparator<CustomerInfo>() {
		// Ascending
		@Override
		public int compare(CustomerInfo object1, CustomerInfo object2) {
			return object1.nSeqNo < object2.nSeqNo ? -1 : object1.nSeqNo > object2.nSeqNo ? 1:0;
		}
	};

	public final static Comparator<CustomerInfo> periodNoComparator = new Comparator<CustomerInfo>() {
		// Ascending
		@Override
		public int compare(CustomerInfo object1, CustomerInfo object2) {
			return object1.nPeriodNo < object2.nPeriodNo ? -1 : object1.nPeriodNo > object2.nPeriodNo ? 1:0;
		}
	};

	public final static Comparator<CustomerInfo> deliveryDateComparator = new Comparator<CustomerInfo>() {
		// Ascending
		@Override
		public int compare(CustomerInfo object1, CustomerInfo object2) {
			return object1.strDeliveryDate.compareTo(object2.strDeliveryDate);
		}
	};
	
	public CustomerInfo duplicate()
	{
		CustomerInfo info = new CustomerInfo();
		info.nIdx = nIdx;
		info.nCustNo = nCustNo;
		info.strAltCustNo = strAltCustNo;
		info.strName = strName;
		info.strCompanyTaxID = strCompanyTaxID;
		info.strCity = strCity;
		info.strAddress1 = strAddress1;
		info.strAddress2 = strAddress2;
		info.strZip = strZip;
		info.strPhone = strPhone;
		info.strEmail = strEmail;
		info.nLocation = nLocation;
		info.nRouteNo = nRouteNo;
		info.strPriority = strPriority;
		info.nMessageCnt = nMessageCnt;
		info.nDayNo = nDayNo;
		info.nSeqNo = nSeqNo;
		info.nChainNo = nChainNo;
		info.bIsRouteSch = bIsRouteSch;
		info.bFromAddCustomer = bFromAddCustomer;
		info.strBuyerName = strBuyerName;
		info.strBuyerPhone = strBuyerPhone;
		info.strAccountName = strAccountName;
		info.strAccountPhone = strAccountPhone;
		info.latitude = latitude;
		info.longitude = longitude;
		info.nPromoPlan = nPromoPlan;
		info.nTemplateGrp = nTemplateGrp;
		info.nFeatGrp = nFeatGrp;
		info.nTempGrp = nTempGrp;
		info.nPriceGrp = nPriceGrp;
		info.nAltPriceGrp = nAltPriceGrp;
		info.strTaxCode = strTaxCode;
		info.strStartTime1 = strStartTime1;
		info.strEndTime1 = strEndTime1;
		info.strStartTime2 = strStartTime2;
		info.strEndTime2 = strEndTime2;
		info.strDeliveryDate = strDeliveryDate;
		info.nPeriodNo = nPeriodNo;
		info.nOrderNo = nOrderNo;
		info.strShipToState = strShipToState;
		info.strShipToZip = strShipToZip;
		info.nInvoiceFmt = nInvoiceFmt;
		info.nCustPriceMaintFlag = nCustPriceMaintFlag;
		info.nShowPrice = nShowPrice;
		info.nDelayedPrint = nDelayedPrint;
		info.strPayType = strPayType;
		info.strBillName = strBillName;
		info.strBillAddr1 = strBillAddr1;
		info.strBillAddr2 = strBillAddr2;
		info.strBillCity = strBillCity;
		info.strBillPeriod = strBillPeriod;
		info.strVendorNo = strVendorNo;
		info.nBillChainNo = nBillChainNo;
		info.nBillCustNo = nBillCustNo;
		info.strState = strState;
		info.strSignature = strSignature;
		info.strTerms = strTerms;
		info.strSalEntryMode = strSalEntryMode;
		info.strRtnEntryMode = strRtnEntryMode;
		info.strDumpsAllowed = strDumpsAllowed;
		info.strDexVersion = strDexVersion;
		info.nCompleteStatus = nCompleteStatus;
		info.nDeferredStatus = nDeferredStatus;
		info.nOrgSeqNo = nOrgSeqNo;
		info.strAuthGrp = strAuthGrp;
		info.driverLatitude = driverLatitude;
		info.driverLongitude = driverLongitude;

		info.strDeliveryWindowFrom = strDeliveryWindowFrom;
		info.strDeliveryWindowTo = strDeliveryWindowTo;
		info.nCreditHold = nCreditHold;
		info.nDeliveryFreq = nDeliveryFreq;
		info.strStoreNo = strStoreNo;
		info.strDeliveryType = strDeliveryType;
		info.strSalesDistrict = strSalesDistrict;
		info.fHandlingFee = fHandlingFee;
		info.fMiniumOrderValue = fMiniumOrderValue;

		info.nConSales = nConSales;

		info.strType = strType;
		info.strUser1 = strUser1;

		if( presoldOrHInfo != null && presoldOrHInfo.size() > 0 )
		{
			for(int i = 0; i < presoldOrHInfo.size(); i++)
			{
				info.presoldOrHInfo.add(presoldOrHInfo.get(i).duplicate());
			}
		}
		else
			info.presoldOrHInfo = new ArrayList<>();
		info.bCompleted = bCompleted;
		info.strInvoiceDueDate = strInvoiceDueDate;
		info.strInvoiceDueMethod = strInvoiceDueMethod;
		info.strPONumberRequired = strPONumberRequired;
		info.strPONumberMask = strPONumberMask;

		info.strCreditLimit = strCreditLimit;
		info.strLegalName = strLegalName;
		return info;
	}
	
	public boolean compare(CustomerInfo info)
	{
		String strText = "";
		if( info.strBillName.length() != 0 )
		{ 
			strText = info.strBillName;
		}
		else
		{
			strText = info.strName;
		}
		if( !strName.equals(strText) ) return false;
			
		if( !strCompanyTaxID.equals(info.strCompanyTaxID) ) return false; 
		
		if( info.strBillAddr1.length() != 0 )
		{ 
			strText = info.strBillAddr1;
		}
		else
		{
			strText = info.strAddress1;
		}
		if( !strAddress1.equals(strText) ) return false;
		
		if( info.strBillAddr2.length() != 0 )
		{ 
			strText = info.strBillAddr2;
		}
		else
		{
			strText = info.strAddress2;
		}
		if( !strAddress2.equals(strText) ) return false;
		
		if( info.strBillCity.length() != 0 )
		{
			strText = info.strBillCity + " " + info.strState + " " + info.strZip;
		}
		else
		{
			strText = info.strCity + " " + info.strShipToState + " " + info.strShipToZip;
		}
		
		String strOrgText = strCity + " " + strShipToState + " " + strShipToZip;
		if( !strOrgText.equals(strText) ) return false;
				
		return true;
	}

	public final static Comparator<CustomerInfo> presold_seqNoComparator = new Comparator<CustomerInfo>() {
		// Ascending
		@Override
		public int compare(CustomerInfo object1, CustomerInfo object2) {
			//return object1.nSeqNo < object2.nSeqNo ? -1 : object1.nSeqNo > object2.nSeqNo ? 1:0;
			PresoldOrHInfo presoldOrHInfo1 = null;
			PresoldOrHInfo presoldOrHInfo2 = null;
			if(object1.presoldOrHInfo != null && object1.presoldOrHInfo.size() > 0)
			{
				try
				{
					presoldOrHInfo1 = object1.presoldOrHInfo.get(0);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}

			if(object2.presoldOrHInfo != null && object2.presoldOrHInfo.size() > 0)
			{
				try
				{
					presoldOrHInfo2 = object2.presoldOrHInfo.get(0);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}

			if(presoldOrHInfo1 == null && presoldOrHInfo2 == null ) return 0;
			else if( presoldOrHInfo1 == null && presoldOrHInfo2 != null ) return 1;
			else if( presoldOrHInfo1 != null && presoldOrHInfo2 == null ) return -1;
			else return presoldOrHInfo1.nDeliverySequence - presoldOrHInfo2.nDeliverySequence;
		}
	};

	public final static Comparator<CustomerInfo> presold_periodNoComparator = new Comparator<CustomerInfo>() {
		// Ascending
		@Override
		public int compare(CustomerInfo object1, CustomerInfo object2) {
			PresoldOrHInfo presoldOrHInfo1 = null;
			PresoldOrHInfo presoldOrHInfo2 = null;
			if(object1.presoldOrHInfo != null && object1.presoldOrHInfo.size() > 0)
			{
				try
				{
					presoldOrHInfo1 = object1.presoldOrHInfo.get(0);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}

			if(object2.presoldOrHInfo != null && object2.presoldOrHInfo.size() > 0)
			{
				try
				{
					presoldOrHInfo2 = object2.presoldOrHInfo.get(0);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}

			if(presoldOrHInfo1 == null && presoldOrHInfo2 == null ) return 0;
			else if( presoldOrHInfo1 == null && presoldOrHInfo2 != null ) return 1;
			else if( presoldOrHInfo1 != null && presoldOrHInfo2 == null ) return -1;
			else return presoldOrHInfo1.nPeriodNo - presoldOrHInfo2.nPeriodNo;
			//return object1.nPeriodNo < object2.nPeriodNo ? -1 : object1.nPeriodNo > object2.nPeriodNo ? 1:0;
		}
	};

	public final static Comparator<CustomerInfo> presold_deliveryDateComparator = new Comparator<CustomerInfo>() {
		// Ascending
		@Override
		public int compare(CustomerInfo object1, CustomerInfo object2) {
			PresoldOrHInfo presoldOrHInfo1 = null;
			PresoldOrHInfo presoldOrHInfo2 = null;
			if( object1.presoldOrHInfo != null && object1.presoldOrHInfo.size() > 0)
			{
				try
				{
					presoldOrHInfo1 = object1.presoldOrHInfo.get(0);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}

			if(object2.presoldOrHInfo != null && object2.presoldOrHInfo.size() > 0)
			{
				try
				{
					presoldOrHInfo2 = object2.presoldOrHInfo.get(0);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}

			if(presoldOrHInfo1 == null && presoldOrHInfo2 == null ) return 0;
			else if( presoldOrHInfo1 == null && presoldOrHInfo2 != null ) return 1;
			else if( presoldOrHInfo1 != null && presoldOrHInfo2 == null ) return -1;
			else return presoldOrHInfo1.strDeliveryDate.compareTo(presoldOrHInfo2.strDeliveryDate);
		}
	};

	@NonNull
	@Override
	public String toString() {
		return strName;
	}
}