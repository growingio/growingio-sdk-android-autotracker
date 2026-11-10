/*
 * Copyright (C) 2026 Beijing Yishu Technology Co., Ltd.
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
package com.growingio.android.sdk.track.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.CdpConfig;

public class ForceLoginProvider implements TrackerLifecycleProvider {
    private static final String TAG = "ForceLoginProvider";
    private static final String PREFERENCES_NAME = "growing3_force_login";
    private static final String KEY_IDENTIFY_DEVICE_ID = "identify_device_id";
    private static final int MAX_DEVICE_ID_LENGTH = 1000;

    private final Object lock = new Object();
    private ConfigurationProvider configurationProvider;
    private SharedPreferences sharedPreferences;
    private String identifiedDeviceId = "";
    private boolean unlocked = false;

    @Override
    public void setup(TrackerContext context) {
        configurationProvider = context.getConfigurationProvider();
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        synchronized (lock) {
            identifiedDeviceId = sharedPreferences.getString(KEY_IDENTIFY_DEVICE_ID, "");
            unlocked = false;
        }
    }

    @Override
    public void shutdown() {
        synchronized (lock) {
            identifiedDeviceId = "";
            unlocked = false;
        }
    }

    /**
     * 与 Web SDK 校验规则一致：非空即有效，不去除首尾空白，超过 1000 字符截断（不拆开代理对）；每次启动仅首次有效调用生效。
     */
    public boolean identify(String deviceId) {
        if (!isForceLoginEnabled()) {
            Logger.i(TAG, "Failed to identify, only available when NewSaaS forceLogin is enabled");
            return false;
        }
        if (TextUtils.isEmpty(deviceId)) {
            Logger.i(TAG, "Failed to identify, deviceId is empty");
            return false;
        }

        String identified = truncateDeviceId(deviceId);
        synchronized (lock) {
            if (unlocked) {
                Logger.i(TAG, "Failed to identify, deviceId has already been set in this launch");
                return false;
            }
            identifiedDeviceId = identified;
            unlocked = true;
            sharedPreferences.edit().putString(KEY_IDENTIFY_DEVICE_ID, identified).apply();
        }
        Logger.i(TAG, "Identify deviceId for forced login");
        return true;
    }

    /**
     * 超过 1000 个 UTF-16 单元时截断；第 1000 个单元是代理对高位时在其前截断，不拆开 emoji 等代理对字符。
     */
    static String truncateDeviceId(String deviceId) {
        if (deviceId.length() <= MAX_DEVICE_ID_LENGTH) {
            return deviceId;
        }
        int end = MAX_DEVICE_ID_LENGTH;
        if (Character.isHighSurrogate(deviceId.charAt(end - 1))) {
            end--;
        }
        return deviceId.substring(0, end);
    }

    /**
     * 未开启强制登录（或非 NewSaaS），或本次启动已 identify 时为 true。
     */
    public boolean canSend() {
        if (!isForceLoginEnabled()) {
            return true;
        }
        synchronized (lock) {
            return unlocked;
        }
    }

    /**
     * 当前构建的事件是否需要在上传时替换 deviceId（即 !canSend）。
     * 待替换事件入库时 deviceId 为自动生成的设备 ID，并在数据库 _force_login_pending 列记 1。
     */
    public boolean isPending() {
        return !canSend();
    }

    /**
     * 当前有效设备 ID：NewSaaS 下已持久化的访问用户 ID，否则为自动生成的设备 ID。
     */
    public String getDeviceId(String generatedDeviceId) {
        if (!isNewSaaS()) {
            return generatedDeviceId;
        }
        synchronized (lock) {
            return TextUtils.isEmpty(identifiedDeviceId) ? generatedDeviceId : identifiedDeviceId;
        }
    }

    /**
     * 构建事件使用的设备 ID：待替换时为自动生成的设备 ID（与老版本 SDK 一致，降级后按设备 ID 上报），否则为当前有效设备 ID。
     */
    public String getEventDeviceId(String generatedDeviceId) {
        if (isPending()) {
            return generatedDeviceId;
        }
        return getDeviceId(generatedDeviceId);
    }

    /**
     * 上传时替换待替换事件 deviceId 所用的值；非 NewSaaS 不会产生待替换事件，返回空字符串表示无需替换。
     */
    public String getPendingReplacement(String generatedDeviceId) {
        if (!isNewSaaS()) {
            return "";
        }
        return getDeviceId(generatedDeviceId);
    }

    private boolean isForceLoginEnabled() {
        return isNewSaaS() && configurationProvider.core().isForceLogin();
    }

    private boolean isNewSaaS() {
        return configurationProvider != null && configurationProvider.getConfiguration(CdpConfig.class) == null;
    }
}
