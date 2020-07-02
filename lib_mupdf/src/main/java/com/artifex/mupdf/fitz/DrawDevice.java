package com.artifex.mupdf.fitz;

import android.support.annotation.Keep;

@Keep
public final class DrawDevice extends NativeDevice {
    static {
        Context.init();
    }

    public DrawDevice(Pixmap pixmap) {
        super(newNative(pixmap));
    }

    private static native long newNative(Pixmap pixmap);
}
