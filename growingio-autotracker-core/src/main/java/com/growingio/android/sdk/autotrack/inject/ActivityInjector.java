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
package com.growingio.android.sdk.autotrack.inject;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.app.ListActivity;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ListView;

public class ActivityInjector {
    private ActivityInjector() {
    }

    public static void onActivityNewIntent(Activity activity, Intent intent) {
        InjectorProvider.get().onActivityNewIntent(activity, intent);
    }

    public static void menuItemOnOptionsItemSelected(Activity activity, MenuItem item) {
        InjectorProvider.get().activityOptionsItemOnClick(activity, item);
    }

    public static void expandableListActivityOnChildClick(ExpandableListActivity activity, ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        InjectorProvider.get().viewOnClick(v);
    }

    public static void listActivityOnListItemClick(ListActivity activity, ListView listView, View view, int position, long id) {
        InjectorProvider.get().viewOnClick(view);
    }
}
