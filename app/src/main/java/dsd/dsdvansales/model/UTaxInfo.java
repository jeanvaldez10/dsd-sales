package dsd.dsdvansales.model;

public class UTaxInfo {
	public int nIdx = 0;
	public long nTrxnNo = 0;
	public int nTrxnType = 0;
	public int nCustNo = 0;
	public int nLocnNo = 0;
	public String strItemNo = "";
	public String strTaxRateCode = "";
	public float nTaxAmount = 0;
	public String strCumulativeFlag = "";
	public int nTaxRate = 0;
	public int nReasonCode = 0;
	public String strTaxRateNumber = "";

	public UTaxInfo duplicate()
	{
		UTaxInfo uTaxInfo = new UTaxInfo();
		uTaxInfo.nIdx = nIdx;
		uTaxInfo.nTrxnNo = nTrxnNo;
		uTaxInfo.nTrxnType = nTrxnType;
		uTaxInfo.nCustNo = nCustNo;
		uTaxInfo.nLocnNo = nLocnNo;
		uTaxInfo.strItemNo = strItemNo;
		uTaxInfo.strTaxRateCode = strTaxRateCode;
		uTaxInfo.nTaxAmount = nTaxAmount;
		uTaxInfo.strCumulativeFlag = strCumulativeFlag;
		uTaxInfo.nTaxRate = nTaxRate;
		uTaxInfo.nReasonCode = nReasonCode;
		uTaxInfo.strTaxRateNumber = strTaxRateNumber;
		return uTaxInfo;
	}
}
