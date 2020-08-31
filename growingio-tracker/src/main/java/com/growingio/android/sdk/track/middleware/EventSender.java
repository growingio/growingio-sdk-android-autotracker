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

package com.growingio.android.sdk.track.middleware;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.utils.NetworkUtil;
import com.growingio.android.sdk.track.utils.ThreadUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 事件发送者
 * - 多个进程公用同一个EventSender
 * - EventSender为单线程模型
 */
class EventSender {
    private static final String TAG = "EventSender";

    private static final int EVENTS_BULK_SIZE = 300;

    private final Context mContext;
    private int mCacheEventNum = 0;
    private final DBSQLite mDbSQLite;

    private GIOSenderService mSenderService;
    private final SharedPreferences mSharedPreferences;
    private final long mCellularDataLimit;

    EventSender(Context context, IEventSender sender) {
        mContext = context;
        mDbSQLite = new DBSQLite(context, sender);
        mSharedPreferences = context.getSharedPreferences("growing_sender", Context.MODE_PRIVATE);
        mCellularDataLimit = ConfigurationProvider.get().getTrackConfiguration().getCellularDataLimit() * 1024 * 1024;
    }

    public void setSenderService(GIOSenderService senderService) {
        this.mSenderService = senderService;
    }

    /**
     * EventSaver通知EventSender已经记录了一个消息
     *
     * @param instant true -- 表示是一个即时消息
     */
    void onEventWrite(boolean instant) {
        mDbSQLite.updateStatics(1, 0, 0, 0, 0);
        if (instant) {
            sendEvents(true);
        } else {
            mCacheEventNum++;
            if (mCacheEventNum >= EVENTS_BULK_SIZE) {
                Logger.d(TAG, "cacheEventNum >= 300, toggle one send action");
                sendEvents(false);
                mCacheEventNum = 0;
            }
        }
    }

    void delAllMsg() {
        Logger.d(TAG, "action: 清库");
        mDbSQLite.mDbHelper.delAllMsg();
    }

    void consumeBytes(int bytes) {
        if (NetworkUtil.isMobileData(mContext)) {
            mDbSQLite.updateStatics(0, 0, bytes, 0, 0);
            todayBytes(bytes);
        }
    }

    /**
     * @param delta 变化量
     * @return 今日移动网络数据发送量
     */
    long todayBytes(int delta) {
        String dateKey = "today";
        String valueKey = "today_bytes";
        String todayStr = mSharedPreferences.getString(dateKey, "");

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
        String realDayTime = dayFormat.format(new Date());

        long oldValue;
        if (!realDayTime.equals(todayStr)) {
            // 新的一天， 重新计算
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(dateKey, realDayTime);
            editor.putLong(valueKey, 0);
            editor.apply();
            oldValue = 0;
        } else {
            // 与记录数据是同一天
            oldValue = mSharedPreferences.getLong(valueKey, 0);
        }
        if (delta != 0) {
            long value = oldValue + delta;
            mSharedPreferences.edit().putLong(valueKey, value).apply();
            return value;
        }
        return oldValue;
    }

    /**
     * 在初始化后调用
     * - 用于清理过期数据
     * - 删除已发送数据文件
     * - copy清理无效的mapper文件, copy一次mapper文件
     * - 发送一次网络请求
     */
    void afterConstructor() {
        cleanInvalid();
        sendEvents(false);
    }

    void cleanInvalid() {
        mDbSQLite.cleanOldLog();
    }


    private void scheduleForNet(final boolean needWifi) {
        // 如果没有网络, 重新调度一个等待
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GIOSenderService senderService = mSenderService;
                if (senderService != null) {
                    senderService.scheduleForNet(needWifi);
                }
            }
        });
    }

    private void cancelScheduleForNet() {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GIOSenderService senderService = mSenderService;
                if (senderService != null) {
                    senderService.cancelScheduleForNet();
                }
            }
        });
    }

    private ActivityManager.MemoryInfo getMemoryInfo() {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    private int numOfMaxEventsPerRequest() {
        ActivityManager.MemoryInfo info = getMemoryInfo();
        if (info.lowMemory) {
            return 3;
        }
        return 50;
    }

    /**
     * 发送事件
     *
     * @param onlyInstant true -- 仅发送实时消息
     */
    void sendEvents(boolean onlyInstant) {
        NetworkUtil.NetworkState networkState = NetworkUtil.getActiveNetworkState(mContext);
        if (!networkState.isConnected()) {
            scheduleForNet(false);
            return;
        }

        Boolean scheduleForNet = null;
        int[] uploadEvents;

        if (onlyInstant) {
            uploadEvents = new int[]{GEvent.SEND_POLICY_INSTANT};
        } else if (networkState.isWifi()) {
            uploadEvents = new int[]{
                    GEvent.SEND_POLICY_INSTANT, GEvent.SEND_POLICY_MOBILE_DATA, GEvent.SEND_POLICY_WIFI
            };
        } else {
            uploadEvents = new int[]{GEvent.SEND_POLICY_INSTANT, GEvent.SEND_POLICY_MOBILE_DATA};
        }
        for (int policy : uploadEvents) {
            int result;
            do {
                if (policy != GEvent.SEND_POLICY_INSTANT
                        && networkState.isMobileData()
                        && mCellularDataLimit < todayBytes(0)) {
                    Logger.d(TAG, "今日流量已耗尽");
                    break;
                }
                result = mDbSQLite.uploadEvent(policy, numOfMaxEventsPerRequest());
                if (result == DBSQLite.UPLOAD_FAILED
                        && (scheduleForNet == null || scheduleForNet)) {
                    scheduleForNet = policy == GEvent.SEND_POLICY_WIFI;
                }
            } while (result == DBSQLite.UPLOAD_SUCCESS);
        }
        if (!onlyInstant) {
            mDbSQLite.uploadStaticEvent();
        }
        if (scheduleForNet != null) {
            Logger.d(TAG, "upload event failed, and schedule for retry later");
            scheduleForNet(scheduleForNet);
        } else {
            cancelScheduleForNet();
        }
        mCacheEventNum = 0;
    }
}
