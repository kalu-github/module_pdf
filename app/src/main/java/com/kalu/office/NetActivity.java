package com.kalu.office;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
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
import android.widget.Toast;

import lib.kalu.mupdf.core.Document;
import lib.kalu.mupdf.core.Page;
import lib.kalu.mupdf.core.AndroidDrawDevice;
import lib.kalu.mupdf.android.util.MupdfUtil;
import lib.kalu.mupdf.android.view.PageView;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class NetActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {

        final ViewPager view = (ViewPager) findViewById(R.id.pager);
        if(null == view.getAdapter())
            return;

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

        new AsyncTask<Void, Void, Object>() {

            @Override
            protected void onPreExecute() {
                Toast.makeText(getApplicationContext(), "正在下载...", Toast.LENGTH_SHORT).show();
                super.onPreExecute();
            }

            @Override
            protected Object doInBackground(Void... voids) {

                String url = "http://cdn07.foxitsoftware.cn/pub/foxit/manual/phantom/en_us/API%20Reference%20for%20Application%20Communication.pdf";
                OkHttpClient okHttpClient = new OkHttpClient();
                final Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                Call call = okHttpClient.newCall(request);
                try {
                    Response execute = call.execute();
                    byte[] clone = execute.body().bytes().clone();
                    return clone;
                } catch (IOException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Object o) {
                try {
                    Toast.makeText(getApplicationContext(), "下载完成...", Toast.LENGTH_SHORT).show();
                    openNet((byte[]) o);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private final void openNet(byte[] bytes) {

        // step1
        final Document document = MupdfUtil.openFromByte(getApplicationContext(), bytes);
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


        final TextView number = (TextView) findViewById(R.id.count);
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
        private final ArrayList<Page> mData = new ArrayList<>();

        public final void clearPage() {

            if(null != mData){
                for (Page b : mData) {
                    if (null != b) {
                        b.destroy();
                    }
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