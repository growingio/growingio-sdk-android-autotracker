/*
 *  Copyright (C) 2026 Beijing Yishu Technology Co., Ltd.
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
package com.growingio.android.sdk.track.utils;

import android.app.Application;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.DeadSystemException;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * 验证 system_server 死亡时 SDK 不会把异常抛给宿主 App。
 * 参考线上堆栈：ConnectivityManager.getActiveNetworkInfo -> DeadSystemRuntimeException。
 */
@RunWith(RobolectricTestRunner.class)
public class NetworkUtilTest {

    @Test
    @Config(shadows = {DeadSystemConnectivityManager.class})
    public void getActiveNetworkStateWhenSystemServerDead() {
        Application application = ApplicationProvider.getApplicationContext();

        NetworkUtil.NetworkState state = NetworkUtil.getActiveNetworkState(application);

        Truth.assertThat(state.isConnected()).isFalse();
        Truth.assertThat(state.isWifi()).isFalse();
        Truth.assertThat(state.isMobileData()).isFalse();
        Truth.assertThat(state.getNetworkName()).isEqualTo(ConstantPool.UNKNOWN);
    }

    @Test
    @Config(shadows = {SecurityRestrictedConnectivityManager.class})
    public void getActiveNetworkStateWhenRomRestricted() {
        Application application = ApplicationProvider.getApplicationContext();

        NetworkUtil.NetworkState state = NetworkUtil.getActiveNetworkState(application);

        Truth.assertThat(state.isConnected()).isFalse();
        Truth.assertThat(state.getNetworkName()).isEqualTo(ConstantPool.UNKNOWN);
    }

    /**
     * system_server 死亡后 framework 的表现。API 35+ 抛 DeadSystemRuntimeException，
     * 更低版本抛 RuntimeException(DeadSystemException)，这里用后者，因为前者在 compileSdk 34 上不存在。
     */
    @Implements(ConnectivityManager.class)
    public static class DeadSystemConnectivityManager {
        @Implementation
        protected NetworkInfo getActiveNetworkInfo() {
            throw new RuntimeException(new DeadSystemException());
        }
    }

    /** 部分 ROM 上的权限收紧行为。 */
    @Implements(ConnectivityManager.class)
    public static class SecurityRestrictedConnectivityManager {
        @Implementation
        protected NetworkInfo getActiveNetworkInfo() {
            throw new SecurityException("permission denied by rom");
        }
    }
}
