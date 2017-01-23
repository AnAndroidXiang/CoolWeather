package com.axiang.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.axiang.coolweather.receiver.AutoUpdateReceiver;
import com.axiang.coolweather.util.AnalyticalData;
import com.axiang.coolweather.util.HttpCallbackListener;
import com.axiang.coolweather.util.HttpUtil;

/**
 * Created by Administrator on 2017/1/23.
 */

public class AutoUpdateService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int extendTime = 1000 * 60 * 60 * 8;    //8小时毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + extendTime;
        Intent i = new Intent(this, AutoUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    //更新天气信息
    private void updateWeather() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String address = "http://www.weather.com.cn/data/cityinfo/" +
                sharedPreferences.getString("weather_code", "") + ".html";
        HttpUtil.sendHttpResquest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                AnalyticalData.analyticalWeatherData(AutoUpdateService.this, response);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AutoUpdateService.this, "发生了未知错误", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
