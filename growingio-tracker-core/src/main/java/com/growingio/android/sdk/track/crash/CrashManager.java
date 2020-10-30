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

package com.growingio.android.sdk.track.crash;

import android.content.Context;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

import com.growingio.android.sdk.monitor.Monitor;
import com.growingio.android.sdk.monitor.MonitorClient;
import com.growingio.android.sdk.monitor.analysis.Analysed;
import com.growingio.android.sdk.monitor.analysis.Analyser;
import com.growingio.android.sdk.monitor.analysis.GIOAnalyser;
import com.growingio.android.sdk.monitor.event.EventBuilder;
import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.log.Logger;

/**
 * 异常捕获并上报
 * RestrictTo 该类仅能在group ID and artifact ID相同情况下调用，会有lint提示
 * 参见：https://developer.android.google.cn/reference/androidx/annotation/RestrictTo.Scope.html#LIBRARY_GROUP_PREFIX
 */
@RestrictTo({RestrictTo.Scope.LIBRARY})
public class CrashManager {
    public static final String ALIAS = "gandroid";

    /**
     * https://sentry.growingio.com/settings/growingio/projects/gioandroid/keys/ 查看对应DSN
     */
    private static final String DSN = "https://3db1f04a7238465ab285f46b462b5ffe@sentry.growingio.com/7";

    private static final String PROJECT_ID = "ai";

    private static volatile boolean sEnabled = false;

    private CrashManager() {

    }

    /**
     * 注册对应的client进行上报异常
     *
     * @param context 建议传入application的context
     */
    public static void register(Context context) {
        if (!sEnabled) {
            MonitorClient client = Monitor.register(ALIAS, DSN, context, new MonitorClient.FilterThrowableRule() {
                @Override
                public boolean filterThrowable(Throwable thrown) {
                    Analyser analyser = GIOAnalyser.getInstance().getAnalyser();
                    Analysed analysed = analyser.getLastAnalysed();
                    if (analysed == null || analysed.getThrowable() != thrown) {
                        analysed = analyser.analyze(thrown);
                    }

                    return isSdkException(analysed);
                }
            });
            client.setRelease(SDKConfig.SDK_VERSION);
            Logger.addLogger(new CrashLogger());

            // TODO: 2020/8/5 添加设备信息
            sEnabled = true;
        }
    }

    /**
     * 建议使用{@link CrashManager#close()}函数，
     */
    public static void unRegister() {
        if (sEnabled) {
            Monitor.unregister(ALIAS);
            sEnabled = false;
        }
    }

    /**
     * 仅关闭上报功能，并不移除client
     */
    public static void close() {
        if (sEnabled) {
            Monitor.close(ALIAS);
        }
    }

    public static boolean isEnabled() {
        return sEnabled;
    }

    public static void sendMessage(String msg) {
        if (sEnabled) {
            Monitor.getStoredClient(ALIAS).sendMessage(msg);
        }
    }

    public static void sendException(Throwable throwable) {
        if (sEnabled) {
            Monitor.getStoredClient(ALIAS).sendException(throwable);
        }
    }

    public static void sendEvent(EventBuilder eventBuilder) {
        if (sEnabled) {
            Monitor.getStoredClient(ALIAS).sendEvent(eventBuilder);
        }
    }

    @VisibleForTesting
    private static boolean isSdkException(Analysed analysed) {
        if (analysed == null || !analysed.isFindTarget()) {
            return false;
        }

        StackTraceElement element = analysed.getFirstTargetElement();
        if (element != null) {
            String className = element.getClassName();
            return className.startsWith("com.growingio");
        }

        return false;
    }
}
