package dsd.dsdvansales.global;

//Honeywell SDK imports
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dsd.dsdvansales.dialogs.DiscoveryBluetoothDeviceDialog;
import honeywell.connection.ConnectionBase;
import honeywell.connection.Connection_Bluetooth;
import honeywell.connection.Connection_TCP;
import honeywell.printer.DocumentDPL;
import honeywell.printer.DocumentDPL.*;
import honeywell.printer.DocumentEZ;
import honeywell.printer.DocumentLP;
import honeywell.printer.DocumentExPCL_LP;
import honeywell.printer.DocumentExPCL_PP;
import honeywell.printer.DocumentExPCL_PP.*;
import honeywell.printer.MuPDFCore;
import honeywell.printer.ParametersDPL;
import honeywell.printer.ParametersDPL.*;
import honeywell.printer.ParametersEZ;
import honeywell.printer.ParametersExPCL_LP;
import honeywell.printer.ParametersExPCL_LP.*;
import honeywell.printer.ParametersExPCL_PP;
import honeywell.printer.ParametersExPCL_PP.*;
import honeywell.printer.UPSMessage;
import honeywell.printer.configuration.dpl.*;
import honeywell.printer.configuration.ez.*;
import honeywell.printer.configuration.expcl.*;


public class HoneywellPrinter {

    private static BluetoothAdapter mBleAdapter = null;
    private static DiscoveryCallback mDiscoveryCallback = null;
    private static Context mContext = null;

    private static List<BluetoothDevice> foundDeviceStack = new ArrayList<>();
    private static boolean discovering = false;
    private static final BroadcastReceiver mDiscoveryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                discovering = true;
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                discovering = false;
                if(foundDeviceStack.isEmpty())
                {
                    mContext.unregisterReceiver(mDiscoveryReceiver);
                    mDiscoveryCallback.discoveryFinished();
                    mBleAdapter = null;
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                foundDeviceStack.add(device);
                //Try to connect to the bluetooth device to confirm that is honeywell device.
                new Thread()
                {
                    @Override
                    public void run() {
                        if(checkDevice(device))
                        {
                            new Handler(mContext.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    mDiscoveryCallback.foundPrinter(device);
                                }
                            });
                        }
                        foundDeviceStack.remove(device);
                        if(foundDeviceStack.size() == 0 && !discovering)
                        {
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mContext.unregisterReceiver(mDiscoveryReceiver);
                                    mDiscoveryCallback.discoveryFinished();
                                    mBleAdapter = null;
                                }
                            }, 100);
                        }
                    }
                }.start();
            }
        }
    };

    private static boolean checkDevice(BluetoothDevice device)
    {
        ConnectionBase conn = null;
        try {
            conn = Connection_Bluetooth.createClient(device.getAddress());
            conn.open();
            String strPrinterType = Utils.getStringSetting(mContext, Global.PREF_HONEYWELL_PRINTER);
            if(strPrinterType.equals("DPL"))
            {
                PrinterInformation_DPL printerInfo = new PrinterInformation_DPL(conn);
                printerInfo.queryPrinter(1000);

                if(printerInfo.getValid() == false)
                {
                    conn.close();
                    return false;
                }
                else {
                    conn.close();
                    return true;
                }
            }
            else if(strPrinterType.equals("Legacy"))
            {
                GeneralStatus generalStatus = new GeneralStatus(conn);
                generalStatus.queryPrinter(1000);
                if(generalStatus.getValid() == false)
                {
                    conn.close();
                    return false;
                }
                else {
                    conn.close();
                    return true;
                }
            }
            else if(strPrinterType.equals("Apex"))
            {
                GeneralStatus_ExPCL generalStatus = new GeneralStatus_ExPCL(conn);
                generalStatus.queryPrinter(1000);
                if(generalStatus.getValid() == false)
                {
                    conn.close();
                    return false;
                }
                else {
                    conn.close();
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public interface DiscoveryCallback {
        void foundPrinter (BluetoothDevice device);
        void discoveryFinished();
        void discoveryError(String message);
    }

    public interface PrintCallback {
        void printFinished();
        void printError(String error);
    }

    public static void cancelDiscoveryPrinters()
    {
        if(mBleAdapter != null)
        {
            mBleAdapter.cancelDiscovery();
        }
    }

    public static void discoveryPrinters(Context context, DiscoveryCallback callback)
    {
        mDiscoveryCallback = callback;
        mContext = context;

        mBleAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(mDiscoveryReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            context.registerReceiver(mDiscoveryReceiver, filter);
        }

        mBleAdapter.startDiscovery();
    }

    private static int m_printHeadWidth = 1000;
    private  static int getPdfWidth(String strPath)
    {
        if (Build.VERSION.SDK_INT >= 21) {
            File file = new File(strPath);
            try
            {
                ParcelFileDescriptor fileDesc = ParcelFileDescriptor.open(file, 268435456);
                PdfRenderer renderer = new PdfRenderer(fileDesc);
                int numPgs = renderer.getPageCount();
                if(numPgs > 0)
                {
                    PdfRenderer.Page page = renderer.openPage(0);
                    int width = page.getWidth();
                    return (int)(width * 3.5);
                }
                else {
                    return 0;
                }
            }catch (Exception e)
            {
                e.printStackTrace();
                return 0;
            }

        }
        else {
            try
            {
                MuPDFCore pdfCore = new MuPDFCore(strPath);
                int numPgs = pdfCore.countPages();
                if(numPgs > 0)
                {
                    float width = pdfCore.getPageSize(0).x;
                    return ((int)(width* 3.5));
                }
                else {
                    return 0;
                }

            }catch (Exception e)
            {
                e.printStackTrace();
                return 0;
            }
        }
    }

    public static void doPrint(Context context, PrintCallback callback, String strDevAddress, String strPdfPath)
    {
        new Thread()
        {
            @Override
            public void run() {
                Connection_Bluetooth conn = null;
                try
                {
                    m_printHeadWidth = getPdfWidth(strPdfPath);
                    if(m_printHeadWidth == 0)
                    {
                        new Handler(Looper.getMainLooper()).post(() -> callback.printError("PDF File is not correct." + " --- " + strPdfPath));
                        return;
                    }

                    conn = Connection_Bluetooth.createClient(strDevAddress, false);

                    boolean isOpened = false;
                    int retryLimit = 3;
                    while (retryLimit >= 0) {
                        try {
                            isOpened = conn.open();
                            if (isOpened) {
                                break;
                            }
                            else {
                                retryLimit -= 1;
                                Thread.sleep(1000);
                            }
                        }
                        catch (IOException e) {
                            Utils.addPrintExceptionStackToFileIfNeeded(context, Global.HONEYWELL_PRINT_ERROR_FILENAME, e);
                            retryLimit -= 1;
                            try {
                                Thread.sleep(1000); // Adding a small delay before retrying
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt(); // Restore the interrupted status
                            }
                        }
                    }

                    if (!isOpened) {
                        new Handler(Looper.getMainLooper()).post(() -> callback.printError("Cannot currently connect to your printer"));
                        conn.close();
                        return;
                    }

                    String strPrinterType = Utils.getStringSetting(context, Global.PREF_HONEYWELL_PRINTER);
                    if(strPrinterType.equals("DPL"))
                    {
                        DocumentDPL document = new DocumentDPL();
                        document.writePDF(strPdfPath, m_printHeadWidth, 0, 0);
                        conn.write(document.getDocumentData());
                    }
                    else if(strPrinterType.equals("Legacy"))
                    {
                        DocumentLP document = new DocumentLP("!");
                        document.writePDF(strPdfPath, m_printHeadWidth);
                        conn.write(document.getDocumentData());
                    }
                    else if(strPrinterType.equals("Apex"))
                    {
                        DocumentExPCL_LP document = new DocumentExPCL_LP(3);
                        document.writePDF(strPdfPath, m_printHeadWidth);
                        conn.write(document.getDocumentData());
                    }
                    conn.close();

                }
                catch (Exception e) {
                    if (conn != null) {
                        conn.close();
                    }
                    new Handler(Looper.getMainLooper()).post(() -> callback.printError(e.getMessage()));
                    e.printStackTrace();
                }

                new Handler(Looper.getMainLooper()).post(() -> callback.printFinished());
            }
        }.start();
    }
}
