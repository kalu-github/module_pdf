package com.artifex.mupdf.fitz;

import android.support.annotation.Keep;

@Keep
public class Shade {
    static {
        Context.init();
    }

    private long pointer;

    private Shade(long p) {
        pointer = p;
    }

    protected native void finalize();

    public void destroy() {
        finalize();
        pointer = 0;
    }
}
