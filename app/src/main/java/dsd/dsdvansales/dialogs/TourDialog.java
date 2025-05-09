package dsd.dsdvansales.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.global.DownloadInstance;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.HttpsTrustManager;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.model.TourDetail;
import dsd.dsdvansales.views.GalleryNavigator;


public class TourDialog extends Dialog {

	MainActivity mContext;
	GalleryNavigator navigator;
	ViewPager mViewPager;
	TourAdapter mPagerAdapter;
	List<TourDetail> tourList = null;
	Map<Integer, Integer> mDownloadStatus = new HashMap<>();
	Map<Integer, View> mItemView = new HashMap<>();

	public interface TourCallback{
		void onSkip();
		void onClose();
		void onCheck(boolean b);
	}
	TourCallback callback;

	public TourDialog(MainActivity context, List<TourDetail> tourList, TourCallback callback) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
		this.tourList = tourList;
		this.callback = callback;
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

		setContentView(R.layout.dialog_tour);

		mPagerAdapter = new TourAdapter();
		mViewPager = (ViewPager)findViewById(R.id.viewPager);

		navigator = (GalleryNavigator)findViewById(R.id.navigator);
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int i, float v, int i1) {

			}

			@Override
			public void onPageSelected(int i) {
				navigator.setPosition(i);
				navigator.invalidate();
				showTourDetails(i);
				if(i == tourList.size() - 1)
				{
					((TextView)findViewById(R.id.btnNext)).setText(Str.Close[Global.LANG]);
					findViewById(R.id.btnDontShowAgain).setVisibility(View.VISIBLE);
				}
				else
				{
					((TextView)findViewById(R.id.btnNext)).setText(Str.Next[Global.LANG]);
					findViewById(R.id.btnDontShowAgain).setVisibility(View.GONE);
				}
			}

			@Override
			public void onPageScrollStateChanged(int i) {

			}
		});

		findViewById(R.id.btnNext).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int nSelIdx = mViewPager.getCurrentItem();
				if(nSelIdx == tourList.size() - 1)
				{
					callback.onClose();
					dismiss();
				}
				else
				{
					mViewPager.setCurrentItem(nSelIdx + 1, true);
				}
			}
		});

		findViewById(R.id.btnSkip).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				callback.onSkip();
				dismiss();
			}
		});

		((CheckBox)findViewById(R.id.btnDontShowAgain)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				callback.onCheck(b);
			}
		});


		((TextView)findViewById(R.id.btnSkip)).setText(Str.Skip[Global.LANG]);
		((CheckBox)findViewById(R.id.btnDontShowAgain)).setText(Str.DontShowAgain[Global.LANG]);
		if(tourList.size() > 1)
		{
			((TextView)findViewById(R.id.btnNext)).setText(Str.Next[Global.LANG]);
			findViewById(R.id.btnDontShowAgain).setVisibility(View.GONE);
		}
		else
		{
			((TextView)findViewById(R.id.btnNext)).setText(Str.Close[Global.LANG]);
			findViewById(R.id.btnDontShowAgain).setVisibility(View.VISIBLE);
		}

		navigator.setSize(tourList.size());
		navigator.setPosition(0);
		navigator.invalidate();
		navigator.setVisibility(View.VISIBLE);

		loadImages();
		showTourDetails(0);
	}

	public void loadImages()
	{
		HttpsTrustManager.allowAllSSL();
		new Thread()
		{
			@Override
			public void run() {
				for(int i = 0; i < tourList.size(); i++)
				{
					mDownloadStatus.put(i, 0);
					DownloadInstance instance = new DownloadInstance(i, tourList.get(i).image_url, new DownloadInstance.DownloadCallback() {
						@Override
						public void startDownload(int nDownloadInstance) {
							mDownloadStatus.put(nDownloadInstance, 1);
							final View view = mItemView.get(nDownloadInstance);
							if(view != null)
							{
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										view.findViewById(R.id.imgTour).setVisibility(View.GONE);
										view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
										((ProgressBar)view.findViewById(R.id.progressBar)).setProgress(0);
									}
								});
							}
						}

						@Override
						public void downloading(int nDownloadInstance, int nOldProgress, final int nNewProgress) {
							final View view = mItemView.get(nDownloadInstance);
							if(view != null)
							{
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										view.findViewById(R.id.imgTour).setVisibility(View.GONE);
										view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
										((ProgressBar)view.findViewById(R.id.progressBar)).setProgress(nNewProgress);
									}
								});

							}
						}

						@Override
						public void endDownload(int nDownloadInstance, final String filePath) {
							mDownloadStatus.put(nDownloadInstance, 2);
							final View view = mItemView.get(nDownloadInstance);
							if(view != null)
							{
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										view.findViewById(R.id.imgTour).setVisibility(View.VISIBLE);
										view.findViewById(R.id.progressBar).setVisibility(View.GONE);
										Picasso.get().load(new File(filePath)).into((ImageView)view.findViewById(R.id.imgTour));
									}
								});
							}
						}
					});
					//Register DownloadInstance
					instance.start();
				}

				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						mViewPager.setAdapter(mPagerAdapter);
//						mPagerAdapter.notifyDataSetChanged();
					}
				});
			}
		}.start();
	}

	public void showTourDetails(int index)
	{
		((TextView)findViewById(R.id.txtTitle)).setText(tourList.get(index).title);
		((TextView)findViewById(R.id.txtDescription)).setText(tourList.get(index).description);
	}

	public class TourAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return tourList.size();
		}

		public Object instantiateItem(ViewGroup container, final int position) {
			View itemView = getLayoutInflater().inflate(R.layout.tour_item, container, false);

			mItemView.put(position, itemView);
			if(mDownloadStatus.get(position) == 2)
			{
				ImageView imageView = (ImageView)itemView.findViewById(R.id.imgTour);
				ProgressBar progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
				imageView.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				String strFilePath = Global.PHOTO_DIR + tourList.get(position).image_url.substring(tourList.get(position).image_url.lastIndexOf("/") + 1);
				Picasso.get().load(new File(strFilePath)).into(imageView);
			}
			else
			{
				ImageView imageView = (ImageView)itemView.findViewById(R.id.imgTour);
				ProgressBar progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
				imageView.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}

			container.addView(itemView);
			return itemView;
		}

		@Override
		public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
			return view == object;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((RelativeLayout)object);
		}
	}

}

