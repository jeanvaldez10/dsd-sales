package dsd.dsdvansales.model;

import com.google.gson.Gson;

public class BluetoothTransferData {
    public int nType = 0; // 0 --- Confirm received. 1 --- Reject received, 10 ---- Adjust Inventory Request
    public String strRouteNumber = "";
    public String strJSONData = "";

    public static BluetoothTransferData fromJSON(String strText)
    {
        Gson gson = new Gson();
        try
        {
            return gson.fromJson(strText, BluetoothTransferData.class);
        }catch (Exception e)
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
