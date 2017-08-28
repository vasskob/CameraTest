package com.example.vasskob.mycamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CameraActivity extends Activity {

    @BindView(R.id.main_container)
    ViewGroup mRootView;
    @BindView(R.id.camera_preview)
    ViewGroup preview;

    private static final long UI_ANIMATION_DELAY = 1000;
    private static final String TAG = CameraActivity.class.getSimpleName();

    private final Handler mHideHandler = new Handler();
    private Camera mCamera;
    private CameraPreview mPreview;
    private View decorView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        makeActivityFullScreen();
        Log.d(TAG, "onCreate: ");
        checkPermissions();
    }

    protected void makeActivityFullScreen() {
        decorView = getWindow().getDecorView();
        makeFullScreen();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    mHideHandler.postDelayed(mHideRunnable, UI_ANIMATION_DELAY);
                }
            }
        });

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void makeFullScreen() {
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private final Runnable mHideRunnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            makeFullScreen();
        }
    };

    private void addCameraPreview() {
        // Create an instance of Camera
        mCamera = getCameraInstance();
        setCameraDisplayOrientation(Camera.getNumberOfCameras() - 1, mCamera);
        setCameraAutoFocus(mCamera);
        //     setFaceDetection(mCamera);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        preview.addView(mPreview);
        Log.d(TAG, "addCameraPreview: ");
    }

//    private void setFaceDetection(Camera mCamera) {
//        mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
//            @Override
//            public void onFaceDetection(Camera.Face[] faces, Camera camera) {
//
//            }
//        });
//        Camera.Parameters params = mCamera.getParameters();
//
//        // start face detection only *after* preview has started
//        if (params.getMaxNumDetectedFaces() > 0){
//            // camera supports face detection, so can start it:
//            mCamera.startFaceDetection();
//        }
//    }

    private void setCameraAutoFocus(Camera c) {
        Camera.Parameters params = c.getParameters();
        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        c.setParameters(params);
    }

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(Camera.getNumberOfCameras() - 1); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Toast.makeText(this, getString(R.string.cameraWarn), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "getCameraInstance: ", e);
        }
        return c; // returns null if camera is unavailable
    }

    public void setCameraDisplayOrientation(int cameraId, android.hardware.Camera camera) {

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();

        android.hardware.Camera.getCameraInfo(cameraId, info);

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHideHandler.removeCallbacks(mHideRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            checkPermissions();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            preview.removeView(mPreview);
            //mPreview.getHolder().removeCallback(mPreview);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mPreview = null;

        }
    }

    //////////////////////// PERMISSIONS /////////////////////////

    private void checkPermissions() {
        Permissions.check(this, new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO},
                getString(R.string.permission_rationale_message),
                new Permissions.Options()
                        .setSettingsDialogTitle(getString(R.string.cameraWarn))
                        .setRationaleDialogTitle(getString(R.string.permission_rationale_title)),
                new PermissionHandler() {
                    @Override
                    public void onGranted() {
                        Log.d(TAG, "onGranted: !!!");
                        addCameraPreview();
                    }

                    @Override
                    public void onJustBlocked(Context context, ArrayList<String> justBlockedList, ArrayList<String> deniedPermissions) {
                        super.onJustBlocked(context, justBlockedList, deniedPermissions);
                        finish();
                    }

                    @Override
                    public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                        super.onDenied(context, deniedPermissions);
                        finish();
                    }
                });
    }
}
