package lib.kalu.mupdf.core;

import android.graphics.Bitmap;
import android.support.annotation.Keep;

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
