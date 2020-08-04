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

import android.support.annotation.AnyThread;

import com.growingio.android.sdk.track.base.BuildConfig;
import com.growingio.android.sdk.track.utils.CustomerInterface;

/**
 * 用于记录状态配置(之所以不适用Provider形式, 是为了性能考虑):
 * - 是否可开启Debug
 * - 是否hook起效
 */
public class GConfig {
    /* 希望将SDK用到的配置文件放在一起， 便于查找 */
    public static final String PREF_ECSID_FILE_NAME = "growing_ecsid";
    public static final String SDK_VERSION = BuildConfig.VERSION_NAME;
    public static final int SDK_VERSION_CODE = BuildConfig.VERSION_CODE;
    private static final String TAG = "GIO.GConfig";
    boolean mUseID = true;
    boolean mIsRnMode = false;
    boolean mUseRnOptimizedPath = false;
    private volatile boolean mInitSucceeded = false;
    private volatile boolean mIsEnableDataCollect = true;
    private CustomerInterface.Encryption mEncryption = null;

    public static GConfig getInstance() {
        return Instance.sConfig;
    }

    @AnyThread
    public void setIsEnableDataCollect(boolean isEnableDataCollect) {
        mIsEnableDataCollect = isEnableDataCollect;
    }

    /**
     * @return 是否允许事件采集
     * 当返回值为false时, 我们尽可能的在事件生成处进行拦截, 但不要破坏监听,  为了方便后续准确enableDataCollect
     * <p>
     * 此处补充受enableDataCollect影响之处:
     * - 1. 所有未生成事件, 包括但不限于
     * - 2. 所有设备信息的采集, 比如说imei, androidId, oaid, googleId, u的生成
     * - 3. 为了保证visit事件的发送, 在返回值为false阶段， 不再记录Activity的pause, resume时间.
     * - 4. 不再校验任何广告业务
     * - 5. 为了调试方便, enableDataCollect不影响mobileDebugger相关逻辑
     */
    @AnyThread
    public boolean isEnableDataCollect() {
        return mIsEnableDataCollect;
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean isInitSucceeded() {
        return mInitSucceeded;
    }

    public void setInitSucceeded(boolean initSucceeded) {
        this.mInitSucceeded = initSucceeded;
    }

    public boolean debug() {
        return true;
    }

    public boolean useID() {
        return this.mUseID;
    }

    public boolean isRnMode() {
        return this.mIsRnMode;
    }

    public boolean useRnOptimizedPath() {
        return this.mUseRnOptimizedPath;
    }

    public CustomerInterface.Encryption getEncryption() {
        return this.mEncryption;
    }

    public void setEncryption(CustomerInterface.Encryption encryption) {
        this.mEncryption = encryption;
    }

    /**
     * 校验版本信息， 若版本信息不同则抛出异常
     */
    public void checkVersion(String submoduleName, String submoduleVersion) {
        if (!BuildConfig.VERSION_NAME.equals(submoduleVersion)) {
            throw new GIOException("GrowingIO要求各个采集SDK版本号相同: ("
                    + submoduleName + ": " + submoduleVersion + ")");
        }
    }

    private static class Instance {
        private static GConfig sConfig = new GConfig();
    }

}
