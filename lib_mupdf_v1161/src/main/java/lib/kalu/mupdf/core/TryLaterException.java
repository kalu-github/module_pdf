package lib.kalu.mupdf.core;


import androidx.annotation.Keep;

@Keep
public class TryLaterException extends RuntimeException {
    TryLaterException(String message) {
        super(message);
    }
}
