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


package com.growingio.android.snappy;

import java.lang.ref.SoftReference;

/**
 * Simple helper class to encapsulate details of basic buffer
 * recycling scheme, which helps a lot (as per profiling) for
 * smaller encoding cases.
 *
 * @author tatu
 */
class BufferRecycler {
    private static final int MIN_ENCODING_BUFFER = 4000;

    private static final int MIN_OUTPUT_BUFFER = 8000;

    /**
     * This <code>ThreadLocal</code> contains a {@link SoftReference}
     * to a {@link BufferRecycler} used to provide a low-cost
     * buffer recycling for buffers we need for encoding, decoding.
     */
    protected static final ThreadLocal<SoftReference<BufferRecycler>> RECYCLER_REF = new ThreadLocal<SoftReference<BufferRecycler>>();

    private short[] encodingHash;

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


    public short[] allocEncodingHash(int suggestedSize) {
        short[] buf = encodingHash;
        if (buf == null || buf.length < suggestedSize) {
            buf = new short[suggestedSize];
        } else {
            encodingHash = null;
        }
        return buf;
    }

    public void releaseEncodingHash(short[] buffer) {
        if (encodingHash == null || (buffer != null && buffer.length > encodingHash.length)) {
            encodingHash = buffer;
        }
    }

    ///////////////////////////////////////////////////////////////////////
    // Buffers for decoding (input)
    ///////////////////////////////////////////////////////////////////////

}
