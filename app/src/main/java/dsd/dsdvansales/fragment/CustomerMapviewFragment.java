package dsd.dsdvansales.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.*;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
//import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.R;
import dsd.dsdvansales.views.icon_generator.IconGenerator;

public class CustomerMapviewFragment extends BaseFragment implements GoogleMap.OnCameraIdleListener, OnMapReadyCallback {

	private MapView mMapView = null;
	private GoogleMap mGoogleMap = null;
	private boolean mIsAllCustomer = false;
	private CustomerInfo mCustomerInfo = new CustomerInfo();
	private List<CustomerInfo> mOrgCustomerList = new ArrayList<>();
	private List<CustomerInfo> mCustomerList = new ArrayList<>();
	private boolean mIsRefreshed = false;
	Marker mNoteMarker;
	LayoutInflater mInflater;
	LocationManager mLocationManager;
	Marker mCurrentMarker;
	Map<String, LatLng> customerLocationMap = new HashMap<>();

	private android.location.LocationListener mLocationListener = new android.location.LocationListener() {

		@Override
		public void onLocationChanged(Location arg0) {
			// TODO Auto-generated method stub
			LatLng position = new LatLng(arg0.getLatitude(), arg0.getLongitude());
			if (mCurrentMarker != null) {
				mCurrentMarker.setPosition(position);
			} else {
				if( mGoogleMap != null )
				{
					mCurrentMarker = mGoogleMap.addMarker(new MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(1, String.valueOf("me")))).anchor(0.5f, 1));
				}
			}

		}

		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub

		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mInflater = inflater;
		mRootView = inflater.inflate(R.layout.fragment_customer_mapview, container, false);
		mMapView = (MapView) mRootView.findViewById(R.id.mapView);
		mMapView.onCreate(savedInstanceState);

		commonInit();
//		mNoteMarker = mGoogleMap.addMarker(new MarkerOptions().title(String.valueOf("")).icon(BitmapDescriptorFactory.fromBitmap(makePinImage("ab\ntest\n"))).anchor(0.3f, 1).visible(false));
		mLocationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);

		if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return mRootView;
		}
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
				5, mLocationListener);
		return mRootView;
	}


	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
		Global.gFragIdx = Global.FRAG_CUSTOMER_MAPVIEW;
	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
/*		Intent intent = new Intent(Global.ACTION_MAP_REFRESH_CUSTOMER_LIST);
//		Intent intent = new Intent(Global.ACTION_REFRESH_CUSTOMER_LIST);
		intent.putExtra(Global.EXTRA_MAP_CUSTOMER_INFO, (Serializable)mCustomerList);
		intent.putExtra(Global.EXTRA_MAP_ORG_CUSTOMER_INFO, (Serializable)mOrgCustomerList);
		mContext.sendBroadcast(intent);*/
		Global.gCurrentCustomerList = mCustomerList;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}

	@Override
	public void initView() {
		MapsInitializer.initialize(mContext);
		mMapView.getMapAsync(this);
		mRootView.findViewById(R.id.mapView).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (mIsRefreshed == false) {
					mIsRefreshed = true;
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							loadData();
						}
					}, 500);
				}
			}
		});
	}

	private Bitmap getMarkerBitmapFromView(int resId, String strText) {
		Bitmap returnedBitmap = null;
		try
		{
			if (resId == 0) {
				resId = (int) R.drawable.map_pin_hi;
			} else if (resId == 1) {
				resId = (int) R.drawable.map_pin_me;
			} else {
				resId = (int) R.drawable.map_mpin_hi;
			}
			View customMarkerView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
			ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.imgContent);
			TextView txtView = (TextView) customMarkerView.findViewById(R.id.txtTitle);
			txtView.setText(strText);
			markerImageView.setImageResource(resId);
			customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
			customMarkerView.buildDrawingCache();
			returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
					Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(returnedBitmap);
			canvas.drawColor(Color.WHITE, Mode.SRC_IN);
			Drawable drawable = customMarkerView.getBackground();
			if (drawable != null)
				drawable.draw(canvas);
			customMarkerView.draw(canvas);
		}catch(Exception e)
		{
			e.printStackTrace();
		}

		return returnedBitmap;
	}

	@Override
	public void loadData() {
		mIsAllCustomer = getArguments().getBoolean(Global.EXTRA_IS_ALL_CUSTOMER, false);
		showProgressDialog(Str.Loading[Global.LANG]);
		new Thread()
		{
			@Override
			public void run() {
				if (mIsAllCustomer == false) {
					mCustomerInfo = (CustomerInfo) getArguments().getSerializable(Global.EXTRA_CUSTOMER_INFO);
					if(mCustomerInfo.longitude == 0 && mCustomerInfo.latitude == 0 && mCustomerInfo.driverLongitude == 0 && mCustomerInfo.driverLatitude == 0)
					{
						/*String strAddress = mCustomerInfo.strAddress1;
						if(!TextUtils.isEmpty(mCustomerInfo.strAddress2))
						{
							strAddress += ", " + mCustomerInfo.strAddress2;
						}*/
						String strAddress = mCustomerInfo.strAddress1 + ", " + mCustomerInfo.strCity + " " + mCustomerInfo.strShipToState + " " + mCustomerInfo.strShipToZip + ", Australia";
						LatLng latLng = Utils.getLocationFromAddress(mContext, strAddress);
						if(latLng != null)
						{
							customerLocationMap.put(mCustomerInfo.nCustNo + "/" + mCustomerInfo.nChainNo, latLng);
						}

					}
				} else {
					mCustomerList = Global.gCurrentCustomerList;

					//Check Customer Address and get the address.
					for(CustomerInfo customerInfo : mCustomerList)
					{
						if(customerInfo.longitude == 0 && customerInfo.latitude == 0 && customerInfo.driverLatitude == 0 && customerInfo.driverLongitude == 0)
						{
							/*String strAddress = customerInfo.strAddress1;
							if(!TextUtils.isEmpty(customerInfo.strAddress2))
							{
								strAddress += ", " + customerInfo.strAddress2;
							}*/
							String strAddress = customerInfo.strAddress1 + ", " + customerInfo.strCity + " " + customerInfo.strShipToState + " " + customerInfo.strShipToZip + ", Australia";
							LatLng latLng = Utils.getLocationFromAddress(mContext, strAddress);
							if(latLng != null)
							{
								customerLocationMap.put(customerInfo.nCustNo + "/" + customerInfo.nChainNo, latLng);
							}
						}
					}
				}


				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						hideProgressDialog();
						refreshPage();
					}
				});
			}
		}.start();

	}


	public Bitmap makePinImage(String strName, int nType) {
		if (strName.length() > 2) strName = strName.substring(strName.length() - 2);
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap bmp = Bitmap.createBitmap(160, 160, conf);
		Canvas canvas1 = new Canvas(bmp);

		// paint defines the text color,
		// stroke width, size
		Paint color = new Paint();
		color.setTextSize(70);
		color.setColor(Color.BLACK);

		//modify canvas
		if (nType == 0) {
			canvas1.drawBitmap(BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.map_pin_hi), 0, 0, color);
			canvas1.drawText(strName, 80 - strName.length() * 12, 72, color);
		} else if (nType == 2) {
			canvas1.drawBitmap(BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.map_mpin_hi), 0, 0, color);
			canvas1.drawText(strName, 80 - strName.length() * 12, 72, color);
		} else {
			canvas1.drawBitmap(BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.map_pin_me), 0, 0, color);
			canvas1.drawText(strName, 80 - strName.length() * 12, 72, color);
		}


		return bmp;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void refreshPage() {
		try
		{
			if (mIsAllCustomer == true) {

				if(ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
						ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
				{
					Toast.makeText(mContext, Str.LocationPermissionDenied[Global.LANG], Toast.LENGTH_LONG).show();
					return;
				}

				showProgressDialog("Getting current location...");

				LocationServices.getFusedLocationProviderClient(mContext).getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
					@Override
					public void onComplete(@NonNull Task<Location> task) {
						if (task.isSuccessful() && task.getResult() != null) {
							hideProgressDialog();
							Location location = task.getResult();
							double nMyLat = location.getLatitude();
							double nMyLng = location.getLongitude();
							int nDay = new Date().getDay();
							LatLng customerLocation = null;
							IconGenerator iconGenerator = new IconGenerator(mContext);
							int nDropNum = 1;
							List<Marker> markers = new ArrayList<>();
							mGoogleMap.setMyLocationEnabled(true);

							for (CustomerInfo info : mCustomerList) {
								if(info.driverLongitude != 0 || info.driverLatitude != 0)
								{
									customerLocation = new LatLng(info.driverLatitude, info.driverLongitude);
								}
								else if(info.longitude != 0 || info.latitude != 0)
								{
									customerLocation = new LatLng(info.latitude, info.longitude);
								}
								else
								{
									customerLocation = customerLocationMap.get(info.nCustNo + "/" + info.nChainNo);
								}
								if (info.nDayNo == nDay) {
									iconGenerator.setStyle(IconGenerator.STYLE_BLUE);
								} else {
									iconGenerator.setStyle(IconGenerator.STYLE_RED);
								}
								String snippet = info.strName + "\n";
								for(int i = 0; i < info.presoldOrHInfo.size(); i++)
								{
									if( info.presoldOrHInfo.get(i) != null )
									{
										if( info.presoldOrHInfo.get(i).strOrderNo != null )
											snippet += info.presoldOrHInfo.get(i).strOrderNo + "\n";
										if( info.presoldOrHInfo.get(i).strPORef != null )
											snippet += info.presoldOrHInfo.get(i).strPORef + "\n";
										if( info.presoldOrHInfo.get(i).strInstrs != null )
											snippet += info.presoldOrHInfo.get(i).strInstrs;
									}
								}
								Marker marker = null;
								if (customerLocation != null) {
									if( info.presoldOrHInfo != null && info.presoldOrHInfo.size() > 0 )
										marker = mGoogleMap.addMarker(new MarkerOptions().position(customerLocation).snippet(snippet).icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(0, String.valueOf(nDropNum++)))).anchor(0.5f, 1));
									else
										marker = mGoogleMap.addMarker(new MarkerOptions().position(customerLocation).snippet(snippet).icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(2, String.valueOf(nDropNum++)))).anchor(0.5f, 1));
									markers.add(marker);
								}
							}
//			mCurrentMarker = mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(mGoogleMap.getMyLocation().getLatitude(), mGoogleMap.getMyLocation().getLongitude())).icon(BitmapDescriptorFactory.fromBitmap(makePinImage(String.valueOf("me"), 1))).anchor(0.5f, 1));
							if (markers.size() > 0) {
//				mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(customerLocation, 14.0f));
								LatLngBounds.Builder builder = new LatLngBounds.Builder();
								for (Marker marker : markers) {
									builder.include(marker.getPosition());
								}
								LatLngBounds bounds = builder.build();
								int padding = 300;
								CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
								mGoogleMap.animateCamera(cu);
							} else if (nMyLat != Global.DEFAULT_FLOAT && nMyLng != Global.DEFAULT_FLOAT) {
								mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(nMyLat, nMyLng), 14.0f));
							}
						} else {
							hideProgressDialog();
							Toast.makeText(mContext, Str.FailedToGetCurrentLocation[Global.LANG], Toast.LENGTH_LONG).show();
						}
					}
				});
			} else {
				LatLng customerLocation = null;
				double nLat = 0, nLng = 0;
				if(mCustomerInfo.driverLatitude != 0 || mCustomerInfo.driverLongitude != 0)
				{
					nLat = mCustomerInfo.driverLatitude;
					nLng = mCustomerInfo.driverLongitude;
				}
				else if(mCustomerInfo.latitude != 0 || mCustomerInfo.longitude != 0)
				{
					nLat = mCustomerInfo.latitude;
					nLng = mCustomerInfo.longitude;
				}


				if (nLat != 0 && nLng != 0) {
					customerLocation = new LatLng(nLat, nLng);
				}
				else
				{
					customerLocation = customerLocationMap.get(mCustomerInfo.nCustNo + "/" + mCustomerInfo.nChainNo);
				}

				if (customerLocation == null) {
					Utils.printDebug(mContext, Str.Invalid_location_info[Global.LANG]);
				} else {
					mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(customerLocation, 14.0f));
					mGoogleMap.addMarker(new MarkerOptions().position(customerLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marker)));
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void refreshLanguage() {
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	@Override
	public void onCameraIdle() {

	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mGoogleMap = googleMap;
		mGoogleMap.setOnCameraIdleListener(this);

		if (mGoogleMap == null) {
			Utils.printDebug(mContext, "Can't load Google Map.");
			mContext.onBackPressed();
			return;
		}

		if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}

		mGoogleMap.setMyLocationEnabled(true);
		mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
		mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
		mGoogleMap.getUiSettings().setCompassEnabled(true);
		mGoogleMap.getUiSettings().setRotateGesturesEnabled(true);
		mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);

		mGoogleMap.setInfoWindowAdapter(new InfoWindowAdapter() {

			@Override
			public View getInfoContents(Marker arg0) {

				return null;
			}

			@Override
			public View getInfoWindow(Marker arg0) {
				if (arg0 != null && arg0.getSnippet() != null) {
					View view = mInflater.inflate(R.layout.note_hover, null);

					((TextView) view.findViewById(R.id.content)).setText(arg0.getSnippet());
					return view;
				} else {
					return null;
				}

			}

		});
	}
}