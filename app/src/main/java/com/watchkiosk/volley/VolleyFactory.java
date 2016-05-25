package com.watchkiosk.volley;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by USER on 9/11/2015.
 */
public class VolleyFactory
{
    private static RequestQueue mRequestQueue;

    public static RequestQueue getRequestQueue(Context context)
    {
        if (mRequestQueue == null)
        {
            mRequestQueue = Volley.newRequestQueue(context);
        }

        return mRequestQueue;
    }

}
