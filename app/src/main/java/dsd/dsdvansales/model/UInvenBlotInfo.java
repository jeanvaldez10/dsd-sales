package dsd.dsdvansales.model;

import java.util.ArrayList;
import java.util.List;

public class UInvenBlotInfo {
    public long nTrxnNo = 0;
    public String strDocNo = "";
    public String strDocType = "";
    public int nVoidFlag = 0;
    public int nPrintedFlag = 0;
    public String strTrxnDate = "";
    public String strTrxnTime = "";
    public String strReference = "";
    public int nTComStatus = 0;
    public int nChainNo = 0;
    public int nCustNo = 0;
    public String strSaleDate = "";
    public String strTrip = "";
    public List<UInvenBlotDInfo> details = new ArrayList<>();
}
