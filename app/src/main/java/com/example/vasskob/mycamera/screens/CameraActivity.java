package com.example.vasskob.mycamera.screens;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.vasskob.mycamera.R;
import com.example.vasskob.mycamera.customView.CameraPreview;
import com.example.vasskob.mycamera.customView.FocusView;
import com.example.vasskob.mycamera.utils.CameraUtils;
import com.example.vasskob.mycamera.utils.PictureSize;
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
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
import static com.example.vasskob.mycamera.utils.Constants.ASPECT_RATIO_16_9;
import static com.example.vasskob.mycamera.utils.Constants.BACK_CAMERA_2_QUALITY;
import static com.example.vasskob.mycamera.utils.Constants.BACK_CAMERA_QUALITY;
import static com.example.vasskob.mycamera.utils.Constants.BACK_VIDEO_QUALITY;
import static com.example.vasskob.mycamera.utils.Constants.FOR_4K_UHD;
import static com.example.vasskob.mycamera.utils.Constants.FRONT_CAMERA_QUALITY;
import static com.example.vasskob.mycamera.utils.Constants.FRONT_VIDEO_QUALITY;
import static com.example.vasskob.mycamera.utils.Constants.JPEG_COMPRESSION;
import static com.example.vasskob.mycamera.utils.Constants.PHOTO_PATH;
import static com.example.vasskob.mycamera.utils.Constants.SD;
import static com.example.vasskob.mycamera.utils.Constants.UHD;


public class CameraActivity extends Activity implements Camera.PictureCallback, Camera.ShutterCallback {

    public static final int HEIGHT = 720;
    private static final int DEFAULT_FLASH_COUNTER_VALUE = 1;
    private static final int DEFAULT_FLASH_BTN_BACKGROUND = R.drawable.ic_flash_auto;
    public static final float FOR_K_MULTIPLIER = 5.0f;
    public static final String SOUND_RECORDING_KEY = "sound_recording_key";
    public static final int VIDEO_PREVIEW_WIDTH = 1280;

    @BindView(R.id.camera_preview)
    ViewGroup preview;

    @BindView(R.id.btn_flash)
    Button btnFlash;

    @BindView(R.id.iv_thumbnail)
    CircularImageView ivThumbnail;

    @BindView(R.id.focus_view)
    FocusView focusView;

    @BindView(R.id.fl_preview_container)
    FrameLayout flPreviewContainer;

    private static final String TAG = CameraActivity.class.getSimpleName();
    private static final String WAKE_LOCK_TAG = "TORCH_WAKE_LOCK";

    private static final long UI_ANIMATION_DELAY = 1000;
    private final Handler mHideHandler = new Handler();
    private final Handler switchHandler = new Handler();
    private Camera mCamera;
    private CameraPreview mPreview;
    private View decorView;
    private Camera.Parameters params;
    private int flashBtnBackground = DEFAULT_FLASH_BTN_BACKGROUND;
    private String flashMode;
    private SensorManager sensorManager;
    private PowerManager.WakeLock wakeLock;
    private File pictureFile;

    private boolean frontCameraOn;
    private MediaRecorder mMediaRecorder;
    private PictureSize photoPictureSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        makeActivityFullScreen();
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
            btnFlash.setCompoundDrawablesWithIntrinsicBounds(0, 0, flashBtnBackground, 0);
            params = mCamera.getParameters();
            params.setFlashMode(flashMode);
            mCamera.setParameters(params);
        }
    };

    private final Runnable videoClickDelay = new Runnable() {
        @Override
        public void run() {
            btnCaptureVideo.setClickable(true);
        }
    };

    private final Runnable photoClickDelay = new Runnable() {
        @Override
        public void run() {
            btnCapturePhoto.setClickable(true);
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
        //setCameraAutoFocus();
        setCameraDefaultFlashMode();
        setCameraResolution();
        setupCameraPreviewSize(false);
        changePreviewSize();
        startWakeLock();
    }


    private void changePreviewSize() {
        mPreview = new CameraPreview(this, mCamera);
        mPreview.setFocusView(focusView);
        preview.addView(mPreview);
    }

    public void setupCameraPreviewSize(boolean isVideoRecording) {
        if (isVideoRecording) {
            Log.d(TAG, "setupCameraPreviewSize: 0");
            setupVideoPreviewSize();
        } else {
            Log.d(TAG, "setupCameraPreviewSize: 1");
            setupPhotoPreviewSize();
        }
    }

    private void setupPhotoPreviewSize() {
        Camera.Parameters params = mCamera.getParameters();
        Camera.Size size = params.getPictureSize();
        float ratio = ((float) size.width) / ((float) size.height);
        PictureSize pictureSize = CameraUtils.getPreviewSizeForRatio(ratio);
        params.setPreviewSize(pictureSize.width(), pictureSize.height());
        mCamera.setParameters(params);
    }

    private void setupVideoPreviewSize() {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(VIDEO_PREVIEW_WIDTH, HEIGHT);
        mCamera.setParameters(parameters);
    }

    //    private void setCameraAutoFocus() {
//        params = mCamera.getParameters();
//        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//        }
//        mCamera.setParameters(params);
//    }

    private void setCameraResolution() {
        String prefSize = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(getKey(), "");
        String prefJpeg = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(JPEG_COMPRESSION, "");

        if (prefSize.isEmpty()) {
            return;
        }
        Log.d(TAG, "setCameraResolution: prefSize" + prefSize + " counter =" + switchCounter);
        photoPictureSize = CameraUtils.fromSettingString(prefSize);

        params = mCamera.getParameters();
        params.setPictureSize(photoPictureSize.width(), photoPictureSize.height());
        params.setJpegQuality(Integer.valueOf(prefJpeg));
        mCamera.setParameters(params);
        setPreviewRatio(photoPictureSize.aspectRatio());
        Log.d(TAG, "setCameraResolution: prefs = " + photoPictureSize.toString() + " jpeg = " + prefJpeg);
    }

    private void setPreviewRatio(float ratio) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        flPreviewContainer.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (displaymetrics.widthPixels * ratio)));
    }

    private String getKey() {
        if (frontCameraOn) {
            return FRONT_CAMERA_QUALITY;
        }
        switch (switchCounter) {
            case 1:
                return BACK_CAMERA_2_QUALITY;
            default:
                return BACK_CAMERA_QUALITY;
        }
    }

    private void startWakeLock() {
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
            }
        }
        wakeLock.acquire(5 * 60 * 1000L /*5 minutes*/);
        Log.d(TAG, "WakeLock acquired");
    }

    private void stopWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
            Log.d(TAG, "WakeLock released");
        }
    }

    private void setCameraDefaultFlashMode() {
        params = mCamera.getParameters();
        if (params.getSupportedFlashModes() != null && params.getSupportedFlashModes().contains(flashMode)) {
            params.setFlashMode(flashMode);
        }
        mCamera.setParameters(params);
        btnFlash.setCompoundDrawablesWithIntrinsicBounds(0, 0, flashBtnBackground, 0);
    }

    private int flashClickCounter = DEFAULT_FLASH_COUNTER_VALUE;

    @OnClick(R.id.btn_flash)
    protected void changeFlashMode() {
        flashMode = Camera.Parameters.FLASH_MODE_AUTO;
        params = mCamera.getParameters();

        flashClickCounter++;
        switch (flashClickCounter) {
            case 1:
                flashBtnBackground = R.drawable.ic_flash_auto;
                flashMode = FLASH_MODE_AUTO;
                break;
            case 2:
                flashBtnBackground = R.drawable.ic_flash_on;
                flashMode = FLASH_MODE_ON;
                break;
            case 3:
                flashBtnBackground = R.drawable.ic_flash_off;
                flashMode = FLASH_MODE_OFF;
                break;
            case 4:
                if (getPackageManager().hasSystemFeature(
                        PackageManager.FEATURE_CAMERA_FLASH)) {
                    flashMode = FLASH_MODE_TORCH;
                }
                flashBtnBackground = R.drawable.ic_flash_torch;
                flashClickCounter = 0;
                break;
        }

        if (!flashMode.equals(FLASH_MODE_TORCH)) {
            params.setFlashMode(FLASH_MODE_OFF);
            mCamera.setParameters(params);
        }
        switchHandler.postDelayed(switchFlashMode, 100);
    }

    @OnClick(R.id.iv_thumbnail)
    protected void onThumbnailClick() {
        if (pictureFile != null) {
            Intent intent = new Intent(this, PhotoViewerActivity.class);
            intent.putExtra(PHOTO_PATH, pictureFile.getAbsolutePath());
            startActivity(intent);
        }
    }

    @BindView(R.id.btn_capture_photo)
    Button btnCapturePhoto;

    @OnClick(R.id.btn_capture_photo)
    protected void onCapturePhotoClick() {
        mCamera.takePicture(this, null, this);
        getOrientation();
        btnCapturePhoto.setClickable(false);
        switchHandler.postDelayed(photoClickDelay, 500);
    }

    @BindView(R.id.btn_capture_video)
    Button btnCaptureVideo;

    @OnClick(R.id.btn_capture_video)
    protected void onCaptureVideoClick() {
        startVideoRecording();
        btnCaptureVideo.setClickable(false);
        switchHandler.postDelayed(videoClickDelay, 500);
    }

    private boolean isRecording = false;

    private void startVideoRecording() {
        if (isRecording) {
            setPreviewRatio(photoPictureSize.aspectRatio());
            mMediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder
            btnCaptureVideo.setSelected(false);
            isRecording = false;
            preview.removeView(mPreview);
            mCamera.stopPreview();
            setupCameraPreviewSize(false);
            changePreviewSize();
        } else {
            setPreviewRatio(ASPECT_RATIO_16_9);
            preview.removeView(mPreview);
            mCamera.stopPreview();
            setupCameraPreviewSize(true);
            changePreviewSize();
            switchHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (prepareVideoRecorder()) {
                        mMediaRecorder.start();
                        btnCaptureVideo.setSelected(true);
                        isRecording = true;
                    } else {
                        releaseMediaRecorder();
                    }
                }
            }, 500);

        }
    }

    private boolean prepareVideoRecorder() {

        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        if (isSoundRecord()) {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        }
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        String videoQualityString = getVideoQualityString();
        String videoQuality = PreferenceManager.getDefaultSharedPreferences(this).getString(videoQualityString, "");
        PictureSize pictureSize = CameraUtils.fromSettingString(videoQuality);
        int profileQuality = getVideoQuality(videoQualityString);
        setMediaProfileQuality(pictureSize, profileQuality);
        mMediaRecorder.setOutputFile(CameraUtils.getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    @NonNull
    private String getVideoQualityString() {
        String videoQualityString;
        if (frontCameraOn) {
            videoQualityString = FRONT_VIDEO_QUALITY;
        } else {
            videoQualityString = BACK_VIDEO_QUALITY;
        }
        return videoQualityString;
    }

    private int getVideoQuality(String cameraId) {
        String videoQuality = PreferenceManager.getDefaultSharedPreferences(this).getString(cameraId, "");
        Log.d(TAG, "getVideoQuality: videoQuality = " + videoQuality);
        return CameraUtils.getVideoQuality(videoQuality);
    }

    private void setMediaProfileQuality(PictureSize pictureSize, int profileQuality) {
        boolean isSoundOn = isSoundRecord();
        if (isUHDQuality(pictureSize)) {
            setProfile4KQuality(pictureSize, isSoundOn);
            return;
        }
        if (isLowQuality(pictureSize)) {
            setProfileLowQuality(pictureSize, isSoundOn);
        } else {
            setProfileQuality(profileQuality, isSoundOn);
        }
    }

    private boolean isUHDQuality(PictureSize pictureSize) {
        return pictureSize.getVideoLabel().equals(FOR_4K_UHD)
                || pictureSize.getVideoLabel().equals(UHD);
    }

    private void setProfile4KQuality(PictureSize videoSize, boolean isAudioOn) {
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
        if (isAudioOn) {
            setAudioParams(profile);
        }
        setVideoRateParams(profile, FOR_K_MULTIPLIER);
        mMediaRecorder.setVideoSize(videoSize.width(), videoSize.height());
    }

    private void setAudioParams(CamcorderProfile profile) {
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mMediaRecorder.setAudioEncodingBitRate(profile.audioBitRate);
        mMediaRecorder.setAudioSamplingRate(profile.audioSampleRate);
        mMediaRecorder.setAudioChannels(profile.audioChannels);
    }

    private void setVideoRateParams(CamcorderProfile profile, float multiplier) {
        mMediaRecorder.setVideoFrameRate(profile.videoFrameRate);
        mMediaRecorder.setVideoEncodingBitRate((int) (profile.videoBitRate * multiplier));
        mMediaRecorder.setVideoEncoder(profile.videoCodec);
    }

    private boolean isLowQuality(PictureSize pictureSize) {
        return pictureSize.getVideoLabel().equals(SD);
    }

    private void setProfileLowQuality(PictureSize videoSize, boolean isAudioOn) {
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        if (isAudioOn) {
            setAudioParams(profile);
        }
        setVideoRateParams(profile, 1);
        mMediaRecorder.setVideoSize(videoSize.width(), videoSize.height());
    }

    private void setProfileQuality(int profileQuality, boolean isAudioOn) {
        CamcorderProfile profile = CamcorderProfile.get(profileQuality);
        if (isAudioOn) {
            setAudioParams(profile);
        }
        setVideoRateParams(profile, 1);
        mMediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
    }

    private boolean isSoundRecord() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SOUND_RECORDING_KEY, true);
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    public void getOrientation() {
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        assert sensorManager != null;
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);

    }

    private int screenOrientation;
    private SensorEventListener listener = new SensorEventListener() {
        int orientation = -1;

        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.d(TAG, "onSensorChanged: eventValue " + event.values[1]);
            if (event.values[1] < 4.5 && event.values[1] > -4.5) {
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

        }
    };

    private void removeListener() {
        sensorManager.unregisterListener(listener);
    }

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
        Log.d(TAG, "onSwitchCameraClick: numberOfCameras = " + Camera.getNumberOfCameras());
        if (switchCounter == Camera.getNumberOfCameras()) {
            switchCounter = 0;
        }
        frontCameraOn = switchCounter == Camera.getNumberOfCameras() - 1;
    }

    @OnClick(R.id.btn_settings)
    protected void onSettingsClick() {
        startActivity(new Intent(this, SettingsActivity.class));
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
            Log.d(TAG, "onResume: ");
            checkPermissions(switchCounter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void releaseCamera() {
        stopWakeLock();
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            preview.removeView(mPreview);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mPreview = null;
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        pictureFile = CameraUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");
            return;
        }
        try {
            int rotateDegree;
            FileOutputStream fos = new FileOutputStream(pictureFile);
            Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length);
            Log.d(TAG, "onCaptureBtnClick: screenOrientation!!! = " + screenOrientation);
            if (screenOrientation == 0) {
                if (frontCameraOn) {
                    rotateDegree = -90;
                } else rotateDegree = 90;
                realImage = rotate(realImage, rotateDegree);
            }
            realImage.compress(Bitmap.CompressFormat.JPEG, 30, fos);
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
                .into(ivThumbnail);
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
