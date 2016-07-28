package com.android.coolweather.util;

import android.text.TextUtils;

import com.android.coolweather.model.City;
import com.android.coolweather.model.CoolWeatherDB;
import com.android.coolweather.model.Country;
import com.android.coolweather.model.Province;

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


}
