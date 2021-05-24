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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.growingio.android.sdk.track.ipc.ProcessLock;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.NetworkUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.growingio.android.sdk.track.middleware.GEvent.SEND_POLICY_INSTANT;

/**
 * 事件发送者
 * - 多个进程公用同一个EventSender
 * - EventSender为单例模型，防止销毁后计数器归零
 */
public class EventSender {
    private static final String TAG = "EventSender";

    private static final int EVENTS_BULK_SIZE = 100;

    private final Context mContext;
    private final EventsSQLite mEventsSQLite;
    private IEventNetSender mEventNetSender;
    private final SharedPreferences mSharedPreferences;
    private final SendHandler mSendHandler;
    private final ProcessLock mProcessLock;

    private final long mDataUploadInterval;
    private final long mCellularDataLimit;

    private int mCacheEventNum = 0;

    /**
     * 事件发送管理类
     *
     * @param context            Context对象
     * @param sender             网络发送的sender
     * @param dataUploadInterval 发送事件的时间周期，单位 s
     * @param cellularDataLimit  事件发送的移动网络的流量限制，单位 MB
     */
    public EventSender(Context context, IEventNetSender sender, long dataUploadInterval, long cellularDataLimit) {
        mContext = context;
        mCellularDataLimit = cellularDataLimit * 1024L * 1024L;
        mDataUploadInterval = dataUploadInterval * 1000L;
        mEventsSQLite = new EventsSQLite(context);
        mEventNetSender = sender;
        mProcessLock = new ProcessLock(context, EventSender.class.getName());
        mSharedPreferences = context.getSharedPreferences("growing3_sender", Context.MODE_PRIVATE);
        HandlerThread thread = new HandlerThread(EventSender.class.getName());
        thread.start();
        mSendHandler = new SendHandler(thread.getLooper());
    }

    public void setEventNetSender(IEventNetSender mEventNetSender) {
        this.mEventNetSender = mEventNetSender;
    }

    public void cacheEvent(GEvent event) {
        mEventsSQLite.insertEvent(event);
    }

    public void sendEvent(GEvent event) {
        mEventsSQLite.insertEvent(event);
        if (event.getSendPolicy() == SEND_POLICY_INSTANT) {
            mSendHandler.uploadInstantEvents();
        } else {
            if (mDataUploadInterval > 0) {
                mCacheEventNum++;
                if (mCacheEventNum >= EVENTS_BULK_SIZE) {
                    Logger.d(TAG, "cacheEventNum >= EVENTS_BULK_SIZE, toggle one send action");
                    mSendHandler.uploadUninstantEvents();
                    mCacheEventNum = 0;
                }
            } else {
                mSendHandler.uploadUninstantEvents();
            }
        }
    }

    void removeAllEvents() {
        Logger.d(TAG, "action: removeAllEvents");
        mEventsSQLite.removeAllEvents();
    }

    /**
     * @param delta 变化量
     * @return 今日移动网络数据发送量
     */
    private long todayBytes(long delta) {
        String dateKey = "today";
        String usedBytesKey = "today_bytes";
        String todayStr = mSharedPreferences.getString(dateKey, "");

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
        String realDayTime = dayFormat.format(new Date());

        long usedBytes;
        if (!realDayTime.equals(todayStr)) {
            // 新的一天， 重新计算
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(dateKey, realDayTime);
            editor.putLong(usedBytesKey, 0);
            editor.apply();
            usedBytes = 0;
        } else {
            // 与记录数据是同一天
            usedBytes = mSharedPreferences.getLong(usedBytesKey, 0);
        }
        if (delta > 0) {
            usedBytes = usedBytes + delta;
            mSharedPreferences.edit().putLong(usedBytesKey, usedBytes).apply();
        }
        return usedBytes;
    }

    public void removeOverdueEvents() {
        mEventsSQLite.removeOverdueEvents();
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
        boolean locked = true;
        try {
            locked = mProcessLock.tryLock();
        } catch (IOException e) {
            Logger.e(TAG, e);
        }
        if (!locked) {
            Logger.e(TAG, "sendEvents: this process can not get lock");
            return;
        }

        NetworkUtil.NetworkState networkState = NetworkUtil.getActiveNetworkState(mContext);
        if (!networkState.isConnected()) {
            return;
        }

        int[] uploadEvents;
        if (onlyInstant) {
            uploadEvents = new int[]{SEND_POLICY_INSTANT};
        } else if (networkState.isWifi()) {
            uploadEvents = new int[]{SEND_POLICY_INSTANT, GEvent.SEND_POLICY_MOBILE_DATA, GEvent.SEND_POLICY_WIFI};
        } else {
            uploadEvents = new int[]{SEND_POLICY_INSTANT, GEvent.SEND_POLICY_MOBILE_DATA};
        }

        for (int policy : uploadEvents) {
            Logger.d(TAG, "uploadEventsPolicy:" + policy);
            boolean succeeded;
            do {
                if (policy != SEND_POLICY_INSTANT
                        && networkState.isMobileData()
                        && mCellularDataLimit < todayBytes(0)) {
                    Logger.e(TAG, "Today's mobile data is exhausted");
                    break;
                }
                List<GEvent> resultEvents = new ArrayList<>();
                long lastId = mEventsSQLite.queryEvents(policy, numOfMaxEventsPerRequest(), resultEvents);
                Logger.d(TAG, "uploadEventsLastId:" + lastId);
                if (!resultEvents.isEmpty()) {
                    SendResponse sendResponse = mEventNetSender.send(resultEvents);
                    succeeded = sendResponse.isSucceeded();
                    if (succeeded) {
                        String eventType = resultEvents.get(0).getEventType();
                        mEventsSQLite.removeEvents(lastId, policy, eventType);
                        if (networkState.isMobileData()) {
                            todayBytes(sendResponse.getUsedBytes());
                        }
                    }
                } else {
                    break;
                }
            } while (succeeded);
        }
    }


    public List<GEvent> getGEventsFromPolicy(int policy) {
        List<GEvent> resultEvents = new ArrayList<>();
        long lastId = mEventsSQLite.queryEventsAndDelete(policy, numOfMaxEventsPerRequest(), resultEvents);
        return resultEvents;
    }

    // 由于数据发送是耗时操作，网络端更有可能被block，所以这里另起一个线程处理
    private final class SendHandler extends Handler {
        private static final int MSG_SEND_INSTANT_EVENTS = 1;
        private static final int MSG_SEND_UNINSTANT_EVENTS = 2;

        private SendHandler(@NonNull Looper looper) {
            super(looper);
            if (mDataUploadInterval > 0) {
                sendEmptyMessageDelayed(MSG_SEND_UNINSTANT_EVENTS, mDataUploadInterval);
            }
        }

        private void uploadInstantEvents() {
            removeMessages(MSG_SEND_INSTANT_EVENTS);
            sendEmptyMessage(MSG_SEND_INSTANT_EVENTS);
        }

        private void uploadUninstantEvents() {
            removeMessages(MSG_SEND_UNINSTANT_EVENTS);
            sendEmptyMessage(MSG_SEND_UNINSTANT_EVENTS);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_SEND_INSTANT_EVENTS:
                    sendEvents(true);
                    break;
                case MSG_SEND_UNINSTANT_EVENTS:
                    removeMessages(MSG_SEND_UNINSTANT_EVENTS);
                    sendEvents(false);
                    if (mDataUploadInterval > 0) {
                        sendEmptyMessageDelayed(MSG_SEND_UNINSTANT_EVENTS, mDataUploadInterval);
                    }
                    break;
                default:
                    Logger.e(TAG, "Unexpected value: " + msg.what);
            }
        }
    }
}
