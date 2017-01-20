package com.axiang.coolweather.util;

/**
 * Created by Administrator on 2017/1/16.
 */

public interface HttpCallbackListener {

    void onFinish(String response);
    void onError(Exception e);

}
