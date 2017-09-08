package com.example.vasskob.mycamera.utils;

import android.annotation.TargetApi;

import java.io.Serializable;

import javax.annotation.Nullable;

import static com.example.vasskob.mycamera.utils.CameraUtils.FHD;
import static com.example.vasskob.mycamera.utils.CameraUtils.FOR_4K_UHD;
import static com.example.vasskob.mycamera.utils.CameraUtils.HD;
import static com.example.vasskob.mycamera.utils.CameraUtils.SD;
import static com.example.vasskob.mycamera.utils.CameraUtils.UHD;

public class PictureSize implements Comparable<PictureSize>, Serializable {

    public static final int MEGA_PIXEL = 1000000;
    private final int height;
    private final int width;

    public PictureSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public String toString() {
        return this.width + "x" + this.height;
    }

    public float resolution() {
        return (float) (this.width * this.height) / MEGA_PIXEL;
    }

    public float aspectRatio() {
        return ((float) this.width) / ((float) this.height);
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof PictureSize)) {
            return false;
        }
        PictureSize otherSize = (PictureSize) other;
        if (otherSize.width == this.width && otherSize.height == this.height) {
            z = true;
        }
        return z;
    }

    @TargetApi(21)
    public static PictureSize[] convert(@Nullable android.util.Size[] sizes) {
        if (sizes == null) {
            return new PictureSize[0];
        }
        PictureSize[] converted = new PictureSize[sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            converted[i] = new PictureSize(sizes[i].getWidth(), sizes[i].getHeight());
        }
        return converted;
    }

    private boolean isLandscape() {
        return this.width >= this.height;
    }

    private boolean isPortrait() {
        return this.height >= this.width;
    }

    public int compareTo(PictureSize size) {
        return Float.compare(resolution(), size.resolution());
    }

    public String getVideoLabel() {
        String label = CameraUtils.UNKNOWN;
        if (width == 3840 && height == 2160) {
            label = FOR_4K_UHD;
        }
        if (width < 3840 && width > 2880 && height >= 1680) {
            label = UHD;
        }
        if (width == 1920 && height == 1080) {
            label = FHD;
        }
        if (width == 1280 && height == 720) {
            label = HD;
        }
        if (width == 640 && height == 480) {
            label = SD;
        }
        return label;
    }
}
