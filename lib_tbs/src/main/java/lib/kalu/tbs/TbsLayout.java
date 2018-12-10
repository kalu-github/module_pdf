package lib.kalu.tbs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;
import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * description: open wps
 * create by kalu on 2018/12/7 13:11
 */
public final class TbsLayout extends FrameLayout {

    private final String FOLDER = TbsLayout.class.getSimpleName().toLowerCase();
    private OkHttpClient mOkHttpClient;

    public TbsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public final void clear() {

    }

    public final void init(final Context context, final OnTbsChangeListener listener) {

        boolean tbsCoreInited = QbSdk.isTbsCoreInited();
        Log.e("kalu", "tbsCoreInited = "+tbsCoreInited);
        if (tbsCoreInited) {
            QbSdk.preInit(context);
            listener.onOpen();
            return;
        }

        final HashMap<String, Object> map = new HashMap<>();
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        QbSdk.initTbsSettings(map);
        QbSdk.setDisableUnpreinitBySwitch(false);
        QbSdk.setDisableUseHostBackupCoreBySwitch(false);
        QbSdk.initX5Environment(context, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                Log.e("apps", "onCoreInitFinished ==>");
            }

            @Override
            public void onViewInitFinished(boolean b) {
                listener.onInittbs(b);
                if (b) {
                    listener.onOpen();
                }
                Log.e("apps", "onViewInitFinished ==> b = " + b);
            }
        });
        QbSdk.setTbsListener(new TbsListener() {

            @Override
            public void onDownloadFinish(int i) {
                listener.onDownload(100);
                Log.e("apps", "onDownloadFinish ==> i = " + i);
            }

            @Override
            public void onInstallFinish(int i) {
                listener.onInstall(i == 200);
                Log.e("apps", "onInstallFinish ==> i = " + i);
            }

            @Override
            public void onDownloadProgress(int i) {
                listener.onDownload(i);
                Log.e("apps", "onDownloadProgress ==> i = " + i);
            }
        });
    }

    @SuppressLint("CheckResult")
    public final void open(final Activity activity, final String url) {

        if (TextUtils.isEmpty(url)) {
            Toast.makeText(getContext(), "网络地址不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        final TbsReaderView tbs = new TbsReaderView(activity, new TbsReaderView.ReaderCallback() {
            @Override
            public void onCallBackAction(Integer integer, Object o, Object o1) {

            }
        });
        addView(tbs);

        if (null == mOkHttpClient) {
            mOkHttpClient = new OkHttpClient();
        }

        Observable.create(new ObservableOnSubscribe<String[]>() {
            @Override
            public void subscribe(ObservableEmitter<String[]> emitter) {

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
                    final String types = url.substring(url.lastIndexOf('.') + 1, url.length());
                    final String name = folder + "/file." + types;
                    final File nameFile = new File(name);
                    nameFile.createNewFile();

                    final byte[] fileReader = new byte[4096];
                    final OutputStream outputStream = new FileOutputStream(nameFile);
                    final InputStream inputStream = body.byteStream();

                    while (true) {
                        int read = inputStream.read(fileReader);
                        if (read == -1) {
                            break;
                        }
                        outputStream.write(fileReader, 0, read);
                        // Log.e("kalu", "size = " + fileSizeDownloaded + ", count = " + body.contentLength());
                    }

                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();

                    emitter.onNext(new String[]{name, types});
                } catch (Exception e) {
                    Log.e("kalu", e.getMessage(), e);
                    emitter.onNext(null);
                }
            }
        }).subscribeOn(Schedulers.io())
                .map(new Function<String[], String[]>() {
                    @Override
                    public String[] apply(String[] s) {
                        if (null == s) {
                            return null;
                        } else {
                            boolean result = tbs.preOpen(s[1], false);
                            return new String[]{s[0], String.valueOf(result)};
                        }
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .map(new Function<String[], Bundle>() {
                    @Override
                    public Bundle apply(String[] strings) {

                        if (null != strings && Boolean.parseBoolean(strings[1])) {
                            final Bundle bundle = new Bundle();
                            bundle.putString("filePath", strings[0]);
                            bundle.putString("tempPath", Environment.getExternalStorageDirectory().getPath());
                            return bundle;
                        } else {
                            return null;
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Bundle>() {
                    @Override
                    public void accept(Bundle bundle) {
                        if (null == bundle) {
                            Toast.makeText(getContext(), "打开失败", Toast.LENGTH_SHORT).show();
                        } else {
                            tbs.openFile(bundle);
                        }
                    }
                });
    }

    /**************************************************************/

    public interface OnTbsChangeListener {

        void onInittbs(final boolean isInitSucc);

        void onInstall(final boolean isInstallSucc);

        void onDownload(final int progress);

        void onOpen();
    }
}
