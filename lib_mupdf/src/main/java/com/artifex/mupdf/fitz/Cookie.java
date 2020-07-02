package com.artifex.mupdf.fitz;

import android.support.annotation.Keep;

@Keep
public class Cookie {
    static {
        Context.init();
    }

    private long pointer;

    public Cookie() {
        pointer = newNative();
    }

    protected native void finalize();

    public void destroy() {
        finalize();
        pointer = 0;
    }

    private native long newNative();

    public native void abort();
}
