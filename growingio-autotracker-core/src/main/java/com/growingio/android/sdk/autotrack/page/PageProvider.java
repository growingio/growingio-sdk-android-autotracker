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

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.UiThread;
import android.text.TextUtils;
import android.view.View;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.Autotracker;
import com.growingio.android.sdk.autotrack.IgnorePolicy;
import com.growingio.android.sdk.track.events.PageAttributesEvent;
import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.utils.ActivityUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class PageProvider implements IActivityLifecycle {
    private static final String TAG = "PageProvider";

    private static final Map<Activity, ActivityPage> ALL_PAGE_TREE = new WeakHashMap<>();
    private static final Map<Object, String> ALL_PAGE_ALIAS = new WeakHashMap<>();
    private static final Map<Class<?>, IgnorePolicy> IGNORE_PAGE_CLASSES = new HashMap<>();
    private static final Map<Object, IgnorePolicy> IGNORE_PAGES = new WeakHashMap<>();
    private static final Map<Object, Map<String, String>> PAGE_ATTRIBUTES_CACHE = new WeakHashMap<>();

    private static class SingleInstance {
        private static final PageProvider INSTANCE = new PageProvider();
    }

    private PageProvider() {
    }

    public static PageProvider get() {
        return SingleInstance.INSTANCE;
    }

    public void start() {
        ActivityStateProvider.get().registerActivityLifecycleListener(this);
    }

    public void addIgnoreActivity(Activity activity, IgnorePolicy policy) {
        IGNORE_PAGES.put(activity, policy);
    }

    public void addIgnoreFragment(SuperFragment<?> fragment, IgnorePolicy policy) {
        IGNORE_PAGES.put(fragment, policy);
    }

    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        Activity activity = event.getActivity();
        if (activity == null) {
            return;
        }

        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_RESUMED) {
            createOrResumePage(activity);
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_DESTROYED) {
            removePage(activity);
        }
    }

    public void addIgnorePageClass(Class<?> pageClazz, IgnorePolicy policy) {
        IGNORE_PAGE_CLASSES.put(pageClazz, policy);
    }

    @UiThread
    private void createOrResumePage(Activity activity) {
        ActivityPage page = ALL_PAGE_TREE.get(activity);
        if (page == null) {
            page = new ActivityPage(activity);
            if (!TextUtils.isEmpty(activity.getTitle())) {
                page.setTitle(activity.getTitle().toString());
            }
            addPageAlias(page);
            ViewAttributeUtil.setViewPage(activity.getWindow().getDecorView(), page);
            ALL_PAGE_TREE.put(activity, page);
        } else {
            page.refreshShowTimestamp();
        }
        sendPage(activity, page);
    }

    private void refreshPages(Context context, Page<?> page) {
        page.refreshShowTimestamp();
        Logger.d(TAG, "refreshPages: " + page.path());
        sendPage(context, page);
        if (!page.getAllChildren().isEmpty()) {
            for (Page<?> child : page.getAllChildren()) {
                refreshPages(context, child);
            }
        }
    }

    private void sendPage(Context context, Page<?> page) {
        if (page.getCarrier() instanceof Activity) {
            page.setIgnored(isIgnoreActivity((Activity) page.getCarrier()));
        } else if (page.getCarrier() instanceof SuperFragment) {
            page.setIgnored(isIgnoreFragment((SuperFragment<?>) page.getCarrier()));
        }
        if (!page.isIgnored()) {
            Logger.d(TAG, "sendPage: path = " + page.path());
            generatePageEvent(context, page);
            reissuePageAttributes(page);
        } else {
            Logger.e(TAG, "sendPage: path = " + page.path() + " is ignored");
        }
    }

    private void generatePageEvent(Context context, Page<?> page) {
        String orientation = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                ? PageEvent.ORIENTATION_PORTRAIT : PageEvent.ORIENTATION_LANDSCAPE;
        TrackMainThread.trackMain().postEventToTrackMain(
                new PageEvent.Builder()
                        .setPath(page.path())
                        .setTitle(page.getTitle())
                        .setTimestamp(page.getShowTimestamp())
                        .setOrientation(orientation)
        );
    }

    public void setActivityAlias(Activity activity, String alias) {
        if (TextUtils.isEmpty(alias)) {
            return;
        }
//        ActivityPage page = ALL_PAGE_TREE.get(activity);
//        if (page != null) {
//            page.setAlias(alias);
//        }
        ALL_PAGE_ALIAS.put(activity, alias);
    }

    public void setFragmentAlias(SuperFragment<?> fragment, String alias) {
        if (TextUtils.isEmpty(alias)) {
            return;
        }

        ALL_PAGE_ALIAS.put(fragment, alias);
    }

    private void addPageAlias(Page<?> page) {
        String alias = ALL_PAGE_ALIAS.get(page.getCarrier());
        if (!TextUtils.isEmpty(alias)) {
            page.setAlias(alias);
        }
    }

    private boolean isIgnoreActivity(Activity activity) {
        IgnorePolicy ignorePolicy = IGNORE_PAGES.get(activity);
        if (ignorePolicy != null) {
            return ignorePolicy == IgnorePolicy.IGNORE_SELF || ignorePolicy == IgnorePolicy.IGNORE_ALL;
        }
        return false;
    }

    private boolean isIgnoreFragment(SuperFragment<?> fragment) {
        IgnorePolicy ignorePolicy = IGNORE_PAGES.get(fragment);
        if (ignorePolicy != null) {
            return ignorePolicy == IgnorePolicy.IGNORE_SELF || ignorePolicy == IgnorePolicy.IGNORE_ALL;
        }
        return isIgnoreByParent(fragment);
    }

    private boolean isIgnoreByParent(SuperFragment<?> fragment) {
        SuperFragment<?> parentFragment = fragment.getParentFragment();
        while (parentFragment != null) {
            IgnorePolicy ignorePolicy = IGNORE_PAGES.get(parentFragment);
            if ((ignorePolicy == IgnorePolicy.IGNORE_ALL || ignorePolicy == IgnorePolicy.IGNORE_CHILD)) {
                return true;
            } else {
                parentFragment = parentFragment.getParentFragment();
            }
        }

        IgnorePolicy ignorePolicy = IGNORE_PAGES.get(fragment.getActivity());
        return ignorePolicy == IgnorePolicy.IGNORE_ALL || ignorePolicy == IgnorePolicy.IGNORE_CHILD;
    }

    @UiThread
    private void removePage(Activity activity) {
        Logger.d(TAG, "removePage: activity is " + activity);
        ALL_PAGE_TREE.remove(activity);
        PAGE_ATTRIBUTES_CACHE.remove(activity);
    }

    private boolean isHidden(SuperFragment<?> fragment) {
        if (fragment.isHidden()) {
            return true;
        }
        SuperFragment<?> parentFragment = fragment.getParentFragment();
        while (parentFragment != null) {
            if (parentFragment.isHidden()) {
                return true;
            }
            parentFragment = parentFragment.getParentFragment();
        }

        return false;
    }

    @UiThread
    public void fragmentOnHiddenChanged(SuperFragment<?> fragment, boolean hidden) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
            return;
        }

        if (!hidden) {
            Page<?> page = findPage(fragment);
            if (page == null) {
                Logger.e(TAG, "fragmentOnHiddenChanged: fragment is NULL");
            } else {
                refreshPages(fragment.getActivity(), page);
            }
        }
    }

    @UiThread
    public void createOrResumePage(SuperFragment<?> fragment) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
            return;
        }

        if (fragment == null) {
            Logger.e(TAG, "createOrResumePage: this fragment can not make superFragment");
            return;
        }

        if (fragment.getView() == null) {
            Logger.e(TAG, "createOrResumePage: this fragment getView is NULL");
            return;
        }

        if (isHidden(fragment)) {
            Logger.e(TAG, "createOrResumePage: this fragment is hidden");
            return;
        }

        if (!fragment.getUserVisibleHint()) {
            Logger.e(TAG, "createOrResumePage: this fragment's UI is currently invisible to the user");
            return;
        }

        if (fragment.getActivity() == null) {
            Logger.e(TAG, "createOrResumePage: this fragment getActivity is NULL");
            return;
        }

        if (!fragment.isResumed()) {
            Logger.e(TAG, "createOrResumePage: this fragment not is resumed");
            return;
        }

        Page<?> page = findPage(fragment);
        if (page == null) {
            page = new FragmentPage(fragment);
            Page<?> pageParent = findPageParent(fragment);
            if (pageParent == null) {
                Logger.e(TAG, "pageParent is NULL");
                return;
            }
            page.assignParent(pageParent);
            pageParent.addChildren(page);
            addPageAlias(page);
            ViewAttributeUtil.setViewPage(fragment.getView(), page);
        } else {
            page.refreshShowTimestamp();
        }
        sendPage(fragment.getActivity(), page);
    }

    @UiThread
    public void removePage(SuperFragment<?> fragment) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
            return;
        }

        if (fragment == null) {
            Logger.e(TAG, "removePage: this fragment can not make superFragment");
            return;
        }

        if (fragment.getView() == null) {
            Logger.e(TAG, "removePage: this fragment getView is NULL");
            return;
        }

        Logger.e(TAG, "removePage: fragment is " + fragment.getRealFragment());
        PAGE_ATTRIBUTES_CACHE.remove(fragment);

        Page<?> page = ALL_PAGE_TREE.get(fragment.getActivity());
        if (page == null) {
            return;
        }
        removePage(fragment, page);
    }

    private void removePage(SuperFragment<?> carrier, Page<?> page) {
        List<Page<?>> pages = page.getAllChildren();
        for (int i = pages.size() - 1; i >= 0; i--) {
            Page<?> onePage = pages.get(i);
            if (carrier.equals(onePage.getCarrier())) {
                pages.remove(i);
                return;
            } else {
                removePage(carrier, onePage);
            }
        }
    }

    private Page<?> findPageParent(SuperFragment<?> fragment) {
        Page<?> pageParent = null;
        SuperFragment<?> parentFragment = fragment.getParentFragment();
        ActivityPage activityPage = ALL_PAGE_TREE.get(fragment.getActivity());
        if (parentFragment == null) {
            pageParent = activityPage;
        } else if (activityPage != null) {
            pageParent = findPage(parentFragment, activityPage);
        }
        // TODO: 2021/04/26 如果为null可能存在以下情况
        // 1. 父fragment getUserVisibleHint为false， 导致子fragment无法找到page
        // 2. 2021/04/26 旋转、内存不足导致activity销毁， fragment无法找到page
        // 3. 延迟初始化导致activityPage为null
        return pageParent;
    }

    protected Page<?> findPage(SuperFragment<?> carrier) {
        Activity activity = carrier.getActivity();
        Page<?> page = ALL_PAGE_TREE.get(activity);
        if (page == null) {
            return null;
        }
        return findPage(carrier, page);
    }

    private Page<?> findPage(SuperFragment<?> carrier, Page<?> page) {
        if (carrier.equals(page.getCarrier())) {
            return page;
        }

        List<Page<?>> pages = page.getAllChildren();
        for (Page<?> onePage : pages) {
            if (onePage != null) {
                Page<?> p = findPage(carrier, onePage);
                if (p != null) {
                    return p;
                }
            }
        }

        return null;
    }

    private void reissuePageAttributes(Page<?> page) {
        Map<String, String> attributes = PAGE_ATTRIBUTES_CACHE.remove(page.getCarrier());
        if (attributes != null) {
            setPageAttributes(page, attributes, false);
            return;
        }

        attributes = page.getAttributes();
        if (attributes != null) {
            setPageAttributes(page, attributes, false);
            return;
        }

        if (page.getParent() != null) {
            attributes = page.getParent().getAttributes();
            if (attributes != null) {
                setPageAttributes(page, attributes, false);
            }
        }
    }

    public void setPageAttributes(Activity activity, Map<String, String> attributes) {
        ActivityPage page = ALL_PAGE_TREE.get(activity);
        if (page != null) {
            setPageAttributes(page, attributes, true);
        } else {
            Logger.e(TAG, "setPageAttributes: can't find Activity " + activity);
            PAGE_ATTRIBUTES_CACHE.put(activity, attributes);
        }
    }

    public void setPageAttributes(SuperFragment<?> fragment, Map<String, String> attributes) {
        Page<?> page = findPage(fragment);
        if (page != null) {
            setPageAttributes(page, attributes, true);
        } else {
            Logger.e(TAG, "setPageAttributes: can't find Fragment " + fragment.getRealFragment());
            PAGE_ATTRIBUTES_CACHE.put(fragment, attributes);
        }
    }

    private void setPageAttributes(Page<?> page, Map<String, String> attributes, boolean checkEquals) {
        if (checkEquals && attributes.equals(page.getAttributes())) {
            Logger.e(TAG, "setPageAttributes is equals page.getAttributes");
            return;
        }

        page.setAttributes(attributes);

        if (!page.isIgnored()) {
            Logger.d(TAG, "setPageAttributes: page = " + page.path() + ", attributes = " + attributes.toString());
            generatePageAttributesEvent(page);
        }

        if (!page.getAllChildren().isEmpty()) {
            for (Page<?> child : page.getAllChildren()) {
                if (child.getShowTimestamp() >= page.getShowTimestamp()) {
                    setPageAttributes(child, attributes, checkEquals);
                }
            }
        }
    }

    private void generatePageAttributesEvent(Page<?> page) {
        TrackMainThread.trackMain().postEventToTrackMain(
                new PageAttributesEvent.Builder()
                        .setPath(page.path())
                        .setPageShowTimestamp(page.getShowTimestamp())
                        .setAttributes(page.getAttributes()));
    }

    public Page<?> findPage(Activity activity) {
        return ALL_PAGE_TREE.get(activity);
    }

    public Page<?> findPage(View view) {
        Page<?> page = ViewAttributeUtil.getViewPage(view);
        if (page != null && (!page.isIgnored() || page instanceof ActivityPage)) {
            return page;
        }

        if (view.getParent() instanceof View) {
            return findPage((View) view.getParent());
        }

        //需要考虑其他Window的上面的View
        Context viewContext = view.getContext();
        Activity activity = ActivityUtil.findActivity(viewContext);
        if (activity == null) {
            activity = ActivityStateProvider.get().getForegroundActivity();
        }
        if (activity != null) {
            if (ALL_PAGE_TREE.containsKey(activity)) {
                return ALL_PAGE_TREE.get(activity);
            } else {
                //一般不会进入，如果出现则新生成page返回
                ActivityPage newPage = new ActivityPage(activity);
                newPage.setTitle(activity.getTitle().toString());
                return newPage;
            }
        }

        // TODO: 2020/6/10 这种情况需要观察
        throw new NullPointerException("Page is NULL");
    }
}
