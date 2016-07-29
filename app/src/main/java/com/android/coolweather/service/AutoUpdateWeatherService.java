package com.android.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.android.coolweather.receiver.AutoUpdateWeatherReceiver;
import com.android.coolweather.util.HttpCallbackListener;
import com.android.coolweather.util.HttpUtil;
import com.android.coolweather.util.Utility;

/**
 * 为了要让酷欧天气更加智能，在第五阶段我们准备加入后台自动更新天气的功能，这样
 * 就可以尽可能地保证用户每次打开软件时看到的都是最新的天气信息
 * 要想实现上述功能，就需要创建一个长期在后台运行的定时任务
 */
public class AutoUpdateWeatherService extends Service
{
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                updateWeather();
            }
        }).start();
        //设置定时任务，每8小时更新一次天气信息
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int hourToRefresh = 8 * 60 * 60 * 1000;
        long triggerTime = SystemClock.elapsedRealtime() + hourToRefresh;
        Intent i = new Intent(this, AutoUpdateWeatherReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this,0,i,0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    /**
     * 更新天气信息。
     */
    private void updateWeather()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherCode = prefs.getString("weather_code", "");
        String address = "http://www.weather.com.cn/data/cityinfo/" +
                weatherCode + ".html";
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener()
        {
            @Override
            public void onFinish(String response)
            {
                Utility.handleWeatherResponse(AutoUpdateWeatherService.this,response);
            }

            @Override
            public void onError(Exception e)
            {
                e.printStackTrace();
            }
        });
    }
}
