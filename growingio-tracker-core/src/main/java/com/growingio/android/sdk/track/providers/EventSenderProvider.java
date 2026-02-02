/*
 *  Copyright (C) 2026 Beijing Yishu Technology Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.growingio.android.sdk.track.providers;

import static com.growingio.android.sdk.track.middleware.GEvent.SEND_POLICY_INSTANT;

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

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.ipc.ProcessLock;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.EventDatabase;
import com.growingio.android.sdk.track.middleware.EventDbResult;
import com.growingio.android.sdk.track.middleware.EventHttpSender;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.middleware.IEventNetSender;
import com.growingio.android.sdk.track.middleware.SendResponse;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.utils.NetworkUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EventSenderProvider implements TrackerLifecycleProvider {

    private static final String TAG = "EventSenderProvider";

    private TrackerContext context;
    private ConfigurationProvider configurationProvider;
    private EventSenderProvider.SendHandler sendHandler;
    private IEventNetSender eventNetSender;
    private ProcessLock processLock;
    private SharedPreferences sharedPreferences;

    @Override
    @SuppressWarnings("WrongConstant")
    public void setup(TrackerContext context) {
        configurationProvider = context.getConfigurationProvider();
        long dataUploadInterval = configurationProvider.core().getDataUploadInterval();

        sharedPreferences = context.getSharedPreferences("growing3_sender", Context.MODE_PRIVATE);
        processLock = new ProcessLock(context, EventSenderProvider.class.getName());
        eventNetSender = new EventHttpSender(context);

        HandlerThread thread = new HandlerThread(EventSenderProvider.class.getName());
        thread.start();
        sendHandler = new EventSenderProvider.SendHandler(thread.getLooper(), dataUploadInterval * 1000L);

        this.context = context;
    }

    public void flush() {
        if (sendHandler != null) sendHandler.flush();
    }

    @Override
    public void shutdown() {
        processLock.release();
        sendHandler.removeCallbacksAndMessages(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            sendHandler.getLooper().quitSafely();
        } else {
            sendHandler.getLooper().quit();
        }
    }

    private ModelLoader<EventDatabase, EventDbResult> getDatabaseModelLoader() {
        return this.context.getRegistry().getModelLoader(EventDatabase.class, EventDbResult.class);
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

    // for test
    void setEventNetSender(IEventNetSender eventNetSender) {
        this.eventNetSender = eventNetSender;
    }

    public void cacheEvent(GEvent event) {
        // 避免不触发非INSTANT事件时（如埋点SDK），cache事件不被发送
        databaseOperation(EventDatabase.insert(event));
    }

    public void sendEvent(GEvent event) {
        databaseOperation(EventDatabase.insert(event));
        if (event.getSendPolicy() == SEND_POLICY_INSTANT) {
            sendHandler.uploadInstantEvent();
        } else {
            sendHandler.uploadUninstantEvent();
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
        String todayStr = sharedPreferences.getString(dateKey, "");

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
        String realDayTime = dayFormat.format(new Date());

        long usedBytes;
        if (!realDayTime.equals(todayStr)) {
            // 新的一天， 重新计算
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(dateKey, realDayTime);
            editor.putLong(usedBytesKey, 0);
            editor.apply();
            usedBytes = 0;
        } else {
            // 与记录数据是同一天
            usedBytes = sharedPreferences.getLong(usedBytesKey, 0);
        }
        if (delta > 0) {
            usedBytes = usedBytes + delta;
            sharedPreferences.edit().putLong(usedBytesKey, usedBytes).apply();
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
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
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
        if (processLock == null || !processLock.isAcquired()) {
            Logger.w(TAG, "sdk sendEvents will in main process,not in sub process.");
            return;
        }

        NetworkUtil.NetworkState networkState = NetworkUtil.getActiveNetworkState(context);
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
        long cellularDataLimit = configurationProvider.core().getCellularDataLimit();
        long cellularDataLimitTotal = cellularDataLimit * 1024L * 1024L;
        for (int policy : uploadEvents) {
            if (!succeeded) {
                Logger.e(TAG, "upload events break with http failed.");
                break;
            }
            do {
                if (policy != SEND_POLICY_INSTANT
                        && networkState.isMobileData()
                        && cellularDataLimitTotal < todayBytes(0)) {
                    Logger.w(TAG, "Today's mobile data is exhausted");
                    break;
                }
                EventDbResult dbResult = databaseOperation(EventDatabase.query(policy, numOfMaxEventsPerRequest()));
                if (dbResult.isSuccess() && dbResult.getSum() > 0) {
                    if (eventNetSender == null) {
                        succeeded = false;
                    } else {
                        SendResponse sendResponse = eventNetSender.send(dbResult.getData(), dbResult.getMediaType());
                        succeeded = sendResponse.isSucceeded();
                        int responseCode = sendResponse.getResponseCode();
                        if (succeeded) {
                            String eventType = dbResult.getEventType();
                            databaseOperation(EventDatabase.delete(dbResult.getLastId(), policy, eventType));
                            if (networkState.isMobileData()) {
                                todayBytes(sendResponse.getUsedBytes());
                            }
                            sendHandler.resetBackoff();
                        } else if (responseCode == 413) {
                            String eventType = dbResult.getEventType();
                            databaseOperation(EventDatabase.delete(dbResult.getLastId(), policy, eventType));
                            if (networkState.isMobileData()) {
                                todayBytes(sendResponse.getUsedBytes());
                            }
                            Logger.e(TAG, "action: sendEvents, delete events with responseCode: " + responseCode);
                            break;
                        } else if (responseCode >= 400 || responseCode == 0) {
                            // mark the events as undeliverable
                            databaseOperation(EventDatabase.update(dbResult.getLastId(), dbResult.getEventType()));
                            // Logger.e(TAG, "action: sendEvents, backoff with some reasons,eg: Unavailable For Legal Reasons");
                            // 5xx Service Unavailable
                            sendHandler.backoff();
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
                    flush();
                }
            } else {
                flush();
            }
        }

        private void flush() {
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
