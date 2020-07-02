package com.artifex.mupdf.fitz;

import android.support.annotation.Keep;

import java.io.IOException;

@Keep
public interface SeekableOutputStream extends SeekableStream {
    /* Behaves like java.io.OutputStream.write */
    void write(byte[] b, int off, int len) throws IOException;
}
