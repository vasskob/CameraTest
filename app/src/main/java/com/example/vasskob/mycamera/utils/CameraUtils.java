package com.example.vasskob.mycamera.utils;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
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
import static com.example.vasskob.mycamera.utils.Constants.ASPECT_RATIO_15_9;
import static com.example.vasskob.mycamera.utils.Constants.ASPECT_RATIO_15_9_STRING;
import static com.example.vasskob.mycamera.utils.Constants.ASPECT_RATIO_16_9;
import static com.example.vasskob.mycamera.utils.Constants.ASPECT_RATIO_16_9_STRING;
import static com.example.vasskob.mycamera.utils.Constants.ASPECT_RATIO_18_9;
import static com.example.vasskob.mycamera.utils.Constants.ASPECT_RATIO_18_9_STRING;
import static com.example.vasskob.mycamera.utils.Constants.ASPECT_RATIO_4_3;
import static com.example.vasskob.mycamera.utils.Constants.ASPECT_RATIO_4_3_STRING;
import static com.example.vasskob.mycamera.utils.Constants.PICTURE_SIZE_KEY;
import static com.example.vasskob.mycamera.utils.Constants.UNKNOWN;

public class CameraUtils {

    private static final String TAG = CameraUtils.class.getSimpleName();
    private static final String DIRECTORY_NAME = "MyCameraApp";
    private static final String PHOTO_SUFFIX = "IMG_";
    private static final String VIDEO_SUFFIX = "VID_";
    private static final String PHOTO_EXTENSION = ".jpg";
    private static final String VIDEO_EXTENSION = ".mp4";
    private static final String DATE_FORMAT_PATTERN = "yyyyMMdd_HHmmss";

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
                Environment.DIRECTORY_PICTURES), DIRECTORY_NAME);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.US).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    PHOTO_SUFFIX + timeStamp + PHOTO_EXTENSION);
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    VIDEO_SUFFIX + timeStamp + VIDEO_EXTENSION);
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
        } else ratio = UNKNOWN;
        return ratio;
    }

    public static int getVideoQuality(String videoQuality) {
        switch (videoQuality) {
            case "3360x1680":
            case "3264x1836":
            case "2880x2160":
                return CamcorderProfile.QUALITY_HIGH;
            case "1920x1080":
                return CamcorderProfile.QUALITY_1080P;
            case "1280x720":
                return CamcorderProfile.QUALITY_720P;
            case "720x480":
                return CamcorderProfile.QUALITY_480P;
            case "640x480":
                return CamcorderProfile.QUALITY_LOW;
            default:
                return CamcorderProfile.QUALITY_720P;
        }
    }

    public static PictureSize fromSettingString(String sizeSettingString) {
        if (sizeSettingString == null) {
            return null;
        }
        String[] parts = sizeSettingString.split("x");
        if (parts.length != 2) {
            return null;
        }
        try {
            return new PictureSize(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (NumberFormatException e) {
            return null;
        }
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
