package dsd.dsdvansales.model;

public class UCustomerFMInfo {
	public int nIdx = 0;
	public int nChainNo = 0;
	public int nCustNo = 0;
	public long nTrxnNo = 0;
	public String strEditType = "";
	public String strName = "";
	public String strCompanyTaxID = "";
	public String strAddress1 = "";
	public String strAddress2 = "";
	public String strCity = "";
	public String strShipToState = "";
	public String strShipToZip = "";
	public String strPhone = "";
	public String strContactType = "";
	public String strContactName = "";
	public String strContactPhoneNumber = "";
	public String strContactEmailAddress = "";
	public String strTrxnDate = "";
	public String strTrxnTime = "";
	public String strStartDate = "";
	public String strStartTime = "";
	public String strEndDate = "";
	public String strEndTime = "";
	public String strDeliveryWindowFrom = "";
	public String strDeliveryWindowTo = "";
	public String strStripeInfo = "";
	public String strType = "";
	public String strUser1 = "";
	public int nTemplateChainNo = 0;
	public int nTemplateCustNo = 0;

	public UCustomerFMInfo duplicate()
	{
		UCustomerFMInfo info = new UCustomerFMInfo();
		info.nIdx = nIdx;
		info.nChainNo = nChainNo;
		info.nCustNo = nCustNo;
		info.nTrxnNo = nTrxnNo;
		info.strEditType = strEditType;
		info.strName = strName;
		info.strCompanyTaxID = strCompanyTaxID;
		info.strAddress1 = strAddress1;
		info.strAddress2 = strAddress2;
		info.strCity = strCity;
		info.strShipToState = strShipToState;
		info.strShipToZip = strShipToZip;
		info.strPhone = strPhone;
		info.strContactType = strContactType;
		info.strContactName = strContactName;
		info.strContactPhoneNumber = strContactPhoneNumber;
		info.strContactEmailAddress = strContactEmailAddress;
		info.strTrxnDate = strTrxnDate;
		info.strTrxnTime = strTrxnTime;
		info.strStartDate = strStartDate;
		info.strStartTime = strStartTime;
		info.strEndDate = strEndDate;
		info.strEndTime = strEndTime;
		info.strDeliveryWindowFrom = strDeliveryWindowFrom;
		info.strDeliveryWindowTo = strDeliveryWindowTo;
		info.strStripeInfo = strStripeInfo;
		info.strType = strType;
		info.strUser1 = strUser1;
		info.nTemplateChainNo = nTemplateChainNo;
		info.nTemplateCustNo = nTemplateCustNo;
		return info;
	}
}
