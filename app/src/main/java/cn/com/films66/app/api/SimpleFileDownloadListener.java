package cn.com.films66.app.api;


import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.shuyu.core.uils.LogUtils;

/**
 * Created by Azure on 2017/3/18.
 */

public class SimpleFileDownloadListener extends FileDownloadListener {

    @Override
    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

    }

    @Override
    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        LogUtils.d(SimpleFileDownloadListener.class.getName(), "progress: " + soFarBytes);
    }

    @Override
    protected void completed(BaseDownloadTask task) {
        LogUtils.d(SimpleFileDownloadListener.class.getName(), "completed");
    }

    @Override
    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

    }

    @Override
    protected void error(BaseDownloadTask task, Throwable e) {

    }

    @Override
    protected void warn(BaseDownloadTask task) {

    }
}
