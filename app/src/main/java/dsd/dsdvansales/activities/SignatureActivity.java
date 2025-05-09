package dsd.dsdvansales.activities;

import java.io.*;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.ViewGroup.*;
import android.widget.*;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;

public class SignatureActivity extends Activity implements OnClickListener {

	private LinearLayout mLayoutSignature = null;
	private SignatureView mSignatureView = null;
	private Bitmap mBitmap = null;
	private View mView = null;
	private File mFileSignature = null;
	private Bitmap mOrigBitmap = null;

	private boolean bTouchDraw = false;

	int nRotate = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		try
		{
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signature);

		InitView();
	}

	@SuppressWarnings("deprecation")
	private void InitView() {
		findViewById(R.id.btnClear).setOnClickListener(this);
		findViewById(R.id.btnSave).setOnClickListener(this);
		findViewById(R.id.btnBack).setOnClickListener(this);
		findViewById(R.id.btnRotate).setOnClickListener(this);

/*		String strSignaturePath = getIntent().getStringExtra(Global.EXTRA_SIGNATURE_PATH);
		if (strSignaturePath.length() > 0) {
			mOrigBitmap = Utils.getSafeDecodeBitmap(strSignaturePath, getResources().getDimensionPixelSize(R.dimen.dip_300));
		}*/
		mFileSignature = new File(Global.DEFAULT_SIGNATURE_FILEPATH);

		mLayoutSignature = (LinearLayout) findViewById(R.id.layoutSignature);
		mSignatureView = new SignatureView(this, null);
		mSignatureView.setBackgroundColor(Color.WHITE);
		mLayoutSignature.addView(mSignatureView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mView = mLayoutSignature;

		refreshLanguage();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void refreshLanguage() {
		Global.LANG = Utils.getIntSetting(getApplicationContext(), Global.PREF_CUR_LANG);
		((Button) findViewById(R.id.btnClear)).setText(Str.Clear[Global.LANG]);
		((Button) findViewById(R.id.btnSave)).setText(Str.Save[Global.LANG]);
		((Button) findViewById(R.id.btnBack)).setText(Str.Exit[Global.LANG]);
	}

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnClear:
				bTouchDraw = false;
				mOrigBitmap = null;
				mSignatureView.clear();
				break;
			case R.id.btnSave:
				if( bTouchDraw )
				{
					mView.setDrawingCacheEnabled(true);
					new Thread()
					{
						@Override
						public void run() {
							mSignatureView.save(mView);
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									Intent intent = new Intent(Global.ACTION_CAPTURE_SIGNATURE);
									intent.putExtra(Global.EXTRA_SIGNATURE_PATH, mFileSignature.getAbsolutePath());
									sendBroadcast(intent);
									finish();
								}
							});
						}
					}.start();
				}
				else
				{
					if(!SignatureActivity.this.isDestroyed() && !SignatureActivity.this.isFinishing())
					{
						Toast.makeText(this, Str.DrawSignature[Global.LANG], Toast.LENGTH_LONG).show();
					}
				}
				break;
			case R.id.btnBack:
				Intent intent = new Intent(Global.ACTION_CAPTURE_SIGNATURE_EXIT);
				intent.putExtra(Global.EXTRA_SIGNATURE_PATH, mFileSignature.getAbsolutePath());
				sendBroadcast(intent);
				finish();
				break;
			case R.id.btnRotate:
				if( nRotate == 0 ) nRotate = 180;
				else nRotate = 0;
				findViewById(R.id.whole_layout).setRotation(nRotate);
				break;
		}
		m_bClicked = false;
	}

	@Override
	public void onBackPressed() {
		findViewById(R.id.btnBack).performClick();
	}

	private class SignatureView extends View
	{
		private static final float STROKE_WIDTH = 5f;
		private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
		private Paint paint = new Paint();
		private Path path = new Path();

		private float lastTouchX;
		private float lastTouchY;
		private final RectF dirtyRect = new RectF();

		public SignatureView(Context context, AttributeSet attrs) 
		{
			super(context, attrs);
			paint.setAntiAlias(true);
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setStrokeWidth(STROKE_WIDTH);
		}

		public void save(final View v)
		{
			Utils.printDebug(null, "Width = [%d], Height = [%d]", v.getWidth(), v.getHeight());
			if (mBitmap == null)
			{
				mBitmap = Bitmap.createBitmap(mLayoutSignature.getWidth(), mLayoutSignature.getHeight(), Bitmap.Config.RGB_565);
			}
			final Canvas canvas = new Canvas(mBitmap);
			try
			{
				FileOutputStream mFileOutStream = new FileOutputStream(mFileSignature);
				v.draw(canvas);
				mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, mFileOutStream);
				mFileOutStream.flush();
				mFileOutStream.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		public void clear()
		{
			path.reset();
			invalidate();
		}

		@Override
		protected void onDraw(Canvas canvas)
		{
			if (mOrigBitmap != null)
			{
				canvas.drawBitmap(mOrigBitmap, 0, 0, paint);
			}
			canvas.drawPath(path, paint);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event)
		{
			float eventX = event.getX();
			float eventY = event.getY();

			switch (event.getAction())
			{
			case MotionEvent.ACTION_DOWN:
				path.moveTo(eventX, eventY);
				lastTouchX = eventX;
				lastTouchY = eventY;
				return true;

			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_UP:
				resetDirtyRect(eventX, eventY);
				int historySize = event.getHistorySize();
				for (int i=0; i<historySize; i++)
				{
					float historicalX = event.getHistoricalX(i);
					float historicalY = event.getHistoricalY(i);
					expandDirtyRect(historicalX, historicalY);
					path.lineTo(historicalX, historicalY);
				}
				path.lineTo(eventX, eventY);
				bTouchDraw = true;
				break;

			default:
				Utils.printDebug(null, "Ignored touch event: ", event.toString());
				return false;
			}

			invalidate(Math.round (dirtyRect.left - HALF_STROKE_WIDTH),
					Math.round (dirtyRect.top - HALF_STROKE_WIDTH),
					Math.round (dirtyRect.right + HALF_STROKE_WIDTH),
					Math.round (dirtyRect.bottom + HALF_STROKE_WIDTH));

			lastTouchX = eventX;
			lastTouchY = eventY;

			return true;
		}

		private void expandDirtyRect(float historicalX, float historicalY)
		{
			if (historicalX < dirtyRect.left)
			{
				dirtyRect.left = historicalX;
			} 
			else if (historicalX > dirtyRect.right)
			{
				dirtyRect.right = historicalX;
			}

			if (historicalY < dirtyRect.top)
			{
				dirtyRect.top = historicalY;
			} 
			else if (historicalY > dirtyRect.bottom)
			{
				dirtyRect.bottom = historicalY;
			}
		}

		private void resetDirtyRect(float eventX, float eventY)
		{
			dirtyRect.left = Math.min(lastTouchX, eventX);
			dirtyRect.right = Math.max(lastTouchX, eventX);
			dirtyRect.top = Math.min(lastTouchY, eventY);
			dirtyRect.bottom = Math.max(lastTouchY, eventY);
		}
	}
}
