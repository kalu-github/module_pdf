package lib.kalu.mupdf.core;

import androidx.annotation.Keep;

@Keep
public class PDFGraftMap {
    static {
        Context.init();
    }

    private long pointer;

    private PDFGraftMap(long p) {
        pointer = p;
    }

    protected native void finalize();

    public void destroy() {
        finalize();
        pointer = 0;
    }

    public native PDFObject graftObject(PDFObject obj);
}
