package com.watchkiosk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.watchkiosk.service.WatchKioskService;

/**
 * Created by USER on 12/23/2015.
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentService = new Intent(context, WatchKioskService.class);
        context.startService(intentService);
    }
}
