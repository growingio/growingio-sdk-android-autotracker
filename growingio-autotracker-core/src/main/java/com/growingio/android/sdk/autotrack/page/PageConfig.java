/*
 *  Copyright (C) 2024 Beijing Yishu Technology Co., Ltd.
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

package com.growingio.android.sdk.autotrack.page;

import java.util.List;

class PageConfig {
    private boolean enableFragmentTag = false;
    private List<PageRule> pageRuleList;
    private boolean isActivityPageEnabled = true;
    private boolean isFragmentPageEnabled = true;
    private boolean isDowngrade = false;

    public PageConfig(List<PageRule> pageRuleList, boolean isActivityPageEnabled, boolean isFragmentPageEnabled, boolean enableFragmentTag, boolean isDowngrade) {
        this.pageRuleList = pageRuleList;
        this.isActivityPageEnabled = isActivityPageEnabled;
        this.isFragmentPageEnabled = isFragmentPageEnabled;
        this.enableFragmentTag = enableFragmentTag;
        this.isDowngrade = isDowngrade;
    }

    public boolean isEnableFragmentTag() {
        return enableFragmentTag;
    }

    public List<PageRule> getPageRules() {
        return pageRuleList;
    }

    public boolean isActivityPageEnabled() {
        return isActivityPageEnabled;
    }

    public boolean isFragmentPageEnabled() {
        return isFragmentPageEnabled;
    }

    public boolean isDowngrade() {
        return isDowngrade;
    }
}
