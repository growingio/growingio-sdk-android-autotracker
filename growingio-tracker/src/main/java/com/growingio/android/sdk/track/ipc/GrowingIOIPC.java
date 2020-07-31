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

package com.growingio.android.sdk.track.ipc;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.growingio.android.sdk.track.GConfig;
import com.growingio.android.sdk.track.utils.LogUtil;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * GrowingIO进程间共享的变量
 */
public class GrowingIOIPC {
    private static final String TAG = "GIO.IPC";

    @VisibleForTesting
    VariableSharer mVariableSharer;

    // 按照既定顺序， 如果需要更改字节码的排序， 需要将ipc文件升级版本， 并且需要编写迁移代码, 类似数据库
    @VisibleForTesting
    int mSessionIndex = -1;       // 0     String(10)
    int mUserIdIndex = -1;        // 1     String(1000)
    int mLastPauseTimeIndex = -1; // 2     long
    int mLastResumeTimeIndex = -1; // 3     long

    public void init(Context context, GConfig config) {
        File dirFile = new File(context.getFilesDir(), ".gio.dir");
        if (!dirFile.exists())
            dirFile.mkdirs();
        File ipcFile = new File(dirFile, "gio.core.ipc.1");
        mVariableSharer = new VariableSharer(ipcFile, true, Process.myPid());
        long startTime = System.currentTimeMillis();
        initVariableVersion1(context);
        LogUtil.e(TAG, "variableSharer init time: %d", (System.currentTimeMillis() - startTime));
    }

    private void initVariableVersion1(Context context) {
        /* 0 */
        mSessionIndex = mVariableSharer.addVariableEntity(VariableEntity.createStringVariable("sessionId", 10));
        /* 1 */
        mUserIdIndex = mVariableSharer.addVariableEntity(VariableEntity.createStringVariable("userId", 1000));
        /* 2 */
        mLastPauseTimeIndex = mVariableSharer.addVariableEntity(VariableEntity.createLongVariable("lastPauseTime"));
        /* 3 */
        mLastResumeTimeIndex = mVariableSharer.addVariableEntity(VariableEntity.createLongVariable("lastResumeTime"));


        mVariableSharer.completeMetaData(getRunningProcess(context));
        if (mVariableSharer.isFirstInit()) {
            // 应用冷启动
            setLastPauseTime(-1);
        }
    }

    private Set<Integer> getRunningProcess(Context context) {
        Set<Integer> myRunningProcess = new HashSet<>();
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
            int myUid = Process.myUid();
            for (ActivityManager.RunningAppProcessInfo info : runningAppProcesses) {
                if (myUid == info.uid) {
                    myRunningProcess.add(info.pid);
                }
            }
        } catch (Throwable e) {
            // for System Service Died exception
            LogUtil.e(TAG, e, e.getMessage());
        }
        return myRunningProcess;
    }

    public String getSessionId() {
        return mVariableSharer.getStringByIndex(mSessionIndex);
    }

    public void setSessionId(String sessionId) {
        mVariableSharer.putStringByIndex(mSessionIndex, sessionId);
    }

    public String getUserId() {
        return mVariableSharer.getStringByIndex(mUserIdIndex);
    }

    public void setUserId(@Nullable String userId) {
        mVariableSharer.putStringByIndex(mUserIdIndex, userId);
    }

    public long getLastResumeTime() {
        return mVariableSharer.getLongByIndex(mLastResumeTimeIndex);
    }

    public void setLastResumeTime(long lastResumeTime) {
        mVariableSharer.putLongByIndex(mLastResumeTimeIndex, lastResumeTime);
    }

    public long getLastPauseTime() {
        return mVariableSharer.getLongByIndex(mLastPauseTimeIndex);
    }

    public void setLastPauseTime(long lastPauseTime) {
        mVariableSharer.putLongByIndex(mLastPauseTimeIndex, lastPauseTime);
    }
}
