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
package com.growingio.android.sdk.track.providers;

import android.text.TextUtils;

import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.listener.ListenerDispatcher;
import com.growingio.android.sdk.track.listener.OnConfigurationChangeListener;
import com.growingio.android.sdk.track.log.DebugLogger;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.ConstantPool;
import com.growingio.android.sdk.track.utils.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationProvider extends ListenerDispatcher<OnConfigurationChangeListener> implements TrackerLifecycleProvider {
    private static final String TAG = "ConfigurationProvider";
    private final CoreConfiguration mCoreConfiguration;
    private final Map<Class<? extends Configurable>, Configurable> sModuleConfigs = new HashMap<>();

    ConfigurationProvider(CoreConfiguration coreConfiguration, Map<Class<? extends Configurable>, Configurable> moduleConfigs) {
        if (TextUtils.isEmpty(coreConfiguration.getProjectId())) {
            throw new IllegalStateException("ProjectId is NULL");
        }

        if (TextUtils.isEmpty(coreConfiguration.getUrlScheme())) {
            throw new IllegalStateException("UrlScheme is NULL");
        }

        if (coreConfiguration.getProjectId().equals(ConstantPool.UNKNOWN)
                || coreConfiguration.getUrlScheme().equals(ConstantPool.UNKNOWN)) {
            Logger.e(TAG, "Growing Sdk 配置加载失败，请重新初始化");
            this.mCoreConfiguration = coreConfiguration;
            return;
        }

        if (!TrackMainThread.trackMain().runningOnUiThread()) {
            throw new IllegalStateException("Growing Sdk 初始化必须在主线程中调用。");
        }

        this.mCoreConfiguration = coreConfiguration;
        sModuleConfigs.clear();
        if (moduleConfigs != null) {
            sModuleConfigs.putAll(moduleConfigs);
        }
        if (coreConfiguration.isDebugEnabled()) {
            Logger.addLogger(new DebugLogger());
        }
        mListeners.clear();
    }

    public static ConfigurationProvider empty() {
        return new ConfigurationProvider(new CoreConfiguration(ConstantPool.UNKNOWN, ConstantPool.UNKNOWN), null);
    }

    @Override
    public void setup(TrackerContext context) {
        Logger.d(TAG, "Setup ConfigurationProvider");
    }

    public void addConfiguration(Configurable config) {
        if (config != null) {
            sModuleConfigs.put(config.getClass(), config);
        }
    }

    public String printAllConfigurationInfo() {
        StringBuilder info = new StringBuilder();
        if (!mCoreConfiguration.isDebugEnabled()) {
            return "GrowingSDK display config info only in debug environment.";
        }
        info.append(ObjectUtils.reflectToString(core()));
        for (Configurable configurable : sModuleConfigs.values()) {
            info.append(ObjectUtils.reflectToString(configurable));
        }
        return info.toString();
    }

    /**
     * @return module's configuration
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfiguration(Class<? extends Configurable> clazz) {
        return (T) sModuleConfigs.get(clazz);
    }


    public CoreConfiguration core() {
        return mCoreConfiguration;
    }

    public void addConfigurationListener(OnConfigurationChangeListener listener) {
        register(listener);
    }

    public void removeConfigurationListener(OnConfigurationChangeListener listener) {
        unregister(listener);
    }

    public void onDataCollectionChanged(boolean enable) {
        synchronized (mListeners) {
            for (OnConfigurationChangeListener listener : mListeners) {
                if (null != listener) {
                    listener.onDataCollectionChanged(enable);
                }
            }
        }
    }

    @Override
    public void shutdown() {

    }
}
