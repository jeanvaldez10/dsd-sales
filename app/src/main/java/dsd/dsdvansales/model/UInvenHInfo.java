package dsd.dsdvansales.model;

import java.util.ArrayList;
import java.util.List;

public class UInvenHInfo {
	public int nIdx = 0;
	public long nTrxnNo = 0;
	public int nCustNo = 0;
	public String strTrxnDate = "";
	public String strTrxnTime = "";
	public String strReference = "";
	public String strSignatureFilePath = "";
	public String strDocType = "IVUP";

	public List<UInvenDInfo> uInvenDList = new ArrayList<>();
	public List<UInvenDInfo> truckList = new ArrayList<>();
	public List<UInvenDInfo> containerList = null;
}
