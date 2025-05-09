package dsd.dsdvansales.model;

import java.io.Serializable;

public class PresoldOrHInfo implements Serializable {
	public int nIdx = 0;
	public int nChainNo = 0;
	public int nCustNo = 0;
	public int nDayNo = 0;
	public String strDetailFile = "";
	public String strOrderNo = "";
	public String strType = "";
	public String strPORef = "";
	public String strCredRef = "";
	public String strInstrs = "";
	public int	nQty = 0;
	public int nVisited = 0;
	public String strModifiable = "N";
	public String strDeliveryDate = "";
	public int nPeriodNo = 0;
	public int nDeliverySequence = 0;
	public String strOrderBarCode = "";
	public String strUser1 = "";
	public String strUser2 = "";
	public int nCustIdx = 0;

	public PresoldOrHInfo duplicate()
	{
		PresoldOrHInfo info = new PresoldOrHInfo();
		info.nIdx = nIdx;
		info.nChainNo = nChainNo;
		info.nCustNo = nCustNo;
		info.nDayNo = nDayNo;
		info.strDetailFile = strDetailFile;
		info.strOrderNo = strOrderNo;
		info.strType = strType;
		info.strPORef = strPORef;
		info.strCredRef = strCredRef;
		info.strInstrs = strInstrs;
		info.nQty = nQty;
		info.nVisited = nVisited;
		info.strModifiable = strModifiable;
		info.strDeliveryDate = strDeliveryDate;
		info.nPeriodNo = nPeriodNo;
		info.nDeliverySequence = nDeliverySequence;
		info.strOrderBarCode = strOrderBarCode;
		info.strUser1 = strUser1;
		info.strUser2 = strUser2;
		info.nCustIdx = nCustIdx;
		return info;
	}
}
