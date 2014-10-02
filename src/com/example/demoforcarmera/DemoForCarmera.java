package com.example.demoforcarmera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.demoforcarmera.util.CameraHelper;
import com.example.demoforcarmera.util.CameraHelper.CameraInfo2;

public class DemoForCarmera extends Activity implements
		TextureView.SurfaceTextureListener {

	CameraHelper mCameraHelper;

	private Camera mCamera;
	private com.example.demoforcarmera.view.CameraPreviewTextureView mTextureView;
	private Button flashMode;

	private int flash_state = 0;

	private int currentCameraId = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCameraHelper = new CameraHelper(this);

		setContentView(R.layout.activity_demo_for_carmera);

		mTextureView = (com.example.demoforcarmera.view.CameraPreviewTextureView) findViewById(R.id.surface_view);
		mTextureView.setSurfaceTextureListener(this);

		flashMode = (Button) findViewById(R.id.flash_mode);
		flashMode.setText("Flash Off");
		flashMode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				changeFlashMode();
			}
		});

		findViewById(R.id.take_pic).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mCamera.autoFocus(new Camera.AutoFocusCallback() {

					@Override
					public void onAutoFocus(boolean success, Camera camera) {

						takePicture();
					}

				});
			}
		});

		if (mCameraHelper.hasFrontCamera() && mCameraHelper.hasBackCamera()) {
			findViewById(R.id.change_camera).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {

							switchCamera();
							CameraInfo2 cameraInfo = new CameraInfo2();
							mCameraHelper.getCameraInfo(currentCameraId,
									cameraInfo);
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
		setupCamera(surface);
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

	protected void changeFlashMode() {
		Parameters parameters = mCamera.getParameters();

		switch (flash_state) {
		case 0:
			flash_state = 1;
			parameters.setFlashMode(Parameters.FLASH_MODE_ON);
			flashMode.setText("Flash On");
			break;
		case 1:
			flash_state = 2;
			parameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
			flashMode.setText("Flash Auto");
			break;
		case 2:
			flash_state = 0;
			parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			flashMode.setText("Flash Off");
			break;
		}

		mCamera.setParameters(parameters);
		mCamera.stopPreview();
		mCamera.startPreview();
	}

	public void switchCamera() {
		mCamera.stopPreview();
		mCamera.release();
		currentCameraId = (currentCameraId + 1)
				% mCameraHelper.getNumberOfCameras();
		setupCamera(mTextureView.getSurfaceTexture());

	}

	public void setupCamera(SurfaceTexture surface) {
		mCamera = mCameraHelper.openCamera(currentCameraId);
		initCamera();
		try {
			mCamera.setPreviewTexture(surface);
			mCamera.startPreview();
		} catch (IOException ioe) {
		}
	}

	protected void initCamera() {
		Parameters parameters = mCamera.getParameters();
		// TODO adjust by getting supportedPreviewSizes and then choosing
		// the best one for screen size (best fill screen)
		List<Size> previewSize = parameters.getSupportedPreviewSizes();
		Size mSize = getSize(previewSize);

		Log.e("preView", "preView Size width:" + mSize.width + " , height: "
				+ mSize.height);
		parameters.setPreviewSize(mSize.width, mSize.height);
		parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
		if (parameters.getSupportedFocusModes().contains(
				Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
			parameters
					.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		}
		mCamera.setParameters(parameters);
		mCamera.setDisplayOrientation(90);
	}

	private void takePicture() {
		// TODO get a size that is about the size of the screen
		Camera.Parameters params = mCamera.getParameters();
		params.setPictureSize(1280, 960);
		params.setRotation(mCameraHelper.getCameraDisplayOrientation(this,
				currentCameraId));
		mCamera.setParameters(params);
		for (Camera.Size size2 : mCamera.getParameters()
				.getSupportedPictureSizes()) {
			Log.i("ASDF", "Supported: " + size2.width + "x" + size2.height);
		}

		mCamera.takePicture(null, null, new Camera.PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, final Camera camera) {

				final File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
				if (pictureFile == null) {
					Log.d("ASDF",
							"Error creating media file, check storage permissions");
					return;
				}

				try {
					FileOutputStream fos = new FileOutputStream(pictureFile);
					fos.write(data);
					fos.close();
				} catch (FileNotFoundException e) {
					Log.d("ASDF", "File not found: " + e.getMessage());
				} catch (IOException e) {
					Log.d("ASDF", "Error accessing file: " + e.getMessage());
				}

			}
		});
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
		mCamera.stopPreview();
		mCamera.release();
		return true;
	}

	public void onSurfaceTextureUpdated(SurfaceTexture surface) {

		Log.e("onSurfaceTextureUpdated",
				"onSurfaceTextureUpdated " + System.currentTimeMillis());

	}
}
