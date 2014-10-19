package com.example.demoforcarmera.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TextureView;

public class CameraPreviewTextureView extends TextureView {

	public CameraPreviewTextureView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CameraPreviewTextureView(Context context) {
		super(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		
		return super.onTouchEvent(event);
		
	}

}
