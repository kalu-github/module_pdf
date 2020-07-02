package com.artifex.mupdf.fitz;


import android.support.annotation.Keep;

@Keep
public class TryLaterException extends RuntimeException {
    TryLaterException(String message) {
        super(message);
    }
}
