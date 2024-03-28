package com.example.demo;

import android.app.*;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.*;
import android.provider.Telephony;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class SmsService extends Service {
    //媒体播放器
    MediaPlayer mediaPlayer;

    //定时任务
    private Timer timer;

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "ForegroundServiceChannel";

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MsgBinder();  // 返回null表示该service不提供绑定服务
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 创建通知渠道（仅限安卓8以上）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        // 创建前台通知
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("违章短信提醒服务")
                .setContentText("正在运行...")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();

        // 将服务设置为前台服务
        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler();
                }
            }, 0, 30 * 1000); // 首次执行延迟0秒，然后每30秒执行一次
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel(); // 取消定时器
            timer = null;
        }
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        super.onDestroy();
    }

    public class MsgBinder extends Binder {
        /**
         * 获取当前Service的实例
         * @return
         */
        public SmsService getService(){
            return SmsService.this;
        }
    }


    public void handler() {
        // 获取ContentResolver对象
        ContentResolver contentResolver = getContentResolver();

        // 构建Uri对象，用于查询短信表
        Uri smsUri = Uri.parse("content://sms/inbox");

        // 构建查询条件，这里查询所有短信
        String[] projection = new String[]{Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.DATE,
                Telephony.Sms.BODY, Telephony.Sms.READ};

        // 执行查询操作，获取Cursor对象
        Cursor cursor = contentResolver.query(smsUri, projection, null, null, null);
        // 遍历Cursor对象，输出每条短信的信息
        while (cursor != null && cursor.moveToNext()) {
            long id = cursor.getLong(0);           // 获取短信ID
            String address = cursor.getString(1);   // 获取发件人号码
            long date = cursor.getLong(2);         // 获取发送时间
            String body = cursor.getString(3);     // 获取短信内容
            int read = cursor.getInt(4);           // 获取是否已读标志

            if ((body.contains("关键字测试") || body.contains("未按规定停放")) && System.currentTimeMillis() - date < 60 * 1000 && read == 0) {
                Log.i("提醒", "收到约定内容短信");
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(this, R.raw.remind);
                }
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    Intent updateUi = new Intent("updateUi");
                    sendBroadcast(updateUi);
                }
            }
        }
        cursor.close();
    }

    /**
     * 停止响铃
     */
    public  void stopMusic(){
        if(this.mediaPlayer!=null && this.mediaPlayer.isPlaying()){
            this.mediaPlayer.stop();
        }
    }
}
