package dsd.dsdvansales.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by silve on 4/17/2017.
 */

public class UOrderStatusHInfo {
    public long nIdx = 0;
    public long nTrxnNo = 0;
    public String strDocNo = "";
    public String strDocType = "";
    public String strTrxnDate = "";
    public String strTrxnTime = "";
    public List<UOrderStatusDInfo> uOrderStatusDInfoList = new ArrayList<>();
}
