package lib.kalu.pdfium.android.scroll;

import lib.kalu.pdfium.android.PDocView;

public interface ScrollHandle {

    /**
     * Used to move the handle, called internally by PDFView
     *
     * @param position current scroll ratio between 0 and 1
     */
    void setScroll(float position);

    /**
     * Method called by PDFView after setting scroll handle.
     * Do not call this method manually.
     * For usage sample see {@link DefaultScrollHandle}
     *
     * @param pDocView PDFView instance
     */
    void setupLayout(PDocView pDocView);

    /**
     * Method called by PDFView when handle should be removed from layout
     * Do not call this method manually.
     */
    void destroyLayout();

    /**
     * Set page number displayed on handle
     *
     * @param pageNum page number
     */
    void setPageNum(int pageNum);

    /**
     * Get handle visibility
     *
     * @return true if handle is visible, false otherwise
     */
    boolean shown();

    /**
     * Show handle
     */
    void show();

    /**
     * Hide handle immediately
     */
    void hide();

    /**
     * Hide handle after some time (defined by implementation)
     */
    void hideDelayed();
}
