package com.example.demoforcarmera;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.demoforcarmera.util.CameraController;
import com.example.demoforcarmera.util.CameraHelper.CameraInfo2;

public class DemoForCarmera extends Activity implements
		TextureView.SurfaceTextureListener {

	private com.example.demoforcarmera.view.CameraPreviewTextureView mTextureView;

	private Button flashMode;

	CameraController mCameraController;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCameraController = new CameraController(this);

		setContentView(R.layout.activity_demo_for_carmera);

		mTextureView = (com.example.demoforcarmera.view.CameraPreviewTextureView) findViewById(R.id.surface_view);
		mTextureView.setSurfaceTextureListener(this);

		flashMode = (Button) findViewById(R.id.flash_mode);
		flashMode.setText("Flash Off");
		flashMode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mCameraController.changeFlashMode();
			}
		});

		findViewById(R.id.take_pic).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mCameraController.takeByAutoFocus(
						getOutputMediaFile(MEDIA_TYPE_IMAGE).getAbsolutePath(),
						1280, 720);
			}
		});

		if (mCameraController.getCameraCount() > 1) {
			findViewById(R.id.change_camera).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {

							mCameraController.switchCamera(mTextureView
									.getSurfaceTexture());
							CameraInfo2 cameraInfo = mCameraController
									.getCameraInfo();
							if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
								flashMode.setEnabled(false);
							} else {
								flashMode.setEnabled(true);
							}

						}
					});
		} else {
			findViewById(R.id.change_camera).setVisibility(View.INVISIBLE);
		}

	}

	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) {
		mCameraController.setupCamera(surface);
	}

	protected Size getSize(List<Size> previewSize) {

		if (previewSize.size() == 1) {
			return previewSize.get(0);
		} else {
			Point mPoint = new Point();
			getWindowManager().getDefaultDisplay().getSize(mPoint);
			int width = (int) (mPoint.x / 1.5);
			Size temp = previewSize.get(0);
			for (Size size : previewSize) {

				temp = size;
				Log.e("preView", "preView Size width:" + size.width
						+ " , height: " + size.height);
				if (width >= size.width) {
					break;
				}
			}

			return temp;
		}

	}

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	private static File getOutputMediaFile(final int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"MyCameraApp");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
		// Ignored, Camera does all the work for us
	}

	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

		mCameraController.closeCamera();
		return true;
	}

	public void onSurfaceTextureUpdated(SurfaceTexture surface) {

		Log.e("onSurfaceTextureUpdated",
				"onSurfaceTextureUpdated " + System.currentTimeMillis());

	}
}
