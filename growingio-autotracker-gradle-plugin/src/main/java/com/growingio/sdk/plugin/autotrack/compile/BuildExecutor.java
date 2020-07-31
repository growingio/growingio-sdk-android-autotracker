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

package com.growingio.sdk.plugin.autotrack.compile;

import com.android.annotations.VisibleForTesting;
import com.android.ide.common.internal.WaitableExecutor;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

abstract public class BuildExecutor implements Executor {

    private static boolean sHasWaitableExecutor;

    static {
        try {
            // 2.3.3版本下execute(Ljava/util/concurrent/Callable;)不是ForkJoinTask
            Class<?> waitableExecutorClazz = Class.forName("com.android.ide.common.internal.WaitableExecutor");
            Method method = waitableExecutorClazz.getMethod("execute", Callable.class);
            sHasWaitableExecutor = method.getReturnType() == ForkJoinTask.class;
        } catch (Throwable e) {
            sHasWaitableExecutor = false;
        }
    }


    public abstract void waitAllTaskComplete() throws InterruptedException;

    public static BuildExecutor createExecutor() {
        return sHasWaitableExecutor ? new BuildWaitableExecutor() : new CacheWaitableExecutor();
    }

    @VisibleForTesting
    static class CacheWaitableExecutor extends BuildExecutor {
        private final ThreadPoolExecutor mPoolExecutor;
        private final AtomicInteger mWaitingTaskCount = new AtomicInteger(0);
        private ThreadFactory mDefaultThreadFactory = Executors.defaultThreadFactory();
        private Throwable mThrowable;
        private final Object mLock = new Object();

        CacheWaitableExecutor() {
            int processorsNum = Runtime.getRuntime().availableProcessors();
            mPoolExecutor = new ThreadPoolExecutor(processorsNum,
                    processorsNum * 2,
                    60L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>()) {
                @Override
                protected void afterExecute(Runnable runnable, Throwable th) {
                    if (mThrowable != null) {
                        return;
                    }
                    if (th != null || mWaitingTaskCount.decrementAndGet() == 0) {
                        synchronized (mLock) {
                            if (mThrowable != null) {
                                return;
                            }
                            if (th != null) {
                                getQueue().clear();
                                mThrowable = th;
                            }
                            mLock.notifyAll();
                        }
                    }
                }
            };
            mPoolExecutor.setThreadFactory(runnable -> {
                Thread result = mDefaultThreadFactory.newThread(runnable);
                result.setUncaughtExceptionHandler((thread, th) -> {
                    // ignore： 仅仅为了消除默认handler的日志
                });
                return result;
            });
        }

        @Override
        public void waitAllTaskComplete() throws InterruptedException {
            checkAndThrow();
            synchronized (mLock) {
                checkAndThrow();
                mPoolExecutor.shutdown();
                if (mWaitingTaskCount.get() != 0) {
                    mLock.wait();
                    checkAndThrow();
                }
            }
            mPoolExecutor.awaitTermination(0, TimeUnit.MILLISECONDS);
        }

        private void checkAndThrow() {
            if (mThrowable instanceof RuntimeException) {
                throw (RuntimeException) mThrowable;
            } else if (mThrowable != null) {
                throw new RuntimeException(mThrowable);
            }
        }

        @Override
        public void execute(@NotNull Runnable runnable) {
            mWaitingTaskCount.incrementAndGet();
            mPoolExecutor.execute(runnable);
        }
    }


    @VisibleForTesting
    static class BuildWaitableExecutor extends BuildExecutor {

        private final WaitableExecutor mExecutor = WaitableExecutor.useGlobalSharedThreadPool();

        @Override
        public void waitAllTaskComplete() throws InterruptedException {
            mExecutor.waitForTasksWithQuickFail(true);
        }

        @Override
        public void execute(@NotNull final Runnable runnable) {
            mExecutor.execute(() -> {
                runnable.run();
                return null;
            });
        }
    }
}
