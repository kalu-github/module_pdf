package com.kalu.office;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.tencent.smtt.sdk.QbSdk;

import lib.kalu.tbs.TbsLayout;

public class TbsActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        QbSdk.clear(getApplicationContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tbs);

        final TextView text = findViewById(R.id.info);
        final TbsLayout frame = findViewById(R.id.tbs_data);

        frame.init(getApplicationContext(), new TbsLayout.OnTbsChangeListener() {
            @Override
            public void onInittbs(boolean isInitSucc) {
                text.setText("初始化：" + isInitSucc);
            }

            @Override
            public void onInstall(boolean isInstallSucc) {
                text.setText("安装：" + isInstallSucc);
            }

            @Override
            public void onDownload(int progress) {
                text.setText("下载：" + progress);
            }

            @Override
            public void onOpen() {
                String url = "http://cdn07.foxitsoftware.cn/pub/foxit/manual/phantom/en_us/API%20Reference%20for%20Application%20Communication.pdf";
                frame.open(TbsActivity.this, url);
            }
        });
    }
}
