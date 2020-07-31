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

import android.support.annotation.NonNull;

/**
 * 表示一个位于VariableSharer的变量meta-data
 */
public class VariableEntity {

    private final String mName;
    private final int mMaxSize;
    private boolean mPersistent = false;   // 此变量是否持久化， 在冷启动时是否需要保留
    private int mIndex = -1;               // 下标， 对应在VariableSharer标量表中的下表位置
    private int mStart = -1;
    private int mEnd = -1;     // 表示此变量位于文件位置
    private int mModCount = -1;            // 当前的modCount
    private boolean mIsChanged = false;
    private int mLenSize = 2;
    private Object mObj;

    /**
     * @param name    变量的key
     * @param maxSize 变量值最大占位数
     */
    public VariableEntity(@NonNull String name, int maxSize) {
        this.mName = name;
        this.mMaxSize = maxSize;
    }

    public static VariableEntity createIntVariable(String variableName) {
        VariableEntity entity = new VariableEntity(variableName, 4);
        entity.setLenSize(0);
        return entity;
    }

    public static VariableEntity createStringVariable(String variableName, int strLen) {
        VariableEntity entity = new VariableEntity(variableName, 4 * strLen);
        entity.setLenSize(2);
        return entity;
    }

    public static VariableEntity createLongVariable(String variableName) {
        VariableEntity entity = new VariableEntity(variableName, 8);
        entity.setLenSize(0);
        return entity;
    }

    public String getName() {
        return mName;
    }

    public int getLenSize() {
        return mLenSize;
    }

    public void setLenSize(int lenSize) {
        this.mLenSize = lenSize;
    }

    public boolean isPersistent() {
        return mPersistent;
    }

    public void setPersistent(boolean persistent) {
        this.mPersistent = persistent;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        this.mIndex = index;
    }

    public int getMaxSize() {
        return mMaxSize;
    }

    public int getStart() {
        return mStart;
    }

    public void setStart(int start) {
        this.mStart = start;
    }

    public int getEnd() {
        return mEnd;
    }

    public void setEnd(int end) {
        this.mEnd = end;
    }

    public int getModCount() {
        return mModCount;
    }

    public void setModCount(int modCount) {
        this.mModCount = modCount;
    }

    public Object getObj() {
        return mObj;
    }

    public void setObj(Object obj) {
        this.mObj = obj;
    }

    public boolean isChanged() {
        return mIsChanged;
    }

    public void setChanged(boolean changed) {
        mIsChanged = changed;
    }
}
