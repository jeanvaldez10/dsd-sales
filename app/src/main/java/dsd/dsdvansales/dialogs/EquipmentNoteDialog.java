package dsd.dsdvansales.dialogs;

import static androidx.core.content.ContextCompat.registerReceiver;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.SignatureActivity;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.PrintEngine;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.EquipmentWithAssInfo;

public class EquipmentNoteDialog extends Dialog {

	Map<String, String> mapEquipmentNotes = new HashMap<>();
	EquipmentWithAssInfo mEquipmentInfo;

	public String strDocPath = "";
	Activity mContext;
	public EquipmentNoteDialog(Activity context, EquipmentWithAssInfo info) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mContext = context;
		mEquipmentInfo = info;
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

		setContentView(R.layout.dialog_equipment_note);

		initView();

		findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());

		findViewById(R.id.btnCompleted).setOnClickListener(view -> doCompleted());
	}

	private ProgressDialog progressDialog = null;
	public void showProgressDialog(String strText)
	{
		try
		{
			if(progressDialog == null)
			{
				progressDialog = new ProgressDialog(mContext);
				progressDialog.setCancelable(false);
				progressDialog.show();
			}
			progressDialog.setMessage(strText);
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public void hideProgressDialog()
	{
		try
		{
			if(progressDialog != null)
			{
				progressDialog.dismiss();
				progressDialog = null;
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void doCompleted()
	{
		showProgressDialog(Str.CreatingPdf[Global.LANG]);
		new Thread()
		{
			@Override
			public void run() {
				super.run();
				PrintEngine printEngine = new PrintEngine();
				printEngine.prepareNewEquipDoc(mContext, mapEquipmentNotes);
				printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mEquipmentInfo.strEquipmentNo + ".pdf", Global.PRINT_EQUIPMENT_DOC);
				new Handler(Looper.getMainLooper()).post(() -> {
					hideProgressDialog();
					mEquipmentInfo.strEquipmentDoc = Global.PDF_DIR + mEquipmentInfo.strEquipmentNo + ".pdf";
					dismiss();
				});
			}
		}.start();

	}

	private LinearLayout getCellNode(Node node)
	{
		LinearLayout layout = new LinearLayout(mContext);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, // Width of LinearLayout matches parent
				LinearLayout.LayoutParams.WRAP_CONTENT  // Height of LinearLayout wraps content
		);

		String marginLeftStr = "0";
		try{
			Node marginNode = node.getAttributes().getNamedItem("marginLeft");
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
			Node marginNode = node.getAttributes().getNamedItem("marginRight");
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
			Node marginNode = node.getAttributes().getNamedItem("marginTop");
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
			Node marginNode = node.getAttributes().getNamedItem("marginBottom");
			if(marginNode != null)
			{
				marginBottomStr = marginNode.getNodeValue();
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}

		String paddingStr = "0";
		try{
			Node marginNode = node.getAttributes().getNamedItem("padding");
			if(marginNode != null)
			{
				paddingStr = marginNode.getNodeValue();
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}


		float marginLeft = Float.parseFloat(marginLeftStr);
		float marginRight = Float.parseFloat(marginRightStr);
		float marginTop = Float.parseFloat(marginTopStr);
		float marginBottom = Float.parseFloat(marginBottomStr);
		float padding = Float.parseFloat(paddingStr);

		layoutParams.setMargins((int)marginLeft, (int)marginTop, (int)marginRight, (int)marginBottom);

		layout.setLayoutParams(layoutParams);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setPadding((int)padding, (int)padding, (int)padding, (int)padding);

		NodeList childrenNodes = node.getChildNodes();
		int nChildLength = childrenNodes.getLength();
		for(int j = 0; j < nChildLength; j++) {
			Node childNode = childrenNodes.item(j);
			if( childNode.getNodeType() != Element.ELEMENT_NODE) continue;
			if( childNode.getNodeName().equalsIgnoreCase("Phrase"))
			{
				layout.addView(getPhrase(childNode));
			}
			if( childNode.getNodeName().equalsIgnoreCase("Input"))
			{
				layout.addView(getInput(childNode));
			}
			if( childNode.getNodeName().equalsIgnoreCase("Signature"))
			{
				layout.addView(getSignature(childNode));
			}
		}

		return layout;
	}

	private Button getSignature(Node node)
	{
		Button button = new Button(mContext);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, // Width of TextView
				LinearLayout.LayoutParams.WRAP_CONTENT  // Height of TextView
		);

		button.setLayoutParams(layoutParams);
		button.setPadding((int)Utils.convertDpToPixel(10, mContext), (int)Utils.convertDpToPixel(10, mContext), (int)Utils.convertDpToPixel(10, mContext), (int)Utils.convertDpToPixel(10, mContext));
		button.setGravity(Gravity.CENTER);
		button.setText(Str.Signature[Global.LANG]);
		button.setBackgroundResource(R.drawable.btn_white_large);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			button.setTextAppearance(R.style.Font20NormalBlack);
		} else {
			button.setTextAppearance(mContext, R.style.Font20NormalBlack);
		}

		String strName = "";
		try{
			Node nameNode = node.getAttributes().getNamedItem("name");
			if(nameNode != null)
			{
				strName = nameNode.getNodeValue();
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}

		String finalStrName = strName;
		button.setOnClickListener(view -> {
			registerReceiver(mContext, new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mContext.unregisterReceiver(this);
					if(intent.getAction().equals(Global.ACTION_CAPTURE_SIGNATURE))
					{
						String strPath = intent.getStringExtra(Global.EXTRA_SIGNATURE_PATH);
						mapEquipmentNotes.put(finalStrName.trim(), strPath);
					}
				}
			}, new IntentFilter(Global.ACTION_CAPTURE_SIGNATURE), ContextCompat.RECEIVER_EXPORTED);

			Intent intent = new Intent(mContext, SignatureActivity.class);
			intent.putExtra(Global.EXTRA_SIGNATURE_PATH, mEquipmentInfo.strEquipmentNo + "_Sig.jpg");
			mContext.startActivity(intent);
			mContext.overridePendingTransition(R.anim.zoom_in, 0);
		});
		return button;
	}

	private EditText getInput(Node node)
	{
		EditText editText = new EditText(mContext);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, // Width of TextView
				LinearLayout.LayoutParams.WRAP_CONTENT  // Height of TextView
		);

		editText.setLayoutParams(layoutParams);
		editText.setPadding((int)Utils.convertDpToPixel(10, mContext), (int)Utils.convertDpToPixel(10, mContext), (int)Utils.convertDpToPixel(10, mContext), (int)Utils.convertDpToPixel(10, mContext));
		editText.setGravity(Gravity.CENTER_VERTICAL);

		String strName = "";
		try{
			Node nameNode = node.getAttributes().getNamedItem("name");
			if(nameNode != null)
			{
				strName = nameNode.getNodeValue();
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}

		String finalStrName = strName;
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				mapEquipmentNotes.put(finalStrName.trim(), editable.toString());
			}
		});
		return editText;
	}

	private TextView getPhrase(Node node)
	{
		TextView textView = new TextView(mContext);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, // Width of TextView
				LinearLayout.LayoutParams.WRAP_CONTENT  // Height of TextView
		);

		String sizeStr = "0";
		try{
			Node marginNode = node.getAttributes().getNamedItem("size");
			if(marginNode != null)
			{
				sizeStr = marginNode.getNodeValue();
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}

		String typeStr = "";
		try{
			Node marginNode = node.getAttributes().getNamedItem("type");
			if(marginNode != null)
			{
				typeStr = marginNode.getNodeValue();
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}

		String colorStr = "";
		try{
			Node marginNode = node.getAttributes().getNamedItem("color");
			if(marginNode != null)
			{
				colorStr = marginNode.getNodeValue();
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}



		textView.setLayoutParams(layoutParams);
		textView.setGravity(Gravity.CENTER_VERTICAL);
		if(typeStr.equalsIgnoreCase("bold"))
		{
			textView.setTypeface(null, android.graphics.Typeface.BOLD);
		}
		if(sizeStr.length() > 0)
		{
			textView.setTextSize(Float.parseFloat(sizeStr));
		}
		if(colorStr.length() > 0)
		{
			if(colorStr.equals("yellow"))
			{
				textView.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
			}
			else if(colorStr.equals("black"))
			{
				textView.setTextColor(Color.BLACK);
			}
			else if(colorStr.equals("gray"))
			{
				textView.setTextColor(Color.GRAY);
			}
			else
			{
				textView.setTextColor(Color.BLACK);
			}
		}
		else {
			textView.setTextColor(Color.BLACK);
		}
		String value = "";
		NodeList nodeList = node.getChildNodes();
		int nLength = nodeList.getLength();
		for(int i = 0; i < nLength; i++)
		{
			Node childNode = nodeList.item(i);
			if( childNode.getNodeType() == Element.TEXT_NODE )
			{
				value += childNode.getNodeValue().trim();
			}
			else if(childNode.getNodeType() == Element.ELEMENT_NODE && childNode.getNodeName().equals("Object") )
			{
				String obj = getObject(childNode);
				value += obj;
			}
		}
		textView.setText(value.trim());
		return textView;
	}

	private String getObject(Node node)
	{
		String strName = "";
		try{
			Node nameNode = node.getAttributes().getNamedItem("name");
			if(nameNode != null)
			{
				strName = nameNode.getNodeValue();
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}

		if(strName.equals("CustomerName"))
		{
			return Global.gCurCustomerInfo.strName;
		}
		else {
			return "";
		}
	}
	private LinearLayout getTableNode(Node node)
	{
		LinearLayout layout = new LinearLayout(mContext);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, // Width of LinearLayout matches parent
				LinearLayout.LayoutParams.WRAP_CONTENT  // Height of LinearLayout wraps content
		);
		layout.setLayoutParams(layoutParams);
		layout.setOrientation(LinearLayout.VERTICAL);

		NodeList childrenNodes = node.getChildNodes();
		int nChildLength = childrenNodes.getLength();
		for(int j = 0; j < nChildLength; j++) {
			Node childNode = childrenNodes.item(j);
			if( childNode.getNodeType() != Element.ELEMENT_NODE) continue;
			if( childNode.getNodeName().equalsIgnoreCase("Cell"))
			{
				layout.addView(getCellNode(childNode));
			}
		}

		return layout;
	}
	private void initView()
	{
		LinearLayout mainLayout = findViewById(R.id.layoutContent);
		try
		{
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			org.w3c.dom.Document document = null;
			document = builder.parse(new File(Global.ROOT_LOCATION +  Global.PRINT_A4_NEW_EQUIPMENT_DOC_FMT_FILENAME));

			NodeList elements = document.getElementsByTagName("Document");
			Node RootElement = elements.item(0);

			NodeList nodes = RootElement.getChildNodes();
			int nLength = nodes.getLength();
			for(int i = 0; i < nLength; i++)
			{
				Node node = nodes.item(i);
				if( node.getNodeType() != Element.ELEMENT_NODE) continue;
				if( node.getNodeName().equalsIgnoreCase("pdf")) continue;
				if( node.getNodeName().equalsIgnoreCase("mobile"))
				{
					NodeList childrenNodes = node.getChildNodes();
					int nChildLength = childrenNodes.getLength();
					for(int j = 0; j < nChildLength; j++) {
						Node childNode = childrenNodes.item(j);
						if( childNode.getNodeType() != Element.ELEMENT_NODE) continue;
						if( childNode.getNodeName().equalsIgnoreCase("Table"))
						{
							LinearLayout layout = getTableNode(childNode);
							mainLayout.addView(layout);
						}
					}
				}
				if( node.getNodeName().equalsIgnoreCase("Table"))
				{
					LinearLayout layout = getTableNode(node);
					mainLayout.addView(layout);
				}
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

