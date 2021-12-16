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

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

public class ProcessLock {
    private static final String TAG = "ProcessLock";

    private final Context mContext;
    private final String mName;
    private FileOutputStream mOutputStream;
    private FileChannel mFileChannel;
    private FileLock mFileLock;

    public ProcessLock(Context context, String name) {
        mContext = context;
        mName = name + ".lock";
    }

    private FileLock getFileLock() {
        if (mFileLock == null) {
            try {
                if (mOutputStream == null || mFileChannel == null) {
                    mOutputStream = mContext.openFileOutput(mName, Context.MODE_PRIVATE);
                    mFileChannel = mOutputStream.getChannel();
                }
                mFileLock = mFileChannel.tryLock();
            } catch (IOException e) {
                return null;
            } catch (OverlappingFileLockException e) {
                return null;
            }
        }
        return mFileLock;
    }

    private void internalRelease() throws IOException {
        try {
            if (mFileLock != null && mFileLock.isValid()) {
                mFileLock.release();
            }
        } finally {
            try {
                if (mFileChannel != null) {
                    mFileChannel.close();
                }
            } finally {
                if (mOutputStream != null) {
                    mOutputStream.close();
                }
            }
        }
    }

    public void lockedRun(Runnable runnable) {
        if (mFileLock == null) {
            try {
                if (mOutputStream == null || mFileChannel == null) {
                    mOutputStream = mContext.openFileOutput(mName, Context.MODE_PRIVATE);
                    mFileChannel = mOutputStream.getChannel();
                }
                mFileLock = mFileChannel.lock();
                runnable.run();
            } catch (Exception ignored) {
            } finally {
                release();
            }
        }

    }

    public void release() {
        try {
            internalRelease();
        } catch (IOException ignored) {
        } finally {
            mOutputStream = null;
            mFileChannel = null;
            mFileLock = null;
        }
    }

    public boolean isAcquired() {
        FileLock fileLock = getFileLock();
        return fileLock != null && fileLock.isValid();
    }
}
