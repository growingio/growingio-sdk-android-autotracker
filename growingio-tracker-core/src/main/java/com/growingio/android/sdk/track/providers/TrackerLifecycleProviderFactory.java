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

import android.content.Context;

import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.CoreConfiguration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 *
 * @author cpacm 2023/7/25
 */
public class TrackerLifecycleProviderFactory {

    private static volatile TrackerLifecycleProviderFactory instance;

    public static TrackerLifecycleProviderFactory create() {
        if (instance == null) {
            instance = new TrackerLifecycleProviderFactory();
        }
        return instance;
    }

    private final Map<Class<? extends TrackerLifecycleProvider>, TrackerLifecycleProvider> providerStore;

    private TrackerLifecycleProviderFactory() {
        providerStore = new LinkedHashMap<>();
    }

    public Map<Class<? extends TrackerLifecycleProvider>, TrackerLifecycleProvider> providers() {
        providerStore.put(AppInfoProvider.class, new AppInfoProvider());
        providerStore.put(DeviceInfoProvider.class, new DeviceInfoProvider());

        providerStore.put(DeepLinkProvider.class, new DeepLinkProvider());
        providerStore.put(SessionProvider.class, new SessionProvider());

        providerStore.put(EventBuilderProvider.class, new EventBuilderProvider());
        providerStore.put(TimingEventProvider.class, new TimingEventProvider());
        providerStore.put(UserInfoProvider.class, new UserInfoProvider());

        return providerStore;
    }

    private void addProvider(Class<? extends TrackerLifecycleProvider> clazz, TrackerLifecycleProvider provider) {
        if (provider != null) {
            providerStore.put(clazz, provider);
        }
    }

    public void createPersistentDataProvider(Context context) {
        providerStore.put(PersistentDataProvider.class, new PersistentDataProvider(context));
    }

    public void createActivityStateProvider(Context context) {
        providerStore.put(ActivityStateProvider.class, new ActivityStateProvider(context));
    }

    public void createConfigurationProviderWithConfig(CoreConfiguration coreConfiguration, Map<Class<? extends Configurable>, Configurable> moduleConfigs) {
        providerStore.put(ConfigurationProvider.class, new ConfigurationProvider(coreConfiguration, moduleConfigs));
    }

    public static Map<Class<? extends TrackerLifecycleProvider>, TrackerLifecycleProvider> emptyMap() {
        return new HashMap<>();
    }
}
