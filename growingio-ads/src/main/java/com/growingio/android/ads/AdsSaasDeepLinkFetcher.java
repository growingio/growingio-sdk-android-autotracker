/*
 *  Copyright (C) 2024 Beijing Yishu Technology Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.growingio.android.ads;

import static com.growingio.android.ads.AdsActivateDataLoader.DEEPLINK_TYPE_NONE;
import static com.growingio.android.ads.AdsActivateDataLoader.DEEPLINK_TYPE_SHORT;
import static com.growingio.android.ads.AdsActivateDataLoader.DEEPLINK_TYPE_URI;
import static com.growingio.android.ads.AdsUtils.SAAS_DEEPLINK_AD_HOST;
import static com.growingio.android.ads.AdsUtils.SAAS_DEEPLINK_AD_HOST_EXPIRED;
import static com.growingio.android.ads.AdsUtils.SAAS_LINK_ID;

import android.net.Uri;
import android.text.TextUtils;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.ActivateEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.ads.AdsResult;
import com.growingio.android.sdk.track.middleware.ads.DeepLinkCallback;
import com.growingio.android.sdk.track.middleware.http.EventResponse;
import com.growingio.android.sdk.track.middleware.http.EventUrl;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.LoadDataFetcher;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.providers.SessionProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

class AdsSaasDeepLinkFetcher implements DataFetcher<AdsResult> {

    private final Uri uri;
    private final DeepLinkCallback deepLinkCallback;
    private final Uri deepLinkHost;
    private final boolean isInApp;

    private final TrackerContext trackerContext;

    public AdsSaasDeepLinkFetcher(TrackerContext trackerContext, Uri uri, String adHost, DeepLinkCallback callback, boolean isInApp) {
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
        Logger.d(TAG, "step2. get deeplink type:" + checkDeepLinkType);
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

    static boolean isSaasDeepLink(Uri uri) {
        // check growing.927e02bbb39901c5://growing?link_id=...
        if (uri != null && uri.getHost().equals("growing") && uri.getQueryParameter(SAAS_LINK_ID) != null) {
            return true;
        }

        return isDeepLinkUrl(uri);
    }

    private static boolean isDeepLinkUrl(Uri uri) {
        // check https://datayi.cn/...
        String host = uri.getHost();
        String scheme = uri.getScheme();
        if (host == null || scheme == null) return false;
        if (!"http".equals(scheme) && !"https".equals(scheme)) return false;
        return SAAS_DEEPLINK_AD_HOST.equals(host) || SAAS_DEEPLINK_AD_HOST_EXPIRED.equals(host);
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
        deeplinkInfo.linkID = uri.getQueryParameter(AdsUtils.SAAS_LINK_ID);
        deeplinkInfo.clickID = uri.getQueryParameter(AdsUtils.SAAS_CLICK_ID) != null ? uri.getQueryParameter(AdsUtils.SAAS_CLICK_ID) : "";
        deeplinkInfo.clickTM = uri.getQueryParameter(AdsUtils.SAAS_CLICK_TIME) != null ? uri.getQueryParameter(AdsUtils.SAAS_CLICK_TIME) : "";
        deeplinkInfo.customParams = uri.getQueryParameter(AdsUtils.SAAS_PARAMS);
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
