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

package com.growingio.android.sdk.track.snappy;

import java.lang.ref.SoftReference;

/**
 * Simple helper class to encapsulate details of basic buffer
 * recycling scheme, which helps a lot (as per profiling) for
 * smaller encoding cases.
 */
class BufferRecycler {
    /**
     * This <code>ThreadLocal</code> contains a {@link SoftReference}
     * to a {@link BufferRecycler} used to provide a low-cost
     * buffer recycling for buffers we need for encoding, decoding.
     */
    protected static final ThreadLocal<SoftReference<BufferRecycler>> RECYCLER_REF = new ThreadLocal<>();
    private static final int MIN_ENCODING_BUFFER = 4000;
    private static final int MIN_OUTPUT_BUFFER = 8000;
    private byte[] mInputBuffer;
    private byte[] mOutputBuffer;

    private byte[] mDecodingBuffer;
    private byte[] mEncodingBuffer;

    private short[] mEncodingHash;

    /**
     * Accessor to get thread-local recycler instance
     */
    public static BufferRecycler instance() {
        SoftReference<BufferRecycler> ref = RECYCLER_REF.get();

        BufferRecycler bufferRecycler;
        if (ref == null) {
            bufferRecycler = null;
        } else {
            bufferRecycler = ref.get();
        }

        if (bufferRecycler == null) {
            bufferRecycler = new BufferRecycler();
            RECYCLER_REF.set(new SoftReference<BufferRecycler>(bufferRecycler));
        }
        return bufferRecycler;
    }

    public void clear() {
        mInputBuffer = null;
        mOutputBuffer = null;
        mDecodingBuffer = null;
        mEncodingBuffer = null;
        mEncodingHash = null;
    }

    ///////////////////////////////////////////////////////////////////////
    // Buffers for encoding (output)
    ///////////////////////////////////////////////////////////////////////

    public byte[] allocEncodingBuffer(int minSize) {
        byte[] buf = mEncodingBuffer;
        if (buf == null || buf.length < minSize) {
            buf = new byte[Math.max(minSize, MIN_ENCODING_BUFFER)];
        } else {
            mEncodingBuffer = null;
        }
        return buf;
    }

    public void releaseEncodeBuffer(byte[] buffer) {
        if (mEncodingBuffer == null || buffer.length > mEncodingBuffer.length) {
            mEncodingBuffer = buffer;
        }
    }

    public byte[] allocOutputBuffer(int minSize) {
        byte[] buf = mOutputBuffer;
        if (buf == null || buf.length < minSize) {
            buf = new byte[Math.max(minSize, MIN_OUTPUT_BUFFER)];
        } else {
            mOutputBuffer = null;
        }
        return buf;
    }

    public void releaseOutputBuffer(byte[] buffer) {
        if (mOutputBuffer == null || (buffer != null && buffer.length > mOutputBuffer.length)) {
            mOutputBuffer = buffer;
        }
    }

    public short[] allocEncodingHash(int suggestedSize) {
        short[] buf = mEncodingHash;
        if (buf == null || buf.length < suggestedSize) {
            buf = new short[suggestedSize];
        } else {
            mEncodingHash = null;
        }
        return buf;
    }

    public void releaseEncodingHash(short[] buffer) {
        if (mEncodingHash == null || (buffer != null && buffer.length > mEncodingHash.length)) {
            mEncodingHash = buffer;
        }
    }

    ///////////////////////////////////////////////////////////////////////
    // Buffers for decoding (input)
    ///////////////////////////////////////////////////////////////////////

    public byte[] allocInputBuffer(int minSize) {
        byte[] buf = mInputBuffer;
        if (buf == null || buf.length < minSize) {
            buf = new byte[Math.max(minSize, MIN_OUTPUT_BUFFER)];
        } else {
            mInputBuffer = null;
        }
        return buf;
    }

    public void releaseInputBuffer(byte[] buffer) {
        if (mInputBuffer == null || (buffer != null && buffer.length > mInputBuffer.length)) {
            mInputBuffer = buffer;
        }
    }

    public byte[] allocDecodeBuffer(int size) {
        byte[] buf = mDecodingBuffer;
        if (buf == null || buf.length < size) {
            buf = new byte[size];
        } else {
            mDecodingBuffer = null;
        }
        return buf;
    }

    public void releaseDecodeBuffer(byte[] buffer) {
        if (mDecodingBuffer == null || (buffer != null && buffer.length > mDecodingBuffer.length)) {
            mDecodingBuffer = buffer;
        }
    }
}
