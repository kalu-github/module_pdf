package com.kalu.office;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;

import java.util.HashMap;

/**
 * description: 全生命周期
 * create by kalu on 2018/10/9 17:04
 */
public final class App extends Application implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);

        final HashMap<String, Object> map = new HashMap<>();
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        QbSdk.initTbsSettings(map);
        QbSdk.setDisableUnpreinitBySwitch(false);
        QbSdk.setDisableUseHostBackupCoreBySwitch(false);
        QbSdk.initBuglyAsync(false);
        QbSdk.initX5Environment(getApplicationContext(), new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                Log.e("apps", "onCoreInitFinished ==>");
            }

            @Override
            public void onViewInitFinished(boolean b) {
                Log.e("apps", "onViewInitFinished ==> b = " + b);
            }
        });
        QbSdk.setTbsListener(new TbsListener() {

            @Override
            public void onDownloadFinish(int i) {
                Log.e("apps", "onDownloadFinish ==> i = " + i);
            }

            @Override
            public void onInstallFinish(int i) {
                Log.e("apps", "onInstallFinish ==> i = " + i);
            }

            @Override
            public void onDownloadProgress(int i) {
                Log.e("apps", "onDownloadProgress ==> i = " + i);
            }
        });
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }
}
