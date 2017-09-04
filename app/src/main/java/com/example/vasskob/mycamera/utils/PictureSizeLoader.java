package com.example.vasskob.mycamera.utils;

import android.hardware.Camera;

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
            return new PictureSizes(computeSizesForCamera(n - 2), null,
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

    private static List<Camera.Size> computeSizesForCamera(int cameraId) {
        Camera camera = CameraUtils.getCameraInstance(cameraId);
        List<Camera.Size> camSizes = camera.getParameters().getSupportedPictureSizes();
        camera.release();
        return camSizes;
    }

    private static List<Camera.Size> computeVideoForCamera(int cameraId) {
        Camera camera = CameraUtils.getCameraInstance(cameraId);
        List<Camera.Size> videoSizes = camera.getParameters().getSupportedVideoSizes();
        camera.release();
        return videoSizes;
    }

    public static class PictureSizes {
        final List<Camera.Size> backCamera1Sizes;
        final List<Camera.Size> backCamera2Sizes;
        final List<Camera.Size> frontCameraSizes;
        final List<Camera.Size> videoQualitiesBack;
        final List<Camera.Size> videoQualitiesFront;

        PictureSizes(List<Camera.Size> backCamera1Sizes, List<Camera.Size> backCamera2Sizes, List<Camera.Size> frontCameraSizes, List<Camera.Size> videoQualitiesBack, List<Camera.Size> videoQualitiesFront) {
            this.backCamera1Sizes = backCamera1Sizes;
            this.backCamera2Sizes = backCamera2Sizes;
            this.frontCameraSizes = frontCameraSizes;
            this.videoQualitiesBack = videoQualitiesBack;
            this.videoQualitiesFront = videoQualitiesFront;
        }

        public List<Camera.Size> getBackCamera1Sizes() {
            return backCamera1Sizes;
        }

        public List<Camera.Size> getBackCamera2Sizes() {
            return backCamera2Sizes;
        }

        public List<Camera.Size> getFrontCameraSizes() {
            return frontCameraSizes;
        }

        public List<Camera.Size> getVideoQualitiesBack() {
            return videoQualitiesBack;
        }

        public List<Camera.Size> getVideoQualitiesFront() {
            return videoQualitiesFront;
        }
    }

}
