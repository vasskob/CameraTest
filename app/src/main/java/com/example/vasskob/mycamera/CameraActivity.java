package com.example.vasskob.mycamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.hardware.Camera.Parameters.FLASH_MODE_AUTO;
import static android.hardware.Camera.Parameters.FLASH_MODE_OFF;
import static android.hardware.Camera.Parameters.FLASH_MODE_ON;
import static android.hardware.Camera.Parameters.FLASH_MODE_TORCH;


public class CameraActivity extends Activity {

    @BindView(R.id.main_container)
    ViewGroup mRootView;

    @BindView(R.id.camera_preview)
    ViewGroup preview;

    @BindView(R.id.btn_flash)
    Button btnFlash;

    private static final String TAG = CameraActivity.class.getSimpleName();
    private static final long UI_ANIMATION_DELAY = 1000;

    private final Handler mHideHandler = new Handler();
    private final Handler switchFlashHandler = new Handler();
    private Camera mCamera;
    private CameraPreview mPreview;
    private View decorView;
    private Camera.Parameters params;
    private Drawable background;
    private String flashMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        makeActivityFullScreen();
        checkPermissions();
    }

    protected void makeActivityFullScreen() {
        decorView = getWindow().getDecorView();
        makeUiInvisible();
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

    private void makeUiInvisible() {
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private final Runnable mHideRunnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            makeUiInvisible();
        }
    };

    private final Runnable switchFlashMode = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            btnFlash.setBackground(background);
            params = mCamera.getParameters();
            params.setFlashMode(flashMode);
            mCamera.setParameters(params);
        }
    };

    private void addCameraPreview() {
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);
        setCameraAutoFocus();
        setCameraDefaultFlashMode();
        Log.d(TAG, "setCameraDefaultFlashMode: " + mCamera.getParameters().getSupportedFlashModes());

        mPreview = new CameraPreview(this, mCamera);
        preview.addView(mPreview);
    }

    private void setCameraDefaultFlashMode() {
        params = mCamera.getParameters();
        if (params.getSupportedFlashModes().contains(FLASH_MODE_AUTO)) {
            params.setFlashMode(FLASH_MODE_AUTO);
        }
        mCamera.setParameters(params);
    }

    private void setCameraAutoFocus() {
        params = mCamera.getParameters();
        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        mCamera.setParameters(params);
    }

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(0);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.cameraWarn), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "getCameraInstance: ", e);
        }
        return c;
    }

    private int clickCounter = 1;

    @OnClick(R.id.btn_flash)
    protected void changeFlashMode() {
        background = ContextCompat.getDrawable(this, R.drawable.ic_flash_auto);
        flashMode = Camera.Parameters.FLASH_MODE_AUTO;
        params = mCamera.getParameters();

        clickCounter++;
        switch (clickCounter) {
            case 1:
                background = ContextCompat.getDrawable(this, R.drawable.ic_flash_auto);
                flashMode = FLASH_MODE_AUTO;
                break;
            case 2:
                background = ContextCompat.getDrawable(this, R.drawable.ic_flash_on);
                flashMode = FLASH_MODE_ON;
                break;
            case 3:
                background = ContextCompat.getDrawable(this, R.drawable.ic_flash_off);
                flashMode = FLASH_MODE_OFF;
                break;
            case 4:
                if (getPackageManager().hasSystemFeature(
                        PackageManager.FEATURE_CAMERA_FLASH)) {
                    flashMode = FLASH_MODE_TORCH;
                }
                background = ContextCompat.getDrawable(this, R.drawable.ic_flash_torch);
                clickCounter = 0;
                break;
        }

        if (!flashMode.equals(FLASH_MODE_TORCH)) {
            params.setFlashMode(FLASH_MODE_OFF);
            mCamera.setParameters(params);
        }
        switchFlashHandler.postDelayed(switchFlashMode, 100);
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
