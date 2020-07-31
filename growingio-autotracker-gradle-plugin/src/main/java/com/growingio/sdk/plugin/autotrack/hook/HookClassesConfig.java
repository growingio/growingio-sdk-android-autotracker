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
 * 该Class是由 inject-compiler 自动生成的，请不要随意更改！ */
public class HookClassesConfig {
  private static final Map<String, TargetClass> AROUND_HOOK_CLASSES = new HashMap<>();

  private static final Map<String, TargetClass> SUPER_HOOK_CLASSES = new HashMap<>();

  static {
    putAroundHookMethod("android/webkit/WebView", "loadUrl", "(Ljava/lang/String;)V", "com/growingio/android/sdk/autotrack/hybrid/WebViewInjector", "webkitWebViewLoadUrl", "(Landroid/webkit/WebView;Ljava/lang/String;)V", false);
    putAroundHookMethod("android/webkit/WebView", "loadUrl", "(Ljava/lang/String;Ljava/util/Map;)V", "com/growingio/android/sdk/autotrack/hybrid/WebViewInjector", "webkitWebViewLoadUrl", "(Landroid/webkit/WebView;Ljava/lang/String;Ljava/util/Map;)V", false);
    putAroundHookMethod("android/webkit/WebView", "loadData", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "com/growingio/android/sdk/autotrack/hybrid/WebViewInjector", "webkitWebViewLoadData", "(Landroid/webkit/WebView;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
    putAroundHookMethod("com/tencent/smtt/sdk/WebView", "loadUrl", "(Ljava/lang/String;)V", "com/growingio/android/sdk/autotrack/hybrid/WebViewInjector", "x5WebViewLoadUrl", "(Lcom/tencent/smtt/sdk/WebView;Ljava/lang/String;)V", false);
    putAroundHookMethod("com/tencent/smtt/sdk/WebView", "loadUrl", "(Ljava/lang/String;Ljava/util/Map;)V", "com/growingio/android/sdk/autotrack/hybrid/WebViewInjector", "x5WebViewLoadUrl", "(Lcom/tencent/smtt/sdk/WebView;Ljava/lang/String;Ljava/util/Map;)V", false);
    putAroundHookMethod("com/tencent/smtt/sdk/WebView", "loadData", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "com/growingio/android/sdk/autotrack/hybrid/WebViewInjector", "x5WebViewLoadData", "(Lcom/tencent/smtt/sdk/WebView;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
    putAroundHookMethod("com/uc/webview/export/WebView", "loadUrl", "(Ljava/lang/String;)V", "com/growingio/android/sdk/autotrack/hybrid/WebViewInjector", "ucWebViewLoadUrl", "(Lcom/uc/webview/export/WebView;Ljava/lang/String;)V", false);
    putAroundHookMethod("com/uc/webview/export/WebView", "loadUrl", "(Ljava/lang/String;Ljava/util/Map;)V", "com/growingio/android/sdk/autotrack/hybrid/WebViewInjector", "ucWebViewLoadUrl", "(Lcom/uc/webview/export/WebView;Ljava/lang/String;Ljava/util/Map;)V", false);
    putAroundHookMethod("com/uc/webview/export/WebView", "loadData", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "com/growingio/android/sdk/autotrack/hybrid/WebViewInjector", "ucWebViewLoadData", "(Lcom/uc/webview/export/WebView;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
    putAroundHookMethod("android/webkit/WebView", "loadDataWithBaseURL", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "com/growingio/android/sdk/autotrack/hybrid/WebViewInjector", "webkitWebViewLoadDataWithBaseURL", "(Landroid/webkit/WebView;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
    putAroundHookMethod("com/tencent/smtt/sdk/WebView", "loadDataWithBaseURL", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "com/growingio/android/sdk/autotrack/hybrid/WebViewInjector", "x5WebViewLoadDataWithBaseURL", "(Lcom/tencent/smtt/sdk/WebView;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
    putAroundHookMethod("com/uc/webview/export/WebView", "loadDataWithBaseURL", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "com/growingio/android/sdk/autotrack/hybrid/WebViewInjector", "ucWebViewLoadDataWithBaseURL", "(Lcom/uc/webview/export/WebView;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
    putSuperHookMethod("android/view/View$OnClickListener", "onClick", "(Landroid/view/View;)V", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "beforeViewOnClick", "(Landroid/view/View$OnClickListener;Landroid/view/View;)V", false);
    putSuperHookMethod("android/app/Fragment", "onResume", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentOnResume", "(Landroid/app/Fragment;)V", true);
    putSuperHookMethod("android/app/Fragment", "setUserVisibleHint", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentSetUserVisibleHint", "(Landroid/app/Fragment;Z)V", true);
    putSuperHookMethod("android/app/Fragment", "onHiddenChanged", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentOnHiddenChanged", "(Landroid/app/Fragment;Z)V", true);
    putSuperHookMethod("android/app/Fragment", "onDestroyView", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "systemFragmentOnDestroyView", "(Landroid/app/Fragment;)V", true);
    putSuperHookMethod("android/webkit/WebViewFragment", "onResume", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "webViewFragmentOnResume", "(Landroid/webkit/WebViewFragment;)V", true);
    putSuperHookMethod("android/webkit/WebViewFragment", "setUserVisibleHint", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "webViewFragmentSetUserVisibleHint", "(Landroid/webkit/WebViewFragment;Z)V", true);
    putSuperHookMethod("android/webkit/WebViewFragment", "onHiddenChanged", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "webViewFragmentOnHiddenChanged", "(Landroid/webkit/WebViewFragment;Z)V", true);
    putSuperHookMethod("android/webkit/WebViewFragment", "onDestroyView", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "webViewFragmentOnDestroyView", "(Landroid/webkit/WebViewFragment;)V", true);
    putSuperHookMethod("android/preference/PreferenceFragment", "onResume", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "preferenceFragmentOnResume", "(Landroid/preference/PreferenceFragment;)V", true);
    putSuperHookMethod("android/preference/PreferenceFragment", "setUserVisibleHint", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "preferenceFragmentSetUserVisibleHint", "(Landroid/preference/PreferenceFragment;Z)V", true);
    putSuperHookMethod("android/preference/PreferenceFragment", "onHiddenChanged", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "preferenceFragmentOnHiddenChanged", "(Landroid/preference/PreferenceFragment;Z)V", true);
    putSuperHookMethod("android/preference/PreferenceFragment", "onDestroyView", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "preferenceFragmentOnDestroyView", "(Landroid/preference/PreferenceFragment;)V", true);
    putSuperHookMethod("android/app/ListFragment", "onResume", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "listFragmentOnResume", "(Landroid/app/ListFragment;)V", true);
    putSuperHookMethod("android/app/ListFragment", "setUserVisibleHint", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "listFragmentSetUserVisibleHint", "(Landroid/app/ListFragment;Z)V", true);
    putSuperHookMethod("android/app/ListFragment", "onHiddenChanged", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "listFragmentOnHiddenChanged", "(Landroid/app/ListFragment;Z)V", true);
    putSuperHookMethod("android/app/ListFragment", "onDestroyView", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "listFragmentOnDestroyView", "(Landroid/app/ListFragment;)V", true);
    putSuperHookMethod("android/support/v4/app/Fragment", "onResume", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "v4FragmentOnResume", "(Landroid/support/v4/app/Fragment;)V", true);
    putSuperHookMethod("android/support/v4/app/Fragment", "setUserVisibleHint", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "v4FragmentSetUserVisibleHint", "(Landroid/support/v4/app/Fragment;Z)V", true);
    putSuperHookMethod("android/support/v4/app/Fragment", "onHiddenChanged", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "v4FragmentOnHiddenChanged", "(Landroid/support/v4/app/Fragment;Z)V", true);
    putSuperHookMethod("android/support/v4/app/Fragment", "onDestroyView", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "v4FragmentOnDestroyView", "(Landroid/support/v4/app/Fragment;)V", true);
    putSuperHookMethod("androidx/fragment/app/Fragment", "onResume", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "androidxFragmentOnResume", "(Landroidx/fragment/app/Fragment;)V", true);
    putSuperHookMethod("androidx/fragment/app/Fragment", "setUserVisibleHint", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "androidxFragmentSetUserVisibleHint", "(Landroidx/fragment/app/Fragment;Z)V", true);
    putSuperHookMethod("androidx/fragment/app/Fragment", "onHiddenChanged", "(Z)V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "androidxFragmentOnHiddenChanged", "(Landroidx/fragment/app/Fragment;Z)V", true);
    putSuperHookMethod("androidx/fragment/app/Fragment", "onDestroyView", "()V", "com/growingio/android/sdk/autotrack/page/FragmentInjector", "androidxFragmentOnDestroyView", "(Landroidx/fragment/app/Fragment;)V", true);
    putSuperHookMethod("android/view/View$OnClickListener", "onClick", "(Landroid/view/View;)V", "com/growingio/android/sdk/autotrack/click/ViewClickInjector", "viewOnClick", "(Landroid/view/View$OnClickListener;Landroid/view/View;)V", true);
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
