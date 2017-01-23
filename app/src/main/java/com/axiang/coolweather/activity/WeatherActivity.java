package com.axiang.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.axiang.coolweather.R;
import com.axiang.coolweather.util.AnalyticalData;
import com.axiang.coolweather.util.HttpCallbackListener;
import com.axiang.coolweather.util.HttpUtil;

/**
 * Created by Administrator on 2017/1/22.
 */

public class WeatherActivity extends Activity {

    private TextView title_county_name;     //头部县名
    private TextView text_ptime;            //发布时间
    private RelativeLayout weather_linear;    //天气详细信息布局
    private TextView temp1_content;         //最小热度
    private TextView temp2_content;         //最大热度
    private TextView weather_content;       //天气情况
    private TextView current_time_content;  //查询时间
    boolean isSelectFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        initView();
        String countyName = getIntent().getStringExtra("county_name");
        String weatherCode = getIntent().getStringExtra("weather_code");
        isSelectFlag = getIntent().getBooleanExtra("isSelectFlag", false);
        title_county_name.setText(countyName);
        text_ptime.setText("正在同步...");
        weather_linear.setVisibility(View.GONE);
        if(isSelectFlag) {
            showWeatherData();
        } else {
            String address = "http://www.weather.com.cn/data/list3/city" + weatherCode + ".xml";
            queryWeatherFromHttp(address, "County");
        }
    }

    private void initView() {
        title_county_name = (TextView) findViewById(R.id.title_county_name);
        text_ptime = (TextView) findViewById(R.id.text_ptime);
        weather_linear = (RelativeLayout) findViewById(R.id.weather_relative);
        temp1_content = (TextView) findViewById(R.id.temp1_content);
        temp2_content = (TextView) findViewById(R.id.temp2_content);
        weather_content = (TextView) findViewById(R.id.weather_content);
        current_time_content = (TextView) findViewById(R.id.current_time_content);
    }

    private void queryWeatherFromHttp(final String address, final String code) {
        HttpUtil.sendHttpResquest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if("County".equals(code)) {
                    String[] weathers = response.split("\\|");
                    if(weathers != null && weathers.length == 2) {
                        String url = "http://www.weather.com.cn/data/cityinfo/" + weathers[1] + ".html";
                        queryWeatherFromHttp(url, "Weather");
                    } else {
                        Toast.makeText(WeatherActivity.this, "发生了未知错误", Toast.LENGTH_SHORT).show();
                    }
                } else if("Weather".equals(code)) {
                    AnalyticalData.analyticalWeatherData(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeatherData();
                        }
                    });
                } else {
                    Toast.makeText(WeatherActivity.this, "发生了未知错误", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(WeatherActivity.this, "发生了未知错误", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showWeatherData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        title_county_name.setText(sharedPreferences.getString("city_name", ""));
        text_ptime.setText("今日 " + sharedPreferences.getString("ptime", "") + " 发布");
        temp1_content.setText(sharedPreferences.getString("temp1", ""));
        temp2_content.setText(sharedPreferences.getString("temp2", ""));
        weather_content.setText(sharedPreferences.getString("weather", ""));
        current_time_content.setText("查询时间: " + sharedPreferences.getString("current_time_content", ""));
        weather_linear.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if(isSelectFlag) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
