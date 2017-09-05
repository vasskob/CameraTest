package com.example.vasskob.mycamera.utils;

import android.hardware.Camera;

import java.util.ArrayList;
import java.util.List;

public class PhotoSizeMapper implements Mapper<Camera.Size, PictureSize> {

    List<PictureSize> mapList(List<Camera.Size> sizes) {
        List<PictureSize> photoSizes = new ArrayList<>();
        for (Camera.Size size : sizes) {
            photoSizes.add(map(size));
        }
        return photoSizes;
    }

    @Override
    public PictureSize map(Camera.Size data) {
        return new PictureSize(data.width, data.height);
    }
}
