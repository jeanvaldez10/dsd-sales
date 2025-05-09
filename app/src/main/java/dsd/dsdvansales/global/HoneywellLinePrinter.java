package dsd.dsdvansales.global;
import android.content.Context;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Xml;

import com.google.android.datatransport.Encoding;
import com.honeywell.mobility.print.LinePrinter;
import com.honeywell.mobility.print.LinePrinterException;
import com.honeywell.mobility.print.PrintProgressListener;
import com.honeywell.mobility.print.PrinterException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class HoneywellLinePrinter {
    public Map<String, Object> mObjectMap = new HashMap<>();
    public Map<String, Object> mArrayObjectMap = new HashMap<>();

    private HoneywellPrinter printer = null;
    private String mStrTemplateFilePath = null;

    private Arabic682x arabic = new Arabic682x();

    private String mStrDocumentType = "";
    private int mLineNo = 0;
    private int mTopPaddingLines = 0;
    private int mBottomPaddingLines = 0;
    private int mLinesPerPage = 0;

    public class HoneywellPrinter{
        private LinePrinter printer = null;

        public String strPrintText = "";
        public HoneywellPrinter(LinePrinter printer){
            this.printer = printer;
        }

        public void write(String strData) throws PrinterException {
            if(this.printer != null)
            {
                if(Utils.isProbablyArabic(strData))
                {
                    try {

                        //byte[] bytes = strData.getBytes("CP864");
                        //byte[] bytes = EncodingConverter.convertUtf8ToCp864(strData);
                        byte[] bytes = arabic.Convert(strData, false);

                        //this.printer.write(new byte[]{0x1B, 0x77, 0x25});
                        this.printer.write(bytes);

                        //this.printer.write(new byte[]{ 27, 33, 0 });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    this.printer.write(strData.getBytes());
                }
            }
            else {
                strPrintText += strData;
            }
        }

        public void writeGraphic(String strFilePath, int graphicRotationDegrees, int xOffset, int nWidth, int nHeight) throws PrinterException {
            if(this.printer != null)
            {
                // this.printer.writeGraphic(strFilePath, graphicRotationDegrees, xOffset, nWidth, nHeight);
                try {
                    File file = new File(strFilePath);
                    FileInputStream fileInputStream = new FileInputStream(file);
                    int fileLength = (int)file.length();
                    byte[] bytes = new byte[fileLength];
                    fileInputStream.read(bytes);
                    String base64QRCodePng = Base64.encodeToString(bytes, Base64.DEFAULT);

                    this.printer.writeGraphicBase64(base64QRCodePng, graphicRotationDegrees, xOffset, nWidth, nHeight);
                    this.printer.newLine(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                strPrintText += "Image";
            }
        }

        public void newLine(int numLines) throws PrinterException {
            if(this.printer != null)
            {
                this.printer.newLine(numLines);
            }
            else {
                strPrintText += "\n";
            }

        }

        public void setBold(boolean bBold) throws PrinterException {
            if(this.printer != null)
            {
                this.printer.setBold(bBold);
            }
        }

        public void setDoubleWide(boolean bDoubleWide) throws PrinterException {
            if(this.printer != null)
            {
                this.printer.setDoubleWide(bDoubleWide);
            }
        }

        public void setCompress(boolean bCompress) throws PrinterException {
            if(this.printer != null)
            {
                this.printer.setCompress(bCompress);
            }
        }
    }

    public class Section {
        public int weight = 1;
        public String strAlign = "left";
        public String strSectionText = "";

        public String getSectionText(int nTotalWeight, int nTotalLineLetterCnt)
        {
            int nMaxLetterCnt = (nTotalLineLetterCnt / nTotalWeight) * weight;
            return Utils.getHoneywellPrintString(strSectionText, nMaxLetterCnt, strAlign);
        }
    }

    public HoneywellPrinter getPrinter()
    {
        return printer;
    }

    HoneywellLinePrinter(PrintEngine printEngine, String strTemplateFilePath, LinePrinter lp)
    {
        mObjectMap.clear();
        mObjectMap.putAll(printEngine.mObjectMap);
        mArrayObjectMap.clear();

        printer = new HoneywellPrinter(lp);
        mStrTemplateFilePath = strTemplateFilePath;
    }

    public void resetDocumentData() {
        mStrDocumentType = "";
        mLineNo = 0;
        mTopPaddingLines = 0;
        mBottomPaddingLines = 0;
        mLinesPerPage = 0;
    }

    public void print()
    {
        if(printer == null)
        {
            return;
        }

        try
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document document = null;

            document = builder.parse(new File(mStrTemplateFilePath));

            resetDocumentData();

            NodeList elements = document.getElementsByTagName("Document");
            Node RootElement = elements.item(0);
            try
            {
                Node documentTypeNode = RootElement.getAttributes().getNamedItem("type");
                if(documentTypeNode != null)
                {
                    mStrDocumentType = documentTypeNode.getNodeValue();
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }

            if (mStrDocumentType.equals(Global.PRINT_DOCUMENT_ALHASA_TYPE_ID)) {
                mLinesPerPage = 65;
                mTopPaddingLines = 9;
                mBottomPaddingLines = 7;
            }
            else {
                mLinesPerPage = 65;
            }

            NodeList nodes = RootElement.getChildNodes();
            int nLength = nodes.getLength();
            for(int i = 0; i < nLength; i++)
            {
                Node node = nodes.item(i);
                if(node.getNodeType() != Element.ELEMENT_NODE) continue;
                if(node.getNodeName().equalsIgnoreCase("Image"))
                {
                    printImage(node, printer);
                }
                else if(node.getNodeName().equalsIgnoreCase("Line"))
                {
                    printLine(node, printer);
                }
                else if(node.getNodeName().equalsIgnoreCase("ObjectArray"))
                {
                    printObjectArray(node, printer);
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void printObjectArray(Node element,  HoneywellPrinter printer)
    {
        NamedNodeMap attrMap = element.getAttributes();
        String strName = getAttrValue(attrMap, "name");

        if(strName.startsWith("Is"))
        {
            String strMapKey = strName.substring(2);
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
                        if( cellItem.getNodeName().equals("Line"))
                        {
                            printLine(cellItem, printer);
                        }
                        else if( cellItem.getNodeName().equals("Image"))
                        {
                            printImage(cellItem, printer);
                        }
                    }
                }
            }
        }
        else {
            Object o = mObjectMap.get(strName);
            if(o instanceof List)
            {
                List items = (List)o;
                for(Object itemObject : items)
                {
                    mArrayObjectMap.put(strName, itemObject);
                    NodeList nodes = element.getChildNodes();
                    for(int i = 0; i < nodes.getLength(); i++){
                        Node cellItem = nodes.item(i);
                        if( cellItem.getNodeType() != Node.ELEMENT_NODE )
                            continue;
                        if( cellItem.getNodeName().equals("Line"))
                        {
                            printLine(cellItem, printer);
                        }
                        else if( cellItem.getNodeName().equals("Image"))
                        {
                            printImage(cellItem, printer);
                        }
                    }
                }
            }
        }
    }

    private void printLine(Node element, HoneywellPrinter printer)
    {
        boolean bWideLine = false;
        boolean bCompressLine = false;
        NamedNodeMap attrMap = element.getAttributes();
        try
        {
            Node columnCountAttrNode = attrMap.getNamedItem("type");
            if( columnCountAttrNode != null )
            {
                String strTypeValue = columnCountAttrNode.getNodeValue().trim();
                printer.setBold(strTypeValue.equals("bold"));
            }
            else {
                printer.setBold(false);
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            Node columnCountAttrNode = attrMap.getNamedItem("wide");
            if( columnCountAttrNode != null )
            {
                String strTypeValue = columnCountAttrNode.getNodeValue().trim();
                printer.setDoubleWide(strTypeValue.equals("true"));
                bWideLine = strTypeValue.equals("true");
            }
            else {
                printer.setDoubleWide(false);
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            Node compressAttrNode = attrMap.getNamedItem("compress");
            if( compressAttrNode != null )
            {
                String strTypeValue = compressAttrNode.getNodeValue().trim();
                printer.setCompress(strTypeValue.equals("true"));
                bCompressLine = strTypeValue.equals("true");
            }
            else {
                printer.setCompress(false);
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        String reservedValue = "";
        try
        {
            Node reservedAttrNode = attrMap.getNamedItem("Reserved");
            if( reservedAttrNode != null )
            {
                reservedValue = reservedAttrNode.getNodeValue().trim();
            }
            else {
                printer.setBold(false);
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        StringBuilder strLinePrintText = new StringBuilder();
        List<Section> sectionList = new ArrayList<>();
        NodeList nodes = element.getChildNodes();
        for(int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;
            if (node.getNodeName().equals("Section")) {
                Section section = getSectionObject(node);
                sectionList.add(section);
            }
        }

        if (mStrDocumentType.equals(Global.PRINT_DOCUMENT_ALHASA_TYPE_ID)) {
            try {
                // Do Line iteration
                int nLineInPage = mLineNo % mLinesPerPage;

                if (nLineInPage < mTopPaddingLines) {
                    int nLinesToWrite = mTopPaddingLines-nLineInPage;
                    printer.newLine(nLinesToWrite);
                    mLineNo += nLinesToWrite;
                }
                else if (nLineInPage >= mLinesPerPage-mBottomPaddingLines) {
                    int nLinesToWrite = mLinesPerPage-nLineInPage+mTopPaddingLines;
                    printer.newLine(nLinesToWrite);
                    mLineNo += nLinesToWrite;
                }

                nLineInPage = mLineNo % mLinesPerPage;

                if (reservedValue.equals("Top|EveryPage")) {
                    while (nLineInPage != mTopPaddingLines) {
                        int nLinesToWrite = mLinesPerPage-nLineInPage+mTopPaddingLines;
                        printer.newLine(nLinesToWrite);
                        mLineNo += nLinesToWrite;
                        nLineInPage = mLineNo % mLinesPerPage;
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(sectionList.size() == 0)
        {
            try {
                printer.newLine(1);
                mLineNo += 1;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            int nTotalWeight = 0;
            int nTotalLineLetterCnt = (960 / 12);
            if(bWideLine)
            {
                nTotalLineLetterCnt = (960 / 24);
            }
            else if (bCompressLine) {
                nTotalLineLetterCnt = (960 / 6);
            }
            for (Section section : sectionList) {
                nTotalWeight += section.weight;
            }

            try {
                for (Section section : sectionList) {
                    String sectionText = section.getSectionText(nTotalWeight, nTotalLineLetterCnt);
                    printer.write(sectionText);
                }
                // conclude the line
                printer.newLine(1);
                mLineNo += 1;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            printer.setDoubleWide(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            printer.setBold(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Section getSectionObject(Node element)
    {
        Section section = new Section();

        NamedNodeMap attrMap = element.getAttributes();
        try
        {
            Node columnCountAttrNode = attrMap.getNamedItem("horzAlignment");
            if( columnCountAttrNode != null )
            {
                section.strAlign = columnCountAttrNode.getNodeValue().trim();
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        try {
            Node columnCountAttrNode = attrMap.getNamedItem("weight");
            if (columnCountAttrNode != null) {
                String strTypeValue = columnCountAttrNode.getNodeValue().trim();
                section.weight = Integer.parseInt(strTypeValue);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        NodeList nodes = element.getChildNodes();
        String strValue = "";
        for(int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if( node.getNodeType() == Element.TEXT_NODE )
            {
                strValue += node.getNodeValue().trim();
            }
            else if(node.getNodeType() == Element.ELEMENT_NODE && node.getNodeName().equals("Object") )
            {
                strValue += getObject(node);
            }
        }
        section.strSectionText = strValue;
        return section;
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
    private String getObject(Node element) {
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
                if( obj != null ) return obj.toString();
                else
                    return null;
            }
            else
            {
                return obj.toString();
            }
        }
        else
            return null;
    }

    private void printImage(Node element, HoneywellPrinter printer)
    {
        NamedNodeMap attrMap = element.getAttributes();
        String strAlign = getAttrValue(attrMap, "horzAlignment");
        String strWidth = getAttrValue(attrMap, "width");
        String strHeight = getAttrValue(attrMap, "height");

        if(TextUtils.isEmpty(strWidth) || Utils.parseIntFromString(strWidth) == 0)
        {
            strWidth = "960";
        }


        NodeList nodes = element.getChildNodes();
        String strFilePath = "";
        for(int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if(node.getNodeType() == Element.ELEMENT_NODE && node.getNodeName().equals("Object") )
            {
                strFilePath = getObject(node);
                break;
            }
        }

        if(!TextUtils.isEmpty(strFilePath) && Utils.isFileExist(strFilePath))
        {
            if(TextUtils.isEmpty(strHeight))
            {
                //Calculate the height based on width, Filename is strFilePath
                int nWidth = Utils.parseIntFromString(strWidth);

                if(nWidth > 0)
                {
                    int nHeight = 0;
                    try {
                        Bitmap bitmap = Utils.getBitMapFromFile(strFilePath);
                        if(bitmap != null)
                        {
                            nHeight = (bitmap.getHeight() * nWidth) / bitmap.getWidth();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    strHeight = String.valueOf(nHeight);
                }
            }

            int xOffset = 0;
            if(strAlign.equals("center"))
            {
                xOffset = (960 - Utils.parseIntFromString(strWidth)) / 2;
            }
            else if(strAlign.equals("right"))
            {
                xOffset = 960 - Utils.parseIntFromString(strWidth);
            }

            try {
                printer.writeGraphic(strFilePath,
                        LinePrinter.GraphicRotationDegrees.DEGREE_0,
                        xOffset,  // Offset in printhead dots from the left of the page
                        Utils.parseIntFromString(strWidth), // Desired graphic width on paper in printhead dots
                        Utils.parseIntFromString(strHeight)); // Desired graphic height on paper in printhead dots
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface PrintCallback {
        void printFinished();
        void printError(String error);
    }

    public static class BadPrinterStateException extends Exception
    {
        static final long serialVersionUID = 1;

        public BadPrinterStateException(String message)
        {
            super(message);
        }
    }

    private static int m_printHeadWidth = 960;

    public static String doTestPrintDotMatric(String strPrintTemplateFilePath, PrintEngine printEngine)
    {
        HoneywellLinePrinter printer = new HoneywellLinePrinter(printEngine, strPrintTemplateFilePath, null);
        printer.print();

        return printer.getPrinter().strPrintText;
    }
    public static void doPrintDotMatrix(Context context, PrintCallback callback, String strDevAddress, String strPrintTemplateFilePath, PrintEngine printEngine)
    {
        new Thread()
        {
            @Override
            public void run() {
                LinePrinter lp = null;
                PrintProgressListener progressListener = null;
                try
                {
                    //Read the printer profile
                    AssetManager assetManager = context.getAssets();
                    InputStream input = assetManager.open("printer_profiles.JSON");
                    int initialBufferSize = 8000;
                    ByteArrayOutputStream output = new ByteArrayOutputStream(initialBufferSize);

                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = input.read(buf)) > 0)
                    {
                        output.write(buf, 0, len);
                    }
                    input.close();

                    output.flush();
                    output.close();
                    String jsonCmdAttribStr = output.toString();

                    String strPrinterURI = "bt://" + strDevAddress;
                    String strPrinterID = "P6824BT";
                    LinePrinter.ExtraSettings exSettings = new LinePrinter.ExtraSettings();
                    exSettings.setContext(context);

                    progressListener = aEvent -> {
                        // Publishes updates on the UI thread.
                        // publishProgress(aEvent.getMessageType());
                    };
                    lp = new LinePrinter(
                            jsonCmdAttribStr,
                            strPrinterID,
                            strPrinterURI,
                            exSettings);
                    // Registers to listen for the print progress events.
                    lp.addPrintProgressListener(progressListener);

                    //A retry sequence in case the bluetooth socket is temporarily not ready
                    int numtries = 0;
                    int maxretry = 2;
                    while(numtries < maxretry)
                    {
                        try{
                            lp.connect();  // Connects to the printer
                            break;
                        }
                        catch(LinePrinterException ex){
                            numtries++;
                            Thread.sleep(1000);
                        }
                    }
                    if (numtries == maxretry) lp.connect();//Final retry

                    // Check the state of the printer and abort printing if there are
                    // any critical errors detected.
                    int[] results = lp.getStatus();
                    if (results != null)
                    {
                        for (int err = 0; err < results.length; err++)
                        {
                            if (results[err] == 223)
                            {
                                // Paper out.
                                throw new BadPrinterStateException("Paper out");
                            }
                            else if (results[err] == 227)
                            {
                                // Lid open.
                                throw new BadPrinterStateException("Printer lid open");
                            }
                        }
                    }

                    HoneywellLinePrinter printer = new HoneywellLinePrinter(printEngine, strPrintTemplateFilePath, lp);
                    printer.print();
                }
                catch (BadPrinterStateException ex)
                {
                    // Stop listening for printer events.
                    lp.removePrintProgressListener(progressListener);
                    new Handler(Looper.getMainLooper()).post(() -> callback.printError("Printer error detected: " + ex.getMessage() + ". Please correct the error and try again."));
                }
                catch (LinePrinterException ex)
                {
                    new Handler(Looper.getMainLooper()).post(() -> callback.printError(ex.getMessage() + "\n\n" + ex.getErrorCode()));
                }
                catch (Exception e)
                {
                    new Handler(Looper.getMainLooper()).post(() -> callback.printError(e.getMessage()));
                    e.printStackTrace();
                }
                finally
                {
                    if (lp != null)
                    {
                        try
                        {
                            lp.disconnect();  // Disconnects from the printer
                            lp.close();  // Releases resources
                        }
                        catch (Exception ex) {}
                    }
                }

                new Handler(Looper.getMainLooper()).post(() -> callback.printFinished());
            }
        }.start();
    }

    public static void doPrint(Context context, PrintCallback callback, String strDevAddress, String strPdfPath)
    {
        new Thread()
        {
            @Override
            public void run() {
                LinePrinter lp = null;
                PrintProgressListener progressListener = null;
                try
                {
                    //Read the printer profile
                    AssetManager assetManager = context.getAssets();
                    InputStream input = assetManager.open("printer_profiles.JSON");
                    int initialBufferSize = 8000;
                    ByteArrayOutputStream output = new ByteArrayOutputStream(initialBufferSize);

                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = input.read(buf)) > 0)
                    {
                        output.write(buf, 0, len);
                    }
                    input.close();

                    output.flush();
                    output.close();
                    String jsonCmdAttribStr = output.toString();

                    String strPrinterURI = "bt://" + strDevAddress;
                    String strPrinterID = "P6824BT";
                    LinePrinter.ExtraSettings exSettings = new LinePrinter.ExtraSettings();
                    exSettings.setContext(context);

                    progressListener = aEvent -> {
                        // Publishes updates on the UI thread.
                        // publishProgress(aEvent.getMessageType());
                    };
                    lp = new LinePrinter(
                            jsonCmdAttribStr,
                            strPrinterID,
                            strPrinterURI,
                            exSettings);
                    // Registers to listen for the print progress events.
                    lp.addPrintProgressListener(progressListener);

                    //A retry sequence in case the bluetooth socket is temporarily not ready
                    int numtries = 0;
                    int maxretry = 2;
                    while(numtries < maxretry)
                    {
                        try{
                            lp.connect();  // Connects to the printer
                            break;
                        }
                        catch(LinePrinterException ex){
                            numtries++;
                            Thread.sleep(1000);
                        }
                    }
                    if (numtries == maxretry) lp.connect();//Final retry

                    // Check the state of the printer and abort printing if there are
                    // any critical errors detected.
                    int[] results = lp.getStatus();
                    if (results != null)
                    {
                        for (int err = 0; err < results.length; err++)
                        {
                            if (results[err] == 223)
                            {
                                // Paper out.
                                throw new BadPrinterStateException("Paper out");
                            }
                            else if (results[err] == 227)
                            {
                                // Lid open.
                                throw new BadPrinterStateException("Printer lid open");
                            }
                        }
                    }

                    //Convert PDF to Base 64 image.
                    File file = new File(strPdfPath);
                    ParcelFileDescriptor fileDesc = ParcelFileDescriptor.open(file, 268435456);
                    PdfRenderer renderer = new PdfRenderer(fileDesc);
                    int numPgs = renderer.getPageCount();

                    for(int i = 0; i < numPgs; ++i) {
                        PdfRenderer.Page page = renderer.openPage(i);
                        int width = page.getWidth();
                        int height = page.getHeight();
                        //float scale = (float)(m_printHeadWidth / width);
                        float scale = (float)(width / width);
                        float scaledWidth = (float)width * scale;
                        float scaledHeight = (float)height * scale;
                        Bitmap pdfImage = Bitmap.createBitmap((int)scaledWidth, (int)scaledHeight, Bitmap.Config.ARGB_8888);
                        pdfImage.eraseColor(Color.WHITE);
                        page.render(pdfImage, (Rect)null, (Matrix)null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
//                            this.writeImage(pdfImage, printHeadWidth);

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        pdfImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream .toByteArray();
                        String base64LogoPng = Base64.encodeToString(byteArray, Base64.DEFAULT);
                        lp.writeGraphicBase64(base64LogoPng,
                                LinePrinter.GraphicRotationDegrees.DEGREE_0,
                                0,  // Offset in printhead dots from the left of the page
                                m_printHeadWidth, // Desired graphic width on paper in printhead dots
                                (int)scaledHeight); // Desired graphic height on paper in printhead dots
                        lp.newLine(1);
                        page.close();
                    }

                    lp.flush();

                }
                catch (BadPrinterStateException ex)
                {
                    // Stop listening for printer events.
                    lp.removePrintProgressListener(progressListener);
                    new Handler(Looper.getMainLooper()).post(() -> callback.printError("Printer error detected: " + ex.getMessage() + ". Please correct the error and try again."));
                }
                catch (LinePrinterException ex)
                {
                    new Handler(Looper.getMainLooper()).post(() -> callback.printError(ex.getMessage() + "\n\n" + ex.getErrorCode()));
                }
                catch (Exception e)
                {
                    new Handler(Looper.getMainLooper()).post(() -> callback.printError(e.getMessage()));
                    e.printStackTrace();
                }
                finally
                {
                    if (lp != null)
                    {
                        try
                        {
                            lp.disconnect();  // Disconnects from the printer
                            lp.close();  // Releases resources
                        }
                        catch (Exception ex) {}
                    }
                }

                new Handler(Looper.getMainLooper()).post(() -> callback.printFinished());
            }
        }.start();
    }
}
