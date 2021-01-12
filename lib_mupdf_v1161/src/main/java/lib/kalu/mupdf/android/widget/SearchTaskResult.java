package lib.kalu.mupdf.android.widget;

import lib.kalu.mupdf.core.Quad;

public class SearchTaskResult {
	public final String txt;
	public final int pageNumber;
	public final Quad searchBoxes[];
	static private SearchTaskResult singleton;

	SearchTaskResult(String _txt, int _pageNumber, Quad _searchBoxes[]) {
		txt = _txt;
		pageNumber = _pageNumber;
		searchBoxes = _searchBoxes;
	}

	static public SearchTaskResult get() {
		return singleton;
	}

	static public void set(SearchTaskResult r) {
		singleton = r;
	}
}
