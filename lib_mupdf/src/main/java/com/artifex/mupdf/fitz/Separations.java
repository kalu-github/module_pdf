package com.artifex.mupdf.fitz;

import android.support.annotation.Keep;

@Keep
public class Separations {
    public static final int SEPARATION_COMPOSITE = 0;
    public static final int SEPARATION_SPOT = 1;
    public static final int SEPARATION_DISABLED = 2;

    static {
        Context.init();
    }

    private long pointer;

    protected Separations(long p) {
        pointer = p;
    }

    protected native void finalize();

    public void destroy() {
        finalize();
        pointer = 0;
    }

    public native int getNumberOfSeparations();

    public native Separation getSeparation(int separation);

    public native boolean areSeparationsControllable();

    public native int getSeparationBehavior(int separation);

    public native void setSeparationBehavior(int separation, int behavior);
}
