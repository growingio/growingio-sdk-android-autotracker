/*
 * Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
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

import androidx.annotation.NonNull;

import com.growingio.android.sdk.track.TrackConfiguration;
import com.growingio.android.sdk.track.base.Configurable;
import com.growingio.android.sdk.track.utils.ConstantPool;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationProvider {
    private volatile TrackConfiguration mTrackConfiguration;
    private static final Map<Class<? extends Configurable>, Configurable> OTHER_CONFIGURATIONS = new HashMap();

    private static class SingleInstance {
        private static final ConfigurationProvider INSTANCE = new ConfigurationProvider();
    }

    private ConfigurationProvider() {

    }

    public static ConfigurationProvider get() {
        return SingleInstance.INSTANCE;
    }

    public boolean isDataCollectionEnabled() {
        return getTrackConfiguration().isDataCollectionEnabled();
    }

    public void addConfiguration(Configurable config) {
        if (config != null) {
            OTHER_CONFIGURATIONS.put(config.getClass(), config);
        }
    }

    public <T> T getConfiguration(Class<? extends Configurable> clazz) {
        return (T) OTHER_CONFIGURATIONS.get(clazz);
    }

    @NonNull
    public TrackConfiguration getTrackConfiguration() {
        if (mTrackConfiguration != null) {
            return mTrackConfiguration.clone();
        }
        return new TrackConfiguration(ConstantPool.UNKNOWN, ConstantPool.UNKNOWN);
    }

    public void setDataCollectionEnabled(boolean enabled) {
        if (mTrackConfiguration != null) {
            mTrackConfiguration.setDataCollectionEnabled(enabled);
        }
    }

    public void setTrackConfiguration(TrackConfiguration configuration) {
        if (mTrackConfiguration == null) {
            mTrackConfiguration = configuration.clone();
        }
    }
}
