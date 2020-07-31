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

package com.growingio.android.sdk.track.utils;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于提供GIO的统一配置
 */
public class GIOProviders {

    private static final ConcurrentHashMap<Class<?>, Object> PROVIDERS = new ConcurrentHashMap<>();

    private GIOProviders() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T provider(@NonNull Class<T> type, @NonNull DefaultCallback<T> callback) {
        if (PROVIDERS.containsKey(type)) {
            return (T) PROVIDERS.get(type);
        }
        synchronized (GIOProviders.class) {
            if (PROVIDERS.containsKey(type)) {
                return (T) PROVIDERS.get(type);
            }
            T value = callback.value();
            if (value != null) {
                PROVIDERS.put(type, value);
            }
            return value;
        }
    }

    public static <T> T provider(final Class<T> interfaceType) {
        return provider(interfaceType, new DefaultCallback<T>() {
            @Override
            public T value() {
                return null;
            }
        });
    }

    public static void update(@NonNull Class<?> key, @Nullable Object value) {
        if (value == null) {
            PROVIDERS.remove(key);
        } else {
            PROVIDERS.put(key, value);
        }
    }

    public interface DefaultCallback<D> {
        D value();
    }
}
