package dsd.dsdvansales.model;

import java.util.*;

public class SurvQuesInfo {
	public int nIdx = 0;
	public String strSurveyID = "";
	public String strQuestionID = "";
	public String strQuestionText = "";
	public String strQuestionType = "";
	public String strMandatory = "";
	public int nLookupNo = 0;
	public String strMultiResp = "";
	public boolean bImageCaptureReq = false;
	public int nQuestionNumberYes = 0;
	public int nQuestionNumberNo = 0;
	public List<SurvQoptInfo> responseList = new ArrayList<>();
}