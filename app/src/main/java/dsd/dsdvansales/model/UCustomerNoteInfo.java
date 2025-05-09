package dsd.dsdvansales.model;

import java.util.ArrayList;
import java.util.List;

public class UCustomerNoteInfo {
    public long nTrxnNo = 0;
    public String strDocNo = "";
    public String strDocType = "";
    public String strTrxnTime = "";
    public String strTrxnDate = "";
    public int nCustNo = 0;
    public int nChainNo = 0;
    public String strSalesDate = "";
    public String strTransactionType = "";
    public String strUser = "";
    public String strNoteType = "";
    public String strReference = "";
    public String strMessage = "";
    public int nAttachment = 0;

    public List<UAttachmentInfo> uAttachmentInfoList = new ArrayList<>();
}
