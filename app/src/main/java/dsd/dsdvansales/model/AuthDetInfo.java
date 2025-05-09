package dsd.dsdvansales.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import dsd.dsdvansales.global.Global;

/**
 * Created by silve on 9/2/2016.
 */
public class AuthDetInfo {
    public int nIdx = 0;
    public int nAuthGrp = 0;
    public String strItemNo = "";
    public String strStartDate = "";
    public String strEndDate = "";

    public boolean isValidate(Date date)
    {
        String strCurDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(date);

        Date startDate = null;
        Date endDate = null;
        try
        {
            startDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).parse(strStartDate);
            endDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).parse(strEndDate);
        }catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }

        if( date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0 && strCurDate.compareTo(strStartDate) >= 0 && strCurDate.compareTo(strEndDate) <= 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
