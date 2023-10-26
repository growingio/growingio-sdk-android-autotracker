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
package com.growingio.android.ads;

import android.app.Activity;

import com.growingio.android.sdk.track.TrackMainThread;


import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.ActivateEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.ads.Activate;
import com.growingio.android.sdk.track.middleware.ads.AdsResult;
import com.growingio.android.sdk.track.middleware.ads.DeepLinkCallback;
import com.growingio.android.sdk.track.middleware.http.EventResponse;
import com.growingio.android.sdk.track.middleware.http.EventUrl;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.LoadDataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.providers.SessionProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 一起处理激活和deeplink的情况
 *
 * @author cpacm 2022/11/23
 */
public class AdsActivateDataLoader implements ModelLoader<Activate, AdsResult> {

    private final static int DEEPLINK_TYPE_NONE = 0; //doesn't contain deeplink.
    private final static int DEEPLINK_TYPE_URI = 1; //web jump,contains ads params.
    private final static int DEEPLINK_TYPE_SHORT = 2; //short scheme,doesn't contain data.


    private final TrackerContext context;

    public AdsActivateDataLoader(TrackerContext context) {
        this.context = context;
    }

    @Override
    public LoadData<AdsResult> buildLoadData(Activate activate) {
        ConfigurationProvider configurationProvider = context.getConfigurationProvider();
        AdsConfig config = configurationProvider.getConfiguration(AdsConfig.class);
        if (config == null) config = new AdsConfig();
        String deepLinkHost = config.getDeepLinkHost();
        if (deepLinkHost == null || deepLinkHost.isEmpty()) {
            deepLinkHost = configurationProvider.core().getDataCollectionServerHost();
        }
        DeepLinkCallback callback = config.getDeepLinkCallback();
        boolean isInApp = false;
        if (activate.getCallback() != null) {
            isInApp = true;
            callback = activate.getCallback();
        }
        return new LoadData<>(new ActivateDataFetcher(context, activate.getUri(), deepLinkHost, callback, isInApp));
    }


    public static class Factory implements ModelLoaderFactory<Activate, AdsResult> {
        private final TrackerContext context;

        public Factory(TrackerContext context) {
            this.context = context;
        }

        @Override
        public ModelLoader<Activate, AdsResult> build() {
            return new AdsActivateDataLoader(this.context);
        }
    }

    public static class ActivateDataFetcher implements DataFetcher<AdsResult> {

        private final Uri uri;
        private final DeepLinkCallback deepLinkCallback;
        private final Uri deepLinkHost;
        private final boolean isInApp;

        private TrackerContext trackerContext;

        public ActivateDataFetcher(TrackerContext trackerContext, Uri uri, String adHost, DeepLinkCallback callback, boolean isInApp) {
            this.trackerContext = trackerContext;
            this.deepLinkHost = Uri.parse(adHost);
            this.uri = uri;
            this.deepLinkCallback = callback;
            this.isInApp = isInApp;
        }

        @Override
        public AdsResult executeData() {
            AdsResult result = new AdsResult();
            Logger.d(TAG, "step1. start checkDeeplink type");
            int checkDeepLinkType = checkDeepLinkType();
            boolean isActivated = AdsUtils.isDeviceActivated();
            Logger.d(TAG, "step2. get deeplink type:" + checkDeepLinkType + " & device activate status:" + isActivated);
            // step3-1. uri not null and contain deeplink
            if (checkDeepLinkType != DEEPLINK_TYPE_NONE) {
                //tip: deeplink doesn't deal with activate
                Logger.d(TAG, "step3-1. deal with deeplink");
                if (checkDeepLinkType == DEEPLINK_TYPE_URI) {
                    result.setHasDealWithDeepLink(true);
                    Logger.d(TAG, "step 4-1. deal with uri link");
                    dealWithUriLink(uri);
                } else if (checkDeepLinkType == DEEPLINK_TYPE_SHORT) {
                    result.setHasDealWithDeepLink(true);
                    Logger.d(TAG, "step 4-2. deal with short url link");
                    dealWithShortLink(uri);
                }
            } else {
                Logger.d(TAG, "step 3-2. check clipboard if the deeplink data is included and send activate");
                if (!isActivated) activateDeviceWithClipBoard();
            }

            // step final. over
            Logger.d(TAG, "step final. over");
            return result;
        }

        private int checkDeepLinkType() {
            Uri data = uri;
            if (data != null) {
                if (data.getScheme() != null && data.getScheme().startsWith("growing.")) {
                    return DEEPLINK_TYPE_URI;
                }
                if (data.getHost() == null) {
                    return DEEPLINK_TYPE_NONE;
                }
                if (isDeepLinkUrl(data)) {
                    return DEEPLINK_TYPE_SHORT;
                }
            }
            return DEEPLINK_TYPE_NONE;
        }

        private boolean isDeepLinkUrl(Uri uri) {
            String host = uri.getHost();
            String scheme = uri.getScheme();
            if (host == null || scheme == null) return false;
            if (!"http".equals(scheme) && !"https".equals(scheme)) return false;
            return deepLinkHost.getHost().equals(host);
        }

        private void dealWithUriLink(Uri data) {
            if (TextUtils.isEmpty(data.getQueryParameter(AdsUtils.DEEP_LINK_ID))) {
                Logger.e(TAG, "onValidSchemaUrlIntent, but not found link_id, return");
                return;
            }
            onParseDeeplinkArgs(data);
        }

        private void dealWithShortLink(Uri data) {
            if (TextUtils.isEmpty(data.getPath())) {
                Logger.e(TAG, "onValidSchemaUrlIntent, but not valid applink, return");
                return;
            }
            final long wakeTime = System.currentTimeMillis();

            // 短链接Uri没有参数，需要请求接口获取参数。
            if (TextUtils.isEmpty(data.getQueryParameter(AdsUtils.DEEP_LINK_ID))) {
                TrackMainThread.trackMain().postActionToTrackMain(() ->
                        requestDeepLinkParamsByTrackId(AdsUtils.parseTrackerId(data.toString()), wakeTime, isInApp));
            } else {
                // 已转长链的连接 依然可以通过 Applink 唤起，path匹配规则均为*
                onParseDeeplinkArgs(data);
            }
        }

        void onParseDeeplinkArgs(Uri data) {
            AdsData deeplinkInfo = new AdsData();
            String dataUri = data.toString();
            Uri uri = Uri.parse(dataUri.replace("&amp;", "&"));
            deeplinkInfo.linkID = uri.getQueryParameter(AdsUtils.DEEP_LINK_ID);
            deeplinkInfo.clickID = uri.getQueryParameter(AdsUtils.DEEP_CLICK_ID) != null ? uri.getQueryParameter(AdsUtils.DEEP_CLICK_ID) : "";
            deeplinkInfo.clickTM = uri.getQueryParameter(AdsUtils.DEEP_CLICK_TIME) != null ? uri.getQueryParameter(AdsUtils.DEEP_CLICK_TIME) : "";
            deeplinkInfo.customParams = uri.getQueryParameter(AdsUtils.DEEP_PARAMS);
            deeplinkInfo.tm = System.currentTimeMillis();
            sendReengage(deeplinkInfo);

            final Map<String, String> params = new HashMap<>();
            int result = AdsUtils.parseJson(deeplinkInfo.customParams, params);
            sendDeepLinkCallback(result, params, 0);
        }

        private void sendDeepLinkCallback(int finalErrorCode, Map<String, String> params, long wakeTime) {
            if (deepLinkCallback == null) return;
            TrackMainThread.trackMain().runOnUiThread(() -> deepLinkCallback.onReceive(params, finalErrorCode, System.currentTimeMillis() - wakeTime));
        }

        private void requestDeepLinkParamsByTrackId(String trackId, long wakeTime, boolean isInApp) {
            ConfigurationProvider configurationProvider = trackerContext.getConfigurationProvider();
            DeviceInfoProvider deviceInfoProvider = trackerContext.getDeviceInfoProvider();
            String projectId = configurationProvider.core().getProjectId();
            String dataSourceId = configurationProvider.core().getDataSourceId();
            String deepType = isInApp ? "inapp" : "defer";
            String url = AdsUtils.getRequestDeepLinkUrl(deepLinkHost.toString(), deepType, projectId, dataSourceId, trackId);
            EventUrl eventUrl = new EventUrl(url, System.currentTimeMillis()).addHeader("User-Agent", deviceInfoProvider.getUserAgent());
            //.addHeader("ip", AdvertUtils.getIP());
            trackerContext.getRegistry().loadData(eventUrl, EventUrl.class, EventResponse.class, new LoadDataFetcher.DataCallback<EventResponse>() {
                @Override
                public void onDataReady(EventResponse data) {
                    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                        InputStream inputStream = data.getStream();
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, len);
                        }
                        AdsData adsData = AdsUtils.parseDeeplinkResponse(outputStream.toString("UTF-8"));
                        if (adsData.errorCode == DeepLinkCallback.SUCCESS) {
                            sendReengage(adsData);
                        }
                        sendDeepLinkCallback(adsData.errorCode, adsData.params, wakeTime);
                    } catch (IOException e) {
                        Logger.e(TAG, e);
                    }
                }

                @Override
                public void onLoadFailed(Exception e) {
                    sendDeepLinkCallback(DeepLinkCallback.ERROR_NET_FAIL, null, wakeTime);
                }
            });
        }

        @Override
        public Class<AdsResult> getDataClass() {
            return AdsResult.class;
        }


        private void activateDevice() {
            if (AdsUtils.isDeviceActivated()) {
                return;
            }
            Logger.d(TAG, "send activate event");
            // just send activate event
            final ActivateEvent.Builder builder = new ActivateEvent.Builder().
                    activate();
            sendEventToMain(builder);
            AdsUtils.setDeviceActivated();
        }

        /**
         * 发送激活事件(ui主线程)
         * 1. 剪贴板数据受隐私政策影响，支持隐私政策不申明时禁止读取剪切板数据
         * 2. 打开隐私政策后,发送设备激活事件，同时发送到用户自定义实现的 {@link DeepLinkCallback} 中
         */
        private void activateDeviceWithClipBoard() {
            ConfigurationProvider configurationProvider = trackerContext.getConfigurationProvider();
            AdsConfig config = configurationProvider.getConfiguration(AdsConfig.class);
            if (config != null && !config.isReadClipBoardEnable()) {
                activateDevice();
                return;
            }

            Activity activity = trackerContext.getActivityStateProvider().getForegroundActivity();
            //Android 10 限制剪切板获取时机，只有输入法或者焦点APP才有权限获取剪切板
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && activity != null) {
                activity.getWindow().getDecorView().post(this::checkClipBoardAndSendActivateEvent);
            } else {
                checkClipBoardAndSendActivateEvent();
            }
        }

        private void checkClipBoardAndSendActivateEvent() {
            ConfigurationProvider configurationProvider = trackerContext.getConfigurationProvider();
            String urlScheme = configurationProvider.core().getUrlScheme();
            final AdsData tempData = new AdsData();
            final boolean success = AdsUtils.checkClipBoard(trackerContext.getBaseContext(), tempData, urlScheme);
            if (success) {
                activateDeviceDefer(tempData);
                final Map<String, String> params = new HashMap<>();
                int result = AdsUtils.parseJson(tempData.customParams, params);
                sendDeepLinkCallback(result, params, 0);
                Logger.d(TAG, "用户通过延迟深度链接方式打开，收到参数准备传给 DeepLinkCallback");
            } else {
                Logger.d(TAG, "非延迟深度链接方式打开应用");
                // 若无最新的剪贴板信息，则加载上一次未发送的激活信息
                String data = AdsUtils.getActivateInfo();
                if (data != null && !TextUtils.isEmpty(data)) {
                    AdsData cacheData = AdsUtils.parseClipBoardInfo(data, urlScheme);
                    if (cacheData != null) tempData.copy(cacheData);
                    activateDeviceDefer(tempData);
                    final Map<String, String> params = new HashMap<>();
                    int result = AdsUtils.parseJson(tempData.customParams, params);
                    sendDeepLinkCallback(result, params, 0);
                } else {
                    // 普通的激活信息
                    activateDevice();
                }
            }
        }

        private void activateDeviceDefer(AdsData data) {
            if (AdsUtils.isDeviceActivated()) {
                return;
            }
            final ActivateEvent.Builder builder = new ActivateEvent.Builder()
                    .clipDefer()
                    .setAdvertData(data.linkID, data.clickID, data.clickTM, data.customParams);
            sendEventToMain(builder);
            AdsUtils.setDeviceActivated();
            AdsUtils.setActivateInfo(""); //清空剪贴板数据
        }

        private void sendReengage(AdsData data) {
            if (TextUtils.isEmpty(data.customParams)) {
                data.customParams = "{}";
            }
            sendEventToMain(new ActivateEvent.Builder().reengage(isInApp).setAdvertData(data.linkID, data.clickID, data.clickTM, data.customParams));
        }

        private final static String TAG = "AdvertModule";

        private void sendEventToMain(ActivateEvent.Builder eventBuilder) {
            SessionProvider sessionProvider = trackerContext.getProvider(SessionProvider.class);
            sessionProvider.checkSessionIntervalAndSendVisit();
            TrackMainThread.trackMain().postEventToTrackMain(eventBuilder);
        }

    }

}
