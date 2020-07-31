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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;

import com.growingio.android.sdk.track.utils.GIOProviders;

public interface NetworkStatusProvider {

    /**
     * 检查网络连接
     */
    void checkNetStatus();

    /**
     * @return true -- 是否有网络连接
     */
    boolean isConnected();

    /**
     * @return true -- 表示移动网络情况下
     */
    boolean isMobileData();

    /**
     * @return true -- 表示wifi连接的情况下
     */
    boolean isWifi();

    /**
     * 返回对应的网络名称:
     * - WIFI, UNKNOWN, 2G, 3G, 4G
     */
    String getNetworkName();

    class NetworkStatus implements NetworkStatusProvider {

        @NonNull
        private final Context mContext;
        private NetworkInfo mNetworkInfo;

        public NetworkStatus(@NonNull Context context) {
            this.mContext = context.getApplicationContext();
        }

        public static NetworkStatusProvider get(final Context context) {
            return GIOProviders.provider(NetworkStatusProvider.class, new GIOProviders.DefaultCallback<NetworkStatusProvider>() {
                @Override
                public NetworkStatusProvider value() {
                    return new NetworkStatus(context);
                }
            });
        }

        // 用于确定NetworkStatus已经初始化的情况下
        public static NetworkStatusProvider get() {
            return GIOProviders.provider(NetworkStatusProvider.class);
        }

        @Override
        public synchronized void checkNetStatus() {
            ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager != null) {
                mNetworkInfo = manager.getActiveNetworkInfo();
            } else {
                // TODO: 异常报警
            }
        }

        @Override
        public synchronized boolean isConnected() {
            return mNetworkInfo != null && mNetworkInfo.isConnected();
        }

        @Override
        public synchronized boolean isMobileData() {
            return isConnected() && mNetworkInfo.getType() != ConnectivityManager.TYPE_WIFI;
        }

        @Override
        public synchronized boolean isWifi() {
            return isConnected() && mNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }

        @Override
        public synchronized String getNetworkName() {
            if (isConnected()) {
                if (mNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    return "WIFI";
                }
                switch (mNetworkInfo.getSubtype()) {
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
                        String subtypeName = mNetworkInfo.getSubtypeName();
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
}
