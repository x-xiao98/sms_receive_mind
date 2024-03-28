package com.example.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceive extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent launchIntentForPackage = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        context.startActivity(launchIntentForPackage);
    }
}
