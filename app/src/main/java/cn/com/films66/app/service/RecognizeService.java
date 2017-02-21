package cn.com.films66.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;

import com.acrcloud.rec.sdk.ACRCloudClient;
import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.IACRCloudListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shuyu.core.uils.LogUtils;
import com.shuyu.core.uils.ToastUtils;

import java.io.File;

import cn.com.films66.app.activity.DialogActivity;
import cn.com.films66.app.model.RecognizeEntity;
import cn.com.films66.app.utils.Constants;

public class RecognizeService extends Service {

    private ACRCloudClient mClient;
    private ACRCloudConfig mConfig;

    private String path = "";
    private boolean mProcessing = false;
    private boolean initState = false;
    private long startTime = 0;

    public RecognizeService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        path = Environment.getExternalStorageDirectory().toString() + "/acrcloud/model";
        File file = new File(path);
        boolean exists = file.exists();
        if (!exists) {
            exists = file.mkdirs();
        }
        if (!exists) {
            return;
        }
        mConfig = new ACRCloudConfig();
        mConfig.context = this;
        mConfig.host = "cn-north-1.api.acrcloud.com";
        // offline db path, you can change it with other path which this app can access.
        mConfig.dbPath = path;
        mConfig.accessKey = "e3c58e23b70a881d960cfa963d0f1965";
        mConfig.accessSecret = "wP2o4851sOditOCixl8s8ru2iTf9pdQ7f10xofxr";
        mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE;
        // mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_LOCAL;
        //mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_BOTH;
        mConfig.acrcloudListener = acrCloudListener;
        mClient = new ACRCloudClient();
        // If reqMode is REC_MODE_LOCAL or REC_MODE_BOTH,
        // the function initWithConfig is used to load offline db, and it may cost long time.
        initState = mClient.initWithConfig(mConfig);
        LogUtils.d(RecognizeService.class.getName(), "initState=" + initState);
        if (initState) {
            //start prerecord, you can call "mClient.stopPreRecord()" to stop prerecord.
            mClient.startPreRecord(3000);
        }
    }

    private IACRCloudListener acrCloudListener = new IACRCloudListener() {

        @Override
        public void onResult(String s) {
            if (mClient != null) {
                mClient.cancel();
                mProcessing = false;
            }
            LogUtils.d(RecognizeService.class.getName(), s);
            long time = (System.currentTimeMillis() - startTime) / 1000;
            ToastUtils.getInstance().showToast("识别结束，用时：" + time + '秒');

            RecognizeEntity recognizeEntity = new Gson().fromJson(s,
                    new TypeToken<RecognizeEntity>() {
                    }.getType());

            if (recognizeEntity != null && recognizeEntity.status.code == 0) {
                if (recognizeEntity.metadata.music != null) {
                    Intent intent = new Intent(RecognizeService.this, DialogActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.KEY_RECOGNIZE_RESULT
                            , recognizeEntity.metadata.music.get(0).title);
                    intent.putExtras(bundle);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    stopSelf();
                }
            }
        }

        @Override
        public void onVolumeChanged(double v) {

        }
    };


    private void startRecognize() {
        if (!initState) {
            ToastUtils.getInstance().showToast("init error");
            return;
        }

        if (!mProcessing) {
            mProcessing = true;
            if (mClient == null || !mClient.startRecognize()) {
                mProcessing = false;
                LogUtils.d(RecognizeService.class.getName(), "start error!");
            }
            startTime = System.currentTimeMillis();
            LogUtils.d(RecognizeService.class.getName(), "startRecognize");
        }
    }

    private void stopRecognize() {
        if (mProcessing && mClient != null) {
            mClient.stopRecordToRecognize();
        }
        mProcessing = false;
        LogUtils.d(RecognizeService.class.getName(), "stopRecognize");
    }

    private void cancelRecognize() {
        if (mProcessing && mClient != null) {
            mProcessing = false;
            mClient.cancel();
        }
        LogUtils.d(RecognizeService.class.getName(), "cancelRecognize");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        cancelRecognize();
        startRecognize();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mClient != null) {
            mClient.release();
            initState = false;
            mClient = null;
        }
    }
}
