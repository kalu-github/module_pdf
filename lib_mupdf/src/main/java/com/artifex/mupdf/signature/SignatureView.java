package com.artifex.mupdf.signature;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import com.artifex.mupdf.R;

import java.util.ArrayList;
import java.util.List;

/**
 * description: 手写轨迹
 * create by kalu on 2019/07/01
 */
public class SignatureView extends AppCompatTextView {
    // View state
    private List<SignaturePoint> mPoints;
    private boolean mIsEmpty;
    private float mLastTouchX;
    private float mLastTouchY;
    private float mLastVelocity;
    private float mLastWidth;
    private RectF mDirtyRect;

    // Configurable parameters
    private int mMinWidth;
    private int mMaxWidth;
    private float mVelocityFilterWeight;
    private OnSignedListener mOnSignedListener;

    private Path mPath = new Path();
    private Bitmap mSignatureBitmap = null;
    private Canvas mSignatureBitmapCanvas = null;

    public SignatureView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SignatureView, 0, 0);

        // Configurable parameters
        try {
            mMinWidth = a.getDimensionPixelSize(R.styleable.SignatureView_sv_width_min, convertDpToPx(4));
            mMaxWidth = a.getDimensionPixelSize(R.styleable.SignatureView_sv_width_max, convertDpToPx(8));
            mVelocityFilterWeight = a.getFloat(R.styleable.SignatureView_sv_stroke, 1.4f);
            int color = a.getColor(R.styleable.SignatureView_sv_color, Color.BLACK);
            setTextColor(color);
        } finally {
            a.recycle();
        }

        // Dirty rectangle to update only the changed portion of the view
        mDirtyRect = new RectF();

        clear();
    }

    /**
     * Set the pen color from a given resource. If the resource is not found,
     * {@link android.graphics.Color#BLACK} is assumed.
     *
     * @param colorRes the color resource.
     */
    @SuppressLint("ResourceType")
    public void setPenColorRes(int colorRes) {
        try {
            setPenColor(getResources().getColor(colorRes));
        } catch (Resources.NotFoundException ex) {
            setPenColor(getResources().getColor(Color.BLACK));
        }
    }

    /**
     * Set the pen color from a given color.
     *
     * @param color the color.
     */
    public void setPenColor(int color) {
        getPaint().setColor(color);
    }

    /**
     * Set the minimum width of the stroke in pixel.
     *
     * @param minWidth the width in dp.
     */
    public void setMinWidth(float minWidth) {
        mMinWidth = convertDpToPx(minWidth);
    }

    /**
     * Set the maximum width of the stroke in pixel.
     *
     * @param maxWidth the width in dp.
     */
    public void setMaxWidth(float maxWidth) {
        mMaxWidth = convertDpToPx(maxWidth);
    }

    /**
     * Set the velocity filter weight.
     *
     * @param velocityFilterWeight the weight.
     */
    public void setVelocityFilterWeight(float velocityFilterWeight) {
        mVelocityFilterWeight = velocityFilterWeight;
    }

    public void clear() {
        mPoints = new ArrayList<SignaturePoint>();
        mLastVelocity = 0;
        mLastWidth = (mMinWidth + mMaxWidth) / 2;
        mPath.reset();

        if (mSignatureBitmap != null) {
            mSignatureBitmap = null;
            ensureSignatureBitmap();
        }

        setIsEmpty(true);

        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled())
            return false;

        float eventX = event.getX();
        float eventY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                mPoints.clear();
                mPath.moveTo(eventX, eventY);
                mLastTouchX = eventX;
                mLastTouchY = eventY;
                addPoint(new SignaturePoint(eventX, eventY));

            case MotionEvent.ACTION_MOVE:
                resetDirtyRect(eventX, eventY);
                addPoint(new SignaturePoint(eventX, eventY));
                break;

            case MotionEvent.ACTION_UP:
                resetDirtyRect(eventX, eventY);
                addPoint(new SignaturePoint(eventX, eventY));
                getParent().requestDisallowInterceptTouchEvent(true);
                setIsEmpty(false);
                break;

            default:
                return false;
        }

        // invalidate();
        invalidate((int) (mDirtyRect.left - mMaxWidth), (int) (mDirtyRect.top - mMaxWidth),
                (int) (mDirtyRect.right + mMaxWidth), (int) (mDirtyRect.bottom + mMaxWidth));

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (null == mSignatureBitmap)
            return;

        TextPaint paint = getPaint();
        paint.setAntiAlias(true);
        paint.setFakeBoldText(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawBitmap(mSignatureBitmap, 0, 0, paint);
    }

    public void setOnSignedListener(OnSignedListener listener) {
        mOnSignedListener = listener;
    }

    public boolean isEmpty() {
        return mIsEmpty;
    }

    public Bitmap getSignatureBitmap() {
        Bitmap originalBitmap = getTransparentSignatureBitmap();
        Bitmap whiteBgBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(whiteBgBitmap);
        canvas.drawColor(0x00000000);
        canvas.drawBitmap(originalBitmap, 0, 0, null);
        return whiteBgBitmap;
    }

    public void setSignatureBitmap(Bitmap signature) {
        clear();
        ensureSignatureBitmap();

        RectF tempSrc = new RectF();
        RectF tempDst = new RectF();

        int dWidth = signature.getWidth();
        int dHeight = signature.getHeight();
        int vWidth = getWidth();
        int vHeight = getHeight();

        // Generate the required transform.
        tempSrc.set(0, 0, dWidth, dHeight);
        tempDst.set(0, 0, vWidth, vHeight);

        Matrix drawMatrix = new Matrix();
        drawMatrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.CENTER);

        Canvas canvas = new Canvas(mSignatureBitmap);
        canvas.drawBitmap(signature, drawMatrix, null);
        setIsEmpty(false);
        invalidate();
    }

    public Bitmap getTransparentSignatureBitmap() {
        ensureSignatureBitmap();
        return mSignatureBitmap;
    }

    public Bitmap getTransparentSignatureBitmap(boolean trimBlankSpace) {

        if (!trimBlankSpace) {
            return getTransparentSignatureBitmap();
        }

        ensureSignatureBitmap();

        int imgHeight = mSignatureBitmap.getHeight();
        int imgWidth = mSignatureBitmap.getWidth();

        int backgroundColor = Color.TRANSPARENT;

        int xMin = Integer.MAX_VALUE, xMax = Integer.MIN_VALUE, yMin = Integer.MAX_VALUE, yMax = Integer.MIN_VALUE;

        boolean foundPixel = false;

        // Find xMin
        for (int x = 0; x < imgWidth; x++) {
            boolean stop = false;
            for (int y = 0; y < imgHeight; y++) {
                if (mSignatureBitmap.getPixel(x, y) != backgroundColor) {
                    xMin = x;
                    stop = true;
                    foundPixel = true;
                    break;
                }
            }
            if (stop)
                break;
        }

        // Image is empty...
        if (!foundPixel)
            return null;

        // Find yMin
        for (int y = 0; y < imgHeight; y++) {
            boolean stop = false;
            for (int x = xMin; x < imgWidth; x++) {
                if (mSignatureBitmap.getPixel(x, y) != backgroundColor) {
                    yMin = y;
                    stop = true;
                    break;
                }
            }
            if (stop)
                break;
        }

        // Find xMax
        for (int x = imgWidth - 1; x >= xMin; x--) {
            boolean stop = false;
            for (int y = yMin; y < imgHeight; y++) {
                if (mSignatureBitmap.getPixel(x, y) != backgroundColor) {
                    xMax = x;
                    stop = true;
                    break;
                }
            }
            if (stop)
                break;
        }

        // Find yMax
        for (int y = imgHeight - 1; y >= yMin; y--) {
            boolean stop = false;
            for (int x = xMin; x <= xMax; x++) {
                if (mSignatureBitmap.getPixel(x, y) != backgroundColor) {
                    yMax = y;
                    stop = true;
                    break;
                }
            }
            if (stop)
                break;
        }

        return Bitmap.createBitmap(mSignatureBitmap, xMin, yMin, xMax - xMin, yMax - yMin);
    }

    private void addPoint(SignaturePoint newPoint) {
        mPoints.add(newPoint);
        if (mPoints.size() > 2) {
            // To reduce the initial lag make it work with 3 mPoints
            // by copying the first point to the beginning.
            if (mPoints.size() == 3)
                mPoints.add(0, mPoints.get(0));

            SignatureModel tmp = calculateCurveControlPoints(mPoints.get(0), mPoints.get(1), mPoints.get(2));
            SignaturePoint c2 = tmp.c2;
            tmp = calculateCurveControlPoints(mPoints.get(1), mPoints.get(2), mPoints.get(3));
            SignaturePoint c3 = tmp.c1;
            SignatureBezier curve = new SignatureBezier(mPoints.get(1), c2, c3, mPoints.get(2));

            SignaturePoint startPoint = curve.startPoint;
            SignaturePoint endPoint = curve.endPoint;

            float velocity = endPoint.velocityFrom(startPoint);
            velocity = Float.isNaN(velocity) ? 0.0f : velocity;

            velocity = mVelocityFilterWeight * velocity + (1 - mVelocityFilterWeight) * mLastVelocity;

            // The new width is a function of the velocity. Higher velocities
            // correspond to thinner strokes.
            float newWidth = strokeWidth(velocity);

            // The SignatureBezier's width starts out as last curve's final width, and
            // gradually changes to the stroke width just calculated. The new
            // width calculation is based on the velocity between the SignatureBezier's
            // start and end mPoints.
            addBezier(curve, mLastWidth, newWidth);

            mLastVelocity = velocity;
            mLastWidth = newWidth;

            // Remove the first element from the list,
            // so that we always have no more than 4 mPoints in mPoints array.
            mPoints.remove(0);
        }
    }

    private void addBezier(SignatureBezier curve, float startWidth, float endWidth) {
        ensureSignatureBitmap();
        float originalWidth = getPaint().getStrokeWidth();
        float widthDelta = endWidth - startWidth;
        float drawSteps = (float) Math.floor(curve.length());

        for (int i = 0; i < drawSteps; i++) {
            // Calculate the SignatureBezier (x, y) coordinate for this step.
            float t = ((float) i) / drawSteps;
            float tt = t * t;
            float ttt = tt * t;
            float u = 1 - t;
            float uu = u * u;
            float uuu = uu * u;

            float x = uuu * curve.startPoint.x;
            x += 3 * uu * t * curve.control1.x;
            x += 3 * u * tt * curve.control2.x;
            x += ttt * curve.endPoint.x;

            float y = uuu * curve.startPoint.y;
            y += 3 * uu * t * curve.control1.y;
            y += 3 * u * tt * curve.control2.y;
            y += ttt * curve.endPoint.y;

            // Set the incremental stroke width and draw.
            getPaint().setStrokeWidth(startWidth + ttt * widthDelta);
            mSignatureBitmapCanvas.drawPoint(x, y, getPaint());

            expandDirtyRect(x, y);
        }

        getPaint().setStrokeWidth(originalWidth);
    }

    private SignatureModel calculateCurveControlPoints(SignaturePoint s1, SignaturePoint s2, SignaturePoint s3) {
        float dx1 = s1.x - s2.x;
        float dy1 = s1.y - s2.y;
        float dx2 = s2.x - s3.x;
        float dy2 = s2.y - s3.y;

        SignaturePoint m1 = new SignaturePoint((s1.x + s2.x) / 2.0f, (s1.y + s2.y) / 2.0f);
        SignaturePoint m2 = new SignaturePoint((s2.x + s3.x) / 2.0f, (s2.y + s3.y) / 2.0f);

        float l1 = (float) Math.sqrt(dx1 * dx1 + dy1 * dy1);
        float l2 = (float) Math.sqrt(dx2 * dx2 + dy2 * dy2);

        float dxm = (m1.x - m2.x);
        float dym = (m1.y - m2.y);
        float k = l2 / (l1 + l2);
        SignaturePoint cm = new SignaturePoint(m2.x + dxm * k, m2.y + dym * k);

        float tx = s2.x - cm.x;
        float ty = s2.y - cm.y;

        return new SignatureModel(new SignaturePoint(m1.x + tx, m1.y + ty), new SignaturePoint(m2.x + tx, m2.y + ty));
    }

    private float strokeWidth(float velocity) {
        return Math.max(mMaxWidth / (velocity + 1), mMinWidth);
    }

    /**
     * Called when replaying history to ensure the dirty region includes all
     * mPoints.
     *
     * @param historicalX the previous x coordinate.
     * @param historicalY the previous y coordinate.
     */
    private void expandDirtyRect(float historicalX, float historicalY) {
        if (historicalX < mDirtyRect.left) {
            mDirtyRect.left = historicalX;
        } else if (historicalX > mDirtyRect.right) {
            mDirtyRect.right = historicalX;
        }
        if (historicalY < mDirtyRect.top) {
            mDirtyRect.top = historicalY;
        } else if (historicalY > mDirtyRect.bottom) {
            mDirtyRect.bottom = historicalY;
        }
    }

    /**
     * Resets the dirty region when the motion event occurs.
     *
     * @param eventX the event x coordinate.
     * @param eventY the event y coordinate.
     */
    private void resetDirtyRect(float eventX, float eventY) {

        // The mLastTouchX and mLastTouchY were set when the ACTION_DOWN motion
        // event occurred.
        mDirtyRect.left = Math.min(mLastTouchX, eventX);
        mDirtyRect.right = Math.max(mLastTouchX, eventX);
        mDirtyRect.top = Math.min(mLastTouchY, eventY);
        mDirtyRect.bottom = Math.max(mLastTouchY, eventY);
    }

    private void setIsEmpty(boolean newValue) {
        mIsEmpty = newValue;
        if (mOnSignedListener != null) {
            if (mIsEmpty) {
                mOnSignedListener.onClear();
            } else {
                mOnSignedListener.onSigned();
            }
        }
    }

    private int width, height;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
    }

    public void ensureSignatureBitmap() {
        if (mSignatureBitmap == null) {
            mSignatureBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

            mSignatureBitmapCanvas = new Canvas(mSignatureBitmap);

            //设置背景图图片  要指定图片大小  否则下面注释的方法会放大
            mSignatureBitmapCanvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.qq),
                    new Rect(0, 0, BitmapFactory.decodeResource(getResources(), R.drawable.qq).getWidth(),
                            BitmapFactory.decodeResource(getResources(), R.drawable.qq).getHeight()),
                    new Rect(0, 0, width, height), getPaint());
//            mSignatureBitmapCanvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bg_splash), matrix, mPaint);

        }
    }

//    public void drawBitmap() {
//        if (mSignatureBitmapCanvas == null) {
//            mSignatureBitmapCanvas = new Canvas();
//        }
//        mSignatureBitmapCanvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bg_splash), new Matrix(), mPaint);
//
//    }

    private int convertDpToPx(float dp) {
        return Math.round(dp * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public interface OnSignedListener {
        public void onSigned();

        public void onClear();
    }
}
