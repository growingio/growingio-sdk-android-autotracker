/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.advert;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.log.DebugLogger;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.advert.AdvertResult;
import com.growingio.android.sdk.track.middleware.http.EventResponse;
import com.growingio.android.sdk.track.middleware.http.EventUrl;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.LoadDataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.middleware.advert.DeepLink;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.utils.ThreadUtils;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * @author cpacm 2022/8/2
 */
public class AdvertDeepLinkDataLoader implements ModelLoader<DeepLink, AdvertResult> {
    @Override
    public LoadData<AdvertResult> buildLoadData(DeepLink deepLink) {
        return new LoadData<>(new DeepLinkDataFetcher(deepLink));
    }


    public static class Factory implements ModelLoaderFactory<DeepLink, AdvertResult> {
        @Override
        public ModelLoader<DeepLink, AdvertResult> build() {
            return new AdvertDeepLinkDataLoader();
        }
    }

    public static class DeepLinkDataFetcher implements DataFetcher<AdvertResult> {

        private final DeepLink deepLink;

        public DeepLinkDataFetcher(DeepLink deepLink) {
            this.deepLink = deepLink;
        }

        @Override
        public AdvertResult executeData() {
            Uri data = deepLink.getUri();
            AdvertResult result = new AdvertResult();
            Activity activity = ActivityStateProvider.get().getForegroundActivity();
            if (activity == null) return result;
            if (data.getScheme().startsWith("growing.")) {
                dealWithLink(data, activity, 0, false);
                result.setHasDealWithDeepLink(true);
                return result;
            }
            if (data.getHost() == null) {
                return result;
            }
            if (isDeepLinkUrl(null, data)) {
                dealWithLink(data, activity, 1, false);
                result.setHasDealWithDeepLink(true);
                return result;
            }
            return result;
        }

        /**
         * 中端不区分 DeepLink 和 AppLink ，大致流程是一样的，唯一的明显区别就是 AppLink 比 DeepLink 多请求一次网络数据
         */
        public void dealWithLink(Uri data, Context context, int type, boolean isInApp) {
            if (type == 0) { // ValidUrlEvent.DEEPLINK
                String openConsole = data.getQueryParameter("openConsoleLog");
                if (!TextUtils.isEmpty(openConsole)) {
                    if ("YES".equalsIgnoreCase(openConsole)) {
                        Logger.addLogger(new DebugLogger());
                    }
                }

                if (TextUtils.isEmpty(data.getQueryParameter("link_id"))) {
                    Logger.e(TAG, "onValidSchemaUrlIntent, but not found link_id, return");
                    return;
                }
                onParseDeeplinkArgs(data);
            } else if (type == 1) { //ValidUrlEvent.APPLINK
                if (TextUtils.isEmpty(data.getPath())) {
                    Logger.e(TAG, "onValidSchemaUrlIntent, but not valid applink, return");
                    return;
                }

                final long wakeTime = System.currentTimeMillis();
                // 已转长链的连接 依然可以通过 Applink 唤起，path匹配规则均为*
                if (TextUtils.isEmpty(data.getQueryParameter("link_id"))) {
                    requestDeepLinkParamsByTrackId(context, AdvertUtils.parseTrackerId(data.toString()), wakeTime, isInApp);
                } else {
                    onParseDeeplinkArgs(data);
                }

            }
        }

        void onParseDeeplinkArgs(Uri data) {
            AdvertData deeplinkInfo = new AdvertData();
            String dataUri = data.toString();
            Uri uri = Uri.parse(dataUri.replace("&amp;", "&"));
            deeplinkInfo.linkID = uri.getQueryParameter("link_id");
            deeplinkInfo.clickID = uri.getQueryParameter("click_id") != null ? uri.getQueryParameter("click_id") : "";
            deeplinkInfo.clickTM = uri.getQueryParameter("tm_click") != null ? uri.getQueryParameter("tm_click") : "";
            deeplinkInfo.customParams = uri.getQueryParameter("custom_params");
            deeplinkInfo.tm = System.currentTimeMillis();
            sendReengage(deeplinkInfo);

            final Map<String, String> params = new HashMap<>();
            int result = AdvertUtils.parseJson(deeplinkInfo.customParams,params);
            sendDeepLinkCallback(result, params, 0);
        }

        private boolean isDeepLinkUrl(String url, Uri uri) {
            if (uri == null) {
                if (url == null) {
                    return false;
                }
                uri = Uri.parse(url);
            }
            String host = uri.getHost();
            String scheme = uri.getScheme();
            if (host == null || scheme == null) return false;
            if (!"http".equals(scheme) && !"https".equals(scheme))
                return false;
            return "gio.ren".equals(host)
                    || "datayi.cn".equals(host)
                    || host.endsWith(".datayi.cn");
        }


        private void requestDeepLinkParamsByTrackId(Context context, String trackId, long wakeTime, boolean isInApp) {
            String url = AdvertUtils.getAppLinkParamsUrl(trackId, isInApp);
            EventUrl eventUrl = new EventUrl(url, System.currentTimeMillis())
                    .addHeader("ua", AdvertUtils.getUserAgent(context))
                    .addHeader("ip", AdvertUtils.getIP());
            TrackerContext.get().loadData(eventUrl, EventUrl.class, EventResponse.class, new LoadDataFetcher.DataCallback<EventResponse>() {
                @Override
                public void onDataReady(EventResponse data) {
                    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                        InputStream inputStream = data.getStream();
                        byte[] buffer = new byte[1024];
                        for (int len; (len = inputStream.read(buffer)) != -1; ) {
                            outputStream.write(buffer, 0, len);
                        }
                        onReceiveAppLinkArgs(outputStream.toString("UTF-8"), true, wakeTime);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onLoadFailed(Exception e) {
                    sendDeepLinkCallback(AdvertReceiveCallback.ERROR_NET_FAIL, null, wakeTime);
                }
            });
        }

        private void onReceiveAppLinkArgs(String body, boolean sendReengage, final long wakeTime) {
            final AdvertData info = new AdvertData();
            int errorCode = AdvertReceiveCallback.SUCCESS;
            try {
                JSONObject rep = new JSONObject(body);
                int code = rep.getInt("code");
                String msg = rep.optString("msg");

                if (code == 200) {
                    JSONObject data = rep.getJSONObject("data");
                    info.clickID = data.getString("click_id");
                    info.linkID = data.getString("link_id");
                    info.clickTM = data.getString("tm_click");
                    info.customParams = data.getString("custom_params");
                    info.tm = System.currentTimeMillis();
                    if (sendReengage) {
                        sendReengage(info);
                    }
                } else {
                    errorCode = code;
                    Logger.d(TAG, "onReceiveApplinkArgs returnCode error: ", errorCode, ": ", msg);
                }
            } catch (Exception e) {
                Logger.e(TAG, "parse the applink params error \n" + e.toString());
                errorCode = AdvertReceiveCallback.ERROR_EXCEPTION;
            }
            final Map<String, String> params;
            if (errorCode == AdvertReceiveCallback.SUCCESS) {
                params = new HashMap<>();
                errorCode = AdvertUtils.parseJson(info.customParams, params);
            } else {
                params = null;
            }
            final int finalErrorCode = errorCode;
            sendDeepLinkCallback(finalErrorCode, params, wakeTime);
        }

        private void sendReengage(AdvertData data) {
            if (TextUtils.isEmpty(data.customParams)) {
                data.customParams = "{}";
            }
            TrackMainThread.trackMain().postEventToTrackMain(new ReengageEvent.Builder().setAdvertData(data));
        }


        private void sendDeepLinkCallback(int finalErrorCode, Map<String, String> params, long wakeTime) {
            AdvertConfig advertConfig = ConfigurationProvider.get().getConfiguration(AdvertConfig.class);
            if (advertConfig == null) return;
            AdvertReceiveCallback callback = advertConfig.getReceiveCallback();
            ThreadUtils.runOnUiThread(() -> {
                if (callback != null) {
                    callback.onReceive(params, finalErrorCode, System.currentTimeMillis() - wakeTime);
                }
            });
        }

        @Override
        public Class<AdvertResult> getDataClass() {
            return AdvertResult.class;
        }

        private final static String TAG = "DeepLink";
    }


}
