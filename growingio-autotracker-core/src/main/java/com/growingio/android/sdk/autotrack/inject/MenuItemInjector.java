/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.sdk.autotrack.inject;

import android.view.MenuItem;
import android.widget.ActionMenuView;
import android.widget.PopupMenu;
import android.widget.Toolbar;

public class MenuItemInjector {
    private static final String TAG = "MenuItemInjector";

    private MenuItemInjector() {
    }

    public static void toolbarOnMenuItemClick(Toolbar.OnMenuItemClickListener listener, MenuItem item) {
        ViewClickProvider.menuItemOnClick(item);
    }
/*
    public static void toolbarXOnMenuItemClick(androidx.appcompat.widget.Toolbar.OnMenuItemClickListener listener, MenuItem item) {
        ViewClickProvider.menuItemOnClick(item);
    }

    public static void toolbarSupportOnMenuItemClick(android.support.v7.widget.Toolbar.OnMenuItemClickListener listener, MenuItem item) {
        ViewClickProvider.menuItemOnClick(item);
    }
*/

    public static void actionMenuViewOnMenuItemClick(ActionMenuView.OnMenuItemClickListener listener, MenuItem item) {
        ViewClickProvider.menuItemOnClick(item);
    }

    public static void popupMenuOnMenuItemClick(PopupMenu.OnMenuItemClickListener listener, MenuItem item) {
        ViewClickProvider.menuItemOnClick(item);
    }

/*
    public static void popupMenuXOnMenuItemClick(androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener listener, MenuItem item) {
        ViewClickProvider.menuItemOnClick(item);
    }

    public static void popupMenuSupportOnMenuItemClick(android.support.v7.widget.PopupMenu.OnMenuItemClickListener listener, MenuItem item) {
        ViewClickProvider.menuItemOnClick(item);
    }
*/

/*
    public static void naviBarViewOnMenuItemClick(com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener listener, MenuItem item) {
        ViewClickProvider.menuItemOnClick(item);
    }

    public static void tabLayoutSelected(TabLayout.Tab tab) {
        ViewClickProvider.viewOnClick(tab.view);
    }
*/

}
