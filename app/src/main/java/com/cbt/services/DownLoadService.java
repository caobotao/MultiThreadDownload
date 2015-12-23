package com.cbt.services;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import com.cbt.entities.FileInfo;
import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by caobotao on 15/12/21.
 */
public class DownLoadService extends Service {
    public static final String DOWNLOAD_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloads/";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final int MSG_INIT = 0;
    private DownloadTask mTask;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        //获取Activity传来的值
        if (ACTION_START.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.i("test","start: " + fileInfo.toString());
            new InitThread(fileInfo).start();
        }else if (ACTION_STOP.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.i("test","stop: " + fileInfo.toString());
            if (mTask != null) {
                mTask.isPause = true;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    mTask = new DownloadTask(DownLoadService.this,fileInfo);
                    mTask.download();
                    Log.i("test","init:" + fileInfo.toString());
                break;
            }
        }
    };

    //初始化子线程
    class InitThread extends Thread{
        private FileInfo mFileInfo;

        public InitThread(FileInfo mFileInfo) {
            this.mFileInfo = mFileInfo;
        }

        @Override
        public void run() {
            HttpURLConnection connection = null;
            RandomAccessFile raf = null;
            try {
                //连接网络文件
                URL url = new URL(mFileInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                int length = -1;
                if (connection.getResponseCode() == HttpStatus.SC_OK) {
                    //获得文件长度
                    length = connection.getContentLength();
                }
                if (length <= 0) {
                    return;
                }
                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()){
                    dir.mkdir();
                }
                //在本地创建文件
                File file = new File(dir,mFileInfo.getFileName());
                raf = new RandomAccessFile(file,"rwd");
                //设置文件长度
                raf.setLength(length);
                mFileInfo.setLength(length);
                //发送到Handler
                mHandler.obtainMessage(MSG_INIT,mFileInfo).sendToTarget();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try {
                    connection.disconnect();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
