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
package com.growingio.sdk.plugin.autotrack.hook;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 该Class是由 inject-compiler 自动生成的，请不要随意更改！
 */
public class HookClassesConfig {
  private static final Map<String, TargetClass> AROUND_HOOK_CLASSES = new HashMap<>();

  private static final Map<String, TargetClass> SUPER_HOOK_CLASSES = new HashMap<>();

  static {
    putAroundHookMethod("android/webkit/WebView", "loadUrl", "(Ljava/lang/String;)V", "com/growingio/android/sdk/autotrack/inject/WebViewInjector", "webkitWebViewLoadUrl", "(Landroid/webkit/WebView;Ljava/lang/String;)V", false);
    putAroundHookMethod("android/webkit/WebView", "loadUrl", "(Ljava/lang/String;Ljava/util/Map;)V", "com/growingio/android/sdk/autotrack/inject/WebViewInjector", "webkitWebViewLoadUrl", "(Landroid/webkit/WebView;Ljava/lang/String;Ljava/util/Map;)V", false);
    putAroundHookMethod("android/webkit/WebView", "loadData", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "com/growingio/android/sdk/autotrack/inject/WebViewInjector", "webkitWebViewLoadData", "(Landroid/webkit/WebView;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
    putAroundHookMethod("android/webkit/WebView", "loadDataWithBaseURL", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "com/growingio/android/sdk/autotrack/inject/WebViewInjector", "webkitWebViewLoadDataWithBaseURL", "(Landroid/webkit/WebView;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
    putAroundHookMethod("android/webkit/WebView", "postUrl", "(Ljava/lang/String;[B)V", "com/growingio/android/sdk/autotrack/inject/WebViewInjector", "webkitWebViewPostUrl", "(Landroid/webkit/WebView;Ljava/lang/String;[B)V", false);
    putAroundHookMethod("com/tencent/smtt/sdk/WebView", "loadUrl", "(Ljava/lang/String;)V", "com/growingio/android/sdk/autotrack/inject/WebViewInjector", "x5WebViewLoadUrl", "(Landroid/view/View;Ljava/lang/String;)V", false);
    putAroundHookMethod("com/tencent/smtt/sdk/WebView", "loadUrl", "(Ljava/lang/String;Ljava/util/Map;)V", "com/growingio/android/sdk/autotrack/inject/WebViewInjector", "x5WebViewLoadUrl", "(Landroid/view/View;Ljava/lang/String;Ljava/util/Map;)V", false);
    putAroundHookMethod("com/tencent/smtt/sdk/WebView", "loadData", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "com/growingio/android/sdk/autotrack/inject/WebViewInjector", "x5WebViewLoadData", "(Landroid/view/View;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
    putAroundHookMethod("com/tencent/smtt/sdk/WebView", "loadDataWithBaseURL", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "com/growingio/android/sdk/autotrack/inject/WebViewInjector", "x5WebViewLoadDataWithBaseURL", "(Landroid/view/View;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
    putAroundHookMethod("com/tencent/smtt/sdk/WebView", "postUrl", "(Ljava/lang/String;[B)V", "com/growingio/android/sdk/autotrack/inject/WebViewInjector", "x5WebViewPostUrl", "(Landroid/view/View;Ljava/lang/String;[B)V", false);
    putAroundHookMethod("com/uc/webview/export/WebView", "loadUrl", "(Ljava/lang/String;)V", "com/growingio/android/sdk/autotrack/inject/WebViewInjector", "ucWebViewLoadUrl", "(Landroid/view/View;Ljava/lang/String;)V", false);
    putAroundHookMethod("com/uc/webview/export/WebView", "loadUrl", "(Ljava/lang/String;Ljava/util/Map;)V", "com/growingio/android/sdk/autotrack/inject/WebViewInjector", "ucWebViewLoadUrl", "(Landroid/view/View;Ljava/lang/String;Ljava/util/Map;)V", false);
    putAroundHookMethod("com/uc/webview/export/WebView", "loadData", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "com/growingio/android/sdk/autotrack/inject/WebViewInjector", "ucWebViewLoadData", "(Landroid/view/View;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
    putAroundHookMethod("com/uc/webview/export/WebView", "loadDataWithBaseURL", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "com/growingio/android/sdk/autotrack/inject/WebViewInjector", "ucWebViewLoadDataWithBaseURL", "(Landroid/view/View;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
    putAroundHookMethod("com/uc/webview/export/WebView", "postUrl", "(Ljava/lang/String;[B)V", "com/growingio/android/sdk/autotrack/inject/WebViewInjector", "ucWebViewPostUrl", "(Landroid/view/View;Ljava/lang/String;[B)V", false);
    putAroundHookMethod("android/app/AlertDialog", "show", "()V", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "alertDialogShow", "(Landroid/app/AlertDialog;)V", true);
    putSuperHookMethod("android/view/View$OnClickListener", "onClick", "(Landroid/view/View;)V", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "viewOnClick", "(Landroid/view/View$OnClickListener;Landroid/view/View;)V", false);
    putSuperHookMethod("android/content/DialogInterface$OnClickListener", "onClick", "(Landroid/content/DialogInterface;I)V", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "dialogOnClick", "(Landroid/content/DialogInterface$OnClickListener;Landroid/content/DialogInterface;I)V", false);
    putSuperHookMethod("android/widget/AdapterView$OnItemClickListener", "onItemClick", "(Landroid/widget/AdapterView;Landroid/view/View;IJ)V", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "adapterViewOnItemClick", "(Landroid/widget/AdapterView$OnItemClickListener;Landroid/widget/AdapterView;Landroid/view/View;IJ)V", false);
    putSuperHookMethod("android/widget/AdapterView$OnItemSelectedListener", "onItemSelected", "(Landroid/widget/AdapterView;Landroid/view/View;IJ)V", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "adapterViewOnItemSelected", "(Landroid/widget/AdapterView$OnItemSelectedListener;Landroid/widget/AdapterView;Landroid/view/View;IJ)V", false);
    putSuperHookMethod("android/widget/ExpandableListView$OnGroupClickListener", "onGroupClick", "(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "expandableListViewOnGroupClick", "(Landroid/widget/ExpandableListView$OnGroupClickListener;Landroid/widget/ExpandableListView;Landroid/view/View;IJ)V", false);
    putSuperHookMethod("android/widget/ExpandableListView$OnChildClickListener", "onChildClick", "(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "expandableListViewOnChildClick", "(Landroid/widget/ExpandableListView$OnChildClickListener;Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)V", false);
    putSuperHookMethod("android/app/ExpandableListActivity", "onChildClick", "(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "expandableListActivityOnChildClick", "(Landroid/app/ExpandableListActivity;Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)V", false);
    putSuperHookMethod("android/app/ListActivity", "onListItemClick", "(Landroid/widget/ListView;Landroid/view/View;IJ)V", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "listActivityOnListItemClick", "(Landroid/app/ListActivity;Landroid/widget/ListView;Landroid/view/View;IJ)V", false);
    putSuperHookMethod("android/widget/CompoundButton$OnCheckedChangeListener", "onCheckedChanged", "(Landroid/widget/CompoundButton;Z)V", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "compoundButtonOnChecked", "(Landroid/widget/CompoundButton$OnCheckedChangeListener;Landroid/widget/CompoundButton;Z)V", false);
    putSuperHookMethod("android/widget/RadioGroup$OnCheckedChangeListener", "onCheckedChanged", "(Landroid/widget/RadioGroup;I)V", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "radioGroupOnChecked", "(Landroid/widget/RadioGroup$OnCheckedChangeListener;Landroid/widget/RadioGroup;I)V", false);
    putSuperHookMethod("android/widget/RatingBar$OnRatingBarChangeListener", "onRatingChanged", "(Landroid/widget/RatingBar;FZ)V", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "ratingBarOnRatingBarChange", "(Landroid/widget/RatingBar$OnRatingBarChangeListener;Landroid/widget/RatingBar;FZ)V", false);
    putSuperHookMethod("android/widget/SeekBar$OnSeekBarChangeListener", "onStopTrackingTouch", "(Landroid/widget/SeekBar;)V", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "seekBarOnSeekBarChange", "(Landroid/widget/SeekBar$OnSeekBarChangeListener;Landroid/widget/SeekBar;)V", false);
    putSuperHookMethod("android/widget/Toolbar$OnMenuItemClickListener", "onMenuItemClick", "(Landroid/view/MenuItem;)Z", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "toolbarOnMenuItemClick", "(Landroid/widget/Toolbar$OnMenuItemClickListener;Landroid/view/MenuItem;)V", false);
    putSuperHookMethod("android/widget/ActionMenuView$OnMenuItemClickListener", "onMenuItemClick", "(Landroid/view/MenuItem;)Z", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "actionMenuViewOnMenuItemClick", "(Landroid/widget/ActionMenuView$OnMenuItemClickListener;Landroid/view/MenuItem;)V", false);
    putSuperHookMethod("android/widget/PopupMenu$OnMenuItemClickListener", "onMenuItemClick", "(Landroid/view/MenuItem;)Z", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "popupMenuOnMenuItemClick", "(Landroid/widget/PopupMenu$OnMenuItemClickListener;Landroid/view/MenuItem;)V", false);
    putSuperHookMethod("android/support/v4/app/Fragment", "onResume", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "v4FragmentOnResume", "(Landroid/support/v4/app/Fragment;)V", true);
    putSuperHookMethod("android/support/v4/app/Fragment", "setUserVisibleHint", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "v4FragmentSetUserVisibleHint", "(Landroid/support/v4/app/Fragment;Z)V", true);
    putSuperHookMethod("android/support/v4/app/Fragment", "onHiddenChanged", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "v4FragmentOnHiddenChanged", "(Landroid/support/v4/app/Fragment;Z)V", true);
    putSuperHookMethod("android/support/v4/app/Fragment", "onDestroyView", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "v4FragmentOnDestroyView", "(Landroid/support/v4/app/Fragment;)V", true);
    putSuperHookMethod("androidx/fragment/app/Fragment", "onResume", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "androidxFragmentOnResume", "(Landroidx/fragment/app/Fragment;)V", true);
    putSuperHookMethod("androidx/fragment/app/Fragment", "setUserVisibleHint", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "androidxFragmentSetUserVisibleHint", "(Landroidx/fragment/app/Fragment;Z)V", true);
    putSuperHookMethod("androidx/fragment/app/Fragment", "onHiddenChanged", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "androidxFragmentOnHiddenChanged", "(Landroidx/fragment/app/Fragment;Z)V", true);
    putSuperHookMethod("androidx/fragment/app/Fragment", "onDestroyView", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "androidxFragmentOnDestroyView", "(Landroidx/fragment/app/Fragment;)V", true);
    putSuperHookMethod("android/app/Activity", "onNewIntent", "(Landroid/content/Intent;)V", "com/growingio/android/sdk/autotrack/inject/ActivityInjector", "onActivityNewIntent", "(Landroid/app/Activity;Landroid/content/Intent;)V", false);
    putSuperHookMethod("android/accounts/AccountAuthenticatorActivity", "onNewIntent", "(Landroid/content/Intent;)V", "com/growingio/android/sdk/autotrack/inject/ActivityInjector", "onActivityNewIntent", "(Landroid/app/Activity;Landroid/content/Intent;)V", false);
    putSuperHookMethod("android/app/ActivityGroup", "onNewIntent", "(Landroid/content/Intent;)V", "com/growingio/android/sdk/autotrack/inject/ActivityInjector", "onActivityNewIntent", "(Landroid/app/Activity;Landroid/content/Intent;)V", false);
    putSuperHookMethod("android/app/AliasActivity", "onNewIntent", "(Landroid/content/Intent;)V", "com/growingio/android/sdk/autotrack/inject/ActivityInjector", "onActivityNewIntent", "(Landroid/app/Activity;Landroid/content/Intent;)V", false);
    putSuperHookMethod("android/app/ExpandableListActivity", "onNewIntent", "(Landroid/content/Intent;)V", "com/growingio/android/sdk/autotrack/inject/ActivityInjector", "onActivityNewIntent", "(Landroid/app/Activity;Landroid/content/Intent;)V", false);
    putSuperHookMethod("android/app/LauncherActivity", "onNewIntent", "(Landroid/content/Intent;)V", "com/growingio/android/sdk/autotrack/inject/ActivityInjector", "onActivityNewIntent", "(Landroid/app/Activity;Landroid/content/Intent;)V", false);
    putSuperHookMethod("android/app/ListActivity", "onNewIntent", "(Landroid/content/Intent;)V", "com/growingio/android/sdk/autotrack/inject/ActivityInjector", "onActivityNewIntent", "(Landroid/app/Activity;Landroid/content/Intent;)V", false);
    putSuperHookMethod("android/app/NativeActivity", "onNewIntent", "(Landroid/content/Intent;)V", "com/growingio/android/sdk/autotrack/inject/ActivityInjector", "onActivityNewIntent", "(Landroid/app/Activity;Landroid/content/Intent;)V", false);
    putSuperHookMethod("android/app/TabActivity", "onNewIntent", "(Landroid/content/Intent;)V", "com/growingio/android/sdk/autotrack/inject/ActivityInjector", "onActivityNewIntent", "(Landroid/app/Activity;Landroid/content/Intent;)V", false);
    putSuperHookMethod("android/preference/PreferenceActivity", "onNewIntent", "(Landroid/content/Intent;)V", "com/growingio/android/sdk/autotrack/inject/ActivityInjector", "onActivityNewIntent", "(Landroid/app/Activity;Landroid/content/Intent;)V", false);
    putSuperHookMethod("android/app/Activity", "onOptionsItemSelected", "(Landroid/view/MenuItem;)Z", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "menuItemOnOptionsItemSelected", "(Landroid/app/Activity;Landroid/view/MenuItem;)V", false);
    putSuperHookMethod("android/accounts/AccountAuthenticatorActivity", "onOptionsItemSelected", "(Landroid/view/MenuItem;)Z", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "menuItemOnOptionsItemSelected", "(Landroid/app/Activity;Landroid/view/MenuItem;)V", false);
    putSuperHookMethod("android/app/ActivityGroup", "onOptionsItemSelected", "(Landroid/view/MenuItem;)Z", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "menuItemOnOptionsItemSelected", "(Landroid/app/Activity;Landroid/view/MenuItem;)V", false);
    putSuperHookMethod("android/app/AliasActivity", "onOptionsItemSelected", "(Landroid/view/MenuItem;)Z", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "menuItemOnOptionsItemSelected", "(Landroid/app/Activity;Landroid/view/MenuItem;)V", false);
    putSuperHookMethod("android/app/ExpandableListActivity", "onOptionsItemSelected", "(Landroid/view/MenuItem;)Z", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "menuItemOnOptionsItemSelected", "(Landroid/app/Activity;Landroid/view/MenuItem;)V", false);
    putSuperHookMethod("android/app/LauncherActivity", "onOptionsItemSelected", "(Landroid/view/MenuItem;)Z", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "menuItemOnOptionsItemSelected", "(Landroid/app/Activity;Landroid/view/MenuItem;)V", false);
    putSuperHookMethod("android/app/ListActivity", "onOptionsItemSelected", "(Landroid/view/MenuItem;)Z", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "menuItemOnOptionsItemSelected", "(Landroid/app/Activity;Landroid/view/MenuItem;)V", false);
    putSuperHookMethod("android/app/NativeActivity", "onOptionsItemSelected", "(Landroid/view/MenuItem;)Z", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "menuItemOnOptionsItemSelected", "(Landroid/app/Activity;Landroid/view/MenuItem;)V", false);
    putSuperHookMethod("android/app/TabActivity", "onOptionsItemSelected", "(Landroid/view/MenuItem;)Z", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "menuItemOnOptionsItemSelected", "(Landroid/app/Activity;Landroid/view/MenuItem;)V", false);
    putSuperHookMethod("android/preference/PreferenceActivity", "onOptionsItemSelected", "(Landroid/view/MenuItem;)Z", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "menuItemOnOptionsItemSelected", "(Landroid/app/Activity;Landroid/view/MenuItem;)V", false);
    putSuperHookMethod("android/app/Fragment", "onResume", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentOnResume", "(Landroid/app/Fragment;)V", true);
    putSuperHookMethod("android/app/DialogFragment", "onResume", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentOnResume", "(Landroid/app/Fragment;)V", true);
    putSuperHookMethod("android/app/ListFragment", "onResume", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentOnResume", "(Landroid/app/Fragment;)V", true);
    putSuperHookMethod("android/preference/PreferenceFragment", "onResume", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentOnResume", "(Landroid/app/Fragment;)V", true);
    putSuperHookMethod("android/webkit/WebViewFragment", "onResume", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentOnResume", "(Landroid/app/Fragment;)V", true);
    putSuperHookMethod("android/app/Fragment", "setUserVisibleHint", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentSetUserVisibleHint", "(Landroid/app/Fragment;Z)V", true);
    putSuperHookMethod("android/app/DialogFragment", "setUserVisibleHint", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentSetUserVisibleHint", "(Landroid/app/Fragment;Z)V", true);
    putSuperHookMethod("android/app/ListFragment", "setUserVisibleHint", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentSetUserVisibleHint", "(Landroid/app/Fragment;Z)V", true);
    putSuperHookMethod("android/preference/PreferenceFragment", "setUserVisibleHint", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentSetUserVisibleHint", "(Landroid/app/Fragment;Z)V", true);
    putSuperHookMethod("android/webkit/WebViewFragment", "setUserVisibleHint", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentSetUserVisibleHint", "(Landroid/app/Fragment;Z)V", true);
    putSuperHookMethod("android/app/Fragment", "onHiddenChanged", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentOnHiddenChanged", "(Landroid/app/Fragment;Z)V", true);
    putSuperHookMethod("android/app/DialogFragment", "onHiddenChanged", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentOnHiddenChanged", "(Landroid/app/Fragment;Z)V", true);
    putSuperHookMethod("android/app/ListFragment", "onHiddenChanged", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentOnHiddenChanged", "(Landroid/app/Fragment;Z)V", true);
    putSuperHookMethod("android/preference/PreferenceFragment", "onHiddenChanged", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentOnHiddenChanged", "(Landroid/app/Fragment;Z)V", true);
    putSuperHookMethod("android/webkit/WebViewFragment", "onHiddenChanged", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentOnHiddenChanged", "(Landroid/app/Fragment;Z)V", true);
    putSuperHookMethod("android/app/Fragment", "onDestroyView", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentOnDestroyView", "(Landroid/app/Fragment;)V", true);
    putSuperHookMethod("android/app/DialogFragment", "onDestroyView", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentOnDestroyView", "(Landroid/app/Fragment;)V", true);
    putSuperHookMethod("android/app/ListFragment", "onDestroyView", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentOnDestroyView", "(Landroid/app/Fragment;)V", true);
    putSuperHookMethod("android/preference/PreferenceFragment", "onDestroyView", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentOnDestroyView", "(Landroid/app/Fragment;)V", true);
    putSuperHookMethod("android/webkit/WebViewFragment", "onDestroyView", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentOnDestroyView", "(Landroid/app/Fragment;)V", true);
  }

  private HookClassesConfig() {
  }

  private static void putAroundHookMethod(String targetClassName, String targetMethodName,
      String targetMethodDesc, String injectClassName, String injectMethodName,
      String injectMethodDesc, boolean isAfter) {
    putHookMethod(AROUND_HOOK_CLASSES, targetClassName, targetMethodName, targetMethodDesc, injectClassName, injectMethodName, injectMethodDesc, isAfter);
  }

  private static void putSuperHookMethod(String targetClassName, String targetMethodName,
      String targetMethodDesc, String injectClassName, String injectMethodName,
      String injectMethodDesc, boolean isAfter) {
    putHookMethod(SUPER_HOOK_CLASSES, targetClassName, targetMethodName, targetMethodDesc, injectClassName, injectMethodName, injectMethodDesc, isAfter);
  }

  private static void putHookMethod(Map<String, TargetClass> classMap, String targetClassName,
      String targetMethodName, String targetMethodDesc, String injectClassName,
      String injectMethodName, String injectMethodDesc, boolean isAfter) {
    TargetClass targetClass = classMap.get(targetClassName);
        if (targetClass == null) {
            targetClass = new TargetClass(targetClassName);
            classMap.put(targetClassName, targetClass);
        }
        TargetMethod targetMethod = targetClass.getTargetMethod(targetMethodName, targetMethodDesc);
        if (targetMethod == null) {
            targetMethod = new TargetMethod(targetMethodName, targetMethodDesc);
            targetClass.addTargetMethod(targetMethod);
        }
        targetMethod.addInjectMethod(new InjectMethod(injectClassName, injectMethodName, injectMethodDesc, isAfter));
  }

  public static Map<String, TargetClass> getAroundHookClasses() {
    return Collections.unmodifiableMap(AROUND_HOOK_CLASSES);
  }

  public static Map<String, TargetClass> getSuperHookClasses() {
    return Collections.unmodifiableMap(SUPER_HOOK_CLASSES);
  }
}
