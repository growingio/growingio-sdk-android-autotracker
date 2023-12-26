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
import android.app.Fragment;
import android.content.res.Resources;
import android.view.View;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

public abstract class SuperFragment<T> {
    private final WeakReference<T> mRealFragment;
    private final String simpleName;

    protected SuperFragment(T realFragment) {
        simpleName = realFragment.getClass().getSimpleName();
        mRealFragment = new WeakReference<>(realFragment);
    }

    @Nullable
    public static SuperFragment<Fragment> make(Fragment fragment) {
        if (fragment == null) {
            return null;
        }
        return new SystemFragment(fragment);
    }

    @Nullable
    public static SuperFragment<android.support.v4.app.Fragment> makeSupport(android.support.v4.app.Fragment fragment) {
        if (fragment == null) {
            return null;
        }
        return new V4Fragment(fragment);
    }

    @Nullable
    public static SuperFragment<androidx.fragment.app.Fragment> makeX(androidx.fragment.app.Fragment fragment) {
        if (fragment == null) {
            return null;
        }
        return new AndroidXFragment(fragment);
    }

    @Nullable
    public T getRealFragment() {
        return mRealFragment.get();
    }

    public String getSimpleName() {
        return simpleName;
    }

    public abstract int getId();

    public abstract String getTag();

    @Nullable
    public abstract View getView();

    public abstract String getResourceEntryName(int id);

    @Nullable
    public abstract Activity getActivity();

    @Nullable
    public abstract SuperFragment<T> getParentFragment();

    public abstract boolean getUserVisibleHint();

    public abstract boolean isResumed();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SuperFragment<?> that = (SuperFragment<?>) o;
        if (mRealFragment.get() == null) {
            return false;
        }
        return mRealFragment.get().equals(that.mRealFragment.get());
    }

    @Override
    public int hashCode() {
        return mRealFragment != null ? mRealFragment.hashCode() : 0;
    }

    public abstract boolean isHidden();

    private static class SystemFragment extends SuperFragment<Fragment> {

        private final String tag;
        private final int id;

        protected SystemFragment(Fragment realFragment) {
            super(realFragment);
            tag = realFragment.getTag();
            id = realFragment.getId();
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public String getTag() {
            return tag;
        }

        @Nullable
        @Override
        public View getView() {
            if (getRealFragment() != null) return getRealFragment().getView();
            return null;
        }

        @Override
        public String getResourceEntryName(int id) {
            if (getRealFragment() == null) return "SystemFragment";
            try {
                if (id == -1) {
                    id = getRealFragment().getId();
                }
                return getRealFragment().getResources().getResourceEntryName(id);
            } catch (Resources.NotFoundException ignored) {
            }
            return "SystemFragment";
        }

        @Nullable
        @Override
        public Activity getActivity() {
            if (getRealFragment() != null) return getRealFragment().getActivity();
            return null;
        }

        @Nullable
        @Override
        public SuperFragment<Fragment> getParentFragment() {
            if (getRealFragment() != null) return make(getRealFragment().getParentFragment());
            return null;
        }

        @Override
        public boolean getUserVisibleHint() {
            if (getRealFragment() != null) return getRealFragment().getUserVisibleHint();
            return false;
        }

        @Override
        public boolean isResumed() {
            if (getRealFragment() != null) return getRealFragment().isResumed();
            return false;
        }

        @Override
        public boolean isHidden() {
            if (getRealFragment() != null) return getRealFragment().isHidden();
            return true;
        }
    }

    private static class V4Fragment extends SuperFragment<android.support.v4.app.Fragment> {

        private final String tag;
        private final int id;

        protected V4Fragment(android.support.v4.app.Fragment realFragment) {
            super(realFragment);
            tag = realFragment.getTag();
            id = realFragment.getId();
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public String getTag() {
            return tag;
        }

        @Nullable
        @Override
        public View getView() {
            if (getRealFragment() != null) return getRealFragment().getView();
            return null;
        }

        @Override
        public String getResourceEntryName(int id) {
            if (getRealFragment() == null) return "SystemFragment";
            try {
                if (id == -1) {
                    id = getRealFragment().getId();
                }
                return getRealFragment().getResources().getResourceEntryName(id);
            } catch (Resources.NotFoundException ignored) {
            }
            return "SystemFragment";
        }

        @Nullable
        @Override
        public Activity getActivity() {
            if (getRealFragment() != null) return getRealFragment().getActivity();
            return null;
        }

        @Override
        public SuperFragment<android.support.v4.app.Fragment> getParentFragment() {
            if (getRealFragment() != null)
                return makeSupport(getRealFragment().getParentFragment());
            return null;
        }

        @Override
        public boolean getUserVisibleHint() {
            if (getRealFragment() != null) return getRealFragment().getUserVisibleHint();
            return false;
        }

        @Override
        public boolean isResumed() {
            if (getRealFragment() != null) return getRealFragment().isResumed();
            return false;
        }

        @Override
        public boolean isHidden() {
            if (getRealFragment() != null) return getRealFragment().isHidden();
            return true;
        }
    }

    private static class AndroidXFragment extends SuperFragment<androidx.fragment.app.Fragment> {

        private final String tag;
        private final int id;

        protected AndroidXFragment(androidx.fragment.app.Fragment realFragment) {
            super(realFragment);
            tag = realFragment.getTag();
            id = realFragment.getId();
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public String getTag() {
            return tag;
        }

        @Nullable
        @Override
        public View getView() {
            if (getRealFragment() != null) return getRealFragment().getView();
            return null;
        }

        @Override
        public String getResourceEntryName(int id) {
            if (getRealFragment() == null || id == View.NO_ID) return null;
            try {
                return getRealFragment().getResources().getResourceEntryName(id);
            } catch (Resources.NotFoundException ignored) {
            }
            return null;
        }

        @Nullable
        @Override
        public Activity getActivity() {
            if (getRealFragment() != null) return getRealFragment().getActivity();
            return null;
        }

        @Nullable
        @Override
        public SuperFragment<androidx.fragment.app.Fragment> getParentFragment() {
            if (getRealFragment() != null) return makeX(getRealFragment().getParentFragment());
            return null;
        }

        @Override
        public boolean getUserVisibleHint() {
            if (getRealFragment() != null) return getRealFragment().getUserVisibleHint();
            return false;
        }

        @Override
        public boolean isResumed() {
            if (getRealFragment() != null) return getRealFragment().isResumed();
            return false;
        }

        @Override
        public boolean isHidden() {
            if (getRealFragment() != null) return getRealFragment().isHidden();
            return true;
        }
    }
}
