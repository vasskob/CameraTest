package com.example.vasskob.mycamera.customView;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.vasskob.mycamera.utils.CameraUtils;
import com.example.vasskob.mycamera.utils.PictureSize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.vasskob.mycamera.utils.CameraUtils.FOCUS_VIEW_HEIGHT;


public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = CameraPreview.class.getSimpleName();
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private FocusView focusView;
    private boolean focusViewSet;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        setupCamera();
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void setupCamera() {
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> list = parameters.getSupportedPreviewSizes();
        for (Camera.Size item : list) {
            Log.d(TAG, "setupCamera: Width= " + item.width + ", Height = " + item.height + "\n");
        }
        //   parameters.setPreviewSize(1280, 960);
        Camera.Parameters params = mCamera.getParameters();
        Camera.Size size = params.getPictureSize();
        float ratio = ((float) size.width) / ((float) size.height);
        PictureSize pictureSize = CameraUtils.getPreviewSizeForRatio(ratio);
        parameters.setPreviewSize(pictureSize.getWidth(), pictureSize.getHeight());
        mCamera.setParameters(parameters);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public void setFocusView(FocusView dView) {
        focusView = dView;
        focusViewSet = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        List<String> supportedFocusModes = mCamera.getParameters().getSupportedFocusModes();
        if (supportedFocusModes.size() == 1 && supportedFocusModes.get(0).equals("fixed")) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            Rect touchRect = new Rect(
                    (int) (x - FOCUS_VIEW_HEIGHT),
                    (int) (y - FOCUS_VIEW_HEIGHT),
                    (int) (x + FOCUS_VIEW_HEIGHT),
                    (int) (y + FOCUS_VIEW_HEIGHT));

            final Rect targetFocusRect = new Rect(
                    touchRect.left * 1000 / this.getWidth() - 500,
                    touchRect.top * 1000 / this.getHeight() - 500,
                    touchRect.right * 1000 / this.getWidth() - 500,
                    touchRect.bottom * 1000 / this.getHeight() - 500);

            doTouchFocus(targetFocusRect);
            if (focusViewSet) {
                Log.d(TAG, "onTouchEvent: ");
                focusView.setHaveTouch(true, touchRect);
                focusView.invalidate();
                // Remove the square after some time
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        focusView.setHaveTouch(false, new Rect(0, 0, 0, 0));
                        focusView.invalidate();
                    }
                }, 1000);
            }

        }
        return false;
    }


    public void doTouchFocus(final Rect tFocusRect) {
        Log.i(TAG, "TouchFocus");
        try {
            final List<Camera.Area> focusList = new ArrayList<>();
            Camera.Area focusArea = new Camera.Area(tFocusRect, 500);
            focusList.add(focusArea);

            Camera.Parameters para = mCamera.getParameters();
            para.setFocusAreas(focusList);
            para.setMeteringAreas(focusList);
            mCamera.setParameters(para);
            mCamera.autoFocus(myAutoFocusCallback);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Unable to autofocus");
        }

    }

    Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean arg0, Camera camera) {
            if (arg0) {
                camera.cancelAutoFocus();
            }
        }
    };

}
