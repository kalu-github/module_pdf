package com.artifex.mupdf.fitz;

import android.support.annotation.Keep;

import java.io.IOException;

@Keep
public interface SeekableInputStream extends SeekableStream {
    /* Behaves like java.io.InputStream.read */
    int read(byte[] b) throws IOException;
}
