package dsd.dsdvansales.model;

import java.util.*;

public class USurveysInfo {
	public int nIdx = 0;
	public long nTrxnNo = 0;
	public int nChainNo = 0;
	public int nCustNo = 0;
	public String strSurveyID = "";
	public boolean bFromSurveyID = true;
	public String strCompleted = "";
	public String strTrxnDate = "";
	public String strTrxnTime = "";
	public String strReference = "";
	public List<USurveyDetailInfo> detailList = new ArrayList<>();
}