package lib.kalu.mupdf.core;


import android.support.annotation.Keep;

@Keep
public class TryLaterException extends RuntimeException {
    TryLaterException(String message) {
        super(message);
    }
}
