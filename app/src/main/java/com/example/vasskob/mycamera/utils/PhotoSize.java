package com.example.vasskob.mycamera.utils;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PhotoSize implements Comparable<PhotoSize> {

    private final int height;
    private final int width;

    public PhotoSize(Point point) {
        this.width = point.x;
        this.height = point.y;
    }

    @TargetApi(21)
    public PhotoSize(android.util.Size size) {
        this.width = size.getWidth();
        this.height = size.getHeight();
    }

    public PhotoSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public static PhotoSize of(Rect rectangle) {
        return new PhotoSize(rectangle.width(), rectangle.height());
    }

    public static PhotoSize of(RectF rectangle) {
        Rect rect = new Rect();
        rectangle.round(rect);
        return of(rect);
    }

    public static String toSettingString(@Nonnull PhotoSize size) {
        return size.width() + "x" + size.height();
    }

    @Nullable
    public static PhotoSize fromSettingString(String sizeSettingString) {
        if (sizeSettingString == null) {
            return null;
        }
        String[] parts = sizeSettingString.split("x");
        if (parts.length != 2) {
            return null;
        }
        try {
            return new PhotoSize(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @TargetApi(21)
    public android.util.Size asSize21() {
        return new android.util.Size(this.width, this.height);
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

    public PhotoSize transpose() {
        return new PhotoSize(this.height, this.width);
    }

    public PhotoSize asLandscape() {
        if (isLandscape()) {
            return this;
        }
        return transpose();
    }

    public PhotoSize asPortrait() {
        if (isPortrait()) {
            return this;
        }
        return transpose();
    }

    public long area() {
        return (long) (this.width * this.height);
    }

    public float aspectRatio() {
        return ((float) this.width) / ((float) this.height);
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof PhotoSize)) {
            return false;
        }
        PhotoSize otherSize = (PhotoSize) other;
        if (otherSize.width == this.width && otherSize.height == this.height) {
            z = true;
        }
        return z;
    }

    @TargetApi(21)
    public static PhotoSize[] convert(@Nullable android.util.Size[] sizes) {
        if (sizes == null) {
            return new PhotoSize[0];
        }
        PhotoSize[] converted = new PhotoSize[sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            converted[i] = new PhotoSize(sizes[i].getWidth(), sizes[i].getHeight());
        }
        return converted;
    }

    private boolean isLandscape() {
        return this.width >= this.height;
    }

    private boolean isPortrait() {
        return this.height >= this.width;
    }


    public Rect asRect() {
        return new Rect(0, 0, this.width, this.height);
    }

    public int compareTo(PhotoSize size) {
        return Long.compare(area(), size.area());
    }
}
