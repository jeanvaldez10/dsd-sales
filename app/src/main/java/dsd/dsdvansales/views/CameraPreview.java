package dsd.dsdvansales.views;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import dsd.dsdvansales.global.Utils;

@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private SurfaceHolder mHolder = null;
	private Camera mCamera = null;
	private PreviewCallback previewCallback = null;
	private AutoFocusCallback autoFocusCallback = null;

	public CameraPreview(Context context, Camera camera, PreviewCallback previewCb, AutoFocusCallback autoFocusCb) {
		super(context);
		mCamera = camera;
		previewCallback = previewCb;
		autoFocusCallback = autoFocusCb;

		// Install a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);

		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the preview.
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (Exception e) {
			Utils.printDebug(null, "Error setting camera preview");
			e.printStackTrace();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Camera preview released in activity
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		/*
		 * If your preview can change or rotate, take care of those events here.
		 * Make sure to stop the preview before resizing or reformatting it.
		 */
		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
			e.printStackTrace();
		}

		try {
			// Hard code camera surface rotation 90 degs to match Activity view in portrait
			mCamera.setDisplayOrientation(90);

			mCamera.setPreviewDisplay(mHolder);
			mCamera.setPreviewCallback(previewCallback);
			mCamera.startPreview();
			mCamera.autoFocus(autoFocusCallback);
		} catch (Exception e) {
			Utils.printDebug(null, "Error starting camera preview");
			e.printStackTrace();
		}
	}
}