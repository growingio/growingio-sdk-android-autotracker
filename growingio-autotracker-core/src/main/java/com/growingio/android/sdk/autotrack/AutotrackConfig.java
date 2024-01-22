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
package com.growingio.android.sdk.autotrack;

import com.growingio.android.sdk.Configurable;

public class AutotrackConfig implements Configurable {
    private float mImpressionScale = 0;
    private boolean mDowngrade = false;

    private boolean supportFragmentTag = false;
    private final AutotrackOptions mAutotrackOptions = new AutotrackOptions();

    public AutotrackConfig setImpressionScale(float scale) {
        if (scale < 0) {
            scale = 0;
        } else if (scale > 1) {
            scale = 1;
        }
        this.mImpressionScale = scale;
        return this;
    }

    public float getImpressionScale() {
        return mImpressionScale;
    }

    public AutotrackConfig setWebViewBridgeEnabled(boolean webViewBridgeEnabled) {
        mAutotrackOptions.setWebViewBridgeEnabled(webViewBridgeEnabled);
        return this;
    }

    public boolean isWebViewBridgeEnabled() {
        return mAutotrackOptions.isWebViewBridgeEnabled();
    }


    public AutotrackOptions getAutotrackOptions() {
        return mAutotrackOptions;
    }

    public AutotrackConfig setFragmentTagSupport(boolean support) {
        this.supportFragmentTag = support;
        return this;
    }

    public boolean isSupportFragmentTag() {
        return supportFragmentTag;
    }

    public void setSupportFragmentTag(boolean supportFragmentTag) {
        this.supportFragmentTag = supportFragmentTag;
    }

    /**
     * You must understand what you are doing before using
     */
    public AutotrackConfig downgrade() {
        this.mDowngrade = true;
        mAutotrackOptions.setFragmentPageEnabled(true);
        mAutotrackOptions.setActivityPageEnabled(true);
        return this;
    }

    public boolean isDowngrade() {
        return this.mDowngrade;
    }
}
