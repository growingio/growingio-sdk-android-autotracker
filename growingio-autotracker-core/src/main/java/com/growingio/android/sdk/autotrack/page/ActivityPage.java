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

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;


public class ActivityPage extends Page<SuperActivity> {

    private PageConfig pageConfig;

    public ActivityPage(Activity carrier) {
        super(new SuperActivity(carrier));
    }

    public ActivityPage(Activity carrier, PageConfig pageConfig) {
        super(new SuperActivity(carrier));
        this.pageConfig = pageConfig;

        if (this.pageConfig != null) {
            String fullPageClassPath = getCarrier().getRealActivity().getClass().getName();
            loadPageRule(this.pageConfig.getPageRules(), fullPageClassPath);
        }
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
    public boolean isAutotrack() {
        // cdp downgrade when activity page is enabled
        if (pageConfig != null && pageConfig.isActivityPageEnabled()) {
            return !isIgnored();
        }
        return super.isAutotrack();
    }

    @Override
    public View getView() {
        if (getCarrier().getRealActivity() != null) {
            return getCarrier().getRealActivity().getWindow().getDecorView();
        }
        return null;
    }

    @Override
    public String getTag() {
        return null;
    }

    @Override
    public String getTitle() {
        Activity activity = getCarrier().getRealActivity();
        if (activity != null && !TextUtils.isEmpty(activity.getTitle())) {
            return activity.getTitle().toString();
        }
        return super.getTitle();
    }

    @Override
    public String pagePath() {
        if (pageConfig != null && pageConfig.isDowngrade()) {
            return originPath(true);
        } else {
            return "/" + getName();
        }
    }
}
