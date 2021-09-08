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

package com.growingio.android.sdk.track.data;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.interfaces.TrackThread;
import com.growingio.android.sdk.track.ipc.IDataSharer;
import com.growingio.android.sdk.track.ipc.MultiProcessDataSharer;
import com.growingio.android.sdk.track.ipc.ProcessLock;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.SessionProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PersistentDataProvider {
    private static final String TAG = "PersistentDataProvider";
    private static final String SHARER_NAME = "PersistentSharerDataProvider";
    private static final int SHARER_MAX_SIZE = 50;

    private static final String KEY_TYPE_GLOBAL = "TYPE_GLOBAL";
    private static final String KEY_LOGIN_USER_ID = "LOGIN_USER_ID";
    private static final String KEY_DEVICE_ID = "DEVICE_ID";
    private static final String KEY_SESSION_ID = "SESSION_ID";
    private static final String KEY_ALIVE_PID = "ALIVE_PID";
    private static final String KEY_LATEST_NON_NULL_USER_ID = "LATEST_NON_NULL_USER_ID";
    private static final String KEY_LATEST_PAUSE_TIME = "LATEST_PAUSE_TIME";
    private static final String KEY_ACTIVITY_COUNT = "ACTIVITY_COUNT";
    private static final String KEY_SEND_VISIT_AFTER_REFRESH_SESSION_ID = "SEND_VISIT_AFTER_REFRESH_SESSION_ID";

    private final IDataSharer mDataSharer;
    private final ProcessLock mProcessLock;
    private final Context mContext;

    private static class SingleInstance {
        private static final PersistentDataProvider INSTANCE = new PersistentDataProvider();
    }

    private PersistentDataProvider() {
        mContext = TrackerContext.get().getApplicationContext();
        mDataSharer = new MultiProcessDataSharer(mContext, SHARER_NAME, SHARER_MAX_SIZE);
        mProcessLock = new ProcessLock(mContext, PersistentDataProvider.class.getName());
    }

    public static PersistentDataProvider get() {
        return SingleInstance.INSTANCE;
    }

    @TrackThread
    public void start() {
        repairPid();
    }

    public EventSequenceId getAndIncrement(String eventType) {
        long globalId = mDataSharer.getAndIncrementLong(KEY_TYPE_GLOBAL, 1L);
        long eventTypeId = mDataSharer.getAndIncrementLong(eventType, 1L);
        return new EventSequenceId(globalId, eventTypeId);
    }

    public String getSessionId() {
        return mDataSharer.getString(KEY_SESSION_ID, "");
    }

    public void setSessionId(String sessionId) {
        mDataSharer.putString(KEY_SESSION_ID, sessionId);
    }

    public String getDeviceId() {
        return mDataSharer.getString(KEY_DEVICE_ID, "");
    }

    public void setDeviceId(@NonNull String deviceId) {
        if (TextUtils.isEmpty(deviceId)) {
            return;
        }
        mDataSharer.putString(KEY_DEVICE_ID, deviceId);
    }

    public String getLoginUserId() {
        return mDataSharer.getString(KEY_LOGIN_USER_ID, "");
    }

    public void setLoginUserId(@Nullable String userId) {
        mDataSharer.putString(KEY_LOGIN_USER_ID, userId);
    }

    public String getLatestNonNullUserId() {
        return mDataSharer.getString(KEY_LATEST_NON_NULL_USER_ID, "");
    }

    public void setLatestNonNullUserId(@Nullable String latestNonNullUserId) {
        mDataSharer.putString(KEY_LATEST_NON_NULL_USER_ID, latestNonNullUserId);
    }

    public long getLatestPauseTime() {
        return mDataSharer.getLong(KEY_LATEST_PAUSE_TIME, 0L);
    }

    public void setLatestPauseTime(long latestPauseTime) {
        mDataSharer.putLong(KEY_LATEST_PAUSE_TIME, latestPauseTime);
    }

    public int getActivityCount() {
        return mDataSharer.getInt(KEY_ACTIVITY_COUNT, 0);
    }

    public void setActivityCount(int activityCount) {
        mDataSharer.putInt(KEY_ACTIVITY_COUNT, activityCount);
    }

    public void addActivityCount() {
        mDataSharer.getAndIncrementInt(KEY_ACTIVITY_COUNT, 0);
    }

    public void delActivityCount() {
        mDataSharer.getAndDecrementInt(KEY_ACTIVITY_COUNT, 0);
    }

    public boolean isSendVisitAfterRefreshSessionId() {
        return mDataSharer.getBoolean(KEY_SEND_VISIT_AFTER_REFRESH_SESSION_ID, false);
    }

    public void setSendVisitAfterRefreshSessionId(boolean sendVisitAfterRefreshSessionId) {
        mDataSharer.putBoolean(KEY_SEND_VISIT_AFTER_REFRESH_SESSION_ID, sendVisitAfterRefreshSessionId);
    }

    public void putString(String key, @Nullable String value) {
        mDataSharer.putString(key, value);
    }

    @Nullable
    public String getString(String key, String defValue) {
        return mDataSharer.getString(key, defValue);
    }

    private void repairPid() {
        mProcessLock.lockedRun(new Runnable() {
            @Override
            public void run() {
                List<Integer> alivePid = new ArrayList<>();
                Set<Integer> runningProcess = getRunningProcess(mContext);
                for (int pid : getAlivePid()) {
                    if (runningProcess.contains(pid)) {
                        alivePid.add(pid);
                    }
                }

                if (alivePid.isEmpty()) {
                    SessionProvider.get().refreshSessionId();
                    SessionProvider.get().generateVisit();
                    setLatestPauseTime(System.currentTimeMillis());
                    setActivityCount(0);
                    setLatestNonNullUserId(getLoginUserId());
                }

                alivePid.add(Process.myPid());
                putAlivePid(alivePid);
            }
        });
    }

    private List<Integer> getAlivePid() {
        return mDataSharer.getIntArray(KEY_ALIVE_PID, new ArrayList<>());
    }

    private void putAlivePid(List<Integer> value) {
        mDataSharer.putIntArray(KEY_ALIVE_PID, value);
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
            Logger.e(TAG, e.getMessage(), e);
        }
        return myRunningProcess;
    }
}