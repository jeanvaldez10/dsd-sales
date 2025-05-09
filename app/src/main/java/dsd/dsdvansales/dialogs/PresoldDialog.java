package dsd.dsdvansales.dialogs;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.fragment.AdjustConsignmentInventorySummaryFragment;
import dsd.dsdvansales.fragment.DeliverySummaryFragment;
import dsd.dsdvansales.fragment.EndingInventorySummaryFragment;
import dsd.dsdvansales.fragment.InstoreFragment;
import dsd.dsdvansales.global.BixolonPrinter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.HoneywellLinePrinter;
import dsd.dsdvansales.global.HoneywellPrinter;
import dsd.dsdvansales.global.PrintEngine;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustomerOrderInfo;
import dsd.dsdvansales.model.OrderLineInfo;
import dsd.dsdvansales.model.PresoldOrDInfo;
import dsd.dsdvansales.model.PresoldOrHInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class PresoldDialog extends Dialog {


	private Font fontBlack10Bold;
	private Font fontBlack12Bold;
	private Font fontBlack9Bold	;
	private Font fontBlack9Normal;


	public PresoldOrHInfo mInfo = null;
	public CustomerOrderInfo mCustomerOrderInfo = null;
	public boolean mIsPickup = false;
	private String mStrPdfFileName = "";
	private String mStrMacAddress = "";
	private String mStrFriendlyName = "";
	private Connection printerConnection = null;

	private Context mContext;
	private InstoreFragment storeFragment;

	PrintEngine mHoneywellPrintEngine = null;
	String mHoneywellTemplatePath = null;

	public PresoldDialog(Context context, InstoreFragment fragment) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mContext = context;
		storeFragment = fragment;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.9f;
		getWindow().setAttributes(lpWindow);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.dialog_presold);

		((TextView) findViewById(R.id.txtTitle)).setText(Str.SalesNote[Global.LANG].toUpperCase());
		((Button) findViewById(R.id.btnLoadOrder)).setText(Str.SellOrder[Global.LANG]);
		((Button) findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);

		((TextView) findViewById(R.id.txtOrderNo)).setText(mInfo != null ? mInfo.strOrderNo : String.valueOf(mCustomerOrderInfo.orderNumber));
		if(mInfo != null)
		{
			((TextView) findViewById(R.id.txtPORef)).setText(mIsPickup == false ? mInfo.strPORef : mInfo.strCredRef);
		}
		else if(mCustomerOrderInfo != null)
		{
			((TextView) findViewById(R.id.txtPORef)).setText(mCustomerOrderInfo.purchaseOrderNumber);
		}
		else
		{
			((TextView) findViewById(R.id.txtPORef)).setText("");
		}

		((TextView) findViewById(R.id.txtInstr)).setText(mInfo != null ? mInfo.strInstrs : mCustomerOrderInfo.deliveryInstructions);

		if(storeFragment != null)
		{
			findViewById(R.id.btnLoadOrder).setOnClickListener(v -> {
				dismiss();
				if(mInfo != null)
				{
					storeFragment.gotoDelivery(mInfo);
				}
				else
				{
					storeFragment.gotoDelivery(mCustomerOrderInfo);
				}
			});
		}
		else
		{
			findViewById(R.id.btnLoadOrder).setVisibility(View.GONE);
		}


		findViewById(R.id.btnReturn).setOnClickListener(v -> dismiss());

		findViewById(R.id.btnSlip).setOnClickListener(arg0 -> doPickSlipPrint());

		loadFont();
	}

	private void loadFont()
	{
		fontBlack10Bold		= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 10, Font.BOLD, BaseColor.BLACK);
		fontBlack12Bold		= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 12, Font.BOLD, BaseColor.BLACK);
		fontBlack9Bold		= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 9, Font.BOLD, BaseColor.BLACK);
		fontBlack9Normal		= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 9, Font.NORMAL, BaseColor.BLACK);
	}

	public PdfPTable createHeader()
	{
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
			cell.setPaddingTop(10);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			phrase = new Phrase("PICK SLIP", fontBlack12Bold);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			long lTime = System.currentTimeMillis();
			String strDeliveryDate = new SimpleDateFormat(Utils.getDateFormat(mContext, Global.DATE_DD_MM_YYYY_FORMAT)).format(Utils.getRoutectlTripDate(mContext, new Date(lTime))) + " " + new SimpleDateFormat(Global.TIME_HH_MM_FORMAT).format(new Date(lTime));

			phrase = new Phrase("Date " + strDeliveryDate, fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setPadding(5);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);


			phrase = new Phrase("Customer: " + Global.gCurCustomerInfo.strName, fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			if(mInfo != null)
			{
				phrase = new Phrase("Order #: " + mInfo.strOrderNo, fontBlack9Bold);
			}
			else
			{
				phrase = new Phrase("Order #: " + mCustomerOrderInfo.orderNumber, fontBlack9Bold);
			}

			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return table;
	}

	public PdfPTable createBody()
	{
		PdfPTable table = new PdfPTable(3);
		PdfPCell cell;
		Phrase phrase;
		try {
			table.setWidths(new int[]{1, 2, 1});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			phrase = new Phrase("Item", fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setColspan(2);
			cell.setBorder(Rectangle.BOTTOM);
			table.addCell(cell);

			phrase = new Phrase("Qty", fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setColspan(1);
			cell.setBorder(Rectangle.BOTTOM);
			table.addCell(cell);

			int nTotal = 0;
			if(mInfo != null)
			{
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();

				List<PresoldOrDInfo> presoldOrDInfos = dbAdapter.getPresoldOrDListWithShortDesc(mInfo.strDetailFile);
				dbAdapter.close();
				boolean bFreeExist = false;
				for(PresoldOrDInfo info : presoldOrDInfos)
				{
					if(info.strTrxnType.equals("4"))
					{
						phrase = new Phrase(Utils.getDisplayItemCode(mContext, info.strItemNo), fontBlack9Normal);
						cell = new PdfPCell(phrase);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						cell.setVerticalAlignment(Element.ALIGN_CENTER);
						cell.setColspan(1);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);

						phrase = new Phrase(info.strShortDesc, fontBlack9Normal);
						cell = new PdfPCell(phrase);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						cell.setVerticalAlignment(Element.ALIGN_CENTER);
						cell.setColspan(1);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);

						phrase = new Phrase(String.valueOf(info.nOrderQty), fontBlack9Normal);
						cell = new PdfPCell(phrase);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						cell.setVerticalAlignment(Element.ALIGN_CENTER);
						cell.setColspan(1);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nTotal += info.nOrderQty;
					}
					else if(info.strTrxnType.equals("11") || info.strTrxnType.equals("12"))
					{
						bFreeExist = true;
					}
				}
				if(bFreeExist)
				{
					phrase = new Phrase("Freezer BDN", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					cell.setColspan(3);
					cell.setBorder(Rectangle.BOTTOM);
					table.addCell(cell);

					for(PresoldOrDInfo info : presoldOrDInfos)
					{
						if(info.strTrxnType.equals("11") || info.strTrxnType.equals("12"))
						{
							phrase = new Phrase(Utils.getDisplayItemCode(mContext, info.strItemNo), fontBlack9Normal);
							cell = new PdfPCell(phrase);
							cell.setHorizontalAlignment(Element.ALIGN_LEFT);
							cell.setVerticalAlignment(Element.ALIGN_CENTER);
							cell.setColspan(1);
							cell.setBorder(Rectangle.NO_BORDER);
							table.addCell(cell);

							phrase = new Phrase(info.strShortDesc, fontBlack9Normal);
							cell = new PdfPCell(phrase);
							cell.setHorizontalAlignment(Element.ALIGN_LEFT);
							cell.setVerticalAlignment(Element.ALIGN_CENTER);
							cell.setColspan(1);
							cell.setBorder(Rectangle.NO_BORDER);
							table.addCell(cell);

							phrase = new Phrase(String.valueOf(info.nOrderQty), fontBlack9Normal);
							cell = new PdfPCell(phrase);
							cell.setHorizontalAlignment(Element.ALIGN_LEFT);
							cell.setVerticalAlignment(Element.ALIGN_CENTER);
							cell.setColspan(1);
							cell.setBorder(Rectangle.NO_BORDER);
							table.addCell(cell);
							nTotal += info.nOrderQty;
						}
					}
				}
			}
			else
			{
				boolean bFreeExist = false;
				for(OrderLineInfo info : mCustomerOrderInfo.orderLines)
				{
					if(info.transactionType == 4)
					{
						phrase = new Phrase(info.itemNumber, fontBlack9Normal);
						cell = new PdfPCell(phrase);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						cell.setVerticalAlignment(Element.ALIGN_CENTER);
						cell.setColspan(1);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);

						phrase = new Phrase(info.strShortDesc, fontBlack9Normal);
						cell = new PdfPCell(phrase);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						cell.setVerticalAlignment(Element.ALIGN_CENTER);
						cell.setColspan(1);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);

						phrase = new Phrase(String.valueOf(info.quantity), fontBlack9Normal);
						cell = new PdfPCell(phrase);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						cell.setVerticalAlignment(Element.ALIGN_CENTER);
						cell.setColspan(1);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nTotal += info.quantity;
					}
					else if(info.transactionType == 11 || info.transactionType == 12)
					{
						bFreeExist = true;
					}
				}
				if(bFreeExist)
				{
					phrase = new Phrase("Freezer BDN", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					cell.setColspan(3);
					cell.setBorder(Rectangle.BOTTOM);
					table.addCell(cell);

					for(OrderLineInfo info : mCustomerOrderInfo.orderLines)
					{
						if(info.transactionType == 11 || info.transactionType == 12)
						{
							phrase = new Phrase(info.itemNumber, fontBlack9Normal);
							cell = new PdfPCell(phrase);
							cell.setHorizontalAlignment(Element.ALIGN_LEFT);
							cell.setVerticalAlignment(Element.ALIGN_CENTER);
							cell.setColspan(1);
							cell.setBorder(Rectangle.NO_BORDER);
							table.addCell(cell);

							phrase = new Phrase(info.strShortDesc, fontBlack9Normal);
							cell = new PdfPCell(phrase);
							cell.setHorizontalAlignment(Element.ALIGN_LEFT);
							cell.setVerticalAlignment(Element.ALIGN_CENTER);
							cell.setColspan(1);
							cell.setBorder(Rectangle.NO_BORDER);
							table.addCell(cell);

							phrase = new Phrase(String.valueOf(info.quantity), fontBlack9Normal);
							cell = new PdfPCell(phrase);
							cell.setHorizontalAlignment(Element.ALIGN_LEFT);
							cell.setVerticalAlignment(Element.ALIGN_CENTER);
							cell.setColspan(1);
							cell.setBorder(Rectangle.NO_BORDER);
							table.addCell(cell);
							nTotal += info.quantity;
						}
					}
				}
			}

			phrase = new Phrase(" ", fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setVerticalAlignment(Element.ALIGN_CENTER);
			cell.setColspan(1);
			cell.setBorder(Rectangle.TOP);
			table.addCell(cell);

			phrase = new Phrase("Total", fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setVerticalAlignment(Element.ALIGN_CENTER);
			cell.setColspan(1);
			cell.setBorder(Rectangle.TOP);
			table.addCell(cell);

			phrase = new Phrase(String.valueOf(nTotal), fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setVerticalAlignment(Element.ALIGN_CENTER);
			cell.setColspan(1);
			cell.setBorder(Rectangle.TOP);
			table.addCell(cell);

		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return table;
	}

	public void doPickSlipPrint()
	{
		showProgressDialog(Str.CreatingPdf[Global.LANG]);

		new Thread(){
			@Override
			public void run() {
				mStrPdfFileName = getOrderNo() + ".pdf";

				String strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_PICK_SLIP_TEMPLATE_FILENAME);
				if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + strTemplateFileName))
				{
					PrintEngine printEngine = new PrintEngine();
					mHoneywellPrintEngine = printEngine;

					long lTime = System.currentTimeMillis();
					String strDeliveryDate = new SimpleDateFormat(Utils.getDateFormat(mContext, Global.DATE_DD_MM_YYYY_FORMAT)).format(Utils.getRoutectlTripDate(mContext, new Date(lTime))) + " " + new SimpleDateFormat(Global.TIME_HH_MM_FORMAT).format(new Date(lTime));
					Utils.createDirectory(Global.PDF_LOCAL_DIR);
					if(mInfo != null)
					{
						Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();
						Map<String, ProdDetlInfo> mProdDetlUPCMap = new HashMap<>();
						DbAdapter dbAdapter = new DbAdapter(mContext);
						dbAdapter.open();
						dbAdapter.getProductList(mProdDetlItemMap, mProdDetlUPCMap);
						List<PresoldOrDInfo> presoldOrDInfos = dbAdapter.getPresoldOrDListWithShortDesc(mInfo.strDetailFile);

						Utils.gDetlMap.clear();
						for(PresoldOrDInfo presoldOrDInfo : presoldOrDInfos)
						{
							ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(presoldOrDInfo.strItemNo);
							if(prodDetlInfo != null)
							{
								Utils.gDetlMap.put(prodDetlInfo.strItemNo, Utils.getPropertyMap(prodDetlInfo));
							}
						}
						dbAdapter.close();

						printEngine.PreparePrint(strDeliveryDate, Global.gCurCustomerInfo.strName, mInfo.strOrderNo, presoldOrDInfos, mInfo.strPORef, mContext, mProdDetlItemMap);
					}
					else if(mCustomerOrderInfo != null)
					{
						DbAdapter dbAdapter = new DbAdapter(mContext);
						dbAdapter.open();
						Utils.gDetlMap.clear();
						if(mCustomerOrderInfo.orderLines != null)
						{
							for(OrderLineInfo orderLineInfo : mCustomerOrderInfo.orderLines)
							{
								ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(orderLineInfo.itemNumber);
								if(prodDetlInfo != null)
								{
									Utils.gDetlMap.put(prodDetlInfo.strItemNo, Utils.getPropertyMap(prodDetlInfo));
								}
							}
						}
						dbAdapter.close();
						printEngine.PreparePrint(strDeliveryDate, Global.gCurCustomerInfo.strName, String.valueOf(mCustomerOrderInfo.orderNumber), mCustomerOrderInfo.orderLines, mCustomerOrderInfo.purchaseOrderNumber);
					}

					String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_PICK_SLIP_TEMPLATE_FILENAME);
					mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;

					printEngine.createDocument(mContext, false, Global.PDF_LOCAL_DIR + mStrPdfFileName, Global.PICK_SLIP_PRINT);
				}
				else
				{
					createPDF(mStrPdfFileName);
				}

				new Handler(Looper.getMainLooper()).post(() -> {
					hideProgressDialog();
					if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
					{
						try
						{
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
							msgDialog.setOnDismissListener(dialog -> {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									outputPrinter();
								} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
							msgDialog.setOnDismissListener(dialog -> {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									outputHoneywellPrinter();
								} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
							msgDialog.setOnDismissListener(dialog -> {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									outputHoneywellLinePrinter();
								} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
							msgDialog.setOnDismissListener(dialog -> {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									outputBixolonPrinter();
								} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				});
			}
		}.start();
	}

	private void outputHoneywellPrinter() {
		if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false) {
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.TurnOnBluetooth_alert[Global.LANG];
				msgDialog.mStrLeft = Str.Cancel[Global.LANG];
				msgDialog.mStrRight = Str.Retry[Global.LANG];
				msgDialog.setOnDismissListener(dialog -> {
					if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
						outputHoneywellPrinter();
					} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

					}
				});
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			return;
		}
		mStrMacAddress = Utils.getStringSetting(mContext, Global.PREF_HONEYWELL_PRINTER_MAC_ADDRESS);

		if (mStrMacAddress.length() == 0) {
			discoveryHoneywellPrinters();
		} else {
			doHoneywellPrintPdfTask();
		}
	}

	private void outputHoneywellLinePrinter() {
		if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false) {
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.TurnOnBluetooth_alert[Global.LANG];
				msgDialog.mStrLeft = Str.Cancel[Global.LANG];
				msgDialog.mStrRight = Str.Retry[Global.LANG];
				msgDialog.setOnDismissListener(dialog -> {
					if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
						outputHoneywellLinePrinter();
					} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

					}
				});
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			return;
		}
		mStrMacAddress = Utils.getStringSetting(mContext, Global.PREF_HONEYWELL_LINE_PRINTER_MAC_ADDRESS);

		if (mStrMacAddress.length() == 0) {
			discoveryHoneywellLinePrinters();
		} else {
			doHoneywellLinePrintPdfTask();
		}
	}

	private void outputBixolonPrinter() {
		if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false) {
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.TurnOnBluetooth_alert[Global.LANG];
				msgDialog.mStrLeft = Str.Cancel[Global.LANG];
				msgDialog.mStrRight = Str.Retry[Global.LANG];
				msgDialog.setOnDismissListener(dialog -> {
					if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
						outputBixolonPrinter();
					} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

					}
				});
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			return;
		}
		mStrMacAddress = Utils.getStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_MAC_ADDRESS);
		mStrFriendlyName = Utils.getStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_FRIENDLY_NAME);

		if (mStrMacAddress.length() == 0 || mStrFriendlyName.length() == 0) {
			discoveryBixolonPrinters();
		} else {
			doBixolonPrintPdfTask();
		}
	}

	private void doHoneywellPrintPdfTask()
	{
		showProgressDialog(Str.Printing[Global.LANG]);
		HoneywellPrinter.doPrint(mContext, new HoneywellPrinter.PrintCallback() {
			@Override
			public void printFinished() {
				hideProgressDialog();
				try {
					Utils.setStringSetting(mContext, Global.PREF_HONEYWELL_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			@Override
			public void printError(String error) {
				hideProgressDialog();
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrTitle = "Printer Error";
					msgDialog.mStrMessage = error;
					msgDialog.mStrLeft = Str.No[Global.LANG];
					msgDialog.mStrRight = Str.Yes[Global.LANG];
					msgDialog.setOnDismissListener(dialog -> {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							doHoneywellPrintPdfTask();
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, Global.PDF_LOCAL_DIR + mStrPdfFileName);
	}

	private void doBixolonPrintPdfTask()
	{
		showProgressDialog(Str.Printing[Global.LANG]);
		BixolonPrinter.doPrint(mContext, new BixolonPrinter.PrintCallback() {
			@Override
			public void printFinished() {
				hideProgressDialog();
				try {
					Utils.setStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.setStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_FRIENDLY_NAME, mStrFriendlyName);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			@Override
			public void printError(String error) {
				hideProgressDialog();
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrTitle = "Printer Error";
					msgDialog.mStrMessage = error;
					msgDialog.mStrLeft = Str.No[Global.LANG];
					msgDialog.mStrRight = Str.Yes[Global.LANG];
					msgDialog.setOnDismissListener(dialog -> {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							doBixolonPrintPdfTask();
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, mStrFriendlyName, Global.PDF_LOCAL_DIR + mStrPdfFileName);
	}

	private void doHoneywellLinePrintPdfTask()
	{
		if(Utils.isFileExist(mHoneywellTemplatePath))
		{
			showProgressDialog(Str.Printing[Global.LANG]);
			HoneywellLinePrinter.doPrintDotMatrix(mContext, new HoneywellLinePrinter.PrintCallback() {
				@Override
				public void printFinished() {
					hideProgressDialog();
					try {
						Utils.setStringSetting(mContext, Global.PREF_HONEYWELL_LINE_PRINTER_MAC_ADDRESS, mStrMacAddress);
						Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
					}catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				@Override
				public void printError(String error) {
					hideProgressDialog();
					try {
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrTitle = "Printer Error";
						msgDialog.mStrMessage = error;
						msgDialog.mStrLeft = Str.No[Global.LANG];
						msgDialog.mStrRight = Str.Yes[Global.LANG];
						msgDialog.setOnDismissListener(dialog -> {
							if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
								doHoneywellLinePrintPdfTask();
							} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
							}
						});
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}, mStrMacAddress, mHoneywellTemplatePath, mHoneywellPrintEngine);
		}
		else {
			dismiss();
		}
	}

	private void discoveryHoneywellPrinters()
	{
		showProgressDialog(Str.DiscoveryingHoneywellPrinters[Global.LANG]);
		new Thread(() -> {
			Looper.prepare();
			try {
				final CountDownLatch latch = new CountDownLatch(1);
				final boolean[] result = {false};
				BluetoothDiscoverer.findPrinters(mContext, new DiscoveryHandler() {
					List<String> deviceList = new ArrayList<>();
					@Override
					public void foundPrinter(DiscoveredPrinter printer) {
						String strFriendlyName = "";
						if (printer instanceof DiscoveredPrinterBluetooth) {
							strFriendlyName = ((DiscoveredPrinterBluetooth) printer).friendlyName;
						}
						String strDevice = printer.address + "\n";
						if (strFriendlyName != null) {
							strDevice += strFriendlyName;
						} else {
							strDevice += "Unknown";
						}
						deviceList.add(strDevice);
					}

					@Override
					public void discoveryFinished() {
						hideProgressDialog();
						if (deviceList.size() > 0) {
							showDeviceList(deviceList);
							result[0] = true;
						}
						else
						{
							result[0] = false;
						}
						latch.countDown();
					}

					@Override
					public void discoveryError(String message) {
						Utils.printDebug(mContext, "Discovery error - " + message);
						result[0] = false;
						latch.countDown();
					}
				});

				latch.await();
				if(result[0] == false)
				{
					hideProgressDialog();
					try
					{
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrMessage = Str.FailedToPrint[Global.LANG];
						msgDialog.mStrMiddle = Str.Ok[Global.LANG];
						msgDialog.setOnDismissListener(dialog -> {
						});
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Looper.myLooper().quit();
			}
		}).start();
	}

	private void discoveryHoneywellLinePrinters()
	{
		showProgressDialog(Str.DiscoveryingHoneywellPrinters[Global.LANG]);
		new Thread(() -> {
			Looper.prepare();
			try {
				final CountDownLatch latch = new CountDownLatch(1);
				final boolean[] result = {false};
				BluetoothDiscoverer.findPrinters(mContext, new DiscoveryHandler() {
					List<String> deviceList = new ArrayList<>();
					@Override
					public void foundPrinter(DiscoveredPrinter printer) {
						String strFriendlyName = "";
						if (printer instanceof DiscoveredPrinterBluetooth) {
							strFriendlyName = ((DiscoveredPrinterBluetooth) printer).friendlyName;
						}
						String strDevice = printer.address + "\n";
						if (strFriendlyName != null) {
							strDevice += strFriendlyName;
						} else {
							strDevice += "Unknown";
						}
						deviceList.add(strDevice);
					}

					@Override
					public void discoveryFinished() {
						hideProgressDialog();
						if (deviceList.size() > 0) {
							showDeviceList(deviceList);
							result[0] = true;
						}
						else
						{
							result[0] = false;
						}
						latch.countDown();
					}

					@Override
					public void discoveryError(String message) {
						Utils.printDebug(mContext, "Discovery error - " + message);
						result[0] = false;
						latch.countDown();
					}
				});

				latch.await();
				if(result[0] == false)
				{
					hideProgressDialog();
					try
					{
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrMessage = Str.FailedToPrint[Global.LANG];
						msgDialog.mStrMiddle = Str.Ok[Global.LANG];
						msgDialog.setOnDismissListener(dialog -> {
						});
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Looper.myLooper().quit();
			}
		}).start();
	}
	private void discoveryBixolonPrinters()
	{
		showProgressDialog(Str.DiscoveryingBixolonPrinters[Global.LANG]);
		new Thread(() -> {
			Looper.prepare();
			try {
				final CountDownLatch latch = new CountDownLatch(1);
				final boolean[] result = {false};
				BluetoothDiscoverer.findPrinters(mContext, new DiscoveryHandler() {
					List<String> deviceList = new ArrayList<>();
					@Override
					public void foundPrinter(DiscoveredPrinter printer) {
						String strFriendlyName = "";
						if (printer instanceof DiscoveredPrinterBluetooth) {
							strFriendlyName = ((DiscoveredPrinterBluetooth) printer).friendlyName;
						}
						String strDevice = printer.address + "\n";
						if (strFriendlyName != null) {
							strDevice += strFriendlyName;
						} else {
							strDevice += "Unknown";
						}
						deviceList.add(strDevice);
					}

					@Override
					public void discoveryFinished() {
						hideProgressDialog();
						if (deviceList.size() > 0) {
							showDeviceList(deviceList);
							result[0] = true;
						}
						else
						{
							result[0] = false;
						}
						latch.countDown();
					}

					@Override
					public void discoveryError(String message) {
						Utils.printDebug(mContext, "Discovery error - " + message);
						result[0] = false;
						latch.countDown();
					}
				});

				latch.await();
				if(result[0] == false)
				{
					hideProgressDialog();
					try
					{
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrMessage = Str.FailedToPrint[Global.LANG];
						msgDialog.mStrMiddle = Str.Ok[Global.LANG];
						msgDialog.setOnDismissListener(dialog -> {
						});
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Looper.myLooper().quit();
			}
		}).start();
	}


	public void createPDF(String pdfFileName)
	{
		try
		{
			Utils.createDirectory(Global.PDF_LOCAL_DIR);

			PdfPTable table_header = createHeader();
			table_header.setLockedWidth(true);
			table_header.setTotalWidth(120f);
			float fHeight = table_header.getTotalHeight();

			PdfPTable table_body = createBody();
			table_body.setLockedWidth(true);
			table_body.setTotalWidth(120f);
			fHeight += table_body.getTotalWidth();
			fHeight += 40;

			Document document = new Document(new Rectangle(0, 0, 140, fHeight), 10, 10, 0, 0);
			PdfWriter.getInstance(document, new FileOutputStream(Global.PDF_LOCAL_DIR + pdfFileName));

			document.open();
			document.add(table_header);
			table_body.setSpacingBefore(5);
			document.add(table_body);
			document.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public String getOrderNo() {
		String strOrderNo = "";
		String strPattern = Utils.getStringSetting(mContext, Global.PREF_INVOICE_NUM_FORMAT);
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

	private void outputPrinter() {
		if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false) {
			try
			{
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.TurnOnBluetooth_alert[Global.LANG];
				msgDialog.mStrLeft = Str.Cancel[Global.LANG];
				msgDialog.mStrRight = Str.Retry[Global.LANG];
				msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							outputPrinter();
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
//						goBack();
						}
					}
				});
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}

			return;
		}
		mStrMacAddress = Utils.getStringSetting(mContext, Global.PREF_PRINTER_MAC_ADDRESS);
		if (mStrMacAddress.length() == 0) {
			discoveryPrinters();
		} else {
			new PrintPdfTask().execute();
		}
	}

	private void discoveryPrinters() {
		showProgressDialog(Str.DiscoveryingPrinters[Global.LANG]);
		new Thread(new Runnable() {
			public void run() {
				Looper.prepare();
				try {
					final CountDownLatch latch = new CountDownLatch(1);
					final boolean[] result = {false};
					BluetoothDiscoverer.findPrinters(mContext, new DiscoveryHandler() {
						List<String> deviceList = new ArrayList<>();
						@Override
						public void foundPrinter(DiscoveredPrinter printer) {
							String strFriendlyName = "";
							if (printer instanceof DiscoveredPrinterBluetooth) {
								strFriendlyName = ((DiscoveredPrinterBluetooth) printer).friendlyName;
							}
							String strDevice = printer.address + "\n";
							if (strFriendlyName != null) {
								strDevice += strFriendlyName;
							} else {
								strDevice += "Unknown";
							}
							deviceList.add(strDevice);
						}

						@Override
						public void discoveryFinished() {
							hideProgressDialog();
							if (deviceList.size() > 0) {
								showDeviceList(deviceList);
								result[0] = true;
							}
							else
							{
								result[0] = false;

							}
							latch.countDown();
						}

						@Override
						public void discoveryError(String message) {
							Utils.printDebug(mContext, "Discovery error - " + message);
							result[0] = false;
							latch.countDown();
						}
					});
//					latch.await(Global.DISCOVER_BLUETOOTH_TIMEOUT, TimeUnit.SECONDS);
					latch.await();
					if(result[0] == false)
					{
						hideProgressDialog();
						try
						{
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.FailedToPrint[Global.LANG] + "\n" + Str.DiscoveryPrinterAgain[Global.LANG];
							msgDialog.mStrMiddle = Str.Ok[Global.LANG];
							msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}

					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Looper.myLooper().quit();
				}
			}
		}).start();
	}


	private void showDeviceList(List<String> deviceList) {
		if(deviceList.size() == 0)
		{
			try
			{
				Toast.makeText(mContext, Str.NoDeviceDiscover[Global.LANG], Toast.LENGTH_LONG).show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			return;
		}
		final String strDevices[] = new String[deviceList.size()];
		for (int i=0; i<strDevices.length; i++) {
			strDevices[i] = deviceList.get(i);
		}
		String strArray[] = strDevices[0].split("\n");
		if (strArray.length > 0) {
			mStrMacAddress = strArray[0];
		}
		if (strArray.length > 1) {
			mStrFriendlyName = strArray[1];
		}

		try
		{
			new AlertDialog.Builder(mContext)
				.setTitle(Str.SelectPrinter[Global.LANG])
				.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strDevices), 0, (dialogInterface, i) -> {
					String strArray1[] = strDevices[i].split("\n");
					if (strArray1.length > 0) {
						mStrMacAddress = strArray1[0];
					}
					if (strArray1.length > 1) {
						mStrFriendlyName = strArray1[1];
					}
				})
					.setPositiveButton(Str.Choose[Global.LANG], (dialogInterface, which) -> {
						if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
						{
							new PrintPdfTask().execute();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
						{
							doHoneywellPrintPdfTask();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
						{
							doHoneywellLinePrintPdfTask();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
						{
							doBixolonPrintPdfTask();
						}
					})
					.setNegativeButton(Str.Research[Global.LANG], (dialogInterface, i) -> {
						if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
						{
							discoveryPrinters();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
						{
							discoveryHoneywellPrinters();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
						{
							discoveryHoneywellLinePrinters();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
						{
							discoveryBixolonPrinters();
						}
					})
				.setCancelable(false)
				.show();

		}catch (Exception e)
		{
			e.printStackTrace();
		}


	}

	private class PrintPdfTask extends AsyncTask<Void, String, String> {

		int nMessageType = 0;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			try
			{
				showProgressDialog(Str.Printing[Global.LANG]);
			}catch (Exception e)
			{
				e.printStackTrace();
			}

		}

		@Override
		protected String doInBackground(Void... params) {
			String strResult = "Unknown Error";
			printerConnection = new BluetoothConnection(mStrMacAddress);
			try {
				printerConnection.open();
				ZebraPrinter printer = null;
				if (printerConnection.isConnected()) {
					printer = ZebraPrinterFactory.getInstance(printerConnection);


					if (printer != null) {
						PrinterStatus printerStatus = printer.getCurrentStatus();
						if(printerStatus.isReadyToPrint)
						{
							printer.sendFileContents(Global.PDF_LOCAL_DIR + mStrPdfFileName);
							strResult = "";
						}
						else if(printerStatus.isPaused)
						{
							strResult = Str.PrinterPaused[Global.LANG];
							nMessageType = 1;
						}
						else if(printerStatus.isHeadOpen)
						{
							strResult = Str.PrinterHeadOpen[Global.LANG];
							nMessageType = 1;
						}
						else if(printerStatus.isPaperOut)
						{
							strResult = Str.PrinterPaperOut[Global.LANG];
							nMessageType = 1;
						}
						else if(printerStatus.isRibbonOut)
						{
							strResult = Str.PrinterRibbonOut[Global.LANG];
							nMessageType = 1;
						}
						else if(printerStatus.isHeadCold)
						{
							strResult = Str.PrinterHeadCold[Global.LANG];
							nMessageType = 1;
						}
						else if(printerStatus.isHeadTooHot)
						{
							strResult = Str.PrinterHeadTooHot[Global.LANG];
							nMessageType = 1;
						}
						else if(printerStatus.isReceiveBufferFull)
						{
							strResult = Str.PrinterReceiverBufferFull[Global.LANG];
							nMessageType = 1;
						}
						else
						{
							strResult = Str.PrinterError[Global.LANG];
							nMessageType = 2;
						}

						printerConnection.close();
					}
				}
			} catch (ConnectionException e) {
				Utils.addPrintExceptionStackToFileIfNeeded(mContext, Global.ZEBRA_PRINT_ERROR_FILENAME, e);
				e.printStackTrace();
				strResult = e.getMessage();
				nMessageType = 2;
			} catch (ZebraPrinterLanguageUnknownException e) {
				Utils.addPrintExceptionStackToFileIfNeeded(mContext, Global.ZEBRA_PRINT_ERROR_FILENAME, e);
				e.printStackTrace();
				strResult = e.getMessage();
				nMessageType = 2;
			} catch(Exception e){
				Utils.addPrintExceptionStackToFileIfNeeded(mContext, Global.ZEBRA_PRINT_ERROR_FILENAME, e);
				e.printStackTrace();
				strResult = e.getMessage();
				nMessageType = 2;
			}
			return strResult;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try
			{
				hideProgressDialog();
				if (result.length() == 0) {
					Utils.setStringSetting(mContext, Global.PREF_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
	/*				Global.gUOrderHInfo.nPrintedFlag = 1;
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					dbAdapter.updateUOrderHInfo(Global.gUOrderHInfo);
					dbAdapter.close();*/
//					goBack();
				} else {
					try {
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						if(nMessageType == 1)
						{
							msgDialog.mStrMessage = result;
							msgDialog.mStrLeft = Str.Cancel[Global.LANG];
							msgDialog.mStrRight = Str.Retry[Global.LANG];
						}
						else
						{
							msgDialog.mStrMessage = Str.Cannot_connect_printer[Global.LANG];
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
						}
						msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									if(nMessageType == 2)
									{
										discoveryPrinters();
									}
									else
									{
										new PrintPdfTask().execute();
									}
								} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
//								goBack();
								}
							}
						});
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}

				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}

		}
	}

	private ProgressDialog mProgressDialog = null;
	public void showProgressDialog(String strText)
	{
		try
		{
			if(mProgressDialog == null)
			{
				mProgressDialog = new ProgressDialog(mContext);
				mProgressDialog.setCancelable(false);
				mProgressDialog.setMessage(strText);
			}
			mProgressDialog.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public void hideProgressDialog()
	{
		try
		{
			if(mProgressDialog != null)
			{
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}
}
