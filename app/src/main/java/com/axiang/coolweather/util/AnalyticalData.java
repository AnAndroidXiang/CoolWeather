package com.axiang.coolweather.util;

import java.text.SimpleDateFormat;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.axiang.coolweather.db.CoolWeatherDBOperate;
import com.axiang.coolweather.model.City;
import com.axiang.coolweather.model.County;
import com.axiang.coolweather.model.Province;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Administrator on 2017/1/20.
 */

public class AnalyticalData {

    //解析Province数据
    public static boolean analyticalProvinceData(String response, CoolWeatherDBOperate operate) {
        String[] provinces = response.split(",");
        if(provinces != null && provinces.length > 0) {
            for(String p : provinces) {
                if(!TextUtils.isEmpty(p)) {
                    String[] array = p.split("\\|");
                    Province province = new Province(array[1], array[0]);
                    operate.saveProvince(province);
                }
            }
            return true;
        }
        return false;
    }

    //解析City数据
    public static boolean analyticalCityData(String response, CoolWeatherDBOperate operate, int provinceId) {
        String[] cities = response.split(",");
        if(cities != null && cities.length > 0) {
            for(String c : cities) {
                if(!TextUtils.isEmpty(c)) {
                    String[] array = c.split("\\|");
                    City city = new City(array[1], array[0], provinceId);
                    operate.saveCity(city);
                }
            }
            return true;
        }
        return false;
    }

    //解析County数据
    public static boolean analyticalCountyData(String response, CoolWeatherDBOperate operate, int cityId) {
        String[] counties = response.split(",");
        if(counties != null && counties.length > 0) {
            for(String c : counties) {
                if(!TextUtils.isEmpty(c)) {
                    String[] array = c.split("\\|");
                    County county = new County(array[1], array[0], cityId);
                    operate.saveCounty(county);
                }
            }
            return true;
        }
        return false;
    }

    //解析Weather的JSON型数据并通过SharedPreferences存储到本地
    public static final void analyticalWeatherData(Context context, String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherObject = jsonObject.getJSONObject("weatherinfo");
            String cityName = weatherObject.getString("city");
            String weatherCode = weatherObject.getString("cityid");
            String temp1 = weatherObject.getString("temp1");
            String temp2 = weatherObject.getString("temp2");
            String weather = weatherObject.getString("weather");
            String ptime = weatherObject.getString("ptime");
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            String currentDate = new SimpleDateFormat("yyyy年MM月dd").format(new Date());
            editor.putBoolean("city_select", true);
            editor.putString("city_name", cityName);
            editor.putString("weather_code", weatherCode);
            editor.putString("temp1", temp1);
            editor.putString("temp2", temp2);
            editor.putString("weather", weather);
            editor.putString("ptime", ptime);
            editor.putString("current_time_content", currentDate);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
