/*
 * Copyright (C) 2024 Beijing Yishu Technology Co., Ltd.
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

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class PageRule {
    private final String pageClassPath;
    private String pageName;
    private final boolean regMatch;
    private Map<String, String> attributes = new HashMap<>();

    public PageRule(String pageName, String pageClassPath) {
        this.pageClassPath = pageClassPath;
        this.pageName = pageName;
        this.regMatch = false;
    }

    public PageRule(String pageClassPath) {
        this.pageClassPath = pageClassPath;
        this.regMatch = true;
    }

    public String getPageName() {
        return pageName;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getPageClassPath() {
        return pageClassPath;
    }

    public boolean isRegMatch() {
        return regMatch;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @NonNull
    @Override
    public String toString() {
        if (isRegMatch()) {
            return "PageRule{" + "pageRegexPath='" + pageClassPath + '\'' + '}';
        } else {
            return "PageRule{" + "pageName='" + pageName + '\'' + ", pageClassPath='" + pageClassPath + '\'' + '}';
        }
    }
}
