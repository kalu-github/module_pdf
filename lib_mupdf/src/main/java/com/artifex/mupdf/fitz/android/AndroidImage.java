package com.artifex.mupdf.fitz.android;

import android.graphics.Bitmap;
import android.support.annotation.Keep;

import com.artifex.mupdf.fitz.Context;
import com.artifex.mupdf.fitz.Image;

@Keep
public final class AndroidImage extends Image {
    static {
        Context.init();
    }

    public AndroidImage(Bitmap bitmap, AndroidImage mask) {
        super(0);
        pointer = newAndroidImageFromBitmap(bitmap, mask.pointer);
    }

    private native long newAndroidImageFromBitmap(Bitmap bitmap, long mask);
}
