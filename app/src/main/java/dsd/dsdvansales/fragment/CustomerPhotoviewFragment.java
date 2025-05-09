package dsd.dsdvansales.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.views.photoview.PhotoViewAttacher;

public class CustomerPhotoviewFragment extends BaseFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_customer_photoview, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_CUSTOMER_PHOTOVIEW;
	}

	@Override
	public void initView() {
		String strPhotoPath = getArguments().getString(Global.EXTRA_PHOTO_PATH);
		ImageView imgPhoto = (ImageView) mRootView.findViewById(R.id.imgPhoto);
		if (Utils.isFileExist(strPhotoPath) == true) {
			Bitmap bmp = Utils.getSafeDecodeBitmap(strPhotoPath, 1000);
			if (bmp != null) {
				imgPhoto.setImageBitmap(bmp);
				PhotoViewAttacher attacher = new PhotoViewAttacher(imgPhoto);
				attacher.setScaleType(ImageView.ScaleType.FIT_CENTER);
			}
		}
	}

	
	@Override
	public void loadData() {
	}

	@Override
	public void refreshPage() {
	}

	@Override
	public void refreshLanguage() {
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}
}