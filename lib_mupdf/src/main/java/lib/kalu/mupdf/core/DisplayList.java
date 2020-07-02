package lib.kalu.mupdf.core;

import android.support.annotation.Keep;

@Keep
public class DisplayList {
    static {
        Context.init();
    }

    private long pointer;

    public DisplayList(Rect mediabox) {
        pointer = newNative(mediabox);
    }

    private DisplayList(long p) {
        pointer = p;
    }

    protected native void finalize();

    public void destroy() {
        finalize();
        pointer = 0;
    }

    private native long newNative(Rect mediabox);

    public native Pixmap toPixmap(Matrix ctm, ColorSpace colorspace, boolean alpha);

    public native StructuredText toStructuredText(String options);

    public StructuredText toStructuredText() {
        return toStructuredText(null);
    }

    public native Quad[] search(String needle);

    public native void run(Device dev, Matrix ctm, Rect scissor, Cookie cookie);

    public void run(Device dev, Matrix ctm, Cookie cookie) {
        run(dev, ctm, null, cookie);
    }
}
