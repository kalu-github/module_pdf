package lib.kalu.mupdf.core;

import android.support.annotation.Keep;

@Keep
public class PDFPage extends Page {
    static {
        Context.init();
    }

    private PDFWidget[] widgets;

    private PDFPage(long p) {
        super(p);
    }

    public native PDFAnnotation[] getAnnotations();

    public native PDFAnnotation createAnnotation(int subtype);

    public native void deleteAnnotation(PDFAnnotation annot);

    public native boolean applyRedactions();

    public native boolean update();

    private native PDFWidget[] getWidgetsNative();

    public PDFWidget[] getWidgets() {
        if (widgets == null)
            widgets = getWidgetsNative();
        return widgets;
    }

    public PDFWidget activateWidgetAt(float pageX, float pageY) {
        for (PDFWidget widget : getWidgets()) {
            if (widget.getBounds().contains(pageX, pageY)) {
                widget.eventEnter();
                widget.eventDown();
                widget.eventFocus();
                widget.eventUp();
                widget.eventExit();
                widget.eventBlur();
                return widget;
            }
        }
        return null;
    }
}
