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
    putAroundHookMethod("com/growingio/sdk/sample/TestOnClickListener", "loadUrl", "(Ljava/lang/String;)V", "com/growingio/sdk/test/TestInjector", "webkitWebViewLoadUrl", "(Lcom/growingio/sdk/sample/TestOnClickListener;Ljava/lang/String;)V", false);
    putAroundHookMethod("com/growingio/sdk/sample/TestOnClickListener", "show", "()V", "com/growingio/sdk/test/TestInjector", "alertDialogShow", "(Ljava/lang/Object;)V", true);
    putAroundHookMethod("com/growingio/sdk/sample/TestOnClickListener", "types", "(FDCSZB)Ljava/lang/String;", "com/growingio/sdk/test/TestInjector", "types", "(Lcom/growingio/sdk/sample/TestOnClickListener;FDCSZB)V", true);
    putAroundHookMethod("com/growingio/sdk/sample/TestOnClickListener", "arrays", "(Ljava/util/List;)Ljava/lang/Object;", "com/growingio/sdk/test/TestInjector", "arrays", "(Lcom/growingio/sdk/sample/TestOnClickListener;Ljava/util/List;)V", true);
    putSuperHookMethod("com/growingio/sdk/sample/TestOnClickListener", "onClick", "(I)V", "com/growingio/sdk/test/TestInjector", "viewOnClick", "(Lcom/growingio/sdk/sample/TestOnClickListener;I)V", false);
    putSuperHookMethod("com/growingio/sdk/sample/TestOnClickListener", "onOptionsItemSelected", "(Ljava/lang/Object;)Z", "com/growingio/sdk/test/TestInjector", "menuItemOnOptionsItemSelected", "(Lcom/growingio/sdk/sample/TestOnClickListener;Ljava/lang/Object;)V", false);
    putSuperHookMethod("com/growingio/sdk/sample/TestOnClickListener", "onOptionsItemSelected2", "(Ljava/lang/Object;)Z", "com/growingio/sdk/test/TestInjector", "menuItemOnOptionsItemSelected", "(Lcom/growingio/sdk/sample/TestOnClickListener;Ljava/lang/Object;)V", false);
    putSuperHookMethod("com/growingio/sdk/sample/TestOnClickListener", "onResume", "()V", "com/growingio/sdk/test/TestInjector", "systemFragmentOnResume", "(Lcom/growingio/sdk/sample/TestOnClickListener;)V", true);
    putSuperHookMethod("com/growingio/sdk/sample/TestOnClickListener", "onResume2", "()V", "com/growingio/sdk/test/TestInjector", "systemFragmentOnResume", "(Lcom/growingio/sdk/sample/TestOnClickListener;)V", true);
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
