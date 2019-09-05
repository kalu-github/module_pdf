package com.kalu.office;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.artifex.mupdf.fitz.Document;
import com.artifex.mupdf.fitz.Matrix;
import com.artifex.mupdf.fitz.Page;
import com.artifex.mupdf.fitz.android.AndroidDrawDevice;
import com.artifex.mupdf.util.MupdfUtil;
import com.artifex.mupdf.view.PageView;

import java.util.ArrayList;

public final class LocalActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {

        final ViewPager view = findViewById(R.id.pager);
        ((OfficeAdapter) view.getAdapter()).clearPage();

        super.onBackPressed();
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // step1
        final Document document = MupdfUtil.openFromRaw(getApplicationContext(), R.raw.test2);
        if (null == document) return;

        final ArrayList<Page> list = new ArrayList<>();
        final int count = document.countPages();
        for (int j = 0; j < count; j++) {
            Page page = document.loadPage(j);
            list.add(page);
        }

        // step2
        final ViewPager view = (ViewPager) findViewById(R.id.pager);
        final OfficeAdapter adapter = new OfficeAdapter(list, document);
        view.setAdapter(adapter);


        final TextView number = findViewById(R.id.count);
        number.setText(1 + " / " + count);
//
        view.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                number.setText((i + 1) + " / " + count);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    private final class OfficeAdapter extends PagerAdapter {

        private Document document;
        private final ArrayList<Page> mData = new ArrayList<>();

        public final void clearPage() {
            for (Page b : mData) {
                if (null != b) {
                    b.destroy();
                }
            }

            if (null != document) {
                document.destroy();
                document = null;
            }
        }

        public OfficeAdapter(ArrayList<Page> list, Document document) {
            clearPage();
            this.mData.clear();
            this.mData.addAll(list);
            this.document = document;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View item = createItem(position);
            container.addView(item);
            return item;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        private final View createItem(int position) {


            final PageView item = new PageView(getApplicationContext());
            final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            item.setLayoutParams(layoutParams);

            try {
                final Page page = mData.get(position);
                WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics dm = new DisplayMetrics();
                wm.getDefaultDisplay().getMetrics(dm);
                Bitmap bitmap = AndroidDrawDevice.drawPageFit(page, dm.widthPixels, dm.heightPixels);
                item.setBitmap(bitmap, false, null, null);
                item.setTag(bitmap);
            } catch (Exception e) {
                Log.e("pdf", e.getMessage(), e);
            }

            return item;
        }
    }
}