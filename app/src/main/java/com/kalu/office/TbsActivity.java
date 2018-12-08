package com.kalu.office;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import lib.kalu.tbs.TbsView;

public class TbsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tbs);

        FrameLayout frame = findViewById(R.id.tbs_data);

        String url = "http://cdn07.foxitsoftware.cn/pub/foxit/manual/phantom/en_us/API%20Reference%20for%20Application%20Communication.pdf";
        TbsView tbsView = new TbsView(this);
        tbsView.open(this, url);
        frame.addView(tbsView);
    }
}
