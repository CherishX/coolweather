package com.android.coolweather.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.coolweather.db.CoolWeatherOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cherish on 2016/7/28.
 */
public class CoolWeatherDB
{
   //数据库名
    public static final String DB_NAME = "cool_weather";
   //数据库版本
    public static final int VERSION = 1;

    private SQLiteDatabase sqLiteDatabase;
    private CoolWeatherDB(Context context)
    {
        CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context,DB_NAME,null,VERSION);
        sqLiteDatabase = dbHelper.getWritableDatabase();
    }

    /**
     * 采用单例模式获取数据库实例
     */
    private static CoolWeatherDB coolWeatherDB;
    public synchronized static  CoolWeatherDB getInstance(Context context)
    {
        if(coolWeatherDB == null){
            coolWeatherDB = new CoolWeatherDB(context);
        }
        return  coolWeatherDB;
    }

    /**
     * 将Province实例存储到数据库。
     */
    public void saveProvince(Province province){
        if(province != null){
            ContentValues contentValues = new ContentValues();
            contentValues.put("province_name",province.getProvinceName());
            contentValues.put("province_code",province.getProvinceCode());
            sqLiteDatabase.insert("Province",null,contentValues);
        }
    }
    /**
     * 从数据库中读取所有省份信息
     */
    public List<Province> loadProvinces(){
        List<Province> provinceList = new ArrayList<Province>();
        Cursor cursor = sqLiteDatabase.query("Province",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                provinceList.add(province);
            }while(cursor.moveToNext());
        }
        return provinceList;
    }

    /**
     * 将City实例存储到数据库中
     */
    public void saveCity(City city){
        if(city != null){
            ContentValues contentValues = new ContentValues();
            contentValues.put("city_name",city.getCityName());
            contentValues.put("city_code",city.getCityCode());
            contentValues.put("province_id",city.getProvinceId());
            sqLiteDatabase.insert("City",null,contentValues);
        }
    }
    /**
     * 从数据库中读取所有城市信息
     */
    public List<City> loadCities(int provinceId){
        List<City> cityList = new ArrayList<City>();
        Cursor cursor = sqLiteDatabase.query("City",null,"province_id = ?",new String[] { String.valueOf(provinceId) },null,null,null);
        if(cursor.moveToFirst()){
            do{
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(provinceId);
                cityList.add(city);
            }while(cursor.moveToNext());
        }
        return cityList;
    }

    /**
     * 将Country实例存储到数据库中
     */
    public void saveCountries(Country country){
        if(country != null){
            ContentValues contentValues = new ContentValues();
            contentValues.put("country_name",country.getCountryName());
            contentValues.put("country_code",country.getCountryCode());
            contentValues.put("city_id",country.getCityId());
            sqLiteDatabase.insert("Country", null , contentValues);
        }
    }
    /**
     * 从数据库中读取所有Country信息
     */
    public List<Country> loadCountries(int cityId){
        List<Country> countryList = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.query("Country", null, "city_id = ?", new String[] { String.valueOf(cityId) }, null, null, null);
        if(cursor.moveToFirst()){
            do{
                Country country = new Country();
                country.setId(cursor.getInt(cursor.getColumnIndex("id")));
                country.setCountryName(cursor.getString(cursor.getColumnIndex("country_name")));
                country.setCountryCode(cursor.getString(cursor.getColumnIndex("country_code")));
                country.setCityId(cityId);
                countryList.add(country);
            }while(cursor.moveToNext());
        }
        return countryList;
    }
}
