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

import java.lang.ref.WeakReference;

/**
 * <p>
 *
 * @author cpacm 2023/8/15
 */
public class SuperActivity {
    private final WeakReference<Activity> realActivity;
    private final String simpleName;

    public SuperActivity(Activity activity) {
        this.realActivity = new WeakReference<>(activity);
        simpleName = activity.getClass().getSimpleName();
    }

    public Activity getRealActivity() {
        return realActivity.get();
    }

    public String getSimpleName() {
        return simpleName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SuperActivity that = (SuperActivity) o;
        if (realActivity.get() == null) {
            return false;
        }
        return realActivity.get().equals(that.realActivity.get());
    }
}
