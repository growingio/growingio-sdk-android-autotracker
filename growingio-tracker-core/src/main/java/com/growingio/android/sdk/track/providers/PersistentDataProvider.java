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
package com.growingio.android.sdk.track.providers;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;
import android.text.TextUtils;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.ipc.IDataSharer;
import com.growingio.android.sdk.track.ipc.MultiProcessDataSharer;
import com.growingio.android.sdk.track.ipc.ProcessLock;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.SystemUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PersistentDataProvider implements TrackerLifecycleProvider {
    private static final String TAG = "PersistentDataProvider";
    private static final String SHARER_NAME = "PersistentSharerDataProvider";
    private static final int SHARER_MAX_SIZE = 50;

    // Key长度校验 0 <= len < 50, 判断文件读取是否合法
    private static final String KEY_TYPE_GLOBAL = "TYPE_GLOBAL";
    private static final String KEY_LOGIN_USER_KEY = "LOGIN_USER_KEY";
    private static final String KEY_LOGIN_USER_ID = "LOGIN_USER_ID";
    private static final String KEY_DEVICE_ID = "DEVICE_ID";
    private static final String KEY_SESSION_ID = "SESSION_ID";
    private static final String KEY_ALIVE_PID = "ALIVE_PID";
    private static final String KEY_LATEST_NON_NULL_USER_ID = "LATEST_NON_NULL_USER_ID";
    private static final String KEY_LATEST_PAUSE_TIME = "LATEST_PAUSE_TIME";
    private static final String KEY_ACTIVITY_COUNT = "ACTIVITY_COUNT";
    private static final String KEY_SEND_VISIT_AFTER_REFRESH_SESSION_ID = "SEND_VISIT_AFTER_REFRESH_SESSION_ID";
    // [true|false]::[pid]::[session]
    private static final String KEY_NEW_DEVICE_TOKEN = "NEW_DEVICE_TOKEN";

    private final IDataSharer dataSharer;
    private final ProcessLock processLock;
    private boolean isFirstProcess = true;

    PersistentDataProvider(Context context) {
        dataSharer = new MultiProcessDataSharer(context, SHARER_NAME, SHARER_MAX_SIZE);
        processLock = new ProcessLock(context, PersistentDataProvider.class.getName());
    }

    @Override
    public void setup(TrackerContext context) {
        // need invoke after setup
        repairPid(context, context.getConfigurationProvider());
    }

    @Override
    public void shutdown() {
        dataSharer.release();
        processLock.release();
    }

    public boolean isFirstProcess() {
        return isFirstProcess;
    }

    public long getGlobalEventSequenceIdAndIncrement() {
        return dataSharer.getAndIncrementLong(KEY_TYPE_GLOBAL, 1L);
    }

    public String getSessionId() {
        return dataSharer.getString(KEY_SESSION_ID, "");
    }

    public void setSessionId(String sessionId) {
        dataSharer.putString(KEY_SESSION_ID, sessionId);
    }

    public String getDeviceId() {
        return dataSharer.getString(KEY_DEVICE_ID, "");
    }

    public void setDeviceId(String deviceId) {
        if (TextUtils.isEmpty(deviceId)) {
            return;
        }
        setNewDeviceToken(true);
        dataSharer.putString(KEY_DEVICE_ID, deviceId);
    }

    public void setNewDeviceToken(boolean isNewDevice) {
        if (isNewDevice) {
            String session = getSessionId();
            String token = "true::" + Process.myPid() + "::" + session;
            dataSharer.putString(KEY_NEW_DEVICE_TOKEN, token);
        } else {
            dataSharer.putString(KEY_NEW_DEVICE_TOKEN, "false::1::NULL");
        }
    }

    /**
     * judge whether the device is new
     * 1. if the token enable is false, return false
     * 2. if the token pid is 0 or equal current pid, return true
     * 3. if the token session is fresh, return true
     */
    public boolean isNewDevice() {
        try {
            String token = dataSharer.getString(KEY_NEW_DEVICE_TOKEN, "true::0::");
            String[] tokenCondition = token.split("::");
            if (tokenCondition.length != 3) {
                setNewDeviceToken(false);
                return false;
            }
            if (tokenCondition[0].equalsIgnoreCase("false")) {
                return false;
            }
            int pid = android.os.Process.myPid();
            if (tokenCondition[1].equals("0")) {
                return true;
            }
            if (!tokenCondition[1].equals(String.valueOf(pid))) {
                return false;
            }

            String currentSession = getSessionId();
            String tokenSession = tokenCondition[2];
            boolean isNewDevice = currentSession.equals(tokenSession);
            if (!isNewDevice) {
                setNewDeviceToken(false);
            }
            return isNewDevice;
        } catch (Exception e) {
            return false;
        }
    }

    public String getLoginUserKey() {
        return dataSharer.getString(KEY_LOGIN_USER_KEY, "");
    }

    public String getLoginUserId() {
        return dataSharer.getString(KEY_LOGIN_USER_ID, "");
    }

    public void setLoginUserIdAndUserKey(String userId, String userKey) {
        HashMap<String, String> map = new HashMap<>();
        map.put(KEY_LOGIN_USER_ID, userId);
        map.put(KEY_LOGIN_USER_KEY, userKey);
        dataSharer.putMultiString(map);
    }

    public String getLatestNonNullUserId() {
        return dataSharer.getString(KEY_LATEST_NON_NULL_USER_ID, "");
    }

    public void setLatestNonNullUserId(String latestNonNullUserId) {
        dataSharer.putString(KEY_LATEST_NON_NULL_USER_ID, latestNonNullUserId);
    }

    public long getLatestPauseTime() {
        return dataSharer.getLong(KEY_LATEST_PAUSE_TIME, 0L);
    }

    public void setLatestPauseTime(long latestPauseTime) {
        dataSharer.putLong(KEY_LATEST_PAUSE_TIME, latestPauseTime);
    }

    public int getActivityCount() {
        return dataSharer.getInt(KEY_ACTIVITY_COUNT, 0);
    }

    public void setActivityCount(int activityCount) {
        dataSharer.putInt(KEY_ACTIVITY_COUNT, activityCount);
    }

    public void addActivityCount() {
        dataSharer.getAndIncrementInt(KEY_ACTIVITY_COUNT, 0);
    }

    public void delActivityCount() {
        dataSharer.getAndDecrementInt(KEY_ACTIVITY_COUNT, 0);
    }

    public boolean isSendVisitAfterRefreshSessionId() {
        return dataSharer.getBoolean(KEY_SEND_VISIT_AFTER_REFRESH_SESSION_ID, true);
    }

    public void setSendVisitAfterRefreshSessionId(boolean sendVisitAfterRefreshSessionId) {
        dataSharer.putBoolean(KEY_SEND_VISIT_AFTER_REFRESH_SESSION_ID, sendVisitAfterRefreshSessionId);
    }

    public void putString(String key, String value) {
        dataSharer.putString(key, value);
    }


    public String getString(String key, String defValue) {
        return dataSharer.getString(key, defValue);
    }

    private void repairPid(Context context, ConfigurationProvider configurationProvider) {
        processLock.lockedRun(() -> {
            isFirstProcess = false;
            if (configurationProvider.core().isRequireAppProcessesEnabled() && configurationProvider.core().isDataCollectionEnabled()) {
                List<Integer> alivePid = new ArrayList<>();
                Set<Integer> runningProcess = getRunningProcess(context);
                for (int pid : getAlivePid()) {
                    if (runningProcess.contains(pid)) {
                        alivePid.add(pid);
                    }
                }
                if (alivePid.isEmpty()) {
                    isFirstProcess = true;
                }
                alivePid.add(Process.myPid());
                putAlivePid(alivePid);
            } else {
                isFirstProcess = SystemUtil.isMainProcess(context);
                // 将主进程id加入alive pid 中
                if (isFirstProcess) putAlivePid(new ArrayList<>(Process.myPid()));
            }

            if (isFirstProcess) {
                setActivityCount(0);
                setLatestPauseTime(0L);
                setLatestNonNullUserId(getLoginUserId());

                // refresh session
                setSessionId(UUID.randomUUID().toString());
                setSendVisitAfterRefreshSessionId(false);
            }
        });
    }

    private List<Integer> getAlivePid() {
        return dataSharer.getIntArray(KEY_ALIVE_PID, new ArrayList<>());
    }

    private void putAlivePid(List<Integer> value) {
        dataSharer.putIntArray(KEY_ALIVE_PID, value);
    }

    private Set<Integer> getRunningProcess(Context context) {
        Set<Integer> myRunningProcess = new HashSet<>();
        try {
            @SuppressLint("WrongConstant") ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
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