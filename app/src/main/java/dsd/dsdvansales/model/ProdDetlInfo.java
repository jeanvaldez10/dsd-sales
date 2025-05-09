package dsd.dsdvansales.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dsd.dsdvansales.global.Global;

public class ProdDetlInfo {
	public int nIdx = 0;
	public String strItemNo = "";
	public String strDescription = "";
	public String strShortDesc = "";
	public String strItemUPC = "";
	public String strItemUPC20 = "";
	public float nWeight = 0;
	public String strItemType = "";
	public String strProdGrp = "";

	public String strMarketGrp = "";
	public String strPromoGrp = "";

	public String strProdLine = "";
	public String strPackSize = "";
	public int nShelfLife = 0;
	public String strBrand = "";
	public String strSubBrand = "";
	public String strType = "";
	public String strDumpsAllowed = "";
	public String strSalesAllowed = "";
	public String strGroupFilterKey = "";
	public String strServiceFee = "";
	public String strLotControl = "";
	public String strItemInactiveSwitch = "";
	public String strActiveDate = "";

	public float nPrice = 0;
	public float nOrgPrice = 0;
	public float nGrossPrice = 0;
	public float nOrgGrossPrice = 0;
	public float nDiscountVal = 0;
	public float nPromoDiscount = 0;
	public int nPromoType = 0;
	public float nMaxQty = 0;
	public int nConsumerUnit = 1;
	public String strImageUrl = "";
	public ProdLocnInfo prodLocnInfo = new ProdLocnInfo();
	public List<UPromotionInfo> uPromotionInfoList = new ArrayList<>();

	public String strDescriptionArray[] = new String[Global.PRODUCT_FILTER_COUNT];
	public ProdDetlInfo() {
		for (int i = 0; i < Global.PRODUCT_FILTER_COUNT; i++) {
			this.strDescriptionArray[i] = "";
		}
	}

	public boolean isInactive() {
		String strNowDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
		if (strItemInactiveSwitch.toUpperCase().equals("Y") && strActiveDate.compareTo(strNowDate) > 0) {
			return true;
		}
		return false;
	}
}