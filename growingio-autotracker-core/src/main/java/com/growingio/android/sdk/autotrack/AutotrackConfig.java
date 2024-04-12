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
import com.growingio.android.sdk.autotrack.page.PageRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AutotrackConfig implements Configurable {
    private float mImpressionScale = 0;
    private boolean enableFragmentTag = false;
    private final AutotrackOptions mAutotrackOptions = new AutotrackOptions();

    private boolean autotrackEnabled = true;
    private int pageXmlRes = 0;
    private final List<PageRule> pageRules = new ArrayList<>();

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

    /**
     * Set the page rule xml resource id for auto page track.
     *
     * @param pageXmlRes page rule xml resource id
     */
    public AutotrackConfig setPageRuleXml(int pageXmlRes) {
        this.pageXmlRes = pageXmlRes;
        return this;
    }

    /**
     * Add page rule for auto track.
     * PageName is the alias name of the page, such as "MainPage".
     * PageClassPath is the full path of the page class, such as "com.growingio.android.sdk.autotrack.MainActivity".
     *
     * @param pageName      page's alias name
     * @param pageClassPath page's class path
     * @return AutotrackConfig
     */
    public AutotrackConfig addPageRule(String pageName, String pageClassPath) {
        pageRules.add(new PageRule(pageName, pageClassPath));
        return this;
    }

    /**
     * Add page rule for auto track.
     * pageClassRegex support regular expression, such as "com.growingio.android.autotrack.*Activity".
     *
     * @param pageClassRegex page's regular expression
     */
    public AutotrackConfig addPageMatchRule(String pageClassRegex) {
        pageRules.add(new PageRule(pageClassRegex));
        return this;
    }

    /**
     * Add page rule for auto track.
     * PageName is the alias name of the page, such as "MainPage".
     * PageClassPath is the full path of the page class, such as "com.growingio.android.sdk.autotrack.MainActivity".
     * PageAttributes is the attributes of the page, such as "{'key1':'value1','key2':'value2'}".
     *
     * @param pageName      page's alias name
     * @param pageClassPath page's class path
     * @param attributes    page's attributes
     * @return AutotrackConfig
     */
    public AutotrackConfig addPageRuleWithAttributes(String pageName, String pageClassPath, Map<String, String> attributes) {
        PageRule pageRule = new PageRule(pageName, pageClassPath);
        pageRule.setAttributes(attributes);
        pageRules.add(pageRule);
        return this;
    }

    /**
     * Add page rule for auto track.
     * pageClassRegex support regular expression, such as "com.growingio.android.autotrack.*Activity".
     * PageAttributes is the attributes of the page, such as "{'key1':'value1','key2':'value2'}".
     *
     * @param pageClassRegex page's regular expression
     * @param attributes     page's attributes
     * @return AutotrackConfig
     */
    public AutotrackConfig addPageMatchRuleWithAttributes(String pageClassRegex, Map<String, String> attributes) {
        PageRule pageRule = new PageRule(pageClassRegex);
        pageRule.setAttributes(attributes);
        pageRules.add(pageRule);
        return this;
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

    public AutotrackConfig enableFragmentTag(boolean enable) {
        this.enableFragmentTag = enable;
        return this;
    }

    public boolean isEnableFragmentTag() {
        return enableFragmentTag;
    }

    public List<PageRule> getPageRules() {
        return pageRules;
    }

    public int getPageXmlRes() {
        return pageXmlRes;
    }

    public boolean isAutotrack() {
        return autotrackEnabled;
    }

    public AutotrackConfig setAutotrack(boolean enabled) {
        this.autotrackEnabled = enabled;
        return this;
    }
}
