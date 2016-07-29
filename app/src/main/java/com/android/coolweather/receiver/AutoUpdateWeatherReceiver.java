package com.android.coolweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Cherish on 2016/7/29.
 */
public class AutoUpdateWeatherReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent i = new Intent(context,AutoUpdateWeatherReceiver.class);
        context.startActivity(i);
    }
}
