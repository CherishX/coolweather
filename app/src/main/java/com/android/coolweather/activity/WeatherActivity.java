package com.android.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.view.menu.ShowableListMenu;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.coolweather.R;
import com.android.coolweather.util.HttpCallbackListener;
import com.android.coolweather.util.HttpUtil;
import com.android.coolweather.util.Utility;

/**
 * Created by Cherish on 2016/7/28.
 */
public class WeatherActivity extends Activity implements View.OnClickListener
{
    private LinearLayout weatherInfoLayout;
    private TextView cityNameText;//显示城市名字
    private TextView publishText;//显示发布时间
    private TextView weatherDespText;//显示天气描述信息
    private TextView temp1Text;//用于显示气温1
    private TextView temp2Text;//用于显示气温2
    private TextView currentDateText;//用于显示当前日期
    private Button switchCity;//切换城市按钮
    private Button refreshWeather;//更新天气按钮

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
        //初始化各控件
        cityNameText = (TextView)findViewById(R.id.city_name);
        publishText = (TextView)findViewById(R.id.publish_text);
        weatherDespText = (TextView)findViewById(R.id.weather_desp);
        temp1Text = (TextView)findViewById(R.id.temp1);
        temp2Text = (TextView)findViewById(R.id.temp2);
        currentDateText = (TextView)findViewById(R.id.current_date);

        switchCity = (Button)findViewById(R.id.switch_city);
        refreshWeather = (Button)findViewById(R.id.refresh_weather);

        String countryCode = getIntent().getStringExtra("country_code");
        if(!TextUtils.isEmpty(countryCode)){
            //有县级代号时就去查询天气
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countryCode);
        }else{
            // 没有县级代号时就直接显示本地天气
            showLocalWeather();
        }
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
    }

    /**
     * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上。
     */
    private void showLocalWeather()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("city_name",""));
        temp1Text.setText(prefs.getString("temp1",""));
        temp2Text.setTag(prefs.getString("temp2", ""));
        publishText.setText("今天" + prefs.getString("publish_time","") + "发布");
        weatherDespText.setText(prefs.getString("weather_desp",""));
        currentDateText.setText(prefs.getString("current_date",""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);


    }

    /**
     * 查询县级代号所对应的天气代号。
     */
    private void queryWeatherCode(String countryCode)
    {
        String address = "http://www.weather.com.cn/data/list3/city" +
                countryCode + ".xml";
        //根据县级代号找到对应地址接口，然后从服务器中查询天气代号所对应的天气
        queryFromServer(address,"countryCode");
    }
    /**
     * 查询天气代号所对应的天气。
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" +
                weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }

    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息。
     */
    private void queryFromServer(final String address,final String type)
    {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener()
        {
            @Override
            public void onFinish(String response)
            {
                if("countryCode".equals(type)){
                    if(!TextUtils.isEmpty(response)){
                        String[] array = response.split("\\|");
                        if(array != null && array.length == 2){
                            String weatherCode = array[1];
                            //从服务器中查询天气代号所对应的天气
                            queryWeatherInfo(weatherCode);
                        }
                    }
                }else if("weatherCode".equals(type)){
                    // 解析处理服务器返回的天气信息（JSON数据），存储在SharedPreferences中
                    Utility.handleWeatherResponse(WeatherActivity.this,response);
                    //将天气新显示在主线程上（UI线程）
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            showLocalWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e)
            {
               runOnUiThread(new Runnable()
               {
                   @Override
                   public void run()
                   {
                       publishText.setText("天气更新失败");
                   }
               });
            }
        });
    }

    @Override
    public void onClick(View view)
    {
       switch(view.getId()){
           case R.id.switch_city:
               Intent intent = new Intent(this,ChooseAreaActivity.class);
               intent.putExtra("from_weather_activity", true);
               startActivity(intent);
               finish();
               break;
           case R.id.refresh_weather:
               publishText.setText("更新天气中...");
               SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
               String weatherCode = sp.getString("weather_code","");
               if(!TextUtils.isEmpty(weatherCode)){
                   queryWeatherInfo(weatherCode);
               }
               break;
           default:
               break;
       }
    }
}
