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

import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;

import com.growingio.android.sdk.autotrack.util.ClassUtil;
import com.growingio.android.sdk.track.utils.ClassExistHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

public abstract class Page<T> {
    private final static int MAX_PAGE_LEVEL = 3;

    private final T mCarrier;
    private Page<?> mParent;
    private long mShowTimestamp;
    private boolean mIsAutotrack = false; //是否标记为可发送

    private boolean mIsIgnored = false;
    private String mAlias;
    private String mTitle;
    private String mPath;

    protected String mOriginPath;
    private String xIndex;

    private Map<String, String> mAttributes;
    private final Set<Page<?>> mChildren = new HashSet<>();

    Page(T carrier) {
        mCarrier = carrier;
        mShowTimestamp = System.currentTimeMillis();
    }

    public T getCarrier() {
        return mCarrier;
    }

    public void refreshShowTimestamp() {
        mShowTimestamp = System.currentTimeMillis();
    }

    public boolean isAutotrack() {
        return mIsAutotrack;
    }

    public void setIsAutotrack(boolean mIsAutotrack) {
        this.mIsAutotrack = mIsAutotrack;
    }

    public boolean isIgnored() {
        return mIsIgnored;
    }

    public void setIgnore(boolean ignore) {
        this.mIsIgnored = ignore;
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

    public abstract String getClassName();

    public abstract View getView();

    public String getTitle() {
        if (mTitle != null) return mTitle;
        if (getView() != null) {
            CharSequence title = findToolbarTitle(getView(), 0);
            if (title != null) {
                mTitle = title.toString();
                return mTitle;
            }
        }
        mTitle = "";
        return mTitle;
    }

    private CharSequence findToolbarTitle(View parent, int level) {
        if (parent instanceof Toolbar) {
            return ((Toolbar) parent).getTitle();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && parent instanceof android.widget.Toolbar) {
            return ((android.widget.Toolbar) parent).getTitle();
        } else if (ClassExistHelper.instanceOfSupportToolBar(parent)) {
            return ClassUtil.getSupportToolBarTitle(parent);
        }
        if (level > 5) return null;
        if (parent instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) parent;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View view = vg.getChildAt(i);
                CharSequence title = findToolbarTitle(view, level + 1);
                if (title != null) {
                    return title;
                }
            }
        }
        return null;
    }

    protected void setTitle(String title) {
        mTitle = title;
    }

    public String getAlias() {
        return mAlias;
    }

    public void setAlias(String alias) {
        mAlias = alias;
    }

    public void assignParent(Page<?> parent) {
        mParent = parent;
    }

    public Page<?> getParent() {
        return mParent;
    }

    public abstract String getTag();

    public void addChildren(Page<?> page) {
        mChildren.add(page);
    }

    public Set<Page<?>> getAllChildren() {
        return mChildren;
    }

    public boolean hasChildren() {
        return !mChildren.isEmpty();
    }

    public Page<?> lastActivePage() {
        Page<?> activePage = this;
        while (activePage != null) {
            if (activePage.isAutotrack()) {
                return activePage;
            }
            activePage = activePage.mParent;
        }
        return null;
    }

    public String activePath() {
        Page<?> activePage = lastActivePage();
        if (activePage != null) return activePage.path();
        return "";
    }

    public Map<String, String> activeAttributes() {
        Page<?> activePage = lastActivePage();
        if (activePage != null) return activePage.getAttributes();
        return Collections.emptyMap();
    }

    public String getXIndex() {
        originPath(false);
        return this.xIndex;
    }

    /**
     * @param omitted true:v3 false:v4
     * @return path
     */
    public String originPath(boolean omitted) {
        if (!TextUtils.isEmpty(mOriginPath)) {
            return mOriginPath;
        }
        List<Page<?>> pageTree = new ArrayList<>();
        pageTree.add(this);
        int pageLevel = 1;
        boolean hasMoreParent = false;
        Page<?> parent = getParent();
        while (parent != null) {
            pageTree.add(parent);
            pageLevel++;
            if (pageLevel >= MAX_PAGE_LEVEL && omitted) {
                hasMoreParent = parent.getParent() != null;
                break;
            }
            if (!TextUtils.isEmpty(parent.getAlias()) && omitted) {
                break;
            }
            parent = parent.getParent();
        }
        StringBuilder path = new StringBuilder();
        StringBuilder xIndex = new StringBuilder();
        for (int i = pageTree.size() - 1; i >= 0; i--) {
            Page<?> page = pageTree.get(i);
            if (hasMoreParent && i == pageTree.size() - 1) {
                path.append("*/");
                xIndex.append("*/");
            } else {
                path.append("/");
                xIndex.append("/");
            }
            path.append(omitted ? page.getName() : page.getClassName());
            String tag = page.getTag();
            if (tag != null) {
                if (omitted) path.append("[").append(tag.isEmpty() ? "-" : tag).append("]");
                xIndex.append(tag.isEmpty() ? "-" : tag);
            } else {
                xIndex.append(0); // 默认page的index都为0,不做index的计算。
            }

        }
        this.xIndex = xIndex.toString();
        mOriginPath = path.toString();
        return mOriginPath;
    }

    public String path() {
        if (!TextUtils.isEmpty(mAlias)) {
            mPath = "/" + mAlias;
            return mPath;
        }

        if (!TextUtils.isEmpty(mPath)) {
            return mPath;
        }

        this.mPath = originPath(true);
        return this.mPath;
    }

    protected void loadPageRule(List<PageRule> pageRuleList, String fullPageClassPath) {
        if (pageRuleList == null || fullPageClassPath == null) return;
        // match exactly page classpath at first
        for (PageRule pageRule : pageRuleList) {
            if (!pageRule.isRegMatch() && fullPageClassPath.equals(pageRule.getPageClassPath())) {
                setAlias(pageRule.getPageName());
                setAttributes(Collections.unmodifiableMap(pageRule.getAttributes()));
                setIsAutotrack(true);
            }
        }
        if (!mIsAutotrack) {
            // match reg page classpath secondly
            for (PageRule pageRule : pageRuleList) {
                if (pageRule.isRegMatch() && matchPageRule(fullPageClassPath, pageRule.getPageClassPath())) {
                    setAlias(getName());
                    setAttributes(Collections.unmodifiableMap(pageRule.getAttributes()));
                    setIsAutotrack(true);
                }
            }
        }
    }

    private boolean matchPageRule(String input, String regex) {
        try {
            return input.matches(regex);
        } catch (PatternSyntaxException e) {
            return false;
        }
    }
}
