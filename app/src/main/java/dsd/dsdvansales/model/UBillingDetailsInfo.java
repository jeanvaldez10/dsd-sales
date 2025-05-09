package dsd.dsdvansales.model;

public class UBillingDetailsInfo {
    public String strCompanyTaxID = "";
    public String strBillingName = "";
    public String strAddress1 = "";
    public String strAddress2 = "";
    public String strCity = "";
    public String strState = "";
    public String strPostCode = "";
    public String strLegalName = "";
    public String strCreditLimit = "";
    public int nChainNo = 0;
    public int nCustNo = 0;

    public UBillingDetailsInfo duplicate()
    {
        UBillingDetailsInfo info = new UBillingDetailsInfo();
        info.strCompanyTaxID = strCompanyTaxID;
        info.strBillingName = strBillingName;
        info.strAddress1 = strAddress1;
        info.strAddress2 = strAddress2;
        info.strCity = strCity;
        info.strState = strState;
        info.strPostCode = strPostCode;
        info.strLegalName = strLegalName;
        info.strCreditLimit = strCreditLimit;
        info.nChainNo = nChainNo;
        info.nCustNo = nCustNo;
        return info;
    }
}
