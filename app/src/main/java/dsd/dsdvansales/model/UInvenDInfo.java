package dsd.dsdvansales.model;

import java.text.*;
import java.util.*;

public class UInvenDInfo {
	public int nIdx = -1;
	public long nTrxnNo = 0;
	public String strItemNo = "";
	public int nLoaded = 0;
	public int nLoadAdj = 0;
	public int nStock = 0;
	public int nPrevStock = 0;
	public int nSales = 0;
	public int nDumps = 0;
	public int nBuybacks = 0;
	public int nTransIn = 0;
	public int nTransOut = 0;
	public int nSpoils = 0;
	public int nUnload = 0;
	public int nDumpsUnload = 0;
	public int nEndAdj = 0;

	public String strShortDesc = "";
	public String strDescription = "";
	public String strPickLocationCode = "";
	public String strUOM = "";
	public String strFullCase = "";
	public float nWeight = 0;
	public boolean bShow = false;
	public int nOrder = 0;
	public int nSCWType = 0;
	public int nProdLoconSCWType = 0;   // From the ProdLocn Table
	public int nType = 0;
	public boolean bCompleted = false;
	public int nCompleteStatus = 0;
	
	public String strDetailFile = "";
	public String strStartTime = "";

	public int nBeginQty = 0;
	public int nSequence = 0;

	public int nBeginPresold = 0;
	public int nBeginPeddle = 0;
	public int nResPresold = 0;
	public int nResPeddle = 0;
	public int nReasonCode = -1;

	public int nAdjQty = 0; // This value is used in Adjustment inventory processing.

	public double dBeginPresold = 0;
	public double dBeginPeddle = 0;
	public double dResPresold = 0;
	public double dResPeddle = 0;
	public double dLoaded = 0;
	public double dLoadAdj = 0;
	public double dStock = 0;
	public double dSales = 0;
	public double dDumps = 0;
	public double dBuybacks = 0;
	public double dTransIn = 0;
	public double dTransOut = 0;
	public double dSpoils = 0;
	public double dUnload = 0;
	public double dDumpsUnload = 0;
	public double dEndAdj = 0;
	public double dBeginQty = 0;
	public double dAdjQty = 0; // This value is used in Adjustment inventory processing.

	public String strBatchCode = "";
	public double dLoadTruck = 0;	//Runtimeable variable
	public int nLoadTruck = 0;	//Runtimeable variable

	public boolean bLotControl = false;

	public List<LoadBlotInfo> loadBlotInfoList = new ArrayList<>();
	public UInvenDInfo copyInfo() {
		UInvenDInfo newInfo = new UInvenDInfo();
		newInfo.nTrxnNo = this.nTrxnNo;
		newInfo.strItemNo = this.strItemNo;
		newInfo.nLoaded = this.nLoaded;
		newInfo.nLoadAdj = this.nLoadAdj;
		newInfo.nBeginQty = this.nBeginQty;
		newInfo.nStock = this.nStock;
		newInfo.nSales = this.nSales;
		newInfo.nDumps = this.nDumps;
		newInfo.nBuybacks = this.nBuybacks;
		newInfo.nTransIn = this.nTransIn;
		newInfo.nTransOut = this.nTransOut;
		newInfo.nSpoils = this.nSpoils;
		newInfo.nUnload = this.nUnload;
		newInfo.nDumpsUnload = this.nDumpsUnload;
		newInfo.nEndAdj = this.nEndAdj;
		newInfo.strShortDesc = this.strShortDesc;
		newInfo.strDescription = this.strDescription;
		newInfo.strPickLocationCode = this.strPickLocationCode;
		newInfo.strUOM = this.strUOM;
		newInfo.strFullCase = this.strFullCase;
		newInfo.nWeight = this.nWeight;
		newInfo.bShow = this.bShow;
		newInfo.nOrder = this.nOrder;
		newInfo.nSCWType = this.nSCWType;
		newInfo.nType = this.nType;
		newInfo.strDetailFile = strDetailFile;
		newInfo.nAdjQty = nAdjQty;

		newInfo.nBeginPeddle = nBeginPeddle;
		newInfo.nBeginPresold = nBeginPresold;
		newInfo.nResPeddle = nResPeddle;
		newInfo.nResPresold = nResPresold;

		newInfo.dLoaded = dLoaded;
		newInfo.dStock = dStock;
		newInfo.dSales = dSales;
		newInfo.dDumps = dDumps;
		newInfo.dLoadAdj = dLoadAdj;
		newInfo.dBeginQty = dBeginQty;
		newInfo.dAdjQty = dAdjQty;
        newInfo.nProdLoconSCWType = nProdLoconSCWType;

		newInfo.dBeginPeddle = dBeginPeddle;
		newInfo.dBeginPresold = dBeginPresold;
		newInfo.dResPeddle = dResPeddle;
		newInfo.dResPresold = dResPresold;

		newInfo.dEndAdj = dEndAdj;
		newInfo.dDumpsUnload = dDumpsUnload;
		newInfo.dUnload = dUnload;

		newInfo.dBuybacks = dBuybacks;
		newInfo.dTransIn = dTransIn;
		newInfo.dTransOut = dTransOut;
		newInfo.dSpoils = dSpoils;
		newInfo.strBatchCode = strBatchCode;
		newInfo.bLotControl = bLotControl;
		newInfo.loadBlotInfoList = new ArrayList<>();
		if(loadBlotInfoList != null)
		{
			for(LoadBlotInfo info : loadBlotInfoList)
			{
				newInfo.loadBlotInfoList.add(info.copyInfo());
			}
		}

		return newInfo;
	}

	public final static Comparator<UInvenDInfo> orderComparator = new Comparator<UInvenDInfo>() {
		// Descending
		@Override
		public int compare(UInvenDInfo object1, UInvenDInfo object2) {
			return (object1.nOrder < object2.nOrder ? -1 : (object1.nOrder == object2.nOrder ? 0 : 1)) * -1;
		}
	};

	public final static Comparator<UInvenDInfo> codeComparator = new Comparator<UInvenDInfo>() {
		private final Collator collator = Collator.getInstance();
		@Override
		public int compare(UInvenDInfo object1, UInvenDInfo object2) {
			return collator.compare(object1.strPickLocationCode, object2.strPickLocationCode);
		}
	};
	public final static Comparator<UInvenDInfo> itemComparator = new Comparator<UInvenDInfo>() {
		// Descending
		@Override
		public int compare(UInvenDInfo object1, UInvenDInfo object2) {
			return object1.strItemNo.compareTo(object2.strItemNo);
		}
	};

}
