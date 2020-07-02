package lib.kalu.mupdf.core;

import android.support.annotation.Keep;

@Keep
public class Link {
    public Rect bounds;
    public int page;
    public String uri;

    public Link(Rect bounds, int page, String uri) {
        this.bounds = bounds;
        this.page = page;
        this.uri = uri;
    }

    public String toString() {
        return "Link(b=" + bounds + ",page=" + page + ",uri=" + uri + ")";
    }
}
