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

package com.growingio.android.sdk.track.log;

import com.google.common.truth.Truth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

@RunWith(PowerMockRunner.class)
public class CircularFifoQueueTest {

    @Test
    public void testQueue() throws IOException, ClassNotFoundException {
        CircularFifoQueue<String> queue = new CircularFifoQueue<>(Arrays.asList("1", "2", "3"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(queue);
        ByteArrayInputStream bis = new ByteArrayInputStream((byte[]) bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        queue = (CircularFifoQueue<String>) ois.readObject();

        Truth.assertThat(queue.element()).isEqualTo("1");
        Truth.assertThat(queue.poll()).isEqualTo("1");
        Truth.assertThat(queue.offer("1")).isTrue();
        queue.add("4");
        Truth.assertThat("4").isEqualTo(queue.get(2));
        Truth.assertThat(queue.isAtFullCapacity()).isTrue();
        queue.remove("4");
        queue.removeAll(Arrays.asList("3", "1"));
        queue.clear();
        Truth.assertThat(queue.isEmpty()).isTrue();
        Truth.assertThat(queue.maxSize() == 3).isTrue();
        Truth.assertThat(queue.ismFull()).isFalse();
    }
}
