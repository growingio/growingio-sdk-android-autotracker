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
package com.growingio.android.sdk.autotrack.page;

import android.text.TextUtils;
import android.view.View;

import com.growingio.android.sdk.autotrack.AutotrackConfig;

public class FragmentPage extends Page<SuperFragment<?>> {

    private AutotrackConfig autotrackConfig;

    public FragmentPage(SuperFragment<?> carrier) {
        super(carrier);
    }

    public FragmentPage(SuperFragment<?> carrier, AutotrackConfig autotrackConfig) {
        super(carrier);
        this.autotrackConfig = autotrackConfig;
        String fullPageClassPath = getCarrier().getRealFragment().getClass().getName();
        loadPageRule(this.autotrackConfig.getPageRules(), fullPageClassPath);
    }

    @Override
    public String getName() {
        if (!TextUtils.isEmpty(getAlias())) {
            return getAlias();
        }
        return getClassName();
    }

    @Override
    public String getClassName() {
        return getCarrier().getSimpleName();
    }

    @Override
    public View getView() {
        return getCarrier().getView();
    }

    @Override
    public String getTag() {
        if (autotrackConfig.isEnableFragmentTag()) {
            String tag = getCarrier().getTag();
            if (!TextUtils.isEmpty(tag)) {
                return transformSwitcherTag(tag);
            }
        }
        return getCarrier().getResourceEntryName(getCarrier().getId());
    }

    /**
     * 如果是ViewPager + Fragment的形式，Fragment的Tag形式是 android:switcher:ViewPagerViewId:Index
     * 例如 android:switcher:2131230945:4
     * 可读性和唯一性比较差
     */
    private String transformSwitcherTag(String tag) {
        String[] e = tag.split(":");
        if (e.length == 4) {
            try {
                e[2] = getCarrier().getResourceEntryName(Integer.parseInt(e[2]));
            } catch (NumberFormatException ignored) {
            }
            StringBuilder stringBuilder = new StringBuilder(e[0]);
            for (int i = 1; i < e.length; i++) {
                stringBuilder.append(":").append(e[i]);
            }
            return stringBuilder.toString();
        }

        return tag;
    }

    @Override
    public boolean isAutotrack() {
        // cdp downgrade when fragment page is enabled
        if (autotrackConfig != null && autotrackConfig.getAutotrackOptions().isFragmentPageEnabled()) {
            return !isIgnored();
        }
        return super.isAutotrack();
    }
}
