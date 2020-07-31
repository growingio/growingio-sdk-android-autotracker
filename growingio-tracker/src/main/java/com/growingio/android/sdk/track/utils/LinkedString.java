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

package com.growingio.android.sdk.track.utils;

import java.lang.ref.WeakReference;

/**
 * 使用链表替代基于Array的String
 * 为了减少对象的分配与复制, 此String与标准String与很多不同， 使用该类前， 请确保了解该类的源码
 */
public class LinkedString {

    private LinkedString mHeadLinkedString;
    private LinkedNode mHead;
    private LinkedNode mTail;
    private int mSize;
    private WeakReference<String> mValueRef;
    private int mHash;   // The cached hash value

    /**
     * Not Deep copy, make sure you know everything
     */
    public static LinkedString copy(LinkedString linkedString) {
        LinkedString result = new LinkedString();
        if (linkedString != null) {
            result.mHeadLinkedString = linkedString;
            result.mSize = linkedString.mSize;
            result.mValueRef = linkedString.mValueRef;
        }
        return result;
    }

    public static LinkedString fromString(String value) {
        LinkedString result = new LinkedString();
        if (value != null) {
            result.append(value);
            result.mValueRef = new WeakReference<>(value);
        }
        return result;
    }

    public LinkedString append(String str) {
        if (str.length() == 0)
            return this;
        LinkedNode node = new LinkedNode();
        node.mValue = str;
        mSize += str.length();
        if (mHead == null) {
            mHead = node;
            mTail = node;
        } else {
            mTail.mNext = node;
            mTail = node;
        }
        mValueRef = null;
        mHash = 0;
        return this;
    }

    public int length() {
        return mSize;
    }

    public LinkedStringIterator iterator() {
        return new LinkedStringIterator(0);
    }

    public LinkedString append(Object any) {
        if (any == null)
            return this;
        return append(any.toString());
    }

    public String toStringValue() {
        if (mValueRef != null && mValueRef.get() != null) {
            return mValueRef.get();
        }

        if (mHeadLinkedString == null && mHead == mTail) {
            // 为了LinkedString.fromString 生成的String提供一个快速的方法
            mValueRef = new WeakReference<>(mHead.mValue);
            return mValueRef.get();
        }

        StringBuilder builder = new StringBuilder(length());
        if (mHeadLinkedString != null) {
            builder.append(mHeadLinkedString.toStringValue());
        }

        if (mHead != null) {
            LinkedNode current = mHead;
            while (current != null) {
                builder.append(current.mValue);
                if (current != mTail) {
                    current = current.mNext;
                } else {
                    break;
                }
            }
        }
        String result = builder.toString();
        mValueRef = new WeakReference<>(result);
        return result;
    }

    public boolean endsWith(String end) {
        if (end.length() > mSize)
            return false;
        LinkedStringIterator iterator = new LinkedStringIterator(mSize - end.length());
        int charIndex = 0;
        if (!iterator.hasNext())
            return false;
        while (iterator.hasNext()) {
            if (end.charAt(charIndex) != iterator.next())
                return false;
            charIndex++;
        }
        return true;
    }

    public char first() {
        if (mHeadLinkedString != null && mHeadLinkedString.mSize > 0)
            return mHeadLinkedString.first();
        if (mHead == null)
            throw new IllegalStateException("mHead should not be null");
        return mHead.mValue.charAt(0);
    }

    public char end() {
        if (mTail == null && mHeadLinkedString != null) {
            return mHeadLinkedString.end();
        }
        if (mTail == null) {
            throw new IllegalStateException("mTail should not be null");
        }
        return mTail.mValue.charAt(mTail.mValue.length() - 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LinkedString) {
            if (length() != ((LinkedString) obj).length())
                return false;
            LinkedStringIterator iterator = iterator();
            LinkedStringIterator objIterator = ((LinkedString) obj).iterator();
            while (iterator.hasNext()) {
                if (!objIterator.hasNext()
                        || iterator.next() != objIterator.next())
                    return false;
            }
            return !objIterator.hasNext();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = mHash;
        if (h == 0 && mSize != 0) {
            LinkedStringIterator iterator = iterator();
            while (iterator.hasNext()) {
                h = 31 * h + iterator.next();
            }
            mHash = h;
        }
        return h;
    }

    @Override
    public String toString() {
        // TODO: 这个方法不应该被使用， 作为一个最后的保护
        return toStringValue();
    }

    private static class LinkedNode {
        private LinkedNode mNext;
        private String mValue;
    }

    public class LinkedStringIterator {
        private LinkedNode mCurrentNode;
        private int mCurrentIndex;
        private int mCurrentStrOffset;
        private boolean mHasNext = false;
        private LinkedStringIterator mHeadIterator;

        private LinkedStringIterator(int offset) {
            this.mCurrentStrOffset = offset;
            if (mHeadLinkedString != null) {
                if (this.mCurrentStrOffset >= mHeadLinkedString.mSize) {
                    // 比头String还要大
                    this.mCurrentStrOffset = this.mCurrentStrOffset - mHeadLinkedString.mSize;
                } else {
                    mHeadIterator = mHeadLinkedString.new LinkedStringIterator(this.mCurrentStrOffset);
                    this.mCurrentStrOffset = 0;
                }
            }

            if (mHeadIterator == null) {
                firstCalRightIndex();
            } else {
                mHasNext = true;
            }
        }

        private void firstCalRightIndex() {
            calculateRightCurrentNodeByOffset();
            findRightIndex();
        }

        public boolean hasNext() {
            return mHasNext;
        }

        public char next() {
            boolean headHasNex = mHeadIterator != null && mHeadIterator.hasNext();

            char result;
            if (headHasNex) {
                // 有Head
                result = mHeadIterator.next();
                if (!mHeadIterator.hasNext()) {
                    firstCalRightIndex();
                }
            } else {
                // 没有head
                result = mCurrentNode.mValue.charAt(mCurrentIndex);
                mCurrentIndex++;
                findRightIndex();
            }
            return result;
        }

        private void calculateRightCurrentNodeByOffset() {
            int currentOffset = 0;
            LinkedNode current = mHead;
            int endIndex;
            while (current != null) {
                endIndex = currentOffset + current.mValue.length();
                if (endIndex > mCurrentStrOffset) {
                    // 节点发生在该Node
                    mCurrentIndex = mCurrentStrOffset - currentOffset;
                    break;
                }
                if (current == mTail) {
                    // overt
                    current = null;
                } else {
                    currentOffset += current.mValue.length();
                    current = current.mNext;
                }
            }
            mCurrentNode = current;
        }

        private void findRightIndex() {
            if (mCurrentNode == null)
                return;
            if (mCurrentIndex == mCurrentNode.mValue.length()) {
                // 可能会越界
                mCurrentIndex = 0;
                if (mCurrentNode == mTail) {
                    // over
                    mCurrentNode = null;
                } else {
                    mCurrentNode = mCurrentNode.mNext;
                }
            }
            mHasNext = mCurrentNode != null;
        }
    }
}
