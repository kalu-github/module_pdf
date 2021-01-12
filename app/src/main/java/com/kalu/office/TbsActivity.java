package com.kalu.office;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import lib.kalu.tbs.TbsLayout;

public class TbsActivity extends AppCompatActivity {

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tbs);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final TextView text = (TextView) findViewById(R.id.info);
        final TbsLayout frame = (TbsLayout) findViewById(R.id.tbs_data);

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
