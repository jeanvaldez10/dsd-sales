package dsd.dsdvansales.model;

import java.util.ArrayList;
import java.util.List;

public class ContactDetail{
    public long nIdx = 0;
    public int nChainNo = 0;
    public int nCustNo = 0;
    public String strContactType = "";
    public String strContactTypeID = "";
    public int nContactTypeNumericKey = 0;
    public String strContactName = "";
    public String strContactPhone1 = "";
    public String strContactPhone2 = "";
    public String strContactPhone3 = "";
    public String strContactEmail1 = "";
    public String strContactEmail2 = "";
    public String strContactEmail3 = "";

    public List<ContactInfo> contactInfoList = new ArrayList<>();

}
