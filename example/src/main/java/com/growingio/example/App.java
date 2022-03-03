package com.growingio.example;

import android.app.Application;

import com.growingio.android.sdk.autotrack.CdpAutotrackConfiguration;
import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.growingio.android.sdk.track.events.helper.EventExcludeFilter;
import com.growingio.android.sdk.track.events.helper.FieldIgnoreFilter;


public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        GrowingAutotracker.startWithConfiguration(this, new CdpAutotrackConfiguration("bc675c65b3b0290e", "growing.47d2b990025d67f5")
                // 设置服务器信息
                // https://growingio.github.io/growingio-sdk-docs/docs/android/base/Configuration#1-sdk%E5%BF%85%E9%9C%80%E5%8F%82%E6%95%B0
                .setDataSourceId("939c0b26233d3ed1")
                .setDataCollectionServerHost("https://cdp-api.growingio.com")
                // 设置应用分发渠道
                // https://growingio.github.io/growingio-sdk-docs/docs/android/base/Configuration#2-channel
                .setChannel("华为应用商店")
                // 设置是否是debug模式
                // https://growingio.github.io/growingio-sdk-docs/docs/android/base/Configuration#3-setdebugenabled
                .setDebugEnabled(true)
                // 设置流量限制，单位M
                // setCellularDataLimit
                .setCellularDataLimit(10)
                // 设置批量发送事件间隔，单位s
                // https://growingio.github.io/growingio-sdk-docs/docs/android/base/Configuration#5-setdatauploadinterval
                .setDataUploadInterval(15)
                // 设置后台会话最长停留时长，单位s
                // https://growingio.github.io/growingio-sdk-docs/docs/android/base/Configuration#6-setsessioninterval
                .setSessionInterval(30)
                // 是否获取进程号，用于判断是否是同一个应用的进程
                // https://growingio.github.io/growingio-sdk-docs/docs/android/base/Configuration#%E9%85%8D%E7%BD%AE%E8%A1%A8%E6%A0%BC
                .setRequireAppProcessesEnabled(false)
                // 是否开启采集，默认开启采集
                // https://growingio.github.io/growingio-sdk-docs/docs/android/base/Configuration#7-setdatacollectionenabled
                .setDataCollectionEnabled(true)
                // 不忽略采集事件，可以配置不需要采集的事件
                // https://growingio.github.io/growingio-sdk-docs/docs/android/base/Configuration#8-setexcludeevent
                .setExcludeEvent(EventExcludeFilter.NONE)
                // 不忽略采集字段，可以配置不需要采集的字段
                // https://growingio.github.io/growingio-sdk-docs/docs/android/base/Configuration#9-setignorefield
                .setIgnoreField(FieldIgnoreFilter.NONE)
                // 设置是否支持设置用户类型
                // https://growingio.github.io/growingio-sdk-docs/docs/android/base/Configuration#10-setidmappingenabled
                .setIdMappingEnabled(true)
                // 设置曝光比例，对应采集view的曝光事件
                // https://growingio.github.io/growingio-sdk-docs/docs/android/base/Configuration#11-setimpressionscale
                .setImpressionScale(1)
                // 用户自定义数据加密模块、网络传输模块等
                // https://growingio.github.io/growingio-sdk-docs/docs/android/base/Configuration#3-sdk%E6%95%B0%E6%8D%AE%E5%8A%A0%E5%AF%86%E4%BC%A0%E8%BE%93
                // .addPreloadComponent()
        );
    }
}
