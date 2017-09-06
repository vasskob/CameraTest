package com.example.vasskob.mycamera.utils;

import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class CameraUtils {

    private static final String TAG = CameraUtils.class.getSimpleName();
    public static final String BACK_CAMERA_QUALITY = "back_camera_quality";
    public static final String BACK_CAMERA_2_QUALITY = "back_camera2_quality";
    public static final String FRONT_CAMERA_QUALITY = "front_camera_quality";
    public static final String BACK_VIDEO_QUALITY = "back_video_quality";
    public static final String FRONT_VIDEO_QUALITY = "front_video_quality";
    public static final String JPEG_COMPRESSION = "jpeg_compression";
    public static final String CAMERA_CATEGORY = "camera_category";

    public static final String PHOTO_PATH = "PHOTO_PATH";
    private static final String ASPECT_RATIO_4_3_STRING = "(4:3)";
    private static final String ASPECT_RATIO_15_9_STRING = "(15:9)";
    private static final String ASPECT_RATIO_16_9_STRING = "(16:9)";

    private static final String ASPECT_RATIO_18_9_STRING = "(18:9)";
    private static final String ASPECT_RATIO_UNKNOWN = "Unknown";
    private static final float ASPECT_RATIO_4_3 = 1.3333334f;
    private static final float ASPECT_RATIO_15_9 = 1.6666666f;
    private static final float ASPECT_RATIO_16_9 = 1.7777778f;
    private static final float ASPECT_RATIO_18_9 = 2.0f;

    public static final int FOCUS_VIEW_HEIGHT = 100;
    private static final String PICTURE_SIZE_KEY = "picture";

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
        } else if (v == ASPECT_RATIO_18_9) {
            ratio = ASPECT_RATIO_18_9_STRING;
        } else ratio = ASPECT_RATIO_UNKNOWN;
        return ratio;
    }

    public static PictureSize getPreviewSizeForRatio(float v) {
        PictureSize pictureSize;
        if (v == ASPECT_RATIO_4_3) {
            pictureSize = new PictureSize(640, 480);
        } else if (v == ASPECT_RATIO_16_9 || v == ASPECT_RATIO_18_9) {
            pictureSize = new PictureSize(1280, 720);
        } else if (v == ASPECT_RATIO_15_9) {
            pictureSize = new PictureSize(1280, 768);
        } else pictureSize = new PictureSize(640, 480);
        return pictureSize;
    }

    public static void savePicSizesToStorage(Context context, PictureSizeLoader.PictureSizes pictureSizes) {
        InternalStorage storage = new InternalStorage();
        try {
            storage.writeObject(context, PICTURE_SIZE_KEY, pictureSizes);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static PictureSizeLoader.PictureSizes loadPicSizesFromStorage(Context context) {
        InternalStorage storage = new InternalStorage();
        PictureSizeLoader.PictureSizes pSizes = null;
        try {
            pSizes = (PictureSizeLoader.PictureSizes) storage.readObject(context, PICTURE_SIZE_KEY);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return pSizes;
    }

    public static final class InternalStorage {

        private InternalStorage() {
        }

        void writeObject(Context context, String key, Object object) throws IOException {
            FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
            fos.close();
        }

        Object readObject(Context context, String key) throws IOException,
                ClassNotFoundException {
            FileInputStream fis = context.openFileInput(key);
            ObjectInputStream ois = new ObjectInputStream(fis);
            return ois.readObject();
        }
    }
}
