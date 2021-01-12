package lib.kalu.mupdf.core;

import androidx.annotation.Keep;

@Keep
public final class DisplayListDevice extends NativeDevice {
    public DisplayListDevice(DisplayList list) {
        super(newNative(list));
    }

    private static native long newNative(DisplayList list);
}
