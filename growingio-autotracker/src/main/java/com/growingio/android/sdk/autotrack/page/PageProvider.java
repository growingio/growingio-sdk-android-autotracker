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
import android.util.Log;
import android.view.View;

import com.growingio.android.sdk.autotrack.IgnorePolicy;
import com.growingio.android.sdk.autotrack.events.PageAttributesEvent;
import com.growingio.android.sdk.autotrack.events.PageEvent;
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

    public void addIgnoreActivityClass(Class<? extends Activity> activityClazz, IgnorePolicy policy) {
        IGNORE_PAGE_CLASSES.put(activityClazz, policy);
    }

    public void addIgnoreSystemFragmentClass(Class<? extends android.app.Fragment> fragmentClazz, IgnorePolicy policy) {
        IGNORE_PAGE_CLASSES.put(fragmentClazz, policy);
    }

    public void addIgnoreV4FragmentClass(Class<? extends android.support.v4.app.Fragment> fragmentClazz, IgnorePolicy policy) {
        IGNORE_PAGE_CLASSES.put(fragmentClazz, policy);
    }

    public void addIgnoreXFragmentClass(Class<? extends androidx.fragment.app.Fragment> fragmentClazz, IgnorePolicy policy) {
        IGNORE_PAGE_CLASSES.put(fragmentClazz, policy);
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
        page.setIgnored(isIgnoreActivity(activity));

        if (!page.isIgnored()) {
            Logger.d(TAG, "createOrResumePage: path = " + page.path());
            generatePageEvent(activity, page);
            reissuePageAttributes(page);
        } else {
            Log.e(TAG, "createOrResumePage: path = " + page.path() + " is ignored");
        }
    }

    private void generatePageEvent(Context context, Page<?> page) {
        String orientation = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                ? PageEvent.ORIENTATION_PORTRAIT : PageEvent.ORIENTATION_LANDSCAPE;
        TrackMainThread.trackMain().postEventToTrackMain(
                new PageEvent.Builder()
                        .setPageName(page.path())
                        .setTitle(page.getTitle())
                        .setTimestamp(page.getShowTimestamp())
                        .setOrientation(orientation)
        );
    }

    public void setActivityAlias(Activity activity, String alias) {
        if (TextUtils.isEmpty(alias)) {
            return;
        }

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
        if (parentFragment != null) {
            IgnorePolicy ignorePolicy = IGNORE_PAGES.get(parentFragment);
            if ((ignorePolicy == IgnorePolicy.IGNORE_ALL || ignorePolicy == IgnorePolicy.IGNORE_CHILD)) {
                return true;
            }
            return isIgnoreByParent(parentFragment);
        } else {
            IgnorePolicy ignorePolicy = IGNORE_PAGES.get(fragment.getActivity());
            return ignorePolicy == IgnorePolicy.IGNORE_ALL || ignorePolicy == IgnorePolicy.IGNORE_CHILD;
        }
    }

    @UiThread
    private void removePage(Activity activity) {
        Log.e(TAG, "removePage: activity is " + activity);
        ALL_PAGE_TREE.remove(activity);
        PAGE_ATTRIBUTES_CACHE.remove(activity);
    }

    @UiThread
    public void createOrResumePage(SuperFragment<?> fragment) {
        if (fragment == null) {
            Logger.e(TAG, "createOrResumePage: this fragment can not make superFragment");
            return;
        }

        if (fragment.getView() == null) {
            Logger.e(TAG, "createOrResumePage: this fragment getView is NULL");
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

        PageGroup<?> page = findPage(fragment);
        if (page == null) {
            page = new FragmentPage(fragment);
            PageGroup<?> pageGroup = findPageParent(fragment);
            page.assignParent(pageGroup);
            pageGroup.addChildren(page);
            addPageAlias(page);
            ViewAttributeUtil.setViewPage(fragment.getView(), page);
        } else {
            page.refreshShowTimestamp();
        }

        page.setIgnored(isIgnoreFragment(fragment));
        if (!page.isIgnored()) {
            Log.e(TAG, "createOrResumePage: path = " + page.path());
            generatePageEvent(fragment.getActivity(), page);
            reissuePageAttributes(page);
        } else {
            Log.e(TAG, "createOrResumePage: path = " + page.path() + " is ignored");
        }
    }

    @UiThread
    public void removePage(SuperFragment<?> fragment) {
        Log.e(TAG, "removePage: fragment is " + fragment.getRealFragment());
        PAGE_ATTRIBUTES_CACHE.remove(fragment);

        PageGroup<?> pageGroup = ALL_PAGE_TREE.get(fragment.getActivity());
        if (pageGroup == null) {
            return;
        }
        removePage(fragment, pageGroup);
    }

    private void removePage(SuperFragment<?> carrier, PageGroup<?> pageGroup) {
        List<Page<?>> pages = pageGroup.getAllChildren();
        for (int i = pages.size() - 1; i >= 0; i--) {
            Page<?> page = pages.get(i);
            if (page.getCarrier().equals(carrier)) {
                pages.remove(i);
                return;
            } else {
                if (page instanceof PageGroup) {
                    removePage(carrier, (PageGroup<?>) page);
                }
            }
        }
    }

    private PageGroup<?> findPageParent(SuperFragment<?> fragment) {
        PageGroup<?> pageParent;
        SuperFragment<?> parentFragment = fragment.getParentFragment();
        ActivityPage activityPage = ALL_PAGE_TREE.get(fragment.getActivity());
        if (parentFragment == null) {
            pageParent = activityPage;
        } else {
            pageParent = findPage(parentFragment, activityPage);
        }
        if (pageParent == null) {
            // TODO: 2020/4/24
            throw new NullPointerException("pageParent is NULL");
        }
        return pageParent;
    }

    private PageGroup<?> findPage(SuperFragment<?> carrier) {
        Activity activity = carrier.getActivity();
        PageGroup<?> pageGroup = ALL_PAGE_TREE.get(activity);
        if (pageGroup == null) {
            return null;
        }
        return findPage(carrier, pageGroup);
    }

    private PageGroup<?> findPage(SuperFragment<?> carrier, PageGroup<?> pageGroup) {
        if (pageGroup.getCarrier().equals(carrier)) {
            return pageGroup;
        }

        List<Page<?>> pages = pageGroup.getAllChildren();
        for (Page<?> page : pages) {
            if (page instanceof PageGroup) {
                PageGroup<?> p = findPage(carrier, (PageGroup<?>) page);
                if (p != null) {
                    return p;
                }
            }
        }

        return null;
    }

    private void reissuePageAttributes(PageGroup<?> pageGroup) {
        Map<String, String> attributes = PAGE_ATTRIBUTES_CACHE.get(pageGroup.getCarrier());
        if (attributes != null) {
            PAGE_ATTRIBUTES_CACHE.remove(pageGroup.getCarrier());
            setPageAttributes(pageGroup, attributes);
            return;
        }

        attributes = pageGroup.getAttributes();
        if (attributes != null) {
            setPageAttributes(pageGroup, attributes);
            return;
        }

        if (pageGroup.getParent() != null) {
            attributes = pageGroup.getParent().getAttributes();
            if (attributes != null) {
                setPageAttributes(pageGroup, attributes);
            }
        }
    }

    public void setPageAttributes(Activity activity, Map<String, String> attributes) {
        ActivityPage page = ALL_PAGE_TREE.get(activity);
        if (page != null) {
            setPageAttributes(page, attributes);
        } else {
            Log.e(TAG, "setPageAttributes: can't find Activity " + activity);
            PAGE_ATTRIBUTES_CACHE.put(activity, attributes);
        }
    }

    public void setPageAttributes(SuperFragment<?> fragment, Map<String, String> attributes) {
        PageGroup<?> page = findPage(fragment);
        if (page != null) {
            setPageAttributes(page, attributes);
        } else {
            Log.e(TAG, "setPageAttributes: can't find Fragment " + fragment.getRealFragment());
            PAGE_ATTRIBUTES_CACHE.put(fragment, attributes);
        }
    }

    private void setPageAttributes(Page<?> page, Map<String, String> attributes) {
        page.setAttributes(attributes);

        if (!page.isIgnored()) {
            Log.e(TAG, "setPageAttributes: page = " + page.path() + ", attributes = " + attributes.toString());
            generatePageAttributesEvent(page);
        }

        if (page instanceof PageGroup) {
            PageGroup<?> pageGroup = (PageGroup<?>) page;
            if (!pageGroup.getAllChildren().isEmpty()) {
                for (Page<?> child : pageGroup.getAllChildren()) {
                    if (child.getShowTimestamp() >= pageGroup.getShowTimestamp()) {
                        setPageAttributes(child, attributes);
                    }
                }
            }
        }
    }

    private void generatePageAttributesEvent(Page<?> page) {
        TrackMainThread.trackMain().postEventToTrackMain(
                new PageAttributesEvent.Builder()
                        .setPageName(page.path())
                        .setPageShowTimestamp(page.getShowTimestamp())
                        .setAttributes(page.getAttributes()));
    }

    public Page<?> findPage(View view) {
        Page<?> page = ViewAttributeUtil.getViewPage(view);
        if (page != null && (!page.isIgnored() || page instanceof ActivityPage)) {
            return page;
        }

        if (view.getParent() instanceof View) {
            return findPage((View) view.getParent());
        }

        //需要考虑Dialog的上面的View
        Context viewContext = view.getContext();
        Activity activity = ActivityUtil.findActivity(viewContext);
        if (activity != null) {
            return ALL_PAGE_TREE.get(activity);
        }

        // TODO: 2020/6/10 这种情况需要观察
        throw new NullPointerException("Page is NULL");
    }
}
