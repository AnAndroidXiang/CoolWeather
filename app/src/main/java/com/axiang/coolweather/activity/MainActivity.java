package com.axiang.coolweather.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.axiang.coolweather.R;
import com.axiang.coolweather.db.CoolWeatherDBOperate;
import com.axiang.coolweather.model.ActivityList;
import com.axiang.coolweather.model.City;
import com.axiang.coolweather.model.County;
import com.axiang.coolweather.model.Province;
import com.axiang.coolweather.util.AnalyticalData;
import com.axiang.coolweather.util.HttpCallbackListener;
import com.axiang.coolweather.util.HttpUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends CoolWeatherManager {

    private ListView content_list;
    private TextView title_text;
    private List<String> dataList = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private ProgressDialog progressDialog;

    private CoolWeatherDBOperate operate;   //数据库操作

    private List<Province> provinceList;
    private Province selectProvince;        //选中的Province

    private List<City> cityList;
    private City selectCity;        //选中的City

    private List<County> countyList;
    private County selectCounty;        //选中的County

    public static final int LEVEL_PROVINCE = 0; //Province界面
    public static final int LEVEL_CITY = 1;     //City界面
    public static final int LEVEL_COUNTY = 2;   //County界面
    private int currentLevel;   //当前所在界面

    //退出程序
    private boolean isExit = false;
    public static final int ISEXIT = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == ISEXIT) {
                isExit = false;
            }
        }
    };

    public static int i = 0;        //本地是否有过查询天气记录并且刚打开程序
    private boolean isWeatherCome;  //是否从WeatherActivity跳转来的

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(i == 0) {
            i++;
            jumpActivity();
        }
        setContentView(R.layout.activity_main);
        isWeatherCome = getIntent().getBooleanExtra("isWeatherCome", false);
        initView();
    }

    private void initView() {
        content_list = (ListView) findViewById(R.id.content_list);
        title_text = (TextView) findViewById(R.id.title_text);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        content_list.setAdapter(adapter);
        content_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE) {
                    selectProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    selectCounty = countyList.get(position);
                    Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
                    intent.putExtra("county_name", selectCounty.getCountyName());
                    intent.putExtra("weather_code", selectCounty.getCountyCode());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "发生了未知错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
        operate = CoolWeatherDBOperate.getDb(this);
        queryProvinces();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void queryProvinces() {     //通过数据库（如果数据库没有再通过Http查询存进数据库）查询所有的Province
        provinceList = operate.takeProvinces();
        if(!provinceList.isEmpty()) {
            dataList.clear();
            for(Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            title_text.setText("中国");
            adapter.notifyDataSetChanged();
            content_list.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryDataFromHttp(null, "Province");
        }
    }

    private void queryCities() {        //通过数据库（如果数据库没有再通过Http查询存进数据库）查询所有的City
        cityList = operate.takeCities(selectProvince.getId());
        if(!cityList.isEmpty()) {
            dataList.clear();
            for(City city : cityList) {
                dataList.add(city.getCityName());
            }
            title_text.setText(selectProvince.getProvinceName());
            adapter.notifyDataSetChanged();
            content_list.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            queryDataFromHttp(selectProvince.getProvinceCode(), "City");
        }
    }

    private void queryCounties() {        //通过数据库（如果数据库没有再通过Http查询存进数据库）查询所有的County
        countyList = operate.takeCounties(selectCity.getId());
        if(!countyList.isEmpty()) {
            dataList.clear();
            for(County county : countyList) {
                dataList.add(county.getCountyName());
            }
            title_text.setText(selectCity.getCityName());
            adapter.notifyDataSetChanged();
            content_list.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            queryDataFromHttp(selectCity.getCityCode(), "County");
        }
    }

    private void queryDataFromHttp(String code, final String type) {
        String address;
        if(!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpResquest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if("Province".equals(type)) {
                    result = AnalyticalData.analyticalProvinceData(response, operate);
                } else if("City".equals(type)) {
                    result = AnalyticalData.analyticalCityData(response, operate, selectProvince.getId());
                } else if("County".equals(type)) {
                    result = AnalyticalData.analyticalCountyData(response, operate, selectCity.getId());
                } else {
                    Toast.makeText(MainActivity.this, "发生了未知错误", Toast.LENGTH_SHORT).show();
                }
                if(result) {
                    runOnUiThread(new Runnable() {  //方法回到主线程中去执行，不然无法更新界面
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("Province".equals(type)) {
                                queryProvinces();
                            } else if("City".equals(type)) {
                                queryCities();
                            } else if("County".equals(type)) {
                                queryCounties();
                            } else {
                                Toast.makeText(MainActivity.this, "发生了未知错误", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this, "发生了未知错误", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgressDialog() {
        if(progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void jumpActivity() {  //跳转到WeahterAcitivity
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isSelectFlag = sharedPreferences.getBoolean("city_select", false);
        if(isSelectFlag) {
            Intent intent = new Intent(this, WeatherActivity.class);
            intent.putExtra("isSelectFlag", isSelectFlag);
            intent.putExtra("county_name", sharedPreferences.getString("city_name", ""));
            intent.putExtra("weather_code", sharedPreferences.getString("weather_code", ""));
            startActivity(intent);
            finish();
            return;
        }
    }

    @Override
    public void onBackPressed() {
        if(currentLevel == LEVEL_CITY) {
            queryProvinces();
        } else if (currentLevel == LEVEL_COUNTY) {
            queryCities();
        } else {
            if(!isWeatherCome) {
                if (!isExit) {
                    isExit = true;
                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    handler.sendEmptyMessageDelayed(ISEXIT, 2000);
                } else {
                    ActivityList.deleteAllActivity();
                }
            } else {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                jumpActivity();
            }
        }
    }
}
