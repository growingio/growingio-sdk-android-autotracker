/*
 *  Copyright (C) 2025 Beijing Yishu Technology Co., Ltd.
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
package com.growingio.android.platform;

import android.content.Context;

import com.growingio.android.platform.harmony.HarmonyHandler;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.middleware.platform.PlatformInfo;
import com.growingio.android.sdk.track.modelloader.DataFetcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PlatformDataFetcher implements DataFetcher<PlatformInfo> {

    private final TrackerContext context;
    private final PlatformConfig config;

    PlatformDataFetcher(TrackerContext context) {
        this.context = context;
        PlatformConfig config = context.getConfigurationProvider().getConfiguration(PlatformConfig.class);
        if (config == null) {
            this.config = new PlatformConfig();
        } else {
            this.config = config;
        }
    }

    @Override
    public PlatformInfo executeData() {
        String platform = null;
        String platformVersion = null;
        if (config.isHarmonyPlatformEnabled()) {
            platformVersion = HarmonyHandler.proceed();
            if (platformVersion != null) platform = HarmonyHandler.HARMONY;
        }

        String deviceType = null;
        if (config.isDeviceTypeCheckEnabled()) {
            deviceType = PlatformType.getDeviceType(context);
        }

        String gmsId = null;
        if (config.isGmsIdEnabled()) {
            try {
                gmsId = getGmsId();
            } catch (Exception ignored) {
            }
        }

        String firebaseId = null;
        if (config.isFirebaseIdEnabled()) {
            try {
                firebaseId = getFirebaseId();
            } catch (Exception ignored) {
            }
        }

        return new PlatformInfo(platform, platformVersion, deviceType, gmsId, firebaseId);
    }

    private String getGmsId() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = Class.forName("com.google.android.gms.iid.InstanceID");
        Method method = clazz.getDeclaredMethod("getInstance", Context.class);
        Object obj = method.invoke(null, context.getApplicationContext());
        Method method1 = obj.getClass().getMethod("getId");
        return (String) method1.invoke(obj);
    }

    private String getFirebaseId() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = Class.forName("com.google.firebase.iid.FirebaseInstanceId");
        Method method = clazz.getDeclaredMethod("getInstance");
        Object obj = method.invoke(null);
        Method method1 = obj.getClass().getMethod("getId");
        return (String) method1.invoke(obj);
    }

    @Override
    public Class<PlatformInfo> getDataClass() {
        return PlatformInfo.class;
    }
}
