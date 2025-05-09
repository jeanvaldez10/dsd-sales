package dsd.dsdvansales.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by silve on 7/7/2016.
 */
public class UStockHInfo {
    public int nIdx = 0;
    public long nTrxnNo = 0;
    public int nChainNo = 0;
    public int nCustNo = 0;
    public String strDocType = "LOAD";
    public String strStockType = "8";
    public String strDocNo = "";
    public String strTrxnDate = "";
    public String strTrxnTime = "";
    public int printedFlag = 0;
    public int nStockType = 8;
    public String strDetailKeyExpanded = "";
    public String strTransferRoute = "";
    public String strRequiredDate = "0";
    public String strPeriod = "";
    public String strToWarehouse = "";
    public int nTripNo = 0;
    public String strReference = "";
    public List<UStockDInfo> uStockDInfoList = new ArrayList<>();
}
