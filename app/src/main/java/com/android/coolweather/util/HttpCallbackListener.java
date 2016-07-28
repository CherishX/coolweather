package com.android.coolweather.util;

/**
 * Created by Cherish on 2016/7/28.
 */
public interface HttpCallbackListener
{
    void onFinish(String response);
    void onError(Exception e);
}
