package dsd.dsdvansales.model;

import java.util.*;

public class UOrderHInfo {
	public int nIdx = 0;
	public long nTrxnNo = 0;
	public String strDocNo = "0";
	public int nChainNo = 0;
	public int nCustNo = 0;
	public String strOrderNumber = "";
	public int nDayNo = 0;
	public String strDSD = "";
	public String strDocType = "";
	public String strTrxnDate = "";
	public String strTrxnTime = "";
	public int nTotalAmount = 0;
	public int nTotalDumps = 0;
	public int nTotalBuybacks = 0;
	public int nTotalFree = 0;
	public String strReference = "";
	public String strSpecialInstructions = "";
	public String strDistributor = "";
	public String strPOReference = "";
	public String strDeliveryDate = "";
	public int futureOrderMethod = 0;
	public String strOrderName = "";
	public String strUser2 = "";
	public String strUser1 = "";
	public String strPresoldChgd = "N";
	public int nTotalSamples = 0;
	public float nSaleAmount = 0;
	public float nTaxAmount = 0;
	public float nPromotionAmount = 0;

	public float nDiscSales = 0;
	public float nDiscBuybacks = 0;
	public float nDiscDumps = 0;
	public float nNetBuybacks = 0;
	public float nNetDumps = 0;
	public float nNetSales = 0;

	public String strSignatureFilePath = "";
	public int nPrintedFlag = 0;
	public int nVoidFlag = 0;

	public String strQRCodeFilePath = "";

	public long nVoidTrxnNo = 0;
	public long nOrgTrxnNo = 0;

	public boolean bCentsRound = false;
	public boolean bPrinted = false;
	public boolean bUploaded = false;
	public int nPeriodNo = 0;
	public int nOrderType = 0;
	public double dTotalAmount = 0;
	public double dTotalDumps = 0;
	public double dTotalBuybacks = 0;
	public double dTotalFree = 0;
	public String strSourceType = "I";
	public String strNettSplit = "N";

	public float nHandlingFeeTaxAmount = 0;
	public float nHandlingFee = 0;
	public String strHandlingTaxCode = "";

	public String strDriverNumber = "";

	public List<UOrderDInfo> uOrderDList = new ArrayList<>();
	public List<UOrderDInfo> deliveryList = new ArrayList<>();
	public List<UOrderDInfo> handCountList = new ArrayList<>();
	public List<UOrderDInfo> pickupList = new ArrayList<>();
	public List<UOrderDInfo> containerList = new ArrayList<>();
	public float nPrintCDSAmount = 0;
	public Map<String, PrintTaxRateInfo> cdsMap = new HashMap<>();

	public void copyFrom(UOrderHInfo uOrderHInfo)
	{
		nTrxnNo = uOrderHInfo.nTrxnNo;
		strDocNo = uOrderHInfo.strDocNo;
		nChainNo = uOrderHInfo.nChainNo;
		nCustNo = uOrderHInfo.nCustNo;
		strOrderNumber = uOrderHInfo.strOrderNumber;
		nDayNo = uOrderHInfo.nDayNo;
		strTrxnDate = uOrderHInfo.strTrxnDate;
		strTrxnTime = uOrderHInfo.strTrxnTime;
		nTotalAmount = uOrderHInfo.nTotalAmount;
		nTotalDumps = uOrderHInfo.nTotalDumps;
		nTotalBuybacks = uOrderHInfo.nTotalBuybacks;
		nTotalFree = uOrderHInfo.nTotalFree;
		strReference = uOrderHInfo.strReference;
		strSpecialInstructions = uOrderHInfo.strSpecialInstructions;
		strDistributor = uOrderHInfo.strDistributor;
		strPOReference = uOrderHInfo.strPOReference;
		strDeliveryDate = uOrderHInfo.strDeliveryDate;
		strOrderName = uOrderHInfo.strOrderName;
		strUser2 = uOrderHInfo.strUser2;
		strUser1 = uOrderHInfo.strUser1;
		strPresoldChgd = uOrderHInfo.strPresoldChgd;
		nTotalSamples = uOrderHInfo.nTotalSamples;
		nDiscSales = uOrderHInfo.nDiscSales;
		nDiscDumps = uOrderHInfo.nDiscDumps;
		nDiscBuybacks = uOrderHInfo.nDiscBuybacks;
		nNetDumps = uOrderHInfo.nNetDumps;
		nNetSales = uOrderHInfo.nNetSales;
		nNetBuybacks = uOrderHInfo.nNetBuybacks;
		nSaleAmount = uOrderHInfo.nSaleAmount;
		nTaxAmount = uOrderHInfo.nTaxAmount;
		nPromotionAmount = uOrderHInfo.nPromotionAmount;
		strSignatureFilePath = uOrderHInfo.strSignatureFilePath;
		nPrintedFlag = uOrderHInfo.nPrintedFlag;
		nVoidFlag = uOrderHInfo.nVoidFlag;
		bCentsRound = uOrderHInfo.bCentsRound;
		bPrinted = uOrderHInfo.bPrinted;
		bUploaded = uOrderHInfo.bUploaded;
		nPeriodNo = uOrderHInfo.nPeriodNo;
		nOrderType = uOrderHInfo.nOrderType;
		dTotalAmount = uOrderHInfo.dTotalAmount;
		dTotalDumps = uOrderHInfo.dTotalDumps;
		dTotalBuybacks = uOrderHInfo.dTotalBuybacks;
		dTotalFree = uOrderHInfo.dTotalFree;
		strSourceType = uOrderHInfo.strSourceType;
		strNettSplit = uOrderHInfo.strNettSplit;
		strDriverNumber = uOrderHInfo.strDriverNumber;

		nHandlingFeeTaxAmount = uOrderHInfo.nHandlingFeeTaxAmount;
		nHandlingFee = uOrderHInfo.nHandlingFee;
		strHandlingTaxCode = uOrderHInfo.strHandlingTaxCode;
	}
}
