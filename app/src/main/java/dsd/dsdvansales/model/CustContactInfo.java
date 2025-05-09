package dsd.dsdvansales.model;

public class CustContactInfo {
	public int nIdx = 0;
	public int nChainNo = 0;
	public int nCustNo = 0;
	public int nContactType = 0;
	public String strContactTypeDesc = "";
	public String strContactName = "";
	public String strContactPhoneNumber = "";
	public String strContactEmailAddress = "";
	public String strContactInfo = "";
	
	public CustContactInfo duplicate()
	{
		CustContactInfo info = new CustContactInfo();
		info.nIdx = nIdx;
		info.nChainNo = nChainNo;
		info.nCustNo = nCustNo;
		info.nContactType = nContactType;
		info.strContactTypeDesc = strContactTypeDesc;
		info.strContactName = strContactName;
		info.strContactPhoneNumber = strContactPhoneNumber;
		info.strContactEmailAddress = strContactEmailAddress;
		info.strContactInfo = strContactInfo;
		
		return info;
	}
}