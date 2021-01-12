package lib.kalu.tbs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;
import com.tencent.smtt.sdk.TbsReaderView;

import java.util.HashMap;

import okhttp3.OkHttpClient;

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

    @SuppressLint("CheckResult")
    public final void open(final Activity activity, final String url) {


    }
}
