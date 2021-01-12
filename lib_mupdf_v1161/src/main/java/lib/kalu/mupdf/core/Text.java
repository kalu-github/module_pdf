package lib.kalu.mupdf.core;

import androidx.annotation.Keep;

@Keep
public class Text implements TextWalker {
    static {
        Context.init();
    }

    private long pointer;

    private Text(long p) {
        pointer = p;
    }

    public Text() {
        pointer = newNative();
    }

    protected native void finalize();

    public void destroy() {
        finalize();
        pointer = 0;
    }

    private native long newNative();

    public native void showGlyph(Font font, Matrix trm, int glyph, int unicode, boolean wmode);

    public native void showString(Font font, Matrix trm, String str, boolean wmode);

    public native Rect getBounds(StrokeState stroke, Matrix ctm);

    public void showGlyph(Font font, Matrix trm, int glyph, int unicode) {
        showGlyph(font, trm, glyph, unicode, false);
    }

    public void showString(Font font, Matrix trm, String str) {
        showString(font, trm, str, false);
    }

    public native void walk(TextWalker walker);
}
