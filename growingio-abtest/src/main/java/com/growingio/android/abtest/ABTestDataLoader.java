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
import android.util.Base64;

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
import com.growingio.android.sdk.track.providers.PersistentDataProvider;
import com.growingio.android.sdk.track.providers.UserInfoProvider;
import com.growingio.android.sdk.track.utils.ObjectUtils;
import com.growingio.android.snappy.XORUtils;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ABTest:
 * 1. ABTestConfig: Configure the parameters of ABTest, including the requested host and the validity period of ABTest data.（Request timeout follows okhttp request timeout）
 * 2. ABTest cache data will be stored in the sharedPreferences (dedicated file "growing_abtest"), keyed by device + login user + layerId,
 *    and the validity period of the cache data is a natural day. Expired entries are cleaned by a full scan at the first fetch.
 * 3. ABTest data is requested by sdk api: getABTest(layerId,callback), carrying obfuscated userId/userKey for user-level diversion.
 *
 * @author cpacm 2023/11/24
 */
public class ABTestDataLoader implements ModelLoader<ABTest, ABExperiment> {

    private static final String TAG = "ABTestDataLoader";

    // AB 独占的 SP 文件：与 growing_profile 隔离，文件边界即归属边界，初始化扫描无需判断 entry 归属
    static final String AB_TEST_PREF_NAME = "growing_abtest";

    private final TrackerContext context;
    // 每个 loader 实例只做一次全量清理
    private final AtomicBoolean cacheCleaned = new AtomicBoolean(false);

    public ABTestDataLoader(TrackerContext context) {
        this.context = context;
    }

    @Override
    public LoadData<ABExperiment> buildLoadData(ABTest abTest) {
        return new LoadData<>(new ABTestDataFetcher(context, abTest, cacheCleaned));
    }

    /**
     * 拆 key 后过期清理不能只靠惰性触发：某身份不再登录，其 entry 将永不被读取。
     * 首次 fetch 开头全量扫描，按既有的自然日规则清理（同线程先于缓存读取，无竞态；
     * 也不在构造线程上引入额外的磁盘 IO——SP 首次加载本就发生在 fetch 线程）。
     * 跨自然日的记录被清理后，重启后首次请求失败将直接返回失败，不再有 ABTEST_EXPIRED 兜底。
     */
    static void cleanExpiredCache(SharedPreferences sharedPreferences) {
        Map<String, ?> all = sharedPreferences.getAll();
        if (all.isEmpty()) return;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean changed = false;
        long now = System.currentTimeMillis();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            Object value = entry.getValue();
            long naturalDaytime = value instanceof String ? ABTestResponse.parseNaturalDaytime((String) value) : -1L;
            // 解析失败（-1）视作过期一并清理：该文件为 AB 独占，无误伤可能
            if (naturalDaytime < now) {
                editor.remove(entry.getKey());
                changed = true;
            }
        }
        if (changed) {
            editor.apply();
            Logger.d(TAG, "clean expired ABTestExperiment cache.");
        }
    }

    /**
     * 命中判定：与 iOS / Web 一致，experimentId 与 strategyId 均非空才视为命中实验。
     */
    static boolean isExperimentHit(ABExperiment abExperiment) {
        return abExperiment != null && abExperiment.getExperimentId() != 0 && abExperiment.getStrategyId() != 0;
    }

    /**
     * 缓存 key：登录身份并入 sha1，身份切换即 miss，无需在 value 中比对身份。
     * 分隔符防止不同字段拼接后撞值；未登录时 userId/userKey 归一为空串，与登录态自然区分。
     */
    static String cacheKey(String deviceId, String userId, String userKey, String layerId) {
        return ObjectUtils.sha1(deviceId + "\n"
                + (userId == null ? "" : userId) + "\n"
                + (userKey == null ? "" : userKey) + "\n"
                + layerId);
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
        private final PersistentDataProvider persistentDataProvider;
        private final UserInfoProvider userInfoProvider;
        private final SharedPreferences sharedPreferences;
        private final AtomicBoolean cacheCleaned;

        private ABTestConfig abTestConfig;
        private final ABTest abTest;

        @SuppressLint("WrongConstant")
        public ABTestDataFetcher(TrackerContext trackerContext, ABTest abTest, AtomicBoolean cacheCleaned) {
            this.trackerContext = trackerContext;
            this.deviceInfoProvider = trackerContext.getDeviceInfoProvider();
            this.persistentDataProvider = trackerContext.getProvider(PersistentDataProvider.class);
            this.userInfoProvider = trackerContext.getUserInfoProvider();
            this.abTestConfig = trackerContext.getConfigurationProvider().getConfiguration(ABTestConfig.class);
            if (abTestConfig == null) {
                abTestConfig = new ABTestConfig();
            }
            sharedPreferences = trackerContext.getSharedPreferences(AB_TEST_PREF_NAME, Context.MODE_PRIVATE);
            this.abTest = abTest;
            this.cacheCleaned = cacheCleaned;
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
            if (cacheCleaned.compareAndSet(false, true)) {
                cleanExpiredCache(sharedPreferences);
            }
            String deviceId = deviceInfoProvider.getDeviceId();
            String layerId = abTest.getLayerId();
            int timeout = (int) abTestConfig.getAbTestTimeout();
            boolean requestImmediately = abTest.isRequestImmediately();
            ABTestCallback abTestCallback = abTest.getAbTestCallback();
            String abTestKey = cacheKey(deviceId, userInfoProvider.getLoginUserId(), userInfoProvider.getLoginUserKey(), layerId);

            // 1. 缓存判定：只决定「TTL 内直接返回」与「lastCached 是什么」
            //    过期数据在任何失败路径下都不再返回（与 iOS 一致），仅用于成功后的 $exp_hit 去重比对
            ABExperiment lastCached = null;
            String abTestData = requestImmediately ? null : sharedPreferences.getString(abTestKey, null);
            ABTestResponse abCachedResponse = abTestData == null ? null : ABTestResponse.parseSavedJson(abTestData);
            if (abCachedResponse != null && abCachedResponse.getABExperiment() != null) {
                long now = System.currentTimeMillis();
                if (abCachedResponse.naturalDaytime < now) {
                    // 跨自然日：请求前先删，不作为兜底
                    Logger.d(TAG, "Remove ABTestExperiment cache when entering a new natural day.");
                    sharedPreferences.edit().remove(abTestKey).apply();
                } else if (abCachedResponse.expiredTime >= now) {
                    Logger.d(TAG, "get Cached ABTestExperiment when it has not expired.");
                    ABExperiment abExperiment = abCachedResponse.getABExperiment();
                    abTestCallback.onABExperimentReceived(abExperiment, ABTestCallback.ABTEST_CACHE);
                    return abExperiment;
                } else {
                    // 同日仅 TTL 过期：记录保留，仅用于去重比对
                    lastCached = abCachedResponse.getABExperiment();
                }
            }
            if (requestImmediately) {
                sharedPreferences.edit().remove(abTestKey).apply();
            }

            // 2. 请求
            Logger.d(TAG, "Request ABTestExperiment from server.");
            ABTestResponse abHttpResponse = requestABTestExperimentData(layerId, timeout);
            if (!abHttpResponse.isSucceed()) {
                Logger.e(TAG, "Request ABTestExperiment failed with error: " + abHttpResponse.getErrorMsg());
                abTestCallback.onABExperimentFailed(new IllegalAccessException(abHttpResponse.getErrorMsg()));
                return null;
            }

            // 3. 成功：去重上报、落盘、回调
            saveABExperiment(abTestKey, abHttpResponse);
            ABExperiment abExperiment = abHttpResponse.getABExperiment();
            if (lastCached == null || !abExperiment.equals(lastCached)) {
                Logger.d(TAG, "Send an ABExperiment event when it not equal cached data.");
                sendAbTestTrackEvent(abExperiment);
            }
            abTestCallback.onABExperimentReceived(abExperiment, ABTestCallback.ABTEST_HTTP);
            return abExperiment;
        }

        private void sendAbTestTrackEvent(ABExperiment abExperiment) {
            if (!isExperimentHit(abExperiment)) return;
            AttributesBuilder attributesBuilder = new AttributesBuilder();
            attributesBuilder
                    .addAttribute("$exp_id", abExperiment.getExperimentId())
                    .addAttribute("$exp_strategy_id", abExperiment.getStrategyId())
                    .addAttribute("$exp_layer_id", abExperiment.getLayerId())
                    .addAttribute("$exp_layer_name", abExperiment.getExpLayerName())
                    .addAttribute("$exp_name", abExperiment.getExpName())
                    .addAttribute("$exp_strategy_name", abExperiment.getExpStrategyName());
            CustomEvent.Builder customEventBuilder = new CustomEvent.Builder();
            customEventBuilder.setEventName("$exp_hit");
            customEventBuilder.setAttributes(attributesBuilder.build());
            TrackMainThread.trackMain().cacheEventToTrackMain(customEventBuilder);
        }


        /**
         * XOR + Base64 混淆用户标识，与事件通道 EncoderDataFetcher 同一套 stm & 0xFF 因子。
         * 注意这是混淆而非加密：因子明文挂在同一请求的 query 上，机密性由 HTTPS 保障。
         */
        private static String obfuscate(String value, long stm) {
            byte[] data = XORUtils.encrypt(value.getBytes(Charset.forName("UTF-8")), (int) (stm & 0xFF));
            return Base64.encodeToString(data, Base64.NO_WRAP);
        }

        private void saveABExperiment(String key, ABTestResponse abTestResponse) {
            if (abTestResponse.getABExperiment() == null) return;
            sharedPreferences.edit().putString(key, abTestResponse.toSavedJson()).apply();
        }

        private ABTestResponse requestABTestExperimentData(String layerId, int timeout) {
            //String path = "/diversion/specified-layer-variables";
            CoreConfiguration coreConfiguration = trackerContext.getConfigurationProvider().core();
            String host = abTestConfig.getAbTestServerHost();
            long expired = abTestConfig.getAbTestExpired();
            long stm = System.currentTimeMillis();
            EventUrl eventUrl = new EventUrl(host, stm)
                    .addPath("diversion")
                    .addPath("specified-layer-variables")
                    // 与事件请求语义一致：混淆因子 stm & 0xFF 同源挂在 query 上，服务端据此还原 body
                    .addParam("stm", String.valueOf(stm))
                    .setRequestMethod(EventUrl.POST)
                    .setCallTimeout(timeout)
                    .setMediaType("application/x-www-form-urlencoded");
            String sb = "accountId=" + Uri.encode(coreConfiguration.getProjectId()) +
                    "&datasourceId=" + Uri.encode(coreConfiguration.getDataSourceId()) +
                    "&distinctId=" + Uri.encode(deviceInfoProvider.getDeviceId()) +
                    "&layerId=" + Uri.encode(layerId);
            boolean isNewDevice = persistentDataProvider.isNewDevice();
            if (isNewDevice) {
                sb += "&newDevice=true";
            }
            // userKey 为空时不携带（idMappingEnabled == false 时 UserInfoProvider 已保证其恒为 null）
            String loginUserId = userInfoProvider.getLoginUserId();
            if (loginUserId != null && !loginUserId.isEmpty()) {
                sb += "&userId=" + Uri.encode(obfuscate(loginUserId, stm));
            }
            String loginUserKey = userInfoProvider.getLoginUserKey();
            if (loginUserKey != null && !loginUserKey.isEmpty()) {
                sb += "&userKey=" + Uri.encode(obfuscate(loginUserKey, stm));
            }
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
