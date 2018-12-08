package lib.kalu.tbs;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * description: open wps
 * create by kalu on 2018/12/7 13:11
 */
public final class TbsView extends TbsReaderView {

    private final String FOLDER = TbsView.class.getSimpleName().toLowerCase();
    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    public TbsView(Activity activity) {
        super(activity, null);
    }

    public final void open(final Activity activity, final String url) {

        if (TextUtils.isEmpty(url)) {
            Toast.makeText(getContext(), "网络地址不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        final String types = url.substring(url.lastIndexOf('.')+1, url.length());

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    final Request request = new Request.Builder()
                            .url(url)
                            .get()
                            .build();
                    final Call call = mOkHttpClient.newCall(request);
                    final Response execute = call.execute();
                    final ResponseBody body = execute.body();

                    // step1
                    final String cache = activity.getCacheDir().getAbsolutePath();
                    final File cacheFile = new File(cache);
                    if (!cacheFile.exists()) {
                        cacheFile.mkdir();
                    }

                    // step2
                    final String folder = cache + File.separator + FOLDER;
                    final File folderFile = new File(folder);
                    if (folderFile.exists()) {
                        folderFile.delete();
                    }
                    folderFile.mkdir();

                    // step3
                    final String name = folder + "/file." + types;
                    final File nameFile = new File(name);
                    nameFile.createNewFile();

                    final byte[] fileReader = new byte[4096];
                    long fileSizeDownloaded = 0;
                    final OutputStream outputStream = new FileOutputStream(nameFile);
                    final InputStream inputStream = body.byteStream();

                    while (true) {
                        int read = inputStream.read(fileReader);
                        if (read == -1) {
                            break;
                        }
                        outputStream.write(fileReader, 0, read);
                        fileSizeDownloaded += read;
                        Log.e("kalu", "size = " + fileSizeDownloaded + ", count = " + body.contentLength());
                    }

                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();

                   // final String var1 = mMap.get(types);
                    boolean result = preOpen(types, false);
                    if (result) {
                        final Bundle bundle = new Bundle();
                        bundle.putString("filePath", name);
                        bundle.putString("tempPath", Environment.getExternalStorageDirectory().getPath());
                        openFile(bundle);
                    } else {
                        Looper.prepare();
                        Toast.makeText(getContext(), "打开失败", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    Log.e("kalu", e.getMessage(), e);
                }

            }
        }).start();
    }
}
