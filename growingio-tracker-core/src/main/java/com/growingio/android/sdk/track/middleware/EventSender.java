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
package com.growingio.android.sdk.track.middleware;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.growingio.android.sdk.track.ipc.ProcessLock;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;
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

    private final Context mContext;
    private IEventNetSender mEventNetSender;
    private final SharedPreferences mSharedPreferences;
    private final SendHandler mSendHandler;
    private final ProcessLock mProcessLock;
    private final long mCellularDataLimit;
    private final TrackerRegistry mRegistry;

    /**
     * 事件发送管理类
     *
     * @param sender             网络发送的sender
     * @param dataUploadInterval 发送事件的时间周期，单位 s
     * @param cellularDataLimit  事件发送的移动网络的流量限制，单位 MB
     */
    @SuppressLint("WrongConstant")
    public EventSender(Context context, TrackerRegistry registry, IEventNetSender sender, long dataUploadInterval, long cellularDataLimit) {
        mContext = context.getApplicationContext();
        mRegistry = registry;
        mCellularDataLimit = cellularDataLimit * 1024L * 1024L;
        mEventNetSender = sender;
        mProcessLock = new ProcessLock(mContext, EventSender.class.getName());
        mSharedPreferences = mContext.getSharedPreferences("growing3_sender", Context.MODE_PRIVATE);
        HandlerThread thread = new HandlerThread(EventSender.class.getName());
        thread.start();
        mSendHandler = new SendHandler(thread.getLooper(), dataUploadInterval * 1000L);
    }

    public void shutdown() {
        mProcessLock.release();
        mSendHandler.removeCallbacksAndMessages(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mSendHandler.getLooper().quitSafely();
        } else {
            mSendHandler.getLooper().quit();
        }
    }

    private ModelLoader<EventDatabase, EventDbResult> getDatabaseModelLoader() {
        return mRegistry.getModelLoader(EventDatabase.class, EventDbResult.class);
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
        // 避免不触发非INSTANT事件时（如埋点SDK），cache事件不被发送
        databaseOperation(EventDatabase.insert(event));
    }

    public void sendEvent(GEvent event) {
        databaseOperation(EventDatabase.insert(event));
        if (event.getSendPolicy() == SEND_POLICY_INSTANT) {
            mSendHandler.uploadInstantEvent();
        } else {
            mSendHandler.uploadUninstantEvent();
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
            return 10;
        }
        return 100;
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

        boolean succeeded = true;
        for (int policy : uploadEvents) {
            if (!succeeded) {
                Logger.e(TAG, "upload events break with http failed.");
                break;
            }
            do {
                if (policy != SEND_POLICY_INSTANT
                        && networkState.isMobileData()
                        && mCellularDataLimit < todayBytes(0)) {
                    Logger.w(TAG, "Today's mobile data is exhausted");
                    break;
                }
                EventDbResult dbResult = databaseOperation(EventDatabase.query(policy, numOfMaxEventsPerRequest()));
                if (dbResult.isSuccess() && dbResult.getSum() > 0) {
                    if (mEventNetSender == null) {
                        succeeded = false;
                    } else {
                        SendResponse sendResponse = mEventNetSender.send(dbResult.getData(), dbResult.getMediaType());
                        succeeded = sendResponse.isSucceeded();
                        int responseCode = sendResponse.getResponseCode();
                        if (succeeded) {
                            String eventType = dbResult.getEventType();
                            databaseOperation(EventDatabase.delete(dbResult.getLastId(), policy, eventType));
                            if (networkState.isMobileData()) {
                                todayBytes(sendResponse.getUsedBytes());
                            }
                            mSendHandler.resetBackoff();
                        } else if (responseCode == 451) {
                            // 451 Unavailable For Legal Reasons
                            mSendHandler.backoff();
                            Logger.e(TAG, "action: sendEvents, backoff with 451 Unavailable For Legal Reasons");
                            break;
                        } else if (responseCode >= 400 && responseCode < 500) {
                            String eventType = dbResult.getEventType();
                            databaseOperation(EventDatabase.delete(dbResult.getLastId(), policy, eventType));
                            if (networkState.isMobileData()) {
                                todayBytes(sendResponse.getUsedBytes());
                            }
                            Logger.e(TAG, "action: sendEvents, delete events with responseCode: " + responseCode);
                            break;
                        } else if (responseCode >= 500) {
                            // 5xx Service Unavailable
                            mSendHandler.backoff();
                            Logger.e(TAG, "action: sendEvents, service unavailable with responseCode: " + responseCode);
                            break;
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

        private static final long EVENTS_UPLOAD_INTERVAL_MAX = 5 * 60 * 1000; // 5 minutes
        private static final int EVENTS_BULK_SIZE = 100;

        private final long mDataUploadInterval;
        private long backoffUploadInterval;
        private int cacheEventNum = 0;

        private SendHandler(@NonNull Looper looper, long dataUploadInterval) {
            super(looper);
            this.mDataUploadInterval = dataUploadInterval;
            backoffUploadInterval = mDataUploadInterval;
            if (backoffUploadInterval > 0) {
                sendEmptyMessageDelayed(MSG_SEND_UNINSTANT_EVENTS, backoffUploadInterval);
            }
        }

        void backoff() {
            if (backoffUploadInterval > 0) {
                backoffUploadInterval = Math.min(backoffUploadInterval * 2, EVENTS_UPLOAD_INTERVAL_MAX);
            } else {
                backoffUploadInterval = 15000L;
            }
            removeCallbacksAndMessages(null);
            sendEmptyMessageDelayed(MSG_SEND_UNINSTANT_EVENTS, backoffUploadInterval);
        }

        void resetBackoff() {
            if (isNotBackoffState()) return;
            backoffUploadInterval = mDataUploadInterval;
            removeCallbacksAndMessages(null);
            if (backoffUploadInterval > 0) {
                sendEmptyMessageDelayed(MSG_SEND_UNINSTANT_EVENTS, backoffUploadInterval);
            }
        }

        boolean isNotBackoffState() {
            return backoffUploadInterval == mDataUploadInterval;
        }

        private void uploadInstantEvent() {
            if (isNotBackoffState()) {
                removeMessages(MSG_SEND_INSTANT_EVENTS);
                sendEmptyMessage(MSG_SEND_INSTANT_EVENTS);
            }
        }

        private void uploadUninstantEvent() {
            if (backoffUploadInterval > 0) {
                cacheEventNum++;
                // If it is a non-real-time event,
                // it will be sent immediately when the number of cached events reaches a certain amount,
                // provided that it is not in a backoff state.
                if (cacheEventNum >= EVENTS_BULK_SIZE && isNotBackoffState()) {
                    Logger.w(TAG, "cacheEventNum >= EVENTS_BULK_SIZE, merge events and send.");
                    cacheEventNum = 0;
                    removeMessages(MSG_SEND_UNINSTANT_EVENTS);
                    sendEmptyMessage(MSG_SEND_UNINSTANT_EVENTS);
                }
            } else {
                removeMessages(MSG_SEND_UNINSTANT_EVENTS);
                sendEmptyMessage(MSG_SEND_UNINSTANT_EVENTS);
            }
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
                    if (backoffUploadInterval > 0) {
                        sendEmptyMessageDelayed(MSG_SEND_UNINSTANT_EVENTS, backoffUploadInterval);
                    }
                    break;
                default:
                    Logger.e(TAG, "Unexpected value: " + msg.what);
            }
        }
    }
}
