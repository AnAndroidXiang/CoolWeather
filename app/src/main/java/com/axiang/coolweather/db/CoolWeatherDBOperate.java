package com.axiang.coolweather.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.axiang.coolweather.model.City;
import com.axiang.coolweather.model.County;
import com.axiang.coolweather.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/16.
 */

public class CoolWeatherDBOperate {

    private static CoolWeatherDatabase coolWeatherDatabase;
    private static CoolWeatherDBOperate operate;
    private static SQLiteDatabase db;

    public static final int VERSION = 1;

    private CoolWeatherDBOperate(Context context) {
        coolWeatherDatabase = new CoolWeatherDatabase(context, "CoolWeather", null, VERSION);
        db = coolWeatherDatabase.getReadableDatabase();
    }

    public synchronized static CoolWeatherDBOperate getDb(Context context) {
        if(operate == null) {
            operate = new CoolWeatherDBOperate(context);
        }
        return operate;
    }

    public void saveProvince(Province province) {    //将Province实例存进数据库
        String sql = "insert into Province(province_name, province_code) values(?,?)";
        db.execSQL(sql, new String[] {province.getProvinceName(), province.getProvinceCode()});
    }

    public List<Province> takeProvinces() {     //取出数据库中的所有Province
        String sql = "select id, province_name, province_code from Province";
        Cursor cursor = db.rawQuery(sql, null);
        List<Province> provinceList = new ArrayList<Province>();
        if(cursor.moveToFirst()) {
            do{
                Province province = new Province(cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("province_name")),
                                cursor.getString(cursor.getColumnIndex("province_code")));
                provinceList.add(province);
            } while (cursor.moveToNext());
        }
        return provinceList;
    }

    public void saveCity(City city) {    //将City实例存进数据库
        String sql = "insert into City(city_name, city_code, province_id) values(?, ?, ?)";
        db.execSQL(sql, new String[] {city.getCityName(), city.getCityCode(), String.valueOf(city.getProvinceId())});
    }

    public List<City> takeCities(int provinceId) {     //取出数据库中所有对应相同provinceId的City
        String sql = "select id, city_name, city_code from City where province_id=?";
        Cursor cursor = db.rawQuery(sql, new String[] {String.valueOf(provinceId)});
        List<City> cityList = new ArrayList<City>();
        if(cursor.moveToFirst()) {
            do{
                City city = new City(cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("city_name")),
                        cursor.getString(cursor.getColumnIndex("city_code")), provinceId);
                cityList.add(city);
            } while (cursor.moveToNext());
        }
        return cityList;
    }

    public void saveCounty(County county) {    //将County实例存进数据库
        String sql = "insert into County(county_name, county_code, city_id) values(?, ?, ?)";
        db.execSQL(sql, new String[] {county.getCountyName(), county.getCountyCode(),
                String.valueOf(county.getCityId())});
    }

    public List<County> takeCounties(int cityId) {     //取出数据库中所有对应相同cityId的County
        String sql = "select id, county_name, county_code from County where city_id=?";
        Cursor cursor = db.rawQuery(sql, new String[] {String.valueOf(cityId)});
        List<County> countyList = new ArrayList<County>();
        if(cursor.moveToFirst()) {
            do{
                County county = new County(cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("county_name")),
                        cursor.getString(cursor.getColumnIndex("county_code")), cityId);
                countyList.add(county);
            } while (cursor.moveToNext());
        }
        return countyList;
    }

}
