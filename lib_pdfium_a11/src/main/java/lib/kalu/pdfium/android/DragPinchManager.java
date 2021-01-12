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

import android.graphics.PointF;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import lib.kalu.pdfium.android.model.LinkTapEvent;
import lib.kalu.pdfium.android.scroll.ScrollHandle;
import lib.kalu.pdfium.android.util.SnapEdge;
import lib.kalu.pdfium.PdfDocument;
import lib.kalu.pdfium.util.SizeF;

import static lib.kalu.pdfium.android.util.Constants.Pinch.MAXIMUM_ZOOM;
import static lib.kalu.pdfium.android.util.Constants.Pinch.MINIMUM_ZOOM;

/**
 * This Manager takes care of moving the PDFView,
 * set its zoom track user actions.
 */
class DragPinchManager implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener {

    private PDocView pDocView;
    private AnimationManager animationManager;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;

    private boolean scrolling = false;
    private boolean scaling = false;
    private boolean enabled = false;

    DragPinchManager(PDocView pDocView, AnimationManager animationManager) {
        this.pDocView = pDocView;
        this.animationManager = animationManager;
        gestureDetector = new GestureDetector(pDocView.getContext(), this);
        scaleGestureDetector = new ScaleGestureDetector(pDocView.getContext(), this);
        pDocView.setOnTouchListener(this);
    }

    void enable() {
        enabled = true;
    }

    void disable() {
        enabled = false;
    }

    void disableLongpress(){
        gestureDetector.setIsLongpressEnabled(false);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        boolean onTapHandled = pDocView.callbacks.callOnTap(e);
        boolean linkTapped = checkLinkTapped(e.getX(), e.getY());
        if (!onTapHandled && !linkTapped) {
            ScrollHandle ps = pDocView.getScrollHandle();
            if (ps != null && !pDocView.documentFitsView()) {
                if (!ps.shown()) {
                    ps.show();
                } else {
                    ps.hide();
                }
            }
        }
        pDocView.performClick();
        return true;
    }

    private boolean checkLinkTapped(float x, float y) {
        PdfFile pdfFile = pDocView.pdfFile;
        if (pdfFile == null) {
            return false;
        }
        float mappedX = -pDocView.getCurrentXOffset() + x;
        float mappedY = -pDocView.getCurrentYOffset() + y;
        int page = pdfFile.getPageAtOffset(pDocView.isSwipeVertical() ? mappedY : mappedX, pDocView.getZoom());
        SizeF pageSize = pdfFile.getScaledPageSize(page, pDocView.getZoom());
        int pageX, pageY;
        if (pDocView.isSwipeVertical()) {
            pageX = (int) pdfFile.getSecondaryPageOffset(page, pDocView.getZoom());
            pageY = (int) pdfFile.getPageOffset(page, pDocView.getZoom());
        } else {
            pageY = (int) pdfFile.getSecondaryPageOffset(page, pDocView.getZoom());
            pageX = (int) pdfFile.getPageOffset(page, pDocView.getZoom());
        }
        for (PdfDocument.Link link : pdfFile.getPageLinks(page)) {
            RectF mapped = pdfFile.mapRectToDevice(page, pageX, pageY, (int) pageSize.getWidth(),
                    (int) pageSize.getHeight(), link.getBounds());
            mapped.sort();
            if (mapped.contains(mappedX, mappedY)) {
                pDocView.callbacks.callLinkHandler(new LinkTapEvent(x, y, mappedX, mappedY, mapped, link));
                return true;
            }
        }
        return false;
    }

    private void startPageFling(MotionEvent downEvent, MotionEvent ev, float velocityX, float velocityY) {
        if (!checkDoPageFling(velocityX, velocityY)) {
            return;
        }

        int direction;
        if (pDocView.isSwipeVertical()) {
            direction = velocityY > 0 ? -1 : 1;
        } else {
            direction = velocityX > 0 ? -1 : 1;
        }
        // get the focused page during the down event to ensure only a single page is changed
        float delta = pDocView.isSwipeVertical() ? ev.getY() - downEvent.getY() : ev.getX() - downEvent.getX();
        float offsetX = pDocView.getCurrentXOffset() - delta * pDocView.getZoom();
        float offsetY = pDocView.getCurrentYOffset() - delta * pDocView.getZoom();
        int startingPage = pDocView.findFocusPage(offsetX, offsetY);
        int targetPage = Math.max(0, Math.min(pDocView.getPageCount() - 1, startingPage + direction));

        SnapEdge edge = pDocView.findSnapEdge(targetPage);
        float offset = pDocView.snapOffsetForPage(targetPage, edge);
        animationManager.startPageFlingAnimation(-offset);
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (!pDocView.isDoubletapEnabled()) {
            return false;
        }

        if (pDocView.getZoom() < pDocView.getMidZoom()) {
            pDocView.zoomWithAnimation(e.getX(), e.getY(), pDocView.getMidZoom());
        } else if (pDocView.getZoom() < pDocView.getMaxZoom()) {
            pDocView.zoomWithAnimation(e.getX(), e.getY(), pDocView.getMaxZoom());
        } else {
            pDocView.resetZoomWithAnimation();
        }
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        animationManager.stopFling();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        scrolling = true;
        if (pDocView.isZooming() || pDocView.isSwipeEnabled()) {
            pDocView.moveRelativeTo(-distanceX, -distanceY);
        }
        if (!scaling || pDocView.doRenderDuringScale()) {
            pDocView.loadPageByOffset();
        }
        return true;
    }

    private void onScrollEnd(MotionEvent event) {
        pDocView.loadPages();
        hideHandle();
        if (!animationManager.isFlinging()) {
            pDocView.performPageSnap();
        }
    }

    @Override
    public void onLongPress(MotionEvent e) {
        pDocView.callbacks.callOnLongPress(e);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!pDocView.isSwipeEnabled()) {
            return false;
        }
        if (pDocView.isPageFlingEnabled()) {
            if (pDocView.pageFillsScreen()) {
                onBoundedFling(velocityX, velocityY);
            } else {
                startPageFling(e1, e2, velocityX, velocityY);
            }
            return true;
        }

        int xOffset = (int) pDocView.getCurrentXOffset();
        int yOffset = (int) pDocView.getCurrentYOffset();

        float minX, minY;
        PdfFile pdfFile = pDocView.pdfFile;
        if (pDocView.isSwipeVertical()) {
            minX = -(pDocView.toCurrentScale(pdfFile.getMaxPageWidth()) - pDocView.getWidth());
            minY = -(pdfFile.getDocLen(pDocView.getZoom()) - pDocView.getHeight());
        } else {
            minX = -(pdfFile.getDocLen(pDocView.getZoom()) - pDocView.getWidth());
            minY = -(pDocView.toCurrentScale(pdfFile.getMaxPageHeight()) - pDocView.getHeight());
        }

        animationManager.startFlingAnimation(xOffset, yOffset, (int) (velocityX), (int) (velocityY),
                (int) minX, 0, (int) minY, 0);
        return true;
    }

    private void onBoundedFling(float velocityX, float velocityY) {
        int xOffset = (int) pDocView.getCurrentXOffset();
        int yOffset = (int) pDocView.getCurrentYOffset();

        PdfFile pdfFile = pDocView.pdfFile;

        float pageStart = -pdfFile.getPageOffset(pDocView.getCurrentPage(), pDocView.getZoom());
        float pageEnd = pageStart - pdfFile.getPageLength(pDocView.getCurrentPage(), pDocView.getZoom());
        float minX, minY, maxX, maxY;
        if (pDocView.isSwipeVertical()) {
            minX = -(pDocView.toCurrentScale(pdfFile.getMaxPageWidth()) - pDocView.getWidth());
            minY = pageEnd + pDocView.getHeight();
            maxX = 0;
            maxY = pageStart;
        } else {
            minX = pageEnd + pDocView.getWidth();
            minY = -(pDocView.toCurrentScale(pdfFile.getMaxPageHeight()) - pDocView.getHeight());
            maxX = pageStart;
            maxY = 0;
        }

        animationManager.startFlingAnimation(xOffset, yOffset, (int) (velocityX), (int) (velocityY),
                (int) minX, (int) maxX, (int) minY, (int) maxY);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float dr = detector.getScaleFactor();
        float wantedZoom = pDocView.getZoom() * dr;
        float minZoom = Math.min(MINIMUM_ZOOM, pDocView.getMinZoom());
        float maxZoom = Math.min(MAXIMUM_ZOOM, pDocView.getMaxZoom());
        if (wantedZoom < minZoom) {
            dr = minZoom / pDocView.getZoom();
        } else if (wantedZoom > maxZoom) {
            dr = maxZoom / pDocView.getZoom();
        }
        pDocView.zoomCenteredRelativeTo(dr, new PointF(detector.getFocusX(), detector.getFocusY()));
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        scaling = true;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        pDocView.loadPages();
        hideHandle();
        scaling = false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!enabled) {
            return false;
        }

        boolean retVal = scaleGestureDetector.onTouchEvent(event);
        retVal = gestureDetector.onTouchEvent(event) || retVal;

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (scrolling) {
                scrolling = false;
                onScrollEnd(event);
            }
        }
        return retVal;
    }

    private void hideHandle() {
        ScrollHandle scrollHandle = pDocView.getScrollHandle();
        if (scrollHandle != null && scrollHandle.shown()) {
            scrollHandle.hideDelayed();
        }
    }

    private boolean checkDoPageFling(float velocityX, float velocityY) {
        float absX = Math.abs(velocityX);
        float absY = Math.abs(velocityY);
        return pDocView.isSwipeVertical() ? absY > absX : absX > absY;
    }
}
