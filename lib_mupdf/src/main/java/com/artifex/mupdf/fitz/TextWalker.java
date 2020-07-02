package com.artifex.mupdf.fitz;

import android.support.annotation.Keep;

@Keep
public interface TextWalker {
    void showGlyph(Font font, Matrix trm, int glyph, int unicode, boolean wmode);
}
