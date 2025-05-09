package dsd.dsdvansales.global;

import static com.github.alexzhirkevich.customqrgenerator.QrCodeGeneratorKt.QrCodeGenerator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.alexzhirkevich.customqrgenerator.QrCodeGenerator;
import com.github.alexzhirkevich.customqrgenerator.QrData;
import com.github.alexzhirkevich.customqrgenerator.QrErrorCorrectionLevel;
import com.github.alexzhirkevich.customqrgenerator.QrOptions;
import com.github.alexzhirkevich.customqrgenerator.ThreadPolicy;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPRow;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.languages.ArabicLigaturizer;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.model.CustConBalInfo;
import dsd.dsdvansales.model.CustomerAddInfo;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.DocTextInfo;
import dsd.dsdvansales.model.LoadBlotInfo;
import dsd.dsdvansales.model.OrderLineInfo;
import dsd.dsdvansales.model.PresoldOrDInfo;
import dsd.dsdvansales.model.PriceGrpInfo;
import dsd.dsdvansales.model.PrintTaxRateInfo;
import dsd.dsdvansales.model.ProMultiInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdInfo;
import dsd.dsdvansales.model.PromAssInfo;
import dsd.dsdvansales.model.PromHeadInfo;
import dsd.dsdvansales.model.SurvQuesInfo;
import dsd.dsdvansales.model.TaxRatesInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UCollectionsDInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;
import dsd.dsdvansales.model.UOrderDInfo;
import dsd.dsdvansales.model.UPromotionInfo;
import dsd.dsdvansales.model.UTaxInfo;
import kotlin.coroutines.Continuation;

/**
 * Created by silve on 7/17/2016.
 */
public class PrintEngine {
    public Map<String, Object> mObjectMap = new HashMap<>();
    public Map<String, Object> mArrayObjectMap = new HashMap<>();
    public static String mTemplatePath = Global.ROOT_LOCATION;

    private Map<String, ProdInfo> mProdInfoMap = new HashMap<>();

    public PrintEngine()
    {

    }

    public void prepareSurvey(Context context, List<SurvQuesInfo> questionList, List<String> answerList, Map<Integer, String> photoMap, String strSurveyTitle, long nTrxnNo, String strDocNo)
    {
        mObjectMap.clear();
        mArrayObjectMap.clear();

        mObjectMap.put("logo", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/Cmpylogo.bmp");
        String strDateTime = Utils.getRoutectlTripDateTime(context, Global.DATE_DD_MM_YYYY_FORMAT, " hh:mm aa", new Date());
        mObjectMap.put("TrxDate", strDateTime);
        mObjectMap.put("TripNo", Global.gTripNumber);
        mObjectMap.put("TrxTime", Utils.getStringSetting(context, Global.PREF_DEF_LOCN_NO));
        mObjectMap.put("UserName", Global.gCurCustomerInfo.strName);
        mObjectMap.put("RouteNo", Global.gRouteNumber);
        mObjectMap.put("TrxNo", String.valueOf(nTrxnNo));
        mObjectMap.put("DocNo", strDocNo);
        mObjectMap.put("SurveyNameAndCustomerName", strSurveyTitle);

        List<Map<String, String>> surveyListParams = new ArrayList<>();
        for(int i = 0; i < questionList.size(); i++)
        {
            Map<String, String> params = new HashMap<>();

            params.put("question", questionList.get(i).strQuestionText);
            if(questionList.get(i).strQuestionType.equals(Global.QUESTION_TYPE_PHOTOGRAPH) || questionList.get(i).strQuestionType.equals(Global.QUESTION_TYPE_SIGNATURE))
            {
                String strPhotoPath = photoMap.get(questionList.get(i).nLookupNo);
                params.put("image", strPhotoPath);
            }
            else if(questionList.get(i).strQuestionType.equals(Global.QUESTION_TYPE_DATE))
            {
                String strDate = Utils.convertTimeFormat(answerList.get(i), "yyyyMMdd", Utils.getDateFormat(context, Global.DATE_DD_MM_YYYY_FORMAT));
                params.put("answer", strDate);
            }
            else {
                params.put("answer", answerList.get(i));
            }
            surveyListParams.add(params);
        }
        mObjectMap.put("Questions", surveyListParams);
    }

    public void prepareNewEquipDoc(Context context, Map<String, String> params)
    {
        mObjectMap.clear();
        mArrayObjectMap.clear();

        mObjectMap.put("logo", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/Cmpylogo.bmp");

        DbAdapter dbAdapter = new DbAdapter(context);
        dbAdapter.open();
        List<DocTextInfo> mDocTextList = new ArrayList<>();
        for(int i = 6; i <= 7; i++)
        {
            List<DocTextInfo> docTextList = dbAdapter.getDocTextList(Global.gCurCustomerInfo.nCustNo,
                    Global.gCurCustomerInfo.nChainNo, i);
            if( docTextList == null || docTextList.size() == 0 )
            {
                docTextList = dbAdapter.getDocTextList(Global.gCurCustomerInfo.nCustNo, i);
            }
            if( docTextList == null || docTextList.size() == 0 )
            {
                docTextList = dbAdapter.getDocTextList(i);
            }
            if( docTextList != null && docTextList.size() > 0)
            {
                mDocTextList.addAll(docTextList);
            }
        }
        dbAdapter.close();
        List<String> headerDocText = new ArrayList<>();
        for(DocTextInfo info : mDocTextList )
        {
            if( info.nTextType == 6 )
            {
                if(info.strDocText.length() == 0)
                {
                    headerDocText.add(" ");
                }
                else
                {
                    headerDocText.add(info.strDocText);
                }
            }
        }
        mObjectMap.put("HeaderDocText", headerDocText);

        try {
            Date date = new Date();
            String strDate = Utils.getRoutectlTripDate(context, "dd/MM/yy", date);
            mObjectMap.put("Date", strDate);
        } catch (Exception e) {
            e.printStackTrace();
            mObjectMap.put("Date", " ");
        }

        mObjectMap.putAll(params);
    }
    public void PrepareCollectionPrint(Context context, List<UCollectionsDInfo> collectionsDInfoList, double dTotalRequired , Date mTrxnDate)
    {
        mObjectMap.clear();
        mArrayObjectMap.clear();

        mObjectMap.put("logo", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/Cmpylogo.bmp");
        String strDateTime = Utils.getRoutectlTripDateTime(context, "dd/MM/yyyy", " hh:mm aa", mTrxnDate);
        mObjectMap.put("CollectionDate", String.format(Locale.ENGLISH, "Date	   %s", strDateTime));
        mObjectMap.put("CollectionTripNo", String.format(Locale.ENGLISH, "Trip #	  %s", Global.gTripNumber));

        List<Map<String, String>> chequeList = new ArrayList<>();
        List<Map<String, String>> cardList = new ArrayList<>();
        List<Map<String, String>> cashList = new ArrayList<>();

        float nChequeTotal = 0.0f;
        float nCardTotal = 0.0f;
        float nCashTotal = 0.0f;
        float nTotal = 0.0f;
        float nTotalCollected = 0.0f;
        for(UCollectionsDInfo collectionsDInfo : collectionsDInfoList)
        {
            Map<String, String> params = new HashMap<>();
            String _date = params.put("date", Utils.getRoutectlTripDate(context, "yyyyMMdd", "dd/MM/yy", collectionsDInfo.strTrxnDate));
            if (_date != null && !_date.isEmpty()) {
                params.put("date", _date);
            }
            else {
                params.put("date", " ");
            }

            params.put("customer", collectionsDInfo.strCustomerName);
            params.put("amount", Global.gCurrencySymbol + String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", collectionsDInfo.nTrxnAmount));

            if(collectionsDInfo.nPayType == Global.COLLECTION_CASH)
            {
                nCashTotal += collectionsDInfo.nTrxnAmount;
                cashList.add(params);
            }
            else if(collectionsDInfo.nPayType == Global.COLLECTION_CARD)
            {
                nCardTotal += collectionsDInfo.nTrxnAmount;
                cardList.add(params);
            }
            else if(collectionsDInfo.nPayType == Global.COLLECTION_CHEQUE)
            {
                nChequeTotal += collectionsDInfo.nTrxnAmount;
                chequeList.add(params);
            }
            nTotalCollected += collectionsDInfo.nTrxnAmount;
        }

        mObjectMap.put("Cheque", chequeList);
        mObjectMap.put("Card", cardList);
        mObjectMap.put("Cash", cashList);
        mObjectMap.put("CardAmount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", nCardTotal));
        mObjectMap.put("CashAmount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", nCashTotal));
        mObjectMap.put("ChequeAmount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", nChequeTotal));
        List<Map<String, String>> creditMapList = new ArrayList<>();
        Map<String, String> creditMap = new HashMap<>();
        if (Utils.getStringSetting(context, Global.PREF_CREDIT_IN_EOD_COLLECTION).equals("1")) {
            nTotal = (float)dTotalRequired;

            float nCreditAmount = nTotal - nTotalCollected;
            creditMap.put("credit_total", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", nCreditAmount));
            creditMapList.add(creditMap);
        }
        else {
            nTotal = nTotalCollected;
        }
        mObjectMap.put("Credit", creditMapList);
        mObjectMap.put("CollectionAmount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", nTotalCollected));
        mObjectMap.put("Total", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", nTotal));
    }

    public void PrepareBankingPrint(Context context, List<UCollectionsDInfo> collectionsDInfoList, double dTotalRequired, long nTrxnNo)
    {
        mObjectMap.clear();
        mArrayObjectMap.clear();

        mObjectMap.put("logo", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/Cmpylogo.bmp");
        mObjectMap.put("TripNumber", "Trip Number : " + Global.gTripNumber);

        mObjectMap.put("Date", "Date : " + Utils.getRoutectlTripDate(context, "dd/MM/yyyy", new Date()));
        mObjectMap.put("Time", "Time : " + new SimpleDateFormat("kk:mm").format(new Date()));
        mObjectMap.put("UserName", "User Name : " + Utils.getStringSetting(context, Global.PREF_USER_NAME));
        mObjectMap.put("RouteNumber", "Route # : " + Global.gRouteNumber);
        mObjectMap.put("Vehicle", "Vehicle # : ");
        mObjectMap.put("Transaction", "Transaction # : " + String.valueOf(nTrxnNo));

        List<Map<String, String>> chequeList = new ArrayList<>();
        List<Map<String, String>> cardList = new ArrayList<>();
        List<Map<String, String>> cashList = new ArrayList<>();

        float nChequeTotal = 0.0f;
        float nCardTotal = 0.0f;
        float nCashTotal = 0.0f;
        float nTotalCollected = 0.0f;
        float nTotal = 0.0f;

        for(UCollectionsDInfo collectionsDInfo : collectionsDInfoList)
        {
            Map<String, String> params = new HashMap<>();
            params.put("name", collectionsDInfo.strCustomerName);
            params.put("price", Global.gCurrencySymbol + String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", collectionsDInfo.nTrxnAmount));

            if(collectionsDInfo.nPayType == Global.COLLECTION_CASH)
            {
                nCashTotal += collectionsDInfo.nTrxnAmount;
                cashList.add(params);
            }
            else if(collectionsDInfo.nPayType == Global.COLLECTION_CARD)
            {
                nCardTotal += collectionsDInfo.nTrxnAmount;
                cardList.add(params);
            }
            else if(collectionsDInfo.nPayType == Global.COLLECTION_CHEQUE)
            {
                nChequeTotal += collectionsDInfo.nTrxnAmount;
                params.put("invoice", "Invoice  " + collectionsDInfo.strInvoiceNo);
                params.put("payment_chq", "Payment Chq#  " + collectionsDInfo.strCheckNo);
                chequeList.add(params);
            }
            nTotalCollected += collectionsDInfo.nTrxnAmount;
        }

        mObjectMap.put("Cheque", chequeList);
        mObjectMap.put("Card", cardList);
        mObjectMap.put("Cash", cashList);
        mObjectMap.put("CardTotal", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", nCardTotal));
        mObjectMap.put("CashTotal", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", nCashTotal));
        mObjectMap.put("ChequeTotal", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", nChequeTotal));

        List<Map<String, String>> creditMapList = new ArrayList<>();
        Map<String, String> creditMap = new HashMap<>();
        if (Utils.getStringSetting(context, Global.PREF_CREDIT_IN_EOD_COLLECTION).equals("1")) {
            nTotal = (float)dTotalRequired;

            float nCreditAmount = nTotal - nTotalCollected;
            creditMap.put("credit_total", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", nCreditAmount));
            creditMapList.add(creditMap);
        }
        else {
            nTotal = nTotalCollected;
        }
        mObjectMap.put("Credit", creditMapList);
        mObjectMap.put("Total", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", nTotal));
    }

    public void PrepareCashPrint(Context context, List<String> strPhotoPathList)
    {
        mObjectMap.clear();
        mArrayObjectMap.clear();
        mObjectMap.put("logo", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/Cmpylogo.bmp");

        if(strPhotoPathList != null && strPhotoPathList.size() > 0)
        {
            mObjectMap.put("Photograph", Utils.decodeFile(strPhotoPathList.get(0), 240 * 240));
            List<String> photos = new ArrayList<>();

            int i = 0;
            for(String strPhoto : strPhotoPathList)
            {
                String strNewPath = Global.UPLOAD_DIR + System.currentTimeMillis() + i + ".jpg";
                Utils.decodeFile(strPhoto, strNewPath, 240 * 240);
                photos.add(strNewPath);
                i++;
            }
            mObjectMap.put("PhotographList", photos);
        }

        DbAdapter dbAdapter = new DbAdapter(context);
        dbAdapter.open();
        List<DocTextInfo> mDocTextList = new ArrayList<>();
        for(int i = 6; i <= 7; i++)
        {
            List<DocTextInfo> docTextList = dbAdapter.getDocTextList(Global.gCurCustomerInfo.nCustNo,
                    Global.gCurCustomerInfo.nChainNo, i);
            if( docTextList == null || docTextList.size() == 0 )
            {
                docTextList = dbAdapter.getDocTextList(Global.gCurCustomerInfo.nCustNo, i);
            }
            if( docTextList == null || docTextList.size() == 0 )
            {
                docTextList = dbAdapter.getDocTextList(i);
            }
            if( docTextList != null && docTextList.size() > 0)
            {
                mDocTextList.addAll(docTextList);
            }
        }
        dbAdapter.close();
        List<String> headerDocText = new ArrayList<>();
        for(DocTextInfo info : mDocTextList )
        {
            if( info.nTextType == 6 )
            {
                if(info.strDocText.length() == 0)
                {
                    headerDocText.add(" ");
                }
                else
                {
                    headerDocText.add(info.strDocText);
                }
            }
        }
        mObjectMap.put("HeaderDocText", headerDocText);

        if (Utils.getStringSetting(context, Global.PREF_ROUTE_NUMBER_HAS_SUFFIX).equals("1")) {
            dbAdapter.open();
            List<DescTypeInfo> invoiceNoSuffixInfoList = dbAdapter.getDescTypeListWithNumericKey("RouteNumber", Utils.getIntFromString(Global.gRouteNumber));
            String strInvoiceNoSuffix = "";
            if(invoiceNoSuffixInfoList != null && invoiceNoSuffixInfoList.size() > 0)
            {
                strInvoiceNoSuffix = invoiceNoSuffixInfoList.get(0).strValue1;
            }
            dbAdapter.close();
            mObjectMap.put("InvoiceNo", Global.gUARTotalInfo.strDocNo+strInvoiceNoSuffix);
        }
        else {
            mObjectMap.put("InvoiceNo", Global.gUARTotalInfo.strDocNo);
        }

        mObjectMap.put("InvoiceNoWithNoLeading", Global.gUARTotalInfo.strDocNo.replaceFirst("^0+(?!$)", ""));
        //Cash Date
        String date = Utils.getRoutectlTripDate(context, Global.DATE_DD_MM_YYYY_FORMAT, new Date());
        mObjectMap.put("CashDate", date.length() > 0 ? date: " ");

        //Time
        SimpleDateFormat tdf = new SimpleDateFormat(Global.TIME_HH_MM_FORMAT);
        String time = tdf.format(new Date());
        mObjectMap.put("Time", time.length() > 0 ? time: " ");

        //InvoiceCustomerCode
        if( Global.gCurCustomerInfo.strAltCustNo.equals("") )
        {
            if( Global.gCurCustomerInfo.nChainNo == 0 )
            {
                mObjectMap.put("InvoiceCustomerCode", String.valueOf(Global.gCurCustomerInfo.nCustNo));
            }
            else
            {
                mObjectMap.put("InvoiceCustomerCode", Global.gCurCustomerInfo.nChainNo + "/" + Global.gCurCustomerInfo.nCustNo);
            }
        }
        else
        {
            mObjectMap.put("InvoiceCustomerCode", Global.gCurCustomerInfo.strAltCustNo);
        }

        //VS182 -- 8/14, 2019 From Andrew.
        mObjectMap.put("CustNoOnly", String.valueOf(Global.gCurCustomerInfo.nCustNo));

        if( Global.gCurCustomerInfo.nChainNo == 0 )
        {
            mObjectMap.put("CustNo", String.valueOf(Global.gCurCustomerInfo.nCustNo));
        }
        else
        {
            mObjectMap.put("CustNo", Global.gCurCustomerInfo.nChainNo + "/" + Global.gCurCustomerInfo.nCustNo);
        }

        mObjectMap.put("AltCustNo", Global.gCurCustomerInfo.strAltCustNo); //--VS182

        //InvoiceTo1
        mObjectMap.put("InvoiceTo1", Global.gCurCustomerInfo.strName.length() > 0 ? Global.gCurCustomerInfo.strName : " ");
        mObjectMap.put("InvoiceTo2", Global.gCurCustomerInfo.strAddress1.length() > 0 ? Global.gCurCustomerInfo.strAddress1 : " ");

        mObjectMap.put("InvoiceTo3", Global.gCurCustomerInfo.strCity + " " + Global.gCurCustomerInfo.strShipToState + " " + Global.gCurCustomerInfo.strShipToZip);

        mObjectMap.put("DeliverAccount", Global.gCurCustomerInfo.strBillName);

        //

        List<Map<String, String>> paymentList = new ArrayList<>();
        if( Global.gUARTotalInfo.nTotalCash > 0 )
        {
            {
                Map<String, String> itemMap = new HashMap<>();
                itemMap.put("first", "Cash");
                itemMap.put("second", " ");
                itemMap.put("third", " ");
                paymentList.add(itemMap);
            }

            {
                Map<String, String> itemMap = new HashMap<>();
                itemMap.put("first", " ");
                itemMap.put("second", " ");
                itemMap.put("third", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Global.gUARTotalInfo.nTotalCash));
                paymentList.add(itemMap);
            }
        }
        if( Global.gUARPaymentChequeInfo.nTrxnAmount > 0 )
        {
            {
                Map<String, String> itemMap = new HashMap<>();
                itemMap.put("first", "Cheque");
                itemMap.put("second", " ");
                itemMap.put("third", " ");
                paymentList.add(itemMap);
            }
            {
                Map<String, String> itemMap = new HashMap<>();
                itemMap.put("first", "Bank BSB#");
                itemMap.put("second", Global.gUARPaymentChequeInfo.strBankNo);
                itemMap.put("third", " ");
                paymentList.add(itemMap);
            }
            {
                Map<String, String> itemMap = new HashMap<>();
                itemMap.put("first", "Account #");
                itemMap.put("second", Global.gUARPaymentChequeInfo.strAccount);
                itemMap.put("third", " ");
                paymentList.add(itemMap);
            }
            {
                Map<String, String> itemMap = new HashMap<>();
                itemMap.put("first", "Cheque #");
                itemMap.put("second", Global.gUARPaymentChequeInfo.strCheckNo);
                itemMap.put("third", " ");
                paymentList.add(itemMap);
            }

            {
                Map<String, String> itemMap = new HashMap<>();
                itemMap.put("first", " ");
                itemMap.put("second", " ");
                itemMap.put("third", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Global.gUARPaymentChequeInfo.nTrxnAmount));
                paymentList.add(itemMap);
            }
        }
        if(Global.gUARPaymentCardInfo.nTrxnAmount > 0)
        {
            {
                Map<String, String> itemMap = new HashMap<>();
                itemMap.put("first", "Card");
                itemMap.put("second", " ");
                itemMap.put("third", " ");
                paymentList.add(itemMap);
            }
            {
                Map<String, String> itemMap = new HashMap<>();
                itemMap.put("first", " ");
                itemMap.put("second", "Reference Number");
                itemMap.put("third", Global.gUARPaymentCardInfo.strInvoiceNo);
                paymentList.add(itemMap);
            }
            {
                Map<String, String> itemMap = new HashMap<>();
                itemMap.put("first", " ");
                itemMap.put("second", "AR Notes");
                itemMap.put("third", Global.gUARPaymentCardInfo.strARNotes);
                paymentList.add(itemMap);
            }
            {
                Map<String, String> itemMap = new HashMap<>();
                itemMap.put("first", " ");
                itemMap.put("second", " ");
                itemMap.put("third", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Global.gUARPaymentCardInfo.nTrxnAmount));

                paymentList.add(itemMap);
            }
        }
        if( Global.gUARPaymentEFTInfo.nTrxnAmount > 0 )
        {
            {
                Map<String, String> itemMap = new HashMap<>();
                itemMap.put("first", "EFT");
                itemMap.put("second", " ");
                itemMap.put("third", " ");
                paymentList.add(itemMap);
            }
            {
                Map<String, String> itemMap = new HashMap<>();
                itemMap.put("first", " ");
                itemMap.put("second", "Reference Number");
                itemMap.put("third", Global.gUARPaymentEFTInfo.strInvoiceNo);
                paymentList.add(itemMap);
            }
            {
                Map<String, String> itemMap = new HashMap<>();
                itemMap.put("first", " ");
                itemMap.put("second", " ");
                itemMap.put("third", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Global.gUARPaymentEFTInfo.nTrxnAmount));
                paymentList.add(itemMap);
            }
        }

        mObjectMap.put("Payment", paymentList);
        mObjectMap.put("Total", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Global.gUARPaymentChequeInfo.nTrxnAmount + Global.gUARTotalInfo.nTotalCash + Global.gUARPaymentCardInfo.nTrxnAmount + Global.gUARPaymentEFTInfo.nTrxnAmount));

        List<String> footerDocText = new ArrayList<>();
        for(DocTextInfo info : mDocTextList )
        {
            if( info.nTextType == 7 )
            {
                if(info.strDocText.length() == 0)
                {
                    footerDocText.add(" ");
                }
                else
                {
                    footerDocText.add(info.strDocText);
                }
            }
        }
        mObjectMap.put("FooterDocText", footerDocText);

    }

    public void PrepareConsignmentInventoryPrint(Context context, List<CustConBalInfo> custConBalInfoList, Date date, String strDocNo, String strSignatureFileName, String strReceiverName)
    {
        mObjectMap.clear();
        mArrayObjectMap.clear();
        mObjectMap.put("logo", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/Cmpylogo.bmp");
        if(!TextUtils.isEmpty(strSignatureFileName))
        {
            mObjectMap.put("Sign", strSignatureFileName);
        }

        String strDateTime = Utils.getRoutectlTripDateTime(context, "dd/MM/yyyy", " hh:mm aa", date);
        mObjectMap.put("Date", String.format(Locale.ENGLISH, "Date	   %s", strDateTime));
        mObjectMap.put("TripNo", String.format(Locale.ENGLISH, "Trip #	  %s", Global.gTripNumber));
        mObjectMap.put("Location", String.format(Locale.ENGLISH, "Location # %s", Utils.getStringSetting(context, Global.PREF_DEF_LOCN_NO)));
        mObjectMap.put("CustomerName", String.format(Locale.ENGLISH, "Customer Name # %s", Global.gCurCustomerInfo.strName));
        if(Global.gCurCustomerInfo.nChainNo != 0)
        {
            mObjectMap.put("CustomerNumber", String.format(Locale.ENGLISH, "Customer Number # %s", Global.gCurCustomerInfo.nCustNo + "/" + Global.gCurCustomerInfo.nChainNo));
        }
        else
        {
            mObjectMap.put("CustomerNumber", String.format(Locale.ENGLISH, "Customer Number # %s", String.valueOf(Global.gCurCustomerInfo.nCustNo)));
        }

        int nTotal = 0;
        float dTotal = 0;
        List<Map<String, String>> loadMapList = new ArrayList<>();
        DbAdapter dbAdapter = new DbAdapter(context);
        dbAdapter.open();
        for(CustConBalInfo info : custConBalInfoList)
        {
            Map<String, String> loadMap = new HashMap<>();
            loadMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
            List<ProdDetlInfo> prodDetInfos =  dbAdapter.getProductList(info.strItemNo);
            String strDesc;
            if( prodDetInfos != null && prodDetInfos.size() > 0)
            {
                strDesc = prodDetInfos.get(0).strShortDesc;
            }
            else
            {
                strDesc = "";
            }
            loadMap.put("description", strDesc);

            if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
            {
                loadMap.put("qty", String.format(Locale.ENGLISH, "%.2f", info.fWeight));
                dTotal += info.fWeight;
            }
            else
            {
                loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nQty));
                nTotal += info.nQty;
            }

            loadMap.put("rrp", Global.gCurrencySymbol + String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nPrice));
            loadMapList.add(loadMap);
        }
        dbAdapter.close();
        mObjectMap.put("Load", loadMapList);

        if(dTotal > 0 && (nTotal > 0))
        {
            mObjectMap.put("Total", String.format(Locale.ENGLISH, "%d\n%.2f Kg", nTotal, dTotal));
        }
        else if( dTotal > 0)
        {
            mObjectMap.put("Total", String.format(Locale.ENGLISH, "%.2f Kg", dTotal));
        }
        else
        {
            mObjectMap.put("Total", String.format(Locale.ENGLISH, "%d", nTotal));
        }
        mObjectMap.put("SalesRepresentative", Utils.getStringSetting(context, Global.PREF_USER_NAME));
        mObjectMap.put("PickedUpBy", Utils.getStringSetting(context, Global.PREF_USER_DESCRIPTION));
        mObjectMap.put("DocumentNo", strDocNo);
        mObjectMap.put("CheckedReceivedBy", "Checked & received by: " + strReceiverName);
        strDateTime = Utils.getRoutectlTripDateTime(context, "dd/MM/yyyy", " hh:mm aa", new Date());
        mObjectMap.put("PrintedDate", "Printed: " +  strDateTime);
    }

    public void PrepareConsignmentPrint(Context context, List<UOrderDInfo> uOrderDInfoList, String strSignaturePath, int nPayType, Map<String, PriceGrpInfo> priceGrpInfoMap, Map<String, PriceGrpInfo> altPriceGrpInfoMap)
    {
        mObjectMap.clear();
        mArrayObjectMap.clear();
        mObjectMap.put("logo", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/Cmpylogo.bmp");
        mObjectMap.put("logo_docket", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/CmpylogoDocket.bmp");
        mObjectMap.put("Sign", strSignaturePath);

        DbAdapter dbAdapter = new DbAdapter(context);
        dbAdapter.open();
        List<DocTextInfo> mDocTextList = new ArrayList<>();
        for(int i = 6; i <= 7; i++)
        {
            List<DocTextInfo> docTextList = dbAdapter.getDocTextList(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo, i);
            if( docTextList == null || docTextList.size() == 0 )
            {
                docTextList = dbAdapter.getDocTextList(Global.gCurCustomerInfo.nCustNo, i);
            }
            if( docTextList == null || docTextList.size() == 0 )
            {
                docTextList = dbAdapter.getDocTextList(i);
            }
            if( docTextList != null && docTextList.size() > 0)
            {
                mDocTextList.addAll(docTextList);
            }
        }
        dbAdapter.close();

        List<String> headerDocText = new ArrayList<>();
        for(DocTextInfo info : mDocTextList )
        {
            if( info.nTextType == 6 )
            {
                if(info.strDocText.length() == 0)
                {
                    headerDocText.add(" ");
                }
                else
                {
                    headerDocText.add(info.strDocText);
                }
            }
        }
        mObjectMap.put("HeaderDocText", headerDocText);

        if (Utils.getStringSetting(context, Global.PREF_ROUTE_NUMBER_HAS_SUFFIX).equals("1")) {
            dbAdapter.open();
            List<DescTypeInfo> invoiceNoSuffixInfoList = dbAdapter.getDescTypeListWithNumericKey("RouteNumber", Utils.getIntFromString(Global.gRouteNumber));
            String strInvoiceNoSuffix = "";
            if(invoiceNoSuffixInfoList != null && invoiceNoSuffixInfoList.size() > 0)
            {
                strInvoiceNoSuffix = invoiceNoSuffixInfoList.get(0).strValue1;
            }
            dbAdapter.close();
            mObjectMap.put("InvoiceNo", Global.gUOrderHInfo.strDocNo.length() > 0 ? Global.gUOrderHInfo.strDocNo+strInvoiceNoSuffix : " ");
        }
        else {
            mObjectMap.put("InvoiceNo", Global.gUOrderHInfo.strDocNo.length() > 0 ? Global.gUOrderHInfo.strDocNo : " ");
        }

        mObjectMap.put("InvoiceNoWithNoLeading", Global.gUOrderHInfo.strDocNo.length() > 0 ? Global.gUOrderHInfo.strDocNo.replaceFirst("^0+(?!$)", "") : " ");
        mObjectMap.put("SalesRepresentative", Utils.getStringSetting(context, Global.PREF_USER_NAME));

        //Invoice Date
        String date = Utils.getRoutectlTripDate(context, Global.DATE_YYYYMMDD_FORMAT, Global.DATE_DD_MM_YYYY_FORMAT, Global.gUOrderHInfo.strTrxnDate);
        mObjectMap.put("InvoiceDate", date.length() > 0 ? date: " ");

        SimpleDateFormat df = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT);
        SimpleDateFormat tdf = new SimpleDateFormat(Global.TIME_HH_MM_FORMAT);
        String time = "";
        try{
            time = tdf.format((df.parse(Global.gUOrderHInfo.strTrxnTime)));
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        //Invoice Time
        mObjectMap.put("Time", time.length() > 0 ? time : " ");

        //Terms
        mObjectMap.put("Terms", Global.gCurCustomerInfo.strTerms.length() > 0 ? Global.gCurCustomerInfo.strTerms : " ");

        //InvoiceCustomerCode
        if( Global.gCurCustomerInfo.strAltCustNo.equals("") )
        {
            if( Global.gCurCustomerInfo.nChainNo == 0 )
            {
                mObjectMap.put("InvoiceCustomerCode", String.valueOf(Global.gCurCustomerInfo.nCustNo));
            }
            else
            {
                mObjectMap.put("InvoiceCustomerCode", Global.gCurCustomerInfo.nChainNo + "/" + Global.gCurCustomerInfo.nCustNo);
            }
        }
        else
        {
            mObjectMap.put("InvoiceCustomerCode", String.valueOf(Global.gCurCustomerInfo.strAltCustNo));
        }

        //VS182 -- 8/14, 2019 From Andrew.
        mObjectMap.put("CustNoOnly", String.valueOf(Global.gCurCustomerInfo.nCustNo));

        if( Global.gCurCustomerInfo.nChainNo == 0 )
        {
            mObjectMap.put("CustNo", String.valueOf(Global.gCurCustomerInfo.nCustNo));
        }
        else
        {
            mObjectMap.put("CustNo", Global.gCurCustomerInfo.nChainNo + "/" + Global.gCurCustomerInfo.nCustNo);
        }

        mObjectMap.put("AltCustNo", Global.gCurCustomerInfo.strAltCustNo); //--VS182

        //InvoiceTo1
        mObjectMap.put("InvoiceTo1", Global.gCurCustomerInfo.strName.length() > 0 ? Global.gCurCustomerInfo.strName : " ");

        String strAddr = Global.gCurCustomerInfo.strAddress1.length() > 0 ? Global.gCurCustomerInfo.strAddress1 : " ";

        mObjectMap.put("InvoiceTo2", strAddr);

        strAddr = Global.gCurCustomerInfo.strAddress2.length() > 0 ? Global.gCurCustomerInfo.strAddress2 : "";
        mObjectMap.put("InvoiceTo4", strAddr);

        mObjectMap.put("InvoiceTo3", Global.gCurCustomerInfo.strCity + " " + Global.gCurCustomerInfo.strShipToState + " " + Global.gCurCustomerInfo.strShipToZip);

        mObjectMap.put("DeliverAccount", Global.gCurCustomerInfo.strBillName);

        if(Global.gCurCustomerInfo.nBillCustNo != 0)
        {
            mObjectMap.put("AccountTo", "Account To");
            if( Global.gCurCustomerInfo.nBillChainNo == 0 )
            {
                mObjectMap.put("BillCustomerCode", String.valueOf(Global.gCurCustomerInfo.nBillCustNo));
            }
            else
            {
                mObjectMap.put("BillCustomerCode", Global.gCurCustomerInfo.nBillChainNo + "/" + Global.gCurCustomerInfo.nBillCustNo);
            }

            //InvoiceTo1
            mObjectMap.put("BillInvoiceTo1", Global.gCurCustomerInfo.strBillName.length() > 0 ? Global.gCurCustomerInfo.strBillName : " ");

            strAddr = Global.gCurCustomerInfo.strBillAddr1.length() > 0 ? Global.gCurCustomerInfo.strBillAddr1 : " ";

            mObjectMap.put("BillInvoiceTo2", strAddr);

            strAddr = Global.gCurCustomerInfo.strBillAddr2.length() > 0 ? Global.gCurCustomerInfo.strBillAddr2 : "";
            if(!TextUtils.isEmpty(strAddr))
            {
                mObjectMap.put("BillInvoiceTo4", strAddr);
            }

            mObjectMap.put("BillInvoiceTo3", Global.gCurCustomerInfo.strBillCity + " " + Global.gCurCustomerInfo.strState + " " + Global.gCurCustomerInfo.strZip);
        }
        else
        {
            mObjectMap.put("AccountTo", " ");
            mObjectMap.put("BillInvoiceTo1", " ");
            mObjectMap.put("BillInvoiceTo2", " ");
            mObjectMap.put("BillInvoiceTo3", " ");
            mObjectMap.put("BillInvoiceTo4", " ");
        }

        if(Global.gUOrderHInfo != null && !TextUtils.isEmpty(Global.gUOrderHInfo.strPOReference))
        {
            mObjectMap.put("PONo", Global.gUOrderHInfo.strPOReference.length() > 0 ? Global.gUOrderHInfo.strPOReference : " ");
        }

        if(Global.gCurCustomerInfo.strStoreNo.length() > 0)
        {
            mObjectMap.put("StoreNo", Global.gCurCustomerInfo.strStoreNo);
        }
        else
        {
            mObjectMap.put("StoreNo", " ");
        }

        if(Global.gCurCustomerInfo.strVendorNo.length() > 0)
        {
            mObjectMap.put("VendorNo", Global.gCurCustomerInfo.strVendorNo);
        }
        else
        {
            mObjectMap.put("VendorNo", " ");
        }

        if(Global.gCurCustomerInfo.strTerms.length() > 0)
        {
            dbAdapter = new DbAdapter(context);
            dbAdapter.open();
            List<DescTypeInfo> paymentTypeDescTypeList = dbAdapter.getDescTypeListWithAlphaKey(Global.TERMS, Global.gCurCustomerInfo.strTerms);
            dbAdapter.close();
            if(paymentTypeDescTypeList.size() != 0)
            {
                String strTerms = "";
                for(DescTypeInfo descTypeInfo : paymentTypeDescTypeList)
                {
                    if(strTerms.length() > 0)
                    {
                        strTerms += "\n";
                    }
                    strTerms += descTypeInfo.strDescription.trim();
                }
                mObjectMap.put("BillTerms", strTerms);
            }
            else
            {
                mObjectMap.put("BillTerms", " ");
            }

        }
        else
        {
            mObjectMap.put("BillTerms", " ");
        }

        mObjectMap.put("RouteNo", Global.gRouteNumber);

        Collections.sort(uOrderDInfoList, new Comparator<UOrderDInfo>() {
            @Override
            public int compare(UOrderDInfo t1, UOrderDInfo t2) {
                return t1.strItemNo.compareTo(t2.strItemNo);
            }
        });

        List<Map<String, String>> deliveryMapList = new ArrayList<>();
        float totalInvoiceValue = 0;
        float totalGSTValue = 0;
        int nTotalStartingQty = 0;
        int nTotalCountQty = 0;
        int nTotalSoldQty = 0;

        float fTotalStartingQty = 0;
        float fTotalCountQty = 0;
        float fTotalSoldQty = 0;

        boolean bExistWeightItem = false, bExistNormalItem = false;
        for(UOrderDInfo info : uOrderDInfoList)
        {
            Map<String, String> deliveryMap = new HashMap<>();
            deliveryMap.put("code", Utils.getDisplayItemCode(context, info.strItemNo));
            deliveryMap.put("description", info.strDescription);
            deliveryMap.put("short_desc", info.strShortDesc);

            PriceGrpInfo priceGrpInfo = priceGrpInfoMap.get(info.strItemNo);
            if(priceGrpInfo == null)
            {
                priceGrpInfo = altPriceGrpInfoMap.get(info.strItemNo);
            }

            if(priceGrpInfo != null)
            {
                deliveryMap.put("price_grp", String.format(Locale.ENGLISH, "%.2f%%", Utils.getDividedValue(priceGrpInfo.nPrice)));
            }
            else
            {
                deliveryMap.put("price_grp", " ");
            }

            if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
            {
                if(info.dPlanQty == 0 && info.dSales == 0) continue;
                deliveryMap.put("starting_qty", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.dPlanQty));
                deliveryMap.put("count_qty", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.dSales));
                deliveryMap.put("sold_qty", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.dPlanQty - info.dSales));
                fTotalStartingQty += Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.dPlanQty));
                fTotalCountQty += Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.dSales));
                fTotalSoldQty += Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.dPlanQty - info.dSales));

                deliveryMap.put("retail_price", Global.gCurrencySymbol + String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice));
                float discount = (info.nBasePrice - info.nPrice) / info.nBasePrice * 100;
                float real_disc = 100 - Utils.getFloatFromString(String.format(Locale.ENGLISH, "%.2f", discount));
                float price_inc_gst = Utils.getFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                //float totalWithTax = (float)(price_inc_gst * real_disc / 100.0f * (info.dPlanQty - info.dSales));
                float totalWithTax = Utils.getRoundedValue((info.dPlanQty - info.dSales)* info.nPrice) + info.nTaxAmount;
                deliveryMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                deliveryMap.put("discount", String.format(Locale.ENGLISH, "%.2f%%", discount));
                deliveryMap.put("totalWithTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalWithTax)));
                float gst = Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nTaxAmount));
                totalInvoiceValue += totalWithTax - gst;
                totalGSTValue += gst;
                bExistWeightItem = true;
            }
            else
            {
                if(info.nPlanQty == 0 && info.nSales == 0) continue;
                deliveryMap.put("starting_qty", String.format(Locale.ENGLISH, "%d", info.nPlanQty));
                deliveryMap.put("count_qty", String.format(Locale.ENGLISH, "%d", info.nSales));
                deliveryMap.put("sold_qty", String.format(Locale.ENGLISH, "%d", info.nPlanQty - info.nSales));

                nTotalStartingQty += info.nPlanQty;
                nTotalCountQty += info.nSales;
                nTotalSoldQty += (info.nPlanQty - info.nSales);

                deliveryMap.put("retail_price", Global.gCurrencySymbol + String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice));
                float discount = (info.nBasePrice - info.nPrice) / info.nBasePrice * 100;
                float real_disc = 100 - Utils.getFloatFromString(String.format(Locale.ENGLISH, "%.2f", discount));
                float price_inc_gst = Utils.getFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                //float totalWithTax = (float)(price_inc_gst * real_disc / 100.0f * (info.nPlanQty - info.nSales));
                float totalWithTax = Utils.getRoundedValue((info.nPlanQty - info.nSales)* info.nPrice) + info.nTaxAmount;
                deliveryMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                deliveryMap.put("discount", String.format(Locale.ENGLISH, "%.2f%%", discount));
                deliveryMap.put("totalWithTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalWithTax)));
                float gst = Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nTaxAmount));
                totalInvoiceValue += totalWithTax - gst;
                totalGSTValue += gst;
                bExistNormalItem = true;
            }
            deliveryMapList.add(deliveryMap);
        }
        mObjectMap.put("Delivery", deliveryMapList);

        if(bExistWeightItem && bExistNormalItem)
        {
            mObjectMap.put("StartingTotalQty", nTotalStartingQty + "\n" + String.format(Locale.ENGLISH, "%.2fKG", fTotalStartingQty));
            mObjectMap.put("CountTotalQty", nTotalCountQty + "\n" + String.format(Locale.ENGLISH, "%.2fKG", fTotalCountQty));
            mObjectMap.put("SoldTotalQty", nTotalSoldQty + "\n" + String.format(Locale.ENGLISH, "%.2fKG", fTotalSoldQty));
        }
        else if(bExistNormalItem)
        {
            mObjectMap.put("StartingTotalQty", String.valueOf(nTotalStartingQty));
            mObjectMap.put("CountTotalQty", String.valueOf(nTotalCountQty));
            mObjectMap.put("SoldTotalQty", String.valueOf(nTotalSoldQty));
        }
        else if(bExistWeightItem)
        {
            mObjectMap.put("StartingTotalQty", String.format(Locale.ENGLISH, "%.2fKG", fTotalStartingQty));
            mObjectMap.put("CountTotalQty", String.format(Locale.ENGLISH, "%.2fKG", fTotalCountQty));
            mObjectMap.put("SoldTotalQty", String.format(Locale.ENGLISH, "%.2fKG", fTotalSoldQty));
        }
        else
        {
            mObjectMap.put("StartingTotalQty", "0");
            mObjectMap.put("CountTotalQty", "0");
            mObjectMap.put("SoldTotalQty", "0");
        }
        mObjectMap.put("TotalInvoice", Global.gCurrencySymbol + String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", totalInvoiceValue));
        mObjectMap.put("GST", Global.gCurrencySymbol + String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", totalGSTValue));
        mObjectMap.put("TotalInvoiceGST", Global.gCurrencySymbol + String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", totalInvoiceValue + totalGSTValue));

        mObjectMap.put("CheckedReceivedBy", "Checked & received by: " + Global.gUOrderHInfo.strOrderName);
        if( nPayType == 0 )
        {
            mObjectMap.put("TotalPaid", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", 0f));
            mObjectMap.put("TotalDue", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", totalInvoiceValue + totalGSTValue));
            Global.gCurRealPaymentAmount = 0;
        }
        else
        {
            mObjectMap.put("TotalPaid", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", totalInvoiceValue + totalGSTValue));
            mObjectMap.put("TotalDue", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", 0f));
        }


        if(!TextUtils.isEmpty(Global.gCurCustomerInfo.strAltCustNo))
        {
            mObjectMap.put("PaymentReference", Global.gCurCustomerInfo.strAltCustNo);
        }
        else
        {
            mObjectMap.put("PaymentReference", String.valueOf(Global.gCurCustomerInfo.nCustNo));
        }

        List<String> footerDocText = new ArrayList<>();
        for(DocTextInfo info : mDocTextList )
        {
            if( info.nTextType == 7 && (TextUtils.isEmpty(info.strSubType) || info.strSubType.equals("P")))
            {
                if(info.strDocText.length() == 0)
                {
                    footerDocText.add(" ");
                }
                else
                {
                    footerDocText.add(info.strDocText);
                }
            }
        }
        mObjectMap.put("FooterDocText", footerDocText);
    }

    public void PrepareOffLoadPrint(Context context, UInvenHInfo uInvenHInfo)
    {
        mObjectMap.put("logo", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/Cmpylogo.bmp");
        String strDateTime = Utils.getRoutectlTripDateTime(context, "dd/MM/yyyy", " hh:mm aa", new Date());
        mObjectMap.put("LoadSheetDate", String.format(Locale.ENGLISH, "Date	   %s", strDateTime));
        mObjectMap.put("LoadSheetTripNo", String.format(Locale.ENGLISH, "Trip #	  %s", Global.gTripNumber));
        mObjectMap.put("LoadSheetLocation", String.format(Locale.ENGLISH, "Location # %s", Utils.getStringSetting(context, Global.PREF_DEF_LOCN_NO)));
        List<UInvenDInfo> uFreshInfoList = new ArrayList<>();
        List<UInvenDInfo> uReturnsInfoList = new ArrayList<>();
        Map<Integer, List<UInvenDInfo>> spoilsMap = new HashMap<>();
        DbAdapter dbAdapter = new DbAdapter(context);
        dbAdapter.open();
        List<DescTypeInfo> descTypeList = dbAdapter.getDescTypeList("SPOILSRSN");
        dbAdapter.close();

        for(UInvenDInfo uInvenDInfo : uInvenHInfo.uInvenDList)
        {
            if(uInvenDInfo.nDumpsUnload != 0 || uInvenDInfo.dDumpsUnload != 0)
            {
                uReturnsInfoList.add(uInvenDInfo);
            }
            if(uInvenDInfo.nUnload != 0 || uInvenDInfo.dUnload != 0)
            {
                uFreshInfoList.add(uInvenDInfo);
            }
            if(uInvenDInfo.nSpoils != 0 || uInvenDInfo.dSpoils != 0)
            {
                List<UInvenDInfo> uSpoilsList = spoilsMap.get(uInvenDInfo.nReasonCode);
                if(uSpoilsList == null)
                {
                    uSpoilsList = new ArrayList<>();
                    spoilsMap.put(uInvenDInfo.nReasonCode, uSpoilsList);
                }
                uSpoilsList.add(uInvenDInfo);
            }
        }
        boolean bShowCase = Utils.getStringSetting(context, Global.PREF_INVENTORYUOM).equals("U") == false;

        if(uFreshInfoList.size() > 0)
        {
            List<Map<String, String>> freshMapList = new ArrayList<>();
            for(UInvenDInfo info : uFreshInfoList)
            {
                Map<String, String> loadMap = new HashMap<>();
                loadMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
                {
                    String strBatchCode = "";
                    for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList)
                    {
                        if(TextUtils.isEmpty(strBatchCode))
                        {
                            strBatchCode = loadBlotInfo.strLotNumber;
                        }
                        else
                        {
                            strBatchCode += ", " + loadBlotInfo.strLotNumber;
                        }
                    }
                    if(TextUtils.isEmpty(info.strBatchCode))
                    {
                        loadMap.put("description", info.strShortDesc + "\n" + strBatchCode);
                    }
                }
                else if(!TextUtils.isEmpty(info.strBatchCode))
                {
                    loadMap.put("description", info.strShortDesc + "\n" + info.strBatchCode);
                }
                else
                {
                    loadMap.put("description", info.strShortDesc);
                }
                if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
                {
                    loadMap.put("qty", String.format(Locale.ENGLISH, "%.2f", info.dUnload));
                    loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%.2f", info.dUnload));
                    loadMap.put("qty_un", String.format(Locale.ENGLISH, "%.2f", info.dUnload));
                    freshMapList.add(loadMap);
                }
                else
                {
                    if(bShowCase)
                    {
                        if (Utils.isContainerItem(context, info.strItemNo)) {
                            loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nUnload));
                            loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nUnload));
                            loadMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nUnload));
                        }
                        else {
                            int nCase = getCaseValue(info.strItemNo, context);
                            loadMap.put("qty", String.format(Locale.ENGLISH, "%d/%d", info.nUnload / nCase, info.nUnload % nCase));
                            loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d/%d", info.nUnload / nCase, info.nUnload % nCase));
                            loadMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nUnload));
                        }
                        freshMapList.add(loadMap);
                    }
                    else
                    {
                        loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nUnload));
                        loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nUnload));
                        loadMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nUnload));
                        freshMapList.add(loadMap);
                    }
                }

            }
            if( freshMapList.size() > 0 )
            {
                mObjectMap.put("FreshLoad", freshMapList);
            }
        }

        if(uReturnsInfoList.size() > 0)
        {
            List<Map<String, String>> returnsMapList = new ArrayList<>();
            for(UInvenDInfo info : uReturnsInfoList)
            {
                Map<String, String> loadMap = new HashMap<>();
                loadMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
                {
                    String strBatchCode = "";
                    for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList)
                    {
                        if(TextUtils.isEmpty(strBatchCode))
                        {
                            strBatchCode = loadBlotInfo.strLotNumber;
                        }
                        else
                        {
                            strBatchCode += ", " + loadBlotInfo.strLotNumber;
                        }
                    }
                    if(TextUtils.isEmpty(info.strBatchCode))
                    {
                        loadMap.put("description", info.strShortDesc + "\n" + strBatchCode);
                    }
                }
                else if(!TextUtils.isEmpty(info.strBatchCode))
                {
                    loadMap.put("description", info.strShortDesc + "\n" + info.strBatchCode);
                }
                else
                {
                    loadMap.put("description", info.strShortDesc);
                }
                if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
                {
                    double dDumpsUnload;
                    if( info.dDumpsUnload > info.nDumps )
                    {
                        dDumpsUnload = info.dDumps;
                    }
                    else
                    {
                        dDumpsUnload = info.dDumpsUnload;
                    }
                    if(dDumpsUnload > 0)
                    {
                        loadMap.put("qty", String.format(Locale.ENGLISH, "%.2f", dDumpsUnload));
                        loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%.2f", dDumpsUnload));
                        loadMap.put("qty_un", String.format(Locale.ENGLISH, "%.2f", dDumpsUnload));
                        returnsMapList.add(loadMap);
                    }

                }
                else
                {
                    int nDumpsUnload;

                    if(info.nDumpsUnload > info.nDumps)
                    {
                        nDumpsUnload = info.nDumps;
                    }
                    else
                    {
                        nDumpsUnload = info.nDumpsUnload;
                    }

                    if(nDumpsUnload > 0)
                    {
                        if(bShowCase)
                        {
                            if (Utils.isContainerItem(context, info.strItemNo)) {
                                loadMap.put("qty", String.format(Locale.ENGLISH, "%d", nDumpsUnload));
                                loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", nDumpsUnload));
                                loadMap.put("qty_un", String.format(Locale.ENGLISH, "%d", nDumpsUnload));
                            }
                            else {
                                int nCase = getCaseValue(info.strItemNo, context);
                                loadMap.put("qty", String.format(Locale.ENGLISH, "%d/%d", nDumpsUnload / nCase, nDumpsUnload % nCase));
                                loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d/%d", nDumpsUnload / nCase, nDumpsUnload % nCase));
                                loadMap.put("qty_un", String.format(Locale.ENGLISH, "%d", nDumpsUnload));
                            }
                            returnsMapList.add(loadMap);
                        }
                        else
                        {
                            loadMap.put("qty", String.format(Locale.ENGLISH, "%d", nDumpsUnload));
                            loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", nDumpsUnload));
                            loadMap.put("qty_un", String.format(Locale.ENGLISH, "%d", nDumpsUnload));
                            returnsMapList.add(loadMap);
                        }
                    }
                }
            }
            if( returnsMapList.size() > 0 )
            {
                mObjectMap.put("Returns", returnsMapList);
            }
        }

        if(spoilsMap.size() > 0)
        {
            List<Map<String, String>> spoilsMapList = new ArrayList<>();
            Iterator it = spoilsMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                Integer nReasonCode = (Integer)pair.getKey();
                {
                    Map<String, String> loadMap = new HashMap<>();
                    for(int i = 0; i < descTypeList.size(); i++){
                        if(descTypeList.get(i).nNumericKey == nReasonCode)
                        {
                            loadMap.put("title", descTypeList.get(i).strDescription);
                            break;
                        }
                    }
                    spoilsMapList.add(loadMap);
                }

                List<UInvenDInfo> uInvenDInfoSpoilList = (List<UInvenDInfo>)pair.getValue();
                for(UInvenDInfo info : uInvenDInfoSpoilList)
                {
                    Map<String, String> loadMap = new HashMap<>();
                    loadMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                    if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
                    {
                        String strBatchCode = "";
                        for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList)
                        {
                            if(TextUtils.isEmpty(strBatchCode))
                            {
                                strBatchCode = loadBlotInfo.strLotNumber;
                            }
                            else
                            {
                                strBatchCode += ", " + loadBlotInfo.strLotNumber;
                            }
                        }
                        if(TextUtils.isEmpty(info.strBatchCode))
                        {
                            loadMap.put("description", info.strShortDesc + "\n" + strBatchCode);
                        }
                    }
                    else if(!TextUtils.isEmpty(info.strBatchCode))
                    {
                        loadMap.put("description", info.strShortDesc + "\n" + info.strBatchCode);
                    }
                    else
                    {
                        loadMap.put("description", info.strShortDesc);
                    }
                    if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
                    {
                        loadMap.put("qty", String.format(Locale.ENGLISH, "%.2f", info.dSpoils));
                        loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%.2f", info.dSpoils));
                        loadMap.put("qty_un", String.format(Locale.ENGLISH, "%.2f", info.dSpoils));
                        spoilsMapList.add(loadMap);
                    }
                    else
                    {
                        if(bShowCase)
                        {
                            if (Utils.isContainerItem(context, info.strItemNo)) {
                                loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nSpoils));
                                loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nSpoils));
                                loadMap.put("qty_un", String.format(Locale.ENGLISH, "%d", (info.nSpoils)));
                            }
                            else {
                                int nCase = getCaseValue(info.strItemNo, context);
                                loadMap.put("qty", String.format(Locale.ENGLISH, "%d/%d", (info.nSpoils) / nCase, (info.nSpoils) % nCase));
                                loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d/%d", (info.nSpoils) / nCase, (info.nSpoils) % nCase));
                                loadMap.put("qty_un", String.format(Locale.ENGLISH, "%d", (info.nSpoils)));
                            }
                            spoilsMapList.add(loadMap);
                        }
                        else
                        {
                            loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nSpoils));
                            loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nSpoils));
                            loadMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nSpoils));
                            spoilsMapList.add(loadMap);
                        }
                    }
                }

                it.remove(); // avoids a ConcurrentModificationException
            }

            if( spoilsMapList.size() > 0 )
            {
                mObjectMap.put("Spoils", spoilsMapList);
            }
        }
    }

    public void PrepareTermsPrint(Context context, File file, String strName)
    {
        mObjectMap.clear();
        mArrayObjectMap.clear();
        mObjectMap.put("logo", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/Cmpylogo.bmp");
        mObjectMap.put("Sign", file.getAbsolutePath());
        DbAdapter dbAdapter = new DbAdapter(context);
        dbAdapter.open();


        List<DocTextInfo> docTextInfoList = dbAdapter.getDocTextList(6);
        List<String> headerDocText = new ArrayList<>();
        for(DocTextInfo info : docTextInfoList )
        {
            if(info.strDocText.length() == 0)
            {
                headerDocText.add(" ");
            }
            else {
                headerDocText.add(info.strDocText);
            }
        }
        mObjectMap.put("HeaderDocText", headerDocText);

        dbAdapter.close();

        String strDate = Utils.getRoutectlTripDate(context, Global.DATE_DD_MM_YYYY_FORMAT, new Date(System.currentTimeMillis()));
        mObjectMap.put("PrintedName", strName);
        mObjectMap.put("SignedDate", strDate);
        mObjectMap.put("BusinessName", "Business Name: " + Global.gCurCustomerInfo.strName);
        mObjectMap.put("BusinessAddress", "Business Address: " + Global.gCurCustomerInfo.strAddress1 + " "  + Global.gCurCustomerInfo.strAddress2);
        mObjectMap.put("OnDate", "On date: " + strDate);
        mObjectMap.put("Refrigerator", "For provision of refrigerator (please specify model, size): " + Global.gCurEquipmentInfo.strModel + ", " + Global.gCurEquipmentInfo.strDescription);
    }


    public void PreparePrint(Context context, List<UInvenDInfo> mPrintList, List<UInvenDInfo> mContainerList, String strKind,
                             Date mTrxnDate, String strSignatureFilePath, String strPdfDocNumber, String strTitle, String strTransferRoute)
    {
        boolean bShowCase = Utils.getStringSetting(context, Global.PREF_INVENTORYUOM).equals("U") == false;
        mObjectMap.clear();
        mArrayObjectMap.clear();
        mObjectMap.put("logo", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/Cmpylogo.bmp");
        mObjectMap.put("Sign", strSignatureFilePath);
        mObjectMap.put("Header", strTitle);
        String strDateTime = Utils.getRoutectlTripDateTime(context, "dd/MM/yyyy", " hh:mm aa", mTrxnDate);
        mObjectMap.put("LoadSheetDate", String.format(Locale.ENGLISH, "Date	   %s", strDateTime));
        mObjectMap.put("LoadSheetTripNo", String.format(Locale.ENGLISH, "Trip #	  %s", Global.gTripNumber));
        mObjectMap.put("LoadSheetLocation", String.format(Locale.ENGLISH, "Location # %s", Utils.getStringSetting(context, Global.PREF_DEF_LOCN_NO)));
        mObjectMap.put("AdjustmentReason", String.format(Locale.ENGLISH, "Adjustment Reason    %s", strKind));
        if(!TextUtils.isEmpty(strTransferRoute))
        {
            mObjectMap.put("TransferRoute", String.format(Locale.ENGLISH, "Transfer Route    %s", strTransferRoute));
        }
        int nTotal = 0;
        int nTotalCase = 0;
        double dTotal = 0;
        if( mPrintList != null && mPrintList.size() > 0 )
        {
            List<Map<String, String>> loadMapList = new ArrayList<>();
            for (UInvenDInfo info : mPrintList) {
                if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
                {
                    if(info.bShow == true && info.dStock > 0)
                    {
                        Map<String, String> loadMap = new HashMap<>();
                        loadMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                        if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
                        {
                            String strBatchCode = "";
                            for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList)
                            {
                                if(TextUtils.isEmpty(strBatchCode))
                                {
                                    strBatchCode = loadBlotInfo.strLotNumber;
                                }
                                else
                                {
                                    strBatchCode += ", " + loadBlotInfo.strLotNumber;
                                }
                            }
                            if(TextUtils.isEmpty(info.strBatchCode))
                            {
                                loadMap.put("description", info.strShortDesc + "\n" + strBatchCode);
                            }
                        }
                        else if(!TextUtils.isEmpty(info.strBatchCode))
                        {
                            loadMap.put("description", info.strShortDesc + "\n" + info.strBatchCode);
                        }
                        else
                        {
                            loadMap.put("description", info.strShortDesc);
                        }

                        loadMap.put("qty", String.format(Locale.ENGLISH, "%.2f", info.dStock));
                        loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%.2f", info.dStock));
                        loadMap.put("qty_un", String.format(Locale.ENGLISH, "%.2f", info.dStock));
                        loadMapList.add(loadMap);
                        dTotal += info.dStock;
                    }
                }
                else
                {
                    if(info.bShow == true && info.nStock > 0)
                    {
                        if(bShowCase)
                        {
                            Map<String, String> loadMap = new HashMap<>();
                            loadMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                            if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
                            {
                                String strBatchCode = "";
                                for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList)
                                {
                                    if(TextUtils.isEmpty(strBatchCode))
                                    {
                                        strBatchCode = loadBlotInfo.strLotNumber;
                                    }
                                    else
                                    {
                                        strBatchCode += ", " + loadBlotInfo.strLotNumber;
                                    }
                                }
                                if(TextUtils.isEmpty(info.strBatchCode))
                                {
                                    loadMap.put("description", info.strShortDesc + "\n" + strBatchCode);
                                }
                            }
                            else if(!TextUtils.isEmpty(info.strBatchCode))
                            {
                                loadMap.put("description", info.strShortDesc + "\n" + info.strBatchCode);
                            }
                            else
                            {
                                loadMap.put("description", info.strShortDesc);
                            }

                            if (Utils.isContainerItem(context, info.strItemNo)) {
                                loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nStock));
                                loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                loadMap.put("qty_un", String.valueOf(info.nStock));
                                loadMapList.add(loadMap);
                                nTotal += info.nStock;
                            }
                            else {
                                int nCase = getCaseValue(info.strItemNo, context);
                                loadMap.put("qty", String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
                                loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
                                loadMap.put("qty_un", String.valueOf(info.nStock));
                                loadMapList.add(loadMap);
                                nTotal += info.nStock % nCase;
                                nTotalCase += info.nStock / nCase;
                            }
                        }
                        else
                        {
                            Map<String, String> loadMap = new HashMap<>();
                            loadMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                            if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
                            {
                                String strBatchCode = "";
                                for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList)
                                {
                                    if(TextUtils.isEmpty(strBatchCode))
                                    {
                                        strBatchCode = loadBlotInfo.strLotNumber;
                                    }
                                    else
                                    {
                                        strBatchCode += ", " + loadBlotInfo.strLotNumber;
                                    }
                                }
                                if(TextUtils.isEmpty(info.strBatchCode))
                                {
                                    loadMap.put("description", info.strShortDesc + "\n" + strBatchCode);
                                }
                            }
                            else if(!TextUtils.isEmpty(info.strBatchCode))
                            {
                                loadMap.put("description", info.strShortDesc + "\n" + info.strBatchCode);
                            }
                            else
                            {
                                loadMap.put("description", info.strShortDesc);
                            }
                            loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nStock));
                            loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                            loadMap.put("qty_un", String.valueOf(info.nStock));
                            loadMapList.add(loadMap);
                            nTotal += info.nStock;
                        }

                    }
                }

            }
            if( loadMapList.size() > 0 )
            {
                mObjectMap.put("Load", loadMapList);
            }
        }


        if( mContainerList != null && mContainerList.size() > 0 )
        {
            List<Map<String, String>> containerMapList = new ArrayList<>();

            for (UInvenDInfo info : mContainerList) {
                if(/*info.bShow == true &&*/ info.nStock > 0)
                {
                    if(bShowCase)
                    {
                        Map<String, String> containerMap = new HashMap<>();
                        containerMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                        containerMap.put("description", info.strShortDesc);
                        if (Utils.isContainerItem(context, info.strItemNo)) {
                            containerMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nStock));
                            containerMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                            containerMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                            containerMapList.add(containerMap);
                            nTotal += info.nStock;
                        }
                        else {
                            int nCase = getCaseValue(info.strItemNo, context);
                            containerMap.put("qty", String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
                            containerMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
                            containerMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                            containerMapList.add(containerMap);
                            nTotal += info.nStock % nCase;
                            nTotalCase += info.nStock / nCase;
                        }
                    }
                    else
                    {
                        Map<String, String> containerMap = new HashMap<>();
                        containerMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                        containerMap.put("description", info.strShortDesc);
                        containerMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nStock));
                        containerMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                        containerMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                        containerMapList.add(containerMap);
                        nTotal += info.nStock;
                    }

                }
            }
            if( containerMapList.size() > 0 )
            {
                mObjectMap.put("Containers", containerMapList);
            }
        }
        if( dTotal > 0 && (nTotal > 0 || nTotalCase > 0))
        {
            if(bShowCase)
            {
                mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%d/%d\n%.2f Kg", nTotalCase , nTotal, dTotal));
            }
            else
            {
                mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%d\n%.2f Kg", nTotal, dTotal));
            }
        }
        else if(dTotal > 0)
        {
            mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%.2f Kg", dTotal));
        }
        else
        {
            if(bShowCase)
            {
                mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%d/%d", nTotalCase , nTotal));
            }
            else
            {
                mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%d", nTotal));
            }
        }

        mObjectMap.put("PickedUpBy", Utils.getStringSetting(context, Global.PREF_USER_DESCRIPTION));
        mObjectMap.put("DocumentNo", strPdfDocNumber);
        strDateTime = Utils.getRoutectlTripDateTime(context, "dd/MM/yyyy", " hh:mm aa", new Date());
        mObjectMap.put("PrintedDate", "Printed: " + strDateTime);
    }

    public void PreparePrint(Context context, List<UInvenDInfo> mPrintList, List<UInvenDInfo> mContainerList, String strKind,
                             Date mTrxnDate, long pdfTrxnNo, String strSignatureFilePath, String strPdfDocNumber,
                             Date requiredDate, String strSheetDescription, List<String> strPhotoPathList
                            )
    {
        mObjectMap.clear();
        mArrayObjectMap.clear();
        mObjectMap.put("logo", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/Cmpylogo.bmp");
        mObjectMap.put("Sign", strSignatureFilePath);
        boolean bShowCase = Utils.getStringSetting(context, Global.PREF_INVENTORYUOM).equals("U") == false;

        if(strPhotoPathList != null && strPhotoPathList.size() > 0)
        {
            mObjectMap.put("Photograph", Utils.decodeFile(strPhotoPathList.get(0), 240 * 240));
            List<String> photos = new ArrayList<>();

            int i = 0;
            for(String strPhoto : strPhotoPathList)
            {
                String strNewPath = Global.UPLOAD_DIR + System.currentTimeMillis() + i + ".jpg";
                Utils.decodeFile(strPhoto, strNewPath, 240 * 240);
                photos.add(strNewPath);
                i++;
            }
            mObjectMap.put("PhotographList", photos);
        }

        //Sort Print List and Container List by Item Number --- VS158
        if(mPrintList != null)
        {
            mPrintList = new ArrayList<>(mPrintList);
            Collections.sort(mPrintList, new Comparator<UInvenDInfo>() {
                @Override
                public int compare(UInvenDInfo uInvenDInfo, UInvenDInfo t1) {
                    return uInvenDInfo.strItemNo.compareTo(t1.strItemNo);
                }
            });
        }
        if(mContainerList != null)
        {
            mContainerList = new ArrayList<>(mContainerList);
            Collections.sort(mContainerList, new Comparator<UInvenDInfo>() {
                @Override
                public int compare(UInvenDInfo uInvenDInfo, UInvenDInfo t1) {
                    return uInvenDInfo.strItemNo.compareTo(t1.strItemNo);
                }
            });
        }


        if( strKind.equals("Load Sheet") )
        {
            String strDateTime = Utils.getRoutectlTripDateTime(context, "dd/MM/yyyy", " hh:mm aa", mTrxnDate);
            mObjectMap.put("LoadSheetDate", String.format(Locale.ENGLISH, "Date	   %s", strDateTime));
            mObjectMap.put("LoadSheetTripNo", String.format(Locale.ENGLISH, "Trip #	  %s", Global.gTripNumber));
            mObjectMap.put("LoadSheetLocation", String.format(Locale.ENGLISH, "Location # %s", Utils.getStringSetting(context, Global.PREF_DEF_LOCN_NO)));
            mObjectMap.put("LoadDescription", String.format(Locale.ENGLISH, "Load Description    %s", strSheetDescription));

            int nTotal = 0;
            int nTotalCase = 0;
            double dTotal = 0;

            int nAdjTotal = 0;
            int nAdjTotalCase = 0;
            double dAdjTotal = 0;

            int nOrgTotal = 0;
            int nOrgTotalCase = 0;
            double dOrgTotal = 0;
            if(Global.gLoadAdjustmentReasonList != null && Global.gLoadAdjustmentReasonList.size() > 0)
            {
                mObjectMap.put("Adjustments", Global.gLoadAdjustmentReasonList);
            }

            if( mPrintList != null && mPrintList.size() > 0 )
            {
                List<Map<String, String>> loadMapList = new ArrayList<>();
                for (UInvenDInfo info : mPrintList) {
                    if(info.bShow == true) // Fixed by Mr.Malcolm's request -- 2023.7.8 ----DSDVanSales Testing 7.7.7
                    {
                        Map<String, String> loadMap = new HashMap<>();
                        loadMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                        if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
                        {
                            String strBatchCode = "";
                            for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList)
                            {
                                if(TextUtils.isEmpty(strBatchCode))
                                {
                                    strBatchCode = loadBlotInfo.strLotNumber;
                                }
                                else
                                {
                                    strBatchCode += ", " + loadBlotInfo.strLotNumber;
                                }
                            }
                            if(TextUtils.isEmpty(info.strBatchCode))
                            {
                                loadMap.put("description", info.strShortDesc + "\n" + strBatchCode);
                            }
                        }
                        else if(!TextUtils.isEmpty(info.strBatchCode))
                        {
                            loadMap.put("description", info.strShortDesc + "\n" + info.strBatchCode);
                        }
                        else
                        {
                            loadMap.put("description", info.strShortDesc);
                        }

                        if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
                        {
                            loadMap.put("qty", String.format(Locale.ENGLISH, "%.2f", info.dStock));
                            loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%.2f", info.dStock));
                            loadMap.put("qty_un", String.format(Locale.ENGLISH, "%.2f", info.dStock));
                            loadMap.put("org_qty", String.format(Locale.ENGLISH, "%.2f", info.dLoadTruck));
                            loadMap.put("adj_qty", String.format(Locale.ENGLISH, "%.2f", info.dStock - info.dLoaded));
                            loadMap.put("bal_qty", String.format(Locale.ENGLISH, "%.2f", info.dStock + info.dLoadTruck));
                            loadMapList.add(loadMap);
                            dTotal += info.dStock;
                            dAdjTotal += (info.dStock - info.dLoaded);
                            dOrgTotal += info.dLoadTruck;
                        }
                        else
                        {
                            if(bShowCase)
                            {
                                if (Utils.isContainerItem(context, info.strItemNo)) {
                                    loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nStock));
                                    loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                    loadMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                    loadMap.put("org_qty", String.format(Locale.ENGLISH, "%d", info.nLoadTruck));
                                    loadMap.put("adj_qty", String.format(Locale.ENGLISH, "%d", (info.nStock - info.nLoaded)));
                                    loadMap.put("bal_qty", String.format(Locale.ENGLISH, "%d", (info.nStock + info.nLoadTruck)));
                                    loadMapList.add(loadMap);
                                    nTotal += info.nStock;
                                    nAdjTotal += (info.nStock - info.nLoaded);
                                    nOrgTotal += info.nLoadTruck;
                                }
                                else {
                                    int nCase = getCaseValue(info.strItemNo, context);
                                    loadMap.put("qty", String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
                                    loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
                                    loadMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                    loadMap.put("org_qty", String.format(Locale.ENGLISH, "%d/%d", info.nLoadTruck / nCase, info.nLoadTruck % nCase));
                                    loadMap.put("adj_qty", String.format(Locale.ENGLISH, "%d/%d", (info.nStock - info.nLoaded) / nCase, (info.nStock - info.nLoaded) % nCase));
                                    loadMap.put("bal_qty", String.format(Locale.ENGLISH, "%d/%d", (info.nStock + info.nLoadTruck) / nCase, (info.nStock + info.nLoadTruck) % nCase));
                                    loadMapList.add(loadMap);
                                    nTotal += info.nStock % nCase;
                                    nTotalCase += info.nStock / nCase;
                                    nAdjTotal += (info.nStock - info.nLoaded) % nCase;
                                    nAdjTotalCase += (info.nStock - info.nLoaded) / nCase;
                                    nOrgTotal += info.nLoadTruck % nCase;
                                    nOrgTotalCase += info.nLoadTruck / nCase;
                                }
                            }
                            else
                            {
                                loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nStock));
                                loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                loadMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                loadMap.put("org_qty", String.format(Locale.ENGLISH, "%d", info.nLoadTruck));
                                loadMap.put("adj_qty", String.format(Locale.ENGLISH, "%d", info.nStock - info.nLoaded));
                                loadMap.put("bal_qty", String.format(Locale.ENGLISH, "%d", info.nStock + info.nLoadTruck));
                                loadMapList.add(loadMap);
                                nTotal += info.nStock ;
                                nAdjTotal += (info.nStock - info.nLoaded);
                                nOrgTotal += info.nLoadTruck;
                            }

                        }
                    }
                }
                if( loadMapList.size() > 0 )
                {
                    mObjectMap.put("Load", loadMapList);
                }
            }


            if( mContainerList != null && mContainerList.size() > 0 )
            {
                List<Map<String, String>> containerMapList = new ArrayList<>();

                for (UInvenDInfo info : mContainerList) {
                    if(bShowCase)
                    {
                        Map<String, String> containerMap = new HashMap<>();
                        containerMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                        containerMap.put("description", info.strShortDesc);
                        if (Utils.isContainerItem(context, info.strItemNo)) {
                            containerMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nStock));
                            containerMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                            containerMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                            containerMapList.add(containerMap);
                            nTotal += info.nStock;
                        }
                        else {
                            int nCase = getCaseValue(info.strItemNo, context);
                            containerMap.put("qty", String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
                            containerMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
                            containerMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                            containerMapList.add(containerMap);
                            nTotal += info.nStock % nCase;
                            nTotalCase += info.nStock / nCase;
                        }
                    }
                    else
                    {
                        Map<String, String> containerMap = new HashMap<>();
                        containerMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                        containerMap.put("description", info.strShortDesc);
                        int nCase = getCaseValue(info.strItemNo, context);
                        containerMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nStock));
                        containerMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                        containerMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                        containerMapList.add(containerMap);
                        nTotal += info.nStock;
                    }
                }
                if( containerMapList.size() > 0 )
                {
                    mObjectMap.put("Containers", containerMapList);
                }
            }

            if(dTotal > 0 && (nTotal > 0 || nTotalCase > 0))
            {
                if(bShowCase)
                {
                    mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%d/%d\n%.2f Kg", nTotalCase , nTotal, dTotal));
                }
                else
                {
                    mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%d\n%.2f Kg", nTotal, dTotal));
                }
            }
            else if( dTotal > 0)
            {
                mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%.2f Kg", dTotal));
            }
            else
            {
                if(bShowCase)
                {
                    mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%d/%d", nTotalCase , nTotal));
                }
                else
                {
                    mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%d", nTotal));
                }

            }

            if(dOrgTotal > 0 && (nOrgTotal > 0 || nOrgTotalCase > 0))
            {
                if(bShowCase)
                {
                    mObjectMap.put("LoadOrgTotal", String.format(Locale.ENGLISH, "%d/%d\n%.2f Kg", nOrgTotalCase , nOrgTotal, dOrgTotal));
                }
                else
                {
                    mObjectMap.put("LoadOrgTotal", String.format(Locale.ENGLISH, "%d\n%.2f Kg", nOrgTotal, dOrgTotal));
                }
            }
            else if( dOrgTotal > 0)
            {
                mObjectMap.put("LoadOrgTotal", String.format(Locale.ENGLISH, "%.2f Kg", dOrgTotal));
            }
            else
            {
                if(bShowCase)
                {
                    mObjectMap.put("LoadOrgTotal", String.format(Locale.ENGLISH, "%d/%d", nOrgTotalCase , nOrgTotal));
                }
                else
                {
                    mObjectMap.put("LoadOrgTotal", String.format(Locale.ENGLISH, "%d", nOrgTotal));
                }
            }

            if(dAdjTotal > 0 && (nAdjTotal > 0 || nAdjTotalCase > 0))
            {
                if(bShowCase)
                {
                    mObjectMap.put("LoadAdjTotal", String.format(Locale.ENGLISH, "%d/%d\n%.2f Kg", nAdjTotalCase , nAdjTotal, dAdjTotal));
                }
                else
                {
                    mObjectMap.put("LoadAdjTotal", String.format(Locale.ENGLISH, "%d\n%.2f Kg", nAdjTotal, dAdjTotal));
                }
            }
            else if( dAdjTotal > 0)
            {
                mObjectMap.put("LoadAdjTotal", String.format(Locale.ENGLISH, "%.2f Kg", dAdjTotal));
            }
            else
            {
                if(bShowCase)
                {
                    mObjectMap.put("LoadAdjTotal", String.format(Locale.ENGLISH, "%d/%d", nAdjTotalCase , nAdjTotal));
                }
                else
                {
                    mObjectMap.put("LoadAdjTotal", String.format(Locale.ENGLISH, "%d", nAdjTotal));
                }
            }

            mObjectMap.put("PickedUpBy", Utils.getStringSetting(context, Global.PREF_USER_DESCRIPTION));
            mObjectMap.put("DocumentNo", strPdfDocNumber);
            strDateTime = Utils.getRoutectlTripDateTime(context, "dd/MM/yyyy", " hh:mm aa", new Date());
            mObjectMap.put("PrintedDate", "Printed: " + strDateTime);
        } else if( strKind.equals("Load Request") )
        {
            String strDateTime = Utils.getRoutectlTripDateTime(context, "dd/MM/yyyy", " hh:mm aa", mTrxnDate);
            mObjectMap.put("LoadSheetDate", String.format(Locale.ENGLISH, "Date	   %s", strDateTime));
            mObjectMap.put("LoadSheetTripNo", String.format(Locale.ENGLISH, "Trip#	  %s", Global.gTripNumber));
            mObjectMap.put("LoadSheetLocation", String.format(Locale.ENGLISH, "Location # %s", Utils.getStringSetting(context, Global.PREF_DEF_LOCN_NO)));
            mObjectMap.put("LoadRequestFor", String.format(Locale.ENGLISH, "Load Request For %s", new SimpleDateFormat(Utils.getDateFormat(context, "dd/MM/yyyy")).format(requiredDate)));
            mObjectMap.put("LoadRequestUnit", Global.gCurrencySymbol);

            int nTotal = 0;
            int nTotalCase = 0;
            double dTotal = 0;
            double dTotalPrice = 0;
            if( mPrintList != null && mPrintList.size() > 0 )
            {
                List<Map<String, String>> loadMapList = new ArrayList<>();
                for (UInvenDInfo info : mPrintList) {
                    if(/*info.bShow == true &&*/ info.nStock > 0 || info.dStock > 0)
                    {
                        Map<String, String> loadMap = new HashMap<>();
                        loadMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                        loadMap.put("description", info.strShortDesc);
                        if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
                        {
                            loadMap.put("qty", String.format(Locale.ENGLISH, "%.2f", info.dStock));
                            loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%.2f", info.dStock));
                            loadMap.put("qty_un", String.format(Locale.ENGLISH, "%.2f", info.dStock));
                            float driverPrice = Utils.getProductDriverPrice(context, info.strItemNo);
                            loadMap.put("price", String.format(Locale.ENGLISH, "%.2f", info.dStock * driverPrice));

                            loadMapList.add(loadMap);
                            dTotal += info.dStock;
                            dTotalPrice += info.dStock * driverPrice;
                        }
                        else
                        {
                            if(bShowCase)
                            {
                                if (Utils.isContainerItem(context, info.strItemNo)) {
                                    loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nStock));
                                    loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                    loadMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                    float driverPrice = Utils.getProductDriverPrice(context, info.strItemNo);
                                    loadMap.put("price", String.format(Locale.ENGLISH, "%.2f", info.dStock * driverPrice));

                                    loadMapList.add(loadMap);
                                    nTotal += info.nStock;
                                    dTotalPrice += info.nStock * driverPrice;
                                }
                                else {
                                    int nCase = getCaseValue(info.strItemNo, context);
                                    loadMap.put("qty", String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
                                    loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
                                    loadMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                    float driverPrice = Utils.getProductDriverPrice(context, info.strItemNo);
                                    loadMap.put("price", String.format(Locale.ENGLISH, "%.2f", info.nStock * driverPrice));

                                    loadMapList.add(loadMap);
                                    nTotal += info.nStock % nCase;
                                    nTotalCase += info.nStock / nCase;
                                    dTotalPrice += info.nStock * driverPrice;
                                }
                            }
                            else
                            {
                                loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nStock));
                                loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                loadMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                float driverPrice = Utils.getProductDriverPrice(context, info.strItemNo);
                                loadMap.put("price", String.format(Locale.ENGLISH, "%.2f", info.nStock * driverPrice));

                                loadMapList.add(loadMap);
                                nTotal += info.nStock;
                                dTotalPrice += info.nStock * driverPrice;
                            }

                        }

                    }
                }
                if( loadMapList.size() > 0 )
                {
                    mObjectMap.put("Load", loadMapList);
                }
            }


            if( mContainerList != null && mContainerList.size() > 0 )
            {
                List<Map<String, String>> containerMapList = new ArrayList<>();

                for (UInvenDInfo info : mContainerList) {
                    if(/*info.bShow == true &&*/ info.nStock > 0)
                    {
                        if(bShowCase)
                        {
                            Map<String, String> containerMap = new HashMap<>();
                            containerMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                            containerMap.put("description", info.strShortDesc);
                            if (Utils.isContainerItem(context, info.strItemNo)) {
                                containerMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nStock));
                                containerMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                containerMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                containerMapList.add(containerMap);
                                nTotal += info.nStock;
                            }
                            else {
                                int nCase = getCaseValue(info.strItemNo, context);
                                containerMap.put("qty", String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
                                containerMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
                                containerMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                containerMapList.add(containerMap);
                                nTotal += info.nStock % nCase;
                                nTotalCase += info.nStock / nCase;
                            }
                        }
                        else
                        {
                            Map<String, String> containerMap = new HashMap<>();
                            containerMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                            containerMap.put("description", info.strShortDesc);

                            containerMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nStock));
                            containerMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                            containerMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                            containerMapList.add(containerMap);
                            nTotal += info.nStock;
                        }

                    }
                }
                if( containerMapList.size() > 0 )
                {
                    mObjectMap.put("Containers", containerMapList);
                }
            }
            if(dTotal > 0 && (nTotal > 0 || nTotalCase > 0))
            {
                if(bShowCase)
                {
                    mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%d/%d\n%.2f Kg", nTotalCase, nTotal, dTotal));
                }
                else
                {
                    mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%d\n%.2f Kg", nTotal, dTotal));
                }
            }
            else if(dTotal > 0)
            {
                mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%.2f Kg", dTotal));
            }
            else
            {
                if(bShowCase)
                {
                    mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%d/%d", nTotalCase, nTotal));
                }
                else
                {
                    mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%d", nTotal));
                }
            }
            String strLoadTotalPrice = String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(dTotalPrice));
            mObjectMap.put("LoadTotalPrice", strLoadTotalPrice);

            mObjectMap.put("PickedUpBy", Utils.getStringSetting(context, Global.PREF_USER_DESCRIPTION));
            mObjectMap.put("DocumentNo", strPdfDocNumber);
            strDateTime = Utils.getRoutectlTripDateTime(context, "dd/MM/yyyy", " hh:mm aa", new Date());
            mObjectMap.put("PrintedDate", "Printed: " + strDateTime);
        }
        else if( strKind.equals("Stocktake Count") )
        {
            String strDateTime = Utils.getRoutectlTripDateTime(context, "dd/MM/yyyy", " hh:mm aa", mTrxnDate);
            mObjectMap.put("LoadSheetDate", String.format(Locale.ENGLISH, "Date	   %s", strDateTime));
            mObjectMap.put("LoadSheetTripNo", String.format(Locale.ENGLISH, "Trip#	  %s", Global.gTripNumber));
            mObjectMap.put("LoadSheetLocation", String.format(Locale.ENGLISH, "Location # %s", Utils.getStringSetting(context, Global.PREF_DEF_LOCN_NO)));
            mObjectMap.put("StocktakeCountFor", String.format(Locale.ENGLISH, "Stocktake For %s", new SimpleDateFormat(Utils.getDateFormat(context, "dd/MM/yyyy")).format(requiredDate)));
            int nTotal = 0;
            int nTotalCase = 0;
            double dTotal = 0;
            if( mPrintList != null && mPrintList.size() > 0 )
            {
                List<Map<String, String>> loadMapList = new ArrayList<>();
                for (UInvenDInfo info : mPrintList) {
                    Map<String, String> loadMap = new HashMap<>();
                    loadMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                    //loadMap.put("description", info.strShortDesc);
                    if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
                    {
                        String strBatchCode = "";
                        for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList)
                        {
                            if(TextUtils.isEmpty(strBatchCode))
                            {
                                strBatchCode = loadBlotInfo.strLotNumber;
                            }
                            else
                            {
                                strBatchCode += ", " + loadBlotInfo.strLotNumber;
                            }
                        }
                        if(TextUtils.isEmpty(info.strBatchCode))
                        {
                            loadMap.put("description", info.strShortDesc + "\n" + strBatchCode);
                        }
                    }
                    else if(!TextUtils.isEmpty(info.strBatchCode))
                    {
                        loadMap.put("description", info.strShortDesc + "\n" + info.strBatchCode);
                    }
                    else
                    {
                        loadMap.put("description", info.strShortDesc);
                    }
                    if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
                    {
                        loadMap.put("qty", String.format(Locale.ENGLISH, "%.2f", info.dStock));
                        loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%.2f", info.dStock));
                        loadMap.put("qty_un", String.format(Locale.ENGLISH, "%.2f", info.dStock));
                        loadMapList.add(loadMap);
                        dTotal += info.dStock;
                    }
                    else
                    {
                        if(bShowCase)
                        {
                            if (Utils.isContainerItem(context, info.strItemNo)) {
                                loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nStock));
                                loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                loadMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                loadMapList.add(loadMap);
                                nTotal += info.nStock;
                            }
                            else {
                                int nCase = getCaseValue(info.strItemNo, context);
                                loadMap.put("qty", String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
                                loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
                                loadMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                loadMapList.add(loadMap);
                                nTotal += info.nStock % nCase;
                                nTotalCase += info.nStock / nCase;
                            }
                        }
                        else
                        {
                            loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nStock));
                            loadMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                            loadMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                            loadMapList.add(loadMap);
                            nTotal += info.nStock;
                        }

                    }

                }
                if( loadMapList.size() > 0 )
                {
                    mObjectMap.put("Load", loadMapList);
                }
            }
            if(dTotal > 0 && (nTotalCase > 0 || nTotal > 0))
            {
                if(bShowCase)
                {
                    mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%d/%d\n%.2f Kg", nTotalCase, nTotal, dTotal));
                }
                else
                {
                    mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%d\n%.2f Kg", nTotal, dTotal));
                }

            }
            else if(dTotal > 0)
            {
                mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%.2f Kg", dTotal));
            }
            else
            {
                if(bShowCase)
                {
                    mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%d/%d", nTotalCase, nTotal));
                }
                else
                {
                    mObjectMap.put("LoadTotal", String.format(Locale.ENGLISH, "%d", nTotal));
                }
            }

            mObjectMap.put("PickedUpBy", Utils.getStringSetting(context, Global.PREF_USER_DESCRIPTION));
            mObjectMap.put("DocumentNo", strPdfDocNumber);
            strDateTime = Utils.getRoutectlTripDateTime(context, "dd/MM/yyyy", " hh:mm aa", new Date());
            mObjectMap.put("PrintedDate", "Printed: " + strDateTime);
        }
        else if(strKind.equals("Truck Inventory"))
        {
            String strDateTime = Utils.getRoutectlTripDateTime(context, "dd/MM/yyyy", " hh:mm aa", mTrxnDate);
            mObjectMap.put("LoadSheetDate", String.format(Locale.ENGLISH, "Date	   %s", strDateTime));
            mObjectMap.put("LoadSheetTripNo", String.format(Locale.ENGLISH, "Trip#	  %s", Global.gTripNumber));
            mObjectMap.put("LoadSheetLocation", String.format(Locale.ENGLISH, "Location # %s", Utils.getStringSetting(context, Global.PREF_DEF_LOCN_NO)));

            int nTotal = 0;
            int nTotalCase = 0;
            double dTotal = 0;

            int nTotalSales = 0;
            int nTotalSalesCase = 0;
            double dTotalSales = 0;

            if( mPrintList != null && mPrintList.size() > 0 )
            {
                List<Map<String, String>> saleMapList = new ArrayList<>();
                for (UInvenDInfo info : mPrintList) {
                    if (info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) {

                        if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
                        {
                            //if( info.dStock == 0 ) continue;
                            Map<String, String> saleMap = new HashMap<>();
                            saleMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                            if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
                            {
                                String strBatchCode = "";
                                for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList)
                                {
                                    if(TextUtils.isEmpty(strBatchCode))
                                    {
                                        strBatchCode = loadBlotInfo.strLotNumber;
                                    }
                                    else
                                    {
                                        strBatchCode += ", " + loadBlotInfo.strLotNumber;
                                    }
                                }
                                if(TextUtils.isEmpty(info.strBatchCode))
                                {
                                    saleMap.put("description", info.strShortDesc + "\n" + strBatchCode);
                                }
                            }
                            else if(!TextUtils.isEmpty(info.strBatchCode))
                            {
                                saleMap.put("description", info.strShortDesc + "\n" + info.strBatchCode);
                            }
                            else
                            {
                                saleMap.put("description", info.strShortDesc);
                            }

                            saleMap.put("qty", String.format(Locale.ENGLISH, "%.2f", info.dStock));
                            saleMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%.2f", info.dStock));
                            saleMap.put("qty_un", String.format(Locale.ENGLISH, "%.2f", info.dStock));

                            saleMap.put("qty_sales", String.format(Locale.ENGLISH, "%.2f", info.dSales));
                            saleMap.put("qty_sales_cs_un", String.format(Locale.ENGLISH, "%.2f", info.dSales));
                            saleMap.put("qty_sales_un", String.format(Locale.ENGLISH, "%.2f", info.dSales));
                            saleMapList.add(saleMap);
                            dTotal += info.dStock;
                            dTotalSales += info.dSales;
                        }
                        else
                        {
                            if(bShowCase)
                            {
                                //if( info.nStock == 0 ) continue;
                                Map<String, String> saleMap = new HashMap<>();
                                saleMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                                if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
                                {
                                    String strBatchCode = "";
                                    for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList)
                                    {
                                        if(TextUtils.isEmpty(strBatchCode))
                                        {
                                            strBatchCode = loadBlotInfo.strLotNumber;
                                        }
                                        else
                                        {
                                            strBatchCode += ", " + loadBlotInfo.strLotNumber;
                                        }
                                    }
                                    if(TextUtils.isEmpty(info.strBatchCode))
                                    {
                                        saleMap.put("description", info.strShortDesc + "\n" + strBatchCode);
                                    }
                                }
                                else if(!TextUtils.isEmpty(info.strBatchCode))
                                {
                                    saleMap.put("description", info.strShortDesc + "\n" + info.strBatchCode);
                                }
                                else
                                {
                                    saleMap.put("description", info.strShortDesc);
                                }

                                if (Utils.isContainerItem(context, info.strItemNo)) {
                                    saleMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nStock));
                                    saleMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                    saleMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));

                                    saleMap.put("qty_sales", String.format(Locale.ENGLISH, "%d", info.nSales));
                                    saleMap.put("qty_sales_cs_un", String.format(Locale.ENGLISH, "%d", info.nSales));
                                    saleMap.put("qty_sales_un", String.format(Locale.ENGLISH, "%d", info.nSales));

                                    saleMapList.add(saleMap);
                                    nTotal += info.nStock;
                                    nTotalSales += info.nSales;
                                }
                                else {
                                    int nCase = getCaseValue(info.strItemNo, context);
                                    saleMap.put("qty", String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
                                    saleMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
                                    saleMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));

                                    saleMap.put("qty_sales", String.format(Locale.ENGLISH, "%d/%d", info.nSales / nCase, info.nSales % nCase));
                                    saleMap.put("qty_sales_cs_un", String.format(Locale.ENGLISH, "%d/%d", info.nSales / nCase, info.nSales % nCase));
                                    saleMap.put("qty_sales_un", String.format(Locale.ENGLISH, "%d", info.nSales));

                                    saleMapList.add(saleMap);
                                    nTotal += info.nStock % nCase;
                                    nTotalCase += info.nStock / nCase;

                                    nTotalSales += info.nSales % nCase;
                                    nTotalSalesCase += info.nSales / nCase;
                                }
                            }
                            else
                            {
                                //if( info.nStock == 0 ) continue;
                                Map<String, String> saleMap = new HashMap<>();
                                saleMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                                if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
                                {
                                    String strBatchCode = "";
                                    for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList)
                                    {
                                        if(TextUtils.isEmpty(strBatchCode))
                                        {
                                            strBatchCode = loadBlotInfo.strLotNumber;
                                        }
                                        else
                                        {
                                            strBatchCode += ", " + loadBlotInfo.strLotNumber;
                                        }
                                    }
                                    if(TextUtils.isEmpty(info.strBatchCode))
                                    {
                                        saleMap.put("description", info.strShortDesc + "\n" + strBatchCode);
                                    }
                                }
                                else if(!TextUtils.isEmpty(info.strBatchCode))
                                {
                                    saleMap.put("description", info.strShortDesc + "\n" + info.strBatchCode);
                                }
                                else
                                {
                                    saleMap.put("description", info.strShortDesc);
                                }

                                saleMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nStock ));
                                saleMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                saleMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));

                                saleMap.put("qty_sales", String.format(Locale.ENGLISH, "%d", info.nSales ));
                                saleMap.put("qty_sales_cs_un", String.format(Locale.ENGLISH, "%d", info.nSales));
                                saleMap.put("qty_sales_un", String.format(Locale.ENGLISH, "%d", info.nSales));
                                saleMapList.add(saleMap);

                                nTotal += info.nStock;
                                nTotalSales += info.nSales;
                            }

                        }

                    }
                }
                if( saleMapList.size() > 0 )
                {
                    mObjectMap.put("Saleable", saleMapList);
                }


                List<Map<String, String>> returnMapList = new ArrayList<>();
                for (UInvenDInfo info : mPrintList) {
                    if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
                    {
                        if( info.dDumps == 0 ) continue;
                        Map<String, String> returnMap = new HashMap<>();
                        returnMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                        if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
                        {
                            String strBatchCode = "";
                            for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList)
                            {
                                if(TextUtils.isEmpty(strBatchCode))
                                {
                                    strBatchCode = loadBlotInfo.strLotNumber;
                                }
                                else
                                {
                                    strBatchCode += ", " + loadBlotInfo.strLotNumber;
                                }
                            }
                            if(TextUtils.isEmpty(info.strBatchCode))
                            {
                                returnMap.put("description", info.strShortDesc + "\n" + strBatchCode);
                            }
                        }
                        else if(!TextUtils.isEmpty(info.strBatchCode))
                        {
                            returnMap.put("description", info.strShortDesc + "\n" + info.strBatchCode);
                        }
                        else
                        {
                            returnMap.put("description", info.strShortDesc);
                        }

                        returnMap.put("qty", String.format(Locale.ENGLISH, "%.2f", info.dDumps));
                        returnMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%.2f", info.dDumps));
                        returnMap.put("qty_un", String.format(Locale.ENGLISH, "%.2f", info.dDumps));
                        returnMapList.add(returnMap);
                        dTotal -= info.dDumps;
                    }
                    else
                    {
                        if(bShowCase)
                        {
                            if( info.nDumps == 0 ) continue;
                            Map<String, String> returnMap = new HashMap<>();
                            returnMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                            if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
                            {
                                String strBatchCode = "";
                                for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList)
                                {
                                    if(TextUtils.isEmpty(strBatchCode))
                                    {
                                        strBatchCode = loadBlotInfo.strLotNumber;
                                    }
                                    else
                                    {
                                        strBatchCode += ", " + loadBlotInfo.strLotNumber;
                                    }
                                }
                                if(TextUtils.isEmpty(info.strBatchCode))
                                {
                                    returnMap.put("description", info.strShortDesc + "\n" + strBatchCode);
                                }
                            }
                            else if(!TextUtils.isEmpty(info.strBatchCode))
                            {
                                returnMap.put("description", info.strShortDesc + "\n" + info.strBatchCode);
                            }
                            else
                            {
                                returnMap.put("description", info.strShortDesc);
                            }
                            if (Utils.isContainerItem(context, info.strItemNo)) {
                                returnMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nDumps));
                                returnMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nDumps));
                                returnMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nDumps));
                                returnMapList.add(returnMap);
                                nTotal -= info.nDumps;
                            }
                            else {
                                int nCase = getCaseValue(info.strItemNo, context);
                                returnMap.put("qty", String.format(Locale.ENGLISH, "%d/%d", info.nDumps / nCase, info.nDumps % nCase));
                                returnMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d/%d", info.nDumps / nCase, info.nDumps % nCase));
                                returnMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nDumps));
                                returnMapList.add(returnMap);
                                nTotal -= info.nDumps % nCase;
                                nTotalCase -= info.nDumps / nCase;
                            }
                        }
                        else
                        {
                            if( info.nDumps == 0 ) continue;
                            Map<String, String> returnMap = new HashMap<>();
                            returnMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                            if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
                            {
                                String strBatchCode = "";
                                for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList)
                                {
                                    if(TextUtils.isEmpty(strBatchCode))
                                    {
                                        strBatchCode = loadBlotInfo.strLotNumber;
                                    }
                                    else
                                    {
                                        strBatchCode += ", " + loadBlotInfo.strLotNumber;
                                    }
                                }
                                if(TextUtils.isEmpty(info.strBatchCode))
                                {
                                    returnMap.put("description", info.strShortDesc + "\n" + strBatchCode);
                                }
                            }
                            else if(!TextUtils.isEmpty(info.strBatchCode))
                            {
                                returnMap.put("description", info.strShortDesc + "\n" + info.strBatchCode);
                            }
                            else
                            {
                                returnMap.put("description", info.strShortDesc);
                            }

                            returnMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nDumps));
                            returnMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nDumps));
                            returnMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nDumps));
                            returnMapList.add(returnMap);
                            nTotal -= info.nDumps;
                        }

                    }
                }
                if( returnMapList.size() > 0 )
                {
                    mObjectMap.put("Return", returnMapList);
                }

                List<Map<String, String>> containerMapList = new ArrayList<>();
                for (UInvenDInfo info : mPrintList) {
                    if (info.nSCWType == Global.INVENTORY_CONTAINERS) {
                        if(bShowCase)
                        {
                            Map<String, String> containerMap = new HashMap<>();
                            containerMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                            containerMap.put("description", info.strShortDesc);
                            if (Utils.isContainerItem(context, info.strItemNo)) {
                                containerMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nStock));
                                containerMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                containerMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                containerMapList.add(containerMap);
                                nTotal += info.nStock;
                            }
                            else {
                                int nCase = getCaseValue(info.strItemNo, context);
                                containerMap.put("qty", String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
                                containerMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
                                containerMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                                containerMapList.add(containerMap);
                                nTotal += info.nStock % nCase;
                                nTotalCase += info.nStock / nCase;
                            }
                        }
                        else
                        {
                            Map<String, String> containerMap = new HashMap<>();
                            containerMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                            containerMap.put("description", info.strShortDesc);
                            containerMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nStock));
                            containerMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                            containerMap.put("qty_un", String.format(Locale.ENGLISH, "%d", info.nStock));
                            containerMapList.add(containerMap);
                            nTotal += info.nStock;
                        }

                    }
                }
                if( containerMapList.size() > 0 )
                {
                    mObjectMap.put("Container", containerMapList);
                }
            }
            if(dTotal > 0 && (nTotalCase > 0 || nTotal > 0))
            {
                if(bShowCase)
                {
                    mObjectMap.put("Total", String.format(Locale.ENGLISH, "%d/%d\n%.2f Kg", nTotalCase, nTotal, dTotal));
                }
                else
                {
                    mObjectMap.put("Total", String.format(Locale.ENGLISH, "%d\n%.2f Kg", nTotal, dTotal));
                }

            }
            else if(dTotal > 0)
            {
                mObjectMap.put("Total", String.format(Locale.ENGLISH, "%.2f Kg", dTotal));
            }
            else
            {
                if(bShowCase)
                {
                    mObjectMap.put("Total", String.format(Locale.ENGLISH, "%d/%d", nTotalCase, nTotal));
                }
                else
                {
                    mObjectMap.put("Total", String.format(Locale.ENGLISH, "%d", nTotal));
                }
            }
        }
    }

    public void PreparePrint(String strDeliveryDate, String strName, String strOrderNo, List<PresoldOrDInfo> infoList, String strPORef, Context context, Map<String, ProdDetlInfo> prodDetlInfoMap)
    {
        mObjectMap.clear();
        mArrayObjectMap.clear();
        mObjectMap.put("logo", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/Cmpylogo.bmp");
        mObjectMap.put("Customer", "Customer: " + strName);
        mObjectMap.put("Date", "Date " + strDeliveryDate);
        mObjectMap.put("OrderNo", "Order #:" + strOrderNo);
        mObjectMap.put("Ref", "PO REF #:" + strPORef);

        int nTotal = 0;
        if( infoList != null && infoList.size() > 0 )
        {
            List<Map<String, String>> loadMapList = new ArrayList<>();
            Collections.sort(infoList, (t1, t2) -> t1.strItemNo.compareTo(t2.strItemNo));
            for (PresoldOrDInfo info : infoList) {
                if(info.nOrderQty > 0 && info.strTrxnType.equals("4"))
                {
                    Map<String, String> loadMap = new HashMap<>();
                    loadMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                    loadMap.put("full_description", info.strLongDesc);
                    loadMap.put("description", info.strShortDesc);
                    loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nOrderQty));

                    ProdDetlInfo productInfo = prodDetlInfoMap.get(info.strItemNo);
                    if(productInfo != null && productInfo.prodLocnInfo != null)
                    {
                        loadMap.put("group", productInfo.strProdGrp);
                    }
                    else
                    {
                        loadMap.put("group", "");
                    }

                    loadMapList.add(loadMap);
                    nTotal += info.nOrderQty;
                }
            }
            if( loadMapList.size() > 0 )
            {
                mObjectMap.put("PickSlip", loadMapList);
            }

            Map<Integer, List<PresoldOrDInfo>> presoldOrDInfoMap = new HashMap<>();
            for(PresoldOrDInfo info : infoList)
            {
                if(info.nOrderQty > 0 && (info.strTrxnType.equals("11") || info.strTrxnType.equals("12")))
                {
                    List<PresoldOrDInfo> presoldOrDInfoList = presoldOrDInfoMap.get(info.nReasonCode);
                    if(presoldOrDInfoList == null)
                    {
                        presoldOrDInfoList = new ArrayList<>();
                        presoldOrDInfoMap.put(info.nReasonCode, presoldOrDInfoList);
                    }
                    presoldOrDInfoList.add(info);
                }
            }

            DbAdapter dbAdapter = new DbAdapter(context);
            dbAdapter.open();
            List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList("FREERSN");
            dbAdapter.close();
            if(descTypeInfoList != null && descTypeInfoList.size() > 0)
            {
                Map<Integer, DescTypeInfo> descTypeInfoMap = new HashMap<>();
                for(DescTypeInfo descTypeInfo : descTypeInfoList)
                {
                    descTypeInfoMap.put(descTypeInfo.nNumericKey, descTypeInfo);
                }

                loadMapList = new ArrayList<>();
                Iterator it = presoldOrDInfoMap.entrySet().iterator();

                while(it.hasNext())
                {
                    Map.Entry pair = (Map.Entry)it.next();
                    Integer nReasonCode = (Integer)pair.getKey();
                    DescTypeInfo descTypeInfo = descTypeInfoMap.get(nReasonCode);
                    if(descTypeInfo != null)
                    {
                        List<PresoldOrDInfo> presoldOrDInfoList = (List<PresoldOrDInfo>)pair.getValue();
                        Collections.sort(presoldOrDInfoList, new Comparator<PresoldOrDInfo>() {
                            @Override
                            public int compare(PresoldOrDInfo t1, PresoldOrDInfo t2) {
                                return t1.strItemNo.compareTo(t2.strItemNo);
                            }
                        });
                        if(presoldOrDInfoList.size() > 0)
                        {
                            Map<String, String> loadMap = new HashMap<>();
                            loadMapList.add(loadMap);
                            loadMap.put("title", descTypeInfo.strDescription);

                            for(PresoldOrDInfo info : presoldOrDInfoList)
                            {
                                loadMap = new HashMap<>();
                                loadMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                                loadMap.put("full_description", info.strLongDesc);
                                loadMap.put("description", info.strShortDesc);
                                loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nOrderQty));

                                ProdDetlInfo productInfo = prodDetlInfoMap.get(info.strItemNo);
                                if(productInfo != null && productInfo.prodLocnInfo != null)
                                {
                                    loadMap.put("group", productInfo.strProdGrp);
                                }
                                else
                                {
                                    loadMap.put("group", "");
                                }

                                loadMapList.add(loadMap);
                                nTotal += info.nOrderQty;
                            }
                        }

                    }
                    it.remove();
                }

                if( loadMapList.size() > 0 )
                {
                    mObjectMap.put("Free", loadMapList);
                }
            }
        }
        mObjectMap.put("Total", String.valueOf(nTotal));
    }

    public void PreparePrint(String strDeliveryDate, String strName, String strOrderNo, List<OrderLineInfo> infoList, String strPORef)
    {
        mObjectMap.clear();
        mArrayObjectMap.clear();
        mObjectMap.put("logo", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/Cmpylogo.bmp");
        mObjectMap.put("Customer", "Customer: " + strName);
        mObjectMap.put("Date", "Date " + strDeliveryDate);
        mObjectMap.put("OrderNo", "Order #:" + strOrderNo);
        mObjectMap.put("Ref", "PO REF #:" + strPORef);

        int nTotal = 0;
        if( infoList != null && infoList.size() > 0 )
        {
            List<Map<String, String>> loadMapList = new ArrayList<>();
            Collections.sort(infoList, new Comparator<OrderLineInfo>() {
                @Override
                public int compare(OrderLineInfo t1, OrderLineInfo t2) {
                    return t1.itemNumber.compareTo(t2.itemNumber);
                }
            });
            for (OrderLineInfo info : infoList) {
                if(info.quantity > 0 && (info.transactionType == 4 || info.transactionType == 11 || info.transactionType == 12))
                {
                    Map<String, String> loadMap = new HashMap<>();
                    loadMap.put("item", info.itemNumber);
                    loadMap.put("full_description", info.strLongDesc);
                    loadMap.put("description", info.strShortDesc);
                    loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.quantity));
                    loadMapList.add(loadMap);
                    nTotal += info.quantity;
                }
            }
            if( loadMapList.size() > 0 )
            {
                mObjectMap.put("PickSlip", loadMapList);
            }

            /*Map<Integer, List<OrderLineInfo>> presoldOrDInfoMap = new HashMap<>();
            for(OrderLineInfo info : infoList)
            {
                if(info.quantity > 0 && (info.transactionType == 11 || info.transactionType == 12))
                {
                    List<PresoldOrDInfo> presoldOrDInfoList = presoldOrDInfoMap.get(info.nReasonCode);
                    if(presoldOrDInfoList == null)
                    {
                        presoldOrDInfoList = new ArrayList<>();
                        presoldOrDInfoMap.put(info.nReasonCode, presoldOrDInfoList);
                    }
                    presoldOrDInfoList.add(info);
                }
            }

            DbAdapter dbAdapter = new DbAdapter(context);
            dbAdapter.open();
            List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList("FREERSN");
            dbAdapter.close();
            if(descTypeInfoList != null && descTypeInfoList.size() > 0)
            {
                Map<Integer, DescTypeInfo> descTypeInfoMap = new HashMap<>();
                for(DescTypeInfo descTypeInfo : descTypeInfoList)
                {
                    descTypeInfoMap.put(descTypeInfo.nNumericKey, descTypeInfo);
                }

                loadMapList = new ArrayList<>();
                Iterator it = presoldOrDInfoMap.entrySet().iterator();

                while(it.hasNext())
                {
                    Map.Entry pair = (Map.Entry)it.next();
                    Integer nReasonCode = (Integer)pair.getKey();
                    DescTypeInfo descTypeInfo = descTypeInfoMap.get(nReasonCode);
                    if(descTypeInfo != null)
                    {
                        List<PresoldOrDInfo> presoldOrDInfoList = (List<PresoldOrDInfo>)pair.getValue();
                        Collections.sort(presoldOrDInfoList, new Comparator<PresoldOrDInfo>() {
                            @Override
                            public int compare(PresoldOrDInfo t1, PresoldOrDInfo t2) {
                                return t1.strItemNo.compareTo(t2.strItemNo);
                            }
                        });
                        if(presoldOrDInfoList.size() > 0)
                        {
                            Map<String, String> loadMap = new HashMap<>();
                            loadMapList.add(loadMap);
                            loadMap.put("title", descTypeInfo.strDescription);

                            for(PresoldOrDInfo info : presoldOrDInfoList)
                            {
                                loadMap = new HashMap<>();
                                loadMap.put("item", info.strItemNo);
                                loadMap.put("full_description", info.strLongDesc);
                                loadMap.put("description", info.strShortDesc);
                                loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nOrderQty));
                                loadMapList.add(loadMap);
                                nTotal += info.nOrderQty;
                            }
                        }

                    }
                    it.remove();
                }

                if( loadMapList.size() > 0 )
                {
                    mObjectMap.put("Free", loadMapList);
                }
            }*/
        }
        mObjectMap.put("Total", String.valueOf(nTotal));
    }

    public void PreparePrint(Context context, List<CustomerInfo> customerInfoList, Date date, String carton)
    {
        mObjectMap.clear();
        mArrayObjectMap.clear();
        mObjectMap.put("logo", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/Cmpylogo.bmp");
        String strDateTime = Utils.getRoutectlTripDateTime(context, "dd/MM/yyyy", " hh:mm aa", date);
        mObjectMap.put("Date", "Date : " + strDateTime );
        mObjectMap.put("Trip", "Trip # : " + Global.gTripNumber);

        if(customerInfoList != null && customerInfoList.size() > 0)
        {
            List<Map<String, String>> loadMapList = new ArrayList<>();
            for(CustomerInfo info : customerInfoList)
            {
                String strContent = "";
                if((!TextUtils.isEmpty(info.strStartTime1) && !TextUtils.isEmpty(info.strEndTime1) && !info.strStartTime1.equals("0000") && !info.strEndTime1.equals("0000"))
                        || (!TextUtils.isEmpty(info.strStartTime2) && !TextUtils.isEmpty(info.strEndTime2) && !info.strStartTime2.equals("0000") && !info.strEndTime2.equals("0000")))
                {
                    strContent += "Delivery ";
                    boolean bTime1Exist = false;
                    if(!info.strStartTime1.equals("0000") && !info.strEndTime1.equals("0000"))
                    {
                        SimpleDateFormat sdf1 = new SimpleDateFormat("kk:mm");
                        SimpleDateFormat sdf2 = new SimpleDateFormat("kkmm");
                        SimpleDateFormat sdf_target = new SimpleDateFormat("h:mm a");

                        String strStartTime = "";
                        if(info.strStartTime1.contains(":"))
                        {
                            try
                            {
                                date = sdf1.parse(info.strStartTime1);
                                strStartTime = sdf_target.format(date);
                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            try
                            {
                                date = sdf2.parse(info.strStartTime1);
                                strStartTime = sdf_target.format(date);
                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }

                        String strEndTime = "";
                        if(info.strEndTime1.contains(":"))
                        {
                            try
                            {
                                date = sdf1.parse(info.strEndTime1);
                                strEndTime = sdf_target.format(date);
                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            try
                            {
                                date = sdf2.parse(info.strEndTime1);
                                strEndTime = sdf_target.format(date);
                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        if(!TextUtils.isEmpty(strStartTime) && !TextUtils.isEmpty(strEndTime))
                        {
                            strContent += strStartTime + " to " + strEndTime;
                            bTime1Exist = true;
                        }
                    }
                    if(!info.strStartTime2.equals("0000") || !info.strEndTime2.equals("0000"))
                    {
                        SimpleDateFormat sdf1 = new SimpleDateFormat("kk:mm");
                        SimpleDateFormat sdf2 = new SimpleDateFormat("kkmm");
                        SimpleDateFormat sdf_target = new SimpleDateFormat("h:mm a");

                        String strStartTime = "";
                        if(info.strStartTime2.contains(":"))
                        {
                            try
                            {
                                date = sdf1.parse(info.strStartTime2);
                                strStartTime = sdf_target.format(date);
                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            try
                            {
                                date = sdf2.parse(info.strStartTime2);
                                strStartTime = sdf_target.format(date);
                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }

                        String strEndTime = "";
                        if(info.strEndTime2.contains(":"))
                        {
                            try
                            {
                                date = sdf1.parse(info.strEndTime2);
                                strEndTime = sdf_target.format(date);
                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            try
                            {
                                date = sdf2.parse(info.strEndTime2);
                                strEndTime = sdf_target.format(date);
                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        if(!TextUtils.isEmpty(strStartTime) && !TextUtils.isEmpty(strEndTime))
                        {
                            if(bTime1Exist)
                            {
                                strContent += " or " +strStartTime + " to " + strEndTime;
                            }
                        }
                    }
                }
                if(info.presoldOrHInfo != null && info.presoldOrHInfo.size() > 0)
                {
                    for(int i = 0; i < info.presoldOrHInfo.size(); i++)
                    {
                        String strUser1 = info.presoldOrHInfo.get(i).strUser1;

                        if(!TextUtils.isEmpty(strContent))
                        {
                            strContent += "\n";
                        }
                        strContent += "Order " + info.presoldOrHInfo.get(i).strOrderNo;
                        if(strUser1.trim().length() > 0 && !strUser1.trim().equals("0"))
                            if(strUser1.trim().length() > 0 && !strUser1.trim().equals("0"))
                            {
                                strContent += " for " + info.presoldOrHInfo.get(i).strUser1;
                                if(!TextUtils.isEmpty(carton))
                                {
                                    int nValue = Utils.parseIntFromString(info.presoldOrHInfo.get(i).strUser1);
                                    if(nValue > 1)
                                    {
                                        strContent += " " + carton + "s";
                                    }
                                    else
                                    {
                                        strContent += " " + carton;
                                    }
                                }
                            }
                        if(info.presoldOrHInfo.get(i).strInstrs.trim().length() > 0)
                        {
                            strContent += "\n" + info.presoldOrHInfo.get(i).strInstrs;
                        }
                    }
                }

                if(info.strStoreNo != null && info.strStoreNo.length() > 0 && !info.strStoreNo.equals("0"))
                {
                    strContent += "\n" + "Store No " + info.strStoreNo;
                }

                if(!TextUtils.isEmpty(info.strCustMsg))
                {
                    strContent += "\n" + info.strCustMsg;
                }

                Map<String, String> loadMap = new HashMap<>();
                String strCustomerName;
                if(info.nChainNo != 0 )
                {
                    strCustomerName = info.nChainNo + "/" + info.nCustNo + " " + info.strName;
                }
                else
                {
                    strCustomerName = info.nCustNo + " " + info.strName;
                }
                loadMap.put("name", strCustomerName);
                loadMap.put("content", strContent);
                loadMapList.add(loadMap);
            }
            if( loadMapList.size() > 0 )
            {
                mObjectMap.put("Customer", loadMapList);
            }
        }
    }

    public void PreparePrint(Context context, String strDeliveryDate, String strName, String strOrderNo, List<UOrderDInfo> infoList, String strPORef)
    {
        mObjectMap.clear();
        mArrayObjectMap.clear();
        mObjectMap.put("logo", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/Cmpylogo.bmp");
        mObjectMap.put("Customer", "Customer: " + strName);
        mObjectMap.put("Date", "Date " + strDeliveryDate);
        mObjectMap.put("OrderNo", "Order #:" + strOrderNo);
        mObjectMap.put("Ref", "PO REF #:" + strPORef);

        int nTotal = 0;
        double dTotal = 0;
        if( infoList != null && infoList.size() > 0 )
        {
            List<Map<String, String>> loadMapList = new ArrayList<>();
            Collections.sort(infoList, (t1, t2) -> t1.strItemNo.compareTo(t2.strItemNo));
            for (UOrderDInfo info : infoList) {
                if( info.nTrxnType == Global.TRXN_DELIVER)
                {
                    if(info.nSales == 0 && info.dSales == 0 )
                        continue;
                    Map<String, String> loadMap = new HashMap<>();
                    loadMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                    if(info.uLoadBlotInfo.size() > 0)
                    {
                        String strLotItems = "";
                        for(LoadBlotInfo loadBlotInfo : info.uLoadBlotInfo)
                        {
                            if(TextUtils.isEmpty(strLotItems))
                            {
                                strLotItems = loadBlotInfo.strLotNumber;
                            }
                            else
                            {
                                strLotItems += ", " + loadBlotInfo.strLotNumber;
                            }
                        }
                        loadMap.put("description", info.strShortDesc + "\n" + strLotItems);
                        loadMap.put("full_description", info.strDescription + "\n" + strLotItems);
                    }
                    else
                    {
                        loadMap.put("description", info.strShortDesc);
                        loadMap.put("full_description", info.strDescription);
                    }

                    loadMap.put("group", info.strProdGroup);
                    if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
                    {
                        loadMap.put("qty", String.format(Locale.ENGLISH, "%.2f Kg", info.dSales));
                        dTotal += info.dSales;
                    }
                    else
                    {
                        loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nSales));
                        nTotal += info.nSales;
                    }

                    loadMapList.add(loadMap);
                }
            }
            if( loadMapList.size() > 0 )
            {
                mObjectMap.put("PickSlip", loadMapList);
            }

            Map<Integer, List<UOrderDInfo>> uOrderDInfoMap = new HashMap<>();
            for(UOrderDInfo info : infoList)
            {
                if(info.nSales == 0 && info.dSales == 0) continue;
                if(info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE)
                {
                    List<UOrderDInfo> uOrderDInfoList= uOrderDInfoMap.get(info.nReasonCode);
                    if(uOrderDInfoList == null)
                    {
                        uOrderDInfoList = new ArrayList<>();
                        uOrderDInfoMap.put(info.nReasonCode, uOrderDInfoList);
                    }
                    uOrderDInfoList.add(info);
                }
            }

            DbAdapter dbAdapter = new DbAdapter(context);
            dbAdapter.open();
            List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList("FREERSN");
            dbAdapter.close();
            if(descTypeInfoList != null && descTypeInfoList.size() > 0)
            {
                Map<Integer, DescTypeInfo> descTypeInfoMap = new HashMap<>();
                for(DescTypeInfo descTypeInfo : descTypeInfoList)
                {
                    descTypeInfoMap.put(descTypeInfo.nNumericKey, descTypeInfo);
                }

                loadMapList = new ArrayList<>();
                Iterator it = uOrderDInfoMap.entrySet().iterator();

                while(it.hasNext())
                {
                    Map.Entry pair = (Map.Entry)it.next();
                    Integer nReasonCode = (Integer)pair.getKey();
                    DescTypeInfo descTypeInfo = descTypeInfoMap.get(nReasonCode);
                    if(descTypeInfo != null)
                    {
                        List<UOrderDInfo> uOrderDInfoList = (List<UOrderDInfo>)pair.getValue();
                        Collections.sort(uOrderDInfoList, new Comparator<UOrderDInfo>() {
                            @Override
                            public int compare(UOrderDInfo t1, UOrderDInfo t2) {
                                return t1.strItemNo.compareTo(t2.strItemNo);
                            }
                        });
                        if(uOrderDInfoList.size() > 0)
                        {
                            Map<String, String> loadMap = new HashMap<>();
                            loadMapList.add(loadMap);
                            loadMap.put("title", descTypeInfo.strDescription);

                            for(UOrderDInfo info : uOrderDInfoList)
                            {
                                loadMap = new HashMap<>();
                                loadMap.put("item", Utils.getDisplayItemCode(context, info.strItemNo));
                                loadMap.put("description", info.strShortDesc);
                                loadMap.put("full_description", info.strDescription);
                                loadMap.put("group", info.strProdGroup);
                                if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
                                {
                                    loadMap.put("qty", String.format(Locale.ENGLISH, "%.2f Kg", info.dSales));
                                    dTotal += info.dSales;
                                }
                                else
                                {
                                    loadMap.put("qty", String.format(Locale.ENGLISH, "%d", info.nSales));
                                    nTotal += info.nSales;
                                }
                                loadMapList.add(loadMap);
                            }
                        }
                    }
                    it.remove();
                }

                if( loadMapList.size() > 0 )
                {
                    mObjectMap.put("Free", loadMapList);
                }
            }
        }
        if(dTotal > 0 && nTotal > 0)
        {
            mObjectMap.put("Total", String.valueOf(nTotal) + "\n" + String.format(Locale.ENGLISH, "%.2f Kg", dTotal));
        }
        else if(dTotal > 0)
        {
            mObjectMap.put("Total", String.format(Locale.ENGLISH, "%.2f Kg", dTotal));
        }
        else
        {
            mObjectMap.put("Total", String.valueOf(nTotal));
        }

    }

    public void PrepareTransactionHistoryPrint(Context context, List<TransactionInfo> mPrintList)
    {
        mObjectMap.clear();
        mArrayObjectMap.clear();
        mObjectMap.put("logo", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/Cmpylogo.bmp");

        //Header Doc Text
        DbAdapter dbAdapter = new DbAdapter(context);
        dbAdapter.open();
        List<DocTextInfo> mDocTextList = new ArrayList<>();
        for(int i = 6; i <= 7; i++)
        {
            List<DocTextInfo> docTextList = dbAdapter.getDocTextList(i);;
            if( docTextList != null && docTextList.size() > 0)
            {
                mDocTextList.addAll(docTextList);
            }
        }
        dbAdapter.close();

        List<String> headerDocText = new ArrayList<>();
        for(DocTextInfo info : mDocTextList )
        {
            if( info.nTextType == 6 )
            {
                if(info.strDocText.length() == 0)
                {
                    headerDocText.add(" ");
                }
                else
                {
                    headerDocText.add(info.strDocText);
                }
            }
        }
        mObjectMap.put("HeaderDocText", headerDocText);

        List<Map<String, String>> transList = new ArrayList<>();

        Collections.sort(mPrintList, new Comparator<TransactionInfo>() {
            @Override
            public int compare(TransactionInfo t1, TransactionInfo t2) {
                return t1.strTime.compareTo(t2.strTime);
            }
        });

        for(TransactionInfo info : mPrintList)
        {
            Map<String, String> transMap = new HashMap<>();
            transMap.put("DocNo", info.strDocNo);
            transMap.put("Time", info.strTime);
            transMap.put("Customer", info.strCustomerName);
            transMap.put("Type", info.strType);
            transMap.put("Vd", info.strVoid);
            if(info.strType.equals("INV") || info.strType.equals("PAY"))
            {
                transMap.put("Value", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.value));
            }
            else
            {
                transMap.put("Value", " ");
            }
            transList.add(transMap);
        }

        mObjectMap.put("Trans", transList);
    }

    private Map<String, String> getA4ItemMap(Context context, UOrderDInfo info)
    {
        int nConsumerUnit = 1;
        if(info.nTrxnType != Global.TRXN_FREE && info.nSCWType != Global.INVENTORY_CONTAINERS)
        {
            DbAdapter dbAdapter = new DbAdapter(context);
            dbAdapter.open();
            ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
            dbAdapter.close();


            if(prodDetlInfo != null)
            {
                nConsumerUnit = prodDetlInfo.nConsumerUnit;
            }
        }

        boolean bShowCase = Utils.getStringSetting(context, Global.PREF_INVENTORYUOM).equals("U") == false;

        Map<String, String> map = new HashMap<>();
        if(info.nTrxnType == Global.TRXN_FREE)
        {
            map.put("code", " ");
            map.put("description", "Free Item");
            map.put("quantity", String.valueOf(info.nQuantity));
            map.put("unit_price", "$0.00");
            map.put("cds_levy", "$0.00");
            map.put("amount", "$0.00");
            map.put("gst", "$0.00");
            map.put("total_amount", "$0.00");
        }
        else if(info.nSCWType == Global.INVENTORY_CONTAINERS)
        {
            map.put("code", Utils.getDisplayItemCode(context, info.strItemNo));
            map.put("description", info.strDescription);

            if( info.nTrxnType == Global.TRXN_CONTAINER_DUMP)
            {
                map.put("quantity", String.valueOf(-1 * info.nQuantity));
            }
            else
            {
                map.put("quantity", String.valueOf(info.nQuantity));
            }
            map.put("unit_price", " ");
            map.put("cds_levy", " ");
            map.put("amount", " ");
            map.put("gst", " ");
            map.put("total_amount", " ");
        }
        else if(info.nTrxnType == Global.TRXN_PICKUP)
        {
            map.put("code", Utils.getDisplayItemCode(context, info.strItemNo));
            map.put("description", info.strDescription);
            map.put("quantity", String.valueOf(info.nQuantity));

            if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
            {
                map.put("qty", String.format(Locale.ENGLISH, "%.2f", info.dQuantity));
            }
            else
            {
                if(bShowCase)
                {
                    if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") )
                    {
                        if (Utils.isContainerItem(context, info.strItemNo)) {
                            map.put("qty", String.format(Locale.ENGLISH, "%d", info.nDumps));
                        }
                        else {
                            int nCase = getCaseValue(info.strItemNo, context);
                            int nQty =  info.nDumps;
                            int nCaseValue = nQty / nCase;
                            int nQtyValue = nQty % nCase;
                            map.put("qty", String.format(Locale.ENGLISH, "%d/%d", nCaseValue, nQtyValue));
                        }
                    }
                    else
                    {
                        map.put("qty", String.valueOf(info.nQuantity));
                    }
                }
                else
                {
                    map.put("qty", String.valueOf(info.nQuantity));
                }
            }

            float nPrice = info.nBasePrice;
            if( nPrice != 0 )
            {
                map.put("unit_price", String.format(Locale.ENGLISH, "-$%.2f", nPrice));
            }
            else
            {
                map.put("unit_price", String.format(Locale.ENGLISH, "$%.2f", nPrice));
            }

            if( info.nCDSRate != 0 )
            {
                map.put("cds_levy", String.format(Locale.ENGLISH, "-$%.2f", Utils.getDividedValue(info.nCDSRate)));
            }
            else
            {
                map.put("cds_levy", String.format(Locale.ENGLISH, "$%.2f", Utils.getDividedValue(info.nCDSRate)));
            }


            if( (nPrice + Utils.get2DecimalValue(Utils.getDividedValue(info.nCDSRate))) * info.nQuantity != 0 )
                map.put("amount", String.format(Locale.ENGLISH, "-$%.2f", (nPrice + Utils.getDividedValue(info.nCDSRate) * nConsumerUnit) * info.nQuantity ));
            else
                map.put("amount", String.format(Locale.ENGLISH, "$%.2f", (nPrice + Utils.getDividedValue(info.nCDSRate) * nConsumerUnit) * info.nQuantity * nConsumerUnit));

            float fGSTAmount = info.nPrintTaxAmount - info.nPrintCDSAmount;

            if( fGSTAmount != 0 )
                map.put("gst", String.format(Locale.ENGLISH, "-$%.2f", fGSTAmount));
            else
                map.put("gst", String.format(Locale.ENGLISH, "$%.2f", fGSTAmount));


            float nPrintAmount = info.nPrice * info.nQuantity + fGSTAmount + Utils.getDividedValue(info.nCDSRate) * info.nQuantity * nConsumerUnit;

            if(nPrintAmount != 0 )
                map.put("total_amount", String.format(Locale.ENGLISH, "-$%.2f", nPrintAmount));
            else
                map.put("total_amount", String.format(Locale.ENGLISH, "$%.2f", nPrintAmount));
        }
        else
        {
            map.put("code", Utils.getDisplayItemCode(context, info.strItemNo));
            map.put("description", info.strDescription);
            map.put("quantity", String.valueOf(info.nQuantity));

            if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
            {
                map.put("qty", String.format(Locale.ENGLISH, "%.2f", info.dQuantity));
            }
            else
            {
                if(bShowCase)
                {
                    if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") )
                    {
                        if (Utils.isContainerItem(context, info.strItemNo)) {
                            map.put("qty", String.format(Locale.ENGLISH, "%d", info.nDumps));
                        }
                        else {
                            int nCase = getCaseValue(info.strItemNo, context);
                            int nQty =  info.nDumps;
                            int nCaseValue = nQty / nCase;
                            int nQtyValue = nQty % nCase;
                            map.put("qty", String.format(Locale.ENGLISH, "%d/%d", nCaseValue, nQtyValue));
                        }
                    }
                    else
                    {
                        map.put("qty", String.valueOf(info.nQuantity));
                    }
                }
                else
                {
                    map.put("qty", String.valueOf(info.nQuantity));
                }
            }

            float nPrice = info.nBasePrice;
            map.put("unit_price", String.format(Locale.ENGLISH, "$%.2f", nPrice));
            map.put("cds_levy", String.format(Locale.ENGLISH, "$%.2f", Utils.getDividedValue(info.nCDSRate)));
            map.put("amount", String.format(Locale.ENGLISH, "$%.2f", (nPrice + Utils.getDividedValue(info.nCDSRate) * nConsumerUnit) * info.nQuantity));
            float fGst = info.nPrintTaxAmount - info.nPrintCDSAmount;
            map.put("gst", String.format(Locale.ENGLISH, "$%.2f", fGst ));
            float fTotalAmount = nPrice * info.nQuantity + fGst + Utils.getDividedValue(info.nCDSRate) * info.nQuantity * nConsumerUnit;
            map.put("total_amount", String.format(Locale.ENGLISH, "$%.2f", fTotalAmount));
        }
        return map;
    }

    public void PreparePrint(Context context, CustomerInfo mCustomerInfo, List<UOrderDInfo> mPrintList, int nPayType, boolean bReservedMode, boolean bFutureMode, Map<String, PriceGrpInfo> priceGrpInfoMap,
                             Map<String, PriceGrpInfo> altPriceGrpInfoMap, List<String> strPhotoPathList, String strTemperature, boolean bPartialPayment, float fPartialPaidAmount)
    {
        mObjectMap.clear();
        mArrayObjectMap.clear();
        mProdInfoMap.clear();

        DbAdapter dbAdapter = new DbAdapter(context);
        dbAdapter.open();
        mProdInfoMap = dbAdapter.getProdInfoMap("Arabic Descript");

        mObjectMap.put("logo", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/Cmpylogo.bmp");
        mObjectMap.put("logo_docket", Utils.getExternalStorageRootPath() + "/.DSDVanSales/logo/CmpylogoDocket.bmp");
        double nMyLat = Utils.getDoubleSetting(context, Global.PREF_MY_LATITUDE);
        double nMyLng = Utils.getDoubleSetting(context, Global.PREF_MY_LONGITUDE);
        mObjectMap.put("PrintedGeoCode", String.format(Locale.ENGLISH, "Printed at %.6f, %.6f", nMyLat, nMyLng));

        if(strPhotoPathList != null && strPhotoPathList.size() > 0)
        {
            mObjectMap.put("Photograph", Utils.decodeFile(strPhotoPathList.get(0), 240 * 240));
            List<String> photos = new ArrayList<>();

            int i = 0;
            for(String strPhoto : strPhotoPathList)
            {
                String strNewPath = Global.UPLOAD_DIR + System.currentTimeMillis() + i + ".jpg";
                Utils.decodeFile(strPhoto, strNewPath, 240 * 240);
                photos.add(strNewPath);
                i++;
            }
            mObjectMap.put("PhotographList", photos);
        }

        boolean bShowCase = Utils.getStringSetting(context, Global.PREF_INVENTORYUOM).equals("U") == false;

        //Header Doc Text
        List<DescTypeInfo> invoiceNoPrefixInfoList = dbAdapter.getDescTypeListWithNumericKey("RouteNumber", Utils.getIntFromString(Global.gRouteNumber));
        String strInvoiceNoPrefix = "";
        if(invoiceNoPrefixInfoList != null && invoiceNoPrefixInfoList.size() > 0)
        {
            strInvoiceNoPrefix = invoiceNoPrefixInfoList.get(0).strValue1;
        }

        List<DocTextInfo> mDocTextList = new ArrayList<>();
        for(int i = 6; i <= 7; i++)
        {
            List<DocTextInfo> docTextList = dbAdapter.getDocTextList(mCustomerInfo.nCustNo, mCustomerInfo.nChainNo, i);
            if( docTextList == null || docTextList.size() == 0 )
            {
                docTextList = dbAdapter.getDocTextList(mCustomerInfo.nCustNo, i);
            }
            if( docTextList == null || docTextList.size() == 0 )
            {
                docTextList = dbAdapter.getDocTextList(i);
            }
            if( docTextList != null && docTextList.size() > 0)
            {
                mDocTextList.addAll(docTextList);
            }
        }

        //Get the Header Doc Text with 3
        List<DocTextInfo> mHeaderDocTextList = dbAdapter.getDocTextList(mCustomerInfo.nCustNo, mCustomerInfo.nChainNo, 3);
        if( mHeaderDocTextList == null || mHeaderDocTextList.size() == 0 )
        {
            mHeaderDocTextList = dbAdapter.getDocTextList(mCustomerInfo.nCustNo, 3);
        }
        if( mHeaderDocTextList == null || mHeaderDocTextList.size() == 0 )
        {
            mHeaderDocTextList = dbAdapter.getDocTextList(3);
        }

        //Fill in the UPC Information
        for(UOrderDInfo uOrderDInfo : mPrintList)
        {
            ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetlInfo(uOrderDInfo.strItemNo);
            if( prodDetlInfo != null )
            {
                uOrderDInfo.strItemUPC = prodDetlInfo.strItemUPC20;
            }
        }

        List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList("FREERSN");
        Map<Integer, DescTypeInfo> descTypeInfoMap = new HashMap<>();
        if(descTypeInfoList != null && descTypeInfoList.size() > 0)
        {
            for(DescTypeInfo descTypeInfo : descTypeInfoList)
            {
                descTypeInfoMap.put(descTypeInfo.nNumericKey, descTypeInfo);
            }
        }

        List<String> headerDocText = new ArrayList<>();
        for(DocTextInfo info : mDocTextList )
        {
            if( info.nTextType == 6 )
            {
                if(info.strDocText.length() == 0)
                {
                    headerDocText.add(" ");
                }
                else
                {
                    headerDocText.add(info.strDocText);
                }
            }
        }
        mObjectMap.put("HeaderDocText", headerDocText);

        mObjectMap.put("Temperature", "Temperature : " + strTemperature);

        if(mHeaderDocTextList != null && mHeaderDocTextList.size() > 0)
        {
            String strHeaderDocText = "";
            for(DocTextInfo info : mHeaderDocTextList)
            {
                if(!TextUtils.isEmpty(strHeaderDocText))
                {
                    strHeaderDocText += "\n";
                }
                strHeaderDocText += info.strDocText;
            }
            mObjectMap.put("Header", strHeaderDocText);
        }

        if(!bFutureMode)
        {
            mObjectMap.put("A4InvoiceTitle", "Tax Invoice");
            mObjectMap.put("A4DocketTitle", "Delivery Docket");
            mObjectMap.put("InvoiceNumberLabel", "Invoice Number");
            mObjectMap.put("DocketNumberLabel", "Docket Number");
        }
        else
        {
            mObjectMap.put("A4InvoiceTitle", "Order Confirmation");
            mObjectMap.put("A4DocketTitle", "Order Confirmation");
            mObjectMap.put("InvoiceNumberLabel", "Order Number");
            mObjectMap.put("DocketNumberLabel", "Order Number");
        }

        if(bReservedMode)
        {
            mObjectMap.put("InvoiceTitle", "TAX ADJUSTMENT");
        }
        else {
            mObjectMap.put("InvoiceTitle", "TAX INVOICE");
        }

        //String strDescription = Utils.getStringSetting(context, Global.PREF_USER_DESCRIPTION);
        //mObjectMap.put("SalesRep", strDescription);

        String strDocType = "DOC TYPE: " + "";
        mObjectMap.put("DocType", strDocType);

        //Invoice No
        String strKKInvoiceNo = strInvoiceNoPrefix + Global.gUOrderHInfo.strDocNo;
        String strInvoiceNo = Global.gUOrderHInfo.strDocNo;
        // String strDeliveryNo = "DELIVERY# " + strInvoiceNo;
        String strDeliveryNo = strInvoiceNo;
        mObjectMap.put("DeliveryNo", "# " + strDeliveryNo);

        if (Utils.getStringSetting(context, Global.PREF_ROUTE_NUMBER_HAS_SUFFIX).equals("1")) {
            //dbAdapter.open();
            List<DescTypeInfo> invoiceNoSuffixInfoList = dbAdapter.getDescTypeListWithNumericKey("RouteNumber", Utils.getIntFromString(Global.gRouteNumber));
            String strInvoiceNoSuffix = "";
            if(invoiceNoSuffixInfoList != null && invoiceNoSuffixInfoList.size() > 0)
            {
                strInvoiceNoSuffix = invoiceNoSuffixInfoList.get(0).strValue1;
            }
            //dbAdapter.close();
            mObjectMap.put("InvoiceNo", strInvoiceNo.length() > 0 ? strInvoiceNo+strInvoiceNoSuffix : " ");
        }
        else {
            mObjectMap.put("InvoiceNo", strInvoiceNo.length() > 0 ? strInvoiceNo : " ");
        }

        mObjectMap.put("KKInvoiceNo", strKKInvoiceNo.length() > 0 ? strKKInvoiceNo : " ");
        mObjectMap.put("InvoiceNoWithNoLeading", Global.gUOrderHInfo.strDocNo.length() > 0 ? Global.gUOrderHInfo.strDocNo.replaceFirst("^0+(?!$)", "") : " ");

        //Invoice 10RR format No
        String strRouteNo = Global.gRouteNumber;
        if(strRouteNo.length() < 2)
        {
            strRouteNo = String.format(Locale.ENGLISH, "%02d", Utils.parseIntFromString(strRouteNo));
        }
        else
        {
            strRouteNo = strRouteNo.substring(strRouteNo.length() - 2, strRouteNo.length());
        }
        String str10RRFormatNo = "I0" + strRouteNo + (Global.gUOrderHInfo.strDocNo.length() > 0 ? Global.gUOrderHInfo.strDocNo : "");
        mObjectMap.put("InvoiceI0RRNo", str10RRFormatNo);

        //PoNo
        mObjectMap.put("PONo", Global.gUOrderHInfo.strPOReference.length() > 0 ? Global.gUOrderHInfo.strPOReference : " ");

        String date = "";
        date = Utils.getRoutectlTripDate(context, Global.DATE_YYYYMMDD_FORMAT, Global.DATE_DD_MM_YYYY_FORMAT, Global.gUOrderHInfo.strTrxnDate);

        mObjectMap.put("InvoiceDate", date.length() > 0 ? date: " ");

        mObjectMap.put("WholeDeliveryDate", "INVOICE DATE: " + date);

        String strSalesDate = "SALES DATE " + date;
        mObjectMap.put("SalesDate", strSalesDate);

        SimpleDateFormat df = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT);
        SimpleDateFormat tdf = new SimpleDateFormat(Global.TIME_HH_MM_FORMAT);
        String time = "";
        try{
            time = tdf.format((df.parse(Global.gUOrderHInfo.strTrxnTime)));
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        //Invoice Time
        mObjectMap.put("Time", time.length() > 0 ? time : " ");
        mObjectMap.put("WholeTime", "TIME: " + time);

        //Delivery Date
        date = Utils.getRoutectlTripDate(context, Global.DATE_YYYYMMDD_FORMAT, Global.DATE_DD_MM_YYYY_FORMAT, Global.gUOrderHInfo.strDeliveryDate);
        mObjectMap.put("DeliveryDate", date.length() > 0 ? date: " ");

        //Terms
        mObjectMap.put("Terms", mCustomerInfo.strTerms.length() > 0 ? mCustomerInfo.strTerms : " ");

        //InvoiceCustomerCode
        if( mCustomerInfo.strAltCustNo.equals("") )
        {
            if( mCustomerInfo.nChainNo == 0 )
            {
                mObjectMap.put("InvoiceCustomerCode", String.valueOf(mCustomerInfo.nCustNo));
            }
            else
            {
                mObjectMap.put("InvoiceCustomerCode", mCustomerInfo.nChainNo + "/" + mCustomerInfo.nCustNo);
            }
        }
        else
        {
            mObjectMap.put("InvoiceCustomerCode", String.valueOf(mCustomerInfo.strAltCustNo));
        }

        //VS182 -- 8/14, 2019 From Andrew.
        mObjectMap.put("CustNoOnly", String.valueOf(Global.gCurCustomerInfo.nCustNo));

        String strCustNo = "";

        if( Global.gCurCustomerInfo.nChainNo == 0 )
        {
            strCustNo = String.valueOf(Global.gCurCustomerInfo.nCustNo);
        }
        else
        {
            strCustNo = Global.gCurCustomerInfo.nChainNo + "/" + Global.gCurCustomerInfo.nCustNo;
        }
        mObjectMap.put("CRNumber", Global.gCurCustomerInfo.strLegalName);
        mObjectMap.put("CustNo", strCustNo);
        mObjectMap.put("AltCustNo", Global.gCurCustomerInfo.strAltCustNo); //--VS182

        mObjectMap.put("EngCustNo", "CUST# " + Global.gCurCustomerInfo.strAltCustNo);

        // Get arab cust no


        //InvoiceTo1
        String strInvoiceTo1 = mCustomerInfo.strName.length() > 0 ? mCustomerInfo.strName : " ";
        mObjectMap.put("InvoiceTo1", strInvoiceTo1);

        // Get details from InfoType = 52 and CustNo
        String additionalInfo = "";
        List<CustomerAddInfo> addInfoList = dbAdapter.getCustAddInfoList(mCustomerInfo.nCustNo, mCustomerInfo.nChainNo, "52");
        if (!addInfoList.isEmpty()) {
            additionalInfo = addInfoList.get(0).strInfo;
        }
        if (additionalInfo.isEmpty()) {
            mObjectMap.put("ArabCustNo", " ");
        }
        else {
            mObjectMap.put("ArabCustNo", additionalInfo);
        }
//        if (additionalInfo.isEmpty()) {
//            mObjectMap.put("ArabCustNo", "سم العميل");
//        }
//        else {
//            mObjectMap.put("ArabCustNo", "سم العميل" + " " + additionalInfo);
//        }

        String strBillAddr = "";
        if (!mCustomerInfo.strBillAddr1.isEmpty()) {
            strBillAddr = mCustomerInfo.strBillAddr1;
        }

        if (!mCustomerInfo.strBillAddr2.isEmpty()) {
            if (!strBillAddr.isEmpty()) {
                strBillAddr += " ";
                strBillAddr += mCustomerInfo.strBillAddr2;
            }
            else {
                strBillAddr = mCustomerInfo.strBillAddr2;
            }
        }

        if (!mCustomerInfo.strBillCity.isEmpty()) {
            if (!strBillAddr.isEmpty()) {
                strBillAddr += " ";
                strBillAddr += mCustomerInfo.strBillCity;
            }
            else {
                strBillAddr = mCustomerInfo.strBillCity;
            }
        }

        mObjectMap.put("ArabInvoiceTo1", strBillAddr);

        String strAddr = mCustomerInfo.strAddress1.length() > 0 ? mCustomerInfo.strAddress1 : " ";

        mObjectMap.put("InvoiceTo2", strAddr);

        String strAddressLines = "";
        String strLine1 = mCustomerInfo.strAddress1.length() > 0 ? mCustomerInfo.strAddress1 : " ";
        strAddressLines += strLine1;
        strAddressLines += "\n";
        String strLine2 = mCustomerInfo.strAddress2;
        if (strLine2.length() > 0) {
            strAddressLines += strLine2;
            strAddressLines += "\n";
        }
        String strLine3 = mCustomerInfo.strCity + " " + mCustomerInfo.strShipToState + " " + mCustomerInfo.strShipToZip;
        strAddressLines += strLine3;

        mObjectMap.put("AddressLines", strAddressLines);

        mObjectMap.put("EngAddress", "ADDRESS : " + strInvoiceTo1);

        strAddr = mCustomerInfo.strAddress2.length() > 0 ? mCustomerInfo.strAddress2 : "";
        mObjectMap.put("InvoiceTo4", strAddr);

        mObjectMap.put("InvoiceTo3", mCustomerInfo.strCity + " " + mCustomerInfo.strShipToState + " " + mCustomerInfo.strShipToZip);

        mObjectMap.put("GSTNumber", "Your GST Number " + mCustomerInfo.strCompanyTaxID);

        mObjectMap.put("CustTaxID", mCustomerInfo.strCompanyTaxID);
        mObjectMap.put("CompTaxID", mCustomerInfo.strCompanyTaxID);

        mObjectMap.put("DeliverAccount", mCustomerInfo.strBillName);

        mObjectMap.put("PrefDriverNo", ": " + Utils.getStringSetting(context, Global.PREF_DRIVER_NUMBER));
        mObjectMap.put("PrefDriverName", "- " + Utils.getStringSetting(context, Global.PREF_USER_DESCRIPTION));

        if(mCustomerInfo.nBillCustNo != 0)
        {
            mObjectMap.put("DeliveryTo", "Deliver To :");
            mObjectMap.put("AccountTo", "Account To");
            if( mCustomerInfo.nBillChainNo == 0 )
            {
                mObjectMap.put("BillCustomerCode", String.valueOf(mCustomerInfo.nBillCustNo));
            }
            else
            {
                mObjectMap.put("BillCustomerCode", mCustomerInfo.nBillChainNo + "/" + mCustomerInfo.nBillCustNo);
            }

            //InvoiceTo1
            mObjectMap.put("BillInvoiceTo1", mCustomerInfo.strBillName.length() > 0 ? mCustomerInfo.strBillName : " ");

            strAddr = mCustomerInfo.strBillAddr1.length() > 0 ? mCustomerInfo.strBillAddr1 : " ";

            mObjectMap.put("BillInvoiceTo2", strAddr);

            strAddr = mCustomerInfo.strBillAddr2.length() > 0 ? mCustomerInfo.strBillAddr2 : "";
            if(!TextUtils.isEmpty(strAddr))
            {
                mObjectMap.put("BillInvoiceTo4", strAddr);
            }

            mObjectMap.put("BillInvoiceTo3", mCustomerInfo.strBillCity + " " + mCustomerInfo.strState + " " + mCustomerInfo.strZip);
        }
        else
        {
            mObjectMap.put("DeliveryTo", "");
            mObjectMap.put("AccountTo", " ");
            mObjectMap.put("BillInvoiceTo1", " ");
            mObjectMap.put("BillInvoiceTo2", " ");
            mObjectMap.put("BillInvoiceTo3", " ");
            mObjectMap.put("BillInvoiceTo4", " ");
        }

        if (mCustomerInfo.strPhone.length() > 0)
        {
            mObjectMap.put("Phone", mCustomerInfo.strPhone);
        }
        else
        {
            mObjectMap.put("Phone", " ");
        }

        if(mCustomerInfo.strStoreNo.length() > 0)
        {
            mObjectMap.put("StoreNo", mCustomerInfo.strStoreNo);
        }
        else
        {
            mObjectMap.put("StoreNo", " ");
        }

        if(mCustomerInfo.strVendorNo.length() > 0)
        {
            mObjectMap.put("VendorNo", mCustomerInfo.strVendorNo);
        }
        else
        {
            mObjectMap.put("VendorNo", " ");
        }

        if(mCustomerInfo.strTerms.length() > 0)
        {
            dbAdapter = new DbAdapter(context);
            dbAdapter.open();
            List<DescTypeInfo> paymentTypeDescTypeList = dbAdapter.getDescTypeListWithAlphaKey(Global.TERMS, mCustomerInfo.strTerms);
            dbAdapter.close();
            if(paymentTypeDescTypeList.size() != 0)
            {
                String strTerms = "";
                for(DescTypeInfo descTypeInfo : paymentTypeDescTypeList)
                {
                    if(strTerms.length() > 0)
                    {
                        strTerms += "\n";
                    }
                    strTerms += descTypeInfo.strDescription.trim();
                }
                mObjectMap.put("BillTerms", strTerms);
            }
            else
            {
                mObjectMap.put("BillTerms", " ");
            }

        }
        else
        {
            mObjectMap.put("BillTerms", " ");
        }

        mObjectMap.put("RouteNo", Global.gRouteNumber);
        mObjectMap.put("SalesRepresentative", Utils.getStringSetting(context, Global.PREF_USER_NAME));

        mObjectMap.put("PrefRouteNo", ": " + Global.gRouteNumber);
        mObjectMap.put("PrefRouteNo1", "- 0");

        /*mObjectMap.put("DeliverTo1", Global.gCurCustomerInfo.);
        mObjectMap.put("DeliverTo2", "130 Nepean Highway");
        mObjectMap.put("DeliverTo3", "Frankston VIC 3199");*/

        mObjectMap.put("PreviousAccountBalance", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Global.gCurPreviousBalance));

        List<UOrderDInfo> deliveryList = new ArrayList<>();
        Map<Integer, List<UOrderDInfo>> freeListMap = new HashMap<>();
        List<UOrderDInfo> replaceList = new ArrayList<>();
        List<UOrderDInfo> containerList = new ArrayList<>();
        List<UOrderDInfo> buybackList = new ArrayList<>();

        List<Map<String, String>> A4DeliveryList = new ArrayList<>();
        List<Map<String, String>> A4DumpsList = new ArrayList<>();
        List<Map<String, String>> A4BuybacksList = new ArrayList<>();
        List<Map<String, String>> A4ContainerList = new ArrayList<>();

        List<Map<String, String>> A4ContainerDeliveredList = new ArrayList<>();
        List<Map<String, String>> A4ContainerReturnedList = new ArrayList<>();
        mObjectMap.put("A4Delivery", A4DeliveryList);
        mObjectMap.put("A4Dumps", A4DumpsList);
        mObjectMap.put("A4Buybacks", A4BuybacksList);
        mObjectMap.put("A4Containers", A4ContainerList);
        mObjectMap.put("A4ContainersDelivered", A4ContainerDeliveredList);
        mObjectMap.put("A4ContainersReturned", A4ContainerReturnedList);

        for(UOrderDInfo infoItem : mPrintList)
        {
            UOrderDInfo info = infoItem.duplicate();

            if(info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM)
            {
                if(info.nTrxnType == Global.TRXN_DELIVER)
                {
                    deliveryList.add(info);
                    A4DeliveryList.add(getA4ItemMap(context, info));
                    //Get FreeItem
                    dbAdapter = new DbAdapter(context);
                    dbAdapter.open();
                    List<UOrderDInfo> uOrderDList = dbAdapter.getUOrderDList(info.nTrxnNo);
                    dbAdapter.close();

                    for(UOrderDInfo uOrderDInfo : uOrderDList)
                    {
                        if (uOrderDInfo.nPromoType == Global.PROMO_TYPE_BUY_FREE_MULTI && uOrderDInfo.strItemNo.equals(info.strItemNo)) {
                            uOrderDInfo.nTrxnType = 12;
                            uOrderDInfo.nQuantity = uOrderDInfo.nPromoDiscount == 0 ? 0 : Math.round(Utils.myDivider(uOrderDInfo.nQuantity, uOrderDInfo.nPromoDiscount));
                            uOrderDInfo.nBasePrice = 0;
                            uOrderDInfo.nRetailPrice = 0;
                            uOrderDInfo.nTaxAmount = 0;
                            uOrderDInfo.nPrice = 0;
                            for(UTaxInfo uTaxInfo : uOrderDInfo.uTaxInfoList)
                            {
                                uTaxInfo.nTrxnNo = uOrderDInfo.nTrxnNo;
                                uTaxInfo.nTrxnType = uOrderDInfo.nTrxnType;
                            }
                            deliveryList.add(uOrderDInfo);
                            A4DeliveryList.add(getA4ItemMap(context, uOrderDInfo));
                        }
                    }
                }
                else if(info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE){
                    List<UOrderDInfo> freeList = freeListMap.get(info.nReasonCode);
                    if(freeList == null)
                    {
                        freeList = new ArrayList<>();
                        freeListMap.put(info.nReasonCode, freeList);
                    }
                    A4DeliveryList.add(getA4ItemMap(context, info));

                    // we should update free order price, base price, taxAmount with the corresponding delivery
                    boolean bFoundDelivery = false;
                    for(UOrderDInfo info1 : mPrintList) {
                        if(info1.nSCWType != Global.INVENTORY_TRUCK && info1.nSCWType != Global.WEIGHT_ITEM) {
                            continue;
                        }
                        if(info1.nTrxnType != Global.TRXN_DELIVER) {
                            continue;
                        }
                        if (info.strItemNo.equals(info1.strItemNo)) {
                            info.nPrice = info1.nPrice;
                            info.nBasePrice = info1.nBasePrice;
                            info.nDiscountVal = info1.nDiscountVal;

                            // Calculate Tax Amount for Promotion
                            List<TaxRatesInfo> taxRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, info.strItemNo);
                            ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
                            info.nTaxAmount = 0;
                            info.nPrintTaxAmount = 0;
                            boolean bAppliedGST = false;
                            float fGSTOnCDS = 0.0f;
                            if(taxRatesInfoList != null)
                            {
                                for(TaxRatesInfo taxRatesInfo : taxRatesInfoList)
                                {
                                    float nTaxAmount = 0;
                                    float nPrintTaxAmount = 0;

                                    float nAmount;
                                    if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
                                    {
                                        nAmount = (float)info.dQuantity;
                                    }
                                    else
                                    {
                                        nAmount = info.nQuantity;
                                    }

                                    if(taxRatesInfo.nTaxMethod == 1) //Applying Cents Amount
                                    {
                                        nTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * nAmount * prodDetlInfo.nConsumerUnit;
                                        nPrintTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * nAmount * prodDetlInfo.nConsumerUnit;
                                    }
                                    else if( taxRatesInfo.nTaxMethod == 2 )
                                    {
                                        nTaxAmount = (nAmount * (info.nBasePrice - info.nDiscountVal) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate) ;
                                        nPrintTaxAmount = (nAmount * (info.nBasePrice) + info.nPrintTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate) ;
                                    }

                                    if(taxRatesInfo.strTaxType.equals("S"))
                                    {
                                        List<TaxRatesInfo> gstOnCDSRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, taxRatesInfo.strTaxRateNumber);
                                        for(TaxRatesInfo gstOnCDSRatesInfo : gstOnCDSRatesInfoList)
                                        {
                                            if(gstOnCDSRatesInfo.nTaxMethod == 2 && gstOnCDSRatesInfo.strTaxRateCode.equals("GST") )
                                            {
                                                fGSTOnCDS += Utils.getDividedValue(taxRatesInfo.nTaxRate) / 100 * Utils.getDividedValue(gstOnCDSRatesInfo.nTaxRate) * nAmount * prodDetlInfo.nConsumerUnit;
                                            }
                                        }
                                    }
                                    info.nTaxAmount += Utils.get5RoundedValue(context, nTaxAmount);
                                    info.nPrintTaxAmount += Utils.get5RoundedValue(context, nPrintTaxAmount);
                                    if(taxRatesInfo.strTaxRateCode.equals("GST"))
                                    {
                                        bAppliedGST = true;
                                    }
                                }
                            }

                            if(!bAppliedGST)
                            {
                                info.nTaxAmount += Utils.get5RoundedValue(context, fGSTOnCDS);
                                info.nPrintTaxAmount += Utils.get5RoundedValue(context, fGSTOnCDS);
                            }

                            bFoundDelivery = true;
                            break;
                        }
                    }
                    //if (bFoundDelivery)
                    freeList.add(info);
                }
            }
            if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) && info.nTrxnType == Global.TRXN_PICKUP)
            {
                replaceList.add(info);
                Map<String, String> itemMap = getA4ItemMap(context, info);
                A4DumpsList.add(itemMap);

                if( info.nTrxnType == Global.TRXN_CONTAINER_SALE)
                {
                    A4ContainerDeliveredList.add(itemMap);
                }
                else
                {
                    A4ContainerReturnedList.add(itemMap);
                }
            }
            if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) && info.nTrxnType == Global.TRXN_BUYBACK)
            {
                buybackList.add(info);
                Map<String, String> itemMap = getA4ItemMap(context, info);
                A4BuybacksList.add(itemMap);

                if( info.nTrxnType == Global.TRXN_CONTAINER_SALE)
                {
                    A4ContainerDeliveredList.add(itemMap);
                }
                else
                {
                    A4ContainerReturnedList.add(itemMap);
                }
            }
            if (info.nSCWType == Global.INVENTORY_CONTAINERS)
            {
                containerList.add(info);
                A4ContainerList.add(getA4ItemMap(context, info));
            }
        }

        boolean isReturnMode = false;
        if (deliveryList.size() == 0) {
            isReturnMode = true;
        }
        else {
            isReturnMode = false;
        }

        if (!isReturnMode) {
            mObjectMap.put("InvoiceCaption", "TAX INVOICE");
            mObjectMap.put("ArabInvoiceCaption", "فاتورة ضريبية");
        }
        else {
            mObjectMap.put("InvoiceCaption", "CREDIT NOTE");
            mObjectMap.put("ArabInvoiceCaption", "إشعار دائن");
        }

        List<UOrderDInfo> itemList = new ArrayList<>();
        Map<String, UOrderDInfo> itemMap = new HashMap<>();
        int nDeliveryCount = 0;
        int nCreditCount = 0;
        double dDeliveryValue = 0;
        double dCreditValue = 0;
        float nSalesAmount = 0;
        double dDiscountAmount = 0;

        float salesNetAmount = 0;
        float salesTaxAmount = 0;
        float salesTotalAmount = 0;
        float freeNetAmount = 0;
        float freeTaxAmount = 0;
        float freeTotalAmount = 0;
        float creditsNetAmount = 0;
        float creditsTaxAmount = 0;
        float creditsTotalAmount = 0;
        float buybacksNetAmount = 0;
        float buybacksTaxAmount = 0;
        float buybacksTotalAmount = 0;

        float extraDiscountAmount = 0;
        float extraDiscountTaxAmount = 0;

        boolean hasExtraDiscount = false;

        boolean isSeparatePercentOffPromo = Utils.getStringSetting(context, Global.PREF_SEPARATE_PERCENT_OFF_PROMO).equals("1");

        if( deliveryList.size() > 0 )
        {
            List<Map<String, String>> deliveryMapList = new ArrayList<>();
            Collections.sort(deliveryList, new Comparator<UOrderDInfo>() {
                @Override
                public int compare(UOrderDInfo t1, UOrderDInfo t2) {
                    return t1.strItemNo.compareTo(t2.strItemNo);
                }
            });

            int nTotalSales = 0;
            float dTotalSales = 0;
            float dTotalSaleUnitPrice = 0;
            float dTotalSaleDiscAmount = 0;
            float dTotalSaleNetAmount = 0;
            float dTotalTaxAmount = 0;

            int nTotalCaseQty = 0;
            int nTotalUnitQty = 0;
            int nTotalTotalQty = 0;

            for(UOrderDInfo info : deliveryList)
            {
                Map<String, String> deliveryMap = new HashMap<>();
                deliveryMap.put("code", Utils.getDisplayItemCode(context, info.strItemNo));
                deliveryMap.put("upccode", info.strItemUPC);
                if(info.uLoadBlotInfo.size() > 0)
                {
                    String strLotNumbers = "";
                    for(LoadBlotInfo loadBlotInfo : info.uLoadBlotInfo)
                    {
                        if(TextUtils.isEmpty(strLotNumbers))
                        {
                            strLotNumbers = loadBlotInfo.strLotNumber;
                        }
                        else
                        {
                            strLotNumbers += ", " + loadBlotInfo.strLotNumber;
                        }
                    }
                    deliveryMap.put("description", info.strDescription + "\n" + strLotNumbers);
                    deliveryMap.put("short_desc", info.strShortDesc + "\n" + strLotNumbers);
                }
                else
                {
                    deliveryMap.put("description", info.strDescription);
                    deliveryMap.put("short_desc", info.strShortDesc);
                }

                ProdInfo prodInfo = mProdInfoMap.get(info.strItemNo);
                if (prodInfo != null) {
                    deliveryMap.put("arab_description", prodInfo.strInfoDetail);
                }
                else {
                    deliveryMap.put("arab_description", " ");
                }

                deliveryMap.put("upc", String.valueOf(info.nCaseFactor));

                PriceGrpInfo priceGrpInfo = priceGrpInfoMap.get(info.strItemNo);
                if(priceGrpInfo == null)
                {
                    priceGrpInfo = altPriceGrpInfoMap.get(info.strItemNo);
                }

                if(priceGrpInfo != null)
                {
                    deliveryMap.put("price_grp", String.format(Locale.ENGLISH, "%.2f%%", Utils.getDividedValue(priceGrpInfo.nPrice)));
                }
                else
                {
                    deliveryMap.put("price_grp", " ");
                }

                if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
                {
                    deliveryMap.put("group", info.strProdGroup);
                    deliveryMap.put("unit_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice));
                    deliveryMap.put("disc_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", (info.nBasePrice - info.nPrice) * info.dQuantity));
                    deliveryMap.put("net_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nPrice * info.dQuantity));

                    deliveryMap.put("qty", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(info.dQuantity)));
                    deliveryMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(info.dQuantity)));
                    deliveryMap.put("qty_un", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(info.dQuantity)));

                    deliveryMap.put("case_qty", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(0))+"/");
                    deliveryMap.put("unit_qty", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(info.dQuantity)));
                    deliveryMap.put("total_qty", String.format(Locale.ENGLISH, "+%.2f", Utils.getRoundedValue(info.dQuantity)));

                    dTotalSales += info.dQuantity;
                    dTotalSaleUnitPrice += Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice * info.dQuantity));

                    float nPrice = info.nPrice;
                    float extraDiscount = 0;
                    for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
                    {
                        if( uPromotionInfo.nPromoAppMethod == 4 )
                            nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);

                        if (isSeparatePercentOffPromo) {
                            if( uPromotionInfo.nPromoType == Global.PROMO_TYPE_PERCENTAGE_OFF) {
                                extraDiscount += Utils.getDividedValue(-1*uPromotionInfo.nAmount) * info.dQuantity;
                                extraDiscountAmount += extraDiscount;
                            }
                        }
                    }

                    deliveryMap.put("price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
                    deliveryMap.put("total", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice * info.dQuantity)));

                    float nBasePrice = info.nBasePrice;
                    deliveryMap.put("pc_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));
                    deliveryMap.put("cs_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));

                    double discountAmount = 0;
                    if(info.prodLocnInfo != null)
                    {
                        discountAmount = (nPrice - Utils.getDividedValue(info.prodLocnInfo.nBasePrice)) * info.dQuantity - extraDiscount;
                    }
                    else
                    {
                        discountAmount = (nPrice - info.nBasePrice) * info.dQuantity - extraDiscount;
                    }
                    dTotalSaleDiscAmount += discountAmount;
                    deliveryMap.put("g_disc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", discountAmount));

                    double totalsAmount = Utils.getRoundedValue(nBasePrice * info.dQuantity) + discountAmount;
                    dTotalSaleNetAmount += totalsAmount;
                    deliveryMap.put("totals", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", totalsAmount));

                    float disc = (info.nBasePrice - nPrice) / info.nBasePrice * 100;

                    float extraDiscountTax = Utils.getRoundedValue(extraDiscount * (float)(info.nTaxAmount / (info.dQuantity * info.nPrice)));
                    extraDiscountTaxAmount += extraDiscountTax;

                    float taxAmount = info.nTaxAmount - extraDiscountTax;
                    dTotalTaxAmount += taxAmount;

                    long nTaxRate = 0;
                    try {
                        nTaxRate = Math.round(taxAmount / totalsAmount * 100);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    deliveryMap.put("total_tax", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f" + "(%d%%)", taxAmount, nTaxRate));

                    float totalWithTax = (float)totalsAmount + taxAmount;
                    deliveryMap.put("totalWithTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalWithTax)));
                    deliveryMap.put("totalNoTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalsAmount)));
                    deliveryMap.put("total_amount", String.format(Locale.ENGLISH, "+%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalWithTax)));
                    nSalesAmount += totalsAmount;

                    deliveryMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                    deliveryMap.put("disc", String.format(Locale.ENGLISH, "%.2f%%", disc));
                    deliveryMapList.add(deliveryMap);
                }
                else
                {
                    deliveryMap.put("group", info.strProdGroup);
                    deliveryMap.put("unit_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice));
                    deliveryMap.put("disc_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", (info.nBasePrice - info.nPrice) * info.nQuantity));
                    deliveryMap.put("net_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nPrice * info.nQuantity));

                    nTotalSales += info.nQuantity;
                    dTotalSaleUnitPrice += Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice * info.nQuantity));

                    if(bShowCase)
                    {
                        int nCase = getCaseValue(info.strItemNo, context);
                        int nCaseValue = info.nQuantity / nCase;
                        int nQty = info.nQuantity % nCase;
                        deliveryMap.put("qty", nCaseValue + "/" + nQty);
                        deliveryMap.put("qty_cs_un", nCaseValue + "/" + nQty);
                        deliveryMap.put("qty_un", String.valueOf(info.nQuantity));

                        deliveryMap.put("case_qty", nCaseValue + "/");
                        deliveryMap.put("unit_qty", String.valueOf(nQty));
                        deliveryMap.put("total_qty", "+" + info.nQuantity);

                        nTotalCaseQty += nCaseValue;
                        nTotalUnitQty += nQty;
                        nTotalTotalQty += info.nQuantity;

                        float nPrice = info.nPrice;
                        float extraDiscount = 0;
                        for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
                        {
                            if( uPromotionInfo.nPromoAppMethod == 4 )
                                nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);

                            if (isSeparatePercentOffPromo) {
                                if( uPromotionInfo.nPromoType == Global.PROMO_TYPE_PERCENTAGE_OFF) {
                                    extraDiscount += Utils.getDividedValue(-1*uPromotionInfo.nAmount) * info.nQuantity;
                                    extraDiscountAmount += extraDiscount;
                                }
                            }
                        }

                        deliveryMap.put("price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
                        deliveryMap.put("total", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice * info.nQuantity)));

                        float nBasePrice = info.nBasePrice;
                        deliveryMap.put("pc_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));
                        deliveryMap.put("cs_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice * nCase)));

                        double discountAmount = 0;
                        if(info.prodLocnInfo != null)
                        {
                            discountAmount = (nPrice - Utils.getDividedValue(info.prodLocnInfo.nBasePrice)) * info.nQuantity - extraDiscount;
                        }
                        else
                        {
                            discountAmount = (nPrice - info.nBasePrice) * info.nQuantity - extraDiscount;
                        }
                        dTotalSaleDiscAmount += discountAmount;
                        deliveryMap.put("g_disc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", discountAmount));

                        double totalsAmount = Utils.getRoundedValue(nBasePrice * info.nQuantity) + discountAmount;
                        dTotalSaleNetAmount += totalsAmount;
                        deliveryMap.put("totals", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", totalsAmount));

                        float disc = (info.nBasePrice - nPrice) / info.nBasePrice * 100;

                        float extraDiscountTax = Utils.getRoundedValue(extraDiscount * (float)(info.nTaxAmount / (info.nQuantity * info.nPrice)));
                        extraDiscountTaxAmount += extraDiscountTax;

                        float taxAmount = info.nTaxAmount - extraDiscountTax;
                        dTotalTaxAmount += taxAmount;

                        long nTaxRate = 0;
                        try {
                            nTaxRate = Math.round(taxAmount / totalsAmount * 100);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        deliveryMap.put("total_tax", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f" + "(%d%%)", taxAmount, nTaxRate));

                        float totalWithTax = (float)totalsAmount + taxAmount;
                        deliveryMap.put("totalWithTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalWithTax)));
                        deliveryMap.put("totalNoTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalsAmount)));
                        deliveryMap.put("total_amount", String.format(Locale.ENGLISH, "+%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalWithTax)));
                        nSalesAmount += totalsAmount;

                        deliveryMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                        deliveryMap.put("disc", String.format(Locale.ENGLISH, "%.2f%%", disc));
                        deliveryMapList.add(deliveryMap);
                    }
                    else
                    {
                        int nQty = info.nQuantity;
                        deliveryMap.put("qty", String.valueOf(nQty));
                        deliveryMap.put("qty_cs_un", String.valueOf(nQty));
                        deliveryMap.put("qty_un", String.valueOf(info.nQuantity));

                        deliveryMap.put("case_qty", "0/");
                        deliveryMap.put("unit_qty", String.valueOf(nQty));
                        deliveryMap.put("total_qty", "+" + nQty);

                        nTotalUnitQty += info.nQuantity;
                        nTotalTotalQty += info.nQuantity;

                        float nPrice = info.nPrice;
                        float extraDiscount = 0;
                        for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
                        {
                            if( uPromotionInfo.nPromoAppMethod == 4 )
                                nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);

                            if (isSeparatePercentOffPromo) {
                                if( uPromotionInfo.nPromoType == Global.PROMO_TYPE_PERCENTAGE_OFF) {
                                    extraDiscount += Utils.getDividedValue(-1*uPromotionInfo.nAmount) * info.nQuantity;
                                    extraDiscountAmount += extraDiscount;
                                }
                            }
                        }

                        deliveryMap.put("price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
                        deliveryMap.put("total", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice * info.nQuantity)));

                        float nBasePrice = info.nBasePrice;
                        deliveryMap.put("pc_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));
                        deliveryMap.put("cs_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));

                        double discountAmount = 0;
                        if(info.prodLocnInfo != null)
                        {
                            discountAmount = (nPrice - Utils.getDividedValue(info.prodLocnInfo.nBasePrice)) * info.nQuantity - extraDiscount;
                        }
                        else
                        {
                            discountAmount = (nPrice - info.nBasePrice) * info.nQuantity - extraDiscount;
                        }
                        dTotalSaleDiscAmount += discountAmount;
                        deliveryMap.put("g_disc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", discountAmount));

                        double totalsAmount = Utils.getRoundedValue(nBasePrice * info.nQuantity) + discountAmount;
                        dTotalSaleNetAmount += totalsAmount;
                        deliveryMap.put("totals", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", totalsAmount));

                        float disc = (info.nBasePrice - nPrice) / info.nBasePrice * 100;

                        float extraDiscountTax = Utils.getRoundedValue(extraDiscount * (float)(info.nTaxAmount / (info.nQuantity * info.nPrice)));
                        extraDiscountTaxAmount += extraDiscountTax;

                        float taxAmount = info.nTaxAmount - extraDiscountTax;
                        dTotalTaxAmount += taxAmount;

                        long nTaxRate = 0;
                        try {
                            nTaxRate = Math.round(taxAmount / totalsAmount * 100);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        deliveryMap.put("total_tax", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f" + "(%d%%)", taxAmount, nTaxRate));

                        float totalWithTax = (float)totalsAmount + taxAmount;
                        deliveryMap.put("totalWithTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalWithTax)));
                        deliveryMap.put("totalNoTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalsAmount)));
                        deliveryMap.put("total_amount", String.format(Locale.ENGLISH, "+%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalWithTax)));
                        nSalesAmount += totalsAmount;

                        deliveryMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                        deliveryMap.put("disc", String.format(Locale.ENGLISH, "%.2f%%", disc));
                        deliveryMapList.add(deliveryMap);
                    }
                }

                deliveryMap.put("base_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice));
                deliveryMap.put("disc_pas", String.format(Locale.ENGLISH, "%.2f%%", (info.nBasePrice - info.nPrice) / info.nBasePrice * 100));

                if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
                {
                    if(itemMap.get(info.strItemNo) == null)
                    {
                        UOrderDInfo itemInfo = info.duplicate();
                        itemInfo.dDeliveryCount = info.dQuantity;
                        itemList.add(itemInfo);
                        itemMap.put(info.strItemNo, itemInfo);
                        dDeliveryValue += info.dQuantity;
                    }
                    else
                    {
                        UOrderDInfo itemInfo = itemMap.get(info.strItemNo);
                        itemInfo.dDeliveryCount += info.dQuantity;
                        dDeliveryValue += info.dQuantity;
                    }
                }
                else
                {
                    if(itemMap.get(info.strItemNo) == null)
                    {
                        UOrderDInfo itemInfo = info.duplicate();
                        itemInfo.nDeliveryCount = info.nQuantity;
                        itemList.add(itemInfo);
                        itemMap.put(info.strItemNo, itemInfo);
                        nDeliveryCount += info.nQuantity;
                    }
                    else
                    {
                        UOrderDInfo itemInfo = itemMap.get(info.strItemNo);
                        itemInfo.nDeliveryCount += info.nQuantity;
                        nDeliveryCount += info.nQuantity;
                    }
                }
            }
            mObjectMap.put("Delivery", deliveryMapList);

            mObjectMap.put("sales_total_case_qty", nTotalCaseQty+"/");
            mObjectMap.put("sales_total_unit_qty", String.valueOf(nTotalUnitQty));
            mObjectMap.put("sales_total_total_qty", "+"+nTotalTotalQty);

            if(nTotalSales > 0 && dTotalSales > 0)
            {
                mObjectMap.put("total_sale_case", nTotalSales + "\n" + String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalSales));
            }
            else if(nTotalSales > 0){
                mObjectMap.put("total_sale_case", String.valueOf(nTotalSales));
            }
            else if(dTotalSales > 0){
                mObjectMap.put("total_sale_case", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalSales));
            }
            else {
                mObjectMap.put("total_sale_case", "0");
            }

            mObjectMap.put("total_sale_unit_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalSaleUnitPrice));
            mObjectMap.put("total_sale_disc_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*dTotalSaleDiscAmount));
            mObjectMap.put("total_sale_sign_disc_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalSaleDiscAmount));
            mObjectMap.put("total_sale_net_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalSaleNetAmount));

            mObjectMap.put("sales_total_g_disc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalSaleDiscAmount));
            dDiscountAmount += dTotalSaleDiscAmount;

            salesNetAmount = dTotalSaleNetAmount;
            salesTaxAmount = dTotalTaxAmount;
            salesTotalAmount = dTotalSaleNetAmount+dTotalTaxAmount;
        }

        mObjectMap.put("sales_total_totals", String.format(Locale.ENGLISH, "+%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", salesNetAmount));
        mObjectMap.put("sales_total_total_tax", String.format(Locale.ENGLISH, "+%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", salesTaxAmount));
        mObjectMap.put("sales_total_total_amount", String.format(Locale.ENGLISH, "+%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", salesTotalAmount));

        if( freeListMap.size() > 0 )
        {
            int nFreeCnt = 0;
            float dFreeCnt = 0;
            float dTotalFreeUnitPrice = 0;
            float dTotalFreeDiscAmount = 0;
            float dTotalFreeDiscAmountAsDelivery = 0;
            float dTotalFreeNetAmount = 0;
            float dTotalFreeNetAmountAsDelivery = 0;
            float dTotalFreeTaxAmount = 0;

            int nTotalCaseQty = 0;
            int nTotalUnitQty = 0;
            int nTotalTotalQty = 0;

            Iterator it = freeListMap.entrySet().iterator();
            List<Map<String, String>> freeMapList = new ArrayList<>();
            while(it.hasNext())
            {
                Map.Entry pair = (Map.Entry)it.next();
                Integer nReasonCode = (Integer)pair.getKey();
                DescTypeInfo descTypeInfo = descTypeInfoMap.get(nReasonCode);
                if(descTypeInfo != null)
                {
                    List<UOrderDInfo> freeList = (List<UOrderDInfo>)pair.getValue();
                    if(freeList.size() > 0)
                    {
                        Map<String, String> freeMap = new HashMap<>();
                        freeMapList.add(freeMap);
                        freeMap.put("title", descTypeInfo.strDescription);
                        freeMap.put("code_comment", "CODE");
                        freeMap.put("description_comment", "DESCRIPTION");
                        freeMap.put("case_comment", "CASE");
                        freeMap.put("price_comment", "PRICE");
                        freeMap.put("total_comment", "TOTAL");
                        freeMap.put("sort", "-1");
                    }
                    if(freeList != null && freeList.size() > 0)
                    {
                        Collections.sort(freeList, new Comparator<UOrderDInfo>() {
                            @Override
                            public int compare(UOrderDInfo t1, UOrderDInfo t2) {
                                return t1.strItemNo.compareTo(t2.strItemNo);
                            }
                        });
                    }

                    for(UOrderDInfo info : freeList)
                    {
                        Map<String, String> freeMap = new HashMap<>();
                        freeMap.put("code", Utils.getDisplayItemCode(context, info.strItemNo));
                        freeMap.put("upccode", info.strItemUPC);
                        if(info.uLoadBlotInfo.size() > 0)
                        {
                            String strLotNumbers = "";
                            for(LoadBlotInfo loadBlotInfo : info.uLoadBlotInfo)
                            {
                                if(TextUtils.isEmpty(strLotNumbers))
                                {
                                    strLotNumbers = loadBlotInfo.strLotNumber;
                                }
                                else
                                {
                                    strLotNumbers += ", " + loadBlotInfo.strLotNumber;
                                }
                            }
                            freeMap.put("description", info.strDescription + "\n" + strLotNumbers);
                            freeMap.put("short_desc", info.strShortDesc + "\n" + strLotNumbers);
                        }
                        else
                        {
                            freeMap.put("description", info.strDescription);
                            freeMap.put("short_desc", info.strShortDesc);
                        }

                        ProdInfo prodInfo = mProdInfoMap.get(info.strItemNo);
                        if (prodInfo != null) {
                            freeMap.put("arab_description", prodInfo.strInfoDetail);
                        }
                        else {
                            freeMap.put("arab_description", " ");
                        }

                        freeMap.put("upc", String.valueOf(info.nCaseFactor));

                        PriceGrpInfo priceGrpInfo = priceGrpInfoMap.get(info.strItemNo);
                        if(priceGrpInfo == null)
                        {
                            priceGrpInfo = altPriceGrpInfoMap.get(info.strItemNo);
                        }

                        if(priceGrpInfo != null)
                        {
                            freeMap.put("price_grp", String.format(Locale.ENGLISH, "%.2f%%", Utils.getDividedValue(priceGrpInfo.nPrice)));
                        }
                        else
                        {
                            freeMap.put("price_grp", " ");
                        }

                        freeMap.put("base_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice));
                        freeMap.put("disc_pas", String.format(Locale.ENGLISH, "%.2f%%", (info.nBasePrice - info.nPrice) / info.nBasePrice * 100));

                        if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
                        {
                            freeMap.put("group", info.strProdGroup);
                            freeMap.put("unit_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice));
                            freeMap.put("disc_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice * info.dQuantity));
                            freeMap.put("net_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", 0f));

                            dFreeCnt += info.dQuantity;
                            dTotalFreeUnitPrice += Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice * info.dQuantity));

                            freeMap.put("qty", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(info.dQuantity)));
                            freeMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(info.dQuantity)));
                            freeMap.put("qty_un", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(info.dQuantity)));

                            freeMap.put("case_qty", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(0))+"/");
                            freeMap.put("unit_qty", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(info.dQuantity)));
                            freeMap.put("total_qty", String.format(Locale.ENGLISH, "+%.2f", Utils.getRoundedValue(info.dQuantity)));

                            float nPrice = info.nPrice;
                            float extraDiscount = 0;
                            for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
                            {
                                if( uPromotionInfo.nPromoAppMethod == 4 )
                                    nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);
                            }

                            freeMap.put("price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
                            freeMap.put("total", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice * info.dQuantity)));
                            freeMap.put("gross_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nOrgGrossPrice)));

                            float nBasePrice = info.nBasePrice;
                            freeMap.put("pc_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));
                            freeMap.put("cs_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));

                            double discountAmount = 0;
                            discountAmount = -1 * info.nBasePrice * info.dQuantity;
                            dTotalFreeDiscAmount += discountAmount;

                            double totalsAmount = 0;
                            dTotalFreeNetAmount += totalsAmount;

                            double discountAmountAsDelivery = 0;
                            if(info.prodLocnInfo != null)
                            {
                                discountAmountAsDelivery = (nPrice - Utils.getDividedValue(info.prodLocnInfo.nBasePrice)) * info.dQuantity - extraDiscount;
                            }
                            else
                            {
                                discountAmountAsDelivery = (nPrice - info.nBasePrice) * info.dQuantity - extraDiscount;
                            }
                            dTotalFreeDiscAmountAsDelivery += discountAmountAsDelivery;

                            freeMap.put("g_disc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", discountAmountAsDelivery));

                            double totalsAmountAsDelivery = Utils.getRoundedValue(nBasePrice * info.dQuantity) + discountAmountAsDelivery;
                            dTotalFreeNetAmountAsDelivery += totalsAmountAsDelivery;
                            freeMap.put("totals", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", totalsAmountAsDelivery));

                            float disc = (info.nBasePrice - nPrice) / info.nBasePrice * 100;

                            float extraDiscountTax = Utils.getRoundedValue(extraDiscount * (float)(info.nTaxAmount / (info.dQuantity * info.nPrice)));
                            //extraDiscountTaxAmount += extraDiscountTax;

                            float taxAmount = info.nTaxAmount - extraDiscountTax;
                            dTotalFreeTaxAmount += taxAmount;

                            long nTaxRate = 0;
                            try {
                                nTaxRate = Math.round(taxAmount / (nPrice * info.dQuantity) * 100);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            freeMap.put("total_tax", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f" + "(%d%%)", taxAmount, nTaxRate));

                            float totalWithTax = (float)totalsAmount + taxAmount;
                            float totalWithTaxAsDelivery = (float)totalsAmountAsDelivery + taxAmount;
                            freeMap.put("totalWithTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalWithTax)));
                            freeMap.put("totalNoTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalsAmount)));
                            freeMap.put("total_amount", String.format(Locale.ENGLISH, "+%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalWithTaxAsDelivery)));
                            nSalesAmount += totalsAmount;

                            freeMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                            freeMap.put("disc", String.format(Locale.ENGLISH, "%.2f%%", disc));
                            freeMapList.add(freeMap);
                        }
                        else
                        {
                            freeMap.put("group", info.strProdGroup);
                            freeMap.put("unit_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice));
                            freeMap.put("disc_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice * info.nQuantity));
                            freeMap.put("net_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", 0f));

                            nFreeCnt += info.nQuantity;
                            dTotalFreeUnitPrice += Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice * info.nQuantity));

                            if(bShowCase)
                            {
                                int nCase = getCaseValue(info.strItemNo, context);
                                int nCaseValue = info.nQuantity / nCase;
                                int nQty = info.nQuantity % nCase;
                                freeMap.put("qty", nCaseValue + "/" + nQty);
                                freeMap.put("qty_cs_un", nCaseValue + "/" + nQty);
                                freeMap.put("qty_un", String.valueOf(info.nQuantity));

                                freeMap.put("case_qty", nCaseValue + "/");
                                freeMap.put("unit_qty", String.valueOf(nQty));
                                freeMap.put("total_qty", "+" + info.nQuantity);

                                nTotalCaseQty += nCaseValue;
                                nTotalUnitQty += nQty;
                                nTotalTotalQty += info.nQuantity;

                                float nPrice = info.nPrice;
                                float extraDiscount = 0;
                                for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
                                {
                                    if( uPromotionInfo.nPromoAppMethod == 4 )
                                        nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);
                                }

                                freeMap.put("price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
                                freeMap.put("total", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice * info.nQuantity)));
                                freeMap.put("gross_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nOrgGrossPrice)));

                                float nBasePrice = info.nBasePrice;
                                freeMap.put("pc_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));
                                freeMap.put("cs_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice * nCase)));

                                double discountAmount = 0;
                                discountAmount = -1 * info.nBasePrice * info.nQuantity;
                                dTotalFreeDiscAmount += discountAmount;

                                double totalsAmount = 0;
                                dTotalFreeNetAmount += totalsAmount;

                                double discountAmountAsDelivery = 0;
                                if(info.prodLocnInfo != null)
                                {
                                    discountAmountAsDelivery = (nPrice - Utils.getDividedValue(info.prodLocnInfo.nBasePrice)) * info.nQuantity - extraDiscount;
                                }
                                else
                                {
                                    discountAmountAsDelivery = (nPrice - info.nBasePrice) * info.nQuantity - extraDiscount;
                                }
                                dTotalFreeDiscAmountAsDelivery += discountAmountAsDelivery;

                                freeMap.put("g_disc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", discountAmountAsDelivery));

                                double totalsAmountAsDelivery = Utils.getRoundedValue(nBasePrice * info.nQuantity) + discountAmountAsDelivery;
                                dTotalFreeNetAmountAsDelivery += totalsAmountAsDelivery;
                                freeMap.put("totals", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", totalsAmountAsDelivery));

                                float disc = (info.nBasePrice - nPrice) / info.nBasePrice * 100;

                                float extraDiscountTax = Utils.getRoundedValue(extraDiscount * (float)(info.nTaxAmount / (info.nQuantity * info.nPrice)));
                                //extraDiscountTaxAmount += extraDiscountTax;

                                float taxAmount = info.nTaxAmount - extraDiscountTax;
                                dTotalFreeTaxAmount += taxAmount;

                                long nTaxRate = 0;
                                try {
                                    nTaxRate = Math.round(taxAmount / (nPrice * info.nQuantity) * 100);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                freeMap.put("total_tax", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f" + "(%d%%)", taxAmount, nTaxRate));

                                float totalWithTax = (float)totalsAmount + taxAmount;
                                float totalWithTaxAsDelivery = (float)totalsAmountAsDelivery + taxAmount;
                                freeMap.put("totalWithTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalWithTax)));
                                freeMap.put("totalNoTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalsAmount)));
                                freeMap.put("total_amount", String.format(Locale.ENGLISH, "+%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalWithTaxAsDelivery)));

                                freeMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                                freeMap.put("disc", String.format(Locale.ENGLISH, "%.2f%%", disc));
                                freeMapList.add(freeMap);
                            }
                            else
                            {
                                int nQty = info.nQuantity;
                                freeMap.put("qty", String.valueOf(nQty));
                                freeMap.put("qty_cs_un", String.valueOf(nQty));
                                freeMap.put("qty_un", String.valueOf(info.nQuantity));

                                freeMap.put("case_qty", "0/");
                                freeMap.put("unit_qty", String.valueOf(nQty));
                                freeMap.put("total_qty", "+" + nQty);

                                nTotalUnitQty += info.nQuantity;
                                nTotalTotalQty += info.nQuantity;

                                float nPrice = info.nPrice;
                                float extraDiscount = 0;
                                for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
                                {
                                    if( uPromotionInfo.nPromoAppMethod == 4 )
                                        nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);
                                }

                                freeMap.put("price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
                                freeMap.put("total", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice * info.nQuantity)));
                                freeMap.put("gross_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nOrgGrossPrice)));

                                float nBasePrice = info.nBasePrice;
                                freeMap.put("pc_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));
                                freeMap.put("cs_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));

                                double discountAmount = 0;
                                discountAmount = -1 * info.nBasePrice * info.nQuantity;
                                dTotalFreeDiscAmount += discountAmount;

                                double totalsAmount = 0;
                                dTotalFreeNetAmount += totalsAmount;

                                double discountAmountAsDelivery = 0;
                                if(info.prodLocnInfo != null)
                                {
                                    discountAmountAsDelivery = (nPrice - Utils.getDividedValue(info.prodLocnInfo.nBasePrice)) * info.nQuantity - extraDiscount;
                                }
                                else
                                {
                                    discountAmountAsDelivery = (nPrice - info.nBasePrice) * info.nQuantity - extraDiscount;
                                }
                                dTotalFreeDiscAmountAsDelivery += discountAmountAsDelivery;

                                freeMap.put("g_disc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", discountAmountAsDelivery));

                                double totalsAmountAsDelivery = Utils.getRoundedValue(nBasePrice * info.nQuantity) + discountAmountAsDelivery;
                                dTotalFreeNetAmountAsDelivery += totalsAmountAsDelivery;
                                freeMap.put("totals", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", totalsAmountAsDelivery));

                                float disc = (info.nBasePrice - nPrice) / info.nBasePrice * 100;

                                float extraDiscountTax = Utils.getRoundedValue(extraDiscount * (float)(info.nTaxAmount / (info.nQuantity * info.nPrice)));
                                // extraDiscountTaxAmount += extraDiscountTax;

                                float taxAmount = info.nTaxAmount - extraDiscountTax;
                                dTotalFreeTaxAmount += taxAmount;

                                long nTaxRate = 0;
                                try {
                                    nTaxRate = Math.round(taxAmount / (nPrice * info.nQuantity) * 100);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                freeMap.put("total_tax", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f" + "(%d%%)", taxAmount, nTaxRate));

                                float totalWithTax = (float)totalsAmount + taxAmount;
                                float totalWithTaxAsDelivery = (float)totalsAmountAsDelivery + taxAmount;
                                freeMap.put("totalWithTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalWithTax)));
                                freeMap.put("totalNoTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalsAmount)));
                                freeMap.put("total_amount", String.format(Locale.ENGLISH, "+%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalWithTaxAsDelivery)));

                                freeMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                                freeMap.put("disc", String.format(Locale.ENGLISH, "%.2f%%", disc));
                                freeMapList.add(freeMap);
                            }

                        }

                        if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
                        {
                            if(itemMap.get(info.strItemNo) == null)
                            {
                                UOrderDInfo itemInfo = info.duplicate();
                                itemInfo.dDeliveryCount = info.dQuantity;
                                itemList.add(itemInfo);
                                itemMap.put(info.strItemNo, itemInfo);
                                dDeliveryValue += info.dQuantity;
                            }
                            else
                            {
                                UOrderDInfo itemInfo = itemMap.get(info.strItemNo);
                                itemInfo.dDeliveryCount += info.dQuantity;
                                dDeliveryValue += info.dQuantity;
                            }
                        }
                        else
                        {
                            if(itemMap.get(info.strItemNo) == null)
                            {
                                UOrderDInfo itemInfo = info.duplicate();
                                itemInfo.nDeliveryCount = info.nQuantity;
                                itemList.add(itemInfo);
                                itemMap.put(info.strItemNo, itemInfo);
                                nDeliveryCount += info.nQuantity;
                            }
                            else
                            {
                                UOrderDInfo itemInfo = itemMap.get(info.strItemNo);
                                itemInfo.nDeliveryCount += info.nQuantity;
                                nDeliveryCount += info.nQuantity;
                            }
                        }
                    }
                }
                it.remove();
            }
            mObjectMap.put("Free", freeMapList);

            mObjectMap.put("free_total_case_qty", nTotalCaseQty+"/");
            mObjectMap.put("free_total_unit_qty", String.valueOf(nTotalUnitQty));
            mObjectMap.put("free_total_total_qty", "+"+nTotalTotalQty);

            if(nFreeCnt > 0 && dFreeCnt > 0)
            {
                mObjectMap.put("total_free_goods", nFreeCnt + "\n" + String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dFreeCnt));
            }
            else if(nFreeCnt > 0){
                mObjectMap.put("total_free_goods", String.valueOf(nFreeCnt));
            }
            else if(dFreeCnt > 0){
                mObjectMap.put("total_free_goods", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dFreeCnt));
            }
            else {
                mObjectMap.put("total_free_goods", "0");
            }

            mObjectMap.put("total_free_unit_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalFreeUnitPrice));
            mObjectMap.put("total_free_disc_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*dTotalFreeDiscAmount));
            mObjectMap.put("total_free_sign_disc_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalFreeDiscAmount));
            mObjectMap.put("total_free_net_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalFreeNetAmount));

            mObjectMap.put("free_total_g_disc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalFreeDiscAmountAsDelivery));

            freeNetAmount = dTotalFreeNetAmountAsDelivery;
            freeTaxAmount = dTotalFreeTaxAmount;
            freeTotalAmount = freeNetAmount+freeTaxAmount;
        }

        mObjectMap.put("free_total_totals", String.format(Locale.ENGLISH, "+%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", freeNetAmount));
        mObjectMap.put("free_total_total_tax", String.format(Locale.ENGLISH, "+%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", freeTaxAmount));
        mObjectMap.put("free_total_total_amount", String.format(Locale.ENGLISH, "+%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", freeTotalAmount));

        mObjectMap.put("free_disc_total_totals", String.format(Locale.ENGLISH, "-%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", freeNetAmount));
        mObjectMap.put("free_disc_total_total_tax", String.format(Locale.ENGLISH, "-%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", freeTaxAmount));
        mObjectMap.put("free_disc_total_total_amount", String.format(Locale.ENGLISH, "-%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", freeTotalAmount));

        if( replaceList.size() > 0 )
        {
            int nTotalCredits = 0;
            float dTotalCredits = 0;
            float dTotalCreditsUnitPrice = 0;
            float dTotalCreditsDiscAmount = 0;
            float dTotalCreditsNetAmount = 0;
            float dTotalTaxAmount = 0;

            int nTotalCaseQty = 0;
            int nTotalUnitQty = 0;
            int nTotalTotalQty = 0;

            List<Map<String, String>> replaceMapList = new ArrayList<>();
            Collections.sort(replaceList, new Comparator<UOrderDInfo>() {
                @Override
                public int compare(UOrderDInfo t1, UOrderDInfo t2) {
                    return t1.strItemNo.compareTo(t2.strItemNo);
                }
            });
            for(UOrderDInfo info : replaceList)
            {
                Map<String, String> replaceMap = new HashMap<>();
                replaceMap.put("code", Utils.getDisplayItemCode(context, info.strItemNo));
                replaceMap.put("upccode", info.strItemUPC);
                if(info.uLoadBlotInfo.size() > 0)
                {
                    String strLotNumbers = "";
                    for(LoadBlotInfo loadBlotInfo : info.uLoadBlotInfo)
                    {
                        if(TextUtils.isEmpty(strLotNumbers))
                        {
                            strLotNumbers = loadBlotInfo.strLotNumber;
                        }
                        else
                        {
                            strLotNumbers += ", " + loadBlotInfo.strLotNumber;
                        }
                    }
                    replaceMap.put("description", info.strDescription + "\n" + strLotNumbers);
                    replaceMap.put("short_desc", info.strShortDesc + "\n" + strLotNumbers);
                }
                else
                {
                    replaceMap.put("description", info.strDescription);
                    replaceMap.put("short_desc", info.strShortDesc);
                }

                ProdInfo prodInfo = mProdInfoMap.get(info.strItemNo);
                if (prodInfo != null) {
                    replaceMap.put("arab_description", prodInfo.strInfoDetail);
                }
                else {
                    replaceMap.put("arab_description", " ");
                }
                replaceMap.put("upc", String.valueOf(info.nCaseFactor));

                PriceGrpInfo priceGrpInfo = priceGrpInfoMap.get(info.strItemNo);
                if(priceGrpInfo == null)
                {
                    priceGrpInfo = altPriceGrpInfoMap.get(info.strItemNo);
                }

                if(priceGrpInfo != null)
                {
                    replaceMap.put("price_grp", String.format(Locale.ENGLISH, "%.2f%%", Utils.getDividedValue(priceGrpInfo.nPrice)));
                }
                else
                {
                    replaceMap.put("price_grp", " ");
                }

                replaceMap.put("base_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice));
                replaceMap.put("disc_pas", String.format(Locale.ENGLISH, "%.2f%%", (info.nBasePrice - info.nPrice) / info.nBasePrice * 100));
                if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
                {
                    replaceMap.put("group", info.strProdGroup);
                    replaceMap.put("unit_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice));
                    replaceMap.put("disc_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", (info.nBasePrice - info.nPrice) * info.dQuantity));
                    replaceMap.put("net_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nPrice * info.dQuantity));

                    dTotalCredits += info.dQuantity;
                    dTotalCreditsUnitPrice += Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice * info.dQuantity));

                    replaceMap.put("qty", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(info.dQuantity)));
                    replaceMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(info.dQuantity)));
                    replaceMap.put("qty_un", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(info.dQuantity)));

                    replaceMap.put("case_qty", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(0)));
                    replaceMap.put("unit_qty", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(info.dQuantity)));
                    replaceMap.put("total_qty", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(-1*info.dQuantity)));

                    float nPrice = info.nPrice;
                    float extraDiscount = 0;
                    for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
                    {
                        if( uPromotionInfo.nPromoAppMethod == 4 )
                            nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);

                        if (isSeparatePercentOffPromo) {
                            if( uPromotionInfo.nPromoType == Global.PROMO_TYPE_PERCENTAGE_OFF) {
                                extraDiscount += Utils.getDividedValue(uPromotionInfo.nAmount) * info.dQuantity;
                                extraDiscountAmount += extraDiscount;
                            }
                        }
                    }
                    if( info.nPrice != 0 )
                    {
                        replaceMap.put("price", String.format(Locale.ENGLISH, "-" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
                        replaceMap.put("total", String.format(Locale.ENGLISH, "-" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice * info.dQuantity)));

                        float nBasePrice = info.nBasePrice;
                        replaceMap.put("pc_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));
                        replaceMap.put("cs_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));

                        double discountAmount = 0;
                        if(info.prodLocnInfo != null)
                        {
                            discountAmount = (nPrice - Utils.getDividedValue(info.prodLocnInfo.nBasePrice)) * -1 * info.dQuantity - extraDiscount;
                        }
                        else
                        {
                            discountAmount = (nPrice - info.nBasePrice) * -1 * info.dQuantity - extraDiscount;
                        }
                        dTotalCreditsDiscAmount += discountAmount;
                        replaceMap.put("g_disc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*discountAmount));

                        double totalsAmount = Utils.getRoundedValue(nBasePrice * info.dQuantity) - discountAmount;
                        dTotalCreditsNetAmount += -1 * totalsAmount;
                        replaceMap.put("totals", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*totalsAmount));

                        float disc = (info.nBasePrice - nPrice) / info.nBasePrice * 100;

                        float extraDiscountTax = Utils.getRoundedValue(extraDiscount * (float)(info.nTaxAmount / (info.dQuantity * info.nPrice)));
                        extraDiscountTaxAmount += extraDiscountTax;

                        float taxAmount = info.nTaxAmount + extraDiscountTax;
                        dTotalTaxAmount -= taxAmount;

                        long nTaxRate = 0;
                        try {
                            nTaxRate = Math.round(taxAmount / totalsAmount * 100);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        replaceMap.put("total_tax", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f" + "(%d%%)", -1*taxAmount, nTaxRate));

                        float totalWithTax = (float)totalsAmount + taxAmount;
                        replaceMap.put("totalWithTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalWithTax)));
                        replaceMap.put("totalNoTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalsAmount)));
                        replaceMap.put("total_amount", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalWithTax)));
                        nSalesAmount -= totalsAmount;

                        replaceMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                        replaceMap.put("disc", String.format(Locale.ENGLISH, "%.2f%%", disc));
                    }
                    else
                    {
                        replaceMap.put("price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
                        replaceMap.put("total", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice * info.dQuantity)));

                        float nBasePrice = info.nBasePrice;
                        replaceMap.put("pc_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));
                        replaceMap.put("cs_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));

                        double discountAmount = 0;
                        if(info.prodLocnInfo != null)
                        {
                            discountAmount = (nPrice - Utils.getDividedValue(info.prodLocnInfo.nBasePrice)) * -1 * info.dQuantity - extraDiscount;
                        }
                        else
                        {
                            discountAmount = (nPrice - info.nBasePrice) * -1 * info.dQuantity - extraDiscount;
                        }
                        dTotalCreditsDiscAmount += discountAmount;
                        replaceMap.put("g_disc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*discountAmount));

                        double totalsAmount = Utils.getRoundedValue(nBasePrice * info.dQuantity) - discountAmount;
                        dTotalCreditsNetAmount += -1 * totalsAmount;
                        replaceMap.put("totals", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*totalsAmount));

                        float disc = (info.nBasePrice - nPrice) / info.nBasePrice * 100;

                        float extraDiscountTax = Utils.getRoundedValue(extraDiscount * (float)(info.nTaxAmount / (info.dQuantity * info.nPrice)));
                        extraDiscountTaxAmount += extraDiscountTax;

                        float taxAmount = info.nTaxAmount + extraDiscountTax;
                        dTotalTaxAmount -= taxAmount;

                        long nTaxRate = 0;
                        try {
                            nTaxRate = Math.round(taxAmount / totalsAmount * 100);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        replaceMap.put("total_tax", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f" + "(%d%%)", -1*taxAmount, nTaxRate));

                        float totalWithTax = (float)totalsAmount + taxAmount;
                        replaceMap.put("totalWithTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalWithTax)));
                        replaceMap.put("totalNoTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalsAmount)));
                        replaceMap.put("total_amount", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalWithTax)));
                        nSalesAmount -= totalsAmount;

                        replaceMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                        replaceMap.put("disc", String.format(Locale.ENGLISH, "%.2f%%", disc));
                    }

                    replaceMapList.add(replaceMap);
                    if(itemMap.get(info.strItemNo) == null)
                    {
                        UOrderDInfo itemInfo = info.duplicate();
                        itemInfo.dCreditCount = info.dQuantity;
                        itemList.add(itemInfo);
                        itemMap.put(info.strItemNo, itemInfo);
                        dCreditValue += info.dQuantity;
                    }
                    else
                    {
                        UOrderDInfo itemInfo = itemMap.get(info.strItemNo);
                        itemInfo.dCreditCount += info.dQuantity;
                        dCreditValue += info.dQuantity;
                    }
                }
                else
                {
                    replaceMap.put("group", info.strProdGroup);
                    replaceMap.put("unit_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice));
                    replaceMap.put("disc_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", (info.nBasePrice - info.nPrice) * info.nQuantity));
                    replaceMap.put("net_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nPrice * info.nQuantity));

                    nTotalCredits += info.nQuantity;
                    dTotalCreditsUnitPrice += Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice * info.nQuantity));

                    int nCase = getCaseValue(info.strItemNo, context);
                    if (nCase == 0)
                        nCase = 1;

                    if(bShowCase)
                    {
                        int nCaseValue = info.nQuantity / nCase;
                        int nQty = info.nQuantity % nCase;
                        replaceMap.put("qty", nCaseValue + "/" + nQty);
                        replaceMap.put("qty_cs_un", nCaseValue + "/" + nQty);
                        replaceMap.put("qty_un", String.valueOf(info.nQuantity));

                        replaceMap.put("case_qty", nCaseValue + "/");
                        replaceMap.put("unit_qty", String.valueOf(nQty));
                        replaceMap.put("total_qty", "-" + info.nQuantity);

                        nTotalCaseQty += nCaseValue;
                        nTotalUnitQty += nQty;
                        nTotalTotalQty += info.nQuantity;
                    }
                    else
                    {
                        replaceMap.put("qty", String.valueOf(info.nQuantity));
                        replaceMap.put("qty_cs_un", String.valueOf(info.nQuantity));
                        replaceMap.put("qty_un", String.valueOf(info.nQuantity));

                        replaceMap.put("case_qty", "0/");
                        replaceMap.put("unit_qty", String.valueOf(info.nQuantity));
                        replaceMap.put("total_qty", "-" + info.nQuantity);

                        nTotalUnitQty += info.nQuantity;
                        nTotalTotalQty += info.nQuantity;
                    }

                    float nPrice = info.nPrice;
                    float extraDiscount = 0;
                    for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
                    {
                        if( uPromotionInfo.nPromoAppMethod == 4 )
                            nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);

                        if (isSeparatePercentOffPromo) {
                            if( uPromotionInfo.nPromoType == Global.PROMO_TYPE_PERCENTAGE_OFF) {
                                extraDiscount += Utils.getDividedValue(uPromotionInfo.nAmount) * info.nQuantity;
                                extraDiscountAmount += extraDiscount;
                            }
                        }
                    }
                    if( info.nPrice != 0 )
                    {
                        replaceMap.put("price", String.format(Locale.ENGLISH, "-" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
                        replaceMap.put("total", String.format(Locale.ENGLISH, "-" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice * info.nQuantity)));

                        float nBasePrice = info.nBasePrice;
                        replaceMap.put("pc_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));
                        replaceMap.put("cs_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice*nCase)));

                        double discountAmount = 0;
                        if(info.prodLocnInfo != null)
                        {
                            discountAmount = (nPrice - Utils.getDividedValue(info.prodLocnInfo.nBasePrice)) * -1 * info.nQuantity - extraDiscount;
                        }
                        else
                        {
                            discountAmount = (nPrice - info.nBasePrice) * -1 * info.nQuantity - extraDiscount;
                        }
                        dTotalCreditsDiscAmount += discountAmount;
                        replaceMap.put("g_disc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*discountAmount));

                        double totalsAmount = Utils.getRoundedValue(nBasePrice * info.nQuantity) - discountAmount;
                        dTotalCreditsNetAmount += -1 * totalsAmount;
                        replaceMap.put("totals", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*totalsAmount));

                        float disc = (info.nBasePrice - nPrice) / info.nBasePrice * 100;

                        float extraDiscountTax = Utils.getRoundedValue(extraDiscount * (float)(info.nTaxAmount / (info.nQuantity * info.nPrice)));
                        extraDiscountTaxAmount += extraDiscountTax;

                        float taxAmount = info.nTaxAmount + extraDiscountTax;
                        dTotalTaxAmount -= taxAmount;

                        long nTaxRate = 0;
                        try {
                            nTaxRate = Math.round(taxAmount / totalsAmount * 100);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        replaceMap.put("total_tax", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f" + "(%d%%)", -1*taxAmount, nTaxRate));

                        float totalWithTax = (float)totalsAmount + taxAmount;
                        replaceMap.put("totalWithTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalWithTax)));
                        replaceMap.put("totalNoTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalsAmount)));
                        replaceMap.put("total_amount", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalWithTax)));
                        nSalesAmount -= totalsAmount;

                        replaceMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                        replaceMap.put("disc", String.format(Locale.ENGLISH, "%.2f%%", disc));
                    }
                    else
                    {
                        replaceMap.put("price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
                        replaceMap.put("total", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice * info.nQuantity)));

                        float nBasePrice = info.nBasePrice;
                        replaceMap.put("pc_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));
                        replaceMap.put("cs_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice * nCase)));

                        double discountAmount = 0;
                        if(info.prodLocnInfo != null)
                        {
                            discountAmount = (nPrice - Utils.getDividedValue(info.prodLocnInfo.nBasePrice)) * -1 * info.nQuantity - extraDiscount;
                        }
                        else
                        {
                            discountAmount = (nPrice - info.nBasePrice) * -1 * info.nQuantity - extraDiscount;
                        }
                        dTotalCreditsDiscAmount += discountAmount;
                        replaceMap.put("g_disc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*discountAmount));

                        double totalsAmount = Utils.getRoundedValue(nBasePrice * info.nQuantity) - discountAmount;
                        dTotalCreditsNetAmount += -1 * totalsAmount;
                        replaceMap.put("totals", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*totalsAmount));

                        float disc = (info.nBasePrice - nPrice) / info.nBasePrice * 100;

                        float extraDiscountTax = Utils.getRoundedValue(extraDiscount * (float)(info.nTaxAmount / (info.nQuantity * info.nPrice)));
                        extraDiscountTaxAmount += extraDiscountTax;

                        float taxAmount = info.nTaxAmount + extraDiscountTax;
                        dTotalTaxAmount -= taxAmount;

                        long nTaxRate = 0;
                        try {
                            nTaxRate = Math.round(taxAmount / totalsAmount * 100);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        replaceMap.put("total_tax", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f" + "(%d%%)", -1*taxAmount, nTaxRate));

                        float totalWithTax = (float)totalsAmount + taxAmount;
                        replaceMap.put("totalWithTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalWithTax)));
                        replaceMap.put("totalNoTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalsAmount)));
                        replaceMap.put("total_amount", String.format(Locale.ENGLISH, "%.+" + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalWithTax)));
                        nSalesAmount -= totalsAmount;

                        replaceMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                        replaceMap.put("disc", String.format(Locale.ENGLISH, "%.2f%%", disc));
                    }

                    replaceMapList.add(replaceMap);
                    if(itemMap.get(info.strItemNo) == null)
                    {
                        UOrderDInfo itemInfo = info.duplicate();
                        itemInfo.nCreditCount = info.nQuantity;
                        itemList.add(itemInfo);
                        itemMap.put(info.strItemNo, itemInfo);
                        nCreditCount += info.nQuantity;
                    }
                    else
                    {
                        UOrderDInfo itemInfo = itemMap.get(info.strItemNo);
                        itemInfo.nCreditCount += info.nQuantity;
                        nCreditCount += info.nQuantity;
                    }
                }

            }
            mObjectMap.put("Credits", replaceMapList);

            mObjectMap.put("credits_total_case_qty", nTotalCaseQty+"/");
            mObjectMap.put("credits_total_unit_qty", String.valueOf(nTotalUnitQty));
            mObjectMap.put("credits_total_total_qty", "-"+nTotalTotalQty);

            if(nTotalCredits > 0 && dTotalCredits > 0)
            {
                mObjectMap.put("total_credits", nTotalCredits + "\n" + String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalCredits));
            }
            else if(nTotalCredits > 0){
                mObjectMap.put("total_credits", String.valueOf(nTotalCredits));
            }
            else if(dTotalCredits > 0){
                mObjectMap.put("total_credits", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalCredits));
            }
            else {
                mObjectMap.put("total_credits", "0");
            }

            mObjectMap.put("total_credits_unit_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalCreditsUnitPrice));
            mObjectMap.put("total_credits_disc_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*dTotalCreditsDiscAmount));
            mObjectMap.put("total_credits_sign_disc_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalCreditsDiscAmount));
            mObjectMap.put("total_credits_net_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalCreditsNetAmount));

            mObjectMap.put("credits_total_g_disc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*dTotalCreditsDiscAmount));
            dDiscountAmount -= dTotalCreditsDiscAmount;

            creditsNetAmount = dTotalCreditsNetAmount;
            creditsTaxAmount = dTotalTaxAmount;
            creditsTotalAmount = dTotalCreditsNetAmount+dTotalTaxAmount;
        }

        mObjectMap.put("credits_total_totals", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", creditsNetAmount));
        mObjectMap.put("credits_total_total_tax", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", creditsTaxAmount));
        mObjectMap.put("credits_total_total_amount", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", creditsTotalAmount));

        if( buybackList.size() > 0 )
        {
            Collections.sort(buybackList, new Comparator<UOrderDInfo>() {
                @Override
                public int compare(UOrderDInfo t1, UOrderDInfo t2) {
                    return t1.strItemNo.compareTo(t2.strItemNo);
                }
            });

            int nTotalBuybacks = 0;
            float dTotalBuybacks = 0;
            float dTotalBuybacksUnitPrice = 0;
            float dTotalBuybacksDiscAmount = 0;
            float dTotalBuybacksNetAmount = 0;
            float dTotalTaxAmount = 0;

            int nTotalCaseQty = 0;
            int nTotalUnitQty = 0;
            int nTotalTotalQty = 0;

            List<Map<String, String>> buybackMapList = new ArrayList<>();
            for(UOrderDInfo info : buybackList)
            {
                Map<String, String> buybackMap = new HashMap<>();
                buybackMap.put("code", Utils.getDisplayItemCode(context, info.strItemNo));
                buybackMap.put("upccode", info.strItemUPC);
                if(info.uLoadBlotInfo.size() > 0)
                {
                    String strLotNumbers = "";
                    for(LoadBlotInfo loadBlotInfo : info.uLoadBlotInfo)
                    {
                        if(TextUtils.isEmpty(strLotNumbers))
                        {
                            strLotNumbers = loadBlotInfo.strLotNumber;
                        }
                        else
                        {
                            strLotNumbers += ", " + loadBlotInfo.strLotNumber;
                        }
                    }
                    buybackMap.put("description", info.strDescription + "\n" + strLotNumbers);
                    buybackMap.put("short_desc", info.strShortDesc + "\n" + strLotNumbers);
                }
                else
                {
                    buybackMap.put("description", info.strDescription);
                    buybackMap.put("short_desc", info.strShortDesc);
                }

                ProdInfo prodInfo = mProdInfoMap.get(info.strItemNo);
                if (prodInfo != null) {
                    buybackMap.put("arab_description", prodInfo.strInfoDetail);
                }
                else {
                    buybackMap.put("arab_description", " ");
                }
                buybackMap.put("upc", String.valueOf(info.nCaseFactor));

                PriceGrpInfo priceGrpInfo = priceGrpInfoMap.get(info.strItemNo);
                if(priceGrpInfo == null)
                {
                    priceGrpInfo = altPriceGrpInfoMap.get(info.strItemNo);
                }

                if(priceGrpInfo != null)
                {
                    buybackMap.put("price_grp", String.format(Locale.ENGLISH, "%.2f%%", Utils.getDividedValue(priceGrpInfo.nPrice)));
                }
                else
                {
                    buybackMap.put("price_grp", " ");
                }

                buybackMap.put("base_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice));
                buybackMap.put("disc_pas", String.format(Locale.ENGLISH, "%.2f%%", (info.nBasePrice - info.nPrice) / info.nBasePrice * 100));

                if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
                {
                    buybackMap.put("group", info.strProdGroup);
                    buybackMap.put("unit_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice));
                    buybackMap.put("disc_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", (info.nBasePrice - info.nPrice) * info.dQuantity));
                    buybackMap.put("net_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nPrice * info.dQuantity));

                    dTotalBuybacks += info.dQuantity;
                    dTotalBuybacksUnitPrice += Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice * info.dQuantity));

                    buybackMap.put("qty_cs_un", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(info.dQuantity)));
                    buybackMap.put("qty", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(info.dQuantity)));
                    buybackMap.put("qty_un", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(info.dQuantity)));

                    buybackMap.put("case_qty", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(0)));
                    buybackMap.put("unit_qty", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(info.dQuantity)));
                    buybackMap.put("total_qty", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(-1*info.dQuantity)));

                    float nPrice = info.nPrice;
                    float extraDiscount = 0;
                    for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
                    {
                        if( uPromotionInfo.nPromoAppMethod == 4 )
                            nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);

                        if (isSeparatePercentOffPromo) {
                            if( uPromotionInfo.nPromoType == Global.PROMO_TYPE_PERCENTAGE_OFF) {
                                extraDiscount += Utils.getDividedValue(uPromotionInfo.nAmount) * info.dQuantity;
                                extraDiscountAmount += extraDiscount;
                            }
                        }
                    }
                    if( info.nPrice != 0 )
                    {
                        buybackMap.put("price", String.format(Locale.ENGLISH, "-" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
                        buybackMap.put("total", String.format(Locale.ENGLISH, "-" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice * info.dQuantity)));

                        float nBasePrice = info.nBasePrice;
                        buybackMap.put("pc_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));
                        buybackMap.put("cs_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));

                        double discountAmount = 0;
                        if(info.prodLocnInfo != null)
                        {
                            discountAmount = (nPrice - Utils.getDividedValue(info.prodLocnInfo.nBasePrice)) * -1 * info.dQuantity - extraDiscount;
                        }
                        else
                        {
                            discountAmount = (nPrice - info.nBasePrice) * -1 * info.dQuantity - extraDiscount;
                        }
                        dTotalBuybacksDiscAmount += discountAmount;
                        buybackMap.put("g_disc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*discountAmount));

                        double totalsAmount = Utils.getRoundedValue(nBasePrice * info.dQuantity) - discountAmount;
                        dTotalBuybacksNetAmount += -1 * totalsAmount;
                        buybackMap.put("totals", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*totalsAmount));

                        float disc = (info.nBasePrice - nPrice) / info.nBasePrice * 100;

                        float extraDiscountTax = Utils.getRoundedValue(extraDiscount * (float)(info.nTaxAmount / (info.dQuantity * info.nPrice)));
                        extraDiscountTaxAmount += extraDiscountTax;

                        float taxAmount = info.nTaxAmount + extraDiscountTax;
                        dTotalTaxAmount -= taxAmount;

                        long nTaxRate = 0;
                        try {
                            nTaxRate = Math.round(taxAmount / totalsAmount * 100);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        buybackMap.put("total_tax", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f" + "(%d%%)", -1*taxAmount, nTaxRate));

                        float totalWithTax = (float)totalsAmount + taxAmount;
                        buybackMap.put("totalWithTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalWithTax)));
                        buybackMap.put("totalNoTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalsAmount)));
                        buybackMap.put("total_amount", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalWithTax)));
                        nSalesAmount -= totalsAmount;

                        buybackMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                        buybackMap.put("disc", String.format(Locale.ENGLISH, "%.2f%%", disc));
                    }
                    else
                    {
                        buybackMap.put("price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
                        buybackMap.put("total", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice * info.dQuantity)));

                        float nBasePrice = info.nBasePrice;
                        buybackMap.put("pc_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));
                        buybackMap.put("cs_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));

                        double discountAmount = 0;
                        if(info.prodLocnInfo != null)
                        {
                            discountAmount = (nPrice - Utils.getDividedValue(info.prodLocnInfo.nBasePrice)) * -1 * info.dQuantity - extraDiscount;
                        }
                        else
                        {
                            discountAmount = (nPrice - info.nBasePrice) * -1 * info.dQuantity - extraDiscount;
                        }
                        dTotalBuybacksDiscAmount += discountAmount;
                        buybackMap.put("g_disc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*discountAmount));

                        double totalsAmount = Utils.getRoundedValue(nBasePrice * info.dQuantity) - discountAmount;
                        dTotalBuybacksNetAmount += -1 * totalsAmount;
                        buybackMap.put("totals", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*totalsAmount));

                        float disc = (info.nBasePrice - nPrice) / info.nBasePrice * 100;

                        float extraDiscountTax = Utils.getRoundedValue(extraDiscount * (float)(info.nTaxAmount / (info.dQuantity * info.nPrice)));
                        extraDiscountTaxAmount += extraDiscountTax;

                        float taxAmount = info.nTaxAmount + extraDiscountTax;
                        dTotalTaxAmount -= taxAmount;

                        long nTaxRate = 0;
                        try {
                            nTaxRate = Math.round(taxAmount / totalsAmount * 100);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        buybackMap.put("total_tax", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f" + "(%d%%)", -1*taxAmount, nTaxRate));

                        float totalWithTax = (float)totalsAmount + taxAmount;
                        buybackMap.put("totalWithTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalWithTax)));
                        buybackMap.put("totalNoTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalsAmount)));
                        buybackMap.put("total_amount", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalWithTax)));
                        nSalesAmount -= totalsAmount;

                        buybackMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                        buybackMap.put("disc", String.format(Locale.ENGLISH, "%.2f%%", disc));
                    }

                    buybackMapList.add(buybackMap);
                    if(itemMap.get(info.strItemNo) == null)
                    {
                        UOrderDInfo itemInfo = info.duplicate();
                        itemInfo.dCreditCount = info.dQuantity;
                        itemList.add(itemInfo);
                        itemMap.put(info.strItemNo, itemInfo);
                        dCreditValue += info.dQuantity;
                    }
                    else
                    {
                        UOrderDInfo itemInfo = itemMap.get(info.strItemNo);
                        itemInfo.dCreditCount += info.dQuantity;
                        dCreditValue += info.dQuantity;
                    }
                }
                else
                {
                    buybackMap.put("group", info.strProdGroup);
                    buybackMap.put("unit_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice));
                    buybackMap.put("disc_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", (info.nBasePrice - info.nPrice) * info.nQuantity));
                    buybackMap.put("net_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nPrice * info.nQuantity));

                    nTotalBuybacks += info.nQuantity;
                    dTotalBuybacksUnitPrice += Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice * info.nQuantity));

                    int nCase = getCaseValue(info.strItemNo, context);
                    if (nCase == 0)
                        nCase = 1;

                    if(bShowCase)
                    {
                        int nCaseValue = info.nQuantity / nCase;
                        int nQty = info.nQuantity % nCase;
                        buybackMap.put("qty_cs_un", nCaseValue + "/" + nQty);
                        buybackMap.put("qty", nCaseValue + "/" + nQty);
                        buybackMap.put("qty_un", String.valueOf(info.nQuantity));

                        buybackMap.put("case_qty", nCaseValue + "/");
                        buybackMap.put("unit_qty", String.valueOf(nQty));
                        buybackMap.put("total_qty", "-" + info.nQuantity);

                        nTotalCaseQty += nCaseValue;
                        nTotalUnitQty += nQty;
                        nTotalTotalQty += info.nQuantity;
                    }
                    else
                    {
                        buybackMap.put("qty_cs_un", String.valueOf(info.nQuantity));
                        buybackMap.put("qty", String.valueOf(info.nQuantity));
                        buybackMap.put("qty_un", String.valueOf(info.nQuantity));

                        buybackMap.put("case_qty", "0/");
                        buybackMap.put("unit_qty", String.valueOf(info.nQuantity));
                        buybackMap.put("total_qty", "-" + info.nQuantity);

                        nTotalUnitQty += info.nQuantity;
                        nTotalTotalQty += info.nQuantity;
                    }

                    float nPrice = info.nPrice;
                    float extraDiscount = 0;
                    for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
                    {
                        if( uPromotionInfo.nPromoAppMethod == 4 )
                            nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);

                        if (isSeparatePercentOffPromo) {
                            if( uPromotionInfo.nPromoType == Global.PROMO_TYPE_PERCENTAGE_OFF) {
                                extraDiscount += Utils.getDividedValue(uPromotionInfo.nAmount) * info.nQuantity;
                                extraDiscountAmount += extraDiscount;
                            }
                        }
                    }
                    if( info.nPrice != 0 )
                    {
                        buybackMap.put("price", String.format(Locale.ENGLISH, "-" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
                        buybackMap.put("total", String.format(Locale.ENGLISH, "-" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice * info.nQuantity)));

                        float nBasePrice = info.nBasePrice;
                        buybackMap.put("pc_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));
                        buybackMap.put("cs_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice*nCase)));

                        double discountAmount = 0;
                        if(info.prodLocnInfo != null)
                        {
                            discountAmount = (nPrice - Utils.getDividedValue(info.prodLocnInfo.nBasePrice)) * -1 * info.nQuantity - extraDiscount;
                        }
                        else
                        {
                            discountAmount = (nPrice - info.nBasePrice) * -1 * info.nQuantity - extraDiscount;
                        }
                        dTotalBuybacksDiscAmount += discountAmount;
                        buybackMap.put("g_disc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*discountAmount));

                        double totalsAmount = Utils.getRoundedValue(nBasePrice * info.nQuantity) - discountAmount;
                        dTotalBuybacksNetAmount += -1 * totalsAmount;
                        buybackMap.put("totals", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*totalsAmount));

                        float disc = (info.nBasePrice - nPrice) / info.nBasePrice * 100;

                        float extraDiscountTax = Utils.getRoundedValue(extraDiscount * (float)(info.nTaxAmount / (info.nQuantity * info.nPrice)));
                        extraDiscountTaxAmount += extraDiscountTax;

                        float taxAmount = info.nTaxAmount + extraDiscountTax;
                        dTotalTaxAmount -= taxAmount;

                        long nTaxRate = 0;
                        try {
                            nTaxRate = Math.round(taxAmount / totalsAmount * 100);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        buybackMap.put("total_tax", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f" + "(%d%%)", -1*taxAmount, nTaxRate));

                        float totalWithTax = (float)totalsAmount + taxAmount;
                        buybackMap.put("totalWithTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalWithTax)));
                        buybackMap.put("total_amount", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalWithTax)));
                        nSalesAmount -= totalsAmount;

                        buybackMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                        buybackMap.put("disc", String.format(Locale.ENGLISH, "%.2f%%", disc));
                    }
                    else
                    {
                        buybackMap.put("price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
                        buybackMap.put("total", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice * info.nQuantity)));

                        float nBasePrice = info.nBasePrice;
                        buybackMap.put("pc_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice)));
                        buybackMap.put("cs_price", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nBasePrice * nCase)));

                        double discountAmount = 0;
                        if(info.prodLocnInfo != null)
                        {
                            discountAmount = (nPrice - Utils.getDividedValue(info.prodLocnInfo.nBasePrice)) * -1 * info.nQuantity - extraDiscount;
                        }
                        else
                        {
                            discountAmount = (nPrice - info.nBasePrice) * -1 * info.nQuantity - extraDiscount;
                        }
                        dTotalBuybacksDiscAmount += discountAmount;
                        buybackMap.put("g_disc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*discountAmount));

                        double totalsAmount = Utils.getRoundedValue(nBasePrice * info.nQuantity) - discountAmount;
                        dTotalBuybacksNetAmount += -1 * totalsAmount;
                        buybackMap.put("totals", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*totalsAmount));

                        float disc = (info.nBasePrice - nPrice) / info.nBasePrice * 100;

                        float extraDiscountTax = Utils.getRoundedValue(extraDiscount * (float)(info.nTaxAmount / (info.nQuantity * info.nPrice)));
                        extraDiscountTaxAmount += extraDiscountTax;

                        float taxAmount = info.nTaxAmount + extraDiscountTax;
                        dTotalTaxAmount -= taxAmount;

                        long nTaxRate = 0;
                        try {
                            nTaxRate = Math.round(taxAmount / totalsAmount * 100);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        buybackMap.put("total_tax", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f" + "(%d%%)", -1*taxAmount, nTaxRate));

                        float totalWithTax = (float)totalsAmount + taxAmount;
                        buybackMap.put("totalWithTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalWithTax)));
                        buybackMap.put("totalNoTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalsAmount)));
                        buybackMap.put("total_amount", String.format(Locale.ENGLISH, "%.+" + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(-1*totalWithTax)));
                        nSalesAmount -= totalsAmount;

                        buybackMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                        buybackMap.put("disc", String.format(Locale.ENGLISH, "%.2f%%", disc));
                    }

                    buybackMapList.add(buybackMap);
                    if(itemMap.get(info.strItemNo) == null)
                    {
                        UOrderDInfo itemInfo = info.duplicate();
                        itemInfo.nCreditCount = info.nQuantity;
                        itemList.add(itemInfo);
                        itemMap.put(info.strItemNo, itemInfo);
                        nCreditCount += info.nQuantity;
                    }
                    else
                    {
                        UOrderDInfo itemInfo = itemMap.get(info.strItemNo);
                        itemInfo.nCreditCount += info.nQuantity;
                        nCreditCount += info.nQuantity;
                    }
                }
            }
            mObjectMap.put("BuyBacks", buybackMapList);

            mObjectMap.put("buybacks_total_case_qty", nTotalCaseQty+"/");
            mObjectMap.put("buybacks_total_unit_qty", String.valueOf(nTotalUnitQty));
            mObjectMap.put("buybacks_total_total_qty", "-"+nTotalTotalQty);

            if(nTotalBuybacks > 0 && dTotalBuybacks > 0)
            {
                mObjectMap.put("total_buybacks", nTotalBuybacks + "\n" + String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalBuybacks));
            }
            else if(nTotalBuybacks > 0){
                mObjectMap.put("total_buybacks", String.valueOf(nTotalBuybacks));
            }
            else if(dTotalBuybacks > 0){
                mObjectMap.put("total_buybacks", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalBuybacks));
            }
            else {
                mObjectMap.put("total_buybacks", "0");
            }

            mObjectMap.put("total_buybacks_unit_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalBuybacksUnitPrice));
            mObjectMap.put("total_buybacks_disc_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*dTotalBuybacksDiscAmount));
            mObjectMap.put("total_buybacks_sign_disc_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalBuybacksDiscAmount));
            mObjectMap.put("total_buybacks_net_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dTotalBuybacksNetAmount));

            mObjectMap.put("buybacks_total_g_disc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", -1*dTotalBuybacksDiscAmount));

            buybacksNetAmount = dTotalBuybacksNetAmount;
            buybacksTaxAmount = dTotalTaxAmount;
            buybacksTotalAmount = dTotalBuybacksNetAmount+dTotalTaxAmount;
        }

        mObjectMap.put("buybacks_total_totals", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", buybacksNetAmount));
        mObjectMap.put("buybacks_total_total_tax", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", buybacksTaxAmount));
        mObjectMap.put("buybacks_total_total_amount", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", buybacksTotalAmount));

        // we check about the extra discount line
        // need to check if the customer has the promotion
        hasExtraDiscount = false;
        String strDiscountDesc = "";
        if (isSeparatePercentOffPromo) {
            DbAdapter dbAdapter1 = new DbAdapter(context);
            dbAdapter1.open();

            List<PromHeadInfo> promHeadList = new ArrayList<PromHeadInfo>();
            if (mCustomerInfo != null && mCustomerInfo.nPromoPlan == 999999)
            {
                List<ProMultiInfo> proMultiList = dbAdapter1.getProMultiList(mCustomerInfo.nChainNo, mCustomerInfo.nCustNo);
                for (ProMultiInfo multiInfo : proMultiList)
                {
                    List<PromHeadInfo> list = dbAdapter1.getPromHeadList(multiInfo.nPlanNo);
                    for (PromHeadInfo info : list)
                    {
                        promHeadList.add(info);
                    }
                }
            }
            else if(mCustomerInfo != null)
            {
                promHeadList = dbAdapter1.getPromHeadList(mCustomerInfo.nPromoPlan);
            }
            else
            {
                promHeadList = dbAdapter1.getPromHeadList(0);
            }
            for (PromHeadInfo promHeadInfo : promHeadList)
            {
                List<PromAssInfo> list = dbAdapter1.getPromAssList(promHeadInfo.nAssignNo);
                for (PromAssInfo info : list)
                {
                    if (info.nPromoType == Global.PROMO_TYPE_PERCENTAGE_OFF) {
                        hasExtraDiscount = true;
                        strDiscountDesc = promHeadInfo.strDescription;
                        break;
                    }
                }
                if (hasExtraDiscount == true) {
                    break;
                }
            }
            dbAdapter1.close();
        }

        if (hasExtraDiscount) {
            List<Map<String, String>> extraDiscountMapList = new ArrayList<>();
            Map<String, String> extraDiscountMap = new HashMap<>();
            float extraDiscountTotal = extraDiscountAmount + extraDiscountTaxAmount;
            extraDiscountMap.put("totals", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", extraDiscountAmount));
            extraDiscountMap.put("total_tax", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", extraDiscountTaxAmount));
            extraDiscountMap.put("total_amount", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", extraDiscountTotal));
            extraDiscountMapList.add(extraDiscountMap);
            mObjectMap.put("ExtraDiscount", extraDiscountMapList);
        }

        float finalNetAmount = salesNetAmount + creditsNetAmount + buybacksNetAmount;
        float finalTaxAmount = salesTaxAmount + creditsTaxAmount + buybacksTaxAmount;
        float finalTotalAmount = salesTotalAmount + creditsTotalAmount + buybacksTotalAmount;

        mObjectMap.put("netsales_totals", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", finalNetAmount));
        mObjectMap.put("netsales_total_tax", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", finalTaxAmount));
        mObjectMap.put("netsales_total_amount", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", finalTotalAmount));

        if (hasExtraDiscount) {
            finalNetAmount += extraDiscountAmount;
            finalTaxAmount += extraDiscountTaxAmount;
            finalTotalAmount += extraDiscountAmount + extraDiscountTaxAmount;
        }

        mObjectMap.put("final_totals", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", finalNetAmount));
        mObjectMap.put("final_total_tax", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", finalTaxAmount));
        mObjectMap.put("final_total_amount", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", finalTotalAmount));

        mObjectMap.put("final_total_amount_unit", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f " + Global.gCurrencySymbol, finalTotalAmount));

        if (!isReturnMode) {
            mObjectMap.put("NetAmountCaption", "NET SALES");
            mObjectMap.put("ArabNetAmountCaption", "صافي المبيعات");
        }
        else {
            mObjectMap.put("NetAmountCaption", "NET RETURNS");
            mObjectMap.put("ArabNetAmountCaption", "صافي المرتجعات");
        }

        List<Map<String, String>> itemMapList = new ArrayList<>();
        Collections.sort(itemList, new Comparator<UOrderDInfo>() {
            @Override
            public int compare(UOrderDInfo t1, UOrderDInfo t2) {
                return t1.strItemNo.compareTo(t2.strItemNo);
            }
        });
        for(UOrderDInfo info : itemList)
        {
            Map<String, String> buybackMap = new HashMap<>();
            buybackMap.put("code", Utils.getDisplayItemCode(context, info.strItemNo));
            buybackMap.put("upccode", info.strItemUPC);
            if(info.uLoadBlotInfo.size() > 0)
            {
                String strLotNumbers = "";
                for(LoadBlotInfo loadBlotInfo : info.uLoadBlotInfo)
                {
                    if(TextUtils.isEmpty(strLotNumbers))
                    {
                        strLotNumbers = loadBlotInfo.strLotNumber;
                    }
                    else
                    {
                        strLotNumbers += ", " + loadBlotInfo.strLotNumber;
                    }
                }
                buybackMap.put("description", info.strDescription + "\n" + strLotNumbers);
                buybackMap.put("short_desc", info.strShortDesc + "\n" + strLotNumbers);
            }
            else
            {
                buybackMap.put("description", info.strDescription);
                buybackMap.put("short_desc", info.strShortDesc);
            }

            PriceGrpInfo priceGrpInfo = priceGrpInfoMap.get(info.strItemNo);
            if(priceGrpInfo == null)
            {
                priceGrpInfo = altPriceGrpInfoMap.get(info.strItemNo);
            }

            if(priceGrpInfo != null)
            {
                buybackMap.put("price_grp", String.format(Locale.ENGLISH, "%.2f%%", Utils.getDividedValue(priceGrpInfo.nPrice)));
            }
            else
            {
                buybackMap.put("price_grp", " ");
            }



            if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
            {
                if(info.dDeliveryCount > 0 )
                {
                    buybackMap.put("dlvd_qty", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(info.dDeliveryCount)));
                }
                else
                {
                    buybackMap.put("dlvd_qty", " ");
                }
                if( info.dCreditCount > 0 )
                {
                    buybackMap.put("crdt_qty", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(info.dCreditCount)));
                }
                else
                {
                    buybackMap.put("crdt_qty", " ");
                }
            }
            else
            {
                if(info.nDeliveryCount > 0 )
                {
                    buybackMap.put("dlvd_qty", String.valueOf(info.nDeliveryCount));
                }
                else
                {
                    buybackMap.put("dlvd_qty", " ");
                }
                if( info.nCreditCount > 0 )
                {
                    buybackMap.put("crdt_qty", String.valueOf(info.nCreditCount));
                }
                else
                {
                    buybackMap.put("crdt_qty", " ");
                }
            }

            float nPrice = info.nPrice;
            for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
            {
                if( uPromotionInfo.nPromoAppMethod == 4 )
                    nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);
            }
            if(mCustomerInfo.nCustPriceMaintFlag > 0)
            {
                buybackMap.put("unit_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
            }
            else
            {
                buybackMap.put("unit_price", " ");
            }

            buybackMap.put("base_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice));
            buybackMap.put("disc_pas", String.format(Locale.ENGLISH, "%.2f%%", (info.nBasePrice - info.nPrice) / info.nBasePrice * 100));
            if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
            {
                buybackMap.put("group", info.strProdGroup);
                buybackMap.put("unit_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice));
                buybackMap.put("disc_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", (info.nBasePrice - info.nPrice) * info.dQuantity));
                buybackMap.put("net_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nPrice * info.dQuantity));

                if( info.nPrice != 0 )
                {
                    buybackMap.put("price", String.format(Locale.ENGLISH, "-" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
                    buybackMap.put("total", String.format(Locale.ENGLISH, "-" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice * info.dQuantity)));
                    float disc = (info.nBasePrice - nPrice) / info.nBasePrice * 100;
                    float real_disc = 100 - Utils.getFloatFromString(String.format(Locale.ENGLISH, "%.2f", disc));
                    float price_inc_gst = Utils.getFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                    //float totalWithTax = (float)(price_inc_gst * real_disc / 100.0f * info.dQuantity);
                    float totalWithTax = Utils.getRoundedValue(info.dQuantity * info.nPrice) + info.nTaxAmount;
                    buybackMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                    buybackMap.put("totalWithTax", String.format(Locale.ENGLISH, "-" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalWithTax)));
                    buybackMap.put("disc", String.format(Locale.ENGLISH, "%.2f%%", disc));
//                    nSalesAmount -= (totalWithTax - info.nTaxAmount);
                }
                else
                {
                    buybackMap.put("price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
                    buybackMap.put("total", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice * info.dQuantity)));
                    float disc = (info.nBasePrice - nPrice) / info.nBasePrice * 100;
                    float real_disc = 100 - Utils.getFloatFromString(String.format(Locale.ENGLISH, "%.2f", disc));
                    float price_inc_gst = Utils.getFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                    //float totalWithTax = (float)(price_inc_gst * real_disc / 100.0f * info.dQuantity);
                    float totalWithTax = Utils.getRoundedValue(info.dQuantity * info.nPrice) + info.nTaxAmount;
                    buybackMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                    buybackMap.put("totalWithTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalWithTax)));
                    buybackMap.put("disc", String.format(Locale.ENGLISH, "%.2f%%", disc));
//                    nSalesAmount -= (totalWithTax - info.nTaxAmount);
                }
            }
            else
            {
                buybackMap.put("group", info.strProdGroup);
                buybackMap.put("unit_price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nBasePrice));
                buybackMap.put("disc_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", (info.nBasePrice - info.nPrice) * info.nQuantity));
                buybackMap.put("net_amount", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", info.nPrice * info.nQuantity));

                if( info.nPrice != 0 )
                {
                    buybackMap.put("price", String.format(Locale.ENGLISH, "-" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
                    buybackMap.put("total", String.format(Locale.ENGLISH, "-" + Global.gCurrencySymbol +  "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice * info.nQuantity)));
                    float disc = (info.nBasePrice - nPrice) / info.nBasePrice * 100;
                    float real_disc = 100 - Utils.getFloatFromString(String.format(Locale.ENGLISH, "%.2f", disc));
                    float price_inc_gst = Utils.getFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                    //float totalWithTax = (float)(price_inc_gst * real_disc / 100.0f * info.nQuantity);
                    float totalWithTax = Utils.getRoundedValue(info.nQuantity * info.nPrice) + info.nTaxAmount;
                    buybackMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                    buybackMap.put("totalWithTax", String.format(Locale.ENGLISH, "-" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalWithTax)));
                    buybackMap.put("disc", String.format(Locale.ENGLISH, "%.2f%%", disc));
//                    nSalesAmount -= (totalWithTax - info.nTaxAmount);
                }
                else
                {
                    buybackMap.put("price", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice)));
                    buybackMap.put("total", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice * info.nQuantity)));
                    float disc = (info.nBasePrice - nPrice) / info.nBasePrice * 100;
                    float real_disc = 100 - Utils.getFloatFromString(String.format(Locale.ENGLISH, "%.2f", disc));
                    float price_inc_gst = Utils.getFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                    //float totalWithTax = (float)(price_inc_gst * real_disc / 100.0f * info.nQuantity);
                    float totalWithTax = Utils.getRoundedValue(info.nQuantity * info.nPrice) + info.nTaxAmount;
                    buybackMap.put("price_inc_gst", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nBasePrice + info.nPrintLineTaxAmount)));
                    buybackMap.put("totalWithTax", String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(totalWithTax)));
                    buybackMap.put("disc", String.format(Locale.ENGLISH, "%.2f%%", disc));
//                    nSalesAmount -= (totalWithTax - info.nTaxAmount);
                }
            }


            itemMapList.add(buybackMap);
        }

        mObjectMap.put("Item", itemMapList);

        //float nSalesAmount = Global.gUOrderHInfo.nSaleAmount;
        //float nSalesAmount = 0;
        int nQty = 0, nUnit = 0;
        double dValue = 0;
        for(UOrderDInfo uOrderDInfo : Global.gUOrderHInfo.deliveryList)
        {
            for(UPromotionInfo uPromotionInfo : uOrderDInfo.uPromotionInfoList)
            {
                if( uPromotionInfo.nPromoAppMethod == 4 )
                {
                    nSalesAmount += Utils.getDividedValue(uPromotionInfo.nAmount * uOrderDInfo.nSales);
                }
            }
            if(Utils.getProdLoconSCWType(context, uOrderDInfo.strItemNo) == Global.WEIGHT_ITEM)
            {
                dValue += uOrderDInfo.dSales;
            }
            else
            {
                if(bShowCase)
                {
                    if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") )
                    {
                        int nCase = getCaseValue(uOrderDInfo.strItemNo, context);
                        nUnit += uOrderDInfo.nSales / nCase;
                        nQty += uOrderDInfo.nSales % nCase;
                    }
                    else
                    {
                        nQty += uOrderDInfo.nSales;
                    }
                }
                else
                {
                    nQty += uOrderDInfo.nSales;
                }
            }
        }
        for(UOrderDInfo uOrderDInfo : Global.gUOrderHInfo.pickupList)
        {
            for(UPromotionInfo uPromotionInfo : uOrderDInfo.uPromotionInfoList)
            {
                if( uPromotionInfo.nPromoAppMethod == 4 )
                {
                    nSalesAmount -= Utils.getDividedValue(uPromotionInfo.nAmount * uOrderDInfo.nDumps);
                }
            }
            if(Utils.getProdLoconSCWType(context, uOrderDInfo.strItemNo) == Global.WEIGHT_ITEM)
            {
                dValue -= uOrderDInfo.dDumps;
            }
            else
            {
                if(bShowCase)
                {
                    if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") )
                    {
                        int nCase = getCaseValue(uOrderDInfo.strItemNo, context);
                        nUnit -= uOrderDInfo.nDumps / nCase;
                        nQty -= uOrderDInfo.nDumps % nCase;
                    }
                    else
                    {
                        nQty -= uOrderDInfo.nDumps;
                    }
                }
                else
                {
                    nQty -= uOrderDInfo.nDumps;
                }
            }
        }
        if(bShowCase)
        {
            if(Global.gCurCustomerInfo.strSalEntryMode.equals("B"))
            {
                if(dValue > 0 && (nUnit > 0 || nQty > 0))
                {
                    mObjectMap.put("TotalQty", String.format(Locale.ENGLISH, "%d/%d\n%.2f", nUnit, nQty, Utils.getRoundedValue(dValue)));
                }
                else if( dValue > 0 )
                {
                    mObjectMap.put("TotalQty", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(dValue)));
                }
                else
                {
                    mObjectMap.put("TotalQty", String.format(Locale.ENGLISH, "%d/%d", nUnit, nQty));
                }
            }
            else
            {
                if(dValue > 0 && nQty > 0)
                {
                    mObjectMap.put("TotalQty", String.format(Locale.ENGLISH, "%d\n%.2f",  nQty, Utils.getRoundedValue(dValue)));
                }
                else if(dValue > 0)
                {
                    mObjectMap.put("TotalQty", String.format(Locale.ENGLISH, "%.2f",  Utils.getRoundedValue(dValue)));
                }
                else
                {
                    mObjectMap.put("TotalQty", String.format(Locale.ENGLISH, "%d",  nQty));
                }
            }
        }
        else
        {
            if(dValue > 0 && nQty > 0)
            {
                mObjectMap.put("TotalQty", String.format(Locale.ENGLISH, "%d\n%.2f",  nQty, Utils.getRoundedValue(dValue)));
            }
            else if(dValue > 0)
            {
                mObjectMap.put("TotalQty", String.format(Locale.ENGLISH, "%.2f",  Utils.getRoundedValue(dValue)));
            }
            else
            {
                mObjectMap.put("TotalQty", String.format(Locale.ENGLISH, "%d",  nQty));
            }
        }

        if(dDiscountAmount != 0)
        {
            mObjectMap.put("DisInc", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(dDiscountAmount)));
        }

        String strInvoiceDate = Utils.getCalculatedInvoiceDueDate(context, Global.gCurCustomerInfo, 0);
        if(!TextUtils.isEmpty(strInvoiceDate))
        {
            mObjectMap.put("InvDate", "PAYMENT DUE ON " + strInvoiceDate);
        }

        mObjectMap.put("SubTotal", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nSalesAmount)));
        mObjectMap.put("HandlingFee", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f" , Global.gUOrderHInfo.nHandlingFee));
        mObjectMap.put("HandlingAmount", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f" , Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount));

        if(dDeliveryValue > 0 && nDeliveryCount > 0)
        {
            mObjectMap.put("DeliveryCount", String.format(Locale.ENGLISH, "%d\n%.2f", nDeliveryCount, Utils.getRoundedValue(dDeliveryValue)));
        }
        else if(dDeliveryValue > 0)
        {
            mObjectMap.put("DeliveryCount", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(dDeliveryValue)));
        }
        else
        {
            mObjectMap.put("DeliveryCount", String.format(Locale.ENGLISH, "%d", nDeliveryCount));
        }
        if(dCreditValue > 0 && nCreditCount > 0)
        {
            mObjectMap.put("CreditCount", String.format(Locale.ENGLISH, "%d\n%.2f", nCreditCount, Utils.getRoundedValue(dCreditValue)));
        }
        else if(dCreditValue > 0)
        {
            mObjectMap.put("CreditCount", String.format(Locale.ENGLISH, "%.2f", Utils.getRoundedValue(dCreditValue)));
        }
        else
        {
            mObjectMap.put("CreditCount", String.format(Locale.ENGLISH, "%d", nCreditCount));
        }

        List<String> cdsValue = new ArrayList<>();
        cdsValue.add(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Global.gUOrderHInfo.nPrintCDSAmount));
        mObjectMap.put("NSWCDSLevy", cdsValue);

        List<String> gstValue = new ArrayList<>();

        gstValue.add(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(Global.gUOrderHInfo.nTaxAmount - Global.gUOrderHInfo.nPrintCDSAmount + Global.gUOrderHInfo.nHandlingFeeTaxAmount)));
        mObjectMap.put("GST", gstValue);

        nSalesAmount += Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount;
        List<String> centsRoundValue = new ArrayList<>();
        boolean bCentsRount;
        if( Utils.getStringSetting(context, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") &&
                (Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2")))
        {
            bCentsRount = true;
        }
        else if(Utils.getStringSetting(context, Global.PREF_ROUTE_CENTSROUND).trim().equals("2"))
        {
            bCentsRount = true;
        }
        else
        {
            bCentsRount = false;
        }

        if( bCentsRount ) {
            BigDecimal decSalesAmount = BigDecimal.valueOf(nSalesAmount);
            BigDecimal decTaxAmount = BigDecimal.valueOf(Global.gUOrderHInfo.nTaxAmount);
            BigDecimal decValue = decSalesAmount.add(decTaxAmount);
            float value = nSalesAmount + Global.gUOrderHInfo.nTaxAmount;
            float fNeg = 1;
            if(value < 0){
                fNeg = -1;
                decValue = decValue.multiply(BigDecimal.valueOf(-1));
            }
            int nTotal = Math.round(Utils.getRoundedDecValue(decValue).multiply(BigDecimal.valueOf(Global.MULTIPLE_VALUE)).floatValue());
            //int nTotal = Math.round(Utils.getRoundedValue(decValue.floatValue()) * Global.MULTIPLE_VALUE);
            //int nTotal = Math.round(Utils.getRoundedValue(value * fNeg) * Global.MULTIPLE_VALUE);
            int nOffSet = nTotal % 5000;
            int nInvoiceTotal = ((nTotal / 5000) + ((Math.abs(nOffSet) >= 2500) ? 1 : 0)) * 5000;
            nOffSet = nTotal % 1000;
            nTotal = ((nTotal / 1000) + ((Math.abs(nOffSet) >= 500) ? 1 : 0)) * 1000;
            nOffSet = nInvoiceTotal - nTotal;

            nTotal *= fNeg;
            nOffSet *= fNeg;
            float dOffSet = nOffSet / (Global.MULTIPLE_VALUE * 1f);
            float dTotal = nTotal / (Global.MULTIPLE_VALUE * 1f);
            centsRoundValue.add(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", dOffSet));
            mObjectMap.put("InvoiceGSTTotal", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(fNeg * nInvoiceTotal / (Global.MULTIPLE_VALUE * 1f))));
        }
        else
        {
            mObjectMap.put("InvoiceGSTTotal", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nSalesAmount + Global.gUOrderHInfo.nTaxAmount)));
        }

        mObjectMap.put("CentsRounding", centsRoundValue);

        // generate QR Code if there is not exist
        String strVatTotal = String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", finalTaxAmount);
        String strTotalWithVAT = String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", finalTotalAmount);

        String qrcodePath = generateQRCode(Global.gUOrderHInfo.strDocNo, Global.gUOrderHInfo.strTrxnDate, Global.gUOrderHInfo.strTrxnTime, strTotalWithVAT, strVatTotal);
        mObjectMap.put("qrcode", qrcodePath);

        mObjectMap.put("Sign", Global.gUOrderHInfo.strSignatureFilePath);
        mObjectMap.put("ReceivedBy", "Received by: " + Global.gUOrderHInfo.strOrderName);
        mObjectMap.put("CheckedReceivedBy", "Checked & received by: " + Global.gUOrderHInfo.strOrderName);
        if( nPayType == 0 )
        {
            mObjectMap.put("TotalPaid", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", 0f));
            if (bPartialPayment) {
                mObjectMap.put("CashPaid", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", fPartialPaidAmount));
            }
            else {
                mObjectMap.put("CashPaid", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", 0f));
            }
            Global.gCurRealPaymentAmount = 0;
        }
        else
        {
            mObjectMap.put("TotalPaid", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(Global.gCurRealPaymentAmount)));
            mObjectMap.put("CashPaid", String.format(Locale.ENGLISH, "%+." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(Global.gCurRealPaymentAmount)));
        }

        mObjectMap.put("TotalDue", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(Global.gCurTotalValue - Global.gCurRealPaymentAmount)));
        float fTotalDueWithoutAR = Global.gCurTotalDueInvoice - Global.gCurRealPaymentAmount;
        if(fTotalDueWithoutAR > 0)
        {
            mObjectMap.put("TotalDueWithoutAR", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(fTotalDueWithoutAR)));
            mObjectMap.put("TotalDuePrior", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(Global.gCurTotalValue - Global.gCurTotalDueInvoice)));
        }
        else
        {
            mObjectMap.put("TotalDueWithoutAR", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", 0f));
            mObjectMap.put("TotalDuePrior", String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(context, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(Global.gCurTotalValue - Global.gCurRealPaymentAmount)));
        }


        if(!TextUtils.isEmpty(mCustomerInfo.strAltCustNo))
        {
            mObjectMap.put("PaymentReference", mCustomerInfo.strAltCustNo);
        }
        else
        {
            mObjectMap.put("PaymentReference", String.valueOf(mCustomerInfo.nCustNo));
        }

        if( Global.gUOrderHInfo.strSpecialInstructions != null && Global.gUOrderHInfo.strSpecialInstructions.length() > 0 )
        {
            int nStart = 0;
            try
            {
                String[] strText = Global.gUOrderHInfo.strSpecialInstructions.split("  ");
                if( strText.length >= 2 )
                {
                    String strOrderTemp = Global.gUOrderHInfo.strSpecialInstructions.split("  ")[0];
                    nStart = strOrderTemp.length();
                }

            }catch(Exception e)
            {
                nStart = 0;
                e.printStackTrace();
            }
            mObjectMap.put("InvoiceNote", Global.gUOrderHInfo.strSpecialInstructions.substring(nStart+2) );
        }

        List<PrintTaxRateInfo> taxRateInfoList = new ArrayList<>();
        float cdsAmount = 0;
        for (Map.Entry<String, PrintTaxRateInfo> entry : Global.gUOrderHInfo.cdsMap.entrySet())
        {
            PrintTaxRateInfo taxRateInfo = entry.getValue();
            if( taxRateInfo.strTaxCode != null && taxRateInfo.strTaxCode.length() > 0 && !taxRateInfo.strTaxCode.equals("0") && taxRateInfo.nBottles != 0)
            {
                taxRateInfoList.add(taxRateInfo);
            }
        }

        Collections.sort(taxRateInfoList, new Comparator<PrintTaxRateInfo>() {
            @Override
            public int compare(PrintTaxRateInfo printTaxRateInfo, PrintTaxRateInfo t1) {
                return printTaxRateInfo.strTaxCode.compareTo(t1.strTaxCode);
            }
        });

        List<Map<String, String>> cdsMapList = new ArrayList<>();
        for(PrintTaxRateInfo taxRateInfo : taxRateInfoList)
        {
            Map<String, String> cdsMap = new HashMap<>();
            cdsMap.put("CDSLevel", String.format(Locale.ENGLISH, "%d bottles @ %s", taxRateInfo.nBottles, taxRateInfo.strRateDescription));
            cdsMap.put("CDSPrice", Utils.getFormattedPrice(context, Utils.get2DecimalValue(taxRateInfo.price) * taxRateInfo.nBottles));
            cdsAmount += Utils.get2DecimalValue(taxRateInfo.price) * taxRateInfo.nBottles;
            cdsMapList.add(cdsMap);
        }
        mObjectMap.put("CDSMap", cdsMapList);

        mObjectMap.put("A4SubTotal", Utils.getFormattedPrice(context, Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nDiscSales));
        mObjectMap.put("A4ShippingCost", Utils.getFormattedPrice(context, Global.gUOrderHInfo.nHandlingFee));
        mObjectMap.put("A4GST", Utils.getFormattedPrice(context, Global.gUOrderHInfo.nTaxAmount - Global.gUOrderHInfo.nPrintCDSAmount + Global.gUOrderHInfo.nHandlingFeeTaxAmount));

        if( Utils.getStringSetting(context, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") && (nPayType == 1 || nPayType == 2))
        {
            int value = (int)((nSalesAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount + Global.gUOrderHInfo.nTaxAmount - Global.gUOrderHInfo.nPrintCDSAmount + cdsAmount) * Global.MULTIPLE_VALUE);
            int nTotal = Math.abs(value);
            int nOffSet = nTotal % 5000;
            int nInvoiceTotal = ((nTotal / 5000) + ((nOffSet >= 2500) ? 1 : 0)) * 5000;
            nOffSet = nTotal % 1000;
            nTotal = ((nTotal / 1000) + ((nOffSet >= 500) ? 1 : 0)) * 1000;
            nOffSet = nInvoiceTotal - nTotal;
            float dOffSet = nOffSet / (Global.MULTIPLE_VALUE * 1f);
            float dTotal = nTotal / (Global.MULTIPLE_VALUE * 1f);
            String strOffSet= "";
            strOffSet = String.format(Locale.ENGLISH, "%.2f", Math.abs(dOffSet));
            if(value < 0)
            {
                dOffSet *= -1.0f;
            }

            if(dOffSet >= 0f)
                strOffSet = "$" + strOffSet;
            else
                strOffSet = "-$" + strOffSet;

            mObjectMap.put("A4Rounding", strOffSet);
            if( value < 0 )
                nInvoiceTotal *= -1;
            mObjectMap.put("A4Total", Utils.getFormattedPrice(context,nInvoiceTotal / (Global.MULTIPLE_VALUE * 1f)));
        }
        else
        {
            mObjectMap.put("A4Total", Utils.getFormattedPrice(context, nSalesAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount + Global.gUOrderHInfo.nTaxAmount));
        }
        if (nPayType == 0 || bFutureMode)
        {
            mObjectMap.put("A4Paid", Utils.getFormattedPrice(context, 0));
            if( Utils.getStringSetting(context, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") && (nPayType == 1 || nPayType == 2))
            {
                int nValue = (int)((nSalesAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount +Global.gUOrderHInfo.nTaxAmount - Global.gUOrderHInfo.nPrintCDSAmount + cdsAmount) * Global.MULTIPLE_VALUE);;
                int nTotal = Math.abs(nValue);
                int nOffSet = nTotal % 5000;
                int nInvoiceTotal = ((nTotal / 5000) + ((nOffSet >= 2500) ? 1 : 0)) * 5000;
                nOffSet = nTotal % 1000;
                nTotal = ((nTotal / 1000) + ((nOffSet >= 500) ? 1 : 0)) * 1000;
                nOffSet = nInvoiceTotal - nTotal;
                if( nValue < 0 )
                {
                    nInvoiceTotal *= -1;
                }

                mObjectMap.put("A4BalanceDue", Utils.getFormattedPrice(context,nInvoiceTotal / (Global.MULTIPLE_VALUE * 1f)));
            }
            else
            {
                mObjectMap.put("A4BalanceDue", Utils.getFormattedPrice(context,nSalesAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount + Global.gUOrderHInfo.nTaxAmount));
            }
        }
        else
        {
            mObjectMap.put("A4BalanceDue", Utils.getFormattedPrice(context, 0));
            if( Utils.getStringSetting(context, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") && (nPayType == 1 || nPayType == 2))
            {
                int nValue = (int)((nSalesAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount +Global.gUOrderHInfo.nTaxAmount - Global.gUOrderHInfo.nPrintCDSAmount + cdsAmount) * Global.MULTIPLE_VALUE);;
                int nTotal = Math.abs(nValue);
                int nOffSet = nTotal % 5000;
                int nInvoiceTotal = ((nTotal / 5000) + ((nOffSet >= 2500) ? 1 : 0)) * 5000;
                nOffSet = nTotal % 1000;
                nTotal = ((nTotal / 1000) + ((nOffSet >= 500) ? 1 : 0)) * 1000;
                nOffSet = nInvoiceTotal - nTotal;
                if( nValue < 0 )
                {
                    nInvoiceTotal *= -1;
                }

                mObjectMap.put("A4Paid", Utils.getFormattedPrice(context,nInvoiceTotal / (Global.MULTIPLE_VALUE * 1f)));
            }
            else
            {
                mObjectMap.put("A4Paid", Utils.getFormattedPrice(context,nSalesAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount + Global.gUOrderHInfo.nTaxAmount));
            }
        }

        List<String> footerDocText = new ArrayList<>();
        for(DocTextInfo info : mDocTextList )
        {
            if( info.nTextType == 7 && (TextUtils.isEmpty(info.strSubType) || info.strSubType.equals("P")))
            {
                if(info.strDocText.length() == 0)
                {
                    footerDocText.add(" ");
                }
                else
                {
                    footerDocText.add(info.strDocText);
                }
            }
        }
        mObjectMap.put("FooterDocText", footerDocText);

        if(nPayType == 0 || bFutureMode)
        {
            mObjectMap.put("ShowPaymentSection", "True");

            List<String> footerText = new ArrayList<>();
            for(int i = 9; i <= 13; i++)
            {
                DocTextInfo curInfo = null;
                for(DocTextInfo info:mDocTextList)
                {
                    if(info.nTextType == 6 && info.nLine == i)
                    {
                        curInfo = info;
                        break;
                    }
                }
                if(curInfo != null)
                {
                    footerText.add(curInfo.strDocText);
                }
                else
                {
                    footerText.add(" ");
                }
            }

            mObjectMap.put("FooterText", footerText);

            String customerString = "";
            if( TextUtils.isEmpty(Global.gCurCustomerInfo.strAltCustNo) )
            {
                if( Global.gCurCustomerInfo.nChainNo == 0 )
                {
                    customerString = String.valueOf(Global.gCurCustomerInfo.nCustNo);
                }
                else
                {
                    customerString = Global.gCurCustomerInfo.nChainNo + " / " + Global.gCurCustomerInfo.nCustNo;
                }
            }
            else
            {
                customerString = Global.gCurCustomerInfo.strAltCustNo;
            }

            mObjectMap.put("FooterNotes", "In order for us to allocate your payments, please use your Customer ID " + customerString + " as reference.");

            DocTextInfo curInfo = null;

            for(DocTextInfo info : mDocTextList)
            {
                if(info.nTextType == 6 && info.nLine == 1)
                {
                    curInfo = info;
                    break;
                }
            }
            if( curInfo != null )
                mObjectMap.put("CheckPayable", curInfo.strDocText.trim());
            else
                mObjectMap.put("CheckPayable", "");
        }

        /*String A4DocketTotalQuantity = "";
        if(Global.gCurCustomerInfo.strSalEntryMode.equals("B"))
        {
            if(dValue != 0)
            {
                A4DocketTotalQuantity = String.format(Locale.ENGLISH, "%d/%d\n%.2f", nUnit, nQty, dValue);
            }
            else
            {
                A4DocketTotalQuantity = String.format(Locale.ENGLISH, "%d/%d", nUnit, nQty);
            }
        }
        else
        {
            if(dValue != 0)
            {
                A4DocketTotalQuantity = String.format(Locale.ENGLISH, "%d\n%.2f", nQty, dValue);
            }
            else
            {
                A4DocketTotalQuantity = String.format(Locale.ENGLISH, "%d", nQty);
            }
        }*/
    }

    private Map<String, Integer> mCaseMap = new HashMap<>();
    private int getCaseValue(String strItemNo, Context context)
    {
        if( mCaseMap.size() == 0 )
        {
            DbAdapter adapter = new DbAdapter(context);
            adapter.open();
            adapter.getCaseValue(mCaseMap);
            adapter.close();
        }
        int nCase = 1;
        if( mCaseMap.get(strItemNo) != null )
            nCase = mCaseMap.get(strItemNo);


        if( nCase < 1 ) nCase = 1;
        return nCase;
    }

    private static Drawable dCheckDrawable = null;

    private PdfPTable createPageNumberTable(int page_index, float table_width, boolean bLast)
    {
        PdfPTable table = new PdfPTable(1);
        try {
            table.setWidths(new int[]{1});
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell cell;
            Phrase phrase;
            if(!bLast)
            {
                phrase = new Phrase("continued on next page", Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 9, Font.BOLD, BaseColor.BLACK));
                cell = new PdfPCell(phrase);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(5);
                cell.setPaddingBottom(5);
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            phrase = new Phrase("-" + page_index + "-", Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 10, Font.NORMAL, BaseColor.BLACK));
            cell = new PdfPCell(phrase);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPaddingTop(5);
            cell.setPaddingBottom(5);
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            table.addCell(cell);

            table.setLockedWidth(true);
            table.setTotalWidth(table_width);


        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return table;
    }

    private PdfPTable createContinuedTable(float table_width, boolean bLast)
    {
        PdfPTable table = new PdfPTable(2);
        try {
            table.setWidths(new int[]{1, 1});
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell cell;
            Phrase phrase;

            if (!bLast) {
                phrase = new Phrase("CONTINUED", Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 12, Font.BOLD, BaseColor.BLACK));
                cell = new PdfPCell(phrase);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(15);
                cell.setPaddingBottom(15);
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                table.addCell(cell);

                phrase = new Phrase("واصلت", Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 12, Font.BOLD, BaseColor.BLACK));
                cell = new PdfPCell(phrase);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(15);
                cell.setPaddingBottom(15);
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
                table.addCell(cell);
            }
            else {
                phrase = new Phrase(" ", Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 5, Font.NORMAL, BaseColor.BLACK));
                cell = new PdfPCell(phrase);
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);

                phrase = new Phrase(" ", Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 5, Font.NORMAL, BaseColor.BLACK));
                cell = new PdfPCell(phrase);
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);
            }

            table.setLockedWidth(true);
            table.setTotalWidth(table_width);

        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return table;
    }

    private PdfPTable createSpaceTable(float space, float table_width, int nLineType)
    {
        float fCurHeight;
        PdfPTable table = new PdfPTable(1);
        try {
            table.setWidths(new int[]{1});
            table.getDefaultCell().setBorder(nLineType);

            PdfPCell cell = new PdfPCell();
            cell.setBorder(nLineType);
            Phrase phrase = new Phrase(" ", Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 9, Font.BOLD, BaseColor.BLACK));
            cell.addElement(phrase);
            table.addCell(phrase);
            table.setLockedWidth(true);
            table.setTotalWidth(table_width);
            float fHeight = table.getTotalHeight();
            fCurHeight = fHeight;

            int nCellCnt = (int)((space - fCurHeight) / fHeight);
            for(int i = 0; i < nCellCnt; i++)
            {
                cell = new PdfPCell();
                cell.setBorder(nLineType);
                phrase = new Phrase(" ", Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 9, Font.BOLD, BaseColor.BLACK));
                cell.addElement(phrase);
                table.addCell(phrase);
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return table;
    }
    public Document createDocument(Context context, boolean bDuplicated, String strPath, int nType)
    {
        Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(context, Global.PREF_LOGIN_DATE) + "/");
        Utils.createDirectory(Global.PDF_LOCAL_DIR);
        dCheckDrawable = context.getResources ().getDrawable (R.drawable.ic_check);

        String strPrinterSize = Utils.getStringSetting(context, Global.PREF_PRINTER_SIZE);
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document document = null;
            if( nType == Global.SALE_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_TEMPLATE_INVOICE_FMT_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if( nType == Global.ORDER_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_TEMPLATE_ORDERACKFMT_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if( nType == Global.SALE_DOCKET_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_TEMPLATE_DELIVERY_FMT_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if( nType == Global.LOAD_SHEET_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_LOAD_SHEET_TEMPLATE_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if( nType == Global.TRUCK_INVENTORY_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_TRUCK_INVENTORY_TEMPLATE_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if( nType == Global.LOAD_REQUEST_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_LOAD_REQUEST_TEMPLATE_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if( nType == Global.LOAD_ADJUSTMENT_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_LOAD_ADJUSTMENT_TEMPLATE_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if( nType == Global.LOAD_STOCKTAKE_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_STOCKTAKE_COUNT_TEMPLATE_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if( nType == Global.PICK_SLIP_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_PICK_SLIP_TEMPLATE_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if( nType == Global.TERMS_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_TERMS_TEMPLATE_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if( nType == Global.SALES_PLAN_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_SALES_PLAN_TEMPLATE_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if( nType == Global.OFFLOAD_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_OFFLOAD_TEMPLATE_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if( nType == Global.CASH_PRINT )
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_RECEIPT_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if(nType == Global.COLLECTION_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_COLLECTION_REPORT_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if(nType == Global.TRANSACTION_HISTORY_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_TRANSACTION_HISTORY_REPORT_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if(nType == Global.CONSIGNMENT_COUNT_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_CONSIGNMENT_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if(nType == Global.CONSIGNMENT_INVENTORY_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_CONSIGNMENT_INVENTORY_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if(nType == Global.CONSIGNMENT_DOCKET_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_CONSIGNMENT_DOCKET_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if(nType == Global.BANKING_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_BANKING_TEMPLATE_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if(nType == Global.A4_INVOICE_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_A4_INVOICE_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if(nType == Global.A4_DOCKET_PRINT)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_A4_DOCKET_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if(nType == Global.PRINT_EQUIPMENT_DOC)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_A4_NEW_EQUIPMENT_DOC_FMT_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }
            else if(nType == Global.PRINT_SURVEY_DOC)
            {
                String strTemplateFileName = Utils.getFinalPrintTemplateFileName(context, Global.PRINT_A4_SURVEY_DOC_FMT_FILENAME, strPrinterSize);
                document = builder.parse(new File(mTemplatePath + "/" + strTemplateFileName));
            }

            NodeList elements = document.getElementsByTagName("Document");
            Node RootElement = elements.item(0);

            String documentTypeStr = "";
            try
            {
                Node documentTypeNode = RootElement.getAttributes().getNamedItem("type");
                if(documentTypeNode != null)
                {
                    documentTypeStr = documentTypeNode.getNodeValue();
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }

            String pageWidthStr = "";
            try
            {
                Node pageWidthNode = RootElement.getAttributes().getNamedItem("pageWidth");
                if(pageWidthNode != null)
                {
                    pageWidthStr = pageWidthNode.getNodeValue();
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }

            if(TextUtils.isEmpty(pageWidthStr))
            {
                try
                {
                    Node pageWidthNode = RootElement.getAttributes().getNamedItem("pageSize");
                    if( pageWidthNode != null )
                    {
                        pageWidthStr = pageWidthNode.getNodeValue();
                    }
                }catch ( Exception e)
                {
                    e.printStackTrace();
                }

            }

            String marginLeftStr = "0";
            try{
                Node marginNode = RootElement.getAttributes().getNamedItem("marginLeft");
                if(marginNode != null)
                {
                    marginLeftStr = marginNode.getNodeValue();
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }

            String marginRightStr = "0";
            try{
                Node marginNode = RootElement.getAttributes().getNamedItem("marginRight");
                if(marginNode != null)
                {
                    marginRightStr = marginNode.getNodeValue();
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }

            String marginTopStr = "0";
            try{
                Node marginNode = RootElement.getAttributes().getNamedItem("marginTop");
                if(marginNode != null)
                {
                    marginTopStr = marginNode.getNodeValue();
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }

            String marginBottomStr = "0";
            try{
                Node marginNode = RootElement.getAttributes().getNamedItem("marginBottom");
                if(marginNode != null)
                {
                    marginBottomStr = marginNode.getNodeValue();
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }


            float marginLeft = Float.parseFloat(marginLeftStr);
            float marginRight = Float.parseFloat(marginRightStr);
            float marginTop = Float.parseFloat(marginTopStr);
            float marginBottom = Float.parseFloat(marginBottomStr);

            Document pdfDocument = null;
            List<PdfPTable> tables = new ArrayList<>();
            if(pageWidthStr.equals("A4"))
            {
                Map<String, PdfPTable> tableMap = new HashMap<>();
                NodeList nodes = RootElement.getChildNodes();
                int nLength = nodes.getLength();
                for(int i = 0; i < nLength; i++)
                {
                    Node node = nodes.item(i);
                    if( node.getNodeType() != Element.ELEMENT_NODE) continue;
                    if( node.getNodeName().equalsIgnoreCase("mobile")) continue;
                    if( node.getNodeName().equalsIgnoreCase("pdf"))
                    {
                        NodeList childrenNodes = node.getChildNodes();
                        int nChildLength = childrenNodes.getLength();
                        for(int j = 0; j < nChildLength; j++)
                        {
                            Node childNode = childrenNodes.item(j);
                            if( childNode.getNodeType() != Element.ELEMENT_NODE) continue;
                            if( childNode.getNodeName().equalsIgnoreCase("Table"))
                            {
                                NamedNodeMap attrMap = childNode.getAttributes();
                                String reserved = "";
                                try
                                {
                                    Node columnCountAttrNode = attrMap.getNamedItem("Reserved");
                                    if( columnCountAttrNode != null )
                                    {
                                        reserved = columnCountAttrNode.getNodeValue().trim();
                                    }
                                }catch(Exception e)
                                {
                                    e.printStackTrace();
                                }

                                PdfPTable table = createPdfTable(childNode);
                                if(table != null)
                                {
                                    if(!TextUtils.isEmpty(reserved))
                                    {
                                        table.setLockedWidth(true);
                                        table.setTotalWidth(PageSize.A4.getWidth() - 40);
                                        tableMap.put(reserved, table);
                                    }
                                    else
                                    {
                                        tables.add(table);
                                    }

                                }
                            }
                        }
                    }
                    if( node.getNodeName().equalsIgnoreCase("Table"))
                    {
                        NamedNodeMap attrMap = node.getAttributes();
                        String reserved = "";
                        try
                        {
                            Node columnCountAttrNode = attrMap.getNamedItem("Reserved");
                            if( columnCountAttrNode != null )
                            {
                                reserved = columnCountAttrNode.getNodeValue().trim();
                            }
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }

                        PdfPTable table = createPdfTable(node);
                        if(table != null)
                        {
                            if(!TextUtils.isEmpty(reserved))
                            {
                                table.setLockedWidth(true);
                                table.setTotalWidth(PageSize.A4.getWidth() - 40);
                                tableMap.put(reserved, table);
                            }
                            else
                            {
                                tables.add(table);
                            }
                        }
                    }
                }

                if(tableMap.size() > 0)
                {
                    float verticalMargin = 40;
                    if (documentTypeStr.equals(Global.PRINT_DOCUMENT_ALHASA_TYPE_ID)) {
                        verticalMargin = 160;
                    }
                    PdfPTable pHeaderTable = tableMap.get("EveryPage|Top");
                    PdfPTable pFooterTable = tableMap.get("LastPage|Bottom");
                    PdfPTable pFetchTable = tableMap.get("Fetch");
                    float fTotalHeight = pHeaderTable.getTotalHeight() + pFooterTable.getTotalHeight() + pFetchTable.getTotalHeight();
                    float fContentHeightWithHeader = pHeaderTable.getTotalHeight() + pFetchTable.getTotalHeight();
                    if(fTotalHeight < PageSize.A4.getHeight() - verticalMargin) {
                        //Add the Fetch table.
                        PdfPTable pSpaceTable = createSpaceTable(PageSize.A4.getHeight() - fTotalHeight - verticalMargin, PageSize.A4.getWidth() - 40, Rectangle.LEFT | Rectangle.RIGHT);
                        tables.add(pHeaderTable);
                        tables.add(pFetchTable);
                        tables.add(pSpaceTable);
                        tables.add(pFooterTable);
                    }
                    else if( fContentHeightWithHeader < PageSize.A4.getHeight() - verticalMargin){
                        PdfPTable pPageNumberTable;
                        if (documentTypeStr.equals(Global.PRINT_DOCUMENT_ALHASA_TYPE_ID)) {
                            pPageNumberTable = createContinuedTable(PageSize.A4.getWidth() - 40, false);
                        }
                        else {
                            pPageNumberTable = createPageNumberTable(1, PageSize.A4.getWidth() - 40, false);
                        }
                        PdfPTable pSpaceTable = createSpaceTable(PageSize.A4.getHeight() - fContentHeightWithHeader - verticalMargin - pPageNumberTable.getTotalHeight(), PageSize.A4.getWidth() - 40, Rectangle.LEFT|Rectangle.RIGHT|Rectangle.BOTTOM);
                        tables.add(pHeaderTable);
                        tables.add(pFetchTable);
                        tables.add(pSpaceTable);
                        tables.add(pPageNumberTable);
                        tables.add(pFooterTable);
                        if (documentTypeStr.equals(Global.PRINT_DOCUMENT_ALHASA_TYPE_ID)) {
                            pPageNumberTable = createContinuedTable(PageSize.A4.getWidth() - 40, true);
                        }
                        else {
                            pPageNumberTable = createPageNumberTable(2, PageSize.A4.getWidth() - 40, true);
                        }
                        pSpaceTable = createSpaceTable(PageSize.A4.getHeight() - pFooterTable.getTotalHeight() - verticalMargin - pPageNumberTable.getTotalHeight(), PageSize.A4.getWidth() - 40, Rectangle.LEFT | Rectangle.RIGHT);
                        tables.add(pSpaceTable);
                        tables.add(pPageNumberTable);
                    }
                    else {
                        int page_index = 1;
                        float curPageHeight = 0;
                        PdfPTable pNewTable = null;
                        PdfPTable pPageNumberTable;
                        if (documentTypeStr.equals(Global.PRINT_DOCUMENT_ALHASA_TYPE_ID)) {
                            pPageNumberTable = createContinuedTable(PageSize.A4.getWidth() - 40, false);
                        }
                        else {
                            pPageNumberTable = createPageNumberTable(page_index, PageSize.A4.getWidth() - 40, false);
                        }
                        for(PdfPRow pdfPRow : pFetchTable.getRows())
                        {
                            if(pNewTable == null)
                            {
                                pNewTable = PdfPTable.shallowCopy(pFetchTable);
                                pNewTable.setLockedWidth(true);
                                pNewTable.setTotalWidth(PageSize.A4.getWidth() - 40);
                            }

                            for(PdfPCell cell : pdfPRow.getCells())
                            {
                                if(cell == null) continue;
                                pNewTable.addCell(new PdfPCell(cell));
                            }

                            curPageHeight = pNewTable.calculateHeights();

                            if(pPageNumberTable.getTotalHeight() + pHeaderTable.getTotalHeight() + curPageHeight > PageSize.A4.getHeight() - verticalMargin)
                            {
                                pNewTable.getRows().remove(pNewTable.getRows().size() - 1);

                                tables.add(pHeaderTable);
                                tables.add(pNewTable);
                                tables.add(pPageNumberTable);

                                if (documentTypeStr.equals(Global.PRINT_DOCUMENT_ALHASA_TYPE_ID)) {
                                    PdfPTable pSpaceTable = createSpaceTable(PageSize.A4.getHeight() - verticalMargin - pHeaderTable.getTotalHeight() - pNewTable.calculateHeights() - pPageNumberTable.getTotalHeight(), PageSize.A4.getWidth() - 40, Rectangle.LEFT | Rectangle.RIGHT);
                                    tables.add(pSpaceTable);
                                }

                                pNewTable = PdfPTable.shallowCopy(pFetchTable);
                                pNewTable.getRows().clear();
                                pNewTable.setLockedWidth(true);
                                pNewTable.setTotalWidth(PageSize.A4.getWidth() - 40);

                                for(PdfPCell cell : pdfPRow.getCells())
                                {
                                    if(cell == null) continue;
                                    pNewTable.addCell(new PdfPCell(cell));
                                }

                                page_index++;
                                if (documentTypeStr.equals(Global.PRINT_DOCUMENT_ALHASA_TYPE_ID)) {
                                    pPageNumberTable = createContinuedTable(PageSize.A4.getWidth() - 40, false);
                                }
                                else {
                                    pPageNumberTable = createPageNumberTable(page_index, PageSize.A4.getWidth() - 40, false);
                                }
                            }
                        }

                        if(pNewTable.getRows().size() > 0)
                        {
                            if (documentTypeStr.equals(Global.PRINT_DOCUMENT_ALHASA_TYPE_ID)) {
                                pPageNumberTable = createContinuedTable(PageSize.A4.getWidth() - 40, true);
                            }
                            else {
                                pPageNumberTable = createPageNumberTable(page_index, PageSize.A4.getWidth() - 40, true);
                            }

                            tables.add(pHeaderTable);
                            tables.add(pNewTable);

                            if(pHeaderTable.getTotalHeight() + pNewTable.calculateHeights() + pFooterTable.getTotalHeight() + pPageNumberTable.getTotalHeight() < PageSize.A4.getHeight() - 40)
                            {
                                PdfPTable pSpaceTable = createSpaceTable(PageSize.A4.getHeight() - verticalMargin - pHeaderTable.getTotalHeight() - pNewTable.getTotalHeight() - pFooterTable.getTotalHeight() - pPageNumberTable.getTotalHeight(), PageSize.A4.getWidth() - 40, Rectangle.LEFT | Rectangle.RIGHT);
                                tables.add(pSpaceTable);
                                tables.add(pFooterTable);
                                tables.add(pPageNumberTable);
                            }
                            else
                            {
                                if (documentTypeStr.equals(Global.PRINT_DOCUMENT_ALHASA_TYPE_ID)) {
                                    pPageNumberTable = createContinuedTable(PageSize.A4.getWidth() - 40, false);
                                }
                                else {
                                    pPageNumberTable = createPageNumberTable(page_index, PageSize.A4.getWidth() - 40, false);
                                }
                                tables.add(pPageNumberTable);

                                PdfPTable pSpaceTable = createSpaceTable(PageSize.A4.getHeight() - verticalMargin - pHeaderTable.getTotalHeight() - pNewTable.getTotalHeight() - pPageNumberTable.getTotalHeight(), PageSize.A4.getWidth() - 40, Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
                                tables.add(pSpaceTable);

                                if (!documentTypeStr.equals(Global.PRINT_DOCUMENT_ALHASA_TYPE_ID)) {
                                    pPageNumberTable = createContinuedTable(PageSize.A4.getWidth() - 40, true);
                                }
                                else {
                                    pPageNumberTable = createPageNumberTable(page_index + 1, PageSize.A4.getWidth() - 40, true);
                                }
                                tables.add(pFooterTable);
                                tables.add(pPageNumberTable);
                            }
                        }
                    }
                }
                pdfDocument = new Document(PageSize.A4, marginLeft, marginRight, marginTop, marginBottom);
            }
            else {
                float pageWidth = Float.parseFloat(pageWidthStr);
                float fHeight = 0.0f;
                tables = new ArrayList<>();
                if( bDuplicated )
                {
                    PdfPTable table = new PdfPTable(1);
                    table.setWidths(new int[]{1});
                    table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                    Phrase phrase = new Phrase("DUPLICATE", Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 7, Font.BOLD, BaseColor.BLACK));
                    PdfPCell cell = new PdfPCell(phrase);
                    cell.setPaddingTop(5);
                    cell.setPaddingBottom(10);
                    cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    cell.setBorder(Rectangle.NO_BORDER);
                    table.addCell(cell);
                    tables.add(table);
                    fHeight += table.getTotalHeight();
                }

                NodeList nodes = RootElement.getChildNodes();
                int nLength = nodes.getLength();

                for(int i = 0; i < nLength; i++)
                {
                    Node node = nodes.item(i);
                    if( node.getNodeType() != Element.ELEMENT_NODE) continue;
                    if( node.getNodeName().equalsIgnoreCase("Table"))
                    {
                        PdfPTable table = createPdfTable(node);
                        if(table != null)
                        {
                            table.setLockedWidth(true);
                            table.setTotalWidth(pageWidth - marginLeft - marginRight);
                            fHeight += table.getTotalHeight();
                            tables.add(table);

                        }
                    }
                }


                pdfDocument = new Document(new Rectangle(0, 0, pageWidth, fHeight + 50.0f), marginLeft, marginRight, marginTop, marginBottom);

            }

            PdfWriter.getInstance(pdfDocument, new FileOutputStream(strPath));

            pdfDocument.open();
            for(PdfPTable table : tables){
                try {
                    pdfDocument.add(table);
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
            pdfDocument.close();
            return pdfDocument;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private PdfPTable createPdfTable(Node element) {
        PdfPTable table = null;
        NamedNodeMap attrMap = element.getAttributes();
        int nColumnCnt = 1;
        String border = "";
        try
        {
            Node columnCountAttrNode = attrMap.getNamedItem("columnCount");
            if( columnCountAttrNode != null )
            {
                nColumnCnt = Integer.parseInt(columnCountAttrNode.getNodeValue());
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            Node columnCountAttrNode = attrMap.getNamedItem("border");
            if( columnCountAttrNode != null )
            {
                border = columnCountAttrNode.getNodeValue().trim();
            }
        }catch (Exception e)
        {

        }

        if( nColumnCnt > 1 )
        {
            int[] nWeight = new int[nColumnCnt];
            for(int i = 1; i <= nColumnCnt; i++)
            {
                try {
                    Node columnWeightNode = attrMap.getNamedItem("column" + i + "Weight");
                    nWeight[i - 1] = Integer.parseInt(columnWeightNode.getNodeValue());
                }catch(Exception e)
                {
                    e.printStackTrace();
                    nWeight[i - 1] = 1;
                }
            }
            try
            {
                table = new PdfPTable(nColumnCnt);
                table.setWidths(nWeight);
                if(TextUtils.isEmpty(border))
                {
                    table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                }
                else if(border.equals("box"))
                {
                    table.getDefaultCell().setBorder(Rectangle.BOX);
                }
                else if(border.equals("bottom"))
                {
                    table.getDefaultCell().setBorder(Rectangle.BOTTOM);
                }
                else if(border.equals("top"))
                {
                    table.getDefaultCell().setBorder(Rectangle.TOP);
                }
                else if(border.equals("top|bottom"))
                {
                    table.getDefaultCell().setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                }
                else if(border.equals("left"))
                {
                    table.getDefaultCell().setBorder(Rectangle.LEFT);
                }
                else if(border.equals("right"))
                {
                    table.getDefaultCell().setBorder(Rectangle.RIGHT);
                }
            }catch(Exception e)
            {
                e.printStackTrace();
                table = null;
            }
        }
        else
        {
            try {
                table = new PdfPTable(1);
                table.setWidths(new int[]{1});
                if(TextUtils.isEmpty(border))
                {
                    table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                }
                else if(border.equals("box"))
                {
                    table.getDefaultCell().setBorder(Rectangle.BOX);
                }
                else if(border.equals("bottom"))
                {
                    table.getDefaultCell().setBorder(Rectangle.BOTTOM);
                }
                else if(border.equals("top"))
                {
                    table.getDefaultCell().setBorder(Rectangle.TOP);
                }
                else if(border.equals("top|bottom"))
                {
                    table.getDefaultCell().setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                }
                else if(border.equals("left"))
                {
                    table.getDefaultCell().setBorder(Rectangle.LEFT);
                }
                else if(border.equals("right"))
                {
                    table.getDefaultCell().setBorder(Rectangle.RIGHT);
                }
            }catch(Exception e)
            {
                e.printStackTrace();
                table = null;
            }
        }

        NodeList nodes = element.getChildNodes();
        for(int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            if( node.getNodeType() != Node.ELEMENT_NODE )
                continue;
            if( node.getNodeName().equals("Cell") )
            {
                PdfPCell cell = getCell(node);
                if ( cell != null)
                {
                    table.addCell(cell);
                }
            }
            else if( node.getNodeName().equals("ObjectArray") )
            {
                NamedNodeMap childAttrMap = node.getAttributes();

                final String strSort = getAttrValue(childAttrMap, "sort");
                String[] strSorts = strSort.split(",");
                if(!TextUtils.isEmpty(strSort))
                {
                    Node node1 = childAttrMap.getNamedItem("name");
                    String strMapKey = node1.getNodeValue();
                    Object o = mObjectMap.get(strMapKey);
                    if(o != null && o instanceof List)
                    {
                        List items = (List)o;
                        List newItems = new ArrayList();
                        List subItems = new ArrayList();

                        for(Object obj : items)
                        {
                            if(obj instanceof Map && ((Map)obj).get("sort") != null &&  ((String)((Map)obj).get("sort")).equals("-1"))
                            {
                                if(subItems.size() > 0)
                                {
                                    Utils.sort(subItems, Arrays.asList(strSorts));
                                    newItems.addAll(subItems);
                                    subItems.clear();
                                }
                                newItems.add(obj);
                            }
                            else
                            {
                                subItems.add(obj);
                            }
                        }

                        if(subItems.size() > 0)
                        {
                            Utils.sort(subItems, Arrays.asList(strSorts));
                            newItems.addAll(subItems);
                            subItems.clear();
                        }
                        items.clear();
                        items.addAll(newItems);
                    }
                }
                List<PdfPCell> cells = getCells(node);
                for(PdfPCell cell : cells)
                {
                    table.addCell(cell);
                }
            }
        }

        return table;
    }

    private List<PdfPCell> getCells(Node element) {
        NamedNodeMap attrMap = element.getAttributes();
        String strMapKey = "";
        try
        {
            List<PdfPCell> cells = new ArrayList<>();
            Node node = attrMap.getNamedItem("name");
            strMapKey = node.getNodeValue();
            boolean bCheckMode = false;
            if( strMapKey.startsWith("Is") )
            {
                strMapKey = strMapKey.substring(2);
                bCheckMode = true;
            }
            if( bCheckMode )
            {
                Object o = mObjectMap.get(strMapKey);
                if(o != null && o instanceof List)
                {
                    List items = (List)o;
                    if( items.size() > 0 )
                    {
                        NodeList nodes = element.getChildNodes();
                        for(int i = 0; i < nodes.getLength(); i++){
                            Node cellItem = nodes.item(i);
                            if( cellItem.getNodeType() != Node.ELEMENT_NODE )
                                continue;
                            PdfPCell cell = getCell(cellItem);
                            if( cell != null)
                                cells.add(cell);
                        }
                    }
                }
                else if( o != null )
                {
                    NodeList nodes = element.getChildNodes();
                    for(int i = 0; i < nodes.getLength(); i++){
                        Node cellItem = nodes.item(i);
                        if( cellItem.getNodeType() != Node.ELEMENT_NODE )
                            continue;
                        PdfPCell cell = getCell(cellItem);
                        if( cell != null)
                            cells.add(cell);
                    }
                }
            }
            else
            {
                Object o = mObjectMap.get(strMapKey);
                if(o instanceof List)
                {
                    List items = (List)o;
                    for(Object itemObject : items)
                    {
                        mArrayObjectMap.put(strMapKey, itemObject);
                        NodeList nodes = element.getChildNodes();
                        for(int i = 0; i < nodes.getLength(); i++){
                            Node cellItem = nodes.item(i);
                            if( cellItem.getNodeType() != Node.ELEMENT_NODE )
                                continue;
                            PdfPCell cell = getCell(cellItem);
                            if( cell != null)
                                cells.add(cell);
                        }
                    }
                }
            }
            return cells;
        }catch(Exception e)
        {
            e.printStackTrace();
            return new ArrayList<>();
        }

    }

    private PdfPCell getCell(Node element) {
        NamedNodeMap attrMap = element.getAttributes();

        String strSpan = getAttrValue(attrMap, "span");
        String strBorder = getAttrValue(attrMap, "border");
        String strPadding = getAttrValue(attrMap, "padding");
        String strPaddingLeft = getAttrValue(attrMap, "paddingLeft");
        String strPaddingRight = getAttrValue(attrMap, "paddingRight");
        String strPaddingTop = getAttrValue(attrMap, "paddingTop");
        String strPaddingBottom = getAttrValue(attrMap, "paddingBottom");
        String strReverse = getAttrValue(attrMap, "reverse");
        String strHorzAlignment = getAttrValue(attrMap, "horzAlignment");
        String strVertAlignment = getAttrValue(attrMap, "vertAlignment");

        int nSpan = 1;
        if( strSpan.length() > 0 )
        {
            try
            {
                nSpan = Integer.parseInt(strSpan);
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        float padding = 0.0f;
        if( strPadding.length() > 0 )
        {
            try
            {
                padding = Float.parseFloat(strPadding);
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        float paddingLeft = 0.0f;
        if( strPaddingLeft.length() > 0 )
        {
            try
            {
                paddingLeft = Float.parseFloat(strPaddingLeft);
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        float paddingRight = 0.0f;
        if( strPaddingRight.length() > 0 )
        {
            try
            {
                paddingRight = Float.parseFloat(strPaddingRight);
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        float paddingTop = 0.0f;
        if( strPaddingTop.length() > 0 )
        {
            try
            {
                paddingTop = Float.parseFloat(strPaddingTop);
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        float paddingBottom = 0.0f;
        if( strPaddingBottom.length() > 0 )
        {
            try
            {
                paddingBottom = Float.parseFloat(strPaddingBottom);
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        int bReverse = 0;
        if(strReverse.length() > 0 && strReverse.equals("1"))
        {
            bReverse = 1;
        }
        else if(strReverse.length() > 0 && strReverse.equals("2"))
        {
            bReverse = 2;
        }

        int nBorderValue = 0;
        String [] strBorderValues = strBorder.split(",");
        for(int i = 0; i < strBorderValues.length; i++)
        {
            String value = strBorderValues[i].trim();
            if( value.equalsIgnoreCase("top") )
                nBorderValue |= Rectangle.TOP;
            else if( value.equalsIgnoreCase("bottom"))
                nBorderValue |= Rectangle.BOTTOM;
            else if( value.equalsIgnoreCase("left"))
                nBorderValue |= Rectangle.LEFT;
            else if( value.equalsIgnoreCase("right"))
                nBorderValue |= Rectangle.RIGHT;
            else if( value.equalsIgnoreCase("box"))
                nBorderValue |= Rectangle.BOX;
        }

        int nHorzAlignment = 0;
        if( strHorzAlignment.equalsIgnoreCase("left") )
        {
            nHorzAlignment = com.itextpdf.text.Element.ALIGN_LEFT;
        }
        else if( strHorzAlignment.equalsIgnoreCase("right") )
        {
            nHorzAlignment = com.itextpdf.text.Element.ALIGN_RIGHT;
        }
        else if( strHorzAlignment.equalsIgnoreCase("center") )
        {
            nHorzAlignment = com.itextpdf.text.Element.ALIGN_CENTER;
        }

        int nVertAlignment = 0;
        if( strVertAlignment.equalsIgnoreCase("left") )
        {
            nVertAlignment = com.itextpdf.text.Element.ALIGN_LEFT;
        }
        else if( strVertAlignment.equalsIgnoreCase("right") )
        {
            nVertAlignment = com.itextpdf.text.Element.ALIGN_RIGHT;
        }
        else if( strVertAlignment.equalsIgnoreCase("center") )
        {
            nVertAlignment = com.itextpdf.text.Element.ALIGN_CENTER;
        }
        else if( strVertAlignment.equalsIgnoreCase("top") )
        {
            nVertAlignment = com.itextpdf.text.Element.ALIGN_TOP;
        }
        else if( strVertAlignment.equalsIgnoreCase("middle") )
        {
            nVertAlignment = com.itextpdf.text.Element.ALIGN_MIDDLE;
        }
        else if( strVertAlignment.equalsIgnoreCase("bottom") )
        {
            nVertAlignment = com.itextpdf.text.Element.ALIGN_BOTTOM;
        }

        PdfPCell cell = null;
        NodeList childNodes = element.getChildNodes();
        for(int i = 0; i < childNodes.getLength(); i++)
        {
            Node childNode = childNodes.item(i);
            if( childNode.getNodeType() != Element.ELEMENT_NODE )
                continue;
            if( childNode.getNodeName().equalsIgnoreCase("Table") )
            {
                PdfPTable table = createPdfTable(childNode);
                if(table != null)
                {
                    cell = new PdfPCell(table);
                    break;
                }

            }
            else if( childNode.getNodeName().equalsIgnoreCase("Phrase") )
            {
                Phrase phrase = getPhrase(childNode);
                if( phrase != null )
                {
                    cell = new PdfPCell(phrase);
                    List<Chunk> chunks = phrase.getChunks();
                    for (int k = 0; k < chunks.size(); k++) {
                        Chunk chunk = chunks.get(k);
                        String content = chunk.getContent();
                        if (Utils.isProbablyArabic(content)) {
                            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
                            break;
                        }
                    }
                    break;
                }
            }
            else if( childNode.getNodeName().equalsIgnoreCase("Image") )
            {
                Image image = getImage(childNode);
                if( image != null )
                {
                    cell = new PdfPCell(image);
                    break;
                }
                else
                {
                    if(childNode.getAttributes().getNamedItem("type") != null)
                    {
                        String strType = childNode.getAttributes().getNamedItem("type").getNodeValue();
                        if(strType.equalsIgnoreCase("image"))
                        {
                            break;
                        }
                    }
                    cell = new PdfPCell(new Phrase("No Signature File", Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 11, Font.BOLD, BaseColor.BLACK)));
                    cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    cell.setBorder(nBorderValue);
                    cell.setPaddingTop(10);
                    return cell;
                }
            }
        }
        if(cell == null)
        {
            cell = new PdfPCell();
        }
        cell.setBorder(nBorderValue);
        if( padding > 0.0f)
        {
            cell.setPadding(padding);
        }
        if(paddingLeft > 0.0f )
        {
            cell.setPaddingLeft(paddingLeft);
        }
        if( paddingRight > 0.0f)
        {
            cell.setPaddingRight(paddingRight);
        }
        if( paddingBottom > 0.0f )
        {
            cell.setPaddingBottom(paddingBottom);
        }
        if( paddingTop > 0.0f)
        {
            cell.setPaddingTop(paddingTop);
        }
        cell.setColspan(nSpan);

        if( nHorzAlignment >= com.itextpdf.text.Element.ALIGN_LEFT )
            cell.setHorizontalAlignment(nHorzAlignment);

        if( nVertAlignment >= com.itextpdf.text.Element.ALIGN_TOP || nVertAlignment == com.itextpdf.text.Element.ALIGN_CENTER)
            cell.setVerticalAlignment(nVertAlignment);

        if(bReverse == 1)
        {
            cell.setBackgroundColor(BaseColor.BLACK);
        }
        else if(bReverse == 2)
        {
            cell.setBackgroundColor(BaseColor.GRAY);
        }
        return cell;
    }

    private Image getImage(Node element) {
        NamedNodeMap attrMap = element.getAttributes();
        String widthStr = getAttrValue(attrMap, "width");
        String heightStr = getAttrValue(attrMap, "height");

        int nWidth = 100;
        int nHeight = 100;
        try
        {
            if(!TextUtils.isEmpty(widthStr))
            {
                nWidth = Integer.parseInt(widthStr);
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if(!TextUtils.isEmpty(heightStr))
            {
                nHeight = Integer.parseInt(heightStr);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        NodeList children = element.getChildNodes();
        for(int i = 0; i < children.getLength(); i++)
        {
            Node child = children.item(i);
            if( child.getNodeType() != Node.ELEMENT_NODE)
                continue;
            if( child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equalsIgnoreCase("Object") )
            {
                Object o = getObject(child);
                if( o != null && o instanceof String)
                {
                    try
                    {
                        Image image = Image.getInstance((String)o);
                        if(nHeight < 0)
                        {
                            nHeight = (int)(nWidth * (image.getScaledHeight() / image.getScaledWidth()));
                        }
                        image.scaleToFit(nWidth, nHeight);
                        return image;
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    private Phrase getPhrase(Node element)
    {
        NamedNodeMap attrMap = element.getAttributes();
        String strSize = getAttrValue(attrMap, "size");
        String strType = getAttrValue(attrMap, "type");
        String reverse = getAttrValue(attrMap, "reverse");

        int nSize = 10;
        try
        {
            nSize = Integer.parseInt(strSize);

        }catch(Exception e)
        {
            e.printStackTrace();
        }

        int nType = 0;
        String[] strValue = strType.split(",");
        for(int i = 0; i < strValue.length; i++)
        {
            if( strValue[i].trim().equalsIgnoreCase("bold") )
            {
                nType |= Font.BOLD;
            }
            else if( strValue[i].trim().equalsIgnoreCase("italic"))
            {
                nType |= Font.ITALIC;
            }
            else if( strValue[i].trim().equalsIgnoreCase("underline"))
            {
                nType |= Font.UNDERLINE;
            }
        }

        Font font;
        if(reverse.length() > 0 && reverse.equals("1"))
        {
            font = Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), nSize, nType, BaseColor.WHITE);
        }
        else
        {
            font = Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), nSize, nType, BaseColor.BLACK);
        }

        NodeList nodeList = element.getChildNodes();
        String value = "";
        boolean bPhaseAddress2 = false;
        boolean bValid = true;
        for(int i = 0; i < nodeList.getLength(); i++)
        {
            Node child = nodeList.item(i);
            if( child.getNodeType() == Element.TEXT_NODE )
            {
                value += child.getNodeValue().trim();
            }
            else if(child.getNodeType() == Element.ELEMENT_NODE && child.getNodeName().equals("Object") )
            {
                Object obj = getObject(child);
                if(obj == null)
                {
                    bValid = false;
                    break;
                }
                if( obj instanceof String)
                {
                    String strObjName = ((Element)child).getAttribute("name");
                    if(strObjName != null && strObjName.equals("InvoiceTo4"))
                    {
                        bPhaseAddress2 = true;
                    }
                    value += (String)obj;
                }
            }
            else if(child.getNodeType() == Element.ELEMENT_NODE && child.getNodeName().equals("CreditCard") )
            {
                Phrase phrase = new Phrase();
                phrase.add(new Chunk("Visa  ", font));

                Drawable d = dCheckDrawable;
                Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] bitmapData = stream.toByteArray();
                Image image = null;
                try {
                    image = Image.getInstance(bitmapData);
                } catch (BadElementException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                image.scaleToFit(new Rectangle(0, 0, 10, 10));
                phrase.add(new Chunk(image, 0, 0, true));

                phrase.add(new Chunk("  Master Card  ", font));
                phrase.add(new Chunk(image, 0, 0, true));
                phrase.add(new Chunk("  American Express  ", font));
                phrase.add(new Chunk(image, 0, 0, true));
                return phrase;
            }
            else if(child.getNodeType() == Element.ELEMENT_NODE && child.getNodeName().equals("Cheque") )
            {
                Phrase phrase = new Phrase();
                phrase.add(new Chunk("Cheque  ", font));

                Drawable d = dCheckDrawable;
                Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] bitmapData = stream.toByteArray();
                Image image = null;
                try {
                    image = Image.getInstance(bitmapData);
                } catch (BadElementException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                image.scaleToFit(new Rectangle(0, 0, 10, 10));
                phrase.add(new Chunk(image, 0, 0, true));
                return phrase;
            }
        }

        if( value.trim().length() == 0 ) {
            if(!bPhaseAddress2)
            {
                value = "   ";
            }
            else
            {
                value = "";
            }

        }
        if(bValid)
        {
            Phrase phrase = new Phrase(value, font);
            return phrase;
        }
        else
        {
            return null;
        }

    }

    private Object getObject(Node element) {
        NamedNodeMap attrMap = element.getAttributes();
        String strName = getAttrValue(attrMap, "name");
        String strProperty = getAttrValue(attrMap, "property");

        Object obj = null;
        if( strName.length() > 0 )
        {
            obj = mArrayObjectMap.get(strName);
            if( obj == null )
                obj = mObjectMap.get(strName);
            if(obj == null )
                return null;
            if( strProperty.length() > 0 && obj instanceof Map)
            {
                Map map = (Map)obj;
                obj = map.get(strProperty);
                if( obj != null ) return obj;
                else
                    return null;
            }
            else
            {
                return obj;
            }
        }
        else
            return null;
    }

    private String getAttrValue(NamedNodeMap attrMap, String attrKey)
    {
        String strValue = "";
        try
        {
            Node spanAttrNode = attrMap.getNamedItem(attrKey);
            if( spanAttrNode != null )
            {
                strValue = spanAttrNode.getNodeValue();
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return strValue;
    }

    private String generateQRCode(String strInvoiceNo, String strTrxnDate, String strTrxnTime, String strTotalWithVAT, String strTotalVAT)
    {
        // get qrcode filename
        String qrcode_filepath = Global.QRCODE_LOCAL_DIR + strInvoiceNo + ".jpg";
        if (Utils.getFileSize(qrcode_filepath) > 0) {
            return qrcode_filepath;
        }

        // --------get the TLV hex values-------
        // seller's name
        byte[] sellerNameBytes = Utils.getTLVForValue((byte)1, "AL-HASA AUTO. BAKERY");
        byte[] vatNumberBytes = Utils.getTLVForValue((byte)2, "300379367500003");

        // qrcodeContent += "SELLER'S NAME: AL-HASA AUTO. BAKERY\n";
        // qrcodeContent += "VAT REGISTRATION NO. OF THE SELLER: 300379367500003\n";

        // generate timestamp
        SimpleDateFormat df = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT);
        SimpleDateFormat tdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = "";
        try {
            date = tdf.format(df.parse(strTrxnDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        df = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT);
        tdf = new SimpleDateFormat("HH:mm:ss");
        String time = "";
        try{
            time = tdf.format((df.parse(strTrxnTime)));
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        String timestampString = date + "T" + time + "Z";
        byte[] timestampBytes = Utils.getTLVForValue((byte)3, timestampString);

        // qrcodeContent += "TIME STAMP OF THE INVOICE: " + date + "T" + time + "Z\n";
        // qrcodeContent += "INVOICE TOTAL(WITH VAT): " + strTotalWithVAT + "\n";

        byte[] invoiceAmountBytes = Utils.getTLVForValue((byte)4, strTotalWithVAT);
        byte[] invoiceVATTotalBytes = Utils.getTLVForValue((byte)5, strTotalVAT);
        byte[] xmlInvoiceHashBytes = Utils.getTLVForValue((byte)6, "");
        byte[] ecdsaSignatureBytes = Utils.getTLVForValue((byte)7, "");
        byte[] ecdsaPublicKeyBytes = Utils.getTLVForValue((byte)8, "");
        byte[] notesBytes = Utils.getTLVForValue((byte)9, "");

        int totalLength = sellerNameBytes.length + vatNumberBytes.length + timestampBytes.length +
                invoiceAmountBytes.length + invoiceVATTotalBytes.length + xmlInvoiceHashBytes.length +
                ecdsaSignatureBytes.length + ecdsaPublicKeyBytes.length + notesBytes.length;
        byte[] resultBytes = new byte[totalLength];

        int copyPos = 0;
        System.arraycopy(sellerNameBytes, 0, resultBytes, copyPos, sellerNameBytes.length);
        copyPos += sellerNameBytes.length;
        System.arraycopy(vatNumberBytes, 0, resultBytes, copyPos, vatNumberBytes.length);
        copyPos += vatNumberBytes.length;
        System.arraycopy(timestampBytes, 0, resultBytes, copyPos, timestampBytes.length);
        copyPos += timestampBytes.length;
        System.arraycopy(invoiceAmountBytes, 0, resultBytes, copyPos, invoiceAmountBytes.length);
        copyPos += invoiceAmountBytes.length;
        System.arraycopy(invoiceVATTotalBytes, 0, resultBytes, copyPos, invoiceVATTotalBytes.length);
        copyPos += invoiceVATTotalBytes.length;
        System.arraycopy(xmlInvoiceHashBytes, 0, resultBytes, copyPos, xmlInvoiceHashBytes.length);
        copyPos += xmlInvoiceHashBytes.length;
        System.arraycopy(ecdsaSignatureBytes, 0, resultBytes, copyPos, ecdsaSignatureBytes.length);
        copyPos += ecdsaSignatureBytes.length;
        System.arraycopy(ecdsaPublicKeyBytes, 0, resultBytes, copyPos, ecdsaPublicKeyBytes.length);
        copyPos += ecdsaPublicKeyBytes.length;
        System.arraycopy(notesBytes, 0, resultBytes, copyPos, notesBytes.length);
        copyPos += notesBytes.length;

        String base64Encoded = Base64.encodeToString(resultBytes, Base64.NO_WRAP);

        Utils.createDirectory(Global.QRCODE_LOCAL_DIR);

        // generate qr code
        QrData qrData = new QrData.Text(base64Encoded);
        QrOptions options = new QrOptions.Builder(240, 240)
                .padding(0.01f)
                .errorCorrectionLevel(QrErrorCorrectionLevel.High)
                .build();
        QrCodeGenerator generator = QrCodeGenerator(ThreadPolicy.SingleThread);
        Bitmap bitmap = generator.generateQrCode(qrData, options, StandardCharsets.ISO_8859_1);

        if (bitmap == null) {
            return "";
        }

        try {
            File qrCodeFile = new File(qrcode_filepath);
            FileOutputStream mFileOutStream = new FileOutputStream(qrCodeFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, mFileOutStream);
            mFileOutStream.flush();
            mFileOutStream.close();

            return qrcode_filepath;
        }
        catch(Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

}
