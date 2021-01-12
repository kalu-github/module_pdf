package lib.kalu.mupdf.core;

import androidx.annotation.Keep;

@Keep
public class Font {
    public static final int LATIN = 0;
    public static final int GREEK = 1;
    public static final int CYRILLIC = 2;
    public static final int ADOBE_CNS = 0;
    public static final int ADOBE_GB = 1;
    public static final int ADOBE_JAPAN = 2;
    public static final int ADOBE_KOREA = 3;

    static {
        Context.init();
    }

    private long pointer;

    private Font(long p) {
        pointer = p;
    }

    public Font(String name, int index) {
        pointer = newNative(name, index);
    }

    public Font(String name) {
        pointer = newNative(name, 0);
    }

    protected native void finalize();

    public void destroy() {
        finalize();
        pointer = 0;
    }

    private native long newNative(String name, int index);

    public native String getName();

    public native int encodeCharacter(int unicode);

    public native float advanceGlyph(int glyph, boolean wmode);

    public float advanceGlyph(int glyph) {
        return advanceGlyph(glyph, false);
    }

    public String toString() {
        return "Font(" + getName() + ")";
    }
}
