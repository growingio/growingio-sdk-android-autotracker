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
import android.content.ContextWrapper;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.growingio.android.sdk.autotrack.IgnorePolicy;
import com.growingio.android.sdk.autotrack.providers.EventAutotrackGeneratorProvider;
import com.growingio.android.sdk.autotrack.util.ViewAttributeUtil;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.utils.GIOProviders;
import com.growingio.android.sdk.track.utils.LogUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public interface PageProvider {

    void start();

    Page<?> findPage(View view);

    void createOrResumePage(SuperFragment<?> superFragment);

    void removePage(SuperFragment<?> superFragment);

    void setActivityAlias(Activity activity, String alias);

    void setFragmentAlias(SuperFragment<?> fragment, String alias);

    void addActivityWithIgnorePolicy(Activity activity, IgnorePolicy policy);

    void addFragmentWithIgnorePolicy(SuperFragment<?> fragment, IgnorePolicy policy);

    void setPageAttributes(Activity activity, Map<String, String> attributes);

    void setPageAttributes(SuperFragment<?> fragment, Map<String, String> attributes);

    class PagePolicy implements PageProvider, IActivityLifecycle {
        private static final String TAG = "PagePolicy";

        private static final Map<Activity, ActivityPage> ALL_PAGE_TREE = new WeakHashMap<>();
        private static final Map<Object, String> ALL_PAGE_ALIAS = new WeakHashMap<>();
        private static final Set<Class<?>> IGNORE_PAGE_CLASSES = new HashSet<>();
        private static final Map<Object, Map<String, String>> PAGE_ATTRIBUTES_CACHE = new WeakHashMap<>();

        private static final Map<Object, IgnorePolicy> ALL_PAGE_IGNORE_POLICY = new WeakHashMap<>();

        private PagePolicy() {
        }

        public static PageProvider get() {
            return GIOProviders.provider(PageProvider.class, new GIOProviders.DefaultCallback<PageProvider>() {
                @Override
                public PageProvider value() {
                    return new PagePolicy();
                }
            });
        }

        @Override
        public void start() {
            ActivityStateProvider.ActivityStatePolicy.get().registerActivityLifecycleListener(this);
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

        public void addIgnoreActivityClass(Class<? extends Activity> activityClazz) {
            IGNORE_PAGE_CLASSES.add(activityClazz);
        }

        public void addIgnoreFragmentClass(Class<? extends Fragment> fragmentClazz) {
            IGNORE_PAGE_CLASSES.add(fragmentClazz);
        }

        @Override
        public void addActivityWithIgnorePolicy(Activity activity, IgnorePolicy policy) {
            if (activity != null && policy != null) {
                ALL_PAGE_IGNORE_POLICY.put(activity, policy);
            }
        }

        @Override
        public void addFragmentWithIgnorePolicy(SuperFragment<?> fragment, IgnorePolicy policy) {
            if (fragment != null && policy != null) {
                ALL_PAGE_IGNORE_POLICY.put(fragment, policy);
            }
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
                Log.e(TAG, "createOrResumePage: path = " + page.path());
                EventAutotrackGeneratorProvider.EventAutotrackGenerator.get().generatePageEvent(page.path(), page.getTitle(), page.getShowTimestamp());
                reissuePageAttributes(page);
            } else {
                Log.e(TAG, "createOrResumePage: path = " + page.path() + " is ignored");
            }
        }

        @Override
        public void setActivityAlias(Activity activity, String alias) {
            if (TextUtils.isEmpty(alias)) {
                return;
            }

            ALL_PAGE_ALIAS.put(activity, alias);
        }

        @Override
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
            if (IGNORE_PAGE_CLASSES.contains(activity.getClass())) {
                return true;
            }

            Activity parentActivity = activity.getParent();
            IgnorePolicy selfPolicy = ALL_PAGE_IGNORE_POLICY.get(activity);
            IgnorePolicy parentPolicy = ALL_PAGE_IGNORE_POLICY.get(parentActivity);


            if (selfPolicy == null &&
                    (parentPolicy == null || parentPolicy == IgnorePolicy.IGNORE_SELF)) {
                return false;
            }

            if (selfPolicy == IgnorePolicy.IGNORE_CHILD &&
                    (parentPolicy == null || parentPolicy == IgnorePolicy.IGNORE_SELF)) {
                return false;
            }

            return true;
        }

        private boolean isIgnoreFragment(SuperFragment<?> fragment) {
            if (IGNORE_PAGE_CLASSES.contains(fragment.getRealFragment().getClass())) {
                return true;
            }

            SuperFragment<?> parentFragment = fragment.getParentFragment();
            IgnorePolicy selfPolicy = ALL_PAGE_IGNORE_POLICY.get(fragment);
            IgnorePolicy parentPolicy = ALL_PAGE_IGNORE_POLICY.get(parentFragment);

            if (selfPolicy == null &&
                    (parentPolicy == null || parentPolicy == IgnorePolicy.IGNORE_SELF)) {
                return false;
            }

            if (selfPolicy == IgnorePolicy.IGNORE_CHILD &&
                    (parentPolicy == null || parentPolicy == IgnorePolicy.IGNORE_SELF)) {
                return false;
            }

            return true;
        }

        @UiThread
        private void removePage(Activity activity) {
            Log.e(TAG, "removePage: activity is " + activity);
            ALL_PAGE_TREE.remove(activity);
            PAGE_ATTRIBUTES_CACHE.remove(activity);
        }

        @UiThread
        @Override
        public void createOrResumePage(SuperFragment<?> fragment) {
            if (fragment == null) {
                LogUtil.e(TAG, "createOrResumePage: this fragment can not make superFragment");
                return;
            }

            if (fragment.getView() == null) {
                LogUtil.e(TAG, "createOrResumePage: this fragment getView is NULL");
                return;
            }

            if (!fragment.getUserVisibleHint()) {
                LogUtil.e(TAG, "createOrResumePage: this fragment's UI is currently invisible to the user");
                return;
            }

            if (fragment.getActivity() == null) {
                LogUtil.e(TAG, "createOrResumePage: this fragment getActivity is NULL");
                return;
            }

            if (!fragment.isResumed()) {
                LogUtil.e(TAG, "createOrResumePage: this fragment not is resumed");
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
                EventAutotrackGeneratorProvider.EventAutotrackGenerator.get().generatePageEvent(page.path(), page.getTitle(), page.getShowTimestamp());
                reissuePageAttributes(page);
            } else {
                Log.e(TAG, "createOrResumePage: path = " + page.path() + " is ignored");
            }
        }

        @UiThread
        @Override
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

        @Override
        public void setPageAttributes(Activity activity, Map<String, String> attributes) {
            ActivityPage page = ALL_PAGE_TREE.get(activity);
            if (page != null) {
                setPageAttributes(page, attributes);
            } else {
                Log.e(TAG, "setPageAttributes: can't find Activity " + activity);
                PAGE_ATTRIBUTES_CACHE.put(activity, attributes);
            }
        }

        @Override
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
                EventAutotrackGeneratorProvider.EventAutotrackGenerator.get().generatePageAttributesEvent(page.path(), page.getShowTimestamp(), page.getAttributes());
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

//    @UiThread
//    public static Page<?> make(androidx.fragment.app.Fragment fragment) {
//        return new XFragmentPage(fragment);
//    }

//    private static PageGroup<?> findPageParent(Activity activity, View view) {
//        ActivityPage activityPage = ALL_PAGE_TREE.get(activity);
//        ViewParent viewParent = view.getParent();
//        while (viewParent instanceof ViewGroup) {
//            PageGroup<?> parent = findPage(view, activityPage);
//            if (parent != null) {
//                return parent;
//            }
//            viewParent = viewParent.getParent();
//        }
//        return (PageGroup<?>) make(activity);
//    }

        @Override
        public Page<?> findPage(View view) {
            Page<?> page = ViewAttributeUtil.getViewPage(view);
            if (page != null && !page.isIgnored()) {
                return page;
            }

            if (view.getParent() instanceof View) {
                return findPage((View) view.getParent());
            }

            //需要考虑Dialog的上面的View
            Context viewContext = view.getContext();
            if (viewContext instanceof Activity) {
                return ALL_PAGE_TREE.get(viewContext);
            } else if (viewContext instanceof ContextWrapper) {
                Context baseContext = ((ContextWrapper) viewContext).getBaseContext();
                if (baseContext instanceof Activity) {
                    return ALL_PAGE_TREE.get(baseContext);
                }
            }

            // TODO: 2020/6/10 这种情况需要观察
            throw new NullPointerException("Page is NULL");
        }

        public String findPageXpath(View view) {
            Page<?> page = findPage(view);
            if (page != null) {
                return page.path();
            }
            return "";
        }
    }
}
