package com.android.coolweather.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

/**
 * Created by Cherish on 2016/7/28.
 */
public class HttpUtil
{
    /**
     *
     * @param address
     * @param listener
     */
    public static void sendHttpRequest(final String address,final HttpCallbackListener listener){
       new Thread(new Runnable()
       {
           @Override
           public void run()
           {
               try{
                   HttpClient httpClient = new DefaultHttpClient();
                   httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,8000);
                   httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,8000);
                   HttpGet httpGet = new HttpGet(address);
                   httpGet.setHeader("Accept-Language","zh-cn");
                   HttpResponse httpResponse = httpClient.execute(httpGet);
                   if(httpResponse.getStatusLine().getStatusCode() == 200){
                       HttpEntity httpEntity = httpResponse.getEntity();
                       String response = EntityUtils.toString(httpEntity,"utf-8");
                       if(listener != null){
                           listener.onFinish(response.toString());
                       }
                   }

               }catch(Exception e){
                   if(listener != null){
                       listener.onError(e);
                   }
               }
           }
       }).start();
    }
}
