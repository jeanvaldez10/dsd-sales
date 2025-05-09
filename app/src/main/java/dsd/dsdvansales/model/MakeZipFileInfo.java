package dsd.dsdvansales.model;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by silve on 3/5/2017.
 */

public class MakeZipFileInfo {
    public int nCustNo;
    public int nChainNo;
    public List<Long> trxnList = new ArrayList<>();

    public static MakeZipFileInfo fromJSON(String json)
    {
        Gson gson = new Gson();
        return gson.fromJson(json, MakeZipFileInfo.class);
    }

    public String toJSON()
    {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
