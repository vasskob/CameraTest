package com.example.vasskob.mycamera.utils;

import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class CameraUtils {
    public static final String BACK_CAMERA_QUALITY = "back_camera_quality";
    public static final String BACK_CAMERA_2_QUALITY = "back_camera2_quality";
    public static final String FRONT_CAMERA_QUALITY = "front_camera_quality";
    public static final String BACK_VIDEO_QUALITY = "back_video_quality";
    public static final String FRONT_VIDEO_QUALITY = "front_video_quality";
    public static final String JPEG_COMPRESSION = "jpeg_compression";
    public static final String CAMERA_CATEGORY = "camera_category";

    public static final String ASPECT_RATIO_4_3_STRING = "(4:3)";
    public static final String ASPECT_RATIO_16_9_STRING = "(16:9)";
    public static final String ASPECT_RATIO_15_9_STRING = "(15:9)";

    private static final float ASPECT_RATIO_4_3 = 1.3333334f;
    private static final float ASPECT_RATIO_16_9 = 1.7777778f;
    private static final float ASPECT_RATIO_15_9 = 1.6666666f;

    public static final String UNKNOWN = "Unknown";


    public static final String PHOTO_PATH = "PHOTO_PATH";
    public static final int FOCUS_VIEW_HEIGHT = 100;

    private static final String TAG = CameraUtils.class.getSimpleName();

    public static Camera getCameraInstance(int cameraId) {
        Camera c = null;
        try {
            c = Camera.open(cameraId);
        } catch (Exception e) {
            Log.e(TAG, "getCameraInstance: ", e);
        }
        return c;
    }

    public static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }

    public static String getStringRatio(float v) {
        String ratio;
        if (v == ASPECT_RATIO_4_3) {
            ratio = ASPECT_RATIO_4_3_STRING;
        } else if (v == ASPECT_RATIO_16_9) {
            ratio = ASPECT_RATIO_16_9_STRING;
        } else if (v == ASPECT_RATIO_15_9) {
            ratio = ASPECT_RATIO_15_9_STRING;
        } else ratio = UNKNOWN;
        return ratio;
    }

    public static PictureSize getPreviewSizeForRatio(float v) {
        PictureSize pictureSize;
        if (v == ASPECT_RATIO_4_3) {
            pictureSize = new PictureSize(800, 600);
        } else if (v == ASPECT_RATIO_16_9) {
            pictureSize = new PictureSize(1280, 720);
        } else if (v == ASPECT_RATIO_15_9) {
            pictureSize = new PictureSize(1280, 768);
        } else pictureSize = new PictureSize(800, 600);
        return pictureSize;
    }

}
