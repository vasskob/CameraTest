package com.example.vasskob.mycamera.utils;

import android.hardware.Camera;

import java.io.Serializable;
import java.util.List;

public class PictureSizeLoader {

    private static final String TAG = PictureSizeLoader.class.getSimpleName();

    public static PictureSizes getPictureSizes() {
        int n = Camera.getNumberOfCameras();
        if (n == 3) {
            return new PictureSizes(computeSizesForCamera(n - 3),
                    computeSizesForCamera(n - 2),
                    computeSizesForCamera(n - 1),
                    computeVideoForCamera(n - 3),
                    computeVideoForCamera(n - 1));
        } else if (n == 2) {
            return new PictureSizes(computeSizesForCamera(n - 2),
                    null,
                    computeSizesForCamera(n - 1),
                    computeVideoForCamera(n - 2),
                    computeVideoForCamera(n - 1));
        } else return new PictureSizes(computeSizesForCamera(n - 1),
                null,
                null,
                computeVideoForCamera(n - 1),
                null
        );
    }

    private static List<PictureSize> computeSizesForCamera(int cameraId) {
        Camera camera = CameraUtils.getCameraInstance(cameraId);
        List<Camera.Size> supportedPictureSizes = camera.getParameters().getSupportedPictureSizes();
        List<PictureSize> camSizes = new PhotoSizeMapper().mapList(supportedPictureSizes);
        camera.release();
        return camSizes;
    }

    private static List<PictureSize> computeVideoForCamera(int cameraId) {
        Camera camera = CameraUtils.getCameraInstance(cameraId);
        List<Camera.Size> supportedVideoSizes = camera.getParameters().getSupportedVideoSizes();
        List<PictureSize> videoSizes = new PhotoSizeMapper().mapList(supportedVideoSizes);
        camera.release();
        return videoSizes;
    }

    public static class PictureSizes implements Serializable {
        final List<PictureSize> backCamera1Sizes;
        final List<PictureSize> backCamera2Sizes;
        final List<PictureSize> frontCameraSizes;
        final List<PictureSize> videoQualitiesBack;
        final List<PictureSize> videoQualitiesFront;

        PictureSizes(List<PictureSize> backCamera1Sizes, List<PictureSize> backCamera2Sizes, List<PictureSize> frontCameraSizes, List<PictureSize> videoQualitiesBack, List<PictureSize> videoQualitiesFront) {
            this.backCamera1Sizes = backCamera1Sizes;
            this.backCamera2Sizes = backCamera2Sizes;
            this.frontCameraSizes = frontCameraSizes;
            this.videoQualitiesBack = videoQualitiesBack;
            this.videoQualitiesFront = videoQualitiesFront;
        }

        public List<PictureSize> getBackCamera1Sizes() {
            return backCamera1Sizes;
        }

        public List<PictureSize> getBackCamera2Sizes() {
            return backCamera2Sizes;
        }

        public List<PictureSize> getFrontCameraSizes() {
            return frontCameraSizes;
        }

        public List<PictureSize> getVideoQualitiesBack() {
            return videoQualitiesBack;
        }

        public List<PictureSize> getVideoQualitiesFront() {
            return videoQualitiesFront;
        }
    }

}
