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
package com.growingio.android.sdk;

import android.content.Context;
import android.content.ContextWrapper;

import com.growingio.android.sdk.track.modelloader.TrackerRegistry;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.providers.TimingEventProvider;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;
import com.growingio.android.sdk.track.providers.UserInfoProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class TrackerContext extends ContextWrapper {
    private final static String TAG = "TrackerContext";
    private static volatile boolean sInitializedSuccessfully = false;

    public static boolean initializedSuccessfully() {
        return sInitializedSuccessfully;
    }

    public static void initSuccess() {
        sInitializedSuccessfully = true;
    }

    private final LinkedHashMap<Class<? extends TrackerLifecycleProvider>, TrackerLifecycleProvider> providerStore;

    public TrackerContext(Context context, Map<Class<? extends TrackerLifecycleProvider>, TrackerLifecycleProvider> providers) {
        super(context);
        registry = new TrackerRegistry();

        providerStore = new LinkedHashMap<>();
        providerStore.putAll(providers);
        providers.clear();
    }

    LinkedHashMap<Class<? extends TrackerLifecycleProvider>, TrackerLifecycleProvider> getProviderStore() {
        return providerStore;
    }

    void setup() {
        for (Class<? extends TrackerLifecycleProvider> key : providerStore.keySet()) {
            TrackerLifecycleProvider provider = providerStore.get(key);
            provider.setup(this);
        }
        sInitializedSuccessfully = true;
    }

    void shutdown() {
        sInitializedSuccessfully = false;
        for (Class<? extends TrackerLifecycleProvider> key : providerStore.keySet()) {
            TrackerLifecycleProvider provider = providerStore.get(key);
            provider.shutdown();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getProvider(Class<? extends TrackerLifecycleProvider> clazz) {
        return (T) providerStore.get(clazz);
    }

    public ActivityStateProvider getActivityStateProvider() {
        return getProvider(ActivityStateProvider.class);
    }

    public ConfigurationProvider getConfigurationProvider() {
        ConfigurationProvider configurationProvider = getProvider(ConfigurationProvider.class);
        if (configurationProvider == null) return ConfigurationProvider.empty();
        return configurationProvider;
    }

    public DeviceInfoProvider getDeviceInfoProvider() {
        return getProvider(DeviceInfoProvider.class);
    }

    public UserInfoProvider getUserInfoProvider() {
        return getProvider(UserInfoProvider.class);
    }

    public TimingEventProvider getTimingEventProvider() {
        return getProvider(TimingEventProvider.class);
    }

    private final TrackerRegistry registry;

    public TrackerRegistry getRegistry() {
        return registry;
    }
}
