package dsd.dsdvansales.global;

import android.text.TextUtils;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.AdjustInventoryDialog;
import dsd.dsdvansales.model.AdjustInventoryBluetoothData;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.InventoryData;
import dsd.dsdvansales.model.MakeZipFileInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UFileTransactionInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;
import dsd.dsdvansales.model.UStockDInfo;
import dsd.dsdvansales.model.UStockHInfo;
import dsd.dsdvansales.model.UTransactionsInfo;

public class BluetoothProcessor {

    private Font fontBlack7Bold;
    private Font fontBlack10Bold;
    private Font fontBlack7Normal;

    long mPdfTrxnNo = 0;
    Date mTrxnDate = null;
    String mStrPdfFileName = "";
    MainActivity mContext;
    int mTruckCnt = 0;
    int mContainerCnt = 0;
    List<UInvenDInfo> mUInvenDList = new ArrayList<>();
    public MakeZipFileInfo makeZipFileInfo = null;
    long m_lTrxnNo = 0;
    Date mCurDate = null;
    public BluetoothProcessor(MainActivity context)
    {
        mContext = context;
    }

    public void initMakeZipInfo()
    {
        if( makeZipFileInfo != null ) {
            makeZipFileInfo.trxnList.clear();
            makeZipFileInfo = null;
        }
    }

    public long setGeneralUploadData(String strDocType, int nChainNo, int nCustNo, long nTrxnNo) {
//        double nLat = Utils.getDoubleSetting(mContext, Global.PREF_MY_LATITUDE);
//        double nLng = Utils.getDoubleSetting(mContext, Global.PREF_MY_LONGITUDE);

        if( nTrxnNo != -1 )
            m_lTrxnNo = nTrxnNo;
        else
            m_lTrxnNo = System.currentTimeMillis();
        mCurDate = new Date(m_lTrxnNo);
        UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
        uTransactionInfo.nTrxnNo = m_lTrxnNo;
        uTransactionInfo.strDocType = strDocType;
        uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mCurDate));
        uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
        uTransactionInfo.nChainNo = nChainNo;
        uTransactionInfo.nCustNo = nCustNo;

        uTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
        uTransactionInfo.strDriverId = "";
        uTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
        uTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);

        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        dbAdapter.insertUTransactionsInfo(uTransactionInfo);
        dbAdapter.close();
        AddTrxnNo(nCustNo, nChainNo, m_lTrxnNo);
        return m_lTrxnNo;
    }

    public long setGeneralUploadData(String strDocType, int nChainNo, int nCustNo, long nTrxnNo, String strReference, String strDocNo) {
//        double nLat = Utils.getDoubleSetting(mContext, Global.PREF_MY_LATITUDE);
//        double nLng = Utils.getDoubleSetting(mContext, Global.PREF_MY_LONGITUDE);

        if( nTrxnNo != -1 )
            m_lTrxnNo = nTrxnNo;
        else
            m_lTrxnNo = System.currentTimeMillis();

        mCurDate = new Date(m_lTrxnNo);

        UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
        uTransactionInfo.nTrxnNo = m_lTrxnNo;
        uTransactionInfo.strDocType = strDocType;
        uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mCurDate));
        uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
        uTransactionInfo.nChainNo = nChainNo;
        uTransactionInfo.strReference = strReference;
        uTransactionInfo.nCustNo = nCustNo;
        uTransactionInfo.strDocNo = strDocNo;

        uTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
        uTransactionInfo.strDriverId = "";
        uTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
        uTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);

        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        dbAdapter.insertUTransactionsInfo(uTransactionInfo);
        dbAdapter.close();

        AddTrxnNo(nCustNo, nChainNo, m_lTrxnNo);

        return m_lTrxnNo;
    }

    public void AddTrxnNo(int nCustNo, int nChainNo, long nTrxnNo)
    {
        if( makeZipFileInfo == null )
        {
            makeZipFileInfo = new MakeZipFileInfo();
            makeZipFileInfo.nCustNo = nCustNo;
            makeZipFileInfo.nChainNo = nChainNo;
        }
        boolean bAdd = true;
        for(Long trxnNo : makeZipFileInfo.trxnList)
        {
            if(trxnNo == nTrxnNo)
            {
                bAdd = false;
                break;
            }
        }
        if( bAdd )
        {
            makeZipFileInfo.trxnList.add(nTrxnNo);
        }
    }

    public UTransactionsInfo setUTransactionsInfo(int nChainNo, int nCustNo, long nTrxnNo, String strDocType, String strReference) {
        Date curDate = new Date(nTrxnNo);
        UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
        uTransactionInfo.nTrxnNo = nTrxnNo;
        uTransactionInfo.strDocType = strDocType;
        uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, curDate));
        uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(curDate);
        uTransactionInfo.strReference = strReference;
        uTransactionInfo.nChainNo = nChainNo;
        uTransactionInfo.nCustNo = nCustNo;

        uTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
        uTransactionInfo.strDriverId = "";
        uTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
        uTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);

        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        dbAdapter.insertUTransactionsInfo(uTransactionInfo);
        dbAdapter.close();
        AddTrxnNo(nCustNo, nChainNo, nTrxnNo);
        return uTransactionInfo;
    }

    public void setUFileTransactionInfo(long nTrxnNo, int nChainNo, int nCustNo, long nFileTrxnNo, String strFileDocNo, String strFileShortDesc, String strFileLongDesc, String strFileCreateDate, String strFileCreateTime, String strFileFileName)
    {
        UFileTransactionInfo uTransactionInfo = new UFileTransactionInfo();
        uTransactionInfo.nTrxnNo = nTrxnNo;
        uTransactionInfo.strDocType = "FARC";
        Date createDateTime = Utils.getRoutectlTripDateTime(mContext, new Date(uTransactionInfo.nTrxnNo));
        uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(createDateTime);
        uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(new Date(nTrxnNo));
        uTransactionInfo.nChainNo = nChainNo;
        uTransactionInfo.nCustNo = nCustNo;
        uTransactionInfo.nTripNo = Utils.parseIntFromString(Global.gTripNumber);
        uTransactionInfo.nFileTrxnNo = nFileTrxnNo;
        uTransactionInfo.strFileDocNo = strFileDocNo;
        uTransactionInfo.strFileShortDescription = strFileShortDesc;
        uTransactionInfo.strFileLongDescription = strFileLongDesc;
        uTransactionInfo.strFileCreateDate = uTransactionInfo.strTrxnDate;
        uTransactionInfo.strFileCreateTime = uTransactionInfo.strTrxnTime;
        uTransactionInfo.strFileFileName = strFileFileName;

        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        dbAdapter.insertUFileTransactionsInfo(uTransactionInfo);
        dbAdapter.close();
        AddTrxnNo(nCustNo, nChainNo, nTrxnNo);
    }

    private int getMaxOrder() {
        int nOrder = 0;
        for (UInvenDInfo info : mUInvenDList) {
            if (info.nOrder > nOrder) {
                nOrder = info.nOrder;
            }
        }
        return nOrder;
    }

    public void doMakeAdjustInventory(AdjustInventoryBluetoothData data)
    {
        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        List<ProdDetlInfo> mProdDetlList = dbAdapter.getProdDetlList();
        List<UInvenDInfo> uInvenDInfos = dbAdapter.getUInvenDList(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
        dbAdapter.close();

        Map<String, InventoryData> dataMap = new HashMap<>();
        Map<String, UInvenDInfo> invenMap = new HashMap<>();
        for(InventoryData dataItem : data.uInvenDInfoList)
        {
            dataMap.put(dataItem.strItem, dataItem);
        }

        //Build the UInventory Data and Update Data
        mUInvenDList.clear();
        mTruckCnt = 0;
        mContainerCnt = 0;
        for(UInvenDInfo info : uInvenDInfos)
        {
            if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
            {
                InventoryData dataItem = dataMap.get(info.strItemNo);
                if(dataItem != null)
                {
                    info.bShow = true;
                    info.dAdjQty = dataItem.weight;
                    info.dStock += info.dAdjQty;
                    mUInvenDList.add(info);
                    invenMap.put(info.strItemNo, info);
                }
                else if( info.bShow )
                {
                    info.dAdjQty = 0;
                    mUInvenDList.add(info);
                    invenMap.put(info.strItemNo, info);
                }
            }
            else
            {
                InventoryData dataItem = dataMap.get(info.strItemNo);
                if(dataItem != null)
                {
                    info.bShow = true;
                    info.nAdjQty = dataItem.nQty;
                    info.nStock += info.nAdjQty;
                    mUInvenDList.add(info);
                    invenMap.put(info.strItemNo, info);
                }
                else if( info.bShow )
                {
                    info.nAdjQty = 0;
                    mUInvenDList.add(info);
                    invenMap.put(info.strItemNo, info);
                }
            }
        }

        for(InventoryData dataItem : data.uInvenDInfoList)
        {
            if(invenMap.get(dataItem.strItem) == null)
            {
                ProdDetlInfo productInfo = null;
                for (int i = 0; i < mProdDetlList.size(); i++) {
                    if (mProdDetlList.get(i).strItemNo.equals(dataItem.strItem) == true ||
                            mProdDetlList.get(i).strItemUPC.equals(dataItem.strItem) == true ||
                            mProdDetlList.get(i).strItemUPC20.equals(dataItem.strItem) == true) {
                        productInfo = mProdDetlList.get(i);
                        break;
                    }
                }
                if(productInfo != null)
                {
                    UInvenDInfo info = new UInvenDInfo();
                    info.strItemNo = dataItem.strItem;
                    info.dAdjQty = dataItem.weight;
                    info.nAdjQty = dataItem.nQty;
                    info.nStock = dataItem.nQty;
                    info.dStock = dataItem.weight;

                    info.strShortDesc = productInfo.strShortDesc;
                    info.strDescription = productInfo.strDescription;
                    info.strUOM = productInfo.prodLocnInfo.strCaseUOM.length() > 0 ? productInfo.prodLocnInfo.strCaseUOM : productInfo.prodLocnInfo.strUnitUOM;
                    info.nWeight = productInfo.nWeight;
                    info.bShow = true;
                    info.nOrder = getMaxOrder() + 1;
                    mUInvenDList.add(info);
                    invenMap.put(info.strItemNo, info);
                }
            }
        }

        if(Global.gAdjustinfo == null)
        {
            Global.gAdjustinfo = new UInvenHInfo();
            Global.gAdjustinfo.nTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
        }
        Global.gAdjustinfo.uInvenDList.clear();
        Global.gAdjustinfo.uInvenDList.addAll(mUInvenDList);
    }

    public void doProcess(final AdjustInventoryBluetoothData data)
    {
        loadFont();

        if (Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO) == 0)
        {
            Utils.setLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO, System.currentTimeMillis());
        }

        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        List<ProdDetlInfo> mProdDetlList = dbAdapter.getProdDetlList();
        List<UInvenDInfo> uInvenDInfos = dbAdapter.getUInvenDList(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
        dbAdapter.close();

        Map<String, InventoryData> dataMap = new HashMap<>();
        Map<String, UInvenDInfo> invenMap = new HashMap<>();
        for(InventoryData dataItem : data.uInvenDInfoList)
        {
            dataMap.put(dataItem.strItem, dataItem);
        }

        //Build the UInventory Data and Update Data
        mUInvenDList.clear();
        mTruckCnt = 0;
        mContainerCnt = 0;
        for(UInvenDInfo info : uInvenDInfos)
        {
            if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
            {
                InventoryData dataItem = dataMap.get(info.strItemNo);
                if(dataItem != null)
                {
                    info.bShow = true;
                    info.dAdjQty = dataItem.weight;
                    info.dStock += info.dAdjQty;
                    info.dTransIn += info.dAdjQty;
                    mUInvenDList.add(info);
                    invenMap.put(info.strItemNo, info);
                }
                else if( info.bShow )
                {
                    info.dAdjQty = 0;
                    mUInvenDList.add(info);
                    invenMap.put(info.strItemNo, info);
                }
            }
            else
            {
                InventoryData dataItem = dataMap.get(info.strItemNo);
                if(dataItem != null)
                {
                    info.bShow = true;
                    info.nAdjQty = dataItem.nQty;
                    info.nStock += info.nAdjQty;
                    info.nTransIn += info.nAdjQty;
                    mUInvenDList.add(info);
                    invenMap.put(info.strItemNo, info);
                }
                else if( info.bShow )
                {
                    info.nAdjQty = 0;
                    mUInvenDList.add(info);
                    invenMap.put(info.strItemNo, info);
                }
            }
        }

        for(InventoryData dataItem : data.uInvenDInfoList)
        {
            if(invenMap.get(dataItem.strItem) == null)
            {
                ProdDetlInfo productInfo = null;
                for (int i = 0; i < mProdDetlList.size(); i++) {
                    if (mProdDetlList.get(i).strItemNo.equals(dataItem.strItem) == true ||
                            mProdDetlList.get(i).strItemUPC.equals(dataItem.strItem) == true ||
                            mProdDetlList.get(i).strItemUPC20.equals(dataItem.strItem) == true) {
                        productInfo = mProdDetlList.get(i);
                        break;
                    }
                }
                if(productInfo != null)
                {
                    UInvenDInfo info = new UInvenDInfo();
                    info.strItemNo = dataItem.strItem;
                    info.dAdjQty = dataItem.weight;
                    info.nAdjQty = dataItem.nQty;
                    info.nStock = dataItem.nQty;
                    info.dStock = dataItem.weight;
                    info.dTransIn = dataItem.weight;
                    info.nTransIn = dataItem.nQty;

                    info.strShortDesc = productInfo.strShortDesc;
                    info.strDescription = productInfo.strDescription;
                    info.strUOM = productInfo.prodLocnInfo.strCaseUOM.length() > 0 ? productInfo.prodLocnInfo.strCaseUOM : productInfo.prodLocnInfo.strUnitUOM;
                    info.nWeight = productInfo.nWeight;
                    info.bShow = true;
                    info.nOrder = getMaxOrder() + 1;
                    mUInvenDList.add(info);
                    invenMap.put(info.strItemNo, info);
                }
            }
        }
        dbAdapter.open();
        for (UInvenDInfo info : mUInvenDList) {
            if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
            {
                if (info.nSCWType == Global.INVENTORY_TRUCK && info.dStock > 0) {
                    mTruckCnt++;
                } else if (info.nSCWType == Global.INVENTORY_CONTAINERS && info.dStock > 0) {
                    mContainerCnt++;
                }
                if( info.nIdx == -1 )
                {
                    info.nTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
                    info.nSCWType = Global.INVENTORY_TRUCK;
                    info.nIdx = (int)dbAdapter.insertUInvenDInfo(info);
                }
                else
                {
                    dbAdapter.updateUInvenDInfo(info);
                }
            }
            else
            {
                if (info.nSCWType == Global.INVENTORY_TRUCK && info.nStock > 0) {
                    mTruckCnt++;
                } else if (info.nSCWType == Global.INVENTORY_CONTAINERS && info.nStock > 0) {
                    mContainerCnt++;
                }
                if( info.nIdx == -1 )
                {
                    info.nTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
                    info.nSCWType = Global.INVENTORY_TRUCK;
                    info.nIdx = (int)dbAdapter.insertUInvenDInfo(info);
                }
                else
                {
                    dbAdapter.updateUInvenDInfo(info);
                }
            }
        }
        dbAdapter.close();
        //Create Pdf
        String strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_LOAD_SHEET_TEMPLATE_FILENAME);
        if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + strTemplateFileName))
        {
            PrintEngine printEngine = new PrintEngine();
            Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
            mPdfTrxnNo = System.currentTimeMillis();
            mStrPdfFileName = getPDFFileName();

            List<UInvenDInfo> truckList = new ArrayList<>();
            List<UInvenDInfo>  containerList = new ArrayList<>();
            for (UInvenDInfo info : mUInvenDList) {
                if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
                {
                    double dValue = info.dAdjQty;
                    UInvenDInfo info1 = info.copyInfo();
                    info1.dStock = dValue;
                    if (info.bShow == true && info.nSCWType == Global.INVENTORY_TRUCK && info1.dStock > 0) {
                        truckList.add(info1);
                    }
                }
                else
                {
                    int nValue = info.nAdjQty;
                    UInvenDInfo info1 = info.copyInfo();
                    info1.nStock = nValue;
                    if (info.bShow == true && info.nSCWType == Global.INVENTORY_TRUCK && info1.nStock > 0) {
                        truckList.add(info1);
                    }
                }

            }
            if (mContainerCnt > 0) {
                for (UInvenDInfo info : mUInvenDList) {
                    if (info.bShow == true && info.nSCWType == Global.INVENTORY_CONTAINERS && info.nStock > 0) {
                        containerList.add(info);
                    }
                }
            }
            String strTitle = "ADJUST INVENTORY – IN";
            String strType = "";
            if(mTrxnDate == null)
            {
                mTrxnDate = new Date(mPdfTrxnNo);
            }

            dbAdapter = new DbAdapter(mContext);
            dbAdapter.open();
            Utils.gDetlMap.clear();
            if(truckList != null)
            {
                for(UInvenDInfo info : truckList)
                {
                    ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
                    if(prodDetlInfo != null)
                    {
                        Utils.gDetlMap.put(prodDetlInfo.strItemNo, Utils.getPropertyMap(prodDetlInfo));
                    }
                }
            }

            for(UInvenDInfo info : containerList)
            {
                ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
                if(prodDetlInfo != null)
                {
                    Utils.gDetlMap.put(prodDetlInfo.strItemNo, Utils.getPropertyMap(prodDetlInfo));
                }
            }
            dbAdapter.close();

            printEngine.PreparePrint(mContext, truckList, containerList, strType, mTrxnDate,
                    "", mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), strTitle, "");

            printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, Global.LOAD_ADJUSTMENT_PRINT);
        }
        else
        {
            createPDF();
        }

        initMakeZipInfo();
        String strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
        long nTrxnNo = setGeneralUploadData("TRNI" , 0, 0, mPdfTrxnNo, "", strDocNo);

        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.strType = "TRNI";
        transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
        transactionInfo.strCustomerName = "";
        transactionInfo.strDocNo = strDocNo;

        dbAdapter.open();
        dbAdapter.insertTransactionHistoryInfo(transactionInfo);
        Global.gAdjustinfo = dbAdapter.getUInvenHInfo(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
        dbAdapter.close();

        UTransactionsInfo uTransactionsInfo = setUTransactionsInfo(0, 0, m_lTrxnNo, "FARC", mStrPdfFileName);
        setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, 0, 0, mPdfTrxnNo, strDocNo,
                "STOCKADJUST", "STOCK ADJUST", uTransactionsInfo.strTrxnDate,
                uTransactionsInfo.strTrxnTime, mStrPdfFileName);
        AddTrxnNo(0, 0, mPdfTrxnNo);


        if (Global.gAdjustinfo == null) {
            Global.gAdjustinfo = new UInvenHInfo();
            Global.gAdjustinfo.nTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
        }

        int nReasonCode = 0;
        if (Global.gAdjustinfo != null) {
            Global.gAdjustinfo.nTrxnNo = mPdfTrxnNo;
            Global.gAdjustinfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date(Global.gAdjustinfo.nTrxnNo));
            Global.gAdjustinfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(new Date(Global.gAdjustinfo.nTrxnNo));
            Global.gAdjustinfo.strReference = "";
            Global.gAdjustinfo.strDocType = "IVUP";
            UStockHInfo stockHInfo = new UStockHInfo();
            stockHInfo.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
            stockHInfo.strReference = stockHInfo.strDocNo + ".pdf";

            stockHInfo.nTrxnNo = Global.gAdjustinfo.nTrxnNo;
            stockHInfo.strStockType = "2";

            stockHInfo.strTransferRoute = "";
            stockHInfo.strToWarehouse = "";

            stockHInfo.strTrxnDate = Global.gAdjustinfo.strTrxnDate;
            stockHInfo.strTrxnTime = Global.gAdjustinfo.strTrxnTime;
            stockHInfo.strDocType = "TRNI";

            dbAdapter = new DbAdapter(mContext);
            dbAdapter.open();
            dbAdapter.insertUStockHInfoList(stockHInfo);
            dbAdapter.insertUInvenHInfo(Global.gAdjustinfo);
            for (UInvenDInfo info : mUInvenDList) {
                if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
                {
                    if (info.nSCWType == Global.INVENTORY_TRUCK && info.dStock > 0) {
                        mTruckCnt++;
                    } else if (info.nSCWType == Global.INVENTORY_CONTAINERS && info.dStock > 0) {
                        mContainerCnt++;
                    }

                    info.nTrxnNo = mPdfTrxnNo;
                    info.nIdx = (int)dbAdapter.insertUInvenDInfo(info);

                    if( info.dAdjQty > 0)
                    {
                        UStockDInfo stockDInfo = new UStockDInfo();
                        stockDInfo.nTrxnNo = mPdfTrxnNo;
                        stockDInfo.strItemNo = info.strItemNo;
                        stockDInfo.nReasonCode = nReasonCode;
                        stockDInfo.dActualQty = info.dAdjQty;
                        stockDInfo.dBeginQty = info.dStock - info.dAdjQty;
                        stockDInfo.dSuggestQty = 0;

                        dbAdapter.insertUStockDInfoList(stockDInfo);
                    }
                }
                else
                {
                    if (info.nSCWType == Global.INVENTORY_TRUCK && info.nStock > 0) {
                        mTruckCnt++;
                    } else if (info.nSCWType == Global.INVENTORY_CONTAINERS && info.nStock > 0) {
                        mContainerCnt++;
                    }

                    info.nTrxnNo = mPdfTrxnNo;
                    info.nIdx = (int)dbAdapter.insertUInvenDInfo(info);

                    if( info.nAdjQty > 0)
                    {
                        UStockDInfo stockDInfo = new UStockDInfo();
                        stockDInfo.nTrxnNo = mPdfTrxnNo;
                        stockDInfo.strItemNo = info.strItemNo;
                        stockDInfo.nReasonCode = nReasonCode;
                        stockDInfo.nActualQty = info.nAdjQty;
                        stockDInfo.nBeginQty = info.nStock - info.nAdjQty;
                        stockDInfo.nSuggestQty = 0;

                        dbAdapter.insertUStockDInfoList(stockDInfo);
                    }
                }

            }
            dbAdapter.close();

            Utils.registerUploadFile(mContext, makeZipFileInfo);
        }
    }

    private void loadFont()
    {
        fontBlack7Bold		= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 7, Font.BOLD, BaseColor.BLACK);
        fontBlack10Bold		= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 10, Font.BOLD, BaseColor.BLACK);
        fontBlack7Normal		= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 7, Font.NORMAL, BaseColor.BLACK);
    }

    public String getOrderNo(){
        String strOrderNo = "";
        String strPattern = Utils.getStringSetting(mContext, Global.PREF_INVOICE_NUM_FORMAT);
        String strKK = Utils.getStringSetting(mContext, Global.PREF_KK_VALUE);

        int nPdfSequenceNo = Utils.getIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_PDF_SEQUENCE_NO, Global.gTripNumber)) + 1;
        Utils.setIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_PDF_SEQUENCE_NO, Global.gTripNumber), nPdfSequenceNo);
        String strRRR = Global.gRouteNumber;
        Date tripDate = Utils.getRoutectlTripDate(mContext, new Date());
        String strY = new SimpleDateFormat("yyyy").format(tripDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(tripDate);
        int nDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        String strJJJ = String.format(Locale.ENGLISH, "%d", nDayOfYear);
        String strSSS = String.format(Locale.ENGLISH, "%d", nPdfSequenceNo);

        strOrderNo = "";

        int nRIdx = 0;
        int nYIdx = 0;
        int nJIdx = 0;
        int nSIdx = 0;
        int nKIdx = 0;

        int nIdx;
        for(int i = strPattern.length() - 1; i >=0; i--)
        {
            char c = strPattern.charAt(i);
            if( c == 'R' )
            {
                nIdx = strRRR.length() - nRIdx - 1;
                if( nIdx >= 0 )
                {
                    strOrderNo = strRRR.substring(nIdx, nIdx + 1) + strOrderNo;
                }
                else
                {
                    strOrderNo = "0" + strOrderNo;
                }
                nRIdx++;
            }
            else if( c == 'Y' )
            {
                nIdx = strY.length() - nYIdx - 1;
                if( nIdx >= 0 )
                {
                    strOrderNo = strY.substring(nIdx, nIdx + 1) + strOrderNo ;
                }
                else
                {
                    strOrderNo = "0" + strOrderNo;
                }
                nYIdx++;
            }
            else if( c == 'J' )
            {
                nIdx = strJJJ.length() - nJIdx - 1;
                if( nIdx >= 0 )
                {
                    strOrderNo = strJJJ.substring(nIdx, nIdx + 1) + strOrderNo;
                }
                else
                {
                    strOrderNo = "0" + strOrderNo;
                }
                nJIdx++;
            }
            else if( c == 'S' )
            {
                nIdx = strSSS.length() - nSIdx - 1;
                if( nIdx >= 0 )
                {
                    strOrderNo = strSSS.substring(nIdx, nIdx + 1) + strOrderNo;
                }
                else
                {
                    strOrderNo = "0" + strOrderNo;
                }
                nSIdx++;
            }
            else if( c == 'K')
            {
                nIdx = strKK.length() - nKIdx - 1;
                if( nIdx >= 0 )
                {
                    strOrderNo = strKK.substring(nIdx, nIdx + 1) + strOrderNo;
                }
                else
                {
                    strOrderNo = "0" + strOrderNo;
                }
                nKIdx++;
            }
        }
        long lastDocNo = Utils.getLongSetting(mContext, Global.PREF_LAST_DOC_NO);
        if(lastDocNo > 0)
        {
            try {
                long curDocNo = Utils.parseLongFromString(strOrderNo);
                if(curDocNo < lastDocNo)
                {
                    strOrderNo = String.valueOf(lastDocNo);
                    lastDocNo++;
                    Utils.setLongSetting(mContext, Global.PREF_LAST_DOC_NO, lastDocNo);
                }
            }catch (Exception e)
            {

            }
        }
        return strOrderNo;
    }

    public String getPDFFileName() {
        String strFileName = getOrderNo() + ".pdf";
        return strFileName;
    }

    private void createPDF() {
        try {
            Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
            mPdfTrxnNo = System.currentTimeMillis();
            mTrxnDate = new Date(mPdfTrxnNo);
            mStrPdfFileName = getPDFFileName();
            int nHeight = 250 + 17 * mTruckCnt;
            if (mContainerCnt > 0) {
                nHeight += 17 * (mContainerCnt + 1);
            }

            Document document = new Document(new Rectangle(0, 0, 140, nHeight), 0, 0, 0, 0);
            PdfWriter.getInstance(document, new FileOutputStream(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName));
            document.open();
            PdfPTable table = createHeader();
            document.add(table);
            table = createBody();
            table.setSpacingBefore(5);
            document.add(table);
            table = createFooter();
            table.setSpacingBefore(5);
            document.add(table);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PdfPTable createHeader() {
        PdfPTable table = new PdfPTable(1);
        PdfPCell cell;
        Phrase phrase;
        try {
            table.setWidths(new int[]{1});
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            if (Utils.isFileExist(Global.DEFAULT_COMPANY_LOGO_FILEPATH) == true) {
                Image image = Image.getInstance(Global.DEFAULT_COMPANY_LOGO_FILEPATH);
                image.scaleToFit(120, 60);
                cell = new PdfPCell(image);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            } else {
                phrase = new Phrase("No Logo File", fontBlack10Bold);
                cell = new PdfPCell(phrase);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_CENTER);
            }
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            phrase = new Phrase("LOAD SHEET", fontBlack10Bold);
            cell = new PdfPCell(phrase);
            cell.setPaddingTop(5);
            cell.setColspan(2);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            phrase = new Phrase(String.format(Locale.ENGLISH, "Date	   %s", new SimpleDateFormat(Utils.getDateFormat(mContext, "dd/MM/yyyy")+ " hh:mm aa").format(mTrxnDate)), fontBlack7Normal);
            cell = new PdfPCell(phrase);
            cell.setPaddingTop(5);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            phrase = new Phrase(String.format(Locale.ENGLISH, "Trip#	  %s", Global.gTripNumber), fontBlack7Normal);
            table.addCell(phrase);

            phrase = new Phrase(String.format(Locale.ENGLISH, "Location # %s", Utils.getStringSetting(mContext, Global.PREF_DEF_LOCN_NO)), fontBlack7Normal);
            table.addCell(phrase);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return table;
    }

    private PdfPTable createBody() {
        PdfPTable table = new PdfPTable(3);
        PdfPCell cell;
        Phrase phrase;
        try {
            table.setWidths(new int[]{3, 4, 2});
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            phrase = new Phrase("Item", fontBlack7Normal);
            cell = new PdfPCell(phrase);
            cell.setColspan(2);
            cell.setBorder(Rectangle.BOTTOM);
            table.addCell(cell);
            phrase = new Phrase("Qty", fontBlack7Normal);
            cell = new PdfPCell(phrase);
            cell.setBorder(Rectangle.BOTTOM);
            table.addCell(cell);

            int nTotal = 0;
            double dTotal = 0;
            for (UInvenDInfo info : mUInvenDList) {
                if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
                {
                    double dValue = info.dAdjQty;

                    if (/*info.bShow == true &&*/ info.nSCWType == Global.INVENTORY_TRUCK && dValue > 0) {
                        phrase = new Phrase(Utils.getDisplayItemCode(mContext, info.strItemNo), fontBlack7Normal);
                        cell = new PdfPCell(phrase);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cell.setBorder(Rectangle.NO_BORDER);
                        table.addCell(cell);
                        phrase = new Phrase(info.strShortDesc, fontBlack7Normal);
                        cell = new PdfPCell(phrase);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cell.setBorder(Rectangle.NO_BORDER);
                        table.addCell(cell);
                        phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", dValue), fontBlack7Normal);
                        cell = new PdfPCell(phrase);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cell.setBorder(Rectangle.NO_BORDER);
                        table.addCell(cell);
                        dTotal += dValue;
                    }
                }
                else
                {
                    int nValue = info.nAdjQty;

                    if (/*info.bShow == true &&*/ info.nSCWType == Global.INVENTORY_TRUCK && nValue > 0) {
                        phrase = new Phrase(Utils.getDisplayItemCode(mContext, info.strItemNo), fontBlack7Normal);
                        cell = new PdfPCell(phrase);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cell.setBorder(Rectangle.NO_BORDER);
                        table.addCell(cell);
                        phrase = new Phrase(info.strShortDesc, fontBlack7Normal);
                        cell = new PdfPCell(phrase);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cell.setBorder(Rectangle.NO_BORDER);
                        table.addCell(cell);
                        phrase = new Phrase(String.format(Locale.ENGLISH, "%d", nValue), fontBlack7Normal);
                        cell = new PdfPCell(phrase);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cell.setBorder(Rectangle.NO_BORDER);
                        table.addCell(cell);
                        nTotal += nValue;
                    }
                }

            }
            if (mContainerCnt > 0) {
                phrase = new Phrase("Containers", fontBlack7Normal);
                cell = new PdfPCell(phrase);
                cell.setPaddingTop(5);
                cell.setColspan(2);
                cell.setBorder(Rectangle.BOTTOM);
                table.addCell(cell);
                phrase = new Phrase("Qty", fontBlack7Normal);
                cell = new PdfPCell(phrase);
                cell.setPaddingTop(5);
                cell.setBorder(Rectangle.BOTTOM);
                table.addCell(cell);
                for (UInvenDInfo info : mUInvenDList) {
                    if (/*info.bShow == true &&*/ info.nSCWType == Global.INVENTORY_CONTAINERS && info.nStock > 0) {
                        phrase = new Phrase(Utils.getDisplayItemCode(mContext, info.strItemNo), fontBlack7Normal);
                        cell = new PdfPCell(phrase);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cell.setBorder(Rectangle.NO_BORDER);
                        table.addCell(cell);
                        phrase = new Phrase(info.strShortDesc, fontBlack7Normal);
                        cell = new PdfPCell(phrase);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cell.setBorder(Rectangle.NO_BORDER);
                        table.addCell(cell);
                        phrase = new Phrase(String.format(Locale.ENGLISH, "%d", info.nStock), fontBlack7Normal);
                        cell = new PdfPCell(phrase);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cell.setBorder(Rectangle.NO_BORDER);
                        table.addCell(cell);
                        nTotal += info.nStock;
                    }
                }
            }
            phrase = new Phrase("Total", fontBlack7Bold);
            cell = new PdfPCell(phrase);
            cell.setColspan(2);
            cell.setBorder(Rectangle.TOP);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
            if(dTotal > 0 && nTotal > 0)
            {
                phrase = new Phrase(String.format(Locale.ENGLISH, "%d\n%.2f Kg", nTotal, dTotal), fontBlack7Normal);
            }
            else if(dTotal > 0)
            {
                phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f Kg", nTotal, dTotal), fontBlack7Normal);
            }
            else
            {
                phrase = new Phrase(String.format(Locale.ENGLISH, "%d", nTotal), fontBlack7Normal);
            }

            cell = new PdfPCell(phrase);
            cell.setBorder(Rectangle.TOP);
            table.addCell(cell);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return table;
    }

    private PdfPTable createFooter() {
        PdfPTable table = new PdfPTable(1);
        PdfPCell cell;
        Phrase phrase;
        try {
            table.setWidths(new int[]{1});
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            phrase = new Phrase("No Signature File", fontBlack10Bold);
            cell = new PdfPCell(phrase);

            cell.setPaddingTop(5);
            cell.setPaddingBottom(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.TOP);
            table.addCell(cell);

            phrase = new Phrase("Picked Up By:", fontBlack7Normal);
            cell = new PdfPCell(phrase);
            cell.setPaddingTop(5);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            phrase = new Phrase(Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION), fontBlack7Normal);
            table.addCell(phrase);

            phrase = new Phrase("Load Document#", fontBlack7Normal);
            cell = new PdfPCell(phrase);
            cell.setPaddingTop(5);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            phrase = new Phrase("" + mPdfTrxnNo, fontBlack7Normal);
            table.addCell(phrase);

            phrase = new Phrase("Printed: " + new SimpleDateFormat(Utils.getDateFormat(mContext, "dd/MM/yyyy") + "hh:mm aa").format(new Date()), fontBlack7Normal);
            cell = new PdfPCell(phrase);
            cell.setPaddingTop(5);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            phrase = new Phrase("-------- END OF REPORT --------", fontBlack7Normal);
            cell = new PdfPCell(phrase);
            cell.setColspan(2);
            cell.setPaddingTop(5);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return table;
    }

}
