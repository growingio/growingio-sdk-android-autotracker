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

import android.content.Context;

import com.growingio.android.sdk.track.log.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class ProcessLock {
    private static final String TAG = "ProcessLock";

    private final Context mContext;
    private final String mName;
    private volatile FileLock mLock;

    public ProcessLock(Context context, String name) {
        mContext = context;
        mName = name + ".lock";
    }

    /**
     * 该方法会block，直到别的持有锁的地方release
     */
    public void lock() throws IOException {
        if (mLock != null) {
            return;
        }
        FileOutputStream outputStream = mContext.openFileOutput(mName, Context.MODE_PRIVATE);
        FileChannel channel = outputStream.getChannel();
        mLock = channel.lock();
    }

    /**
     * 该方法不会block，无论有没有获取到lock都会立刻返回
     *
     * @return 是否成功获取lock
     */
    public boolean tryLock() throws IOException {
        if (mLock != null) {
            return mLock.isValid();
        }
        FileOutputStream outputStream = mContext.openFileOutput(mName, Context.MODE_PRIVATE);
        FileChannel channel = outputStream.getChannel();
        mLock = channel.tryLock();
        return mLock != null;
    }

    /**
     * 该方法会block，直到成功获取lock或者超时
     *
     * @param timeout 超时时间
     * @return 是否成功获取lock
     */
    public boolean tryLock(long timeout) throws IOException {
        long endTime = System.currentTimeMillis() + timeout;
        do {
            if (tryLock()) {
                return true;
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Logger.e(TAG, e);
                }
            }
        } while (System.currentTimeMillis() < endTime);
        return false;
    }

    public void release() {
        try {
            if (mLock != null) {
                mLock.release();
            }
            mLock = null;
        } catch (IOException e) {
            Logger.e(TAG, e);
        }
    }
}
