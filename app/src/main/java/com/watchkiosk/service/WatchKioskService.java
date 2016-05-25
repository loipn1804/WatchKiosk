package com.watchkiosk.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.watchkiosk.receiver.CrashReceiver;
import com.watchkiosk.volley.VolleyFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by USER on 12/23/2015.
 */
public class WatchKioskService extends Service {

    private String BASE_URL = "http://virtnet.techub.io/api/";
    private Handler handler;
    private String FOLDER = "Config";
    private String FILE_NAME = "config.txt";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        stopAlarm();
//        startAlarm();
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                String s = msg.getData().getString("message");
                Toast.makeText(WatchKioskService.this, s, Toast.LENGTH_SHORT).show();
            }

        };
        startWatch();
        return Service.START_STICKY;
    }

    private void sendMessage(String msg) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("message", msg);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    private void startWatch() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(2000);
                        if (readFromFile().equals("1")) {
                            checkTopApp();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    private void checkTopApp() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        // The first in the list of RunningTasks is always the foreground task.
        ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);

        String[] strings = getLollipop(this);

//        final String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();
        String foregroundTaskPackageName = "";
        for (int i = 0; i < strings.length; i++) {
            foregroundTaskPackageName += strings[i];
        }

        if (foregroundTaskPackageName.equalsIgnoreCase("com.kiosk")) {
//            sendMessage("App ok");
        } else {
//            sendMessage("App crash");
//            crashReport(this);
            openApp(this, "com.kiosk");
        }
    }

    private String readFromFile() {
        String contents = "0";
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            String mCurrentPhotoPath = root + "/" + FOLDER + "/" + FILE_NAME;
            File file = new File(mCurrentPhotoPath);
            int length = (int) file.length();

            byte[] bytes = new byte[length];

            FileInputStream in = new FileInputStream(file);

            in.read(bytes);
            in.close();
            contents = new String(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }

        return contents;
    }

    public void crashReport(final Context context) {

        String url = this.BASE_URL + "kiosk/track_crash";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                String android_id = Settings.Secure.getString(context.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
//                params.put("kiosk_id", "17");
                params.put("uuid", android_id);
                params.put("content", "Crash " + android_id);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyFactory.getRequestQueue(context).add(stringRequest);
    }

    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                return false;
                //throw new PackageManager.NameNotFoundException();
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String[] getLollipop(Context context) {
        final int PROCESS_STATE_TOP = 2;

        try {
            Field processStateField = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");

            List<ActivityManager.RunningAppProcessInfo> processes =
                    ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo process : processes) {
                if (
                    // Filters out most non-activity processes
                        process.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                                &&
                                // Filters out processes that are just being
                                // _used_ by the process with the activity
                                process.importanceReasonCode == 0
                        ) {
                    int state = processStateField.getInt(process);

                    if (state == PROCESS_STATE_TOP)
                        /*
                         If multiple candidate processes can get here,
                         it's most likely that apps are being switched.
                         The first one provided by the OS seems to be
                         the one being switched to, so we stop here.
                         */
                        return process.pkgList;
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return new String[]{};
    }

    private void startAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intentCrash = new Intent(this, CrashReceiver.class);

        PendingIntent intentExecuted = PendingIntent.getBroadcast(this, 2, intentCrash, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 10 * 1000, intentExecuted); // 10s
    }

    private void stopAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intentCrash = new Intent(this, CrashReceiver.class);

        PendingIntent intentExecuted = PendingIntent.getBroadcast(this, 2, intentCrash, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(intentExecuted);
    }
}
