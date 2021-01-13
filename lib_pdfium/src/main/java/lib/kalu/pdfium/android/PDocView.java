/**
 * Copyright 2016 Bartosz Schiller
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lib.kalu.pdfium.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import lib.kalu.pdfium.android.exception.PageRenderingException;
import lib.kalu.pdfium.android.link.LinkHandler;
import lib.kalu.pdfium.android.listener.Callbacks;
import lib.kalu.pdfium.android.listener.OnDrawListener;
import lib.kalu.pdfium.android.listener.OnErrorListener;
import lib.kalu.pdfium.android.listener.OnLoadCompleteListener;
import lib.kalu.pdfium.android.listener.OnLongPressListener;
import lib.kalu.pdfium.android.listener.OnPageChangeListener;
import lib.kalu.pdfium.android.listener.OnPageErrorListener;
import lib.kalu.pdfium.android.listener.OnPageScrollListener;
import lib.kalu.pdfium.android.listener.OnRenderListener;
import lib.kalu.pdfium.android.listener.OnTapListener;
import lib.kalu.pdfium.android.model.PagePart;
import lib.kalu.pdfium.android.scroll.ScrollHandle;
import lib.kalu.pdfium.android.source.AssetSource;
import lib.kalu.pdfium.android.source.ByteArraySource;
import lib.kalu.pdfium.android.source.DocumentSource;
import lib.kalu.pdfium.android.source.FileSource;
import lib.kalu.pdfium.android.source.InputStreamSource;
import lib.kalu.pdfium.android.source.UriSource;
import lib.kalu.pdfium.android.util.Constants;
import lib.kalu.pdfium.android.util.FitPolicy;
import lib.kalu.pdfium.android.util.MathUtils;
import lib.kalu.pdfium.android.util.SnapEdge;
import lib.kalu.pdfium.android.util.Util;
import lib.kalu.pdfium.PdfDocument;
import lib.kalu.pdfium.PdfiumCore;
import lib.kalu.pdfium.util.Size;
import lib.kalu.pdfium.util.SizeF;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * It supports animations, zoom, cache, and swipe.
 * <p>
 * To fully understand this class you must know its principles :
 * - The PDF document is seen as if we always want to draw all the pages.
 * - The thing is that we only draw the visible parts.
 * - All parts are the same size, this is because we can't interrupt a native page rendering,
 * so we need these renderings to be as fast as possible, and be able to interrupt them
 * as soon as we can.
 * - The parts are loaded when the current offset or the current zoom level changes
 * <p>
 * Important :
 * - DocumentPage = A page of the PDF document.
 * - UserPage = A page as defined by the user.
 * By default, they're the same. But the user can change the pages order
 * using {@link #load(DocumentSource, String)}. In this
 * particular case, a userPage of 5 can refer to a documentPage of 17.
 */
public class PDocView extends RelativeLayout {
    private static final String TAG = PDocView.class.getSimpleName();
    public static final float DEFAULT_MAX_SCALE = 3.0f;
    public static final float DEFAULT_MID_SCALE = 1.75f;
    public static final float DEFAULT_MIN_SCALE = 1.0f;
	
	/** Pdfium core for loading and rendering PDFs */
	private PdfiumCore pdfiumCore;
	
	PdfFile pdfFile;
	
    /** Rendered parts go to the cache manager */
    CacheManager cacheManager;

    /** Animation manager manage all offset and zoom animation */
    private AnimationManager animationManager;

    /** Drag manager manage all touch events */
    private DragPinchManager dragPinchManager;

    /** The index of the current sequence */
    private int currentPage;

    /** If you picture all the pages side by side in their optimal width,
     * and taking into account the zoom level, the current offset is the
     * position of the left border of the screen in this big picture */
    private float currentXOffset = 0;

    /** If you picture all the pages side by side in their optimal width,
     * and taking into account the zoom level, the current offset is the
     * position of the left border of the screen in this big picture */
    private float currentYOffset = 0;

    /** The zoom level, always >= 1 */
    private float zoom = 1f;

    /** True if the PDFView has been recycled */
    private boolean recycled = true;

    /** Current state of the view */
    private State state = State.DEFAULT;

    /** Async task used during the loading phase to decode a PDF document */
    private DecodingAsyncTask decodingAsyncTask;

    /** The thread {@link #renderingHandler} will run on */
    private HandlerThread renderingHandlerThread;
    /** Handler always waiting in the background and rendering tasks */
    RenderingHandler renderingHandler;
    private PagesLoader pagesLoader;
    Callbacks callbacks = new Callbacks();
    /** Paint object for drawing */
    private Paint paint;
    /** Paint object for drawing debug stuff */
    private Paint debugPaint;
    /** Policy for fitting pages to screen */
    private FitPolicy pageFitPolicy = FitPolicy.WIDTH;
    private boolean fitEachPage = false;
    private int defaultPage = 0;
    /** True if should scroll through pages vertically instead of horizontally */
    private boolean swipeVertical = true;
    private boolean enableSwipe = true;
    private boolean doubletapEnabled = true;
    private boolean nightMode = false;
    private boolean pageSnap = false;
	
	private float minZoom = DEFAULT_MIN_SCALE;
	private float midZoom = DEFAULT_MID_SCALE;
	private float maxZoom = DEFAULT_MAX_SCALE;
	/** START - scrolling in first page direction
	 * END - scrolling in last page direction
	 * NONE - not scrolling */
	enum ScrollDir { NONE, START, END }
	private ScrollDir scrollDir = ScrollDir.NONE;
    private ScrollHandle scrollHandle;
    private boolean isScrollHandleInit = false;

    ScrollHandle getScrollHandle() {
        return scrollHandle;
    }

    /** ARGB_8888 or RGB_565 */
    public boolean bestQuality = false;
	
	public boolean annotationRendering = false;
	
	public boolean renderDuringScale = false;

    /** Antialiasing and bitmap filtering */
	public boolean enableAntialiasing = true;
    private PaintFlagsDrawFilter antialiasFilter =
            new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    /** Spacing between pages, in px */
    private int spacingPx = 0;

    /** Add dynamic spacing to fit each page separately on the screen. */
    private boolean autoSpacing = false;

    /** Fling a single page at a time */
    private boolean pageFling = false;

    /** Pages numbers used when calling onDrawAllListener */
    private List<Integer> onDrawPagesNums = new ArrayList<>(10);

    /** Holds info whether view has been added to layout and has width and height */
    private boolean hasSize = false;

    /** Holds last used Configurator that should be loaded when view has size */
    private Builder waitingDocumentConfigurator;

    /** Construct the initial view */
    public PDocView(Context context, AttributeSet set) {
        super(context, set);
        renderingHandlerThread = new HandlerThread("PDF renderer");
        if (isInEditMode()) return;
		pdfiumCore = new PdfiumCore(context);
		cacheManager = new CacheManager();
		animationManager = new AnimationManager(this);
		dragPinchManager = new DragPinchManager(this, animationManager);
		pagesLoader = new PagesLoader(this);
		paint = new Paint();
		debugPaint = new Paint();
		debugPaint.setStyle(Style.STROKE);
        setWillNotDraw(false);
    }

    private void load(DocumentSource docSource, String password) {
        if (!recycled) {
            throw new IllegalStateException("Don't call load on a PDF View without recycling it first.");
        }
        recycled = false;
        // Start decoding document
        decodingAsyncTask = new DecodingAsyncTask(docSource, password, this, pdfiumCore);
        decodingAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /** Go to the given page.
     * @param page Page index. */
    public void jumpTo(int page, boolean withAnimation) {
        if (pdfFile == null) {
            return;
        }
        page = pdfFile.determineValidPageNumberFrom(page);
        float offset = page == 0 ? 0 : -pdfFile.getPageOffset(page, zoom);
        if (swipeVertical) {
            if (withAnimation) {
                animationManager.startYAnimation(currentYOffset, offset);
            } else {
                moveTo(currentXOffset, offset);
            }
        } else {
            if (withAnimation) {
                animationManager.startXAnimation(currentXOffset, offset);
            } else {
                moveTo(offset, currentYOffset);
            }
        }
        showPage(page);
    }

    public void jumpTo(int page) {
        jumpTo(page, false);
    }

    void showPage(int pageNb) {
        if (recycled) {
            return;
        }
        // Check the page number and makes the
        // difference between UserPages and DocumentPages
        pageNb = pdfFile.determineValidPageNumberFrom(pageNb);
        currentPage = pageNb;
        
        loadPages();
        
        if (scrollHandle != null && !documentFitsView()) {
            scrollHandle.setPageNum(currentPage + 1);
        }
        callbacks.callOnPageChange(currentPage, pdfFile.getPagesCount());
    }

    /**
     * Get current position as ratio of document length to visible area.
     * 0 means that document start is visible, 1 that document end is visible
     *
     * @return offset between 0 and 1
     */
    public float getPositionOffset() {
        float offset;
        if (swipeVertical) {
            offset = -currentYOffset / (pdfFile.getDocLen(zoom) - getHeight());
        } else {
            offset = -currentXOffset / (pdfFile.getDocLen(zoom) - getWidth());
        }
        return MathUtils.limit(offset, 0, 1);
    }

    /**
     * @param progress   must be between 0 and 1
     * @param moveHandle whether to move scroll handle
     * @see PDocView#getPositionOffset()
     */
    public void setPositionOffset(float progress, boolean moveHandle) {
        if (swipeVertical) {
            moveTo(currentXOffset, (-pdfFile.getDocLen(zoom) + getHeight()) * progress, moveHandle);
        } else {
            moveTo((-pdfFile.getDocLen(zoom) + getWidth()) * progress, currentYOffset, moveHandle);
        }
        loadPageByOffset();
    }

    public void setPositionOffset(float progress) {
        setPositionOffset(progress, true);
    }

    public void stopFling() {
        animationManager.stopFling();
    }

    public int getPageCount() {
        if (pdfFile == null) {
            return 0;
        }
        return pdfFile.getPagesCount();
    }

    public void setSwipeEnabled(boolean enableSwipe) {
        this.enableSwipe = enableSwipe;
    }

    public void setNightMode(boolean nightMode) {
        this.nightMode = nightMode;
        if (nightMode) {
            ColorMatrix colorMatrixInverted =
                    new ColorMatrix(new float[]{
                            -1, 0, 0, 0, 255,
                            0, -1, 0, 0, 255,
                            0, 0, -1, 0, 255,
                            0, 0, 0, 1, 0});

            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrixInverted);
            paint.setColorFilter(filter);
        } else {
            paint.setColorFilter(null);
        }
    }

    public void enableDoubletap(boolean enableDoubletap) {
        this.doubletapEnabled = enableDoubletap;
    }

    boolean isDoubletapEnabled() {
        return doubletapEnabled;
    }

    void onPageError(PageRenderingException ex) {
        if (!callbacks.callOnPageError(ex.getPage(), ex.getCause())) {
            Log.e(TAG, "Cannot open page " + ex.getPage(), ex.getCause());
        }
    }

    public void recycle() {
        waitingDocumentConfigurator = null;

        animationManager.stopAll();
        dragPinchManager.disable();

        // Stop tasks
        if (renderingHandler != null) {
            renderingHandler.stop();
            renderingHandler.removeMessages(RenderingHandler.MSG_RENDER_TASK);
        }
        if (decodingAsyncTask != null) {
            decodingAsyncTask.cancel(true);
        }

        // Clear caches
        cacheManager.recycle();

        if (scrollHandle != null && isScrollHandleInit) {
            scrollHandle.destroyLayout();
        }

        if (pdfFile != null) {
            pdfFile.dispose();
            pdfFile = null;
        }

        renderingHandler = null;
        scrollHandle = null;
        isScrollHandleInit = false;
        currentXOffset = currentYOffset = 0;
        zoom = 1f;
        recycled = true;
        callbacks = new Callbacks();
        state = State.DEFAULT;
    }

    public boolean isRecycled() {
        return recycled;
    }

    /** Handle fling animation */
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (isInEditMode()) {
            return;
        }
        animationManager.computeFling();
    }

    @Override
    protected void onDetachedFromWindow() {
        recycle();
        if (renderingHandlerThread != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                renderingHandlerThread.quitSafely();
            } else {
                renderingHandlerThread.quit();
            }
            renderingHandlerThread = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        hasSize = true;
        if (waitingDocumentConfigurator != null) {
            waitingDocumentConfigurator.load();
        }
        if (isInEditMode() || state != State.SHOWN) {
            return;
        }

        // calculates the position of the point which in the center of view relative to big strip
        float centerPointInStripXOffset = -currentXOffset + oldw * 0.5f;
        float centerPointInStripYOffset = -currentYOffset + oldh * 0.5f;

        float relativeCenterPointInStripXOffset;
        float relativeCenterPointInStripYOffset;

        if (swipeVertical){
            relativeCenterPointInStripXOffset = centerPointInStripXOffset / pdfFile.getMaxPageWidth();
            relativeCenterPointInStripYOffset = centerPointInStripYOffset / pdfFile.getDocLen(zoom);
        }else {
            relativeCenterPointInStripXOffset = centerPointInStripXOffset / pdfFile.getDocLen(zoom);
            relativeCenterPointInStripYOffset = centerPointInStripYOffset / pdfFile.getMaxPageHeight();
        }

        animationManager.stopAll();
        pdfFile.recalculatePageSizes(new Size(w, h));

        if (swipeVertical) {
            currentXOffset = -relativeCenterPointInStripXOffset * pdfFile.getMaxPageWidth() + w * 0.5f;
            currentYOffset = -relativeCenterPointInStripYOffset * pdfFile.getDocLen(zoom) + h * 0.5f ;
        }else {
            currentXOffset = -relativeCenterPointInStripXOffset * pdfFile.getDocLen(zoom) + w * 0.5f;
            currentYOffset = -relativeCenterPointInStripYOffset * pdfFile.getMaxPageHeight() + h * 0.5f;
        }
        moveTo(currentXOffset,currentYOffset);
        loadPageByOffset();
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        if (pdfFile == null) {
            return true;
        }

        if (swipeVertical) {
            if (direction < 0 && currentXOffset < 0) {
                return true;
            } else if (direction > 0 && currentXOffset + toCurrentScale(pdfFile.getMaxPageWidth()) > getWidth()) {
                return true;
            }
        } else {
            if (direction < 0 && currentXOffset < 0) {
                return true;
            } else if (direction > 0 && currentXOffset + pdfFile.getDocLen(zoom) > getWidth()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (pdfFile == null) {
            return true;
        }

        if (swipeVertical) {
            if (direction < 0 && currentYOffset < 0) {
                return true;
            } else if (direction > 0 && currentYOffset + pdfFile.getDocLen(zoom) > getHeight()) {
                return true;
            }
        } else {
            if (direction < 0 && currentYOffset < 0) {
                return true;
            } else if (direction > 0 && currentYOffset + toCurrentScale(pdfFile.getMaxPageHeight()) > getHeight()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            return;
        }
        // As I said in this class javadoc, we can think of this canvas as a huge
        // strip on which we draw all the images. We actually only draw the rendered
        // parts, of course, but we render them in the place they belong in this huge
        // strip.

        // That's where Canvas.translate(x, y) becomes very helpful.
        // This is the situation :
        //  _______________________________________________
        // |   			 |					 			   |
        // | the actual  |					The big strip  |
        // |	canvas	 | 								   |
        // |_____________|								   |
        // |_______________________________________________|
        //
        // If the rendered part is on the bottom right corner of the strip
        // we can draw it but we won't see it because the canvas is not big enough.

        // But if we call translate(-X, -Y) on the canvas just before drawing the object :
        //  _______________________________________________
        // |   			  					  _____________|
        // |   The big strip     			 |			   |
        // |		    					 |	the actual |
        // |								 |	canvas	   |
        // |_________________________________|_____________|
        //
        // The object will be on the canvas.
        // This technique is massively used in this method, and allows
        // abstraction of the screen position when rendering the parts.

        // Draws background

        if (enableAntialiasing) {
            canvas.setDrawFilter(antialiasFilter);
        }

        Drawable bg = getBackground();
        if (bg == null) {
            canvas.drawColor(nightMode ? Color.BLACK : Color.WHITE);
        } else {
            bg.draw(canvas);
        }
        if (recycled) {
            return;
        }
        if (state != State.SHOWN) {
            return;
        }
        // Moves the canvas before drawing any element
        float currentXOffset = this.currentXOffset;
        float currentYOffset = this.currentYOffset;
        canvas.translate(currentXOffset, currentYOffset);

        // Draws thumbnails
        for (PagePart part : cacheManager.getThumbnails()) {
            drawPart(canvas, part);
        }

        // Draws parts
        for (PagePart part : cacheManager.getPageParts()) {
            drawPart(canvas, part);
            if (callbacks.getOnDrawAll() != null
                    && !onDrawPagesNums.contains(part.getPage())) {
                onDrawPagesNums.add(part.getPage());
            }
        }
        for (Integer page : onDrawPagesNums) {
            drawWithListener(canvas, page, callbacks.getOnDrawAll());
        }
        onDrawPagesNums.clear();
        drawWithListener(canvas, currentPage, callbacks.getOnDraw());
        // Restores the canvas position
        canvas.translate(-currentXOffset, -currentYOffset);
    }

    private void drawWithListener(Canvas canvas, int page, OnDrawListener listener) {
        if (listener != null) {
            float translateX, translateY;
            if (swipeVertical) {
                translateX = 0;
                translateY = pdfFile.getPageOffset(page, zoom);
            } else {
                translateY = 0;
                translateX = pdfFile.getPageOffset(page, zoom);
            }

            canvas.translate(translateX, translateY);
            SizeF size = pdfFile.getPageSize(page);
            listener.onLayerDrawn(canvas,
                    toCurrentScale(size.getWidth()),
                    toCurrentScale(size.getHeight()),
                    page);

            canvas.translate(-translateX, -translateY);
        }
    }

    /** Draw a given PagePart on the canvas */
    private void drawPart(Canvas canvas, PagePart part) {
        // Can seem strange, but avoid lot of calls
        RectF pageRelativeBounds = part.getPageRelativeBounds();
        Bitmap renderedBitmap = part.getRenderedBitmap();

        if (renderedBitmap.isRecycled()) {
            return;
        }
        // Move to the target page
        float localTranslationX = 0;
        float localTranslationY = 0;
        SizeF size = pdfFile.getPageSize(part.getPage());
        
        if (swipeVertical) {
            localTranslationY = pdfFile.getPageOffset(part.getPage(), zoom);
            float maxWidth = pdfFile.getMaxPageWidth();
            localTranslationX = toCurrentScale(maxWidth - size.getWidth()) / 2;
        } else {
            localTranslationX = pdfFile.getPageOffset(part.getPage(), zoom);
            float maxHeight = pdfFile.getMaxPageHeight();
            localTranslationY = toCurrentScale(maxHeight - size.getHeight()) / 2;
        }
        canvas.translate(localTranslationX, localTranslationY);

        Rect srcRect = new Rect(0, 0, renderedBitmap.getWidth(),
                renderedBitmap.getHeight());

        float offsetX = toCurrentScale(pageRelativeBounds.left * size.getWidth());
        float offsetY = toCurrentScale(pageRelativeBounds.top * size.getHeight());
        float width = toCurrentScale(pageRelativeBounds.width() * size.getWidth());
        float height = toCurrentScale(pageRelativeBounds.height() * size.getHeight());

        // If we use float values for this rectangle, there will be
        // a possible gap between page parts, especially when
        // the zoom level is high.
        RectF dstRect = new RectF((int) offsetX, (int) offsetY,
                (int) (offsetX + width),
                (int) (offsetY + height));

        // Check if bitmap is in the screen
        float translationX = currentXOffset + localTranslationX;
        float translationY = currentYOffset + localTranslationY;
        if (translationX + dstRect.left >= getWidth() || translationX + dstRect.right <= 0 ||
                translationY + dstRect.top >= getHeight() || translationY + dstRect.bottom <= 0) {
            canvas.translate(-localTranslationX, -localTranslationY);
            return;
        }

        canvas.drawBitmap(renderedBitmap, srcRect, dstRect, paint);

        if (Constants.DEBUG_MODE) {
            debugPaint.setColor(part.getPage() % 2 == 0 ? Color.RED : Color.BLUE);
            canvas.drawRect(dstRect, debugPaint);
        }

        // Restore the canvas position
        canvas.translate(-localTranslationX, -localTranslationY);
    }

    /**
     * Load all the parts around the center of the screen,
     * taking into account X and Y offsets, zoom level, and
     * the current page displayed
     */
    public void loadPages() {
        if (pdfFile == null || renderingHandler == null) {
            return;
        }

        // Cancel all current tasks
        renderingHandler.removeMessages(RenderingHandler.MSG_RENDER_TASK);
        cacheManager.makeANewSet();

        pagesLoader.loadPages();
        redraw();
    }

    /** Called when the PDF is loaded */
    void loadComplete(PdfFile pdfFile) {
        state = State.LOADED;
        this.pdfFile = pdfFile;
        if (!renderingHandlerThread.isAlive()) {
            renderingHandlerThread.start();
        }
        renderingHandler = new RenderingHandler(renderingHandlerThread.getLooper(), this);
        renderingHandler.start();
        if (scrollHandle != null) {
            scrollHandle.setupLayout(this);
            isScrollHandleInit = true;
        }
        dragPinchManager.enable();
        callbacks.callOnLoadComplete(pdfFile.getPagesCount());
        jumpTo(defaultPage, false);
    }

    void loadError(Throwable t) {
        state = State.ERROR;
        // store reference, because callbacks will be cleared in recycle() method
        OnErrorListener onErrorListener = callbacks.getOnError();
        recycle();
        invalidate();
        if (onErrorListener != null) {
            onErrorListener.onError(t);
        } else {
            Log.e("PDFView", "load pdf error", t);
        }
    }

    void redraw() {
        invalidate();
    }

    /**
     * Called when a rendering task is over and
     * a PagePart has been freshly created.
     *
     * @param part The created PagePart.
     */
    public void onBitmapRendered(PagePart part) {
        // when it is first rendered part
        if (state == State.LOADED) {
            state = State.SHOWN;
            callbacks.callOnRender(pdfFile.getPagesCount());
        }

        if (part.isThumbnail()) {
            cacheManager.cacheThumbnail(part);
        } else {
            cacheManager.cachePart(part);
        }
        redraw();
    }

    public void moveTo(float offsetX, float offsetY) {
        moveTo(offsetX, offsetY, true);
    }

    /**
     * Move to the given X and Y offsets, but check them ahead of time
     * to be sure not to go outside the the big strip.
     *
     * @param offsetX    The big strip X offset to use as the left border of the screen.
     * @param offsetY    The big strip Y offset to use as the right border of the screen.
     * @param moveHandle whether to move scroll handle or not
     */
    public void moveTo(float offsetX, float offsetY, boolean moveHandle) {
        if (swipeVertical) {
            // Check X offset
            float scaledPageWidth = toCurrentScale(pdfFile.getMaxPageWidth());
            if (scaledPageWidth < getWidth()) {
                offsetX = getWidth() / 2 - scaledPageWidth / 2;
            } else {
                if (offsetX > 0) {
                    offsetX = 0;
                } else if (offsetX + scaledPageWidth < getWidth()) {
                    offsetX = getWidth() - scaledPageWidth;
                }
            }

            // Check Y offset
            float contentHeight = pdfFile.getDocLen(zoom);
            if (contentHeight < getHeight()) { // whole document height visible on screen
                offsetY = (getHeight() - contentHeight) / 2;
            } else {
                if (offsetY > 0) { // top visible
                    offsetY = 0;
                } else if (offsetY + contentHeight < getHeight()) { // bottom visible
                    offsetY = -contentHeight + getHeight();
                }
            }

            if (offsetY < currentYOffset) {
                scrollDir = ScrollDir.END;
            } else if (offsetY > currentYOffset) {
                scrollDir = ScrollDir.START;
            } else {
                scrollDir = ScrollDir.NONE;
            }
        }
        else {
            // Check Y offset
            float scaledPageHeight = toCurrentScale(pdfFile.getMaxPageHeight());
            if (scaledPageHeight < getHeight()) {
                offsetY = getHeight() / 2 - scaledPageHeight / 2;
            } else {
                if (offsetY > 0) {
                    offsetY = 0;
                } else if (offsetY + scaledPageHeight < getHeight()) {
                    offsetY = getHeight() - scaledPageHeight;
                }
            }

            // Check X offset
            float contentWidth = pdfFile.getDocLen(zoom);
            if (contentWidth < getWidth()) { // whole document width visible on screen
                offsetX = (getWidth() - contentWidth) / 2;
            } else {
                if (offsetX > 0) { // left visible
                    offsetX = 0;
                } else if (offsetX + contentWidth < getWidth()) { // right visible
                    offsetX = -contentWidth + getWidth();
                }
            }

            if (offsetX < currentXOffset) {
                scrollDir = ScrollDir.END;
            } else if (offsetX > currentXOffset) {
                scrollDir = ScrollDir.START;
            } else {
                scrollDir = ScrollDir.NONE;
            }
        }

        currentXOffset = offsetX;
        currentYOffset = offsetY;
        float positionOffset = getPositionOffset();

        if (moveHandle && scrollHandle != null && !documentFitsView()) {
            scrollHandle.setScroll(positionOffset);
        }

        callbacks.callOnPageScroll(getCurrentPage(), positionOffset);

        redraw();
    }

    void loadPageByOffset() {
        if (0 == pdfFile.getPagesCount()) {
            return;
        }

        float offset, screenCenter;
        if (swipeVertical) {
            offset = currentYOffset;
            screenCenter = ((float) getHeight()) / 2;
        } else {
            offset = currentXOffset;
            screenCenter = ((float) getWidth()) / 2;
        }

        int page = pdfFile.getPageAtOffset(-(offset - screenCenter), zoom);

        if (page >= 0 && page <= pdfFile.getPagesCount() - 1 && page != getCurrentPage()) {
            showPage(page);
        } else {
            loadPages();
        }
    }

    /**
     * Animate to the nearest snapping position for the current SnapPolicy
     */
    public void performPageSnap() {
        if (!pageSnap || pdfFile == null || pdfFile.getPagesCount() == 0) {
            return;
        }
        int centerPage = findFocusPage(currentXOffset, currentYOffset);
        SnapEdge edge = findSnapEdge(centerPage);
        if (edge == SnapEdge.NONE) {
            return;
        }

        float offset = snapOffsetForPage(centerPage, edge);
        if (swipeVertical) {
            animationManager.startYAnimation(currentYOffset, -offset);
        } else {
            animationManager.startXAnimation(currentXOffset, -offset);
        }
    }

    /**
     * Find the edge to snap to when showing the specified page
     */
    SnapEdge findSnapEdge(int page) {
        if (!pageSnap || page < 0) {
            return SnapEdge.NONE;
        }
        float currentOffset = swipeVertical ? currentYOffset : currentXOffset;
        float offset = -pdfFile.getPageOffset(page, zoom);
        int length = swipeVertical ? getHeight() : getWidth();
        float pageLength = pdfFile.getPageLength(page, zoom);

        if (length >= pageLength) {
            return SnapEdge.CENTER;
        } else if (currentOffset >= offset) {
            return SnapEdge.START;
        } else if (offset - pageLength > currentOffset - length) {
            return SnapEdge.END;
        } else {
            return SnapEdge.NONE;
        }
    }

    /**
     * Get the offset to move to in order to snap to the page
     */
    float snapOffsetForPage(int pageIndex, SnapEdge edge) {
        float offset = pdfFile.getPageOffset(pageIndex, zoom);

        float length = swipeVertical ? getHeight() : getWidth();
        float pageLength = pdfFile.getPageLength(pageIndex, zoom);

        if (edge == SnapEdge.CENTER) {
            offset = offset - length / 2f + pageLength / 2f;
        } else if (edge == SnapEdge.END) {
            offset = offset - length + pageLength;
        }
        return offset;
    }

    int findFocusPage(float xOffset, float yOffset) {
        float currOffset = swipeVertical ? yOffset : xOffset;
        float length = swipeVertical ? getHeight() : getWidth();
        // make sure first and last page can be found
        if (currOffset > -1) {
            return 0;
        } else if (currOffset < -pdfFile.getDocLen(zoom) + length + 1) {
            return pdfFile.getPagesCount() - 1;
        }
        // else find page in center
        float center = currOffset - length / 2f;
        return pdfFile.getPageAtOffset(-center, zoom);
    }

    /**
     * @return true if single page fills the entire screen in the scrolling direction
     */
    public boolean pageFillsScreen() {
        float start = -pdfFile.getPageOffset(currentPage, zoom);
        float end = start - pdfFile.getPageLength(currentPage, zoom);
        if (isSwipeVertical()) {
            return start > currentYOffset && end < currentYOffset - getHeight();
        } else {
            return start > currentXOffset && end < currentXOffset - getWidth();
        }
    }

    /**
     * Move relatively to the current position.
     *
     * @param dx The X difference you want to apply.
     * @param dy The Y difference you want to apply.
     * @see #moveTo(float, float)
     */
    public void moveRelativeTo(float dx, float dy) {
        moveTo(currentXOffset + dx, currentYOffset + dy);
    }

    /**
     * Change the zoom level
     */
    public void zoomTo(float zoom) {
        this.zoom = zoom;
    }

    /**
     * Change the zoom level, relatively to a pivot point.
     * It will call moveTo() to make sure the given point stays
     * in the middle of the screen.
     *
     * @param zoom  The zoom level.
     * @param pivot The point on the screen that should stays.
     */
    public void zoomCenteredTo(float zoom, PointF pivot) {
        float dzoom = zoom / this.zoom;
        zoomTo(zoom);
        float baseX = currentXOffset * dzoom;
        float baseY = currentYOffset * dzoom;
        baseX += (pivot.x - pivot.x * dzoom);
        baseY += (pivot.y - pivot.y * dzoom);
        moveTo(baseX, baseY);
    }

    /**
     * @see #zoomCenteredTo(float, PointF)
     */
    public void zoomCenteredRelativeTo(float dzoom, PointF pivot) {
        zoomCenteredTo(zoom * dzoom, pivot);
    }

    /**
     * Checks if whole document can be displayed on screen, doesn't include zoom
     *
     * @return true if whole document can displayed at once, false otherwise
     */
    public boolean documentFitsView() {
        float len = pdfFile.getDocLen(1);
        if (swipeVertical) {
            return len < getHeight();
        } else {
            return len < getWidth();
        }
    }

    public void fitToWidth(int page) {
        if (state != State.SHOWN) {
            Log.e(TAG, "Cannot fit, document not rendered yet");
            return;
        }
        zoomTo(getWidth() / pdfFile.getPageSize(page).getWidth());
        jumpTo(page);
    }

    public SizeF getPageSize(int pageIndex) {
        if (pdfFile == null) {
            return new SizeF(0, 0);
        }
        return pdfFile.getPageSize(pageIndex);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public float getCurrentXOffset() {
        return currentXOffset;
    }

    public float getCurrentYOffset() {
        return currentYOffset;
    }

    public float toRealScale(float size) {
        return size / zoom;
    }

    public float toCurrentScale(float size) {
        return size * zoom;
    }

    public float getZoom() {
        return zoom;
    }

    public boolean isZooming() {
        return zoom != minZoom;
    }

    private void setDefaultPage(int defaultPage) {
        this.defaultPage = defaultPage;
    }

    public void resetZoom() {
        zoomTo(minZoom);
    }

    public void resetZoomWithAnimation() {
        zoomWithAnimation(minZoom);
    }

    public void zoomWithAnimation(float centerX, float centerY, float scale) {
        animationManager.startZoomAnimation(centerX, centerY, zoom, scale);
    }

    public void zoomWithAnimation(float scale) {
        animationManager.startZoomAnimation(getWidth() / 2, getHeight() / 2, zoom, scale);
    }

    private void setScrollHandle(ScrollHandle scrollHandle) {
        this.scrollHandle = scrollHandle;
    }

    /**
     * Get page number at given offset
     *
     * @param positionOffset scroll offset between 0 and 1
     * @return page number at given offset, starting from 0
     */
    public int getPageAtPositionOffset(float positionOffset) {
        return pdfFile.getPageAtOffset(pdfFile.getDocLen(zoom) * positionOffset, zoom);
    }

    public float getMinZoom() {
        return minZoom;
    }

    public void setMinZoom(float minZoom) {
        this.minZoom = minZoom;
    }

    public float getMidZoom() {
        return midZoom;
    }

    public void setMidZoom(float midZoom) {
        this.midZoom = midZoom;
    }

    public float getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(float maxZoom) {
        this.maxZoom = maxZoom;
    }

    public boolean isSwipeVertical() {
        return swipeVertical;
    }

    public boolean isSwipeEnabled() {
        return enableSwipe;
    }

    private void setSwipeVertical(boolean swipeVertical) {
        this.swipeVertical = swipeVertical;
    }
    
    public void enableRenderDuringScale(boolean renderDuringScale) {
        this.renderDuringScale = renderDuringScale;
    }

    public void enableAntialiasing(boolean enableAntialiasing) {
        this.enableAntialiasing = enableAntialiasing;
    }

    public int getSpacingPx() {
        return spacingPx;
    }

    public boolean isAutoSpacingEnabled() {
        return autoSpacing;
    }

    public void setPageFling(boolean pageFling) {
        this.pageFling = pageFling;
    }

    public boolean isPageFlingEnabled() {
        return pageFling;
    }

    private void setSpacing(int spacingDp) {
        this.spacingPx = Util.getDP(getContext(), spacingDp);
    }

    private void setAutoSpacing(boolean autoSpacing) {
        this.autoSpacing = autoSpacing;
    }

    private void setPageFitPolicy(FitPolicy pageFitPolicy) {
        this.pageFitPolicy = pageFitPolicy;
    }

    public FitPolicy getPageFitPolicy() {
        return pageFitPolicy;
    }

    private void setFitEachPage(boolean fitEachPage) {
        this.fitEachPage = fitEachPage;
    }

    public boolean isFitEachPage() {
        return fitEachPage;
    }

    public boolean isPageSnap() {
        return pageSnap;
    }

    public void setPageSnap(boolean pageSnap) {
        this.pageSnap = pageSnap;
    }

    public boolean doRenderDuringScale() {
        return renderDuringScale;
    }

    /** Returns null if document is not loaded */
    public PdfDocument.Meta getDocumentMeta() {
        if (pdfFile == null) {
            return null;
        }
        return pdfFile.getMetaData();
    }

    /** Will be empty until document is loaded */
    public List<PdfDocument.Bookmark> getTableOfContents() {
        if (pdfFile == null) {
            return Collections.emptyList();
        }
        return pdfFile.getBookmarks();
    }

    /** Will be empty until document is loaded */
    public List<PdfDocument.Link> getLinks(int page) {
        if (pdfFile == null) {
            return Collections.emptyList();
        }
        return pdfFile.getPageLinks(page);
    }

    /** Use an asset file as the pdf source */
    public Builder fromAsset(String assetName) {
        return new Builder(this, new AssetSource(assetName));
    }

    /** Use a file as the pdf source */
    public Builder fromFile(File file) {
        return new Builder(this, new FileSource(file));
    }

    /** Use URI as the pdf source, for use with content providers */
    public Builder fromUri(Uri uri) {
        return new Builder(this, new UriSource(uri));
    }

    /** Use bytearray as the pdf source, documents is not saved */
    public Builder fromBytes(byte[] bytes) {
        return new Builder(this, new ByteArraySource(bytes));
    }

    /** Use stream as the pdf source. Stream will be written to bytearray, because native code does not support Java Streams */
    public Builder fromStream(InputStream stream) {
        return new Builder(this, new InputStreamSource(stream));
    }

    /** Use custom source as pdf source */
    public Builder fromSource(DocumentSource docSource) {
        return new Builder(this, docSource);
    }

    private enum State {DEFAULT, LOADED, SHOWN, ERROR}

    public class Builder {
		final PDocView pThis;
        private final DocumentSource documentSource;
        private String password = null;
        private Builder(PDocView pThis, DocumentSource documentSource) {
			this.pThis = pThis;
			this.documentSource = documentSource;
			pThis.recycle();
			CMN.Log("build", pThis.isPageSnap());
        }
        public Builder enableSwipe(boolean enableSwipe) {
			pThis.setSwipeEnabled(enableSwipe);
            return this;
        }
        public Builder enableDoubletap(boolean enableDoubletap) {
			pThis.enableDoubletap(enableDoubletap);
            return this;
        }
        public Builder enableAnnotationRendering(boolean annotationRendering) {
			pThis.annotationRendering = annotationRendering;
            return this;
        }
        public Builder onDraw(OnDrawListener onDrawListener) {
			pThis.callbacks.setOnDraw(onDrawListener);
            return this;
        }
        public Builder onDrawAll(OnDrawListener onDrawAllListener) {
			pThis.callbacks.setOnDrawAll(onDrawAllListener);
            return this;
        }
        public Builder onLoad(OnLoadCompleteListener onLoadCompleteListener) {
			pThis.callbacks.setOnLoadComplete(onLoadCompleteListener);
            return this;
        }
        public Builder onPageScroll(OnPageScrollListener onPageScrollListener) {
			pThis.callbacks.setOnPageScroll(onPageScrollListener);
            return this;
        }
        public Builder onError(OnErrorListener onErrorListener) {
			pThis.callbacks.setOnError(onErrorListener);
            return this;
        }
        public Builder onPageError(OnPageErrorListener onPageErrorListener) {
			pThis.callbacks.setOnPageError(onPageErrorListener);
            return this;
        }
        public Builder onPageChange(OnPageChangeListener onPageChangeListener) {
			pThis.callbacks.setOnPageChange(onPageChangeListener);
            return this;
        }
        public Builder onRender(OnRenderListener onRenderListener) {
			pThis.callbacks.setOnRender(onRenderListener);
            return this;
        }
        public Builder onTap(OnTapListener onTapListener) {
			pThis.callbacks.setOnTap(onTapListener);
            return this;
        }
        public Builder onLongPress(OnLongPressListener onLongPressListener) {
			pThis.callbacks.setOnLongPress(onLongPressListener);
            return this;
        }
        public Builder linkHandler(LinkHandler linkHandler) {
			pThis.callbacks.setLinkHandler(linkHandler);
            return this;
        }
        public Builder defaultPage(int defaultPage) {
			pThis.setDefaultPage(defaultPage);
            return this;
        }
        public Builder swipeHorizontal(boolean swipeHorizontal) {
			pThis.setSwipeVertical(!swipeHorizontal);
            return this;
        }
        public Builder password(String password) {
            this.password = password;
            return this;
        }
        public Builder scrollHandle(ScrollHandle scrollHandle) {
			pThis.setScrollHandle(scrollHandle);
            return this;
        }
        public Builder enableAntialiasing(boolean antialiasing) {
			pThis.enableAntialiasing(antialiasing);
            return this;
        }
        public Builder spacing(int spacing) {
			pThis.setSpacing(spacing);
            return this;
        }
        public Builder autoSpacing(boolean autoSpacing) {
			pThis.setAutoSpacing(autoSpacing);
            return this;
        }
        public Builder pageFitPolicy(FitPolicy pageFitPolicy) {
			pThis.setPageFitPolicy(pageFitPolicy);
            return this;
        }
        public Builder fitEachPage(boolean fitEachPage) {
			pThis.setFitEachPage(fitEachPage);
			return this;
        }
        public Builder pageSnap(boolean pageSnap) {
			pThis.setPageSnap(pageSnap);
            return this;
        }
        public Builder pageFling(boolean pageFling) {
			pThis.setPageFling(pageFling);
            return this;
        }
        public Builder nightMode(boolean nightMode) {
			pThis.setNightMode(nightMode);
            return this;
        }
        public Builder disableLongpress() {
            pThis.dragPinchManager.disableLongpress();
            return this;
        }
        public void load() {
            if (!hasSize) {
                waitingDocumentConfigurator = this;
                return;
            }
			pThis.load(documentSource, password);
        }
    }
}
