package dsd.dsdvansales.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;


public class PhotoDetailActivity extends Activity{


	String strImagePath;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_detail);
		strImagePath = Global.gShowImageUrl;
		initView();
	}

	private void initView(){

		if(!TextUtils.isEmpty(strImagePath))
		{
			findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
			Glide.with(this)
					.load(strImagePath)
					.listener(new RequestListener<Drawable>() {
						@Override
						public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
							findViewById(R.id.progressBar).setVisibility(View.GONE);
							return false;
						}

						@Override
						public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
							findViewById(R.id.progressBar).setVisibility(View.GONE);
							return false;
						}
					})
					.dontTransform()
					.placeholder(R.drawable.unknown)
					.error(R.drawable.ic_error)
					.into((ImageView)findViewById(R.id.imgItem));
		}
	}

	@Override
	public void onPause()
	{
//		Intent intent = getIntent();
//		Uri data = intent.getData();

		super.onPause();
		System.gc();
	}

	@Override
	public void onStop()
	{
		super.onStop();
		System.gc();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		System.gc();
	}

}
