package com.kalu.office;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.artifex.mupdf.viewer.DocumentActivity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.pdfium1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), PdfiumActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.local).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String uriStr = "android.resource://" + getPackageName() + "/" + R.raw.sample;
                Uri uri = Uri.parse(uriStr);

                Intent intent = new Intent(getApplicationContext(), DocumentActivity.class);
                // API>=21: intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT); /* launch as a new document */
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT); /* launch as a new document */
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET); /* launch as a new document */
                intent.setAction(Intent.ACTION_VIEW);
                intent.setType("file");
                intent.setDataAndType(uri, "file");
                startActivity(intent);

            }
        });

        findViewById(R.id.net).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                        Request request = new Request.Builder()
                                .url(url)
                                .get()
                                .build();
                        Call call = okHttpClient.newCall(request);
                        try {
                            Response execute = call.execute();
                            byte[] bytes = execute.body().bytes().clone();

                            String fileName = getFilesDir().getAbsolutePath() + "/temp.pdf";
                            Path path = Paths.get(fileName);
                            Files.write(path, bytes);

                            return fileName;
                        } catch (Exception e) {
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(Object o) {

                        Toast.makeText(getApplicationContext(), null == o ? "下载失败..." : "下载完成...", Toast.LENGTH_SHORT).show();

//                        if (null == o)
//                            return;
//
//                        Uri uri = Uri.parse((String) o);
//
//                        Intent intent = new Intent(getApplicationContext(), DocumentActivity.class);
//                        // API>=21: intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT); /* launch as a new document */
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT); /* launch as a new document */
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET); /* launch as a new document */
//                        intent.setAction(Intent.ACTION_VIEW);
//                        intent.setType("file");
//                        intent.setDataAndType(uri, "file");
//                        startActivity(intent);
                    }
                }.execute();
            }
        });

        findViewById(R.id.wps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), X5Activity.class);
                startActivity(intent);
            }
        });

//        findViewById(R.id.sign).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
////                Intent intent = new Intent(getApplicationContext(), SignActivity.class);
////                startActivity(intent);
//            }
//        });
    }
}