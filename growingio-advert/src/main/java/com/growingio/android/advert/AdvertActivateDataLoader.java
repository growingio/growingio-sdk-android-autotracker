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

import static android.content.Context.CLIPBOARD_SERVICE;

import static com.growingio.android.advert.AdvertUtils.parseJson;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Build;
import android.text.TextUtils;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.advert.Activate;
import com.growingio.android.sdk.track.middleware.advert.AdvertResult;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.utils.ThreadUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * @author cpacm 2022/8/2
 */
public class AdvertActivateDataLoader implements ModelLoader<Activate, AdvertResult> {
    @Override
    public LoadData<AdvertResult> buildLoadData(Activate activate) {
        return new LoadData<>(new ActivateDataFetcher(activate));
    }

    public static class Factory implements ModelLoaderFactory<Activate, AdvertResult> {
        @Override
        public ModelLoader<Activate, AdvertResult> build() {
            return new AdvertActivateDataLoader();
        }
    }

    public static class ActivateDataFetcher implements DataFetcher<AdvertResult> {

        private final Activate activate;

        public ActivateDataFetcher(Activate activate) {
            this.activate = activate;
        }

        @Override
        public AdvertResult executeData() {
            Activity activity = ActivityStateProvider.get().getForegroundActivity();
            if (activity != null) {
                checkActivateStatus(activity);
            }
            return new AdvertResult();
        }

        @Override
        public Class<AdvertResult> getDataClass() {
            return AdvertResult.class;
        }

        /**
         * 发送激活事件(ui主线程)
         * 1. 剪贴板数据受隐私政策影响，支持隐私政策不申明时禁止读取剪切板数据
         * 2. 打开隐私政策后,发送设备激活事件，同时发送到用户自定义实现的 {@link AdvertReceiveCallback} 中
         */
        private void checkActivateStatus(Activity activity) {
            if (!ConfigurationProvider.core().isDataCollectionEnabled()) {
                return;
            }

            if (AdvertUtils.isDeviceActivated()) {
                return;
            }

            // just send activate event
            submitActivateEvent(null);

//            AdvertConfig config = ConfigurationProvider.get().getConfiguration(AdvertConfig.class);
//            if (config != null && config.isReadClipBoardEnable()) {
//                //Android 10 限制剪切板获取时机，只有输入法或者焦点APP才有权限获取剪切板
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && activity != null) {
//                    activity.getWindow().getDecorView().post(this::checkClipBoardAndSendActivateEvent);
//                } else {
//                    checkClipBoardAndSendActivateEvent();
//                }
//            } else {
//                submitActivateEvent(null);
//            }
        }

        private void checkClipBoardAndSendActivateEvent() {
            final AdvertData tempData = new AdvertData();
            final boolean success = checkClipBoard(tempData);
            if (success) {
                sendInfoToDefferCallback(tempData);
                Logger.d(TAG, "用户通过延迟深度链接方式打开，收到参数准备传给 DeepLinkCallback");
            } else {
                Logger.d(TAG, "非延迟深度链接方式打开应用");
                // 若无最新的剪贴板信息，则加载上一次未发送的激活信息
                String data = AdvertUtils.getActivateInfo();
                if (data != null && !TextUtils.isEmpty(data)) {
                    AdvertData cacheData = parseClipBoardInfo(data);
                    if (cacheData != null) tempData.copy(cacheData);
                }
            }
            submitActivateEvent(tempData);
        }

        private void submitActivateEvent(AdvertData data) {
            if (data != null && data.tm != 0L) {
                //当deeplink不为空时补发reengage事件
                sendReengage(data);
            }
            //原有逻辑：若intent被消费，则不上传剪贴板的数据,即applink优先度高于activate
            //现有逻辑：一律上传剪贴板的数据。
            final ActivateEvent.Builder builder = new ActivateEvent.Builder().setAdvertData(data);
            TrackMainThread.trackMain().postEventToTrackMain(builder);
            AdvertUtils.setDeviceActivated();
            AdvertUtils.setActivateInfo(""); //清空剪贴板数据
        }

        private void sendReengage(AdvertData data) {
            if (TextUtils.isEmpty(data.customParams)) {
                data.customParams = "{}";
            }
            TrackMainThread.trackMain().postEventToTrackMain(new ReengageEvent.Builder().setAdvertData(data));
        }

        /**
         * 剪切板数据类型不做校验, 不同浏览器返回MIME类型不同
         * 1. 夸克返回 ClipDescription.MIMETYPE_TEXT_HTML
         * 2. 小米原生浏览器返回 ClipDescription.MIMETYPE_TEXT_PLAIN
         * <p>
         * 点击短链跳转下载后打开的 app ，期望剪贴板被前端落地页写入自定义参数等信息
         * wiki : https://growingio.atlassian.net/wiki/spaces/ads/pages/954336317
         * wiki : https://growingio.atlassian.net/wiki/spaces/ads/pages/942310739/Deferred+Deeplink
         * ZWSP : https://zh.wikipedia.org/wiki/%E9%9B%B6%E5%AE%BD%E7%A9%BA%E6%A0%BC
         *
         * @return true 是 Deffer 延迟深度链接，拿到了有效的剪贴板数据
         * false 是普通打开，剪贴板里没有广告组写进去的数据
         */
        boolean checkClipBoard(AdvertData info) {
            try {
                @SuppressLint("WrongConstant") ClipboardManager cm = (ClipboardManager) TrackerContext.get().getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = cm != null ? cm.getPrimaryClip() : null;
                if (clipData == null) return false;
                if (clipData.getItemCount() == 0) return false;
                ClipData.Item item = clipData.getItemAt(0);
                CharSequence charSequence = item.coerceToText(TrackerContext.get());
                if (charSequence == null || charSequence.length() == 0) return false;
                char zero = (char) 8204;
                StringBuilder binaryList = new StringBuilder();
                for (int i = 0; i < charSequence.length(); i++) {
                    binaryList.append(charSequence.charAt(i) == zero ? 0 : 1);
                }
                final int singleCharLength = 16;
                if (binaryList.length() % 16 != 0) {
                    return false;
                }
                ArrayList<String> bs = new ArrayList<>();
                int i = 0;
                while (i < binaryList.length()) {
                    bs.add(binaryList.substring(i, i + singleCharLength));
                    i += singleCharLength;
                }
                StringBuilder listString = new StringBuilder();
                for (String s : bs) {
                    listString.append((char) Integer.parseInt(s, 2));
                }

                String data = listString.toString();
                AdvertData advertData = parseClipBoardInfo(data);
                if (advertData != null) {
                    info.copy(advertData);
                    //保存剪切板数据
                    AdvertUtils.setActivateInfo(data);
                    //clean up the clip board
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        cm.clearPrimaryClip();
                    } else {
                        cm.setPrimaryClip(ClipData.newPlainText(null, null));
                    }
                    return true;
                } else {
                    return false;
                }

            } catch (Exception e) {
                Logger.e(TAG, e.toString());
                return false;
            }
        }

        /**
         * @param data json格式
         * @return 广告参数对象
         */
        AdvertData parseClipBoardInfo(String data) {
            try {
                JSONObject jsonObject = new JSONObject(data);
                if (!"gads".equals(jsonObject.getString("typ"))) {
                    Logger.e(TAG, "非剪贴板数据！");
                    return null;
                }
                if (!ConfigurationProvider.core().getUrlScheme().equals(jsonObject.getString("scheme"))) {
                    Logger.e(TAG, "非此应用的延迟深度链接， urlsheme 不匹配，期望为：" + ConfigurationProvider.core().getUrlScheme() + "， 实际为：" + jsonObject.getString("scheme"));
                    return null;
                }
                AdvertData info = new AdvertData();
                info.linkID = jsonObject.getString("link_id");
                info.clickID = jsonObject.getString("click_id");
                info.clickTM = jsonObject.getString("tm_click");
                JSONObject v1 = jsonObject.getJSONObject("v1");
                String customParams = v1.getString("custom_params");
                info.customParams = AdvertUtils.decode(customParams);
                info.tm = System.currentTimeMillis();

                return info;
            } catch (JSONException e) {
                Logger.e(TAG, "Clipboard 解析异常 ", e);
            }
            return null;
        }

        private void sendInfoToDefferCallback(AdvertData info) {
            if (info == null) return;
            AdvertConfig advertConfig = ConfigurationProvider.get().getConfiguration(AdvertConfig.class);
            if (advertConfig == null) return;
            AdvertReceiveCallback callback = advertConfig.getReceiveCallback();
            try {
                JSONObject params = new JSONObject(info.customParams);
                final Map<String, String> paramsMap = new HashMap<>();
                final int result = parseJson(params.toString(), paramsMap);
                ThreadUtils.runOnUiThread(() -> {
                    if (callback != null) {
                        callback.onReceive(paramsMap, result, 0);
                    }
                });
            } catch (JSONException e) {
                Logger.e(TAG, "deeplink info 解析异常 ", e);
            }
        }

        private final static String TAG = "Activate";

    }

}
