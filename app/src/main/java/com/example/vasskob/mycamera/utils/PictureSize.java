package com.example.vasskob.mycamera.utils;

import android.annotation.TargetApi;

import java.io.Serializable;

import javax.annotation.Nullable;

public class PictureSize implements Comparable<PictureSize>, Serializable {

    public static final int MEGA_PIXEL = 1000000;
    private final int height;
    private final int width;

    public PictureSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
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
}
