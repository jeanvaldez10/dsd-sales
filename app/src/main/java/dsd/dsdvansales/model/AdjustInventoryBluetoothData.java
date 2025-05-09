package dsd.dsdvansales.model;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AdjustInventoryBluetoothData {
    public String strUserName = "";
    public String strRouteNumber = "";
    public List<InventoryData> uInvenDInfoList = new ArrayList<>();

    public static AdjustInventoryBluetoothData fromJSON(String json)
    {
        Gson gson = new Gson();
        try
        {
            return gson.fromJson(json, AdjustInventoryBluetoothData.class);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

public String toJSON()
    {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
