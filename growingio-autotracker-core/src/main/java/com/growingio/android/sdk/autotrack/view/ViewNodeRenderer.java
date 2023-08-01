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
package com.growingio.android.sdk.autotrack.view;

import android.app.Activity;
import android.view.MenuItem;
import android.view.View;

import com.growingio.android.sdk.track.view.DecorView;

import org.json.JSONArray;

import java.util.List;

/**
 * <p>
 *
 * @author cpacm 2023/7/10
 */
interface ViewNodeRenderer {
    String TAG = "ViewNodeRenderer";

    void generateMenuItemEvent(Activity activity, MenuItem menuItem);

    void generateViewClickEvent(View view);

    void generateViewChangeEvent(View view);

    JSONArray buildScreenPages(List<DecorView> decorViews);

    JSONArray buildScreenViews(List<DecorView> decorViews);
}
