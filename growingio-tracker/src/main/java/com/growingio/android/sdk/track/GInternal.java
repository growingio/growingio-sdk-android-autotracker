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

package com.growingio.android.sdk.track;


import android.support.annotation.Nullable;

import com.growingio.android.sdk.track.interfaces.IGrowingTracker;
import com.growingio.android.sdk.track.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * GrowingIO内部API
 */
public class GInternal {
    private static final String TAG = "GIO.InternalAPI";

    private String mFeaturesVersionJson = null;

    public static GInternal getInstance() {
        return Internal.sInternal;
    }

    public @Nullable
    GIOMainThread getMainThread() {
        IGrowingTracker gio = GrowingTracker.getInstance();
        if (gio instanceof GrowingTracker) {
            return ((GrowingTracker) gio).mGioMain;
        }
        return null;
    }


    /**
     * 添加特性与其对应的版本
     *
     * @param feature2Version 已key, value, key, value的形式传参
     *                        示例:
     *                        GInternal.getInstance().addFeaturesVersion("gtouch", "0.5.60", "web-circle", "2")
     */
    public synchronized GInternal addFeaturesVersion(String... feature2Version) {
        if (feature2Version.length % 2 != 0) {
            String errorMsg = "GInternal addFeaturesVersion the num of arguments must be even";
            if (GConfig.getInstance().debug()) {
                throw new IllegalArgumentException(errorMsg);
            } else {
                LogUtil.e(TAG, errorMsg);
            }
            return this;
        }

        try {
            JSONObject jsonObject = mFeaturesVersionJson == null ? new JSONObject() : new JSONObject(mFeaturesVersionJson);
            int current = 0;
            while (current < feature2Version.length) {
                String key = feature2Version[current];
                String value = feature2Version[current + 1];
                if (key == null || value == null) {
                    String errorMsg = "key or value is null";
                    if (GConfig.getInstance().debug()) {
                        throw new IllegalArgumentException(errorMsg);
                    } else {
                        LogUtil.e(TAG, errorMsg);
                    }
                    return this;
                }
                current += 2;
                if (jsonObject.has(key)) {
                    LogUtil.d(TAG, "addFeaturesVersion key: %s has exist on featuresVersionJson, oops", key);
                }
                jsonObject.put(key, value);
            }
            if (jsonObject.length() == 0) {
                mFeaturesVersionJson = null;
            } else {
                mFeaturesVersionJson = jsonObject.toString();
            }
        } catch (JSONException e) {
            LogUtil.e(TAG, e.getMessage(), e);
        }
        return this;
    }

    public String getFeaturesVersionJson() {
        return mFeaturesVersionJson;
    }

    private static class Internal {
        static GInternal sInternal = new GInternal();
    }
}
