package dsd.dsdvansales.model;

/**
 * Created by silve on 5/21/2017.
 */

public class UPromotionInfo {
    public int nIdx;
    public int nCustNo = 0;
    public int nChainNo = 0;
    public long nTrxnNo = 0;
    public String strItemNo = "";
    public String strFreeItemNo = "";
    public int nPlanNo = 0;
    public int nAssignNo = 0;
    public int nTrxnType = 0;
    public int nAmount = 0;
    public int nPriceDiscFlag = 0;
    public int nReasonCode = 0;
    public int nPromoAppMethod = 0;
    public int nPromoType = 0;
    public int nPromoMethod = 0;
    public int nDiscAmt = 0;
    public int nTrxnAmt = 0;
    public String strDateStart = "";
    public int nSeqNo = 0;

    public UPromotionInfo duplicate()
    {
        UPromotionInfo info = new UPromotionInfo();
        info.nTrxnNo = nTrxnNo;
        info.strItemNo = strItemNo;
        info.strFreeItemNo = strFreeItemNo;
        info.nPlanNo = nPlanNo;
        info.nAssignNo = nAssignNo;
        info.nTrxnType = nTrxnType;
        info.nAmount = nAmount;
        info.nPriceDiscFlag = nPriceDiscFlag;
        info.nReasonCode = nReasonCode;
        info.nPromoAppMethod = nPromoAppMethod;
        info.nPromoType = nPromoType;
        info.nDiscAmt = nDiscAmt;
        info.nTrxnAmt = nTrxnAmt;
        info.strDateStart = strDateStart;
        info.nPromoMethod = nPromoMethod;
        return info;
    }
}
