package com.axiang.coolweather.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.axiang.coolweather.model.ActivityList;

/**
 * Created by Administrator on 2017/1/23.
 */

public class CoolWeatherManager extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        ActivityList.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityList.removeActivity(this);
    }
}
