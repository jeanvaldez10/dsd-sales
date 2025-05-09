package dsd.dsdvansales.model;

import java.util.ArrayList;
import java.util.List;

public class URouteSchHeader {
	public int nIdx = 0;
	public long nTrxnNo = 0;
	public String strDocType = "SCHD";
	public String strTrxnDate = "";
	public String strTrxnTime = "";
	
	public List<URouteSchBody> body = new ArrayList<>();
}
