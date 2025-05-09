package dsd.dsdvansales.model;

public class LoadBlotInfo {
    public String strDetailFile = "";
    public String strItemNo = "";
    public String strLotNumber = "";
    public int nLotQty = 0;
    public float fLotWeight = 0;
    public String strBatchDate = "";

    public LoadBlotInfo copyInfo()
    {
        LoadBlotInfo info = new LoadBlotInfo();
        info.strDetailFile = strDetailFile;
        info.strItemNo = strItemNo;
        info.strLotNumber = strLotNumber;
        info.nLotQty = nLotQty;
        info.fLotWeight = fLotWeight;
        info.strBatchDate = strBatchDate;
        return info;
    }
}
