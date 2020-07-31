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

import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.growingio.android.sdk.track.utils.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 变量共享者， 使用文件映射内存的形式共享变量
 * <p>
 * /magic_num/process_num/process_id/process_id/process_id/..../
 * /modCount/ modCount   / modCount /...
 * /len     / xxxxxxx/
 * - magic_num: 0x13e: 表明此文件用于VariableSharer ---> short  2字节
 * - process_num: 当前进程数量                      ---> short  2字节
 * - process_id: 其中的一个进程进程号                ----> int   4字节
 * 进程号最多有10进程， 占位40字节
 * - modCount: 总体的modCount, 单个Index的modCount
 * <p>
 * <p>
 * 目前VariableSharer中有三种锁, lock data, lock meta, lock process, lock mByteBuffer
 * <p>
 * 如果涉及多个锁时， 获取锁顺序为: lock mByteBuffer --> lock data --> lock meta 其他锁不允许交叉获取
 * <p>
 */
public class VariableSharer {
    private static final String TAG = "GIO.Sharer";

    @VisibleForTesting
    private static final short MAGIC_NUM = 0x13e;   // short int
    // 初始化完成后， 开始并发读, 所以不会有多线程问题
    @VisibleForTesting
    List<VariableEntity> mEntityList = new ArrayList<>();
    @VisibleForTesting
    int mMetaBaseAddress = 44;             // 原信息基地址(modCount的开始位置) 2 + 2 + 40
    @VisibleForTesting
    int mVariableBaseAddress = -1;         // 变量存储区的基地址
    int mPid;
    @VisibleForTesting
    ByteBuffer mByteBuffer;
    boolean mIsFirstInit = true;                         // 是不是冷启动
    private int mTotalModCount = -1;
    private int mCurrentVariableIndex = 0;
    private int mCurrentVariableOffset = 0;        // 当前变量位置的偏移量
    private boolean mUsingMultiProcess;            // 是否使用多进程
    private FileChannel mFileChannel;

    /**
     * @param file 共享文件
     */
    public VariableSharer(File file, boolean usingMultiProcess, int pid) {
        this.mUsingMultiProcess = usingMultiProcess;
        mPid = pid;
        if (usingMultiProcess) {
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                mFileChannel = randomAccessFile.getChannel();
            } catch (FileNotFoundException e) {
                LogUtil.e(TAG, e, "多进程共享初始化失败: ");
                this.mUsingMultiProcess = false;
            }
        }
    }

    public void destroy() {
        if (mFileChannel != null) {
            try {
                mFileChannel.close();
            } catch (IOException e) {
                LogUtil.e(TAG, "close failed");
            } finally {
                mFileChannel = null;
                mByteBuffer = null;
                mUsingMultiProcess = false;
            }
        }
    }

    public boolean isFirstInit() {
        return mIsFirstInit;
    }

    /**
     * 向变量表中添加一个新的Item, 并计算index与位置偏移量
     *
     * @return 返回改Item对应的下表
     */
    public int addVariableEntity(@NonNull VariableEntity entity) {
        entity.setIndex(mCurrentVariableIndex++);
        mEntityList.add(entity);
        entity.setStart(mCurrentVariableOffset);
        mCurrentVariableOffset += entity.getMaxSize() + entity.getLenSize();
        entity.setEnd(mCurrentVariableOffset);
        entity.setChanged(true);
        return entity.getIndex();
    }

    /**
     * 原信息补充完全，开始计算各基地址与偏移量
     */
    public void completeMetaData(final Set<Integer> runningProcess) {
        mVariableBaseAddress = mMetaBaseAddress + mCurrentVariableIndex * 4 + 4;
        if (mUsingMultiProcess) {
            try {
                mByteBuffer = mFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, mCurrentVariableOffset + mVariableBaseAddress);
                withLockProcessArea(new Runnable() {
                    @Override
                    public void run() {
                        checkOrPrepareMagic();
                        repairPid(runningProcess);
                    }
                });
            } catch (IOException e) {
                LogUtil.e(TAG, "多进程映射内存失败: ", e);
                mUsingMultiProcess = false;
            }
        }
    }

    // 整理修复Pid列表
    void repairPid(Set<Integer> runningProcess) {
        List<Integer> alivePid = getAlivePidWithLock(new ArrayList<Integer>(), runningProcess);
        if (alivePid.size() >= 10) {
            LogUtil.e(TAG, "alivePid num large than 10, failed");
            mUsingMultiProcess = false;
            return;
        }
        if (alivePid.size() == 0) {
            LogUtil.d(TAG, "find first init process, and reset variable");
            mIsFirstInit = true;
        } else {
            mIsFirstInit = false;
        }
        alivePid.add(mPid);
        mByteBuffer.position(2);
        mByteBuffer.putShort((short) alivePid.size());
        for (int pid : alivePid) {
            mByteBuffer.putInt(pid);
        }
    }

    @NonNull
    private List<Integer> getAlivePidWithLock(List<Integer> alivePid, Set<Integer> runningProcess) {
        int processNum = mByteBuffer.getShort(2);
        for (int i = 0; i < processNum; i++) {
            int pid = mByteBuffer.getInt(4 + i * 4);
            if (pid < 0) {
                continue;
            }
            if (runningProcess != null && runningProcess.contains(pid)) {
                alivePid.add(pid);
            }
        }
        return alivePid;
    }

    public List<Integer> getAlivePid(final Set<Integer> runningProcess) {
        final List<Integer> alivePid = new ArrayList<>();
        if (mUsingMultiProcess) {
            withLockProcessArea(new Runnable() {
                @Override
                public void run() {
                    getAlivePidWithLock(alivePid, runningProcess);
                }
            });
        } else {
            alivePid.add(Process.myPid());
        }
        return alivePid;
    }

    @VisibleForTesting
    void checkOrPrepareMagic() {
        mByteBuffer.rewind();
        try {
            int magicNum = mByteBuffer.getShort();
            if (magicNum == 0) {
                LogUtil.d(TAG, "first init multi process file");
                prepareMagic();
            } else if (magicNum != MAGIC_NUM) {
                LogUtil.e(TAG, "文件校验失败, 多进程共享失败");
                mUsingMultiProcess = false;
            }
        } catch (BufferUnderflowException e) {
            prepareMagic();
        }
    }

    public boolean isUsingMultiProcess() {
        return mUsingMultiProcess;
    }

    private void prepareMagic() {
        mByteBuffer.rewind();
        mByteBuffer.putShort(MAGIC_NUM);
    }

    public long getLongByIndex(int index) {
        checkEntityChanged();
        final VariableEntity entity = mEntityList.get(index);
        synchronized (this) {
            if (mUsingMultiProcess && entity.isChanged()) {
                withLockData(new Runnable() {
                    @Override
                    public void run() {
                        long result = mByteBuffer.getLong(mVariableBaseAddress + entity.getStart() + entity.getLenSize());
                        entity.setObj(result);
                        entity.setChanged(false);
                    }
                }, entity);
            }
            return entity.getObj() == null ? 0 : (long) entity.getObj();
        }
    }

    public void putLongByIndex(int index, final long value) {
        final VariableEntity entity = mEntityList.get(index);
        synchronized (this) {
            entity.setObj(value);
            if (mUsingMultiProcess) {
                withLockData(new Runnable() {
                    @Override
                    public void run() {
                        mByteBuffer.putLong(mVariableBaseAddress + entity.getStart() + entity.getLenSize(), value);
                        updateMetaWithLock(entity);
                    }
                }, entity);
            }
        }
    }

    public boolean compareAndSetIntByIndex(int index, final int oldValue, final int newValue) {
        final VariableEntity entity = mEntityList.get(index);
        final AtomicBoolean result = new AtomicBoolean();
        synchronized (this) {
            if (mUsingMultiProcess) {
                withLockData(new Runnable() {
                    @Override
                    public void run() {
                        int value = mByteBuffer.getInt(mVariableBaseAddress + entity.getStart() + entity.getLenSize());
                        if (value == oldValue) {
                            result.set(true);
                            mByteBuffer.putInt(mVariableBaseAddress + entity.getStart() + entity.getLenSize(), newValue);
                            updateMetaWithLock(entity);
                        } else {
                            result.set(false);
                        }
                    }
                }, entity);
            } else {
                if (Integer.valueOf(oldValue).equals(entity.getObj())) {
                    entity.setObj(newValue);
                    return true;
                }
            }
        }
        return result.get();
    }

    /**
     * 根据变量的index获取属性值int值
     */
    public int getIntByIndex(int index) {
        checkEntityChanged();
        final VariableEntity entity = mEntityList.get(index);
        synchronized (this) {
            if (mUsingMultiProcess && entity.isChanged()) {
                withLockData(new Runnable() {
                    @Override
                    public void run() {
                        int result = mByteBuffer.getInt(mVariableBaseAddress + entity.getStart() + entity.getLenSize());
                        entity.setObj(result);
                        entity.setChanged(false);
                    }
                }, entity);
            }
            if (entity.getObj() == null) return 0;
            return entity.getObj() == null ? 0 : (int) entity.getObj();
        }
    }

    public void putIntByIndex(int index, final int value) {
        final VariableEntity entity = mEntityList.get(index);
        synchronized (this) {
            entity.setObj(value);

            if (mUsingMultiProcess) {
                withLockData(new Runnable() {
                    @Override
                    public void run() {
                        mByteBuffer.putInt(mVariableBaseAddress + entity.getStart() + entity.getLenSize(), value);
                        updateMetaWithLock(entity);
                    }
                }, entity);
            }
        }
    }

    public void putStringByIndex(int index, @Nullable String value) {
        putDataByIndex(index, value == null ? null : value.getBytes());
    }

    public String getStringByIndex(int index) {
        byte[] data = getDataByIndex(index);
        if (data == null || data.length == 0)
            return null;
        return new String(data);
    }

    public void putDataByIndex(int index, @Nullable final byte[] bytes) {
        final VariableEntity entity = mEntityList.get(index);
        synchronized (this) {
            entity.setObj(bytes);

            if (mUsingMultiProcess) {
                withLockData(new Runnable() {
                    @Override
                    public void run() {
                        int len = bytes == null ? 0 : bytes.length;
                        if (entity.getLenSize() == 2) {
                            mByteBuffer.putShort((short) len);
                        } else if (entity.getLenSize() == 4) {
                            mByteBuffer.putInt(len);
                        } else {
                            throw new IllegalStateException("String type len must be 2 or 4");
                        }
                        if (bytes != null) {
                            mByteBuffer.put(bytes);
                        }
                        updateMetaWithLock(entity);

                    }
                }, entity);
            }
        }
    }

    public byte[] getDataByIndex(final int index) {
        checkEntityChanged();
        final VariableEntity entity = mEntityList.get(index);
        synchronized (this) {
            if (mUsingMultiProcess && entity.isChanged()) {
                withLockData(new Runnable() {
                    @Override
                    public void run() {
                        short len = mByteBuffer.getShort();
                        byte[] result;
                        if (len == 0) {
                            result = null;
                        } else {
                            result = new byte[len];
                            mByteBuffer.get(result);
                        }
                        entity.setObj(result);
                        entity.setChanged(false);
                    }
                }, entity);
            }
            return (byte[]) entity.getObj();
        }
    }

    private void updateMetaWithLock(final VariableEntity entity) {
        withLockMeta(new Runnable() {
            @Override
            public void run() {
                int modCount = mByteBuffer.getInt(mMetaBaseAddress);
                if (modCount != mTotalModCount) {
                    //  other process has update mod count
                    checkEntityChanged();
                }
                int entityModCountIndex = mMetaBaseAddress + (entity.getIndex() + 1) * 4;
                int indexModCount = mByteBuffer.getInt(entityModCountIndex);
                mByteBuffer.putInt(mMetaBaseAddress, ++modCount);
                mTotalModCount = modCount;

                mByteBuffer.putInt(entityModCountIndex, ++indexModCount);
                entity.setModCount(indexModCount);
            }
        });
    }

    private void withLockData(Runnable runnable, VariableEntity entity) {
        int position = mVariableBaseAddress + entity.getStart();
        FileLock lock = null;
        try {
            mByteBuffer.position(position);
            lock = mFileChannel.lock(position, entity.getMaxSize(), false);
            runnable.run();
        } catch (Exception e) {
            LogUtil.e(TAG, "数据区加锁失败: ", e);
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /* lock meta时已经全部lock data完成， 不需要额外加锁synchronized */
    private void withLockMeta(Runnable runnable) {
        FileLock lock = null;
        try {
            lock = mFileChannel.lock(mMetaBaseAddress, mVariableBaseAddress - mMetaBaseAddress, false);
            runnable.run();
        } catch (Exception e) {
            LogUtil.e(TAG, "文件原信息失败", e);
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    private void withLockProcessArea(Runnable runnable) {
        FileLock lock = null;
        try {
            lock = mFileChannel.lock(0, mMetaBaseAddress, false);
            runnable.run();
        } catch (Exception e) {
            LogUtil.e(TAG, "文件进程区加锁失败", e);
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    // 作为一个纯读操作， 并不需要文件锁
    @VisibleForTesting
    void checkEntityChanged() {
        if (!mUsingMultiProcess) return;
        try {
            synchronized (this) {
                mByteBuffer.position(mMetaBaseAddress);
                int modCount = mByteBuffer.getInt();
                if (mTotalModCount == modCount)
                    // 没有任何改动
                    return;

                for (VariableEntity entity : mEntityList) {
                    int mod = mByteBuffer.getInt();
                    if (mod != entity.getModCount()) {
                        entity.setModCount(mod);
                        entity.setChanged(true);
                    }
                }
                mTotalModCount = modCount;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e, "check changed failed: ");
        }
    }

    public void dumpModCountInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append("dumpModCountInfo: \n");
        synchronized (this) {
            mByteBuffer.position(mMetaBaseAddress);
            int modCount = mByteBuffer.getInt();
            builder.append("modCount=").append(modCount).append("\n");
            for (VariableEntity entity : mEntityList) {
                builder.append(entity.getName()).append("'s modCount=").append(mByteBuffer.getInt()).append("\n");
            }
        }
        builder.append(")");
        LogUtil.d(TAG, builder.toString());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VariableSharer(");
        builder.append("totalModCount=").append(mTotalModCount).append(", \n");
        for (VariableEntity entity : mEntityList) {
            builder.append(entity.getName()).append("=").append(entity.getObj()).append("\n");
        }
        builder.append(")");
        return builder.toString();
    }
}
