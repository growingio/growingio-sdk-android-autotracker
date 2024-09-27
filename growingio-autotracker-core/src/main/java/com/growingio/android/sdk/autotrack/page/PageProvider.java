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
import android.view.View;

import androidx.annotation.UiThread;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.AutotrackConfig;
import com.growingio.android.sdk.autotrack.util.XmlParserUtil;
import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;
import com.growingio.android.sdk.track.utils.ActivityUtil;


import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private PageConfig pageConfig;

    private PageProvider() {
        ALL_PAGE_TREE.clear();
        CACHE_PAGES.clear();
    }

    @Override
    public void setup(TrackerContext context) {
        activityStateProvider = context.getActivityStateProvider();

        ConfigurationProvider configurationProvider = context.getConfigurationProvider();
        AutotrackConfig autotrackConfig = configurationProvider.getConfiguration(AutotrackConfig.class);

        loadPageConfig(context, autotrackConfig, configurationProvider.isDowngrade());

        activityStateProvider.registerActivityLifecycleListener(this);
    }

    private void loadPageConfig(TrackerContext context, AutotrackConfig autotrackConfig, boolean isDowngrade) {
        if (autotrackConfig != null) {
            boolean isActivityPageEnabled = autotrackConfig.getAutotrackOptions().isActivityPageEnabled();
            boolean isFragmentPageEnabled = autotrackConfig.getAutotrackOptions().isFragmentPageEnabled();
            boolean enableFragmentTag = autotrackConfig.isEnableFragmentTag();
            List<PageRule> pageRuleList = XmlParserUtil.loadPageRuleXml(context, autotrackConfig.getPageXmlRes());
            autotrackConfig.getPageRules().addAll(0, pageRuleList);

            List<PageRule> pageRules = Collections.unmodifiableList(autotrackConfig.getPageRules());
            pageConfig = new PageConfig(pageRules, isActivityPageEnabled, isFragmentPageEnabled, enableFragmentTag, isDowngrade, autotrackConfig.isAutotrack());
        } else {
            pageConfig = new PageConfig(null, isDowngrade, isDowngrade, false, isDowngrade, true);
        }
    }


    @Override
    public void shutdown() {
        ALL_PAGE_TREE.clear();
        CACHE_PAGES.clear();
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
            createOrResumeActivity(activity);
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_DESTROYED) {
            destroyActivity(activity);
        }
    }

    @UiThread
    private void createOrResumeActivity(Activity activity) {
        ActivityPage page = generateActivityPageInTree(activity);
        ViewAttributeUtil.setViewPage(activity.getWindow().getDecorView(), page);
        page.refreshShowTimestamp();
        sendPage(page);
    }

    private ActivityPage generateActivityPageInTree(Activity activity) {
        ActivityPage page = ALL_PAGE_TREE.get(activity);
        if (page == null && CACHE_PAGES.containsKey(activity)) {
            page = (ActivityPage) CACHE_PAGES.get(activity);
            CACHE_PAGES.remove(activity);
        }
        if (page == null) {
            page = new ActivityPage(activity, pageConfig);
        }
        ALL_PAGE_TREE.put(activity, page);
        return page;
    }

    protected ActivityPage findOrCreateActivityPage(Activity activity) {
        if (activity == null) return null;
        ActivityPage page = ALL_PAGE_TREE.get(activity);
        if (page != null) {
            return page;
        }
        if (CACHE_PAGES.containsKey(activity)) {
            page = (ActivityPage) CACHE_PAGES.get(activity);
            return page;
        }
        if (page == null) {
            page = new ActivityPage(activity, pageConfig);
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
                sendPage(page);
            }
            return;
        }
        if (CACHE_PAGES.containsKey(activity)) {
            page = (ActivityPage) CACHE_PAGES.get(activity);
        } else {
            page = new ActivityPage(activity, pageConfig);
            CACHE_PAGES.put(activity, page);
        }
        page.setIsAutotrack(true);
        if (alias != null) page.setAlias(alias);
        page.setAttributes(attributes);
    }

    public void ignoreActivity(Activity activity, boolean ignored) {
        ActivityPage page = ALL_PAGE_TREE.get(activity);
        if (page != null) {
            page.setIgnore(ignored);
            return;
        }
        if (CACHE_PAGES.containsKey(activity)) {
            page = (ActivityPage) CACHE_PAGES.get(activity);
        } else {
            page = new ActivityPage(activity, pageConfig);
            CACHE_PAGES.put(activity, page);
        }
        page.setIgnore(ignored);
    }

    public ActivityPage searchActivityPage(Activity activity) {
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

    private void showPagesFromHidden(Page<?> page) {
        page.refreshShowTimestamp();
        Logger.d(TAG, "refreshPages: " + page.path());
        sendPage(page);
        if (!page.getAllChildren().isEmpty()) {
            for (Page<?> child : page.getAllChildren()) {
                showPagesFromHidden(child);
            }
        }
    }

    private void sendPage(Page<?> page) {
        if (page.isAutotrack()) {
            Logger.d(TAG, "sendPage: path = " + page.path());
            generatePageEvent(page);
        } else {
            Logger.d(TAG, "AutotrackOptions: page enable is false");
        }
    }

    private void generatePageEvent(Page<?> page) {
        String orientation = TrackMainThread.trackMain().getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
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
        if (fragment.getActivity() == null) return null;
        Activity activity = fragment.getActivity();
        ActivityPage page = findOrCreateActivityPage(activity);

        // find fragment page
        Page<?> result = searchFragmentPage(fragment, page);
        if (result != null) {
            return (FragmentPage) result;
        }
        Object realFragment = fragment.getRealFragment();
        if (CACHE_PAGES.containsKey(realFragment)) {
            return (FragmentPage) CACHE_PAGES.get(realFragment);
        }
        FragmentPage fragmentPage = new FragmentPage(fragment, pageConfig);
        CACHE_PAGES.put(realFragment, fragmentPage);
        return fragmentPage;
    }

    @UiThread
    private void destroyActivity(Activity activity) {
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
            if (activity == null) return;
            Page<?> page = ALL_PAGE_TREE.get(activity);
            if (page != null) {
                Page<?> fragmentPage = searchFragmentPage(fragment, page);
                if (fragmentPage == null) {
                    Logger.w(TAG, "fragmentOnHiddenChanged: fragment is NULL");
                } else {
                    showPagesFromHidden(fragmentPage);
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

        sendPage(page);
    }

    private FragmentPage generateFragmentPage(SuperFragment<?> fragment) {
        // find activity page
        ActivityPage page = findOrCreateActivityPage(fragment.getActivity());

        // find fragment page
        Page<?> result = searchFragmentPage(fragment, page);
        if (result != null) {
            return (FragmentPage) result;
        }
        Object realFragment = fragment.getRealFragment();
        if (realFragment != null && CACHE_PAGES.containsKey(realFragment)) {
            FragmentPage fragmentPage = (FragmentPage) CACHE_PAGES.get(realFragment);
            CACHE_PAGES.remove(realFragment);
            return fragmentPage;
        }
        return new FragmentPage(fragment, pageConfig);
    }

    public void autotrackFragment(SuperFragment<?> fragment, String alias, Map<String, String> attributes) {
        if (fragment.getActivity() == null) return;
        // find activity page
        ActivityPage page = findOrCreateActivityPage(fragment.getActivity());

        // find fragment page
        Page<?> fragmentPage = searchFragmentPage(fragment, page);
        if (fragmentPage != null) {
            if (alias != null) fragmentPage.setAlias(alias);
            fragmentPage.setAttributes(attributes);
            if (!fragmentPage.isAutotrack()) {
                fragmentPage.setIsAutotrack(true);
                sendPage(fragmentPage);
            }
            return;
        }
        Object realFragment = fragment.getRealFragment();
        if (CACHE_PAGES.containsKey(realFragment)) {
            fragmentPage = (FragmentPage) CACHE_PAGES.get(realFragment);
        } else {
            fragmentPage = new FragmentPage(fragment, pageConfig);
            CACHE_PAGES.put(realFragment, fragmentPage);
        }
        fragmentPage.setIsAutotrack(true);
        if (alias != null) fragmentPage.setAlias(alias);
        fragmentPage.setAttributes(attributes);
    }

    public void ignoreFragment(SuperFragment<?> fragment, boolean ignored) {
        if (fragment.getActivity() == null) return;
        // find activity page
        ActivityPage page = findOrCreateActivityPage(fragment.getActivity());

        // find fragment page
        Page<?> fragmentPage = searchFragmentPage(fragment, page);
        if (fragmentPage != null) {
            fragmentPage.setIgnore(ignored);
            return;
        }
        Object realFragment = fragment.getRealFragment();
        if (CACHE_PAGES.containsKey(realFragment)) {
            fragmentPage = (FragmentPage) CACHE_PAGES.get(realFragment);
        } else {
            fragmentPage = new FragmentPage(fragment, pageConfig);
            CACHE_PAGES.put(realFragment, fragmentPage);
        }
        fragmentPage.setIgnore(ignored);
    }

    @UiThread
    public void removePage(SuperFragment<?> fragment) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
            return;
        }

        if (fragment == null) {
            Logger.w(TAG, "removePageToCache: this fragment can not make superFragment");
            return;
        }

        Logger.d(TAG, "removePageToCache: fragment is " + fragment.getSimpleName());

        Object realFragment = fragment.getRealFragment();
        if (fragment.getRealFragment() != null) {
            Activity fragmentActivity = fragment.getActivity();
            Page<?> page = ALL_PAGE_TREE.get(fragmentActivity);
            if (page != null) {
                Page<?> removePage = removePageFromTree(fragment, page);
                if (removePage != null) {
                    CACHE_PAGES.put(realFragment, removePage);
                }
            }
        }
    }

    private Page<?> removePageFromTree(SuperFragment<?> carrier, Page<?> page) {
        Iterator<Page<?>> iterator = page.getAllChildren().iterator();
        while (iterator.hasNext()) {
            Page<?> onePage = iterator.next();
            if (carrier.equals(onePage.getCarrier())) {
                iterator.remove();
                return onePage;
            } else {
                Page<?> childPage = removePageFromTree(carrier, onePage);
                if (childPage != null) return childPage;
            }
        }
        return null;
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

        Set<Page<?>> pages = page.getAllChildren();
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
        if (page == null || attributes == null) return;
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
            return findOrCreateActivityPage(activity);
        }
        return null;
    }

    public Page<?> findLatestPage(){
        if (ALL_PAGE_TREE.isEmpty()) {
            return null;
        }
        Page<?> page = null;
        for (ActivityPage activityPage : ALL_PAGE_TREE.values()) {
            if (page == null) {
                page = activityPage;
            } else {
                if (activityPage.getShowTimestamp() > page.getShowTimestamp()) {
                    page = activityPage;
                }
            }
        }
        return page;
    }
}
