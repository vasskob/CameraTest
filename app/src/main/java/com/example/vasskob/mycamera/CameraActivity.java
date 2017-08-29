package com.example.vasskob.mycamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.vasskob.mycamera.utils.CameraUtils;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.hardware.Camera.Parameters.FLASH_MODE_AUTO;
import static android.hardware.Camera.Parameters.FLASH_MODE_OFF;
import static android.hardware.Camera.Parameters.FLASH_MODE_ON;
import static android.hardware.Camera.Parameters.FLASH_MODE_TORCH;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;


public class CameraActivity extends Activity implements Camera.PictureCallback, Camera.ShutterCallback {

    @BindView(R.id.main_container)
    ViewGroup mRootView;

    @BindView(R.id.camera_preview)
    ViewGroup preview;

    @BindView(R.id.btn_flash)
    Button btnFlash;

    @BindView(R.id.iv_thumbnail)
    CircularImageView ivThumbnail;

    private static final String TAG = CameraActivity.class.getSimpleName();
    private static final long UI_ANIMATION_DELAY = 1000;

    private final Handler mHideHandler = new Handler();
    private final Handler switchHandler = new Handler();
    private Camera mCamera;
    private CameraPreview mPreview;
    private View decorView;
    private Camera.Parameters params;
    private Drawable background;
    private String flashMode;
    private SensorManager sensorManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        makeActivityFullScreen();
        checkPermissions(0);
    }

    protected void makeActivityFullScreen() {
        decorView = getWindow().getDecorView();
        makeUiInvisible();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    mHideHandler.postDelayed(mHideStatusBar, UI_ANIMATION_DELAY);
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

    private final Runnable mHideStatusBar = new Runnable() {
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

    private void addCameraPreview(int cameraId) {
        mCamera = CameraUtils.getCameraInstance(cameraId);
        if (mCamera == null) {
            Toast.makeText(this, getString(R.string.cameraWarn), Toast.LENGTH_SHORT).show();
            return;
        }
        mCamera.setDisplayOrientation(90);
        if (mCamera.getParameters().getSupportedFlashModes() == null) {
            btnFlash.setVisibility(View.GONE);
        } else {
            btnFlash.setVisibility(View.VISIBLE);
        }
        setCameraAutoFocus();
        setCameraDefaultFlashMode();
        Log.d(TAG, "setCameraDefaultFlashMode: " + mCamera.getParameters().getSupportedFlashModes());

        mPreview = new CameraPreview(this, mCamera);
        preview.addView(mPreview);
    }

    private void setCameraDefaultFlashMode() {
        params = mCamera.getParameters();
        if (params.getSupportedFlashModes() != null && params.getSupportedFlashModes().contains(FLASH_MODE_AUTO)) {
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
        switchHandler.postDelayed(switchFlashMode, 100);
    }

    @OnClick(R.id.btn_capture)
    protected void onCaptureBtnClick() {
        mCamera.takePicture(this, null, this);
        getOrientation();

    }

    private void removeListener() {
        sensorManager.unregisterListener(listener);
    }

    public void getOrientation() {
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);

    }

    private SensorEventListener listener = new SensorEventListener() {
        int orientation = -1;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.values[1] < 6.5 && event.values[1] > -6.5) {
                if (orientation != 1) {
                    Log.d("Sensor", "Landscape");
                    screenOrientation = 1;
                }
                orientation = 1;
            } else {
                if (orientation != 0) {
                    Log.d("Sensor", "Portrait");
                    screenOrientation = 0;
                }
                orientation = 0;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    };

    private int screenOrientation;


    @BindView(R.id.btn_camera_switch)
    ImageView ivCameraSwitch;
    private int switchCounter;

    @OnClick(R.id.btn_camera_switch)
    protected void onSwitchCameraClick() {
        Drawable vector = ivCameraSwitch.getDrawable();
        ((Animatable) vector).start();
        switchCounter++;
        switchHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                releaseCamera();
                checkPermissions(switchCounter);
            }
        }, 200);
        if (switchCounter == Camera.getNumberOfCameras()) {
            switchCounter = 0;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHideHandler.removeCallbacks(mHideStatusBar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            checkPermissions(0);
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

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        File pictureFile = CameraUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length);
            Log.d(TAG, "onCaptureBtnClick: orientation = " + screenOrientation);
            if (screenOrientation == 0) {
                realImage = rotate(realImage, -90);
            }
            realImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            removeListener();
            mCamera.startPreview();
            showThumbnails(pictureFile);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        Log.d(TAG, "rotate: = " + degree);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        //       mtx.postRotate(degree);
        mtx.setRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    private void showThumbnails(final File pictureFile) {
        Glide.with(this)
                .load(pictureFile)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Toast.makeText(CameraActivity.this, String.format(getString(R.string.success_save_warn), pictureFile), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                })
                .into(ivThumbnail)
                .onLoadFailed(ContextCompat.getDrawable(this, R.drawable.picture));
    }

    @Override
    public void onShutter() {
        Toast.makeText(this, getString(R.string.success_warn), Toast.LENGTH_SHORT).show();
    }

    //////////////////////// PERMISSIONS /////////////////////////

    private void checkPermissions(final int cameraId) {
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
                        addCameraPreview(cameraId);
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
