package dsd.dsdvansales.model;

import java.util.ArrayList;
import java.util.List;

public class UOrderDInfo {
	public int nIdx = 0;
	public long nTrxnNo = 0;
	public int nTrxnType = 0;
	public int nCustNo = 0;
	public int nLocnNo = 0;
	public String strItemNo = "";
	public String strItemUPC = "";
	public int nQuantity = 0;
	public int nCaseFactor = 1;
	public int nRetailPrice = 0;
	public int nReasonCode = 0;
	public float nBasePrice = 0;
	public float nDiscountVal = 0;
	public float nTaxAmount = 0;
	public float nPrintTaxAmount = 0;
	public float nPrintLineTaxAmount = 0;
	public float nPromoDiscount = 0;
	public float nPrice = 0;
	public float nOrgPrice = 0;
	public float nGrossPrice = 0;
	public float nOrgGrossPrice = 0;
	public int nPromoType = 0;
	public String strDescription = "";
	public String strShortDesc = "";
	public int nOrderQty = 0;
	public int nSuggestedQty = -1;
	public String strPickLocationCode = "";
	public int nPlanQty = 0;
	public String strFullCase = "";
	public int nSCWType = 0;
	public int nSales = 0;
	public int nDumps = 0;
	public int nPriceOverride = 0;
	public float nCustomerRetailPrice = 0;
	public String strItemTaxCode = "";
	public boolean isFromPresoldOrder = false;
	public int	nPresoldValue = 0;
	public String strProdGroup = "";

	//This variables are for printing.
	public int nDeliveryCount = 0;
	public int nCreditCount = 0;
	public double dDeliveryCount = 0;
	public double dCreditCount = 0;


	//Double Format
	public double dQuantity = 0;
	public double dSales = 0;
	public double dDumps = 0;
	public double dPlanQty = 0;
	public double dPresoldValue = 0;

	public List<UPromotionInfo> uPromotionInfoList = new ArrayList<>();

	public float nPrintCDSAmount = 0;
	public int nCDSRate = 0;
	public List<UTaxInfo> uTaxInfoList = new ArrayList<>();

	public List<LoadBlotInfo> uLoadBlotInfo = new ArrayList<>();

	public ProdLocnInfo prodLocnInfo = null; // Runtimeable variable.
	public UOrderDInfo duplicate()
	{
		UOrderDInfo info = new UOrderDInfo();
		info.nIdx = nIdx;
		info.nTrxnNo = nTrxnNo;
		info.nTrxnType = nTrxnType;
		info.nCustNo = nCustNo;
		info.nLocnNo = nLocnNo;
		info.strItemNo = strItemNo;
		info.strItemUPC = strItemUPC;
		info.nQuantity = nQuantity;
		info.strProdGroup = strProdGroup;

		info.nCaseFactor = nCaseFactor;
		info.nRetailPrice = nRetailPrice;
		info.nReasonCode = nReasonCode;
		info.nBasePrice = nBasePrice;
		info.nDiscountVal = nDiscountVal;
		info.nTaxAmount = nTaxAmount;
		info.nPromoDiscount = nPromoDiscount;
		info.nPromoType = nPromoType;

		info.strDescription = strDescription;
		info.strShortDesc = strShortDesc;
		info.nPrice = nPrice;
		info.nGrossPrice = nGrossPrice;
		info.nOrgGrossPrice = nOrgGrossPrice;
		info.nOrgPrice = nOrgPrice;
		info.nOrderQty = nOrderQty;
		info.nSuggestedQty = nSuggestedQty;

		info.strPickLocationCode = strPickLocationCode;
		info.nPlanQty = nPlanQty;

		info.strFullCase = strFullCase;
		info.nSCWType = nSCWType;

		info.nSales = nSales;
		info.nDumps = nDumps;
		info.strItemTaxCode = strItemTaxCode;

		info.isFromPresoldOrder = isFromPresoldOrder;
		info.nPresoldValue = nPresoldValue;

		info.dQuantity = dQuantity;
		info.dSales = dSales;
		info.dDumps = dDumps;
		info.dPlanQty = dPlanQty;
		info.dPresoldValue = dPresoldValue;

		if(uTaxInfoList != null)
		{
			for(UTaxInfo taxInfo : uTaxInfoList)
			{
				info.uTaxInfoList.add(taxInfo.duplicate());
			}
		}

		if(uPromotionInfoList != null)
		{
			for(UPromotionInfo promotionInfo : uPromotionInfoList)
			{
				info.uPromotionInfoList.add(promotionInfo.duplicate());
			}
		}

		if(uLoadBlotInfo != null)
		{
			for(LoadBlotInfo loadBlotInfo : uLoadBlotInfo)
			{
				info.uLoadBlotInfo.add(loadBlotInfo.copyInfo());
			}
		}
		return info;
		
	}
}
