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
import android.app.Fragment;
import android.content.res.Resources;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public abstract class SuperFragment<T> {
    private final T mRealFragment;

    //将SuperFragment对象和Fragment对象产生关联，防止GC后SuperFragment对象被释放，保证SuperFragment对象和Fragment对象生命周期一致
    private static final Map<Object, SuperFragment<?>> ASSOCIATION_OBJECTS = new WeakHashMap<>();

    protected SuperFragment(T realFragment) {
        mRealFragment = realFragment;
        ASSOCIATION_OBJECTS.put(mRealFragment, this);
    }

    @Nullable
    public static SuperFragment<Fragment> make(Fragment fragment) {
        if (fragment == null) {
            return null;
        }
        return new SystemFragment(fragment);
    }

    @Nullable
    public static SuperFragment<android.support.v4.app.Fragment> make(android.support.v4.app.Fragment fragment) {
        if (fragment == null) {
            return null;
        }
        return new V4Fragment(fragment);
    }

    @Nullable
    public static SuperFragment<androidx.fragment.app.Fragment> make(androidx.fragment.app.Fragment fragment) {
        if (fragment == null) {
            return null;
        }
        return new AndroidXFragment(fragment);
    }

    public T getRealFragment() {
        return mRealFragment;
    }

    public abstract int getId();

    public abstract String getTag();

    public abstract View getView();

    public abstract Resources getResources();

    public abstract Activity getActivity();

    public abstract SuperFragment<T> getParentFragment();

    public abstract boolean getUserVisibleHint();

    public abstract boolean isResumed();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SuperFragment<?> that = (SuperFragment<?>) o;

        return mRealFragment != null ? mRealFragment.equals(that.mRealFragment) : that.mRealFragment == null;
    }

    @Override
    public int hashCode() {
        return mRealFragment != null ? mRealFragment.hashCode() : 0;
    }

    public abstract boolean isHidden();

    private static class SystemFragment extends SuperFragment<Fragment> {

        protected SystemFragment(Fragment realFragment) {
            super(realFragment);
        }

        @Override
        public int getId() {
            return getRealFragment().getId();
        }

        @Override
        public String getTag() {
            return getRealFragment().getTag();
        }

        @Override
        public View getView() {
            return getRealFragment().getView();
        }

        @Override
        public Resources getResources() {
            return getRealFragment().getResources();
        }

        @Override
        public Activity getActivity() {
            return getRealFragment().getActivity();
        }

        @Override
        public SuperFragment<Fragment> getParentFragment() {
            return make(getRealFragment().getParentFragment());
        }

        @Override
        public boolean getUserVisibleHint() {
            return getRealFragment().getUserVisibleHint();
        }

        @Override
        public boolean isResumed() {
            return getRealFragment().isResumed();
        }

        @Override
        public boolean isHidden() {
            return getRealFragment().isHidden();
        }
    }

    private static class V4Fragment extends SuperFragment<android.support.v4.app.Fragment> {
        protected V4Fragment(android.support.v4.app.Fragment realFragment) {
            super(realFragment);
        }

        @Override
        public int getId() {
            return getRealFragment().getId();
        }

        @Override
        public String getTag() {
            return getRealFragment().getTag();
        }

        @Override
        public View getView() {
            return getRealFragment().getView();
        }

        @Override
        public Resources getResources() {
            return getRealFragment().getResources();
        }

        @Override
        public Activity getActivity() {
            return getRealFragment().getActivity();
        }

        @Override
        public SuperFragment<android.support.v4.app.Fragment> getParentFragment() {
            return make(getRealFragment().getParentFragment());
        }

        @Override
        public boolean getUserVisibleHint() {
            return getRealFragment().getUserVisibleHint();
        }

        @Override
        public boolean isResumed() {
            return getRealFragment().isResumed();
        }

        @Override
        public boolean isHidden() {
            return getRealFragment().isHidden();
        }
    }

    private static class AndroidXFragment extends SuperFragment<androidx.fragment.app.Fragment> {
        protected AndroidXFragment(androidx.fragment.app.Fragment realFragment) {
            super(realFragment);
        }

        @Override
        public int getId() {
            return getRealFragment().getId();
        }

        @Override
        public String getTag() {
            return getRealFragment().getTag();
        }

        @Override
        public View getView() {
            return getRealFragment().getView();
        }

        @Override
        public Resources getResources() {
            return getRealFragment().getResources();
        }

        @Override
        public Activity getActivity() {
            return getRealFragment().getActivity();
        }

        @Nullable
        @Override
        public SuperFragment<androidx.fragment.app.Fragment> getParentFragment() {
            return make(getRealFragment().getParentFragment());
        }

        @Override
        public boolean getUserVisibleHint() {
            return getRealFragment().getUserVisibleHint();
        }

        @Override
        public boolean isResumed() {
            return getRealFragment().isResumed();
        }

        @Override
        public boolean isHidden() {
            return getRealFragment().isHidden();
        }
    }
}
