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
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.app.job.JobWorkItem;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.BaseBundle;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.UiThread;

import com.growingio.android.sdk.track.ContextProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.ThreadUtils;
import com.growingio.android.sdk.track.variation.EventHttpSender;
import com.growingio.android.sdk.track.variation.TrackEventJsonMarshaller;

/**
 * 发送服务, 主要使用JobScheduler进行任务调度, 由于Android 5.0之下没有JobScheduler, 5.0以下Service长活
 */
public class GIOSenderService extends Service {
    private static final int JOB_SCHEDULER_START_COMMAND = 202052013;
    private static final int JOB_SCHEDULER_WAIT_NET = 202052014;
    private static final int WAIT_NET_MINIMUM_LATENCY = 60_000;
    private static final int WAIT_NET_OVERRIDE_DEADLINE = 60 * 60_000;
    private static final long DELAY_STOP_TIME_MILLS = 70_000;

    public static final String ARG_COMMAND = "arg_command";
    static final int ACTION_SEND_WITH_LIMIT = 1;  // 发送一次事件
    static final int ACTION_NEW_EVENT = 2;              // 接受到一个新的事件
    static final int ACTION_DEL_ALL = 3;                  // 清除数据库所有数据
    static final int ACTION_CLEAN_INVALID = 4;      // 清除过期数据
    private static final String TAG = "GIO.GSender";
    private static EventSender sEventSender;
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private ServiceImpl mServiceImpl;

    /**
     * 调用context的startService方法
     * 由于Android8.0不允许后台应用startService, 这里使用JobScheduler进行封装
     */
    public static void startService(Context context, Bundle bundle) {
        try {
            Intent intent = new Intent(context, GIOSenderService.class);
            intent.putExtras(bundle);
            context.startService(intent);
        } catch (IllegalStateException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Logger.d(TAG, "after Android O, only foreground app can startService, use JobScheduler");
                enqueueWork(context, bundle);
            } else {
                Logger.e(TAG, "startService, and IllegalStateException");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("JobSchedulerService")
    private static void enqueueWork(Context context, Bundle bundle) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler == null) {
            Logger.e(TAG, "enqueueWork, but jobScheduler is null, return");
            return;
        }
        JobInfo job = new JobInfo.Builder(JOB_SCHEDULER_START_COMMAND,
                new ComponentName(context, GIOSenderService.class))
                .setOverrideDeadline(0)
                .build();
        Intent intent = new Intent();
        intent.putExtras(bundle);
        jobScheduler.enqueue(job, new JobWorkItem(intent));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d(TAG, "onCreate");
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mServiceImpl = new JobServiceDelegate(this);
        } else {
            mServiceImpl = new StartCommandServiceDelegate();
        }
        if (sEventSender != null) {
            sEventSender.setSenderService(this);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mServiceImpl.onBind(intent);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        WorkItem item = mServiceImpl.newWorkItem(intent);
        if (item == null) {
            Logger.d(TAG, "onStartCommand, but WorkItem is null, skip this work");
            return;
        }
        Message message = Message.obtain(mServiceHandler, ServiceHandler.MSG_WORK);
        message.obj = item;
        mServiceHandler.sendMessage(message);
    }

    void stopSelfInMain() {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mServiceHandler.hasMoreWork()) {
                    Logger.d(TAG, "delayStop, but has more message");
                    delayStop();
                } else {
                    Logger.d(TAG, "no message in a long time, and stop service");
                    stopSelf();
                }
            }
        });
    }

    // 子线程中处理任务, single thread
    void handleExtras(BaseBundle intent) {
        Integer action = getFromBundle(intent, ARG_COMMAND);
        if (action == null) {
            return;
        }
        if (sEventSender == null) {
            sEventSender = new EventSender(getApplicationContext(), new EventHttpSender(new TrackEventJsonMarshaller()));
            sEventSender.setSenderService(this);
            sEventSender.afterConstructor();
        }
        switch (action) {
            case ACTION_SEND_WITH_LIMIT:
                sEventSender.sendEvents(false);
                break;
            case ACTION_NEW_EVENT: {
                Boolean instant = getFromBundle(intent, "instant");
                sEventSender.onEventWrite(instant != null ? instant : false);
                if (instant != null && !instant && !mServiceHandler.hasMessages(ServiceHandler.MSG_WINDOW_SEND)) {
                    mServiceHandler.sendEmptyMessageDelayed(ServiceHandler.MSG_WINDOW_SEND,
                            ConfigurationProvider.get().getTrackConfiguration().getDataUploadInterval());
                }
                break;
            }
            case ACTION_DEL_ALL: {
                sEventSender.delAllMsg();
                break;
            }
            case ACTION_CLEAN_INVALID: {
                sEventSender.cleanInvalid();
                break;
            }
            default:
                Logger.d(TAG, "unknown action: %s", action);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T> T getFromBundle(BaseBundle bundle, String key) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return (T) bundle.get(key);
        } else {
            return (T) ((Bundle) bundle).get(key);
        }
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG, "onDestroy");
        super.onDestroy();
        if (mServiceHandler.hasMoreWork()) {
            delayRestartWhenDestroy();
        }
        if (mServiceLooper != null) {
            mServiceLooper.quit();
            mServiceLooper = null;
        }
        if (sEventSender != null) {
            sEventSender.setSenderService(null);
        }
    }

    private void delayRestartWhenDestroy() {
        Logger.d(TAG, "serviceHandler has more work, and restart service");
        ThreadUtils.postOnUiThreadDelayed(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putInt(ARG_COMMAND, ACTION_SEND_WITH_LIMIT);
                GIOSenderService.startService(ContextProvider.getApplicationContext(), bundle);
            }
        }, 2000);
    }

    void delayStop() {
        mServiceImpl.delayStop();
    }

    void scheduleForNet(boolean isWifi) {
        mServiceImpl.scheduleForNet(isWifi);
    }

    void cancelScheduleForNet() {
        mServiceImpl.cancelScheduleForNet();
    }

    private interface ServiceImpl {
        IBinder onBind(Intent intent);

        WorkItem newWorkItem(Intent intent);

        void delayStop();

        void scheduleForNet(boolean isWifi);

        void cancelScheduleForNet();
    }

    private interface WorkItem {
        BaseBundle getExtras();

        void onWorkComplete();
    }

    static class StartCommandWorkItemImpl implements WorkItem {
        StartCommandServiceDelegate mDelegate;
        Intent mIntent;

        StartCommandWorkItemImpl(StartCommandServiceDelegate delegate, Intent intent) {
            this.mDelegate = delegate;
            this.mIntent = intent;
        }

        @Override
        public BaseBundle getExtras() {
            return mIntent.getExtras();
        }

        @Override
        public void onWorkComplete() {
            mDelegate.onWorkComplete();
        }
    }

    // Android 5.0之上使用JobScheduler作为任务调度
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    static class JobServiceDelegate extends JobService implements ServiceImpl {

        private final GIOSenderService mService;
        private StartCommandServiceDelegate mStartCommandServiceDelegate;

        JobServiceDelegate(GIOSenderService service) {
            this.mService = service;
        }

        @Override
        public Looper getMainLooper() {
            return mService.getMainLooper();
        }

        @Override
        public boolean onStartJob(JobParameters params) {
            if (params.getExtras().size() != 0) {
                JobParamsWorkItem item = new JobParamsWorkItem(params);
                Message message = Message.obtain(mService.mServiceHandler, ServiceHandler.MSG_WORK);
                message.obj = item;
                mService.mServiceHandler.sendMessage(message);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startJobAfterO(params);
            } else {
                return false;
            }
            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void startJobAfterO(JobParameters params) {
            postOneQueueWork(params);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        void postOneQueueWork(JobParameters parameters) {
            JobWorkItem workItem = parameters.dequeueWork();
            if (workItem == null) {
                mService.delayStop();
                return;
            }
            JobQueueWorkItem item = new JobQueueWorkItem(workItem, parameters);
            Message msg = Message.obtain(mService.mServiceHandler, ServiceHandler.MSG_WORK);
            msg.obj = item;
            mService.mServiceHandler.sendMessage(msg);
        }

        @Override
        public boolean onStopJob(JobParameters params) {
            return false;
        }

        @Override
        public WorkItem newWorkItem(Intent intent) {
            if (intent == null) {
                return null;
            }
            if (mStartCommandServiceDelegate == null) {
                mStartCommandServiceDelegate = mService.new StartCommandServiceDelegate() {
                    @Override
                    public void onWorkComplete() {
                        super.onWorkComplete();
                        mService.delayStop();
                    }
                };
            }
            return mStartCommandServiceDelegate.newWorkItem(intent);
        }

        @Override
        public void delayStop() {
            if (mService.mServiceLooper == null) {
                return;
            }
            mService.mServiceHandler.sendMessageDelayed(
                    Message.obtain(mService.mServiceHandler, ServiceHandler.MSG_DELAY_STOP),
                    DELAY_STOP_TIME_MILLS);
        }

        @Override
        public Context getBaseContext() {
            return mService.getBaseContext();
        }

        @Override
        public void scheduleForNet(boolean isWifi) {
            @SuppressLint("JobSchedulerService")
            JobInfo.Builder builder = new JobInfo.Builder(JOB_SCHEDULER_WAIT_NET,
                    new ComponentName(mService.getApplicationContext(), GIOSenderService.class));
            if (isWifi) {
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
            } else {
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            }
            PersistableBundle persistableBundle = new PersistableBundle();
            persistableBundle.putInt(ARG_COMMAND, ACTION_SEND_WITH_LIMIT);
            builder.setExtras(persistableBundle);
            builder.setMinimumLatency(WAIT_NET_MINIMUM_LATENCY);
            builder.setOverrideDeadline(WAIT_NET_OVERRIDE_DEADLINE); // 最长延迟一小时唤醒一次
            JobInfo info = builder.build();
            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (jobScheduler == null) {
                mStartCommandServiceDelegate.scheduleForNet(isWifi);
            } else {
                jobScheduler.schedule(info);
            }
        }

        @Override
        public Object getSystemService(String name) {
            return mService.getSystemService(name);
        }

        @Override
        public void cancelScheduleForNet() {
            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (jobScheduler != null) {
                jobScheduler.cancel(JOB_SCHEDULER_WAIT_NET);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        class JobQueueWorkItem implements WorkItem {

            JobWorkItem mJobWorkItem;
            JobParameters mJobParameters;

            JobQueueWorkItem(JobWorkItem jobWorkItem, JobParameters jobParameters) {
                this.mJobWorkItem = jobWorkItem;
                this.mJobParameters = jobParameters;
            }

            @Override
            public BaseBundle getExtras() {
                return mJobWorkItem.getIntent().getExtras();
            }

            @Override
            public void onWorkComplete() {
                mJobParameters.completeWork(mJobWorkItem);
                postOneQueueWork(mJobParameters);
            }
        }

        class JobParamsWorkItem implements WorkItem {
            JobParameters mParameters;

            JobParamsWorkItem(JobParameters parameters) {
                this.mParameters = parameters;
            }

            @Override
            public BaseBundle getExtras() {
                return mParameters.getExtras();
            }

            @Override
            public void onWorkComplete() {
                jobFinished(mParameters, true);
            }
        }
    }

    class ServiceHandler extends Handler {

        static final int MSG_DELAY_STOP = 2;
        static final int MSG_WINDOW_SEND = 3;
        static final int MSG_WORK = 4;
        static final int MSG_LOW_VERSION_WAIT = 5;

        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public boolean sendMessageAtTime(@NonNull Message msg, long uptimeMillis) {
            removeMessages(MSG_DELAY_STOP);
            return super.sendMessageAtTime(msg, uptimeMillis);
        }

        public boolean hasMoreWork() {
            return hasMessages(MSG_WINDOW_SEND)
                    || hasMessages(MSG_WORK)
                    || hasMessages(MSG_LOW_VERSION_WAIT);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_WORK: {
                    WorkItem item = (WorkItem) msg.obj;
                    try {
                        handleExtras(item.getExtras());
                    } finally {
                        item.onWorkComplete();
                        delayStop();
                    }
                    break;
                }
                case MSG_WINDOW_SEND: {
                    removeMessages(MSG_WINDOW_SEND);
                    sEventSender.sendEvents(false);
                    break;
                }
                case MSG_DELAY_STOP: {
                    removeMessages(MSG_DELAY_STOP);
                    stopSelfInMain();
                    break;
                }
                case MSG_LOW_VERSION_WAIT: {
                    removeMessages(MSG_LOW_VERSION_WAIT);
                    sEventSender.sendEvents(false);
                    break;
                }
                default:
                    Logger.e(TAG, "ServiceHandler unknown msg: %s", msg);
            }
        }
    }

    // startService成功的Delegate
    class StartCommandServiceDelegate implements ServiceImpl {

        PowerManager.WakeLock mWakeLock;

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @UiThread
        @Override
        public WorkItem newWorkItem(Intent intent) {
            if (intent == null) {
                return null;
            }
            if (mWakeLock == null) {
                PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, ":GIO.Sender.run");
            }
            mWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
            return new StartCommandWorkItemImpl(this, intent);
        }

        @Override
        public void delayStop() {
            // ignore Android 5.0一下没有JobScheduler, 不停止Service
        }

        @Override
        public void scheduleForNet(boolean isWifi) {
            mServiceHandler.sendMessageDelayed(Message.obtain(mServiceHandler, ServiceHandler.MSG_LOW_VERSION_WAIT),
                    ConfigurationProvider.get().getTrackConfiguration().getDataUploadInterval());
        }

        @Override
        public void cancelScheduleForNet() {

        }

        public void onWorkComplete() {
            mWakeLock.release();
        }
    }
}
