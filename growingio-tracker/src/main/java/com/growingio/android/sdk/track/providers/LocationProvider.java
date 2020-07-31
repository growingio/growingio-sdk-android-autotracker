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

import android.support.annotation.NonNull;

import com.growingio.android.sdk.track.CoreAppState;
import com.growingio.android.sdk.track.interfaces.GMainThread;
import com.growingio.android.sdk.track.utils.GIOProviders;
import com.growingio.android.sdk.track.utils.LogUtil;

public interface LocationProvider {
    /**
     * Location满足条件更新后需要重发一次visit事件
     * 任意精度从null变为非null
     * <p>
     * 同时为null为clearLocation，单个为null非法
     *
     * @param latitude
     * @param longitude
     */
    @GMainThread
    void setLocation(Double latitude, Double longitude);

    @GMainThread
    Double getLatitude();

    @GMainThread
    Double getLongitude();

    class LocationPolicy implements LocationProvider {
        private static final String TAG = "GIO.LocationPolicy";
        private Double mLatitude, mLongitude;
        private CoreAppState mCoreAppState;
        public LocationPolicy(@NonNull CoreAppState coreAppState) {
            mCoreAppState = coreAppState;
        }

        public static LocationProvider get(final CoreAppState coreAppState) {
            return GIOProviders.provider(LocationProvider.class, new GIOProviders.DefaultCallback<LocationProvider>() {
                @Override
                public LocationProvider value() {
                    return new LocationPolicy(coreAppState);
                }
            });
        }

        @Override
        public void setLocation(Double latitude, Double longitude) {
            if (latitude == null && longitude == null) {
                mLatitude = null;
                mLongitude = null;
                return;
            }

            double eps = 1e-5;
            if (latitude == null || longitude == null ||
                    (Math.abs(latitude) < eps && Math.abs(longitude) < eps)) {
                LogUtil.d(TAG, "found invalid latitude and longitude, and return: ", latitude, ", ", longitude);
                return;
            }

            // 任意精度从null到非null，重新发visit事件
            if ((mLatitude == null && Math.abs(latitude) > eps) ||
                    (mLongitude == null && Math.abs(longitude) > eps)) {
                SessionProvider.SessionPolicy.get(mCoreAppState).resendVisit();
            }

            mLatitude = latitude;
            mLongitude = longitude;
        }

        @Override
        public Double getLatitude() {
            if (mLatitude == null) {
                return 0d;
            }
            return mLatitude;
        }

        @Override
        public Double getLongitude() {
            if (mLongitude == null) {
                return 0d;
            }
            return mLongitude;
        }
    }
}
