package com.watchkiosk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.watchkiosk.R;
import com.watchkiosk.service.WatchKioskService;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intentService = new Intent(this, WatchKioskService.class);
        startService(intentService);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Intent intentService = new Intent(this, WatchKioskService.class);
//        stopService(intentService);
    }
}
