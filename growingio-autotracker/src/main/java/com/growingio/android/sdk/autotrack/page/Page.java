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

package com.growingio.android.sdk.autotrack.page;

import android.text.TextUtils;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Page<T> {
    private final static int MAX_PAGE_LEVEL = 3;

    private final WeakReference<T> mCarrier;
    private PageGroup<?> mParent;
    private long mShowTimestamp;
    private boolean mIsIgnored = false;
    private String mAlias;
    private String mTitle;
    private String mPath;
    private Map<String, String> mAttributes;

    Page(T carrier) {
        mCarrier = new WeakReference<>(carrier);
        mShowTimestamp = System.currentTimeMillis();
    }

    public T getCarrier() {
        return mCarrier.get();
    }

    public void refreshShowTimestamp() {
        mShowTimestamp = System.currentTimeMillis();
    }

    public boolean isIgnored() {
        return mIsIgnored;
    }

    public void setIgnored(boolean ignored) {
        mIsIgnored = ignored;
    }

    public long getShowTimestamp() {
        return mShowTimestamp;
    }

    public Map<String, String> getAttributes() {
        return mAttributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        mAttributes = attributes;
    }

    public abstract String getName();

    public abstract View getView();

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getAlias() {
        return mAlias;
    }

    public void setAlias(String alias) {
        mAlias = alias;
    }

    public void assignParent(PageGroup<?> parent) {
        mParent = parent;
    }

    public PageGroup<?> getParent() {
        return mParent;
    }

    abstract String getTag();

    public String path() {
        if (!TextUtils.isEmpty(mPath)) {
            return mPath;
        }

        if (!TextUtils.isEmpty(mAlias)) {
            mPath = "/" + mAlias;
            return mPath;
        }

        List<Page<?>> pageTree = new ArrayList<>();
        pageTree.add(this);
        int pageLevel = 1;
        boolean hasMoreParent = false;
        PageGroup<?> parent = getParent();
        while (parent != null) {
            pageTree.add(parent);
            pageLevel++;
            if (pageLevel >= MAX_PAGE_LEVEL) {
                hasMoreParent = parent.getParent() != null;
                break;
            }
            if (!TextUtils.isEmpty(parent.getAlias())) {
                break;
            }
            parent = parent.getParent();
        }
        StringBuilder path = new StringBuilder();
        for (int i = pageTree.size() - 1; i >= 0; i--) {
            Page<?> page = pageTree.get(i);
            if (hasMoreParent && i == pageTree.size() - 1) {
                path.append("*/");
            } else {
                path.append("/");
            }
            path.append(page.getName());
        }
        mPath = path.toString();
        return mPath;
    }
}
