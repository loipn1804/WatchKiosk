package com.watchkiosk.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by USER on 12/23/2015.
 */
public class CrashService extends IntentService {

    public CrashService() {
        super(CrashService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
