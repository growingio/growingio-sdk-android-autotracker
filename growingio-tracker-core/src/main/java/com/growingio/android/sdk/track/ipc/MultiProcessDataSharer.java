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

import androidx.annotation.Nullable;

import com.growingio.android.sdk.track.log.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;


public class MultiProcessDataSharer implements IDataSharer {
    private static final String TAG = "MultiProcessDataSharer";

    private final Context mContext;
    private final String mName;
    private final int mMaxSize;

    private volatile boolean mLoaded;
    private MappedByteBuffer mMappedByteBuffer;
    private FileChannel mFileChannel;
    private final Map<String, SharedEntry> mSharedEntries = new HashMap<>();
    private int mCurrentPosition = 0;

    public MultiProcessDataSharer(Context context, String name, int maxSize) {
        mContext = context;
        mName = name + ".shared";
        mMaxSize = maxSize;
        mLoaded = false;
        startLoadFromDisk();
    }

    private void startLoadFromDisk() {
        synchronized (this) {
            mLoaded = false;
        }
        new Thread("MultiProcessDataSharer-load") {
            public void run() {
                loadFromDisk();
            }
        }.start();
    }

    private void awaitLoadedLocked() {
        while (!mLoaded) {
            try {
                Logger.d(TAG, "awaitLoadedLocked");
                wait();
                Logger.d(TAG, "awaitLoadedLocked end");
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void loadFromDisk() {
        synchronized (this) {
            Logger.d(TAG, "loadFromDisk mLoaded is " + mLoaded);
            if (mLoaded) {
                return;
            }
            File file = mContext.getFileStreamPath(mName);
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                mFileChannel = randomAccessFile.getChannel();
                mMappedByteBuffer = mFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, mMaxSize * SharedEntry.MAX_SIZE);
                incrementLoadFromDisk();
            } catch (IOException e) {
                Logger.e(TAG, e);
            }
            Logger.d(TAG, "loadFromDisk successfully ");
            mLoaded = true;
            notifyAll();
        }
    }

    private void lockedRun(Runnable run) {
        lockedRun(run, 0L, Long.MAX_VALUE);
    }

    private void lockedRun(Runnable run, long position, long size) {
        FileLock lock = null;
        try {
            lock = mFileChannel.lock(position, size, false);
            run.run();
        } catch (IOException e) {
            Logger.e(TAG, e);
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                } catch (IOException e) {
                    Logger.e(TAG, e);
                }
            }
        }
    }

    private void incrementLoadFromDisk() {
        int surplus = mMaxSize - mSharedEntries.size();
        for (int i = 0; i < surplus; i++) {
            try {
                SharedEntry sharedEntry = new SharedEntry(mMappedByteBuffer, mCurrentPosition);
                mSharedEntries.put(sharedEntry.getKey(), sharedEntry);
                mCurrentPosition = mCurrentPosition + SharedEntry.MAX_SIZE;
            } catch (IllegalArgumentException e) {
                break;
            }
        }
    }

    private void incrementPutValue(String key, int valueType, Object value) {
        lockedRun(new Runnable() {
            @Override
            public void run() {
                incrementLoadFromDisk();
                SharedEntry entry = mSharedEntries.get(key);
                if (entry == null) {
                    entry = new SharedEntry(mMappedByteBuffer, mCurrentPosition, key);
                    mSharedEntries.put(key, entry);
                }
                entry.putObject(mMappedByteBuffer, valueType, value);
            }
        });
    }

    private Object incrementGetValue(String key) {
        final Object[] result = {null};
        lockedRun(new Runnable() {
            @Override
            public void run() {
                incrementLoadFromDisk();
                SharedEntry entry = mSharedEntries.get(key);
                if (entry != null) {
                    result[0] = entry.getValue(mMappedByteBuffer);
                }
            }
        });
        return result[0];
    }

    private Object getValue(String key) {
        SharedEntry entry = mSharedEntries.get(key);
        if (entry != null) {
            final Object[] result = new Object[1];
            SharedEntry finalEntry = entry;
            lockedRun(new Runnable() {
                @Override
                public void run() {
                    result[0] = finalEntry.getValue(mMappedByteBuffer);
                }
            }, entry.getPosition(), SharedEntry.MAX_SIZE);
            return result[0];
        } else {
            return incrementGetValue(key);
        }
    }

    private void putValue(String key, int valueType, Object value) {
        SharedEntry entry = mSharedEntries.get(key);
        if (entry != null) {
            lockedRun(new Runnable() {
                @Override
                public void run() {
                    entry.putObject(mMappedByteBuffer, valueType, value);
                }
            }, entry.getPosition(), SharedEntry.MAX_SIZE);
        } else {
            incrementPutValue(key, valueType, value);
        }
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            String value = (String) getValue(key);
            return value != null ? value : defValue;
        }
    }

    @Override
    public int getInt(String key, int defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Integer value = (Integer) getValue(key);
            return value != null ? value : defValue;
        }
    }

    @Override
    public long getLong(String key, long defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Long value = (Long) getValue(key);
            return value != null ? value : defValue;
        }
    }

    @Override
    public float getFloat(String key, float defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Float value = (Float) getValue(key);
            return value != null ? value : defValue;
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Boolean value = (Boolean) getValue(key);
            return value != null ? value : defValue;
        }
    }

    @Override
    public void putString(String key, @Nullable String value) {
        synchronized (this) {
            awaitLoadedLocked();
            putValue(key, SharedEntry.VALUE_TYPE_STRING, value);
        }
    }

    @Override
    public void putInt(String key, int value) {
        synchronized (this) {
            awaitLoadedLocked();
            putValue(key, SharedEntry.VALUE_TYPE_INT, value);
        }
    }

    @Override
    public void putLong(String key, long value) {
        synchronized (this) {
            awaitLoadedLocked();
            putValue(key, SharedEntry.VALUE_TYPE_LONG, value);
        }
    }

    @Override
    public void putFloat(String key, float value) {
        synchronized (this) {
            awaitLoadedLocked();
            putValue(key, SharedEntry.VALUE_TYPE_FLOAT, value);
        }
    }

    @Override
    public void putBoolean(String key, boolean value) {
        synchronized (this) {
            awaitLoadedLocked();
            putValue(key, SharedEntry.VALUE_TYPE_BOOLEAN, value);
        }
    }

    @Override
    public long getAndIncrement(String key, long startValue) {
        return getAndAdd(key, 1, startValue);
    }

    @Override
    public long getAndAdd(String key, long delta, long startValue) {
        synchronized (this) {
            awaitLoadedLocked();
            final long[] result = new long[1];
            SharedEntry entry = mSharedEntries.get(key);
            if (entry != null) {
                lockedRun(new Runnable() {
                    @Override
                    public void run() {
                        result[0] = (long) entry.getValue(mMappedByteBuffer);
                        entry.putLong(mMappedByteBuffer, result[0] + delta);
                    }
                }, entry.getPosition(), SharedEntry.MAX_SIZE);
                return result[0];
            } else {
                putValue(key, SharedEntry.VALUE_TYPE_LONG, startValue);
                return startValue;
            }
        }
    }
}
