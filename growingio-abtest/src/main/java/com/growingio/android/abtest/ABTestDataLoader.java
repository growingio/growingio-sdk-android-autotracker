/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.growingio.android.abtest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.AttributesBuilder;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.abtest.ABExperiment;
import com.growingio.android.sdk.track.middleware.abtest.ABTest;
import com.growingio.android.sdk.track.middleware.abtest.ABTestCallback;
import com.growingio.android.sdk.track.middleware.http.EventResponse;
import com.growingio.android.sdk.track.middleware.http.EventUrl;
import com.growingio.android.sdk.track.modelloader.LoadDataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.utils.ConstantPool;
import com.growingio.android.sdk.track.utils.ObjectUtils;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ABTest:
 * 1. ABTestConfig: Configure the parameters of ABTest, including the requested host and the validity period of ABTest data.（Request timeout follows okhttp request timeout）
 * 2. ABTest cache data will be stored in the sharedPreferences, and the validity period of the cache data is a natural day.
 * 3. ABTest data is requested by sdk api: getABTest(layerId,callback)
 *
 * @author cpacm 2023/11/24
 */
public class ABTestDataLoader implements ModelLoader<ABTest, ABExperiment> {

    private static final String TAG = "ABTestDataLoader";

    private final TrackerContext context;

    public ABTestDataLoader(TrackerContext context) {
        this.context = context;
    }

    @Override
    public LoadData<ABExperiment> buildLoadData(ABTest abTest) {
        return new LoadData<>(new ABTestDataFetcher(context, abTest));
    }


    public static class Factory implements ModelLoaderFactory<ABTest, ABExperiment> {
        private final TrackerContext context;

        public Factory(TrackerContext context) {
            this.context = context;
        }

        @Override
        public ModelLoader<ABTest, ABExperiment> build() {
            return new ABTestDataLoader(this.context);
        }
    }

    public static class ABTestDataFetcher implements LoadDataFetcher<ABExperiment> {

        private final TrackerContext trackerContext;
        private final DeviceInfoProvider deviceInfoProvider;
        private final SharedPreferences sharedPreferences;

        private ABTestConfig abTestConfig;
        private final ABTest abTest;

        @SuppressLint("WrongConstant")
        public ABTestDataFetcher(TrackerContext trackerContext, ABTest abTest) {
            this.trackerContext = trackerContext;
            this.deviceInfoProvider = trackerContext.getDeviceInfoProvider();
            this.abTestConfig = trackerContext.getConfigurationProvider().getConfiguration(ABTestConfig.class);
            if (abTestConfig == null) {
                abTestConfig = new ABTestConfig();
            }
            sharedPreferences = trackerContext.getSharedPreferences(ConstantPool.PREF_FILE_NAME, Context.MODE_PRIVATE);
            this.abTest = abTest;
        }

        @Override
        public void loadData(DataCallback<? super ABExperiment> callback) {
            ABExperiment abExperiment = executeData();
            if (abExperiment != null) {
                callback.onDataReady(abExperiment);
            } else {
                callback.onLoadFailed(new IllegalAccessException("Can't get ABTestExperiment."));
            }
        }

        @Override
        public ABExperiment executeData() {
            String deviceId = deviceInfoProvider.getDeviceId();
            String layerId = abTest.getLayerId();
            boolean requestImmediately = abTest.isRequestImmediately();
            ABTestCallback abTestCallback = abTest.getAbTestCallback();
            String abTestKey = ObjectUtils.sha1(deviceId + layerId);

            String abTestData = sharedPreferences.getString(abTestKey, null);
            if (!requestImmediately && abTestData != null) {
                ABTestResponse abCachedResponse = ABTestResponse.parseSavedJson(abTestData);
                if (abCachedResponse != null && abCachedResponse.getABExperiment() != null) {
                    Logger.d(TAG, "get ABTestExperiment from cache at first.");
                    if (abCachedResponse.expiredTime >= System.currentTimeMillis() && abCachedResponse.naturalDaytime >= System.currentTimeMillis()) {
                        Logger.d(TAG, "get Cached ABTestExperiment when it has not expired.");
                        ABExperiment abExperiment = abCachedResponse.getABExperiment();
                        abTestCallback.onABExperimentReceived(abExperiment, ABTestCallback.ABTEST_CACHE);
                        return abExperiment;
                    }

                    if (abCachedResponse.naturalDaytime < System.currentTimeMillis()) {
                        ABTestResponse abHttpResponse = requestABTestExperimentData(layerId);
                        if (abHttpResponse.isSucceed()) {
                            Logger.d(TAG, "Refresh ABTestExperiment when entering a new natural day. And send an ABExperiment event.");
                            saveABExperiment(abTestKey, abHttpResponse);
                            ABExperiment abExperiment = abHttpResponse.getABExperiment();
                            abTestCallback.onABExperimentReceived(abExperiment, ABTestCallback.ABTEST_HTTP);
                            sendAbTestTrackEvent(abExperiment);
                            return abExperiment;
                        } else {
                            Logger.d(TAG, "get Expired ABTestExperiment only once and delete it when refreshing data failed");
                            ABExperiment abExperiment = abCachedResponse.getABExperiment();
                            abTestCallback.onABExperimentReceived(abExperiment, ABTestCallback.ABTEST_EXPIRED);
                            sharedPreferences.edit().remove(abTestKey).apply();
                            return abExperiment;
                        }
                    }

                    if (abCachedResponse.expiredTime < System.currentTimeMillis()) {
                        ABTestResponse abHttpResponse = requestABTestExperimentData(layerId);
                        if (abHttpResponse.isSucceed()) {
                            Logger.d(TAG, "Refresh ABTestExperiment when it has expired.");
                            saveABExperiment(abTestKey, abHttpResponse);
                            ABExperiment abExperiment = abHttpResponse.getABExperiment();
                            if (!abExperiment.equals(abCachedResponse.getABExperiment())) {
                                Logger.d(TAG, "Send an ABExperiment event when it not equal cached data");
                                sendAbTestTrackEvent(abExperiment);
                            }
                            abTestCallback.onABExperimentReceived(abExperiment, ABTestCallback.ABTEST_HTTP);
                            return abExperiment;
                        } else {
                            Logger.d(TAG, "get Expired ABTestExperiment when refreshing data failed");
                            ABExperiment abExperiment = abCachedResponse.getABExperiment();
                            abTestCallback.onABExperimentReceived(abExperiment, ABTestCallback.ABTEST_EXPIRED);
                            return abExperiment;
                        }
                    }
                }
            }

            Logger.d(TAG, "No Cached ABTestExperiment or request immediately, request new data from server.");
            if (requestImmediately) sharedPreferences.edit().remove(abTestKey).apply();
            ABTestResponse abHttpResponse = requestABTestExperimentData(layerId);
            if (abHttpResponse.isSucceed()) {
                saveABExperiment(abTestKey, abHttpResponse);
                ABExperiment abExperiment = abHttpResponse.getABExperiment();
                sendAbTestTrackEvent(abExperiment);
                abTestCallback.onABExperimentReceived(abExperiment, ABTestCallback.ABTEST_HTTP);
                return abHttpResponse.getABExperiment();
            } else {
                Logger.e(TAG, "Request ABTestExperiment failed with error: " + abHttpResponse.getErrorMsg());
                abTestCallback.onABExperimentFailed(new IllegalAccessException(abHttpResponse.getErrorMsg()));
                return null;
            }
        }

        private void sendAbTestTrackEvent(ABExperiment abExperiment) {
            AttributesBuilder attributesBuilder = new AttributesBuilder();
            attributesBuilder
                    .addAttribute("$exp_id", abExperiment.getExperimentId())
                    .addAttribute("$exp_strategy_id", abExperiment.getStrategyId())
                    .addAttribute("$exp_layer_id", abExperiment.getLayerId());
            CustomEvent.Builder customEventBuilder = new CustomEvent.Builder();
            customEventBuilder.setEventName("$exp_hit");
            customEventBuilder.setCustomEventType(ConstantPool.CUSTOM_TYPE_SYSTEM);
            customEventBuilder.setAttributes(attributesBuilder.build());
            TrackMainThread.trackMain().cacheEventToTrackMain(customEventBuilder);
        }


        private void saveABExperiment(String key, ABTestResponse abTestResponse) {
            if (abTestResponse.getABExperiment() == null) return;
            sharedPreferences.edit().putString(key, abTestResponse.toSavedJson()).apply();
        }

        private ABTestResponse requestABTestExperimentData(String layerId) {
            //String path = "/diversion/specified-layer-variables";
            CoreConfiguration coreConfiguration = trackerContext.getConfigurationProvider().core();
            String host = abTestConfig.getAbTestServerHost();
            long expired = abTestConfig.getAbTestExpired();
            EventUrl eventUrl = new EventUrl(host, System.currentTimeMillis())
                    .addPath("diversion")
                    .addPath("specified-layer-variables")
                    .setRequestMethod(EventUrl.POST)
                    .setMediaType("application/x-www-form-urlencoded");
            String sb = "accountId=" + Uri.encode(coreConfiguration.getProjectId()) +
                    "&datasourceId=" + Uri.encode(coreConfiguration.getDataSourceId()) +
                    "&distinctId=" + Uri.encode(deviceInfoProvider.getDeviceId()) +
                    "&layerId=" + Uri.encode(layerId);
            eventUrl.setBodyData(sb.getBytes());
            EventResponse response = trackerContext.getRegistry().executeData(eventUrl, EventUrl.class, EventResponse.class);
            ABTestResponse outABTestResponse = new ABTestResponse();
            if (response.isSucceeded()) {
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    InputStream inputStream = response.getStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                    inputStream.close();
                    ABTestResponse abTestResponse = ABTestResponse.parseHttpJson(expired, layerId, outputStream.toString("UTF-8"));
                    if (!abTestResponse.isSucceed()) {
                        abTestResponse.setErrorMsg("ABExperiment data failed with:" + abTestResponse.getErrorMsg());
                    }
                    return abTestResponse;
                } catch (IOException e) {
                    Logger.e(TAG, e);
                    outABTestResponse.setErrorMsg("ABExperiment data IO failed with:" + e.getMessage());
                }
            } else {
                outABTestResponse.setErrorMsg("ABTest request failed with:" + eventUrl.toUrl());
            }
            return outABTestResponse;
        }

        @Override
        public Class<ABExperiment> getDataClass() {
            return ABExperiment.class;
        }
    }

}
