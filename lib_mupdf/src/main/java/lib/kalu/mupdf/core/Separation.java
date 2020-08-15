package lib.kalu.mupdf.core;

import android.support.annotation.Keep;

@Keep
public class Separation {
    public String name;
    public int bgra;
    public int cmyk;

    public Separation(String name, int bgra, int cmyk) {
        this.name = name;
        this.bgra = bgra;
        this.cmyk = cmyk;
    }
}