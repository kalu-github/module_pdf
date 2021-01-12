package lib.kalu.mupdf.core;

// This class handles the loading of the MuPDF shared library, together
// with the ThreadLocal magic to get the required context.
//
// The only publicly accessible method here is Context.setStoreSize, which
// sets the store size to use. This must be called before any other MuPDF
// function.

import androidx.annotation.Keep;

@Keep
public class Context {
    // Make sure to initialize inited before calling
    // init() from the static block below.
    private static boolean inited = false;

    static {
        init();
    }

    private static native int initNative();

    public static void init() {
        if (!inited) {
            inited = true;
            System.loadLibrary("mupdf-android");
            if (initNative() < 0)
                throw new RuntimeException("cannot initialize mupdf-android library");
        }
    }

// FIXME: We should support the store size being changed dynamically.
// This requires changes within the MuPDF core.
//public native static void setStoreSize(long newSize);
}
