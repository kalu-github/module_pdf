package lib.kalu.mupdf.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class PDFPager extends ViewPager {

    public PDFPager(@NonNull Context context) {
        super(context);
    }

    public PDFPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } catch (Exception e) {
            Log.e("pdf", e.getMessage(), e);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception e) {
            Log.e("pdf", e.getMessage(), e);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        try {
            super.dispatchDraw(canvas);
        } catch (Exception e) {
            Log.e("pdf", e.getMessage(), e);
        }
    }

    @Override
    public void setAdapter(@Nullable PagerAdapter adapter) {
        try {
            super.setAdapter(adapter);
        } catch (Exception e) {
            Log.e("pdf", e.getMessage(), e);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
        } catch (Exception e) {
            Log.e("pdf", e.getMessage(), e);
        }
    }
}
