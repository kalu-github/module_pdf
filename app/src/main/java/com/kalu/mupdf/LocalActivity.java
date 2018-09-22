package com.kalu.mupdf;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
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

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class LocalActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {

        final ViewPager view = (ViewPager) findViewById(R.id.pager);
        final Document document = ((OfficeAdapter) view.getAdapter()).getDocument();
        if (null != document) {
            document.destroy();
        }

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
        final Document document = MupdfUtil.openFromRaw(getApplicationContext(), R.raw.test);
        if (null == document) return;

        // step2
        final ViewPager view = (ViewPager) findViewById(R.id.pager);
        final OfficeAdapter adapter = new OfficeAdapter(document);
        view.setAdapter(adapter);


        final TextView number = (TextView) findViewById(R.id.count);
        final int count = document.countPages();
        number.setText(1 + " / " + count);

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

        public OfficeAdapter(Document document) {
            this.document = document;
        }

        public Document getDocument() {
            return document;
        }

        @Override
        public int getCount() {
            if (null == document)
                return 0;

            return document.countPages();
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
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            item.setLayoutParams(layoutParams);

            Page page = document.loadPage(position);
            WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            int width = dm.widthPixels;         //
            Matrix ctm = AndroidDrawDevice.fitPageWidth(page, width);
            Bitmap bitmap = AndroidDrawDevice.drawPage(page, ctm);
            item.setBitmap(bitmap, true, null, null);
            return item;
        }
    }
}