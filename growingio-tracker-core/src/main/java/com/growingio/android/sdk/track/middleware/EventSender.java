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

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.ipc.ProcessLock;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.utils.NetworkUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

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
     * @param sender             网络发送的sender
     * @param dataUploadInterval 发送事件的时间周期，单位 s
     * @param cellularDataLimit  事件发送的移动网络的流量限制，单位 MB
     */
    @SuppressLint("WrongConstant")
    public EventSender(IEventNetSender sender, long dataUploadInterval, long cellularDataLimit) {
        mContext = TrackerContext.get().getApplicationContext();
        mCellularDataLimit = cellularDataLimit * 1024L * 1024L;
        mDataUploadInterval = dataUploadInterval * 1000L;
        mEventNetSender = sender;
        mProcessLock = new ProcessLock(mContext, EventSender.class.getName());
        mSharedPreferences = mContext.getSharedPreferences("growing3_sender", Context.MODE_PRIVATE);
        HandlerThread thread = new HandlerThread(EventSender.class.getName());
        thread.start();
        mSendHandler = new SendHandler(thread.getLooper());
    }

    private ModelLoader<EventDatabase, EventDbResult> getDatabaseModelLoader() {
        return TrackerContext.get().getRegistry().getModelLoader(EventDatabase.class, EventDbResult.class);
    }

    private EventDbResult databaseOperation(EventDatabase eventDatabase) {
        ModelLoader<EventDatabase, EventDbResult> modelLoader = getDatabaseModelLoader();
        if (modelLoader == null) {
            Logger.e(TAG, "please register database component first");
            return new EventDbResult(false);
        }
        ModelLoader.LoadData<EventDbResult> loadData = modelLoader.buildLoadData(eventDatabase);
        return loadData.fetcher.executeData();
    }

    void setEventNetSender(IEventNetSender mEventNetSender) {
        this.mEventNetSender = mEventNetSender;
    }

    public void cacheEvent(GEvent event) {
        databaseOperation(EventDatabase.insert(event));
        // 避免不触发非INSTANT事件时（如埋点SDK），cache事件不被发送
        if (mDataUploadInterval <= 0) {
            mSendHandler.uploadUninstantEvents();
        }
    }

    public void sendEvent(GEvent event) {
        databaseOperation(EventDatabase.insert(event));
        if (event.getSendPolicy() == SEND_POLICY_INSTANT) {
            mSendHandler.uploadInstantEvents();
        } else {
            if (mDataUploadInterval > 0) {
                mCacheEventNum++;
                if (mCacheEventNum >= EVENTS_BULK_SIZE) {
                    Logger.w(TAG, "cacheEventNum >= EVENTS_BULK_SIZE, toggle one send action");
                    mSendHandler.uploadUninstantEvents();
                    mCacheEventNum = 0;
                }
            } else {
                mSendHandler.uploadUninstantEvents();
            }
        }
    }

    void removeAllEvents() {
        Logger.w(TAG, "action: removeAllEvents");
        databaseOperation(EventDatabase.clear());
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
        try {
            databaseOperation(EventDatabase.outDated());
        } catch (Exception e) {
            Logger.w(TAG, "action: removeOverdueEvents,failed");
        }
    }

    @SuppressLint("WrongConstant")
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
     * 发送事件，为了保证单进程发送数据，获取进程锁后不再释放 mProcessLock.release();
     *
     * @param onlyInstant true -- 仅发送实时消息
     */
    void sendEvents(boolean onlyInstant) {
        if (!mProcessLock.isAcquired()) {
            Logger.w(TAG, "sdk sendEvents will in main process,not in sub process.");
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
            boolean succeeded;
            do {
                if (policy != SEND_POLICY_INSTANT
                        && networkState.isMobileData()
                        && mCellularDataLimit < todayBytes(0)) {
                    Logger.w(TAG, "Today's mobile data is exhausted");
                    break;
                }
                EventDbResult dbResult = databaseOperation(EventDatabase.query(policy, numOfMaxEventsPerRequest()));
                if (dbResult.isSuccess() && dbResult.getSum() > 0) {
                    SendResponse sendResponse = mEventNetSender.send(dbResult.getData(), dbResult.getMediaType());
                    succeeded = sendResponse.isSucceeded();
                    if (succeeded) {
                        String eventType = dbResult.getEventType();
                        databaseOperation(EventDatabase.delete(dbResult.getLastId(), policy, eventType));
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


    EventDbResult getGEventsFromPolicy(int policy) {
        return databaseOperation(EventDatabase.queryAndDelete(policy, numOfMaxEventsPerRequest()));
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
