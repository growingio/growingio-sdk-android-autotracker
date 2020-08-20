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

package com.growingio.android.sdk.autotrack.click;

import android.accounts.AccountAuthenticatorActivity;
import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.AliasActivity;
import android.app.ExpandableListActivity;
import android.app.LauncherActivity;
import android.app.ListActivity;
import android.app.NativeActivity;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ActionMenuView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toolbar;

import com.growingio.sdk.inject.annotation.BeforeSuper;

public class ViewClickInjector {
    private static final String TAG = "ViewClickInjector";

    private ViewClickInjector() {
    }

    @BeforeSuper(clazz = View.OnClickListener.class, method = "onClick", parameterTypes = {View.class})
    public static void viewOnClick(View.OnClickListener listener, View view) {
        ViewClickProvider.viewOnClick(view);
    }

    @BeforeSuper(clazz = DialogInterface.OnClickListener.class, method = "onClick", parameterTypes = {DialogInterface.class, int.class})
    public static void dialogOnClick(DialogInterface.OnClickListener listener, DialogInterface dialogInterface, int which) {
        if (dialogInterface instanceof AlertDialog) {
            ViewClickProvider.viewOnClick(((AlertDialog) dialogInterface).getButton(which));
        }
    }

    @BeforeSuper(clazz = AdapterView.OnItemClickListener.class, method = "onItemClick", parameterTypes = {AdapterView.class, View.class, int.class, long.class})
    public static void adapterViewOnItemClick(AdapterView.OnItemClickListener listener, AdapterView adapterView, View view, int position, long id) {
        ViewClickProvider.viewOnClick(view);
    }

    @BeforeSuper(clazz = AdapterView.OnItemSelectedListener.class, method = "onItemSelected", parameterTypes = {AdapterView.class, View.class, int.class, long.class})
    public static void adapterViewOnItemSelected(AdapterView.OnItemSelectedListener listener, AdapterView adapterView, View view, int position, long id) {
        if (adapterView instanceof Spinner) {
            // 目前只需要将Spinner的onItemSelected回调触发点击事件,因为Spinner的元素点击只会触发onItemSelected回调
            ViewClickProvider.viewOnClick(view);
        }
    }

    @BeforeSuper(clazz = ExpandableListView.OnGroupClickListener.class, method = "onGroupClick", parameterTypes = {ExpandableListView.class, View.class, int.class, long.class}, returnType = boolean.class)
    public static void expandableListViewOnGroupClick(ExpandableListView.OnGroupClickListener listener, ExpandableListView parent, View v, int groupPosition, long id) {
        ViewClickProvider.viewOnClick(v);
    }

    @BeforeSuper(clazz = ExpandableListView.OnChildClickListener.class, method = "onChildClick", parameterTypes = {ExpandableListView.class, View.class, int.class, int.class, long.class}, returnType = boolean.class)
    public static void expandableListViewOnChildClick(ExpandableListView.OnChildClickListener listener, ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        ViewClickProvider.viewOnClick(v);
    }

    @BeforeSuper(clazz = ExpandableListActivity.class, method = "onChildClick", parameterTypes = {ExpandableListView.class, View.class, int.class, int.class, long.class}, returnType = boolean.class)
    public static void expandableListActivityOnChildClick(ExpandableListActivity activity, ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        ViewClickProvider.viewOnClick(v);
    }

    @BeforeSuper(clazz = ListActivity.class, method = "onListItemClick", parameterTypes = {ListView.class, View.class, int.class, long.class})
    public static void listActivityOnListItemClick(ListActivity activity, ListView listView, View view, int position, long id) {
        ViewClickProvider.viewOnClick(view);
    }

    @BeforeSuper(clazz = CompoundButton.OnCheckedChangeListener.class, method = "onCheckedChanged", parameterTypes = {CompoundButton.class, boolean.class})
    public static void compoundButtonOnChecked(CompoundButton.OnCheckedChangeListener listener, CompoundButton button, boolean checked) {
        ViewClickProvider.viewOnClick(button);
    }

    @BeforeSuper(clazz = RadioGroup.OnCheckedChangeListener.class, method = "onCheckedChanged", parameterTypes = {RadioGroup.class, int.class})
    public static void radioGroupOnChecked(RadioGroup.OnCheckedChangeListener listener, RadioGroup radioGroup, int i) {
        ViewClickProvider.viewOnClick(radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()));
    }

    @BeforeSuper(clazz = RatingBar.OnRatingBarChangeListener.class, method = "onRatingChanged", parameterTypes = {RatingBar.class, float.class, boolean.class})
    public static void ratingBarOnRatingBarChange(RatingBar.OnRatingBarChangeListener listener, RatingBar ratingBar, float rating, boolean fromUser) {
        if (fromUser) {
            ViewClickProvider.viewOnClick(ratingBar);
        }
    }

    @BeforeSuper(clazz = SeekBar.OnSeekBarChangeListener.class, method = "onStopTrackingTouch", parameterTypes = {SeekBar.class})
    public static void seekBarOnSeekBarChange(SeekBar.OnSeekBarChangeListener listener, SeekBar seekBar) {
        ViewClickProvider.viewOnClick(seekBar);
    }

    @BeforeSuper(clazz = MenuItem.OnMenuItemClickListener.class, method = "onMenuItemClick", parameterTypes = {MenuItem.class}, returnType = boolean.class)
    public static void menuItemOnMenuItemClick(MenuItem.OnMenuItemClickListener listener, MenuItem item) {
        ViewClickProvider.menuItemOnClick(item);
    }

    @BeforeSuper(clazz = Toolbar.OnMenuItemClickListener.class, method = "onMenuItemClick", parameterTypes = {MenuItem.class}, returnType = boolean.class)
    public static void toolbarOnMenuItemClick(Toolbar.OnMenuItemClickListener listener, MenuItem item) {
        ViewClickProvider.menuItemOnClick(item);
    }

    @BeforeSuper(clazz = ActionMenuView.OnMenuItemClickListener.class, method = "onMenuItemClick", parameterTypes = {MenuItem.class}, returnType = boolean.class)
    public static void actionMenuViewOnMenuItemClick(ActionMenuView.OnMenuItemClickListener listener, MenuItem item) {
        ViewClickProvider.menuItemOnClick(item);
    }

    @BeforeSuper(clazz = PopupMenu.OnMenuItemClickListener.class, method = "onMenuItemClick", parameterTypes = {MenuItem.class}, returnType = boolean.class)
    public static void popupMenuOnMenuItemClick(PopupMenu.OnMenuItemClickListener listener, MenuItem item) {
        ViewClickProvider.menuItemOnClick(item);
    }

    @BeforeSuper(clazz = Activity.class,                     method = "onOptionsItemSelected", parameterTypes = {MenuItem.class}, returnType = boolean.class)
    @BeforeSuper(clazz = AccountAuthenticatorActivity.class, method = "onOptionsItemSelected", parameterTypes = {MenuItem.class}, returnType = boolean.class)
    @BeforeSuper(clazz = ActivityGroup.class,                method = "onOptionsItemSelected", parameterTypes = {MenuItem.class}, returnType = boolean.class)
    @BeforeSuper(clazz = AliasActivity.class,                method = "onOptionsItemSelected", parameterTypes = {MenuItem.class}, returnType = boolean.class)
    @BeforeSuper(clazz = ExpandableListActivity.class,       method = "onOptionsItemSelected", parameterTypes = {MenuItem.class}, returnType = boolean.class)
    @BeforeSuper(clazz = LauncherActivity.class,             method = "onOptionsItemSelected", parameterTypes = {MenuItem.class}, returnType = boolean.class)
    @BeforeSuper(clazz = ListActivity.class,                 method = "onOptionsItemSelected", parameterTypes = {MenuItem.class}, returnType = boolean.class)
    @BeforeSuper(clazz = NativeActivity.class,               method = "onOptionsItemSelected", parameterTypes = {MenuItem.class}, returnType = boolean.class)
    @BeforeSuper(clazz = TabActivity.class,                  method = "onOptionsItemSelected", parameterTypes = {MenuItem.class}, returnType = boolean.class)
    @BeforeSuper(clazz = PreferenceActivity.class,           method = "onOptionsItemSelected", parameterTypes = {MenuItem.class}, returnType = boolean.class)
    public static void menuItemOnOptionsItemSelected(Activity activity, MenuItem item) {
        ViewClickProvider.menuItemOnClick(item);
    }
}
