package lib.kalu.mupdf.fitz;

public class Shade
{
	static {
		Context.init();
	}

	private long pointer;

	protected native void finalize();

	public void destroy() {
		finalize();
	}

	private Shade(long p) {
		pointer = p;
	}
}
