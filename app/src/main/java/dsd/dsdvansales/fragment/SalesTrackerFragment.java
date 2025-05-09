package dsd.dsdvansales.fragment;

import android.content.*;
import android.os.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Locale;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;

public class SalesTrackerFragment extends BaseFragment implements View.OnClickListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_sales_tracker, container, false);
		
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_SALES_TRACKER;
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);

		loadData();
		refreshLanguage();
	}

	@Override
	public void loadData() {
		refreshPage();
	}

	@Override
	public void refreshPage() {
		((TextView)mRootView.findViewById(R.id.txtDriverName)).setText(Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION));
		SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy");
        String newFormat = formatter.format(System.currentTimeMillis());
        ((TextView)mRootView.findViewById(R.id.txtDeliveryDate)).setText(newFormat);

		double salesToday = Utils.parseDoubleFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Utils.getFloatSetting(mContext, Global.SALE_TODAY)));

		((TextView)mRootView.findViewById(R.id.txtSalesMTD)).setText(NumberFormat.getInstance().format(Utils.getDoubleSetting(mContext, Global.PREF_SALES_MTD) + salesToday));
		((TextView)mRootView.findViewById(R.id.txtMonthTarget)).setText(NumberFormat.getInstance().format(Utils.getDoubleSetting(mContext, Global.PREF_MONTH_TARGET)));
		((TextView)mRootView.findViewById(R.id.txtRemaining)).setText(NumberFormat.getInstance().format(Utils.getDoubleSetting(mContext, Global.PREF_MONTH_TARGET) - Utils.getDoubleSetting(mContext, Global.PREF_SALES_MTD) - salesToday));
		((TextView)mRootView.findViewById(R.id.txtPercentage)).setText(NumberFormat.getPercentInstance().format((Utils.getDoubleSetting(mContext, Global.PREF_SALES_MTD) + salesToday) / Utils.getDoubleSetting(mContext, Global.PREF_MONTH_TARGET)));

		((TextView)mRootView.findViewById(R.id.txtSalesToday)).setText(getFormattedPrice(Utils.getFloatSetting(mContext, Global.SALE_TODAY)));
		((TextView)mRootView.findViewById(R.id.txtSalesThisWeek)).setText(NumberFormat.getInstance().format(Utils.getDoubleSetting(mContext, Global.PREF_SALES_WTD) + salesToday));
		((TextView)mRootView.findViewById(R.id.txtSalesLastWeek)).setText(NumberFormat.getInstance().format(Utils.getDoubleSetting(mContext, Global.PREF_SALES_LWTD)));
		((TextView)mRootView.findViewById(R.id.txtThisweekvsLastweek)).setText(NumberFormat.getPercentInstance().format((Utils.getDoubleSetting(mContext, Global.PREF_SALES_WTD) + salesToday) / Utils.getDoubleSetting(mContext, Global.PREF_SALES_LWTD)));

		((TextView)mRootView.findViewById(R.id.txtSalesQuantityToday)).setText(NumberFormat.getInstance().format(Utils.getIntSetting(mContext, Global.TOTAL_SALES_COUNT)));
		((TextView)mRootView.findViewById(R.id.txtReturnsQuantityToday)).setText(NumberFormat.getInstance().format(Utils.getIntSetting(mContext, Global.TOTAL_RETURNS_COUNT)));
		((TextView)mRootView.findViewById(R.id.txtCollectionsToday)).setText(getFormattedPrice(Utils.getFloatSetting(mContext, Global.COLLECTIONS_TODAY)));
		((TextView)mRootView.findViewById(R.id.txtReturnsToday)).setText(getFormattedPrice(Utils.getFloatSetting(mContext, Global.RETURNS_TODAY)));
	}

	private String getFormattedPrice(float nPrice) {
		String strResult = String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Math.abs(nPrice));
		return strResult;
	}

	@Override
	public void refreshDriverName() {
		((TextView)mRootView.findViewById(R.id.txtDriverName)).setText(Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION));
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblSalesTracker)).setText(Str.Sales_Tracker[Global.LANG].toUpperCase());
		((Button) mRootView.findViewById(R.id.btnBack)).setText(Str.Return[Global.LANG]);

		((TextView) mRootView.findViewById(R.id.lblMonthPerformance)).setText(Str.MonthPerformance[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblSalesMTD)).setText(Str.SalesMTD[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblMonthTarget)).setText(Str.MonthTarget[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblRemaining)).setText(Str.Remaining[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblPercentage)).setText(Str.Percentage[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblWeekPerformance)).setText(Str.WeekPerformance[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblSalesToday)).setText(Str.SalesToday[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblSalesThisWeek)).setText(Str.SalesThisWeek[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblSalesLastWeek)).setText(Str.SalesLastWeek[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblThisweekvslastweek)).setText(Str.ThisWeekVSLastWeek[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblSalesQuantityToday)).setText(Str.TotalSalesQuantityToday[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblTotalPaymentsToday)).setText(Str.TotalPaymentsToday[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblReturnsToday)).setText(Str.ReturnsToday[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblReturnQuantityToday)).setText(Str.TotalReturnsQuantityToday[Global.LANG]);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		int id = arg0.getId();
		switch(id)
		{
		case R.id.btnBack:
			mContext.onBackPressed();
			break;
		}
	}
}