package com.android.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.android.coolweather.model.City;
import com.android.coolweather.model.CoolWeatherDB;
import com.android.coolweather.model.Country;
import com.android.coolweather.model.Province;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 该工具类用于解析和处理服务器返回的省市县数据
 * 数据都是“代号|城市,代号|城市”这种格式的
 */
public class Utility
{
    /**
     * 解析和处理服务器返回的省级数据
     */
    public synchronized static boolean handleProvinceResponse(CoolWeatherDB coolWeatherDB,String response){

        if(!TextUtils.isEmpty(response)){
            String[] allProvince = response.split(",");//去掉逗号,返回字符串数组
            if(allProvince !=null && allProvince.length>0){
                for(String p:allProvince){
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return true;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public synchronized static boolean handleCityResponse(CoolWeatherDB coolWeatherDB,String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            String[] allCities = response.split(",");
            if(allCities != null && allCities.length >0 ){
                for(String p:allCities){
                    String[] array = p.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return true;
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    public synchronized static boolean handleCountryResponse(CoolWeatherDB coolWeatherDB,String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            String[] allCountries = response.split(",");
            if(allCountries != null && allCountries.length > 0){
                for(String p:allCountries){
                    String[] array = p.split("\\|");
                    Country country = new Country();
                    country.setCountryCode(array[0]);
                    country.setCountryName(array[1]);
                    country.setCityId(cityId);
                    coolWeatherDB.saveCountries(country);
                }
                return true;
            }
        }
        return true;
    }

    /**
     * 解析服务器返回的JSON数据，并将解析出的数据存储到本地。
     */
    public synchronized static void handleWeatherResponse(Context context,String response){
        try{
            JSONObject weatherInfo = new JSONObject(response).getJSONObject("weatherInfo");
            String cityName = weatherInfo.getString("city");
            String weatherCode = weatherInfo.getString("cityid");
            String temp1 = weatherInfo.getString("temp1");
            String temp2 = weatherInfo.getString("temp2");
            String weatherDesp = weatherInfo.getString("weather");
            String publishTime = weatherInfo.getString("ptime");
            saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherDesp,publishTime);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 将服务器返回的所有天气信息存储到SharedPreferences文件中。
     */
    private static void saveWeatherInfo(Context context,String cityName,String weatherCode,
                                        String temp1,String temp2,String weatherDesp,String publishTime){
        SimpleDateFormat sdf = new SimpleDateFormat("yyy年M月d日", Locale.CANADA);
        SharedPreferences.Editor spEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        spEditor.putBoolean("city_selected",true);
        spEditor.putString("city_name", cityName);
        spEditor.putString("weather_code", weatherCode);
        spEditor.putString("temp1", temp1);
        spEditor.putString("temp2", temp2);
        spEditor.putString("weather_desp", weatherDesp);
        spEditor.putString("publish_time",publishTime);
        spEditor.putString("current_day",sdf.format(new Date()));
        spEditor.commit();
    }

}
