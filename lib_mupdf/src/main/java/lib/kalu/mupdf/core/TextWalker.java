package lib.kalu.mupdf.core;

import android.support.annotation.Keep;

@Keep
public interface TextWalker {
    void showGlyph(Font font, Matrix trm, int glyph, int unicode, boolean wmode);
}
