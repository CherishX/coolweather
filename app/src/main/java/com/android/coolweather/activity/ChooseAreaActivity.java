package com.android.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.coolweather.R;
import com.android.coolweather.model.City;
import com.android.coolweather.model.CoolWeatherDB;
import com.android.coolweather.model.Country;
import com.android.coolweather.model.Province;
import com.android.coolweather.util.ActivityCollector;
import com.android.coolweather.util.HttpCallbackListener;
import com.android.coolweather.util.HttpUtil;
import com.android.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于遍历省市县数据
 */
public class ChooseAreaActivity extends Activity
{
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;

    private TextView mTitleText;
    private ListView mAreaListView;
    private ProgressDialog mDownLoadWeatherDataPd;
    private ArrayAdapter<String> mShowAreaAdapter;
    private List<String> mAreaDataList = new ArrayList<>();
    private CoolWeatherDB mCoolWeatherDB;

    private List<Province> mProvinceList;//省份列表
    private List<City> mCityList;//市列表
    private List<Country> mCountryList;//县列表

    private Province selectedProvince;//选中的省份
    private City selectedCity;//选中的城市
    private Country slectedCountry;//选中的县城

    private int currentLevel;//当前选中的级别

   /**
     * 是否从WeatherActivity中跳转过来。
     */
    private boolean isFromWeatherActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity",false);
        //一开始先从SharedPreferences 文件中读取city_selected 标志位，
        // 如果为true 就说明当前已经选择过城市了，直接跳转到WeatherActivity 即可
        // 已经选择了城市且不是从WeatherActivity跳转过来，才会直接跳转到WeatherActivity
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(preferences.getBoolean("country_selected",false) && !isFromWeatherActivity){
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        ActivityCollector.addActivity(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);

        mTitleText = (TextView)findViewById(R.id.title_text);
        //绑定适配器
        mAreaListView = (ListView)findViewById(R.id.area_list_view);
        mShowAreaAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,mAreaDataList);
        mAreaListView.setAdapter(mShowAreaAdapter);

        mCoolWeatherDB = CoolWeatherDB.getInstance(this);
        queryProvinces(); // 加载省级数据
        mAreaListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                if (currentLevel == LEVEL_PROVINCE)
                {
                    selectedProvince = mProvinceList.get(i);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY)
                {
                    selectedCity = mCityList.get(i);
                    queryCountries();
                } else if (currentLevel == LEVEL_COUNTRY)
                {
                    String countryCode = mCountryList.get(i).getCountryCode();
                    Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                    intent.putExtra("country_code", countryCode);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryProvinces()
    {
        mProvinceList = mCoolWeatherDB.loadProvinces();
        if(mProvinceList.size()>0){
            mAreaDataList.clear();
            for(Province province:mProvinceList){
                mAreaDataList.add(province.getProvinceName());
            }
            //刷新适配器
            mShowAreaAdapter.notifyDataSetChanged();
            mAreaListView.setSelection(0);//ListView定位在第一条
            mTitleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        }else{
            queryFromServer(null,"province");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCountries()
    {
        mCountryList = mCoolWeatherDB.loadCountries(selectedCity.getId());
        if(mCountryList.size()>0){
            mAreaDataList.clear();
            for(Country country:mCountryList){
                mAreaDataList.add(country.getCountryName());
            }
            mShowAreaAdapter.notifyDataSetChanged();
            mAreaListView.setSelection(0);
            mTitleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTRY;
        }else{
            queryFromServer(selectedCity.getCityCode(),"country");
        }
    }
    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCities()
    {
        mCityList = mCoolWeatherDB.loadCities(selectedProvince.getId());
        if(mCityList.size()>0){
            mAreaDataList.clear();
            for(City city:mCityList){
                mAreaDataList.add(city.getCityName());
            }
            mShowAreaAdapter.notifyDataSetChanged();
            mAreaListView.setSelection(0);
            mTitleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        }else{
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }

    }

    /**
     * 根据传入的代号和类型从服务器上查询省市县数据。
     * @param code
     * @param type
     */
    private void queryFromServer(final String code,final String type)
    {
        String address;
        if(!TextUtils.isEmpty(code)){
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml"; //找到对应代号下的数据
        }else{
            address = "http://www.weather.com.cn/data/list3/city.xml"; //代号为空，表示需要全部数据
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener()
        {
            @Override
            public void onFinish(String response)
            {
                boolean result = false;
                if("province".equals(type)){
                    result = Utility.handleProvinceResponse(mCoolWeatherDB,response);
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(mCoolWeatherDB,response,selectedProvince.getId());
                }else if("country".equals(type)){
                    result = Utility.handleCountryResponse(mCoolWeatherDB,response,selectedCity.getId());
                }
                if(result){
                    // 通过runOnUiThread()方法回到主线程处理逻辑
                    //从服务器加载数据后，此时数据库已经存储了需要的数据，再从数据库取出即可
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("country".equals(type)){
                                queryCountries();
                            }
                        }
                    });
                }

            }

            @Override
            public void onError(Exception e)
            {
                // 通过runOnUiThread()方法回到主线程处理逻辑
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog()
    {
        if(mDownLoadWeatherDataPd == null){
            mDownLoadWeatherDataPd = new ProgressDialog(this);
            mDownLoadWeatherDataPd.setMessage("正在加载...");
            //设置不可关闭
            mDownLoadWeatherDataPd.setCanceledOnTouchOutside(false);
        }
        mDownLoadWeatherDataPd.show();
    }
    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog()
    {
      if(mDownLoadWeatherDataPd != null){
          mDownLoadWeatherDataPd.dismiss();
      }
    }

    /**
     * 捕获Back按键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出。
     */
    @Override
    public void onBackPressed()
    {
        if(currentLevel == LEVEL_COUNTRY){
            queryCities();
        }else if(currentLevel == LEVEL_CITY){
            queryProvinces();
        }else if(currentLevel == LEVEL_PROVINCE){
            Intent i = new Intent(this,LoginActivity.class);
            startActivity(i);
        }else{
            if(isFromWeatherActivity){
                Intent intent = new Intent(this,WeatherActivity.class);
                startActivity(intent);
            }
            finish();//退出ChooseAreaActivity
        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
