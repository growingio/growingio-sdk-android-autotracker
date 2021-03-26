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
