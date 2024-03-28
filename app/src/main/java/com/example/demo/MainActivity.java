package com.example.demo;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    /**
     * activity绑定的service
     */
    SmsService msgService = new SmsService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //动态申请d短信、通知权限
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECEIVE_SMS
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.POST_NOTIFICATIONS},
                    1);
        }

        //绑定service，并开启
        Intent intent = new Intent(this, SmsService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        ContextCompat.startForegroundService(this, intent);



         //页面的【暂停】按钮
        View viewById = findViewById(R.id.stopBtn);

        //自定义广播的回调
        BroadcastReceiver updateUi = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("updateUi")) {
                    viewById.setVisibility(View.VISIBLE);
                }
            }
        };
        //注册自定义广播
        IntentFilter filter = new IntentFilter("updateUi");
        registerReceiver(updateUi, filter);

        //点击【暂停】按钮的回调
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (msgService.getMediaPlayer() != null && msgService.getMediaPlayer().isPlaying()) {
                    msgService.stopMusic();
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Toast.makeText(this, "服务已开启", Toast.LENGTH_SHORT).show();
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //返回一个MsgService对象
            msgService = ((SmsService.MsgBinder) service).getService();
        }
    };
}