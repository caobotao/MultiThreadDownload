package com.cbt.learndownload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cbt.entities.FileInfo;
import com.cbt.services.DownLoadService;

public class MainActivity extends AppCompatActivity {
    private TextView mTvFileName;
    private ProgressBar mPbProgress;
    private Button mBtnStart;
    private Button mBtnStop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvFileName = (TextView) findViewById(R.id.tv_fileName);
        mPbProgress = (ProgressBar) findViewById(R.id.pb_progress);
        mPbProgress.setMax(100);
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnStop = (Button) findViewById(R.id.btn_stop);
        //创建文件信息对象
        final FileInfo fileInfo = new FileInfo(
                    0,
                    "http://dlsw.baidu.com/sw-search-sp/soft/1a/11798/kugou_V7.6.85.17344_setup.1427079848.exe",
                    "kugou_V7.6.85.17344_setup.1427079848.exe",
                    0,
                    0);
        mTvFileName.setText(fileInfo.getFileName());
        //添加事件监听
        mBtnStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //通过Intent给Service传递参数
                Intent intent = new Intent(MainActivity.this,DownLoadService.class);
                intent.setAction(DownLoadService.ACTION_START);
                intent.putExtra("fileInfo",fileInfo);
                startService(intent);
            }
        });
        mBtnStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //通过Intent给Service传递参数
                Intent intent = new Intent(MainActivity.this,DownLoadService.class);
                intent.setAction(DownLoadService.ACTION_STOP);
                intent.putExtra("fileInfo",fileInfo);
                startService(intent);
            }
        });

        //注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownLoadService.ACTION_UPDATE);
        registerReceiver(mBroadcastReceiver,filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    //更新UI的广播接收器
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownLoadService.ACTION_UPDATE.equals(intent.getAction())) {
                int finished = intent.getIntExtra("finished",0);
                mPbProgress.setProgress(finished);
            }
        }
    };
}
