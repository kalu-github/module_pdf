package lib.kalu.mupdf.core;

import android.support.annotation.Keep;

@Keep
public final class DisplayListDevice extends NativeDevice {
    public DisplayListDevice(DisplayList list) {
        super(newNative(list));
    }

    private static native long newNative(DisplayList list);
}
