package lib.kalu.mupdf.core;

import androidx.annotation.Keep;

@Keep
public class DocumentWriter {
    static {
        Context.init();
    }

    private long pointer;

    public DocumentWriter(String filename, String format, String options) {
        pointer = newNativeDocumentWriter(filename, format, options);
    }

    protected native void finalize();

    public void destroy() {
        finalize();
        pointer = 0;
    }

    private native long newNativeDocumentWriter(String filename, String format, String options);

    public native Device beginPage(Rect mediabox);

    public native void endPage();

    public native void close();
}
