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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NetworkUtil {
    public static class NetworkState {
        private final boolean mIsConnected;
        private final boolean mIsMobileData;
        private final boolean mIsWifi;
        private final String mNetworkName;

        private NetworkState(boolean isConnected, boolean isMobileData, boolean isWifi, String networkName) {
            mIsConnected = isConnected;
            mIsMobileData = isMobileData;
            mIsWifi = isWifi;
            mNetworkName = networkName;
        }

        public boolean isConnected() {
            return mIsConnected;
        }

        public boolean isMobileData() {
            return mIsMobileData;
        }

        public boolean isWifi() {
            return mIsWifi;
        }

        public String getNetworkName() {
            return mNetworkName;
        }
    }

    private NetworkUtil() {
    }

    @Nullable
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            return manager.getActiveNetworkInfo();
        }
        return null;
    }

    @NonNull
    public static NetworkState getActiveNetworkState(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        if (networkInfo == null) {
            return new NetworkState(false, false, false, ConstantPool.UNKNOWN);
        } else {
            return new NetworkState(networkInfo.isConnected(), networkInfo.isConnected() && networkInfo.getType() != ConnectivityManager.TYPE_WIFI,
                    networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI, getNetworkName(networkInfo));
        }
    }

    /**
     * @return true -- 是否有网络连接
     */
    public static boolean isConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * @return true -- 表示移动网络情况下
     */
    public static boolean isMobileData(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        return networkInfo != null && networkInfo.isConnected() && networkInfo.getType() != ConnectivityManager.TYPE_WIFI;
    }

    /**
     * @return true -- 表示wifi连接的情况下
     */
    public static boolean isWifi(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        return networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 返回对应的网络名称:
     * - WIFI, UNKNOWN, 2G, 3G, 4G
     */
    public static String getNetworkName(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        return getNetworkName(networkInfo);
    }

    public static String getNetworkName(NetworkInfo networkInfo) {
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return "WIFI";
            }
            switch (networkInfo.getSubtype()) {
                //如果是2g类型
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return "2G";
                //如果是3g类型
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return "3G";
                //如果是4g类型
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return "4G";
                default:
                    //中国移动 联通 电信 三种3G制式
                    String subtypeName = networkInfo.getSubtypeName();
                    if ("TD-SCDMA".equalsIgnoreCase(subtypeName)
                            || "WCDMA".equalsIgnoreCase(subtypeName)
                            || "CDMA2000".equalsIgnoreCase(subtypeName)) {
                        return "3G";
                    }
            }
        }
        return "UNKNOWN";
    }
}
