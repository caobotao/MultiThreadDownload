package com.cbt.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cbt.db.ThreadDAO;
import com.cbt.db.ThreadDAOImpl;
import com.cbt.entities.FileInfo;
import com.cbt.entities.ThreadInfo;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by caobotao on 15/12/22.
 * 下载任务类
 */
public class DownloadTask {
    private Context mContext;
    private FileInfo mFileInfo;
    private ThreadDAO mDao;
    private int mFinished = 0;
    public boolean isPause = false;
    public DownloadTask(Context mContext, FileInfo fileInfo) {
        this.mContext = mContext;
        this.mFileInfo = fileInfo;
        mDao = new ThreadDAOImpl(mContext);
    }
    public void download(){
        //读取数据库的线程信息
        List<ThreadInfo> threadInfos = mDao.getThreads(mFileInfo.getUrl());
        ThreadInfo threadInfo = null;
        Log.i("aaa","aaa");
        if (threadInfos.size() == 0) {
            //初始化线程信息对象
            threadInfo = new ThreadInfo(0,mFileInfo.getUrl(),0,mFileInfo.getLength(),0);
        }else {
            threadInfo = threadInfos.get(0);
        }
        //创建子线程进行下载
        new DownloadThread(threadInfo).start();
    }
    //下载线程
    class DownloadThread extends Thread{
        private ThreadInfo mThreadInfo = null;

        public DownloadThread(ThreadInfo mThreadInfo) {
            this.mThreadInfo = mThreadInfo;
        }

        @Override
        public void run() {
            //向数据库插入线程信息
            if (!mDao.isExists(mThreadInfo.getUrl(),mThreadInfo.getId())){
                mDao.insertThread(mThreadInfo);
            }
            HttpURLConnection connection = null;
            RandomAccessFile raf = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(mThreadInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                //设置下载位置
                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                connection.setRequestProperty("Range","bytes=" + start + "-" + mThreadInfo.getEnd());
                File file = new File(DownLoadService.DOWNLOAD_PATH,mFileInfo.getFileName());
                raf = new RandomAccessFile(file,"rwd");
                //设置文件写入位置
                raf.seek(start);
                Intent intent = new Intent(DownLoadService.ACTION_UPDATE);
                mFinished += mThreadInfo.getFinished();
                //开始下载
                if (connection.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT){
                    //读取数据
                    inputStream = connection.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int len = inputStream.read(buffer);
                    long time = System.currentTimeMillis();
                    do {
                        //写入文件
                        raf.write(buffer,0,len);
                        //把下载进度发送给Activity
                        mFinished += len;
                        len = inputStream.read(buffer);
                        if (System.currentTimeMillis() - time > 200 || len == -1) {
                            time = System.currentTimeMillis();
                            intent.putExtra("finished",mFinished * 100 / mFileInfo.getLength());
                            Log.i("bbb",(mFinished * 100 / mFileInfo.getLength())+"");
                            mContext.sendBroadcast(intent);
                        }

                        //下载暂停时,保存下载进度
                        if (isPause) {
                            mDao.updateThread(mThreadInfo.getUrl(),mThreadInfo.getId(),mFinished);
                            return;
                        }
                    } while (len != -1) ;
//                    int len = -1;
//                    long time = System.currentTimeMillis();
//                    while ((len = inputStream.read(buffer)) != -1) {
//                        //写入文件
//                        raf.write(buffer,0,len);
//                        //把下载进度发送给Activity
//                        mFinished += len;
//                        if (System.currentTimeMillis() - time > 200) {
//                            time = System.currentTimeMillis();
//                            intent.putExtra("finished",mFinished * 100 / mFileInfo.getLength());
//                            Log.i("bbb",(mFinished * 100 / mFileInfo.getLength())+"");
//                            mContext.sendBroadcast(intent);
//                        }
//
//                        //下载暂停时,保存下载进度
//                        if (isPause) {
//                            mDao.updateThread(mThreadInfo.getUrl(),mThreadInfo.getId(),mFinished);
//                            return;
//                        }
//                    }
                    //删除线程信息
                    mDao.deleteThread(mThreadInfo.getUrl(),mThreadInfo.getId());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    connection.disconnect();
                    raf.close();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
