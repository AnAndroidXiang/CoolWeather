package com.axiang.coolweather.util;

import android.text.TextUtils;

import com.axiang.coolweather.db.CoolWeatherDBOperate;
import com.axiang.coolweather.model.City;
import com.axiang.coolweather.model.County;
import com.axiang.coolweather.model.Province;

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

}
