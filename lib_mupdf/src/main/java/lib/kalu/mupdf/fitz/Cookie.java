package lib.kalu.mupdf.fitz;

public class Cookie
{
	static {
		Context.init();
	}

	private long pointer;

	protected native void finalize();

	public void destroy() {
		finalize();
	}

	private native long newNative();

	public Cookie() {
		pointer = newNative();
	}

	public native void abort();
}
