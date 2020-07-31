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

import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * An value in a <tt>WeakSet</tt> will automatically be removed when
 * it is no longer in ordinary use.
 *
 *
 * <strong>This class not thread-safe</strong>
 * <strong>此类暂时没有支持所有的方法， 如有需要， 请自行添加</strong>
 *
 * @see java.util.WeakHashMap
 */
public class WeakSet<T> implements Set<T> {

    private static final Object PRESENT = new Object();
    private transient WeakHashMap<T, Object> mMap;

    @Override
    public int size() {
        if (mMap == null)
            return 0;
        return mMap.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (isEmpty() || o == null)
            return false;
        return mMap.containsKey(o);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
        if (isEmpty())
            return EmptyIterator.EMPTY_ITERATOR;
        return mMap.keySet().iterator();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("method toArray() not supported");
    }

    @NonNull
    @Override
    public <T1> T1[] toArray(@NonNull T1[] a) {
        throw new UnsupportedOperationException("method toArray(T[] a) not supported");
    }

    @Override
    public boolean add(T t) {
        if (t == null) {
            throw new IllegalArgumentException("The argument t can't be null");
        }
        if (mMap == null)
            mMap = new WeakHashMap<>();
        return mMap.put(t, PRESENT) != null;
    }

    @Override
    public boolean remove(Object o) {
        if (isEmpty() || o == null)
            return false;
        return mMap.remove(o) == PRESENT;
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        throw new UnsupportedOperationException("method containsAll not supported");
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> c) {
        throw new UnsupportedOperationException("method addAll not supported now");
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        throw new UnsupportedOperationException("method retainAll not supported now");
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        throw new UnsupportedOperationException("method removeAll not supported now");
    }

    @Override
    public void clear() {
        if (mMap != null)
            mMap.clear();
    }

    private static class NonEmptyIterator<E> implements Iterator<E> {

        private final Iterator<WeakReference<E>> mIterator;

        private NonEmptyIterator(Iterator<WeakReference<E>> iterator) {
            this.mIterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return mIterator.hasNext();
        }

        @Override
        public E next() {
            return mIterator.next().get();
        }
    }

    private static class EmptyIterator<E> implements Iterator<E> {

        private static final EmptyIterator EMPTY_ITERATOR = new EmptyIterator();

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public E next() {
            throw new UnsupportedOperationException("EmptyIterator should not call this method directly");
        }
    }
}
