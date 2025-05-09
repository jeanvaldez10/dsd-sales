package dsd.dsdvansales.model;

import com.google.gson.Gson;

import java.io.Serializable;

public class DescTypeInfo implements Serializable {
	public int nIdx = 0;
	public String strDescriptionEntryKey = "";
	public int nSystemDescriptionTypeYN = 0;
	public String strDescriptionTypeID = "";
	public int nNumericCodeYN = 0;
	public int nNumericKey = 0;
	public String strAlphaKey = "";
	public String strDescription = "";
	public String strOwnership = "";
	public int nAlternateSeq = 0;
	public String strValue1 = "";
	public String strValue2 = "";
	public String strValue3 = "";
	public String strValue4 = "";
	public String strValue5 = "";
	public String strValue6 = "";

	public String toJSON()
	{
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public static DescTypeInfo fromJSON(String json)
	{
		Gson gson = new Gson();
		return gson.fromJson(json, DescTypeInfo.class);
	}
}