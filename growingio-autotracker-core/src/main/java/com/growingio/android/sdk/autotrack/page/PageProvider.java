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
import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.UiThread;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.AutotrackConfig;
import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;
import com.growingio.android.sdk.track.utils.ActivityUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class PageProvider implements IActivityLifecycle, TrackerLifecycleProvider {
    private static final String TAG = "PageProvider";

    /**
     * Page Tree that is already in the Activity or Fragment Lifecycle.
     */
    private final Map<Activity, ActivityPage> ALL_PAGE_TREE = new WeakHashMap<>();
    /**
     * Cache Pages that not in the Lifecycle but has set properties.
     */
    private final Map<Object, Page<?>> CACHE_PAGES = new WeakHashMap<>();

    private static class SingleInstance {
        private static final PageProvider INSTANCE = new PageProvider();
    }

    private ActivityStateProvider activityStateProvider;
    private AutotrackConfig autotrackConfig;

    private PageProvider() {
        ALL_PAGE_TREE.clear();
        CACHE_PAGES.clear();
    }

    @Override
    public void setup(TrackerContext context) {
        activityStateProvider = context.getActivityStateProvider();
        autotrackConfig = context.getConfigurationProvider().getConfiguration(AutotrackConfig.class);
        activityStateProvider.registerActivityLifecycleListener(this);
    }

    @Override
    public void shutdown() {
        activityStateProvider.unregisterActivityLifecycleListener(this);
    }

    public static PageProvider get() {
        return SingleInstance.INSTANCE;
    }

    @Override
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

    @UiThread
    private void createOrResumePage(Activity activity) {
        ActivityPage page = generateActivityPageInTree(activity);
        if (!TextUtils.isEmpty(activity.getTitle())) {
            page.setTitle(activity.getTitle().toString());
        }
        ViewAttributeUtil.setViewPage(activity.getWindow().getDecorView(), page);
        page.refreshShowTimestamp();
        sendPage(activity, page);
    }

    private ActivityPage generateActivityPageInTree(Activity activity) {
        ActivityPage page = ALL_PAGE_TREE.get(activity);
        if (page == null && CACHE_PAGES.containsKey(activity)) {
            page = (ActivityPage) CACHE_PAGES.get(activity);
            CACHE_PAGES.remove(activity);
        }
        if (page == null) {
            page = new ActivityPage(activity, autotrackConfig);
        }
        ALL_PAGE_TREE.put(activity, page);
        return page;
    }

    protected ActivityPage findOrCreateActivityPage(Activity activity) {
        ActivityPage page = ALL_PAGE_TREE.get(activity);
        if (page != null) {
            return page;
        }
        if (CACHE_PAGES.containsKey(activity)) {
            page = (ActivityPage) CACHE_PAGES.get(activity);
            return page;
        }
        if (page == null) {
            page = new ActivityPage(activity, autotrackConfig);
            CACHE_PAGES.put(activity, page);
        }
        return page;
    }

    public void autotrackActivity(Activity activity, String alias, Map<String, String> attributes) {
        ActivityPage page = ALL_PAGE_TREE.get(activity);
        if (page != null) {
            if (alias != null) page.setAlias(alias);
            page.setAttributes(attributes);
            if (!page.isAutotrack()) {
                page.setIsAutotrack(true);
                sendPage(activity, page);
            }
            return;
        }
        if (CACHE_PAGES.containsKey(activity)) {
            page = (ActivityPage) CACHE_PAGES.get(activity);
        } else {
            page = new ActivityPage(activity, autotrackConfig);
            CACHE_PAGES.put(activity, page);
        }
        page.setIsAutotrack(true);
        if (alias != null) page.setAlias(alias);
        page.setAttributes(attributes);
    }

    public Page<Activity> searchActivityPage(Activity activity) {
        ActivityPage page = ALL_PAGE_TREE.get(activity);
        if (page != null) {
            return page;
        }
        if (CACHE_PAGES.containsKey(activity)) {
            page = (ActivityPage) CACHE_PAGES.get(activity);
            return page;
        }
        return page;
    }

    private void showPagesFromHidden(Context context, Page<?> page) {
        page.refreshShowTimestamp();
        Logger.d(TAG, "refreshPages: " + page.path());
        sendPage(context, page);
        if (!page.getAllChildren().isEmpty()) {
            for (Page<?> child : page.getAllChildren()) {
                showPagesFromHidden(context, child);
            }
        }
    }

    private void sendPage(Context context, Page<?> page) {
        if (page.isAutotrack()) {
            Logger.d(TAG, "sendPage: path = " + page.path());
            generatePageEvent(context, page);
        } else {
            Logger.d(TAG, "AutotrackOptions: page enable is false");
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
                        .setAttributes(page.getAttributes())
        );
    }

    protected FragmentPage findOrCreateFragmentPage(SuperFragment<?> fragment) {
        // find activity page
        Activity activity = fragment.getActivity();
        ActivityPage page = findOrCreateActivityPage(activity);

        // find fragment page
        Page<?> result = searchFragmentPage(fragment, page);
        if (result != null) {
            return (FragmentPage) result;
        }
        if (CACHE_PAGES.containsKey(fragment)) {
            return (FragmentPage) CACHE_PAGES.get(fragment);
        }
        FragmentPage fragmentPage = new FragmentPage(fragment, autotrackConfig);
        CACHE_PAGES.put(fragment, fragmentPage);
        return fragmentPage;
    }

    @UiThread
    private void removePage(Activity activity) {
        Logger.d(TAG, "removePage: activity is " + activity);
        ALL_PAGE_TREE.remove(activity);
        CACHE_PAGES.remove(activity);
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
            Activity activity = fragment.getActivity();
            Page<?> page = ALL_PAGE_TREE.get(activity);
            if (page != null) {
                Page<?> fragmentPage = searchFragmentPage(fragment, page);
                if (fragmentPage == null) {
                    Logger.w(TAG, "fragmentOnHiddenChanged: fragment is NULL");
                } else {
                    showPagesFromHidden(fragment.getActivity(), fragmentPage);
                }
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
            Logger.w(TAG, "createOrResumePage: this fragment is hidden");
            return;
        }

        if (!fragment.getUserVisibleHint()) {
            Logger.w(TAG, "createOrResumePage: this fragment's UI is currently invisible to the user");
            return;
        }

        if (fragment.getActivity() == null) {
            Logger.e(TAG, "createOrResumePage: this fragment getActivity is NULL");
            return;
        }

        if (!fragment.isResumed()) {
            Logger.w(TAG, "createOrResumePage: this fragment not is resumed");
            return;
        }

        FragmentPage page = generateFragmentPage(fragment);
        Page<?> pageParent = searchPageParent(fragment);
        if (pageParent == null) {
            Logger.e(TAG, fragment.getClass().getSimpleName() + "'s pageParent is NULL");
            return;
        }
        page.assignParent(pageParent);
        pageParent.addChildren(page);
        ViewAttributeUtil.setViewPage(fragment.getView(), page);
        page.refreshShowTimestamp();

        sendPage(fragment.getActivity(), page);
    }

    private FragmentPage generateFragmentPage(SuperFragment<?> fragment) {
        // find activity page
        Activity activity = fragment.getActivity();
        ActivityPage page = findOrCreateActivityPage(activity);

        // find fragment page
        Page<?> result = searchFragmentPage(fragment, page);
        if (result != null) {
            return (FragmentPage) result;
        }
        if (CACHE_PAGES.containsKey(fragment)) {
            FragmentPage fragmentPage = (FragmentPage) CACHE_PAGES.get(fragment);
            CACHE_PAGES.remove(fragment);
            return fragmentPage;
        }
        return new FragmentPage(fragment, autotrackConfig);
    }

    public void autotrackFragment(SuperFragment<?> fragment, String alias, Map<String, String> attributes) {
        // find activity page
        Activity activity = fragment.getActivity();
        ActivityPage page = findOrCreateActivityPage(activity);

        // find fragment page
        Page<?> fragmentPage = searchFragmentPage(fragment, page);
        if (fragmentPage != null) {
            if (alias != null) fragmentPage.setAlias(alias);
            fragmentPage.setAttributes(attributes);
            if (!fragmentPage.isAutotrack()) {
                fragmentPage.setIsAutotrack(true);
                sendPage(activity, fragmentPage);
            }
            return;
        }
        if (CACHE_PAGES.containsKey(fragment)) {
            fragmentPage = (FragmentPage) CACHE_PAGES.get(fragment);
        } else {
            fragmentPage = new FragmentPage(fragment, autotrackConfig);
            CACHE_PAGES.put(fragment, fragmentPage);
        }
        fragmentPage.setIsAutotrack(true);
        if (alias != null) fragmentPage.setAlias(alias);
        fragmentPage.setAttributes(attributes);
    }

    @UiThread
    public void removePage(SuperFragment<?> fragment) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
            return;
        }

        if (fragment == null) {
            Logger.w(TAG, "removePage: this fragment can not make superFragment");
            return;
        }

        if (fragment.getView() == null) {
            Logger.e(TAG, "removePage: this fragment getView is NULL");
            return;
        }

        Logger.d(TAG, "removePage: fragment is " + fragment.getRealFragment().getClass().getSimpleName());

        Page<?> page = ALL_PAGE_TREE.get(fragment.getActivity());
        if (page != null) {
            removePageFromTree(fragment, page);
        }
        CACHE_PAGES.remove(fragment);
    }

    private void removePageFromTree(SuperFragment<?> carrier, Page<?> page) {
        List<Page<?>> pages = page.getAllChildren();
        for (int i = pages.size() - 1; i >= 0; i--) {
            Page<?> onePage = pages.get(i);
            if (carrier.equals(onePage.getCarrier())) {
                pages.remove(i);
                return;
            } else {
                removePageFromTree(carrier, onePage);
            }
        }
    }

    private Page<?> searchPageParent(SuperFragment<?> fragment) {
        Page<?> pageParent = null;
        SuperFragment<?> parentFragment = fragment.getParentFragment();
        ActivityPage activityPage = findOrCreateActivityPage(fragment.getActivity());
        if (parentFragment == null) {
            pageParent = activityPage;
        } else if (activityPage != null) {
            while (parentFragment != null) {
                pageParent = searchFragmentPage(parentFragment, activityPage);
                if (pageParent != null) {
                    break;
                }
                // fragment not hook in gradle plugin, find next parent fragment.
                parentFragment = parentFragment.getParentFragment();
            }
            if (pageParent == null) {
                pageParent = activityPage;
            }
        }
        return pageParent;
    }

    protected Page<?> searchFragmentPage(SuperFragment<?> carrier, Page<?> page) {
        if (carrier.equals(page.getCarrier())) {
            return page;
        }

        List<Page<?>> pages = page.getAllChildren();
        for (Page<?> onePage : pages) {
            if (onePage != null) {
                Page<?> p = searchFragmentPage(carrier, onePage);
                if (p != null) {
                    return p;
                }
            }
        }

        return null;
    }

    public void setPageAttributes(Activity activity, Map<String, String> attributes) {
        Page page = findOrCreateActivityPage(activity);
        setPageAttributes(page, attributes);
    }

    public void setPageAttributes(SuperFragment<?> fragment, Map<String, String> attributes) {
        Page<?> page = findOrCreateFragmentPage(fragment);
        setPageAttributes(page, attributes);
    }

    private void setPageAttributes(Page<?> page, Map<String, String> attributes) {
        if (attributes == null) attributes = new HashMap<>();
        if (attributes.equals(page.getAttributes())) {
            Logger.w(TAG, "setPageAttributes is equals page.getAttributes");
            return;
        }
        page.setAttributes(attributes);
    }

    public Page<?> findPage(View view) {
        Page<?> page = ViewAttributeUtil.getViewPage(view);
        if (page != null) {
            return page;
        }

        if (view.getParent() instanceof View) {
            return findPage((View) view.getParent());
        }

        //需要考虑其他Window的上面的View
        Context viewContext = view.getContext();
        Activity activity = ActivityUtil.findActivity(viewContext);
        if (activity == null && activityStateProvider != null) {
            activity = activityStateProvider.getForegroundActivity();
        }
        if (activity != null) {
            ActivityPage activityPage = findOrCreateActivityPage(activity);
            if (!TextUtils.isEmpty(activity.getTitle())) {
                activityPage.setTitle(activity.getTitle().toString());
            } else {
                //一般不会进入
                //如穿山甲广告：会自己生成一个ActivityWrapper做代理并自己控制生命周期导致sdk的page无法命中，具体类为：PluginFragmentActivityWrapper
                activityPage.setTitle("WrapperActivity");
            }
            return activityPage;
        }
        return null;
    }
}
